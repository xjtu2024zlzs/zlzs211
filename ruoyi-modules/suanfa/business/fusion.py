import numpy as np
import torch
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from common.plot_config import set_matplotlib_global
from common.utils import fig_to_base64, set_random_seed
from common.model import load_model_once

set_matplotlib_global()
set_random_seed(0)

# 绘图工具
def feature_remap(x):
    fig = plt.figure(figsize=(10, 6))
    feat_2d = x[0].cpu().numpy()
    im = plt.imshow(feat_2d, aspect="auto", cmap="viridis")
    plt.colorbar(im, label="特征值")
    plt.xlabel("时间步")
    plt.ylabel("神经网络通道")
    plt.title("通道热力图")
    plt.tight_layout()
    return fig

def feature_dot(x):
    fig = plt.figure(figsize=(12, 5))
    feat_chan = x[0, :, :].cpu().numpy()
    time_steps = np.arange(len(feat_chan))
    plt.scatter(time_steps, feat_chan, color="#ff7f0e", s=15, zorder=3)
    plt.plot(time_steps, feat_chan, color="#ff7f0e", alpha=0.4)
    plt.xlabel("时间步")
    plt.ylabel("通道特征值")
    plt.title("特征点线图")
    plt.grid(alpha=0.3)
    plt.tight_layout()
    return fig

def cor_feature_dot(x, y):
    fig = plt.figure(figsize=(7, 7))
    f1_all_chan = x[0, :, 0].cpu().numpy()
    f2_all_chan = y[0, :, 0].cpu().numpy()
    plt.scatter(f1_all_chan, f2_all_chan, alpha=0.7, s=30)
    plt.xlabel("模态一特征值")
    plt.ylabel("模态二特征值")
    plt.title("双模态特征相关度散点图")
    plt.grid(alpha=0.3)
    plt.tight_layout()
    return fig

def run_feature_fusion_biz(
    preprocess_result: dict,
    w_x1: float = 0.5,
    w_x2: float = 0.5
):
    """
    业务1：双模态加权特征融合（独立接口，只输出特征，无故障分类）
    :param preprocess_result: 预处理结果，包含norm_de、norm_fe两路信号
    :param w_x1: DE模态权重
    :param w_x2: FE模态权重
    :return: 两路原始特征、加权融合特征、全套特征可视化图片
    """
    params = preprocess_result["params"]
    win_length = params["win_length"]
    norm_de = np.array(preprocess_result["data"]["norm_de"])
    norm_fe = np.array(preprocess_result["data"]["norm_fe"])

    # 构造输入张量
    x1_tensor = torch.from_numpy(norm_de).float().reshape(1, 1, win_length)
    x2_tensor = torch.from_numpy(norm_fe).float().reshape(1, 1, win_length)

    model = load_model_once()
    with torch.no_grad():
        # 只执行融合分支，不进分类层
        fuse_feature, feat1_weighted, feat2_weighted = model.forward_fusion(x1_tensor, x2_tensor, w_x1, w_x2)
        feat1_raw = model.extract(x1_tensor)
        feat2_raw = model.extract(x2_tensor)

    # 生成所有特征可视化图片
    fig1_dot = feature_dot(feat1_raw)
    img1_dot = fig_to_base64(fig1_dot)

    fig2_dot = feature_dot(feat2_raw)
    img2_dot = fig_to_base64(fig2_dot)

    fig1_heat = feature_remap(feat1_raw)
    img1_heat = fig_to_base64(fig1_heat)

    fig2_heat = feature_remap(feat2_raw)
    img2_heat = fig_to_base64(fig2_heat)

    fig_cor = cor_feature_dot(feat1_raw, feat2_raw)
    img_cor = fig_to_base64(fig_cor)

    return {
        "biz_name": "feature_fusion",
        "status": "success",
        "params": {
            "win_length": win_length,
            "w_x1": w_x1,
            "w_x2": w_x2
        },
        "feature_data": {
            "feat1_raw": feat1_raw.cpu().numpy().tolist(),
            "feat2_raw": feat2_raw.cpu().numpy().tolist(),
            "feat1_weighted": feat1_weighted.cpu().numpy().tolist(),
            "feat2_weighted": feat2_weighted.cpu().numpy().tolist(),
            "fusion_feature": fuse_feature.cpu().numpy().tolist()
        },
        "images": {
            "feat1_dot": img1_dot,
            "feat2_dot": img2_dot,
            "feat1_heatmap": img1_heat,
            "feat2_heatmap": img2_heat,
            "feature_correlation": img_cor
        }
    }