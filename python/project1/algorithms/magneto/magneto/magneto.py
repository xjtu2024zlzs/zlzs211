import os
from typing import Dict, Tuple, Any
import pandas as pd

from magneto.basic_matcher import get_str_similarity_candidates
from magneto.bp_reranker import arrange_bipartite_matches
from magneto.embedding_matcher import DEFAULT_MODELS, EmbeddingMatcher
from magneto.llm_reranker import LLMReranker
from magneto.utils.utils import (
    clean_df,
    convert_to_valentine_format,
    get_samples,
    remove_invalid_characters,
)
from magneto.utils.dataframe_table import DataframeTable

os.environ["TOKENIZERS_PARALLELISM"] = "false"

class Magneto:
    """
    Magneto 封装了多个列匹配策略，支持字符串相似度、嵌入向量、列名完全一致等手段，
    并可选用二分图或 LLM 重排序器来进一步提升结果质量。
    """

    DEFAULT_PARAMS = {
        "embedding_model": "mpnet",
        "llm_model": "gpt-4o-mini",
        "llm_model_kwargs": {},
        "encoding_mode": "header_values_verbose",
        "sampling_mode": "mixed",
        "sampling_size": 10,
        "topk": 20,
        "include_strsim_matches": False,
        "include_embedding_matches": True,
        "embedding_threshold": 0.1,
        "include_equal_matches": True,
        "use_bp_reranker": True,
        "use_gpt_reranker": False,
        "gpt_only": False,
    }

    def __init__(self, **kwargs: Any) -> None:
        """
        初始化 Magneto，可通过关键字参数覆盖默认配置。

        参数:
            **kwargs: 控制匹配流程的开关，例如编码模式、Top-K 数量等。
        """
        # 将自定义参数与默认配置合并，便于进行消融实验
        self.params = {**self.DEFAULT_PARAMS, **kwargs}

    def apply_strsim_matches(self) -> None:
        """
        若开启 include_strsim_matches，则计算列名的字符串相似度并写入 self.input_sim_map。
        """
        if self.params["include_strsim_matches"]:
            strsim_candidates = get_str_similarity_candidates(
                self.df_source.columns, self.df_target.columns
            )
            for (source_col, target_col), score in strsim_candidates.items():
                self.input_sim_map[source_col][target_col] = score

    def apply_embedding_matches(self) -> None:
        """
        若 include_embedding_matches 为真，则调用 EmbeddingMatcher 计算列嵌入的余弦相似度。
        """
        if not self.params["include_embedding_matches"]:
            return

        embeddingMatcher = EmbeddingMatcher(params=self.params)

        # 传递表名（如果有）
        embedding_candidates = embeddingMatcher.get_embedding_similarity_candidates(
            self.df_source, self.df_target,
            source_table_name=self.source_table_name,
            target_table_name=self.target_table_name
        )
        for (col_source, col_target), score in embedding_candidates.items():
            self.input_sim_map[col_source][col_target] = score

    def apply_equal_matches(self) -> None:
        """
        若 include_equal_matches 为真，则对清洗后列名完全一致的列直接赋予 1.0 的匹配得分。
        """
        if self.params["include_equal_matches"]:
            source_cols_cleaned = {
                col: remove_invalid_characters(col.strip().lower())
                for col in self.df_source.columns
            }
            target_cols_cleaned = {
                col: remove_invalid_characters(col.strip().lower())
                for col in self.df_target.columns
            }

            for source_col, cand_source in source_cols_cleaned.items():
                for target_col, cand_target in target_cols_cleaned.items():
                    if cand_source == cand_target:
                        self.input_sim_map[source_col][target_col] = 1.0

    def get_top_k_matches(self, col_matches: Dict[str, float]) -> list:
        """
        对匹配得分进行排序并截断到 Top-K。

        参数:
            col_matches: {列名: 相似度} 的映射。

        返回:
            列出得分最高的若干 (列名, 得分) 元组。
        """
        sorted_matches = sorted(
            col_matches.items(), key=lambda item: item[1], reverse=True
        )
        top_k_matches = sorted_matches[: self.params["topk"]]
        return [(col, score) for col, score in top_k_matches]

    def _build_schema_contexts(self):
        """当 source_schema / target_schema 存在时，为所有列构建结构上下文字典。"""
        from algorithms.similarity_flooding.sql_schema_parser import build_column_context

        source_schema = self.params.get("source_schema")
        target_schema = self.params.get("target_schema")
        if source_schema is None or target_schema is None:
            return None, None

        src_table = self.source_table_name
        tgt_table = self.target_table_name
        if src_table is None or tgt_table is None:
            return None, None

        src_ctx = {}
        for col in self.df_source.columns:
            src_ctx[col] = build_column_context(source_schema, src_table, col)

        tgt_ctx = {}
        for col in self.df_target.columns:
            tgt_ctx[col] = build_column_context(target_schema, tgt_table, col)

        return src_ctx, tgt_ctx

    def call_llm_reranker(self, source_table: DataframeTable, target_table: DataframeTable, 
                          matches: Dict[Tuple[Tuple[str, str], Tuple[str, str]], float]) -> dict:
        """
        在 use_gpt_reranker 为真时，利用 LLM 对输入的 Valentine 格式匹配结果重新排序。
        若 source_schema / target_schema 存在，自动注入图上下文。
        """
        source_df = source_table.get_df()
        target_df = target_table.get_df()

        reranker = LLMReranker(
            llm_model=self.params["llm_model"],
            **self.params.get("llm_model_kwargs", {})
        )

        source_values = {
            col: get_samples(source_df[col], 10) for col in source_df.columns
        }
        target_values = {
            col: get_samples(target_df[col], 10) for col in target_df.columns
        }

        matched_columns = {}
        for entry, score in matches.items():
            source_col = entry[0][1]
            target_col = entry[1][1]
            if source_col not in matched_columns:
                matched_columns[source_col] = [(target_col, score)]
            else:
                matched_columns[source_col].append((target_col, score))

        src_ctx, tgt_ctx = self._build_schema_contexts()

        matched_columns = reranker.rematch(
            source_df,
            target_df,
            source_values,
            target_values,
            matched_columns,
            source_schema_context=src_ctx,
            target_schema_context=tgt_ctx,
        )

        return matched_columns
    
    def apply_strategies_in_order(self, order: Dict[str, int]) -> None:
        """
        根据用户提供的执行顺序依次运行匹配策略，值为 -1 的策略会被跳过。
        """
        strategy_functions = {
            "strsim": self.apply_strsim_matches,
            "embedding": self.apply_embedding_matches,
            "equal": self.apply_equal_matches,
        }

        order = {k: v for k, v in order.items() if v != -1}
        sorted_strategies = sorted(order.items(), key=lambda item: item[1])

        for strategy, _ in sorted_strategies:
            strategy_functions[strategy]()

    def get_matches(
        self, source: pd.DataFrame, target: pd.DataFrame,
        source_table_name: str = None, target_table_name: str = None
    ) -> Dict[Tuple[Tuple[str, str], Tuple[str, str]], float]:
        """
        主入口：给定源、目标 DataFrame，返回 Valentine 规范的匹配结果。
        
        Args:
            source: 源DataFrame
            target: 目标DataFrame
            source_table_name: 源表名（可选，用于表名上下文）
            target_table_name: 目标表名（可选，用于表名上下文）
        """
        # 使用实际表名或默认值
        source_table = DataframeTable(source, source_table_name or "source")
        target_table = DataframeTable(target, target_table_name or "target")
        self.df_source = clean_df(source)
        self.df_target = clean_df(target)
        
        # 保存表名供后续使用
        self.source_table_name = source_table_name
        self.target_table_name = target_table_name

        if len(self.df_source.columns) == 0 or len(self.df_target.columns) == 0:
            return {}

        # 维护列到列的得分映射；后续的“更强”匹配器会覆盖同一列对的旧得分

        if self.params["gpt_only"]:
            self.input_sim_map = {col: [] for col in self.df_source.columns}
            # 在纯 GPT 模式下，把所有目标列都交给 LLM 做最终筛选
            for col in self.df_source.columns:
                self.input_sim_map[col] = [(tgt_col, 0.0) for tgt_col in self.df_target.columns]

            matches = convert_to_valentine_format(
                self.input_sim_map, source_table.name, target_table.name
            )
            matches = self.call_llm_reranker(source_table, target_table, matches)
            matches = convert_to_valentine_format(
                matches, source_table.name, target_table.name
            )
        
        else:
            self.input_sim_map = {col: {} for col in self.df_source.columns}
            
            if "strategy_order" in self.params:
                self.apply_strategies_in_order(self.params["strategy_order"])
            else:
                match_strategies = [
                    self.apply_strsim_matches,
                    self.apply_embedding_matches,
                    self.apply_equal_matches,
                ]

                for strategy in match_strategies:
                    strategy()  # 执行策略并更新 input_sim_map

            # 针对每个源列只保留前 K 个目标列
            for col_source in self.input_sim_map:
                self.input_sim_map[col_source] = self.get_top_k_matches(
                    self.input_sim_map[col_source]
                )

            matches = convert_to_valentine_format(
                self.input_sim_map, source_table.name, target_table.name
            )

            if self.params["use_bp_reranker"]:
                matches = arrange_bipartite_matches(
                    matches,
                    self.df_source,
                    source_table.name,
                    self.df_target,
                    target_table.name,
                )

            if self.params["use_gpt_reranker"]:
                print("Applying LLM reranker")
                matches = self.call_llm_reranker(source_table, target_table, matches)
                matches = convert_to_valentine_format(
                    matches, source_table.name, target_table.name
                )

        return matches