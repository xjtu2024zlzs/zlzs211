-- =============================================================================
-- Digital dossier template management completion - MySQL 8.0
-- Purpose: support RuoYi Cloud dossier template management page with editable
-- chapters, chapter data sources, validation rules, and generation parameters.
-- =============================================================================

SET NAMES utf8mb4;
USE `ry-cloud`;

-- -----------------------------------------------------------------------------
-- 模板章节：把模板目录从大 JSON 拆成可维护的树形表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_dossier_template_chapter (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '模板章节ID',
  template_id char(36) NOT NULL COMMENT '模板ID',
  parent_id char(36) DEFAULT NULL COMMENT '父章节ID',
  chapter_code varchar(100) NOT NULL COMMENT '章节编码',
  chapter_name varchar(300) NOT NULL COMMENT '章节名称',
  chapter_level int NOT NULL DEFAULT 1 COMMENT '章节层级',
  chapter_path varchar(1000) DEFAULT NULL COMMENT '章节路径',
  node_kind varchar(30) NOT NULL DEFAULT 'chapter' COMMENT '节点类型：chapter/group/item',
  sort_order int NOT NULL DEFAULT 0 COMMENT '排序号',
  required_flag tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否必填章节',
  enabled_flag tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  default_expand tinyint(1) NOT NULL DEFAULT 0 COMMENT '前端是否默认展开',
  completeness_requirement varchar(30) NOT NULL DEFAULT 'normal' COMMENT '完整性要求：normal/strict/optional',
  chapter_desc text COMMENT '章节说明',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_by varchar(100) NOT NULL DEFAULT 'system' COMMENT '创建人',
  updated_by varchar(100) DEFAULT NULL COMMENT '修改人',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_dtc_template_code (template_id, chapter_code),
  KEY idx_dtc_template_parent (template_id, parent_id),
  KEY idx_dtc_template_sort (template_id, sort_order),
  KEY idx_dtc_enabled (enabled_flag),
  CONSTRAINT chk_dtc_node_kind CHECK (node_kind IN ('chapter','group','item')),
  CONSTRAINT chk_dtc_requirement CHECK (completeness_requirement IN ('normal','strict','optional'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗模板章节：用于模板目录树配置';

-- -----------------------------------------------------------------------------
-- 模板章节数据来源：配置每个章节从哪些系统和业务表取数
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_dossier_template_data_source (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '模板数据来源ID',
  template_id char(36) NOT NULL COMMENT '模板ID',
  chapter_id char(36) NOT NULL COMMENT '模板章节ID',
  source_code varchar(100) NOT NULL COMMENT '数据来源编码',
  source_system varchar(50) DEFAULT NULL COMMENT '来源系统：PLM/MES/MRO/OPS/MANUAL等',
  source_table varchar(200) NOT NULL COMMENT '来源表名',
  source_name varchar(300) NOT NULL COMMENT '来源名称',
  source_desc text COMMENT '来源说明',
  lifecycle_stage varchar(30) DEFAULT NULL COMMENT '生命周期阶段：DESIGN/MANUFACTURING/SERVICE/DOSSIER',
  source_record_type varchar(100) DEFAULT NULL COMMENT '来源记录类型',
  join_condition_json json NOT NULL DEFAULT (json_object()) COMMENT '关联条件配置',
  filter_condition_json json NOT NULL DEFAULT (json_object()) COMMENT '过滤条件配置',
  apply_object_type varchar(50) DEFAULT NULL COMMENT '适用对象：aircraft/bom_node/t1_part_instance/key_part等',
  supply_mode_scope varchar(30) NOT NULL DEFAULT 'all' COMMENT '供给方式范围：all/self_made/purchased/outsourced',
  key_part_scope varchar(30) NOT NULL DEFAULT 'all' COMMENT '关重件范围：all/key_only/non_key_only',
  required_flag tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否必选来源',
  enabled_flag tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  sort_order int NOT NULL DEFAULT 0 COMMENT '排序号',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_by varchar(100) NOT NULL DEFAULT 'system' COMMENT '创建人',
  updated_by varchar(100) DEFAULT NULL COMMENT '修改人',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_dtds_chapter_source (chapter_id, source_code),
  KEY idx_dtds_template (template_id),
  KEY idx_dtds_chapter (chapter_id),
  KEY idx_dtds_source (source_system, source_table),
  KEY idx_dtds_rules (supply_mode_scope, key_part_scope),
  KEY idx_dtds_enabled (enabled_flag),
  CONSTRAINT chk_dtds_supply_scope CHECK (supply_mode_scope IN ('all','self_made','purchased','outsourced')),
  CONSTRAINT chk_dtds_key_scope CHECK (key_part_scope IN ('all','key_only','non_key_only'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗模板数据来源：配置章节绑定的业务数据表和适用条件';

-- -----------------------------------------------------------------------------
-- 模板校验规则：配置模板或章节使用哪些校验规则
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_dossier_template_rule (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '模板规则ID',
  template_id char(36) NOT NULL COMMENT '模板ID',
  chapter_id char(36) DEFAULT NULL COMMENT '模板章节ID',
  rule_code varchar(100) NOT NULL COMMENT '规则编码',
  rule_name varchar(200) NOT NULL COMMENT '规则名称',
  rule_type varchar(30) NOT NULL COMMENT '规则类型：required/enum/expression/relation/business',
  target_table varchar(200) DEFAULT NULL COMMENT '校验表名',
  target_field varchar(200) DEFAULT NULL COMMENT '校验字段',
  target_path varchar(500) DEFAULT NULL COMMENT '校验路径',
  rule_expression varchar(1000) DEFAULT NULL COMMENT '规则表达式',
  rule_expression_json json NOT NULL DEFAULT (json_object()) COMMENT '规则表达式配置',
  severity varchar(24) NOT NULL DEFAULT 'warning' COMMENT '严重程度：info/warning/error/blocker',
  error_message varchar(500) DEFAULT NULL COMMENT '错误提示',
  remediation_hint text COMMENT '整改建议',
  bind_quality_rule_id char(36) DEFAULT NULL COMMENT '关联通用质量规则ID',
  apply_object_type varchar(50) DEFAULT NULL COMMENT '适用对象类型',
  supply_mode_scope varchar(30) NOT NULL DEFAULT 'all' COMMENT '供给方式范围：all/self_made/purchased/outsourced',
  key_part_scope varchar(30) NOT NULL DEFAULT 'all' COMMENT '关重件范围：all/key_only/non_key_only',
  required_flag tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否必须通过',
  enabled_flag tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  sort_order int NOT NULL DEFAULT 0 COMMENT '排序号',
  created_by varchar(100) NOT NULL DEFAULT 'system' COMMENT '创建人',
  updated_by varchar(100) DEFAULT NULL COMMENT '修改人',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_dtr_template_rule (template_id, rule_code),
  KEY idx_dtr_template (template_id),
  KEY idx_dtr_chapter (chapter_id),
  KEY idx_dtr_target (target_table, target_field),
  KEY idx_dtr_quality_rule (bind_quality_rule_id),
  KEY idx_dtr_enabled (enabled_flag),
  CONSTRAINT chk_dtr_rule_type CHECK (rule_type IN ('required','enum','expression','relation','business')),
  CONSTRAINT chk_dtr_severity CHECK (severity IN ('info','warning','error','blocker')),
  CONSTRAINT chk_dtr_supply_scope CHECK (supply_mode_scope IN ('all','self_made','purchased','outsourced')),
  CONSTRAINT chk_dtr_key_scope CHECK (key_part_scope IN ('all','key_only','non_key_only'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗模板校验规则：配置模板和章节的生成前检查规则';

-- -----------------------------------------------------------------------------
-- 模板生成参数：把常用生成参数拆成可维护的键值配置
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_dossier_template_param (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '模板参数ID',
  template_id char(36) NOT NULL COMMENT '模板ID',
  chapter_id char(36) DEFAULT NULL COMMENT '模板章节ID，可为空表示全局参数',
  param_code varchar(100) NOT NULL COMMENT '参数编码',
  param_name varchar(200) NOT NULL COMMENT '参数名称',
  param_type varchar(30) NOT NULL DEFAULT 'string' COMMENT '参数类型：string/number/boolean/json/enum',
  param_value text COMMENT '参数值',
  default_value text COMMENT '默认值',
  option_json json NOT NULL DEFAULT (json_array()) COMMENT '枚举选项',
  required_flag tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否必填',
  editable_flag tinyint(1) NOT NULL DEFAULT 1 COMMENT '生成时是否可编辑',
  enabled_flag tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  sort_order int NOT NULL DEFAULT 0 COMMENT '排序号',
  param_desc text COMMENT '参数说明',
  created_by varchar(100) NOT NULL DEFAULT 'system' COMMENT '创建人',
  updated_by varchar(100) DEFAULT NULL COMMENT '修改人',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_dtp_template_param (template_id, chapter_id, param_code),
  KEY idx_dtp_template (template_id),
  KEY idx_dtp_chapter (chapter_id),
  KEY idx_dtp_enabled (enabled_flag),
  CONSTRAINT chk_dtp_param_type CHECK (param_type IN ('string','number','boolean','json','enum'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗模板生成参数：配置生成卷宗时使用的默认参数';

-- -----------------------------------------------------------------------------
-- 补充模板主表字段
-- -----------------------------------------------------------------------------
DELIMITER $$

DROP PROCEDURE IF EXISTS add_column_if_missing $$
CREATE PROCEDURE add_column_if_missing(
  IN p_table_name varchar(64),
  IN p_column_name varchar(64),
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
END $$

DROP PROCEDURE IF EXISTS add_index_if_missing $$
CREATE PROCEDURE add_index_if_missing(
  IN p_table_name varchar(64),
  IN p_index_name varchar(64),
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
END $$

DROP PROCEDURE IF EXISTS drop_single_unique_index_if_exists $$
CREATE PROCEDURE drop_single_unique_index_if_exists(
  IN p_table_name varchar(64),
  IN p_column_name varchar(64)
)
BEGIN
  SET @single_unique_index_name = NULL;
  SELECT s.index_name
    INTO @single_unique_index_name
  FROM information_schema.statistics s
  WHERE s.table_schema = DATABASE()
    AND s.table_name = p_table_name
    AND s.non_unique = 0
    AND s.column_name = p_column_name
    AND s.index_name <> 'PRIMARY'
    AND NOT EXISTS (
      SELECT 1
      FROM information_schema.statistics s2
      WHERE s2.table_schema = s.table_schema
        AND s2.table_name = s.table_name
        AND s2.index_name = s.index_name
        AND s2.seq_in_index > 1
    )
  LIMIT 1;

  IF @single_unique_index_name IS NOT NULL THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` DROP INDEX `', @single_unique_index_name, '`');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    SET @single_unique_index_name = NULL;
  END IF;
END $$

DELIMITER ;

CALL add_column_if_missing('t1_dossier_template', 'template_version', '`template_version` varchar(64) NOT NULL DEFAULT ''V1.0'' COMMENT ''模板版本'' AFTER `template_code`');
CALL add_column_if_missing('t1_dossier_template', 'is_default', '`is_default` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否默认模板'' AFTER `status`');
CALL add_column_if_missing('t1_dossier_template', 'effective_from', '`effective_from` datetime(6) DEFAULT NULL COMMENT ''生效时间'' AFTER `is_default`');
CALL add_column_if_missing('t1_dossier_template', 'effective_to', '`effective_to` datetime(6) DEFAULT NULL COMMENT ''失效时间'' AFTER `effective_from`');
CALL add_column_if_missing('t1_dossier_template', 'created_by', '`created_by` varchar(100) NOT NULL DEFAULT ''system'' COMMENT ''创建人'' AFTER `effective_to`');
CALL add_column_if_missing('t1_dossier_template', 'updated_by', '`updated_by` varchar(100) DEFAULT NULL COMMENT ''修改人'' AFTER `created_by`');

-- 旧数据可能没有模板编码，先补一个稳定编码，方便页面管理和后续生成。
UPDATE t1_dossier_template
SET template_code = CONCAT('TPL-', UPPER(SUBSTRING(REPLACE(id, '-', ''), 1, 12)))
WHERE template_code IS NULL OR template_code = '';

-- template_code 是模板管理核心标识，补齐后改为必填。
ALTER TABLE t1_dossier_template MODIFY COLUMN template_code varchar(100) NOT NULL COMMENT '模板编码';

-- 模板需要支持同一编码下的多个版本，唯一性落在“模板编码 + 模板版本”上。
CALL drop_single_unique_index_if_exists('t1_dossier_template', 'template_code');
CALL add_index_if_missing('t1_dossier_template', 'uk_dossier_template_code_version', 'ADD UNIQUE KEY `uk_dossier_template_code_version` (`template_code`, `template_version`)');
CALL add_index_if_missing('t1_dossier_template', 'idx_dossier_template_type_object', 'ADD INDEX `idx_dossier_template_type_object` (`template_type`, `applicable_object_type`)');
CALL add_index_if_missing('t1_dossier_template', 'idx_dossier_template_status_default', 'ADD INDEX `idx_dossier_template_status_default` (`status`, `is_default`)');

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;
DROP PROCEDURE IF EXISTS drop_single_unique_index_if_exists;
