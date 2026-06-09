-- Fix Project 3 menu structure.
-- Goal:
-- 1. Keep "课题三" as a directory menu.
-- 2. Put "生命周期质量监管与故障预防系统首页" as the first child menu.
-- 3. Point the child menu to ruoyi-ui/src/views/project_3/index.vue.

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
  perms = 'project3:home:view',
  icon = 'dashboard',
  update_by = 'admin',
  update_time = NOW(),
  remark = '生命周期质量监管与故障预防系统首页菜单'
WHERE perms = 'project3:home:view';

UPDATE sys_menu
SET
  parent_id = @project3_parent_id,
  order_num = 2,
  path = 'monitor',
  component = 'project_3/monitor/index',
  route_name = 'Project3MenuMonitor',
  menu_type = 'C',
  visible = '0',
  status = '0'
WHERE perms = 'project3:monitor:view';

UPDATE sys_menu
SET
  parent_id = @project3_parent_id,
  order_num = 3,
  path = 'feedback/warning',
  component = 'project_3/feedback/warning',
  route_name = 'Project3MenuFeedbackWarning',
  menu_type = 'C',
  visible = '0',
  status = '0'
WHERE perms = 'project3:feedback:warning';

UPDATE sys_menu
SET
  parent_id = @project3_parent_id,
  order_num = 4,
  path = 'service/identify',
  component = 'project_3/service/identify',
  route_name = 'Project3MenuServiceIdentify',
  menu_type = 'C',
  visible = '0',
  status = '0'
WHERE perms = 'project3:service:identify';

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, m.menu_id
FROM sys_menu m
WHERE EXISTS (SELECT 1 FROM sys_role WHERE role_id = 2)
  AND (
    m.menu_id = @project3_parent_id
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
