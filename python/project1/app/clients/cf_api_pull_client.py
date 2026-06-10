from __future__ import annotations

from typing import Any

import pandas as pd
import requests

from ..domain import DataBundle, TableSchema


class CfApiPullClient:
    def __init__(
        self,
        base_url: str,
        schema_endpoint: str = "/api/schema",
        pull_endpoint: str = "/api/pull",
        health_endpoint: str = "/api/health",
        timeout_seconds: int = 30,
    ) -> None:
        self.base_url = base_url.rstrip("/")
        self.schema_endpoint = schema_endpoint
        self.pull_endpoint = pull_endpoint
        self.health_endpoint = health_endpoint
        self.timeout_seconds = timeout_seconds

    def _url(self, endpoint: str) -> str:
        endpoint = endpoint if endpoint.startswith("/") else f"/{endpoint}"
        return f"{self.base_url}{endpoint}"

    def health(self) -> dict[str, Any]:
        response = requests.get(self._url(self.health_endpoint), timeout=self.timeout_seconds)
        response.raise_for_status()
        return response.json()

    def fetch_schema(self) -> dict[str, Any]:
        response = requests.get(self._url(self.schema_endpoint), timeout=self.timeout_seconds)
        response.raise_for_status()
        return response.json()

    def pull_data(
        self,
        *,
        request_id: str | None,
        tables: list[str],
        limit_per_table: int,
    ) -> dict[str, Any]:
        payload: dict[str, Any] = {
            "request_id": request_id,
            "limit_per_table": limit_per_table,
        }
        if tables:
            payload["tables"] = [{"source_table": table, "columns": None} for table in tables]

        response = requests.post(
            self._url(self.pull_endpoint),
            json=payload,
            timeout=max(self.timeout_seconds, 120),
        )
        response.raise_for_status()
        return response.json()

    def load_bundle(
        self,
        *,
        system_key: str,
        request_id: str | None,
        tables: list[str],
        limit_per_table: int,
    ) -> DataBundle:
        schema_payload = self.fetch_schema()
        pull_payload = self.pull_data(
            request_id=request_id,
            tables=tables,
            limit_per_table=limit_per_table,
        )

        schemas: dict[str, TableSchema] = {}
        for entity in schema_payload.get("entities", []):
            table_name = entity.get("source_table") or entity.get("entity_name")
            if not table_name:
                continue
            schemas[table_name] = TableSchema(
                table_name=table_name,
                physical_table_name=table_name,
                source_database=schema_payload.get("source_label"),
                row_count=int(entity.get("row_count") or 0),
                columns=list(entity.get("fields") or []),
                foreign_keys=list(entity.get("foreign_keys") or []),
            )

        dataframes: dict[str, pd.DataFrame] = {}
        for table_payload in pull_payload.get("tables", []):
            table_name = table_payload.get("source_table")
            if not table_name:
                continue
            records = table_payload.get("records") or []
            rows = [record.get("raw_data") or {} for record in records]
            dataframes[table_name] = pd.DataFrame(rows)

        return DataBundle(
            system_key=system_key,
            source_label=schema_payload.get("source_label") or system_key.upper(),
            schema_key=schema_payload.get("schema_key") or system_key,
            schemas=schemas,
            dataframes=dataframes,
            raw_schema_payload=schema_payload,
            raw_pull_payload=pull_payload,
        )

