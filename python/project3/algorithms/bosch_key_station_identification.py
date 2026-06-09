# -*- coding: utf-8 -*-
"""
Bosch key station identification.

Algorithm:
KQC-weighted causal PageRank, adapted to Bosch stations.

Input:
- selected variable-level adjacency from bosch_global_kqc_coke.py
- edge frequency matrix from bosch_global_kqc_coke.py

Output:
- station-level criticality ranking
- station evidence edges into Response
- station network matrix
"""

from __future__ import annotations

import argparse
import re
from pathlib import Path
from typing import Dict, List, Tuple

import numpy as np
import pandas as pd


FEATURE_RE = re.compile(r"^L(?P<line>\d+)_S(?P<station>\d+)_F(?P<feature>\d+)$")


def station_name(col: str) -> str:
    if col == "Response":
        return "KQC_Response"
    m = FEATURE_RE.match(col)
    if not m:
        return "UNKNOWN"
    return f"L{m.group('line')}_S{m.group('station')}"


def minmax(scores: Dict[str, float]) -> Dict[str, float]:
    vals = np.asarray(list(scores.values()), dtype=float)
    if vals.size == 0:
        return {}
    lo, hi = float(np.nanmin(vals)), float(np.nanmax(vals))
    if not np.isfinite(lo) or not np.isfinite(hi) or hi - lo < 1e-12:
        return {k: 0.0 for k in scores}
    return {k: float((v - lo) / (hi - lo)) for k, v in scores.items()}


def pagerank(P: pd.DataFrame, damping=0.85, max_iter=200, tol=1e-10) -> Dict[str, float]:
    names = list(P.index)
    A = P.to_numpy(dtype=float, copy=True)
    np.fill_diagonal(A, 0.0)
    n = A.shape[0]
    T = np.zeros_like(A)
    row_sum = A.sum(axis=1)
    for i in range(n):
        T[i] = A[i] / row_sum[i] if row_sum[i] > 0 else np.ones(n) / n
    pr = np.ones(n) / n
    teleport = np.ones(n) / n
    for _ in range(max_iter):
        new = (1.0 - damping) * teleport + damping * (T.T @ pr)
        if np.linalg.norm(new - pr, ord=1) < tol:
            pr = new
            break
        pr = new
    return {name: float(v) for name, v in zip(names, pr)}


def edge_strength(adj: pd.DataFrame, freq: pd.DataFrame, keep_freq: float, weight_thresh: float) -> pd.DataFrame:
    freq = freq.loc[adj.index, adj.columns]
    E = adj.abs() * freq
    E[(freq < keep_freq) | (adj.abs() < weight_thresh)] = 0.0
    arr = E.to_numpy(dtype=float, copy=True)
    np.fill_diagonal(arr, 0.0)
    return pd.DataFrame(arr, index=adj.index, columns=adj.columns)


def aggregate_station_network(E: pd.DataFrame) -> pd.DataFrame:
    stations = sorted({station_name(c) for c in E.index if station_name(c) != "KQC_Response"})
    P = pd.DataFrame(0.0, index=stations, columns=stations)
    for src in E.index:
        ps = station_name(src)
        if ps not in P.index:
            continue
        for tgt in E.columns:
            pt = station_name(tgt)
            if pt in P.columns:
                P.loc[ps, pt] += float(E.loc[src, tgt])
    return P


def direct_response_score(E: pd.DataFrame) -> Dict[str, float]:
    stations = sorted({station_name(c) for c in E.index if station_name(c) != "KQC_Response"})
    out = {s: 0.0 for s in stations}
    if "Response" not in E.columns:
        return out
    for src in E.index:
        st = station_name(src)
        if st in out:
            out[st] += float(E.loc[src, "Response"])
    return out


def path_response_score(E: pd.DataFrame, max_depth=3, decay=0.6) -> Dict[str, float]:
    stations = sorted({station_name(c) for c in E.index if station_name(c) != "KQC_Response"})
    out = {s: 0.0 for s in stations}
    if "Response" not in E.columns:
        return out
    names = list(E.index)
    idx = {n: i for i, n in enumerate(names)}
    A = E.to_numpy(dtype=float, copy=True)
    if A.max() > 0:
        A = A / A.max()
    target_idx = idx["Response"]
    power = A.copy()
    for depth in range(1, max_depth + 1):
        dw = decay ** max(depth - 1, 0)
        for var in names:
            st = station_name(var)
            if st in out:
                out[st] += dw * float(power[idx[var], target_idx])
        power = power @ A
    return out


def evidence_edges(E: pd.DataFrame, adj: pd.DataFrame, freq: pd.DataFrame) -> pd.DataFrame:
    rows = []
    if "Response" not in E.columns:
        return pd.DataFrame()
    for src in E.index:
        strength = float(E.loc[src, "Response"])
        if strength > 0:
            rows.append(
                {
                    "station": station_name(src),
                    "source_variable": src,
                    "target_kqc": "Response",
                    "weight": float(adj.loc[src, "Response"]),
                    "frequency": float(freq.loc[src, "Response"]),
                    "edge_strength": strength,
                }
            )
    return pd.DataFrame(rows).sort_values("edge_strength", ascending=False)


def main():
    parser = argparse.ArgumentParser(description="Bosch KQC-weighted key station identification")
    parser.add_argument("--adj", required=True, help="bosch_adj_matrix_selected.csv")
    parser.add_argument("--freq", required=True, help="bosch_edge_frequency.csv")
    parser.add_argument("--out_dir", default="bosch_key_station_output")
    parser.add_argument("--keep_freq", type=float, default=0.6)
    parser.add_argument("--weight_thresh", type=float, default=1e-4)
    args = parser.parse_args()

    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    adj = pd.read_csv(args.adj, index_col=0, encoding="utf-8-sig").astype(float)
    freq = pd.read_csv(args.freq, index_col=0, encoding="utf-8-sig").astype(float)
    E = edge_strength(adj, freq, args.keep_freq, args.weight_thresh)
    P = aggregate_station_network(E)
    pr = pagerank(P)
    direct = direct_response_score(E)
    path = path_response_score(E)
    nd, npth, npr = minmax(direct), minmax(path), minmax(pr)

    rows = []
    for station in sorted(direct):
        score = 0.45 * nd[station] + 0.35 * npth[station] + 0.20 * npr[station]
        rows.append(
            {
                "station": station,
                "rank_score": float(score),
                "direct_response_score": float(direct[station]),
                "path_response_score": float(path[station]),
                "pagerank_score": float(pr.get(station, 0.0)),
            }
        )
    ranking = pd.DataFrame(rows).sort_values("rank_score", ascending=False).reset_index(drop=True)
    ranking.insert(0, "rank", np.arange(1, len(ranking) + 1))
    ranking.to_csv(out_dir / "bosch_key_station_ranking.csv", index=False, encoding="utf-8-sig")
    evidence_edges(E, adj, freq).to_csv(out_dir / "bosch_key_station_evidence.csv", index=False, encoding="utf-8-sig")
    P.to_csv(out_dir / "bosch_station_network.csv", encoding="utf-8-sig")
    print(ranking.head(30).to_string(index=False))


if __name__ == "__main__":
    main()
