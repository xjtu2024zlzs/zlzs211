import json
import threading
from pathlib import Path

import pytest

import task_manager


@pytest.fixture
def isolated_task_root(tmp_path, monkeypatch):
    task_root = tmp_path / "tasks"
    monkeypatch.setattr(task_manager, "TASK_ROOT", task_root)
    return task_root


def test_create_task_writes_status_and_logs(isolated_task_root):
    task = task_manager.create_task("KQC_MINING", {"source": "sample.csv"})

    assert task["status"] == task_manager.PENDING
    assert task["version"] == 1
    assert task["progress"] == 0
    assert task_manager.get_status(task["taskId"])["payload"]["source"] == "sample.csv"
    assert task_manager.get_logs(task["taskId"])[0]["level"] == "INFO"
    assert (isolated_task_root / task["taskId"] / "status.json").exists()


def test_run_task_persists_success_result(isolated_task_root):
    task = task_manager.create_task("KEY_PROCESS", {})

    task_manager.run_task(
        task["taskId"],
        "KEY_PROCESS",
        {},
        lambda task_id, algorithm_type, payload: {
            "success": True,
            "status": "SUCCESS",
            "taskId": task_id,
            "algorithmType": algorithm_type,
        },
    )

    status = task_manager.get_status(task["taskId"])
    result = task_manager.get_result(task["taskId"])
    assert status["status"] == task_manager.SUCCESS
    assert status["version"] == 3
    assert status["progress"] == 100
    assert result["taskId"] == task["taskId"]
    assert task_manager.get_logs(task["taskId"])[-1]["level"] == "INFO"


def test_run_task_records_runner_failure(isolated_task_root):
    task = task_manager.create_task("PROCESS_ANOMALY", {})

    def failing_runner(task_id, algorithm_type, payload):
        raise RuntimeError("model unavailable")

    task_manager.run_task(task["taskId"], "PROCESS_ANOMALY", {}, failing_runner)

    status = task_manager.get_status(task["taskId"])
    assert status["status"] == task_manager.FAILED
    assert status["error"] == "model unavailable"
    assert task_manager.get_logs(task["taskId"])[-1]["message"] == "model unavailable"


def test_cancel_prevents_terminal_state_overwrite(isolated_task_root):
    task = task_manager.create_task("KQC_MINING", {})

    response = task_manager.cancel_task(task["taskId"])
    task_manager.update_status(task["taskId"], status=task_manager.SUCCESS, progress=100)

    assert response["success"] is True
    assert task_manager.get_status(task["taskId"])["status"] == task_manager.CANCELED


def test_repeated_cancel_is_idempotent(isolated_task_root):
    task = task_manager.create_task("KQC_MINING", {})

    first = task_manager.cancel_task(task["taskId"])
    second = task_manager.cancel_task(task["taskId"])

    assert first["success"] is True
    assert second["success"] is True
    assert first["version"] == second["version"]
    cancel_logs = [item for item in task_manager.get_logs(task["taskId"]) if item["message"] == "Task canceled"]
    assert len(cancel_logs) == 1


def test_cancel_during_runner_prevents_result_write(isolated_task_root):
    task = task_manager.create_task("PROCESS_ANOMALY", {})
    started = threading.Event()
    release = threading.Event()

    def runner(task_id, algorithm_type, payload):
        started.set()
        assert release.wait(timeout=2)
        return {"success": True, "status": "SUCCESS"}

    worker = threading.Thread(
        target=task_manager.run_task,
        args=(task["taskId"], "PROCESS_ANOMALY", {}, runner),
    )
    worker.start()
    assert started.wait(timeout=2)
    task_manager.cancel_task(task["taskId"])
    release.set()
    worker.join(timeout=2)

    assert task_manager.get_status(task["taskId"])["status"] == task_manager.CANCELED
    assert task_manager.get_result(task["taskId"]) is None


def test_missing_task_reads_return_empty_values(isolated_task_root):
    assert task_manager.get_status("missing") is None
    assert task_manager.get_result("missing") is None
    assert task_manager.get_logs("missing") == []
    assert task_manager.cancel_task("missing")["success"] is False


def test_read_json_returns_default_for_corrupt_file(tmp_path):
    path = tmp_path / "broken.json"
    path.write_text("{broken", encoding="utf-8")

    assert task_manager.read_json(path, {"fallback": True}) == {"fallback": True}


def test_read_json_recovers_interrupted_temp_file(tmp_path):
    path = tmp_path / "state.json"
    path.with_suffix(".json.tmp").write_text('{"status":"RUNNING"}', encoding="utf-8")

    assert task_manager.read_json(path, {}) == {"status": "RUNNING"}


def test_status_version_increments_only_on_status_change(isolated_task_root):
    task = task_manager.create_task("KQC_MINING", {})

    running = task_manager.update_status(task["taskId"], status=task_manager.RUNNING, progress=10)
    progress = task_manager.update_status(task["taskId"], progress=50)
    success = task_manager.update_status(task["taskId"], status=task_manager.SUCCESS, progress=100)

    assert running["version"] == 2
    assert progress["version"] == 2
    assert success["version"] == 3


def test_status_version_is_backward_compatible_for_old_status_file(isolated_task_root):
    task = task_manager.create_task("KQC_MINING", {})
    path = isolated_task_root / task["taskId"] / "status.json"
    old_status = task_manager.get_status(task["taskId"])
    old_status.pop("version")
    path.write_text(json.dumps(old_status), encoding="utf-8")

    running = task_manager.update_status(task["taskId"], status=task_manager.RUNNING)

    assert running["version"] == 1


def test_recover_incomplete_tasks_marks_pending_and_running_failed(isolated_task_root):
    pending = task_manager.create_task("KQC_MINING", {})
    running = task_manager.create_task("KEY_PROCESS", {})
    done = task_manager.create_task("KEY_PROCESS", {})
    task_manager.update_status(running["taskId"], status=task_manager.RUNNING)
    task_manager.update_status(done["taskId"], status=task_manager.RUNNING)
    task_manager.update_status(done["taskId"], status=task_manager.SUCCESS)

    recovered = task_manager.recover_incomplete_tasks()

    assert recovered == 2
    assert task_manager.get_status(pending["taskId"])["status"] == task_manager.FAILED
    assert task_manager.get_status(running["taskId"])["status"] == task_manager.FAILED
    assert task_manager.get_status(done["taskId"])["status"] == task_manager.SUCCESS


def test_write_json_replaces_existing_content_atomically(tmp_path):
    path = tmp_path / "state.json"
    task_manager.write_json(path, {"status": "PENDING"})
    task_manager.write_json(path, {"status": "SUCCESS"})

    assert json.loads(path.read_text(encoding="utf-8")) == {"status": "SUCCESS"}
    assert not path.with_suffix(".json.tmp").exists()


def test_write_json_failure_keeps_previous_file_and_cleans_temp(tmp_path, monkeypatch):
    path = tmp_path / "state.json"
    path.write_text('{"status":"PENDING"}', encoding="utf-8")

    def fail_replace(self, target):
        raise OSError("replace interrupted")

    monkeypatch.setattr(Path, "replace", fail_replace)
    with pytest.raises(OSError, match="replace interrupted"):
        task_manager.write_json(path, {"status": "SUCCESS"})

    assert json.loads(path.read_text(encoding="utf-8")) == {"status": "PENDING"}
    assert not path.with_suffix(".json.tmp").exists()


def test_result_write_failure_marks_task_failed(isolated_task_root, monkeypatch):
    task = task_manager.create_task("KEY_PROCESS", {})
    original_write_json = task_manager.write_json

    def fail_result(path, data):
        if path.name == "result.json":
            raise OSError("disk unavailable")
        return original_write_json(path, data)

    monkeypatch.setattr(task_manager, "write_json", fail_result)
    task_manager.run_task(
        task["taskId"],
        "KEY_PROCESS",
        {},
        lambda task_id, algorithm_type, payload: {"success": True, "status": "SUCCESS"},
    )

    status = task_manager.get_status(task["taskId"])
    assert status["status"] == task_manager.FAILED
    assert status["error"] == "disk unavailable"


def test_cooperative_cancel_exception_marks_task_canceled(isolated_task_root):
    task = task_manager.create_task("PREVENTIVE_MAINTENANCE", {})

    def canceled_runner(task_id, algorithm_type, payload):
        raise task_manager.TaskCanceledError("canceled during iteration 2")

    task_manager.run_task(task["taskId"], "PREVENTIVE_MAINTENANCE", {}, canceled_runner)

    status = task_manager.get_status(task["taskId"])
    assert status["status"] == task_manager.CANCELED
    assert "iteration 2" in status["message"]
    assert task_manager.get_result(task["taskId"]) is None
