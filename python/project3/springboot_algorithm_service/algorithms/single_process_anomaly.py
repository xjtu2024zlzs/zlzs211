"""Single-process / single-column pointwise anomaly detection using MemAE + Multi-score + ECDF + EWMA + UCL.

Algorithm:
- Train a Memory-augmented Autoencoder (MemAE) on normal-condition process data.
- Compute multiple anomaly scores: reconstruction error, attention entropy, minimum cosine distance, and T².
- Combine scores with robust z-score normalization.
- Transform combined scores via ECDF (empirical CDF) + EWMA smoothing + UCL threshold.
- Output pointwise anomaly scores and alarm flags for every sample.

Designed for FastAPI service calls. No print statements in core logic.
"""

from __future__ import annotations

import os
import pickle
import random
import uuid
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple, Union

import numpy as np
import pandas as pd

import torch
import torch.nn as nn
import torch.nn.functional as F
from torch.utils.data import DataLoader, TensorDataset


# ---------------------------------------------------------------------------
# MemAE model
# ---------------------------------------------------------------------------

class MemAE(nn.Module):
    """Memory-augmented Autoencoder with cosine-similarity attention and sparse addressing."""

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


# ---------------------------------------------------------------------------
# Preprocess state (for save / load)
# ---------------------------------------------------------------------------

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


# ---------------------------------------------------------------------------
# TEP meta columns (for auto feature selection)
# ---------------------------------------------------------------------------

_TEP_META_COLS = {"faultNumber", "simulationRun", "sample"}


# ---------------------------------------------------------------------------
# Utility helpers
# ---------------------------------------------------------------------------

def _set_seed(seed: int) -> None:
    seed = int(seed)
    random.seed(seed)
    np.random.seed(seed)
    torch.manual_seed(seed)
    torch.cuda.manual_seed_all(seed)


def _parse_hidden_dims(raw: Union[str, Tuple[int, ...]]) -> Tuple[int, ...]:
    if isinstance(raw, tuple):
        return raw
    return tuple(int(x.strip()) for x in str(raw).split(",") if x.strip())


def _get_device(preferred: str) -> str:
    if preferred == "auto" or not preferred:
        return "cuda" if torch.cuda.is_available() else "cpu"
    return preferred


def _read_data_file(filepath: str) -> pd.DataFrame:
    ext = os.path.splitext(filepath)[1].lower()
    if ext in (".xlsx", ".xls"):
        return pd.read_excel(filepath)
    return pd.read_csv(filepath)


def _extract_single_column(df: pd.DataFrame, col: str) -> np.ndarray:
    if col not in df.columns:
        raise ValueError(f"Column '{col}' not found. Available columns (first 30): {list(df.columns)[:30]}")
    x = pd.to_numeric(df[col], errors="coerce").to_numpy(np.float32)
    if not np.all(np.isfinite(x)):
        x = pd.Series(x).replace([np.inf, -np.inf], np.nan).ffill().bfill().to_numpy(np.float32)
    return x.reshape(-1, 1)


def _auto_select_feature_col(df: pd.DataFrame) -> str:
    for c in df.columns:
        if c in _TEP_META_COLS:
            continue
        s = pd.to_numeric(df[c], errors="coerce")
        if s.notna().sum() > 0:
            return c
    raise ValueError("No usable numeric feature column found.")


# ---------------------------------------------------------------------------
# Preprocessing
# ---------------------------------------------------------------------------

def _fit_scaler(x_train: np.ndarray) -> Tuple[float, float]:
    mean = float(np.mean(x_train))
    std = float(np.std(x_train, ddof=1))
    if not np.isfinite(std) or std <= 1e-8:
        std = float(np.std(x_train, ddof=0))
    if not np.isfinite(std) or std <= 1e-8:
        std = 1.0
    return mean, std


def _apply_scaler(x: np.ndarray, mean: float, std: float) -> np.ndarray:
    return ((np.asarray(x, dtype=np.float32) - float(mean)) / float(std)).astype(np.float32)


def _robust_median_mad(x: np.ndarray, eps: float = 1e-8) -> Tuple[float, float]:
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


def _robust_z(x: np.ndarray, med: float, scale: float, clip: float) -> np.ndarray:
    z = (np.asarray(x, dtype=np.float64) - float(med)) / max(float(scale), 1e-8)
    z = np.clip(z, -float(clip), float(clip))
    return z.astype(np.float32, copy=False)


# ---------------------------------------------------------------------------
# ECDF transform
# ---------------------------------------------------------------------------

class ECDFTransform:
    def __init__(self, cal_scores: np.ndarray):
        cal = np.asarray(cal_scores, dtype=np.float64)
        cal = cal[np.isfinite(cal)]
        if cal.size < 10:
            raise ValueError("Too few training scores to construct ECDF.")
        self.sorted = np.sort(cal)
        self.n = int(self.sorted.size)

    def transform(self, scores: np.ndarray, eps: float = 1e-12) -> np.ndarray:
        s = np.asarray(scores, dtype=np.float64)
        idx = np.searchsorted(self.sorted, s, side="right")
        ge = (self.n - idx).astype(np.int64, copy=False)
        p = (ge + 1).astype(np.float64) / float(self.n + 1)
        return (-np.log(p + float(eps))).astype(np.float32, copy=False)


# ---------------------------------------------------------------------------
# EWMA and UCL
# ---------------------------------------------------------------------------

def _ewma_stat(x: np.ndarray, lam: float, z0: float = 0.0) -> np.ndarray:
    x = np.asarray(x, dtype=np.float32)
    y = np.empty_like(x, dtype=np.float32)
    yt = float(z0)
    lam = float(lam)
    for i in range(len(x)):
        yt = (1.0 - lam) * yt + lam * float(x[i])
        y[i] = yt
    return y


def _ucl_quantile(x: np.ndarray, alpha: float) -> float:
    x = np.asarray(x, dtype=np.float64)
    x = x[np.isfinite(x)]
    if x.size < 10:
        raise RuntimeError("Too few training statistics to calibrate UCL.")
    return float(np.quantile(x, 1.0 - float(alpha)))


# ---------------------------------------------------------------------------
# MemAE training
# ---------------------------------------------------------------------------

def _train_memae(
    x_train_scaled: np.ndarray,
    hidden_dims: Tuple[int, ...],
    latent_dim: int,
    mem_dim: int,
    dropout_p: float,
    shrink_thres: float,
    batch_size: int,
    epochs: int,
    lr: float,
    weight_decay: float,
    device: str,
) -> MemAE:
    x = torch.tensor(x_train_scaled, dtype=torch.float32)
    ds = TensorDataset(x)
    drop_last = len(ds) > int(batch_size)
    loader = DataLoader(ds, batch_size=int(batch_size), shuffle=True, drop_last=drop_last)

    model = MemAE(
        input_dim=1,
        hidden_dims=hidden_dims,
        latent_dim=latent_dim,
        mem_dim=mem_dim,
        dropout_p=dropout_p,
        shrink_thres=shrink_thres,
    ).to(device)

    opt = torch.optim.AdamW(model.parameters(), lr=lr, weight_decay=weight_decay)
    model.train()

    for ep in range(1, epochs + 1):
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

    model.eval()
    return model


# ---------------------------------------------------------------------------
# MemAE scoring
# ---------------------------------------------------------------------------

@torch.no_grad()
def _score_memae(model: MemAE, x_scaled: np.ndarray, batch_size: int, device: str) -> Dict[str, np.ndarray]:
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


# ---------------------------------------------------------------------------
# Build composite scores
# ---------------------------------------------------------------------------

def _build_scores(
    comp: Dict[str, np.ndarray],
    x_scaled: np.ndarray,
    params: Optional[Dict[str, Tuple[float, float]]],
    t2_param: Optional[Tuple[float, float]],
    w_recon: float,
    w_entropy: float,
    w_dmin: float,
    mix_w: float,
    z_clip: float,
) -> Tuple[np.ndarray, np.ndarray, np.ndarray, Dict[str, Tuple[float, float]], Tuple[float, float]]:
    if params is None:
        params = {
            "recon": _robust_median_mad(comp["recon"]),
            "entropy": _robust_median_mad(comp["entropy"]),
            "dmin": _robust_median_mad(comp["dmin"]),
        }

    recon_z = _robust_z(comp["recon"], params["recon"][0], params["recon"][1], z_clip)
    entropy_z = _robust_z(comp["entropy"], params["entropy"][0], params["entropy"][1], z_clip)
    dmin_z = _robust_z(comp["dmin"], params["dmin"][0], params["dmin"][1], z_clip)

    weight_sum = max(w_recon + w_entropy + w_dmin, 1e-12)
    memae_score = (w_recon * recon_z + w_entropy * entropy_z + w_dmin * dmin_z) / weight_sum

    t2_raw = (np.asarray(x_scaled).reshape(-1) ** 2).astype(np.float32)
    if t2_param is None:
        t2_param = _robust_median_mad(t2_raw)
    t2_score = _robust_z(t2_raw, t2_param[0], t2_param[1], z_clip)

    raw_mix_score = (mix_w * memae_score + (1.0 - mix_w) * t2_score).astype(np.float32)
    return raw_mix_score, memae_score.astype(np.float32), t2_score.astype(np.float32), params, t2_param


# ---------------------------------------------------------------------------
# Write score CSV
# ---------------------------------------------------------------------------

def _write_scores(
    path: str,
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

    out.to_csv(path, index=False, encoding="utf-8-sig")
    return out


def _write_alarm_points(score_df: pd.DataFrame, path: str) -> pd.DataFrame:
    alarms = score_df[score_df["alarm"] == 1].copy()
    alarms = alarms.sort_values("excess", ascending=False)
    alarms.to_csv(path, index=False, encoding="utf-8-sig")
    return alarms


# ---------------------------------------------------------------------------
# Main API function
# ---------------------------------------------------------------------------

def run_single_process_anomaly(
    *,
    train_path: str,
    test_path: str,
    output_dir: Union[str, Path],
    output_path: Optional[str] = None,
    task_id: Optional[str] = None,
    dataset_id: Optional[str] = None,
    task_type: str = "SINGLE_PROCESS_ANOMALY",
    # --- data ---
    feature_col: Optional[str] = None,
    process_name: Optional[str] = None,
    # --- MemAE architecture ---
    hidden_dims: Union[str, Tuple[int, ...]] = "64,32,16",
    latent_dim: int = 8,
    mem_dim: int = 32,
    dropout_p: float = 0.1,
    shrink_thres: float = 0.005,
    # --- training ---
    epochs: int = 80,
    batch_size: int = 256,
    lr: float = 1e-3,
    weight_decay: float = 1e-5,
    seed: int = 0,
    device: str = "auto",
    force_retrain: bool = False,
    # --- anomaly score weights ---
    w_recon: float = 1.0,
    w_entropy: float = 0.5,
    w_dmin: float = 1.0,
    mix_w: float = 0.75,
    z_clip: float = 8.0,
    # --- EWMA / UCL ---
    ewma_lambda: float = 0.30,
    alpha_sample: float = 0.01,
) -> Dict[str, Any]:
    """Run single-process pointwise anomaly detection (MemAE + ECDF + EWMA + UCL)."""

    # Validate input files
    if not train_path:
        raise ValueError("trainPath cannot be empty.")
    if not test_path:
        raise ValueError("testPath cannot be empty.")
    if not Path(train_path).exists():
        raise FileNotFoundError(f"Training file not found: {train_path}")
    if not Path(test_path).exists():
        raise FileNotFoundError(f"Test file not found: {test_path}")

    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    resolved_device = _get_device(device)
    _set_seed(seed)

    hidden_dims_tuple = _parse_hidden_dims(hidden_dims)

    # Read data
    df_train = _read_data_file(train_path)
    df_test = _read_data_file(test_path)

    # Auto-detect feature column
    if not feature_col:
        feature_col = _auto_select_feature_col(df_train)
    if not process_name:
        process_name = feature_col.replace("/", "_").replace("\\", "_").replace(" ", "_")

    # Extract single-column data
    x_train_raw = _extract_single_column(df_train, feature_col)
    x_test_raw = _extract_single_column(df_test, feature_col)

    # Check for saved artifacts
    model_path = output_dir / f"{process_name}_memae.pt"
    prep_path = output_dir / f"{process_name}_preprocess.pkl"
    use_saved = (not force_retrain) and model_path.exists() and prep_path.exists()

    if use_saved:
        # Load saved model
        with open(prep_path, "rb") as f:
            prep: PreprocessState = pickle.load(f)

        mean = float(prep.mean)
        std = float(prep.std)
        comp_params = prep.component_params
        t2_param = prep.t2_param
        raw_train_for_ecdf = np.asarray(prep.ecdf_raw_scores, dtype=np.float32)
        ucl = float(prep.ucl_sample)

        model = MemAE(
            input_dim=1,
            hidden_dims=hidden_dims_tuple,
            latent_dim=latent_dim,
            mem_dim=mem_dim,
            dropout_p=dropout_p,
            shrink_thres=shrink_thres,
        ).to(resolved_device)
        state = torch.load(model_path, map_location=resolved_device)
        model.load_state_dict(state)
        model.eval()

        x_train_scaled = _apply_scaler(x_train_raw, mean, std)

        comp_train = _score_memae(model, x_train_scaled, batch_size, resolved_device)
        raw_train, memae_train, t2_train, _, _ = _build_scores(
            comp_train, x_train_scaled, comp_params, t2_param,
            w_recon, w_entropy, w_dmin, mix_w, z_clip,
        )

        ecdf = ECDFTransform(raw_train_for_ecdf)
        ecdf_train = ecdf.transform(raw_train)
        ewma_train = _ewma_stat(ecdf_train, ewma_lambda)

        training_mode = "loaded"
    else:
        # Train from scratch
        mean, std = _fit_scaler(x_train_raw)
        x_train_scaled = _apply_scaler(x_train_raw, mean, std)

        model = _train_memae(
            x_train_scaled, hidden_dims_tuple, latent_dim, mem_dim,
            dropout_p, shrink_thres, batch_size, epochs, lr, weight_decay,
            resolved_device,
        )

        comp_train = _score_memae(model, x_train_scaled, batch_size, resolved_device)
        raw_train, memae_train, t2_train, comp_params, t2_param = _build_scores(
            comp_train, x_train_scaled, None, None,
            w_recon, w_entropy, w_dmin, mix_w, z_clip,
        )

        ecdf = ECDFTransform(raw_train)
        ecdf_train = ecdf.transform(raw_train)
        ewma_train = _ewma_stat(ecdf_train, ewma_lambda)
        ucl = _ucl_quantile(ewma_train, alpha_sample)

        # Save model and preprocessing state
        torch.save(model.state_dict(), model_path)

        prep = PreprocessState(
            feature_col=feature_col,
            mean=mean,
            std=std,
            component_params=comp_params,
            t2_param=t2_param,
            ecdf_raw_scores=raw_train,
            ucl_sample=float(ucl),
            args={},
        )
        with open(prep_path, "wb") as f:
            pickle.dump(prep, f)

        training_mode = "trained"

    # --- Write train scores ---
    train_score_path = output_dir / f"{process_name}_train_scores.csv"
    _write_scores(
        str(train_score_path), feature_col, x_train_raw, x_train_scaled,
        comp_train, memae_train, t2_train, raw_train, ecdf_train,
        ewma_train, ucl,
    )
    train_alarm_path = output_dir / f"{process_name}_train_alarm_points.csv"
    train_alarm_df = _write_alarm_points(
        pd.read_csv(train_score_path, encoding="utf-8-sig"), str(train_alarm_path),
    )

    # --- Test scoring ---
    x_test_scaled = _apply_scaler(x_test_raw, mean, std)
    comp_test = _score_memae(model, x_test_scaled, batch_size, resolved_device)
    raw_test, memae_test, t2_test, _, _ = _build_scores(
        comp_test, x_test_scaled, comp_params, t2_param,
        w_recon, w_entropy, w_dmin, mix_w, z_clip,
    )
    ecdf_test = ecdf.transform(raw_test)
    ewma_test = _ewma_stat(ecdf_test, ewma_lambda)

    test_score_path = output_dir / f"{process_name}_test_scores.csv"
    test_score_df = _write_scores(
        str(test_score_path), feature_col, x_test_raw, x_test_scaled,
        comp_test, memae_test, t2_test, raw_test, ecdf_test,
        ewma_test, ucl,
    )
    test_alarm_path = output_dir / f"{process_name}_test_alarm_points.csv"
    test_alarm_df = _write_alarm_points(test_score_df, str(test_alarm_path))

    # --- Summary ---
    n_train = int(len(x_train_raw))
    n_test = int(len(x_test_raw))
    n_train_alarm = int(len(train_alarm_df))
    n_test_alarm = int(len(test_alarm_df))

    # --- Return response ---
    return {
        "taskId": task_id,
        "taskType": task_type,
        "datasetId": dataset_id,
        "status": "SUCCESS",
        "message": "single process anomaly detection completed",
        "method": "MemAE_ECDF_EWMA_UCL",
        "trainingMode": training_mode,
        "trainPath": str(Path(train_path)).replace("\\", "/"),
        "testPath": str(Path(test_path)).replace("\\", "/"),
        "outputDir": str(output_dir).replace("\\", "/"),
        "trainScorePath": str(train_score_path).replace("\\", "/"),
        "trainAlarmPath": str(train_alarm_path).replace("\\", "/"),
        "testScorePath": str(test_score_path).replace("\\", "/"),
        "testAlarmPath": str(test_alarm_path).replace("\\", "/"),
        "modelPath": str(model_path).replace("\\", "/"),
        "preprocessPath": str(prep_path).replace("\\", "/"),
        "featureCol": feature_col,
        "processName": process_name,
        "trainRows": n_train,
        "testRows": n_test,
        "trainAlarmCount": n_train_alarm,
        "testAlarmCount": n_test_alarm,
        "trainAlarmRate": float(n_train_alarm / max(n_train, 1)),
        "testAlarmRate": float(n_test_alarm / max(n_test, 1)),
        "ucl": float(ucl),
        "epochs": epochs,
        "batchSize": batch_size,
        "hiddenDims": list(hidden_dims_tuple),
        "latentDim": latent_dim,
        "memDim": mem_dim,
        "ewmaLambda": ewma_lambda,
        "alphaSample": alpha_sample,
    }
