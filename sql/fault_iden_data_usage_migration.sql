ALTER TABLE `fault_iden_sample`
    ADD COLUMN `data_usage` VARCHAR(64) NOT NULL DEFAULT 'FEATURE_ANALYSIS' COMMENT 'FEATURE_ANALYSIS/FAULT_PREDICT/FAULT_IDENTIFY/COMMON' AFTER `file_size`,
    DROP INDEX `uk_fault_iden_sample`,
    ADD UNIQUE KEY `uk_fault_iden_sample` (`condition_label`, `bearing_code`, `sample_no`, `data_usage`),
    ADD KEY `idx_fault_iden_sample_data_usage` (`data_usage`);

ALTER TABLE `fault_iden_file_package`
    ADD COLUMN `data_usage` VARCHAR(64) NOT NULL DEFAULT 'FEATURE_ANALYSIS' COMMENT 'FEATURE_ANALYSIS/FAULT_PREDICT/FAULT_IDENTIFY/COMMON' AFTER `selected_sample_ids`,
    ADD KEY `idx_fault_iden_file_package_data_usage` (`data_usage`);

