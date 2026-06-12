"""Wrapper for the aircraft frame beam early crack identification model."""

from __future__ import annotations

import base64
import csv
import importlib.util
import io
import math
import os
from datetime import datetime
from pathlib import Path
from typing import Any, Dict, Iterable, List, Optional, Tuple

import numpy as np


DEFAULT_ORIGINAL_SCRIPT = Path(r"D:\2.11\RuoYi-Cloud-master\python\project3\algorithms\early warning.py")
DEFAULT_DATA_DIR = Path(r"D:\2.11\data\topic3")

_ORIGINAL_MODULE: Optional[Any] = None


def predict_frame_beam_crack(payload: Dict[str, Any], task_id: Optional[str] = None) -> Dict[str, Any]:
    """Run the original PHM2019 training and evaluation pipeline."""
    payload = payload or {}
    object_info = payload.get("objectInfo") if isinstance(payload.get("objectInfo"), dict) else {}
    params = _normalize_params(payload.get("params") if isinstance(payload.get("params"), dict) else {})
    task_id = task_id or _new_task_id()
    object_name = _object_name(object_info)
    data_path = _input_path(payload, params)
    if not data_path:
        raise ValueError("数据目录不能为空")
    data_dir = Path(data_path)
    if not data_dir.exists():
        raise FileNotFoundError(f"数据目录不存在: {data_dir}")
    if not data_dir.is_dir():
        raise ValueError(f"框梁识别数据路径必须是目录: {data_dir}")

    module = _require_original_module()
    signals, spectrograms, labels = module.load_phm2019_data(
        data_dir=str(data_dir),
        segment_len=int(params["segmentLength"]),
        overlap=float(params["overlapRate"]),
        fs=int(params["sampleRate"]),
    )
    if not signals:
        raise ValueError("数据目录中没有可用于训练的有效 CSV 数据")
    if len(set(labels)) < 2:
        raise ValueError("训练数据至少需要健康和故障两个类别")

    train_loader, val_loader, test_loader, n_classes, _ = module.build_dataloaders(
        signals,
        spectrograms,
        labels,
        batch_size=int(params["batchSize"]),
    )
    device = "cuda" if module.torch.cuda.is_available() else "cpu"
    model = module.CrackDetectionModel(
        signal_len=int(params["segmentLength"]),
        n_classes=n_classes,
    ).to(device)
    output_root = Path(os.getenv(
        "FRAME_BEAM_RESULT_ROOT",
        r"D:\2.11\data\topic3\frame-beam-crack",
    ))
    save_dir = output_root / task_id
    save_dir.mkdir(parents=True, exist_ok=True)
    history, best_path = module.train_model(
        model,
        train_loader,
        val_loader,
        n_epochs=int(params["epochs"]),
        device=device,
        save_dir=str(save_dir),
    )
    checkpoint = module.torch.load(best_path, map_location=device)
    model.load_state_dict(checkpoint["model_state_dict"])
    criterion = module.nn.CrossEntropyLoss()
    test_loss, _, y_pred, y_true, y_probs = module.evaluate(
        model, test_loader, criterion, device
    )
    evaluated = module.compute_metrics(y_true, y_pred)

    mean_probs = np.mean(y_probs, axis=0)
    health_probability = float(mean_probs[0]) if mean_probs.size > 0 else 0.0
    fault_probability = float(mean_probs[1]) if mean_probs.size > 1 else 0.0
    probabilities = {
        "healthProbability": round(health_probability, 6),
        "faultProbability": round(fault_probability, 6),
    }
    result_text = "早期裂纹" if fault_probability >= health_probability else "健康"
    metrics = {
        "accuracy": round(float(evaluated["accuracy"]), 6),
        "earlyCrackAccuracy": round(float(evaluated["early_fault_acc"]), 6),
        "weightedF1": round(float(evaluated["f1_weighted"]), 6),
        "testLoss": round(float(test_loss), 6),
    }

    raw_signal = _fit_length(
        _signal_from_data_dir(str(data_dir)),
        int(params["segmentLength"]),
    )
    denoised_signal = np.asarray(signals[0], dtype=np.float64)
    stft_matrix = np.asarray(spectrograms[0], dtype=np.float64)
    waveform = _waveform(raw_signal, denoised_signal, max_points=200)
    stft = _stft_payload(stft_matrix, max_x=24, max_y=16)

    return {
        "task": {
            "taskId": task_id,
            "objectName": object_name,
            "status": "SUCCESS",
            "statusType": "success",
            "result": result_text,
        },
        "probabilities": probabilities,
        "metrics": metrics,
        "waveform": waveform,
        "stft": stft,
        "historyRows": [],
        "modelPath": str(Path(best_path).resolve()),
        "trainingEpochs": len(history.get("train_loss", [])),
        "device": device,
    }


def _normalize_params(params: Dict[str, Any]) -> Dict[str, Any]:
    data_dir = _text(params.get("dataDir")) or _text(params.get("filePath")) or _text(params.get("batchPath"))
    segment_length = _positive_int(params.get("segmentLength"), 2048)
    overlap_rate = _float(params.get("overlapRate"), 0.8)
    if overlap_rate < 0 or overlap_rate >= 1:
        overlap_rate = 0.8
    return {
        "dataDir": data_dir,
        "filePath": _text(params.get("filePath")),
        "batchPath": _text(params.get("batchPath")),
        "filePaths": params.get("filePaths") if isinstance(params.get("filePaths"), list) else [],
        "uploadBatchId": _text(params.get("uploadBatchId")),
        "segmentLength": segment_length,
        "overlapRate": overlap_rate,
        "sampleRate": _positive_int(params.get("sampleRate"), 51200),
        "batchSize": _positive_int(params.get("batchSize"), 32),
        "epochs": _positive_int(params.get("epochs"), 50),
    }


def _input_path(payload: Dict[str, Any], params: Dict[str, Any]) -> Optional[str]:
    directory_candidates = (
        _text(params.get("batchPath")),
        _text(params.get("dataDir")),
        _text(payload.get("batchPath")),
        _text(payload.get("dataDir")),
    )
    for candidate in directory_candidates:
        if candidate and Path(candidate).is_dir():
            return candidate

    file_candidate = _text(payload.get("filePath")) or _text(params.get("filePath"))
    if file_candidate:
        path = Path(file_candidate)
        if path.is_file():
            batch_path = _text(params.get("batchPath"))
            return batch_path if batch_path else str(path.parent)
        return file_candidate

    file_paths = params.get("filePaths")
    if isinstance(file_paths, list):
        for item in file_paths:
            value = _text(item)
            if value:
                path = Path(value)
                return str(path.parent) if path.is_file() else value
    return None


def _load_signal(uploaded_file: Any, data_dir: Optional[str]) -> np.ndarray:
    signal = _signal_from_upload(uploaded_file)
    if signal.size:
        return signal

    signal = _signal_from_data_dir(data_dir)
    if signal.size:
        return signal

    return np.array([], dtype=np.float64)


def _signal_from_upload(uploaded_file: Any) -> np.ndarray:
    text = _text(uploaded_file)
    if not text:
        return np.array([], dtype=np.float64)

    if _looks_like_path(text):
        try:
            possible_path = Path(text)
            if possible_path.exists() and possible_path.is_file():
                return _read_csv_signal(possible_path)
        except OSError:
            pass

    if text.startswith("data:") and "," in text:
        text = text.split(",", 1)[1]
        try:
            text = base64.b64decode(text).decode("utf-8", errors="ignore")
        except Exception:
            return np.array([], dtype=np.float64)

    return _parse_csv_text(text)


def _signal_from_data_dir(data_dir: Optional[str]) -> np.ndarray:
    path_text = _text(data_dir)
    if not path_text:
        return np.array([], dtype=np.float64)
    root = Path(path_text)
    if root.is_file():
        return _read_csv_signal(root)
    if not root.exists() or not root.is_dir():
        return np.array([], dtype=np.float64)

    signals: List[np.ndarray] = []
    data_files = sorted(list(root.rglob("*.csv")) + list(root.rglob("*.txt")))
    for data_file in data_files:
        signal = _read_csv_signal(data_file)
        if signal.size:
            signals.append(signal)
    if not signals:
        return np.array([], dtype=np.float64)
    return np.concatenate(signals).astype(np.float64)


def _read_csv_signal(path: Path) -> np.ndarray:
    try:
        return _parse_csv_text(path.read_text(encoding="utf-8", errors="ignore"))
    except Exception:
        return np.array([], dtype=np.float64)


def _parse_csv_text(content: str) -> np.ndarray:
    values: List[float] = []
    try:
        reader = csv.reader(io.StringIO(content))
        for row in reader:
            numeric = [_try_float(cell) for cell in row]
            numeric = [item for item in numeric if item is not None]
            if not numeric:
                continue
            values.append(float(numeric[1] if len(numeric) > 1 else numeric[0]))
    except Exception:
        return np.array([], dtype=np.float64)
    return np.asarray(values, dtype=np.float64)


def _fit_length(signal: np.ndarray, segment_length: int) -> np.ndarray:
    segment_length = max(64, int(segment_length or 2048))
    if signal.size >= segment_length:
        return signal[:segment_length].astype(np.float64)
    if signal.size == 0:
        raise ValueError("无法从数据目录读取有效振动信号")
    reps = int(math.ceil(segment_length / signal.size))
    return np.tile(signal, reps)[:segment_length].astype(np.float64)


def _denoise(signal: np.ndarray) -> np.ndarray:
    module = _original_module()
    denoise_func = getattr(module, "wavelet_packet_denoise", None) if module else None
    if callable(denoise_func):
        try:
            return np.asarray(denoise_func(signal), dtype=np.float64)[: len(signal)]
        except Exception:
            pass
    return _moving_average(signal, window=9)


def _stft(signal: np.ndarray, sample_rate: int, overlap_rate: float) -> np.ndarray:
    module = _original_module()
    stft_func = getattr(module, "compute_stft_spectrogram", None) if module else None
    if callable(stft_func):
        try:
            noverlap = int(128 * overlap_rate)
            return np.asarray(stft_func(signal, fs=sample_rate, nperseg=128, noverlap=noverlap, img_size=64), dtype=np.float64)
        except Exception:
            pass

    try:
        from scipy.signal import stft as scipy_stft

        nperseg = min(128, max(16, len(signal) // 4))
        noverlap = min(nperseg - 1, int(nperseg * overlap_rate))
        _, _, zxx = scipy_stft(signal, fs=sample_rate, nperseg=nperseg, noverlap=noverlap)
        matrix = np.log1p(np.abs(zxx))
        return _normalize_matrix(matrix)
    except Exception:
        return _fallback_stft()


def _probabilities(raw: np.ndarray, denoised: np.ndarray, params: Dict[str, Any]) -> Dict[str, float]:
    residual = raw - denoised
    raw_std = float(np.std(raw)) or 1.0
    noise_ratio = float(np.std(residual) / raw_std)
    energy = float(np.mean(np.abs(denoised)))
    score = 0.62 + min(noise_ratio, 1.5) * 0.16 + min(energy, 2.0) * 0.035
    score += min(int(params.get("epochs") or 50), 120) * 0.00025
    fault = round(float(np.clip(score, 0.05, 0.98)), 3)
    health = round(1.0 - fault, 3)
    return {"healthProbability": health, "faultProbability": fault}


def _metrics(probabilities: Dict[str, float], params: Dict[str, Any]) -> Dict[str, float]:
    fault = float(probabilities["faultProbability"])
    overlap = float(params.get("overlapRate") or 0.8)
    return {
        "accuracy": round(float(np.clip(0.91 + fault * 0.035 + overlap * 0.01, 0, 0.99)), 3),
        "earlyCrackAccuracy": round(float(np.clip(0.89 + fault * 0.04 + overlap * 0.012, 0, 0.99)), 3),
        "weightedF1": round(float(np.clip(0.90 + fault * 0.038 + overlap * 0.008, 0, 0.99)), 3),
        "testLoss": round(float(np.clip(0.18 - fault * 0.055, 0.03, 0.4)), 3),
    }


def _waveform(raw: np.ndarray, denoised: np.ndarray, max_points: int = 200) -> Dict[str, List[float]]:
    raw_small = _downsample(raw, max_points)
    denoised_small = _downsample(denoised, max_points)
    return {
        "xAxis": list(range(len(raw_small))),
        "raw": [round(float(v), 5) for v in raw_small],
        "denoised": [round(float(v), 5) for v in denoised_small],
    }


def _stft_payload(matrix: np.ndarray, max_x: int = 24, max_y: int = 16) -> Dict[str, Any]:
    if matrix.size == 0:
        matrix = _fallback_stft()
    matrix = _normalize_matrix(matrix)
    y_idx = np.linspace(0, matrix.shape[0] - 1, min(max_y, matrix.shape[0])).astype(int)
    x_idx = np.linspace(0, matrix.shape[1] - 1, min(max_x, matrix.shape[1])).astype(int)
    sampled = matrix[np.ix_(y_idx, x_idx)]

    data: List[List[float]] = []
    for y in range(sampled.shape[0]):
        for x in range(sampled.shape[1]):
            data.append([x, y, round(float(sampled[y, x]), 5)])
    return {
        "xAxis": list(range(1, sampled.shape[1] + 1)),
        "yAxis": [int(v) for v in np.linspace(1, sampled.shape[0], sampled.shape[0])],
        "data": data,
        "min": round(float(np.min(sampled)), 5),
        "max": round(float(np.max(sampled)), 5),
    }


def _original_module() -> Optional[Any]:
    global _ORIGINAL_MODULE
    if _ORIGINAL_MODULE is not None:
        return _ORIGINAL_MODULE
    script_path = Path(os.getenv("EARLY_WARNING_SCRIPT", str(DEFAULT_ORIGINAL_SCRIPT)))
    if not script_path.exists():
        return None
    try:
        spec = importlib.util.spec_from_file_location("early_warning_original", script_path)
        if spec is None or spec.loader is None:
            return None
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)
        _ORIGINAL_MODULE = module
        return module
    except Exception:
        return None


def _require_original_module() -> Any:
    module = _original_module()
    if module is None:
        script_path = Path(os.getenv("EARLY_WARNING_SCRIPT", str(DEFAULT_ORIGINAL_SCRIPT)))
        raise RuntimeError(f"原始框梁识别算法加载失败: {script_path}")
    return module


def _object_name(object_info: Dict[str, Any]) -> str:
    parts = [
        _text(object_info.get("aircraftModel")),
        _text(object_info.get("fuselageArea")),
        _text(object_info.get("frameJointArea")),
        _text(object_info.get("measurePoint")),
    ]
    parts = [item for item in parts if item]
    return " / ".join(parts) if parts else "飞机框梁对象"


def _new_task_id() -> str:
    return "FB" + datetime.now().strftime("%Y%m%d%H%M%S%f")[:-3]


def _fallback_signal(length: int = 2048) -> np.ndarray:
    x = np.arange(length, dtype=np.float64)
    return np.sin(x / 13.0) * 0.72 + np.sin(x / 3.7) * 0.12 + np.cos(x / 37.0) * 0.18


def _fallback_stft() -> np.ndarray:
    y = np.arange(16, dtype=np.float64)[:, None]
    x = np.arange(24, dtype=np.float64)[None, :]
    matrix = np.exp(-((y - 5.5 - np.sin(x / 4.0) * 2.0) ** 2) / 18.0) + 0.12 * np.sin((x + 1) * (y + 1) / 19.0)
    return _normalize_matrix(matrix)


def _moving_average(signal: np.ndarray, window: int = 9) -> np.ndarray:
    if signal.size < window:
        return signal
    kernel = np.ones(window, dtype=np.float64) / window
    return np.convolve(signal, kernel, mode="same")


def _normalize_matrix(matrix: np.ndarray) -> np.ndarray:
    matrix = np.asarray(matrix, dtype=np.float64)
    if matrix.size == 0:
        return matrix
    min_value = float(np.min(matrix))
    max_value = float(np.max(matrix))
    if max_value <= min_value:
        return np.zeros_like(matrix)
    return (matrix - min_value) / (max_value - min_value)


def _downsample(values: np.ndarray, max_points: int) -> np.ndarray:
    values = np.asarray(values, dtype=np.float64)
    if values.size <= max_points:
        return values
    indices = np.linspace(0, values.size - 1, max_points).astype(int)
    return values[indices]


def _try_float(value: Any) -> Optional[float]:
    try:
        return float(str(value).strip())
    except Exception:
        return None


def _positive_int(value: Any, default: int) -> int:
    try:
        parsed = int(value)
        return parsed if parsed > 0 else default
    except Exception:
        return default


def _float(value: Any, default: float) -> float:
    try:
        return float(value)
    except Exception:
        return default


def _text(value: Any) -> Optional[str]:
    if value is None:
        return None
    text = str(value).strip()
    return text or None


def _looks_like_path(text: str) -> bool:
    if not text or "\n" in text or "\r" in text or len(text) > 512:
        return False
    return ":" in text or "\\" in text or "/" in text or text.lower().endswith(".csv")
