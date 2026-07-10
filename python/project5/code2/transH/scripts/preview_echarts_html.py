from __future__ import annotations

import argparse
import json
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Generate a local ECharts graph HTML preview.")
    parser.add_argument("--domain", choices=["bearing", "hydraulic"], required=True)
    parser.add_argument("--case_id", required=True)
    parser.add_argument("--graph_type", choices=["original", "enhanced"], required=True)
    return parser.parse_args()


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


def graph_paths(domain: str, case_id: str, graph_type: str) -> tuple[Path, Path]:
    graph_dir = ROOT / "outputs" / domain / "graphs"
    if graph_type == "original":
        input_path = graph_dir / f"echarts_original_graph_{case_id}.json"
        output_path = graph_dir / f"preview_original_graph_{case_id}.html"
    else:
        input_path = graph_dir / f"echarts_fault_enhanced_graph_{case_id}.json"
        output_path = graph_dir / f"preview_fault_enhanced_graph_{case_id}.html"
    return input_path, output_path


def require_file(path: Path) -> Path:
    if not path.exists():
        raise FileNotFoundError(f"Missing ECharts option JSON: {path}")
    return path


def load_option(path: Path) -> dict[str, Any]:
    option = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(option, dict):
        raise ValueError(f"ECharts option must be a JSON object: {path}")
    return option


def graph_series(option: dict[str, Any]) -> list[dict[str, Any]]:
    series = option.get("series", [])
    if isinstance(series, dict):
        series = [series]
    if not isinstance(series, list):
        return []
    return [item for item in series if isinstance(item, dict) and item.get("type") == "graph"]


def series_nodes(series: dict[str, Any]) -> list[dict[str, Any]]:
    nodes = series.get("data", series.get("nodes", []))
    return nodes if isinstance(nodes, list) else []


def series_links(series: dict[str, Any]) -> list[dict[str, Any]]:
    links = series.get("links", series.get("edges", []))
    return links if isinstance(links, list) else []


def count_graph_items(option: dict[str, Any]) -> dict[str, int]:
    node_count = 0
    edge_count = 0
    top6_count = 0
    for series in graph_series(option):
        nodes = series_nodes(series)
        links = series_links(series)
        node_count += len(nodes)
        edge_count += len(links)
        for node in nodes:
            if not isinstance(node, dict):
                continue
            props = node.get("properties", {})
            is_top_candidate = isinstance(props, dict) and bool(props.get("is_top_candidate"))
            rank = props.get("rank") if isinstance(props, dict) else None
            rank_is_top6 = isinstance(rank, int) and 1 <= rank <= 6
            category_text = str(node.get("category", ""))
            category_is_top = category_text in {"top_candidate", "Top6疑似原因"}
            if is_top_candidate or rank_is_top6 or category_is_top:
                top6_count += 1
    return {"node_count": node_count, "edge_count": edge_count, "top6_count": top6_count}


def node_legend_items(option: dict[str, Any]) -> list[dict[str, str]]:
    items: list[dict[str, str]] = []
    seen: set[str] = set()
    for series in graph_series(option):
        for category in series.get("categories", []) or []:
            if not isinstance(category, dict):
                continue
            name = str(category.get("name", ""))
            if not name or name in seen:
                continue
            seen.add(name)
            style = category.get("itemStyle", {})
            color = style.get("color", "#8f8f8f") if isinstance(style, dict) else "#8f8f8f"
            items.append({"name": name, "color": str(color)})
    return items


def default_edge_legend() -> list[dict[str, Any]]:
    return [
        {"name": "TransH疑似原因关系", "color": "#d62728", "width": 3.5, "type": "solid"},
        {"name": "RCA根因证据关系", "color": "#c2185b", "width": 2.8, "type": "solid"},
        {"name": "传感器激活关系", "color": "#7b1fa2", "width": 2.2, "type": "dashed"},
        {"name": "部件诊断/子类型候选关系", "color": "#f57c00", "width": 2.0, "type": "dashed"},
        {"name": "生命周期阶段关系", "color": "#666666", "width": 1.6, "type": "solid"},
        {"name": "管路参数关系", "color": "#90caf9", "width": 1.0, "type": "dotted"},
        {"name": "辅助属性关系", "color": "#bdbdbd", "width": 0.8, "type": "solid"},
    ]


def edge_legend_items(option: dict[str, Any]) -> list[dict[str, Any]]:
    edge_legend = option.get("edgeLegend")
    if isinstance(edge_legend, list) and edge_legend:
        return [item for item in edge_legend if isinstance(item, dict)]
    return default_edge_legend()


def render_node_legend(option: dict[str, Any]) -> str:
    rows = []
    for item in node_legend_items(option):
        rows.append(
            f'<span class="legend-item"><span class="node-dot" style="background:{item["color"]}"></span><span>{item["name"]}</span></span>'
        )
    return "\n".join(rows)


def render_edge_legend(option: dict[str, Any]) -> str:
    rows = []
    for item in edge_legend_items(option):
        color = str(item.get("color", "#bdbdbd"))
        width = str(item.get("width", 1.0))
        line_type = str(item.get("type", "solid"))
        border_style = "dotted" if line_type == "dotted" else "dashed" if line_type == "dashed" else "solid"
        rows.append(
            f'<span class="legend-item"><span class="edge-line" style="border-top:{width}px {border_style} {color}"></span><span>{item.get("name", "")}</span></span>'
        )
    return "\n".join(rows)


def safe_json_for_script(value: Any) -> str:
    return json.dumps(value, ensure_ascii=False).replace("</", "<\\/")


def display_graph_type(graph_type: str) -> str:
    return {"original": "原始生命周期知识图谱", "enhanced": "故障增强知识图谱"}.get(graph_type, graph_type)


def render_html(domain: str, case_id: str, graph_type: str, option: dict[str, Any], counts: dict[str, int]) -> str:
    page_title = f"{domain} {case_id} {display_graph_type(graph_type)}"
    option_json = safe_json_for_script(option)
    meta_json = safe_json_for_script({"domain": domain, "case_id": case_id, "graph_type": graph_type, **counts})
    node_legend_html = render_node_legend(option)
    edge_legend_html = render_edge_legend(option)
    return f"""<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>{page_title}</title>
  <style>
    html, body {{
      min-height: 100%;
    }}
    body {{
      margin: 0;
      font-family: Arial, "Microsoft YaHei", sans-serif;
      color: #1f2933;
      background: #ffffff;
    }}
    header {{
      min-height: 122px;
      box-sizing: border-box;
      padding: 18px 24px 14px;
      background: #ffffff;
      border-bottom: 1px solid #d9e2ec;
    }}
    h1 {{
      margin: 0 0 12px;
      font-size: 22px;
      font-weight: 700;
    }}
    .meta {{
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      font-size: 14px;
      align-items: center;
    }}
    .meta span {{
      display: inline-flex;
      gap: 6px;
      padding: 6px 10px;
      background: #eef2f7;
      border: 1px solid #d9e2ec;
      border-radius: 6px;
    }}
    .meta b {{
      color: #52606d;
      font-weight: 600;
    }}
    .guide {{
      margin: 12px 0 0;
      color: #334e68;
      font-size: 14px;
      line-height: 1.55;
    }}
    .legend-wrap {{
      display: grid;
      grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
      gap: 14px;
      padding: 14px 24px;
      background: #ffffff;
      border-bottom: 1px solid #edf2f7;
    }}
    .legend-section {{
      border: 1px solid #d9e2ec;
      border-radius: 8px;
      padding: 10px 12px;
      background: #fbfdff;
    }}
    .legend-title {{
      margin: 0 0 8px;
      color: #243b53;
      font-weight: 700;
      font-size: 14px;
    }}
    .legend-row {{
      display: flex;
      flex-wrap: wrap;
      gap: 8px 14px;
      align-items: center;
      font-size: 13px;
      line-height: 1.45;
    }}
    .legend-item {{
      display: inline-flex;
      align-items: center;
      gap: 6px;
      white-space: nowrap;
    }}
    .node-dot {{
      width: 11px;
      height: 11px;
      border-radius: 999px;
      display: inline-block;
      box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.08);
    }}
    .edge-line {{
      width: 34px;
      height: 0;
      display: inline-block;
    }}
    #chart {{
      height: 850px;
      width: 100%;
      background: #ffffff;
    }}
    footer {{
      padding: 10px 24px 18px;
      color: #52606d;
      font-size: 13px;
      border-top: 1px solid #edf2f7;
      background: #ffffff;
    }}
  </style>
</head>
<body>
  <header>
    <h1>{page_title}</h1>
    <div class="meta">
      <span><b>domain</b>{domain}</span>
      <span><b>case_id</b>{case_id}</span>
      <span><b>graph_type</b>{graph_type}</span>
      <span><b>节点数量</b>{counts["node_count"]}</span>
      <span><b>边数量</b>{counts["edge_count"]}</span>
      <span><b>Top6 节点数量</b>{counts["top6_count"]}</span>
    </div>
    <p class="guide">红色粗线表示 TransH 疑似原因关系；橙色节点表示 Top6 疑似原因；蓝色节点表示反馈对象；红色节点表示质量反馈。</p>
  </header>
  <section class="legend-wrap">
    <div class="legend-section">
      <div class="legend-title">节点图例</div>
      <div class="legend-row">
        {node_legend_html}
      </div>
    </div>
    <div class="legend-section">
      <div class="legend-title">关系图例</div>
      <div class="legend-row">
        {edge_legend_html}
      </div>
    </div>
  </section>
  <main>
    <div id="chart"></div>
  </main>
  <footer>操作提示：鼠标滚轮缩放，拖拽空白区域平移，拖拽节点调整布局；悬停节点或边可查看 properties 明细。</footer>
  <script src="https://cdn.jsdelivr.net/npm/echarts@5/dist/echarts.min.js"></script>
  <script>
    const previewMeta = {meta_json};
    const option = {option_json};

    function escapeHtml(value) {{
      return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
    }}

    function renderProperties(properties) {{
      if (!properties || typeof properties !== "object") {{
        return "";
      }}
      return Object.entries(properties)
        .map(([key, value]) => {{
          const rendered = typeof value === "object" && value !== null ? JSON.stringify(value) : value;
          return `<div><b>${{escapeHtml(key)}}:</b> ${{escapeHtml(rendered)}}</div>`;
        }})
        .join("");
    }}

    function fieldLine(label, value) {{
      if (value === undefined || value === null || value === "") {{
        return "";
      }}
      return `<div><b>${{escapeHtml(label)}}:</b> ${{escapeHtml(value)}}</div>`;
    }}

    function nodeTooltip(data, props) {{
      const title = data.name || data.id || props.name || props.id || "";
      return [
        `<div style="max-width:560px;white-space:normal;line-height:1.55">`,
        `<div style="font-weight:700;margin-bottom:4px">${{escapeHtml(title)}}</div>`,
        fieldLine("id", data.id || props.id),
        fieldLine("type", props.type),
        fieldLine("stage_name", props.stage_name),
        fieldLine("是否反馈对象", props.is_target_object === true ? "是" : props.is_target_object === false ? "否" : ""),
        fieldLine("是否 Top6", props.is_top_candidate === true ? "是" : props.is_top_candidate === false ? "否" : ""),
        fieldLine("rank", props.rank),
        fieldLine("score", props.score ?? data.value),
        fieldLine("candidate_scope", props.candidate_scope),
        fieldLine("reason_description", props.reason_description),
        `<hr style="border:none;border-top:1px solid #d9e2ec;margin:6px 0">`,
        renderProperties(props),
        `</div>`
      ].join("");
    }}

    function edgeTooltip(data, props) {{
      const relation = data.relation || data.label || props.relation || "";
      return [
        `<div style="max-width:560px;white-space:normal;line-height:1.55">`,
        `<div style="font-weight:700;margin-bottom:4px">关系：${{escapeHtml(data.name || props.relation_name || relation)}}</div>`,
        fieldLine("source -> target", `${{data.source || ""}} -> ${{data.target || ""}}`),
        fieldLine("relation", relation),
        fieldLine("relation_name", props.relation_name || data.name),
        fieldLine("关系类型说明", props.business_meaning || props.description),
        fieldLine("weight / score", props.score ?? props.weight ?? data.value),
        `<hr style="border:none;border-top:1px solid #d9e2ec;margin:6px 0">`,
        renderProperties(props),
        `</div>`
      ].join("");
    }}

    function tooltipFormatter(params) {{
      const data = params.data || {{}};
      const props = data.properties || {{}};
      return params.dataType === "edge" ? edgeTooltip(data, props) : nodeTooltip(data, props);
    }}

    const graphSeries = Array.isArray(option.series) ? option.series : [option.series].filter(Boolean);
    if (Array.isArray(option.legend)) {{
      option.legend = option.legend.map((legend) => Object.assign({{}}, legend, {{ show: false }}));
    }}
    graphSeries.forEach((series) => {{
      if (series && series.type === "graph") {{
        series.roam = true;
        series.draggable = true;
        series.focusNodeAdjacency = true;
        series.emphasis = Object.assign({{}}, series.emphasis || {{}}, {{ focus: "adjacency" }});
        series.force = Object.assign({{}}, series.force || {{}}, {{ repulsion: (series.force && series.force.repulsion) || 420 }});
      }}
    }});

    option.tooltip = Object.assign({{}}, option.tooltip || {{}}, {{
      trigger: "item",
      confine: true,
      enterable: true,
      formatter: tooltipFormatter
    }});

    const chart = echarts.init(document.getElementById("chart"));
    chart.setOption(option, true);
    window.addEventListener("resize", () => chart.resize());
    window.previewMeta = previewMeta;
  </script>
</body>
</html>
"""


def main() -> None:
    args = parse_args()
    case_id = normalize_case_id_alias(args.case_id)
    input_path, output_path = graph_paths(args.domain, case_id, args.graph_type)
    option = load_option(require_file(input_path))
    counts = count_graph_items(option)
    output_path.write_text(render_html(args.domain, case_id, args.graph_type, option, counts), encoding="utf-8")
    print(f"HTML preview path: {output_path}")


if __name__ == "__main__":
    main()
