# 团队整合说明文档

本文档用于记录平台后续与其他团队整合时需要同步的内容。以后每次完成平台相关任务前，都需要检查本文件是否需要更新。

## 什么时候需要更新本文档

只要任务涉及下面任意内容，就需要同步更新本文档：

- 外部服务地址、端口、请求参数、返回字段发生变化。
- 数据库表、字段、初始化脚本、升级脚本、权限数据发生变化。
- 团队之间交换的文件格式发生变化，例如 STEP、Parasolid、STL、图片、Excel、模型文件、仿真结果包等。
- 启动方式、环境变量、部署顺序、本地软件路径发生变化。
- 任务流程状态、审批规则、菜单权限、用户角色发生变化。
- SolidWorks、ANSYS、Python 代理模型或其他第三方工具的集成方式发生变化。
- 错误提示、重试逻辑、日志文件、输出目录发生变化。

如果一次任务不影响团队整合，则不需要修改本文档。

## 当前平台模块

| 模块 | 职责 | 主要目录 | 整合说明 |
| --- | --- | --- | --- |
| RuoYi Java 后端 | 任务流程、数据保存、服务编排 | `ruoyi-modules/ruoyi-designtask1` | 负责调用 Python、SolidWorks、ANSYS 等外部服务。 |
| RuoYi 前端 | 设计任务、求解、CAD、仿真、归档页面 | `ruoyi-ui/src/views/designtask` | 页面请求参数需要和后端接口保持一致。 |
| Python 代理模型服务 | 代理模型优化求解 | `python/project2` | 默认端口 `9721`。 |
| SolidWorks Worker | 根据设计变量生成管路 CAD 模型 | `solidworks_worker` | 默认端口 `18080`，输出 SLDPRT、STEP、Parasolid、STL、预览图。 |
| ANSYS Worker | 将 CAD 几何导入 ANSYS Workbench，并尝试执行 Mechanical 结构求解 | `ansys_worker` | 默认端口 `18081`，需要配置 `ANSYS_WORKBENCH_CMD`。 |
| SQL 脚本 | 表结构、初始化数据、升级脚本 | `sql` | 涉及表结构变化时，需要提供升级脚本。 |

## 服务接口清单

| 服务 | 接口 | 用途 |
| --- | --- | --- |
| Python 代理模型服务 | `POST http://127.0.0.1:9721/api/surrogate/optimize` | 启动代理模型优化求解。 |
| Python 健康检查 | `GET http://127.0.0.1:9721/health` | 检查代理模型服务是否可用。 |
| SolidWorks Worker | `POST http://127.0.0.1:18080/api/pipe-model` | 根据设计变量生成 CAD 文件。 |
| ANSYS Worker | `POST http://127.0.0.1:18081/api/ansys/import-geometry` | 将 CAD 几何导入 Workbench，并在 `transient_structural` 模式下调用 Mechanical 求解。 |
| ANSYS Worker 健康检查 | `GET http://127.0.0.1:18081/api/ansys/health` | 检查 ANSYS Worker 是否启动、Workbench 路径是否配置、实际解析到的启动文件是否存在。 |

后端默认配置项：

```properties
designtask.surrogate.url=http://127.0.0.1:9721/api/surrogate/optimize
designtask.solidworks.worker-url=http://127.0.0.1:18080/api/pipe-model
designtask.ansys.worker-url=http://127.0.0.1:18081/api/ansys/import-geometry
```

## 本地工具配置要求

### SolidWorks

- Worker 所在机器需要安装 SolidWorks。
- 需要支持 SolidWorks COM 自动化。
- 通过 `solidworks_worker/start_pipe_worker.bat` 启动本地服务。
- 正常情况下应生成以下文件：
  - `pipe_native.SLDPRT`
  - `pipe_model.step`
  - `pipe_model.x_t`
  - `pipe.stl`
  - `pipe_preview.png`

### ANSYS Workbench

启动 ANSYS Worker 前，需要将 `ANSYS_WORKBENCH_CMD` 配置为本机 Workbench 官方启动脚本 `runwb2.bat` 的完整路径。

示例：

```bat
set "ANSYS_WORKBENCH_CMD=D:\Program Files\ANSYS Inc\v221\Framework\bin\Win64\runwb2.bat"
```

必须配置真实文件路径，不能配置 Windows 开始菜单中的 `.lnk` 快捷方式。`runwb2.bat` 会先设置 ANSYS 运行环境再启动 Workbench；如果配置为同目录的 `RunWB2.exe`，ANSYS Worker 会自动切换到旁边的 `runwb2.bat`，避免 Workbench 批处理启动即退出。

如果返回 `Ansys.Utilities.Registry.RegistryException`，表示 Workbench 在执行 Journal 前的框架初始化阶段失败，还没有开始导入几何或求解。此时应先关闭所有 ANSYS/Mechanical 进程，用 `runwb2.bat` 重新启动 Worker；如果仍失败，需要重启 Windows 或修复 ANSYS Workbench 用户配置/安装 registry。

然后启动：

```bat
ansys_worker\start_ansys_worker.bat
```

## 数据和文件约定

### CAD 输出约定

SolidWorks Worker 返回的关键字段：

| 字段 | 含义 |
| --- | --- |
| `sldprtPath` | SolidWorks 原生零件文件路径。ANSYS 导入时第二优先使用。 |
| `stepPath` | STEP 文件路径，用于外部 CAD/CAE 软件导入。ANSYS 导入时第一优先使用。 |
| `parasolidPath` | Parasolid 文件路径。当前环境中该格式导入 SpaceClaim 不稳定，作为第三优先级兜底。 |
| `stlPath` | STL 文件路径，用于预览或几何调试。 |
| `centerlineCsvPath` | 管道中心线 CSV 路径。ANSYS Worker 用它结合内径识别内壁压力面。 |
| `previewPngPath` | CAD 预览图路径。 |

CAD Worker 现在要求管道截面同时包含外径和内径：

```text
pipeDiameter / pipe_outer_diameter_mm      管道外径，单位 mm
pipeInnerDiameter / pipe_inner_diameter_mm 管道内径，单位 mm
```

SolidWorks 生成的 `pipe_native.SLDPRT`、`pipe_model.step` 和 `pipe_model.x_t` 必须是真实空心管。Worker 会先扫掠外径，再抽壳或切除内孔；如果内孔无法生成，任务应失败，不能把实心模型交给 ANSYS。

### ANSYS 几何导入约定

后端选择几何文件的顺序为：

1. `stepPath`
2. `sldprtPath`
3. `parasolidPath`
4. `stlPath`

其中 STEP 是当前平台优先使用的中性几何格式；`SLDPRT` 是已在 SolidWorks 中验证可打开的原生兜底格式；Parasolid 在当前环境中可能出现 SpaceClaim 无法编辑的问题，因此不再作为第一优先级。

当前平台正式支持 `DEMO_SIMULATION_MODEL` 和 `BIDIRECTIONAL_FSI_MODEL` 两个 `simulationMode`。其中 `DEMO_SIMULATION_MODEL` 会执行 Mechanical 等效静力结构求解，在入口峰值压力下生成应力云图；旧值 `static_structural` 会兼容映射到该演示模型。静力模式优先尝试 `Static Structural`、`静态结构` 模板，并优先使用 `Solver=ANSYS` 查找，以兼容英文/中文 Workbench。脚本使用默认材料和自动网格，以几何 X 方向两端端面作为固定约束，并结合 `pipe_centerline.csv`、管道外径和管道内径识别空心管内壁面施加入口内压。入口载荷优先读取平台传入的故障管段参数：`INLET_PRESSURE_INITIAL`、`INLET_PRESSURE_PEAK`、`INLET_PRESSURE_RISE_TIME`、`INLET_PRESSURE_EXPRESSION`；演示模型使用 `INLET_PRESSURE_PEAK`。默认网格尺寸为 `3 mm`，可通过 `ANSYS_MESH_SIZE_MM` 覆盖。如果内壁面无法识别，ANSYS Worker 会失败退出，不再退回端面载荷，避免得到不符合工程实际的结果。

ANSYS 2022 R1 中部分 Workbench 容器对象不支持 `.Update()` 方法，Worker 已采用安全更新逻辑：支持则调用，不支持则跳过，并通过 `workbench_steps.txt` 记录执行进度。

Workbench 向 Mechanical 发送正式脚本前，会先发送最小握手命令；生成 `mechanical_command_ready.txt` 表示 Workbench 到 Mechanical 的命令通道已打通。握手成功后再通过 `execfile(...)` 执行 `mechanical_setup.py`，并等待 `mechanical_started.txt` 确认正式脚本已经开始执行。脚本运行中会写入 `mechanical_trace.txt`，用于定位当前处于 `MESHING`、`SOLVING`、`SOLVED` 等阶段；最终结果写入 `mechanical_result.json`。

当前默认使用交互方式打开 Mechanical（`ANSYS_MECHANICAL_INTERACTIVE=1`），便于确认 Mechanical 是否真正启动。若需要后台模式，可将该变量设为 `0`，但不同 ANSYS 版本对后台 `SendCommand` 的支持可能不一致。可通过 `ANSYS_MECHANICAL_HANDSHAKE_RETRIES` 和 `ANSYS_MECHANICAL_HANDSHAKE_WAIT` 调整命令通道握手重试次数和单次等待时间。

ANSYS Worker 会将云图导出为任务目录下的 `equivalent_stress.png`。Java 后端不直接把本机磁盘路径交给浏览器展示，而是通过 `GET /designtask/task/{taskId}/ansys-simulation/image` 返回图片文件；前端使用带登录凭证的接口请求图片并生成临时预览地址。

云图导出默认使用高分辨率 `1920 x 1080`，可通过 `ANSYS_IMAGE_EXPORT_WIDTH` 和 `ANSYS_IMAGE_EXPORT_HEIGHT` 调整。前端页面提供原图预览，避免缩放后图例和最大/最小值标注过小。

由于 ANSYS 2022 R1 对中文最大/最小值 callout 的图形导出字体支持不稳定，ANSYS Worker 会在 PNG 导出后使用 Windows 中文字体重新绘制“最大应力点 / 最小应力点”标注，避免平台端展示乱码或缺字。

Mechanical 求解阶段使用 `analysis.Solve(True)` 触发分析系统求解，并在结果 JSON 中记录 `solutionStatus`、`stressStatus`、`deformationStatus`、`warnings`、`maxEquivalentStressValue` 和 `maxTotalDeformationValue`。如果入口压力非 0 但等效应力和总变形仍同时为 0，Worker 会将该次结果标记为失败，防止平台把无效结果显示为成功。
Worker 还会基于内压薄壁管公式计算名义环向应力、名义轴向应力和名义 Von-Mises 应力，并结合材料屈服强度、拉伸极限强度和中心线长度写入 `engineeringStatus`、`engineeringWarnings`、`engineeringEstimates`。若最大应力超过名义应力 10 倍、超过材料拉伸极限强度，或最大变形超过管中心线长度 20%，本次结果会标记为失败，需要复核载荷面、边界条件、网格或改用非线性模型。

请求示例：

```json
{
  "taskId": 27,
  "geometry": {
    "geometryPath": "C:/path/to/pipe_model.x_t",
    "geometryType": "PARASOLID"
  }
}
```

成功返回时需要关注的字段：

| 字段 | 含义 |
| --- | --- |
| `status` | `SUCCESS` 表示导入完成。 |
| `projectPath` | 生成的 Workbench 项目文件路径。 |
| `resultFilePath` | 主要结果文件路径。 |
| `workDir` | Worker 输出目录。 |
| `metrics` | 导入状态和基础文件信息。 |

## 数据库整合检查项

与其他团队整合时，需要确认是否需要新增或调整以下内容：

- 外部系统任务 ID、追踪 ID。
- 来源系统名称和版本。
- 输入文件路径、文件类型、校验值、上传用户。
- 输出文件路径、结果状态、错误信息。
- 仿真指标、单位、结果图片。
- 审批状态和最终归档快照。

当前 CAD/ANSYS 相关升级脚本：

```text
sql/t2_cad_ansys_geometry_columns.sql
```

## 权限和菜单检查项

新增或移除模块时，需要检查：

- 前端路由和侧边栏菜单。
- 后端权限标识和控制器接口。
- 菜单、角色、按钮权限的 SQL 初始化数据。
- 老菜单是隐藏、删除，还是为了兼容继续保留。

当前设计流程中已经移除了“标准资源管理”菜单。如果其他团队仍依赖该入口，需要先定义替代入口，再考虑恢复。

## 对外移交检查清单

平台交给其他团队前，需要准备：

- 服务启动顺序。
- 端口和防火墙要求。
- 环境变量和本地软件安装路径。
- 数据库初始化脚本和升级脚本。
- API 请求与响应示例。
- 文件交换目录约定。
- 任务状态流转说明。
- 已知限制和常见失败提示。
- 测试数据和一条完整演示任务。

## 每次任务结束前检查

以后每次任务收尾前，需要检查：

1. 是否修改了接口、字段名或返回结构？
2. 是否新增或修改了数据库表、字段或脚本？
3. 是否修改了外部文件格式或生成路径？
4. 是否修改了启动步骤、端口或环境变量？
5. 是否影响了角色、菜单或权限？
6. 是否新增了对其他团队系统的依赖？
7. 是否修改了排查问题的方法或已知限制？

如果以上任意一项为“是”，就需要更新本文档。

## Project22 框梁裂纹扩展与寿命预测

本次新增框梁裂纹扩展与寿命预测能力，需与现有液压弯管抗冲击优化任务保持隔离。详细文件清单见根目录 `project22readme.md`。

新增任务类型：

```text
FRAME_BEAM_CRACK_LIFE_PREDICTION
```

新增 Python 代理模型服务：

```text
GET  http://127.0.0.1:9822/health
POST http://127.0.0.1:9822/api/frame-beam-crack/growth-predict
```

后端配置项：

```properties
designtask.frame-beam.surrogate-base-url=http://127.0.0.1:9822
```

新增 Java 后端接口：

```text
GET  /designtask/task/{taskId}/frame-beam-crack
POST /designtask/task/{taskId}/frame-beam-crack
POST /designtask/task/{taskId}/frame-beam-load-spectrum
GET  /designtask/task/{taskId}/frame-beam-life-prediction
POST /designtask/task/{taskId}/frame-beam-life-prediction
POST /designtask/task/{taskId}/frame-beam-maintenance-advice/confirm
```

新增数据库脚本：

```text
sql/t2_frame_beam_crack_life_schema.sql
sql/t2_frame_beam_crack_life_menu.sql
```

新增前端目录：

```text
ruoyi-ui/src/views/designtask/frameBeam/
```

载荷谱约定：

- 支持 CSV、Excel、JSON 文件记录。
- 核心字段包括 `maxStress`、`minStress`、`cycles`、`currentFlightHours`。
- 应力单位默认 MPa，裂纹长度默认 mm。
# 近期更新：SolidWorks 空心管与 ANSYS 内壁识别

本次修复影响 CAD/ANSYS 文件交换与排错方式：

- SolidWorks Worker 生成管道时，`pipe_native.SLDPRT`、`pipe_model.step`、`pipe_model.x_t` 必须是真正贯通的空心管，不能只生成外实体或一端封闭的模型。
- 内孔切除路径会使用 `wallCutExtension / wall_cut_extension_mm` 向两端延长，实际延长量至少为 `max(配置值, 外径 * 3, 20mm)`，用于避免端部残留封盖。
- 如果 SolidWorks 无法完成抽壳或内孔切除，Worker 必须返回失败，不允许把实心管模型继续传给 ANSYS。
- ANSYS Worker 识别内壁压力面时会同时尝试米和毫米两种尺度，避免 Workbench/Mechanical 返回几何坐标单位与 `pipe_centerline.csv` 不一致导致误判。
- 排查 ANSYS 内壁识别时，优先查看任务目录下的 `mechanical_trace.txt`，其中会输出 `INNER_WALL_UNIT`、`INNER_WALL_TARGET_RADIUS`、`INNER_WALL_TOLERANCE`、`INNER_WALL_FACES` 和 `FACE_INFO`。

补充：如果 SolidWorks 对整条弯曲中心线的圆形扫掠切除在端部留下封盖，Worker 会额外生成 `InnerBoreStartOpen3D` 和 `InnerBoreEndOpen3D` 两条短直线开口切除路径，用同一内径在两端各自打通，确保导出的 STEP/SLDPRT 是双端贯通空心管。

补充：ANSYS 内壁压力面识别时必须排除两端端面。端部环形面虽然属于空心管壁厚实体，但不是内壁圆柱面，不能施加入口内压；`mechanical_trace.txt` 中 `FACE_INFO` 会标记 `endFace=True/False`，只有非端面且中心线距离匹配内半径的面才会作为 `INNER_WALL_FACES`。

补充：ANSYS Worker 的 `mechanical_result.json.status` 只表示 Mechanical 是否完成有效求解。应力超过材料强度、FE 峰值远高于薄壁管名义应力、变形超过管长 5% 等属于工程复核预警，应通过 `engineeringStatus=REVIEW_REQUIRED`、`engineeringWarnings` 和 `engineeringEstimates` 返回给平台展示，不应再触发 HTTP 500 或显示为 `ANSYS Worker unavailable`。

补充：当前 Mechanical 自动求解默认采用两端固定支撑，即几何 X 方向最小端面和最大端面同时作为 `Fixed Support`；`mechanical_trace.txt` 会写入 `FIXED_SUPPORT_MODE=both_ends`、`FIXED_SUPPORT_MIN_X`、`FIXED_SUPPORT_MAX_X` 和 `FIXED_SUPPORT_FACES`。ANSYS Worker 默认设置 `ANSYS_KEEP_MECHANICAL_OPEN=1`，求解完成后保留 Mechanical 窗口，便于人工检查模型树、网格、载荷和结果；如需批处理自动关闭，可设置为 `0`。

# 近期更新：ANSYS 双模型仿真选择

平台仿真确认页现在支持在开始 ANSYS 仿真前选择仿真模型。前端、Java 后端和 ANSYS Worker 统一使用 `simulationMode` 区分结果，避免两个模型互相覆盖。

| 展示名称 | simulationMode | 用途 |
| --- | --- | --- |
| 演示仿真模型 | `DEMO_SIMULATION_MODEL` | 保留当前 Mechanical 静力结构流程，用于平台展示、快速校核和应力云图输出。 |
| 双向流固耦合仿真模型 | `BIDIRECTIONAL_FSI_MODEL` | 按参考模型参数创建 Fluent + Transient Structural + System Coupling 双向耦合工程文件。 |

新增/调整的接口约定：

- `POST /designtask/task/{taskId}/ansys-simulation` 请求体可传入 `simulationMode`。
- `GET /designtask/task/{taskId}/ansys-simulation` 支持 `simulationMode` 查询参数。
- `GET /designtask/task/{taskId}/ansys-simulation/image` 支持 `simulationMode` 查询参数，并通过后端读取本机图片文件返回给浏览器。

数据表 `t2_design_ansys_simulation_task` 新增 `simulation_mode` 字段，并以 `(task_id, simulation_mode)` 作为主键；升级脚本 `sql/t2_ansys_simulation_placeholder.sql` 已同步。历史未区分模式的数据会按 `DEMO_SIMULATION_MODEL` 兼容。

ANSYS Worker 输出目录按模型拆分：

```text
ansys_worker/output/task_{taskId}/demo_simulation_model/
ansys_worker/output/task_{taskId}/bidirectional_fsi_model/
```

当前 `BIDIRECTIONAL_FSI_MODEL` 分支会生成双向流固耦合参考工程、配置 JSON 和 Fluent 设置说明文件，用于与参考 `1.wbpz` 的参数保持一致。若要做到全自动真实双向耦合求解，还需要后续补齐流体域几何、入口/出口/壁面命名边界、Fluent 网格与 System Coupling 数据传递的可执行自动化脚本。

# 近期更新：课题二数据库整合迁移脚本

为便于合并到 `project2-dev` 并交付其他团队部署，已整理课题二业务库完整迁移脚本：

```text
sql/t2_project2_full_migration.sql
```

该脚本面向已有 RuoYi-Cloud 基础库执行，包含课题二表结构、兼容升级字段、初始化数据、优化任务预置数据、故障管段参数、梁裂纹寿命菜单与“标准资源管理”菜单移除。脚本中已包含历史表名兼容重命名逻辑，目标库如存在旧表 `design_*` 或 `p2_*`，会先迁移为 `t2_*` 命名。

注意事项：

- 执行前请先备份目标库。
- 该脚本不包含 `flowable相关表.sql`，因为其中包含 Flowable 引擎表重建和 `DROP TABLE`，只应在干净 Flowable 库或明确需要重建流程引擎表时单独执行。
- `flowable网关路由.sql`、`nacos配置.sql` 属于网关/Nacos 配置库脚本，不建议混入课题二业务库迁移脚本，应按部署环境单独确认后执行。
