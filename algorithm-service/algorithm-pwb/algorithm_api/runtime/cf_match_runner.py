from __future__ import annotations

import os
import time
from dataclasses import dataclass
from functools import lru_cache
from typing import Any

import pandas as pd
import torch
import torch.nn.functional as F

from ..domain import DataBundle, GroundTruthRow, MatchRow
from .metrics_evaluator import MetricBundle, evaluate_matches


MODEL_MAP = {
    "mpnet": "sentence-transformers/all-mpnet-base-v2",
    "roberta": "sentence-transformers/all-roberta-large-v1",
    "e5": "intfloat/e5-base",
    "arctic": "Snowflake/snowflake-arctic-embed-l-v2.0",
    "minilm": "sentence-transformers/all-MiniLM-L6-v2",
}


@dataclass(frozen=True)
class RunnerConfig:
    method: str
    embedding_model: str
    encoding_mode: str
    sampling_mode: str
    sampling_size: int
    topk: int
    threshold: float
    boost_threshold: float
    boost_alpha: float
    llm_model: str | None = None
    llm_model_kwargs: dict[str, Any] | None = None


@dataclass(frozen=True)
class RunnerResult:
    rows: list[MatchRow]
    metrics: MetricBundle
    runtime_seconds: float


@dataclass(frozen=True)
class _ColumnRef:
    side: str
    table: str
    column: str
    cache_key: str


class CfMatchRunner:
    def __init__(self, config: RunnerConfig) -> None:
        self.config = config
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

    def run(
        self,
        *,
        source_bundle: DataBundle,
        target_bundle: DataBundle,
        source_schema: Any,
        target_schema: Any,
        ground_truth: list[GroundTruthRow],
    ) -> RunnerResult:
        start = time.time()
        source_dfs = _non_empty_dataframes(source_bundle.dataframes)
        target_dfs = _non_empty_dataframes(target_bundle.dataframes)
        if not source_dfs:
            raise ValueError("No source dataframes were loaded from the CF API Pull adapter.")
        if not target_dfs:
            raise ValueError("No target dossier dataframes were loaded.")

        source_refs, target_refs = self._build_refs(source_dfs, target_dfs)
        embeddings = self._encode_refs(source_refs + target_refs, source_dfs, target_dfs)
        topk_map = self._compute_topk(source_refs, target_refs, embeddings)

        if self.config.method == "MagnetoBoost":
            rows = self._match_boost(source_bundle, target_bundle, source_schema, topk_map)
        elif self.config.method == "MagnetoGPT":
            rows = self._match_gpt(source_bundle, target_bundle, source_dfs, target_dfs, topk_map)
        else:
            rows = self._match_magneto(source_bundle, target_bundle, topk_map)

        metrics = evaluate_matches(rows, ground_truth)
        return RunnerResult(
            rows=rows,
            metrics=metrics,
            runtime_seconds=time.time() - start,
        )

    def _build_refs(
        self,
        source_dfs: dict[str, pd.DataFrame],
        target_dfs: dict[str, pd.DataFrame],
    ) -> tuple[list[_ColumnRef], list[_ColumnRef]]:
        source_refs = [
            _ColumnRef("source", table, column, f"source::{table}.{column}")
            for table, df in source_dfs.items()
            for column in df.columns
        ]
        target_refs = [
            _ColumnRef("target", table, column, f"target::{table}.{column}")
            for table, df in target_dfs.items()
            for column in df.columns
        ]
        return source_refs, target_refs

    def _encode_refs(
        self,
        refs: list[_ColumnRef],
        source_dfs: dict[str, pd.DataFrame],
        target_dfs: dict[str, pd.DataFrame],
    ) -> dict[str, torch.Tensor]:
        from magneto.column_encoder import ColumnEncoder  # type: ignore

        model, tokenizer = _load_model(self.config.embedding_model, str(self.device))
        encoder = ColumnEncoder(
            tokenizer,
            encoding_mode=self.config.encoding_mode,
            sampling_mode=self.config.sampling_mode,
            n_samples=self.config.sampling_size,
        )

        texts: list[str] = []
        keys: list[str] = []
        for ref in refs:
            df = source_dfs[ref.table] if ref.side == "source" else target_dfs[ref.table]
            texts.append(encoder.encode(df, ref.column, table_name=ref.table))
            keys.append(ref.cache_key)

        if not texts:
            return {}

        with torch.no_grad():
            encoded = model.encode(
                texts,
                convert_to_tensor=True,
                show_progress_bar=False,
                device=self.device,
                batch_size=64,
            )
        return {key: encoded[index].detach() for index, key in enumerate(keys)}

    def _compute_topk(
        self,
        source_refs: list[_ColumnRef],
        target_refs: list[_ColumnRef],
        embeddings: dict[str, torch.Tensor],
    ) -> dict[_ColumnRef, list[tuple[_ColumnRef, float]]]:
        result: dict[_ColumnRef, list[tuple[_ColumnRef, float]]] = {}
        if not source_refs or not target_refs:
            return result

        source_tensors = torch.stack([embeddings[ref.cache_key] for ref in source_refs]).to(self.device)
        target_tensors = torch.stack([embeddings[ref.cache_key] for ref in target_refs]).to(self.device)
        source_tensors = F.normalize(source_tensors, dim=1)
        target_tensors = F.normalize(target_tensors, dim=1)
        sim = source_tensors @ target_tensors.T
        k = min(self.config.topk, len(target_refs))
        scores, indices = torch.topk(sim, k, dim=1)

        for src_index, src_ref in enumerate(source_refs):
            candidates: list[tuple[_ColumnRef, float]] = []
            for rank_index in range(k):
                target_index = int(indices[src_index, rank_index].item())
                score = float(scores[src_index, rank_index].item())
                if score >= self.config.threshold:
                    candidates.append((target_refs[target_index], score))
            result[src_ref] = candidates
        return result

    def _match_magneto(
        self,
        source_bundle: DataBundle,
        target_bundle: DataBundle,
        topk_map: dict[_ColumnRef, list[tuple[_ColumnRef, float]]],
    ) -> list[MatchRow]:
        return self._rows_from_topk(source_bundle, target_bundle, topk_map, method=self.config.method)

    def _match_boost(
        self,
        source_bundle: DataBundle,
        target_bundle: DataBundle,
        source_schema: Any,
        topk_map: dict[_ColumnRef, list[tuple[_ColumnRef, float]]],
    ) -> list[MatchRow]:
        table_boost: dict[tuple[str, str], float] = {}
        for source_ref, candidates in topk_map.items():
            if not _is_anchor_column(source_schema, source_ref.table, source_ref.column):
                continue
            for target_ref, score in candidates:
                if score >= self.config.boost_threshold:
                    key = (source_ref.table, target_ref.table)
                    table_boost[key] = max(table_boost.get(key, 0.0), self.config.boost_alpha * score)

        boosted: dict[_ColumnRef, list[tuple[_ColumnRef, float]]] = {}
        for source_ref, candidates in topk_map.items():
            adjusted = []
            for target_ref, score in candidates:
                boost = table_boost.get((source_ref.table, target_ref.table), 0.0)
                adjusted.append((target_ref, min(1.0, score + boost)))
            adjusted.sort(key=lambda item: item[1], reverse=True)
            boosted[source_ref] = adjusted[: self.config.topk]

        return self._rows_from_topk(source_bundle, target_bundle, boosted, method=self.config.method)

    def _match_gpt(
        self,
        source_bundle: DataBundle,
        target_bundle: DataBundle,
        source_dfs: dict[str, pd.DataFrame],
        target_dfs: dict[str, pd.DataFrame],
        topk_map: dict[_ColumnRef, list[tuple[_ColumnRef, float]]],
    ) -> list[MatchRow]:
        from magneto.llm_reranker import LLMReranker  # type: ignore
        from magneto.utils.utils import get_samples  # type: ignore

        if not self.config.llm_model:
            return self._rows_from_topk(source_bundle, target_bundle, topk_map, method="Magneto")

        reranker = LLMReranker(
            llm_model=self.config.llm_model,
            **(self.config.llm_model_kwargs or {}),
        )
        reranked: dict[_ColumnRef, list[tuple[_ColumnRef, float]]] = {}
        target_values = {
            table: {column: get_samples(df[column], 10) for column in df.columns}
            for table, df in target_dfs.items()
        }

        for source_ref, candidates in topk_map.items():
            src_df = source_dfs[source_ref.table]
            source_values = {column: get_samples(src_df[column], 10) for column in src_df.columns}
            candidate_payload = [(target.table, target.column, score) for target, score in candidates]
            result = reranker.rematch_global(
                source_ref.column,
                candidate_payload,
                source_values,
                target_values,
                source_schema_context={"__table__": source_ref.table},
            )
            ref_lookup = {(ref.table, ref.column): ref for ref, _ in candidates}
            reranked[source_ref] = [
                (ref_lookup[(table, column)], float(score))
                for table, column, score in result
                if (table, column) in ref_lookup
            ]
        return self._rows_from_topk(source_bundle, target_bundle, reranked, method=self.config.method)

    def _rows_from_topk(
        self,
        source_bundle: DataBundle,
        target_bundle: DataBundle,
        topk_map: dict[_ColumnRef, list[tuple[_ColumnRef, float]]],
        *,
        method: str,
    ) -> list[MatchRow]:
        rows: list[MatchRow] = []
        for source_ref, candidates in topk_map.items():
            for rank, (target_ref, score) in enumerate(
                sorted(candidates, key=lambda item: item[1], reverse=True),
                start=1,
            ):
                target_schema = target_bundle.schemas.get(target_ref.table)
                rows.append(
                    MatchRow(
                        source_database=source_bundle.source_label.upper(),
                        source_table=source_ref.table,
                        source_column=source_ref.column,
                        target_table=target_ref.table,
                        target_column=target_ref.column,
                        target_physical_table=target_schema.physical_table_name if target_schema else None,
                        score=score,
                        rank=rank,
                        method=method,
                        raw_payload={
                            "sourceSchemaKey": source_bundle.schema_key,
                            "targetSchemaKey": target_bundle.schema_key,
                        },
                    )
                )
        rows.sort(key=lambda item: (-item.score, item.source_table, item.source_column))
        return rows


@lru_cache(maxsize=8)
def _load_model(embedding_model: str, device: str):
    from sentence_transformers import SentenceTransformer  # type: ignore
    from transformers import AutoTokenizer  # type: ignore

    model_path = MODEL_MAP.get(embedding_model, embedding_model)
    if embedding_model not in MODEL_MAP and os.path.exists(embedding_model):
        model_path = embedding_model
    model = SentenceTransformer(model_path, device=device)
    model.eval()
    tokenizer_path = MODEL_MAP.get(embedding_model, MODEL_MAP["mpnet"])
    tokenizer = AutoTokenizer.from_pretrained(tokenizer_path)
    return model, tokenizer


def _non_empty_dataframes(dataframes: dict[str, pd.DataFrame]) -> dict[str, pd.DataFrame]:
    return {
        table: frame
        for table, frame in dataframes.items()
        if frame is not None and len(frame.columns) > 0
    }


def _is_anchor_column(schema: Any, table_name: str, column_name: str) -> bool:
    table = getattr(schema, "tables", {}).get(table_name)
    if table is None:
        return False
    if getattr(table, "primary_key", None) == column_name:
        return True
    for fk in getattr(table, "foreign_keys", []):
        if getattr(fk, "source_column", None) == column_name:
            return True
    for other in getattr(schema, "tables", {}).values():
        for fk in getattr(other, "foreign_keys", []):
            if getattr(fk, "target_table", None) == table_name and getattr(fk, "target_column", None) == column_name:
                return True
    return False

