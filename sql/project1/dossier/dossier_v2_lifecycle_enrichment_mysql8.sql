-- Dossier second-version data enrichment for B-1234.
-- Purpose:
-- 1. Flatten subsystem "composition equipment" from a second BOM tree into a status list.
-- 2. Correct the active template catalog without changing the template version.
-- 3. Add realistic full-lifecycle demo records for the aircraft, hydraulic system,
--    landing gear system, and descendants under both systems.

SET NAMES utf8mb4;
SET @template_id = 't1000001-0001-4001-8001-000000000111';
SET @aircraft_id = 'b0000001-0001-4001-8001-000000000001';
SET @aircraft_bom_root_id = 'f1000001-0001-4001-8001-000000000001';
SET @hyd_system_id = 'f1000031-0031-4031-8031-000000000031';
SET @lgr_system_id = 'f1000002-0002-4002-8002-000000000002';

UPDATE t1_dossier_template
   SET description = '用于生成单架飞机的综合卷宗。已修正子系统目录：保留“子系统结构”作为唯一结构树，将“组成设备”改为平铺的设备清单与状态；补充整机、液压系统、起落架系统及其设备/组件/零件全生命周期数据。模板版本保持不变，卷宗新版本由数据变化触发。',
       chapter_tree_json = json_set(
         chapter_tree_json,
         '$.dataSecondVersionReady', true,
         '$.catalogAdjustment', 'SUBSYSTEM_EQUIPMENT_FLAT_STATUS_LIST',
         '$.lifecycleCoverage', json_array('aircraft', 'hydraulic_system', 'landing_gear_system', 'equipment', 'component', 'part')
       ),
       validation_rules_json = json_set(
         validation_rules_json,
         '$.v2LifecycleEnrichment', true,
         '$.subsystemEquipmentAsFlatList', true,
         '$.lifecycleSourcePatch', true
       ),
       default_generator_params_json = json_set(
         default_generator_params_json,
         '$.regenerate_strategy', 'new_version',
         '$.data_change_versioning', true
       ),
       updated_by = 'codex',
       updated_at = sysdate(6)
 WHERE id = @template_id;

UPDATE t1_dossier_template_chapter
   SET chapter_name = '设备清单与状态',
       chapter_path = '子系统目录/设备清单与状态',
       chapter_desc = '平铺展示子系统直属设备的身份、装机位置、序列号、技术状态、质量状态和生命周期覆盖情况；不再作为第二棵 BOM 树展开。',
       attrs_json = json_object(
         'blocks', json_array('summary', 'details', 'issues'),
         'sortMode', 'business_order',
         'displayType', 'summary_table',
         'objectLevel', 'subsystem',
         'primaryFields', json_array('equipment_code', 'equipment_name', 'position_code', 'serial_number', 'technical_status', 'quality_status'),
         'showMissingTips', true,
         'noBomTree', true,
         'replaces', 'SUBSYSTEM_EQUIPMENT_BOM_TREE'
       ),
       updated_by = 'codex',
       updated_at = sysdate(6)
 WHERE template_id = @template_id
   AND chapter_code = 'SUBSYSTEM_EQUIPMENT';

UPDATE t1_dossier_template_data_source
   SET source_code = 'SRC-SUBSYSTEM-EQUIPMENT-STATUS',
       source_system = 'CONFIG',
       source_table = 't1_aircraft_bom_node',
       source_name = '子系统直属设备清单与状态',
       source_desc = '按子系统节点平铺查询直属设备，并关联技术状态/质量状态；不再展开设备、组件、零件 BOM 树。',
       lifecycle_stage = 'DOSSIER',
       source_record_type = 'business',
       join_condition_json = json_object('parent_id', '${nodeId}', 'node_type', 'EQUIPMENT'),
       filter_condition_json = json_object('is_active', 1, 'flatListOnly', true, 'noTreeExpansion', true),
       apply_object_type = 'subsystem',
       attrs_json = json_object('displayType', 'summary_table', 'noBomTree', true, 'v2CatalogFix', true),
       updated_by = 'codex',
       updated_at = sysdate(6)
 WHERE template_id = @template_id
   AND chapter_id = (
     SELECT id FROM t1_dossier_template_chapter
      WHERE template_id = @template_id AND chapter_code = 'SUBSYSTEM_EQUIPMENT'
      LIMIT 1
   );

DELETE FROM t1_dossier_template_data_source
 WHERE template_id = @template_id
   AND (id LIKE 'v2ds%' OR source_code LIKE 'SRC-%-FULL-LIFECYCLE');

INSERT INTO t1_dossier_template_data_source (
  id, template_id, chapter_id, source_code, source_system, source_table,
  source_name, source_desc, lifecycle_stage, source_record_type,
  join_condition_json, filter_condition_json, apply_object_type,
  supply_mode_scope, key_part_scope, required_flag, enabled_flag,
  sort_order, attrs_json, created_by, updated_by
)
SELECT concat('v2ds', substr(md5(c.chapter_code), 1, 32)),
       @template_id,
       c.id,
       concat('SRC-', c.chapter_code, '-FULL-LIFECYCLE'),
       'DOSSIER',
       't1_object_lifecycle_record',
       '全生命周期事件流',
       concat(c.chapter_name, '补充读取设计、制造、装机/交付、服役、维修/故障阶段事件。'),
       'FULL_LIFECYCLE',
       'lifecycle_event',
       json_object('aircraft_id', '${aircraftId}', 'bom_node_id', '${nodeId}', 'part_instance_id', '${partInstanceId}'),
       json_object('enabled', true, 'v2Scope', 'B-1234/SYS-29/SYS-32'),
       x.apply_object_type,
       'all',
       'all',
       0,
       1,
       x.sort_order,
       json_object('patch', 'dossier_v2_lifecycle_enrichment_mysql8.sql', 'autoBackfill', true),
       'codex',
       'codex'
  FROM (
    SELECT 'AIRCRAFT_SERVICE' chapter_code, 'aircraft' apply_object_type, 551 sort_order UNION ALL
    SELECT 'AIRCRAFT_TECH_STATUS', 'aircraft', 552 UNION ALL
    SELECT 'SYSTEM_MAINTENANCE', 'system', 1075 UNION ALL
    SELECT 'SUBSYSTEM_MAINTENANCE', 'subsystem', 1585 UNION ALL
    SELECT 'EQUIPMENT_CHANGE', 'equipment', 2095 UNION ALL
    SELECT 'COMPONENT_SERVICE', 'component', 3085 UNION ALL
    SELECT 'PART_SERVICE', 'part', 4065
  ) x
  JOIN t1_dossier_template_chapter c
    ON c.template_id = @template_id
   AND c.chapter_code = x.chapter_code;

DELETE FROM t1_object_lifecycle_record WHERE id LIKE 'v2lc%';
DELETE FROM t1_object_technical_status WHERE id LIKE 'v2ts%' OR status_code LIKE 'V2-TECH-%' OR status_code LIKE 'D2-TECH-%';
DELETE FROM t1_object_status_history WHERE id LIKE 'v2sh%';
DELETE FROM t1_object_interface WHERE id LIKE 'v2if%' OR interface_code LIKE 'IF-V2-%' OR interface_code LIKE 'IF-D2-%';
DELETE FROM t1_life_usage_record WHERE id LIKE 'v2lu%';
DELETE FROM t1_part_document WHERE id LIKE 'v2pd%';
DELETE FROM t1_work_order WHERE id LIKE 'v2wo%' OR wo_number LIKE 'WO-V2-%' OR wo_number LIKE 'WO-D2-%';
DELETE FROM t1_fault_event WHERE id LIKE 'v2fe%';

DROP TEMPORARY TABLE IF EXISTS tmp_v2_scope_nodes;
CREATE TEMPORARY TABLE tmp_v2_scope_nodes AS
WITH RECURSIVE system_tree AS (
  SELECT id, parent_id, aircraft_id, node_level, node_type, part_number, part_name,
         serial_number, position_code, ata_chapter, part_instance_id, part_number AS root_scope
    FROM t1_aircraft_bom_node
   WHERE id IN (@hyd_system_id, @lgr_system_id)
     AND aircraft_id = @aircraft_id
     AND is_active = 1
  UNION ALL
  SELECT c.id, c.parent_id, c.aircraft_id, c.node_level, c.node_type, c.part_number, c.part_name,
         c.serial_number, c.position_code, c.ata_chapter, c.part_instance_id, p.root_scope
    FROM t1_aircraft_bom_node c
    JOIN system_tree p ON p.id = c.parent_id
   WHERE c.aircraft_id = @aircraft_id
     AND c.is_active = 1
),
all_nodes AS (
  SELECT id, parent_id, aircraft_id, node_level, node_type, part_number, part_name,
         serial_number, position_code, ata_chapter, part_instance_id, 'AIRCRAFT' AS root_scope
    FROM t1_aircraft_bom_node
   WHERE id = @aircraft_bom_root_id
  UNION ALL
  SELECT id, parent_id, aircraft_id, node_level, node_type, part_number, part_name,
         serial_number, position_code, ata_chapter, part_instance_id, root_scope
    FROM system_tree
)
SELECT row_number() over (
         order by case root_scope when 'AIRCRAFT' then 0 when 'SYS-29' then 1 when 'SYS-32' then 2 else 9 end,
                  node_level, position_code, part_number
       ) AS sort_seq,
       id, parent_id, aircraft_id, node_level, node_type,
       CASE WHEN node_type = 'CONSUMABLE' THEN 'part' ELSE lower(node_type) END AS object_level,
       part_number, part_name, serial_number, position_code, ata_chapter, part_instance_id, root_scope
  FROM all_nodes;

INSERT INTO t1_object_lifecycle_record (
  id, object_profile_id, object_level, object_id, aircraft_id, bom_node_id, part_instance_id,
  lifecycle_stage, business_record_type, business_record_id, business_record_key,
  event_time, event_title, event_summary, result_status, source_system, source_table,
  source_record_id, source_trace_json, attrs_json
)
SELECT concat('v2lc', substr(md5(concat(n.id, '|', s.lifecycle_stage)), 1, 32)),
       n.id,
       n.object_level,
       n.id,
       @aircraft_id,
       n.id,
       n.part_instance_id,
       s.lifecycle_stage,
       s.business_record_type,
       n.id,
       concat(n.part_number, '/', s.lifecycle_stage, '/DATA-SECOND'),
       date_add(timestamp('2025-09-01 09:00:00'), interval (s.day_offset + (n.sort_seq % 45)) day),
       CASE s.lifecycle_stage
         WHEN 'design' THEN concat(n.part_name, '设计定义发布')
         WHEN 'manufacturing' THEN concat(n.part_name, '制造/验收记录关闭')
         WHEN 'installation' THEN concat(n.part_name, '装机与构型确认')
         WHEN 'service' THEN concat(n.part_name, '服役监控与定检复核')
         ELSE concat(n.part_name, '状态评审')
       END,
       CASE
         WHEN n.node_type = 'AIRCRAFT' AND s.lifecycle_stage = 'design' THEN '完成 B-1234 单台份构型定义、系统边界、适航符合性矩阵和卷宗对象锚点确认。'
         WHEN n.node_type = 'AIRCRAFT' AND s.lifecycle_stage = 'manufacturing' THEN '总装、系统接通、地面联试和交付前质量门审查均已关闭，形成整机制造追溯包。'
         WHEN n.node_type = 'AIRCRAFT' AND s.lifecycle_stage = 'installation' THEN '交付前构型快照固化，SYS-29 液压系统与 SYS-32 起落架系统装机状态已纳入第二版卷宗。'
         WHEN n.node_type = 'AIRCRAFT' THEN '累计飞行小时、飞行循环、定检、故障关闭和技术状态变更已汇总到整机生命周期事件流。'
         WHEN n.node_type = 'SYSTEM' AND n.part_number = 'SYS-29' THEN concat('液压系统完成需求、接口、管路/泵阀/蓄压器构型、污染度控制和压力脉动监控数据补齐；当前节点：', n.part_number, '。')
         WHEN n.node_type = 'SYSTEM' AND n.part_number = 'SYS-32' THEN concat('起落架系统完成收放、刹车、防滑、转弯、舱门/锁机构与地面勤务数据补齐；当前节点：', n.part_number, '。')
         WHEN n.node_type = 'SUBSYSTEM' THEN concat('子系统功能边界、直属设备清单、接口关系、状态变更和维修记录已补齐，采用平铺设备清单展示：', n.part_name, '。')
         WHEN n.node_type = 'EQUIPMENT' THEN concat('设备级设计定义、装配验收、装机位置、序列号、技术状态、使用小时和维护记录已形成闭环：', coalesce(n.serial_number, n.position_code, n.part_number), '。')
         WHEN n.node_type = 'COMPONENT' THEN concat('组件级材料/工艺、装配扭矩、检验结果、装机父项和服役复核记录已补齐：', coalesce(n.serial_number, n.position_code, n.part_number), '。')
         WHEN n.node_type = 'PART' THEN concat('零件级图纸、材料批次、制造/来料复验、装机履历、使用寿命和证明附件已补齐：', coalesce(n.serial_number, n.position_code, n.part_number), '。')
         ELSE concat('对象生命周期数据已纳入第二版卷宗：', n.part_name, '。')
       END,
       s.result_status,
       s.source_system,
       s.source_table,
       n.id,
       json_object('aircraftId', @aircraft_id, 'bomNodeId', n.id, 'partNumber', n.part_number, 'scope', n.root_scope, 'dataBatch', 'SECOND_VERSION_DATA'),
       json_object(
         'patch', 'dossier_v2_lifecycle_enrichment_mysql8.sql',
         'coverage', 'full_lifecycle',
         'objectLevel', n.object_level,
         'rootScope', n.root_scope,
         'ataChapter', n.ata_chapter,
         'positionCode', n.position_code,
         'evidenceQuality', CASE WHEN n.part_number = 'HYD-TUBE-MLG-32A' THEN 'key_demo_complete' ELSE 'demo_complete' END
       )
  FROM tmp_v2_scope_nodes n
  JOIN (
    SELECT 'design' lifecycle_stage, 'design_definition' business_record_type, 0 day_offset, 'released' result_status, 'DESIGN' source_system, 't1_part_document' source_table UNION ALL
    SELECT 'manufacturing', 'manufacturing_acceptance', 85, 'accepted', 'MES', 't1_shop_order' UNION ALL
    SELECT 'installation', 'installation_release', 170, 'installed', 'CONFIG', 't1_install_removal' UNION ALL
    SELECT 'service', 'service_operation', 255, 'in_service', 'MRO', 't1_life_usage_record'
  ) s;

INSERT INTO t1_object_technical_status (
  id, object_profile_id, object_level, object_id, aircraft_id, bom_node_id, part_instance_id,
  status_code, status_name, baseline_code, bom_version, drawing_revision, process_revision,
  software_version, hardware_version, modification_status, deviation_status, verification_status,
  release_status, effective_from, source_system, source_table, source_record_id, attrs_json
)
SELECT concat('v2ts', substr(md5(n.id), 1, 32)),
       n.id,
       n.object_level,
       n.id,
       @aircraft_id,
       n.id,
       n.part_instance_id,
       concat('D2-TECH-', left(replace(n.part_number, '/', '-'), 48), '-', substr(md5(n.id), 1, 8)),
       concat(n.part_name, ' 第二版数据当前技术状态'),
       CASE n.root_scope WHEN 'SYS-29' THEN 'BL-B1234-HYD-2026-06-D2'
                         WHEN 'SYS-32' THEN 'BL-B1234-LGR-2026-06-D2'
                         ELSE 'BL-B1234-AIRCRAFT-2026-06-D2' END,
       'BOM-B1234-2026.06.D2',
       CASE n.node_type WHEN 'AIRCRAFT' THEN 'AC-REV-D'
                        WHEN 'SYSTEM' THEN 'SYS-ICD-REV-C'
                        WHEN 'SUBSYSTEM' THEN 'SUBSYS-FSD-REV-C'
                        WHEN 'EQUIPMENT' THEN 'EQP-ID-REV-B'
                        WHEN 'COMPONENT' THEN 'CMP-PR-REV-B'
                        ELSE 'PART-DWG-REV-C' END,
       'PROC-2026-06',
       CASE WHEN n.part_name LIKE '%控制%' OR n.part_name LIKE '%传感%' THEN 'SW-BIT-2.3.1' ELSE NULL END,
       CASE WHEN n.node_type IN ('EQUIPMENT', 'COMPONENT') THEN 'HW-B2' ELSE NULL END,
       'incorporated',
       'none',
       'verified',
       'released',
       timestamp('2026-06-01 08:00:00'),
       'CONFIG',
       't1_aircraft_bom_node',
       n.id,
       json_object('v2LifecycleCovered', true, 'rootScope', n.root_scope, 'positionCode', n.position_code, 'serialNumber', n.serial_number)
  FROM tmp_v2_scope_nodes n;

INSERT INTO t1_object_status_history (
  id, object_profile_id, object_level, object_id, aircraft_id, bom_node_id, part_instance_id,
  status_category, old_status, new_status, change_time, change_reason, changed_by,
  source_table, source_record_id, attrs_json
)
SELECT concat('v2sh', substr(md5(concat(n.id, '|', s.status_category)), 1, 32)),
       n.id,
       n.object_level,
       n.id,
       @aircraft_id,
       n.id,
       n.part_instance_id,
       s.status_category,
       s.old_status,
       s.new_status,
       date_add(timestamp('2026-02-01 09:00:00'), interval (s.day_offset + (n.sort_seq % 30)) day),
       concat(n.part_name, s.reason_suffix),
       'codex',
       s.source_table,
       n.id,
       json_object('v2StatusHistory', true, 'rootScope', n.root_scope, 'partNumber', n.part_number)
  FROM tmp_v2_scope_nodes n
  JOIN (
    SELECT 'CONFIGURATION' status_category, 'planned' old_status, 'frozen' new_status, 0 day_offset, ' 构型冻结并纳入第二版卷宗基线。' reason_suffix, 't1_config_baseline' source_table UNION ALL
    SELECT 'QUALITY', 'pending_release', 'released', 38, ' 完成质量放行和证明文件挂接。', 't1_inspection_record' UNION ALL
    SELECT 'SERVICE', 'installed_pending_monitor', 'serviceable', 96, ' 完成装机后功能检查，进入服役监控。', 't1_life_usage_record'
  ) s;

INSERT INTO t1_life_usage_record (
  id, object_profile_id, object_level, object_id, aircraft_id, bom_node_id, part_instance_id,
  usage_source_type, usage_source_id, event_time, fh_delta, fc_delta, landing_delta,
  calendar_days_delta, total_fh_after, total_fc_after, remaining_life_value,
  remaining_life_unit, calculated_by, attrs_json
)
SELECT concat('v2lu', substr(md5(n.id), 1, 32)),
       n.id,
       n.object_level,
       n.id,
       @aircraft_id,
       n.id,
       n.part_instance_id,
       'v2_lifecycle_rollup',
       n.id,
       date_add(timestamp('2026-05-30 18:00:00'), interval (n.sort_seq % 12) day),
       round(4.500 + (conv(substr(md5(n.id), 1, 2), 16, 10) % 24) * 0.250, 3),
       1 + (conv(substr(md5(n.id), 3, 2), 16, 10) % 5),
       CASE WHEN n.root_scope = 'SYS-32' THEN 1 ELSE 0 END,
       30 + (n.sort_seq % 20),
       round(1286.400 + n.sort_seq * 0.037, 3),
       742 + (n.sort_seq % 90),
       CASE WHEN n.node_type IN ('PART', 'COMPONENT', 'EQUIPMENT') THEN round(9000 - (n.sort_seq % 900), 3) ELSE NULL END,
       CASE WHEN n.node_type IN ('PART', 'COMPONENT', 'EQUIPMENT') THEN 'FH' ELSE NULL END,
       'v2_lifecycle_enrichment',
       json_object('scope', n.root_scope, 'positionCode', n.position_code, 'serviceability', 'serviceable')
  FROM tmp_v2_scope_nodes n;

INSERT INTO t1_part_document (
  id, part_number, doc_type, doc_number, doc_revision, doc_title,
  effective_date, file_path, is_current
)
SELECT concat('v2pd', substr(md5(n.id), 1, 32)),
       n.part_number,
       CASE n.node_type
         WHEN 'AIRCRAFT' THEN 'SPEC'
         WHEN 'SYSTEM' THEN 'SPEC'
         WHEN 'SUBSYSTEM' THEN 'SPEC'
         WHEN 'EQUIPMENT' THEN 'IPC'
         WHEN 'COMPONENT' THEN 'CMM'
         WHEN 'PART' THEN 'DRAWING'
         ELSE 'SPEC'
       END,
       concat('DOC-D2-', left(replace(n.part_number, '/', '-'), 60), '-', substr(md5(n.id), 1, 6)),
       'Rev.D2',
       concat(n.part_name, ' 第二版设计/状态证明文件'),
       date('2026-06-01'),
       concat('/project1/dossier/files/2026/06/B-1234/v2/', n.root_scope, '/', replace(n.part_number, '/', '-'), '.pdf'),
       1
  FROM tmp_v2_scope_nodes n;

INSERT INTO t1_work_order (
  id, aircraft_id, bom_node_id, part_instance_id, object_level, object_profile_id,
  wo_number, wo_type, wo_status, open_date, close_date, station, mro_org,
  mro_approval_number, wo_open_tsn_fh, wo_open_tsn_fc, release_date, release_by,
  crs_number, remark
)
SELECT concat('v2wo', substr(md5(n.id), 1, 32)),
       @aircraft_id,
       n.id,
       n.part_instance_id,
       n.object_level,
       n.id,
       concat('WO-D2-', left(replace(n.part_number, '/', '-'), 50), '-', substr(md5(n.id), 1, 6)),
       CASE WHEN n.node_type IN ('SYSTEM', 'SUBSYSTEM') THEN 'C_CHECK' ELSE 'LINE' END,
       'CLOSED',
       date_add(timestamp('2026-05-12 08:30:00'), interval (n.sort_seq % 18) day),
       date_add(timestamp('2026-05-12 18:20:00'), interval (n.sort_seq % 18) day),
       CASE n.root_scope WHEN 'SYS-29' THEN 'COMAC-PD' WHEN 'SYS-32' THEN 'ZSPD' ELSE 'PUDONG' END,
       CASE n.root_scope WHEN 'SYS-29' THEN '中国商飞试飞维修中心' WHEN 'SYS-32' THEN '东航技术有限公司' ELSE '总装交付中心' END,
       'MRO-CN-2026-D2',
       round(1260.000 + n.sort_seq * 0.110, 2),
       720 + (n.sort_seq % 120),
       date_add(timestamp('2026-05-12 19:00:00'), interval (n.sort_seq % 18) day),
       'D2_RELEASE_ENGINEER',
       concat('CRS-D2-', substr(md5(n.id), 1, 10)),
       concat(n.part_name, ' 第二版生命周期补充：状态复核、功能检查、附件核验均关闭。')
  FROM tmp_v2_scope_nodes n
 WHERE n.node_type IN ('SYSTEM', 'SUBSYSTEM', 'EQUIPMENT')
    OR n.part_number = 'HYD-TUBE-MLG-32A';

INSERT INTO t1_fault_event (
  id, aircraft_id, bom_node_id, part_instance_id, object_level, object_profile_id,
  node_id, instance_id, reported_at, flight_leg_id, fault_code_id,
  fault_description, severity, fault_source, status, resolution_type, deferral_ref
)
SELECT concat('v2fe', substr(md5(n.id), 1, 32)),
       @aircraft_id,
       n.id,
       n.part_instance_id,
       n.object_level,
       n.id,
       n.id,
       n.part_instance_id,
       date_add(timestamp('2026-05-18 10:15:00'), interval (n.sort_seq % 16) day),
       NULL,
       NULL,
       CASE
         WHEN n.part_number = 'HYD-TUBE-MLG-32A' THEN '第二版补充：主起供压弯管压力脉动复核，支承垫片更换后复测合格。'
         WHEN n.root_scope = 'SYS-29' THEN concat(n.part_name, ' 压力/污染度趋势触发预防性复查，未发现泄漏。')
         WHEN n.root_scope = 'SYS-32' THEN concat(n.part_name, ' 收放/锁定间隙趋势复核，调整后关闭。')
         ELSE concat(n.part_name, ' 服役监控提示复查，结果可接受。')
       END,
       CASE WHEN n.part_number = 'HYD-TUBE-MLG-32A' THEN 'MAJOR' ELSE 'MINOR' END,
       'MONITORING_ALERT',
       'RESOLVED',
       CASE WHEN n.part_number = 'HYD-TUBE-MLG-32A' THEN 'PART_REPLACED' ELSE 'ADJUSTMENT' END,
       NULL
  FROM tmp_v2_scope_nodes n
 WHERE n.part_number IN ('HYD-TUBE-MLG-32A', 'EQP-HYD-MLG-SUPPLY', 'MLG-ACT-04')
    OR (n.node_type IN ('EQUIPMENT', 'COMPONENT', 'PART') AND conv(substr(md5(n.id), 1, 2), 16, 10) % 37 = 0);

INSERT INTO t1_object_interface (
  id, interface_code, interface_name, interface_type,
  source_object_level, source_object_id, source_bom_node_id,
  target_object_level, target_object_id, target_bom_node_id,
  aircraft_id, interface_summary, requirement_summary,
  maturity_status, verification_status, effectivity, attrs_json
)
SELECT concat('v2if', substr(md5(x.interface_code), 1, 32)),
       x.interface_code,
       x.interface_name,
       x.interface_type,
       x.source_level,
       x.source_id,
       x.source_id,
       x.target_level,
       x.target_id,
       x.target_id,
       @aircraft_id,
       x.interface_summary,
       x.requirement_summary,
       'released',
       'verified',
       json_object('tailNumber', 'B-1234', 'dataBatch', 'SECOND_VERSION_DATA'),
       json_object('patch', 'dossier_v2_lifecycle_enrichment_mysql8.sql', 'criticalInterface', x.critical_flag)
  FROM (
    SELECT 'IF-D2-HYD-LGR-001' interface_code, '液压系统至起落架系统供压接口' interface_name, 'HYDRAULIC' interface_type,
           'system' source_level, @hyd_system_id source_id, 'system' target_level, @lgr_system_id target_id,
           'SYS-29 向 SYS-32 收放、刹车、防滑和转弯功能提供额定 21 MPa 液压能源。' interface_summary,
           '压力 21 MPa，峰值不超过 24 MPa；清洁度 NAS 6 级；关阀冲击不超过结构允许值。' requirement_summary, true critical_flag
    UNION ALL
    SELECT 'IF-D2-HYD-MLG-002', '起落架液压子系统至主起落架接口', 'HYDRAULIC',
           'subsystem', 'f1000032-0032-4032-8032-000000000032', 'equipment', 'f1000003-0003-4003-8003-000000000003',
           '起落架液压子系统向主起落架收放作动筒、锁机构和刹车控制组件供压。',
           '接口软硬管连接需完成气密、压力保持和振动裕度复核。', true
    UNION ALL
    SELECT 'IF-D2-HYD-ACT-003', '主起液压供压设备至支柱作动筒接口', 'HYDRAULIC',
           'equipment', 'f1000033-0033-4033-8033-000000000033', 'equipment', 'f1000004-0004-4004-8004-000000000004',
           '主起供压设备经供压管路组件向支柱作动筒总成供压。',
           '作动筒全行程压力波动不超过 0.6 MPa pp，收放时间 7-11 s。', true
    UNION ALL
    SELECT 'IF-D2-TUBE-PKG-004', '液压弯管至供压管路组件接口', 'HYDRAULIC',
           'part', 'f1000006-0006-4006-8006-000000000006', 'component', 'f1000010-0010-4010-8010-000000000010',
           'HYD-TUBE-MLG-32A 作为供压管路组件关键零件，承担主起供压段压力冲击和振动载荷。',
           '弯管外径 12.7 mm，壁厚 0.9 mm，耐压/气密/支承间距满足 C919-32-1187 Rev.C。', true
    UNION ALL
    SELECT 'IF-D2-LGR-BRAKE-005', '起落架系统至刹车控制接口', 'HYDRAULIC_CONTROL',
           'system', @lgr_system_id, 'subsystem', 'b861b0e0-76d2-bcda-0cec-3f4fa1aa8e6b',
           '起落架系统向刹车控制与防滑子系统传递轮速、压力和刹车温度状态。',
           '防滑控制延迟不超过 80 ms，轮速传感状态需进入 BIT 监控。', false
  ) x;

SELECT 'dossier_v2_lifecycle_enrichment_mysql8.sql applied' AS result,
       (SELECT template_version FROM t1_dossier_template WHERE id = @template_id) AS template_version,
       (SELECT chapter_name FROM t1_dossier_template_chapter WHERE template_id = @template_id AND chapter_code = 'SUBSYSTEM_EQUIPMENT') AS subsystem_equipment_chapter,
       (SELECT count(1) FROM tmp_v2_scope_nodes) AS scoped_node_count,
       (SELECT count(1) FROM t1_object_lifecycle_record WHERE id LIKE 'v2lc%') AS v2_lifecycle_count,
       (SELECT count(1) FROM t1_object_technical_status WHERE id LIKE 'v2ts%') AS v2_technical_status_count,
       (SELECT count(1) FROM t1_object_status_history WHERE id LIKE 'v2sh%') AS v2_status_history_count,
       (SELECT count(1) FROM t1_object_interface WHERE id LIKE 'v2if%') AS v2_interface_count,
       (SELECT count(1) FROM t1_life_usage_record WHERE id LIKE 'v2lu%') AS v2_usage_count,
       (SELECT count(1) FROM t1_work_order WHERE id LIKE 'v2wo%') AS v2_work_order_count,
       (SELECT count(1) FROM t1_fault_event WHERE id LIKE 'v2fe%') AS v2_fault_count,
       (SELECT count(1) FROM t1_part_document WHERE id LIKE 'v2pd%') AS v2_part_document_count;
