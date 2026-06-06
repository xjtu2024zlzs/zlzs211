"""TEP process anomaly detection using PCA / DPCA statistical monitoring.

Algorithm:
- Use standard/normal process data as reference.
- Optionally build DPCA lag features within each simulationRun.
- Fit PCA on the reference data.
- Score real process data with Hotelling T2, SPE/Q, and fused anomaly score.

No print statements. Designed for FastAPI service calls.
"""

from __future__ import annotations

import os
import uuid
from pathlib import Path
from typing import Any, Dict, List, Optional, Sequence, Union

import numpy as np
import pandas as pd
from sklearn.decomposition import IncrementalPCA
from sklearn.preprocessing import StandardScaler
from sklearn.utils.validation import check_is_fitted


NumberLike = Union[int, float]


class TEPStatisticalAnomalyDetector:
    """PCA / DPCA statistical process monitor for TEP-like data."""

    def __init__(
        self,
        n_components: NumberLike = 30,
        threshold_quantile: float = 0.995,
        batch_size: int = 50000,
        eps: float = 1e-12,
    ) -> None:
        self.n_components = n_components
        self.threshold_quantile = threshold_quantile
        self.batch_size = batch_size
        self.eps = eps

        self.scaler = StandardScaler()
        self.pca: Optional[IncrementalPCA] = None

        self.feature_cols_: Optional[List[str]] = None
        self.eigenvalues_: Optional[np.ndarray] = None
        self.t2_threshold_: Optional[float] = None
        self.q_threshold_: Optional[float] = None

    def fit(self, df_train: pd.DataFrame, feature_cols: Sequence[str]) -> "TEPStatisticalAnomalyDetector":
        self.feature_cols_ = list(feature_cols)
        if not self.feature_cols_:
            raise ValueError("feature_cols cannot be empty.")
        if len(df_train) == 0:
            raise ValueError("Training data is empty. Cannot fit anomaly detector.")

        X = df_train[self.feature_cols_].to_numpy(dtype=np.float64)
        X_scaled = self.scaler.fit_transform(X)

        n_features = X_scaled.shape[1]
        n_samples = X_scaled.shape[0]
        n_components = self._resolve_n_components(X_scaled, n_samples, n_features)

        if n_components < 1:
            raise ValueError("n_components must be at least 1.")
        if n_components > min(n_samples, n_features):
            n_components = min(n_samples, n_features)

        pca = IncrementalPCA(n_components=n_components, batch_size=self.batch_size)
        for start in range(0, n_samples, self.batch_size):
            end = min(start + self.batch_size, n_samples)
            pca.partial_fit(X_scaled[start:end])

        self.pca = pca
        self.eigenvalues_ = pca.explained_variance_

        t2_all: List[np.ndarray] = []
        q_all: List[np.ndarray] = []
        for start in range(0, n_samples, self.batch_size):
            end = min(start + self.batch_size, n_samples)
            X_batch = X_scaled[start:end]
            scores = self.pca.transform(X_batch)
            X_recon = self.pca.inverse_transform(scores)
            t2_all.append(self._compute_t2(scores))
            q_all.append(self._compute_q(X_batch, X_recon))

        t2_values = np.concatenate(t2_all)
        q_values = np.concatenate(q_all)
        self.t2_threshold_ = float(np.quantile(t2_values, self.threshold_quantile))
        self.q_threshold_ = float(np.quantile(q_values, self.threshold_quantile))
        return self

    def decision_function(self, df: pd.DataFrame, meta_cols: Optional[Sequence[str]] = None) -> pd.DataFrame:
        self._check_fitted()
        assert self.feature_cols_ is not None
        assert self.pca is not None
        assert self.t2_threshold_ is not None
        assert self.q_threshold_ is not None

        if meta_cols is None:
            meta_cols = [col for col in ["faultNumber", "simulationRun", "sample"] if col in df.columns]

        outputs: List[pd.DataFrame] = []
        n_rows = len(df)
        for start in range(0, n_rows, self.batch_size):
            end = min(start + self.batch_size, n_rows)
            df_batch = df.iloc[start:end]
            X_batch = df_batch[self.feature_cols_].to_numpy(dtype=np.float64)
            X_scaled = self.scaler.transform(X_batch)
            scores = self.pca.transform(X_scaled)
            X_recon = self.pca.inverse_transform(scores)

            t2 = self._compute_t2(scores)
            q = self._compute_q(X_scaled, X_recon)
            t2_norm = t2 / (self.t2_threshold_ + self.eps)
            q_norm = q / (self.q_threshold_ + self.eps)
            anomaly_score = t2_norm + q_norm

            score_df = pd.DataFrame(
                {
                    "T2": t2,
                    "Q": q,
                    "T2_norm": t2_norm,
                    "Q_norm": q_norm,
                    "anomaly_score": anomaly_score,
                }
            )
            selected_meta_cols = [col for col in meta_cols if col in df_batch.columns]
            if selected_meta_cols:
                score_df = pd.concat(
                    [df_batch[selected_meta_cols].reset_index(drop=True), score_df.reset_index(drop=True)],
                    axis=1,
                )
            outputs.append(score_df)

        if not outputs:
            return pd.DataFrame()
        return pd.concat(outputs, axis=0).reset_index(drop=True)

    def _resolve_n_components(self, X_scaled: np.ndarray, n_samples: int, n_features: int) -> int:
        if isinstance(self.n_components, float) and 0 < self.n_components < 1:
            cov = np.cov(X_scaled, rowvar=False)
            eigvals = np.linalg.eigvalsh(cov)[::-1]
            eigvals = np.maximum(eigvals, 0)
            total = float(np.sum(eigvals))
            if total <= self.eps:
                return min(1, n_features, n_samples)
            explained_ratio = eigvals / total
            return int(np.searchsorted(np.cumsum(explained_ratio), self.n_components) + 1)
        return int(self.n_components)

    def _compute_t2(self, scores: np.ndarray) -> np.ndarray:
        if self.eigenvalues_ is None:
            raise RuntimeError("PCA eigenvalues are not available.")
        return np.sum((scores ** 2) / (self.eigenvalues_ + self.eps), axis=1)

    @staticmethod
    def _compute_q(X_scaled: np.ndarray, X_recon: np.ndarray) -> np.ndarray:
        residual = X_scaled - X_recon
        return np.sum(residual ** 2, axis=1)

    def _check_fitted(self) -> None:
        if self.feature_cols_ is None:
            raise RuntimeError("Model is not fitted yet.")
        check_is_fitted(self.scaler, attributes=["mean_", "scale_"])
        if self.pca is None:
            raise RuntimeError("PCA model is not fitted yet.")


def read_table(path: Union[str, Path]) -> pd.DataFrame:
    path = Path(path)
    if not path.exists():
        raise FileNotFoundError(f"Input file not found: {path}")
    ext = path.suffix.lower()
    if ext == ".csv":
        df = pd.read_csv(path)
        if len(df.columns) == 1:
            df = pd.read_csv(path, sep=None, engine="python")
        return df
    if ext in {".parquet", ".pq"}:
        return pd.read_parquet(path)
    raise ValueError("Input file must be .csv, .parquet, or .pq")


def write_table(df: pd.DataFrame, path: Union[str, Path]) -> str:
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    ext = path.suffix.lower()
    if ext == ".csv":
        df.to_csv(path, index=False)
    elif ext in {".parquet", ".pq"}:
        df.to_parquet(path, index=False)
    else:
        raise ValueError("Output file must be .csv, .parquet, or .pq")
    return str(path).replace("\\", "/")


def infer_tep_feature_columns(df: pd.DataFrame) -> List[str]:
    feature_cols: List[str] = []
    for col in df.columns:
        name = str(col).lower().strip()
        if name.startswith("xmeas_") or name.startswith("xmv_"):
            feature_cols.append(col)
        elif name.startswith("xmeas(") or name.startswith("xmv("):
            feature_cols.append(col)
        elif name.startswith("xmeas ") or name.startswith("xmv "):
            feature_cols.append(col)

    if not feature_cols:
        raise ValueError(
            "Could not infer TEP feature columns. Expected columns like "
            "xmeas_1...xmeas_41 and xmv_1...xmv_11, or XMEAS(1)...XMV(11)."
        )
    return feature_cols


def make_lagged_features_by_run(
    df: pd.DataFrame,
    feature_cols: Sequence[str],
    group_col: str = "simulationRun",
    time_col: str = "sample",
    n_lags: int = 2,
    meta_cols: Optional[Sequence[str]] = None,
) -> pd.DataFrame:
    if n_lags <= 0:
        return df.copy()
    if len(df) == 0:
        raise ValueError("No rows available for lagged feature generation.")

    work_df = df.copy()
    if group_col not in work_df.columns:
        work_df[group_col] = 1
    if time_col not in work_df.columns:
        work_df[time_col] = np.arange(len(work_df))
    if meta_cols is None:
        meta_cols = [col for col in ["faultNumber", group_col, time_col] if col in work_df.columns]

    result_list: List[pd.DataFrame] = []
    for _, group in work_df.groupby(group_col, sort=False):
        if len(group) <= n_lags:
            continue
        group = group.sort_values(time_col).copy()
        lagged_parts: List[pd.DataFrame] = []
        for lag in range(0, n_lags + 1):
            lagged = group[list(feature_cols)].shift(lag)
            lagged.columns = [f"{col}_lag{lag}" for col in feature_cols]
            lagged_parts.append(lagged)
        lagged_df = pd.concat(lagged_parts, axis=1)
        out = pd.concat([group[list(meta_cols)].reset_index(drop=True), lagged_df.reset_index(drop=True)], axis=1)
        out = out.dropna().reset_index(drop=True)
        if len(out) > 0:
            result_list.append(out)

    if not result_list:
        raise ValueError(
            "No lagged samples were generated. Check whether the data is empty, "
            "whether each simulationRun has more rows than n_lags, and whether n_lags is too large."
        )
    return pd.concat(result_list, axis=0).reset_index(drop=True)


def add_ewma_score(
    df: pd.DataFrame,
    score_col: str = "anomaly_score",
    output_col: str = "anomaly_score_ewma",
    alpha: Optional[float] = 0.2,
    group_cols: Optional[Sequence[str]] = None,
) -> pd.DataFrame:
    if alpha is None or alpha <= 0:
        return df
    out = df.copy()
    if group_cols is None:
        group_cols = [col for col in ["faultNumber", "simulationRun"] if col in out.columns]
    group_cols = [col for col in group_cols if col in out.columns]
    if group_cols:
        out[output_col] = out.groupby(list(group_cols))[score_col].transform(
            lambda s: s.ewm(alpha=alpha, adjust=False).mean()
        )
    else:
        out[output_col] = out[score_col].ewm(alpha=alpha, adjust=False).mean()
    return out


def _normalize_n_components(value: NumberLike) -> NumberLike:
    value = float(value)
    if 0 < value < 1:
        return value
    return int(value)


def run_process_anomaly(
    *,
    train_path: str,
    test_path: str,
    output_dir: Union[str, Path],
    output_path: Optional[str] = None,
    task_id: Optional[str] = None,
    dataset_id: Optional[str] = None,
    task_type: str = "PROCESS_ANOMALY_DETECT",
    n_lags: int = 2,
    n_components: NumberLike = 30,
    threshold_quantile: float = 0.995,
    batch_size: int = 50000,
    group_col: str = "simulationRun",
    time_col: str = "sample",
    normal_only: bool = False,
    pre_fault_sample: Optional[int] = None,
    ewma_alpha: Optional[float] = 0.2,
) -> Dict[str, Any]:
    """Run TEP statistical process anomaly detection and return service response."""

    train_df = read_table(train_path)
    test_df = read_table(test_path)

    feature_cols = infer_tep_feature_columns(train_df)
    missing_in_test = [col for col in feature_cols if col not in test_df.columns]
    if missing_in_test:
        raise ValueError(f"Test data is missing feature columns: {missing_in_test[:10]}")

    original_train_rows = int(len(train_df))
    if normal_only and "faultNumber" in train_df.columns:
        train_df = train_df[train_df["faultNumber"] == 0].copy()
    if pre_fault_sample is not None:
        if time_col not in train_df.columns:
            raise ValueError(f"pre_fault_sample was set, but time_col '{time_col}' is not in training data.")
        train_df = train_df[train_df[time_col] < pre_fault_sample].copy()
    if len(train_df) == 0:
        raise ValueError(
            "Training data is empty after filtering. Remove normalOnly, provide faultNumber == 0 data, "
            "or set/use a valid preFaultSample based on the sample column."
        )

    if n_lags > 0:
        train_model_df = make_lagged_features_by_run(
            train_df,
            feature_cols=feature_cols,
            group_col=group_col,
            time_col=time_col,
            n_lags=n_lags,
        )
        test_model_df = make_lagged_features_by_run(
            test_df,
            feature_cols=feature_cols,
            group_col=group_col,
            time_col=time_col,
            n_lags=n_lags,
        )
        model_feature_cols = [col for col in train_model_df.columns if "_lag" in str(col)]
        meta_cols = [col for col in ["faultNumber", group_col, time_col] if col in test_model_df.columns]
    else:
        train_model_df = train_df
        test_model_df = test_df
        model_feature_cols = feature_cols
        meta_cols = [col for col in ["faultNumber", group_col, time_col] if col in test_model_df.columns]

    detector = TEPStatisticalAnomalyDetector(
        n_components=_normalize_n_components(n_components),
        threshold_quantile=threshold_quantile,
        batch_size=batch_size,
    )
    detector.fit(train_model_df, model_feature_cols)
    score_df = detector.decision_function(test_model_df, meta_cols=meta_cols)
    score_df = add_ewma_score(score_df, alpha=ewma_alpha)

    if output_path:
        final_output_path = Path(output_path)
    else:
        base_dir = Path(output_dir)
        name = f"{dataset_id or task_id or 'tep'}_process_anomaly_{uuid.uuid4().hex[:8]}.parquet"
        final_output_path = base_dir / name

    saved_path = write_table(score_df, final_output_path)

    return {
        "taskId": task_id,
        "taskType": task_type,
        "datasetId": dataset_id,
        "status": "SUCCESS",
        "message": "process anomaly detection completed",
        "method": "DPCA_T2_Q" if n_lags > 0 else "PCA_T2_Q",
        "trainPath": str(Path(train_path)).replace("\\", "/"),
        "testPath": str(Path(test_path)).replace("\\", "/"),
        "outputPath": saved_path,
        "featureCount": len(feature_cols),
        "modelFeatureCount": len(model_feature_cols),
        "trainRowsOriginal": original_train_rows,
        "trainRowsUsed": int(len(train_df)),
        "scoreRows": int(len(score_df)),
        "nLags": n_lags,
        "nComponents": int(detector.pca.n_components_) if detector.pca is not None else None,
        "thresholdQuantile": threshold_quantile,
        "t2Threshold": detector.t2_threshold_,
        "qThreshold": detector.q_threshold_,
        "normalOnly": normal_only,
        "preFaultSample": pre_fault_sample,
    }
