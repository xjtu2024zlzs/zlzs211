from fastapi import FastAPI

from app.schemas.solve_schema import OptimizeRequest, OptimizeResponse
from app.services.model_registry import get_active_model_info
from app.services.surrogate_solver import optimize

app = FastAPI(title="Project2 Surrogate Optimization Service", version="1.0.0")


@app.get("/health")
def health():
    return {"status": "UP", "service": "project2-surrogate-solver", "port": 9721}


@app.get("/api/models/active")
def active_model():
    return get_active_model_info()


@app.post("/api/surrogate/optimize", response_model=OptimizeResponse)
def optimize_surrogate(request: OptimizeRequest):
    return optimize(request)
