import os

import torch
from fuzzywuzzy import fuzz
from sentence_transformers import SentenceTransformer, models
from transformers import AutoTokenizer

from magneto.column_encoder import ColumnEncoder
from magneto.utils.embedding_utils import compute_cosine_similarity_simple
from magneto.utils.utils import detect_column_type, get_samples

"""
EmbeddingMatcher 负责用 SentenceTransformer 或本地微调模型计算列表示向量，
并基于余弦相似度产出候选列对，是 Magneto 的核心检索组件。
"""

DEFAULT_MODELS = {
    "mpnet": "sentence-transformers/all-mpnet-base-v2",
    "roberta": "sentence-transformers/all-roberta-large-v1",
    "e5": "intfloat/e5-base",
    "arctic": "Snowflake/snowflake-arctic-embed-l-v2.0",
    "minilm": "sentence-transformers/all-MiniLM-L6-v2"
}

class EmbeddingMatcher:
    """封装向量化与相似度计算逻辑，支持默认模型与微调权重。
    
    使用类级别缓存避免每次 get_matches 调用时重新加载模型。
    """
    # 类级别模型缓存：{model_name: (model, tokenizer)}
    _model_cache: dict = {}

    def __init__(self, params):
        self.params = params
        self.topk = params["topk"]
        self.embedding_threshold = params["embedding_threshold"]
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.model_name = params["embedding_model"]
        self.use_prompt_query = True if "arctic" in self.model_name else False

        base_key = next((key for key in DEFAULT_MODELS if key in self.model_name), "mpnet")
        if base_key not in DEFAULT_MODELS:
            print(f"Warning: No base model detected in {self.model_name}, defaulting to 'mpnet'")
            base_key = "mpnet"

        base_model_path = DEFAULT_MODELS[base_key]

        # 从缓存加载模型（避免每个表对都重新初始化 SentenceTransformer）
        cache_key = self.model_name
        if cache_key in EmbeddingMatcher._model_cache:
            self.model, self.tokenizer = EmbeddingMatcher._model_cache[cache_key]
        else:
            self.model = SentenceTransformer(base_model_path, device=self.device)
            self.tokenizer = AutoTokenizer.from_pretrained(base_model_path)

            # 加载底座模型与对应分词器
            if self.model_name in DEFAULT_MODELS:
                print(f"Loaded default model '{self.model_name}' on {self.device}")
            else:
                print(f"Loaded base model '{base_key}' on {self.device}")
                # 若提供的是自定义目录，则尝试载入微调权重
                if os.path.exists(self.model_name):
                    print(f"Loading fine-tuned weights from {self.model_name}")
                    state_dict = torch.load(self.model_name, map_location=self.device)
                    self.model.load_state_dict(state_dict)
                    self.model.eval().to(self.device)
                else:
                    print(f"No local model found at {self.model_name}, using base model")

            # 缓存模型
            EmbeddingMatcher._model_cache[cache_key] = (self.model, self.tokenizer)

    def _get_embeddings(self, texts, use_prompt_query=False, batch_size=32):
        """调用 SentenceTransformer 的 encode 接口批量生成列向量。"""
        embeddings = []
        for i in range(0, len(texts), batch_size):
            batch = texts[i:i + batch_size]
            with torch.no_grad():
                if use_prompt_query:
                    print("Using prompt query")
                    embeds = self.model.encode(
                        batch,
                        convert_to_tensor=True,
                        show_progress_bar=False,
                        device=self.device,
                        prompt_name="query"
                    )
                else:
                    embeds = self.model.encode(
                        batch,
                        convert_to_tensor=True,
                        show_progress_bar=False,
                        device=self.device
                    )
            embeddings.append(embeds)
        return torch.cat(embeddings)

    def get_embedding_similarity_candidates(self, source_df, target_df, 
                                           source_table_name=None, target_table_name=None):
        """
        对源表和目标表进行编码并返回相似度超过阈值的列对。
        
        Args:
            source_df: 源DataFrame
            target_df: 目标DataFrame
            source_table_name: 源表名（可选，用于表名上下文）
            target_table_name: 目标表名（可选，用于表名上下文）
        """
        encoder = ColumnEncoder(
            self.tokenizer,
            encoding_mode=self.params["encoding_mode"],
            sampling_mode=self.params["sampling_mode"],
            n_samples=self.params["sampling_size"],
        )

        # 传递表名给encoder
        input_col_repr_dict = {
            encoder.encode(source_df, col, source_table_name): col 
            for col in source_df.columns
        }
        target_col_repr_dict = {
            encoder.encode(target_df, col, target_table_name): col 
            for col in target_df.columns
        }

        cleaned_input_cols = list(input_col_repr_dict.keys())
        cleaned_target_cols = list(target_col_repr_dict.keys())

        input_embeddings = self._get_embeddings(cleaned_input_cols, self.use_prompt_query)
        target_embeddings = self._get_embeddings(cleaned_target_cols)

        top_k = min(self.topk, len(cleaned_target_cols))
        similarities, indices = compute_cosine_similarity_simple(
            input_embeddings, target_embeddings, top_k
        )

        candidates = {}
        for i, input_col in enumerate(cleaned_input_cols):
            original_input = input_col_repr_dict[input_col]
            for j in range(top_k):
                target_idx = indices[i, j]
                similarity = similarities[i, j].item()
                if similarity >= self.embedding_threshold:
                    original_target = target_col_repr_dict[cleaned_target_cols[target_idx]]
                    candidates[(original_input, original_target)] = similarity

        return candidates