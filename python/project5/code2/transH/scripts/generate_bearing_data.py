from __future__ import annotations

from pathlib import Path

import numpy as np
import pandas as pd


SEED = 20260603
ROOT = Path(__file__).resolve().parents[1]
DATA_DIR = ROOT / "data" / "bearing"
REPORT_DIR = ROOT / "outputs" / "bearing" / "reports"

SCALE = {
    "component": 5000,
    "design": 800,
    "material": 500,
    "manufacturing": 1200,
    "assembly": 5000,
    "inspection": 7000,
    "feedback": 5000,
}

NORMAL_RATIO = 0.12
SEVERITY_DISTRIBUTION = {"slight": 0.30, "medium": 0.45, "severe": 0.25}

FAULT_LABELS = {
    "inner_race_fault": {"position": "inner_race", "prefix": "IR"},
    "rolling_element_fault": {"position": "rolling_element", "prefix": "B"},
    "outer_race_fault": {"position": "outer_race", "prefix": "OR"},
}

FAULT_TARGET_SHARE = {
    "inner_race_fault": 1 / 3,
    "outer_race_fault": 1 / 3,
    "rolling_element_fault": 1 / 3,
}

STAGE_REASON_TYPES = {
    "design": "BearingDesignSpec",
    "material": "MaterialBatch",
    "manufacturing": "ManufacturingBatch",
    "assembly": "AssemblyRecord",
    "inspection": "InspectionRecord",
    "operation": "MaintenanceRecord",
}

FAULT_STAGE_PRIOR = {
    "inner_race_fault": {
        "design": 0.12,
        "material": 0.22,
        "manufacturing": 0.30,
        "assembly": 0.24,
        "inspection": 0.07,
        "operation": 0.05,
    },
    "outer_race_fault": {
        "design": 0.14,
        "material": 0.18,
        "manufacturing": 0.28,
        "assembly": 0.25,
        "inspection": 0.08,
        "operation": 0.07,
    },
    "rolling_element_fault": {
        "design": 0.10,
        "material": 0.25,
        "manufacturing": 0.28,
        "assembly": 0.20,
        "inspection": 0.10,
        "operation": 0.07,
    },
}


def risk_level(score: float) -> str:
    if score < 0.25:
        return "low"
    if score < 0.50:
        return "medium"
    if score < 0.75:
        return "high"
    return "critical"


def clip01(values):
    return np.clip(values, 0.0, 1.0)


def sigmoid(values):
    return 1.0 / (1.0 + np.exp(-values))


def norm_dev(values, center, span):
    return np.abs(values - center) / span


def weighted_choice(rng: np.random.Generator, items: list[str], weights: list[float]) -> str:
    weights_array = np.asarray(weights, dtype=float)
    if weights_array.sum() <= 0:
        weights_array = np.ones(len(items))
    weights_array = weights_array / weights_array.sum()
    return str(rng.choice(items, p=weights_array))


def weighted_sample_without_replacement(rng: np.random.Generator, indices: np.ndarray, weights: np.ndarray, size: int) -> np.ndarray:
    if size <= 0:
        return np.asarray([], dtype=int)
    if size >= len(indices):
        return np.asarray(indices, dtype=int)
    weights_array = np.asarray(weights, dtype=float)
    weights_array = np.clip(weights_array, 0.0, None)
    if weights_array.sum() <= 0:
        weights_array = np.ones(len(indices), dtype=float)
    probabilities = weights_array / weights_array.sum()
    return rng.choice(indices, size=size, replace=False, p=probabilities).astype(int)


def add_level_column(df: pd.DataFrame, score_col: str, level_col: str) -> pd.DataFrame:
    df[level_col] = df[score_col].map(risk_level)
    return df


def generate_design(rng: np.random.Generator) -> pd.DataFrame:
    n = SCALE["design"]
    models = rng.choice(["6205", "6306", "NU205", "NJ306", "7206AC"], size=n)
    rated_load = rng.normal(28, 7, size=n).clip(10, 48)
    rated_speed = rng.normal(6200, 1500, size=n).clip(2500, 11000)
    clearance = rng.normal(0.030, 0.012, size=n).clip(0.006, 0.070)
    life = rng.normal(24000, 6500, size=n).clip(8000, 42000)
    lubrication = rng.choice(["grease", "oil_bath", "oil_mist"], size=n, p=[0.62, 0.28, 0.10])

    low_load_risk = clip01((24 - rated_load) / 16)
    high_speed_risk = clip01((rated_speed - 6500) / 3600)
    clearance_risk = clip01(norm_dev(clearance, 0.030, 0.030))
    life_risk = clip01((22000 - life) / 15000)
    risk = clip01(0.30 * low_load_risk + 0.25 * high_speed_risk + 0.30 * clearance_risk + 0.15 * life_risk)
    risk = clip01(risk + rng.normal(0, 0.035, size=n))

    df = pd.DataFrame(
        {
            "design_id": [f"BDES-{i:05d}" for i in range(1, n + 1)],
            "bearing_model": models,
            "rated_load": rated_load.round(2),
            "rated_speed": rated_speed.round(0).astype(int),
            "clearance": clearance.round(4),
            "expected_life_hours": life.round(0).astype(int),
            "lubrication_method": lubrication,
            "design_risk_score": risk.round(4),
        }
    )
    return add_level_column(df, "design_risk_score", "design_risk_level")


def generate_material(rng: np.random.Generator) -> pd.DataFrame:
    n = SCALE["material"]
    hardness = rng.normal(61, 2.7, size=n).clip(52, 68)
    inclusion = rng.gamma(2.0, 0.8, size=n).clip(0.1, 6.0)
    fatigue = rng.normal(1280, 170, size=n).clip(780, 1650)
    hardness_risk = clip01(norm_dev(hardness, 61, 7))
    inclusion_risk = clip01(inclusion / 5.0)
    fatigue_risk = clip01((1250 - fatigue) / 430)
    risk = clip01(0.28 * hardness_risk + 0.34 * inclusion_risk + 0.38 * fatigue_risk)
    risk = clip01(risk + rng.normal(0, 0.04, size=n))

    df = pd.DataFrame(
        {
            "material_batch_id": [f"BMAT-{i:05d}" for i in range(1, n + 1)],
            "material_grade": rng.choice(["GCr15", "GCr15SiMn", "M50", "SUJ2"], size=n, p=[0.55, 0.23, 0.12, 0.10]),
            "supplier_id": [f"SUP-{i:03d}" for i in rng.integers(1, 61, size=n)],
            "hardness": hardness.round(2),
            "inclusion_level": inclusion.round(3),
            "fatigue_strength": fatigue.round(1),
            "material_risk_score": risk.round(4),
        }
    )
    return add_level_column(df, "material_risk_score", "material_risk_level")


def generate_manufacturing(rng: np.random.Generator) -> pd.DataFrame:
    n = SCALE["manufacturing"]
    heat_temp = rng.normal(835, 22, size=n).clip(770, 900)
    temper_temp = rng.normal(175, 18, size=n).clip(120, 230)
    grinding_quality = rng.beta(5.0, 2.0, size=n).clip(0.30, 0.99)
    roundness = rng.gamma(2.0, 1.2, size=n).clip(0.2, 9.0)
    roughness = rng.gamma(2.0, 0.16, size=n).clip(0.05, 1.25)
    heat_risk = clip01(norm_dev(heat_temp, 835, 55))
    temper_risk = clip01(norm_dev(temper_temp, 175, 45))
    grind_risk = 1 - grinding_quality
    round_risk = clip01(roundness / 7.5)
    rough_risk = clip01(roughness / 1.0)
    risk = clip01(0.22 * heat_risk + 0.14 * temper_risk + 0.25 * grind_risk + 0.20 * round_risk + 0.19 * rough_risk)
    risk = clip01(risk + rng.normal(0, 0.035, size=n))

    df = pd.DataFrame(
        {
            "manufacturing_batch_id": [f"BMAN-{i:05d}" for i in range(1, n + 1)],
            "heat_treatment_temp": heat_temp.round(1),
            "tempering_temp": temper_temp.round(1),
            "grinding_quality": grinding_quality.round(4),
            "roundness_error": roundness.round(3),
            "roughness": roughness.round(3),
            "equipment_id": [f"EQ-GR-{i:03d}" for i in rng.integers(1, 121, size=n)],
            "manufacturing_risk_score": risk.round(4),
        }
    )
    return add_level_column(df, "manufacturing_risk_score", "manufacturing_risk_level")


def generate_assembly(rng: np.random.Generator) -> pd.DataFrame:
    n = SCALE["assembly"]
    fit = rng.normal(0.018, 0.009, size=n).clip(-0.006, 0.050)
    preload = rng.normal(1.00, 0.28, size=n).clip(0.30, 1.90)
    alignment = rng.gamma(2.0, 0.028, size=n).clip(0.002, 0.22)
    fit_risk = clip01(norm_dev(fit, 0.018, 0.026))
    preload_risk = clip01(norm_dev(preload, 1.0, 0.75))
    align_risk = clip01(alignment / 0.18)
    risk = clip01(0.32 * fit_risk + 0.30 * preload_risk + 0.38 * align_risk)
    risk = clip01(risk + rng.normal(0, 0.035, size=n))

    df = pd.DataFrame(
        {
            "assembly_id": [f"BASM-{i:05d}" for i in range(1, n + 1)],
            "interference_fit": fit.round(4),
            "preload": preload.round(3),
            "alignment_error": alignment.round(4),
            "operator_id": [f"OP-B-{i:03d}" for i in rng.integers(1, 151, size=n)],
            "station_id": [f"ST-B-{i:02d}" for i in rng.integers(1, 31, size=n)],
            "assembly_risk_score": risk.round(4),
        }
    )
    return add_level_column(df, "assembly_risk_score", "assembly_risk_level")


def generate_components(
    rng: np.random.Generator,
    design: pd.DataFrame,
    material: pd.DataFrame,
    manufacturing: pd.DataFrame,
    assembly: pd.DataFrame,
) -> pd.DataFrame:
    n = SCALE["component"]
    design_idx = rng.integers(0, len(design), size=n)
    material_idx = rng.integers(0, len(material), size=n)
    manufacturing_idx = rng.integers(0, len(manufacturing), size=n)
    operation_hours = rng.gamma(4.0, 950, size=n).clip(80, 15000)
    lubrication_age = rng.gamma(2.5, 0.18, size=n).clip(0.02, 1.0)
    operation_risk = clip01(0.45 * (operation_hours / 14000) + 0.40 * lubrication_age + rng.normal(0, 0.04, size=n))

    df = pd.DataFrame(
        {
            "bearing_id": [f"BRG-{i:05d}" for i in range(1, n + 1)],
            "design_id": design.iloc[design_idx]["design_id"].to_numpy(),
            "material_batch_id": material.iloc[material_idx]["material_batch_id"].to_numpy(),
            "manufacturing_batch_id": manufacturing.iloc[manufacturing_idx]["manufacturing_batch_id"].to_numpy(),
            "assembly_id": assembly["assembly_id"].to_numpy(),
            "maintenance_record_id": [f"BOPR-{i:05d}" for i in range(1, n + 1)],
            "operation_hours": operation_hours.round(1),
            "lubrication_age_ratio": lubrication_age.round(4),
            "design_risk_score": design.iloc[design_idx]["design_risk_score"].to_numpy(),
            "material_risk_score": material.iloc[material_idx]["material_risk_score"].to_numpy(),
            "manufacturing_risk_score": manufacturing.iloc[manufacturing_idx]["manufacturing_risk_score"].to_numpy(),
            "assembly_risk_score": assembly["assembly_risk_score"].to_numpy(),
            "operation_risk_score": operation_risk.round(4),
        }
    )
    return df


def generate_inspection(rng: np.random.Generator, components: pd.DataFrame) -> pd.DataFrame:
    assigned_risk = components[
        ["design_risk_score", "material_risk_score", "manufacturing_risk_score", "assembly_risk_score", "operation_risk_score"]
    ].astype(float).mean(axis=1).to_numpy()
    extra_n = SCALE["inspection"] - len(components)
    base_risk = np.concatenate([assigned_risk, rng.beta(2.2, 4.0, size=extra_n)])
    vibration = (0.28 + 2.4 * base_risk + rng.normal(0, 0.18, size=len(base_risk))).clip(0.08, 4.5)
    kurtosis = (2.6 + 7.5 * base_risk + rng.normal(0, 0.75, size=len(base_risk))).clip(1.6, 16.0)
    temperature = (39 + 42 * base_risk + rng.normal(0, 3.0, size=len(base_risk))).clip(28, 105)
    hardness_check = (61 + rng.normal(0, 1.5 + 2.5 * base_risk, size=len(base_risk))).clip(52, 69)
    roundness_check = (1.2 + 6.2 * base_risk + rng.normal(0, 0.7, size=len(base_risk))).clip(0.2, 11.0)

    metric_risk = clip01(
        0.30 * (vibration / 4.2)
        + 0.24 * ((kurtosis - 2.5) / 12)
        + 0.22 * ((temperature - 35) / 65)
        + 0.12 * norm_dev(hardness_check, 61, 8)
        + 0.12 * (roundness_check / 10)
    )
    result = np.where(metric_risk >= 0.62, "fail", np.where(metric_risk >= 0.38, "warning", "pass"))
    df = pd.DataFrame(
        {
            "inspection_id": [f"BINSP-{i:05d}" for i in range(1, len(base_risk) + 1)],
            "vibration_rms": vibration.round(4),
            "kurtosis": kurtosis.round(4),
            "temperature": temperature.round(2),
            "hardness_check": hardness_check.round(2),
            "roundness_check": roundness_check.round(3),
            "inspection_result": result,
            "inspection_risk_score": metric_risk.round(4),
        }
    )
    return add_level_column(df, "inspection_risk_score", "inspection_risk_level")


def severity_from_score(score: float) -> tuple[str, str]:
    if score < 0.52:
        return "slight", "007"
    if score < 0.74:
        return "medium", "014"
    return "severe", "021"


def severity_label(severity: str) -> tuple[str, str]:
    return {
        "slight": ("slight", "007"),
        "medium": ("medium", "014"),
        "severe": ("severe", "021"),
    }[severity]


def fault_scores(row: pd.Series) -> dict[str, float]:
    d = float(row.design_risk_score)
    m = float(row.material_risk_score)
    mf = float(row.manufacturing_risk_score)
    a = float(row.assembly_risk_score)
    ins = float(row.inspection_risk_score)
    op = float(row.operation_risk_score)
    return {
        "inner_race_fault": 0.08 + 0.16 * d + 0.24 * m + 0.32 * mf + 0.30 * a + 0.08 * ins + 0.10 * op,
        "outer_race_fault": 0.07 + 0.25 * d + 0.18 * m + 0.28 * mf + 0.32 * a + 0.10 * ins + 0.12 * op,
        "rolling_element_fault": 0.08 + 0.14 * d + 0.34 * m + 0.30 * mf + 0.18 * a + 0.08 * ins + 0.28 * op,
    }


def choose_true_reason(rng: np.random.Generator, row: pd.Series, fault_type: str) -> tuple[str, str, str]:
    candidates = {
        "design": (row.design_id, "BearingDesignSpec", row.design_risk_score),
        "material": (row.material_batch_id, "MaterialBatch", row.material_risk_score),
        "manufacturing": (row.manufacturing_batch_id, "ManufacturingBatch", row.manufacturing_risk_score),
        "assembly": (row.assembly_id, "AssemblyRecord", row.assembly_risk_score),
        "inspection": (row.inspection_id, "InspectionRecord", row.inspection_risk_score),
        "operation": (row.maintenance_record_id, "MaintenanceRecord", row.operation_risk_score),
    }
    stages = list(candidates)
    weights = []
    for stage in stages:
        base = FAULT_STAGE_PRIOR[fault_type][stage]
        risk = float(candidates[stage][2])
        weights.append(base * (0.25 + 1.8 * risk))
    stage = weighted_choice(rng, stages, weights)
    reason_id, reason_type, _ = candidates[stage]
    return str(reason_id), reason_type, stage


def select_normal_indices(rng: np.random.Generator, components: pd.DataFrame) -> set[int]:
    target_count = int(round(len(components) * NORMAL_RATIO))
    risks = components["overall_lifecycle_risk"].astype(float).to_numpy()
    low_risk_weights = np.power(1.0 - clip01(risks), 4.0)
    selected = weighted_sample_without_replacement(rng, components.index.to_numpy(dtype=int), low_risk_weights, target_count)
    return set(int(i) for i in selected)


def fault_type_quotas(non_normal_count: int) -> dict[str, int]:
    faults = list(FAULT_TARGET_SHARE)
    raw = {fault: non_normal_count * FAULT_TARGET_SHARE[fault] for fault in faults}
    quotas = {fault: int(np.floor(raw[fault])) for fault in faults}
    remainder = non_normal_count - sum(quotas.values())
    for fault in sorted(faults, key=lambda item: raw[item] - quotas[item], reverse=True)[:remainder]:
        quotas[fault] += 1
    return quotas


def assign_fault_types(rng: np.random.Generator, components: pd.DataFrame, normal_indices: set[int]) -> dict[int, str]:
    candidate_indices = np.asarray([idx for idx in components.index if int(idx) not in normal_indices], dtype=int)
    quotas = fault_type_quotas(len(candidate_indices))
    assignments: dict[int, str] = {}
    remaining = candidate_indices.copy()
    fault_order = list(FAULT_TARGET_SHARE)
    rng.shuffle(fault_order)
    for fault_type in fault_order:
        if len(remaining) == 0:
            break
        size = min(quotas[fault_type], len(remaining))
        weights = np.asarray([fault_scores(components.loc[idx])[fault_type] ** 2.2 for idx in remaining], dtype=float)
        selected = weighted_sample_without_replacement(rng, remaining, weights, size)
        for idx in selected:
            assignments[int(idx)] = fault_type
        selected_set = set(int(idx) for idx in selected)
        remaining = np.asarray([idx for idx in remaining if int(idx) not in selected_set], dtype=int)
    for idx in remaining:
        scores = fault_scores(components.loc[idx])
        assignments[int(idx)] = weighted_choice(rng, list(scores), [scores[fault] ** 2.2 for fault in scores])
    return assignments


def assign_severities(severity_scores: dict[int, float]) -> dict[int, tuple[str, str]]:
    ordered = sorted(severity_scores, key=severity_scores.get)
    total = len(ordered)
    slight_end = int(round(total * SEVERITY_DISTRIBUTION["slight"]))
    medium_end = slight_end + int(round(total * SEVERITY_DISTRIBUTION["medium"]))
    severities: dict[int, tuple[str, str]] = {}
    for rank, idx in enumerate(ordered):
        if rank < slight_end:
            severities[idx] = severity_label("slight")
        elif rank < medium_end:
            severities[idx] = severity_label("medium")
        else:
            severities[idx] = severity_label("severe")
    return severities


def build_rca_text(rng: np.random.Generator, fault_type: str, severity: str, stage: str, reason_id: str) -> tuple[str, str]:
    fault_cn = {
        "inner_race_fault": "内圈故障",
        "outer_race_fault": "外圈故障",
        "rolling_element_fault": "滚动体故障",
    }[fault_type]
    severity_cn = {"slight": "轻微", "medium": "中等", "severe": "严重"}[severity]
    stage_cn = {
        "design": "设计阶段",
        "material": "材料阶段",
        "manufacturing": "制造阶段",
        "assembly": "装配阶段",
        "inspection": "检测阶段",
        "operation": "运维阶段",
    }[stage]
    rca_templates = [
        f"诊断结果显示轴承出现{severity_cn}{fault_cn}，根因更可能来自{stage_cn}实体 {reason_id} 的风险累积。",
        f"结合振动、峭度和温升特征，{fault_cn}与{stage_cn}异常高度一致，优先追溯到 {reason_id}。",
        f"该样本的生命周期风险在{stage_cn}较突出，能够解释当前{severity_cn}{fault_cn}的主要失效模式。",
    ]
    evo_templates = [
        f"早期表现为局部接触应力升高，随后振动冲击增强，最终演化为{severity_cn}{fault_cn}。",
        f"风险首先在{stage_cn}形成隐患，运行中持续放大，并通过温度与振动指标表现出来。",
        f"异常由微小缺陷逐步发展为周期性冲击，检测指标随严重度上升而同步恶化。",
    ]
    return str(rng.choice(rca_templates)), str(rng.choice(evo_templates))


def generate_feedback(rng: np.random.Generator, components: pd.DataFrame) -> pd.DataFrame:
    rows = []
    normal_indices = select_normal_indices(rng, components)
    fault_assignments = assign_fault_types(rng, components, normal_indices)
    severity_scores = {
        idx: float(
            clip01(
                0.62 * fault_scores(components.loc[idx])[fault_type]
                + 0.28 * components.loc[idx, "inspection_risk_score"]
                + 0.10 * components.loc[idx, "overall_lifecycle_risk"]
            )
        )
        for idx, fault_type in fault_assignments.items()
    }
    severity_assignments = assign_severities(severity_scores)
    for i, row in components.iterrows():
        if i in normal_indices:
            rows.append(
                {
                    "feedback_id": f"BFB-{i + 1:05d}",
                    "bearing_id": row.bearing_id,
                    "raw_label": "Normal",
                    "fault_type": "normal",
                    "fault_position": "none",
                    "fault_severity": "normal",
                    "diagnosis_confidence": round(float(0.86 + rng.random() * 0.10), 4),
                    "rca_text": "该轴承样本生命周期风险较低，未形成明确故障追溯链。",
                    "fault_evolution_text": "运行状态平稳，检测指标未呈现持续恶化趋势。",
                    "rca_confidence": round(float(0.78 + rng.random() * 0.12), 4),
                    "true_reason_id": "NORMAL_BASELINE",
                    "true_reason_type": "QualityFeedback",
                    "true_stage": "feedback",
                }
            )
            continue

        fault_type = fault_assignments[i]
        severity_score = severity_scores[i]
        severity, suffix = severity_assignments[i]
        label = f"{FAULT_LABELS[fault_type]['prefix']}{suffix}"
        reason_id, reason_type, stage = choose_true_reason(rng, row, fault_type)
        rca_text, evo_text = build_rca_text(rng, fault_type, severity, stage, reason_id)
        confidence_base = 0.58 + 0.30 * severity_score + 0.10 * row.inspection_risk_score
        rows.append(
            {
                "feedback_id": f"BFB-{i + 1:05d}",
                "bearing_id": row.bearing_id,
                "raw_label": label,
                "fault_type": fault_type,
                "fault_position": FAULT_LABELS[fault_type]["position"],
                "fault_severity": severity,
                "diagnosis_confidence": round(float(clip01(confidence_base + rng.normal(0, 0.04))), 4),
                "rca_text": rca_text,
                "fault_evolution_text": evo_text,
                "rca_confidence": round(float(clip01(confidence_base - 0.03 + rng.normal(0, 0.05))), 4),
                "true_reason_id": reason_id,
                "true_reason_type": reason_type,
                "true_stage": stage,
            }
        )
    return pd.DataFrame(rows)


def save_csv(df: pd.DataFrame, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    df.to_csv(path, index=False, encoding="utf-8-sig")
    print(f"{path} rows={len(df)}")


def main() -> None:
    rng = np.random.default_rng(SEED)
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    REPORT_DIR.mkdir(parents=True, exist_ok=True)

    design = generate_design(rng)
    material = generate_material(rng)
    manufacturing = generate_manufacturing(rng)
    assembly = generate_assembly(rng)
    components = generate_components(rng, design, material, manufacturing, assembly)
    inspection = generate_inspection(rng, components)
    components["inspection_id"] = inspection.iloc[: len(components)]["inspection_id"].to_numpy()
    components["inspection_risk_score"] = inspection.iloc[: len(components)]["inspection_risk_score"].to_numpy()
    risk_cols = [
        "design_risk_score",
        "material_risk_score",
        "manufacturing_risk_score",
        "assembly_risk_score",
        "inspection_risk_score",
        "operation_risk_score",
    ]
    components["overall_lifecycle_risk"] = (
        0.16 * components["design_risk_score"].astype(float)
        + 0.20 * components["material_risk_score"].astype(float)
        + 0.24 * components["manufacturing_risk_score"].astype(float)
        + 0.18 * components["assembly_risk_score"].astype(float)
        + 0.10 * components["inspection_risk_score"].astype(float)
        + 0.12 * components["operation_risk_score"].astype(float)
    ).round(4)
    components["overall_lifecycle_risk_level"] = components["overall_lifecycle_risk"].map(risk_level)
    feedback = generate_feedback(rng, components)

    save_csv(components, DATA_DIR / "bearing_component.csv")
    save_csv(design, DATA_DIR / "bearing_design.csv")
    save_csv(material, DATA_DIR / "bearing_material.csv")
    save_csv(manufacturing, DATA_DIR / "bearing_manufacturing.csv")
    save_csv(assembly, DATA_DIR / "bearing_assembly.csv")
    save_csv(inspection, DATA_DIR / "bearing_inspection.csv")
    save_csv(feedback, DATA_DIR / "bearing_feedback.csv")


if __name__ == "__main__":
    main()
