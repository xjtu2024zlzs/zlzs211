from __future__ import annotations

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from .config import SOURCE_SYSTEMS
from .schemas import MatchRunAllRequest, MatchRunRequest
from .services.match_service import MatchService


app = FastAPI(
    title="CF Schema Matching Algorithm Service",
    description="FastAPI service that wraps Magneto-based CF schema matching for RuoYi backends.",
    version="0.1.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

service = MatchService()


@app.get("/api/health")
def health() -> dict:
    return {
        "status": "ok",
        "service": "cf-schema-matching-algorithm",
        "sourceSystems": list(SOURCE_SYSTEMS),
    }


@app.get("/api/v1/capabilities")
def capabilities() -> dict:
    return service.capabilities()


@app.post("/api/v1/match/run")
def run_match(request: MatchRunRequest) -> dict:
    try:
        return service.run(request)
    except Exception as exc:
        raise HTTPException(status_code=500, detail=str(exc)) from exc


@app.post("/api/v1/match/run-all")
def run_all(request: MatchRunAllRequest) -> dict:
    try:
        return service.run_all(request)
    except Exception as exc:
        raise HTTPException(status_code=500, detail=str(exc)) from exc

