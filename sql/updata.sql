-- Update Project 3 service menu hierarchy.
-- Target:
-- 课题三
--   生命周期质量监管与故障预防系统首页
--   生命周期质量统一监测
--   全域制造过程反馈监管
--   服役性能周期故障预防
--     服役性能周期故障预防       -> ruoyi-ui/src/views/project_3/service/identify.vue
--     飞机框梁故障识别           -> ruoyi-ui/src/views/project_3/service/FrameBeamCrackIdentify.vue
--
-- Idempotent: safe to execute repeatedly.

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

UPDATE sys_menu
SET
  menu_name = '全域制造过程反馈监管',
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
  remark = '全域制造过程反馈监管菜单'
WHERE perms = 'project3:feedback:warning';

-- Prefer an existing service directory. If it does not exist yet, reuse the old
-- top-level identify menu row as the directory to preserve existing role grants.
SET @service_dir_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = @project3_parent_id
    AND route_name = 'Project3MenuService'
  LIMIT 1
);

SET @service_dir_id := IFNULL(@service_dir_id, (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = @project3_parent_id
    AND perms = 'project3:service:identify'
  LIMIT 1
));

SET @service_dir_id := IFNULL(@service_dir_id, (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = @project3_parent_id
    AND menu_name = '服役性能周期故障预防'
    AND menu_type = 'M'
  LIMIT 1
));

INSERT INTO sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
SELECT
  '服役性能周期故障预防', @project3_parent_id, 4, 'service', 'ParentView', '', 'Project3MenuService',
  1, 0, 'M', '0', '0', '', 'skill',
  'admin', NOW(), '', NULL, '服役性能周期故障预防目录'
WHERE @service_dir_id IS NULL;

SET @service_dir_id := IFNULL(@service_dir_id, (
  SELECT menu_id
  FROM sys_menu
  WHERE parent_id = @project3_parent_id
    AND route_name = 'Project3MenuService'
  LIMIT 1
));

UPDATE sys_menu
SET
  menu_name = '服役性能周期故障预防',
  parent_id = @project3_parent_id,
  order_num = 4,
  path = 'service',
  component = 'ParentView',
  query = '',
  route_name = 'Project3MenuService',
  is_frame = 1,
  is_cache = 0,
  menu_type = 'M',
  visible = '0',
  status = '0',
  perms = '',
  icon = 'skill',
  update_by = 'admin',
  update_time = NOW(),
  remark = '服役性能周期故障预防目录'
WHERE menu_id = @service_dir_id;

-- First child: original identify.vue page.
SET @identify_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE perms = 'project3:service:identify'
  LIMIT 1
);

INSERT INTO sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
SELECT
  '服役性能周期故障预防', @service_dir_id, 1, 'identify', 'project_3/service/identify', '', 'Project3MenuServiceIdentify',
  1, 0, 'C', '0', '0', 'project3:service:identify', 'skill',
  'admin', NOW(), '', NULL, '服役性能周期故障预防页面菜单'
WHERE @identify_menu_id IS NULL;

UPDATE sys_menu
SET
  menu_name = '服役性能周期故障预防',
  parent_id = @service_dir_id,
  order_num = 1,
  path = 'identify',
  component = 'project_3/service/identify',
  query = '',
  route_name = 'Project3MenuServiceIdentify',
  is_frame = 1,
  is_cache = 0,
  menu_type = 'C',
  visible = '0',
  status = '0',
  perms = 'project3:service:identify',
  icon = 'skill',
  update_by = 'admin',
  update_time = NOW(),
  remark = '服役性能周期故障预防页面菜单'
WHERE perms = 'project3:service:identify';

SET @identify_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE perms = 'project3:service:identify'
  LIMIT 1
);

-- Second child: new frame beam crack identification page.
SET @frame_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE perms = 'project3:service:frameBeamCrackIdentify'
  LIMIT 1
);

INSERT INTO sys_menu (
  menu_name, parent_id, order_num, path, component, query, route_name,
  is_frame, is_cache, menu_type, visible, status, perms, icon,
  create_by, create_time, update_by, update_time, remark
)
SELECT
  '飞机框梁故障识别', @service_dir_id, 2, 'frame-beam-crack-identify', 'project_3/service/FrameBeamCrackIdentify', '', 'Project3FrameBeamCrackIdentify',
  1, 0, 'C', '0', '0', 'project3:service:frameBeamCrackIdentify', '#',
  'admin', NOW(), '', NULL, '飞机框梁故障识别菜单'
WHERE @frame_menu_id IS NULL;

UPDATE sys_menu
SET
  menu_name = '飞机框梁故障识别',
  parent_id = @service_dir_id,
  order_num = 2,
  path = 'frame-beam-crack-identify',
  component = 'project_3/service/FrameBeamCrackIdentify',
  query = '',
  route_name = 'Project3FrameBeamCrackIdentify',
  is_frame = 1,
  is_cache = 0,
  menu_type = 'C',
  visible = '0',
  status = '0',
  perms = 'project3:service:frameBeamCrackIdentify',
  icon = '#',
  update_by = 'admin',
  update_time = NOW(),
  remark = '飞机框梁故障识别菜单'
WHERE perms = 'project3:service:frameBeamCrackIdentify';

SET @frame_menu_id := (
  SELECT menu_id
  FROM sys_menu
  WHERE perms = 'project3:service:frameBeamCrackIdentify'
  LIMIT 1
);

-- Grant child menus to roles that already have the service directory.
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT source_roles.role_id, child_menu.menu_id
FROM (
  SELECT DISTINCT role_id
  FROM sys_role_menu
  WHERE menu_id = @service_dir_id
) source_roles
JOIN (
  SELECT @identify_menu_id AS menu_id
  UNION ALL
  SELECT @frame_menu_id AS menu_id
) child_menu ON child_menu.menu_id IS NOT NULL
LEFT JOIN sys_role_menu exists_rm
  ON exists_rm.role_id = source_roles.role_id
  AND exists_rm.menu_id = child_menu.menu_id
WHERE exists_rm.menu_id IS NULL;

-- Preserve the original role_id = 2 behavior from project3_menu.sql.
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, m.menu_id
FROM sys_menu m
LEFT JOIN sys_role_menu rm
  ON rm.role_id = 2
  AND rm.menu_id = m.menu_id
WHERE EXISTS (SELECT 1 FROM sys_role WHERE role_id = 2)
  AND (
    m.menu_id = @project3_parent_id
    OR m.menu_id = @service_dir_id
    OR m.perms IN (
      'project3:home:view',
      'project3:monitor:view',
      'project3:feedback:warning',
      'project3:service:identify',
      'project3:service:frameBeamCrackIdentify'
    )
  )
  AND rm.menu_id IS NULL;
