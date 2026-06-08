import sys
from pathlib import Path

import pandas as pd


PROJECT_ROOT = Path(__file__).resolve().parents[1]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))


def _match_row(source_column, target_column, score, *, source_table="source_table", target_table="target_table", rank=1):
    from algorithm_api.domain import MatchRow

    return MatchRow(
        source_database="PLM",
        source_table=source_table,
        source_column=source_column,
        target_table=target_table,
        target_column=target_column,
        score=score,
        rank=rank,
        method="Magneto",
    )


def _ground_truth(source_column, target_column, *, source_table="source_table", target_table="target_table"):
    from algorithm_api.domain import GroundTruthRow

    return GroundTruthRow(
        source_database="PLM",
        source_table=source_table,
        source_column=source_column,
        target_table=target_table,
        target_column=target_column,
    )


def test_result_set_metrics_reuse_legacy_cf_benchmark_functions():
    from experiments.benchmarks.benchmark_utils import (
        calculate_recall_at_k,
        compute_mean_ranking_reciprocal_adjusted,
    )
    from valentine import MatcherResults

    from algorithm_api.runtime.metrics_evaluator import evaluate_result_set

    rows = [
        _match_row("source_a", "wrong_a", 0.99, rank=1),
        _match_row("source_a", "target_a", 0.90, rank=2),
        _match_row("source_b", "target_b", 0.80, rank=1),
    ]
    ground_truth = [
        _ground_truth("source_a", "target_a"),
        _ground_truth("source_b", "target_b"),
    ]

    legacy_matches = MatcherResults(
        {
            (("source_table", "source_a"), ("target_table", "wrong_a")): 0.99,
            (("source_table", "source_a"), ("target_table", "target_a")): 0.90,
            (("source_table", "source_b"), ("target_table", "target_b")): 0.80,
        }
    )
    legacy_gt = [("source_a", "target_a"), ("source_b", "target_b")]
    legacy_metrics = legacy_matches.get_metrics(legacy_gt)

    result = evaluate_result_set(rows, ground_truth)

    assert result.summary["mrr"] == compute_mean_ranking_reciprocal_adjusted(legacy_matches, legacy_gt)
    assert result.summary["recallAt20"] == calculate_recall_at_k(legacy_matches, legacy_gt)
    assert result.summary["precision"] == legacy_metrics["Precision"]
    assert result.summary["recall"] == legacy_metrics["Recall"]
    assert result.summary["f1Score"] == legacy_metrics["F1Score"]
    assert result.summary["matchCount"] == len(rows)
    assert result.summary["groundTruthCount"] == len(legacy_gt)
    assert result.summary["matchedGroundTruthCount"] == 2


def test_evaluate_matches_derives_one_to_one_rows_with_valentine():
    from algorithm_api.runtime.metrics_evaluator import evaluate_matches

    rows = [
        _match_row("source_a", "target_a", 0.99, rank=1),
        _match_row("source_a", "target_b", 0.97, rank=2),
        _match_row("source_b", "target_b", 0.98, rank=1),
        _match_row("source_b", "target_a", 0.96, rank=2),
    ]
    ground_truth = [
        _ground_truth("source_a", "target_a"),
        _ground_truth("source_b", "target_b"),
    ]

    result = evaluate_matches(rows, ground_truth)

    assert len(result.one_to_one_rows) == 2
    assert {(row.source_column, row.target_column) for row in result.one_to_one_rows} == {
        ("source_a", "target_a"),
        ("source_b", "target_b"),
    }
    assert result.all_summary["matchCount"] == 4
    assert result.one_to_one_summary["matchCount"] == 2


def test_match_service_run_returns_six_method_variant_result_sets(monkeypatch):
    from algorithm_api.domain import DataBundle, GroundTruthRow, TableSchema
    from algorithm_api.schemas import MatchRunRequest
    from algorithm_api.services import match_service as match_service_module
    from algorithm_api.services.match_service import MatchService

    source_bundle = DataBundle(
        system_key="plm",
        source_label="PLM",
        schema_key="cf_plm",
        schemas={"part_master": TableSchema(table_name="part_master")},
        dataframes={"part_master": pd.DataFrame({"part_no": ["P1"], "part_name": ["Pump"]})},
    )
    target_bundle = DataBundle(
        system_key="dossier",
        source_label="dossier",
        schema_key="dossier",
        schemas={"material": TableSchema(table_name="material", physical_table_name="p1p_dossier_material")},
        dataframes={"material": pd.DataFrame({"material_no": ["P1"], "material_name": ["Pump"]})},
    )
    ground_truth = [
        GroundTruthRow("PLM", "part_master", "part_no", "material", "material_no"),
        GroundTruthRow("PLM", "part_master", "part_name", "material", "material_name"),
    ]

    class GroundTruthCatalogStub:
        def for_source_system(self, source_system):
            assert source_system == "plm"
            return ground_truth

        def lookup(self):
            return {
                (
                    row.source_database,
                    row.source_table,
                    row.source_column,
                    row.target_table,
                    row.target_column,
                ): row
                for row in ground_truth
            }

    class DummyRunner:
        def __init__(self, config):
            self.config = config

        def run_methods(self, *, source_bundle, target_bundle, source_schema, target_schema, ground_truth, methods):
            from algorithm_api.domain import MatchRow
            from algorithm_api.runtime.cf_match_runner import RunnerResult
            from algorithm_api.runtime.metrics_evaluator import evaluate_matches

            results = []
            for method in methods:
                rows = [
                    MatchRow("PLM", "part_master", "part_no", "material", "material_no", 0.95, 1, method),
                    MatchRow("PLM", "part_master", "part_name", "material", "material_name", 0.90, 1, method),
                ]
                results.append(
                    RunnerResult(
                        method=method,
                        rows=rows,
                        metrics=evaluate_matches(rows, ground_truth),
                        runtime_seconds=0.01,
                    )
                )
            return results

    monkeypatch.setattr(match_service_module, "CfMatchRunner", DummyRunner)
    service = MatchService()
    monkeypatch.setattr(service, "_load_source", lambda source, request_id: source_bundle)
    monkeypatch.setattr(service, "_load_target", lambda target: target_bundle)
    monkeypatch.setattr(service, "_load_ground_truth", lambda enabled, root_path: GroundTruthCatalogStub())

    response = service.run(
        MatchRunRequest(
            algorithm={
                "methods": ["Magneto", "MagnetoBoost", "MagnetoGPT"],
                "variants": ["all", "one2one"],
                "embeddingModel": "minilm",
                "llmModel": "deepseek/deepseek-chat",
            }
        )
    )

    assert response["resultSetCount"] == 6
    assert {(item["method"], item["variant"]) for item in response["resultSets"]} == {
        ("Magneto", "all"),
        ("Magneto", "one2one"),
        ("MagnetoBoost", "all"),
        ("MagnetoBoost", "one2one"),
        ("MagnetoGPT", "all"),
        ("MagnetoGPT", "one2one"),
    }
    for result_set in response["resultSets"]:
        assert {
            "mrr",
            "recallAt20",
            "precision",
            "recall",
            "f1Score",
            "matchCount",
            "groundTruthCount",
            "matchedGroundTruthCount",
        } <= set(result_set["metricsSummary"])
