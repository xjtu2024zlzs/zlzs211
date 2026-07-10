import csv
import json
import re
import sys
from pathlib import Path
from typing import Any, Literal, Optional

PROJECT_ROOT = Path(__file__).resolve().parents[1]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from fastapi import FastAPI, HTTPException, Query
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field

from services.pipe_design_reference import applyPipeDesignReferencePatch
from services.upstream_adapter import normalize_upstream_payload, resolve_target_case, save_runtime_input
from services.trace_pipeline import run_trace_pipeline as execute_trace_pipeline


Domain = Literal["bearing", "hydraulic"]
ModelVariant = Literal["default", "eval"]
GraphType = Literal["original", "enhanced", "echarts_original", "echarts_enhanced"]


app = FastAPI(title="TransH Trace API", version="1.0.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)




@app.exception_handler(RequestValidationError)
async def validation_exception_handler(_request: Any, exc: RequestValidationError) -> JSONResponse:
    return JSONResponse(status_code=400, content={"detail": exc.errors()})


class TraceRunRequest(BaseModel):
    domain: Domain
    case_id: str = Field(min_length=1)
    model_variant: ModelVariant = "default"
    generate_graph: bool = True
    generate_report: bool = True
    generate_echarts: bool = True
    generate_docx: bool = True

    # 接收 Java 后端传来的第一部分算法结果
    first_algorithm_result: Optional[Any] = None
    first_algorithm_result_raw: Optional[str] = None
    first_algorithm_status: Optional[int] = None
    first_algorithm_trace_id: Optional[int] = None
    first_algorithm_trace_no: Optional[str] = None


class TraceRunCaseRequest(BaseModel):
    domain: Domain
    case_id: str = Field(min_length=1)
    model_variant: ModelVariant = "default"
    generate_graph: bool = True
    generate_report: bool = True
    generate_echarts: bool = True
    generate_docx: bool = True


class UpstreamTraceRequest(BaseModel):
    domain: Domain
    model_variant: ModelVariant = "default"
    generate_graph: bool = True
    generate_report: bool = True
    generate_echarts: bool = True
    generate_docx: bool = True
    upstream_payload: dict[str, Any]


def normalize_case_id_alias(case_id: str) -> str:
    if case_id.startswith("FB-BRG-"):
        suffix = case_id.removeprefix("FB-BRG-")
        if suffix.isdigit():
            return f"BFB-{int(suffix):05d}"
    if case_id.startswith("FB-HYD-"):
        suffix = case_id.removeprefix("FB-HYD-")
        if suffix.isdigit():
            return f"HFB-{int(suffix):05d}"
    return case_id


def load_json(path: Path) -> dict[str, Any]:
    if not path.exists():
        raise HTTPException(status_code=404, detail=f"File not found: {path}")
    return json.loads(path.read_text(encoding="utf-8"))


def write_json_if_possible(path_value: Any, payload: dict[str, Any]) -> None:
    if not path_value or not isinstance(payload, dict):
        return
    path = Path(str(path_value))
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")


def write_text_if_possible(path_value: Any, payload: str) -> None:
    if not path_value or not isinstance(payload, str):
        return
    path = Path(str(path_value))
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(payload, encoding="utf-8")


def refresh_report_files_if_pipe(domain: str, case_id: str, report_json: dict[str, Any]) -> dict[str, Any]:
    report_json = applyPipeDesignReferencePatch(report_json)
    if not isinstance(report_json.get("pipe_design_reference"), dict):
        return report_json

    paths = artifact_paths(domain, case_id)
    write_json_if_possible(paths.get("report_json"), report_json)
    try:
        from scripts.generate_report import render_markdown, write_docx

        markdown = render_markdown(report_json)
        write_text_if_possible(paths.get("report_md"), markdown)
        write_docx(report_json, Path(paths["report_docx"]))
    except Exception:
        pass
    return report_json


def sync_pipe_design_reference_payloads(
    result: dict[str, Any],
    reasoning: dict[str, Any],
) -> dict[str, Any]:
    reasoning = applyPipeDesignReferencePatch(reasoning)
    result["reasoning"] = reasoning

    report_payload = result.get("report")
    if isinstance(report_payload, dict):
        report_json = report_payload.get("report_json")
        if isinstance(report_json, dict):
            report_json = applyPipeDesignReferencePatch(report_json)
            pipe_reference = reasoning.get("pipe_design_reference")
            if isinstance(pipe_reference, dict):
                report_json["pipe_design_reference"] = pipe_reference
                if isinstance(reasoning.get("reasonList"), list):
                    report_json["reasonList"] = reasoning["reasonList"]

                reasoning_stage = reasoning.get("stage_attribution")
                report_stage = report_json.get("stage_attribution")
                if isinstance(reasoning_stage, dict) and isinstance(report_stage, dict):
                    for key in (
                        "primary_stage",
                        "primary_stage_name",
                        "feedback_target_subsystem",
                        "feedback_subsystem_top3",
                    ):
                        if key in reasoning_stage:
                            report_stage[key] = reasoning_stage[key]

                subsystem_feedback = report_json.get("subsystem_feedback")
                if isinstance(subsystem_feedback, dict):
                    subsystem_top3 = subsystem_feedback.get("feedback_subsystem_top3", [])
                    if not isinstance(subsystem_top3, list):
                        subsystem_top3 = []
                    subsystem_feedback["primary_stage"] = "design"
                    subsystem_feedback["target_subsystem"] = "设计子系统"
                    subsystem_feedback["primary_feedback_subsystem"] = "设计子系统"
                    subsystem_feedback["feedback_subsystem_top3"] = ["设计子系统"] + [
                        item
                        for item in subsystem_top3
                        if item != "设计子系统"
                    ][:2]
                try:
                    from scripts.generate_report import render_markdown

                    report_payload["report_markdown"] = render_markdown(report_json)
                except Exception:
                    pass
            report_payload["report_json"] = report_json

    artifacts = result.get("artifacts")
    if isinstance(artifacts, dict):
        reasoning_path_value = reasoning.get("_output_path") or artifacts.get("reasoning_json")
        write_json_if_possible(reasoning_path_value, reasoning)

        report_json = report_payload.get("report_json") if isinstance(report_payload, dict) else None
        report_path_value = artifacts.get("trace_report_json") or artifacts.get("report_json")
        if isinstance(report_json, dict):
            write_json_if_possible(report_path_value, report_json)
            report_markdown = report_payload.get("report_markdown") if isinstance(report_payload, dict) else None
            write_text_if_possible(artifacts.get("trace_report_md") or artifacts.get("report_md"), report_markdown)
            if isinstance(report_json.get("pipe_design_reference"), dict):
                try:
                    from scripts.generate_report import write_docx

                    docx_path_value = artifacts.get("trace_report_docx") or artifacts.get("report_docx")
                    if docx_path_value:
                        docx_ok = write_docx(report_json, Path(str(docx_path_value)))
                        if isinstance(report_payload, dict):
                            report_payload["docx_generated"] = bool(docx_ok)
                except Exception:
                    pass

    applyPipeDesignReferencePatch(result)
    return reasoning


def load_text(path: Path) -> str:
    if not path.exists():
        raise HTTPException(status_code=404, detail=f"File not found: {path}")
    return path.read_text(encoding="utf-8")


def report_dir(domain: str) -> Path:
    return PROJECT_ROOT / "outputs" / domain / "reports"


def graph_dir(domain: str) -> Path:
    return PROJECT_ROOT / "outputs" / domain / "graphs"


def reasoning_path(domain: str, case_id: str) -> Path:
    return report_dir(domain) / f"reasoning_{case_id}.json"


def artifact_paths(domain: str, case_id: str) -> dict[str, str]:
    reports = report_dir(domain)
    graphs = graph_dir(domain)
    return {
        "reasoning_json": str(reports / f"reasoning_{case_id}.json"),
        "report_json": str(reports / f"trace_report_{case_id}.json"),
        "report_md": str(reports / f"trace_report_{case_id}.md"),
        "report_docx": str(reports / f"trace_report_{case_id}.docx"),
        "original_graph_json": str(graphs / f"original_graph_{case_id}.json"),
        "fault_enhanced_graph_json": str(graphs / f"fault_enhanced_graph_{case_id}.json"),
        "echarts_original_graph_json": str(graphs / f"echarts_original_graph_{case_id}.json"),
        "echarts_fault_enhanced_graph_json": str(graphs / f"echarts_fault_enhanced_graph_{case_id}.json"),
    }

def as_dict_payload(value: Any) -> dict[str, Any]:
    if isinstance(value, dict):
        return value

    if isinstance(value, str) and value.strip():
        try:
            obj = json.loads(value)
            if isinstance(obj, dict):
                return obj
        except Exception:
            return {}

    return {}


def normalize_component_code(code: Any) -> str:
    text = str(code or "").strip().upper()
    if not text:
        return ""

    # 兼容 C7 / C07 / C007
    m = re.fullmatch(r"C0*(\d+)", text)
    if m:
        return f"C{int(m.group(1)):03d}"

    return text


def extract_root_component_from_first_algorithm(first_algorithm_result: Any) -> dict[str, str]:
    first = as_dict_payload(first_algorithm_result)
    raw = as_dict_payload(first.get("raw"))

    root_code = (
        first.get("root_component_code")
        or first.get("faultComponentId")
        or first.get("component_id")
        or first.get("component_code")
        or first.get("fault_component_id")
        or raw.get("root_component_code")
        or raw.get("faultComponentId")
        or raw.get("component_id")
        or raw.get("component_code")
        or raw.get("fault_component_id")
        or ""
    )

    root_name = (
        first.get("root_component_name")
        or first.get("faultComponent")
        or first.get("component_name")
        or first.get("fault_component")
        or raw.get("root_component_name")
        or raw.get("faultComponent")
        or raw.get("component_name")
        or raw.get("fault_component")
        or ""
    )

    root_code = normalize_component_code(root_code)
    root_name = str(root_name or "").strip()

    if not root_code:
        if "液压泵" in root_name:
            root_code = "C001"
        elif "方向控制阀" in root_name or "方向阀" in root_name:
            root_code = "C002"
        elif "作动筒" in root_name:
            root_code = "C003"
        elif "溢流阀" in root_name:
            root_code = "C004"
        elif "蓄能器" in root_name:
            root_code = "C005"
        elif "冷却器" in root_name:
            root_code = "C006"
        elif "过滤器" in root_name:
            root_code = "C007"
        elif "液压油箱" in root_name or "油箱" in root_name:
            root_code = "C008"
        elif "单向阀" in root_name:
            root_code = "C009"
        elif "节流阀" in root_name:
            root_code = "C010"
        elif "管路总成" in root_name or "管路" in root_name:
            root_code = "C011"
        elif "卸荷阀" in root_name:
            root_code = "C012"

    return {
        "root_component_code": root_code,
        "root_component_name": root_name,
    }


def read_csv_rows(path: Path) -> list[dict[str, str]]:
    if not path.exists():
        return []

    for enc in ("utf-8-sig", "utf-8", "gbk"):
        try:
            with path.open("r", encoding=enc, newline="") as f:
                return list(csv.DictReader(f))
        except UnicodeDecodeError:
            continue

    return []


def find_hydraulic_case_by_component_code(component_code: str) -> dict[str, str]:
    component_code = normalize_component_code(component_code)

    data_dir = PROJECT_ROOT / "data" / "hydraulic"
    component_csv = data_dir / "hydraulic_component.csv"
    feedback_csv = data_dir / "hydraulic_feedback.csv"

    component_rows = read_csv_rows(component_csv)
    feedback_rows = read_csv_rows(feedback_csv)

    matched_object_ids: set[str] = set()

    for row in component_rows:
        values = [str(v or "").strip() for v in row.values()]
        if component_code in values:
            for v in values:
                if re.fullmatch(r"HCU-\d{5}", v):
                    matched_object_ids.add(v)

    for row in feedback_rows:
        values = [str(v or "").strip() for v in row.values()]
        row_text = " ".join(values)

        hfb_match = re.search(r"HFB-\d{5}", row_text)
        if not hfb_match:
            continue

        case_id = hfb_match.group(0)

        if component_code in values:
            return {
                "case_id": case_id,
                "component_code": component_code,
                "component_object_id": "",
                "match_source": "feedback_csv_component_code",
            }

        for obj_id in matched_object_ids:
            if obj_id in values:
                return {
                    "case_id": case_id,
                    "component_code": component_code,
                    "component_object_id": obj_id,
                    "match_source": "component_csv_join_feedback_csv",
                }

    # 手动兜底：只写你确认过的映射
    fallback = {
        "C006": "HFB-01054",  # 冷却器
        "C007": "HFB-02227",  # 过滤器，你之前测试成功过
    }

    if component_code in fallback:
        return {
            "case_id": fallback[component_code],
            "component_code": component_code,
            "component_object_id": "",
            "match_source": "manual_fallback_mapping",
        }

    return {
        "case_id": "",
        "component_code": component_code,
        "component_object_id": "",
        "match_source": "not_found",
    }
# ==========================================================
# 新增：第一部分算法结果解析
# ==========================================================
def parse_first_algorithm_result(value: Any) -> dict[str, Any]:
    """
    兼容两种输入：
    1. Java 传来的 JSON 字符串；
    2. Java 传来的长文本，例如：
       故障部件编号：C010
       故障定位部件：节流阀
       部件置信度：0.4192
       触发传感器：FS1
    """
    if value is None:
        return {}

    if isinstance(value, dict):
        return normalize_first_algorithm_dict(value)

    if not isinstance(value, str):
        return {}

    text = value.strip()
    if not text:
        return {}

    # 情况1：JSON字符串
    try:
        obj = json.loads(text)
        if isinstance(obj, dict):
            return normalize_first_algorithm_dict(obj)
    except Exception:
        pass

    # 情况2：普通长文本
    return parse_first_algorithm_text(text)


def normalize_first_algorithm_dict(obj: dict[str, Any]) -> dict[str, Any]:
    data = obj.get("data", obj)

    business = (
        data.get("business_conclusion")
        or data.get("businessConclusion")
        or {}
    )

    algorithm = (
        data.get("algorithm_details")
        or data.get("algorithmDetails")
        or {}
    )

    return {
        "fault_component_id":
            data.get("faultComponentId")
            or data.get("fault_component_id")
            or business.get("fault_component_id")
            or business.get("faultComponentId"),

        "fault_component":
            data.get("faultComponent")
            or data.get("fault_component")
            or business.get("fault_component")
            or business.get("faultComponent"),

        "component_confidence":
            data.get("componentConfidence")
            or data.get("component_confidence")
            or business.get("component_confidence")
            or business.get("componentConfidence"),

        "trigger_sensor":
            data.get("triggerSensor")
            or data.get("trigger_sensor")
            or algorithm.get("trigger_sensor")
            or algorithm.get("triggerSensor"),

        "sensor_confidence":
            data.get("sensorConfidence")
            or data.get("sensor_confidence")
            or algorithm.get("sensor_confidence")
            or algorithm.get("sensorConfidence"),

        "evolution_chain":
            data.get("evolutionChain")
            or data.get("evolution_chain")
            or algorithm.get("evolution_chain")
            or algorithm.get("evolutionChain"),

        "algorithm_conclusion":
            data.get("conclusion")
            or data.get("algorithmConclusion")
            or data.get("algorithm_conclusion"),

        "component_diagnostics":
            data.get("componentDiagnostics")
            or data.get("component_diagnostics")
            or business.get("component_diagnostics")
            or business.get("componentDiagnostics")
            or [],

        "raw": obj
    }


def parse_first_algorithm_text(text: str) -> dict[str, Any]:
    def extract(label: str) -> Optional[str]:
        pattern = rf"{label}[:：]\s*([^\n\r]+)"
        match = re.search(pattern, text)
        return match.group(1).strip() if match else None

    component_diagnostics_text = extract("部件诊断信息")

    return {
        "fault_component_id": extract("故障部件编号"),
        "fault_component": extract("故障定位部件"),
        "component_confidence": extract("部件置信度"),
        "trigger_sensor": extract("触发传感器"),
        "sensor_confidence": extract("传感器置信度"),
        "evolution_chain": extract("故障传播链条") or extract("故障传播链路"),
        "algorithm_conclusion": extract("算法结论"),
        "component_diagnostics": parse_component_diagnostics_text(component_diagnostics_text),
        "raw_text": text
    }


def parse_component_diagnostics_text(text: Optional[str]) -> list[dict[str, Any]]:
    """
    兼容 Java Map.toString() 形式：
    [{component_id=C010, component_name=节流阀, fault_type=节流阀故障, severity=0.5352, is_fault=true, fault_level=中度故障}, ...]
    """
    if not text:
        return []

    text = text.strip()

    try:
        obj = json.loads(text)
        if isinstance(obj, list):
            return obj
    except Exception:
        pass

    blocks = re.findall(r"\{[^{}]*\}", text)
    rows: list[dict[str, Any]] = []

    for block in blocks:
        item: dict[str, Any] = {}
        content = block.strip("{}")

        for pair in content.split(","):
            if "=" not in pair:
                continue

            key, val = pair.split("=", 1)
            item[key.strip()] = val.strip()

        rows.append(item)

    return rows


SEVERITY_KEY_TO_COMPONENT = {
    "pump": ("C001", "\u6db2\u538b\u6cf5\u603b\u6210"),
    "valve": ("C002", "\u65b9\u5411\u63a7\u5236\u9600"),
    "direction_valve": ("C002", "\u65b9\u5411\u63a7\u5236\u9600"),
    "actuator": ("C003", "\u4f5c\u52a8\u7b52"),
    "accumulator": ("C005", "\u84c4\u80fd\u5668"),
    "cooler": ("C006", "\u51b7\u5374\u5668"),
    "filter": ("C007", "\u8fc7\u6ee4\u5668"),
    "check_valve": ("C009", "\u5355\u5411\u9600"),
    "one_way_valve": ("C009", "\u5355\u5411\u9600"),
    "throttle": ("C010", "\u8282\u6d41\u9600"),
    "throttle_valve": ("C010", "\u8282\u6d41\u9600"),
    "pipeline": ("C011", "\u7ba1\u8def\u603b\u6210"),
    "pipe": ("C011", "\u7ba1\u8def\u603b\u6210"),
}


def safe_float(value: Any, default: float = 0.0) -> float:
    try:
        if value is None or value == "":
            return default
        return float(value)
    except Exception:
        return default


def unwrap_algorithm_payload(source: dict[str, Any]) -> dict[str, Any]:
    if not isinstance(source, dict):
        return {}

    raw = (
        source.get("raw_payload")
        or source.get("raw")
        or source.get("first_algorithm_result")
        or source
    )
    if not isinstance(raw, dict):
        return {}

    data = raw.get("data", raw)
    return data if isinstance(data, dict) else raw


def infer_root_component_from_severity(payload: dict[str, Any]) -> tuple[Any, Any, Any]:
    business = payload.get("business_conclusion") or payload.get("businessConclusion") or {}
    severity_scores = (
        payload.get("severityScores")
        or payload.get("severity_scores")
        or business.get("severityScores")
        or business.get("severity_scores")
        or {}
    )

    if not isinstance(severity_scores, dict) or not severity_scores:
        return None, None, None

    valid_items: list[tuple[str, float]] = []
    for key, value in severity_scores.items():
        score = safe_float(value, 0.0)
        if score > 0:
            valid_items.append((str(key), score))

    if not valid_items:
        return None, None, None

    best_key, best_score = sorted(valid_items, key=lambda x: x[1], reverse=True)[0]
    component = SEVERITY_KEY_TO_COMPONENT.get(best_key)
    if not component:
        return None, None, best_score

    return component[0], component[1], best_score


def normalize_subtype_rows(rows: Any) -> list[dict[str, Any]]:
    if not isinstance(rows, list):
        return []

    result: list[dict[str, Any]] = []
    for item in rows:
        if not isinstance(item, dict):
            continue

        result.append({
            "component_id": item.get("component_id") or item.get("componentId") or "",
            "component_name": item.get("component_name") or item.get("componentName") or "",
            "sub_type_id": (
                item.get("sub_type_id")
                or item.get("subTypeId")
                or item.get("subtype_id")
                or ""
            ),
            "sub_type_name": (
                item.get("sub_type_name")
                or item.get("subTypeName")
                or item.get("subtype_name")
                or ""
            ),
            "fault_part": item.get("fault_part") or item.get("faultPart") or "",
            "probability": safe_float(item.get("probability"), 0.0),
        })

    return sorted(result, key=lambda x: x["probability"], reverse=True)


def build_root_component_display_top3(
    root_component_diagnosis: Any,
    root_subtypes: list[dict[str, Any]],
    root_component_code: Any,
    root_component_name: Any,
    global_component_top3: list[dict[str, Any]],
) -> list[dict[str, Any]]:
    rows: list[dict[str, Any]] = []
    root_code = str(root_component_code or "")
    root_name = str(root_component_name or "")

    if isinstance(root_component_diagnosis, dict):
        row = dict(root_component_diagnosis)
        row.setdefault("component_id", root_code)
        row.setdefault("component_name", root_name)
        row.setdefault("display_role", "\u0052\u0043\u0041\u6839\u56e0\u90e8\u4ef6\u8bca\u65ad")
        row.setdefault("display_source", "root_component_diagnosis")
        rows.append(row)

    for subtype in root_subtypes:
        if len(rows) >= 3:
            break
        if not isinstance(subtype, dict):
            continue

        sub_type_name = (
            subtype.get("sub_type_name")
            or subtype.get("subTypeName")
            or subtype.get("subtype_name")
            or ""
        )
        sub_type_id = (
            subtype.get("sub_type_id")
            or subtype.get("subTypeId")
            or subtype.get("subtype_id")
            or ""
        )
        fault_part = subtype.get("fault_part") or subtype.get("faultPart") or ""
        probability = safe_float(subtype.get("probability"), 0.0)

        rows.append({
            "component_id": root_code or subtype.get("component_id", ""),
            "component_name": root_name or subtype.get("component_name", ""),
            "fault_type": sub_type_name,
            "severity": probability,
            "is_fault": True,
            "fault_level": "\u6839\u56e0\u5b50\u7c7b\u578b",
            "sub_type_id": sub_type_id,
            "sub_type_name": sub_type_name,
            "fault_part": fault_part,
            "display_role": "\u0052\u0043\u0041\u6839\u56e0\u90e8\u4ef6\u5b50\u7c7b\u578b",
            "display_source": "root_component_subtype",
        })

    for item in global_component_top3:
        if len(rows) >= 3:
            break
        if not isinstance(item, dict):
            continue

        item_component_id = str(item.get("component_id") or item.get("componentId") or "")
        if root_code and item_component_id == root_code:
            continue

        row = dict(item)
        row.setdefault("display_role", "\u5168\u5c40\u5019\u9009\u8865\u5145")
        row.setdefault("display_source", "global_component_fallback")
        rows.append(row)

    return rows[:3]


def build_root_consistent_rca_fields(
    source: dict[str, Any],
    current_rca: dict[str, Any] | None = None,
) -> dict[str, Any]:
    if current_rca is None:
        current_rca = {}

    payload = unwrap_algorithm_payload(source)
    business = payload.get("business_conclusion") or payload.get("businessConclusion") or {}
    rca_result = source.get("rca_result") if isinstance(source.get("rca_result"), dict) else {}

    raw_payload = payload.get("raw") if isinstance(payload.get("raw"), dict) else {}
    raw_payload = raw_payload or (source.get("raw") if isinstance(source.get("raw"), dict) else {})
    raw_payload = raw_payload or (source.get("raw_payload") if isinstance(source.get("raw_payload"), dict) else {})

    # 最高优先级：若依第一部分算法明确给出的根因部件
    # 注意：这些字段不能被 severityScores.cooler 覆盖
    authoritative_root_code = (
            payload.get("root_component_code")
            or payload.get("faultComponentId")
            or payload.get("fault_component_id")
            or payload.get("component_id")
            or payload.get("component_code")
            or source.get("root_component_code")
            or source.get("faultComponentId")
            or source.get("fault_component_id")
            or source.get("component_id")
            or source.get("component_code")
            or raw_payload.get("root_component_code")
            or raw_payload.get("faultComponentId")
            or raw_payload.get("fault_component_id")
            or raw_payload.get("component_id")
            or raw_payload.get("component_code")
    )

    authoritative_root_name = (
            payload.get("root_component_name")
            or payload.get("faultComponent")
            or payload.get("fault_component")
            or payload.get("component_name")
            or source.get("root_component_name")
            or source.get("faultComponent")
            or source.get("fault_component")
            or source.get("component_name")
            or raw_payload.get("root_component_name")
            or raw_payload.get("faultComponent")
            or raw_payload.get("fault_component")
            or raw_payload.get("component_name")
    )

    root_component_code = (
            authoritative_root_code
            or current_rca.get("root_component_code")
            or rca_result.get("root_component_code")
            or rca_result.get("rootComponentCode")
            or source.get("root_component_code")
            or source.get("rootComponentCode")
    )

    root_component_name = (
            authoritative_root_name
            or current_rca.get("root_component_name")
            or rca_result.get("root_component_name")
            or rca_result.get("rootComponentName")
            or source.get("root_component_name")
            or source.get("rootComponentName")
    )

    root_component_confidence = (
            current_rca.get("component_confidence")
            or current_rca.get("rca_confidence")
            or rca_result.get("component_confidence")
            or rca_result.get("componentConfidence")
            or source.get("component_confidence")
            or source.get("componentConfidence")
    )

    root_component_code = str(root_component_code or "").strip().upper()
    root_component_name = str(root_component_name or "").strip()

    m = re.fullmatch(r"C0*(\d+)", root_component_code)
    if m:
        root_component_code = f"C{int(m.group(1)):03d}"

    if not root_component_code:
        if "作动筒" in root_component_name:
            root_component_code = "C003"
        elif "冷却器" in root_component_name:
            root_component_code = "C006"
        elif "过滤器" in root_component_name:
            root_component_code = "C007"
        elif "蓄能器" in root_component_name:
            root_component_code = "C005"
        elif "管路总成" in root_component_name or "管路" in root_component_name:
            root_component_code = "C011"

    # 只有当上游没有明确 root_component_code / faultComponentId 时，才允许用 severityScores 推断
    if not root_component_code:
        inferred_code, inferred_name, inferred_score = infer_root_component_from_severity(payload)
        root_component_code = inferred_code
        root_component_name = root_component_name or inferred_name
        root_component_confidence = root_component_confidence or inferred_score

    component_diagnostics = (
            payload.get("componentDiagnostics")
            or payload.get("component_diagnostics")
            or business.get("componentDiagnostics")
            or business.get("component_diagnostics")
            or source.get("component_diagnostics")
            or source.get("componentDiagnostics")
            or []
    )
    if not isinstance(component_diagnostics, list):
        component_diagnostics = []

    valid_component_diagnostics = [x for x in component_diagnostics if isinstance(x, dict)]
    global_component_top3 = sorted(
        valid_component_diagnostics,
        key=lambda x: safe_float(x.get("severity"), 0.0),
        reverse=True,
    )[:3]

    root_component_diagnosis = None
    for item in valid_component_diagnostics:
        item_component_id = str(item.get("component_id") or item.get("componentId") or "")
        if root_component_code and item_component_id == str(root_component_code):
            root_component_diagnosis = item
            break

    subtype_by_component = (
            payload.get("subtypeByComponent")
            or payload.get("subtype_by_component")
            or business.get("subtypeByComponent")
            or business.get("subtype_by_component")
            or source.get("subtype_by_component")
            or source.get("subtypeByComponent")
            or {}
    )

    subtype_probabilities = (
            payload.get("subtypeProbabilities")
            or payload.get("subtype_probabilities")
            or business.get("subtypeProbabilities")
            or business.get("subtype_probabilities")
            or source.get("subtype_probabilities")
            or source.get("subtypeProbabilities")
            or []
    )

    root_subtypes_raw = []
    if isinstance(subtype_by_component, dict) and root_component_code:
        root_block = subtype_by_component.get(str(root_component_code)) or {}
        if isinstance(root_block, dict):
            root_subtypes_raw = root_block.get("sub_types") or root_block.get("subTypes") or []

    root_subtypes = normalize_subtype_rows(root_subtypes_raw)
    all_subtypes = normalize_subtype_rows(subtype_probabilities)

    if not root_subtypes:
        root_subtypes = [
            x for x in all_subtypes
            if str(x.get("component_id", "")) == str(root_component_code)
        ]

    global_subtype_top5 = all_subtypes[:5]
    subtype_display_note = ""

    if not root_subtypes:
        root_subtypes = global_subtype_top5
        subtype_display_note = (
            "No subtype matched the RCA root component; "
            "showing global subtype Top5 as auxiliary reference."
        )

    root_subtypes = root_subtypes[:5]

    root_consistent_component_top3 = build_root_component_display_top3(
        root_component_diagnosis=root_component_diagnosis,
        root_subtypes=root_subtypes,
        root_component_code=root_component_code,
        root_component_name=root_component_name,
        global_component_top3=global_component_top3,
    )

    global_top3_ids = {
        str(x.get("component_id") or x.get("componentId") or "")
        for x in global_component_top3
        if isinstance(x, dict)
    }

    consistency_warning = ""
    if root_component_code and str(root_component_code) not in global_top3_ids:
        consistency_warning = (
            "RCA root component is not in global component diagnosis Top3; "
            "please review RCA score and component severity score consistency."
        )

    return {
        "root_component_code": root_component_code,
        "root_component_name": root_component_name,
        "component_confidence": safe_float(root_component_confidence, 0.0),
        "rca_confidence": safe_float(
            current_rca.get("rca_confidence") or root_component_confidence,
            0.0,
        ),
        "root_component_diagnosis": root_component_diagnosis,
        "root_component_subtype_topk": root_subtypes,
        "component_diagnosis_top3": root_consistent_component_top3,
        "subtype_top5": root_subtypes,
        "global_component_diagnosis_top3": global_component_top3,
        "global_subtype_top5": global_subtype_top5,
        "subtype_display_note": subtype_display_note,
        "consistency_warning": consistency_warning,
    }


def build_root_focus_normalized_upstream(
    domain: str,
    base_case_id: str,
    first_result: dict[str, Any],
    rca_fields: dict[str, Any],
) -> dict[str, Any]:
    root_component_code = rca_fields.get("root_component_code")
    root_component_name = rca_fields.get("root_component_name")
    feedback_component_code = first_result.get("fault_component_id")
    feedback_component_name = first_result.get("fault_component")
    component_code = normalize_component_code(root_component_code or feedback_component_code)
    upstream_rca_result = {
        **rca_fields,
        "root_component_code": normalize_component_code(root_component_code),
        "root_component_name": root_component_name,
        "root_sensor": first_result.get("trigger_sensor"),
        "root_sensor_confidence": first_result.get("sensor_confidence"),
        "feedback_component_code": normalize_component_code(feedback_component_code),
        "feedback_component_name": feedback_component_name,
    }

    return {
        "upstream_case_id": first_result.get("upstream_case_id") or first_result.get("case_id") or base_case_id,
        "trace_focus": "root",
        "component_code": component_code,
        "component_name": root_component_name or feedback_component_name,
        "feedback_component_code": feedback_component_code,
        "feedback_component_name": feedback_component_name,
        "fault_type": first_result.get("fault_type") or first_result.get("algorithm_conclusion") or "",
        "fault_position": root_component_name or feedback_component_name or "",
        "diagnosis_confidence": rca_fields.get("rca_confidence") or first_result.get("component_confidence"),
        "fault_severity": first_result.get("fault_severity") or "",
        "rca_result": upstream_rca_result,
        "raw_payload": first_result.get("raw") or first_result,
    }


def normalize_component_code(value: Any) -> str:
    text = "" if value is None else str(value).strip().upper()
    match = re.fullmatch(r"C0*(\d+)", text)
    if match:
        return f"C{int(match.group(1)):03d}"
    return text


FAULT_TYPE_GROUPS = [
    {"pressure_abnormal", "pressure_low", "pressure_high"},
    {"flow_abnormal", "flow_low", "flow_high", "filter_blocked"},
    {"temperature_abnormal", "temperature_high", "temperature_low"},
    {"oil_leakage", "leakage", "oil_leak"},
    {"valve_stuck", "stuck"},
    {"vibration_abnormal", "vibration_high"},
]


def normalize_match_text(value: Any) -> str:
    return "" if value is None else str(value).strip().lower()


def fault_types_same_group(left: Any, right: Any) -> bool:
    left_text = normalize_match_text(left)
    right_text = normalize_match_text(right)
    if not left_text or not right_text or left_text == right_text:
        return False
    return any(left_text in group and right_text in group for group in FAULT_TYPE_GROUPS)


def select_best_feedback_case(
    df: Any,
    component_code: Any,
    component_name: Any,
    upstream_fault_type: Any,
    upstream_fault_severity: Any,
) -> dict[str, Any]:
    normalized_code = normalize_component_code(component_code)
    normalized_name = "" if component_name is None else str(component_name).strip()
    upstream_type = normalize_match_text(upstream_fault_type)
    upstream_severity = normalize_match_text(upstream_fault_severity)
    severity_bonus = {"severe": 3.0, "medium": 2.0, "slight": 1.0}
    if hasattr(df, "to_dict"):
        rows = df.to_dict("records")
    else:
        rows = list(df or [])

    candidates: list[dict[str, Any]] = []
    for row in rows:
        row_code = normalize_component_code(row.get("component_code"))
        if row_code != normalized_code:
            continue

        row_name = "" if row.get("component_name") is None else str(row.get("component_name")).strip()
        row_type = normalize_match_text(row.get("fault_type"))
        row_severity = normalize_match_text(row.get("fault_severity"))
        diagnosis_confidence = safe_float(row.get("diagnosis_confidence"), 0.0)
        rca_confidence = safe_float(row.get("rca_confidence"), 0.0)

        score = 100.0
        reasons = [f"component_code={row_code} exact +100"]
        if normalized_name and row_name == normalized_name:
            score += 50.0
            reasons.append("component_name exact +50")
        if upstream_type and row_type == upstream_type:
            score += 40.0
            reasons.append("fault_type exact +40")
        elif upstream_type and fault_types_same_group(row_type, upstream_type):
            score += 20.0
            reasons.append("fault_type same group +20")
        if upstream_severity and row_severity == upstream_severity:
            score += 20.0
            reasons.append("fault_severity exact +20")

        if upstream_severity and row_severity in severity_bonus:
            score += severity_bonus[row_severity]
            reasons.append(f"fault_severity bonus +{severity_bonus[row_severity]:g}")

        candidates.append({
            "row": row,
            "score": score,
            "diagnosis_confidence": diagnosis_confidence,
            "rca_confidence": rca_confidence,
            "selection_reason": "; ".join(reasons),
        })

    if not candidates:
        return {
            "fallback_used": False,
            "matched_component_code": normalized_code,
            "candidate_count": 0,
            "match_reason": f"No feedback row matched component_code={normalized_code}.",
        }

    ranked = sorted(
        candidates,
        key=lambda item: (
            item["score"],
            item["diagnosis_confidence"],
            item["rca_confidence"],
            str(item["row"].get("feedback_id", "")),
        ),
        reverse=True,
    )
    best = ranked[0]
    best_row = best["row"]
    selected_case_id = normalize_case_id_alias(best_row.get("feedback_id") or best_row.get("case_id") or "")

    top_candidates = []
    for item in ranked[:5]:
        row = item["row"]
        top_candidates.append({
            "feedback_id": row.get("feedback_id", ""),
            "case_id": row.get("case_id", ""),
            "component_code": normalize_component_code(row.get("component_code")),
            "component_name": row.get("component_name", ""),
            "fault_type": row.get("fault_type", ""),
            "fault_severity": row.get("fault_severity", ""),
            "diagnosis_confidence": item["diagnosis_confidence"],
            "rca_confidence": item["rca_confidence"],
            "score": round(item["score"], 6),
            "selection_reason": item["selection_reason"],
        })

    return {
        "fallback_used": bool(selected_case_id),
        "selected_case_id": selected_case_id,
        "matched_component_code": normalize_component_code(best_row.get("component_code")),
        "normalized_component_code": normalized_code,
        "matched_feedback_id": best_row.get("feedback_id", ""),
        "selected_feedback_id": best_row.get("feedback_id", ""),
        "matched_case_id": best_row.get("case_id", ""),
        "matched_component_name": best_row.get("component_name", ""),
        "selected_fault_type": best_row.get("fault_type", ""),
        "selected_fault_severity": best_row.get("fault_severity", ""),
        "selected_score": round(best["score"], 6),
        "candidate_count": len(candidates),
        "selection_reason": best["selection_reason"],
        "top_candidates": top_candidates,
        "match_reason": (
            f"Selected best feedback row by component_code={normalized_code}, "
            "fault_type, fault_severity and confidence ranking."
        ),
    }


def fallback_select_case_by_component_code(
    domain: str,
    component_code: Any,
    component_name: Any = None,
    upstream_fault_type: Any = None,
    upstream_fault_severity: Any = None,
) -> dict[str, Any]:
    normalized_code = normalize_component_code(component_code)
    if not normalized_code:
        return {
            "fallback_used": False,
            "match_reason": "No component_code provided for fallback matching.",
        }

    feedback_path = PROJECT_ROOT / "data" / domain / f"{domain}_feedback.csv"
    if not feedback_path.exists():
        return {
            "fallback_used": False,
            "matched_component_code": normalized_code,
            "match_reason": f"Feedback file not found: {feedback_path}",
        }

    with feedback_path.open("r", encoding="utf-8-sig", newline="") as handle:
        rows = list(csv.DictReader(handle))

    return select_best_feedback_case(
        rows,
        normalized_code,
        component_name,
        upstream_fault_type,
        upstream_fault_severity,
    )


def apply_component_feedback_fallback(
    domain: str,
    root_focus_normalized: dict[str, Any],
    target_resolution: dict[str, Any],
    original_case_id: str = "",
) -> tuple[str, dict[str, Any]]:
    selected_case_id = normalize_case_id_alias(
        str(target_resolution.get("selected_case_id") or original_case_id)
    )
    root_component_code = root_focus_normalized.get("component_code")
    root_component_name = root_focus_normalized.get("component_name")
    component_fallback = fallback_select_case_by_component_code(
        domain,
        root_component_code,
        root_component_name,
        root_focus_normalized.get("fault_type"),
        root_focus_normalized.get("fault_severity"),
    )
    if component_fallback.get("fallback_used"):
        selected_case_id = str(component_fallback["selected_case_id"])
        target_resolution["selected_case_id"] = selected_case_id
        target_resolution["match_mode"] = "component_code_feedback_fallback"
        target_resolution["match_reason"] = component_fallback.get("match_reason")

    target_resolution["component_fallback"] = component_fallback
    return selected_case_id, target_resolution


def extract_authoritative_root_from_first_result(first_result: dict[str, Any]) -> dict[str, str]:
    """
    从若依第一部分算法结果中提取最高优先级根因部件。
    注意：first_result.raw 中的 faultComponentId/root_component_code 优先级最高，
    不允许被 severityScores.cooler 等字段覆盖。
    """
    if not isinstance(first_result, dict):
        return {"component_code": "", "component_name": ""}

    raw = first_result.get("raw")
    if not isinstance(raw, dict):
        raw = {}

    component_code = (
        raw.get("root_component_code")
        or raw.get("faultComponentId")
        or raw.get("component_id")
        or raw.get("component_code")
        or first_result.get("root_component_code")
        or first_result.get("faultComponentId")
        or first_result.get("component_id")
        or first_result.get("component_code")
        or first_result.get("fault_component_id")
        or ""
    )

    component_name = (
        raw.get("root_component_name")
        or raw.get("faultComponent")
        or raw.get("component_name")
        or first_result.get("root_component_name")
        or first_result.get("faultComponent")
        or first_result.get("component_name")
        or first_result.get("fault_component")
        or ""
    )

    component_code = str(component_code or "").strip().upper()
    component_name = str(component_name or "").strip()

    m = re.fullmatch(r"C0*(\d+)", component_code)
    if m:
        component_code = f"C{int(m.group(1)):03d}"

    if not component_code:
        if "作动筒" in component_name:
            component_code = "C003"
        elif "冷却器" in component_name:
            component_code = "C006"
        elif "过滤器" in component_name:
            component_code = "C007"
        elif "蓄能器" in component_name:
            component_code = "C005"
        elif "管路总成" in component_name or "管路" in component_name:
            component_code = "C011"

    if not component_name:
        name_map = {
            "C003": "作动筒",
            "C006": "冷却器",
            "C007": "过滤器",
            "C005": "蓄能器",
            "C011": "管路总成",
        }
        component_name = name_map.get(component_code, "")

    return {
        "component_code": component_code,
        "component_name": component_name,
    }


def force_selected_case_by_authoritative_root(
    selected_case_id: str,
    root_focus_normalized: dict[str, Any],
    root_focus_resolution: dict[str, Any],
    first_result: dict[str, Any],
) -> tuple[str, dict[str, Any], dict[str, Any]]:
    """
    以第一部分算法输出的明确根因部件为准，修正 selected_case_id。
    """
    root = extract_authoritative_root_from_first_result(first_result)
    component_code = root["component_code"]
    component_name = root["component_name"]

    manual_case_map = {
        "C003": "HFB-04025",   # 作动筒
        "C006": "HFB-01054",   # 冷却器
        "C007": "HFB-02227",   # 过滤器
    }

    if component_code in manual_case_map:
        selected_case_id = manual_case_map[component_code]

        root_focus_normalized["component_code"] = component_code
        root_focus_normalized["component_name"] = component_name
        root_focus_normalized["root_component_code"] = component_code
        root_focus_normalized["root_component_name"] = component_name
        root_focus_normalized["fault_component_id"] = component_code
        root_focus_normalized["fault_component"] = component_name

        root_focus_resolution["selected_case_id"] = selected_case_id
        root_focus_resolution["root_component_code"] = component_code
        root_focus_resolution["root_component_name"] = component_name
        root_focus_resolution["match_mode"] = "authoritative_first_algorithm_root_override"
        root_focus_resolution["manual_component_case_override"] = True
        root_focus_resolution["manual_selected_case_id"] = selected_case_id

    if selected_case_id == "HFB-01054" and component_code and component_code != "C006":
        raise HTTPException(
            status_code=400,
            detail=(
                f"错误匹配：第一部分算法根因是 {component_code} {component_name}，"
                f"但系统仍选择了冷却器案例 HFB-01054，已阻止。"
            ),
        )

    return selected_case_id, root_focus_normalized, root_focus_resolution
def resolve_case_id_by_root_component(
    domain: str,
    original_case_id: str,
    first_result: dict[str, Any],
) -> tuple[str, dict[str, Any], dict[str, Any]]:
    original_case_id = normalize_case_id_alias(original_case_id)

    if not first_result:
        return original_case_id, {}, {
            "selected_case_id": original_case_id,
            "match_mode": "original_case_id",
            "match_reason": "No first algorithm result; using original case_id.",
        }

    rca_fields = build_root_consistent_rca_fields(first_result, {})
    root_focus_normalized = build_root_focus_normalized_upstream(
        domain=domain,
        base_case_id=original_case_id,
        first_result=first_result,
        rca_fields=rca_fields,
    )

    raw_first = first_result.get("raw") if isinstance(first_result.get("raw"), dict) else {}
    root_component_code = (
            raw_first.get("root_component_code")
            or raw_first.get("faultComponentId")
            or raw_first.get("component_id")
            or raw_first.get("component_code")
            or raw_first.get("fault_component_id")
            or first_result.get("root_component_code")
            or first_result.get("faultComponentId")
            or first_result.get("component_id")
            or first_result.get("component_code")
            or first_result.get("fault_component_id")
            or root_focus_normalized.get("component_code")
            or root_focus_normalized.get("root_component_code")
            or root_focus_normalized.get("fault_component_id")
            or ""
    )

    root_component_name = (
            raw_first.get("root_component_name")
            or raw_first.get("faultComponent")
            or raw_first.get("component_name")
            or raw_first.get("fault_component")
            or first_result.get("root_component_name")
            or first_result.get("faultComponent")
            or first_result.get("component_name")
            or first_result.get("fault_component")
            or root_focus_normalized.get("component_name")
            or root_focus_normalized.get("root_component_name")
            or root_focus_normalized.get("fault_component")
            or ""
    )

    root_component_code = str(root_component_code or "").strip().upper()
    root_component_name = str(root_component_name or "").strip()

    # 兼容 C3 / C03 / C003
    m = re.fullmatch(r"C0*(\d+)", root_component_code)
    if m:
        root_component_code = f"C{int(m.group(1)):03d}"

    # 编码缺失时，用中文名称兜底
    if not root_component_code:
        if "液压泵" in root_component_name:
            root_component_code = "C001"
        elif "方向控制阀" in root_component_name or "方向阀" in root_component_name:
            root_component_code = "C002"
        elif "作动筒" in root_component_name:
            root_component_code = "C003"
        elif "溢流阀" in root_component_name:
            root_component_code = "C004"
        elif "蓄能器" in root_component_name:
            root_component_code = "C005"
        elif "冷却器" in root_component_name:
            root_component_code = "C006"
        elif "过滤器" in root_component_name:
            root_component_code = "C007"
        elif "油箱" in root_component_name:
            root_component_code = "C008"
        elif "单向阀" in root_component_name:
            root_component_code = "C009"
        elif "节流阀" in root_component_name:
            root_component_code = "C010"
        elif "管路总成" in root_component_name or "管路" in root_component_name:
            root_component_code = "C011"
        elif "卸荷阀" in root_component_name:
            root_component_code = "C012"

    root_focus_normalized["component_code"] = root_component_code
    root_focus_normalized["component_name"] = root_component_name
    root_focus_normalized["root_component_code"] = root_component_code
    root_focus_normalized["root_component_name"] = root_component_name
    root_focus_normalized["fault_component_id"] = root_component_code
    root_focus_normalized["fault_component"] = root_component_name

    if not root_component_code and not root_component_name:
        return original_case_id, root_focus_normalized, {
            "selected_case_id": original_case_id,
            "match_mode": "fallback_original_case_id",
            "match_reason": "No RCA root component parsed; using original case_id.",
        }

    # ============================================================
    # 关键：人工映射表。只填你确认过的案例。
    # C006 是冷却器，才允许 HFB-01054。
    # C007 你之前已经验证过是 HFB-02227。
    # C003 作动筒需要你查到真实案例后填进去。
    # ============================================================
    manual_case_map = {
        "C006": "HFB-01054",  # 冷却器
        "C007": "HFB-02227",  # 过滤器
        "C003": "HFB-04025",  # 作动筒
    }
        # "C003": "HFB-xxxxx", # 作动筒：查到真实编号后取消注释并填写


    if root_component_code in manual_case_map:
        selected_case_id = manual_case_map[root_component_code]
        return selected_case_id, root_focus_normalized, {
            "selected_case_id": selected_case_id,
            "match_mode": "manual_component_case_map",
            "match_reason": f"Manual mapping by RCA root component {root_component_code} {root_component_name}.",
            "trace_focus": "root",
            "root_component_code": root_component_code,
            "root_component_name": root_component_name,
            "original_case_id": original_case_id,
            "manual_component_case_override": True,
        }

    try:
        target_resolution = resolve_target_case(domain, root_focus_normalized, PROJECT_ROOT)

        selected_case_id, target_resolution = apply_component_feedback_fallback(
            domain,
            root_focus_normalized,
            target_resolution,
            original_case_id,
        )

        selected_case_id = normalize_case_id_alias(str(selected_case_id))

        # ============================================================
        # 关键保护：非 C006 输入，不允许落到冷却器案例 HFB-01054
        # ============================================================
        if selected_case_id == "HFB-01054" and root_component_code != "C006":

            component_fallback = fallback_select_case_by_component_code(
                domain,
                root_component_code,
                root_component_name,
                root_focus_normalized.get("fault_type"),
                root_focus_normalized.get("fault_severity"),
            )

            fallback_case_id = str(component_fallback.get("selected_case_id", "")).strip()

            if (
                component_fallback.get("fallback_used")
                and fallback_case_id
                and not (fallback_case_id == "HFB-01054" and root_component_code != "C006")
            ):
                selected_case_id = normalize_case_id_alias(fallback_case_id)
                return selected_case_id, root_focus_normalized, {
                    "selected_case_id": selected_case_id,
                    "match_mode": "component_code_feedback_fallback_after_wrong_cooler_match",
                    "match_reason": component_fallback.get("match_reason"),
                    "trace_focus": "root",
                    "root_component_code": root_component_code,
                    "root_component_name": root_component_name,
                    "original_case_id": original_case_id,
                    "blocked_wrong_case_id": "HFB-01054",
                    "component_fallback": component_fallback,
                }

            raise HTTPException(
                status_code=400,
                detail=(
                    f"错误匹配：上游根因部件是 {root_component_code} {root_component_name}，"
                    f"但系统选择了冷却器案例 HFB-01054。已阻止默认回退。"
                    f"请为 {root_component_code} 配置正确的本地 HFB 案例映射。"
                ),
            )

        target_resolution["trace_focus"] = "root"
        target_resolution["root_component_code"] = root_component_code
        target_resolution["root_component_name"] = root_component_name
        target_resolution["original_case_id"] = original_case_id

        return selected_case_id, root_focus_normalized, target_resolution

    except HTTPException:
        raise

    except Exception as exc:
        component_fallback = fallback_select_case_by_component_code(
            domain,
            root_component_code,
            root_component_name,
            root_focus_normalized.get("fault_type"),
            root_focus_normalized.get("fault_severity"),
        )

        if component_fallback.get("fallback_used"):
            selected_case_id = str(component_fallback["selected_case_id"])

            if selected_case_id == "HFB-01054" and root_component_code != "C006":
                raise HTTPException(
                    status_code=400,
                    detail=(
                        f"错误兜底：上游根因部件是 {root_component_code} {root_component_name}，"
                        f"但 fallback 仍然选择了冷却器案例 HFB-01054。已阻止。"
                    ),
                )

            return selected_case_id, root_focus_normalized, {
                "selected_case_id": selected_case_id,
                "match_mode": "component_code_feedback_fallback",
                "match_reason": component_fallback.get("match_reason"),
                "trace_focus": "root",
                "root_component_code": root_component_code,
                "root_component_name": root_component_name,
                "original_case_id": original_case_id,
                "resolve_target_case_error": str(exc),
                "component_fallback": component_fallback,
            }

        raise HTTPException(
            status_code=400,
            detail=(
                f"根因部件案例匹配失败：上游根因部件为 {root_component_code} {root_component_name}，"
                f"未找到对应本地生命周期案例。原始错误：{exc}"
            ),
        )



def apply_first_algorithm_context(reasoning: dict[str, Any], first_result: dict[str, Any]) -> dict[str, Any]:
    if not isinstance(reasoning, dict):
        reasoning = {}

    rca_context = reasoning.get("rca_context")
    if not isinstance(rca_context, dict):
        rca_context = {}

    if first_result:
        rca_context["root_sensor"] = first_result.get("trigger_sensor")
        rca_context["root_sensor_confidence"] = first_result.get("sensor_confidence")

        if first_result.get("fault_component_id"):
            rca_context.setdefault("root_component_code", first_result.get("fault_component_id"))
        if first_result.get("fault_component"):
            rca_context.setdefault("root_component_name", first_result.get("fault_component"))

        rca_context["rca_confidence"] = first_result.get("component_confidence")
        rca_context["evolution_chain"] = first_result.get("evolution_chain")
        rca_context["algorithm_conclusion"] = first_result.get("algorithm_conclusion")
        rca_context["component_diagnostics_from_first_algorithm"] = first_result.get("component_diagnostics", [])
        rca_context["first_algorithm_result"] = first_result

        root_consistent_fields = build_root_consistent_rca_fields(first_result, rca_context)
        rca_context.update(root_consistent_fields)

    reasoning["rca_context"] = rca_context
    return reasoning


UPSTREAM_RCA_OVERRIDE_FIELDS = [
    "root_component_code",
    "root_component_name",
    "component_confidence",
    "rca_confidence",
    "root_sensor",
    "root_sensor_confidence",
    "root_component_diagnosis",
    "root_component_subtype_topk",
    "component_diagnosis_top3",
    "subtype_top5",
    "global_component_diagnosis_top3",
    "global_subtype_top5",
    "subtype_display_note",
    "feedback_component_code",
    "feedback_component_name",
]


def build_upstream_conclusion_text(
    reasoning: dict[str, Any],
    root_focus_normalized: dict[str, Any],
    selected_case_id: str,
) -> str:
    rca_result = root_focus_normalized.get("rca_result")
    if not isinstance(rca_result, dict):
        rca_result = {}

    target_object = reasoning.get("target_object")
    if not isinstance(target_object, dict):
        target_object = {}

    feedback_code = (
        root_focus_normalized.get("component_code")
        or rca_result.get("feedback_component_code")
        or root_focus_normalized.get("feedback_component_code")
        or ""
    )
    feedback_name = (
        target_object.get("object_name")
        or root_focus_normalized.get("component_name")
        or rca_result.get("feedback_component_name")
        or root_focus_normalized.get("feedback_component_name")
        or ""
    )
    root_code = normalize_component_code(
        rca_result.get("root_component_code") or root_focus_normalized.get("component_code")
    )
    root_name = (
        rca_result.get("root_component_name")
        or root_focus_normalized.get("component_name")
        or feedback_name
        or ""
    )
    root_sensor = rca_result.get("root_sensor") or root_focus_normalized.get("root_sensor") or "N/A"

    top6 = reasoning.get("top6_candidates")
    top1_reason = ""
    if isinstance(top6, list) and top6:
        top1 = top6[0] if isinstance(top6[0], dict) else {}
        top1_reason = str(top1.get("candidate_id") or top1.get("id") or "")
    if not top1_reason:
        top1_reason = "当前 selected_case_id 的 Top1 TransH 追溯结果"

    return (
        f"液压反馈对象为 {feedback_code} {feedback_name}，"
        f"上游 RCA 根因部件为 {root_code} {root_name}，"
        f"根因传感器为 {root_sensor}。"
        f"Top1 疑似原因仍来自当前 selected_case_id={selected_case_id} 的 TransH 追溯结果：{top1_reason}。"
        "本次追溯以 root_focus 方式根据上游 RCA 根因部件匹配本地生命周期案例，"
        "返回 JSON 中的 RCA 上下文以上游 RCA 结果为准。"
    )


def apply_upstream_root_focus_rca_override(
    reasoning: dict[str, Any],
    root_focus_normalized: dict[str, Any],
    selected_case_id: str,
) -> dict[str, Any]:
    if not isinstance(reasoning, dict):
        reasoning = {}
    if not isinstance(root_focus_normalized, dict):
        return reasoning

    rca_result = root_focus_normalized.get("rca_result")
    if not isinstance(rca_result, dict) or not rca_result:
        return reasoning

    rca_context = reasoning.get("rca_context")
    if not isinstance(rca_context, dict):
        rca_context = {}

    for field in UPSTREAM_RCA_OVERRIDE_FIELDS:
        if field in rca_result and rca_result[field] is not None:
            rca_context[field] = rca_result[field]

    root_code = rca_result.get("root_component_code") or root_focus_normalized.get("component_code")
    if root_code:
        rca_context["root_component_code"] = normalize_component_code(root_code)

    fallback_fields = {
        "root_component_name": root_focus_normalized.get("component_name"),
        "feedback_component_code": root_focus_normalized.get("feedback_component_code"),
        "feedback_component_name": root_focus_normalized.get("feedback_component_name"),
    }
    for field, value in fallback_fields.items():
        if not rca_context.get(field) and value is not None:
            rca_context[field] = value

    rca_context["rca_source"] = "upstream_first_algorithm"
    rca_context["local_case_rca_overridden"] = True
    reasoning["rca_context"] = rca_context

    upstream_conclusion = build_upstream_conclusion_text(
        reasoning,
        root_focus_normalized,
        selected_case_id,
    )
    reasoning["upstream_conclusion_text"] = upstream_conclusion
    reasoning["conclusion_text"] = upstream_conclusion
    return reasoning


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok", "service": "transh-trace-api"}


@app.post("/api/v1/trace/run")
def run_trace(request: TraceRunRequest) -> dict[str, Any]:
    original_case_id = normalize_case_id_alias(request.case_id)
    first_result = parse_first_algorithm_result(request.first_algorithm_result)

    print("\n========== first algorithm result ==========")
    print(json.dumps(first_result, ensure_ascii=False, indent=2))
    print("==========================================\n")

    selected_case_id, root_focus_normalized, root_focus_resolution = resolve_case_id_by_root_component(
        domain=request.domain,
        original_case_id=original_case_id,
        first_result=first_result,
    )

    selected_case_id, root_focus_normalized, root_focus_resolution = force_selected_case_by_authoritative_root(
        selected_case_id=selected_case_id,
        root_focus_normalized=root_focus_normalized,
        root_focus_resolution=root_focus_resolution,
        first_result=first_result,
    )

    print("\n========== final selected case after authoritative root override ==========")
    print("selected_case_id =", selected_case_id)
    print("root_component_code =", root_focus_normalized.get("component_code"))
    print("root_component_name =", root_focus_normalized.get("component_name"))
    print("========================================================================\n")
    # ==================== 新增：提取上游根因部件编码 ====================
    upstream_root_code = (
        root_focus_normalized.get("root_component_code")
        or root_focus_normalized.get("component_code")
        or root_focus_normalized.get("fault_component_id")
        or root_focus_normalized.get("faultComponentId")
        or first_result.get("root_component_code")
        or first_result.get("component_code")
        or first_result.get("component_id")
        or first_result.get("fault_component_id")
        or first_result.get("faultComponentId")
        or ""
    )

    upstream_root_name = (
        root_focus_normalized.get("root_component_name")
        or root_focus_normalized.get("component_name")
        or root_focus_normalized.get("fault_component")
        or root_focus_normalized.get("faultComponent")
        or first_result.get("root_component_name")
        or first_result.get("component_name")
        or first_result.get("fault_component")
        or first_result.get("faultComponent")
        or ""
    )

    upstream_root_code = str(upstream_root_code or "").strip().upper()
    upstream_root_name = str(upstream_root_name or "").strip()

    # 兼容 C7 / C07 / C007
    match = re.fullmatch(r"C0*(\d+)", upstream_root_code)
    if match:
        upstream_root_code = f"C{int(match.group(1)):03d}"

    # 如果没有编码，则根据中文名兜底
    if not upstream_root_code:
        if "作动筒" in upstream_root_name:
            upstream_root_code = "C003"
        elif "冷却器" in upstream_root_name:
            upstream_root_code = "C006"
        elif "过滤器" in upstream_root_name:
            upstream_root_code = "C007"
        elif "蓄能器" in upstream_root_name:
            upstream_root_code = "C005"
        elif "管路总成" in upstream_root_name or "管路" in upstream_root_name:
            upstream_root_code = "C011"

    # ==================== 关键保护：禁止错误回退到冷却器 ====================
    # 如果上游根因不是 C006，却被匹配成 HFB-01054，说明匹配逻辑有问题，直接报错。
    # 这样可以避免“无论输入什么都显示冷却器”。
    if selected_case_id == "HFB-01054" and upstream_root_code and upstream_root_code != "C006":
        raise HTTPException(
            status_code=400,
            detail=(
                f"错误匹配：上游根因部件是 {upstream_root_code} {upstream_root_name}，"
                f"但系统选择了冷却器案例 HFB-01054。已阻止默认回退，请检查 "
                f"resolve_case_id_by_root_component 的部件到案例匹配逻辑。"
            ),
        )

    print("\n========== RCA root component match ==========")
    print("original_case_id =", original_case_id)
    print("selected_case_id =", selected_case_id)
    print("upstream_root_code =", upstream_root_code)
    print("upstream_root_name =", upstream_root_name)
    print(json.dumps(root_focus_resolution, ensure_ascii=False, indent=2))
    print("============================================\n")

    result = execute_trace_pipeline(
        request.domain,
        selected_case_id,
        request.model_variant,
        request.generate_graph,
        request.generate_report,
        request.generate_echarts,
        request.generate_docx,
    )

    reasoning = apply_first_algorithm_context(result["reasoning"], first_result)
    reasoning = apply_upstream_root_focus_rca_override(
        reasoning,
        root_focus_normalized,
        selected_case_id,
    )
    reasoning = sync_pipe_design_reference_payloads(result, reasoning)

    result_case_id = result.get("case_id", selected_case_id)

    response = {
        "code": 200,
        "message": "success",
        "input_mode": "case_id_root_focus" if first_result else "case_id",
        "domain": request.domain,
        "case_id": result_case_id,
        "original_case_id": original_case_id,
        "selected_case_id": selected_case_id,
        "model_variant": request.model_variant,
        "first_algorithm_context": first_result,
        "reasoning": reasoning,
        "graphs": result["graphs"],
        "report": result["report"],
        "artifacts": result["artifacts"],
        "debug": {
            "trace_focus": "root",
            "original_case_id": original_case_id,
            "selected_case_id": selected_case_id,
            "result_case_id": result_case_id,
            "upstream_root_component_code": upstream_root_code,
            "upstream_root_component_name": upstream_root_name,
            "root_focus_normalized": root_focus_normalized,
            "root_focus_resolution": root_focus_resolution,
            "merge_upstream_context_applied": bool(first_result),
        },
    }
    return applyPipeDesignReferencePatch(response)

@app.post("/api/v1/trace/run-case")
def run_trace_case(request: TraceRunCaseRequest) -> dict[str, Any]:
    case_id = normalize_case_id_alias(request.case_id)
    result = execute_trace_pipeline(
        request.domain,
        case_id,
        request.model_variant,
        request.generate_graph,
        request.generate_report,
        request.generate_echarts,
        request.generate_docx,
    )
    reasoning = sync_pipe_design_reference_payloads(result, result["reasoning"])
    response = {
        "code": 200,
        "message": "success",
        "input_mode": "case_id",
        "domain": request.domain,
        "case_id": result["case_id"],
        "model_variant": request.model_variant,
        "reasoning": reasoning,
        "graphs": result["graphs"],
        "report": result["report"],
        "artifacts": result["artifacts"],
    }
    return applyPipeDesignReferencePatch(response)


@app.post("/api/v1/trace/run-from-upstream")
def run_trace_from_upstream(request: UpstreamTraceRequest) -> dict[str, Any]:
    normalized_upstream = normalize_upstream_payload(request.domain, request.upstream_payload)

    upstream_rca = normalized_upstream.get("rca_result", {})
    if isinstance(upstream_rca, dict):
        root_component_code = normalize_component_code(upstream_rca.get("root_component_code"))
        root_component_name = upstream_rca.get("root_component_name")
    else:
        root_component_code = None
        root_component_name = None

    root_focus_normalized = dict(normalized_upstream)
    root_focus_normalized["trace_focus"] = "root"
    root_focus_normalized["feedback_component_code"] = normalized_upstream.get("component_code")
    root_focus_normalized["feedback_component_name"] = normalized_upstream.get("component_name")
    if root_component_code or root_component_name:
        root_focus_normalized["component_code"] = root_component_code
        root_focus_normalized["component_name"] = root_component_name

    target_resolution = resolve_target_case(request.domain, root_focus_normalized, PROJECT_ROOT)
    selected_case_id, root_focus_resolution = apply_component_feedback_fallback(
        request.domain,
        root_focus_normalized,
        target_resolution,
        normalize_case_id_alias(str(target_resolution.get("selected_case_id") or "")),
    )
    root_focus_resolution["trace_focus"] = "root"
    root_focus_resolution["root_component_code"] = root_component_code
    root_focus_resolution["root_component_name"] = root_component_name
    runtime_input_path = save_runtime_input(
        request.domain,
        request.upstream_payload,
        normalized_upstream,
        root_focus_resolution,
        PROJECT_ROOT,
    )
    result = execute_trace_pipeline(
        request.domain,
        selected_case_id,
        request.model_variant,
        request.generate_graph,
        request.generate_report,
        request.generate_echarts,
        request.generate_docx,
    )

    reasoning = result.get("reasoning", {})
    if not isinstance(reasoning, dict):
        reasoning = {}

    rca_context = reasoning.get("rca_context")
    if not isinstance(rca_context, dict):
        rca_context = {}

    if isinstance(upstream_rca, dict):
        for key, value in upstream_rca.items():
            if value is not None:
                rca_context[key] = value

    root_consistent_fields = build_root_consistent_rca_fields(normalized_upstream, rca_context)
    rca_context.update(root_consistent_fields)

    reasoning["rca_context"] = rca_context
    root_focus_normalized["rca_result"] = {
        **root_consistent_fields,
        "root_sensor": rca_context.get("root_sensor") or root_focus_normalized.get("root_sensor"),
        "root_sensor_confidence": rca_context.get("root_sensor_confidence"),
        "feedback_component_code": root_focus_normalized.get("feedback_component_code"),
        "feedback_component_name": root_focus_normalized.get("feedback_component_name"),
    }
    reasoning = apply_upstream_root_focus_rca_override(
        reasoning,
        root_focus_normalized,
        selected_case_id,
    )
    reasoning = sync_pipe_design_reference_payloads(result, reasoning)

    artifacts = dict(result["artifacts"])
    artifacts["runtime_input_json"] = str(runtime_input_path)
    response = {
        "code": 200,
        "message": "success",
        "input_mode": "upstream_json",
        "domain": request.domain,
        "upstream_case_id": normalized_upstream.get("upstream_case_id", ""),
        "selected_case_id": selected_case_id,
        "model_variant": request.model_variant,
        "normalized_upstream": normalized_upstream,
        "target_resolution": root_focus_resolution,
        "reasoning": reasoning,
        "graphs": result["graphs"],
        "report": result["report"],
        "artifacts": artifacts,
        "debug": {
            "trace_focus": "root",
            "selected_case_id": selected_case_id,
            "root_component_code": root_component_code,
            "root_component_name": root_component_name,
            "root_focus_normalized": root_focus_normalized,
            "root_focus_resolution": root_focus_resolution,
            "target_resolution": root_focus_resolution,
        },
        "note": "当前接口采用上游JSON适配模式：根据上游模型输出在本地生命周期数据中匹配事实一致的案例，并调用现有TransH追溯流程。",
    }
    return applyPipeDesignReferencePatch(response)


@app.get("/api/v1/trace/reasoning/{domain}/{case_id}")
def get_reasoning(domain: Domain, case_id: str) -> dict[str, Any]:
    normalized_case_id = normalize_case_id_alias(case_id)
    return applyPipeDesignReferencePatch(load_json(reasoning_path(domain, normalized_case_id)))


@app.get("/api/v1/trace/report/{domain}/{case_id}")
def get_report(domain: Domain, case_id: str) -> dict[str, Any]:
    normalized_case_id = normalize_case_id_alias(case_id)
    path = report_dir(domain) / f"trace_report_{normalized_case_id}.json"
    return refresh_report_files_if_pipe(domain, normalized_case_id, load_json(path))


@app.get("/api/v1/trace/report-md/{domain}/{case_id}")
def get_report_markdown(domain: Domain, case_id: str) -> dict[str, str]:
    normalized_case_id = normalize_case_id_alias(case_id)
    path = report_dir(domain) / f"trace_report_{normalized_case_id}.md"
    markdown = load_text(path)
    json_path = report_dir(domain) / f"trace_report_{normalized_case_id}.json"
    if json_path.exists():
        report_json = refresh_report_files_if_pipe(domain, normalized_case_id, load_json(json_path))
        if isinstance(report_json.get("pipe_design_reference"), dict):
            try:
                from scripts.generate_report import render_markdown

                markdown = render_markdown(report_json)
            except Exception:
                pass
    return {"domain": domain, "case_id": normalized_case_id, "markdown": markdown}


@app.get("/api/v1/trace/graph/{domain}/{case_id}")
def get_graph(
    domain: Domain,
    case_id: str,
    graph_type: GraphType = Query(default="echarts_enhanced"),
) -> dict[str, Any]:
    normalized_case_id = normalize_case_id_alias(case_id)
    graph_files = {
        "original": graph_dir(domain) / f"original_graph_{normalized_case_id}.json",
        "enhanced": graph_dir(domain) / f"fault_enhanced_graph_{normalized_case_id}.json",
        "echarts_original": graph_dir(domain) / f"echarts_original_graph_{normalized_case_id}.json",
        "echarts_enhanced": graph_dir(domain) / f"echarts_fault_enhanced_graph_{normalized_case_id}.json",
    }

    return load_json(graph_files[graph_type])


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=9782, reload=False)
