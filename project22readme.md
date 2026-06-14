# Project22 框梁裂纹扩展与寿命预测集成说明

本文档记录框梁裂纹扩展与寿命预测能力的新增内容，便于后续合成项目时按需集成。该能力与现有液压弯管抗冲击优化任务保持隔离。

## 1. 功能边界

- 新增任务类型：`FRAME_BEAM_CRACK_LIFE_PREDICTION`
- 框梁任务使用独立 API、数据库表、前端组件目录和 Python 服务。
- 液压任务继续使用现有 `9721` 代理模型服务和 `t2_surrogate_solve_task` 等既有表。
- 框梁任务默认调用 `9822` 服务，不复用液压任务请求体或返回体。

## 2. 新增菜单与权限

新增菜单脚本：

```text
sql/t2_frame_beam_crack_life_menu.sql
```

建议菜单结构：

```text
设计制造协同优化平台
├─ 任务协同
│  ├─ 协同机制生成
│  └─ 目标约束选择
├─ 模型分析
│  ├─ 模型解耦求解
│  └─ 寿命预测评估
├─ 验证与决策
│  ├─ 仿真验证确认
│  └─ 决策建议确认
└─ 任务归档文档
```

新增权限标识：

```text
designtask:framebeam:input
designtask:framebeam:predict
designtask:framebeam:advice
designtask:framebeam:approve
```

## 3. 新增 Java 后端 API

接口前缀仍为 `/designtask`，但接口路径带 `frame-beam`，与液压接口区分：

```text
GET  /designtask/task/{taskId}/frame-beam-crack
POST /designtask/task/{taskId}/frame-beam-crack
POST /designtask/task/{taskId}/frame-beam-load-spectrum
GET  /designtask/task/{taskId}/frame-beam-life-prediction
POST /designtask/task/{taskId}/frame-beam-life-prediction
POST /designtask/task/{taskId}/frame-beam-maintenance-advice/confirm
```

新增 Java 文件：

```text
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/controller/FrameBeamCrackController.java
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/service/FrameBeamCrackService.java
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/service/FrameBeamPredictionClient.java
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/service/FrameBeamMaintenanceAdviceService.java
```

公共后端改动：

```text
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/controller/DesignTaskController.java
```

该文件仅新增任务类型 `FRAME_BEAM_CRACK_LIFE_PREDICTION`。

## 4. 新增 Python 服务

新增目录：

```text
python/project22
```

默认端口：

```text
9822
```

接口：

```text
GET  http://127.0.0.1:9822/health
POST http://127.0.0.1:9822/api/frame-beam-crack/growth-predict
```

后端配置项：

```properties
designtask.frame-beam.surrogate-base-url=http://127.0.0.1:9822
```

启动方式：

```bash
cd python/project22
pip install -r requirements.txt
uvicorn app.main:app --host 127.0.0.1 --port 9822
```

## 5. 新增数据库

新增脚本：

```text
sql/t2_frame_beam_crack_life_schema.sql
```

新增表：

```text
t2_frame_beam_crack_input
t2_frame_beam_load_spectrum
t2_frame_beam_life_prediction
t2_frame_beam_maintenance_advice
```

这些表只保存框梁任务数据，不写入液压任务求解结果表。

## 6. 新增前端文件

新增独立目录：

```text
ruoyi-ui/src/views/designtask/frameBeam/
```

新增组件：

```text
CrackInputPanel.vue
LoadSpectrumPanel.vue
LifePredictionPanel.vue
MaintenanceAdvicePanel.vue
life-prediction.vue
decision-advice.vue
```

公共前端改动：

```text
ruoyi-ui/src/api/designtask/optimization.js
ruoyi-ui/src/router/index.js
```

公共文件只新增框梁 API 封装和两个页面路由。

## 7. Gateway / System / Auth / Common 改动

Gateway：

- 仍复用现有 `/designtask/**` 路由。
- 如环境中未配置 `ruoyi-designtask1` 路由，需要按现有 designtask 路由方式补齐。
- Python `9822` 服务默认由 Java 后端调用，前端不直接访问。

System：

- 新增菜单和权限见 `sql/t2_frame_beam_crack_life_menu.sql`。
- 需要给结构工程师、制造/维修工程师、专家或管理员角色分配新增权限。

Auth：

- 新增权限码后，登录用户需要重新登录或刷新权限缓存。
- 若系统启用按钮级鉴权，需要同步配置 `designtask:framebeam:*` 权限。

Common：

- 当前未新增公共 Java 枚举或公共工具类。
- 当前未修改公共文件上传服务。载荷谱文件路径先记录在框梁专属表中。

## 8. 载荷谱文件约定

第一版支持页面录入载荷谱摘要，也允许记录已上传文件名和文件路径。

建议文件格式：

```text
CSV
Excel
JSON
```

建议字段：

```text
spectrumName
loadSpectrumLevel
maxStress
minStress
cycles
currentFlightHours
fileName
filePath
```

建议单位：

```text
应力：MPa
裂纹长度：mm
循环次数：cycles
时间：flight hours
```

## 9. 后续合成项目集成建议

必须集成，框梁任务才能运行：

```text
FrameBeamCrackController.java
FrameBeamCrackService.java
FrameBeamPredictionClient.java
FrameBeamMaintenanceAdviceService.java
python/project22
sql/t2_frame_beam_crack_life_schema.sql
ruoyi-ui/src/views/designtask/frameBeam/
```

需要按项目情况选择集成：

```text
sql/t2_frame_beam_crack_life_menu.sql
ruoyi-ui/src/router/index.js
ruoyi-ui/src/api/designtask/optimization.js
DesignTaskController.java 中的任务类型新增
```

如果合成项目暂不集成框梁任务：

- 不执行 `t2_frame_beam_crack_life_schema.sql`
- 不执行 `t2_frame_beam_crack_life_menu.sql`
- 不启动 `python/project22`
- 不合入 `frameBeam` 前端目录
- 保留液压任务原有 `9721` 服务和页面即可
