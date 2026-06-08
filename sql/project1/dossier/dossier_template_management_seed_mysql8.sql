-- =============================================================================
-- Digital dossier template management seed data - MySQL 8.0
-- Purpose: initialize RuoYi Cloud Vue3 menu and a realistic aircraft composite
-- dossier template. The hydraulic tube in landing gear is the key demo object.
-- =============================================================================

SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- Template version uniqueness: one template code may have multiple versions.
-- -----------------------------------------------------------------------------
USE `ry-cloud`;

DELIMITER $$

DROP PROCEDURE IF EXISTS drop_single_unique_index_if_exists $$
CREATE PROCEDURE drop_single_unique_index_if_exists(
  IN p_table_name varchar(64),
  IN p_column_name varchar(64)
)
BEGIN
  SET @single_unique_index_name = NULL;
  SELECT s.index_name
    INTO @single_unique_index_name
  FROM information_schema.statistics s
  WHERE s.table_schema = DATABASE()
    AND s.table_name = p_table_name
    AND s.non_unique = 0
    AND s.column_name = p_column_name
    AND s.index_name <> 'PRIMARY'
    AND NOT EXISTS (
      SELECT 1
      FROM information_schema.statistics s2
      WHERE s2.table_schema = s.table_schema
        AND s2.table_name = s.table_name
        AND s2.index_name = s.index_name
        AND s2.seq_in_index > 1
    )
  LIMIT 1;

  IF @single_unique_index_name IS NOT NULL THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` DROP INDEX `', @single_unique_index_name, '`');
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    SET @single_unique_index_name = NULL;
  END IF;
END $$

DROP PROCEDURE IF EXISTS add_index_if_missing $$
CREATE PROCEDURE add_index_if_missing(
  IN p_table_name varchar(64),
  IN p_index_name varchar(64),
  IN p_index_definition text
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND index_name = p_index_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ', p_index_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END $$

DELIMITER ;

CALL drop_single_unique_index_if_exists('t1_dossier_template', 'template_code');
CALL add_index_if_missing('t1_dossier_template', 'uk_dossier_template_code_version', 'ADD UNIQUE KEY `uk_dossier_template_code_version` (`template_code`, `template_version`)');

DROP PROCEDURE IF EXISTS drop_single_unique_index_if_exists;
DROP PROCEDURE IF EXISTS add_index_if_missing;

-- -----------------------------------------------------------------------------
-- Dossier module menus.
-- -----------------------------------------------------------------------------
USE `ry-cloud`;

INSERT INTO sys_menu (
  menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
) VALUES
  (2010, '数字卷宗', 0, 1, 'dossier', NULL, NULL, 'Dossier', 1, 0, 'M', '0', '0', NULL, 'documentation', 'system', NOW(), '', NULL, '数字卷宗'),
  (2020, '卷宗生成管理', 2010, 1, 'manage', NULL, NULL, 'DossierManage', 1, 0, 'M', '0', '0', NULL, 'list', 'system', NOW(), '', NULL, '卷宗生成管理'),
  (2030, '卷宗模板管理', 2020, 1, 'template', 'project1/dossier/template/index', NULL, 'DossierTemplate', 1, 0, 'C', '0', '0', 'project1:dossier:template:list', 'table', 'system', NOW(), '', NULL, '卷宗模板管理'),
  (2031, '卷宗生成控制', 2020, 2, 'generation', 'project1/dossier/generation/index', NULL, 'DossierGeneration', 1, 0, 'C', '0', '0', 'project1:dossier:generation:list', 'build', 'system', NOW(), '', NULL, '卷宗生成控制'),
  (2032, '卷宗详情可视化', 2020, 3, 'detail', 'project1/dossier/detail/index', NULL, 'DossierDetail', 1, 0, 'C', '0', '0', 'project1:dossier:detail:list', 'tree-table', 'system', NOW(), '', NULL, '卷宗详情可视化'),
  (2033, '模板查询', 2030, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'project1:dossier:template:query', '#', 'system', NOW(), '', NULL, ''),
  (2034, '模板新增', 2030, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'project1:dossier:template:add', '#', 'system', NOW(), '', NULL, ''),
  (2035, '模板修改', 2030, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'project1:dossier:template:edit', '#', 'system', NOW(), '', NULL, ''),
  (2036, '模板删除', 2030, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'project1:dossier:template:remove', '#', 'system', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  parent_id = VALUES(parent_id),
  order_num = VALUES(order_num),
  path = VALUES(path),
  component = VALUES(component),
  route_name = VALUES(route_name),
  menu_type = VALUES(menu_type),
  visible = VALUES(visible),
  status = VALUES(status),
  perms = VALUES(perms),
  icon = VALUES(icon),
  update_time = NOW(),
  remark = VALUES(remark);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu WHERE menu_id BETWEEN 2010 AND 2036;

-- -----------------------------------------------------------------------------
-- Template data.
-- -----------------------------------------------------------------------------
USE `ry-cloud`;

SET @tpl_v11 = 't1000001-0001-4001-8001-000000000111';
SET @tpl_v10 = 't1000001-0001-4001-8001-000000000110';
SET @tpl_draft = 't1000001-0001-4001-8001-000000000109';

START TRANSACTION;

DELETE FROM t1_dossier_template_param WHERE template_id IN (@tpl_v11, @tpl_v10, @tpl_draft);
DELETE FROM t1_dossier_template_rule WHERE template_id IN (@tpl_v11, @tpl_v10, @tpl_draft);
DELETE FROM t1_dossier_template_data_source WHERE template_id IN (@tpl_v11, @tpl_v10, @tpl_draft);
DELETE FROM t1_dossier_template_chapter WHERE template_id IN (@tpl_v11, @tpl_v10, @tpl_draft);
DELETE FROM t1_dossier_template WHERE id IN (@tpl_v11, @tpl_v10, @tpl_draft);

INSERT INTO t1_dossier_template (
  id, template_code, template_version, name, description, template_type,
  applicable_object_type, chapter_tree_json, validation_rules_json,
  default_generator_params_json, status, is_default, effective_from,
  effective_to, created_by, updated_by
) VALUES
  (
    @tpl_v10, 'TPL-AIRCRAFT-COMPOSITE-LEGACY', 'V1.0', '单台份飞机综合卷宗模板',
    '历史版本：用于 2026 年 4 月前生成的整机综合卷宗，目录和规则已冻结。',
    'general', 'aircraft',
    '{"scopeMode":"aircraft_composite","nodeDrilldown":true,"frozen":true}',
    '{"ruleCount":12}',
    '{"default_export_format":"PDF+ZIP","snapshot_enabled":true}',
    'archived', 0, '2026-04-01 00:00:00', '2026-05-28 23:59:59', 'system', 'system'
  ),
  (
    @tpl_v11, 'TPL-AIRCRAFT-COMPOSITE', 'V1.0', '单台份飞机综合卷宗模板',
    '用于生成单架飞机的综合卷宗。系统、子系统、设备、组件、零件不单独生成卷宗，作为整机卷宗内的下钻目录展示；液压弯管作为关键演示对象。',
    'general', 'aircraft',
    '{"scopeMode":"aircraft_composite","nodeDrilldown":true,"levels":["aircraft","system","subsystem","equipment","component","part"],"focusDemo":["B-1234","HYD-TUBE-MLG-32A","HT-MLG-32A-2026-0042"]}',
    '{"ruleCount":19,"blockerCount":6}',
    '{"keep_empty_chapter":true,"regenerate_strategy":"new_version","snapshot_enabled":true,"missing_data_policy":"block_key_warn_normal","default_export_format":"PDF+ZIP","auto_attach_files":true}',
    'active', 1, '2026-05-29 00:00:00', NULL, 'system', 'system'
  ),
  (
    @tpl_draft, 'TPL-AIRCRAFT-DRAFT', 'V0.2', '整机综合卷宗模板草稿',
    '草稿：用于验证新的展示配置，不参与默认生成。',
    'general', 'aircraft',
    '{"scopeMode":"aircraft_composite","nodeDrilldown":true,"draft":true}',
    '{"ruleCount":0}',
    '{"default_export_format":"PDF"}',
    'draft', 0, NULL, NULL, 'system', 'system'
  );

INSERT INTO t1_dossier_template_chapter (
  id, template_id, parent_id, chapter_code, chapter_name, chapter_level,
  chapter_path, node_kind, sort_order, required_flag, enabled_flag,
  default_expand, completeness_requirement, chapter_desc, attrs_json,
  created_by, updated_by
) VALUES
  ('ta110001-0001-4001-8001-000000000001', @tpl_v11, NULL, 'AIRCRAFT_ROOT', '单台份飞机综合卷宗', 1, '单台份飞机综合卷宗', 'chapter', 1, 1, 1, 1, 'strict', '整机综合卷宗根目录。', '{"objectLevel":"aircraft","displayType":"tree_table","sortMode":"business_order","primaryFields":["tail_number","msn","aircraft_type","status"],"blocks":["summary","relation","details","documents","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta110101-0101-4101-8101-000000000101', @tpl_v11, 'ta110001-0001-4001-8001-000000000001', 'AIRCRAFT_BASIC', '飞机基本信息', 2, '单台份飞机综合卷宗/飞机基本信息', 'chapter', 10, 1, 1, 1, 'strict', '记录飞机身份、型号、序列号、交付状态、当前使用状态。', '{"objectLevel":"aircraft","displayType":"summary_table","sortMode":"business_order","primaryFields":["tail_number","msn","aircraft_type","delivery_status","current_status"],"blocks":["summary","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta110102-0102-4102-8102-000000000102', @tpl_v11, 'ta110001-0001-4001-8001-000000000001', 'AIRCRAFT_BOM', '构型 / BOM', 2, '单台份飞机综合卷宗/构型 / BOM', 'chapter', 20, 1, 1, 1, 'strict', '展示整机下的系统、子系统、设备、组件、零件结构，点击节点切换下钻目录。', '{"objectLevel":"aircraft","displayType":"tree_table","sortMode":"structure_order","primaryFields":["node_type","part_number","part_name","position_code","serial_number"],"blocks":["summary","relation","details","issues"],"drilldown":true,"showMissingTips":true}', 'system', 'system'),
  ('ta110103-0103-4103-8103-000000000103', @tpl_v11, 'ta110001-0001-4001-8001-000000000001', 'AIRCRAFT_DESIGN', '设计数据', 2, '单台份飞机综合卷宗/设计数据', 'chapter', 30, 1, 1, 0, 'strict', '展示整机层面的设计定义、系统划分、构型要求和技术文件。', '{"objectLevel":"aircraft","displayType":"summary_table","sortMode":"business_order","primaryFields":["model","configuration","system","t1_technical_file","design_parameter"],"blocks":["summary","details","documents","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta110104-0104-4104-8104-000000000104', @tpl_v11, 'ta110001-0001-4001-8001-000000000001', 'AIRCRAFT_MANUFACTURING', '制造数据', 2, '单台份飞机综合卷宗/制造数据', 'chapter', 40, 1, 1, 0, 'strict', '展示整机相关制造工单、装配、检验、异常等记录。', '{"objectLevel":"aircraft","displayType":"timeline_files","sortMode":"time_asc","primaryFields":["t1_shop_order","task_code","assembly_time","inspection_result"],"blocks":["summary","timeline","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta110105-0105-4105-8105-000000000105', @tpl_v11, 'ta110001-0001-4001-8001-000000000001', 'AIRCRAFT_SERVICE', '服役数据', 2, '单台份飞机综合卷宗/服役数据', 'chapter', 50, 0, 1, 0, 'normal', '展示飞行、维修、装拆、使用状态等记录。', '{"objectLevel":"aircraft","displayType":"timeline_files","sortMode":"time_desc","primaryFields":["flight_leg","t1_work_order","t1_install_removal","tsn_fh"],"blocks":["summary","timeline","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta110106-0106-4106-8106-000000000106', @tpl_v11, 'ta110001-0001-4001-8001-000000000001', 'AIRCRAFT_FAULT', '故障维修', 2, '单台份飞机综合卷宗/故障维修', 'chapter', 60, 0, 1, 0, 'normal', '展示故障事件、维修工单、处置动作和关闭情况。', '{"objectLevel":"aircraft","displayType":"timeline_files","sortMode":"time_desc","primaryFields":["fault_code","severity","status","t1_work_order","close_date"],"blocks":["summary","timeline","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta110107-0107-4107-8107-000000000107', @tpl_v11, 'ta110001-0001-4001-8001-000000000001', 'AIRCRAFT_TECH_STATUS', '技术状态', 2, '单台份飞机综合卷宗/技术状态', 'chapter', 70, 0, 1, 0, 'normal', '展示构型基线、构型快照、工程更改和状态变化。', '{"objectLevel":"aircraft","displayType":"summary_table","sortMode":"time_desc","primaryFields":["baseline","snapshot","change_order","status"],"blocks":["summary","details","documents","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta110108-0108-4108-8108-000000000108', @tpl_v11, 'ta110001-0001-4001-8001-000000000001', 'AIRCRAFT_ATTACHMENTS', '附件材料', 2, '单台份飞机综合卷宗/附件材料', 'chapter', 80, 0, 1, 0, 'normal', '展示文件、证明、报告、图片和其他材料。', '{"objectLevel":"aircraft","displayType":"file_list","sortMode":"business_order","primaryFields":["doc_no","title","doc_type","file_storage_key"],"blocks":["documents","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta110900-0900-4900-8900-000000000900', @tpl_v11, 'ta110001-0001-4001-8001-000000000001', 'NODE_TEMPLATE_ROOT', '节点目录模板', 2, '单台份飞机综合卷宗/节点目录模板', 'group', 90, 1, 1, 1, 'strict', 'BOM 节点下钻目录模板，仍属于当前整机综合卷宗。', '{"objectLevel":"template_group","displayType":"tree_table","sortMode":"business_order","primaryFields":["object_level","chapter_name"],"blocks":["summary","details"],"showMissingTips":true}', 'system', 'system'),
  ('ta111000-1000-4000-8000-000000001000', @tpl_v11, 'ta110900-0900-4900-8900-000000000900', 'SYSTEM_ROOT', '系统目录', 3, '节点目录模板/系统目录', 'group', 100, 1, 1, 1, 'strict', '系统层关注结构、接口、状态和相关问题。', '{"objectLevel":"system","displayType":"tree_table","sortMode":"business_order","primaryFields":["system_code","system_name","interface","status"],"blocks":["summary","relation","details","documents","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta111001-1001-4001-8001-000000001001', @tpl_v11, 'ta111000-1000-4000-8000-000000001000', 'SYSTEM_OVERVIEW', '系统概况', 4, '系统目录/系统概况', 'chapter', 101, 1, 1, 1, 'strict', '系统身份、功能和当前状态。', '{"objectLevel":"system","displayType":"summary_table","sortMode":"business_order","primaryFields":["code","name","status","ata_chapter"],"blocks":["summary","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta111002-1002-4002-8002-000000001002', @tpl_v11, 'ta111000-1000-4000-8000-000000001000', 'SYSTEM_STRUCTURE', '系统结构', 4, '系统目录/系统结构', 'chapter', 102, 1, 1, 0, 'strict', '系统下级对象和结构关系。', '{"objectLevel":"system","displayType":"tree_table","sortMode":"structure_order","primaryFields":["node_type","part_number","part_name","position_code"],"blocks":["relation","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta111003-1003-4003-8003-000000001003', @tpl_v11, 'ta111000-1000-4000-8000-000000001000', 'SYSTEM_INTERFACE', '接口关系', 4, '系统目录/接口关系', 'chapter', 103, 0, 1, 0, 'normal', '系统与系统、子系统或设备之间的接口关系。', '{"objectLevel":"system","displayType":"summary_table","sortMode":"business_order","primaryFields":["interface_no","source_node","target_node","status"],"blocks":["relation","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta111004-1004-4004-8004-000000001004', @tpl_v11, 'ta111000-1000-4000-8000-000000001000', 'SYSTEM_DESIGN', '系统设计数据', 4, '系统目录/系统设计数据', 'chapter', 104, 1, 1, 0, 'strict', '系统功能、系统组成、接口关系和系统级技术文件。', '{"objectLevel":"system","displayType":"summary_table","sortMode":"business_order","primaryFields":["system_function","t1_technical_file","revision"],"blocks":["summary","details","documents","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta111005-1005-4005-8005-000000001005', @tpl_v11, 'ta111000-1000-4000-8000-000000001000', 'SYSTEM_TECH_STATUS', '技术状态', 4, '系统目录/技术状态', 'chapter', 105, 0, 1, 0, 'normal', '系统技术状态、构型基线和更改影响。', '{"objectLevel":"system","displayType":"summary_table","sortMode":"time_desc","primaryFields":["baseline","change_order","effective_status"],"blocks":["summary","details","documents"],"showMissingTips":true}', 'system', 'system'),
  ('ta111006-1006-4006-8006-000000001006', @tpl_v11, 'ta111000-1000-4000-8000-000000001000', 'SYSTEM_FAULT', '相关故障', 4, '系统目录/相关故障', 'chapter', 106, 0, 1, 0, 'normal', '系统相关故障事件和关闭状态。', '{"objectLevel":"system","displayType":"timeline_files","sortMode":"time_desc","primaryFields":["fault_code","severity","status","reported_at"],"blocks":["summary","timeline","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta111007-1007-4007-8007-000000001007', @tpl_v11, 'ta111000-1000-4000-8000-000000001000', 'SYSTEM_MAINTENANCE', '维修记录', 4, '系统目录/维修记录', 'chapter', 107, 0, 1, 0, 'normal', '系统层维修工单和处置动作。', '{"objectLevel":"system","displayType":"timeline_files","sortMode":"time_desc","primaryFields":["wo_number","wo_status","station","close_date"],"blocks":["timeline","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta111008-1008-4008-8008-000000001008', @tpl_v11, 'ta111000-1000-4000-8000-000000001000', 'SYSTEM_ATTACHMENTS', '附件材料', 4, '系统目录/附件材料', 'chapter', 108, 0, 1, 0, 'normal', '系统级文件和证明附件。', '{"objectLevel":"system","displayType":"file_list","sortMode":"business_order","primaryFields":["doc_no","title","file_storage_key"],"blocks":["documents","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta111500-1500-4500-8500-000000001500', @tpl_v11, 'ta110900-0900-4900-8900-000000000900', 'SUBSYSTEM_ROOT', '子系统目录', 3, '节点目录模板/子系统目录', 'group', 150, 1, 1, 1, 'strict', '子系统层关注功能边界、接口、组成设备和相关问题。', '{"objectLevel":"subsystem","displayType":"tree_table","sortMode":"business_order","primaryFields":["subsystem_code","subsystem_name","interface","status"],"blocks":["summary","relation","details","documents","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta111501-1501-4501-8501-000000001501', @tpl_v11, 'ta111500-1500-4500-8500-000000001500', 'SUBSYSTEM_OVERVIEW', '子系统概况', 4, '子系统目录/子系统概况', 'chapter', 151, 1, 1, 1, 'strict', '子系统身份、功能边界和当前状态。', '{"objectLevel":"subsystem","displayType":"summary_table","sortMode":"business_order","primaryFields":["code","name","status","parent_system"],"blocks":["summary","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta111502-1502-4502-8502-000000001502', @tpl_v11, 'ta111500-1500-4500-8500-000000001500', 'SUBSYSTEM_STRUCTURE', '子系统结构', 4, '子系统目录/子系统结构', 'chapter', 152, 1, 1, 0, 'strict', '子系统下级设备和结构关系。', '{"objectLevel":"subsystem","displayType":"tree_table","sortMode":"structure_order","primaryFields":["node_type","part_number","part_name","position_code"],"blocks":["relation","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta111503-1503-4503-8503-000000001503', @tpl_v11, 'ta111500-1500-4500-8500-000000001500', 'SUBSYSTEM_INTERFACE', '接口关系', 4, '子系统目录/接口关系', 'chapter', 153, 0, 1, 0, 'normal', '子系统与系统、其他子系统或设备之间的接口关系。', '{"objectLevel":"subsystem","displayType":"summary_table","sortMode":"business_order","primaryFields":["interface_no","source_node","target_node","status"],"blocks":["relation","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta111504-1504-4504-8504-000000001504', @tpl_v11, 'ta111500-1500-4500-8500-000000001500', 'SUBSYSTEM_EQUIPMENT', '组成设备', 4, '子系统目录/组成设备', 'chapter', 154, 1, 1, 0, 'strict', '子系统下的设备清单和上下级关系。', '{"objectLevel":"subsystem","displayType":"tree_table","sortMode":"structure_order","primaryFields":["node_type","part_number","part_name","position_code"],"blocks":["relation","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta111505-1505-4505-8505-000000001505', @tpl_v11, 'ta111500-1500-4500-8500-000000001500', 'SUBSYSTEM_DESIGN', '子系统设计数据', 4, '子系统目录/子系统设计数据', 'chapter', 155, 1, 1, 0, 'strict', '子系统功能边界、组成设备、接口关系和子系统级技术文件。', '{"objectLevel":"subsystem","displayType":"summary_table","sortMode":"business_order","primaryFields":["subsystem_function","t1_technical_file","revision"],"blocks":["summary","details","documents","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta111506-1506-4506-8506-000000001506', @tpl_v11, 'ta111500-1500-4500-8500-000000001500', 'SUBSYSTEM_TECH_STATUS', '技术状态', 4, '子系统目录/技术状态', 'chapter', 156, 0, 1, 0, 'normal', '子系统技术状态、构型基线和更改影响。', '{"objectLevel":"subsystem","displayType":"summary_table","sortMode":"time_desc","primaryFields":["baseline","change_order","effective_status"],"blocks":["summary","details","documents"],"showMissingTips":true}', 'system', 'system'),
  ('ta111507-1507-4507-8507-000000001507', @tpl_v11, 'ta111500-1500-4500-8500-000000001500', 'SUBSYSTEM_FAULT', '相关故障', 4, '子系统目录/相关故障', 'chapter', 157, 0, 1, 0, 'normal', '子系统相关故障事件和关闭状态。', '{"objectLevel":"subsystem","displayType":"timeline_files","sortMode":"time_desc","primaryFields":["fault_code","severity","status","reported_at"],"blocks":["summary","timeline","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta111508-1508-4508-8508-000000001508', @tpl_v11, 'ta111500-1500-4500-8500-000000001500', 'SUBSYSTEM_MAINTENANCE', '维修记录', 4, '子系统目录/维修记录', 'chapter', 158, 0, 1, 0, 'normal', '子系统层维修工单和处置动作。', '{"objectLevel":"subsystem","displayType":"timeline_files","sortMode":"time_desc","primaryFields":["wo_number","wo_status","station","close_date"],"blocks":["timeline","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta111509-1509-4509-8509-000000001509', @tpl_v11, 'ta111500-1500-4500-8500-000000001500', 'SUBSYSTEM_ATTACHMENTS', '附件材料', 4, '子系统目录/附件材料', 'chapter', 159, 0, 1, 0, 'normal', '子系统级文件和证明附件。', '{"objectLevel":"subsystem","displayType":"file_list","sortMode":"business_order","primaryFields":["doc_no","title","file_storage_key"],"blocks":["documents","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta112000-2000-4000-8000-000000002000', @tpl_v11, 'ta110900-0900-4900-8900-000000000900', 'EQUIPMENT_ROOT', '设备目录', 3, '节点目录模板/设备目录', 'group', 200, 1, 1, 1, 'strict', '设备层关注组成、设计、制造、装配、装机和维护记录。', '{"objectLevel":"equipment","displayType":"tree_table","sortMode":"business_order","primaryFields":["part_number","part_name","position_code","status"],"blocks":["summary","relation","details","documents","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta112001-2001-4001-8001-000000002001', @tpl_v11, 'ta112000-2000-4000-8000-000000002000', 'EQUIPMENT_BASIC', '设备基本信息', 4, '设备目录/设备基本信息', 'chapter', 201, 1, 1, 1, 'strict', '设备编号、名称、位置和状态。', '{"objectLevel":"equipment","displayType":"summary_table","sortMode":"business_order","primaryFields":["part_number","part_name","position_code","serial_number"],"blocks":["summary","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta112002-2002-4002-8002-000000002002', @tpl_v11, 'ta112000-2000-4000-8000-000000002000', 'EQUIPMENT_STRUCTURE', '组成结构', 4, '设备目录/组成结构', 'chapter', 202, 1, 1, 0, 'strict', '设备下级组件或零件。', '{"objectLevel":"equipment","displayType":"tree_table","sortMode":"structure_order","primaryFields":["node_type","part_number","part_name","position_code"],"blocks":["relation","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta112003-2003-4003-8003-000000002003', @tpl_v11, 'ta112000-2000-4000-8000-000000002000', 'EQUIPMENT_DESIGN', '设计数据', 4, '设备目录/设计数据', 'chapter', 203, 1, 1, 0, 'strict', '设备结构、组成关系、装配要求、设备级技术文件。', '{"objectLevel":"equipment","displayType":"summary_table","sortMode":"business_order","primaryFields":["part_number","material_spec","t1_technical_file","revision"],"blocks":["summary","details","documents","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta112004-2004-4004-8004-000000002004', @tpl_v11, 'ta112000-2000-4000-8000-000000002000', 'EQUIPMENT_MANUFACTURING', '制造数据', 4, '设备目录/制造数据', 'chapter', 204, 1, 1, 0, 'strict', '设备制造工单、工序和异常。', '{"objectLevel":"equipment","displayType":"timeline_files","sortMode":"time_asc","primaryFields":["t1_shop_order","route_code","task_code","status"],"blocks":["summary","timeline","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta112005-2005-4005-8005-000000002005', @tpl_v11, 'ta112000-2000-4000-8000-000000002000', 'EQUIPMENT_ASSEMBLY_RECORD', '装配记录', 4, '设备目录/装配记录', 'chapter', 205, 1, 1, 0, 'strict', '设备装配过程和关键参数。', '{"objectLevel":"equipment","displayType":"timeline_files","sortMode":"time_asc","primaryFields":["assembly_time","assembled_by_id","verified_by_id","assembly_params"],"blocks":["timeline","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta112006-2006-4006-8006-000000002006', @tpl_v11, 'ta112000-2000-4000-8000-000000002000', 'EQUIPMENT_INSPECTION', '检验记录', 4, '设备目录/检验记录', 'chapter', 206, 1, 1, 0, 'strict', '设备检验和试验记录。', '{"objectLevel":"equipment","displayType":"summary_table","sortMode":"time_desc","primaryFields":["inspection_type","inspection_std_doc","result","inspection_date"],"blocks":["summary","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta112007-2007-4007-8007-000000002007', @tpl_v11, 'ta112000-2000-4000-8000-000000002000', 'EQUIPMENT_INSTALL', '装机履历', 4, '设备目录/装机履历', 'chapter', 207, 1, 1, 0, 'strict', '设备装机、拆换和当前装机位置。', '{"objectLevel":"equipment","displayType":"timeline_files","sortMode":"time_desc","primaryFields":["install_date","position_code","tsn_fh","tsn_fc"],"blocks":["summary","timeline","details"],"showMissingTips":true}', 'system', 'system'),
  ('ta112008-2008-4008-8008-000000002008', @tpl_v11, 'ta112000-2000-4000-8000-000000002000', 'EQUIPMENT_FAULT', '故障维修', 4, '设备目录/故障维修', 'chapter', 208, 0, 1, 0, 'normal', '设备故障和维修记录。', '{"objectLevel":"equipment","displayType":"timeline_files","sortMode":"time_desc","primaryFields":["fault_code","wo_number","status","resolution_type"],"blocks":["timeline","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta112009-2009-4009-8009-000000002009', @tpl_v11, 'ta112000-2000-4000-8000-000000002000', 'EQUIPMENT_CHANGE', '技术变更', 4, '设备目录/技术变更', 'chapter', 209, 0, 1, 0, 'normal', '设备级工程更改和状态变化。', '{"objectLevel":"equipment","displayType":"summary_table","sortMode":"time_desc","primaryFields":["change_order","baseline","effective_status"],"blocks":["summary","details","documents"],"showMissingTips":true}', 'system', 'system'),
  ('ta112010-2010-4010-8010-000000002010', @tpl_v11, 'ta112000-2000-4000-8000-000000002000', 'EQUIPMENT_ATTACHMENTS', '附件材料', 4, '设备目录/附件材料', 'chapter', 210, 0, 1, 0, 'normal', '设备相关附件。', '{"objectLevel":"equipment","displayType":"file_list","sortMode":"business_order","primaryFields":["doc_no","title","file_storage_key"],"blocks":["documents","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta113000-3000-4000-8000-000000003000', @tpl_v11, 'ta110900-0900-4900-8900-000000000900', 'COMPONENT_ROOT', '组件目录', 3, '节点目录模板/组件目录', 'group', 300, 1, 1, 1, 'strict', '组件层关注零件组成、制造追溯、检验、装配和使用情况。', '{"objectLevel":"component","displayType":"tree_table","sortMode":"business_order","primaryFields":["part_number","part_name","serial_number","status"],"blocks":["summary","relation","details","documents","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta113001-3001-4001-8001-000000003001', @tpl_v11, 'ta113000-3000-4000-8000-000000003000', 'COMPONENT_BASIC', '组件基本信息', 4, '组件目录/组件基本信息', 'chapter', 301, 1, 1, 1, 'strict', '组件身份和状态。', '{"objectLevel":"component","displayType":"summary_table","sortMode":"business_order","primaryFields":["part_number","part_name","serial_number","status"],"blocks":["summary","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta113002-3002-4002-8002-000000003002', @tpl_v11, 'ta113000-3000-4000-8000-000000003000', 'COMPONENT_PARTS', '组成零件', 4, '组件目录/组成零件', 'chapter', 302, 1, 1, 0, 'strict', '组件包含的零件。', '{"objectLevel":"component","displayType":"tree_table","sortMode":"structure_order","primaryFields":["part_number","part_name","serial_number","is_critical"],"blocks":["relation","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta113003-3003-4003-8003-000000003003', @tpl_v11, 'ta113000-3000-4000-8000-000000003000', 'COMPONENT_DESIGN', '设计数据', 4, '组件目录/设计数据', 'chapter', 303, 1, 1, 0, 'strict', '组件组成、关键参数、制造和装配要求。', '{"objectLevel":"component","displayType":"summary_table","sortMode":"business_order","primaryFields":["part_number","t1_technical_file","design_parameter"],"blocks":["summary","details","documents","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta113004-3004-4004-8004-000000003004', @tpl_v11, 'ta113000-3000-4000-8000-000000003000', 'COMPONENT_TRACE', '制造追溯', 4, '组件目录/制造追溯', 'chapter', 304, 1, 1, 0, 'strict', '组件制造工单、工序和追溯信息。', '{"objectLevel":"component","displayType":"timeline_files","sortMode":"time_asc","primaryFields":["t1_shop_order","task_code","material_lot","operation_record"],"blocks":["summary","timeline","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta113005-3005-4005-8005-000000003005', @tpl_v11, 'ta113000-3000-4000-8000-000000003000', 'COMPONENT_INSPECTION', '检验记录', 4, '组件目录/检验记录', 'chapter', 305, 1, 1, 0, 'strict', '组件检验记录。', '{"objectLevel":"component","displayType":"summary_table","sortMode":"time_desc","primaryFields":["inspection_type","result","inspection_date"],"blocks":["summary","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta113006-3006-4006-8006-000000003006', @tpl_v11, 'ta113000-3000-4000-8000-000000003000', 'COMPONENT_ASSEMBLY_REL', '装配关系', 4, '组件目录/装配关系', 'chapter', 306, 0, 1, 0, 'normal', '组件装配关系。', '{"objectLevel":"component","displayType":"tree_table","sortMode":"structure_order","primaryFields":["parent_node","child_node","position_code"],"blocks":["relation","details"],"showMissingTips":true}', 'system', 'system'),
  ('ta113007-3007-4007-8007-000000003007', @tpl_v11, 'ta113000-3000-4000-8000-000000003000', 'COMPONENT_INSTALL', '装机履历', 4, '组件目录/装机履历', 'chapter', 307, 1, 1, 0, 'strict', '组件装机位置和装拆履历。', '{"objectLevel":"component","displayType":"timeline_files","sortMode":"time_desc","primaryFields":["install_date","position_code","tsn_fh","tsn_fc"],"blocks":["summary","timeline","details"],"showMissingTips":true}', 'system', 'system'),
  ('ta113008-3008-4008-8008-000000003008', @tpl_v11, 'ta113000-3000-4000-8000-000000003000', 'COMPONENT_SERVICE', '服役记录', 4, '组件目录/服役记录', 'chapter', 308, 0, 1, 0, 'normal', '组件服役巡检和维护记录。', '{"objectLevel":"component","displayType":"timeline_files","sortMode":"time_desc","primaryFields":["flight_leg","t1_inspection_record","t1_work_order"],"blocks":["timeline","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta113009-3009-4009-8009-000000003009', @tpl_v11, 'ta113000-3000-4000-8000-000000003000', 'COMPONENT_FAULT', '故障维修', 4, '组件目录/故障维修', 'chapter', 309, 0, 1, 0, 'normal', '组件故障和维修。', '{"objectLevel":"component","displayType":"timeline_files","sortMode":"time_desc","primaryFields":["fault_code","wo_number","status"],"blocks":["timeline","details","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta113010-3010-4010-8010-000000003010', @tpl_v11, 'ta113000-3000-4000-8000-000000003000', 'COMPONENT_CERT', '证明附件', 4, '组件目录/证明附件', 'chapter', 310, 0, 1, 0, 'normal', '组件证明和附件。', '{"objectLevel":"component","displayType":"file_list","sortMode":"business_order","primaryFields":["doc_no","title","file_storage_key"],"blocks":["documents","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta114000-4000-4000-8000-000000004000', @tpl_v11, 'ta110900-0900-4900-8900-000000000900', 'PART_ROOT', '零件目录', 3, '节点目录模板/零件目录', 'group', 400, 1, 1, 1, 'strict', '零件层关注设计定义、制造来源、装机位置、使用履历和证明材料。', '{"objectLevel":"part","displayType":"part_card_param_table","sortMode":"business_order","primaryFields":["part_number","part_name","serial_number","batch_number","is_critical"],"blocks":["summary","relation","params","documents","issues"],"showMissingTips":true}', 'system', 'system'),
  ('ta114001-4001-4001-8001-000000004001', @tpl_v11, 'ta114000-4000-4000-8000-000000004000', 'PART_BASIC', '零件基本信息', 4, '零件目录/零件基本信息', 'chapter', 401, 1, 1, 1, 'strict', '件号、名称、序列号、批次号和关键性。', '{"objectLevel":"part","displayType":"part_card_param_table","sortMode":"business_order","primaryFields":["part_number","part_name","serial_number","batch_number","manufacturer","is_critical"],"blocks":["summary","relation","details","issues"],"demoFocus":"HYD-TUBE-MLG-32A","showMissingTips":true}', 'system', 'system'),
  ('ta114002-4002-4002-8002-000000004002', @tpl_v11, 'ta114000-4000-4000-8000-000000004000', 'PART_DESIGN', '设计数据', 4, '零件目录/设计数据', 'chapter', 402, 1, 1, 0, 'strict', '件号、材料、尺寸、参数、寿命、关键性、技术文件、替代关系。液压弯管展示冲击仿真模型。', '{"objectLevel":"part","displayType":"part_card_param_table","sortMode":"business_order","primaryFields":["part_number","material_spec","outer_diameter_mm","wall_thickness_mm","design_pressure_mpa","proof_pressure_mpa","doc_number"],"blocks":["summary","params","documents","issues"],"demoFocus":"HYD-TUBE-MLG-32A","showMissingTips":true}', 'system', 'system'),
  ('ta114003-4003-4003-8003-000000004003', @tpl_v11, 'ta114000-4000-4000-8000-000000004000', 'PART_TRACE', '制造追溯', 4, '零件目录/制造追溯', 'chapter', 403, 1, 1, 0, 'strict', '制造工单、工序任务、原材料批次、加工记录、检验记录、制造异常和证明文件。', '{"objectLevel":"part","displayType":"timeline_files","sortMode":"time_asc","primaryFields":["t1_shop_order","task_code","material_lot","operation_record","inspection_result"],"blocks":["summary","timeline","details","documents","issues"],"demoFocus":"HT-MLG-32A-2026-0042","showMissingTips":true}', 'system', 'system'),
  ('ta114004-4004-4004-8004-000000004004', @tpl_v11, 'ta114000-4000-4000-8000-000000004000', 'PART_INSPECTION', '检验记录', 4, '零件目录/检验记录', 'chapter', 404, 1, 1, 0, 'strict', '来料复验、终检、气密和尺寸检验。', '{"objectLevel":"part","displayType":"summary_table","sortMode":"time_desc","primaryFields":["inspection_type","inspection_std_doc","measurement_values","result","inspection_date"],"blocks":["summary","details","issues"],"demoFocus":"HYD-LEAK-CHK-32A","showMissingTips":true}', 'system', 'system'),
  ('ta114005-4005-4005-8005-000000004005', @tpl_v11, 'ta114000-4000-4000-8000-000000004000', 'PART_INSTALL', '装机履历', 4, '零件目录/装机履历', 'chapter', 405, 1, 1, 0, 'strict', '装机飞机、装机位置、装拆动作和装机参数。', '{"objectLevel":"part","displayType":"timeline_files","sortMode":"time_desc","primaryFields":["tail_number","position_code","install_date","torque_n_m","leak_check_result"],"blocks":["summary","timeline","details","issues"],"demoFocus":"B-1234","showMissingTips":true}', 'system', 'system'),
  ('ta114006-4006-4006-8006-000000004006', @tpl_v11, 'ta114000-4000-4000-8000-000000004000', 'PART_SERVICE', '服役维修', 4, '零件目录/服役维修', 'chapter', 406, 0, 1, 0, 'normal', '服役巡检、维护工单和装拆履历。', '{"objectLevel":"part","displayType":"timeline_files","sortMode":"time_desc","primaryFields":["tsn_fh","tsn_fc","wo_number","t1_inspection_record","t1_install_removal"],"blocks":["timeline","details","issues"],"demoFocus":"WO-B1234-2026-0329","showMissingTips":true}', 'system', 'system'),
  ('ta114007-4007-4007-8007-000000004007', @tpl_v11, 'ta114000-4000-4000-8000-000000004000', 'PART_FAULT', '故障记录', 4, '零件目录/故障记录', 'chapter', 407, 0, 1, 0, 'normal', '零件关联故障和关闭情况。', '{"objectLevel":"part","displayType":"timeline_files","sortMode":"time_desc","primaryFields":["fault_code","fault_description","severity","status","resolution_type"],"blocks":["summary","timeline","details","issues"],"demoFocus":"32-HYD-PULSE-01","showMissingTips":true}', 'system', 'system'),
  ('ta114008-4008-4008-8008-000000004008', @tpl_v11, 'ta114000-4000-4000-8000-000000004000', 'PART_CERT', '证明附件', 4, '零件目录/证明附件', 'chapter', 408, 0, 1, 0, 'normal', '图纸、材料规范、仿真报告、工卡和证明文件。', '{"objectLevel":"part","displayType":"file_list","sortMode":"business_order","primaryFields":["doc_no","title","doc_type","file_storage_key"],"blocks":["documents","issues"],"demoFocus":"C919-32-1187","showMissingTips":true}', 'system', 'system');

INSERT INTO t1_dossier_template_data_source (
  id, template_id, chapter_id, source_code, source_system, source_table,
  source_name, source_desc, lifecycle_stage, source_record_type,
  join_condition_json, filter_condition_json, apply_object_type,
  supply_mode_scope, key_part_scope, required_flag, enabled_flag, sort_order,
  attrs_json, created_by, updated_by
) VALUES
  ('ds110001-0001-4001-8001-000000000001', @tpl_v11, 'ta110101-0101-4101-8101-000000000101', 'SRC-AIRCRAFT-BASIC', 'PHYSICAL', 't1_physical_aircraft', '飞机身份与状态', '按 aircraft_id 获取 B-1234 等单架飞机身份、型号、序列号和在役状态。', 'DOSSIER', 'aircraft', '{"aircraft_id":"${aircraftId}"}', '{}', 'aircraft', 'all', 'all', 1, 1, 1, '{"sample":"B-1234 / C919 / MSN 67890"}', 'system', 'system'),
  ('ds110002-0002-4002-8002-000000000002', @tpl_v11, 'ta110102-0102-4102-8102-000000000102', 'SRC-AIRCRAFT-BOM', 'CONFIG', 't1_aircraft_bom_node', '装机构型树', '读取整机、系统、子系统、设备、组件、零件层级结构，B-1234 液压系统下的液压弯管为演示重点。', 'DOSSIER', 'bom_node', '{"aircraft_id":"${aircraftId}"}', '{"is_active":1}', 'aircraft', 'all', 'all', 1, 1, 2, '{"rootNode":"AC-B1234","focusPath":"液压系统/起落架液压子系统/主起液压供压设备/液压供压管路组件/主起液压供压弯管"}', 'system', 'system'),
  ('ds110003-0003-4003-8003-000000000003', @tpl_v11, 'ta110103-0103-4103-8103-000000000103', 'SRC-AIRCRAFT-DESIGN-DOC', 'DESIGN', 't1_part_document', '整机与系统技术文件', '按整机构型涉及的件号汇总当前有效设计文件。', 'DESIGN', 'document', '{"part_number":"${bom.part_number}"}', '{"is_current":1}', 'aircraft', 'all', 'all', 1, 1, 3, '{}', 'system', 'system'),
  ('ds110004-0004-4004-8004-000000000004', @tpl_v11, 'ta110104-0104-4104-8104-000000000104', 'SRC-AIRCRAFT-MFG-SHOP', 'MES', 't1_shop_order', '整机相关制造工单', '汇总当前飞机相关制造、装配和测试工单。', 'MANUFACTURING', 't1_shop_order', '{"aircraft_id":"${aircraftId}"}', '{}', 'aircraft', 'all', 'all', 1, 1, 4, '{}', 'system', 'system'),
  ('ds110005-0005-4005-8005-000000000005', @tpl_v11, 'ta110105-0105-4105-8105-000000000105', 'SRC-AIRCRAFT-SERVICE-WO', 'MRO', 't1_work_order', '整机维修工单', '汇总飞机服役阶段维修工单。', 'SERVICE', 't1_work_order', '{"aircraft_id":"${aircraftId}"}', '{}', 'aircraft', 'all', 'all', 0, 1, 5, '{}', 'system', 'system'),
  ('ds110006-0006-4006-8006-000000000006', @tpl_v11, 'ta110106-0106-4106-8106-000000000106', 'SRC-AIRCRAFT-FAULT', 'MRO', 't1_fault_event', '整机关联故障', '读取飞机故障事件、严重度、状态和处置类型。', 'SERVICE', 't1_fault_event', '{"aircraft_id":"${aircraftId}"}', '{}', 'aircraft', 'all', 'all', 0, 1, 6, '{}', 'system', 'system'),
  ('ds110007-0007-4007-8007-000000000007', @tpl_v11, 'ta110108-0108-4108-8108-000000000108', 'SRC-DOSSIER-DOC', 'DOSSIER', 't1_document_entry', '卷宗文档条目', '读取卷宗版本中的文件、证明和报告。', 'DOSSIER', 't1_document_entry', '{"dossier_version_id":"${versionId}"}', '{}', 'aircraft', 'all', 'all', 0, 1, 7, '{}', 'system', 'system'),
  ('ds110010-0010-4010-8010-000000000010', @tpl_v11, 'ta111002-1002-4002-8002-000000001002', 'SRC-SYSTEM-STRUCTURE', 'CONFIG', 't1_aircraft_bom_node', '系统结构节点', '按系统节点展开下级子系统、设备、组件和零件。', 'DOSSIER', 'bom_node', '{"parent_or_self_node_id":"${nodeId}"}', '{"is_active":1}', 'system', 'all', 'all', 1, 1, 10, '{}', 'system', 'system'),
  ('ds110011-0011-4011-8011-000000000011', @tpl_v11, 'ta111006-1006-4006-8006-000000001006', 'SRC-SYSTEM-FAULT', 'MRO', 't1_fault_event', '系统相关故障', '按系统及下级节点聚合故障事件。', 'SERVICE', 't1_fault_event', '{"node_tree_root":"${nodeId}"}', '{}', 'system', 'all', 'all', 0, 1, 11, '{}', 'system', 'system'),
  ('ds110015-0015-4015-8015-000000000015', @tpl_v11, 'ta111504-1504-4504-8504-000000001504', 'SRC-SUBSYSTEM-EQUIPMENT', 'CONFIG', 't1_aircraft_bom_node', '子系统组成设备', '按子系统节点展开下级设备、组件和零件。', 'DOSSIER', 'bom_node', '{"parent_id":"${nodeId}"}', '{"is_active":1}', 'subsystem', 'all', 'all', 1, 1, 15, '{}', 'system', 'system'),
  ('ds110020-0020-4020-8020-000000000020', @tpl_v11, 'ta112002-2002-4002-8002-000000002002', 'SRC-EQUIPMENT-STRUCTURE', 'CONFIG', 't1_aircraft_bom_node', '设备组成结构', '按设备节点展开组件或零件组成。', 'DOSSIER', 'bom_node', '{"parent_id":"${nodeId}"}', '{"is_active":1}', 'equipment', 'all', 'all', 1, 1, 20, '{}', 'system', 'system'),
  ('ds110021-0021-4021-8021-000000000021', @tpl_v11, 'ta112004-2004-4004-8004-000000002004', 'SRC-EQUIPMENT-MFG', 'MES', 't1_shop_order', '设备制造工单', '读取设备制造和装配工单。', 'MANUFACTURING', 't1_shop_order', '{"part_number":"${node.part_number}"}', '{}', 'equipment', 'all', 'all', 1, 1, 21, '{}', 'system', 'system'),
  ('ds110030-0030-4030-8030-000000000030', @tpl_v11, 'ta113004-3004-4004-8004-000000003004', 'SRC-COMPONENT-TRACE', 'MES', 't1_shop_order_task', '组件制造任务', '组件层制造任务、工序和设备人员。', 'MANUFACTURING', 't1_shop_order_task', '{"shop_order_id":"${shopOrderId}"}', '{}', 'component', 'all', 'all', 1, 1, 30, '{}', 'system', 'system'),
  ('ds110031-0031-4031-8031-000000000031', @tpl_v11, 'ta113005-3005-4005-8005-000000003005', 'SRC-COMPONENT-INSPECTION', 'MES', 't1_inspection_record', '组件检验记录', '组件终检、过程检验和试验结果。', 'MANUFACTURING', 't1_inspection_record', '{"shop_order_task_id":"${taskId}"}', '{}', 'component', 'all', 'all', 1, 1, 31, '{}', 'system', 'system'),
  ('ds110040-0040-4040-8040-000000000040', @tpl_v11, 'ta114001-4001-4001-8001-000000004001', 'SRC-PART-INSTANCE-HYD', 'PHYSICAL', 't1_part_instance', '液压弯管实物实例', '重点演示件：HYD-TUBE-MLG-32A，S/N HT-MLG-32A-2026-0042，炉批 LOT-TB3-2025-Q4-18。', 'DOSSIER', 't1_part_instance', '{"part_instance_id":"${partInstanceId}"}', '{"part_number":"HYD-TUBE-MLG-32A","serial_number":"HT-MLG-32A-2026-0042"}', 'part', 'self_made', 'key_only', 1, 1, 40, '{"demoFocus":true,"tailNumber":"B-1234"}', 'system', 'system'),
  ('ds110041-0041-4041-8041-000000000041', @tpl_v11, 'ta114002-4002-4002-8002-000000004002', 'SRC-PART-DESIGN-MASTER-HYD', 'DESIGN', 't1_part_master', '液压弯管件号主数据', '材料 TB3 Ti-3Al-2.5V、设计压力 28 MPa、证明压力 42 MPa、工作液 MIL-PRF-83282。', 'DESIGN', 't1_part_master', '{"part_number":"${node.part_number}"}', '{"part_number":"HYD-TUBE-MLG-32A"}', 'part', 'self_made', 'key_only', 1, 1, 41, '{"primaryFields":["material_spec","design_pressure_mpa","proof_pressure_mpa","working_fluid"]}', 'system', 'system'),
  ('ds110042-0042-4042-8042-000000000042', @tpl_v11, 'ta114002-4002-4002-8002-000000004002', 'SRC-PART-DESIGN-PARAM-HYD', 'DESIGN', 't1_part_parameter_value', '液压弯管设计参数', '外径 12.7 mm、壁厚 1.02 mm、弯曲半径 38 mm、直管总长 267 mm、屈服强度 620 MPa。', 'DESIGN', 't1_part_parameter_value', '{"part_number":"${node.part_number}"}', '{"part_number":"HYD-TUBE-MLG-32A"}', 'part', 'self_made', 'key_only', 1, 1, 42, '{"displayType":"parameter_table"}', 'system', 'system'),
  ('ds110043-0043-4043-8043-000000000043', @tpl_v11, 'ta114002-4002-4002-8002-000000004002', 'SRC-PART-IMPACT-MODEL-HYD', 'DESIGN', 't1_part_hydraulic_tube_impact_model', '液压冲击仿真模型', '一维水锤波传播示意模型，评估 35 ms 关阀瞬态、卡箍预紧与峰值压力。', 'DESIGN', 'impact_model', '{"part_number":"${node.part_number}"}', '{"part_number":"HYD-TUBE-MLG-32A"}', 'part', 'self_made', 'key_only', 1, 1, 43, '{"linkedTables":["t1_impact_tube_segment","t1_impact_tube_support","t1_impact_tube_fluid","t1_impact_tube_boundary_condition"]}', 'system', 'system'),
  ('ds110044-0044-4044-8044-000000000044', @tpl_v11, 'ta114002-4002-4002-8002-000000004002', 'SRC-PART-IMPACT-SEGMENT-HYD', 'DESIGN', 't1_impact_tube_segment', '液压弯管几何段', '5 段管路：85 mm 直段、45°弯、120 mm 直段、30°弯、62 mm 直段。', 'DESIGN', 'impact_segment', '{"model_part_number":"${node.part_number}"}', '{"model_part_number":"HYD-TUBE-MLG-32A"}', 'part', 'self_made', 'key_only', 1, 1, 44, '{}', 'system', 'system'),
  ('ds110045-0045-4045-8045-000000000045', @tpl_v11, 'ta114002-4002-4002-8002-000000004002', 'SRC-PART-DOCUMENT-HYD', 'DESIGN', 't1_part_document', '液压弯管技术文件', '工程图 C919-32-1187 Rev.C、材料规范 AMS-4944-TB3、冲击仿真 HYD-IMP-2025-0412。', 'DESIGN', 't1_part_document', '{"part_number":"${node.part_number}"}', '{"part_number":"HYD-TUBE-MLG-32A","is_current":1}', 'part', 'self_made', 'key_only', 1, 1, 45, '{}', 'system', 'system'),
  ('ds110046-0046-4046-8046-000000000046', @tpl_v11, 'ta114003-4003-4003-8003-000000004003', 'SRC-PART-SHOP-ORDER-HYD', 'MES', 't1_shop_order', '液压弯管制造工单', 'SO-HYD-2026-0042，2026-01-10 开始，2026-01-22 关闭，产出 1 件。', 'MANUFACTURING', 't1_shop_order', '{"produced_by_shop_order_id":"${instance.produced_by_shop_order_id}"}', '{"part_number":"HYD-TUBE-MLG-32A","status":"CLOSED"}', 'part', 'self_made', 'key_only', 1, 1, 46, '{}', 'system', 'system'),
  ('ds110047-0047-4047-8047-000000000047', @tpl_v11, 'ta114003-4003-4003-8003-000000004003', 'SRC-PART-ROUTE-HYD', 'MES', 't1_process_route', '液压弯管工艺路线', 'PR-HYD-TUBE-32A Rev.B，OP10 来料复验、OP20 数控弯管、OP30 去应力、OP40 无损检测、OP50 气密标识。', 'MANUFACTURING', 't1_process_route', '{"route_code":"${shopOrder.route_code}"}', '{"part_number":"HYD-TUBE-MLG-32A","default_flag":1}', 'part', 'self_made', 'key_only', 1, 1, 47, '{}', 'system', 'system'),
  ('ds110048-0048-4048-8048-000000000048', @tpl_v11, 'ta114003-4003-4003-8003-000000004003', 'SRC-PART-MATERIAL-HYD', 'MES', 't1_material_lot_trace', '液压弯管原材料炉批', 'TB3-TUBE-12.7x1.02，HEAT-25Q4-8841，MILL-2025-8841-A。', 'MANUFACTURING', 'material_trace', '{"instance_id":"${partInstanceId}"}', '{"material_pn":"TB3-TUBE-12.7x1.02"}', 'part', 'self_made', 'key_only', 1, 1, 48, '{}', 'system', 'system'),
  ('ds110049-0049-4049-8049-000000000049', @tpl_v11, 'ta114003-4003-4003-8003-000000004003', 'SRC-PART-OPERATION-HYD', 'MES', 't1_production_operation_record', '液压弯管加工记录', '数控弯管机 CNC-BEND-03，45.02°/29.98° 成形，回弹补偿 +0.5°。', 'MANUFACTURING', 'operation_record', '{"shop_order_task_id":"${taskId}"}', '{}', 'part', 'self_made', 'key_only', 1, 1, 49, '{}', 'system', 'system'),
  ('ds110050-0050-4050-8050-000000000050', @tpl_v11, 'ta114004-4004-4004-8004-000000004004', 'SRC-PART-INSPECTION-HYD', 'MES', 't1_inspection_record', '液压弯管终检与气密', 'HYD-LEAK-CHK-32A，21 MPa/5 min 气密 PASS，泄漏率 0 ml/min。', 'MANUFACTURING', 't1_inspection_record', '{"shop_order_task_id":"${produced_by_shop_task_id}"}', '{"result":"PASS","inspection_std_doc":"HYD-LEAK-CHK-32A"}', 'part', 'self_made', 'key_only', 1, 1, 50, '{}', 'system', 'system'),
  ('ds110051-0051-4051-8051-000000000051', @tpl_v11, 'ta114005-4005-4005-8005-000000004005', 'SRC-PART-INSTALL-HYD', 'CONFIG', 't1_aircraft_bom_node', '液压弯管装机节点', 'B-1234 主起舱左侧供压管路 A 路由，装机日期 2026-02-18。', 'DOSSIER', 'bom_node', '{"node_id":"${nodeId}"}', '{"part_number":"HYD-TUBE-MLG-32A","aircraft_id":"b0000001-0001-4001-8001-000000000001"}', 'part', 'self_made', 'key_only', 1, 1, 51, '{}', 'system', 'system'),
  ('ds110052-0052-4052-8052-000000000052', @tpl_v11, 'ta114005-4005-4005-8005-000000004005', 'SRC-PART-ASSEMBLY-HYD', 'MES', 't1_assembly_record', '液压弯管装配记录', '2026-02-18 10:20，张工装配、李检复核，24 N·m，21 MPa 装后试压 PASS。', 'MANUFACTURING', 't1_assembly_record', '{"node_id":"${nodeId}","instance_id":"${partInstanceId}"}', '{}', 'part', 'self_made', 'key_only', 1, 1, 52, '{}', 'system', 'system'),
  ('ds110053-0053-4053-8053-000000000053', @tpl_v11, 'ta114006-4006-4006-8006-000000004006', 'SRC-PART-IR-HYD', 'MRO', 't1_install_removal', '液压弯管装拆履历', '2026-03-29 拆下送检并装回翻修件，关联工卡 WC-32-1187-029。', 'SERVICE', 't1_install_removal', '{"node_id":"${nodeId}","instance_id":"${partInstanceId}"}', '{}', 'part', 'self_made', 'key_only', 0, 1, 53, '{}', 'system', 'system'),
  ('ds110054-0054-4054-8054-000000000054', @tpl_v11, 'ta114007-4007-4007-8007-000000004007', 'SRC-PART-FAULT-HYD', 'MRO', 't1_fault_event', '液压弯管故障事件', '32-HYD-PULSE-01，主起舱液压异响，压力脉动 0.8 MPa pp，已关闭。', 'SERVICE', 't1_fault_event', '{"node_id":"${nodeId}","instance_id":"${partInstanceId}"}', '{"status":"RESOLVED"}', 'part', 'self_made', 'key_only', 0, 1, 54, '{}', 'system', 'system'),
  ('ds110055-0055-4055-8055-000000000055', @tpl_v11, 'ta114006-4006-4006-8006-000000004006', 'SRC-PART-WO-HYD', 'MRO', 't1_work_order', '液压弯管维修工单', 'WO-B1234-2026-0329，ZSPD 航线维修，2026-03-29 18:30 关闭。', 'SERVICE', 't1_work_order', '{"work_order_id":"${installRemoval.work_order_id}"}', '{"wo_status":"CLOSED"}', 'part', 'self_made', 'key_only', 0, 1, 55, '{}', 'system', 'system'),
  ('ds110056-0056-4056-8056-000000000056', @tpl_v11, 'ta114008-4008-4008-8008-000000004008', 'SRC-PART-DOSSIER-DOC-HYD', 'DOSSIER', 't1_document_entry', '液压弯管卷宗附件', 'C919-32-1187 Rev.C、HYD-IMP-2025-0412、WC-32-1187-029 已挂接到卷宗版本。', 'DOSSIER', 't1_document_entry', '{"structure_node_id":"${structureNodeId}"}', '{}', 'part', 'self_made', 'key_only', 0, 1, 56, '{}', 'system', 'system');

INSERT INTO t1_dossier_template_rule (
  id, template_id, chapter_id, rule_code, rule_name, rule_type,
  target_table, target_field, target_path, rule_expression, rule_expression_json,
  severity, error_message, remediation_hint, bind_quality_rule_id,
  apply_object_type, supply_mode_scope, key_part_scope, required_flag,
  enabled_flag, sort_order, created_by, updated_by
) VALUES
  ('ru110001-0001-4001-8001-000000000001', @tpl_v11, 'ta110101-0101-4101-8101-000000000101', 'RULE-AIRCRAFT-CODE-REQUIRED', '飞机编号检查', 'required', 't1_physical_aircraft', 'tail_number', '$.aircraft.tail_number', 'tail_number is not null and aircraft_type is not null', '{"fields":["tail_number","aircraft_type","msn"]}', 'blocker', '飞机编号、型号、序列号不能为空。', '补齐 t1_physical_aircraft 中 tail_number、aircraft_type、msn。', NULL, 'aircraft', 'all', 'all', 1, 1, 1, 'system', 'system'),
  ('ru110002-0002-4002-8002-000000000002', @tpl_v11, 'ta110102-0102-4102-8102-000000000102', 'RULE-AIRCRAFT-BOM-ACTIVE', 'BOM 有效性检查', 'relation', 't1_aircraft_bom_node', 'aircraft_id', '$.bom.root', 'active bom root exists', '{"requiredRoot":true,"activeOnly":true}', 'blocker', '必须存在有效 BOM，且根节点必须属于当前飞机。', '检查 t1_aircraft_bom_node 是否存在当前 aircraft_id 的有效根节点。', NULL, 'aircraft', 'all', 'all', 1, 1, 2, 'system', 'system'),
  ('ru110003-0003-4003-8003-000000000003', @tpl_v11, 'ta111003-1003-4003-8003-000000001003', 'RULE-SYSTEM-INTERFACE', '系统接口检查', 'relation', 't1_aircraft_bom_node', 'parent_id', '$.system.interface', 'system relation is traceable', '{"relation":["upstream_aircraft","downstream_nodes","interface_docs"]}', 'warning', '系统目录中应能展示上级飞机、下级组成和关键接口关系。', '补充系统接口文件或关系映射。', NULL, 'system', 'all', 'all', 1, 1, 3, 'system', 'system'),
  ('ru110019-0019-4019-8019-000000000019', @tpl_v11, 'ta111504-1504-4504-8504-000000001504', 'RULE-SUBSYSTEM-EQUIPMENT', '子系统组成检查', 'relation', 't1_aircraft_bom_node', 'parent_id', '$.subsystem.equipment', 'equipment count > 0', '{"minChildren":1}', 'warning', '子系统目录应能定位组成设备，缺失时提示。', '补齐子系统下级设备 BOM 节点或确认该子系统为占位节点。', NULL, 'subsystem', 'all', 'all', 1, 1, 19, 'system', 'system'),
  ('ru110004-0004-4004-8004-000000000004', @tpl_v11, 'ta112002-2002-4002-8002-000000002002', 'RULE-EQUIPMENT-STRUCTURE', '设备组成检查', 'relation', 't1_aircraft_bom_node', 'parent_id', '$.equipment.structure', 'children count > 0', '{"minChildren":1}', 'warning', '设备目录应能定位下级组件或零件，缺失时提示。', '补齐设备下级 BOM 节点或确认该设备为叶子节点。', NULL, 'equipment', 'all', 'all', 1, 1, 4, 'system', 'system'),
  ('ru110005-0005-4005-8005-000000000005', @tpl_v11, 'ta113005-3005-4005-8005-000000003005', 'RULE-COMPONENT-INSPECTION', '组件检验检查', 'expression', 't1_inspection_record', 'result', '$.component.inspection.result', 'result in (PASS, ACCEPTED)', '{"accepted":["PASS","ACCEPTED"]}', 'warning', '组件检验记录缺失或检验结果异常时提示。', '补充组件检验记录，异常结果需挂接处置说明。', NULL, 'component', 'all', 'all', 1, 1, 5, 'system', 'system'),
  ('ru110006-0006-4006-8006-000000000006', @tpl_v11, 'ta114001-4001-4001-8001-000000004001', 'RULE-PART-INSTANCE', '零件实物检查', 'relation', 't1_part_instance', 'id', '$.part.instance_id', 'part instance exists', '{"required":["part_number","serial_number","batch_number"]}', 'blocker', '零件节点必须能关联件号或实物件，不能只停留在空节点。', '补齐 t1_part_instance 并与 t1_aircraft_bom_node.part_instance_id 关联。', NULL, 'part', 'all', 'all', 1, 1, 6, 'system', 'system'),
  ('ru110007-0007-4007-8007-000000000007', @tpl_v11, 'ta110108-0108-4108-8108-000000000108', 'RULE-DOCUMENT-FILE-KEY', '附件地址检查', 'business', 't1_document_entry', 'file_storage_key', '$.document.file_storage_key', 'file_storage_key is not null', '{"field":"file_storage_key"}', 'warning', '文档条目没有文件地址时提示风险。', '补充文件存储键或标记线下文件位置。', NULL, 'aircraft', 'all', 'all', 1, 1, 7, 'system', 'system'),
  ('ru110008-0008-4008-8008-000000000008', @tpl_v11, 'ta114002-4002-4002-8002-000000004002', 'RULE-HYD-DOC-CURRENT', '液压弯管当前图纸检查', 'relation', 't1_part_document', 'is_current', '$.part.documents', 'current drawing exists', '{"part_number":"HYD-TUBE-MLG-32A","requiredDocs":["DRAWING","SPEC","TEST_REPORT"]}', 'blocker', 'HYD-TUBE-MLG-32A 必须有关联当前工程图、材料规范和冲击仿真报告。', '确认 C919-32-1187 Rev.C、AMS-4944-TB3、HYD-IMP-2025-0412 为当前有效。', NULL, 'part', 'self_made', 'key_only', 1, 1, 8, 'system', 'system'),
  ('ru110009-0009-4009-8009-000000000009', @tpl_v11, 'ta114002-4002-4002-8002-000000004002', 'RULE-HYD-IMPACT-MODEL', '液压弯管冲击仿真检查', 'business', 't1_part_hydraulic_tube_impact_model', 'part_number', '$.part.impact_model', 'model and segment and support and fluid and boundary exist', '{"requiredTables":["t1_part_hydraulic_tube_impact_model","t1_impact_tube_segment","t1_impact_tube_support","t1_impact_tube_fluid","t1_impact_tube_boundary_condition"],"minSegments":5,"minSupports":4,"valveCloseTimeS":0.035}', 'blocker', '液压弯管缺少冲击仿真模型、几何段、卡箍支撑、流体或边界条件。', '补齐水锤仿真模型；B-1234 演示要求 5 段管路、4 个支撑和 35 ms 关阀工况。', NULL, 'part', 'self_made', 'key_only', 1, 1, 9, 'system', 'system'),
  ('ru110010-0010-4010-8010-000000000010', @tpl_v11, 'ta114003-4003-4003-8003-000000004003', 'RULE-HYD-MATERIAL-TRACE', '液压弯管炉批追溯检查', 'relation', 't1_material_lot_trace', 'lot_number', '$.part.material_lot', 'lot_number and mill_cert_number exist', '{"material_pn":"TB3-TUBE-12.7x1.02","lot":"HEAT-25Q4-8841"}', 'blocker', '液压弯管必须能追溯原材料炉批和材质证明。', '补齐 t1_material_lot_trace.lot_number 与 mill_cert_number。', NULL, 'part', 'self_made', 'key_only', 1, 1, 10, 'system', 'system'),
  ('ru110011-0011-4011-8011-000000000011', @tpl_v11, 'ta114003-4003-4003-8003-000000004003', 'RULE-HYD-SHOP-CLOSED', '液压弯管工单关闭检查', 'expression', 't1_shop_order', 'status', '$.part.t1_shop_order.status', 'status = CLOSED and quantity_produced = quantity_ordered', '{"requiredStatus":"CLOSED","orderCode":"SO-HYD-2026-0042"}', 'blocker', '液压弯管制造工单必须关闭且产出数量满足需求。', '检查 SO-HYD-2026-0042 的关闭状态和产出数量。', NULL, 'part', 'self_made', 'key_only', 1, 1, 11, 'system', 'system'),
  ('ru110012-0012-4012-8012-000000000012', @tpl_v11, 'ta114003-4003-4003-8003-000000004003', 'RULE-HYD-OPS-COMPLETE', '液压弯管关键工序完成检查', 'business', 't1_shop_order_task', 'status', '$.part.tasks', 'OP10 OP20 OP40 OP50 completed', '{"requiredSteps":["HYD-TUBE-32A-OP10","HYD-TUBE-32A-OP20","HYD-TUBE-32A-OP40","HYD-TUBE-32A-OP50"],"requiredStatus":"COMPLETED"}', 'error', '液压弯管关键工序必须完成。', '检查来料复验、数控弯管、无损检测、气密标识任务状态。', NULL, 'part', 'self_made', 'key_only', 1, 1, 12, 'system', 'system'),
  ('ru110013-0013-4013-8013-000000000013', @tpl_v11, 'ta114004-4004-4004-8004-000000004004', 'RULE-HYD-LEAK-PASS', '液压弯管气密检验检查', 'expression', 't1_inspection_record', 'result', '$.part.inspection.result', 'result = PASS and hold_pressure_mpa >= 21', '{"inspectionStd":"HYD-LEAK-CHK-32A","holdPressureMpa":21,"holdMin":5,"maxLeakRateMlMin":0}', 'blocker', '液压弯管气密试验必须 PASS，21 MPa 保压 5 min，泄漏率为 0。', '复核 t1_inspection_record.measurement_values 和 result。', NULL, 'part', 'self_made', 'key_only', 1, 1, 13, 'system', 'system'),
  ('ru110014-0014-4014-8014-000000000014', @tpl_v11, 'ta114005-4005-4005-8005-000000004005', 'RULE-HYD-INSTALL-PARAM', '液压弯管装机参数检查', 'business', 't1_assembly_record', 'assembly_params', '$.part.assembly_params', 'torque_n_m = 24 and leak_check_result = PASS', '{"torqueNm":24,"postInstallLeakCheckMpa":21,"result":"PASS"}', 'error', '液压弯管装机记录应包含 24 N·m 力矩和装后试压 PASS。', '补充 t1_assembly_record.assembly_params 中的 torque_n_m、post_install_leak_check_mpa、leak_check_result。', NULL, 'part', 'self_made', 'key_only', 1, 1, 14, 'system', 'system'),
  ('ru110015-0015-4015-8015-000000000015', @tpl_v11, 'ta114007-4007-4007-8007-000000004007', 'RULE-HYD-FAULT-CLOSED', '液压弯管故障关闭检查', 'expression', 't1_fault_event', 'status', '$.part.fault.status', 'status = RESOLVED', '{"faultCode":"32-HYD-PULSE-01","requiredStatus":"RESOLVED"}', 'warning', '已纳入卷宗的液压弯管故障应有关联处置并关闭。', '检查故障 32-HYD-PULSE-01 是否关联 WO-B1234-2026-0329 并关闭。', NULL, 'part', 'self_made', 'key_only', 1, 1, 15, 'system', 'system'),
  ('ru110016-0016-4016-8016-000000000016', @tpl_v11, 'ta114006-4006-4006-8006-000000004006', 'RULE-HYD-WO-CLOSED', '液压弯管维修工单关闭检查', 'expression', 't1_work_order', 'wo_status', '$.part.t1_work_order.wo_status', 'wo_status = CLOSED and close_date is not null', '{"woNumber":"WO-B1234-2026-0329"}', 'warning', '液压弯管维修工单应关闭并记录关闭时间。', '补齐 t1_work_order.close_date 和处置备注。', NULL, 'part', 'self_made', 'key_only', 1, 1, 16, 'system', 'system'),
  ('ru110017-0017-4017-8017-000000000017', @tpl_v11, 'ta114006-4006-4006-8006-000000004006', 'RULE-HYD-SERVICE-INSPECTION', '液压弯管服役巡检检查', 'relation', 't1_quality_text_record', 'record_type', '$.part.service.inspection', 'service inspection exists', '{"recordType":"INSPECTION_REPORT","summary":"无渗漏"}', 'warning', '液压弯管服役目录应能看到最近一次巡检记录。', '补充 QTR-HYD-SVC-128 或同类记录。', NULL, 'part', 'self_made', 'key_only', 1, 1, 17, 'system', 'system'),
  ('ru110018-0018-4018-8018-000000000018', @tpl_v11, 'ta114008-4008-4008-8008-000000004008', 'RULE-HYD-ATTACHMENT-COMPLETE', '液压弯管证明附件检查', 'business', 't1_document_entry', 'doc_no', '$.part.documents', 'drawing and impact report and work card attached', '{"requiredDocs":["C919-32-1187","HYD-IMP-2025-0412","WC-32-1187-029"]}', 'warning', '液压弯管卷宗应挂接工程图、冲击仿真报告和拆换工卡。', '确认 t1_document_entry 中三个文件均有 file_storage_key。', NULL, 'part', 'self_made', 'key_only', 1, 1, 18, 'system', 'system');

-- Generic completeness backfill for required non-hydraulic chapters. The
-- hydraulic tube rows above stay deliberately more detailed for the demo.
INSERT INTO t1_dossier_template_data_source (
  id, template_id, chapter_id, source_code, source_system, source_table,
  source_name, source_desc, lifecycle_stage, source_record_type, join_condition_json,
  filter_condition_json, apply_object_type, supply_mode_scope, key_part_scope,
  required_flag, enabled_flag, sort_order, attrs_json, created_by, updated_by
)
SELECT
  UUID(),
  @tpl_v11,
  c.id,
  CONCAT('SRC-', c.chapter_code, '-BUSINESS'),
  CASE
    WHEN c.chapter_code LIKE '%DESIGN%' THEN 'DESIGN'
    WHEN c.chapter_code LIKE '%MANUFACTURING%' OR c.chapter_code LIKE '%ASSEMBLY_RECORD%' OR c.chapter_code LIKE '%INSPECTION%' THEN 'MES'
    WHEN c.chapter_code LIKE '%INSTALL%' OR c.chapter_code LIKE '%PARTS%' OR c.chapter_code LIKE '%STRUCTURE%' THEN 'CONFIG'
    ELSE 'DOSSIER'
  END,
  CASE
    WHEN c.chapter_code LIKE '%DESIGN%' THEN 't1_part_document'
    WHEN c.chapter_code LIKE '%MANUFACTURING%' THEN 't1_shop_order'
    WHEN c.chapter_code LIKE '%ASSEMBLY_RECORD%' THEN 't1_assembly_record'
    WHEN c.chapter_code LIKE '%INSPECTION%' THEN 't1_inspection_record'
    WHEN c.chapter_code LIKE '%INSTALL%' THEN 't1_install_removal'
    WHEN c.chapter_code LIKE '%PARTS%' OR c.chapter_code LIKE '%STRUCTURE%' THEN 't1_aircraft_bom_node'
    WHEN c.chapter_code LIKE '%BASIC%' THEN 't1_part_master'
    ELSE 't1_dossier_structure_node'
  END,
  CONCAT(c.chapter_name, '业务数据'),
  CONCAT('补齐 ', c.chapter_name, ' 的标准业务数据来源；具体演示重点仍在起落架液压弯管。'),
  CASE
    WHEN c.chapter_code LIKE '%DESIGN%' THEN 'DESIGN'
    WHEN c.chapter_code LIKE '%MANUFACTURING%' OR c.chapter_code LIKE '%ASSEMBLY_RECORD%' OR c.chapter_code LIKE '%INSPECTION%' THEN 'MANUFACTURING'
    WHEN c.chapter_code LIKE '%INSTALL%' OR c.chapter_code LIKE '%PARTS%' OR c.chapter_code LIKE '%STRUCTURE%' THEN 'DOSSIER'
    ELSE 'DOSSIER'
  END,
  'business',
  JSON_OBJECT('node_id', '${nodeId}', 'aircraft_id', '${aircraftId}'),
  JSON_OBJECT('enabled', TRUE),
  COALESCE(JSON_UNQUOTE(JSON_EXTRACT(c.attrs_json, '$.objectLevel')), 'aircraft'),
  'all',
  'all',
  1,
  1,
  c.sort_order + 600,
  JSON_OBJECT('autoBackfill', TRUE, 'source', 'seed completeness'),
  'system',
  'system'
FROM t1_dossier_template_chapter c
WHERE c.template_id = @tpl_v11
  AND c.node_kind = 'chapter'
  AND c.required_flag = 1
  AND c.enabled_flag = 1
  AND NOT EXISTS (
    SELECT 1
    FROM t1_dossier_template_data_source ds
    WHERE ds.chapter_id = c.id
      AND ds.enabled_flag = 1
  );

INSERT INTO t1_dossier_template_rule (
  id, template_id, chapter_id, rule_code, rule_name, rule_type,
  target_table, target_field, target_path, rule_expression, rule_expression_json,
  severity, error_message, remediation_hint, bind_quality_rule_id,
  apply_object_type, supply_mode_scope, key_part_scope, required_flag,
  enabled_flag, sort_order, created_by, updated_by
)
SELECT
  UUID(),
  @tpl_v11,
  c.id,
  CONCAT('RULE-', c.chapter_code, '-COMPLETE'),
  CONCAT(c.chapter_name, '完整性检查'),
  'business',
  CASE
    WHEN c.chapter_code LIKE '%DESIGN%' THEN 't1_part_document'
    WHEN c.chapter_code LIKE '%MANUFACTURING%' THEN 't1_shop_order'
    WHEN c.chapter_code LIKE '%ASSEMBLY_RECORD%' THEN 't1_assembly_record'
    WHEN c.chapter_code LIKE '%INSPECTION%' THEN 't1_inspection_record'
    WHEN c.chapter_code LIKE '%INSTALL%' THEN 't1_install_removal'
    WHEN c.chapter_code LIKE '%PARTS%' OR c.chapter_code LIKE '%STRUCTURE%' THEN 't1_aircraft_bom_node'
    WHEN c.chapter_code LIKE '%BASIC%' THEN 't1_part_master'
    ELSE 't1_dossier_structure_node'
  END,
  'id',
  CONCAT('$.', COALESCE(JSON_UNQUOTE(JSON_EXTRACT(c.attrs_json, '$.objectLevel')), 'aircraft'), '.', LOWER(c.chapter_code)),
  'required chapter business data exists',
  JSON_OBJECT('chapterCode', c.chapter_code, 'primaryFields', JSON_EXTRACT(c.attrs_json, '$.primaryFields')),
  'warning',
  CONCAT(c.chapter_name, '缺少可生成的业务数据时提示。'),
  CONCAT('检查 ', c.chapter_name, ' 对应业务表和主键关联。'),
  NULL,
  COALESCE(JSON_UNQUOTE(JSON_EXTRACT(c.attrs_json, '$.objectLevel')), 'aircraft'),
  'all',
  'all',
  1,
  1,
  c.sort_order + 600,
  'system',
  'system'
FROM t1_dossier_template_chapter c
WHERE c.template_id = @tpl_v11
  AND c.node_kind = 'chapter'
  AND c.required_flag = 1
  AND c.enabled_flag = 1
  AND NOT EXISTS (
    SELECT 1
    FROM t1_dossier_template_rule r
    WHERE r.chapter_id = c.id
      AND r.enabled_flag = 1
  );

UPDATE t1_dossier_template
SET validation_rules_json = JSON_OBJECT(
      'ruleCount', (SELECT COUNT(*) FROM t1_dossier_template_rule WHERE template_id = @tpl_v11),
      'blockerCount', (SELECT COUNT(*) FROM t1_dossier_template_rule WHERE template_id = @tpl_v11 AND severity = 'blocker'),
      'completenessBackfill', TRUE
    ),
    updated_at = CURRENT_TIMESTAMP(6),
    updated_by = 'system'
WHERE id = @tpl_v11;

INSERT INTO t1_dossier_template_param (
  id, template_id, chapter_id, param_code, param_name, param_type,
  param_value, default_value, option_json, required_flag, editable_flag,
  enabled_flag, sort_order, param_desc, created_by, updated_by
) VALUES
  ('pa110001-0001-4001-8001-000000000001', @tpl_v11, NULL, 'keep_empty_chapter', '空目录处理', 'boolean', 'true', 'true', '[]', 0, 1, 1, 1, '保留空目录并在页面标记无数据。', 'system', 'system'),
  ('pa110002-0002-4002-8002-000000000002', @tpl_v11, NULL, 'regenerate_strategy', '重新生成策略', 'enum', 'new_version', 'new_version', '[{"label":"生成新版本","value":"new_version"},{"label":"覆盖草稿","value":"overwrite_draft"}]', 0, 1, 1, 2, '重新生成卷宗时默认形成新版本。', 'system', 'system'),
  ('pa110003-0003-4003-8003-000000000003', @tpl_v11, NULL, 'snapshot_enabled', '数据快照', 'boolean', 'true', 'true', '[]', 0, 1, 1, 3, '固定生成时的数据状态，便于追溯。', 'system', 'system'),
  ('pa110004-0004-4004-8004-000000000004', @tpl_v11, NULL, 'missing_data_policy', '缺失数据处理', 'enum', 'block_key_warn_normal', 'block_key_warn_normal', '[{"label":"关键缺失阻止生成，普通缺失提醒","value":"block_key_warn_normal"},{"label":"全部允许生成并提醒","value":"warn_all"}]', 0, 1, 1, 4, '关键数据缺失不允许生成，非关键缺失给出风险提示。', 'system', 'system'),
  ('pa110005-0005-4005-8005-000000000005', @tpl_v11, NULL, 'default_export_format', '默认导出格式', 'enum', 'PDF+ZIP', 'PDF+ZIP', '[{"label":"PDF + 附件 ZIP","value":"PDF+ZIP"},{"label":"PDF","value":"PDF"},{"label":"Word","value":"WORD"}]', 0, 1, 1, 5, '默认导出主卷 PDF 和附件 ZIP。', 'system', 'system'),
  ('pa110006-0006-4006-8006-000000000006', @tpl_v11, NULL, 'auto_attach_files', '文件自动挂接', 'boolean', 'true', 'true', '[]', 0, 1, 1, 6, '自动挂接图纸、报告、证明和工卡。', 'system', 'system'),
  ('pa110007-0007-4007-8007-000000000007', @tpl_v11, NULL, 'default_time_range', '默认时间范围', 'enum', 'all_history', 'all_history', '[{"label":"全部历史","value":"all_history"},{"label":"最近一年","value":"last_year"},{"label":"最近一次维修周期","value":"last_maintenance_cycle"}]', 0, 1, 1, 7, '默认读取全部历史记录。', 'system', 'system'),
  ('pa110008-0008-4008-8008-000000000008', @tpl_v11, NULL, 'demo_aircraft_id', '演示飞机 ID', 'string', 'b0000001-0001-4001-8001-000000000001', 'b0000001-0001-4001-8001-000000000001', '[]', 0, 0, 1, 8, 'B-1234 演示飞机。', 'system', 'system'),
  ('pa110009-0009-4009-8009-000000000009', @tpl_v11, NULL, 'demo_part_number', '演示件号', 'string', 'HYD-TUBE-MLG-32A', 'HYD-TUBE-MLG-32A', '[]', 0, 0, 1, 9, '起落架主起液压供压弯管。', 'system', 'system'),
  ('pa110010-0010-4010-8010-000000000010', @tpl_v11, NULL, 'demo_serial_number', '演示序列号', 'string', 'HT-MLG-32A-2026-0042', 'HT-MLG-32A-2026-0042', '[]', 0, 0, 1, 10, '液压弯管演示实物实例。', 'system', 'system');

COMMIT;

SELECT 'dossier_template_management_seed_mysql8.sql applied' AS result,
       (SELECT COUNT(*) FROM t1_dossier_template_chapter WHERE template_id = @tpl_v11) AS chapter_count,
       (SELECT COUNT(*) FROM t1_dossier_template_data_source WHERE template_id = @tpl_v11) AS source_count,
       (SELECT COUNT(*) FROM t1_dossier_template_rule WHERE template_id = @tpl_v11) AS rule_count,
       (SELECT COUNT(*) FROM t1_dossier_template_param WHERE template_id = @tpl_v11) AS param_count;
