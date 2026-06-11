from __future__ import annotations

from dataclasses import dataclass
from typing import Any

from ..domain import GroundTruthRow, MatchRow
from .legacy_cf_metrics import (
    calculate_recall_at_k,
    calculate_recall_hit_count,
    compute_mean_ranking_reciprocal_adjusted,
    ground_truth_to_legacy_pairs,
    matcher_results_to_rows,
    rows_to_matcher_results,
)


@dataclass(frozen=True)
class ResultSetMetricBundle:
    metrics: list[dict[str, Any]]
    summary: dict[str, Any]


@dataclass(frozen=True)
class MetricBundle:
    metrics: list[dict[str, Any]]
    one_to_one_rows: list[MatchRow]
    ground_truth_count: int
    matched_ground_truth_count: int
    all_metrics: list[dict[str, Any]]
    one_to_one_metrics: list[dict[str, Any]]
    all_summary: dict[str, Any]
    one_to_one_summary: dict[str, Any]


def evaluate_matches(rows: list[MatchRow], ground_truth: list[GroundTruthRow]) -> MetricBundle:
    _ensure_ground_truth(ground_truth)
    matches, row_lookup = rows_to_matcher_results(rows)
    one_to_one_matches = matches.one_to_one()
    one_to_one_rows = matcher_results_to_rows(one_to_one_matches, row_lookup)

    all_result = evaluate_result_set(rows, ground_truth)
    one_to_one_result = evaluate_result_set(one_to_one_rows, ground_truth)

    metrics = [
        _metric("MRR", "MRR", all_result.summary["mrr"]),
        _metric("Recall@20", "Recall@20", all_result.summary["recallAt20"]),
        _metric("All_Precision", "All Precision", all_result.summary["precision"]),
        _metric("All_F1Score", "All F1 Score", all_result.summary["f1Score"]),
        _metric("All_Recall", "All Recall", all_result.summary["recall"]),
        _metric("All_PrecisionTop10Percent", "All Precision Top 10 Percent", all_result.summary["precisionTop10Percent"]),
        _metric("All_RecallAtSizeofGroundTruth", "All Recall At Size of Ground Truth", all_result.summary["recallAtSizeofGroundTruth"]),
        _metric("One2One_Count", "One2One match count", one_to_one_result.summary["matchCount"]),
        _metric("One2One_Precision", "One2One Precision", one_to_one_result.summary["precision"]),
        _metric("One2One_F1Score", "One2One F1 Score", one_to_one_result.summary["f1Score"]),
        _metric("One2One_Recall", "One2One Recall", one_to_one_result.summary["recall"]),
        _metric("One2One_PrecisionTop10Percent", "One2One Precision Top 10 Percent", one_to_one_result.summary["precisionTop10Percent"]),
        _metric("One2One_RecallAtSizeofGroundTruth", "One2One Recall At Size of Ground Truth", one_to_one_result.summary["recallAtSizeofGroundTruth"]),
    ]

    return MetricBundle(
        metrics=metrics,
        one_to_one_rows=one_to_one_rows,
        ground_truth_count=all_result.summary["groundTruthCount"],
        matched_ground_truth_count=all_result.summary["matchedGroundTruthCount"],
        all_metrics=all_result.metrics,
        one_to_one_metrics=one_to_one_result.metrics,
        all_summary=all_result.summary,
        one_to_one_summary=one_to_one_result.summary,
    )


def evaluate_result_set(rows: list[MatchRow], ground_truth: list[GroundTruthRow]) -> ResultSetMetricBundle:
    _ensure_ground_truth(ground_truth)
    matches, _ = rows_to_matcher_results(rows)
    legacy_ground_truth = ground_truth_to_legacy_pairs(ground_truth)
    valentine_metrics = matches.get_metrics(legacy_ground_truth) if matches else _empty_valentine_metrics()

    summary = {
        "mrr": _as_float(compute_mean_ranking_reciprocal_adjusted(matches, legacy_ground_truth)),
        "recallAt20": _as_float(calculate_recall_at_k(matches, legacy_ground_truth)),
        "precision": _as_float(valentine_metrics["Precision"]),
        "recall": _as_float(valentine_metrics["Recall"]),
        "f1Score": _as_float(valentine_metrics["F1Score"]),
        "precisionTop10Percent": _as_float(valentine_metrics["PrecisionTop10Percent"]),
        "recallAtSizeofGroundTruth": _as_float(valentine_metrics["RecallAtSizeofGroundTruth"]),
        "matchCount": len(rows),
        "groundTruthCount": len(legacy_ground_truth),
        "matchedGroundTruthCount": calculate_recall_hit_count(matches, legacy_ground_truth),
    }
    return ResultSetMetricBundle(
        metrics=[
            _metric("MRR", "MRR", summary["mrr"]),
            _metric("Recall@20", "Recall@20", summary["recallAt20"]),
            _metric("Precision", "Precision", summary["precision"]),
            _metric("Recall", "Recall", summary["recall"]),
            _metric("F1Score", "F1 Score", summary["f1Score"]),
            _metric("PrecisionTop10Percent", "Precision Top 10 Percent", summary["precisionTop10Percent"]),
            _metric("RecallAtSizeofGroundTruth", "Recall At Size of Ground Truth", summary["recallAtSizeofGroundTruth"]),
            _metric("matchCount", "Match Count", summary["matchCount"]),
            _metric("groundTruthCount", "Ground Truth Count", summary["groundTruthCount"]),
            _metric("matchedGroundTruthCount", "Matched Ground Truth Count", summary["matchedGroundTruthCount"]),
        ],
        summary=summary,
    )


def make_one_to_one(rows: list[MatchRow]) -> list[MatchRow]:
    matches, row_lookup = rows_to_matcher_results(rows)
    return matcher_results_to_rows(matches.one_to_one(), row_lookup)


def _empty_valentine_metrics() -> dict[str, float]:
    return {
        "Precision": 0.0,
        "F1Score": 0.0,
        "Recall": 0.0,
        "PrecisionTop10Percent": 0.0,
        "RecallAtSizeofGroundTruth": 0.0,
    }


def _ensure_ground_truth(ground_truth: list[GroundTruthRow]) -> None:
    if not ground_truth:
        raise ValueError("Ground truth is required to compute CF schema matching metrics.")


def _metric(key: str, name: str, value: float | int) -> dict[str, Any]:
    return {
        "metricKey": key,
        "metricName": name,
        "metricValue": round(float(value), 6),
    }


def _as_float(value: Any) -> float:
    return float(value)
