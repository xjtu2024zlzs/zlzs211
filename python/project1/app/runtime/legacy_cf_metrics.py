from __future__ import annotations

from dataclasses import replace
from typing import Iterable

from valentine import MatcherResults

from ..domain import GroundTruthRow, MatchRow


ValentineKey = tuple[tuple[str, str], tuple[str, str]]


def rows_to_matcher_results(rows: Iterable[MatchRow]) -> tuple[MatcherResults, dict[ValentineKey, MatchRow]]:
    matches: dict[ValentineKey, float] = {}
    row_lookup: dict[ValentineKey, MatchRow] = {}
    for row in sorted(rows, key=lambda item: (item.source_table, item.source_column, item.rank, -item.score)):
        key = ((row.source_table, row.source_column), (row.target_table, row.target_column))
        if key not in matches or row.score > matches[key]:
            matches[key] = float(row.score)
            row_lookup[key] = row
    return MatcherResults(matches), row_lookup


def matcher_results_to_rows(matches: MatcherResults, row_lookup: dict[ValentineKey, MatchRow]) -> list[MatchRow]:
    rows: list[MatchRow] = []
    for rank, (key, score) in enumerate(sorted(matches.items(), key=lambda item: item[1], reverse=True), start=1):
        base_row = row_lookup.get(key)
        if base_row is None:
            continue
        rows.append(replace(base_row, score=float(score), rank=rank))
    return rows


def ground_truth_to_legacy_pairs(ground_truth: Iterable[GroundTruthRow]) -> list[tuple[str, str]]:
    return [(row.source_column, row.target_column) for row in ground_truth]


def sort_matches(matches: MatcherResults) -> dict[str, list[tuple[str, float]]]:
    sorted_matches = {entry[0][1]: [] for entry in matches}
    for entry in matches:
        sorted_matches[entry[0][1]].append((entry[1][1], matches[entry]))
    return sorted_matches


def compute_mean_ranking_reciprocal_adjusted(
    matches: MatcherResults,
    ground_truth: list[tuple[str, str]],
) -> float:
    gt_per_input_col: dict[str, set[str]] = {}
    for input_col, target_col in ground_truth:
        gt_per_input_col.setdefault(input_col, set()).add(target_col)

    ordered_matches = sort_matches(matches)
    total_score = 0.0
    total_queries = 0
    for input_col in ordered_matches.keys():
        gt = gt_per_input_col.get(input_col, set())
        if len(gt) == 0:
            continue
        total_queries += 1
        for idx, (target_col, _) in enumerate(ordered_matches[input_col]):
            if target_col in gt:
                total_score += 1 / (idx + 1)
                break

    return total_score / total_queries if total_queries else 0.0


def calculate_recall_hit_count(matches: MatcherResults, ground_truth: list[tuple[str, str]]) -> int:
    ground_truth_set = set(frozenset(pair) for pair in ground_truth)
    correct_matches = 0
    for ((_, source_col), (_, target_col)), _ in matches.items():
        match_pair = frozenset((source_col, target_col))
        if match_pair in ground_truth_set:
            correct_matches += 1
            ground_truth_set.remove(match_pair)
    return correct_matches


def calculate_recall_at_k(matches: MatcherResults, ground_truth: list[tuple[str, str]]) -> float:
    total_ground_truth = len(ground_truth)
    if total_ground_truth == 0:
        return 0.0
    return calculate_recall_hit_count(matches, ground_truth) / total_ground_truth
