from pathlib import Path
from typing import Any, Dict, List

from fastapi import FastAPI
from pydantic import BaseModel, Field

from surrogate_model_api import load_model, predict_from_dict

BASE_DIR = Path(__file__).resolve().parent
MODEL_PATH = BASE_DIR / "kriging_surrogate_model.joblib"

app = FastAPI(title="Fatigue Crack Surrogate Worker", version="fatigue-v1.0")
model = load_model(str(MODEL_PATH))


class FatigueSample(BaseModel):
    a0_mm: float = Field(gt=0)
    p1_kn: float = Field(gt=0)
    n1: float = Field(ge=0)
    p2_kn: float = Field(gt=0)
    n2: float = Field(ge=0)
    p3_kn: float = Field(gt=0)
    n3: float = Field(ge=0)
    task_count_T: float = Field(ge=0)


class FatigueRequest(BaseModel):
    samples: List[FatigueSample]


def to_model_payload(sample: FatigueSample) -> Dict[str, Any]:
    return {
        "a0_mm": sample.a0_mm,
        "P1_kN": sample.p1_kn,
        "N1": sample.n1,
        "P2_kN": sample.p2_kn,
        "N2": sample.n2,
        "P3_kN": sample.p3_kn,
        "N3": sample.n3,
        "task_count_T": sample.task_count_T,
    }


@app.get("/health")
def health():
    return {"status": "UP", "service": "fatigue-crack-surrogate-worker"}


@app.post("/api/surrogate/fatigue/predict")
def predict(request: FatigueRequest):
    results = []
    for sample in request.samples:
        prediction = predict_from_dict(model, to_model_payload(sample), return_std=True)
        results.append({
            "mu": prediction.get("a_final_mm"),
            "sigma": prediction.get("pred_std_mm", 0.0),
            "a_final_mm": prediction.get("a_final_mm"),
            "growth_mm": prediction.get("growth_mm"),
            "pred_std_mm": prediction.get("pred_std_mm", 0.0),
        })
    return {
        "status": "success",
        "modelVersion": "fatigue-v1.0",
        "results": results,
    }
