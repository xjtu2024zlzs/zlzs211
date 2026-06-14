-- Normalize B-1234 dossier versions for integration data.
-- MySQL 8.0+
--
-- Purpose:
--   Keep only the latest three active B-1234 dossier versions.
--   Re-number them from V1.1, V1.2, V1.3.
--
-- Prerequisite:
--   Run after dossier_full_schema_mysql8.sql, dossier_instance_management_mysql8.sql,
--   and dossier_mock_data_seed_mysql8.sql.

USE `ry-cloud`;

START TRANSACTION;

SET @b1234_instance_id := 'fb123401-0001-4001-8001-202606070001';
SET @archive_base_no := 900000;

DROP TEMPORARY TABLE IF EXISTS tmp_b1234_keep_versions;
DROP TEMPORARY TABLE IF EXISTS tmp_b1234_archive_versions;

CREATE TEMPORARY TABLE tmp_b1234_keep_versions AS
SELECT
  numbered.id,
  numbered.new_no,
  LAG(numbered.id) OVER (ORDER BY numbered.new_no) AS previous_version_id
FROM (
  SELECT
    latest.id,
    ROW_NUMBER() OVER (ORDER BY latest.version_no, latest.created_at, latest.id) AS new_no
  FROM (
    SELECT id, version_no, created_at
    FROM t1_dossier_version
    WHERE dossier_instance_id = @b1234_instance_id
      AND deleted_at IS NULL
    ORDER BY version_no DESC, created_at DESC, id DESC
    LIMIT 3
  ) latest
) numbered;

CREATE TEMPORARY TABLE tmp_b1234_archive_versions AS
SELECT
  v.id,
  ROW_NUMBER() OVER (ORDER BY v.version_no, v.created_at, v.id) AS archive_no
FROM t1_dossier_version v
LEFT JOIN tmp_b1234_keep_versions k ON k.id = v.id
WHERE v.dossier_instance_id = @b1234_instance_id
  AND k.id IS NULL;

UPDATE t1_dossier_version v
INNER JOIN tmp_b1234_archive_versions a ON a.id = v.id
SET
  v.version_no = @archive_base_no + a.archive_no,
  v.version_label = CONCAT('ARCHIVED-', LPAD(a.archive_no, 6, '0'), '-', LEFT(v.id, 8)),
  v.is_current = 0,
  v.deleted_by = COALESCE(v.deleted_by, 'system'),
  v.deleted_at = COALESCE(v.deleted_at, CURRENT_TIMESTAMP(6));

UPDATE t1_dossier_version v
INNER JOIN tmp_b1234_keep_versions k ON k.id = v.id
SET
  v.version_no = k.new_no,
  v.version_label = CONCAT('V1.', k.new_no),
  v.major_version_no = 1,
  v.minor_version_no = k.new_no,
  v.version_level = 'minor',
  v.previous_version_id = k.previous_version_id,
  v.version_reason = CASE WHEN k.new_no = 1 THEN 'initial' ELSE 'data_update' END,
  v.is_current = 0,
  v.deleted_by = NULL,
  v.deleted_at = NULL,
  v.content_summary_json = JSON_SET(
    COALESCE(v.content_summary_json, JSON_OBJECT()),
    '$.versionLabel',
    CONCAT('V1.', k.new_no)
  );

UPDATE t1_dossier_version v
INNER JOIN (
  SELECT id, new_no
  FROM tmp_b1234_keep_versions
  ORDER BY new_no DESC
  LIMIT 1
) current_v ON current_v.id = v.id
SET v.is_current = 1;

UPDATE t1_dossier_instance di
INNER JOIN (
  SELECT id, new_no
  FROM tmp_b1234_keep_versions
  ORDER BY new_no DESC
  LIMIT 1
) current_v ON current_v.id IS NOT NULL
SET
  di.current_version_id = current_v.id,
  di.current_version_no = current_v.new_no,
  di.updated_by = 'system',
  di.updated_at = CURRENT_TIMESTAMP(6)
WHERE di.id = @b1234_instance_id
  AND di.deleted_at IS NULL;

UPDATE t1_generation_job gj
INNER JOIN tmp_b1234_keep_versions k ON k.id = gj.dossier_version_id
SET
  gj.result_summary_json = JSON_SET(
    COALESCE(gj.result_summary_json, JSON_OBJECT()),
    '$.versionLabel',
    CONCAT('V1.', k.new_no)
  ),
  gj.output_json = JSON_SET(
    COALESCE(gj.output_json, JSON_OBJECT()),
    '$.versionLabel',
    CONCAT('V1.', k.new_no)
  )
WHERE gj.dossier_instance_id = @b1234_instance_id;

UPDATE t1_generation_job gj
INNER JOIN tmp_b1234_keep_versions k ON k.id = gj.dossier_version_id
SET gj.result_summary_json = JSON_SET(
  gj.result_summary_json,
  '$.fileName',
  REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(gj.result_summary_json, '$.fileName')), 'V1\\.[0-9]+', CONCAT('V1.', k.new_no))
)
WHERE gj.dossier_instance_id = @b1234_instance_id
  AND JSON_CONTAINS_PATH(gj.result_summary_json, 'one', '$.fileName');

UPDATE t1_generation_job gj
INNER JOIN tmp_b1234_keep_versions k ON k.id = gj.dossier_version_id
SET gj.result_summary_json = JSON_SET(
  gj.result_summary_json,
  '$.packageName',
  REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(gj.result_summary_json, '$.packageName')), 'V1\\.[0-9]+', CONCAT('V1.', k.new_no))
)
WHERE gj.dossier_instance_id = @b1234_instance_id
  AND JSON_CONTAINS_PATH(gj.result_summary_json, 'one', '$.packageName');

UPDATE t1_generation_job gj
INNER JOIN tmp_b1234_keep_versions k ON k.id = gj.dossier_version_id
SET gj.result_summary_json = JSON_SET(
  gj.result_summary_json,
  '$.outputPath',
  REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(gj.result_summary_json, '$.outputPath')), 'V1\\.[0-9]+', CONCAT('V1.', k.new_no))
)
WHERE gj.dossier_instance_id = @b1234_instance_id
  AND JSON_CONTAINS_PATH(gj.result_summary_json, 'one', '$.outputPath');

UPDATE t1_generation_job gj
INNER JOIN tmp_b1234_keep_versions k ON k.id = gj.dossier_version_id
SET gj.output_json = JSON_SET(
  gj.output_json,
  '$.fileName',
  REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(gj.output_json, '$.fileName')), 'V1\\.[0-9]+', CONCAT('V1.', k.new_no))
)
WHERE gj.dossier_instance_id = @b1234_instance_id
  AND JSON_CONTAINS_PATH(gj.output_json, 'one', '$.fileName');

UPDATE t1_generation_job gj
INNER JOIN tmp_b1234_keep_versions k ON k.id = gj.dossier_version_id
SET gj.output_json = JSON_SET(
  gj.output_json,
  '$.packageName',
  REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(gj.output_json, '$.packageName')), 'V1\\.[0-9]+', CONCAT('V1.', k.new_no))
)
WHERE gj.dossier_instance_id = @b1234_instance_id
  AND JSON_CONTAINS_PATH(gj.output_json, 'one', '$.packageName');

UPDATE t1_generation_job gj
INNER JOIN tmp_b1234_keep_versions k ON k.id = gj.dossier_version_id
SET gj.output_json = JSON_SET(
  gj.output_json,
  '$.outputPath',
  REGEXP_REPLACE(JSON_UNQUOTE(JSON_EXTRACT(gj.output_json, '$.outputPath')), 'V1\\.[0-9]+', CONCAT('V1.', k.new_no))
)
WHERE gj.dossier_instance_id = @b1234_instance_id
  AND JSON_CONTAINS_PATH(gj.output_json, 'one', '$.outputPath');

UPDATE t1_dossier_export_job ej
INNER JOIN tmp_b1234_keep_versions k ON k.id = ej.dossier_version_id
SET
  ej.export_code = REGEXP_REPLACE(ej.export_code, 'V1\\.[0-9]+', CONCAT('V1.', k.new_no)),
  ej.export_scope_json = JSON_SET(
    COALESCE(ej.export_scope_json, JSON_OBJECT()),
    '$.versionLabel',
    CONCAT('V1.', k.new_no)
  ),
  ej.export_params_json = JSON_SET(
    COALESCE(ej.export_params_json, JSON_OBJECT()),
    '$.versionLabel',
    CONCAT('V1.', k.new_no)
  )
WHERE ej.dossier_instance_id = @b1234_instance_id;

UPDATE t1_dossier_export_file ef
INNER JOIN t1_dossier_export_job ej ON ej.id = ef.export_job_id
INNER JOIN tmp_b1234_keep_versions k ON k.id = ej.dossier_version_id
SET ef.file_name = REGEXP_REPLACE(ef.file_name, 'V1\\.[0-9]+', CONCAT('V1.', k.new_no))
WHERE ej.dossier_instance_id = @b1234_instance_id;

UPDATE t1_data_snapshot ds
INNER JOIN t1_dossier_version v ON v.data_snapshot_id = ds.id
INNER JOIN tmp_b1234_keep_versions k ON k.id = v.id
SET ds.snapshot_data_json = JSON_SET(
  COALESCE(ds.snapshot_data_json, JSON_OBJECT()),
  '$.versionLabel',
  CONCAT('V1.', k.new_no),
  '$.dossier.versionLabel',
  CONCAT('V1.', k.new_no)
)
WHERE ds.dossier_instance_id = @b1234_instance_id;

SELECT
  v.id,
  v.version_no,
  v.version_label,
  v.previous_version_id,
  v.is_current,
  v.deleted_at
FROM t1_dossier_version v
WHERE v.dossier_instance_id = @b1234_instance_id
ORDER BY v.deleted_at IS NOT NULL, v.version_no;

DROP TEMPORARY TABLE IF EXISTS tmp_b1234_keep_versions;
DROP TEMPORARY TABLE IF EXISTS tmp_b1234_archive_versions;

COMMIT;
