-- Digital dossier instance management seed data - MySQL 8.0
-- Runtime database: ry-cloud

USE `ry-cloud`;

SET @tpl_composite = 't1000001-0001-4001-8001-000000000111';
SET @tpl_c919 = 'd0000001-0001-4001-8001-000000000001';
SET @tpl_arj21 = 'd0000002-0002-4002-8002-000000000002';

UPDATE t1_dossier_instance
SET current_version_id = NULL
WHERE id IN (
  '10000001-0001-4001-8001-000000000001',
  '10000002-0002-4002-8002-000000000002',
  '10000003-0003-4003-8003-000000000003',
  '10000004-0004-4004-8004-000000000004',
  '10000005-0005-4005-8005-000000000005',
  '10000006-0006-4006-8006-000000000006',
  '10000007-0007-4007-8007-000000000007',
  '10000008-0008-4008-8008-000000000008'
);

DELETE FROM t1_dossier_export_file WHERE export_job_id IN (
  '40000001-0001-4001-8001-000000000001',
  '40000002-0002-4002-8002-000000000002',
  '40000003-0003-4003-8003-000000000003',
  '40000004-0004-4004-8004-000000000004',
  '40000007-0007-4007-8007-000000000007',
  '40000008-0008-4008-8008-000000000008'
);
DELETE FROM t1_dossier_export_job WHERE id IN (
  '40000001-0001-4001-8001-000000000001',
  '40000002-0002-4002-8002-000000000002',
  '40000003-0003-4003-8003-000000000003',
  '40000004-0004-4004-8004-000000000004',
  '40000007-0007-4007-8007-000000000007',
  '40000008-0008-4008-8008-000000000008'
);
DELETE FROM t1_dossier_version WHERE id IN (
  '20000001-0001-4001-8001-000000000001',
  '20000002-0002-4002-8002-000000000002',
  '20000003-0003-4003-8003-000000000003',
  '20000004-0004-4004-8004-000000000004',
  '20000007-0007-4007-8007-000000000007',
  '20000008-0008-4008-8008-000000000008'
);
DELETE FROM t1_generation_job WHERE id IN (
  '30000001-0001-4001-8001-000000000001',
  '30000002-0002-4002-8002-000000000002',
  '30000003-0003-4003-8003-000000000003',
  '30000004-0004-4004-8004-000000000004',
  '30000005-0005-4005-8005-000000000005',
  '30000006-0006-4006-8006-000000000006',
  '30000007-0007-4007-8007-000000000007',
  '30000008-0008-4008-8008-000000000008'
);
DELETE FROM t1_dossier_instance WHERE id IN (
  '10000001-0001-4001-8001-000000000001',
  '10000002-0002-4002-8002-000000000002',
  '10000003-0003-4003-8003-000000000003',
  '10000004-0004-4004-8004-000000000004',
  '10000005-0005-4005-8005-000000000005',
  '10000006-0006-4006-8006-000000000006',
  '10000007-0007-4007-8007-000000000007',
  '10000008-0008-4008-8008-000000000008'
);

INSERT INTO t1_dossier_instance (
  id, instance_code, instance_name, template_id, aircraft_id, current_version_no,
  instance_label, status, instance_options_json, created_by, updated_by, created_at, updated_at,
  published_at, archived_at
) VALUES
  ('10000001-0001-4001-8001-000000000001', 'DOS-B1235-20260529', 'B-1235 单台份飞机综合卷宗', @tpl_composite, 'b0000004-0004-4004-8004-000000000004', 1, 'B-1235 单台份飞机综合卷宗模板', 'ready', JSON_OBJECT('objectType','aircraft','scope','single_aircraft','tailNumber','B-1235'), 'system', 'system', '2026-05-29 09:10:00.000000', '2026-05-29 09:36:18.000000', NULL, NULL),
  ('10000002-0002-4002-8002-000000000002', 'DOS-B1236-20260527', 'B-1236 单台份飞机综合卷宗', @tpl_composite, 'b0000005-0005-4005-8005-000000000005', 2, 'B-1236 批产交付后状态卷宗', 'published', JSON_OBJECT('objectType','aircraft','scope','single_aircraft','tailNumber','B-1236'), 'system', 'system', '2026-05-27 10:20:00.000000', '2026-05-27 11:02:44.000000', '2026-05-27 14:30:00.000000', NULL),
  ('10000003-0003-4003-8003-000000000003', 'DOS-B8891-20260524', 'B-8891 单台份飞机综合卷宗', @tpl_arj21, 'b0000002-0002-4002-8002-000000000002', 1, 'B-8891 ARJ21 整机综合卷宗', 'published', JSON_OBJECT('objectType','aircraft','scope','single_aircraft','tailNumber','B-8891'), 'system', 'system', '2026-05-24 08:45:00.000000', '2026-05-24 09:18:25.000000', '2026-05-24 15:00:00.000000', NULL),
  ('10000004-0004-4004-8004-000000000004', 'DOS-B8892-20260522', 'B-8892 单台份飞机综合卷宗', @tpl_arj21, 'b0000006-0006-4006-8006-000000000006', 1, 'B-8892 ARJ21 整机综合卷宗', 'ready', JSON_OBJECT('objectType','aircraft','scope','single_aircraft','tailNumber','B-8892'), 'system', 'system', '2026-05-22 13:00:00.000000', '2026-05-22 13:32:09.000000', NULL, NULL),
  ('10000005-0005-4005-8005-000000000005', 'DOS-B1235-20260531-R1', 'B-1235 单台份飞机综合卷宗重新生成', @tpl_composite, 'b0000004-0004-4004-8004-000000000004', 0, 'B-1235 数据更新重新生成任务', 'building', JSON_OBJECT('objectType','aircraft','scope','single_aircraft','tailNumber','B-1235'), 'system', 'system', '2026-05-31 09:05:00.000000', '2026-05-31 09:12:40.000000', NULL, NULL),
  ('10000006-0006-4006-8006-000000000006', 'DOS-B8892-20260530-R1', 'B-8892 单台份飞机综合卷宗重新生成', @tpl_arj21, 'b0000006-0006-4006-8006-000000000006', 0, 'B-8892 维修记录回写重新生成任务', 'building', JSON_OBJECT('objectType','aircraft','scope','single_aircraft','tailNumber','B-8892'), 'system', 'system', '2026-05-30 16:10:00.000000', '2026-05-30 16:24:12.000000', NULL, NULL),
  ('10000007-0007-4007-8007-000000000007', 'DOS-B1236-20260418', 'B-1236 单台份飞机综合卷宗历史版', @tpl_c919, 'b0000005-0005-4005-8005-000000000005', 1, 'B-1236 交付构型历史卷宗', 'archived', JSON_OBJECT('objectType','aircraft','scope','single_aircraft','tailNumber','B-1236'), 'system', 'system', '2026-04-18 10:00:00.000000', '2026-04-18 10:29:33.000000', '2026-04-18 12:00:00.000000', '2026-05-20 18:00:00.000000'),
  ('10000008-0008-4008-8008-000000000008', 'DOS-B8891-20260509', 'B-8891 单台份飞机综合卷宗小版本', @tpl_arj21, 'b0000002-0002-4002-8002-000000000002', 2, 'B-8891 航线运行数据小版本', 'ready', JSON_OBJECT('objectType','aircraft','scope','single_aircraft','tailNumber','B-8891'), 'system', 'system', '2026-05-09 09:30:00.000000', '2026-05-09 10:04:11.000000', NULL, NULL);

INSERT INTO t1_generation_job (
  id, job_code, job_type, dossier_instance_id, dossier_version_id, job_status, current_stage,
  progress_percent, pull_strategy_json, generator_params_json, source_system, requested_by,
  started_at, finished_at, error_message, result_summary_json, output_json, created_at
) VALUES
  ('30000001-0001-4001-8001-000000000001', 'DGJ-20260529093618-B1235', 'generate', '10000001-0001-4001-8001-000000000001', '20000001-0001-4001-8001-000000000001', 'succeeded', 'finished', 100.00, JSON_OBJECT('mode','template_full','aircraft','B-1235'), JSON_OBJECT('format','PDF+ZIP','watermark','controlled'), 'DOSSIER', 'system', '2026-05-29 09:10:12.000000', '2026-05-29 09:36:18.000000', NULL, JSON_OBJECT('pageCount',174,'fileCount',31,'sourceRecordCount',166,'warningCount',2), JSON_OBJECT('pageCount',174,'fileCount',31,'sourceRecordCount',166,'fileName','B-1235_综合卷宗_V1.0.pdf','packageName','B-1235_综合卷宗_V1.0.zip'), '2026-05-29 09:10:12.000000'),
  ('30000002-0002-4002-8002-000000000002', 'DGJ-20260527110244-B1236', 'generate', '10000002-0002-4002-8002-000000000002', '20000002-0002-4002-8002-000000000002', 'succeeded', 'finished', 100.00, JSON_OBJECT('mode','template_full','aircraft','B-1236'), JSON_OBJECT('format','PDF+ZIP','watermark','controlled'), 'DOSSIER', 'system', '2026-05-27 10:20:14.000000', '2026-05-27 11:02:44.000000', NULL, JSON_OBJECT('pageCount',181,'fileCount',34,'sourceRecordCount',176,'warningCount',1), JSON_OBJECT('pageCount',181,'fileCount',34,'sourceRecordCount',176,'fileName','B-1236_综合卷宗_V1.1.pdf','packageName','B-1236_综合卷宗_V1.1.zip'), '2026-05-27 10:20:14.000000'),
  ('30000003-0003-4003-8003-000000000003', 'DGJ-20260524091825-B8891', 'generate', '10000003-0003-4003-8003-000000000003', '20000003-0003-4003-8003-000000000003', 'succeeded', 'finished', 100.00, JSON_OBJECT('mode','template_full','aircraft','B-8891'), JSON_OBJECT('format','PDF+ZIP','watermark','controlled'), 'DOSSIER', 'system', '2026-05-24 08:45:18.000000', '2026-05-24 09:18:25.000000', NULL, JSON_OBJECT('pageCount',152,'fileCount',27,'sourceRecordCount',139,'warningCount',2), JSON_OBJECT('pageCount',152,'fileCount',27,'sourceRecordCount',139,'fileName','B-8891_综合卷宗_V1.0.pdf','packageName','B-8891_综合卷宗_V1.0.zip'), '2026-05-24 08:45:18.000000'),
  ('30000004-0004-4004-8004-000000000004', 'DGJ-20260522133209-B8892', 'generate', '10000004-0004-4004-8004-000000000004', '20000004-0004-4004-8004-000000000004', 'succeeded', 'finished', 100.00, JSON_OBJECT('mode','template_full','aircraft','B-8892'), JSON_OBJECT('format','PDF+ZIP','watermark','controlled'), 'DOSSIER', 'system', '2026-05-22 13:00:11.000000', '2026-05-22 13:32:09.000000', NULL, JSON_OBJECT('pageCount',149,'fileCount',26,'sourceRecordCount',136,'warningCount',3), JSON_OBJECT('pageCount',149,'fileCount',26,'sourceRecordCount',136,'fileName','B-8892_综合卷宗_V1.0.pdf','packageName','B-8892_综合卷宗_V1.0.zip'), '2026-05-22 13:00:11.000000'),
  ('30000005-0005-4005-8005-000000000005', 'DGJ-20260531091240-B1235-R1', 'regenerate', '10000005-0005-4005-8005-000000000005', NULL, 'running', 'collecting_source_data', 42.00, JSON_OBJECT('mode','template_full','aircraft','B-1235'), JSON_OBJECT('format','PDF+ZIP','watermark','controlled'), 'DOSSIER', 'system', '2026-05-31 09:05:20.000000', NULL, NULL, JSON_OBJECT('pageCount',0,'fileCount',0,'sourceRecordCount',91,'warningCount',0), JSON_OBJECT('pageCount',0,'fileCount',0,'sourceRecordCount',91), '2026-05-31 09:05:20.000000'),
  ('30000006-0006-4006-8006-000000000006', 'DGJ-20260530162412-B8892-R1', 'regenerate', '10000006-0006-4006-8006-000000000006', NULL, 'failed', 'render_pdf', 68.00, JSON_OBJECT('mode','template_full','aircraft','B-8892'), JSON_OBJECT('format','PDF+ZIP','watermark','controlled'), 'DOSSIER', 'system', '2026-05-30 16:10:18.000000', '2026-05-30 16:24:12.000000', '维修工单附件缺少受控文件存储键', JSON_OBJECT('pageCount',0,'fileCount',0,'sourceRecordCount',128,'warningCount',4), JSON_OBJECT('pageCount',0,'fileCount',0,'sourceRecordCount',128), '2026-05-30 16:10:18.000000'),
  ('30000007-0007-4007-8007-000000000007', 'DGJ-20260418102933-B1236', 'generate', '10000007-0007-4007-8007-000000000007', '20000007-0007-4007-8007-000000000007', 'succeeded', 'finished', 100.00, JSON_OBJECT('mode','template_full','aircraft','B-1236'), JSON_OBJECT('format','PDF+ZIP','watermark','controlled'), 'DOSSIER', 'system', '2026-04-18 10:00:15.000000', '2026-04-18 10:29:33.000000', NULL, JSON_OBJECT('pageCount',168,'fileCount',29,'sourceRecordCount',151,'warningCount',2), JSON_OBJECT('pageCount',168,'fileCount',29,'sourceRecordCount',151,'fileName','B-1236_交付构型卷宗_V1.0.pdf','packageName','B-1236_交付构型卷宗_V1.0.zip'), '2026-04-18 10:00:15.000000'),
  ('30000008-0008-4008-8008-000000000008', 'DGJ-20260509100411-B8891', 'generate', '10000008-0008-4008-8008-000000000008', '20000008-0008-4008-8008-000000000008', 'succeeded', 'finished', 100.00, JSON_OBJECT('mode','template_full','aircraft','B-8891'), JSON_OBJECT('format','PDF+ZIP','watermark','controlled'), 'DOSSIER', 'system', '2026-05-09 09:30:20.000000', '2026-05-09 10:04:11.000000', NULL, JSON_OBJECT('pageCount',158,'fileCount',28,'sourceRecordCount',145,'warningCount',1), JSON_OBJECT('pageCount',158,'fileCount',28,'sourceRecordCount',145,'fileName','B-8891_综合卷宗_V1.1.pdf','packageName','B-8891_综合卷宗_V1.1.zip'), '2026-05-09 09:30:20.000000');

INSERT INTO t1_dossier_version (
  id, dossier_instance_id, template_id, template_code, template_version, template_snapshot_json,
  version_no, version_label, major_version_no, minor_version_no, version_level,
  previous_version_id, generation_job_id, data_snapshot_id, version_reason, is_current,
  change_summary, content_summary_json, generation_params_json, created_by, created_at, published_at
) VALUES
  ('20000001-0001-4001-8001-000000000001', '10000001-0001-4001-8001-000000000001', @tpl_composite, 'TPL-AIRCRAFT-COMPOSITE', 'V1.0', JSON_OBJECT('templateName','单台份飞机综合卷宗模板'), 1, 'V1.0', 1, 0, 'major', NULL, '30000001-0001-4001-8001-000000000001', NULL, 'initial', 1, '首次生成 B-1235 单台份飞机综合卷宗。', JSON_OBJECT('pageCount',174,'fileCount',31,'sourceRecordCount',166), JSON_OBJECT('format','PDF+ZIP'), 'system', '2026-05-29 09:36:18.000000', NULL),
  ('20000002-0002-4002-8002-000000000002', '10000002-0002-4002-8002-000000000002', @tpl_composite, 'TPL-AIRCRAFT-COMPOSITE', 'V1.0', JSON_OBJECT('templateName','单台份飞机综合卷宗模板'), 2, 'V1.1', 1, 1, 'minor', NULL, '30000002-0002-4002-8002-000000000002', NULL, 'data_update', 1, '补充批产交付后的终检记录与放行附件。', JSON_OBJECT('pageCount',181,'fileCount',34,'sourceRecordCount',176), JSON_OBJECT('format','PDF+ZIP'), 'system', '2026-05-27 11:02:44.000000', '2026-05-27 14:30:00.000000'),
  ('20000003-0003-4003-8003-000000000003', '10000003-0003-4003-8003-000000000003', @tpl_arj21, 'TPL-D00000020002', 'V1.0', JSON_OBJECT('templateName','ARJ21 · 整机卷宗'), 1, 'V1.0', 1, 0, 'major', NULL, '30000003-0003-4003-8003-000000000003', NULL, 'initial', 1, '首次生成 ARJ21 整机综合卷宗。', JSON_OBJECT('pageCount',152,'fileCount',27,'sourceRecordCount',139), JSON_OBJECT('format','PDF+ZIP'), 'system', '2026-05-24 09:18:25.000000', '2026-05-24 15:00:00.000000'),
  ('20000004-0004-4004-8004-000000000004', '10000004-0004-4004-8004-000000000004', @tpl_arj21, 'TPL-D00000020002', 'V1.0', JSON_OBJECT('templateName','ARJ21 · 整机卷宗'), 1, 'V1.0', 1, 0, 'major', NULL, '30000004-0004-4004-8004-000000000004', NULL, 'initial', 1, '首次生成 B-8892 整机综合卷宗。', JSON_OBJECT('pageCount',149,'fileCount',26,'sourceRecordCount',136), JSON_OBJECT('format','PDF+ZIP'), 'system', '2026-05-22 13:32:09.000000', NULL),
  ('20000007-0007-4007-8007-000000000007', '10000007-0007-4007-8007-000000000007', @tpl_c919, 'TPL-D00000010001', 'V1.0', JSON_OBJECT('templateName','C919 · 整机卷宗（批产全寿命）'), 1, 'V1.0', 1, 0, 'major', NULL, '30000007-0007-4007-8007-000000000007', NULL, 'initial', 1, 'B-1236 交付构型历史卷宗归档。', JSON_OBJECT('pageCount',168,'fileCount',29,'sourceRecordCount',151), JSON_OBJECT('format','PDF+ZIP'), 'system', '2026-04-18 10:29:33.000000', '2026-04-18 12:00:00.000000'),
  ('20000008-0008-4008-8008-000000000008', '10000008-0008-4008-8008-000000000008', @tpl_arj21, 'TPL-D00000020002', 'V1.0', JSON_OBJECT('templateName','ARJ21 · 整机卷宗'), 2, 'V1.1', 1, 1, 'minor', NULL, '30000008-0008-4008-8008-000000000008', NULL, 'data_update', 1, '补充航线运行与例行维修数据形成小版本。', JSON_OBJECT('pageCount',158,'fileCount',28,'sourceRecordCount',145), JSON_OBJECT('format','PDF+ZIP'), 'system', '2026-05-09 10:04:11.000000', NULL);

UPDATE t1_dossier_instance SET current_version_id = '20000001-0001-4001-8001-000000000001' WHERE id = '10000001-0001-4001-8001-000000000001';
UPDATE t1_dossier_instance SET current_version_id = '20000002-0002-4002-8002-000000000002' WHERE id = '10000002-0002-4002-8002-000000000002';
UPDATE t1_dossier_instance SET current_version_id = '20000003-0003-4003-8003-000000000003' WHERE id = '10000003-0003-4003-8003-000000000003';
UPDATE t1_dossier_instance SET current_version_id = '20000004-0004-4004-8004-000000000004' WHERE id = '10000004-0004-4004-8004-000000000004';
UPDATE t1_dossier_instance SET current_version_id = '20000007-0007-4007-8007-000000000007' WHERE id = '10000007-0007-4007-8007-000000000007';
UPDATE t1_dossier_instance SET current_version_id = '20000008-0008-4008-8008-000000000008' WHERE id = '10000008-0008-4008-8008-000000000008';

INSERT INTO t1_dossier_export_job (
  id, dossier_instance_id, dossier_version_id, generation_job_id, export_code, export_status,
  export_scope_json, export_params_json, requested_by, started_at, finished_at, created_at
) VALUES
  ('40000001-0001-4001-8001-000000000001', '10000001-0001-4001-8001-000000000001', '20000001-0001-4001-8001-000000000001', '30000001-0001-4001-8001-000000000001', 'EXP-B1235-20260529', 'succeeded', JSON_OBJECT('tailNumber','B-1235'), JSON_OBJECT('formats', JSON_ARRAY('PDF','ZIP','JSON')), 'system', '2026-05-29 09:36:20.000000', '2026-05-29 09:38:10.000000', '2026-05-29 09:36:20.000000'),
  ('40000002-0002-4002-8002-000000000002', '10000002-0002-4002-8002-000000000002', '20000002-0002-4002-8002-000000000002', '30000002-0002-4002-8002-000000000002', 'EXP-B1236-20260527', 'succeeded', JSON_OBJECT('tailNumber','B-1236'), JSON_OBJECT('formats', JSON_ARRAY('PDF','ZIP','JSON')), 'system', '2026-05-27 11:02:50.000000', '2026-05-27 11:05:20.000000', '2026-05-27 11:02:50.000000'),
  ('40000003-0003-4003-8003-000000000003', '10000003-0003-4003-8003-000000000003', '20000003-0003-4003-8003-000000000003', '30000003-0003-4003-8003-000000000003', 'EXP-B8891-20260524', 'succeeded', JSON_OBJECT('tailNumber','B-8891'), JSON_OBJECT('formats', JSON_ARRAY('PDF','ZIP','JSON')), 'system', '2026-05-24 09:18:28.000000', '2026-05-24 09:20:32.000000', '2026-05-24 09:18:28.000000'),
  ('40000004-0004-4004-8004-000000000004', '10000004-0004-4004-8004-000000000004', '20000004-0004-4004-8004-000000000004', '30000004-0004-4004-8004-000000000004', 'EXP-B8892-20260522', 'succeeded', JSON_OBJECT('tailNumber','B-8892'), JSON_OBJECT('formats', JSON_ARRAY('PDF','ZIP','JSON')), 'system', '2026-05-22 13:32:12.000000', '2026-05-22 13:34:30.000000', '2026-05-22 13:32:12.000000'),
  ('40000007-0007-4007-8007-000000000007', '10000007-0007-4007-8007-000000000007', '20000007-0007-4007-8007-000000000007', '30000007-0007-4007-8007-000000000007', 'EXP-B1236-20260418', 'succeeded', JSON_OBJECT('tailNumber','B-1236'), JSON_OBJECT('formats', JSON_ARRAY('PDF','ZIP','JSON')), 'system', '2026-04-18 10:29:36.000000', '2026-04-18 10:31:10.000000', '2026-04-18 10:29:36.000000'),
  ('40000008-0008-4008-8008-000000000008', '10000008-0008-4008-8008-000000000008', '20000008-0008-4008-8008-000000000008', '30000008-0008-4008-8008-000000000008', 'EXP-B8891-20260509', 'succeeded', JSON_OBJECT('tailNumber','B-8891'), JSON_OBJECT('formats', JSON_ARRAY('PDF','ZIP','JSON')), 'system', '2026-05-09 10:04:15.000000', '2026-05-09 10:06:41.000000', '2026-05-09 10:04:15.000000');

INSERT INTO t1_dossier_export_file (
  id, export_job_id, file_name, file_storage_key, file_format, mime_type,
  file_role, is_primary, file_size, page_count, file_hash, display_order, created_at
) VALUES
  ('50000001-0001-4001-8001-000000000001', '40000001-0001-4001-8001-000000000001', 'B-1235_综合卷宗_V1.0.pdf', '/project1/dossier/B-1235/20260529/B-1235_综合卷宗_V1.0.pdf', 'PDF', 'application/pdf', 'main_pdf', 1, 39845888, 174, 'sha256-b1235-pdf-v10', 1, '2026-05-29 09:38:10.000000'),
  ('60000001-0001-4001-8001-000000000001', '40000001-0001-4001-8001-000000000001', 'B-1235_综合卷宗_V1.0.zip', '/project1/dossier/B-1235/20260529/B-1235_综合卷宗_V1.0.zip', 'ZIP', 'application/zip', 'attachment_zip', 0, 116916224, NULL, 'sha256-b1235-zip-v10', 2, '2026-05-29 09:38:10.000000'),
  ('70000001-0001-4001-8001-000000000001', '40000001-0001-4001-8001-000000000001', 'B-1235_structure_snapshot.json', '/project1/dossier/B-1235/20260529/B-1235_structure_snapshot.json', 'JSON', 'application/json', 'snapshot_json', 0, 2145280, NULL, 'sha256-b1235-json-v10', 3, '2026-05-29 09:38:10.000000'),
  ('50000002-0002-4002-8002-000000000002', '40000002-0002-4002-8002-000000000002', 'B-1236_综合卷宗_V1.1.pdf', '/project1/dossier/B-1236/20260527/B-1236_综合卷宗_V1.1.pdf', 'PDF', 'application/pdf', 'main_pdf', 1, 42729472, 181, 'sha256-b1236-pdf-v11', 1, '2026-05-27 11:05:20.000000'),
  ('60000002-0002-4002-8002-000000000002', '40000002-0002-4002-8002-000000000002', 'B-1236_综合卷宗_V1.1.zip', '/project1/dossier/B-1236/20260527/B-1236_综合卷宗_V1.1.zip', 'ZIP', 'application/zip', 'attachment_zip', 0, 129499136, NULL, 'sha256-b1236-zip-v11', 2, '2026-05-27 11:05:20.000000'),
  ('70000002-0002-4002-8002-000000000002', '40000002-0002-4002-8002-000000000002', 'B-1236_structure_snapshot.json', '/project1/dossier/B-1236/20260527/B-1236_structure_snapshot.json', 'JSON', 'application/json', 'snapshot_json', 0, 2293760, NULL, 'sha256-b1236-json-v11', 3, '2026-05-27 11:05:20.000000'),
  ('50000003-0003-4003-8003-000000000003', '40000003-0003-4003-8003-000000000003', 'B-8891_综合卷宗_V1.0.pdf', '/project1/dossier/B-8891/20260524/B-8891_综合卷宗_V1.0.pdf', 'PDF', 'application/pdf', 'main_pdf', 1, 34537472, 152, 'sha256-b8891-pdf-v10', 1, '2026-05-24 09:20:32.000000'),
  ('60000003-0003-4003-8003-000000000003', '40000003-0003-4003-8003-000000000003', 'B-8891_综合卷宗_V1.0.zip', '/project1/dossier/B-8891/20260524/B-8891_综合卷宗_V1.0.zip', 'ZIP', 'application/zip', 'attachment_zip', 0, 101711872, NULL, 'sha256-b8891-zip-v10', 2, '2026-05-24 09:20:32.000000'),
  ('70000003-0003-4003-8003-000000000003', '40000003-0003-4003-8003-000000000003', 'B-8891_structure_snapshot.json', '/project1/dossier/B-8891/20260524/B-8891_structure_snapshot.json', 'JSON', 'application/json', 'snapshot_json', 0, 1843200, NULL, 'sha256-b8891-json-v10', 3, '2026-05-24 09:20:32.000000'),
  ('50000004-0004-4004-8004-000000000004', '40000004-0004-4004-8004-000000000004', 'B-8892_综合卷宗_V1.0.pdf', '/project1/dossier/B-8892/20260522/B-8892_综合卷宗_V1.0.pdf', 'PDF', 'application/pdf', 'main_pdf', 1, 33685504, 149, 'sha256-b8892-pdf-v10', 1, '2026-05-22 13:34:30.000000'),
  ('60000004-0004-4004-8004-000000000004', '40000004-0004-4004-8004-000000000004', 'B-8892_综合卷宗_V1.0.zip', '/project1/dossier/B-8892/20260522/B-8892_综合卷宗_V1.0.zip', 'ZIP', 'application/zip', 'attachment_zip', 0, 99123200, NULL, 'sha256-b8892-zip-v10', 2, '2026-05-22 13:34:30.000000'),
  ('70000004-0004-4004-8004-000000000004', '40000004-0004-4004-8004-000000000004', 'B-8892_structure_snapshot.json', '/project1/dossier/B-8892/20260522/B-8892_structure_snapshot.json', 'JSON', 'application/json', 'snapshot_json', 0, 1781760, NULL, 'sha256-b8892-json-v10', 3, '2026-05-22 13:34:30.000000'),
  ('50000007-0007-4007-8007-000000000007', '40000007-0007-4007-8007-000000000007', 'B-1236_交付构型卷宗_V1.0.pdf', '/project1/dossier/B-1236/20260418/B-1236_交付构型卷宗_V1.0.pdf', 'PDF', 'application/pdf', 'main_pdf', 1, 38273024, 168, 'sha256-b1236-pdf-v10-archived', 1, '2026-04-18 10:31:10.000000'),
  ('60000007-0007-4007-8007-000000000007', '40000007-0007-4007-8007-000000000007', 'B-1236_交付构型卷宗_V1.0.zip', '/project1/dossier/B-1236/20260418/B-1236_交付构型卷宗_V1.0.zip', 'ZIP', 'application/zip', 'attachment_zip', 0, 111476736, NULL, 'sha256-b1236-zip-v10-archived', 2, '2026-04-18 10:31:10.000000'),
  ('70000007-0007-4007-8007-000000000007', '40000007-0007-4007-8007-000000000007', 'B-1236_structure_snapshot_20260418.json', '/project1/dossier/B-1236/20260418/B-1236_structure_snapshot_20260418.json', 'JSON', 'application/json', 'snapshot_json', 0, 2037760, NULL, 'sha256-b1236-json-v10-archived', 3, '2026-04-18 10:31:10.000000'),
  ('50000008-0008-4008-8008-000000000008', '40000008-0008-4008-8008-000000000008', 'B-8891_综合卷宗_V1.1.pdf', '/project1/dossier/B-8891/20260509/B-8891_综合卷宗_V1.1.pdf', 'PDF', 'application/pdf', 'main_pdf', 1, 36044800, 158, 'sha256-b8891-pdf-v11', 1, '2026-05-09 10:06:41.000000'),
  ('60000008-0008-4008-8008-000000000008', '40000008-0008-4008-8008-000000000008', 'B-8891_综合卷宗_V1.1.zip', '/project1/dossier/B-8891/20260509/B-8891_综合卷宗_V1.1.zip', 'ZIP', 'application/zip', 'attachment_zip', 0, 105906176, NULL, 'sha256-b8891-zip-v11', 2, '2026-05-09 10:06:41.000000'),
  ('70000008-0008-4008-8008-000000000008', '40000008-0008-4008-8008-000000000008', 'B-8891_structure_snapshot_20260509.json', '/project1/dossier/B-8891/20260509/B-8891_structure_snapshot_20260509.json', 'JSON', 'application/json', 'snapshot_json', 0, 1904640, NULL, 'sha256-b8891-json-v11', 3, '2026-05-09 10:06:41.000000');
