-- 课题四：菜单、页面、按钮权限 SQL
-- 注意：menu_id、parent_id 需要由主课题统一分配后再执行，避免与其他课题冲突。

-- 页面路径建议：
-- project4/fileofda/index
-- project4/resultofen/index
-- project4/resultoffu/index
-- project4/resultofgr/index
-- project4/resultofrc/index

-- 权限标识建议：
-- project4:file:list
-- project4:file:add
-- project4:file:edit
-- project4:file:remove
-- project4:augment:list
-- project4:augment:run
-- project4:diagnosis:list
-- project4:diagnosis:run
-- project4:fusion:list
-- project4:fusion:run
-- project4:rootcause:list
-- project4:rootcause:run

-- 示例：请主课题确认 parent_id 和 menu_id 后再启用
/*
INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
(4000, '课题四故障诊断', 0, 4, 'project4', NULL, NULL, 1, 0, 'M', '0', '0', '', 'monitor', 'admin', NOW(), '课题四目录'),

(4001, '数据文件管理', 4000, 1, 'fileofda', 'project4/fileofda/index', NULL, 1, 0, 'C', '0', '0', 'project4:file:list', 'upload', 'admin', NOW(), '数据文件管理'),

(4002, '数据增强结果', 4000, 2, 'resultofen', 'project4/resultofen/index', NULL, 1, 0, 'C', '0', '0', 'project4:augment:list', 'chart', 'admin', NOW(), '数据增强结果'),

(4003, '故障诊断结果', 4000, 3, 'resultofgr', 'project4/resultofgr/index', NULL, 1, 0, 'C', '0', '0', 'project4:diagnosis:list', 'bug', 'admin', NOW(), '故障诊断结果'),

(4004, '特征融合结果', 4000, 4, 'resultoffu', 'project4/resultoffu/index', NULL, 1, 0, 'C', '0', '0', 'project4:fusion:list', 'tree', 'admin', NOW(), '特征融合结果'),

(4005, '根因分析结果', 4000, 5, 'resultofrc', 'project4/resultofrc/index', NULL, 1, 0, 'C', '0', '0', 'project4:rootcause:list', 'search', 'admin', NOW(), '根因分析结果');
*/
