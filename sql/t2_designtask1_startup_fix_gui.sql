-- GUI friendly startup fix for t2_ tables.
-- Use this file in DBeaver/Navicat/DataGrip when DELIMITER/PROCEDURE execution fails.
-- It does not rename old tables. Run it after t2 tables exist or for a fresh t2 schema.

CREATE TABLE IF NOT EXISTS t2_design_task (
  task_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Task ID',
  task_name varchar(200) NOT NULL COMMENT 'Task name',
  task_no varchar(50) DEFAULT NULL COMMENT 'Task number',
  task_type varchar(20) NOT NULL COMMENT 'Task type',
  template_id bigint(20) DEFAULT NULL COMMENT 'Template ID',
  priority int(2) DEFAULT 2 COMMENT 'Priority',
  description text COMMENT 'Description',
  status varchar(20) DEFAULT 'DRAFT' COMMENT 'Task status',
  responsible_role varchar(50) DEFAULT NULL COMMENT 'Responsible role',
  collab_roles varchar(200) DEFAULT NULL COMMENT 'Collaboration roles',
  process_definition_id varchar(128) DEFAULT NULL COMMENT 'Flowable process definition ID',
  process_instance_id varchar(128) DEFAULT NULL COMMENT 'Flowable process instance ID',
  current_flowable_task_id varchar(128) DEFAULT NULL COMMENT 'Current Flowable task ID',
  current_node_key varchar(100) DEFAULT NULL COMMENT 'Current node key',
  current_node_name varchar(100) DEFAULT NULL COMMENT 'Current node name',
  owner_user_id bigint(20) DEFAULT NULL COMMENT 'Owner user ID',
  owner_user_name varchar(100) DEFAULT NULL COMMENT 'Owner user name',
  structure_user_id bigint(20) DEFAULT NULL COMMENT 'Structure engineer user ID',
  layout_user_id bigint(20) DEFAULT NULL COMMENT 'Layout engineer user ID',
  aero_user_id bigint(20) DEFAULT NULL COMMENT 'Aero engineer user ID',
  hydraulic_user_id bigint(20) DEFAULT NULL COMMENT 'Hydraulic engineer user ID',
  manufacturing_user_id bigint(20) DEFAULT NULL COMMENT 'Manufacturing engineer user ID',
  design_user_id bigint(20) DEFAULT NULL COMMENT 'Design engineer user ID',
  leader_user_id bigint(20) DEFAULT NULL COMMENT 'Approval leader user ID',
  plan_start_time datetime DEFAULT NULL COMMENT 'Planned start time',
  expected_end_time datetime DEFAULT NULL COMMENT 'Expected end time',
  create_by varchar(64) DEFAULT '' COMMENT 'Created by',
  create_time datetime DEFAULT NULL COMMENT 'Created time',
  update_by varchar(64) DEFAULT '' COMMENT 'Updated by',
  update_time datetime DEFAULT NULL COMMENT 'Updated time',
  del_flag char(1) DEFAULT '0' COMMENT 'Delete flag',
  PRIMARY KEY (task_id),
  KEY idx_t2_design_task_status (status),
  KEY idx_t2_design_task_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design task';

CREATE TABLE IF NOT EXISTS t2_design_template (
  template_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Template ID',
  template_name varchar(100) NOT NULL COMMENT 'Template name',
  template_desc varchar(500) DEFAULT NULL COMMENT 'Template description',
  template_config text COMMENT 'Template JSON',
  is_default char(1) DEFAULT '0' COMMENT 'Default flag',
  status char(1) DEFAULT '0' COMMENT 'Status',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  update_by varchar(64) DEFAULT '',
  update_time datetime DEFAULT NULL,
  remark varchar(500) DEFAULT NULL,
  PRIMARY KEY (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design template';

CREATE TABLE IF NOT EXISTS t2_design_template_node (
  node_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Node ID',
  template_id bigint(20) NOT NULL COMMENT 'Template ID',
  node_code varchar(50) NOT NULL COMMENT 'Node code',
  node_name varchar(100) NOT NULL COMMENT 'Node name',
  node_order int(4) DEFAULT 0 COMMENT 'Node order',
  parallel_group varchar(50) DEFAULT NULL COMMENT 'Parallel group',
  responsible_role varchar(50) DEFAULT NULL COMMENT 'Responsible role',
  time_limit int(4) DEFAULT NULL COMMENT 'Time limit',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  update_by varchar(64) DEFAULT '',
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (node_id),
  UNIQUE KEY uk_t2_template_node (template_id, node_code),
  KEY idx_t2_template_node_template (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design template node';

CREATE TABLE IF NOT EXISTS t2_design_template_node_role (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  node_id bigint(20) NOT NULL COMMENT 'Node ID',
  role_code varchar(50) NOT NULL COMMENT 'Role code',
  role_name varchar(100) DEFAULT NULL COMMENT 'Role name',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_t2_template_node_role_node (node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design template node role';

CREATE TABLE IF NOT EXISTS t2_design_task_node (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Instance ID',
  task_id bigint(20) NOT NULL COMMENT 'Task ID',
  template_node_id bigint(20) NOT NULL COMMENT 'Template node ID',
  node_code varchar(50) NOT NULL COMMENT 'Node code',
  node_name varchar(100) NOT NULL COMMENT 'Node name',
  node_order int(4) DEFAULT 0 COMMENT 'Node order',
  parallel_group varchar(50) DEFAULT NULL COMMENT 'Parallel group',
  status varchar(20) DEFAULT 'PENDING' COMMENT 'Status',
  start_time datetime DEFAULT NULL COMMENT 'Start time',
  end_time datetime DEFAULT NULL COMMENT 'End time',
  assignee_id bigint(20) DEFAULT NULL COMMENT 'Assignee ID',
  assignee_name varchar(100) DEFAULT NULL COMMENT 'Assignee name',
  result text COMMENT 'Result',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  update_by varchar(64) DEFAULT '',
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_t2_task_node_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design task node';

CREATE TABLE IF NOT EXISTS t2_design_task_file (
  file_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'File ID',
  task_id bigint(20) NOT NULL COMMENT 'Task ID',
  file_name varchar(255) NOT NULL COMMENT 'File name',
  file_path varchar(500) NOT NULL COMMENT 'File path',
  file_size bigint(20) DEFAULT NULL COMMENT 'File size',
  file_type varchar(128) DEFAULT NULL COMMENT 'File type',
  file_suffix varchar(64) DEFAULT NULL COMMENT 'File suffix',
  upload_by varchar(64) DEFAULT '' COMMENT 'Uploaded by',
  upload_time datetime DEFAULT NULL COMMENT 'Uploaded time',
  PRIMARY KEY (file_id),
  KEY idx_t2_task_file_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design task file';


CREATE TABLE IF NOT EXISTS t2_design_task_log (
  log_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Log ID',
  task_id bigint(20) NOT NULL COMMENT 'Task ID',
  node_id bigint(20) DEFAULT NULL COMMENT 'Node ID',
  action varchar(50) NOT NULL COMMENT 'Action',
  content text COMMENT 'Content',
  operator_id bigint(20) DEFAULT NULL COMMENT 'Operator ID',
  operator_name varchar(100) DEFAULT NULL COMMENT 'Operator name',
  create_time datetime DEFAULT NULL COMMENT 'Created time',
  PRIMARY KEY (log_id),
  KEY idx_t2_task_log_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design task log';

CREATE TABLE IF NOT EXISTS t2_design_objective_catalog (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  discipline varchar(50) NOT NULL COMMENT 'Discipline',
  discipline_name varchar(80) NOT NULL COMMENT 'Discipline name',
  item_type varchar(20) NOT NULL COMMENT 'objective/constraint',
  item_code varchar(80) NOT NULL COMMENT 'Item code',
  item_name varchar(200) NOT NULL COMMENT 'Item name',
  direction varchar(30) DEFAULT NULL COMMENT 'Direction',
  default_weight int(3) DEFAULT 50 COMMENT 'Default weight',
  default_limit_value varchar(100) DEFAULT NULL COMMENT 'Default limit',
  unit varchar(50) DEFAULT NULL COMMENT 'Unit',
  sort_order int(4) DEFAULT 0 COMMENT 'Sort order',
  status char(1) DEFAULT '0' COMMENT 'Status',
  remark varchar(500) DEFAULT NULL COMMENT 'Remark',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  update_by varchar(64) DEFAULT '',
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_t2_objective_catalog_code (item_code),
  KEY idx_t2_objective_catalog_discipline (discipline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design objective catalog';

CREATE TABLE IF NOT EXISTS t2_design_objective_constraint (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  task_id bigint(20) NOT NULL COMMENT 'Task ID',
  discipline varchar(50) NOT NULL COMMENT 'Discipline',
  item_type varchar(20) NOT NULL COMMENT 'objective/constraint',
  item_code varchar(80) NOT NULL COMMENT 'Item code',
  item_name varchar(200) NOT NULL COMMENT 'Item name',
  direction varchar(30) DEFAULT NULL COMMENT 'Direction',
  weight int(3) DEFAULT NULL COMMENT 'Weight',
  limit_value varchar(100) DEFAULT NULL COMMENT 'Limit value',
  unit varchar(50) DEFAULT NULL COMMENT 'Unit',
  remark varchar(500) DEFAULT NULL COMMENT 'Remark',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_t2_objective_constraint_task (task_id),
  KEY idx_t2_objective_constraint_discipline (discipline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design objective constraint';

CREATE TABLE IF NOT EXISTS t2_design_variable_catalog (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  discipline varchar(50) NOT NULL COMMENT 'Discipline',
  discipline_name varchar(80) NOT NULL COMMENT 'Discipline name',
  subtask_code varchar(80) DEFAULT NULL COMMENT 'Subtask code',
  variable_code varchar(80) NOT NULL COMMENT 'Variable code',
  variable_name varchar(200) NOT NULL COMMENT 'Variable name',
  variable_type varchar(30) DEFAULT 'continuous' COMMENT 'Variable type',
  default_value varchar(100) DEFAULT NULL COMMENT 'Default value',
  lower_bound varchar(100) DEFAULT NULL COMMENT 'Lower bound',
  upper_bound varchar(100) DEFAULT NULL COMMENT 'Upper bound',
  step_value varchar(100) DEFAULT NULL COMMENT 'Step value',
  unit varchar(50) DEFAULT NULL COMMENT 'Unit',
  sort_order int(4) DEFAULT 0 COMMENT 'Sort order',
  status char(1) DEFAULT '0' COMMENT 'Status',
  remark varchar(500) DEFAULT NULL COMMENT 'Remark',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  update_by varchar(64) DEFAULT '',
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_t2_variable_catalog_code (variable_code),
  KEY idx_t2_variable_catalog_discipline (discipline),
  KEY idx_t2_variable_catalog_subtask (subtask_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design variable catalog';

CREATE TABLE IF NOT EXISTS t2_design_task_variable_selection (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  task_id bigint(20) NOT NULL COMMENT 'Task ID',
  discipline varchar(50) NOT NULL COMMENT 'Discipline',
  subtask_code varchar(80) DEFAULT NULL COMMENT 'Subtask code',
  variable_code varchar(80) NOT NULL COMMENT 'Variable code',
  variable_name varchar(200) NOT NULL COMMENT 'Variable name',
  variable_type varchar(30) DEFAULT 'continuous' COMMENT 'Variable type',
  initial_value varchar(100) DEFAULT NULL COMMENT 'Initial value',
  lower_bound varchar(100) DEFAULT NULL COMMENT 'Lower bound',
  upper_bound varchar(100) DEFAULT NULL COMMENT 'Upper bound',
  step_value varchar(100) DEFAULT NULL COMMENT 'Step value',
  unit varchar(50) DEFAULT NULL COMMENT 'Unit',
  remark varchar(500) DEFAULT NULL COMMENT 'Remark',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_t2_task_variable_task (task_id),
  KEY idx_t2_task_variable_discipline (discipline),
  KEY idx_t2_task_variable_code (variable_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design task variable selection';

CREATE TABLE IF NOT EXISTS t2_design_conflict_check (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  task_id bigint(20) NOT NULL COMMENT 'Task ID',
  passed char(1) DEFAULT '1' COMMENT 'Passed',
  score int(3) DEFAULT NULL COMMENT 'Score',
  conflicts_json text COMMENT 'Conflicts JSON',
  suggestions_json text COMMENT 'Suggestions JSON',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_t2_conflict_check_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design conflict check';

CREATE TABLE IF NOT EXISTS t2_design_subtask_solution (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  task_id bigint(20) NOT NULL COMMENT 'Task ID',
  subtask_code varchar(80) NOT NULL COMMENT 'Subtask code',
  subtask_name varchar(200) NOT NULL COMMENT 'Subtask name',
  params_json text COMMENT 'Params JSON',
  result_json text COMMENT 'Result JSON',
  recommended_solution varchar(80) DEFAULT NULL COMMENT 'Recommended solution',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_t2_subtask_solution_task (task_id),
  KEY idx_t2_subtask_solution_code (subtask_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design subtask solution';

CREATE TABLE IF NOT EXISTS t2_design_simulation_result (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  task_id bigint(20) NOT NULL COMMENT 'Task ID',
  passed char(1) DEFAULT '1' COMMENT 'Passed',
  metrics_json text COMMENT 'Metrics JSON',
  conclusion varchar(500) DEFAULT NULL COMMENT 'Conclusion',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_t2_simulation_result_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design simulation result';

CREATE TABLE IF NOT EXISTS t2_design_approval_record (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  task_id bigint(20) NOT NULL COMMENT 'Task ID',
  approved char(1) DEFAULT '1' COMMENT 'Approved',
  comment varchar(1000) DEFAULT NULL COMMENT 'Approval comment',
  approve_by varchar(64) DEFAULT '',
  approve_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_t2_approval_record_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design approval record';

CREATE TABLE IF NOT EXISTS t2_design_task_archive (
  archive_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Archive ID',
  task_id bigint(20) NOT NULL COMMENT 'Task ID',
  archive_code varchar(80) NOT NULL COMMENT 'Archive code',
  archive_title varchar(300) NOT NULL COMMENT 'Archive title',
  archive_status varchar(30) DEFAULT 'ARCHIVED' COMMENT 'Archive status',
  archive_json longtext COMMENT 'Archive snapshot JSON',
  archive_time datetime DEFAULT NULL COMMENT 'Archive time',
  archive_by varchar(64) DEFAULT '' COMMENT 'Archive user',
  export_file_id bigint(20) DEFAULT NULL COMMENT 'Export file ID',
  create_time datetime DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (archive_id),
  UNIQUE KEY uk_t2_task_archive_task (task_id),
  KEY idx_t2_task_archive_code (archive_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design task archive';

CREATE TABLE IF NOT EXISTS t2_design_resource (
  resource_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Resource ID',
  resource_name varchar(200) NOT NULL COMMENT 'Resource name',
  category varchar(80) DEFAULT NULL COMMENT 'Category',
  version varchar(40) DEFAULT NULL COMMENT 'Version',
  file_type varchar(40) DEFAULT NULL COMMENT 'File type',
  file_path varchar(500) DEFAULT NULL COMMENT 'File path',
  description varchar(500) DEFAULT NULL COMMENT 'Description',
  status char(1) DEFAULT '0' COMMENT 'Status',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  update_by varchar(64) DEFAULT '',
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design resource';

INSERT INTO t2_design_template
  (template_id, template_name, template_desc, template_config, is_default, status, create_by, create_time, remark)
VALUES
  (1, '标准协同优化流程', '液压弯管抗冲击优化与线缆管路布局设计两子任务流程',
   '[{"nodeCode":"objective_select","nodeName":"目标约束选择"},{"nodeCode":"conflict_check","nodeName":"冲突校验"},{"nodeCode":"model_decompose_solve","nodeName":"模型分解求解"},{"nodeCode":"simulation_verify","nodeName":"仿真验证"},{"nodeCode":"approval","nodeName":"审批归档"}]',
   '1', '0', 'admin', SYSDATE(), 'designtask1 default template')
ON DUPLICATE KEY UPDATE
  template_name = VALUES(template_name),
  template_desc = VALUES(template_desc),
  template_config = VALUES(template_config),
  is_default = VALUES(is_default),
  status = VALUES(status),
  update_by = 'admin',
  update_time = SYSDATE();

INSERT INTO t2_design_template_node
  (template_id, node_code, node_name, node_order, responsible_role, create_by, create_time)
VALUES
  (1, 'objective_select', '目标约束选择', 1, 'structure_engineer', 'admin', SYSDATE()),
  (1, 'conflict_check', '冲突校验', 2, 'design_task_owner', 'admin', SYSDATE()),
  (1, 'model_decompose_solve', '模型分解求解', 3, 'design_task_owner', 'admin', SYSDATE()),
  (1, 'simulation_verify', '仿真验证', 4, 'manufacturing_engineer', 'admin', SYSDATE()),
  (1, 'approval', '审批归档', 5, 'approval_leader', 'admin', SYSDATE())
ON DUPLICATE KEY UPDATE
  node_name = VALUES(node_name),
  node_order = VALUES(node_order),
  responsible_role = VALUES(responsible_role),
  update_by = 'admin',
  update_time = SYSDATE();

UPDATE t2_design_variable_catalog
SET status = '1', update_by = 'admin', update_time = SYSDATE()
WHERE variable_code NOT IN (
  'PIPE_L1', 'PIPE_L2', 'PIPE_BEND_RADIUS', 'PIPE_THETA_1', 'PIPE_THETA_2',
  'CABLE_ROUTE_SIDE', 'CABLE_PIPE_CLEARANCE', 'CABLE_OFFSET',
  'CABLE_BEND_RADIUS', 'CLAMP_SPACING', 'SERVICE_MARGIN'
);

INSERT INTO t2_design_variable_catalog
  (discipline, discipline_name, subtask_code, variable_code, variable_name, variable_type, default_value, lower_bound, upper_bound, step_value, unit, sort_order, status, remark, create_by, create_time)
VALUES
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_L1', 'L1 第一段直管长度', 'continuous', '300', '50', '550', '1', 'mm', 10, '0', '液压弯管抗冲击优化几何变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_L2', 'L2 第二段直管长度', 'continuous', '150', '50', '300', '1', 'mm', 20, '0', '液压弯管抗冲击优化几何变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_BEND_RADIUS', 'R 两处弯管圆角半径', 'continuous', '30', '20', '80', '1', 'mm', 30, '0', '液压弯管抗冲击优化几何变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_THETA_1', 'θ1 第一个弯角弯曲角度', 'continuous', '110', '30', '150', '1', 'deg', 40, '0', '液压弯管抗冲击优化几何变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_THETA_2', 'θ2 第二个弯角弯曲角度', 'continuous', '120', '30', '150', '1', 'deg', 50, '0', '液压弯管抗冲击优化几何变量', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CABLE_ROUTE_SIDE', '线缆布置侧别', 'enum', 'upper', 'upper', 'outer', '', '', 60, '0', '可选 upper/lower/inner/outer', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CABLE_PIPE_CLEARANCE', '线缆与液压管最小隔离距离', 'continuous', '40', '20', '80', '1', 'mm', 70, '0', '线缆与液压管保持安全距离', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CABLE_OFFSET', '线缆相对液压管中心线偏移距离', 'continuous', '60', '30', '120', '1', 'mm', 80, '0', '围绕液压管路径的偏移布置参数', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CABLE_BEND_RADIUS', '线缆最小弯曲半径', 'continuous', '50', '30', '120', '1', 'mm', 90, '0', '满足线缆弯曲半径要求', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CLAMP_SPACING', '线夹/管夹布置间距', 'continuous', '180', '100', '250', '5', 'mm', 100, '0', '控制线夹和管夹支撑间距', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'SERVICE_MARGIN', '检修操作预留空间', 'continuous', '40', '30', '100', '1', 'mm', 110, '0', '保证维护和装配操作空间', 'admin', SYSDATE())
ON DUPLICATE KEY UPDATE
  discipline = VALUES(discipline), discipline_name = VALUES(discipline_name), subtask_code = VALUES(subtask_code),
  variable_name = VALUES(variable_name), variable_type = VALUES(variable_type), default_value = VALUES(default_value),
  lower_bound = VALUES(lower_bound), upper_bound = VALUES(upper_bound), step_value = VALUES(step_value), unit = VALUES(unit),
  sort_order = VALUES(sort_order), status = VALUES(status), remark = VALUES(remark), update_by = 'admin', update_time = SYSDATE();

UPDATE t2_design_objective_catalog
SET status = '1', update_by = 'admin', update_time = SYSDATE()
WHERE item_code IN (
  'LAY_LENGTH_MIN', 'LAY_INTERFERENCE_MIN', 'LAY_CLEARANCE_LIMIT', 'LAY_PIPE_CABLE_DISTANCE',
  'LAY_FORBIDDEN_ZONE', 'LAY_CLAMP_INTERVAL', 'LAY_BEND_RADIUS_LIMIT', 'LAY_PIPE_ENDPOINT_FIXED',
  'LAY_PIPE_HORIZONTAL_SPAN', 'LAY_PIPE_VERTICAL_SPAN'
);

INSERT INTO t2_design_objective_catalog
  (discipline, discipline_name, item_type, item_code, item_name, direction, default_weight, default_limit_value, unit, sort_order, status, remark, create_by, create_time)
VALUES
  ('hydraulic', '液压工程师', 'objective', 'HYD_STRESS_MIN', '最大等效应力最小', 'min', 50, '', 'MPa', 10, '0', '降低液压管路冲击工况下应力水平', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'objective', 'HYD_DEFORMATION_MIN', '最大变形量最小', 'min', 50, '', 'mm', 20, '0', '降低液压管路变形', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_STRESS_LIMIT', '最大等效应力不超过屈服强度', '<=', 50, '', 'MPa', 30, '0', '满足液压管路强度约束', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_DEFORMATION_LIMIT', '最大变形量不超过允许变形', '<=', 50, '', 'mm', 40, '0', '满足液压管路变形约束', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_MIN_BEND_RADIUS', '液压管弯曲半径不小于下限', '>=', 50, '20', 'mm', 50, '0', '弯曲半径 R 不小于 20 mm', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'objective', 'LAY_INTERFERENCE_RISK_MIN', '线缆与液压管干涉风险最小', 'min', 50, '', 'risk', 60, '0', '降低线缆与液压管、结构件、运动件的干涉风险', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'objective', 'LAY_CABLE_LENGTH_MIN', '线缆路径长度最小', 'min', 50, '', 'm', 70, '0', '在满足避让条件下缩短线缆路径', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'objective', 'LAY_MAINTAINABILITY_MAX', '维护可达性最大', 'max', 50, '', 'score', 80, '0', '提升检查、维护与更换便利性', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'objective', 'LAY_COMPACTNESS_MAX', '布局紧凑度最大', 'max', 50, '', 'score', 90, '0', '提升局部管线布置紧凑性', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_PIPE_CLEARANCE_LIMIT', '线缆与液压管保持安全间距', '>=', 50, '40', 'mm', 100, '0', 'clearance >= CABLE_PIPE_CLEARANCE', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_FORBIDDEN_ZONE_AVOID', '线缆不得穿越结构禁布区域', 'avoid', 50, '', '', 110, '0', '避让结构禁布区域', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_DOOR_ENVELOPE_AVOID', '线缆不得进入舱门运动包络', 'avoid', 50, '', '', 120, '0', '避让舱门运动包络', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_CABLE_BEND_RADIUS_LIMIT', '线缆弯曲半径满足要求', '>=', 50, '50', 'mm', 130, '0', 'CABLE_BEND_RADIUS 满足线缆弯曲要求', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_CLAMP_SPACING_LIMIT', '线夹间距不超过上限', '<=', 50, '250', 'mm', 140, '0', 'CLAMP_SPACING <= 250 mm', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_SERVICE_MARGIN_LIMIT', '检修空间满足要求', '>=', 50, '30', 'mm', 150, '0', 'SERVICE_MARGIN >= 30 mm', 'admin', SYSDATE())
ON DUPLICATE KEY UPDATE
  discipline = VALUES(discipline), discipline_name = VALUES(discipline_name), item_type = VALUES(item_type),
  item_name = VALUES(item_name), direction = VALUES(direction), default_weight = VALUES(default_weight),
  default_limit_value = VALUES(default_limit_value), unit = VALUES(unit), sort_order = VALUES(sort_order),
  status = VALUES(status), remark = VALUES(remark), update_by = 'admin', update_time = SYSDATE();

INSERT INTO t2_design_resource
  (resource_id, resource_name, category, version, file_type, file_path, description, status, create_by, create_time)
VALUES
  (9001, '起落架舱门结构边界条件', '结构资源', 'v1.0', 'PDF', '/profile/design/resource/lgd_structure_boundary.pdf', '用于结构目标约束选择', '0', 'admin', SYSDATE()),
  (9002, '舱门线缆管路禁布区域', '布局资源', 'v1.0', 'DWG', '/profile/design/resource/lgd_forbidden_zone.dwg', '用于线缆管路布局避让', '0', 'admin', SYSDATE()),
  (9003, '液压弯管冲击载荷谱', '液压资源', 'v1.0', 'XLSX', '/profile/design/resource/hydraulic_impact_load.xlsx', '用于液压弯管抗冲击优化', '0', 'admin', SYSDATE())
ON DUPLICATE KEY UPDATE
  resource_name = VALUES(resource_name), category = VALUES(category), version = VALUES(version), file_type = VALUES(file_type),
  file_path = VALUES(file_path), description = VALUES(description), status = VALUES(status), update_by = 'admin', update_time = SYSDATE();

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
  set_name = VALUES(set_name), fault_segment_name = VALUES(fault_segment_name), material_name = VALUES(material_name),
  source_type = VALUES(source_type), is_default = VALUES(is_default), status = VALUES(status), remark = VALUES(remark),
  update_by = 'admin', update_time = SYSDATE();

SET @fault_pipe_default_set_id = (SELECT parameter_set_id FROM t2_design_fault_pipe_parameter_set WHERE set_code = 'FAULT_PIPE_DEFAULT_001' LIMIT 1);

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
  param_group = VALUES(param_group), group_name = VALUES(group_name), param_name = VALUES(param_name),
  param_value = VALUES(param_value), param_unit = VALUES(param_unit), value_type = VALUES(value_type),
  formula_text = VALUES(formula_text), description = VALUES(description), sort_order = VALUES(sort_order),
  status = VALUES(status), update_by = 'admin', update_time = SYSDATE();


SET FOREIGN_KEY_CHECKS = 1;
