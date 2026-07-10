# 全生命周期质量追溯分析报告

## 1. 报告基本信息
- domain：hydraulic
- case_id：HFB-02227
- feedback_id：HFB-02227
- report_time：2026-06-08 00:17:48
- model_name：TransH
- top_k：6
- reasoning_json：D:\2.11\topic5_code\code2\transH\outputs\hydraulic\reports\reasoning_HFB-02227.json
- original_graph_json：D:\2.11\topic5_code\code2\transH\outputs\hydraulic\graphs\original_graph_HFB-02227.json
- fault_enhanced_graph_json：D:\2.11\topic5_code\code2\transH\outputs\hydraulic\graphs\fault_enhanced_graph_HFB-02227.json

## 2. 质量反馈概述
- 故障类型：flow_abnormal
- 故障位置：过滤器
- 严重程度：medium
- 诊断置信度：0.8115
- RCA置信度：0.6784
- 目标对象：HCU-02227（HydraulicComponent，过滤器）

## 3. 上游诊断/RCA结果
- 根因传感器：FS1（0.9109）
- 次因传感器：TS3（0.768）
- 根因部件：C008 液压油箱
- 部件置信度：0.8361
- 传播说明：RCA根因部件与当前反馈部件不完全一致，可能表示故障传播或相邻部件诱发。

### 12部件诊断排序 Top5
| rank | component_code | component_name | fault_type | severity | judgement | fault_level |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | C008 | 液压油箱 | 油箱故障 | 0.81167 | 故障 | 重度故障 |
| 2 | C006 | 冷却器 | 冷却器故障 | 0.3544 | 故障 | 中度故障 |
| 3 | C001 | 液压泵总成 | 液压泵故障 | 0.31096 | 正常 | 轻微退化 |
| 4 | C007 | 过滤器 | 过滤器堵塞 | 0.28354 | 正常 | 轻微退化 |
| 5 | C002 | 方向控制阀 | 方向阀故障 | 0.12046 | 正常 | 轻微退化 |

### 子类型概率 Top5
| component_code | subtype_id | subtype_name | fault_part | probability |
| --- | --- | --- | --- | --- |
| C007 | C007-S1 | 滤芯堵塞 | - | 0.563814 |
| C006 | C006-S2 | 管束堵塞 | - | 0.553826 |
| C011 | C011-S4 | 管路堵塞 | - | 0.448604 |
| C004 | C004-S4 | 阻尼孔堵塞 | - | 0.439066 |
| C010 | C010-S1 | 节流口堵塞 | - | 0.437902 |

### 传感器异常能量 Top5
| sensor | energy |
| --- | --- |
| FS1 | 0.89764 |
| TS3 | 0.82869 |
| FS2 | 0.76367 |
| VS1 | 0.14775 |
| PS4 | 0.14127 |

## 4. 原始生命周期知识图谱概述
| graph | node_count | edge_count | 包含生命周期阶段 |
| --- | --- | --- | --- |
| original_graph | 22 | 26 | 使用/运维阶段、装配阶段、设计阶段、制造阶段、检测阶段、材料阶段 |

目标对象生命周期节点摘要：
| relation | node_id | node_type | stage | node_name |
| --- | --- | --- | --- | --- |
| 设计依据 | HDES-00547 | DesignSpec | 设计阶段 | 设计规范 HDES-00547 |
| 使用材料批次 | HMAT-00063 | MaterialBatch | 材料阶段 | 材料批次 HMAT-00063 |
| 制造批次 | HMAN-01074 | ManufacturingBatch | 制造阶段 | 制造批次 HMAN-01074 |
| 装配记录 | HASM-02227 | AssemblyRecord | 装配阶段 | 装配记录 HASM-02227 |
| 检测记录 | HINSP-02227 | InspectionRecord | 检测阶段 | 检测记录 HINSP-02227 |
| 运维记录 | HOPR-02227 | MaintenanceRecord | 使用/运维阶段 | 运维记录 HOPR-02227 |
| 监测传感器 | TS4 | Sensor | RCA 诊断 | 传感器 TS4 |
| 监测传感器 | FS1 | Sensor | RCA 诊断 | 传感器 FS1 |
| has_component_code | C007 | ComponentCode | 未知阶段 | ComponentCode C007 |
| has_component_name | COMPONENT_NAME_过滤器 | ComponentName | 未知阶段 | ComponentName COMPONENT_NAME_过滤器 |
| belongs_to_category | CATEGORY_filtration | ComponentCategory | 未知阶段 | ComponentCategory CATEGORY_filtration |
| has_overall_lifecycle_risk_score | RISK_25_50 | RiskScore | 未知阶段 | RiskScore RISK_25_50 |

## 5. 故障增强知识图谱概述
| graph | node_count | edge_count | added_node_count | added_edge_count |
| --- | --- | --- | --- | --- |
| fault_enhanced_graph | 54 | 67 | 32 | 41 |

故障增强图在原始生命周期链基础上新增 32 个节点，主要包括 ComponentDiagnosis, ConfidenceLevel, FaultEvolutionText, FaultPosition, FaultSeverity, FaultSubtype, FaultType, HydraulicComponent 等，用于承载故障、RCA、Top6候选和阶段归因信息。

## 6. TransH Top6疑似原因分析
| rank | candidate_id | candidate_type | stage | final | transh | path | evidence | prior | scope | description |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | HMAT-00063 | MaterialBatch | 材料阶段 | 0.526299 | 0.397597 | 1.0 | 0.75 | 0.1 | lifecycle_candidate | 该实体位于当前反馈对象生命周期链上，属于材料阶段，具备直接路径证据。 |
| 2 | HMAT-00470 | MaterialBatch | 材料阶段 | 0.521259 | 0.432517 | 0.85 | 0.8 | 0.1 | rca_root_component_candidate | 该候选来自 RCA 根因部件生命周期链，属于材料阶段。RCA 根因部件与反馈对象不完全一致时，可能表示故障传播或相邻部件诱发。 |
| 3 | HMAN-01074 | ManufacturingBatch | 制造阶段 | 0.490706 | 0.320412 | 1.0 | 0.75 | 0.12 | lifecycle_candidate | 该实体位于当前反馈对象生命周期链上，属于制造阶段，具备直接路径证据。 |
| 4 | HOPR-02227 | MaintenanceRecord | 使用/运维阶段 | 0.437261 | 0.153522 | 1.0 | 0.75 | 0.32 | lifecycle_candidate | 该实体位于当前反馈对象生命周期链上，属于使用/运维阶段，具备直接路径证据。 |
| 5 | HASM-02227 | AssemblyRecord | 装配阶段 | 0.430692 | 0.188383 | 1.0 | 0.75 | 0.16 | lifecycle_candidate | 该实体位于当前反馈对象生命周期链上，属于装配阶段，具备直接路径证据。 |
| 6 | HINSP-02227 | InspectionRecord | 检测阶段 | 0.393072 | 0.137143 | 1.0 | 0.75 | 0.08 | lifecycle_candidate | 该实体位于当前反馈对象生命周期链上，属于检测阶段，具备直接路径证据。 |

## 7. 生命周期阶段归因
- 首要阶段：材料阶段
- 反馈目标子系统：材料/供应商管理子系统
- 说明：材料阶段得分最高，主要因为 Top6 中 HMAT-00063、HMAT-00470 聚集在该阶段，且综合分数、路径分数或证据分数较高。

| stage | stage_name | score |
| --- | --- | --- |
| material | 材料阶段 | 1.0 |
| manufacturing | 制造阶段 | 0.407991 |
| operation | 使用/运维阶段 | 0.374497 |
| assembly | 装配阶段 | 0.361233 |
| inspection | 检测阶段 | 0.258153 |
| design | 设计阶段 | 0.055482 |

### 重点关注阶段 Top3
| rank | stage | stage_name | score | feedback_subsystem |
| --- | --- | --- | --- | --- |
| 1 | material | 材料阶段 | 1.0 | 材料/供应商管理子系统 |
| 2 | manufacturing | 制造阶段 | 0.407991 | 制造工艺子系统 |
| 3 | operation | 使用/运维阶段 | 0.374497 | 运维保障子系统 |

## 8. 关键证据链
- HFB-02227 -> occurs_on -> HCU-02227：质量反馈首先定位到发生故障的目标对象。
- HCU-02227 -> uses_material_batch -> HMAT-00063：候选 HMAT-00063 位于目标对象生命周期链或其关键扩展路径上，path_score=1.0。
- HCU-02227 -> manufactured_in -> HMAN-01074：候选 HMAN-01074 位于目标对象生命周期链或其关键扩展路径上，path_score=1.0。
- HFB-02227 -> may_caused_by -> HMAT-00063：TransH 综合重排序将 HMAT-00063 排为 Top1，final_score=0.526299。

## 9. 子系统反馈建议
- 目标子系统：材料/供应商管理子系统
- 首要反馈子系统：材料/供应商管理子系统
- 重点反馈子系统 Top3：材料/供应商管理子系统、制造工艺子系统、运维保障子系统
- 建议：建议反馈材料/供应商管理子系统，复核材料批次、硬度、疲劳性能。

## 10. 追溯结论
本次追溯的首要疑似原因是 HMAT-00063（MaterialBatch），首要疑似阶段为材料阶段。主要证据包括生命周期路径、TransH 评分、RCA/证据匹配以及规则先验，其中 TransH 综合重排序将 HMAT-00063 排为 Top1，final_score=0.526299。RCA根因部件与当前反馈部件不完全一致，可能表示故障传播或相邻部件诱发。 建议优先由材料/供应商管理子系统牵头处置，并联动相关生命周期环节复核。除首要疑似生命周期阶段外，系统同时给出重点关注阶段 Top3，用于支持跨阶段联合复核。建议优先反馈至材料/供应商管理子系统，并联合制造工艺子系统、运维保障子系统进行辅助复核。


### 生命周期阶段复核子系统参考
| stage | stage_name | score | feedback_subsystem |
| --- | --- | --- | --- |
| material | 材料阶段 | 1.0 | 材料/供应商管理子系统 |
| manufacturing | 制造阶段 | 0.407991 | 制造工艺子系统 |
| operation | 使用/运维阶段 | 0.374497 | 运维保障子系统 |
| assembly | 装配阶段 | 0.361233 | 装配子系统 |
| inspection | 检测阶段 | 0.258153 | 检测子系统 |
| design | 设计阶段 | 0.055482 | 设计子系统 |
