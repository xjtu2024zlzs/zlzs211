from __future__ import annotations

from algorithms.similarity_flooding.sql_schema_parser import (  # type: ignore
    ColumnInfo,
    ForeignKeyInfo,
    SchemaInfo,
    TableInfo,
)

from ..domain import DataBundle


def bundle_to_schema_info(bundle: DataBundle, schema_name: str | None = None) -> SchemaInfo:
    schema = SchemaInfo(name=schema_name or bundle.schema_key)
    for table_name, table_schema in bundle.schemas.items():
        table = TableInfo(name=table_name)
        for column in table_schema.columns:
            column_name = column.get("source_column") or column.get("field_name")
            if not column_name:
                continue
            is_primary_key = bool(column.get("is_primary_key"))
            column_info = ColumnInfo(
                name=column_name,
                data_type=str(column.get("data_type") or "unknown").upper(),
                is_primary_key=is_primary_key,
                is_nullable=bool(column.get("is_nullable", True)),
                comment=column.get("field_comment"),
            )
            table.columns[column_name] = column_info
            if is_primary_key and table.primary_key is None:
                table.primary_key = column_name

        for fk in table_schema.foreign_keys:
            source_column = fk.get("column_name") or fk.get("source_column")
            target_table = fk.get("referenced_table") or fk.get("target_table")
            target_column = fk.get("referenced_column") or fk.get("target_column")
            if not source_column or not target_table or not target_column:
                continue
            table.foreign_keys.append(
                ForeignKeyInfo(
                    constraint_name=fk.get("constraint_name") or f"fk_{table_name}_{source_column}",
                    source_table=table_name,
                    source_column=source_column,
                    target_table=target_table,
                    target_column=target_column,
                )
            )
        schema.tables[table_name] = table
    return schema

