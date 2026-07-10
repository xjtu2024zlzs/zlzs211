from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]

CATEGORY_ORDER = [
    "feedback",
    "target_object",
    "top_candidate",
    "design",
    "material",
    "manufacturing",
    "assembly",
    "inspection",
    "operation",
    "sensor",
    "rca",
    "other",
]

CATEGORY_COLORS = {
    "feedback": "#d94841",
    "target_object": "#2f80ed",
    "top_candidate": "#f2994a",
    "design": "#6f52ed",
    "material": "#27ae60",
    "manufacturing": "#b7791f",
    "assembly": "#00a6a6",
    "inspection": "#2d9cdb",
    "operation": "#7f8c8d",
    "sensor": "#9b51e0",
    "rca": "#eb5757",
    "other": "#8f8f8f",
}

CATEGORY_LABELS = {
    "feedback": "质量反馈",
    "target_object": "反馈对象",
    "top_candidate": "Top6疑似原因",
    "design": "设计阶段",
    "material": "材料阶段",
    "manufacturing": "制造阶段",
    "assembly": "装配阶段",
    "inspection": "检测阶段",
    "operation": "使用/运维阶段",
    "sensor": "传感器",
    "rca": "RCA证据",
    "other": "辅助信息",
}

STAGE_LABELS = {
    "design": "设计阶段",
    "material": "材料阶段",
    "manufacturing": "制造阶段",
    "assembly": "装配阶段",
    "inspection": "检测阶段",
    "operation": "使用/运维阶段",
}

TYPE_LABELS = {
    "QualityFeedback": "质量反馈",
    "HydraulicComponent": "液压部件",
    "Bearing": "轴承",
    "DesignSpec": "设计规范",
    "BearingDesignSpec": "设计规范",
    "PipeDesignParam": "管路设计参数",
    "MaterialBatch": "材料批次",
    "ManufacturingBatch": "制造批次",
    "AssemblyRecord": "装配记录",
    "InspectionRecord": "检测记录",
    "MaintenanceRecord": "运维记录",
    "LubricationRecord": "润滑记录",
    "ComponentDiagnosis": "部件诊断",
    "FaultSubtype": "故障子类型",
    "Sensor": "传感器",
    "SensorEnergy": "传感器证据",
    "TargetSubsystem": "反馈子系统",
    "LifecycleStage": "生命周期阶段",
}

HIDDEN_LABEL_TYPES = {
    "RiskScore",
    "RiskLevel",
    "ConfidenceLevel",
    "ComponentName",
    "ComponentCode",
    "FaultSeverity",
    "FaultPosition",
    "FaultType",
    "PipeParameter",
    "MaterialGrade",
    "InspectionResult",
    "LubricationRecord",
    "Station",
    "Operator",
    "Equipment",
    "Supplier",
    "ManufacturingProcess",
}

VISIBLE_LABEL_TYPES = {
    "QualityFeedback",
    "HydraulicComponent",
    "Bearing",
    "DesignSpec",
    "BearingDesignSpec",
    "PipeDesignParam",
    "MaterialBatch",
    "ManufacturingBatch",
    "AssemblyRecord",
    "InspectionRecord",
    "MaintenanceRecord",
}

RCA_EDGE_RELATIONS = {
    "has_root_sensor",
    "has_secondary_sensor",
    "diagnosed_root_component",
    "activates_sensor",
    "has_component_diagnosis",
    "has_subtype_candidate",
}

RCA_NODE_TYPES = ("ComponentDiagnosis", "FaultSubtype", "RCA", "SensorEnergy")

EDGE_STYLE_RULES = {
    "may_caused_by": {"color": "#d62728", "width": 3.5, "type": "solid", "opacity": 0.95},
    "has_root_sensor": {"color": "#c2185b", "width": 2.8, "type": "solid", "opacity": 0.9},
    "has_secondary_sensor": {"color": "#c2185b", "width": 2.8, "type": "solid", "opacity": 0.9},
    "diagnosed_root_component": {"color": "#c2185b", "width": 2.8, "type": "solid", "opacity": 0.9},
    "activates_sensor": {"color": "#7b1fa2", "width": 2.2, "type": "dashed", "opacity": 0.85},
    "has_component_diagnosis": {"color": "#f57c00", "width": 2.0, "type": "dashed", "opacity": 0.85},
    "has_subtype_candidate": {"color": "#f57c00", "width": 2.0, "type": "dashed", "opacity": 0.85},
    "designed_by": {"color": "#6f42c1", "width": 1.8, "type": "solid", "opacity": 0.8},
    "has_pipe_design_param": {"color": "#6f42c1", "width": 1.8, "type": "solid", "opacity": 0.8},
    "uses_material_batch": {"color": "#2ca25f", "width": 1.8, "type": "solid", "opacity": 0.8},
    "supplied_by": {"color": "#2ca25f", "width": 1.8, "type": "solid", "opacity": 0.8},
    "manufactured_in": {"color": "#b7791f", "width": 1.8, "type": "solid", "opacity": 0.8},
    "uses_process": {"color": "#b7791f", "width": 1.8, "type": "solid", "opacity": 0.8},
    "processed_by": {"color": "#b7791f", "width": 1.8, "type": "solid", "opacity": 0.8},
    "assembled_in": {"color": "#00a6a6", "width": 1.6, "type": "solid", "opacity": 0.75},
    "operated_by": {"color": "#00a6a6", "width": 1.6, "type": "solid", "opacity": 0.75},
    "located_at": {"color": "#00a6a6", "width": 1.6, "type": "solid", "opacity": 0.75},
    "inspected_by": {"color": "#2b8cbe", "width": 1.6, "type": "solid", "opacity": 0.75},
    "has_inspection_result": {"color": "#2b8cbe", "width": 1.6, "type": "solid", "opacity": 0.75},
    "uses_inspection_type": {"color": "#2b8cbe", "width": 1.6, "type": "solid", "opacity": 0.75},
    "maintained_by": {"color": "#607d8b", "width": 1.6, "type": "solid", "opacity": 0.75},
    "has_lubrication_record": {"color": "#607d8b", "width": 1.6, "type": "solid", "opacity": 0.75},
    "has_pipe_length": {"color": "#90caf9", "width": 1.0, "type": "dotted", "opacity": 0.55},
    "has_bend_angle": {"color": "#90caf9", "width": 1.0, "type": "dotted", "opacity": 0.55},
    "has_bend_radius": {"color": "#90caf9", "width": 1.0, "type": "dotted", "opacity": 0.55},
    "has_max_deformation": {"color": "#90caf9", "width": 1.0, "type": "dotted", "opacity": 0.55},
    "has_max_equivalent_stress": {"color": "#90caf9", "width": 1.0, "type": "dotted", "opacity": 0.55},
    "has_stress_ratio": {"color": "#90caf9", "width": 1.0, "type": "dotted", "opacity": 0.55},
    "has_design_risk_score": {"color": "#90caf9", "width": 1.0, "type": "dotted", "opacity": 0.55},
    "has_design_risk_level": {"color": "#90caf9", "width": 1.0, "type": "dotted", "opacity": 0.55},
}

DEFAULT_EDGE_STYLE = {"color": "#bdbdbd", "width": 0.8, "type": "solid", "opacity": 0.4}

EDGE_LEGEND = [
    {
        "name": "TransH疑似原因关系",
        "relations": ["may_caused_by"],
        "color": "#d62728",
        "width": 3.5,
        "type": "solid",
        "description": "质量反馈指向Top6疑似根因的推理关系",
    },
    {
        "name": "RCA根因证据关系",
        "relations": ["has_root_sensor", "has_secondary_sensor", "diagnosed_root_component"],
        "color": "#c2185b",
        "width": 2.8,
        "type": "solid",
        "description": "上游根因分析、根因部件或根因传感器证据",
    },
    {
        "name": "传感器激活关系",
        "relations": ["activates_sensor"],
        "color": "#7b1fa2",
        "width": 2.2,
        "type": "dashed",
        "description": "传感器异常或激活证据关系",
    },
    {
        "name": "部件诊断/子类型候选关系",
        "relations": ["has_component_diagnosis", "has_subtype_candidate"],
        "color": "#f57c00",
        "width": 2.0,
        "type": "dashed",
        "description": "部件诊断和故障子类型候选关系",
    },
    {
        "name": "生命周期阶段关系",
        "relations": ["designed_by", "uses_material_batch", "manufactured_in", "assembled_in", "inspected_by", "maintained_by"],
        "color": "#666666",
        "width": 1.6,
        "type": "solid",
        "description": "设计、材料、制造、装配、检测、运维等生命周期关系",
    },
    {
        "name": "管路参数关系",
        "relations": ["has_pipe_length", "has_bend_angle", "has_bend_radius", "has_max_deformation", "has_max_equivalent_stress", "has_stress_ratio", "has_design_risk_score", "has_design_risk_level"],
        "color": "#90caf9",
        "width": 1.0,
        "type": "dotted",
        "description": "管路设计参数或仿真约束信息",
    },
    {
        "name": "辅助属性关系",
        "relations": ["other"],
        "color": "#bdbdbd",
        "width": 0.8,
        "type": "solid",
        "description": "故障属性、风险分箱、文本证据等辅助关系",
    },
]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Convert local graph JSON to ECharts graph option JSON.")
    parser.add_argument("--domain", choices=["bearing", "hydraulic"], required=True)
    parser.add_argument("--case_id", required=True)
    parser.add_argument("--graph_type", choices=["original", "enhanced"], required=True)
    parser.add_argument("--print_json", action="store_true", help="Print generated ECharts option JSON payload.")
    return parser.parse_args()


def normalize_case_id_alias(case_id: str) -> str:
    bearing_match = re.fullmatch(r"FB-BRG-(\d+)", case_id)
    if bearing_match:
        return f"BFB-{int(bearing_match.group(1)):05d}"
    hydraulic_match = re.fullmatch(r"FB-HYD-(\d+)", case_id)
    if hydraulic_match:
        return f"HFB-{int(hydraulic_match.group(1)):05d}"
    return case_id


def input_prefix(graph_type: str) -> str:
    return "original_graph" if graph_type == "original" else "fault_enhanced_graph"


def output_prefix(graph_type: str) -> str:
    return "echarts_original_graph" if graph_type == "original" else "echarts_fault_enhanced_graph"


def title_text(graph_type: str) -> str:
    return "原始生命周期知识图谱" if graph_type == "original" else "故障增强知识图谱"


def resolve_graph_path(domain: str, case_id: str, graph_type: str) -> Path:
    graph_dir = ROOT / "outputs" / domain / "graphs"
    prefix = input_prefix(graph_type)
    if case_id == "latest":
        files = sorted(graph_dir.glob(f"{prefix}_*.json"), key=lambda path: path.stat().st_mtime, reverse=True)
        if not files:
            raise FileNotFoundError(f"未找到 {graph_dir / (prefix + '_*.json')}，请先运行 scripts/export_graph.py。")
        return files[0]
    normalized = normalize_case_id_alias(case_id)
    path = graph_dir / f"{prefix}_{normalized}.json"
    if not path.exists():
        raise FileNotFoundError(f"输入图谱不存在: {path}\n请先运行 python scripts/export_graph.py --domain {domain} --case_id {case_id}")
    return path


def value_or_default(*values: Any, default: float = 1.0) -> float:
    for value in values:
        if value is None:
            continue
        try:
            return float(value)
        except (TypeError, ValueError):
            continue
    return default


def node_category(node: dict[str, Any]) -> str:
    node_type = str(node.get("type", ""))
    stage = str(node.get("stage", ""))
    if node.get("is_feedback") is True:
        return "feedback"
    if node.get("is_target_object") is True:
        return "target_object"
    if node.get("is_top_candidate") is True:
        return "top_candidate"
    if "Sensor" in node_type:
        return "sensor"
    if any(token in node_type for token in RCA_NODE_TYPES):
        return "rca"
    if stage in {"design", "material", "manufacturing", "assembly", "inspection", "operation"}:
        return stage
    return "other"


def symbol_size(node: dict[str, Any], category: str) -> int:
    node_type = str(node.get("type", ""))
    rank = node.get("rank")
    if node_type == "QualityFeedback" or category == "feedback":
        return 68
    if category == "target_object":
        return 62
    if node.get("is_top_candidate") is True:
        try:
            rank_int = int(rank)
        except (TypeError, ValueError):
            rank_int = 6
        if rank_int == 1:
            return 58
        if rank_int <= 3:
            return 50
        return 44
    if category == "sensor":
        return 36
    if category == "rca":
        return 38
    if category in {"design", "material", "manufacturing", "assembly", "inspection", "operation"}:
        if node_type in HIDDEN_LABEL_TYPES:
            return 24
        return 32
    if node_type in HIDDEN_LABEL_TYPES:
        return 22
    return 26


def short_node_name(node: dict[str, Any], properties: dict[str, Any]) -> str:
    node_id = str(node.get("id") or properties.get("id") or "")
    node_type = str(node.get("type") or properties.get("type") or "")
    raw_name = str(node.get("name") or properties.get("name") or node_id)
    type_label = TYPE_LABELS.get(node_type, "")
    if type_label and node_id:
        return f"{type_label} {node_id}"
    if len(raw_name) <= 24:
        return raw_name
    return f"{raw_name[:21]}..."


def top_candidate_label(node: dict[str, Any], properties: dict[str, Any]) -> str:
    rank = properties.get("rank") or node.get("rank") or ""
    prefix = f"Top{rank}" if str(rank).strip() else "Top"
    return f"{prefix} {short_node_name(node, properties)}"


def should_show_label(node: dict[str, Any], category: str, properties: dict[str, Any]) -> bool:
    node_type = str(node.get("type") or properties.get("type") or "")
    node_id = str(node.get("id") or properties.get("id") or "")
    if category in {"feedback", "target_object", "top_candidate"}:
        return True
    if properties.get("from_rca_root_component") or str(node.get("id", "")).startswith("RCA_ROOT_"):
        return True
    if node_type in VISIBLE_LABEL_TYPES:
        return True
    if node_id.startswith(("LENGTH_", "ANGLE_", "RADIUS_", "STRESS_", "DEFORMATION_")):
        return False
    if node_type in HIDDEN_LABEL_TYPES:
        return False
    return category in {"rca"}


def node_opacity(node: dict[str, Any], category: str, properties: dict[str, Any]) -> float:
    node_type = str(node.get("type") or properties.get("type") or "")
    node_id = str(node.get("id") or properties.get("id") or "")
    if category in {"feedback", "target_object", "top_candidate", "rca", "sensor"}:
        return 1.0
    if node_type in HIDDEN_LABEL_TYPES or node_id.startswith(("LENGTH_", "ANGLE_", "RADIUS_", "STRESS_", "DEFORMATION_")):
        return 0.42
    if category == "other":
        return 0.5
    return 0.92


def convert_node(node: dict[str, Any]) -> dict[str, Any]:
    category = node_category(node)
    properties = dict(node.get("properties") or {})
    for key in [
        "id",
        "name",
        "type",
        "stage",
        "stage_name",
        "is_feedback",
        "is_target_object",
        "is_top_candidate",
        "rank",
        "score",
    ]:
        if key in node:
            properties[key] = node.get(key)
    scope = properties.get("candidate_scope")
    if node.get("rank") is not None:
        properties["rank"] = node.get("rank")
    if node.get("score") is not None:
        properties["score"] = node.get("score")
    if category == "top_candidate" or node.get("is_top_candidate") is True:
        properties["is_top_candidate"] = True
    if scope in {"rca_root_component_candidate", "rca_root_component_expanded_candidate"}:
        properties["from_rca_root_component"] = True
    elif "from_rca_root_component" in properties:
        properties.pop("from_rca_root_component")
    result = {
        "id": str(node.get("id", "")),
        "name": str(node.get("name") or node.get("id", "")),
        "value": value_or_default(node.get("score"), default=1.0),
        "category": CATEGORY_LABELS.get(category, category),
        "symbolSize": symbol_size(node, category),
        "properties": properties,
        "itemStyle": {"color": CATEGORY_COLORS.get(category, CATEGORY_COLORS["other"]), "opacity": node_opacity(node, category, properties)},
    }
    properties["category_key"] = category
    if node.get("is_top_candidate") is True:
        result["label"] = {
            "show": True,
            "fontWeight": "bold",
            "formatter": top_candidate_label(node, properties),
        }
    else:
        result["label"] = {
            "show": should_show_label(node, category, properties),
            "formatter": short_node_name(node, properties),
        }
    return result


def edge_style(edge: dict[str, Any]) -> dict[str, Any]:
    relation = str(edge.get("relation", ""))
    return dict(EDGE_STYLE_RULES.get(relation, DEFAULT_EDGE_STYLE))


def relation_business_meaning(relation: str) -> str:
    if relation == "may_caused_by":
        return "该边表示 TransH 综合推理得到的疑似根因关系。"
    if relation in RCA_EDGE_RELATIONS:
        return "该边表示上游根因分析或传感器诊断证据。"
    if relation in {"designed_by", "has_pipe_design_param"}:
        return "该边表示设计阶段生命周期关系。"
    if relation in {"uses_material_batch", "supplied_by"}:
        return "该边表示材料或供应商生命周期关系。"
    if relation in {"manufactured_in", "uses_process", "processed_by"}:
        return "该边表示制造阶段生命周期关系。"
    if relation in {"assembled_in", "operated_by", "located_at"}:
        return "该边表示装配或工位相关关系。"
    if relation in {"inspected_by", "has_inspection_result", "uses_inspection_type"}:
        return "该边表示检测阶段生命周期关系。"
    if relation in {"maintained_by", "has_lubrication_record"}:
        return "该边表示使用/运维阶段生命周期关系。"
    if relation.startswith("has_pipe_") or relation in {
        "has_bend_angle",
        "has_bend_radius",
        "has_max_deformation",
        "has_max_equivalent_stress",
        "has_stress_ratio",
        "has_design_risk_score",
        "has_design_risk_level",
    }:
        return "该边表示管路设计参数关系。"
    return "该边表示辅助图谱关系。"


def convert_edge(edge: dict[str, Any]) -> dict[str, Any]:
    relation = str(edge.get("relation", ""))
    properties = dict(edge.get("properties") or {})
    if edge.get("score") is not None:
        properties["score"] = edge.get("score")
    if edge.get("weight") is not None:
        properties["weight"] = edge.get("weight")
    properties["relation"] = relation
    properties["relation_name"] = str(edge.get("relation_name") or relation)
    properties["business_meaning"] = relation_business_meaning(relation)
    style = edge_style(edge)
    return {
        "source": str(edge.get("source", "")),
        "target": str(edge.get("target", "")),
        "name": str(edge.get("relation_name") or relation),
        "value": value_or_default(edge.get("score"), edge.get("weight"), default=1.0),
        "properties": properties,
        "lineStyle": {
            "color": style["color"],
            "width": style["width"],
            "type": style["type"],
            "opacity": style["opacity"],
            "curveness": 0.12,
        },
    }


def build_categories() -> list[dict[str, Any]]:
    return [{"name": CATEGORY_LABELS[key], "key": key, "itemStyle": {"color": CATEGORY_COLORS[key]}} for key in CATEGORY_ORDER]


def build_option(graph: dict[str, Any], graph_type: str) -> dict[str, Any]:
    categories = build_categories()
    data = [convert_node(node) for node in graph.get("nodes", [])]
    links = [convert_edge(edge) for edge in graph.get("edges", [])]
    return {
        "title": {
            "text": title_text(graph_type),
            "subtext": f"{graph.get('domain', '')} | {graph.get('case_id', '')}",
        },
        "tooltip": {
            "trigger": "item",
            "confine": True,
        },
        "legend": [{"data": [CATEGORY_LABELS[key] for key in CATEGORY_ORDER]}],
        "edgeLegend": EDGE_LEGEND,
        "series": [
            {
                "name": "知识图谱",
                "type": "graph",
                "layout": "force",
                "roam": True,
                "draggable": True,
                "focusNodeAdjacency": True,
                "label": {"show": False, "position": "right", "formatter": "{b}"},
                "force": {"repulsion": 420, "edgeLength": [90, 220], "gravity": 0.06, "friction": 0.6},
                "categories": categories,
                "data": data,
                "links": links,
                "lineStyle": {"opacity": 0.55, "curveness": 0.12},
                "emphasis": {"focus": "adjacency", "lineStyle": {"width": 3}},
            }
        ],
    }


def build_echarts_option(
    graph: dict[str, Any] | None = None,
    domain: str | None = None,
    case_id: str | None = None,
    graph_type: str = "enhanced",
    save_files: bool = True,
) -> dict[str, Any]:
    if graph_type not in {"original", "enhanced"}:
        raise ValueError("graph_type must be original or enhanced")

    input_path: Path | None = None
    if graph is None:
        if domain is None or case_id is None:
            raise ValueError("domain and case_id are required when graph is not provided")
        input_path = resolve_graph_path(domain, case_id, graph_type)
        graph = json.loads(input_path.read_text(encoding="utf-8"))
    else:
        domain = domain or str(graph.get("domain", ""))
        case_id = case_id or str(graph.get("case_id", ""))

    option = build_option(graph, graph_type)
    output_case_id = graph.get("case_id", normalize_case_id_alias(str(case_id)))
    output_dir = ROOT / "outputs" / str(domain) / "graphs"
    output_path = output_dir / f"{output_prefix(graph_type)}_{output_case_id}.json"
    if input_path is not None:
        output_path = input_path.parent / output_path.name
    if save_files:
        output_path.parent.mkdir(parents=True, exist_ok=True)
        output_path.write_text(json.dumps(option, ensure_ascii=False, indent=2), encoding="utf-8")
    return {
        "domain": domain,
        "case_id": output_case_id,
        "graph_type": graph_type,
        "option": option,
        "artifact_path": str(output_path),
        "input_path": str(input_path) if input_path else "",
    }


def export(domain: str, case_id: str, graph_type: str) -> dict[str, Any]:
    result = build_echarts_option(domain=domain, case_id=case_id, graph_type=graph_type, save_files=True)
    option = result["option"]
    input_path = result.get("input_path", "")
    output_path = result["artifact_path"]
    node_count = len(option["series"][0]["data"])
    edge_count = len(option["series"][0]["links"])
    category_count = len(option["series"][0]["categories"])
    print(f"输入图谱路径: {input_path}")
    print(f"输出 ECharts option 路径: {output_path}")
    print(f"节点数量: {node_count}")
    print(f"边数量: {edge_count}")
    print(f"categories 数量: {category_count}")


    return result


def main() -> None:
    args = parse_args()
    result = export(args.domain, args.case_id, args.graph_type)
    if args.print_json:
        print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
