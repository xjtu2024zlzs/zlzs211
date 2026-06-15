# ANSYS Mechanical 求解 Worker

该本地 HTTP Worker 用于接收平台传入的 CAD 几何文件，并调用 ANSYS Workbench / Mechanical 创建结构分析项目、导入几何、划分网格、施加载荷并尝试求解。

## 启动方式

启动前需要配置 Workbench 命令路径。推荐在 `start_ansys_worker.bat` 中配置官方启动脚本：

```bat
set "ANSYS_WORKBENCH_CMD=D:\Program Files\ANSYS Inc\v221\Framework\bin\Win64\runwb2.bat"
```

上面的路径仅为当前机器的示例，请以本机 ANSYS 安装目录为准。`runwb2.bat` 会先设置 `AWP_ROOT221`、`IN_WB2` 和必要的 `PATH`，再启动 Workbench，通常比直接调用 exe 更稳定。

注意：`.lnk` 快捷方式路径不能作为 `ANSYS_WORKBENCH_CMD`。如果误配置为同目录的 `RunWB2.exe`，Worker 会自动改用旁边的 `runwb2.bat`；如果没有该 bat，则按配置的 exe 直接启动。

如果 Worker 返回 `Ansys.Utilities.Registry.RegistryException`，说明 Workbench 在执行 Journal 之前的框架初始化阶段失败，还没有开始导入几何或求解。这通常不是 CAD 模型问题。请先关闭所有 ANSYS/Mechanical 进程，用 `runwb2.bat` 重新启动 Worker；如果仍失败，重启 Windows 或修复 ANSYS Workbench 用户配置/安装 registry。

启动后服务地址为：

```text
http://127.0.0.1:18081/api/ansys/import-geometry
```

健康检查地址为：

```text
http://127.0.0.1:18081/api/ansys/health
```

该接口会返回 Worker 状态、原始 `ANSYS_WORKBENCH_CMD`、实际解析后的 Workbench 启动路径以及该文件是否存在。

## 请求示例

```json
{
  "taskId": 27,
  "geometry": {
    "geometryPath": "C:/path/to/pipe_model.step",
    "geometryType": "STEP",
    "centerlineCsvPath": "C:/path/to/pipe_centerline.csv"
  },
  "cadModel": {
    "params": {
      "pipeDiameter": 9.53,
      "pipeInnerDiameter": 7.73
    }
  },
  "simulationMode": "static_structural"
}
```

## 几何文件优先级

Java 后端传入 ANSYS Worker 的几何文件选择顺序为：

1. STEP：`pipe_model.step`
2. SolidWorks 原生文件：`pipe_native.SLDPRT`
3. Parasolid：`pipe_model.x_t`
4. STL：`pipe_model.stl`

其中 STEP 是当前优先格式。`SLDPRT` 是已经在 SolidWorks 中验证可打开的兜底格式。Parasolid 在当前环境中可能出现 SpaceClaim 无法编辑的问题，因此不再优先使用。

Worker 会把实际导入的几何文件复制到当前任务输出目录，文件名类似：

```text
ansys_worker/output/task_28/input_geometry.step
```

Workbench 项目会引用这份本地副本，便于排查平台实际传入了哪个 CAD 文件。

## Mechanical 求解流程

默认 `simulationMode` 为 `DEMO_SIMULATION_MODEL`，用于在入口峰值压力下生成等效静力应力云图。历史调用中的 `static_structural` 会兼容映射到该演示模型。

当 `simulationMode` 为 `DEMO_SIMULATION_MODEL` 时，Worker 会：

1. 创建结构分析系统。演示模型优先尝试：`Static Structural`、`静态结构`。每个模板会优先使用 `Solver=ANSYS` 查找，失败后再尝试不带 Solver 的查找方式。
2. 导入平台传入的 CAD 几何。
3. 打开 Mechanical，先发送一个最小握手命令，确认 Mechanical 可以接收 Workbench 的 `SendCommand`。
4. 握手成功后，通过 `execfile(...)` 执行 `mechanical_setup.py`。
5. 使用默认材料和自动网格。
6. 以几何 X 方向两端端面作为固定约束。
7. 读取平台传入的外径、内径和 `pipe_centerline.csv`，优先识别空心管内壁面，并将入口压力作为内压施加到内壁。
8. 读取平台传入的故障管段入口压强参数，并直接施加峰值压力。
9. 求解等效应力和总变形。
10. 输出 `mechanical_result.json` 和可用时的 `equivalent_stress.png`。

任务目录中还会生成：

```text
progress.json
ansys_stdout.txt
ansys_stderr.txt
import_geometry.wbjn
mechanical_setup.py
mechanical_command_ready.txt
mechanical_started.txt
mechanical_trace.txt
mechanical_result.json
workbench_command.txt
```

其中 `progress.json` 用于查看当前阶段，`workbench_command.txt` 用于查看实际调用的 Workbench 命令，`mechanical_command_ready.txt` 表示 Workbench 到 Mechanical 的命令通道已经打通，`mechanical_started.txt` 表示 Mechanical 正式求解脚本已经开始执行，`mechanical_trace.txt` 会记录 `MESHING`、`SOLVING`、`SOLVED` 等脚本阶段，`mechanical_result.json` 用于查看 Mechanical 求解是否成功。如果握手或脚本启动在限定时间内没有完成，Worker 会明确返回失败，避免平台长时间停留在处理中。

`mechanical_trace.txt` 还会记录 `FACE_INFO`、`INNER_WALL_TARGET_RADIUS_M`、`INNER_WALL_FACES` 等内壁识别信息。若没有任何面能匹配管道内半径，Worker 会失败退出，不会退回到端面压力，避免得到不符合工程实际的结果。

如果当前 Workbench 是中文环境，Worker 会自动尝试中文模板名。任务目录中的 `selected_template.txt` 会记录最终使用的模板。

针对 ANSYS 2022 R1 的 Workbench 对象差异，Worker 不再直接强制调用 `Geometry.Update()` 或 `Model.Update()`；如果当前对象不支持这些方法，会跳过并继续执行。任务目录中的 `workbench_steps.txt` 会记录 Workbench Journal 执行到的关键步骤。

压力载荷优先读取平台传入的 `faultPipeParameters.values`：

```text
INLET_PRESSURE_INITIAL
INLET_PRESSURE_PEAK
INLET_PRESSURE_RISE_TIME
INLET_PRESSURE_EXPRESSION
```

当前 Mechanical 脚本会把入口压力设置为从 `INLET_PRESSURE_INITIAL` 线性升至 `INLET_PRESSURE_PEAK`，上升时间取 `INLET_PRESSURE_RISE_TIME`。

管道几何优先读取 CAD 任务参数：

```text
pipeDiameter / PIPE_OUTER_DIAMETER
pipeInnerDiameter / PIPE_INNER_DIAMETER
pipe_centerline.csv
```

其中 `pipe_centerline.csv` 由 SolidWorks Worker 生成，用于在 Mechanical 中按管道中心线和内半径识别内壁压力面。

如果平台没有传入这些参数，才会使用环境变量默认值：

```bat
set "ANSYS_MESH_SIZE_MM=3"
set "ANSYS_KEEP_MECHANICAL_OPEN=1"
set "ANSYS_INITIAL_PRESSURE_PA=101325"
set "ANSYS_PRESSURE_PA=30000000"
set "ANSYS_RISE_TIME_S=0.001"
set "ANSYS_MECHANICAL_INTERACTIVE=1"
set "ANSYS_MECHANICAL_OPEN_WAIT=20"
set "ANSYS_MECHANICAL_HANDSHAKE_RETRIES=6"
set "ANSYS_MECHANICAL_HANDSHAKE_WAIT=10"
set "ANSYS_MECHANICAL_START_TIMEOUT=60"
set "ANSYS_MECHANICAL_RESULT_TIMEOUT=540"
set "ANSYS_IMAGE_EXPORT_WIDTH=1920"
set "ANSYS_IMAGE_EXPORT_HEIGHT=1080"
```

`ANSYS_MECHANICAL_INTERACTIVE=1` 时会用交互方式打开 Mechanical，便于确认 Mechanical 是否真正启动并接收脚本。若 Workbench 到 Mechanical 的命令通道未打通，Worker 会在 `workbench_steps.txt` 中记录 `Mechanical handshake timeout`；若握手成功但正式脚本未执行，会记录 `Mechanical script start timeout`。

Mechanical 求解使用 `analysis.Solve(True)` 触发分析系统求解，随后强制评估等效应力和总变形结果，并导出 `equivalent_stress.png`。结果 JSON 会记录 `solutionStatus`、`stressStatus`、`deformationStatus`、`warnings`、`maxEquivalentStressValue` 和 `maxTotalDeformationValue`。如果入口压力非 0 但应力和变形仍同时为 0，Worker 会把本次结果标记为失败，避免把无效结果误判为仿真成功。

为避免把数值异常的线弹性结果当作工程结论，Worker 会基于内压薄壁管公式计算名义环向应力、名义轴向应力和名义 Von-Mises 应力，并结合 `TENSILE_YIELD_STRENGTH`、`TENSILE_ULTIMATE_STRENGTH`、中心线长度进行合理性校核。结果 JSON 中会写入 `engineeringStatus`、`engineeringWarnings` 和 `engineeringEstimates`。若最大应力超过名义应力 10 倍、超过材料拉伸极限强度，或最大变形超过中心线长度 20%，本次结果会被标记为失败，需要复核约束、载荷面、网格或改用更合适的非线性模型。

云图优先按高分辨率导出，默认尺寸为 `1920 x 1080`，可通过 `ANSYS_IMAGE_EXPORT_WIDTH` 和 `ANSYS_IMAGE_EXPORT_HEIGHT` 调整。如果当前 ANSYS 版本不支持高分辨率导出 API，Worker 会自动回退为普通 `ExportImage`。

ANSYS 2022 R1 导出的最大/最小值中文 callout 可能出现字体替换异常。Worker 会在 PNG 导出后使用 Windows 中文字体进行图片后处理，重新绘制“最大应力点 / 最小应力点”标注，优先使用 `C:\Windows\Fonts\msyh.ttc`，再回退到黑体或宋体。

该流程是第一版自动化求解逻辑，边界面自动识别依赖几何方向。后续如果需要更精确的入口、出口、固定支座或压力作用面，应由 CAD 阶段输出命名面或由平台传入明确的面选择规则。

如果没有配置 `ANSYS_WORKBENCH_CMD`，Worker 会返回失败，不会伪造成功结果。

## 仿真模型选择

平台通过 `simulationMode` 选择本次要调用的仿真模型：

| 展示名称 | simulationMode | 输出目录 |
| --- | --- | --- |
| 演示仿真模型 | `DEMO_SIMULATION_MODEL` | `output/task_{taskId}/demo_simulation_model/` |
| 双向流固耦合仿真模型 | `BIDIRECTIONAL_FSI_MODEL` | `output/task_{taskId}/bidirectional_fsi_model/` |

`DEMO_SIMULATION_MODEL` 会继续执行当前 Mechanical 静力结构求解流程，并输出 `mechanical_result.json`、`mechanical_trace.txt` 和 `equivalent_stress.png`。

`BIDIRECTIONAL_FSI_MODEL` 会按参考模型参数生成 Fluent + Transient Structural + System Coupling 工程文件，并写出：

```text
fsi_reference.wbpj
fsi_reference_config.json
fluent_reference_setup.jou
workbench_steps.txt
```

该分支当前用于沉淀参考双向流固耦合工程的参数和 Workbench 系统结构。要进一步实现无人值守的真实双向耦合求解，需要补齐流体域几何、命名入口/出口/壁面、Fluent 可执行网格与求解 journal、System Coupling 数据传递验证。
