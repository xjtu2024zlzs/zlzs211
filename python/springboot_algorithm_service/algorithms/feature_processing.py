"""
算法2：特征处理

输入 data_combined.npy，输出滑动窗口统计特征。
"""

from __future__ import annotations

import json
import uuid
from pathlib import Path
from typing import Dict, List, Optional

import numpy as np
from scipy.stats import kurtosis


def generate_feature_set_id(prefix: str = "fs") -> str:
    return f"{prefix}_{uuid.uuid4().hex}"


def load_data_combined(combined_data_path: str | Path) -> np.ndarray:
    path = Path(combined_data_path)
    if not path.exists():
        raise FileNotFoundError(f"data_combined 文件不存在: {path}")
    data = np.load(path)
    if data.ndim != 1:
        data = np.ravel(data)
    return data.astype(float)


def extract_window_features(
    data: np.ndarray,
    sampling_frequency: int,
    window_size: float = 1.0,
    overlap_percent: int = 50,
) -> List[Dict]:
    """滑动窗口提取 mean/std/max/min/rms/kurtosis/peak_to_peak。"""
    data = np.asarray(data, dtype=float)
    window_samples = int(window_size * sampling_frequency)
    if window_samples <= 0:
        raise ValueError("window_size 过小，窗口样本数必须大于 0")
    if data.size < window_samples:
        raise ValueError("数据长度小于窗口长度，无法提取特征")

    step_samples = int(window_samples * (1 - overlap_percent / 100))
    if step_samples <= 0:
        step_samples = window_samples

    features: List[Dict] = []
    for start_idx in range(0, data.size - window_samples + 1, step_samples):
        end_idx = start_idx + window_samples
        window_data = data[start_idx:end_idx]

        start_time = start_idx / sampling_frequency
        end_time = end_idx / sampling_frequency
        center_time = (start_time + end_time) / 2

        max_val = float(np.max(window_data))
        min_val = float(np.min(window_data))

        features.append({
            "startTime": float(start_time),
            "endTime": float(end_time),
            "centerTime": float(center_time),
            "mean": float(np.mean(window_data)),
            "std": float(np.std(window_data)),
            "max": max_val,
            "min": min_val,
            "rms": float(np.sqrt(np.mean(window_data ** 2))),
            "kurtosis": float(kurtosis(window_data, fisher=True)),
            "peakToPeak": float(max_val - min_val),
        })

    return features


def extract_features_from_dataset(
    dataset_id: str,
    combined_data_path: str | Path,
    output_dir: str | Path,
    task_id: Optional[str] = None,
    task_type: str = "FEATURE_PROCESSING",
    source_task_id: Optional[str] = None,
    sampling_frequency: int = 25600,
    window_size: float = 1.0,
    overlap_percent: int = 50,
    feature_set_id: Optional[str] = None,
) -> Dict:
    """算法2主入口：读取 data_combined.npy，保存并返回特征结果。"""
    feature_set_id = feature_set_id or f"FS_{dataset_id}"
    data = load_data_combined(combined_data_path)
    features = extract_window_features(
        data,
        sampling_frequency=sampling_frequency,
        window_size=window_size,
        overlap_percent=overlap_percent,
    )

    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    feature_path = output_dir / "features.json"
    feature_series = {
        "time": [item["centerTime"] for item in features],
        "mean": [item["mean"] for item in features],
        "std": [item["std"] for item in features],
        "max": [item["max"] for item in features],
        "min": [item["min"] for item in features],
        "rms": [item["rms"] for item in features],
        "kurtosis": [item["kurtosis"] for item in features],
        "peakToPeak": [item["peakToPeak"] for item in features],
    }

    result = {
        "taskId": task_id,
        "taskType": task_type,
        "sourceTaskId": source_task_id,
        "status": "SUCCESS",
        "message": "success",
        "featureSetId": feature_set_id,
        "datasetId": dataset_id,
        "combinedDataPath": str(Path(combined_data_path)).replace("\\", "/"),
        "featurePath": str(feature_path).replace("\\", "/"),
        "samplingFrequency": int(sampling_frequency),
        "windowSize": float(window_size),
        "overlapPercent": int(overlap_percent),
        "windowCount": len(features),
        "features": features,
        "featureSeries": feature_series,
    }

    with open(feature_path, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False)

    return result


def load_feature_result(feature_path: str | Path) -> Dict:
    path = Path(feature_path)
    if not path.exists():
        raise FileNotFoundError(f"特征文件不存在: {path}")
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)
