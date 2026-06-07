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
                "rows": "Field matching result rows for p1p_match_result_row.",
                "metrics": "Evaluation metrics for p1p_task_metric.",
                "charts": "Metric chart_data payload.",
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

        runner_config = self._runner_config(request)
        runner = CfMatchRunner(runner_config)
        result = runner.run(
            source_bundle=source_bundle,
            target_bundle=target_bundle,
            source_schema=source_schema,
            target_schema=target_schema,
            ground_truth=ground_truth,
        )

        return {
            "requestId": request.requestId,
            "status": "success",
            "sourceSystem": source_bundle.source_label,
            "sourceSchemaKey": source_bundle.schema_key,
            "targetKey": "dossier",
            "method": runner_config.method,
            "summary": {
                "sourceTableCount": len(source_bundle.dataframes),
                "targetTableCount": len(target_bundle.dataframes),
                "totalRows": len(result.rows),
                "avgScore": _avg_score(result.rows),
                "runtimeSeconds": round(result.runtime_seconds, 3),
                "groundTruthEnabled": request.groundTruth.enabled,
                "groundTruthCount": result.metrics.ground_truth_count,
                "matchedGroundTruthCount": result.metrics.matched_ground_truth_count,
            },
            "rows": rows_to_payload(result.rows, gt_lookup),
            "oneToOneRows": rows_to_payload(result.metrics.one_to_one_rows, gt_lookup),
            "metrics": result.metrics.metrics,
            "charts": build_charts(result.metrics.one_to_one_rows or result.rows),
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
        combined_one_to_one_rows = []
        runtime = 0.0
        for item in results:
            combined_rows.extend(item["rows"])
            combined_one_to_one_rows.extend(item["oneToOneRows"])
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
            "method": request.algorithm.method,
            "summary": {
                "sourceSystemCount": len(results),
                "totalRows": len(combined_rows),
                "oneToOneRows": len(combined_one_to_one_rows),
                "runtimeSeconds": round(runtime, 3),
            },
            "results": results,
            "rows": combined_rows,
            "oneToOneRows": combined_one_to_one_rows,
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

    def _runner_config(self, request: MatchRunRequest | MatchRunAllRequest) -> RunnerConfig:
        algorithm = request.algorithm
        method = "MagnetoGPT" if algorithm.useLlmReranker else algorithm.method
        return RunnerConfig(
            method=method,
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


def _scope_ground_truth(ground_truth, source_bundle, target_bundle):
    source_tables = set(source_bundle.dataframes)
    target_tables = set(target_bundle.dataframes)
    return [
        row
        for row in ground_truth
        if row.source_table in source_tables and row.target_table in target_tables
    ]
