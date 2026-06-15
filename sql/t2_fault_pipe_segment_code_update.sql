-- Normalize the default reusable pipe segment to an engineering-style segment code.
-- This keeps existing databases aligned with the collaboration mechanism selector.

SET NAMES utf8mb4;

UPDATE t2_design_fault_pipe_parameter_set
SET set_code = 'HP-PIPE-SEG-001',
    set_name = 'HP-PIPE-SEG-001 原始设计参数',
    fault_segment_name = 'HP-PIPE-SEG-001',
    update_by = 'admin',
    update_time = SYSDATE()
WHERE set_code = 'FAULT_PIPE_DEFAULT_001';

UPDATE t2_design_fault_pipe_parameter_set
SET set_name = 'HP-PIPE-SEG-001 原始设计参数',
    fault_segment_name = 'HP-PIPE-SEG-001',
    update_by = 'admin',
    update_time = SYSDATE()
WHERE set_code = 'HP-PIPE-SEG-001'
  AND task_id IS NULL;

UPDATE t2_design_fault_pipe_parameter_item item
JOIN t2_design_fault_pipe_parameter_set param_set
  ON param_set.parameter_set_id = item.parameter_set_id
SET item.description = '管段材料名称',
    item.update_by = 'admin',
    item.update_time = SYSDATE()
WHERE param_set.set_code = 'HP-PIPE-SEG-001'
  AND item.param_code = 'MATERIAL_NAME'
  AND item.description = '故障管段材料名称';
