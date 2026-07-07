import numpy as np
import torch
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from common.plot_config import set_matplotlib_global
from common.utils import fig_to_base64, set_random_seed
from common.model import load_model_once
from business.fusion import run_feature_fusion_biz

set_matplotlib_global()
set_random_seed(0)

# 诊断专属绘图
def feature_plot(logits):
    fig = plt.figure(figsize=(10, 6))
    logits_np = logits[0].numpy()
    classes = np.arange(10)
    bars = plt.barh(classes, logits_np, color="#1f77b4")
    pred_label = torch.argmax(logits, dim=1).item()
    bars[pred_label].set_color("#ff7f0e")
    plt.xlabel("贡献值")
    plt.ylabel("类别标签")
    plt.xticks(fontsize=14)
    plt.yticks(classes, fontsize=14)
    plt.title("特征的诊断类别贡献度", fontsize=18)
    plt.grid(axis='x', alpha=0.3)
    plt.tight_layout()
    return fig

# 故障标签工具
def get_fault_info(label: int) -> dict:
    label_mapping = {
        0:  {"full_name": "正常状态(N)", "fault_size": 0},
        1:  {"full_name": "疲劳裂纹(F)", "fault_size": 0.007},
        2:  {"full_name": "疲劳裂纹(F)", "fault_size": 0.014},
        3:  {"full_name": "疲劳裂纹(F)", "fault_size": 0.021},
        4:  {"full_name": "磨损(W)", "fault_size": 0.007},
        5:  {"full_name": "磨损(W)", "fault_size": 0.014},
        6:  {"full_name": "磨损(W)", "fault_size": 0.021},
        7:  {"full_name": "凹痕(D)", "fault_size": 0.007},
        8:  {"full_name": "凹痕(D)", "fault_size": 0.014},
        9:  {"full_name": "凹痕(D)", "fault_size": 0.021},
    }
    if not isinstance(label, int) or label not in label_mapping:
        raise ValueError(f"标签输入错误！标签必须是0~9的整数，当前输入：{label}")
    info = label_mapping[label]
    abbr = info["full_name"].split("(")[-1].replace(")", "")
    return {
        "label": label,
        "fault_abbr": abbr,
        "fault_full_name": info["full_name"],
        "fault_size_inch": info["fault_size"],
        "load_hp_range": "0 / 1 / 2 / 3 hp"
    }

def run_diagnose_biz(
    preprocess_result: dict,
    w_x1: float = 0.5,
    w_x2: float = 0.5
):
    """
    业务2：故障诊断主入口
    1. 先调用融合业务得到完整融合特征与特征图
    2. 使用融合特征执行分类推理
    3. 输出故障预测、类别贡献图、融合业务全部图片
    """
    # 第一步：调用融合业务，获取融合特征与特征可视化
    params = preprocess_result["params"]
    fuse_result = run_feature_fusion_biz(preprocess_result, w_x1, w_x2)
    win_length = params["win_length"]
    norm_de = np.array(preprocess_result["data"]["norm_de"])
    norm_fe = np.array(preprocess_result["data"]["norm_fe"])

    # 构造输入张量
    x1_tensor = torch.from_numpy(norm_de).float().reshape(1, 1, win_length)
    x2_tensor = torch.from_numpy(norm_fe).float().reshape(1, 1, win_length)


    model = load_model_once()
    with torch.no_grad():
        # 仅使用分类层推理
        logits = model.forward(x1_tensor, x2_tensor,w_x1,w_x2)
        print(logits)

    # 生成诊断专属可视化：类别贡献图
    fig_contrib = feature_plot(logits)
    img_contrib = fig_to_base64(fig_contrib)

    # 解析故障预测
    pred_label = torch.argmax(logits, dim=1).item()
    fault_info = get_fault_info(pred_label)

    # 整合返回结果，包含融合业务所有图片+诊断新增图片
    return {
        "biz_name": "fault_diagnose",
        "status": "success",
        "params": fuse_result["params"],
        "fusion_feature_info": fuse_result["feature_data"],
        "model_output": {
            "logits": logits.numpy().tolist()[0],
            "predict_label": pred_label,
            ",": fault_info#000000000000000000000000000000000000000000000000
        },
        "images": {
            # 复用融合业务全部特征图
            "feat1_dot": fuse_result["images"]["feat1_dot"],
            "feat2_dot": fuse_result["images"]["feat2_dot"],
            "feat1_heatmap": fuse_result["images"]["feat1_heatmap"],
            "feat2_heatmap": fuse_result["images"]["feat2_heatmap"],
            "feature_correlation": fuse_result["images"]["feature_correlation"],
            # 诊断独有图片
            "class_contribution": img_contrib
        }
    }