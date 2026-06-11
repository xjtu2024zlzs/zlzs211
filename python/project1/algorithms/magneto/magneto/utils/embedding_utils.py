import torch

"""
提供余弦相似度计算的工具函数，封装为简单/完整两个版本，方便复用。
"""


def compute_cosine_similarity_simple(embeddings_df1, embeddings_df2, k):
    embeddings_df1 = torch.nn.functional.normalize(embeddings_df1, p=2, dim=1)
    embeddings_df2 = torch.nn.functional.normalize(embeddings_df2, p=2, dim=1)

    # 先计算余弦相似度矩阵，形状约为 (N1, N2)
    similarity_matrix = torch.matmul(embeddings_df1, embeddings_df2.T)

    # 再针对每一行直接提取前 k 个最大值及其索引
    topk_similarity, topk_indices = torch.topk(similarity_matrix, k, dim=1)

    return topk_similarity, topk_indices


def compute_cosine_similarity(
    embeddings_input: torch.Tensor, embeddings_target: torch.Tensor, top_k: int
):
    """
    计算输入与目标向量之间的 Top-K 余弦相似度，并返回分值与索引。

    参数:
        embeddings_input: 形状 (num_input, dim) 的张量
        embeddings_target: 形状 (num_target, dim) 的张量
        top_k: 需保留的候选数量

    返回:
        top_k_scores: numpy 数组，记录每个输入的前 k 个得分
        top_k_indices: numpy 数组，记录对应的目标索引
    """
    # 确保两个张量位于同一设备
    device = embeddings_input.device
    embeddings_target = embeddings_target.to(device)

    # 对输入与目标向量分别做归一化
    input_norm = torch.norm(embeddings_input, dim=1, keepdim=True)
    target_norm = torch.norm(embeddings_target, dim=1, keepdim=True)

    # 计算余弦相似度矩阵
    similarities = torch.mm(embeddings_input, embeddings_target.T) / (
        input_norm * target_norm.T
    )

    min_top_k = min(top_k, similarities.shape[1])

    # 选取前 k 大的分值与索引
    top_k_scores, top_k_indices = torch.topk(
        similarities, min_top_k, dim=1, largest=True, sorted=True
    )

    # 转为 numpy，方便后续处理
    top_k_scores = top_k_scores.cpu().numpy()
    top_k_indices = top_k_indices.cpu().numpy()

    return top_k_scores, top_k_indices, similarities
