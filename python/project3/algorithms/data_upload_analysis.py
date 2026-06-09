"""
算法1：数据上传、数据清洗、时域分析、时频分析

面向 Spring Boot / REST 服务调用的纯算法模块。
不依赖 PyQt，不绘图，只返回 JSON 可序列化结果和保存后的 data_combined.npy 路径。
"""

from __future__ import annotations

import os
import json
import shutil
import uuid
from pathlib import Path
from typing import Dict, List, Optional, Sequence, Tuple
from datetime import datetime

import numpy as np
import pandas as pd
from scipy import signal


DEFAULT_SAMPLING_FREQUENCY = 25600


def ensure_dir(path: str | Path) -> Path:
    p = Path(path)
    p.mkdir(parents=True, exist_ok=True)
    return p


def generate_dataset_id(prefix: str = "ds") -> str:
    return f"{prefix}_{uuid.uuid4().hex}"


def validate_and_clean_data(data: np.ndarray, remove_outliers: bool = True) -> np.ndarray:
    """清洗单个 CSV 指定列数据：删除 NaN/Inf，可选 IQR 异常值过滤。"""
    data = np.asarray(data, dtype=float)
    data = data[np.isfinite(data)]

    if remove_outliers and data.size > 0:
        q1 = np.percentile(data, 25)
        q3 = np.percentile(data, 75)
        iqr = q3 - q1
        lower_bound = q1 - 1.5 * iqr
        upper_bound = q3 + 1.5 * iqr
        data = data[(data >= lower_bound) & (data <= upper_bound)]

    return data


def _numeric_file_sort_key(file_path: str | Path):
    stem = Path(file_path).stem
    try:
        return int(stem)
    except ValueError:
        return stem


def read_and_combine_csv_files(
    file_paths: Sequence[str | Path],
    remove_outliers: bool = True,
    column_index: int = 0,
    sample_ids: Optional[Sequence[int]] = None,
) -> Tuple[np.ndarray, List[Dict]]:
    """
    读取多个 CSV 文件，取 column_index 指定列，清洗后按文件名顺序拼接。

    返回：
    - data_combined: 合并后的 NumPy 一维数组
    - file_reports: 每个文件的处理报告
    """
    if column_index < 0:
        raise ValueError("columnIndex 必须大于等于 0")

    sorted_paths = [Path(p) for p in file_paths]
    vertical_data_list: List[np.ndarray] = []
    file_reports: List[Dict] = []

    for index, file_path in enumerate(sorted_paths):
        report = {
            "sampleId": sample_ids[index] if sample_ids is not None and index < len(sample_ids) else None,
            "fileName": file_path.name,
            "filePath": file_path.as_posix(),
            "status": "SKIPPED",
            "rawCount": 0,
            "validCount": 0,
            "columnIndex": int(column_index),
            "message": "",
        }
        try:
            if not file_path.exists():
                report["status"] = "ERROR"
                report["message"] = f"文件不存在: {file_path}"
                file_reports.append(report)
                continue

            df = pd.read_csv(file_path, header=None)
            report["rawCount"] = int(df.shape[0])

            if df.shape[1] <= column_index:
                report["message"] = f"CSV 列数不足：当前 {df.shape[1]} 列，无法读取 columnIndex={column_index}"
                file_reports.append(report)
                continue

            selected_data = pd.to_numeric(df.iloc[:, column_index], errors="coerce").dropna().values
            selected_data = validate_and_clean_data(selected_data, remove_outliers=remove_outliers)

            if selected_data.size == 0:
                report["message"] = "清洗后无有效数据"
                file_reports.append(report)
                continue

            vertical_data_list.append(selected_data)
            report["status"] = "SUCCESS"
            report["validCount"] = int(selected_data.size)
            report["message"] = "处理成功"
            report["message"] = "success" if report["status"] == "SUCCESS" else report["message"]
            file_reports.append(report)
        except Exception as exc:  # 保留单文件错误，不中断整个批次
            report["status"] = "ERROR"
            report["message"] = str(exc)
            file_reports.append(report)

    if not vertical_data_list:
        raise ValueError("没有读取到任何有效数据，请检查 CSV 文件路径、columnIndex 以及数据列是否为数值数据")

    return np.concatenate(vertical_data_list), file_reports


def build_time_domain_response(
    data: np.ndarray,
    sampling_frequency: int,
    max_points: int = 50000,
) -> Dict:
    """返回前端绘图用的时域降采样数据。"""
    total_samples = int(data.size)
    if total_samples == 0:
        return {"times": [], "values": [], "downsampled": False, "pointCount": 0}

    step = max(1, total_samples // max_points)
    indexes = np.arange(0, total_samples, step)
    times = indexes / sampling_frequency

    return {
        "times": times.astype(float).tolist(),
        "values": data[indexes].astype(float).tolist(),
        "downsampled": step > 1,
        "pointCount": int(indexes.size),
        "time": times.astype(float).tolist(),
        "amplitude": data[indexes].astype(float).tolist(),
        "downsampleStep": int(step),
    }


def build_time_frequency_response(
    data: np.ndarray,
    sampling_frequency: int,
    window_size: float = 1.0,
    overlap_percent: float = 50.0,
    max_seconds: float = 5.0,
    max_freq_bins: int = 256,
    max_time_bins: int = 256,
) -> Dict:
    """
    计算 STFT 时频分析结果。
    为避免 JSON 过大，对频率轴和时间轴做二次降采样。
    """
    if data.size == 0:
        return {
            "frequencies": [],
            "times": [],
            "amplitudes": [],
            "amplitudeMatrix": [],
            "windowSize": float(window_size),
            "overlapPercent": float(overlap_percent),
            "maxSeconds": float(max_seconds),
        }

    if sampling_frequency <= 0:
        raise ValueError("samplingFrequency 必须大于 0")
    if window_size <= 0:
        raise ValueError("windowSize 必须大于 0")
    if not (0 <= overlap_percent < 100):
        raise ValueError("overlapPercent 必须在 [0, 100) 范围内")
    if max_seconds <= 0:
        raise ValueError("maxSeconds 必须大于 0")

    nperseg = max(8, int(window_size * sampling_frequency))
    nperseg = min(nperseg, int(data.size))
    noverlap = int(nperseg * overlap_percent / 100.0)
    noverlap = min(max(0, noverlap), nperseg - 1)

    max_samples = min(int(data.size), int(max_seconds * sampling_frequency))
    data_to_analyze = data[:max_samples]

    f, t, zxx = signal.stft(
        data_to_analyze,
        fs=sampling_frequency,
        nperseg=nperseg,
        noverlap=noverlap,
    )
    amplitude = np.abs(zxx)

    freq_step = max(1, len(f) // max_freq_bins)
    time_step = max(1, len(t) // max_time_bins)

    f_ds = f[::freq_step]
    t_ds = t[::time_step]
    amp_ds = amplitude[::freq_step, ::time_step]

    return {
        "frequencies": f_ds.astype(float).tolist(),
        "times": t_ds.astype(float).tolist(),
        "amplitudes": amp_ds.astype(float).tolist(),
        "amplitudeMatrix": amp_ds.astype(float).tolist(),
        "windowSize": float(window_size),
        "overlapPercent": float(overlap_percent),
        "maxSeconds": float(max_seconds),
        "analyzedSeconds": float(max_samples / sampling_frequency),
        "frequencyDownsampleStep": int(freq_step),
        "timeDownsampleStep": int(time_step),
    }


def process_uploaded_dataset(
    dataset_id: Optional[str],
    file_paths: Sequence[str | Path],
    storage_root: str | Path,
    sampling_frequency: int = DEFAULT_SAMPLING_FREQUENCY,
    window_size: float = 1.0,
    overlap_percent: float = 50.0,
    remove_outliers: bool = True,
    save_original_files: bool = True,
    column_index: int = 0,
    max_seconds: float = 5.0,
    task_id: Optional[str] = None,
    task_type: Optional[str] = None,
    file_mode: Optional[str] = None,
    sample_ids: Optional[Sequence[int]] = None,
    file_urls: Optional[Sequence[str]] = None,
    batch_path: Optional[str] = None,
) -> Dict:
    """
    算法1主入口：CSV -> data_combined.npy -> 时域/时频 JSON 结果。

    dataset_id 优先级：dataset_id > task_id > 自动生成。
    Spring Boot 当前请求可直接把 taskId 作为 datasetId 使用。
    """
    dataset_id = dataset_id or task_id or generate_dataset_id()
    storage_root = Path(storage_root)
    dataset_dir = ensure_dir(storage_root / "datasets" / dataset_id)
    original_dir = ensure_dir(dataset_dir / "original")

    source_paths = [Path(p) for p in file_paths]
    if not source_paths:
        raise ValueError("filePaths 不能为空")

    if save_original_files:
        copied_paths = []
        for p in source_paths:
            target = original_dir / p.name
            if not p.exists():
                # 不在 copy 阶段抛错，交给 read_and_combine_csv_files 生成详细文件报告
                copied_paths.append(p)
                continue
            if p.resolve() != target.resolve():
                shutil.copy2(p, target)
            copied_paths.append(target)
        source_paths = copied_paths

    data_combined, file_reports = read_and_combine_csv_files(
        source_paths,
        remove_outliers=remove_outliers,
        column_index=column_index,
        sample_ids=sample_ids,
    )

    combined_data_path = dataset_dir / "data_combined.npy"
    np.save(combined_data_path, data_combined)

    total_samples = int(data_combined.size)
    total_time = float(total_samples / sampling_frequency)

    time_domain = build_time_domain_response(data_combined, sampling_frequency)
    time_frequency = build_time_frequency_response(
        data_combined,
        sampling_frequency=sampling_frequency,
        window_size=window_size,
        overlap_percent=overlap_percent,
        max_seconds=max_seconds,
    )

    combined_path_text = combined_data_path.as_posix()
    metadata_path = dataset_dir / "metadata.json"
    result = {
        "taskId": task_id,
        "datasetId": dataset_id,
        "taskType": task_type or "FEATURE_ANALYSIS",
        "status": "SUCCESS",
        "message": "success",
        "fileMode": file_mode,
        "sampleIds": list(sample_ids) if sample_ids is not None else None,
        "filePaths": [Path(p).as_posix() for p in file_paths],
        "fileUrls": list(file_urls) if file_urls is not None else None,
        "batchPath": batch_path,
        "combinedDataPath": combined_path_text,
        "metadataPath": metadata_path.as_posix(),
        "originalDataDir": original_dir.as_posix(),
        "samplingFrequency": int(sampling_frequency),
        "columnIndex": int(column_index),
        "removeOutliers": bool(remove_outliers),
        "totalSamples": total_samples,
        "totalTime": total_time,
        "fileReports": file_reports,
        "timeDomain": time_domain,
        "timeFrequency": time_frequency,
    }
    metadata = {
        "taskId": task_id,
        "datasetId": dataset_id,
        "taskType": task_type or "FEATURE_ANALYSIS",
        "fileMode": file_mode,
        "sampleIds": list(sample_ids) if sample_ids is not None else None,
        "filePaths": [Path(p).as_posix() for p in file_paths],
        "fileUrls": list(file_urls) if file_urls is not None else None,
        "batchPath": batch_path,
        "samplingFrequency": int(sampling_frequency),
        "columnIndex": int(column_index),
        "totalSamples": total_samples,
        "totalTime": total_time,
        "combinedDataPath": combined_path_text,
        "createdAt": datetime.now().isoformat(timespec="seconds"),
    }
    with open(metadata_path, "w", encoding="utf-8") as fp:
        json.dump(metadata, fp, ensure_ascii=False, indent=2)
    return result
