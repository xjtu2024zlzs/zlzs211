-- ============================================================
-- Source: main_data.sql
-- ============================================================
-- 主数据层表

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


DROP TABLE IF EXISTS `t3_part_instances`;
DROP TABLE IF EXISTS `t3_part_templates`;
DROP TABLE IF EXISTS `t3_components`;
DROP TABLE IF EXISTS `t3_equipments`;
DROP TABLE IF EXISTS `t3_subsystems`;
DROP TABLE IF EXISTS `t3_aircraft`;

CREATE TABLE `t3_aircraft` (
  `aircraft_id` varchar(50) NOT NULL,
  `aircraft_name` varchar(100) DEFAULT NULL,
  `aircraft_model` varchar(50) DEFAULT NULL,
  `serial_number` varchar(50) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `remarks` text,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`aircraft_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_subsystems` (
  `subsystem_id` varchar(50) NOT NULL,
  `subsystem_name` varchar(100) DEFAULT NULL,
  `aircraft_id` varchar(50) DEFAULT NULL,
  `remarks` text,
  PRIMARY KEY (`subsystem_id`),
  KEY `idx_subsystems_aircraft_id` (`aircraft_id`),
  CONSTRAINT `fk_subsystems_aircraft_id` FOREIGN KEY (`aircraft_id`) REFERENCES `t3_aircraft` (`aircraft_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_equipments` (
  `equipment_id` varchar(50) NOT NULL,
  `equipment_name` varchar(100) DEFAULT NULL,
  `subsystem_id` varchar(50) DEFAULT NULL,
  `remarks` text,
  PRIMARY KEY (`equipment_id`),
  KEY `idx_equipments_subsystem_id` (`subsystem_id`),
  CONSTRAINT `fk_equipments_subsystem_id` FOREIGN KEY (`subsystem_id`) REFERENCES `t3_subsystems` (`subsystem_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_components` (
  `component_id` varchar(50) NOT NULL,
  `component_name` varchar(100) DEFAULT NULL,
  `equipment_id` varchar(50) DEFAULT NULL,
  `specification` varchar(50) DEFAULT NULL,
  `remarks` text,
  PRIMARY KEY (`component_id`),
  KEY `idx_components_equipment_id` (`equipment_id`),
  CONSTRAINT `fk_components_equipment_id` FOREIGN KEY (`equipment_id`) REFERENCES `t3_equipments` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_part_templates` (
  `part_template_id` varchar(50) NOT NULL,
  `part_number` varchar(50) DEFAULT NULL,
  `part_name` varchar(100) DEFAULT NULL,
  `component_id` varchar(50) DEFAULT NULL,
  `material` varchar(50) DEFAULT NULL,
  `specification` varchar(50) DEFAULT NULL,
  `design_version` varchar(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`part_template_id`),
  KEY `idx_part_templates_component_id` (`component_id`),
  CONSTRAINT `fk_part_templates_component_id` FOREIGN KEY (`component_id`) REFERENCES `t3_components` (`component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_part_instances` (
  `part_instance_id` varchar(50) NOT NULL,
  `part_template_id` varchar(50) DEFAULT NULL,
  `serial_number` varchar(50) DEFAULT NULL,
  `batch_number` varchar(50) DEFAULT NULL,
  `manufacturer` varchar(100) DEFAULT NULL,
  `production_date` date DEFAULT NULL,
  `current_status` varchar(20) DEFAULT NULL,
  `quality_level` varchar(20) DEFAULT NULL,
  `key_degree` varchar(20) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`part_instance_id`),
  KEY `idx_part_instances_part_template_id` (`part_template_id`),
  CONSTRAINT `fk_part_instances_part_template_id` FOREIGN KEY (`part_template_id`) REFERENCES `t3_part_templates` (`part_template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- Source: monitor_quality.sql
-- ============================================================
-- 零件质量信息表

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `t3_service_quality`;
DROP TABLE IF EXISTS `t3_manufacturing_quality`;
DROP TABLE IF EXISTS `t3_design_quality`;

CREATE TABLE `t3_design_quality` (
  `design_quality_id` varchar(50) NOT NULL,
  `part_template_id` varchar(50) DEFAULT NULL,
  `design_version` varchar(20) DEFAULT NULL,
  `drawing_version` varchar(50) DEFAULT NULL,
  `design_requirements` text,
  `functional_requirements` text,
  `tolerance_requirements` text,
  `key_quality_characteristics` text,
  `design_review_result` varchar(100) DEFAULT NULL,
  `verification_result` varchar(100) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`design_quality_id`),
  KEY `idx_design_quality_part_template_id` (`part_template_id`),
  CONSTRAINT `fk_design_quality_part_template_id` FOREIGN KEY (`part_template_id`) REFERENCES `t3_part_templates` (`part_template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_manufacturing_quality` (
  `manufacturing_quality_id` varchar(50) NOT NULL,
  `part_instance_id` varchar(50) DEFAULT NULL,
  `production_order_id` varchar(50) DEFAULT NULL,
  `workshop_id` varchar(50) DEFAULT NULL,
  `production_line_id` varchar(50) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `process_status` varchar(30) DEFAULT NULL,
  `final_inspection_result` varchar(100) DEFAULT NULL,
  `defect_count` int DEFAULT NULL,
  `rework_count` int DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`manufacturing_quality_id`),
  KEY `idx_manufacturing_quality_part_instance_id` (`part_instance_id`),
  CONSTRAINT `fk_manufacturing_quality_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `t3_part_instances` (`part_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_service_quality` (
  `service_quality_id` varchar(50) NOT NULL,
  `part_instance_id` varchar(50) DEFAULT NULL,
  `equipment_id` varchar(50) DEFAULT NULL,
  `component_id` varchar(50) DEFAULT NULL,
  `installed_aircraft_id` varchar(50) DEFAULT NULL,
  `installation_position` varchar(100) DEFAULT NULL,
  `installation_date` date DEFAULT NULL,
  `service_status` varchar(30) DEFAULT NULL,
  `total_running_hours` decimal(10,2) DEFAULT NULL,
  `cycle_count` int DEFAULT NULL,
  `health_score` decimal(10,2) DEFAULT NULL,
  `last_inspection_date` date DEFAULT NULL,
  `next_inspection_date` date DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`service_quality_id`),
  KEY `idx_service_quality_part_instance_id` (`part_instance_id`),
  KEY `idx_service_quality_equipment_id` (`equipment_id`),
  KEY `idx_service_quality_component_id` (`component_id`),
  KEY `idx_service_quality_installed_aircraft_id` (`installed_aircraft_id`),
  CONSTRAINT `fk_service_quality_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `t3_part_instances` (`part_instance_id`),
  CONSTRAINT `fk_service_quality_equipment_id` FOREIGN KEY (`equipment_id`) REFERENCES `t3_equipments` (`equipment_id`),
  CONSTRAINT `fk_service_quality_component_id` FOREIGN KEY (`component_id`) REFERENCES `t3_components` (`component_id`),
  CONSTRAINT `fk_service_quality_installed_aircraft_id` FOREIGN KEY (`installed_aircraft_id`) REFERENCES `t3_aircraft` (`aircraft_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- Source: feedback_warning.sql
-- ============================================================
-- 模块二：全域制造过程反馈监管

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `t3_key_process_identifications`;
DROP TABLE IF EXISTS `t3_process_anomalies`;
DROP TABLE IF EXISTS `t3_processing_quality_data`;
DROP TABLE IF EXISTS `t3_process_executions`;
DROP TABLE IF EXISTS `t3_work_orders`;
DROP TABLE IF EXISTS `t3_process_standard_params`;
DROP TABLE IF EXISTS `t3_process_definitions`;
DROP TABLE IF EXISTS `t3_process_routes`;
DROP TABLE IF EXISTS `t3_manufacturing_devices`;

CREATE TABLE `t3_manufacturing_devices` (
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

CREATE TABLE `t3_process_routes` (
  `route_id` varchar(50) NOT NULL,
  `part_template_id` varchar(50) DEFAULT NULL,
  `route_name` varchar(100) DEFAULT NULL,
  `version` varchar(20) DEFAULT NULL,
  `effective_date` date DEFAULT NULL,
  `is_active` tinyint DEFAULT NULL,
  PRIMARY KEY (`route_id`),
  KEY `idx_process_routes_part_template_id` (`part_template_id`),
  CONSTRAINT `fk_process_routes_part_template_id` FOREIGN KEY (`part_template_id`) REFERENCES `t3_part_templates` (`part_template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_process_definitions` (
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
  CONSTRAINT `fk_process_definitions_route_id` FOREIGN KEY (`route_id`) REFERENCES `t3_process_routes` (`route_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_process_standard_params` (
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
  CONSTRAINT `fk_process_standard_params_process_def_id` FOREIGN KEY (`process_def_id`) REFERENCES `t3_process_definitions` (`process_def_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_work_orders` (
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
  CONSTRAINT `fk_work_orders_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `t3_part_instances` (`part_instance_id`),
  CONSTRAINT `fk_work_orders_manufacturing_quality_id` FOREIGN KEY (`manufacturing_quality_id`) REFERENCES `t3_manufacturing_quality` (`manufacturing_quality_id`),
  CONSTRAINT `fk_work_orders_route_id` FOREIGN KEY (`route_id`) REFERENCES `t3_process_routes` (`route_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_process_executions` (
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
  CONSTRAINT `fk_process_executions_work_order_id` FOREIGN KEY (`work_order_id`) REFERENCES `t3_work_orders` (`work_order_id`),
  CONSTRAINT `fk_process_executions_process_def_id` FOREIGN KEY (`process_def_id`) REFERENCES `t3_process_definitions` (`process_def_id`),
  CONSTRAINT `fk_process_executions_device_id` FOREIGN KEY (`device_id`) REFERENCES `t3_manufacturing_devices` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_processing_quality_data` (
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
  CONSTRAINT `fk_processing_quality_data_process_exec_id` FOREIGN KEY (`process_exec_id`) REFERENCES `t3_process_executions` (`process_exec_id`),
  CONSTRAINT `fk_processing_quality_data_standard_param_id` FOREIGN KEY (`standard_param_id`) REFERENCES `t3_process_standard_params` (`standard_param_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_process_anomalies` (
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
  CONSTRAINT `fk_process_anomalies_process_exec_id` FOREIGN KEY (`process_exec_id`) REFERENCES `t3_process_executions` (`process_exec_id`),
  CONSTRAINT `fk_process_anomalies_quality_data_id` FOREIGN KEY (`quality_data_id`) REFERENCES `t3_processing_quality_data` (`quality_data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_key_process_identifications` (
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
  CONSTRAINT `fk_key_process_identifications_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `t3_part_instances` (`part_instance_id`),
  CONSTRAINT `fk_key_process_identifications_process_def_id` FOREIGN KEY (`process_def_id`) REFERENCES `t3_process_definitions` (`process_def_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- Source: service_prevention.sql
-- ============================================================
-- 模块三：服役性能周期故障预防

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `t3_inspection_results`;
DROP TABLE IF EXISTS `t3_health_baselines`;
DROP TABLE IF EXISTS `t3_inspection_tasks`;
DROP TABLE IF EXISTS `t3_fault_predictions`;
DROP TABLE IF EXISTS `t3_fault_detections`;
DROP TABLE IF EXISTS `t3_fault_records`;
DROP TABLE IF EXISTS `t3_sensors`;

CREATE TABLE `t3_sensors` (
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
  CONSTRAINT `fk_sensors_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `t3_part_instances` (`part_instance_id`),
  CONSTRAINT `fk_sensors_service_quality_id` FOREIGN KEY (`service_quality_id`) REFERENCES `t3_service_quality` (`service_quality_id`),
  CONSTRAINT `fk_sensors_equipment_id` FOREIGN KEY (`equipment_id`) REFERENCES `t3_equipments` (`equipment_id`),
  CONSTRAINT `fk_sensors_component_id` FOREIGN KEY (`component_id`) REFERENCES `t3_components` (`component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_fault_records` (
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
  CONSTRAINT `fk_fault_records_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `t3_part_instances` (`part_instance_id`),
  CONSTRAINT `fk_fault_records_service_quality_id` FOREIGN KEY (`service_quality_id`) REFERENCES `t3_service_quality` (`service_quality_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_fault_detections` (
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
  CONSTRAINT `fk_fault_detections_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `t3_part_instances` (`part_instance_id`),
  CONSTRAINT `fk_fault_detections_service_quality_id` FOREIGN KEY (`service_quality_id`) REFERENCES `t3_service_quality` (`service_quality_id`),
  CONSTRAINT `fk_fault_detections_sensor_id` FOREIGN KEY (`sensor_id`) REFERENCES `t3_sensors` (`sensor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_fault_predictions` (
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
  CONSTRAINT `fk_fault_predictions_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `t3_part_instances` (`part_instance_id`),
  CONSTRAINT `fk_fault_predictions_service_quality_id` FOREIGN KEY (`service_quality_id`) REFERENCES `t3_service_quality` (`service_quality_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_inspection_tasks` (
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
  CONSTRAINT `fk_inspection_tasks_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `t3_part_instances` (`part_instance_id`),
  CONSTRAINT `fk_inspection_tasks_service_quality_id` FOREIGN KEY (`service_quality_id`) REFERENCES `t3_service_quality` (`service_quality_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_health_baselines` (
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
  CONSTRAINT `fk_health_baselines_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `t3_part_instances` (`part_instance_id`),
  CONSTRAINT `fk_health_baselines_service_quality_id` FOREIGN KEY (`service_quality_id`) REFERENCES `t3_service_quality` (`service_quality_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `t3_inspection_results` (
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
  CONSTRAINT `fk_inspection_results_task_id` FOREIGN KEY (`task_id`) REFERENCES `t3_inspection_tasks` (`task_id`),
  CONSTRAINT `fk_inspection_results_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `t3_part_instances` (`part_instance_id`),
  CONSTRAINT `fk_inspection_results_service_quality_id` FOREIGN KEY (`service_quality_id`) REFERENCES `t3_service_quality` (`service_quality_id`),
  CONSTRAINT `fk_inspection_results_baseline_id` FOREIGN KEY (`baseline_id`) REFERENCES `t3_health_baselines` (`baseline_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- Source: algorithm_task_results.sql
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 算法结果表
CREATE TABLE IF NOT EXISTS `t3_algorithm_task_results` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` varchar(64) NOT NULL COMMENT '算法任务ID',
  `task_type` varchar(64) NOT NULL COMMENT '算法任务类型，如 FEATURE_ANALYSIS、DEGRADATION_DETECT、FAULT_PREDICT',
  `task_name` varchar(128) DEFAULT NULL COMMENT '算法任务名称',
  `status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态：PENDING/RUNNING/SUCCESS/FAILED',

  `request_id` varchar(64) DEFAULT NULL COMMENT '请求流水号',
  `algorithm_version` varchar(32) DEFAULT NULL COMMENT '算法版本',

  `aircraft_id` varchar(64) DEFAULT NULL COMMENT '飞机ID',
  `subsystem_id` varchar(64) DEFAULT NULL COMMENT '分系统ID',
  `equipment_id` varchar(64) DEFAULT NULL COMMENT '设备ID',
  `component_id` varchar(64) DEFAULT NULL COMMENT '组件ID',

  `business_object_id` varchar(64) DEFAULT NULL COMMENT '本次选择的业务对象ID',
  `business_object_level` varchar(32) DEFAULT NULL COMMENT '业务对象层级：aircraft/subsystem/equipment/component',
  `business_object_name` varchar(128) DEFAULT NULL COMMENT '业务对象名称',

  `feature_analysis_task_id` varchar(64) DEFAULT NULL COMMENT '上游特征分析任务ID，仅退化点检测使用',

  `summary_result` varchar(512) DEFAULT NULL COMMENT '摘要结果',
  `result_value` varchar(128) DEFAULT NULL COMMENT '主要结果值',
  `result_unit` varchar(64) DEFAULT NULL COMMENT '主要结果单位',

  `request_json` longtext COMMENT '算法请求JSON',
  `result_json` longtext COMMENT '算法返回结果JSON',
  `error_message` text COMMENT '失败原因',

  `started_at` datetime DEFAULT NULL COMMENT '开始执行时间',
  `finished_at` datetime DEFAULT NULL COMMENT '完成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_algorithm_task_results_task_id` (`task_id`),
  KEY `idx_algorithm_task_results_task_type` (`task_type`),
  KEY `idx_algorithm_task_results_status` (`status`),
  KEY `idx_algorithm_task_results_aircraft_id` (`aircraft_id`),
  KEY `idx_algorithm_task_results_subsystem_id` (`subsystem_id`),
  KEY `idx_algorithm_task_results_equipment_id` (`equipment_id`),
  KEY `idx_algorithm_task_results_component_id` (`component_id`),
  KEY `idx_algorithm_task_results_business_object` (`business_object_id`, `business_object_level`),
  KEY `idx_algorithm_task_results_business_object_id` (`business_object_id`),
  KEY `idx_algorithm_task_results_feature_task_id` (`feature_analysis_task_id`),
  KEY `idx_algorithm_task_results_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通用算法任务结果表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- Source: fault_iden_dataset.sql
-- ============================================================
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `t3_fault_iden_sample` (
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

CREATE TABLE IF NOT EXISTS `t3_fault_iden_file_package` (
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

-- ============================================================
-- Source: part_instance_image_migration.sql
-- ============================================================
SET NAMES utf8mb4;

ALTER TABLE `t3_part_instances`
  ADD COLUMN `image_url` varchar(255) DEFAULT NULL COMMENT '零件图片地址' AFTER `key_degree`;