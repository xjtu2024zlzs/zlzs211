-- 平台二期表名前缀迁移脚本。
-- 用途：如果数据库里已经存在旧的 design_* 表，执行本脚本将其重命名为 p2_design_*。
-- 注意：执行前请先备份数据库；如果目标 p2_* 表已经存在，对应旧表不会被重命名。

DELIMITER $$

DROP PROCEDURE IF EXISTS rename_table_if_exists $$
CREATE PROCEDURE rename_table_if_exists(
    IN old_table_name VARCHAR(128),
    IN new_table_name VARCHAR(128)
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
END $$

DELIMITER ;

CALL rename_table_if_exists('design_task', 'p2_design_task');
CALL rename_table_if_exists('design_template', 'p2_design_template');
CALL rename_table_if_exists('design_template_node', 'p2_design_template_node');
CALL rename_table_if_exists('design_template_node_role', 'p2_design_template_node_role');
CALL rename_table_if_exists('design_task_node', 'p2_design_task_node');
CALL rename_table_if_exists('design_task_file', 'p2_design_task_file');
CALL rename_table_if_exists('design_task_log', 'p2_design_task_log');
CALL rename_table_if_exists('design_objective_catalog', 'p2_design_objective_catalog');
CALL rename_table_if_exists('design_objective_constraint', 'p2_design_objective_constraint');
CALL rename_table_if_exists('design_conflict_check', 'p2_design_conflict_check');
CALL rename_table_if_exists('design_subtask_solution', 'p2_design_subtask_solution');
CALL rename_table_if_exists('design_simulation_result', 'p2_design_simulation_result');
CALL rename_table_if_exists('design_approval_record', 'p2_design_approval_record');
CALL rename_table_if_exists('design_resource', 'p2_design_resource');

DROP PROCEDURE IF EXISTS rename_table_if_exists;
