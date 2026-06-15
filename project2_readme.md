# 课题二合并到 master 整合手册

本文档用于把 `project2-dev` 分支中的课题二能力合并到 `master`。整合人员应按本文核对文件、公共模块、数据库脚本、服务配置、外部软件路径和验收流程。

## 1. 推荐合并方式

优先使用 Git 合并，保留文件增删改和冲突上下文：

```powershell
git fetch origin
git checkout master
git pull origin master
git merge origin/project2-dev
```

如出现冲突，逐个解决后执行：

```powershell
git add <已解决冲突的文件>
git commit
git push origin master
```

如果团队要求手工整合，按下面章节逐项复制和修改。

## 2. 必须合入的课题二文件

### 2.1 Java 业务模块

合入整个模块：

```text
ruoyi-modules/ruoyi-designtask1/
```

重点文件：

```text
ruoyi-modules/ruoyi-designtask1/pom.xml
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/RuoYiDesigntask1Application.java
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/controller/DesignOptimizationController.java
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/controller/DesignTaskController.java
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/controller/FrameBeamCrackController.java
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/service/DesignOptimizationService.java
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/service/FrameBeamCrackService.java
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/service/FrameBeamMaintenanceAdviceService.java
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/service/FrameBeamPredictionClient.java
```

这些文件包含：任务流转、目标/约束、目标权重归口、代理模型求解、CAD 建模、ANSYS 仿真、人工验证结论、归档、框梁裂纹寿命预测等逻辑。

### 2.2 前端页面与 API

合入：

```text
ruoyi-ui/src/api/designtask/
ruoyi-ui/src/views/designtask/
ruoyi-ui/src/views/project_2/
```

重点文件：

```text
ruoyi-ui/src/api/designtask/optimization.js
ruoyi-ui/src/api/designtask/task.js
ruoyi-ui/src/views/designtask/platform-theme.scss
ruoyi-ui/src/views/designtask/dashboard/index.vue
ruoyi-ui/src/views/designtask/task/create.vue
ruoyi-ui/src/views/designtask/mechanism/index.vue
ruoyi-ui/src/views/designtask/objective/index.vue
ruoyi-ui/src/views/designtask/solve/index.vue
ruoyi-ui/src/views/designtask/simulation/index.vue
ruoyi-ui/src/views/designtask/simulation/CadStlViewer.vue
ruoyi-ui/src/views/designtask/archive/index.vue
ruoyi-ui/src/views/designtask/frameBeam/life-prediction.vue
ruoyi-ui/src/views/designtask/frameBeam/decision-advice.vue
```

前端近期关键变化：

- 协同机制生成页新增“管段编号”选择，发起任务时绑定故障管段参数快照。
- 目标约束选择页只负责各专业选择目标/约束，不再让各专业填写目标权重。
- 模型解耦求解页由负责人统一归口全部目标权重，权重范围为 `0-10`。
- 目标优化类型统一展示为 `min` / `max`。
- 目标约束校验不再进入页面后默认通过，必须点击校验后动态显示结果。
- 仿真验证页指标对比使用代理模型、CAD、ANSYS 的真实数据，不再使用假数据。
- 验证结论改为工程师手动选择“通过/不通过”，不再默认显示通过。
- 课题二主要页面统一为与首页仪表盘一致的工业软件风格，并补充按钮小图标。

如 `master` 已有 `ruoyi-ui/src/router/index.js`，不要直接覆盖，应手工合并课题二路由，避免影响其他课题。

### 2.3 Python 服务

合入：

```text
python/project2/
python/project22/
```

说明：

- `python/project2/`：液压弯管代理模型优化服务，默认端口 `9721`。
- `python/project22/`：框梁裂纹寿命预测服务，默认端口 `9822`。

正式部署建议为两个服务分别建立虚拟环境。不要把本机临时缓存、运行日志或虚拟环境目录提交到 master。

### 2.4 SolidWorks 与 ANSYS Worker

合入：

```text
solidworks_pipe/
solidworks_worker/
ansys_worker/
```

只提交源码、脚本和说明，不提交运行输出：

```text
solidworks_pipe/README.md
solidworks_pipe/create_pipe_native.vbs
solidworks_pipe/generate_pipe.py
solidworks_pipe/open_in_solidworks.vbs
solidworks_worker/README.md
solidworks_worker/pipe_worker.py
solidworks_worker/start_pipe_worker.bat
ansys_worker/README.md
ansys_worker/ansys_import_worker.py
ansys_worker/start_ansys_worker.bat
```

不要提交：

```text
ansys_worker/output/
solidworks_worker/output/
solidworks_pipe/params.json
tmp_wbpz_compare_1/
```

### 2.5 SQL 和整合文档

必须合入：

```text
sql/t2_project2_full_migration.sql
TEAM_INTEGRATION_README.md
project2_readme.md
project22readme.md
PROJECT2_PENDING_COMMIT_FILES.md
```

其他 `sql/t2_*.sql` 可以作为拆分来源或局部补丁参考保留在仓库中，但正式迁移数据库只执行：

```text
sql/t2_project2_full_migration.sql
```

不要把 `flowable相关表.sql`、`flowable网关路由.sql`、`nacos配置.sql` 当作课题二业务库迁移脚本直接执行。

## 3. master 公共模块需要合并的内容

### 3.1 根 POM

文件：

```text
pom.xml
```

需要核对 `project2-dev` 中新增或调整过的公共依赖版本，例如：

```text
spring-boot.version
spring-cloud.version
spring-cloud-alibaba.version
spring-boot-admin.version
mybatis-spring-boot.version
springdoc.version
flowable.version
hutool.version
thumbnailator.version
tika.version
aviator.version
easyexcel.version
mybatis-plus-boot.version
jsqlparser.version
spring-integration.version
```

如果 `master` 已经升级到不同版本，不要机械覆盖，需以全仓库能编译和启动为准。

### 3.2 ruoyi-modules 聚合 POM

文件：

```text
ruoyi-modules/pom.xml
```

增加：

```xml
<module>ruoyi-designtask1</module>
```

### 3.3 网关与认证模块

涉及：

```text
ruoyi-gateway/pom.xml
ruoyi-gateway/src/main/java/com/ruoyi/gateway/RuoYiGatewayApplication.java
ruoyi-gateway/src/main/resources/bootstrap.yml
ruoyi-auth/pom.xml
ruoyi-auth/src/main/java/com/ruoyi/auth/RuoYiAuthApplication.java
ruoyi-auth/src/main/resources/bootstrap.yml
```

整合重点：

- `DataSourceAutoConfiguration` 的 import 包路径要与 `master` 使用的 Spring Boot 版本匹配。
- 如果 `project2-dev` 中增加了 `scanBasePackages`，需确认 Feign、system api、fallback 能正常扫描。
- `bootstrap.yml` 中不要保留本机 `127.0.0.1` 固定注册 IP；服务器部署时应改为服务器内网 IP，或删除固定 IP 让 Nacos 自动识别。

### 3.4 前端公共入口

涉及：

```text
ruoyi-ui/package.json
ruoyi-ui/src/main.js
ruoyi-ui/src/router/index.js
```

整合重点：

- 合入课题二新增依赖后执行 `npm install`。
- `router/index.js` 合并课题二路由，但不能覆盖其他课题路由。
- 若 `main.js` 注册了流程设计器、全局样式或组件，需要与 master 当前内容合并。

### 3.5 Flowable 前端组件

如果 `master` 没有流程设计器相关前端文件，需要合入：

```text
ruoyi-ui/src/api/workflow/
ruoyi-ui/src/components/ProcessDesigner/
ruoyi-ui/src/components/ProcessViewer/
ruoyi-ui/src/modules/
ruoyi-ui/src/package/
ruoyi-ui/src/utils/min-dash.js
ruoyi-ui/src/views/workflow/
```

如果 `master` 已有自己的 Flowable 前端实现，需要逐项对比，不要直接覆盖。

## 4. 新增和需要保留的 Java API

以下接口由 `ruoyi-designtask1` 提供，前端通过网关访问 `/designtask/**`。

### 4.1 任务与流程

```text
GET    /designtask/task/list
GET    /designtask/task/{taskId}
POST   /designtask/task
PUT    /designtask/task
DELETE /designtask/task/{taskIds}
POST   /designtask/task/{taskId}/submit
GET    /designtask/task/{taskId}/logs
GET    /designtask/task/types
PUT    /designtask/task/{taskId}/cancel
PUT    /designtask/task/node/{nodeId}/complete
GET    /designtask/flow/template/list
GET    /designtask/flow/node/list/{templateId}
GET    /designtask/flow/task/node/list/{taskId}
POST   /designtask/flow/task/node/{nodeId}/submit
```

### 4.2 设计任务与优化流程

```text
GET  /designtask/dashboard
GET  /designtask/process/definitions
POST /designtask/process/deploy-default
GET  /designtask/process/definition/{processDefinitionId}/nodes
GET  /designtask/assignee-options
POST /designtask/task/start
POST /designtask/task/upload-attachment
GET  /designtask/task/attachment/{fileId}
GET  /designtask/task/detail/{taskId}
GET  /designtask/task/{taskId}/archive
GET  /designtask/objective/catalog/{discipline}
GET  /designtask/design-variable/catalog/{discipline}
GET  /designtask/fault-pipe-parameters/default
GET  /designtask/fault-pipe-parameters/options
GET  /designtask/task/{taskId}/fault-pipe-parameters
POST /designtask/task/{taskId}/objective-constraints
POST /designtask/task/{taskId}/objective-weights
POST /designtask/task/{taskId}/design-variables
POST /designtask/task/{taskId}/conflict-check
POST /designtask/task/{taskId}/decompose
POST /designtask/task/{taskId}/solve
POST /designtask/task/{taskId}/surrogate-solve
GET  /designtask/task/{taskId}/surrogate-solve
POST /designtask/task/{taskId}/surrogate-solve/confirm
POST /designtask/task/{taskId}/simulation
POST /designtask/task/{taskId}/approve
```

`POST /designtask/task/start` 必须传入本次任务选择的管段参数集：

```json
{
  "faultPipeParameterSetId": 1
}
```

后端会将可复用参数集复制为任务快照，后续 CAD、ANSYS、指标对比均读取该任务绑定参数。

`POST /designtask/task/{taskId}/objective-weights` 用于负责人在模型解耦求解页统一保存目标权重：

```json
{
  "items": [
    { "discipline": "hydraulic", "itemCode": "HYD_STRESS_MIN", "weight": 6 },
    { "discipline": "hydraulic", "itemCode": "HYD_DEFORMATION_MIN", "weight": 4 }
  ]
}
```

权重范围为 `0-10`。后端兼容旧的 `0-100` 数据，读取时会换算展示。

`POST /designtask/task/{taskId}/simulation` 现在必须包含人工验证结论：

```json
{
  "simulationPassed": true
}
```

如果缺少 `simulationPassed`，后端应拒绝提交，避免刷新指标时误完成流程。任务详情接口会返回 `simulation.verified`、`simulation.passed` 和 `canConfirmSimulation`。

### 4.3 CAD 与 ANSYS

```text
POST /designtask/task/{taskId}/cad-model
GET  /designtask/task/{taskId}/cad-model
GET  /designtask/task/{taskId}/cad-model/file/{kind}
POST /designtask/task/{taskId}/ansys-simulation
GET  /designtask/task/{taskId}/ansys-simulation
GET  /designtask/task/{taskId}/ansys-simulation/image
```

ANSYS 接口支持 `simulationMode`：

```text
DEMO_SIMULATION_MODEL
BIDIRECTIONAL_FSI_MODEL
```

图片接口通过后端读取本机图片文件返回给浏览器，不直接暴露服务器磁盘路径。

### 4.4 框梁裂纹寿命

```text
GET  /designtask/task/{taskId}/frame-beam-crack
POST /designtask/task/{taskId}/frame-beam-crack
POST /designtask/task/{taskId}/frame-beam-load-spectrum
GET  /designtask/task/{taskId}/frame-beam-life-prediction
POST /designtask/task/{taskId}/frame-beam-life-prediction
POST /designtask/task/{taskId}/frame-beam-maintenance-advice/confirm
```

### 4.5 标准资源菜单

代码中可以保留资源接口，但当前业务不需要“标准资源管理”菜单。整合脚本会移除菜单入口和对应菜单权限，不删除资源表。

## 5. Python 和 Worker 服务配置

### 5.1 代理模型服务

目录：

```text
python/project2/
```

接口：

```text
GET  /health
GET  /api/models/active
POST /api/surrogate/optimize
```

默认端口：

```text
9721
```

本机可使用已有 conda 环境启动：

```powershell
cd python/project2
conda activate project2-fastapi
python -m uvicorn app.main:app --host 127.0.0.1 --port 9721
```

如果服务器没有该环境，需要创建新环境并安装依赖：

```powershell
cd python/project2
pip install -r requirements.txt
python -m uvicorn app.main:app --host 0.0.0.0 --port 9721
```

Java 配置：

```properties
design.solver.surrogate-base-url=http://<代理模型服务器IP>:9721
```

健康检查：

```text
http://<代理模型服务器IP>:9721/health
```

### 5.2 框梁裂纹寿命服务

目录：

```text
python/project22/
```

接口：

```text
GET  /health
POST /api/frame-beam-crack/growth-predict
```

默认端口：

```text
9822
```

启动：

```powershell
cd python/project22
pip install -r requirements.txt
python -m uvicorn app.main:app --host 0.0.0.0 --port 9822
```

Java 配置：

```properties
designtask.frame-beam.surrogate-base-url=http://<框梁服务IP>:9822
```

### 5.3 SolidWorks Worker

目录：

```text
solidworks_worker/
```

接口：

```text
POST /api/pipe-model
```

默认地址：

```text
http://127.0.0.1:18080/api/pipe-model
```

Java 配置：

```properties
designtask.pipe-worker-base-url=http://<SolidWorks服务器IP>:18080
```

服务器要求：

- Windows 环境。
- 已安装 SolidWorks，许可证可用。
- 启动 Worker 的用户能正常打开 SolidWorks。
- 不建议作为无桌面系统服务启动，推荐使用可交互桌面用户。

SolidWorks Worker 必须生成双端贯通空心管，输出的 `pipe_native.SLDPRT`、`pipe_model.step`、`pipe_model.x_t` 不允许是一端封闭或实心模型。

### 5.4 ANSYS Worker

目录：

```text
ansys_worker/
```

接口：

```text
GET  /api/ansys/health
POST /api/ansys/import-geometry
```

默认地址：

```text
http://127.0.0.1:18081/api/ansys/import-geometry
```

Java 配置：

```properties
designtask.ansys-worker-base-url=http://<ANSYS服务器IP>:18081
```

关键环境变量：

```bat
set "ANSYS_WORKBENCH_CMD=C:\Program Files\ANSYS Inc\v221\Framework\bin\Win64\runwb2.bat"
set "ANSYS_WORKER_HOST=0.0.0.0"
set "ANSYS_WORKER_PORT=18081"
set "ANSYS_MESH_SIZE_MM=3"
set "ANSYS_KEEP_MECHANICAL_OPEN=0"
set "ANSYS_MECHANICAL_INTERACTIVE=0"
set "ANSYS_MECHANICAL_RESULT_TIMEOUT=540"
set "ANSYS_IMAGE_EXPORT_WIDTH=1920"
set "ANSYS_IMAGE_EXPORT_HEIGHT=1080"
```

注意：

- `ANSYS_WORKBENCH_CMD` 必须改成服务器真实路径。
- 不要配置开始菜单 `.lnk` 快捷方式。
- 推荐配置 `runwb2.bat`，不要优先配置 `RunWB2.exe`。
- 版本目录如 `v221` 要按服务器安装版本调整。

## 6. 数据库迁移

正式迁移课题二业务库时只执行：

```text
sql/t2_project2_full_migration.sql
```

执行示例：

```powershell
mysql -h <数据库地址> -P <端口> -u <用户名> -p <数据库名> < sql/t2_project2_full_migration.sql
```

该整合脚本包含：

- 课题二 `t2_*` 表结构。
- 旧表 `design_*` / `p2_*` 到 `t2_*` 的兼容重命名。
- 兼容升级字段。
- 菜单、角色权限、初始化数据。
- 故障管段参数集。
- 管段编号标准化：`HP-PIPE-SEG-001`。
- 优化前设计变量基准值回填，包括已有任务快照。
- ANSYS 双模型结果表升级，`t2_design_ansys_simulation_task` 使用 `(task_id, simulation_mode)` 区分结果。
- 框梁裂纹寿命表和菜单。
- 移除“标准资源管理”菜单。

不要在同一个目标库中重复执行其他 `sql/t2_*.sql` 零散脚本。`sql/t2_fault_pipe_segment_code_update.sql`、`sql/t2_fault_pipe_design_variable_baseline.sql` 等已经并入完整迁移脚本，只在局部补丁场景下单独使用。

特殊脚本说明：

- `flowable相关表.sql`：包含 Flowable 引擎表和 `DROP TABLE`，只在干净 Flowable 库或明确需要重建 Flowable 引擎表时单独执行。
- `flowable网关路由.sql`：如果 master 的网关路由由数据库/Nacos 管理，需要按目标环境单独合入。
- `nacos配置.sql`：属于 Nacos 配置库，不能直接当业务库脚本执行。

目标库要求：

- 已有 RuoYi-Cloud 基础表，例如 `sys_menu`、`sys_role`、`sys_role_menu`。
- 已有基础角色数据，否则菜单授权语句无法产生预期效果。
- 执行前必须备份。

## 7. Nacos、网关和服务器地址

### 7.1 ruoyi-designtask1 配置

服务名和端口建议：

```yaml
server:
  port: 9801
spring:
  application:
    name: ruoyi-designtask1
```

Nacos 中需要新增或合并：

```text
ruoyi-designtask1-dev.yml
```

至少包含：

```yaml
spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://<数据库IP>:<端口>/<数据库名>
          username: <用户名>
          password: <密码>

design:
  solver:
    surrogate-base-url: http://<代理模型服务IP>:9721

designtask:
  frame-beam:
    surrogate-base-url: http://<框梁服务IP>:9822
  pipe-worker-base-url: http://<SolidWorks服务器IP>:18080
  ansys-worker-base-url: http://<ANSYS服务器IP>:18081
```

具体配置键以 Java 代码读取为准，整合时搜索：

```text
surrogate-base-url
pipe-worker
ansys-worker
```

### 7.2 网关路由

网关需要能转发：

```text
Path=/designtask/**
Service=ruoyi-designtask1
```

如果保留旧接口别名 `/task/**`、`/flow/**`、`/template/**`，也要确认是否转发到 `ruoyi-designtask1`。前端主要使用 `/designtask/**`。

### 7.3 固定 IP

以下配置不能在服务器上保留本机值：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        ip: 127.0.0.1
```

部署到服务器后，应改为服务器内网 IP，或删除固定 IP 让 Nacos 自动识别。

## 8. 合并后启动顺序

推荐顺序：

1. 备份目标数据库。
2. 执行 `sql/t2_project2_full_migration.sql`。
3. 合并 Nacos 配置和网关路由。
4. 启动 Nacos、Redis、MySQL。
5. 启动 RuoYi 基础服务：gateway、auth、system。
6. 启动 `ruoyi-designtask1`。
7. 启动 `python/project2` 代理模型服务。
8. 启动 `python/project22` 框梁裂纹寿命服务。
9. 启动 `solidworks_worker`。
10. 启动 `ansys_worker`。
11. 启动或重新部署前端。

编译检查：

```powershell
mvn clean package -DskipTests
```

前端检查：

```powershell
cd ruoyi-ui
npm install
npm run build:prod
```

## 9. 合并验收清单

### 9.1 文件检查

master 中应存在：

```text
ruoyi-modules/ruoyi-designtask1/
ruoyi-ui/src/views/designtask/
ruoyi-ui/src/api/designtask/
python/project2/
python/project22/
solidworks_pipe/
solidworks_worker/
ansys_worker/
sql/t2_project2_full_migration.sql
TEAM_INTEGRATION_README.md
project2_readme.md
```

master 中不应包含：

```text
ansys_worker/output/
solidworks_worker/output/
tmp_wbpz_compare_1/
solidworks_pipe/params.json
```

### 9.2 服务健康检查

```text
GET http://<代理模型服务IP>:9721/health
GET http://<框梁服务IP>:9822/health
GET http://<ANSYS服务器IP>:18081/api/ansys/health
```

SolidWorks Worker 通过平台生成 CAD 模型验证。

### 9.3 业务链路验收

至少跑通：

```text
创建课题二任务
选择管段编号
各专业选择目标/约束
负责人执行目标约束校验
负责人归口设置目标权重
执行代理模型求解
生成 SolidWorks CAD
启动 ANSYS 演示仿真模型
查看应力图和真实结果指标
工程师手动提交仿真验证通过/不通过
领导审批与任务归档
```

框梁裂纹链路单独验证：

```text
录入裂纹参数
上传或选择载荷谱
调用 9822 寿命预测服务
生成维修/决策建议
确认建议
```

### 9.4 页面验收

重点检查：

- 首页、协同机制、目标选择、模型解耦、仿真验证、归档页面风格统一。
- 协同机制生成页有管段编号选择。
- 目标约束选择页不再展示权重滑条。
- 模型解耦页左侧目标、右侧约束分栏展示，权重为 `0-10`。
- 校验结果只在点击执行校验后出现。
- 仿真验证页只显示设计变量对比、最大等效应力、最大总变形。
- 验证结论必须人工选择后才能提交。
- 管理员或当前可处理人可提交仿真结论，历史任务补录不会重复推动流程。

## 10. 主要风险点

- 根 `pom.xml` 版本变更会影响全项目，合并后必须全仓库编译。
- `ruoyi-ui/src/router/index.js` 容易与其他课题冲突，不能直接覆盖。
- Nacos 中所有 `127.0.0.1` 到服务器后都可能失效，需要逐项替换。
- Python 默认环境不一定安装 FastAPI、NumPy、SciPy 等依赖；代理模型不可用时优先检查 `9721/health` 和虚拟环境。
- SolidWorks 和 ANSYS 依赖 Windows 软件、许可证和可交互桌面环境，不适合直接部署到普通 Linux 服务节点。
- ANSYS `BIDIRECTIONAL_FSI_MODEL` 当前用于生成参考工程和配置；若要无人值守真实双向流固耦合求解，还需继续补齐 Fluent 流体域、命名边界、网格和 System Coupling 自动化脚本。
- 数据库正式迁移只执行 `sql/t2_project2_full_migration.sql`，不要重复执行零散 SQL。
