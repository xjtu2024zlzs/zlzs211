from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from scripts.preview_echarts_html import count_graph_items, render_html


STAGE_COLORS = {
    "design": "#6f52ed",
    "material": "#27ae60",
    "manufacturing": "#b7791f",
    "assembly": "#00a6a6",
    "inspection": "#2d9cdb",
    "operation": "#7f8c8d",
}

CATEGORY_COLORS = {
    "质量反馈": "#d94841",
    "反馈对象": "#2f80ed",
    "Top6疑似原因": "#f2994a",
    "生命周期阶段": "#607d8b",
    "反馈子系统": "#81d4fa",
    "RCA证据": "#f48fb1",
}

TYPE_LABELS = {
    "DesignSpec": "设计规范",
    "BearingDesignSpec": "设计规范",
    "PipeDesignParam": "管路设计参数",
    "MaterialBatch": "材料批次",
    "ManufacturingBatch": "制造批次",
    "AssemblyRecord": "装配记录",
    "InspectionRecord": "检测记录",
    "MaintenanceRecord": "运维记录",
}

EDGE_STYLES = {
    "occurs_on": {"color": "#2f80ed", "width": 2.0, "type": "solid", "opacity": 0.8},
    "targets_object": {"color": "#2f80ed", "width": 2.0, "type": "solid", "opacity": 0.8},
    "may_caused_by": {"color": "#d62728", "width": 3.5, "type": "solid", "opacity": 0.95},
    "belongs_to_stage": {"color": "#607d8b", "width": 1.8, "type": "solid", "opacity": 0.78},
    "feedback_to_subsystem": {"color": "#0288d1", "width": 2.0, "type": "solid", "opacity": 0.8},
    "diagnosed_root_component": {"color": "#c2185b", "width": 2.8, "type": "solid", "opacity": 0.9},
    "has_root_sensor": {"color": "#c2185b", "width": 2.8, "type": "solid", "opacity": 0.9},
    "has_secondary_sensor": {"color": "#c2185b", "width": 2.2, "type": "dashed", "opacity": 0.85},
}

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
        "relations": ["diagnosed_root_component", "has_root_sensor", "has_secondary_sensor"],
        "color": "#c2185b",
        "width": 2.8,
        "type": "solid",
        "description": "上游根因分析、根因部件或根因传感器证据",
    },
    {
        "name": "生命周期阶段关系",
        "relations": ["belongs_to_stage"],
        "color": "#607d8b",
        "width": 1.8,
        "type": "solid",
        "description": "Top6疑似原因所属生命周期阶段",
    },
    {
        "name": "重点反馈子系统关系",
        "relations": ["feedback_to_subsystem"],
        "color": "#0288d1",
        "width": 2.0,
        "type": "solid",
        "description": "重点生命周期阶段建议反馈至相关子系统",
    },
    {
        "name": "反馈对象关系",
        "relations": ["occurs_on", "targets_object"],
        "color": "#2f80ed",
        "width": 2.0,
        "type": "solid",
        "description": "质量反馈定位到发生故障的反馈对象",
    },
]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Export a business-friendly Top6 evidence graph.")
    parser.add_argument("--domain", choices=["bearing", "hydraulic"], required=True)
    parser.add_argument("--case_id", required=True)
    return parser.parse_args()


def normalize_case_id_alias(case_id: str) -> str:
    bearing_match = re.fullmatch(r"FB-BRG-(\d+)", case_id)
    if bearing_match:
        return f"BFB-{int(bearing_match.group(1)):05d}"
    hydraulic_match = re.fullmatch(r"FB-HYD-(\d+)", case_id)
    if hydraulic_match:
        return f"HFB-{int(hydraulic_match.group(1)):05d}"
    return case_id


def require_file(path: Path) -> Path:
    if not path.exists():
        raise FileNotFoundError(f"Missing reasoning JSON: {path}")
    return path


def relation_meaning(relation: str) -> str:
    if relation == "may_caused_by":
        return "该边表示 TransH 综合推理得到的疑似根因关系。"
    if relation in {"diagnosed_root_component", "has_root_sensor", "has_secondary_sensor"}:
        return "该边表示上游根因分析或传感器诊断证据。"
    if relation == "belongs_to_stage":
        return "该边表示疑似原因所属生命周期阶段。"
    if relation == "feedback_to_subsystem":
        return "该边表示建议反馈复核的业务子系统。"
    return "该边表示 Top6 证据链关系。"


def add_node(nodes: dict[str, dict[str, Any]], node: dict[str, Any]) -> None:
    nodes.setdefault(str(node["id"]), node)


def add_edge(edges: list[dict[str, Any]], source: str, target: str, relation: str, relation_name: str, **properties: Any) -> None:
    edges.append(
        {
            "source": source,
            "target": target,
            "relation": relation,
            "relation_name": relation_name,
            "properties": {"relation": relation, "relation_name": relation_name, "business_meaning": relation_meaning(relation), **properties},
        }
    )


def stage_node_id(stage: str) -> str:
    return f"STAGE_{stage}"


def subsystem_node_id(name: str) -> str:
    safe = re.sub(r"[^0-9A-Za-z\u4e00-\u9fff]+", "_", name).strip("_")
    return f"SUBSYSTEM_{safe or 'unknown'}"


def candidate_label(candidate_type: str, candidate_id: str, rank: int) -> str:
    type_label = TYPE_LABELS.get(candidate_type, candidate_type)
    return f"Top{rank} {type_label} {candidate_id}"


def build_raw_graph(domain: str, reasoning: dict[str, Any]) -> dict[str, Any]:
    feedback = reasoning["feedback_summary"]
    target = reasoning["target_object"]
    stage_attr = reasoning.get("stage_attribution", {})
    rca_context = reasoning.get("rca_context", {})
    nodes: dict[str, dict[str, Any]] = {}
    edges: list[dict[str, Any]] = []

    feedback_id = str(feedback["feedback_id"])
    target_id = str(target["object_id"])
    add_node(
        nodes,
        {
            "id": feedback_id,
            "name": f"质量反馈 {feedback_id}",
            "type": "QualityFeedback",
            "category": "质量反馈",
            "symbolSize": 72,
            "properties": {**feedback, "is_feedback": True, "is_top_candidate": False},
        },
    )
    add_node(
        nodes,
        {
            "id": target_id,
            "name": f"{target.get('object_name', target_id)} {target_id}",
            "type": str(target.get("object_type", "")),
            "category": "反馈对象",
            "symbolSize": 64,
            "properties": {**target, "is_target_object": True, "is_top_candidate": False},
        },
    )
    add_edge(edges, feedback_id, target_id, "occurs_on", "发生于")

    stage_to_subsystem: dict[str, str] = {}
    for item in stage_attr.get("focus_stage_top3", []) or []:
        if isinstance(item, dict):
            stage = str(item.get("stage", ""))
            subsystem = str(item.get("feedback_subsystem", ""))
            if stage and subsystem:
                stage_to_subsystem[stage] = subsystem

    top6 = reasoning.get("top6_candidates", [])[:6]
    for candidate in top6:
        rank = int(candidate.get("rank", len(nodes)) or 6)
        cid = str(candidate.get("candidate_id", ""))
        ctype = str(candidate.get("candidate_type", ""))
        stage = str(candidate.get("candidate_stage", ""))
        stage_name = str(candidate.get("candidate_stage_name", stage))
        score = candidate.get("final_score", candidate.get("adjusted_final_score", ""))
        size = 60 if rank == 1 else 52 if rank <= 3 else 46
        add_node(
            nodes,
            {
                "id": cid,
                "name": candidate_label(ctype, cid, rank),
                "type": ctype,
                "stage": stage,
                "stage_name": stage_name,
                "category": "Top6疑似原因",
                "symbolSize": size,
                "properties": {**candidate, "is_top_candidate": True},
            },
        )
        add_edge(edges, feedback_id, cid, "may_caused_by", "疑似原因", rank=rank, score=score)

        sid = stage_node_id(stage)
        add_node(
            nodes,
            {
                "id": sid,
                "name": stage_name,
                "type": "LifecycleStage",
                "stage": stage,
                "stage_name": stage_name,
                "category": "生命周期阶段",
                "symbolSize": 38,
                "properties": {"stage": stage, "stage_name": stage_name, "is_top_candidate": False},
            },
        )
        add_edge(edges, cid, sid, "belongs_to_stage", "所属阶段", rank=rank)

    for item in stage_attr.get("focus_stage_top3", []) or []:
        if not isinstance(item, dict):
            continue
        stage = str(item.get("stage", ""))
        stage_name = str(item.get("stage_name", stage))
        subsystem = str(item.get("feedback_subsystem", ""))
        if not stage:
            continue
        sid = stage_node_id(stage)
        add_node(
            nodes,
            {
                "id": sid,
                "name": stage_name,
                "type": "LifecycleStage",
                "stage": stage,
                "stage_name": stage_name,
                "category": "生命周期阶段",
                "symbolSize": 42,
                "properties": {"stage": stage, "stage_name": stage_name, "stage_score": item.get("score"), "is_focus_stage_top3": True},
            },
        )
        if subsystem:
            sub_id = subsystem_node_id(subsystem)
            add_node(
                nodes,
                {
                    "id": sub_id,
                    "name": subsystem,
                    "type": "TargetSubsystem",
                    "category": "反馈子系统",
                    "symbolSize": 40,
                    "properties": {"feedback_subsystem": subsystem, "stage": stage, "stage_name": stage_name},
                },
            )
            add_edge(edges, sid, sub_id, "feedback_to_subsystem", "反馈至子系统")

    for subsystem in stage_attr.get("feedback_subsystem_top3", []) or []:
        subsystem = str(subsystem)
        if subsystem:
            add_node(
                nodes,
                {
                    "id": subsystem_node_id(subsystem),
                    "name": subsystem,
                    "type": "TargetSubsystem",
                    "category": "反馈子系统",
                    "symbolSize": 40,
                    "properties": {"feedback_subsystem": subsystem},
                },
            )

    if domain == "hydraulic" and rca_context:
        root_component_code = str(rca_context.get("root_component_code", ""))
        root_component_name = str(rca_context.get("root_component_name", root_component_code))
        if root_component_code:
            rid = f"RCA_ROOT_{root_component_code}"
            add_node(
                nodes,
                {
                    "id": rid,
                    "name": f"RCA根因部件 {root_component_name}",
                    "type": "HydraulicComponent",
                    "category": "RCA证据",
                    "symbolSize": 44,
                    "properties": {"root_component_code": root_component_code, "root_component_name": root_component_name, "score": rca_context.get("component_confidence")},
                },
            )
            add_edge(edges, feedback_id, rid, "diagnosed_root_component", "RCA根因部件", score=rca_context.get("component_confidence"))
        for key, relation, label in [
            ("root_sensor", "has_root_sensor", "根因传感器"),
            ("secondary_sensor", "has_secondary_sensor", "次因传感器"),
        ]:
            sensor = str(rca_context.get(key, ""))
            if sensor:
                sid = f"RCA_SENSOR_{sensor}"
                add_node(
                    nodes,
                    {
                        "id": sid,
                        "name": f"{label} {sensor}",
                        "type": "Sensor",
                        "category": "RCA证据",
                        "symbolSize": 42 if key == "root_sensor" else 38,
                        "properties": {"sensor": sensor, "score": rca_context.get(f"{key}_confidence")},
                    },
                )
                add_edge(edges, feedback_id, sid, relation, label, score=rca_context.get(f"{key}_confidence"))

    return {
        "domain": domain,
        "case_id": reasoning["case_id"],
        "graph_type": "top6_evidence",
        "summary": {"node_count": len(nodes), "edge_count": len(edges), "top6_count": len(top6)},
        "nodes": list(nodes.values()),
        "edges": edges,
    }


def edge_style(relation: str) -> dict[str, Any]:
    return dict(EDGE_STYLES.get(relation, {"color": "#bdbdbd", "width": 1.0, "type": "solid", "opacity": 0.5}))


def node_color(node: dict[str, Any]) -> str:
    if node.get("category") == "生命周期阶段":
        return STAGE_COLORS.get(str(node.get("stage", "")), CATEGORY_COLORS["生命周期阶段"])
    return CATEGORY_COLORS.get(str(node.get("category", "")), "#8f8f8f")


def build_echarts_option(graph: dict[str, Any]) -> dict[str, Any]:
    categories = [{"name": name, "itemStyle": {"color": color}} for name, color in CATEGORY_COLORS.items()]
    data = []
    for node in graph["nodes"]:
        data.append(
            {
                "id": str(node["id"]),
                "name": str(node["name"]),
                "category": str(node["category"]),
                "symbolSize": node.get("symbolSize", 36),
                "value": node.get("properties", {}).get("score", 1.0),
                "properties": node.get("properties", {}),
                "itemStyle": {"color": node_color(node)},
                "label": {"show": True, "formatter": str(node["name"]), "fontWeight": "bold" if node.get("category") in {"质量反馈", "Top6疑似原因"} else "normal"},
            }
        )
    links = []
    for edge in graph["edges"]:
        style = edge_style(str(edge["relation"]))
        links.append(
            {
                "source": str(edge["source"]),
                "target": str(edge["target"]),
                "name": str(edge["relation_name"]),
                "value": edge.get("properties", {}).get("score", 1.0),
                "properties": edge.get("properties", {}),
                "lineStyle": {"color": style["color"], "width": style["width"], "type": style["type"], "opacity": style["opacity"], "curveness": 0.1},
            }
        )
    return {
        "title": {"text": "Top6 证据链简图", "subtext": f"{graph['domain']} | {graph['case_id']}"},
        "tooltip": {"trigger": "item", "confine": True},
        "legend": [{"data": list(CATEGORY_COLORS.keys())}],
        "edgeLegend": EDGE_LEGEND,
        "series": [
            {
                "name": "Top6证据链",
                "type": "graph",
                "layout": "force",
                "roam": True,
                "draggable": True,
                "focusNodeAdjacency": True,
                "label": {"show": True, "position": "right"},
                "force": {"repulsion": 520, "edgeLength": [110, 240], "gravity": 0.05, "friction": 0.6},
                "categories": categories,
                "data": data,
                "links": links,
                "lineStyle": {"curveness": 0.1},
                "emphasis": {"focus": "adjacency", "lineStyle": {"width": 4}},
            }
        ],
    }


def export(domain: str, case_id_arg: str) -> tuple[Path, Path, Path]:
    case_id = normalize_case_id_alias(case_id_arg)
    report_dir = ROOT / "outputs" / domain / "reports"
    graph_dir = ROOT / "outputs" / domain / "graphs"
    reasoning_path = require_file(report_dir / f"reasoning_{case_id}.json")
    reasoning = json.loads(reasoning_path.read_text(encoding="utf-8"))
    graph = build_raw_graph(domain, reasoning)
    option = build_echarts_option(graph)
    raw_path = graph_dir / f"top6_evidence_graph_{case_id}.json"
    option_path = graph_dir / f"echarts_top6_evidence_graph_{case_id}.json"
    html_path = graph_dir / f"preview_top6_evidence_graph_{case_id}.html"
    graph_dir.mkdir(parents=True, exist_ok=True)
    raw_path.write_text(json.dumps(graph, ensure_ascii=False, indent=2), encoding="utf-8")
    option_path.write_text(json.dumps(option, ensure_ascii=False, indent=2), encoding="utf-8")
    html_path.write_text(render_html(domain, case_id, "top6_evidence", option, count_graph_items(option)), encoding="utf-8")
    return raw_path, option_path, html_path


def main() -> None:
    args = parse_args()
    raw_path, option_path, html_path = export(args.domain, args.case_id)
    print(f"Top6 evidence graph JSON: {raw_path}")
    print(f"Top6 evidence ECharts option JSON: {option_path}")
    print(f"Top6 evidence HTML preview: {html_path}")


if __name__ == "__main__":
    main()
