-- 主数据层表

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


DROP TABLE IF EXISTS `part_instances`;
DROP TABLE IF EXISTS `part_templates`;
DROP TABLE IF EXISTS `components`;
DROP TABLE IF EXISTS `equipments`;
DROP TABLE IF EXISTS `subsystems`;
DROP TABLE IF EXISTS `aircraft`;

CREATE TABLE `aircraft` (
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

CREATE TABLE `subsystems` (
  `subsystem_id` varchar(50) NOT NULL,
  `subsystem_name` varchar(100) DEFAULT NULL,
  `aircraft_id` varchar(50) DEFAULT NULL,
  `remarks` text,
  PRIMARY KEY (`subsystem_id`),
  KEY `idx_subsystems_aircraft_id` (`aircraft_id`),
  CONSTRAINT `fk_subsystems_aircraft_id` FOREIGN KEY (`aircraft_id`) REFERENCES `aircraft` (`aircraft_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `equipments` (
  `equipment_id` varchar(50) NOT NULL,
  `equipment_name` varchar(100) DEFAULT NULL,
  `subsystem_id` varchar(50) DEFAULT NULL,
  `remarks` text,
  PRIMARY KEY (`equipment_id`),
  KEY `idx_equipments_subsystem_id` (`subsystem_id`),
  CONSTRAINT `fk_equipments_subsystem_id` FOREIGN KEY (`subsystem_id`) REFERENCES `subsystems` (`subsystem_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `components` (
  `component_id` varchar(50) NOT NULL,
  `component_name` varchar(100) DEFAULT NULL,
  `equipment_id` varchar(50) DEFAULT NULL,
  `specification` varchar(50) DEFAULT NULL,
  `remarks` text,
  PRIMARY KEY (`component_id`),
  KEY `idx_components_equipment_id` (`equipment_id`),
  CONSTRAINT `fk_components_equipment_id` FOREIGN KEY (`equipment_id`) REFERENCES `equipments` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `part_templates` (
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
  CONSTRAINT `fk_part_templates_component_id` FOREIGN KEY (`component_id`) REFERENCES `components` (`component_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `part_instances` (
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
  CONSTRAINT `fk_part_instances_part_template_id` FOREIGN KEY (`part_template_id`) REFERENCES `part_templates` (`part_template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;
