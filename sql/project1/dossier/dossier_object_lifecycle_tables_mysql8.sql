-- =============================================================================
-- Digital dossier object and lifecycle completion tables - MySQL 8.0
-- Purpose: complete six-level object storage and lifecycle-stage data anchors
-- for aircraft, system, subsystem, equipment, component and part dossiers.
-- =============================================================================

SET NAMES utf8mb4;
USE `ry-cloud`;

-- -----------------------------------------------------------------------------
-- Helper procedures: safe column/index completion for existing tables.
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;

DELIMITER //

CREATE PROCEDURE add_column_if_missing(
  IN p_table_name varchar(128),
  IN p_column_name varchar(128),
  IN p_column_definition text
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND column_name = p_column_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN ', p_column_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

CREATE PROCEDURE add_index_if_missing(
  IN p_table_name varchar(128),
  IN p_index_name varchar(128),
  IN p_index_definition text
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND index_name = p_index_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ', p_index_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

DELIMITER ;

-- -----------------------------------------------------------------------------
-- 1. Unified object anchor: one searchable identity for all six product levels.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_product_object_profile (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '对象ID，可与BOM节点或实物实例对齐',
  aircraft_id char(36) DEFAULT NULL COMMENT '所属飞机ID',
  aircraft_no varchar(50) DEFAULT NULL COMMENT '所属机号',
  object_level varchar(30) NOT NULL COMMENT '层级：aircraft/system/subsystem/equipment/component/part',
  object_code varchar(200) NOT NULL COMMENT '对象编码：机号、系统码、件号、实例编码等',
  object_name varchar(500) NOT NULL COMMENT '对象名称',
  object_name_en varchar(500) DEFAULT NULL COMMENT '英文名称',
  object_type varchar(100) DEFAULT NULL COMMENT '对象类型',
  parent_object_id char(36) DEFAULT NULL COMMENT '上级对象ID',
  parent_object_code varchar(200) DEFAULT NULL COMMENT '上级对象编码',
  root_aircraft_id char(36) DEFAULT NULL COMMENT '根整机ID',
  node_path varchar(1000) DEFAULT NULL COMMENT '层级路径',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  part_master_id varchar(200) DEFAULT NULL COMMENT '件号主数据ID/件号',
  part_instance_id char(36) DEFAULT NULL COMMENT '实物实例ID',
  part_number varchar(200) DEFAULT NULL COMMENT '件号',
  serial_number varchar(200) DEFAULT NULL COMMENT '序列号',
  batch_number varchar(200) DEFAULT NULL COMMENT '批次号',
  position_code varchar(100) DEFAULT NULL COMMENT '位号/安装位置编码',
  ata_code varchar(20) DEFAULT NULL COMMENT 'ATA编码',
  sns_code varchar(100) DEFAULT NULL COMMENT 'SNS编码',
  configuration_status varchar(50) DEFAULT NULL COMMENT '构型状态',
  effectivity json NOT NULL DEFAULT (json_object()) COMMENT '适用性',
  baseline_code varchar(200) DEFAULT NULL COMMENT '构型/技术状态基线',
  lifecycle_status varchar(50) DEFAULT NULL COMMENT '生命周期状态',
  quality_status varchar(50) DEFAULT NULL COMMENT '质量状态',
  operational_status varchar(50) DEFAULT NULL COMMENT '运行状态',
  responsible_department varchar(200) DEFAULT NULL COMMENT '责任部门',
  owner_org varchar(200) DEFAULT NULL COMMENT '归属单位',
  supplier_name varchar(200) DEFAULT NULL COMMENT '供应商/制造商',
  criticality_level varchar(50) DEFAULT NULL COMMENT '关键等级',
  maintainability_type varchar(50) DEFAULT NULL COMMENT '维修属性',
  trace_code varchar(200) DEFAULT NULL COMMENT '追溯码',
  source_system varchar(50) DEFAULT NULL COMMENT '来源系统',
  source_table varchar(200) DEFAULT NULL COMMENT '来源表',
  source_record_id char(36) DEFAULT NULL COMMENT '来源记录ID',
  source_record_key varchar(500) DEFAULT NULL COMMENT '来源业务键',
  snapshot_id char(36) DEFAULT NULL COMMENT '快照ID',
  data_status varchar(24) NOT NULL DEFAULT 'active' COMMENT '数据状态',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_object_bom_node (bom_node_id),
  KEY idx_product_object_aircraft_level (aircraft_id, object_level),
  KEY idx_product_object_code (object_code),
  KEY idx_product_object_parent (parent_object_id),
  KEY idx_product_object_part_instance (part_instance_id),
  KEY idx_product_object_source (source_system, source_table, source_record_id),
  CONSTRAINT chk_product_object_level CHECK (object_level IN ('aircraft','system','subsystem','equipment','component','part')),
  CONSTRAINT chk_product_object_data_status CHECK (data_status IN ('active','inactive','archived','missing','corrected'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='六层产品对象统一身份表：整机、系统、子系统、设备、组件、零件';

-- -----------------------------------------------------------------------------
-- 2. Six-level profile tables.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_aircraft_object_profile (
  aircraft_id char(36) NOT NULL COMMENT '飞机ID，对应t1_physical_aircraft.id',
  object_profile_id char(36) DEFAULT NULL COMMENT '统一对象ID',
  aircraft_no varchar(50) NOT NULL COMMENT '机号/运营编号',
  registration_no varchar(50) DEFAULT NULL COMMENT '注册号',
  msn varchar(100) NOT NULL COMMENT '制造序列号',
  line_number varchar(100) DEFAULT NULL COMMENT '生产线序号',
  aircraft_model_id char(36) DEFAULT NULL COMMENT '机型ID',
  aircraft_model_code varchar(64) DEFAULT NULL COMMENT '机型编码',
  aircraft_model_name varchar(200) DEFAULT NULL COMMENT '机型名称',
  aircraft_variant varchar(100) DEFAULT NULL COMMENT '构型改型/批次',
  type_certificate_no varchar(200) DEFAULT NULL COMMENT '型号合格证编号',
  airworthiness_certificate_no varchar(200) DEFAULT NULL COMMENT '单机适航证编号',
  manufacturer varchar(200) DEFAULT NULL COMMENT '制造商',
  final_assembly_site varchar(200) DEFAULT NULL COMMENT '总装地点',
  production_batch varchar(100) DEFAULT NULL COMMENT '生产批次',
  rollout_date date DEFAULT NULL COMMENT '下线日期',
  first_flight_date date DEFAULT NULL COMMENT '首飞日期',
  delivery_date date DEFAULT NULL COMMENT '交付日期',
  acceptance_date date DEFAULT NULL COMMENT '接收/验收日期',
  owner_org varchar(200) DEFAULT NULL COMMENT '所有人/资产归属单位',
  operator_org varchar(200) DEFAULT NULL COMMENT '当前运营方/使用单位',
  base_airport varchar(50) DEFAULT NULL COMMENT '基地机场',
  fleet_code varchar(100) DEFAULT NULL COMMENT '机队编号',
  aircraft_lifecycle_status varchar(50) DEFAULT NULL COMMENT '飞机生命周期状态',
  operational_status varchar(50) DEFAULT NULL COMMENT '运行状态',
  airworthiness_status varchar(50) DEFAULT NULL COMMENT '适航状态',
  delivery_status varchar(50) DEFAULT NULL COMMENT '交付状态',
  current_configuration_baseline varchar(200) DEFAULT NULL COMMENT '当前构型基线',
  current_bom_version varchar(100) DEFAULT NULL COMMENT '当前BOM版本',
  major_system_count int NOT NULL DEFAULT 0 COMMENT '一级系统数量',
  installed_equipment_count int NOT NULL DEFAULT 0 COMMENT '已装机设备数量',
  open_engineering_change_count int NOT NULL DEFAULT 0 COMMENT '未关闭工程更改数量',
  total_flight_hours decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计飞行小时',
  total_flight_cycles int NOT NULL DEFAULT 0 COMMENT '累计飞行循环',
  total_landings int DEFAULT NULL COMMENT '累计起落次数',
  latest_maintenance_date date DEFAULT NULL COMMENT '最近维修日期',
  next_due_maintenance varchar(500) DEFAULT NULL COMMENT '下一次到期维修摘要',
  open_fault_count int NOT NULL DEFAULT 0 COMMENT '未关闭故障数',
  current_dossier_version_id char(36) DEFAULT NULL COMMENT '当前卷宗版本ID',
  data_snapshot_id char(36) DEFAULT NULL COMMENT '数据快照ID',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (aircraft_id),
  UNIQUE KEY uk_aircraft_profile_no (aircraft_no),
  KEY idx_aircraft_profile_model (aircraft_model_id, aircraft_model_code),
  KEY idx_aircraft_profile_status (operational_status, airworthiness_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='整机层基础信息扩展表';

CREATE TABLE IF NOT EXISTS t1_system_object_profile (
  system_id char(36) NOT NULL DEFAULT (uuid()) COMMENT '系统对象ID',
  object_profile_id char(36) DEFAULT NULL COMMENT '统一对象ID',
  aircraft_id char(36) NOT NULL COMMENT '所属飞机ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  system_code varchar(100) NOT NULL COMMENT '系统编码',
  system_name varchar(300) NOT NULL COMMENT '系统名称',
  system_name_en varchar(300) DEFAULT NULL COMMENT '英文名称',
  ata_chapter varchar(20) DEFAULT NULL COMMENT 'ATA章节',
  sns_system_code varchar(100) DEFAULT NULL COMMENT 'SNS系统码',
  system_category varchar(100) DEFAULT NULL COMMENT '系统分类',
  function_summary text COMMENT '功能摘要',
  system_boundary text COMMENT '系统边界',
  covered_subsystems text COMMENT '下级子系统摘要',
  main_interfaces text COMMENT '主要接口摘要',
  redundancy_summary text COMMENT '余度摘要',
  configuration_baseline varchar(200) DEFAULT NULL COMMENT '系统构型基线',
  effectivity json NOT NULL DEFAULT (json_object()) COMMENT '适用性',
  design_status varchar(50) DEFAULT NULL COMMENT '设计状态',
  technical_status varchar(50) DEFAULT NULL COMMENT '技术状态',
  safety_classification varchar(50) DEFAULT NULL COMMENT '安全等级',
  criticality_level varchar(50) DEFAULT NULL COMMENT '关键等级',
  quality_status varchar(50) DEFAULT NULL COMMENT '质量状态',
  design_department varchar(200) DEFAULT NULL COMMENT '设计责任部门',
  maintenance_department varchar(200) DEFAULT NULL COMMENT '维修/保障责任部门',
  system_owner varchar(200) DEFAULT NULL COMMENT '系统负责人/管理单位',
  operational_status varchar(50) DEFAULT NULL COMMENT '运行状态',
  open_fault_count int NOT NULL DEFAULT 0 COMMENT '未关闭故障数',
  latest_status_date datetime(6) DEFAULT NULL COMMENT '最近状态更新时间',
  main_design_doc_id char(36) DEFAULT NULL COMMENT '主设计文件ID',
  main_maintenance_doc_id char(36) DEFAULT NULL COMMENT '主维护文件ID',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (system_id),
  UNIQUE KEY uk_system_profile_aircraft_code (aircraft_id, system_code),
  UNIQUE KEY uk_system_profile_bom_node (bom_node_id),
  KEY idx_system_profile_ata (ata_chapter),
  KEY idx_system_profile_status (technical_status, operational_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统层基础信息表';

CREATE TABLE IF NOT EXISTS t1_subsystem_object_profile (
  subsystem_id char(36) NOT NULL DEFAULT (uuid()) COMMENT '子系统对象ID',
  object_profile_id char(36) DEFAULT NULL COMMENT '统一对象ID',
  aircraft_id char(36) NOT NULL COMMENT '所属飞机ID',
  system_id char(36) DEFAULT NULL COMMENT '所属系统对象ID',
  parent_bom_node_id char(36) DEFAULT NULL COMMENT '上级系统BOM节点ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  subsystem_code varchar(100) NOT NULL COMMENT '子系统编码',
  subsystem_name varchar(300) NOT NULL COMMENT '子系统名称',
  ata_section varchar(20) DEFAULT NULL COMMENT 'ATA子章节',
  sns_subsystem_code varchar(100) DEFAULT NULL COMMENT 'SNS子系统码',
  function_area varchar(200) DEFAULT NULL COMMENT '功能分区',
  function_summary text COMMENT '功能摘要',
  boundary_description text COMMENT '边界说明',
  included_equipment_count int NOT NULL DEFAULT 0 COMMENT '组成设备数量',
  main_interfaces text COMMENT '接口摘要',
  configuration_baseline varchar(200) DEFAULT NULL COMMENT '构型基线',
  effectivity json NOT NULL DEFAULT (json_object()) COMMENT '适用性',
  design_status varchar(50) DEFAULT NULL COMMENT '设计状态',
  operational_status varchar(50) DEFAULT NULL COMMENT '运行状态',
  health_status varchar(50) DEFAULT NULL COMMENT '健康状态',
  quality_status varchar(50) DEFAULT NULL COMMENT '质量状态',
  criticality_level varchar(50) DEFAULT NULL COMMENT '关键等级',
  key_quality_characteristics text COMMENT '关键质量特性摘要',
  design_department varchar(200) DEFAULT NULL COMMENT '设计责任部门',
  maintenance_department varchar(200) DEFAULT NULL COMMENT '保障责任部门',
  interface_doc_id char(36) DEFAULT NULL COMMENT '接口文件ID',
  verification_doc_id char(36) DEFAULT NULL COMMENT '验证/联试文件ID',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (subsystem_id),
  UNIQUE KEY uk_subsystem_profile_aircraft_code (aircraft_id, subsystem_code),
  UNIQUE KEY uk_subsystem_profile_bom_node (bom_node_id),
  KEY idx_subsystem_profile_system (system_id),
  KEY idx_subsystem_profile_status (health_status, operational_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='子系统层基础信息表';

CREATE TABLE IF NOT EXISTS t1_equipment_object_master (
  equipment_master_id char(36) NOT NULL DEFAULT (uuid()) COMMENT '设备主数据ID',
  equipment_code varchar(100) NOT NULL COMMENT '设备编码',
  equipment_name varchar(300) NOT NULL COMMENT '设备名称',
  equipment_type varchar(100) DEFAULT NULL COMMENT '设备类型',
  part_number varchar(200) DEFAULT NULL COMMENT '设备件号/型号',
  model_spec varchar(200) DEFAULT NULL COMMENT '规格型号',
  manufacturer varchar(200) DEFAULT NULL COMMENT '制造商',
  supplier_name varchar(200) DEFAULT NULL COMMENT '供应商',
  supplier_code varchar(100) DEFAULT NULL COMMENT '供应商代码',
  configuration_item_flag tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否构型项',
  maintainability_type varchar(50) DEFAULT NULL COMMENT '维修属性',
  life_limit varchar(200) DEFAULT NULL COMMENT '寿命限制摘要',
  tbo varchar(200) DEFAULT NULL COMMENT '翻修间隔摘要',
  criticality_level varchar(50) DEFAULT NULL COMMENT '关键等级',
  failure_effect_summary text COMMENT '失效影响摘要',
  certificate_doc_id char(36) DEFAULT NULL COMMENT '合格/放行证明文件ID',
  test_report_doc_id char(36) DEFAULT NULL COMMENT '测试报告文件ID',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (equipment_master_id),
  UNIQUE KEY uk_equipment_master_code (equipment_code),
  KEY idx_equipment_master_part (part_number),
  KEY idx_equipment_master_type (equipment_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备层主数据表';

CREATE TABLE IF NOT EXISTS t1_equipment_object_instance (
  equipment_instance_id char(36) NOT NULL DEFAULT (uuid()) COMMENT '设备实物实例ID',
  equipment_master_id char(36) DEFAULT NULL COMMENT '设备主数据ID',
  object_profile_id char(36) DEFAULT NULL COMMENT '统一对象ID',
  aircraft_id char(36) DEFAULT NULL COMMENT '所属飞机ID',
  system_id char(36) DEFAULT NULL COMMENT '所属系统ID',
  subsystem_id char(36) DEFAULT NULL COMMENT '所属子系统ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  part_instance_id char(36) DEFAULT NULL COMMENT '关联零件/设备实物ID',
  equipment_code varchar(100) NOT NULL COMMENT '设备编码',
  equipment_name varchar(300) DEFAULT NULL COMMENT '设备名称',
  part_number varchar(200) DEFAULT NULL COMMENT '件号/型号',
  serial_number varchar(200) DEFAULT NULL COMMENT '序列号',
  batch_number varchar(200) DEFAULT NULL COMMENT '批次号',
  software_version varchar(100) DEFAULT NULL COMMENT '软件版本',
  hardware_version varchar(100) DEFAULT NULL COMMENT '硬件版本',
  manufacturer varchar(200) DEFAULT NULL COMMENT '制造商',
  supplier_name varchar(200) DEFAULT NULL COMMENT '供应商',
  supplier_code varchar(100) DEFAULT NULL COMMENT '供应商代码',
  manufacture_date date DEFAULT NULL COMMENT '制造日期',
  delivery_batch varchar(100) DEFAULT NULL COMMENT '交付批次',
  installation_position varchar(200) DEFAULT NULL COMMENT '装机位置',
  position_code varchar(100) DEFAULT NULL COMMENT '位号/站位',
  installation_date date DEFAULT NULL COMMENT '装机日期',
  installation_status varchar(50) DEFAULT NULL COMMENT '装机状态',
  configuration_version varchar(100) DEFAULT NULL COMMENT '构型版本',
  modification_status varchar(50) DEFAULT NULL COMMENT '改装状态',
  effectivity json NOT NULL DEFAULT (json_object()) COMMENT '适用性',
  tsn decimal(12,2) DEFAULT NULL COMMENT '自新品以来时间',
  csn int DEFAULT NULL COMMENT '自新品以来循环',
  tso decimal(12,2) DEFAULT NULL COMMENT '自上次翻修以来时间',
  cso int DEFAULT NULL COMMENT '自上次翻修以来循环',
  operational_status varchar(50) DEFAULT NULL COMMENT '运行状态',
  quality_status varchar(50) DEFAULT NULL COMMENT '质量状态',
  airworthiness_release_status varchar(50) DEFAULT NULL COMMENT '适航放行状态',
  trace_code varchar(200) DEFAULT NULL COMMENT '追溯码',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (equipment_instance_id),
  UNIQUE KEY uk_equipment_instance_bom_node (bom_node_id),
  KEY idx_equipment_instance_master (equipment_master_id),
  KEY idx_equipment_instance_aircraft (aircraft_id),
  KEY idx_equipment_instance_part (part_number, serial_number),
  KEY idx_equipment_instance_status (installation_status, operational_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备层实物实例表';

CREATE TABLE IF NOT EXISTS t1_component_object_master (
  component_master_id char(36) NOT NULL DEFAULT (uuid()) COMMENT '组件主数据ID',
  component_code varchar(100) NOT NULL COMMENT '组件编码',
  component_name varchar(300) NOT NULL COMMENT '组件名称',
  component_type varchar(100) DEFAULT NULL COMMENT '组件类型',
  assembly_part_number varchar(200) DEFAULT NULL COMMENT '装配件号',
  drawing_no varchar(200) DEFAULT NULL COMMENT '装配图号',
  drawing_revision varchar(100) DEFAULT NULL COMMENT '图纸版本',
  manufacturer varchar(200) DEFAULT NULL COMMENT '制造商/装配单位',
  supplier_name varchar(200) DEFAULT NULL COMMENT '供应商',
  repairable_flag tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否可修',
  replaceable_flag tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否可更换',
  life_limit varchar(200) DEFAULT NULL COMMENT '寿命限制摘要',
  criticality_level varchar(50) DEFAULT NULL COMMENT '关键等级',
  key_quality_characteristics text COMMENT '关键质量特性摘要',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (component_master_id),
  UNIQUE KEY uk_component_master_code (component_code),
  KEY idx_component_master_part (assembly_part_number),
  KEY idx_component_master_type (component_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组件层主数据表';

CREATE TABLE IF NOT EXISTS t1_component_object_instance (
  component_instance_id char(36) NOT NULL DEFAULT (uuid()) COMMENT '组件实物实例ID',
  component_master_id char(36) DEFAULT NULL COMMENT '组件主数据ID',
  object_profile_id char(36) DEFAULT NULL COMMENT '统一对象ID',
  aircraft_id char(36) DEFAULT NULL COMMENT '所属飞机ID',
  parent_equipment_instance_id char(36) DEFAULT NULL COMMENT '所属设备实例ID',
  parent_component_instance_id char(36) DEFAULT NULL COMMENT '所属上级组件实例ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  part_instance_id char(36) DEFAULT NULL COMMENT '关联实物件ID',
  component_code varchar(100) NOT NULL COMMENT '组件编码',
  component_name varchar(300) DEFAULT NULL COMMENT '组件名称',
  assembly_part_number varchar(200) DEFAULT NULL COMMENT '装配件号',
  assembly_serial_number varchar(200) DEFAULT NULL COMMENT '组件序列号',
  assembly_batch_number varchar(200) DEFAULT NULL COMMENT '组件批次号',
  assembly_work_order_no varchar(100) DEFAULT NULL COMMENT '装配工单号',
  assembly_date date DEFAULT NULL COMMENT '装配日期',
  assembly_version varchar(100) DEFAULT NULL COMMENT '装配构型版本',
  part_count int NOT NULL DEFAULT 0 COMMENT '下级零件数量',
  key_part_count int NOT NULL DEFAULT 0 COMMENT '关键零件数量',
  installation_position varchar(200) DEFAULT NULL COMMENT '装机位置',
  position_code varchar(100) DEFAULT NULL COMMENT '位号',
  installation_date date DEFAULT NULL COMMENT '装机日期',
  assembly_status varchar(50) DEFAULT NULL COMMENT '装配状态',
  quality_status varchar(50) DEFAULT NULL COMMENT '质量状态',
  operational_status varchar(50) DEFAULT NULL COMMENT '使用状态',
  tsn decimal(12,2) DEFAULT NULL COMMENT '自新品以来时间',
  csn int DEFAULT NULL COMMENT '自新品以来循环',
  trace_code varchar(200) DEFAULT NULL COMMENT '追溯码',
  material_trace_summary text COMMENT '材料/批次追溯摘要',
  assembly_record_doc_id char(36) DEFAULT NULL COMMENT '装配记录文件ID',
  release_certificate_doc_id char(36) DEFAULT NULL COMMENT '放行证明文件ID',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (component_instance_id),
  UNIQUE KEY uk_component_instance_bom_node (bom_node_id),
  KEY idx_component_instance_master (component_master_id),
  KEY idx_component_instance_aircraft (aircraft_id),
  KEY idx_component_instance_part (assembly_part_number, assembly_serial_number),
  KEY idx_component_instance_status (assembly_status, quality_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组件层实物实例表';

-- -----------------------------------------------------------------------------
-- 3. Complete part-level fields on existing part tables.
-- -----------------------------------------------------------------------------
CALL add_column_if_missing('t1_part_master', 'drawing_no', '`drawing_no` varchar(200) DEFAULT NULL COMMENT ''图纸编号'' AFTER `part_name_en`');
CALL add_column_if_missing('t1_part_master', 'drawing_revision', '`drawing_revision` varchar(100) DEFAULT NULL COMMENT ''图纸版本'' AFTER `drawing_no`');
CALL add_column_if_missing('t1_part_master', 'design_revision', '`design_revision` varchar(100) DEFAULT NULL COMMENT ''设计版本'' AFTER `drawing_revision`');
CALL add_column_if_missing('t1_part_master', 'specification', '`specification` varchar(500) DEFAULT NULL COMMENT ''规格型号'' AFTER `design_revision`');
CALL add_column_if_missing('t1_part_master', 'material', '`material` varchar(200) DEFAULT NULL COMMENT ''材料'' AFTER `material_spec`');
CALL add_column_if_missing('t1_part_master', 'material_grade', '`material_grade` varchar(200) DEFAULT NULL COMMENT ''材料牌号'' AFTER `material`');
CALL add_column_if_missing('t1_part_master', 'standard_no', '`standard_no` varchar(200) DEFAULT NULL COMMENT ''标准号/规范号'' AFTER `material_grade`');
CALL add_column_if_missing('t1_part_master', 'substitute_part_number', '`substitute_part_number` varchar(200) DEFAULT NULL COMMENT ''替代件号'' AFTER `alternate_part_number`');
CALL add_column_if_missing('t1_part_master', 'criticality_level', '`criticality_level` varchar(50) DEFAULT NULL COMMENT ''关键等级'' AFTER `is_critical`');
CALL add_column_if_missing('t1_part_master', 'key_quality_characteristics', '`key_quality_characteristics` text COMMENT ''关键质量特性摘要'' AFTER `criticality_level`');
CALL add_column_if_missing('t1_part_master', 'trace_code', '`trace_code` varchar(200) DEFAULT NULL COMMENT ''追溯码'' AFTER `status`');

CALL add_column_if_missing('t1_part_instance', 'lot_number', '`lot_number` varchar(200) DEFAULT NULL COMMENT ''炉批/生产批/供应批号'' AFTER `batch_number`');
CALL add_column_if_missing('t1_part_instance', 'quantity', '`quantity` decimal(12,4) NOT NULL DEFAULT 1.0000 COMMENT ''数量'' AFTER `lot_number`');
CALL add_column_if_missing('t1_part_instance', 'unit', '`unit` varchar(20) NOT NULL DEFAULT ''EA'' COMMENT ''单位'' AFTER `quantity`');
CALL add_column_if_missing('t1_part_instance', 'current_aircraft_id', '`current_aircraft_id` char(36) DEFAULT NULL COMMENT ''当前所属飞机ID'' AFTER `current_node_id`');
CALL add_column_if_missing('t1_part_instance', 'parent_component_instance_id', '`parent_component_instance_id` char(36) DEFAULT NULL COMMENT ''所属组件实例ID'' AFTER `current_aircraft_id`');
CALL add_column_if_missing('t1_part_instance', 'installation_position', '`installation_position` varchar(200) DEFAULT NULL COMMENT ''装机位置'' AFTER `parent_component_instance_id`');
CALL add_column_if_missing('t1_part_instance', 'position_code', '`position_code` varchar(100) DEFAULT NULL COMMENT ''位号/站位'' AFTER `installation_position`');
CALL add_column_if_missing('t1_part_instance', 'installation_date', '`installation_date` date DEFAULT NULL COMMENT ''装机日期'' AFTER `position_code`');
CALL add_column_if_missing('t1_part_instance', 'installation_status', '`installation_status` varchar(50) DEFAULT NULL COMMENT ''装机状态'' AFTER `installation_date`');
CALL add_column_if_missing('t1_part_instance', 'life_limit_value', '`life_limit_value` decimal(12,2) DEFAULT NULL COMMENT ''寿命限制值'' AFTER `installation_status`');
CALL add_column_if_missing('t1_part_instance', 'life_limit_unit', '`life_limit_unit` varchar(30) DEFAULT NULL COMMENT ''寿命单位'' AFTER `life_limit_value`');
CALL add_column_if_missing('t1_part_instance', 'remaining_life_value', '`remaining_life_value` decimal(12,2) DEFAULT NULL COMMENT ''剩余寿命值'' AFTER `life_limit_unit`');
CALL add_column_if_missing('t1_part_instance', 'remaining_life_unit', '`remaining_life_unit` varchar(30) DEFAULT NULL COMMENT ''剩余寿命单位'' AFTER `remaining_life_value`');
CALL add_column_if_missing('t1_part_instance', 'inspection_status', '`inspection_status` varchar(50) DEFAULT NULL COMMENT ''检验状态'' AFTER `remaining_life_unit`');
CALL add_column_if_missing('t1_part_instance', 'release_status', '`release_status` varchar(50) DEFAULT NULL COMMENT ''放行状态'' AFTER `inspection_status`');
CALL add_column_if_missing('t1_part_instance', 'quality_status', '`quality_status` varchar(50) DEFAULT NULL COMMENT ''质量状态'' AFTER `release_status`');
CALL add_column_if_missing('t1_part_instance', 'key_quality_characteristics', '`key_quality_characteristics` text COMMENT ''关键质量特性摘要'' AFTER `quality_status`');
CALL add_column_if_missing('t1_part_instance', 'trace_code', '`trace_code` varchar(200) DEFAULT NULL COMMENT ''实物追溯码'' AFTER `key_quality_characteristics`');

CALL add_index_if_missing('t1_part_master', 'idx_part_master_drawing', 'ADD INDEX `idx_part_master_drawing` (`drawing_no`, `drawing_revision`)');
CALL add_index_if_missing('t1_part_master', 'idx_part_master_criticality', 'ADD INDEX `idx_part_master_criticality` (`criticality_level`)');
CALL add_index_if_missing('t1_part_instance', 'idx_part_instance_aircraft_node', 'ADD INDEX `idx_part_instance_aircraft_node` (`current_aircraft_id`, `current_node_id`)');
CALL add_index_if_missing('t1_part_instance', 'idx_part_instance_quality_release', 'ADD INDEX `idx_part_instance_quality_release` (`quality_status`, `release_status`)');
CALL add_index_if_missing('t1_part_instance', 'idx_part_instance_trace', 'ADD INDEX `idx_part_instance_trace` (`trace_code`)');

-- -----------------------------------------------------------------------------
-- 4. Cross-stage lifecycle, interface, status and quality tables.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_object_lifecycle_record (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '生命周期记录ID',
  object_profile_id char(36) DEFAULT NULL COMMENT '统一对象ID',
  object_level varchar(30) NOT NULL COMMENT '对象层级',
  object_id char(36) DEFAULT NULL COMMENT '对象ID',
  aircraft_id char(36) DEFAULT NULL COMMENT '飞机ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  part_instance_id char(36) DEFAULT NULL COMMENT '实物实例ID',
  lifecycle_stage varchar(30) NOT NULL COMMENT '阶段：design/manufacturing/inspection/installation/service/fault/technical_status/attachment',
  business_record_type varchar(100) NOT NULL COMMENT '业务记录类型',
  business_record_id char(36) DEFAULT NULL COMMENT '业务记录ID',
  business_record_key varchar(500) DEFAULT NULL COMMENT '业务记录业务键',
  event_time datetime(6) DEFAULT NULL COMMENT '事件时间',
  event_title varchar(500) DEFAULT NULL COMMENT '事件标题',
  event_summary text COMMENT '事件摘要',
  result_status varchar(50) DEFAULT NULL COMMENT '结果状态',
  responsible_person_id char(36) DEFAULT NULL COMMENT '责任人ID',
  responsible_org_id char(36) DEFAULT NULL COMMENT '责任单位ID',
  source_system varchar(50) DEFAULT NULL COMMENT '来源系统',
  source_table varchar(200) DEFAULT NULL COMMENT '来源表',
  source_record_id char(36) DEFAULT NULL COMMENT '来源记录ID',
  source_trace_json json NOT NULL DEFAULT (json_object()) COMMENT '来源追踪',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_olr_object (object_level, object_id),
  KEY idx_olr_bom_stage (bom_node_id, lifecycle_stage),
  KEY idx_olr_part_stage (part_instance_id, lifecycle_stage),
  KEY idx_olr_aircraft_time (aircraft_id, event_time),
  KEY idx_olr_source (source_system, source_table, source_record_id),
  CONSTRAINT chk_olr_level CHECK (object_level IN ('aircraft','system','subsystem','equipment','component','part')),
  CONSTRAINT chk_olr_stage CHECK (lifecycle_stage IN ('design','manufacturing','inspection','installation','service','fault','technical_status','attachment'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对象生命周期记录：统一串联各层级各阶段业务记录';

CREATE TABLE IF NOT EXISTS t1_object_data_link (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '数据关联ID',
  object_profile_id char(36) DEFAULT NULL COMMENT '统一对象ID',
  object_level varchar(30) NOT NULL COMMENT '对象层级',
  object_id char(36) DEFAULT NULL COMMENT '对象ID',
  aircraft_id char(36) DEFAULT NULL COMMENT '飞机ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  part_instance_id char(36) DEFAULT NULL COMMENT '实物实例ID',
  data_domain varchar(30) NOT NULL COMMENT '数据域：design/manufacturing/inspection/service/fault/status/document',
  source_system varchar(50) DEFAULT NULL COMMENT '来源系统',
  source_table varchar(200) NOT NULL COMMENT '来源表',
  source_record_id char(36) DEFAULT NULL COMMENT '来源记录ID',
  source_record_key varchar(500) DEFAULT NULL COMMENT '来源业务键',
  relation_type varchar(50) NOT NULL DEFAULT 'related' COMMENT '关系类型',
  required_flag tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否必需',
  included_flag tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否纳入卷宗',
  validity_status varchar(24) NOT NULL DEFAULT 'active' COMMENT '有效状态',
  summary text COMMENT '摘要',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_object_data_link_source (object_level, object_id, data_domain, source_table, source_record_id),
  KEY idx_object_data_link_bom (bom_node_id, data_domain),
  KEY idx_object_data_link_part (part_instance_id, data_domain),
  KEY idx_object_data_link_aircraft (aircraft_id, data_domain),
  KEY idx_object_data_link_source_key (source_table, source_record_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对象与各阶段业务数据的统一关联表';

CREATE TABLE IF NOT EXISTS t1_object_interface (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '接口关系ID',
  interface_code varchar(100) NOT NULL COMMENT '接口编码',
  interface_name varchar(300) NOT NULL COMMENT '接口名称',
  interface_type varchar(50) DEFAULT NULL COMMENT '接口类型：mechanical/electrical/hydraulic/software/data等',
  source_object_level varchar(30) NOT NULL COMMENT '源对象层级',
  source_object_id char(36) DEFAULT NULL COMMENT '源对象ID',
  source_bom_node_id char(36) DEFAULT NULL COMMENT '源BOM节点ID',
  target_object_level varchar(30) DEFAULT NULL COMMENT '目标对象层级',
  target_object_id char(36) DEFAULT NULL COMMENT '目标对象ID',
  target_bom_node_id char(36) DEFAULT NULL COMMENT '目标BOM节点ID',
  aircraft_id char(36) DEFAULT NULL COMMENT '所属飞机ID',
  interface_summary text COMMENT '接口摘要',
  requirement_summary text COMMENT '接口要求摘要',
  control_doc_id char(36) DEFAULT NULL COMMENT '接口控制文件ID',
  maturity_status varchar(50) DEFAULT NULL COMMENT '接口成熟度/状态',
  verification_status varchar(50) DEFAULT NULL COMMENT '验证状态',
  effectivity json NOT NULL DEFAULT (json_object()) COMMENT '适用性',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_object_interface_code (interface_code),
  KEY idx_object_interface_source (source_object_level, source_object_id),
  KEY idx_object_interface_target (target_object_level, target_object_id),
  KEY idx_object_interface_aircraft (aircraft_id, interface_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对象接口关系表：系统、子系统、设备、组件之间的接口';

CREATE TABLE IF NOT EXISTS t1_object_technical_status (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '技术状态ID',
  object_profile_id char(36) DEFAULT NULL COMMENT '统一对象ID',
  object_level varchar(30) NOT NULL COMMENT '对象层级',
  object_id char(36) DEFAULT NULL COMMENT '对象ID',
  aircraft_id char(36) DEFAULT NULL COMMENT '飞机ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  part_instance_id char(36) DEFAULT NULL COMMENT '实物实例ID',
  status_code varchar(100) NOT NULL COMMENT '技术状态编码',
  status_name varchar(300) DEFAULT NULL COMMENT '技术状态名称',
  baseline_id char(36) DEFAULT NULL COMMENT '构型基线ID',
  baseline_code varchar(200) DEFAULT NULL COMMENT '构型基线编码',
  bom_version varchar(100) DEFAULT NULL COMMENT 'BOM版本',
  drawing_revision varchar(100) DEFAULT NULL COMMENT '图纸版本',
  process_revision varchar(100) DEFAULT NULL COMMENT '工艺版本',
  software_version varchar(100) DEFAULT NULL COMMENT '软件版本',
  hardware_version varchar(100) DEFAULT NULL COMMENT '硬件版本',
  modification_status varchar(50) DEFAULT NULL COMMENT '改装状态',
  deviation_status varchar(50) DEFAULT NULL COMMENT '偏离/让步状态',
  verification_status varchar(50) DEFAULT NULL COMMENT '验证状态',
  release_status varchar(50) DEFAULT NULL COMMENT '放行状态',
  effective_from datetime(6) DEFAULT NULL COMMENT '生效时间',
  effective_to datetime(6) DEFAULT NULL COMMENT '失效时间',
  source_system varchar(50) DEFAULT NULL COMMENT '来源系统',
  source_table varchar(200) DEFAULT NULL COMMENT '来源表',
  source_record_id char(36) DEFAULT NULL COMMENT '来源记录ID',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_technical_status_code (status_code),
  KEY idx_technical_status_object (object_level, object_id),
  KEY idx_technical_status_bom (bom_node_id),
  KEY idx_technical_status_part (part_instance_id),
  KEY idx_technical_status_baseline (baseline_id, baseline_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对象技术状态表';

CREATE TABLE IF NOT EXISTS t1_object_status_history (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '状态历史ID',
  object_profile_id char(36) DEFAULT NULL COMMENT '统一对象ID',
  object_level varchar(30) NOT NULL COMMENT '对象层级',
  object_id char(36) DEFAULT NULL COMMENT '对象ID',
  aircraft_id char(36) DEFAULT NULL COMMENT '飞机ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  part_instance_id char(36) DEFAULT NULL COMMENT '实物实例ID',
  status_category varchar(50) NOT NULL COMMENT '状态类别：lifecycle/quality/operation/configuration/release',
  old_status varchar(100) DEFAULT NULL COMMENT '原状态',
  new_status varchar(100) NOT NULL COMMENT '新状态',
  change_time datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '变更时间',
  change_reason text COMMENT '变更原因',
  changed_by varchar(100) DEFAULT NULL COMMENT '变更人',
  source_table varchar(200) DEFAULT NULL COMMENT '来源表',
  source_record_id char(36) DEFAULT NULL COMMENT '来源记录ID',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_status_history_object (object_level, object_id, change_time),
  KEY idx_status_history_bom (bom_node_id, change_time),
  KEY idx_status_history_part (part_instance_id, change_time),
  KEY idx_status_history_category (status_category, new_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对象状态变化历史表';

CREATE TABLE IF NOT EXISTS t1_quality_characteristic (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '质量特性ID',
  characteristic_code varchar(100) NOT NULL COMMENT '特性编码',
  characteristic_name varchar(300) NOT NULL COMMENT '特性名称',
  characteristic_type varchar(50) DEFAULT NULL COMMENT '特性类型：design/process/product/safety/reliability/maintainability',
  object_level varchar(30) DEFAULT NULL COMMENT '适用对象层级',
  object_id char(36) DEFAULT NULL COMMENT '适用对象ID',
  aircraft_id char(36) DEFAULT NULL COMMENT '飞机ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  part_number varchar(200) DEFAULT NULL COMMENT '件号',
  part_instance_id char(36) DEFAULT NULL COMMENT '实物实例ID',
  characteristic_source varchar(100) DEFAULT NULL COMMENT '来源：图纸/规范/工艺/适航/质量计划等',
  key_characteristic_flag tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否关键特性',
  nominal_value varchar(200) DEFAULT NULL COMMENT '名义值',
  upper_limit varchar(200) DEFAULT NULL COMMENT '上限',
  lower_limit varchar(200) DEFAULT NULL COMMENT '下限',
  tolerance varchar(200) DEFAULT NULL COMMENT '公差',
  unit varchar(50) DEFAULT NULL COMMENT '单位',
  inspection_method varchar(500) DEFAULT NULL COMMENT '检验方法',
  control_plan_ref varchar(200) DEFAULT NULL COMMENT '控制计划引用',
  risk_level varchar(50) DEFAULT NULL COMMENT '风险等级',
  is_active tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否有效',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_quality_characteristic_code (characteristic_code),
  KEY idx_quality_characteristic_object (object_level, object_id),
  KEY idx_quality_characteristic_bom (bom_node_id),
  KEY idx_quality_characteristic_part (part_number, part_instance_id),
  KEY idx_quality_characteristic_key (key_characteristic_flag, risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='质量特性定义表';

CREATE TABLE IF NOT EXISTS t1_inspection_plan (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '检验计划ID',
  plan_code varchar(100) NOT NULL COMMENT '计划编码',
  plan_name varchar(300) NOT NULL COMMENT '计划名称',
  object_level varchar(30) DEFAULT NULL COMMENT '适用对象层级',
  object_id char(36) DEFAULT NULL COMMENT '适用对象ID',
  aircraft_id char(36) DEFAULT NULL COMMENT '飞机ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  part_number varchar(200) DEFAULT NULL COMMENT '件号',
  lifecycle_stage varchar(30) DEFAULT NULL COMMENT '适用阶段',
  inspection_stage varchar(50) DEFAULT NULL COMMENT '检验阶段：incoming/first_article/in_process/final/functional/delivery/service',
  inspection_type varchar(50) DEFAULT NULL COMMENT '检验类型',
  sampling_plan varchar(200) DEFAULT NULL COMMENT '抽样方案',
  required_flag tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否必检',
  plan_status varchar(50) NOT NULL DEFAULT 'active' COMMENT '计划状态',
  effective_from date DEFAULT NULL COMMENT '生效日期',
  effective_to date DEFAULT NULL COMMENT '失效日期',
  owner_org_id char(36) DEFAULT NULL COMMENT '责任单位',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_inspection_plan_code (plan_code),
  KEY idx_inspection_plan_object (object_level, object_id),
  KEY idx_inspection_plan_bom (bom_node_id),
  KEY idx_inspection_plan_stage (lifecycle_stage, inspection_stage),
  CONSTRAINT chk_inspection_plan_status CHECK (plan_status IN ('active','inactive','archived'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检验/质量计划表';

CREATE TABLE IF NOT EXISTS t1_nonconformance_record (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '不合格记录ID',
  nc_number varchar(100) NOT NULL COMMENT '不合格编号',
  nc_title varchar(500) DEFAULT NULL COMMENT '不合格标题',
  object_level varchar(30) DEFAULT NULL COMMENT '对象层级',
  object_id char(36) DEFAULT NULL COMMENT '对象ID',
  aircraft_id char(36) DEFAULT NULL COMMENT '飞机ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  part_instance_id char(36) DEFAULT NULL COMMENT '实物实例ID',
  source_stage varchar(30) DEFAULT NULL COMMENT '发生阶段',
  source_table varchar(200) DEFAULT NULL COMMENT '来源表',
  source_record_id char(36) DEFAULT NULL COMMENT '来源记录ID',
  defect_code varchar(100) DEFAULT NULL COMMENT '缺陷代码',
  defect_level varchar(50) DEFAULT NULL COMMENT '缺陷等级',
  description text COMMENT '问题描述',
  severity varchar(50) DEFAULT NULL COMMENT '严重度',
  disposition varchar(50) DEFAULT NULL COMMENT '处置方式：rework/repair/use_as_is/scrap/return',
  status varchar(50) NOT NULL DEFAULT 'open' COMMENT '状态',
  reported_by varchar(100) DEFAULT NULL COMMENT '报告人',
  reported_at datetime(6) DEFAULT NULL COMMENT '报告时间',
  closed_by varchar(100) DEFAULT NULL COMMENT '关闭人',
  closed_at datetime(6) DEFAULT NULL COMMENT '关闭时间',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_nonconformance_number (nc_number),
  KEY idx_nonconformance_object (object_level, object_id),
  KEY idx_nonconformance_bom (bom_node_id),
  KEY idx_nonconformance_part (part_instance_id),
  KEY idx_nonconformance_status (status, severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='不合格/缺陷记录表';

CREATE TABLE IF NOT EXISTS t1_concession_record (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '让步/偏离记录ID',
  concession_number varchar(100) NOT NULL COMMENT '让步/偏离编号',
  concession_type varchar(50) DEFAULT NULL COMMENT '类型：deviation/concession/waiver',
  nc_record_id char(36) DEFAULT NULL COMMENT '关联不合格记录ID',
  object_level varchar(30) DEFAULT NULL COMMENT '对象层级',
  object_id char(36) DEFAULT NULL COMMENT '对象ID',
  aircraft_id char(36) DEFAULT NULL COMMENT '飞机ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  part_instance_id char(36) DEFAULT NULL COMMENT '实物实例ID',
  description text COMMENT '说明',
  approval_status varchar(50) NOT NULL DEFAULT 'draft' COMMENT '审批状态',
  approved_by varchar(100) DEFAULT NULL COMMENT '批准人',
  approved_at datetime(6) DEFAULT NULL COMMENT '批准时间',
  effective_from date DEFAULT NULL COMMENT '生效日期',
  effective_to date DEFAULT NULL COMMENT '失效日期',
  limitation text COMMENT '限制条件',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_concession_number (concession_number),
  KEY idx_concession_nc (nc_record_id),
  KEY idx_concession_object (object_level, object_id),
  KEY idx_concession_status (approval_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='让步接收/偏离批准记录表';

CREATE TABLE IF NOT EXISTS t1_release_record (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '放行记录ID',
  release_number varchar(100) NOT NULL COMMENT '放行编号',
  release_type varchar(50) NOT NULL COMMENT '放行类型：manufacturing/quality/airworthiness/delivery/service',
  object_level varchar(30) DEFAULT NULL COMMENT '对象层级',
  object_id char(36) DEFAULT NULL COMMENT '对象ID',
  aircraft_id char(36) DEFAULT NULL COMMENT '飞机ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  part_instance_id char(36) DEFAULT NULL COMMENT '实物实例ID',
  source_table varchar(200) DEFAULT NULL COMMENT '来源表',
  source_record_id char(36) DEFAULT NULL COMMENT '来源记录ID',
  release_status varchar(50) NOT NULL DEFAULT 'pending' COMMENT '放行状态',
  release_basis text COMMENT '放行依据',
  released_by varchar(100) DEFAULT NULL COMMENT '放行人',
  released_at datetime(6) DEFAULT NULL COMMENT '放行时间',
  certificate_doc_id char(36) DEFAULT NULL COMMENT '证书/文件ID',
  expiry_date date DEFAULT NULL COMMENT '有效期',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_release_number (release_number),
  KEY idx_release_object (object_level, object_id),
  KEY idx_release_bom (bom_node_id),
  KEY idx_release_part (part_instance_id),
  KEY idx_release_status (release_type, release_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='质量/制造/适航/维修放行记录表';

CREATE TABLE IF NOT EXISTS t1_life_usage_record (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '寿命使用记录ID',
  object_profile_id char(36) DEFAULT NULL COMMENT '统一对象ID',
  object_level varchar(30) NOT NULL COMMENT '对象层级',
  object_id char(36) DEFAULT NULL COMMENT '对象ID',
  aircraft_id char(36) DEFAULT NULL COMMENT '飞机ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  part_instance_id char(36) DEFAULT NULL COMMENT '实物实例ID',
  usage_source_type varchar(50) NOT NULL COMMENT '来源类型：flight/operation/maintenance/installation/manual_adjustment',
  usage_source_id char(36) DEFAULT NULL COMMENT '来源记录ID',
  event_time datetime(6) DEFAULT NULL COMMENT '事件时间',
  fh_delta decimal(12,3) NOT NULL DEFAULT 0.000 COMMENT '本次飞行小时增量',
  fc_delta int NOT NULL DEFAULT 0 COMMENT '本次循环增量',
  landing_delta int NOT NULL DEFAULT 0 COMMENT '本次起落增量',
  calendar_days_delta int NOT NULL DEFAULT 0 COMMENT '日历天增量',
  total_fh_after decimal(12,3) DEFAULT NULL COMMENT '累计小时',
  total_fc_after int DEFAULT NULL COMMENT '累计循环',
  remaining_life_value decimal(12,3) DEFAULT NULL COMMENT '剩余寿命',
  remaining_life_unit varchar(30) DEFAULT NULL COMMENT '剩余寿命单位',
  calculated_by varchar(100) DEFAULT NULL COMMENT '计算来源/算法',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_life_usage_object (object_level, object_id, event_time),
  KEY idx_life_usage_bom (bom_node_id, event_time),
  KEY idx_life_usage_part (part_instance_id, event_time),
  KEY idx_life_usage_aircraft (aircraft_id, event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='寿命与使用消耗记录表';

CREATE TABLE IF NOT EXISTS t1_software_load_record (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '软件/参数加载记录ID',
  load_record_no varchar(100) NOT NULL COMMENT '加载记录编号',
  aircraft_id char(36) DEFAULT NULL COMMENT '飞机ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  equipment_instance_id char(36) DEFAULT NULL COMMENT '设备实例ID',
  part_instance_id char(36) DEFAULT NULL COMMENT '实物实例ID',
  software_part_number varchar(200) DEFAULT NULL COMMENT '软件件号',
  software_version varchar(100) DEFAULT NULL COMMENT '软件版本',
  hardware_version varchar(100) DEFAULT NULL COMMENT '硬件版本',
  load_type varchar(50) DEFAULT NULL COMMENT '加载类型：software/firmware/database/parameter',
  load_time datetime(6) DEFAULT NULL COMMENT '加载时间',
  loaded_by varchar(100) DEFAULT NULL COMMENT '加载人',
  verified_by varchar(100) DEFAULT NULL COMMENT '复核人',
  verification_status varchar(50) DEFAULT NULL COMMENT '验证状态',
  source_table varchar(200) DEFAULT NULL COMMENT '来源表',
  source_record_id char(36) DEFAULT NULL COMMENT '来源记录ID',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_software_load_no (load_record_no),
  KEY idx_software_load_equipment (equipment_instance_id),
  KEY idx_software_load_bom (bom_node_id),
  KEY idx_software_load_version (software_part_number, software_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='软件、固件、数据库和参数加载记录表';

CREATE TABLE IF NOT EXISTS t1_certificate_record (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '证书/证明记录ID',
  certificate_no varchar(200) NOT NULL COMMENT '证书编号',
  certificate_type varchar(100) NOT NULL COMMENT '证书类型',
  object_level varchar(30) DEFAULT NULL COMMENT '对象层级',
  object_id char(36) DEFAULT NULL COMMENT '对象ID',
  aircraft_id char(36) DEFAULT NULL COMMENT '飞机ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  part_instance_id char(36) DEFAULT NULL COMMENT '实物实例ID',
  issuing_authority varchar(200) DEFAULT NULL COMMENT '签发机构',
  issue_date date DEFAULT NULL COMMENT '签发日期',
  expiry_date date DEFAULT NULL COMMENT '有效期',
  certificate_status varchar(50) DEFAULT NULL COMMENT '证书状态',
  document_entry_id char(36) DEFAULT NULL COMMENT '卷宗文档条目ID',
  file_storage_key text COMMENT '文件存储地址',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_certificate_no_type (certificate_no, certificate_type),
  KEY idx_certificate_object (object_level, object_id),
  KEY idx_certificate_bom (bom_node_id),
  KEY idx_certificate_part (part_instance_id),
  KEY idx_certificate_status (certificate_type, certificate_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='证书、证明和适航文件记录表';

-- -----------------------------------------------------------------------------
-- 5. Initial synchronization from current BOM and aircraft data.
-- -----------------------------------------------------------------------------
INSERT INTO t1_product_object_profile (
  id, aircraft_id, aircraft_no, object_level, object_code, object_name, object_name_en,
  object_type, parent_object_id, parent_object_code, root_aircraft_id, node_path,
  bom_node_id, part_master_id, part_instance_id, part_number, serial_number, batch_number,
  position_code, ata_code, lifecycle_status, operational_status, supplier_name,
  trace_code, source_system, source_table, source_record_id, source_record_key, data_status
)
SELECT
  abn.id,
  abn.aircraft_id,
  pa.tail_number,
  CASE WHEN UPPER(abn.node_type) = 'CONSUMABLE' THEN 'part' ELSE LOWER(abn.node_type) END,
  COALESCE(NULLIF(abn.part_number, ''), pa.tail_number, abn.id),
  COALESCE(abn.part_name, pa.tail_number, abn.part_number, abn.id),
  abn.part_name_en,
  LOWER(abn.node_type),
  abn.parent_id,
  pabn.part_number,
  abn.aircraft_id,
  NULL,
  abn.id,
  abn.part_number,
  abn.part_instance_id,
  abn.part_number,
  abn.serial_number,
  abn.batch_number,
  abn.position_code,
  abn.ata_chapter,
  CASE WHEN abn.is_active = 1 THEN 'installed' ELSE 'removed' END,
  CASE WHEN abn.is_active = 1 THEN 'active' ELSE 'inactive' END,
  abn.manufacturer,
  COALESCE(abn.serial_number, abn.batch_number, abn.part_number),
  'CONFIG',
  't1_aircraft_bom_node',
  abn.id,
  COALESCE(NULLIF(abn.part_number, ''), abn.id),
  'active'
FROM t1_aircraft_bom_node abn
LEFT JOIN t1_physical_aircraft pa ON pa.id = abn.aircraft_id
LEFT JOIN t1_aircraft_bom_node pabn ON pabn.id = abn.parent_id
WHERE UPPER(abn.node_type) IN ('AIRCRAFT','SYSTEM','SUBSYSTEM','EQUIPMENT','COMPONENT','PART','CONSUMABLE')
ON DUPLICATE KEY UPDATE
  aircraft_no = VALUES(aircraft_no),
  object_name = VALUES(object_name),
  parent_object_id = VALUES(parent_object_id),
  parent_object_code = VALUES(parent_object_code),
  part_instance_id = VALUES(part_instance_id),
  serial_number = VALUES(serial_number),
  batch_number = VALUES(batch_number),
  position_code = VALUES(position_code),
  operational_status = VALUES(operational_status),
  updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_aircraft_object_profile (
  aircraft_id, object_profile_id, aircraft_no, registration_no, msn,
  aircraft_model_id, aircraft_model_code, aircraft_model_name, aircraft_variant,
  manufacturer, delivery_date, operator_org, operational_status,
  total_flight_hours, total_flight_cycles, major_system_count, installed_equipment_count
)
SELECT
  pa.id,
  pop.id,
  pa.tail_number,
  pa.registration_number,
  pa.msn,
  pa.model_id,
  am.model_code,
  am.name,
  pa.variant,
  pa.manufacturer,
  pa.delivery_date,
  pa.current_operator,
  pa.operational_status,
  pa.total_fh,
  pa.total_fc,
  (SELECT COUNT(1) FROM t1_aircraft_bom_node n WHERE n.aircraft_id = pa.id AND UPPER(n.node_type) = 'SYSTEM' AND n.is_active = 1),
  (SELECT COUNT(1) FROM t1_aircraft_bom_node n WHERE n.aircraft_id = pa.id AND UPPER(n.node_type) = 'EQUIPMENT' AND n.is_active = 1)
FROM t1_physical_aircraft pa
LEFT JOIN t1_ac_model am ON am.id = pa.model_id
LEFT JOIN t1_product_object_profile pop ON pop.aircraft_id = pa.id AND pop.object_level = 'aircraft'
ON DUPLICATE KEY UPDATE
  aircraft_no = VALUES(aircraft_no),
  registration_no = VALUES(registration_no),
  msn = VALUES(msn),
  aircraft_model_code = VALUES(aircraft_model_code),
  aircraft_model_name = VALUES(aircraft_model_name),
  operational_status = VALUES(operational_status),
  total_flight_hours = VALUES(total_flight_hours),
  total_flight_cycles = VALUES(total_flight_cycles),
  major_system_count = VALUES(major_system_count),
  installed_equipment_count = VALUES(installed_equipment_count),
  updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_system_object_profile (
  system_id, object_profile_id, aircraft_id, bom_node_id, system_code, system_name,
  system_name_en, ata_chapter, function_summary, operational_status, attrs_json
)
SELECT
  abn.id,
  pop.id,
  abn.aircraft_id,
  abn.id,
  COALESCE(NULLIF(abn.part_number, ''), abn.id),
  COALESCE(abn.part_name, abn.part_number, abn.id),
  abn.part_name_en,
  abn.ata_chapter,
  abn.remark,
  CASE WHEN abn.is_active = 1 THEN 'active' ELSE 'inactive' END,
  json_object('positionCode', abn.position_code, 'manufacturer', abn.manufacturer)
FROM t1_aircraft_bom_node abn
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = abn.id
WHERE UPPER(abn.node_type) = 'SYSTEM'
ON DUPLICATE KEY UPDATE
  system_name = VALUES(system_name),
  system_name_en = VALUES(system_name_en),
  ata_chapter = VALUES(ata_chapter),
  operational_status = VALUES(operational_status),
  updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_subsystem_object_profile (
  subsystem_id, object_profile_id, aircraft_id, parent_bom_node_id, bom_node_id,
  subsystem_code, subsystem_name, ata_section, function_summary, operational_status, attrs_json
)
SELECT
  abn.id,
  pop.id,
  abn.aircraft_id,
  abn.parent_id,
  abn.id,
  COALESCE(NULLIF(abn.part_number, ''), abn.id),
  COALESCE(abn.part_name, abn.part_number, abn.id),
  abn.ata_chapter,
  abn.remark,
  CASE WHEN abn.is_active = 1 THEN 'active' ELSE 'inactive' END,
  json_object('positionCode', abn.position_code, 'manufacturer', abn.manufacturer)
FROM t1_aircraft_bom_node abn
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = abn.id
WHERE UPPER(abn.node_type) = 'SUBSYSTEM'
ON DUPLICATE KEY UPDATE
  subsystem_name = VALUES(subsystem_name),
  ata_section = VALUES(ata_section),
  parent_bom_node_id = VALUES(parent_bom_node_id),
  operational_status = VALUES(operational_status),
  updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_equipment_object_master (
  equipment_code, equipment_name, equipment_type, part_number, manufacturer, attrs_json
)
SELECT DISTINCT
  COALESCE(NULLIF(abn.part_number, ''), abn.id),
  COALESCE(abn.part_name, abn.part_number, abn.id),
  UPPER(abn.node_type),
  NULLIF(abn.part_number, ''),
  abn.manufacturer,
  json_object('ataChapter', abn.ata_chapter)
FROM t1_aircraft_bom_node abn
WHERE UPPER(abn.node_type) = 'EQUIPMENT'
ON DUPLICATE KEY UPDATE
  equipment_name = VALUES(equipment_name),
  part_number = VALUES(part_number),
  manufacturer = VALUES(manufacturer),
  updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_equipment_object_instance (
  equipment_instance_id, equipment_master_id, object_profile_id, aircraft_id, bom_node_id,
  part_instance_id, equipment_code, equipment_name, part_number, serial_number, batch_number,
  manufacturer, installation_position, position_code, installation_date, installation_status,
  tsn, csn, operational_status, trace_code
)
SELECT
  abn.id,
  em.equipment_master_id,
  pop.id,
  abn.aircraft_id,
  abn.id,
  abn.part_instance_id,
  COALESCE(NULLIF(abn.part_number, ''), abn.id),
  COALESCE(abn.part_name, abn.part_number, abn.id),
  NULLIF(abn.part_number, ''),
  abn.serial_number,
  abn.batch_number,
  abn.manufacturer,
  abn.position_desc,
  abn.position_code,
  abn.install_date,
  CASE WHEN abn.is_active = 1 THEN 'installed' ELSE 'removed' END,
  abn.tsn_fh,
  abn.tsn_fc,
  CASE WHEN abn.is_active = 1 THEN 'active' ELSE 'inactive' END,
  COALESCE(abn.serial_number, abn.batch_number, abn.part_number)
FROM t1_aircraft_bom_node abn
LEFT JOIN t1_equipment_object_master em ON em.equipment_code = COALESCE(NULLIF(abn.part_number, ''), abn.id)
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = abn.id
WHERE UPPER(abn.node_type) = 'EQUIPMENT'
ON DUPLICATE KEY UPDATE
  equipment_name = VALUES(equipment_name),
  serial_number = VALUES(serial_number),
  batch_number = VALUES(batch_number),
  installation_status = VALUES(installation_status),
  operational_status = VALUES(operational_status),
  updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_component_object_master (
  component_code, component_name, component_type, assembly_part_number, manufacturer, attrs_json
)
SELECT DISTINCT
  COALESCE(NULLIF(abn.part_number, ''), abn.id),
  COALESCE(abn.part_name, abn.part_number, abn.id),
  UPPER(abn.node_type),
  NULLIF(abn.part_number, ''),
  abn.manufacturer,
  json_object('ataChapter', abn.ata_chapter)
FROM t1_aircraft_bom_node abn
WHERE UPPER(abn.node_type) = 'COMPONENT'
ON DUPLICATE KEY UPDATE
  component_name = VALUES(component_name),
  assembly_part_number = VALUES(assembly_part_number),
  manufacturer = VALUES(manufacturer),
  updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_component_object_instance (
  component_instance_id, component_master_id, object_profile_id, aircraft_id, bom_node_id,
  part_instance_id, component_code, component_name, assembly_part_number, assembly_serial_number,
  assembly_batch_number, installation_position, position_code, installation_date, assembly_status,
  quality_status, operational_status, tsn, csn, trace_code
)
SELECT
  abn.id,
  cm.component_master_id,
  pop.id,
  abn.aircraft_id,
  abn.id,
  abn.part_instance_id,
  COALESCE(NULLIF(abn.part_number, ''), abn.id),
  COALESCE(abn.part_name, abn.part_number, abn.id),
  NULLIF(abn.part_number, ''),
  abn.serial_number,
  abn.batch_number,
  abn.position_desc,
  abn.position_code,
  abn.install_date,
  CASE WHEN abn.is_active = 1 THEN 'installed' ELSE 'removed' END,
  NULL,
  CASE WHEN abn.is_active = 1 THEN 'active' ELSE 'inactive' END,
  abn.tsn_fh,
  abn.tsn_fc,
  COALESCE(abn.serial_number, abn.batch_number, abn.part_number)
FROM t1_aircraft_bom_node abn
LEFT JOIN t1_component_object_master cm ON cm.component_code = COALESCE(NULLIF(abn.part_number, ''), abn.id)
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = abn.id
WHERE UPPER(abn.node_type) = 'COMPONENT'
ON DUPLICATE KEY UPDATE
  component_name = VALUES(component_name),
  assembly_serial_number = VALUES(assembly_serial_number),
  assembly_batch_number = VALUES(assembly_batch_number),
  assembly_status = VALUES(assembly_status),
  operational_status = VALUES(operational_status),
  updated_at = CURRENT_TIMESTAMP(6);

UPDATE t1_part_instance pi
LEFT JOIN t1_aircraft_bom_node abn ON abn.part_instance_id = pi.id
SET
  pi.current_aircraft_id = COALESCE(pi.current_aircraft_id, abn.aircraft_id),
  pi.position_code = COALESCE(pi.position_code, abn.position_code),
  pi.installation_position = COALESCE(pi.installation_position, abn.position_desc),
  pi.installation_date = COALESCE(pi.installation_date, abn.install_date),
  pi.installation_status = COALESCE(pi.installation_status, CASE WHEN abn.is_active = 1 THEN 'installed' ELSE 'removed' END),
  pi.quality_status = COALESCE(pi.quality_status, 'unknown'),
  pi.release_status = COALESCE(pi.release_status, pi.airworthiness_tag_type),
  pi.trace_code = COALESCE(pi.trace_code, pi.serial_number, pi.batch_number)
WHERE abn.id IS NOT NULL;

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;
