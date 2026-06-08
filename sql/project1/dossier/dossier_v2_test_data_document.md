# 卷宗第二版测试数据说明

## 版本边界

- 模板：`单台份飞机综合卷宗模板`
- 模板版本：`V1.0`，本次不升级模板版本
- 卷宗实例：`fb123401-0001-4001-8001-202606070001`
- 第二版卷宗：`V1.1`
- 版本原因：`data_update`
- 第二版版本 ID：`62a80a89-a734-4030-956f-522dbd31cfcd`
- 生成任务：`DGJ-20260608140949-dbb05b88`

本次模板目录调整是对模板设计问题的修正，不作为模板版本升级处理。卷宗版本变化仅由飞机生命周期数据补充触发。

## 模板目录修正

子系统层级原来同时存在“子系统结构”和“组成设备”两棵 BOM 树，展示上会重复。第二版修正为：

- `SUBSYSTEM_STRUCTURE`：保留为“子系统结构”，展示类型 `tree_table`
- `SUBSYSTEM_EQUIPMENT`：改为“设备清单与状态”，展示类型 `summary_table`
- `SUBSYSTEM_EQUIPMENT` 数据源：仍来自 `t1_aircraft_bom_node`，但只平铺查询子系统直属设备，不再展开设备、组件、零件树

## 数据覆盖范围

本次补强范围为 B-1234 整机、液压系统 `SYS-29`、起落架系统 `SYS-32` 及两套系统下属设备、组件、零件。共覆盖 1014 个对象锚点：

| 层级 | 生命周期事件数 |
| --- | ---: |
| aircraft | 4 |
| system | 8 |
| subsystem | 96 |
| equipment | 576 |
| component | 1160 |
| part | 2212 |

生命周期阶段按现库枚举生成：

| 阶段 | 记录数 |
| --- | ---: |
| design | 1014 |
| manufacturing | 1014 |
| installation | 1014 |
| service | 1014 |

## 补充数据统计

| 表 | 新增记录数 | 说明 |
| --- | ---: | --- |
| `t1_object_lifecycle_record` | 4056 | 设计、制造、装机、服役四阶段事件 |
| `t1_object_technical_status` | 1014 | 构型基线、BOM 版本、图纸/工艺版本、放行状态 |
| `t1_object_status_history` | 3042 | 构型、质量、服役状态历史 |
| `t1_life_usage_record` | 1014 | 飞行小时、循环、起落、剩余寿命 |
| `t1_part_document` | 1014 | 图纸、CMM、IPC、SPEC 等证明文件 |
| `t1_work_order` | 171 | 系统、子系统、设备及关键弯管维修/复核工单 |
| `t1_fault_event` | 33 | 监控告警、预防性复查、故障关闭 |
| `t1_object_interface` | 5 | 液压、起落架、作动筒、弯管、刹车控制接口 |

## 液压弯管重点演示数据

关键对象：`HYD-TUBE-MLG-32A`，主起液压供压弯管。

| 字段 | 值 |
| --- | --- |
| BOM 节点 ID | `f1000006-0006-4006-8006-000000000006` |
| 零件实例 ID | `c7000001-0001-4001-8001-000000000001` |
| 序列号 | `HT-MLG-32A-2026-0042` |
| 位置 | `POS-HYD-00695` |
| ATA | `29-695` |

生命周期事件：

| 阶段 | 日期 | 状态 | 标题 |
| --- | --- | --- | --- |
| design | 2025-09-06 | released | 主起液压供压弯管设计定义发布 |
| manufacturing | 2025-11-30 | accepted | 主起液压供压弯管制造/验收记录关闭 |
| installation | 2026-02-23 | installed | 主起液压供压弯管装机与构型确认 |
| service | 2026-05-19 | in_service | 主起液压供压弯管服役监控与定检复核 |

技术状态：

- 状态编码：`D2-TECH-HYD-TUBE-MLG-32A-423ed682`
- 构型基线：`BL-B1234-HYD-2026-06-D2`
- BOM 版本：`BOM-B1234-2026.06.D2`
- 图纸版本：`PART-DWG-REV-C`
- 验证状态：`verified`
- 放行状态：`released`

维修与故障：

- 工单：`WO-D2-HYD-TUBE-MLG-32A-423ed6`
- 工单类型：`LINE`
- 工单状态：`CLOSED`
- 放行号：`CRS-D2-423ed682a2`
- 故障描述：第二版补充：主起供压弯管压力脉动复核，支承垫片更换后复测合格。
- 严重度：`MAJOR`
- 处理结果：`RESOLVED / PART_REPLACED`

## 生成验证

生成后卷宗版本表：

| version_no | version_label | version_level | version_reason | template_version | is_current |
| ---: | --- | --- | --- | --- | ---: |
| 1 | V1.0 | major | initial | V1.0 | 0 |
| 2 | V1.1 | minor | data_update | V1.0 | 1 |

第二版卷宗结构节点数：2014。

第二版卷宗内容项数：2014。

重复生成验证：在相同模板 `V1.0` 和相同数据指纹下再次调用生成接口，返回 `duplicated=true`，命中已有版本 `62a80a89-a734-4030-956f-522dbd31cfcd`，未产生第三个版本。

