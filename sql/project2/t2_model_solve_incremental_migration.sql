-- Incremental migration for design-manufacturing collaborative optimization.
-- MySQL 8.0.x. Safe to run repeatedly. It does not delete or reinitialize data.

CREATE TABLE IF NOT EXISTS t2_surrogate_solve_task (
  task_id bigint NOT NULL PRIMARY KEY,
  status varchar(32) NOT NULL,
  model_name varchar(255),
  model_type varchar(255),
  objective_name varchar(128),
  objective_unit varchar(64),
  params_json text,
  best_solution_json text,
  candidate_solutions_json text,
  iteration_history_json text,
  iterations int DEFAULT 0,
  error_message varchar(1000),
  confirmed char(1) DEFAULT '0',
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Surrogate model optimization task';

CREATE TABLE IF NOT EXISTS t2_cad_model_task (
  task_id bigint NOT NULL PRIMARY KEY,
  status varchar(32) NOT NULL,
  params_json text,
  l3 decimal(18,6),
  initial_angle decimal(18,6),
  closure_status varchar(128),
  error_message varchar(1000),
  sldprt_path varchar(1000),
  step_path varchar(1000),
  parasolid_path varchar(1000),
  stl_path varchar(1000),
  centerline_csv_path varchar(1000),
  preview_png_path varchar(1000),
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CAD model generation task';

CREATE TABLE IF NOT EXISTS t2_design_ansys_simulation_task (
  task_id bigint NOT NULL,
  simulation_mode varchar(64) NOT NULL DEFAULT 'DEMO_SIMULATION_MODEL',
  status varchar(32) NOT NULL,
  simulation_type varchar(100),
  input_json text,
  result_json text,
  metrics_json text,
  stress_image_url varchar(1000),
  result_file_path varchar(1000),
  error_message text,
  placeholder char(1) DEFAULT '1',
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (task_id, simulation_mode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ANSYS simulation task';

CREATE TABLE IF NOT EXISTS t2_design_report_submission (
  id bigint NOT NULL AUTO_INCREMENT,
  task_id bigint NOT NULL,
  report_code varchar(128) DEFAULT NULL,
  report_title varchar(255) DEFAULT NULL,
  report_json longtext,
  report_html longtext,
  report_file_id bigint DEFAULT NULL,
  report_file_path varchar(1000) DEFAULT NULL,
  report_file_name varchar(255) DEFAULT NULL,
  passed char(1) DEFAULT '1',
  submit_comment varchar(1000) DEFAULT NULL,
  submit_by varchar(64) DEFAULT '',
  submit_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_t2_design_report_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design report submission';

CREATE TABLE IF NOT EXISTS t2_design_simulation_result (
  id bigint NOT NULL AUTO_INCREMENT,
  task_id bigint NOT NULL,
  passed char(1) DEFAULT '1',
  metrics_json text,
  conclusion varchar(500) DEFAULT NULL,
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_t2_design_simulation_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Simulation verification result';

CREATE TABLE IF NOT EXISTS t2_design_approval_record (
  id bigint NOT NULL AUTO_INCREMENT,
  task_id bigint NOT NULL,
  approved char(1) DEFAULT '1',
  comment varchar(1000) DEFAULT NULL,
  approve_by varchar(64) DEFAULT '',
  approve_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_t2_design_approval_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Leader approval record';

CREATE TABLE IF NOT EXISTS t2_design_task_archive (
  id bigint NOT NULL AUTO_INCREMENT,
  task_id bigint NOT NULL,
  archive_json longtext,
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_t2_design_archive_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design task archive';

DELIMITER $$

DROP PROCEDURE IF EXISTS t2_add_column_if_missing $$
CREATE PROCEDURE t2_add_column_if_missing(
  IN p_table_name varchar(128),
  IN p_column_name varchar(128),
  IN p_column_definition varchar(1000)
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND column_name = p_column_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN ', p_column_name, ' ', p_column_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END $$

DELIMITER ;

CALL t2_add_column_if_missing('t2_surrogate_solve_task', 'model_name', 'varchar(255)');
CALL t2_add_column_if_missing('t2_surrogate_solve_task', 'model_type', 'varchar(255)');
CALL t2_add_column_if_missing('t2_surrogate_solve_task', 'objective_name', 'varchar(128)');
CALL t2_add_column_if_missing('t2_surrogate_solve_task', 'objective_unit', 'varchar(64)');
CALL t2_add_column_if_missing('t2_surrogate_solve_task', 'params_json', 'text');
CALL t2_add_column_if_missing('t2_surrogate_solve_task', 'best_solution_json', 'text');
CALL t2_add_column_if_missing('t2_surrogate_solve_task', 'candidate_solutions_json', 'text');
CALL t2_add_column_if_missing('t2_surrogate_solve_task', 'iteration_history_json', 'text');
CALL t2_add_column_if_missing('t2_surrogate_solve_task', 'iterations', 'int DEFAULT 0');
CALL t2_add_column_if_missing('t2_surrogate_solve_task', 'confirmed', 'char(1) DEFAULT ''0''');

CALL t2_add_column_if_missing('t2_cad_model_task', 'step_path', 'varchar(1000)');
CALL t2_add_column_if_missing('t2_cad_model_task', 'parasolid_path', 'varchar(1000)');
CALL t2_add_column_if_missing('t2_cad_model_task', 'centerline_csv_path', 'varchar(1000)');
CALL t2_add_column_if_missing('t2_cad_model_task', 'preview_png_path', 'varchar(1000)');

CALL t2_add_column_if_missing('t2_design_ansys_simulation_task', 'simulation_mode', 'varchar(64) NOT NULL DEFAULT ''DEMO_SIMULATION_MODEL''');
CALL t2_add_column_if_missing('t2_design_ansys_simulation_task', 'simulation_type', 'varchar(100)');
CALL t2_add_column_if_missing('t2_design_ansys_simulation_task', 'metrics_json', 'text');
CALL t2_add_column_if_missing('t2_design_ansys_simulation_task', 'stress_image_url', 'varchar(1000)');
CALL t2_add_column_if_missing('t2_design_ansys_simulation_task', 'result_file_path', 'varchar(1000)');
CALL t2_add_column_if_missing('t2_design_ansys_simulation_task', 'placeholder', 'char(1) DEFAULT ''1''');

UPDATE t2_design_ansys_simulation_task
SET simulation_mode = 'DEMO_SIMULATION_MODEL'
WHERE simulation_mode IS NULL OR simulation_mode = '';

ALTER TABLE t2_design_ansys_simulation_task MODIFY COLUMN error_message text;

SET @ansys_pk_cols = (
  SELECT COUNT(1)
  FROM information_schema.key_column_usage
  WHERE table_schema = DATABASE()
    AND table_name = 't2_design_ansys_simulation_task'
    AND constraint_name = 'PRIMARY'
    AND column_name IN ('task_id', 'simulation_mode')
);
SET @ansys_pk_total = (
  SELECT COUNT(1)
  FROM information_schema.key_column_usage
  WHERE table_schema = DATABASE()
    AND table_name = 't2_design_ansys_simulation_task'
    AND constraint_name = 'PRIMARY'
);
SET @ansys_pk_sql = IF(
  @ansys_pk_cols >= 2,
  'SELECT ''t2_design_ansys_simulation_task primary key already ok''',
  IF(
    @ansys_pk_total = 0,
    'ALTER TABLE t2_design_ansys_simulation_task ADD PRIMARY KEY (task_id, simulation_mode)',
    'ALTER TABLE t2_design_ansys_simulation_task DROP PRIMARY KEY, ADD PRIMARY KEY (task_id, simulation_mode)'
  )
);
PREPARE stmt FROM @ansys_pk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE sys_menu
SET component = 'designtask/solve/index'
WHERE menu_name = '模型解耦求解'
  AND (component IS NULL OR component <> 'designtask/solve/index');

UPDATE sys_menu
SET component = 'designtask/approval/index'
WHERE menu_name = '领导审批'
  AND (component IS NULL OR component <> 'designtask/approval/index');

DROP PROCEDURE IF EXISTS t2_add_column_if_missing;
