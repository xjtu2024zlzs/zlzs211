# 全生命周期质量追溯分析报告

## 1. 报告基本信息
- domain：bearing
- case_id：BFB-01676
- feedback_id：BFB-01676
- report_time：2026-06-07 20:25:55
- model_name：TransH
- top_k：6
- reasoning_json：D:\2.11\topic5_code\code2\transH\outputs\bearing\reports\reasoning_BFB-01676.json
- original_graph_json：D:\2.11\topic5_code\code2\transH\outputs\bearing\graphs\original_graph_BFB-01676.json
- fault_enhanced_graph_json：D:\2.11\topic5_code\code2\transH\outputs\bearing\graphs\fault_enhanced_graph_BFB-01676.json

## 2. 质量反馈概述
- 故障类型：rolling_element_fault
- 故障位置：rolling_element
- 严重程度：severe
- 诊断置信度：0.6278
- RCA置信度：0.6719
- 目标对象：BRG-01676（Bearing，BRG-01676）

## 3. 上游诊断/RCA结果
- RCA文本：该样本的生命周期风险在制造阶段较突出，能够解释当前严重滚动体故障的主要失效模式。
- 故障演化文本：早期表现为局部接触应力升高，随后振动冲击增强，最终演化为严重滚动体故障。

## 4. 原始生命周期知识图谱概述
| graph | node_count | edge_count | 包含生命周期阶段 |
| --- | --- | --- | --- |
| original_graph | 23 | 26 | 使用/运维阶段、装配阶段、设计阶段、检测阶段、制造阶段、材料阶段 |

目标对象生命周期节点摘要：
| relation | node_id | node_type | stage | node_name |
| --- | --- | --- | --- | --- |
| 设计依据 | BDES-00227 | BearingDesignSpec | 设计阶段 | 轴承设计规范 BDES-00227 |
| 使用材料批次 | BMAT-00431 | MaterialBatch | 材料阶段 | 材料批次 BMAT-00431 |
| 制造批次 | BMAN-00147 | ManufacturingBatch | 制造阶段 | 制造批次 BMAN-00147 |
| 装配记录 | BASM-01676 | AssemblyRecord | 装配阶段 | 装配记录 BASM-01676 |
| 检测记录 | BINSP-01676 | InspectionRecord | 检测阶段 | 检测记录 BINSP-01676 |
| 运维记录 | BOPR-01676 | MaintenanceRecord | 使用/运维阶段 | 运维记录 BOPR-01676 |
| has_overall_lifecycle_risk_level | RISK_LEVEL_medium | RiskLevel | 未知阶段 | RiskLevel RISK_LEVEL_medium |
| has_overall_lifecycle_risk_score | RISK_25_50 | RiskScore | 未知阶段 | RiskScore RISK_25_50 |

## 5. 故障增强知识图谱概述
| graph | node_count | edge_count | added_node_count | added_edge_count |
| --- | --- | --- | --- | --- |
| fault_enhanced_graph | 38 | 48 | 15 | 22 |

故障增强图在原始生命周期链基础上新增 15 个节点，主要包括 ConfidenceLevel, FaultEvolutionText, FaultPosition, FaultSeverity, FaultType, LifecycleStage, QualityFeedback, RcaText 等，用于承载故障、RCA、Top6候选和阶段归因信息。

## 6. TransH Top6疑似原因分析
| rank | candidate_id | candidate_type | stage | final | transh | path | evidence | prior | scope | description |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | BMAN-00147 | ManufacturingBatch | 制造阶段 | 0.8515 | 1.0 | 1.0 | 0.75 | 0.26 | lifecycle_candidate | 该实体位于当前反馈对象生命周期链上，属于制造阶段，具备直接路径证据。 |
| 2 | BMAT-00431 | MaterialBatch | 材料阶段 | 0.648658 | 0.606315 | 1.0 | 0.75 | 0.22 | lifecycle_candidate | 该实体位于当前反馈对象生命周期链上，属于材料阶段，具备直接路径证据。 |
| 3 | BOPR-01676 | MaintenanceRecord | 使用/运维阶段 | 0.607995 | 0.54899 | 1.0 | 0.75 | 0.14 | lifecycle_candidate | 该实体位于当前反馈对象生命周期链上，属于使用/运维阶段，具备直接路径证据。 |
| 4 | BASM-01676 | AssemblyRecord | 装配阶段 | 0.604428 | 0.541856 | 1.0 | 0.75 | 0.14 | lifecycle_candidate | 该实体位于当前反馈对象生命周期链上，属于装配阶段，具备直接路径证据。 |
| 5 | BDES-00227 | BearingDesignSpec | 设计阶段 | 0.542853 | 0.430706 | 1.0 | 0.75 | 0.1 | lifecycle_candidate | 该实体位于当前反馈对象生命周期链上，属于设计阶段，具备直接路径证据。 |
| 6 | BINSP-01676 | InspectionRecord | 检测阶段 | 0.532215 | 0.409431 | 1.0 | 0.75 | 0.1 | lifecycle_candidate | 该实体位于当前反馈对象生命周期链上，属于检测阶段，具备直接路径证据。 |

## 7. 生命周期阶段归因
- 首要阶段：制造阶段
- 反馈目标子系统：制造工艺子系统
- 说明：制造阶段得分最高，主要因为 Top6 中 BMAN-00147 聚集在该阶段，且综合分数、路径分数或证据分数较高。

| stage | stage_name | score |
| --- | --- | --- |
| manufacturing | 制造阶段 | 1.0 |
| material | 材料阶段 | 0.696467 |
| operation | 使用/运维阶段 | 0.587883 |
| assembly | 装配阶段 | 0.501577 |
| design | 设计阶段 | 0.370898 |
| inspection | 检测阶段 | 0.337578 |

### 重点关注阶段 Top3
| rank | stage | stage_name | score | feedback_subsystem |
| --- | --- | --- | --- | --- |
| 1 | manufacturing | 制造阶段 | 1.0 | 制造工艺子系统 |
| 2 | material | 材料阶段 | 0.696467 | 材料/供应商管理子系统 |
| 3 | operation | 使用/运维阶段 | 0.587883 | 运维保障子系统 |

## 8. 关键证据链
- BFB-01676 -> occurs_on -> BRG-01676：质量反馈首先定位到发生故障的目标对象。
- BRG-01676 -> manufactured_in -> BMAN-00147：候选 BMAN-00147 位于目标对象生命周期链或其关键扩展路径上，path_score=1.0。
- BRG-01676 -> uses_material_batch -> BMAT-00431：候选 BMAT-00431 位于目标对象生命周期链或其关键扩展路径上，path_score=1.0。
- BFB-01676 -> may_caused_by -> BMAN-00147：TransH 综合重排序将 BMAN-00147 排为 Top1，final_score=0.8515。

## 9. 子系统反馈建议
- 目标子系统：制造工艺子系统
- 首要反馈子系统：制造工艺子系统
- 重点反馈子系统 Top3：制造工艺子系统、材料/供应商管理子系统、运维保障子系统
- 建议：建议反馈制造工艺子系统，复核加工误差、表面粗糙度、洁净度、热处理或磨削过程。

## 10. 追溯结论
本次追溯的首要疑似原因是 BMAN-00147（ManufacturingBatch），首要疑似阶段为制造阶段。主要证据包括生命周期路径、TransH 评分、RCA/证据匹配以及规则先验，其中 TransH 综合重排序将 BMAN-00147 排为 Top1，final_score=0.8515。当前报告未发现明确跨部件传播描述，仍建议结合现场记录复核跨阶段影响。 建议优先由制造工艺子系统牵头处置，并联动相关生命周期环节复核。除首要疑似生命周期阶段外，系统同时给出重点关注阶段 Top3，用于支持跨阶段联合复核。建议优先反馈至制造工艺子系统，并联合材料/供应商管理子系统、运维保障子系统进行辅助复核。


### 生命周期阶段复核子系统参考
| stage | stage_name | score | feedback_subsystem |
| --- | --- | --- | --- |
| manufacturing | 制造阶段 | 1.0 | 制造工艺子系统 |
| material | 材料阶段 | 0.696467 | 材料/供应商管理子系统 |
| operation | 使用/运维阶段 | 0.587883 | 运维保障子系统 |
| assembly | 装配阶段 | 0.501577 | 装配子系统 |
| design | 设计阶段 | 0.370898 | 设计子系统 |
| inspection | 检测阶段 | 0.337578 | 检测子系统 |
