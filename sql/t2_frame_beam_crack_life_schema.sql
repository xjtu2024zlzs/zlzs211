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
