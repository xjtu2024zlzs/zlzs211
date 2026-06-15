import json
import sys
from functools import lru_cache
from pathlib import Path
from typing import Any, Dict

import joblib
import numpy as np

PROJECT_ROOT = Path(__file__).resolve().parents[2]
MODELS_DIR = PROJECT_ROOT / "models"
REGISTRY_PATH = MODELS_DIR / "model_registry.json"


class AeroFSIKriging:
    """Compatibility class for pkl files saved with __main__.AeroFSIKriging."""

    def predict(self, x):
        x_array = np.asarray(x, dtype=float)
        if x_array.ndim == 1:
            x_array = x_array.reshape(1, -1)
        x_norm = (x_array - self.X_mean) / self.X_std
        pred = self._kriging_predict(x_norm)
        y_train = np.asarray(self.y_train, dtype=float).reshape(-1)
        y_min = float(np.nanmin(y_train))
        y_max = float(np.nanmax(y_train))
        y_span = max(1.0, y_max - y_min)
        valid = np.isfinite(pred) & (pred >= y_min - 2.0 * y_span) & (pred <= y_max + 2.0 * y_span)
        if np.all(valid):
            return np.asarray(pred).reshape(-1)
        fallback = self._rbf_weighted_predict(x_norm)
        pred = np.where(valid, pred, fallback)
        return np.asarray(pred).reshape(-1)

    def _kriging_predict(self, x_norm):
        diff = x_norm[:, None, :] - self.X_train[None, :, :]
        theta = np.power(10.0, np.asarray(self.theta, dtype=float))
        correlation = np.exp(-np.sum(theta * diff * diff, axis=2))
        residual = np.asarray(self.y_train).reshape(-1) - float(self.mu)
        pred = float(self.mu) + correlation @ self.K_inv @ residual
        return np.asarray(pred, dtype=float).reshape(-1)

    def _rbf_weighted_predict(self, x_norm):
        diff = x_norm[:, None, :] - self.X_train[None, :, :]
        distance2 = np.sum(diff * diff, axis=2)
        weights = np.exp(-distance2)
        weights_sum = np.sum(weights, axis=1, keepdims=True)
        weights = weights / np.maximum(weights_sum, 1e-12)
        y_train = np.asarray(self.y_train, dtype=float).reshape(-1)
        return weights @ y_train


setattr(sys.modules["__main__"], "AeroFSIKriging", AeroFSIKriging)


def get_active_model_info() -> Dict[str, Any]:
    with REGISTRY_PATH.open("r", encoding="utf-8") as file:
        data = json.load(file)
    model_path = MODELS_DIR / data["activeModel"]
    return {
        **data,
        "modelPath": str(model_path),
        "exists": model_path.exists(),
    }


@lru_cache(maxsize=4)
def load_model(model_name: str):
    model_path = MODELS_DIR / model_name
    return joblib.load(model_path)


def active_model():
    info = get_active_model_info()
    return info, load_model(info["activeModel"])
