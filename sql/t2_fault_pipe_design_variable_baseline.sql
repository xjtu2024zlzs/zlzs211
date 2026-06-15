-- Add baseline pipe design variables into all active fault pipe parameter sets.
-- This includes reusable default sets and task-specific snapshots already copied from them.

SET NAMES utf8mb4;

INSERT INTO t2_design_fault_pipe_parameter_item
  (parameter_set_id, param_group, group_name, param_code, param_name, param_value, param_unit, value_type, formula_text, description, sort_order, status, create_by, create_time)
SELECT parameter_set_id, 'design_variable_baseline', '管道设计变量基准', 'PIPE_L1', 'L1 第一段直管长度', '280', 'mm', 'number', '', '优化前管道设计变量基准值', 140, '0', 'admin', SYSDATE()
FROM t2_design_fault_pipe_parameter_set
WHERE status = '0'
ON DUPLICATE KEY UPDATE
  param_group = VALUES(param_group),
  group_name = VALUES(group_name),
  param_name = VALUES(param_name),
  param_value = VALUES(param_value),
  param_unit = VALUES(param_unit),
  value_type = VALUES(value_type),
  formula_text = VALUES(formula_text),
  description = VALUES(description),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  update_by = 'admin',
  update_time = SYSDATE();

INSERT INTO t2_design_fault_pipe_parameter_item
  (parameter_set_id, param_group, group_name, param_code, param_name, param_value, param_unit, value_type, formula_text, description, sort_order, status, create_by, create_time)
SELECT parameter_set_id, 'design_variable_baseline', '管道设计变量基准', 'PIPE_L2', 'L2 第二段直管长度', '150', 'mm', 'number', '', '优化前管道设计变量基准值', 150, '0', 'admin', SYSDATE()
FROM t2_design_fault_pipe_parameter_set
WHERE status = '0'
ON DUPLICATE KEY UPDATE
  param_group = VALUES(param_group),
  group_name = VALUES(group_name),
  param_name = VALUES(param_name),
  param_value = VALUES(param_value),
  param_unit = VALUES(param_unit),
  value_type = VALUES(value_type),
  formula_text = VALUES(formula_text),
  description = VALUES(description),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  update_by = 'admin',
  update_time = SYSDATE();

INSERT INTO t2_design_fault_pipe_parameter_item
  (parameter_set_id, param_group, group_name, param_code, param_name, param_value, param_unit, value_type, formula_text, description, sort_order, status, create_by, create_time)
SELECT parameter_set_id, 'design_variable_baseline', '管道设计变量基准', 'PIPE_THETA_1', 'θ1 第一个弯角弯曲角度', '110', 'deg', 'number', '', '优化前管道设计变量基准值', 160, '0', 'admin', SYSDATE()
FROM t2_design_fault_pipe_parameter_set
WHERE status = '0'
ON DUPLICATE KEY UPDATE
  param_group = VALUES(param_group),
  group_name = VALUES(group_name),
  param_name = VALUES(param_name),
  param_value = VALUES(param_value),
  param_unit = VALUES(param_unit),
  value_type = VALUES(value_type),
  formula_text = VALUES(formula_text),
  description = VALUES(description),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  update_by = 'admin',
  update_time = SYSDATE();

INSERT INTO t2_design_fault_pipe_parameter_item
  (parameter_set_id, param_group, group_name, param_code, param_name, param_value, param_unit, value_type, formula_text, description, sort_order, status, create_by, create_time)
SELECT parameter_set_id, 'design_variable_baseline', '管道设计变量基准', 'PIPE_THETA_2', 'θ2 第二个弯角弯曲角度', '120', 'deg', 'number', '', '优化前管道设计变量基准值', 170, '0', 'admin', SYSDATE()
FROM t2_design_fault_pipe_parameter_set
WHERE status = '0'
ON DUPLICATE KEY UPDATE
  param_group = VALUES(param_group),
  group_name = VALUES(group_name),
  param_name = VALUES(param_name),
  param_value = VALUES(param_value),
  param_unit = VALUES(param_unit),
  value_type = VALUES(value_type),
  formula_text = VALUES(formula_text),
  description = VALUES(description),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  update_by = 'admin',
  update_time = SYSDATE();

INSERT INTO t2_design_fault_pipe_parameter_item
  (parameter_set_id, param_group, group_name, param_code, param_name, param_value, param_unit, value_type, formula_text, description, sort_order, status, create_by, create_time)
SELECT parameter_set_id, 'design_variable_baseline', '管道设计变量基准', 'PIPE_BEND_RADIUS', 'R 两处弯管圆角半径', '20', 'mm', 'number', '', '优化前管道设计变量基准值', 180, '0', 'admin', SYSDATE()
FROM t2_design_fault_pipe_parameter_set
WHERE status = '0'
ON DUPLICATE KEY UPDATE
  param_group = VALUES(param_group),
  group_name = VALUES(group_name),
  param_name = VALUES(param_name),
  param_value = VALUES(param_value),
  param_unit = VALUES(param_unit),
  value_type = VALUES(value_type),
  formula_text = VALUES(formula_text),
  description = VALUES(description),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  update_by = 'admin',
  update_time = SYSDATE();

UPDATE t2_design_fault_pipe_parameter_item
SET sort_order = CASE param_code
    WHEN 'INLET_PRESSURE_EXPRESSION' THEN 210
    WHEN 'INLET_PRESSURE_INITIAL' THEN 220
    WHEN 'INLET_PRESSURE_PEAK' THEN 230
    WHEN 'INLET_PRESSURE_RISE_TIME' THEN 240
    ELSE sort_order
  END,
  update_by = 'admin',
  update_time = SYSDATE()
WHERE param_code IN (
  'INLET_PRESSURE_EXPRESSION',
  'INLET_PRESSURE_INITIAL',
  'INLET_PRESSURE_PEAK',
  'INLET_PRESSURE_RISE_TIME'
);
