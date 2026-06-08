SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `fault_iden_sample` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'primary key',
    `condition_label` VARCHAR(64) NOT NULL COMMENT 'condition folder, e.g. 35Hz12kN',
    `bearing_code` VARCHAR(64) NOT NULL COMMENT 'bearing folder, e.g. Bearing1_1',
    `sample_no` INT NOT NULL COMMENT 'sample number parsed from csv file name',
    `file_name` VARCHAR(255) NOT NULL COMMENT 'original csv file name',
    `source_file` VARCHAR(1000) NOT NULL COMMENT 'absolute original csv path on server',
    `file_size` BIGINT NULL COMMENT 'file size in bytes',
    `data_usage` VARCHAR(64) NOT NULL DEFAULT 'FEATURE_ANALYSIS' COMMENT 'FEATURE_ANALYSIS/FAULT_PREDICT/FAULT_IDENTIFY/COMMON',
    `aircraft_id` VARCHAR(50) NULL COMMENT 'aircraft id',
    `subsystem_id` VARCHAR(50) NULL COMMENT 'subsystem id',
    `equipment_id` VARCHAR(50) NULL COMMENT 'equipment id',
    `component_id` VARCHAR(50) NULL COMMENT 'component id',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    UNIQUE KEY `uk_fault_iden_sample` (`condition_label`, `bearing_code`, `sample_no`, `data_usage`),
    UNIQUE KEY `uk_fault_iden_sample_file` (`condition_label`, `bearing_code`, `file_name`, `data_usage`),
    KEY `idx_condition_bearing` (`condition_label`, `bearing_code`),
    KEY `idx_fault_iden_sample_data_usage` (`data_usage`),
    KEY `idx_file_name` (`file_name`),
    KEY `idx_fault_iden_sample_aircraft_id` (`aircraft_id`),
    KEY `idx_fault_iden_sample_subsystem_id` (`subsystem_id`),
    KEY `idx_fault_iden_sample_equipment_id` (`equipment_id`),
    KEY `idx_fault_iden_sample_component_id` (`component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Fault identification original csv sample file';

CREATE TABLE IF NOT EXISTS `fault_iden_file_package` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'primary key',
    `task_id` VARCHAR(64) NOT NULL COMMENT 'feature analysis task id',
    `file_mode` VARCHAR(32) NOT NULL COMMENT 'RAW_SINGLE or RAW_ZIP',
    `file_type` VARCHAR(32) NOT NULL COMMENT 'csv or zip',
    `file_name` VARCHAR(255) NOT NULL COMMENT 'file name passed to Python',
    `file_path` VARCHAR(1000) NULL COMMENT 'local zip path, null for single csv',
    `file_url` VARCHAR(1000) NOT NULL COMMENT 'download url for Python',
    `selected_sample_ids` TEXT NULL COMMENT 'selected sample ids as json array',
    `data_usage` VARCHAR(64) NOT NULL DEFAULT 'FEATURE_ANALYSIS' COMMENT 'FEATURE_ANALYSIS/FAULT_PREDICT/FAULT_IDENTIFY/COMMON',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    KEY `idx_task_id` (`task_id`),
    KEY `idx_fault_iden_file_package_data_usage` (`data_usage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Fault identification file passed to Python';
