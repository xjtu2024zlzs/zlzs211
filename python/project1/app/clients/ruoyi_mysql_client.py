from __future__ import annotations

from pathlib import Path
from typing import Any

import pandas as pd
from sqlalchemy import bindparam, create_engine, text

from ..domain import DataBundle, TableSchema


def _logical_table_name(physical_name: str, table_prefix: str) -> str:
    if physical_name.startswith(table_prefix):
        return physical_name[len(table_prefix) :]
    return physical_name


def _physical_table_name(logical_name: str, table_prefix: str) -> str:
    return logical_name if logical_name.startswith(table_prefix) else f"{table_prefix}{logical_name}"


def _quote_mysql(identifier: str) -> str:
    return "`" + identifier.replace("`", "``") + "`"


class RuoyiMysqlClient:
    def __init__(self, mysql_url: str, table_prefix: str = "p1p_dossier_") -> None:
        self.mysql_url = mysql_url
        self.table_prefix = table_prefix
        self.engine = create_engine(mysql_url, future=True, pool_pre_ping=True)

    def health(self) -> dict[str, Any]:
        with self.engine.connect() as conn:
            row = conn.execute(text("select database() as database_name")).mappings().one()
        return {"status": "ok", "database": row["database_name"], "tablePrefix": self.table_prefix}

    def _list_physical_tables(self, requested_tables: list[str]) -> list[str]:
        requested = {
            _physical_table_name(table.strip(), self.table_prefix)
            for table in requested_tables
            if table and table.strip()
        }
        query = """
            select table_name as table_name
            from information_schema.tables
            where table_schema = database()
              and table_type = 'BASE TABLE'
              and table_name like :table_like
            order by table_name
        """
        with self.engine.connect() as conn:
            rows = conn.execute(text(query), {"table_like": f"{self.table_prefix}%"}).mappings().all()
        names = [row["table_name"] for row in rows]
        if requested:
            names = [name for name in names if name in requested]
        return names

    def _load_columns(self, physical_tables: list[str]) -> dict[str, list[dict[str, Any]]]:
        if not physical_tables:
            return {}
        query = """
            select
                table_name as table_name,
                column_name as column_name,
                ordinal_position as ordinal_position,
                column_type as column_type,
                data_type as data_type,
                is_nullable as is_nullable,
                column_key as column_key,
                column_comment as column_comment
            from information_schema.columns
            where table_schema = database()
              and table_name in :table_names
            order by table_name, ordinal_position
        """
        with self.engine.connect() as conn:
            rows = conn.execute(
                text(query).bindparams(bindparam("table_names", expanding=True)),
                {"table_names": physical_tables},
            ).mappings().all()

        by_table: dict[str, list[dict[str, Any]]] = {}
        for row in rows:
            by_table.setdefault(row["table_name"], []).append(
                {
                    "field_name": row["column_name"],
                    "source_column": row["column_name"],
                    "data_type": row["column_type"] or row["data_type"],
                    "ordinal_position": int(row["ordinal_position"]),
                    "is_primary_key": row["column_key"] == "PRI",
                    "is_nullable": row["is_nullable"] == "YES",
                    "field_comment": row["column_comment"],
                    "sample_values": [],
                }
            )
        return by_table

    def _load_foreign_keys(self, physical_tables: list[str]) -> dict[str, list[dict[str, Any]]]:
        if not physical_tables:
            return {}
        query = """
            select
                kcu.table_name as table_name,
                kcu.constraint_name as constraint_name,
                kcu.column_name as column_name,
                kcu.referenced_table_name as referenced_table_name,
                kcu.referenced_column_name as referenced_column_name
            from information_schema.key_column_usage kcu
            where kcu.table_schema = database()
              and kcu.table_name in :table_names
              and kcu.referenced_table_name is not null
            order by kcu.table_name, kcu.constraint_name, kcu.ordinal_position
        """
        with self.engine.connect() as conn:
            rows = conn.execute(
                text(query).bindparams(bindparam("table_names", expanding=True)),
                {"table_names": physical_tables},
            ).mappings().all()

        by_table: dict[str, list[dict[str, Any]]] = {}
        for row in rows:
            by_table.setdefault(row["table_name"], []).append(
                {
                    "constraint_name": row["constraint_name"],
                    "column_name": row["column_name"],
                    "referenced_table": _logical_table_name(row["referenced_table_name"], self.table_prefix),
                    "referenced_column": row["referenced_column_name"],
                }
            )
        return by_table

    def _load_sample_values(
        self,
        physical_table: str,
        columns: list[dict[str, Any]],
        limit: int = 5,
    ) -> None:
        with self.engine.connect() as conn:
            for column in columns:
                column_name = column["field_name"]
                query = (
                    f"select distinct {_quote_mysql(column_name)} as sample_value "
                    f"from {_quote_mysql(physical_table)} "
                    f"where {_quote_mysql(column_name)} is not null limit :limit"
                )
                try:
                    rows = conn.execute(text(query), {"limit": limit}).mappings().all()
                    column["sample_values"] = [row["sample_value"] for row in rows]
                except Exception:
                    column["sample_values"] = []

    def _count_rows(self, physical_table: str) -> int:
        with self.engine.connect() as conn:
            row = conn.execute(
                text(f"select count(*) as total from {_quote_mysql(physical_table)}")
            ).mappings().one()
        return int(row["total"])

    def _load_dataframe(self, physical_table: str, limit_per_table: int) -> pd.DataFrame:
        with self.engine.connect() as conn:
            rows = conn.execute(
                text(f"select * from {_quote_mysql(physical_table)} limit :limit"),
                {"limit": limit_per_table},
            ).mappings().all()
        return pd.DataFrame([dict(row) for row in rows])

    def load_bundle(self, *, tables: list[str], limit_per_table: int) -> DataBundle:
        physical_tables = self._list_physical_tables(tables)
        columns_by_table = self._load_columns(physical_tables)
        foreign_keys_by_table = self._load_foreign_keys(physical_tables)

        schemas: dict[str, TableSchema] = {}
        dataframes: dict[str, pd.DataFrame] = {}
        for physical_table in physical_tables:
            logical_name = _logical_table_name(physical_table, self.table_prefix)
            columns = columns_by_table.get(physical_table, [])
            self._load_sample_values(physical_table, columns)
            schemas[logical_name] = TableSchema(
                table_name=logical_name,
                physical_table_name=physical_table,
                source_database="dossier",
                row_count=self._count_rows(physical_table),
                columns=columns,
                foreign_keys=foreign_keys_by_table.get(physical_table, []),
            )
            dataframes[logical_name] = self._load_dataframe(physical_table, limit_per_table)

        return DataBundle(
            system_key="dossier",
            source_label="dossier",
            schema_key="dossier",
            schemas=schemas,
            dataframes=dataframes,
        )


class DossierFileClient:
    def __init__(self, file_root: Path, table_prefix: str = "p1p_dossier_") -> None:
        self.file_root = file_root
        self.table_prefix = table_prefix

    def load_bundle(self, *, tables: list[str], limit_per_table: int) -> DataBundle:
        requested = {
            _logical_table_name(table.strip(), self.table_prefix)
            for table in tables
            if table and table.strip()
        }
        schemas: dict[str, TableSchema] = {}
        dataframes: dict[str, pd.DataFrame] = {}
        for path in sorted(self.file_root.glob("dossier_*.csv")):
            logical_name = path.stem.removeprefix("dossier_")
            if requested and logical_name not in requested:
                continue
            df = pd.read_csv(path).head(limit_per_table)
            columns = [
                {
                    "field_name": column,
                    "source_column": column,
                    "data_type": str(df[column].dtype),
                    "ordinal_position": index + 1,
                    "is_primary_key": column.endswith("_id"),
                    "is_nullable": True,
                    "sample_values": [str(value) for value in df[column].dropna().unique()[:5]],
                }
                for index, column in enumerate(df.columns)
            ]
            schemas[logical_name] = TableSchema(
                table_name=logical_name,
                physical_table_name=f"{self.table_prefix}{logical_name}",
                source_database="dossier",
                row_count=len(df),
                columns=columns,
            )
            dataframes[logical_name] = df
        return DataBundle(
            system_key="dossier",
            source_label="dossier",
            schema_key="dossier",
            schemas=schemas,
            dataframes=dataframes,
        )
