from typing import Any, Dict, List, Optional

from pydantic import BaseModel, Field


class GrowthPoint(BaseModel):
    cycle: int
    crackLength: float


class MaintenanceAdvice(BaseModel):
    riskLevel: str = "MEDIUM"
    adviceType: str = "SHORTENED_INSPECTION"
    summary: str
    inspectionIntervalCycles: float
    continueServiceAllowed: bool
    loadRestrictionRequired: bool
    expertReviewRequired: bool


class CrackGrowthRequest(BaseModel):
    taskId: Optional[int] = None
    crackInput: Dict[str, Any] = Field(default_factory=dict)
    loadSpectrum: Dict[str, Any] = Field(default_factory=dict)


class CrackGrowthResponse(BaseModel):
    status: str = "SUCCESS"
    initialCrackLength: float
    criticalCrackLength: float
    predictedRemainingCycles: int
    predictedRemainingFlightHours: float
    riskLevel: str
    confidence: float = 0.82
    growthCurve: List[GrowthPoint] = Field(default_factory=list)
    modelName: str = "frame_beam_crack_life_surrogate.pkl"
    modelType: str = "Surrogate Crack Growth Predictor"
    maintenanceAdvice: Optional[MaintenanceAdvice] = None
    errorMessage: str = ""
