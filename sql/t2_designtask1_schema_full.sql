-- Project 2 / designtask1 full schema.
-- Use this file for a clean master import. It contains final table definitions only.
-- It intentionally excludes rename scripts, alter-upgrade scripts, cleanup scripts, and seed data.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS t2_design_task (
  task_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Task ID',
  task_name varchar(200) NOT NULL COMMENT 'Task name',
  task_no varchar(50) DEFAULT NULL COMMENT 'Task number',
  task_type varchar(20) NOT NULL COMMENT 'Task type',
  template_id bigint(20) DEFAULT NULL COMMENT 'Template ID',
  priority int(2) DEFAULT 2 COMMENT 'Priority',
  description text COMMENT 'Description',
  planned_start_time datetime DEFAULT NULL COMMENT 'Planned start time',
  planned_end_time datetime DEFAULT NULL COMMENT 'Planned end time',
  plan_start_time datetime DEFAULT NULL COMMENT 'Legacy planned start time',
  expected_end_time datetime DEFAULT NULL COMMENT 'Legacy expected end time',
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
  rule_type varchar(64) DEFAULT NULL COMMENT 'Executable rule type',
  rule_expression varchar(500) DEFAULT NULL COMMENT 'Executable rule expression',
  target_field varchar(128) DEFAULT NULL COMMENT 'Rule target field',
  reference_field varchar(128) DEFAULT NULL COMMENT 'Rule reference field',
  operator_code varchar(32) DEFAULT NULL COMMENT 'Rule operator code',
  threshold_value varchar(100) DEFAULT NULL COMMENT 'Rule threshold value',
  execute_mode varchar(32) DEFAULT 'reserved' COMMENT 'Execution mode',
  rule_payload text COMMENT 'Extended rule payload JSON',
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
  rule_type varchar(64) DEFAULT NULL COMMENT 'Executable rule type',
  rule_expression varchar(500) DEFAULT NULL COMMENT 'Executable rule expression',
  target_field varchar(128) DEFAULT NULL COMMENT 'Rule target field',
  reference_field varchar(128) DEFAULT NULL COMMENT 'Rule reference field',
  operator_code varchar(32) DEFAULT NULL COMMENT 'Rule operator code',
  threshold_value varchar(100) DEFAULT NULL COMMENT 'Rule threshold value',
  execute_mode varchar(32) DEFAULT 'reserved' COMMENT 'Execution mode',
  rule_payload text COMMENT 'Extended rule payload JSON',
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

CREATE TABLE IF NOT EXISTS t2_surrogate_solve_task (
  task_id bigint(20) NOT NULL COMMENT 'Task ID',
  status varchar(32) NOT NULL COMMENT 'Solve status',
  model_name varchar(255) DEFAULT NULL COMMENT 'Model name',
  model_type varchar(255) DEFAULT NULL COMMENT 'Model type',
  objective_name varchar(128) DEFAULT NULL COMMENT 'Objective name',
  objective_unit varchar(64) DEFAULT NULL COMMENT 'Objective unit',
  params_json text COMMENT 'Input params JSON',
  best_solution_json text COMMENT 'Best solution JSON',
  candidate_solutions_json text COMMENT 'Candidate solutions JSON',
  iteration_history_json text COMMENT 'Iteration history JSON',
  iterations int DEFAULT 0 COMMENT 'Iterations',
  error_message varchar(1000) DEFAULT NULL COMMENT 'Error message',
  confirmed char(1) DEFAULT '0' COMMENT 'Confirmed flag',
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Surrogate solve task';

CREATE TABLE IF NOT EXISTS t2_cad_model_task (
  task_id bigint(20) NOT NULL COMMENT 'Task ID',
  status varchar(32) NOT NULL COMMENT 'CAD status',
  params_json text COMMENT 'Input params JSON',
  l3 decimal(18,6) DEFAULT NULL COMMENT 'Computed L3',
  initial_angle decimal(18,6) DEFAULT NULL COMMENT 'Initial angle',
  closure_status varchar(128) DEFAULT NULL COMMENT 'Geometry closure status',
  error_message varchar(1000) DEFAULT NULL COMMENT 'Error message',
  sldprt_path varchar(1000) DEFAULT NULL COMMENT 'SLDPRT path',
  stl_path varchar(1000) DEFAULT NULL COMMENT 'STL path',
  preview_png_path varchar(1000) DEFAULT NULL COMMENT 'Preview image path',
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CAD model task';

CREATE TABLE IF NOT EXISTS t2_design_ansys_simulation_task (
  task_id bigint(20) NOT NULL COMMENT 'Task ID',
  status varchar(32) NOT NULL COMMENT 'Simulation status',
  simulation_type varchar(100) DEFAULT NULL COMMENT 'Simulation type',
  input_json text COMMENT 'Input JSON',
  result_json text COMMENT 'Result JSON',
  metrics_json text COMMENT 'Metrics JSON',
  stress_image_url varchar(1000) DEFAULT NULL COMMENT 'Stress image URL',
  result_file_path varchar(1000) DEFAULT NULL COMMENT 'Result file path',
  error_message varchar(1000) DEFAULT NULL COMMENT 'Error message',
  placeholder char(1) DEFAULT '1' COMMENT 'Placeholder flag',
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ANSYS simulation task';

SET FOREIGN_KEY_CHECKS = 1;
