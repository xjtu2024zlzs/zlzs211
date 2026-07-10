from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from typing import Iterable

import numpy as np
import pandas as pd


ROOT = Path(__file__).resolve().parents[1]

TRIPLE_COLUMNS = ["head", "relation", "tail", "head_type", "tail_type", "domain", "source_table", "weight"]

FORBIDDEN_NAMES = ["调速阀/节流阀", "密封件/管路总成"]

BEARING_REQUIRED_FILES = [
    "bearing_component.csv",
    "bearing_design.csv",
    "bearing_material.csv",
    "bearing_manufacturing.csv",
    "bearing_assembly.csv",
    "bearing_inspection.csv",
    "bearing_feedback.csv",
]

HYDRAULIC_REQUIRED_FILES = [
    "hydraulic_component.csv",
    "hydraulic_design.csv",
    "pipe_design_param.csv",
    "hydraulic_material.csv",
    "hydraulic_manufacturing.csv",
    "hydraulic_assembly.csv",
    "hydraulic_inspection.csv",
    "hydraulic_feedback.csv",
    "hydraulic_rca_case.csv",
    "hydraulic_sensor_energy.csv",
    "hydraulic_component_diagnosis.csv",
    "hydraulic_subtype_probability.csv",
]

HYDRAULIC_COMPONENTS = {
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

HYDRAULIC_SENSOR_MAP = {
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

HYDRAULIC_COMPONENT_CATEGORY = {
    "C001": "power_unit",
    "C002": "control_valve",
    "C003": "actuator",
    "C004": "pressure_control_valve",
    "C005": "energy_storage",
    "C006": "thermal_management",
    "C007": "filtration",
    "C008": "reservoir",
    "C009": "check_valve",
    "C010": "flow_control_valve",
    "C011": "pipeline",
    "C012": "unloading_valve",
}

SENSOR_COLUMNS = ["PS1", "PS2", "PS3", "PS4", "PS5", "PS6", "EPS1", "FS1", "FS2", "TS1", "TS2", "TS3", "TS4", "VS1"]


def normalize_id(value: object, prefix: str = "NODE") -> str:
    text = str(value).strip()
    text = re.sub(r"\s+", "_", text)
    text = re.sub(r"[^0-9A-Za-z_\-\u4e00-\u9fff]", "_", text)
    return f"{prefix}_{text}" if prefix and not text.startswith(prefix) else text


def risk_bin(value: object) -> str:
    score = safe_float(value)
    if score < 0.25:
        return "RISK_0_25"
    if score < 0.50:
        return "RISK_25_50"
    if score < 0.75:
        return "RISK_50_75"
    return "RISK_75_100"


def confidence_bin(value: object) -> str:
    score = safe_float(value)
    if score < 0.25:
        return "CONF_0_25"
    if score < 0.50:
        return "CONF_25_50"
    if score < 0.75:
        return "CONF_50_75"
    return "CONF_75_100"


def energy_bin(value: object) -> str:
    score = safe_float(value)
    if score < 0.25:
        return "ENERGY_0_25"
    if score < 0.50:
        return "ENERGY_25_50"
    if score < 0.75:
        return "ENERGY_50_75"
    return "ENERGY_75_100"


def severity_bin(value: object) -> str:
    score = safe_float(value)
    if score < 0.10:
        return "SEV_0_10"
    if score < 0.35:
        return "SEV_10_35"
    if score < 0.70:
        return "SEV_35_70"
    return "SEV_70_100"


def length_bin(value: object) -> str:
    v = safe_float(value)
    if v < 120:
        return "LENGTH_080_120"
    if v < 200:
        return "LENGTH_120_200"
    if v < 300:
        return "LENGTH_200_300"
    return "LENGTH_300_450"


def angle_bin(value: object) -> str:
    v = safe_float(value)
    if v < 80:
        return "ANGLE_060_080"
    if v < 110:
        return "ANGLE_080_110"
    if v < 140:
        return "ANGLE_110_140"
    return "ANGLE_140_160"


def radius_bin(value: object) -> str:
    v = safe_float(value)
    if v < 10:
        return "RADIUS_005_010"
    if v < 20:
        return "RADIUS_010_020"
    if v < 30:
        return "RADIUS_020_030"
    return "RADIUS_030_040"


def magnitude_bin(prefix: str, value: object) -> str:
    score = safe_float(value)
    if score < 0.25:
        return f"{prefix}_0_25"
    if score < 0.50:
        return f"{prefix}_25_50"
    if score < 0.75:
        return f"{prefix}_50_75"
    return f"{prefix}_75_100"


def safe_float(value: object, default: float = 0.0) -> float:
    try:
        if pd.isna(value):
            return default
        return float(value)
    except (TypeError, ValueError):
        return default


def is_nonempty(value: object) -> bool:
    return not pd.isna(value) and str(value).strip() != ""


class TripleBuilder:
    def __init__(self, domain: str) -> None:
        self.domain = domain
        self.rows: list[dict[str, object]] = []

    def add(self, head: object, relation: str, tail: object, head_type: str, tail_type: str, source_table: str, weight: object = 1.0) -> None:
        if not is_nonempty(head) or not is_nonempty(relation) or not is_nonempty(tail):
            return
        self.rows.append(
            {
                "head": str(head).strip(),
                "relation": relation,
                "tail": str(tail).strip(),
                "head_type": head_type,
                "tail_type": tail_type,
                "domain": self.domain,
                "source_table": source_table,
                "weight": round(float(safe_float(weight, 1.0)), 6),
            }
        )

    def to_frame(self) -> pd.DataFrame:
        df = pd.DataFrame(self.rows, columns=TRIPLE_COLUMNS)
        if df.empty:
            return df
        df = df[(df["head"].astype(str).str.len() > 0) & (df["relation"].astype(str).str.len() > 0) & (df["tail"].astype(str).str.len() > 0)]
        return df.drop_duplicates(ignore_index=True)


def require_files(data_dir: Path, filenames: Iterable[str]) -> dict[str, pd.DataFrame]:
    missing = [name for name in filenames if not (data_dir / name).exists()]
    if missing:
        missing_text = "\n".join(f"- {data_dir / name}" for name in missing)
        raise FileNotFoundError(f"缺少构建三元组所需 CSV 文件：\n{missing_text}")
    return {name: pd.read_csv(data_dir / name, encoding="utf-8-sig") for name in filenames}


def warn_forbidden_names(dataframes: dict[str, pd.DataFrame]) -> None:
    for filename, df in dataframes.items():
        object_cols = [col for col in df.columns if df[col].dtype == object]
        for col in object_cols:
            values = df[col].dropna().astype(str)
            for forbidden in FORBIDDEN_NAMES:
                count = int(values.str.contains(forbidden, regex=False).sum())
                if count:
                    print(f"WARNING: {filename}.{col} 出现禁用名称 {forbidden}，count={count}")


def save_evidence(rows: list[dict[str, object]], path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    pd.DataFrame(rows, columns=["evidence_id", "feedback_id", "evidence_type", "evidence_text", "confidence", "domain"]).drop_duplicates().to_csv(
        path, index=False, encoding="utf-8-sig"
    )


def build_bearing() -> None:
    domain = "bearing"
    data_dir = ROOT / "data" / domain
    output_path = data_dir / "bearing_triples.csv"
    evidence_path = data_dir / "bearing_evidence.csv"
    tables = require_files(data_dir, BEARING_REQUIRED_FILES)
    warn_forbidden_names(tables)

    tb = TripleBuilder(domain)
    evidence_rows: list[dict[str, object]] = []

    component = tables["bearing_component.csv"]
    design = tables["bearing_design.csv"]
    material = tables["bearing_material.csv"]
    manufacturing = tables["bearing_manufacturing.csv"]
    assembly = tables["bearing_assembly.csv"]
    inspection = tables["bearing_inspection.csv"]
    feedback = tables["bearing_feedback.csv"]

    build_bearing_lifecycle(tb, component)
    build_bearing_design(tb, design)
    build_bearing_material(tb, material)
    build_bearing_manufacturing(tb, manufacturing)
    build_bearing_assembly(tb, assembly)
    build_bearing_inspection(tb, inspection)
    build_bearing_feedback(tb, feedback, evidence_rows)

    triples = tb.to_frame()
    output_path.parent.mkdir(parents=True, exist_ok=True)
    triples.to_csv(output_path, index=False, encoding="utf-8-sig")
    save_evidence(evidence_rows, evidence_path)
    write_statistics(domain, triples, ROOT / "outputs" / domain / "reports" / "kg_statistics.json")
    print_summary(domain, triples, output_path)


def build_bearing_lifecycle(tb: TripleBuilder, component: pd.DataFrame) -> None:
    for row in component.itertuples(index=False):
        bearing_id = row.bearing_id
        tb.add(bearing_id, "designed_by", row.design_id, "Bearing", "BearingDesignSpec", "bearing_component.csv")
        tb.add(bearing_id, "uses_material_batch", row.material_batch_id, "Bearing", "MaterialBatch", "bearing_component.csv")
        tb.add(bearing_id, "manufactured_in", row.manufacturing_batch_id, "Bearing", "ManufacturingBatch", "bearing_component.csv")
        tb.add(bearing_id, "assembled_in", row.assembly_id, "Bearing", "AssemblyRecord", "bearing_component.csv")
        tb.add(bearing_id, "inspected_by", row.inspection_id, "Bearing", "InspectionRecord", "bearing_component.csv")
        if hasattr(row, "maintenance_record_id"):
            tb.add(bearing_id, "maintained_by", row.maintenance_record_id, "Bearing", "MaintenanceRecord", "bearing_component.csv")
        if hasattr(row, "overall_lifecycle_risk_level"):
            tb.add(bearing_id, "has_overall_lifecycle_risk_level", normalize_id(row.overall_lifecycle_risk_level, "RISK_LEVEL"), "Bearing", "RiskLevel", "bearing_component.csv")
        if hasattr(row, "overall_lifecycle_risk"):
            tb.add(bearing_id, "has_overall_lifecycle_risk_score", risk_bin(row.overall_lifecycle_risk), "Bearing", "RiskScore", "bearing_component.csv", row.overall_lifecycle_risk)


def build_bearing_design(tb: TripleBuilder, design: pd.DataFrame) -> None:
    for row in design.itertuples(index=False):
        tb.add(row.design_id, "has_bearing_model", normalize_id(row.bearing_model, "BEARING_MODEL"), "BearingDesignSpec", "ComponentName", "bearing_design.csv")
        tb.add(row.design_id, "has_lubrication_method", normalize_id(row.lubrication_method, "LUBRICATION"), "BearingDesignSpec", "LubricationRecord", "bearing_design.csv")
        tb.add(row.design_id, "has_design_risk_level", normalize_id(row.design_risk_level, "RISK_LEVEL"), "BearingDesignSpec", "RiskLevel", "bearing_design.csv")
        tb.add(row.design_id, "has_design_risk_score", risk_bin(row.design_risk_score), "BearingDesignSpec", "RiskScore", "bearing_design.csv", row.design_risk_score)


def build_bearing_material(tb: TripleBuilder, material: pd.DataFrame) -> None:
    for row in material.itertuples(index=False):
        tb.add(row.material_batch_id, "supplied_by", row.supplier_id, "MaterialBatch", "Supplier", "bearing_material.csv")
        tb.add(row.material_batch_id, "has_material_grade", normalize_id(row.material_grade, "MATERIAL_GRADE"), "MaterialBatch", "MaterialGrade", "bearing_material.csv")
        tb.add(row.material_batch_id, "has_material_risk_level", normalize_id(row.material_risk_level, "RISK_LEVEL"), "MaterialBatch", "RiskLevel", "bearing_material.csv")


def build_bearing_manufacturing(tb: TripleBuilder, manufacturing: pd.DataFrame) -> None:
    for row in manufacturing.itertuples(index=False):
        tb.add(row.manufacturing_batch_id, "processed_by", row.equipment_id, "ManufacturingBatch", "Equipment", "bearing_manufacturing.csv")
        tb.add(row.manufacturing_batch_id, "has_manufacturing_risk_level", normalize_id(row.manufacturing_risk_level, "RISK_LEVEL"), "ManufacturingBatch", "RiskLevel", "bearing_manufacturing.csv")
        tb.add(row.manufacturing_batch_id, "uses_process", "PROCESS_BEARING_MANUFACTURING", "ManufacturingBatch", "ManufacturingProcess", "bearing_manufacturing.csv")
        tb.add(row.manufacturing_batch_id, "uses_process", "PROCESS_HEAT_TREATMENT", "ManufacturingBatch", "HeatTreatmentProcess", "bearing_manufacturing.csv")
        tb.add(row.manufacturing_batch_id, "uses_process", "PROCESS_GRINDING", "ManufacturingBatch", "GrindingProcess", "bearing_manufacturing.csv")


def build_bearing_assembly(tb: TripleBuilder, assembly: pd.DataFrame) -> None:
    for row in assembly.itertuples(index=False):
        tb.add(row.assembly_id, "operated_by", row.operator_id, "AssemblyRecord", "Operator", "bearing_assembly.csv")
        tb.add(row.assembly_id, "located_at", row.station_id, "AssemblyRecord", "Station", "bearing_assembly.csv")
        tb.add(row.assembly_id, "has_assembly_risk_level", normalize_id(row.assembly_risk_level, "RISK_LEVEL"), "AssemblyRecord", "RiskLevel", "bearing_assembly.csv")


def build_bearing_inspection(tb: TripleBuilder, inspection: pd.DataFrame) -> None:
    for row in inspection.itertuples(index=False):
        tb.add(row.inspection_id, "has_inspection_result", normalize_id(row.inspection_result, "INSPECTION_RESULT"), "InspectionRecord", "InspectionResult", "bearing_inspection.csv")
        tb.add(row.inspection_id, "has_inspection_risk_level", normalize_id(row.inspection_risk_level, "RISK_LEVEL"), "InspectionRecord", "RiskLevel", "bearing_inspection.csv")
        tb.add(row.inspection_id, "uses_inspection_type", "VIBRATION_INSPECTION", "InspectionRecord", "VibrationInspectionRecord", "bearing_inspection.csv")


def build_bearing_feedback(tb: TripleBuilder, feedback: pd.DataFrame, evidence_rows: list[dict[str, object]]) -> None:
    for row in feedback.itertuples(index=False):
        feedback_id = row.feedback_id
        tb.add(feedback_id, "occurs_on", row.bearing_id, "QualityFeedback", "Bearing", "bearing_feedback.csv")
        tb.add(feedback_id, "has_fault_type", normalize_id(row.fault_type, "FAULT_TYPE"), "QualityFeedback", "FaultType", "bearing_feedback.csv")
        tb.add(feedback_id, "located_at", normalize_id(row.fault_position, "FAULT_POSITION"), "QualityFeedback", "FaultPosition", "bearing_feedback.csv")
        tb.add(feedback_id, "has_fault_severity", normalize_id(row.fault_severity, "FAULT_SEVERITY"), "QualityFeedback", "FaultSeverity", "bearing_feedback.csv")
        tb.add(feedback_id, "has_diagnosis_confidence", confidence_bin(row.diagnosis_confidence), "QualityFeedback", "ConfidenceLevel", "bearing_feedback.csv", row.diagnosis_confidence)
        tb.add(feedback_id, "has_rca_confidence", confidence_bin(row.rca_confidence), "QualityFeedback", "ConfidenceLevel", "bearing_feedback.csv", row.rca_confidence)
        if str(row.fault_type) != "normal" and is_nonempty(row.true_reason_id):
            tb.add(feedback_id, "may_caused_by", row.true_reason_id, "QualityFeedback", row.true_reason_type, "bearing_feedback.csv", row.rca_confidence)
        tb.add(feedback_id, "attributed_to_stage", normalize_id(row.true_stage, "STAGE"), "QualityFeedback", "LifecycleStage", "bearing_feedback.csv")
        rca_id = f"RCA_TEXT_{feedback_id}"
        evo_id = f"EVO_TEXT_{feedback_id}"
        tb.add(feedback_id, "has_rca_text", rca_id, "QualityFeedback", "RcaText", "bearing_feedback.csv", row.rca_confidence)
        tb.add(feedback_id, "has_fault_evolution_text", evo_id, "QualityFeedback", "FaultEvolutionText", "bearing_feedback.csv", row.rca_confidence)
        evidence_rows.append({"evidence_id": rca_id, "feedback_id": feedback_id, "evidence_type": "rca_text", "evidence_text": row.rca_text, "confidence": row.rca_confidence, "domain": "bearing"})
        evidence_rows.append({"evidence_id": evo_id, "feedback_id": feedback_id, "evidence_type": "fault_evolution_text", "evidence_text": row.fault_evolution_text, "confidence": row.rca_confidence, "domain": "bearing"})


def build_hydraulic() -> None:
    domain = "hydraulic"
    data_dir = ROOT / "data" / domain
    output_path = data_dir / "hydraulic_triples.csv"
    evidence_path = data_dir / "hydraulic_evidence.csv"
    tables = require_files(data_dir, HYDRAULIC_REQUIRED_FILES)
    warn_forbidden_names(tables)
    validate_hydraulic_names(tables)

    tb = TripleBuilder(domain)
    evidence_rows: list[dict[str, object]] = []

    component = tables["hydraulic_component.csv"]
    design = tables["hydraulic_design.csv"]
    pipe = tables["pipe_design_param.csv"]
    material = tables["hydraulic_material.csv"]
    manufacturing = tables["hydraulic_manufacturing.csv"]
    assembly = tables["hydraulic_assembly.csv"]
    inspection = tables["hydraulic_inspection.csv"]
    feedback = tables["hydraulic_feedback.csv"]
    rca = tables["hydraulic_rca_case.csv"]
    sensor_energy = tables["hydraulic_sensor_energy.csv"]
    diagnosis = tables["hydraulic_component_diagnosis.csv"]
    subtype = tables["hydraulic_subtype_probability.csv"]

    build_hydraulic_lifecycle(tb, component)
    build_hydraulic_design(tb, design)
    build_hydraulic_pipe(tb, component, pipe)
    build_hydraulic_material(tb, material)
    build_hydraulic_manufacturing(tb, manufacturing)
    build_hydraulic_assembly(tb, assembly)
    build_hydraulic_inspection(tb, inspection)
    build_hydraulic_feedback(tb, feedback, evidence_rows)
    build_hydraulic_rca(tb, feedback, rca)
    build_hydraulic_sensor_energy(tb, feedback, sensor_energy)
    build_hydraulic_component_diagnosis(tb, feedback, diagnosis)
    build_hydraulic_subtypes(tb, feedback, rca, subtype)

    triples = tb.to_frame()
    output_path.parent.mkdir(parents=True, exist_ok=True)
    triples.to_csv(output_path, index=False, encoding="utf-8-sig")
    save_evidence(evidence_rows, evidence_path)
    write_statistics(domain, triples, ROOT / "outputs" / domain / "reports" / "kg_statistics.json", extra={"component_diagnosis_raw_rows": int(len(diagnosis))})
    print_summary(domain, triples, output_path)


def validate_hydraulic_names(tables: dict[str, pd.DataFrame]) -> None:
    for filename, df in tables.items():
        if "component_code" in df.columns and "component_name" in df.columns:
            bad = df[df["component_code"].astype(str).map(HYDRAULIC_COMPONENTS) != df["component_name"].astype(str)]
            if not bad.empty:
                examples = bad[["component_code", "component_name"]].head(5).to_dict(orient="records")
                print(f"WARNING: {filename} 存在液压部件命名不统一记录，examples={examples}")
        if "root_component_code" in df.columns and "root_component_name" in df.columns:
            bad = df[df["root_component_code"].astype(str).map(HYDRAULIC_COMPONENTS) != df["root_component_name"].astype(str)]
            if not bad.empty:
                examples = bad[["root_component_code", "root_component_name"]].head(5).to_dict(orient="records")
                print(f"WARNING: {filename} 存在 RCA 根因部件命名不统一记录，examples={examples}")


def build_hydraulic_lifecycle(tb: TripleBuilder, component: pd.DataFrame) -> None:
    for row in component.itertuples(index=False):
        uid = row.component_uid
        code = row.component_code
        tb.add(uid, "designed_by", row.design_id, "HydraulicComponent", "DesignSpec", "hydraulic_component.csv")
        tb.add(uid, "uses_material_batch", row.material_batch_id, "HydraulicComponent", "MaterialBatch", "hydraulic_component.csv")
        tb.add(uid, "manufactured_in", row.manufacturing_batch_id, "HydraulicComponent", "ManufacturingBatch", "hydraulic_component.csv")
        tb.add(uid, "assembled_in", row.assembly_id, "HydraulicComponent", "AssemblyRecord", "hydraulic_component.csv")
        tb.add(uid, "inspected_by", row.inspection_id, "HydraulicComponent", "InspectionRecord", "hydraulic_component.csv")
        tb.add(uid, "maintained_by", row.maintenance_record_id, "HydraulicComponent", "MaintenanceRecord", "hydraulic_component.csv")
        tb.add(uid, "has_component_code", code, "HydraulicComponent", "ComponentCode", "hydraulic_component.csv")
        tb.add(uid, "has_component_name", normalize_id(row.component_name, "COMPONENT_NAME"), "HydraulicComponent", "ComponentName", "hydraulic_component.csv")
        tb.add(uid, "belongs_to_category", normalize_id(HYDRAULIC_COMPONENT_CATEGORY.get(code, "unknown"), "CATEGORY"), "HydraulicComponent", "ComponentCategory", "hydraulic_component.csv")
        tb.add(uid, "has_overall_lifecycle_risk_score", risk_bin(row.overall_lifecycle_risk), "HydraulicComponent", "RiskScore", "hydraulic_component.csv", row.overall_lifecycle_risk)
        tb.add(uid, "has_overall_lifecycle_risk_level", normalize_id(row.overall_lifecycle_risk_level, "RISK_LEVEL"), "HydraulicComponent", "RiskLevel", "hydraulic_component.csv")
        for sensor, weight in get_component_sensors(row).items():
            tb.add(uid, "monitored_by", sensor, "HydraulicComponent", "Sensor", "hydraulic_component.csv", weight)


def get_component_sensors(row: object) -> dict[str, float]:
    if hasattr(row, "sensor_list") and is_nonempty(row.sensor_list):
        parsed = parse_sensor_list(row.sensor_list)
        if parsed:
            return parsed
    return HYDRAULIC_SENSOR_MAP.get(str(row.component_code), {})


def parse_sensor_list(value: object) -> dict[str, float]:
    text = str(value).strip().strip("[]")
    if not text:
        return {}
    sensors: dict[str, float] = {}
    for item in re.split(r"[,;|]", text):
        item = item.strip().strip("'\"")
        if not item:
            continue
        if ":" in item:
            sensor, weight = item.split(":", 1)
            sensors[sensor.strip()] = safe_float(weight, 1.0)
        else:
            sensors[item] = 1.0
    return sensors


def build_hydraulic_design(tb: TripleBuilder, design: pd.DataFrame) -> None:
    for row in design.itertuples(index=False):
        tb.add(row.design_id, "has_design_risk_level", normalize_id(row.design_risk_level, "RISK_LEVEL"), "DesignSpec", "RiskLevel", "hydraulic_design.csv")
        tb.add(row.design_id, "has_design_risk_score", risk_bin(row.design_risk_score), "DesignSpec", "RiskScore", "hydraulic_design.csv", row.design_risk_score)
        tb.add(row.design_id, "designed_for_component_code", row.component_code, "DesignSpec", "ComponentCode", "hydraulic_design.csv")


def build_hydraulic_pipe(tb: TripleBuilder, component: pd.DataFrame, pipe: pd.DataFrame) -> None:
    c011_uids = set(component.loc[component["component_code"] == "C011", "component_uid"].astype(str))
    for row in pipe.itertuples(index=False):
        if str(row.component_uid) not in c011_uids:
            print(f"WARNING: pipe_design_param.csv 中 {row.pipe_design_id} 未关联 C011 管路总成")
            continue
        tb.add(row.component_uid, "has_pipe_design_param", row.pipe_design_id, "HydraulicComponent", "PipeDesignParam", "pipe_design_param.csv")
        tb.add(row.pipe_design_id, "has_pipe_length_L1", length_bin(row.L1), "PipeDesignParam", "PipeParameter", "pipe_design_param.csv")
        tb.add(row.pipe_design_id, "has_pipe_length_L2", length_bin(row.L2), "PipeDesignParam", "PipeParameter", "pipe_design_param.csv")
        tb.add(row.pipe_design_id, "has_pipe_length_L3", length_bin(row.L3), "PipeDesignParam", "PipeParameter", "pipe_design_param.csv")
        tb.add(row.pipe_design_id, "has_bend_angle_theta1", angle_bin(row.theta1), "PipeDesignParam", "PipeParameter", "pipe_design_param.csv")
        tb.add(row.pipe_design_id, "has_bend_angle_theta2", angle_bin(row.theta2), "PipeDesignParam", "PipeParameter", "pipe_design_param.csv")
        tb.add(row.pipe_design_id, "has_bend_radius_R", radius_bin(row.R), "PipeDesignParam", "PipeParameter", "pipe_design_param.csv")
        tb.add(row.pipe_design_id, "has_max_deformation", magnitude_bin("DEFORMATION", row.max_deformation), "PipeDesignParam", "PipeParameter", "pipe_design_param.csv")
        stress_ratio = safe_float(row.max_equivalent_stress) / max(safe_float(row.allowable_stress, 1.0), 1e-6)
        tb.add(row.pipe_design_id, "has_max_equivalent_stress", magnitude_bin("STRESS_RATIO", stress_ratio), "PipeDesignParam", "PipeParameter", "pipe_design_param.csv", stress_ratio)
        tb.add(row.pipe_design_id, "has_design_risk_level", normalize_id(row.design_risk_level, "RISK_LEVEL"), "PipeDesignParam", "RiskLevel", "pipe_design_param.csv")
        tb.add(row.pipe_design_id, "has_design_risk_score", risk_bin(row.design_risk_score), "PipeDesignParam", "RiskScore", "pipe_design_param.csv", row.design_risk_score)


def build_hydraulic_material(tb: TripleBuilder, material: pd.DataFrame) -> None:
    for row in material.itertuples(index=False):
        tb.add(row.material_batch_id, "supplied_by", row.supplier_id, "MaterialBatch", "Supplier", "hydraulic_material.csv")
        tb.add(row.material_batch_id, "has_material_grade", normalize_id(row.material_grade, "MATERIAL_GRADE"), "MaterialBatch", "MaterialGrade", "hydraulic_material.csv")
        tb.add(row.material_batch_id, "has_material_risk_level", normalize_id(row.material_risk_level, "RISK_LEVEL"), "MaterialBatch", "RiskLevel", "hydraulic_material.csv")


def build_hydraulic_manufacturing(tb: TripleBuilder, manufacturing: pd.DataFrame) -> None:
    for row in manufacturing.itertuples(index=False):
        tb.add(row.manufacturing_batch_id, "uses_process", normalize_id(row.process_type, "PROCESS"), "ManufacturingBatch", "ManufacturingProcess", "hydraulic_manufacturing.csv")
        tb.add(row.manufacturing_batch_id, "processed_by", row.equipment_id, "ManufacturingBatch", "Equipment", "hydraulic_manufacturing.csv")
        tb.add(row.manufacturing_batch_id, "has_process_risk_level", normalize_id(row.manufacturing_risk_level, "RISK_LEVEL"), "ManufacturingBatch", "RiskLevel", "hydraulic_manufacturing.csv")


def build_hydraulic_assembly(tb: TripleBuilder, assembly: pd.DataFrame) -> None:
    for row in assembly.itertuples(index=False):
        tb.add(row.assembly_id, "operated_by", row.operator_id, "AssemblyRecord", "Operator", "hydraulic_assembly.csv")
        tb.add(row.assembly_id, "located_at", row.station_id, "AssemblyRecord", "Station", "hydraulic_assembly.csv")
        tb.add(row.assembly_id, "has_assembly_risk_level", normalize_id(row.assembly_risk_level, "RISK_LEVEL"), "AssemblyRecord", "RiskLevel", "hydraulic_assembly.csv")


def build_hydraulic_inspection(tb: TripleBuilder, inspection: pd.DataFrame) -> None:
    for row in inspection.itertuples(index=False):
        tb.add(row.inspection_id, "has_inspection_result", normalize_id(row.inspection_result, "INSPECTION_RESULT"), "InspectionRecord", "InspectionResult", "hydraulic_inspection.csv")
        tb.add(row.inspection_id, "has_inspection_risk_level", normalize_id(row.inspection_risk_level, "RISK_LEVEL"), "InspectionRecord", "RiskLevel", "hydraulic_inspection.csv")


def build_hydraulic_feedback(tb: TripleBuilder, feedback: pd.DataFrame, evidence_rows: list[dict[str, object]]) -> None:
    for row in feedback.itertuples(index=False):
        feedback_id = row.feedback_id
        tb.add(feedback_id, "occurs_on", row.component_uid, "QualityFeedback", "HydraulicComponent", "hydraulic_feedback.csv")
        tb.add(feedback_id, "has_fault_type", normalize_id(row.fault_type, "FAULT_TYPE"), "QualityFeedback", "FaultType", "hydraulic_feedback.csv")
        tb.add(feedback_id, "located_at", normalize_id(row.fault_position, "FAULT_POSITION"), "QualityFeedback", "FaultPosition", "hydraulic_feedback.csv")
        tb.add(feedback_id, "has_fault_severity", normalize_id(row.fault_severity, "FAULT_SEVERITY"), "QualityFeedback", "FaultSeverity", "hydraulic_feedback.csv")
        tb.add(feedback_id, "has_diagnosis_confidence", confidence_bin(row.diagnosis_confidence), "QualityFeedback", "ConfidenceLevel", "hydraulic_feedback.csv", row.diagnosis_confidence)
        tb.add(feedback_id, "has_rca_confidence", confidence_bin(row.rca_confidence), "QualityFeedback", "ConfidenceLevel", "hydraulic_feedback.csv", row.rca_confidence)
        if is_nonempty(row.true_reason_id):
            tb.add(feedback_id, "may_caused_by", row.true_reason_id, "QualityFeedback", row.true_reason_type, "hydraulic_feedback.csv", row.rca_confidence)
        tb.add(feedback_id, "attributed_to_stage", normalize_id(row.true_stage, "STAGE"), "QualityFeedback", "LifecycleStage", "hydraulic_feedback.csv")
        rca_id = f"RCA_TEXT_{feedback_id}"
        evo_id = f"EVO_TEXT_{feedback_id}"
        tb.add(feedback_id, "has_rca_text", rca_id, "QualityFeedback", "RcaText", "hydraulic_feedback.csv", row.rca_confidence)
        tb.add(feedback_id, "has_fault_evolution_text", evo_id, "QualityFeedback", "FaultEvolutionText", "hydraulic_feedback.csv", row.rca_confidence)
        evidence_rows.append({"evidence_id": rca_id, "feedback_id": feedback_id, "evidence_type": "rca_text", "evidence_text": row.rca_text, "confidence": row.rca_confidence, "domain": "hydraulic"})
        evidence_rows.append({"evidence_id": evo_id, "feedback_id": feedback_id, "evidence_type": "fault_evolution_text", "evidence_text": row.fault_evolution_text, "confidence": row.rca_confidence, "domain": "hydraulic"})


def build_hydraulic_rca(tb: TripleBuilder, feedback: pd.DataFrame, rca: pd.DataFrame) -> None:
    feedback_map = feedback.set_index("feedback_id")[["component_uid", "component_code"]].to_dict(orient="index")
    code_to_uid = feedback.drop_duplicates("component_code").set_index("component_code")["component_uid"].to_dict()
    for row in rca.itertuples(index=False):
        if row.feedback_id not in feedback_map:
            continue
        feedback_info = feedback_map[row.feedback_id]
        root_uid = feedback_info["component_uid"] if row.root_component_code == feedback_info["component_code"] else code_to_uid.get(row.root_component_code, feedback_info["component_uid"])
        tb.add(row.feedback_id, "has_root_sensor", row.root_sensor, "QualityFeedback", "Sensor", "hydraulic_rca_case.csv", row.root_sensor_confidence)
        tb.add(row.feedback_id, "has_secondary_sensor", row.secondary_sensor, "QualityFeedback", "Sensor", "hydraulic_rca_case.csv", row.secondary_sensor_confidence)
        tb.add(row.feedback_id, "diagnosed_root_component", root_uid, "QualityFeedback", "HydraulicComponent", "hydraulic_rca_case.csv", row.component_confidence)
        tb.add(row.feedback_id, "has_component_confidence", confidence_bin(row.component_confidence), "QualityFeedback", "ConfidenceLevel", "hydraulic_rca_case.csv", row.component_confidence)
        tb.add(row.feedback_id, "has_rca_confidence", confidence_bin(row.rca_confidence), "QualityFeedback", "ConfidenceLevel", "hydraulic_rca_case.csv", row.rca_confidence)
        tb.add(row.root_sensor, "indicates_fault_of", root_uid, "Sensor", "HydraulicComponent", "hydraulic_rca_case.csv", row.root_sensor_confidence)
        tb.add(row.secondary_sensor, "indicates_fault_of", root_uid, "Sensor", "HydraulicComponent", "hydraulic_rca_case.csv", row.secondary_sensor_confidence)


def build_hydraulic_sensor_energy(tb: TripleBuilder, feedback: pd.DataFrame, sensor_energy: pd.DataFrame) -> None:
    case_to_feedback = feedback.set_index("case_id")["feedback_id"].to_dict()
    for row in sensor_energy.itertuples(index=False):
        feedback_id = case_to_feedback.get(row.case_id)
        if not feedback_id:
            continue
        energies = {sensor: safe_float(getattr(row, sensor)) for sensor in SENSOR_COLUMNS if hasattr(row, sensor)}
        if not energies:
            continue
        max_sensor = max(energies, key=energies.get)
        for sensor, energy in energies.items():
            if energy >= 0.05:
                tb.add(feedback_id, "activates_sensor", sensor, "QualityFeedback", "Sensor", "hydraulic_sensor_energy.csv", energy)
                tb.add(f"SENSOR_ENERGY_{row.case_id}_{sensor}", "energy_of_sensor", sensor, "SensorEnergy", "Sensor", "hydraulic_sensor_energy.csv", energy)
                tb.add(f"SENSOR_ENERGY_{row.case_id}_{sensor}", "has_energy_level", energy_bin(energy), "SensorEnergy", "EnergyLevel", "hydraulic_sensor_energy.csv", energy)
        tb.add(feedback_id, "most_activated_sensor", max_sensor, "QualityFeedback", "Sensor", "hydraulic_sensor_energy.csv", energies[max_sensor])


def build_hydraulic_component_diagnosis(tb: TripleBuilder, feedback: pd.DataFrame, diagnosis: pd.DataFrame) -> None:
    case_to_feedback = feedback.set_index("case_id")["feedback_id"].to_dict()
    case_component_uid = feedback.set_index(["case_id", "component_code"])["component_uid"].to_dict()
    fallback_uid = feedback.drop_duplicates("component_code").set_index("component_code")["component_uid"].to_dict()
    top5 = diagnosis[diagnosis["rank_no"].astype(float) <= 5]
    for row in top5.itertuples(index=False):
        feedback_id = case_to_feedback.get(row.case_id)
        if not feedback_id:
            continue
        diag_id = f"DIAG_{row.case_id}_{row.component_code}"
        component_uid = case_component_uid.get((row.case_id, row.component_code), fallback_uid.get(row.component_code, row.component_code))
        tb.add(feedback_id, "has_component_diagnosis", diag_id, "QualityFeedback", "ComponentDiagnosis", "hydraulic_component_diagnosis.csv", row.severity)
        tb.add(diag_id, "diagnoses_component", component_uid, "ComponentDiagnosis", "HydraulicComponent", "hydraulic_component_diagnosis.csv", row.severity)
        tb.add(diag_id, "diagnosed_as_fault_type", normalize_id(row.fault_type, "COMPONENT_FAULT_TYPE"), "ComponentDiagnosis", "ComponentFaultType", "hydraulic_component_diagnosis.csv")
        tb.add(diag_id, "has_fault_level", normalize_id(row.fault_level, "FAULT_LEVEL"), "ComponentDiagnosis", "FaultLevel", "hydraulic_component_diagnosis.csv")
        tb.add(diag_id, "has_judgement", normalize_id(row.judgement, "JUDGEMENT"), "ComponentDiagnosis", "Judgement", "hydraulic_component_diagnosis.csv")
        tb.add(diag_id, "has_severity_level", severity_bin(row.severity), "ComponentDiagnosis", "FaultSeverity", "hydraulic_component_diagnosis.csv", row.severity)


def build_hydraulic_subtypes(tb: TripleBuilder, feedback: pd.DataFrame, rca: pd.DataFrame, subtype: pd.DataFrame) -> None:
    case_to_feedback = feedback.set_index("case_id")["feedback_id"].to_dict()
    root_code_by_case = rca.set_index("case_id")["root_component_code"].to_dict()
    selected = subtype[subtype["probability"].astype(float) >= 0.30].copy()
    root_rows = []
    for case_id, group in subtype.groupby("case_id", sort=False):
        root_code = root_code_by_case.get(case_id)
        if not root_code:
            continue
        root_group = group[group["component_code"] == root_code].sort_values("probability", ascending=False).head(3)
        root_rows.append(root_group)
    if root_rows:
        selected = pd.concat([selected, *root_rows], ignore_index=True).drop_duplicates(["case_id", "subtype_id"])
    subtype_defs = subtype.drop_duplicates("subtype_id")
    for row in subtype_defs.itertuples(index=False):
        tb.add(row.component_code, "has_fault_subtype", row.subtype_id, "ComponentCode", "FaultSubtype", "hydraulic_subtype_probability.csv")
        tb.add(row.subtype_id, "has_subtype_name", normalize_id(row.subtype_name, "SUBTYPE_NAME"), "FaultSubtype", "ComponentFaultType", "hydraulic_subtype_probability.csv")
        tb.add(row.subtype_id, "belongs_to_component_code", row.component_code, "FaultSubtype", "ComponentCode", "hydraulic_subtype_probability.csv")
        if is_nonempty(row.fault_part) and str(row.fault_part) != "-":
            part_id = normalize_id(row.fault_part, "FAULT_PART")
            tb.add(row.subtype_id, "has_fault_part", part_id, "FaultSubtype", "FaultPart", "hydraulic_subtype_probability.csv")
            if str(row.is_precise_part).lower() == "true":
                tb.add(row.subtype_id, "located_at_precise_part", part_id, "FaultSubtype", "FaultPart", "hydraulic_subtype_probability.csv")
    for row in selected.itertuples(index=False):
        feedback_id = case_to_feedback.get(row.case_id)
        if feedback_id:
            tb.add(feedback_id, "has_subtype_candidate", row.subtype_id, "QualityFeedback", "FaultSubtype", "hydraulic_subtype_probability.csv", row.probability)


def write_statistics(domain: str, triples: pd.DataFrame, path: Path, extra: dict[str, object] | None = None) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    if triples.empty:
        stats = {
            "domain": domain,
            "triple_count": 0,
            "entity_count": 0,
            "relation_count": 0,
            "entity_type_count": {},
            "relation_count_detail": {},
            "source_table_count": {},
            "may_caused_by_count": 0,
        }
    else:
        head_entities = triples[["head", "head_type"]].rename(columns={"head": "entity", "head_type": "entity_type"})
        tail_entities = triples[["tail", "tail_type"]].rename(columns={"tail": "entity", "tail_type": "entity_type"})
        entities = pd.concat([head_entities, tail_entities], ignore_index=True).drop_duplicates(["entity", "entity_type"])
        stats = {
            "domain": domain,
            "triple_count": int(len(triples)),
            "entity_count": int(entities["entity"].nunique()),
            "relation_count": int(triples["relation"].nunique()),
            "entity_type_count": {str(k): int(v) for k, v in entities.groupby("entity_type")["entity"].nunique().sort_index().items()},
            "relation_count_detail": {str(k): int(v) for k, v in triples["relation"].value_counts().sort_index().items()},
            "source_table_count": {str(k): int(v) for k, v in triples["source_table"].value_counts().sort_index().items()},
            "may_caused_by_count": int((triples["relation"] == "may_caused_by").sum()),
        }
    if extra:
        stats.update(extra)
    path.write_text(json.dumps(stats, ensure_ascii=False, indent=2), encoding="utf-8")


def print_summary(domain: str, triples: pd.DataFrame, output_path: Path) -> None:
    entity_count = 0
    if not triples.empty:
        entity_count = int(pd.concat([triples["head"], triples["tail"]]).nunique())
    print(f"domain={domain}")
    print(f"triple_count={len(triples)}")
    print(f"entity_count={entity_count}")
    print(f"relation_count={triples['relation'].nunique() if not triples.empty else 0}")
    print(f"may_caused_by_count={int((triples['relation'] == 'may_caused_by').sum()) if not triples.empty else 0}")
    print(f"output_path={output_path}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Build knowledge graph triples from generated lifecycle CSV data.")
    parser.add_argument("--domain", choices=["bearing", "hydraulic", "all"], required=True)
    args = parser.parse_args()

    if args.domain in {"bearing", "all"}:
        build_bearing()
    if args.domain in {"hydraulic", "all"}:
        build_hydraulic()


if __name__ == "__main__":
    main()
