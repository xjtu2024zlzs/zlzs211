from __future__ import annotations

from pathlib import Path
from typing import Any

from ..clients.cf_api_pull_client import CfApiPullClient
from ..clients.ruoyi_mysql_client import DossierFileClient, RuoyiMysqlClient
from ..config import (
    SOURCE_SYSTEMS,
    build_default_mysql_url,
    default_ground_truth_root,
    default_target_table_prefix,
    llm_availability,
    normalize_system_key,
)
from ..runtime.cf_match_runner import CfMatchRunner, RunnerConfig
from ..runtime.ground_truth_loader import GroundTruthCatalog
from ..runtime.result_normalizer import build_charts, rows_to_payload
from ..runtime.schema_builder import bundle_to_schema_info
from ..schemas import MatchRunAllRequest, MatchRunRequest, SourceRequest, TargetRequest


class MatchService:
    def capabilities(self) -> dict[str, Any]:
        return {
            "service": "cf-schema-matching-algorithm",
            "methods": ["Magneto", "MagnetoBoost", "MagnetoGPT"],
            "variants": ["all", "one2one"],
            "embeddingModels": ["mpnet", "roberta", "e5", "arctic", "minilm"],
            "encodingModes": [
                "header_values_default",
                "header_values_verbose",
                "header_values_verbose_with_table",
                "header_values_repeat_with_table",
                "header_only",
            ],
            "sourceSystems": [
                {
                    "systemKey": item.key,
                    "sourceLabel": item.label,
                    "schemaKey": item.schema_key,
                    "defaultBaseUrl": item.default_base_url,
                }
                for item in SOURCE_SYSTEMS.values()
            ],
            "outputs": {
                "resultSets": "Method and variant result sets for p1p_match_result_set.",
                "rows": "Field matching result rows for p1p_match_result_row.",
                "metrics": "Legacy CF evaluation metrics for p1p_task_metric.",
                "charts": "Metric chart_data payload.",
            },
            "llm": {
                "defaultModel": "deepseek/deepseek-chat",
                "magnetoGpt": llm_availability("deepseek/deepseek-chat"),
            },
        }

    def run(self, request: MatchRunRequest) -> dict[str, Any]:
        source_key = normalize_system_key(request.source.systemKey)
        source_bundle = self._load_source(request.source, request.requestId)
        target_bundle = self._load_target(request.target)
        source_schema = bundle_to_schema_info(source_bundle, schema_name=source_key)
        target_schema = bundle_to_schema_info(target_bundle, schema_name="dossier")
        ground_truth_catalog = self._load_ground_truth(request.groundTruth.enabled, request.groundTruth.rootPath)
        all_ground_truth = ground_truth_catalog.for_source_system(source_key) if ground_truth_catalog else []
        ground_truth = _scope_ground_truth(all_ground_truth, source_bundle, target_bundle)
        gt_lookup = ground_truth_catalog.lookup() if ground_truth_catalog else {}

        methods, warnings = _requested_methods(request.algorithm)
        variants = _requested_variants(request.algorithm)
        runner_config = self._runner_config(request, default_method=methods[0])
        runner = CfMatchRunner(runner_config)
        method_results = runner.run_methods(
            source_bundle=source_bundle,
            target_bundle=target_bundle,
            source_schema=source_schema,
            target_schema=target_schema,
            ground_truth=ground_truth,
            methods=methods,
        )

        result_sets = []
        for result in method_results:
            if "all" in variants:
                result_sets.append(
                    _result_set_payload(
                        result=result,
                        variant="all",
                        rows=result.rows,
                        metrics=result.metrics.all_metrics,
                        metrics_summary=result.metrics.all_summary,
                        gt_lookup=gt_lookup,
                    )
                )
            if "one2one" in variants:
                result_sets.append(
                    _result_set_payload(
                        result=result,
                        variant="one2one",
                        rows=result.metrics.one_to_one_rows,
                        metrics=result.metrics.one_to_one_metrics,
                        metrics_summary=result.metrics.one_to_one_summary,
                        gt_lookup=gt_lookup,
                    )
                )

        combined_rows = [row for result_set in result_sets for row in result_set["rows"]]
        combined_metrics = [
            {**metric, "resultSetKey": result_set["resultSetKey"]}
            for result_set in result_sets
            for metric in result_set["metrics"]
        ]
        return {
            "requestId": request.requestId,
            "status": "success",
            "sourceSystem": source_bundle.source_label,
            "sourceSchemaKey": source_bundle.schema_key,
            "targetKey": "dossier",
            "methods": methods,
            "variants": variants,
            "warnings": warnings,
            "resultSetCount": len(result_sets),
            "summary": {
                "sourceTableCount": len(source_bundle.dataframes),
                "targetTableCount": len(target_bundle.dataframes),
                "totalRows": len(combined_rows),
                "runtimeSeconds": round(sum(item.runtime_seconds for item in method_results), 3),
                "groundTruthEnabled": request.groundTruth.enabled,
                "groundTruthCount": len(ground_truth),
            },
            "resultSets": result_sets,
            "rows": combined_rows,
            "metrics": combined_metrics,
        }

    def run_all(self, request: MatchRunAllRequest) -> dict[str, Any]:
        sources = request.sources or [
            SourceRequest(systemKey=system.key, baseUrl=system.default_base_url)
            for system in SOURCE_SYSTEMS.values()
        ]
        results = []
        for source in sources:
            single_request = MatchRunRequest(
                requestId=request.requestId,
                source=source,
                target=request.target,
                groundTruth=request.groundTruth,
                algorithm=request.algorithm,
            )
            results.append(self.run(single_request))

        combined_metrics = []
        combined_rows = []
        runtime = 0.0
        combined_result_sets = []
        for item in results:
            combined_rows.extend(item["rows"])
            combined_result_sets.extend(
                {
                    **result_set,
                    "sourceSystem": item["sourceSystem"],
                }
                for result_set in item["resultSets"]
            )
            runtime += item["summary"]["runtimeSeconds"]
            combined_metrics.extend(
                {
                    **metric,
                    "sourceSystem": item["sourceSystem"],
                }
                for metric in item["metrics"]
            )

        return {
            "requestId": request.requestId,
            "status": "success",
            "targetKey": "dossier",
            "methods": _requested_methods(request.algorithm)[0],
            "variants": _requested_variants(request.algorithm),
            "warnings": [warning for item in results for warning in item.get("warnings", [])],
            "resultSetCount": len(combined_result_sets),
            "summary": {
                "sourceSystemCount": len(results),
                "totalRows": len(combined_rows),
                "runtimeSeconds": round(runtime, 3),
            },
            "results": results,
            "resultSets": combined_result_sets,
            "rows": combined_rows,
            "metrics": combined_metrics,
        }

    def _load_source(self, source: SourceRequest, request_id: str | None):
        source_key = normalize_system_key(source.systemKey)
        system = SOURCE_SYSTEMS[source_key]
        client = CfApiPullClient(source.baseUrl or system.default_base_url)
        return client.load_bundle(
            system_key=source_key,
            request_id=request_id,
            tables=source.tables,
            limit_per_table=source.limitPerTable,
        )

    def _load_target(self, target: TargetRequest):
        table_prefix = target.tablePrefix or default_target_table_prefix()
        if target.mode == "file":
            file_root = Path(target.fileRoot) if target.fileRoot else (
                Path(__file__).resolve().parents[2] / "data" / "magneto_cf" / "target-tables"
            )
            return DossierFileClient(file_root=file_root, table_prefix=table_prefix).load_bundle(
                tables=target.tables,
                limit_per_table=target.limitPerTable,
            )
        client = RuoyiMysqlClient(target.mysqlUrl or build_default_mysql_url(), table_prefix=table_prefix)
        return client.load_bundle(tables=target.tables, limit_per_table=target.limitPerTable)

    def _load_ground_truth(self, enabled: bool, root_path: str | None) -> GroundTruthCatalog | None:
        if not enabled:
            return None
        root = Path(root_path) if root_path else default_ground_truth_root()
        return GroundTruthCatalog.from_file_root(root)

    def _runner_config(self, request: MatchRunRequest | MatchRunAllRequest, *, default_method: str) -> RunnerConfig:
        algorithm = request.algorithm
        return RunnerConfig(
            method=default_method,
            embedding_model=algorithm.embeddingModel,
            encoding_mode=algorithm.encodingMode,
            sampling_mode=algorithm.samplingMode,
            sampling_size=algorithm.samplingSize,
            topk=algorithm.topk,
            threshold=algorithm.threshold,
            boost_threshold=algorithm.boostThreshold,
            boost_alpha=algorithm.boostAlpha,
            llm_model=algorithm.llmModel,
            llm_model_kwargs=algorithm.llmModelKwargs,
        )


def _avg_score(rows) -> float:
    if not rows:
        return 0.0
    return round(sum(row.score for row in rows) / len(rows), 6)


def _requested_methods(algorithm) -> tuple[list[str], list[dict[str, Any]]]:
    methods = algorithm.methods or ([algorithm.method] if algorithm.method else ["Magneto", "MagnetoBoost", "MagnetoGPT"])
    deduped = list(dict.fromkeys(methods))
    warnings: list[dict[str, Any]] = []
    if "MagnetoGPT" in deduped:
        llm_status = llm_availability(algorithm.llmModel)
        if not llm_status["available"]:
            deduped = [method for method in deduped if method != "MagnetoGPT"]
            warnings.append(
                {
                    "code": "magnetogpt_skipped_no_key",
                    "method": "MagnetoGPT",
                    "envVar": llm_status.get("envVar"),
                    "message": llm_status.get("message") or "LLM 配置不可用，已跳过 MagnetoGPT。",
                }
            )
    if not deduped:
        deduped = ["Magneto", "MagnetoBoost"]
    return deduped, warnings


def _requested_variants(algorithm) -> list[str]:
    return list(dict.fromkeys(algorithm.variants or ["all", "one2one"]))


def _result_set_payload(
    *,
    result,
    variant: str,
    rows,
    metrics,
    metrics_summary,
    gt_lookup,
) -> dict[str, Any]:
    method = result.method
    result_set_key = f"{method}:{variant}"
    return {
        "resultSetKey": result_set_key,
        "method": method,
        "variant": variant,
        "status": "success",
        "resultSetName": f"{method} CF-SF {variant}",
        "summary": {
            "totalRows": len(rows),
            "avgScore": _avg_score(rows),
            "runtimeSeconds": round(result.runtime_seconds, 3),
            "groundTruthCount": metrics_summary["groundTruthCount"],
            "matchedGroundTruthCount": metrics_summary["matchedGroundTruthCount"],
        },
        "metricsSummary": metrics_summary,
        "rows": rows_to_payload(rows, gt_lookup),
        "metrics": metrics,
        "charts": build_charts(rows),
    }


def _scope_ground_truth(ground_truth, source_bundle, target_bundle):
    source_tables = set(source_bundle.dataframes)
    target_tables = set(target_bundle.dataframes)
    return [
        row
        for row in ground_truth
        if row.source_table in source_tables and row.target_table in target_tables
    ]
