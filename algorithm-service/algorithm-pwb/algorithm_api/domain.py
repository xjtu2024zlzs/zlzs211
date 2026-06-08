from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any

import pandas as pd


@dataclass(frozen=True)
class TableSchema:
    table_name: str
    physical_table_name: str | None = None
    source_database: str | None = None
    row_count: int = 0
    columns: list[dict[str, Any]] = field(default_factory=list)
    foreign_keys: list[dict[str, Any]] = field(default_factory=list)


@dataclass
class DataBundle:
    system_key: str
    source_label: str
    schema_key: str
    schemas: dict[str, TableSchema]
    dataframes: dict[str, pd.DataFrame]
    raw_schema_payload: dict[str, Any] | None = None
    raw_pull_payload: dict[str, Any] | None = None


@dataclass(frozen=True)
class GroundTruthRow:
    source_database: str
    source_table: str
    source_column: str
    target_table: str
    target_column: str
    mapping_type: str | None = None
    difficulty: str | None = None
    notes: str | None = None


@dataclass(frozen=True)
class MatchRow:
    source_database: str
    source_table: str
    source_column: str
    target_table: str
    target_column: str
    score: float
    rank: int
    method: str
    target_physical_table: str | None = None
    raw_payload: dict[str, Any] = field(default_factory=dict)

    @property
    def key(self) -> tuple[str, str, str, str, str]:
        return (
            self.source_database.upper(),
            self.source_table,
            self.source_column,
            self.target_table,
            self.target_column,
        )

