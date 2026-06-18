-- Add neutral CAD geometry paths used by ANSYS import.

SET NAMES utf8mb4;

SET @has_step_path := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 't2_cad_model_task'
    AND column_name = 'step_path'
);
SET @ddl := IF(@has_step_path = 0,
  'ALTER TABLE t2_cad_model_task ADD COLUMN step_path varchar(1000) DEFAULT NULL COMMENT ''STEP path for ANSYS import''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_ansys_error_message := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 't2_design_ansys_simulation_task'
    AND column_name = 'error_message'
);
SET @ddl := IF(@has_ansys_error_message = 1,
  'ALTER TABLE t2_design_ansys_simulation_task MODIFY COLUMN error_message text COMMENT ''ANSYS worker error message and diagnostics''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_parasolid_path := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 't2_cad_model_task'
    AND column_name = 'parasolid_path'
);
SET @ddl := IF(@has_parasolid_path = 0,
  'ALTER TABLE t2_cad_model_task ADD COLUMN parasolid_path varchar(1000) DEFAULT NULL COMMENT ''Parasolid path for ANSYS import''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_centerline_csv_path := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 't2_cad_model_task'
    AND column_name = 'centerline_csv_path'
);
SET @ddl := IF(@has_centerline_csv_path = 0,
  'ALTER TABLE t2_cad_model_task ADD COLUMN centerline_csv_path varchar(1000) DEFAULT NULL COMMENT ''Pipe centerline CSV path for ANSYS inner-wall selection''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
