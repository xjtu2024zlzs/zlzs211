-- 模块三：服役性能周期故障预防

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `inspection_results`;
DROP TABLE IF EXISTS `health_baselines`;
DROP TABLE IF EXISTS `inspection_tasks`;
DROP TABLE IF EXISTS `fault_predictions`;
DROP TABLE IF EXISTS `fault_detections`;
DROP TABLE IF EXISTS `fault_records`;
DROP TABLE IF EXISTS `sensors`;

CREATE TABLE `sensors` (
  `sensor_id` varchar(50) NOT NULL,
  `part_instance_id` varchar(50) DEFAULT NULL,
  `service_quality_id` varchar(50) DEFAULT NULL,
  `equipment_id` varchar(50) DEFAULT NULL,
  `component_id` varchar(50) DEFAULT NULL,
  `sensor_name` varchar(100) DEFAULT NULL,
  `sensor_type` varchar(50) DEFAULT NULL,
  `data_type` varchar(50) DEFAULT NULL,
  `installation_position` varchar(100) DEFAULT NULL,
  `unit` varchar(20) DEFAULT NULL,
  `sampling_frequency` varchar(50) DEFAULT NULL,
  `data_file_path` varchar(255) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `calibration_date` date DEFAULT NULL,
  PRIMARY KEY (`sensor_id`),
  KEY `idx_sensors_part_instance_id` (`part_instance_id`),
  KEY `idx_sensors_service_quality_id` (`service_quality_id`),
  KEY `idx_sensors_equipment_id` (`equipment_id`),
  KEY `idx_sensors_component_id` (`component_id`),
  CONSTRAINT `fk_sensors_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `part_instances` (`part_instance_id`),
  CONSTRAINT `fk_sensors_service_quality_id` FOREIGN KEY (`service_quality_id`) REFERENCES `service_quality` (`service_quality_id`),
  CONSTRAINT `fk_sensors_equipment_id` FOREIGN KEY (`equipment_id`) REFERENCES `equipments` (`equipment_id`),
  CONSTRAINT `fk_sensors_component_id` FOREIGN KEY (`component_id`) REFERENCES `components` (`component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `fault_records` (
  `fault_id` varchar(50) NOT NULL,
  `part_instance_id` varchar(50) DEFAULT NULL,
  `service_quality_id` varchar(50) DEFAULT NULL,
  `fault_code` varchar(50) DEFAULT NULL,
  `fault_name` varchar(100) DEFAULT NULL,
  `fault_type` varchar(50) DEFAULT NULL,
  `fault_level` varchar(20) DEFAULT NULL,
  `occurrence_time` datetime DEFAULT NULL,
  `fault_description` text,
  `cause_analysis` text,
  `handling_measures` text,
  `handling_result` text,
  PRIMARY KEY (`fault_id`),
  KEY `idx_fault_records_part_instance_id` (`part_instance_id`),
  KEY `idx_fault_records_service_quality_id` (`service_quality_id`),
  CONSTRAINT `fk_fault_records_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `part_instances` (`part_instance_id`),
  CONSTRAINT `fk_fault_records_service_quality_id` FOREIGN KEY (`service_quality_id`) REFERENCES `service_quality` (`service_quality_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `fault_detections` (
  `detection_id` varchar(50) NOT NULL,
  `part_instance_id` varchar(50) DEFAULT NULL,
  `service_quality_id` varchar(50) DEFAULT NULL,
  `sensor_id` varchar(50) DEFAULT NULL,
  `model_version` varchar(50) DEFAULT NULL,
  `detection_time` datetime DEFAULT NULL,
  `fault_label` varchar(50) DEFAULT NULL,
  `confidence` decimal(10,4) DEFAULT NULL,
  `feature_summary` text,
  `result_description` text,
  `degradation_point` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`detection_id`),
  KEY `idx_fault_detections_part_instance_id` (`part_instance_id`),
  KEY `idx_fault_detections_service_quality_id` (`service_quality_id`),
  KEY `idx_fault_detections_sensor_id` (`sensor_id`),
  CONSTRAINT `fk_fault_detections_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `part_instances` (`part_instance_id`),
  CONSTRAINT `fk_fault_detections_service_quality_id` FOREIGN KEY (`service_quality_id`) REFERENCES `service_quality` (`service_quality_id`),
  CONSTRAINT `fk_fault_detections_sensor_id` FOREIGN KEY (`sensor_id`) REFERENCES `sensors` (`sensor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `fault_predictions` (
  `prediction_id` varchar(50) NOT NULL,
  `part_instance_id` varchar(50) DEFAULT NULL,
  `service_quality_id` varchar(50) DEFAULT NULL,
  `model_version` varchar(50) DEFAULT NULL,
  `prediction_time` datetime DEFAULT NULL,
  `risk_level` varchar(20) DEFAULT NULL,
  `risk_score` decimal(10,4) DEFAULT NULL,
  `remaining_useful_life` decimal(10,2) DEFAULT NULL,
  `time_to_fault` decimal(10,2) DEFAULT NULL,
  `predicted_fault_type` varchar(100) DEFAULT NULL,
  `disposal_suggestion` text,
  PRIMARY KEY (`prediction_id`),
  KEY `idx_fault_predictions_part_instance_id` (`part_instance_id`),
  KEY `idx_fault_predictions_service_quality_id` (`service_quality_id`),
  CONSTRAINT `fk_fault_predictions_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `part_instances` (`part_instance_id`),
  CONSTRAINT `fk_fault_predictions_service_quality_id` FOREIGN KEY (`service_quality_id`) REFERENCES `service_quality` (`service_quality_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `inspection_tasks` (
  `task_id` varchar(50) NOT NULL,
  `part_instance_id` varchar(50) DEFAULT NULL,
  `service_quality_id` varchar(50) DEFAULT NULL,
  `inspection_type` varchar(50) DEFAULT NULL,
  `planned_time` datetime DEFAULT NULL,
  `actual_time` datetime DEFAULT NULL,
  `inspector` varchar(50) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`task_id`),
  KEY `idx_inspection_tasks_part_instance_id` (`part_instance_id`),
  KEY `idx_inspection_tasks_service_quality_id` (`service_quality_id`),
  CONSTRAINT `fk_inspection_tasks_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `part_instances` (`part_instance_id`),
  CONSTRAINT `fk_inspection_tasks_service_quality_id` FOREIGN KEY (`service_quality_id`) REFERENCES `service_quality` (`service_quality_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `health_baselines` (
  `baseline_id` varchar(50) NOT NULL,
  `part_instance_id` varchar(50) DEFAULT NULL,
  `service_quality_id` varchar(50) DEFAULT NULL,
  `baseline_name` varchar(100) DEFAULT NULL,
  `baseline_version` varchar(50) DEFAULT NULL,
  `feature_profile` text,
  `establish_time` datetime DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`baseline_id`),
  KEY `idx_health_baselines_part_instance_id` (`part_instance_id`),
  KEY `idx_health_baselines_service_quality_id` (`service_quality_id`),
  CONSTRAINT `fk_health_baselines_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `part_instances` (`part_instance_id`),
  CONSTRAINT `fk_health_baselines_service_quality_id` FOREIGN KEY (`service_quality_id`) REFERENCES `service_quality` (`service_quality_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `inspection_results` (
  `result_id` varchar(50) NOT NULL,
  `task_id` varchar(50) DEFAULT NULL,
  `part_instance_id` varchar(50) DEFAULT NULL,
  `service_quality_id` varchar(50) DEFAULT NULL,
  `baseline_id` varchar(50) DEFAULT NULL,
  `health_score` decimal(10,4) DEFAULT NULL,
  `rms` decimal(20,6) DEFAULT NULL,
  `standard_deviation` decimal(20,6) DEFAULT NULL,
  `kurtosis` decimal(20,6) DEFAULT NULL,
  `peak_value` decimal(20,6) DEFAULT NULL,
  `comparison_result` text,
  `inspection_conclusion` text,
  `report_path` varchar(255) DEFAULT NULL,
  `inspection_time` datetime DEFAULT NULL,
  PRIMARY KEY (`result_id`),
  KEY `idx_inspection_results_task_id` (`task_id`),
  KEY `idx_inspection_results_part_instance_id` (`part_instance_id`),
  KEY `idx_inspection_results_service_quality_id` (`service_quality_id`),
  KEY `idx_inspection_results_baseline_id` (`baseline_id`),
  CONSTRAINT `fk_inspection_results_task_id` FOREIGN KEY (`task_id`) REFERENCES `inspection_tasks` (`task_id`),
  CONSTRAINT `fk_inspection_results_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `part_instances` (`part_instance_id`),
  CONSTRAINT `fk_inspection_results_service_quality_id` FOREIGN KEY (`service_quality_id`) REFERENCES `service_quality` (`service_quality_id`),
  CONSTRAINT `fk_inspection_results_baseline_id` FOREIGN KEY (`baseline_id`) REFERENCES `health_baselines` (`baseline_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;
