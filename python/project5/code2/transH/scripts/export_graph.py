from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from typing import Any

import pandas as pd


ROOT = Path(__file__).resolve().parents[1]

STAGE_NAMES = {
    "design": "设计阶段",
    "material": "材料阶段",
    "manufacturing": "制造阶段",
    "assembly": "装配阶段",
    "inspection": "检测阶段",
    "operation": "使用/运维阶段",
    "feedback": "反馈阶段",
    "fault": "故障信息",
    "rca": "RCA 诊断",
    "unknown": "未知阶段",
}

TYPE_STAGE = {
    "DesignSpec": "design",
    "BearingDesignSpec": "design",
    "PipeDesignParam": "design",
    "PipeParameter": "design",
    "MaterialBatch": "material",
    "Supplier": "material",
    "ManufacturingBatch": "manufacturing",
    "ManufacturingProcess": "manufacturing",
    "GrindingProcess": "manufacturing",
    "HeatTreatmentProcess": "manufacturing",
    "Equipment": "manufacturing",
    "AssemblyRecord": "assembly",
    "FitToleranceRecord": "assembly",
    "Station": "assembly",
    "Operator": "operation",
    "InspectionRecord": "inspection",
    "VibrationInspectionRecord": "inspection",
    "InspectionEquipment": "inspection",
    "LubricationRecord": "operation",
    "MaintenanceRecord": "operation",
    "QualityFeedback": "feedback",
    "FaultType": "fault",
    "FaultPosition": "fault",
    "FaultSeverity": "fault",
    "Sensor": "rca",
    "ComponentDiagnosis": "rca",
    "FaultSubtype": "rca",
    "SensorEnergy": "rca",
    "EnergyLevel": "rca",
    "HydraulicComponent": "operation",
    "Bearing": "operation",
}

RELATION_NAMES = {
    "occurs_on": "发生于",
    "designed_by": "设计依据",
    "uses_material_batch": "使用材料批次",
    "manufactured_in": "制造批次",
    "assembled_in": "装配记录",
    "inspected_by": "检测记录",
    "maintained_by": "运维记录",
    "has_pipe_design_param": "管路设计参数",
    "monitored_by": "监测传感器",
    "supplied_by": "供应商",
    "processed_by": "加工设备",
    "uses_process": "使用工艺",
    "operated_by": "操作人员",
    "located_at": "所在位置",
    "uses_inspection_type": "检测类型",
    "may_caused_by": "疑似原因",
    "has_fault_type": "故障类型",
    "has_fault_severity": "故障严重度",
    "has_fault_position": "故障位置",
    "has_rca_text": "RCA 文本",
    "has_fault_evolution_text": "故障演化文本",
    "has_root_sensor": "根因传感器",
    "has_secondary_sensor": "次级传感器",
    "diagnosed_root_component": "诊断根因部件",
    "has_component_diagnosis": "部件诊断",
    "has_subtype_candidate": "故障子类型候选",
    "activates_sensor": "激活传感器",
    "has_conclusion": "推理结论",
    "routes_to_subsystem": "反馈子系统",
}

HYDRAULIC_NAMES = {
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

LIFECYCLE_RELATIONS = [
    "designed_by",
    "uses_material_batch",
    "manufactured_in",
    "assembled_in",
    "inspected_by",
    "maintained_by",
    "has_pipe_design_param",
    "monitored_by",
]

BEARING_EXPAND_RELATIONS = ["supplied_by", "processed_by", "uses_process", "operated_by", "located_at", "uses_inspection_type"]
HYDRAULIC_EXPAND_RELATIONS = ["supplied_by", "processed_by", "uses_process", "operated_by", "located_at"]
PIPE_PARAM_RELATIONS = {
    "has_pipe_length_L1",
    "has_pipe_length_L2",
    "has_pipe_length_L3",
    "has_bend_angle_theta1",
    "has_bend_angle_theta2",
    "has_bend_radius_R",
    "has_max_deformation",
    "has_max_equivalent_stress",
    "has_design_risk_level",
    "has_design_risk_score",
}

CONTEXT_ATTRIBUTE_RELATIONS = {
    "has_overall_lifecycle_risk_level",
    "has_overall_lifecycle_risk_score",
    "has_bearing_model",
    "has_lubrication_method",
    "has_design_risk_level",
    "has_design_risk_score",
    "has_material_grade",
    "has_material_risk_level",
    "has_manufacturing_risk_level",
    "has_process_risk_level",
    "has_assembly_risk_level",
    "has_inspection_result",
    "has_inspection_risk_level",
    "has_component_code",
    "has_component_name",
    "belongs_to_category",
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Export local graph JSON for original and fault-enhanced lifecycle graphs.")
    parser.add_argument("--domain", choices=["bearing", "hydraulic"], required=True)
    parser.add_argument("--case_id", required=True)
    parser.add_argument("--print_json", action="store_true", help="Print generated graph JSON payload.")
    return parser.parse_args()


def normalize_case_id_alias(case_id: str) -> str:
    bearing_match = re.fullmatch(r"FB-BRG-(\d+)", case_id)
    if bearing_match:
        return f"BFB-{int(bearing_match.group(1)):05d}"
    hydraulic_match = re.fullmatch(r"FB-HYD-(\d+)", case_id)
    if hydraulic_match:
        return f"HFB-{int(hydraulic_match.group(1)):05d}"
    return case_id


def require_file(path: Path, hint: str) -> Path:
    if not path.exists():
        raise FileNotFoundError(f"缺少文件: {path}\n请先完成: {hint}")
    return path


def reasoning_path(domain: str, case_id: str) -> Path:
    report_dir = ROOT / "outputs" / domain / "reports"
    if case_id == "latest":
        files = sorted(report_dir.glob("reasoning_*.json"), key=lambda p: p.stat().st_mtime, reverse=True)
        if not files:
            raise FileNotFoundError(f"{report_dir} 下没有 reasoning_*.json，请先运行 scripts/run_reasoning.py。")
        return files[0]
    normalized = normalize_case_id_alias(case_id)
    path = report_dir / f"reasoning_{normalized}.json"
    return require_file(path, f"python scripts/run_reasoning.py --domain {domain} --case_id {case_id}")


def load_inputs(domain: str, case_id: str) -> dict[str, Any]:
    data_dir = ROOT / "data" / domain
    triples_path = require_file(data_dir / f"{domain}_triples.csv", f"python scripts/build_triples.py --domain {domain}")
    rp = reasoning_path(domain, case_id)
    loaded: dict[str, Any] = {
        "triples": pd.read_csv(triples_path, encoding="utf-8-sig"),
        "reasoning": json.loads(rp.read_text(encoding="utf-8")),
        "reasoning_path": rp,
    }
    evidence_path = data_dir / f"{domain}_evidence.csv"
    loaded["evidence"] = pd.read_csv(evidence_path, encoding="utf-8-sig") if evidence_path.exists() else pd.DataFrame()
    if domain == "hydraulic":
        loaded["rca"] = pd.read_csv(require_file(data_dir / "hydraulic_rca_case.csv", "python scripts/generate_hydraulic_data.py"), encoding="utf-8-sig")
        loaded["diagnosis"] = pd.read_csv(require_file(data_dir / "hydraulic_component_diagnosis.csv", "python scripts/generate_hydraulic_data.py"), encoding="utf-8-sig")
        loaded["subtype"] = pd.read_csv(require_file(data_dir / "hydraulic_subtype_probability.csv", "python scripts/generate_hydraulic_data.py"), encoding="utf-8-sig")
        loaded["sensor"] = pd.read_csv(require_file(data_dir / "hydraulic_sensor_energy.csv", "python scripts/generate_hydraulic_data.py"), encoding="utf-8-sig")
    return loaded


def sanitize_name(text: str) -> str:
    return str(text).replace("调速阀/节流阀", "节流阀").replace("密封件/管路总成", "管路总成")


def stage_for_type(node_type: str) -> str:
    return TYPE_STAGE.get(node_type, "unknown")


def readable_name(node_id: str, node_type: str, properties: dict[str, Any] | None = None) -> str:
    props = properties or {}
    if "name" in props and props["name"]:
        return sanitize_name(str(props["name"]))
    if node_type == "QualityFeedback":
        return f"质量反馈 {node_id}"
    if node_type == "HydraulicComponent":
        code = str(props.get("component_code", ""))
        if code in HYDRAULIC_NAMES:
            return f"{HYDRAULIC_NAMES[code]} {node_id}"
    if node_type == "Bearing":
        return f"轴承 {node_id}"
    if node_type == "Sensor":
        return f"传感器 {node_id}"
    type_names = {
        "PipeDesignParam": "管路设计参数",
        "MaterialBatch": "材料批次",
        "Supplier": "供应商",
        "ManufacturingBatch": "制造批次",
        "ManufacturingProcess": "制造工艺",
        "HeatTreatmentProcess": "热处理工艺",
        "GrindingProcess": "磨削工艺",
        "Equipment": "设备",
        "AssemblyRecord": "装配记录",
        "Station": "工位",
        "Operator": "操作员",
        "InspectionRecord": "检测记录",
        "VibrationInspectionRecord": "振动检测",
        "MaintenanceRecord": "运维记录",
        "DesignSpec": "设计规范",
        "BearingDesignSpec": "轴承设计规范",
        "FaultType": "故障类型",
        "FaultPosition": "故障位置",
        "FaultSeverity": "故障严重度",
        "RcaText": "RCA 文本证据",
        "FaultEvolutionText": "故障演化证据",
        "ComponentDiagnosis": "部件诊断",
        "FaultSubtype": "故障子类型",
        "SensorEnergy": "传感器能量",
        "EnergyLevel": "能量等级",
        "PipeParameter": "管路参数",
    }
    prefix = type_names.get(node_type, node_type)
    return sanitize_name(f"{prefix} {node_id}")


class GraphBuilder:
    def __init__(self, domain: str, case_id: str, graph_type: str, reasoning: dict[str, Any]) -> None:
        self.domain = domain
        self.case_id = case_id
        self.graph_type = graph_type
        self.reasoning = reasoning
        self.nodes: dict[str, dict[str, Any]] = {}
        self.edges: dict[tuple[str, str, str], dict[str, Any]] = {}

    def add_node(
        self,
        node_id: str,
        node_type: str,
        *,
        name: str | None = None,
        stage: str | None = None,
        is_feedback: bool = False,
        is_target_object: bool = False,
        is_top_candidate: bool = False,
        rank: int | None = None,
        score: float | None = None,
        properties: dict[str, Any] | None = None,
    ) -> None:
        props = properties or {}
        current = self.nodes.get(node_id, {})
        node_stage = stage or current.get("stage") or stage_for_type(node_type)
        merged_props = dict(current.get("properties", {}))
        merged_props.update(props)
        self.nodes[node_id] = {
            "id": node_id,
            "name": name or current.get("name") or readable_name(node_id, node_type, merged_props),
            "type": node_type,
            "stage": node_stage,
            "stage_name": STAGE_NAMES.get(node_stage, "未知阶段"),
            "is_feedback": bool(current.get("is_feedback", False) or is_feedback),
            "is_target_object": bool(current.get("is_target_object", False) or is_target_object),
            "is_top_candidate": bool(current.get("is_top_candidate", False) or is_top_candidate),
            "rank": rank if rank is not None else current.get("rank"),
            "score": score if score is not None else current.get("score"),
            "properties": merged_props,
        }

    def add_edge(
        self,
        source: str,
        target: str,
        relation: str,
        *,
        weight: float = 1.0,
        score: float | None = None,
        properties: dict[str, Any] | None = None,
    ) -> None:
        key = (source, target, relation)
        if key not in self.edges:
            self.edges[key] = {
                "source": source,
                "target": target,
                "relation": relation,
                "relation_name": RELATION_NAMES.get(relation, relation),
                "weight": float(weight),
                "score": score,
                "properties": properties or {},
            }
        else:
            self.edges[key]["properties"].update(properties or {})
            if score is not None:
                self.edges[key]["score"] = score

    def to_json(self) -> dict[str, Any]:
        target = self.reasoning.get("target_object", {}).get("object_id")
        top1 = None
        if self.reasoning.get("top6_candidates"):
            top1 = self.reasoning["top6_candidates"][0]["candidate_id"]
        summary = {
            "node_count": len(self.nodes),
            "edge_count": len(self.edges),
            "primary_stage": self.reasoning.get("stage_attribution", {}).get("primary_stage"),
            "top1_candidate": top1,
            "target_object": target,
        }
        return {
            "domain": self.domain,
            "case_id": self.case_id,
            "graph_type": self.graph_type,
            "nodes": sorted(self.nodes.values(), key=lambda n: (not n["is_feedback"], not n["is_target_object"], n["rank"] or 999, n["id"])),
            "edges": list(self.edges.values()),
            "summary": summary,
        }


def entity_type_map(triples: pd.DataFrame) -> dict[str, str]:
    mapping: dict[str, str] = {}
    for entity, entity_type in triples[["head", "head_type"]].drop_duplicates().itertuples(index=False):
        mapping.setdefault(str(entity), str(entity_type))
    for entity, entity_type in triples[["tail", "tail_type"]].drop_duplicates().itertuples(index=False):
        mapping[str(entity)] = str(entity_type)
    return mapping


def enrich_target_properties(domain: str, target: dict[str, Any], triples: pd.DataFrame) -> dict[str, Any]:
    props: dict[str, Any] = {}
    if domain == "hydraulic":
        rows = triples[(triples["head"].astype(str) == target["object_id"]) & (triples["relation"] == "has_component_code")]
        if not rows.empty:
            code = str(rows.iloc[0]["tail"])
            props["component_code"] = code
            props["name"] = f"{HYDRAULIC_NAMES.get(code, target.get('object_name', target['object_id']))} {target['object_id']}"
    return props


def add_triple_edge(builder: GraphBuilder, row: Any) -> None:
    builder.add_node(str(row.head), str(row.head_type))
    builder.add_node(str(row.tail), str(row.tail_type))
    builder.add_edge(str(row.head), str(row.tail), str(row.relation), weight=float(row.weight))


def add_original_graph(builder: GraphBuilder, triples: pd.DataFrame, domain: str, reasoning: dict[str, Any]) -> set[str]:
    target = reasoning["target_object"]
    target_props = enrich_target_properties(domain, target, triples)
    builder.add_node(
        target["object_id"],
        target["object_type"],
        name=target_props.get("name") or target.get("object_name"),
        is_target_object=True,
        properties=target_props,
    )
    lifecycle_ids: set[str] = set()
    target_rows = triples[(triples["head"].astype(str) == target["object_id"]) & (triples["relation"].isin(LIFECYCLE_RELATIONS))]
    if domain == "bearing":
        target_rows = target_rows[target_rows["relation"] != "monitored_by"]
    for row in target_rows.itertuples(index=False):
        add_triple_edge(builder, row)
        lifecycle_ids.add(str(row.tail))

    expand_relations = BEARING_EXPAND_RELATIONS if domain == "bearing" else HYDRAULIC_EXPAND_RELATIONS
    expand_rows = triples[(triples["head"].astype(str).isin(lifecycle_ids)) & (triples["relation"].isin(expand_relations))]
    for row in expand_rows.itertuples(index=False):
        add_triple_edge(builder, row)

    if domain == "hydraulic":
        pipe_ids = set(target_rows.loc[target_rows["relation"] == "has_pipe_design_param", "tail"].astype(str))
        if pipe_ids:
            pipe_rows = triples[(triples["head"].astype(str).isin(pipe_ids)) & (triples["relation"].isin(PIPE_PARAM_RELATIONS))]
            for row in pipe_rows.head(20).itertuples(index=False):
                add_triple_edge(builder, row)
    add_context_attributes(builder, triples, max_edges=35)
    return lifecycle_ids


def add_context_attributes(builder: GraphBuilder, triples: pd.DataFrame, max_edges: int) -> None:
    added = 0
    seen_heads = set(builder.nodes)
    rows = triples[(triples["head"].astype(str).isin(seen_heads)) & (triples["relation"].isin(CONTEXT_ATTRIBUTE_RELATIONS))]
    for row in rows.itertuples(index=False):
        if added >= max_edges:
            break
        add_triple_edge(builder, row)
        added += 1


def add_feedback_fault_nodes(builder: GraphBuilder, triples: pd.DataFrame, reasoning: dict[str, Any]) -> None:
    feedback = reasoning["feedback_summary"]
    feedback_id = feedback["feedback_id"]
    builder.add_node(feedback_id, "QualityFeedback", is_feedback=True, properties=feedback)
    target_id = reasoning["target_object"]["object_id"]
    builder.add_edge(feedback_id, target_id, "occurs_on", weight=1.0)
    rows = triples[
        (triples["head"].astype(str) == feedback_id)
        & (
            triples["relation"].isin(
                [
                    "has_fault_type",
                    "located_at",
                    "has_fault_severity",
                    "has_diagnosis_confidence",
                    "has_rca_confidence",
                    "attributed_to_stage",
                    "has_rca_text",
                    "has_fault_evolution_text",
                ]
            )
        )
    ]
    for row in rows.itertuples(index=False):
        add_triple_edge(builder, row)


def add_top6(builder: GraphBuilder, reasoning: dict[str, Any]) -> None:
    feedback_id = reasoning["feedback_summary"]["feedback_id"]
    for item in reasoning.get("top6_candidates", []):
        builder.add_node(
            item["candidate_id"],
            item["candidate_type"],
            stage=item["candidate_stage"],
            is_top_candidate=True,
            rank=int(item["rank"]),
            score=float(item["final_score"]),
            properties={
                "candidate_scope": item["candidate_scope"],
                "transh_score": item["transh_score"],
                "path_score": item["path_score"],
                "evidence_score": item["evidence_score"],
                "rule_prior_score": item["rule_prior_score"],
                "reason_description": item["reason_description"],
            },
        )
        builder.add_edge(
            feedback_id,
            item["candidate_id"],
            "may_caused_by",
            weight=1.0,
            score=float(item["final_score"]),
            properties={"rank": item["rank"], "candidate_scope": item["candidate_scope"]},
        )


def add_top_candidate_context(builder: GraphBuilder, triples: pd.DataFrame, reasoning: dict[str, Any], domain: str, max_edges: int = 30) -> None:
    top_ids = [item["candidate_id"] for item in reasoning.get("top6_candidates", [])]
    if not top_ids:
        return
    relations = set(BEARING_EXPAND_RELATIONS if domain == "bearing" else HYDRAULIC_EXPAND_RELATIONS)
    relations.update(CONTEXT_ATTRIBUTE_RELATIONS)
    rows = triples[(triples["head"].astype(str).isin(top_ids)) & (triples["relation"].isin(relations))]
    added = 0
    for row in rows.itertuples(index=False):
        if added >= max_edges:
            break
        add_triple_edge(builder, row)
        added += 1


def add_stage_nodes(builder: GraphBuilder, reasoning: dict[str, Any]) -> None:
    feedback_id = reasoning["feedback_summary"]["feedback_id"]
    for item in reasoning.get("stage_attribution", {}).get("stage_rank", []):
        if float(item["score"]) <= 0:
            continue
        node_id = f"STAGE_{item['stage']}"
        builder.add_node(
            node_id,
            "LifecycleStage",
            name=item["stage_name"],
            stage=item["stage"],
            score=float(item["score"]),
            properties={"stage_score": item["score"]},
        )
        builder.add_edge(feedback_id, node_id, "attributed_to_stage", weight=1.0, score=float(item["score"]))


def add_conclusion_nodes(builder: GraphBuilder, reasoning: dict[str, Any]) -> None:
    feedback_id = reasoning["feedback_summary"]["feedback_id"]
    conclusion_id = f"CONCLUSION_{feedback_id}"
    conclusion_text = reasoning.get("conclusion_text", "")
    builder.add_node(
        conclusion_id,
        "ReasoningConclusion",
        name=f"推理结论 {feedback_id}",
        stage="feedback",
        properties={"conclusion_text": conclusion_text},
    )
    builder.add_edge(feedback_id, conclusion_id, "has_conclusion", weight=1.0)
    subsystem = reasoning.get("stage_attribution", {}).get("feedback_target_subsystem")
    if subsystem:
        subsystem_id = f"SUBSYSTEM_{reasoning['stage_attribution']['primary_stage']}"
        builder.add_node(subsystem_id, "TargetSubsystem", name=subsystem, stage=reasoning["stage_attribution"]["primary_stage"])
        builder.add_edge(feedback_id, subsystem_id, "routes_to_subsystem", weight=1.0)


def add_evidence(builder: GraphBuilder, evidence: pd.DataFrame, feedback_id: str) -> None:
    if evidence.empty:
        return
    rows = evidence[evidence["feedback_id"].astype(str) == feedback_id]
    for row in rows.itertuples(index=False):
        node_type = "RcaText" if str(row.evidence_type) == "rca_text" else "FaultEvolutionText"
        builder.add_node(
            str(row.evidence_id),
            node_type,
            name=f"{'RCA文本' if node_type == 'RcaText' else '演化文本'} {row.feedback_id}",
            stage="feedback",
            properties={"evidence_type": row.evidence_type, "evidence_text": row.evidence_text, "confidence": float(row.confidence)},
        )
        relation = "has_rca_text" if node_type == "RcaText" else "has_fault_evolution_text"
        builder.add_edge(str(row.feedback_id), str(row.evidence_id), relation, weight=float(row.confidence), score=float(row.confidence))


def add_hydraulic_enhancement(builder: GraphBuilder, data: dict[str, Any], reasoning: dict[str, Any]) -> None:
    feedback_id = reasoning["feedback_summary"]["feedback_id"]
    ctx = reasoning.get("rca_context", {})
    if not ctx:
        return
    root_sensor = ctx.get("root_sensor")
    secondary_sensor = ctx.get("secondary_sensor")
    if root_sensor:
        builder.add_node(str(root_sensor), "Sensor", score=float(ctx.get("root_sensor_confidence", 0.0)))
        builder.add_edge(feedback_id, str(root_sensor), "has_root_sensor", weight=float(ctx.get("root_sensor_confidence", 1.0)), score=float(ctx.get("root_sensor_confidence", 0.0)))
    if secondary_sensor:
        builder.add_node(str(secondary_sensor), "Sensor", score=float(ctx.get("secondary_sensor_confidence", 0.0)))
        builder.add_edge(feedback_id, str(secondary_sensor), "has_secondary_sensor", weight=float(ctx.get("secondary_sensor_confidence", 1.0)), score=float(ctx.get("secondary_sensor_confidence", 0.0)))

    root_code = str(ctx.get("root_component_code", ""))
    root_name = HYDRAULIC_NAMES.get(root_code, sanitize_name(str(ctx.get("root_component_name", root_code))))
    root_node = f"RCA_ROOT_{root_code}" if root_code else "RCA_ROOT_COMPONENT"
    builder.add_node(root_node, "HydraulicComponent", name=f"{root_name} RCA根因部件", stage="operation", score=float(ctx.get("component_confidence", 0.0)), properties={"component_code": root_code, "root_component_name": root_name})
    builder.add_edge(feedback_id, root_node, "diagnosed_root_component", weight=float(ctx.get("component_confidence", 1.0)), score=float(ctx.get("component_confidence", 0.0)))

    for row in ctx.get("component_diagnosis_top3", [])[:5]:
        diag_id = f"DIAG_{ctx.get('case_id')}_{row['component_code']}"
        builder.add_node(
            diag_id,
            "ComponentDiagnosis",
            name=f"{HYDRAULIC_NAMES.get(str(row['component_code']), row['component_name'])} 诊断{row['rank_no']}",
            stage="rca",
            score=float(row["severity"]),
            properties=row,
        )
        builder.add_edge(feedback_id, diag_id, "has_component_diagnosis", weight=float(row["severity"]), score=float(row["severity"]))

    for row in ctx.get("subtype_top5", [])[:8]:
        subtype_id = str(row["subtype_id"])
        builder.add_node(subtype_id, "FaultSubtype", name=sanitize_name(str(row["subtype_name"])), stage="rca", score=float(row["probability"]), properties=row)
        builder.add_edge(feedback_id, subtype_id, "has_subtype_candidate", weight=float(row["probability"]), score=float(row["probability"]))

    for sensor, energy in list(ctx.get("sensor_energy_top5", {}).items())[:8]:
        builder.add_node(str(sensor), "Sensor", score=float(energy))
        builder.add_edge(feedback_id, str(sensor), "activates_sensor", weight=float(energy), score=float(energy), properties={"energy": float(energy)})


def write_graph(path: Path, graph: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(graph, ensure_ascii=False, indent=2), encoding="utf-8")


def build_graphs_for_case(
    domain: str,
    case_id_arg: str,
    reasoning: dict[str, Any] | None = None,
    save_files: bool = True,
) -> dict[str, Any]:
    data = load_inputs(domain, case_id_arg)
    if reasoning is not None:
        data["reasoning"] = reasoning
    reasoning = data["reasoning"]
    triples = data["triples"]
    case_id = reasoning["case_id"]

    original = GraphBuilder(domain, case_id, "original_graph", reasoning)
    add_original_graph(original, triples, domain, reasoning)

    enhanced = GraphBuilder(domain, case_id, "fault_enhanced_graph", reasoning)
    add_original_graph(enhanced, triples, domain, reasoning)
    add_feedback_fault_nodes(enhanced, triples, reasoning)
    add_top6(enhanced, reasoning)
    add_top_candidate_context(enhanced, triples, reasoning, domain)
    add_context_attributes(enhanced, triples, max_edges=55)
    add_stage_nodes(enhanced, reasoning)
    add_conclusion_nodes(enhanced, reasoning)
    add_evidence(enhanced, data["evidence"], reasoning["feedback_summary"]["feedback_id"])
    if domain == "hydraulic":
        add_hydraulic_enhancement(enhanced, data, reasoning)

    output_dir = ROOT / "outputs" / domain / "graphs"
    original_path = output_dir / f"original_graph_{case_id}.json"
    enhanced_path = output_dir / f"fault_enhanced_graph_{case_id}.json"
    original_graph = original.to_json()
    enhanced_graph = enhanced.to_json()
    if save_files:
        write_graph(original_path, original_graph)
        write_graph(enhanced_path, enhanced_graph)

    return {
        "domain": domain,
        "case_id": case_id,
        "original_graph": original_graph,
        "fault_enhanced_graph": enhanced_graph,
        "artifacts": {
            "original_graph_json": str(original_path),
            "fault_enhanced_graph_json": str(enhanced_path),
        },
    }


def export(domain: str, case_id_arg: str) -> dict[str, Any]:
    result = build_graphs_for_case(domain, case_id_arg, save_files=True)
    original_path = Path(result["artifacts"]["original_graph_json"])
    enhanced_path = Path(result["artifacts"]["fault_enhanced_graph_json"])
    original_graph = result["original_graph"]
    enhanced_graph = result["fault_enhanced_graph"]

    print(f"original_graph: {original_path} nodes={original_graph['summary']['node_count']} edges={original_graph['summary']['edge_count']}")
    print(f"fault_enhanced_graph: {enhanced_path} nodes={enhanced_graph['summary']['node_count']} edges={enhanced_graph['summary']['edge_count']}")
    return result


def main() -> None:
    args = parse_args()
    result = export(args.domain, args.case_id)
    if args.print_json:
        print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
