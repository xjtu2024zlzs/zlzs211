# 全生命周期质量追溯分析报告

## 1. 报告基本信息
- domain：hydraulic
- case_id：HFB-00356
- feedback_id：HFB-00356
- report_time：2026-06-05 22:38:25
- model_name：TransH
- top_k：6
- reasoning_json：D:\Cursor\transH\outputs\hydraulic\reports\reasoning_HFB-00356.json
- original_graph_json：D:\Cursor\transH\outputs\hydraulic\graphs\original_graph_HFB-00356.json
- fault_enhanced_graph_json：D:\Cursor\transH\outputs\hydraulic\graphs\fault_enhanced_graph_HFB-00356.json

## 2. 质量反馈概述
- 故障类型：valve_stuck
- 故障位置：方向控制阀
- 严重程度：medium
- 诊断置信度：0.7754
- RCA置信度：0.6498
- 目标对象：HCU-00356（HydraulicComponent，方向控制阀）

## 3. 上游诊断/RCA结果
- 根因传感器：PS3（0.9214）
- 次因传感器：PS2（0.5373）
- 根因部件：C002 方向控制阀
- 部件置信度：0.8242
- 传播说明：

### 12部件诊断排序 Top5
| rank | component_code | component_name | fault_type | severity | judgement | fault_level |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | C002 | 方向控制阀 | 方向阀故障 | 0.69236 | 故障 | 中度故障 |
| 2 | C009 | 单向阀 | 单向阀故障 | 0.3851 | 故障 | 中度故障 |
| 3 | C010 | 节流阀 | 节流阀故障 | 0.32755 | 正常 | 轻微退化 |
| 4 | C003 | 作动筒 | 作动筒故障 | 0.30439 | 正常 | 轻微退化 |
| 5 | C005 | 蓄能器 | 蓄能器故障 | 0.27687 | 正常 | 轻微退化 |

### 子类型概率 Top5
| component_code | subtype_id | subtype_name | fault_part | probability |
| --- | --- | --- | --- | --- |
| C012 | C012-S2 | 阀芯卡滞常开 | - | 0.601854 |
| C009 | C009-S1 | 阀芯卡死 | - | 0.59728 |
| C007 | C007-S1 | 滤芯堵塞 | - | 0.590173 |
| C008 | C008-S2 | 空气滤清器堵塞 | - | 0.589914 |
| C006 | C006-S2 | 管束堵塞 | - | 0.578913 |

### 传感器异常能量 Top5
| sensor | energy |
| --- | --- |
| PS3 | 0.91955 |
| FS1 | 0.80196 |
| PS4 | 0.46777 |
| PS2 | 0.27924 |
| TS2 | 0.148 |

## 4. 原始生命周期知识图谱概述
| graph | node_count | edge_count | 包含生命周期阶段 |
| --- | --- | --- | --- |
| original_graph | 24 | 27 | 使用/运维阶段、装配阶段、设计阶段、制造阶段、检测阶段、材料阶段 |

目标对象生命周期节点摘要：
| relation | node_id | node_type | stage | node_name |
| --- | --- | --- | --- | --- |
| 设计依据 | HDES-01161 | DesignSpec | 设计阶段 | 设计规范 HDES-01161 |
| 使用材料批次 | HMAT-00085 | MaterialBatch | 材料阶段 | 材料批次 HMAT-00085 |
| 制造批次 | HMAN-01086 | ManufacturingBatch | 制造阶段 | 制造批次 HMAN-01086 |
| 装配记录 | HASM-00356 | AssemblyRecord | 装配阶段 | 装配记录 HASM-00356 |
| 检测记录 | HINSP-00356 | InspectionRecord | 检测阶段 | 检测记录 HINSP-00356 |
| 运维记录 | HOPR-00356 | MaintenanceRecord | 使用/运维阶段 | 运维记录 HOPR-00356 |
| 监测传感器 | PS3 | Sensor | RCA 诊断 | 传感器 PS3 |
| 监测传感器 | FS1 | Sensor | RCA 诊断 | 传感器 FS1 |
| 监测传感器 | PS4 | Sensor | RCA 诊断 | 传感器 PS4 |
| has_component_code | C002 | ComponentCode | 未知阶段 | ComponentCode C002 |
| has_component_name | COMPONENT_NAME_方向控制阀 | ComponentName | 未知阶段 | ComponentName COMPONENT_NAME_方向控制阀 |
| belongs_to_category | CATEGORY_control_valve | ComponentCategory | 未知阶段 | ComponentCategory CATEGORY_control_valve |

## 5. 故障增强知识图谱概述
| graph | node_count | edge_count | added_node_count | added_edge_count |
| --- | --- | --- | --- | --- |
| fault_enhanced_graph | 50 | 64 | 26 | 37 |

故障增强图在原始生命周期链基础上新增 26 个节点，主要包括 ComponentDiagnosis, ConfidenceLevel, FaultEvolutionText, FaultPosition, FaultSeverity, FaultSubtype, FaultType, HydraulicComponent 等，用于承载故障、RCA、Top6候选和阶段归因信息。

## 6. TransH Top6疑似原因分析
| rank | candidate_id | candidate_type | stage | final | transh | path | evidence | prior | scope | description |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | HINSP-00356 | InspectionRecord | 检测阶段 | 0.8245 | 1.0 | 1.0 | 0.75 | 0.08 | lifecycle_candidate | 该实体位于当前反馈对象生命周期链上，属于检测阶段，具备直接路径证据。 |
| 2 | HMAN-01086 | ManufacturingBatch | 制造阶段 | 0.529539 | 0.374078 | 1.0 | 0.75 | 0.2 | lifecycle_candidate | 该实体位于当前反馈对象生命周期链上，属于制造阶段，具备直接路径证据。 |
| 3 | OP-H-022 | Operator | 使用/运维阶段 | 0.502201 | 0.372402 | 0.8 | 0.74 | 0.3 | expanded_candidate | 该实体由当前反馈对象生命周期节点一跳扩展得到，属于使用/运维阶段，具备间接路径证据。 |
| 4 | HOPR-00356 | MaintenanceRecord | 使用/运维阶段 | 0.451887 | 0.188773 | 1.0 | 0.75 | 0.3 | lifecycle_candidate | 该实体位于当前反馈对象生命周期链上，属于使用/运维阶段，具备直接路径证据。 |
| 5 | HMAT-00085 | MaterialBatch | 材料阶段 | 0.439998 | 0.212996 | 1.0 | 0.75 | 0.14 | lifecycle_candidate | 该实体位于当前反馈对象生命周期链上，属于材料阶段，具备直接路径证据。 |
| 6 | HASM-00356 | AssemblyRecord | 装配阶段 | 0.381069 | 0.101137 | 1.0 | 0.75 | 0.12 | lifecycle_candidate | 该实体位于当前反馈对象生命周期链上，属于装配阶段，具备直接路径证据。 |

## 7. 生命周期阶段归因
- 首要阶段：检测阶段
- 反馈目标子系统：检测子系统
- 说明：检测阶段得分最高，主要因为 Top6 中 HINSP-00356 聚集在该阶段，且综合分数、路径分数或证据分数较高。

| stage | stage_name | score |
| --- | --- | --- |
| inspection | 检测阶段 | 1.0 |
| manufacturing | 制造阶段 | 0.550921 |
| operation | 使用/运维阶段 | 0.481788 |
| assembly | 装配阶段 | 0.292867 |
| material | 材料阶段 | 0.26445 |
| design | 设计阶段 | 0.0 |

### 重点关注阶段 Top3
| rank | stage | stage_name | score | feedback_subsystem |
| --- | --- | --- | --- | --- |
| 1 | inspection | 检测阶段 | 1.0 | 检测子系统 |
| 2 | manufacturing | 制造阶段 | 0.550921 | 制造工艺子系统 |
| 3 | operation | 使用/运维阶段 | 0.481788 | 运维保障子系统 |

## 8. 关键证据链
- HFB-00356 -> occurs_on -> HCU-00356：质量反馈首先定位到发生故障的目标对象。
- HCU-00356 -> inspected_by -> HINSP-00356：候选 HINSP-00356 位于目标对象生命周期链或其关键扩展路径上，path_score=1.0。
- HCU-00356 -> manufactured_in -> HMAN-01086：候选 HMAN-01086 位于目标对象生命周期链或其关键扩展路径上，path_score=1.0。
- HFB-00356 -> may_caused_by -> HINSP-00356：TransH 综合重排序将 HINSP-00356 排为 Top1，final_score=0.8245。

## 9. 子系统反馈建议
- 目标子系统：检测子系统
- 首要反馈子系统：检测子系统
- 重点反馈子系统 Top3：检测子系统、制造工艺子系统、运维保障子系统
- 建议：建议反馈检测子系统，复核检测设备、检测记录和异常指标。

## 10. 追溯结论
本次追溯的首要疑似原因是 HINSP-00356（InspectionRecord），首要疑似阶段为检测阶段。主要证据包括生命周期路径、TransH 评分、RCA/证据匹配以及规则先验，其中 TransH 综合重排序将 HINSP-00356 排为 Top1，final_score=0.8245。当前报告未发现明确跨部件传播描述，仍建议结合现场记录复核跨阶段影响。 建议优先由检测子系统牵头处置，并联动相关生命周期环节复核。除首要疑似生命周期阶段外，系统同时给出重点关注阶段 Top3，用于支持跨阶段联合复核。建议优先反馈至检测子系统，并联合制造工艺子系统、运维保障子系统进行辅助复核。


### 生命周期阶段复核子系统参考
| stage | stage_name | score | feedback_subsystem |
| --- | --- | --- | --- |
| inspection | 检测阶段 | 1.0 | 检测子系统 |
| manufacturing | 制造阶段 | 0.550921 | 制造工艺子系统 |
| operation | 使用/运维阶段 | 0.481788 | 运维保障子系统 |
| assembly | 装配阶段 | 0.292867 | 装配子系统 |
| material | 材料阶段 | 0.26445 | 材料/供应商管理子系统 |
| design | 设计阶段 | 0.0 | 设计子系统 |
