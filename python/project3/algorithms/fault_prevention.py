from __future__ import annotations

import json
import uuid
from pathlib import Path
from typing import Dict, List, Optional, Sequence

import numpy as np
from scipy.stats import kurtosis

from .feature_processing import load_data_combined


def generate_prevention_id(prefix: str = "prev") -> str:
    return f"{prefix}_{uuid.uuid4().hex}"


def _norm_path(value: str | Path) -> str:
    return str(value).replace("\\", "/")


def _safe_rate(after_value: float, before_value: float) -> float:
    return float((after_value - before_value) / before_value) if before_value > 0 else 0.0


def calculate_segment_statistics(data: np.ndarray) -> Dict:
    if data.size == 0:
        raise ValueError("data segment is empty")
    return {
        "mean": float(np.mean(data)),
        "std": float(np.std(data)),
        "rms": float(np.sqrt(np.mean(data ** 2))),
        "kurtosis": float(kurtosis(data, fisher=True)),
        "max": float(np.max(data)),
        "min": float(np.min(data)),
        "peakToPeak": float(np.ptp(data)),
    }


def calculate_feature_statistics(features: Sequence[Dict], feature_name: str = "rms") -> Dict:
    if not features:
        raise ValueError("feature segment is empty")

    def values(name: str) -> np.ndarray:
        return np.asarray([float(item.get(name, 0) or 0) for item in features], dtype=float)

    selected = values(feature_name)
    return {
        "mean": float(np.mean(values("mean"))),
        "std": float(np.mean(values("std"))),
        "rms": float(np.mean(values("rms"))),
        "kurtosis": float(np.mean(values("kurtosis"))),
        "max": float(np.max(values("max"))),
        "min": float(np.min(values("min"))),
        "peakToPeak": float(np.mean(values("peakToPeak"))),
        "selectedFeature": feature_name,
        "selectedFeatureMean": float(np.mean(selected)),
        "selectedFeatureMax": float(np.max(selected)),
    }


def calculate_risk_score(before_stats: Dict, after_stats: Dict, risk_threshold: float = 0.8) -> Dict:
    rms_increase_rate = _safe_rate(after_stats["rms"], before_stats["rms"])
    std_increase_rate = _safe_rate(after_stats["std"], before_stats["std"])
    kurtosis_delta = after_stats["kurtosis"] - before_stats["kurtosis"]
    risk_score = abs(rms_increase_rate) * 50 + abs(std_increase_rate) * 30 + abs(kurtosis_delta) * 2

    high_line = max(30.0, float(risk_threshold) * 100)
    medium_line = max(10.0, high_line * 0.38)
    if risk_score >= high_line:
        risk_level = "高风险"
        risk_level_code = "HIGH"
    elif risk_score >= medium_line:
        risk_level = "中风险"
        risk_level_code = "MEDIUM"
    else:
        risk_level = "低风险"
        risk_level_code = "LOW"

    return {
        "riskScore": float(risk_score),
        "riskLevel": risk_level,
        "riskLevelCode": risk_level_code,
        "rmsIncreaseRate": float(rms_increase_rate),
        "stdIncreaseRate": float(std_increase_rate),
        "kurtosisDelta": float(kurtosis_delta),
    }


def predict_remaining_life(total_time: float, degradation_time: float, rms_increase_rate: float) -> float:
    remaining_time = max(0.0, total_time - degradation_time)
    if rms_increase_rate > 0.01:
        return float(remaining_time / (1 + rms_increase_rate))
    return float(remaining_time * 2)


def generate_maintenance_advice(
    risk_level_code: str,
    risk_score: float,
    rms_increase_rate: float,
    predicted_lifetime: float,
) -> List[str]:
    if risk_level_code == "HIGH":
        advice = [
            "建议立即安排停机检查，重点检查轴承、齿轮、连接件和液压泵关键部位。",
            "缩短巡检周期，并复核传感器安装状态和采集链路。",
            "如现场存在明显冲击、异响或温升，应优先执行预防性维修。",
        ]
    elif risk_level_code == "MEDIUM":
        advice = [
            "建议增加巡检频率，持续跟踪 RMS、标准差和峭度趋势。",
            "安排计划性检修窗口，检查润滑、紧固和传动状态。",
        ]
    else:
        advice = [
            "当前风险较低，建议保持常规巡检。",
            "继续积累健康基准数据，用于后续趋势对比。",
        ]

    if rms_increase_rate > 0.2:
        advice.append("RMS 增长明显，说明整体振动能量上升，应关注结构松动或负载异常。")
    if predicted_lifetime < 1:
        advice.append("预测剩余寿命较短，建议优先处理该设备。")
    advice.append(f"当前风险评分为 {risk_score:.2f}，建议结合现场工况和历史维修记录复核。")
    return advice


def _read_detection_time(detection_path: Optional[str | Path]) -> Optional[float]:
    if not detection_path:
        return None
    with open(detection_path, "r", encoding="utf-8") as f:
        detection_result = json.load(f)
    for key in ("earlyDegradationTime", "earlyDegradationPoint", "degradationTime"):
        value = detection_result.get(key)
        if value is not None:
            return float(value)
    return None


def _feature_total_time(features: Sequence[Dict]) -> float:
    if not features:
        return 0.0
    return float(max(float(item.get("endTime", item.get("centerTime", 0)) or 0) for item in features))


def _split_features(features: Sequence[Dict], degradation_time: float) -> tuple[List[Dict], List[Dict]]:
    before = [item for item in features if float(item.get("centerTime", 0) or 0) < degradation_time]
    after = [item for item in features if float(item.get("centerTime", 0) or 0) >= degradation_time]
    if not before or not after:
        raise ValueError("degradation time cannot split features into before and after segments")
    return before, after


def _series_from_features(features: Sequence[Dict], feature_name: str) -> Dict:
    return {
        "time": [float(item.get("centerTime", 0) or 0) for item in features],
        feature_name: [float(item.get(feature_name, 0) or 0) for item in features],
        "rms": [float(item.get("rms", 0) or 0) for item in features],
        "std": [float(item.get("std", 0) or 0) for item in features],
        "kurtosis": [float(item.get("kurtosis", 0) or 0) for item in features],
    }


def _linear_forecast(
    times: Sequence[float],
    values: Sequence[float],
    prediction_horizon: float,
    max_points: int = 200,
) -> Dict:
    if not times or not values or prediction_horizon <= 0:
        return {"predictionTime": [], "predictedValues": []}

    x = np.asarray(times, dtype=float)
    y = np.asarray(values, dtype=float)
    size = min(x.size, y.size)
    x = x[:size]
    y = y[:size]
    valid = np.isfinite(x) & np.isfinite(y)
    x = x[valid]
    y = y[valid]
    if x.size == 0:
        return {"predictionTime": [], "predictedValues": []}

    recent_count = min(20, x.size)
    recent_x = x[-recent_count:]
    recent_y = y[-recent_count:]
    if recent_count >= 2 and np.ptp(recent_x) > 0:
        slope, intercept = np.polyfit(recent_x, recent_y, 1)
    else:
        slope, intercept = 0.0, float(recent_y[-1])

    positive_steps = np.diff(x)
    positive_steps = positive_steps[positive_steps > 0]
    step = float(np.median(positive_steps)) if positive_steps.size else 1.0
    point_count = max(2, min(max_points, int(np.ceil(prediction_horizon / step))))
    future_times = np.linspace(float(x[-1]) + step, float(x[-1]) + prediction_horizon, point_count)
    forecast = np.maximum(0.0, slope * future_times + intercept)
    return {
        "predictionTime": future_times.tolist(),
        "predictedValues": forecast.tolist(),
    }


def _downsample_signal(data: np.ndarray, sampling_frequency: int, max_points: int = 1200) -> Dict:
    signal = np.asarray(data, dtype=float)
    if signal.size == 0:
        return {"time": [], "amplitude": []}
    indexes = np.linspace(0, signal.size - 1, min(max_points, signal.size), dtype=int)
    return {
        "time": (indexes / float(sampling_frequency)).tolist(),
        "amplitude": signal[indexes].tolist(),
    }


def _forecast_signal(
    data: np.ndarray,
    sampling_frequency: int,
    prediction_horizon: float,
    rms_times: Sequence[float],
    rms_values: Sequence[float],
    max_points: int = 600,
) -> Dict:
    signal = np.asarray(data, dtype=float)
    if signal.size == 0 or prediction_horizon <= 0:
        return {"predictionTime": [], "predictedValues": []}

    point_count = max(2, min(max_points, int(np.ceil(prediction_horizon * 20))))
    template_size = min(signal.size, max(64, min(2048, sampling_frequency)))
    template = signal[-template_size:].copy()
    template -= float(np.mean(template))
    template_rms = float(np.sqrt(np.mean(template ** 2)))
    if template_rms <= 0:
        template_rms = 1.0

    rms_forecast = _linear_forecast(rms_times, rms_values, prediction_horizon, max_points=point_count)
    target_rms = np.asarray(rms_forecast["predictedValues"], dtype=float)
    if target_rms.size != point_count:
        base_rms = float(np.sqrt(np.mean(signal ** 2)))
        target_rms = np.full(point_count, base_rms, dtype=float)

    template_positions = np.linspace(0, template_size - 1, point_count)
    waveform = np.interp(template_positions, np.arange(template_size), template)
    predicted = waveform / template_rms * target_rms
    start_time = signal.size / float(sampling_frequency)
    future_times = np.linspace(
        start_time + prediction_horizon / point_count,
        start_time + prediction_horizon,
        point_count,
    )
    return {
        "predictionTime": future_times.tolist(),
        "predictedValues": predicted.tolist(),
    }


def _load_features(feature_path: str | Path) -> Dict:
    with open(feature_path, "r", encoding="utf-8") as f:
        feature_result = json.load(f)
    features = feature_result.get("features") or []
    if not features:
        raise ValueError("featurePath does not contain features")
    return feature_result


def run_fault_prevention(
    dataset_id: str,
    output_dir: str | Path,
    feature_path: Optional[str | Path] = None,
    combined_data_path: Optional[str | Path] = None,
    sampling_frequency: int = 25600,
    degradation_time: Optional[float] = None,
    detection_path: Optional[str | Path] = None,
    prevention_id: Optional[str] = None,
    task_id: Optional[str] = None,
    task_type: str = "FAULT_PREDICT",
    source_task_id: Optional[str] = None,
    feature_set_id: Optional[str] = None,
    detection_id: Optional[str] = None,
    risk_threshold: float = 0.8,
    rul_unit: str = "second",
    feature_name: str = "rms",
    prediction_horizon: float = 50,
) -> Dict:
    prevention_id = prevention_id or generate_prevention_id()
    degradation_time = degradation_time if degradation_time is not None else _read_detection_time(detection_path)
    if degradation_time is None:
        raise ValueError("missing degradation time; please run early degradation detection first")

    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    prevention_path = output_dir / f"{prevention_id}.json"

    if feature_path:
        feature_result = _load_features(feature_path)
        features = feature_result.get("features") or []
        before_features, after_features = _split_features(features, float(degradation_time))
        before_stats = calculate_feature_statistics(before_features, feature_name=feature_name)
        after_stats = calculate_feature_statistics(after_features, feature_name=feature_name)
        total_time = _feature_total_time(features)
        prediction_series = _series_from_features(features, feature_name)
        feature_set_id = feature_set_id or feature_result.get("featureSetId")
        combined_data_path = combined_data_path or feature_result.get("combinedDataPath")
    else:
        if not combined_data_path:
            raise ValueError("missing featurePath or combinedDataPath")
        data = load_data_combined(combined_data_path)
        total_time = float(data.size / sampling_frequency)
        degradation_index = int(float(degradation_time) * sampling_frequency)
        if degradation_index <= 0 or degradation_index >= data.size:
            raise ValueError("degradation time is out of data range")
        before_stats = calculate_segment_statistics(data[:degradation_index])
        after_stats = calculate_segment_statistics(data[degradation_index:])
        prediction_series = {}

    prediction_horizon = max(0.0, float(prediction_horizon or 0))
    rms_prediction = _linear_forecast(
        prediction_series.get("time", []),
        prediction_series.get("rms", []),
        prediction_horizon,
    )
    rms_trend_prediction = {
        "time": prediction_series.get("time", []),
        "rms": prediction_series.get("rms", []),
        **rms_prediction,
    }

    vibration_signal_prediction = {}
    if combined_data_path and Path(combined_data_path).exists():
        vibration_data = load_data_combined(combined_data_path)
        vibration_signal_prediction = {
            **_downsample_signal(vibration_data, sampling_frequency),
            **_forecast_signal(
                vibration_data,
                sampling_frequency,
                prediction_horizon,
                prediction_series.get("time", []),
                prediction_series.get("rms", []),
            ),
        }

    risk = calculate_risk_score(before_stats, after_stats, risk_threshold=risk_threshold)
    predicted_lifetime = predict_remaining_life(
        total_time=total_time,
        degradation_time=float(degradation_time),
        rms_increase_rate=risk["rmsIncreaseRate"],
    )
    advice = generate_maintenance_advice(
        risk_level_code=risk["riskLevelCode"],
        risk_score=risk["riskScore"],
        rms_increase_rate=risk["rmsIncreaseRate"],
        predicted_lifetime=predicted_lifetime,
    )

    result = {
        "taskId": task_id,
        "taskType": task_type,
        "sourceTaskId": source_task_id,
        "status": "SUCCESS",
        "message": "success",
        "preventionId": prevention_id,
        "datasetId": dataset_id,
        "featureSetId": feature_set_id,
        "featurePath": _norm_path(feature_path) if feature_path else None,
        "combinedDataPath": _norm_path(combined_data_path) if combined_data_path else None,
        "detectionId": detection_id,
        "detectionPath": _norm_path(detection_path) if detection_path else None,
        "preventionPath": _norm_path(prevention_path),
        "earlyDegradationTime": float(degradation_time),
        "degradationTime": float(degradation_time),
        "samplingFrequency": int(sampling_frequency),
        "totalTime": float(total_time),
        "rulUnit": rul_unit,
        "featureName": feature_name,
        "beforeDegradation": before_stats,
        "afterDegradation": after_stats,
        "increaseRate": {
            "rms": risk["rmsIncreaseRate"],
            "std": risk["stdIncreaseRate"],
            "kurtosisDelta": risk["kurtosisDelta"],
        },
        "riskScore": risk["riskScore"],
        "riskLevel": risk["riskLevel"],
        "riskLevelCode": risk["riskLevelCode"],
        "predictedRemainingLife": predicted_lifetime,
        "remainingLife": predicted_lifetime,
        "maintenanceAdvice": advice,
        "predictionSeries": prediction_series,
        "rmsTrendPrediction": rms_trend_prediction,
        "vibrationSignalPrediction": vibration_signal_prediction,
    }

    with open(prevention_path, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False)

    return result
