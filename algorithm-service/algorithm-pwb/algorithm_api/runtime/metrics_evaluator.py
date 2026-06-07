from __future__ import annotations

from collections import defaultdict
from dataclasses import dataclass
from typing import Any

from ..domain import GroundTruthRow, MatchRow


@dataclass(frozen=True)
class MetricBundle:
    metrics: list[dict[str, Any]]
    one_to_one_rows: list[MatchRow]
    ground_truth_count: int
    matched_ground_truth_count: int


def evaluate_matches(rows: list[MatchRow], ground_truth: list[GroundTruthRow]) -> MetricBundle:
    gt_set = {
        (
            gt.source_database.upper(),
            gt.source_table,
            gt.source_column,
            gt.target_table,
            gt.target_column,
        )
        for gt in ground_truth
    }
    pred_set = {row.key for row in rows}
    tp = len(pred_set & gt_set)

    precision = tp / len(pred_set) if pred_set else 0.0
    recall = tp / len(gt_set) if gt_set else 0.0
    f1 = 2 * precision * recall / (precision + recall) if precision + recall else 0.0

    mrr = _mean_reciprocal_rank(rows, ground_truth)
    recall_at_20 = _recall_at_k(rows, ground_truth, 20)
    precision_top_10_percent = _precision_top_fraction(rows, gt_set, 0.10)
    recall_at_gt = _recall_at_size_of_ground_truth(rows, gt_set)

    one_to_one = make_one_to_one(rows)
    one_to_one_set = {row.key for row in one_to_one}
    one_tp = len(one_to_one_set & gt_set)
    one_precision = one_tp / len(one_to_one_set) if one_to_one_set else 0.0
    one_recall = one_tp / len(gt_set) if gt_set else 0.0
    one_f1 = (
        2 * one_precision * one_recall / (one_precision + one_recall)
        if one_precision + one_recall
        else 0.0
    )
    one_precision_top_10_percent = _precision_top_fraction(one_to_one, gt_set, 0.10)
    one_recall_at_gt = _recall_at_size_of_ground_truth(one_to_one, gt_set)

    metrics = [
        _metric("MRR", "MRR", mrr),
        _metric("Recall@20", "Recall@20", recall_at_20),
        _metric("All_Precision", "All Precision", precision),
        _metric("All_F1Score", "All F1 Score", f1),
        _metric("All_Recall", "All Recall", recall),
        _metric("All_PrecisionTop10Percent", "All Precision Top 10 Percent", precision_top_10_percent),
        _metric("All_RecallAtSizeofGroundTruth", "All Recall At Size of Ground Truth", recall_at_gt),
        _metric("One2One_Count", "One2One match count", float(len(one_to_one))),
        _metric("One2One_Precision", "One2One Precision", one_precision),
        _metric("One2One_F1Score", "One2One F1 Score", one_f1),
        _metric("One2One_Recall", "One2One Recall", one_recall),
        _metric("One2One_PrecisionTop10Percent", "One2One Precision Top 10 Percent", one_precision_top_10_percent),
        _metric("One2One_RecallAtSizeofGroundTruth", "One2One Recall At Size of Ground Truth", one_recall_at_gt),
    ]

    return MetricBundle(
        metrics=metrics,
        one_to_one_rows=one_to_one,
        ground_truth_count=len(gt_set),
        matched_ground_truth_count=tp,
    )


def make_one_to_one(rows: list[MatchRow]) -> list[MatchRow]:
    selected: list[MatchRow] = []
    used_sources: set[tuple[str, str, str]] = set()
    used_targets: set[tuple[str, str]] = set()
    for row in sorted(rows, key=lambda item: item.score, reverse=True):
        source_key = (row.source_database.upper(), row.source_table, row.source_column)
        target_key = (row.target_table, row.target_column)
        if source_key in used_sources or target_key in used_targets:
            continue
        selected.append(row)
        used_sources.add(source_key)
        used_targets.add(target_key)
    return selected


def _mean_reciprocal_rank(rows: list[MatchRow], ground_truth: list[GroundTruthRow]) -> float:
    gt_by_source: dict[tuple[str, str, str], set[tuple[str, str]]] = defaultdict(set)
    for gt in ground_truth:
        gt_by_source[(gt.source_database.upper(), gt.source_table, gt.source_column)].add(
            (gt.target_table, gt.target_column)
        )

    rows_by_source: dict[tuple[str, str, str], list[MatchRow]] = defaultdict(list)
    for row in rows:
        rows_by_source[(row.source_database.upper(), row.source_table, row.source_column)].append(row)

    total = 0.0
    query_count = 0
    for source_key, gt_targets in gt_by_source.items():
        ranked = sorted(rows_by_source.get(source_key, []), key=lambda item: item.score, reverse=True)
        if not ranked:
            query_count += 1
            continue
        query_count += 1
        for index, row in enumerate(ranked, start=1):
            if (row.target_table, row.target_column) in gt_targets:
                total += 1.0 / index
                break
    return total / query_count if query_count else 0.0


def _recall_at_k(rows: list[MatchRow], ground_truth: list[GroundTruthRow], k: int) -> float:
    gt_set = {
        (
            gt.source_database.upper(),
            gt.source_table,
            gt.source_column,
            gt.target_table,
            gt.target_column,
        )
        for gt in ground_truth
    }
    if not gt_set:
        return 0.0

    rows_by_source: dict[tuple[str, str, str], list[MatchRow]] = defaultdict(list)
    for row in rows:
        rows_by_source[(row.source_database.upper(), row.source_table, row.source_column)].append(row)

    hits: set[tuple[str, str, str, str, str]] = set()
    for ranked_rows in rows_by_source.values():
        for row in sorted(ranked_rows, key=lambda item: item.score, reverse=True)[:k]:
            if row.key in gt_set:
                hits.add(row.key)
    return len(hits) / len(gt_set)


def _precision_top_fraction(rows: list[MatchRow], gt_set: set[tuple[str, str, str, str, str]], fraction: float) -> float:
    if not rows:
        return 0.0
    count = max(1, int(len(rows) * fraction))
    top_rows = sorted(rows, key=lambda item: item.score, reverse=True)[:count]
    hits = sum(1 for row in top_rows if row.key in gt_set)
    return hits / len(top_rows)


def _recall_at_size_of_ground_truth(rows: list[MatchRow], gt_set: set[tuple[str, str, str, str, str]]) -> float:
    if not gt_set:
        return 0.0
    top_rows = sorted(rows, key=lambda item: item.score, reverse=True)[: len(gt_set)]
    hits = len({row.key for row in top_rows} & gt_set)
    return hits / len(gt_set)


def _metric(key: str, name: str, value: float) -> dict[str, Any]:
    return {
        "metricKey": key,
        "metricName": name,
        "metricValue": round(float(value), 6),
    }

