-- project3 algorithm task synchronization metadata migration.
-- Safe to execute repeatedly on MySQL 8.

SET @status_version_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't3_algorithm_task_results' AND COLUMN_NAME = 'status_version'
);
SET @ddl := IF(@status_version_exists = 0,
  'ALTER TABLE `t3_algorithm_task_results` ADD COLUMN `status_version` bigint NOT NULL DEFAULT 0 COMMENT ''任务状态版本号，每次合法状态同步递增'' AFTER `status`',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @last_sync_at_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't3_algorithm_task_results' AND COLUMN_NAME = 'last_sync_at'
);
SET @ddl := IF(@last_sync_at_exists = 0,
  'ALTER TABLE `t3_algorithm_task_results` ADD COLUMN `last_sync_at` datetime DEFAULT NULL COMMENT ''最近一次与算法服务同步时间'' AFTER `status_version`',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @last_sync_error_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't3_algorithm_task_results' AND COLUMN_NAME = 'last_sync_error'
);
SET @ddl := IF(@last_sync_error_exists = 0,
  'ALTER TABLE `t3_algorithm_task_results` ADD COLUMN `last_sync_error` varchar(1000) DEFAULT NULL COMMENT ''最近一次状态同步错误'' AFTER `last_sync_at`',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sync_retry_count_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 't3_algorithm_task_results' AND COLUMN_NAME = 'sync_retry_count'
);
SET @ddl := IF(@sync_retry_count_exists = 0,
  'ALTER TABLE `t3_algorithm_task_results` ADD COLUMN `sync_retry_count` int NOT NULL DEFAULT 0 COMMENT ''连续状态同步失败次数'' AFTER `last_sync_error`',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
