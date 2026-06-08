from fuzzywuzzy import fuzz

from magneto.utils.utils import (
    common_prefix,
    detect_column_type,
    get_samples,
    preprocess_string,
)

"""
基于字符串的快速匹配器：利用字符对齐与模糊匹配度来筛选潜在的列名对应关系，
在深度模型运行前提供轻量级候选。
"""


def alignment_score_consecutive(str1, str2, max_distance=2, size_ratio_threshold=2):
    s1 = str1
    s2 = str2

    str1 = preprocess_string(str1)
    str2 = preprocess_string(str2)

    if len(str1) <= len(str2):
        shorter, longer = str1, str2
    else:
        shorter, longer = str2, str1

    # 如果字符长度差异过大，就没必要做精细匹配
    if len(longer) > len(shorter) * size_ratio_threshold:
        return 0

    matches = 0
    last_index = -1

    # 逐字符寻找相对顺序一致的匹配位置
    for char in shorter:
        for i in range(last_index + 1, len(longer)):
            if longer[i] == char:
                # 仅当相邻匹配距离不超过 max_distance 时才记作有效对齐
                if last_index == -1 or (i - last_index) <= max_distance:
                    matches += 1
                    last_index = i
                    break
                else:
                    break

    score = matches / len(shorter) if len(shorter) > 0 else 0

    return score


def fuzzy_similarity(s1: str, s2: str) -> float:
    return fuzz.ratio(s1, s2) / 100.0


def get_str_similarity_candidates(
    source_column_names,
    target_column_names,
    alignment_threshold=0.95,
    fuzzy_similarity_threshold=0.6,
):
    """
    基于列名计算两类得分：字符对齐与模糊匹配。任一命中阈值即视为候选。
    """
    prefix_source = common_prefix(list(source_column_names))
    prefix_target = common_prefix(list(target_column_names))

    candidates = {}
    for source_col in source_column_names:
        prep_source_col = source_col.replace(prefix_source, "")

        for target_col in target_column_names:
            prep_target_col = target_col.replace(prefix_target, "")

            alignment_score = alignment_score_consecutive(
                prep_source_col, prep_target_col
            )

            if alignment_score >= alignment_threshold:
                candidates[(source_col, target_col)] = alignment_score

            name_similarity = fuzzy_similarity(prep_source_col, prep_target_col)

            if name_similarity >= fuzzy_similarity_threshold:
                candidates[(source_col, target_col)] = name_similarity

    return candidates
