from __future__ import annotations

from pathlib import Path

import pandas as pd

from ..domain import GroundTruthRow


class GroundTruthCatalog:
    def __init__(self, rows: list[GroundTruthRow]) -> None:
        self.rows = rows

    @classmethod
    def from_file_root(cls, root: Path) -> "GroundTruthCatalog":
        all_mappings = root / "_all_mappings.csv"
        if not all_mappings.is_file():
            raise FileNotFoundError(f"Ground truth file not found: {all_mappings}")
        frame = pd.read_csv(all_mappings)
        rows = [
            GroundTruthRow(
                source_database=str(row["source_database"]).upper(),
                source_table=str(row["source_table"]),
                source_column=str(row["source_column"]),
                target_table=str(row["target_table"]),
                target_column=str(row["target_column"]),
                mapping_type=_optional_str(row.get("mapping_type")),
                difficulty=_optional_str(row.get("difficulty")),
                notes=_optional_str(row.get("notes")),
            )
            for _, row in frame.iterrows()
        ]
        return cls(rows)

    def for_source_system(self, source_system: str) -> list[GroundTruthRow]:
        label = source_system.upper()
        return [row for row in self.rows if row.source_database.upper() == label]

    def lookup(self) -> dict[tuple[str, str, str, str, str], GroundTruthRow]:
        return {
            (
                row.source_database.upper(),
                row.source_table,
                row.source_column,
                row.target_table,
                row.target_column,
            ): row
            for row in self.rows
        }


def _optional_str(value) -> str | None:
    if value is None:
        return None
    text = str(value)
    if text == "nan":
        return None
    return text

