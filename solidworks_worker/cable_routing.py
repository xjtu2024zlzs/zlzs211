import json
import math
import subprocess
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection


DEFAULT_PIPE_COLORS = ["#e14b4b", "#2f80ed", "#24a148"]
DEFAULT_GRID_SHAPE = [12, 12, 8]
DEFAULT_PIPES = [
    {"name": "管路 1", "start": [0, 0, 0], "end": [11, 11, 7], "color": DEFAULT_PIPE_COLORS[0]},
    {"name": "管路 2", "start": [0, 1, 1], "end": [10, 2, 6], "color": DEFAULT_PIPE_COLORS[1]},
    {"name": "管路 3", "start": [2, 0, 7], "end": [9, 10, 0], "color": DEFAULT_PIPE_COLORS[2]},
]
DEFAULT_OBSTACLES = [
    {"name": "障碍物 1", "min": [3, 3, 1], "max": [5, 5, 4]},
    {"name": "障碍物 2", "min": [7, 2, 0], "max": [8, 8, 2]},
    {"name": "障碍物 3", "min": [1, 8, 2], "max": [4, 10, 6]},
]
DEFAULT_BOUNDARY_WALLS = ["floor", "left", "back"]
EPS = 1e-9


def default_routing_payload():
    return {
        "algorithmKey": "lp_bend_3d",
        "algorithmCode": "lp_bend_3d",
        "algorithmSource": "project2_service",
        "algorithmName": "LP_Bend_3D 三维管线路径规划算法",
        "gridShape": list(DEFAULT_GRID_SHAPE),
        "gridUnitMm": 50.0,
        "bendWeight": 2.0,
        "solverMode": "milp",
        "timeLimitSeconds": 60,
        "pipeOuterDiameterMm": 9.53,
        "pipeInnerDiameterMm": 7.73,
        "bendRadiusMm": 20.0,
        "wallThicknessMm": 4.0,
        "objectives": [],
        "constraints": [],
        "objectiveWeights": {},
        "constraintParams": {},
        "droppedObjectiveConstraintCodes": [],
        "pipes": [dict(item) for item in DEFAULT_PIPES],
        "obstacles": [dict(item) for item in DEFAULT_OBSTACLES],
        "boundaryWalls": list(DEFAULT_BOUNDARY_WALLS),
    }


def build_cable_scene(payload, output_dir, run_solidworks=True):
    job_dir = Path(output_dir).resolve()
    job_dir.mkdir(parents=True, exist_ok=True)
    cfg = normalize_payload(payload or {})
    solve_result = payload.get("solveResult") if isinstance(payload, dict) else None
    if not isinstance(solve_result, dict) or solve_result.get("status") != "SUCCESS" or not solve_result.get("paths"):
        return {
            "status": "FAILED",
            "statusLabel": "模型生成失败",
            "errorMessage": "请先完成线缆管路布局求解，再生成 SolidWorks 模型。",
            "params": public_payload(cfg),
        }

    paths = solve_result["paths"]
    routing_json_path = job_dir / "cable_routing_paths.json"
    stl_path = job_dir / "cable_layout_scene.stl"
    preview_path = job_dir / "cable_layout_preview.png"
    metadata_path = job_dir / "cable_layout_metadata.json"
    vbs_path = job_dir / "create_cable_layout_scene.vbs"
    sldprt_path = job_dir / "cable_layout_scene.SLDPRT"
    step_path = job_dir / "cable_layout_scene.step"

    routing_json_path.write_text(json.dumps(solve_result, ensure_ascii=False, indent=2), encoding="utf-8")
    write_scene_stl(stl_path, cfg, paths)
    render_scene_preview(preview_path, cfg, paths)
    metadata_path.write_text(json.dumps(scene_metadata(cfg, paths), ensure_ascii=False, indent=2), encoding="utf-8")
    write_solidworks_native_vbs(vbs_path, cfg, paths, sldprt_path, step_path)

    if run_solidworks:
        for path in (
            sldprt_path,
            step_path,
            job_dir / "solidworks_cable_log.txt",
            job_dir / "solidworks_cable_stdout.txt",
            job_dir / "solidworks_cable_stderr.txt",
        ):
            path.unlink(missing_ok=True)
        try:
            proc = subprocess.run(
                ["cscript", "//nologo", str(vbs_path)],
                cwd=str(job_dir),
                capture_output=True,
                text=True,
                timeout=240,
            )
            (job_dir / "solidworks_cable_stdout.txt").write_text(proc.stdout or "", encoding="utf-8", errors="ignore")
            (job_dir / "solidworks_cable_stderr.txt").write_text(proc.stderr or "", encoding="utf-8", errors="ignore")
            if proc.returncode != 0:
                raise RuntimeError(solidworks_log_message(job_dir, proc.stderr or proc.stdout or "SolidWorks 线缆管路模型生成失败。"))
        except subprocess.TimeoutExpired:
            raise RuntimeError("SolidWorks 线缆管路模型生成超时。")
        if not sldprt_path.exists():
            raise RuntimeError(solidworks_log_message(job_dir, "SolidWorks 未生成 cable_layout_scene.SLDPRT。"))

    return {
        "status": "SUCCESS",
        "statusLabel": "模型生成成功",
        "params": public_payload(cfg),
        "solveResult": solve_result,
        "sldprtPath": str(sldprt_path) if sldprt_path.exists() else "",
        "stepPath": str(step_path) if step_path.exists() else "",
        "stlPath": str(stl_path),
        "previewPngPath": str(preview_path),
        "routingJsonPath": str(routing_json_path),
        "metadataJsonPath": str(metadata_path),
        "vbsPath": str(vbs_path),
        "errorMessage": "" if run_solidworks else "SolidWorks 自动化未启用，已生成 STL、预览图和脚本。",
    }


def normalize_payload(payload):
    defaults = default_routing_payload()
    data = dict(defaults)
    data.update(payload or {})
    source = payload or {}
    grid_shape = normalize_grid_shape(source.get("gridShape") if "gridShape" in source else defaults["gridShape"], {**defaults, **source})
    pipes = normalize_pipes(data.get("pipes") or defaults["pipes"])
    obstacles = normalize_obstacles(data.get("obstacles") or defaults["obstacles"])
    outer_diameter = float_value(source, "pipeOuterDiameterMm", "pipeDiameter", default=defaults["pipeOuterDiameterMm"])
    inner_diameter = float_value(source, "pipeInnerDiameterMm", "pipeInnerDiameter", default=defaults["pipeInnerDiameterMm"])
    grid_unit = float_value(source, "gridUnitMm", "gridUnit", "cellSizeMm", default=defaults["gridUnitMm"])
    bend_radius = float_value(source, "bendRadiusMm", "bendRadius", default=defaults["bendRadiusMm"])
    algorithm_key = normalize_algorithm_key(data.get("algorithmKey") or data.get("algorithmCode") or defaults["algorithmKey"])
    algorithm_source = str(data.get("algorithmSource") or "project2_service")
    algorithm_name = str(data.get("algorithmName") or defaults["algorithmName"])

    if any(value <= 0 for value in grid_shape):
        raise ValueError("网格尺寸必须大于 0。")
    if grid_unit <= 0:
        raise ValueError("网格单元格大小必须大于 0。")
    if outer_diameter <= 0 or inner_diameter <= 0 or inner_diameter >= outer_diameter:
        raise ValueError("管道外径和内径不合法，内径必须大于 0 且小于外径。")
    if bend_radius <= 0:
        raise ValueError("圆角弯管半径必须大于 0。")

    return {
        "algorithmKey": algorithm_key,
        "algorithmCode": algorithm_key,
        "algorithmSource": algorithm_source,
        "algorithmName": algorithm_name,
        "gridShape": grid_shape,
        "gridUnitMm": grid_unit,
        "bendWeight": float_value(source, "bendWeight", default=defaults["bendWeight"]),
        "solverMode": str(data.get("solverMode") or "auto").lower(),
        "timeLimitSeconds": int(float_value(source, "timeLimitSeconds", default=defaults["timeLimitSeconds"])),
        "pipeOuterDiameterMm": outer_diameter,
        "pipeInnerDiameterMm": inner_diameter,
        "bendRadiusMm": bend_radius,
        "wallThicknessMm": float_value(source, "wallThicknessMm", default=max(4.0, grid_unit * 0.08)),
        "objectives": list(data.get("objectives") or []),
        "constraints": list(data.get("constraints") or []),
        "objectiveWeights": dict(data.get("objectiveWeights") or {}),
        "constraintParams": dict(data.get("constraintParams") or {}),
        "droppedObjectiveConstraintCodes": list(data.get("droppedObjectiveConstraintCodes") or []),
        "pipes": pipes,
        "obstacles": obstacles,
        "boundaryWalls": list(data.get("boundaryWalls") or DEFAULT_BOUNDARY_WALLS),
    }


def normalize_grid_shape(value, data):
    if isinstance(value, dict):
        raw = [value.get("x"), value.get("y"), value.get("z")]
    elif isinstance(value, (list, tuple)) and len(value) >= 3:
        raw = value[:3]
    else:
        raw = [data.get("gridX", DEFAULT_GRID_SHAPE[0]), data.get("gridY", DEFAULT_GRID_SHAPE[1]), data.get("gridZ", DEFAULT_GRID_SHAPE[2])]
    return [int(float(item)) for item in raw]


def normalize_algorithm_key(value):
    key = str(value or "lp_bend_3d").strip()
    if key in ("builtin", "builtin_cable_router", "default", "lp_bend_3d"):
        return "lp_bend_3d"
    return key


def public_payload(cfg):
    result = dict(cfg or {})
    return result


def normalize_pipes(value):
    result = []
    for index, item in enumerate(value or []):
        if isinstance(item, dict):
            start = normalize_point(item.get("start") or [item.get("sx"), item.get("sy"), item.get("sz")])
            end = normalize_point(item.get("end") or [item.get("ex"), item.get("ey"), item.get("ez")])
            name = item.get("name") or f"管路 {index + 1}"
            color = item.get("color") or DEFAULT_PIPE_COLORS[index % len(DEFAULT_PIPE_COLORS)]
        else:
            start = normalize_point(item[0])
            end = normalize_point(item[1])
            name = f"管路 {index + 1}"
            color = DEFAULT_PIPE_COLORS[index % len(DEFAULT_PIPE_COLORS)]
        result.append({"name": str(name), "start": start, "end": end, "color": str(color)})
    if not result:
        raise ValueError("至少需要设置 1 条管路。")
    return result


def normalize_obstacles(value):
    result = []
    for index, item in enumerate(value or []):
        if isinstance(item, dict):
            p_min = normalize_point(item.get("min") or [item.get("xMin"), item.get("yMin"), item.get("zMin")])
            p_max = normalize_point(item.get("max") or [item.get("xMax"), item.get("yMax"), item.get("zMax")])
            name = item.get("name") or f"障碍物 {index + 1}"
        else:
            p_min = normalize_point(item[:3])
            p_max = normalize_point(item[3:6])
            name = f"障碍物 {index + 1}"
        lower = [min(p_min[i], p_max[i]) for i in range(3)]
        upper = [max(p_min[i], p_max[i]) for i in range(3)]
        result.append({"name": str(name), "min": lower, "max": upper})
    return result


def normalize_point(value):
    if isinstance(value, dict):
        raw = [value.get("x"), value.get("y"), value.get("z")]
    else:
        raw = list(value or [])
    if len(raw) < 3:
        raise ValueError("坐标点必须包含 x、y、z 三个值。")
    return [int(float(raw[0])), int(float(raw[1])), int(float(raw[2]))]


def float_value(data, *names, default=0.0):
    for name in names:
        if name in data and data.get(name) not in (None, ""):
            return float(data.get(name))
    return float(default)


def grid_to_mm(point, grid_unit):
    return (point[0] * grid_unit, point[1] * grid_unit, point[2] * grid_unit)


def smooth_centerline(points, grid_unit, bend_radius):
    mm_points = [grid_to_mm(point, grid_unit) for point in points]
    if len(mm_points) <= 2:
        return mm_points
    result = [mm_points[0]]
    for index in range(1, len(mm_points) - 1):
        prev_p = mm_points[index - 1]
        point = mm_points[index]
        next_p = mm_points[index + 1]
        v_in = normalize(vector(prev_p, point))
        v_out = normalize(vector(point, next_p))
        if almost_same(v_in, v_out):
            result.append(point)
            continue
        prev_len = distance(prev_p, point)
        next_len = distance(point, next_p)
        trim = min(bend_radius, prev_len * 0.45, next_len * 0.45)
        before = sub(point, scale(v_in, trim))
        after = add(point, scale(v_out, trim))
        if distance(result[-1], before) > EPS:
            result.append(before)
        samples = 6
        for step in range(1, samples):
            t = step / samples
            result.append(quadratic_bezier(before, point, after, t))
        result.append(after)
    if distance(result[-1], mm_points[-1]) > EPS:
        result.append(mm_points[-1])
    return result


def write_scene_stl(path, cfg, paths):
    outer_radius = cfg["pipeOuterDiameterMm"] / 2.0
    inner_radius = cfg["pipeInnerDiameterMm"] / 2.0
    with path.open("w", encoding="ascii") as fh:
        for item in paths:
            centerline = smooth_centerline(item["points"], cfg["gridUnitMm"], cfg["bendRadiusMm"])
            write_solid(fh, f"pipe_{item['pipeIndex'] + 1}", tube_triangles(centerline, outer_radius, inner_radius, 18))
        for index, obstacle in enumerate(cfg["obstacles"]):
            write_solid(fh, f"obstacle_{index + 1}", cuboid_triangles(obstacle_bounds_mm(obstacle, cfg["gridUnitMm"])))
        for name, bounds in wall_bounds_mm(cfg).items():
            write_solid(fh, f"wall_{name}", cuboid_triangles(bounds))


def write_solid(fh, name, triangles):
    fh.write(f"solid {name}\n")
    for tri in triangles:
        normal = normalize(cross(vector(tri[0], tri[1]), vector(tri[0], tri[2])))
        fh.write(f"  facet normal {normal[0]:.8e} {normal[1]:.8e} {normal[2]:.8e}\n")
        fh.write("    outer loop\n")
        for vertex in tri:
            fh.write(f"      vertex {vertex[0]:.8e} {vertex[1]:.8e} {vertex[2]:.8e}\n")
        fh.write("    endloop\n")
        fh.write("  endfacet\n")
    fh.write(f"endsolid {name}\n")


def tube_triangles(points, outer_radius, inner_radius, segments):
    outer = tube_rings(points, outer_radius, segments)
    inner = tube_rings(points, inner_radius, segments)
    triangles = []
    for i in range(len(points) - 1):
        for j in range(segments):
            j2 = (j + 1) % segments
            triangles.append((outer[i][j], outer[i + 1][j], outer[i + 1][j2]))
            triangles.append((outer[i][j], outer[i + 1][j2], outer[i][j2]))
            triangles.append((inner[i][j2], inner[i + 1][j2], inner[i + 1][j]))
            triangles.append((inner[i][j2], inner[i + 1][j], inner[i][j]))
    for j in range(segments):
        j2 = (j + 1) % segments
        triangles.append((outer[0][j2], outer[0][j], inner[0][j]))
        triangles.append((outer[0][j2], inner[0][j], inner[0][j2]))
        triangles.append((outer[-1][j], outer[-1][j2], inner[-1][j]))
        triangles.append((outer[-1][j2], inner[-1][j2], inner[-1][j]))
    return triangles


def tube_rings(points, radius, segments):
    rings = []
    for index, point in enumerate(points):
        if index == 0:
            tangent = normalize(vector(points[0], points[1]))
        elif index == len(points) - 1:
            tangent = normalize(vector(points[-2], points[-1]))
        else:
            tangent = normalize(vector(points[index - 1], points[index + 1]))
        normal, binormal = frame_from_tangent(tangent)
        ring = []
        for segment in range(segments):
            angle = 2.0 * math.pi * segment / segments
            ring.append(add(point, add(scale(normal, math.cos(angle) * radius), scale(binormal, math.sin(angle) * radius))))
        rings.append(ring)
    return rings


def frame_from_tangent(tangent):
    reference = (0.0, 0.0, 1.0)
    if abs(dot(tangent, reference)) > 0.92:
        reference = (0.0, 1.0, 0.0)
    normal = normalize(cross(reference, tangent))
    binormal = normalize(cross(tangent, normal))
    return normal, binormal


def obstacle_bounds_mm(obstacle, grid_unit):
    x0, y0, z0 = obstacle["min"]
    x1, y1, z1 = obstacle["max"]
    return (x0 * grid_unit, y0 * grid_unit, z0 * grid_unit, (x1 + 1) * grid_unit, (y1 + 1) * grid_unit, (z1 + 1) * grid_unit)


def wall_bounds_mm(cfg):
    grid_x, grid_y, grid_z = cfg["gridShape"]
    unit = cfg["gridUnitMm"]
    thickness = cfg["wallThicknessMm"]
    max_x, max_y, max_z = grid_x * unit, grid_y * unit, grid_z * unit
    walls = {}
    selected = set(cfg.get("boundaryWalls") or DEFAULT_BOUNDARY_WALLS)
    if "floor" in selected:
        walls["floor"] = (0, 0, -thickness, max_x, max_y, 0)
    if "left" in selected:
        walls["left"] = (-thickness, 0, 0, 0, max_y, max_z)
    if "back" in selected:
        walls["back"] = (0, -thickness, 0, max_x, 0, max_z)
    return walls


def cuboid_triangles(bounds):
    x0, y0, z0, x1, y1, z1 = bounds
    vertices = [
        (x0, y0, z0), (x1, y0, z0), (x1, y1, z0), (x0, y1, z0),
        (x0, y0, z1), (x1, y0, z1), (x1, y1, z1), (x0, y1, z1),
    ]
    faces = [
        (0, 1, 2, 3), (4, 7, 6, 5), (0, 4, 5, 1),
        (1, 5, 6, 2), (2, 6, 7, 3), (3, 7, 4, 0),
    ]
    triangles = []
    for a, b, c, d in faces:
        triangles.append((vertices[a], vertices[b], vertices[c]))
        triangles.append((vertices[a], vertices[c], vertices[d]))
    return triangles


def render_scene_preview(path, cfg, paths):
    fig = plt.figure(figsize=(10.2, 7.0), dpi=150)
    ax = fig.add_subplot(111, projection="3d")
    for bounds in wall_bounds_mm(cfg).values():
        add_box_collection(ax, bounds, color="#83b9e8", alpha=0.16, edge_alpha=0.14)
    for obstacle in cfg["obstacles"]:
        add_box_collection(ax, obstacle_bounds_mm(obstacle, cfg["gridUnitMm"]), color="#7b8794", alpha=1.0, edge_alpha=0.82)
    for item in paths:
        centerline = smooth_centerline(item["points"], cfg["gridUnitMm"], cfg["bendRadiusMm"])
        xs, ys, zs = zip(*centerline)
        ax.plot(xs, ys, zs, color=item["color"], linewidth=3.0, solid_capstyle="round")
        ax.scatter([xs[0], xs[-1]], [ys[0], ys[-1]], [zs[0], zs[-1]], color=item["color"], s=24, depthshade=False)
    max_x, max_y, max_z = [value * cfg["gridUnitMm"] for value in cfg["gridShape"]]
    ax.set_xlim(-cfg["wallThicknessMm"], max_x)
    ax.set_ylim(-cfg["wallThicknessMm"], max_y)
    ax.set_zlim(-cfg["wallThicknessMm"], max_z)
    ax.set_box_aspect((max_x, max_y, max_z))
    ax.set_xlabel("X / mm")
    ax.set_ylabel("Y / mm")
    ax.set_zlabel("Z / mm")
    ax.view_init(elev=24, azim=-48)
    fig.patch.set_facecolor("#f6f8fb")
    ax.set_facecolor("#f6f8fb")
    ax.grid(True, color="#d7e0ea")
    plt.tight_layout()
    fig.savefig(path, facecolor=fig.get_facecolor(), bbox_inches="tight", pad_inches=0.08)
    plt.close(fig)


def add_box_collection(ax, bounds, color, alpha, edge_alpha):
    triangles = cuboid_triangles(bounds)
    collection = Poly3DCollection(
        triangles,
        facecolor=rgba(color, alpha),
        edgecolor=rgba(color, edge_alpha),
        linewidth=0.4,
    )
    ax.add_collection3d(collection)


def rgba(hex_color, alpha):
    text = hex_color.lstrip("#")
    return tuple(int(text[i:i + 2], 16) / 255.0 for i in (0, 2, 4)) + (alpha,)


def scene_metadata(cfg, paths):
    return {
        "unit": "mm",
        "algorithmKey": cfg.get("algorithmKey"),
        "algorithmName": cfg.get("algorithmName"),
        "algorithmSource": cfg.get("algorithmSource"),
        "objectives": cfg.get("objectives") or [],
        "constraints": cfg.get("constraints") or [],
        "constraintParams": cfg.get("constraintParams") or {},
        "gridShape": cfg["gridShape"],
        "gridUnitMm": cfg["gridUnitMm"],
        "pipes": [
            {
                "name": item["name"],
                "color": item["color"],
                "outerDiameterMm": cfg["pipeOuterDiameterMm"],
                "innerDiameterMm": cfg["pipeInnerDiameterMm"],
                "bendRadiusMm": cfg["bendRadiusMm"],
            }
            for item in paths
        ],
        "obstacles": [{"name": item["name"], "color": "#7b8794", "alpha": 1.0, "solid": True} for item in cfg["obstacles"]],
        "walls": [{"name": item, "color": "#83b9e8", "alpha": 0.16} for item in cfg["boundaryWalls"]],
    }


def write_solidworks_native_vbs(path, cfg, paths, sldprt_path, step_path):
    log_path = path.with_name("solidworks_cable_log.txt")
    outer_diameter_m = cfg["pipeOuterDiameterMm"] / 1000.0
    inner_diameter_m = cfg["pipeInnerDiameterMm"] / 1000.0
    lines = [
        "Option Explicit",
        "Dim swApp, swModel, fso, partTemplate, ok, saveOk, longStatus, longWarnings",
        "Dim swFeat, swPathFeat, swInnerPathFeat, swInnerCutFeat, swBoxFeat, swBoxProfileFeat, swBoxPlaneFeat",
        "Sub WriteLog(message)",
        "  Dim logFso, logFile",
        "  Set logFso = CreateObject(\"Scripting.FileSystemObject\")",
        f"  Set logFile = logFso.OpenTextFile(\"{vbs_literal(log_path)}\", 8, True)",
        "  logFile.WriteLine Now & \" \" & message",
        "  logFile.Close",
        "End Sub",
        "Function FindLast3DSketchFeature(model)",
        "  Dim feat, last3dSketch, typeName",
        "  Set last3dSketch = Nothing",
        "  Set feat = model.FirstFeature",
        "  Do While Not feat Is Nothing",
        "    typeName = feat.GetTypeName2",
        "    If typeName = \"3DProfileFeature\" Then Set last3dSketch = feat",
        "    Set feat = feat.GetNextFeature",
        "  Loop",
        "  Set FindLast3DSketchFeature = last3dSketch",
        "End Function",
        "Function FindLastSketchFeature(model)",
        "  Dim feat, lastSketch, typeName",
        "  Set lastSketch = Nothing",
        "  Set feat = model.FirstFeature",
        "  Do While Not feat Is Nothing",
        "    typeName = feat.GetTypeName2",
        "    If typeName = \"ProfileFeature\" Then Set lastSketch = feat",
        "    Set feat = feat.GetNextFeature",
        "  Loop",
        "  Set FindLastSketchFeature = lastSketch",
        "End Function",
        "Function SelectFrontReferencePlane(model)",
        "  Dim names, planeName, feat, typeName",
        "  SelectFrontReferencePlane = False",
        "  model.ClearSelection2 True",
        "  names = Array(\"Front Plane\")",
        "  For Each planeName In names",
        "    If model.Extension.SelectByID2(planeName, \"PLANE\", 0, 0, 0, False, 0, Nothing, 0) Then",
        "      SelectFrontReferencePlane = True",
        "      Exit Function",
        "    End If",
        "  Next",
        "  Set feat = model.FirstFeature",
        "  Do While Not feat Is Nothing",
        "    typeName = feat.GetTypeName2",
        "    If typeName = \"RefPlane\" Then",
        "      If feat.Select2(False, 0) Then",
        "        SelectFrontReferencePlane = True",
        "        Exit Function",
        "      End If",
        "    End If",
        "    Set feat = feat.GetNextFeature",
        "  Loop",
        "End Function",
        "Sub ApplyFeatureAppearance(feat, r, g, b, transparency)",
        "  Dim props, bodies, body, faces, face, applied",
        "  If feat Is Nothing Then Exit Sub",
        "  props = Array(r, g, b, 0.6, 0.6, 0.5, 0.35, transparency, 0.0)",
        "  applied = False",
        "  On Error Resume Next",
        "  bodies = feat.GetBodies2(0)",
        "  If Err.Number = 0 And IsArray(bodies) Then",
        "    For Each body In bodies",
        "      body.MaterialPropertyValues2 = props",
        "      If Err.Number = 0 Then",
        "        applied = True",
        "      Else",
        "        Err.Clear",
        "      End If",
        "    Next",
        "  Else",
        "    Err.Clear",
        "  End If",
        "  If Not applied Then",
        "    faces = feat.GetFaces",
        "    If Err.Number = 0 And IsArray(faces) Then",
        "      For Each face In faces",
        "        face.MaterialPropertyValues = props",
        "        If Err.Number = 0 Then",
        "          applied = True",
        "        Else",
        "          Err.Clear",
        "        End If",
        "      Next",
        "    Else",
        "      Err.Clear",
        "    End If",
        "  End If",
        "  If Not applied Then",
        "    feat.SetMaterialPropertyValues2 props, 1, Empty",
        "    If Err.Number = 0 Then applied = True Else Err.Clear",
        "  End If",
        "  If Not applied Then WriteLog \"Appearance skipped for \" & feat.Name",
        "  On Error GoTo 0",
        "End Sub",
        "Sub HideSketchFeature(model, sketchFeat)",
        "  Dim hideOk",
        "  If sketchFeat Is Nothing Then Exit Sub",
        "  model.ClearSelection2 True",
        "  hideOk = False",
        "  On Error Resume Next",
        "  hideOk = sketchFeat.Select2(False, 0)",
        "  If hideOk Then model.BlankSketch",
        "  WriteLog \"Hide sketch \" & sketchFeat.Name & \" ok=\" & CStr(hideOk) & \", err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
        "  If Err.Number <> 0 Then Err.Clear",
        "  On Error GoTo 0",
        "  model.ClearSelection2 True",
        "End Sub",
        "Sub HideRefPlaneFeature(model, planeFeat)",
        "  Dim hideOk",
        "  If planeFeat Is Nothing Then Exit Sub",
        "  model.ClearSelection2 True",
        "  hideOk = False",
        "  On Error Resume Next",
        "  hideOk = planeFeat.Select2(False, 0)",
        "  If hideOk Then model.BlankRefGeom",
        "  WriteLog \"Hide reference plane \" & planeFeat.Name & \" ok=\" & CStr(hideOk) & \", err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
        "  If Err.Number <> 0 Then Err.Clear",
        "  On Error GoTo 0",
        "  model.ClearSelection2 True",
        "End Sub",
        "Sub HideAllSketches(model)",
        "  Dim feat, typeName",
        "  Set feat = model.FirstFeature",
        "  Do While Not feat Is Nothing",
        "    typeName = feat.GetTypeName2",
        "    If typeName = \"3DProfileFeature\" Or typeName = \"ProfileFeature\" Then HideSketchFeature model, feat",
        "    Set feat = feat.GetNextFeature",
        "  Loop",
        "End Sub",
        "Function ApplySweptInnerCut(model, pathFeat, innerDiameter, cutName)",
        "  Dim cutOk, cutFeat",
        "  Set cutFeat = Nothing",
        "  model.ClearSelection2 True",
        "  cutOk = False",
        "  If Not pathFeat Is Nothing Then cutOk = pathFeat.Select2(False, 4)",
        "  If Not cutOk Then",
        "    WriteLog cutName & \" path selection failed\"",
        "    Set ApplySweptInnerCut = Nothing",
        "    Exit Function",
        "  End If",
        "  On Error Resume Next",
        "  Set cutFeat = model.FeatureManager.InsertCutSwept5(False, False, 0, False, False, 0, 0, False, 0, 0, 0, 0, True, True, 0, True, False, True, True, True, innerDiameter, 0)",
        "  WriteLog cutName & \" InsertCutSwept5 err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
        "  If Err.Number <> 0 Then Err.Clear",
        "  On Error GoTo 0",
        "  If Not cutFeat Is Nothing Then cutFeat.Name = cutName",
        "  Set ApplySweptInnerCut = cutFeat",
        "End Function",
        "Function CreateBoxBody(boxName, x0, y0, x1, y1, zMid, depth, redValue, greenValue, blueValue, transparency, failureCode, fatalOnFailure)",
        "  Dim boxOk, boxPlaneFeat, boxProfileFeat, boxFeat",
        "  Set boxPlaneFeat = Nothing",
        "  Set boxProfileFeat = Nothing",
        "  Set boxFeat = Nothing",
        "  Set CreateBoxBody = Nothing",
        "  WriteLog boxName & \" creating solid box by 2D closed sketch boss extrusion\"",
        "  swModel.ClearSelection2 True",
        "  boxOk = SelectFrontReferencePlane(swModel)",
        "  If Not boxOk Then",
        "    WriteLog boxName & \" front reference plane selection failed\"",
        "    If fatalOnFailure Then WScript.Quit failureCode Else Exit Function",
        "  End If",
        "  On Error Resume Next",
        "  Set boxPlaneFeat = swModel.FeatureManager.InsertRefPlane(8, zMid, 0, 0, 0, 0)",
        "  WriteLog boxName & \" InsertRefPlane err=\" & CStr(Err.Number) & \", desc=\" & Err.Description & \", result=\" & CStr(Not (boxPlaneFeat Is Nothing))",
        "  If Err.Number <> 0 Then Err.Clear",
        "  On Error GoTo 0",
        "  If boxPlaneFeat Is Nothing Then",
        "    WriteLog boxName & \" offset sketch plane creation failed\"",
        "    If fatalOnFailure Then WScript.Quit failureCode Else Exit Function",
        "  End If",
        "  boxPlaneFeat.Name = boxName & \"_MidPlane\"",
        "  swModel.ClearSelection2 True",
        "  boxOk = boxPlaneFeat.Select2(False, 0)",
        "  If Not boxOk Then",
        "    WriteLog boxName & \" mid-plane selection failed\"",
        "    HideRefPlaneFeature swModel, boxPlaneFeat",
        "    If fatalOnFailure Then WScript.Quit failureCode Else Exit Function",
        "  End If",
        "  swModel.SketchManager.InsertSketch True",
        "  swModel.SketchManager.AddToDB = True",
        "  swModel.SketchManager.CreateCornerRectangle x0, y0, 0, x1, y1, 0",
        "  swModel.SketchManager.AddToDB = False",
        "  swModel.SketchManager.InsertSketch True",
        "  Set boxProfileFeat = FindLastSketchFeature(swModel)",
        "  If Not boxProfileFeat Is Nothing Then boxProfileFeat.Name = boxName & \"_Profile\"",
        "  swModel.ClearSelection2 True",
        "  boxOk = False",
        "  If Not boxProfileFeat Is Nothing Then boxOk = boxProfileFeat.Select2(False, 0)",
        "  If Not boxOk Then",
        "    WriteLog boxName & \" profile selection failed\"",
        "    HideSketchFeature swModel, boxProfileFeat",
        "    HideRefPlaneFeature swModel, boxPlaneFeat",
        "    If fatalOnFailure Then WScript.Quit failureCode Else Exit Function",
        "  End If",
        "  On Error Resume Next",
        "  Set boxFeat = swModel.FeatureManager.FeatureExtrusion2(True, False, False, 6, 0, depth, 0, False, False, False, False, 0, 0, False, False, False, False, False, True, True, 0, 0, False)",
        "  WriteLog boxName & \" FeatureExtrusion2 err=\" & CStr(Err.Number) & \", desc=\" & Err.Description & \", result=\" & CStr(Not (boxFeat Is Nothing))",
        "  If Err.Number <> 0 Then Err.Clear",
        "  On Error GoTo 0",
        "  If boxFeat Is Nothing Then",
        "    WriteLog boxName & \" solid box extrusion failed\"",
        "    HideSketchFeature swModel, boxProfileFeat",
        "    HideRefPlaneFeature swModel, boxPlaneFeat",
        "    If fatalOnFailure Then WScript.Quit failureCode Else Exit Function",
        "  End If",
        "  boxFeat.Name = boxName",
        "  ApplyFeatureAppearance boxFeat, redValue, greenValue, blueValue, transparency",
        "  HideSketchFeature swModel, boxProfileFeat",
        "  HideRefPlaneFeature swModel, boxPlaneFeat",
        "  Set CreateBoxBody = boxFeat",
        "End Function",
        "Set fso = CreateObject(\"Scripting.FileSystemObject\")",
        f"If fso.FileExists(\"{vbs_literal(log_path)}\") Then fso.DeleteFile \"{vbs_literal(log_path)}\", True",
        "WriteLog \"Starting native cable routing SolidWorks build.\"",
        "On Error Resume Next",
        "Set swApp = CreateObject(\"SldWorks.Application\")",
        "WriteLog \"Connect SolidWorks err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
        "If Err.Number <> 0 Then Err.Clear",
        "On Error GoTo 0",
        "If swApp Is Nothing Then",
        "  WriteLog \"Cannot connect to SolidWorks COM automation.\"",
        "  WScript.Quit 1",
        "End If",
        "swApp.Visible = True",
        "On Error Resume Next",
        "partTemplate = swApp.GetUserPreferenceStringValue(1)",
        "WriteLog \"Part template=\" & partTemplate",
        "If Err.Number <> 0 Then Err.Clear",
        "If Len(partTemplate) > 0 Then",
        "  Set swModel = swApp.NewDocument(partTemplate, 0, 0, 0)",
        "Else",
        "  swApp.NewPart",
        "  Set swModel = swApp.ActiveDoc",
        "End If",
        "WriteLog \"New part err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
        "If Err.Number <> 0 Then Err.Clear",
        "On Error GoTo 0",
        "If swModel Is Nothing Then",
        "  WriteLog \"Cannot create a new SolidWorks part.\"",
        "  WScript.Quit 2",
        "End If",
    ]

    for index, item in enumerate(paths):
        pipe_no = index + 1
        path_entities = solidworks_path_entities(item["points"], cfg["gridUnitMm"], cfg["bendRadiusMm"])
        if not path_entities:
            continue
        color = rgb01(item.get("color") or DEFAULT_PIPE_COLORS[index % len(DEFAULT_PIPE_COLORS)])
        lines.extend(
            [
                f"WriteLog \"Creating CablePipe_{pipe_no} path with {len(path_entities)} line/arc entities.\"",
                "swModel.SketchManager.Insert3DSketch True",
                "swModel.SketchManager.AddToDB = True",
            ]
        )
        lines.extend(vbs_path_entities(path_entities))
        lines.extend(
            [
                "swModel.SketchManager.AddToDB = False",
                "swModel.SketchManager.Insert3DSketch True",
                "Set swPathFeat = FindLast3DSketchFeature(swModel)",
                f"If Not swPathFeat Is Nothing Then swPathFeat.Name = \"CablePipePath_{pipe_no}\"",
                "swModel.ClearSelection2 True",
                "ok = False",
                "If Not swPathFeat Is Nothing Then ok = swPathFeat.Select2(False, 4)",
                "If Not ok Then",
                f"  WriteLog \"CablePipe_{pipe_no} path selection failed.\"",
                f"  WScript.Quit {20 + pipe_no}",
                "End If",
                "On Error Resume Next",
                f"Set swFeat = swModel.FeatureManager.InsertProtrusionSwept4(False, False, 0, False, False, 0, 0, False, 0, 0, 0, 0, True, True, True, 0, True, True, {outer_diameter_m:.10f}, 0)",
                f"WriteLog \"CablePipe_{pipe_no} outer sweep err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
                "If Err.Number <> 0 Then Err.Clear",
                "On Error GoTo 0",
                "If swFeat Is Nothing Then",
                f"  WriteLog \"CablePipe_{pipe_no} outer sweep failed.\"",
                f"  WScript.Quit {40 + pipe_no}",
                "End If",
                f"swFeat.Name = \"CablePipeOuter_{pipe_no}\"",
                f"ApplyFeatureAppearance swFeat, {color[0]:.6f}, {color[1]:.6f}, {color[2]:.6f}, 0.08",
            ]
        )

        lines.extend(
            [
                f"WriteLog \"Creating CablePipe_{pipe_no} hollow-bore cut from outer sweep path.\"",
                f"Set swInnerCutFeat = ApplySweptInnerCut(swModel, swPathFeat, {inner_diameter_m:.10f}, \"CablePipeInnerCut_{pipe_no}\")",
                "If swInnerCutFeat Is Nothing Then",
                f"  WriteLog \"CablePipe_{pipe_no} hollow-bore cut failed.\"",
                f"  WScript.Quit {60 + pipe_no}",
                "End If",
                "HideSketchFeature swModel, swPathFeat",
            ]
        )

    append_box_vbs_lines(lines, cfg)

    lines.extend(
        [
            "On Error Resume Next",
            "HideAllSketches swModel",
            "swModel.ForceRebuild3 False",
            "swModel.ViewZoomtofit2",
            "If Err.Number <> 0 Then",
            "  WriteLog \"Rebuild/view err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
            "  Err.Clear",
            "End If",
            "On Error GoTo 0",
            "longStatus = CLng(0)",
            "longWarnings = CLng(0)",
            f"If fso.FileExists(\"{vbs_literal(sldprt_path)}\") Then fso.DeleteFile \"{vbs_literal(sldprt_path)}\", True",
            f"WriteLog \"Saving SLDPRT: {vbs_literal(sldprt_path)}\"",
            "On Error Resume Next",
            f"saveOk = swModel.SaveAs3(\"{vbs_literal(sldprt_path)}\", CLng(0), CLng(2))",
            "WriteLog \"SLDPRT SaveAs3 result=\" & CStr(saveOk) & \", err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
            "If Err.Number <> 0 Then Err.Clear",
            f"If Not fso.FileExists(\"{vbs_literal(sldprt_path)}\") Then",
            f"  saveOk = swModel.Extension.SaveAs(\"{vbs_literal(sldprt_path)}\", CLng(0), CLng(1), Nothing, longStatus, longWarnings)",
            "  WriteLog \"SLDPRT Extension.SaveAs fallback result=\" & CStr(saveOk) & \", status=\" & CStr(longStatus) & \", warnings=\" & CStr(longWarnings) & \", err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
            "  If Err.Number <> 0 Then Err.Clear",
            "End If",
            "On Error GoTo 0",
            f"If Not fso.FileExists(\"{vbs_literal(sldprt_path)}\") Then",
            "  WriteLog \"SLDPRT output missing after native build.\"",
            "  WScript.Quit 90",
            "End If",
            f"If fso.FileExists(\"{vbs_literal(step_path)}\") Then fso.DeleteFile \"{vbs_literal(step_path)}\", True",
            "longStatus = CLng(0)",
            "longWarnings = CLng(0)",
            "On Error Resume Next",
            f"saveOk = swModel.SaveAs3(\"{vbs_literal(step_path)}\", CLng(0), CLng(2))",
            "WriteLog \"STEP SaveAs3 result=\" & CStr(saveOk) & \", err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
            "If Err.Number <> 0 Then Err.Clear",
            f"If Not fso.FileExists(\"{vbs_literal(step_path)}\") Then",
            f"  saveOk = swModel.Extension.SaveAs(\"{vbs_literal(step_path)}\", CLng(0), CLng(1), Nothing, longStatus, longWarnings)",
            "  WriteLog \"STEP Extension.SaveAs fallback result=\" & CStr(saveOk) & \", status=\" & CStr(longStatus) & \", warnings=\" & CStr(longWarnings) & \", err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
            "End If",
            "If Err.Number <> 0 Then Err.Clear",
            "On Error GoTo 0",
            "WriteLog \"Native cable routing scene model created.\"",
            "WScript.Quit 0",
        ]
    )
    path.write_text("\n".join(lines) + "\n", encoding="ascii")


def append_box_vbs_lines(lines, cfg):
    for index, obstacle in enumerate(cfg["obstacles"]):
        lines.extend(
            vbs_box_body_commands(
                f"Obstacle_{index + 1}",
                obstacle_bounds_mm(obstacle, cfg["gridUnitMm"]),
                (0.482353, 0.529412, 0.580392),
                0.0,
                70 + index,
                True,
            )
        )

    wall_colors = {
        "floor": (0.513725, 0.725490, 0.909804),
        "left": (0.450980, 0.768627, 0.686275),
        "back": (0.956863, 0.686275, 0.388235),
    }
    for wall_name, bounds in wall_bounds_mm(cfg).items():
        r, g, b = wall_colors.get(wall_name, (0.513725, 0.725490, 0.909804))
        lines.extend(
            vbs_box_body_commands(
                f"BoundaryWall_{wall_name}",
                bounds,
                (r, g, b),
                0.72,
                80 + len(lines) % 10,
                False,
            )
        )


def vbs_box_body_commands(name, bounds, color, transparency, failure_code, fatal_on_failure):
    x0, y0, z0, x1, y1, z1 = bounds_to_m(bounds)
    depth = z1 - z0
    z_mid = (z0 + z1) / 2.0
    r, g, b = color
    fatal_literal = "True" if fatal_on_failure else "False"
    return [
        "Set swBoxFeat = CreateBoxBody("
        f"\"{vbs_literal(name)}\", "
        f"{x0:.10f}, {y0:.10f}, {x1:.10f}, {y1:.10f}, {z_mid:.10f}, {depth:.10f}, "
        f"{r:.6f}, {g:.6f}, {b:.6f}, {transparency:.6f}, {int(failure_code)}, {fatal_literal})"
    ]


def vbs_literal(value):
    return str(value).replace('"', '""')


def safe_vbs_identifier(value):
    text = "".join(ch if ch.isalnum() else "_" for ch in str(value))
    if not text or text[0].isdigit():
        text = f"Label_{text}"
    return text


def rgb01(hex_color):
    text = str(hex_color or "#2f80ed").strip().lstrip("#")
    if len(text) != 6:
        text = "2f80ed"
    try:
        return tuple(int(text[i:i + 2], 16) / 255.0 for i in (0, 2, 4))
    except ValueError:
        return (0.184314, 0.501961, 0.929412)


def remove_duplicate_points(points):
    result = []
    for point in points:
        if not result or distance(result[-1], point) > EPS:
            result.append(point)
    return result


def solidworks_path_entities(grid_points, grid_unit, bend_radius):
    mm_points = remove_duplicate_points([grid_to_mm(point, grid_unit) for point in grid_points])
    if len(mm_points) < 2:
        return []
    entities = []
    current = mm_points[0]
    for index in range(1, len(mm_points) - 1):
        prev_point = mm_points[index - 1]
        point = mm_points[index]
        next_point = mm_points[index + 1]
        v_in = normalize(vector(prev_point, point))
        v_out = normalize(vector(point, next_point))
        turn_dot = dot(v_in, v_out)
        if turn_dot > 0.999:
            continue
        if turn_dot < -0.999:
            if distance(current, point) > EPS:
                entities.append({"type": "line", "start": current, "end": point})
            current = point
            continue
        prev_len = distance(prev_point, point)
        next_len = distance(point, next_point)
        trim = min(bend_radius, prev_len * 0.45, next_len * 0.45)
        if trim <= EPS:
            if distance(current, point) > EPS:
                entities.append({"type": "line", "start": current, "end": point})
            current = point
            continue
        before = sub(point, scale(v_in, trim))
        after = add(point, scale(v_out, trim))
        if distance(current, before) > EPS:
            entities.append({"type": "line", "start": current, "end": before})
        entities.append({
            "type": "arc",
            "start": before,
            "end": after,
            "mid": fillet_arc_midpoint(before, after, v_in, v_out, trim),
        })
        current = after
    if distance(current, mm_points[-1]) > EPS:
        entities.append({"type": "line", "start": current, "end": mm_points[-1]})
    return entities


def fillet_arc_midpoint(before, after, v_in, v_out, radius):
    center = add(before, scale(v_out, radius))
    radial_start = normalize(vector(center, before))
    radial_end = normalize(vector(center, after))
    mid_dir = normalize(add(radial_start, radial_end))
    if distance((0.0, 0.0, 0.0), mid_dir) <= EPS:
        return quadratic_bezier(before, add(before, scale(v_in, radius)), after, 0.5)
    return add(center, scale(mid_dir, radius))


def vbs_path_entities(entities):
    commands = []
    for entity in entities:
        if entity["type"] == "line":
            a = entity["start"]
            b = entity["end"]
            if distance(a, b) <= EPS:
                continue
            commands.append(
                "swModel.SketchManager.CreateLine "
                f"{a[0] / 1000.0:.10f}, {a[1] / 1000.0:.10f}, {a[2] / 1000.0:.10f}, "
                f"{b[0] / 1000.0:.10f}, {b[1] / 1000.0:.10f}, {b[2] / 1000.0:.10f}"
            )
        elif entity["type"] == "arc":
            start = entity["start"]
            end = entity["end"]
            mid = entity["mid"]
            commands.append(
                "swModel.SketchManager.Create3PointArc "
                f"{start[0] / 1000.0:.10f}, {start[1] / 1000.0:.10f}, {start[2] / 1000.0:.10f}, "
                f"{end[0] / 1000.0:.10f}, {end[1] / 1000.0:.10f}, {end[2] / 1000.0:.10f}, "
                f"{mid[0] / 1000.0:.10f}, {mid[1] / 1000.0:.10f}, {mid[2] / 1000.0:.10f}"
            )
    return commands


def vbs_line_segments(points):
    commands = []
    for a, b in zip(points, points[1:]):
        if distance(a, b) <= EPS:
            continue
        commands.append(
            "swModel.SketchManager.CreateLine "
            f"{a[0] / 1000.0:.10f}, {a[1] / 1000.0:.10f}, {a[2] / 1000.0:.10f}, "
            f"{b[0] / 1000.0:.10f}, {b[1] / 1000.0:.10f}, {b[2] / 1000.0:.10f}"
        )
    return commands


def bounds_to_m(bounds):
    return tuple(float(value) / 1000.0 for value in bounds)


def write_solidworks_import_vbs(path, stl_path, sldprt_path, step_path):
    log_path = path.with_name("solidworks_cable_log.txt")
    lines = [
        "Option Explicit",
        "Dim swApp, swModel, partTemplate, errors, warnings, saveOk, longStatus, longWarnings, fso, importData",
        "Sub WriteLog(message)",
        "  Dim logFso, logFile",
        "  Set logFso = CreateObject(\"Scripting.FileSystemObject\")",
        f"  Set logFile = logFso.OpenTextFile(\"{log_path}\", 8, True)",
        "  logFile.WriteLine Now & \" \" & message",
        "  logFile.Close",
        "End Sub",
        "On Error Resume Next",
        "Set swApp = GetObject(, \"SldWorks.Application\")",
        "If Err.Number <> 0 Or swApp Is Nothing Then",
        "  Err.Clear",
        "  Set swApp = CreateObject(\"SldWorks.Application\")",
        "End If",
        "On Error GoTo 0",
        "If swApp Is Nothing Then",
        "  WriteLog \"Cannot connect to SolidWorks COM automation.\"",
        "  WScript.Quit 1",
        "End If",
        "swApp.Visible = True",
        "Set fso = CreateObject(\"Scripting.FileSystemObject\")",
        "errors = CLng(0)",
        "warnings = CLng(0)",
        "longStatus = CLng(0)",
        "longWarnings = CLng(0)",
        f"WriteLog \"Opening STL: {stl_path}\"",
        "On Error Resume Next",
        f"Set swModel = swApp.OpenDoc6(\"{stl_path}\", CLng(1), CLng(1), \"\", errors, warnings)",
        "WriteLog \"OpenDoc6 returned. err=\" & CStr(Err.Number) & \", desc=\" & Err.Description & \", errors=\" & CStr(errors) & \", warnings=\" & CStr(warnings)",
        "If Err.Number <> 0 Then Err.Clear",
        "If swModel Is Nothing Then",
        "  WriteLog \"OpenDoc6 returned no model\"",
        "Else",
        "  WriteLog \"OpenDoc6 model acquired\"",
        "End If",
        "If swModel Is Nothing Then",
        "  WriteLog \"Trying legacy OpenDoc STL import fallback\"",
        f"  Set swModel = swApp.OpenDoc(\"{stl_path}\", CLng(1))",
        "  WriteLog \"OpenDoc returned. err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
        "  If Err.Number <> 0 Then Err.Clear",
        "  If swModel Is Nothing Then",
        "    WriteLog \"OpenDoc returned no model\"",
        "  Else",
        "    WriteLog \"OpenDoc model acquired\"",
        "  End If",
        "End If",
        "If swModel Is Nothing Then",
        "  WriteLog \"Trying GetImportFileData + LoadFile4 STL import fallback\"",
        f"  Set importData = swApp.GetImportFileData(\"{stl_path}\")",
        "  WriteLog \"GetImportFileData err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
        "  If Err.Number <> 0 Then Err.Clear",
        "  errors = CLng(0)",
        f"  Set swModel = swApp.LoadFile4(\"{stl_path}\", \"\", importData, errors)",
        "  WriteLog \"LoadFile4 returned. err=\" & CStr(Err.Number) & \", desc=\" & Err.Description & \", errors=\" & CStr(errors)",
        "  If Err.Number <> 0 Then Err.Clear",
        "  If swModel Is Nothing Then",
        "    WriteLog \"LoadFile4 returned no model\"",
        "  Else",
        "    WriteLog \"LoadFile4 model acquired\"",
        "  End If",
        "End If",
        "If swModel Is Nothing Then",
        "  Set swModel = swApp.ActiveDoc",
        "  WriteLog \"ActiveDoc fallback. err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
        "  If Err.Number <> 0 Then Err.Clear",
        "  If swModel Is Nothing Then",
        "    WriteLog \"ActiveDoc fallback returned no model\"",
        "  Else",
        "    WriteLog \"ActiveDoc fallback model acquired\"",
        "  End If",
        "End If",
        "On Error GoTo 0",
        "If swModel Is Nothing Then",
        "  WriteLog \"OpenDoc6 failed. errors=\" & CStr(errors) & \", warnings=\" & CStr(warnings)",
        "  WScript.Quit 2",
        "End If",
        "WriteLog \"Rebuilding imported STL document\"",
        "On Error Resume Next",
        "swModel.EditRebuild3",
        "WriteLog \"EditRebuild3 err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
        "If Err.Number <> 0 Then Err.Clear",
        "On Error GoTo 0",
        f"If fso.FileExists(\"{sldprt_path}\") Then fso.DeleteFile \"{sldprt_path}\", True",
        f"WriteLog \"Saving SLDPRT: {sldprt_path}\"",
        "On Error Resume Next",
        f"saveOk = swModel.Extension.SaveAs(\"{sldprt_path}\", CLng(0), CLng(1), Nothing, longStatus, longWarnings)",
        "WriteLog \"SLDPRT SaveAs result=\" & CStr(saveOk) & \", status=\" & CStr(longStatus) & \", warnings=\" & CStr(longWarnings) & \", err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
        "If Err.Number <> 0 Then Err.Clear",
        f"If Not fso.FileExists(\"{sldprt_path}\") Then",
        f"  saveOk = swModel.SaveAs3(\"{sldprt_path}\", CLng(0), CLng(2))",
        "  WriteLog \"SLDPRT SaveAs3 fallback result=\" & CStr(saveOk) & \", err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
        "  If Err.Number <> 0 Then Err.Clear",
        "End If",
        f"If Not fso.FileExists(\"{sldprt_path}\") Then",
        "  WriteLog \"SLDPRT output missing after SaveAs.\"",
        "  WScript.Quit 3",
        "End If",
        f"If fso.FileExists(\"{step_path}\") Then fso.DeleteFile \"{step_path}\", True",
        f"saveOk = swModel.Extension.SaveAs(\"{step_path}\", CLng(0), CLng(1), Nothing, longStatus, longWarnings)",
        "WriteLog \"STEP SaveAs result=\" & CStr(saveOk) & \", status=\" & CStr(longStatus) & \", warnings=\" & CStr(longWarnings)",
        "On Error GoTo 0",
        "WriteLog \"Cable routing scene model created.\"",
        "WScript.Quit 0",
    ]
    path.write_text("\n".join(lines) + "\n", encoding="ascii")


def solidworks_log_message(job_dir, fallback):
    log_path = job_dir / "solidworks_cable_log.txt"
    if log_path.exists():
        lines = [line.strip() for line in log_path.read_text(encoding="utf-8", errors="ignore").splitlines() if line.strip()]
        if lines:
            return fallback + " " + " | ".join(lines[-5:])
    return fallback


def vector(a, b):
    return (b[0] - a[0], b[1] - a[1], b[2] - a[2])


def add(a, b):
    return (a[0] + b[0], a[1] + b[1], a[2] + b[2])


def sub(a, b):
    return (a[0] - b[0], a[1] - b[1], a[2] - b[2])


def scale(a, value):
    return (a[0] * value, a[1] * value, a[2] * value)


def dot(a, b):
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]


def cross(a, b):
    return (
        a[1] * b[2] - a[2] * b[1],
        a[2] * b[0] - a[0] * b[2],
        a[0] * b[1] - a[1] * b[0],
    )


def normalize(a):
    length = math.sqrt(dot(a, a))
    if length < EPS:
        return (1.0, 0.0, 0.0)
    return (a[0] / length, a[1] / length, a[2] / length)


def distance(a, b):
    return math.sqrt(dot(vector(a, b), vector(a, b)))


def almost_same(a, b):
    return distance(a, b) < 1e-6


def quadratic_bezier(a, b, c, t):
    return add(add(scale(a, (1 - t) * (1 - t)), scale(b, 2 * (1 - t) * t)), scale(c, t * t))
