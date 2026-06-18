# -*- coding: utf-8 -*-
"""
课题四：样本增强算法脚本
用法：
    python augment.py input.json result.json

input.json 示例：
{
  "filePath": "D:/topic4-data/CWRU/12k Drive End Bearing Fault Data/108.mat",
  "keyNum": 108,
  "sampleCodePrefix": "AUG-TEST",
  "length": 1024,
  "label": 1,
  "augmentRatio": 2,
  "randomAug": true,
  "outputDir": "D:/topic4-output/augmented/P-TEST"
}
"""

import json
import os
import random
import sys
from typing import Dict, Optional, Tuple

import numpy as np
import scipy.io as scio
from sklearn import preprocessing

random.seed(0)
np.random.seed(0)


def _find_key(mat: Dict, suffix: str, key_num: Optional[int] = None) -> str:
    if key_num is not None:
        expected_key = "X" + "%03d" % int(key_num) + suffix
        if expected_key in mat:
            return expected_key
    candidates = [k for k in mat.keys() if k.endswith(suffix)]
    if not candidates:
        raise ValueError(f"MAT 文件中未找到 *{suffix} 通道，可用键为：{list(mat.keys())[:20]}")
    return candidates[0]


def open_data(file_path: str, key_num: Optional[int]) -> Tuple[np.ndarray, np.ndarray, Dict]:
    if not os.path.exists(file_path):
        raise FileNotFoundError(f"CWRU 文件不存在：{file_path}")
    data = scio.loadmat(file_path)
    de_key = _find_key(data, "_DE_time", key_num)
    fe_key = _find_key(data, "_FE_time", key_num)
    return data[de_key], data[fe_key], {"deKey": de_key, "feKey": fe_key}


def deal_data(data1: np.ndarray, data2: np.ndarray, length: int, label: int) -> np.ndarray:
    data1 = np.reshape(data1, (-1))
    num1 = len(data1) // length
    data1 = data1[0:num1 * length]
    data1 = np.reshape(data1, (num1, length))
    scaler1 = preprocessing.MinMaxScaler()
    data1 = scaler1.fit_transform(np.transpose(data1, [1, 0]))
    data1 = np.transpose(data1, [1, 0])

    data2 = np.reshape(data2, (-1))
    num2 = len(data2) // length
    data2 = data2[0:num2 * length]
    data2 = np.reshape(data2, (num2, length))
    scaler2 = preprocessing.MinMaxScaler()
    data2 = scaler2.fit_transform(np.transpose(data2, [1, 0]))
    data2 = np.transpose(data2, [1, 0])

    num = min(num1, num2)
    data1 = data1[:num]
    data2 = data2[:num]
    labels = np.ones((num, 1)) * label
    return np.column_stack((data1, data2, labels))


def signal_augment(signal: np.ndarray, aug_type: Optional[str] = None) -> np.ndarray:
    length = len(signal)
    if aug_type is None:
        aug_type = random.choice(["noise", "scale", "flip", "shift", "mask"])

    if aug_type == "noise":
        noise = np.random.normal(loc=0, scale=0.02, size=length)
        return signal + noise
    if aug_type == "scale":
        scale_factor = np.random.uniform(0.8, 1.2)
        return signal * scale_factor
    if aug_type == "flip":
        return np.flip(signal)
    if aug_type == "shift":
        shift_len = np.random.randint(10, max(11, length // 4))
        return np.roll(signal, shift_len)
    if aug_type == "mask":
        mask_ratio = np.random.uniform(0.05, 0.15)
        mask_num = int(length * mask_ratio)
        mask_idx = np.random.choice(length, mask_num, replace=False)
        aug_sig = signal.copy()
        aug_sig[mask_idx] = 0
        return aug_sig
    raise ValueError(f"未知增强方式：{aug_type}")


def batch_augment(data_batch: np.ndarray, aug_num: int = 1, random_aug: bool = True) -> np.ndarray:
    _, total_dim = data_batch.shape
    length = (total_dim - 1) // 2
    aug_list = [data_batch]
    aug_methods = ["noise", "scale", "flip", "shift", "mask"]

    for i in range(aug_num):
        aug_samples = []
        for row in data_batch:
            de_sig = row[:length]
            fe_sig = row[length: 2 * length]
            label = row[-1]
            method = None if random_aug else aug_methods[i % len(aug_methods)]
            de_aug = signal_augment(de_sig, method)
            fe_aug = signal_augment(fe_sig, method)
            aug_samples.append(np.concatenate([de_aug, fe_aug, [label]]))
        aug_list.append(np.array(aug_samples))
    return np.vstack(aug_list)


def run_augment(input_json_path: str, output_json_path: str) -> None:
    with open(input_json_path, "r", encoding="utf-8") as f:
        req = json.load(f)

    file_path = req["filePath"]
    key_num = req.get("keyNum")
    length = int(req.get("length", 1024))
    label = int(req.get("label", 1))
    augment_ratio = int(req.get("augmentRatio", 1))
    random_aug = bool(req.get("randomAug", True))
    output_dir = req.get("outputDir", os.path.join(os.path.dirname(output_json_path), "augmented"))
    # 统一路径分隔符，避免 Windows 下出现 D:/xxx\xxx 的混合路径
    output_dir = output_dir.replace("\\", "/")
    sample_code_prefix = req.get("sampleCodePrefix", "AUG")

    os.makedirs(output_dir, exist_ok=True)

    data1, data2, channel_info = open_data(file_path, key_num)
    raw_data = deal_data(data1, data2, length, label)
    aug_data = batch_augment(raw_data, aug_num=augment_ratio, random_aug=random_aug)

    output_npy = os.path.join(output_dir, f"{sample_code_prefix}_augmented.npy")
    # 统一输出到 JSON 和数据库中的路径格式
    output_npy = output_npy.replace("\\", "/")
    np.save(output_npy, aug_data)

    original_count = int(raw_data.shape[0])
    total_count = int(aug_data.shape[0])
    generated_count = total_count - original_count

    # 给前端画一条“原始-增强”对比曲线，避免把大数组全部塞入数据库。
    compare_index = 0
    first_aug_index = original_count if generated_count > 0 else 0
    original_de = raw_data[compare_index, :length]
    augmented_de = aug_data[first_aug_index, :length]
    show_len = min(256, length)
    curve_data = {
        "x": list(range(show_len)),
        "originalDE": original_de[:show_len].round(6).tolist(),
        "augmentedDE": augmented_de[:show_len].round(6).tolist(),
    }

    result = {
        "success": True,
        "algorithm": "noise_scale_flip_shift_mask",
        "augmentRatio": augment_ratio,
        "originalCount": original_count,
        "generatedCount": generated_count,
        "totalCount": total_count,
        "outputPath": output_npy,
        "channelInfo": channel_info,
        "curveData": curve_data,
    }

    out_dir = os.path.dirname(output_json_path)
    if out_dir:
        os.makedirs(out_dir, exist_ok=True)
    with open(output_json_path, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=2)


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("用法：python augment.py input.json result.json")
        sys.exit(1)
    run_augment(sys.argv[1], sys.argv[2])
