"""
全局 Top-K 匹配器

将 unknown_table 模式从"按表对匹配"重构为"全局 top-K"。
所有 5 种方法（Magneto / MagnetoBoost / MagnetoGPT / MagnetoGPT_neo4j / MagnetoGPT_neo4j2）
共用全局候选检索，只是后续处理逻辑不同。
"""

import torch
import torch.nn.functional as F
import pandas as pd
from typing import Dict, List, Optional, Set, Tuple

from magneto.llm_reranker import LLMReranker
from magneto.utils.utils import get_samples


class GlobalMatcher:
    """全局 top-K 匹配器。

    所有方法共用嵌入缓存和全局候选检索逻辑，
    通过 match_xxx() 方法分发到不同的后续处理。
    """

    def __init__(
        self,
        source_schemas: Dict,
        target_schema,
        embedding_cache: Dict[str, torch.Tensor],
        llm_model: str = None,
        llm_model_kwargs: dict = None,
        neo4j_graph=None,
        boost_threshold: float = 0.9,
        boost_alpha: float = 0.15,
        topk: int = 20,
        device: str = None,
    ):
        self.source_schemas = source_schemas
        self.target_schema = target_schema
        self._embedding_cache = embedding_cache
        self.llm_model = llm_model
        self.neo4j_graph = neo4j_graph
        self.boost_threshold = boost_threshold
        self.boost_alpha = boost_alpha
        self.topk = topk
        self.device = device or ("cuda" if torch.cuda.is_available() else "cpu")

        self._reranker = None
        if llm_model and llm_model.lower() != "none":
            self._reranker = LLMReranker(
                llm_model=llm_model,
                **(llm_model_kwargs or {}),
            )

    # ==================================================================
    # 全局 Top-K 计算
    # ==================================================================

    def compute_global_topk(
        self,
        src_table: str,
        src_df: pd.DataFrame,
        all_target_dfs: Dict[str, pd.DataFrame],
        topk: int = None,
    ) -> Dict[str, List[Tuple[str, str, float]]]:
        """计算一个源表的每个列与所有目标列的余弦相似度，取全局 top-K。

        Returns:
            {src_col: [(tgt_table, tgt_col, score), ...]}
        """
        topk = topk or self.topk

        src_cols = list(src_df.columns)
        src_tensors = []
        src_valid_cols = []
        for col in src_cols:
            key = f"{src_table}.{col}"
            if key in self._embedding_cache:
                src_tensors.append(self._embedding_cache[key])
                src_valid_cols.append(col)

        if not src_tensors:
            return {}

        tgt_keys = []
        tgt_tensors = []
        for tgt_table, tgt_df in all_target_dfs.items():
            for col in tgt_df.columns:
                key = f"{tgt_table}.{col}"
                if key in self._embedding_cache:
                    tgt_keys.append((tgt_table, col))
                    tgt_tensors.append(self._embedding_cache[key])

        if not tgt_tensors:
            return {}

        S = torch.stack(src_tensors).to(self.device)
        T = torch.stack(tgt_tensors).to(self.device)
        S = F.normalize(S, dim=1)
        T = F.normalize(T, dim=1)

        sim = S @ T.T
        k = min(topk, len(tgt_keys))
        scores, indices = torch.topk(sim, k, dim=1)

        result = {}
        for i, src_col in enumerate(src_valid_cols):
            cands = []
            for j in range(k):
                idx = indices[i, j].item()
                score = scores[i, j].item()
                if score > 0:
                    tgt_table, tgt_col = tgt_keys[idx]
                    cands.append((tgt_table, tgt_col, score))
            result[src_col] = cands
        return result

    # ==================================================================
    # 辅助：收集样本值、构建 schema 上下文
    # ==================================================================

    def _collect_source_values(self, src_df: pd.DataFrame) -> Dict[str, list]:
        return {col: get_samples(src_df[col], 10) for col in src_df.columns}

    def _collect_all_target_values(
        self, all_target_dfs: Dict[str, pd.DataFrame],
    ) -> Dict[str, Dict[str, list]]:
        result = {}
        for tgt_table, tgt_df in all_target_dfs.items():
            result[tgt_table] = {
                col: get_samples(tgt_df[col], 10) for col in tgt_df.columns
            }
        return result

    def _build_source_context(self, src_table: str, src_schema_name: str, src_df) -> Dict[str, str]:
        from algorithms.similarity_flooding.sql_schema_parser import build_column_context
        schema = self.source_schemas.get(src_schema_name)
        if schema is None:
            return {"__table__": src_table}
        ctx = {"__table__": src_table}
        for col in src_df.columns:
            ctx[col] = build_column_context(schema, src_table, col)
        return ctx

    def _build_target_context(
        self, global_topk: Dict[str, List[Tuple[str, str, float]]],
    ) -> Dict[Tuple[str, str], str]:
        from algorithms.similarity_flooding.sql_schema_parser import build_column_context
        ctx = {}
        seen = set()
        for cands in global_topk.values():
            for tgt_table, tgt_col, _ in cands:
                if (tgt_table, tgt_col) not in seen:
                    seen.add((tgt_table, tgt_col))
                    ctx[(tgt_table, tgt_col)] = build_column_context(
                        self.target_schema, tgt_table, tgt_col,
                    )
        return ctx

    def _get_anchor_columns_from_schema(self, schema, table_name: str) -> Set[str]:
        """从 SchemaInfo 提取锚点列（不依赖 Neo4j）。"""
        anchors = set()
        table_info = schema.tables.get(table_name)
        if table_info is None:
            return anchors
        if table_info.primary_key:
            anchors.add(table_info.primary_key)
        for fk in table_info.foreign_keys:
            if fk.source_table == table_name:
                anchors.add(fk.source_column)
        for other_table in schema.tables.values():
            for fk in other_table.foreign_keys:
                if fk.target_table == table_name:
                    anchors.add(fk.target_column)
        return anchors

    def _get_target_anchor_columns(self, table_name: str) -> Set[str]:
        return self._get_anchor_columns_from_schema(self.target_schema, table_name)

    # ==================================================================
    # 方法 1: Magneto（纯嵌入基线）
    # ==================================================================

    def match_magneto(
        self,
        global_topk: Dict[str, List[Tuple[str, str, float]]],
        src_table: str,
    ) -> Dict:
        """直接将全局 top-K 转为 Valentine 格式。"""
        matches = {}
        for src_col, cands in global_topk.items():
            for tgt_table, tgt_col, score in cands:
                key = ((src_table, src_col), (tgt_table, tgt_col))
                matches[key] = score
        return matches

    # ==================================================================
    # 方法 2: MagnetoBoost（约束增强）
    # ==================================================================

    def match_boost(
        self,
        global_topk: Dict[str, List[Tuple[str, str, float]]],
        src_table: str,
        src_schema_name: str,
    ) -> Dict:
        """对全局 top-K 施加约束增强（只升不降）。

        逻辑与 ConstraintBoostMatcher._apply_constraint_boost 一致：
        1. 找到锚点列对（双侧 PK/FK，嵌入分 >= threshold）
        2. 对来自同一目标表的其他候选施加 boost
        """
        src_schema = self.source_schemas.get(src_schema_name)
        anchor_src = self._get_anchor_columns_from_schema(src_schema, src_table) if src_schema else set()

        anchor_pairs = {}
        for src_col in anchor_src:
            if src_col not in global_topk:
                continue
            for tgt_table, tgt_col, score in global_topk[src_col]:
                if score >= 0.8:
                    pair_key = (src_table, tgt_table)
                    if pair_key not in anchor_pairs:
                        anchor_pairs[pair_key] = []
                    anchor_pairs[pair_key].append(score)

        table_boost = {}
        for (st, tt), scores in anchor_pairs.items():
            avg = sum(scores) / len(scores)
            table_boost[tt] = self.boost_alpha * avg

        matches = {}
        for src_col, cands in global_topk.items():
            for tgt_table, tgt_col, score in cands:
                boost = table_boost.get(tgt_table, 0.0)
                key = ((src_table, src_col), (tgt_table, tgt_col))
                matches[key] = min(1.0, score + boost)
        return matches

    # ==================================================================
    # 方法 3: MagnetoGPT（LLM 重排，无图上下文）
    # ==================================================================

    def match_gpt(
        self,
        global_topk: Dict[str, List[Tuple[str, str, float]]],
        src_table: str,
        src_df: pd.DataFrame,
        all_target_dfs: Dict[str, pd.DataFrame],
    ) -> Dict:
        src_values = self._collect_source_values(src_df)
        all_tgt_values = self._collect_all_target_values(all_target_dfs)
        src_ctx = {"__table__": src_table}

        matches = {}
        for src_col, candidates in global_topk.items():
            if not candidates:
                continue
            results = self._reranker.rematch_global(
                src_col, candidates, src_values, all_tgt_values,
                source_schema_context=src_ctx,
            )
            for tgt_table, tgt_col, score in results:
                key = ((src_table, src_col), (tgt_table, tgt_col))
                matches[key] = score
        return matches

    # ==================================================================
    # 方法 4: MagnetoGPT_neo4j（LLM + 图上下文）
    # ==================================================================

    def match_gpt_neo4j(
        self,
        global_topk: Dict[str, List[Tuple[str, str, float]]],
        src_table: str,
        src_schema_name: str,
        src_df: pd.DataFrame,
        all_target_dfs: Dict[str, pd.DataFrame],
    ) -> Dict:
        src_values = self._collect_source_values(src_df)
        all_tgt_values = self._collect_all_target_values(all_target_dfs)
        src_ctx = self._build_source_context(src_table, src_schema_name, src_df)
        tgt_ctx = self._build_target_context(global_topk)

        matches = {}
        for src_col, candidates in global_topk.items():
            if not candidates:
                continue
            results = self._reranker.rematch_global(
                src_col, candidates, src_values, all_tgt_values,
                source_schema_context=src_ctx,
                target_schema_context=tgt_ctx,
            )
            for tgt_table, tgt_col, score in results:
                key = ((src_table, src_col), (tgt_table, tgt_col))
                matches[key] = score
        return matches

    # ==================================================================
    # 方法 5: MagnetoGPT_neo4j2（两轮 LLM + 语义边传播）
    # ==================================================================

    def match_gpt_neo4j2(
        self,
        global_topk: Dict[str, List[Tuple[str, str, float]]],
        src_table: str,
        src_schema_name: str,
        src_df: pd.DataFrame,
        all_target_dfs: Dict[str, pd.DataFrame],
        dataset_name: str = "",
    ) -> Dict:
        src_values = self._collect_source_values(src_df)
        all_tgt_values = self._collect_all_target_values(all_target_dfs)
        src_ctx = self._build_source_context(src_table, src_schema_name, src_df)
        tgt_ctx = self._build_target_context(global_topk)

        src_schema = self.source_schemas.get(src_schema_name)
        anchor_src = self._get_anchor_columns_from_schema(src_schema, src_table) if src_schema else set()

        anchor_topk = {c: v for c, v in global_topk.items() if c in anchor_src}
        non_anchor_topk = {c: v for c, v in global_topk.items() if c not in anchor_src}

        matches = {}

        # --- Pass 1: 重排锚点列（图上下文，无语义边）---
        for src_col, candidates in anchor_topk.items():
            if not candidates:
                continue
            results = self._reranker.rematch_global(
                src_col, candidates, src_values, all_tgt_values,
                source_schema_context=src_ctx,
                target_schema_context=tgt_ctx,
            )
            for tgt_table, tgt_col, score in results:
                key = ((src_table, src_col), (tgt_table, tgt_col))
                matches[key] = score

            if results and self.neo4j_graph:
                for tgt_table, tgt_col, score in results:
                    if score >= 0.85:
                        self.neo4j_graph.add_semantic_edge(
                            src_table, src_col,
                            tgt_table, tgt_col,
                            score, dataset=dataset_name,
                        )

        # --- Pass 2: 重排非锚点列（图上下文 + 语义边）---
        for src_col, candidates in non_anchor_topk.items():
            if not candidates:
                continue

            semantic_edges = []
            if self.neo4j_graph:
                cand_tables = list(set(t for t, c, s in candidates))
                semantic_edges = self.neo4j_graph.get_semantic_edges(src_table, cand_tables)

            results = self._reranker.rematch_global(
                src_col, candidates, src_values, all_tgt_values,
                source_schema_context=src_ctx,
                target_schema_context=tgt_ctx,
                semantic_edges=semantic_edges if semantic_edges else None,
            )
            for tgt_table, tgt_col, score in results:
                key = ((src_table, src_col), (tgt_table, tgt_col))
                matches[key] = score

        return matches
