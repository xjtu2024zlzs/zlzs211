import json
import os
import re
import shutil
import subprocess
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlparse


ROOT = Path(__file__).resolve().parents[1]
OUTPUT_DIR = ROOT / "ansys_worker" / "output"
HOST = os.environ.get("ANSYS_WORKER_HOST", "127.0.0.1")
PORT = int(os.environ.get("ANSYS_WORKER_PORT", "18081"))
WORKBENCH_CMD = os.environ.get("ANSYS_WORKBENCH_CMD", "").strip()
WORKBENCH_TIMEOUT = int(os.environ.get("ANSYS_WORKBENCH_TIMEOUT", "600"))
MECHANICAL_START_TIMEOUT = int(os.environ.get("ANSYS_MECHANICAL_START_TIMEOUT", "60"))
MECHANICAL_RESULT_TIMEOUT = int(os.environ.get("ANSYS_MECHANICAL_RESULT_TIMEOUT", "540"))
MECHANICAL_OPEN_WAIT = int(os.environ.get("ANSYS_MECHANICAL_OPEN_WAIT", "20"))
MECHANICAL_HANDSHAKE_RETRIES = int(os.environ.get("ANSYS_MECHANICAL_HANDSHAKE_RETRIES", "6"))
MECHANICAL_HANDSHAKE_WAIT = int(os.environ.get("ANSYS_MECHANICAL_HANDSHAKE_WAIT", "10"))
MECHANICAL_INTERACTIVE = os.environ.get("ANSYS_MECHANICAL_INTERACTIVE", "1") == "1"
KEEP_MECHANICAL_OPEN = os.environ.get("ANSYS_KEEP_MECHANICAL_OPEN", "1") == "1"
MECHANICAL_MESH_SIZE_MM = float(os.environ.get("ANSYS_MESH_SIZE_MM", "3"))
IMAGE_EXPORT_WIDTH = int(os.environ.get("ANSYS_IMAGE_EXPORT_WIDTH", "1920"))
IMAGE_EXPORT_HEIGHT = int(os.environ.get("ANSYS_IMAGE_EXPORT_HEIGHT", "1080"))
DEFAULT_PRESSURE_PA = float(os.environ.get("ANSYS_PRESSURE_PA", "30000000"))
DEFAULT_INITIAL_PRESSURE_PA = float(os.environ.get("ANSYS_INITIAL_PRESSURE_PA", "101325"))
DEFAULT_RISE_TIME_S = float(os.environ.get("ANSYS_RISE_TIME_S", "0.001"))
MODE_DEMO = "DEMO_SIMULATION_MODEL"
MODE_FSI = "BIDIRECTIONAL_FSI_MODEL"


def nested(data, *keys, default=None):
    value = data
    for key in keys:
        if not isinstance(value, dict) or key not in value:
            return default
        value = value[key]
    return value


def to_float(value, default):
    if value is None:
        return default
    try:
        text = str(value).strip()
        if not text:
            return default
        return float(text)
    except Exception:
        return default


def fault_value(payload, key, default):
    values = nested(payload, "faultPipeParameters", "values", default={})
    if isinstance(values, dict) and key in values:
        return values.get(key)
    items = nested(payload, "faultPipeParameters", "items", default=[])
    if isinstance(items, list):
        for item in items:
            if isinstance(item, dict) and item.get("paramCode") == key:
                return item.get("paramValue")
    return default


def pressure_config(payload):
    initial_pa = to_float(fault_value(payload, "INLET_PRESSURE_INITIAL", DEFAULT_INITIAL_PRESSURE_PA), DEFAULT_INITIAL_PRESSURE_PA)
    peak_pa = to_float(fault_value(payload, "INLET_PRESSURE_PEAK", DEFAULT_PRESSURE_PA), DEFAULT_PRESSURE_PA)
    rise_time_s = to_float(fault_value(payload, "INLET_PRESSURE_RISE_TIME", DEFAULT_RISE_TIME_S), DEFAULT_RISE_TIME_S)
    expression = str(fault_value(payload, "INLET_PRESSURE_EXPRESSION", payload.get("pressureLoad") or "") or "").strip()
    if not expression:
        expression = f"IF(t <= {rise_time_s}, {initial_pa} + ({peak_pa} - {initial_pa}) * t / {rise_time_s}, {peak_pa})"
    return {
        "initialPressurePa": initial_pa,
        "peakPressurePa": peak_pa,
        "riseTimeS": rise_time_s,
        "expression": expression,
    }


def material_config(payload):
    material_name = str(fault_value(payload, "MATERIAL_NAME", "Structural Steel") or "Structural Steel").strip()
    return {
        "materialName": material_name,
        "youngModulusPa": to_float(fault_value(payload, "YOUNG_MODULUS", 1.93e11), 1.93e11),
        "poissonRatio": to_float(fault_value(payload, "POISSON_RATIO", 0.31), 0.31),
        "tensileYieldStrengthPa": to_float(fault_value(payload, "TENSILE_YIELD_STRENGTH", 2.07e8), 2.07e8),
        "tensileUltimateStrengthPa": to_float(fault_value(payload, "TENSILE_ULTIMATE_STRENGTH", 5.86e8), 5.86e8),
    }


def first_value(*values):
    for value in values:
        if value not in (None, ""):
            return value
    return None


def parse_centerline_points(path):
    points = []
    if not path:
        return points
    centerline = Path(str(path))
    if not centerline.exists() or not centerline.is_file():
        return points
    for line in centerline.read_text(encoding="utf-8", errors="ignore").splitlines():
        parts = [part.strip() for part in line.split(",")]
        if len(parts) < 3 or parts[0] in ("", "x_mm"):
            continue
        try:
            points.append([float(parts[0]) / 1000.0, float(parts[1]) / 1000.0, float(parts[2]) / 1000.0])
        except Exception:
            continue
    return points


def pipe_geometry_config(payload):
    fault_values = nested(payload, "faultPipeParameters", "values", default={})
    cad_params = nested(payload, "cadModel", "params", default={})
    geometry = nested(payload, "geometry", default={})
    if not isinstance(fault_values, dict):
        fault_values = {}
    if not isinstance(cad_params, dict):
        cad_params = {}
    if not isinstance(geometry, dict):
        geometry = {}

    outer_mm = to_float(first_value(
        cad_params.get("pipeDiameter"),
        cad_params.get("pipe_outer_diameter_mm"),
        fault_values.get("PIPE_OUTER_DIAMETER"),
    ), 9.53)
    inner_mm = to_float(first_value(
        cad_params.get("pipeInnerDiameter"),
        cad_params.get("pipe_inner_diameter_mm"),
        fault_values.get("PIPE_INNER_DIAMETER"),
    ), outer_mm - 1.8)
    wall_mm = max((outer_mm - inner_mm) / 2.0, 0.0)
    centerline_path = first_value(
        geometry.get("centerlineCsvPath"),
        nested(payload, "cadModel", "files", "centerlineCsv", "filePath", default=""),
    )
    return {
        "outerDiameterMm": outer_mm,
        "innerDiameterMm": inner_mm,
        "wallThicknessMm": wall_mm,
        "centerlineCsvPath": str(centerline_path or ""),
        "centerlinePoints": parse_centerline_points(centerline_path),
    }


def safe_task_id(value):
    return re.sub(r"[^A-Za-z0-9_.-]", "_", str(value or "manual"))


def normalize_simulation_mode(value):
    text = str(value or "").strip()
    upper = text.upper()
    if upper in ("BIDIRECTIONAL_FSI_MODEL", "BIDIRECTIONAL_FSI", "FSI_REFERENCE") or text == "双向流固耦合仿真模型":
        return MODE_FSI
    return MODE_DEMO


def simulation_mode_slug(mode):
    return "bidirectional_fsi_model" if mode == MODE_FSI else "demo_simulation_model"


def is_fsi_mode(mode):
    return normalize_simulation_mode(mode) == MODE_FSI


def decode_process_output(value):
    if not value:
        return ""
    if isinstance(value, str):
        return value
    for encoding in ("utf-8", "gbk", "mbcs"):
        try:
            return value.decode(encoding)
        except Exception:
            pass
    return value.decode("utf-8", errors="ignore")


def resolve_workbench_path(raw_cmd):
    configured = Path(str(raw_cmd).strip().strip('"'))
    if not configured:
        raise RuntimeError(
            "ANSYS_WORKBENCH_CMD is not configured. Set it to runwb2.bat or RunWB2.exe before starting the ANSYS worker."
        )
    if configured.suffix.lower() == ".lnk":
        raise RuntimeError(
            "ANSYS_WORKBENCH_CMD cannot be a Windows shortcut. Use the real runwb2.bat or RunWB2.exe path."
        )
    if configured.name.lower() == "runwb2.exe":
        official_bat = configured.with_name("runwb2.bat")
        if official_bat.exists():
            configured = official_bat
    return configured


def workbench_command_line(raw_cmd, journal_path):
    configured = resolve_workbench_path(raw_cmd)
    if configured.suffix.lower() in (".bat", ".cmd"):
        return f'"{configured}" -B -R "{journal_path}"', True
    return [str(configured), "-B", "-R", str(journal_path)], False


def worker_health():
    payload = {
        "status": "UP",
        "host": HOST,
        "port": PORT,
        "workbenchConfigured": bool(WORKBENCH_CMD),
        "workbenchCommandRaw": WORKBENCH_CMD,
        "outputDir": str(OUTPUT_DIR),
    }
    if WORKBENCH_CMD:
        try:
            resolved = resolve_workbench_path(WORKBENCH_CMD)
            payload.update({
                "workbenchCommandResolved": str(resolved),
                "workbenchCommandExists": resolved.exists(),
                "workbenchCommandType": resolved.suffix.lower().lstrip("."),
            })
        except Exception as exc:
            payload.update({
                "status": "MISCONFIGURED",
                "workbenchCommandExists": False,
                "errorMessage": str(exc),
            })
    return payload


def import_geometry(payload):
    task_id = safe_task_id(payload.get("taskId") or "manual")
    geometry_path = payload.get("geometryPath") or nested(payload, "geometry", "geometryPath")
    geometry_type = payload.get("geometryType") or nested(payload, "geometry", "geometryType", default="")
    simulation_mode = normalize_simulation_mode(payload.get("simulationMode") or "static_structural")
    if not geometry_path:
        raise RuntimeError("Missing geometryPath. Generate CAD and pass STEP or Parasolid geometry first.")

    geometry = Path(str(geometry_path))
    if not geometry.exists() or not geometry.is_file():
        raise RuntimeError(f"Geometry file does not exist: {geometry}")

    job_dir = OUTPUT_DIR / f"task_{task_id}" / simulation_mode_slug(simulation_mode)
    job_dir.mkdir(parents=True, exist_ok=True)
    project_path = job_dir / "pipe_import.wbpj"
    journal_path = job_dir / "import_geometry.wbjn"
    mechanical_script_path = job_dir / "mechanical_setup.py"
    mechanical_result_path = job_dir / "mechanical_result.json"
    mechanical_started_path = job_dir / "mechanical_started.txt"
    mechanical_ready_path = job_dir / "mechanical_command_ready.txt"
    mechanical_trace_path = job_dir / "mechanical_trace.txt"
    mechanical_command_error_path = job_dir / "mechanical_command_error.txt"
    stress_image_path = job_dir / "equivalent_stress.png"
    stdout_path = job_dir / "ansys_stdout.txt"
    stderr_path = job_dir / "ansys_stderr.txt"
    pressure = pressure_config(payload)
    pipe_geometry = pipe_geometry_config(payload)
    material = material_config(payload)

    reset_job_outputs(job_dir, [
        mechanical_result_path,
        mechanical_started_path,
        mechanical_ready_path,
        mechanical_trace_path,
        mechanical_command_error_path,
        stress_image_path,
        stdout_path,
        stderr_path,
        job_dir / "workbench_command.txt",
        job_dir / "workbench_steps.txt",
    ])

    write_progress(job_dir, "COPY_GEOMETRY", "复制 CAD 几何文件到 ANSYS 任务目录")
    workbench_geometry = copy_geometry_to_job(geometry, job_dir, geometry_type)

    if is_fsi_mode(simulation_mode):
        return run_bidirectional_fsi_reference(
            payload,
            job_dir,
            geometry,
            geometry_type,
            workbench_geometry,
            pressure,
            pipe_geometry,
            material,
            simulation_mode,
        )

    write_progress(job_dir, "GENERATE_SCRIPTS", "生成 Workbench Journal 和 Mechanical 脚本")
    mechanical_script_path.write_text(
        mechanical_script(
            mechanical_result_path,
            mechanical_started_path,
            mechanical_trace_path,
            stress_image_path,
            MECHANICAL_MESH_SIZE_MM,
            IMAGE_EXPORT_WIDTH,
            IMAGE_EXPORT_HEIGHT,
            pressure,
            pipe_geometry,
            material,
            simulation_mode,
        ),
        encoding="utf-8",
    )
    journal_path.write_text(
        workbench_journal(
            workbench_geometry,
            project_path,
            mechanical_script_path,
            mechanical_result_path,
            mechanical_ready_path,
            mechanical_command_error_path,
            simulation_mode,
        ),
        encoding="utf-8",
    )

    if not WORKBENCH_CMD:
        raise RuntimeError(
            "ANSYS_WORKBENCH_CMD is not configured. Set it to runwb2.bat or RunWB2.exe before starting the ANSYS worker."
        )

    write_progress(job_dir, "RUN_WORKBENCH", "启动 Workbench 批处理并调用 Mechanical")
    cmd, use_shell = workbench_command_line(WORKBENCH_CMD, journal_path)
    command_text = cmd if isinstance(cmd, str) else " ".join(cmd)
    (job_dir / "workbench_command.txt").write_text(command_text, encoding="utf-8", errors="ignore")
    proc = subprocess.run(
        cmd,
        cwd=str(job_dir),
        capture_output=True,
        text=False,
        shell=use_shell,
        timeout=WORKBENCH_TIMEOUT,
    )
    stdout_path.write_text(decode_process_output(proc.stdout), encoding="utf-8", errors="ignore")
    stderr_path.write_text(decode_process_output(proc.stderr), encoding="utf-8", errors="ignore")

    if proc.returncode != 0:
        raise RuntimeError(ansys_error_message(job_dir, f"ANSYS geometry import failed with exit code {proc.returncode}."))
    if not project_path.exists():
        raise RuntimeError(ansys_error_message(job_dir, "ANSYS did not create the Workbench project file."))

    mechanical_result = read_json(mechanical_result_path)
    if stress_image_path.exists():
        try:
            annotate_stress_image(stress_image_path, mechanical_result)
            append_trace(job_dir, "IMAGE_CHINESE_LABELS_ANNOTATED")
        except Exception as exc:
            append_trace(job_dir, "IMAGE_CHINESE_LABELS_FAILED=" + str(exc))
    mechanical_solved = str(mechanical_result.get("status", "")).upper() == "SUCCESS"
    selected_template = read_text(job_dir / "selected_template.txt")
    if simulation_mode != "geometry_import" and not mechanical_result_path.exists():
        raise RuntimeError(ansys_error_message(job_dir, "Mechanical did not produce mechanical_result.json."))
    if simulation_mode != "geometry_import" and not mechanical_solved:
        raise RuntimeError(ansys_error_message(
            job_dir,
            "Mechanical solve failed: " + str(mechanical_result.get("errorMessage", "unknown error"))
        ))
    write_progress(job_dir, "SUCCESS", "ANSYS Workbench / Mechanical 执行完成")
    metrics = [
        {"name": "Workbench 项目", "value": "已生成", "unit": "", "source": "ANSYS Workbench"},
        {"name": "Workbench 模板", "value": selected_template or "未知", "unit": "", "source": "ANSYS Workbench"},
        {"name": "导入几何格式", "value": geometry_type or geometry.suffix.lstrip(".").upper(), "unit": "", "source": "CAD Worker"},
        {"name": "几何文件大小", "value": round(workbench_geometry.stat().st_size / 1024.0, 2), "unit": "KB", "source": "ANSYS Worker"},
        {"name": "Mechanical 求解", "value": "成功" if mechanical_solved else "待确认", "unit": "", "source": "Mechanical"},
        {"name": "入口初始压强", "value": round(pressure["initialPressurePa"] / 1000000.0, 6), "unit": "MPa", "source": "故障管段参数"},
        {"name": "入口峰值压强", "value": round(pressure["peakPressurePa"] / 1000000.0, 6), "unit": "MPa", "source": "故障管段参数"},
        {"name": "压强上升时间", "value": pressure["riseTimeS"], "unit": "s", "source": "故障管段参数"},
        {"name": "入口压强表达式", "value": pressure["expression"], "unit": "Pa", "source": "故障管段参数"},
        {"name": "管道外径", "value": pipe_geometry["outerDiameterMm"], "unit": "mm", "source": "CAD/故障管段参数"},
        {"name": "管道内径", "value": pipe_geometry["innerDiameterMm"], "unit": "mm", "source": "CAD/故障管段参数"},
        {"name": "管道壁厚", "value": round(pipe_geometry["wallThicknessMm"], 6), "unit": "mm", "source": "CAD/故障管段参数"},
        {"name": "网格尺寸", "value": MECHANICAL_MESH_SIZE_MM, "unit": "mm", "source": "ANSYS Worker"},
        {"name": "材料", "value": material["materialName"], "unit": "", "source": "故障管段参数"},
        {"name": "材料屈服强度", "value": material["tensileYieldStrengthPa"], "unit": "Pa", "source": "故障管段参数"},
    ]
    for item in mechanical_result.get("metrics", []):
        if isinstance(item, dict):
            metrics.append(item)
    return {
        "status": "SUCCESS",
        "summary": "CAD 几何文件已导入 ANSYS Workbench，并已尝试执行 Mechanical 结构求解。",
        "sourceGeometryPath": str(geometry),
        "geometryPath": str(workbench_geometry),
        "geometryType": geometry_type or geometry.suffix.lstrip(".").upper(),
        "simulationMode": simulation_mode,
        "simulationModelName": "演示仿真模型",
        "workDir": str(job_dir),
        "projectPath": str(project_path),
        "resultFilePath": str(project_path),
        "mechanicalScriptPath": str(mechanical_script_path),
        "mechanicalResultPath": str(mechanical_result_path),
        "stressImageUrl": str(stress_image_path) if stress_image_path.exists() else "",
        "engineeringStatus": mechanical_result.get("engineeringStatus", ""),
        "engineeringWarnings": mechanical_result.get("engineeringWarnings", []),
        "engineeringEstimates": mechanical_result.get("engineeringEstimates", {}),
        "maxEquivalentStressValue": mechanical_result.get("maxEquivalentStressValue"),
        "maxTotalDeformationValue": mechanical_result.get("maxTotalDeformationValue"),
        "mechanicalResult": mechanical_result,
        "metrics": metrics,
        "placeholder": False,
    }


def copy_geometry_to_job(geometry, job_dir, geometry_type):
    suffix = geometry.suffix or geometry_suffix(geometry_type)
    target = job_dir / f"input_geometry{suffix.lower()}"
    if geometry.resolve() != target.resolve():
        shutil.copy2(geometry, target)
    return target


def geometry_suffix(geometry_type):
    normalized = (geometry_type or "").strip().upper()
    if normalized == "STEP":
        return ".step"
    if normalized == "SLDPRT":
        return ".SLDPRT"
    if normalized == "PARASOLID":
        return ".x_t"
    if normalized == "STL":
        return ".stl"
    return ".cad"


def reset_job_outputs(job_dir, paths):
    for path in paths:
        try:
            if path.exists() and path.is_file():
                path.unlink()
        except Exception:
            pass


def read_json(path):
    if not path.exists():
        return {}
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception:
        return {}


def read_text(path):
    if not path.exists():
        return ""
    try:
        return path.read_text(encoding="utf-8", errors="ignore").strip()
    except Exception:
        return ""


def write_progress(job_dir, stage, message):
    payload = {
        "stage": stage,
        "message": message,
    }
    (job_dir / "progress.json").write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")


def append_trace(job_dir, message):
    try:
        with (job_dir / "mechanical_trace.txt").open("a", encoding="utf-8") as fh:
            fh.write(str(message) + "\n")
    except Exception:
        pass


def annotate_stress_image(image_path, mechanical_result):
    try:
        from PIL import Image, ImageDraw, ImageFont
        import numpy as np
    except Exception:
        return

    image = Image.open(image_path).convert("RGB")
    draw = ImageDraw.Draw(image)
    width, height = image.size
    font_path = chinese_font_path()
    label_font = ImageFont.truetype(font_path, max(16, int(height * 0.028))) if font_path else ImageFont.load_default()
    panel_font = ImageFont.truetype(font_path, max(18, int(height * 0.03))) if font_path else ImageFont.load_default()

    max_text = "最大应力点"
    min_text = "最小应力点"
    max_value = mechanical_result.get("maxEquivalentStressValue")
    min_value = mechanical_result.get("minEquivalentStressValue")
    if max_value is not None:
        max_text += " " + format_image_value(max_value, "Pa")
    if min_value is not None:
        min_text += " " + format_image_value(min_value, "Pa")

    replaced = 0
    replaced += replace_color_callout(image, draw, "red", max_text, label_font)
    replaced += replace_color_callout(image, draw, "blue", min_text, label_font)
    if replaced < 2:
        draw_fallback_label_panel(draw, image.size, panel_font, max_text, min_text)
    image.save(image_path)


def chinese_font_path():
    for path in (
        r"C:\Windows\Fonts\msyh.ttc",
        r"C:\Windows\Fonts\simhei.ttf",
        r"C:\Windows\Fonts\simsun.ttc",
    ):
        if Path(path).exists():
            return path
    return ""


def replace_color_callout(image, draw, color, text, font):
    import numpy as np

    arr = np.asarray(image)
    if color == "red":
        mask = (arr[:, :, 0] > 170) & (arr[:, :, 1] < 90) & (arr[:, :, 2] < 90)
        fill = (222, 34, 34)
        border = (120, 0, 0)
        text_fill = (255, 255, 255)
    else:
        mask = (arr[:, :, 2] > 130) & (arr[:, :, 0] < 90) & (arr[:, :, 1] < 120)
        fill = (24, 74, 210)
        border = (0, 28, 126)
        text_fill = (255, 255, 255)

    bbox = find_callout_bbox(mask)
    if not bbox:
        return 0

    x0, y0, x1, y1 = bbox
    padding_x = max(8, int((y1 - y0) * 0.45))
    padding_y = max(4, int((y1 - y0) * 0.18))
    text_bbox = draw.textbbox((0, 0), text, font=font)
    target_w = (text_bbox[2] - text_bbox[0]) + padding_x * 2
    target_h = max(y1 - y0 + padding_y * 2, (text_bbox[3] - text_bbox[1]) + padding_y * 2)
    x1 = min(image.size[0] - 2, max(x1 + padding_x, x0 + target_w))
    y1 = min(image.size[1] - 2, max(y1 + padding_y, y0 + target_h))

    draw.rounded_rectangle((x0, y0, x1, y1), radius=max(3, int(target_h * 0.16)), fill=fill, outline=border, width=2)
    tx = x0 + max(4, (x1 - x0 - (text_bbox[2] - text_bbox[0])) // 2)
    ty = y0 + max(2, (y1 - y0 - (text_bbox[3] - text_bbox[1])) // 2) - 1
    draw.text((tx, ty), text, font=font, fill=text_fill)
    return 1


def find_callout_bbox(mask):
    import numpy as np

    height, width = mask.shape
    visited = np.zeros(mask.shape, dtype=bool)
    coords = np.argwhere(mask)
    best = None
    best_area = 0
    for start_y, start_x in coords:
        if visited[start_y, start_x] or not mask[start_y, start_x]:
            continue
        stack = [(int(start_y), int(start_x))]
        visited[start_y, start_x] = True
        min_x = max_x = int(start_x)
        min_y = max_y = int(start_y)
        area = 0
        while stack:
            y, x = stack.pop()
            area += 1
            min_x = min(min_x, x)
            max_x = max(max_x, x)
            min_y = min(min_y, y)
            max_y = max(max_y, y)
            for ny, nx in ((y - 1, x), (y + 1, x), (y, x - 1), (y, x + 1)):
                if 0 <= ny < height and 0 <= nx < width and mask[ny, nx] and not visited[ny, nx]:
                    visited[ny, nx] = True
                    stack.append((ny, nx))
        box_w = max_x - min_x + 1
        box_h = max_y - min_y + 1
        if (
            area > 80
            and box_w >= 18
            and box_h >= 8
            and box_w / max(box_h, 1) > 1.15
            and min_x > width * 0.12
            and min_y < height * 0.78
        ):
            score = area + box_w * box_h
            if score > best_area:
                best_area = score
                best = (max(0, min_x - 2), max(0, min_y - 2), min(width - 1, max_x + 2), min(height - 1, max_y + 2))
    return best


def draw_fallback_label_panel(draw, size, font, max_text, min_text):
    width, height = size
    lines = [max_text, min_text]
    boxes = [draw.textbbox((0, 0), line, font=font) for line in lines]
    text_w = max(box[2] - box[0] for box in boxes)
    text_h = max(box[3] - box[1] for box in boxes)
    pad = max(12, int(height * 0.018))
    panel_w = min(width - 20, text_w + pad * 2)
    panel_h = text_h * len(lines) + pad * 3
    x0 = max(10, width - panel_w - 24)
    y0 = max(10, height - panel_h - 24)
    draw.rounded_rectangle((x0, y0, x0 + panel_w, y0 + panel_h), radius=8, fill=(255, 255, 255), outline=(45, 76, 122), width=2)
    y = y0 + pad
    for line, fill in ((max_text, (205, 38, 38)), (min_text, (32, 85, 210))):
        draw.text((x0 + pad, y), line, font=font, fill=fill)
        y += text_h + pad


def format_image_value(value, unit):
    try:
        number = float(value)
        return ("%.3e" % number) + " " + unit
    except Exception:
        return str(value) + " " + unit


def fsi_reference_config(pressure, pipe_geometry, material):
    initial_pa = pressure["initialPressurePa"]
    peak_pa = pressure["peakPressurePa"]
    return {
        "modelName": "双向流固耦合仿真模型",
        "reference": "1.wbpz",
        "workflow": "Fluent + Transient Structural + System Coupling",
        "couplingType": "two_way_fsi",
        "fluid": {
            "materialName": "hydraulic-oil",
            "densityKgM3": 1080.0,
            "dynamicViscosityPaS": 0.0105,
            "bulkModulusPa": 1.42e9,
        },
        "fluent": {
            "precision": "Double",
            "runParallel": True,
            "numberOfProcessors": 20,
            "viscousModel": "standard k-epsilon",
            "gravity": {"x": 0.0, "y": -9.8, "z": 0.0, "unit": "m/s^2"},
            "inlet": {
                "name": "inlet",
                "type": "pressure-inlet",
                "initialGaugePressurePa": initial_pa,
                "peakPressurePa": peak_pa,
                "expression": pressure["expression"],
            },
            "outlet": {
                "name": "outlet",
                "type": "pressure-outlet",
                "gaugePressurePa": initial_pa,
                "hydraulicDiameterM": pipe_geometry["innerDiameterMm"] / 1000.0,
            },
            "fsiWall": {"name": "wall", "type": "system-coupling"},
        },
        "structural": {
            "template": "Transient Structural",
            "material": material,
            "fsiRegion": "FSIN_1",
        },
        "systemCoupling": {
            "endTimeS": 0.002,
            "stepSizeS": 1.0e-5,
            "maximumIterations": 5,
            "minimumIterations": 1,
            "convergenceTarget": 0.01,
            "dataTransfers": [
                {"source": "Fluent.wall.force", "target": "MAPDL.FSIN_1.FORC"},
                {"source": "MAPDL.FSIN_1.INCD", "target": "Fluent.wall.displacement"},
            ],
        },
    }


def run_bidirectional_fsi_reference(payload, job_dir, source_geometry, geometry_type, workbench_geometry, pressure, pipe_geometry, material, simulation_mode):
    project_path = job_dir / "fsi_reference.wbpj"
    journal_path = job_dir / "fsi_reference_setup.wbjn"
    stdout_path = job_dir / "ansys_stdout.txt"
    stderr_path = job_dir / "ansys_stderr.txt"
    config_path = job_dir / "fsi_reference_config.json"
    fluent_journal_path = job_dir / "fluent_reference_setup.jou"
    steps_path = job_dir / "workbench_steps.txt"

    reset_job_outputs(job_dir, [
        project_path,
        journal_path,
        stdout_path,
        stderr_path,
        config_path,
        fluent_journal_path,
        steps_path,
        job_dir / "workbench_command.txt",
    ])

    config = fsi_reference_config(pressure, pipe_geometry, material)
    config_path.write_text(json.dumps(config, ensure_ascii=False, indent=2), encoding="utf-8")
    fluent_journal_path.write_text(fluent_reference_journal(config), encoding="utf-8")
    journal_path.write_text(fsi_workbench_journal(workbench_geometry, project_path, config_path, steps_path), encoding="utf-8")

    if not WORKBENCH_CMD:
        raise RuntimeError(
            "ANSYS_WORKBENCH_CMD is not configured. Set it to runwb2.bat or RunWB2.exe before starting the ANSYS worker."
        )

    write_progress(job_dir, "RUN_WORKBENCH", "Create reference two-way FSI Workbench project")
    cmd, use_shell = workbench_command_line(WORKBENCH_CMD, journal_path)
    command_text = cmd if isinstance(cmd, str) else " ".join(cmd)
    (job_dir / "workbench_command.txt").write_text(command_text, encoding="utf-8", errors="ignore")
    proc = subprocess.run(
        cmd,
        cwd=str(job_dir),
        capture_output=True,
        text=False,
        shell=use_shell,
        timeout=WORKBENCH_TIMEOUT,
    )
    stdout_path.write_text(decode_process_output(proc.stdout), encoding="utf-8", errors="ignore")
    stderr_path.write_text(decode_process_output(proc.stderr), encoding="utf-8", errors="ignore")

    if proc.returncode != 0:
        raise RuntimeError(ansys_error_message(job_dir, f"ANSYS FSI reference project setup failed with exit code {proc.returncode}."))
    if not project_path.exists():
        raise RuntimeError(ansys_error_message(job_dir, "ANSYS did not create the FSI reference Workbench project file."))

    write_progress(job_dir, "SUCCESS", "Reference two-way FSI Workbench project generated")
    metrics = [
        {"name": "仿真模型", "value": "双向流固耦合仿真模型", "unit": "", "source": "ANSYS Worker"},
        {"name": "Workbench 工程", "value": "已生成", "unit": "", "source": "ANSYS Workbench"},
        {"name": "流体材料", "value": "hydraulic-oil", "unit": "", "source": "参考模型参数"},
        {"name": "油液密度", "value": 1080.0, "unit": "kg/m3", "source": "参考模型参数"},
        {"name": "油液动力粘度", "value": 0.0105, "unit": "Pa*s", "source": "参考模型参数"},
        {"name": "耦合总时间", "value": 0.002, "unit": "s", "source": "参考模型参数"},
        {"name": "耦合步长", "value": 1.0e-5, "unit": "s", "source": "参考模型参数"},
        {"name": "最大耦合迭代", "value": 5, "unit": "", "source": "参考模型参数"},
    ]
    return {
        "status": "SUCCESS",
        "summary": "已按参考模型参数生成双向流固耦合 Workbench 工程；该模式使用 Fluent + Transient Structural + System Coupling。",
        "sourceGeometryPath": str(source_geometry),
        "geometryPath": str(workbench_geometry),
        "geometryType": geometry_type or source_geometry.suffix.lstrip(".").upper(),
        "simulationMode": simulation_mode,
        "simulationModelName": "双向流固耦合仿真模型",
        "workDir": str(job_dir),
        "projectPath": str(project_path),
        "resultFilePath": str(project_path),
        "fsiReferenceConfigPath": str(config_path),
        "fluentReferenceJournalPath": str(fluent_journal_path),
        "metrics": metrics,
        "placeholder": False,
    }


def fluent_reference_journal(config):
    fluent = config["fluent"]
    fluid = config["fluid"]
    return f'''; Fluent reference setup notes generated by ansys_import_worker.py
; Intended boundary names: inlet, outlet, wall
; Material: hydraulic-oil
; density = {fluid["densityKgM3"]} kg/m3
; viscosity = {fluid["dynamicViscosityPaS"]} Pa*s
; bulk modulus = {fluid["bulkModulusPa"]} Pa
; inlet type = {fluent["inlet"]["type"]}
; inlet initial gauge pressure = {fluent["inlet"]["initialGaugePressurePa"]} Pa
; outlet gauge pressure = {fluent["outlet"]["gaugePressurePa"]} Pa
; wall is the System Coupling moving wall / force transfer region
'''


def fsi_workbench_journal(geometry_path, project_path, config_path, steps_path):
    geometry_text = str(geometry_path).replace("\\", "\\\\")
    project_text = str(project_path).replace("\\", "\\\\")
    config_text = str(config_path).replace("\\", "\\\\")
    steps_text = str(steps_path).replace("\\", "\\\\")
    return f'''# -*- coding: utf-8 -*-
# Auto-generated by ansys_import_worker.py
geometry_path = r"{geometry_text}"
project_path = r"{project_text}"
config_path = r"{config_text}"
steps_log_path = r"{steps_text}"

def log_step(message):
    try:
        fh = open(steps_log_path, "a")
        fh.write(str(message) + "\\n")
        fh.close()
    except Exception:
        pass

def get_first_template(candidates):
    last_error = None
    for template_name, solver_name in candidates:
        try:
            if solver_name:
                template = GetTemplate(TemplateName=template_name, Solver=solver_name)
                return template, template_name + " / " + solver_name
            template = GetTemplate(TemplateName=template_name)
            return template, template_name
        except Exception as exc:
            last_error = exc
            log_step("Template probe failed: " + str(template_name) + " / " + str(solver_name) + ": " + str(exc))
    raise Exception("No supported template found. Last error: " + str(last_error))

def safe_update(target, label):
    try:
        target.Update()
        log_step(label + ".Update succeeded")
        return
    except Exception as exc:
        log_step(label + ".Update skipped: " + str(exc))

fluid_template, fluid_template_name = get_first_template([
    (u"Fluid Flow", u"Fluent"),
    (u"Fluid Flow (Fluent)", None),
    (u"流体流动", u"Fluent"),
])
struct_template, struct_template_name = get_first_template([
    (u"Transient Structural", u"ANSYS"),
    (u"瞬态结构", u"ANSYS"),
])
coupling_template, coupling_template_name = get_first_template([
    (u"System Coupling", None),
    (u"系统耦合", None),
])

fluid_system = fluid_template.CreateSystem()
log_step("Fluid system created: " + str(fluid_template_name))
fluid_geometry = fluid_system.GetContainer(ComponentName="Geometry")
fluid_geometry.SetFile(FilePath=geometry_path)
safe_update(fluid_geometry, "Fluid Geometry")
safe_update(fluid_system, "Fluid System")

struct_system = struct_template.CreateSystem(Position="Right", RelativeTo=fluid_system)
log_step("Structural system created: " + str(struct_template_name))
struct_geometry = struct_system.GetContainer(ComponentName="Geometry")
struct_geometry.SetFile(FilePath=geometry_path)
safe_update(struct_geometry, "Structural Geometry")
safe_update(struct_system, "Structural System")

coupling_system = coupling_template.CreateSystem(Position="Right", RelativeTo=struct_system)
log_step("System Coupling system created: " + str(coupling_template_name))

try:
    fluid_solution = fluid_system.GetComponent(Name="Solution")
    coupling_setup = coupling_system.GetComponent(Name="Setup")
    fluid_solution.TransferData(TargetComponent=coupling_setup)
    log_step("Fluid solution transferred to System Coupling setup")
except Exception as exc:
    log_step("Fluid transfer to System Coupling skipped: " + str(exc))

try:
    struct_setup = struct_system.GetComponent(Name="Setup")
    coupling_setup = coupling_system.GetComponent(Name="Setup")
    struct_setup.TransferData(TargetComponent=coupling_setup)
    log_step("Structural transfer to System Coupling setup")
except Exception as exc:
    log_step("Structural transfer to System Coupling skipped: " + str(exc))

try:
    setup = coupling_system.GetContainer(ComponentName="Setup")
    analysis_settings = setup.GetAnalysisSettings()
    analysis_settings.EndTime = "0.002 [s]"
    analysis_settings.StepSize = "1E-05 [s]"
    log_step("System Coupling time settings applied")
except Exception as exc:
    log_step("System Coupling time settings skipped: " + str(exc))

log_step("Reference config: " + config_path)
Save(FilePath=project_path, Overwrite=True)
'''


def workbench_journal(
    geometry_path,
    project_path,
    mechanical_script_path,
    mechanical_result_path,
    mechanical_ready_path,
    mechanical_command_error_path,
    simulation_mode,
):
    geometry_text = str(geometry_path).replace("\\", "\\\\")
    project_text = str(project_path).replace("\\", "\\\\")
    mechanical_script_text = str(mechanical_script_path).replace("\\", "\\\\")
    mechanical_result_text = str(mechanical_result_path).replace("\\", "\\\\")
    mechanical_started_text = str(mechanical_result_path.with_name("mechanical_started.txt")).replace("\\", "\\\\")
    mechanical_ready_text = str(mechanical_ready_path).replace("\\", "\\\\")
    mechanical_command_error_text = str(mechanical_command_error_path).replace("\\", "\\\\")
    mechanical_interactive_text = "True" if MECHANICAL_INTERACTIVE else "False"
    keep_mechanical_open_text = "True" if KEEP_MECHANICAL_OPEN else "False"
    selected_template_text = str(project_path.with_name("selected_template.txt")).replace("\\", "\\\\")
    template_errors_text = str(project_path.with_name("template_probe_errors.txt")).replace("\\", "\\\\")
    steps_log_text = str(project_path.with_name("workbench_steps.txt")).replace("\\", "\\\\")
    if simulation_mode == "geometry_import":
        template_candidates_text = '["Geometry"]'
    elif simulation_mode in (MODE_DEMO, "static_structural", "equivalent_static"):
        template_candidates_text = (
            '[u"Static Structural", '
            'u"\\u9759\\u6001\\u7ed3\\u6784", '
            'u"Transient Structural", '
            'u"\\u77ac\\u6001\\u7ed3\\u6784"]'
        )
    else:
        template_candidates_text = (
            '[u"Transient Structural", '
            'u"\\u77ac\\u6001\\u7ed3\\u6784", '
            'u"Static Structural", '
            'u"\\u9759\\u6001\\u7ed3\\u6784"]'
        )
    mechanical_block = ""
    if simulation_mode != "geometry_import":
        mechanical_block = f'''
model = system.GetContainer(ComponentName="Model")
safe_update(model, "Model")
mechanical_script_path = r"{mechanical_script_text}"
mechanical_started_path = r"{mechanical_started_text}"
mechanical_ready_path = r"{mechanical_ready_text}"
mechanical_command_error_path = r"{mechanical_command_error_text}"
mechanical_result_path = r"{mechanical_result_text}"
mechanical_handshake_command = "open(" + repr(mechanical_ready_path) + ", 'w').write('READY')"
mechanical_command = "execfile(" + repr(mechanical_script_path) + ")"
import os
import time
log_step("Opening Mechanical")
model.Edit(Interactive={mechanical_interactive_text})
time.sleep({MECHANICAL_OPEN_WAIT})

def wait_for_file(path, seconds):
    for wait_index in range(seconds):
        if os.path.exists(path):
            return True
        time.sleep(1)
    return False

def remove_file(path):
    try:
        if os.path.exists(path):
            os.remove(path)
    except Exception:
        pass

def send_python_command(label, command):
    try:
        model.SendCommand(Language="Python", Command=command)
        log_step(label + " sent")
        return True
    except Exception as send_exc:
        try:
            error_log = open(mechanical_command_error_path, "a")
            error_log.write(label + ": " + str(send_exc) + "\\n")
            error_log.close()
        except Exception:
            pass
        log_step(label + " SendCommand failed: " + str(send_exc))
        return False

for attempt in range({MECHANICAL_HANDSHAKE_RETRIES}):
    remove_file(mechanical_ready_path)
    log_step("Sending Mechanical handshake attempt " + str(attempt + 1))
    send_python_command("Mechanical handshake", mechanical_handshake_command)
    if wait_for_file(mechanical_ready_path, {MECHANICAL_HANDSHAKE_WAIT}):
        log_step("Mechanical command channel ready")
        break
    log_step("Mechanical handshake timeout")
else:
    raise Exception("Mechanical command channel did not become ready. Check Mechanical scripting support and license.")

log_step("Sending Mechanical script")
send_python_command("Mechanical script", mechanical_command)

if not wait_for_file(mechanical_started_path, {MECHANICAL_START_TIMEOUT}):
    log_step("Mechanical script start timeout")
    raise Exception("Mechanical script did not start after command channel handshake. Check execfile support and mechanical_setup.py.")

log_step("Mechanical script started")
for wait_index in range({MECHANICAL_RESULT_TIMEOUT}):
    if os.path.exists(mechanical_result_path):
        log_step("Mechanical result detected")
        break
    time.sleep(1)
else:
    log_step("Mechanical result timeout")
if {keep_mechanical_open_text}:
    log_step("Mechanical left open")
else:
    model.Exit()
    log_step("Mechanical closed")
'''
    return f'''# -*- coding: utf-8 -*-
# Auto-generated by ansys_import_worker.py
geometry_path = r"{geometry_text}"
project_path = r"{project_text}"
selected_template_path = r"{selected_template_text}"
template_errors_path = r"{template_errors_text}"
steps_log_path = r"{steps_log_text}"
template_candidates = {template_candidates_text}

def log_step(message):
    try:
        step_log = open(steps_log_path, "a")
        step_log.write(str(message) + "\\n")
        step_log.close()
    except Exception:
        pass

template = None
selected_template = ""
last_error = None
template_errors = []
for template_name in template_candidates:
    for solver_name in ["ANSYS", None]:
        try:
            if solver_name is None:
                template = GetTemplate(TemplateName=template_name)
                selected_template = template_name
            else:
                template = GetTemplate(TemplateName=template_name, Solver=solver_name)
                selected_template = template_name + " / " + solver_name
            break
        except Exception as exc:
            last_error = exc
            template_errors.append(str(template_name) + " / " + str(solver_name) + ": " + str(exc))
    if template is not None:
        break

if template is None:
    error_log = open(template_errors_path, "w")
    error_log.write("\\n".join(template_errors))
    error_log.close()
    raise Exception("No supported ANSYS template found. Tried: " + ", ".join(template_candidates) + ". Last error: " + str(last_error))

template_log = open(selected_template_path, "w")
try:
    template_log.write(selected_template.encode("utf-8"))
except Exception:
    template_log.write(str(selected_template))
template_log.close()

def safe_update(target, label):
    try:
        target.Update()
        log_step(label + ".Update succeeded")
        return
    except Exception:
        pass
    try:
        target.Refresh()
        log_step(label + ".Refresh succeeded")
        return
    except Exception:
        pass
    log_step(label + " update skipped")

system = template.CreateSystem()
log_step("System created from template: " + str(selected_template))
geometry = system.GetContainer(ComponentName="Geometry")
geometry.SetFile(FilePath=geometry_path)
log_step("Geometry file set: " + geometry_path)
safe_update(geometry, "Geometry")
safe_update(system, "System")
{mechanical_block}
Save(FilePath=project_path, Overwrite=True)
'''


def mechanical_script(result_path, started_path, trace_path, stress_image_path, mesh_size_mm, image_width, image_height, pressure, pipe_geometry, material, simulation_mode):
    result_text = str(result_path).replace("\\", "\\\\")
    started_text = str(started_path).replace("\\", "\\\\")
    trace_text = str(trace_path).replace("\\", "\\\\")
    image_text = str(stress_image_path).replace("\\", "\\\\")
    initial_pressure_pa = pressure["initialPressurePa"]
    peak_pressure_pa = pressure["peakPressurePa"]
    rise_time_s = pressure["riseTimeS"]
    pressure_expression = json.dumps(pressure["expression"], ensure_ascii=False)
    initial_pressure_mpa = initial_pressure_pa / 1000000.0
    peak_pressure_mpa = peak_pressure_pa / 1000000.0
    outer_diameter_mm = pipe_geometry["outerDiameterMm"]
    inner_diameter_mm = pipe_geometry["innerDiameterMm"]
    wall_thickness_mm = pipe_geometry["wallThicknessMm"]
    centerline_points = json.dumps(pipe_geometry.get("centerlinePoints") or [])
    material_name = json.dumps(material["materialName"], ensure_ascii=False)
    young_modulus_pa = material["youngModulusPa"]
    poisson_ratio = material["poissonRatio"]
    tensile_yield_strength_pa = material["tensileYieldStrengthPa"]
    tensile_ultimate_strength_pa = material["tensileUltimateStrengthPa"]
    static_mode = simulation_mode in (MODE_DEMO, "static_structural", "equivalent_static")
    static_mode_text = "True" if static_mode else "False"
    return f'''# -*- coding: utf-8 -*-
# Auto-generated by ansys_import_worker.py
import json
import traceback
import time

result_path = r"{result_text}"
started_path = r"{started_text}"
trace_path = r"{trace_text}"
stress_image_path = r"{image_text}"
mesh_size_mm = {mesh_size_mm}
image_width = {image_width}
image_height = {image_height}
initial_pressure_mpa = {initial_pressure_mpa}
peak_pressure_mpa = {peak_pressure_mpa}
rise_time_s = {rise_time_s}
pressure_expression = {pressure_expression}
outer_diameter_mm = {outer_diameter_mm}
inner_diameter_mm = {inner_diameter_mm}
wall_thickness_mm = {wall_thickness_mm}
centerline_points = {centerline_points}
material_name = {material_name}
young_modulus_pa = {young_modulus_pa}
poisson_ratio = {poisson_ratio}
tensile_yield_strength_pa = {tensile_yield_strength_pa}
tensile_ultimate_strength_pa = {tensile_ultimate_strength_pa}
static_mode = {static_mode_text}

def write_result(payload):
    with open(result_path, "w") as fh:
        json.dump(payload, fh)

def write_trace(message):
    try:
        with open(trace_path, "a") as fh:
            fh.write(str(message) + "\\n")
    except Exception:
        pass

def safe_text(value):
    try:
        return str(value)
    except Exception:
        return ""

def safe_attr(value, name):
    try:
        return getattr(value, name)
    except Exception:
        return None

def quantity_value(value):
    try:
        return float(value.Value)
    except Exception:
        pass
    try:
        text = safe_text(value).strip()
        return float(text.split("[", 1)[0].strip())
    except Exception:
        return None

def quantity_unit(value, default_unit):
    try:
        return str(value.Unit)
    except Exception:
        pass
    text = safe_text(value)
    if "[" in text and "]" in text:
        return text.split("[", 1)[1].split("]", 1)[0]
    return default_unit

def add_quantity_metric(metrics, name, quantity, default_unit):
    raw_value = safe_text(quantity)
    numeric_value = quantity_value(quantity)
    unit = quantity_unit(quantity, default_unit)
    display_value = numeric_value if numeric_value is not None else raw_value
    metrics.append({{
        "name": name,
        "value": display_value,
        "rawValue": raw_value,
        "numericValue": numeric_value,
        "unit": unit,
        "source": "Mechanical"
    }})
    return numeric_value

def add_plain_metric(metrics, name, value, unit, source):
    metrics.append({{
        "name": name,
        "value": value,
        "rawValue": safe_text(value) + (" [" + unit + "]" if unit else ""),
        "numericValue": value,
        "unit": unit,
        "source": source
    }})

def centerline_length_m():
    if not centerline_points or len(centerline_points) < 2:
        return None
    length = 0.0
    for index in range(len(centerline_points) - 1):
        ax, ay, az = centerline_points[index]
        bx, by, bz = centerline_points[index + 1]
        dx = bx - ax
        dy = by - ay
        dz = bz - az
        length += (dx * dx + dy * dy + dz * dz) ** 0.5
    return length

try:
    with open(started_path, "w") as fh:
        fh.write("STARTED")
    write_trace("STARTED")
except Exception:
    pass

def all_faces():
    faces = []
    geo = ExtAPI.DataModel.GeoData
    for assembly in geo.Assemblies:
        for part in assembly.Parts:
            for body in part.Bodies:
                for face in body.Faces:
                    faces.append(face)
    return faces

def all_bodies():
    bodies = []
    geo = ExtAPI.DataModel.GeoData
    for assembly in geo.Assemblies:
        for part in assembly.Parts:
            for body in part.Bodies:
                bodies.append(body)
    return bodies

def tree_children(obj):
    children = safe_attr(obj, "Children")
    if children is None:
        return []
    try:
        return list(children)
    except Exception:
        result = []
        try:
            for child in children:
                result.append(child)
        except Exception:
            pass
        return result

def all_tree_bodies(root):
    result = []
    stack = [root]
    while stack:
        item = stack.pop()
        if safe_attr(item, "Material") is not None:
            result.append(item)
        for child in tree_children(item):
            stack.append(child)
    return result

def material_candidates():
    names = []
    if material_name:
        names.append(material_name)
    if "不锈" in material_name or "stainless" in material_name.lower():
        names.extend(["Stainless Steel", "不锈钢"])
    names.extend(["Structural Steel", "结构钢"])
    unique = []
    for name in names:
        if name and name not in unique:
            unique.append(name)
    return unique

def face_center_x(face):
    centroid = face.Centroid
    try:
        return float(centroid[0])
    except Exception:
        return float(centroid.X)

def point_xyz(point):
    try:
        return (float(point[0]), float(point[1]), float(point[2]))
    except Exception:
        pass
    try:
        return (float(point.X), float(point.Y), float(point.Z))
    except Exception:
        pass
    coords = safe_attr(point, "Coordinates")
    if coords is not None:
        try:
            return (float(coords[0]), float(coords[1]), float(coords[2]))
        except Exception:
            pass
    return None

def face_centroid_xyz(face):
    point = point_xyz(face.Centroid)
    if point is not None:
        return point
    return (face_center_x(face), 0.0, 0.0)

def face_area(face):
    area = safe_attr(face, "Area")
    if area is None:
        return None
    return quantity_value(area)

def face_radius(face):
    for name in ["Radius", "MinorRadius"]:
        value = safe_attr(face, name)
        parsed = quantity_value(value)
        if parsed is not None and parsed > 0:
            return parsed
    surface = safe_attr(face, "Surface")
    if surface is not None:
        for name in ["Radius", "MinorRadius"]:
            value = safe_attr(surface, name)
            parsed = quantity_value(value)
            if parsed is not None and parsed > 0:
                return parsed
    return None

def face_sample_points(face):
    points = []
    for collection_name in ["Vertices", "Points"]:
        collection = safe_attr(face, collection_name)
        if collection is None:
            continue
        try:
            for item in collection:
                point = point_xyz(safe_attr(item, "Point") or safe_attr(item, "Location") or item)
                if point is not None:
                    points.append(point)
        except Exception:
            pass
    edges = safe_attr(face, "Edges")
    if edges is not None:
        try:
            for edge in edges:
                vertices = safe_attr(edge, "Vertices")
                if vertices is None:
                    continue
                for vertex in vertices:
                    point = point_xyz(safe_attr(vertex, "Point") or safe_attr(vertex, "Location") or vertex)
                    if point is not None:
                        points.append(point)
        except Exception:
            pass
    return points

def dist_point_segment(point, a, b):
    px, py, pz = point
    ax, ay, az = a
    bx, by, bz = b
    vx = bx - ax
    vy = by - ay
    vz = bz - az
    wx = px - ax
    wy = py - ay
    wz = pz - az
    vv = vx * vx + vy * vy + vz * vz
    if vv <= 1.0e-24:
        dx = px - ax
        dy = py - ay
        dz = pz - az
        return (dx * dx + dy * dy + dz * dz) ** 0.5
    t = (wx * vx + wy * vy + wz * vz) / vv
    if t < 0.0:
        t = 0.0
    elif t > 1.0:
        t = 1.0
    cx = ax + t * vx
    cy = ay + t * vy
    cz = az + t * vz
    dx = px - cx
    dy = py - cy
    dz = pz - cz
    return (dx * dx + dy * dy + dz * dz) ** 0.5

def scaled_centerline_points(scale):
    return [[point[0] * scale, point[1] * scale, point[2] * scale] for point in centerline_points]

def distance_to_centerline(point, scaled_points):
    if not scaled_points or len(scaled_points) < 2:
        return None
    best = None
    for index in range(len(scaled_points) - 1):
        distance = dist_point_segment(point, scaled_points[index], scaled_points[index + 1])
        if best is None or distance < best:
            best = distance
    return best

def face_mean_centerline_distance(face, scaled_points):
    points = face_sample_points(face)
    distances = []
    for point in points:
        distance = distance_to_centerline(point, scaled_points)
        if distance is not None:
            distances.append(distance)
    if distances:
        return sum(distances) / len(distances)
    return distance_to_centerline(face_centroid_xyz(face), scaled_points)

def select_inner_wall_faces(faces, fixed_face_ids):
    if inner_diameter_mm <= 0 or outer_diameter_mm <= inner_diameter_mm:
        raise Exception("Invalid pipe diameters for inner-wall pressure selection.")
    xs = [face_center_x(face) for face in faces]
    min_x = min(xs)
    max_x = max(xs)
    x_span = max(max_x - min_x, 1.0e-9)
    end_x_tolerance = x_span * 0.02

    unit_candidates = [
        ("m", 1.0, inner_diameter_mm / 2000.0, outer_diameter_mm / 2000.0, wall_thickness_mm / 1000.0),
        ("mm", 1000.0, inner_diameter_mm / 2.0, outer_diameter_mm / 2.0, wall_thickness_mm),
    ]
    best_result = None
    best_debug = []
    for unit_name, centerline_scale, inner_radius, outer_radius, wall_thickness in unit_candidates:
        if inner_radius <= 0 or outer_radius <= inner_radius:
            continue
        tolerance = max(wall_thickness * 0.25, outer_radius * 0.02, 5.0e-5 * centerline_scale)
        scaled_points = scaled_centerline_points(centerline_scale)
        candidates = []
        face_debug = []
        for face in faces:
            center_x = face_center_x(face)
            radius = face_radius(face)
            mean_distance = face_mean_centerline_distance(face, scaled_points)
            area = face_area(face)
            metric = radius if radius is not None else mean_distance
            metric_source = "radius" if radius is not None else "centerline-distance"
            is_end_face = center_x <= min_x + end_x_tolerance or center_x >= max_x - end_x_tolerance
            face_debug.append(
                "FACE_INFO unit={{0}}, id={{1}}, x={{2:.8g}}, area={{3}}, radius={{4}}, centerlineDistance={{5}}, endFace={{6}}".format(
                    unit_name,
                    face.Id,
                    center_x,
                    safe_text(area),
                    safe_text(radius),
                    safe_text(mean_distance),
                    str(is_end_face),
                )
            )
            if face.Id in fixed_face_ids:
                continue
            if is_end_face:
                continue
            if metric is not None and abs(metric - inner_radius) <= tolerance:
                candidates.append((abs(metric - inner_radius), face.Id, metric_source, metric))
        face_debug.insert(0, "INNER_WALL_UNIT_TRY={{0}}, targetRadius={{1}}, tolerance={{2}}, matches={{3}}".format(
            unit_name,
            inner_radius,
            tolerance,
            len(candidates),
        ))
        if candidates and (best_result is None or len(candidates) > len(best_result[4])):
            best_result = (unit_name, inner_radius, tolerance, face_debug, candidates)
        if not best_debug or len(face_debug) > len(best_debug):
            best_debug = face_debug

    debug_lines = best_result[3] if best_result else best_debug
    for line in debug_lines[:90]:
        write_trace(line)
    if best_result:
        unit_name, inner_radius, tolerance, _face_debug, candidates = best_result
        candidates.sort(key=lambda item: item[0])
        ids = []
        for _delta, face_id, _source, _metric in candidates:
            if face_id not in ids:
                ids.append(face_id)
        write_trace("INNER_WALL_SELECTION=radius_or_centerline_match")
        write_trace("INNER_WALL_UNIT=" + unit_name)
        write_trace("INNER_WALL_TARGET_RADIUS=" + str(inner_radius))
        write_trace("INNER_WALL_TOLERANCE=" + str(tolerance))
        write_trace("INNER_WALL_FACES=" + ",".join([str(face_id) for face_id in ids]))
        return ids
    raise Exception("No inner wall faces matched the pipe inner radius in meter or millimeter scale. Regenerate CAD as a true through-hollow pipe and check centerline CSV import.")

def geometry_selection(ids):
    selection = ExtAPI.SelectionManager.CreateSelectionInfo(SelectionTypeEnum.GeometryEntities)
    selection.Ids = list(ids)
    return selection

def status_text(obj):
    return safe_text(safe_attr(obj, "Status"))

def solve_required(text):
    normalized = (text or "").lower()
    return normalized == "" or "solverequired" in normalized or "required" in normalized or "none" == normalized

try:
    model = ExtAPI.DataModel.Project.Model
    write_trace("MODEL_READY")
    analysis = model.Analyses[0]
    try:
        analysis.Activate()
    except Exception:
        pass
    if not static_mode:
        analysis.AnalysisSettings.NumberOfSteps = 1
        analysis.AnalysisSettings.CurrentStepNumber = 1
        try:
            analysis.AnalysisSettings.StepEndTime = Quantity(str(rise_time_s) + " [s]")
        except Exception:
            pass

    material_assigned = 0
    for body in all_tree_bodies(model.Geometry):
        for candidate_material in material_candidates():
            try:
                body.Material = candidate_material
                material_assigned += 1
                break
            except Exception:
                pass
    write_trace("MATERIAL_ASSIGNED=" + str(material_assigned))

    write_trace("MESHING")
    model.Mesh.ElementSize = Quantity(str(mesh_size_mm) + " [mm]")
    model.Mesh.GenerateMesh()
    write_trace("MESH_DONE")

    faces = all_faces()
    write_trace("FACE_COUNT=" + str(len(faces)))
    if len(faces) < 2:
        raise Exception("Mechanical geometry has fewer than two faces; cannot apply end constraints.")
    xs = [face_center_x(face) for face in faces]
    min_x = min(xs)
    max_x = max(xs)
    span = max(max_x - min_x, 1.0e-9)
    tolerance = span * 0.02
    fixed_faces = [
        face.Id for face in faces
        if face_center_x(face) <= min_x + tolerance or face_center_x(face) >= max_x - tolerance
    ]
    if not fixed_faces:
        fixed_faces = [faces[xs.index(min_x)].Id]
    pressure_faces = select_inner_wall_faces(faces, fixed_faces)

    fixed = analysis.AddFixedSupport()
    fixed.Location = geometry_selection(fixed_faces)
    write_trace("FIXED_SUPPORT_MODE=both_ends")
    write_trace("FIXED_SUPPORT_MIN_X=" + str(min_x))
    write_trace("FIXED_SUPPORT_MAX_X=" + str(max_x))
    write_trace("FIXED_SUPPORT_FACES=" + str(len(fixed_faces)))

    pressure = analysis.AddPressure()
    pressure.Location = geometry_selection(pressure_faces)
    if static_mode:
        try:
            pressure.Magnitude = Quantity(str(peak_pressure_mpa) + " [MPa]")
        except Exception:
            pressure.Magnitude.Output.DiscreteValues = [Quantity(str(peak_pressure_mpa) + " [MPa]")]
    else:
        pressure.Magnitude.Inputs[0].DiscreteValues = [Quantity("0 [s]"), Quantity(str(rise_time_s) + " [s]")]
        pressure.Magnitude.Output.DiscreteValues = [Quantity(str(initial_pressure_mpa) + " [MPa]"), Quantity(str(peak_pressure_mpa) + " [MPa]")]
    write_trace("PRESSURE_FACES=" + str(len(pressure_faces)))

    solution = analysis.Solution
    stress = solution.AddEquivalentStress()
    deformation = solution.AddTotalDeformation()
    if not static_mode:
        try:
            stress.DisplayTime = Quantity(str(rise_time_s) + " [s]")
            deformation.DisplayTime = Quantity(str(rise_time_s) + " [s]")
        except Exception:
            pass
    write_trace("SOLVING")
    solve_errors = []
    try:
        analysis.Solve(True)
    except Exception as solve_exc:
        solve_errors.append("analysis.Solve: " + str(solve_exc))
        try:
            solution.Solve(True)
        except Exception as solution_solve_exc:
            solve_errors.append("solution.Solve: " + str(solution_solve_exc))
    if solve_required(status_text(solution)):
        try:
            solution.Solve(True)
        except Exception as solution_retry_exc:
            solve_errors.append("solution.Solve retry: " + str(solution_retry_exc))
    write_trace("ANALYSIS_SOLVE_DONE")
    try:
        solution.EvaluateAllResults()
    except Exception:
        pass
    try:
        stress.EvaluateAllResults()
        deformation.EvaluateAllResults()
    except Exception:
        pass
    write_trace("SOLVED")

    metrics = []
    warnings = []
    for solve_error in solve_errors:
        warnings.append(solve_error)
    solution_status = safe_text(safe_attr(solution, "Status"))
    stress_status = safe_text(safe_attr(stress, "Status"))
    deformation_status = safe_text(safe_attr(deformation, "Status"))
    write_trace("SOLUTION_STATUS=" + solution_status)
    write_trace("STRESS_STATUS=" + stress_status)
    write_trace("DEFORMATION_STATUS=" + deformation_status)
    stress_max_value = None
    stress_min_value = None
    deformation_max_value = None
    try:
        stress_max_value = add_quantity_metric(metrics, "最大等效应力", stress.Maximum, "Pa")
    except Exception as metric_exc:
        warnings.append("读取最大等效应力失败：" + str(metric_exc))
    try:
        stress_min_value = add_quantity_metric(metrics, "最小等效应力", stress.Minimum, "Pa")
    except Exception as metric_exc:
        warnings.append("读取最小等效应力失败：" + str(metric_exc))
    try:
        deformation_max_value = add_quantity_metric(metrics, "最大总变形", deformation.Maximum, "m")
    except Exception as metric_exc:
        warnings.append("读取最大总变形失败：" + str(metric_exc))
    if peak_pressure_mpa > 0 and (stress_max_value is None or abs(stress_max_value) <= 1.0e-18) and (deformation_max_value is None or abs(deformation_max_value) <= 1.0e-18):
        warnings.append("Mechanical 求解完成但等效应力和总变形均为 0，说明载荷、约束或结果评估没有形成有效响应。")

    engineering_warnings = []
    engineering_status = "NOT_CHECKED"
    engineering_estimates = {{}}
    try:
        pressure_pa = peak_pressure_mpa * 1000000.0
        inner_radius_m = inner_diameter_mm / 2000.0
        wall_m = wall_thickness_mm / 1000.0
        length_m = centerline_length_m()
        if pressure_pa > 0 and inner_radius_m > 0 and wall_m > 0:
            nominal_hoop_pa = pressure_pa * inner_radius_m / wall_m
            nominal_axial_pa = nominal_hoop_pa / 2.0
            nominal_vm_pa = (nominal_hoop_pa * nominal_hoop_pa - nominal_hoop_pa * nominal_axial_pa + nominal_axial_pa * nominal_axial_pa) ** 0.5
            engineering_estimates = {{
                "nominalHoopStressPa": nominal_hoop_pa,
                "nominalAxialStressPa": nominal_axial_pa,
                "nominalVonMisesStressPa": nominal_vm_pa,
                "centerlineLengthM": length_m,
                "tensileYieldStrengthPa": tensile_yield_strength_pa,
                "tensileUltimateStrengthPa": tensile_ultimate_strength_pa,
            }}
            add_plain_metric(metrics, "薄壁管名义环向应力估算", nominal_hoop_pa, "Pa", "Engineering check")
            add_plain_metric(metrics, "薄壁管名义轴向应力估算", nominal_axial_pa, "Pa", "Engineering check")
            add_plain_metric(metrics, "薄壁管名义 Von-Mises 应力估算", nominal_vm_pa, "Pa", "Engineering check")
            if stress_max_value is not None and nominal_vm_pa > 0:
                stress_ratio = stress_max_value / nominal_vm_pa
                engineering_estimates["feToNominalStressRatio"] = stress_ratio
                add_plain_metric(metrics, "FE 最大应力 / 名义应力", stress_ratio, "", "Engineering check")
                if stress_ratio > 10.0:
                    engineering_warnings.append("FE 最大应力超过薄壁管名义应力 10 倍，结果可能受约束奇异点、错误载荷面或网格异常影响。")
                elif stress_ratio > 6.0:
                    engineering_warnings.append("FE 最大应力超过薄壁管名义应力 6 倍，建议复核约束、局部网格和应力集中位置。")
            if stress_max_value is not None and tensile_yield_strength_pa > 0:
                yield_ratio = stress_max_value / tensile_yield_strength_pa
                engineering_estimates["yieldUtilization"] = yield_ratio
                add_plain_metric(metrics, "屈服强度利用率", yield_ratio, "", "Engineering check")
                if yield_ratio > 1.0:
                    engineering_warnings.append("最大等效应力超过材料屈服强度，线弹性结果只能作为失效判据，不能作为安全工作状态。")
            if stress_max_value is not None and tensile_ultimate_strength_pa > 0:
                ultimate_ratio = stress_max_value / tensile_ultimate_strength_pa
                engineering_estimates["ultimateUtilization"] = ultimate_ratio
                add_plain_metric(metrics, "极限强度利用率", ultimate_ratio, "", "Engineering check")
                if ultimate_ratio > 1.0:
                    engineering_warnings.append("最大等效应力超过材料拉伸极限强度，当前设计在该载荷下不满足强度要求。")
            if deformation_max_value is not None and length_m and length_m > 0:
                deformation_ratio = deformation_max_value / length_m
                engineering_estimates["deformationToLengthRatio"] = deformation_ratio
                add_plain_metric(metrics, "最大变形 / 管中心线长度", deformation_ratio, "", "Engineering check")
                if deformation_ratio > 0.2:
                    engineering_warnings.append("最大变形超过管中心线长度 20%，线性小变形静力假设不可信。")
                elif deformation_ratio > 0.05:
                    engineering_warnings.append("最大变形超过管中心线长度 5%，建议采用非线性大变形或重新检查边界条件。")
            engineering_status = "PASS" if not engineering_warnings else "REVIEW_REQUIRED"
    except Exception as engineering_exc:
        engineering_status = "CHECK_FAILED"
        engineering_warnings.append("工程合理性校核失败：" + str(engineering_exc))
    for engineering_warning in engineering_warnings:
        warnings.append(engineering_warning)
    try:
        stress.Activate()
        time.sleep(1)
        try:
            ExtAPI.Graphics.Camera.SetFit()
        except Exception:
            try:
                ExtAPI.Graphics.Camera.Fit()
            except Exception:
                pass
        time.sleep(1)
        try:
            from Ansys.Mechanical.Graphics import GraphicsImageExportSettings, GraphicsImageExportFormat, GraphicsResolutionType
            image_settings = GraphicsImageExportSettings()
            image_settings.Resolution = GraphicsResolutionType.HighResolution
            image_settings.Width = image_width
            image_settings.Height = image_height
            ExtAPI.Graphics.ExportImage(stress_image_path, GraphicsImageExportFormat.PNG, image_settings)
            write_trace("IMAGE_EXPORTED_HIGH_RES=" + str(image_width) + "x" + str(image_height))
        except Exception as high_res_exc:
            write_trace("IMAGE_HIGH_RES_EXPORT_FAILED=" + str(high_res_exc))
            ExtAPI.Graphics.ExportImage(stress_image_path)
        write_trace("IMAGE_EXPORTED")
    except Exception as image_exc:
        warnings.append("导出等效应力云图失败：" + str(image_exc))
    result_status = "SUCCESS"
    error_message = ""
    if warnings and peak_pressure_mpa > 0 and (stress_max_value is None or abs(stress_max_value) <= 1.0e-18) and (deformation_max_value is None or abs(deformation_max_value) <= 1.0e-18):
        result_status = "FAILED"
        error_message = warnings[-1]
    write_result({{
        "status": result_status,
        "errorMessage": error_message,
        "warnings": warnings,
        "engineeringStatus": engineering_status,
        "engineeringWarnings": engineering_warnings,
        "engineeringEstimates": engineering_estimates,
        "solutionStatus": solution_status,
        "stressStatus": stress_status,
        "deformationStatus": deformation_status,
        "maxEquivalentStressValue": stress_max_value,
        "minEquivalentStressValue": stress_min_value,
        "maxTotalDeformationValue": deformation_max_value,
        "staticMode": static_mode,
        "solveErrors": solve_errors,
        "fixedFaceCount": len(fixed_faces),
        "pressureFaceCount": len(pressure_faces),
        "pressureFaceMode": "inner_wall",
        "pipeOuterDiameterMm": outer_diameter_mm,
        "pipeInnerDiameterMm": inner_diameter_mm,
        "pipeWallThicknessMm": wall_thickness_mm,
        "materialName": material_name,
        "youngModulusPa": young_modulus_pa,
        "poissonRatio": poisson_ratio,
        "tensileYieldStrengthPa": tensile_yield_strength_pa,
        "tensileUltimateStrengthPa": tensile_ultimate_strength_pa,
        "meshSizeMm": mesh_size_mm,
        "initialPressureMpa": initial_pressure_mpa,
        "peakPressureMpa": peak_pressure_mpa,
        "riseTimeS": rise_time_s,
        "pressureExpression": pressure_expression,
        "metrics": metrics
    }})
    write_trace("RESULT_WRITTEN")
except Exception as exc:
    write_trace("FAILED: " + str(exc))
    write_result({{"status": "FAILED", "errorMessage": str(exc), "traceback": traceback.format_exc()}})
    raise
'''


def ansys_error_message(job_dir, fallback):
    details = []
    for name in (
        "workbench_steps.txt",
        "mechanical_command_error.txt",
        "mechanical_trace.txt",
        "mechanical_result.json",
        "workbench_command.txt",
        "ansys_stdout.txt",
        "ansys_stderr.txt",
    ):
        path = job_dir / name
        if path.exists():
            text = path.read_text(encoding="utf-8", errors="ignore").strip()
            if text:
                details.append(name + ": " + text[-1500:])
    message = fallback + (" " + " ".join(details) if details else "")
    if "RegistryException" in message:
        message += (
            " Workbench failed before executing the journal. This is an ANSYS Workbench startup/registry environment "
            "issue, not a CAD geometry or Mechanical solve failure. Close ANSYS, restart the ANSYS worker from "
            "runwb2.bat, and if it persists restart Windows or repair the ANSYS Workbench user/installation registry."
        )
    return message


class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        path = urlparse(self.path).path
        if path in ("/health", "/api/ansys/health"):
            self.respond(200, worker_health())
            return
        self.send_error(404)

    def do_POST(self):
        if urlparse(self.path).path != "/api/ansys/import-geometry":
            self.send_error(404)
            return
        payload = {}
        try:
            length = int(self.headers.get("Content-Length", "0"))
            payload = json.loads(self.rfile.read(length).decode("utf-8") or "{}")
            self.respond(200, import_geometry(payload))
        except Exception as exc:
            task_id = safe_task_id(payload.get("taskId") or "manual")
            job_dir = OUTPUT_DIR / f"task_{task_id}"
            job_dir.mkdir(parents=True, exist_ok=True)
            write_progress(job_dir, "FAILED", str(exc))
            self.respond(500, {"status": "FAILED", "errorMessage": str(exc)})

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
    print(f"ANSYS import worker listening on http://{HOST}:{PORT}/api/ansys/import-geometry")
    print("Set ANSYS_WORKBENCH_CMD to runwb2.bat or RunWB2.exe before starting this worker.")
    ThreadingHTTPServer((HOST, PORT), Handler).serve_forever()
