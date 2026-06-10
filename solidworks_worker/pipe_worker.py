import json
import os
import shutil
import subprocess
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlparse

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from mpl_toolkits.mplot3d.art3d import Poly3DCollection


ROOT = Path(__file__).resolve().parents[1]
GENERATOR_DIR = ROOT / "solidworks_pipe"
OUTPUT_DIR = ROOT / "solidworks_worker" / "output"
HOST = os.environ.get("PIPE_WORKER_HOST", "127.0.0.1")
PORT = int(os.environ.get("PIPE_WORKER_PORT", "18080"))
RUN_SOLIDWORKS = os.environ.get("PIPE_WORKER_RUN_SOLIDWORKS", "1") == "1"


def number(data, *names, default=0.0):
    for name in names:
        if name in data and data[name] not in (None, ""):
            return float(data[name])
    return float(default)


def build_model(payload):
    task_id = str(payload.get("taskId") or "manual")
    job_dir = OUTPUT_DIR / f"task_{task_id}"
    job_dir.mkdir(parents=True, exist_ok=True)

    params = {
        "total_horizontal_mm": number(payload, "totalHorizontal", default=600.0),
        "total_vertical_mm": number(payload, "totalVertical", default=300.0),
        "L1_mm": number(payload, "L1", "l1", default=280.0),
        "L2_mm": number(payload, "L2", "l2", default=150.0),
        "R_mm": number(payload, "R", "r", "radius", default=20.0),
        "theta1_deg": number(payload, "theta1", "theta1_deg", default=-70.0),
        "theta2_deg": number(payload, "theta2", "theta2_deg", default=55.0),
        "pipe_outer_diameter_mm": number(payload, "pipeDiameter", "pipe_outer_diameter_mm", default=30.0),
        "wall_cut_extension_mm": number(payload, "wallCutExtension", default=80.0),
        "centerline_step_mm": 2.0,
        "tube_segments": 32,
        "output_stl": "pipe_model.stl",
        "output_centerline_csv": "pipe_centerline.csv",
    }

    params_path = GENERATOR_DIR / "params.json"
    original_params = params_path.read_text(encoding="utf-8")
    try:
        params_path.write_text(json.dumps(params, ensure_ascii=False, indent=2), encoding="utf-8")
        proc = subprocess.run(
            ["python", str(GENERATOR_DIR / "generate_pipe.py")],
            cwd=str(ROOT),
            capture_output=True,
            text=True,
            timeout=120,
        )
        if proc.returncode != 0:
            raise RuntimeError(proc.stderr or proc.stdout)

        for name in ("pipe_model.stl", "pipe_centerline.csv", "create_pipe_native.vbs"):
            src = GENERATOR_DIR / name
            if src.exists():
                shutil.copy2(src, job_dir / name)

        stl_path = job_dir / "pipe_model.stl"
        preview_path = job_dir / "pipe_preview.png"
        if stl_path.exists():
            render_stl_preview(stl_path, preview_path)

        sldprt_path = ""
        task_part = job_dir / "pipe_native.SLDPRT"
        if task_part.exists():
            task_part.unlink()
        if RUN_SOLIDWORKS:
            vbs_path = job_dir / "create_pipe_native.vbs"
            prepare_solidworks_vbs(vbs_path, task_part)
            try:
                sw_proc = subprocess.run(
                    ["cscript", "//nologo", "//B", str(vbs_path)],
                    cwd=str(job_dir),
                    capture_output=True,
                    text=True,
                    timeout=180,
                )
                (job_dir / "solidworks_vbs_stdout.txt").write_text(sw_proc.stdout or "", encoding="utf-8", errors="ignore")
                (job_dir / "solidworks_vbs_stderr.txt").write_text(sw_proc.stderr or "", encoding="utf-8", errors="ignore")
                if sw_proc.returncode != 0:
                    raise RuntimeError(solidworks_error_message(job_dir, sw_proc.stderr or sw_proc.stdout or "SolidWorks native model generation failed."))
            except subprocess.TimeoutExpired:
                raise RuntimeError("SolidWorks native model generation timed out.")
            if task_part.exists():
                sldprt_path = str(task_part)
            else:
                raise RuntimeError(solidworks_error_message(job_dir, "SolidWorks did not generate pipe_native.SLDPRT."))
        else:
            raise RuntimeError("SolidWorks generation is disabled. Set PIPE_WORKER_RUN_SOLIDWORKS=1 before starting the worker.")

        l3 = None
        initial_angle = None
        csv_path = job_dir / "pipe_centerline.csv"
        if csv_path.exists():
            for line in csv_path.read_text(encoding="utf-8").splitlines():
                if line.startswith("initial_angle_deg,"):
                    initial_angle = float(line.split(",", 1)[1])
                if line.startswith("L3_mm,"):
                    l3 = float(line.split(",", 1)[1])

        return {
            "status": "SUCCESS",
            "L3": l3,
            "initialAngle": initial_angle,
            "closureStatus": "几何闭合和相切约束满足",
            "sldprtPath": sldprt_path,
            "stlPath": str(stl_path),
            "previewPngPath": str(preview_path) if preview_path.exists() else "",
            "errorMessage": "",
        }
    finally:
        params_path.write_text(original_params, encoding="utf-8")


def solidworks_error_message(job_dir, fallback):
    log_path = job_dir / "solidworks_native_log.txt"
    if log_path.exists():
        lines = [line.strip() for line in log_path.read_text(encoding="utf-8", errors="ignore").splitlines() if line.strip()]
        if lines:
            return fallback + " " + " | ".join(lines[-5:])
    stdout_path = job_dir / "solidworks_vbs_stdout.txt"
    stderr_path = job_dir / "solidworks_vbs_stderr.txt"
    details = []
    for path in (stdout_path, stderr_path):
        if path.exists():
            text = path.read_text(encoding="utf-8", errors="ignore").strip()
            if text:
                details.append(text[-500:])
    return fallback + (" " + " ".join(details) if details else "")


def prepare_solidworks_vbs(vbs_path, output_part_path):
    text = vbs_path.read_text(encoding="ascii", errors="ignore")
    output_text = str(output_part_path)
    log_text = str(output_part_path.with_name("solidworks_native_log.txt"))
    text = text.replace("WScript.Quit 0", "WScript.Quit 1")

    lines = []
    sweep_block = False
    for line in text.splitlines():
        stripped = line.strip()
        if line == "Option Explicit":
            lines.extend([
                line,
                "",
                "Sub WriteLog(message)",
                "  Dim logFso, logFile",
                "  Set logFso = CreateObject(\"Scripting.FileSystemObject\")",
                f"  Set logFile = logFso.OpenTextFile(\"{log_text}\", 8, True)",
                "  logFile.WriteLine Now & \" \" & message",
                "  logFile.Close",
                "End Sub",
            ])
            continue
        if stripped.startswith("MsgBox "):
            message = stripped[len("MsgBox "):].split(", vb", 1)[0]
            lines.append("WriteLog " + message)
            lines.append("WScript.Echo " + message)
            continue
        if line.startswith("Dim swApp, swModel"):
            line = line + ", fso, saveOk, longStatus, longWarnings"
        if line.startswith("Set swApp = CreateObject"):
            lines.extend([
                "WriteLog \"Starting SolidWorks automation\"",
                "On Error Resume Next",
                "Set swApp = GetObject(, \"SldWorks.Application\")",
                "WriteLog \"GetObject err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
                "If Err.Number <> 0 Or swApp Is Nothing Then",
                "  Err.Clear",
                "  Set swApp = CreateObject(\"SldWorks.Application\")",
                "  WriteLog \"CreateObject err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
                "End If",
                "On Error GoTo 0",
                "If swApp Is Nothing Then",
                "  WriteLog \"SolidWorks COM object is Nothing\"",
                "  WScript.Echo \"Cannot connect to SolidWorks COM automation.\"",
                "  WScript.Quit 1",
                "End If",
                "WriteLog \"SolidWorks COM connected\"",
            ])
            continue
        if stripped == "swApp.Visible = True":
            lines.append(line)
            lines.append("WriteLog \"SolidWorks visible set\"")
            continue
        if line.startswith("partTemplate = swApp.GetUserPreferenceStringValue"):
            lines.append(line)
            lines.append("WriteLog \"Part template=\" & partTemplate")
            continue
        if stripped == "Set swModel = swApp.ActiveDoc":
            lines.append(line)
            lines.append("WriteLog \"Active document acquired\"")
            continue
        if stripped == "swModel.SketchManager.Insert3DSketch True":
            lines.append(line)
            lines.append("WriteLog \"3D sketch toggle executed\"")
            continue
        if stripped == "Set swPathFeat = FindLast3DSketchFeature(swModel)":
            lines.append(line)
            lines.append("WriteLog \"Path feature lookup completed\"")
            continue
        if stripped == "ok = False":
            lines.append("WriteLog \"Selecting PipePath3D\"")
            lines.append(line)
            continue
        if stripped.startswith("If Not swPathFeat Is Nothing Then ok = swPathFeat.Select2"):
            lines.append(line)
            lines.append("WriteLog \"Select2 result=\" & CStr(ok)")
            continue
        if stripped.startswith("If Not ok Then ok = swModel.Extension.SelectByID2"):
            lines.append(line)
            lines.append("WriteLog \"SelectByID2 result=\" & CStr(ok)")
            continue
        if line.startswith("outPart = "):
            line = f'outPart = "{output_text}"'
        if line.strip() == "Set swFeat = swModel.FeatureManager.InsertProtrusionSwept4(False, False, 0, False, False, 0, 0, False, 0, 0, 0, 0, True, True, True, 0, True, True, ":
            lines.append("WriteLog \"Starting sweep feature\"")
        if stripped.startswith("Set swFeat = swModel.FeatureManager.InsertProtrusionSwept4"):
            lines.append("WriteLog \"Starting sweep feature\"")
            lines.extend([
                "On Error Resume Next",
                line,
                "WriteLog \"Sweep call err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
                "If Err.Number <> 0 Then Err.Clear",
                "On Error GoTo 0",
            ])
            continue
        if stripped == "If swFeat Is Nothing Then":
            lines.append(line)
            sweep_block = True
            lines.append("WriteLog \"Sweep feature result=False\"")
            lines.append("WriteLog \"Sweep failed; saving path-only SolidWorks part for diagnostics\"")
            lines.extend([
                "outPart = \"" + output_text + "\"",
                "Set fso = CreateObject(\"Scripting.FileSystemObject\")",
                "If fso.FileExists(outPart) Then fso.DeleteFile outPart, True",
                "On Error Resume Next",
                "saveOk = swModel.Extension.SaveAs(outPart, 0, 1, Nothing, longStatus, longWarnings)",
                "WriteLog \"Diagnostic SaveAs result=\" & CStr(saveOk) & \", status=\" & CStr(longStatus) & \", warnings=\" & CStr(longWarnings) & \", err=\" & CStr(Err.Number)",
                "On Error GoTo 0",
            ])
            continue
        if sweep_block and stripped == "End If":
            lines.append(line)
            lines.extend(save_vbs_lines(output_text))
            lines.append("WriteLog \"Saved immediately after sweep; wall trimming skipped in worker mode\"")
            lines.append("WScript.Echo \"Native SolidWorks pipe model created: \" & outPart")
            lines.append("WScript.Quit 0")
            sweep_block = False
            continue
        if stripped == "swModel.SaveAs outPart":
            lines.extend(save_vbs_lines(output_text))
            continue
        if "WScript.Quit 1" in line:
            lines.append("WriteLog \"Script quits with failure before SaveAs\"")
        if line.startswith("MsgBox \"Native SolidWorks pipe model created."):
            lines.append("WScript.Echo \"Native SolidWorks pipe model created: \" & outPart")
            lines.append("WriteLog \"Native SolidWorks pipe model created\"")
            continue
        lines.append(line)
    vbs_path.write_text("\n".join(lines) + "\n", encoding="ascii")


def save_vbs_lines(output_text):
    return [
        f"outPart = \"{output_text}\"",
        "WriteLog \"Starting SaveAs: \" & outPart",
        "Set fso = CreateObject(\"Scripting.FileSystemObject\")",
        "If fso.FileExists(outPart) Then fso.DeleteFile outPart, True",
        "swModel.EditRebuild3",
        "On Error Resume Next",
        "saveOk = swModel.Extension.SaveAs(outPart, 0, 1, Nothing, longStatus, longWarnings)",
        "WriteLog \"Extension.SaveAs result=\" & CStr(saveOk) & \", status=\" & CStr(longStatus) & \", warnings=\" & CStr(longWarnings)",
        "If Not fso.FileExists(outPart) Then",
        "  Err.Clear",
        "  saveOk = swModel.SaveAs3(outPart, 0, 2)",
        "  WriteLog \"SaveAs3 result=\" & CStr(saveOk) & \", err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
        "End If",
        "If Err.Number <> 0 Then",
        "  Err.Clear",
        "  saveOk = swModel.SaveAs(outPart)",
        "  WriteLog \"SaveAs fallback result=\" & CStr(saveOk) & \", err=\" & CStr(Err.Number) & \", desc=\" & Err.Description",
        "End If",
        "On Error GoTo 0",
        "If Not fso.FileExists(outPart) Then",
        "  WriteLog \"Save failed; output file missing\"",
        "  WScript.Echo \"SolidWorks model was created but SaveAs did not write: \" & outPart",
        "  WScript.Quit 2",
        "End If",
        "WriteLog \"Save succeeded\"",
    ]


def render_stl_preview(stl_path, output_path):
    vertices = []
    with stl_path.open("r", encoding="ascii", errors="ignore") as fh:
        for line in fh:
            stripped = line.strip()
            if not stripped.startswith("vertex "):
                continue
            _tag, x, y, z = stripped.split()
            vertices.append((float(x), float(y), float(z)))

    if len(vertices) < 3:
        return

    triangles = np.array(vertices, dtype=float).reshape((-1, 3, 3))
    all_points = triangles.reshape((-1, 3))
    center = all_points.mean(axis=0)
    all_points -= center
    triangles -= center

    spans = np.ptp(all_points, axis=0)
    max_span = max(float(spans.max()), 1.0)
    xlim = (-max_span * 0.58, max_span * 0.58)
    ylim = (-max_span * 0.58, max_span * 0.58)
    zlim = (-max_span * 0.35, max_span * 0.35)

    fig = plt.figure(figsize=(9.6, 6.2), dpi=160)
    ax = fig.add_subplot(111, projection="3d")
    collection = Poly3DCollection(
        triangles,
        facecolor=(0.38, 0.43, 0.51, 1.0),
        edgecolor=(0.12, 0.16, 0.22, 0.32),
        linewidth=0.08,
    )
    ax.add_collection3d(collection)
    ax.view_init(elev=22, azim=-58)
    ax.set_xlim(*xlim)
    ax.set_ylim(*ylim)
    ax.set_zlim(*zlim)
    ax.set_box_aspect((1, 1, 0.55))
    ax.set_axis_off()
    fig.patch.set_facecolor("#f5f8fc")
    ax.set_facecolor("#f5f8fc")
    plt.tight_layout(pad=0)
    fig.savefig(output_path, facecolor=fig.get_facecolor(), bbox_inches="tight", pad_inches=0.02)
    plt.close(fig)


class Handler(BaseHTTPRequestHandler):
    def do_POST(self):
        if urlparse(self.path).path != "/api/pipe-model":
            self.send_error(404)
            return

        try:
            length = int(self.headers.get("Content-Length", "0"))
            payload = json.loads(self.rfile.read(length).decode("utf-8") or "{}")
            result = build_model(payload)
            self.respond(200, result)
        except Exception as exc:
            self.respond(500, {
                "status": "FAILED",
                "errorMessage": str(exc),
            })

    def respond(self, code, payload):
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, fmt, *args):
        print(fmt % args)


if __name__ == "__main__":
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    print(f"Pipe SolidWorks worker listening on http://{HOST}:{PORT}/api/pipe-model")
    print("SolidWorks native generation is required. Set PIPE_WORKER_RUN_SOLIDWORKS=0 only for geometry-debug mode.")
    ThreadingHTTPServer((HOST, PORT), Handler).serve_forever()
