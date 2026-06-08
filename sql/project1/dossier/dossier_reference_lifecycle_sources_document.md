# 卷宗公开依据增强测试数据说明 V1.2

## 1. 数据增强目标

本次增强用于“单台份飞机综合卷宗模板”的演示数据补强，重点补充三类卷宗内容：

- 整机全生命周期数据：设计、制造、装机、试验、交付、运营、维修、故障、技术状态和证明文件。
- 液压系统全生命周期数据：按 ATA 29 补充液压动力、压力控制、过滤污染监控、蓄压器、管路、弯管、接口、维修和故障闭环。
- 起落架系统全生命周期数据：按 ATA 32 补充收放机构、锁定/位置指示、刹车防滑、轮胎机轮、减震支柱、应急放下、维护和故障闭环。

模板版本保持 `V1.0`。本次产生的卷宗版本为 `V1.2`，版本变化原因是数据变化，不是模板变化。

## 2. 公开依据来源

本次未把公开资料当作某架真实飞机的原始记录使用，而是用作演示数据的专业口径依据。具体 B-1234 的序列号、日期、工单、航段、测量值和证据文件编号均为按口径生成的模拟数据。

| 来源 | 用途 | 对应卷宗数据口径 |
| --- | --- | --- |
| FAA, Aviation Maintenance Technician Handbook - Airframe Volume 2, Chapter 12 Hydraulic and Pneumatic Power Systems | 液压系统维护、压力/指示、过滤、污染、蓄压器、管路和液压动力系统口径 | `t1_object_lifecycle_record`, `t1_inspection_record`, `t1_inspection_measurement`, `t1_work_order`, `t1_fault_event`, `t1_part_document` |
| FAA, Aviation Maintenance Technician Handbook - Airframe Volume 2, Chapter 13 Aircraft Landing Gear Systems | 起落架收放、减震支柱、机轮轮胎、刹车、防滑、维护检查和放气/勤务口径 | `t1_object_lifecycle_record`, `t1_inspection_record`, `t1_inspection_measurement`, `t1_work_order`, `t1_fault_event`, `t1_part_document` |
| 14 CFR 25.1435 Hydraulic systems | 液压系统元件 proof/ultimate pressure、系统指示、压力控制、功能试验和整机联试要求 | 液压系统设计验证、压力试验、功能试验、接口数据和证明文件 |
| 14 CFR 25.729 Retracting mechanism | 起落架收放机构、锁定、应急放下、位置指示、警告和轮舱设备防护要求 | 起落架系统设计验证、收放试验、位置指示、应急放下、维护和故障闭环 |

参考链接：

- FAA AMT Handbook Airframe Vol. 2: https://www.faa.gov/handbooksmanuals/aviation/aviation-maintenance-technician-handbook-airframe-volume-2
- FAA PDF: https://www.faa.gov/sites/faa.gov/files/2022-06/amt_airframe_hb_vol_2.pdf
- 14 CFR 25.1435: https://www.ecfr.gov/current/title-14/section-25.1435
- 14 CFR 25.729: https://www.ecfr.gov/current/title-14/section-25.729

## 3. 数据落库脚本

增强脚本：`sql/dossier_reference_lifecycle_enrichment_mysql8.sql`

脚本批次号：`REF_PUBLIC_20260608`

脚本特性：

- 只清理本批次 `REF_PUBLIC_20260608` 相关演示数据，不删除旧卷宗版本。
- 不修改模板版本号，模板仍为 `V1.0`。
- 补充模板章节数据来源，使章节能从生命周期、航段、检查、维修、故障、接口、技术状态和证明文件表中取数。
- 以整机、液压系统、起落架系统及其下属设备、组件、零件为重点生成数据。
- 通过卷宗数据指纹触发新的卷宗数据版本，旧卷宗快照不会跟随飞机节点或生命周期表变化。

## 4. 目录结构映射

| 卷宗目录节点 | 数据来源表 | 本次补强方式 |
| --- | --- | --- |
| `AIRCRAFT_DESIGN` 整机设计数据 | `t1_object_lifecycle_record`, `t1_part_document` | 增加需求基线、安全性分析、维修性分析、符合性矩阵、接口控制和对象锚点冻结事件 |
| `AIRCRAFT_MANUFACTURING` 整机制造数据 | `t1_object_lifecycle_record`, `t1_inspection_record`, `t1_inspection_measurement` | 增加大部段对接、液压系统安装、起落架安装、压力试验、收放试验和总装通电检查 |
| `AIRCRAFT_SERVICE` 整机服役数据 | `t1_event_flight_leg`, `t1_life_usage_record`, `t1_object_lifecycle_record` | 增加航段、飞行小时、循环、起落循环、系统动作次数和服役复核事件 |
| `AIRCRAFT_FAULT` 整机故障维修 | `t1_fault_event`, `t1_work_order`, `t1_object_status_history` | 增加液压渗漏、污染度告警、起落架位置指示、刹车防滑等故障闭环数据 |
| `AIRCRAFT_TECH_STATUS` 整机技术状态 | `t1_object_technical_status`, `t1_object_status_history` | 增加配置状态、服务通告/适航指令复核、寿命限制件状态和定检状态 |
| `SUBSYSTEM_STRUCTURE` 子系统结构 | `t1_aircraft_bom_node` | 保留唯一 BOM 树，用于结构和层级展示 |
| `SUBSYSTEM_EQUIPMENT` 设备清单与状态 | `t1_aircraft_bom_node`, `t1_object_technical_status`, `t1_object_status_history` | 不再展示第二棵 BOM 树，改为设备清单、状态和生命周期摘要 |
| `SYSTEM_INTERFACE` 系统接口 | `t1_object_interface` | 增加液压到起落架收放作动、刹车防滑压力、起落架位置指示接口 |
| `SYSTEM_MAINTENANCE` 系统维护 | `t1_work_order`, `t1_fault_event`, `t1_inspection_record` | 增加系统定检、压力保持、污染度检查、减震支柱勤务、收放测试和故障排除 |
| `PART_INSPECTION` 零件检验 | `t1_inspection_record`, `t1_inspection_measurement` | 增加尺寸、压力、泄漏、污染度、锁定间隙、胎压、刹车磨耗等测量记录 |
| `PART_DOCUMENT` 零件证明文件 | `t1_part_document` | 增加 COC、FAI、NDT、压力试验、终检、装机、维修放行和寿命记录 |

## 5. 本次新增数据规模

| 数据项 | 新增数量 |
| --- | ---: |
| 重点作用域节点 | 1014 |
| 生命周期事件 | 2058 |
| 航段记录 | 24 |
| 寿命使用记录 | 24 |
| 检查记录 | 171 |
| 检查测量项 | 342 |
| 技术状态 | 9 |
| 状态历史 | 1014 |
| 接口记录 | 3 |
| 维修工单 | 172 |
| 故障记录 | 68 |
| 证明文件 | 4048 |

脚本执行后，整机维度可追溯记录总量约为：

- `t1_object_lifecycle_record`: 6117 条
- `t1_part_document`: 5065 条
- `t1_event_flight_leg`: 25 条
- `t1_inspection_record`: 172 条

卷宗生成输出中的 `sourceRecordCount` 为 `20321`。

## 6. 液压弯管重点数据

演示重点件：`HYD-TUBE-MLG-32A`

本次卷宗内容摘要中，该节点已汇总：

- 生命周期事件：9 条
- 技术状态：3 条
- 维修工单：3 条
- 故障记录：3 条
- 证明文件：8 条

覆盖内容包括：

- 件号、序列号、对象锚点和装机位置。
- 弯管设计参数、材料和压力等级口径。
- 成形、清洗、压力试验、终检和装机记录。
- 服役期间渗漏、污染度、接头力矩、压力保持和复检闭环。
- COC、FAI、NDT、压力试验、终检、装机和维修放行证明文件。

## 7. 生成结果验证

当前数据库中卷宗版本：

| 版本 | 变化原因 | 模板版本 | 当前版本 |
| --- | --- | --- | --- |
| `V1.0` | initial | `V1.0` | 否 |
| `V1.1` | data_update | `V1.0` | 否 |
| `V1.2` | data_update | `V1.0` | 是 |

`V1.2` 生成结果：

- 卷宗版本 ID：`b64df37c-f2bb-4dbc-a1d8-dceb52aef0f0`
- 结构节点：2014 个
- 内容节点：2014 个
- 生成任务：`DGJ-20260608145630-870e8a6a`
- 重复生成校验：相同模板和相同数据指纹再次生成时返回已有 `V1.2`，不会重复生成新卷宗。

## 8. 真实性边界

本批数据的真实性分为三层：

- 公开资料直接支撑：液压系统、起落架系统的章节主题、检查对象、试验类型、证明文件类别和适航关注点。
- 工程合理生成：单台份飞机的制造、装机、交付、运行、维修、故障和检查事件链条。
- 演示模拟数据：B-1234 的具体日期、航班号、工单号、序列号、批次号、测量值和人员/部门信息。

因此，本批数据适合用于卷宗模板、卷宗生成、版本管理、详情可视化和追溯链演示，不应被表述为真实飞机的原始适航档案。
