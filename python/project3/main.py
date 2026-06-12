"""FastAPI algorithm service for Spring Boot."""

from __future__ import annotations

import os
import json
from pathlib import Path
from typing import Any, Dict, List, Optional

from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from pydantic import ValidationError

from algorithms.data_upload_analysis import process_uploaded_dataset
from algorithms.feature_processing import extract_features_from_dataset
from algorithms.early_fault_detection import run_early_fault_detection
from algorithms.fault_prevention import run_fault_prevention
from algorithms.process_anomaly import run_process_anomaly
from algorithms.single_process_anomaly import run_single_process_anomaly
from algorithms.early_warning_wrapper import predict_frame_beam_crack
from algorithms.frame_beam_crack import run_frame_beam_crack
from algorithms.bosch_services import (
    run_bosch_key_station,
    run_bosch_kqc_mining,
    run_bosch_process_anomaly,
    run_id,
)
from task_manager import cancel_task, get_logs, get_result, get_status, submit_task


STORAGE_ROOT = Path(os.getenv("ALGORITHM_STORAGE_ROOT", "D:/2.11/data/topic3/result")).resolve()
STORAGE_ROOT.mkdir(parents=True, exist_ok=True)

app = FastAPI(
    title="Vibration PHM Algorithm Service",
    description="Data analysis, feature processing, early degradation detection and fault prevention service",
    version="1.1.0",
)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)


class UploadAnalyzeRequest(BaseModel):
    """Spring Boot upload-analysis request."""

    taskId: Optional[str] = None
    datasetId: Optional[str] = None
    taskType: Optional[str] = None
    fileMode: Optional[str] = None
    sampleIds: List[int] = Field(default_factory=list)
    filePath: Optional[str] = None
    fileUrl: Optional[str] = None
    filePaths: List[str] = Field(default_factory=list)
    fileUrls: List[str] = Field(default_factory=list)
    batchPath: Optional[str] = None
    samplingFrequency: int = 25600
    columnIndex: int = 0
    windowSize: float = 1.0
    overlapPercent: float = 50.0
    removeOutliers: bool = True
    maxSeconds: float = 5.0


class FeatureExtractRequest(BaseModel):
    taskId: Optional[str] = None
    taskType: Optional[str] = "FEATURE_PROCESSING"
    sourceTaskId: Optional[str] = None
    datasetId: str
    combinedDataPath: str
    samplingFrequency: int = 25600
    windowSize: float = 1.0
    overlapPercent: float = 50.0
    featureSetId: Optional[str] = None


class EarlyFaultDetectionRequest(BaseModel):
    taskId: Optional[str] = None
    taskType: Optional[str] = "EARLY_DEGRADATION_POINT_DETECT"
    sourceTaskId: Optional[str] = None
    datasetId: str
    featureSetId: str
    featurePath: str
    methods: List[str] = Field(default_factory=lambda: ["kurtosis_3sigma", "rms_trend"])
    detectionMethods: List[str] = Field(default_factory=list)
    baselineWindows: int = 500
    baselineWindowCount: Optional[int] = None
    rmsSensitivity: float = 0.2
    samplingFrequency: int = 25600
    windowSize: float = 1.0
    overlapPercent: float = 50.0
    fusionStrategy: str = "earliest"
    detectionId: Optional[str] = None


class FaultPreventionRequest(BaseModel):
    taskId: Optional[str] = None
    taskType: Optional[str] = "FAULT_PREDICT"
    sourceTaskId: Optional[str] = None
    degradationTaskId: Optional[str] = None
    datasetId: str
    featureSetId: Optional[str] = None
    featurePath: Optional[str] = None
    combinedDataPath: Optional[str] = None
    samplingFrequency: int = 25600
    degradationTime: Optional[float] = None
    earlyDegradationTime: Optional[float] = None
    detectionId: Optional[str] = None
    detectionPath: Optional[str] = None
    preventionId: Optional[str] = None
    predictionHorizon: Optional[float] = 50
    riskThreshold: float = 0.8
    rulUnit: str = "second"
    featureName: str = "rms"


class ProcessAnomalyRequest(BaseModel):
    """TEP process anomaly detection request.

    standardDataPath / trainPath:
        Standard or reference process data path.
    realDataPath / testPath:
        Real process data path to be scored.
    """

    taskId: Optional[str] = None
    taskType: Optional[str] = "PROCESS_ANOMALY_DETECT"
    sourceTaskId: Optional[str] = None
    datasetId: Optional[str] = None

    trainPath: Optional[str] = None
    testPath: Optional[str] = None
    standardDataPath: Optional[str] = None
    realDataPath: Optional[str] = None
    outputPath: Optional[str] = None

    nLags: int = 2
    nComponents: float = 30
    thresholdQuantile: float = 0.995
    batchSize: int = 50000
    groupCol: str = "simulationRun"
    timeCol: str = "sample"

    normalOnly: bool = False
    preFaultSample: Optional[int] = None
    ewmaAlpha: Optional[float] = 0.2

    boschTrainNumericPath: Optional[str] = None
    bosch_train_numeric_path: Optional[str] = None
    station: Optional[str] = None
    featureColName: Optional[str] = None
    feature_col: Optional[str] = None
    selectionMode: str = "response_assoc"
    maxRows: int = 200000
    minFailedObserved: int = 10
    minObserved: int = 1000
    epochs: int = 60
    alphaSample: float = 0.01
    ewmaLambda: float = 0.30
    seed: int = 0
    device: str = "auto"


class BoschKqcRequest(BaseModel):
    taskId: Optional[str] = None
    trainNumericPath: Optional[str] = None
    train_numeric_path: Optional[str] = None
    boschTrainNumericPath: Optional[str] = None
    bosch_train_numeric_path: Optional[str] = None
    outputDir: Optional[str] = None
    maxRows: int = 200000
    maxFeatures: int = 80
    perStation: int = 2
    selectionMode: str = "response_assoc"
    keepFreq: float = 0.6
    weightThresh: float = 1e-4
    topK: int = 3
    ssRuns: int = 20
    ssFrac: float = 0.8


class BoschKeyStationRequest(BaseModel):
    taskId: Optional[str] = None
    kqcTaskId: Optional[str] = None
    kqc_task_id: Optional[str] = None
    trainNumericPath: Optional[str] = None
    train_numeric_path: Optional[str] = None
    boschTrainNumericPath: Optional[str] = None
    bosch_train_numeric_path: Optional[str] = None
    kqcOutputDir: Optional[str] = None
    kqc_output_dir: Optional[str] = None
    adjPath: Optional[str] = None
    adj_path: Optional[str] = None
    freqPath: Optional[str] = None
    freq_path: Optional[str] = None
    topN: int = 10
    maxRows: int = 200000
    maxFeatures: int = 80
    perStation: int = 2
    selectionMode: str = "response_assoc"
    keepFreq: float = 0.6
    weightThresh: float = 1e-4
    ssRuns: int = 20


class AlgorithmTaskSubmitRequest(BaseModel):
    algorithmType: str
    payload: Dict[str, Any] = Field(default_factory=dict)


@app.get("/health")
def health():
    return {"status": "UP", "storageRoot": str(STORAGE_ROOT)}


@app.post("/predict")
async def predict_frame_beam_crack_endpoint(http_request: Request):
    """Aircraft frame beam early crack identification endpoint.

    Java can call this endpoint with JSON. For future CSV forwarding, multipart
    form requests are also accepted with a file/uploadedFile field.
    """
    try:
        payload = await _parse_frame_beam_predict_payload(http_request)
        return _frame_beam_success(predict_frame_beam_crack(payload))
    except Exception as exc:
        return _frame_beam_failure(str(exc))


@app.post("/frame-beam-crack/predict")
async def frame_beam_crack_predict(http_request: Request):
    try:
        payload = await _parse_frame_beam_predict_payload(http_request)
        return _frame_beam_success(run_frame_beam_crack(payload))
    except Exception as exc:
        return _frame_beam_failure(str(exc))


def _frame_beam_success(result: Dict[str, Any]) -> Dict[str, Any]:
    result = result or {}
    result["success"] = True
    result["message"] = "success"
    result.setdefault("task", {})
    result.setdefault("probabilities", {"healthProbability": 0.0, "faultProbability": 0.0})
    result.setdefault("metrics", {"accuracy": 0.0, "earlyCrackAccuracy": 0.0, "weightedF1": 0.0, "testLoss": 0.0})
    result.setdefault("waveform", {"xAxis": [], "raw": [], "denoised": []})
    result.setdefault("stft", {"xAxis": [], "yAxis": [], "data": [], "min": 0, "max": 1})
    result.setdefault("historyRows", [])
    return result


def _frame_beam_failure(message: str) -> Dict[str, Any]:
    return {
        "success": False,
        "message": message or "algorithm failed",
        "task": {},
        "probabilities": {"healthProbability": 0.0, "faultProbability": 0.0},
        "metrics": {"accuracy": 0.0, "earlyCrackAccuracy": 0.0, "weightedF1": 0.0, "testLoss": 0.0},
        "waveform": {"xAxis": [], "raw": [], "denoised": []},
        "stft": {"xAxis": [], "yAxis": [], "data": [], "min": 0, "max": 1},
        "historyRows": [],
    }


async def _parse_frame_beam_predict_payload(http_request: Request) -> Dict[str, Any]:
    content_type = http_request.headers.get("content-type", "").lower()
    if "multipart/form-data" not in content_type:
        try:
            payload = await http_request.json()
            return payload if isinstance(payload, dict) else {}
        except Exception:
            return {}

    form = await http_request.form()
    payload: Dict[str, Any] = {}

    raw_payload = form.get("payload") or form.get("data") or form.get("json")
    if raw_payload:
        parsed = _json_field(raw_payload)
        if isinstance(parsed, dict):
            payload.update(parsed)

    object_info = form.get("objectInfo")
    if object_info:
        parsed_object_info = _json_field(object_info)
        if isinstance(parsed_object_info, dict):
            payload["objectInfo"] = parsed_object_info

    params = form.get("params")
    if params:
        parsed_params = _json_field(params)
        if isinstance(parsed_params, dict):
            payload["params"] = parsed_params

    uploaded = form.get("uploadedFile") or form.get("file")
    if uploaded is not None:
        if hasattr(uploaded, "read"):
            content = await uploaded.read()
            payload["uploadedFile"] = content.decode("utf-8", errors="ignore")
        else:
            payload["uploadedFile"] = str(uploaded)

    return payload


def _json_field(value: Any) -> Any:
    if isinstance(value, (dict, list)):
        return value
    try:
        return json.loads(str(value))
    except Exception:
        return None


@app.post("/algorithm/tasks")
def submit_algorithm_task(request: AlgorithmTaskSubmitRequest):
    algorithm_type = (request.algorithmType or "").strip().upper()
    if algorithm_type not in {"PROCESS_ANOMALY", "KEY_PROCESS", "KQC_MINING"}:
        return {"success": False, "message": f"unsupported algorithmType: {request.algorithmType}"}
    task = submit_task(algorithm_type, dict(request.payload or {}), _run_algorithm_task)
    return {
        "success": True,
        "taskId": task["taskId"],
        "algorithmType": algorithm_type,
        "status": task["status"],
        "progress": task["progress"],
        "message": task["message"],
    }


@app.get("/algorithm/tasks/{task_id}")
def get_algorithm_task(task_id: str):
    status = get_status(task_id)
    if not status:
        return {"success": False, "message": "任务不存在"}
    return status


@app.get("/algorithm/tasks/{task_id}/logs")
def get_algorithm_task_logs(task_id: str):
    if not get_status(task_id):
        return {"success": False, "message": "任务不存在", "logs": []}
    return {"success": True, "taskId": task_id, "logs": get_logs(task_id)}


@app.get("/algorithm/tasks/{task_id}/result")
def get_algorithm_task_result(task_id: str):
    if not get_status(task_id):
        return {"success": False, "message": "任务不存在", "result": None}
    return {"success": True, "taskId": task_id, "result": get_result(task_id)}


@app.post("/algorithm/tasks/{task_id}/cancel")
def cancel_algorithm_task(task_id: str):
    return cancel_task(task_id)


# ============================================================
# 特征分析/数据分析 — runFeature()
# POST /python/data-analysis
# ============================================================
@app.post("/python/data-analysis")
async def python_data_analysis(http_request: Request):
    """Interface one: data analysis."""
    request, invalid_response = await _upload_analyze_request(http_request)
    if invalid_response is not None:
        return invalid_response

    try:
        dataset_id = request.datasetId or request.taskId
        if not dataset_id:
            return {
                "taskId": request.taskId,
                "datasetId": request.datasetId,
                "taskType": request.taskType or "FEATURE_ANALYSIS",
                "status": "FAILED",
                "message": "datasetId and taskId cannot both be empty",
                "errorCode": "INVALID_PARAMETER",
                "fileReports": [],
            }

        file_mode = (request.fileMode or "").upper()
        file_paths = list(request.filePaths or [])
        file_urls = list(request.fileUrls or [])
        if file_mode == "SINGLE_FILE":
            if request.filePath:
                file_paths = [request.filePath]
            elif file_paths:
                file_paths = [file_paths[0]]
            if request.fileUrl:
                file_urls = [request.fileUrl]
            elif file_urls:
                file_urls = [file_urls[0]]
        elif request.filePath and not file_paths:
            file_paths = [request.filePath]
        if not file_paths:
            return {
                "taskId": request.taskId,
                "datasetId": dataset_id,
                "taskType": request.taskType or "FEATURE_ANALYSIS",
                "status": "FAILED",
                "message": "filePaths cannot be empty",
                "errorCode": "FILE_PATHS_EMPTY",
                "fileReports": [],
            }

        return process_uploaded_dataset(
            dataset_id=dataset_id,
            task_id=request.taskId,
            task_type=request.taskType or "FEATURE_ANALYSIS",
            file_mode=request.fileMode,
            sample_ids=request.sampleIds,
            file_paths=file_paths,
            file_urls=file_urls,
            batch_path=request.batchPath,
            storage_root=STORAGE_ROOT,
            sampling_frequency=request.samplingFrequency,
            column_index=request.columnIndex,
            window_size=request.windowSize,
            overlap_percent=request.overlapPercent,
            remove_outliers=request.removeOutliers,
            max_seconds=request.maxSeconds,
            save_original_files=False,
        )
    except Exception as exc:
        dataset_id = request.datasetId or request.taskId
        return {
            "taskId": request.taskId,
            "datasetId": dataset_id,
            "taskType": request.taskType or "FEATURE_ANALYSIS",
            "status": "FAILED",
            "message": str(exc),
            "errorCode": "DATA_ANALYSIS_FAILED",
            "fileReports": [],
        }


async def _upload_analyze_request(http_request: Request):
    """Parse Spring Boot and direct frontend-compatible payloads without FastAPI 422."""
    try:
        raw_payload = await http_request.json()
    except Exception as exc:
        return None, _invalid_data_analysis_response(
            None,
            "request body must be valid JSON",
            f"INVALID_JSON: {exc}",
        )

    if not isinstance(raw_payload, dict):
        return None, _invalid_data_analysis_response(
            None,
            "request body must be a JSON object",
            "BODY_NOT_OBJECT",
        )

    payload = _normalize_upload_analyze_payload(raw_payload)
    try:
        return UploadAnalyzeRequest.model_validate(payload), None
    except ValidationError as exc:
        task_id = _text(payload.get("taskId") or payload.get("task_id"))
        return None, _invalid_data_analysis_response(
            task_id,
            "request body does not match data-analysis schema",
            exc.errors(),
        )


def _normalize_upload_analyze_payload(payload: Dict[str, Any]) -> Dict[str, Any]:
    """Accept both flat Java payloads and wrapper payloads containing feature_params."""
    normalized = dict(payload)
    nested = payload.get("feature_params") or payload.get("featureParams") or payload.get("params")
    if isinstance(nested, dict):
        merged = dict(nested)
        merged.update({k: v for k, v in normalized.items() if v is not None})
        normalized = merged

    aliases = {
        "task_id": "taskId",
        "dataset_id": "datasetId",
        "task_type": "taskType",
        "file_mode": "fileMode",
        "sample_ids": "sampleIds",
        "file_path": "filePath",
        "file_url": "fileUrl",
        "file_paths": "filePaths",
        "file_urls": "fileUrls",
        "batch_path": "batchPath",
        "sampling_frequency": "samplingFrequency",
        "samplingRate": "samplingFrequency",
        "sampling_rate": "samplingFrequency",
        "column_index": "columnIndex",
        "window_size": "windowSize",
        "overlap_percent": "overlapPercent",
        "overlapRate": "overlapPercent",
        "overlap_rate": "overlapPercent",
        "remove_outliers": "removeOutliers",
        "max_seconds": "maxSeconds",
    }
    for source, target in aliases.items():
        if source in normalized and target not in normalized:
            normalized[target] = normalized[source]

    if not normalized.get("datasetId"):
        normalized["datasetId"] = (
            normalized.get("taskId")
            or normalized.get("import_record_id")
            or normalized.get("importRecordId")
        )
    return normalized


def _invalid_data_analysis_response(task_id: Optional[str], message: str, detail: Any):
    return {
        "taskId": task_id,
        "datasetId": task_id,
        "taskType": "FEATURE_ANALYSIS",
        "status": "FAILED",
        "message": message,
        "errorCode": "INVALID_REQUEST_SCHEMA",
        "detail": detail,
        "fileReports": [],
    }


def _text(value: Any) -> Optional[str]:
    if value is None:
        return None
    text = str(value).strip()
    return text or None


# ============================================================
# 特征处理 — runFeatureProcessing()
# POST /python/feature-analysis
# ============================================================
@app.post("/python/feature-analysis")
def python_feature_analysis(request: FeatureExtractRequest):
    """Interface two: feature analysis from data_combined.npy."""
    try:
        if not request.taskId:
            return {
                "taskId": request.taskId,
                "datasetId": request.datasetId,
                "status": "FAILED",
                "message": "taskId cannot be empty",
                "errorCode": "INVALID_PARAMETER",
            }
        if not request.datasetId:
            return {
                "taskId": request.taskId,
                "datasetId": request.datasetId,
                "status": "FAILED",
                "message": "datasetId cannot be empty",
                "errorCode": "INVALID_PARAMETER",
            }
        if not request.combinedDataPath or not Path(request.combinedDataPath).exists():
            return {
                "taskId": request.taskId,
                "datasetId": request.datasetId,
                "status": "FAILED",
                "message": "combinedDataPath not found, please run interface one first",
                "errorCode": "COMBINED_DATA_NOT_FOUND",
            }

        output_dir = STORAGE_ROOT / "features" / request.datasetId
        return extract_features_from_dataset(
            dataset_id=request.datasetId,
            combined_data_path=request.combinedDataPath,
            output_dir=output_dir,
            task_id=request.taskId,
            task_type=request.taskType or "FEATURE_PROCESSING",
            source_task_id=request.sourceTaskId,
            sampling_frequency=request.samplingFrequency,
            window_size=request.windowSize,
            overlap_percent=request.overlapPercent,
            feature_set_id=request.featureSetId,
        )
    except Exception as exc:
        return {
            "taskId": request.taskId,
            "datasetId": request.datasetId,
            "status": "FAILED",
            "message": str(exc),
            "errorCode": "FEATURE_ANALYSIS_FAILED",
        }


# ============================================================
# 早期退化点检测 — detect_degradation()
# POST /algorithm/detection/early-fault
# ============================================================
@app.post("/algorithm/detection/early-fault")
def early_fault_detection(request: EarlyFaultDetectionRequest):
    """Algorithm three: early degradation detection from featurePath."""
    return _run_early_fault_detection_request(request)


def _run_early_fault_detection_request(request: EarlyFaultDetectionRequest):
    try:
        if not request.datasetId:
            return {
                "taskId": request.taskId,
                "datasetId": request.datasetId,
                "status": "FAILED",
                "message": "datasetId cannot be empty",
                "errorCode": "INVALID_PARAMETER",
            }
        if not request.featureSetId:
            return {
                "taskId": request.taskId,
                "datasetId": request.datasetId,
                "status": "FAILED",
                "message": "featureSetId cannot be empty",
                "errorCode": "INVALID_PARAMETER",
            }
        if not request.featurePath or not Path(request.featurePath).exists():
            return {
                "taskId": request.taskId,
                "datasetId": request.datasetId,
                "featureSetId": request.featureSetId,
                "featurePath": request.featurePath,
                "status": "FAILED",
                "message": "featurePath not found, please run interface two first",
                "errorCode": "FEATURE_PATH_NOT_FOUND",
            }

        output_dir = STORAGE_ROOT / "detections" / request.datasetId
        methods = request.detectionMethods or request.methods
        baseline_windows = request.baselineWindowCount if request.baselineWindowCount is not None else request.baselineWindows
        result = run_early_fault_detection(
            dataset_id=request.datasetId,
            feature_set_id=request.featureSetId,
            feature_path=request.featurePath,
            output_dir=output_dir,
            methods=methods,
            baseline_windows=baseline_windows,
            rms_sensitivity=request.rmsSensitivity,
            fusion_strategy=request.fusionStrategy,
            detection_id=request.detectionId,
        )
        result["taskId"] = request.taskId
        result["taskType"] = request.taskType or "EARLY_DEGRADATION_POINT_DETECT"
        result["sourceTaskId"] = request.sourceTaskId
        result["featurePath"] = str(Path(request.featurePath)).replace("\\", "/")
        result["samplingFrequency"] = request.samplingFrequency
        result["windowSize"] = request.windowSize
        result["overlapPercent"] = request.overlapPercent
        result["baselineWindowCount"] = baseline_windows
        result["rmsSensitivity"] = request.rmsSensitivity
        return result
    except Exception as exc:
        return {
            "taskId": request.taskId,
            "datasetId": request.datasetId,
            "featureSetId": request.featureSetId,
            "featurePath": request.featurePath,
            "status": "FAILED",
            "message": str(exc),
            "errorCode": "EARLY_DEGRADATION_DETECT_FAILED",
        }


# ============================================================
# 故障预测/故障预防 — runPredict()
# POST /algorithm/prevention/analyze
# ============================================================
@app.post("/algorithm/prevention/analyze")
def prevention_analyze(request: FaultPreventionRequest):
    """Algorithm four: fault prevention analysis."""
    try:
        output_dir = STORAGE_ROOT / "preventions" / request.datasetId
        return run_fault_prevention(
            dataset_id=request.datasetId,
            output_dir=output_dir,
            feature_path=request.featurePath,
            combined_data_path=request.combinedDataPath,
            sampling_frequency=request.samplingFrequency,
            degradation_time=request.degradationTime if request.degradationTime is not None else request.earlyDegradationTime,
            detection_path=request.detectionPath,
            prevention_id=request.preventionId,
            task_id=request.taskId,
            task_type=request.taskType or "FAULT_PREDICT",
            source_task_id=request.sourceTaskId or request.degradationTaskId,
            feature_set_id=request.featureSetId,
            detection_id=request.detectionId,
            risk_threshold=request.riskThreshold,
            rul_unit=request.rulUnit,
            feature_name=request.featureName,
            prediction_horizon=request.predictionHorizon,
        )
    except Exception as exc:
        return {
            "taskId": request.taskId,
            "taskType": request.taskType or "FAULT_PREDICT",
            "sourceTaskId": request.sourceTaskId or request.degradationTaskId,
            "datasetId": request.datasetId,
            "status": "FAILED",
            "message": str(exc),
            "errorCode": "FAULT_PREVENTION_FAILED",
        }


# ============================================================
# 工序异常/预警检测 — runWarningDetect()
# POST /algorithm/process-anomaly/execute
# ============================================================
@app.post("/algorithm/process-anomaly/execute")
def process_anomaly_execute(request: ProcessAnomalyRequest):
    """Process anomaly detection based on TEP PCA / DPCA statistics."""
    try:
        bosch_path = request.boschTrainNumericPath or request.bosch_train_numeric_path
        if bosch_path:
            output_dir = STORAGE_ROOT / "bosch_process_anomaly" / (request.datasetId or request.taskId or run_id("bosch_pa"))
            result = run_bosch_process_anomaly(
                train_numeric_csv=bosch_path,
                output_dir=output_dir,
                task_id=request.taskId,
                station=request.station or "",
                feature_col=request.featureColName or request.feature_col or "",
                selection_mode=request.selectionMode,
                max_rows=request.maxRows,
                min_failed_observed=request.minFailedObserved,
                min_observed=request.minObserved,
                epochs=request.epochs,
                batch_size=request.batchSize,
                alpha_sample=request.alphaSample,
                ewma_lambda=request.ewmaLambda,
                seed=request.seed,
                device=request.device,
            )
            return _algorithm_response(result)

        train_path = request.trainPath or request.standardDataPath
        test_path = request.testPath or request.realDataPath
        if not train_path:
            return {
                "taskId": request.taskId,
                "taskType": request.taskType or "PROCESS_ANOMALY_DETECT",
                "datasetId": request.datasetId,
                "status": "FAILED",
                "message": "trainPath or standardDataPath cannot be empty",
                "errorCode": "TRAIN_PATH_EMPTY",
            }
        if not test_path:
            return {
                "taskId": request.taskId,
                "taskType": request.taskType or "PROCESS_ANOMALY_DETECT",
                "datasetId": request.datasetId,
                "status": "FAILED",
                "message": "testPath or realDataPath cannot be empty",
                "errorCode": "TEST_PATH_EMPTY",
            }
        if not Path(train_path).exists():
            return {
                "taskId": request.taskId,
                "taskType": request.taskType or "PROCESS_ANOMALY_DETECT",
                "datasetId": request.datasetId,
                "status": "FAILED",
                "message": f"training data path not found: {train_path}",
                "errorCode": "TRAIN_PATH_NOT_FOUND",
            }
        if not Path(test_path).exists():
            return {
                "taskId": request.taskId,
                "taskType": request.taskType or "PROCESS_ANOMALY_DETECT",
                "datasetId": request.datasetId,
                "status": "FAILED",
                "message": f"test data path not found: {test_path}",
                "errorCode": "TEST_PATH_NOT_FOUND",
            }

        output_dir = STORAGE_ROOT / "anomaly" / (request.datasetId or request.taskId or "default")
        result = run_process_anomaly(
            train_path=train_path,
            test_path=test_path,
            output_dir=output_dir,
            output_path=request.outputPath,
            task_id=request.taskId,
            dataset_id=request.datasetId,
            task_type=request.taskType or "PROCESS_ANOMALY_DETECT",
            n_lags=request.nLags,
            n_components=request.nComponents,
            threshold_quantile=request.thresholdQuantile,
            batch_size=request.batchSize,
            group_col=request.groupCol,
            time_col=request.timeCol,
            normal_only=request.normalOnly,
            pre_fault_sample=request.preFaultSample,
            ewma_alpha=request.ewmaAlpha,
        )
        result["sourceTaskId"] = request.sourceTaskId
        return result
    except Exception as exc:
        return {
            "taskId": request.taskId,
            "taskType": request.taskType or "PROCESS_ANOMALY_DETECT",
            "sourceTaskId": request.sourceTaskId,
            "datasetId": request.datasetId,
            "status": "FAILED",
            "message": str(exc),
            "errorCode": "PROCESS_ANOMALY_FAILED",
        }


@app.post("/algorithm/bosch/kqc-mining/execute")
def bosch_kqc_mining_execute(request: BoschKqcRequest):
    """Bosch global key quality characteristic mining."""
    task_id = request.taskId
    try:
        train_path = (
            request.trainNumericPath
            or request.train_numeric_path
            or request.boschTrainNumericPath
            or request.bosch_train_numeric_path
        )
        if not train_path:
            return _failed_algorithm_response(task_id, "KQC_MINING", "trainNumericPath cannot be empty", "TRAIN_PATH_EMPTY")
        output_dir = Path(request.outputDir) if request.outputDir else STORAGE_ROOT / "bosch_kqc" / (task_id or run_id("bosch_kqc"))
        result = run_bosch_kqc_mining(
            train_numeric_csv=train_path,
            output_dir=output_dir,
            task_id=task_id,
            max_rows=request.maxRows,
            max_features=request.maxFeatures,
            per_station=request.perStation,
            selection_mode=request.selectionMode,
            keep_freq=request.keepFreq,
            weight_thresh=request.weightThresh,
            top_k=request.topK,
            ss_runs=request.ssRuns,
            ss_frac=request.ssFrac,
        )
        return _algorithm_response(result)
    except Exception as exc:
        return _failed_algorithm_response(task_id, "KQC_MINING", str(exc), "BOSCH_KQC_MINING_FAILED")


@app.post("/api/v1/key-process/identify")
async def key_process_identify(http_request: Request):
    """Bosch key station identification, compatible with the Java key-process task."""
    try:
        raw_payload = await http_request.json()
    except Exception:
        return _failed_algorithm_response(None, "KEY_PROCESS_IDENTIFY", "request body must be valid JSON", "INVALID_JSON")
    if not isinstance(raw_payload, dict):
        return _failed_algorithm_response(None, "KEY_PROCESS_IDENTIFY", "request body must be a JSON object", "BODY_NOT_OBJECT")

    payload = dict(raw_payload)
    params = payload.get("params") if isinstance(payload.get("params"), dict) else {}
    merged = {k: v for k, v in payload.items() if k != "params" and v is not None}
    merged.update({k: v for k, v in params.items() if v is not None})
    try:
        request = BoschKeyStationRequest.model_validate(merged)
        task_id = request.taskId or _text(payload.get("taskId"))
        output_root = STORAGE_ROOT / "bosch_key_station" / (task_id or run_id("bosch_key"))

        adj_path = request.adjPath or request.adj_path
        freq_path = request.freqPath or request.freq_path
        kqc_output_dir = request.kqcOutputDir or request.kqc_output_dir
        if kqc_output_dir:
            adj_path = adj_path or str(Path(kqc_output_dir) / "bosch_adj_matrix_selected.csv")
            freq_path = freq_path or str(Path(kqc_output_dir) / "bosch_edge_frequency.csv")

        if not adj_path or not freq_path:
            return _failed_algorithm_response(
                task_id,
                "KEY_PROCESS_IDENTIFY",
                "KQC output directory or adjPath/freqPath cannot be empty",
                "KEY_PROCESS_KQC_INPUT_EMPTY",
            )

        result = run_bosch_key_station(
            adj_path=adj_path,
            freq_path=freq_path,
            output_dir=output_root / "key_station",
            task_id=task_id,
            keep_freq=request.keepFreq,
            weight_thresh=request.weightThresh,
            top_n=request.topN,
        )
        return _algorithm_response(result)
    except Exception as exc:
        return _failed_algorithm_response(_text(raw_payload.get("taskId")), "KEY_PROCESS_IDENTIFY", str(exc), "KEY_PROCESS_IDENTIFY_FAILED")


# ============================================================
# 单工序逐点异常检测 — MemAE + ECDF + EWMA + UCL
# POST /algorithm/single-process-anomaly/execute
# ============================================================
@app.post("/algorithm/single-process-anomaly/execute")
async def single_process_anomaly_execute(http_request: Request):
    """Single-process pointwise anomaly detection using MemAE."""
    try:
        raw_payload = await http_request.json()
    except Exception:
        return _single_process_error(None, "request body must be valid JSON", "INVALID_JSON")

    if not isinstance(raw_payload, dict):
        return _single_process_error(None, "request body must be a JSON object", "BODY_NOT_OBJECT")

    # --- 从 Spring Boot 格式中提取参数 ---
    task_id = _text(raw_payload.get("taskId"))
    dataset_id = _text(raw_payload.get("processId") or raw_payload.get("targetId") or task_id)
    task_type = "SINGLE_PROCESS_ANOMALY"

    # 训练文件路径
    train_info = raw_payload.get("trainFileInfo") or {}
    train_path = _text(train_info.get("fileUrl"))

    # 检测文件路径（优先 detectFileInfo，备选 fileInfo）
    detect_info = raw_payload.get("detectFileInfo") or {}
    test_path = _text(detect_info.get("fileUrl"))
    if not test_path:
        file_info = raw_payload.get("fileInfo") or {}
        test_path = _text(file_info.get("fileUrl"))

    # --- 参数校验 ---
    if not train_path:
        return _single_process_error(task_id, "trainFileInfo.fileUrl cannot be empty",
                                      "TRAIN_PATH_EMPTY", dataset_id=dataset_id)
    if not test_path:
        return _single_process_error(task_id, "detectFileInfo.fileUrl or fileInfo.fileUrl cannot be empty",
                                      "TEST_PATH_EMPTY", dataset_id=dataset_id)
    if not Path(train_path).exists():
        return _single_process_error(task_id, f"training data path not found: {train_path}",
                                      "TRAIN_PATH_NOT_FOUND", dataset_id=dataset_id)
    if not Path(test_path).exists():
        return _single_process_error(task_id, f"test data path not found: {test_path}",
                                      "TEST_PATH_NOT_FOUND", dataset_id=dataset_id)

    output_dir = STORAGE_ROOT / "single_process_anomaly" / (dataset_id or task_id or "default")

    try:
        result = run_single_process_anomaly(
            train_path=train_path,
            test_path=test_path,
            output_dir=output_dir,
            task_id=task_id,
            dataset_id=dataset_id,
            task_type=task_type,
        )
        result["sourceTaskId"] = task_id
        return result
    except Exception as exc:
        return _single_process_error(
            task_id, str(exc), "SINGLE_PROCESS_ANOMALY_FAILED",
            dataset_id=dataset_id, source_task_id=task_id,
        )


def _single_process_error(
    task_id: Optional[str],
    message: str,
    error_code: str,
    task_type: str = "SINGLE_PROCESS_ANOMALY",
    dataset_id: Optional[str] = None,
    source_task_id: Optional[str] = None,
) -> Dict[str, Any]:
    return {
        "taskId": task_id,
        "taskType": task_type,
        "sourceTaskId": source_task_id,
        "datasetId": dataset_id,
        "status": "FAILED",
        "message": message,
        "errorCode": error_code,
    }


def _algorithm_response(result: Dict[str, Any]) -> Dict[str, Any]:
    task_id = result.get("taskId")
    task_type = result.get("taskType")
    data = {
        "taskId": task_id,
        "status": result.get("status", "SUCCESS"),
        "result": result,
        "logs": result.get("logs", []),
    }
    return {
        "success": True,
        "code": 0,
        "message": result.get("message", "success"),
        "taskId": task_id,
        "taskType": task_type,
        "status": result.get("status", "SUCCESS"),
        "data": data,
        "payload": result,
    }


def _failed_algorithm_response(task_id: Optional[str], task_type: str, message: str, error_code: str) -> Dict[str, Any]:
    result = {
        "taskId": task_id,
        "taskType": task_type,
        "status": "FAILED",
        "message": message,
        "errorCode": error_code,
    }
    return {
        "success": False,
        "code": 500,
        "message": message,
        "taskId": task_id,
        "taskType": task_type,
        "status": "FAILED",
        "data": {"taskId": task_id, "status": "FAILED", "result": result, "logs": []},
        "payload": result,
    }


def _run_algorithm_task(task_id: str, algorithm_type: str, payload: Dict[str, Any]) -> Dict[str, Any]:
    data = dict(payload or {})
    data["taskId"] = data.get("taskId") or task_id
    if algorithm_type == "PROCESS_ANOMALY":
        return process_anomaly_execute(ProcessAnomalyRequest.model_validate(data))
    if algorithm_type == "KQC_MINING":
        return bosch_kqc_mining_execute(BoschKqcRequest.model_validate(data))
    if algorithm_type == "KEY_PROCESS":
        return _key_process_identify_payload(data)
    raise ValueError(f"unsupported algorithmType: {algorithm_type}")


def _key_process_identify_payload(payload: Dict[str, Any]) -> Dict[str, Any]:
    params = payload.get("params") if isinstance(payload.get("params"), dict) else {}
    merged = {k: v for k, v in payload.items() if k != "params" and v is not None}
    merged.update({k: v for k, v in params.items() if v is not None})
    request = BoschKeyStationRequest.model_validate(merged)
    task_id = request.taskId or _text(payload.get("taskId"))
    output_root = STORAGE_ROOT / "bosch_key_station" / (task_id or run_id("bosch_key"))

    adj_path = request.adjPath or request.adj_path
    freq_path = request.freqPath or request.freq_path
    kqc_output_dir = request.kqcOutputDir or request.kqc_output_dir
    if kqc_output_dir:
        adj_path = adj_path or str(Path(kqc_output_dir) / "bosch_adj_matrix_selected.csv")
        freq_path = freq_path or str(Path(kqc_output_dir) / "bosch_edge_frequency.csv")

    if not adj_path or not freq_path:
        return _failed_algorithm_response(
            task_id,
            "KEY_PROCESS_IDENTIFY",
            "KQC output directory or adjPath/freqPath cannot be empty",
            "KEY_PROCESS_KQC_INPUT_EMPTY",
        )

    result = run_bosch_key_station(
        adj_path=adj_path,
        freq_path=freq_path,
        output_dir=output_root / "key_station",
        task_id=task_id,
        keep_freq=request.keepFreq,
        weight_thresh=request.weightThresh,
        top_n=request.topN,
    )
    return _algorithm_response(result)
