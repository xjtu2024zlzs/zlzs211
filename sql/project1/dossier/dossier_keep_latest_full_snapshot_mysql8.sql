-- 保留最新两千节点演示卷宗：
-- 1. 删除旧卷宗实例、版本、生成产物和非目标模板；
-- 2. 仅保留“单台份飞机综合卷宗模板”；
-- 3. 基于 B-1234 当前 2014 个有效 BOM 节点生成一个固化卷宗快照。

SET NAMES utf8mb4;

SET @keep_template_id := 't1000001-0001-4001-8001-000000000111';
SET @aircraft_id := 'b0000001-0001-4001-8001-000000000001';
SET @instance_id := 'fb123401-0001-4001-8001-202606070001';
SET @version_id := 'fb123401-0001-4001-8001-202606070101';
SET @job_id := 'fb123401-0001-4001-8001-202606070201';
SET @snapshot_id := 'fb123401-0001-4001-8001-202606070301';
SET @run_id := 'fb123401-0001-4001-8001-202606070401';

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM t1_qa_message
WHERE session_id IN (
  SELECT id FROM t1_qa_session WHERE dossier_instance_id IS NOT NULL
);
DELETE FROM t1_qa_session WHERE dossier_instance_id IS NOT NULL;
DELETE FROM t1_search_index_record WHERE dossier_instance_id IS NOT NULL OR dossier_version_id IS NOT NULL;
DELETE FROM t1_data_relation_edge WHERE dossier_instance_id IS NOT NULL;
DELETE FROM t1_data_relation_node WHERE dossier_instance_id IS NOT NULL OR dossier_version_id IS NOT NULL;

DELETE FROM t1_dossier_export_file
WHERE export_job_id IN (SELECT id FROM t1_dossier_export_job);
DELETE FROM t1_dossier_export_job;
DELETE FROM t1_dossier_operation_log;
DELETE FROM t1_dossier_completeness_summary;
DELETE FROM t1_dossier_version_diff;
DELETE FROM t1_document_entry;
DELETE FROM t1_dossier_content_item;
DELETE FROM t1_dossier_structure_node;
DELETE FROM t1_generation_job_item;
DELETE FROM t1_generation_job;
DELETE FROM t1_data_inspection_run;
UPDATE t1_dossier_instance SET current_version_id = NULL;
DELETE FROM t1_dossier_version;
DELETE FROM t1_data_snapshot;
DELETE FROM t1_dossier_instance;

DELETE FROM t1_dossier_template_param WHERE template_id <> @keep_template_id;
DELETE FROM t1_dossier_template_rule WHERE template_id <> @keep_template_id;
DELETE FROM t1_dossier_template_data_source WHERE template_id <> @keep_template_id;
DELETE FROM t1_dossier_template_chapter WHERE template_id <> @keep_template_id;
DELETE FROM t1_dossier_template WHERE id <> @keep_template_id;

SET FOREIGN_KEY_CHECKS = 1;

UPDATE t1_dossier_template
   SET template_code = 'TPL-AIRCRAFT-COMPOSITE',
       template_version = 'V1.0',
       name = '单台份飞机综合卷宗模板',
       template_type = 'general',
       applicable_object_type = 'aircraft',
       status = 'active',
       is_default = 1,
       updated_by = 'codex',
       updated_at = sysdate(6)
 WHERE id = @keep_template_id;

DROP TEMPORARY TABLE IF EXISTS tmp_dossier_full_nodes;
CREATE TEMPORARY TABLE tmp_dossier_full_nodes AS
SELECT uuid() AS structure_node_id,
       abn.id AS bom_node_id,
       abn.parent_id AS parent_bom_node_id,
       abn.aircraft_id,
       abn.node_level,
       abn.node_type,
       abn.part_number,
       abn.part_name,
       abn.serial_number,
       abn.position_code,
       abn.ata_chapter,
       abn.manufacturer,
       abn.install_date,
       abn.tsn_fh,
       abn.tsn_fc,
       abn.part_instance_id,
       pop.id AS object_profile_id,
       COALESCE(pop.node_path, abn.part_number) AS node_path,
       CASE
         WHEN abn.part_number = 'HYD-MLG-PKG-01' THEN 'component'
         WHEN abn.node_type = 'AIRCRAFT' OR abn.node_level = 1 THEN 'aircraft'
         WHEN abn.node_type = 'SYSTEM' OR abn.node_level = 2 THEN 'system'
         WHEN abn.node_type IN ('SUBSYSTEM', 'SUB_SYS') THEN 'subsystem'
         WHEN abn.node_type = 'EQUIPMENT' THEN 'equipment'
         WHEN abn.node_type = 'COMPONENT' THEN 'component'
         WHEN abn.node_type IN ('PART', 'CONSUMABLE') THEN 'part'
         WHEN abn.node_level = 3 THEN 'subsystem'
         WHEN abn.node_level = 4 THEN 'equipment'
         WHEN abn.node_level = 5 THEN 'component'
         ELSE 'part'
       END AS object_level,
       CASE
         WHEN abn.part_number = 'AC-B1234' THEN '整机'
         WHEN abn.part_number = 'SYS-29' THEN '系统'
         WHEN abn.part_number = 'SUBSYS-HYD-MLG' THEN '子系统'
         WHEN abn.part_number = 'EQP-HYD-MLG-SUPPLY' THEN '设备'
         WHEN abn.part_number = 'HYD-MLG-PKG-01' THEN '组件'
         WHEN abn.part_number = 'HYD-TUBE-MLG-32A' THEN '液压弯管'
         ELSE concat(abn.node_type, '-', abn.node_level)
       END AS node_label,
       row_number() over (order by abn.node_level, abn.parent_id, abn.position_code, abn.part_number, abn.id) AS sort_seq
FROM t1_aircraft_bom_node abn
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = abn.id
WHERE abn.aircraft_id = @aircraft_id
  AND abn.is_active = 1;

ALTER TABLE tmp_dossier_full_nodes
  ADD PRIMARY KEY (bom_node_id),
  ADD KEY idx_tmp_parent_bom_node_id (parent_bom_node_id),
  ADD KEY idx_tmp_part_number (part_number);

DROP TEMPORARY TABLE IF EXISTS tmp_dossier_parent_nodes;
CREATE TEMPORARY TABLE tmp_dossier_parent_nodes AS
SELECT bom_node_id, structure_node_id
FROM tmp_dossier_full_nodes;

ALTER TABLE tmp_dossier_parent_nodes
  ADD PRIMARY KEY (bom_node_id);

SET @bom_count := (SELECT count(1) FROM tmp_dossier_full_nodes);

INSERT INTO t1_dossier_instance (
  id, instance_code, instance_name, template_id, aircraft_id, current_version_id, current_version_no,
  instance_label, status, instance_options_json, created_by, updated_by, created_at, updated_at
)
SELECT @instance_id,
       'DOS-B1234-FULL-20260607',
       'B-1234 单台份飞机综合卷宗（全量2014节点）',
       @keep_template_id,
       @aircraft_id,
       NULL,
       1,
       concat(pa.tail_number, ' 单台份飞机综合卷宗模板'),
       'ready',
       json_object(
         'aircraftTailNumber', pa.tail_number,
         'modelCode', am.model_code,
         'templateCode', dt.template_code,
         'templateVersion', dt.template_version,
         'executionScope', '按模板全量生成',
         'bomNodeCount', @bom_count,
         'snapshotMode', 'frozen_version'
       ),
       'codex',
       'codex',
       sysdate(6),
       sysdate(6)
FROM t1_physical_aircraft pa
LEFT JOIN t1_ac_model am ON am.id = pa.model_id
JOIN t1_dossier_template dt ON dt.id = @keep_template_id
WHERE pa.id = @aircraft_id;

INSERT INTO t1_data_inspection_run (
  id, dossier_instance_id, dossier_version_id, check_scope_type, run_code, trigger_type,
  run_status, scope_description, rule_set_version, checked_object_count, issue_count,
  initiated_by, summary_json, finished_at
)
VALUES (
  @run_id, @instance_id, @version_id, 'dossier', 'DPC-B1234-FULL-20260607', 'manual',
  'passed', 'B-1234 active BOM nodes + single aircraft dossier template', 'R-DOSSIER-GENERATION-1.0',
  @bom_count + 48, 0, 'codex',
  json_object(
    'warningCount', 0,
    'blockingCount', 0,
    'checkedObjectCount', @bom_count + 48,
    'bomNodeCount', @bom_count,
    'tubeRecordCount', 48,
    'snapshotMode', 'frozen_version'
  ),
  sysdate(6)
);

INSERT INTO t1_data_snapshot (
  id, dossier_instance_id, snapshot_code, snapshot_type, subject_type, subject_id,
  snapshot_data_json, created_by
)
VALUES (
  @snapshot_id, @instance_id, 'SNAP-B1234-FULL-20260607', 'checkpoint', 'dossier', @instance_id,
  json_object(
    'aircraftId', @aircraft_id,
    'templateId', @keep_template_id,
    'dossierInstanceId', @instance_id,
    'dossierVersionId', @version_id,
    'bomNodeCount', @bom_count,
    'tubeKeyPart', 'HYD-TUBE-MLG-32A',
    'snapshotSource', 't1_aircraft_bom_node active full tree',
    'snapshotMode', 'frozen_version',
    'createdAt', date_format(sysdate(6), '%Y-%m-%d %H:%i:%s')
  ),
  'codex'
);

INSERT INTO t1_generation_job (
  id, job_code, job_type, dossier_instance_id, dossier_version_id, precheck_run_id,
  job_status, current_stage, progress_percent, pull_strategy_json, generator_params_json,
  source_system, requested_by, started_at, finished_at, result_summary_json, output_json
)
VALUES (
  @job_id, 'JOB-B1234-FULL-20260607', 'generate', @instance_id, @version_id, @run_id,
  'succeeded', 'finished', 100.00,
  json_object(
    'scope', 'template_data_sources',
    'runtimeSelectable', false,
    'snapshotMode', 'frozen_version',
    'keyNodePriority', json_array('AC-B1234', 'SYS-29', 'SUBSYS-HYD-MLG', 'EQP-HYD-MLG-SUPPLY', 'HYD-MLG-PKG-01', 'HYD-TUBE-MLG-32A')
  ),
  json_object(
    'aircraftId', @aircraft_id,
    'templateId', @keep_template_id,
    'executionScope', '按模板全量生成',
    'snapshotEnabled', true,
    'bomNodeCount', @bom_count
  ),
  'DOSSIER', 'codex', sysdate(6), sysdate(6),
  json_object(
    'status', 'succeeded',
    'versionLabel', 'V1.0',
    'checkSummary', json_object('warningCount', 0, 'blockingCount', 0, 'bomNodeCount', @bom_count),
    'pageCount', 186,
    'fileCount', 36,
    'sourceRecordCount', @bom_count + 182,
    'generatedAt', date_format(sysdate(6), '%Y-%m-%d %H:%i:%s')
  ),
  json_object(
    'fileName', 'B-1234_单台份飞机综合卷宗模板_V1.0.pdf',
    'packageName', 'B-1234_单台份飞机综合卷宗模板_V1.0.zip',
    'outputPath', '/project1/dossier/output/2026/06/B-1234/V1.0/',
    'exportFormat', 'PDF+ZIP',
    'pageCount', 186,
    'fileCount', 36,
    'sourceRecordCount', @bom_count + 182,
    'bomNodeCount', @bom_count,
    'tubeRecordCount', 48
  )
);

INSERT INTO t1_dossier_version (
  id, dossier_instance_id, template_id, template_code, template_version, template_snapshot_json,
  version_no, version_label, major_version_no, minor_version_no, version_level,
  previous_version_id, generation_job_id, data_snapshot_id, version_reason, is_current,
  change_summary, content_summary_json, generation_params_json, created_by, created_at, published_at
)
SELECT @version_id,
       @instance_id,
       dt.id,
       dt.template_code,
       dt.template_version,
       json_object(
         'templateId', dt.id,
         'templateCode', dt.template_code,
         'templateVersion', dt.template_version,
         'templateName', dt.name,
         'chapterCount', (SELECT count(1) FROM t1_dossier_template_chapter WHERE template_id = dt.id),
         'sourceCount', (SELECT count(1) FROM t1_dossier_template_data_source WHERE template_id = dt.id),
         'ruleCount', (SELECT count(1) FROM t1_dossier_template_rule WHERE template_id = dt.id),
         'snapshotAt', date_format(sysdate(6), '%Y-%m-%d %H:%i:%s')
       ),
       1,
       'V1.0',
       1,
       0,
       'major',
       NULL,
       @job_id,
       @snapshot_id,
       'initial',
       1,
       concat('按单台份飞机综合卷宗模板生成，固化 B-1234 当前 ', @bom_count, ' 个有效 BOM 节点。'),
       json_object(
         'status', 'succeeded',
         'pageCount', 186,
         'fileCount', 36,
         'sourceRecordCount', @bom_count + 182,
         'bomNodeCount', @bom_count,
         'tubeRecordCount', 48,
         'checkSummary', json_object('warningCount', 0, 'blockingCount', 0, 'checkedObjectCount', @bom_count + 48)
       ),
       json_object(
         'aircraftId', @aircraft_id,
         'templateId', dt.id,
         'executionScope', '按模板全量生成',
         'versionStrategy', 'initial_full_snapshot',
         'snapshotEnabled', true
       ),
       'codex',
       sysdate(6),
       sysdate(6)
FROM t1_dossier_template dt
WHERE dt.id = @keep_template_id;

UPDATE t1_dossier_instance
   SET current_version_id = @version_id,
       current_version_no = 1,
       status = 'ready',
       updated_by = 'codex',
       updated_at = sysdate(6)
 WHERE id = @instance_id;

INSERT INTO t1_dossier_structure_node (
  id, dossier_instance_id, dossier_version_id, aircraft_id, bom_node_id, parent_bom_node_id,
  bom_node_code, object_level, part_instance_id, parent_id, node_kind, code, name,
  node_path, sort_order, chapter_status, completeness_status, content_count,
  missing_count, required_flag, attrs_json, source_trace_json
)
SELECT n.structure_node_id,
       @instance_id,
       @version_id,
       n.aircraft_id,
       n.bom_node_id,
       n.parent_bom_node_id,
       n.part_number,
       n.object_level,
       n.part_instance_id,
       p.structure_node_id,
       n.object_level,
       n.part_number,
       n.part_name,
       n.node_path,
       n.sort_seq,
       'normal',
       'complete',
       CASE WHEN n.part_number = 'HYD-TUBE-MLG-32A' THEN 6 ELSE 1 END,
       0,
       1,
       json_object(
         'nodeLabel', n.node_label,
         'partNumber', n.part_number,
         'partName', n.part_name,
         'serialNumber', n.serial_number,
         'positionCode', n.position_code,
         'nodeType', n.node_type,
         'nodeLevel', n.node_level,
         'ataChapter', n.ata_chapter,
         'manufacturer', n.manufacturer,
         'installDate', n.install_date,
         'tsnFh', n.tsn_fh,
         'tsnFc', n.tsn_fc,
         'objectProfileId', n.object_profile_id,
         'highlight', n.part_number = 'HYD-TUBE-MLG-32A',
         'demoKeyPath', 'B-1234/SYS-29/SUBSYS-HYD-MLG/EQP-HYD-MLG-SUPPLY/HYD-MLG-PKG-01/HYD-TUBE-MLG-32A'
       ),
       json_object(
         'sourceSystem', 'CONFIG',
         'sourceTable', 't1_aircraft_bom_node',
         'sourceRecordId', n.bom_node_id,
         'sourceRecordKey', n.part_number,
         'snapshotAt', date_format(sysdate(6), '%Y-%m-%d %H:%i:%s')
       )
FROM tmp_dossier_full_nodes n
LEFT JOIN tmp_dossier_parent_nodes p ON p.bom_node_id = n.parent_bom_node_id
ORDER BY n.sort_seq;

INSERT INTO t1_dossier_content_item (
  id, dossier_instance_id, dossier_version_id, structure_node_id, item_code, item_name,
  item_type, lifecycle_stage, aircraft_id, bom_node_id, part_instance_id, is_key_part,
  supply_mode, source_system, source_table, source_record_id, source_record_key,
  include_design_data, include_manufacturing_data, include_service_data, include_source_proof,
  required_flag, included_flag, completeness_status, item_status, sort_order,
  content_summary, file_storage_key, source_trace_json, attrs_json
)
SELECT uuid(),
       @instance_id,
       @version_id,
       n.structure_node_id,
       concat('CONTENT-', lpad(n.sort_seq, 4, '0'), '-', n.part_number),
       concat(n.node_label, '业务摘要-', n.part_name),
       CASE WHEN n.part_number = 'HYD-TUBE-MLG-32A' THEN 'key_part_full_lifecycle' ELSE 'bom_node_summary' END,
       CASE WHEN n.part_number = 'HYD-TUBE-MLG-32A' THEN 'FULL_LIFECYCLE' ELSE 'DOSSIER' END,
       n.aircraft_id,
       n.bom_node_id,
       n.part_instance_id,
       CASE WHEN n.part_number = 'HYD-TUBE-MLG-32A' THEN 1 ELSE 0 END,
       CASE WHEN n.object_level IN ('part', 'component') THEN 'self_made' ELSE 'unknown' END,
       'DOSSIER',
       't1_aircraft_bom_node',
       n.bom_node_id,
       n.part_number,
       CASE WHEN n.part_number = 'HYD-TUBE-MLG-32A' THEN 1 ELSE 0 END,
       CASE WHEN n.part_number = 'HYD-TUBE-MLG-32A' THEN 1 ELSE 0 END,
       CASE WHEN n.part_number = 'HYD-TUBE-MLG-32A' THEN 1 ELSE 0 END,
       1,
       1,
       1,
       'complete',
       'active',
       n.sort_seq,
       CASE
         WHEN n.part_number = 'HYD-TUBE-MLG-32A'
           THEN '液压弯管件号、序列号、设计参数、制造工序、压力试验、终检、装机和服役数据已纳入当前卷宗版本。'
         ELSE concat(n.node_label, '数据已纳入单台份飞机综合卷宗，作为本版本固化快照保存。')
       END,
       CASE WHEN n.part_number = 'HYD-TUBE-MLG-32A' THEN '/project1/dossier/files/2026/06/B-1234/HYD-TUBE-MLG-32A/' ELSE NULL END,
       json_object(
         'sourceSystem', 'CONFIG',
         'sourceTable', 't1_aircraft_bom_node',
         'sourceRecordId', n.bom_node_id,
         'sourceRecordKey', n.part_number,
         'snapshotAt', date_format(sysdate(6), '%Y-%m-%d %H:%i:%s')
       ),
       CASE WHEN n.part_number = 'HYD-TUBE-MLG-32A'
         THEN json_object(
           'identity', json_array('件号 HYD-TUBE-MLG-32A', '序列号 HT-MLG-32A-2026-0042', '安装日期 2026-02-18'),
           'design', json_array('材料 06Cr19Ni10 不锈钢无缝管', '外径 9.53 mm', '壁厚 0.71 mm', '展开长度 485 mm', '最小弯曲半径 28.6 mm', '工作压力 20.7 MPa'),
           'manufacturing', json_array('工单 MO-HYD-TUBE-202602-0042', '炉批 HT-L20260218-A', '工序：下料、去毛刺、数控弯管、端头扩口、钝化、清洁封存'),
           'inspection', json_array('31.5 MPa 保压 5 min 无渗漏', '清洁度 NAS 1638 6 级', '尺寸复验合格'),
           'service', json_array('装机状态 INSTALLED', '无未关闭故障', '最近 A 检复核可用')
         )
         ELSE json_object(
           'identity', json_array(n.part_name, n.part_number),
           'snapshotMode', 'frozen_version',
           'objectLevel', n.object_level,
           'objectProfileId', n.object_profile_id
         )
       END
FROM tmp_dossier_full_nodes n
ORDER BY n.sort_seq;

SET @default_category_id := (SELECT id FROM t1_document_category ORDER BY sort_order, id LIMIT 1);
SET @design_category_id := COALESCE((SELECT id FROM t1_document_category WHERE name = '设计文件' ORDER BY sort_order, id LIMIT 1), @default_category_id);
SET @test_category_id := COALESCE((SELECT id FROM t1_document_category WHERE name = '试验与仿真' ORDER BY sort_order, id LIMIT 1), @default_category_id);
SET @work_card_category_id := COALESCE((SELECT id FROM t1_document_category WHERE name = '维修工卡' ORDER BY sort_order, id LIMIT 1), @default_category_id);

INSERT INTO t1_document_entry (
  id, dossier_instance_id, dossier_version_id, category_id, structure_node_id,
  aircraft_id, bom_node_id, part_instance_id, object_level, object_profile_id,
  doc_no, title, file_storage_key, source_trace_json, source_system, source_table,
  source_record_id, source_record_key, document_status, completeness_status,
  required_flag, included_flag, attrs_json
)
SELECT uuid(),
       @instance_id,
       @version_id,
       CASE d.category_code
         WHEN 'design' THEN @design_category_id
         WHEN 'test' THEN @test_category_id
         WHEN 'work_card' THEN @work_card_category_id
         ELSE @default_category_id
       END,
       n.structure_node_id,
       n.aircraft_id,
       n.bom_node_id,
       n.part_instance_id,
       n.object_level,
       n.object_profile_id,
       d.doc_no,
       d.title,
       concat('/project1/dossier/files/2026/06/B-1234/HYD-TUBE-MLG-32A/', d.file_name),
       json_object(
         'sourceSystem', d.source_system,
         'sourceTable', 't1_document_entry',
         'sourceRecordKey', concat('HYD-TUBE-MLG-32A/', d.file_name),
         'snapshotAt', date_format(sysdate(6), '%Y-%m-%d %H:%i:%s')
       ),
       d.source_system,
       't1_document_entry',
       NULL,
       concat('HYD-TUBE-MLG-32A/', d.file_name),
       'active',
       'complete',
       1,
       1,
       json_object(
         'sortOrder', d.sort_order,
         'keyPart', 'HYD-TUBE-MLG-32A',
         'documentPurpose', d.purpose
       )
FROM tmp_dossier_full_nodes n
JOIN (
  SELECT 1 AS sort_order, 'design' AS category_code, 'COC-HYD-TUBE-MLG-32A-0042' AS doc_no,
         '材料合格证' AS title, 'COC-HYD-TUBE-MLG-32A-0042.pdf' AS file_name, 'DESIGN' AS source_system,
         '材料牌号、炉批、供方复验和放行证明' AS purpose
  UNION ALL
  SELECT 2, 'design', 'MO-HYD-TUBE-202602-0042', '制造随工单', 'MO-HYD-TUBE-202602-0042.pdf', 'MES',
         '下料、去毛刺、数控弯管、扩口、钝化和清洗封存工序记录'
  UNION ALL
  SELECT 3, 'test', 'IR-HYD-TUBE-202602-0042', '终检记录', 'IR-HYD-TUBE-202602-0042.pdf', 'MES',
         '尺寸复验、外观、端口保护和标识齐套检查'
  UNION ALL
  SELECT 4, 'test', 'PTR-HYD-TUBE-202602-0042', '压力试验报告', 'PTR-HYD-TUBE-202602-0042.pdf', 'MES',
         '31.5 MPa 保压 5 min 无渗漏压力试验记录'
  UNION ALL
  SELECT 5, 'work_card', 'IMG-MLG-HYD-STA-20260218', '装机照片', 'IMG-MLG-HYD-STA-20260218.zip', 'MRO',
         '主起轮舱区域装机位置和管夹固定影像'
  UNION ALL
  SELECT 6, 'design', 'DOC-HYD-TUBE-CONFORMITY-0042', '符合性声明', 'DOC-HYD-TUBE-CONFORMITY-0042.pdf', 'DOSSIER',
         '设计、制造、检验、装机证据链符合性归档声明'
) d
WHERE n.part_number = 'HYD-TUBE-MLG-32A'
ORDER BY d.sort_order;

INSERT INTO t1_dossier_operation_log (
  id, dossier_instance_id, dossier_version_id, operation_type, operation_name, operation_status,
  business_subject_type, business_subject_id, operator_id, operator_name, source_ip,
  detail_json, result_message
)
VALUES (
  uuid(), @instance_id, @version_id, 'generate', '卷宗生成', 'succeeded',
  't1_generation_job', @job_id, 'codex', 'codex', '127.0.0.1',
  json_object(
    'aircraftId', @aircraft_id,
    'templateId', @keep_template_id,
    'bomNodeCount', @bom_count,
    'snapshotMode', 'frozen_version'
  ),
  concat('单台份飞机综合卷宗生成完成，已固化 ', @bom_count, ' 个BOM节点；液压弯管 HYD-TUBE-MLG-32A 关键数据已写入。')
);

SELECT 'dossier_keep_latest_full_snapshot_mysql8.sql applied' AS result,
       (SELECT count(1) FROM t1_dossier_template) AS template_count,
       (SELECT count(1) FROM t1_dossier_instance) AS instance_count,
       (SELECT count(1) FROM t1_dossier_version) AS version_count,
       (SELECT count(1) FROM t1_dossier_structure_node WHERE dossier_version_id = @version_id) AS structure_node_count,
       (SELECT count(1) FROM t1_dossier_content_item WHERE dossier_version_id = @version_id) AS content_item_count,
       (SELECT count(1) FROM t1_document_entry WHERE dossier_version_id = @version_id) AS document_count,
       @version_id AS kept_version_id;
