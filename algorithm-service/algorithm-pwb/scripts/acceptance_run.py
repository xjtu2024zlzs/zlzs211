from __future__ import annotations

import sys
import time
from datetime import datetime
from pathlib import Path
from typing import Any

from fastapi.testclient import TestClient


ROOT = Path(__file__).resolve().parents[1]
BACKEND_ROOT = ROOT / "backend"
if str(BACKEND_ROOT) not in sys.path:
    sys.path.insert(0, str(BACKEND_ROOT))
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from main import app  # noqa: E402


def expect(status: bool, message: str) -> None:
    if not status:
        raise RuntimeError(message)


def get_json(response):
    try:
        return response.json()
    except Exception as exc:  # pragma: no cover
        raise RuntimeError(f"Non-JSON response: {response.status_code} {response.text}") from exc


def assert_ok(response, *, allowed_statuses: set[int] | None = None):
    allowed = allowed_statuses or {200}
    if response.status_code not in allowed:
        raise RuntimeError(f"Unexpected status {response.status_code}: {response.text}")
    return get_json(response)


def find_by_name(items: list[dict[str, Any]], name: str) -> dict[str, Any] | None:
    for item in items:
        if item.get("name") == name:
            return item
    return None


def ensure_datasource(client: TestClient, payload: dict[str, Any]) -> dict[str, Any]:
    current = assert_ok(client.get("/api/datasources"))
    existing = find_by_name(current, payload["name"])
    if existing:
        assert_ok(client.put(f"/api/datasources/{existing['id']}", json=payload))
        return assert_ok(client.get(f"/api/datasources/{existing['id']}"))
    created = assert_ok(client.post("/api/datasources", json=payload))
    return assert_ok(client.get(f"/api/datasources/{created['id']}"))


def ensure_task(
    client: TestClient,
    *,
    name: str,
    datasource_id: int,
    llm_fields: dict[str, Any] | None = None,
) -> dict[str, Any]:
    payload = {
        "name": name,
        "source_datasource_id": datasource_id,
        "encoding_mode_key": "header_values_default",
        "embedding_model_key": "mpnet",
        "eval_mode_key": "global_unknown",
        "auto_approve_threshold": 0.9,
        "notes": "acceptance automation",
    }
    if llm_fields:
        payload.update(llm_fields)
    tasks = assert_ok(client.get("/api/match-tasks"))
    existing = find_by_name(tasks, name)
    if existing:
        assert_ok(client.put(f"/api/match-tasks/{existing['id']}", json=payload))
        return assert_ok(client.get(f"/api/match-tasks/{existing['id']}"))
    created = assert_ok(client.post("/api/match-tasks", json=payload))
    return created


def terminal_status(status: str | None) -> bool:
    return status in {"success", "blocked", "failed", "canceled"}


def wait_for_record(client: TestClient, task_record_id: int, *, timeout_s: int = 240) -> dict[str, Any]:
    deadline = time.time() + timeout_s
    last_payload: dict[str, Any] | None = None
    while time.time() < deadline:
        payload = assert_ok(client.get(f"/api/match-task-records/{task_record_id}"))
        last_payload = payload
        if terminal_status(payload.get("execution_status") or payload.get("status")):
            return payload
        time.sleep(2)
    raise RuntimeError(f"Timed out waiting for task_record {task_record_id}; last payload={last_payload}")


def first_enabled_id(items: list[dict[str, Any]], preferred_name: str | None = None) -> int | None:
    if preferred_name:
        for item in items:
            if item.get("name") == preferred_name and item.get("enabled", True):
                return int(item["id"])
    for item in items:
        if item.get("enabled", True):
            return int(item["id"])
    return None


def run_chain(
    client: TestClient,
    *,
    scenario_name: str,
    datasource_payload: dict[str, Any],
    llm_fields: dict[str, Any] | None = None,
) -> dict[str, Any]:
    datasource = ensure_datasource(client, datasource_payload)
    datasource_id = int(datasource["id"])

    test_result = assert_ok(client.post(f"/api/datasources/{datasource_id}/test"))
    expect(bool(test_result.get("success")), f"{scenario_name}: datasource test failed: {test_result}")

    ingest_result = assert_ok(client.post(f"/api/datasources/{datasource_id}/ingest"))
    batch_id = ingest_result.get("batch_id")
    expect(batch_id is not None, f"{scenario_name}: missing batch_id from ingest result {ingest_result}")

    batches = assert_ok(client.get(f"/api/staging/batches?datasource_id={datasource_id}"))
    expect(any(int(row["id"]) == int(batch_id) for row in batches), f"{scenario_name}: staging batch {batch_id} not found")

    task = ensure_task(
        client,
        name=f"{scenario_name}_task",
        datasource_id=datasource_id,
        llm_fields=llm_fields,
    )
    task_id = int(task["id"])
    current_llm = (task.get("current_version") or {}).get("llm_config") or {}
    if llm_fields:
        expect(bool(current_llm.get("id")), f"{scenario_name}: expected llm_config to be resolved")
    else:
        expect(current_llm.get("name") == "none", f"{scenario_name}: expected none llm config, got {current_llm}")

    run_result = assert_ok(client.post(f"/api/match-tasks/{task_id}/run"))
    task_record_id = int(run_result["task_record_id"])
    record = wait_for_record(client, task_record_id)
    status = record.get("execution_status") or record.get("status")
    expect(status != "failed", f"{scenario_name}: task execution failed: {record}")

    result_sets_payload = assert_ok(client.get(f"/api/result-sets?task_record_id={task_record_id}"))
    result_sets = result_sets_payload.get("items", result_sets_payload) if isinstance(result_sets_payload, dict) else result_sets_payload
    expect(bool(result_sets), f"{scenario_name}: no result sets produced")
    first_set = result_sets[0]
    result_rows_payload = assert_ok(client.get(f"/api/result-sets/{first_set['id']}/rows"))
    result_rows = result_rows_payload.get("items", result_rows_payload) if isinstance(result_rows_payload, dict) else result_rows_payload
    expect(bool(result_rows), f"{scenario_name}: result set {first_set['id']} has no rows")

    matches_payload = assert_ok(client.get(f"/api/matches?task_id={task_id}&task_record_id={task_record_id}"))
    matches = matches_payload.get("items", [])
    expect(bool(matches), f"{scenario_name}: reviewed_matches not synced")

    assert_ok(client.post(f"/api/mapping-specs/generate?task_id={task_id}&task_record_id={task_record_id}"))
    specs_payload = assert_ok(client.get(f"/api/mapping-specs?task_id={task_id}&task_record_id={task_record_id}"))
    specs = specs_payload.get("items", specs_payload) if isinstance(specs_payload, dict) else specs_payload
    expect(bool(specs), f"{scenario_name}: no mapping specs generated")
    target_table = specs[0]["target_table"]

    preview = assert_ok(client.get(f"/api/migration/preview/{target_table}?task_id={task_id}&task_record_id={task_record_id}&limit=3"))
    expect("preview" in preview, f"{scenario_name}: preview response malformed: {preview}")

    execute_body = {"task_id": task_id, "task_record_id": task_record_id, "target_tables": [target_table]}
    execute_result = assert_ok(client.post("/api/migration/execute", json=execute_body))
    migration_results = execute_result.get("results", [])
    expect(bool(migration_results), f"{scenario_name}: no migration results returned")
    bad_results = [row for row in migration_results if row.get("status") == "failed"]
    expect(not bad_results, f"{scenario_name}: migration returned failed result {bad_results}")

    jobs = assert_ok(client.get(f"/api/migration/jobs?task_id={task_id}&task_record_id={task_record_id}"))
    expect(bool(jobs), f"{scenario_name}: no migration jobs written")
    bad_jobs = [row for row in jobs if row.get("status") == "failed"]
    expect(not bad_jobs, f"{scenario_name}: migration job failed {bad_jobs}")

    return {
        "scenario": scenario_name,
        "datasource_id": datasource_id,
        "task_id": task_id,
        "task_record_id": task_record_id,
        "status": status,
        "batch_id": batch_id,
        "result_set_id": first_set["id"],
        "migration_statuses": [row.get("status") for row in migration_results],
        "llm_config_id": current_llm.get("id"),
    }


def run_cancel_test(
    client: TestClient,
    *,
    datasource_id: int,
) -> dict[str, Any]:
    task = ensure_task(
        client,
        name=f"cancel_test_{datetime.now().strftime('%Y%m%d_%H%M%S')}",
        datasource_id=datasource_id,
    )
    task_id = int(task["id"])
    run_result = assert_ok(client.post(f"/api/match-tasks/{task_id}/run"))
    task_record_id = int(run_result["task_record_id"])
    cancel = assert_ok(client.post(f"/api/match-task-records/{task_record_id}/cancel"))
    expect(bool(cancel.get("updated")), f"cancel test: cancel endpoint did not update record: {cancel}")
    record = wait_for_record(client, task_record_id, timeout_s=60)
    status = record.get("execution_status") or record.get("status")
    expect(status == "canceled", f"cancel test: expected canceled status, got {status}")
    return {"task_id": task_id, "task_record_id": task_record_id, "status": status}


def run_llm_reuse_test(client: TestClient, *, datasource_id: int) -> dict[str, Any]:
    llm_fields = {
        "llm_provider": "openai",
        "llm_model_name": "gpt-4.1-mini",
        "llm_base_url": "https://api.openai.com/v1",
        "llm_api_key": "sk-acceptance-demo-key",
    }
    first = ensure_task(
        client,
        name=f"llm_reuse_a_{datetime.now().strftime('%Y%m%d_%H%M%S')}",
        datasource_id=datasource_id,
        llm_fields=llm_fields,
    )
    second = ensure_task(
        client,
        name=f"llm_reuse_b_{datetime.now().strftime('%Y%m%d_%H%M%S')}",
        datasource_id=datasource_id,
        llm_fields=llm_fields,
    )
    first_id = ((first.get("current_version") or {}).get("llm_config") or {}).get("id")
    second_id = ((second.get("current_version") or {}).get("llm_config") or {}).get("id")
    expect(bool(first_id) and bool(second_id), "llm reuse test: expected llm_config ids")
    expect(int(first_id) == int(second_id), f"llm reuse test: expected reuse, got {first_id} and {second_id}")

    updated = assert_ok(
        client.put(
            f"/api/match-tasks/{second['id']}",
            json={
                "llm_provider": "openai",
                "llm_model_name": "gpt-4.1-mini",
                "llm_base_url": "https://api.openai.com/v1",
                "change_summary": "acceptance llm reuse without key",
            },
        )
    )
    updated_id = (((updated.get("current_version") or {}).get("llm_config")) or {}).get("id")
    expect(int(updated_id) == int(second_id), f"llm reuse test: update should reuse existing config, got {updated_id}")
    return {"first_task_id": first["id"], "second_task_id": second["id"], "llm_config_id": int(first_id)}


def main() -> int:
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    summaries: list[dict[str, Any]] = []

    with TestClient(app) as client:
        # Config smoke checks
        encoding_options = assert_ok(client.get("/api/encoding-modes"))
        embedding_options = assert_ok(client.get("/api/embedding-models"))
        eval_options = assert_ok(client.get("/api/eval-modes"))
        expect(bool(encoding_options), "No encoding_mode options available")
        expect(bool(embedding_options), "No embedding_model options available")
        expect(bool(eval_options), "No eval_mode options available")

        scenarios = [
            (
                "PLM_api_push",
                {
                    "name": f"PLM_api_push_acceptance_{timestamp}",
                    "integration_mode": "api_push",
                    "auth_type": "none",
                    "auth_config": {},
                    "connection_config": {"adapter": "plm", "row_limit": 80},
                    "enabled": True,
                    "notes": "acceptance automation",
                },
            ),
            (
                "ERP_api_pull",
                {
                    "name": f"ERP_api_pull_acceptance_{timestamp}",
                    "integration_mode": "api_pull",
                    "auth_type": "none",
                    "auth_config": {},
                    "connection_config": {"adapter": "erp", "row_limit": 80},
                    "enabled": True,
                    "notes": "acceptance automation",
                },
            ),
            (
                "MES_db_direct",
                {
                    "name": f"MES_db_direct_acceptance_{timestamp}",
                    "integration_mode": "db_direct",
                    "host": "localhost",
                    "port": 5463,
                    "database_name": "aviation_mes",
                    "username": "aviation",
                    "password": "aviation123",
                    "save_password": True,
                    "db_type": "postgresql",
                    "auth_type": "none",
                    "auth_config": {},
                    "connection_config": {"row_limit": 80},
                    "enabled": True,
                    "notes": "acceptance automation",
                },
            ),
            (
                "MRO_db_direct",
                {
                    "name": f"MRO_db_direct_acceptance_{timestamp}",
                    "integration_mode": "db_direct",
                    "host": "localhost",
                    "port": 5464,
                    "database_name": "aviation_mro",
                    "username": "aviation",
                    "password": "aviation123",
                    "save_password": True,
                    "db_type": "postgresql",
                    "auth_type": "none",
                    "auth_config": {},
                    "connection_config": {"row_limit": 80},
                    "enabled": True,
                    "notes": "acceptance automation",
                },
            ),
        ]

        for scenario_name, datasource_payload in scenarios:
            print(f"\n=== Running acceptance chain: {scenario_name} ===")
            summary = run_chain(
                client,
                scenario_name=scenario_name,
                datasource_payload=datasource_payload,
            )
            print(summary)
            summaries.append(summary)

        llm_reuse_summary = run_llm_reuse_test(client, datasource_id=summaries[-1]["datasource_id"])
        print("\n=== LLM reuse acceptance ===")
        print(llm_reuse_summary)

        cancel_summary = run_cancel_test(
            client,
            datasource_id=summaries[-1]["datasource_id"],
        )
        print("\n=== Cancel acceptance ===")
        print(cancel_summary)

    print("\nAll automated backend acceptance chains passed:")
    for item in summaries:
        print(
            f"- {item['scenario']}: task_id={item['task_id']}, task_record_id={item['task_record_id']}, "
            f"batch_id={item['batch_id']}, status={item['status']}, migrations={item['migration_statuses']}"
        )
    print(
        f"- llm_reuse_test: first_task_id={llm_reuse_summary['first_task_id']}, "
        f"second_task_id={llm_reuse_summary['second_task_id']}, llm_config_id={llm_reuse_summary['llm_config_id']}"
    )
    print(f"- cancel_test: task_id={cancel_summary['task_id']}, task_record_id={cancel_summary['task_record_id']}, status={cancel_summary['status']}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
