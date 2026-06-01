"""FastAPI algorithm service for Spring Boot."""











from __future__ import annotations

import os
import shutil
import tempfile
from pathlib import Path
from typing import List, Optional

from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from pydantic import BaseModel, Field

from algorithms.data_upload_analysis import process_uploaded_dataset
from algorithms.feature_processing import extract_features_from_dataset
from algorithms.early_fault_detection import run_early_fault_detection
from algorithms.fault_prevention import run_fault_prevention
from algorithms.process_anomaly import run_process_anomaly


STORAGE_ROOT = Path(os.getenv("ALGORITHM_STORAGE_ROOT", "F:/TotalData/FaultIdentifyData/AlgorithmData")).resolve()
STORAGE_ROOT.mkdir(parents=True, exist_ok=True)

app = FastAPI(
    title="Vibration PHM Algorithm Service",
    description="Data analysis, feature processing, early degradation detection and fault prevention service",
    version="1.1.0",
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


class ProcessByPathRequest(BaseModel):
    datasetId: Optional[str] = None
    filePaths: List[str]
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


@app.get("/health")
def health():
    return {"status": "UP", "storageRoot": str(STORAGE_ROOT)}


@app.post("/python/data-analysis")
def python_data_analysis(request: UploadAnalyzeRequest):
    """Interface one: data analysis."""
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


@app.post("/algorithm/data/upload-analyze")
def upload_analyze(request: UploadAnalyzeRequest):
    """Compatibility upload-analyze route."""


    try:
        if not request.filePaths:
            raise HTTPException(status_code=400, detail="filePaths cannot be empty")

        return process_uploaded_dataset(
            dataset_id=request.datasetId or request.taskId,
            task_id=request.taskId,
            task_type=request.taskType,
            file_mode=request.fileMode,
            sample_ids=request.sampleIds,
            file_paths=request.filePaths,
            file_urls=request.fileUrls,
            batch_path=request.batchPath,
            storage_root=STORAGE_ROOT,
            sampling_frequency=request.samplingFrequency,
            column_index=request.columnIndex,
            window_size=request.windowSize,
            overlap_percent=request.overlapPercent,
            remove_outliers=request.removeOutliers,
            max_seconds=request.maxSeconds,
            save_original_files=True,
        )
    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(status_code=500, detail=str(exc)) from exc


@app.post("/algorithm/data/upload-multipart")
async def upload_multipart(
    files: List[UploadFile] = File(...),
    datasetId: Optional[str] = Form(default=None),
    samplingFrequency: int = Form(default=25600),
    columnIndex: int = Form(default=0),
    windowSize: float = Form(default=1.0),
    overlapPercent: float = Form(default=50.0),
    removeOutliers: bool = Form(default=True),
    maxSeconds: float = Form(default=5.0),
):
    """Multipart upload route."""


    if not files:
        raise HTTPException(status_code=400, detail="files cannot be empty")
    with tempfile.TemporaryDirectory() as tmp_dir:
        tmp_paths = []
        for upload in files:
            if not upload.filename.lower().endswith(".csv"):
                raise HTTPException(status_code=400, detail=f"only CSV files are supported: {upload.filename}")
            target = Path(tmp_dir) / Path(upload.filename).name
            with open(target, "wb") as f:
                shutil.copyfileobj(upload.file, f)
            tmp_paths.append(str(target))

        try:
            return process_uploaded_dataset(
                dataset_id=datasetId,
                file_paths=tmp_paths,
                storage_root=STORAGE_ROOT,
                sampling_frequency=samplingFrequency,
                column_index=columnIndex,
                window_size=windowSize,
                overlap_percent=overlapPercent,
                remove_outliers=removeOutliers,
                max_seconds=maxSeconds,
                save_original_files=True,
            )
        except Exception as exc:
            raise HTTPException(status_code=500, detail=str(exc)) from exc


@app.post("/algorithm/data/process-by-path")
def process_by_path(request: ProcessByPathRequest):
    """Process saved CSV paths."""

    try:
        return process_uploaded_dataset(
            dataset_id=request.datasetId,
            file_paths=request.filePaths,
            storage_root=STORAGE_ROOT,
            sampling_frequency=request.samplingFrequency,
            column_index=request.columnIndex,
            window_size=request.windowSize,
            overlap_percent=request.overlapPercent,
            remove_outliers=request.removeOutliers,
            max_seconds=request.maxSeconds,
            save_original_files=True,
        )
    except Exception as exc:
        raise HTTPException(status_code=500, detail=str(exc)) from exc


@app.post("/algorithm/features/extract")
def extract_features(request: FeatureExtractRequest):
    """Algorithm two: extract features from data_combined.npy."""
    try:
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
        raise HTTPException(status_code=500, detail=str(exc)) from exc


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


@app.post("/algorithm/detection/early-fault")
def early_fault_detection(request: EarlyFaultDetectionRequest):
    """Algorithm three: early degradation detection from featurePath."""
    return _run_early_fault_detection_request(request)


@app.post("/python/early-degradation-detect")
def python_early_degradation_detect(request: EarlyFaultDetectionRequest):
    """Interface three: early degradation detection."""
    return _run_early_fault_detection_request(request)


@app.post("/python/degradation-detect")
def python_degradation_detect(request: EarlyFaultDetectionRequest):
    """Interface three compatibility route."""
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


@app.post("/python/anomaly")
def python_process_anomaly(request: ProcessAnomalyRequest):
    """Process anomaly detection interface based on TEP PCA / DPCA statistics."""
    try:
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

@app.post("/algorithm/prevention/analyze")
@app.post("/python/fault-prevention")
@app.post("/python/prevention-analysis")
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
