"""
算法3：早期故障识别

支持：
- 峰度 + 3σ 准则
- RMS 趋势检测

优先使用算法2生成的 featureSet；也可通过 main.py 传 featurePath 调用。
"""

from __future__ import annotations

import json
import uuid
from pathlib import Path
from typing import Dict, List, Optional, Sequence

import numpy as np


def generate_detection_id(prefix: str = "det") -> str:
    return f"{prefix}_{uuid.uuid4().hex}"


def _feature_array(features: Sequence[Dict], key: str) -> np.ndarray:
    return np.array([float(item[key]) for item in features], dtype=float)


def _time_array(features: Sequence[Dict]) -> np.ndarray:
    return np.array([float(item["centerTime"]) for item in features], dtype=float)


def detect_by_kurtosis_features(
    features: Sequence[Dict],
    baseline_windows: int = 500,
) -> Dict:
    """
    峰度 + 3σ 检测。
    若连续两个窗口的峰度值超出基线 mu ± 3sigma，则认为出现退化。
    """
    times = _time_array(features)
    values = _feature_array(features, "kurtosis")

    if values.size < baseline_windows + 2:
        return {
            "detected": False,
            "degradationTime": None,
            "reason": "窗口数量不足，无法建立峰度基线",
            "baselineWindows": int(baseline_windows),
            "thresholdUpper": None,
            "thresholdLower": None,
            "times": times.tolist(),
            "values": values.tolist(),
        }

    baseline = values[:baseline_windows]
    mu = float(np.mean(baseline))
    sigma = float(np.std(baseline))
    threshold_upper = mu + 3 * sigma
    threshold_lower = mu - 3 * sigma

    degradation_idx = None
    for i in range(baseline_windows, values.size - 1):
        current_out = values[i] > threshold_upper or values[i] < threshold_lower
        next_out = values[i + 1] > threshold_upper or values[i + 1] < threshold_lower
        if current_out and next_out:
            degradation_idx = i
            break

    degradation_time = float(times[degradation_idx]) if degradation_idx is not None else None
    return {
        "detected": degradation_idx is not None,
        "degradationTime": degradation_time,
        "baselineMean": mu,
        "baselineStd": sigma,
        "baselineWindows": int(baseline_windows),
        "thresholdUpper": float(threshold_upper),
        "thresholdLower": float(threshold_lower),
        "times": times.tolist(),
        "values": values.tolist(),
    }


def detect_by_rms_features(
    features: Sequence[Dict],
    sensitivity: float = 0.2,
) -> Dict:
    """
    RMS 趋势检测。
    当前逻辑沿用原程序：前 max(10, 窗口数//5) 个窗口作为 RMS 基线。
    """
    times = _time_array(features)
    values = _feature_array(features, "rms")

    if values.size == 0:
        return {
            "detected": False,
            "degradationTime": None,
            "reason": "无 RMS 特征数据",
            "baselineRms": None,
            "threshold": None,
            "times": [],
            "values": [],
        }

    baseline_count = max(10, values.size // 5)
    baseline_count = min(baseline_count, values.size)
    baseline_rms = float(np.mean(values[:baseline_count]))
    threshold = baseline_rms * (1 + sensitivity)

    degradation_idx = None
    for i, value in enumerate(values):
        if value > threshold:
            degradation_idx = i
            break

    degradation_time = float(times[degradation_idx]) if degradation_idx is not None else None
    return {
        "detected": degradation_idx is not None,
        "degradationTime": degradation_time,
        "baselineRms": baseline_rms,
        "baselineCount": int(baseline_count),
        "sensitivity": float(sensitivity),
        "threshold": float(threshold),
        "times": times.tolist(),
        "values": values.tolist(),
    }


def fuse_degradation_time(results: Dict[str, Dict], strategy: str = "earliest") -> Optional[float]:
    times = [r.get("degradationTime") for r in results.values() if r.get("degradationTime") is not None]
    if not times:
        return None
    if strategy == "earliest":
        return float(min(times))
    if strategy == "latest":
        return float(max(times))
    return float(min(times))


def run_early_fault_detection(
    dataset_id: str,
    feature_set_id: str,
    feature_path: str | Path,
    output_dir: str | Path,
    methods: Sequence[str] = ("kurtosis_3sigma", "rms_trend"),
    baseline_windows: int = 500,
    rms_sensitivity: float = 0.2,
    fusion_strategy: str = "earliest",
    detection_id: Optional[str] = None,
) -> Dict:
    """算法3主入口：读取特征文件，运行早期故障识别，保存检测结果。"""
    detection_id = detection_id or generate_detection_id()

    with open(feature_path, "r", encoding="utf-8") as f:
        feature_result = json.load(f)
    features = feature_result.get("features", [])

    results: Dict[str, Dict] = {}
    if "kurtosis_3sigma" in methods or "kurtosis" in methods:
        results["kurtosis_3sigma"] = detect_by_kurtosis_features(
            features,
            baseline_windows=baseline_windows,
        )
    if "rms_trend" in methods or "rms" in methods:
        results["rms_trend"] = detect_by_rms_features(
            features,
            sensitivity=rms_sensitivity,
        )

    early_degradation_time = fuse_degradation_time(results, strategy=fusion_strategy)

    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    detection_path = output_dir / f"{detection_id}.json"

    result = {
        "taskId": None,
        "taskType": "EARLY_DEGRADATION_POINT_DETECT",
        "status": "SUCCESS",
        "message": "success",
        "detectionId": detection_id,
        "datasetId": dataset_id,
        "featureSetId": feature_set_id,
        "detectionPath": str(detection_path).replace("\\", "/"),
        "methods": list(methods),
        "fusionStrategy": fusion_strategy,
        "earlyDegradationPoint": early_degradation_time,
        "degradationPointUnit": "second",
        "earlyDegradationTime": early_degradation_time,
        "detected": early_degradation_time is not None,
        "results": results,
    }

    with open(detection_path, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False)

    return result
