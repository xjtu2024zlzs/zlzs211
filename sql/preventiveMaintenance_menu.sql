-- 设备预防性维护优化菜单
-- 页面组件：ruoyi-ui/src/views/project3/preventiveMaintenance/index.vue
-- 可重复执行，不创建业务表。

SET @project3_parent_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = 0
    AND (
      path = 'project_3'
      OR route_name = 'Project3Menu'
      OR menu_name = '课题三'
    )
  ORDER BY menu_id
  LIMIT 1
);

INSERT INTO sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
SELECT
  '设备预防性维护优化',
  @project3_parent_id,
  6,
  'preventive-maintenance',
  'project3/preventiveMaintenance/index',
  '',
  'Project3PreventiveMaintenance',
  1,
  0,
  'C',
  '0',
  '0',
  'project3:preventiveMaintenance:view',
  'tool',
  'admin',
  NOW(),
  '',
  NULL,
  '蒙特卡洛模拟与 EMBKA/BKA 多目标预防性维护优化展示页面'
WHERE @project3_parent_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM sys_menu
    WHERE perms = 'project3:preventiveMaintenance:view'
  );

UPDATE sys_menu
SET
  menu_name = '设备预防性维护优化',
  parent_id = @project3_parent_id,
  order_num = 6,
  path = 'preventive-maintenance',
  component = 'project3/preventiveMaintenance/index',
  query = '',
  route_name = 'Project3PreventiveMaintenance',
  is_frame = 1,
  is_cache = 0,
  menu_type = 'C',
  visible = '0',
  status = '0',
  icon = 'tool',
  update_by = 'admin',
  update_time = NOW(),
  remark = '蒙特卡洛模拟与 EMBKA/BKA 多目标预防性维护优化展示页面'
WHERE perms = 'project3:preventiveMaintenance:view'
  AND @project3_parent_id IS NOT NULL;

-- 与现有 project3 菜单脚本保持一致：存在 role_id = 2 时自动授权。
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, m.menu_id
FROM sys_menu m
WHERE m.perms = 'project3:preventiveMaintenance:view'
  AND EXISTS (SELECT 1 FROM sys_role WHERE role_id = 2)
  AND NOT EXISTS (
    SELECT 1
    FROM sys_role_menu rm
    WHERE rm.role_id = 2
      AND rm.menu_id = m.menu_id
  );
