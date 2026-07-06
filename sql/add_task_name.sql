ALTER TABLE `t3_fault_iden_sample`
    ADD COLUMN `task_name` VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'name of the data import task' AFTER `data_usage`,
    ADD COLUMN `upload_batch_id` VARCHAR(96) NOT NULL DEFAULT '' COMMENT 'upload batch used to distinguish import tasks' AFTER `task_name`;

ALTER TABLE `t3_fault_iden_sample`
    DROP INDEX `uk_fault_iden_sample`,
    DROP INDEX `uk_fault_iden_sample_file`,
    ADD UNIQUE KEY `uk_fault_iden_sample` (`condition_label`, `bearing_code`, `sample_no`, `data_usage`, `task_name`),
    ADD UNIQUE KEY `uk_fault_iden_sample_file` (`condition_label`, `bearing_code`, `file_name`, `data_usage`, `task_name`),
    ADD KEY `idx_fault_iden_sample_task_name` (`task_name`),
    ADD KEY `idx_fault_iden_sample_task_batch` (`data_usage`, `task_name`, `upload_batch_id`);
