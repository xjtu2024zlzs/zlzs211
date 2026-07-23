from importlib import import_module

from fastapi import Body, FastAPI, HTTPException

from app.schemas.fatigue_schema import FatiguePredictRequest, FatiguePredictResponse
from app.schemas.solve_schema import OptimizeRequest, OptimizeResponse
from app.services.model_registry import get_active_model_info, get_model_catalog
from app.services.fatigue_predictor import predict_fatigue
from app.services.surrogate_solver import optimize

app = FastAPI(title="Project2 Surrogate Optimization Service", version="1.1.0")

CABLE_ROUTING_ALGORITHMS = [
    {
        "label": "LP_Bend_3D 三维管线路径规划算法",
        "value": "lp_bend_3d",
        "algorithmKey": "lp_bend_3d",
        "badge": "MILP",
        "type": "success",
        "module": "pip_path.LP_Bend_3D",
        "entry": "solve_cable_routing",
        "aliases": ["builtin_cable_router", "builtin", "default"],
        "default": True,
        "description": "基于 PuLP/CBC 的三维网格管线路径规划算法，目标为路径长度与转弯惩罚最小。",
    }
]


@app.get("/health")
def health():
    return {
        "status": "UP",
        "service": "project2-surrogate-solver",
        "port": 9721,
        "apis": [
            "/api/surrogate/optimize",
            "/api/surrogate/fatigue/predict",
            "/api/cable-routing/algorithms",
            "/api/cable-routing/defaults",
            "/api/cable-routing/solve",
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


@app.get("/api/cable-routing/algorithms")
def cable_routing_algorithms():
    return {
        "status": "SUCCESS",
        "algorithms": [
            {key: value for key, value in item.items() if key not in ("module", "entry", "aliases")}
            for item in CABLE_ROUTING_ALGORITHMS
        ],
    }


@app.get("/api/cable-routing/defaults")
def cable_routing_defaults():
    algorithm = resolve_cable_routing_algorithm("lp_bend_3d")
    module = load_cable_routing_module(algorithm)
    if hasattr(module, "default_routing_payload"):
        return module.default_routing_payload()
    return {
        "algorithmKey": "lp_bend_3d",
        "algorithmCode": "lp_bend_3d",
        "algorithmSource": "project2_service",
        "algorithmName": "LP_Bend_3D 三维管线路径规划算法",
        "gridShape": [12, 12, 8],
        "gridUnitMm": 50.0,
        "bendWeight": 2.0,
        "solverMode": "milp",
        "timeLimitSeconds": 60,
        "pipeOuterDiameterMm": 9.53,
        "pipeInnerDiameterMm": 7.73,
        "bendRadiusMm": 20.0,
        "wallThicknessMm": 4.0,
        "boundaryWalls": ["floor", "left", "back"],
        "pipes": [
            {"name": "管路 1", "start": [0, 0, 0], "end": [11, 11, 7], "color": "#e14b4b"},
            {"name": "管路 2", "start": [0, 1, 1], "end": [10, 2, 6], "color": "#2f80ed"},
            {"name": "管路 3", "start": [2, 0, 7], "end": [9, 10, 0], "color": "#24a148"},
        ],
        "obstacles": [
            {"name": "障碍物 1", "min": [3, 3, 1], "max": [5, 5, 4]},
            {"name": "障碍物 2", "min": [7, 2, 0], "max": [8, 8, 2]},
            {"name": "障碍物 3", "min": [1, 8, 2], "max": [4, 10, 6]},
        ],
    }


@app.post("/api/cable-routing/solve")
def solve_cable_routing(request: dict = Body(default_factory=dict)):
    algorithm_key = request.get("algorithmKey") or request.get("algorithmCode") or "lp_bend_3d"
    algorithm = resolve_cable_routing_algorithm(algorithm_key)
    module = load_cable_routing_module(algorithm)
    entry = getattr(module, algorithm["entry"], None)
    if not callable(entry):
        raise HTTPException(status_code=500, detail=f"算法入口不存在：{algorithm['module']}.{algorithm['entry']}")
    try:
        result = entry(request)
    except Exception as exc:
        return {
            "status": "FAILED",
            "statusLabel": "求解失败",
            "algorithmKey": algorithm["algorithmKey"],
            "algorithmName": algorithm["label"],
            "errorMessage": str(exc),
        }
    if not isinstance(result, dict):
        raise HTTPException(status_code=500, detail="算法返回结果必须为 JSON 对象。")
    return result


def resolve_cable_routing_algorithm(algorithm_key):
    normalized = str(algorithm_key or "lp_bend_3d").strip()
    for item in CABLE_ROUTING_ALGORITHMS:
        supported_keys = {item["value"], item["algorithmKey"], *item.get("aliases", [])}
        if normalized in supported_keys:
            return item
    raise HTTPException(status_code=400, detail=f"不支持的线缆管路算法：{algorithm_key}")


def load_cable_routing_module(algorithm):
    try:
        return import_module(algorithm["module"])
    except ModuleNotFoundError as exc:
        raise HTTPException(status_code=500, detail=f"算法依赖或模块缺失：{exc}") from exc
    except Exception as exc:
        raise HTTPException(status_code=500, detail=f"算法模块加载失败：{exc}") from exc
