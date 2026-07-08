from fastapi import FastAPI

from app.schemas.fatigue_schema import FatiguePredictRequest, FatiguePredictResponse
from app.schemas.solve_schema import OptimizeRequest, OptimizeResponse
from app.services.model_registry import get_active_model_info, get_model_catalog
from app.services.fatigue_predictor import predict_fatigue
from app.services.surrogate_solver import optimize

app = FastAPI(title="Project2 Surrogate Optimization Service", version="1.1.0")


@app.get("/health")
def health():
    return {
        "status": "UP",
        "service": "project2-surrogate-solver",
        "port": 9721,
        "apis": [
            "/api/surrogate/optimize",
            "/api/surrogate/fatigue/predict",
        ],
    }


@app.get("/api/models/active")
def active_model():
    return get_active_model_info()


@app.get("/api/models")
def models():
    return {
        "activeModel": get_active_model_info().get("modelName", ""),
        "models": get_model_catalog(),
    }


@app.post("/api/surrogate/optimize", response_model=OptimizeResponse)
def optimize_surrogate(request: OptimizeRequest):
    return optimize(request)


@app.post("/api/surrogate/fatigue/predict", response_model=FatiguePredictResponse)
def fatigue_predict(request: FatiguePredictRequest):
    return predict_fatigue(request)
