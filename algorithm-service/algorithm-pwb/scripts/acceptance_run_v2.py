from __future__ import annotations

import json
import os
import socket
import subprocess
import sys
import time
from contextlib import ExitStack
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Any
from urllib import error as urlerror
from urllib import request as urlrequest


ROOT = Path(__file__).resolve().parents[1]
BACKEND_ROOT = ROOT / "backend"
BACKEND_BASE_URL = "http://127.0.0.1:18000"
PLM_ADAPTER_BASE_URL = "http://127.0.0.1:19001"
ERP_ADAPTER_BASE_URL = "http://127.0.0.1:19002"
DEFAULT_APP_PYTHON = Path(r"C:\Users\29159\.conda\envs\py310-magneto\python.exe")
APP_PYTHON = str(Path(os.getenv("MAGNETO_PYTHON", str(DEFAULT_APP_PYTHON if DEFAULT_APP_PYTHON.exists() else sys.executable))))


def expect(status: bool, message: str) -> None:
    if not status:
        raise RuntimeError(message)


def _json_request(
    method: str,
    url: str,
    *,
    body: dict[str, Any] | None = None,
    headers: dict[str, str] | None = None,
    allowed_statuses: set[int] | None = None,
) -> Any:
    payload = None
    request_headers = dict(headers or {})
    if body is not None:
        payload = json.dumps(body).encode("utf-8")
        request_headers.setdefault("Content-Type", "application/json")
    elif method.upper() in {"POST", "PUT", "PATCH"}:
        payload = b""
    request = urlrequest.Request(url, data=payload, headers=request_headers, method=method.upper())
    try:
        with urlrequest.urlopen(request, timeout=120) as response:
            allowed = allowed_statuses or {200}
            if response.status not in allowed:
                text_body = response.read().decode("utf-8", errors="replace")
                raise RuntimeError(f"Unexpected status {response.status}: {text_body}")
            raw = response.read().decode("utf-8", errors="replace")
            return json.loads(raw) if raw else {}
    except urlerror.HTTPError as exc:
        text_body = exc.read().decode("utf-8", errors="replace")
        allowed = allowed_statuses or {200}
        if exc.code in allowed:
            return json.loads(text_body) if text_body else {}
        raise RuntimeError(f"Unexpected status {exc.code}: {text_body}") from exc


def get_json(path: str) -> Any:
    return _json_request("GET", f"{BACKEND_BASE_URL}{path}")


def post_json(path: str, body: dict[str, Any] | None = None) -> Any:
    return _json_request("POST", f"{BACKEND_BASE_URL}{path}", body=body)


def put_json(path: str, body: dict[str, Any]) -> Any:
    return _json_request("PUT", f"{BACKEND_BASE_URL}{path}", body=body)


def find_by_name(items: list[dict[str, Any]], name: str) -> dict[str, Any] | None:
    for item in items:
        if item.get("name") == name:
            return item
    return None


def _wait_for_url(url: str, *, timeout_s: int = 45) -> None:
    deadline = time.time() + timeout_s
    last_error = "unavailable"
    while time.time() < deadline:
        try:
            with urlrequest.urlopen(url, timeout=3) as response:
                if 200 <= response.status < 500:
                    return
        except Exception as exc:  # pragma: no cover
            last_error = str(exc)
        time.sleep(1)
    raise RuntimeError(f"Timed out waiting for {url}: {last_error}")


def _wait_port_closed(host: str, port: int, *, timeout_s: int = 10) -> None:
    deadline = time.time() + timeout_s
    while time.time() < deadline:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
            sock.settimeout(1)
            try:
                result = sock.connect_ex((host, port))
            except OSError:
                result = 1
        if result != 0:
            return
        time.sleep(0.5)


@dataclass
class ManagedService:
    name: str
    base_url: str
    health_path: str
    command: list[str]
    cwd: Path
    env: dict[str, str] | None = None
    process: subprocess.Popen[str] | None = None
    log_path: Path | None = None

    def start(self) -> None:
        health_url = f"{self.base_url.rstrip('/')}{self.health_path}"
        try:
            _wait_for_url(health_url, timeout_s=2)
            return
        except Exception:
            pass

        host = "127.0.0.1"
        port = int(self.base_url.rsplit(":", 1)[-1])
        _wait_port_closed(host, port)
        logs_dir = ROOT / ".acceptance_logs"
        logs_dir.mkdir(parents=True, exist_ok=True)
        self.log_path = logs_dir / f"{self.name}.log"
        log_handle = open(self.log_path, "w", encoding="utf-8")
        env = os.environ.copy()
        env["PYTHONPATH"] = os.pathsep.join(
            [
                str(ROOT),
                str(BACKEND_ROOT),
                env.get("PYTHONPATH", ""),
            ]
        ).strip(os.pathsep)
        if self.env:
            env.update(self.env)
        creationflags = getattr(subprocess, "CREATE_NO_WINDOW", 0)
        self.process = subprocess.Popen(
            self.command,
            cwd=str(self.cwd),
            env=env,
            stdout=log_handle,
            stderr=log_handle,
            text=True,
            creationflags=creationflags,
        )
        try:
            _wait_for_url(health_url, timeout_s=45)
        except Exception:
            self.stop()
            raise RuntimeError(f"Failed to start {self.name}. See log: {self.log_path}")

    def stop(self) -> None:
        if not self.process:
            return
        if self.process.poll() is None:
            self.process.terminate()
            try:
                self.process.wait(timeout=8)
            except subprocess.TimeoutExpired:
                self.process.kill()
                self.process.wait(timeout=5)
        self.process = None


def ensure_datasource(payload: dict[str, Any]) -> dict[str, Any]:
    current = get_json("/api/datasources")
    existing = find_by_name(current, payload["name"])
    if existing:
        put_json(f"/api/datasources/{existing['id']}", payload)
        return get_json(f"/api/datasources/{existing['id']}")
    created = post_json("/api/datasources", payload)
    return get_json(f"/api/datasources/{created['id']}")


def ensure_task(
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
    tasks = get_json("/api/match-tasks")
    existing = find_by_name(tasks, name)
    if existing:
        put_json(f"/api/match-tasks/{existing['id']}", payload)
        return get_json(f"/api/match-tasks/{existing['id']}")
    return post_json("/api/match-tasks", payload)


def terminal_status(status: str | None) -> bool:
    return status in {"success", "blocked", "failed", "canceled"}


def wait_for_record(task_record_id: int, *, timeout_s: int = 240) -> dict[str, Any]:
    deadline = time.time() + timeout_s
    last_payload: dict[str, Any] | None = None
    while time.time() < deadline:
        payload = get_json(f"/api/match-task-records/{task_record_id}")
        last_payload = payload
        if terminal_status(payload.get("execution_status") or payload.get("status")):
            return payload
        time.sleep(2)
    raise RuntimeError(f"Timed out waiting for task_record {task_record_id}; last payload={last_payload}")


def run_chain(
    *,
    scenario_name: str,
    datasource_payload: dict[str, Any],
    llm_fields: dict[str, Any] | None = None,
) -> dict[str, Any]:
    datasource = ensure_datasource(datasource_payload)
    datasource_id = int(datasource["id"])

    test_result = post_json(f"/api/datasources/{datasource_id}/test")
    expect(bool(test_result.get("success")), f"{scenario_name}: datasource test failed: {test_result}")

    ingest_result = post_json(f"/api/datasources/{datasource_id}/ingest")
    nested_response = ingest_result.get("integration_response") if isinstance(ingest_result, dict) else None
    nested_batch_id = nested_response.get("batch_id") if isinstance(nested_response, dict) else None
    batch_id = ingest_result.get("batch_id") or nested_batch_id
    expect(batch_id is not None, f"{scenario_name}: missing batch_id from ingest result {ingest_result}")

    batches = get_json(f"/api/staging/batches?datasource_id={datasource_id}")
    expect(any(int(row["id"]) == int(batch_id) for row in batches), f"{scenario_name}: staging batch {batch_id} not found")

    task = ensure_task(
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

    run_result = post_json(f"/api/match-tasks/{task_id}/run")
    task_record_id = int(run_result["task_record_id"])
    record = wait_for_record(task_record_id)
    status = record.get("execution_status") or record.get("status")
    expect(status != "failed", f"{scenario_name}: task execution failed: {record}")

    result_sets_payload = get_json(f"/api/result-sets?task_record_id={task_record_id}")
    result_sets = result_sets_payload.get("items", result_sets_payload) if isinstance(result_sets_payload, dict) else result_sets_payload
    expect(bool(result_sets), f"{scenario_name}: no result sets produced")
    first_set = result_sets[0]
    result_rows_payload = get_json(f"/api/result-sets/{first_set['id']}/rows")
    result_rows = result_rows_payload.get("items", result_rows_payload) if isinstance(result_rows_payload, dict) else result_rows_payload
    expect(bool(result_rows), f"{scenario_name}: result set {first_set['id']} has no rows")

    matches_payload = get_json(f"/api/matches?task_id={task_id}&task_record_id={task_record_id}")
    matches = matches_payload.get("items", [])
    expect(bool(matches), f"{scenario_name}: reviewed_matches not synced")

    post_json(f"/api/mapping-specs/generate?task_id={task_id}&task_record_id={task_record_id}")
    specs_payload = get_json(f"/api/mapping-specs?task_id={task_id}&task_record_id={task_record_id}")
    specs = specs_payload.get("items", specs_payload) if isinstance(specs_payload, dict) else specs_payload
    expect(bool(specs), f"{scenario_name}: no mapping specs generated")
    target_table = specs[0]["target_table"]

    preview = get_json(f"/api/migration/preview/{target_table}?task_id={task_id}&task_record_id={task_record_id}&limit=3")
    expect("preview" in preview, f"{scenario_name}: preview response malformed: {preview}")

    execute_result = post_json(
        "/api/migration/execute",
        {"task_id": task_id, "task_record_id": task_record_id, "target_tables": [target_table]},
    )
    migration_results = execute_result.get("results", [])
    expect(bool(migration_results), f"{scenario_name}: no migration results returned")
    expect(not [row for row in migration_results if row.get("status") == "failed"], f"{scenario_name}: migration returned failed result {migration_results}")

    jobs = get_json(f"/api/migration/jobs?task_id={task_id}&task_record_id={task_record_id}")
    expect(bool(jobs), f"{scenario_name}: no migration jobs written")
    expect(not [row for row in jobs if row.get("status") == "failed"], f"{scenario_name}: migration job failed {jobs}")

    return {
        "scenario": scenario_name,
        "datasource_id": datasource_id,
        "task_id": task_id,
        "task_record_id": task_record_id,
        "status": status,
        "batch_id": int(batch_id),
        "result_set_id": int(first_set["id"]),
        "migration_statuses": [row.get("status") for row in migration_results],
        "llm_config_id": current_llm.get("id"),
    }


def run_cancel_test(*, datasource_id: int) -> dict[str, Any]:
    task = ensure_task(
        name=f"cancel_test_{datetime.now().strftime('%Y%m%d_%H%M%S')}",
        datasource_id=datasource_id,
    )
    task_id = int(task["id"])
    run_result = post_json(f"/api/match-tasks/{task_id}/run")
    task_record_id = int(run_result["task_record_id"])
    cancel = post_json(f"/api/match-task-records/{task_record_id}/cancel")
    expect(bool(cancel.get("updated")), f"cancel test: cancel endpoint did not update record: {cancel}")
    record = wait_for_record(task_record_id, timeout_s=60)
    status = record.get("execution_status") or record.get("status")
    expect(status == "canceled", f"cancel test: expected canceled status, got {status}")
    return {"task_id": task_id, "task_record_id": task_record_id, "status": status}


def run_llm_reuse_test(*, datasource_id: int) -> dict[str, Any]:
    llm_fields = {
        "llm_provider": "openai",
        "llm_model_name": "gpt-4.1-mini",
        "llm_base_url": "https://api.openai.com/v1",
        "llm_api_key": "sk-acceptance-demo-key",
    }
    first = ensure_task(
        name=f"llm_reuse_a_{datetime.now().strftime('%Y%m%d_%H%M%S')}",
        datasource_id=datasource_id,
        llm_fields=llm_fields,
    )
    second = ensure_task(
        name=f"llm_reuse_b_{datetime.now().strftime('%Y%m%d_%H%M%S')}",
        datasource_id=datasource_id,
        llm_fields=llm_fields,
    )
    first_id = ((first.get("current_version") or {}).get("llm_config") or {}).get("id")
    second_id = ((second.get("current_version") or {}).get("llm_config") or {}).get("id")
    expect(bool(first_id) and bool(second_id), "llm reuse test: expected llm_config ids")
    expect(int(first_id) == int(second_id), f"llm reuse test: expected reuse, got {first_id} and {second_id}")

    updated = put_json(
        f"/api/match-tasks/{second['id']}",
        {
            "llm_provider": "openai",
            "llm_model_name": "gpt-4.1-mini",
            "llm_base_url": "https://api.openai.com/v1",
            "change_summary": "acceptance llm reuse without key",
        },
    )
    updated_id = (((updated.get("current_version") or {}).get("llm_config")) or {}).get("id")
    expect(int(updated_id) == int(second_id), f"llm reuse test: update should reuse existing config, got {updated_id}")
    return {"first_task_id": first["id"], "second_task_id": second["id"], "llm_config_id": int(first_id)}


def main() -> int:
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    summaries: list[dict[str, Any]] = []

    backend_service = ManagedService(
        name="backend",
        base_url=BACKEND_BASE_URL,
        health_path="/api/health",
        command=[APP_PYTHON, "-m", "uvicorn", "main:app", "--host", "127.0.0.1", "--port", "18000"],
        cwd=BACKEND_ROOT,
        env={"INTEGRATION_BASE_URL": BACKEND_BASE_URL},
    )
    plm_service = ManagedService(
        name="plm_adapter",
        base_url=PLM_ADAPTER_BASE_URL,
        health_path="/api/health",
        command=[APP_PYTHON, "-m", "uvicorn", "mock_adapters.plm.main:app", "--host", "127.0.0.1", "--port", "19001"],
        cwd=ROOT,
    )
    erp_service = ManagedService(
        name="erp_adapter",
        base_url=ERP_ADAPTER_BASE_URL,
        health_path="/api/health",
        command=[APP_PYTHON, "-m", "uvicorn", "mock_adapters.erp.main:app", "--host", "127.0.0.1", "--port", "19002"],
        cwd=ROOT,
    )

    with ExitStack() as stack:
        for service in (backend_service, plm_service, erp_service):
            service.start()
            stack.callback(service.stop)

        encoding_options = get_json("/api/encoding-modes")
        embedding_options = get_json("/api/embedding-models")
        eval_options = get_json("/api/eval-modes")
        expect(bool(encoding_options), "No encoding_mode options available")
        expect(bool(embedding_options), "No embedding_model options available")
        expect(bool(eval_options), "No eval_mode options available")

        scenarios = [
            (
                "PLM_api_push",
                {
                    "name": f"PLM_api_push_acceptance_{timestamp}",
                    "integration_mode": "api_push",
                    "push_source_base_url": PLM_ADAPTER_BASE_URL,
                    "trigger_endpoint": "/api/trigger-push",
                    "health_endpoint": "/api/health",
                    "api_key": "plm-demo-key",
                    "push_secret": "plm-demo-secret",
                    "enabled": True,
                    "notes": "acceptance automation",
                },
            ),
            (
                "ERP_api_pull",
                {
                    "name": f"ERP_api_pull_acceptance_{timestamp}",
                    "integration_mode": "api_pull",
                    "base_url": ERP_ADAPTER_BASE_URL,
                    "pull_endpoint": "/api/pull",
                    "health_endpoint": "/api/health",
                    "api_key": "erp-demo-key",
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
                    "enabled": True,
                    "notes": "acceptance automation",
                },
            ),
        ]

        for scenario_name, datasource_payload in scenarios:
            print(f"\n=== Running acceptance chain: {scenario_name} ===")
            summary = run_chain(scenario_name=scenario_name, datasource_payload=datasource_payload)
            print(summary)
            summaries.append(summary)

        llm_reuse_summary = run_llm_reuse_test(datasource_id=summaries[-1]["datasource_id"])
        print("\n=== LLM reuse acceptance ===")
        print(llm_reuse_summary)

        cancel_summary = run_cancel_test(datasource_id=summaries[-1]["datasource_id"])
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
