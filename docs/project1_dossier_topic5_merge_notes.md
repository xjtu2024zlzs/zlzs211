# 课题一数字卷宗合并联调说明

本文档用于课题五合并联调时确认课题一数字卷宗构件的改动范围、接口变化、数据补丁和验证情况。

## 一、提交建议

当前本地工作分支是 `project5-dev`，但本次改动属于课题一数字卷宗构件，建议提交到课题一个人远端分支 `project1-dev-fzw`，再由课题五从该分支合并。

不建议直接提交本地运行目录：

- `docker/mysql/data/`
- `docker/nacos/logs/`
- `docker/redis/data/`

这些目录是本地数据库、缓存和日志文件，不属于代码改动。

## 二、本次功能改动概述

本次改动主要围绕数字卷宗详情、卷宗刷新、课题三数据导出和主起液压供压弯管节点数据。

- 卷宗详情页增强：更完整展示设计参数、制造过程、检验记录、放行记录等内容。
- 卷宗实例页增强：新增卷宗刷新入口，用于按当前源数据重新更新卷宗。
- 课题三联调用导出增强：新增两个 Excel 导出接口，供课题三获取层级对象和零件制造过程数据。
- 数据补丁：新增只更新“主起液压供压弯管”单个节点的数据脚本。
- 状态显示修正：卷宗实例管理 SQL 中补充 `failed` 状态显示为“生成失败”。

## 三、建议提交的文件

### 1. 后端代码

这些文件负责卷宗数据读取、生成、刷新、详情展示和对外接口。

- `ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/controller/DossierInstanceController.java`
- `ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/controller/DossierOpenApiController.java`
- `ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/mapper/DossierGenerationMapper.java`
- `ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/mapper/DossierOpenApiMapper.java`
- `ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/service/IDossierGenerationService.java`
- `ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/service/IDossierOpenApiService.java`
- `ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/service/impl/DossierDetailServiceImpl.java`
- `ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/service/impl/DossierGenerationServiceImpl.java`
- `ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/service/impl/DossierInstanceServiceImpl.java`
- `ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/service/impl/DossierOpenApiServiceImpl.java`
- `ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/service/impl/Project3HierarchyWorkbookBuilder.java`
- `ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/service/impl/Project3PartProcessWorkbookBuilder.java`

### 2. 后端查询配置

这些文件负责从数据库取数字卷宗相关数据。

- `ruoyi-modules/ruoyi-project1/src/main/resources/mapper/project1/dossier/DossierGenerationMapper.xml`
- `ruoyi-modules/ruoyi-project1/src/main/resources/mapper/project1/dossier/DossierInstanceMapper.xml`
- `ruoyi-modules/ruoyi-project1/src/main/resources/mapper/project1/dossier/DossierOpenApiMapper.xml`

### 3. 前端代码

这些文件负责浏览器页面展示和按钮调用。

- `ruoyi-ui/src/api/project1/dossier/instance.js`
- `ruoyi-ui/src/views/project1/dossier/detail/index.vue`
- `ruoyi-ui/src/views/project1/dossier/instance/index.vue`

### 4. 数据库脚本

- `sql/project1/dossier/dossier_instance_management_mysql8.sql`
- `sql/project1/dossier/dossier_hyd_tube_mlg_32a_node_data_patch_mysql8.sql`

其中 `dossier_hyd_tube_mlg_32a_node_data_patch_mysql8.sql` 是本次新增的单节点数据补丁，只处理：

- 件号：`HYD-TUBE-MLG-32A`
- 节点：`f1000006-0006-4006-8006-000000000006`
- 实物：`c7000001-0001-4001-8001-000000000001`

### 5. 测试文件

这些文件用于检查制造数据显示和课题三 Excel 导出是否正常。

- `ruoyi-modules/ruoyi-project1/pom.xml`
- `ruoyi-modules/ruoyi-project1/src/test/java/com/ruoyi/project1/dossier/service/impl/DossierManufacturingDisplayTest.java`
- `ruoyi-modules/ruoyi-project1/src/test/java/com/ruoyi/project1/dossier/service/impl/Project3HierarchyWorkbookBuilderTest.java`
- `ruoyi-modules/ruoyi-project1/src/test/java/com/ruoyi/project1/dossier/service/impl/Project3PartProcessWorkbookBuilderTest.java`

### 6. 合并说明文档

- `docs/project1_dossier_topic5_merge_notes.md`

## 四、新增接口

本次新增 3 个接口，原有接口地址未改名、未删除。

| 用途 | 方法 | 地址 |
| --- | --- | --- |
| 刷新卷宗 | POST | `/project1/dossier/instance/{instanceId}/refresh` |
| 导出课题三层级对象 Excel | GET | `/project1/dossier/openapi/project3/hierarchy/export` |
| 导出课题三零件制造过程 Excel | GET | `/project1/dossier/openapi/project3/part-process/export` |

课题三导出接口常用参数：

- `partNumber`：默认可使用 `HYD-TUBE-MLG-32A`
- `aircraftId`：默认可使用 `b0000001-0001-4001-8001-000000000001`
- `nodeId`：默认可使用 `f1000006-0006-4006-8006-000000000006`

## 五、数据补丁说明

新增脚本：

```text
sql/project1/dossier/dossier_hyd_tube_mlg_32a_node_data_patch_mysql8.sql
```

脚本作用：

- 删除该节点旧的设计参数、制造任务、材料追溯、生产操作、检验、放行等源数据。
- 写入当前本地已确认的新数据。
- 可重复执行，不会重复插入同一批数据。
- 不会清空其他节点的数据。

导入后如果页面仍显示旧卷宗内容，需要在卷宗实例页点击刷新，或重新生成卷宗，因为页面详情可能读取的是已生成卷宗版本。

## 六、数据补丁本地验证结果

该 SQL 已在本地 MySQL 容器中按 UTF-8 方式连续执行两次，均执行成功。

关键数据核对结果：

| 数据项 | 数量 |
| --- | ---: |
| 设计参数 | 23 |
| 管段几何 | 5 |
| 工艺步骤 | 12 |
| 制造工单 | 2 |
| 制造任务 | 16 |
| 材料批次追溯 | 6 |
| 生产操作记录 | 16 |
| 检验记录 | 24 |
| 检验测量值 | 34 |
| 放行记录 | 1 |

关键设计参数核对结果：

| 参数 | 值 |
| --- | --- |
| 材料 | 不锈钢 |
| 外径 | 9.53 mm |
| 壁厚 | 0.9 mm |
| L1 | 280 mm |
| L2 | 150 mm |
| 第一弯角 | 110 deg |
| 第二弯角 | 120 deg |
| 弯曲半径 | 20 mm |

## 七、代码测试验证结果

已执行本次新增/相关测试：

```bash
mvn -pl ruoyi-modules/ruoyi-project1 -am "-Dtest=DossierManufacturingDisplayTest,Project3HierarchyWorkbookBuilderTest,Project3PartProcessWorkbookBuilderTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
```

执行结果：

- `DossierManufacturingDisplayTest`：8 项通过
- `Project3HierarchyWorkbookBuilderTest`：1 项通过
- `Project3PartProcessWorkbookBuilderTest`：1 项通过
- 合计：10 项通过，0 失败

## 八、需要课题五注意

1. 合并时请优先合并课题一数字卷宗目录下的改动。
2. 请不要合并本地 Docker 数据目录和日志目录。
3. `ruoyi-ui/src/views/designtask/frameBeam/LifePredictionPanel.vue` 不属于数字卷宗目录，只是一个页面语法修正，是否合并请课题五单独判断。
4. 数据库脚本导入时请使用 UTF-8，避免中文字段变成问号。
5. 导入数据补丁后，如卷宗详情仍显示旧内容，请刷新或重新生成卷宗实例。

## 九、建议提交命令

如需只提交本次数字卷宗相关改动，可按以下范围添加文件：

```bash
git add \
  ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/controller/DossierInstanceController.java \
  ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/controller/DossierOpenApiController.java \
  ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/mapper/DossierGenerationMapper.java \
  ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/mapper/DossierOpenApiMapper.java \
  ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/service/IDossierGenerationService.java \
  ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/service/IDossierOpenApiService.java \
  ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/service/impl/DossierDetailServiceImpl.java \
  ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/service/impl/DossierGenerationServiceImpl.java \
  ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/service/impl/DossierInstanceServiceImpl.java \
  ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/service/impl/DossierOpenApiServiceImpl.java \
  ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/service/impl/Project3HierarchyWorkbookBuilder.java \
  ruoyi-modules/ruoyi-project1/src/main/java/com/ruoyi/project1/dossier/service/impl/Project3PartProcessWorkbookBuilder.java \
  ruoyi-modules/ruoyi-project1/src/main/resources/mapper/project1/dossier/DossierGenerationMapper.xml \
  ruoyi-modules/ruoyi-project1/src/main/resources/mapper/project1/dossier/DossierInstanceMapper.xml \
  ruoyi-modules/ruoyi-project1/src/main/resources/mapper/project1/dossier/DossierOpenApiMapper.xml \
  ruoyi-ui/src/api/project1/dossier/instance.js \
  ruoyi-ui/src/views/project1/dossier/detail/index.vue \
  ruoyi-ui/src/views/project1/dossier/instance/index.vue \
  sql/project1/dossier/dossier_instance_management_mysql8.sql \
  sql/project1/dossier/dossier_hyd_tube_mlg_32a_node_data_patch_mysql8.sql \
  ruoyi-modules/ruoyi-project1/pom.xml \
  ruoyi-modules/ruoyi-project1/src/test/java/com/ruoyi/project1/dossier/service/impl/DossierManufacturingDisplayTest.java \
  ruoyi-modules/ruoyi-project1/src/test/java/com/ruoyi/project1/dossier/service/impl/Project3HierarchyWorkbookBuilderTest.java \
  ruoyi-modules/ruoyi-project1/src/test/java/com/ruoyi/project1/dossier/service/impl/Project3PartProcessWorkbookBuilderTest.java \
  docs/project1_dossier_topic5_merge_notes.md
```

如需同时合并 `LifePredictionPanel.vue` 的语法修正，请单独添加：

```bash
git add ruoyi-ui/src/views/designtask/frameBeam/LifePredictionPanel.vue
```
