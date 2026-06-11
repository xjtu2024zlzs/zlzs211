from dataclasses import dataclass
from typing import Any, Dict, List, Tuple

from valentine.metrics.base_metric import Metric
from valentine.metrics.metric_helpers import *

"""
该模块实现“Top-K 召回率”指标，帮助我们衡量候选匹配列表中保留前 K 个结果时，
是否仍然能够覆盖真实匹配关系。
"""


@dataclass(eq=True, frozen=True)
class RecallAtTopK(Metric):
    """Top-K 召回率。

    参数
    ----
    k : int
        计算召回率时保留的候选数量。
    """

    k: int

    def _filtered_matches(
        self, matches: List[Tuple[Tuple[str, str], Tuple[str, str]]]
    ) -> List[Tuple[Tuple[str, str], Tuple[str, str]]]:
        """将匹配结果按源列划分，并仅保留每列得分最高的前 K 个候选。"""

        matches_per_col = {}
        for match in matches:
            source_col = match[0][1]
            if source_col not in matches_per_col:
                matches_per_col[source_col] = []
            matches_per_col[source_col].append(match)

        filtered_matches = []
        for col in matches_per_col:
            matches_for_col = matches_per_col[col]
            matches_per_col_sorted = sorted(
                matches_for_col, key=lambda x: matches[x], reverse=True
            )
            filtered_matches += matches_per_col_sorted[: self.k]

        return filtered_matches

    def apply(
        self,
        matches: List[Tuple[Tuple[str, str], Tuple[str, str]]],
        ground_truth: List[Tuple[str, str]],
    ) -> Dict[str, Any]:
        """在给定真实匹配的情况下，计算 Top-K 召回率。"""

        matches_set = set()
        filtered_matches = self._filtered_matches(matches)
        for fm in filtered_matches:
            fmatch = (fm[0][1], fm[1][1])
            matches_set.add(fmatch)

        ground_truth_set = set(ground_truth)

        tp = len(ground_truth_set.intersection(matches_set))
        recall = round((tp / len(ground_truth_set)), 3)

        values_not_in_gt = ground_truth_set - matches_set

        # for v in values_not_in_gt:
        #     print("真实标注中的列未出现在候选匹配里", v)

        return recall
