from typing import Any, Dict, List

from pydantic import BaseModel, Field


class FatiguePredictRequest(BaseModel):
    samples: List[Dict[str, Any]] = Field(default_factory=list)


class FatiguePredictResponse(BaseModel):
    status: str
    modelVersion: str = "fatigue-v1.0"
    results: List[Dict[str, Any]] = Field(default_factory=list)
    errorMessage: str = ""
