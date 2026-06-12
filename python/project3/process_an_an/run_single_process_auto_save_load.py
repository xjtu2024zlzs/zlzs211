# -*- coding: utf-8 -*-
"""
run_single_process_auto_save_load.py

单工序/单列逐点异常检测脚本（Generic 模式）。

用途：
- 不修改当前全局模型；
- 不做其他方法对比；
- 每次指定一个单列工序数据作为输入；
- 使用当前研究思路：MemAE + 多异常分数 + ECDF + EWMA + UCL；
- 输出该工序中"每一个采样点"的异常分数和判异结果；
- 额外输出被判为异常的点。

输入：
- --train_file : 正常工况训练数据文件（必填，支持 .csv / .xlsx / .xls）
- --test_file  : 待检测测试数据文件（必填，支持 .csv / .xlsx / .xls）
- --label_col  : 测试数据中的标签列名（可选，用于评估）

运行示例：
python run_single_process_auto_save_load.py ^
  --train_file D:/data/normal_train.xlsx ^
  --test_file D:/data/test_data.xlsx ^
  --feature_col temperature ^
  --process_name temperature ^
  --label_col anomaly_label ^
  --epochs 80 ^
  --out_dir D:/output/single_process

输出：
- <process_name>_train_scores.csv       : 训练集每个点的分数
- <process_name>_train_alarm_points.csv : 训练集报警点
- <process_name>_test_scores.csv        : 测试集每个点的分数
- <process_name>_test_alarm_points.csv  : 测试集报警点
- <process_name>_summary.csv            : 汇总统计
- 可选：<process_name>_memae.pt, <process_name>_preprocess.pkl（自动保存/加载）
"""

import argparse
import os
import pickle
import random
from dataclasses import dataclass
from typing import Dict, List, Optional, Tuple

import numpy as np
import pandas as pd

import torch
import torch.nn as nn
import torch.nn.functional as F
from torch.utils.data import DataLoader, TensorDataset


class MemAE(nn.Module):
    """
    当前研究方法中的 MemAE 思路：
    encoder/decoder + memory bank + cosine similarity + sparse attention。
    单列工序检测时 input_dim=1。
    """

    def __init__(
        self,
        input_dim: int = 1,
        hidden_dims: Tuple[int, ...] = (64, 32, 16),
        latent_dim: int = 8,
        mem_dim: int = 32,
        dropout_p: float = 0.1,
        shrink_thres: float = 0.005,
    ):
        super().__init__()
        self.input_dim = int(input_dim)
        self.mem_dim = int(mem_dim)
        self.shrink_thres = float(shrink_thres)

        enc_layers: List[nn.Module] = []
        prev = self.input_dim
        for h in hidden_dims:
            enc_layers.append(nn.Linear(prev, int(h)))
            enc_layers.append(nn.BatchNorm1d(int(h)))
            enc_layers.append(nn.ReLU())
            enc_layers.append(nn.Dropout(p=float(dropout_p)))
            prev = int(h)
        enc_layers.append(nn.Linear(prev, int(latent_dim)))
        self.encoder = nn.Sequential(*enc_layers)

        self.memory = nn.Parameter(torch.randn(int(mem_dim), int(latent_dim)))

        dec_layers: List[nn.Module] = []
        prev = int(latent_dim)
        for h in reversed(hidden_dims):
            dec_layers.append(nn.Linear(prev, int(h)))
            dec_layers.append(nn.BatchNorm1d(int(h)))
            dec_layers.append(nn.ReLU())
            dec_layers.append(nn.Dropout(p=float(dropout_p)))
            prev = int(h)
        dec_layers.append(nn.Linear(prev, self.input_dim))
        self.decoder = nn.Sequential(*dec_layers)

    def forward(self, x: torch.Tensor):
        z = self.encoder(x)
        z_norm = F.normalize(z, p=2, dim=1)
        mem_norm = F.normalize(self.memory, p=2, dim=1)

        cos = torch.matmul(z_norm, mem_norm.t())
        attn = F.softmax(cos, dim=1)

        if self.shrink_thres is not None and self.shrink_thres > 0:
            attn_shrunk = F.relu(attn - self.shrink_thres)
            denom = attn_shrunk.sum(dim=1, keepdim=True)
            mask = denom > 0
            attn_normed = attn_shrunk / (denom + 1e-8)
            attn = torch.where(mask, attn_normed, attn)

        z_hat = torch.matmul(attn, self.memory)
        x_hat = self.decoder(z_hat)
        return x_hat, z, cos, attn


@dataclass
class PreprocessState:
    feature_col: str
    mean: float
    std: float
    component_params: Dict[str, Tuple[float, float]]
    t2_param: Tuple[float, float]
    ecdf_raw_scores: np.ndarray
    ucl_sample: float
    args: Dict


def set_seed(seed: int) -> None:
    seed = int(seed)
    random.seed(seed)
    np.random.seed(seed)
    torch.manual_seed(seed)
    torch.cuda.manual_seed_all(seed)


def ensure_dir(path: str) -> None:
    os.makedirs(path, exist_ok=True)


def parse_hidden_dims(s: str) -> Tuple[int, ...]:
    return tuple(int(x.strip()) for x in s.split(",") if x.strip())


def extract_single_column(df: pd.DataFrame, col: str) -> np.ndarray:
    if col not in df.columns:
        raise ValueError(f"列不存在：{col}。当前文件前 30 个列名为：{list(df.columns)[:30]}")
    x = pd.to_numeric(df[col], errors="coerce").to_numpy(np.float32)
    if not np.all(np.isfinite(x)):
        x = pd.Series(x).replace([np.inf, -np.inf], np.nan).ffill().bfill().to_numpy(np.float32)
    return x.reshape(-1, 1)


# TEP 元数据列（generic 模式下不含这些列，但保留定义兼容旧逻辑）
_TEP_META_COLS = {"faultNumber", "simulationRun", "sample"}

def auto_select_feature_col(df: pd.DataFrame) -> str:
    """自动选择第一个非元数据、可转为数值的列。"""
    for c in df.columns:
        if c in _TEP_META_COLS:
            continue
        s = pd.to_numeric(df[c], errors="coerce")
        if s.notna().sum() > 0:
            return c
    raise ValueError("没有找到可用的数值特征列。请检查 CSV 或手动传入 --feature_col。")


def read_data_file(filepath: str) -> pd.DataFrame:
    """读取数据文件，根据扩展名自动选择 csv 或 xlsx 读取方式。"""
    ext = os.path.splitext(filepath)[1].lower()
    if ext in (".xlsx", ".xls"):
        return pd.read_excel(filepath)
    else:
        return pd.read_csv(filepath)


def make_model_from_args(args) -> MemAE:
    return MemAE(
        input_dim=1,
        hidden_dims=parse_hidden_dims(args.hidden_dims),
        latent_dim=args.latent_dim,
        mem_dim=args.mem_dim,
        dropout_p=args.dropout_p,
        shrink_thres=args.shrink_thres,
    )


def model_artifact_paths(args):
    model_path = os.path.join(args.out_dir, f"{args.process_name}_memae.pt")
    prep_path = os.path.join(args.out_dir, f"{args.process_name}_preprocess.pkl")
    return model_path, prep_path


def artifacts_exist(args) -> bool:
    model_path, prep_path = model_artifact_paths(args)
    return os.path.exists(model_path) and os.path.exists(prep_path)


def load_saved_model_and_preprocess(args, device: str):
    model_path, prep_path = model_artifact_paths(args)
    with open(prep_path, "rb") as f:
        prep = pickle.load(f)

    model = make_model_from_args(args).to(device)
    state = torch.load(model_path, map_location=device)
    model.load_state_dict(state)
    model.eval()

    print(f"[INFO] 已加载模型：{model_path}")
    print(f"[INFO] 已加载预处理参数：{prep_path}")
    return model, prep


def fit_scaler(x_train: np.ndarray) -> Tuple[float, float]:
    mean = float(np.mean(x_train))
    std = float(np.std(x_train, ddof=1))
    if not np.isfinite(std) or std <= 1e-8:
        std = float(np.std(x_train, ddof=0))
    if not np.isfinite(std) or std <= 1e-8:
        std = 1.0
    return mean, std


def apply_scaler(x: np.ndarray, mean: float, std: float) -> np.ndarray:
    return ((np.asarray(x, dtype=np.float32) - float(mean)) / float(std)).astype(np.float32)


def robust_median_mad(x: np.ndarray, eps: float = 1e-8) -> Tuple[float, float]:
    x = np.asarray(x, dtype=np.float64)
    x = x[np.isfinite(x)]
    if x.size == 0:
        return 0.0, 1.0
    med = float(np.median(x))
    mad = float(np.median(np.abs(x - med)) * 1.4826)
    if not np.isfinite(mad) or mad < eps:
        std = float(np.std(x))
        mad = std if std > eps else 1.0
    return med, mad


def robust_z(x: np.ndarray, med: float, scale: float, clip: float) -> np.ndarray:
    z = (np.asarray(x, dtype=np.float64) - float(med)) / max(float(scale), 1e-8)
    z = np.clip(z, -float(clip), float(clip))
    return z.astype(np.float32, copy=False)


class ECDFTransform:
    def __init__(self, cal_scores: np.ndarray):
        cal = np.asarray(cal_scores, dtype=np.float64)
        cal = cal[np.isfinite(cal)]
        if cal.size < 10:
            raise ValueError("训练分数太少，无法构造 ECDF")
        self.sorted = np.sort(cal)
        self.n = int(self.sorted.size)

    def transform(self, scores: np.ndarray, eps: float = 1e-12) -> np.ndarray:
        s = np.asarray(scores, dtype=np.float64)
        idx = np.searchsorted(self.sorted, s, side="right")
        ge = (self.n - idx).astype(np.int64, copy=False)
        p = (ge + 1).astype(np.float64) / float(self.n + 1)
        return (-np.log(p + float(eps))).astype(np.float32, copy=False)


def ewma_stat(x: np.ndarray, lam: float, z0: float = 0.0) -> np.ndarray:
    x = np.asarray(x, dtype=np.float32)
    y = np.empty_like(x, dtype=np.float32)
    yt = float(z0)
    lam = float(lam)
    for i in range(len(x)):
        yt = (1.0 - lam) * yt + lam * float(x[i])
        y[i] = yt
    return y


def ucl_quantile(x: np.ndarray, alpha: float) -> float:
    x = np.asarray(x, dtype=np.float64)
    x = x[np.isfinite(x)]
    if x.size < 10:
        raise RuntimeError("训练统计量太少，无法标定 UCL")
    return float(np.quantile(x, 1.0 - float(alpha)))


def train_memae(
    x_train_scaled: np.ndarray,
    args,
    device: str,
) -> MemAE:
    x = torch.tensor(x_train_scaled, dtype=torch.float32)
    ds = TensorDataset(x)
    drop_last = len(ds) > int(args.batch_size)
    loader = DataLoader(ds, batch_size=int(args.batch_size), shuffle=True, drop_last=drop_last)

    model = MemAE(
        input_dim=1,
        hidden_dims=parse_hidden_dims(args.hidden_dims),
        latent_dim=args.latent_dim,
        mem_dim=args.mem_dim,
        dropout_p=args.dropout_p,
        shrink_thres=args.shrink_thres,
    ).to(device)

    opt = torch.optim.AdamW(model.parameters(), lr=args.lr, weight_decay=args.weight_decay)

    model.train()
    for ep in range(1, args.epochs + 1):
        losses = []
        for (xb,) in loader:
            xb = xb.to(device)
            x_hat, z, cos, attn = model(xb)

            recon_loss = F.mse_loss(x_hat, xb)
            entropy = -torch.sum(attn * torch.log(attn + 1e-12), dim=1).mean()
            loss = recon_loss + 1e-4 * entropy

            opt.zero_grad(set_to_none=True)
            loss.backward()
            opt.step()
            losses.append(float(loss.detach().cpu()))

        if ep == 1 or ep == args.epochs or ep % max(1, args.epochs // 5) == 0:
            print(f"[train] epoch={ep:04d}/{args.epochs}, loss={np.mean(losses):.6f}")

    return model


@torch.no_grad()
def score_memae(model: MemAE, x_scaled: np.ndarray, batch_size: int, device: str) -> Dict[str, np.ndarray]:
    model.eval()
    x = torch.tensor(x_scaled, dtype=torch.float32)
    loader = DataLoader(TensorDataset(x), batch_size=int(batch_size), shuffle=False)

    recon_list = []
    entropy_list = []
    dmin_list = []
    xhat_list = []

    for (xb,) in loader:
        xb = xb.to(device)
        x_hat, z, cos, attn = model(xb)

        recon = torch.mean((x_hat - xb) ** 2, dim=1)
        entropy = -torch.sum(attn * torch.log(attn + 1e-12), dim=1)
        dmin = 1.0 - torch.max(cos, dim=1).values

        recon_list.append(recon.detach().cpu().numpy())
        entropy_list.append(entropy.detach().cpu().numpy())
        dmin_list.append(dmin.detach().cpu().numpy())
        xhat_list.append(x_hat.detach().cpu().numpy())

    return {
        "recon": np.concatenate(recon_list).astype(np.float32),
        "entropy": np.concatenate(entropy_list).astype(np.float32),
        "dmin": np.concatenate(dmin_list).astype(np.float32),
        "x_hat_scaled": np.concatenate(xhat_list).astype(np.float32).reshape(-1, 1),
    }


def build_scores(
    comp: Dict[str, np.ndarray],
    x_scaled: np.ndarray,
    args,
    params: Optional[Dict[str, Tuple[float, float]]] = None,
    t2_param: Optional[Tuple[float, float]] = None,
) -> Tuple[np.ndarray, np.ndarray, np.ndarray, Dict[str, Tuple[float, float]], Tuple[float, float]]:
    if params is None:
        params = {
            "recon": robust_median_mad(comp["recon"]),
            "entropy": robust_median_mad(comp["entropy"]),
            "dmin": robust_median_mad(comp["dmin"]),
        }

    recon_z = robust_z(comp["recon"], params["recon"][0], params["recon"][1], args.z_clip)
    entropy_z = robust_z(comp["entropy"], params["entropy"][0], params["entropy"][1], args.z_clip)
    dmin_z = robust_z(comp["dmin"], params["dmin"][0], params["dmin"][1], args.z_clip)

    weight_sum = max(args.w_recon + args.w_entropy + args.w_dmin, 1e-12)
    memae_score = (
        args.w_recon * recon_z +
        args.w_entropy * entropy_z +
        args.w_dmin * dmin_z
    ) / weight_sum

    # 单列 T²：标准化值平方，再做 robust z。
    t2_raw = (np.asarray(x_scaled).reshape(-1) ** 2).astype(np.float32)
    if t2_param is None:
        t2_param = robust_median_mad(t2_raw)
    t2_score = robust_z(t2_raw, t2_param[0], t2_param[1], args.z_clip)

    raw_mix_score = (args.mix_w * memae_score + (1.0 - args.mix_w) * t2_score).astype(np.float32)
    return raw_mix_score, memae_score.astype(np.float32), t2_score.astype(np.float32), params, t2_param


def write_scores(
    path: str,
    df_meta: Optional[pd.DataFrame],
    feature_col: str,
    x_raw: np.ndarray,
    x_scaled: np.ndarray,
    comp: Dict[str, np.ndarray],
    memae_score: np.ndarray,
    t2_score: np.ndarray,
    raw_score: np.ndarray,
    ecdf_score: np.ndarray,
    ewma: np.ndarray,
    ucl: float,
    y_true: Optional[np.ndarray],
) -> pd.DataFrame:
    out = pd.DataFrame({
        "index": np.arange(len(x_raw), dtype=np.int64),
        feature_col: np.asarray(x_raw).reshape(-1),
        "x_scaled": np.asarray(x_scaled).reshape(-1),
        "x_hat_scaled": comp["x_hat_scaled"].reshape(-1),
        "recon": comp["recon"],
        "entropy": comp["entropy"],
        "dmin": comp["dmin"],
        "memae_score": memae_score,
        "t2_score": t2_score,
        "raw_mix_score": raw_score,
        "ecdf_score": ecdf_score,
        "ewma_stat": ewma,
        "UCL_sample": float(ucl),
        "excess": ewma - float(ucl),
        "alarm": (ewma >= float(ucl)).astype(np.int32),
    })

    if y_true is not None:
        out["label"] = np.asarray(y_true).astype(np.int32)
        out["point_type"] = np.select(
            [
                (out["label"] == 1) & (out["alarm"] == 1),
                (out["label"] == 0) & (out["alarm"] == 1),
                (out["label"] == 1) & (out["alarm"] == 0),
                (out["label"] == 0) & (out["alarm"] == 0),
            ],
            ["TP", "FP", "FN", "TN"],
            default="UNKNOWN",
        )

    if df_meta is not None:
        for c in df_meta.columns:
            if c not in out.columns:
                out[c] = df_meta[c].to_numpy()

    out.to_csv(path, index=False, encoding="utf-8-sig")
    return out


def write_alarm_points(score_df: pd.DataFrame, path: str) -> pd.DataFrame:
    alarms = score_df[score_df["alarm"] == 1].copy()
    alarms = alarms.sort_values("excess", ascending=False)
    alarms.to_csv(path, index=False, encoding="utf-8-sig")
    return alarms


def simple_summary(score_df: pd.DataFrame, eval_set: str) -> Dict[str, float]:
    row = {
        "eval_set": eval_set,
        "n_points": int(len(score_df)),
        "n_alarm_points": int(score_df["alarm"].sum()),
        "alarm_rate": float(score_df["alarm"].mean()) if len(score_df) else float("nan"),
        "max_ewma_stat": float(score_df["ewma_stat"].max()) if len(score_df) else float("nan"),
        "mean_ewma_stat": float(score_df["ewma_stat"].mean()) if len(score_df) else float("nan"),
        "UCL_sample": float(score_df["UCL_sample"].iloc[0]) if len(score_df) else float("nan"),
    }

    if "label" in score_df.columns:
        y_true = score_df["label"].to_numpy(np.int32)
        y_pred = score_df["alarm"].to_numpy(np.int32)
        tp = int(((y_true == 1) & (y_pred == 1)).sum())
        fp = int(((y_true == 0) & (y_pred == 1)).sum())
        tn = int(((y_true == 0) & (y_pred == 0)).sum())
        fn = int(((y_true == 1) & (y_pred == 0)).sum())

        row.update({
            "TP": tp,
            "FP": fp,
            "TN": tn,
            "FN": fn,
            "FAR": float(fp) / float(max(fp + tn, 1)),
            "FDR": float(tp) / float(max(tp + fn, 1)),
            "precision": float(tp) / float(max(tp + fp, 1)),
            "recall": float(tp) / float(max(tp + fn, 1)),
        })
        p = row["precision"]
        r = row["recall"]
        row["f1"] = 2.0 * p * r / max(p + r, 1e-12)

    return row


def run_core(args, x_train_raw, eval_items):
    device = args.device
    if device == "auto":
        device = "cuda" if torch.cuda.is_available() else "cpu"
    print(f"[INFO] device={device}")

    use_saved = (not getattr(args, "force_retrain", False)) and artifacts_exist(args)

    if use_saved:
        model, prep = load_saved_model_and_preprocess(args, device)

        mean = float(prep.mean)
        std = float(prep.std)
        comp_params = prep.component_params
        t2_param = prep.t2_param
        raw_train_for_ecdf = np.asarray(prep.ecdf_raw_scores, dtype=np.float32)
        ucl = float(prep.ucl_sample)

        ecdf = ECDFTransform(raw_train_for_ecdf)
        x_train_scaled = apply_scaler(x_train_raw, mean, std)

        comp_train = score_memae(model, x_train_scaled, args.batch_size, device)
        raw_train, memae_train, t2_train, _, _ = build_scores(
            comp_train, x_train_scaled, args, params=comp_params, t2_param=t2_param
        )
        ecdf_train = ecdf.transform(raw_train)
        ewma_train = ewma_stat(ecdf_train, args.ewma_lambda)

        print("[INFO] 检测模式：发现已有模型与预处理文件，本次跳过训练。")
        print("[INFO] 如需重新训练，请运行时添加 --force_retrain")
    else:
        print("[INFO] 训练模式：未发现已有模型，或指定 --force_retrain，将训练并自动保存模型。")

        mean, std = fit_scaler(x_train_raw)
        x_train_scaled = apply_scaler(x_train_raw, mean, std)

        model = train_memae(x_train_scaled, args, device)

        comp_train = score_memae(model, x_train_scaled, args.batch_size, device)
        raw_train, memae_train, t2_train, comp_params, t2_param = build_scores(
            comp_train, x_train_scaled, args, params=None, t2_param=None
        )

        ecdf = ECDFTransform(raw_train)
        ecdf_train = ecdf.transform(raw_train)
        ewma_train = ewma_stat(ecdf_train, args.ewma_lambda)
        ucl = ucl_quantile(ewma_train, args.alpha_sample)

        # 自动保存模型和预处理参数，便于下次跳过训练直接检测。
        model_path, prep_path = model_artifact_paths(args)
        torch.save(model.state_dict(), model_path)

        prep = PreprocessState(
            feature_col=args.feature_col,
            mean=mean,
            std=std,
            component_params=comp_params,
            t2_param=t2_param,
            ecdf_raw_scores=raw_train,
            ucl_sample=float(ucl),
            args=vars(args),
        )
        with open(prep_path, "wb") as f:
            pickle.dump(prep, f)

        print(f"[OK] model saved: {model_path}")
        print(f"[OK] preprocess saved: {prep_path}")

    summary_rows = []

    train_path = os.path.join(args.out_dir, f"{args.process_name}_train_scores.csv")
    train_df = write_scores(
        path=train_path,
        df_meta=None,
        feature_col=args.feature_col,
        x_raw=x_train_raw,
        x_scaled=x_train_scaled,
        comp=comp_train,
        memae_score=memae_train,
        t2_score=t2_train,
        raw_score=raw_train,
        ecdf_score=ecdf_train,
        ewma=ewma_train,
        ucl=ucl,
        y_true=None,
    )
    write_alarm_points(train_df, os.path.join(args.out_dir, f"{args.process_name}_train_alarm_points.csv"))
    summary_rows.append(simple_summary(train_df, "train"))

    for item in eval_items:
        eval_name = item["name"]
        x_raw = item["x_raw"]
        df_meta = item.get("df_meta")
        y_true = item.get("y_true")

        x_scaled = apply_scaler(x_raw, mean, std)
        comp = score_memae(model, x_scaled, args.batch_size, device)
        raw_score, memae_score, t2_score, _, _ = build_scores(
            comp, x_scaled, args, params=comp_params, t2_param=t2_param
        )
        ecdf_score = ecdf.transform(raw_score)
        ewma = ewma_stat(ecdf_score, args.ewma_lambda)

        score_path = os.path.join(args.out_dir, f"{args.process_name}_{eval_name}_scores.csv")
        score_df = write_scores(
            path=score_path,
            df_meta=df_meta,
            feature_col=args.feature_col,
            x_raw=x_raw,
            x_scaled=x_scaled,
            comp=comp,
            memae_score=memae_score,
            t2_score=t2_score,
            raw_score=raw_score,
            ecdf_score=ecdf_score,
            ewma=ewma,
            ucl=ucl,
            y_true=y_true,
        )

        alarm_path = os.path.join(args.out_dir, f"{args.process_name}_{eval_name}_alarm_points.csv")
        alarm_df = write_alarm_points(score_df, alarm_path)

        print(f"[OK] {eval_name} scores: {score_path}")
        print(f"[OK] {eval_name} alarm points: {alarm_path} | n={len(alarm_df)}")

        summary_rows.append(simple_summary(score_df, eval_name))

    summary_df = pd.DataFrame(summary_rows)
    summary_path = os.path.join(args.out_dir, f"{args.process_name}_summary.csv")
    summary_df.to_csv(summary_path, index=False, encoding="utf-8-sig")
    print(f"[OK] summary: {summary_path}")
    print(summary_df.to_string(index=False))

def main():
    parser = argparse.ArgumentParser(description="单工序/单列逐点异常检测（MemAE + ECDF + EWMA + UCL）")

    # ---- 数据文件 ----
    parser.add_argument("--train_file", type=str, default="./train_data.xlsx",
                        help="正常工况训练数据文件路径，支持 .csv / .xlsx / .xls（默认: ./train_data.xlsx）")
    parser.add_argument("--test_file", type=str, default="./test_data.xlsx",
                        help="待检测测试数据文件路径，支持 .csv / .xlsx / .xls（默认: ./test_data.xlsx）")

    # ---- 数据相关 ----
    parser.add_argument("--feature_col", type=str, default="",
                        help="单个工序变量列名；不填则自动选择第一个数值特征列")
    parser.add_argument("--label_col", type=str, default="",
                        help="测试数据中的标签列名（可选，0=正常,1=异常，用于评估指标）")
    parser.add_argument("--process_name", type=str, default="",
                        help="工序名称（用于输出文件命名；不填则从 feature_col 自动生成）")

    # ---- 输出 ----
    parser.add_argument("--out_dir", type=str, default="./single_process_output",
                        help="输出目录（默认: ./single_process_output）")

    # ---- 模型结构 ----
    parser.add_argument("--hidden_dims", type=str, default="64,32,16",
                        help="编码器/解码器隐藏层维度（逗号分隔，默认: 64,32,16）")
    parser.add_argument("--latent_dim", type=int, default=8,
                        help="潜在空间维度（默认: 8）")
    parser.add_argument("--mem_dim", type=int, default=32,
                        help="记忆模块大小（默认: 32）")
    parser.add_argument("--dropout_p", type=float, default=0.1)
    parser.add_argument("--shrink_thres", type=float, default=0.005,
                        help="稀疏注意力收缩阈值")

    # ---- 训练 ----
    parser.add_argument("--epochs", type=int, default=80)
    parser.add_argument("--batch_size", type=int, default=256)
    parser.add_argument("--lr", type=float, default=1e-3, help="学习率")
    parser.add_argument("--weight_decay", type=float, default=1e-5)
    parser.add_argument("--seed", type=int, default=0)
    parser.add_argument("--device", type=str, default="auto",
                        help="计算设备：auto / cpu / cuda")

    # ---- 异常分数权重 ----
    parser.add_argument("--w_recon", type=float, default=1.0,
                        help="重构误差权重")
    parser.add_argument("--w_entropy", type=float, default=0.5,
                        help="注意力熵权重")
    parser.add_argument("--w_dmin", type=float, default=1.0,
                        help="最小余弦距离权重")
    parser.add_argument("--mix_w", type=float, default=0.75,
                        help="MemAE分数与T²分数的混合权重")
    parser.add_argument("--z_clip", type=float, default=8.0,
                        help="Robust Z-score 截断值")
    parser.add_argument("--ewma_lambda", type=float, default=0.30,
                        help="EWMA 平滑系数")
    parser.add_argument("--alpha_sample", type=float, default=0.01,
                        help="UCL 控制限显著性水平")

    # ---- 其他 ----
    parser.add_argument("--force_retrain", action="store_true",
                        help="强制重新训练并覆盖已有模型")

    args = parser.parse_args()

    # ---- 校验文件存在 ----
    if not os.path.isfile(args.train_file):
        raise FileNotFoundError(f"训练文件不存在: {args.train_file}")
    if not os.path.isfile(args.test_file):
        raise FileNotFoundError(f"测试文件不存在: {args.test_file}")

    # ---- 读取数据（自动识别 csv / xlsx） ----
    df_train = read_data_file(args.train_file)
    df_test = read_data_file(args.test_file)

    # ---- 自动选择特征列 ----
    if not args.feature_col:
        args.feature_col = auto_select_feature_col(df_train)
        print(f"[INFO] 未指定 --feature_col，自动选择: {args.feature_col}")

    if not args.process_name:
        args.process_name = args.feature_col.replace("/", "_").replace("\\", "_").replace(" ", "_")

    # ---- 提取数据 ----
    x_train = extract_single_column(df_train, args.feature_col)
    x_test = extract_single_column(df_test, args.feature_col)

    # ---- 标签（可选） ----
    y_test = None
    if args.label_col:
        if args.label_col not in df_test.columns:
            raise ValueError(f"label_col 不存在: {args.label_col}")
        y_test = pd.to_numeric(df_test[args.label_col], errors="coerce").fillna(0).to_numpy(np.int32)

    eval_items = [
        {"name": "test", "x_raw": x_test, "df_meta": df_test, "y_true": y_test},
    ]

    # ---- 创建输出目录 ----
    ensure_dir(args.out_dir)
    set_seed(args.seed)

    print("[INFO] 单工序逐点异常检测（MemAE + ECDF + EWMA + UCL）")
    print(f"[INFO] train_file   = {args.train_file}")
    print(f"[INFO] test_file    = {args.test_file}")
    print(f"[INFO] feature_col  = {args.feature_col}")
    print(f"[INFO] process_name = {args.process_name}")
    print(f"[INFO] label_col    = {args.label_col if args.label_col else '<未指定>'}")
    print(f"[INFO] out_dir      = {args.out_dir}")

    run_core(args, x_train, eval_items)


if __name__ == "__main__":
    main()
