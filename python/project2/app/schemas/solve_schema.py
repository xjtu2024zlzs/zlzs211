from typing import Dict, List, Optional

from pydantic import BaseModel, Field


class VariableBound(BaseModel):
    lower: float
    upper: float
    step: Optional[float] = None


class AlgorithmConfig(BaseModel):
    type: str = "differential_evolution"
    maxIterations: int = Field(default=80, ge=1, le=1000)
    populationSize: int = Field(default=15, ge=3, le=100)
    seed: int = 42


class OptimizeRequest(BaseModel):
    taskId: Optional[int] = None
    modelPath: Optional[str] = None
    variables: Dict[str, VariableBound]
    algorithm: AlgorithmConfig = Field(default_factory=AlgorithmConfig)


class BestSolution(BaseModel):
    L1: float
    L2: float
    theta1: float
    theta2: float
    R: float
    predictedStress: float


class CandidateSolution(BestSolution):
    rank: int
    feasible: bool = True


class IterationPoint(BaseModel):
    iteration: int
    bestStress: float


class OptimizeResponse(BaseModel):
    status: str
    bestSolution: Optional[BestSolution] = None
    candidates: List[CandidateSolution] = Field(default_factory=list)
    history: List[IterationPoint] = Field(default_factory=list)
    iterations: int = 0
    modelName: str = ""
    modelType: str = ""
    objectiveName: str = "predictedStress"
    objectiveUnit: str = "MPa"
    errorMessage: str = ""
