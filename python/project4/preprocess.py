import json
import os
import sys
import numpy as np
import scipy.io as sio


def normalize_path(path):
    return path.replace("\\", "/")


def find_signal_key(mat_data, key_num=None, channel="DE"):
    keys = [k for k in mat_data.keys() if not k.startswith("__")]

    if key_num is not None:
        key_num_str = str(key_num)
        preferred = [k for k in keys if key_num_str in k and channel in k and "time" in k]
        if preferred:
            return preferred[0]

    preferred = [k for k in keys if channel in k and "time" in k]
    if preferred:
        return preferred[0]

    time_keys = [k for k in keys if "time" in k.lower()]
    if time_keys:
        return time_keys[0]

    raise RuntimeError("未找到 CWRU 信号字段，请检查 .mat 文件内容")


def sliding_window(signal, window_size=1024, stride=512, max_windows=12):
    signal = np.asarray(signal).reshape(-1)

    samples = []
    starts = []

    for start in range(0, len(signal) - window_size + 1, stride):
        samples.append(signal[start:start + window_size])
        starts.append(start)

        if len(samples) >= max_windows:
            break

    if not samples:
        raise RuntimeError("信号长度不足，无法进行滑动窗口切分")

    return np.asarray(samples), starts


def run_preprocess(input_json_path, output_json_path):
    with open(input_json_path, "r", encoding="utf-8-sig") as f:
        params = json.load(f)

    file_path = normalize_path(params["filePath"])
    key_num = params.get("keyNum")
    window_size = int(params.get("windowSize", 1024))
    stride = int(params.get("stride", 512))
    max_windows = int(params.get("maxWindows", 12))
    dataset_code = params.get("datasetCode", "CWRU")
    sample_prefix = params.get("samplePrefix", "SAMPLE")
    label = int(params.get("label", 0))
    fault_type = params.get("faultType", "未知")
    output_dir = normalize_path(params.get("outputDir", "D:/topic4-output/preprocess"))

    if not os.path.exists(file_path):
        raise FileNotFoundError(f"CWRU 文件不存在：{file_path}")

    os.makedirs(output_dir, exist_ok=True)

    mat_data = sio.loadmat(file_path)

    de_key = find_signal_key(mat_data, key_num, "DE")
    de_signal = mat_data[de_key].reshape(-1)

    samples, starts = sliding_window(
        de_signal,
        window_size=window_size,
        stride=stride,
        max_windows=max_windows
    )

    sample_file_path = normalize_path(os.path.join(output_dir, f"{sample_prefix}_windows.npy"))
    np.save(sample_file_path, samples)

    records = []

    for index, start in enumerate(starts):
        sample_code = f"{sample_prefix}-{index + 1:03d}"

        records.append({
            "sampleCode": sample_code,
            "datasetCode": dataset_code,
            "sourceFile": os.path.basename(file_path),
            "filePath": file_path,
            "sampleFilePath": sample_file_path,
            "keyNum": key_num,
            "signalKey": de_key,
            "windowSize": window_size,
            "stride": stride,
            "startIndex": int(start),
            "endIndex": int(start + window_size),
            "label": label,
            "faultType": fault_type,
            "validity": "有效"
        })

    result = {
        "success": True,
        "datasetCode": dataset_code,
        "sourceFile": os.path.basename(file_path),
        "filePath": file_path,
        "sampleFilePath": sample_file_path,
        "keyNum": key_num,
        "signalKey": de_key,
        "windowSize": window_size,
        "stride": stride,
        "sampleCount": len(records),
        "records": records
    }

    with open(output_json_path, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=2)

    return result


if __name__ == "__main__":
    if len(sys.argv) < 3:
        raise RuntimeError("Usage: python preprocess.py preprocess_input.json preprocess_result.json")

    run_preprocess(sys.argv[1], sys.argv[2])