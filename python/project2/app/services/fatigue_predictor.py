from pathlib import Path
import sys
from typing import Any, Dict, List

from app.schemas.fatigue_schema import FatiguePredictRequest, FatiguePredictResponse

FATIGUE_WORKER_DIR = Path(__file__).resolve().parents[3] / "project22" / "fatigue_worker"
MODEL_PATH = FATIGUE_WORKER_DIR / "kriging_surrogate_model.joblib"

if str(FATIGUE_WORKER_DIR) not in sys.path:
    sys.path.insert(0, str(FATIGUE_WORKER_DIR))

from surrogate_model_api import load_model, predict_from_dict  # noqa: E402

MODEL_VERSION = "fatigue-v1.0"
_model = None


def _fatigue_model():
    global _model
    if _model is None:
        _model = load_model(str(MODEL_PATH))
    return _model


def _first(sample: Dict[str, Any], *keys: str) -> Any:
    for key in keys:
        if key in sample and sample[key] is not None:
            return sample[key]
    raise ValueError(f"missing field: {keys[0]}")


def _model_payload(sample: Dict[str, Any]) -> Dict[str, Any]:
    return {
        "a0_mm": _first(sample, "a0_mm"),
        "P1_kN": _first(sample, "P1_kN", "p1_kn"),
        "N1": _first(sample, "N1", "n1"),
        "P2_kN": _first(sample, "P2_kN", "p2_kn"),
        "N2": _first(sample, "N2", "n2"),
        "P3_kN": _first(sample, "P3_kN", "p3_kn"),
        "N3": _first(sample, "N3", "n3"),
        "task_count_T": _first(sample, "task_count_T", "task_count_t"),
    }


def predict_fatigue(request: FatiguePredictRequest) -> FatiguePredictResponse:
    try:
        if not request.samples:
            raise ValueError("samples cannot be empty")

        model = _fatigue_model()
        results: List[Dict[str, Any]] = []
        for sample in request.samples:
            prediction = predict_from_dict(model, _model_payload(sample), return_std=True)
            results.append({
                "mu": prediction.get("a_final_mm"),
                "sigma": prediction.get("pred_std_mm", 0.0),
                "a_final_mm": prediction.get("a_final_mm"),
                "growth_mm": prediction.get("growth_mm"),
                "pred_std_mm": prediction.get("pred_std_mm", 0.0),
                "critical_length_mm": prediction.get("critical_length_mm"),
                "failed": prediction.get("is_failed"),
                "failure_status": prediction.get("failure_status"),
                "remaining_margin_mm": prediction.get("remaining_margin_mm"),
            })

        return FatiguePredictResponse(status="success", modelVersion=MODEL_VERSION, results=results)
    except Exception as exc:
        return FatiguePredictResponse(status="failed", modelVersion=MODEL_VERSION, errorMessage=str(exc))
