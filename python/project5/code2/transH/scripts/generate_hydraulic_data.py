from __future__ import annotations

from pathlib import Path

import numpy as np
import pandas as pd


SEED = 20260603
ROOT = Path(__file__).resolve().parents[1]
DATA_DIR = ROOT / "data" / "hydraulic"
REPORT_DIR = ROOT / "outputs" / "hydraulic" / "reports"

SCALE = {
    "component": 5000,
    "design": 1200,
    "material": 600,
    "manufacturing": 1200,
    "assembly": 5000,
    "inspection": 7000,
    "feedback": 5000,
}

COMPONENTS = {
    "C001": "液压泵总成",
    "C002": "方向控制阀",
    "C003": "作动筒",
    "C004": "溢流阀",
    "C005": "蓄能器",
    "C006": "冷却器",
    "C007": "过滤器",
    "C008": "液压油箱",
    "C009": "单向阀",
    "C010": "节流阀",
    "C011": "管路总成",
    "C012": "卸荷阀",
}

SENSORS = ["PS1", "PS2", "PS3", "PS4", "PS5", "PS6", "EPS1", "FS1", "FS2", "TS1", "TS2", "TS3", "TS4", "VS1"]

SENSOR_MAP = {
    "C001": {"VS1": 0.50, "PS1": 0.30, "TS1": 0.20},
    "C002": {"PS3": 0.55, "FS1": 0.30, "PS4": 0.15},
    "C003": {"PS4": 0.35, "PS5": 0.35, "FS2": 0.30},
    "C004": {"PS2": 0.60, "EPS1": 0.40},
    "C005": {"PS6": 0.75, "PS2": 0.25},
    "C006": {"TS2": 0.50, "TS3": 0.50},
    "C007": {"TS4": 0.55, "FS1": 0.45},
    "C008": {"FS1": 0.40, "TS3": 0.35, "FS2": 0.25},
    "C009": {"PS2": 0.45, "PS3": 0.35, "FS1": 0.20},
    "C010": {"FS1": 0.40, "PS3": 0.30, "PS5": 0.30},
    "C011": {"PS1": 0.25, "PS2": 0.25, "PS6": 0.25, "VS1": 0.25},
    "C012": {"PS1": 0.30, "EPS1": 0.30, "FS1": 0.25, "TS1": 0.15},
}

NEIGHBORS = {
    "C001": ["C011", "C012", "C008"],
    "C002": ["C009", "C010", "C003"],
    "C003": ["C002", "C011", "C010"],
    "C004": ["C005", "C012", "C009"],
    "C005": ["C004", "C011"],
    "C006": ["C008", "C007"],
    "C007": ["C008", "C010"],
    "C008": ["C001", "C006", "C007"],
    "C009": ["C002", "C004", "C010"],
    "C010": ["C002", "C007", "C009"],
    "C011": ["C001", "C003", "C005"],
    "C012": ["C001", "C004", "C009"],
}

FAULT_AFFINITY = {
    "oil_leakage": ["C011", "C003", "C001", "C004", "C012"],
    "valve_stuck": ["C002", "C004", "C009", "C010", "C012"],
    "pressure_abnormal": ["C001", "C004", "C005", "C011", "C012"],
    "flow_abnormal": ["C002", "C007", "C008", "C010", "C011"],
    "temperature_abnormal": ["C006", "C007", "C008", "C001"],
    "vibration_abnormal": ["C001", "C003", "C011"],
}

DIAGNOSIS_FAULT_NAME = {
    "C001": "液压泵故障",
    "C002": "方向阀故障",
    "C003": "作动筒故障",
    "C004": "溢流阀故障",
    "C005": "蓄能器故障",
    "C006": "冷却器故障",
    "C007": "过滤器堵塞",
    "C008": "油箱故障",
    "C009": "单向阀故障",
    "C010": "节流阀故障",
    "C011": "管路泄漏",
    "C012": "卸荷阀故障",
}

SUBTYPES = {
    "C001": [("C001-S1", "柱塞副磨损", "柱塞-缸体配合副"), ("C001-S2", "配流盘气蚀", "配流盘"), ("C001-S3", "轴承疲劳剥落", "主轴滚动轴承"), ("C001-S4", "轴封泄漏", "主轴密封件")],
    "C002": [("C002-S1", "阀芯卡滞", "阀芯-阀体配合面"), ("C002-S2", "电磁铁烧毁", "电磁线圈"), ("C002-S3", "复位弹簧断裂", "复位弹簧"), ("C002-S4", "阀体冲蚀", "阀体内部流道")],
    "C003": [("C003-S1", "活塞密封圈老化", "活塞密封圈"), ("C003-S2", "活塞杆拉伤", "活塞杆表面"), ("C003-S3", "缸筒内壁刮伤", "缸筒内壁"), ("C003-S4", "端盖密封渗漏", "端盖O形圈")],
    "C004": [("C004-S1", "先导阀芯卡滞", "-"), ("C004-S2", "主阀芯弹簧疲劳", "-"), ("C004-S3", "阀口冲蚀泄漏", "-"), ("C004-S4", "阻尼孔堵塞", "-")],
    "C005": [("C005-S1", "气囊破裂", "-"), ("C005-S2", "预充压力不足", "-"), ("C005-S3", "菌形阀密封失效", "-")],
    "C006": [("C006-S1", "换热效率下降", "-"), ("C006-S2", "管束堵塞", "-"), ("C006-S3", "冷却介质泄漏", "-")],
    "C007": [("C007-S1", "滤芯堵塞", "-"), ("C007-S2", "旁通阀异常开启", "-"), ("C007-S3", "滤芯破损", "-")],
    "C008": [("C008-S1", "油位过低", "-"), ("C008-S2", "空气滤清器堵塞", "-"), ("C008-S3", "回油泡沫化", "-")],
    "C009": [("C009-S1", "阀芯卡死", "-"), ("C009-S2", "反向泄漏", "-"), ("C009-S3", "弹簧断裂失效", "-")],
    "C010": [("C010-S1", "节流口堵塞", "-"), ("C010-S2", "阀芯卡滞", "-"), ("C010-S3", "节流口冲蚀", "-")],
    "C011": [("C011-S1", "管接头渗漏", "-"), ("C011-S2", "管路裂纹泄漏", "-"), ("C011-S3", "软管老化破裂", "-"), ("C011-S4", "管路堵塞", "-")],
    "C012": [("C012-S1", "卸荷压力漂移", "-"), ("C012-S2", "阀芯卡滞常开", "-"), ("C012-S3", "导阀密封失效", "-")],
}

FAULT_STAGE_PRIOR = {
    "oil_leakage": {"design": 0.18, "material": 0.16, "manufacturing": 0.12, "assembly": 0.22, "inspection": 0.08, "operation": 0.24},
    "valve_stuck": {"design": 0.10, "material": 0.14, "manufacturing": 0.24, "assembly": 0.12, "inspection": 0.08, "operation": 0.32},
    "pressure_abnormal": {"design": 0.20, "material": 0.10, "manufacturing": 0.14, "assembly": 0.14, "inspection": 0.08, "operation": 0.34},
    "flow_abnormal": {"design": 0.16, "material": 0.10, "manufacturing": 0.16, "assembly": 0.12, "inspection": 0.10, "operation": 0.36},
    "temperature_abnormal": {"design": 0.12, "material": 0.12, "manufacturing": 0.10, "assembly": 0.08, "inspection": 0.10, "operation": 0.48},
    "vibration_abnormal": {"design": 0.12, "material": 0.12, "manufacturing": 0.18, "assembly": 0.20, "inspection": 0.08, "operation": 0.30},
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


def weighted_choice(rng: np.random.Generator, items: list[str], weights: list[float]) -> str:
    weights_array = np.asarray(weights, dtype=float)
    if weights_array.sum() <= 0:
        weights_array = np.ones(len(items))
    return str(rng.choice(items, p=weights_array / weights_array.sum()))


def add_level(df: pd.DataFrame, score_col: str, level_col: str) -> pd.DataFrame:
    df[level_col] = df[score_col].map(risk_level)
    return df


def make_component_codes(rng: np.random.Generator) -> list[str]:
    codes = ["C011"] * 850
    remaining = SCALE["component"] - len(codes)
    other_codes = [code for code in COMPONENTS if code != "C011"]
    base = remaining // len(other_codes)
    extra = remaining % len(other_codes)
    for i, code in enumerate(other_codes):
        codes.extend([code] * (base + (1 if i < extra else 0)))
    rng.shuffle(codes)
    return codes


def generate_design(rng: np.random.Generator) -> pd.DataFrame:
    rows = []
    codes = np.resize(list(COMPONENTS), SCALE["design"])
    rng.shuffle(codes)
    for i, code in enumerate(codes, start=1):
        pressure_base = 24 if code in {"C001", "C004", "C005", "C011", "C012"} else 18
        flow_base = 90 if code in {"C001", "C002", "C008", "C010"} else 55
        rated_pressure = float(rng.normal(pressure_base, 4.5))
        rated_flow = float(rng.normal(flow_base, 18))
        temp_limit = float(rng.normal(85 if code in {"C006", "C007", "C008"} else 78, 10))
        margin = float(rng.beta(4.0, 2.5) * 0.45 + 0.05)
        pressure_risk = np.clip((pressure_base - rated_pressure) / 10, 0, 1)
        temp_risk = np.clip((76 - temp_limit) / 28, 0, 1)
        margin_risk = np.clip((0.42 - margin) / 0.40, 0, 1)
        risk = np.clip(0.52 * margin_risk + 0.25 * pressure_risk + 0.23 * temp_risk + rng.normal(0, 0.035), 0, 1)
        rows.append(
            {
                "design_id": f"HDES-{i:05d}",
                "component_code": code,
                "component_name": COMPONENTS[code],
                "rated_pressure": round(max(6, rated_pressure), 2),
                "rated_flow": round(max(12, rated_flow), 2),
                "temperature_limit": round(max(45, temp_limit), 2),
                "design_margin": round(margin, 4),
                "design_risk_score": round(float(risk), 4),
            }
        )
    return add_level(pd.DataFrame(rows), "design_risk_score", "design_risk_level")


def generate_material(rng: np.random.Generator) -> pd.DataFrame:
    n = SCALE["material"]
    hardness = rng.normal(58, 4.2, size=n).clip(42, 72)
    aging = rng.beta(2.1, 4.2, size=n)
    corrosion = rng.beta(5.0, 2.0, size=n)
    risk = clip01(0.25 * (np.abs(hardness - 58) / 16) + 0.40 * aging + 0.35 * (1 - corrosion) + rng.normal(0, 0.04, size=n))
    df = pd.DataFrame(
        {
            "material_batch_id": [f"HMAT-{i:05d}" for i in range(1, n + 1)],
            "material_grade": rng.choice(["40Cr", "35CrMo", "304", "316L", "NBR", "FKM"], size=n),
            "supplier_id": [f"HSUP-{i:03d}" for i in rng.integers(1, 81, size=n)],
            "hardness": hardness.round(2),
            "aging_level": aging.round(4),
            "corrosion_resistance": corrosion.round(4),
            "material_risk_score": risk.round(4),
        }
    )
    return add_level(df, "material_risk_score", "material_risk_level")


def generate_manufacturing(rng: np.random.Generator) -> pd.DataFrame:
    n = SCALE["manufacturing"]
    process = rng.choice(["machining", "grinding", "cleaning", "welding", "heat_treatment"], size=n)
    machining = rng.gamma(2.0, 0.018, size=n).clip(0.002, 0.16)
    roughness = rng.gamma(2.0, 0.25, size=n).clip(0.05, 2.3)
    cleanliness = rng.beta(2.5, 3.8, size=n)
    risk = clip01(0.36 * (machining / 0.14) + 0.30 * (roughness / 2.0) + 0.34 * cleanliness + rng.normal(0, 0.04, size=n))
    df = pd.DataFrame(
        {
            "manufacturing_batch_id": [f"HMAN-{i:05d}" for i in range(1, n + 1)],
            "process_type": process,
            "equipment_id": [f"HEQ-{i:03d}" for i in rng.integers(1, 141, size=n)],
            "machining_error": machining.round(4),
            "surface_roughness": roughness.round(4),
            "cleanliness_level": cleanliness.round(4),
            "manufacturing_risk_score": risk.round(4),
        }
    )
    return add_level(df, "manufacturing_risk_score", "manufacturing_risk_level")


def generate_assembly(rng: np.random.Generator) -> pd.DataFrame:
    n = SCALE["assembly"]
    torque = rng.normal(42, 9.0, size=n).clip(12, 75)
    seal = rng.normal(1.0, 0.28, size=n).clip(0.25, 1.9)
    alignment = rng.gamma(2.0, 0.035, size=n).clip(0.002, 0.28)
    torque_risk = np.abs(torque - 42) / 38
    seal_risk = np.abs(seal - 1.0) / 0.8
    risk = clip01(0.35 * torque_risk + 0.32 * seal_risk + 0.33 * (alignment / 0.25) + rng.normal(0, 0.04, size=n))
    df = pd.DataFrame(
        {
            "assembly_id": [f"HASM-{i:05d}" for i in range(1, n + 1)],
            "assembly_torque": torque.round(3),
            "seal_preload": seal.round(3),
            "alignment_error": alignment.round(4),
            "operator_id": [f"OP-H-{i:03d}" for i in rng.integers(1, 181, size=n)],
            "station_id": [f"ST-H-{i:02d}" for i in rng.integers(1, 41, size=n)],
            "assembly_risk_score": risk.round(4),
        }
    )
    return add_level(df, "assembly_risk_score", "assembly_risk_level")


def choose_design_ids(rng: np.random.Generator, codes: list[str], design: pd.DataFrame) -> list[str]:
    grouped = {code: group["design_id"].to_numpy() for code, group in design.groupby("component_code")}
    return [str(rng.choice(grouped[code])) for code in codes]


def generate_components(
    rng: np.random.Generator,
    design: pd.DataFrame,
    material: pd.DataFrame,
    manufacturing: pd.DataFrame,
    assembly: pd.DataFrame,
) -> pd.DataFrame:
    codes = make_component_codes(rng)
    n = len(codes)
    mat_idx = rng.integers(0, len(material), size=n)
    man_idx = rng.integers(0, len(manufacturing), size=n)
    operation_hours = rng.gamma(4.0, 850, size=n).clip(60, 16000)
    oil_age = rng.beta(2.3, 3.5, size=n)
    operation_risk = clip01(0.48 * (operation_hours / 15000) + 0.52 * oil_age + rng.normal(0, 0.04, size=n))
    df = pd.DataFrame(
        {
            "component_uid": [f"HCU-{i:05d}" for i in range(1, n + 1)],
            "component_code": codes,
            "component_name": [COMPONENTS[c] for c in codes],
            "design_id": choose_design_ids(rng, codes, design),
            "material_batch_id": material.iloc[mat_idx]["material_batch_id"].to_numpy(),
            "manufacturing_batch_id": manufacturing.iloc[man_idx]["manufacturing_batch_id"].to_numpy(),
            "assembly_id": assembly["assembly_id"].to_numpy(),
            "maintenance_record_id": [f"HOPR-{i:05d}" for i in range(1, n + 1)],
            "operation_hours": operation_hours.round(1),
            "oil_age_ratio": oil_age.round(4),
            "material_risk_score": material.iloc[mat_idx]["material_risk_score"].to_numpy(),
            "manufacturing_risk_score": manufacturing.iloc[man_idx]["manufacturing_risk_score"].to_numpy(),
            "assembly_risk_score": assembly["assembly_risk_score"].to_numpy(),
            "operation_risk_score": operation_risk.round(4),
            "pipe_design_id": "",
            "pipe_design_risk_score": 0.0,
        }
    )
    design_risk = design.set_index("design_id")["design_risk_score"]
    df["design_risk_score"] = df["design_id"].map(design_risk).astype(float).round(4)
    return df


def generate_pipe_design(rng: np.random.Generator, components: pd.DataFrame) -> pd.DataFrame:
    c011 = components[components["component_code"] == "C011"].copy()
    rows = []
    for i, uid in enumerate(c011["component_uid"], start=1):
        r = float(rng.uniform(5, 40))
        l1 = float(rng.uniform(max(80, 2 * r), 450))
        l2 = float(rng.uniform(max(80, 2 * r), 450))
        theta1 = float(rng.uniform(60, 160))
        theta2 = float(rng.uniform(60, 160))
        l3 = float(rng.uniform(max(50, 2 * r), 260))
        angle_extreme = (abs(theta1 - 110) + abs(theta2 - 110)) / 100
        stress = 130 + 780 * (1 / r) + 130 * angle_extreme + 0.10 * (l1 + l2) + rng.normal(0, 18)
        allowable = float(rng.normal(360, 35))
        deformation = 0.15 + 0.0025 * (l1 + l2) + 3.8 * (1 / r) + 0.40 * angle_extreme + rng.normal(0, 0.06)
        stress_ratio = stress / allowable
        risk = np.clip(0.58 * stress_ratio + 0.16 * (deformation / 2.5) + 0.16 * ((40 - r) / 35) + 0.10 * angle_extreme - 0.35, 0, 1)
        rows.append(
            {
                "pipe_design_id": f"HPIP-{i:05d}",
                "component_uid": uid,
                "L1": round(l1, 3),
                "L2": round(l2, 3),
                "L3": round(l3, 3),
                "theta1": round(theta1, 3),
                "theta2": round(theta2, 3),
                "R": round(r, 3),
                "max_deformation": round(max(0.02, deformation), 4),
                "max_equivalent_stress": round(max(80, stress), 3),
                "allowable_stress": round(max(240, allowable), 3),
                "design_risk_score": round(float(risk), 4),
                "design_risk_level": risk_level(float(risk)),
            }
        )
    return pd.DataFrame(rows)


def generate_inspection(rng: np.random.Generator, components: pd.DataFrame) -> pd.DataFrame:
    base = components[
        ["design_risk_score", "material_risk_score", "manufacturing_risk_score", "assembly_risk_score", "operation_risk_score"]
    ].astype(float).mean(axis=1).to_numpy()
    extra = rng.beta(2.2, 4.2, size=SCALE["inspection"] - len(base))
    risks = np.concatenate([base, extra])
    pressure = (20 + rng.normal(0, 1.8, size=len(risks)) + 8 * risks).clip(6, 40)
    leakage = (0.02 + 0.90 * risks + rng.normal(0, 0.06, size=len(risks))).clip(0, 1.8)
    flow = (75 + rng.normal(0, 8, size=len(risks)) - 22 * risks).clip(15, 125)
    temp = (42 + 40 * risks + rng.normal(0, 4, size=len(risks))).clip(22, 115)
    vibration = (0.12 + 2.4 * risks + rng.normal(0, 0.18, size=len(risks))).clip(0.02, 4.2)
    metric = clip01(0.23 * (np.abs(pressure - 22) / 18) + 0.25 * (leakage / 1.5) + 0.18 * (np.abs(flow - 75) / 60) + 0.18 * ((temp - 35) / 80) + 0.16 * (vibration / 4.0))
    result = np.where(metric >= 0.62, "fail", np.where(metric >= 0.38, "warning", "pass"))
    df = pd.DataFrame(
        {
            "inspection_id": [f"HINSP-{i:05d}" for i in range(1, len(risks) + 1)],
            "pressure_test_value": pressure.round(4),
            "leakage_rate": leakage.round(5),
            "flow_test_value": flow.round(4),
            "temperature_test_value": temp.round(4),
            "vibration_value": vibration.round(5),
            "inspection_result": result,
            "inspection_risk_score": metric.round(4),
        }
    )
    return add_level(df, "inspection_risk_score", "inspection_risk_level")


def update_component_risks(components: pd.DataFrame, inspection: pd.DataFrame, pipe: pd.DataFrame) -> pd.DataFrame:
    components = components.copy()
    components["inspection_id"] = inspection.iloc[: len(components)]["inspection_id"].to_numpy()
    components["inspection_risk_score"] = inspection.iloc[: len(components)]["inspection_risk_score"].to_numpy()
    pipe_map = pipe.set_index("component_uid")
    c011_mask = components["component_code"] == "C011"
    components.loc[c011_mask, "pipe_design_id"] = components.loc[c011_mask, "component_uid"].map(pipe_map["pipe_design_id"])
    components.loc[c011_mask, "pipe_design_risk_score"] = components.loc[c011_mask, "component_uid"].map(pipe_map["design_risk_score"]).astype(float)
    components.loc[c011_mask, "design_risk_score"] = (
        0.35 * components.loc[c011_mask, "design_risk_score"].astype(float) + 0.65 * components.loc[c011_mask, "pipe_design_risk_score"].astype(float)
    ).round(4)
    components["overall_lifecycle_risk"] = (
        0.18 * components["design_risk_score"].astype(float)
        + 0.16 * components["material_risk_score"].astype(float)
        + 0.18 * components["manufacturing_risk_score"].astype(float)
        + 0.17 * components["assembly_risk_score"].astype(float)
        + 0.11 * components["inspection_risk_score"].astype(float)
        + 0.20 * components["operation_risk_score"].astype(float)
    ).round(4)
    components["overall_lifecycle_risk_level"] = components["overall_lifecycle_risk"].map(risk_level)
    return components


def fault_scores(row: pd.Series) -> dict[str, float]:
    d, m, mf, a, ins, op = [float(row[c]) for c in ["design_risk_score", "material_risk_score", "manufacturing_risk_score", "assembly_risk_score", "inspection_risk_score", "operation_risk_score"]]
    pipe = float(row.get("pipe_design_risk_score", 0.0))
    code = row.component_code
    scores = {
        "oil_leakage": 0.24 * d + 0.22 * m + 0.12 * mf + 0.25 * a + 0.07 * ins + 0.25 * op + 0.45 * pipe,
        "valve_stuck": 0.10 * d + 0.16 * m + 0.32 * mf + 0.10 * a + 0.08 * ins + 0.30 * op,
        "pressure_abnormal": 0.28 * d + 0.10 * m + 0.16 * mf + 0.16 * a + 0.10 * ins + 0.32 * op + 0.25 * pipe,
        "flow_abnormal": 0.20 * d + 0.12 * m + 0.24 * mf + 0.14 * a + 0.12 * ins + 0.30 * op + 0.16 * pipe,
        "temperature_abnormal": 0.15 * d + 0.14 * m + 0.10 * mf + 0.08 * a + 0.14 * ins + 0.44 * op,
        "vibration_abnormal": 0.18 * d + 0.14 * m + 0.18 * mf + 0.28 * a + 0.10 * ins + 0.30 * op + 0.30 * pipe,
    }
    for fault, codes in FAULT_AFFINITY.items():
        if code in codes:
            scores[fault] += 0.18
    return scores


def choose_true_reason(rng: np.random.Generator, row: pd.Series, fault_type: str) -> tuple[str, str, str]:
    candidates = {
        "design": (row.pipe_design_id if row.component_code == "C011" and row.pipe_design_id else row.design_id, "PipeDesignParam" if row.component_code == "C011" and row.pipe_design_id else "DesignSpec", max(float(row.design_risk_score), float(row.pipe_design_risk_score))),
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
        weights.append(base * (0.25 + 1.9 * risk))
    stage = weighted_choice(rng, stages, weights)
    reason_id, reason_type, _ = candidates[stage]
    return str(reason_id), reason_type, stage


def severity_label(score: float) -> str:
    if score < 0.38:
        return "slight"
    if score < 0.68:
        return "medium"
    return "severe"


def build_text(rng: np.random.Generator, fault_type: str, severity: str, component_name: str, stage: str, reason_id: str) -> tuple[str, str]:
    fault_cn = {
        "oil_leakage": "泄漏异常",
        "valve_stuck": "阀芯卡滞",
        "pressure_abnormal": "压力异常",
        "flow_abnormal": "流量异常",
        "temperature_abnormal": "温度异常",
        "vibration_abnormal": "振动异常",
    }[fault_type]
    severity_cn = {"slight": "轻微", "medium": "中度", "severe": "重度"}[severity]
    stage_cn = {"design": "设计阶段", "material": "材料阶段", "manufacturing": "制造阶段", "assembly": "装配阶段", "inspection": "检测阶段", "operation": "运维阶段"}[stage]
    rca = [
        f"{component_name}出现{severity_cn}{fault_cn}，生命周期风险集中在{stage_cn}，建议优先追溯 {reason_id}。",
        f"结合传感器能量和检测记录，{component_name}的{fault_cn}与{stage_cn}高风险实体 {reason_id} 一致。",
        f"该案例表现为{severity_cn}{fault_cn}，根因更可能由{stage_cn}隐患累积触发。",
    ]
    evo = [
        f"隐患先导致局部性能退化，随后传感器能量向关联通道集中，最终形成{fault_cn}。",
        f"早期异常较弱，运行载荷放大后出现指标漂移，并逐步演化为{severity_cn}故障。",
        f"{stage_cn}风险与运行工况耦合后引起状态劣化，检测指标随严重度同步升高。",
    ]
    return str(rng.choice(rca)), str(rng.choice(evo))


def generate_sensor_energy(rng: np.random.Generator, case_id: str, root_code: str, secondary_code: str, fault_type: str, severity_score: float) -> dict[str, float]:
    energy = {sensor: float(rng.uniform(0.02, 0.16)) for sensor in SENSORS}
    for sensor, weight in SENSOR_MAP[root_code].items():
        energy[sensor] += 0.45 * weight + 0.52 * severity_score + rng.uniform(0.04, 0.12)
    for sensor, weight in SENSOR_MAP[secondary_code].items():
        energy[sensor] += 0.22 * weight + 0.22 * severity_score + rng.uniform(0.01, 0.08)
    if fault_type == "pressure_abnormal":
        for sensor in [s for s in SENSORS if s.startswith("PS")]:
            energy[sensor] += 0.18 * severity_score
    elif fault_type == "flow_abnormal":
        for sensor in ["FS1", "FS2"]:
            energy[sensor] += 0.22 * severity_score
    elif fault_type == "temperature_abnormal":
        for sensor in ["TS1", "TS2", "TS3", "TS4"]:
            energy[sensor] += 0.22 * severity_score
    elif fault_type == "vibration_abnormal":
        energy["VS1"] += 0.35 * severity_score
    elif fault_type == "valve_stuck":
        energy["PS3"] += 0.26 * severity_score
        energy["FS1"] += 0.20 * severity_score
    elif fault_type == "oil_leakage":
        for sensor in ["PS1", "PS2", "PS6"]:
            energy[sensor] += 0.18 * severity_score
    row = {"case_id": case_id}
    row.update({sensor: round(min(1.0, value), 5) for sensor, value in energy.items()})
    return row


def choose_root_sensor(rng: np.random.Generator, code: str) -> str:
    sensors = list(SENSOR_MAP[code])
    weights = list(SENSOR_MAP[code].values())
    return weighted_choice(rng, sensors, weights)


def generate_feedback_bundle(rng: np.random.Generator, components: pd.DataFrame):
    feedback_rows, rca_rows, sensor_rows, diag_rows, subtype_rows = [], [], [], [], []
    for idx, row in components.iterrows():
        case_id = f"HCASE-{idx + 1:05d}"
        feedback_id = f"HFB-{idx + 1:05d}"
        scores = fault_scores(row)
        fault_type = weighted_choice(rng, list(scores), [scores[k] ** 2.1 for k in scores])
        secondary_code = weighted_choice(rng, NEIGHBORS[row.component_code], [1.0] * len(NEIGHBORS[row.component_code]))
        severity_score = float(np.clip(0.52 * scores[fault_type] + 0.22 * row.overall_lifecycle_risk + 0.18 * row.inspection_risk_score + 0.08 * rng.random(), 0, 1))
        severity = severity_label(severity_score)
        root_for_rca = row.component_code if rng.random() > 0.12 else secondary_code
        sensor_row = generate_sensor_energy(rng, case_id, root_for_rca, secondary_code, fault_type, severity_score)
        sensor_rows.append(sensor_row)
        root_sensor = choose_root_sensor(rng, root_for_rca)
        secondary_sensor = choose_root_sensor(rng, secondary_code)
        sensor_values = np.array([sensor_row[s] for s in SENSORS], dtype=float)
        concentration = float(sensor_values.max() / max(sensor_values.sum(), 1e-6))
        root_sensor_conf = float(np.clip(0.48 + 0.48 * sensor_row[root_sensor], 0, 1))
        component_conf = float(np.clip(0.50 + 0.35 * severity_score + 0.20 * sensor_row[root_sensor], 0, 1))
        diagnosis_conf = float(np.clip(0.56 + 0.30 * severity_score + 0.16 * row.inspection_risk_score + rng.normal(0, 0.035), 0, 1))
        rca_conf = float(np.clip(0.35 * diagnosis_conf + 0.40 * component_conf + 0.25 * concentration + rng.normal(0, 0.025), 0, 1))
        reason_id, reason_type, true_stage = choose_true_reason(rng, row, fault_type)
        rca_text, evolution_text = build_text(rng, fault_type, severity, row.component_name, true_stage, reason_id)
        feedback_rows.append(
            {
                "feedback_id": feedback_id,
                "case_id": case_id,
                "component_uid": row.component_uid,
                "component_code": row.component_code,
                "component_name": row.component_name,
                "fault_type": fault_type,
                "fault_position": row.component_name,
                "fault_severity": severity,
                "diagnosis_confidence": round(diagnosis_conf, 4),
                "rca_text": rca_text,
                "fault_evolution_text": evolution_text,
                "rca_confidence": round(rca_conf, 4),
                "true_reason_id": reason_id,
                "true_reason_type": reason_type,
                "true_stage": true_stage,
            }
        )
        rca_rows.append(
            {
                "case_id": case_id,
                "feedback_id": feedback_id,
                "root_sensor": root_sensor,
                "root_sensor_confidence": round(root_sensor_conf, 4),
                "secondary_sensor": secondary_sensor,
                "secondary_sensor_confidence": round(float(np.clip(0.42 + 0.42 * sensor_row[secondary_sensor], 0, 1)), 4),
                "root_component_code": root_for_rca,
                "root_component_name": COMPONENTS[root_for_rca],
                "component_confidence": round(component_conf, 4),
                "rca_confidence": round(rca_conf, 4),
                "rca_text": rca_text,
                "fault_evolution_text": evolution_text,
            }
        )
        diag_rows.extend(generate_diagnosis_rows(rng, case_id, root_for_rca, secondary_code, fault_type, severity_score))
        subtype_rows.extend(generate_subtype_rows(rng, case_id, root_for_rca, fault_type))
    return (
        pd.DataFrame(feedback_rows),
        pd.DataFrame(rca_rows),
        pd.DataFrame(sensor_rows),
        pd.DataFrame(diag_rows),
        pd.DataFrame(subtype_rows),
    )


def generate_diagnosis_rows(rng: np.random.Generator, case_id: str, root_code: str, secondary_code: str, fault_type: str, severity_score: float) -> list[dict]:
    rows = []
    for code, name in COMPONENTS.items():
        if code == root_code:
            sev = np.clip(0.58 + 0.38 * severity_score + rng.normal(0, 0.04), 0, 1)
        elif code == secondary_code or code in NEIGHBORS[root_code]:
            sev = np.clip(0.18 + 0.36 * severity_score + rng.normal(0, 0.05), 0, 0.76)
        else:
            sev = np.clip(rng.beta(1.4, 8.5) * 0.45, 0, 0.34)
        if sev < 0.10:
            level = "正常"
        elif sev < 0.35:
            level = "轻微退化"
        elif sev < 0.70:
            level = "中度故障"
        else:
            level = "重度故障"
        rows.append(
            {
                "case_id": case_id,
                "component_code": code,
                "component_name": name,
                "fault_type": DIAGNOSIS_FAULT_NAME[code],
                "severity": round(float(sev), 5),
                "judgement": "故障" if sev >= 0.35 else "正常",
                "fault_level": level,
                "rank_no": 0,
            }
        )
    rows = sorted(rows, key=lambda item: item["severity"], reverse=True)
    for rank, item in enumerate(rows, start=1):
        item["rank_no"] = rank
    return rows


def subtype_match_bonus(fault_type: str, subtype_name: str) -> float:
    if fault_type == "oil_leakage" and any(k in subtype_name for k in ["泄漏", "渗漏", "破裂", "老化"]):
        return 1.2
    if fault_type == "valve_stuck" and any(k in subtype_name for k in ["卡滞", "卡死", "堵塞"]):
        return 1.1
    if fault_type == "pressure_abnormal" and any(k in subtype_name for k in ["压力", "弹簧", "气囊", "卸荷"]):
        return 1.0
    if fault_type == "flow_abnormal" and any(k in subtype_name for k in ["堵塞", "节流", "油位", "泡沫"]):
        return 1.0
    if fault_type == "temperature_abnormal" and any(k in subtype_name for k in ["换热", "堵塞", "油位"]):
        return 0.9
    if fault_type == "vibration_abnormal" and any(k in subtype_name for k in ["轴承", "拉伤", "裂纹"]):
        return 0.9
    return 0.0


def generate_subtype_rows(rng: np.random.Generator, case_id: str, root_code: str, fault_type: str) -> list[dict]:
    rows = []
    for code, name in COMPONENTS.items():
        logits = []
        for _, subtype_name, _ in SUBTYPES[code]:
            base = 1.0 if code == root_code else 0.2
            logits.append(base + subtype_match_bonus(fault_type, subtype_name) + rng.normal(0, 0.08))
        exp_logits = np.exp(np.asarray(logits) - np.max(logits))
        probs = exp_logits / exp_logits.sum()
        for (subtype_id, subtype_name, fault_part), prob in zip(SUBTYPES[code], probs):
            rows.append(
                {
                    "case_id": case_id,
                    "component_code": code,
                    "component_name": name,
                    "subtype_id": subtype_id,
                    "subtype_name": subtype_name,
                    "fault_part": fault_part if code in {"C001", "C002", "C003"} else "-",
                    "probability": round(float(prob), 6),
                    "is_precise_part": bool(code in {"C001", "C002", "C003"}),
                }
            )
    return rows


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
    pipe = generate_pipe_design(rng, components)
    inspection = generate_inspection(rng, components)
    components = update_component_risks(components, inspection, pipe)
    feedback, rca, sensor, diagnosis, subtype = generate_feedback_bundle(rng, components)

    save_csv(components, DATA_DIR / "hydraulic_component.csv")
    save_csv(design, DATA_DIR / "hydraulic_design.csv")
    save_csv(pipe, DATA_DIR / "pipe_design_param.csv")
    save_csv(material, DATA_DIR / "hydraulic_material.csv")
    save_csv(manufacturing, DATA_DIR / "hydraulic_manufacturing.csv")
    save_csv(assembly, DATA_DIR / "hydraulic_assembly.csv")
    save_csv(inspection, DATA_DIR / "hydraulic_inspection.csv")
    save_csv(feedback, DATA_DIR / "hydraulic_feedback.csv")
    save_csv(rca, DATA_DIR / "hydraulic_rca_case.csv")
    save_csv(sensor, DATA_DIR / "hydraulic_sensor_energy.csv")
    save_csv(diagnosis, DATA_DIR / "hydraulic_component_diagnosis.csv")
    save_csv(subtype, DATA_DIR / "hydraulic_subtype_probability.csv")


if __name__ == "__main__":
    main()
