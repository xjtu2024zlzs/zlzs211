# hydraulic Top6追溯批量评估摘要

## 总体指标
- 评估样本数量：20
- 跳过/失败样本数量：0
- Top1命中率：1.0
- Top3命中率：1.0
- Top6命中率：1.0
- MRR：1.0
- 类型命中率：1.0
- 阶段归因准确率：0.45

## 不同 fault_type 的表现
| group | case_count | top1 | top3 | top6 | mrr | stage_acc |
| --- | --- | --- | --- | --- | --- | --- |
| flow_abnormal | 3 | 1.0 | 1.0 | 1.0 | 1.0 | 0.666667 |
| oil_leakage | 6 | 1.0 | 1.0 | 1.0 | 1.0 | 0.166667 |
| pressure_abnormal | 4 | 1.0 | 1.0 | 1.0 | 1.0 | 0.75 |
| temperature_abnormal | 1 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 |
| valve_stuck | 4 | 1.0 | 1.0 | 1.0 | 1.0 | 0.25 |
| vibration_abnormal | 2 | 1.0 | 1.0 | 1.0 | 1.0 | 0.5 |

## 不同 true_stage 的表现
| group | case_count | top1 | top3 | top6 | mrr | stage_acc |
| --- | --- | --- | --- | --- | --- | --- |
| assembly | 5 | 1.0 | 1.0 | 1.0 | 1.0 | 0.2 |
| design | 3 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 |
| inspection | 1 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 |
| manufacturing | 3 | 1.0 | 1.0 | 1.0 | 1.0 | 0.333333 |
| material | 1 | 1.0 | 1.0 | 1.0 | 1.0 | 0.0 |
| operation | 7 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 |

## 候选来源分布
| scope | count | ratio |
| --- | --- | --- |
| lifecycle_candidate | 57 | 0.475 |
| expanded_candidate | 20 | 0.166667 |
| rca_root_component_candidate | 4 | 0.033333 |
| rca_root_component_expanded_candidate | 0 | 0.0 |
| global_same_type_candidate | 39 | 0.325 |

## hydraulic RCA 根因部件候选作用
- RCA根因部件与反馈部件不一致比例：0.05
- Top6包含RCA根因部件候选比例：0.05
- 包含RCA根因部件候选时Top6命中率：1.0
- 不包含RCA根因部件候选时Top6命中率：1.0

## 简短结论
本次评估显示，hydraulic 入口的 Top6 命中率为 1.0，阶段归因准确率为 0.45。后续可结合错误样本进一步优化候选池、关系权重和重排序策略。
