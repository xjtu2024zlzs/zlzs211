-- ANSYS simulation placeholder task table.
-- The current platform stores preset placeholder results here before the real ANSYS worker is connected.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS t2_design_ansys_simulation_task (
  task_id bigint NOT NULL COMMENT 'Task ID',
  simulation_mode varchar(64) NOT NULL DEFAULT 'DEMO_SIMULATION_MODEL' COMMENT 'Simulation model mode',
  status varchar(32) NOT NULL COMMENT 'Simulation status',
  simulation_type varchar(100) DEFAULT NULL COMMENT 'Simulation type',
  input_json text COMMENT 'Simulation input JSON',
  result_json text COMMENT 'Simulation result JSON',
  metrics_json text COMMENT 'Simulation metrics JSON',
  stress_image_url varchar(1000) DEFAULT NULL COMMENT 'Stress contour image URL',
  result_file_path varchar(1000) DEFAULT NULL COMMENT 'ANSYS result file path',
  error_message varchar(1000) DEFAULT NULL COMMENT 'Error message',
  placeholder char(1) DEFAULT '1' COMMENT 'Placeholder result flag',
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (task_id, simulation_mode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ANSYS simulation task placeholder';
