"""
Kriging 代理模型调用示例
========================

本脚本演示如何加载已训练模型并对单组工况进行预测。
运行前请确保：
  1. 已安装依赖：pip install -r requirements.txt
  2. 已存在模型文件 kriging_surrogate_model.joblib（若不存在，需先训练，见下方说明）

训练模型（首次使用时）：
  在 Python 中执行：
    from surrogate_model_api import train_from_file
    metrics = train_from_file("surrogate_model_samples.csv")
    print(metrics)

运行本示例：
  python example_call.py
"""

from surrogate_model_api import load_model, predict_from_dict

# ---------------------------------------------------------------------------
# 1. 加载模型
#    模型是 sklearn Pipeline（StandardScaler + GaussianProcessRegressor），
#    由 train_from_file() / train_model() 训练后序列化为 .joblib 文件。
# ---------------------------------------------------------------------------
model = load_model("kriging_surrogate_model.joblib")

# ---------------------------------------------------------------------------
# 2. 构造输入 payload（字典形式，键名必须与 FEATURE_COLUMNS 一致）
#
#    字段含义：
#    - a0_mm        初始裂纹长度 (mm)
#    - P1_kN ~ P3_kN  三段载荷谱各段峰值载荷 (kN)
#    - N1 ~ N3        三段载荷谱各段循环次数
#    - task_count_T   工况/任务序号（同一样本在不同 T 下的扩展阶段）
# ---------------------------------------------------------------------------
payload = {
    "a0_mm": 20.0,
    "P1_kN": 100.0,
    "N1": 15000,
    "P2_kN": 200.0,
    "N2": 15000,
    "P3_kN": 270.0,
    "N3": 15000,
    "task_count_T": 2,
}

# ---------------------------------------------------------------------------
# 3. 调用预测（return_std=True 时附带 Kriging 不确定性估计）
# ---------------------------------------------------------------------------
result = predict_from_dict(model, payload)
print(result)

# ---------------------------------------------------------------------------
# 4. 返回字段说明（完整定义见 api_schema.json）
# ---------------------------------------------------------------------------
# a_final_mm          预测最终裂纹长度 (mm)
# growth_mm           裂纹扩展量 = a_final_mm - a0_mm (mm)
# pred_std_mm         Kriging 预测标准差 (mm)，越大表示该点不确定性越高
# critical_length_mm  临界裂纹长度阈值，固定 60.0 mm
# is_failed           是否达到临界裂纹长度（True = 失效/临界）
# failure_status      失效判定文字（「达到临界裂纹长度」或「未达到临界裂纹长度」）
# remaining_margin_mm 距离临界长度的剩余裕度 (mm)，已失效时为 0
