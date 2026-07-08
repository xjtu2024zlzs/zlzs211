-- Project1 dossier patch: split installation history and service maintenance sources.
-- Safe to rerun. It does not delete business data.

SET @template_id = 't1000001-0001-4001-8001-000000000111';
SET @part_instance_id = 'c7000001-0001-4001-8001-000000000001';

SELECT @part_install_chapter_id := id
FROM t1_dossier_template_chapter
WHERE template_id = @template_id AND chapter_code = 'PART_INSTALL'
LIMIT 1;

SELECT @part_service_chapter_id := id
FROM t1_dossier_template_chapter
WHERE template_id = @template_id AND chapter_code = 'PART_SERVICE'
LIMIT 1;

START TRANSACTION;

UPDATE t1_dossier_template_chapter
SET chapter_desc = '装机飞机、装机位置、安装动作和装机参数。',
    attrs_json = JSON_SET(COALESCE(attrs_json, JSON_OBJECT()), '$.splitInstallService', TRUE),
    updated_by = 'codex',
    updated_at = CURRENT_TIMESTAMP(6)
WHERE id = @part_install_chapter_id;

UPDATE t1_dossier_template_chapter
SET chapter_desc = '服役巡检、维修工单、拆卸复查和寿命使用记录。',
    attrs_json = JSON_SET(COALESCE(attrs_json, JSON_OBJECT()), '$.splitInstallService', TRUE),
    updated_by = 'codex',
    updated_at = CURRENT_TIMESTAMP(6)
WHERE id = @part_service_chapter_id;

UPDATE t1_dossier_template_data_source
SET source_name = '液压弯管拆卸/复查履历',
    source_desc = '只读取拆卸、更换和复查动作；首次装机动作归入装机履历目录。',
    filter_condition_json = JSON_OBJECT('action_type', JSON_ARRAY('REMOVAL', 'REPLACE')),
    updated_by = 'codex',
    updated_at = CURRENT_TIMESTAMP(6)
WHERE template_id = @template_id
  AND source_code = 'SRC-PART-IR-HYD';

UPDATE t1_dossier_template_data_source
SET chapter_id = @part_install_chapter_id,
    source_name = '液压弯管装机动作',
    source_desc = '读取主起液压供压弯管首次装机动作、装机位置、力矩和装后检查结果。',
    lifecycle_stage = 'INSTALLATION',
    source_record_type = 'part_installation',
    filter_condition_json = JSON_OBJECT('action_type', 'INSTALL', 'part_instance_id', @part_instance_id),
    attrs_json = JSON_SET(COALESCE(attrs_json, JSON_OBJECT()),
        '$.objectAnchorEnabled', TRUE,
        '$.splitInstallService', TRUE,
        '$.reviewNote', '装机动作归入装机履历；拆卸/复查动作归入服役维修。'),
    updated_by = 'codex',
    updated_at = CURRENT_TIMESTAMP(6)
WHERE template_id = @template_id
  AND source_code = 'SRC-PART-INSTALL-ACTION-HYD';

INSERT INTO t1_dossier_template_data_source (
  id, template_id, chapter_id, source_code, source_system, source_table,
  source_name, source_desc, lifecycle_stage, source_record_type,
  join_condition_json, filter_condition_json, apply_object_type,
  supply_mode_scope, key_part_scope, required_flag, enabled_flag,
  sort_order, attrs_json, created_by, updated_by, created_at, updated_at
)
SELECT
  UUID(), @template_id, @part_install_chapter_id,
  'SRC-PART-INSTALL-ACTION-HYD', 'MES', 't1_install_removal',
  '液压弯管装机动作',
  '读取主起液压供压弯管首次装机动作、装机位置、力矩和装后检查结果。',
  'INSTALLATION', 'part_installation',
  JSON_OBJECT(
    'node_id', '${nodeId}',
    'aircraft_id', '${aircraftId}',
    'bom_node_id', '${bomNodeId}',
    'instance_id', '${partInstanceId}',
    'object_level', '${objectLevel}',
    'part_instance_id', '${partInstanceId}',
    'object_profile_id', '${objectProfileId}'
  ),
  JSON_OBJECT('action_type', 'INSTALL', 'part_instance_id', @part_instance_id),
  'part', 'self_made', 'key_only', 1, 1, 53,
  JSON_OBJECT(
    'objectAnchorEnabled', TRUE,
    'splitInstallService', TRUE,
    'reviewNote', '装机动作归入装机履历；拆卸/复查动作归入服役维修。'
  ),
  'codex', 'codex', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE @part_install_chapter_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM t1_dossier_template_data_source
    WHERE template_id = @template_id
      AND source_code = 'SRC-PART-INSTALL-ACTION-HYD'
  );

COMMIT;

SELECT source_code, chapter_id, source_table, lifecycle_stage,
       CAST(filter_condition_json AS CHAR) AS filter_condition_json
FROM t1_dossier_template_data_source
WHERE template_id = @template_id
  AND source_code IN ('SRC-PART-INSTALL-HYD', 'SRC-PART-ASSEMBLY-HYD',
                      'SRC-PART-INSTALL-ACTION-HYD', 'SRC-PART-IR-HYD',
                      'SRC-PART-WO-HYD', 'SRC-PART-LIFE-USAGE-HYD',
                      'SRC-PART-SERVICE-TEXT-HYD', 'SRC-PART-FAULT-HYD')
ORDER BY chapter_id, sort_order;
