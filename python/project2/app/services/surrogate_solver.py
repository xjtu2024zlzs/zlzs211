from typing import Dict, List, Tuple

import numpy as np
from scipy.optimize import differential_evolution

from app.schemas.solve_schema import (
    BestSolution,
    CandidateSolution,
    IterationPoint,
    OptimizeRequest,
    OptimizeResponse,
)
from app.services.model_registry import active_model

INPUT_KEYS = ["L1", "L2", "theta1", "theta2", "R"]


def _bounds(request: OptimizeRequest) -> List[Tuple[float, float]]:
    bounds = []
    for key in INPUT_KEYS:
        item = request.variables.get(key)
        if item is None:
            raise ValueError(f"Missing variable bound: {key}")
        if item.upper <= item.lower:
            raise ValueError(f"Invalid bound for {key}: upper must be greater than lower")
        bounds.append((item.lower, item.upper))
    return bounds


def _solution(vector, stress: float) -> BestSolution:
    return BestSolution(
        L1=round(float(vector[0]), 4),
        L2=round(float(vector[1]), 4),
        theta1=round(float(vector[2]), 4),
        theta2=round(float(vector[3]), 4),
        R=round(float(vector[4]), 4),
        predictedStress=round(float(stress), 6),
    )


def optimize(request: OptimizeRequest) -> OptimizeResponse:
    try:
        model_info, model = active_model()
        bounds = _bounds(request)
        history: List[IterationPoint] = []
        candidates = {}
        iteration = {"value": 0}

        def objective(x):
            x_array = np.asarray(x, dtype=float).reshape(1, -1)
            stress = float(model.predict(x_array)[0])
            candidates[tuple(round(float(v), 6) for v in x)] = stress
            return stress

        def callback(xk, convergence):
            iteration["value"] += 1
            history.append(IterationPoint(iteration=iteration["value"], bestStress=round(objective(xk), 6)))
            return False

        result = differential_evolution(
            objective,
            bounds,
            maxiter=request.algorithm.maxIterations,
            popsize=request.algorithm.populationSize,
            seed=request.algorithm.seed,
            polish=True,
            updating="immediate",
            callback=callback,
        )

        best_stress = float(result.fun)
        best_solution = _solution(result.x, best_stress)
        ranked = sorted(candidates.items(), key=lambda item: item[1])[:10]
        top_candidates = [
            CandidateSolution(rank=index + 1, **_solution(vector, stress).dict())
            for index, (vector, stress) in enumerate(ranked)
        ]
        if not history:
            history.append(IterationPoint(iteration=1, bestStress=round(best_stress, 6)))

        return OptimizeResponse(
            status="SUCCESS",
            bestSolution=best_solution,
            candidates=top_candidates,
            history=history,
            iterations=max(iteration["value"], int(getattr(result, "nit", 0) or 0)),
            modelName=model_info.get("activeModel", ""),
            modelType=model_info.get("modelType", ""),
            objectiveName=model_info.get("outputName", "predictedStress"),
            objectiveUnit=model_info.get("outputUnit", "MPa"),
            errorMessage="",
        )
    except Exception as exc:
        return OptimizeResponse(status="FAILED", errorMessage=str(exc))
