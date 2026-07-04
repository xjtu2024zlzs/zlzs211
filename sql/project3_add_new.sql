-- ============================================================
-- Project3 incremental schema additions.
-- Use this on an existing database created from the older project3 SQL.
-- Safe to execute repeatedly on MySQL 8.
-- ============================================================
SET NAMES utf8mb4;

-- t3_fault_iden_sample: import task metadata.
SET @task_name_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't3_fault_iden_sample'
    AND COLUMN_NAME = 'task_name'
);
SET @task_name_ddl := IF(
  @task_name_exists = 0,
  'ALTER TABLE `t3_fault_iden_sample` ADD COLUMN `task_name` VARCHAR(128) NOT NULL DEFAULT '''' COMMENT ''name of the data import task'' AFTER `data_usage`',
  'SELECT 1'
);
PREPARE task_name_stmt FROM @task_name_ddl;
EXECUTE task_name_stmt;
DEALLOCATE PREPARE task_name_stmt;

SET @upload_batch_id_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't3_fault_iden_sample'
    AND COLUMN_NAME = 'upload_batch_id'
);
SET @upload_batch_id_ddl := IF(
  @upload_batch_id_exists = 0,
  'ALTER TABLE `t3_fault_iden_sample` ADD COLUMN `upload_batch_id` VARCHAR(96) NOT NULL DEFAULT '''' COMMENT ''upload batch used to distinguish import tasks'' AFTER `task_name`',
  'SELECT 1'
);
PREPARE upload_batch_id_stmt FROM @upload_batch_id_ddl;
EXECUTE upload_batch_id_stmt;
DEALLOCATE PREPARE upload_batch_id_stmt;

-- Rebuild old unique indexes so different import tasks can reuse the same sample/file names.
SET @uk_fault_iden_sample_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't3_fault_iden_sample'
    AND INDEX_NAME = 'uk_fault_iden_sample'
);
SET @drop_uk_fault_iden_sample_ddl := IF(
  @uk_fault_iden_sample_exists > 0,
  'ALTER TABLE `t3_fault_iden_sample` DROP INDEX `uk_fault_iden_sample`',
  'SELECT 1'
);
PREPARE drop_uk_fault_iden_sample_stmt FROM @drop_uk_fault_iden_sample_ddl;
EXECUTE drop_uk_fault_iden_sample_stmt;
DEALLOCATE PREPARE drop_uk_fault_iden_sample_stmt;

SET @uk_fault_iden_sample_file_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't3_fault_iden_sample'
    AND INDEX_NAME = 'uk_fault_iden_sample_file'
);
SET @drop_uk_fault_iden_sample_file_ddl := IF(
  @uk_fault_iden_sample_file_exists > 0,
  'ALTER TABLE `t3_fault_iden_sample` DROP INDEX `uk_fault_iden_sample_file`',
  'SELECT 1'
);
PREPARE drop_uk_fault_iden_sample_file_stmt FROM @drop_uk_fault_iden_sample_file_ddl;
EXECUTE drop_uk_fault_iden_sample_file_stmt;
DEALLOCATE PREPARE drop_uk_fault_iden_sample_file_stmt;

SET @add_uk_fault_iden_sample_ddl := 'ALTER TABLE `t3_fault_iden_sample` ADD UNIQUE KEY `uk_fault_iden_sample` (`condition_label`, `bearing_code`, `sample_no`, `data_usage`, `task_name`)';
PREPARE add_uk_fault_iden_sample_stmt FROM @add_uk_fault_iden_sample_ddl;
EXECUTE add_uk_fault_iden_sample_stmt;
DEALLOCATE PREPARE add_uk_fault_iden_sample_stmt;

SET @add_uk_fault_iden_sample_file_ddl := 'ALTER TABLE `t3_fault_iden_sample` ADD UNIQUE KEY `uk_fault_iden_sample_file` (`condition_label`, `bearing_code`, `file_name`, `data_usage`, `task_name`)';
PREPARE add_uk_fault_iden_sample_file_stmt FROM @add_uk_fault_iden_sample_file_ddl;
EXECUTE add_uk_fault_iden_sample_file_stmt;
DEALLOCATE PREPARE add_uk_fault_iden_sample_file_stmt;

SET @idx_fault_iden_sample_task_name_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't3_fault_iden_sample'
    AND INDEX_NAME = 'idx_fault_iden_sample_task_name'
);
SET @idx_fault_iden_sample_task_name_ddl := IF(
  @idx_fault_iden_sample_task_name_exists = 0,
  'ALTER TABLE `t3_fault_iden_sample` ADD KEY `idx_fault_iden_sample_task_name` (`task_name`)',
  'SELECT 1'
);
PREPARE idx_fault_iden_sample_task_name_stmt FROM @idx_fault_iden_sample_task_name_ddl;
EXECUTE idx_fault_iden_sample_task_name_stmt;
DEALLOCATE PREPARE idx_fault_iden_sample_task_name_stmt;

SET @idx_fault_iden_sample_task_batch_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't3_fault_iden_sample'
    AND INDEX_NAME = 'idx_fault_iden_sample_task_batch'
);
SET @idx_fault_iden_sample_task_batch_ddl := IF(
  @idx_fault_iden_sample_task_batch_exists = 0,
  'ALTER TABLE `t3_fault_iden_sample` ADD KEY `idx_fault_iden_sample_task_batch` (`data_usage`, `task_name`, `upload_batch_id`)',
  'SELECT 1'
);
PREPARE idx_fault_iden_sample_task_batch_stmt FROM @idx_fault_iden_sample_task_batch_ddl;
EXECUTE idx_fault_iden_sample_task_batch_stmt;
DEALLOCATE PREPARE idx_fault_iden_sample_task_batch_stmt;

-- t3_algorithm_task_results: async task status sync metadata.
SET @status_version_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't3_algorithm_task_results'
    AND COLUMN_NAME = 'status_version'
);
SET @status_version_ddl := IF(
  @status_version_exists = 0,
  'ALTER TABLE `t3_algorithm_task_results` ADD COLUMN `status_version` bigint NOT NULL DEFAULT 0 COMMENT ''任务状态版本号，每次合法状态同步递增'' AFTER `status`',
  'SELECT 1'
);
PREPARE status_version_stmt FROM @status_version_ddl;
EXECUTE status_version_stmt;
DEALLOCATE PREPARE status_version_stmt;

SET @last_sync_at_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't3_algorithm_task_results'
    AND COLUMN_NAME = 'last_sync_at'
);
SET @last_sync_at_ddl := IF(
  @last_sync_at_exists = 0,
  'ALTER TABLE `t3_algorithm_task_results` ADD COLUMN `last_sync_at` datetime DEFAULT NULL COMMENT ''最近一次与算法服务同步时间'' AFTER `status_version`',
  'SELECT 1'
);
PREPARE last_sync_at_stmt FROM @last_sync_at_ddl;
EXECUTE last_sync_at_stmt;
DEALLOCATE PREPARE last_sync_at_stmt;

SET @last_sync_error_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't3_algorithm_task_results'
    AND COLUMN_NAME = 'last_sync_error'
);
SET @last_sync_error_ddl := IF(
  @last_sync_error_exists = 0,
  'ALTER TABLE `t3_algorithm_task_results` ADD COLUMN `last_sync_error` varchar(1000) DEFAULT NULL COMMENT ''最近一次状态同步错误'' AFTER `last_sync_at`',
  'SELECT 1'
);
PREPARE last_sync_error_stmt FROM @last_sync_error_ddl;
EXECUTE last_sync_error_stmt;
DEALLOCATE PREPARE last_sync_error_stmt;

SET @sync_retry_count_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't3_algorithm_task_results'
    AND COLUMN_NAME = 'sync_retry_count'
);
SET @sync_retry_count_ddl := IF(
  @sync_retry_count_exists = 0,
  'ALTER TABLE `t3_algorithm_task_results` ADD COLUMN `sync_retry_count` int NOT NULL DEFAULT 0 COMMENT ''连续状态同步失败次数'' AFTER `last_sync_error`',
  'SELECT 1'
);
PREPARE sync_retry_count_stmt FROM @sync_retry_count_ddl;
EXECUTE sync_retry_count_stmt;
DEALLOCATE PREPARE sync_retry_count_stmt;