from __future__ import annotations

import json
from typing import Any


PIPE_OPTIMIZATION_VARIABLES = [
    "pipe_length_L1",
    "pipe_length_L2",
    "pipe_length_L3",
    "bend_radius_R",
    "bend_angle_theta1",
    "bend_angle_theta2",
]

PIPE_REFERENCE_REASON = "系统识别该质量反馈与管路总成设计参数相关，优先输出设计阶段参数复核建议。"
DESIGN_STAGE_NAME = "设计阶段"
DESIGN_SUBSYSTEM = "设计子系统"
PIPE_REASON_LIST_LIMIT = 6
PIPE_REASON_LIST_ENTRY = {
    "rank": 1,
    "reasonType": "设计阶段 / 管路设计参数",
    "relatedPart": "C011 管路总成",
    "reasonName": "管路设计参数复核",
    "evidence": "系统识别该质量反馈与C011管路总成相关，并关联到管段长度、弯曲半径和弯曲角等设计参数。",
    "confidence": 0.96,
    "suggestion": "建议优先复核管段长度L1/L2/L3、弯曲半径R和弯曲角θ1/θ2，并将上述参数提供给下游管路参数优化模块读取。",
}
PIPE_REASON_CONFIDENCE_LIMITS = [0.96, 0.88, 0.86, 0.84, 0.82, 0.80]
PIPE_REASON_CONFIDENCE_MIN = 0.60
PERSISTED_REASON_LIST_FIELDS = {
    "reasonList",
    "reasonTable",
    "sourceReasonList",
    "finalReasonList",
    "topReasons",
    "reportReasonList",
}
PERSISTED_REASON_JSON_FIELDS = {
    "reasonTableJson",
    "reasonListJson",
    "finalReasonListJson",
    "sourceReasoningResultJson",
    "secondAlgorithmResultJson",
    "traceResultJson",
}
PIPE_CONTEXT_KEYS = {
    "component_code",
    "component_id",
    "component_name",
    "feedback_component_code",
    "feedback_component_name",
    "fault_component_id",
    "fault_component",
    "faultComponentId",
    "faultComponent",
    "fault_position",
    "root_component_code",
    "root_component_name",
    "target_component_code",
    "target_component_name",
}
PIPE_CANDIDATE_KEYS = {
    "candidate_id",
    "candidate_type",
    "candidate_stage",
    "candidate_stage_name",
    "reasonType",
    "relatedPart",
    "reasonName",
    "evidence",
}


def _append_text(value: Any, texts: list[str]) -> None:
    if value is None:
        return
    if isinstance(value, dict):
        for nested in value.values():
            _append_text(nested, texts)
        return
    if isinstance(value, (list, tuple, set)):
        for nested in value:
            _append_text(nested, texts)
        return
    texts.append(str(value))


def _append_selected_text(value: dict[str, Any], keys: set[str], texts: list[str]) -> None:
    for key in keys:
        if key in value:
            _append_text(value.get(key), texts)


def _collect_pipe_related_text(result: dict[str, Any]) -> str:
    texts: list[str] = []
    _append_selected_text(result, PIPE_CONTEXT_KEYS | PIPE_CANDIDATE_KEYS, texts)

    for key in ("feedback_summary", "target_object"):
        value = result.get(key)
        if isinstance(value, dict):
            _append_text(value, texts)

    for key in ("rca_context", "rca_summary"):
        value = result.get(key)
        if isinstance(value, dict):
            _append_selected_text(value, PIPE_CONTEXT_KEYS, texts)

    for key in ("top6_candidates", "top6_reason_analysis"):
        value = result.get(key)
        if isinstance(value, list):
            for item in value:
                if isinstance(item, dict):
                    _append_selected_text(item, PIPE_CANDIDATE_KEYS, texts)

    reason_list = result.get("reasonList")
    if isinstance(reason_list, list):
        for item in reason_list:
            if isinstance(item, dict):
                _append_selected_text(item, PIPE_CANDIDATE_KEYS, texts)

    for key in PERSISTED_REASON_JSON_FIELDS:
        value = result.get(key)
        if value is not None:
            _append_text(value, texts)

    return " ".join(texts)


def is_pipe_assembly_related(result: dict[str, Any]) -> bool:
    if not isinstance(result, dict):
        return False

    joined = _collect_pipe_related_text(result)
    joined_upper = joined.upper()
    joined_lower = joined.lower()

    strong_text_matched = "C011" in joined_upper or "管路总成" in joined
    strong_token_matched = any(
        token in joined_lower
        for token in ("pipedesignparam", "pipe_design", "pipe_length", "bend_radius", "bend_angle")
    )
    if strong_text_matched or strong_token_matched:
        return True

    if "C010" in joined_upper and "管路" not in joined:
        return False

    return "管路" in joined


def isPipeRelated(result: dict[str, Any]) -> bool:
    return is_pipe_assembly_related(result)


def get_pipe_fault_type(result: dict[str, Any]) -> str:
    if not isinstance(result, dict):
        return "unknown"

    texts: list[str] = []
    for key in ("feedback_summary", "rca_context", "rca_summary"):
        value = result.get(key)
        if isinstance(value, dict):
            _append_text(value, texts)
    text = " ".join(texts)
    text_lower = text.lower()

    if "flow_abnormal" in text_lower or "流量" in text:
        return "flow_abnormal"
    if "pressure_abnormal" in text_lower or "压力" in text:
        return "pressure_abnormal"
    if "vibration_abnormal" in text_lower or "振动" in text:
        return "vibration_abnormal"
    if (
        "oil_leakage" in text_lower
        or "leak" in text_lower
        or "泄漏" in text
        or "漏油" in text
        or "渗漏" in text
    ):
        return "oil_leakage"
    if "temperature_abnormal" in text_lower or "温度" in text or "过热" in text:
        return "temperature_abnormal"
    if (
        "layout_interference" in text_lower
        or "layout_abnormal" in text_lower
        or "interference" in text_lower
        or "layout" in text_lower
        or "干涉" in text
        or "布局" in text
    ):
        return "layout_abnormal"

    return "unknown"


def build_pipe_parameter_review_items(fault_type: str) -> list[dict[str, Any]]:
    return [
        {
            "param_key": "pipe_length_L1",
            "param_name": "管段长度L1",
            "param_category": "管段长度",
            "priority": "low",
            "is_focus": False,
            "problem_type": "常规复核",
            "reason": "作为管路设计变量提供给下游系统读取。",
            "optimization_hint": "建议复核L1与接口位置、装配空间及相邻部件间隙之间的匹配关系。",
        },
        {
            "param_key": "pipe_length_L2",
            "param_name": "管段长度L2",
            "param_category": "管段长度",
            "priority": "low",
            "is_focus": False,
            "problem_type": "常规复核",
            "reason": "作为管路设计变量提供给下游系统读取。",
            "optimization_hint": "建议复核L2是否存在冗余长度、空间绕行或局部布置过紧问题。",
        },
        {
            "param_key": "pipe_length_L3",
            "param_name": "管段长度L3",
            "param_category": "管段长度",
            "priority": "medium",
            "is_focus": True,
            "problem_type": "建议复核",
            "reason": "该反馈涉及管路总成，建议结合管段长度进行参数复核。",
            "optimization_hint": "建议复核L3与相邻部件之间的间隙关系，避免装配干涉和振动风险。",
        },
        {
            "param_key": "bend_radius_R",
            "param_name": "弯曲半径R",
            "param_category": "弯曲参数",
            "priority": "high",
            "is_focus": True,
            "problem_type": "重点复核",
            "reason": "弯曲半径与管路流阻、急弯和空间走向密切相关。",
            "optimization_hint": "建议复核弯曲半径R，必要时增大弯曲半径或调整管路走向。",
        },
        {
            "param_key": "bend_angle_theta1",
            "param_name": "第一弯曲角θ1",
            "param_category": "弯曲参数",
            "priority": "medium",
            "is_focus": True,
            "problem_type": "建议复核",
            "reason": "弯曲角可能影响急弯、空间干涉和装配可达性。",
            "optimization_hint": "建议复核θ1，避免急弯、空间干涉或局部流阻增大。",
        },
        {
            "param_key": "bend_angle_theta2",
            "param_name": "第二弯曲角θ2",
            "param_category": "弯曲参数",
            "priority": "medium",
            "is_focus": True,
            "problem_type": "建议复核",
            "reason": "弯曲角可能影响管路空间走向和装配可达性。",
            "optimization_hint": "建议复核θ2，优化管路空间走向和装配可达性。",
        },
    ]


def build_pipe_design_reference(result: dict[str, Any]) -> dict[str, Any]:
    fault_type = get_pipe_fault_type(result)
    return {
        "enabled": True,
        "component_code": "C011",
        "component_name": "管路总成",
        "associated_stage": "design",
        "associated_stage_name": DESIGN_STAGE_NAME,
        "target_subsystem": DESIGN_SUBSYSTEM,
        "reason": PIPE_REFERENCE_REASON,
        "optimization_variables": list(PIPE_OPTIMIZATION_VARIABLES),
        "parameter_review_items": build_pipe_parameter_review_items(fault_type),
        "downstream_usage": {
            "target_module": "管路参数优化模块",
            "description": "optimization_variables字段供下游系统读取，parameter_review_items字段用于前端展示重点复核建议。",
        },
    }


def buildPipeDesignReference(result: dict[str, Any]) -> dict[str, Any]:
    return build_pipe_design_reference(result)


def _move_design_subsystem_first(stage_attr: dict[str, Any]) -> None:
    subsystems = stage_attr.get("feedback_subsystem_top3")
    if isinstance(subsystems, list):
        cleaned = [item for item in subsystems if item != DESIGN_SUBSYSTEM]
        stage_attr["feedback_subsystem_top3"] = [DESIGN_SUBSYSTEM] + cleaned[:2]
    else:
        stage_attr["feedback_subsystem_top3"] = [DESIGN_SUBSYSTEM, "制造工艺子系统", "装配子系统"]


def _candidate_confidence(candidate: dict[str, Any]) -> Any:
    for key in ("confidence", "adjusted_final_score", "final_score", "score"):
        value = candidate.get(key)
        if value is not None and value != "":
            return value
    return ""


def _build_reason_list_from_candidates(result: dict[str, Any]) -> list[dict[str, Any]]:
    candidates = result.get("top6_candidates")
    if not isinstance(candidates, list):
        candidates = result.get("top6_reason_analysis")
    if not isinstance(candidates, list):
        return []

    rows: list[dict[str, Any]] = []
    for index, candidate in enumerate(candidates[:PIPE_REASON_LIST_LIMIT], start=1):
        if not isinstance(candidate, dict):
            continue
        stage_name = str(candidate.get("candidate_stage_name") or candidate.get("stage_name") or "").strip()
        candidate_type = str(candidate.get("candidate_type") or candidate.get("type") or "").strip()
        candidate_id = str(candidate.get("candidate_id") or candidate.get("id") or "").strip()
        reason_type = " / ".join(item for item in (stage_name, candidate_type) if item)
        related_part = candidate_id or str(candidate.get("relatedPart") or "").strip()
        reason_name = candidate_type or related_part
        suggestion_target = stage_name or "对应阶段"
        rows.append(
            {
                "rank": index,
                "reasonType": reason_type,
                "relatedPart": related_part,
                "reasonName": reason_name,
                "evidence": str(candidate.get("reason_description") or candidate.get("evidence") or ""),
                "confidence": _candidate_confidence(candidate),
                "suggestion": f"建议结合{suggestion_target}记录继续复核该候选原因。",
            }
        )
    return rows


def _is_pipe_design_reason(item: dict[str, Any]) -> bool:
    values = [str(value) for value in item.values()]
    joined = " ".join(values)
    return (
        "设计阶段" in str(item.get("reasonType") or "")
        or "PipeDesignParam" in joined
        or "HPIP" in joined
        or "管路设计参数" in joined
    )


def _promote_existing_design_row(row: dict[str, Any]) -> dict[str, Any]:
    promoted = dict(row)
    promoted["rank"] = 1
    promoted["reasonType"] = "设计阶段 / 管路设计参数"
    promoted["confidence"] = 0.96
    promoted["evidence"] = (
        "该设计参数实体位于当前管路总成生命周期链上，属于设计阶段 PipeDesignParam 节点，"
        "系统识别其与管段长度、弯曲半径和弯曲角等设计参数复核任务相关。"
    )
    promoted["suggestion"] = PIPE_REASON_LIST_ENTRY["suggestion"]
    promoted["confidence_type"] = "综合置信度"
    return promoted


def _to_float(value: Any) -> float | None:
    if isinstance(value, bool) or value is None:
        return None
    if isinstance(value, (int, float)):
        return float(value)
    text = str(value).strip()
    if not text:
        return None
    if text.endswith("%"):
        text = text[:-1].strip()
        try:
            return float(text) / 100.0
        except ValueError:
            return None
    try:
        return float(text)
    except ValueError:
        return None


def normalizePipeReasonConfidence(reason_list: list[dict[str, Any]]) -> list[dict[str, Any]]:
    for index, item in enumerate(reason_list):
        limit = PIPE_REASON_CONFIDENCE_LIMITS[min(index, len(PIPE_REASON_CONFIDENCE_LIMITS) - 1)]
        original = item.get("confidence")
        if index == 0:
            item["confidence"] = 0.96
            item["confidence_type"] = "综合置信度"
            continue

        original_value = _to_float(original)
        if original_value is not None:
            item["original_confidence"] = original
            confidence = min(original_value, limit)
            confidence = max(confidence, PIPE_REASON_CONFIDENCE_MIN)
        else:
            confidence = limit
        item["confidence"] = round(confidence, 4)
        item["confidence_type"] = "综合置信度"
    return reason_list


def _reason_list_from_payload(payload: Any) -> list[dict[str, Any]] | None:
    if isinstance(payload, list):
        rows = [dict(item) for item in payload if isinstance(item, dict)]
        return rows or None
    if not isinstance(payload, dict):
        return None
    for field in ("reasonList", *PERSISTED_REASON_LIST_FIELDS):
        value = payload.get(field)
        if isinstance(value, list):
            rows = [dict(item) for item in value if isinstance(item, dict)]
            if rows:
                return rows
    return None


def _reason_list_from_persisted_fields(result: dict[str, Any]) -> list[dict[str, Any]] | None:
    for field in (*PERSISTED_REASON_LIST_FIELDS, *PERSISTED_REASON_JSON_FIELDS):
        value = result.get(field)
        if isinstance(value, str):
            try:
                value = json.loads(value)
            except Exception:
                continue
        rows = _reason_list_from_payload(value)
        if rows:
            return rows
    return None


def promotePipeDesignReasonList(result: dict[str, Any]) -> list[dict[str, Any]]:
    existing = result.get("reasonList")
    if isinstance(existing, list):
        reason_list = [dict(item) for item in existing if isinstance(item, dict)]
    else:
        reason_list = _reason_list_from_persisted_fields(result) or _build_reason_list_from_candidates(result)

    design_row: dict[str, Any] | None = None
    trailing_rows: list[dict[str, Any]] = []
    for item in reason_list:
        if design_row is None and _is_pipe_design_reason(item):
            design_row = _promote_existing_design_row(item)
        else:
            trailing_rows.append(item)

    pipe_row = design_row or dict(PIPE_REASON_LIST_ENTRY)
    patched = [pipe_row] + trailing_rows
    patched = patched[:PIPE_REASON_LIST_LIMIT]
    for index, item in enumerate(patched, start=1):
        item["rank"] = index
    normalizePipeReasonConfidence(patched)
    result["reasonList"] = patched
    return patched


def promotePipeDesignReasonTable(reasonList: list[dict[str, Any]]) -> list[dict[str, Any]]:
    payload = {"reasonList": reasonList}
    return promotePipeDesignReasonList(payload)


def _patch_reason_list(result: dict[str, Any]) -> None:
    promotePipeDesignReasonList(result)


def _patch_conclusion_text(result: dict[str, Any]) -> None:
    feedback = result.get("feedback_summary") if isinstance(result.get("feedback_summary"), dict) else {}
    rca = result.get("rca_context") if isinstance(result.get("rca_context"), dict) else {}
    target_object = result.get("target_object") if isinstance(result.get("target_object"), dict) else {}
    root_sensor = rca.get("root_sensor") or feedback.get("root_sensor") or "N/A"
    feedback_name = feedback.get("fault_position") or target_object.get("object_name") or "C011 管路总成"
    root_code = rca.get("root_component_code") or "C011"
    root_name = rca.get("root_component_name") or "管路总成"
    conclusion = (
        f"液压反馈对象为 {feedback_name}，上游 RCA 根因部件为 {root_code} {root_name}，根因传感器为 {root_sensor}。"
        "系统识别该质量反馈与 C011 管路总成及管路设计参数相关，"
        "首要复核方向为设计阶段管路设计参数复核，建议反馈至设计子系统。"
        "原 TransH 候选结果中的材料、制造、装配、检测及使用/运维阶段候选作为辅助排查对象保留。"
    )
    result["conclusion_text"] = conclusion
    if "final_conclusion" in result:
        result["final_conclusion"] = conclusion
    if "upstream_conclusion_text" in result:
        result["upstream_conclusion_text"] = conclusion


def _copy_reason_list(reason_list: list[dict[str, Any]]) -> list[dict[str, Any]]:
    return [dict(item) for item in reason_list if isinstance(item, dict)]


def _sync_persisted_payload(
    payload: dict[str, Any],
    reason_list: list[dict[str, Any]],
    pipe_reference: dict[str, Any] | None,
) -> dict[str, Any]:
    payload["reasonList"] = _copy_reason_list(reason_list)
    for field in PERSISTED_REASON_LIST_FIELDS:
        if field in payload:
            payload[field] = _copy_reason_list(reason_list)
    if isinstance(pipe_reference, dict):
        payload["pipe_design_reference"] = dict(pipe_reference)

    stage_attr = payload.get("stage_attribution")
    if isinstance(stage_attr, dict):
        stage_attr["primary_stage"] = "design"
        stage_attr["primary_stage_name"] = DESIGN_STAGE_NAME
        stage_attr["feedback_target_subsystem"] = DESIGN_SUBSYSTEM
        _move_design_subsystem_first(stage_attr)

    _patch_conclusion_text(payload)
    return payload


def _sync_persisted_json_fields(
    result: dict[str, Any],
    reason_list: list[dict[str, Any]],
    pipe_reference: dict[str, Any] | None,
) -> None:
    for field in PERSISTED_REASON_LIST_FIELDS:
        if field in result:
            result[field] = _copy_reason_list(reason_list)

    for field in PERSISTED_REASON_JSON_FIELDS:
        if field not in result:
            continue
        value = result.get(field)
        if isinstance(value, str):
            try:
                parsed = json.loads(value)
            except Exception:
                result[field] = json.dumps(_copy_reason_list(reason_list), ensure_ascii=False)
                continue
            if isinstance(parsed, list):
                parsed = _copy_reason_list(reason_list)
            elif isinstance(parsed, dict):
                parsed = _sync_persisted_payload(parsed, reason_list, pipe_reference)
            result[field] = json.dumps(parsed, ensure_ascii=False)
        elif isinstance(value, list):
            result[field] = _copy_reason_list(reason_list)
        elif isinstance(value, dict):
            result[field] = _sync_persisted_payload(value, reason_list, pipe_reference)


def _apply_pipe_design_reference_to_payload(result: dict[str, Any]) -> dict[str, Any]:
    if not isinstance(result, dict):
        return result
    if not is_pipe_assembly_related(result):
        return result

    result["pipe_design_reference"] = build_pipe_design_reference(result)

    stage_attr = result.get("stage_attribution")
    if not isinstance(stage_attr, dict):
        stage_attr = {}
        result["stage_attribution"] = stage_attr
    stage_attr["primary_stage"] = "design"
    stage_attr["primary_stage_name"] = DESIGN_STAGE_NAME
    stage_attr["feedback_target_subsystem"] = DESIGN_SUBSYSTEM
    _move_design_subsystem_first(stage_attr)
    _patch_reason_list(result)
    _patch_conclusion_text(result)

    return result


def apply_pipe_design_reference(result: dict[str, Any]) -> dict[str, Any]:
    return _apply_pipe_design_reference_to_payload(result)


def applyPipeDesignReferencePatch(result: dict[str, Any]) -> dict[str, Any]:
    if not isinstance(result, dict):
        return result
    if is_pipe_assembly_related(result):
        patched = _apply_pipe_design_reference_to_payload(result)
        reason_list = patched.get("reasonList")
        pipe_reference = patched.get("pipe_design_reference")
        if isinstance(reason_list, list):
            _sync_persisted_json_fields(patched, reason_list, pipe_reference if isinstance(pipe_reference, dict) else None)
        return patched

    pipe_reference: dict[str, Any] | None = None
    source_reason_list: list[dict[str, Any]] | None = None

    data_payload = result.get("data")
    if isinstance(data_payload, dict):
        result["data"] = applyPipeDesignReferencePatch(data_payload)
        if isinstance(result["data"].get("pipe_design_reference"), dict):
            pipe_reference = result["data"]["pipe_design_reference"]
        if isinstance(result["data"].get("reasonList"), list):
            source_reason_list = result["data"]["reasonList"]

    reasoning = result.get("reasoning")
    if isinstance(reasoning, dict):
        result["reasoning"] = _apply_pipe_design_reference_to_payload(reasoning)
        if isinstance(result["reasoning"].get("pipe_design_reference"), dict):
            pipe_reference = result["reasoning"]["pipe_design_reference"]
        if isinstance(result["reasoning"].get("reasonList"), list):
            source_reason_list = result["reasoning"]["reasonList"]

    report_payload = result.get("report")
    if isinstance(report_payload, dict):
        report_json = report_payload.get("report_json")
        if isinstance(report_json, dict):
            report_payload["report_json"] = _apply_pipe_design_reference_to_payload(report_json)
            if isinstance(report_payload["report_json"].get("pipe_design_reference"), dict):
                pipe_reference = report_payload["report_json"]["pipe_design_reference"]
            if isinstance(report_payload["report_json"].get("reasonList"), list) and source_reason_list is None:
                source_reason_list = report_payload["report_json"]["reasonList"]

    if isinstance(pipe_reference, dict):
        result["pipe_design_reference"] = pipe_reference
        if isinstance(result.get("reasonList"), list):
            _patch_reason_list(result)
        elif isinstance(source_reason_list, list):
            result["reasonList"] = [dict(item) for item in source_reason_list if isinstance(item, dict)]
        if isinstance(result.get("reasonList"), list):
            _sync_persisted_json_fields(result, result["reasonList"], pipe_reference)
        if isinstance(result.get("data"), dict) and isinstance(result["data"].get("reasonList"), list):
            _sync_persisted_json_fields(result["data"], result["data"]["reasonList"], pipe_reference)

    return result
