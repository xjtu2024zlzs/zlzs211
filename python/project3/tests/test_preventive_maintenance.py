import pytest

from algorithms.preventive_maintenance import run_preventive_maintenance
from task_manager import TaskCanceledError


def valid_payload():
    return {
        "taskId": "PYTEST001",
        "taskNo": "PM-001",
        "equipmentId": "E001",
        "partInstanceId": "PI001",
        "processExecutionId": "PE001",
        "T1": 90,
        "T2": 80,
        "T3": 70,
        "N1": 2,
        "N2": 2,
        "N3": 2,
        "sampleCount": 5,
        "population": 4,
        "iterations": 1,
        "Rm": 0.1,
        "attackThreshold": 0.3,
        "seed": 42,
    }


def test_preventive_maintenance_returns_stable_contract():
    result = run_preventive_maintenance(valid_payload())

    assert result["success"] is True
    assert result["status"] == "SUCCESS"
    assert result["taskId"] == "PYTEST001"
    assert result["paretoResults"]
    assert sum(row["status"] == "RECOMMENDED" for row in result["paretoResults"]) == 1
    assert any(row["id"] == result["recommended"]["id"] for row in result["paretoResults"])
    assert all({"T1", "T2", "T3", "Cu", "A", "minR"} <= row.keys() for row in result["paretoResults"])
    assert len(result["monteCarloSamples"]["tm"]) == 5


@pytest.mark.parametrize(
    ("field", "value", "message"),
    [
        ("sampleCount", 0, "sampleCount"),
        ("population", 3, "population"),
        ("iterations", 0, "iterations"),
        ("Rm", 1.1, "Rm"),
        ("attackThreshold", -0.1, "attackThreshold"),
    ],
)
def test_preventive_maintenance_rejects_invalid_parameters(field, value, message):
    payload = valid_payload()
    payload[field] = value

    with pytest.raises(ValueError, match=message):
        run_preventive_maintenance(payload)


def test_preventive_maintenance_cooperatively_cancels_during_iteration():
    payload = valid_payload()
    payload["iterations"] = 20
    checks = 0

    def cancel_check():
        nonlocal checks
        checks += 1
        return checks >= 8

    with pytest.raises(TaskCanceledError, match="取消"):
        run_preventive_maintenance(payload, cancel_check=cancel_check)
