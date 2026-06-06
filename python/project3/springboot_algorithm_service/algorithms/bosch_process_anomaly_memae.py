# -*- coding: utf-8 -*-
"""
Bosch process anomaly identification.

Algorithm kept from the original process anomaly script:
MemAE + reconstruction/entropy/memory-distance score + T2 + ECDF + EWMA + UCL.

Data-related adaptation:
- input is Bosch Production Line Performance train_numeric.csv or compatible numeric process data;
- Response is used as the product quality failure label when present;
- feature names like L3_S36_F3939 are parsed as line/station/process signals;
- normal samples (Response == 0) calibrate the detector, or all rows are used for unlabeled data.
"""

from __future__ import annotations

import argparse
import os
import pickle
import random
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, List, Optional, Tuple

import numpy as np
import pandas as pd
import torch
import torch.nn as nn
import torch.nn.functional as F
from torch.utils.data import DataLoader, TensorDataset


FEATURE_RE = re.compile(r"^L(?P<line>\d+)_S(?P<station>\d+)_F(?P<feature>\d+)$")


class MemAE(nn.Module):
    def __init__(self, input_dim=1, hidden_dims=(64, 32, 16), latent_dim=8, mem_dim=32, dropout_p=0.1, shrink_thres=0.005):
        super().__init__()
        self.shrink_thres = float(shrink_thres)
        enc: List[nn.Module] = []
        prev = int(input_dim)
        for h in hidden_dims:
            enc.extend([nn.Linear(prev, int(h)), nn.BatchNorm1d(int(h)), nn.ReLU(), nn.Dropout(float(dropout_p))])
            prev = int(h)
        enc.append(nn.Linear(prev, int(latent_dim)))
        self.encoder = nn.Sequential(*enc)
        self.memory = nn.Parameter(torch.randn(int(mem_dim), int(latent_dim)))
        dec: List[nn.Module] = []
        prev = int(latent_dim)
        for h in reversed(hidden_dims):
            dec.extend([nn.Linear(prev, int(h)), nn.BatchNorm1d(int(h)), nn.ReLU(), nn.Dropout(float(dropout_p))])
            prev = int(h)
        dec.append(nn.Linear(prev, int(input_dim)))
        self.decoder = nn.Sequential(*dec)

    def forward(self, x):
        z = self.encoder(x)
        cos = F.normalize(z, p=2, dim=1) @ F.normalize(self.memory, p=2, dim=1).t()
        attn = F.softmax(cos, dim=1)
        if self.shrink_thres and self.shrink_thres > 0:
            shrunk = F.relu(attn - self.shrink_thres)
            denom = shrunk.sum(dim=1, keepdim=True)
            attn = torch.where(denom > 0, shrunk / (denom + 1e-8), attn)
        z_hat = attn @ self.memory
        return self.decoder(z_hat), z, cos, attn


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
    random.seed(seed)
    np.random.seed(seed)
    torch.manual_seed(seed)
    torch.cuda.manual_seed_all(seed)


def parse_hidden_dims(s: str) -> Tuple[int, ...]:
    return tuple(int(x.strip()) for x in s.split(",") if x.strip())


def parse_station(feature_name: str) -> str:
    m = FEATURE_RE.match(feature_name)
    return f"L{m.group('line')}_S{m.group('station')}" if m else ""


def list_numeric_features(train_numeric_csv: str) -> List[str]:
    cols = pd.read_csv(train_numeric_csv, nrows=0).columns.tolist()
    return [c for c in cols if FEATURE_RE.match(c)]


def has_response_column(train_numeric_csv: str) -> bool:
    cols = pd.read_csv(train_numeric_csv, nrows=0).columns.tolist()
    return "Response" in cols


def auto_select_feature(
    train_numeric_csv: str,
    station: str = "",
    sample_rows: int = 100000,
    selection_mode: str = "response_assoc",
    min_failed_observed: int = 10,
    min_observed: int = 1000,
) -> str:
    cols = list_numeric_features(train_numeric_csv)
    if station:
        cols = [c for c in cols if parse_station(c) == station]
    if not cols:
        raise ValueError(f"No Bosch numeric features found for station={station!r}.")
    source_has_response = has_response_column(train_numeric_csv)
    sample = pd.read_csv(train_numeric_csv, usecols=cols, nrows=int(sample_rows))

    if selection_mode == "coverage" or not source_has_response:
        notna = sample.notna().sum().sort_values(ascending=False)
        return str(notna.index[0])

    y = pd.read_csv(train_numeric_csv, usecols=["Response"], nrows=int(sample_rows))["Response"].fillna(0).astype(int)
    scored = []
    for col in cols:
        x = pd.to_numeric(sample[col], errors="coerce")
        mask0 = (y == 0) & x.notna()
        mask1 = (y == 1) & x.notna()
        observed = int(x.notna().sum())
        if mask0.sum() < 50 or mask1.sum() < min_failed_observed or observed < min_observed:
            continue
        std = float(x[mask0 | mask1].std())
        if not np.isfinite(std) or std <= 1e-12:
            continue
        effect = abs(float(x[mask1].mean()) - float(x[mask0].mean())) / std
        coverage_bonus = 1e-6 * float(x.notna().sum())
        scored.append((col, effect + coverage_bonus, effect, int(mask1.sum()), observed))
    if not scored:
        notna = sample.notna().sum().sort_values(ascending=False)
        return str(notna.index[0])
    scored.sort(key=lambda z: z[1], reverse=True)
    best = scored[0]
    print(
        "[INFO] auto-selected by response association: "
        f"feature={best[0]}, effect={best[2]:.4f}, failed_observed={best[3]}, observed={best[4]}"
    )
    return str(best[0])


def load_bosch_feature_table(train_numeric_csv: str, feature_col: str, max_rows: int = 200000) -> pd.DataFrame:
    cols = pd.read_csv(train_numeric_csv, nrows=0).columns.tolist()
    usecols = [feature_col]
    if "Id" in cols:
        usecols.insert(0, "Id")
    if "Response" in cols:
        usecols.append("Response")
    df = pd.read_csv(train_numeric_csv, usecols=usecols, nrows=max_rows if max_rows > 0 else None)
    if "Id" not in df.columns:
        df.insert(0, "Id", np.arange(len(df), dtype=np.int64))
    if "Response" not in df.columns:
        df["Response"] = 0
    df[feature_col] = pd.to_numeric(df[feature_col], errors="coerce").ffill().bfill().fillna(0.0)
    df["Response"] = pd.to_numeric(df["Response"], errors="coerce").fillna(0).astype(int)
    return df


def fit_scaler(x):
    mean = float(np.mean(x))
    std = float(np.std(x, ddof=1))
    if not np.isfinite(std) or std <= 1e-8:
        std = 1.0
    return mean, std


def apply_scaler(x, mean, std):
    return ((np.asarray(x, dtype=np.float32) - mean) / std).astype(np.float32)


def robust_median_mad(x, eps=1e-8):
    x = np.asarray(x, dtype=np.float64)
    x = x[np.isfinite(x)]
    med = float(np.median(x)) if x.size else 0.0
    mad = float(np.median(np.abs(x - med)) * 1.4826) if x.size else 1.0
    if not np.isfinite(mad) or mad < eps:
        mad = max(float(np.std(x)) if x.size else 1.0, 1.0)
    return med, mad


def robust_z(x, med, scale, clip):
    z = (np.asarray(x, dtype=np.float64) - med) / max(scale, 1e-8)
    return np.clip(z, -clip, clip).astype(np.float32)


class ECDFTransform:
    def __init__(self, cal_scores):
        cal = np.asarray(cal_scores, dtype=np.float64)
        cal = np.sort(cal[np.isfinite(cal)])
        if cal.size < 10:
            raise ValueError("Too few calibration scores.")
        self.sorted = cal
        self.n = int(cal.size)

    def transform(self, scores, eps=1e-12):
        idx = np.searchsorted(self.sorted, np.asarray(scores, dtype=np.float64), side="right")
        p = (self.n - idx + 1).astype(np.float64) / float(self.n + 1)
        return (-np.log(p + eps)).astype(np.float32)


def ewma_stat(x, lam, z0=0.0):
    y = np.empty_like(np.asarray(x, dtype=np.float32), dtype=np.float32)
    yt = float(z0)
    for i, v in enumerate(x):
        yt = (1.0 - lam) * yt + lam * float(v)
        y[i] = yt
    return y


def ucl_quantile(x, alpha):
    return float(np.quantile(np.asarray(x, dtype=np.float64), 1.0 - alpha))


def make_model(args):
    return MemAE(1, parse_hidden_dims(args.hidden_dims), args.latent_dim, args.mem_dim, args.dropout_p, args.shrink_thres)


def train_memae(x_train_scaled, args, device):
    x = torch.tensor(x_train_scaled, dtype=torch.float32)
    loader = DataLoader(TensorDataset(x), batch_size=args.batch_size, shuffle=True, drop_last=len(x) > args.batch_size)
    model = make_model(args).to(device)
    opt = torch.optim.AdamW(model.parameters(), lr=args.lr, weight_decay=args.weight_decay)
    model.train()
    for ep in range(1, args.epochs + 1):
        losses = []
        for (xb,) in loader:
            xb = xb.to(device)
            x_hat, _, _, attn = model(xb)
            recon = F.mse_loss(x_hat, xb)
            entropy = -torch.sum(attn * torch.log(attn + 1e-12), dim=1).mean()
            loss = recon + 1e-4 * entropy
            opt.zero_grad(set_to_none=True)
            loss.backward()
            opt.step()
            losses.append(float(loss.detach().cpu()))
        if ep == 1 or ep == args.epochs or ep % max(1, args.epochs // 5) == 0:
            print(f"[train] epoch={ep:04d}/{args.epochs}, loss={np.mean(losses):.6f}")
    return model


@torch.no_grad()
def score_memae(model, x_scaled, batch_size, device):
    loader = DataLoader(TensorDataset(torch.tensor(x_scaled, dtype=torch.float32)), batch_size=batch_size, shuffle=False)
    model.eval()
    recon, entropy, dmin, xhat = [], [], [], []
    for (xb,) in loader:
        xb = xb.to(device)
        yh, _, cos, attn = model(xb)
        recon.append(torch.mean((yh - xb) ** 2, dim=1).cpu().numpy())
        entropy.append((-torch.sum(attn * torch.log(attn + 1e-12), dim=1)).cpu().numpy())
        dmin.append((1.0 - torch.max(cos, dim=1).values).cpu().numpy())
        xhat.append(yh.cpu().numpy())
    return {
        "recon": np.concatenate(recon).astype(np.float32),
        "entropy": np.concatenate(entropy).astype(np.float32),
        "dmin": np.concatenate(dmin).astype(np.float32),
        "x_hat_scaled": np.concatenate(xhat).astype(np.float32).reshape(-1, 1),
    }


def build_scores(comp, x_scaled, args, params=None, t2_param=None):
    if params is None:
        params = {k: robust_median_mad(comp[k]) for k in ["recon", "entropy", "dmin"]}
    rz = robust_z(comp["recon"], *params["recon"], args.z_clip)
    ez = robust_z(comp["entropy"], *params["entropy"], args.z_clip)
    dz = robust_z(comp["dmin"], *params["dmin"], args.z_clip)
    memae = (args.w_recon * rz + args.w_entropy * ez + args.w_dmin * dz) / max(args.w_recon + args.w_entropy + args.w_dmin, 1e-12)
    t2_raw = (np.asarray(x_scaled).reshape(-1) ** 2).astype(np.float32)
    if t2_param is None:
        t2_param = robust_median_mad(t2_raw)
    t2 = robust_z(t2_raw, *t2_param, args.z_clip)
    raw = (args.mix_w * memae + (1.0 - args.mix_w) * t2).astype(np.float32)
    return raw, memae.astype(np.float32), t2.astype(np.float32), params, t2_param


def confusion(y_true, y_pred):
    tp = int(((y_true == 1) & (y_pred == 1)).sum())
    fp = int(((y_true == 0) & (y_pred == 1)).sum())
    tn = int(((y_true == 0) & (y_pred == 0)).sum())
    fn = int(((y_true == 1) & (y_pred == 0)).sum())
    precision = tp / max(tp + fp, 1)
    recall = tp / max(tp + fn, 1)
    f1 = 2 * precision * recall / max(precision + recall, 1e-12)
    return {"TP": tp, "FP": fp, "TN": tn, "FN": fn, "precision": precision, "recall": recall, "f1": f1}


def main():
    p = argparse.ArgumentParser(description="Bosch MemAE process anomaly detection")
    p.add_argument("--train_numeric_csv", required=True, help="Path to Bosch train_numeric.csv")
    p.add_argument("--feature_col", default="", help="Bosch feature, e.g. L3_S36_F3939. Empty means auto-select.")
    p.add_argument("--station", default="", help="Optional station filter, e.g. L3_S36")
    p.add_argument("--selection_mode", choices=["response_assoc", "coverage"], default="response_assoc")
    p.add_argument("--min_failed_observed", type=int, default=10)
    p.add_argument("--min_observed", type=int, default=1000)
    p.add_argument("--max_rows", type=int, default=200000)
    p.add_argument("--out_dir", default="bosch_anomaly_output")
    p.add_argument("--hidden_dims", default="64,32,16")
    p.add_argument("--latent_dim", type=int, default=8)
    p.add_argument("--mem_dim", type=int, default=32)
    p.add_argument("--dropout_p", type=float, default=0.1)
    p.add_argument("--shrink_thres", type=float, default=0.005)
    p.add_argument("--epochs", type=int, default=60)
    p.add_argument("--batch_size", type=int, default=256)
    p.add_argument("--lr", type=float, default=1e-3)
    p.add_argument("--weight_decay", type=float, default=1e-5)
    p.add_argument("--seed", type=int, default=0)
    p.add_argument("--device", default="auto")
    p.add_argument("--w_recon", type=float, default=1.0)
    p.add_argument("--w_entropy", type=float, default=0.5)
    p.add_argument("--w_dmin", type=float, default=1.0)
    p.add_argument("--mix_w", type=float, default=0.75)
    p.add_argument("--z_clip", type=float, default=8.0)
    p.add_argument("--ewma_lambda", type=float, default=0.30)
    p.add_argument("--alpha_sample", type=float, default=0.01)
    args = p.parse_args()

    os.makedirs(args.out_dir, exist_ok=True)
    set_seed(args.seed)
    if not args.feature_col:
        args.feature_col = auto_select_feature(
            args.train_numeric_csv,
            args.station,
            args.max_rows,
            args.selection_mode,
            args.min_failed_observed,
            args.min_observed,
        )
    args.process_name = args.feature_col
    station = parse_station(args.feature_col)
    print(f"[INFO] feature={args.feature_col}, station={station}")

    df = load_bosch_feature_table(args.train_numeric_csv, args.feature_col, args.max_rows)
    x_all = df[args.feature_col].to_numpy(np.float32).reshape(-1, 1)
    y = df["Response"].to_numpy(np.int32)
    x_train = x_all[y == 0]
    if len(x_train) < 100:
        raise ValueError("Too few normal samples for calibration.")

    device = "cuda" if args.device == "auto" and torch.cuda.is_available() else ("cpu" if args.device == "auto" else args.device)
    mean, std = fit_scaler(x_train)
    x_train_scaled = apply_scaler(x_train, mean, std)
    model = train_memae(x_train_scaled, args, device)
    comp_train = score_memae(model, x_train_scaled, args.batch_size, device)
    raw_train, _, _, params, t2_param = build_scores(comp_train, x_train_scaled, args)
    ecdf = ECDFTransform(raw_train)
    ucl = ucl_quantile(ewma_stat(ecdf.transform(raw_train), args.ewma_lambda), args.alpha_sample)

    x_all_scaled = apply_scaler(x_all, mean, std)
    comp = score_memae(model, x_all_scaled, args.batch_size, device)
    raw, memae, t2, _, _ = build_scores(comp, x_all_scaled, args, params, t2_param)
    ewma = ewma_stat(ecdf.transform(raw), args.ewma_lambda)
    alarm = (ewma >= ucl).astype(np.int32)

    out = pd.DataFrame({
        "Id": df["Id"].to_numpy(),
        "station": station,
        "feature_col": args.feature_col,
        args.feature_col: x_all.reshape(-1),
        "Response": y,
        "recon": comp["recon"],
        "entropy": comp["entropy"],
        "dmin": comp["dmin"],
        "memae_score": memae,
        "t2_score": t2,
        "raw_mix_score": raw,
        "ewma_stat": ewma,
        "UCL_sample": ucl,
        "excess": ewma - ucl,
        "alarm": alarm,
    })
    out.to_csv(Path(args.out_dir) / f"{args.feature_col}_scores.csv", index=False, encoding="utf-8-sig")
    out[out["alarm"] == 1].sort_values("excess", ascending=False).to_csv(
        Path(args.out_dir) / f"{args.feature_col}_alarm_points.csv", index=False, encoding="utf-8-sig"
    )
    summary = {
        "feature_col": args.feature_col,
        "station": station,
        "n_points": int(len(out)),
        "n_failed_response": int(y.sum()),
        "n_alarm_points": int(alarm.sum()),
        "alarm_rate": float(alarm.mean()),
        "UCL_sample": float(ucl),
    }
    summary.update(confusion(y, alarm))
    pd.DataFrame([summary]).to_csv(Path(args.out_dir) / f"{args.feature_col}_summary.csv", index=False, encoding="utf-8-sig")
    with open(Path(args.out_dir) / f"{args.feature_col}_preprocess.pkl", "wb") as f:
        pickle.dump(PreprocessState(args.feature_col, mean, std, params, t2_param, raw_train, ucl, vars(args)), f)
    torch.save(model.state_dict(), Path(args.out_dir) / f"{args.feature_col}_memae.pt")
    print(pd.DataFrame([summary]).to_string(index=False))


if __name__ == "__main__":
    main()
