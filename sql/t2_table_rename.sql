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
