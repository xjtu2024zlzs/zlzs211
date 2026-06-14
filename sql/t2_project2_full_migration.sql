
-- =====================================================================
-- 00 Header
-- =====================================================================
-- Project 2 full database migration for project2-dev -> master integration.
-- This script targets the business database, usually `ry-cloud`.
-- It is designed to be repeatable where possible and keeps existing task data.
-- It intentionally excludes destructive Flowable engine table rebuild scripts.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
-- 01 Rename legacy design-task tables to t2_ prefix
-- =====================================================================
-- Source: sql\t2_table_rename.sql

-- Rename existing designtask1 tables to the t2_ naming convention.
-- Execute in the business database used by ruoyi-system-dev.yml, usually `ry-cloud`.
-- Existing data is preserved. If the target t2_* table already exists, the source table is left unchanged.

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS rename_table_to_t2_if_exists;
DELIMITER $$
CREATE PROCEDURE rename_table_to_t2_if_exists(
  IN old_table_name varchar(128),
  IN new_table_name varchar(128)
)
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = old_table_name
  ) AND NOT EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = new_table_name
  ) THEN
    SET @rename_sql = CONCAT('RENAME TABLE `', old_table_name, '` TO `', new_table_name, '`');
    PREPARE stmt FROM @rename_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

-- Old unprefixed design_* tables.
CALL rename_table_to_t2_if_exists('design_task', 't2_design_task');
CALL rename_table_to_t2_if_exists('design_template', 't2_design_template');
CALL rename_table_to_t2_if_exists('design_template_node', 't2_design_template_node');
CALL rename_table_to_t2_if_exists('design_template_node_role', 't2_design_template_node_role');
CALL rename_table_to_t2_if_exists('design_task_node', 't2_design_task_node');
CALL rename_table_to_t2_if_exists('design_task_file', 't2_design_task_file');
CALL rename_table_to_t2_if_exists('design_task_log', 't2_design_task_log');
CALL rename_table_to_t2_if_exists('design_objective_catalog', 't2_design_objective_catalog');
CALL rename_table_to_t2_if_exists('design_objective_constraint', 't2_design_objective_constraint');
CALL rename_table_to_t2_if_exists('design_variable_catalog', 't2_design_variable_catalog');
CALL rename_table_to_t2_if_exists('design_task_variable_selection', 't2_design_task_variable_selection');
CALL rename_table_to_t2_if_exists('design_conflict_check', 't2_design_conflict_check');
CALL rename_table_to_t2_if_exists('design_subtask_solution', 't2_design_subtask_solution');
CALL rename_table_to_t2_if_exists('design_simulation_result', 't2_design_simulation_result');
CALL rename_table_to_t2_if_exists('design_approval_record', 't2_design_approval_record');
CALL rename_table_to_t2_if_exists('design_resource', 't2_design_resource');
CALL rename_table_to_t2_if_exists('surrogate_solve_task', 't2_surrogate_solve_task');
CALL rename_table_to_t2_if_exists('cad_model_task', 't2_cad_model_task');

-- Existing p2_* tables.
CALL rename_table_to_t2_if_exists('p2_design_task', 't2_design_task');
CALL rename_table_to_t2_if_exists('p2_design_template', 't2_design_template');
CALL rename_table_to_t2_if_exists('p2_design_template_node', 't2_design_template_node');
CALL rename_table_to_t2_if_exists('p2_design_template_node_role', 't2_design_template_node_role');
CALL rename_table_to_t2_if_exists('p2_design_task_node', 't2_design_task_node');
CALL rename_table_to_t2_if_exists('p2_design_task_file', 't2_design_task_file');
CALL rename_table_to_t2_if_exists('p2_design_task_log', 't2_design_task_log');
CALL rename_table_to_t2_if_exists('p2_design_objective_catalog', 't2_design_objective_catalog');
CALL rename_table_to_t2_if_exists('p2_design_objective_constraint', 't2_design_objective_constraint');
CALL rename_table_to_t2_if_exists('p2_design_variable_catalog', 't2_design_variable_catalog');
CALL rename_table_to_t2_if_exists('p2_design_task_variable_selection', 't2_design_task_variable_selection');
CALL rename_table_to_t2_if_exists('p2_design_conflict_check', 't2_design_conflict_check');
CALL rename_table_to_t2_if_exists('p2_design_subtask_solution', 't2_design_subtask_solution');
CALL rename_table_to_t2_if_exists('p2_design_simulation_result', 't2_design_simulation_result');
CALL rename_table_to_t2_if_exists('p2_design_approval_record', 't2_design_approval_record');
CALL rename_table_to_t2_if_exists('p2_design_resource', 't2_design_resource');
CALL rename_table_to_t2_if_exists('p2_surrogate_solve_task', 't2_surrogate_solve_task');
CALL rename_table_to_t2_if_exists('p2_cad_model_task', 't2_cad_model_task');

DROP PROCEDURE IF EXISTS rename_table_to_t2_if_exists;

-- =====================================================================
-- 02 Final t2_ table schema
-- =====================================================================
-- Source: sql\t2_designtask1_schema_full.sql

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
  step_path varchar(1000) DEFAULT NULL COMMENT 'STEP path for ANSYS import',
  parasolid_path varchar(1000) DEFAULT NULL COMMENT 'Parasolid path for ANSYS import',
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

-- =====================================================================
-- 03 Idempotent schema compatibility upgrades
-- =====================================================================
-- ---------------------------------------------------------------------
-- Compatibility upgrades for databases created from earlier project2-dev scripts.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS t2_add_column_if_missing;
DELIMITER $$
CREATE PROCEDURE t2_add_column_if_missing(
  IN p_table_name varchar(128),
  IN p_column_name varchar(128),
  IN p_column_def text
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND column_name = p_column_name
  ) THEN
    SET @t2_add_column_sql = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN `', p_column_name, '` ', p_column_def);
    PREPARE stmt FROM @t2_add_column_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CALL t2_add_column_if_missing('t2_design_objective_catalog', 'rule_type', 'varchar(64) DEFAULT NULL COMMENT ''Executable rule type''');
CALL t2_add_column_if_missing('t2_design_objective_catalog', 'rule_expression', 'varchar(500) DEFAULT NULL COMMENT ''Executable rule expression''');
CALL t2_add_column_if_missing('t2_design_objective_catalog', 'target_field', 'varchar(128) DEFAULT NULL COMMENT ''Rule target field''');
CALL t2_add_column_if_missing('t2_design_objective_catalog', 'reference_field', 'varchar(128) DEFAULT NULL COMMENT ''Rule reference field''');
CALL t2_add_column_if_missing('t2_design_objective_catalog', 'operator_code', 'varchar(32) DEFAULT NULL COMMENT ''Rule operator code''');
CALL t2_add_column_if_missing('t2_design_objective_catalog', 'threshold_value', 'varchar(100) DEFAULT NULL COMMENT ''Rule threshold value''');
CALL t2_add_column_if_missing('t2_design_objective_catalog', 'execute_mode', 'varchar(32) DEFAULT ''reserved'' COMMENT ''Execution mode''');
CALL t2_add_column_if_missing('t2_design_objective_catalog', 'rule_payload', 'text COMMENT ''Extended rule payload JSON''');

CALL t2_add_column_if_missing('t2_design_objective_constraint', 'rule_type', 'varchar(64) DEFAULT NULL COMMENT ''Executable rule type''');
CALL t2_add_column_if_missing('t2_design_objective_constraint', 'rule_expression', 'varchar(500) DEFAULT NULL COMMENT ''Executable rule expression''');
CALL t2_add_column_if_missing('t2_design_objective_constraint', 'target_field', 'varchar(128) DEFAULT NULL COMMENT ''Rule target field''');
CALL t2_add_column_if_missing('t2_design_objective_constraint', 'reference_field', 'varchar(128) DEFAULT NULL COMMENT ''Rule reference field''');
CALL t2_add_column_if_missing('t2_design_objective_constraint', 'operator_code', 'varchar(32) DEFAULT NULL COMMENT ''Rule operator code''');
CALL t2_add_column_if_missing('t2_design_objective_constraint', 'threshold_value', 'varchar(100) DEFAULT NULL COMMENT ''Rule threshold value''');
CALL t2_add_column_if_missing('t2_design_objective_constraint', 'execute_mode', 'varchar(32) DEFAULT ''reserved'' COMMENT ''Execution mode''');
CALL t2_add_column_if_missing('t2_design_objective_constraint', 'rule_payload', 'text COMMENT ''Extended rule payload JSON''');

CALL t2_add_column_if_missing('t2_cad_model_task', 'step_path', 'varchar(1000) DEFAULT NULL COMMENT ''STEP path for ANSYS import''');
CALL t2_add_column_if_missing('t2_cad_model_task', 'parasolid_path', 'varchar(1000) DEFAULT NULL COMMENT ''Parasolid path for ANSYS import''');
CALL t2_add_column_if_missing('t2_cad_model_task', 'centerline_csv_path', 'varchar(1000) DEFAULT NULL COMMENT ''Pipe centerline CSV path for ANSYS inner-wall selection''');

CALL t2_add_column_if_missing('t2_design_ansys_simulation_task', 'simulation_mode', 'varchar(64) NOT NULL DEFAULT ''DEMO_SIMULATION_MODEL'' COMMENT ''Simulation model mode''');
ALTER TABLE t2_design_ansys_simulation_task MODIFY COLUMN error_message text COMMENT 'ANSYS worker error message and diagnostics';
UPDATE t2_design_ansys_simulation_task
SET simulation_mode = 'DEMO_SIMULATION_MODEL'
WHERE simulation_mode IS NULL OR simulation_mode = '';

SET @t2_ansys_pk_has_mode := (
  SELECT COUNT(1)
  FROM information_schema.key_column_usage
  WHERE table_schema = DATABASE()
    AND table_name = 't2_design_ansys_simulation_task'
    AND constraint_name = 'PRIMARY'
    AND column_name = 'simulation_mode'
);
SET @t2_ansys_pk_column_count := (
  SELECT COUNT(1)
  FROM information_schema.key_column_usage
  WHERE table_schema = DATABASE()
    AND table_name = 't2_design_ansys_simulation_task'
    AND constraint_name = 'PRIMARY'
);
SET @t2_ansys_pk_sql := IF(@t2_ansys_pk_has_mode = 0 AND @t2_ansys_pk_column_count > 0,
  'ALTER TABLE t2_design_ansys_simulation_task DROP PRIMARY KEY, ADD PRIMARY KEY (task_id, simulation_mode)',
  'SELECT 1'
);
PREPARE stmt FROM @t2_ansys_pk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @t2_ansys_pk_sql := IF(@t2_ansys_pk_has_mode = 0 AND @t2_ansys_pk_column_count = 0,
  'ALTER TABLE t2_design_ansys_simulation_task ADD PRIMARY KEY (task_id, simulation_mode)',
  'SELECT 1'
);
PREPARE stmt FROM @t2_ansys_pk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

DROP PROCEDURE IF EXISTS t2_add_column_if_missing;

-- =====================================================================
-- 04 Initial menus, roles, templates, catalogs, variables, resources, fault-pipe parameters
-- =====================================================================
-- Source: sql\t2_designtask1_data_init.sql

-- Project 2 / designtask1 initial data.
-- Run after sql/t2_designtask1_schema_full.sql.
-- This script is repeatable: existing records are updated, missing records are inserted.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------
-- 1. System menu and permissions
-- ---------------------------------------------------------------------

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, update_by, update_time, remark)
VALUES
  (2600, '设计制造协同优化平台', 0, 5, 'design-platform', NULL, '', '',
   1, 0, 'M', '0', '0', '', 'tree-table',
   'admin', SYSDATE(), '', NULL, '课题二设计制造协同优化平台目录'),
  (2601, '首页（任务看板）', 2600, 1, 'dashboard', 'designtask/dashboard/index', '', '',
   1, 0, 'C', '0', '0', 'designtask:task:list', 'dashboard',
   'admin', SYSDATE(), '', NULL, ''),
  (2602, '协同机制生成', 2600, 2, 'mechanism', 'designtask/mechanism/index', '', '',
   1, 0, 'C', '0', '0', 'designtask:task:add', 'guide',
   'admin', SYSDATE(), '', NULL, ''),
  (2603, '目标约束选择', 2600, 3, 'objective', 'designtask/objective/index', '', '',
   1, 0, 'C', '0', '0', 'designtask:task:handle', 'checkbox',
   'admin', SYSDATE(), '', NULL, ''),
  (2604, '模型解耦求解', 2600, 4, 'solve', 'designtask/solve/index', '', '',
   1, 0, 'C', '0', '0', 'designtask:task:solve', 'component',
   'admin', SYSDATE(), '', NULL, ''),
  (2605, '仿真验证确认', 2600, 5, 'simulation', 'designtask/simulation/index', '', '',
   1, 0, 'C', '0', '0', 'designtask:task:simulate', 'chart',
   'admin', SYSDATE(), '', NULL, ''),
  (2621, '任务归档文档', 2600, 7, 'archive', 'designtask/archive/index', '', '',
   1, 0, 'C', '1', '0', 'designtask:archive:view', 'documentation',
   'admin', SYSDATE(), '', NULL, '已完成任务归档查看页面')
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  parent_id = VALUES(parent_id),
  order_num = VALUES(order_num),
  path = VALUES(path),
  component = VALUES(component),
  query = VALUES(query),
  route_name = VALUES(route_name),
  is_frame = VALUES(is_frame),
  is_cache = VALUES(is_cache),
  menu_type = VALUES(menu_type),
  visible = VALUES(visible),
  status = VALUES(status),
  perms = VALUES(perms),
  icon = VALUES(icon),
  remark = VALUES(remark),
  update_by = 'admin',
  update_time = SYSDATE();

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, update_by, update_time, remark)
VALUES
  (2606, '任务查询', 2601, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:task:query', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2607, '提交目标约束', 2603, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:task:handle', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2608, '冲突校验', 2604, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:task:conflict', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2609, '任务解耦', 2604, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:task:decompose', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2610, '模型求解', 2604, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:task:solve', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2611, '仿真验证', 2605, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:task:simulate', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2612, '领导审批', 2605, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:task:approve', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2613, '流程模板查询', 2602, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:flow:list', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2616, '流程模板详情', 2602, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:flow:query', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2617, '流程模板新增', 2602, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:flow:add', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2618, '流程模板修改', 2602, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:flow:edit', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2619, '流程模板删除', 2602, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:flow:remove', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2620, '设计变量选择', 2604, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:task:variable', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2622, '归档文档查看', 2621, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:archive:view', '#', 'admin', SYSDATE(), '', NULL, '')
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  parent_id = VALUES(parent_id),
  order_num = VALUES(order_num),
  path = VALUES(path),
  component = VALUES(component),
  menu_type = VALUES(menu_type),
  visible = VALUES(visible),
  status = VALUES(status),
  perms = VALUES(perms),
  icon = VALUES(icon),
  remark = VALUES(remark),
  update_by = 'admin',
  update_time = SYSDATE();

-- ---------------------------------------------------------------------
-- 2. Platform roles and role-menu permissions
-- ---------------------------------------------------------------------

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_t2_platform_role (
  role_key varchar(100) NOT NULL,
  role_name varchar(30) NOT NULL,
  role_sort int NOT NULL,
  remark varchar(500) DEFAULT NULL,
  PRIMARY KEY (role_key)
) ENGINE=MEMORY;

DELETE FROM tmp_t2_platform_role;
INSERT INTO tmp_t2_platform_role(role_key, role_name, role_sort, remark) VALUES
  ('design_task_owner', '任务负责人', 10, '发起协同优化任务，执行冲突校验、任务解耦、模型求解、仿真验证和归档查看'),
  ('structure_engineer', '结构工程师', 20, '选择结构学科目标与约束'),
  ('layout_engineer', '布局工程师', 30, '选择布局学科目标与约束'),
  ('aero_engineer', '气动工程师', 40, '选择气动学科目标与约束'),
  ('hydraulic_engineer', '液压工程师', 50, '选择液压学科目标与约束'),
  ('manufacturing_engineer', '制造工程师', 60, '选择制造相关约束并参与仿真验证确认'),
  ('approval_leader', '审批领导', 70, '查看任务结果并完成最终审批');

UPDATE sys_role r
JOIN tmp_t2_platform_role t ON r.role_name = t.role_name
SET r.role_key = t.role_key,
    r.role_sort = t.role_sort,
    r.status = '0',
    r.del_flag = '0',
    r.remark = t.remark,
    r.update_by = 'admin',
    r.update_time = SYSDATE()
WHERE r.del_flag <> '2';

INSERT INTO sys_role
  (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly,
   status, del_flag, create_by, create_time, update_by, update_time, remark)
SELECT
  t.role_name, t.role_key, t.role_sort, '2', 1, 1,
  '0', '0', 'admin', SYSDATE(), '', NULL, t.remark
FROM tmp_t2_platform_role t
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role r WHERE r.role_key = t.role_key AND r.del_flag <> '2'
);

DELETE rm
FROM sys_role_menu rm
JOIN sys_role r ON r.role_id = rm.role_id
JOIN tmp_t2_platform_role t ON (r.role_key = t.role_key OR r.role_name = t.role_name)
WHERE rm.menu_id BETWEEN 2600 AND 2622;

INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN tmp_t2_platform_role t ON (r.role_key = t.role_key OR r.role_name = t.role_name)
JOIN sys_menu m ON m.menu_id IN (2600, 2601, 2606, 2621, 2622)
WHERE r.del_flag <> '2';

INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (2602, 2604, 2605, 2608, 2609, 2610, 2611, 2613, 2616, 2620, 2621, 2622)
WHERE r.role_key = 'design_task_owner'
  AND r.del_flag <> '2';

INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (2603, 2607)
WHERE r.role_key IN ('structure_engineer', 'layout_engineer', 'aero_engineer', 'hydraulic_engineer')
  AND r.del_flag <> '2';

INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (2603, 2605, 2607, 2611, 2621, 2622)
WHERE r.role_key = 'manufacturing_engineer'
  AND r.del_flag <> '2';

INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (2605, 2612, 2621, 2622)
WHERE r.role_key = 'approval_leader'
  AND r.del_flag <> '2';

INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id BETWEEN 2600 AND 2622
WHERE r.role_key IN ('admin', 'design_platform_admin')
  AND r.del_flag <> '2';

DROP TEMPORARY TABLE IF EXISTS tmp_t2_platform_role;

-- ---------------------------------------------------------------------
-- 3. Design process template and template nodes
-- ---------------------------------------------------------------------

INSERT INTO t2_design_template
  (template_id, template_name, template_desc, template_config, is_default, status, create_by, create_time, remark)
VALUES
  (1, '标准协同优化流程',
   '液压弯管抗冲击优化与线缆管路布局设计协同流程',
   '[{"nodeCode":"objective_select","nodeName":"目标约束选择"},{"nodeCode":"conflict_check","nodeName":"冲突校验"},{"nodeCode":"model_decompose_solve","nodeName":"模型解耦求解"},{"nodeCode":"simulation_verify","nodeName":"仿真验证"},{"nodeCode":"approval","nodeName":"审批归档"}]',
   '1', '0', 'admin', SYSDATE(), 'designtask1 default template')
ON DUPLICATE KEY UPDATE
  template_name = VALUES(template_name),
  template_desc = VALUES(template_desc),
  template_config = VALUES(template_config),
  is_default = VALUES(is_default),
  status = VALUES(status),
  remark = VALUES(remark),
  update_by = 'admin',
  update_time = SYSDATE();

INSERT INTO t2_design_template_node
  (template_id, node_code, node_name, node_order, parallel_group, responsible_role, time_limit, create_by, create_time)
VALUES
  (1, 'objective_select', '目标约束选择', 1, 'discipline_parallel', 'discipline_engineer', 24, 'admin', SYSDATE()),
  (1, 'conflict_check', '冲突校验', 2, NULL, 'design_task_owner', 8, 'admin', SYSDATE()),
  (1, 'model_decompose_solve', '模型解耦求解', 3, NULL, 'design_task_owner', 24, 'admin', SYSDATE()),
  (1, 'simulation_verify', '仿真验证确认', 4, NULL, 'manufacturing_engineer', 24, 'admin', SYSDATE()),
  (1, 'approval', '审批归档', 5, NULL, 'approval_leader', 12, 'admin', SYSDATE())
ON DUPLICATE KEY UPDATE
  node_name = VALUES(node_name),
  node_order = VALUES(node_order),
  parallel_group = VALUES(parallel_group),
  responsible_role = VALUES(responsible_role),
  time_limit = VALUES(time_limit),
  update_by = 'admin',
  update_time = SYSDATE();

DELETE FROM t2_design_template_node_role
WHERE node_id IN (SELECT node_id FROM t2_design_template_node WHERE template_id = 1);

INSERT INTO t2_design_template_node_role(node_id, role_code, role_name, create_by, create_time)
SELECT node_id, 'structure_engineer', '结构工程师', 'admin', SYSDATE()
FROM t2_design_template_node WHERE template_id = 1 AND node_code = 'objective_select';
INSERT INTO t2_design_template_node_role(node_id, role_code, role_name, create_by, create_time)
SELECT node_id, 'layout_engineer', '布局工程师', 'admin', SYSDATE()
FROM t2_design_template_node WHERE template_id = 1 AND node_code = 'objective_select';
INSERT INTO t2_design_template_node_role(node_id, role_code, role_name, create_by, create_time)
SELECT node_id, 'aero_engineer', '气动工程师', 'admin', SYSDATE()
FROM t2_design_template_node WHERE template_id = 1 AND node_code = 'objective_select';
INSERT INTO t2_design_template_node_role(node_id, role_code, role_name, create_by, create_time)
SELECT node_id, 'hydraulic_engineer', '液压工程师', 'admin', SYSDATE()
FROM t2_design_template_node WHERE template_id = 1 AND node_code = 'objective_select';
INSERT INTO t2_design_template_node_role(node_id, role_code, role_name, create_by, create_time)
SELECT node_id, 'manufacturing_engineer', '制造工程师', 'admin', SYSDATE()
FROM t2_design_template_node WHERE template_id = 1 AND node_code = 'objective_select';
INSERT INTO t2_design_template_node_role(node_id, role_code, role_name, create_by, create_time)
SELECT node_id, responsible_role, responsible_role, 'admin', SYSDATE()
FROM t2_design_template_node
WHERE template_id = 1
  AND node_code <> 'objective_select'
  AND responsible_role IS NOT NULL;

-- ---------------------------------------------------------------------
-- 4. Objective and constraint catalog
-- ---------------------------------------------------------------------

UPDATE t2_design_objective_catalog
SET status = '1',
    update_by = 'admin',
    update_time = SYSDATE()
WHERE item_code NOT IN (
  'HYD_STRESS_MIN', 'HYD_DEFORMATION_MIN', 'HYD_STRESS_LIMIT', 'HYD_DEFORMATION_LIMIT', 'HYD_MIN_BEND_RADIUS',
  'LAY_INTERFERENCE_RISK_MIN', 'LAY_CABLE_LENGTH_MIN', 'LAY_MAINTAINABILITY_MAX',
  'LAY_PIPE_CLEARANCE_LIMIT', 'LAY_FORBIDDEN_ZONE_AVOID', 'LAY_CABLE_BEND_RADIUS_LIMIT', 'LAY_CLAMP_SPACING_LIMIT', 'LAY_SERVICE_MARGIN_LIMIT',
  'STR_DEFORMATION_RISK_MIN', 'STR_FORBIDDEN_ZONE', 'STR_INTERFACE_FIXED', 'STR_CLAMP_SUPPORT_VALID',
  'AERO_ENVELOPE_IMPACT_MIN', 'AERO_OUTER_ENVELOPE',
  'MFG_PROCESS_COMPLEXITY_MIN', 'MFG_ASSEMBLY_EFFICIENCY_MAX', 'MFG_BEND_RADIUS_LIMIT', 'MFG_CLAMP_INSTALLABLE', 'MFG_TOOL_ACCESS'
);

INSERT INTO t2_design_objective_catalog
  (discipline, discipline_name, item_type, item_code, item_name, direction,
   default_weight, default_limit_value, unit, sort_order, status, remark,
   rule_type, rule_expression, target_field, reference_field, operator_code,
   threshold_value, execute_mode, rule_payload, create_by, create_time)
VALUES
  ('hydraulic', '液压工程师', 'objective', 'HYD_STRESS_MIN', '最大等效应力最小', 'min', 50, '', 'MPa', 10, '0', '液压弯管抗冲击优化核心目标', 'objective_metric', 'min(maxEquivalentStress)', 'maxEquivalentStress', '', 'minimize', '', 'reserved', '', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'objective', 'HYD_DEFORMATION_MIN', '最大变形量最小', 'min', 50, '', 'mm', 20, '0', '液压弯管抗冲击优化核心目标', 'objective_metric', 'min(maxDeformation)', 'maxDeformation', '', 'minimize', '', 'reserved', '', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_STRESS_LIMIT', '最大等效应力不超过许用应力', '<=', 50, '', 'MPa', 30, '0', '后续由仿真或代理模型返回应力值后执行', 'strength_check', 'maxEquivalentStress <= allowableStress', 'maxEquivalentStress', 'allowableStress', 'less_equal', '', 'reserved', '', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_DEFORMATION_LIMIT', '最大变形量不超过允许变形', '<=', 50, '', 'mm', 40, '0', '后续由仿真或代理模型返回变形值后执行', 'deformation_check', 'maxDeformation <= allowableDeformation', 'maxDeformation', 'allowableDeformation', 'less_equal', '', 'reserved', '', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_MIN_BEND_RADIUS', '液压管弯曲半径不小于下限', '>=', 50, '20', 'mm', 50, '0', '当前可按变量边界执行', 'radius_check', 'PIPE_BEND_RADIUS >= minPipeBendRadius', 'PIPE_BEND_RADIUS', 'minPipeBendRadius', 'greater_equal', '20', 'reserved', '', 'admin', SYSDATE()),

  ('layout', '布局工程师', 'objective', 'LAY_INTERFERENCE_RISK_MIN', '线缆与液压管干涉风险最小', 'min', 50, '', 'risk', 60, '0', '布局算法接入前先保留指标字段', 'objective_metric', 'min(interferenceRisk)', 'interferenceRisk', '', 'minimize', '', 'reserved', '', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'objective', 'LAY_CABLE_LENGTH_MIN', '线缆路径长度最小', 'min', 50, '', 'm', 70, '0', '布局算法接入前先保留指标字段', 'objective_metric', 'min(cablePathLength)', 'cablePathLength', '', 'minimize', '', 'reserved', '', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'objective', 'LAY_MAINTAINABILITY_MAX', '维护可达性最大', 'max', 50, '', 'score', 80, '0', '布局算法接入前先保留指标字段', 'objective_metric', 'max(maintainabilityScore)', 'maintainabilityScore', '', 'maximize', '', 'reserved', '', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_PIPE_CLEARANCE_LIMIT', '线缆与液压管保持安全间距', '>=', 50, '40', 'mm', 90, '0', '可执行为最小距离校核', 'clearance_check', 'minCablePipeClearance >= CABLE_PIPE_CLEARANCE', 'minCablePipeClearance', 'CABLE_PIPE_CLEARANCE', 'greater_equal', '40', 'reserved', '', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_FORBIDDEN_ZONE_AVOID', '线缆不得穿越结构禁布区域', 'avoid', 50, '', '', 100, '0', '可执行为路径与禁布区域的几何相交校核', 'geometry_intersection', 'intersect(cableRouteGeometry, structureForbiddenZones) = false', 'cableRouteGeometry', 'structureForbiddenZones', 'none_intersect', '', 'reserved', '', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_CABLE_BEND_RADIUS_LIMIT', '线缆弯曲半径满足要求', '>=', 50, '50', 'mm', 110, '0', '可执行为弯曲半径下限校核', 'radius_check', 'actualCableBendRadius >= CABLE_BEND_RADIUS', 'actualCableBendRadius', 'CABLE_BEND_RADIUS', 'greater_equal', '50', 'reserved', '', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_CLAMP_SPACING_LIMIT', '线夹间距不超过上限', '<=', 50, '250', 'mm', 120, '0', '可执行为线夹间距上限校核', 'spacing_check', 'actualClampSpacing <= CLAMP_SPACING', 'actualClampSpacing', 'CLAMP_SPACING', 'less_equal', '250', 'reserved', '', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_SERVICE_MARGIN_LIMIT', '检修空间满足要求', '>=', 50, '30', 'mm', 130, '0', '可执行为检修空间下限校核', 'clearance_check', 'actualServiceMargin >= SERVICE_MARGIN', 'actualServiceMargin', 'SERVICE_MARGIN', 'greater_equal', '30', 'reserved', '', 'admin', SYSDATE()),

  ('structure', '结构工程师', 'objective', 'STR_DEFORMATION_RISK_MIN', '管线变形碰撞风险最小', 'min', 50, '', 'risk', 140, '0', '结构侧保留与管线变形相关的核心目标', 'objective_metric', 'min(deformationCollisionRisk)', 'deformationCollisionRisk', '', 'minimize', '', 'reserved', '', 'admin', SYSDATE()),
  ('structure', '结构工程师', 'constraint', 'STR_FORBIDDEN_ZONE', '结构禁布区域不可穿越', 'avoid', 50, '', '', 150, '0', '可执行为路径与结构禁布区域几何相交校核', 'geometry_intersection', 'intersect(routeGeometry, structureForbiddenZones) = false', 'routeGeometry', 'structureForbiddenZones', 'none_intersect', '', 'reserved', '', 'admin', SYSDATE()),
  ('structure', '结构工程师', 'constraint', 'STR_INTERFACE_FIXED', '铰链、锁机构、作动器接口位置不可更改', 'fixed', 50, '', '', 160, '0', '可执行为关键接口点与基准点一致性校核', 'boundary_lock', 'interfacePoints = baselineInterfacePoints', 'interfacePoints', 'baselineInterfacePoints', 'equal', '', 'reserved', '', 'admin', SYSDATE()),
  ('structure', '结构工程师', 'constraint', 'STR_CLAMP_SUPPORT_VALID', '卡箍支撑点结构强度满足要求', 'meet', 50, '', '', 170, '0', '可执行为支撑点强度校核', 'strength_check', 'clampSupportStrength >= requiredSupportStrength', 'clampSupportStrength', 'requiredSupportStrength', 'pass', '', 'reserved', '', 'admin', SYSDATE()),

  ('aero', '气动工程师', 'objective', 'AERO_ENVELOPE_IMPACT_MIN', '对舱门外形包络影响最小', 'min', 50, '', 'score', 180, '0', '气动侧仅保留包络影响核心项', 'objective_metric', 'min(envelopeImpactScore)', 'envelopeImpactScore', '', 'minimize', '', 'reserved', '', 'admin', SYSDATE()),
  ('aero', '气动工程师', 'constraint', 'AERO_OUTER_ENVELOPE', '管线与附件不得超出舱门外形包络', '<=', 50, '', 'mm', 190, '0', '可执行为包络内外关系校核', 'envelope_check', 'routeEnvelope inside doorOuterEnvelope', 'routeEnvelope', 'doorOuterEnvelope', 'inside_or_equal', '', 'reserved', '', 'admin', SYSDATE()),

  ('manufacturing', '制造工程师', 'objective', 'MFG_PROCESS_COMPLEXITY_MIN', '管线制造加工复杂度最小', 'min', 50, '', 'score', 200, '0', '制造侧保留加工复杂度目标', 'objective_metric', 'min(processComplexityScore)', 'processComplexityScore', '', 'minimize', '', 'reserved', '', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'objective', 'MFG_ASSEMBLY_EFFICIENCY_MAX', '装配效率最大', 'max', 50, '', 'score', 210, '0', '制造侧保留装配效率目标', 'objective_metric', 'max(assemblyEfficiencyScore)', 'assemblyEfficiencyScore', '', 'maximize', '', 'reserved', '', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'constraint', 'MFG_BEND_RADIUS_LIMIT', '弯曲半径满足制造下限', '>=', 50, '20', 'mm', 220, '0', '可执行为制造弯曲半径下限校核', 'radius_check', 'bendRadius >= manufacturingMinBendRadius', 'bendRadius', 'manufacturingMinBendRadius', 'greater_equal', '20', 'reserved', '', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'constraint', 'MFG_CLAMP_INSTALLABLE', '管夹/线夹可安装', 'meet', 50, '', '', 230, '0', '可执行为安装空间/孔位可用性校核', 'installability_check', 'clampInstallability = pass', 'clampInstallability', '', 'pass', '', 'reserved', '', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'constraint', 'MFG_TOOL_ACCESS', '工具操作空间满足要求', 'meet', 50, '', '', 240, '0', '可执行为工具可达性校核', 'accessibility_check', 'toolAccessStatus = pass', 'toolAccessStatus', '', 'pass', '', 'reserved', '', 'admin', SYSDATE())
ON DUPLICATE KEY UPDATE
  discipline = VALUES(discipline),
  discipline_name = VALUES(discipline_name),
  item_type = VALUES(item_type),
  item_name = VALUES(item_name),
  direction = VALUES(direction),
  default_weight = VALUES(default_weight),
  default_limit_value = VALUES(default_limit_value),
  unit = VALUES(unit),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  remark = VALUES(remark),
  rule_type = VALUES(rule_type),
  rule_expression = VALUES(rule_expression),
  target_field = VALUES(target_field),
  reference_field = VALUES(reference_field),
  operator_code = VALUES(operator_code),
  threshold_value = VALUES(threshold_value),
  execute_mode = VALUES(execute_mode),
  rule_payload = VALUES(rule_payload),
  update_by = 'admin',
  update_time = SYSDATE();

-- ---------------------------------------------------------------------
-- 5. Design variable catalog
-- ---------------------------------------------------------------------

UPDATE t2_design_variable_catalog
SET status = '1',
    update_by = 'admin',
    update_time = SYSDATE()
WHERE variable_code NOT IN (
  'PIPE_L1', 'PIPE_L2', 'PIPE_BEND_RADIUS', 'PIPE_THETA_1', 'PIPE_THETA_2',
  'CABLE_ROUTE_SIDE', 'CABLE_PIPE_CLEARANCE', 'CABLE_OFFSET',
  'CABLE_BEND_RADIUS', 'CLAMP_SPACING', 'SERVICE_MARGIN'
);

INSERT INTO t2_design_variable_catalog
  (discipline, discipline_name, subtask_code, variable_code, variable_name, variable_type,
   default_value, lower_bound, upper_bound, step_value, unit, sort_order, status, remark, create_by, create_time)
VALUES
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_L1', 'L1 第一段直管长度', 'continuous', '300', '50', '550', '1', 'mm', 10, '0', '液压弯管抗冲击优化变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_L2', 'L2 第二段直管长度', 'continuous', '150', '50', '300', '1', 'mm', 20, '0', '液压弯管抗冲击优化变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_BEND_RADIUS', 'R 两处弯管圆角半径', 'continuous', '30', '20', '80', '1', 'mm', 30, '0', '液压弯管抗冲击优化变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_THETA_1', 'θ1 第一个弯角弯曲角度', 'continuous', '110', '30', '150', '1', 'deg', 40, '0', '液压弯管抗冲击优化变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_THETA_2', 'θ2 第二个弯角弯曲角度', 'continuous', '120', '30', '150', '1', 'deg', 50, '0', '液压弯管抗冲击优化变量', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CABLE_ROUTE_SIDE', '线缆布置侧别', 'enum', 'upper', 'upper', 'outer', '', '', 60, '0', '预留布局策略字段：upper/lower/inner/outer', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CABLE_PIPE_CLEARANCE', '线缆与液压管最小隔离距离', 'continuous', '40', '20', '80', '1', 'mm', 70, '0', '预留最小距离校核字段', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CABLE_OFFSET', '线缆相对液压管中心线偏移距离', 'continuous', '60', '30', '120', '1', 'mm', 80, '0', '预留路径偏移字段', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CABLE_BEND_RADIUS', '线缆最小弯曲半径', 'continuous', '50', '30', '120', '1', 'mm', 90, '0', '预留弯曲半径校核字段', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CLAMP_SPACING', '线夹/管夹布置间距', 'continuous', '180', '100', '250', '5', 'mm', 100, '0', '预留线夹间距校核字段', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'SERVICE_MARGIN', '检修操作预留空间', 'continuous', '40', '30', '100', '1', 'mm', 110, '0', '预留检修空间校核字段', 'admin', SYSDATE())
ON DUPLICATE KEY UPDATE
  discipline = VALUES(discipline),
  discipline_name = VALUES(discipline_name),
  subtask_code = VALUES(subtask_code),
  variable_name = VALUES(variable_name),
  variable_type = VALUES(variable_type),
  default_value = VALUES(default_value),
  lower_bound = VALUES(lower_bound),
  upper_bound = VALUES(upper_bound),
  step_value = VALUES(step_value),
  unit = VALUES(unit),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  remark = VALUES(remark),
  update_by = 'admin',
  update_time = SYSDATE();

-- ---------------------------------------------------------------------
-- 6. Standard resources
-- ---------------------------------------------------------------------

CREATE TEMPORARY TABLE IF NOT EXISTS tmp_t2_design_resource_seed (
  resource_name varchar(200) NOT NULL,
  category varchar(50) DEFAULT NULL,
  version varchar(50) DEFAULT NULL,
  file_type varchar(20) DEFAULT NULL,
  file_path varchar(500) DEFAULT NULL,
  description varchar(500) DEFAULT NULL,
  status char(1) DEFAULT '0',
  PRIMARY KEY (resource_name)
) ENGINE=MEMORY;

DELETE FROM tmp_t2_design_resource_seed;
INSERT INTO tmp_t2_design_resource_seed
  (resource_name, category, version, file_type, file_path, description, status)
VALUES
  ('起落架舱门标准装配约束', 'design_standard', 'V1.0', 'pdf', '', '用于协同优化任务的装配约束参考', '0'),
  ('液压管路设计规范', 'hydraulic_standard', 'V1.0', 'pdf', '', '液压弯管半径、角度、强度与制造约束参考', '0'),
  ('线缆管路布局规范', 'layout_standard', 'V1.0', 'pdf', '', '线缆布置、间距、弯曲半径和检修空间参考', '0');

UPDATE t2_design_resource r
JOIN tmp_t2_design_resource_seed s ON s.resource_name = r.resource_name
SET r.category = s.category,
    r.version = s.version,
    r.file_type = s.file_type,
    r.file_path = s.file_path,
    r.description = s.description,
    r.status = s.status,
    r.update_by = 'admin',
    r.update_time = SYSDATE();

INSERT INTO t2_design_resource
  (resource_name, category, version, file_type, file_path, description, status, create_by, create_time)
SELECT
  s.resource_name, s.category, s.version, s.file_type, s.file_path, s.description, s.status, 'admin', SYSDATE()
FROM tmp_t2_design_resource_seed s
WHERE NOT EXISTS (
  SELECT 1 FROM t2_design_resource r WHERE r.resource_name = s.resource_name
);

DROP TEMPORARY TABLE IF EXISTS tmp_t2_design_resource_seed;

-- ---------------------------------------------------------------------
-- 7. Fault pipe original design parameters
-- ---------------------------------------------------------------------

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

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- 05 Refined optimization catalog data
-- =====================================================================
-- Source: sql\t2_refined_optimization_plan_init.sql

-- Refined two-subtask optimization plan for t2_ tables.
-- Run after `t2_rule_fields_upgrade.sql`.
-- The layout task keeps executable-rule fields reserved for future algorithm integration.

SET NAMES utf8mb4;

UPDATE t2_design_objective_catalog
SET status = '1',
    update_by = 'admin',
    update_time = SYSDATE()
WHERE item_code IN (
  'STR_SPACE_SUPPORT_MAX',
  'STR_PIPE_CLEARANCE',
  'HYD_PRESSURE_DROP_MIN',
  'HYD_CLAMP_VALID',
  'HYD_PRESSURE_VELOCITY_INPUT',
  'HYD_VALVE_CLOSE_TIME',
  'LAY_COMPACTNESS_MAX',
  'LAY_DOOR_ENVELOPE_AVOID',
  'LAY_HYDRAULIC_PIPE_FIXED',
  'AERO_DOOR_GAP_CLEARANCE',
  'MFG_MAINTENANCE_ACCESS_MAX',
  'MFG_BEND_ANGLE_RANGE',
  'MFG_WALL_THICKNESS_LIMIT'
);

INSERT INTO t2_design_objective_catalog
  (discipline, discipline_name, item_type, item_code, item_name, direction,
   default_weight, default_limit_value, unit, sort_order, status, remark,
   rule_type, rule_expression, target_field, reference_field, operator_code,
   threshold_value, execute_mode, rule_payload, create_by, create_time)
VALUES
  ('hydraulic', '液压工程师', 'objective', 'HYD_STRESS_MIN', '最大等效应力最小', 'min',
   50, '', 'MPa', 10, '0', '液压弯管抗冲击优化核心目标',
   'objective_metric', 'min(maxEquivalentStress)', 'maxEquivalentStress', '', 'minimize',
   '', 'reserved', '', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'objective', 'HYD_DEFORMATION_MIN', '最大变形量最小', 'min',
   50, '', 'mm', 20, '0', '液压弯管抗冲击优化核心目标',
   'objective_metric', 'min(maxDeformation)', 'maxDeformation', '', 'minimize',
   '', 'reserved', '', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_STRESS_LIMIT', '最大等效应力不超过许用应力', '<=',
   50, '', 'MPa', 30, '0', '后续由仿真或代理模型返回应力值后执行',
   'strength_check', 'maxEquivalentStress <= allowableStress', 'maxEquivalentStress', 'allowableStress', 'less_equal',
   '', 'reserved', '', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_DEFORMATION_LIMIT', '最大变形量不超过允许变形', '<=',
   50, '', 'mm', 40, '0', '后续由仿真或代理模型返回变形值后执行',
   'deformation_check', 'maxDeformation <= allowableDeformation', 'maxDeformation', 'allowableDeformation', 'less_equal',
   '', 'reserved', '', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_MIN_BEND_RADIUS', '液压管弯曲半径不小于下限', '>=',
   50, '20', 'mm', 50, '0', '当前可按变量边界执行',
   'radius_check', 'PIPE_BEND_RADIUS >= minPipeBendRadius', 'PIPE_BEND_RADIUS', 'minPipeBendRadius', 'greater_equal',
   '20', 'reserved', '', 'admin', SYSDATE()),

  ('layout', '布局工程师', 'objective', 'LAY_INTERFERENCE_RISK_MIN', '线缆与液压管干涉风险最小', 'min',
   50, '', 'risk', 60, '0', '布局算法接入前先保留指标字段',
   'objective_metric', 'min(interferenceRisk)', 'interferenceRisk', '', 'minimize',
   '', 'reserved', '', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'objective', 'LAY_CABLE_LENGTH_MIN', '线缆路径长度最小', 'min',
   50, '', 'm', 70, '0', '布局算法接入前先保留指标字段',
   'objective_metric', 'min(cablePathLength)', 'cablePathLength', '', 'minimize',
   '', 'reserved', '', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'objective', 'LAY_MAINTAINABILITY_MAX', '维护可达性最大', 'max',
   50, '', 'score', 80, '0', '布局算法接入前先保留指标字段',
   'objective_metric', 'max(maintainabilityScore)', 'maintainabilityScore', '', 'maximize',
   '', 'reserved', '', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_PIPE_CLEARANCE_LIMIT', '线缆与液压管保持安全间距', '>=',
   50, '40', 'mm', 90, '0', '可执行为最小距离校核',
   'clearance_check', 'minCablePipeClearance >= CABLE_PIPE_CLEARANCE', 'minCablePipeClearance', 'CABLE_PIPE_CLEARANCE', 'greater_equal',
   '40', 'reserved', '', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_FORBIDDEN_ZONE_AVOID', '线缆不得穿越结构禁布区域', 'avoid',
   50, '', '', 100, '0', '可执行为路径与禁布区域的几何相交校核',
   'geometry_intersection', 'intersect(cableRouteGeometry, structureForbiddenZones) = false', 'cableRouteGeometry', 'structureForbiddenZones', 'none_intersect',
   '', 'reserved', '', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_CABLE_BEND_RADIUS_LIMIT', '线缆弯曲半径满足要求', '>=',
   50, '50', 'mm', 110, '0', '可执行为弯曲半径下限校核',
   'radius_check', 'actualCableBendRadius >= CABLE_BEND_RADIUS', 'actualCableBendRadius', 'CABLE_BEND_RADIUS', 'greater_equal',
   '50', 'reserved', '', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_CLAMP_SPACING_LIMIT', '线夹间距不超过上限', '<=',
   50, '250', 'mm', 120, '0', '可执行为线夹间距上限校核',
   'spacing_check', 'actualClampSpacing <= CLAMP_SPACING', 'actualClampSpacing', 'CLAMP_SPACING', 'less_equal',
   '250', 'reserved', '', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_SERVICE_MARGIN_LIMIT', '检修空间满足要求', '>=',
   50, '30', 'mm', 130, '0', '可执行为检修空间下限校核',
   'clearance_check', 'actualServiceMargin >= SERVICE_MARGIN', 'actualServiceMargin', 'SERVICE_MARGIN', 'greater_equal',
   '30', 'reserved', '', 'admin', SYSDATE()),

  ('structure', '结构工程师', 'objective', 'STR_DEFORMATION_RISK_MIN', '管线变形碰撞风险最小', 'min',
   50, '', 'risk', 140, '0', '结构侧保留与管线变形相关的核心目标',
   'objective_metric', 'min(deformationCollisionRisk)', 'deformationCollisionRisk', '', 'minimize',
   '', 'reserved', '', 'admin', SYSDATE()),
  ('structure', '结构工程师', 'constraint', 'STR_FORBIDDEN_ZONE', '结构禁布区域不可穿越', 'avoid',
   50, '', '', 150, '0', '可执行为路径与结构禁布区域几何相交校核',
   'geometry_intersection', 'intersect(routeGeometry, structureForbiddenZones) = false', 'routeGeometry', 'structureForbiddenZones', 'none_intersect',
   '', 'reserved', '', 'admin', SYSDATE()),
  ('structure', '结构工程师', 'constraint', 'STR_INTERFACE_FIXED', '铰链、锁机构、作动器接口位置不可更改', 'fixed',
   50, '', '', 160, '0', '可执行为关键接口点与基准点一致性校核',
   'boundary_lock', 'interfacePoints = baselineInterfacePoints', 'interfacePoints', 'baselineInterfacePoints', 'equal',
   '', 'reserved', '', 'admin', SYSDATE()),
  ('structure', '结构工程师', 'constraint', 'STR_CLAMP_SUPPORT_VALID', '卡箍支撑点结构强度满足要求', 'meet',
   50, '', '', 170, '0', '可执行为支撑点强度校核',
   'strength_check', 'clampSupportStrength >= requiredSupportStrength', 'clampSupportStrength', 'requiredSupportStrength', 'pass',
   '', 'reserved', '', 'admin', SYSDATE()),

  ('aero', '气动工程师', 'objective', 'AERO_ENVELOPE_IMPACT_MIN', '对舱门外形包络影响最小', 'min',
   50, '', 'score', 180, '0', '气动侧仅保留包络影响核心项',
   'objective_metric', 'min(envelopeImpactScore)', 'envelopeImpactScore', '', 'minimize',
   '', 'reserved', '', 'admin', SYSDATE()),
  ('aero', '气动工程师', 'constraint', 'AERO_OUTER_ENVELOPE', '管线与附件不得超出舱门外形包络', '<=',
   50, '', 'mm', 190, '0', '可执行为包络内外关系校核',
   'envelope_check', 'routeEnvelope inside doorOuterEnvelope', 'routeEnvelope', 'doorOuterEnvelope', 'inside_or_equal',
   '', 'reserved', '', 'admin', SYSDATE()),

  ('manufacturing', '制造工程师', 'objective', 'MFG_PROCESS_COMPLEXITY_MIN', '管线制造加工复杂度最小', 'min',
   50, '', 'score', 200, '0', '制造侧保留加工复杂度目标',
   'objective_metric', 'min(processComplexityScore)', 'processComplexityScore', '', 'minimize',
   '', 'reserved', '', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'objective', 'MFG_ASSEMBLY_EFFICIENCY_MAX', '装配效率最大', 'max',
   50, '', 'score', 210, '0', '制造侧保留装配效率目标',
   'objective_metric', 'max(assemblyEfficiencyScore)', 'assemblyEfficiencyScore', '', 'maximize',
   '', 'reserved', '', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'constraint', 'MFG_BEND_RADIUS_LIMIT', '弯曲半径满足制造下限', '>=',
   50, '20', 'mm', 220, '0', '可执行为制造弯曲半径下限校核',
   'radius_check', 'bendRadius >= manufacturingMinBendRadius', 'bendRadius', 'manufacturingMinBendRadius', 'greater_equal',
   '20', 'reserved', '', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'constraint', 'MFG_CLAMP_INSTALLABLE', '管夹/线夹可安装', 'meet',
   50, '', '', 230, '0', '可执行为安装空间/孔位可用性校核',
   'installability_check', 'clampInstallability = pass', 'clampInstallability', '', 'pass',
   '', 'reserved', '', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'constraint', 'MFG_TOOL_ACCESS', '工具操作空间满足要求', 'meet',
   50, '', '', 240, '0', '可执行为工具可达性校核',
   'accessibility_check', 'toolAccessStatus = pass', 'toolAccessStatus', '', 'pass',
   '', 'reserved', '', 'admin', SYSDATE())
ON DUPLICATE KEY UPDATE
  discipline = VALUES(discipline),
  discipline_name = VALUES(discipline_name),
  item_type = VALUES(item_type),
  item_name = VALUES(item_name),
  direction = VALUES(direction),
  default_weight = VALUES(default_weight),
  default_limit_value = VALUES(default_limit_value),
  unit = VALUES(unit),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  remark = VALUES(remark),
  rule_type = VALUES(rule_type),
  rule_expression = VALUES(rule_expression),
  target_field = VALUES(target_field),
  reference_field = VALUES(reference_field),
  operator_code = VALUES(operator_code),
  threshold_value = VALUES(threshold_value),
  execute_mode = VALUES(execute_mode),
  rule_payload = VALUES(rule_payload),
  update_by = 'admin',
  update_time = SYSDATE();

UPDATE t2_design_variable_catalog
SET status = '1',
    update_by = 'admin',
    update_time = SYSDATE()
WHERE variable_code NOT IN (
  'PIPE_L1', 'PIPE_L2', 'PIPE_BEND_RADIUS', 'PIPE_THETA_1', 'PIPE_THETA_2',
  'CABLE_ROUTE_SIDE', 'CABLE_PIPE_CLEARANCE', 'CABLE_OFFSET',
  'CABLE_BEND_RADIUS', 'CLAMP_SPACING', 'SERVICE_MARGIN'
);

INSERT INTO t2_design_variable_catalog
  (discipline, discipline_name, subtask_code, variable_code, variable_name, variable_type,
   default_value, lower_bound, upper_bound, step_value, unit, sort_order, status, remark, create_by, create_time)
VALUES
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_L1', 'L1 第一段直管长度', 'continuous', '300', '50', '550', '1', 'mm', 10, '0', '液压弯管抗冲击优化变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_L2', 'L2 第二段直管长度', 'continuous', '150', '50', '300', '1', 'mm', 20, '0', '液压弯管抗冲击优化变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_BEND_RADIUS', 'R 两处弯管圆角半径', 'continuous', '30', '20', '80', '1', 'mm', 30, '0', '液压弯管抗冲击优化变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_THETA_1', 'θ1 第一个弯角弯曲角度', 'continuous', '110', '30', '150', '1', 'deg', 40, '0', '液压弯管抗冲击优化变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_THETA_2', 'θ2 第二个弯角弯曲角度', 'continuous', '120', '30', '150', '1', 'deg', 50, '0', '液压弯管抗冲击优化变量', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CABLE_ROUTE_SIDE', '线缆布置侧别', 'enum', 'upper', 'upper', 'outer', '', '', 60, '0', '预留布局策略字段：upper/lower/inner/outer', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CABLE_PIPE_CLEARANCE', '线缆与液压管最小隔离距离', 'continuous', '40', '20', '80', '1', 'mm', 70, '0', '预留最小距离校核字段', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CABLE_OFFSET', '线缆相对液压管中心线偏移距离', 'continuous', '60', '30', '120', '1', 'mm', 80, '0', '预留路径偏移字段', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CABLE_BEND_RADIUS', '线缆最小弯曲半径', 'continuous', '50', '30', '120', '1', 'mm', 90, '0', '预留弯曲半径校核字段', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CLAMP_SPACING', '线夹/管夹布置间距', 'continuous', '180', '100', '250', '5', 'mm', 100, '0', '预留线夹间距校核字段', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'SERVICE_MARGIN', '检修操作预留空间', 'continuous', '40', '30', '100', '1', 'mm', 110, '0', '预留检修空间校核字段', 'admin', SYSDATE())
ON DUPLICATE KEY UPDATE
  discipline = VALUES(discipline),
  discipline_name = VALUES(discipline_name),
  subtask_code = VALUES(subtask_code),
  variable_name = VALUES(variable_name),
  variable_type = VALUES(variable_type),
  default_value = VALUES(default_value),
  lower_bound = VALUES(lower_bound),
  upper_bound = VALUES(upper_bound),
  step_value = VALUES(step_value),
  unit = VALUES(unit),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  remark = VALUES(remark),
  update_by = 'admin',
  update_time = SYSDATE();

-- =====================================================================
-- 06 Pre-decomposition cleanup data fix
-- =====================================================================
-- Source: sql\t2_pre_decomposition_cleanup.sql

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

-- =====================================================================
-- 07 Fault-pipe diameter data update
-- =====================================================================
-- Source: sql\t2_fault_pipe_diameter_update.sql

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

-- =====================================================================
-- 08 Frame-beam crack life schema
-- =====================================================================
-- Source: sql\t2_frame_beam_crack_life_schema.sql

-- Project22 / frame beam crack life prediction schema.
-- This script is independent from the existing hydraulic surrogate tables.

CREATE TABLE IF NOT EXISTS t2_frame_beam_crack_input (
  id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  task_id BIGINT(20) NOT NULL COMMENT 'Design task ID',
  input_json LONGTEXT NOT NULL COMMENT 'Crack, material, and structure input JSON',
  create_by VARCHAR(64) DEFAULT '' COMMENT 'Creator',
  create_time DATETIME DEFAULT NULL COMMENT 'Create time',
  update_by VARCHAR(64) DEFAULT '' COMMENT 'Updater',
  update_time DATETIME DEFAULT NULL COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_t2_frame_beam_crack_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Frame beam crack input';

CREATE TABLE IF NOT EXISTS t2_frame_beam_load_spectrum (
  id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  task_id BIGINT(20) NOT NULL COMMENT 'Design task ID',
  spectrum_name VARCHAR(160) DEFAULT '' COMMENT 'Load spectrum name',
  file_name VARCHAR(260) DEFAULT '' COMMENT 'Original or uploaded file name',
  file_path VARCHAR(800) DEFAULT '' COMMENT 'Stored file path',
  spectrum_json LONGTEXT NOT NULL COMMENT 'Load spectrum JSON',
  create_by VARCHAR(64) DEFAULT '' COMMENT 'Creator',
  create_time DATETIME DEFAULT NULL COMMENT 'Create time',
  update_by VARCHAR(64) DEFAULT '' COMMENT 'Updater',
  update_time DATETIME DEFAULT NULL COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_t2_frame_beam_spectrum_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Frame beam load spectrum';

CREATE TABLE IF NOT EXISTS t2_frame_beam_life_prediction (
  id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  task_id BIGINT(20) NOT NULL COMMENT 'Design task ID',
  status VARCHAR(30) DEFAULT 'NOT_SUBMITTED' COMMENT 'Prediction status',
  risk_level VARCHAR(30) DEFAULT '' COMMENT 'LOW/MEDIUM/HIGH/CRITICAL',
  prediction_json LONGTEXT NOT NULL COMMENT 'Prediction response JSON',
  create_by VARCHAR(64) DEFAULT '' COMMENT 'Creator',
  create_time DATETIME DEFAULT NULL COMMENT 'Create time',
  update_by VARCHAR(64) DEFAULT '' COMMENT 'Updater',
  update_time DATETIME DEFAULT NULL COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_t2_frame_beam_prediction_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Frame beam life prediction';

CREATE TABLE IF NOT EXISTS t2_frame_beam_maintenance_advice (
  id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  task_id BIGINT(20) NOT NULL COMMENT 'Design task ID',
  advice_status VARCHAR(30) DEFAULT 'SYSTEM_GENERATED' COMMENT 'Advice status',
  advice_json LONGTEXT NOT NULL COMMENT 'Maintenance advice JSON',
  create_by VARCHAR(64) DEFAULT '' COMMENT 'Creator',
  create_time DATETIME DEFAULT NULL COMMENT 'Create time',
  update_by VARCHAR(64) DEFAULT '' COMMENT 'Updater',
  update_time DATETIME DEFAULT NULL COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_t2_frame_beam_advice_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Frame beam maintenance advice';

-- =====================================================================
-- 09 Frame-beam crack life menu
-- =====================================================================
-- Source: sql\t2_frame_beam_crack_life_menu.sql

-- Project22 / frame beam crack life prediction menu and permissions.
-- Parent menu 2600 is the existing "设计制造协同优化平台".
-- Adjust IDs if your sys_menu already uses 2630-2644.

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, update_by, update_time, remark)
VALUES
  (2630, '任务协同', 2600, 2, 'task-collaboration', NULL, '', '',
   1, 0, 'M', '0', '0', '', 'guide', 'admin', SYSDATE(), '', NULL, 'Project22 grouped menu'),
  (2631, '模型分析', 2600, 4, 'model-analysis', NULL, '', '',
   1, 0, 'M', '0', '0', '', 'component', 'admin', SYSDATE(), '', NULL, 'Project22 grouped menu'),
  (2632, '验证与决策', 2600, 5, 'verification-decision', NULL, '', '',
   1, 0, 'M', '0', '0', '', 'checkbox', 'admin', SYSDATE(), '', NULL, 'Project22 grouped menu'),
  (2633, '寿命预测评估', 2631, 2, 'life-prediction', 'designtask/frameBeam/life-prediction', '', '',
   1, 0, 'C', '0', '0', 'designtask:framebeam:predict', 'chart', 'admin', SYSDATE(), '', NULL, 'Frame beam life prediction'),
  (2634, '决策建议确认', 2632, 2, 'decision-advice', 'designtask/frameBeam/decision-advice', '', '',
   1, 0, 'C', '0', '0', 'designtask:framebeam:advice', 'checkbox', 'admin', SYSDATE(), '', NULL, 'Frame beam maintenance advice')
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  parent_id = VALUES(parent_id),
  order_num = VALUES(order_num),
  path = VALUES(path),
  component = VALUES(component),
  menu_type = VALUES(menu_type),
  visible = VALUES(visible),
  status = VALUES(status),
  perms = VALUES(perms),
  icon = VALUES(icon),
  update_time = SYSDATE();

-- Optional grouping for existing design-platform pages.
-- Keep the original menu IDs, only move them under the new grouped menus.
UPDATE sys_menu SET parent_id = 2630, order_num = 1, update_time = SYSDATE()
WHERE menu_id = 2602 AND menu_name = '协同机制生成';

UPDATE sys_menu SET parent_id = 2630, order_num = 2, update_time = SYSDATE()
WHERE menu_id = 2603 AND menu_name = '目标约束选择';

UPDATE sys_menu SET parent_id = 2631, order_num = 1, update_time = SYSDATE()
WHERE menu_id = 2604 AND menu_name = '模型解耦求解';

UPDATE sys_menu SET parent_id = 2632, order_num = 1, update_time = SYSDATE()
WHERE menu_id = 2605 AND menu_name = '仿真验证确认';

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, update_by, update_time, remark)
VALUES
  (2640, '框梁裂纹输入', 2633, 1, '', '', '', '', 1, 0, 'F', '0', '0',
   'designtask:framebeam:input', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2641, '框梁寿命预测', 2633, 2, '', '', '', '', 1, 0, 'F', '0', '0',
   'designtask:framebeam:predict', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2642, '框梁维修建议', 2634, 1, '', '', '', '', 1, 0, 'F', '0', '0',
   'designtask:framebeam:advice', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2643, '框梁专家审批', 2634, 2, '', '', '', '', 1, 0, 'F', '0', '0',
   'designtask:framebeam:approve', '#', 'admin', SYSDATE(), '', NULL, '')
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  parent_id = VALUES(parent_id),
  order_num = VALUES(order_num),
  perms = VALUES(perms),
  update_time = SYSDATE();

-- =====================================================================
-- 10 Frame-beam role-menu grants
-- =====================================================================
-- Grant frame-beam menu permissions to project2 roles.
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (2630, 2631, 2632, 2633, 2634, 2640, 2641, 2642, 2643)
WHERE r.role_key IN ('design_task_owner', 'admin');

INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (2630, 2631, 2633, 2640, 2641)
WHERE r.role_key IN ('structure_engineer');

INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (2632, 2634, 2642)
WHERE r.role_key IN ('manufacturing_engineer');

INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (2632, 2634, 2643)
WHERE r.role_key IN ('approval_leader');

-- =====================================================================
-- 11 Remove standard resource menu from design platform
-- =====================================================================
-- Source: sql\t2_remove_standard_resource_menu.sql

-- Remove the Standard Resource Management menu from the design platform.
-- This only removes menu entries and role-menu grants. Resource data tables are kept intact.

SET NAMES utf8mb4;

DELETE FROM sys_role_menu
WHERE menu_id IN (2614, 2615)
   OR menu_id IN (
     SELECT menu_id
     FROM sys_menu
     WHERE perms IN ('designtask:resource:list', 'designtask:resource:add')
        OR component = 'designtask/resource/index'
   );

DELETE FROM sys_menu
WHERE menu_id IN (2615, 2614)
   OR perms IN ('designtask:resource:add', 'designtask:resource:list')
   OR component = 'designtask/resource/index';

-- =====================================================================
-- 12 Footer
-- =====================================================================
SET FOREIGN_KEY_CHECKS = 1;
