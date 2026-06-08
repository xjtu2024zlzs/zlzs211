-- Literature-backed dossier data enrichment for B-1234.
-- This script keeps template_version unchanged. It adds reference-driven aircraft,
-- hydraulic system, and landing gear lifecycle data aligned to the existing dossier chapters.

SET NAMES utf8mb4;

SET @template_id = 't1000001-0001-4001-8001-000000000111';
SET @aircraft_id = 'b0000001-0001-4001-8001-000000000001';
SET @aircraft_bom_root_id = 'f1000001-0001-4001-8001-000000000001';
SET @hyd_system_id = 'f1000031-0031-4031-8031-000000000031';
SET @lgr_system_id = 'f1000002-0002-4002-8002-000000000002';

DELETE FROM t1_inspection_measurement WHERE id LIKE 'refim%';
DELETE FROM t1_inspection_record WHERE id LIKE 'refir%';
DELETE FROM t1_life_usage_record WHERE id LIKE 'reflu%';
DELETE FROM t1_object_lifecycle_record WHERE id LIKE 'refolr%';
DELETE FROM t1_object_technical_status WHERE id LIKE 'refts%';
DELETE FROM t1_object_status_history WHERE id LIKE 'refsh%';
DELETE FROM t1_object_interface WHERE id LIKE 'refif%' OR interface_code LIKE 'IF-REF-%';
DELETE FROM t1_work_order WHERE id LIKE 'refwo%' OR wo_number LIKE 'WO-REF-%';
DELETE FROM t1_fault_event WHERE id LIKE 'refflt%';
DELETE FROM t1_part_document WHERE id LIKE 'refpd%' OR doc_number LIKE 'DOC-REF-%';
DELETE FROM t1_event_flight_leg WHERE id LIKE 'reflg%' OR leg_number LIKE 'B1234-REF-%';
DELETE FROM t1_dossier_template_data_source WHERE template_id = @template_id AND id LIKE 'refds%';

INSERT INTO t1_dossier_template_data_source (
  id, template_id, chapter_id, source_code, source_system, source_table,
  source_name, source_desc, lifecycle_stage, source_record_type,
  join_condition_json, filter_condition_json, apply_object_type,
  supply_mode_scope, key_part_scope, required_flag, enabled_flag,
  sort_order, attrs_json, created_by, updated_by
)
SELECT concat('refds', substr(md5(concat(c.chapter_code, '|', x.source_code)), 1, 31)),
       @template_id,
       c.id,
       x.source_code,
       x.source_system,
       x.source_table,
       x.source_name,
       x.source_desc,
       x.lifecycle_stage,
       x.source_record_type,
       x.join_condition_json,
       x.filter_condition_json,
       x.apply_object_type,
       'all',
       'all',
       x.required_flag,
       1,
       x.sort_order,
       json_object('basis', x.basis, 'referenceDriven', true, 'batch', 'REF_PUBLIC_20260608'),
       'codex',
       'codex'
  FROM (
    SELECT 'AIRCRAFT_DESIGN' chapter_code, 'SRC-REF-AIRCRAFT-DESIGN-LIFECYCLE' source_code, 'PLM' source_system, 't1_object_lifecycle_record' source_table,
           '整机设计与符合性生命周期事件' source_name,
           '覆盖需求冻结、系统安全评估、维护性分析、适航符合性矩阵、接口控制和卷宗对象锚点。' source_desc,
           'design' lifecycle_stage, 'lifecycle_event' source_record_type,
           json_object('aircraft_id','${aircraftId}','object_level','aircraft') join_condition_json,
           json_object('batch','REF_PUBLIC_20260608') filter_condition_json,
           'aircraft' apply_object_type, 1 required_flag, 610 sort_order,
           'FAA AMT handbook, eCFR/CS-25, continuing-airworthiness record practice' basis
    UNION ALL
    SELECT 'AIRCRAFT_MANUFACTURING', 'SRC-REF-AIRCRAFT-MANUFACTURING', 'MES', 't1_inspection_record',
           '整机制造、检验和总装记录',
           '覆盖总装、液压压力试验、起落架收放试验、地面联试、交付前构型确认。',
           'manufacturing', 't1_inspection_record',
           json_object('aircraft_id','${aircraftId}'), json_object('result','PASS'),
           'aircraft', 1, 620,
           'FAA AMT representative systems and manufacturing traceability practice'
    UNION ALL
    SELECT 'AIRCRAFT_SERVICE', 'SRC-REF-AIRCRAFT-FLIGHT-USAGE', 'OPS', 't1_event_flight_leg',
           '整机航段与寿命消耗记录',
           '覆盖航段、飞行小时、飞行循环、起落次数和持续适航状态。',
           'service', 'flight_usage',
           json_object('aircraft_id','${aircraftId}'), json_object('tail_number','B-1234'),
           'aircraft', 1, 630,
           'Continuing-airworthiness records include total time, cycles, landings and maintenance status'
    UNION ALL
    SELECT 'AIRCRAFT_FAULT', 'SRC-REF-AIRCRAFT-DEFECT-CLOSURE', 'MRO', 't1_fault_event',
           '整机缺陷与故障闭环',
           '覆盖监控告警、机组报告、检查发现、延期项和关闭记录。',
           'fault', 't1_fault_event',
           json_object('aircraft_id','${aircraftId}'), json_object('status','RESOLVED'),
           'aircraft', 1, 640,
           'Continuing-airworthiness defect recording and rectification practice'
    UNION ALL
    SELECT 'AIRCRAFT_TECH_STATUS', 'SRC-REF-AIRCRAFT-TECH-STATUS', 'CONFIG', 't1_object_technical_status',
           '整机构型和技术状态',
           '覆盖构型基线、适航指令/服务通告适用性、寿命件状态和当前放行状态。',
           'technical_status', 'technical_status',
           json_object('aircraft_id','${aircraftId}'), json_object('release_status','released'),
           'aircraft', 1, 650,
           'Continuing-airworthiness records include AD/SB status and life-limited parts'
    UNION ALL
    SELECT 'SYSTEM_MAINTENANCE', 'SRC-REF-SYSTEM-WORK-ORDER', 'MRO', 't1_work_order',
           '系统维护工单',
           '液压和起落架系统的压力、污染度、收放、锁定、刹车、防滑和应急放下检查工单。',
           'service', 't1_work_order',
           json_object('aircraft_id','${aircraftId}','bom_node_id','${nodeId}'), json_object('scope','SYS-29/SYS-32'),
           'system', 1, 1068,
           'FAA AMT hydraulic and landing gear maintenance topics'
    UNION ALL
    SELECT 'SYSTEM_INTERFACE', 'SRC-REF-SYSTEM-INTERFACE', 'CONFIG', 't1_object_interface',
           '系统接口与验证要求',
           '液压供压、起落架作动、锁定反馈、刹车控制和位置告警接口。',
           'design', 'interface',
           json_object('aircraft_id','${aircraftId}','source_bom_node_id','${nodeId}'), json_object('scope','SYS-29/SYS-32'),
           'system', 1, 1038,
           '14 CFR/CS-25 landing gear and hydraulic system safety requirements'
    UNION ALL
    SELECT 'PART_INSPECTION', 'SRC-REF-PART-INSPECTION-MEASUREMENT', 'QA', 't1_inspection_measurement',
           '零件级检验测量项',
           '对液压弯管、管接头、作动筒、锁机构、刹车/机轮零件补充压力、间隙、清洁度和磨耗测量。',
           'inspection', 't1_inspection_measurement',
           json_object('aircraft_id','${aircraftId}','bom_node_id','${nodeId}'), json_object('result_flag','PASS'),
           'part', 1, 4048,
           'FAA AMT shock strut, tire, brake and hydraulic servicing inspection topics'
  ) x
  JOIN t1_dossier_template_chapter c
    ON c.template_id = @template_id
   AND c.chapter_code = x.chapter_code;

DROP TEMPORARY TABLE IF EXISTS tmp_ref_scope_nodes;
CREATE TEMPORARY TABLE tmp_ref_scope_nodes AS
WITH RECURSIVE system_tree AS (
  SELECT id, parent_id, aircraft_id, node_level, node_type, part_number, part_name,
         serial_number, position_code, ata_chapter, part_instance_id, part_number AS root_scope
    FROM t1_aircraft_bom_node
   WHERE aircraft_id = @aircraft_id
     AND id IN (@hyd_system_id, @lgr_system_id)
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

DROP TEMPORARY TABLE IF EXISTS tmp_ref_aircraft_events;
CREATE TEMPORARY TABLE tmp_ref_aircraft_events (
  event_key varchar(80) NOT NULL,
  lifecycle_stage varchar(30) NOT NULL,
  business_record_type varchar(100) NOT NULL,
  event_time datetime(6) NOT NULL,
  event_title varchar(500) NOT NULL,
  event_summary text NOT NULL,
  result_status varchar(50) NOT NULL,
  source_system varchar(50) NOT NULL,
  source_table varchar(200) NOT NULL,
  sort_order int NOT NULL
);

INSERT INTO tmp_ref_aircraft_events VALUES
('AC-DES-001','design','requirements_baseline','2024-03-18 09:00:00','B-1234 单台份任务剖面和卷宗对象范围冻结','整机任务剖面按短中程航线、试飞验证、交付前演示和运营维护场景冻结；对象范围覆盖整机、ATA 29 液压系统、ATA 32 起落架系统及其下级设备、组件、零件。','released','PLM','requirements_baseline',10),
('AC-DES-002','design','system_safety_assessment','2024-04-06 10:00:00','整机系统安全评估闭环','完成液压供压丧失、起落架未锁定、刹车压力异常、位置指示不一致等危害条目的系统级安全评估和验证项分配。','released','PLM','safety_assessment',20),
('AC-DES-003','design','maintenance_program_analysis','2024-04-22 14:00:00','维护显著项目和任务间隔分析完成','参考公开维修手册中的液压、起落架、轮胎、刹车、减震支柱维护主题，建立 A 检、C 检、航线检查和专项复核任务。','released','MRO','maintenance_program',30),
('AC-DES-004','design','certification_basis_matrix','2024-05-08 11:30:00','适航符合性矩阵建立','将液压系统 proof/ultimate 压力、起落架收放、锁定、应急放下、位置告警和刹车控制要求挂接到系统/部件验证记录。','released','AIRWORTHINESS','compliance_matrix',40),
('AC-DES-005','design','interface_control','2024-05-26 15:20:00','液压与起落架接口控制文件发布','冻结 SYS-29 至 SYS-32 的供压、回油、作动、锁定反馈、刹车和防滑控制接口。','released','PLM','interface_control_document',50),
('AC-DES-006','design','digital_dossier_anchor','2024-06-10 09:40:00','整机数字卷宗对象锚点方案发布','确认 aircraft_id、bom_node_id、part_instance_id、source_record_key 四类锚点在设计、制造、装机、服役记录中的统一用法。','released','DOSSIER','object_anchor_plan',60),
('AC-MFG-001','manufacturing','major_join','2025-06-14 08:30:00','机体大部段对接完成','机身、机翼、尾翼大部段对接完成，关键测量点复核合格，进入系统安装阶段。','accepted','MES','major_join_record',70),
('AC-MFG-002','manufacturing','system_installation','2025-07-03 16:00:00','液压与起落架系统装机放行','液压泵、蓄压器、阀组件、管路、主/前起落架、刹车与机轮组件装机记录完成。','accepted','MES','system_installation_record',80),
('AC-MFG-003','inspection','hydraulic_pressure_test','2025-07-18 13:00:00','整机液压压力与泄漏检查通过','按系统工作压力、保压和泄漏检查要求完成地面液压联试；关键管路和接头无渗漏。','pass','QA','hydraulic_pressure_test',90),
('AC-MFG-004','inspection','landing_gear_swing_test','2025-07-26 10:00:00','起落架收放和锁定试验通过','完成正常收放、上锁/下锁、位置指示、舱门联动和应急放下功能验证。','pass','QA','landing_gear_swing_test',100),
('AC-MFG-005','inspection','brake_antiskid_test','2025-08-02 11:00:00','刹车与防滑地面联试通过','完成刹车压力调节、防滑阀响应、机轮速度传感器和驾驶舱告警链路检查。','pass','QA','brake_antiskid_test',110),
('AC-MFG-006','manufacturing','power_on_ground_test','2025-08-13 09:00:00','整机通电与维护信息系统联试完成','通电后维护信息、液压压力、起落架位置、刹车压力和故障告警信号采集正常。','accepted','MES','power_on_ground_test',120),
('AC-INST-001','installation','configuration_snapshot','2025-08-29 17:00:00','交付前构型快照冻结','BOM 构型、序列号、装机位置、寿命件状态和关键附件完成冻结。','installed','CONFIG','configuration_snapshot',130),
('AC-INST-002','installation','delivery_acceptance','2025-09-15 15:30:00','单台份交付验收记录关闭','完成交付验收、随机文件核验、维修记录初始化和持续适航记录起始状态建立。','accepted','DELIVERY','delivery_acceptance_record',140),
('AC-SRV-001','service','entry_into_service','2025-10-01 09:00:00','B-1234 进入演示运营服役','运营记录开始累计飞行小时、飞行循环、起落次数、定检和缺陷闭环信息。','in_service','OPS','service_entry_record',150),
('AC-SRV-002','service','a_check','2025-11-18 18:00:00','首次 A 检关闭','完成液压油量、污染度、渗漏、起落架目视、轮胎磨耗、刹车磨耗和勤务点状态复核。','closed','MRO','t1_work_order',160),
('AC-SRV-003','service','reliability_review','2026-01-12 15:00:00','三个月可靠性评审完成','液压压力波动、起落架锁定趋势、刹车温度和轮胎磨耗趋势均在演示阈值内。','closed','MRO','reliability_review',170),
('AC-SRV-004','fault','defect_review','2026-03-05 10:30:00','整机缺陷清单月度复核完成','未关闭缺陷为 0；液压弯管压力脉动复核项已转为预防性维护闭环。','closed','MRO','defect_review',180),
('AC-SRV-005','technical_status','ad_sb_review','2026-04-16 11:00:00','AD/SB 适用性复核完成','完成适航指令、服务通告、构型偏离和寿命件状态复核，未发现阻断项。','released','AIRWORTHINESS','ad_sb_status',190),
('AC-SRV-006','attachment','records_audit','2026-06-08 09:00:00','公开资料驱动数据补强完成','根据公开维修手册、适航条款和持续适航记录口径，补强整机、液压系统和起落架系统全生命周期数据。','included','DOSSIER','reference_data_enrichment',200);

INSERT INTO t1_object_lifecycle_record (
  id, object_profile_id, object_level, object_id, aircraft_id, bom_node_id, part_instance_id,
  lifecycle_stage, business_record_type, business_record_id, business_record_key,
  event_time, event_title, event_summary, result_status, source_system, source_table,
  source_record_id, source_trace_json, attrs_json
)
SELECT concat('refolr', substr(md5(concat('aircraft|', e.event_key)), 1, 30)),
       @aircraft_bom_root_id, 'aircraft', @aircraft_bom_root_id,
       @aircraft_id, @aircraft_bom_root_id, NULL,
       e.lifecycle_stage, e.business_record_type,
       concat('refbiz', substr(md5(e.event_key), 1, 30)),
       concat('B-1234/', e.event_key),
       e.event_time, e.event_title, e.event_summary, e.result_status,
       e.source_system, e.source_table, @aircraft_bom_root_id,
       json_object('aircraftId', @aircraft_id, 'tailNumber', 'B-1234', 'referenceBatch', 'REF_PUBLIC_20260608', 'eventKey', e.event_key),
       json_object('chapterFocus', 'aircraft_full_lifecycle', 'sortOrder', e.sort_order, 'sourceBasis', 'public_reference_and_local_generation')
  FROM tmp_ref_aircraft_events e;

DROP TEMPORARY TABLE IF EXISTS tmp_ref_system_templates;
CREATE TEMPORARY TABLE tmp_ref_system_templates AS
SELECT 'SYS-DES-ARCH' event_key, 'design' lifecycle_stage, 'system_architecture' business_record_type, 0 day_offset,
       '系统架构与边界复核完成' title_suffix,
       '按系统边界、接口、关键功能、故障影响和维修任务重新梳理目录数据来源。' summary_suffix,
       'released' result_status, 'PLM' source_system, 'system_architecture' source_table
UNION ALL SELECT 'SYS-DES-SAFETY', 'design', 'safety_requirement', 8, '安全与适航要求分配完成',
       '将相关适航要求分配到系统、设备、组件和关键零件验证项。', 'released', 'AIRWORTHINESS', 'compliance_matrix'
UNION ALL SELECT 'SYS-MFG-INSTALL', 'manufacturing', 'system_installation', 40, '系统装机和联试记录关闭',
       '系统装机、管路/线束连接、功能联试和质量放行记录已补充。', 'accepted', 'MES', 'system_installation_record'
UNION ALL SELECT 'SYS-INSP-FUNCTION', 'inspection', 'functional_test', 52, '系统功能测试通过',
       '完成正常功能、备用/应急功能、告警和维护口数据检查。', 'pass', 'QA', 'functional_test_record'
UNION ALL SELECT 'SYS-SRV-TREND', 'service', 'trend_monitoring', 120, '服役趋势监控复核完成',
       '基于飞行后检查和维修记录补充压力、温度、锁定、磨耗或污染度趋势。', 'closed', 'MRO', 'trend_monitoring_record'
UNION ALL SELECT 'SYS-TS-BASELINE', 'technical_status', 'configuration_baseline', 160, '技术状态基线复核完成',
       '完成构型、软件/硬件版本、偏离状态、放行状态和寿命件适用性复核。', 'released', 'CONFIG', 't1_object_technical_status'
UNION ALL SELECT 'SYS-FAULT-REVIEW', 'fault', 'defect_closure', 190, '系统缺陷闭环复核完成',
       '系统相关监控告警、检查发现和维修关闭状态均已纳入卷宗。', 'closed', 'MRO', 't1_fault_event'
UNION ALL SELECT 'SYS-ATTACH-REVIEW', 'attachment', 'evidence_package', 220, '系统证明附件包补齐',
       '补齐设计说明、接口控制、试验报告、工卡、检查单和放行记录。', 'included', 'DOSSIER', 't1_part_document';

INSERT INTO t1_object_lifecycle_record (
  id, object_profile_id, object_level, object_id, aircraft_id, bom_node_id, part_instance_id,
  lifecycle_stage, business_record_type, business_record_id, business_record_key,
  event_time, event_title, event_summary, result_status, source_system, source_table,
  source_record_id, source_trace_json, attrs_json
)
SELECT concat('refolr', substr(md5(concat(n.id, '|', t.event_key)), 1, 30)),
       n.id, n.object_level, n.id, @aircraft_id, n.id, n.part_instance_id,
       t.lifecycle_stage, t.business_record_type,
       concat('refbiz', substr(md5(concat(n.id, '|', t.event_key)), 1, 30)),
       concat(n.part_number, '/', t.event_key),
       date_add(timestamp('2025-10-01 09:00:00'), interval (t.day_offset + n.sort_seq % 17) day),
       concat(n.part_name, t.title_suffix),
       CASE
         WHEN n.part_number = 'SYS-29' THEN concat('液压系统：', t.summary_suffix, ' 重点包括 3000 psi 级压力监控、油液清洁度、过滤器旁通指示、蓄压器和起落架供压接口。')
         WHEN n.part_number = 'SYS-32' THEN concat('起落架系统：', t.summary_suffix, ' 重点包括收放机构、上/下锁、应急放下、位置指示、减震支柱、刹车防滑和机轮轮胎。')
         ELSE t.summary_suffix
       END,
       t.result_status, t.source_system, t.source_table, n.id,
       json_object('aircraftId', @aircraft_id, 'bomNodeId', n.id, 'partNumber', n.part_number, 'referenceBatch', 'REF_PUBLIC_20260608'),
       json_object('chapterFocus', CASE WHEN n.part_number = 'SYS-29' THEN 'hydraulic_system' ELSE 'landing_gear_system' END,
                   'referenceDriven', true)
  FROM tmp_ref_scope_nodes n
  JOIN tmp_ref_system_templates t
 WHERE n.part_number IN ('SYS-29', 'SYS-32');

INSERT INTO t1_object_lifecycle_record (
  id, object_profile_id, object_level, object_id, aircraft_id, bom_node_id, part_instance_id,
  lifecycle_stage, business_record_type, business_record_id, business_record_key,
  event_time, event_title, event_summary, result_status, source_system, source_table,
  source_record_id, source_trace_json, attrs_json
)
SELECT concat('refolr', substr(md5(concat(n.id, '|', x.event_key)), 1, 30)),
       n.id, n.object_level, n.id, @aircraft_id, n.id, n.part_instance_id,
       x.lifecycle_stage, x.business_record_type,
       concat('refbiz', substr(md5(concat(n.id, '|', x.event_key)), 1, 30)),
       concat(n.part_number, '/', x.event_key),
       date_add(timestamp('2026-02-01 08:00:00'), interval (x.day_offset + n.sort_seq % 41) day),
       concat(n.part_name, x.title_suffix),
       CASE
         WHEN n.root_scope = 'SYS-29' THEN concat('液压对象补充：', x.hyd_summary, ' 对象锚点 ', n.part_number, ' / ', coalesce(n.serial_number, n.position_code, 'NO-SN'), '。')
         WHEN n.root_scope = 'SYS-32' THEN concat('起落架对象补充：', x.lgr_summary, ' 对象锚点 ', n.part_number, ' / ', coalesce(n.serial_number, n.position_code, 'NO-SN'), '。')
         ELSE x.hyd_summary
       END,
       x.result_status, x.source_system, x.source_table, n.id,
       json_object('aircraftId', @aircraft_id, 'bomNodeId', n.id, 'partNumber', n.part_number, 'rootScope', n.root_scope, 'referenceBatch', 'REF_PUBLIC_20260608'),
       json_object('chapterFocus', 'node_full_lifecycle', 'referenceDriven', true, 'ataChapter', n.ata_chapter)
  FROM tmp_ref_scope_nodes n
  JOIN (
    SELECT 'REF-INSP' event_key, 'inspection' lifecycle_stage, 'reference_inspection' business_record_type, 0 day_offset,
           '公开手册口径检查记录补齐' title_suffix,
           '补充油液清洁度、压力保持、过滤器旁通、管路接头渗漏和液压压力指示检查。' hyd_summary,
           '补充减震支柱勤务、收放锁定、轮胎磨耗、刹车磨耗、位置指示和应急放下检查。' lgr_summary,
           'pass' result_status, 'QA' source_system, 't1_inspection_record' source_table
    UNION ALL
    SELECT 'REF-TS', 'technical_status', 'reference_configuration_status', 7,
           '持续适航技术状态补齐',
           '补充构型、寿命件适用性、维护任务适用性和当前放行状态。',
           '补充构型、寿命件适用性、维护任务适用性和当前放行状态。',
           'released', 'CONFIG', 't1_object_technical_status'
  ) x
 WHERE n.root_scope IN ('SYS-29', 'SYS-32')
   AND n.node_type <> 'SYSTEM';

INSERT INTO t1_event_flight_leg (
  id, aircraft_id, leg_number, departure_airport, arrival_airport,
  takeoff_time, landing_time, fh_this_leg, fc_this_leg
)
SELECT concat('reflg', substr(md5(concat('B1234-REF-', seq)), 1, 31)),
       @aircraft_id,
       concat('B1234-REF-', lpad(seq, 3, '0')),
       dep,
       arr,
       date_add(timestamp('2026-03-01 08:00:00'), interval seq * 2 day),
       date_add(timestamp('2026-03-01 10:05:00'), interval seq * 2 day),
       fh,
       1
  FROM (
    SELECT 1 seq, 'ZSPD' dep, 'ZBAA' arr, 2.150 fh UNION ALL
    SELECT 2, 'ZBAA', 'ZSPD', 2.050 UNION ALL
    SELECT 3, 'ZSPD', 'ZGGG', 2.250 UNION ALL
    SELECT 4, 'ZGGG', 'ZSPD', 2.180 UNION ALL
    SELECT 5, 'ZSPD', 'ZUUU', 2.920 UNION ALL
    SELECT 6, 'ZUUU', 'ZSPD', 2.850 UNION ALL
    SELECT 7, 'ZSPD', 'ZLXY', 2.400 UNION ALL
    SELECT 8, 'ZLXY', 'ZSPD', 2.350 UNION ALL
    SELECT 9, 'ZSPD', 'ZSHC', 0.750 UNION ALL
    SELECT 10, 'ZSHC', 'ZSPD', 0.700 UNION ALL
    SELECT 11, 'ZSPD', 'ZGSZ', 2.300 UNION ALL
    SELECT 12, 'ZGSZ', 'ZSPD', 2.220 UNION ALL
    SELECT 13, 'ZSPD', 'ZSAM', 1.700 UNION ALL
    SELECT 14, 'ZSAM', 'ZSPD', 1.660 UNION ALL
    SELECT 15, 'ZSPD', 'ZYTX', 2.620 UNION ALL
    SELECT 16, 'ZYTX', 'ZSPD', 2.580 UNION ALL
    SELECT 17, 'ZSPD', 'ZUCK', 2.720 UNION ALL
    SELECT 18, 'ZUCK', 'ZSPD', 2.650 UNION ALL
    SELECT 19, 'ZSPD', 'ZGHA', 1.920 UNION ALL
    SELECT 20, 'ZGHA', 'ZSPD', 1.880 UNION ALL
    SELECT 21, 'ZSPD', 'ZSNJ', 0.950 UNION ALL
    SELECT 22, 'ZSNJ', 'ZSPD', 0.900 UNION ALL
    SELECT 23, 'ZSPD', 'ZSQD', 1.350 UNION ALL
    SELECT 24, 'ZSQD', 'ZSPD', 1.300
  ) x;

INSERT INTO t1_life_usage_record (
  id, object_profile_id, object_level, object_id, aircraft_id, bom_node_id, part_instance_id,
  usage_source_type, usage_source_id, event_time, fh_delta, fc_delta, landing_delta,
  calendar_days_delta, total_fh_after, total_fc_after, remaining_life_value,
  remaining_life_unit, calculated_by, attrs_json
)
SELECT concat('reflu', substr(md5(id), 1, 31)),
       @aircraft_bom_root_id, 'aircraft', @aircraft_bom_root_id,
       @aircraft_id, @aircraft_bom_root_id, NULL,
       'flight', id, landing_time, fh_this_leg, fc_this_leg, 1, 2,
       round(1539.000 + sum(fh_this_leg) over (order by takeoff_time), 3),
       1283 + sum(fc_this_leg) over (order by takeoff_time),
       NULL, NULL, 'OPS_FLIGHT_LEG_ROLLUP',
       json_object('legNumber', leg_number, 'departure', departure_airport, 'arrival', arrival_airport, 'referenceBatch', 'REF_PUBLIC_20260608')
  FROM t1_event_flight_leg
 WHERE aircraft_id = @aircraft_id
   AND id LIKE 'reflg%';

INSERT INTO t1_inspection_record (
  id, shop_order_task_id, aircraft_id, bom_node_id, part_instance_id, object_level, object_profile_id,
  inspection_type, inspection_std_doc, measurement_values, result, inspector_id, inspection_date, remarks
)
SELECT concat('refir', substr(md5(n.id), 1, 31)),
       concat('task-', substr(md5(n.id), 1, 31)),
       @aircraft_id, n.id, n.part_instance_id, n.object_level, n.id,
       CASE WHEN n.node_type IN ('SYSTEM','SUBSYSTEM') THEN 'FINAL' ELSE 'IN_PROCESS' END,
       CASE WHEN n.root_scope = 'SYS-29' THEN 'FAA-AMT-CH12-HYD-REF / AMM-29-00-00'
            WHEN n.root_scope = 'SYS-32' THEN 'FAA-AMT-CH13-LGR-REF / AMM-32-00-00'
            ELSE 'REF-DOSSIER-INSPECTION' END,
       CASE WHEN n.root_scope = 'SYS-29'
            THEN json_object('pressurePsi', 3000, 'proofPressurePsi', 4500, 'cleanlinessNas', 6, 'leakage', 'none')
            ELSE json_object('downLock', 'locked', 'uplock', 'checked', 'tireWearMm', 2, 'brakeWearPercent', 24) END,
       'PASS',
       CASE WHEN n.root_scope = 'SYS-29' THEN 'HYD-QA-REF' ELSE 'LGR-QA-REF' END,
       date_add(timestamp('2026-05-20 09:00:00'), interval (n.sort_seq % 18) day),
       CASE WHEN n.root_scope = 'SYS-29'
            THEN '公开手册口径补充：液压压力、油液污染度、过滤器、管路/接头和压力指示检查。'
            ELSE '公开手册口径补充：减震支柱、收放锁、刹车、机轮轮胎和位置指示检查。' END
  FROM tmp_ref_scope_nodes n
 WHERE n.root_scope IN ('SYS-29','SYS-32')
   AND (n.node_type IN ('SYSTEM','SUBSYSTEM','EQUIPMENT') OR n.part_number = 'HYD-TUBE-MLG-32A');

INSERT INTO t1_inspection_measurement (
  id, inspection_record_id, aircraft_id, bom_node_id, part_instance_id, object_level, object_profile_id,
  indicator_code, indicator_name, nominal_value, upper_limit, lower_limit, measured_value,
  unit, result_flag, defect_code, defect_level, remark
)
SELECT concat('refim', substr(md5(concat(ir.id, '|PRIMARY')), 1, 31)),
       ir.id, ir.aircraft_id, ir.bom_node_id, ir.part_instance_id, ir.object_level, ir.object_profile_id,
       CASE WHEN abn.part_number = 'HYD-TUBE-MLG-32A' THEN 'HYD-PROOF-PSI'
            WHEN abn.ata_chapter LIKE '29%' THEN 'HYD-SYS-PRESSURE-PSI'
            ELSE 'LGR-LOCK-CHECK' END,
       CASE WHEN abn.part_number = 'HYD-TUBE-MLG-32A' THEN '液压弯管证明压力'
            WHEN abn.ata_chapter LIKE '29%' THEN '液压系统工作压力'
            ELSE '起落架锁定状态' END,
       CASE WHEN abn.part_number = 'HYD-TUBE-MLG-32A' THEN 4500
            WHEN abn.ata_chapter LIKE '29%' THEN 3000
            ELSE 1 END,
       CASE WHEN abn.part_number = 'HYD-TUBE-MLG-32A' THEN 4550
            WHEN abn.ata_chapter LIKE '29%' THEN 3050
            ELSE 1 END,
       CASE WHEN abn.part_number = 'HYD-TUBE-MLG-32A' THEN 4450
            WHEN abn.ata_chapter LIKE '29%' THEN 2950
            ELSE 1 END,
       CASE WHEN abn.part_number = 'HYD-TUBE-MLG-32A' THEN 4500
            WHEN abn.ata_chapter LIKE '29%' THEN 3002
            ELSE 1 END,
       CASE WHEN abn.ata_chapter LIKE '29%' THEN 'psi' ELSE 'status' END,
       'PASS', NULL, NULL,
       '参考 14 CFR/CS-25 液压压力试验要求和 FAA AMT 系统检查主题生成。'
  FROM t1_inspection_record ir
  JOIN t1_aircraft_bom_node abn ON abn.id = ir.bom_node_id
 WHERE ir.id LIKE 'refir%';

INSERT INTO t1_inspection_measurement (
  id, inspection_record_id, aircraft_id, bom_node_id, part_instance_id, object_level, object_profile_id,
  indicator_code, indicator_name, nominal_value, upper_limit, lower_limit, measured_value,
  unit, result_flag, defect_code, defect_level, remark
)
SELECT concat('refim', substr(md5(concat(ir.id, '|SECONDARY')), 1, 31)),
       ir.id, ir.aircraft_id, ir.bom_node_id, ir.part_instance_id, ir.object_level, ir.object_profile_id,
       CASE WHEN abn.ata_chapter LIKE '29%' THEN 'HYD-CLEANLINESS-NAS' ELSE 'LGR-WEAR-MARGIN' END,
       CASE WHEN abn.ata_chapter LIKE '29%' THEN '油液清洁度等级' ELSE '磨耗/间隙剩余裕度' END,
       CASE WHEN abn.ata_chapter LIKE '29%' THEN 6 ELSE 5 END,
       CASE WHEN abn.ata_chapter LIKE '29%' THEN 6 ELSE 8 END,
       CASE WHEN abn.ata_chapter LIKE '29%' THEN 0 ELSE 2 END,
       CASE WHEN abn.ata_chapter LIKE '29%' THEN 6 ELSE 5 END,
       CASE WHEN abn.ata_chapter LIKE '29%' THEN 'NAS' ELSE 'mm' END,
       'PASS', NULL, NULL,
       '参考 FAA AMT 液压污染度、减震支柱、刹车和轮胎检查主题生成。'
  FROM t1_inspection_record ir
  JOIN t1_aircraft_bom_node abn ON abn.id = ir.bom_node_id
 WHERE ir.id LIKE 'refir%';

INSERT INTO t1_object_technical_status (
  id, object_profile_id, object_level, object_id, aircraft_id, bom_node_id, part_instance_id,
  status_code, status_name, baseline_id, baseline_code, bom_version, drawing_revision, process_revision,
  software_version, hardware_version, modification_status, deviation_status, verification_status,
  release_status, effective_from, source_system, source_table, source_record_id, attrs_json
)
SELECT concat('refts', substr(md5(n.id), 1, 31)),
       n.id, n.object_level, n.id, @aircraft_id, n.id, n.part_instance_id,
       concat('REF-TECH-', left(replace(n.part_number, '/', '-'), 45), '-', substr(md5(n.id), 1, 8)),
       concat(n.part_name, ' 公开资料口径技术状态'),
       concat('refbl', substr(md5(n.root_scope), 1, 31)),
       CASE n.root_scope WHEN 'AIRCRAFT' THEN 'REF-BL-B1234-AIRCRAFT-2026-06'
                         WHEN 'SYS-29' THEN 'REF-BL-B1234-HYD-2026-06'
                         ELSE 'REF-BL-B1234-LGR-2026-06' END,
       'BOM-B1234-2026.06.REF',
       CASE WHEN n.node_type = 'PART' THEN 'PART-DWG-REF-C' ELSE 'SYS-SPEC-REF-B' END,
       CASE WHEN n.root_scope = 'SYS-29' THEN 'HYD-AMM-REF-29' WHEN n.root_scope = 'SYS-32' THEN 'LGR-AMM-REF-32' ELSE 'AIRCRAFT-MP-REF' END,
       NULL, NULL, 'incorporated', 'none', 'verified', 'released',
       timestamp('2026-06-08 10:00:00'), 'CONFIG', 't1_object_technical_status', n.id,
       json_object('referenceBatch', 'REF_PUBLIC_20260608', 'adSbStatus', 'reviewed_no_blocking', 'lifeLimitedStatus', 'tracked')
  FROM tmp_ref_scope_nodes n
 WHERE n.root_scope = 'AIRCRAFT'
    OR n.part_number IN ('SYS-29','SYS-32','SUBSYS-HYD-MLG','EQP-HYD-MLG-SUPPLY','HYD-MLG-PKG-01','HYD-TUBE-MLG-32A','CMP-002-01','MLG-ACT-04');

INSERT INTO t1_object_status_history (
  id, object_profile_id, object_level, object_id, aircraft_id, bom_node_id, part_instance_id,
  status_category, old_status, new_status, change_time, change_reason, changed_by,
  source_table, source_record_id, attrs_json
)
SELECT concat('refsh', substr(md5(concat(n.id, '|REF-RELEASE')), 1, 31)),
       n.id, n.object_level, n.id, @aircraft_id, n.id, n.part_instance_id,
       'release', 'reference_pending', 'released',
       date_add(timestamp('2026-06-08 10:10:00'), interval (n.sort_seq % 10) day),
       concat(n.part_name, ' 公开资料口径数据补强完成，当前卷宗可展示全生命周期记录。'),
       'codex',
       'reference_data_enrichment',
       n.id,
       json_object('referenceBatch', 'REF_PUBLIC_20260608')
  FROM tmp_ref_scope_nodes n
 WHERE n.root_scope IN ('AIRCRAFT','SYS-29','SYS-32');

INSERT INTO t1_work_order (
  id, aircraft_id, bom_node_id, part_instance_id, object_level, object_profile_id,
  wo_number, wo_type, wo_status, open_date, close_date, station, mro_org,
  mro_approval_number, wo_open_tsn_fh, wo_open_tsn_fc, release_date, release_by,
  crs_number, remark
)
SELECT concat('refwo', substr(md5(n.id), 1, 31)),
       @aircraft_id, n.id, n.part_instance_id, n.object_level, n.id,
       concat('WO-REF-', left(replace(n.part_number, '/', '-'), 48), '-', substr(md5(n.id), 1, 6)),
       CASE WHEN n.root_scope = 'AIRCRAFT' THEN 'A_CHECK'
            WHEN n.node_type IN ('SYSTEM','SUBSYSTEM') THEN 'C_CHECK'
            WHEN n.part_number = 'HYD-TUBE-MLG-32A' THEN 'UNSCHEDULED'
            ELSE 'LINE' END,
       'CLOSED',
       date_add(timestamp('2026-06-01 08:30:00'), interval (n.sort_seq % 16) day),
       date_add(timestamp('2026-06-01 18:00:00'), interval (n.sort_seq % 16) day),
       CASE n.root_scope WHEN 'SYS-29' THEN 'COMAC-PD' WHEN 'SYS-32' THEN 'ZSPD' ELSE 'PUDONG' END,
       CASE n.root_scope WHEN 'SYS-29' THEN '中国商飞试飞维修中心' WHEN 'SYS-32' THEN '东航技术有限公司' ELSE '总装交付中心' END,
       'MRO-CN-2026-REF',
       round(1580.000 + n.sort_seq * 0.090, 2),
       1320 + (n.sort_seq % 180),
       date_add(timestamp('2026-06-01 19:00:00'), interval (n.sort_seq % 16) day),
       'REF_RELEASE_ENGINEER',
       concat('CRS-REF-', substr(md5(n.id), 1, 10)),
       CASE
         WHEN n.root_scope = 'SYS-29' THEN concat(n.part_name, ' 参考 FAA 液压系统维护主题补充：油液、压力、过滤器、泵/阀、管路接头和泄漏检查关闭。')
         WHEN n.root_scope = 'SYS-32' THEN concat(n.part_name, ' 参考 FAA 起落架维护主题补充：减震支柱、收放锁、刹车、轮胎和应急放下检查关闭。')
         ELSE '整机 A 检：持续适航记录、航段使用、AD/SB、寿命件和缺陷关闭状态复核。'
       END
  FROM tmp_ref_scope_nodes n
 WHERE n.root_scope = 'AIRCRAFT'
    OR n.node_type IN ('SYSTEM','SUBSYSTEM','EQUIPMENT')
    OR n.part_number = 'HYD-TUBE-MLG-32A';

INSERT INTO t1_fault_event (
  id, aircraft_id, bom_node_id, part_instance_id, object_level, object_profile_id,
  node_id, instance_id, reported_at, flight_leg_id, fault_code_id,
  fault_description, severity, fault_source, status, resolution_type, deferral_ref
)
SELECT concat('refflt', substr(md5(n.id), 1, 30)),
       @aircraft_id, n.id, n.part_instance_id, n.object_level, n.id,
       n.id, n.part_instance_id,
       date_add(timestamp('2026-05-28 10:20:00'), interval (n.sort_seq % 20) day),
       NULL, NULL,
       CASE
         WHEN n.part_number = 'HYD-TUBE-MLG-32A' THEN '参考资料补强：主起供压弯管压力脉动与支承垫片复核，复测压力保持、清洁度和外观均合格。'
         WHEN n.root_scope = 'SYS-29' THEN concat(n.part_name, ' 液压压力/污染度趋势触发检查，过滤器旁通未触发，关闭为预防性复核。')
         WHEN n.root_scope = 'SYS-32' THEN concat(n.part_name, ' 起落架收放/锁定/刹车磨耗趋势复核，调整或润滑后关闭。')
         ELSE '整机可靠性月度复核产生的观察项，工程评审关闭。'
       END,
       CASE WHEN n.part_number = 'HYD-TUBE-MLG-32A' THEN 'MAJOR' ELSE 'MINOR' END,
       CASE WHEN n.root_scope = 'AIRCRAFT' THEN 'PILOT_REPORT' WHEN n.node_type = 'PART' THEN 'INSPECTION_FINDING' ELSE 'MONITORING_ALERT' END,
       'RESOLVED',
       CASE WHEN n.part_number = 'HYD-TUBE-MLG-32A' THEN 'ADJUSTMENT'
            WHEN n.root_scope = 'SYS-29' THEN 'NO_FAULT_FOUND'
            ELSE 'ADJUSTMENT' END,
       NULL
  FROM tmp_ref_scope_nodes n
 WHERE n.root_scope = 'AIRCRAFT'
    OR n.part_number IN ('SYS-29','SYS-32','HYD-TUBE-MLG-32A','EQP-HYD-MLG-SUPPLY','MLG-ACT-04')
    OR (n.node_type IN ('EQUIPMENT','COMPONENT','PART') AND conv(substr(md5(concat(n.id,'fault')), 1, 2), 16, 10) % 23 = 0);

INSERT INTO t1_object_interface (
  id, interface_code, interface_name, interface_type,
  source_object_level, source_object_id, source_bom_node_id,
  target_object_level, target_object_id, target_bom_node_id,
  aircraft_id, interface_summary, requirement_summary,
  maturity_status, verification_status, effectivity, attrs_json
)
SELECT concat('refif', substr(md5(x.interface_code), 1, 31)),
       x.interface_code, x.interface_name, x.interface_type,
       x.source_level, x.source_id, x.source_id,
       x.target_level, x.target_id, x.target_id,
       @aircraft_id, x.interface_summary, x.requirement_summary,
       'released', 'verified',
       json_object('tailNumber','B-1234','referenceBatch','REF_PUBLIC_20260608'),
       json_object('sourceBasis', x.source_basis, 'chapterFocus', x.chapter_focus)
  FROM (
    SELECT 'IF-REF-HYD-LGR-SUPPLY' interface_code, '液压系统至起落架收放供压接口' interface_name, 'hydraulic' interface_type,
           'system' source_level, @hyd_system_id source_id, 'system' target_level, @lgr_system_id target_id,
           'SYS-29 向 SYS-32 提供起落架收放、锁定和刹车相关液压能源。' interface_summary,
           '接口应覆盖工作压力、压力保持、回油、过滤、泄漏检查和维护告警。' requirement_summary,
           '14 CFR/CS-25.1435 hydraulic system pressure and safety requirements' source_basis,
           'system_interface' chapter_focus
    UNION ALL
    SELECT 'IF-REF-LGR-POSITION-WARNING', '起落架位置指示与告警接口', 'electrical',
           'system', @lgr_system_id, 'system', @aircraft_bom_root_id,
           '起落架位置、上锁/下锁状态输出至驾驶舱显示和维护信息系统。',
           '接口应支持收放位置指示、未锁定告警和故障维护记录追溯。',
           '14 CFR/CS-25.729 landing gear indication and warning requirements',
           'system_interface'
    UNION ALL
    SELECT 'IF-REF-HYD-TUBE-SUPPORT', '液压弯管支承与结构安装接口', 'mechanical',
           'part', 'f1000006-0006-4006-8006-000000000006', 'component', 'f1000010-0010-4010-8010-000000000010',
           'HYD-TUBE-MLG-32A 通过管夹、支承垫片和组件安装点承受振动与压力脉动。',
           '接口应记录安装扭矩、支承间隙、擦伤复核、压力保持和清洁封堵状态。',
           'FAA AMT hydraulic tubing, leak and servicing inspection topics',
           'key_part_interface'
  ) x;

INSERT INTO t1_part_document (
  id, part_number, doc_type, doc_number, doc_revision, doc_title,
  effective_date, file_path, is_current
)
SELECT concat('refpd', substr(md5(concat(n.id, '|', d.doc_type)), 1, 30)),
       n.part_number,
       d.doc_type,
       concat('DOC-REF-', d.doc_type, '-', left(replace(n.part_number, '/', '-'), 55), '-', substr(md5(n.id), 1, 6)),
       'Ref.D1',
       concat(n.part_name, ' ', d.doc_title),
       date('2026-06-08'),
       concat('/project1/dossier/files/2026/06/B-1234/reference/', n.root_scope, '/', d.doc_type, '/', replace(n.part_number, '/', '-'), '.pdf'),
       1
  FROM tmp_ref_scope_nodes n
  JOIN (
    SELECT 'SPEC' doc_type, '持续适航与构型状态说明' doc_title UNION ALL
    SELECT 'TEST_REPORT', '检验/功能试验记录' UNION ALL
    SELECT 'CMM', '部件维护与修理说明' UNION ALL
    SELECT 'IPC', '装机位置与可更换件清单'
  ) d
 WHERE n.root_scope IN ('AIRCRAFT','SYS-29','SYS-32')
   AND (n.root_scope = 'AIRCRAFT' OR n.node_type IN ('SYSTEM','SUBSYSTEM','EQUIPMENT','COMPONENT','PART'));

SELECT 'dossier_reference_lifecycle_enrichment_mysql8.sql applied' AS result,
       (SELECT template_version FROM t1_dossier_template WHERE id = @template_id) AS template_version,
       (SELECT count(1) FROM tmp_ref_scope_nodes) AS scoped_node_count,
       (SELECT count(1) FROM t1_object_lifecycle_record WHERE id LIKE 'refolr%') AS ref_lifecycle_count,
       (SELECT count(1) FROM t1_event_flight_leg WHERE id LIKE 'reflg%') AS ref_flight_leg_count,
       (SELECT count(1) FROM t1_life_usage_record WHERE id LIKE 'reflu%') AS ref_usage_count,
       (SELECT count(1) FROM t1_inspection_record WHERE id LIKE 'refir%') AS ref_inspection_count,
       (SELECT count(1) FROM t1_inspection_measurement WHERE id LIKE 'refim%') AS ref_measurement_count,
       (SELECT count(1) FROM t1_object_technical_status WHERE id LIKE 'refts%') AS ref_technical_status_count,
       (SELECT count(1) FROM t1_object_status_history WHERE id LIKE 'refsh%') AS ref_status_history_count,
       (SELECT count(1) FROM t1_object_interface WHERE id LIKE 'refif%') AS ref_interface_count,
       (SELECT count(1) FROM t1_work_order WHERE id LIKE 'refwo%') AS ref_work_order_count,
       (SELECT count(1) FROM t1_fault_event WHERE id LIKE 'refflt%') AS ref_fault_count,
       (SELECT count(1) FROM t1_part_document WHERE id LIKE 'refpd%') AS ref_part_document_count;
