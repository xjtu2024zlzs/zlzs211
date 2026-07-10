# 全生命周期质量追溯分析报告

## 1. 报告基本信息
- domain：hydraulic
- case_id：HFB-01676
- feedback_id：HFB-01676
- report_time：2026-07-05 20:54:46
- model_name：TransH
- top_k：6
- reasoning_json：D:\Cursor\trasnH2\transH\outputs\hydraulic\reports\reasoning_HFB-01676.json
- original_graph_json：D:\Cursor\trasnH2\transH\outputs\hydraulic\graphs\original_graph_HFB-01676.json
- fault_enhanced_graph_json：D:\Cursor\trasnH2\transH\outputs\hydraulic\graphs\fault_enhanced_graph_HFB-01676.json

## 2. 质量反馈概述
- 故障类型：pressure_abnormal
- 故障位置：管路总成
- 严重程度：medium
- 诊断置信度：0.7308
- RCA置信度：0.6181
- 目标对象：HCU-01676（HydraulicComponent，管路总成）

## 3. 上游诊断/RCA结果
- 根因传感器：PS6（0.96）
- 次因传感器：PS2（0.7556）
- 根因部件：C005 蓄能器
- 部件置信度：0.863
- 传播说明：RCA根因部件与当前反馈部件不完全一致，可能表示故障传播或相邻部件诱发。

### 12部件诊断排序 Top5
| rank | component_code | component_name | fault_type | severity | judgement | fault_level |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | C005 | 蓄能器 | 蓄能器故障 | 0.82339 | 故障 | 重度故障 |
| 2 | C011 | 管路总成 | 管路泄漏 | 0.43848 | 故障 | 中度故障 |
| 3 | C004 | 溢流阀 | 溢流阀故障 | 0.39472 | 故障 | 中度故障 |
| 4 | C003 | 作动筒 | 作动筒故障 | 0.13872 | 正常 | 轻微退化 |
| 5 | C007 | 过滤器 | 过滤器堵塞 | 0.13594 | 正常 | 轻微退化 |

### 子类型概率 Top5
| component_code | subtype_id | subtype_name | fault_part | probability |
| --- | --- | --- | --- | --- |
| C009 | C009-S3 | 弹簧断裂失效 | - | 0.586926 |
| C012 | C012-S1 | 卸荷压力漂移 | - | 0.580998 |
| C004 | C004-S2 | 主阀芯弹簧疲劳 | - | 0.479914 |
| C005 | C005-S1 | 气囊破裂 | - | 0.465117 |
| C002 | C002-S3 | 复位弹簧断裂 | 复位弹簧 | 0.458642 |

### 传感器异常能量 Top5
| sensor | energy |
| --- | --- |
| PS6 | 1.0 |
| PS2 | 0.79911 |
| PS3 | 0.19495 |
| PS1 | 0.19344 |
| PS4 | 0.16131 |

## 4. 原始生命周期知识图谱概述
| graph | node_count | edge_count | 包含生命周期阶段 |
| --- | --- | --- | --- |
| original_graph | 36 | 39 | 使用/运维阶段、设计阶段、装配阶段、制造阶段、检测阶段、材料阶段 |

目标对象生命周期节点摘要：
| relation | node_id | node_type | stage | node_name |
| --- | --- | --- | --- | --- |
| 设计依据 | HDES-00252 | DesignSpec | 设计阶段 | 设计规范 HDES-00252 |
| 使用材料批次 | HMAT-00417 | MaterialBatch | 材料阶段 | 材料批次 HMAT-00417 |
| 制造批次 | HMAN-01042 | ManufacturingBatch | 制造阶段 | 制造批次 HMAN-01042 |
| 装配记录 | HASM-01676 | AssemblyRecord | 装配阶段 | 装配记录 HASM-01676 |
| 检测记录 | HINSP-01676 | InspectionRecord | 检测阶段 | 检测记录 HINSP-01676 |
| 运维记录 | HOPR-01676 | MaintenanceRecord | 使用/运维阶段 | 运维记录 HOPR-01676 |
| 监测传感器 | PS1 | Sensor | RCA 诊断 | 传感器 PS1 |
| 监测传感器 | PS2 | Sensor | RCA 诊断 | 传感器 PS2 |
| 监测传感器 | PS6 | Sensor | RCA 诊断 | 传感器 PS6 |
| 监测传感器 | VS1 | Sensor | RCA 诊断 | 传感器 VS1 |
| 管路设计参数 | HPIP-00297 | PipeDesignParam | 设计阶段 | 管路设计参数 HPIP-00297 |
| has_component_code | C011 | ComponentCode | 未知阶段 | ComponentCode C011 |

## 5. 故障增强知识图谱概述
| graph | node_count | edge_count | added_node_count | added_edge_count |
| --- | --- | --- | --- | --- |
| fault_enhanced_graph | 63 | 77 | 27 | 38 |

故障增强图在原始生命周期链基础上新增 27 个节点，主要包括 ComponentDiagnosis, ConfidenceLevel, FaultEvolutionText, FaultPosition, FaultSeverity, FaultSubtype, FaultType, HydraulicComponent 等，用于承载故障、RCA、Top6候选和阶段归因信息。

## 6. 最终溯源原因表
综合置信度综合考虑模型推理结果、图谱路径证据、反馈对象与生命周期阶段之间的关联关系以及管路设计参数证据。
| 排名 | 原因阶段/类型 | 原因名称 | 关联对象 | 置信度 | 关键证据 | 建议措施 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 设计阶段 / 管路设计参数 | HPIP-00297 | PipeDesignParam | 0.96 | 该设计参数实体位于当前管路总成生命周期链上，属于设计阶段 PipeDesignParam 节点，系统识别其与管段长度、弯曲半径和弯曲角等设计参数复核任务相关。 | 建议优先复核管段长度L1/L2/L3、弯曲半径R和弯曲角θ1/θ2，并将上述参数提供给下游管路参数优化模块读取。 |
| 2 | 制造阶段 / ManufacturingBatch | HMAN-01042 | ManufacturingBatch | 0.8705 | 该实体位于当前反馈对象生命周期链上，属于制造阶段，具备直接路径证据。 | 建议结合制造阶段记录继续复核该候选原因。 |
| 3 | 使用/运维阶段 / MaintenanceRecord | HOPR-01651 | MaintenanceRecord | 0.6 | 该候选来自 RCA 根因部件生命周期链，属于使用/运维阶段。RCA 根因部件与反馈对象不完全一致时，可能表示故障传播或相邻部件诱发。 | 建议结合使用/运维阶段记录继续复核该候选原因。 |
| 4 | 材料阶段 / MaterialBatch | HMAT-00417 | MaterialBatch | 0.6 | 该实体位于当前反馈对象生命周期链上，属于材料阶段，具备直接路径证据。 | 建议结合材料阶段记录继续复核该候选原因。 |
| 5 | 装配阶段 / AssemblyRecord | HASM-01676 | AssemblyRecord | 0.6 | 该实体位于当前反馈对象生命周期链上，属于装配阶段，具备直接路径证据。 | 建议结合装配阶段记录继续复核该候选原因。 |
| 6 | 检测阶段 / InspectionRecord | HINSP-01676 | InspectionRecord | 0.6 | 该实体位于当前反馈对象生命周期链上，属于检测阶段，具备直接路径证据。 | 建议结合检测阶段记录继续复核该候选原因。 |

## 7. 生命周期阶段归因
- 首要阶段：设计阶段
- 反馈目标子系统：设计子系统
- 说明：设计阶段得分最高，主要因为 Top6 中 HPIP-00297 聚集在该阶段，且综合分数、路径分数或证据分数较高。

| stage | stage_name | score |
| --- | --- | --- |
| manufacturing | 制造阶段 | 1.0 |
| operation | 使用/运维阶段 | 0.589153 |
| material | 材料阶段 | 0.485119 |
| design | 设计阶段 | 0.427814 |
| assembly | 装配阶段 | 0.334815 |
| inspection | 检测阶段 | 0.284923 |

### 重点关注阶段 Top3
| rank | stage | stage_name | score | feedback_subsystem |
| --- | --- | --- | --- | --- |
| 1 | manufacturing | 制造阶段 | 1.0 | 制造工艺子系统 |
| 2 | operation | 使用/运维阶段 | 0.589153 | 运维保障子系统 |
| 3 | material | 材料阶段 | 0.485119 | 材料/供应商管理子系统 |

## 8. 关键证据链
- HFB-01676 -> occurs_on -> HCU-01676：质量反馈首先定位到发生故障的目标对象。
- HCU-01676 -> manufactured_in -> HMAN-01042：候选 HMAN-01042 位于目标对象生命周期链或其关键扩展路径上，path_score=1.0。
- HCU-01676 -> uses_material_batch -> HMAT-00417：候选 HMAT-00417 位于目标对象生命周期链或其关键扩展路径上，path_score=1.0。
- HFB-01676 -> may_caused_by -> HMAN-01042：TransH 综合重排序将 HMAN-01042 排为 Top1，final_score=0.8305。

## 9. 子系统反馈建议
- 目标子系统：设计子系统
- 首要反馈子系统：设计子系统
- 重点反馈子系统 Top3：设计子系统、制造工艺子系统、运维保障子系统
- 建议：建议反馈设计子系统，复核设计参数、裕度、结构约束。

## 10. 追溯结论
本次追溯的首要疑似原因是 PipeDesignParam，首要疑似阶段为设计阶段。主要证据为：该设计参数实体位于当前管路总成生命周期链上，属于设计阶段 PipeDesignParam 节点，系统识别其与管段长度、弯曲半径和弯曲角等设计参数复核任务相关。建议优先由设计子系统牵头处置，并将管段长度、弯曲半径和弯曲角等参数提供给下游管路参数优化模块读取。


### 生命周期阶段复核子系统参考
| stage | stage_name | score | feedback_subsystem |
| --- | --- | --- | --- |
| manufacturing | 制造阶段 | 1.0 | 制造工艺子系统 |
| operation | 使用/运维阶段 | 0.589153 | 运维保障子系统 |
| material | 材料阶段 | 0.485119 | 材料/供应商管理子系统 |
| design | 设计阶段 | 0.427814 | 设计子系统 |
| assembly | 装配阶段 | 0.334815 | 装配子系统 |
| inspection | 检测阶段 | 0.284923 | 检测子系统 |


## 11. 管路参数优化参考
- 关联对象：C011 管路总成
- 关联阶段：设计阶段
- 建议反馈子系统：设计子系统
- 说明：系统识别该质量反馈与管路总成设计参数相关，输出设计阶段参数复核建议，供下游管路参数优化模块读取。
- 下游读取变量：pipe_length_L1、pipe_length_L2、pipe_length_L3、bend_radius_R、bend_angle_theta1、bend_angle_theta2

参数复核建议表：
| 参数名称 | 参数类别 | 优先级 | 复核类型 | 复核原因 | 优化提示 |
| --- | --- | --- | --- | --- | --- |
| 管段长度L1 | 管段长度 | 低 | 常规复核 | 作为管路设计变量提供给下游系统读取。 | 建议复核L1与接口位置、装配空间及相邻部件间隙之间的匹配关系。 |
| 管段长度L2 | 管段长度 | 低 | 常规复核 | 作为管路设计变量提供给下游系统读取。 | 建议复核L2是否存在冗余长度、空间绕行或局部布置过紧问题。 |
| 管段长度L3 | 管段长度 | 中 | 建议复核 | 该反馈涉及管路总成，建议结合管段长度进行参数复核。 | 建议复核L3与相邻部件之间的间隙关系，避免装配干涉和振动风险。 |
| 弯曲半径R | 弯曲参数 | 高 | 重点复核 | 弯曲半径与管路流阻、急弯和空间走向密切相关。 | 建议复核弯曲半径R，必要时增大弯曲半径或调整管路走向。 |
| 第一弯曲角θ1 | 弯曲参数 | 中 | 建议复核 | 弯曲角可能影响急弯、空间干涉和装配可达性。 | 建议复核θ1，避免急弯、空间干涉或局部流阻增大。 |
| 第二弯曲角θ2 | 弯曲参数 | 中 | 建议复核 | 弯曲角可能影响管路空间走向和装配可达性。 | 建议复核θ2，优化管路空间走向和装配可达性。 |
