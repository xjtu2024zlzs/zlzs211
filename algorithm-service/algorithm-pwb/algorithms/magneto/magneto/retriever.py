import numpy as np
import torch
from sentence_transformers import SentenceTransformer
from torch.nn.functional import normalize
from transformers import AutoModel, AutoTokenizer

from magneto.utils.retriever_utils import (
    detect_column_type,
    infer_column_dtype,
    lm_map,
    sentence_transformer_map,
)

"""
Retriever 模块提供对本地微调模型的封装，负责载入权重、序列化列、生成向量，
并按照余弦相似度产出候选匹配。
"""

QUERY_PREFIX = "Represent this sentence for searching relevant passages: "


class Retriever:
    """利用微调好的 SentenceTransformer 模型执行列级检索。"""
    def __init__(
        self,
        model_path,
        serialization="header_values_prefix",
        norm=False,
        batch_size=32,
        margin=1,
    ):
        # 如需启用 GPU，可恢复上方注释中的自动检测逻辑
        self.device = torch.device("cpu")
        self.serialization = serialization
        self.norm = norm
        self.batch_size = batch_size
        self.margin = margin

        self._model = SentenceTransformer("sentence-transformers/all-mpnet-base-v2")

        # 将权重加载到 CPU，避免跨设备报错
        state_dict = torch.load(
            model_path, map_location=torch.device("cpu"), weights_only=True
        )

        # 把 state_dict 写入 SentenceTransformer
        self._model.load_state_dict(state_dict)

        self._model.eval()
        self._model.to(self.device)

        print(f"Loaded FinedTuned Model on {self.device} in RetrieverClass")

        self._tokenizer = AutoTokenizer.from_pretrained(
            "sentence-transformers/all-mpnet-base-v2"
        )
        self.norm = norm

    def encode_columns(self, table, values):
        return {
            col: self._encode_column(col, table[col], values[col])
            for col in table.columns
        }

    def encode_columns(self, table, values):
        """将每一列序列化为文本并批量送入模型，输出 numpy 向量。"""
        texts = [self._tokenize(col, table[col], values[col]) for col in table.columns]
        # 若使用零样本模型，可启用下方批量推理逻辑
        # if "zs" in self.model_type:
        #     batched_embeddings = {}
        #     for i in range(0, len(texts), self.batch_size):
        #         batch_texts = texts[i: i + self.batch_size]
        #         inputs = self._tokenizer(
        #             batch_texts, return_tensors="pt", padding=True, truncation=True
        #         ).to(self.device)
        #         outputs = self._model(**inputs)
        #         embeddings = outputs.last_hidden_state[:, 0, :].detach(
        #         ).cpu().numpy()
        #         for j, col in enumerate(table.columns[i: i + self.batch_size]):
        #             batched_embeddings[col] = embeddings[j]
        #     return batched_embeddings

        # 如果是微调模型，直接用 encode 即可
        embeddings = self._model.encode(
            texts, convert_to_tensor=True, device=self.device
        )
        return {
            col: embeddings[i].detach().cpu().numpy()
            for i, col in enumerate(table.columns)
        }

    def _tokenize(self, header, values, tokens):
        """根据序列化模式拼接列名、类型与样本值。"""
        # data_type = infer_column_dtype(values)
        data_type = detect_column_type(values)
        serialization = {
            "header": header,
            "header_values_default": f"{self._tokenizer.cls_token}{header}{self._tokenizer.sep_token}{data_type}{self._tokenizer.sep_token}{self._tokenizer.sep_token.join(tokens)}",
            "header_values_prefix": f"{self._tokenizer.cls_token}header:{header}{self._tokenizer.sep_token}datatype:{data_type}{self._tokenizer.sep_token}values:{', '.join(tokens)}",
            "header_values_repeat": f"{self._tokenizer.cls_token}{self._tokenizer.sep_token.join([header] * 5)}{self._tokenizer.sep_token}{data_type}{self._tokenizer.sep_token}{self._tokenizer.sep_token.join(tokens)}",
        }
        return serialization[self.serialization]

    def find_matches(
        self, source_table, target_table, source_values, target_values, top_k
    ):
        """编码源/目标表后计算相似度，返回每个源列的 Top-K 候选。"""
        # 如果模型类型包含 arctic，可走下方特化路径
        # if "arctic" in self.model_type:
        #     return self._match_columns_arctic(
        #         source_table, target_table, source_values, target_values, top_k
        #     )
        # else:
        source_embeddings = self.encode_columns(source_table, source_values)
        target_embeddings = self.encode_columns(target_table, target_values)
        return self._match_columns(source_embeddings, target_embeddings, top_k)

    def _match_columns(self, source_embeddings, target_embeddings, top_k):
        """对所有源列逐一与目标列计算余弦相似度，并挑选分值最高的列对。"""
        matched_columns = {}
        for s_col, s_emb in source_embeddings.items():
            similarities = {
                t_col: self._cosine_similarity(s_emb, t_emb)
                for t_col, t_emb in target_embeddings.items()
            }
            sorted_similarities = sorted(
                similarities.items(), key=lambda x: x[1], reverse=True
            )[:top_k]
            if self.norm:
                normalized_similarities = self._normalize_similarities(
                    sorted_similarities
                )
                matched_columns[s_col] = normalized_similarities
            else:
                matched_columns[s_col] = sorted_similarities

        return matched_columns

    def _cosine_similarity(self, vec1, vec2):
        """计算两个 numpy 向量的余弦相似度。"""
        return np.dot(vec1, vec2.T) / (np.linalg.norm(vec1) * np.linalg.norm(vec2))
        # 如果是零样本模型，可按需返回单值

    def _normalize_similarities(self, scores):
        """将得分线性映射到 [0,1]，便于不同列之间对比。"""
        min_score = min(score for _, score in scores)
        max_score = max(score for _, score in scores)
        if max_score - min_score > 0:
            return [
                (col, (score - min_score) / (max_score - min_score))
                for col, score in scores
            ]
        else:
            # Normalize to 1 if all scores are equal
            return [(col, 1.0) for col, _ in scores]

    def _normalize_similarities(self, scores):
        """重复定义保留以兼容旧代码路径。"""
        min_score = min(score for _, score in scores)
        max_score = max(score for _, score in scores)
        if max_score - min_score > 0:
            return [
                (col, (score - min_score) / (max_score - min_score))
                for col, score in scores
            ]
        else:
            # Normalize to 1 if all scores are equal
            return [(col, 1.0) for col, _ in scores]

    def _match_columns_arctic(
        self, source_table, target_table, source_values, target_values, top_k
    ):
        """针对 arctic 模型的特化匹配流程。"""
        queries = []
        for col in source_table.columns:
            queries.append(self._tokenize(col, source_table[col], source_values[col]))
        queries_with_prefix = [f"{QUERY_PREFIX}{q}" for q in queries]
        query_tokens = self._tokenizer(
            queries_with_prefix,
            padding=True,
            truncation=True,
            return_tensors="pt",
            max_length=512,
        ).to(self.device)

        documents = []
        for col in target_table.columns:
            documents.append(self._tokenize(col, target_table[col], target_values[col]))
        document_tokens = self._tokenizer(
            documents,
            padding=True,
            truncation=True,
            return_tensors="pt",
            max_length=512,
        ).to(self.device)

        with torch.inference_mode():
            query_embeddings = self._model(**query_tokens)[0][:, 0]
            document_embeddings = self._model(**document_tokens)[0][:, 0]

        query_embeddings = normalize(query_embeddings)
        document_embeddings = normalize(document_embeddings)

        scores = query_embeddings @ document_embeddings.T
        matched_columns = {}

        for col, query_scores in zip(source_table.columns, scores):
            doc_score_pairs = list(zip(target_table.columns, query_scores))
            doc_score_pairs = [(doc, score.item()) for doc, score in doc_score_pairs]
            doc_score_pairs_sorted = sorted(
                doc_score_pairs, key=lambda x: x[1], reverse=True
            )[:top_k]
            # 如果需要对 arctic 结果做归一化，可启用下方逻辑
            # if self.norm:
            #     normalized_scores = self._normalize_similarities(doc_score_pairs_sorted)
            #     matched_columns[col] = normalized_scores
            # else:
            matched_columns[col] = doc_score_pairs_sorted

        return matched_columns
