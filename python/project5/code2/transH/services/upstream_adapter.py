from __future__ import annotations

import csv
import json
import re
from datetime import datetime
from pathlib import Path
from typing import Any


HYDRAULIC_COMPONENT_NAMES = {
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

HYDRAULIC_FAULT_TYPE_MAP = {
    "方向阀故障": "valve_stuck",
    "方向控制阀故障": "valve_stuck",
    "阀芯卡滞": "valve_stuck",
    "单向阀故障": "flow_abnormal",
    "节流阀故障": "flow_abnormal",
    "节流口堵塞": "flow_abnormal",
    "过滤器堵塞": "flow_abnormal",
    "管路泄漏": "oil_leakage",
    "油液泄漏": "oil_leakage",
    "液压泵泄漏": "oil_leakage",
    "蓄能器故障": "pressure_abnormal",
    "溢流阀故障": "pressure_abnormal",
    "卸荷阀故障": "pressure_abnormal",
    "作动筒故障": "pressure_abnormal",
    "冷却器故障": "temperature_abnormal",
    "油箱故障": "oil_leakage",
    "振动异常": "vibration_abnormal",
}

HYDRAULIC_ENGLISH_FAULT_TYPES = {
    "pressure_abnormal",
    "flow_abnormal",
    "oil_leakage",
    "temperature_abnormal",
    "valve_stuck",
    "vibration_abnormal",
}


def normalize_text(value: Any) -> str:
    return "" if value is None else str(value).strip()


def normalize_hydraulic_component_name(name: str) -> str:
    replacements = {
        "调速阀": "节流阀",
        "管路": "管路总成",
        "管路组件": "管路总成",
    }
    name = normalize_text(name)
    return replacements.get(name, name)


def normalize_hydraulic_fault_type(value: Any) -> str:
    text = normalize_text(value)
    if text in HYDRAULIC_ENGLISH_FAULT_TYPES:
        return text
    return HYDRAULIC_FAULT_TYPE_MAP.get(text, text)


def normalize_upstream_payload(domain: str, payload: dict[str, Any]) -> dict[str, Any]:
    if domain == "hydraulic":
        rca = payload.get("rca_result") if isinstance(payload.get("rca_result"), dict) else {}
        component_code = normalize_text(payload.get("component_code") or rca.get("root_component_code"))
        component_name = normalize_hydraulic_component_name(
            payload.get("component_name") or rca.get("root_component_name") or HYDRAULIC_COMPONENT_NAMES.get(component_code, "")
        )
        fault_position = normalize_hydraulic_component_name(payload.get("fault_position") or component_name)
        return {
            "upstream_case_id": normalize_text(payload.get("upstream_case_id")) or f"UPSTREAM-{datetime.now().strftime('%Y%m%d%H%M%S')}",
            "fault_type": normalize_hydraulic_fault_type(payload.get("fault_type")),
            "raw_fault_type": normalize_text(payload.get("fault_type")),
            "fault_position": fault_position,
            "component_code": component_code,
            "component_name": component_name,
            "diagnosis_confidence": payload.get("diagnosis_confidence"),
            "fault_severity": normalize_text(payload.get("fault_severity")),
            "root_sensor": normalize_text(rca.get("root_sensor")),
            "secondary_sensor": normalize_text(rca.get("secondary_sensor")),
            "root_component_code": normalize_text(rca.get("root_component_code")),
            "root_component_name": normalize_hydraulic_component_name(rca.get("root_component_name")),
            "sensor_energy": payload.get("sensor_energy") if isinstance(payload.get("sensor_energy"), dict) else {},
            "rca_result": rca,
        }

    rca = payload.get("rca_result") if isinstance(payload.get("rca_result"), dict) else {}
    return {
        "upstream_case_id": normalize_text(payload.get("upstream_case_id")) or f"UPSTREAM-{datetime.now().strftime('%Y%m%d%H%M%S')}",
        "bearing_id": normalize_text(payload.get("bearing_id")),
        "object_id": normalize_text(payload.get("object_id") or payload.get("component_uid") or payload.get("transh_object_id")),
        "fault_type": normalize_text(payload.get("fault_type")),
        "fault_position": normalize_text(payload.get("fault_position")),
        "diagnosis_confidence": payload.get("diagnosis_confidence"),
        "fault_severity": normalize_text(payload.get("fault_severity")),
        "rca_result": rca,
    }


def read_csv_rows(path: Path) -> list[dict[str, str]]:
    if not path.exists():
        return []
    with path.open("r", encoding="utf-8-sig", newline="") as handle:
        return [dict(row) for row in csv.DictReader(handle)]


def candidate(case_id: str, object_id: str, object_name: str, score: float, reason: str) -> dict[str, Any]:
    return {
        "case_id": case_id,
        "object_id": object_id,
        "object_name": object_name,
        "score": round(float(score), 6),
        "reason": reason,
    }


def topk_candidates(candidates: list[dict[str, Any]], minimum: int = 3) -> list[dict[str, Any]]:
    ranked = sorted(candidates, key=lambda item: (-float(item["score"]), str(item["case_id"])))
    return ranked[: max(minimum, min(len(ranked), 5))]


def resolve_hydraulic_target_case(normalized: dict[str, Any], project_root: Path) -> dict[str, Any]:
    feedback_rows = read_csv_rows(project_root / "data" / "hydraulic" / "hydraulic_feedback.csv")
    rca_rows = {row.get("feedback_id", ""): row for row in read_csv_rows(project_root / "data" / "hydraulic" / "hydraulic_rca_case.csv")}
    component_code = normalized.get("component_code", "")
    component_name = normalized.get("component_name", "")
    fault_type = normalized.get("fault_type", "")
    fault_position = normalized.get("fault_position", "")
    root_sensor = normalized.get("root_sensor", "")
    secondary_sensor = normalized.get("secondary_sensor", "")
    candidates: list[dict[str, Any]] = []

    for row in feedback_rows:
        fid = row.get("feedback_id", "")
        rca = rca_rows.get(fid, {})
        score = 0.0
        reasons: list[str] = []
        if component_code and row.get("component_code") == component_code:
            score += 0.40
            reasons.append(f"component_code={component_code}")
        if fault_type and row.get("fault_type") == fault_type:
            score += 0.30
            reasons.append(f"fault_type={fault_type}")
        row_position = normalize_hydraulic_component_name(row.get("fault_position", ""))
        row_name = normalize_hydraulic_component_name(row.get("component_name", ""))
        if fault_position and fault_position in {row_position, row_name}:
            score += 0.15
            reasons.append(f"fault_position={fault_position}")
        sensor_match = False
        if root_sensor and root_sensor == rca.get("root_sensor"):
            sensor_match = True
        if secondary_sensor and secondary_sensor == rca.get("secondary_sensor"):
            sensor_match = True
        if normalized.get("root_component_code") and normalized.get("root_component_code") == rca.get("root_component_code"):
            sensor_match = True
        if sensor_match:
            score += 0.10
            reasons.append("RCA/sensor evidence matched")
        if row.get("component_uid") and row.get("true_reason_id"):
            score += 0.05
            reasons.append("local lifecycle data complete")
        if score > 0:
            candidates.append(candidate(fid, row.get("component_uid", ""), row.get("component_name", ""), score, "; ".join(reasons)))

    if not candidates and fault_type:
        for row in feedback_rows:
            if row.get("fault_type") == fault_type:
                candidates.append(candidate(row.get("feedback_id", ""), row.get("component_uid", ""), row.get("component_name", ""), 0.35, f"fallback same fault_type={fault_type}"))

    warning = None
    if candidates:
        topk = topk_candidates(candidates)
        best = topk[0]
        best_score = float(best["score"])
        mode = "inferred_from_component_code" if component_code and "component_code=" in best["reason"] else "inferred_from_fault_type_and_position"
        if component_code and "component_code=" not in best["reason"]:
            warning = "未找到完全一致的组件实例，当前采用最接近的本地案例用于流程跑通。"
        confidence = min(0.85, best_score) if mode == "inferred_from_component_code" else min(0.75, max(0.6, best_score))
        return {
            "mode": mode,
            "selected_case_id": best["case_id"],
            "selected_object_id": best["object_id"],
            "selected_object_name": best["object_name"],
            "confidence": round(max(0.0, confidence), 6),
            "match_reason": best["reason"],
            "match_warning": warning,
            "candidate_topk": topk,
        }

    fallback = next((row for row in feedback_rows if row.get("feedback_id") == "HFB-01676"), feedback_rows[0] if feedback_rows else {})
    return {
        "mode": "fallback",
        "selected_case_id": fallback.get("feedback_id", "HFB-01676"),
        "selected_object_id": fallback.get("component_uid", ""),
        "selected_object_name": fallback.get("component_name", ""),
        "confidence": 0.5,
        "match_reason": "fallback to default hydraulic demonstration case",
        "match_warning": "未找到完全一致的组件实例，当前采用兜底本地案例用于流程跑通。",
        "candidate_topk": [
            candidate(row.get("feedback_id", ""), row.get("component_uid", ""), row.get("component_name", ""), 0.0, "fallback candidate")
            for row in feedback_rows[:3]
        ],
    }


def resolve_bearing_target_case(normalized: dict[str, Any], project_root: Path) -> dict[str, Any]:
    feedback_rows = read_csv_rows(project_root / "data" / "bearing" / "bearing_feedback.csv")
    bearing_id = normalized.get("bearing_id", "")
    object_id = normalized.get("object_id", "")
    fault_type = normalized.get("fault_type", "")
    fault_position = normalized.get("fault_position", "")
    severity = normalized.get("fault_severity", "")

    if bearing_id:
        for row in feedback_rows:
            if row.get("bearing_id") == bearing_id:
                best = candidate(row.get("feedback_id", ""), row.get("bearing_id", ""), row.get("bearing_id", ""), 1.0, f"exact bearing_id={bearing_id}")
                related = [
                    candidate(r.get("feedback_id", ""), r.get("bearing_id", ""), r.get("bearing_id", ""), 0.2, "same domain reference")
                    for r in feedback_rows[:3]
                    if r.get("feedback_id") != row.get("feedback_id")
                ]
                return {
                    "mode": "provided_bearing_id",
                    "selected_case_id": best["case_id"],
                    "selected_object_id": best["object_id"],
                    "selected_object_name": best["object_name"],
                    "confidence": 1.0,
                    "match_reason": best["reason"],
                    "match_warning": None,
                    "candidate_topk": [best, *related][:3],
                }

    if object_id:
        for row in feedback_rows:
            if row.get("bearing_id") == object_id or row.get("feedback_id") == object_id:
                best = candidate(row.get("feedback_id", ""), row.get("bearing_id", ""), row.get("bearing_id", ""), 0.9, f"provided object id matched {object_id}")
                return {
                    "mode": "provided_uid",
                    "selected_case_id": best["case_id"],
                    "selected_object_id": best["object_id"],
                    "selected_object_name": best["object_name"],
                    "confidence": 0.9,
                    "match_reason": best["reason"],
                    "match_warning": None,
                    "candidate_topk": topk_candidates([best]),
                }

    candidates: list[dict[str, Any]] = []
    for row in feedback_rows:
        score = 0.0
        reasons: list[str] = []
        if fault_type and row.get("fault_type") == fault_type:
            score += 0.60
            reasons.append(f"fault_type={fault_type}")
        if fault_position and row.get("fault_position") == fault_position:
            score += 0.30
            reasons.append(f"fault_position={fault_position}")
        if severity and row.get("fault_severity") == severity:
            score += 0.10
            reasons.append(f"fault_severity={severity}")
        if score > 0:
            candidates.append(candidate(row.get("feedback_id", ""), row.get("bearing_id", ""), row.get("bearing_id", ""), score, "; ".join(reasons)))

    if candidates:
        topk = topk_candidates(candidates)
        best = topk[0]
        return {
            "mode": "inferred_from_fault_type_and_position",
            "selected_case_id": best["case_id"],
            "selected_object_id": best["object_id"],
            "selected_object_name": best["object_name"],
            "confidence": round(min(0.75, max(0.6, float(best["score"]))), 6),
            "match_reason": best["reason"],
            "match_warning": None,
            "candidate_topk": topk,
        }

    fallback = next((row for row in feedback_rows if row.get("feedback_id") == "BFB-01676"), feedback_rows[0] if feedback_rows else {})
    return {
        "mode": "fallback",
        "selected_case_id": fallback.get("feedback_id", "BFB-01676"),
        "selected_object_id": fallback.get("bearing_id", ""),
        "selected_object_name": fallback.get("bearing_id", ""),
        "confidence": 0.5,
        "match_reason": "fallback to default bearing demonstration case",
        "match_warning": "未找到可由上游信息匹配的轴承案例，当前采用兜底本地案例用于流程跑通。",
        "candidate_topk": [
            candidate(row.get("feedback_id", ""), row.get("bearing_id", ""), row.get("bearing_id", ""), 0.0, "fallback candidate")
            for row in feedback_rows[:3]
        ],
    }


def resolve_target_case(domain: str, normalized_upstream: dict[str, Any], project_root: Path) -> dict[str, Any]:
    if domain == "hydraulic":
        return resolve_hydraulic_target_case(normalized_upstream, project_root)
    if domain == "bearing":
        return resolve_bearing_target_case(normalized_upstream, project_root)
    raise ValueError(f"unsupported domain: {domain}")


def safe_filename(value: str) -> str:
    safe = re.sub(r"[^0-9A-Za-z._-]+", "_", value).strip("_")
    return safe or f"upstream_{datetime.now().strftime('%Y%m%d%H%M%S')}"


def save_runtime_input(
    domain: str,
    upstream_payload: dict[str, Any],
    normalized_upstream: dict[str, Any],
    target_resolution: dict[str, Any],
    project_root: Path,
) -> Path:
    upstream_case_id = normalize_text(normalized_upstream.get("upstream_case_id")) or f"UPSTREAM-{datetime.now().strftime('%Y%m%d%H%M%S')}"
    output_dir = project_root / "outputs" / domain / "runtime_inputs"
    output_dir.mkdir(parents=True, exist_ok=True)
    path = output_dir / f"{safe_filename(upstream_case_id)}.json"
    content = {
        "input_mode": "upstream_json",
        "upstream_payload": upstream_payload,
        "normalized_upstream": normalized_upstream,
        "target_resolution": target_resolution,
        "selected_case_id": target_resolution.get("selected_case_id"),
        "created_time": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
    }
    path.write_text(json.dumps(content, ensure_ascii=False, indent=2), encoding="utf-8")
    return path
