# 课题二后续 Git 提交文件清单

本文档用于记录当前工作区中后续需要提交到 `project2-dev` 的文件，避免把运行产物或临时文件误提交。

## 1. 本轮功能变更

主题：协同机制生成页增加“管段编号选择”，并在发起任务后将所选管段参数绑定到当前任务；模型解耦页改为执行校验后再展示结果，并由负责人统一归口目标权重；仿真验证页改为使用任务原始管段参数和真实 ANSYS 结果生成指标对比。

补充：仿真验证确认页已优化页面布局，将“返回任务列表”调整为页面级导航，管段原始设计参数与指标对比改为等宽双栏，验证结论改为独立全宽区域；CAD 建模与 ANSYS 仿真区域改为左右等高面板，内容超出时在面板内滚动。

本轮需要提交的文件：

```text
ruoyi-ui/src/views/designtask/mechanism/index.vue
ruoyi-ui/src/views/designtask/dashboard/index.vue
ruoyi-ui/src/views/designtask/solve/index.vue
ruoyi-ui/src/views/designtask/simulation/index.vue
ruoyi-ui/src/views/designtask/objective/index.vue
ruoyi-ui/src/views/designtask/archive/index.vue
ruoyi-ui/src/views/designtask/task/create.vue
ruoyi-ui/src/views/designtask/frameBeam/life-prediction.vue
ruoyi-ui/src/views/designtask/frameBeam/decision-advice.vue
ruoyi-ui/src/views/designtask/platform-theme.scss
ruoyi-ui/src/views/designtask/simulation/CadStlViewer.vue
ruoyi-ui/src/api/designtask/optimization.js
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/controller/DesignOptimizationController.java
ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/service/DesignOptimizationService.java
TEAM_INTEGRATION_README.md
project2_readme.md
PROJECT2_PENDING_COMMIT_FILES.md
sql/t2_project2_master_migration.sql
sql/t2_fault_pipe_segment_code_update.sql
sql/t2_fault_pipe_design_variable_baseline.sql
sql/t2_designtask1_data_init.sql
sql/t2_designtask1_startup_fix.sql
sql/t2_designtask1_startup_fix_gui.sql
sql/t2_fault_pipe_diameter_update.sql
sql/t2_fault_pipe_parameter_init.sql
```

## 2. 本轮新增接口与请求字段

新增接口：

```text
GET /designtask/fault-pipe-parameters/options
```

发起任务接口新增请求字段：

```json
{
  "faultPipeParameterSetId": 1
}
```

后端会把所选可复用管段参数集复制成任务专属参数集，并通过 `task_id` 绑定到新任务。后续任务处置、CAD 生成和 ANSYS 仿真继续调用：

```text
GET /designtask/task/{taskId}/fault-pipe-parameters
```

任务详情接口 `GET /designtask/task/detail/{taskId}` 的返回语义调整：

- `conflictCheck.checked=false` 表示尚未执行目标约束校验。
- `conflictCheck.status=PASSED/FAILED` 表示已执行校验后的真实结果。
- `simulation.metrics` 现在包含预置优化前管道设计变量与优化后结果的对比，并保留最新真实 ANSYS 成功结果中的最大等效应力和最大总变形；不再使用演示假数据，也不展示未变化的输入参数。
- `POST /designtask/task/{taskId}/simulation` 必须由工程师提交 `simulationPassed`，不再根据 ANSYS 状态自动通过。
- `POST /designtask/task/{taskId}/objective-weights` 用于模型解耦求解阶段统一保存所有已选目标的权重，权重范围为 `0-10`，只更新 `t2_design_objective_constraint.weight`，不推动流程。

## 3. 建议执行的提交命令

```powershell
git add -- PROJECT2_PENDING_COMMIT_FILES.md TEAM_INTEGRATION_README.md project2_readme.md

git add -- ruoyi-ui/src/views/designtask/mechanism/index.vue ruoyi-ui/src/views/designtask/solve/index.vue ruoyi-ui/src/views/designtask/simulation/index.vue ruoyi-ui/src/api/designtask/optimization.js

git add -- ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/controller/DesignOptimizationController.java

git add -- ruoyi-modules/ruoyi-designtask1/src/main/java/com/ruoyi/designtask1/service/DesignOptimizationService.java

git add -- sql/t2_project2_master_migration.sql sql/t2_fault_pipe_segment_code_update.sql sql/t2_fault_pipe_design_variable_baseline.sql

git add -- sql/t2_designtask1_data_init.sql sql/t2_designtask1_startup_fix.sql sql/t2_designtask1_startup_fix_gui.sql sql/t2_fault_pipe_diameter_update.sql sql/t2_fault_pipe_parameter_init.sql
```

检查暂存清单：

```powershell
git --no-pager diff --cached --name-only
git status --short
```

确认无误后提交：

```powershell
git commit -m "feat: bind fault pipe parameters when starting design task"
git push origin project2-dev
```

## 4. 不要提交的文件或目录

这些是本机运行产物、临时参数或调试目录，不应提交：

```text
ansys_worker/output/
solidworks_pipe/params.json
tmp_wbpz_compare_1/
AGENTS.md
```

如果 `git status --short` 中仍显示这些文件为 `??`，可以保持不处理。

## 5. 提交前检查点

- 协同机制生成页可以看到“管段编号”选择框。
- 默认会选择数据库中 `is_default = '1'` 的可复用管段参数集。
- 选择后页面能预览材料、外径、内径和壁厚。
- 不选择管段编号时不允许发起任务。
- 发起任务后，后续任务处置页读取的是当前任务绑定的管段参数。
- 模型解耦页进入后先显示“待执行目标约束校验”，点击“执行校验”后才显示通过结果。
- 目标约束选择页不再展示目标权重滑条；模型解耦页展示所有学科已选目标，并由负责人统一设置、归一和保存权重。
- 目标权重控件由 `0-100` 调整为 `0-10`，模型解耦页整体风格调整为更平直、紧凑的工业软件界面，并为主要操作按钮增加图标与克制的彩色状态样式。
- 模型解耦页“目标约束汇总”改为按学科展示目标和约束，顶部显示目标/约束数量，避免标题与内容不匹配。
- 模型解耦页“目标与约束归口确认”改为左右分栏表格：左侧展示目标并在确认时同步设置权重，右侧展示约束关系与阈值，不再使用标签式堆叠汇总。
- 目标/约束左右分栏进一步压缩为固定高度滚动列表，避免页面被条目数量拉长；模型解耦页上下模块统一为白底细边框、紧凑标题栏和克制彩色状态的工业软件风格。
- 模型解耦页目标/约束条目中的学科标签按来源使用不同颜色区分，便于快速识别结构、布局、气动、液压、制造等专业来源。
- 模型解耦页“管段原始设计参数”摘要改为按真实数据渲染，有值才展示摘要卡片；未读取到任务绑定管段参数时显示明确提示，避免出现无内容空框。
- 模型解耦页“管段原始设计参数”中的参数集、管段编号、材料改为轻量基础信息栏，不再用三个大框展示，避免与下方参数分组形成错误对应关系。
- 协同机制、目标选择、模型解耦、仿真验证、任务看板、归档和任务发起相关页面删除说明式提示文案，统一改为短状态、元信息栏和紧凑控件表达，降低页面 AI 感。
- 设计任务相关页面统一切换为模型解耦页同款工业软件风格：白底、细边框、紧凑标题栏、6px 圆角、克制阴影和左侧色条；同步调整任务发起、CAD/ANSYS 预览和归档信息格局部样式。
- 各模块顶部标头恢复为平台首页仪表盘同款大标题卡位置，去除模型解耦页左侧蓝竖线；主流程功能按钮补充刷新、返回、查看、生成、下载、提交等小图标，使页面更接近若依原有操作风格。
- 目标优化方向不再使用“方向”作为列名，页面统一展示为“优化类型：min / max”。
- 仿真验证页“刷新对比”后展示管道设计变量对比、最大等效应力和最大总变形；无真实 ANSYS 结果时不显示假数值。
- 优化前管道设计变量基准值来自 `t2_design_fault_pipe_parameter_item`；正式迁移数据库只执行 `sql/t2_project2_master_migration.sql`，该整合脚本已包含 `sql/t2_fault_pipe_design_variable_baseline.sql` 的回填逻辑。
- 验证结论区域默认不显示通过，必须手动选择“仿真验证通过/不通过”后提交。
- 仿真确认节点的提交按钮以后端返回的 `canConfirmSimulation` 为准，管理员或当前可处理人从查看入口进入时也不会被前端误禁用；历史任务若已到领导审批但缺少人工验证结论，管理员可补录结论且不会重复推动流程。
- “返回任务列表”位于页面顶部，不再放在指标对比卡片内；管段原始设计参数和指标对比卡片等宽展示。
- CAD 参数面板与 CAD 模型预览面板左右等高，ANSYS 指标表格与云图面板左右等高，超出内容在面板内滚动。
- 数据库无需新增表字段，仍使用 `t2_design_fault_pipe_parameter_set` 和 `t2_design_fault_pipe_parameter_item`。
