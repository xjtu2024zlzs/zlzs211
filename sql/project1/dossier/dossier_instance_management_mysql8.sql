-- =============================================================================
-- Digital dossier instance management completion - MySQL 8.0
-- Purpose: support dossier instance list, version-aware downloads, generation
-- time sorting, and instance management queries.
-- =============================================================================

SET NAMES utf8mb4;
USE `ry-cloud`;

-- -----------------------------------------------------------------------------
-- Reusable migration helpers
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

DROP PROCEDURE IF EXISTS replace_check_if_exists $$
CREATE PROCEDURE replace_check_if_exists(
  IN p_table_name varchar(64),
  IN p_constraint_name varchar(64),
  IN p_check_clause text
)
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
  ) THEN
    IF EXISTS (
      SELECT 1
      FROM information_schema.check_constraints
      WHERE constraint_schema = DATABASE()
        AND constraint_name = p_constraint_name
    ) THEN
      SET @drop_sql = CONCAT('ALTER TABLE `', p_table_name, '` DROP CHECK `', p_constraint_name, '`');
      PREPARE stmt FROM @drop_sql;
      EXECUTE stmt;
      DEALLOCATE PREPARE stmt;
    END IF;

    SET @add_sql = CONCAT('ALTER TABLE `', p_table_name, '` ADD CONSTRAINT `', p_constraint_name, '` CHECK (', p_check_clause, ')');
    PREPARE stmt FROM @add_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END $$

DELIMITER ;

-- -----------------------------------------------------------------------------
-- Dossier instance management fields
-- -----------------------------------------------------------------------------
CALL add_column_if_missing('t1_dossier_instance', 'instance_code',
  '`instance_code` varchar(100) DEFAULT NULL COMMENT ''卷宗编号，用于列表展示和业务查询'' AFTER `id`');
CALL add_column_if_missing('t1_dossier_instance', 'instance_name',
  '`instance_name` varchar(300) DEFAULT NULL COMMENT ''卷宗名称，用于卷宗实例管理列表展示'' AFTER `instance_code`');
CALL add_column_if_missing('t1_dossier_instance', 'created_by',
  '`created_by` varchar(100) NOT NULL DEFAULT ''system'' COMMENT ''创建人'' AFTER `instance_options_json`');
CALL add_column_if_missing('t1_dossier_instance', 'updated_by',
  '`updated_by` varchar(100) DEFAULT NULL COMMENT ''更新人'' AFTER `created_by`');
CALL add_column_if_missing('t1_dossier_instance', 'published_at',
  '`published_at` datetime(6) DEFAULT NULL COMMENT ''发布时间'' AFTER `updated_at`');
CALL add_column_if_missing('t1_dossier_instance', 'archived_at',
  '`archived_at` datetime(6) DEFAULT NULL COMMENT ''归档时间'' AFTER `published_at`');

UPDATE t1_dossier_instance di
LEFT JOIN t1_physical_aircraft pa ON pa.id = di.aircraft_id
LEFT JOIN t1_dossier_version dv ON dv.id = di.current_version_id
LEFT JOIN t1_generation_job gj ON gj.id = dv.generation_job_id
SET
  di.instance_code = COALESCE(
    NULLIF(di.instance_code, ''),
    CONCAT(
      'DOS-',
      REPLACE(COALESCE(pa.tail_number, LEFT(di.id, 8)), '-', ''),
      '-',
      DATE_FORMAT(COALESCE(gj.finished_at, dv.created_at, di.created_at), '%Y%m%d')
    )
  ),
  di.instance_name = COALESCE(
    NULLIF(di.instance_name, ''),
    CONCAT(COALESCE(pa.tail_number, '未知飞机'), ' 单台份飞机综合卷宗')
  ),
  di.updated_by = COALESCE(NULLIF(di.updated_by, ''), di.created_by),
  di.published_at = CASE WHEN di.status = 'published' THEN COALESCE(di.published_at, dv.published_at, di.updated_at) ELSE di.published_at END,
  di.archived_at = CASE WHEN di.status = 'archived' THEN COALESCE(di.archived_at, di.updated_at) ELSE di.archived_at END
WHERE di.instance_code IS NULL
   OR di.instance_name IS NULL
   OR di.updated_by IS NULL
   OR (di.status = 'published' AND di.published_at IS NULL)
   OR (di.status = 'archived' AND di.archived_at IS NULL);

CALL add_index_if_missing('t1_dossier_instance', 'idx_dossier_instance_code',
  'ADD INDEX `idx_dossier_instance_code` (`instance_code`)');
CALL add_index_if_missing('t1_dossier_instance', 'idx_dossier_instance_status_time',
  'ADD INDEX `idx_dossier_instance_status_time` (`status`, `updated_at`)');
CALL add_index_if_missing('t1_dossier_instance', 'idx_dossier_instance_aircraft_template',
  'ADD INDEX `idx_dossier_instance_aircraft_template` (`aircraft_id`, `template_id`)');

-- -----------------------------------------------------------------------------
-- Version-aware export jobs and files
-- -----------------------------------------------------------------------------
CALL add_column_if_missing('t1_dossier_export_job', 'dossier_version_id',
  '`dossier_version_id` char(36) DEFAULT NULL COMMENT ''卷宗版本ID，用于区分同一卷宗不同版本的导出产物'' AFTER `dossier_instance_id`');
CALL add_column_if_missing('t1_dossier_export_job', 'generation_job_id',
  '`generation_job_id` char(36) DEFAULT NULL COMMENT ''关联生成任务ID'' AFTER `dossier_version_id`');

CALL add_column_if_missing('t1_dossier_export_file', 'file_role',
  '`file_role` varchar(30) NOT NULL DEFAULT ''attachment'' COMMENT ''文件角色：main_pdf/attachment_zip/snapshot_json/preview/attachment/other'' AFTER `file_format`');
CALL add_column_if_missing('t1_dossier_export_file', 'is_primary',
  '`is_primary` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否主文件'' AFTER `file_role`');
CALL add_column_if_missing('t1_dossier_export_file', 'page_count',
  '`page_count` int DEFAULT NULL COMMENT ''PDF页数'' AFTER `file_size`');
CALL add_column_if_missing('t1_dossier_export_file', 'mime_type',
  '`mime_type` varchar(200) DEFAULT NULL COMMENT ''MIME类型'' AFTER `file_format`');
CALL add_column_if_missing('t1_dossier_export_file', 'display_order',
  '`display_order` int NOT NULL DEFAULT 0 COMMENT ''展示排序'' AFTER `file_hash`');

UPDATE t1_dossier_export_file
SET
  file_role = CASE
    WHEN file_role IS NOT NULL AND file_role <> '' THEN file_role
    WHEN UPPER(file_format) = 'PDF' THEN 'main_pdf'
    WHEN UPPER(file_format) = 'ZIP' THEN 'attachment_zip'
    WHEN UPPER(file_format) = 'JSON' THEN 'snapshot_json'
    ELSE 'attachment'
  END,
  is_primary = CASE
    WHEN is_primary = 1 THEN 1
    WHEN UPPER(file_format) = 'PDF' THEN 1
    ELSE is_primary
  END
WHERE file_role IS NULL
   OR file_role = ''
   OR (is_primary = 0 AND UPPER(file_format) = 'PDF');

CALL add_index_if_missing('t1_dossier_export_job', 'idx_dossier_export_job_instance_version',
  'ADD INDEX `idx_dossier_export_job_instance_version` (`dossier_instance_id`, `dossier_version_id`, `created_at`)');
CALL add_index_if_missing('t1_dossier_export_job', 'idx_dossier_export_job_generation',
  'ADD INDEX `idx_dossier_export_job_generation` (`generation_job_id`)');
CALL add_index_if_missing('t1_dossier_export_job', 'idx_dossier_export_job_status_finished',
  'ADD INDEX `idx_dossier_export_job_status_finished` (`export_status`, `finished_at`)');
CALL add_index_if_missing('t1_dossier_export_file', 'idx_dossier_export_file_role',
  'ADD INDEX `idx_dossier_export_file_role` (`file_role`, `is_primary`, `display_order`)');

CALL replace_check_if_exists('t1_dossier_export_file', 'chk_dossier_export_file_role',
  '`file_role` IN (''main_pdf'',''attachment_zip'',''snapshot_json'',''preview'',''attachment'',''other'')');

-- -----------------------------------------------------------------------------
-- Query indexes for generation time sorting and management list filtering
-- -----------------------------------------------------------------------------
CALL add_index_if_missing('t1_generation_job', 'idx_generation_job_instance_finished',
  'ADD INDEX `idx_generation_job_instance_finished` (`dossier_instance_id`, `finished_at`)');
CALL add_index_if_missing('t1_generation_job', 'idx_generation_job_version',
  'ADD INDEX `idx_generation_job_version` (`dossier_version_id`)');
CALL add_index_if_missing('t1_dossier_version', 'idx_dossier_version_created',
  'ADD INDEX `idx_dossier_version_created` (`created_at`)');
CALL add_index_if_missing('t1_document_entry', 'idx_document_entry_version_included',
  'ADD INDEX `idx_document_entry_version_included` (`dossier_version_id`, `included_flag`)');

-- Keep template constraints consistent with the aircraft hierarchy:
-- aircraft -> system -> subsystem -> equipment -> component -> part.
CALL replace_check_if_exists('t1_dossier_template', 'dossier_template_chk_2',
  '`applicable_object_type` IN (''aircraft'',''system'',''subsystem'',''equipment'',''component'',''part'',''fault'',''other'')');

-- -----------------------------------------------------------------------------
-- Management view: one row per dossier instance for the instance list page.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE VIEW t1_v_dossier_instance_manage AS
SELECT
  di.id AS instance_id,
  di.instance_code,
  di.instance_name,
  di.instance_label,
  di.status AS instance_status,
  CASE
    WHEN lj.job_status = 'failed' THEN '生成失败'
    WHEN lj.job_status IN ('queued','running') OR di.status = 'building' THEN '生成中'
    WHEN di.status = 'published' THEN '已发布'
    WHEN di.status = 'archived' THEN '已归档'
    WHEN di.status = 'ready' THEN '已生成'
    ELSE '草稿'
  END AS status_name,
  di.template_id,
  dt.template_code,
  dt.name AS template_name,
  cv.template_version,
  di.aircraft_id,
  pa.model_id,
  am.model_code,
  am.name AS model_name,
  pa.tail_number,
  pa.msn,
  pa.aircraft_type,
  pa.operational_status,
  cv.id AS current_version_id,
  cv.version_no AS current_version_no,
  cv.version_label AS current_version_label,
  cv.major_version_no,
  cv.minor_version_no,
  cv.version_level,
  cv.version_reason,
  COALESCE(lj.id, vj.id) AS generation_job_id,
  COALESCE(lj.job_code, vj.job_code) AS generation_job_code,
  COALESCE(lj.job_status, vj.job_status) AS generation_job_status,
  COALESCE(
    CASE WHEN lj.job_status = 'succeeded' THEN lj.finished_at ELSE NULL END,
    vj.finished_at,
    cv.created_at,
    di.updated_at,
    di.created_at
  ) AS generate_time,
  COALESCE(
    CAST(NULLIF(JSON_UNQUOTE(JSON_EXTRACT(CASE WHEN lj.job_status = 'succeeded' THEN lj.output_json ELSE NULL END, '$.pageCount')), 'null') AS UNSIGNED),
    CAST(NULLIF(JSON_UNQUOTE(JSON_EXTRACT(vj.output_json, '$.pageCount')), 'null') AS UNSIGNED),
    CAST(NULLIF(JSON_UNQUOTE(JSON_EXTRACT(CASE WHEN lj.job_status = 'succeeded' THEN lj.result_summary_json ELSE NULL END, '$.pageCount')), 'null') AS UNSIGNED),
    CAST(NULLIF(JSON_UNQUOTE(JSON_EXTRACT(vj.result_summary_json, '$.pageCount')), 'null') AS UNSIGNED),
    0
  ) AS page_count,
  COALESCE(
    CAST(NULLIF(JSON_UNQUOTE(JSON_EXTRACT(CASE WHEN lj.job_status = 'succeeded' THEN lj.output_json ELSE NULL END, '$.fileCount')), 'null') AS UNSIGNED),
    CAST(NULLIF(JSON_UNQUOTE(JSON_EXTRACT(vj.output_json, '$.fileCount')), 'null') AS UNSIGNED),
    CAST(NULLIF(JSON_UNQUOTE(JSON_EXTRACT(CASE WHEN lj.job_status = 'succeeded' THEN lj.result_summary_json ELSE NULL END, '$.fileCount')), 'null') AS UNSIGNED),
    CAST(NULLIF(JSON_UNQUOTE(JSON_EXTRACT(vj.result_summary_json, '$.fileCount')), 'null') AS UNSIGNED),
    efa.export_file_count,
    doc.document_entry_count,
    0
  ) AS file_count,
  COALESCE(
    CAST(NULLIF(JSON_UNQUOTE(JSON_EXTRACT(CASE WHEN lj.job_status = 'succeeded' THEN lj.output_json ELSE NULL END, '$.sourceRecordCount')), 'null') AS UNSIGNED),
    CAST(NULLIF(JSON_UNQUOTE(JSON_EXTRACT(vj.output_json, '$.sourceRecordCount')), 'null') AS UNSIGNED),
    CAST(NULLIF(JSON_UNQUOTE(JSON_EXTRACT(CASE WHEN lj.job_status = 'succeeded' THEN lj.result_summary_json ELSE NULL END, '$.sourceRecordCount')), 'null') AS UNSIGNED),
    CAST(NULLIF(JSON_UNQUOTE(JSON_EXTRACT(vj.result_summary_json, '$.sourceRecordCount')), 'null') AS UNSIGNED),
    ci.content_item_count,
    0
  ) AS data_record_count,
  doc.document_entry_count,
  ci.content_item_count,
  COALESCE(
    JSON_UNQUOTE(JSON_EXTRACT(CASE WHEN lj.job_status = 'succeeded' THEN lj.output_json ELSE NULL END, '$.fileName')),
    JSON_UNQUOTE(JSON_EXTRACT(vj.output_json, '$.fileName')),
    efa.pdf_file_name
  ) AS pdf_file_name,
  COALESCE(
    JSON_UNQUOTE(JSON_EXTRACT(CASE WHEN lj.job_status = 'succeeded' THEN lj.output_json ELSE NULL END, '$.packageName')),
    JSON_UNQUOTE(JSON_EXTRACT(vj.output_json, '$.packageName')),
    efa.zip_file_name
  ) AS zip_file_name,
  di.created_by,
  di.updated_by,
  di.created_at,
  di.updated_at,
  di.published_at,
  di.archived_at,
  di.deleted_at
FROM t1_dossier_instance di
LEFT JOIN (
  SELECT v1.*
  FROM t1_dossier_version v1
  INNER JOIN (
    SELECT dossier_instance_id, MAX(version_no) AS max_version_no
    FROM t1_dossier_version
    GROUP BY dossier_instance_id
  ) vm ON vm.dossier_instance_id = v1.dossier_instance_id
      AND vm.max_version_no = v1.version_no
) lv ON lv.dossier_instance_id = di.id
LEFT JOIN t1_dossier_version cv
  ON cv.id = di.current_version_id
  OR (di.current_version_id IS NULL AND cv.id = lv.id)
LEFT JOIN t1_generation_job vj ON vj.id = cv.generation_job_id
LEFT JOIN (
  SELECT gj1.*
  FROM t1_generation_job gj1
  INNER JOIN (
    SELECT dossier_instance_id, MAX(created_at) AS max_created_at
    FROM t1_generation_job
    WHERE job_type IN ('generate','regenerate')
    GROUP BY dossier_instance_id
  ) gm ON gm.dossier_instance_id = gj1.dossier_instance_id
      AND gm.max_created_at = gj1.created_at
  WHERE gj1.job_type IN ('generate','regenerate')
) lj ON lj.dossier_instance_id = di.id
LEFT JOIN t1_physical_aircraft pa ON pa.id = di.aircraft_id
LEFT JOIN t1_ac_model am ON am.id = pa.model_id
LEFT JOIN t1_dossier_template dt ON dt.id = di.template_id
LEFT JOIN (
  SELECT dossier_version_id, COUNT(1) AS document_entry_count
  FROM t1_document_entry
  WHERE included_flag = 1
  GROUP BY dossier_version_id
) doc ON doc.dossier_version_id = cv.id
LEFT JOIN (
  SELECT dossier_version_id, COUNT(1) AS content_item_count
  FROM t1_dossier_content_item
  WHERE included_flag = 1
  GROUP BY dossier_version_id
) ci ON ci.dossier_version_id = cv.id
LEFT JOIN (
  SELECT
    ej.dossier_instance_id,
    ej.dossier_version_id,
    COUNT(ef.id) AS export_file_count,
    MAX(CASE WHEN ef.file_role = 'main_pdf' THEN ef.file_name ELSE NULL END) AS pdf_file_name,
    MAX(CASE WHEN ef.file_role = 'attachment_zip' THEN ef.file_name ELSE NULL END) AS zip_file_name
  FROM t1_dossier_export_job ej
  LEFT JOIN t1_dossier_export_file ef ON ef.export_job_id = ej.id
  WHERE ej.export_status = 'succeeded'
  GROUP BY ej.dossier_instance_id, ej.dossier_version_id
) efa ON efa.dossier_instance_id = di.id
     AND efa.dossier_version_id = cv.id;

-- -----------------------------------------------------------------------------
-- Cleanup helpers
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;
DROP PROCEDURE IF EXISTS replace_check_if_exists;
