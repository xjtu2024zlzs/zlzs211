from fastapi import FastAPI
from pydantic import BaseModel
from typing import Optional, Dict, Any

app = FastAPI(title="Project4 Fault Diagnosis Service")


class RunRequest(BaseModel):
    file_path: Optional[str] = None
    params: Optional[Dict[str, Any]] = None


@app.get("/health")
def health():
    return {
        "code": 200,
        "message": "project4 fastapi service is running",
        "service": "project4",
        "port": 9761
    }


@app.post("/preprocess/run")
def preprocess(req: RunRequest):
    return {
        "code": 200,
        "message": "preprocess success",
        "data": {
            "file_path": req.file_path,
            "params": req.params
        }
    }


@app.post("/augment/run")
def augment(req: RunRequest):
    return {
        "code": 200,
        "message": "augment success",
        "data": {
            "file_path": req.file_path,
            "params": req.params
        }
    }


@app.post("/diagnosis/run")
def diagnosis(req: RunRequest):
    return {
        "code": 200,
        "message": "diagnosis success",
        "data": {
            "fault_type": "unknown",
            "confidence": 0.0
        }
    }


@app.post("/fusion/run")
def fusion(req: RunRequest):
    return {
        "code": 200,
        "message": "fusion success",
        "data": {}
    }


@app.post("/root-cause/run")
def root_cause(req: RunRequest):
    return {
        "code": 200,
        "message": "root cause analysis success",
        "data": {}
    }