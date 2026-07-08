import numpy as np
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from common.plot_config import set_matplotlib_global
from common.utils import fig_to_base64, set_random_seed
from business.preprocessing import *

set_matplotlib_global()
set_random_seed(0)

# 底层增强工具
def signal_augment(signal, aug_type=None):
    length = len(signal)
    if aug_type is None:
        aug_type = np.random.choice(["noise", "scale", "flip", "shift", "mask"])
    if aug_type == "noise":
        noise = np.random.normal(loc=0, scale=0.02, size=length)
        return signal + noise
    elif aug_type == "scale":
        scale_factor = np.random.uniform(0.8, 1.2)
        return signal * scale_factor
    elif aug_type == "flip":
        return np.flip(signal)
    elif aug_type == "shift":
        shift_len = np.random.randint(10, length // 4)
        return np.roll(signal, shift_len)
    elif aug_type == "mask":
        mask_ratio = np.random.uniform(0.05, 0.15)
        mask_num = int(length * mask_ratio)
        mask_idx = np.random.choice(length, mask_num, replace=False)
        aug_sig = signal.copy()
        aug_sig[mask_idx] = 0
        return aug_sig
    else:
        raise ValueError("增强类型仅支持: noise/scale/flip/shift/mask")

def batch_augment(data_batch, aug_num, aug_model):
    N, total_dim = data_batch.shape
    length = (total_dim - 1) // 2
    aug_list = [data_batch]
    for _ in range(aug_num):
        aug_samples = []
        for row in data_batch:
            de_sig = row[:length]
            fe_sig = row[length: 2*length]
            label = row[-1]
            de_aug = signal_augment(de_sig, aug_model)
            fe_aug = signal_augment(fe_sig, aug_model)
            new_sample = np.concatenate([de_aug, fe_aug, [label]])
            aug_samples.append(new_sample)
        aug_list.append(np.array(aug_samples))
    return np.vstack(aug_list)

def plot_de_overlay(orig_de, aug_de, show_len=256):
    fig = plt.figure(figsize=(12, 4))
    x = np.arange(show_len)
    plt.plot(x, orig_de[:show_len], color="#1f77b4", linewidth=1, label="原始 DE")
    plt.plot(x, aug_de[:show_len], color="#ff7f0e", linewidth=1, alpha=0.8, label="增强后 DE")
    plt.title(f"DE信号 原始 & 增强对比 (前{show_len}个采样点)")
    plt.xlabel("采样点")
    plt.ylabel("幅值")
    plt.legend(loc="upper right")
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.close(fig)
    return fig

# ===================== 业务顶层执行函数【带完整返回值】 =====================
def run_augment_biz(preprocess_result: dict, aug_model: str = "scale", aug_scale: int = 2):
    """
    业务2：数据增强主入口
    :param preprocess_result: 业务1预处理返回结果
    :return 增强业务结构化结果，可入库
    """

    if not isinstance(preprocess_result, dict):
        raise ValueError("preprocess_result 必须是字典")

    # 兼容 Java 保存实体：
    # {"id":..., "bizResult":"{\"code\":200,\"data\":{...}}"}
    if "bizResult" in preprocess_result:
        import json
        biz_result = preprocess_result["bizResult"]
        if isinstance(biz_result, str):
            preprocess_result = json.loads(biz_result)
        elif isinstance(biz_result, dict):
            preprocess_result = biz_result

    # 兼容统一响应体：
    # {"code":200, "msg":"操作成功", "data":{...}}
    if "code" in preprocess_result and "data" in preprocess_result:
        preprocess_result = preprocess_result["data"]

    # 兼容标准业务结构：
    # {"params": {...}, "data": {"norm_de":..., "norm_fe":...}}
    params = preprocess_result.get("params", {})

    if "data" in preprocess_result and isinstance(preprocess_result["data"], dict):
        data = preprocess_result["data"]
    else:
        data = preprocess_result

    win_length = params.get("win_length", data.get("win_length", 1024))
    key_num = params.get("key_num", data.get("key_num", None))

    if "norm_de" not in data:
        raise ValueError("preprocess_result 中缺少 norm_de，请粘贴预处理结果里的 data 内容，而不是空对象或外层对象")

    if "norm_fe" not in data:
        raise ValueError("preprocess_result 中缺少 norm_fe，请粘贴预处理结果里的 data 内容，而不是空对象或外层对象")

    norm_de = np.array(data["norm_de"])
    norm_fe = np.array(data["norm_fe"])

    # 构造批量原始数据集
    raw_batch = deal_data(norm_de, norm_fe, win_length, label=1)

    # 批量增强
    aug_batch = batch_augment(raw_batch, aug_num=aug_scale, aug_model=aug_model)

    # 绘制对比图
    pick_sample = 0
    orig_de = raw_batch[pick_sample, :win_length]
    aug_de = aug_batch[len(raw_batch) + pick_sample, :win_length]
    fig = plot_de_overlay(orig_de, aug_de, show_len=min(256, win_length))
    aug_img_b64 = fig_to_base64(fig)

    return {
        "biz_name": "augment",
        "status": "success",
        "params": {
            "source_preprocess_key_num": key_num,
            "win_length": win_length,
            "aug_model": aug_model,
            "aug_scale": aug_scale
        },
        "data": {
            "raw_batch": raw_batch.tolist(),
            "aug_batch": aug_batch.tolist(),
            "single_orig_de": orig_de.tolist(),
            "single_aug_de": aug_de.tolist()
        },
        "images": {
            "augment_compare": aug_img_b64
        }
    }