-- Fault pipe original design parameter initialization for t2_ tables.
-- These parameters are fixed simulation/solver inputs, not design variables.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS t2_design_fault_pipe_parameter_set (
  parameter_set_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Parameter set ID',
  task_id bigint(20) DEFAULT NULL COMMENT 'Task ID, null means reusable default set',
  set_code varchar(80) NOT NULL COMMENT 'Parameter set code',
  set_name varchar(200) NOT NULL COMMENT 'Parameter set name',
  fault_segment_name varchar(200) DEFAULT NULL COMMENT 'Fault pipe segment name',
  material_name varchar(100) DEFAULT NULL COMMENT 'Material name',
  source_type varchar(40) DEFAULT 'database' COMMENT 'Source type',
  is_default char(1) DEFAULT '0' COMMENT 'Default set flag',
  status char(1) DEFAULT '0' COMMENT 'Status',
  remark varchar(500) DEFAULT NULL COMMENT 'Remark',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  update_by varchar(64) DEFAULT '',
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (parameter_set_id),
  UNIQUE KEY uk_t2_fault_pipe_param_set_code (set_code),
  KEY idx_t2_fault_pipe_param_set_task (task_id),
  KEY idx_t2_fault_pipe_param_set_default (is_default, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Fault pipe original design parameter set';

CREATE TABLE IF NOT EXISTS t2_design_fault_pipe_parameter_item (
  item_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Parameter item ID',
  parameter_set_id bigint(20) NOT NULL COMMENT 'Parameter set ID',
  param_group varchar(60) NOT NULL COMMENT 'Parameter group code',
  group_name varchar(100) NOT NULL COMMENT 'Parameter group name',
  param_code varchar(100) NOT NULL COMMENT 'Parameter code',
  param_name varchar(200) NOT NULL COMMENT 'Parameter name',
  param_value varchar(500) DEFAULT NULL COMMENT 'Parameter value',
  param_unit varchar(50) DEFAULT NULL COMMENT 'Parameter unit',
  value_type varchar(40) DEFAULT 'number' COMMENT 'Value type',
  formula_text varchar(1000) DEFAULT NULL COMMENT 'Formula text',
  description varchar(500) DEFAULT NULL COMMENT 'Description',
  sort_order int(4) DEFAULT 0 COMMENT 'Sort order',
  status char(1) DEFAULT '0' COMMENT 'Status',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  update_by varchar(64) DEFAULT '',
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (item_id),
  UNIQUE KEY uk_t2_fault_pipe_param_item_code (parameter_set_id, param_code),
  KEY idx_t2_fault_pipe_param_item_set (parameter_set_id),
  KEY idx_t2_fault_pipe_param_item_group (param_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Fault pipe original design parameter item';

INSERT INTO t2_design_fault_pipe_parameter_set
  (set_code, set_name, fault_segment_name, material_name, source_type, is_default, status, remark, create_by, create_time)
VALUES
  ('FAULT_PIPE_DEFAULT_001', '故障管段默认原始设计参数', '故障液压弯管段', '不锈钢', 'database', '1', '0', '材料属性与入口压强载荷谱默认值', 'admin', SYSDATE())
ON DUPLICATE KEY UPDATE
  set_name = VALUES(set_name),
  fault_segment_name = VALUES(fault_segment_name),
  material_name = VALUES(material_name),
  source_type = VALUES(source_type),
  is_default = VALUES(is_default),
  status = VALUES(status),
  remark = VALUES(remark),
  update_by = 'admin',
  update_time = SYSDATE();

SET @fault_pipe_default_set_id = (
  SELECT parameter_set_id
  FROM t2_design_fault_pipe_parameter_set
  WHERE set_code = 'FAULT_PIPE_DEFAULT_001'
  LIMIT 1
);

INSERT INTO t2_design_fault_pipe_parameter_item
  (parameter_set_id, param_group, group_name, param_code, param_name, param_value, param_unit, value_type, formula_text, description, sort_order, status, create_by, create_time)
VALUES
  (@fault_pipe_default_set_id, 'material', '材料属性', 'MATERIAL_NAME', '材料', '不锈钢', '', 'text', '', '故障管段材料名称', 10, '0', 'admin', SYSDATE()),
  (@fault_pipe_default_set_id, 'material', '材料属性', 'MATERIAL_DENSITY', '密度', '7750', 'kg*m^-3', 'number', '', '材料密度', 20, '0', 'admin', SYSDATE()),
  (@fault_pipe_default_set_id, 'material', '材料属性', 'THERMAL_EXPANSION_COEFFICIENT', '热膨胀系数', '1.7E-05', 'C^-1', 'number', '', '热膨胀系数', 30, '0', 'admin', SYSDATE()),
  (@fault_pipe_default_set_id, 'material', '材料属性', 'YOUNG_MODULUS', '杨氏模量', '1.93E+11', 'Pa', 'number', '', '弹性模量', 40, '0', 'admin', SYSDATE()),
  (@fault_pipe_default_set_id, 'material', '材料属性', 'POISSON_RATIO', '泊松比', '0.31', '', 'number', '', '泊松比', 50, '0', 'admin', SYSDATE()),
  (@fault_pipe_default_set_id, 'material', '材料属性', 'BULK_MODULUS', '体积模量', '1.693E+11', 'Pa', 'number', '', '体积模量', 60, '0', 'admin', SYSDATE()),
  (@fault_pipe_default_set_id, 'material', '材料属性', 'SHEAR_MODULUS', '剪切模量', '7.3664E+10', 'Pa', 'number', '', '剪切模量', 70, '0', 'admin', SYSDATE()),
  (@fault_pipe_default_set_id, 'material', '材料属性', 'TENSILE_YIELD_STRENGTH', '拉伸屈服强度', '2.07E+08', 'Pa', 'number', '', '拉伸屈服强度，可作为许用应力来源', 80, '0', 'admin', SYSDATE()),
  (@fault_pipe_default_set_id, 'material', '材料属性', 'COMPRESSIVE_YIELD_STRENGTH', '压缩屈服强度', '2.07E+08', 'Pa', 'number', '', '压缩屈服强度', 90, '0', 'admin', SYSDATE()),
  (@fault_pipe_default_set_id, 'material', '材料属性', 'TENSILE_ULTIMATE_STRENGTH', '拉伸极限强度', '5.86E+08', 'Pa', 'number', '', '拉伸极限强度', 100, '0', 'admin', SYSDATE()),
  (@fault_pipe_default_set_id, 'geometry', '管段几何参数', 'PIPE_OUTER_DIAMETER', '管道外径', '9.53', 'mm', 'number', '', '当前设计管道外径', 110, '0', 'admin', SYSDATE()),
  (@fault_pipe_default_set_id, 'geometry', '管段几何参数', 'PIPE_WALL_THICKNESS', '管道壁厚', '0.9', 'mm', 'number', '', '当前设计管道壁厚', 120, '0', 'admin', SYSDATE()),
  (@fault_pipe_default_set_id, 'geometry', '管段几何参数', 'PIPE_INNER_DIAMETER', '管道内径', '7.73', 'mm', 'number', '9.53 - 2 * 0.9', '由外径减去两倍壁厚得到', 130, '0', 'admin', SYSDATE()),
  (@fault_pipe_default_set_id, 'pressure_load', '入口压强载荷', 'INLET_PRESSURE_EXPRESSION', '入口压强表达式', 'IF(t <= 0.001, 101325 + (30000000 - 101325) * t / 0.001, 30000000)', 'Pa', 'formula', 'IF(t <= 0.001, 101325 + (30000000 - 101325) * t / 0.001, 30000000)', '0 到 0.001 秒线性升压，之后保持峰值压强', 140, '0', 'admin', SYSDATE()),
  (@fault_pipe_default_set_id, 'pressure_load', '入口压强载荷', 'INLET_PRESSURE_INITIAL', '初始压强', '101325', 'Pa', 'number', '', '入口初始压强', 150, '0', 'admin', SYSDATE()),
  (@fault_pipe_default_set_id, 'pressure_load', '入口压强载荷', 'INLET_PRESSURE_PEAK', '峰值压强', '30000000', 'Pa', 'number', '', '入口峰值压强', 160, '0', 'admin', SYSDATE()),
  (@fault_pipe_default_set_id, 'pressure_load', '入口压强载荷', 'INLET_PRESSURE_RISE_TIME', '上升时间', '0.001', 's', 'number', '', '压强从初始值升至峰值所需时间', 170, '0', 'admin', SYSDATE())
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
