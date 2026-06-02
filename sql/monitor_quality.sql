-- 零件质量信息表

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `service_quality`;
DROP TABLE IF EXISTS `manufacturing_quality`;
DROP TABLE IF EXISTS `design_quality`;

CREATE TABLE `design_quality` (
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
  CONSTRAINT `fk_design_quality_part_template_id` FOREIGN KEY (`part_template_id`) REFERENCES `part_templates` (`part_template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `manufacturing_quality` (
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
  CONSTRAINT `fk_manufacturing_quality_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `part_instances` (`part_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `service_quality` (
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
  CONSTRAINT `fk_service_quality_part_instance_id` FOREIGN KEY (`part_instance_id`) REFERENCES `part_instances` (`part_instance_id`),
  CONSTRAINT `fk_service_quality_equipment_id` FOREIGN KEY (`equipment_id`) REFERENCES `equipments` (`equipment_id`),
  CONSTRAINT `fk_service_quality_component_id` FOREIGN KEY (`component_id`) REFERENCES `components` (`component_id`),
  CONSTRAINT `fk_service_quality_installed_aircraft_id` FOREIGN KEY (`installed_aircraft_id`) REFERENCES `aircraft` (`aircraft_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;
