-- Pre-decomposition cleanup.
-- Use this when objective/constraint selection happens before task decomposition.

SET NAMES utf8mb4;

UPDATE t2_design_objective_catalog
SET status = '1',
    update_by = 'admin',
    update_time = SYSDATE()
WHERE item_code = 'LAY_HYDRAULIC_PIPE_FIXED';

DELETE FROM t2_design_objective_constraint
WHERE item_code = 'LAY_HYDRAULIC_PIPE_FIXED';

UPDATE t2_design_task
SET description = '基于前置故障追因结论，当前任务先由各专业工程师选择与舱门管线问题相关的优化目标、约束条件和必要设计变量。任务解耦将在目标与约束选择完成后执行，解耦前不预先固定子任务关系或上下游边界。',
    update_by = 'admin',
    update_time = SYSDATE()
WHERE description LIKE '%任务拆分为%'
   OR description LIKE '%两个子任务%'
   OR description LIKE '%布局子任务%'
   OR description LIKE '%固定边界%';
