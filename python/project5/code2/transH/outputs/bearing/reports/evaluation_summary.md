# bearing Top6追溯批量评估摘要

## 总体指标
- 评估样本数量：20
- 跳过/失败样本数量：600
- Top1命中率：1.0
- Top3命中率：1.0
- Top6命中率：1.0
- MRR：1.0
- 类型命中率：1.0
- 阶段归因准确率：0.85

## 不同 fault_type 的表现
| group | case_count | top1 | top3 | top6 | mrr | stage_acc |
| --- | --- | --- | --- | --- | --- | --- |
| inner_race_fault | 8 | 1.0 | 1.0 | 1.0 | 1.0 | 0.75 |
| outer_race_fault | 7 | 1.0 | 1.0 | 1.0 | 1.0 | 0.857143 |
| rolling_element_fault | 5 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 |

## 不同 true_stage 的表现
| group | case_count | top1 | top3 | top6 | mrr | stage_acc |
| --- | --- | --- | --- | --- | --- | --- |
| assembly | 5 | 1.0 | 1.0 | 1.0 | 1.0 | 0.8 |
| design | 1 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 |
| inspection | 2 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 |
| manufacturing | 9 | 1.0 | 1.0 | 1.0 | 1.0 | 1.0 |
| material | 3 | 1.0 | 1.0 | 1.0 | 1.0 | 0.333333 |

## 候选来源分布
| scope | count | ratio |
| --- | --- | --- |
| lifecycle_candidate | 104 | 0.866667 |
| expanded_candidate | 0 | 0.0 |
| rca_root_component_candidate | 0 | 0.0 |
| rca_root_component_expanded_candidate | 0 | 0.0 |
| global_same_type_candidate | 16 | 0.133333 |

## 简短结论
本次评估显示，bearing 入口的 Top6 命中率为 1.0，阶段归因准确率为 0.85。后续可结合错误样本进一步优化候选池、关系权重和重排序策略。
