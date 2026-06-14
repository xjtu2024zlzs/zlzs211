-- -----------------------------------------------------------------------------
-- System/subsystem profile enrichment and detail views
-- Runtime database: ry-cloud
-- MySQL 8.0+
--
-- Policy:
-- 1. t1_aircraft_bom_node is the source of truth for system/subsystem instances.
-- 2. t1_product_object_profile is the unified object identity for every system and
--    subsystem node.
-- 3. t1_system_object_profile and t1_subsystem_object_profile store display/profile
--    enrichment and cached aggregates only.
-- 4. Lifecycle details remain in event tables: t1_object_lifecycle_record,
--    t1_object_technical_status, t1_object_status_history, t1_life_usage_record,
--    t1_inspection_record, t1_fault_event, t1_work_order, and t1_dossier_content_item.
-- -----------------------------------------------------------------------------

USE `ry-cloud`;

INSERT INTO t1_system_object_profile (
  system_id, object_profile_id, aircraft_id, bom_node_id, system_code, system_name,
  system_name_en, ata_chapter, sns_system_code, system_category, function_summary,
  system_boundary, covered_subsystems, main_interfaces, redundancy_summary,
  configuration_baseline, effectivity, design_status, technical_status,
  safety_classification, criticality_level, quality_status, design_department,
  maintenance_department, system_owner, operational_status, open_fault_count,
  latest_status_date, main_design_doc_id, main_maintenance_doc_id, attrs_json
)
SELECT
  b.id,
  pop.id,
  b.aircraft_id,
  b.id,
  COALESCE(NULLIF(b.part_number, ''), b.id),
  COALESCE(b.part_name, b.part_number, b.id),
  b.part_name_en,
  b.ata_chapter,
  CONCAT('SNS-', REPLACE(COALESCE(b.ata_chapter, 'SYS'), '-', '')),
  CASE
    WHEN b.ata_chapter = '21' THEN 'environmental_control'
    WHEN b.ata_chapter = '23' THEN 'communication_navigation_surveillance'
    WHEN b.ata_chapter = '24' THEN 'electrical_power'
    WHEN b.ata_chapter = '27' THEN 'flight_control'
    WHEN b.ata_chapter = '28' THEN 'fuel'
    WHEN b.ata_chapter = '29' THEN 'hydraulic'
    WHEN b.ata_chapter = '30' THEN 'ice_rain_protection'
    WHEN b.ata_chapter = '32' THEN 'landing_gear'
    WHEN b.ata_chapter = '42' THEN 'avionics_mission'
    WHEN b.ata_chapter = '52' THEN 'door_cabin_emergency'
    WHEN b.ata_chapter = '53' THEN 'airframe_structure'
    WHEN b.ata_chapter = '71' THEN 'powerplant'
    ELSE 'aircraft_system'
  END,
  CONCAT(
    COALESCE(b.part_name, b.part_number, '系统'),
    '用于支撑单机 ',
    COALESCE(pa.tail_number, b.aircraft_id),
    ' 的构型、设计、制造、检验、服役和维修数据组织。'
  ),
  CONCAT(
    '以 BOM 节点 ', COALESCE(b.part_number, b.id),
    ' 为系统边界，向上关联整机节点，向下覆盖直接子系统、设备、组件和零件。'
  ),
  COALESCE(cs.covered_subsystems, '暂无下级子系统，直接挂接设备或部件。'),
  COALESCE(oi.interface_summary, '接口关系可通过 t1_object_interface 按 bom_node_id 或 object_profile_id 下钻查看。'),
  CASE
    WHEN b.ata_chapter IN ('27','29','32') THEN '关键功能具备冗余或备份工作模式，具体余度由设计文件和接口控制文件说明。'
    ELSE '按系统功能分区配置必要冗余或故障隔离能力。'
  END,
  COALESCE(aop.current_configuration_baseline, CONCAT('BL-', COALESCE(pa.tail_number, 'AIRCRAFT'), '-', COALESCE(b.ata_chapter, 'SYS'))),
  JSON_OBJECT(
    'aircraftId', b.aircraft_id,
    'tailNumber', pa.tail_number,
    'ataChapter', b.ata_chapter,
    'currentEffective', b.is_active = 1,
    'bomNodeId', b.id
  ),
  'released',
  CASE WHEN b.is_active = 1 THEN 'released' ELSE 'inactive' END,
  CASE WHEN b.ata_chapter IN ('27','29','32') THEN 'safety_critical' ELSE 'normal' END,
  CASE WHEN b.ata_chapter IN ('27','29','32') THEN 'critical' ELSE 'standard' END,
  'qualified',
  CASE
    WHEN b.ata_chapter IN ('21','29','32') THEN '系统工程与适航验证部'
    WHEN b.ata_chapter IN ('23','24','27','42') THEN '航电与控制系统设计部'
    WHEN b.ata_chapter IN ('52','53','71') THEN '结构与动力装置设计部'
    ELSE '总体设计部'
  END,
  CASE
    WHEN b.ata_chapter IN ('29','32') THEN '维修工程与持续适航部'
    ELSE '运营保障与维修工程部'
  END,
  COALESCE(aop.operator_org, aop.owner_org, pa.current_operator, '系统责任单位待确认'),
  CASE WHEN b.is_active = 1 THEN 'active' ELSE 'inactive' END,
  COALESCE(fs.open_fault_count, 0),
  COALESCE(ls.latest_event_time, sh.latest_change_time, b.updated_at),
  NULL,
  NULL,
  JSON_OBJECT(
    'profilePolicy', 't1_aircraft_bom_node is canonical for system instance fields',
    'mockDataBatch', 'SYSTEM_SUBSYSTEM_PROFILE_ENRICH_20260609',
    'mockData', true,
    'lifecycleStorage', 'event tables only; profile keeps aggregates and display fields',
    'directSubsystemCount', COALESCE(cs.subsystem_count, 0),
    'directEquipmentCount', COALESCE(eq.equipment_count, 0),
    'dossierContentCount', COALESCE(dc.content_count, 0)
  )
FROM t1_aircraft_bom_node b
LEFT JOIN t1_physical_aircraft pa ON pa.id = b.aircraft_id
LEFT JOIN t1_aircraft_object_profile aop ON aop.aircraft_id = b.aircraft_id
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = b.id
LEFT JOIN (
  SELECT
    parent_id,
    COUNT(*) AS subsystem_count,
    GROUP_CONCAT(part_name ORDER BY position_code SEPARATOR '；') AS covered_subsystems
  FROM t1_aircraft_bom_node
  WHERE UPPER(node_type) = 'SUBSYSTEM'
  GROUP BY parent_id
) cs ON cs.parent_id = b.id
LEFT JOIN (
  SELECT parent_id, COUNT(*) AS equipment_count
  FROM t1_aircraft_bom_node
  WHERE UPPER(node_type) = 'EQUIPMENT'
  GROUP BY parent_id
) eq ON eq.parent_id = b.id
LEFT JOIN (
  SELECT
    x.bom_node_id,
    GROUP_CONCAT(x.interface_name ORDER BY x.interface_name SEPARATOR '；') AS interface_summary
  FROM (
    SELECT source_bom_node_id AS bom_node_id, interface_name
    FROM t1_object_interface
    WHERE source_bom_node_id IS NOT NULL
    UNION ALL
    SELECT target_bom_node_id AS bom_node_id, interface_name
    FROM t1_object_interface
    WHERE target_bom_node_id IS NOT NULL
  ) x
  GROUP BY x.bom_node_id
) oi ON oi.bom_node_id = b.id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS open_fault_count
  FROM t1_fault_event
  WHERE UPPER(status) NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')
  GROUP BY bom_node_id
) fs ON fs.bom_node_id = b.id
LEFT JOIN (
  SELECT bom_node_id, MAX(event_time) AS latest_event_time
  FROM t1_object_lifecycle_record
  WHERE object_level = 'system'
  GROUP BY bom_node_id
) ls ON ls.bom_node_id = b.id
LEFT JOIN (
  SELECT bom_node_id, MAX(change_time) AS latest_change_time
  FROM t1_object_status_history
  WHERE object_level = 'system'
  GROUP BY bom_node_id
) sh ON sh.bom_node_id = b.id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS content_count
  FROM t1_dossier_content_item
  GROUP BY bom_node_id
) dc ON dc.bom_node_id = b.id
WHERE UPPER(b.node_type) = 'SYSTEM'
ON DUPLICATE KEY UPDATE
  object_profile_id = VALUES(object_profile_id),
  aircraft_id = VALUES(aircraft_id),
  bom_node_id = VALUES(bom_node_id),
  system_code = VALUES(system_code),
  system_name = VALUES(system_name),
  system_name_en = VALUES(system_name_en),
  ata_chapter = VALUES(ata_chapter),
  sns_system_code = VALUES(sns_system_code),
  system_category = VALUES(system_category),
  function_summary = VALUES(function_summary),
  system_boundary = VALUES(system_boundary),
  covered_subsystems = VALUES(covered_subsystems),
  main_interfaces = VALUES(main_interfaces),
  redundancy_summary = VALUES(redundancy_summary),
  configuration_baseline = VALUES(configuration_baseline),
  effectivity = VALUES(effectivity),
  design_status = VALUES(design_status),
  technical_status = VALUES(technical_status),
  safety_classification = VALUES(safety_classification),
  criticality_level = VALUES(criticality_level),
  quality_status = VALUES(quality_status),
  design_department = VALUES(design_department),
  maintenance_department = VALUES(maintenance_department),
  system_owner = VALUES(system_owner),
  operational_status = VALUES(operational_status),
  open_fault_count = VALUES(open_fault_count),
  latest_status_date = VALUES(latest_status_date),
  attrs_json = JSON_MERGE_PATCH(COALESCE(t1_system_object_profile.attrs_json, JSON_OBJECT()), VALUES(attrs_json)),
  updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_subsystem_object_profile (
  subsystem_id, object_profile_id, aircraft_id, system_id, parent_bom_node_id,
  bom_node_id, subsystem_code, subsystem_name, ata_section, sns_subsystem_code,
  function_area, function_summary, boundary_description, included_equipment_count,
  main_interfaces, configuration_baseline, effectivity, design_status,
  operational_status, health_status, quality_status, criticality_level,
  key_quality_characteristics, design_department, maintenance_department,
  interface_doc_id, verification_doc_id, attrs_json
)
SELECT
  b.id,
  pop.id,
  b.aircraft_id,
  sop.system_id,
  b.parent_id,
  b.id,
  COALESCE(NULLIF(b.part_number, ''), b.id),
  COALESCE(b.part_name, b.part_number, b.id),
  b.ata_chapter,
  CONCAT('SNS-', REPLACE(COALESCE(b.ata_chapter, 'SUB'), '-', '')),
  COALESCE(parent_system.system_category, 'aircraft_subsystem'),
  CONCAT(
    COALESCE(b.part_name, b.part_number, '子系统'),
    '隶属于 ',
    COALESCE(parent_system.system_name, parent_bom.part_name, '上级系统'),
    '，用于组织该子系统的构型、质量、服役和维修数据。'
  ),
  CONCAT(
    '以 BOM 节点 ', COALESCE(b.part_number, b.id),
    ' 为子系统边界，上级系统节点为 ',
    COALESCE(parent_bom.part_number, b.parent_id, '未指定'),
    '。'
  ),
  COALESCE(eq.equipment_count, 0),
  COALESCE(oi.interface_summary, '接口关系可通过 t1_object_interface 按 bom_node_id 或 object_profile_id 下钻查看。'),
  COALESCE(parent_system.configuration_baseline, aop.current_configuration_baseline, CONCAT('BL-', COALESCE(pa.tail_number, 'AIRCRAFT'), '-', COALESCE(b.ata_chapter, 'SUB'))),
  JSON_OBJECT(
    'aircraftId', b.aircraft_id,
    'tailNumber', pa.tail_number,
    'ataSection', b.ata_chapter,
    'parentSystemBomNodeId', b.parent_id,
    'currentEffective', b.is_active = 1,
    'bomNodeId', b.id
  ),
  'released',
  CASE WHEN b.is_active = 1 THEN 'active' ELSE 'inactive' END,
  CASE WHEN COALESCE(fs.open_fault_count, 0) > 0 THEN 'attention' ELSE 'healthy' END,
  'qualified',
  CASE WHEN LEFT(COALESCE(b.ata_chapter, ''), 2) IN ('27','29','32') THEN 'critical' ELSE 'standard' END,
  CONCAT(
    '关键质量特性按 ',
    COALESCE(parent_bom.part_name, parent_system.system_name, '上级系统'),
    ' 要求管控，重点关注接口、安装、功能试验和状态闭环。'
  ),
  COALESCE(parent_system.design_department, '总体设计部'),
  COALESCE(parent_system.maintenance_department, '运营保障与维修工程部'),
  NULL,
  NULL,
  JSON_OBJECT(
    'profilePolicy', 't1_aircraft_bom_node is canonical for subsystem instance fields',
    'mockDataBatch', 'SYSTEM_SUBSYSTEM_PROFILE_ENRICH_20260609',
    'mockData', true,
    'lifecycleStorage', 'event tables only; profile keeps aggregates and display fields',
    'directEquipmentCount', COALESCE(eq.equipment_count, 0),
    'dossierContentCount', COALESCE(dc.content_count, 0),
    'parentSystemCode', COALESCE(parent_system.system_code, parent_bom.part_number)
  )
FROM t1_aircraft_bom_node b
LEFT JOIN t1_aircraft_bom_node parent_bom ON parent_bom.id = b.parent_id
LEFT JOIN t1_physical_aircraft pa ON pa.id = b.aircraft_id
LEFT JOIN t1_aircraft_object_profile aop ON aop.aircraft_id = b.aircraft_id
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = b.id
LEFT JOIN t1_system_object_profile sop ON sop.bom_node_id = b.parent_id
LEFT JOIN t1_system_object_profile parent_system ON parent_system.bom_node_id = b.parent_id
LEFT JOIN (
  SELECT parent_id, COUNT(*) AS equipment_count
  FROM t1_aircraft_bom_node
  WHERE UPPER(node_type) = 'EQUIPMENT'
  GROUP BY parent_id
) eq ON eq.parent_id = b.id
LEFT JOIN (
  SELECT
    x.bom_node_id,
    GROUP_CONCAT(x.interface_name ORDER BY x.interface_name SEPARATOR '；') AS interface_summary
  FROM (
    SELECT source_bom_node_id AS bom_node_id, interface_name
    FROM t1_object_interface
    WHERE source_bom_node_id IS NOT NULL
    UNION ALL
    SELECT target_bom_node_id AS bom_node_id, interface_name
    FROM t1_object_interface
    WHERE target_bom_node_id IS NOT NULL
  ) x
  GROUP BY x.bom_node_id
) oi ON oi.bom_node_id = b.id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS open_fault_count
  FROM t1_fault_event
  WHERE UPPER(status) NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')
  GROUP BY bom_node_id
) fs ON fs.bom_node_id = b.id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS content_count
  FROM t1_dossier_content_item
  GROUP BY bom_node_id
) dc ON dc.bom_node_id = b.id
WHERE UPPER(b.node_type) = 'SUBSYSTEM'
ON DUPLICATE KEY UPDATE
  object_profile_id = VALUES(object_profile_id),
  aircraft_id = VALUES(aircraft_id),
  system_id = VALUES(system_id),
  parent_bom_node_id = VALUES(parent_bom_node_id),
  bom_node_id = VALUES(bom_node_id),
  subsystem_code = VALUES(subsystem_code),
  subsystem_name = VALUES(subsystem_name),
  ata_section = VALUES(ata_section),
  sns_subsystem_code = VALUES(sns_subsystem_code),
  function_area = VALUES(function_area),
  function_summary = VALUES(function_summary),
  boundary_description = VALUES(boundary_description),
  included_equipment_count = VALUES(included_equipment_count),
  main_interfaces = VALUES(main_interfaces),
  configuration_baseline = VALUES(configuration_baseline),
  effectivity = VALUES(effectivity),
  design_status = VALUES(design_status),
  operational_status = VALUES(operational_status),
  health_status = VALUES(health_status),
  quality_status = VALUES(quality_status),
  criticality_level = VALUES(criticality_level),
  key_quality_characteristics = VALUES(key_quality_characteristics),
  design_department = VALUES(design_department),
  maintenance_department = VALUES(maintenance_department),
  attrs_json = JSON_MERGE_PATCH(COALESCE(t1_subsystem_object_profile.attrs_json, JSON_OBJECT()), VALUES(attrs_json)),
  updated_at = CURRENT_TIMESTAMP(6);

CREATE OR REPLACE VIEW v_system_profile_detail AS
SELECT
  sop.system_id,
  sop.object_profile_id,
  sop.aircraft_id,
  pa.tail_number,
  sop.bom_node_id,
  b.parent_id AS aircraft_root_bom_node_id,
  sop.system_code,
  sop.system_name,
  sop.system_name_en,
  sop.ata_chapter,
  sop.sns_system_code,
  sop.system_category,
  sop.function_summary,
  sop.system_boundary,
  sop.covered_subsystems,
  sop.main_interfaces,
  sop.redundancy_summary,
  sop.configuration_baseline,
  sop.design_status,
  sop.technical_status,
  sop.safety_classification,
  sop.criticality_level,
  sop.quality_status,
  sop.operational_status,
  sop.design_department,
  sop.maintenance_department,
  sop.system_owner,
  sop.open_fault_count,
  sop.latest_status_date,
  COALESCE(child.subsystem_count, 0) AS subsystem_count,
  COALESCE(child.direct_equipment_count, 0) AS direct_equipment_count,
  COALESCE(lc.design_count, 0) AS design_event_count,
  COALESCE(lc.manufacturing_count, 0) AS manufacturing_event_count,
  COALESCE(lc.installation_count, 0) AS installation_event_count,
  COALESCE(lc.inspection_count, 0) AS inspection_event_count,
  COALESCE(lc.service_count, 0) AS service_event_count,
  COALESCE(lc.fault_count, 0) AS fault_event_timeline_count,
  COALESCE(lc.technical_status_count, 0) AS technical_status_event_count,
  COALESCE(ts.technical_status_record_count, 0) AS technical_status_record_count,
  COALESCE(sh.status_history_count, 0) AS status_history_count,
  COALESCE(ir.inspection_record_count, 0) AS inspection_record_count,
  COALESCE(wo.work_order_count, 0) AS work_order_count,
  COALESCE(fe.fault_event_count, 0) AS fault_event_count,
  COALESCE(dc.dossier_content_count, 0) AS dossier_content_count,
  COALESCE(dc.design_content_count, 0) AS design_content_count,
  COALESCE(dc.manufacturing_content_count, 0) AS manufacturing_content_count,
  COALESCE(dc.installation_content_count, 0) AS installation_content_count,
  COALESCE(dc.inspection_content_count, 0) AS inspection_content_count,
  COALESCE(dc.service_content_count, 0) AS service_content_count,
  COALESCE(dc.fault_content_count, 0) AS fault_content_count,
  COALESCE(dc.technical_status_content_count, 0) AS technical_status_content_count,
  COALESCE(dc.document_content_count, 0) AS document_content_count,
  sop.attrs_json,
  sop.updated_at
FROM t1_system_object_profile sop
JOIN t1_aircraft_bom_node b ON b.id = sop.bom_node_id
LEFT JOIN t1_physical_aircraft pa ON pa.id = sop.aircraft_id
LEFT JOIN (
  SELECT
    parent_id AS bom_node_id,
    SUM(CASE WHEN UPPER(node_type) = 'SUBSYSTEM' THEN 1 ELSE 0 END) AS subsystem_count,
    SUM(CASE WHEN UPPER(node_type) = 'EQUIPMENT' THEN 1 ELSE 0 END) AS direct_equipment_count
  FROM t1_aircraft_bom_node
  GROUP BY parent_id
) child ON child.bom_node_id = sop.bom_node_id
LEFT JOIN (
  SELECT
    bom_node_id,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'design' THEN 1 ELSE 0 END) AS design_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'manufacturing' THEN 1 ELSE 0 END) AS manufacturing_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'installation' THEN 1 ELSE 0 END) AS installation_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'inspection' THEN 1 ELSE 0 END) AS inspection_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'service' THEN 1 ELSE 0 END) AS service_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'fault' THEN 1 ELSE 0 END) AS fault_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'technical_status' THEN 1 ELSE 0 END) AS technical_status_count
  FROM t1_object_lifecycle_record
  WHERE object_level = 'system'
  GROUP BY bom_node_id
) lc ON lc.bom_node_id = sop.bom_node_id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS technical_status_record_count
  FROM t1_object_technical_status
  WHERE object_level = 'system'
  GROUP BY bom_node_id
) ts ON ts.bom_node_id = sop.bom_node_id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS status_history_count
  FROM t1_object_status_history
  WHERE object_level = 'system'
  GROUP BY bom_node_id
) sh ON sh.bom_node_id = sop.bom_node_id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS inspection_record_count
  FROM t1_inspection_record
  WHERE object_level = 'system'
  GROUP BY bom_node_id
) ir ON ir.bom_node_id = sop.bom_node_id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS work_order_count
  FROM t1_work_order
  WHERE object_level = 'system'
  GROUP BY bom_node_id
) wo ON wo.bom_node_id = sop.bom_node_id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS fault_event_count
  FROM t1_fault_event
  WHERE object_level = 'system'
  GROUP BY bom_node_id
) fe ON fe.bom_node_id = sop.bom_node_id
LEFT JOIN (
  SELECT
    bom_node_id,
    COUNT(*) AS dossier_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'design' THEN 1 ELSE 0 END) AS design_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'manufacturing' THEN 1 ELSE 0 END) AS manufacturing_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'installation' THEN 1 ELSE 0 END) AS installation_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'inspection' THEN 1 ELSE 0 END) AS inspection_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'service' THEN 1 ELSE 0 END) AS service_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'fault' THEN 1 ELSE 0 END) AS fault_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'technical_status' THEN 1 ELSE 0 END) AS technical_status_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'document' THEN 1 ELSE 0 END) AS document_content_count
  FROM t1_dossier_content_item
  GROUP BY bom_node_id
) dc ON dc.bom_node_id = sop.bom_node_id;

CREATE OR REPLACE VIEW v_subsystem_profile_detail AS
SELECT
  ssop.subsystem_id,
  ssop.object_profile_id,
  ssop.aircraft_id,
  pa.tail_number,
  ssop.system_id,
  sop.system_code,
  sop.system_name,
  ssop.parent_bom_node_id,
  ssop.bom_node_id,
  ssop.subsystem_code,
  ssop.subsystem_name,
  ssop.ata_section,
  ssop.sns_subsystem_code,
  ssop.function_area,
  ssop.function_summary,
  ssop.boundary_description,
  ssop.included_equipment_count,
  ssop.main_interfaces,
  ssop.configuration_baseline,
  ssop.design_status,
  ssop.operational_status,
  ssop.health_status,
  ssop.quality_status,
  ssop.criticality_level,
  ssop.key_quality_characteristics,
  ssop.design_department,
  ssop.maintenance_department,
  COALESCE(child.direct_equipment_count, 0) AS direct_equipment_count,
  COALESCE(lc.design_count, 0) AS design_event_count,
  COALESCE(lc.manufacturing_count, 0) AS manufacturing_event_count,
  COALESCE(lc.installation_count, 0) AS installation_event_count,
  COALESCE(lc.inspection_count, 0) AS inspection_event_count,
  COALESCE(lc.service_count, 0) AS service_event_count,
  COALESCE(lc.technical_status_count, 0) AS technical_status_event_count,
  COALESCE(ts.technical_status_record_count, 0) AS technical_status_record_count,
  COALESCE(sh.status_history_count, 0) AS status_history_count,
  COALESCE(ir.inspection_record_count, 0) AS inspection_record_count,
  COALESCE(wo.work_order_count, 0) AS work_order_count,
  COALESCE(fe.fault_event_count, 0) AS fault_event_count,
  COALESCE(dc.dossier_content_count, 0) AS dossier_content_count,
  COALESCE(dc.design_content_count, 0) AS design_content_count,
  COALESCE(dc.manufacturing_content_count, 0) AS manufacturing_content_count,
  COALESCE(dc.installation_content_count, 0) AS installation_content_count,
  COALESCE(dc.inspection_content_count, 0) AS inspection_content_count,
  COALESCE(dc.service_content_count, 0) AS service_content_count,
  COALESCE(dc.fault_content_count, 0) AS fault_content_count,
  COALESCE(dc.technical_status_content_count, 0) AS technical_status_content_count,
  COALESCE(dc.document_content_count, 0) AS document_content_count,
  ssop.attrs_json,
  ssop.updated_at
FROM t1_subsystem_object_profile ssop
JOIN t1_aircraft_bom_node b ON b.id = ssop.bom_node_id
LEFT JOIN t1_system_object_profile sop ON sop.system_id = ssop.system_id
LEFT JOIN t1_physical_aircraft pa ON pa.id = ssop.aircraft_id
LEFT JOIN (
  SELECT
    parent_id AS bom_node_id,
    SUM(CASE WHEN UPPER(node_type) = 'EQUIPMENT' THEN 1 ELSE 0 END) AS direct_equipment_count
  FROM t1_aircraft_bom_node
  GROUP BY parent_id
) child ON child.bom_node_id = ssop.bom_node_id
LEFT JOIN (
  SELECT
    bom_node_id,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'design' THEN 1 ELSE 0 END) AS design_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'manufacturing' THEN 1 ELSE 0 END) AS manufacturing_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'installation' THEN 1 ELSE 0 END) AS installation_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'inspection' THEN 1 ELSE 0 END) AS inspection_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'service' THEN 1 ELSE 0 END) AS service_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'technical_status' THEN 1 ELSE 0 END) AS technical_status_count
  FROM t1_object_lifecycle_record
  WHERE object_level = 'subsystem'
  GROUP BY bom_node_id
) lc ON lc.bom_node_id = ssop.bom_node_id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS technical_status_record_count
  FROM t1_object_technical_status
  WHERE object_level = 'subsystem'
  GROUP BY bom_node_id
) ts ON ts.bom_node_id = ssop.bom_node_id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS status_history_count
  FROM t1_object_status_history
  WHERE object_level = 'subsystem'
  GROUP BY bom_node_id
) sh ON sh.bom_node_id = ssop.bom_node_id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS inspection_record_count
  FROM t1_inspection_record
  WHERE object_level = 'subsystem'
  GROUP BY bom_node_id
) ir ON ir.bom_node_id = ssop.bom_node_id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS work_order_count
  FROM t1_work_order
  WHERE object_level = 'subsystem'
  GROUP BY bom_node_id
) wo ON wo.bom_node_id = ssop.bom_node_id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS fault_event_count
  FROM t1_fault_event
  WHERE object_level = 'subsystem'
  GROUP BY bom_node_id
) fe ON fe.bom_node_id = ssop.bom_node_id
LEFT JOIN (
  SELECT
    bom_node_id,
    COUNT(*) AS dossier_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'design' THEN 1 ELSE 0 END) AS design_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'manufacturing' THEN 1 ELSE 0 END) AS manufacturing_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'installation' THEN 1 ELSE 0 END) AS installation_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'inspection' THEN 1 ELSE 0 END) AS inspection_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'service' THEN 1 ELSE 0 END) AS service_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'fault' THEN 1 ELSE 0 END) AS fault_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'technical_status' THEN 1 ELSE 0 END) AS technical_status_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'document' THEN 1 ELSE 0 END) AS document_content_count
  FROM t1_dossier_content_item
  GROUP BY bom_node_id
) dc ON dc.bom_node_id = ssop.bom_node_id;

CREATE OR REPLACE VIEW v_system_profile_coverage_check AS
SELECT
  b.id AS bom_node_id,
  b.aircraft_id,
  b.part_number AS system_code,
  b.part_name AS system_name,
  CASE
    WHEN sop.system_id IS NULL THEN 'missing_system_object_profile'
    WHEN pop.id IS NULL THEN 'missing_product_object_profile'
    WHEN NOT (sop.object_profile_id <=> pop.id) THEN 'object_profile_id_mismatch'
    WHEN NOT (sop.system_code <=> COALESCE(NULLIF(b.part_number, ''), b.id)) THEN 'system_code_mismatch'
    WHEN NOT (sop.system_name <=> COALESCE(b.part_name, b.part_number, b.id)) THEN 'system_name_mismatch'
    ELSE 'ok'
  END AS issue_type
FROM t1_aircraft_bom_node b
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = b.id
LEFT JOIN t1_system_object_profile sop ON sop.bom_node_id = b.id
WHERE UPPER(b.node_type) = 'SYSTEM'
  AND (
    sop.system_id IS NULL
    OR pop.id IS NULL
    OR NOT (sop.object_profile_id <=> pop.id)
    OR NOT (sop.system_code <=> COALESCE(NULLIF(b.part_number, ''), b.id))
    OR NOT (sop.system_name <=> COALESCE(b.part_name, b.part_number, b.id))
  );

CREATE OR REPLACE VIEW v_subsystem_profile_coverage_check AS
SELECT
  b.id AS bom_node_id,
  b.aircraft_id,
  b.parent_id AS parent_system_bom_node_id,
  b.part_number AS subsystem_code,
  b.part_name AS subsystem_name,
  CASE
    WHEN ssop.subsystem_id IS NULL THEN 'missing_subsystem_object_profile'
    WHEN parent_sop.system_id IS NULL THEN 'missing_parent_system_profile'
    WHEN pop.id IS NULL THEN 'missing_product_object_profile'
    WHEN NOT (ssop.object_profile_id <=> pop.id) THEN 'object_profile_id_mismatch'
    WHEN NOT (ssop.system_id <=> parent_sop.system_id) THEN 'parent_system_id_mismatch'
    WHEN NOT (ssop.subsystem_code <=> COALESCE(NULLIF(b.part_number, ''), b.id)) THEN 'subsystem_code_mismatch'
    WHEN NOT (ssop.subsystem_name <=> COALESCE(b.part_name, b.part_number, b.id)) THEN 'subsystem_name_mismatch'
    ELSE 'ok'
  END AS issue_type
FROM t1_aircraft_bom_node b
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = b.id
LEFT JOIN t1_subsystem_object_profile ssop ON ssop.bom_node_id = b.id
LEFT JOIN t1_system_object_profile parent_sop ON parent_sop.bom_node_id = b.parent_id
WHERE UPPER(b.node_type) = 'SUBSYSTEM'
  AND (
    ssop.subsystem_id IS NULL
    OR parent_sop.system_id IS NULL
    OR pop.id IS NULL
    OR NOT (ssop.object_profile_id <=> pop.id)
    OR NOT (ssop.system_id <=> parent_sop.system_id)
    OR NOT (ssop.subsystem_code <=> COALESCE(NULLIF(b.part_number, ''), b.id))
    OR NOT (ssop.subsystem_name <=> COALESCE(b.part_name, b.part_number, b.id))
  );
