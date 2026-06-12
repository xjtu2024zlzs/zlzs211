# -*- coding: utf-8 -*-
"""
Bosch global key quality characteristic mining.

Algorithm kept from the original KQC script:
COKE/NOTEARS-style causal structure learning + station-forward constraint
+ stability selection + target incoming-edge output.

Data-related adaptation:
- input is Bosch Production Line Performance train_numeric.csv;
- feature names L#_S##_F#### are parsed into line/station/process order;
- Response is used as the global quality failure KQC target;
- a compact feature subset is selected by non-missing coverage per station.
"""

from __future__ import annotations

import argparse
import re
from pathlib import Path
from typing import Dict, List, Tuple

import numpy as np
import pandas as pd
from scipy.linalg import expm
from scipy.optimize import minimize
from sklearn.impute import SimpleImputer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import roc_auc_score
from sklearn.model_selection import StratifiedKFold
from sklearn.preprocessing import StandardScaler


FEATURE_RE = re.compile(r"^L(?P<line>\d+)_S(?P<station>\d+)_F(?P<feature>\d+)$")
KQC_MISSING_RESPONSE_MESSAGE = (
    "当前 train_numeric.csv 缺少响应结果列，无法直接进行关键质量特性挖掘。"
    "请使用包含响应结果列的训练数据；如果没有响应结果列，请提供已完成的 KQC输出目录用于关键工序识别。"
    "该目录需要包含 bosch_adj_matrix_selected.csv 和 bosch_edge_frequency.csv。"
)
KQC_MISSING_FEATURE_MESSAGE = "当前 train_numeric.csv 缺少工序特征列，列名需要类似 L3_S36_F3939。"


def parse_feature(col: str) -> Tuple[int, int, int]:
    m = FEATURE_RE.match(col)
    if not m:
        return 99, 999, 99999
    return int(m.group("line")), int(m.group("station")), int(m.group("feature"))


def station_name(col: str) -> str:
    line, station, _ = parse_feature(col)
    return f"L{line}_S{station}"


def select_bosch_features(
    train_numeric_csv: str,
    max_rows: int,
    max_features: int,
    per_station: int,
    selection_mode: str = "response_assoc",
) -> List[str]:
    sample = pd.read_csv(train_numeric_csv, nrows=max_rows if max_rows > 0 else None)
    feature_cols = [c for c in sample.columns if FEATURE_RE.match(c)]
    if not feature_cols:
        raise ValueError(KQC_MISSING_FEATURE_MESSAGE)
    if selection_mode != "coverage" and "Response" not in sample.columns:
        raise ValueError(KQC_MISSING_RESPONSE_MESSAGE)
    coverage = sample[feature_cols].notna().sum()

    if selection_mode == "coverage":
        ranked = coverage.sort_values(ascending=False).index.tolist()
    else:
        y = pd.to_numeric(sample["Response"], errors="coerce").fillna(0).astype(int)
        rows = []
        for col in feature_cols:
            x = pd.to_numeric(sample[col], errors="coerce")
            mask0 = (y == 0) & x.notna()
            mask1 = (y == 1) & x.notna()
            if mask0.sum() < 20 or mask1.sum() < 2:
                continue
            std = float(x[mask0 | mask1].std())
            if not np.isfinite(std) or std <= 1e-12:
                continue
            score = abs(float(x[mask1].mean()) - float(x[mask0].mean())) / std
            # Tiny coverage bonus breaks ties toward more stable signals.
            score += 1e-6 * float(coverage[col])
            rows.append((col, score))
        if not rows:
            ranked = coverage.sort_values(ascending=False).index.tolist()
        else:
            ranked = [col for col, _ in sorted(rows, key=lambda z: z[1], reverse=True)]

    selected: List[str] = []
    station_counts: Dict[str, int] = {}
    for col in ranked:
        st = station_name(col)
        if station_counts.get(st, 0) >= per_station:
            continue
        selected.append(col)
        station_counts[st] = station_counts.get(st, 0) + 1
        if len(selected) >= max_features:
            break
    return sorted(selected, key=parse_feature)


def load_bosch_matrix(train_numeric_csv: str, selected_features: List[str], max_rows: int, target_col: str = "Response") -> pd.DataFrame:
    usecols = ["Id"] + selected_features + [target_col]
    columns = set(pd.read_csv(train_numeric_csv, nrows=0).columns)
    if target_col not in columns:
        raise ValueError(KQC_MISSING_RESPONSE_MESSAGE)
    df = pd.read_csv(train_numeric_csv, usecols=usecols, nrows=max_rows if max_rows > 0 else None)
    for c in selected_features + [target_col]:
        df[c] = pd.to_numeric(df[c], errors="coerce")
    return df


def acyclicity(W):
    return np.trace(expm(W * W)) - W.shape[0]


def loss_and_grad(w_vec, X, obs_mask, lambda1, rho, alpha):
    n, d = X.shape
    W = w_vec.reshape(d, d)
    np.fill_diagonal(W, 0.0)
    R = X - X @ W
    obs_count = max(obs_mask.sum(), 1)
    f_recon = 0.5 * np.sum((R * R) * obs_mask) / obs_count
    f_l1 = lambda1 * np.sum(np.abs(W))
    h = acyclicity(W)
    f_aug = 0.5 * rho * (h**2) + alpha * h
    f = f_recon + f_l1 + f_aug

    Grad = -(X.T @ (R * obs_mask)) / obs_count
    Grad += lambda1 * np.sign(W)
    E = expm(W * W)
    Grad += (rho * h + alpha) * (2 * W * E.T)
    np.fill_diagonal(Grad, 0.0)
    return f, Grad.ravel()


def solve_coke_forward(X, obs_mask, hard_block, lambda1=1e-3, max_iter=80, rho_max=1e16, h_tol=1e-8, W_init=None):
    n, d = X.shape
    w = np.zeros((d, d)).ravel() if W_init is None else W_init.ravel().copy()
    rho = 1.0
    alpha = 0.0

    def obj(v):
        return loss_and_grad(v, X, obs_mask, lambda1, rho, alpha)

    for it in range(max_iter):
        res = minimize(lambda v: obj(v)[0], w, method="L-BFGS-B", jac=lambda v: obj(v)[1], options={"maxiter": 200})
        w = res.x
        W = w.reshape(d, d)
        W[hard_block > 0] = 0.0
        np.fill_diagonal(W, 0.0)
        w = W.ravel()
        h = acyclicity(W)
        print(f"[ITER {it:03d}] h(W)={h:.3e} obj={res.fun:.6f} max|W|={np.max(np.abs(W)):.4f}")
        if h <= h_tol or rho > rho_max:
            break
        rho *= 10.0
        alpha += rho * h
    W = w.reshape(d, d)
    np.fill_diagonal(W, 0.0)
    return W


def stability_selection(X, obs_mask, hard_block, runs, sample_frac, lambda1_a, lambda1_b, it_a, it_b, edge_eps, seed=42):
    rng = np.random.default_rng(seed)
    n, d = X.shape
    S = np.zeros((d, d), dtype=int)
    for r in range(runs):
        idx = rng.choice(n, max(20, int(n * sample_frac)), replace=True)
        Xa = X[idx]
        Ma = obs_mask[idx]
        Wa = solve_coke_forward(Xa, Ma, hard_block, lambda1=lambda1_a, max_iter=it_a)
        Wb = solve_coke_forward(Xa, Ma, hard_block, lambda1=lambda1_b, max_iter=it_b, W_init=Wa)
        S += (np.abs(Wb) > edge_eps).astype(int)
        print(f"[SS] run {r + 1}/{runs} done")
    return S / runs


def build_hard_block(names: List[str]) -> np.ndarray:
    orders = []
    for c in names:
        if c == "Response":
            orders.append((99, 999))
        else:
            line, station, _ = parse_feature(c)
            orders.append((line, station))
    d = len(names)
    hard = np.zeros((d, d), dtype=float)
    for i in range(d):
        for j in range(d):
            # Allow edges forward in line/station order and always into Response.
            if i == j:
                hard[i, j] = 1.0
            elif names[j] == "Response":
                hard[i, j] = 0.0
            elif names[i] == "Response" or orders[i] >= orders[j]:
                hard[i, j] = 1.0
    return hard


def select_edges(W, freq, hard_block, keep_freq, weight_thresh, top_k):
    W_sel = W.copy()
    W_sel[freq < keep_freq] = 0.0
    W_sel[np.abs(W_sel) < weight_thresh] = 0.0
    for j in range(W_sel.shape[1]):
        col = np.abs(W_sel[:, j])
        nz = np.where(col > 0)[0]
        if nz.size > top_k:
            kth = np.partition(col[nz], -top_k)[-top_k]
            W_sel[col < kth, j] = 0.0
    W_sel[hard_block > 0] = 0.0
    np.fill_diagonal(W_sel, 0.0)
    return W_sel


def main():
    parser = argparse.ArgumentParser(description="Bosch global KQC mining with COKE/NOTEARS-style DAG learning")
    parser.add_argument("--train_numeric_csv", required=True)
    parser.add_argument("--out_dir", default="bosch_kqc_output")
    parser.add_argument("--max_rows", type=int, default=200000)
    parser.add_argument("--max_features", type=int, default=80)
    parser.add_argument("--per_station", type=int, default=2)
    parser.add_argument("--selection_mode", choices=["response_assoc", "coverage"], default="response_assoc")
    parser.add_argument("--keep_freq", type=float, default=0.6)
    parser.add_argument("--weight_thresh", type=float, default=1e-4)
    parser.add_argument("--edge_indicator_eps", type=float, default=1e-6)
    parser.add_argument("--top_k", type=int, default=3)
    parser.add_argument("--lambda1_a", type=float, default=1e-3)
    parser.add_argument("--lambda1_b", type=float, default=2e-3)
    parser.add_argument("--it_a", type=int, default=60)
    parser.add_argument("--it_b", type=int, default=80)
    parser.add_argument("--ss_runs", type=int, default=20)
    parser.add_argument("--ss_frac", type=float, default=0.8)
    args = parser.parse_args()

    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    selected = select_bosch_features(args.train_numeric_csv, args.max_rows, args.max_features, args.per_station, args.selection_mode)
    df = load_bosch_matrix(args.train_numeric_csv, selected, args.max_rows)
    names = selected + ["Response"]
    df_use = df[names].copy()
    obs_mask = ~df_use.isna().to_numpy(dtype=bool)
    X_filled = SimpleImputer(strategy="mean").fit_transform(df_use)
    X = StandardScaler().fit_transform(X_filled)
    hard_block = build_hard_block(names)

    with open(out_dir / "bosch_debug_stats.txt", "w", encoding="utf-8") as f:
        f.write(f"n_samples={len(df_use)}, n_variables={len(names)}\n")
        f.write(f"n_failed_response={int(df_use['Response'].sum())}\n")
        f.write("selected_features:\n")
        for c in selected:
            f.write(f"  {c}, station={station_name(c)}, observed={int(df_use[c].notna().sum())}\n")

    W_A = solve_coke_forward(X, obs_mask.astype(float), hard_block, lambda1=args.lambda1_a, max_iter=args.it_a)
    W_B = solve_coke_forward(X, obs_mask.astype(float), hard_block, lambda1=args.lambda1_b, max_iter=args.it_b, W_init=W_A)
    pd.DataFrame(W_B, index=names, columns=names).to_csv(out_dir / "bosch_adj_matrix_raw.csv", encoding="utf-8-sig")

    freq = stability_selection(
        X,
        obs_mask.astype(float),
        hard_block,
        args.ss_runs,
        args.ss_frac,
        args.lambda1_a,
        args.lambda1_b,
        max(10, args.it_a // 2),
        max(10, args.it_b // 2),
        args.edge_indicator_eps,
    )
    pd.DataFrame(freq, index=names, columns=names).to_csv(out_dir / "bosch_edge_frequency.csv", encoding="utf-8-sig")

    W_sel = select_edges(W_B, freq, hard_block, args.keep_freq, args.weight_thresh, args.top_k)
    pd.DataFrame(W_sel, index=names, columns=names).to_csv(out_dir / "bosch_adj_matrix_selected.csv", encoding="utf-8-sig")

    target = "Response"
    tgt = names.index(target)
    edges = []
    for i, src in enumerate(names):
        if abs(W_sel[i, tgt]) > 0:
            edges.append((src, station_name(src), target, W_sel[i, tgt], float(freq[i, tgt])))
    edges = sorted(edges, key=lambda x: -abs(x[3]))
    pd.DataFrame(edges, columns=["source_variable", "station", "target_kqc", "weight", "frequency"]).to_csv(
        out_dir / "bosch_influences_on_response.csv", index=False, encoding="utf-8-sig"
    )

    if edges and df_use["Response"].nunique() == 2 and df_use["Response"].sum() >= 2:
        kqc_cols = [e[0] for e in edges]
        X_kqc = SimpleImputer(strategy="mean").fit_transform(df_use[kqc_cols])
        y = df_use["Response"].astype(int).to_numpy()
        splits = min(5, int(y.sum()), int((y == 0).sum()))
        if splits >= 2:
            scores = []
            for tr, te in StratifiedKFold(n_splits=splits, shuffle=True, random_state=42).split(X_kqc, y):
                clf = LogisticRegression(max_iter=1000, class_weight="balanced")
                clf.fit(X_kqc[tr], y[tr])
                prob = clf.predict_proba(X_kqc[te])[:, 1]
                scores.append(roc_auc_score(y[te], prob))
            with open(out_dir / "bosch_kqc_cv_score.txt", "w", encoding="utf-8") as f:
                f.write(f"Target: Response\n")
                f.write(f"Features from selected incoming edges: {kqc_cols}\n")
                f.write(f"{splits}-fold CV ROC-AUC: {np.mean(scores):.4f} +/- {np.std(scores):.4f}\n")

    print(f"[OK] output_dir={out_dir.resolve()}")
    print(pd.DataFrame(edges, columns=["source_variable", "station", "target_kqc", "weight", "frequency"]).head(20).to_string(index=False))


if __name__ == "__main__":
    main()
