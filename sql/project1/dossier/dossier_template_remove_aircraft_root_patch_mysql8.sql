USE `ry-cloud`;

START TRANSACTION;

-- Make "卷宗目录" the template root and remove the old display shell
-- "单台份飞机综合卷宗".
UPDATE t1_dossier_template_chapter c
SET c.parent_id = NULL,
    c.chapter_name = '卷宗目录',
    c.chapter_level = 1,
    c.chapter_path = '卷宗目录',
    c.sort_order = 1,
    c.updated_by = 'system'
WHERE c.chapter_code = 'NODE_TEMPLATE_ROOT';

INSERT INTO t1_dossier_template_chapter (
  id, template_id, parent_id, chapter_code, chapter_name, chapter_level,
  chapter_path, node_kind, sort_order, required_flag, enabled_flag,
  default_expand, completeness_requirement, chapter_desc, attrs_json,
  created_by, updated_by
)
SELECT
  UUID(), r.template_id, r.id, 'AIRCRAFT_DIRECTORY_ROOT', '整机目录', 2,
  '卷宗目录/整机目录', 'group', 90, 1, 1,
  1, 'strict', '整机层目录，和系统、子系统、设备、组件、零件目录同属于卷宗目录。',
  JSON_OBJECT(
    'objectLevel', 'aircraft',
    'displayType', 'tree_table',
    'sortMode', 'business_order',
    'primaryFields', JSON_ARRAY('tail_number', 'msn', 'aircraft_type', 'status'),
    'blocks', JSON_ARRAY('summary', 'relation', 'details', 'documents', 'issues'),
    'showMissingTips', TRUE
  ),
  'system', 'system'
FROM t1_dossier_template_chapter r
WHERE r.chapter_code = 'NODE_TEMPLATE_ROOT'
  AND NOT EXISTS (
    SELECT 1
    FROM t1_dossier_template_chapter a
    WHERE a.template_id = r.template_id
      AND a.chapter_code = 'AIRCRAFT_DIRECTORY_ROOT'
  );

UPDATE t1_dossier_template_data_source ds
JOIN t1_dossier_template_chapter old_root
  ON old_root.id = ds.chapter_id
 AND old_root.chapter_code = 'AIRCRAFT_ROOT'
JOIN t1_dossier_template_chapter new_root
  ON new_root.template_id = old_root.template_id
 AND new_root.chapter_code = 'NODE_TEMPLATE_ROOT'
SET ds.chapter_id = new_root.id,
    ds.updated_by = 'system';

UPDATE t1_dossier_template_rule ru
JOIN t1_dossier_template_chapter old_root
  ON old_root.id = ru.chapter_id
 AND old_root.chapter_code = 'AIRCRAFT_ROOT'
JOIN t1_dossier_template_chapter new_root
  ON new_root.template_id = old_root.template_id
 AND new_root.chapter_code = 'NODE_TEMPLATE_ROOT'
SET ru.chapter_id = new_root.id,
    ru.updated_by = 'system';

UPDATE t1_dossier_template_param p
JOIN t1_dossier_template_chapter old_root
  ON old_root.id = p.chapter_id
 AND old_root.chapter_code = 'AIRCRAFT_ROOT'
JOIN t1_dossier_template_chapter new_root
  ON new_root.template_id = old_root.template_id
 AND new_root.chapter_code = 'NODE_TEMPLATE_ROOT'
SET p.chapter_id = new_root.id,
    p.updated_by = 'system';

UPDATE t1_dossier_template_chapter c
JOIN t1_dossier_template_chapter root
  ON root.template_id = c.template_id
 AND root.chapter_code = 'NODE_TEMPLATE_ROOT'
SET c.parent_id = root.id,
    c.chapter_level = 2,
    c.chapter_path = CONCAT('卷宗目录/', c.chapter_name),
    c.updated_by = 'system'
WHERE c.chapter_code IN (
  'AIRCRAFT_DIRECTORY_ROOT',
  'SYSTEM_ROOT',
  'SUBSYSTEM_ROOT',
  'EQUIPMENT_ROOT',
  'COMPONENT_ROOT',
  'PART_ROOT'
);

UPDATE t1_dossier_template_chapter c
JOIN t1_dossier_template_chapter aircraft_dir
  ON aircraft_dir.template_id = c.template_id
 AND aircraft_dir.chapter_code = 'AIRCRAFT_DIRECTORY_ROOT'
SET c.parent_id = aircraft_dir.id,
    c.chapter_level = 3,
    c.chapter_path = CONCAT('卷宗目录/整机目录/', c.chapter_name),
    c.updated_by = 'system'
WHERE JSON_UNQUOTE(JSON_EXTRACT(c.attrs_json, '$.objectLevel')) = 'aircraft'
  AND c.chapter_code NOT IN ('AIRCRAFT_ROOT', 'AIRCRAFT_DIRECTORY_ROOT');

UPDATE t1_dossier_template_chapter c
JOIN t1_dossier_template_chapter old_root
  ON old_root.id = c.parent_id
 AND old_root.chapter_code = 'AIRCRAFT_ROOT'
JOIN t1_dossier_template_chapter new_root
  ON new_root.template_id = old_root.template_id
 AND new_root.chapter_code = 'NODE_TEMPLATE_ROOT'
SET c.parent_id = new_root.id,
    c.updated_by = 'system';

DROP TEMPORARY TABLE IF EXISTS tmp_dossier_chapter_tree;
CREATE TEMPORARY TABLE tmp_dossier_chapter_tree AS
WITH RECURSIVE chapter_tree AS (
  SELECT
    c.id,
    c.chapter_name,
    c.parent_id,
    1 AS chapter_level,
    CAST('卷宗目录' AS CHAR(1000)) AS chapter_path
  FROM t1_dossier_template_chapter c
  WHERE c.chapter_code = 'NODE_TEMPLATE_ROOT'

  UNION ALL

  SELECT
    child.id,
    child.chapter_name,
    child.parent_id,
    parent.chapter_level + 1 AS chapter_level,
    CONCAT(parent.chapter_path, '/', child.chapter_name) AS chapter_path
  FROM t1_dossier_template_chapter child
  JOIN chapter_tree parent
    ON parent.id = child.parent_id
)
SELECT id, chapter_level, chapter_path
FROM chapter_tree;

UPDATE t1_dossier_template_chapter c
JOIN tmp_dossier_chapter_tree t
  ON t.id = c.id
SET c.chapter_level = t.chapter_level,
    c.chapter_path = t.chapter_path,
    c.updated_by = 'system';

DROP TEMPORARY TABLE IF EXISTS tmp_dossier_chapter_tree;

DELETE old_root
FROM t1_dossier_template_chapter old_root
WHERE old_root.chapter_code = 'AIRCRAFT_ROOT';

COMMIT;
