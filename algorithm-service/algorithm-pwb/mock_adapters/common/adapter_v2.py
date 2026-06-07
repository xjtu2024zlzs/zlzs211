"""Mock HTTP adapter for local external API simulation."""

from __future__ import annotations

import json
import os
import uuid
from datetime import date, datetime
from typing import Any

import urllib.error
import urllib.request

from fastapi import Body, FastAPI, HTTPException, Query
from sqlalchemy import create_engine, text


DEFAULT_PORTS = {"plm": 9001, "erp": 9002, "mes": 9003, "mro": 9004}
DEFAULT_DB_NAMES = {
    "plm": "aviation_plm",
    "erp": "aviation_erp",
    "mes": "aviation_mes",
    "mro": "aviation_mro",
}
DEFAULT_DB_PORTS = {"plm": 5461, "erp": 5462, "mes": 5463, "mro": 5464}


def _dsn_from_env(system_key: str) -> str:
    host = os.getenv("DB_HOST", "localhost")
    port = int(os.getenv("DB_PORT", str(DEFAULT_DB_PORTS[system_key])))
    database = os.getenv("DB_NAME", DEFAULT_DB_NAMES[system_key])
    user = os.getenv("DB_USER", "aviation")
    password = os.getenv("DB_PASSWORD", "aviation123")
    return f"postgresql://{user}:{password}@{host}:{port}/{database}"


def make_adapter_app(system_key: str) -> FastAPI:
    system_key = system_key.lower()
    if system_key not in DEFAULT_DB_NAMES:
        raise ValueError(f"Unknown system_key: {system_key}")

    engine = create_engine(_dsn_from_env(system_key), pool_pre_ping=True, connect_args={"connect_timeout": 5})
    app = FastAPI(title=f"{system_key.upper()} adapter", version="2.0.0")

    def _build_unified_payload(external_batch_id: str | None = None) -> dict[str, Any]:
        with engine.connect() as conn:
            table_names = conn.execute(
                text(
                    """
                    SELECT table_name
                    FROM information_schema.tables
                    WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
                    ORDER BY table_name
                    """
                )
            ).scalars().all()
            entities: list[dict[str, Any]] = []
            for table in table_names:
                columns = conn.execute(
                    text(
                        """
                        SELECT column_name, data_type, is_nullable, ordinal_position
                        FROM information_schema.columns
                        WHERE table_schema = 'public' AND table_name = :name
                        ORDER BY ordinal_position
                        """
                    ),
                    {"name": table},
                ).mappings().all()
                pks = conn.execute(
                    text(
                        """
                        SELECT kcu.column_name
                        FROM information_schema.table_constraints tc
                        JOIN information_schema.key_column_usage kcu
                          ON tc.constraint_name = kcu.constraint_name
                        WHERE tc.table_schema = 'public'
                          AND tc.table_name = :name
                          AND tc.constraint_type = 'PRIMARY KEY'
                        """
                    ),
                    {"name": table},
                ).scalars().all()
                pk_set = {str(c) for c in pks}
                rows = conn.execute(text(f'SELECT * FROM "{table}"')).mappings().all()
                records = []
                for row in rows:
                    record: dict[str, Any] = {}
                    for key, value in dict(row).items():
                        record[key] = value.isoformat() if isinstance(value, (datetime, date)) else value
                    records.append(record)
                fields = [
                    {
                        "field_name": column["column_name"],
                        "data_type": column["data_type"],
                        "ordinal_position": column["ordinal_position"],
                        "is_nullable": column["is_nullable"] == "YES",
                        "is_primary_key": column["column_name"] in pk_set,
                        "source_column": column["column_name"],
                    }
                    for column in columns
                ]
                entities.append(
                    {
                        "entity_name": str(table),
                        "source_table": str(table),
                        "fields": fields,
                        "records": records,
                    }
                )

        batch_id = external_batch_id or str(uuid.uuid4())
        return {
            "schema_version": "1.0",
            "external_batch_id": batch_id,
            "batch_name": f"{system_key}-payload-{batch_id[:8]}",
            "source_label": system_key.upper(),
            "schema_key": f"system_{system_key}",
            "metadata": {"mode": "api", "adapter": system_key},
            "entities": entities,
        }

    @app.get("/api/health")
    def health() -> dict[str, Any]:
        try:
            with engine.connect() as conn:
                conn.execute(text("SELECT 1"))
            return {"status": "ok", "system": system_key}
        except Exception as exc:  # pragma: no cover
            return {"status": "degraded", "system": system_key, "error": str(exc)}

    @app.get("/api/pull")
    def pull_payload(external_batch_id: str | None = Query(default=None)) -> dict[str, Any]:
        return _build_unified_payload(external_batch_id)

    @app.post("/api/trigger-push")
    def trigger_push(body: dict[str, Any] = Body(default_factory=dict)) -> dict[str, Any]:
        base_url = body.get("integration_base_url") or os.getenv("INTEGRATION_BASE_URL", "http://localhost:8000")
        datasource_id = body.get("datasource_id") or os.getenv("PUSH_DATASOURCE_ID")
        push_secret = body.get("push_secret") or os.getenv("PUSH_SECRET")
        if not datasource_id:
            raise HTTPException(status_code=400, detail="datasource_id is required")
        if not push_secret:
            raise HTTPException(status_code=400, detail="push_secret is required")

        unified = _build_unified_payload(body.get("external_batch_id"))
        request_body = {
            "payload": unified,
            "task_id": body.get("task_id"),
            "task_version_id": body.get("task_version_id"),
            "external_batch_id": unified["external_batch_id"],
            "notes": body.get("notes"),
        }
        url = f"{base_url.rstrip('/')}/api/staging/push/{datasource_id}"
        request = urllib.request.Request(
            url,
            data=json.dumps(request_body).encode("utf-8"),
            headers={
                "Content-Type": "application/json",
                "X-Push-Secret": push_secret,
            },
            method="POST",
        )
        try:
            with urllib.request.urlopen(request, timeout=60) as response:
                body_text = response.read().decode("utf-8", errors="replace")
                return {
                    "pushed": True,
                    "integration_status": response.status,
                    "integration_response": json.loads(body_text) if body_text else None,
                    "external_batch_id": unified["external_batch_id"],
                    "entity_count": len(unified["entities"]),
                }
        except urllib.error.HTTPError as exc:
            body_text = exc.read().decode("utf-8", errors="replace")
            return {
                "pushed": False,
                "integration_status": exc.code,
                "integration_response": body_text,
                "external_batch_id": unified["external_batch_id"],
            }
        except Exception as exc:  # pragma: no cover
            raise HTTPException(status_code=502, detail=f"Push failed: {exc}") from exc

    @app.get("/api/entities")
    def list_entities() -> dict[str, Any]:
        with engine.connect() as conn:
            rows = conn.execute(
                text(
                    """
                    SELECT table_name
                    FROM information_schema.tables
                    WHERE table_schema = 'public'
                      AND table_type = 'BASE TABLE'
                    ORDER BY table_name
                    """
                )
            ).scalars().all()
        return {"system": system_key, "items": [str(row) for row in rows]}

    @app.get("/api/entities/{name}")
    def describe_entity(name: str) -> dict[str, Any]:
        with engine.connect() as conn:
            columns = conn.execute(
                text(
                    """
                    SELECT column_name, data_type, is_nullable, ordinal_position
                    FROM information_schema.columns
                    WHERE table_schema = 'public' AND table_name = :name
                    ORDER BY ordinal_position
                    """
                ),
                {"name": name},
            ).mappings().all()
            if not columns:
                raise HTTPException(status_code=404, detail=f"Entity {name} not found")
        return {"system": system_key, "entity_name": name, "columns": [dict(row) for row in columns]}

    @app.get("/api/data/{name}")
    def list_data(name: str, page: int = Query(default=1, ge=1), size: int = Query(default=200, ge=1, le=2000)) -> dict[str, Any]:
        offset = (page - 1) * size
        with engine.connect() as conn:
            total = conn.execute(text(f'SELECT COUNT(*) FROM "{name}"')).scalar() or 0
            rows = conn.execute(
                text(f'SELECT * FROM "{name}" LIMIT :limit OFFSET :offset'),
                {"limit": size, "offset": offset},
            ).mappings().all()
        items = []
        for row in rows:
            record = {}
            for key, value in dict(row).items():
                record[key] = value.isoformat() if isinstance(value, (datetime, date)) else value
            items.append(record)
        return {"system": system_key, "entity_name": name, "items": items, "total": int(total), "page": page, "size": size}

    return app
