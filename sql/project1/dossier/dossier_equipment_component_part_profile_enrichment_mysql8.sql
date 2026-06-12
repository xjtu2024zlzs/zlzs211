-- -----------------------------------------------------------------------------
-- Equipment/component/part enrichment and detail views
-- Runtime database: ry-cloud
-- MySQL 8.0+
--
-- Policy:
-- 1. t1_aircraft_bom_node is the source of truth for installed structure nodes.
-- 2. t1_product_object_profile is the software-owned unified object identity.
-- 3. t1_part_master and t1_part_instance store common item-number and physical-instance
--    traceability for equipment, components, and parts.
-- 4. equipment_object_* and component_object_* store layer-specific enrichment.
-- 5. Part layer currently has no part_object_* pair; v_part_profile_detail uses
--    t1_part_master, t1_part_instance, t1_aircraft_bom_node, and event tables.
-- -----------------------------------------------------------------------------

USE `ry-cloud`;

-- Fill minimal item-number master records for BOM nodes whose part_number is not
-- yet represented in t1_part_master. This keeps detail views complete without
-- changing the BOM or inventing physical instances.
INSERT INTO t1_part_master (
  part_number, part_name, part_name_en, ata_chapter, part_category,
  unit_of_measure, material, is_life_limited, is_serialized, is_rotable,
  is_expendable, is_critical, criticality_level, key_quality_characteristics,
  default_supply_mode, design_params_json
)
SELECT
  b.part_number,
  COALESCE(MAX(NULLIF(b.part_name, '')), b.part_number),
  MAX(b.part_name_en),
  MAX(b.ata_chapter),
  CASE
    WHEN MAX(UPPER(b.node_type)) = 'EQUIPMENT' THEN 'VENDOR_PART'
    ELSE 'MANUFACTURER_PART'
  END,
  COALESCE(MAX(NULLIF(b.unit, '')), 'EA'),
  '按构型节点补充的材料信息',
  MAX(b.is_life_limited),
  MAX(b.is_serialized),
  MAX(b.is_rotable),
  MAX(b.is_expendable),
  CASE WHEN MAX(UPPER(b.node_type)) IN ('EQUIPMENT','COMPONENT') THEN 1 ELSE MAX(b.is_life_limited) END,
  CASE WHEN MAX(UPPER(b.node_type)) IN ('EQUIPMENT','COMPONENT') THEN 'important' ELSE 'standard' END,
  CONCAT(COALESCE(MAX(NULLIF(b.part_name, '')), b.part_number), ' 关键质量特性按构型、检验和放行记录追溯。'),
  CASE WHEN MAX(UPPER(b.node_type)) = 'COMPONENT' THEN 'MAKE_OR_BUY' ELSE 'BUY' END,
  JSON_OBJECT(
    'mockDataBatch', 'EQUIPMENT_COMPONENT_PART_PROFILE_ENRICH_20260610',
    'sourceTable', 't1_aircraft_bom_node',
    'sourceNodeType', MAX(UPPER(b.node_type)),
    'sourcePartNumber', b.part_number
  )
FROM t1_aircraft_bom_node b
LEFT JOIN t1_part_master pm ON pm.part_number = b.part_number
WHERE UPPER(b.node_type) IN ('EQUIPMENT','COMPONENT','PART')
  AND b.part_number IS NOT NULL
  AND b.part_number <> ''
  AND pm.part_number IS NULL
GROUP BY b.part_number
ON DUPLICATE KEY UPDATE
  part_name = VALUES(part_name),
  part_name_en = COALESCE(t1_part_master.part_name_en, VALUES(part_name_en)),
  ata_chapter = COALESCE(t1_part_master.ata_chapter, VALUES(ata_chapter)),
  part_category = COALESCE(t1_part_master.part_category, VALUES(part_category)),
  key_quality_characteristics = COALESCE(t1_part_master.key_quality_characteristics, VALUES(key_quality_characteristics)),
  design_params_json = JSON_MERGE_PATCH(COALESCE(t1_part_master.design_params_json, JSON_OBJECT()), VALUES(design_params_json)),
  updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_equipment_object_master (
  equipment_master_id, equipment_code, equipment_name, equipment_type, part_number,
  model_spec, manufacturer, supplier_name, supplier_code, configuration_item_flag,
  maintainability_type, life_limit, tbo, criticality_level, failure_effect_summary,
  certificate_doc_id, test_report_doc_id, attrs_json
)
SELECT
  MIN(b.id),
  CASE
    WHEN MAX(NULLIF(b.part_number, '')) IS NOT NULL AND CHAR_LENGTH(MAX(NULLIF(b.part_number, ''))) <= 100
      THEN MAX(NULLIF(b.part_number, ''))
    ELSE CONCAT('EQ-', SUBSTRING(MD5(COALESCE(MAX(NULLIF(b.part_number, '')), MIN(b.id))), 1, 32))
  END,
  COALESCE(MAX(NULLIF(b.part_name, '')), MAX(NULLIF(b.part_number, '')), MIN(b.id)),
  CASE
    WHEN MAX(b.ata_chapter) = '29' THEN 'hydraulic_equipment'
    WHEN MAX(b.ata_chapter) = '32' THEN 'landing_gear_equipment'
    WHEN MAX(b.ata_chapter) = '27' THEN 'flight_control_equipment'
    ELSE 'aircraft_equipment'
  END,
  MAX(NULLIF(b.part_number, '')),
  COALESCE(MAX(pm.specification), MAX(NULLIF(b.position_desc, '')), CONCAT('ATA-', COALESCE(MAX(b.ata_chapter), 'NA'))),
  COALESCE(MAX(NULLIF(b.manufacturer, '')), MAX(pm.material_spec), '供应商待确认'),
  COALESCE(MAX(NULLIF(b.manufacturer, '')), '供应商待确认'),
  MAX(NULLIF(b.cage_code, '')),
  1,
  CASE WHEN MAX(b.is_rotable) = 1 THEN 'rotable' ELSE 'line_replaceable' END,
  CASE
    WHEN MAX(b.is_life_limited) = 1 THEN CONCAT('寿命限制：', COALESCE(MAX(pm.life_limit_fh), 0), ' FH / ', COALESCE(MAX(pm.life_limit_fc), 0), ' FC')
    ELSE '按持续适航和维修大纲控制'
  END,
  '按设备维修大纲和可靠性监控确定',
  COALESCE(MAX(pm.criticality_level), CASE WHEN MAX(b.ata_chapter) IN ('27','29','32') THEN 'critical' ELSE 'standard' END),
  CONCAT(COALESCE(MAX(NULLIF(b.part_name, '')), MAX(NULLIF(b.part_number, '')), '设备'), ' 失效影响通过系统安全性、接口和故障闭环记录追溯。'),
  NULL,
  NULL,
  JSON_OBJECT(
    'mockDataBatch', 'EQUIPMENT_COMPONENT_PART_PROFILE_ENRICH_20260610',
    'sourceTable', 't1_aircraft_bom_node',
    'sourceNodeType', 'EQUIPMENT',
    'bomNodeCount', COUNT(*)
  )
FROM t1_aircraft_bom_node b
LEFT JOIN t1_part_master pm ON pm.part_number = b.part_number
WHERE UPPER(b.node_type) = 'EQUIPMENT'
GROUP BY CASE
  WHEN b.part_number IS NOT NULL AND b.part_number <> '' AND CHAR_LENGTH(b.part_number) <= 100
    THEN b.part_number
  ELSE CONCAT('EQ-', SUBSTRING(MD5(COALESCE(NULLIF(b.part_number, ''), b.id)), 1, 32))
END
ON DUPLICATE KEY UPDATE
  equipment_name = VALUES(equipment_name),
  equipment_type = VALUES(equipment_type),
  part_number = VALUES(part_number),
  model_spec = VALUES(model_spec),
  manufacturer = VALUES(manufacturer),
  supplier_name = VALUES(supplier_name),
  supplier_code = VALUES(supplier_code),
  configuration_item_flag = VALUES(configuration_item_flag),
  maintainability_type = VALUES(maintainability_type),
  life_limit = VALUES(life_limit),
  tbo = VALUES(tbo),
  criticality_level = VALUES(criticality_level),
  failure_effect_summary = VALUES(failure_effect_summary),
  attrs_json = JSON_MERGE_PATCH(COALESCE(t1_equipment_object_master.attrs_json, JSON_OBJECT()), VALUES(attrs_json)),
  updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_equipment_object_instance (
  equipment_instance_id, equipment_master_id, object_profile_id, aircraft_id,
  system_id, subsystem_id, bom_node_id, part_instance_id, equipment_code,
  equipment_name, part_number, serial_number, batch_number, software_version,
  hardware_version, manufacturer, supplier_name, supplier_code, manufacture_date,
  delivery_batch, installation_position, position_code, installation_date,
  installation_status, configuration_version, modification_status, effectivity,
  tsn, csn, tso, cso, operational_status, quality_status,
  airworthiness_release_status, trace_code, attrs_json
)
SELECT
  b.id,
  em.equipment_master_id,
  pop.id,
  b.aircraft_id,
  COALESCE(parent_system.system_id, parent_subsystem.system_id),
  parent_subsystem.subsystem_id,
  b.id,
  b.part_instance_id,
  em.equipment_code,
  COALESCE(b.part_name, em.equipment_name, b.part_number, b.id),
  b.part_number,
  COALESCE(b.serial_number, pi.serial_number),
  COALESCE(b.batch_number, pi.batch_number),
  JSON_UNQUOTE(JSON_EXTRACT(pop.attrs_json, '$.softwareVersion')),
  JSON_UNQUOTE(JSON_EXTRACT(pop.attrs_json, '$.hardwareVersion')),
  COALESCE(NULLIF(b.manufacturer, ''), pi.manufacturer, em.manufacturer),
  em.supplier_name,
  COALESCE(NULLIF(b.cage_code, ''), em.supplier_code),
  pi.manufacture_date,
  COALESCE(pi.lot_number, pi.batch_number, b.batch_number),
  COALESCE(b.position_desc, pi.installation_position),
  COALESCE(b.position_code, pi.position_code),
  COALESCE(b.install_date, pi.installation_date),
  CASE WHEN b.is_active = 1 THEN 'installed' ELSE 'removed' END,
  COALESCE(JSON_UNQUOTE(JSON_EXTRACT(pop.effectivity, '$.excelBomVersion')), aop.current_bom_version, 'as-maintained'),
  'baseline',
  JSON_OBJECT(
    'aircraftId', b.aircraft_id,
    'bomNodeId', b.id,
    'currentEffective', b.is_active = 1,
    'partInstanceId', b.part_instance_id
  ),
  COALESCE(b.tsn_fh, pi.tsn_fh_at_birth),
  COALESCE(b.tsn_fc, pi.tsn_fc_at_birth),
  NULL,
  NULL,
  CASE WHEN b.is_active = 1 THEN 'active' ELSE 'inactive' END,
  COALESCE(pi.quality_status, pop.quality_status, 'qualified'),
  COALESCE(pi.release_status, 'released'),
  COALESCE(pi.trace_code, b.serial_number, b.batch_number, b.part_number),
  JSON_OBJECT(
    'mockDataBatch', 'EQUIPMENT_COMPONENT_PART_PROFILE_ENRICH_20260610',
    'sourceTable', 't1_aircraft_bom_node',
    'sourceNodeType', 'EQUIPMENT',
    'lifecycleStorage', 'event tables only; instance keeps profile and aggregate fields'
  )
FROM t1_aircraft_bom_node b
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = b.id
LEFT JOIN t1_part_instance pi ON pi.id = b.part_instance_id
LEFT JOIN t1_aircraft_object_profile aop ON aop.aircraft_id = b.aircraft_id
LEFT JOIN t1_subsystem_object_profile parent_subsystem ON parent_subsystem.bom_node_id = b.parent_id
LEFT JOIN t1_system_object_profile parent_system ON parent_system.bom_node_id = b.parent_id
LEFT JOIN t1_equipment_object_master em ON em.equipment_code = CASE
    WHEN b.part_number IS NOT NULL AND b.part_number <> '' AND CHAR_LENGTH(b.part_number) <= 100
      THEN b.part_number
    ELSE CONCAT('EQ-', SUBSTRING(MD5(COALESCE(NULLIF(b.part_number, ''), b.id)), 1, 32))
  END
WHERE UPPER(b.node_type) = 'EQUIPMENT'
ON DUPLICATE KEY UPDATE
  equipment_master_id = VALUES(equipment_master_id),
  object_profile_id = VALUES(object_profile_id),
  aircraft_id = VALUES(aircraft_id),
  system_id = VALUES(system_id),
  subsystem_id = VALUES(subsystem_id),
  part_instance_id = VALUES(part_instance_id),
  equipment_code = VALUES(equipment_code),
  equipment_name = VALUES(equipment_name),
  part_number = VALUES(part_number),
  serial_number = VALUES(serial_number),
  batch_number = VALUES(batch_number),
  software_version = VALUES(software_version),
  hardware_version = VALUES(hardware_version),
  manufacturer = VALUES(manufacturer),
  supplier_name = VALUES(supplier_name),
  supplier_code = VALUES(supplier_code),
  manufacture_date = VALUES(manufacture_date),
  delivery_batch = VALUES(delivery_batch),
  installation_position = VALUES(installation_position),
  position_code = VALUES(position_code),
  installation_date = VALUES(installation_date),
  installation_status = VALUES(installation_status),
  configuration_version = VALUES(configuration_version),
  modification_status = VALUES(modification_status),
  effectivity = VALUES(effectivity),
  tsn = VALUES(tsn),
  csn = VALUES(csn),
  operational_status = VALUES(operational_status),
  quality_status = VALUES(quality_status),
  airworthiness_release_status = VALUES(airworthiness_release_status),
  trace_code = VALUES(trace_code),
  attrs_json = JSON_MERGE_PATCH(COALESCE(t1_equipment_object_instance.attrs_json, JSON_OBJECT()), VALUES(attrs_json)),
  updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_component_object_master (
  component_master_id, component_code, component_name, component_type,
  assembly_part_number, drawing_no, drawing_revision, manufacturer, supplier_name,
  repairable_flag, replaceable_flag, life_limit, criticality_level,
  key_quality_characteristics, attrs_json
)
SELECT
  MIN(b.id),
  CASE
    WHEN MAX(NULLIF(b.part_number, '')) IS NOT NULL AND CHAR_LENGTH(MAX(NULLIF(b.part_number, ''))) <= 100
      THEN MAX(NULLIF(b.part_number, ''))
    ELSE CONCAT('CMP-', SUBSTRING(MD5(COALESCE(MAX(NULLIF(b.part_number, '')), MIN(b.id))), 1, 32))
  END,
  COALESCE(MAX(NULLIF(b.part_name, '')), MAX(NULLIF(b.part_number, '')), MIN(b.id)),
  CASE
    WHEN MAX(b.ata_chapter) LIKE '29%' THEN 'hydraulic_component'
    WHEN MAX(b.ata_chapter) LIKE '32%' THEN 'landing_gear_component'
    ELSE 'aircraft_component'
  END,
  MAX(NULLIF(b.part_number, '')),
  MAX(pm.drawing_no),
  MAX(pm.drawing_revision),
  COALESCE(MAX(NULLIF(b.manufacturer, '')), '制造/装配单位待确认'),
  COALESCE(MAX(NULLIF(b.manufacturer, '')), '供应商待确认'),
  1,
  1,
  CASE
    WHEN MAX(b.is_life_limited) = 1 THEN CONCAT('寿命限制：', COALESCE(MAX(pm.life_limit_fh), 0), ' FH / ', COALESCE(MAX(pm.life_limit_fc), 0), ' FC')
    ELSE '按组件维修和更换策略控制'
  END,
  COALESCE(MAX(pm.criticality_level), CASE WHEN MAX(b.ata_chapter) LIKE '29%' OR MAX(b.ata_chapter) LIKE '32%' THEN 'critical' ELSE 'standard' END),
  CONCAT(COALESCE(MAX(NULLIF(b.part_name, '')), MAX(NULLIF(b.part_number, '')), '组件'), ' 关键质量特性由装配、检验、材料追溯和放行记录共同证明。'),
  JSON_OBJECT(
    'mockDataBatch', 'EQUIPMENT_COMPONENT_PART_PROFILE_ENRICH_20260610',
    'sourceTable', 't1_aircraft_bom_node',
    'sourceNodeType', 'COMPONENT',
    'bomNodeCount', COUNT(*)
  )
FROM t1_aircraft_bom_node b
LEFT JOIN t1_part_master pm ON pm.part_number = b.part_number
WHERE UPPER(b.node_type) = 'COMPONENT'
GROUP BY CASE
  WHEN b.part_number IS NOT NULL AND b.part_number <> '' AND CHAR_LENGTH(b.part_number) <= 100
    THEN b.part_number
  ELSE CONCAT('CMP-', SUBSTRING(MD5(COALESCE(NULLIF(b.part_number, ''), b.id)), 1, 32))
END
ON DUPLICATE KEY UPDATE
  component_name = VALUES(component_name),
  component_type = VALUES(component_type),
  assembly_part_number = VALUES(assembly_part_number),
  drawing_no = VALUES(drawing_no),
  drawing_revision = VALUES(drawing_revision),
  manufacturer = VALUES(manufacturer),
  supplier_name = VALUES(supplier_name),
  repairable_flag = VALUES(repairable_flag),
  replaceable_flag = VALUES(replaceable_flag),
  life_limit = VALUES(life_limit),
  criticality_level = VALUES(criticality_level),
  key_quality_characteristics = VALUES(key_quality_characteristics),
  attrs_json = JSON_MERGE_PATCH(COALESCE(t1_component_object_master.attrs_json, JSON_OBJECT()), VALUES(attrs_json)),
  updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_component_object_instance (
  component_instance_id, component_master_id, object_profile_id, aircraft_id,
  parent_equipment_instance_id, parent_component_instance_id, bom_node_id,
  part_instance_id, component_code, component_name, assembly_part_number,
  assembly_serial_number, assembly_batch_number, assembly_work_order_no,
  assembly_date, assembly_version, part_count, key_part_count, installation_position,
  position_code, installation_date, assembly_status, quality_status,
  operational_status, tsn, csn, trace_code, material_trace_summary, attrs_json
)
SELECT
  b.id,
  cm.component_master_id,
  pop.id,
  b.aircraft_id,
  parent_equipment.equipment_instance_id,
  parent_component.component_instance_id,
  b.id,
  b.part_instance_id,
  cm.component_code,
  COALESCE(b.part_name, cm.component_name, b.part_number, b.id),
  b.part_number,
  COALESCE(b.serial_number, pi.serial_number),
  COALESCE(b.batch_number, pi.batch_number),
  so.order_code,
  COALESCE(pi.production_date, b.install_date),
  COALESCE(JSON_UNQUOTE(JSON_EXTRACT(pop.effectivity, '$.excelBomVersion')), aop.current_bom_version, 'as-maintained'),
  COALESCE(child.part_count, 0),
  COALESCE(child.key_part_count, 0),
  COALESCE(b.position_desc, pi.installation_position),
  COALESCE(b.position_code, pi.position_code),
  COALESCE(b.install_date, pi.installation_date),
  CASE WHEN b.is_active = 1 THEN 'assembled' ELSE 'removed' END,
  COALESCE(pi.quality_status, pop.quality_status, 'qualified'),
  CASE WHEN b.is_active = 1 THEN 'active' ELSE 'inactive' END,
  COALESCE(b.tsn_fh, pi.tsn_fh_at_birth),
  COALESCE(b.tsn_fc, pi.tsn_fc_at_birth),
  COALESCE(pi.trace_code, b.serial_number, b.batch_number, b.part_number),
  CONCAT('材料/批次追溯通过 t1_part_instance、t1_material_lot_trace、t1_inspection_record 和 t1_release_record 关联查询；BOM节点 ', b.id),
  JSON_OBJECT(
    'mockDataBatch', 'EQUIPMENT_COMPONENT_PART_PROFILE_ENRICH_20260610',
    'sourceTable', 't1_aircraft_bom_node',
    'sourceNodeType', 'COMPONENT',
    'lifecycleStorage', 'event tables only; instance keeps profile and aggregate fields'
  )
FROM t1_aircraft_bom_node b
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = b.id
LEFT JOIN t1_part_instance pi ON pi.id = b.part_instance_id
LEFT JOIN t1_aircraft_object_profile aop ON aop.aircraft_id = b.aircraft_id
LEFT JOIN t1_aircraft_bom_node parent_bom ON parent_bom.id = b.parent_id
LEFT JOIN t1_equipment_object_instance parent_equipment ON parent_equipment.bom_node_id = b.parent_id
LEFT JOIN t1_component_object_instance parent_component ON parent_component.bom_node_id = b.parent_id
LEFT JOIN t1_shop_order so ON so.id = pi.produced_by_shop_order_id
LEFT JOIN (
  SELECT
    parent_id,
    SUM(CASE WHEN UPPER(node_type) = 'PART' THEN 1 ELSE 0 END) AS part_count,
    SUM(CASE WHEN UPPER(node_type) = 'PART' AND is_life_limited = 1 THEN 1 ELSE 0 END) AS key_part_count
  FROM t1_aircraft_bom_node
  GROUP BY parent_id
) child ON child.parent_id = b.id
LEFT JOIN t1_component_object_master cm ON cm.component_code = CASE
    WHEN b.part_number IS NOT NULL AND b.part_number <> '' AND CHAR_LENGTH(b.part_number) <= 100
      THEN b.part_number
    ELSE CONCAT('CMP-', SUBSTRING(MD5(COALESCE(NULLIF(b.part_number, ''), b.id)), 1, 32))
  END
WHERE UPPER(b.node_type) = 'COMPONENT'
ON DUPLICATE KEY UPDATE
  component_master_id = VALUES(component_master_id),
  object_profile_id = VALUES(object_profile_id),
  aircraft_id = VALUES(aircraft_id),
  parent_equipment_instance_id = VALUES(parent_equipment_instance_id),
  parent_component_instance_id = VALUES(parent_component_instance_id),
  part_instance_id = VALUES(part_instance_id),
  component_code = VALUES(component_code),
  component_name = VALUES(component_name),
  assembly_part_number = VALUES(assembly_part_number),
  assembly_serial_number = VALUES(assembly_serial_number),
  assembly_batch_number = VALUES(assembly_batch_number),
  assembly_work_order_no = VALUES(assembly_work_order_no),
  assembly_date = VALUES(assembly_date),
  assembly_version = VALUES(assembly_version),
  part_count = VALUES(part_count),
  key_part_count = VALUES(key_part_count),
  installation_position = VALUES(installation_position),
  position_code = VALUES(position_code),
  installation_date = VALUES(installation_date),
  assembly_status = VALUES(assembly_status),
  quality_status = VALUES(quality_status),
  operational_status = VALUES(operational_status),
  tsn = VALUES(tsn),
  csn = VALUES(csn),
  trace_code = VALUES(trace_code),
  material_trace_summary = VALUES(material_trace_summary),
  attrs_json = JSON_MERGE_PATCH(COALESCE(t1_component_object_instance.attrs_json, JSON_OBJECT()), VALUES(attrs_json)),
  updated_at = CURRENT_TIMESTAMP(6);

CREATE OR REPLACE VIEW v_equipment_profile_detail AS
SELECT
  ei.equipment_instance_id,
  ei.equipment_master_id,
  ei.object_profile_id,
  ei.aircraft_id,
  pa.tail_number,
  ei.system_id,
  sop.system_code,
  sop.system_name,
  ei.subsystem_id,
  ssop.subsystem_code,
  ssop.subsystem_name,
  ei.bom_node_id,
  b.parent_id AS parent_bom_node_id,
  ei.equipment_code,
  ei.equipment_name,
  em.equipment_type,
  ei.part_number,
  ei.part_instance_id,
  ei.serial_number,
  ei.batch_number,
  ei.software_version,
  ei.hardware_version,
  ei.manufacturer,
  ei.supplier_name,
  ei.installation_position,
  ei.position_code,
  ei.installation_date,
  ei.installation_status,
  ei.configuration_version,
  ei.operational_status,
  ei.quality_status,
  ei.airworthiness_release_status,
  ei.tsn,
  ei.csn,
  ei.trace_code,
  pm.drawing_no,
  pm.drawing_revision,
  pm.design_revision,
  pm.specification,
  pm.material,
  pm.material_grade,
  pm.is_life_limited,
  pm.is_serialized,
  pm.is_rotable,
  pm.criticality_level,
  em.maintainability_type,
  em.life_limit,
  em.tbo,
  COALESCE(child.component_count, 0) AS component_count,
  COALESCE(child.part_count, 0) AS direct_part_count,
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
  COALESCE(pd.document_count, 0) AS part_document_count,
  COALESCE(dc.dossier_content_count, 0) AS dossier_content_count,
  COALESCE(dc.design_content_count, 0) AS design_content_count,
  COALESCE(dc.manufacturing_content_count, 0) AS manufacturing_content_count,
  COALESCE(dc.inspection_content_count, 0) AS inspection_content_count,
  COALESCE(dc.service_content_count, 0) AS service_content_count,
  COALESCE(dc.technical_status_content_count, 0) AS technical_status_content_count,
  ei.attrs_json,
  ei.updated_at
FROM t1_equipment_object_instance ei
JOIN t1_aircraft_bom_node b ON b.id = ei.bom_node_id
LEFT JOIN t1_equipment_object_master em ON em.equipment_master_id = ei.equipment_master_id
LEFT JOIN t1_product_object_profile pop ON pop.id = ei.object_profile_id
LEFT JOIN t1_physical_aircraft pa ON pa.id = ei.aircraft_id
LEFT JOIN t1_system_object_profile sop ON sop.system_id = ei.system_id
LEFT JOIN t1_subsystem_object_profile ssop ON ssop.subsystem_id = ei.subsystem_id
LEFT JOIN t1_part_master pm ON pm.part_number = ei.part_number
LEFT JOIN t1_part_instance pi ON pi.id = ei.part_instance_id
LEFT JOIN (
  SELECT
    parent_id AS bom_node_id,
    SUM(CASE WHEN UPPER(node_type) = 'COMPONENT' THEN 1 ELSE 0 END) AS component_count,
    SUM(CASE WHEN UPPER(node_type) = 'PART' THEN 1 ELSE 0 END) AS part_count
  FROM t1_aircraft_bom_node
  GROUP BY parent_id
) child ON child.bom_node_id = ei.bom_node_id
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
  WHERE object_level = 'equipment'
  GROUP BY bom_node_id
) lc ON lc.bom_node_id = ei.bom_node_id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS technical_status_record_count
  FROM t1_object_technical_status
  WHERE object_level = 'equipment'
  GROUP BY bom_node_id
) ts ON ts.bom_node_id = ei.bom_node_id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS status_history_count
  FROM t1_object_status_history
  WHERE object_level = 'equipment'
  GROUP BY bom_node_id
) sh ON sh.bom_node_id = ei.bom_node_id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS inspection_record_count
  FROM t1_inspection_record
  WHERE object_level = 'equipment'
  GROUP BY bom_node_id
) ir ON ir.bom_node_id = ei.bom_node_id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS work_order_count
  FROM t1_work_order
  WHERE object_level = 'equipment'
  GROUP BY bom_node_id
) wo ON wo.bom_node_id = ei.bom_node_id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS fault_event_count
  FROM t1_fault_event
  WHERE object_level = 'equipment'
  GROUP BY bom_node_id
) fe ON fe.bom_node_id = ei.bom_node_id
LEFT JOIN (
  SELECT part_number, COUNT(*) AS document_count
  FROM t1_file_relation
  WHERE is_current = 1
    AND included_flag = 1
    AND relation_status != 'deleted'
    AND relation_type IN ('PART_DOCUMENT','TECHNICAL_FILE','DRAWING','MODEL','ATTACHMENT','CERTIFICATE','SOURCE')
  GROUP BY part_number
) pd ON pd.part_number = ei.part_number
LEFT JOIN (
  SELECT
    bom_node_id,
    COUNT(*) AS dossier_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'design' THEN 1 ELSE 0 END) AS design_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'manufacturing' THEN 1 ELSE 0 END) AS manufacturing_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'inspection' THEN 1 ELSE 0 END) AS inspection_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'service' THEN 1 ELSE 0 END) AS service_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'technical_status' THEN 1 ELSE 0 END) AS technical_status_content_count
  FROM t1_dossier_content_item
  GROUP BY bom_node_id
) dc ON dc.bom_node_id = ei.bom_node_id;

CREATE OR REPLACE VIEW v_component_profile_detail AS
SELECT
  ci.component_instance_id,
  ci.component_master_id,
  ci.object_profile_id,
  ci.aircraft_id,
  pa.tail_number,
  ci.parent_equipment_instance_id,
  pei.equipment_code AS parent_equipment_code,
  pei.equipment_name AS parent_equipment_name,
  ci.parent_component_instance_id,
  pci.component_code AS parent_component_code,
  pci.component_name AS parent_component_name,
  ci.bom_node_id,
  b.parent_id AS parent_bom_node_id,
  ci.component_code,
  ci.component_name,
  cm.component_type,
  ci.assembly_part_number AS part_number,
  ci.part_instance_id,
  ci.assembly_serial_number AS serial_number,
  ci.assembly_batch_number AS batch_number,
  ci.assembly_work_order_no,
  ci.assembly_date,
  ci.assembly_version,
  ci.part_count,
  ci.key_part_count,
  ci.installation_position,
  ci.position_code,
  ci.installation_date,
  ci.assembly_status,
  ci.quality_status,
  ci.operational_status,
  ci.tsn,
  ci.csn,
  ci.trace_code,
  ci.material_trace_summary,
  pm.drawing_no,
  pm.drawing_revision,
  pm.design_revision,
  pm.specification,
  pm.material,
  pm.material_grade,
  pm.is_life_limited,
  pm.is_serialized,
  pm.criticality_level,
  cm.repairable_flag,
  cm.replaceable_flag,
  cm.life_limit,
  cm.key_quality_characteristics,
  COALESCE(lc.design_count, 0) AS design_event_count,
  COALESCE(lc.manufacturing_count, 0) AS manufacturing_event_count,
  COALESCE(lc.installation_count, 0) AS installation_event_count,
  COALESCE(lc.inspection_count, 0) AS inspection_event_count,
  COALESCE(lc.service_count, 0) AS service_event_count,
  COALESCE(lc.technical_status_count, 0) AS technical_status_event_count,
  COALESCE(ts.technical_status_record_count, 0) AS technical_status_record_count,
  COALESCE(sh.status_history_count, 0) AS status_history_count,
  COALESCE(fe.fault_event_count, 0) AS fault_event_count,
  COALESCE(pd.document_count, 0) AS part_document_count,
  COALESCE(dc.dossier_content_count, 0) AS dossier_content_count,
  COALESCE(dc.design_content_count, 0) AS design_content_count,
  COALESCE(dc.manufacturing_content_count, 0) AS manufacturing_content_count,
  COALESCE(dc.inspection_content_count, 0) AS inspection_content_count,
  COALESCE(dc.service_content_count, 0) AS service_content_count,
  COALESCE(dc.technical_status_content_count, 0) AS technical_status_content_count,
  ci.attrs_json,
  ci.updated_at
FROM t1_component_object_instance ci
JOIN t1_aircraft_bom_node b ON b.id = ci.bom_node_id
LEFT JOIN t1_component_object_master cm ON cm.component_master_id = ci.component_master_id
LEFT JOIN t1_physical_aircraft pa ON pa.id = ci.aircraft_id
LEFT JOIN t1_equipment_object_instance pei ON pei.equipment_instance_id = ci.parent_equipment_instance_id
LEFT JOIN t1_component_object_instance pci ON pci.component_instance_id = ci.parent_component_instance_id
LEFT JOIN t1_part_master pm ON pm.part_number = ci.assembly_part_number
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
  WHERE object_level = 'component'
  GROUP BY bom_node_id
) lc ON lc.bom_node_id = ci.bom_node_id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS technical_status_record_count
  FROM t1_object_technical_status
  WHERE object_level = 'component'
  GROUP BY bom_node_id
) ts ON ts.bom_node_id = ci.bom_node_id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS status_history_count
  FROM t1_object_status_history
  WHERE object_level = 'component'
  GROUP BY bom_node_id
) sh ON sh.bom_node_id = ci.bom_node_id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS fault_event_count
  FROM t1_fault_event
  WHERE object_level = 'component'
  GROUP BY bom_node_id
) fe ON fe.bom_node_id = ci.bom_node_id
LEFT JOIN (
  SELECT part_number, COUNT(*) AS document_count
  FROM t1_file_relation
  WHERE is_current = 1
    AND included_flag = 1
    AND relation_status != 'deleted'
    AND relation_type IN ('PART_DOCUMENT','TECHNICAL_FILE','DRAWING','MODEL','ATTACHMENT','CERTIFICATE','SOURCE')
  GROUP BY part_number
) pd ON pd.part_number = ci.assembly_part_number
LEFT JOIN (
  SELECT
    bom_node_id,
    COUNT(*) AS dossier_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'design' THEN 1 ELSE 0 END) AS design_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'manufacturing' THEN 1 ELSE 0 END) AS manufacturing_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'inspection' THEN 1 ELSE 0 END) AS inspection_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'service' THEN 1 ELSE 0 END) AS service_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'technical_status' THEN 1 ELSE 0 END) AS technical_status_content_count
  FROM t1_dossier_content_item
  GROUP BY bom_node_id
) dc ON dc.bom_node_id = ci.bom_node_id;

CREATE OR REPLACE VIEW v_part_profile_detail AS
SELECT
  b.id AS bom_node_id,
  pop.id AS object_profile_id,
  b.aircraft_id,
  pa.tail_number,
  b.parent_id AS parent_bom_node_id,
  parent_bom.node_type AS parent_node_type,
  parent_bom.part_number AS parent_code,
  parent_bom.part_name AS parent_name,
  b.part_number,
  COALESCE(pm.part_name, b.part_name) AS part_name,
  pm.part_name_en,
  b.part_instance_id,
  pi.serial_number,
  pi.batch_number,
  pi.lot_number,
  COALESCE(pi.manufacturer, b.manufacturer) AS manufacturer,
  pi.manufacture_date,
  pi.source_type,
  pi.airworthiness_tag_type,
  pi.airworthiness_tag_number,
  pi.instance_status,
  pi.provenance_kind,
  COALESCE(pi.current_aircraft_id, b.aircraft_id) AS current_aircraft_id,
  COALESCE(pi.current_node_id, b.id) AS current_node_id,
  COALESCE(pi.installation_position, b.position_desc) AS installation_position,
  COALESCE(pi.position_code, b.position_code) AS position_code,
  COALESCE(pi.installation_date, b.install_date) AS installation_date,
  pi.installation_status,
  pi.life_limit_value,
  pi.life_limit_unit,
  pi.remaining_life_value,
  pi.remaining_life_unit,
  pi.inspection_status,
  pi.release_status,
  pi.quality_status,
  COALESCE(pi.trace_code, b.serial_number, b.batch_number, b.part_number) AS trace_code,
  pm.drawing_no,
  pm.drawing_revision,
  pm.design_revision,
  pm.specification,
  pm.ata_chapter,
  pm.part_category,
  pm.material,
  pm.material_grade,
  pm.standard_no,
  pm.surface_treatment,
  pm.is_life_limited,
  pm.is_serialized,
  pm.is_rotable,
  pm.is_expendable,
  pm.is_critical,
  pm.criticality_level,
  pm.key_quality_characteristics,
  pm.life_limit_fh,
  pm.life_limit_fc,
  pm.life_limit_year,
  pm.default_supply_mode,
  COALESCE(param.parameter_count, 0) AS parameter_count,
  COALESCE(pd.document_count, 0) AS part_document_count,
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
  COALESCE(dc.inspection_content_count, 0) AS inspection_content_count,
  COALESCE(dc.service_content_count, 0) AS service_content_count,
  COALESCE(dc.technical_status_content_count, 0) AS technical_status_content_count,
  pop.attrs_json AS object_attrs_json,
  b.updated_at AS bom_updated_at
FROM t1_aircraft_bom_node b
LEFT JOIN t1_aircraft_bom_node parent_bom ON parent_bom.id = b.parent_id
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = b.id
LEFT JOIN t1_physical_aircraft pa ON pa.id = b.aircraft_id
LEFT JOIN t1_part_master pm ON pm.part_number = b.part_number
LEFT JOIN t1_part_instance pi ON pi.id = b.part_instance_id
LEFT JOIN (
  SELECT part_number, COUNT(*) AS parameter_count
  FROM t1_part_parameter_value
  WHERE effective_to IS NULL
  GROUP BY part_number
) param ON param.part_number = b.part_number
LEFT JOIN (
  SELECT part_number, COUNT(*) AS document_count
  FROM t1_file_relation
  WHERE is_current = 1
    AND included_flag = 1
    AND relation_status != 'deleted'
    AND relation_type IN ('PART_DOCUMENT','TECHNICAL_FILE','DRAWING','MODEL','ATTACHMENT','CERTIFICATE','SOURCE')
  GROUP BY part_number
) pd ON pd.part_number = b.part_number
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
  WHERE object_level = 'part'
  GROUP BY bom_node_id
) lc ON lc.bom_node_id = b.id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS technical_status_record_count
  FROM t1_object_technical_status
  WHERE object_level = 'part'
  GROUP BY bom_node_id
) ts ON ts.bom_node_id = b.id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS status_history_count
  FROM t1_object_status_history
  WHERE object_level = 'part'
  GROUP BY bom_node_id
) sh ON sh.bom_node_id = b.id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS inspection_record_count
  FROM t1_inspection_record
  WHERE object_level = 'part'
  GROUP BY bom_node_id
) ir ON ir.bom_node_id = b.id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS work_order_count
  FROM t1_work_order
  WHERE object_level = 'part'
  GROUP BY bom_node_id
) wo ON wo.bom_node_id = b.id
LEFT JOIN (
  SELECT bom_node_id, COUNT(*) AS fault_event_count
  FROM t1_fault_event
  WHERE object_level = 'part'
  GROUP BY bom_node_id
) fe ON fe.bom_node_id = b.id
LEFT JOIN (
  SELECT
    bom_node_id,
    COUNT(*) AS dossier_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'design' THEN 1 ELSE 0 END) AS design_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'manufacturing' THEN 1 ELSE 0 END) AS manufacturing_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'inspection' THEN 1 ELSE 0 END) AS inspection_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'service' THEN 1 ELSE 0 END) AS service_content_count,
    SUM(CASE WHEN LOWER(lifecycle_stage) = 'technical_status' THEN 1 ELSE 0 END) AS technical_status_content_count
  FROM t1_dossier_content_item
  GROUP BY bom_node_id
) dc ON dc.bom_node_id = b.id
WHERE UPPER(b.node_type) = 'PART';

CREATE OR REPLACE VIEW v_equipment_profile_coverage_check AS
SELECT
  b.id AS bom_node_id,
  b.aircraft_id,
  b.part_number,
  b.part_name,
  CASE
    WHEN pop.id IS NULL THEN 'missing_product_object_profile'
    WHEN pm.part_number IS NULL THEN 'missing_part_master'
    WHEN ei.equipment_instance_id IS NULL THEN 'missing_equipment_object_instance'
    WHEN ei.equipment_master_id IS NULL THEN 'missing_equipment_object_master'
    WHEN NOT (ei.object_profile_id <=> pop.id) THEN 'object_profile_id_mismatch'
    WHEN NOT (ei.part_number <=> b.part_number) THEN 'part_number_mismatch'
    ELSE 'ok'
  END AS issue_type
FROM t1_aircraft_bom_node b
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = b.id
LEFT JOIN t1_part_master pm ON pm.part_number = b.part_number
LEFT JOIN t1_equipment_object_instance ei ON ei.bom_node_id = b.id
WHERE UPPER(b.node_type) = 'EQUIPMENT'
  AND (
    pop.id IS NULL
    OR pm.part_number IS NULL
    OR ei.equipment_instance_id IS NULL
    OR ei.equipment_master_id IS NULL
    OR NOT (ei.object_profile_id <=> pop.id)
    OR NOT (ei.part_number <=> b.part_number)
  );

CREATE OR REPLACE VIEW v_component_profile_coverage_check AS
SELECT
  b.id AS bom_node_id,
  b.aircraft_id,
  b.part_number,
  b.part_name,
  CASE
    WHEN pop.id IS NULL THEN 'missing_product_object_profile'
    WHEN pm.part_number IS NULL THEN 'missing_part_master'
    WHEN ci.component_instance_id IS NULL THEN 'missing_component_object_instance'
    WHEN ci.component_master_id IS NULL THEN 'missing_component_object_master'
    WHEN NOT (ci.object_profile_id <=> pop.id) THEN 'object_profile_id_mismatch'
    WHEN NOT (ci.assembly_part_number <=> b.part_number) THEN 'part_number_mismatch'
    ELSE 'ok'
  END AS issue_type
FROM t1_aircraft_bom_node b
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = b.id
LEFT JOIN t1_part_master pm ON pm.part_number = b.part_number
LEFT JOIN t1_component_object_instance ci ON ci.bom_node_id = b.id
WHERE UPPER(b.node_type) = 'COMPONENT'
  AND (
    pop.id IS NULL
    OR pm.part_number IS NULL
    OR ci.component_instance_id IS NULL
    OR ci.component_master_id IS NULL
    OR NOT (ci.object_profile_id <=> pop.id)
    OR NOT (ci.assembly_part_number <=> b.part_number)
  );

CREATE OR REPLACE VIEW v_part_profile_coverage_check AS
SELECT
  b.id AS bom_node_id,
  b.aircraft_id,
  b.part_number,
  b.part_name,
  CASE
    WHEN pop.id IS NULL THEN 'missing_product_object_profile'
    WHEN pm.part_number IS NULL THEN 'missing_part_master'
    WHEN b.part_instance_id IS NOT NULL AND pi.id IS NULL THEN 'part_instance_id_not_found'
    ELSE 'ok'
  END AS issue_type
FROM t1_aircraft_bom_node b
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = b.id
LEFT JOIN t1_part_master pm ON pm.part_number = b.part_number
LEFT JOIN t1_part_instance pi ON pi.id = b.part_instance_id
WHERE UPPER(b.node_type) = 'PART'
  AND (
    pop.id IS NULL
    OR pm.part_number IS NULL
    OR (b.part_instance_id IS NOT NULL AND pi.id IS NULL)
  );
