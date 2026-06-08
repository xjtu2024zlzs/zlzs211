-- =============================================================================
-- Digital dossier detail feature completion - MySQL 8.0
-- Purpose: support dossier detail page, content items, completeness summary,
-- version diff, and business operation timeline.
-- =============================================================================

SET NAMES utf8mb4;
USE `ry-cloud`;

-- -----------------------------------------------------------------------------
-- 卷宗内容明细：统一承载卷宗详情页中的文档、BOM、工单、故障、证明等内容
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_dossier_content_item (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '内容明细ID',
  dossier_instance_id char(36) NOT NULL COMMENT '卷宗实例ID',
  dossier_version_id char(36) NOT NULL COMMENT '卷宗版本ID',
  structure_node_id char(36) DEFAULT NULL COMMENT '所属目录节点ID',
  item_code varchar(100) NOT NULL COMMENT '内容编码',
  item_name varchar(500) NOT NULL COMMENT '内容名称',
  item_type varchar(50) NOT NULL COMMENT '内容类型：AIRCRAFT/BOM_NODE/PART/DOCUMENT/WORK_ORDER/FAULT/PROOF等',
  lifecycle_stage varchar(30) DEFAULT NULL COMMENT '生命周期阶段：DESIGN/MANUFACTURING/SERVICE/DOSSIER',
  aircraft_id char(36) DEFAULT NULL COMMENT '飞机ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  part_instance_id char(36) DEFAULT NULL COMMENT '零件实例ID',
  is_key_part tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否关重件',
  supply_mode varchar(30) DEFAULT NULL COMMENT '供给方式：self_made/purchased/outsourced/unknown',
  source_system varchar(50) DEFAULT NULL COMMENT '来源系统',
  source_table varchar(200) DEFAULT NULL COMMENT '来源表名',
  source_record_id char(36) DEFAULT NULL COMMENT '来源记录ID',
  source_record_key varchar(500) DEFAULT NULL COMMENT '来源业务键',
  include_design_data tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否纳入设计数据',
  include_manufacturing_data tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否纳入制造数据',
  include_service_data tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否纳入服役数据',
  include_source_proof tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否纳入来源证明',
  required_flag tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否必需',
  included_flag tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否纳入当前卷宗',
  completeness_status varchar(24) NOT NULL DEFAULT 'not_checked' COMMENT '完整性状态：not_checked/complete/warning/missing/error',
  item_status varchar(24) NOT NULL DEFAULT 'active' COMMENT '内容状态：active/excluded/archived',
  sort_order int NOT NULL DEFAULT 0 COMMENT '排序号',
  content_summary text COMMENT '内容摘要',
  file_storage_key text COMMENT '文件存储地址',
  source_trace_json json NOT NULL DEFAULT (json_object()) COMMENT '来源追踪信息',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_dci_version_code (dossier_version_id, item_code),
  KEY idx_dci_instance_version (dossier_instance_id, dossier_version_id),
  KEY idx_dci_structure_node (structure_node_id),
  KEY idx_dci_bom_node (bom_node_id),
  KEY idx_dci_part_instance (part_instance_id),
  KEY idx_dci_source (source_system, source_table, source_record_id),
  KEY idx_dci_key_supply (is_key_part, supply_mode),
  KEY idx_dci_completeness (completeness_status),
  CONSTRAINT chk_dci_completeness CHECK (completeness_status IN ('not_checked','complete','warning','missing','error')),
  CONSTRAINT chk_dci_item_status CHECK (item_status IN ('active','excluded','archived'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗内容明细：统一组织卷宗详情页展示的目录内容和业务来源';

-- -----------------------------------------------------------------------------
-- 卷宗完整性汇总：供详情页快速展示章节、BOM节点、关重件等完整性状态
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_dossier_completeness_summary (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '完整性汇总ID',
  dossier_instance_id char(36) NOT NULL COMMENT '卷宗实例ID',
  dossier_version_id char(36) NOT NULL COMMENT '卷宗版本ID',
  structure_node_id char(36) DEFAULT NULL COMMENT '目录节点ID',
  summary_scope varchar(50) NOT NULL COMMENT '汇总范围：dossier/section/bom_node/key_part/purchased_part',
  subject_type varchar(50) DEFAULT NULL COMMENT '对象类型',
  subject_id char(36) DEFAULT NULL COMMENT '对象ID',
  expected_count int NOT NULL DEFAULT 0 COMMENT '应有数量',
  actual_count int NOT NULL DEFAULT 0 COMMENT '已有数量',
  missing_count int NOT NULL DEFAULT 0 COMMENT '缺失数量',
  warning_count int NOT NULL DEFAULT 0 COMMENT '提醒数量',
  error_count int NOT NULL DEFAULT 0 COMMENT '错误数量',
  completeness_rate decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT '完整率',
  check_status varchar(24) NOT NULL DEFAULT 'not_checked' COMMENT '检查状态：not_checked/pass/warning/fail',
  issue_summary text COMMENT '问题摘要',
  latest_inspection_run_id char(36) DEFAULT NULL COMMENT '最近检查批次ID',
  checked_at datetime(6) DEFAULT NULL COMMENT '检查时间',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_dcs_version_scope_subject (dossier_version_id, summary_scope, subject_type, subject_id),
  KEY idx_dcs_instance_version (dossier_instance_id, dossier_version_id),
  KEY idx_dcs_structure_node (structure_node_id),
  KEY idx_dcs_status (check_status),
  KEY idx_dcs_inspection (latest_inspection_run_id),
  CONSTRAINT chk_dcs_status CHECK (check_status IN ('not_checked','pass','warning','fail'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗完整性汇总：用于详情页快速展示完整率、缺失和异常';

-- -----------------------------------------------------------------------------
-- 卷宗版本差异：支撑详情页版本对比
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_dossier_version_diff (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '版本差异ID',
  dossier_instance_id char(36) NOT NULL COMMENT '卷宗实例ID',
  base_version_id char(36) NOT NULL COMMENT '基准版本ID',
  compare_version_id char(36) NOT NULL COMMENT '对比版本ID',
  diff_type varchar(30) NOT NULL COMMENT '差异类型：added/removed/modified/status_changed/source_changed',
  object_type varchar(50) NOT NULL COMMENT '对象类型',
  object_id char(36) DEFAULT NULL COMMENT '对象ID',
  object_key varchar(500) DEFAULT NULL COMMENT '对象业务键',
  structure_node_id char(36) DEFAULT NULL COMMENT '目录节点ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  field_name varchar(200) DEFAULT NULL COMMENT '变化字段',
  old_value_json json DEFAULT NULL COMMENT '旧值',
  new_value_json json DEFAULT NULL COMMENT '新值',
  impact_level varchar(24) NOT NULL DEFAULT 'normal' COMMENT '影响级别：low/normal/high/critical',
  impact_summary text COMMENT '影响说明',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_dvd_instance (dossier_instance_id),
  KEY idx_dvd_versions (base_version_id, compare_version_id),
  KEY idx_dvd_type (diff_type),
  KEY idx_dvd_object (object_type, object_id),
  KEY idx_dvd_structure_node (structure_node_id),
  KEY idx_dvd_bom_node (bom_node_id),
  CONSTRAINT chk_dvd_diff_type CHECK (diff_type IN ('added','removed','modified','status_changed','source_changed')),
  CONSTRAINT chk_dvd_impact_level CHECK (impact_level IN ('low','normal','high','critical'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗版本差异：记录两个版本之间的内容变化';

-- -----------------------------------------------------------------------------
-- 卷宗业务操作日志：支撑详情页时间线和业务追溯
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_dossier_operation_log (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '操作日志ID',
  dossier_instance_id char(36) NOT NULL COMMENT '卷宗实例ID',
  dossier_version_id char(36) DEFAULT NULL COMMENT '卷宗版本ID',
  operation_type varchar(50) NOT NULL COMMENT '操作类型：create/generate/regenerate/publish/archive/export/update/check等',
  operation_name varchar(200) NOT NULL COMMENT '操作名称',
  operation_status varchar(24) NOT NULL DEFAULT 'succeeded' COMMENT '操作状态：succeeded/failed/running',
  business_subject_type varchar(50) DEFAULT NULL COMMENT '业务对象类型',
  business_subject_id char(36) DEFAULT NULL COMMENT '业务对象ID',
  operator_id varchar(100) DEFAULT NULL COMMENT '操作人ID',
  operator_name varchar(100) DEFAULT NULL COMMENT '操作人名称',
  source_ip varchar(64) DEFAULT NULL COMMENT '来源IP',
  detail_json json NOT NULL DEFAULT (json_object()) COMMENT '操作详情',
  result_message text COMMENT '结果说明',
  operated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '操作时间',
  PRIMARY KEY (id),
  KEY idx_dol_instance_version (dossier_instance_id, dossier_version_id),
  KEY idx_dol_operation_type (operation_type),
  KEY idx_dol_subject (business_subject_type, business_subject_id),
  KEY idx_dol_operated_at (operated_at),
  CONSTRAINT chk_dol_status CHECK (operation_status IN ('succeeded','failed','running'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗业务操作日志：记录生成、发布、归档、导出等业务时间线';

-- -----------------------------------------------------------------------------
-- 可重复执行的补字段工具
-- -----------------------------------------------------------------------------
DELIMITER $$

DROP PROCEDURE IF EXISTS add_column_if_missing $$
CREATE PROCEDURE add_column_if_missing(
  IN p_table_name varchar(64),
  IN p_column_name varchar(64),
  IN p_column_definition text
)
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
  ) AND NOT EXISTS (
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
END $$

DROP PROCEDURE IF EXISTS add_index_if_missing $$
CREATE PROCEDURE add_index_if_missing(
  IN p_table_name varchar(64),
  IN p_index_name varchar(64),
  IN p_index_definition text
)
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
  ) AND NOT EXISTS (
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
END $$

DELIMITER ;

-- -----------------------------------------------------------------------------
-- 补充目录节点字段
-- -----------------------------------------------------------------------------
-- -----------------------------------------------------------------------------
-- Keep the real dossier directory table under the dossier domain name.
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS ensure_dossier_structure_node_table;
DELIMITER $$

CREATE PROCEDURE ensure_dossier_structure_node_table()
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'structure_node'
  ) AND NOT EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 't1_dossier_structure_node'
  ) THEN
    RENAME TABLE structure_node TO t1_dossier_structure_node;
  END IF;
END $$

DELIMITER ;

CALL ensure_dossier_structure_node_table();
DROP PROCEDURE IF EXISTS ensure_dossier_structure_node_table;

CREATE TABLE IF NOT EXISTS t1_dossier_structure_node (
  id char(36) NOT NULL DEFAULT (uuid()),
  dossier_instance_id char(36) NOT NULL,
  dossier_version_id char(36) NOT NULL,
  aircraft_id char(36) DEFAULT NULL COMMENT 'aircraft id for locating dossier BOM nodes',
  bom_node_id char(36) DEFAULT NULL COMMENT 'related BOM node id',
  parent_bom_node_id char(36) DEFAULT NULL COMMENT 'parent BOM node id for navigation and lazy loading',
  bom_node_code varchar(100) DEFAULT NULL COMMENT 'BOM node business code or part number',
  object_level varchar(30) DEFAULT NULL COMMENT 'object level: aircraft/system/subsystem/equipment/component/part/chapter',
  part_instance_id char(36) DEFAULT NULL COMMENT 'physical part instance id',
  parent_id char(36) DEFAULT NULL,
  node_kind varchar(24) NOT NULL,
  code varchar(128) NOT NULL,
  name text NOT NULL,
  node_path varchar(1000) DEFAULT NULL COMMENT 'chapter path',
  sort_order int NOT NULL DEFAULT 0,
  chapter_status varchar(24) NOT NULL DEFAULT 'normal' COMMENT 'normal/disabled/archived',
  completeness_status varchar(24) NOT NULL DEFAULT 'not_checked' COMMENT 'not_checked/complete/warning/missing/error',
  content_count int NOT NULL DEFAULT 0,
  missing_count int NOT NULL DEFAULT 0,
  required_flag tinyint(1) NOT NULL DEFAULT 0,
  attrs_json json NOT NULL DEFAULT (json_object()),
  source_trace_json json DEFAULT NULL,
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uq_dsn_child_code (dossier_version_id, parent_id, code),
  UNIQUE KEY uq_dsn_root_code (dossier_version_id, code, ((case when (parent_id is null) then 1 else NULL end))),
  KEY idx_dsn_instance (dossier_instance_id),
  KEY idx_dsn_version (dossier_version_id),
  KEY idx_structure_node_completeness (dossier_version_id, completeness_status),
  KEY idx_structure_node_aircraft_parent_bom (aircraft_id, parent_bom_node_id, sort_order),
  KEY idx_structure_node_version_bom (dossier_version_id, bom_node_id),
  KEY idx_structure_node_version_object (dossier_version_id, object_level, sort_order),
  KEY idx_structure_node_bom_code (bom_node_code),
  KEY idx_structure_node_path_prefix (node_path(191)),
  CONSTRAINT chk_dsn_node_kind CHECK (node_kind IN ('aircraft','system','subsystem','equipment','component','part','chapter'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Dossier generated directory and structure node table';

ALTER TABLE t1_dossier_structure_node
  COMMENT = '卷宗实际目录节点：本次卷宗实际生成的目录和构型节点';
CALL add_column_if_missing('t1_dossier_structure_node', 'node_path', '`node_path` varchar(1000) DEFAULT NULL COMMENT ''章节路径'' AFTER `name`');
CALL add_column_if_missing('t1_dossier_structure_node', 'aircraft_id', '`aircraft_id` char(36) DEFAULT NULL COMMENT ''飞机ID，用于卷宗详情按飞机定位BOM'' AFTER `dossier_version_id`');
CALL add_column_if_missing('t1_dossier_structure_node', 'bom_node_id', '`bom_node_id` char(36) DEFAULT NULL COMMENT ''关联BOM节点ID'' AFTER `aircraft_id`');
CALL add_column_if_missing('t1_dossier_structure_node', 'parent_bom_node_id', '`parent_bom_node_id` char(36) DEFAULT NULL COMMENT ''上级BOM节点ID，用于返回上级和懒加载'' AFTER `bom_node_id`');
CALL add_column_if_missing('t1_dossier_structure_node', 'bom_node_code', '`bom_node_code` varchar(100) DEFAULT NULL COMMENT ''BOM节点业务编码/件号'' AFTER `parent_bom_node_id`');
CALL add_column_if_missing('t1_dossier_structure_node', 'object_level', '`object_level` varchar(30) DEFAULT NULL COMMENT ''对象层级：aircraft/system/subsystem/equipment/component/part/chapter'' AFTER `bom_node_code`');
CALL add_column_if_missing('t1_dossier_structure_node', 'part_instance_id', '`part_instance_id` char(36) DEFAULT NULL COMMENT ''实物件ID'' AFTER `object_level`');
CALL add_column_if_missing('t1_dossier_structure_node', 'chapter_status', '`chapter_status` varchar(24) NOT NULL DEFAULT ''normal'' COMMENT ''章节状态：normal/disabled/archived'' AFTER `sort_order`');
CALL add_column_if_missing('t1_dossier_structure_node', 'completeness_status', '`completeness_status` varchar(24) NOT NULL DEFAULT ''not_checked'' COMMENT ''完整性状态：not_checked/complete/warning/missing/error'' AFTER `chapter_status`');
CALL add_column_if_missing('t1_dossier_structure_node', 'content_count', '`content_count` int NOT NULL DEFAULT 0 COMMENT ''内容数量'' AFTER `completeness_status`');
CALL add_column_if_missing('t1_dossier_structure_node', 'missing_count', '`missing_count` int NOT NULL DEFAULT 0 COMMENT ''缺失数量'' AFTER `content_count`');
CALL add_column_if_missing('t1_dossier_structure_node', 'required_flag', '`required_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否必需章节'' AFTER `missing_count`');

UPDATE t1_dossier_template_data_source
   SET source_table = 't1_dossier_structure_node'
 WHERE source_table = 'structure_node';
DROP PROCEDURE IF EXISTS replace_dossier_structure_node_kind_check;
DELIMITER $$

CREATE PROCEDURE replace_dossier_structure_node_kind_check()
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 't1_dossier_structure_node'
  ) THEN
    IF EXISTS (
      SELECT 1
      FROM information_schema.table_constraints
      WHERE constraint_schema = DATABASE()
        AND table_name = 't1_dossier_structure_node'
        AND constraint_name = 'structure_node_chk_1'
    ) THEN
      ALTER TABLE t1_dossier_structure_node DROP CHECK structure_node_chk_1;
    END IF;
    IF EXISTS (
      SELECT 1
      FROM information_schema.table_constraints
      WHERE constraint_schema = DATABASE()
        AND table_name = 't1_dossier_structure_node'
        AND constraint_name = 'dossier_structure_node_chk_1'
    ) THEN
      ALTER TABLE t1_dossier_structure_node DROP CHECK dossier_structure_node_chk_1;
    END IF;
    IF EXISTS (
      SELECT 1
      FROM information_schema.table_constraints
      WHERE constraint_schema = DATABASE()
        AND table_name = 't1_dossier_structure_node'
        AND constraint_name = 'chk_dsn_node_kind'
    ) THEN
      ALTER TABLE t1_dossier_structure_node DROP CHECK chk_dsn_node_kind;
    END IF;
    ALTER TABLE t1_dossier_structure_node
      ADD CONSTRAINT chk_dsn_node_kind
      CHECK (node_kind IN ('aircraft','system','subsystem','equipment','component','part','chapter'));
  END IF;
END $$

DELIMITER ;

CALL replace_dossier_structure_node_kind_check();
DROP PROCEDURE IF EXISTS replace_dossier_structure_node_kind_check;

UPDATE t1_dossier_structure_node sn
LEFT JOIN t1_aircraft_bom_node abn
  ON abn.id = COALESCE(
    NULLIF(JSON_UNQUOTE(JSON_EXTRACT(sn.source_trace_json, '$.aircraft_bom_node_id')), 'null'),
    NULLIF(JSON_UNQUOTE(JSON_EXTRACT(sn.source_trace_json, '$.sourceRecordId')), 'null'),
    sn.bom_node_id
  )
SET
  sn.bom_node_id = COALESCE(sn.bom_node_id, abn.id),
  sn.parent_bom_node_id = COALESCE(sn.parent_bom_node_id, abn.parent_id),
  sn.aircraft_id = COALESCE(
    sn.aircraft_id,
    abn.aircraft_id,
    NULLIF(JSON_UNQUOTE(JSON_EXTRACT(sn.source_trace_json, '$.aircraft_id')), 'null')
  ),
  sn.bom_node_code = COALESCE(sn.bom_node_code, abn.part_number, sn.code),
  sn.object_level = COALESCE(
    sn.object_level,
    CASE
      WHEN sn.node_kind IN ('aircraft','system','subsystem','equipment','component','part','chapter') THEN sn.node_kind
      WHEN LOWER(abn.node_type) IN ('aircraft','system','subsystem','equipment','component','part') THEN LOWER(abn.node_type)
      WHEN abn.node_level = 1 THEN 'aircraft'
      WHEN abn.node_level = 2 THEN 'system'
      WHEN abn.node_level = 3 THEN 'subsystem'
      WHEN abn.node_level = 4 THEN 'equipment'
      WHEN abn.node_level = 5 THEN 'component'
      WHEN abn.node_level >= 6 THEN 'part'
      ELSE NULL
    END
  ),
  sn.part_instance_id = COALESCE(sn.part_instance_id, abn.part_instance_id)
WHERE sn.bom_node_id IS NULL
   OR sn.parent_bom_node_id IS NULL
   OR sn.aircraft_id IS NULL
   OR sn.bom_node_code IS NULL
   OR sn.object_level IS NULL
   OR sn.part_instance_id IS NULL;

CALL add_index_if_missing('t1_dossier_structure_node', 'idx_structure_node_completeness', 'ADD INDEX `idx_structure_node_completeness` (`dossier_version_id`, `completeness_status`)');
CALL add_index_if_missing('t1_dossier_structure_node', 'idx_structure_node_aircraft_parent_bom', 'ADD INDEX `idx_structure_node_aircraft_parent_bom` (`aircraft_id`, `parent_bom_node_id`, `sort_order`)');
CALL add_index_if_missing('t1_dossier_structure_node', 'idx_structure_node_version_bom', 'ADD INDEX `idx_structure_node_version_bom` (`dossier_version_id`, `bom_node_id`)');
CALL add_index_if_missing('t1_dossier_structure_node', 'idx_structure_node_version_object', 'ADD INDEX `idx_structure_node_version_object` (`dossier_version_id`, `object_level`, `sort_order`)');
CALL add_index_if_missing('t1_dossier_structure_node', 'idx_structure_node_bom_code', 'ADD INDEX `idx_structure_node_bom_code` (`bom_node_code`)');
CALL add_index_if_missing('t1_dossier_structure_node', 'idx_structure_node_path_prefix', 'ADD INDEX `idx_structure_node_path_prefix` (`node_path`(191))');

-- -----------------------------------------------------------------------------
-- 补充BOM节点索引：支持详情页按父节点懒加载、路径返回和关键词定位
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS replace_aircraft_bom_node_type_check;
DELIMITER $$

CREATE PROCEDURE replace_aircraft_bom_node_type_check()
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 't1_aircraft_bom_node'
  ) THEN
    IF EXISTS (
      SELECT 1
      FROM information_schema.check_constraints
      WHERE constraint_schema = DATABASE()
        AND constraint_name = 'aircraft_bom_node_chk_2'
    ) THEN
      ALTER TABLE t1_aircraft_bom_node DROP CHECK aircraft_bom_node_chk_2;
    END IF;

    UPDATE t1_aircraft_bom_node
       SET node_type = 'SUBSYSTEM'
     WHERE UPPER(node_type) = 'SUBSYSTEM';

    UPDATE t1_aircraft_bom_node
       SET node_type = 'EQUIPMENT'
     WHERE UPPER(node_type) = 'EQUIPMENT';

    UPDATE t1_aircraft_bom_node
       SET node_type = 'COMPONENT'
     WHERE UPPER(node_type) = 'COMPONENT';

    UPDATE t1_aircraft_bom_node
       SET node_type = UPPER(node_type)
     WHERE UPPER(node_type) IN ('AIRCRAFT', 'SYSTEM', 'PART', 'CONSUMABLE');

    ALTER TABLE t1_aircraft_bom_node
      ADD CONSTRAINT aircraft_bom_node_chk_2
      CHECK (UPPER(node_type) IN ('AIRCRAFT','SYSTEM','SUBSYSTEM','EQUIPMENT','COMPONENT','PART','CONSUMABLE'));
  END IF;
END $$

DELIMITER ;

CALL replace_aircraft_bom_node_type_check();
DROP PROCEDURE IF EXISTS replace_aircraft_bom_node_type_check;

CALL add_index_if_missing('t1_aircraft_bom_node', 'idx_aircraft_bom_parent_lazy', 'ADD INDEX `idx_aircraft_bom_parent_lazy` (`aircraft_id`, `parent_id`, `is_active`, `node_level`)');
CALL add_index_if_missing('t1_aircraft_bom_node', 'idx_aircraft_bom_level_type', 'ADD INDEX `idx_aircraft_bom_level_type` (`aircraft_id`, `node_level`, `node_type`, `is_active`)');
CALL add_index_if_missing('t1_aircraft_bom_node', 'idx_aircraft_bom_search_key', 'ADD INDEX `idx_aircraft_bom_search_key` (`aircraft_id`, `part_number`, `serial_number`, `position_code`)');
CALL add_index_if_missing('t1_aircraft_bom_node', 'idx_aircraft_bom_part_instance', 'ADD INDEX `idx_aircraft_bom_part_instance` (`part_instance_id`)');

-- -----------------------------------------------------------------------------
-- 补充文档条目字段
-- -----------------------------------------------------------------------------
CALL add_column_if_missing('t1_document_entry', 'source_system', '`source_system` varchar(50) DEFAULT NULL COMMENT ''来源系统'' AFTER `source_trace_json`');
CALL add_column_if_missing('t1_document_entry', 'source_table', '`source_table` varchar(200) DEFAULT NULL COMMENT ''来源表名'' AFTER `source_system`');
CALL add_column_if_missing('t1_document_entry', 'source_record_id', '`source_record_id` char(36) DEFAULT NULL COMMENT ''来源记录ID'' AFTER `source_table`');
CALL add_column_if_missing('t1_document_entry', 'source_record_key', '`source_record_key` varchar(500) DEFAULT NULL COMMENT ''来源业务键'' AFTER `source_record_id`');
CALL add_column_if_missing('t1_document_entry', 'document_status', '`document_status` varchar(24) NOT NULL DEFAULT ''active'' COMMENT ''文档状态：active/missing/archived'' AFTER `source_record_key`');
CALL add_column_if_missing('t1_document_entry', 'completeness_status', '`completeness_status` varchar(24) NOT NULL DEFAULT ''not_checked'' COMMENT ''完整性状态：not_checked/complete/warning/missing/error'' AFTER `document_status`');
CALL add_column_if_missing('t1_document_entry', 'required_flag', '`required_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否必需文档'' AFTER `completeness_status`');
CALL add_column_if_missing('t1_document_entry', 'included_flag', '`included_flag` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''是否纳入当前卷宗'' AFTER `required_flag`');

CALL add_index_if_missing('t1_document_entry', 'idx_document_entry_source', 'ADD INDEX `idx_document_entry_source` (`source_system`, `source_table`, `source_record_id`)');
CALL add_index_if_missing('t1_document_entry', 'idx_document_entry_completeness', 'ADD INDEX `idx_document_entry_completeness` (`dossier_version_id`, `completeness_status`)');

-- -----------------------------------------------------------------------------
-- 补充检查批次字段
-- -----------------------------------------------------------------------------
CALL add_column_if_missing('t1_data_inspection_run', 'dossier_version_id', '`dossier_version_id` char(36) DEFAULT NULL COMMENT ''卷宗版本ID'' AFTER `dossier_instance_id`');
CALL add_column_if_missing('t1_data_inspection_run', 'check_scope_type', '`check_scope_type` varchar(50) DEFAULT NULL COMMENT ''检查范围类型：dossier/section/bom_node/key_part/purchased_part'' AFTER `dossier_version_id`');
CALL add_column_if_missing('t1_data_inspection_run', 'check_scope_id', '`check_scope_id` char(36) DEFAULT NULL COMMENT ''检查范围对象ID'' AFTER `check_scope_type`');

CALL add_index_if_missing('t1_data_inspection_run', 'idx_data_inspection_run_version', 'ADD INDEX `idx_data_inspection_run_version` (`dossier_version_id`)');
CALL add_index_if_missing('t1_data_inspection_run', 'idx_data_inspection_run_scope', 'ADD INDEX `idx_data_inspection_run_scope` (`check_scope_type`, `check_scope_id`)');

-- -----------------------------------------------------------------------------
-- 补充生成明细字段：支持按BOM节点、关重件、自制/外购规则生成
-- -----------------------------------------------------------------------------
CALL add_column_if_missing('t1_generation_job_item', 'bom_node_id', '`bom_node_id` char(36) DEFAULT NULL COMMENT ''BOM节点ID'' AFTER `source_record_key`');
CALL add_column_if_missing('t1_generation_job_item', 'part_instance_id', '`part_instance_id` char(36) DEFAULT NULL COMMENT ''零件实例ID'' AFTER `bom_node_id`');
CALL add_column_if_missing('t1_generation_job_item', 'is_key_part', '`is_key_part` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否关重件'' AFTER `part_instance_id`');
CALL add_column_if_missing('t1_generation_job_item', 'supply_mode', '`supply_mode` varchar(30) DEFAULT NULL COMMENT ''供给方式：self_made/purchased/outsourced/unknown'' AFTER `is_key_part`');
CALL add_column_if_missing('t1_generation_job_item', 'include_design_data', '`include_design_data` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否纳入设计数据'' AFTER `supply_mode`');
CALL add_column_if_missing('t1_generation_job_item', 'include_manufacturing_data', '`include_manufacturing_data` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否纳入制造数据'' AFTER `include_design_data`');
CALL add_column_if_missing('t1_generation_job_item', 'include_service_data', '`include_service_data` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否纳入服役数据'' AFTER `include_manufacturing_data`');
CALL add_column_if_missing('t1_generation_job_item', 'include_source_proof', '`include_source_proof` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否纳入来源证明'' AFTER `include_service_data`');
CALL add_column_if_missing('t1_generation_job_item', 'completeness_status', '`completeness_status` varchar(24) NOT NULL DEFAULT ''not_checked'' COMMENT ''完整性状态：not_checked/complete/warning/missing/error'' AFTER `include_source_proof`');

CALL add_index_if_missing('t1_generation_job_item', 'idx_generation_job_item_bom', 'ADD INDEX `idx_generation_job_item_bom` (`bom_node_id`)');
CALL add_index_if_missing('t1_generation_job_item', 'idx_generation_job_item_part', 'ADD INDEX `idx_generation_job_item_part` (`part_instance_id`)');
CALL add_index_if_missing('t1_generation_job_item', 'idx_generation_job_item_rules', 'ADD INDEX `idx_generation_job_item_rules` (`is_key_part`, `supply_mode`)');
CALL add_index_if_missing('t1_generation_job_item', 'idx_generation_job_item_completeness', 'ADD INDEX `idx_generation_job_item_completeness` (`completeness_status`)');

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;
