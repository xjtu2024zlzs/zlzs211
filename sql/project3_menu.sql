-- Project 3 dynamic menu for RuoYi-Cloud
-- Target table structure: zlzs211/sql/ry_20260417.sql
-- This script is idempotent by menu_name/perms checks.

SET @project3_parent_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = 0
    AND menu_name = '课题三'
  LIMIT 1
);

UPDATE sys_menu
SET
  order_num = 9,
  path = 'project_3',
  component = 'Layout',
  query = '',
  route_name = 'Project3Menu',
  is_frame = 1,
  is_cache = 0,
  menu_type = 'M',
  visible = '0',
  status = '0',
  perms = '',
  icon = 'dashboard',
  update_by = 'admin',
  update_time = NOW(),
  remark = '课题三目录'
WHERE menu_id = @project3_parent_id;

INSERT INTO sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
SELECT
  '课题三', 0, 9, 'project_3', 'Layout', '', 'Project3Menu',
  1, 0, 'M', '0', '0', '', 'dashboard',
  'admin', NOW(), '', NULL, '课题三目录'
WHERE @project3_parent_id IS NULL;

SET @project3_parent_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = 0
    AND menu_name = '课题三'
  LIMIT 1
);

INSERT INTO sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
SELECT
  '生命周期质量监管与故障预防系统首页', @project3_parent_id, 1, 'index', 'project_3/index', '', 'Project3MenuIndex',
  1, 0, 'C', '0', '0', 'project3:home:view', 'dashboard',
  'admin', NOW(), '', NULL, '生命周期质量监管与故障预防系统首页菜单'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE perms = 'project3:home:view'
);

UPDATE sys_menu
SET
  menu_name = '生命周期质量监管与故障预防系统首页',
  parent_id = @project3_parent_id,
  order_num = 1,
  path = 'index',
  component = 'project_3/index',
  query = '',
  route_name = 'Project3MenuIndex',
  is_frame = 1,
  is_cache = 0,
  menu_type = 'C',
  visible = '0',
  status = '0',
  icon = 'dashboard',
  update_by = 'admin',
  update_time = NOW(),
  remark = '生命周期质量监管与故障预防系统首页菜单'
WHERE perms = 'project3:home:view';

INSERT INTO sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
SELECT
  '生命周期质量统一监测', @project3_parent_id, 2, 'monitor', 'project_3/monitor/index', '', 'Project3MenuMonitor',
  1, 0, 'C', '0', '0', 'project3:monitor:view', 'monitor',
  'admin', NOW(), '', NULL, '生命周期质量统一监测菜单'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE perms = 'project3:monitor:view'
);

UPDATE sys_menu
SET
  menu_name = '生命周期质量统一监测',
  parent_id = @project3_parent_id,
  order_num = 2,
  path = 'monitor',
  component = 'project_3/monitor/index',
  query = '',
  route_name = 'Project3MenuMonitor',
  is_frame = 1,
  is_cache = 0,
  menu_type = 'C',
  visible = '0',
  status = '0',
  icon = 'monitor',
  update_by = 'admin',
  update_time = NOW(),
  remark = '生命周期质量统一监测菜单'
WHERE perms = 'project3:monitor:view';

INSERT INTO sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
SELECT
  '关键工序异常信号预警', @project3_parent_id, 3, 'feedback/warning', 'project_3/feedback/warning', '', 'Project3MenuFeedbackWarning',
  1, 0, 'C', '0', '0', 'project3:feedback:warning', 'message',
  'admin', NOW(), '', NULL, '关键工序异常信号预警菜单'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE perms = 'project3:feedback:warning'
);

UPDATE sys_menu
SET
  menu_name = '关键工序异常信号预警',
  parent_id = @project3_parent_id,
  order_num = 3,
  path = 'feedback/warning',
  component = 'project_3/feedback/warning',
  query = '',
  route_name = 'Project3MenuFeedbackWarning',
  is_frame = 1,
  is_cache = 0,
  menu_type = 'C',
  visible = '0',
  status = '0',
  icon = 'message',
  update_by = 'admin',
  update_time = NOW(),
  remark = '关键工序异常信号预警菜单'
WHERE perms = 'project3:feedback:warning';

INSERT INTO sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
SELECT
  '故障识别与预测', @project3_parent_id, 4, 'service/identify', 'project_3/service/identify', '', 'Project3MenuServiceIdentify',
  1, 0, 'C', '0', '0', 'project3:service:identify', 'skill',
  'admin', NOW(), '', NULL, '故障识别与预测菜单'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_menu WHERE perms = 'project3:service:identify'
);

UPDATE sys_menu
SET
  menu_name = '故障识别与预测',
  parent_id = @project3_parent_id,
  order_num = 4,
  path = 'service/identify',
  component = 'project_3/service/identify',
  query = '',
  route_name = 'Project3MenuServiceIdentify',
  is_frame = 1,
  is_cache = 0,
  menu_type = 'C',
  visible = '0',
  status = '0',
  icon = 'skill',
  update_by = 'admin',
  update_time = NOW(),
  remark = '故障识别与预测菜单'
WHERE perms = 'project3:service:identify';

-- Super administrator in RuoYi does not require sys_role_menu records.
-- The following grants project_3 menus to role_id = 2 if that role exists.
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, m.menu_id
FROM sys_menu m
WHERE EXISTS (SELECT 1 FROM sys_role WHERE role_id = 2)
  AND (
    (m.parent_id = 0 AND m.menu_name = '课题三')
    OR m.perms IN (
      'project3:home:view',
      'project3:monitor:view',
      'project3:feedback:warning',
      'project3:service:identify'
    )
  )
  AND NOT EXISTS (
    SELECT 1
    FROM sys_role_menu rm
    WHERE rm.role_id = 2
      AND rm.menu_id = m.menu_id
  );
