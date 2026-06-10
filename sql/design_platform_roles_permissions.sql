-- 设计制造协同优化平台角色与权限初始化脚本
-- 适用库：ry-cloud
-- 说明：
-- 1. 可重复执行。
-- 2. 会按 role_key 创建/更新平台角色。
-- 3. 如果你已经手动创建了同名角色，会把这些角色的 role_key 对齐为本脚本约定值。
-- 4. 只重置这些平台角色在 2600-2620 菜单范围内的授权，不影响系统管理等其他菜单权限。

SET NAMES utf8mb4;

-- 一、平台菜单与按钮权限
INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
  (2600, '设计制造协同优化平台', 0, 5, 'design-platform', NULL, '', '', 1, 0, 'M', '0', '0', '', 'tree-table', 'admin', SYSDATE(), '', NULL, '协同优化平台目录'),
  (2601, '首页（任务看板）', 2600, 1, 'dashboard', 'designtask/dashboard/index', '', '', 1, 0, 'C', '0', '0', 'designtask:task:list', 'dashboard', 'admin', SYSDATE(), '', NULL, ''),
  (2602, '协同机制生成', 2600, 2, 'mechanism', 'designtask/mechanism/index', '', '', 1, 0, 'C', '0', '0', 'designtask:task:add', 'guide', 'admin', SYSDATE(), '', NULL, ''),
  (2603, '目标约束选择', 2600, 3, 'objective', 'designtask/objective/index', '', '', 1, 0, 'C', '0', '0', 'designtask:task:handle', 'checkbox', 'admin', SYSDATE(), '', NULL, ''),
  (2604, '模型解耦求解', 2600, 4, 'solve', 'designtask/solve/index', '', '', 1, 0, 'C', '0', '0', 'designtask:task:solve', 'component', 'admin', SYSDATE(), '', NULL, ''),
  (2605, '仿真验证确认', 2600, 5, 'simulation', 'designtask/simulation/index', '', '', 1, 0, 'C', '0', '0', 'designtask:task:simulate', 'chart', 'admin', SYSDATE(), '', NULL, '')
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  parent_id = VALUES(parent_id),
  order_num = VALUES(order_num),
  path = VALUES(path),
  component = VALUES(component),
  perms = VALUES(perms),
  icon = VALUES(icon),
  status = '0',
  visible = '0',
  update_by = 'admin',
  update_time = SYSDATE();

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
  (2606, '任务查询', 2601, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:task:query', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2607, '提交目标约束', 2603, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:task:handle', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2608, '冲突校验', 2604, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:task:conflict', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2609, '任务分解', 2604, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:task:decompose', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2610, '模型求解', 2604, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:task:solve', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2611, '仿真验证', 2605, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:task:simulate', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2612, '领导审批', 2605, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:task:approve', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2613, '流程模板查询', 2602, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:flow:list', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2614, '标准资源管理', 2600, 6, 'resource', 'designtask/resource/index', '', '', 1, 0, 'C', '0', '0', 'designtask:resource:list', 'documentation', 'admin', SYSDATE(), '', NULL, ''),
  (2615, '标准资源新增', 2614, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:resource:add', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2616, '流程模板详情', 2602, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:flow:query', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2617, '流程模板新增', 2602, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:flow:add', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2618, '流程模板修改', 2602, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:flow:edit', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2619, '流程模板删除', 2602, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:flow:remove', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2620, '设计变量选择', 2604, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'designtask:task:variable', '#', 'admin', SYSDATE(), '', NULL, '')
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  parent_id = VALUES(parent_id),
  order_num = VALUES(order_num),
  path = VALUES(path),
  component = VALUES(component),
  menu_type = VALUES(menu_type),
  perms = VALUES(perms),
  icon = VALUES(icon),
  status = '0',
  visible = '0',
  update_by = 'admin',
  update_time = SYSDATE();

-- 二、平台角色
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_design_platform_role (
  role_key varchar(100) NOT NULL,
  role_name varchar(30) NOT NULL,
  role_sort int NOT NULL,
  remark varchar(500) DEFAULT NULL,
  PRIMARY KEY (role_key)
) ENGINE=MEMORY;

DELETE FROM tmp_design_platform_role;
INSERT INTO tmp_design_platform_role(role_key, role_name, role_sort, remark) VALUES
  ('design_task_owner', '任务负责人', 10, '发起协同优化任务，执行冲突校验、任务分解、模型求解和仿真验证'),
  ('structure_engineer', '结构工程师', 20, '选择结构学科目标与约束'),
  ('layout_engineer', '布局工程师', 30, '选择布局学科目标与约束'),
  ('aero_engineer', '气动工程师', 40, '选择气动学科目标与约束'),
  ('hydraulic_engineer', '液压工程师', 50, '选择液压学科目标与约束'),
  ('manufacturing_engineer', '制造工程师', 60, '选择制造相关约束并参与仿真验证确认'),
  ('approval_leader', '审批领导', 70, '查看任务结果并完成最终审批');

-- 如果已手动创建同名角色，先把 role_key 对齐到本方案。
UPDATE sys_role r
JOIN tmp_design_platform_role t ON r.role_name = t.role_name
SET r.role_key = t.role_key,
    r.role_sort = t.role_sort,
    r.status = '0',
    r.del_flag = '0',
    r.remark = t.remark,
    r.update_by = 'admin',
    r.update_time = SYSDATE()
WHERE r.del_flag <> '2';

-- 创建缺失角色。
INSERT INTO sys_role
  (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, update_by, update_time, remark)
SELECT
  t.role_name, t.role_key, t.role_sort, '2', 1, 1, '0', '0', 'admin', SYSDATE(), '', NULL, t.remark
FROM tmp_design_platform_role t
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role r WHERE r.role_key = t.role_key AND r.del_flag <> '2'
);

-- 三、角色菜单授权
-- 先清理本平台菜单范围内的旧授权，避免重复和权限残留。
DELETE rm
FROM sys_role_menu rm
JOIN sys_role r ON r.role_id = rm.role_id
JOIN tmp_design_platform_role t ON (r.role_key = t.role_key OR r.role_name = t.role_name)
WHERE rm.menu_id BETWEEN 2600 AND 2620;

-- 所有平台角色都能进入平台目录和首页看板。
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN tmp_design_platform_role t ON (r.role_key = t.role_key OR r.role_name = t.role_name)
JOIN sys_menu m ON m.menu_id IN (2600, 2601, 2606)
WHERE r.del_flag <> '2';

-- 任务负责人：发起任务、查看流程模板、模型解耦求解、仿真验证、资源管理。
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (2602, 2604, 2605, 2608, 2609, 2610, 2611, 2613, 2614, 2615, 2616, 2620)
WHERE (r.role_key = 'design_task_owner' OR r.role_name = '任务负责人')
  AND r.del_flag <> '2';

-- 学科工程师：目标约束选择。
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (2603, 2607)
WHERE (r.role_key IN ('structure_engineer', 'layout_engineer', 'aero_engineer', 'hydraulic_engineer')
   OR r.role_name IN ('结构工程师', '布局工程师', '气动工程师', '液压工程师'))
  AND r.del_flag <> '2';

-- 制造工程师：目标约束选择 + 仿真验证确认。
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (2603, 2605, 2607, 2611)
WHERE (r.role_key = 'manufacturing_engineer' OR r.role_name = '制造工程师')
  AND r.del_flag <> '2';

-- 平台管理员可维护模板和资源；未创建时不产生影响。
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id BETWEEN 2600 AND 2620
WHERE r.role_key IN ('admin', 'design_platform_admin');

-- 审批领导：仿真验证确认页中的审批区。
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (2605, 2612)
WHERE (r.role_key = 'approval_leader' OR r.role_name = '审批领导')
  AND r.del_flag <> '2';

DROP TEMPORARY TABLE IF EXISTS tmp_design_platform_role;
