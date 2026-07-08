"""
裂纹扩展 Kriging 代理模型 API
=============================

本模块基于 scikit-learn 的高斯过程回归（Gaussian Process Regression, GPR）实现
裂纹扩展的代理建模。在工程上常将 GPR 称为 Kriging 插值/克里金方法。

项目用途
--------
用有限元或试验得到的样本数据，训练一个可快速预测「最终裂纹长度」的代理模型，
替代每次重新跑仿真，用于参数扫描、可靠性评估或失效判定。

典型工作流
----------
1. 准备样本 CSV（见 surrogate_model_samples.csv），包含 FEATURE_COLUMNS + TARGET_COLUMN
2. 调用 train_from_file() 或 train_model() 训练并保存 .joblib 模型文件
3. 调用 load_model() 加载模型
4. 调用 predict_from_dict() / predict_single() / predict_batch() 进行预测
5. 预测结果自动附带失效判定（临界裂纹长度默认 60 mm）

文件依赖
--------
- 训练数据: surrogate_model_samples.csv
- 模型文件: kriging_surrogate_model.joblib（训练后生成）
- 接口说明: api_schema.json
- 调用示例: example_call.py
"""

from __future__ import annotations
from dataclasses import dataclass
from typing import Dict, Optional

import joblib
import numpy as np
import pandas as pd

from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.gaussian_process import GaussianProcessRegressor
from sklearn.gaussian_process.kernels import ConstantKernel, Matern, WhiteKernel
from sklearn.model_selection import train_test_split
from sklearn.metrics import r2_score, mean_squared_error, mean_absolute_error


# ---------------------------------------------------------------------------
# 常量：特征列、目标列、失效阈值
# ---------------------------------------------------------------------------

# 模型输入的 8 个特征列，顺序固定，与训练/预测时数组列顺序一致
FEATURE_COLUMNS = [
    "a0_mm",        # 初始裂纹长度 (mm)
    "P1_kN",        # 第 1 段载荷 (kN)
    "N1",           # 第 1 段循环次数
    "P2_kN",        # 第 2 段载荷 (kN)
    "N2",           # 第 2 段循环次数
    "P3_kN",        # 第 3 段载荷 (kN)
    "N3",           # 第 3 段循环次数
    "task_count_T", # 任务/工况序号（同一样本在不同 T 下的扩展结果）
]

# 训练时的预测目标：仿真或试验得到的最终裂纹长度
TARGET_COLUMN = "a_final_mm"

# 临界裂纹长度 (mm)：超过此值判定为失效/达到临界状态
CRITICAL_CRACK_LENGTH_MM = 60.0


## 用于判断是否失效，并且返回数值
def judge_failure(a_final_mm: float, critical_length_mm: float = CRITICAL_CRACK_LENGTH_MM) -> Dict[str, object]:
    """
    根据预测的最终裂纹长度判定是否达到失效临界状态。

    判定规则
    --------
    当 a_final_mm >= critical_length_mm 时，判定为「达到临界裂纹长度」。

    参数
    ----
    a_final_mm : float
        预测或实测的最终裂纹长度 (mm)。
    critical_length_mm : float, optional
        临界裂纹长度阈值，默认 60.0 mm。

    返回
    ----
    dict，包含以下键：
    - critical_length_mm : 使用的临界长度阈值
    - is_failed : bool，True 表示已达到临界裂纹长度
    - failure_status : str，中文状态描述
    - remaining_margin_mm : float，距离临界长度的剩余裕度（已失效则为 0）
    """
    a_final = float(a_final_mm)
    critical = float(critical_length_mm)
    is_failed = a_final >= critical

    return {
        "critical_length_mm": critical,
        "is_failed": bool(is_failed),
        "failure_status": "达到临界裂纹长度" if is_failed else "未达到临界裂纹长度",
        "remaining_margin_mm": max(0.0, critical - a_final),
    }

## 训练完成后，测试算法性能
@dataclass
class ModelMetrics:
    """
    训练完成后在测试集上计算的模型评估指标。

    属性
    ----
    r2 : float
        决定系数 R²，越接近 1 表示拟合越好。
    rmse_mm : float
        均方根误差 (mm)，衡量预测与真值的平均偏差。
    mae_mm : float
        平均绝对误差 (mm)，对异常值不如 RMSE 敏感。
    train_samples : int
        训练集样本数。
    test_samples : int
        测试集样本数（默认占总样本 20%）。
    total_samples : int
        参与训练的总样本数。
    """
    r2: float
    rmse_mm: float
    mae_mm: float
    train_samples: int
    test_samples: int
    total_samples: int


def load_model(model_path: str = "kriging_surrogate_model.joblib") -> Pipeline:
    """
    从磁盘加载已训练的 sklearn Pipeline 模型。

    Pipeline 结构：StandardScaler → GaussianProcessRegressor

    参数
    ----
    model_path : str
        .joblib 模型文件路径，默认当前目录下的 kriging_surrogate_model.joblib。

    返回
    ----
    sklearn.pipeline.Pipeline
        可直接调用 .predict() 的完整流水线。
    """
    return joblib.load(model_path)


## 模型预测(单个)
def predict_single(
    model: Pipeline,
    a0_mm: float,
    P1_kN: float,
    N1: int,
    P2_kN: float,
    N2: int,
    P3_kN: float,
    N3: int,
    task_count_T: int,
    return_std: bool = True,
) -> Dict[str, float]:
    """
    对单组工况参数进行裂纹扩展预测。

    参数
    ----
    model : Pipeline
        已加载的 Kriging 代理模型。
    a0_mm, P1_kN, N1, P2_kN, N2, P3_kN, N3, task_count_T
        8 个输入特征，含义见 FEATURE_COLUMNS 注释。
    return_std : bool, optional
        True 时同时返回 Kriging 预测标准差 pred_std_mm（不确定性估计）。

    返回
    ----
    dict，核心字段：
    - a_final_mm : 预测最终裂纹长度 (mm)
    - growth_mm : 裂纹扩展量 = a_final_mm - a0_mm (mm)
    - pred_std_mm : 预测标准差 (mm)，仅 return_std=True 时包含
    - 以及 judge_failure() 返回的失效判定字段
    """
    x = np.array([[a0_mm, P1_kN, N1, P2_kN, N2, P3_kN, N3, task_count_T]], dtype=float)

    if return_std:
        pred, std = model.predict(x, return_std=True)
        a_final = float(pred[0])
        result = {
            "a_final_mm": a_final,
            "growth_mm": a_final - float(a0_mm),
            "pred_std_mm": float(std[0]),
        }
        result.update(judge_failure(a_final))
        return result

    pred = model.predict(x)
    a_final = float(pred[0])
    result = {
        "a_final_mm": a_final,
        "growth_mm": a_final - float(a0_mm),
    }
    result.update(judge_failure(a_final))
    return result

## 模型预测(集群)
def predict_from_dict(model: Pipeline, payload: Dict[str, float], return_std: bool = True) -> Dict[str, float]:
    """
    从字典形式的输入进行预测（推荐的外部调用方式）。

    适用于 HTTP 接口、配置文件或 example_call.py 中的调用场景。
    字典键名必须与 FEATURE_COLUMNS 完全一致，缺少任意字段会抛出 ValueError。

    参数
    ----
    model : Pipeline
        已加载的模型。
    payload : dict
        键为特征名、值为数值的字典，例如：
        {"a0_mm": 20.0, "P1_kN": 100.0, "N1": 15000, ...}
    return_std : bool, optional
        是否返回预测标准差。

    返回
    ----
    dict
        同 predict_single() 的返回结构。
    """
    missing = [c for c in FEATURE_COLUMNS if c not in payload]
    if missing:
        raise ValueError(f"missing fields: {missing}")

    return predict_single(
        model=model,
        a0_mm=float(payload["a0_mm"]),
        P1_kN=float(payload["P1_kN"]),
        N1=int(payload["N1"]),
        P2_kN=float(payload["P2_kN"]),
        N2=int(payload["N2"]),
        P3_kN=float(payload["P3_kN"]),
        N3=int(payload["N3"]),
        task_count_T=int(payload["task_count_T"]),
        return_std=return_std,
    )


def predict_batch(model: Pipeline, input_df: pd.DataFrame, return_std: bool = True) -> pd.DataFrame:
    """
    对 DataFrame 中的多行样本批量预测。

    输入 DataFrame 必须包含 FEATURE_COLUMNS 中的全部列；
    返回在原 DataFrame 基础上追加预测列（不修改原始特征列）。

    追加列说明
    ----------
    - a_final_pred_mm : 预测最终裂纹长度
    - growth_pred_mm : 预测扩展量
    - pred_std_mm : 预测标准差（return_std=True 时）
    - critical_length_mm / is_failed / failure_status / remaining_margin_mm : 失效判定

    参数
    ----
    model : Pipeline
        已加载的模型。
    input_df : pd.DataFrame
        每行一组工况，列名需含 FEATURE_COLUMNS。
    return_std : bool, optional
        是否计算并返回标准差列。

    返回
    ----
    pd.DataFrame
        原输入副本 + 预测与判定列。
    """
    for col in FEATURE_COLUMNS:
        if col not in input_df.columns:
            raise ValueError(f"missing column: {col}")

    X = input_df[FEATURE_COLUMNS].to_numpy(dtype=float)
    result_df = input_df.copy()

    if return_std:
        pred, std = model.predict(X, return_std=True)
        result_df["a_final_pred_mm"] = pred
        result_df["growth_pred_mm"] = pred - result_df["a0_mm"].to_numpy(dtype=float)
        result_df["critical_length_mm"] = CRITICAL_CRACK_LENGTH_MM
        result_df["is_failed"] = result_df["a_final_pred_mm"] >= CRITICAL_CRACK_LENGTH_MM
        result_df["failure_status"] = result_df["is_failed"].map({True: "达到临界裂纹长度", False: "未达到临界裂纹长度"})
        result_df["remaining_margin_mm"] = (CRITICAL_CRACK_LENGTH_MM - result_df["a_final_pred_mm"]).clip(lower=0.0)
        result_df["pred_std_mm"] = std
        result_df["critical_length_mm"] = CRITICAL_CRACK_LENGTH_MM
        result_df["is_failed"] = result_df["a_final_pred_mm"] >= CRITICAL_CRACK_LENGTH_MM
        result_df["failure_status"] = result_df["is_failed"].map({True: "达到临界裂纹长度", False: "未达到临界裂纹长度"})
        result_df["remaining_margin_mm"] = (CRITICAL_CRACK_LENGTH_MM - result_df["a_final_pred_mm"]).clip(lower=0.0)
    else:
        pred = model.predict(X)
        result_df["a_final_pred_mm"] = pred
        result_df["growth_pred_mm"] = pred - result_df["a0_mm"].to_numpy(dtype=float)
        result_df["critical_length_mm"] = CRITICAL_CRACK_LENGTH_MM
        result_df["is_failed"] = result_df["a_final_pred_mm"] >= CRITICAL_CRACK_LENGTH_MM
        result_df["failure_status"] = result_df["is_failed"].map({True: "达到临界裂纹长度", False: "未达到临界裂纹长度"})
        result_df["remaining_margin_mm"] = (CRITICAL_CRACK_LENGTH_MM - result_df["a_final_pred_mm"]).clip(lower=0.0)

    return result_df


## 构建高斯模型
def build_model(length_scale: float = 2.0, noise_level: float = 1e-6, optimizer: Optional[str] = None) -> Pipeline:
    """
    构建 Kriging（GPR）模型流水线，尚未 fit。

    模型结构
    --------
    1. StandardScaler：对 8 维输入做零均值单位方差标准化
    2. GaussianProcessRegressor：高斯过程回归
       - 核函数：ConstantKernel × Matern(nu=2.5) + WhiteKernel
         · Matern 核：控制样本间相关性随距离衰减的速度
         · WhiteKernel：吸收观测噪声
       - normalize_y=True：对目标值 y 也做标准化，提升数值稳定性

    参数
    ----
    length_scale : float, optional
        Matern 核的长度尺度初值，默认 2.0。越大表示输入变化对输出影响越「平滑」。
    noise_level : float, optional
        WhiteKernel 噪声水平初值，默认 1e-6。
    optimizer : str or None, optional
        None：固定超参数不优化（训练更快、结果可复现）。
        "fmin_l_bfgs_b"：启用 L-BFGS-B 优化核超参数（更灵活但更慢）。

    返回
    ----
    sklearn.pipeline.Pipeline
        未训练的 Pipeline，需调用 .fit(X, y) 或 train_model()。
    """
    kernel = ConstantKernel(1.0, constant_value_bounds="fixed") * Matern(
        length_scale=np.ones(len(FEATURE_COLUMNS)) * length_scale,
        length_scale_bounds="fixed" if optimizer is None else (1e-2, 1e3),
        nu=2.5,
    ) + WhiteKernel(
        noise_level=noise_level,
        noise_level_bounds="fixed" if optimizer is None else (1e-8, 1e-2),
    )

    return Pipeline([
        ("scaler", StandardScaler()),
        ("gpr", GaussianProcessRegressor(
            kernel=kernel,
            optimizer=optimizer,
            normalize_y=True,
            alpha=1e-8,
            random_state=20260612,
            n_restarts_optimizer=0 if optimizer is None else 5,
        )),
    ])

## 训练模型
def train_model(
    dataset_df: pd.DataFrame,
    model_path: str = "kriging_surrogate_model.joblib",
    test_size: float = 0.2,
    random_state: int = 20260612,
    optimizer: Optional[str] = None,
) -> ModelMetrics:
    """
    从 DataFrame 训练 Kriging 代理模型并保存到磁盘。

    流程
    ----
    1. 校验 FEATURE_COLUMNS + TARGET_COLUMN 列是否存在
    2. 按 test_size 随机划分训练/测试集（默认 80%/20%）
    3. build_model() → fit → 在测试集上计算 R²/RMSE/MAE
    4. joblib.dump 保存完整 Pipeline

    参数
    ----
    dataset_df : pd.DataFrame
        训练样本，需含 8 个特征列和 a_final_mm 目标列。
    model_path : str, optional
        模型保存路径。
    test_size : float, optional
        测试集比例，默认 0.2。
    random_state : int, optional
        随机划分种子，保证可复现。
    optimizer : str or None, optional
        传给 build_model()，是否优化核超参数。

    返回
    ----
    ModelMetrics
        测试集上的评估指标及样本统计。
    """
    for col in FEATURE_COLUMNS + [TARGET_COLUMN]:
        if col not in dataset_df.columns:
            raise ValueError(f"missing column: {col}")

    X = dataset_df[FEATURE_COLUMNS].to_numpy(dtype=float)
    y = dataset_df[TARGET_COLUMN].to_numpy(dtype=float)

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=test_size, random_state=random_state
    )

    model = build_model(optimizer=optimizer)
    model.fit(X_train, y_train)
    y_pred = model.predict(X_test)

    metrics = ModelMetrics(
        r2=float(r2_score(y_test, y_pred)),
        rmse_mm=float(np.sqrt(mean_squared_error(y_test, y_pred))),
        mae_mm=float(mean_absolute_error(y_test, y_pred)),
        train_samples=len(X_train),
        test_samples=len(X_test),
        total_samples=len(X),
    )

    joblib.dump(model, model_path)
    return metrics


def train_from_file(
    dataset_path: str,
    model_path: str = "kriging_surrogate_model.joblib",
    sheet_name: str = "代理模型样本库",
    optimizer: Optional[str] = None,
) -> ModelMetrics:
    """
    从 CSV 或 Excel 文件读取样本并训练模型（便捷入口）。

    参数
    ----
    dataset_path : str
        数据文件路径。以 .csv 结尾则 read_csv，否则 read_excel。
    model_path : str, optional
        模型保存路径。
    sheet_name : str, optional
        Excel 工作表名，默认「代理模型样本库」。
    optimizer : str or None, optional
        是否优化核超参数。

    返回
    ----
    ModelMetrics
        同 train_model()。

    示例
    ----
    metrics = train_from_file("surrogate_model_samples.csv")
    print(metrics.r2, metrics.rmse_mm)  # 例如 0.979089 1.592384
    """
    if dataset_path.lower().endswith(".csv"):
        df = pd.read_csv(dataset_path)
    else:
        df = pd.read_excel(dataset_path, sheet_name=sheet_name)

    return train_model(df, model_path=model_path, optimizer=optimizer)


if __name__ == "__main__":
    # 直接运行本文件时：加载已有模型并对一组示例工况做预测
    model = load_model("kriging_surrogate_model.joblib")
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
    print(predict_from_dict(model, payload))
