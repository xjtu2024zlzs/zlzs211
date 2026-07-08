import json
import math
import sys
from functools import lru_cache
from pathlib import Path
from typing import Any, Dict, List

import joblib
import numpy as np

PROJECT_ROOT = Path(__file__).resolve().parents[2]
MODELS_DIR = PROJECT_ROOT / "models"
REGISTRY_PATH = MODELS_DIR / "model_registry.json"
EPS = 1e-9

PHYSICS_FEATURE_NAMES = [
    "总直线长度/端点弦长",
    "总转角/180°",
    "外径/R",
    "最大跨弦偏距/端点弦长",
    "L3/总直线长度",
]


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


class DictPipelineModel:
    """Adapter for pkl files saved as a dict of preprocessing objects and estimator."""

    def __init__(self, payload: Dict[str, Any]):
        self.payload = payload
        self.model = payload.get("model")
        self.polynomial = payload.get("polynomial")
        self.basis_scaler = payload.get("basis_scaler")
        self.physics_names = payload.get("physics_names") or []

    @property
    def uses_physics_features(self) -> bool:
        return list(self.physics_names) == PHYSICS_FEATURE_NAMES

    def predict(self, x):
        if self.model is None or not hasattr(self.model, "predict"):
            raise ValueError("Dict model payload does not contain a predict-capable 'model'")
        x_array = np.asarray(x, dtype=float)
        if x_array.ndim == 1:
            x_array = x_array.reshape(1, -1)
        features = x_array
        if self.polynomial is not None:
            features = self.polynomial.transform(features)
        if self.basis_scaler is not None:
            features = self.basis_scaler.transform(features)
        return self.model.predict(features)


def _physics_features_from_design(x_array, fixed_params: Dict[str, Any] | None = None):
    """Compute the exact physics features used by stage03_physics_feature_kriging0701.py."""
    fixed_params = fixed_params or {}
    total_x = float(fixed_params.get("totalHorizontal", fixed_params.get("total_horizontal_mm", 600.0)))
    total_y = -float(fixed_params.get("totalVertical", fixed_params.get("total_vertical_mm", 300.0)))
    outer_diameter = float(fixed_params.get("pipeDiameter", fixed_params.get("pipe_outer_diameter_mm", 9.53)))
    chord = complex(total_x, total_y)
    endpoint_chord = abs(chord)
    if endpoint_chord < EPS:
        raise ValueError("Endpoint chord length must be greater than zero.")

    rows = []
    for row in np.asarray(x_array, dtype=float):
        l1, l2, angle1, angle2, radius = [float(value) for value in row[:5]]
        if l1 <= 0 or l2 <= 0 or radius <= 0:
            raise ValueError("L1, L2 and R must be greater than zero.")

        turn1 = math.radians(180.0 - angle1)
        turn2 = math.radians(180.0 - angle2)
        direction2 = complex(math.cos(-turn1), math.sin(-turn1))
        direction3_angle = -turn1 + turn2
        direction3 = complex(math.cos(direction3_angle), math.sin(direction3_angle))
        first_two = l1 + l2 * direction2

        b = 2.0 * (first_two * direction3.conjugate()).real
        c = abs(first_two) ** 2 - endpoint_chord ** 2
        discriminant = b ** 2 - 4.0 * c
        if discriminant < -1e-8:
            raise ValueError("The selected design cannot satisfy the fixed endpoint closure constraint.")

        l3 = (-b + math.sqrt(max(discriminant, 0.0))) / 2.0
        if l3 <= 0:
            raise ValueError("The endpoint closure calculation produced a non-positive L3.")

        local_end = first_two + l3 * direction3
        rotation = math.atan2(chord.imag, chord.real) - math.atan2(local_end.imag, local_end.real)
        rotate = complex(math.cos(rotation), math.sin(rotation))
        p1 = l1 * rotate
        p2 = p1 + l2 * rotate * direction2
        p3 = p2 + l3 * rotate * direction3
        if abs(p3 - chord) > 1e-6:
            raise RuntimeError("Geometry closure error is too large.")

        h1 = (total_x * p1.imag - total_y * p1.real) / endpoint_chord
        h2 = (total_x * p2.imag - total_y * p2.real) / endpoint_chord
        maximum_offset_ratio = max(abs(h1), abs(h2)) / endpoint_chord
        total_straight_length = l1 + l2 + l3
        if total_straight_length < EPS:
            raise ValueError("Total straight length must be greater than zero.")

        rows.append([
            total_straight_length / endpoint_chord,
            (math.degrees(turn1) + math.degrees(turn2)) / 180.0,
            outer_diameter / radius,
            maximum_offset_ratio,
            l3 / total_straight_length,
        ])

    return np.asarray(rows, dtype=float)


def model_input_features(model, x_array, fixed_params: Dict[str, Any] | None = None):
    if isinstance(model, DictPipelineModel) and model.uses_physics_features:
        return _physics_features_from_design(x_array, fixed_params)
    return x_array


def _registry_data() -> Dict[str, Any]:
    with REGISTRY_PATH.open("r", encoding="utf-8") as file:
        return json.load(file)


def _safe_model_name(model_name: str) -> str:
    name = Path(model_name or "").name
    if not name.endswith(".pkl"):
        raise ValueError("Model file must be a .pkl file")
    return name


def get_model_catalog() -> List[Dict[str, Any]]:
    data = _registry_data()
    active_model = _safe_model_name(data.get("activeModel", ""))
    configured = data.get("models") or []
    by_name: Dict[str, Dict[str, Any]] = {}

    if not configured:
        configured = [{
            "modelName": active_model,
            "displayName": active_model,
            "modelType": data.get("modelType", ""),
            "inputOrder": data.get("inputOrder", []),
            "outputName": data.get("outputName", "predictedStress"),
            "outputUnit": data.get("outputUnit", "MPa"),
        }]

    for item in configured:
        model_name = _safe_model_name(str(item.get("modelName") or item.get("name") or ""))
        model_path = MODELS_DIR / model_name
        by_name[model_name] = {
            **data,
            **item,
            "modelName": model_name,
            "displayName": item.get("displayName") or item.get("modelLabel") or model_name,
            "active": model_name == active_model,
            "modelPath": str(model_path),
            "exists": model_path.exists(),
        }

    for model_path in sorted(MODELS_DIR.glob("*.pkl")):
        if model_path.name not in by_name:
            by_name[model_path.name] = {
                **data,
                "modelName": model_path.name,
                "displayName": model_path.name,
                "active": model_path.name == active_model,
                "modelPath": str(model_path),
                "exists": True,
            }

    return list(by_name.values())


def get_model_info(model_name: str | None = None) -> Dict[str, Any]:
    data = _registry_data()
    requested = _safe_model_name(model_name or data["activeModel"])
    for item in get_model_catalog():
        if item["modelName"] == requested:
            return item
    model_path = MODELS_DIR / requested
    return {
        **data,
        "modelName": requested,
        "displayName": requested,
        "active": requested == data.get("activeModel"),
        "modelPath": str(model_path),
        "exists": model_path.exists(),
    }


def get_active_model_info() -> Dict[str, Any]:
    return get_model_info()


@lru_cache(maxsize=4)
def load_model(model_name: str):
    model_path = MODELS_DIR / _safe_model_name(model_name)
    if not model_path.exists():
        raise FileNotFoundError(f"Model file not found: {model_path.name}")
    model = joblib.load(model_path)
    if isinstance(model, dict):
        return DictPipelineModel(model)
    return model


def active_model(model_name: str | None = None):
    info = get_model_info(model_name)
    return info, load_model(info["modelName"])
