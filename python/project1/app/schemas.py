from __future__ import annotations

from typing import Any, Literal

from pydantic import BaseModel, Field, model_validator


MethodName = Literal["Magneto", "MagnetoBoost", "MagnetoGPT"]
VariantName = Literal["all", "one2one"]


class SourceRequest(BaseModel):
    systemKey: str = "plm"
    baseUrl: str | None = None
    tables: list[str] = Field(default_factory=list)
    limitPerTable: int = Field(default=300, ge=1, le=5000)


class TargetRequest(BaseModel):
    mode: Literal["mysql", "file"] = "mysql"
    mysqlUrl: str | None = None
    tablePrefix: str = "p1p_dossier_"
    tables: list[str] = Field(default_factory=list)
    limitPerTable: int = Field(default=300, ge=1, le=5000)
    fileRoot: str | None = None


class GroundTruthRequest(BaseModel):
    enabled: bool = True
    mode: Literal["file"] = "file"
    rootPath: str | None = None


class AlgorithmRequest(BaseModel):
    method: MethodName | None = None
    methods: list[MethodName] = Field(default_factory=list)
    variants: list[VariantName] = Field(default_factory=lambda: ["all", "one2one"])
    embeddingModel: str = "mpnet"
    encodingMode: str = "header_values_verbose_with_table"
    samplingMode: str = "mixed"
    samplingSize: int = Field(default=10, ge=1, le=100)
    topk: int = Field(default=20, ge=1, le=200)
    threshold: float = Field(default=0.0, ge=0.0, le=1.0)
    useLlmReranker: bool = False
    llmModel: str | None = None
    llmModelKwargs: dict[str, Any] = Field(default_factory=dict)
    boostThreshold: float = Field(default=0.8, ge=0.0, le=1.0)
    boostAlpha: float = Field(default=0.15, ge=0.0, le=1.0)

    @model_validator(mode="before")
    @classmethod
    def normalize_legacy_method(cls, data):
        if not isinstance(data, dict):
            return data
        normalized = dict(data)
        if "methods" not in normalized:
            if normalized.get("useLlmReranker"):
                normalized["methods"] = ["MagnetoGPT"]
            elif normalized.get("method"):
                normalized["methods"] = [normalized["method"]]
        return normalized


class MatchRunRequest(BaseModel):
    requestId: str | None = None
    source: SourceRequest = Field(default_factory=SourceRequest)
    target: TargetRequest = Field(default_factory=TargetRequest)
    groundTruth: GroundTruthRequest = Field(default_factory=GroundTruthRequest)
    algorithm: AlgorithmRequest = Field(default_factory=AlgorithmRequest)


class MatchRunAllRequest(BaseModel):
    requestId: str | None = None
    sources: list[SourceRequest] = Field(default_factory=list)
    target: TargetRequest = Field(default_factory=TargetRequest)
    groundTruth: GroundTruthRequest = Field(default_factory=GroundTruthRequest)
    algorithm: AlgorithmRequest = Field(default_factory=AlgorithmRequest)

