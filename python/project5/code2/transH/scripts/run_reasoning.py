from __future__ import annotations

import argparse
import json
import os
import random
import re
import sys
from pathlib import Path
from typing import Any

os.environ.setdefault("KMP_DUPLICATE_LIB_OK", "TRUE")

import numpy as np
import pandas as pd
import torch

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from src.models.transh import TransHModel


LIFECYCLE_RELATIONS = [
    "designed_by",
    "uses_material_batch",
    "manufactured_in",
    "assembled_in",
    "inspected_by",
    "maintained_by",
    "has_pipe_design_param",
]

EXPAND_RELATIONS = ["uses_process", "processed_by", "operated_by", "located_at", "supplied_by"]

SCOPE_PATH_SCORES = {
    "lifecycle_candidate": 1.0,
    "rca_root_component_candidate": 0.85,
    "expanded_candidate": 0.80,
    "rca_root_component_expanded_candidate": 0.70,
    "global_same_type_candidate": 0.35,
}

SCOPE_PRIORITY = {
    "lifecycle_candidate": 5,
    "rca_root_component_candidate": 4,
    "expanded_candidate": 3,
    "rca_root_component_expanded_candidate": 2,
    "global_same_type_candidate": 1,
}

CANDIDATE_TYPES = {
    "DesignSpec",
    "BearingDesignSpec",
    "PipeDesignParam",
    "MaterialBatch",
    "Supplier",
    "ManufacturingBatch",
    "ManufacturingProcess",
    "HeatTreatmentProcess",
    "GrindingProcess",
    "Equipment",
    "AssemblyRecord",
    "FitToleranceRecord",
    "Station",
    "InspectionRecord",
    "VibrationInspectionRecord",
    "InspectionEquipment",
    "LubricationRecord",
    "MaintenanceRecord",
    "Operator",
}

STAGE_NAMES = {
    "design": "设计阶段",
    "material": "材料阶段",
    "manufacturing": "制造阶段",
    "assembly": "装配阶段",
    "inspection": "检测阶段",
    "operation": "使用/运维阶段",
    "unknown": "未知阶段",
}

STAGE_SUBSYSTEMS = {
    "design": "设计子系统",
    "material": "材料/供应商管理子系统",
    "manufacturing": "制造工艺子系统",
    "assembly": "装配子系统",
    "inspection": "检测子系统",
    "operation": "运维保障子系统",
    "unknown": "待人工复核子系统",
}

SCOPE_ADJUSTMENT = {
    "lifecycle_candidate": 0.04,
    "rca_root_component_candidate": 0.035,
    "expanded_candidate": 0.015,
    "rca_root_component_expanded_candidate": 0.01,
    "global_same_type_candidate": -0.10,
}

SCOPE_STAGE_WEIGHT = {
    "lifecycle_candidate": 1.0,
    "rca_root_component_candidate": 0.90,
    "expanded_candidate": 0.65,
    "rca_root_component_expanded_candidate": 0.60,
    "global_same_type_candidate": 0.25,
}

STAGE_RANK_SCORES = {1: 1.00, 2: 0.85, 3: 0.72, 4: 0.60, 5: 0.50, 6: 0.42}

TYPE_STAGE_WEIGHT = {
    "DesignSpec": 1.10,
    "BearingDesignSpec": 1.10,
    "PipeDesignParam": 1.15,
    "MaterialBatch": 1.05,
    "ManufacturingBatch": 1.05,
    "AssemblyRecord": 1.15,
    "InspectionRecord": 1.15,
    "MaintenanceRecord": 1.10,
    "Supplier": 0.65,
    "Equipment": 0.65,
    "Operator": 0.65,
    "Station": 0.65,
    "ManufacturingProcess": 0.65,
    "HeatTreatmentProcess": 0.65,
    "GrindingProcess": 0.65,
}

DIRECT_LIFECYCLE_TYPES = {
    "DesignSpec",
    "BearingDesignSpec",
    "PipeDesignParam",
    "MaterialBatch",
    "ManufacturingBatch",
    "AssemblyRecord",
    "InspectionRecord",
    "MaintenanceRecord",
}

STAGE_COVERAGE_BONUS = {
    "design": 0.06,
    "material": 0.04,
    "manufacturing": 0.04,
    "assembly": 0.07,
    "inspection": 0.07,
    "operation": 0.05,
}

FAULT_STAGE_PRIOR = {
    "bearing": {
        "inner_race_fault": {"manufacturing": 0.06, "assembly": 0.05, "material": 0.04},
        "outer_race_fault": {"assembly": 0.06, "manufacturing": 0.05, "design": 0.03},
        "rolling_element_fault": {"material": 0.06, "manufacturing": 0.05, "operation": 0.04},
    },
    "hydraulic": {
        "oil_leakage": {"assembly": 0.06, "material": 0.05, "manufacturing": 0.04, "design": 0.03},
        "valve_stuck": {"manufacturing": 0.06, "assembly": 0.05, "inspection": 0.04},
        "pressure_abnormal": {"design": 0.05, "manufacturing": 0.05, "inspection": 0.04, "operation": 0.04},
        "flow_abnormal": {"design": 0.06, "assembly": 0.05, "operation": 0.04},
        "temperature_abnormal": {"operation": 0.06, "inspection": 0.05, "design": 0.04},
        "vibration_abnormal": {"operation": 0.06, "manufacturing": 0.05, "assembly": 0.04},
    },
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Run TransH Top6 reason tracing for a feedback case.")
    parser.add_argument("--domain", choices=["bearing", "hydraulic"], required=True)
    parser.add_argument("--case_id", required=True)
    parser.add_argument("--batch_size", type=int, default=4096)
    parser.add_argument("--seed", type=int, default=20260604)
    parser.add_argument("--model_variant", choices=["default", "eval"], default="default")
    parser.add_argument("--print_json", action="store_true", help="Print full reasoning JSON payload.")
    return parser.parse_args()


def require_file(path: Path, hint: str) -> Path:
    if not path.exists():
        raise FileNotFoundError(f"缺少文件: {path}\n请先完成: {hint}")
    return path


def load_json(path: Path) -> dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8"))


def model_dir_for_variant(domain: str, model_variant: str = "default") -> Path:
    if model_variant == "default":
        return ROOT / "outputs" / domain / "models"
    if model_variant == "eval":
        return ROOT / "outputs" / domain / "models_eval"
    raise ValueError(f"不支持的 model_variant={model_variant}，可选值为 default/eval。")


def load_inputs(domain: str, model_variant: str = "default") -> dict[str, Any]:
    model_dir = model_dir_for_variant(domain, model_variant)
    data_dir = ROOT / "data" / domain
    config_dir = ROOT / "config"
    train_hint = "python scripts/train_transh.py --domain " + domain
    if model_variant == "eval":
        train_hint = "python scripts/train_transh.py --domain " + domain + " --use_train_split"
    triples_name = f"{domain}_triples_train.csv" if model_variant == "eval" else f"{domain}_triples.csv"
    triples_hint = "python scripts/create_eval_split.py --domain " + domain if model_variant == "eval" else "python scripts/build_triples.py --domain " + domain
    paths = {
        "model_dir": model_dir,
        "model": require_file(model_dir / "transh_model.pt", train_hint),
        "entity2id": require_file(model_dir / "entity2id.json", train_hint),
        "relation2id": require_file(model_dir / "relation2id.json", train_hint),
        "triples": require_file(data_dir / triples_name, triples_hint),
        "stage_mapping": require_file(config_dir / "stage_mapping.json", "检查 config/stage_mapping.json"),
        "fault_prior": require_file(config_dir / "fault_prior.json", "检查 config/fault_prior.json"),
    }
    if domain == "bearing":
        paths["feedback"] = require_file(data_dir / "bearing_feedback.csv", "python scripts/generate_bearing_data.py")
    else:
        paths["feedback"] = require_file(data_dir / "hydraulic_feedback.csv", "python scripts/generate_hydraulic_data.py")
        paths["component"] = require_file(data_dir / "hydraulic_component.csv", "python scripts/generate_hydraulic_data.py")
        paths["rca"] = require_file(data_dir / "hydraulic_rca_case.csv", "python scripts/generate_hydraulic_data.py")
        paths["diagnosis"] = require_file(data_dir / "hydraulic_component_diagnosis.csv", "python scripts/generate_hydraulic_data.py")
        paths["subtype"] = require_file(data_dir / "hydraulic_subtype_probability.csv", "python scripts/generate_hydraulic_data.py")
        paths["sensor"] = require_file(data_dir / "hydraulic_sensor_energy.csv", "python scripts/generate_hydraulic_data.py")

    loaded = {
        "paths": paths,
        "entity2id": load_json(paths["entity2id"]),
        "relation2id": load_json(paths["relation2id"]),
        "triples": pd.read_csv(paths["triples"], encoding="utf-8-sig"),
        "feedback": pd.read_csv(paths["feedback"], encoding="utf-8-sig"),
        "stage_mapping": load_json(paths["stage_mapping"]),
        "fault_prior": load_json(paths["fault_prior"]),
    }
    if domain == "hydraulic":
        loaded["component"] = pd.read_csv(paths["component"], encoding="utf-8-sig")
        loaded["rca"] = pd.read_csv(paths["rca"], encoding="utf-8-sig")
        loaded["diagnosis"] = pd.read_csv(paths["diagnosis"], encoding="utf-8-sig")
        loaded["subtype"] = pd.read_csv(paths["subtype"], encoding="utf-8-sig")
        loaded["sensor"] = pd.read_csv(paths["sensor"], encoding="utf-8-sig")
    return loaded


def load_model(domain: str, model_path: Path, device: torch.device) -> TransHModel:
    checkpoint = torch.load(model_path, map_location=device)
    model = TransHModel(
        num_entities=int(checkpoint["num_entities"]),
        num_relations=int(checkpoint["num_relations"]),
        embedding_dim=int(checkpoint["embedding_dim"]),
        margin=float(checkpoint["margin"]),
    )
    model.load_state_dict(checkpoint["model_state_dict"])
    model.to(device)
    model.eval()
    return model


def resolve_feedback(feedback: pd.DataFrame, case_id: str, rng: random.Random) -> pd.Series:
    if case_id == "random":
        return feedback.sample(n=1, random_state=rng.randint(1, 2**31 - 1)).iloc[0]
    normalized_case_id = normalize_case_id_alias(case_id)
    matched = feedback[feedback["feedback_id"].astype(str).isin({case_id, normalized_case_id})]
    if matched.empty and "case_id" in feedback.columns:
        matched = feedback[feedback["case_id"].astype(str).isin({case_id, normalized_case_id})]
    if matched.empty:
        raise ValueError(f"未找到 case_id={case_id} 对应的 feedback_id 或 case_id。")
    return matched.iloc[0]


def normalize_case_id_alias(case_id: str) -> str:
    bearing_match = re.fullmatch(r"FB-BRG-(\d+)", case_id)
    if bearing_match:
        return f"BFB-{int(bearing_match.group(1)):05d}"
    hydraulic_match = re.fullmatch(r"FB-HYD-(\d+)", case_id)
    if hydraulic_match:
        return f"HFB-{int(hydraulic_match.group(1)):05d}"
    return case_id


def build_entity_type_map(triples: pd.DataFrame) -> dict[str, str]:
    entity_types: dict[str, str] = {}
    for entity, entity_type in triples[["head", "head_type"]].drop_duplicates().itertuples(index=False):
        entity_types.setdefault(str(entity), str(entity_type))
    for entity, entity_type in triples[["tail", "tail_type"]].drop_duplicates().itertuples(index=False):
        if str(entity_type) in CANDIDATE_TYPES or str(entity) not in entity_types:
            entity_types[str(entity)] = str(entity_type)
    return entity_types


def outgoing(triples: pd.DataFrame, head: str, relations: list[str]) -> pd.DataFrame:
    return triples[(triples["head"].astype(str) == head) & (triples["relation"].isin(relations))]


def add_candidate(candidates: dict[str, dict[str, Any]], entity: str, entity_type: str, scope: str, path_score: float | None = None) -> None:
    if entity_type not in CANDIDATE_TYPES:
        return
    score = SCOPE_PATH_SCORES.get(scope, 0.35) if path_score is None else path_score
    old = candidates.get(entity)
    if old is None or SCOPE_PRIORITY.get(scope, 0) > SCOPE_PRIORITY.get(str(old["candidate_scope"]), 0):
        candidates[entity] = {"candidate_id": entity, "candidate_type": entity_type, "candidate_scope": scope, "path_score": score}


def lifecycle_candidates_for_object(
    triples: pd.DataFrame,
    object_id: str,
    candidates: dict[str, dict[str, Any]],
    lifecycle_scope: str = "lifecycle_candidate",
    lifecycle_score: float = 1.0,
    expanded_scope: str = "expanded_candidate",
    expanded_score: float = 0.8,
    max_expanded: int | None = 80,
) -> set[str]:
    lifecycle_rows = outgoing(triples, object_id, LIFECYCLE_RELATIONS)
    lifecycle_ids: set[str] = set()
    for row in lifecycle_rows.itertuples(index=False):
        entity = str(row.tail)
        lifecycle_ids.add(entity)
        add_candidate(candidates, entity, str(row.tail_type), lifecycle_scope, lifecycle_score)
    expanded_added = 0
    for entity in lifecycle_ids:
        for row in outgoing(triples, entity, EXPAND_RELATIONS).itertuples(index=False):
            if max_expanded is not None and expanded_added >= max_expanded:
                break
            before = len(candidates)
            add_candidate(candidates, str(row.tail), str(row.tail_type), expanded_scope, expanded_score)
            if len(candidates) > before:
                expanded_added += 1
        if max_expanded is not None and expanded_added >= max_expanded:
            break
    return lifecycle_ids


def stage_for_type(entity_type: str, stage_mapping: dict[str, Any]) -> str:
    return stage_mapping.get("entity_type_stage_mapping", {}).get(entity_type, "unknown")


def build_global_candidates(
    triples: pd.DataFrame,
    candidates: dict[str, dict[str, Any]],
    fault_type: str,
    domain: str,
    stage_mapping: dict[str, Any],
    fault_prior: dict[str, Any],
    rng: np.random.Generator,
) -> None:
    prior = fault_prior.get("domains", {}).get(domain, {}).get(fault_type, {})
    ranked_stages = [stage for stage, _ in sorted(prior.items(), key=lambda item: float(item[1]), reverse=True) if stage != "feedback"]
    if not ranked_stages:
        ranked_stages = ["manufacturing", "assembly", "material", "design", "inspection", "operation"]

    tail_entities = triples[["tail", "tail_type"]].drop_duplicates().rename(columns={"tail": "entity", "tail_type": "entity_type"})
    head_entities = triples[["head", "head_type"]].drop_duplicates().rename(columns={"head": "entity", "head_type": "entity_type"})
    entities = pd.concat([tail_entities, head_entities], ignore_index=True).drop_duplicates(["entity", "entity_type"])
    entities = entities[entities["entity_type"].isin(CANDIDATE_TYPES)].copy()
    entities["stage"] = entities["entity_type"].map(lambda t: stage_for_type(str(t), stage_mapping))
    entities = entities[entities["stage"].isin(ranked_stages)]
    entities = entities[~entities["entity"].astype(str).isin(candidates.keys())]
    if entities.empty:
        return
    stage_weight = {stage: float(prior.get(stage, 0.5)) for stage in ranked_stages}
    weights = entities["stage"].map(stage_weight).fillna(0.5).astype(float).to_numpy()
    weights = weights / max(weights.sum(), 1e-8)
    take = min(200, len(entities))
    selected_idx = rng.choice(entities.index.to_numpy(), size=take, replace=False, p=weights)
    for row in entities.loc[selected_idx].itertuples(index=False):
        add_candidate(candidates, str(row.entity), str(row.entity_type), "global_same_type_candidate", 0.35)


def target_object_from_feedback(domain: str, feedback_row: pd.Series, triples: pd.DataFrame) -> dict[str, str]:
    feedback_id = str(feedback_row.feedback_id)
    occurs = outgoing(triples, feedback_id, ["occurs_on"])
    if not occurs.empty:
        target_id = str(occurs.iloc[0]["tail"])
        target_type = str(occurs.iloc[0]["tail_type"])
    elif domain == "bearing":
        target_id = str(feedback_row.bearing_id)
        target_type = "Bearing"
    else:
        target_id = str(feedback_row.component_uid)
        target_type = "HydraulicComponent"
    if domain == "bearing":
        name = target_id
    else:
        name = str(getattr(feedback_row, "component_name", target_id))
    return {"object_id": target_id, "object_type": target_type, "object_name": name}


def numeric_suffix(value: object) -> int | None:
    match = re.search(r"(\d+)$", str(value))
    return int(match.group(1)) if match else None


def get_rca_root_component_uids(feedback_row: pd.Series, rca_row: pd.Series | None, component_df: pd.DataFrame, triples_df: pd.DataFrame) -> list[str]:
    if rca_row is None:
        return []
    root_component_code = str(rca_row.get("root_component_code", "")).strip()
    feedback_component_code = str(feedback_row.get("component_code", "")).strip()
    feedback_component_uid = str(feedback_row.get("component_uid", "")).strip()
    if not root_component_code:
        return []
    if root_component_code == feedback_component_code:
        return [feedback_component_uid] if feedback_component_uid else []
    matches = component_df[component_df["component_code"].astype(str) == root_component_code].copy()
    if matches.empty:
        return []
    feedback_suffix = numeric_suffix(feedback_component_uid)
    if feedback_suffix is not None:
        matches["_distance"] = matches["component_uid"].map(lambda value: abs((numeric_suffix(value) or 10**12) - feedback_suffix))
        matches = matches.sort_values(["_distance", "component_uid"])
    else:
        matches = matches.sort_values("component_uid")
    root_uids = matches["component_uid"].astype(str).head(3).tolist()
    graph_entities = set(pd.concat([triples_df["head"].astype(str), triples_df["tail"].astype(str)], ignore_index=True))
    return [uid for uid in root_uids if uid in graph_entities]


def collect_component_lifecycle_candidates(
    component_uid: str,
    triples_df: pd.DataFrame,
    candidates: dict[str, dict[str, Any]],
    scope_prefix: str,
) -> set[str]:
    return lifecycle_candidates_for_object(
        triples_df,
        component_uid,
        candidates,
        lifecycle_scope=f"{scope_prefix}_candidate",
        lifecycle_score=SCOPE_PATH_SCORES[f"{scope_prefix}_candidate"],
        expanded_scope=f"{scope_prefix}_expanded_candidate",
        expanded_score=SCOPE_PATH_SCORES[f"{scope_prefix}_expanded_candidate"],
        max_expanded=80,
    )


def hydraulic_rca_context(data: dict[str, Any], feedback_row: pd.Series) -> tuple[dict[str, Any], set[str], list[str], pd.Series | None]:
    if "case_id" not in feedback_row:
        return {}, set(), [], None
    case_id = str(feedback_row.case_id)
    rca_df = data["rca"]
    rca_match = rca_df[rca_df["case_id"].astype(str) == case_id]
    rca_context: dict[str, Any] = {}
    rca_related: set[str] = set()
    root_uids: list[str] = []
    rca_row: pd.Series | None = None
    if not rca_match.empty:
        rca = rca_match.iloc[0]
        rca_row = rca
        root_uids = get_rca_root_component_uids(feedback_row, rca_row, data["component"], data["triples"])
        root_component_code = str(rca.root_component_code)
        feedback_component_code = str(feedback_row.get("component_code", ""))
        rca_context.update(
            {
                "case_id": case_id,
                "root_sensor": str(rca.root_sensor),
                "root_sensor_confidence": float(rca.root_sensor_confidence),
                "secondary_sensor": str(rca.secondary_sensor),
                "secondary_sensor_confidence": float(rca.secondary_sensor_confidence),
                "root_component_code": str(rca.root_component_code),
                "root_component_name": str(rca.root_component_name),
                "root_component_uids": root_uids,
                "component_confidence": float(rca.component_confidence),
                "rca_confidence": float(rca.rca_confidence),
                "is_root_component_different_from_feedback": bool(root_component_code != feedback_component_code),
            }
        )
        rca_related.update([str(rca.root_sensor), str(rca.secondary_sensor)])
        rca_related.update(root_uids)

    diagnosis = data["diagnosis"]
    diag = diagnosis[diagnosis["case_id"].astype(str) == case_id].sort_values("rank_no").head(3)
    if not diag.empty:
        rca_context["component_diagnosis_top3"] = diag.to_dict(orient="records")

    subtype = data["subtype"]
    sub = subtype[subtype["case_id"].astype(str) == case_id].sort_values("probability", ascending=False).head(5)
    if not sub.empty:
        rca_context["subtype_top5"] = sub.to_dict(orient="records")
        rca_related.update(sub["subtype_id"].astype(str).tolist())

    sensor = data["sensor"]
    sen = sensor[sensor["case_id"].astype(str) == case_id]
    if not sen.empty:
        sensor_values = sen.iloc[0].drop(labels=["case_id"]).astype(float)
        rca_context["sensor_energy_top5"] = {str(k): float(v) for k, v in sensor_values.sort_values(ascending=False).head(5).items()}

    return rca_context, rca_related, root_uids, rca_row


def build_candidates(data: dict[str, Any], domain: str, feedback_row: pd.Series, rng: np.random.Generator) -> tuple[dict[str, dict[str, Any]], dict[str, Any], set[str]]:
    triples = data["triples"]
    candidates: dict[str, dict[str, Any]] = {}
    target = target_object_from_feedback(domain, feedback_row, triples)
    lifecycle_candidates_for_object(triples, target["object_id"], candidates)
    rca_context: dict[str, Any] = {}
    rca_related: set[str] = set()
    if domain == "hydraulic":
        rca_context, rca_related, root_uids, _ = hydraulic_rca_context(data, feedback_row)
        for root_uid in root_uids:
            root_lifecycle = collect_component_lifecycle_candidates(root_uid, triples, candidates, "rca_root_component")
            rca_related.update(root_lifecycle)
    build_global_candidates(triples, candidates, str(feedback_row.fault_type), domain, data["stage_mapping"], data["fault_prior"], rng)
    return candidates, rca_context, rca_related


def score_candidates(
    model: TransHModel,
    device: torch.device,
    feedback_id: str,
    candidates: dict[str, dict[str, Any]],
    entity2id: dict[str, int],
    relation2id: dict[str, int],
    batch_size: int,
) -> dict[str, float]:
    if feedback_id not in entity2id:
        raise ValueError("当前模型只支持已进入训练图谱的 feedback；后续可通过场景实体或增量映射支持新反馈。")
    if "may_caused_by" not in relation2id:
        raise ValueError("当前模型 relation2id 中缺少 may_caused_by，无法执行原因追溯。")
    candidate_ids = [cid for cid in candidates if cid in entity2id]
    if not candidate_ids:
        raise ValueError("候选原因实体均未进入训练图谱，无法执行 TransH 打分。")
    raw_scores: dict[str, float] = {}
    relation_id = relation2id["may_caused_by"]
    head_id = entity2id[feedback_id]
    for start in range(0, len(candidate_ids), batch_size):
        batch = candidate_ids[start : start + batch_size]
        triples = torch.tensor([[head_id, relation_id, entity2id[cid]] for cid in batch], dtype=torch.long, device=device)
        scores = model.score_triples(triples).detach().cpu().numpy()
        for cid, score in zip(batch, scores):
            raw_scores[cid] = float(score)
    values = np.asarray(list(raw_scores.values()), dtype=float)
    min_score = float(values.min())
    max_score = float(values.max())
    return {cid: float(1.0 - (score - min_score) / (max_score - min_score + 1e-8)) for cid, score in raw_scores.items()}


def rule_prior_score(domain: str, fault_type: str, candidate_type: str, stage: str, fault_prior: dict[str, Any]) -> float:
    prior = fault_prior.get("domains", {}).get(domain, {}).get(fault_type, {})
    if candidate_type in prior:
        return float(prior[candidate_type])
    if stage in prior:
        return float(prior[stage])
    return 0.5


FAULT_STAGE_EVIDENCE_PRIOR = {
    "inner_race_fault": {"manufacturing": 0.75, "assembly": 0.70, "material": 0.70},
    "outer_race_fault": {"assembly": 0.75, "manufacturing": 0.70, "design": 0.68},
    "rolling_element_fault": {"material": 0.75, "manufacturing": 0.70, "operation": 0.68},
    "oil_leakage": {"operation": 0.72, "assembly": 0.70, "design": 0.68, "material": 0.65},
    "valve_stuck": {"operation": 0.74, "manufacturing": 0.70, "material": 0.65},
    "pressure_abnormal": {"operation": 0.74, "design": 0.70, "assembly": 0.66, "manufacturing": 0.65},
    "flow_abnormal": {"operation": 0.74, "design": 0.70, "assembly": 0.66, "manufacturing": 0.65},
    "temperature_abnormal": {"operation": 0.75, "inspection": 0.66, "design": 0.64},
    "vibration_abnormal": {"operation": 0.72, "assembly": 0.70, "manufacturing": 0.68},
}


def evidence_score(candidate: dict[str, Any], feedback_row: pd.Series, rca_related: set[str], candidate_stage: str) -> float:
    candidate_id = str(candidate["candidate_id"])
    scope = str(candidate["candidate_scope"])
    score = 0.4
    if scope == "lifecycle_candidate":
        score = max(score, 0.75)
    if scope == "expanded_candidate":
        score = max(score, 0.60)
    if scope == "rca_root_component_candidate":
        score = max(score, 0.80)
    if scope == "rca_root_component_expanded_candidate":
        score = max(score, 0.68)
    if scope == "global_same_type_candidate":
        score = max(score, 0.45)
    if candidate_id in rca_related:
        score = max(score, 0.75)
    if scope in {"rca_root_component_candidate", "rca_root_component_expanded_candidate"}:
        score = max(score, 0.78)
    stage_prior = FAULT_STAGE_EVIDENCE_PRIOR.get(str(feedback_row.fault_type), {})
    if candidate_stage in stage_prior:
        score = max(score, float(stage_prior[candidate_stage]))
    return score


def describe_candidate(candidate: dict[str, Any], candidate_stage: str, feedback_row: pd.Series, rca_context: dict[str, Any] | None = None) -> str:
    scope = candidate["candidate_scope"]
    stage_name = STAGE_NAMES.get(candidate_stage, "未知阶段")
    if scope == "lifecycle_candidate":
        return f"该实体位于当前反馈对象生命周期链上，属于{stage_name}，具备直接路径证据。"
    if scope == "rca_root_component_candidate":
        return f"该候选来自 RCA 根因部件生命周期链，属于{stage_name}。RCA 根因部件与反馈对象不完全一致时，可能表示故障传播或相邻部件诱发。"
    if scope == "expanded_candidate":
        return f"该实体由当前反馈对象生命周期节点一跳扩展得到，属于{stage_name}，具备间接路径证据。"
    if scope == "rca_root_component_expanded_candidate":
        if rca_context and rca_context.get("is_root_component_different_from_feedback"):
            return f"该候选由 RCA 根因部件生命周期节点扩展得到，属于{stage_name}，可作为传播链辅助证据。RCA 根因部件与反馈对象不完全一致，可能表示故障传播或相邻部件诱发。"
        return f"该候选由 RCA 根因部件生命周期节点扩展得到，属于{stage_name}，可作为传播链辅助证据。"
    return f"该实体为同类型全局补充候选，不直接证明属于当前对象生命周期链，需结合 TransH 与规则先验复核。"


def select_topk_with_scope_constraints(
    candidate_rows: list[dict[str, Any]],
    top_k: int = 6,
    domain: str = "bearing",
    feedback_row: pd.Series | None = None,
) -> list[dict[str, Any]]:
    """Select TopK candidates with direct lifecycle type coverage constraints."""
    sorted_rows = sorted(candidate_rows, key=lambda item: float(item["adjusted_final_score"]), reverse=True)
    selected: list[dict[str, Any]] = []
    selected_ids: set[str] = set()

    hydraulic_lifecycle_type_order = [
        "ManufacturingBatch",
        "MaterialBatch",
        "AssemblyRecord",
        "InspectionRecord",
        "MaintenanceRecord",
        "DesignSpec",
        "PipeDesignParam",
    ]
    bearing_lifecycle_type_order = [
        "BearingDesignSpec",
        "MaterialBatch",
        "ManufacturingBatch",
        "AssemblyRecord",
        "InspectionRecord",
        "MaintenanceRecord",
    ]

    def scope_count(scope: str) -> int:
        return sum(item["candidate_scope"] == scope for item in selected)

    def lifecycle_count() -> int:
        return scope_count("lifecycle_candidate")

    def can_add(row: dict[str, Any], strict_expanded_limit: bool = True) -> bool:
        if len(selected) >= top_k or row["candidate_id"] in selected_ids:
            return False
        scope = str(row["candidate_scope"])
        if scope == "global_same_type_candidate" and scope_count("global_same_type_candidate") >= 1:
            return False
        if strict_expanded_limit and scope == "expanded_candidate" and scope_count("expanded_candidate") >= 1 and lifecycle_count() >= 4:
            return False
        return True

    def add_row(row: dict[str, Any], strict_expanded_limit: bool = True) -> bool:
        if not can_add(row, strict_expanded_limit=strict_expanded_limit):
            return False
        selected.append(row)
        selected_ids.add(row["candidate_id"])
        return True

    def best_by_scope_and_type(scope: str, candidate_type: str) -> dict[str, Any] | None:
        matches = [row for row in sorted_rows if row["candidate_scope"] == scope and row["candidate_type"] == candidate_type and row["candidate_id"] not in selected_ids]
        return matches[0] if matches else None

    def add_lifecycle_type_coverage(type_order: list[str], max_count: int) -> None:
        for candidate_type in type_order:
            if lifecycle_count() >= max_count or len(selected) >= top_k:
                break
            row = best_by_scope_and_type("lifecycle_candidate", candidate_type)
            if row is not None:
                add_row(row)

    if domain == "hydraulic":
        component_code = "" if feedback_row is None else str(feedback_row.get("component_code", ""))
        if component_code == "C011":
            pipe_row = best_by_scope_and_type("lifecycle_candidate", "PipeDesignParam")
            if pipe_row is not None:
                add_row(pipe_row)
        add_lifecycle_type_coverage(hydraulic_lifecycle_type_order, max_count=min(5, top_k))
        for row in sorted_rows:
            if scope_count("rca_root_component_candidate") >= 1 or len(selected) >= top_k:
                break
            if row["candidate_scope"] == "rca_root_component_candidate":
                add_row(row)
    else:
        add_lifecycle_type_coverage(bearing_lifecycle_type_order, max_count=top_k)
        if lifecycle_count() < top_k:
            for row in sorted_rows:
                if lifecycle_count() >= top_k or len(selected) >= top_k:
                    break
                if row["candidate_scope"] == "lifecycle_candidate":
                    add_row(row)

    for row in sorted_rows:
        if len(selected) >= top_k:
            break
        add_row(row, strict_expanded_limit=True)

    if len(selected) < top_k:
        for row in sorted_rows:
            if len(selected) >= top_k:
                break
            add_row(row, strict_expanded_limit=False)

    selected.sort(key=lambda item: float(item["adjusted_final_score"]), reverse=True)
    for rank, row in enumerate(selected, start=1):
        row["rank"] = rank
    return selected


def rank_candidates(
    candidates: dict[str, dict[str, Any]],
    transh_scores: dict[str, float],
    data: dict[str, Any],
    domain: str,
    feedback_row: pd.Series,
    rca_related: set[str],
    rca_context: dict[str, Any] | None = None,
) -> list[dict[str, Any]]:
    rows = []
    for cid, transh_score in transh_scores.items():
        candidate = candidates[cid]
        candidate_stage = stage_for_type(candidate["candidate_type"], data["stage_mapping"])
        path_score = float(candidate["path_score"])
        ev_score = evidence_score(candidate, feedback_row, rca_related, candidate_stage)
        prior_score = rule_prior_score(domain, str(feedback_row.fault_type), candidate["candidate_type"], candidate_stage, data["fault_prior"])
        final_score = 0.50 * transh_score + 0.20 * path_score + 0.15 * ev_score + 0.15 * prior_score
        candidate_scope = str(candidate["candidate_scope"])
        adjusted_final_score = final_score + SCOPE_ADJUSTMENT.get(candidate_scope, 0.0)
        rows.append(
            {
                "candidate_id": cid,
                "candidate_type": candidate["candidate_type"],
                "candidate_stage": candidate_stage,
                "candidate_stage_name": STAGE_NAMES.get(candidate_stage, "未知阶段"),
                "transh_score": round(float(transh_score), 6),
                "path_score": round(path_score, 6),
                "evidence_score": round(float(ev_score), 6),
                "rule_prior_score": round(float(prior_score), 6),
                "final_score": round(float(final_score), 6),
                "adjusted_final_score": round(float(adjusted_final_score), 6),
                "candidate_scope": candidate_scope,
                "reason_description": describe_candidate(candidate, candidate_stage, feedback_row, rca_context),
            }
        )
    return select_topk_with_scope_constraints(rows, top_k=6, domain=domain, feedback_row=feedback_row)


def compute_stage_attribution(
    top6_candidates: list[dict[str, Any]],
    feedback_row: pd.Series,
    domain: str,
    rca_context: dict[str, Any] | None = None,
) -> dict[str, Any]:
    """Compute stage attribution from Top6 evidence without using answer labels."""
    del rca_context
    stages = ["design", "material", "manufacturing", "assembly", "inspection", "operation"]
    stage_totals = {stage: 0.0 for stage in stages}
    detail: list[dict[str, Any]] = []
    covered_stages: set[str] = set()

    for idx, item in enumerate(top6_candidates, start=1):
        stage = str(item.get("candidate_stage", "unknown"))
        if stage not in stage_totals:
            continue
        scope = str(item.get("candidate_scope", ""))
        candidate_type = str(item.get("candidate_type", ""))
        adjusted_score = float(item.get("adjusted_final_score", item.get("final_score", 0.0)) or 0.0)
        rank_score = STAGE_RANK_SCORES.get(int(item.get("rank", idx) or idx), STAGE_RANK_SCORES.get(idx, 0.42))
        scope_weight = SCOPE_STAGE_WEIGHT.get(scope, 0.25)
        type_stage_weight = TYPE_STAGE_WEIGHT.get(candidate_type, 0.50)
        contribution = adjusted_score * rank_score * scope_weight * type_stage_weight
        stage_totals[stage] += contribution

        if scope in {"lifecycle_candidate", "rca_root_component_candidate"} and candidate_type in DIRECT_LIFECYCLE_TYPES:
            covered_stages.add(stage)

        detail.append(
            {
                "candidate_id": str(item.get("candidate_id", "")),
                "candidate_type": candidate_type,
                "candidate_stage": stage,
                "rank": int(item.get("rank", idx) or idx),
                "adjusted_final_score": round(adjusted_score, 6),
                "rank_score": round(float(rank_score), 6),
                "scope_weight": round(float(scope_weight), 6),
                "type_stage_weight": round(float(type_stage_weight), 6),
                "contribution": round(float(contribution), 6),
            }
        )

    for stage in covered_stages:
        stage_totals[stage] += STAGE_COVERAGE_BONUS.get(stage, 0.0)

    fault_type = str(feedback_row.get("fault_type", ""))
    for stage, bonus in FAULT_STAGE_PRIOR.get(domain, {}).get(fault_type, {}).items():
        if stage in stage_totals:
            stage_totals[stage] += float(bonus)

    max_total = max(stage_totals.values()) if stage_totals else 0.0
    scores = {stage: round(total / (max_total + 1e-8), 6) if max_total > 0 else 0.0 for stage, total in stage_totals.items()}
    rank = [
        {"stage": stage, "stage_name": STAGE_NAMES[stage], "score": score}
        for stage, score in sorted(scores.items(), key=lambda item: item[1], reverse=True)
    ]
    primary = rank[0]["stage"] if rank else "unknown"
    focus_stage_top3 = [
        {
            "stage": str(item["stage"]),
            "stage_name": str(item["stage_name"]),
            "score": float(item["score"]),
            "feedback_subsystem": STAGE_SUBSYSTEMS.get(str(item["stage"]), "待人工复核子系统"),
        }
        for item in rank[:3]
    ]
    return {
        "primary_stage": primary,
        "primary_stage_name": STAGE_NAMES.get(primary, "未知阶段"),
        "feedback_target_subsystem": STAGE_SUBSYSTEMS.get(primary, "待人工复核子系统"),
        "stage_scores": scores,
        "stage_rank": rank,
        "focus_stage_top3": focus_stage_top3,
        "feedback_subsystem_top3": [item["feedback_subsystem"] for item in focus_stage_top3],
        "stage_attribution_detail": detail,
    }


def stage_attribution(top6: list[dict[str, Any]]) -> dict[str, Any]:
    dummy_feedback = pd.Series({"fault_type": ""})
    return compute_stage_attribution(top6, dummy_feedback, domain="bearing")


def focus_stage_text(attribution: dict[str, Any]) -> str:
    focus_items = attribution.get("focus_stage_top3", [])
    stage_names = [
        str(item.get("stage_name", ""))
        for item in focus_items
        if str(item.get("stage_name", "")).strip()
    ]
    secondary_names = stage_names[1:3]
    if not secondary_names:
        return ""
    return "同时，系统建议重点关注" + "、".join(secondary_names) + "等相关阶段，以支持跨阶段联合复核。"


def conclusion_text(domain: str, feedback_row: pd.Series, target: dict[str, str], top6: list[dict[str, Any]], attribution: dict[str, Any], rca_context: dict[str, Any]) -> str:
    top1 = top6[0] if top6 else {"candidate_id": "N/A"}
    focus_note = focus_stage_text(attribution)
    if domain == "hydraulic":
        root_component = rca_context.get("root_component_name", str(getattr(feedback_row, "component_name", target["object_name"])))
        root_sensor = rca_context.get("root_sensor", "N/A")
        propagation = ""
        if rca_context.get("root_component_code") and str(rca_context.get("root_component_code")) != str(getattr(feedback_row, "component_code", "")):
            propagation = "本次追溯同时考虑了反馈对象的生命周期链和 RCA 根因部件的生命周期链。RCA 根因部件与反馈对象不完全一致，可能表示故障传播或相邻部件诱发。"
        else:
            propagation = "RCA 根因部件与反馈部件一致，暂未发现明显跨部件传播迹象。"
        has_rca_root_candidate = any(
            item.get("candidate_scope") in {"rca_root_component_candidate", "rca_root_component_expanded_candidate"} for item in top6
        )
        rca_candidate_note = ""
        if has_rca_root_candidate:
            rca_candidate_note = "Top6 中包含来自 RCA 根因部件生命周期链的候选原因，说明根因部件的生命周期数据对本次追溯具有辅助解释作用。"
        return (
            f"液压反馈对象为 {target['object_name']}，根因部件为 {root_component}，根因传感器为 {root_sensor}。"
            f"Top1 疑似原因是 {top1['candidate_id']}，首要疑似生命周期阶段为 {attribution['primary_stage_name']}，"
            f"建议优先反馈至{attribution['feedback_target_subsystem']}。{focus_note}{propagation}{rca_candidate_note}"
        )
    return (
        f"轴承反馈对象为 {target['object_id']}，Top1 疑似原因是 {top1['candidate_id']}，"
        f"首要疑似生命周期阶段为 {attribution['primary_stage_name']}，建议优先反馈至{attribution['feedback_target_subsystem']}复核。{focus_note}"
    )


def print_tables(domain: str, case_id: str, feedback_row: pd.Series, target: dict[str, str], top6: list[dict[str, Any]], attribution: dict[str, Any], output_path: Path) -> None:
    print(f"domain: {domain}")
    print(f"case_id: {case_id}")
    print(f"fault_type: {feedback_row.fault_type}")
    print(f"target object: {target['object_id']} ({target['object_type']}, {target['object_name']})")
    print("\nTop6 candidates:")
    top_df = pd.DataFrame(top6)[
        [
            "rank",
            "candidate_id",
            "candidate_type",
            "candidate_stage",
            "transh_score",
            "path_score",
            "evidence_score",
            "rule_prior_score",
            "final_score",
            "adjusted_final_score",
            "candidate_scope",
        ]
    ]
    print(top_df.to_string(index=False))
    print("\nstage_attribution:")
    print(pd.DataFrame(attribution["stage_rank"]).to_string(index=False))
    print(f"\nJSON output: {output_path}")


def run_reasoning_for_case(
    domain: str,
    case_id: str,
    save_json: bool = True,
    seed: int = 20260604,
    batch_size: int = 4096,
    prepared: dict[str, Any] | None = None,
    model_variant: str = "default",
) -> dict[str, Any]:
    py_rng = random.Random(seed)
    np_rng = np.random.default_rng(seed)
    data = prepared["data"] if prepared else load_inputs(domain, model_variant=model_variant)
    feedback_row = resolve_feedback(data["feedback"], case_id, py_rng)
    feedback_id = str(feedback_row.feedback_id)
    entity2id = prepared["entity2id"] if prepared else {str(k): int(v) for k, v in data["entity2id"].items()}
    relation2id = prepared["relation2id"] if prepared else {str(k): int(v) for k, v in data["relation2id"].items()}
    if feedback_id not in entity2id:
        raise ValueError("当前模型只支持已进入训练图谱的 feedback；后续可通过场景实体或增量映射支持新反馈。")

    if prepared:
        device = prepared["device"]
        model = prepared["model"]
    else:
        device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        model = load_model(domain, data["paths"]["model"], device)
    target = target_object_from_feedback(domain, feedback_row, data["triples"])
    candidates, rca_context, rca_related = build_candidates(data, domain, feedback_row, np_rng)
    transh_scores = score_candidates(model, device, feedback_id, candidates, entity2id, relation2id, batch_size)
    top6 = rank_candidates(candidates, transh_scores, data, domain, feedback_row, rca_related, rca_context)
    attribution = compute_stage_attribution(top6, feedback_row, domain, rca_context)
    attribution_detail = attribution.get("stage_attribution_detail", [])
    conclusion = conclusion_text(domain, feedback_row, target, top6, attribution, rca_context)

    report = {
        "domain": domain,
        "case_id": feedback_id,
        "feedback_summary": {
            "feedback_id": feedback_id,
            "fault_type": str(feedback_row.fault_type),
            "fault_position": str(feedback_row.fault_position),
            "fault_severity": str(feedback_row.fault_severity),
            "diagnosis_confidence": float(feedback_row.diagnosis_confidence),
            "rca_confidence": float(feedback_row.rca_confidence),
        },
        "target_object": target,
        "top6_candidates": top6,
        "stage_attribution": attribution,
        "stage_attribution_detail": attribution_detail,
        "rca_context": rca_context,
        "conclusion_text": conclusion,
    }
    if save_json:
        output_dir = ROOT / "outputs" / domain / "reports"
        output_dir.mkdir(parents=True, exist_ok=True)
        safe_case_id = feedback_id.replace("/", "_").replace("\\", "_")
        output_path = output_dir / f"reasoning_{safe_case_id}.json"
        output_path.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
        report["_output_path"] = str(output_path)
    return report


def prepare_reasoning_context(domain: str, model_variant: str = "default") -> dict[str, Any]:
    data = load_inputs(domain, model_variant=model_variant)
    entity2id = {str(k): int(v) for k, v in data["entity2id"].items()}
    relation2id = {str(k): int(v) for k, v in data["relation2id"].items()}
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    model = load_model(domain, data["paths"]["model"], device)
    return {"data": data, "entity2id": entity2id, "relation2id": relation2id, "device": device, "model": model, "model_variant": model_variant}

def print_rca_context(rca_context: dict) -> None:
    print("\nRCA诊断信息:")

    if not rca_context:
        print("  无 RCA 上下文信息。该情况可能出现在 bearing 域，或 hydraulic 当前 case 未匹配到 RCA 数据。")
        return

    print(f"  根因传感器: {rca_context.get('root_sensor', 'N/A')}")
    print(f"  根因传感器置信度: {rca_context.get('root_sensor_confidence', 'N/A')}")
    print(f"  次因传感器: {rca_context.get('secondary_sensor', 'N/A')}")
    print(f"  次因传感器置信度: {rca_context.get('secondary_sensor_confidence', 'N/A')}")
    print(f"  根因部件编码: {rca_context.get('root_component_code', 'N/A')}")
    print(f"  根因部件名称: {rca_context.get('root_component_name', 'N/A')}")
    print(f"  根因部件UID: {rca_context.get('root_component_uids', [])}")
    print(f"  部件置信度: {rca_context.get('component_confidence', 'N/A')}")
    print(f"  RCA置信度: {rca_context.get('rca_confidence', 'N/A')}")

    print("\n  传感器异常能量Top5:")
    sensor_energy_top5 = rca_context.get("sensor_energy_top5", {})
    if sensor_energy_top5:
        for sensor, value in sensor_energy_top5.items():
            print(f"    {sensor}: {value}")
    else:
        print("    无")

    print("\n  部件诊断Top3:")
    component_diagnosis_top3 = rca_context.get("component_diagnosis_top3", [])
    if component_diagnosis_top3:
        for item in component_diagnosis_top3:
            print(f"    {item}")
    else:
        print("    无")

    print("\n  故障子类型Top5:")
    subtype_top5 = rca_context.get("subtype_top5", [])
    if subtype_top5:
        for item in subtype_top5:
            print(f"    {item}")
    else:
        print("    无")

def main() -> None:
    args = parse_args()
    if torch.cuda.is_available():
        print(f"device: cuda ({torch.cuda.get_device_name(0)})")
    else:
        print("device: cpu")
    print(f"model_variant: {args.model_variant}")
    prepared = prepare_reasoning_context(args.domain, model_variant=args.model_variant)
    report = run_reasoning_for_case(
        args.domain,
        args.case_id,
        save_json=True,
        seed=args.seed,
        batch_size=args.batch_size,
        prepared=prepared,
        model_variant=args.model_variant,
    )
    feedback_row = resolve_feedback(prepared["data"]["feedback"], report["case_id"], random.Random(args.seed))
    target = report["target_object"]
    print_tables(args.domain, report["case_id"], feedback_row, target, report["top6_candidates"], report["stage_attribution"], Path(report["_output_path"]))
    print_rca_context(report.get("rca_context", {}))
    if args.print_json:
        print(json.dumps(report, ensure_ascii=False, indent=2))



if __name__ == "__main__":
    main()
