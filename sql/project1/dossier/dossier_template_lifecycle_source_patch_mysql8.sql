-- =============================================================================
-- Digital dossier lifecycle source and hydraulic tube demo data patch - MySQL 8.0
-- Purpose:
--   1. Backfill enabled template chapters that do not yet have a data source.
--   2. Add parameter-level source anchors for the landing-gear hydraulic tube.
--   3. Complete realistic demo data for hydraulic impact, manufacturing,
--      inspection, installation/service and component assembly relation.
-- =============================================================================

SET NAMES utf8mb4;
USE `ry-cloud`;

SET @tpl_v11 = 't1000001-0001-4001-8001-000000000111';
SET @aircraft_b1234 = 'b0000001-0001-4001-8001-000000000001';
SET @hyd_tube_node = 'f1000006-0006-4006-8006-000000000006';
SET @hyd_tube_instance = 'c7000001-0001-4001-8001-000000000001';
SET @hyd_pkg_node = 'f1000010-0010-4010-8010-000000000010';
SET @hyd_pkg_instance = 'c7100001-0001-4001-8001-000000000001';
SET @hyd_shop_order = 'c6000001-0001-4001-8001-000000000001';
SET @hyd_op10_task = 'c6100001-0001-4001-8001-000000000001';
SET @hyd_op20_task = 'c6100002-0002-4002-8002-000000000002';
SET @hyd_op40_task = 'c6100004-0004-4004-8004-000000000004';
SET @hyd_op50_task = 'c6100003-0003-4003-8003-000000000003';
SET @hyd_final_inspection = 'c6400001-0001-4001-8001-000000000001';
SET @hyd_work_order = 'c9200001-0001-4001-8001-000000000001';
SET @hyd_fault_event = 'c9000001-0001-4001-8001-000000000001';

START TRANSACTION;

-- -----------------------------------------------------------------------------
-- 1. Backfill data sources for enabled chapters that still have no source.
-- -----------------------------------------------------------------------------
INSERT INTO t1_dossier_template_data_source (
  id, template_id, chapter_id, source_code, source_system, source_table,
  source_name, source_desc, lifecycle_stage, source_record_type,
  join_condition_json, filter_condition_json, apply_object_type,
  supply_mode_scope, key_part_scope, required_flag, enabled_flag,
  sort_order, attrs_json, created_by, updated_by
)
SELECT
  UUID(),
  @tpl_v11,
  c.id,
  CONCAT('SRC-', c.chapter_code, '-LIFECYCLE'),
  CASE
    WHEN c.chapter_code LIKE '%INTERFACE%' THEN 'CONFIG'
    WHEN c.chapter_code LIKE '%TECH_STATUS%' OR c.chapter_code LIKE '%CHANGE%' THEN 'CONFIG'
    WHEN c.chapter_code LIKE '%MAINTENANCE%' OR c.chapter_code LIKE '%SERVICE%' OR c.chapter_code LIKE '%FAULT%' THEN 'MRO'
    WHEN c.chapter_code LIKE '%ATTACHMENTS%' OR c.chapter_code LIKE '%CERT%' THEN 'DOSSIER'
    WHEN c.chapter_code LIKE '%ASSEMBLY_REL%' THEN 'MES'
    ELSE 'DOSSIER'
  END,
  CASE
    WHEN c.chapter_code LIKE '%INTERFACE%' THEN 't1_object_interface'
    WHEN c.chapter_code LIKE '%TECH_STATUS%' THEN 't1_object_technical_status'
    WHEN c.chapter_code LIKE '%CHANGE%' THEN 't1_engineering_change_execution'
    WHEN c.chapter_code LIKE '%MAINTENANCE%' THEN 't1_work_order'
    WHEN c.chapter_code LIKE '%SERVICE%' THEN 't1_life_usage_record'
    WHEN c.chapter_code LIKE '%FAULT%' THEN 't1_fault_event'
    WHEN c.chapter_code LIKE '%ATTACHMENTS%' OR c.chapter_code LIKE '%CERT%' THEN 't1_document_entry'
    WHEN c.chapter_code LIKE '%ASSEMBLY_REL%' THEN 't1_part_instance_assembly'
    ELSE 't1_dossier_content_item'
  END,
  CONCAT(c.chapter_name, '生命周期数据'),
  CONCAT('补齐 ', c.chapter_name, ' 的生命周期数据来源；生成时按当前 BOM 节点、实物实例和飞机过滤。'),
  CASE
    WHEN c.chapter_code LIKE '%MAINTENANCE%' OR c.chapter_code LIKE '%SERVICE%' OR c.chapter_code LIKE '%FAULT%' THEN 'SERVICE'
    WHEN c.chapter_code LIKE '%ASSEMBLY_REL%' THEN 'MANUFACTURING'
    ELSE 'DOSSIER'
  END,
  'business',
  JSON_OBJECT(
    'aircraft_id', '${aircraftId}',
    'bom_node_id', '${bomNodeId}',
    'part_instance_id', '${partInstanceId}',
    'object_level', JSON_UNQUOTE(JSON_EXTRACT(c.attrs_json, '$.objectLevel'))
  ),
  JSON_OBJECT('enabled', TRUE, 'activeOnly', TRUE),
  COALESCE(JSON_UNQUOTE(JSON_EXTRACT(c.attrs_json, '$.objectLevel')), 'aircraft'),
  'all',
  'all',
  0,
  1,
  c.sort_order + 700,
  JSON_OBJECT('autoBackfill', TRUE, 'patch', 'dossier_template_lifecycle_source_patch_mysql8.sql'),
  'system',
  'system'
FROM t1_dossier_template_chapter c
WHERE c.template_id = @tpl_v11
  AND c.node_kind = 'chapter'
  AND c.enabled_flag = 1
  AND NOT EXISTS (
    SELECT 1
    FROM t1_dossier_template_data_source ds
    WHERE ds.chapter_id = c.id
      AND ds.enabled_flag = 1
  )
ON DUPLICATE KEY UPDATE
  source_system = VALUES(source_system),
  source_table = VALUES(source_table),
  source_name = VALUES(source_name),
  source_desc = VALUES(source_desc),
  lifecycle_stage = VALUES(lifecycle_stage),
  join_condition_json = VALUES(join_condition_json),
  filter_condition_json = VALUES(filter_condition_json),
  apply_object_type = VALUES(apply_object_type),
  attrs_json = VALUES(attrs_json),
  updated_by = 'system',
  updated_at = CURRENT_TIMESTAMP(6);

-- -----------------------------------------------------------------------------
-- 2. Add extra parameter-level source anchors for the hydraulic tube demo.
-- -----------------------------------------------------------------------------
INSERT INTO t1_dossier_template_data_source (
  id, template_id, chapter_id, source_code, source_system, source_table,
  source_name, source_desc, lifecycle_stage, source_record_type,
  join_condition_json, filter_condition_json, apply_object_type,
  supply_mode_scope, key_part_scope, required_flag, enabled_flag,
  sort_order, attrs_json, created_by, updated_by
)
SELECT UUID(), @tpl_v11, c.id, 'SRC-PART-IMPACT-FLUID-HYD', 'DESIGN', 't1_impact_tube_fluid',
       '液压弯管冲击流体参数',
       '读取 Skydrol LD-4 等液压油密度、体积弹性模量、运动黏度和蒸汽压，用于水锤冲击仿真。',
       'DESIGN', 'impact_fluid',
       JSON_OBJECT('model_part_number', '${partNumber}'),
       JSON_OBJECT('model_part_number', 'HYD-TUBE-MLG-32A'),
       'part', 'self_made', 'key_only', 1, 1, 62,
       JSON_OBJECT('demoCritical', TRUE, 'block', 'params'), 'system', 'system'
FROM t1_dossier_template_chapter c
WHERE c.template_id = @tpl_v11 AND c.chapter_code = 'PART_DESIGN'
ON DUPLICATE KEY UPDATE source_desc = VALUES(source_desc), updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_dossier_template_data_source (
  id, template_id, chapter_id, source_code, source_system, source_table,
  source_name, source_desc, lifecycle_stage, source_record_type,
  join_condition_json, filter_condition_json, apply_object_type,
  supply_mode_scope, key_part_scope, required_flag, enabled_flag,
  sort_order, attrs_json, created_by, updated_by
)
SELECT UUID(), @tpl_v11, c.id, 'SRC-PART-IMPACT-BOUNDARY-HYD', 'DESIGN', 't1_impact_tube_boundary_condition',
       '液压弯管冲击边界条件',
       '读取振动谱、入口速度、入口压力、阀门关闭时间和作动筒堵转时间，用于演示液压冲击仿真边界。',
       'DESIGN', 'impact_boundary',
       JSON_OBJECT('model_part_number', '${partNumber}'),
       JSON_OBJECT('model_part_number', 'HYD-TUBE-MLG-32A'),
       'part', 'self_made', 'key_only', 1, 1, 63,
       JSON_OBJECT('demoCritical', TRUE, 'block', 'params'), 'system', 'system'
FROM t1_dossier_template_chapter c
WHERE c.template_id = @tpl_v11 AND c.chapter_code = 'PART_DESIGN'
ON DUPLICATE KEY UPDATE source_desc = VALUES(source_desc), updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_dossier_template_data_source (
  id, template_id, chapter_id, source_code, source_system, source_table,
  source_name, source_desc, lifecycle_stage, source_record_type,
  join_condition_json, filter_condition_json, apply_object_type,
  supply_mode_scope, key_part_scope, required_flag, enabled_flag,
  sort_order, attrs_json, created_by, updated_by
)
SELECT UUID(), @tpl_v11, c.id, 'SRC-PART-INSPECTION-MEASURE-HYD', 'MES', 't1_inspection_measurement',
       '液压弯管检验测量明细',
       '读取外径、壁厚、弯曲半径、保压压力和泄漏率等检验测量值，支撑参数级完整性展示。',
       'MANUFACTURING', 't1_inspection_measurement',
       JSON_OBJECT('inspection_record_id', '${inspectionRecordId}'),
       JSON_OBJECT('part_number', 'HYD-TUBE-MLG-32A'),
       'part', 'self_made', 'key_only', 1, 1, 42,
       JSON_OBJECT('demoCritical', TRUE, 'block', 'details'), 'system', 'system'
FROM t1_dossier_template_chapter c
WHERE c.template_id = @tpl_v11 AND c.chapter_code = 'PART_INSPECTION'
ON DUPLICATE KEY UPDATE source_desc = VALUES(source_desc), updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_dossier_template_data_source (
  id, template_id, chapter_id, source_code, source_system, source_table,
  source_name, source_desc, lifecycle_stage, source_record_type,
  join_condition_json, filter_condition_json, apply_object_type,
  supply_mode_scope, key_part_scope, required_flag, enabled_flag,
  sort_order, attrs_json, created_by, updated_by
)
SELECT UUID(), @tpl_v11, c.id, 'SRC-PART-LIFE-USAGE-HYD', 'MRO', 't1_life_usage_record',
       '液压弯管寿命使用记录',
       '读取装机后的飞行小时、循环、起落和剩余寿命，用于服役维修目录的寿命展示。',
       'SERVICE', 'life_usage',
       JSON_OBJECT('part_instance_id', '${partInstanceId}', 'bom_node_id', '${bomNodeId}'),
       JSON_OBJECT('part_instance_id', @hyd_tube_instance),
       'part', 'self_made', 'key_only', 0, 1, 56,
       JSON_OBJECT('demoCritical', TRUE, 'block', 'timeline'), 'system', 'system'
FROM t1_dossier_template_chapter c
WHERE c.template_id = @tpl_v11 AND c.chapter_code = 'PART_SERVICE'
ON DUPLICATE KEY UPDATE source_desc = VALUES(source_desc), updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_dossier_template_data_source (
  id, template_id, chapter_id, source_code, source_system, source_table,
  source_name, source_desc, lifecycle_stage, source_record_type,
  join_condition_json, filter_condition_json, apply_object_type,
  supply_mode_scope, key_part_scope, required_flag, enabled_flag,
  sort_order, attrs_json, created_by, updated_by
)
SELECT UUID(), @tpl_v11, c.id, 'SRC-PART-SERVICE-TEXT-HYD', 'MRO', 't1_quality_text_record',
       '液压弯管服役巡检记录',
       '读取最近一次目视检查、渗漏检查和压力脉动复核文本记录，作为服役维修目录证据。',
       'SERVICE', 'service_inspection',
       JSON_OBJECT('part_instance_id', '${partInstanceId}', 'record_type', 'INSPECTION_REPORT'),
       JSON_OBJECT('part_instance_id', @hyd_tube_instance, 'record_type', 'INSPECTION_REPORT'),
       'part', 'self_made', 'key_only', 0, 1, 57,
       JSON_OBJECT('demoCritical', TRUE, 'block', 'documents'), 'system', 'system'
FROM t1_dossier_template_chapter c
WHERE c.template_id = @tpl_v11 AND c.chapter_code = 'PART_SERVICE'
ON DUPLICATE KEY UPDATE source_desc = VALUES(source_desc), updated_at = CURRENT_TIMESTAMP(6);

-- -----------------------------------------------------------------------------
-- 3. Complete hydraulic tube engineering data.
-- -----------------------------------------------------------------------------
INSERT INTO t1_impact_tube_fluid (
  model_part_number, fluid_name, density_kgm3, bulk_modulus_mpa,
  kinematic_viscosity_cst, reference_temperature_c, vapor_pressure_pa
) VALUES (
  'HYD-TUBE-MLG-32A', 'Skydrol LD-4 phosphate ester hydraulic fluid',
  1005.00, 1680.00, 14.2000, 40.00, 18.00
)
ON DUPLICATE KEY UPDATE
  fluid_name = VALUES(fluid_name),
  density_kgm3 = VALUES(density_kgm3),
  bulk_modulus_mpa = VALUES(bulk_modulus_mpa),
  kinematic_viscosity_cst = VALUES(kinematic_viscosity_cst),
  reference_temperature_c = VALUES(reference_temperature_c),
  vapor_pressure_pa = VALUES(vapor_pressure_pa);

INSERT INTO t1_impact_tube_boundary_condition (
  model_part_number, vibration_profile_json, inlet_velocity_json,
  inlet_pressure_json, valve_close_time_s, actuator_stall_time_s
) VALUES (
  'HYD-TUBE-MLG-32A',
  JSON_ARRAY(
    JSON_OBJECT('freq_hz', 20, 'accel_g', 0.35, 'axis', 'X'),
    JSON_OBJECT('freq_hz', 80, 'accel_g', 0.72, 'axis', 'Y'),
    JSON_OBJECT('freq_hz', 160, 'accel_g', 1.05, 'axis', 'Z'),
    JSON_OBJECT('freq_hz', 320, 'accel_g', 0.58, 'axis', 'combined')
  ),
  JSON_ARRAY(
    JSON_OBJECT('time_s', 0.000, 'velocity_m_s', 4.20),
    JSON_OBJECT('time_s', 0.015, 'velocity_m_s', 4.55),
    JSON_OBJECT('time_s', 0.035, 'velocity_m_s', 0.40),
    JSON_OBJECT('time_s', 0.050, 'velocity_m_s', 0.05)
  ),
  JSON_ARRAY(
    JSON_OBJECT('time_s', 0.000, 'pressure_mpa', 15.50),
    JSON_OBJECT('time_s', 0.020, 'pressure_mpa', 18.20),
    JSON_OBJECT('time_s', 0.035, 'pressure_mpa', 21.30),
    JSON_OBJECT('time_s', 0.060, 'pressure_mpa', 16.10)
  ),
  0.0350,
  0.1800
)
ON DUPLICATE KEY UPDATE
  vibration_profile_json = VALUES(vibration_profile_json),
  inlet_velocity_json = VALUES(inlet_velocity_json),
  inlet_pressure_json = VALUES(inlet_pressure_json),
  valve_close_time_s = VALUES(valve_close_time_s),
  actuator_stall_time_s = VALUES(actuator_stall_time_s),
  updated_at = CURRENT_TIMESTAMP(6);

-- -----------------------------------------------------------------------------
-- 4. Complete manufacturing trace, including the OP40 task required by rules.
-- -----------------------------------------------------------------------------
INSERT INTO t1_shop_order_task (
  id, task_code, shop_order_id, step_code, task_seq, parent_task_id,
  quantity_to_produce, quantity_completed, priority, urgent_flag, status,
  planned_start, planned_finish, actual_start, actual_finish,
  assigned_workstation_code, assigned_equipment_id, assigned_personnel_id,
  result_notes, task_attrs_json
) VALUES (
  @hyd_op40_task, 'TASK-HYD-42-OP40', @hyd_shop_order, 'HYD-TUBE-32A-OP40',
  40, NULL, 1, 1, 5, 0, 'COMPLETED',
  '2026-01-19 08:00:00', '2026-01-19 12:00:00',
  '2026-01-19 08:15:00', '2026-01-19 11:30:00',
  'NDT-01', 'UT-HYD-02', 'SP-HYD-02',
  '荧光渗透与内窥检查完成，未见裂纹、夹伤和内壁毛刺。',
  JSON_OBJECT('ndt', 'FPI + borescope', 'acceptance', 'no crack, no burr, no dent')
)
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  quantity_completed = VALUES(quantity_completed),
  actual_start = VALUES(actual_start),
  actual_finish = VALUES(actual_finish),
  result_notes = VALUES(result_notes),
  task_attrs_json = VALUES(task_attrs_json),
  updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_material_lot_trace (
  id, instance_id, shop_order_task_id, material_pn, material_spec,
  lot_number, supplier, mill_cert_number, quantity_used, unit
) VALUES (
  'c6200001-0001-4001-8001-000000000001',
  @hyd_tube_instance, @hyd_op10_task, 'TB3-TUBE-12.7x1.02',
  'AMS 4944 Grade 3 seamless titanium tube',
  'HEAT-25Q4-8841', 'Baoji Titanium Tube Co., Ltd.',
  'MTC-HEAT-25Q4-8841-REV-A', 0.7800, 'm'
)
ON DUPLICATE KEY UPDATE
  shop_order_task_id = VALUES(shop_order_task_id),
  material_pn = VALUES(material_pn),
  material_spec = VALUES(material_spec),
  lot_number = VALUES(lot_number),
  supplier = VALUES(supplier),
  mill_cert_number = VALUES(mill_cert_number),
  quantity_used = VALUES(quantity_used),
  unit = VALUES(unit);

INSERT INTO t1_production_operation_record (
  id, shop_order_task_id, work_step_detail_id, detail_seq, actual_params,
  equipment_id, operator_id, start_time, end_time, data_source, remarks
) VALUES
  ('c6300001-0001-4001-8001-000000000001', @hyd_op10_task, NULL, 10,
   JSON_OBJECT('incoming_od_mm', 12.70, 'wall_thickness_mm', 1.02, 'surface', 'no scratch', 'lot', 'HEAT-25Q4-8841'),
   'IQC-HYD-01', 'OP-HYD-11', '2026-01-10 08:30:00', '2026-01-10 10:20:00', 'MANUAL', '来料复验合格，炉批与材质证明一致。'),
  ('c6300002-0002-4002-8002-000000000002', @hyd_op20_task, NULL, 20,
   JSON_OBJECT('bend_radius_mm', 38.0, 'bend_angle_deg', 72.5, 'springback_comp_deg', 1.8, 'mandrel_lube', 'approved'),
   'CNC-BEND-07', 'OP-HYD-21', '2026-01-14 09:00:00', '2026-01-14 14:10:00', 'MANUAL', '数控弯管完成，回弹补偿后角度满足工艺卡。'),
  ('c6300003-0003-4003-8003-000000000003', @hyd_op40_task, NULL, 40,
   JSON_OBJECT('fpi_result', 'PASS', 'borescope_result', 'PASS', 'dent_depth_mm', 0, 'burr', 'none'),
   'NDT-01', 'SP-HYD-02', '2026-01-19 08:15:00', '2026-01-19 11:30:00', 'MANUAL', '无损检测和内壁检查合格。'),
  ('c6300004-0004-4004-8004-000000000004', @hyd_op50_task, NULL, 50,
   JSON_OBJECT('hold_pressure_mpa', 21, 'hold_min', 5, 'leak_rate_ml_min', 0, 'marking', 'HT-MLG-32A-2026-0042'),
   'LEAK-HYD-03', 'SP-HYD-02', '2026-01-22 13:30:00', '2026-01-22 16:00:00', 'MANUAL', '气密保压合格，完成标识和放行。')
ON DUPLICATE KEY UPDATE
  actual_params = VALUES(actual_params),
  equipment_id = VALUES(equipment_id),
  operator_id = VALUES(operator_id),
  start_time = VALUES(start_time),
  end_time = VALUES(end_time),
  data_source = VALUES(data_source),
  remarks = VALUES(remarks);

-- -----------------------------------------------------------------------------
-- 5. Complete inspection measurement details.
-- Values below are stored as integer engineering units where the column scale is 0.
-- -----------------------------------------------------------------------------
INSERT INTO t1_inspection_measurement (
  id, inspection_record_id, indicator_code, indicator_name, nominal_value,
  upper_limit, lower_limit, measured_value, unit, result_flag, defect_code,
  defect_level, remark
) VALUES
  ('c6410001-0001-4001-8001-000000000001', @hyd_final_inspection, 'OD_UM', '外径', 12700, 12730, 12670, 12702, 'um', 'PASS', NULL, NULL, '12.702 mm，满足图纸 12.70 +/-0.03 mm。'),
  ('c6410002-0002-4002-8002-000000000002', @hyd_final_inspection, 'WALL_UM', '壁厚', 1020, 1050, 990, 1018, 'um', 'PASS', NULL, NULL, '1.018 mm，满足图纸 1.02 +/-0.03 mm。'),
  ('c6410003-0003-4003-8003-000000000003', @hyd_final_inspection, 'BEND_RADIUS_UM', '主弯半径', 38000, 38500, 37500, 38080, 'um', 'PASS', NULL, NULL, '38.080 mm，满足工艺控制范围。'),
  ('c6410004-0004-4004-8004-000000000004', @hyd_final_inspection, 'HOLD_PRESSURE_MPA', '保压压力', 21, 22, 21, 21, 'MPa', 'PASS', NULL, NULL, '21 MPa 保压 5 min。'),
  ('c6410005-0005-4005-8005-000000000005', @hyd_final_inspection, 'LEAK_RATE_ML_MIN', '泄漏率', 0, 0, 0, 0, 'ml/min', 'PASS', NULL, NULL, '保压阶段未见泄漏。')
ON DUPLICATE KEY UPDATE
  measured_value = VALUES(measured_value),
  result_flag = VALUES(result_flag),
  remark = VALUES(remark);

-- -----------------------------------------------------------------------------
-- 6. Complete component instance and assembly relation.
-- -----------------------------------------------------------------------------
INSERT INTO t1_part_instance (
  id, part_number, serial_number, batch_number, lot_number, quantity, unit,
  manufacturer, manufacture_date, source_type, instance_status,
  provenance_kind, current_node_id, current_aircraft_id, installation_position,
  position_code, installation_date, installation_status, quality_status,
  release_status, trace_code, remark, produced_by_shop_order_id,
  produced_by_shop_task_id, production_date
) VALUES (
  @hyd_pkg_instance, 'HYD-MLG-PKG-01', 'HYD-PKG-01-2026-0042',
  'BATCH-HYD-PKG-2026-01', 'LOT-HYD-PKG-2026-01', 1.0000, 'EA',
  'Xi''an Aircraft Hydraulic Assembly Shop', '2026-01-24',
  'NEW', 'INSTALLED', 'CONSOLIDATED_BY_ASSEMBLY', @hyd_pkg_node, @aircraft_b1234,
  '主起舱左侧液压供压管路包', 'MLG-HYD-SUPPLY-PKG', '2026-02-02',
  'installed', 'PASS', 'RELEASED', 'HYD-PKG-01-2026-0042',
  '用于展示组件到零件的装配关系，子件包含 HYD-TUBE-MLG-32A。',
  @hyd_shop_order, @hyd_op50_task, '2026-01-24 16:00:00'
)
ON DUPLICATE KEY UPDATE
  current_node_id = VALUES(current_node_id),
  current_aircraft_id = VALUES(current_aircraft_id),
  installation_status = VALUES(installation_status),
  quality_status = VALUES(quality_status),
  release_status = VALUES(release_status),
  remark = VALUES(remark),
  updated_at = CURRENT_TIMESTAMP(6);

UPDATE t1_aircraft_bom_node
SET part_instance_id = @hyd_pkg_instance,
    serial_number = COALESCE(serial_number, 'HYD-PKG-01-2026-0042'),
    is_serialized = 1,
    updated_at = CURRENT_TIMESTAMP(6)
WHERE id = @hyd_pkg_node
  AND (part_instance_id IS NULL OR part_instance_id = @hyd_pkg_instance);

UPDATE t1_part_instance
SET parent_component_instance_id = @hyd_pkg_instance,
    current_node_id = @hyd_tube_node,
    current_aircraft_id = @aircraft_b1234,
    installation_status = 'installed',
    updated_at = CURRENT_TIMESTAMP(6)
WHERE id = @hyd_tube_instance;

INSERT INTO t1_part_instance_assembly (
  id, parent_instance_id, child_instance_id, bom_node_id,
  shop_order_task_id, quantity, assembled_at, remark
) VALUES (
  'c7110001-0001-4001-8001-000000000001',
  @hyd_pkg_instance, @hyd_tube_instance, @hyd_tube_node,
  @hyd_op50_task, 1.000, '2026-01-24 15:20:00',
  '液压供压管路组件装配关系：主起液压供压弯管装入 MLG-HYD-STA 位。'
)
ON DUPLICATE KEY UPDATE
  parent_instance_id = VALUES(parent_instance_id),
  child_instance_id = VALUES(child_instance_id),
  bom_node_id = VALUES(bom_node_id),
  shop_order_task_id = VALUES(shop_order_task_id),
  quantity = VALUES(quantity),
  assembled_at = VALUES(assembled_at),
  remark = VALUES(remark);

-- -----------------------------------------------------------------------------
-- 7. Complete interfaces, technical status, change and service usage.
-- -----------------------------------------------------------------------------
INSERT INTO t1_object_interface (
  id, interface_code, interface_name, interface_type, source_object_level,
  source_object_id, source_bom_node_id, target_object_level, target_object_id,
  target_bom_node_id, aircraft_id, interface_summary, requirement_summary,
  maturity_status, verification_status, effectivity, attrs_json
) VALUES
  ('c7200001-0001-4001-8001-000000000001', 'IF-SYS29-SYS32-HYD-001',
   '液压系统到起落架系统压力供给接口', 'hydraulic_pressure',
   'system', 'f1000031-0031-4031-8031-000000000031', 'f1000031-0031-4031-8031-000000000031',
   'system', 'f1000002-0002-4002-8002-000000000002', 'f1000002-0002-4002-8002-000000000002',
   @aircraft_b1234, '液压系统向起落架系统提供 15.5 MPa 正常工作压力，峰值冲击不超过 21.3 MPa。',
   '接口需满足 ATA 29/32 液压供压、压力脉动和隔振安装要求。',
   'released', 'verified', JSON_OBJECT('aircraft', 'B-1234'),
   JSON_OBJECT('normalPressureMpa', 15.5, 'impactPeakMpa', 21.3)),
  ('c7200002-0002-4002-8002-000000000002', 'IF-SUBHYD-EQP-MLG-001',
   '起落架液压子系统到主起供压设备接口', 'hydraulic_line',
   'subsystem', 'f1000032-0032-4032-8032-000000000032', 'f1000032-0032-4032-8032-000000000032',
   'equipment', 'f1000033-0033-4033-8033-000000000033', 'f1000033-0033-4033-8033-000000000033',
   @aircraft_b1234, '子系统通过主起供压设备向管路组件分配液压流量。',
   '接口需包含压力、流量、支承间距和维护可达性要求。',
   'released', 'verified', JSON_OBJECT('aircraft', 'B-1234'),
   JSON_OBJECT('flowLMin', 18.5, 'supportCount', 4)),
  ('c7200003-0003-4003-8003-000000000003', 'IF-EQP-PKG-TUBE-001',
   '主起供压设备到液压供压管路组件接口', 'mechanical_hydraulic',
   'equipment', 'f1000033-0033-4033-8033-000000000033', 'f1000033-0033-4033-8033-000000000033',
   'component', @hyd_pkg_node, @hyd_pkg_node,
   @aircraft_b1234, '供压设备出口连接液压供压管路组件，关键子件为 HYD-TUBE-MLG-32A。',
   '装配力矩 24 N·m，装后 21 MPa 保压无泄漏。',
   'released', 'verified', JSON_OBJECT('aircraft', 'B-1234'),
   JSON_OBJECT('torqueNm', 24, 'leakCheckMpa', 21))
ON DUPLICATE KEY UPDATE
  interface_summary = VALUES(interface_summary),
  requirement_summary = VALUES(requirement_summary),
  maturity_status = VALUES(maturity_status),
  verification_status = VALUES(verification_status),
  attrs_json = VALUES(attrs_json),
  updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_object_technical_status (
  id, object_profile_id, object_level, object_id, aircraft_id, bom_node_id,
  part_instance_id, status_code, status_name, baseline_code, bom_version,
  drawing_revision, process_revision, modification_status, deviation_status,
  verification_status, release_status, effective_from, source_system,
  source_table, source_record_id, attrs_json
) VALUES
  ('c7300001-0001-4001-8001-000000000001', 'f1000001-0001-4001-8001-000000000001',
   'aircraft', @aircraft_b1234, @aircraft_b1234, 'f1000001-0001-4001-8001-000000000001',
   NULL, 'TS-B1234-CFG-2026-02', 'B-1234 当前构型状态', 'B1234-CFG-2026-02',
   'SBOM-B1234-2026-02', 'C919-GA-REV-D', 'FAI-2026-02', 'no_open_mod',
   'none', 'verified', 'released', '2026-02-02 00:00:00', 'CONFIG',
   't1_aircraft_bom_node', 'f1000001-0001-4001-8001-000000000001',
   JSON_OBJECT('systemCount', 2, 'keyDemoPart', 'HYD-TUBE-MLG-32A')),
  ('c7300002-0002-4002-8002-000000000002', 'f1000031-0031-4031-8031-000000000031',
   'system', 'f1000031-0031-4031-8031-000000000031', @aircraft_b1234,
   'f1000031-0031-4031-8031-000000000031', NULL, 'TS-SYS29-HYD-2026-02',
   '液压系统技术状态', 'HYD-SYS-BL-2026-02', 'SBOM-HYD-2026-02',
   'ATA29-HYD-REV-C', 'PR-HYD-REV-B', 'no_open_mod', 'none',
   'verified', 'released', '2026-02-02 00:00:00', 'CONFIG',
   't1_aircraft_bom_node', 'f1000031-0031-4031-8031-000000000031',
   JSON_OBJECT('interfaceCode', 'IF-SYS29-SYS32-HYD-001')),
  ('c7300003-0003-4003-8003-000000000003', 'f1000032-0032-4032-8032-000000000032',
   'subsystem', 'f1000032-0032-4032-8032-000000000032', @aircraft_b1234,
   'f1000032-0032-4032-8032-000000000032', NULL, 'TS-SUBHYD-MLG-2026-02',
   '起落架液压子系统技术状态', 'HYD-MLG-BL-2026-02', 'SBOM-HYD-MLG-2026-02',
   'ATA29-MLG-REV-C', 'PR-HYD-REV-B', 'no_open_mod', 'none',
   'verified', 'released', '2026-02-02 00:00:00', 'CONFIG',
   't1_aircraft_bom_node', 'f1000032-0032-4032-8032-000000000032',
   JSON_OBJECT('interfaceCode', 'IF-SUBHYD-EQP-MLG-001')),
  ('c7300004-0004-4004-8004-000000000004', 'f1000033-0033-4033-8033-000000000033',
   'equipment', 'f1000033-0033-4033-8033-000000000033', @aircraft_b1234,
   'f1000033-0033-4033-8033-000000000033', NULL, 'TS-EQP-HYD-MLG-2026-02',
   '主起液压供压设备技术状态', 'HYD-MLG-EQP-BL-2026-02', 'SBOM-HYD-EQP-2026-02',
   'EQP-HYD-REV-C', 'PR-HYD-REV-B', 'no_open_mod', 'none',
   'verified', 'released', '2026-02-02 00:00:00', 'CONFIG',
   't1_aircraft_bom_node', 'f1000033-0033-4033-8033-000000000033',
   JSON_OBJECT('interfaceCode', 'IF-EQP-PKG-TUBE-001')),
  ('c7300005-0005-4005-8005-000000000005', @hyd_tube_node,
   'part', @hyd_tube_node, @aircraft_b1234, @hyd_tube_node,
   @hyd_tube_instance, 'TS-HYD-TUBE-32A-0042', '液压弯管技术状态',
   'HYD-MLG-TUBE-BL-2026-02', 'SBOM-HYD-PKG-2026-02',
   'C919-32-1187 Rev.C', 'PR-HYD-TUBE-32A Rev.B', 'no_open_mod',
   'none', 'verified', 'released', '2026-02-02 00:00:00', 'CONFIG',
   't1_aircraft_bom_node', @hyd_tube_node,
   JSON_OBJECT('impactPeakMpa', 21.3, 'leakRateMlMin', 0))
ON DUPLICATE KEY UPDATE
  status_name = VALUES(status_name),
  baseline_code = VALUES(baseline_code),
  bom_version = VALUES(bom_version),
  drawing_revision = VALUES(drawing_revision),
  process_revision = VALUES(process_revision),
  verification_status = VALUES(verification_status),
  release_status = VALUES(release_status),
  attrs_json = VALUES(attrs_json),
  updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_engineering_change_execution (
  id, execution_code, change_type, change_code, title, aircraft_id,
  bom_node_id, part_instance_id, execution_status, planned_start_at,
  planned_finish_at, actual_start_at, actual_finish_at, verified_by,
  verified_at, result_summary, evidence_json, created_by
) VALUES (
  'c7400001-0001-4001-8001-000000000001', 'ECE-HYD-MLG-2026-017',
  'eco', 'ECO-HYD-MLG-2026-017', '主起液压供压管路支承间距优化',
  @aircraft_b1234, 'f1000033-0033-4033-8033-000000000033', NULL,
  'completed', '2026-02-10 08:00:00', '2026-02-12 18:00:00',
  '2026-02-10 09:00:00', '2026-02-12 15:30:00', 'QA-HYD-01',
  '2026-02-12 17:00:00', '按冲击仿真复核结果调整卡箍 3 与卡箍 4 间距，装后复测通过。',
  JSON_ARRAY(JSON_OBJECT('docNo', 'ECO-HYD-MLG-2026-017'), JSON_OBJECT('docNo', 'HYD-IMP-2025-0412')),
  'system'
)
ON DUPLICATE KEY UPDATE
  execution_status = VALUES(execution_status),
  result_summary = VALUES(result_summary),
  evidence_json = VALUES(evidence_json),
  updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_life_usage_record (
  id, object_profile_id, object_level, object_id, aircraft_id, bom_node_id,
  part_instance_id, usage_source_type, usage_source_id, event_time,
  fh_delta, fc_delta, landing_delta, calendar_days_delta, total_fh_after,
  total_fc_after, remaining_life_value, remaining_life_unit, calculated_by,
  attrs_json
) VALUES
  ('c7500001-0001-4001-8001-000000000001', @hyd_tube_node, 'part',
   @hyd_tube_node, @aircraft_b1234, @hyd_tube_node, @hyd_tube_instance,
   'installation', 'c9100002-0002-4002-8002-000000000002',
   '2026-03-29 16:00:00', 0.000, 0, 0, 0, 1286.40, 742,
   8713.60, 'FH', 'install_removal_sync',
   JSON_OBJECT('event', 'post repair install', 'pressureCheck', 'PASS')),
  ('c7500002-0002-4002-8002-000000000002', @hyd_tube_node, 'part',
   @hyd_tube_node, @aircraft_b1234, @hyd_tube_node, @hyd_tube_instance,
   'flight', NULL, '2026-04-06 21:35:00',
   9.600, 5, 5, 8, 1296.00, 747,
   8704.00, 'FH', 'flight_usage_rollup',
   JSON_OBJECT('latestInspection', '无渗漏，压力脉动复核正常', 'deltaLandings', 5))
ON DUPLICATE KEY UPDATE
  total_fh_after = VALUES(total_fh_after),
  total_fc_after = VALUES(total_fc_after),
  remaining_life_value = VALUES(remaining_life_value),
  attrs_json = VALUES(attrs_json);

INSERT INTO t1_quality_text_record (
  id, record_number, lifecycle_stage, record_type, title, content, summary,
  keywords, record_time, review_status, classification, language, attachments,
  trace_code, aircraft_id, aircraft_bom_node_id, part_instance_id,
  source_record_type, source_record_id, extended_attrs, remark
) VALUES (
  'c7600001-0001-4001-8001-000000000001', 'QTR-HYD-SVC-128',
  'SERVICE', 'INSPECTION_REPORT', '液压弯管服役巡检记录',
  'B-1234 航后检查主起液压供压弯管，外观无渗漏、卡箍无松动、护套无磨穿；压力脉动复核峰峰值 0.18 MPa。',
  '主起液压供压弯管服役巡检正常，无渗漏，压力脉动恢复到正常范围。',
  'HYD-TUBE-MLG-32A;servicing;leak check;pressure pulsation',
  '2026-04-06 22:10:00', 'APPROVED', 'INTERNAL', 'zh',
  JSON_ARRAY(JSON_OBJECT('docNo', 'WC-32-1187-029', 'file', '/project1/dossier/files/2026/04/B-1234/WC-32-1187-029.pdf')),
  'HYD-TUBE-MLG-32A/HT-MLG-32A-2026-0042', @aircraft_b1234,
  @hyd_tube_node, @hyd_tube_instance, 't1_work_order', @hyd_work_order,
  JSON_OBJECT('leakCheck', 'PASS', 'pressurePulsationMpaPp', 0.18),
  '用于零件服役维修目录展示。'
)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  content = VALUES(content),
  summary = VALUES(summary),
  review_status = VALUES(review_status),
  extended_attrs = VALUES(extended_attrs),
  updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_object_lifecycle_record (
  id, object_profile_id, object_level, object_id, aircraft_id, bom_node_id,
  part_instance_id, lifecycle_stage, business_record_type,
  business_record_id, business_record_key, event_time, event_title,
  event_summary, result_status, source_system, source_table,
  source_record_id, source_trace_json, attrs_json
) VALUES
  ('c7700001-0001-4001-8001-000000000001', @hyd_tube_node, 'part',
   @hyd_tube_node, @aircraft_b1234, @hyd_tube_node, @hyd_tube_instance,
   'DESIGN', 'impact_model', 'HYD-TUBE-MLG-32A', 'HYD-IMP-2025-0412',
   '2025-12-18 10:00:00', '完成液压冲击仿真模型定义',
   '定义 5 段管路、4 个支承、Skydrol LD-4 流体和 35 ms 关阀边界条件。',
   'released', 'DESIGN', 't1_part_hydraulic_tube_impact_model',
   NULL, JSON_OBJECT('partNumber', 'HYD-TUBE-MLG-32A'),
   JSON_OBJECT('peakPressureMpa', 21.3)),
  ('c7700002-0002-4002-8002-000000000002', @hyd_tube_node, 'part',
   @hyd_tube_node, @aircraft_b1234, @hyd_tube_node, @hyd_tube_instance,
   'MANUFACTURING', 't1_shop_order', @hyd_shop_order, 'SO-HYD-2026-0042',
   '2026-01-22 16:30:00', '制造工单关闭',
   '来料复验、数控弯管、无损检测和气密标识全部完成。',
   'closed', 'MES', 't1_shop_order', @hyd_shop_order,
   JSON_OBJECT('orderCode', 'SO-HYD-2026-0042'), JSON_OBJECT('quantityProduced', 1)),
  ('c7700003-0003-4003-8003-000000000003', @hyd_tube_node, 'part',
   @hyd_tube_node, @aircraft_b1234, @hyd_tube_node, @hyd_tube_instance,
   'SERVICE', 't1_fault_event', @hyd_fault_event, '32-HYD-PULSE-01',
   '2026-03-29 08:00:00', '压力脉动故障处置',
   '拆下复查后更换支承垫片并复装，故障关闭。',
   'resolved', 'MRO', 't1_fault_event', @hyd_fault_event,
   JSON_OBJECT('woNumber', 'WO-B1234-2026-0329'), JSON_OBJECT('postRepairLeakCheck', 'PASS'))
ON DUPLICATE KEY UPDATE
  event_title = VALUES(event_title),
  event_summary = VALUES(event_summary),
  result_status = VALUES(result_status),
  attrs_json = VALUES(attrs_json),
  updated_at = CURRENT_TIMESTAMP(6);

UPDATE t1_dossier_template
SET validation_rules_json = JSON_OBJECT(
      'ruleCount', (SELECT COUNT(*) FROM t1_dossier_template_rule WHERE template_id = @tpl_v11),
      'blockerCount', (SELECT COUNT(*) FROM t1_dossier_template_rule WHERE template_id = @tpl_v11 AND severity = 'blocker'),
      'sourceCount', (SELECT COUNT(*) FROM t1_dossier_template_data_source WHERE template_id = @tpl_v11),
      'lifecycleSourcePatch', TRUE,
      'hydraulicTubeDemoData', TRUE
    ),
    updated_by = 'system',
    updated_at = CURRENT_TIMESTAMP(6)
WHERE id = @tpl_v11;

COMMIT;

SELECT 'dossier_template_lifecycle_source_patch_mysql8.sql applied' AS result,
       (SELECT COUNT(*) FROM t1_dossier_template_data_source WHERE template_id = @tpl_v11) AS source_count,
       (SELECT COUNT(*) FROM t1_dossier_template_chapter c
         WHERE c.template_id = @tpl_v11
           AND c.node_kind = 'chapter'
           AND c.enabled_flag = 1
           AND NOT EXISTS (
             SELECT 1 FROM t1_dossier_template_data_source ds
             WHERE ds.chapter_id = c.id AND ds.enabled_flag = 1
           )) AS enabled_chapter_without_source,
       (SELECT COUNT(*) FROM t1_impact_tube_fluid WHERE model_part_number = 'HYD-TUBE-MLG-32A') AS hyd_fluid_count,
       (SELECT COUNT(*) FROM t1_impact_tube_boundary_condition WHERE model_part_number = 'HYD-TUBE-MLG-32A') AS hyd_boundary_count,
       (SELECT COUNT(*) FROM t1_inspection_measurement WHERE inspection_record_id = @hyd_final_inspection) AS hyd_measurement_count,
       (SELECT COUNT(*) FROM t1_life_usage_record WHERE part_instance_id = @hyd_tube_instance) AS hyd_life_usage_count;
