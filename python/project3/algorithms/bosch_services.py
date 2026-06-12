"""Service adapters for the Bosch production-line algorithms."""

from __future__ import annotations

import pickle
import uuid
from pathlib import Path
from types import SimpleNamespace
from typing import Any, Dict, List, Optional, Union

import numpy as np
import pandas as pd
import torch
from sklearn.impute import SimpleImputer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import roc_auc_score
from sklearn.model_selection import StratifiedKFold
from sklearn.preprocessing import StandardScaler

from algorithms.bosch_global_kqc_coke import (
    FEATURE_RE,
    KQC_MISSING_FEATURE_MESSAGE,
    KQC_MISSING_RESPONSE_MESSAGE,
    build_hard_block,
    load_bosch_matrix,
    select_bosch_features,
    select_edges,
    solve_coke_forward,
    stability_selection,
    station_name,
)
from algorithms.bosch_key_station_identification import (
    aggregate_station_network,
    direct_response_score,
    edge_strength,
    evidence_edges,
    minmax,
    pagerank,
    path_response_score,
)
from algorithms.bosch_process_anomaly_memae import (
    ECDFTransform,
    PreprocessState,
    apply_scaler,
    auto_select_feature,
    build_scores,
    confusion,
    fit_scaler,
    load_bosch_feature_table,
    parse_hidden_dims,
    parse_station,
    score_memae,
    set_seed,
    train_memae,
    ucl_quantile,
    ewma_stat,
)


def _clean_path_text(value: Any) -> str:
    text = "" if value is None else str(value)
    for ch in ("\ufeff", "\u200e", "\u200f", "\u202a", "\u202b", "\u202c", "\u202d", "\u202e", "\u2066", "\u2067", "\u2068", "\u2069"):
        text = text.replace(ch, "")
    return text.strip().strip("\"'")


def run_bosch_process_anomaly(
    *,
    train_numeric_csv: str,
    output_dir: Union[str, Path],
    task_id: Optional[str] = None,
    station: str = "",
    feature_col: str = "",
    selection_mode: str = "response_assoc",
    max_rows: int = 200000,
    min_failed_observed: int = 10,
    min_observed: int = 1000,
    epochs: int = 60,
    batch_size: int = 256,
    alpha_sample: float = 0.01,
    ewma_lambda: float = 0.30,
    seed: int = 0,
    device: str = "auto",
) -> Dict[str, Any]:
    train_numeric_csv = _clean_path_text(train_numeric_csv)
    source = Path(train_numeric_csv)
    if not source.exists():
        raise FileNotFoundError(f"Bosch train_numeric.csv not found: {train_numeric_csv}")

    out_dir = Path(output_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    set_seed(seed)

    args = SimpleNamespace(
        hidden_dims="64,32,16",
        latent_dim=8,
        mem_dim=32,
        dropout_p=0.1,
        shrink_thres=0.005,
        epochs=int(epochs),
        batch_size=int(batch_size),
        lr=1e-3,
        weight_decay=1e-5,
        w_recon=1.0,
        w_entropy=0.5,
        w_dmin=1.0,
        mix_w=0.75,
        z_clip=8.0,
        ewma_lambda=float(ewma_lambda),
        alpha_sample=float(alpha_sample),
        hidden_dims_tuple=parse_hidden_dims("64,32,16"),
    )

    if not feature_col:
        feature_col = auto_select_feature(
            str(source),
            station,
            int(max_rows),
            selection_mode,
            int(min_failed_observed),
            int(min_observed),
        )
    station_value = parse_station(feature_col)

    df = load_bosch_feature_table(str(source), feature_col, int(max_rows))
    x_all = df[feature_col].to_numpy(np.float32).reshape(-1, 1)
    y = df["Response"].to_numpy(np.int32)
    x_train = x_all[y == 0]
    if len(x_train) < 100:
        raise ValueError("Too few normal samples for calibration.")

    resolved_device = "cuda" if device == "auto" and torch.cuda.is_available() else ("cpu" if device == "auto" else device)
    mean, std = fit_scaler(x_train)
    x_train_scaled = apply_scaler(x_train, mean, std)
    model = train_memae(x_train_scaled, args, resolved_device)
    comp_train = score_memae(model, x_train_scaled, args.batch_size, resolved_device)
    raw_train, _, _, params, t2_param = build_scores(comp_train, x_train_scaled, args)
    ecdf = ECDFTransform(raw_train)
    ucl = ucl_quantile(ewma_stat(ecdf.transform(raw_train), args.ewma_lambda), args.alpha_sample)

    x_all_scaled = apply_scaler(x_all, mean, std)
    comp = score_memae(model, x_all_scaled, args.batch_size, resolved_device)
    raw, memae, t2, _, _ = build_scores(comp, x_all_scaled, args, params, t2_param)
    ewma = ewma_stat(ecdf.transform(raw), args.ewma_lambda)
    alarm = (ewma >= ucl).astype(np.int32)

    safe_feature = feature_col.replace("/", "_").replace("\\", "_")
    score_path = out_dir / f"{safe_feature}_scores.csv"
    alarm_path = out_dir / f"{safe_feature}_alarm_points.csv"
    summary_path = out_dir / f"{safe_feature}_summary.csv"
    preprocess_path = out_dir / f"{safe_feature}_preprocess.pkl"
    model_path = out_dir / f"{safe_feature}_memae.pt"

    scores = pd.DataFrame({
        "Id": df["Id"].to_numpy(),
        "station": station_value,
        "feature_col": feature_col,
        feature_col: x_all.reshape(-1),
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
    scores.to_csv(score_path, index=False, encoding="utf-8-sig")
    alarms = scores[scores["alarm"] == 1].sort_values("excess", ascending=False)
    alarms.to_csv(alarm_path, index=False, encoding="utf-8-sig")

    summary = {
        "feature_col": feature_col,
        "station": station_value,
        "n_points": int(len(scores)),
        "n_failed_response": int(y.sum()),
        "n_alarm_points": int(alarm.sum()),
        "alarm_rate": float(alarm.mean()),
        "UCL_sample": float(ucl),
    }
    summary.update(confusion(y, alarm))
    pd.DataFrame([summary]).to_csv(summary_path, index=False, encoding="utf-8-sig")
    with open(preprocess_path, "wb") as fh:
        pickle.dump(PreprocessState(feature_col, mean, std, params, t2_param, raw_train, ucl, vars(args)), fh)
    torch.save(model.state_dict(), model_path)

    top_points = _head_records(alarms, 20)
    abnormal_score = float(summary["alarm_rate"])
    return {
        "taskId": task_id,
        "taskType": "PROCESS_ANOMALY_DETECT",
        "status": "SUCCESS",
        "message": "Bosch process anomaly detection completed",
        "method": "MemAE_T2_ECDF_EWMA_UCL",
        "isAbnormal": bool(summary["n_alarm_points"] > 0),
        "abnormalLevel": _level(abnormal_score),
        "abnormalScore": abnormal_score,
        "abnormalTime": str(top_points[0].get("Id")) if top_points else None,
        "suggestion": "建议复核EWMA超限最大的告警点，并追溯所选工序特征。",
        "curveData": [{"x": int(i), "value": float(v)} for i, v in enumerate(scores["ewma_stat"].head(200).tolist())],
        "summary": summary,
        "topAlarmPoints": top_points,
        "outputFiles": _paths(score_path, alarm_path, summary_path, preprocess_path, model_path),
    }


def run_bosch_kqc_mining(
    *,
    train_numeric_csv: str,
    output_dir: Union[str, Path],
    task_id: Optional[str] = None,
    max_rows: int = 200000,
    max_features: int = 80,
    per_station: int = 2,
    selection_mode: str = "response_assoc",
    keep_freq: float = 0.6,
    weight_thresh: float = 1e-4,
    edge_indicator_eps: float = 1e-6,
    top_k: int = 3,
    lambda1_a: float = 1e-3,
    lambda1_b: float = 2e-3,
    it_a: int = 60,
    it_b: int = 80,
    ss_runs: int = 20,
    ss_frac: float = 0.8,
) -> Dict[str, Any]:
    train_numeric_csv = _clean_path_text(train_numeric_csv)
    source = Path(train_numeric_csv)
    if not source.exists():
        raise FileNotFoundError(f"未找到 train_numeric.csv 文件：{train_numeric_csv}")
    columns = list(pd.read_csv(source, nrows=0).columns)
    if "Response" not in columns:
        raise ValueError(KQC_MISSING_RESPONSE_MESSAGE)
    if not any(FEATURE_RE.match(col) for col in columns):
        raise ValueError(KQC_MISSING_FEATURE_MESSAGE)
    if "Response" not in columns:
        raise ValueError(
            "当前 train_numeric.csv 缺少 Response 列，无法直接进行关键工序识别。"
            "请使用包含 Response 列的训练数据；如果没有 Response 列，请提供已完成的 KQC输出目录。"
            "该目录中需要包含 bosch_adj_matrix_selected.csv 和 bosch_edge_frequency.csv。"
        )
    if not any(FEATURE_RE.match(col) for col in columns):
        raise ValueError("当前 train_numeric.csv 缺少工序特征列，列名需要类似 L3_S36_F3939。")
    out_dir = Path(output_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    selected = select_bosch_features(str(source), int(max_rows), int(max_features), int(per_station), selection_mode)
    df = load_bosch_matrix(str(source), selected, int(max_rows))
    names = selected + ["Response"]
    df_use = df[names].copy()
    obs_mask = ~df_use.isna().to_numpy(dtype=bool)
    X_filled = SimpleImputer(strategy="mean").fit_transform(df_use)
    X = StandardScaler().fit_transform(X_filled)
    hard_block = build_hard_block(names)

    debug_path = out_dir / "bosch_debug_stats.txt"
    with open(debug_path, "w", encoding="utf-8") as fh:
        fh.write(f"n_samples={len(df_use)}, n_variables={len(names)}\n")
        fh.write(f"n_failed_response={int(df_use['Response'].sum())}\n")
        fh.write("selected_features:\n")
        for col in selected:
            fh.write(f"  {col}, station={station_name(col)}, observed={int(df_use[col].notna().sum())}\n")

    W_A = solve_coke_forward(X, obs_mask.astype(float), hard_block, lambda1=float(lambda1_a), max_iter=int(it_a))
    W_B = solve_coke_forward(X, obs_mask.astype(float), hard_block, lambda1=float(lambda1_b), max_iter=int(it_b), W_init=W_A)
    raw_path = out_dir / "bosch_adj_matrix_raw.csv"
    pd.DataFrame(W_B, index=names, columns=names).to_csv(raw_path, encoding="utf-8-sig")

    freq = stability_selection(
        X, obs_mask.astype(float), hard_block, int(ss_runs), float(ss_frac),
        float(lambda1_a), float(lambda1_b), max(10, int(it_a) // 2), max(10, int(it_b) // 2),
        float(edge_indicator_eps),
    )
    freq_path = out_dir / "bosch_edge_frequency.csv"
    pd.DataFrame(freq, index=names, columns=names).to_csv(freq_path, encoding="utf-8-sig")

    W_sel = select_edges(W_B, freq, hard_block, float(keep_freq), float(weight_thresh), int(top_k))
    selected_path = out_dir / "bosch_adj_matrix_selected.csv"
    pd.DataFrame(W_sel, index=names, columns=names).to_csv(selected_path, encoding="utf-8-sig")

    target = "Response"
    target_index = names.index(target)
    edges = []
    for i, src in enumerate(names):
        if abs(W_sel[i, target_index]) > 0:
            edges.append((src, station_name(src), target, W_sel[i, target_index], float(freq[i, target_index])))
    edges = sorted(edges, key=lambda item: -abs(item[3]))
    influence_path = out_dir / "bosch_influences_on_response.csv"
    influences = pd.DataFrame(edges, columns=["source_variable", "station", "target_kqc", "weight", "frequency"])
    influences.to_csv(influence_path, index=False, encoding="utf-8-sig")

    graph_data = _graph_data(W_sel, names, freq)
    response_graph_data = {"nodes": [], "links": [], "categories": graph_data["categories"]}
    if edges:
        keep = {target}
        keep.update([edge[0] for edge in edges])
        idx = [names.index(name) for name in keep]
        sub_names = [names[i] for i in idx]
        W_sub = np.zeros((len(idx), len(idx)))
        idx_map = {orig: key for key, orig in enumerate(idx)}
        for i in idx:
            for j in idx:
                W_sub[idx_map[i], idx_map[j]] = W_sel[i, j]
        freq_sub = np.zeros((len(idx), len(idx)))
        for i in idx:
            for j in idx:
                freq_sub[idx_map[i], idx_map[j]] = freq[i, j]
        response_graph_data = _graph_data(W_sub, sub_names, freq_sub)

    cv_path = out_dir / "bosch_kqc_cv_score.txt"
    cv_score = None
    if edges and df_use["Response"].nunique() == 2 and df_use["Response"].sum() >= 2:
        kqc_cols = [edge[0] for edge in edges]
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
            cv_score = {"mean": float(np.mean(scores)), "std": float(np.std(scores)), "splits": int(splits)}
            with open(cv_path, "w", encoding="utf-8") as fh:
                fh.write("Target: Response\n")
                fh.write(f"Features from selected incoming edges: {kqc_cols}\n")
                fh.write(f"{splits}-fold CV ROC-AUC: {cv_score['mean']:.4f} +/- {cv_score['std']:.4f}\n")

    top_influences = _head_records(influences, 20)
    return {
        "taskId": task_id,
        "taskType": "KQC_MINING",
        "status": "SUCCESS",
        "message": "Bosch KQC mining completed",
        "targetKqc": "Response",
        "selectedFeatureCount": len(selected),
        "influenceCount": len(influences),
        "topInfluences": top_influences,
        "graphData": graph_data,
        "responseGraphData": response_graph_data,
        "cvScore": cv_score,
        "adjPath": str(selected_path).replace("\\", "/"),
        "freqPath": str(freq_path).replace("\\", "/"),
        "outputDir": str(out_dir).replace("\\", "/"),
        "outputFiles": _paths(raw_path, freq_path, selected_path, influence_path, debug_path, cv_path),
    }


def run_bosch_key_station(
    *,
    adj_path: str,
    freq_path: str,
    output_dir: Union[str, Path],
    task_id: Optional[str] = None,
    keep_freq: float = 0.6,
    weight_thresh: float = 1e-4,
    top_n: int = 10,
) -> Dict[str, Any]:
    adj_file = Path(adj_path)
    freq_file = Path(freq_path)
    if not adj_file.exists():
        raise FileNotFoundError(f"adjacency matrix not found: {adj_path}")
    if not freq_file.exists():
        raise FileNotFoundError(f"edge frequency matrix not found: {freq_path}")
    out_dir = Path(output_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    adj = pd.read_csv(adj_file, index_col=0, encoding="utf-8-sig").astype(float)
    freq = pd.read_csv(freq_file, index_col=0, encoding="utf-8-sig").astype(float)
    E = edge_strength(adj, freq, float(keep_freq), float(weight_thresh))
    P = aggregate_station_network(E)
    pr = pagerank(P)
    direct = direct_response_score(E)
    path = path_response_score(E)
    nd, npth, npr = minmax(direct), minmax(path), minmax(pr)

    rows = []
    for station in sorted(direct):
        score = 0.45 * nd[station] + 0.35 * npth[station] + 0.20 * npr[station]
        rows.append({
            "station": station,
            "rank_score": float(score),
            "direct_response_score": float(direct[station]),
            "path_response_score": float(path[station]),
            "pagerank_score": float(pr.get(station, 0.0)),
        })
    ranking = pd.DataFrame(rows).sort_values("rank_score", ascending=False).reset_index(drop=True)
    ranking.insert(0, "rank", np.arange(1, len(ranking) + 1))
    ranking_path = out_dir / "bosch_key_station_ranking.csv"
    evidence_path = out_dir / "bosch_key_station_evidence.csv"
    network_path = out_dir / "bosch_station_network.csv"
    ranking.to_csv(ranking_path, index=False, encoding="utf-8-sig")
    evidence_edges(E, adj, freq).to_csv(evidence_path, index=False, encoding="utf-8-sig")
    P.to_csv(network_path, encoding="utf-8-sig")

    top_rows = _head_records(ranking, int(top_n))
    best = top_rows[0] if top_rows else {}
    return {
        "taskId": task_id,
        "taskType": "KEY_PROCESS_IDENTIFY",
        "status": "SUCCESS",
        "message": "Bosch key station identification completed",
        "keyProcessCode": best.get("station"),
        "keyProcessName": best.get("station"),
        "confidence": best.get("rank_score"),
        "score": best.get("rank_score"),
        "reason": "根据KQC加权的直接响应影响、路径响应影响和工序PageRank综合排序。",
        "suggestion": "建议优先对排名最高的工序进行工艺复核和质量控制检查。",
        "candidateProcesses": [
            {
                "rank": row.get("rank"),
                "processCode": row.get("station"),
                "processName": row.get("station"),
                "score": row.get("rank_score"),
                "directResponseScore": row.get("direct_response_score"),
                "pathResponseScore": row.get("path_response_score"),
                "pagerankScore": row.get("pagerank_score"),
            }
            for row in top_rows
        ],
        "outputDir": str(out_dir).replace("\\", "/"),
        "outputFiles": _paths(ranking_path, evidence_path, network_path),
    }


def _head_records(df: pd.DataFrame, limit: int) -> List[Dict[str, Any]]:
    if df is None or df.empty:
        return []
    clean = df.head(limit).replace({np.nan: None})
    return clean.to_dict(orient="records")


def _graph_data(W: np.ndarray, names: List[str], freq: Optional[np.ndarray] = None) -> Dict[str, Any]:
    nodes: List[Dict[str, Any]] = []
    links: List[Dict[str, Any]] = []
    for name in names:
        is_target = name == "Response"
        nodes.append({
            "id": name,
            "name": name,
            "station": None if is_target else station_name(name),
            "category": 1 if is_target else 0,
            "symbolSize": 34 if is_target else 22,
            "value": 1 if is_target else 0,
        })
    for i, source in enumerate(names):
        for j, target in enumerate(names):
            weight = float(W[i, j])
            if abs(weight) <= 0:
                continue
            links.append({
                "source": source,
                "target": target,
                "weight": weight,
                "frequency": None if freq is None else float(freq[i, j]),
                "lineStyle": {
                    "color": "#2f8f5b" if weight >= 0 else "#c94a4a",
                    "width": max(1.0, min(6.0, abs(weight) * 100.0)),
                },
            })
    return {
        "nodes": nodes,
        "links": links,
        "categories": [
            {"name": "质量特性"},
            {"name": "响应结果"},
        ],
    }


def _paths(*paths: Path) -> Dict[str, str]:
    return {path.stem: str(path).replace("\\", "/") for path in paths if path.exists()}


def _level(score: float) -> str:
    if score >= 0.2:
        return "HIGH"
    if score >= 0.05:
        return "MEDIUM"
    return "LOW"


def run_id(prefix: str) -> str:
    return f"{prefix}_{uuid.uuid4().hex[:8]}"
