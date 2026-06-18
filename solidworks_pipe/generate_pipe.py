import csv
import json
import math
from pathlib import Path


EPS = 1e-9


def add(a, b):
    return (a[0] + b[0], a[1] + b[1])


def sub(a, b):
    return (a[0] - b[0], a[1] - b[1])


def mul(a, s):
    return (a[0] * s, a[1] * s)


def dot(a, b):
    return a[0] * b[0] + a[1] * b[1]


def rot(a, ang):
    c = math.cos(ang)
    s = math.sin(ang)
    return (a[0] * c - a[1] * s, a[0] * s + a[1] * c)


def v3_sub(a, b):
    return (a[0] - b[0], a[1] - b[1], a[2] - b[2])


def v3_cross(a, b):
    return (
        a[1] * b[2] - a[2] * b[1],
        a[2] * b[0] - a[0] * b[2],
        a[0] * b[1] - a[1] * b[0],
    )


def v3_norm(a):
    length = math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2])
    if length < EPS:
        return (0.0, 0.0, 1.0)
    return (a[0] / length, a[1] / length, a[2] / length)


def arc_delta(radius, angle):
    if abs(angle) < EPS:
        return (0.0, 0.0)
    turn_sign = 1.0 if angle > 0 else -1.0
    return (
        turn_sign * radius * math.sin(angle),
        turn_sign * radius * (1.0 - math.cos(angle)),
    )


def solve_start_angle_and_l3(cfg):
    total_x = float(cfg["total_horizontal_mm"])
    total_y = -float(cfg["total_vertical_mm"])
    l1 = float(cfg["L1_mm"])
    l2 = float(cfg["L2_mm"])
    radius = float(cfg["R_mm"])
    theta1 = math.radians(float(cfg["theta1_deg"]))
    theta2 = math.radians(float(cfg["theta2_deg"]))

    a = (0.0, 0.0)
    a = add(a, (l1, 0.0))
    a = add(a, arc_delta(radius, theta1))
    a = add(a, rot((l2, 0.0), theta1))
    a = add(a, rot(arc_delta(radius, theta2), theta1))

    beta = theta1 + theta2
    u = (math.cos(beta), math.sin(beta))
    n = (-math.sin(beta), math.cos(beta))
    d = (total_x, total_y)
    d_len = math.hypot(d[0], d[1])
    c = dot(a, n)

    if abs(c) > d_len + 1e-7:
        raise ValueError(
            "The selected L1, L2, R and angles cannot close between the fixed endpoints."
        )

    angle_d = math.atan2(d[1], d[0])
    angle_n = math.atan2(n[1], n[0])
    acos_arg = max(-1.0, min(1.0, c / d_len))
    candidates = []

    for sign in (1.0, -1.0):
        phi = angle_d - angle_n + sign * math.acos(acos_arg)
        local_d = rot(d, -phi)
        l3 = dot(sub(local_d, a), u)
        err = abs(dot(sub(local_d, a), n))
        if l3 > 0.0:
            candidates.append((err, l3, phi))

    if not candidates:
        raise ValueError("No positive L3 solution exists for these parameters.")

    candidates.sort(key=lambda item: (item[0], item[1]))
    _, l3, phi = candidates[0]
    return phi, l3


def sample_line(points, start, heading, length, step):
    count = max(1, int(math.ceil(length / step)))
    for i in range(1, count + 1):
        s = length * i / count
        points.append(add(start, (s * math.cos(heading), s * math.sin(heading))))
    return points[-1]


def sample_arc(points, start, heading, radius, angle, step):
    arc_len = abs(radius * angle)
    count = max(4, int(math.ceil(arc_len / step)))
    sign = 1.0 if angle >= 0 else -1.0
    center = add(start, mul((-math.sin(heading), math.cos(heading)), sign * radius))
    start_vec = sub(start, center)

    for i in range(1, count + 1):
        a = angle * i / count
        points.append(add(center, rot(start_vec, a)))
    return points[-1]


def unique_consecutive_points(points, min_distance=1e-7):
    if not points:
        return []
    filtered = [points[0]]
    for point in points[1:]:
        last = filtered[-1]
        if math.hypot(point[0] - last[0], point[1] - last[1]) >= min_distance:
            filtered.append(point)
    return filtered


def centerline_points(cfg):
    phi, l3 = solve_start_angle_and_l3(cfg)
    l1 = float(cfg["L1_mm"])
    l2 = float(cfg["L2_mm"])
    radius = float(cfg["R_mm"])
    theta1 = math.radians(float(cfg["theta1_deg"]))
    theta2 = math.radians(float(cfg["theta2_deg"]))
    step = max(0.5, float(cfg.get("centerline_step_mm", 5.0)))

    p = (0.0, 0.0)
    points = [p]
    p = sample_line(points, p, phi, l1, step)
    p = sample_arc(points, p, phi, radius, theta1, step)
    p = sample_line(points, p, phi + theta1, l2, step)
    p = sample_arc(points, p, phi + theta1, radius, theta2, step)
    p = sample_line(points, p, phi + theta1 + theta2, l3, step)

    end = (float(cfg["total_horizontal_mm"]), -float(cfg["total_vertical_mm"]))
    if math.hypot(points[-1][0] - end[0], points[-1][1] - end[1]) > 1e-5:
        raise ValueError("Internal geometry error: generated path does not reach the fixed endpoint.")
    points[-1] = end
    return unique_consecutive_points(points), phi, l3


def exact_path_segments(cfg):
    phi, l3 = solve_start_angle_and_l3(cfg)
    l1 = float(cfg["L1_mm"])
    l2 = float(cfg["L2_mm"])
    radius = float(cfg["R_mm"])
    theta1 = math.radians(float(cfg["theta1_deg"]))
    theta2 = math.radians(float(cfg["theta2_deg"]))

    segments = []
    p = (0.0, 0.0)
    heading = phi

    def line(length):
        nonlocal p, heading
        q = add(p, (length * math.cos(heading), length * math.sin(heading)))
        segments.append(("line", p, q, None, None, None))
        p = q

    def arc(angle):
        nonlocal p, heading
        if abs(angle) < EPS:
            return
        turn_sign = 1.0 if angle > 0 else -1.0
        center = add(p, mul((-math.sin(heading), math.cos(heading)), turn_sign * radius))
        start_vec = sub(p, center)
        mid = add(center, rot(start_vec, angle / 2.0))
        q = add(center, rot(start_vec, angle))
        segments.append(("arc", p, q, center, angle > 0, mid))
        p = q
        heading += angle

    line(l1)
    arc(theta1)
    line(l2)
    arc(theta2)
    line(l3)
    return segments, phi, l3


def extended_wall_cut_segments(path_segments, extension_mm):
    if extension_mm <= 0:
        return path_segments

    segments = list(path_segments)
    first = segments[0]
    last = segments[-1]

    first_kind, first_start, first_end, first_center, first_is_ccw, first_mid = first
    last_kind, last_start, last_end, last_center, last_is_ccw, last_mid = last
    if first_kind != "line" or last_kind != "line":
        raise ValueError("The first and last path entities must be straight segments.")

    first_len = math.hypot(first_end[0] - first_start[0], first_end[1] - first_start[1])
    last_len = math.hypot(last_end[0] - last_start[0], last_end[1] - last_start[1])
    if first_len < EPS or last_len < EPS:
        raise ValueError("The first and last straight segments must have positive length.")

    first_u = ((first_end[0] - first_start[0]) / first_len, (first_end[1] - first_start[1]) / first_len)
    last_u = ((last_end[0] - last_start[0]) / last_len, (last_end[1] - last_start[1]) / last_len)
    extended_start = sub(first_start, mul(first_u, extension_mm))
    extended_end = add(last_end, mul(last_u, extension_mm))

    segments[0] = (first_kind, extended_start, first_end, first_center, first_is_ccw, first_mid)
    segments[-1] = (last_kind, last_start, extended_end, last_center, last_is_ccw, last_mid)
    return segments


def write_centerline_csv(path, points, phi, l3):
    with path.open("w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["x_mm", "y_mm", "z_mm"])
        for x, y in points:
            writer.writerow([f"{x:.6f}", f"{y:.6f}", "0.000000"])
        writer.writerow([])
        writer.writerow(["initial_angle_deg", f"{math.degrees(phi):.6f}"])
        writer.writerow(["L3_mm", f"{l3:.6f}"])


def max_tangent_error_deg(path_segments):
    errors = []
    prev_heading = None
    for kind, start, end, center, is_counterclockwise, _mid in path_segments:
        if kind == "line":
            heading = math.atan2(end[1] - start[1], end[0] - start[0])
            if prev_heading is not None:
                errors.append(angle_diff_deg(prev_heading, heading))
            prev_heading = heading
            continue

        radius_start = sub(start, center)
        radius_end = sub(end, center)
        start_tangent = math.atan2(radius_start[1], radius_start[0]) + math.pi / 2.0
        end_tangent = math.atan2(radius_end[1], radius_end[0]) + math.pi / 2.0
        if not is_counterclockwise:
            start_tangent -= math.pi
            end_tangent -= math.pi
        if prev_heading is not None:
            errors.append(angle_diff_deg(prev_heading, start_tangent))
        prev_heading = end_tangent
    return max(errors) if errors else 0.0


def angle_diff_deg(a, b):
    diff = (a - b + math.pi) % (2.0 * math.pi) - math.pi
    return abs(math.degrees(diff))


def tube_vertices(points, tube_radius, segments):
    rings = []
    z_axis = (0.0, 0.0, 1.0)
    for i, p in enumerate(points):
        if i == 0:
            q = points[1]
        elif i == len(points) - 1:
            q = points[-2]
        else:
            q = points[i + 1]
        tangent = v3_norm((q[0] - p[0], q[1] - p[1], 0.0))
        normal = v3_norm(v3_cross(z_axis, tangent))
        if normal == (0.0, 0.0, 1.0):
            normal = (1.0, 0.0, 0.0)
        ring = []
        for j in range(segments):
            a = 2.0 * math.pi * j / segments
            ca = math.cos(a)
            sa = math.sin(a)
            ring.append(
                (
                    p[0] + tube_radius * ca * normal[0],
                    p[1] + tube_radius * ca * normal[1],
                    tube_radius * sa,
                )
            )
        rings.append(ring)
    return rings


def pipe_diameters(cfg):
    outer_diameter = float(cfg["pipe_outer_diameter_mm"])
    if "pipe_inner_diameter_mm" in cfg and str(cfg["pipe_inner_diameter_mm"]).strip():
        inner_diameter = float(cfg["pipe_inner_diameter_mm"])
    else:
        wall_thickness = float(cfg.get("wall_thickness_mm", 0.9))
        inner_diameter = outer_diameter - 2.0 * wall_thickness

    if outer_diameter <= 0:
        raise ValueError("pipe_outer_diameter_mm must be greater than 0.")
    if inner_diameter <= 0:
        raise ValueError("pipe_inner_diameter_mm must be greater than 0.")
    if inner_diameter >= outer_diameter:
        raise ValueError("pipe_inner_diameter_mm must be smaller than pipe_outer_diameter_mm.")

    return outer_diameter, inner_diameter, (outer_diameter - inner_diameter) / 2.0


def write_ascii_stl(path, points, outer_radius, inner_radius, segments):
    outer_rings = tube_vertices(points, outer_radius, segments)
    inner_rings = tube_vertices(points, inner_radius, segments)

    def facet(f, a, b, c):
        normal = v3_norm(v3_cross(v3_sub(b, a), v3_sub(c, a)))
        f.write(f"  facet normal {normal[0]:.8e} {normal[1]:.8e} {normal[2]:.8e}\n")
        f.write("    outer loop\n")
        for v in (a, b, c):
            f.write(f"      vertex {v[0]:.8e} {v[1]:.8e} {v[2]:.8e}\n")
        f.write("    endloop\n")
        f.write("  endfacet\n")

    with path.open("w", encoding="ascii") as f:
        f.write("solid parametric_hollow_pipe\n")
        for i in range(len(outer_rings) - 1):
            for j in range(segments):
                j2 = (j + 1) % segments
                facet(f, outer_rings[i][j], outer_rings[i + 1][j], outer_rings[i + 1][j2])
                facet(f, outer_rings[i][j], outer_rings[i + 1][j2], outer_rings[i][j2])
                facet(f, inner_rings[i][j2], inner_rings[i + 1][j2], inner_rings[i + 1][j])
                facet(f, inner_rings[i][j2], inner_rings[i + 1][j], inner_rings[i][j])

        for j in range(segments):
            j2 = (j + 1) % segments
            facet(f, outer_rings[0][j2], outer_rings[0][j], inner_rings[0][j])
            facet(f, outer_rings[0][j2], inner_rings[0][j], inner_rings[0][j2])
            facet(f, outer_rings[-1][j], outer_rings[-1][j2], inner_rings[-1][j])
            facet(f, outer_rings[-1][j2], inner_rings[-1][j2], inner_rings[-1][j])
        f.write("endsolid parametric_hollow_pipe\n")


def write_solidworks_vbs(path, cfg, path_segments, sampled_points, phi, l3):
    out_part = path.with_name("pipe_native.SLDPRT")
    outer_diameter_mm, inner_diameter_mm, wall_thickness_mm = pipe_diameters(cfg)
    cut_extension_mm = max(
        float(cfg.get("wall_cut_extension_mm", 80.0)),
        outer_diameter_mm * 3.0,
        20.0,
    )
    opening_cut_depth_mm = max(outer_diameter_mm * 6.0, 30.0)
    inner_cut_segments = extended_wall_cut_segments(path_segments, cut_extension_mm)
    diameter_m = outer_diameter_mm / 1000.0
    inner_diameter_m = inner_diameter_mm / 1000.0
    wall_thickness_m = wall_thickness_mm / 1000.0

    first_kind, first_start, first_end, _first_center, _first_is_ccw, _first_mid = path_segments[0]
    last_kind, last_start, last_end, _last_center, _last_is_ccw, _last_mid = path_segments[-1]
    if first_kind != "line" or last_kind != "line":
        raise ValueError("The first and last path entities must be straight segments.")
    first_len = math.hypot(first_end[0] - first_start[0], first_end[1] - first_start[1])
    last_len = math.hypot(last_end[0] - last_start[0], last_end[1] - last_start[1])
    first_u = ((first_end[0] - first_start[0]) / first_len, (first_end[1] - first_start[1]) / first_len)
    last_u = ((last_end[0] - last_start[0]) / last_len, (last_end[1] - last_start[1]) / last_len)
    select_radius_m = max(diameter_m * 2.0, 0.01)
    lines = [
        "Option Explicit",
        "",
        "Function FindLast3DSketchFeature(model)",
        "  Dim feat, last3dSketch, typeName",
        "  Set last3dSketch = Nothing",
        "  Set feat = model.FirstFeature",
        "  Do While Not feat Is Nothing",
        "    typeName = feat.GetTypeName2",
        "    If typeName = \"3DProfileFeature\" Then",
        "      Set last3dSketch = feat",
        "    End If",
        "    Set feat = feat.GetNextFeature",
        "  Loop",
        "  Set FindLast3DSketchFeature = last3dSketch",
        "End Function",
        "",
        "Function FindNthFeatureByType(model, wantedType, wantedIndex)",
        "  Dim feat, count",
        "  count = 0",
        "  Set feat = model.FirstFeature",
        "  Do While Not feat Is Nothing",
        "    If feat.GetTypeName2 = wantedType Then",
        "      count = count + 1",
        "      If count = wantedIndex Then",
        "        Set FindNthFeatureByType = feat",
        "        Exit Function",
        "      End If",
        "    End If",
        "    Set feat = feat.GetNextFeature",
        "  Loop",
        "  Set FindNthFeatureByType = Nothing",
        "End Function",
        "",
        "Function FindLastFeatureByType(model, wantedType)",
        "  Dim feat, lastFeat",
        "  Set lastFeat = Nothing",
        "  Set feat = model.FirstFeature",
        "  Do While Not feat Is Nothing",
        "    If feat.GetTypeName2 = wantedType Then",
        "      Set lastFeat = feat",
        "    End If",
        "    Set feat = feat.GetNextFeature",
        "  Loop",
        "  Set FindLastFeatureByType = lastFeat",
        "End Function",
        "",
        "Function SelectPipeEndFacesForShell(model, sx, sy, sz, stx, sty, stz, ex, ey, ez, etx, ety, etz, searchRadius)",
        "  Dim ok1, ok2",
        "  model.ClearSelection2 True",
        "  ok1 = model.Extension.SelectByRay(sx - stx * searchRadius, sy - sty * searchRadius, sz - stz * searchRadius, stx, sty, stz, searchRadius, 2, False, 0, 0)",
        "  ok2 = model.Extension.SelectByRay(ex + etx * searchRadius, ey + ety * searchRadius, ez + etz * searchRadius, -etx, -ety, -etz, searchRadius, 2, True, 0, 0)",
        "  SelectPipeEndFacesForShell = ok1 And ok2",
        "End Function",
        "",
        "Function ApplyPipeShell(model, thickness, sx, sy, sz, stx, sty, stz, ex, ey, ez, etx, ety, etz, searchRadius)",
        "  Dim ok, shellFeat",
        "  Set shellFeat = Nothing",
        "  ok = SelectPipeEndFacesForShell(model, sx, sy, sz, stx, sty, stz, ex, ey, ez, etx, ety, etz, searchRadius)",
        "  If Not ok Then",
        "    Set ApplyPipeShell = Nothing",
        "    Exit Function",
        "  End If",
        "  On Error Resume Next",
        "  Set shellFeat = model.FeatureManager.InsertShell(thickness, False)",
        "  If shellFeat Is Nothing Then",
        "    Err.Clear",
        "    model.ClearSelection2 True",
        "    ok = SelectPipeEndFacesForShell(model, sx, sy, sz, stx, sty, stz, ex, ey, ez, etx, ety, etz, searchRadius)",
        "    If ok Then Set shellFeat = model.FeatureManager.InsertShell(thickness, True)",
        "  End If",
        "  If Err.Number <> 0 Then Err.Clear",
        "  On Error GoTo 0",
        "  If Not shellFeat Is Nothing Then shellFeat.Name = \"PipeHollowShell\"",
        "  Set ApplyPipeShell = shellFeat",
        "End Function",
        "",
        "Function ApplySweptInnerCut(model, pathFeat, innerDiameter)",
        "  Dim ok, cutFeat",
        "  Set cutFeat = Nothing",
        "  model.ClearSelection2 True",
        "  ok = False",
        "  If Not pathFeat Is Nothing Then ok = pathFeat.Select2(False, 4)",
        "  If Not ok Then ok = model.Extension.SelectByID2(\"InnerBorePath3D\", \"SKETCH\", 0, 0, 0, False, 4, Nothing, 0)",
        "  If Not ok Then",
        "    Set ApplySweptInnerCut = Nothing",
        "    Exit Function",
        "  End If",
        "  On Error Resume Next",
        "  Set cutFeat = model.FeatureManager.InsertCutSwept5(False, False, 0, False, False, 0, 0, False, 0, 0, 0, 0, True, True, 0, True, False, True, True, True, innerDiameter, 0)",
        "  If Err.Number <> 0 Then Err.Clear",
        "  On Error GoTo 0",
        "  If Not cutFeat Is Nothing Then cutFeat.Name = \"PipeInnerBoreCut\"",
        "  Set ApplySweptInnerCut = cutFeat",
        "End Function",
        "",
        "Function CutWithPlane(model, planeFeat, cutName, flipSide)",
        "  Dim ok, cutFeat, errors",
        "  Set cutFeat = Nothing",
        "  errors = 0",
        "  model.ClearSelection2 True",
        "  ok = planeFeat.Select2(False, 0)",
        "  If Not ok Then",
        "    Set CutWithPlane = Nothing",
        "    Exit Function",
        "  End If",
        "  Set cutFeat = model.FeatureManager.InsertCutSurface(flipSide, 0, False, True, Nothing, errors)",
        "  If Not cutFeat Is Nothing Then cutFeat.Name = cutName",
        "  Set CutWithPlane = cutFeat",
        "End Function",
        "",
        "Dim swApp, swModel, swFeat, swPathSketch, swPathFeat, swInnerPathSketch, swInnerPathFeat, swShellFeat, swInnerCutFeat, ok, partTemplate, outPart",
        "Dim swStartOpenPathFeat, swEndOpenPathFeat, swStartOpenCutFeat, swEndOpenCutFeat",
        "Dim swRightBasePlaneFeat, swRightWallPlaneFeat, swLeftCutFeat, swRightCutFeat, swRefPlane",
        "Set swApp = CreateObject(\"SldWorks.Application\")",
        "swApp.Visible = True",
        "partTemplate = swApp.GetUserPreferenceStringValue(1)",
        "If Len(partTemplate) > 0 Then",
        "  Set swModel = swApp.NewDocument(partTemplate, 0, 0, 0)",
        "Else",
        "  swApp.NewPart",
        "  Set swModel = swApp.ActiveDoc",
        "End If",
        "If swModel Is Nothing Then",
        "  MsgBox \"Cannot create a new SolidWorks part. Please check the default part template.\", vbExclamation, \"Native pipe model\"",
        "  WScript.Quit 1",
        "End If",
        "",
        "swModel.SketchManager.Insert3DSketch True",
        "Set swPathSketch = swModel.GetActiveSketch2",
        "swModel.SketchManager.AddToDB = True",
    ]

    def m(value):
        return value / 1000.0

    for kind, start, end, center, _is_counterclockwise, mid in path_segments:
        if kind == "line":
            lines.append(
                "swModel.SketchManager.CreateLine "
                f"{m(start[0]):.10f}, {m(start[1]):.10f}, 0, "
                f"{m(end[0]):.10f}, {m(end[1]):.10f}, 0"
            )
        else:
            lines.append(
                "swModel.SketchManager.Create3PointArc "
                f"{m(start[0]):.10f}, {m(start[1]):.10f}, 0, "
                f"{m(end[0]):.10f}, {m(end[1]):.10f}, 0, "
                f"{m(mid[0]):.10f}, {m(mid[1]):.10f}, 0"
            )

    lines.extend(
        [
            "swModel.SketchManager.AddToDB = False",
            "swModel.SketchManager.Insert3DSketch True",
            "Set swPathFeat = FindLast3DSketchFeature(swModel)",
            "If Not swPathFeat Is Nothing Then swPathFeat.Name = \"PipePath3D\"",
            "swModel.ClearSelection2 True",
            "",
            "ok = False",
            "If Not swPathFeat Is Nothing Then ok = swPathFeat.Select2(False, 4)",
            "If Not ok Then ok = swModel.Extension.SelectByID2(\"PipePath3D\", \"SKETCH\", 0, 0, 0, False, 4, Nothing, 0)",
            "If Not ok Then",
            "  MsgBox \"The path sketch was created, but SolidWorks could not select PipePath3D for sweep. Please select it manually and use Sweep Boss/Base with circular profile.\", vbExclamation, \"Native pipe model\"",
            "  WScript.Quit 0",
            "End If",
            "",
            "Set swFeat = swModel.FeatureManager.InsertProtrusionSwept4(False, False, 0, False, False, 0, 0, False, 0, 0, 0, 0, True, True, True, 0, True, True, "
            f"{diameter_m:.10f}, 0)",
            "If swFeat Is Nothing Then",
            "  MsgBox \"The 3D path was created, but the automatic sweep failed. Please check that pipe_outer_diameter_mm is smaller than the bend radius, then run Sweep Boss/Base using circular profile.\", vbExclamation, \"Native pipe model\"",
            "  WScript.Quit 1",
            "End If",
            "",
            "swModel.SketchManager.Insert3DSketch True",
            "Set swInnerPathSketch = swModel.GetActiveSketch2",
            "swModel.SketchManager.AddToDB = True",
        ]
    )

    for kind, start, end, center, _is_counterclockwise, mid in inner_cut_segments:
        if kind == "line":
            lines.append(
                "swModel.SketchManager.CreateLine "
                f"{m(start[0]):.10f}, {m(start[1]):.10f}, 0, "
                f"{m(end[0]):.10f}, {m(end[1]):.10f}, 0"
            )
        else:
            lines.append(
                "swModel.SketchManager.Create3PointArc "
                f"{m(start[0]):.10f}, {m(start[1]):.10f}, 0, "
                f"{m(end[0]):.10f}, {m(end[1]):.10f}, 0, "
                f"{m(mid[0]):.10f}, {m(mid[1]):.10f}, 0"
            )

    lines.extend(
        [
            "swModel.SketchManager.AddToDB = False",
            "swModel.SketchManager.Insert3DSketch True",
            "Set swInnerPathFeat = FindLast3DSketchFeature(swModel)",
            "If Not swInnerPathFeat Is Nothing Then swInnerPathFeat.Name = \"InnerBorePath3D\"",
            "swModel.ClearSelection2 True",
            "",
            "Set swShellFeat = ApplyPipeShell(swModel, "
            f"{wall_thickness_m:.10f}, "
            f"{m(first_start[0]):.10f}, {m(first_start[1]):.10f}, 0, "
            f"{first_u[0]:.10f}, {first_u[1]:.10f}, 0, "
            f"{m(last_end[0]):.10f}, {m(last_end[1]):.10f}, 0, "
            f"{last_u[0]:.10f}, {last_u[1]:.10f}, 0, {select_radius_m:.10f})",
            "If swShellFeat Is Nothing Then",
            f"  Set swInnerCutFeat = ApplySweptInnerCut(swModel, swInnerPathFeat, {inner_diameter_m:.10f})",
            "  If Not swInnerCutFeat Is Nothing Then",
            "    swModel.SketchManager.Insert3DSketch True",
            "    swModel.SketchManager.AddToDB = True",
            "    swModel.SketchManager.CreateLine "
            f"{m(first_start[0] - first_u[0] * cut_extension_mm):.10f}, {m(first_start[1] - first_u[1] * cut_extension_mm):.10f}, 0, "
            f"{m(first_start[0] + first_u[0] * opening_cut_depth_mm):.10f}, {m(first_start[1] + first_u[1] * opening_cut_depth_mm):.10f}, 0",
            "    swModel.SketchManager.AddToDB = False",
            "    swModel.SketchManager.Insert3DSketch True",
            "    Set swStartOpenPathFeat = FindLast3DSketchFeature(swModel)",
            "    If Not swStartOpenPathFeat Is Nothing Then swStartOpenPathFeat.Name = \"InnerBoreStartOpen3D\"",
            "    Set swStartOpenCutFeat = ApplySweptInnerCut(swModel, swStartOpenPathFeat, "
            f"{inner_diameter_m:.10f})",
            "    swModel.SketchManager.Insert3DSketch True",
            "    swModel.SketchManager.AddToDB = True",
            "    swModel.SketchManager.CreateLine "
            f"{m(last_end[0] + last_u[0] * cut_extension_mm):.10f}, {m(last_end[1] + last_u[1] * cut_extension_mm):.10f}, 0, "
            f"{m(last_end[0] - last_u[0] * opening_cut_depth_mm):.10f}, {m(last_end[1] - last_u[1] * opening_cut_depth_mm):.10f}, 0",
            "    swModel.SketchManager.AddToDB = False",
            "    swModel.SketchManager.Insert3DSketch True",
            "    Set swEndOpenPathFeat = FindLast3DSketchFeature(swModel)",
            "    If Not swEndOpenPathFeat Is Nothing Then swEndOpenPathFeat.Name = \"InnerBoreEndOpen3D\"",
            "    Set swEndOpenCutFeat = ApplySweptInnerCut(swModel, swEndOpenPathFeat, "
            f"{inner_diameter_m:.10f})",
            "  End If",
            "End If",
            "If swShellFeat Is Nothing And swInnerCutFeat Is Nothing Then",
            "  MsgBox \"The pipe outer sweep succeeded, but SolidWorks could not create the hollow bore. The model was not saved to avoid sending a solid pipe to ANSYS.\", vbExclamation, \"Native hollow pipe model\"",
            "  WScript.Quit 1",
            "End If",
            "",
            f"outPart = \"{str(out_part).replace(chr(92), chr(92) + chr(92))}\"",
            "swModel.SaveAs outPart",
            "swModel.ViewZoomtofit2",
            "MsgBox \"Native SolidWorks pipe model created.\" & vbCrLf & outPart & vbCrLf & \"L3 = "
            f"{l3:.3f} mm, initial angle = {math.degrees(phi):.3f} deg, wall = {wall_thickness_mm:.3f} mm\", vbInformation, \"Native pipe model\"",
        ]
    )
    path.write_text("\n".join(lines) + "\n", encoding="ascii")


def main():
    base = Path(__file__).resolve().parent
    cfg_path = base / "params.json"
    cfg = json.loads(cfg_path.read_text(encoding="utf-8"))

    points, phi, l3 = centerline_points(cfg)
    path_segments, _, _ = exact_path_segments(cfg)
    stl_path = base / cfg.get("output_stl", "pipe_model.stl")
    csv_path = base / cfg.get("output_centerline_csv", "pipe_centerline.csv")
    native_vbs_path = base / "create_pipe_native.vbs"

    outer_diameter_mm, inner_diameter_mm, wall_thickness_mm = pipe_diameters(cfg)
    outer_radius = outer_diameter_mm / 2.0
    inner_radius = inner_diameter_mm / 2.0
    segments = max(12, int(cfg.get("tube_segments", 32)))
    write_ascii_stl(stl_path, points, outer_radius, inner_radius, segments)
    write_centerline_csv(csv_path, points, phi, l3)
    write_solidworks_vbs(native_vbs_path, cfg, path_segments, points, phi, l3)

    print(f"Generated: {stl_path}")
    print(f"Generated: {csv_path}")
    print(f"Generated: {native_vbs_path}")
    print(f"Initial angle: {math.degrees(phi):.6f} deg")
    print(f"L3: {l3:.6f} mm")
    print(f"Max tangent error: {max_tangent_error_deg(path_segments):.9f} deg")
    print(f"Outer diameter: {outer_diameter_mm:.6f} mm")
    print(f"Inner diameter: {inner_diameter_mm:.6f} mm")
    print(f"Wall thickness: {wall_thickness_mm:.6f} mm")


if __name__ == "__main__":
    main()
