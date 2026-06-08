-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: ry-cloud
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `ry-cloud`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `ry-cloud` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `ry-cloud`;

--
-- Table structure for table `t1_ac_model`
--

DROP TABLE IF EXISTS `t1_ac_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_ac_model` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `model_code` varchar(64) NOT NULL,
  `name` varchar(200) NOT NULL,
  `description` text,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `model_code` (`model_code`),
  KEY `idx_ac_model_code` (`model_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机型 / 型别主数据';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_actor_organization`
--

DROP TABLE IF EXISTS `t1_actor_organization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_actor_organization` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `org_code` varchar(100) NOT NULL,
  `org_name` varchar(500) NOT NULL,
  `parent_org_id` char(36) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `org_code` (`org_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_actor_person`
--

DROP TABLE IF EXISTS `t1_actor_person`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_actor_person` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `employee_id` varchar(100) NOT NULL,
  `full_name` varchar(200) NOT NULL,
  `org_id` char(36) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `employee_id` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_ad_sb_compliance`
--

DROP TABLE IF EXISTS `t1_ad_sb_compliance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_ad_sb_compliance` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `aircraft_id` char(36) NOT NULL,
  `node_id` char(36) DEFAULT NULL,
  `directive_type` varchar(10) NOT NULL,
  `directive_number` varchar(100) NOT NULL,
  `directive_title` varchar(500) DEFAULT NULL,
  `issuing_authority` varchar(50) DEFAULT NULL,
  `issue_date` date DEFAULT NULL,
  `compliance_type` varchar(20) DEFAULT NULL,
  `initial_compliance_fh` decimal(12,2) DEFAULT NULL,
  `initial_compliance_date` date DEFAULT NULL,
  `repeat_interval_fh` decimal(12,2) DEFAULT NULL,
  `repeat_interval_days` int DEFAULT NULL,
  `compliance_status` varchar(20) NOT NULL DEFAULT 'OPEN',
  `last_compliance_date` date DEFAULT NULL,
  `last_compliance_fh` decimal(12,2) DEFAULT NULL,
  `last_compliance_fc` int DEFAULT NULL,
  `next_due_date` date DEFAULT NULL,
  `next_due_fh` decimal(12,2) DEFAULT NULL,
  `compliance_wo_id` char(36) DEFAULT NULL,
  `compliance_remark` text,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_ad_sb_compliance_aircraft` (`aircraft_id`),
  KEY `idx_ad_sb_compliance_status` (`compliance_status`,`next_due_fh`),
  CONSTRAINT `ad_sb_compliance_chk_1` CHECK ((`directive_type` in (_utf8mb4'AD',_utf8mb4'SB',_utf8mb4'ASB'))),
  CONSTRAINT `ad_sb_compliance_chk_2` CHECK (((`compliance_type` is null) or (`compliance_type` in (_utf8mb4'ONE_TIME',_utf8mb4'REPETITIVE')))),
  CONSTRAINT `ad_sb_compliance_chk_3` CHECK ((`compliance_status` in (_utf8mb4'OPEN',_utf8mb4'COMPLIED',_utf8mb4'NOT_APPLICABLE',_utf8mb4'DEFERRED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AD/SB/ASB 符合性';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_aircraft_bom_node`
--

DROP TABLE IF EXISTS `t1_aircraft_bom_node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_aircraft_bom_node` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `parent_id` char(36) DEFAULT NULL,
  `aircraft_id` char(36) NOT NULL,
  `node_level` smallint NOT NULL,
  `node_type` varchar(20) NOT NULL,
  `part_number` varchar(200) NOT NULL DEFAULT '',
  `part_name` varchar(500) DEFAULT NULL,
  `part_name_en` varchar(500) DEFAULT NULL,
  `serial_number` varchar(200) DEFAULT NULL,
  `batch_number` varchar(200) DEFAULT NULL,
  `manufacturer` varchar(200) DEFAULT NULL,
  `cage_code` varchar(20) DEFAULT NULL,
  `quantity` decimal(10,3) NOT NULL DEFAULT '1.000',
  `unit` varchar(20) NOT NULL DEFAULT 'EA',
  `position_code` varchar(50) DEFAULT NULL,
  `position_desc` varchar(200) DEFAULT NULL,
  `ata_chapter` varchar(20) DEFAULT NULL,
  `install_date` date DEFAULT NULL,
  `install_tsn` decimal(10,2) DEFAULT NULL,
  `install_csn` int DEFAULT NULL,
  `removal_date` date DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `is_life_limited` tinyint(1) NOT NULL DEFAULT '0',
  `is_serialized` tinyint(1) NOT NULL DEFAULT '0',
  `is_rotable` tinyint(1) NOT NULL DEFAULT '0',
  `is_expendable` tinyint(1) NOT NULL DEFAULT '0',
  `tsn_fh` decimal(12,2) DEFAULT '0.00',
  `tsn_fc` int DEFAULT '0',
  `part_instance_id` char(36) DEFAULT NULL,
  `install_shop_task_id` char(36) DEFAULT NULL,
  `tsn_fh_at_install` decimal(12,2) DEFAULT NULL,
  `tsn_fc_at_install` int DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `created_by` varchar(100) DEFAULT NULL,
  `updated_by` varchar(100) DEFAULT NULL,
  `remark` text,
  `install_ir_id` char(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_aircraft_bom_one_root` (((case when (`parent_id` is null) then `aircraft_id` else NULL end))),
  KEY `idx_aircraft_bom_node_aircraft` (`aircraft_id`),
  KEY `idx_aircraft_bom_node_parent` (`parent_id`),
  KEY `idx_aircraft_bom_node_type` (`aircraft_id`,`node_type`),
  KEY `idx_aircraft_bom_node_part_instance` (`part_instance_id`),
  KEY `idx_aircraft_bom_node_shop_task` (`install_shop_task_id`),
  KEY `idx_aircraft_bom_node_install_ir` (`install_ir_id`),
  KEY `idx_aircraft_bom_parent_lazy` (`aircraft_id`,`parent_id`,`is_active`,`node_level`),
  KEY `idx_aircraft_bom_level_type` (`aircraft_id`,`node_level`,`node_type`,`is_active`),
  KEY `idx_aircraft_bom_search_key` (`aircraft_id`,`part_number`,`serial_number`,`position_code`),
  KEY `idx_aircraft_bom_part_instance` (`part_instance_id`),
  CONSTRAINT `aircraft_bom_node_chk_1` CHECK ((`node_level` >= 1)),
  CONSTRAINT `aircraft_bom_node_chk_2` CHECK ((upper(`node_type`) in (_utf8mb4'AIRCRAFT',_utf8mb4'SYSTEM',_utf8mb4'SUBSYSTEM',_utf8mb4'EQUIPMENT',_utf8mb4'COMPONENT',_utf8mb4'PART',_utf8mb4'CONSUMABLE'))),
  CONSTRAINT `chk_aircraft_bom_root` CHECK ((((`parent_id` is null) and (`node_type` = _utf8mb4'AIRCRAFT') and (`node_level` = 1)) or ((`parent_id` is not null) and (`node_type` <> _utf8mb4'AIRCRAFT'))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='单机装机邻接表：含 part_instance_id、install_shop_task_id、install_ir_id、tsn_*_at_install 等';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_aircraft_delivery`
--

DROP TABLE IF EXISTS `t1_aircraft_delivery`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_aircraft_delivery` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `aircraft_id` char(36) NOT NULL,
  `delivery_date` date NOT NULL,
  `delivery_type` varchar(20) DEFAULT NULL,
  `from_party` varchar(200) DEFAULT NULL,
  `to_party` varchar(200) DEFAULT NULL,
  `delivery_location` varchar(100) DEFAULT NULL,
  `delivery_tsn_fh` decimal(12,2) DEFAULT '0.00',
  `delivery_tsn_fc` int DEFAULT '0',
  `delivery_tso_fh` decimal(12,2) DEFAULT '0.00',
  `airworthiness_cert_number` varchar(200) DEFAULT NULL,
  `airworthiness_cert_expiry` date DEFAULT NULL,
  `noise_cert_number` varchar(200) DEFAULT NULL,
  `config_snapshot_id` char(36) DEFAULT NULL,
  `acceptance_signed_by` varchar(200) DEFAULT NULL,
  `delivery_remark` text,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_aircraft_delivery_aircraft` (`aircraft_id`),
  KEY `idx_aircraft_delivery_snapshot` (`config_snapshot_id`),
  CONSTRAINT `aircraft_delivery_chk_1` CHECK ((`delivery_type` in (_utf8mb4'INITIAL',_utf8mb4'REDELIVERY')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='飞机交付档案；可指向交付时 t1_config_snapshot';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_aircraft_object_profile`
--

DROP TABLE IF EXISTS `t1_aircraft_object_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_aircraft_object_profile` (
  `aircraft_id` char(36) NOT NULL COMMENT '飞机ID，对应t1_physical_aircraft.id',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '统一对象ID',
  `aircraft_no` varchar(50) NOT NULL COMMENT '机号/运营编号',
  `registration_no` varchar(50) DEFAULT NULL COMMENT '注册号',
  `msn` varchar(100) NOT NULL COMMENT '制造序列号',
  `line_number` varchar(100) DEFAULT NULL COMMENT '生产线序号',
  `aircraft_model_id` char(36) DEFAULT NULL COMMENT '机型ID',
  `aircraft_model_code` varchar(64) DEFAULT NULL COMMENT '机型编码',
  `aircraft_model_name` varchar(200) DEFAULT NULL COMMENT '机型名称',
  `aircraft_variant` varchar(100) DEFAULT NULL COMMENT '构型改型/批次',
  `type_certificate_no` varchar(200) DEFAULT NULL COMMENT '型号合格证编号',
  `airworthiness_certificate_no` varchar(200) DEFAULT NULL COMMENT '单机适航证编号',
  `manufacturer` varchar(200) DEFAULT NULL COMMENT '制造商',
  `final_assembly_site` varchar(200) DEFAULT NULL COMMENT '总装地点',
  `production_batch` varchar(100) DEFAULT NULL COMMENT '生产批次',
  `rollout_date` date DEFAULT NULL COMMENT '下线日期',
  `first_flight_date` date DEFAULT NULL COMMENT '首飞日期',
  `delivery_date` date DEFAULT NULL COMMENT '交付日期',
  `acceptance_date` date DEFAULT NULL COMMENT '接收/验收日期',
  `owner_org` varchar(200) DEFAULT NULL COMMENT '所有人/资产归属单位',
  `operator_org` varchar(200) DEFAULT NULL COMMENT '当前运营方/使用单位',
  `base_airport` varchar(50) DEFAULT NULL COMMENT '基地机场',
  `fleet_code` varchar(100) DEFAULT NULL COMMENT '机队编号',
  `aircraft_lifecycle_status` varchar(50) DEFAULT NULL COMMENT '飞机生命周期状态',
  `operational_status` varchar(50) DEFAULT NULL COMMENT '运行状态',
  `airworthiness_status` varchar(50) DEFAULT NULL COMMENT '适航状态',
  `delivery_status` varchar(50) DEFAULT NULL COMMENT '交付状态',
  `current_configuration_baseline` varchar(200) DEFAULT NULL COMMENT '当前构型基线',
  `current_bom_version` varchar(100) DEFAULT NULL COMMENT '当前BOM版本',
  `major_system_count` int NOT NULL DEFAULT '0' COMMENT '一级系统数量',
  `installed_equipment_count` int NOT NULL DEFAULT '0' COMMENT '已装机设备数量',
  `open_engineering_change_count` int NOT NULL DEFAULT '0' COMMENT '未关闭工程更改数量',
  `total_flight_hours` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '累计飞行小时',
  `total_flight_cycles` int NOT NULL DEFAULT '0' COMMENT '累计飞行循环',
  `total_landings` int DEFAULT NULL COMMENT '累计起落次数',
  `latest_maintenance_date` date DEFAULT NULL COMMENT '最近维修日期',
  `next_due_maintenance` varchar(500) DEFAULT NULL COMMENT '下一次到期维修摘要',
  `open_fault_count` int NOT NULL DEFAULT '0' COMMENT '未关闭故障数',
  `current_dossier_version_id` char(36) DEFAULT NULL COMMENT '当前卷宗版本ID',
  `data_snapshot_id` char(36) DEFAULT NULL COMMENT '数据快照ID',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`aircraft_id`),
  UNIQUE KEY `uk_aircraft_profile_no` (`aircraft_no`),
  KEY `idx_aircraft_profile_model` (`aircraft_model_id`,`aircraft_model_code`),
  KEY `idx_aircraft_profile_status` (`operational_status`,`airworthiness_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='整机层基础信息扩展表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_analysis_result`
--

DROP TABLE IF EXISTS `t1_analysis_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_analysis_result` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '分析结果ID',
  `task_id` char(36) NOT NULL COMMENT '分析任务ID',
  `result_type` varchar(50) NOT NULL COMMENT '结果类型',
  `result_title` varchar(300) DEFAULT NULL COMMENT '结果标题',
  `result_summary` text COMMENT '结果摘要',
  `result_value_json` json NOT NULL DEFAULT (json_object()) COMMENT '结果数据',
  `confidence` decimal(5,4) DEFAULT NULL COMMENT '可信度',
  `rank_no` int DEFAULT NULL COMMENT '排序号',
  `related_object_type` varchar(50) DEFAULT NULL COMMENT '关联对象类型',
  `related_object_id` char(36) DEFAULT NULL COMMENT '关联对象ID',
  `evidence_json` json NOT NULL DEFAULT (json_array()) COMMENT '证据',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_analysis_result_task` (`task_id`),
  KEY `idx_analysis_result_type` (`result_type`),
  KEY `idx_analysis_result_object` (`related_object_type`,`related_object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据分析结果汇总';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_analysis_task`
--

DROP TABLE IF EXISTS `t1_analysis_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_analysis_task` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '分析任务ID',
  `task_code` varchar(100) NOT NULL COMMENT '任务编码',
  `task_name` varchar(300) NOT NULL COMMENT '任务名称',
  `analysis_type` varchar(50) NOT NULL COMMENT '分析类型：text_mining/correlation/statistics/cross_domain',
  `scope_type` varchar(50) DEFAULT NULL COMMENT '分析范围类型',
  `scope_json` json NOT NULL DEFAULT (json_object()) COMMENT '分析范围',
  `task_status` varchar(24) NOT NULL DEFAULT 'queued' COMMENT '任务状态：queued/running/succeeded/failed/cancelled',
  `algorithm_code` varchar(64) DEFAULT NULL COMMENT '算法编码',
  `algorithm_version` varchar(32) DEFAULT NULL COMMENT '算法版本',
  `params_json` json NOT NULL DEFAULT (json_object()) COMMENT '分析参数',
  `started_at` datetime(6) DEFAULT NULL COMMENT '开始时间',
  `finished_at` datetime(6) DEFAULT NULL COMMENT '完成时间',
  `error_message` text COMMENT '错误信息',
  `created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_analysis_task_code` (`task_code`),
  KEY `idx_analysis_task_type` (`analysis_type`),
  KEY `idx_analysis_task_status` (`task_status`),
  CONSTRAINT `chk_analysis_task_status` CHECK ((`task_status` in (_utf8mb4'queued',_utf8mb4'running',_utf8mb4'succeeded',_utf8mb4'failed',_utf8mb4'cancelled')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据分析任务：记录文本挖掘、关联分析、统计分析和跨域分析任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_analytics_scope`
--

DROP TABLE IF EXISTS `t1_analytics_scope`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_analytics_scope` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `label` text,
  `dossier_instance_ids` json NOT NULL DEFAULT (json_array()),
  `slice_params_json` json NOT NULL DEFAULT (json_object()),
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外部分析范围登记：数据来源仅为所列卷宗实例，可供外部分析软件读取';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_assembly_record`
--

DROP TABLE IF EXISTS `t1_assembly_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_assembly_record` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `node_id` char(36) NOT NULL,
  `instance_id` char(36) NOT NULL,
  `aircraft_id` char(36) DEFAULT NULL COMMENT '对象锚点：飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT '对象锚点：BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '对象锚点：实物实例ID',
  `object_level` varchar(30) DEFAULT NULL COMMENT '对象锚点：层级',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '对象锚点：统一对象ID',
  `shop_order_task_id` char(36) DEFAULT NULL,
  `assembly_time` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `assembled_by_id` varchar(50) DEFAULT NULL,
  `verified_by_id` varchar(50) DEFAULT NULL,
  `aircraft_tsn_fh` decimal(12,2) DEFAULT NULL,
  `aircraft_tsn_fc` int DEFAULT NULL,
  `assembly_params` json NOT NULL DEFAULT (json_object()),
  `software_pn` varchar(200) DEFAULT NULL,
  `software_version` varchar(100) DEFAULT NULL,
  `remarks` text,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_assembly_record_node` (`node_id`),
  KEY `idx_assembly_record_instance` (`instance_id`),
  KEY `idx_assembly_record_shop_task` (`shop_order_task_id`),
  KEY `idx_ar_bom_anchor` (`aircraft_id`,`bom_node_id`),
  KEY `idx_ar_part_anchor` (`part_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='装配报工记录（可关联 t1_shop_order_task）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_audit_log`
--

DROP TABLE IF EXISTS `t1_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_audit_log` (
  `log_id` char(36) NOT NULL DEFAULT (uuid()),
  `table_name` varchar(200) NOT NULL,
  `record_id` char(36) NOT NULL,
  `operation` varchar(20) DEFAULT NULL,
  `old_data` json DEFAULT NULL,
  `new_data` json DEFAULT NULL,
  `operator_id` varchar(100) DEFAULT NULL,
  `operated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`log_id`),
  KEY `idx_audit_log_table` (`table_name`,`record_id`),
  CONSTRAINT `audit_log_chk_1` CHECK ((`operation` in (_utf8mb4'INSERT',_utf8mb4'UPDATE',_utf8mb4'DELETE')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_certificate_record`
--

DROP TABLE IF EXISTS `t1_certificate_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_certificate_record` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '证书/证明记录ID',
  `certificate_no` varchar(200) NOT NULL COMMENT '证书编号',
  `certificate_type` varchar(100) NOT NULL COMMENT '证书类型',
  `object_level` varchar(30) DEFAULT NULL COMMENT '对象层级',
  `object_id` char(36) DEFAULT NULL COMMENT '对象ID',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '实物实例ID',
  `issuing_authority` varchar(200) DEFAULT NULL COMMENT '签发机构',
  `issue_date` date DEFAULT NULL COMMENT '签发日期',
  `expiry_date` date DEFAULT NULL COMMENT '有效期',
  `certificate_status` varchar(50) DEFAULT NULL COMMENT '证书状态',
  `document_entry_id` char(36) DEFAULT NULL COMMENT '卷宗文档条目ID',
  `file_storage_key` text COMMENT '文件存储地址',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_certificate_no_type` (`certificate_no`,`certificate_type`),
  KEY `idx_certificate_object` (`object_level`,`object_id`),
  KEY `idx_certificate_bom` (`bom_node_id`),
  KEY `idx_certificate_part` (`part_instance_id`),
  KEY `idx_certificate_status` (`certificate_type`,`certificate_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='证书、证明和适航文件记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_component_object_instance`
--

DROP TABLE IF EXISTS `t1_component_object_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_component_object_instance` (
  `component_instance_id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '组件实物实例ID',
  `component_master_id` char(36) DEFAULT NULL COMMENT '组件主数据ID',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '统一对象ID',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '所属飞机ID',
  `parent_equipment_instance_id` char(36) DEFAULT NULL COMMENT '所属设备实例ID',
  `parent_component_instance_id` char(36) DEFAULT NULL COMMENT '所属上级组件实例ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '关联实物件ID',
  `component_code` varchar(100) NOT NULL COMMENT '组件编码',
  `component_name` varchar(300) DEFAULT NULL COMMENT '组件名称',
  `assembly_part_number` varchar(200) DEFAULT NULL COMMENT '装配件号',
  `assembly_serial_number` varchar(200) DEFAULT NULL COMMENT '组件序列号',
  `assembly_batch_number` varchar(200) DEFAULT NULL COMMENT '组件批次号',
  `assembly_work_order_no` varchar(100) DEFAULT NULL COMMENT '装配工单号',
  `assembly_date` date DEFAULT NULL COMMENT '装配日期',
  `assembly_version` varchar(100) DEFAULT NULL COMMENT '装配构型版本',
  `part_count` int NOT NULL DEFAULT '0' COMMENT '下级零件数量',
  `key_part_count` int NOT NULL DEFAULT '0' COMMENT '关键零件数量',
  `installation_position` varchar(200) DEFAULT NULL COMMENT '装机位置',
  `position_code` varchar(100) DEFAULT NULL COMMENT '位号',
  `installation_date` date DEFAULT NULL COMMENT '装机日期',
  `assembly_status` varchar(50) DEFAULT NULL COMMENT '装配状态',
  `quality_status` varchar(50) DEFAULT NULL COMMENT '质量状态',
  `operational_status` varchar(50) DEFAULT NULL COMMENT '使用状态',
  `tsn` decimal(12,2) DEFAULT NULL COMMENT '自新品以来时间',
  `csn` int DEFAULT NULL COMMENT '自新品以来循环',
  `trace_code` varchar(200) DEFAULT NULL COMMENT '追溯码',
  `material_trace_summary` text COMMENT '材料/批次追溯摘要',
  `assembly_record_doc_id` char(36) DEFAULT NULL COMMENT '装配记录文件ID',
  `release_certificate_doc_id` char(36) DEFAULT NULL COMMENT '放行证明文件ID',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`component_instance_id`),
  UNIQUE KEY `uk_component_instance_bom_node` (`bom_node_id`),
  KEY `idx_component_instance_master` (`component_master_id`),
  KEY `idx_component_instance_aircraft` (`aircraft_id`),
  KEY `idx_component_instance_part` (`assembly_part_number`,`assembly_serial_number`),
  KEY `idx_component_instance_status` (`assembly_status`,`quality_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组件层实物实例表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_component_object_master`
--

DROP TABLE IF EXISTS `t1_component_object_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_component_object_master` (
  `component_master_id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '组件主数据ID',
  `component_code` varchar(100) NOT NULL COMMENT '组件编码',
  `component_name` varchar(300) NOT NULL COMMENT '组件名称',
  `component_type` varchar(100) DEFAULT NULL COMMENT '组件类型',
  `assembly_part_number` varchar(200) DEFAULT NULL COMMENT '装配件号',
  `drawing_no` varchar(200) DEFAULT NULL COMMENT '装配图号',
  `drawing_revision` varchar(100) DEFAULT NULL COMMENT '图纸版本',
  `manufacturer` varchar(200) DEFAULT NULL COMMENT '制造商/装配单位',
  `supplier_name` varchar(200) DEFAULT NULL COMMENT '供应商',
  `repairable_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否可修',
  `replaceable_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否可更换',
  `life_limit` varchar(200) DEFAULT NULL COMMENT '寿命限制摘要',
  `criticality_level` varchar(50) DEFAULT NULL COMMENT '关键等级',
  `key_quality_characteristics` text COMMENT '关键质量特性摘要',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`component_master_id`),
  UNIQUE KEY `uk_component_master_code` (`component_code`),
  KEY `idx_component_master_part` (`assembly_part_number`),
  KEY `idx_component_master_type` (`component_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组件层主数据表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_concession_record`
--

DROP TABLE IF EXISTS `t1_concession_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_concession_record` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '让步/偏离记录ID',
  `concession_number` varchar(100) NOT NULL COMMENT '让步/偏离编号',
  `concession_type` varchar(50) DEFAULT NULL COMMENT '类型：deviation/concession/waiver',
  `nc_record_id` char(36) DEFAULT NULL COMMENT '关联不合格记录ID',
  `object_level` varchar(30) DEFAULT NULL COMMENT '对象层级',
  `object_id` char(36) DEFAULT NULL COMMENT '对象ID',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '实物实例ID',
  `description` text COMMENT '说明',
  `approval_status` varchar(50) NOT NULL DEFAULT 'draft' COMMENT '审批状态',
  `approved_by` varchar(100) DEFAULT NULL COMMENT '批准人',
  `approved_at` datetime(6) DEFAULT NULL COMMENT '批准时间',
  `effective_from` date DEFAULT NULL COMMENT '生效日期',
  `effective_to` date DEFAULT NULL COMMENT '失效日期',
  `limitation` text COMMENT '限制条件',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_concession_number` (`concession_number`),
  KEY `idx_concession_nc` (`nc_record_id`),
  KEY `idx_concession_object` (`object_level`,`object_id`),
  KEY `idx_concession_status` (`approval_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='让步接收/偏离批准记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_config_baseline`
--

DROP TABLE IF EXISTS `t1_config_baseline`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_config_baseline` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `baseline_number` varchar(200) NOT NULL,
  `baseline_type` varchar(50) DEFAULT NULL,
  `baseline_status` varchar(50) DEFAULT NULL,
  `applicable_aircraft_ids` json DEFAULT (json_array()),
  `applicable_msn_range` json DEFAULT (json_object()),
  `ebom_id` char(36) DEFAULT NULL,
  `mbom_id` char(36) DEFAULT NULL,
  `sbom_id` char(36) DEFAULT NULL,
  `frozen_by` varchar(100) DEFAULT NULL,
  `frozen_at` datetime(6) DEFAULT NULL,
  `description` text,
  `created_by` varchar(100) NOT NULL DEFAULT 'system',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `baseline_number` (`baseline_number`),
  KEY `idx_config_baseline_ebom` (`ebom_id`),
  KEY `idx_config_baseline_mbom` (`mbom_id`),
  CONSTRAINT `config_baseline_chk_1` CHECK ((`baseline_type` in (_utf8mb4'FUNCTIONAL',_utf8mb4'ALLOCATED',_utf8mb4'PRODUCT',_utf8mb4'AS_BUILT',_utf8mb4'AS_MAINTAINED'))),
  CONSTRAINT `config_baseline_chk_2` CHECK ((`baseline_status` in (_utf8mb4'OPEN',_utf8mb4'FROZEN',_utf8mb4'RELEASED',_utf8mb4'SUPERSEDED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='技术状态基线；适用机队用 applicable_aircraft_ids；可选 ebom/mbom/sbom';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_config_bom`
--

DROP TABLE IF EXISTS `t1_config_bom`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_config_bom` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `bom_type` varchar(20) NOT NULL,
  `label` varchar(200) NOT NULL,
  `bom_version` varchar(50) NOT NULL,
  `bom_status` varchar(50) DEFAULT NULL,
  `model_id` char(36) DEFAULT NULL,
  `aircraft_id` char(36) DEFAULT NULL,
  `effectivity` json NOT NULL DEFAULT (json_object()),
  `created_by` varchar(100) NOT NULL DEFAULT 'system',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_config_bom_model_version` (`bom_type`,`bom_version`,((case when (`aircraft_id` is null) then `model_id` else NULL end))),
  UNIQUE KEY `uq_config_bom_aircraft_version` (`bom_type`,`bom_version`,((case when (`model_id` is null) then `aircraft_id` else NULL end))),
  KEY `idx_config_bom_model` (`model_id`),
  KEY `idx_config_bom_aircraft` (`aircraft_id`),
  CONSTRAINT `chk_config_bom_scope` CHECK ((((`model_id` is not null) and (`aircraft_id` is null)) or ((`model_id` is null) and (`aircraft_id` is not null)))),
  CONSTRAINT `config_bom_chk_1` CHECK ((`bom_type` in (_utf8mb4'EBOM',_utf8mb4'MBOM',_utf8mb4'SBOM',_utf8mb4'PBOM'))),
  CONSTRAINT `config_bom_chk_2` CHECK ((`bom_status` in (_utf8mb4'DRAFT',_utf8mb4'RELEASED',_utf8mb4'OBSOLETE')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='BOM 头：model_id 与 aircraft_id 二选一（型号 BOM / 单机 BOM）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_config_bom_line`
--

DROP TABLE IF EXISTS `t1_config_bom_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_config_bom_line` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `bom_id` char(36) NOT NULL,
  `parent_line_id` char(36) DEFAULT NULL,
  `level_no` int NOT NULL DEFAULT '1',
  `path` text,
  `part_number` varchar(200) NOT NULL,
  `part_name` varchar(500) DEFAULT NULL,
  `part_name_en` varchar(500) DEFAULT NULL,
  `find_number` varchar(50) DEFAULT NULL,
  `position_code` varchar(100) DEFAULT NULL,
  `quantity` decimal(12,4) NOT NULL DEFAULT '1.0000',
  `quantity_unit` varchar(20) NOT NULL DEFAULT 'EA',
  `is_phantom` tinyint(1) NOT NULL DEFAULT '0',
  `effectivity` json NOT NULL DEFAULT (json_object()),
  `notes` text,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_config_bom_line_bom` (`bom_id`),
  KEY `idx_config_bom_line_parent` (`parent_line_id`),
  KEY `idx_config_bom_line_part` (`bom_id`,`part_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='BOM 行：件号/名称落在行上，树形 parent_line_id（与 t1_aircraft_bom_node 装机树解耦）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_config_deviation`
--

DROP TABLE IF EXISTS `t1_config_deviation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_config_deviation` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `deviation_number` varchar(200) NOT NULL,
  `deviation_type` varchar(50) DEFAULT NULL,
  `snapshot_id` char(36) NOT NULL,
  `baseline_id` char(36) NOT NULL,
  `deviation_description` text NOT NULL,
  `disposition` varchar(50) DEFAULT NULL,
  `approval_status` varchar(50) DEFAULT NULL,
  `approved_by` varchar(100) DEFAULT NULL,
  `approved_at` datetime(6) DEFAULT NULL,
  `effectivity` json NOT NULL DEFAULT (json_object()),
  `created_by` varchar(100) NOT NULL DEFAULT 'system',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `deviation_number` (`deviation_number`),
  KEY `idx_config_deviation_snapshot` (`snapshot_id`),
  KEY `idx_config_deviation_baseline` (`baseline_id`),
  CONSTRAINT `config_deviation_chk_1` CHECK ((`deviation_type` in (_utf8mb4'DEVIATION',_utf8mb4'WAIVER',_utf8mb4'NCR',_utf8mb4'OUT_OF_TOLERANCE'))),
  CONSTRAINT `config_deviation_chk_2` CHECK ((`disposition` in (_utf8mb4'USE_AS_IS',_utf8mb4'REPAIR',_utf8mb4'REWORK',_utf8mb4'SCRAP',_utf8mb4'RETURN_TO_SUPPLIER',_utf8mb4'PENDING')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='偏离类：snapshot_id + baseline_id 双外键，无单独关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_config_increment`
--

DROP TABLE IF EXISTS `t1_config_increment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_config_increment` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `source_snapshot_id` char(36) NOT NULL,
  `target_snapshot_id` char(36) NOT NULL,
  `summary_json` json NOT NULL DEFAULT (json_object()),
  `algorithm_code` varchar(64) DEFAULT NULL,
  `algorithm_version` varchar(32) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_config_increment_src` (`source_snapshot_id`),
  KEY `idx_config_increment_tgt` (`target_snapshot_id`),
  CONSTRAINT `chk_increment_distinct` CHECK ((`source_snapshot_id` <> `target_snapshot_id`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='快照间增量：源/目标快照外键在同一行';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_config_snapshot`
--

DROP TABLE IF EXISTS `t1_config_snapshot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_config_snapshot` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `aircraft_id` char(36) NOT NULL,
  `snapshot_type` varchar(50) DEFAULT NULL,
  `snapshot_time` datetime(6) NOT NULL,
  `fh_at_snapshot` decimal(12,2) DEFAULT NULL,
  `fc_at_snapshot` int DEFAULT NULL,
  `configuration_data` json NOT NULL DEFAULT (json_object()),
  `snapshot_json` json NOT NULL DEFAULT (json_object()),
  `integrity_hash` varchar(64) DEFAULT NULL,
  `recorded_node_count` int DEFAULT NULL,
  `baseline_id` char(36) DEFAULT NULL,
  `notes` text,
  `created_by` varchar(100) NOT NULL DEFAULT 'system',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_config_snapshot_aircraft` (`aircraft_id`),
  KEY `idx_config_snapshot_time` (`snapshot_time`),
  KEY `idx_config_snapshot_baseline` (`baseline_id`),
  CONSTRAINT `config_snapshot_chk_1` CHECK ((`snapshot_type` in (_utf8mb4'AS_DESIGNED',_utf8mb4'AS_BUILT',_utf8mb4'AS_MAINTAINED',_utf8mb4'DELIVERY',_utf8mb4'PRE_CHECK',_utf8mb4'POST_CHECK',_utf8mb4'AD_HOC')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='构型快照；含 snapshot_json、integrity_hash、recorded_node_count；baseline_id 可选';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_config_snapshot_node`
--

DROP TABLE IF EXISTS `t1_config_snapshot_node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_config_snapshot_node` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `snapshot_id` char(36) NOT NULL,
  `source_node_id` char(36) DEFAULT NULL,
  `part_number` varchar(200) DEFAULT NULL,
  `serial_number` varchar(200) DEFAULT NULL,
  `position_code` varchar(100) DEFAULT NULL,
  `ata_chapter` varchar(20) DEFAULT NULL,
  `tsn_fh` decimal(12,2) DEFAULT NULL,
  `tsn_fc` int DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT NULL,
  `detail_json` json NOT NULL DEFAULT (json_object()),
  PRIMARY KEY (`id`),
  KEY `idx_config_snapshot_node_snap` (`snapshot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='快照时刻 BOM 节点关键字段冻结拷贝';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_correlation_result`
--

DROP TABLE IF EXISTS `t1_correlation_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_correlation_result` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '相关性结果ID',
  `task_id` char(36) NOT NULL COMMENT '分析任务ID',
  `source_object_type` varchar(50) NOT NULL COMMENT '源对象类型',
  `source_object_id` char(36) DEFAULT NULL COMMENT '源对象ID',
  `target_object_type` varchar(50) NOT NULL COMMENT '目标对象类型',
  `target_object_id` char(36) DEFAULT NULL COMMENT '目标对象ID',
  `relation_type` varchar(50) NOT NULL COMMENT '关联类型',
  `correlation_score` decimal(10,6) NOT NULL DEFAULT '0.000000' COMMENT '相关性得分',
  `support_count` int NOT NULL DEFAULT '0' COMMENT '支持样本数',
  `confidence` decimal(5,4) DEFAULT NULL COMMENT '可信度',
  `lift_value` decimal(10,6) DEFAULT NULL COMMENT '提升度',
  `result_summary` text COMMENT '结果说明',
  `evidence_json` json NOT NULL DEFAULT (json_array()) COMMENT '证据',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_correlation_task` (`task_id`),
  KEY `idx_correlation_source` (`source_object_type`,`source_object_id`),
  KEY `idx_correlation_target` (`target_object_type`,`target_object_id`),
  KEY `idx_correlation_type` (`relation_type`),
  KEY `idx_correlation_score` (`correlation_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='跨域相关性分析结果';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_alert`
--

DROP TABLE IF EXISTS `t1_data_alert`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_alert` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `monitoring_run_id` char(36) DEFAULT NULL,
  `issue_id` char(36) DEFAULT NULL,
  `alert_code` varchar(100) DEFAULT NULL,
  `alert_level` varchar(24) NOT NULL DEFAULT 'warning',
  `alert_status` varchar(24) NOT NULL DEFAULT 'open',
  `alert_title` varchar(300) NOT NULL,
  `alert_message` text,
  `notified_to_json` json NOT NULL DEFAULT (json_array()),
  `first_seen_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `last_seen_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `acknowledged_by` varchar(100) DEFAULT NULL,
  `acknowledged_at` datetime(6) DEFAULT NULL,
  `closed_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `alert_code` (`alert_code`),
  KEY `idx_data_alert_monitoring` (`monitoring_run_id`),
  KEY `idx_data_alert_issue` (`issue_id`),
  KEY `idx_data_alert_status` (`alert_status`,`alert_level`),
  CONSTRAINT `data_alert_chk_1` CHECK ((`alert_level` in (_utf8mb4'info',_utf8mb4'warning',_utf8mb4'major',_utf8mb4'critical'))),
  CONSTRAINT `data_alert_chk_2` CHECK ((`alert_status` in (_utf8mb4'open',_utf8mb4'acknowledged',_utf8mb4'closed',_utf8mb4'suppressed')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据监控告警：由周期监控或在线规则产生，可关联问题台账';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_approval_task`
--

DROP TABLE IF EXISTS `t1_data_approval_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_approval_task` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `subject_type` varchar(30) NOT NULL,
  `subject_id` char(36) NOT NULL,
  `step_no` int NOT NULL DEFAULT '1',
  `approval_status` varchar(24) NOT NULL DEFAULT 'pending',
  `approver_person_id` char(36) DEFAULT NULL,
  `approver_org_id` char(36) DEFAULT NULL,
  `opinion` text,
  `acted_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `subject_type` (`subject_type`,`subject_id`,`step_no`),
  KEY `idx_data_approval_subject` (`subject_type`,`subject_id`),
  KEY `idx_data_approval_status` (`approval_status`),
  CONSTRAINT `data_approval_task_chk_1` CHECK ((`subject_type` in (_utf8mb4'correction_ticket',_utf8mb4'update_request',_utf8mb4'issue_close',_utf8mb4'export_job',_utf8mb4'support_request',_utf8mb4'other'))),
  CONSTRAINT `data_approval_task_chk_2` CHECK ((`approval_status` in (_utf8mb4'pending',_utf8mb4'approved',_utf8mb4'rejected',_utf8mb4'skipped',_utf8mb4'cancelled')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据维护审批任务：支持更正、更新、问题关闭、导出和对外支持审批';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_check_result`
--

DROP TABLE IF EXISTS `t1_data_check_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_check_result` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `inspection_run_id` char(36) DEFAULT NULL,
  `monitoring_run_id` char(36) DEFAULT NULL,
  `rule_id` char(36) DEFAULT NULL,
  `issue_id` char(36) DEFAULT NULL,
  `target_table` varchar(200) NOT NULL,
  `target_record_id` char(36) DEFAULT NULL,
  `target_record_key` varchar(500) DEFAULT NULL,
  `target_field` varchar(200) DEFAULT NULL,
  `target_path` text,
  `pass_status` varchar(20) NOT NULL,
  `severity` varchar(24) DEFAULT NULL,
  `expected_value_json` json DEFAULT NULL,
  `actual_value_json` json DEFAULT NULL,
  `evidence_json` json NOT NULL DEFAULT (json_object()),
  `checked_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_data_check_result_inspection` (`inspection_run_id`),
  KEY `idx_data_check_result_monitoring` (`monitoring_run_id`),
  KEY `idx_data_check_result_rule` (`rule_id`),
  KEY `idx_data_check_result_issue` (`issue_id`),
  KEY `idx_data_check_result_target` (`target_table`,`target_record_id`,`target_record_key`),
  CONSTRAINT `chk_data_check_result_from_run` CHECK ((((`inspection_run_id` is not null) and (`monitoring_run_id` is null)) or ((`inspection_run_id` is null) and (`monitoring_run_id` is not null)))),
  CONSTRAINT `data_check_result_chk_1` CHECK ((`pass_status` in (_utf8mb4'passed',_utf8mb4'failed',_utf8mb4'warning',_utf8mb4'skipped')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='规则执行结果明细：记录目标对象、字段、期望/实际值，可关联问题台账';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_correction_ticket`
--

DROP TABLE IF EXISTS `t1_data_correction_ticket`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_correction_ticket` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `issue_id` char(36) NOT NULL,
  `ticket_code` varchar(100) DEFAULT NULL,
  `correction_type` varchar(30) NOT NULL DEFAULT 'data_fix',
  `status` varchar(24) NOT NULL DEFAULT 'open',
  `assigned_to_person_id` char(36) DEFAULT NULL,
  `assigned_to_org_id` char(36) DEFAULT NULL,
  `plan_description` text,
  `execution_result` text,
  `verified_by_person_id` char(36) DEFAULT NULL,
  `verified_at` datetime(6) DEFAULT NULL,
  `closed_at` datetime(6) DEFAULT NULL,
  `close_reason` text,
  `payload_json` json NOT NULL DEFAULT (json_object()),
  `created_by` varchar(100) NOT NULL DEFAULT 'system',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `ticket_code` (`ticket_code`),
  KEY `idx_data_correction_issue` (`issue_id`),
  KEY `idx_data_correction_status` (`status`),
  KEY `idx_data_correction_assignee` (`assigned_to_org_id`,`assigned_to_person_id`),
  CONSTRAINT `data_correction_ticket_chk_1` CHECK ((`correction_type` in (_utf8mb4'data_fix',_utf8mb4'supplement',_utf8mb4'relink',_utf8mb4'reclassify',_utf8mb4'ignore',_utf8mb4'other'))),
  CONSTRAINT `data_correction_ticket_chk_2` CHECK ((`status` in (_utf8mb4'open',_utf8mb4'assigned',_utf8mb4'processing',_utf8mb4'pending_verify',_utf8mb4'verified',_utf8mb4'closed',_utf8mb4'rejected')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='更正工单：针对 t1_data_issue，记录整改方案、执行结果和验证关闭';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_evidence_file`
--

DROP TABLE IF EXISTS `t1_data_evidence_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_evidence_file` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `subject_type` varchar(30) NOT NULL,
  `subject_id` char(36) NOT NULL,
  `file_name` varchar(500) NOT NULL,
  `file_storage_key` text NOT NULL,
  `file_hash` varchar(128) DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `mime_type` varchar(200) DEFAULT NULL,
  `description` text,
  `uploaded_by` varchar(100) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_data_evidence_subject` (`subject_type`,`subject_id`),
  CONSTRAINT `data_evidence_file_chk_1` CHECK ((`subject_type` in (_utf8mb4'issue',_utf8mb4'correction_ticket',_utf8mb4'update_request',_utf8mb4'update_item',_utf8mb4'check_result',_utf8mb4'other')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='维护证据附件：挂接问题、工单、更新申请、更新明细或校验结果';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_exchange_log`
--

DROP TABLE IF EXISTS `t1_data_exchange_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_exchange_log` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `exchange_type` varchar(30) NOT NULL,
  `subject_type` varchar(50) NOT NULL,
  `subject_id` char(36) NOT NULL,
  `receiver` varchar(300) DEFAULT NULL,
  `format_type` varchar(30) DEFAULT NULL,
  `transport_method` varchar(50) DEFAULT NULL,
  `exchange_status` varchar(24) NOT NULL DEFAULT 'pending',
  `sent_at` datetime(6) DEFAULT NULL,
  `error_message` text,
  `detail_json` json NOT NULL DEFAULT (json_object()),
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_data_exchange_subject` (`subject_type`,`subject_id`),
  KEY `idx_data_exchange_status` (`exchange_status`,`created_at`),
  CONSTRAINT `data_exchange_log_chk_1` CHECK ((`exchange_type` in (_utf8mb4'dossier_export',_utf8mb4't1_data_support_package',_utf8mb4'api_push',_utf8mb4'manual_delivery',_utf8mb4'other'))),
  CONSTRAINT `data_exchange_log_chk_2` CHECK ((`exchange_status` in (_utf8mb4'pending',_utf8mb4'sent',_utf8mb4'failed',_utf8mb4'cancelled')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据交换日志：记录卷宗导出或对外数据包的发送/交付状态';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_inspection_run`
--

DROP TABLE IF EXISTS `t1_data_inspection_run`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_inspection_run` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `dossier_instance_id` char(36) NOT NULL,
  `dossier_version_id` char(36) DEFAULT NULL COMMENT '卷宗版本ID',
  `check_scope_type` varchar(50) DEFAULT NULL COMMENT '检查范围类型：dossier/section/bom_node/key_part/purchased_part',
  `check_scope_id` char(36) DEFAULT NULL COMMENT '检查范围对象ID',
  `run_code` varchar(100) DEFAULT NULL,
  `trigger_type` varchar(24) NOT NULL DEFAULT 'manual',
  `run_status` varchar(24) NOT NULL DEFAULT 'running',
  `scope_description` text,
  `rule_set_version` varchar(64) DEFAULT NULL,
  `checked_object_count` int NOT NULL DEFAULT '0',
  `issue_count` int NOT NULL DEFAULT '0',
  `initiated_by` varchar(100) DEFAULT NULL,
  `summary_json` json NOT NULL DEFAULT (json_object()),
  `started_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `finished_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `run_code` (`run_code`),
  KEY `idx_data_inspection_instance` (`dossier_instance_id`),
  KEY `idx_data_inspection_status` (`run_status`,`started_at`),
  KEY `idx_data_inspection_run_version` (`dossier_version_id`),
  KEY `idx_data_inspection_run_scope` (`check_scope_type`,`check_scope_id`),
  CONSTRAINT `data_inspection_run_chk_1` CHECK ((`trigger_type` in (_utf8mb4'manual',_utf8mb4'scheduled',_utf8mb4'api',_utf8mb4'import',_utf8mb4'pre_export',_utf8mb4'other'))),
  CONSTRAINT `data_inspection_run_chk_2` CHECK ((`run_status` in (_utf8mb4'queued',_utf8mb4'running',_utf8mb4'passed',_utf8mb4'failed',_utf8mb4'cancelled')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据检验批次：先执行；数据来源为卷宗实例下已存储的 t1_document_entry / structure_node 等';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_issue`
--

DROP TABLE IF EXISTS `t1_data_issue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_issue` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `issue_code` varchar(100) DEFAULT NULL,
  `rule_id` char(36) DEFAULT NULL,
  `inspection_run_id` char(36) DEFAULT NULL,
  `monitoring_run_id` char(36) DEFAULT NULL,
  `document_entry_id` char(36) DEFAULT NULL,
  `structure_node_id` char(36) DEFAULT NULL,
  `issue_type` varchar(30) NOT NULL DEFAULT 'quality',
  `issue_status` varchar(24) NOT NULL DEFAULT 'open',
  `priority` varchar(24) NOT NULL DEFAULT 'medium',
  `source_type` varchar(30) NOT NULL DEFAULT 'inspection',
  `target_table` varchar(200) DEFAULT NULL,
  `target_record_id` char(36) DEFAULT NULL,
  `target_record_key` varchar(500) DEFAULT NULL,
  `target_field` varchar(200) DEFAULT NULL,
  `target_path` text,
  `expected_value_json` json DEFAULT NULL,
  `actual_value_json` json DEFAULT NULL,
  `related_trace_json` json DEFAULT NULL,
  `owner_person_id` char(36) DEFAULT NULL,
  `owner_org_id` char(36) DEFAULT NULL,
  `due_at` datetime(6) DEFAULT NULL,
  `severity` varchar(24) NOT NULL,
  `message` text NOT NULL,
  `detail_json` json NOT NULL DEFAULT (json_object()),
  `close_reason` text,
  `resolved_at` datetime(6) DEFAULT NULL,
  `closed_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(100) NOT NULL DEFAULT 'system',
  `updated_by` varchar(100) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `issue_code` (`issue_code`),
  KEY `idx_data_issue_inspection` (`inspection_run_id`),
  KEY `idx_data_issue_monitoring` (`monitoring_run_id`),
  KEY `idx_data_issue_rule` (`rule_id`),
  KEY `idx_data_issue_status` (`issue_status`,`priority`),
  KEY `idx_data_issue_owner` (`owner_org_id`,`owner_person_id`),
  KEY `idx_data_issue_target` (`target_table`,`target_record_id`,`target_record_key`),
  CONSTRAINT `chk_data_issue_from_run` CHECK ((((`inspection_run_id` is not null) and (`monitoring_run_id` is null)) or ((`inspection_run_id` is null) and (`monitoring_run_id` is not null)))),
  CONSTRAINT `data_issue_chk_1` CHECK ((`issue_type` in (_utf8mb4'quality',_utf8mb4'completeness',_utf8mb4'consistency',_utf8mb4'validity',_utf8mb4'timeliness',_utf8mb4'relation',_utf8mb4'duplicate',_utf8mb4'business_exception',_utf8mb4'other'))),
  CONSTRAINT `data_issue_chk_2` CHECK ((`issue_status` in (_utf8mb4'open',_utf8mb4'assigned',_utf8mb4'processing',_utf8mb4'pending_approval',_utf8mb4'corrected',_utf8mb4'verified',_utf8mb4'closed',_utf8mb4'rejected'))),
  CONSTRAINT `data_issue_chk_3` CHECK ((`priority` in (_utf8mb4'low',_utf8mb4'medium',_utf8mb4'high',_utf8mb4'urgent'))),
  CONSTRAINT `data_issue_chk_4` CHECK ((`source_type` in (_utf8mb4'inspection',_utf8mb4'monitoring',_utf8mb4'manual',_utf8mb4'import',_utf8mb4'other')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='问题台账：来源于某次检验或某次监控（二选一）；可分派责任人/组织并进入更正闭环';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_monitoring_run`
--

DROP TABLE IF EXISTS `t1_data_monitoring_run`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_monitoring_run` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `dossier_instance_id` char(36) NOT NULL,
  `monitoring_code` varchar(100) DEFAULT NULL,
  `trigger_type` varchar(24) NOT NULL DEFAULT 'scheduled',
  `run_status` varchar(24) NOT NULL DEFAULT 'running',
  `scope_description` text,
  `rule_set_version` varchar(64) DEFAULT NULL,
  `checked_object_count` int NOT NULL DEFAULT '0',
  `issue_count` int NOT NULL DEFAULT '0',
  `alert_count` int NOT NULL DEFAULT '0',
  `initiated_by` varchar(100) DEFAULT NULL,
  `summary_json` json NOT NULL DEFAULT (json_object()),
  `started_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `finished_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `monitoring_code` (`monitoring_code`),
  KEY `idx_data_monitoring_instance` (`dossier_instance_id`),
  KEY `idx_data_monitoring_status` (`run_status`,`started_at`),
  CONSTRAINT `data_monitoring_run_chk_1` CHECK ((`trigger_type` in (_utf8mb4'manual',_utf8mb4'scheduled',_utf8mb4'api',_utf8mb4'import',_utf8mb4'pre_export',_utf8mb4'other'))),
  CONSTRAINT `data_monitoring_run_chk_2` CHECK ((`run_status` in (_utf8mb4'queued',_utf8mb4'running',_utf8mb4'passed',_utf8mb4'failed',_utf8mb4'cancelled')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据监控批次：检验之后的持续/周期监控；数据来源同上';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_quality_rule`
--

DROP TABLE IF EXISTS `t1_data_quality_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_quality_rule` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `rule_code` varchar(100) NOT NULL,
  `rule_name` varchar(200) NOT NULL,
  `rule_type` varchar(30) NOT NULL,
  `target_domain` varchar(50) NOT NULL DEFAULT 'DOSSIER',
  `target_table` varchar(200) DEFAULT NULL,
  `target_field` varchar(200) DEFAULT NULL,
  `severity_default` varchar(24) NOT NULL DEFAULT 'medium',
  `rule_expression_json` json NOT NULL DEFAULT (json_object()),
  `remediation_hint` text,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `version_label` varchar(64) NOT NULL DEFAULT 'v1',
  `owner_org_id` char(36) DEFAULT NULL,
  `created_by` varchar(100) NOT NULL DEFAULT 'system',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `rule_code` (`rule_code`),
  KEY `idx_data_quality_rule_active` (`is_active`,`rule_type`),
  KEY `idx_data_quality_rule_target` (`target_domain`,`target_table`,`target_field`),
  CONSTRAINT `data_quality_rule_chk_1` CHECK ((`rule_type` in (_utf8mb4'COMPLETENESS',_utf8mb4'CONSISTENCY',_utf8mb4'VALIDITY',_utf8mb4'TIMELINESS',_utf8mb4'RELATION',_utf8mb4'UNIQUENESS',_utf8mb4'BUSINESS',_utf8mb4'OTHER'))),
  CONSTRAINT `data_quality_rule_chk_2` CHECK ((`severity_default` in (_utf8mb4'low',_utf8mb4'medium',_utf8mb4'high',_utf8mb4'critical')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据质量规则：完整性、一致性、有效性、时效性、关联性、唯一性和业务规则';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_relation_edge`
--

DROP TABLE IF EXISTS `t1_data_relation_edge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_relation_edge` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '关系ID',
  `edge_code` varchar(100) DEFAULT NULL COMMENT '关系编码',
  `source_node_id` char(36) NOT NULL COMMENT '起点节点ID',
  `target_node_id` char(36) NOT NULL COMMENT '终点节点ID',
  `relation_type` varchar(50) NOT NULL COMMENT '关系类型：BELONGS_TO/SOURCE_OF/GENERATES/REFERENCES/IMPACTS等',
  `relation_name` varchar(200) DEFAULT NULL COMMENT '关系名称',
  `direction` varchar(20) NOT NULL DEFAULT 'directed' COMMENT '方向：directed/undirected',
  `weight` decimal(10,4) NOT NULL DEFAULT '1.0000' COMMENT '关系权重',
  `confidence` decimal(5,4) DEFAULT NULL COMMENT '可信度',
  `evidence_type` varchar(50) DEFAULT NULL COMMENT '证据类型',
  `evidence_table` varchar(200) DEFAULT NULL COMMENT '证据来源表',
  `evidence_record_id` char(36) DEFAULT NULL COMMENT '证据记录ID',
  `evidence_summary` text COMMENT '证据摘要',
  `dossier_instance_id` char(36) DEFAULT NULL COMMENT '关联卷宗实例ID',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '关联飞机ID',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '关系扩展属性',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否有效',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_data_relation_edge_code` (`edge_code`),
  KEY `idx_data_relation_edge_source` (`source_node_id`),
  KEY `idx_data_relation_edge_target` (`target_node_id`),
  KEY `idx_data_relation_edge_type` (`relation_type`),
  KEY `idx_data_relation_edge_dossier` (`dossier_instance_id`),
  KEY `idx_data_relation_edge_aircraft` (`aircraft_id`),
  CONSTRAINT `chk_data_relation_edge_direction` CHECK ((`direction` in (_utf8mb4'directed',_utf8mb4'undirected')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据关系图关系：用于检索结果关联展开和跨域关联展示';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_relation_node`
--

DROP TABLE IF EXISTS `t1_data_relation_node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_relation_node` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '节点ID',
  `node_code` varchar(100) NOT NULL COMMENT '节点编码',
  `node_type` varchar(50) NOT NULL COMMENT '节点类型：AIRCRAFT/PART/DOCUMENT/WORK_ORDER/FAULT/DOSSIER等',
  `domain` varchar(30) DEFAULT NULL COMMENT '所属阶段：DESIGN/MANUFACTURING/SERVICE/DOSSIER',
  `display_name` varchar(300) NOT NULL COMMENT '展示名称',
  `source_table` varchar(200) NOT NULL COMMENT '来源表名',
  `source_record_id` char(36) DEFAULT NULL COMMENT '来源记录ID',
  `source_record_key` varchar(500) DEFAULT NULL COMMENT '来源业务键',
  `dossier_instance_id` char(36) DEFAULT NULL COMMENT '关联卷宗实例ID',
  `dossier_version_id` char(36) DEFAULT NULL COMMENT '关联卷宗版本ID',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '关联飞机ID',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '节点扩展属性',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否有效',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_data_relation_node_code` (`node_code`),
  KEY `idx_data_relation_node_type` (`node_type`,`domain`),
  KEY `idx_data_relation_node_source` (`source_table`,`source_record_id`),
  KEY `idx_data_relation_node_dossier` (`dossier_instance_id`,`dossier_version_id`),
  KEY `idx_data_relation_node_aircraft` (`aircraft_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据关系图节点：用于卷宗数据关联关系可视化';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_snapshot`
--

DROP TABLE IF EXISTS `t1_data_snapshot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_snapshot` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `dossier_instance_id` char(36) DEFAULT NULL,
  `snapshot_code` varchar(100) DEFAULT NULL,
  `snapshot_type` varchar(30) NOT NULL,
  `subject_type` varchar(50) DEFAULT NULL,
  `subject_id` char(36) DEFAULT NULL,
  `snapshot_data_json` json NOT NULL,
  `created_by` varchar(100) NOT NULL DEFAULT 'system',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `snapshot_code` (`snapshot_code`),
  KEY `idx_data_snapshot_instance` (`dossier_instance_id`),
  KEY `idx_data_snapshot_subject` (`subject_type`,`subject_id`),
  CONSTRAINT `data_snapshot_chk_1` CHECK ((`snapshot_type` in (_utf8mb4'before_update',_utf8mb4'after_update',_utf8mb4'export',_utf8mb4'checkpoint',_utf8mb4'other')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据快照：维护前后、导出或关键检查点的数据冻结记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_support_dataset_item`
--

DROP TABLE IF EXISTS `t1_data_support_dataset_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_support_dataset_item` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `package_id` char(36) NOT NULL,
  `dossier_instance_id` char(36) DEFAULT NULL,
  `structure_node_id` char(36) DEFAULT NULL,
  `document_entry_id` char(36) DEFAULT NULL,
  `source_table` varchar(200) NOT NULL,
  `source_record_id` char(36) DEFAULT NULL,
  `source_record_key` varchar(500) DEFAULT NULL,
  `include_reason` text,
  `attrs_json` json NOT NULL DEFAULT (json_object()),
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_data_support_item_package` (`package_id`),
  KEY `idx_data_support_item_dossier` (`dossier_instance_id`),
  KEY `idx_data_support_item_source` (`source_table`,`source_record_id`,`source_record_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对外数据包条目：列出数据包包含的卷宗、结构节点、文档或源表记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_support_package`
--

DROP TABLE IF EXISTS `t1_data_support_package`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_support_package` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `request_id` char(36) NOT NULL,
  `package_code` varchar(100) NOT NULL,
  `package_name` varchar(300) NOT NULL,
  `package_status` varchar(24) NOT NULL DEFAULT 'building',
  `data_format` varchar(30) NOT NULL DEFAULT 'JSON',
  `scope_json` json NOT NULL DEFAULT (json_object()),
  `file_storage_key` text,
  `file_hash` varchar(128) DEFAULT NULL,
  `record_count` int NOT NULL DEFAULT '0',
  `generated_at` datetime(6) DEFAULT NULL,
  `delivered_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `package_code` (`package_code`),
  KEY `idx_data_support_package_request` (`request_id`),
  KEY `idx_data_support_package_status` (`package_status`),
  CONSTRAINT `data_support_package_chk_1` CHECK ((`package_status` in (_utf8mb4'building',_utf8mb4'ready',_utf8mb4'delivered',_utf8mb4'failed',_utf8mb4'cancelled'))),
  CONSTRAINT `data_support_package_chk_2` CHECK ((`data_format` in (_utf8mb4'JSON',_utf8mb4'CSV',_utf8mb4'EXCEL',_utf8mb4'ZIP',_utf8mb4'PARQUET',_utf8mb4'OTHER')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对外数据包：记录数据格式、范围、文件和交付状态';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_support_request`
--

DROP TABLE IF EXISTS `t1_data_support_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_support_request` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `request_code` varchar(100) NOT NULL,
  `requester_topic` varchar(300) NOT NULL,
  `requester_org` varchar(200) DEFAULT NULL,
  `requester_contact` varchar(200) DEFAULT NULL,
  `purpose` text,
  `requested_scope_json` json NOT NULL DEFAULT (json_object()),
  `approval_status` varchar(24) NOT NULL DEFAULT 'pending',
  `delivery_status` varchar(24) NOT NULL DEFAULT 'draft',
  `approved_by` varchar(100) DEFAULT NULL,
  `approved_at` datetime(6) DEFAULT NULL,
  `closed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `request_code` (`request_code`),
  KEY `idx_data_support_request_status` (`approval_status`,`delivery_status`),
  CONSTRAINT `data_support_request_chk_1` CHECK ((`approval_status` in (_utf8mb4'pending',_utf8mb4'approved',_utf8mb4'rejected',_utf8mb4'cancelled'))),
  CONSTRAINT `data_support_request_chk_2` CHECK ((`delivery_status` in (_utf8mb4'draft',_utf8mb4'building',_utf8mb4'delivered',_utf8mb4'closed',_utf8mb4'failed')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对外数据支持申请：面向其他课题/外部分析软件的数据需求登记';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_update_execution`
--

DROP TABLE IF EXISTS `t1_data_update_execution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_update_execution` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `request_id` char(36) NOT NULL,
  `execution_status` varchar(24) NOT NULL DEFAULT 'running',
  `executor` varchar(100) DEFAULT NULL,
  `started_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `finished_at` datetime(6) DEFAULT NULL,
  `affected_count` int NOT NULL DEFAULT '0',
  `error_message` text,
  `execution_log_json` json NOT NULL DEFAULT (json_object()),
  `rollback_payload_json` json NOT NULL DEFAULT (json_object()),
  PRIMARY KEY (`id`),
  KEY `idx_data_update_execution_request` (`request_id`),
  CONSTRAINT `data_update_execution_chk_1` CHECK ((`execution_status` in (_utf8mb4'running',_utf8mb4'succeeded',_utf8mb4'failed',_utf8mb4'rolled_back')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='更新执行日志：记录实际写入、失败原因与回滚载荷';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_update_item`
--

DROP TABLE IF EXISTS `t1_data_update_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_update_item` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `request_id` char(36) NOT NULL,
  `item_order` int NOT NULL DEFAULT '0',
  `target_table` varchar(200) NOT NULL,
  `target_record_id` char(36) DEFAULT NULL,
  `target_record_key` varchar(500) DEFAULT NULL,
  `target_field` varchar(200) DEFAULT NULL,
  `target_path` text,
  `old_value_json` json DEFAULT NULL,
  `new_value_json` json DEFAULT NULL,
  `change_reason` text,
  `validation_status` varchar(24) NOT NULL DEFAULT 'pending',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_data_update_item_request` (`request_id`),
  KEY `idx_data_update_item_target` (`target_table`,`target_record_id`,`target_record_key`),
  CONSTRAINT `data_update_item_chk_1` CHECK ((`validation_status` in (_utf8mb4'pending',_utf8mb4'passed',_utf8mb4'failed',_utf8mb4'skipped')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='更新申请字段级明细：目标表/记录/字段、旧值、新值和变更原因';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_data_update_request`
--

DROP TABLE IF EXISTS `t1_data_update_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_data_update_request` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `dossier_instance_id` char(36) NOT NULL,
  `source_ticket_id` char(36) DEFAULT NULL,
  `request_code` varchar(100) DEFAULT NULL,
  `request_type` varchar(30) NOT NULL DEFAULT 'correction',
  `request_title` varchar(300) DEFAULT NULL,
  `request_reason` text,
  `impact_scope_json` json NOT NULL DEFAULT (json_object()),
  `approval_status` varchar(24) NOT NULL DEFAULT 'pending',
  `execution_status` varchar(24) NOT NULL DEFAULT 'pending',
  `request_payload_json` json NOT NULL DEFAULT (json_object()),
  `requested_by_person_id` char(36) DEFAULT NULL,
  `requested_by_org_id` char(36) DEFAULT NULL,
  `approved_by_person_id` char(36) DEFAULT NULL,
  `approved_at` datetime(6) DEFAULT NULL,
  `rejected_reason` text,
  `applied_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `request_code` (`request_code`),
  KEY `idx_data_update_instance` (`dossier_instance_id`),
  KEY `idx_data_update_ticket` (`source_ticket_id`),
  KEY `idx_data_update_status` (`approval_status`,`execution_status`),
  CONSTRAINT `data_update_request_chk_1` CHECK ((`request_type` in (_utf8mb4'correction',_utf8mb4'supplement',_utf8mb4'sync_refresh',_utf8mb4'manual_update',_utf8mb4'rollback',_utf8mb4'other'))),
  CONSTRAINT `data_update_request_chk_2` CHECK ((`approval_status` in (_utf8mb4'pending',_utf8mb4'approved',_utf8mb4'rejected',_utf8mb4'cancelled'))),
  CONSTRAINT `data_update_request_chk_3` CHECK ((`execution_status` in (_utf8mb4'pending',_utf8mb4'running',_utf8mb4'succeeded',_utf8mb4'failed',_utf8mb4'rolled_back')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='更新申请：审批通过后生成新的 t1_dossier_version，并刷新 t1_dossier_instance.current_version_id/current_version_no；applied_at 表示已执行';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_dispatch_assignment`
--

DROP TABLE IF EXISTS `t1_dispatch_assignment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_dispatch_assignment` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `shop_order_task_id` char(36) NOT NULL,
  `equipment_id` varchar(50) DEFAULT NULL,
  `personnel_id` varchar(50) DEFAULT NULL,
  `assigned_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `assigned_by` varchar(100) DEFAULT NULL,
  `planned_hours` decimal(8,2) DEFAULT NULL,
  `actual_hours` decimal(8,2) DEFAULT NULL,
  `assignment_type` varchar(20) NOT NULL DEFAULT 'DIRECT',
  `assignment_attrs_json` json NOT NULL DEFAULT (json_object()),
  `status` varchar(20) NOT NULL DEFAULT 'ASSIGNED',
  `remarks` text,
  PRIMARY KEY (`id`),
  KEY `idx_dispatch_assignment_task` (`shop_order_task_id`),
  KEY `idx_dispatch_assignment_equipment` (`equipment_id`),
  KEY `idx_dispatch_assignment_personnel` (`personnel_id`),
  CONSTRAINT `chk_dispatch_assignee` CHECK (((`equipment_id` is not null) or (`personnel_id` is not null))),
  CONSTRAINT `dispatch_assignment_chk_1` CHECK ((`status` in (_utf8mb4'ASSIGNED',_utf8mb4'ACCEPTED',_utf8mb4'REJECTED',_utf8mb4'COMPLETED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='派工：任务到人/设备';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_document_archive`
--

DROP TABLE IF EXISTS `t1_document_archive`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_document_archive` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `ref_type` varchar(30) NOT NULL,
  `ref_id` char(36) NOT NULL,
  `doc_type` varchar(30) NOT NULL,
  `doc_number` varchar(200) DEFAULT NULL,
  `issue_date` date DEFAULT NULL,
  `issued_by` varchar(200) DEFAULT NULL,
  `expiry_date` date DEFAULT NULL,
  `file_path` varchar(1000) DEFAULT NULL,
  `file_hash` varchar(64) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_document_archive_ref` (`ref_type`,`ref_id`),
  KEY `idx_document_archive_doc_type` (`doc_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='适航/交付等多态文档档案（ref_type + ref_id）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_document_category`
--

DROP TABLE IF EXISTS `t1_document_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_document_category` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `parent_id` char(36) DEFAULT NULL,
  `template_id` char(36) DEFAULT NULL,
  `name` text NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  `attrs_json` json NOT NULL DEFAULT (json_object()),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_document_entry`
--

DROP TABLE IF EXISTS `t1_document_entry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_document_entry` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `dossier_instance_id` char(36) NOT NULL,
  `dossier_version_id` char(36) NOT NULL,
  `category_id` char(36) NOT NULL,
  `structure_node_id` char(36) DEFAULT NULL,
  `aircraft_id` char(36) DEFAULT NULL COMMENT '对象锚点：飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT '对象锚点：BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '对象锚点：实物实例ID',
  `object_level` varchar(30) DEFAULT NULL COMMENT '对象锚点：层级',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '对象锚点：统一对象ID',
  `doc_no` varchar(128) DEFAULT NULL,
  `title` text,
  `file_storage_key` text,
  `source_trace_json` json DEFAULT NULL,
  `source_system` varchar(50) DEFAULT NULL COMMENT '来源系统',
  `source_table` varchar(200) DEFAULT NULL COMMENT '来源表名',
  `source_record_id` char(36) DEFAULT NULL COMMENT '来源记录ID',
  `source_record_key` varchar(500) DEFAULT NULL COMMENT '来源业务键',
  `document_status` varchar(24) NOT NULL DEFAULT 'active' COMMENT '文档状态：active/missing/archived',
  `completeness_status` varchar(24) NOT NULL DEFAULT 'not_checked' COMMENT '完整性状态：not_checked/complete/warning/missing/error',
  `required_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否必需文档',
  `included_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否纳入当前卷宗',
  `attrs_json` json NOT NULL DEFAULT (json_object()),
  `document_master_id` char(36) DEFAULT NULL,
  `system_record_id` char(36) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_document_entry_instance` (`dossier_instance_id`),
  KEY `idx_document_entry_version` (`dossier_version_id`),
  KEY `idx_document_entry_category` (`category_id`),
  KEY `idx_document_entry_source` (`source_system`,`source_table`,`source_record_id`),
  KEY `idx_document_entry_completeness` (`dossier_version_id`,`completeness_status`),
  KEY `idx_document_entry_version_included` (`dossier_version_id`,`included_flag`),
  KEY `idx_doc_bom_anchor` (`aircraft_id`,`bom_node_id`),
  KEY `idx_doc_part_anchor` (`part_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_document_master`
--

DROP TABLE IF EXISTS `t1_document_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_document_master` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `document_number` varchar(500) NOT NULL,
  `document_type` varchar(100) NOT NULL,
  `title` varchar(1000) NOT NULL,
  `revision` varchar(50) DEFAULT 'A',
  `doc_status` varchar(50) DEFAULT NULL,
  `primary_file_path` varchar(1000) DEFAULT NULL,
  `effectivity` json NOT NULL DEFAULT (json_object()),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_document_master_number_rev` (`document_number`,`revision`),
  KEY `idx_document_master_number` (`document_number`),
  CONSTRAINT `document_master_chk_1` CHECK ((`doc_status` in (_utf8mb4'DRAFT',_utf8mb4'IN_REVIEW',_utf8mb4'RELEASED',_utf8mb4'SUPERSEDED',_utf8mb4'CANCELLED',_utf8mb4'ARCHIVED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_dossier_completeness_summary`
--

DROP TABLE IF EXISTS `t1_dossier_completeness_summary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_dossier_completeness_summary` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '完整性汇总ID',
  `dossier_instance_id` char(36) NOT NULL COMMENT '卷宗实例ID',
  `dossier_version_id` char(36) NOT NULL COMMENT '卷宗版本ID',
  `structure_node_id` char(36) DEFAULT NULL COMMENT '目录节点ID',
  `summary_scope` varchar(50) NOT NULL COMMENT '汇总范围：dossier/section/bom_node/key_part/purchased_part',
  `subject_type` varchar(50) DEFAULT NULL COMMENT '对象类型',
  `subject_id` char(36) DEFAULT NULL COMMENT '对象ID',
  `expected_count` int NOT NULL DEFAULT '0' COMMENT '应有数量',
  `actual_count` int NOT NULL DEFAULT '0' COMMENT '已有数量',
  `missing_count` int NOT NULL DEFAULT '0' COMMENT '缺失数量',
  `warning_count` int NOT NULL DEFAULT '0' COMMENT '提醒数量',
  `error_count` int NOT NULL DEFAULT '0' COMMENT '错误数量',
  `completeness_rate` decimal(6,2) NOT NULL DEFAULT '0.00' COMMENT '完整率',
  `check_status` varchar(24) NOT NULL DEFAULT 'not_checked' COMMENT '检查状态：not_checked/pass/warning/fail',
  `issue_summary` text COMMENT '问题摘要',
  `latest_inspection_run_id` char(36) DEFAULT NULL COMMENT '最近检查批次ID',
  `checked_at` datetime(6) DEFAULT NULL COMMENT '检查时间',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dcs_version_scope_subject` (`dossier_version_id`,`summary_scope`,`subject_type`,`subject_id`),
  KEY `idx_dcs_instance_version` (`dossier_instance_id`,`dossier_version_id`),
  KEY `idx_dcs_structure_node` (`structure_node_id`),
  KEY `idx_dcs_status` (`check_status`),
  KEY `idx_dcs_inspection` (`latest_inspection_run_id`),
  CONSTRAINT `chk_dcs_status` CHECK ((`check_status` in (_utf8mb4'not_checked',_utf8mb4'pass',_utf8mb4'warning',_utf8mb4'fail')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗完整性汇总：用于详情页快速展示完整率、缺失和异常';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_dossier_content_item`
--

DROP TABLE IF EXISTS `t1_dossier_content_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_dossier_content_item` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '内容明细ID',
  `dossier_instance_id` char(36) NOT NULL COMMENT '卷宗实例ID',
  `dossier_version_id` char(36) NOT NULL COMMENT '卷宗版本ID',
  `structure_node_id` char(36) DEFAULT NULL COMMENT '所属目录节点ID',
  `item_code` varchar(100) NOT NULL COMMENT '内容编码',
  `item_name` varchar(500) NOT NULL COMMENT '内容名称',
  `item_type` varchar(50) NOT NULL COMMENT '内容类型：AIRCRAFT/BOM_NODE/PART/DOCUMENT/WORK_ORDER/FAULT/PROOF等',
  `lifecycle_stage` varchar(30) DEFAULT NULL COMMENT '生命周期阶段：DESIGN/MANUFACTURING/SERVICE/DOSSIER',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '零件实例ID',
  `is_key_part` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否关重件',
  `supply_mode` varchar(30) DEFAULT NULL COMMENT '供给方式：self_made/purchased/outsourced/unknown',
  `source_system` varchar(50) DEFAULT NULL COMMENT '来源系统',
  `source_table` varchar(200) DEFAULT NULL COMMENT '来源表名',
  `source_record_id` char(36) DEFAULT NULL COMMENT '来源记录ID',
  `source_record_key` varchar(500) DEFAULT NULL COMMENT '来源业务键',
  `include_design_data` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否纳入设计数据',
  `include_manufacturing_data` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否纳入制造数据',
  `include_service_data` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否纳入服役数据',
  `include_source_proof` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否纳入来源证明',
  `required_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否必需',
  `included_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否纳入当前卷宗',
  `completeness_status` varchar(24) NOT NULL DEFAULT 'not_checked' COMMENT '完整性状态：not_checked/complete/warning/missing/error',
  `item_status` varchar(24) NOT NULL DEFAULT 'active' COMMENT '内容状态：active/excluded/archived',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `content_summary` text COMMENT '内容摘要',
  `file_storage_key` text COMMENT '文件存储地址',
  `source_trace_json` json NOT NULL DEFAULT (json_object()) COMMENT '来源追踪信息',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dci_version_code` (`dossier_version_id`,`item_code`),
  KEY `idx_dci_instance_version` (`dossier_instance_id`,`dossier_version_id`),
  KEY `idx_dci_structure_node` (`structure_node_id`),
  KEY `idx_dci_bom_node` (`bom_node_id`),
  KEY `idx_dci_part_instance` (`part_instance_id`),
  KEY `idx_dci_source` (`source_system`,`source_table`,`source_record_id`),
  KEY `idx_dci_key_supply` (`is_key_part`,`supply_mode`),
  KEY `idx_dci_completeness` (`completeness_status`),
  CONSTRAINT `chk_dci_completeness` CHECK ((`completeness_status` in (_utf8mb4'not_checked',_utf8mb4'complete',_utf8mb4'warning',_utf8mb4'missing',_utf8mb4'error'))),
  CONSTRAINT `chk_dci_item_status` CHECK ((`item_status` in (_utf8mb4'active',_utf8mb4'excluded',_utf8mb4'archived')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗内容明细：统一组织卷宗详情页展示的目录内容和业务来源';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_dossier_export_file`
--

DROP TABLE IF EXISTS `t1_dossier_export_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_dossier_export_file` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `export_job_id` char(36) NOT NULL,
  `file_name` varchar(500) NOT NULL,
  `file_storage_key` text NOT NULL,
  `file_format` varchar(30) DEFAULT NULL,
  `mime_type` varchar(200) DEFAULT NULL COMMENT 'MIME类型',
  `file_role` varchar(30) NOT NULL DEFAULT 'attachment' COMMENT '文件角色：main_pdf/attachment_zip/snapshot_json/preview/attachment/other',
  `is_primary` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否主文件',
  `file_size` bigint DEFAULT NULL,
  `page_count` int DEFAULT NULL COMMENT 'PDF页数',
  `file_hash` varchar(128) DEFAULT NULL,
  `display_order` int NOT NULL DEFAULT '0' COMMENT '展示排序',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_dossier_export_file_job` (`export_job_id`),
  KEY `idx_dossier_export_file_role` (`file_role`,`is_primary`,`display_order`),
  CONSTRAINT `chk_dossier_export_file_role` CHECK ((`file_role` in (_utf8mb4'main_pdf',_utf8mb4'attachment_zip',_utf8mb4'snapshot_json',_utf8mb4'preview',_utf8mb4'attachment',_utf8mb4'other')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗导出文件：记录导出产物路径、格式、大小和哈希';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_dossier_export_job`
--

DROP TABLE IF EXISTS `t1_dossier_export_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_dossier_export_job` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `dossier_instance_id` char(36) NOT NULL,
  `dossier_version_id` char(36) DEFAULT NULL COMMENT '卷宗版本ID，用于区分同一卷宗不同版本的导出产物',
  `generation_job_id` char(36) DEFAULT NULL COMMENT '关联生成任务ID',
  `export_template_id` char(36) DEFAULT NULL,
  `export_code` varchar(100) DEFAULT NULL,
  `export_status` varchar(24) NOT NULL DEFAULT 'queued',
  `export_scope_json` json NOT NULL DEFAULT (json_object()),
  `export_params_json` json NOT NULL DEFAULT (json_object()),
  `requested_by` varchar(100) DEFAULT NULL,
  `started_at` datetime(6) DEFAULT NULL,
  `finished_at` datetime(6) DEFAULT NULL,
  `error_message` text,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `export_code` (`export_code`),
  KEY `idx_dossier_export_job_instance` (`dossier_instance_id`),
  KEY `idx_dossier_export_job_status` (`export_status`,`created_at`),
  KEY `idx_dossier_export_job_instance_version` (`dossier_instance_id`,`dossier_version_id`,`created_at`),
  KEY `idx_dossier_export_job_generation` (`generation_job_id`),
  KEY `idx_dossier_export_job_status_finished` (`export_status`,`finished_at`),
  CONSTRAINT `dossier_export_job_chk_1` CHECK ((`export_status` in (_utf8mb4'queued',_utf8mb4'running',_utf8mb4'succeeded',_utf8mb4'failed',_utf8mb4'cancelled')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗导出任务：记录导出范围、参数、状态和错误信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_dossier_export_template`
--

DROP TABLE IF EXISTS `t1_dossier_export_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_dossier_export_template` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `template_code` varchar(100) NOT NULL,
  `template_name` varchar(200) NOT NULL,
  `format_type` varchar(20) NOT NULL DEFAULT 'ZIP',
  `include_options_json` json NOT NULL DEFAULT (json_object()),
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `template_code` (`template_code`),
  CONSTRAINT `dossier_export_template_chk_1` CHECK ((`format_type` in (_utf8mb4'ZIP',_utf8mb4'PDF',_utf8mb4'JSON',_utf8mb4'EXCEL',_utf8mb4'HTML',_utf8mb4'OTHER')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗导出模板：仅服务卷宗导出，不做通用报表平台';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_dossier_instance`
--

DROP TABLE IF EXISTS `t1_dossier_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_dossier_instance` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `instance_code` varchar(100) DEFAULT NULL COMMENT '卷宗编号，用于列表展示和业务查询',
  `instance_name` varchar(300) DEFAULT NULL COMMENT '卷宗名称，用于卷宗实例管理列表展示',
  `template_id` char(36) NOT NULL,
  `aircraft_id` char(36) NOT NULL,
  `current_version_id` char(36) DEFAULT NULL,
  `current_version_no` int NOT NULL DEFAULT '1',
  `instance_label` text,
  `status` varchar(24) NOT NULL DEFAULT 'draft',
  `instance_options_json` json NOT NULL DEFAULT (json_object()),
  `created_by` varchar(100) NOT NULL DEFAULT 'system' COMMENT '创建人',
  `updated_by` varchar(100) DEFAULT NULL COMMENT '更新人',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `published_at` datetime(6) DEFAULT NULL COMMENT '发布时间',
  `archived_at` datetime(6) DEFAULT NULL COMMENT '归档时间',
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_dossier_instance_aircraft` (`aircraft_id`),
  KEY `idx_dossier_instance_template` (`template_id`),
  KEY `idx_dossier_instance_current_version` (`current_version_id`),
  KEY `idx_dossier_instance_code` (`instance_code`),
  KEY `idx_dossier_instance_status_time` (`status`,`updated_at`),
  KEY `idx_dossier_instance_aircraft_template` (`aircraft_id`,`template_id`),
  CONSTRAINT `fk_dossier_instance_current_version` FOREIGN KEY (`current_version_id`) REFERENCES `t1_dossier_version` (`id`) ON DELETE SET NULL,
  CONSTRAINT `dossier_instance_chk_1` CHECK ((`status` in (_utf8mb4'draft',_utf8mb4'building',_utf8mb4'ready',_utf8mb4'published',_utf8mb4'archived')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗实例：template_id + aircraft_id；同一实例可产生多个 t1_dossier_version';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_dossier_operation_log`
--

DROP TABLE IF EXISTS `t1_dossier_operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_dossier_operation_log` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '操作日志ID',
  `dossier_instance_id` char(36) NOT NULL COMMENT '卷宗实例ID',
  `dossier_version_id` char(36) DEFAULT NULL COMMENT '卷宗版本ID',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型：create/generate/regenerate/publish/archive/export/update/check等',
  `operation_name` varchar(200) NOT NULL COMMENT '操作名称',
  `operation_status` varchar(24) NOT NULL DEFAULT 'succeeded' COMMENT '操作状态：succeeded/failed/running',
  `business_subject_type` varchar(50) DEFAULT NULL COMMENT '业务对象类型',
  `business_subject_id` char(36) DEFAULT NULL COMMENT '业务对象ID',
  `operator_id` varchar(100) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) DEFAULT NULL COMMENT '操作人名称',
  `source_ip` varchar(64) DEFAULT NULL COMMENT '来源IP',
  `detail_json` json NOT NULL DEFAULT (json_object()) COMMENT '操作详情',
  `result_message` text COMMENT '结果说明',
  `operated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_dol_instance_version` (`dossier_instance_id`,`dossier_version_id`),
  KEY `idx_dol_operation_type` (`operation_type`),
  KEY `idx_dol_subject` (`business_subject_type`,`business_subject_id`),
  KEY `idx_dol_operated_at` (`operated_at`),
  CONSTRAINT `chk_dol_status` CHECK ((`operation_status` in (_utf8mb4'succeeded',_utf8mb4'failed',_utf8mb4'running')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗业务操作日志：记录生成、发布、归档、导出等业务时间线';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_dossier_structure_node`
--

DROP TABLE IF EXISTS `t1_dossier_structure_node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_dossier_structure_node` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `dossier_instance_id` char(36) NOT NULL,
  `dossier_version_id` char(36) NOT NULL,
  `aircraft_id` char(36) DEFAULT NULL COMMENT '??ID????????????BOM',
  `bom_node_id` char(36) DEFAULT NULL COMMENT '??BOM??ID',
  `parent_bom_node_id` char(36) DEFAULT NULL COMMENT '??BOM??ID???????????',
  `bom_node_code` varchar(100) DEFAULT NULL COMMENT 'BOM??????/??',
  `object_level` varchar(30) DEFAULT NULL COMMENT '?????aircraft/system/assembly/component/part/chapter',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '???ID',
  `parent_id` char(36) DEFAULT NULL,
  `node_kind` varchar(24) NOT NULL,
  `code` varchar(128) NOT NULL,
  `name` text NOT NULL,
  `node_path` varchar(1000) DEFAULT NULL COMMENT '章节路径',
  `sort_order` int NOT NULL DEFAULT '0',
  `chapter_status` varchar(24) NOT NULL DEFAULT 'normal' COMMENT '章节状态：normal/disabled/archived',
  `completeness_status` varchar(24) NOT NULL DEFAULT 'not_checked' COMMENT '完整性状态：not_checked/complete/warning/missing/error',
  `content_count` int NOT NULL DEFAULT '0' COMMENT '内容数量',
  `missing_count` int NOT NULL DEFAULT '0' COMMENT '缺失数量',
  `required_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否必需章节',
  `attrs_json` json NOT NULL DEFAULT (json_object()),
  `source_trace_json` json DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_structure_node_child_code` (`dossier_version_id`,`parent_id`,`code`),
  UNIQUE KEY `uq_structure_node_root_code` (`dossier_version_id`,`code`,((case when (`parent_id` is null) then 1 else NULL end))),
  KEY `idx_structure_node_instance` (`dossier_instance_id`),
  KEY `idx_structure_node_version` (`dossier_version_id`),
  KEY `idx_structure_node_completeness` (`dossier_version_id`,`completeness_status`),
  KEY `idx_structure_node_aircraft_parent_bom` (`aircraft_id`,`parent_bom_node_id`,`sort_order`),
  KEY `idx_structure_node_version_bom` (`dossier_version_id`,`bom_node_id`),
  KEY `idx_structure_node_version_object` (`dossier_version_id`,`object_level`,`sort_order`),
  KEY `idx_structure_node_bom_code` (`bom_node_code`),
  KEY `idx_structure_node_path_prefix` (`node_path`(191)),
  CONSTRAINT `chk_dsn_node_kind` CHECK ((`node_kind` in (_utf8mb4'aircraft',_utf8mb4'system',_utf8mb4'subsystem',_utf8mb4'equipment',_utf8mb4'component',_utf8mb4'part',_utf8mb4'chapter')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗实际目录节点：本次卷宗实际生成的目录和构型节点';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_dossier_template`
--

DROP TABLE IF EXISTS `t1_dossier_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_dossier_template` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `template_code` varchar(100) NOT NULL COMMENT '模板编码',
  `template_version` varchar(64) NOT NULL DEFAULT 'V1.0' COMMENT '模板版本',
  `name` text NOT NULL,
  `description` text,
  `template_type` varchar(40) NOT NULL DEFAULT 'general',
  `applicable_object_type` varchar(40) NOT NULL DEFAULT 'aircraft',
  `chapter_tree_json` json NOT NULL DEFAULT (json_object()),
  `validation_rules_json` json NOT NULL DEFAULT (json_object()),
  `default_generator_params_json` json NOT NULL DEFAULT (json_object()),
  `status` varchar(24) NOT NULL DEFAULT 'active',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认模板',
  `effective_from` datetime(6) DEFAULT NULL COMMENT '生效时间',
  `effective_to` datetime(6) DEFAULT NULL COMMENT '失效时间',
  `created_by` varchar(100) NOT NULL DEFAULT 'system' COMMENT '创建人',
  `updated_by` varchar(100) DEFAULT NULL COMMENT '修改人',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dossier_template_code_version` (`template_code`,`template_version`),
  KEY `idx_dossier_template_type_object` (`template_type`,`applicable_object_type`),
  KEY `idx_dossier_template_status_default` (`status`,`is_default`),
  CONSTRAINT `dossier_template_chk_1` CHECK ((`template_type` in (_utf8mb4'general',_utf8mb4'topic2',_utf8mb4'topic3',_utf8mb4'topic4',_utf8mb4'topic5',_utf8mb4'export',_utf8mb4'other'))),
  CONSTRAINT `dossier_template_chk_2` CHECK ((`applicable_object_type` in (_utf8mb4'aircraft',_utf8mb4'system',_utf8mb4'subsystem',_utf8mb4'equipment',_utf8mb4'component',_utf8mb4'part',_utf8mb4'fault',_utf8mb4'other')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗模板：只定义目录、数据清单、校验规则和默认生成参数；不表示卷宗版本';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_dossier_template_chapter`
--

DROP TABLE IF EXISTS `t1_dossier_template_chapter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_dossier_template_chapter` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '模板章节ID',
  `template_id` char(36) NOT NULL COMMENT '模板ID',
  `parent_id` char(36) DEFAULT NULL COMMENT '父章节ID',
  `chapter_code` varchar(100) NOT NULL COMMENT '章节编码',
  `chapter_name` varchar(300) NOT NULL COMMENT '章节名称',
  `chapter_level` int NOT NULL DEFAULT '1' COMMENT '章节层级',
  `chapter_path` varchar(1000) DEFAULT NULL COMMENT '章节路径',
  `node_kind` varchar(30) NOT NULL DEFAULT 'chapter' COMMENT '节点类型：chapter/group/item',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `required_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否必填章节',
  `enabled_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `default_expand` tinyint(1) NOT NULL DEFAULT '0' COMMENT '前端是否默认展开',
  `completeness_requirement` varchar(30) NOT NULL DEFAULT 'normal' COMMENT '完整性要求：normal/strict/optional',
  `chapter_desc` text COMMENT '章节说明',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_by` varchar(100) NOT NULL DEFAULT 'system' COMMENT '创建人',
  `updated_by` varchar(100) DEFAULT NULL COMMENT '修改人',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dtc_template_code` (`template_id`,`chapter_code`),
  KEY `idx_dtc_template_parent` (`template_id`,`parent_id`),
  KEY `idx_dtc_template_sort` (`template_id`,`sort_order`),
  KEY `idx_dtc_enabled` (`enabled_flag`),
  CONSTRAINT `chk_dtc_node_kind` CHECK ((`node_kind` in (_utf8mb4'chapter',_utf8mb4'group',_utf8mb4'item'))),
  CONSTRAINT `chk_dtc_requirement` CHECK ((`completeness_requirement` in (_utf8mb4'normal',_utf8mb4'strict',_utf8mb4'optional')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗模板章节：用于模板目录树配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_dossier_template_data_source`
--

DROP TABLE IF EXISTS `t1_dossier_template_data_source`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_dossier_template_data_source` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '模板数据来源ID',
  `template_id` char(36) NOT NULL COMMENT '模板ID',
  `chapter_id` char(36) NOT NULL COMMENT '模板章节ID',
  `source_code` varchar(100) NOT NULL COMMENT '数据来源编码',
  `source_system` varchar(50) DEFAULT NULL COMMENT '来源系统：PLM/MES/MRO/OPS/MANUAL等',
  `source_table` varchar(200) NOT NULL COMMENT '来源表名',
  `source_name` varchar(300) NOT NULL COMMENT '来源名称',
  `source_desc` text COMMENT '来源说明',
  `lifecycle_stage` varchar(30) DEFAULT NULL COMMENT '生命周期阶段：DESIGN/MANUFACTURING/SERVICE/DOSSIER',
  `source_record_type` varchar(100) DEFAULT NULL COMMENT '来源记录类型',
  `join_condition_json` json NOT NULL DEFAULT (json_object()) COMMENT '关联条件配置',
  `filter_condition_json` json NOT NULL DEFAULT (json_object()) COMMENT '过滤条件配置',
  `apply_object_type` varchar(50) DEFAULT NULL COMMENT '适用对象：aircraft/bom_node/t1_part_instance/key_part等',
  `supply_mode_scope` varchar(30) NOT NULL DEFAULT 'all' COMMENT '供给方式范围：all/self_made/purchased/outsourced',
  `key_part_scope` varchar(30) NOT NULL DEFAULT 'all' COMMENT '关重件范围：all/key_only/non_key_only',
  `required_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否必选来源',
  `enabled_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_by` varchar(100) NOT NULL DEFAULT 'system' COMMENT '创建人',
  `updated_by` varchar(100) DEFAULT NULL COMMENT '修改人',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dtds_chapter_source` (`chapter_id`,`source_code`),
  KEY `idx_dtds_template` (`template_id`),
  KEY `idx_dtds_chapter` (`chapter_id`),
  KEY `idx_dtds_source` (`source_system`,`source_table`),
  KEY `idx_dtds_rules` (`supply_mode_scope`,`key_part_scope`),
  KEY `idx_dtds_enabled` (`enabled_flag`),
  CONSTRAINT `chk_dtds_key_scope` CHECK ((`key_part_scope` in (_utf8mb4'all',_utf8mb4'key_only',_utf8mb4'non_key_only'))),
  CONSTRAINT `chk_dtds_supply_scope` CHECK ((`supply_mode_scope` in (_utf8mb4'all',_utf8mb4'self_made',_utf8mb4'purchased',_utf8mb4'outsourced')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗模板数据来源：配置章节绑定的业务数据表和适用条件';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_dossier_template_param`
--

DROP TABLE IF EXISTS `t1_dossier_template_param`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_dossier_template_param` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '模板参数ID',
  `template_id` char(36) NOT NULL COMMENT '模板ID',
  `chapter_id` char(36) DEFAULT NULL COMMENT '模板章节ID，可为空表示全局参数',
  `param_code` varchar(100) NOT NULL COMMENT '参数编码',
  `param_name` varchar(200) NOT NULL COMMENT '参数名称',
  `param_type` varchar(30) NOT NULL DEFAULT 'string' COMMENT '参数类型：string/number/boolean/json/enum',
  `param_value` text COMMENT '参数值',
  `default_value` text COMMENT '默认值',
  `option_json` json NOT NULL DEFAULT (json_array()) COMMENT '枚举选项',
  `required_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否必填',
  `editable_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT '生成时是否可编辑',
  `enabled_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `param_desc` text COMMENT '参数说明',
  `created_by` varchar(100) NOT NULL DEFAULT 'system' COMMENT '创建人',
  `updated_by` varchar(100) DEFAULT NULL COMMENT '修改人',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dtp_template_param` (`template_id`,`chapter_id`,`param_code`),
  KEY `idx_dtp_template` (`template_id`),
  KEY `idx_dtp_chapter` (`chapter_id`),
  KEY `idx_dtp_enabled` (`enabled_flag`),
  CONSTRAINT `chk_dtp_param_type` CHECK ((`param_type` in (_utf8mb4'string',_utf8mb4'number',_utf8mb4'boolean',_utf8mb4'json',_utf8mb4'enum')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗模板生成参数：配置生成卷宗时使用的默认参数';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_dossier_template_rule`
--

DROP TABLE IF EXISTS `t1_dossier_template_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_dossier_template_rule` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '模板规则ID',
  `template_id` char(36) NOT NULL COMMENT '模板ID',
  `chapter_id` char(36) DEFAULT NULL COMMENT '模板章节ID',
  `rule_code` varchar(100) NOT NULL COMMENT '规则编码',
  `rule_name` varchar(200) NOT NULL COMMENT '规则名称',
  `rule_type` varchar(30) NOT NULL COMMENT '规则类型：required/enum/expression/relation/business',
  `target_table` varchar(200) DEFAULT NULL COMMENT '校验表名',
  `target_field` varchar(200) DEFAULT NULL COMMENT '校验字段',
  `target_path` varchar(500) DEFAULT NULL COMMENT '校验路径',
  `rule_expression` varchar(1000) DEFAULT NULL COMMENT '规则表达式',
  `rule_expression_json` json NOT NULL DEFAULT (json_object()) COMMENT '规则表达式配置',
  `severity` varchar(24) NOT NULL DEFAULT 'warning' COMMENT '严重程度：info/warning/error/blocker',
  `error_message` varchar(500) DEFAULT NULL COMMENT '错误提示',
  `remediation_hint` text COMMENT '整改建议',
  `bind_quality_rule_id` char(36) DEFAULT NULL COMMENT '关联通用质量规则ID',
  `apply_object_type` varchar(50) DEFAULT NULL COMMENT '适用对象类型',
  `supply_mode_scope` varchar(30) NOT NULL DEFAULT 'all' COMMENT '供给方式范围：all/self_made/purchased/outsourced',
  `key_part_scope` varchar(30) NOT NULL DEFAULT 'all' COMMENT '关重件范围：all/key_only/non_key_only',
  `required_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否必须通过',
  `enabled_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `created_by` varchar(100) NOT NULL DEFAULT 'system' COMMENT '创建人',
  `updated_by` varchar(100) DEFAULT NULL COMMENT '修改人',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dtr_template_rule` (`template_id`,`rule_code`),
  KEY `idx_dtr_template` (`template_id`),
  KEY `idx_dtr_chapter` (`chapter_id`),
  KEY `idx_dtr_target` (`target_table`,`target_field`),
  KEY `idx_dtr_quality_rule` (`bind_quality_rule_id`),
  KEY `idx_dtr_enabled` (`enabled_flag`),
  CONSTRAINT `chk_dtr_key_scope` CHECK ((`key_part_scope` in (_utf8mb4'all',_utf8mb4'key_only',_utf8mb4'non_key_only'))),
  CONSTRAINT `chk_dtr_rule_type` CHECK ((`rule_type` in (_utf8mb4'required',_utf8mb4'enum',_utf8mb4'expression',_utf8mb4'relation',_utf8mb4'business'))),
  CONSTRAINT `chk_dtr_severity` CHECK ((`severity` in (_utf8mb4'info',_utf8mb4'warning',_utf8mb4'error',_utf8mb4'blocker'))),
  CONSTRAINT `chk_dtr_supply_scope` CHECK ((`supply_mode_scope` in (_utf8mb4'all',_utf8mb4'self_made',_utf8mb4'purchased',_utf8mb4'outsourced')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗模板校验规则：配置模板和章节的生成前检查规则';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_dossier_version`
--

DROP TABLE IF EXISTS `t1_dossier_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_dossier_version` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `dossier_instance_id` char(36) NOT NULL,
  `template_id` char(36) DEFAULT NULL COMMENT '????????ID',
  `template_code` varchar(100) DEFAULT NULL COMMENT '??????????',
  `template_version` varchar(64) DEFAULT NULL COMMENT '??????????',
  `template_snapshot_json` json NOT NULL DEFAULT (json_object()) COMMENT '?????????',
  `version_no` int NOT NULL,
  `version_label` varchar(64) NOT NULL,
  `major_version_no` int NOT NULL DEFAULT '1' COMMENT '??????',
  `minor_version_no` int NOT NULL DEFAULT '0' COMMENT '??????',
  `version_level` varchar(16) NOT NULL DEFAULT 'minor' COMMENT '?????major/minor/draft',
  `previous_version_id` char(36) DEFAULT NULL,
  `generation_job_id` char(36) DEFAULT NULL COMMENT '????ID',
  `data_snapshot_id` char(36) DEFAULT NULL COMMENT '????ID',
  `version_reason` varchar(40) NOT NULL DEFAULT 'initial',
  `is_current` tinyint(1) NOT NULL DEFAULT '1',
  `change_summary` text,
  `content_hash` varchar(128) DEFAULT NULL,
  `content_summary_json` json NOT NULL DEFAULT (json_object()),
  `generation_params_json` json NOT NULL DEFAULT (json_object()) COMMENT '??????',
  `created_by` varchar(100) NOT NULL DEFAULT 'system',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `published_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dossier_instance_id` (`dossier_instance_id`,`version_no`),
  UNIQUE KEY `dossier_instance_id_2` (`dossier_instance_id`,`version_label`),
  KEY `idx_dossier_version_instance` (`dossier_instance_id`,`version_no`),
  KEY `idx_dossier_version_current` (`dossier_instance_id`,`is_current`),
  KEY `idx_dossier_version_template_trace` (`template_id`,`template_version`),
  KEY `idx_dossier_version_level` (`dossier_instance_id`,`major_version_no`,`minor_version_no`,`version_level`),
  KEY `idx_dossier_version_generation_job` (`generation_job_id`),
  KEY `idx_dossier_version_snapshot` (`data_snapshot_id`),
  KEY `idx_dossier_version_created` (`created_at`),
  CONSTRAINT `fk_dossier_version_data_snapshot` FOREIGN KEY (`data_snapshot_id`) REFERENCES `t1_data_snapshot` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_dossier_version_generation_job` FOREIGN KEY (`generation_job_id`) REFERENCES `t1_generation_job` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_dossier_version_template` FOREIGN KEY (`template_id`) REFERENCES `t1_dossier_template` (`id`) ON DELETE SET NULL,
  CONSTRAINT `chk_dossier_version_level` CHECK ((`version_level` in (_utf8mb4'major',_utf8mb4'minor',_utf8mb4'draft'))),
  CONSTRAINT `dossier_version_chk_1` CHECK ((`version_reason` in (_utf8mb4'initial',_utf8mb4'data_update',_utf8mb4'manual_completion',_utf8mb4'topic_feedback',_utf8mb4'regeneration',_utf8mb4'publish',_utf8mb4'other')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗版本：卷宗内容版本。数据更新、人工补全、课题成果回写或重新生成时新增版本';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_dossier_version_diff`
--

DROP TABLE IF EXISTS `t1_dossier_version_diff`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_dossier_version_diff` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '版本差异ID',
  `dossier_instance_id` char(36) NOT NULL COMMENT '卷宗实例ID',
  `base_version_id` char(36) NOT NULL COMMENT '基准版本ID',
  `compare_version_id` char(36) NOT NULL COMMENT '对比版本ID',
  `diff_type` varchar(30) NOT NULL COMMENT '差异类型：added/removed/modified/status_changed/source_changed',
  `object_type` varchar(50) NOT NULL COMMENT '对象类型',
  `object_id` char(36) DEFAULT NULL COMMENT '对象ID',
  `object_key` varchar(500) DEFAULT NULL COMMENT '对象业务键',
  `structure_node_id` char(36) DEFAULT NULL COMMENT '目录节点ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `field_name` varchar(200) DEFAULT NULL COMMENT '变化字段',
  `old_value_json` json DEFAULT NULL COMMENT '旧值',
  `new_value_json` json DEFAULT NULL COMMENT '新值',
  `impact_level` varchar(24) NOT NULL DEFAULT 'normal' COMMENT '影响级别：low/normal/high/critical',
  `impact_summary` text COMMENT '影响说明',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_dvd_instance` (`dossier_instance_id`),
  KEY `idx_dvd_versions` (`base_version_id`,`compare_version_id`),
  KEY `idx_dvd_type` (`diff_type`),
  KEY `idx_dvd_object` (`object_type`,`object_id`),
  KEY `idx_dvd_structure_node` (`structure_node_id`),
  KEY `idx_dvd_bom_node` (`bom_node_id`),
  CONSTRAINT `chk_dvd_diff_type` CHECK ((`diff_type` in (_utf8mb4'added',_utf8mb4'removed',_utf8mb4'modified',_utf8mb4'status_changed',_utf8mb4'source_changed'))),
  CONSTRAINT `chk_dvd_impact_level` CHECK ((`impact_level` in (_utf8mb4'low',_utf8mb4'normal',_utf8mb4'high',_utf8mb4'critical')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗版本差异：记录两个版本之间的内容变化';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_engineering_change_execution`
--

DROP TABLE IF EXISTS `t1_engineering_change_execution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_engineering_change_execution` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '执行记录ID',
  `execution_code` varchar(100) NOT NULL COMMENT '执行记录编码',
  `change_type` varchar(30) NOT NULL COMMENT '更改类型：ecr/eco/sb/deviation',
  `change_ref_id` char(36) DEFAULT NULL COMMENT '更改来源ID',
  `change_code` varchar(100) NOT NULL COMMENT '更改编号',
  `title` varchar(300) NOT NULL COMMENT '标题',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '零件实例ID',
  `execution_status` varchar(24) NOT NULL DEFAULT 'planned' COMMENT '执行状态：planned/running/completed/verified/cancelled',
  `planned_start_at` datetime(6) DEFAULT NULL COMMENT '计划开始时间',
  `planned_finish_at` datetime(6) DEFAULT NULL COMMENT '计划完成时间',
  `actual_start_at` datetime(6) DEFAULT NULL COMMENT '实际开始时间',
  `actual_finish_at` datetime(6) DEFAULT NULL COMMENT '实际完成时间',
  `responsible_person_id` char(36) DEFAULT NULL COMMENT '责任人ID',
  `responsible_org_id` char(36) DEFAULT NULL COMMENT '责任组织ID',
  `verified_by` varchar(100) DEFAULT NULL COMMENT '确认人',
  `verified_at` datetime(6) DEFAULT NULL COMMENT '确认时间',
  `before_snapshot_id` char(36) DEFAULT NULL COMMENT '执行前快照ID',
  `after_snapshot_id` char(36) DEFAULT NULL COMMENT '执行后快照ID',
  `result_summary` text COMMENT '执行结果说明',
  `evidence_json` json NOT NULL DEFAULT (json_array()) COMMENT '执行证据',
  `created_by` varchar(100) NOT NULL DEFAULT 'system' COMMENT '创建人',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_engineering_change_execution_code` (`execution_code`),
  KEY `idx_ece_change` (`change_type`,`change_code`),
  KEY `idx_ece_aircraft` (`aircraft_id`),
  KEY `idx_ece_bom_node` (`bom_node_id`),
  KEY `idx_ece_part_instance` (`part_instance_id`),
  KEY `idx_ece_status` (`execution_status`),
  CONSTRAINT `chk_ece_change_type` CHECK ((`change_type` in (_utf8mb4'ecr',_utf8mb4'eco',_utf8mb4'sb',_utf8mb4'deviation'))),
  CONSTRAINT `chk_ece_status` CHECK ((`execution_status` in (_utf8mb4'planned',_utf8mb4'running',_utf8mb4'completed',_utf8mb4'verified',_utf8mb4'cancelled')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工程更改执行记录：记录更改落实到单机、BOM节点或零件实例的执行情况';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_equipment_object_instance`
--

DROP TABLE IF EXISTS `t1_equipment_object_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_equipment_object_instance` (
  `equipment_instance_id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '设备实物实例ID',
  `equipment_master_id` char(36) DEFAULT NULL COMMENT '设备主数据ID',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '统一对象ID',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '所属飞机ID',
  `system_id` char(36) DEFAULT NULL COMMENT '所属系统ID',
  `subsystem_id` char(36) DEFAULT NULL COMMENT '所属子系统ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '关联零件/设备实物ID',
  `equipment_code` varchar(100) NOT NULL COMMENT '设备编码',
  `equipment_name` varchar(300) DEFAULT NULL COMMENT '设备名称',
  `part_number` varchar(200) DEFAULT NULL COMMENT '件号/型号',
  `serial_number` varchar(200) DEFAULT NULL COMMENT '序列号',
  `batch_number` varchar(200) DEFAULT NULL COMMENT '批次号',
  `software_version` varchar(100) DEFAULT NULL COMMENT '软件版本',
  `hardware_version` varchar(100) DEFAULT NULL COMMENT '硬件版本',
  `manufacturer` varchar(200) DEFAULT NULL COMMENT '制造商',
  `supplier_name` varchar(200) DEFAULT NULL COMMENT '供应商',
  `supplier_code` varchar(100) DEFAULT NULL COMMENT '供应商代码',
  `manufacture_date` date DEFAULT NULL COMMENT '制造日期',
  `delivery_batch` varchar(100) DEFAULT NULL COMMENT '交付批次',
  `installation_position` varchar(200) DEFAULT NULL COMMENT '装机位置',
  `position_code` varchar(100) DEFAULT NULL COMMENT '位号/站位',
  `installation_date` date DEFAULT NULL COMMENT '装机日期',
  `installation_status` varchar(50) DEFAULT NULL COMMENT '装机状态',
  `configuration_version` varchar(100) DEFAULT NULL COMMENT '构型版本',
  `modification_status` varchar(50) DEFAULT NULL COMMENT '改装状态',
  `effectivity` json NOT NULL DEFAULT (json_object()) COMMENT '适用性',
  `tsn` decimal(12,2) DEFAULT NULL COMMENT '自新品以来时间',
  `csn` int DEFAULT NULL COMMENT '自新品以来循环',
  `tso` decimal(12,2) DEFAULT NULL COMMENT '自上次翻修以来时间',
  `cso` int DEFAULT NULL COMMENT '自上次翻修以来循环',
  `operational_status` varchar(50) DEFAULT NULL COMMENT '运行状态',
  `quality_status` varchar(50) DEFAULT NULL COMMENT '质量状态',
  `airworthiness_release_status` varchar(50) DEFAULT NULL COMMENT '适航放行状态',
  `trace_code` varchar(200) DEFAULT NULL COMMENT '追溯码',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`equipment_instance_id`),
  UNIQUE KEY `uk_equipment_instance_bom_node` (`bom_node_id`),
  KEY `idx_equipment_instance_master` (`equipment_master_id`),
  KEY `idx_equipment_instance_aircraft` (`aircraft_id`),
  KEY `idx_equipment_instance_part` (`part_number`,`serial_number`),
  KEY `idx_equipment_instance_status` (`installation_status`,`operational_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备层实物实例表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_equipment_object_master`
--

DROP TABLE IF EXISTS `t1_equipment_object_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_equipment_object_master` (
  `equipment_master_id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '设备主数据ID',
  `equipment_code` varchar(100) NOT NULL COMMENT '设备编码',
  `equipment_name` varchar(300) NOT NULL COMMENT '设备名称',
  `equipment_type` varchar(100) DEFAULT NULL COMMENT '设备类型',
  `part_number` varchar(200) DEFAULT NULL COMMENT '设备件号/型号',
  `model_spec` varchar(200) DEFAULT NULL COMMENT '规格型号',
  `manufacturer` varchar(200) DEFAULT NULL COMMENT '制造商',
  `supplier_name` varchar(200) DEFAULT NULL COMMENT '供应商',
  `supplier_code` varchar(100) DEFAULT NULL COMMENT '供应商代码',
  `configuration_item_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否构型项',
  `maintainability_type` varchar(50) DEFAULT NULL COMMENT '维修属性',
  `life_limit` varchar(200) DEFAULT NULL COMMENT '寿命限制摘要',
  `tbo` varchar(200) DEFAULT NULL COMMENT '翻修间隔摘要',
  `criticality_level` varchar(50) DEFAULT NULL COMMENT '关键等级',
  `failure_effect_summary` text COMMENT '失效影响摘要',
  `certificate_doc_id` char(36) DEFAULT NULL COMMENT '合格/放行证明文件ID',
  `test_report_doc_id` char(36) DEFAULT NULL COMMENT '测试报告文件ID',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`equipment_master_id`),
  UNIQUE KEY `uk_equipment_master_code` (`equipment_code`),
  KEY `idx_equipment_master_part` (`part_number`),
  KEY `idx_equipment_master_type` (`equipment_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备层主数据表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_equipment_operation_log`
--

DROP TABLE IF EXISTS `t1_equipment_operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_equipment_operation_log` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `equipment_id` varchar(50) NOT NULL,
  `operator_id` varchar(50) NOT NULL,
  `operation_time` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `operation_type` varchar(20) DEFAULT NULL,
  `shop_order_task_id` char(36) DEFAULT NULL,
  `parameters` json NOT NULL DEFAULT (json_object()),
  `remarks` text,
  PRIMARY KEY (`id`),
  KEY `idx_equipment_op_log_equipment` (`equipment_id`),
  KEY `idx_equipment_op_log_time` (`operation_time`),
  KEY `idx_equipment_op_log_task` (`shop_order_task_id`),
  CONSTRAINT `equipment_operation_log_chk_1` CHECK (((`operation_type` is null) or (`operation_type` in (_utf8mb4'START',_utf8mb4'STOP',_utf8mb4'MAINTENANCE',_utf8mb4'CALIBRATION'))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备使用/维护操作日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_event_eco`
--

DROP TABLE IF EXISTS `t1_event_eco`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_event_eco` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `eco_number` varchar(200) NOT NULL,
  `ecr_id` char(36) DEFAULT NULL,
  `title` varchar(500) NOT NULL,
  `change_description` text NOT NULL,
  `effectivity` json NOT NULL DEFAULT (json_object()),
  `applicable_aircraft_ids` json DEFAULT NULL,
  `workflow_status` varchar(50) DEFAULT NULL,
  `released_baseline_id` char(36) DEFAULT NULL,
  `approved_by` varchar(100) DEFAULT NULL,
  `approved_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(100) NOT NULL DEFAULT 'system',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `eco_number` (`eco_number`),
  KEY `idx_event_eco_ecr` (`ecr_id`),
  CONSTRAINT `event_eco_chk_1` CHECK ((`workflow_status` in (_utf8mb4'DRAFT',_utf8mb4'APPROVED',_utf8mb4'IN_IMPLEMENTATION',_utf8mb4'PARTIALLY_IMPLEMENTED',_utf8mb4'COMPLETED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_event_ecr`
--

DROP TABLE IF EXISTS `t1_event_ecr`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_event_ecr` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `ecr_number` varchar(200) NOT NULL,
  `title` varchar(500) NOT NULL,
  `description` text,
  `workflow_status` varchar(50) DEFAULT NULL,
  `affected_aircraft_ids` json DEFAULT NULL,
  `target_baseline_id` char(36) DEFAULT NULL,
  `created_by` varchar(100) NOT NULL DEFAULT 'system',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `ecr_number` (`ecr_number`),
  CONSTRAINT `event_ecr_chk_1` CHECK ((`workflow_status` in (_utf8mb4'DRAFT',_utf8mb4'SUBMITTED',_utf8mb4'UNDER_REVIEW',_utf8mb4'APPROVED',_utf8mb4'REJECTED',_utf8mb4'WITHDRAWN',_utf8mb4'IN_IMPLEMENTATION',_utf8mb4'CLOSED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_event_flight_leg`
--

DROP TABLE IF EXISTS `t1_event_flight_leg`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_event_flight_leg` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `aircraft_id` char(36) NOT NULL,
  `leg_number` varchar(50) NOT NULL,
  `departure_airport` varchar(10) NOT NULL,
  `arrival_airport` varchar(10) NOT NULL,
  `takeoff_time` datetime(6) DEFAULT NULL,
  `landing_time` datetime(6) DEFAULT NULL,
  `fh_this_leg` decimal(8,3) DEFAULT NULL,
  `fc_this_leg` int DEFAULT '1',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_flight_leg` (`aircraft_id`,`leg_number`),
  KEY `idx_flight_leg_aircraft` (`aircraft_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_fault_action`
--

DROP TABLE IF EXISTS `t1_fault_action`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_fault_action` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `fault_event_id` char(36) NOT NULL,
  `aircraft_id` char(36) DEFAULT NULL COMMENT '对象锚点：飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT '对象锚点：BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '对象锚点：实物实例ID',
  `object_level` varchar(30) DEFAULT NULL COMMENT '对象锚点：层级',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '对象锚点：统一对象ID',
  `action_type` varchar(20) NOT NULL,
  `work_order_task_id` char(36) DEFAULT NULL,
  `install_removal_id` char(36) DEFAULT NULL,
  `action_notes` text,
  `performed_by` varchar(100) DEFAULT NULL,
  `performed_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_fault_action_event` (`fault_event_id`),
  KEY `idx_fault_action_wot` (`work_order_task_id`),
  KEY `idx_fault_action_ir` (`install_removal_id`),
  KEY `idx_fa_bom_anchor` (`aircraft_id`,`bom_node_id`),
  KEY `idx_fa_part_anchor` (`part_instance_id`),
  CONSTRAINT `fault_action_chk_1` CHECK ((`action_type` in (_utf8mb4'TROUBLESHOOTING',_utf8mb4'CORRECTIVE_REPAIR',_utf8mb4'PART_REMOVAL',_utf8mb4'TEST',_utf8mb4'DEFERRAL')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='故障处置动作：串联 t1_fault_event 与工单任务、装拆记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_fault_code_dict`
--

DROP TABLE IF EXISTS `t1_fault_code_dict`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_fault_code_dict` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `ata_chapter` varchar(10) DEFAULT NULL,
  `fault_code` varchar(50) NOT NULL,
  `fault_name` varchar(200) DEFAULT NULL,
  `fault_description` text,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_fault_code_dict_code` (`fault_code`),
  KEY `idx_fault_code_dict_ata` (`ata_chapter`),
  KEY `idx_fault_code_dict_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='故障代码字典（参照 ATA Spec 100 / S1000D）；fault_code 全局唯一';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_fault_event`
--

DROP TABLE IF EXISTS `t1_fault_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_fault_event` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `aircraft_id` char(36) NOT NULL,
  `bom_node_id` char(36) DEFAULT NULL COMMENT '对象锚点：BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '对象锚点：实物实例ID',
  `object_level` varchar(30) DEFAULT NULL COMMENT '对象锚点：层级',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '对象锚点：统一对象ID',
  `node_id` char(36) DEFAULT NULL,
  `instance_id` char(36) DEFAULT NULL,
  `reported_at` datetime(6) NOT NULL,
  `flight_leg_id` char(36) DEFAULT NULL,
  `fault_code_id` char(36) DEFAULT NULL,
  `fault_description` text,
  `severity` varchar(20) DEFAULT NULL,
  `fault_source` varchar(20) NOT NULL,
  `status` varchar(20) NOT NULL,
  `resolution_type` varchar(30) DEFAULT NULL,
  `deferral_ref` varchar(100) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_fault_event_aircraft` (`aircraft_id`),
  KEY `idx_fault_event_node` (`node_id`),
  KEY `idx_fault_event_instance` (`instance_id`),
  KEY `idx_fault_event_code` (`fault_code_id`),
  KEY `idx_fault_event_reported` (`aircraft_id`,`reported_at` DESC),
  KEY `idx_fault_event_status` (`status`),
  KEY `idx_fe_bom_anchor` (`aircraft_id`,`bom_node_id`),
  KEY `idx_fe_part_anchor` (`part_instance_id`),
  CONSTRAINT `fault_event_chk_1` CHECK (((`severity` is null) or (`severity` in (_utf8mb4'MINOR',_utf8mb4'MAJOR',_utf8mb4'CRITICAL',_utf8mb4'CATASTROPHIC')))),
  CONSTRAINT `fault_event_chk_2` CHECK ((`fault_source` in (_utf8mb4'PILOT_REPORT',_utf8mb4'MONITORING_ALERT',_utf8mb4'INSPECTION_FINDING'))),
  CONSTRAINT `fault_event_chk_3` CHECK ((`status` in (_utf8mb4'OPEN',_utf8mb4'UNDER_INVESTIGATION',_utf8mb4'RESOLVED',_utf8mb4'DEFERRED'))),
  CONSTRAINT `fault_event_chk_4` CHECK (((`resolution_type` is null) or (`resolution_type` in (_utf8mb4'RESET',_utf8mb4'ADJUSTMENT',_utf8mb4'PART_REPLACED',_utf8mb4'SOFTWARE_UPDATE',_utf8mb4'NO_FAULT_FOUND'))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='运营/维修故障事件主表；与航段、BOM 节点、件实例及标准故障码关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_generation_job`
--

DROP TABLE IF EXISTS `t1_generation_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_generation_job` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `job_code` varchar(100) DEFAULT NULL COMMENT '??????',
  `job_type` varchar(30) NOT NULL DEFAULT 'generate' COMMENT '?????generate/regenerate/precheck/other',
  `dossier_instance_id` char(36) NOT NULL,
  `dossier_version_id` char(36) DEFAULT NULL,
  `precheck_run_id` char(36) DEFAULT NULL COMMENT '???????ID',
  `job_status` varchar(24) NOT NULL DEFAULT 'queued',
  `current_stage` varchar(50) DEFAULT NULL COMMENT '????',
  `progress_percent` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '???????',
  `pull_strategy_json` json NOT NULL DEFAULT (json_object()),
  `generator_params_json` json NOT NULL DEFAULT (json_object()),
  `algorithm_code` varchar(64) DEFAULT NULL,
  `algorithm_version` varchar(32) DEFAULT NULL,
  `source_system` varchar(50) DEFAULT NULL,
  `requested_by` varchar(100) DEFAULT NULL COMMENT '???',
  `started_at` datetime(6) DEFAULT NULL,
  `finished_at` datetime(6) DEFAULT NULL,
  `error_message` text,
  `result_summary_json` json NOT NULL DEFAULT (json_object()) COMMENT '??????',
  `output_json` json NOT NULL DEFAULT (json_object()) COMMENT '?????????',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_generation_job_code` (`job_code`),
  KEY `idx_generation_job_instance` (`dossier_instance_id`),
  KEY `idx_generation_job_type_status` (`job_type`,`job_status`),
  KEY `idx_generation_job_precheck` (`precheck_run_id`),
  KEY `idx_generation_job_instance_finished` (`dossier_instance_id`,`finished_at`),
  KEY `idx_generation_job_version` (`dossier_version_id`),
  CONSTRAINT `fk_generation_job_precheck` FOREIGN KEY (`precheck_run_id`) REFERENCES `t1_data_inspection_run` (`id`) ON DELETE SET NULL,
  CONSTRAINT `chk_generation_job_progress` CHECK (((`progress_percent` >= 0) and (`progress_percent` <= 100))),
  CONSTRAINT `chk_generation_job_type` CHECK ((`job_type` in (_utf8mb4'generate',_utf8mb4'regenerate',_utf8mb4'precheck',_utf8mb4'other'))),
  CONSTRAINT `generation_job_chk_1` CHECK ((`job_status` in (_utf8mb4'queued',_utf8mb4'running',_utf8mb4'succeeded',_utf8mb4'failed',_utf8mb4'cancelled')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_generation_job_item`
--

DROP TABLE IF EXISTS `t1_generation_job_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_generation_job_item` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '生成明细ID',
  `generation_job_id` char(36) NOT NULL COMMENT '生成任务ID',
  `item_order` int NOT NULL DEFAULT '0' COMMENT '明细顺序',
  `source_domain` varchar(50) DEFAULT NULL COMMENT '数据阶段：DESIGN/MANUFACTURING/SERVICE',
  `source_system` varchar(50) DEFAULT NULL COMMENT '来源系统',
  `source_table` varchar(200) DEFAULT NULL COMMENT '来源表',
  `source_record_id` char(36) DEFAULT NULL COMMENT '来源记录ID',
  `source_record_key` varchar(500) DEFAULT NULL COMMENT '来源业务键',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '零件实例ID',
  `is_key_part` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否关重件',
  `supply_mode` varchar(30) DEFAULT NULL COMMENT '供给方式：self_made/purchased/outsourced/unknown',
  `include_design_data` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否纳入设计数据',
  `include_manufacturing_data` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否纳入制造数据',
  `include_service_data` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否纳入服役数据',
  `include_source_proof` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否纳入来源证明',
  `completeness_status` varchar(24) NOT NULL DEFAULT 'not_checked' COMMENT '完整性状态：not_checked/complete/warning/missing/error',
  `target_node_id` char(36) DEFAULT NULL COMMENT '目标目录节点ID',
  `target_document_entry_id` char(36) DEFAULT NULL COMMENT '目标文档条目ID',
  `action_type` varchar(30) NOT NULL DEFAULT 'collect' COMMENT '动作类型：collect/generate/link/skip',
  `item_status` varchar(24) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/running/succeeded/failed/skipped',
  `error_message` text COMMENT '错误信息',
  `source_trace_json` json NOT NULL DEFAULT (json_object()) COMMENT '来源追踪',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_generation_job_item_job` (`generation_job_id`,`item_order`),
  KEY `idx_generation_job_item_source` (`source_table`,`source_record_id`),
  KEY `idx_generation_job_item_target_node` (`target_node_id`),
  KEY `idx_generation_job_item_status` (`item_status`),
  KEY `idx_generation_job_item_bom` (`bom_node_id`),
  KEY `idx_generation_job_item_part` (`part_instance_id`),
  KEY `idx_generation_job_item_rules` (`is_key_part`,`supply_mode`),
  KEY `idx_generation_job_item_completeness` (`completeness_status`),
  CONSTRAINT `fk_generation_job_item_job` FOREIGN KEY (`generation_job_id`) REFERENCES `t1_generation_job` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_generation_job_item_action` CHECK ((`action_type` in (_utf8mb4'collect',_utf8mb4'generate',_utf8mb4'link',_utf8mb4'skip'))),
  CONSTRAINT `chk_generation_job_item_status` CHECK ((`item_status` in (_utf8mb4'pending',_utf8mb4'running',_utf8mb4'succeeded',_utf8mb4'failed',_utf8mb4'skipped')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗生成明细：记录每次生成时抽取和生成的具体数据';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_impact_tube_boundary_condition`
--

DROP TABLE IF EXISTS `t1_impact_tube_boundary_condition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_impact_tube_boundary_condition` (
  `model_part_number` varchar(200) NOT NULL,
  `vibration_profile_json` json NOT NULL DEFAULT (json_array()),
  `inlet_velocity_json` json NOT NULL DEFAULT (json_array()),
  `inlet_pressure_json` json NOT NULL DEFAULT (json_array()),
  `valve_close_time_s` decimal(10,4) DEFAULT NULL,
  `actuator_stall_time_s` decimal(10,4) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`model_part_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='边界条件与载荷：振动谱、入口时程（JSONB）、阀门/作动器时间';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_impact_tube_fluid`
--

DROP TABLE IF EXISTS `t1_impact_tube_fluid`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_impact_tube_fluid` (
  `model_part_number` varchar(200) NOT NULL,
  `fluid_name` varchar(200) DEFAULT NULL,
  `density_kgm3` decimal(10,2) DEFAULT NULL,
  `bulk_modulus_mpa` decimal(10,2) DEFAULT NULL,
  `kinematic_viscosity_cst` decimal(10,4) DEFAULT NULL,
  `reference_temperature_c` decimal(10,2) DEFAULT NULL,
  `vapor_pressure_pa` decimal(10,2) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`model_part_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='液压油参数；与冲击模型 1:1';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_impact_tube_segment`
--

DROP TABLE IF EXISTS `t1_impact_tube_segment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_impact_tube_segment` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `model_part_number` varchar(200) NOT NULL,
  `segment_order` int NOT NULL,
  `segment_type` varchar(20) NOT NULL,
  `straight_length_mm` decimal(10,3) DEFAULT NULL,
  `bend_radius_mm` decimal(10,3) DEFAULT NULL,
  `bend_angle_deg` decimal(5,2) DEFAULT NULL,
  `bend_direction` varchar(50) DEFAULT NULL,
  `local_outer_diameter_mm` decimal(10,3) DEFAULT NULL,
  `local_wall_thickness_mm` decimal(10,3) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_tube_segment_model` (`model_part_number`,`segment_order`),
  CONSTRAINT `impact_tube_segment_chk_1` CHECK ((`segment_type` in (_utf8mb4'STRAIGHT',_utf8mb4'BEND')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管路分段几何（直管/弯管），按 segment_order 顺序排列';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_impact_tube_support`
--

DROP TABLE IF EXISTS `t1_impact_tube_support`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_impact_tube_support` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `model_part_number` varchar(200) NOT NULL,
  `support_order` int NOT NULL,
  `position_mm` decimal(10,3) DEFAULT NULL,
  `clamp_type` varchar(100) DEFAULT NULL,
  `material_spec` varchar(200) DEFAULT NULL,
  `density_kgm3` decimal(10,2) DEFAULT NULL,
  `poisson_ratio` decimal(5,4) DEFAULT NULL,
  `youngs_modulus_gpa` decimal(10,2) DEFAULT NULL,
  `yield_strength_mpa` decimal(10,2) DEFAULT NULL,
  `preload_n` decimal(10,2) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_tube_support_model` (`model_part_number`,`support_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管路上卡箍/支撑位置与材料特性，按 support_order 排列';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_inspection_measurement`
--

DROP TABLE IF EXISTS `t1_inspection_measurement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_inspection_measurement` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `inspection_record_id` char(36) NOT NULL,
  `aircraft_id` char(36) DEFAULT NULL COMMENT '对象锚点：飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT '对象锚点：BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '对象锚点：实物实例ID',
  `object_level` varchar(30) DEFAULT NULL COMMENT '对象锚点：层级',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '对象锚点：统一对象ID',
  `indicator_code` varchar(100) NOT NULL,
  `indicator_name` varchar(200) DEFAULT NULL,
  `nominal_value` decimal(10,0) DEFAULT NULL,
  `upper_limit` decimal(10,0) DEFAULT NULL,
  `lower_limit` decimal(10,0) DEFAULT NULL,
  `measured_value` decimal(10,0) DEFAULT NULL,
  `unit` varchar(20) DEFAULT NULL,
  `result_flag` varchar(10) DEFAULT NULL,
  `defect_code` varchar(50) DEFAULT NULL,
  `defect_level` varchar(20) DEFAULT NULL,
  `remark` text,
  PRIMARY KEY (`id`),
  KEY `idx_insp_measurement_record` (`inspection_record_id`),
  KEY `idx_insp_measurement_indicator` (`indicator_code`),
  KEY `idx_insp_measurement_result_flag` (`result_flag`),
  KEY `idx_im_bom_anchor` (`aircraft_id`,`bom_node_id`),
  KEY `idx_im_part_anchor` (`part_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检验记录分项测量：指标编码/名义值/公差/实测与 PASS/FAIL/WARNING 等';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_inspection_plan`
--

DROP TABLE IF EXISTS `t1_inspection_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_inspection_plan` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '检验计划ID',
  `plan_code` varchar(100) NOT NULL COMMENT '计划编码',
  `plan_name` varchar(300) NOT NULL COMMENT '计划名称',
  `object_level` varchar(30) DEFAULT NULL COMMENT '适用对象层级',
  `object_id` char(36) DEFAULT NULL COMMENT '适用对象ID',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `part_number` varchar(200) DEFAULT NULL COMMENT '件号',
  `lifecycle_stage` varchar(30) DEFAULT NULL COMMENT '适用阶段',
  `inspection_stage` varchar(50) DEFAULT NULL COMMENT '检验阶段：incoming/first_article/in_process/final/functional/delivery/service',
  `inspection_type` varchar(50) DEFAULT NULL COMMENT '检验类型',
  `sampling_plan` varchar(200) DEFAULT NULL COMMENT '抽样方案',
  `required_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否必检',
  `plan_status` varchar(50) NOT NULL DEFAULT 'active' COMMENT '计划状态',
  `effective_from` date DEFAULT NULL COMMENT '生效日期',
  `effective_to` date DEFAULT NULL COMMENT '失效日期',
  `owner_org_id` char(36) DEFAULT NULL COMMENT '责任单位',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inspection_plan_code` (`plan_code`),
  KEY `idx_inspection_plan_object` (`object_level`,`object_id`),
  KEY `idx_inspection_plan_bom` (`bom_node_id`),
  KEY `idx_inspection_plan_stage` (`lifecycle_stage`,`inspection_stage`),
  CONSTRAINT `chk_inspection_plan_status` CHECK ((`plan_status` in (_utf8mb4'active',_utf8mb4'inactive',_utf8mb4'archived')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检验/质量计划表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_inspection_record`
--

DROP TABLE IF EXISTS `t1_inspection_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_inspection_record` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `shop_order_task_id` char(36) NOT NULL,
  `aircraft_id` char(36) DEFAULT NULL COMMENT '对象锚点：飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT '对象锚点：BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '对象锚点：实物实例ID',
  `object_level` varchar(30) DEFAULT NULL COMMENT '对象锚点：层级',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '对象锚点：统一对象ID',
  `work_step_detail_id` char(36) DEFAULT NULL,
  `inspection_type` varchar(20) NOT NULL,
  `inspection_std_doc` varchar(200) DEFAULT NULL,
  `measurement_values` json NOT NULL DEFAULT (json_object()),
  `result` varchar(20) NOT NULL,
  `inspector_id` varchar(50) DEFAULT NULL,
  `inspection_date` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `concession_number` varchar(100) DEFAULT NULL,
  `remarks` text,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_insp_record_task` (`shop_order_task_id`),
  KEY `idx_insp_record_work_step_detail` (`work_step_detail_id`),
  KEY `idx_ir_bom_anchor` (`aircraft_id`,`bom_node_id`),
  KEY `idx_ir_part_anchor` (`part_instance_id`),
  CONSTRAINT `inspection_record_chk_1` CHECK ((`inspection_type` in (_utf8mb4'IN_PROCESS',_utf8mb4'FINAL',_utf8mb4'FIRST_ARTICLE',_utf8mb4'RECEIVING'))),
  CONSTRAINT `inspection_record_chk_2` CHECK ((`result` in (_utf8mb4'PASS',_utf8mb4'FAIL',_utf8mb4'CONCESSION')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工序级检验记录；可选 work_step_detail_id 标定工步间检验点';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_install_removal`
--

DROP TABLE IF EXISTS `t1_install_removal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_install_removal` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `node_id` char(36) NOT NULL,
  `instance_id` char(36) DEFAULT NULL,
  `aircraft_id` char(36) NOT NULL,
  `bom_node_id` char(36) DEFAULT NULL COMMENT '对象锚点：BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '对象锚点：实物实例ID',
  `object_level` varchar(30) DEFAULT NULL COMMENT '对象锚点：层级',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '对象锚点：统一对象ID',
  `action_type` varchar(20) NOT NULL,
  `action_date` datetime(6) NOT NULL,
  `action_by` varchar(100) DEFAULT NULL,
  `authorized_by` varchar(100) DEFAULT NULL,
  `work_order_id` char(36) DEFAULT NULL,
  `aircraft_tsn_fh` decimal(12,2) DEFAULT NULL,
  `aircraft_tsn_fc` int DEFAULT NULL,
  `removal_reason` varchar(30) DEFAULT NULL,
  `removal_fault_code` varchar(100) DEFAULT NULL,
  `removal_remark` text,
  `assembly_params` json NOT NULL DEFAULT (json_object()),
  `software_pn` varchar(200) DEFAULT NULL,
  `software_version` varchar(100) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `fault_event_id` char(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_install_removal_node` (`node_id`),
  KEY `idx_install_removal_instance` (`instance_id`),
  KEY `idx_install_removal_aircraft` (`aircraft_id`,`action_date`),
  KEY `idx_install_removal_wo` (`work_order_id`),
  KEY `idx_install_removal_fault_event` (`fault_event_id`),
  KEY `idx_inst_bom_anchor` (`aircraft_id`,`bom_node_id`),
  KEY `idx_inst_part_anchor` (`part_instance_id`),
  CONSTRAINT `install_removal_chk_1` CHECK ((`action_type` in (_utf8mb4'INSTALL',_utf8mb4'REMOVAL',_utf8mb4'REPLACE'))),
  CONSTRAINT `install_removal_chk_2` CHECK (((`removal_reason` is null) or (`removal_reason` in (_utf8mb4'SCHEDULED',_utf8mb4'UNSCHEDULED',_utf8mb4'UPGRADE',_utf8mb4'DAMAGE'))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='装拆履历：关联 BOM 位置节点与 t1_part_instance；可选 fault_event_id 指向统一故障事件';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_life_usage_record`
--

DROP TABLE IF EXISTS `t1_life_usage_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_life_usage_record` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '寿命使用记录ID',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '统一对象ID',
  `object_level` varchar(30) NOT NULL COMMENT '对象层级',
  `object_id` char(36) DEFAULT NULL COMMENT '对象ID',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '实物实例ID',
  `usage_source_type` varchar(50) NOT NULL COMMENT '来源类型：flight/operation/maintenance/installation/manual_adjustment',
  `usage_source_id` char(36) DEFAULT NULL COMMENT '来源记录ID',
  `event_time` datetime(6) DEFAULT NULL COMMENT '事件时间',
  `fh_delta` decimal(12,3) NOT NULL DEFAULT '0.000' COMMENT '本次飞行小时增量',
  `fc_delta` int NOT NULL DEFAULT '0' COMMENT '本次循环增量',
  `landing_delta` int NOT NULL DEFAULT '0' COMMENT '本次起落增量',
  `calendar_days_delta` int NOT NULL DEFAULT '0' COMMENT '日历天增量',
  `total_fh_after` decimal(12,3) DEFAULT NULL COMMENT '累计小时',
  `total_fc_after` int DEFAULT NULL COMMENT '累计循环',
  `remaining_life_value` decimal(12,3) DEFAULT NULL COMMENT '剩余寿命',
  `remaining_life_unit` varchar(30) DEFAULT NULL COMMENT '剩余寿命单位',
  `calculated_by` varchar(100) DEFAULT NULL COMMENT '计算来源/算法',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_life_usage_object` (`object_level`,`object_id`,`event_time`),
  KEY `idx_life_usage_bom` (`bom_node_id`,`event_time`),
  KEY `idx_life_usage_part` (`part_instance_id`,`event_time`),
  KEY `idx_life_usage_aircraft` (`aircraft_id`,`event_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='寿命与使用消耗记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_manufacturing_anomaly`
--

DROP TABLE IF EXISTS `t1_manufacturing_anomaly`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_manufacturing_anomaly` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `anomaly_number` varchar(100) NOT NULL,
  `anomaly_name` varchar(200) DEFAULT NULL,
  `shop_order_task_id` char(36) DEFAULT NULL,
  `shop_order_id` char(36) DEFAULT NULL,
  `part_instance_id` char(36) DEFAULT NULL,
  `reported_by_id` varchar(50) DEFAULT NULL,
  `report_date` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `anomaly_date` date DEFAULT NULL,
  `description` text,
  `severity` varchar(20) NOT NULL DEFAULT 'LOW',
  `status` varchar(20) NOT NULL DEFAULT 'OPEN',
  `resolution` text,
  `resolved_by_id` varchar(50) DEFAULT NULL,
  `resolved_date` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `anomaly_number` (`anomaly_number`),
  KEY `idx_mfg_anomaly_task` (`shop_order_task_id`),
  KEY `idx_mfg_anomaly_order` (`shop_order_id`),
  KEY `idx_mfg_anomaly_status` (`status`),
  CONSTRAINT `manufacturing_anomaly_chk_1` CHECK ((`severity` in (_utf8mb4'LOW',_utf8mb4'MEDIUM',_utf8mb4'HIGH',_utf8mb4'CRITICAL'))),
  CONSTRAINT `manufacturing_anomaly_chk_2` CHECK ((`status` in (_utf8mb4'OPEN',_utf8mb4'INVESTIGATING',_utf8mb4'RESOLVED',_utf8mb4'CLOSED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='制造异常/偏差';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_material_lot_trace`
--

DROP TABLE IF EXISTS `t1_material_lot_trace`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_material_lot_trace` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `instance_id` char(36) NOT NULL,
  `shop_order_task_id` char(36) DEFAULT NULL,
  `aircraft_id` char(36) DEFAULT NULL COMMENT '对象锚点：飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT '对象锚点：BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '对象锚点：实物实例ID',
  `object_level` varchar(30) DEFAULT NULL COMMENT '对象锚点：层级',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '对象锚点：统一对象ID',
  `material_pn` varchar(200) DEFAULT NULL,
  `material_spec` varchar(200) DEFAULT NULL,
  `lot_number` varchar(100) DEFAULT NULL,
  `supplier` varchar(200) DEFAULT NULL,
  `mill_cert_number` varchar(200) DEFAULT NULL,
  `quantity_used` decimal(10,4) DEFAULT NULL,
  `unit` varchar(20) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_material_lot_instance` (`instance_id`),
  KEY `idx_material_lot_lot` (`lot_number`),
  KEY `idx_material_lot_shop_task` (`shop_order_task_id`),
  KEY `idx_mlt_bom_anchor` (`aircraft_id`,`bom_node_id`),
  KEY `idx_mlt_part_anchor` (`part_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='原材料批次追溯；可选 shop_order_task_id 指向消耗炉批的 MES 工序任务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_mining_job`
--

DROP TABLE IF EXISTS `t1_mining_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_mining_job` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `scope_id` char(36) NOT NULL,
  `algorithm_code` varchar(64) NOT NULL,
  `algorithm_version` varchar(32) NOT NULL,
  `params_json` json NOT NULL DEFAULT (json_object()),
  `result_summary_json` json NOT NULL DEFAULT (json_object()),
  `job_status` varchar(24) NOT NULL DEFAULT 'queued',
  `started_at` datetime(6) DEFAULT NULL,
  `finished_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_mining_job_scope` (`scope_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='外部挖掘任务登记：仅记录外部工具调用与结果摘要，不在本系统内置复杂挖掘平台';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_nonconformance_record`
--

DROP TABLE IF EXISTS `t1_nonconformance_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_nonconformance_record` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '不合格记录ID',
  `nc_number` varchar(100) NOT NULL COMMENT '不合格编号',
  `nc_title` varchar(500) DEFAULT NULL COMMENT '不合格标题',
  `object_level` varchar(30) DEFAULT NULL COMMENT '对象层级',
  `object_id` char(36) DEFAULT NULL COMMENT '对象ID',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '实物实例ID',
  `source_stage` varchar(30) DEFAULT NULL COMMENT '发生阶段',
  `source_table` varchar(200) DEFAULT NULL COMMENT '来源表',
  `source_record_id` char(36) DEFAULT NULL COMMENT '来源记录ID',
  `defect_code` varchar(100) DEFAULT NULL COMMENT '缺陷代码',
  `defect_level` varchar(50) DEFAULT NULL COMMENT '缺陷等级',
  `description` text COMMENT '问题描述',
  `severity` varchar(50) DEFAULT NULL COMMENT '严重度',
  `disposition` varchar(50) DEFAULT NULL COMMENT '处置方式：rework/repair/use_as_is/scrap/return',
  `status` varchar(50) NOT NULL DEFAULT 'open' COMMENT '状态',
  `reported_by` varchar(100) DEFAULT NULL COMMENT '报告人',
  `reported_at` datetime(6) DEFAULT NULL COMMENT '报告时间',
  `closed_by` varchar(100) DEFAULT NULL COMMENT '关闭人',
  `closed_at` datetime(6) DEFAULT NULL COMMENT '关闭时间',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_nonconformance_number` (`nc_number`),
  KEY `idx_nonconformance_object` (`object_level`,`object_id`),
  KEY `idx_nonconformance_bom` (`bom_node_id`),
  KEY `idx_nonconformance_part` (`part_instance_id`),
  KEY `idx_nonconformance_status` (`status`,`severity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='不合格/缺陷记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_object_data_link`
--

DROP TABLE IF EXISTS `t1_object_data_link`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_object_data_link` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '数据关联ID',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '统一对象ID',
  `object_level` varchar(30) NOT NULL COMMENT '对象层级',
  `object_id` char(36) DEFAULT NULL COMMENT '对象ID',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '实物实例ID',
  `data_domain` varchar(30) NOT NULL COMMENT '数据域：design/manufacturing/inspection/service/fault/status/document',
  `source_system` varchar(50) DEFAULT NULL COMMENT '来源系统',
  `source_table` varchar(200) NOT NULL COMMENT '来源表',
  `source_record_id` char(36) DEFAULT NULL COMMENT '来源记录ID',
  `source_record_key` varchar(500) DEFAULT NULL COMMENT '来源业务键',
  `relation_type` varchar(50) NOT NULL DEFAULT 'related' COMMENT '关系类型',
  `required_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否必需',
  `included_flag` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否纳入卷宗',
  `validity_status` varchar(24) NOT NULL DEFAULT 'active' COMMENT '有效状态',
  `summary` text COMMENT '摘要',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_object_data_link_source` (`object_level`,`object_id`,`data_domain`,`source_table`,`source_record_id`),
  KEY `idx_object_data_link_bom` (`bom_node_id`,`data_domain`),
  KEY `idx_object_data_link_part` (`part_instance_id`,`data_domain`),
  KEY `idx_object_data_link_aircraft` (`aircraft_id`,`data_domain`),
  KEY `idx_object_data_link_source_key` (`source_table`,`source_record_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对象与各阶段业务数据的统一关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_object_interface`
--

DROP TABLE IF EXISTS `t1_object_interface`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_object_interface` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '接口关系ID',
  `interface_code` varchar(100) NOT NULL COMMENT '接口编码',
  `interface_name` varchar(300) NOT NULL COMMENT '接口名称',
  `interface_type` varchar(50) DEFAULT NULL COMMENT '接口类型：mechanical/electrical/hydraulic/software/data等',
  `source_object_level` varchar(30) NOT NULL COMMENT '源对象层级',
  `source_object_id` char(36) DEFAULT NULL COMMENT '源对象ID',
  `source_bom_node_id` char(36) DEFAULT NULL COMMENT '源BOM节点ID',
  `target_object_level` varchar(30) DEFAULT NULL COMMENT '目标对象层级',
  `target_object_id` char(36) DEFAULT NULL COMMENT '目标对象ID',
  `target_bom_node_id` char(36) DEFAULT NULL COMMENT '目标BOM节点ID',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '所属飞机ID',
  `interface_summary` text COMMENT '接口摘要',
  `requirement_summary` text COMMENT '接口要求摘要',
  `control_doc_id` char(36) DEFAULT NULL COMMENT '接口控制文件ID',
  `maturity_status` varchar(50) DEFAULT NULL COMMENT '接口成熟度/状态',
  `verification_status` varchar(50) DEFAULT NULL COMMENT '验证状态',
  `effectivity` json NOT NULL DEFAULT (json_object()) COMMENT '适用性',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_object_interface_code` (`interface_code`),
  KEY `idx_object_interface_source` (`source_object_level`,`source_object_id`),
  KEY `idx_object_interface_target` (`target_object_level`,`target_object_id`),
  KEY `idx_object_interface_aircraft` (`aircraft_id`,`interface_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对象接口关系表：系统、子系统、设备、组件之间的接口';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_object_lifecycle_record`
--

DROP TABLE IF EXISTS `t1_object_lifecycle_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_object_lifecycle_record` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '生命周期记录ID',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '统一对象ID',
  `object_level` varchar(30) NOT NULL COMMENT '对象层级',
  `object_id` char(36) DEFAULT NULL COMMENT '对象ID',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '实物实例ID',
  `lifecycle_stage` varchar(30) NOT NULL COMMENT '阶段：design/manufacturing/inspection/installation/service/fault/technical_status/attachment',
  `business_record_type` varchar(100) NOT NULL COMMENT '业务记录类型',
  `business_record_id` char(36) DEFAULT NULL COMMENT '业务记录ID',
  `business_record_key` varchar(500) DEFAULT NULL COMMENT '业务记录业务键',
  `event_time` datetime(6) DEFAULT NULL COMMENT '事件时间',
  `event_title` varchar(500) DEFAULT NULL COMMENT '事件标题',
  `event_summary` text COMMENT '事件摘要',
  `result_status` varchar(50) DEFAULT NULL COMMENT '结果状态',
  `responsible_person_id` char(36) DEFAULT NULL COMMENT '责任人ID',
  `responsible_org_id` char(36) DEFAULT NULL COMMENT '责任单位ID',
  `source_system` varchar(50) DEFAULT NULL COMMENT '来源系统',
  `source_table` varchar(200) DEFAULT NULL COMMENT '来源表',
  `source_record_id` char(36) DEFAULT NULL COMMENT '来源记录ID',
  `source_trace_json` json NOT NULL DEFAULT (json_object()) COMMENT '来源追踪',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_olr_object` (`object_level`,`object_id`),
  KEY `idx_olr_bom_stage` (`bom_node_id`,`lifecycle_stage`),
  KEY `idx_olr_part_stage` (`part_instance_id`,`lifecycle_stage`),
  KEY `idx_olr_aircraft_time` (`aircraft_id`,`event_time`),
  KEY `idx_olr_source` (`source_system`,`source_table`,`source_record_id`),
  CONSTRAINT `chk_olr_level` CHECK ((`object_level` in (_utf8mb4'aircraft',_utf8mb4'system',_utf8mb4'subsystem',_utf8mb4'equipment',_utf8mb4'component',_utf8mb4'part'))),
  CONSTRAINT `chk_olr_stage` CHECK ((`lifecycle_stage` in (_utf8mb4'design',_utf8mb4'manufacturing',_utf8mb4'inspection',_utf8mb4'installation',_utf8mb4'service',_utf8mb4'fault',_utf8mb4'technical_status',_utf8mb4'attachment')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对象生命周期记录：统一串联各层级各阶段业务记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_object_status_history`
--

DROP TABLE IF EXISTS `t1_object_status_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_object_status_history` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '状态历史ID',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '统一对象ID',
  `object_level` varchar(30) NOT NULL COMMENT '对象层级',
  `object_id` char(36) DEFAULT NULL COMMENT '对象ID',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '实物实例ID',
  `status_category` varchar(50) NOT NULL COMMENT '状态类别：lifecycle/quality/operation/configuration/release',
  `old_status` varchar(100) DEFAULT NULL COMMENT '原状态',
  `new_status` varchar(100) NOT NULL COMMENT '新状态',
  `change_time` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '变更时间',
  `change_reason` text COMMENT '变更原因',
  `changed_by` varchar(100) DEFAULT NULL COMMENT '变更人',
  `source_table` varchar(200) DEFAULT NULL COMMENT '来源表',
  `source_record_id` char(36) DEFAULT NULL COMMENT '来源记录ID',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_status_history_object` (`object_level`,`object_id`,`change_time`),
  KEY `idx_status_history_bom` (`bom_node_id`,`change_time`),
  KEY `idx_status_history_part` (`part_instance_id`,`change_time`),
  KEY `idx_status_history_category` (`status_category`,`new_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对象状态变化历史表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_object_technical_status`
--

DROP TABLE IF EXISTS `t1_object_technical_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_object_technical_status` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '技术状态ID',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '统一对象ID',
  `object_level` varchar(30) NOT NULL COMMENT '对象层级',
  `object_id` char(36) DEFAULT NULL COMMENT '对象ID',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '实物实例ID',
  `status_code` varchar(100) NOT NULL COMMENT '技术状态编码',
  `status_name` varchar(300) DEFAULT NULL COMMENT '技术状态名称',
  `baseline_id` char(36) DEFAULT NULL COMMENT '构型基线ID',
  `baseline_code` varchar(200) DEFAULT NULL COMMENT '构型基线编码',
  `bom_version` varchar(100) DEFAULT NULL COMMENT 'BOM版本',
  `drawing_revision` varchar(100) DEFAULT NULL COMMENT '图纸版本',
  `process_revision` varchar(100) DEFAULT NULL COMMENT '工艺版本',
  `software_version` varchar(100) DEFAULT NULL COMMENT '软件版本',
  `hardware_version` varchar(100) DEFAULT NULL COMMENT '硬件版本',
  `modification_status` varchar(50) DEFAULT NULL COMMENT '改装状态',
  `deviation_status` varchar(50) DEFAULT NULL COMMENT '偏离/让步状态',
  `verification_status` varchar(50) DEFAULT NULL COMMENT '验证状态',
  `release_status` varchar(50) DEFAULT NULL COMMENT '放行状态',
  `effective_from` datetime(6) DEFAULT NULL COMMENT '生效时间',
  `effective_to` datetime(6) DEFAULT NULL COMMENT '失效时间',
  `source_system` varchar(50) DEFAULT NULL COMMENT '来源系统',
  `source_table` varchar(200) DEFAULT NULL COMMENT '来源表',
  `source_record_id` char(36) DEFAULT NULL COMMENT '来源记录ID',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_technical_status_code` (`status_code`),
  KEY `idx_technical_status_object` (`object_level`,`object_id`),
  KEY `idx_technical_status_bom` (`bom_node_id`),
  KEY `idx_technical_status_part` (`part_instance_id`),
  KEY `idx_technical_status_baseline` (`baseline_id`,`baseline_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对象技术状态表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_param_definition`
--

DROP TABLE IF EXISTS `t1_param_definition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_param_definition` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `param_code` varchar(100) NOT NULL,
  `param_name` varchar(200) NOT NULL,
  `data_type` varchar(20) NOT NULL,
  `unit` varchar(20) DEFAULT NULL,
  `category` varchar(50) DEFAULT NULL,
  `description` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `param_code` (`param_code`),
  CONSTRAINT `param_definition_chk_1` CHECK ((`data_type` in (_utf8mb4'NUMERIC',_utf8mb4'STRING',_utf8mb4'TINYINT(1)',_utf8mb4'JSON')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通用参数定义字典，供 t1_part_parameter_value 引用';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_part_document`
--

DROP TABLE IF EXISTS `t1_part_document`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_part_document` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `part_number` varchar(200) NOT NULL,
  `doc_type` varchar(30) NOT NULL,
  `doc_number` varchar(200) NOT NULL,
  `doc_revision` varchar(20) DEFAULT NULL,
  `doc_title` varchar(500) DEFAULT NULL,
  `effective_date` date DEFAULT NULL,
  `file_path` varchar(1000) DEFAULT NULL,
  `is_current` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_part_document_pn` (`part_number`),
  KEY `idx_part_document_type` (`doc_type`),
  CONSTRAINT `part_document_chk_1` CHECK ((`doc_type` in (_utf8mb4'DRAWING',_utf8mb4'CMM',_utf8mb4'IPC',_utf8mb4'SRM',_utf8mb4'SPEC',_utf8mb4'TEST_REPORT')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='件号关联工程/维修类文档';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_part_hydraulic_tube_impact_model`
--

DROP TABLE IF EXISTS `t1_part_hydraulic_tube_impact_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_part_hydraulic_tube_impact_model` (
  `part_number` varchar(200) NOT NULL,
  `material_spec` varchar(200) DEFAULT NULL,
  `density_kgm3` decimal(10,2) DEFAULT NULL,
  `poisson_ratio` decimal(5,4) DEFAULT NULL,
  `youngs_modulus_gpa` decimal(10,2) DEFAULT NULL,
  `yield_strength_mpa` decimal(10,2) DEFAULT NULL,
  `elastic_modulus_gpa` decimal(10,2) DEFAULT NULL,
  `outer_diameter_mm` decimal(10,3) DEFAULT NULL,
  `wall_thickness_mm` decimal(10,3) DEFAULT NULL,
  `analysis_description` text,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`part_number`),
  CONSTRAINT `part_hydraulic_tube_impact_model_chk_1` CHECK (((`poisson_ratio` >= 0) and (`poisson_ratio` <= 0.5)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='液压管路冲击仿真主模型：管体材料与等截面几何；一件号至多一条';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_part_instance`
--

DROP TABLE IF EXISTS `t1_part_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_part_instance` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `part_number` varchar(200) NOT NULL,
  `serial_number` varchar(200) DEFAULT NULL,
  `batch_number` varchar(200) DEFAULT NULL,
  `lot_number` varchar(200) DEFAULT NULL COMMENT '炉批/生产批/供应批号',
  `quantity` decimal(12,4) NOT NULL DEFAULT '1.0000' COMMENT '数量',
  `unit` varchar(20) NOT NULL DEFAULT 'EA' COMMENT '单位',
  `manufacturer` varchar(200) DEFAULT NULL,
  `cage_code` varchar(20) DEFAULT NULL,
  `manufacture_date` date DEFAULT NULL,
  `cure_date` date DEFAULT NULL,
  `shelf_life_expiry` date DEFAULT NULL,
  `source_type` varchar(20) DEFAULT NULL,
  `po_number` varchar(100) DEFAULT NULL,
  `receiving_date` date DEFAULT NULL,
  `receiving_inspection_passed` tinyint(1) DEFAULT NULL,
  `airworthiness_tag_type` varchar(20) DEFAULT NULL,
  `airworthiness_tag_number` varchar(200) DEFAULT NULL,
  `airworthiness_tag_date` date DEFAULT NULL,
  `tsn_fh_at_birth` decimal(10,2) NOT NULL DEFAULT '0.00',
  `tsn_fc_at_birth` int NOT NULL DEFAULT '0',
  `instance_status` varchar(20) NOT NULL DEFAULT 'IN_STOCK',
  `provenance_kind` varchar(30) NOT NULL DEFAULT 'UNKNOWN',
  `current_node_id` char(36) DEFAULT NULL,
  `current_aircraft_id` char(36) DEFAULT NULL COMMENT '当前所属飞机ID',
  `parent_component_instance_id` char(36) DEFAULT NULL COMMENT '所属组件实例ID',
  `installation_position` varchar(200) DEFAULT NULL COMMENT '装机位置',
  `position_code` varchar(100) DEFAULT NULL COMMENT '位号/站位',
  `installation_date` date DEFAULT NULL COMMENT '装机日期',
  `installation_status` varchar(50) DEFAULT NULL COMMENT '装机状态',
  `life_limit_value` decimal(12,2) DEFAULT NULL COMMENT '寿命限制值',
  `life_limit_unit` varchar(30) DEFAULT NULL COMMENT '寿命单位',
  `remaining_life_value` decimal(12,2) DEFAULT NULL COMMENT '剩余寿命值',
  `remaining_life_unit` varchar(30) DEFAULT NULL COMMENT '剩余寿命单位',
  `inspection_status` varchar(50) DEFAULT NULL COMMENT '检验状态',
  `release_status` varchar(50) DEFAULT NULL COMMENT '放行状态',
  `quality_status` varchar(50) DEFAULT NULL COMMENT '质量状态',
  `key_quality_characteristics` text COMMENT '关键质量特性摘要',
  `trace_code` varchar(200) DEFAULT NULL COMMENT '实物追溯码',
  `storage_location` varchar(200) DEFAULT NULL,
  `remark` text,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `produced_by_shop_order_id` char(36) DEFAULT NULL,
  `produced_by_shop_task_id` char(36) DEFAULT NULL,
  `production_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_part_instance_sn` (`part_number`,`serial_number`),
  KEY `idx_part_instance_status` (`instance_status`),
  KEY `idx_part_instance_current_node` (`current_node_id`),
  KEY `idx_part_instance_provenance` (`provenance_kind`),
  KEY `idx_part_instance_shop_order` (`produced_by_shop_order_id`),
  KEY `idx_part_instance_aircraft_node` (`current_aircraft_id`,`current_node_id`),
  KEY `idx_part_instance_quality_release` (`quality_status`,`release_status`),
  KEY `idx_part_instance_trace` (`trace_code`),
  CONSTRAINT `fk_part_instance_current_node` FOREIGN KEY (`current_node_id`) REFERENCES `t1_aircraft_bom_node` (`id`) ON DELETE SET NULL,
  CONSTRAINT `part_instance_chk_1` CHECK (((`source_type` is null) or (`source_type` in (_utf8mb4'NEW',_utf8mb4'OVERHAULED',_utf8mb4'REPAIRED',_utf8mb4'SERVICEABLE')))),
  CONSTRAINT `part_instance_chk_2` CHECK ((`instance_status` in (_utf8mb4'IN_STOCK',_utf8mb4'INSTALLED',_utf8mb4'IN_REPAIR',_utf8mb4'SCRAPPED',_utf8mb4'QUARANTINE'))),
  CONSTRAINT `part_instance_chk_3` CHECK ((`provenance_kind` in (_utf8mb4'UNKNOWN',_utf8mb4'IN_HOUSE_FROM_RAW',_utf8mb4'PROCURED',_utf8mb4'CONSOLIDATED_BY_ASSEMBLY')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实物实例；provenance_kind 区分自制自原材料/外购/装配合并；current_node_id↔BOM；可挂 produced_by_shop_*';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_part_instance_assembly`
--

DROP TABLE IF EXISTS `t1_part_instance_assembly`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_part_instance_assembly` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `parent_instance_id` char(36) NOT NULL,
  `child_instance_id` char(36) NOT NULL,
  `bom_node_id` char(36) DEFAULT NULL,
  `shop_order_task_id` char(36) DEFAULT NULL,
  `quantity` decimal(10,3) NOT NULL DEFAULT '1.000',
  `assembled_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `remark` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_part_instance_asm_pair` (`parent_instance_id`,`child_instance_id`),
  KEY `idx_part_instance_asm_parent` (`parent_instance_id`),
  KEY `idx_part_instance_asm_child` (`child_instance_id`),
  CONSTRAINT `chk_part_instance_assembly_no_self` CHECK ((`parent_instance_id` <> `child_instance_id`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='零件实例父子装配关系（子实例装入父成品实例）；与 BOM 装机树互补';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_part_master`
--

DROP TABLE IF EXISTS `t1_part_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_part_master` (
  `part_number` varchar(200) NOT NULL,
  `part_name` varchar(500) NOT NULL,
  `part_name_en` varchar(500) DEFAULT NULL,
  `drawing_no` varchar(200) DEFAULT NULL COMMENT '图纸编号',
  `drawing_revision` varchar(100) DEFAULT NULL COMMENT '图纸版本',
  `design_revision` varchar(100) DEFAULT NULL COMMENT '设计版本',
  `specification` varchar(500) DEFAULT NULL COMMENT '规格型号',
  `ata_chapter` varchar(20) DEFAULT NULL,
  `part_category` varchar(30) DEFAULT NULL,
  `weight_kg` decimal(10,4) DEFAULT NULL,
  `unit_of_measure` varchar(20) NOT NULL DEFAULT 'EA',
  `material_spec` varchar(200) DEFAULT NULL,
  `material` varchar(200) DEFAULT NULL COMMENT '材料',
  `material_grade` varchar(200) DEFAULT NULL COMMENT '材料牌号',
  `standard_no` varchar(200) DEFAULT NULL COMMENT '标准号/规范号',
  `surface_treatment` varchar(200) DEFAULT NULL,
  `is_life_limited` tinyint(1) NOT NULL DEFAULT '0',
  `is_serialized` tinyint(1) NOT NULL DEFAULT '0',
  `is_rotable` tinyint(1) NOT NULL DEFAULT '0',
  `is_expendable` tinyint(1) NOT NULL DEFAULT '0',
  `is_critical` tinyint(1) NOT NULL DEFAULT '0',
  `criticality_level` varchar(50) DEFAULT NULL COMMENT '关键等级',
  `key_quality_characteristics` text COMMENT '关键质量特性摘要',
  `life_limit_fh` decimal(10,2) DEFAULT NULL,
  `life_limit_fc` int DEFAULT NULL,
  `life_limit_year` int DEFAULT NULL,
  `life_limit_remark` text,
  `interchangeability_code` varchar(10) DEFAULT NULL,
  `alternate_part_number` varchar(200) DEFAULT NULL,
  `substitute_part_number` varchar(200) DEFAULT NULL COMMENT '替代件号',
  `effectivity_from` varchar(100) DEFAULT NULL,
  `effectivity_to` varchar(100) DEFAULT NULL,
  `revision` varchar(20) DEFAULT NULL,
  `revision_date` date DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `trace_code` varchar(200) DEFAULT NULL COMMENT '追溯码',
  `superseded_by` varchar(200) DEFAULT NULL,
  `default_supply_mode` varchar(20) NOT NULL DEFAULT 'BUY',
  `design_params_json` json NOT NULL DEFAULT (json_object()),
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`part_number`),
  KEY `idx_part_master_ata` (`ata_chapter`),
  KEY `idx_part_master_category` (`part_category`),
  KEY `idx_part_master_supply_mode` (`default_supply_mode`),
  KEY `idx_part_master_drawing` (`drawing_no`,`drawing_revision`),
  KEY `idx_part_master_criticality` (`criticality_level`),
  CONSTRAINT `part_master_chk_1` CHECK (((`part_category` is null) or (`part_category` in (_utf8mb4'RAW_MATERIAL',_utf8mb4'STANDARD_PART',_utf8mb4'VENDOR_PART',_utf8mb4'MANUFACTURER_PART',_utf8mb4'SOFTWARE')))),
  CONSTRAINT `part_master_chk_2` CHECK ((`status` in (_utf8mb4'ACTIVE',_utf8mb4'OBSOLETE',_utf8mb4'SUPERSEDED'))),
  CONSTRAINT `part_master_chk_3` CHECK ((`default_supply_mode` in (_utf8mb4'MAKE',_utf8mb4'BUY',_utf8mb4'MAKE_OR_BUY')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='件号主数据；含 default_supply_mode（设计约定自制/外购）；与 t1_aircraft_bom_node.part_number 业务对齐（非强制 FK）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_part_parameter_value`
--

DROP TABLE IF EXISTS `t1_part_parameter_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_part_parameter_value` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `part_number` varchar(200) NOT NULL,
  `param_code` varchar(100) NOT NULL,
  `value_numeric` decimal(20,8) DEFAULT NULL,
  `value_string` text,
  `value_boolean` tinyint(1) DEFAULT NULL,
  `value_json` json DEFAULT NULL,
  `effective_from` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `effective_to` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_part_param_value` (`part_number`,`param_code`,`effective_from`),
  KEY `idx_ppv_part_number` (`part_number`),
  KEY `idx_ppv_param_code` (`param_code`),
  KEY `idx_ppv_numeric` (`param_code`,`value_numeric`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='零件参数值历史（按生效时间版本化）；值类型按 t1_param_definition.data_type 约定落位';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_personnel_equipment_cert`
--

DROP TABLE IF EXISTS `t1_personnel_equipment_cert`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_personnel_equipment_cert` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `personnel_id` varchar(50) NOT NULL,
  `equipment_id` varchar(50) NOT NULL,
  `proficiency` decimal(3,2) DEFAULT NULL,
  `certified_date` date DEFAULT NULL,
  `certified_by` varchar(100) DEFAULT NULL,
  `expiry_date` date DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `personnel_id` (`personnel_id`,`equipment_id`),
  KEY `idx_pec_personnel` (`personnel_id`),
  KEY `idx_pec_equipment` (`equipment_id`),
  CONSTRAINT `personnel_equipment_cert_chk_1` CHECK (((`proficiency` is null) or ((`proficiency` >= 0) and (`proficiency` <= 1))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='人员—设备操作资质';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_physical_aircraft`
--

DROP TABLE IF EXISTS `t1_physical_aircraft`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_physical_aircraft` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `model_id` char(36) DEFAULT NULL,
  `tail_number` varchar(50) NOT NULL,
  `msn` varchar(100) NOT NULL,
  `registration_number` varchar(50) DEFAULT NULL,
  `aircraft_type` varchar(100) NOT NULL,
  `variant` varchar(100) DEFAULT NULL,
  `engine_type` varchar(200) DEFAULT NULL,
  `manufacturer` varchar(200) DEFAULT NULL,
  `delivery_date` date DEFAULT NULL,
  `operational_status` varchar(50) DEFAULT NULL,
  `total_fh` decimal(12,2) DEFAULT '0.00',
  `total_fc` int DEFAULT '0',
  `current_operator` varchar(200) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `tail_number` (`tail_number`),
  UNIQUE KEY `msn` (`msn`),
  KEY `idx_physical_aircraft_model` (`model_id`),
  CONSTRAINT `physical_aircraft_chk_1` CHECK ((`operational_status` in (_utf8mb4'IN_PRODUCTION',_utf8mb4'IN_TEST',_utf8mb4'DELIVERED',_utf8mb4'IN_SERVICE',_utf8mb4'AOG',_utf8mb4'IN_MAINTENANCE',_utf8mb4'STORED',_utf8mb4'RETIRED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='整机（单机）主数据；主键 id 被卷宗、构型、装机 BOM 节点等引用';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_physical_tooling`
--

DROP TABLE IF EXISTS `t1_physical_tooling`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_physical_tooling` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `tool_number` varchar(200) NOT NULL,
  `tool_name` varchar(500) NOT NULL,
  `tool_type` varchar(100) DEFAULT NULL,
  `calibration_required` tinyint(1) NOT NULL DEFAULT '0',
  `calibration_interval_days` int DEFAULT NULL,
  `last_calibration_date` date DEFAULT NULL,
  `next_calibration_date` date DEFAULT NULL,
  `location` varchar(200) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `tool_number` (`tool_number`),
  CONSTRAINT `physical_tooling_chk_1` CHECK ((`tool_type` in (_utf8mb4'JIG',_utf8mb4'FIXTURE',_utf8mb4'GAUGE',_utf8mb4'TEST_EQUIPMENT',_utf8mb4'SPECIAL_TOOL',_utf8mb4'STANDARD_TOOL')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工装主数据';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_process_route`
--

DROP TABLE IF EXISTS `t1_process_route`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_process_route` (
  `route_code` varchar(100) NOT NULL,
  `route_name` varchar(200) NOT NULL,
  `part_number` varchar(200) DEFAULT NULL,
  `model_id` char(36) DEFAULT NULL,
  `revision` varchar(20) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `total_standard_hours` decimal(10,2) DEFAULT NULL,
  `remarks` text,
  `route_type` varchar(20) DEFAULT NULL,
  `route_attrs_json` json NOT NULL DEFAULT (json_object()),
  `effectivity_from` date DEFAULT NULL,
  `effectivity_to` date DEFAULT NULL,
  `default_flag` tinyint(1) NOT NULL DEFAULT '0',
  `approved_by` varchar(100) DEFAULT NULL,
  `approval_date` date DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`route_code`),
  UNIQUE KEY `uq_process_route_default` (((case when (`default_flag` = 1) then `part_number` else NULL end))),
  KEY `idx_process_route_part` (`part_number`),
  KEY `idx_process_route_model` (`model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工艺路线头';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_process_step`
--

DROP TABLE IF EXISTS `t1_process_step`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_process_step` (
  `step_code` varchar(150) NOT NULL,
  `route_code` varchar(100) NOT NULL,
  `step_seq` int NOT NULL,
  `step_name` varchar(200) NOT NULL,
  `step_description` text,
  `workstation_code` varchar(20) DEFAULT NULL,
  `standard_time_min` decimal(10,2) DEFAULT NULL,
  `required_skill_level` varchar(20) DEFAULT NULL,
  `required_equipment_type` varchar(100) DEFAULT NULL,
  `inspection_required` tinyint(1) NOT NULL DEFAULT '0',
  `control_plan_ref` varchar(200) DEFAULT NULL,
  `step_type` varchar(30) DEFAULT NULL,
  `required_certification` varchar(200) DEFAULT NULL,
  `is_kpc` tinyint(1) NOT NULL DEFAULT '0',
  `mandatory` tinyint(1) NOT NULL DEFAULT '1',
  `setup_time_min` decimal(10,2) DEFAULT NULL,
  `teardown_time_min` decimal(10,2) DEFAULT NULL,
  `remarks` text,
  `step_attrs_json` json NOT NULL DEFAULT (json_object()),
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`step_code`),
  UNIQUE KEY `route_code` (`route_code`,`step_seq`),
  KEY `idx_process_step_route` (`route_code`),
  KEY `idx_process_step_workstation` (`workstation_code`),
  CONSTRAINT `process_step_chk_1` CHECK ((`step_type` in (_utf8mb4'FABRICATION',_utf8mb4'ASSEMBLY',_utf8mb4'INSPECTION',_utf8mb4'TEST',_utf8mb4'SPECIAL_PROCESS',_utf8mb4'MATERIAL_HANDLING',_utf8mb4'OUTSOURCE')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工艺路线工序';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_product_object_profile`
--

DROP TABLE IF EXISTS `t1_product_object_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_product_object_profile` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '对象ID，可与BOM节点或实物实例对齐',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '所属飞机ID',
  `aircraft_no` varchar(50) DEFAULT NULL COMMENT '所属机号',
  `object_level` varchar(30) NOT NULL COMMENT '层级：aircraft/system/subsystem/equipment/component/part',
  `object_code` varchar(200) NOT NULL COMMENT '对象编码：机号、系统码、件号、实例编码等',
  `object_name` varchar(500) NOT NULL COMMENT '对象名称',
  `object_name_en` varchar(500) DEFAULT NULL COMMENT '英文名称',
  `object_type` varchar(100) DEFAULT NULL COMMENT '对象类型',
  `parent_object_id` char(36) DEFAULT NULL COMMENT '上级对象ID',
  `parent_object_code` varchar(200) DEFAULT NULL COMMENT '上级对象编码',
  `root_aircraft_id` char(36) DEFAULT NULL COMMENT '根整机ID',
  `node_path` varchar(1000) DEFAULT NULL COMMENT '层级路径',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `part_master_id` varchar(200) DEFAULT NULL COMMENT '件号主数据ID/件号',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '实物实例ID',
  `part_number` varchar(200) DEFAULT NULL COMMENT '件号',
  `serial_number` varchar(200) DEFAULT NULL COMMENT '序列号',
  `batch_number` varchar(200) DEFAULT NULL COMMENT '批次号',
  `position_code` varchar(100) DEFAULT NULL COMMENT '位号/安装位置编码',
  `ata_code` varchar(20) DEFAULT NULL COMMENT 'ATA编码',
  `sns_code` varchar(100) DEFAULT NULL COMMENT 'SNS编码',
  `configuration_status` varchar(50) DEFAULT NULL COMMENT '构型状态',
  `effectivity` json NOT NULL DEFAULT (json_object()) COMMENT '适用性',
  `baseline_code` varchar(200) DEFAULT NULL COMMENT '构型/技术状态基线',
  `lifecycle_status` varchar(50) DEFAULT NULL COMMENT '生命周期状态',
  `quality_status` varchar(50) DEFAULT NULL COMMENT '质量状态',
  `operational_status` varchar(50) DEFAULT NULL COMMENT '运行状态',
  `responsible_department` varchar(200) DEFAULT NULL COMMENT '责任部门',
  `owner_org` varchar(200) DEFAULT NULL COMMENT '归属单位',
  `supplier_name` varchar(200) DEFAULT NULL COMMENT '供应商/制造商',
  `criticality_level` varchar(50) DEFAULT NULL COMMENT '关键等级',
  `maintainability_type` varchar(50) DEFAULT NULL COMMENT '维修属性',
  `trace_code` varchar(200) DEFAULT NULL COMMENT '追溯码',
  `source_system` varchar(50) DEFAULT NULL COMMENT '来源系统',
  `source_table` varchar(200) DEFAULT NULL COMMENT '来源表',
  `source_record_id` char(36) DEFAULT NULL COMMENT '来源记录ID',
  `source_record_key` varchar(500) DEFAULT NULL COMMENT '来源业务键',
  `snapshot_id` char(36) DEFAULT NULL COMMENT '快照ID',
  `data_status` varchar(24) NOT NULL DEFAULT 'active' COMMENT '数据状态',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_object_bom_node` (`bom_node_id`),
  KEY `idx_product_object_aircraft_level` (`aircraft_id`,`object_level`),
  KEY `idx_product_object_code` (`object_code`),
  KEY `idx_product_object_parent` (`parent_object_id`),
  KEY `idx_product_object_part_instance` (`part_instance_id`),
  KEY `idx_product_object_source` (`source_system`,`source_table`,`source_record_id`),
  CONSTRAINT `chk_product_object_data_status` CHECK ((`data_status` in (_utf8mb4'active',_utf8mb4'inactive',_utf8mb4'archived',_utf8mb4'missing',_utf8mb4'corrected'))),
  CONSTRAINT `chk_product_object_level` CHECK ((`object_level` in (_utf8mb4'aircraft',_utf8mb4'system',_utf8mb4'subsystem',_utf8mb4'equipment',_utf8mb4'component',_utf8mb4'part')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='六层产品对象统一身份表：整机、系统、子系统、设备、组件、零件';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_production_operation_record`
--

DROP TABLE IF EXISTS `t1_production_operation_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_production_operation_record` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `shop_order_task_id` char(36) NOT NULL,
  `aircraft_id` char(36) DEFAULT NULL COMMENT '对象锚点：飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT '对象锚点：BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '对象锚点：实物实例ID',
  `object_level` varchar(30) DEFAULT NULL COMMENT '对象锚点：层级',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '对象锚点：统一对象ID',
  `work_step_detail_id` char(36) DEFAULT NULL,
  `detail_seq` int DEFAULT NULL,
  `actual_params` json NOT NULL DEFAULT (json_object()),
  `equipment_id` varchar(50) DEFAULT NULL,
  `operator_id` varchar(50) DEFAULT NULL,
  `start_time` datetime(6) DEFAULT NULL,
  `end_time` datetime(6) DEFAULT NULL,
  `data_source` varchar(20) NOT NULL DEFAULT 'MANUAL',
  `remarks` text,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_prod_op_record_task` (`shop_order_task_id`),
  KEY `idx_prod_op_record_detail` (`work_step_detail_id`),
  KEY `idx_por_bom_anchor` (`aircraft_id`,`bom_node_id`),
  KEY `idx_por_part_anchor` (`part_instance_id`),
  CONSTRAINT `production_operation_record_chk_1` CHECK ((`data_source` in (_utf8mb4'MANUAL',_utf8mb4'DNC',_utf8mb4'PLC')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工步加工实际记录：关 t1_shop_order_task，可选关 t1_work_step_detail 与设备/人员';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_push_record`
--

DROP TABLE IF EXISTS `t1_push_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_push_record` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '推送记录ID',
  `rule_id` char(36) DEFAULT NULL COMMENT '推送规则ID',
  `subject_type` varchar(50) NOT NULL COMMENT '推送对象类型',
  `subject_id` char(36) DEFAULT NULL COMMENT '推送对象ID',
  `receiver` varchar(300) NOT NULL COMMENT '接收人或接收地址',
  `channel` varchar(30) NOT NULL DEFAULT 'system' COMMENT '推送渠道：system/email/message/webhook',
  `push_title` varchar(300) NOT NULL COMMENT '推送标题',
  `push_content` text COMMENT '推送内容',
  `push_status` varchar(24) NOT NULL DEFAULT 'pending' COMMENT '推送状态：pending/sent/read/failed',
  `sent_at` datetime(6) DEFAULT NULL COMMENT '发送时间',
  `read_at` datetime(6) DEFAULT NULL COMMENT '已读时间',
  `error_message` text COMMENT '错误信息',
  `payload_json` json NOT NULL DEFAULT (json_object()) COMMENT '推送载荷',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_push_record_rule` (`rule_id`),
  KEY `idx_push_record_subject` (`subject_type`,`subject_id`),
  KEY `idx_push_record_receiver` (`receiver`),
  KEY `idx_push_record_status` (`push_status`),
  CONSTRAINT `chk_push_record_status` CHECK ((`push_status` in (_utf8mb4'pending',_utf8mb4'sent',_utf8mb4'read',_utf8mb4'failed')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能信息推送记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_push_rule`
--

DROP TABLE IF EXISTS `t1_push_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_push_rule` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '推送规则ID',
  `rule_code` varchar(100) NOT NULL COMMENT '规则编码',
  `rule_name` varchar(200) NOT NULL COMMENT '规则名称',
  `trigger_type` varchar(50) NOT NULL COMMENT '触发类型：issue/alert/update/export/search_subscription',
  `target_domain` varchar(50) DEFAULT NULL COMMENT '目标领域',
  `condition_json` json NOT NULL DEFAULT (json_object()) COMMENT '触发条件',
  `receiver_type` varchar(30) NOT NULL COMMENT '接收对象类型：user/role/org/external',
  `receiver_ref` varchar(300) NOT NULL COMMENT '接收对象标识',
  `message_template` text COMMENT '消息模板',
  `rule_status` varchar(24) NOT NULL DEFAULT 'active' COMMENT '规则状态：active/disabled',
  `created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_push_rule_code` (`rule_code`),
  KEY `idx_push_rule_trigger` (`trigger_type`),
  KEY `idx_push_rule_status` (`rule_status`),
  CONSTRAINT `chk_push_rule_status` CHECK ((`rule_status` in (_utf8mb4'active',_utf8mb4'disabled')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能信息推送规则';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_qa_message`
--

DROP TABLE IF EXISTS `t1_qa_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_qa_message` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '消息ID',
  `session_id` char(36) NOT NULL COMMENT '会话ID',
  `message_role` varchar(20) NOT NULL COMMENT '消息角色：user/assistant/system',
  `message_text` longtext NOT NULL COMMENT '消息内容',
  `related_search_id` char(36) DEFAULT NULL COMMENT '关联检索记录ID',
  `evidence_json` json NOT NULL DEFAULT (json_array()) COMMENT '回答依据',
  `model_name` varchar(100) DEFAULT NULL COMMENT '模型名称',
  `token_count` int DEFAULT NULL COMMENT '消耗量',
  `feedback_score` int DEFAULT NULL COMMENT '反馈评分',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_qa_message_session` (`session_id`,`created_at`),
  KEY `idx_qa_message_search` (`related_search_id`),
  CONSTRAINT `chk_qa_message_role` CHECK ((`message_role` in (_utf8mb4'user',_utf8mb4'assistant',_utf8mb4'system')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能问答消息记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_qa_session`
--

DROP TABLE IF EXISTS `t1_qa_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_qa_session` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '会话ID',
  `session_code` varchar(100) NOT NULL COMMENT '会话编码',
  `session_title` varchar(300) DEFAULT NULL COMMENT '会话标题',
  `dossier_instance_id` char(36) DEFAULT NULL COMMENT '卷宗实例ID',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '飞机ID',
  `session_status` varchar(24) NOT NULL DEFAULT 'open' COMMENT '会话状态：open/closed/archived',
  `context_scope_json` json NOT NULL DEFAULT (json_object()) COMMENT '问答上下文范围',
  `created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  `closed_at` datetime(6) DEFAULT NULL COMMENT '关闭时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_qa_session_code` (`session_code`),
  KEY `idx_qa_session_dossier` (`dossier_instance_id`),
  KEY `idx_qa_session_aircraft` (`aircraft_id`),
  KEY `idx_qa_session_status` (`session_status`),
  CONSTRAINT `chk_qa_session_status` CHECK ((`session_status` in (_utf8mb4'open',_utf8mb4'closed',_utf8mb4'archived')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能问答会话';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_quality_characteristic`
--

DROP TABLE IF EXISTS `t1_quality_characteristic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_quality_characteristic` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '质量特性ID',
  `characteristic_code` varchar(100) NOT NULL COMMENT '特性编码',
  `characteristic_name` varchar(300) NOT NULL COMMENT '特性名称',
  `characteristic_type` varchar(50) DEFAULT NULL COMMENT '特性类型：design/process/product/safety/reliability/maintainability',
  `object_level` varchar(30) DEFAULT NULL COMMENT '适用对象层级',
  `object_id` char(36) DEFAULT NULL COMMENT '适用对象ID',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `part_number` varchar(200) DEFAULT NULL COMMENT '件号',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '实物实例ID',
  `characteristic_source` varchar(100) DEFAULT NULL COMMENT '来源：图纸/规范/工艺/适航/质量计划等',
  `key_characteristic_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否关键特性',
  `nominal_value` varchar(200) DEFAULT NULL COMMENT '名义值',
  `upper_limit` varchar(200) DEFAULT NULL COMMENT '上限',
  `lower_limit` varchar(200) DEFAULT NULL COMMENT '下限',
  `tolerance` varchar(200) DEFAULT NULL COMMENT '公差',
  `unit` varchar(50) DEFAULT NULL COMMENT '单位',
  `inspection_method` varchar(500) DEFAULT NULL COMMENT '检验方法',
  `control_plan_ref` varchar(200) DEFAULT NULL COMMENT '控制计划引用',
  `risk_level` varchar(50) DEFAULT NULL COMMENT '风险等级',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否有效',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_quality_characteristic_code` (`characteristic_code`),
  KEY `idx_quality_characteristic_object` (`object_level`,`object_id`),
  KEY `idx_quality_characteristic_bom` (`bom_node_id`),
  KEY `idx_quality_characteristic_part` (`part_number`,`part_instance_id`),
  KEY `idx_quality_characteristic_key` (`key_characteristic_flag`,`risk_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='质量特性定义表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_quality_text_record`
--

DROP TABLE IF EXISTS `t1_quality_text_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_quality_text_record` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `record_number` varchar(200) NOT NULL,
  `lifecycle_stage` varchar(20) NOT NULL,
  `record_type` varchar(30) NOT NULL,
  `title` varchar(500) DEFAULT NULL,
  `content` text,
  `summary` text,
  `keywords` text,
  `record_time` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `created_by_person_id` char(36) DEFAULT NULL,
  `created_by_org_id` char(36) DEFAULT NULL,
  `review_status` varchar(20) NOT NULL DEFAULT 'DRAFT',
  `classification` varchar(20) NOT NULL DEFAULT 'INTERNAL',
  `language` varchar(10) NOT NULL DEFAULT 'zh',
  `attachments` json DEFAULT (json_array()),
  `trace_code` varchar(200) DEFAULT NULL,
  `aircraft_id` char(36) DEFAULT NULL,
  `aircraft_bom_node_id` char(36) DEFAULT NULL,
  `bom_node_id` char(36) DEFAULT NULL COMMENT '对象锚点：标准BOM节点ID；由aircraft_bom_node_id回填',
  `part_instance_id` char(36) DEFAULT NULL,
  `object_level` varchar(30) DEFAULT NULL COMMENT '对象锚点：层级',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '对象锚点：统一对象ID',
  `source_record_type` varchar(50) DEFAULT NULL,
  `source_record_id` char(36) DEFAULT NULL,
  `extended_attrs` json NOT NULL DEFAULT (json_object()),
  `remark` text,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `record_number` (`record_number`),
  KEY `idx_quality_text_record_aircraft` (`aircraft_id`),
  KEY `idx_quality_text_record_bom_node` (`aircraft_bom_node_id`),
  KEY `idx_quality_text_record_part_instance` (`part_instance_id`),
  KEY `idx_quality_text_record_stage` (`lifecycle_stage`),
  KEY `idx_quality_text_record_type` (`record_type`),
  KEY `idx_quality_text_record_review` (`review_status`),
  KEY `idx_quality_text_record_time` (`record_time`),
  KEY `idx_quality_text_record_source` (`source_record_type`,`source_record_id`),
  KEY `fk_qtr_created_by_person` (`created_by_person_id`),
  KEY `fk_qtr_created_by_org` (`created_by_org_id`),
  KEY `idx_qtr_bom_anchor` (`aircraft_id`,`bom_node_id`),
  KEY `idx_qtr_part_anchor` (`part_instance_id`),
  CONSTRAINT `fk_qtr_created_by_org` FOREIGN KEY (`created_by_org_id`) REFERENCES `t1_actor_organization` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_qtr_created_by_person` FOREIGN KEY (`created_by_person_id`) REFERENCES `t1_actor_person` (`id`) ON DELETE SET NULL,
  CONSTRAINT `quality_text_record_chk_1` CHECK ((`lifecycle_stage` in (_utf8mb4'DESIGN',_utf8mb4'MANUFACTURING',_utf8mb4'SERVICE'))),
  CONSTRAINT `quality_text_record_chk_2` CHECK ((`record_type` in (_utf8mb4'INSPECTION_REPORT',_utf8mb4'REPAIR_RECORD',_utf8mb4'NONCONFORMITY_REPORT',_utf8mb4'FAULT_REPORT',_utf8mb4'TEST_REPORT',_utf8mb4'DESIGN_REVIEW',_utf8mb4'AUDIT_REPORT',_utf8mb4'CORRECTIVE_ACTION',_utf8mb4'OTHER'))),
  CONSTRAINT `quality_text_record_chk_3` CHECK ((`review_status` in (_utf8mb4'DRAFT',_utf8mb4'SUBMITTED',_utf8mb4'APPROVED',_utf8mb4'REJECTED'))),
  CONSTRAINT `quality_text_record_chk_4` CHECK ((`classification` in (_utf8mb4'PUBLIC',_utf8mb4'INTERNAL',_utf8mb4'CONFIDENTIAL',_utf8mb4'SECRET')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='质量文本记录：设计/制造/服役非结构化文本；可挂飞机/BOM/件实例及来源结构化记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_release_record`
--

DROP TABLE IF EXISTS `t1_release_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_release_record` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '放行记录ID',
  `release_number` varchar(100) NOT NULL COMMENT '放行编号',
  `release_type` varchar(50) NOT NULL COMMENT '放行类型：manufacturing/quality/airworthiness/delivery/service',
  `object_level` varchar(30) DEFAULT NULL COMMENT '对象层级',
  `object_id` char(36) DEFAULT NULL COMMENT '对象ID',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '实物实例ID',
  `source_table` varchar(200) DEFAULT NULL COMMENT '来源表',
  `source_record_id` char(36) DEFAULT NULL COMMENT '来源记录ID',
  `release_status` varchar(50) NOT NULL DEFAULT 'pending' COMMENT '放行状态',
  `release_basis` text COMMENT '放行依据',
  `released_by` varchar(100) DEFAULT NULL COMMENT '放行人',
  `released_at` datetime(6) DEFAULT NULL COMMENT '放行时间',
  `certificate_doc_id` char(36) DEFAULT NULL COMMENT '证书/文件ID',
  `expiry_date` date DEFAULT NULL COMMENT '有效期',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_release_number` (`release_number`),
  KEY `idx_release_object` (`object_level`,`object_id`),
  KEY `idx_release_bom` (`bom_node_id`),
  KEY `idx_release_part` (`part_instance_id`),
  KEY `idx_release_status` (`release_type`,`release_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='质量/制造/适航/维修放行记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_search_index_record`
--

DROP TABLE IF EXISTS `t1_search_index_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_search_index_record` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '索引记录ID',
  `index_code` varchar(100) NOT NULL COMMENT '索引编码',
  `object_type` varchar(50) NOT NULL COMMENT '对象类型：DOSSIER/DOCUMENT/STRUCTURE_NODE/WORK_ORDER/FAULT等',
  `object_id` char(36) DEFAULT NULL COMMENT '对象ID',
  `object_key` varchar(500) DEFAULT NULL COMMENT '对象业务键',
  `dossier_instance_id` char(36) DEFAULT NULL COMMENT '卷宗实例ID',
  `dossier_version_id` char(36) DEFAULT NULL COMMENT '卷宗版本ID',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '飞机ID',
  `title` varchar(1000) NOT NULL COMMENT '标题',
  `summary` text COMMENT '摘要',
  `keywords` varchar(1000) DEFAULT NULL COMMENT '关键词',
  `lifecycle_stage` varchar(30) DEFAULT NULL COMMENT '生命周期阶段：DESIGN/MANUFACTURING/SERVICE',
  `source_system` varchar(50) DEFAULT NULL COMMENT '来源系统',
  `source_table` varchar(200) DEFAULT NULL COMMENT '来源表',
  `source_record_id` char(36) DEFAULT NULL COMMENT '来源记录ID',
  `content_text` longtext COMMENT '可检索正文',
  `metadata_json` json NOT NULL DEFAULT (json_object()) COMMENT '检索元数据',
  `index_status` varchar(24) NOT NULL DEFAULT 'active' COMMENT '索引状态：active/disabled/deleted',
  `indexed_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '入库时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_search_index_record_code` (`index_code`),
  KEY `idx_search_index_object` (`object_type`,`object_id`),
  KEY `idx_search_index_dossier` (`dossier_instance_id`,`dossier_version_id`),
  KEY `idx_search_index_aircraft` (`aircraft_id`),
  KEY `idx_search_index_stage` (`lifecycle_stage`),
  KEY `idx_search_index_source` (`source_system`,`source_table`,`source_record_id`),
  FULLTEXT KEY `ft_search_index_text` (`title`,`summary`,`keywords`,`content_text`),
  CONSTRAINT `chk_search_index_status` CHECK ((`index_status` in (_utf8mb4'active',_utf8mb4'disabled',_utf8mb4'deleted')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能检索索引记录：汇总卷宗、文档和业务数据的可检索内容';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_search_record`
--

DROP TABLE IF EXISTS `t1_search_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_search_record` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '检索记录ID',
  `search_code` varchar(100) NOT NULL COMMENT '检索编码',
  `search_type` varchar(30) NOT NULL COMMENT '检索类型：keyword/filter/semantic/qa',
  `query_text` text NOT NULL COMMENT '检索内容',
  `filter_json` json NOT NULL DEFAULT (json_object()) COMMENT '筛选条件',
  `result_count` int NOT NULL DEFAULT '0' COMMENT '结果数量',
  `top_result_json` json NOT NULL DEFAULT (json_array()) COMMENT '前置结果摘要',
  `search_status` varchar(24) NOT NULL DEFAULT 'succeeded' COMMENT '状态：succeeded/failed',
  `execution_ms` int DEFAULT NULL COMMENT '执行耗时毫秒',
  `requested_by` varchar(100) DEFAULT NULL COMMENT '发起人',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_search_record_code` (`search_code`),
  KEY `idx_search_record_type` (`search_type`),
  KEY `idx_search_record_created_at` (`created_at`),
  CONSTRAINT `chk_search_record_status` CHECK ((`search_status` in (_utf8mb4'succeeded',_utf8mb4'failed'))),
  CONSTRAINT `chk_search_record_type` CHECK ((`search_type` in (_utf8mb4'keyword',_utf8mb4'filter',_utf8mb4'semantic',_utf8mb4'qa')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能检索历史：记录全域检索、多维筛选和语义检索行为';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_shop_equipment`
--

DROP TABLE IF EXISTS `t1_shop_equipment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_shop_equipment` (
  `id` varchar(50) NOT NULL,
  `equipment_name` varchar(200) NOT NULL,
  `equipment_num` varchar(100) DEFAULT NULL,
  `equipment_type` varchar(100) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `workstation_code` varchar(20) DEFAULT NULL,
  `manufacturer` varchar(200) DEFAULT NULL,
  `model` varchar(100) DEFAULT NULL,
  `serial_number` varchar(100) DEFAULT NULL,
  `purchase_date` date DEFAULT NULL,
  `last_calibration` date DEFAULT NULL,
  `next_calibration` date DEFAULT NULL,
  `calibration_cycle_days` int DEFAULT NULL,
  `color_tag` varchar(20) DEFAULT NULL,
  `remarks` text,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `equipment_num` (`equipment_num`),
  KEY `idx_shop_equipment_workstation` (`workstation_code`),
  KEY `idx_shop_equipment_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='MES 设备主数据';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_shop_order`
--

DROP TABLE IF EXISTS `t1_shop_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_shop_order` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `order_code` varchar(100) NOT NULL,
  `order_type` varchar(20) NOT NULL,
  `part_number` varchar(200) NOT NULL,
  `aircraft_id` char(36) DEFAULT NULL,
  `bom_node_id` char(36) DEFAULT NULL COMMENT '对象锚点：BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '对象锚点：实物实例ID',
  `object_level` varchar(30) DEFAULT NULL COMMENT '对象锚点：层级',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '对象锚点：统一对象ID',
  `route_code` varchar(100) DEFAULT NULL,
  `quantity_ordered` int NOT NULL,
  `quantity_produced` int NOT NULL DEFAULT '0',
  `priority` int NOT NULL DEFAULT '5',
  `urgent_flag` tinyint(1) NOT NULL DEFAULT '0',
  `status` varchar(20) NOT NULL DEFAULT 'PLANNED',
  `planned_start_date` date DEFAULT NULL,
  `planned_finish_date` date DEFAULT NULL,
  `actual_start` datetime(6) DEFAULT NULL,
  `actual_finish` datetime(6) DEFAULT NULL,
  `released_by` varchar(200) DEFAULT NULL,
  `released_date` datetime(6) DEFAULT NULL,
  `remarks` text,
  `order_attrs_json` json NOT NULL DEFAULT (json_object()),
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_code` (`order_code`),
  KEY `idx_shop_order_part` (`part_number`),
  KEY `idx_shop_order_aircraft` (`aircraft_id`),
  KEY `idx_shop_order_status` (`status`),
  KEY `idx_so_bom_anchor` (`aircraft_id`,`bom_node_id`),
  KEY `idx_so_part_anchor` (`part_instance_id`),
  CONSTRAINT `shop_order_chk_1` CHECK ((`order_type` in (_utf8mb4'FABRICATION',_utf8mb4'ASSEMBLY',_utf8mb4'SUB_ASSEMBLY'))),
  CONSTRAINT `shop_order_chk_2` CHECK ((`status` in (_utf8mb4'PLANNED',_utf8mb4'RELEASED',_utf8mb4'IN_PROGRESS',_utf8mb4'COMPLETED',_utf8mb4'CLOSED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='车间制造/装配工单（对应 INDENT）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_shop_order_task`
--

DROP TABLE IF EXISTS `t1_shop_order_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_shop_order_task` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `task_code` varchar(100) NOT NULL,
  `shop_order_id` char(36) NOT NULL,
  `aircraft_id` char(36) DEFAULT NULL COMMENT '对象锚点：飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT '对象锚点：BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '对象锚点：实物实例ID',
  `object_level` varchar(30) DEFAULT NULL COMMENT '对象锚点：层级',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '对象锚点：统一对象ID',
  `step_code` varchar(150) NOT NULL,
  `task_seq` int NOT NULL,
  `parent_task_id` char(36) DEFAULT NULL,
  `quantity_to_produce` int NOT NULL,
  `quantity_completed` int NOT NULL DEFAULT '0',
  `priority` int NOT NULL DEFAULT '5',
  `urgent_flag` tinyint(1) NOT NULL DEFAULT '0',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `planned_start` datetime(6) DEFAULT NULL,
  `planned_finish` datetime(6) DEFAULT NULL,
  `actual_start` datetime(6) DEFAULT NULL,
  `actual_finish` datetime(6) DEFAULT NULL,
  `assigned_workstation_code` varchar(20) DEFAULT NULL,
  `assigned_equipment_id` varchar(50) DEFAULT NULL,
  `assigned_personnel_id` varchar(50) DEFAULT NULL,
  `result_notes` text,
  `task_attrs_json` json NOT NULL DEFAULT (json_object()),
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `task_code` (`task_code`),
  UNIQUE KEY `shop_order_id` (`shop_order_id`,`task_seq`),
  KEY `idx_shop_order_task_order` (`shop_order_id`),
  KEY `idx_shop_order_task_step` (`step_code`),
  KEY `idx_shop_order_task_status` (`status`),
  KEY `idx_shop_order_task_ws` (`assigned_workstation_code`),
  KEY `idx_sot_bom_anchor` (`aircraft_id`,`bom_node_id`),
  KEY `idx_sot_part_anchor` (`part_instance_id`),
  CONSTRAINT `shop_order_task_chk_1` CHECK ((`status` in (_utf8mb4'PENDING',_utf8mb4'DISPATCHED',_utf8mb4'IN_PROGRESS',_utf8mb4'COMPLETED',_utf8mb4'INSPECTION')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='车间工序级执行任务（对应 child_order）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_shop_personnel`
--

DROP TABLE IF EXISTS `t1_shop_personnel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_shop_personnel` (
  `id` varchar(50) NOT NULL,
  `employee_num` varchar(100) NOT NULL,
  `employee_name` varchar(200) NOT NULL,
  `workshop` varchar(100) DEFAULT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `skill_level` varchar(20) DEFAULT NULL,
  `job_type` varchar(20) DEFAULT NULL,
  `remarks` text,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `employee_num` (`employee_num`),
  KEY `idx_shop_personnel_workshop` (`workshop`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='MES 车间人员主数据';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_shop_visit`
--

DROP TABLE IF EXISTS `t1_shop_visit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_shop_visit` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `instance_id` char(36) NOT NULL,
  `node_id` char(36) DEFAULT NULL,
  `sv_number` varchar(100) DEFAULT NULL,
  `repair_shop` varchar(200) DEFAULT NULL,
  `shop_approval_number` varchar(100) DEFAULT NULL,
  `removal_reason` text,
  `tsn_fh_in` decimal(12,2) DEFAULT NULL,
  `tsn_fc_in` int DEFAULT NULL,
  `induction_date` date DEFAULT NULL,
  `repair_level` varchar(20) DEFAULT NULL,
  `repair_description` text,
  `tsn_fh_out` decimal(12,2) DEFAULT NULL,
  `tsn_fc_out` int DEFAULT NULL,
  `return_date` date DEFAULT NULL,
  `release_tag_number` varchar(200) DEFAULT NULL,
  `sv_status` varchar(20) NOT NULL DEFAULT 'IN_SHOP',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_shop_visit_instance` (`instance_id`),
  CONSTRAINT `shop_visit_chk_1` CHECK (((`repair_level` is null) or (`repair_level` in (_utf8mb4'OVERHAUL',_utf8mb4'REPAIR',_utf8mb4'SCRAP',_utf8mb4'BER')))),
  CONSTRAINT `shop_visit_chk_2` CHECK ((`sv_status` in (_utf8mb4'IN_SHOP',_utf8mb4'RETURNED',_utf8mb4'SCRAPPED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='送修履历';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_software_load_record`
--

DROP TABLE IF EXISTS `t1_software_load_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_software_load_record` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '软件/参数加载记录ID',
  `load_record_no` varchar(100) NOT NULL COMMENT '加载记录编号',
  `aircraft_id` char(36) DEFAULT NULL COMMENT '飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `equipment_instance_id` char(36) DEFAULT NULL COMMENT '设备实例ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '实物实例ID',
  `software_part_number` varchar(200) DEFAULT NULL COMMENT '软件件号',
  `software_version` varchar(100) DEFAULT NULL COMMENT '软件版本',
  `hardware_version` varchar(100) DEFAULT NULL COMMENT '硬件版本',
  `load_type` varchar(50) DEFAULT NULL COMMENT '加载类型：software/firmware/database/parameter',
  `load_time` datetime(6) DEFAULT NULL COMMENT '加载时间',
  `loaded_by` varchar(100) DEFAULT NULL COMMENT '加载人',
  `verified_by` varchar(100) DEFAULT NULL COMMENT '复核人',
  `verification_status` varchar(50) DEFAULT NULL COMMENT '验证状态',
  `source_table` varchar(200) DEFAULT NULL COMMENT '来源表',
  `source_record_id` char(36) DEFAULT NULL COMMENT '来源记录ID',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_software_load_no` (`load_record_no`),
  KEY `idx_software_load_equipment` (`equipment_instance_id`),
  KEY `idx_software_load_bom` (`bom_node_id`),
  KEY `idx_software_load_version` (`software_part_number`,`software_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='软件、固件、数据库和参数加载记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_subsystem_object_profile`
--

DROP TABLE IF EXISTS `t1_subsystem_object_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_subsystem_object_profile` (
  `subsystem_id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '子系统对象ID',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '统一对象ID',
  `aircraft_id` char(36) NOT NULL COMMENT '所属飞机ID',
  `system_id` char(36) DEFAULT NULL COMMENT '所属系统对象ID',
  `parent_bom_node_id` char(36) DEFAULT NULL COMMENT '上级系统BOM节点ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `subsystem_code` varchar(100) NOT NULL COMMENT '子系统编码',
  `subsystem_name` varchar(300) NOT NULL COMMENT '子系统名称',
  `ata_section` varchar(20) DEFAULT NULL COMMENT 'ATA子章节',
  `sns_subsystem_code` varchar(100) DEFAULT NULL COMMENT 'SNS子系统码',
  `function_area` varchar(200) DEFAULT NULL COMMENT '功能分区',
  `function_summary` text COMMENT '功能摘要',
  `boundary_description` text COMMENT '边界说明',
  `included_equipment_count` int NOT NULL DEFAULT '0' COMMENT '组成设备数量',
  `main_interfaces` text COMMENT '接口摘要',
  `configuration_baseline` varchar(200) DEFAULT NULL COMMENT '构型基线',
  `effectivity` json NOT NULL DEFAULT (json_object()) COMMENT '适用性',
  `design_status` varchar(50) DEFAULT NULL COMMENT '设计状态',
  `operational_status` varchar(50) DEFAULT NULL COMMENT '运行状态',
  `health_status` varchar(50) DEFAULT NULL COMMENT '健康状态',
  `quality_status` varchar(50) DEFAULT NULL COMMENT '质量状态',
  `criticality_level` varchar(50) DEFAULT NULL COMMENT '关键等级',
  `key_quality_characteristics` text COMMENT '关键质量特性摘要',
  `design_department` varchar(200) DEFAULT NULL COMMENT '设计责任部门',
  `maintenance_department` varchar(200) DEFAULT NULL COMMENT '保障责任部门',
  `interface_doc_id` char(36) DEFAULT NULL COMMENT '接口文件ID',
  `verification_doc_id` char(36) DEFAULT NULL COMMENT '验证/联试文件ID',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`subsystem_id`),
  UNIQUE KEY `uk_subsystem_profile_aircraft_code` (`aircraft_id`,`subsystem_code`),
  UNIQUE KEY `uk_subsystem_profile_bom_node` (`bom_node_id`),
  KEY `idx_subsystem_profile_system` (`system_id`),
  KEY `idx_subsystem_profile_status` (`health_status`,`operational_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='子系统层基础信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_sys_source_system`
--

DROP TABLE IF EXISTS `t1_sys_source_system`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_sys_source_system` (
  `code` varchar(50) NOT NULL,
  `name` varchar(200) NOT NULL,
  `description` text,
  `is_active` tinyint(1) DEFAULT '1',
  `sort_order` int DEFAULT '0',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_system_integration_log`
--

DROP TABLE IF EXISTS `t1_system_integration_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_system_integration_log` (
  `log_id` char(36) NOT NULL DEFAULT (uuid()),
  `source_system` varchar(50) NOT NULL,
  `target_system` varchar(50) NOT NULL DEFAULT 'DOSSIER',
  `message_type` varchar(200) DEFAULT NULL,
  `processing_status` varchar(50) DEFAULT NULL,
  `error_message` text,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`log_id`),
  CONSTRAINT `system_integration_log_chk_1` CHECK ((`processing_status` in (_utf8mb4'RECEIVED',_utf8mb4'PROCESSING',_utf8mb4'SUCCESS',_utf8mb4'FAILED',_utf8mb4'RETRYING',_utf8mb4'DISCARDED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_system_object_profile`
--

DROP TABLE IF EXISTS `t1_system_object_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_system_object_profile` (
  `system_id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '系统对象ID',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '统一对象ID',
  `aircraft_id` char(36) NOT NULL COMMENT '所属飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  `system_code` varchar(100) NOT NULL COMMENT '系统编码',
  `system_name` varchar(300) NOT NULL COMMENT '系统名称',
  `system_name_en` varchar(300) DEFAULT NULL COMMENT '英文名称',
  `ata_chapter` varchar(20) DEFAULT NULL COMMENT 'ATA章节',
  `sns_system_code` varchar(100) DEFAULT NULL COMMENT 'SNS系统码',
  `system_category` varchar(100) DEFAULT NULL COMMENT '系统分类',
  `function_summary` text COMMENT '功能摘要',
  `system_boundary` text COMMENT '系统边界',
  `covered_subsystems` text COMMENT '下级子系统摘要',
  `main_interfaces` text COMMENT '主要接口摘要',
  `redundancy_summary` text COMMENT '余度摘要',
  `configuration_baseline` varchar(200) DEFAULT NULL COMMENT '系统构型基线',
  `effectivity` json NOT NULL DEFAULT (json_object()) COMMENT '适用性',
  `design_status` varchar(50) DEFAULT NULL COMMENT '设计状态',
  `technical_status` varchar(50) DEFAULT NULL COMMENT '技术状态',
  `safety_classification` varchar(50) DEFAULT NULL COMMENT '安全等级',
  `criticality_level` varchar(50) DEFAULT NULL COMMENT '关键等级',
  `quality_status` varchar(50) DEFAULT NULL COMMENT '质量状态',
  `design_department` varchar(200) DEFAULT NULL COMMENT '设计责任部门',
  `maintenance_department` varchar(200) DEFAULT NULL COMMENT '维修/保障责任部门',
  `system_owner` varchar(200) DEFAULT NULL COMMENT '系统负责人/管理单位',
  `operational_status` varchar(50) DEFAULT NULL COMMENT '运行状态',
  `open_fault_count` int NOT NULL DEFAULT '0' COMMENT '未关闭故障数',
  `latest_status_date` datetime(6) DEFAULT NULL COMMENT '最近状态更新时间',
  `main_design_doc_id` char(36) DEFAULT NULL COMMENT '主设计文件ID',
  `main_maintenance_doc_id` char(36) DEFAULT NULL COMMENT '主维护文件ID',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`system_id`),
  UNIQUE KEY `uk_system_profile_aircraft_code` (`aircraft_id`,`system_code`),
  UNIQUE KEY `uk_system_profile_bom_node` (`bom_node_id`),
  KEY `idx_system_profile_ata` (`ata_chapter`),
  KEY `idx_system_profile_status` (`technical_status`,`operational_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统层基础信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_system_record`
--

DROP TABLE IF EXISTS `t1_system_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_system_record` (
  `record_id` char(36) NOT NULL DEFAULT (uuid()),
  `source_system` varchar(50) NOT NULL,
  `source_record_id` varchar(500) NOT NULL,
  `source_record_type` varchar(200) NOT NULL,
  `record_title` varchar(1000) DEFAULT NULL,
  `record_data` json NOT NULL DEFAULT (json_object()),
  `linked_aircraft_id` char(36) DEFAULT NULL,
  `linked_document_id` char(36) DEFAULT NULL,
  `aircraft_tail_number` varchar(50) DEFAULT NULL,
  `aircraft_msn` varchar(100) DEFAULT NULL,
  `synced_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`record_id`),
  UNIQUE KEY `uq_system_record` (`source_system`,`source_record_id`),
  KEY `idx_system_record_aircraft` (`linked_aircraft_id`),
  KEY `idx_system_record_doc` (`linked_document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_technical_file`
--

DROP TABLE IF EXISTS `t1_technical_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_technical_file` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `file_name` varchar(500) NOT NULL,
  `file_type` varchar(50) DEFAULT NULL,
  `reference_type` varchar(30) NOT NULL,
  `reference_id` varchar(200) NOT NULL,
  `file_path` varchar(1000) DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `uploaded_by_id` varchar(50) DEFAULT NULL,
  `uploaded_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `version` varchar(20) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `remarks` text,
  PRIMARY KEY (`id`),
  KEY `idx_technical_file_ref` (`reference_type`,`reference_id`),
  KEY `idx_technical_file_type` (`file_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='技术文件（多态 reference_type + reference_id）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_text_mining_result`
--

DROP TABLE IF EXISTS `t1_text_mining_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_text_mining_result` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '文本挖掘结果ID',
  `task_id` char(36) NOT NULL COMMENT '分析任务ID',
  `source_object_type` varchar(50) NOT NULL COMMENT '来源对象类型',
  `source_object_id` char(36) DEFAULT NULL COMMENT '来源对象ID',
  `source_text_hash` varchar(128) DEFAULT NULL COMMENT '来源文本哈希',
  `keyword` varchar(200) DEFAULT NULL COMMENT '关键词',
  `topic_label` varchar(200) DEFAULT NULL COMMENT '主题标签',
  `entity_type` varchar(50) DEFAULT NULL COMMENT '实体类型',
  `entity_value` varchar(500) DEFAULT NULL COMMENT '实体值',
  `sentiment_label` varchar(50) DEFAULT NULL COMMENT '倾向标签',
  `confidence` decimal(5,4) DEFAULT NULL COMMENT '可信度',
  `evidence_excerpt` text COMMENT '证据片段',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_text_mining_task` (`task_id`),
  KEY `idx_text_mining_source` (`source_object_type`,`source_object_id`),
  KEY `idx_text_mining_keyword` (`keyword`),
  KEY `idx_text_mining_entity` (`entity_type`,`entity_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文本挖掘结果：记录关键词、主题和实体抽取结果';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary view structure for view `t1_v_aircraft_dossier_summary`
--

DROP TABLE IF EXISTS `t1_v_aircraft_dossier_summary`;
/*!50001 DROP VIEW IF EXISTS `t1_v_aircraft_dossier_summary`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `t1_v_aircraft_dossier_summary` AS SELECT 
 1 AS `aircraft_id`,
 1 AS `tail_number`,
 1 AS `msn`,
 1 AS `open_dossier_count`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `t1_v_dossier_instance_aircraft`
--

DROP TABLE IF EXISTS `t1_v_dossier_instance_aircraft`;
/*!50001 DROP VIEW IF EXISTS `t1_v_dossier_instance_aircraft`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `t1_v_dossier_instance_aircraft` AS SELECT 
 1 AS `id`,
 1 AS `template_id`,
 1 AS `aircraft_id`,
 1 AS `current_version_id`,
 1 AS `current_version_no`,
 1 AS `instance_label`,
 1 AS `status`,
 1 AS `instance_options_json`,
 1 AS `created_at`,
 1 AS `updated_at`,
 1 AS `deleted_at`,
 1 AS `tail_number`,
 1 AS `msn`,
 1 AS `aircraft_type`,
 1 AS `variant`,
 1 AS `operational_status`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `t1_v_dossier_instance_manage`
--

DROP TABLE IF EXISTS `t1_v_dossier_instance_manage`;
/*!50001 DROP VIEW IF EXISTS `t1_v_dossier_instance_manage`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `t1_v_dossier_instance_manage` AS SELECT 
 1 AS `instance_id`,
 1 AS `instance_code`,
 1 AS `instance_name`,
 1 AS `instance_label`,
 1 AS `instance_status`,
 1 AS `status_name`,
 1 AS `template_id`,
 1 AS `template_code`,
 1 AS `template_name`,
 1 AS `template_version`,
 1 AS `aircraft_id`,
 1 AS `model_id`,
 1 AS `model_code`,
 1 AS `model_name`,
 1 AS `tail_number`,
 1 AS `msn`,
 1 AS `aircraft_type`,
 1 AS `operational_status`,
 1 AS `current_version_id`,
 1 AS `current_version_no`,
 1 AS `current_version_label`,
 1 AS `major_version_no`,
 1 AS `minor_version_no`,
 1 AS `version_level`,
 1 AS `version_reason`,
 1 AS `generation_job_id`,
 1 AS `generation_job_code`,
 1 AS `generation_job_status`,
 1 AS `generate_time`,
 1 AS `page_count`,
 1 AS `file_count`,
 1 AS `data_record_count`,
 1 AS `document_entry_count`,
 1 AS `content_item_count`,
 1 AS `pdf_file_name`,
 1 AS `zip_file_name`,
 1 AS `created_by`,
 1 AS `updated_by`,
 1 AS `created_at`,
 1 AS `updated_at`,
 1 AS `published_at`,
 1 AS `archived_at`,
 1 AS `deleted_at`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `t1_v_part_instance_on_aircraft`
--

DROP TABLE IF EXISTS `t1_v_part_instance_on_aircraft`;
/*!50001 DROP VIEW IF EXISTS `t1_v_part_instance_on_aircraft`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `t1_v_part_instance_on_aircraft` AS SELECT 
 1 AS `part_instance_id`,
 1 AS `part_number`,
 1 AS `part_name`,
 1 AS `serial_number`,
 1 AS `aircraft_id`,
 1 AS `tail_number`,
 1 AS `installed_system_id`,
 1 AS `system_code`,
 1 AS `system_name`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `t1_v_shop_order_task_status`
--

DROP TABLE IF EXISTS `t1_v_shop_order_task_status`;
/*!50001 DROP VIEW IF EXISTS `t1_v_shop_order_task_status`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `t1_v_shop_order_task_status` AS SELECT 
 1 AS `order_code`,
 1 AS `part_number`,
 1 AS `part_name`,
 1 AS `task_code`,
 1 AS `step_name`,
 1 AS `status`,
 1 AS `assigned_workstation_code`,
 1 AS `assigned_equipment_id`,
 1 AS `assigned_personnel_id`,
 1 AS `planned_finish`,
 1 AS `actual_finish`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `t1_virtual_physical_mapping`
--

DROP TABLE IF EXISTS `t1_virtual_physical_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_virtual_physical_mapping` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '映射ID',
  `mapping_code` varchar(100) NOT NULL COMMENT '映射编码',
  `physical_object_type` varchar(50) NOT NULL COMMENT '实体对象类型：AIRCRAFT/PART_INSTANCE/BOM_NODE/TOOLING等',
  `physical_object_id` char(36) DEFAULT NULL COMMENT '实体对象ID',
  `physical_object_key` varchar(500) DEFAULT NULL COMMENT '实体对象业务键',
  `virtual_object_type` varchar(50) NOT NULL COMMENT '数字对象类型：DOSSIER_ENTRY/STRUCTURE_NODE/DOCUMENT_ENTRY/SYSTEM_RECORD等',
  `virtual_object_id` char(36) DEFAULT NULL COMMENT '数字对象ID',
  `virtual_object_key` varchar(500) DEFAULT NULL COMMENT '数字对象业务键',
  `source_system` varchar(50) DEFAULT NULL COMMENT '来源系统',
  `mapping_status` varchar(24) NOT NULL DEFAULT 'active' COMMENT '映射状态：active/inactive/pending/invalid',
  `confidence` decimal(5,4) DEFAULT NULL COMMENT '映射可信度',
  `valid_from` datetime(6) DEFAULT NULL COMMENT '生效时间',
  `valid_to` datetime(6) DEFAULT NULL COMMENT '失效时间',
  `mapping_rule_json` json NOT NULL DEFAULT (json_object()) COMMENT '映射规则',
  `attrs_json` json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  `created_by` varchar(100) NOT NULL DEFAULT 'system' COMMENT '创建人',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_virtual_physical_mapping_code` (`mapping_code`),
  KEY `idx_vpm_physical` (`physical_object_type`,`physical_object_id`),
  KEY `idx_vpm_virtual` (`virtual_object_type`,`virtual_object_id`),
  KEY `idx_vpm_status` (`mapping_status`),
  KEY `idx_vpm_source_system` (`source_system`),
  CONSTRAINT `chk_vpm_status` CHECK ((`mapping_status` in (_utf8mb4'active',_utf8mb4'inactive',_utf8mb4'pending',_utf8mb4'invalid')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='虚实映射：记录实物对象与数字卷宗对象的对应关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_virtual_physical_mapping_history`
--

DROP TABLE IF EXISTS `t1_virtual_physical_mapping_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_virtual_physical_mapping_history` (
  `id` char(36) NOT NULL DEFAULT (uuid()) COMMENT '历史ID',
  `mapping_id` char(36) NOT NULL COMMENT '映射ID',
  `change_type` varchar(30) NOT NULL COMMENT '变更类型：create/update/activate/deactivate/invalidate',
  `old_status` varchar(24) DEFAULT NULL COMMENT '变更前状态',
  `new_status` varchar(24) DEFAULT NULL COMMENT '变更后状态',
  `old_payload_json` json DEFAULT NULL COMMENT '变更前内容',
  `new_payload_json` json DEFAULT NULL COMMENT '变更后内容',
  `change_reason` text COMMENT '变更原因',
  `changed_by` varchar(100) NOT NULL DEFAULT 'system' COMMENT '变更人',
  `changed_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '变更时间',
  PRIMARY KEY (`id`),
  KEY `idx_vpm_history_mapping` (`mapping_id`),
  KEY `idx_vpm_history_changed_at` (`changed_at`),
  CONSTRAINT `chk_vpm_history_change_type` CHECK ((`change_type` in (_utf8mb4'create',_utf8mb4'update',_utf8mb4'activate',_utf8mb4'deactivate',_utf8mb4'invalidate')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='虚实映射变更历史';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_work_order`
--

DROP TABLE IF EXISTS `t1_work_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_work_order` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `aircraft_id` char(36) NOT NULL,
  `bom_node_id` char(36) DEFAULT NULL COMMENT '对象锚点：BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '对象锚点：实物实例ID',
  `object_level` varchar(30) DEFAULT NULL COMMENT '对象锚点：层级',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '对象锚点：统一对象ID',
  `wo_number` varchar(100) NOT NULL,
  `wo_type` varchar(20) NOT NULL,
  `wo_status` varchar(20) NOT NULL DEFAULT 'OPEN',
  `open_date` datetime(6) NOT NULL,
  `close_date` datetime(6) DEFAULT NULL,
  `station` varchar(20) DEFAULT NULL,
  `mro_org` varchar(200) DEFAULT NULL,
  `mro_approval_number` varchar(100) DEFAULT NULL,
  `wo_open_tsn_fh` decimal(12,2) DEFAULT NULL,
  `wo_open_tsn_fc` int DEFAULT NULL,
  `release_date` datetime(6) DEFAULT NULL,
  `release_by` varchar(100) DEFAULT NULL,
  `crs_number` varchar(200) DEFAULT NULL,
  `remark` text,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `wo_number` (`wo_number`),
  KEY `idx_work_order_aircraft` (`aircraft_id`),
  KEY `idx_work_order_status` (`wo_status`),
  KEY `idx_wo_bom_anchor` (`aircraft_id`,`bom_node_id`),
  KEY `idx_wo_part_anchor` (`part_instance_id`),
  CONSTRAINT `work_order_chk_1` CHECK ((`wo_type` in (_utf8mb4'A_CHECK',_utf8mb4'C_CHECK',_utf8mb4'D_CHECK',_utf8mb4'LINE',_utf8mb4'UNSCHEDULED',_utf8mb4'SB',_utf8mb4'AD'))),
  CONSTRAINT `work_order_chk_2` CHECK ((`wo_status` in (_utf8mb4'OPEN',_utf8mb4'IN_PROGRESS',_utf8mb4'CLOSED',_utf8mb4'DEFERRED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='维修工单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_work_order_task`
--

DROP TABLE IF EXISTS `t1_work_order_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_work_order_task` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `wo_id` char(36) NOT NULL,
  `aircraft_id` char(36) DEFAULT NULL COMMENT '对象锚点：飞机ID',
  `bom_node_id` char(36) DEFAULT NULL COMMENT '对象锚点：BOM节点ID',
  `part_instance_id` char(36) DEFAULT NULL COMMENT '对象锚点：实物实例ID',
  `object_level` varchar(30) DEFAULT NULL COMMENT '对象锚点：层级',
  `object_profile_id` char(36) DEFAULT NULL COMMENT '对象锚点：统一对象ID',
  `node_id` char(36) DEFAULT NULL,
  `task_type` varchar(30) DEFAULT NULL,
  `task_card_number` varchar(100) DEFAULT NULL,
  `task_description` text,
  `performed_by` varchar(100) DEFAULT NULL,
  `inspector_id` varchar(100) DEFAULT NULL,
  `complete_date` datetime(6) DEFAULT NULL,
  `task_result` varchar(20) DEFAULT NULL,
  `finding_remark` text,
  `corrective_action` text,
  `install_removal_id` char(36) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `fault_event_id` char(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_work_order_task_wo` (`wo_id`),
  KEY `idx_work_order_task_node` (`node_id`),
  KEY `idx_work_order_task_fault_event` (`fault_event_id`),
  KEY `idx_wot_bom_anchor` (`aircraft_id`,`bom_node_id`),
  KEY `idx_wot_part_anchor` (`part_instance_id`),
  CONSTRAINT `work_order_task_chk_1` CHECK (((`task_type` is null) or (`task_type` in (_utf8mb4'INSPECTION',_utf8mb4'REPLACEMENT',_utf8mb4'REPAIR',_utf8mb4'OVERHAUL',_utf8mb4'LUBRICATION',_utf8mb4'ADJUSTMENT',_utf8mb4'SB_COMPLIANCE',_utf8mb4'AD_COMPLIANCE')))),
  CONSTRAINT `work_order_task_chk_2` CHECK (((`task_result` is null) or (`task_result` in (_utf8mb4'PASS',_utf8mb4'FAIL',_utf8mb4'DEFER'))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单任务项；可关联 t1_install_removal；可选 fault_event_id 指向统一故障事件';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_work_step_detail`
--

DROP TABLE IF EXISTS `t1_work_step_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_work_step_detail` (
  `id` char(36) NOT NULL DEFAULT (uuid()),
  `step_code` varchar(150) NOT NULL,
  `detail_seq` int NOT NULL,
  `detail_name` varchar(200) NOT NULL,
  `detail_description` text,
  `standard_time_min` decimal(10,2) DEFAULT NULL,
  `detail_type` varchar(30) DEFAULT NULL,
  `is_critical` tinyint(1) NOT NULL DEFAULT '0',
  `mandatory` tinyint(1) NOT NULL DEFAULT '1',
  `setup_time_min` decimal(10,2) DEFAULT NULL,
  `teardown_time_min` decimal(10,2) DEFAULT NULL,
  `reference_document` varchar(200) DEFAULT NULL,
  `required_tool_id` char(36) DEFAULT NULL,
  `detail_attrs_json` json NOT NULL DEFAULT (json_object()),
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `step_code` (`step_code`,`detail_seq`),
  KEY `idx_work_step_detail_step` (`step_code`),
  KEY `fk_work_step_detail_required_tool` (`required_tool_id`),
  CONSTRAINT `fk_work_step_detail_required_tool` FOREIGN KEY (`required_tool_id`) REFERENCES `t1_physical_tooling` (`id`),
  CONSTRAINT `work_step_detail_chk_1` CHECK ((`detail_type` in (_utf8mb4'PREPARATION',_utf8mb4'MACHINING',_utf8mb4'ASSEMBLY',_utf8mb4'INSPECTION',_utf8mb4'TEST',_utf8mb4'MATERIAL_HANDLING',_utf8mb4'SPECIAL_PROCESS',_utf8mb4'CLEANING')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工序下工步明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t1_workstation`
--

DROP TABLE IF EXISTS `t1_workstation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t1_workstation` (
  `workstation_code` varchar(20) NOT NULL,
  `workstation_name` varchar(200) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `location` varchar(100) DEFAULT NULL,
  `per_capacity` int DEFAULT '0',
  `equ_capacity` int DEFAULT '0',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`workstation_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='MES 工位/工作中心';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'ry-cloud'
--

--
-- Dumping routines for database 'ry-cloud'
--

--
-- Current Database: `ry-cloud`
--

USE `ry-cloud`;

--
-- Final view structure for view `t1_v_aircraft_dossier_summary`
--

/*!50001 DROP VIEW IF EXISTS `t1_v_aircraft_dossier_summary`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 SQL SECURITY DEFINER */
/*!50001 VIEW `t1_v_aircraft_dossier_summary` AS select `pa`.`id` AS `aircraft_id`,`pa`.`tail_number` AS `tail_number`,`pa`.`msn` AS `msn`,sum((case when (`di`.`deleted_at` is null) then 1 else 0 end)) AS `open_dossier_count` from (`t1_physical_aircraft` `pa` left join `t1_dossier_instance` `di` on((`di`.`aircraft_id` = `pa`.`id`))) group by `pa`.`id`,`pa`.`tail_number`,`pa`.`msn` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `t1_v_dossier_instance_aircraft`
--

/*!50001 DROP VIEW IF EXISTS `t1_v_dossier_instance_aircraft`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 SQL SECURITY DEFINER */
/*!50001 VIEW `t1_v_dossier_instance_aircraft` AS select `di`.`id` AS `id`,`di`.`template_id` AS `template_id`,`di`.`aircraft_id` AS `aircraft_id`,`di`.`current_version_id` AS `current_version_id`,`di`.`current_version_no` AS `current_version_no`,`di`.`instance_label` AS `instance_label`,`di`.`status` AS `status`,`di`.`instance_options_json` AS `instance_options_json`,`di`.`created_at` AS `created_at`,`di`.`updated_at` AS `updated_at`,`di`.`deleted_at` AS `deleted_at`,`pa`.`tail_number` AS `tail_number`,`pa`.`msn` AS `msn`,`pa`.`aircraft_type` AS `aircraft_type`,`pa`.`variant` AS `variant`,`pa`.`operational_status` AS `operational_status` from (`t1_dossier_instance` `di` join `t1_physical_aircraft` `pa` on((`di`.`aircraft_id` = `pa`.`id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `t1_v_dossier_instance_manage`
--

/*!50001 DROP VIEW IF EXISTS `t1_v_dossier_instance_manage`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 SQL SECURITY DEFINER */
/*!50001 VIEW `t1_v_dossier_instance_manage` AS select `di`.`id` AS `instance_id`,`di`.`instance_code` AS `instance_code`,`di`.`instance_name` AS `instance_name`,`di`.`instance_label` AS `instance_label`,`di`.`status` AS `instance_status`,(case when (`lj`.`job_status` = 'failed') then '生成失败' when ((`lj`.`job_status` in ('queued','running')) or (`di`.`status` = 'building')) then '生成中' when (`di`.`status` = 'published') then '已发布' when (`di`.`status` = 'archived') then '已归档' when (`di`.`status` = 'ready') then '已生成' else '草稿' end) AS `status_name`,`di`.`template_id` AS `template_id`,`dt`.`template_code` AS `template_code`,`dt`.`name` AS `template_name`,`cv`.`template_version` AS `template_version`,`di`.`aircraft_id` AS `aircraft_id`,`pa`.`model_id` AS `model_id`,`am`.`model_code` AS `model_code`,`am`.`name` AS `model_name`,`pa`.`tail_number` AS `tail_number`,`pa`.`msn` AS `msn`,`pa`.`aircraft_type` AS `aircraft_type`,`pa`.`operational_status` AS `operational_status`,`cv`.`id` AS `current_version_id`,`cv`.`version_no` AS `current_version_no`,`cv`.`version_label` AS `current_version_label`,`cv`.`major_version_no` AS `major_version_no`,`cv`.`minor_version_no` AS `minor_version_no`,`cv`.`version_level` AS `version_level`,`cv`.`version_reason` AS `version_reason`,coalesce(`lj`.`id`,`vj`.`id`) AS `generation_job_id`,coalesce(`lj`.`job_code`,`vj`.`job_code`) AS `generation_job_code`,coalesce(`lj`.`job_status`,`vj`.`job_status`) AS `generation_job_status`,coalesce((case when (`lj`.`job_status` = 'succeeded') then `lj`.`finished_at` else NULL end),`vj`.`finished_at`,`cv`.`created_at`,`di`.`updated_at`,`di`.`created_at`) AS `generate_time`,coalesce(cast(nullif(json_unquote(json_extract((case when (`lj`.`job_status` = 'succeeded') then `lj`.`output_json` else NULL end),'$.pageCount')),'null') as unsigned),cast(nullif(json_unquote(json_extract(`vj`.`output_json`,'$.pageCount')),'null') as unsigned),cast(nullif(json_unquote(json_extract((case when (`lj`.`job_status` = 'succeeded') then `lj`.`result_summary_json` else NULL end),'$.pageCount')),'null') as unsigned),cast(nullif(json_unquote(json_extract(`vj`.`result_summary_json`,'$.pageCount')),'null') as unsigned),0) AS `page_count`,coalesce(cast(nullif(json_unquote(json_extract((case when (`lj`.`job_status` = 'succeeded') then `lj`.`output_json` else NULL end),'$.fileCount')),'null') as unsigned),cast(nullif(json_unquote(json_extract(`vj`.`output_json`,'$.fileCount')),'null') as unsigned),cast(nullif(json_unquote(json_extract((case when (`lj`.`job_status` = 'succeeded') then `lj`.`result_summary_json` else NULL end),'$.fileCount')),'null') as unsigned),cast(nullif(json_unquote(json_extract(`vj`.`result_summary_json`,'$.fileCount')),'null') as unsigned),`efa`.`export_file_count`,`doc`.`document_entry_count`,0) AS `file_count`,coalesce(cast(nullif(json_unquote(json_extract((case when (`lj`.`job_status` = 'succeeded') then `lj`.`output_json` else NULL end),'$.sourceRecordCount')),'null') as unsigned),cast(nullif(json_unquote(json_extract(`vj`.`output_json`,'$.sourceRecordCount')),'null') as unsigned),cast(nullif(json_unquote(json_extract((case when (`lj`.`job_status` = 'succeeded') then `lj`.`result_summary_json` else NULL end),'$.sourceRecordCount')),'null') as unsigned),cast(nullif(json_unquote(json_extract(`vj`.`result_summary_json`,'$.sourceRecordCount')),'null') as unsigned),`ci`.`content_item_count`,0) AS `data_record_count`,`doc`.`document_entry_count` AS `document_entry_count`,`ci`.`content_item_count` AS `content_item_count`,coalesce(json_unquote(json_extract((case when (`lj`.`job_status` = 'succeeded') then `lj`.`output_json` else NULL end),'$.fileName')),json_unquote(json_extract(`vj`.`output_json`,'$.fileName')),`efa`.`pdf_file_name`) AS `pdf_file_name`,coalesce(json_unquote(json_extract((case when (`lj`.`job_status` = 'succeeded') then `lj`.`output_json` else NULL end),'$.packageName')),json_unquote(json_extract(`vj`.`output_json`,'$.packageName')),`efa`.`zip_file_name`) AS `zip_file_name`,`di`.`created_by` AS `created_by`,`di`.`updated_by` AS `updated_by`,`di`.`created_at` AS `created_at`,`di`.`updated_at` AS `updated_at`,`di`.`published_at` AS `published_at`,`di`.`archived_at` AS `archived_at`,`di`.`deleted_at` AS `deleted_at` from ((((((((((`t1_dossier_instance` `di` left join (select `v1`.`id` AS `id`,`v1`.`dossier_instance_id` AS `dossier_instance_id`,`v1`.`template_id` AS `template_id`,`v1`.`template_code` AS `template_code`,`v1`.`template_version` AS `template_version`,`v1`.`template_snapshot_json` AS `template_snapshot_json`,`v1`.`version_no` AS `version_no`,`v1`.`version_label` AS `version_label`,`v1`.`major_version_no` AS `major_version_no`,`v1`.`minor_version_no` AS `minor_version_no`,`v1`.`version_level` AS `version_level`,`v1`.`previous_version_id` AS `previous_version_id`,`v1`.`generation_job_id` AS `generation_job_id`,`v1`.`data_snapshot_id` AS `data_snapshot_id`,`v1`.`version_reason` AS `version_reason`,`v1`.`is_current` AS `is_current`,`v1`.`change_summary` AS `change_summary`,`v1`.`content_hash` AS `content_hash`,`v1`.`content_summary_json` AS `content_summary_json`,`v1`.`generation_params_json` AS `generation_params_json`,`v1`.`created_by` AS `created_by`,`v1`.`created_at` AS `created_at`,`v1`.`published_at` AS `published_at` from (`t1_dossier_version` `v1` join (select `t1_dossier_version`.`dossier_instance_id` AS `dossier_instance_id`,max(`t1_dossier_version`.`version_no`) AS `max_version_no` from `t1_dossier_version` group by `t1_dossier_version`.`dossier_instance_id`) `vm` on(((`vm`.`dossier_instance_id` = `v1`.`dossier_instance_id`) and (`vm`.`max_version_no` = `v1`.`version_no`))))) `lv` on((`lv`.`dossier_instance_id` = `di`.`id`))) left join `t1_dossier_version` `cv` on(((`cv`.`id` = `di`.`current_version_id`) or ((`di`.`current_version_id` is null) and (`cv`.`id` = `lv`.`id`))))) left join `t1_generation_job` `vj` on((`vj`.`id` = `cv`.`generation_job_id`))) left join (select `gj1`.`id` AS `id`,`gj1`.`job_code` AS `job_code`,`gj1`.`job_type` AS `job_type`,`gj1`.`dossier_instance_id` AS `dossier_instance_id`,`gj1`.`dossier_version_id` AS `dossier_version_id`,`gj1`.`precheck_run_id` AS `precheck_run_id`,`gj1`.`job_status` AS `job_status`,`gj1`.`current_stage` AS `current_stage`,`gj1`.`progress_percent` AS `progress_percent`,`gj1`.`pull_strategy_json` AS `pull_strategy_json`,`gj1`.`generator_params_json` AS `generator_params_json`,`gj1`.`algorithm_code` AS `algorithm_code`,`gj1`.`algorithm_version` AS `algorithm_version`,`gj1`.`source_system` AS `source_system`,`gj1`.`requested_by` AS `requested_by`,`gj1`.`started_at` AS `started_at`,`gj1`.`finished_at` AS `finished_at`,`gj1`.`error_message` AS `error_message`,`gj1`.`result_summary_json` AS `result_summary_json`,`gj1`.`output_json` AS `output_json`,`gj1`.`created_at` AS `created_at` from (`t1_generation_job` `gj1` join (select `t1_generation_job`.`dossier_instance_id` AS `dossier_instance_id`,max(`t1_generation_job`.`created_at`) AS `max_created_at` from `t1_generation_job` where (`t1_generation_job`.`job_type` in ('generate','regenerate')) group by `t1_generation_job`.`dossier_instance_id`) `gm` on(((`gm`.`dossier_instance_id` = `gj1`.`dossier_instance_id`) and (`gm`.`max_created_at` = `gj1`.`created_at`)))) where (`gj1`.`job_type` in ('generate','regenerate'))) `lj` on((`lj`.`dossier_instance_id` = `di`.`id`))) left join `t1_physical_aircraft` `pa` on((`pa`.`id` = `di`.`aircraft_id`))) left join `t1_ac_model` `am` on((`am`.`id` = `pa`.`model_id`))) left join `t1_dossier_template` `dt` on((`dt`.`id` = `di`.`template_id`))) left join (select `t1_document_entry`.`dossier_version_id` AS `dossier_version_id`,count(1) AS `document_entry_count` from `t1_document_entry` where (`t1_document_entry`.`included_flag` = 1) group by `t1_document_entry`.`dossier_version_id`) `doc` on((`doc`.`dossier_version_id` = `cv`.`id`))) left join (select `t1_dossier_content_item`.`dossier_version_id` AS `dossier_version_id`,count(1) AS `content_item_count` from `t1_dossier_content_item` where (`t1_dossier_content_item`.`included_flag` = 1) group by `t1_dossier_content_item`.`dossier_version_id`) `ci` on((`ci`.`dossier_version_id` = `cv`.`id`))) left join (select `ej`.`dossier_instance_id` AS `dossier_instance_id`,`ej`.`dossier_version_id` AS `dossier_version_id`,count(`ef`.`id`) AS `export_file_count`,max((case when (`ef`.`file_role` = 'main_pdf') then `ef`.`file_name` else NULL end)) AS `pdf_file_name`,max((case when (`ef`.`file_role` = 'attachment_zip') then `ef`.`file_name` else NULL end)) AS `zip_file_name` from (`t1_dossier_export_job` `ej` left join `t1_dossier_export_file` `ef` on((`ef`.`export_job_id` = `ej`.`id`))) where (`ej`.`export_status` = 'succeeded') group by `ej`.`dossier_instance_id`,`ej`.`dossier_version_id`) `efa` on(((`efa`.`dossier_instance_id` = `di`.`id`) and (`efa`.`dossier_version_id` = `cv`.`id`)))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `t1_v_part_instance_on_aircraft`
--

/*!50001 DROP VIEW IF EXISTS `t1_v_part_instance_on_aircraft`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 SQL SECURITY DEFINER */
/*!50001 VIEW `t1_v_part_instance_on_aircraft` AS select `n`.`id` AS `part_instance_id`,`n`.`part_number` AS `part_number`,`n`.`part_name` AS `part_name`,`n`.`serial_number` AS `serial_number`,`n`.`aircraft_id` AS `aircraft_id`,`pa`.`tail_number` AS `tail_number`,cast(NULL as char(36) charset utf8mb4) AS `installed_system_id`,cast(NULL as char(200) charset utf8mb4) AS `system_code`,cast(NULL as char(500) charset utf8mb4) AS `system_name` from (`t1_aircraft_bom_node` `n` join `t1_physical_aircraft` `pa` on((`n`.`aircraft_id` = `pa`.`id`))) where (`n`.`node_type` in ('PART','CONSUMABLE','SUBASSY','ASSEMBLY')) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `t1_v_shop_order_task_status`
--

/*!50001 DROP VIEW IF EXISTS `t1_v_shop_order_task_status`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 SQL SECURITY DEFINER */
/*!50001 VIEW `t1_v_shop_order_task_status` AS select `so`.`order_code` AS `order_code`,`so`.`part_number` AS `part_number`,`pm`.`part_name` AS `part_name`,`sot`.`task_code` AS `task_code`,`ps`.`step_name` AS `step_name`,`sot`.`status` AS `status`,`sot`.`assigned_workstation_code` AS `assigned_workstation_code`,`sot`.`assigned_equipment_id` AS `assigned_equipment_id`,`sot`.`assigned_personnel_id` AS `assigned_personnel_id`,`sot`.`planned_finish` AS `planned_finish`,`sot`.`actual_finish` AS `actual_finish` from (((`t1_shop_order_task` `sot` join `t1_shop_order` `so` on((`sot`.`shop_order_id` = `so`.`id`))) join `t1_process_step` `ps` on((`sot`.`step_code` = `ps`.`step_code`))) join `t1_part_master` `pm` on((`so`.`part_number` = `pm`.`part_number`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-08 16:17:40
