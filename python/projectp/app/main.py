from __future__ import annotations

from collections import defaultdict
from datetime import date, datetime, time, timezone
from decimal import Decimal
from uuid import uuid4

from fastapi import Body, FastAPI, HTTPException, Query
from pydantic import BaseModel, Field
from sqlalchemy import create_engine, text
from sqlalchemy.exc import SQLAlchemyError

from app.config import build_database_url, get_database_connection_info, get_system_config, get_table_prefix


APP_VERSION = "1.0.0"

config = get_system_config()
db_info = get_database_connection_info()
table_prefix = get_table_prefix(config)

engine = create_engine(
    build_database_url(),
    future=True,
    pool_pre_ping=True,
)

app = FastAPI(
    title=f"projectp CF {config.source_label} API Pull Adapter",
    version=APP_VERSION,
    description="FastAPI adapter that simulates an external enterprise source system from RuoYi MySQL tables.",
)


class PullTableRequest(BaseModel):
    source_table: str = Field(..., description="Logical source table name.")
    columns: list[str] | None = Field(default=None, description="Optional source columns to include in raw_data.")


class PullRequest(BaseModel):
    request_id: str | None = Field(default=None, description="Caller generated request id.")
    limit_per_table: int | None = Field(default=500, ge=1, le=5000)
    tables: list[PullTableRequest] | None = Field(default=None, description="Optional scoped source tables.")


def now_iso() -> str:
    return datetime.now(timezone.utc).astimezone().isoformat(timespec="seconds")


def quote_ident(identifier: str) -> str:
    return "`" + identifier.replace("`", "``") + "`"


def logical_table_name(physical_table: str) -> str:
    if physical_table.startswith(table_prefix):
        return physical_table[len(table_prefix) :]
    return physical_table


def physical_table_name(logical_table: str) -> str:
    return f"{table_prefix}{logical_table}"


def logical_constraint_name(name: str) -> str:
    for prefix in (f"fk_{table_prefix}", f"idx_{table_prefix}", f"uq_{table_prefix}"):
        if name.startswith(prefix):
            token = prefix.split("_", 1)[0]
            return f"{token}_{name[len(prefix):]}"
    return name


def json_value(value):
    if isinstance(value, Decimal):
        return str(value)
    if isinstance(value, (datetime, date, time)):
        return value.isoformat()
    if isinstance(value, bytes):
        return value.hex()
    return value


def fetch_tables(conn) -> list[dict]:
    rows = conn.execute(
        text(
            """
            select table_name as table_name, table_comment as table_comment
            from information_schema.tables
            where table_schema = :database_name
              and table_type = 'BASE TABLE'
              and table_name like :prefix_like
            order by table_name
            """
        ),
        {"database_name": db_info["database"], "prefix_like": f"{table_prefix}%"},
    ).mappings()
    return [dict(row) for row in rows]


def fetch_columns(conn) -> dict[str, list[dict]]:
    rows = conn.execute(
        text(
            """
            select
                table_name as table_name,
                column_name as column_name,
                ordinal_position as ordinal_position,
                column_type as column_type,
                data_type as data_type,
                is_nullable as is_nullable,
                column_comment as column_comment
            from information_schema.columns
            where table_schema = :database_name
              and table_name like :prefix_like
            order by table_name, ordinal_position
            """
        ),
        {"database_name": db_info["database"], "prefix_like": f"{table_prefix}%"},
    ).mappings()

    columns_by_table: dict[str, list[dict]] = defaultdict(list)
    for row in rows:
        columns_by_table[row["table_name"]].append(dict(row))
    return columns_by_table


def fetch_primary_keys(conn) -> dict[str, list[str]]:
    rows = conn.execute(
        text(
            """
            select kcu.table_name as table_name, kcu.column_name as column_name
            from information_schema.table_constraints tc
            join information_schema.key_column_usage kcu
              on tc.constraint_name = kcu.constraint_name
             and tc.table_schema = kcu.table_schema
             and tc.table_name = kcu.table_name
            where tc.table_schema = :database_name
              and tc.constraint_type = 'PRIMARY KEY'
              and tc.table_name like :prefix_like
            order by kcu.table_name, kcu.ordinal_position
            """
        ),
        {"database_name": db_info["database"], "prefix_like": f"{table_prefix}%"},
    ).mappings()

    pks_by_table: dict[str, list[str]] = defaultdict(list)
    for row in rows:
        pks_by_table[row["table_name"]].append(row["column_name"])
    return pks_by_table


def fetch_foreign_keys(conn) -> dict[str, list[dict]]:
    rows = conn.execute(
        text(
            """
            select
                constraint_name as constraint_name,
                table_name as table_name,
                column_name as column_name,
                referenced_table_name as referenced_table_name,
                referenced_column_name as referenced_column_name
            from information_schema.key_column_usage
            where table_schema = :database_name
              and table_name like :prefix_like
              and referenced_table_name is not null
            order by table_name, constraint_name, ordinal_position
            """
        ),
        {"database_name": db_info["database"], "prefix_like": f"{table_prefix}%"},
    ).mappings()

    fks_by_table: dict[str, list[dict]] = defaultdict(list)
    for row in rows:
        fks_by_table[row["table_name"]].append(
            {
                "constraint_name": logical_constraint_name(row["constraint_name"]),
                "column_name": row["column_name"],
                "referenced_table": logical_table_name(row["referenced_table_name"]),
                "referenced_column": row["referenced_column_name"],
            }
        )
    return fks_by_table


def fetch_sample_values(conn, physical_table: str, column_name: str, limit: int = 5) -> list:
    try:
        rows = conn.execute(
            text(
                f"""
                select distinct {quote_ident(column_name)} as sample_value
                from {quote_ident(physical_table)}
                where {quote_ident(column_name)} is not null
                limit :limit
                """
            ),
            {"limit": limit},
        ).mappings()
        return [json_value(row["sample_value"]) for row in rows]
    except SQLAlchemyError:
        return []


def count_rows(conn, physical_table: str) -> int:
    row = conn.execute(text(f"select count(*) as total from {quote_ident(physical_table)}")).mappings().one()
    return int(row["total"])


def normalize_table_names(tables: list[str] | None) -> list[str]:
    if not tables:
        return []

    names: list[str] = []
    for item in tables:
        for name in item.split(","):
            stripped = name.strip()
            if stripped:
                names.append(stripped)
    return list(dict.fromkeys(names))


def require_known_table(table_name: str, known_tables: set[str]) -> None:
    if table_name not in known_tables:
        raise HTTPException(status_code=400, detail=f"Unknown source_table: {table_name}")


def require_known_columns(table_name: str, requested_columns: list[str], available_columns: list[str]) -> None:
    unknown = [column for column in requested_columns if column not in available_columns]
    if unknown:
        joined = ", ".join(unknown)
        raise HTTPException(status_code=400, detail=f"Unknown column(s) for {table_name}: {joined}")


def build_source_pk(row: dict, pk_columns: list[str], fallback_index: int) -> str:
    if not pk_columns:
        return f"row-{fallback_index}"

    values = [json_value(row.get(column)) for column in pk_columns]
    if len(values) == 1:
        return "" if values[0] is None else str(values[0])
    return "|".join(f"{column}={'' if value is None else value}" for column, value in zip(pk_columns, values))


def build_schema_payload() -> dict:
    generated_at = now_iso()
    with engine.connect() as conn:
        tables = fetch_tables(conn)
        columns_by_table = fetch_columns(conn)
        pks_by_table = fetch_primary_keys(conn)
        fks_by_table = fetch_foreign_keys(conn)

        entities: list[dict] = []
        for table in tables:
            physical_table = table["table_name"]
            logical_table = logical_table_name(physical_table)
            pk_columns = set(pks_by_table.get(physical_table, []))
            fields: list[dict] = []
            for column in columns_by_table.get(physical_table, []):
                column_name = column["column_name"]
                fields.append(
                    {
                        "field_name": column_name,
                        "data_type": column["column_type"],
                        "ordinal_position": int(column["ordinal_position"]),
                        "is_primary_key": column_name in pk_columns,
                        "is_nullable": column["is_nullable"] == "YES",
                        "sample_values": fetch_sample_values(conn, physical_table, column_name),
                        "source_column": column_name,
                    }
                )

            entities.append(
                {
                    "entity_name": logical_table,
                    "source_table": logical_table,
                    "row_count": count_rows(conn, physical_table),
                    "fields": fields,
                    "foreign_keys": fks_by_table.get(physical_table, []),
                }
            )

    return {
        "schema_version": "1.0",
        "schema_key": config.schema_key,
        "source_label": config.source_label,
        "generated_at": generated_at,
        "database": {
            "database_name": db_info["database"],
            "schema_name": db_info["database"],
            "storage": "mysql",
        },
        "entities": entities,
        "metadata": {
            "adapter": "projectp-api-pull",
            "adapter_version": APP_VERSION,
        },
    }


def fetch_metadata_for_pull(conn) -> tuple[dict[str, str], dict[str, list[str]], dict[str, list[str]]]:
    tables = fetch_tables(conn)
    columns_by_physical = fetch_columns(conn)
    pks_by_physical = fetch_primary_keys(conn)

    physical_by_logical: dict[str, str] = {}
    columns_by_logical: dict[str, list[str]] = {}
    pks_by_logical: dict[str, list[str]] = {}
    for table in tables:
        physical_table = table["table_name"]
        logical_table = logical_table_name(physical_table)
        physical_by_logical[logical_table] = physical_table
        columns_by_logical[logical_table] = [column["column_name"] for column in columns_by_physical.get(physical_table, [])]
        pks_by_logical[logical_table] = pks_by_physical.get(physical_table, [])
    return physical_by_logical, columns_by_logical, pks_by_logical


def build_pull_payload(request_id: str | None, limit_per_table: int, requested_tables: list[PullTableRequest] | None) -> dict:
    pulled_at = now_iso()
    request_id = request_id or f"{config.schema_key}-pull-{uuid4().hex[:12]}"
    limit = max(1, min(int(limit_per_table or 500), 5000))

    with engine.connect() as conn:
        physical_by_logical, columns_by_logical, pks_by_logical = fetch_metadata_for_pull(conn)
        known_tables = set(physical_by_logical)

        if requested_tables:
            table_requests = requested_tables
        else:
            table_requests = [PullTableRequest(source_table=table_name) for table_name in sorted(known_tables)]

        tables_payload: list[dict] = []
        total_records = 0
        for table_request in table_requests:
            logical_table = table_request.source_table
            require_known_table(logical_table, known_tables)
            available_columns = columns_by_logical[logical_table]
            selected_columns = table_request.columns or available_columns
            require_known_columns(logical_table, selected_columns, available_columns)

            column_sql = ", ".join(quote_ident(column) for column in selected_columns)
            physical_table = physical_by_logical[logical_table]
            rows = conn.execute(
                text(f"select {column_sql} from {quote_ident(physical_table)} limit :limit"),
                {"limit": limit},
            ).mappings()

            records: list[dict] = []
            for index, row in enumerate(rows, start=1):
                raw_data = {column: json_value(row[column]) for column in selected_columns}
                records.append(
                    {
                        "source_pk": build_source_pk(row, pks_by_logical.get(logical_table, []), index),
                        "source_operation": "upsert",
                        "raw_data": raw_data,
                    }
                )

            total_records += len(records)
            tables_payload.append(
                {
                    "source_table": logical_table,
                    "returned_count": len(records),
                    "records": records,
                }
            )

    return {
        "pull_version": "1.0",
        "external_batch_id": f"{config.schema_key}-pull-{pulled_at.replace(':', '').replace('-', '')}-{uuid4().hex[:8]}",
        "request_id": request_id,
        "schema_key": config.schema_key,
        "source_label": config.source_label,
        "pulled_at": pulled_at,
        "database": {
            "database_name": db_info["database"],
            "schema_name": db_info["database"],
            "storage": "mysql",
        },
        "limit_per_table": limit,
        "tables": tables_payload,
        "summary": {
            "table_count": len(tables_payload),
            "record_count": total_records,
        },
    }


@app.get("/api/health")
def health() -> dict:
    checked_at = now_iso()
    try:
        with engine.connect() as conn:
            conn.execute(text("select 1"))
            table_count = len(fetch_tables(conn))
        return {
            "status": "ok",
            "adapter": "projectp-api-pull",
            "adapter_version": APP_VERSION,
            "source_label": config.source_label,
            "schema_key": config.schema_key,
            "database_name": db_info["database"],
            "storage": "mysql",
            "table_count": table_count,
            "checked_at": checked_at,
        }
    except SQLAlchemyError as exc:
        return {
            "status": "error",
            "adapter": "projectp-api-pull",
            "adapter_version": APP_VERSION,
            "source_label": config.source_label,
            "schema_key": config.schema_key,
            "database_name": db_info["database"],
            "storage": "mysql",
            "table_count": 0,
            "checked_at": checked_at,
            "error": str(exc),
        }


@app.get("/api/schema")
def schema() -> dict:
    try:
        return build_schema_payload()
    except SQLAlchemyError as exc:
        raise HTTPException(status_code=503, detail=f"Failed to read MySQL schema: {exc}") from exc


@app.get("/api/pull")
def pull_get(
    limit: int = Query(default=500, ge=1, le=5000),
    tables: list[str] | None = Query(default=None),
) -> dict:
    normalized_tables = normalize_table_names(tables)
    requested_tables = [PullTableRequest(source_table=table_name) for table_name in normalized_tables] if normalized_tables else None
    try:
        return build_pull_payload(None, limit, requested_tables)
    except SQLAlchemyError as exc:
        raise HTTPException(status_code=503, detail=f"Failed to pull MySQL records: {exc}") from exc


@app.post("/api/pull")
def pull_post(payload: PullRequest = Body(default_factory=PullRequest)) -> dict:
    try:
        return build_pull_payload(payload.request_id, payload.limit_per_table or 500, payload.tables)
    except SQLAlchemyError as exc:
        raise HTTPException(status_code=503, detail=f"Failed to pull MySQL records: {exc}") from exc
