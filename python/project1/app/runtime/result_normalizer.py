from __future__ import annotations

from collections import Counter
from typing import Any

from ..domain import GroundTruthRow, MatchRow


def rows_to_payload(rows: list[MatchRow], gt_lookup: dict[tuple[str, str, str, str, str], GroundTruthRow]) -> list[dict[str, Any]]:
    payload = []
    for index, row in enumerate(sorted(rows, key=lambda item: (-item.score, item.source_table, item.source_column)), start=1):
        gt = gt_lookup.get(row.key)
        payload.append(
            {
                "rowNo": index,
                "sourceDatabase": row.source_database,
                "sourceTable": row.source_table,
                "sourceColumn": row.source_column,
                "targetTable": row.target_table,
                "targetPhysicalTable": row.target_physical_table,
                "targetColumn": row.target_column,
                "score": round(float(row.score), 6),
                "rank": row.rank,
                "method": row.method,
                "isGroundTruthHit": gt is not None,
                "groundTruth": _ground_truth_payload(gt),
                "rawPayload": row.raw_payload,
            }
        )
    return payload


def build_charts(rows: list[MatchRow]) -> dict[str, Any]:
    score_bins = Counter()
    target_counts = Counter()
    for row in rows:
        if row.score >= 1.0:
            score_bins["score = 1.00"] += 1
        elif row.score >= 0.90:
            score_bins["0.90 - 0.99"] += 1
        elif row.score >= 0.80:
            score_bins["0.80 - 0.89"] += 1
        else:
            score_bins["< 0.80"] += 1
        target_counts[row.target_table] += 1

    return {
        "scoreDistribution": [
            {"label": "score = 1.00", "value": score_bins["score = 1.00"]},
            {"label": "0.90 - 0.99", "value": score_bins["0.90 - 0.99"]},
            {"label": "0.80 - 0.89", "value": score_bins["0.80 - 0.89"]},
            {"label": "< 0.80", "value": score_bins["< 0.80"]},
        ],
        "targetTopDistribution": [
            {"targetTable": table, "value": count}
            for table, count in target_counts.most_common(20)
        ],
    }


def _ground_truth_payload(row: GroundTruthRow | None) -> dict[str, Any] | None:
    if row is None:
        return None
    return {
        "sourceDatabase": row.source_database,
        "sourceTable": row.source_table,
        "sourceColumn": row.source_column,
        "targetTable": row.target_table,
        "targetColumn": row.target_column,
        "mappingType": row.mapping_type,
        "difficulty": row.difficulty,
        "notes": row.notes,
    }

