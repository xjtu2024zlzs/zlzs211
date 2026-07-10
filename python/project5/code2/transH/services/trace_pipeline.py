from __future__ import annotations

import re
import json
from pathlib import Path
from typing import Any

PROJECT_ROOT = Path(__file__).resolve().parents[1]

from services.pipe_design_reference import applyPipeDesignReferencePatch
from scripts.export_echarts_graph import build_echarts_option
from scripts.export_graph import build_graphs_for_case
from scripts.generate_report import build_report_for_case
from scripts.run_reasoning import run_reasoning_for_case


def normalize_case_id_alias(case_id: str) -> str:
    bearing_match = re.fullmatch(r"FB-BRG-(\d+)", case_id)
    if bearing_match:
        return f"BFB-{int(bearing_match.group(1)):05d}"
    hydraulic_match = re.fullmatch(r"FB-HYD-(\d+)", case_id)
    if hydraulic_match:
        return f"HFB-{int(hydraulic_match.group(1)):05d}"
    return case_id


def artifact_paths(domain: str, case_id: str) -> dict[str, str]:
    reports = PROJECT_ROOT / "outputs" / domain / "reports"
    graphs = PROJECT_ROOT / "outputs" / domain / "graphs"
    return {
        "reasoning_json": str(reports / f"reasoning_{case_id}.json"),
        "trace_report_json": str(reports / f"trace_report_{case_id}.json"),
        "trace_report_md": str(reports / f"trace_report_{case_id}.md"),
        "trace_report_docx": str(reports / f"trace_report_{case_id}.docx"),
        "report_json": str(reports / f"trace_report_{case_id}.json"),
        "report_md": str(reports / f"trace_report_{case_id}.md"),
        "report_docx": str(reports / f"trace_report_{case_id}.docx"),
        "original_graph_json": str(graphs / f"original_graph_{case_id}.json"),
        "fault_enhanced_graph_json": str(graphs / f"fault_enhanced_graph_{case_id}.json"),
        "echarts_original_graph_json": str(graphs / f"echarts_original_graph_{case_id}.json"),
        "echarts_fault_enhanced_graph_json": str(graphs / f"echarts_fault_enhanced_graph_{case_id}.json"),
    }


def run_trace_pipeline(
    domain: str,
    case_id: str,
    model_variant: str = "default",
    generate_graph: bool = True,
    generate_report: bool = True,
    generate_echarts: bool = True,
    generate_docx: bool = True,
) -> dict[str, Any]:
    normalized_case_id = normalize_case_id_alias(case_id)
    reasoning = run_reasoning_for_case(
        domain,
        normalized_case_id,
        save_json=True,
        model_variant=model_variant,
    )
    reasoning = applyPipeDesignReferencePatch(reasoning)
    if reasoning.get("_output_path"):
        output_path = Path(str(reasoning["_output_path"]))
        output_path.write_text(json.dumps(reasoning, ensure_ascii=False, indent=2), encoding="utf-8")
    resolved_case_id = str(reasoning["case_id"])

    graphs: dict[str, Any] = {}
    if generate_graph or generate_echarts or generate_report:
        graph_result = build_graphs_for_case(domain, resolved_case_id, reasoning=reasoning, save_files=True)
        graphs["original_graph"] = graph_result["original_graph"]
        graphs["fault_enhanced_graph"] = graph_result["fault_enhanced_graph"]
    else:
        graph_result = {"artifacts": {}}

    if generate_echarts:
        echarts_original_result = build_echarts_option(
            graph=graphs.get("original_graph"),
            domain=domain,
            case_id=resolved_case_id,
            graph_type="original",
            save_files=True,
        )
        echarts_enhanced_result = build_echarts_option(
            graph=graphs.get("fault_enhanced_graph"),
            domain=domain,
            case_id=resolved_case_id,
            graph_type="enhanced",
            save_files=True,
        )
        graphs["echarts_original_graph"] = echarts_original_result["option"]
        graphs["echarts_fault_enhanced_graph"] = echarts_enhanced_result["option"]
    else:
        echarts_original_result = {"artifact_path": ""}
        echarts_enhanced_result = {"artifact_path": ""}

    report_payload: dict[str, Any] = {}
    if generate_report:
        report_result = build_report_for_case(
            domain,
            resolved_case_id,
            reasoning=reasoning,
            save_files=True,
            generate_docx=generate_docx,
        )
        report_payload = {
            "report_json": report_result["report_json"],
            "report_markdown": report_result["report_markdown"],
            "docx_generated": report_result["docx_generated"],
        }
    else:
        report_result = {"artifacts": {}}

    artifacts = artifact_paths(domain, resolved_case_id)
    artifacts.update(graph_result.get("artifacts", {}))
    if echarts_original_result.get("artifact_path"):
        artifacts["echarts_original_graph_json"] = str(echarts_original_result["artifact_path"])
    if echarts_enhanced_result.get("artifact_path"):
        artifacts["echarts_fault_enhanced_graph_json"] = str(echarts_enhanced_result["artifact_path"])
    artifacts.update(report_result.get("artifacts", {}))
    if reasoning.get("_output_path"):
        artifacts["reasoning_json"] = str(reasoning["_output_path"])

    return {
        "domain": domain,
        "case_id": resolved_case_id,
        "model_variant": model_variant,
        "reasoning": reasoning,
        "graphs": graphs,
        "report": report_payload,
        "artifacts": artifacts,
    }
