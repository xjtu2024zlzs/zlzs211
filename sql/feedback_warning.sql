-- 模块二：全域制造过程反馈监管

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `key_process_identifications`;
DROP TABLE IF EXISTS `process_anomalies`;
DROP TABLE IF EXISTS `processing_quality_data`;
DROP TABLE IF EXISTS `process_executions`;
DROP TABLE IF EXISTS `work_orders`;
DROP TABLE IF EXISTS `process_standard_params`;
DROP TABLE IF EXISTS `process_definitions`;
DROP TABLE IF EXISTS `process_routes`;
DROP TABLE IF EXISTS `manufacturing_devices`;

CREATE TABLE `manufacturing_devices` (
  `device_id` varchar(50) NOT NULL,
  `device_name` varchar(100) DEFAULT NULL,
  `device_type` varchar(50) DEFAULT NULL,
  `device_model` varchar(50) DEFAULT NULL,
  `device_status` varchar(20) DEFAULT NULL,
  `risk_level` varchar(20) DEFAULT NULL,
  `remarks` text,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `process_routes` (
  `route_id` varchar(50) NOT NULL,
  `part_template_id` varchar(50) DEFAULT NULL,
  `route_name` varchar(100) DEFAULT NULL,
  `version` varchar(20) DEFAULT NULL,
  `effective_date` date DEFAULT NULL,
  `is_active` tinyint DEFAULT NULL,
  PRIMARY KEY (`route_id`),
  KEY `idx_process_routes_part_template_id` (`part_template_id`),
  CONSTRAINT `fk_process_routes_part_template_id` FOREIGN KEY (`part_template_id`) REFERENCES `part_templates` (`part_template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `process_definitions` (
  `process_def_id` varchar(50) NOT NULL,
  `route_id` varchar(50) DEFAULT NULL,
  `process_number` int DEFAULT NULL,
  `process_name` varchar(100) DEFAULT NULL,
  `equipment_type` varchar(50) DEFAULT NULL,
  `standard_duration` decimal(10,2) DEFAULT NULL,
  `is_key_process` tinyint DEFAULT NULL,
  `is_high_risk` tinyint DEFAULT NULL,
  PRIMARY KEY (`process_def_id`),
  KEY `idx_process_definitions_route_id` (`route_id`),
  CONSTRAINT `fk_process_definitions_route_id` FOREIGN KEY (`route_id`) REFERENCES `process_routes` (`route_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `process_standard_params` (
  `standard_param_id` varchar(50) NOT NULL,
  `process_def_id` varchar(50) DEFAULT NULL,
  `param_name` varchar(100) DEFAULT NULL,
  `standard_value` varchar(100) DEFAULT NULL,
  `tolerance_upper` varchar(100) DEFAULT NULL,
  `tolerance_lower` varchar(100) DEFAULT NULL,
  `unit` varchar(20) DEFAULT NULL,
  `data_type` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`standard_param_id`),
  KEY `idx_process_standard_params_process_def_id` (`process_def_id`),
  CONSTRAINT `fk_process_standard_params_process_def_id` FOREIGN KEY (`process_def_id`) REFERENCES `process_definitions` (`process_def_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `work_orders` (
  `work_order_id` varchar(50) NOT NULL,
  `part_instance_id` varchar(50) DEFAULT NULL,
  `manufacturing_quality_id` varchar(50) DEFAULT NULL,
  `route_id` varchar(50) DEFAULT NULL,
  `production_line` varchar(50) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `operator` varchar(50) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`work_order_id`),
  KEY `idx_work_orders_part_instance_id` (`part_instance_id`),
  KEY `idx_work_orders_manufacturing_quality_id` (`manufacturing_quality_id`),
  KEY `idx_work_orders_route_id` (`route_id`),
  CONSTRAINT `fk_work_orders_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `part_instances` (`part_instance_id`),
  CONSTRAINT `fk_work_orders_manufacturing_quality_id` FOREIGN KEY (`manufacturing_quality_id`) REFERENCES `manufacturing_quality` (`manufacturing_quality_id`),
  CONSTRAINT `fk_work_orders_route_id` FOREIGN KEY (`route_id`) REFERENCES `process_routes` (`route_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `process_executions` (
  `process_exec_id` varchar(50) NOT NULL,
  `work_order_id` varchar(50) DEFAULT NULL,
  `process_def_id` varchar(50) DEFAULT NULL,
  `device_id` varchar(50) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `operator` varchar(50) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`process_exec_id`),
  KEY `idx_process_executions_work_order_id` (`work_order_id`),
  KEY `idx_process_executions_process_def_id` (`process_def_id`),
  KEY `idx_process_executions_device_id` (`device_id`),
  CONSTRAINT `fk_process_executions_work_order_id` FOREIGN KEY (`work_order_id`) REFERENCES `work_orders` (`work_order_id`),
  CONSTRAINT `fk_process_executions_process_def_id` FOREIGN KEY (`process_def_id`) REFERENCES `process_definitions` (`process_def_id`),
  CONSTRAINT `fk_process_executions_device_id` FOREIGN KEY (`device_id`) REFERENCES `manufacturing_devices` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `processing_quality_data` (
  `quality_data_id` varchar(50) NOT NULL,
  `process_exec_id` varchar(50) DEFAULT NULL,
  `standard_param_id` varchar(50) DEFAULT NULL,
  `actual_value` varchar(100) DEFAULT NULL,
  `measure_time` datetime DEFAULT NULL,
  `measurer` varchar(50) DEFAULT NULL,
  `is_out_of_tolerance` tinyint DEFAULT NULL,
  PRIMARY KEY (`quality_data_id`),
  KEY `idx_processing_quality_data_process_exec_id` (`process_exec_id`),
  KEY `idx_processing_quality_data_standard_param_id` (`standard_param_id`),
  CONSTRAINT `fk_processing_quality_data_process_exec_id` FOREIGN KEY (`process_exec_id`) REFERENCES `process_executions` (`process_exec_id`),
  CONSTRAINT `fk_processing_quality_data_standard_param_id` FOREIGN KEY (`standard_param_id`) REFERENCES `process_standard_params` (`standard_param_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `process_anomalies` (
  `anomaly_id` varchar(50) NOT NULL,
  `process_exec_id` varchar(50) DEFAULT NULL,
  `quality_data_id` varchar(50) DEFAULT NULL,
  `anomaly_type` varchar(50) DEFAULT NULL,
  `anomaly_level` varchar(20) DEFAULT NULL,
  `anomaly_description` text,
  `detection_time` datetime DEFAULT NULL,
  `detection_result` varchar(100) DEFAULT NULL,
  `handling_status` varchar(20) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`anomaly_id`),
  KEY `idx_process_anomalies_process_exec_id` (`process_exec_id`),
  KEY `idx_process_anomalies_quality_data_id` (`quality_data_id`),
  CONSTRAINT `fk_process_anomalies_process_exec_id` FOREIGN KEY (`process_exec_id`) REFERENCES `process_executions` (`process_exec_id`),
  CONSTRAINT `fk_process_anomalies_quality_data_id` FOREIGN KEY (`quality_data_id`) REFERENCES `processing_quality_data` (`quality_data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `key_process_identifications` (
  `identification_id` varchar(50) NOT NULL,
  `part_instance_id` varchar(50) DEFAULT NULL,
  `process_def_id` varchar(50) DEFAULT NULL,
  `criticality_score` decimal(10,2) DEFAULT NULL,
  `ranking` int DEFAULT NULL,
  `identification_basis` text,
  `identification_time` datetime DEFAULT NULL,
  PRIMARY KEY (`identification_id`),
  KEY `idx_key_process_identifications_part_instance_id` (`part_instance_id`),
  KEY `idx_key_process_identifications_process_def_id` (`process_def_id`),
  CONSTRAINT `fk_key_process_identifications_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `part_instances` (`part_instance_id`),
  CONSTRAINT `fk_key_process_identifications_process_def_id` FOREIGN KEY (`process_def_id`) REFERENCES `process_definitions` (`process_def_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;
