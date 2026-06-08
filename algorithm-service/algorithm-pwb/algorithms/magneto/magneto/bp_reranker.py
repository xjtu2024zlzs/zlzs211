import numpy as np
from scipy.optimize import linear_sum_assignment
from valentine import MatcherResults

"""
该模块使用匈牙利算法（线性分配）将匹配问题转化为二分图，
帮助我们从候选集中挑选一对一的最优匹配。
"""


def bipartite_filtering(
    initial_matches, source_table_name, source_table, target_table, target_table_name
):
    source_cols = set()
    target_cols = set()
    for (col_source, col_target), score in initial_matches.items():
        col_source = col_source[1]
        col_target = col_target[1]
        source_cols.add(col_source)
        target_cols.add(col_target)

    # 列名映射到矩阵坐标，便于构造加权邻接矩阵
    source_col_to_num = {col: idx for idx, col in enumerate(source_cols)}
    target_col_to_num = {col: idx for idx, col in enumerate(target_cols)}

    # 先用零矩阵占位，后续再填入相似度得分
    score_matrix = np.zeros((len(source_cols), len(target_cols)))

    # 将候选匹配的得分写入矩阵，形成线性分配输入
    for (col_source, col_target), score in initial_matches.items():
        col_source = col_source[1]
        col_target = col_target[1]
        source_idx = source_col_to_num[col_source]
        target_idx = target_col_to_num[col_target]
        score_matrix[source_idx, target_idx] = score

    # print("得分矩阵:\n", score_matrix)

    row_ind, col_ind = linear_sum_assignment(score_matrix, maximize=True)
    assignment = list(zip(row_ind, col_ind))

    # print("匹配索引:", assignment)

    filtered_matches = {}

    source_idx_to_col = {idx: col for col, idx in source_col_to_num.items()}
    target_idx_to_col = {idx: col for col, idx in target_col_to_num.items()}

    for source_idx, target_idx in assignment:
        source_col = source_idx_to_col[source_idx]
        target_col = target_idx_to_col[target_idx]
        filtered_matches[
            ((source_table_name, source_col), (target_table_name, target_col))
        ] = score_matrix[source_idx, target_idx]

    return MatcherResults(filtered_matches)


def arrange_bipartite_matches(
    initial_matches, source_table, source_table_name, target_table, target_table_name
):
    # 如果没有任何初始匹配，直接返回空结果
    if not initial_matches:
        return MatcherResults({})
    
    filtered_matches = bipartite_filtering(
        initial_matches,
        source_table_name,
        source_table,
        target_table,
        target_table_name,
    )

    # 先删除已被选中的一对一匹配，避免重复
    for key in filtered_matches.keys():
        initial_matches.pop(key, None)

    # 如果还有剩余候选匹配，重新缩放分值
    if initial_matches:
        # 找到当前最佳匹配中的最小分值
        min_filtered_score = min(filtered_matches.values())

        # 重新缩放剩余候选的分值，使其略低于精选匹配但保留相对差异
        initial_max_score = max(initial_matches.values())
        scaling_factor = (
            (min_filtered_score - 0.01) / initial_max_score if initial_max_score > 0 else 1
        )

        # 通过缩放保持原先的排序关系
        adjusted_initial_matches = {
            key: score * scaling_factor for key, score in initial_matches.items()
        }

        # 合并精选匹配与被压低分值的剩余候选，以便后续指标计算
        filtered_matches.update(adjusted_initial_matches)

    return filtered_matches
