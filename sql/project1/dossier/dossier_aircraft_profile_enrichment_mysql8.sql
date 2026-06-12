-- -----------------------------------------------------------------------------
-- Aircraft profile enrichment and duplicate-field policy
-- Runtime database: ry-cloud
-- MySQL 8.0+
--
-- Policy:
-- 1. t1_physical_aircraft is the source of truth for stable aircraft identity fields:
--    tail_number, msn, registration_number, aircraft_type, variant, manufacturer,
--    delivery_date, operational_status, total_fh, total_fc, current_operator.
-- 2. t1_aircraft_object_profile stores display enrichment, certificates, ownership,
--    delivery/airworthiness status, fleet/base information, and cached aggregates.
-- 3. Full lifecycle events stay in event/detail tables such as
--    t1_object_lifecycle_record, t1_object_status_history, t1_life_usage_record,
--    t1_fault_event, t1_work_order, t1_inspection_record, and dossier_* tables.
-- 4. Frontend/detail pages should prefer v_aircraft_profile_detail so duplicate
--    fields are read from t1_physical_aircraft consistently.
-- -----------------------------------------------------------------------------

USE `ry-cloud`;

DROP TEMPORARY TABLE IF EXISTS tmp_aircraft_profile_seed;

CREATE TEMPORARY TABLE tmp_aircraft_profile_seed (
  tail_number varchar(50) NOT NULL PRIMARY KEY,
  registration_number varchar(50) NOT NULL,
  aircraft_variant varchar(100) NOT NULL,
  engine_type varchar(200) NOT NULL,
  manufacturer varchar(200) NOT NULL,
  final_assembly_site varchar(200) NOT NULL,
  production_batch varchar(100) NOT NULL,
  line_number varchar(100) NOT NULL,
  rollout_date date NOT NULL,
  first_flight_date date NOT NULL,
  delivery_date date NOT NULL,
  acceptance_date date NOT NULL,
  type_certificate_no varchar(200) NOT NULL,
  airworthiness_certificate_no varchar(200) NOT NULL,
  owner_org varchar(200) NOT NULL,
  operator_org varchar(200) NOT NULL,
  base_airport varchar(50) NOT NULL,
  fleet_code varchar(100) NOT NULL,
  aircraft_lifecycle_status varchar(50) NOT NULL,
  airworthiness_status varchar(50) NOT NULL,
  delivery_status varchar(50) NOT NULL,
  current_configuration_baseline varchar(200) NOT NULL,
  current_bom_version varchar(100) NOT NULL,
  next_due_maintenance varchar(500) NOT NULL
) ENGINE=Memory DEFAULT CHARSET=utf8mb4;

INSERT INTO tmp_aircraft_profile_seed (
  tail_number, registration_number, aircraft_variant, engine_type, manufacturer,
  final_assembly_site, production_batch, line_number, rollout_date, first_flight_date,
  delivery_date, acceptance_date, type_certificate_no, airworthiness_certificate_no,
  owner_org, operator_org, base_airport, fleet_code, aircraft_lifecycle_status,
  airworthiness_status, delivery_status, current_configuration_baseline,
  current_bom_version, next_due_maintenance
) VALUES
  (
    'B-1234', 'B-1234', 'C919-STD-2025A', 'LEAP-1C',
    '中国商飞总装制造中心', '浦东总装基地', 'C919-BATCH-2025-01', 'FAL-PVG-C919-001',
    '2025-08-29', '2025-09-18', '2025-12-20', '2025-12-18',
    'CAAC-TC-C919-2022-001', 'CAAC-AWC-B1234-2025-001',
    '中国商飞试飞与交付中心', '中国商飞试飞与交付中心', 'ZSPD', 'C919-DEMO-FLEET',
    'IN_SERVICE', 'VALID', 'DELIVERED', 'BL-C919-B1234-2026-01',
    'BOM-C919-B1234-ASMAINT-V1.0',
    'A检计划：2026-07-15 或 1800FH/1500FC 先到；重点复核液压系统与起落架系统。'
  ),
  (
    'B-1235', 'B-1235', 'C919-STD-2025A', 'LEAP-1C',
    '中国商飞总装制造中心', '浦东总装基地', 'C919-BATCH-2025-01', 'FAL-PVG-C919-002',
    '2025-09-10', '2025-09-29', '2026-01-12', '2026-01-10',
    'CAAC-TC-C919-2022-001', 'CAAC-AWC-B1235-2026-001',
    '东方航空股份有限公司', '东方航空股份有限公司', 'ZSPD', 'C919-EASTERN-01',
    'IN_SERVICE', 'VALID', 'DELIVERED', 'BL-C919-B1235-2026-01',
    'BOM-C919-B1235-ASMAINT-V1.0',
    'A检计划：2026-08-10 或 750FH/600FC 先到。'
  ),
  (
    'B-1236', 'B-1236', 'C919-STD-2025B', 'LEAP-1C',
    '中国商飞总装制造中心', '浦东总装基地', 'C919-BATCH-2025-02', 'FAL-PVG-C919-003',
    '2025-10-05', '2025-10-23', '2026-02-18', '2026-02-16',
    'CAAC-TC-C919-2022-001', 'CAAC-AWC-B1236-2026-001',
    '东方航空股份有限公司', '东方航空股份有限公司', 'ZSPD', 'C919-EASTERN-01',
    'IN_SERVICE', 'VALID', 'DELIVERED', 'BL-C919-B1236-2026-02',
    'BOM-C919-B1236-ASMAINT-V1.0',
    'A检计划：2026-09-05 或 750FH/600FC 先到。'
  ),
  (
    'B-8891', 'B-8891', 'ARJ21-700-STD', 'CF34-10A',
    '中国商飞总装制造中心', '浦东总装基地', 'ARJ21-BATCH-2025-01', 'FAL-PVG-ARJ21-001',
    '2025-05-12', '2025-05-28', '2025-08-15', '2025-08-13',
    'CAAC-TC-ARJ21-2014-001', 'CAAC-AWC-B8891-2025-001',
    '成都航空股份有限公司', '成都航空股份有限公司', 'ZUUU', 'ARJ21-CDAIR-01',
    'IN_SERVICE', 'VALID', 'DELIVERED', 'BL-ARJ21-B8891-2025-08',
    'BOM-ARJ21-B8891-ASMAINT-V1.0',
    'A检计划：2026-07-28 或 600FH/500FC 先到。'
  ),
  (
    'B-8892', 'B-8892', 'ARJ21-700-STD', 'CF34-10A',
    '中国商飞总装制造中心', '浦东总装基地', 'ARJ21-BATCH-2025-01', 'FAL-PVG-ARJ21-002',
    '2025-06-08', '2025-06-22', '2025-09-20', '2025-09-18',
    'CAAC-TC-ARJ21-2014-001', 'CAAC-AWC-B8892-2025-001',
    '成都航空股份有限公司', '成都航空股份有限公司', 'ZUUU', 'ARJ21-CDAIR-01',
    'IN_SERVICE', 'VALID', 'DELIVERED', 'BL-ARJ21-B8892-2025-09',
    'BOM-ARJ21-B8892-ASMAINT-V1.0',
    'A检计划：2026-08-30 或 600FH/500FC 先到。'
  ),
  (
    'B-8893', 'B-8893', 'ARJ21-700-STD', 'CF34-10A',
    '中国商飞总装制造中心', '浦东总装基地', 'ARJ21-BATCH-2025-02', 'FAL-PVG-ARJ21-003',
    '2025-07-01', '2025-07-16', '2025-10-25', '2025-10-23',
    'CAAC-TC-ARJ21-2014-001', 'CAAC-AWC-B8893-2025-001',
    '成都航空股份有限公司', '成都航空股份有限公司', 'ZUUU', 'ARJ21-CDAIR-02',
    'IN_SERVICE', 'VALID', 'DELIVERED', 'BL-ARJ21-B8893-2025-10',
    'BOM-ARJ21-B8893-ASMAINT-V1.0',
    'A检计划：2026-09-20 或 600FH/500FC 先到。'
  );

-- Fill stable aircraft master data. This does not store lifecycle events; it only
-- fills aircraft identity/current-status attributes already owned by this table.
UPDATE t1_physical_aircraft pa
JOIN tmp_aircraft_profile_seed s ON s.tail_number = pa.tail_number
LEFT JOIN (
  SELECT
    aircraft_id,
    MAX(total_fh_after) AS total_fh_after,
    MAX(total_fc_after) AS total_fc_after
  FROM t1_life_usage_record
  WHERE object_level = 'aircraft'
  GROUP BY aircraft_id
) lu ON lu.aircraft_id = pa.id
SET
  pa.registration_number = COALESCE(NULLIF(pa.registration_number, ''), s.registration_number),
  pa.variant = COALESCE(NULLIF(pa.variant, ''), s.aircraft_variant),
  pa.engine_type = COALESCE(NULLIF(pa.engine_type, ''), s.engine_type),
  pa.manufacturer = CASE
    WHEN pa.manufacturer IS NULL OR pa.manufacturer = '' OR pa.manufacturer REGEXP '^\\?+$' THEN s.manufacturer
    ELSE pa.manufacturer
  END,
  pa.delivery_date = COALESCE(pa.delivery_date, s.delivery_date),
  pa.current_operator = CASE
    WHEN pa.current_operator IS NULL OR pa.current_operator = '' OR pa.current_operator REGEXP '^\\?+$' THEN s.operator_org
    ELSE pa.current_operator
  END,
  pa.total_fh = CASE
    WHEN COALESCE(pa.total_fh, 0) = 0 AND lu.total_fh_after IS NOT NULL THEN lu.total_fh_after
    ELSE pa.total_fh
  END,
  pa.total_fc = CASE
    WHEN COALESCE(pa.total_fc, 0) = 0 AND lu.total_fc_after IS NOT NULL THEN lu.total_fc_after
    ELSE pa.total_fc
  END,
  pa.updated_at = CURRENT_TIMESTAMP(6);

INSERT INTO t1_aircraft_object_profile (
  aircraft_id, object_profile_id, aircraft_no, registration_no, msn, line_number,
  aircraft_model_id, aircraft_model_code, aircraft_model_name, aircraft_variant,
  type_certificate_no, airworthiness_certificate_no, manufacturer,
  final_assembly_site, production_batch, rollout_date, first_flight_date,
  delivery_date, acceptance_date, owner_org, operator_org, base_airport, fleet_code,
  aircraft_lifecycle_status, operational_status, airworthiness_status,
  delivery_status, current_configuration_baseline, current_bom_version,
  major_system_count, installed_equipment_count, open_engineering_change_count,
  total_flight_hours, total_flight_cycles, total_landings, latest_maintenance_date,
  next_due_maintenance, open_fault_count, current_dossier_version_id,
  data_snapshot_id, attrs_json
)
SELECT
  pa.id,
  pop.id,
  pa.tail_number,
  pa.registration_number,
  pa.msn,
  s.line_number,
  pa.model_id,
  am.model_code,
  COALESCE(am.name, pa.aircraft_type),
  pa.variant,
  s.type_certificate_no,
  s.airworthiness_certificate_no,
  pa.manufacturer,
  s.final_assembly_site,
  s.production_batch,
  s.rollout_date,
  s.first_flight_date,
  pa.delivery_date,
  s.acceptance_date,
  s.owner_org,
  pa.current_operator,
  s.base_airport,
  s.fleet_code,
  s.aircraft_lifecycle_status,
  pa.operational_status,
  s.airworthiness_status,
  s.delivery_status,
  s.current_configuration_baseline,
  s.current_bom_version,
  COALESCE(bs.major_system_count, 0),
  COALESCE(bs.installed_equipment_count, 0),
  COALESCE(ec.open_engineering_change_count, 0),
  COALESCE(lu.total_fh_after, pa.total_fh, 0),
  COALESCE(lu.total_fc_after, pa.total_fc, 0),
  lu.total_landings,
  wm.latest_maintenance_date,
  s.next_due_maintenance,
  COALESCE(fs.open_fault_count, 0),
  dv.current_dossier_version_id,
  dv.data_snapshot_id,
  JSON_OBJECT(
    'profilePolicy', 't1_physical_aircraft is canonical for duplicate identity fields',
    'mockDataBatch', 'AIRCRAFT_PROFILE_ENRICH_20260609',
    'mockData', true,
    'lifecycleStorage', 'event tables only; profile keeps aggregates and display fields'
  )
FROM t1_physical_aircraft pa
JOIN tmp_aircraft_profile_seed s ON s.tail_number = pa.tail_number
LEFT JOIN t1_ac_model am ON am.id = pa.model_id
LEFT JOIN t1_product_object_profile pop
  ON pop.aircraft_id = pa.id AND pop.object_level = 'aircraft'
LEFT JOIN (
  SELECT
    aircraft_id,
    SUM(CASE WHEN UPPER(node_type) = 'SYSTEM' AND is_active = 1 THEN 1 ELSE 0 END) AS major_system_count,
    SUM(CASE WHEN UPPER(node_type) = 'EQUIPMENT' AND is_active = 1 THEN 1 ELSE 0 END) AS installed_equipment_count
  FROM t1_aircraft_bom_node
  GROUP BY aircraft_id
) bs ON bs.aircraft_id = pa.id
LEFT JOIN (
  SELECT
    aircraft_id,
    MAX(total_fh_after) AS total_fh_after,
    MAX(total_fc_after) AS total_fc_after,
    SUM(landing_delta) AS total_landings
  FROM t1_life_usage_record
  WHERE object_level = 'aircraft'
  GROUP BY aircraft_id
) lu ON lu.aircraft_id = pa.id
LEFT JOIN (
  SELECT
    aircraft_id,
    COUNT(*) AS open_fault_count
  FROM t1_fault_event
  WHERE UPPER(status) NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED')
  GROUP BY aircraft_id
) fs ON fs.aircraft_id = pa.id
LEFT JOIN (
  SELECT
    aircraft_id,
    MAX(DATE(COALESCE(release_date, close_date, open_date))) AS latest_maintenance_date
  FROM t1_work_order
  WHERE UPPER(wo_status) IN ('CLOSED', 'RELEASED', 'COMPLETED')
  GROUP BY aircraft_id
) wm ON wm.aircraft_id = pa.id
LEFT JOIN (
  SELECT
    aircraft_id,
    COUNT(*) AS open_engineering_change_count
  FROM t1_engineering_change_execution
  WHERE UPPER(execution_status) NOT IN ('CLOSED', 'COMPLETED', 'CANCELLED')
  GROUP BY aircraft_id
) ec ON ec.aircraft_id = pa.id
LEFT JOIN (
  SELECT aircraft_id, current_dossier_version_id, data_snapshot_id
  FROM (
    SELECT
      di.aircraft_id,
      dv.id AS current_dossier_version_id,
      dv.data_snapshot_id,
      ROW_NUMBER() OVER (
        PARTITION BY di.aircraft_id
        ORDER BY dv.created_at DESC, dv.id DESC
      ) AS rn
    FROM t1_dossier_instance di
    JOIN t1_dossier_version dv ON dv.dossier_instance_id = di.id
    WHERE di.deleted_at IS NULL AND dv.is_current = 1
  ) ranked
  WHERE rn = 1
) dv ON dv.aircraft_id = pa.id
ON DUPLICATE KEY UPDATE
  object_profile_id = VALUES(object_profile_id),
  aircraft_no = VALUES(aircraft_no),
  registration_no = VALUES(registration_no),
  msn = VALUES(msn),
  line_number = VALUES(line_number),
  aircraft_model_id = VALUES(aircraft_model_id),
  aircraft_model_code = VALUES(aircraft_model_code),
  aircraft_model_name = VALUES(aircraft_model_name),
  aircraft_variant = VALUES(aircraft_variant),
  type_certificate_no = VALUES(type_certificate_no),
  airworthiness_certificate_no = VALUES(airworthiness_certificate_no),
  manufacturer = VALUES(manufacturer),
  final_assembly_site = VALUES(final_assembly_site),
  production_batch = VALUES(production_batch),
  rollout_date = VALUES(rollout_date),
  first_flight_date = VALUES(first_flight_date),
  delivery_date = VALUES(delivery_date),
  acceptance_date = VALUES(acceptance_date),
  owner_org = VALUES(owner_org),
  operator_org = VALUES(operator_org),
  base_airport = VALUES(base_airport),
  fleet_code = VALUES(fleet_code),
  aircraft_lifecycle_status = VALUES(aircraft_lifecycle_status),
  operational_status = VALUES(operational_status),
  airworthiness_status = VALUES(airworthiness_status),
  delivery_status = VALUES(delivery_status),
  current_configuration_baseline = VALUES(current_configuration_baseline),
  current_bom_version = VALUES(current_bom_version),
  major_system_count = VALUES(major_system_count),
  installed_equipment_count = VALUES(installed_equipment_count),
  open_engineering_change_count = VALUES(open_engineering_change_count),
  total_flight_hours = VALUES(total_flight_hours),
  total_flight_cycles = VALUES(total_flight_cycles),
  total_landings = VALUES(total_landings),
  latest_maintenance_date = VALUES(latest_maintenance_date),
  next_due_maintenance = VALUES(next_due_maintenance),
  open_fault_count = VALUES(open_fault_count),
  current_dossier_version_id = VALUES(current_dossier_version_id),
  data_snapshot_id = VALUES(data_snapshot_id),
  attrs_json = JSON_MERGE_PATCH(
    COALESCE(t1_aircraft_object_profile.attrs_json, JSON_OBJECT()),
    VALUES(attrs_json)
  ),
  updated_at = CURRENT_TIMESTAMP(6);

-- Canonical aircraft detail view for page/API display.
-- It reads duplicate identity fields from t1_physical_aircraft, not from the cached
-- values in t1_aircraft_object_profile.
CREATE OR REPLACE VIEW v_aircraft_profile_detail AS
SELECT
  pa.id AS aircraft_id,
  pop.id AS object_profile_id,
  pa.tail_number,
  pa.registration_number,
  pa.msn,
  pa.model_id,
  am.model_code,
  COALESCE(am.name, pa.aircraft_type) AS model_name,
  pa.aircraft_type,
  pa.variant,
  pa.engine_type,
  pa.manufacturer,
  pa.delivery_date,
  pa.operational_status,
  pa.total_fh,
  pa.total_fc,
  pa.current_operator,
  aop.line_number,
  aop.type_certificate_no,
  aop.airworthiness_certificate_no,
  aop.final_assembly_site,
  aop.production_batch,
  aop.rollout_date,
  aop.first_flight_date,
  aop.acceptance_date,
  aop.owner_org,
  aop.base_airport,
  aop.fleet_code,
  aop.aircraft_lifecycle_status,
  aop.airworthiness_status,
  aop.delivery_status,
  aop.current_configuration_baseline,
  aop.current_bom_version,
  aop.major_system_count,
  aop.installed_equipment_count,
  aop.open_engineering_change_count,
  aop.total_landings,
  aop.latest_maintenance_date,
  aop.next_due_maintenance,
  aop.open_fault_count,
  aop.current_dossier_version_id,
  aop.data_snapshot_id,
  aop.attrs_json,
  pa.created_at AS aircraft_created_at,
  pa.updated_at AS aircraft_updated_at,
  aop.updated_at AS profile_updated_at
FROM t1_physical_aircraft pa
LEFT JOIN t1_ac_model am ON am.id = pa.model_id
LEFT JOIN t1_aircraft_object_profile aop ON aop.aircraft_id = pa.id
LEFT JOIN t1_product_object_profile pop
  ON pop.aircraft_id = pa.id AND pop.object_level = 'aircraft';

-- Operational check view. It should return zero rows after this script runs.
CREATE OR REPLACE VIEW v_aircraft_profile_duplicate_check AS
SELECT
  pa.id AS aircraft_id,
  pa.tail_number,
  NOT (pa.tail_number <=> aop.aircraft_no) AS aircraft_no_mismatch,
  NOT (pa.registration_number <=> aop.registration_no) AS registration_mismatch,
  NOT (pa.msn <=> aop.msn) AS msn_mismatch,
  NOT (pa.model_id <=> aop.aircraft_model_id) AS model_id_mismatch,
  NOT (am.model_code <=> aop.aircraft_model_code) AS model_code_mismatch,
  NOT (COALESCE(am.name, pa.aircraft_type) <=> aop.aircraft_model_name) AS model_name_mismatch,
  NOT (pa.variant <=> aop.aircraft_variant) AS variant_mismatch,
  NOT (pa.manufacturer <=> aop.manufacturer) AS manufacturer_mismatch,
  NOT (pa.delivery_date <=> aop.delivery_date) AS delivery_date_mismatch,
  NOT (pa.operational_status <=> aop.operational_status) AS operational_status_mismatch,
  NOT (pa.current_operator <=> aop.operator_org) AS operator_mismatch
FROM t1_physical_aircraft pa
LEFT JOIN t1_ac_model am ON am.id = pa.model_id
LEFT JOIN t1_aircraft_object_profile aop ON aop.aircraft_id = pa.id
WHERE
  aop.aircraft_id IS NULL
  OR NOT (pa.tail_number <=> aop.aircraft_no)
  OR NOT (pa.registration_number <=> aop.registration_no)
  OR NOT (pa.msn <=> aop.msn)
  OR NOT (pa.model_id <=> aop.aircraft_model_id)
  OR NOT (am.model_code <=> aop.aircraft_model_code)
  OR NOT (COALESCE(am.name, pa.aircraft_type) <=> aop.aircraft_model_name)
  OR NOT (pa.variant <=> aop.aircraft_variant)
  OR NOT (pa.manufacturer <=> aop.manufacturer)
  OR NOT (pa.delivery_date <=> aop.delivery_date)
  OR NOT (pa.operational_status <=> aop.operational_status)
  OR NOT (pa.current_operator <=> aop.operator_org);

DROP TEMPORARY TABLE IF EXISTS tmp_aircraft_profile_seed;
