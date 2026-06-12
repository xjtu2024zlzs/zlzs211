"""Lightweight file-backed task runner for algorithm service calls."""

from __future__ import annotations

import json
import threading
import uuid
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime
from pathlib import Path
from typing import Any, Callable, Dict, Optional


TASK_ROOT = Path(__file__).resolve().parent / "runtime" / "tasks"
TASK_ROOT.mkdir(parents=True, exist_ok=True)

EXECUTOR = ThreadPoolExecutor(max_workers=2)
LOCK = threading.RLock()

PENDING = "PENDING"
RUNNING = "RUNNING"
SUCCESS = "SUCCESS"
FAILED = "FAILED"
CANCELED = "CANCELED"
DONE = {SUCCESS, FAILED, CANCELED}


def is_terminal(status: Any) -> bool:
    return str(status or "").upper() in DONE


def can_transition(from_status: Any, to_status: Any) -> bool:
    current = str(from_status or "").upper()
    target = str(to_status or "").upper()
    if not target or current == target:
        return True
    if current in DONE:
        return False
    return (current, target) in {
        (PENDING, RUNNING),
        (PENDING, FAILED),
        (PENDING, CANCELED),
        (RUNNING, SUCCESS),
        (RUNNING, FAILED),
        (RUNNING, CANCELED),
    }


def now_text() -> str:
    return datetime.now().isoformat(timespec="seconds")


def new_task_id() -> str:
    return "PY" + datetime.now().strftime("%Y%m%d%H%M%S%f")[:17] + uuid.uuid4().hex[:4].upper()


def task_dir(task_id: str) -> Path:
    return TASK_ROOT / task_id


def status_path(task_id: str) -> Path:
    return task_dir(task_id) / "status.json"


def result_path(task_id: str) -> Path:
    return task_dir(task_id) / "result.json"


def logs_path(task_id: str) -> Path:
    return task_dir(task_id) / "logs.json"


def read_json(path: Path, default: Any) -> Any:
    if not path.exists():
        return default
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception:
        return default


def write_json(path: Path, data: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    tmp = path.with_suffix(path.suffix + ".tmp")
    tmp.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
    tmp.replace(path)


def get_status(task_id: str) -> Optional[Dict[str, Any]]:
    data = read_json(status_path(task_id), None)
    return data if isinstance(data, dict) else None


def get_result(task_id: str) -> Optional[Dict[str, Any]]:
    data = read_json(result_path(task_id), None)
    return data if isinstance(data, dict) else None


def get_logs(task_id: str) -> list:
    data = read_json(logs_path(task_id), [])
    return data if isinstance(data, list) else []


def append_log(task_id: str, level: str, message: str) -> None:
    with LOCK:
        logs = get_logs(task_id)
        logs.append({"time": now_text(), "level": level, "message": message})
        write_json(logs_path(task_id), logs)


def update_status(task_id: str, **fields: Any) -> Dict[str, Any]:
    with LOCK:
        current = get_status(task_id) or {"taskId": task_id}
        current_status = current.get("status")
        next_status = fields.get("status")
        if next_status and not can_transition(current_status, next_status):
            return current
        if is_terminal(current_status) and not next_status:
            return current
        current.update({k: v for k, v in fields.items() if v is not None})
        write_json(status_path(task_id), current)
        return current


def is_canceled(task_id: str) -> bool:
    status = get_status(task_id)
    return bool(status and status.get("status") == CANCELED)


def create_task(algorithm_type: str, payload: Dict[str, Any]) -> Dict[str, Any]:
    task_id = new_task_id()
    status = {
        "success": True,
        "taskId": task_id,
        "algorithmType": algorithm_type,
        "status": PENDING,
        "progress": 0,
        "message": "任务已提交",
        "createdAt": now_text(),
        "startedAt": None,
        "finishedAt": None,
        "result": None,
        "error": None,
        "payload": payload,
    }
    write_json(status_path(task_id), status)
    write_json(logs_path(task_id), [{"time": now_text(), "level": "INFO", "message": "任务已提交"}])
    return status


def submit_task(algorithm_type: str, payload: Dict[str, Any], runner: Callable[[str, str, Dict[str, Any]], Dict[str, Any]]) -> Dict[str, Any]:
    task = create_task(algorithm_type, payload)
    EXECUTOR.submit(run_task, task["taskId"], algorithm_type, payload, runner)
    return task


def run_task(task_id: str, algorithm_type: str, payload: Dict[str, Any], runner: Callable[[str, str, Dict[str, Any]], Dict[str, Any]]) -> None:
    if is_canceled(task_id):
        return
    update_status(task_id, status=RUNNING, progress=10, message="算法开始执行", startedAt=now_text())
    append_log(task_id, "INFO", "算法开始执行")
    try:
        if is_canceled(task_id):
            return
        update_status(task_id, progress=40, message="算法执行中")
        result = runner(task_id, algorithm_type, payload)
        if is_canceled(task_id):
            return
        write_json(result_path(task_id), result)
        result_status = str(result.get("status") or "").upper() if isinstance(result, dict) else ""
        result_success = result.get("success") if isinstance(result, dict) else True
        if result_status == FAILED or result_success is False:
            message = ""
            if isinstance(result, dict):
                message = str(result.get("message") or result.get("error") or "任务执行失败")
            update_status(task_id, status=FAILED, progress=100, message="任务执行失败", finishedAt=now_text(), result=None, error=message)
            append_log(task_id, "ERROR", message)
            return
        update_status(
            task_id,
            status=SUCCESS,
            progress=100,
            message="任务完成",
            finishedAt=now_text(),
            result=result,
            error="",
        )
        append_log(task_id, "INFO", "任务完成")
    except Exception as exc:
        if is_canceled(task_id):
            return
        message = str(exc) or "任务执行失败"
        update_status(task_id, status=FAILED, progress=100, message="任务执行失败", finishedAt=now_text(), error=message)
        append_log(task_id, "ERROR", message)


def cancel_task(task_id: str) -> Dict[str, Any]:
    with LOCK:
        status = get_status(task_id)
        if not status:
            return {"success": False, "message": "任务不存在"}
        current = status.get("status")
        if current == CANCELED:
            return {"success": True, "taskId": task_id, "status": CANCELED, "message": "任务已取消"}
        if current in {SUCCESS, FAILED}:
            return {"success": False, "taskId": task_id, "status": current, "message": "任务已结束，不能取消"}
        status.update({"status": CANCELED, "progress": status.get("progress", 0), "message": "任务已取消", "finishedAt": now_text()})
        write_json(status_path(task_id), status)
        append_log(task_id, "INFO", "任务已取消")
        return {"success": True, "taskId": task_id, "status": CANCELED, "message": "任务已取消"}
