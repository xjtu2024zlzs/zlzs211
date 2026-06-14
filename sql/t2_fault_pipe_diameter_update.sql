-- Update the default fault-pipe geometry parameters for existing databases.

SET @fault_pipe_default_set_id = (
  SELECT parameter_set_id
  FROM t2_design_fault_pipe_parameter_set
  WHERE set_code = 'FAULT_PIPE_DEFAULT_001'
  LIMIT 1
);

INSERT INTO t2_design_fault_pipe_parameter_item
  (parameter_set_id, param_group, group_name, param_code, param_name, param_value, param_unit, value_type, formula_text, description, sort_order, status, create_by, create_time)
VALUES
  (@fault_pipe_default_set_id, 'geometry', '管段几何参数', 'PIPE_OUTER_DIAMETER', '管道外径', '9.53', 'mm', 'number', '', '当前设计管道外径', 110, '0', 'admin', SYSDATE()),
  (@fault_pipe_default_set_id, 'geometry', '管段几何参数', 'PIPE_WALL_THICKNESS', '管道壁厚', '0.9', 'mm', 'number', '', '当前设计管道壁厚', 120, '0', 'admin', SYSDATE()),
  (@fault_pipe_default_set_id, 'geometry', '管段几何参数', 'PIPE_INNER_DIAMETER', '管道内径', '7.73', 'mm', 'number', '9.53 - 2 * 0.9', '由外径减去两倍壁厚得到', 130, '0', 'admin', SYSDATE())
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
    WHEN 'INLET_PRESSURE_EXPRESSION' THEN 140
    WHEN 'INLET_PRESSURE_INITIAL' THEN 150
    WHEN 'INLET_PRESSURE_PEAK' THEN 160
    WHEN 'INLET_PRESSURE_RISE_TIME' THEN 170
    ELSE sort_order
  END,
  update_by = 'admin',
  update_time = SYSDATE()
WHERE parameter_set_id = @fault_pipe_default_set_id
  AND param_code IN (
    'INLET_PRESSURE_EXPRESSION',
    'INLET_PRESSURE_INITIAL',
    'INLET_PRESSURE_PEAK',
    'INLET_PRESSURE_RISE_TIME'
  );
