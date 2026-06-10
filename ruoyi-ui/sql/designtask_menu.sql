-- ============================================
-- 设计任务模块菜单SQL
-- ============================================

-- 注意：这些SQL需要根据实际菜单结构设置 parent_id

-- 1、添加一级菜单（课题二）
INSERT INTO sys_menu (
  menu_id, menu_name, parent_name, parent_id, order_num,
  path, component, is_frame, is_cache, menu_type,
  visible, status, perms, icon, create_by, create_time, remark
) VALUES (
  2000, '课题二', '0', 0, 9,
  'project_2', 'project_2/index',
  1, 0, 'M',
  '0', '0', '',
  'clipboard',
  'admin', SYSDATE(), '课题二主菜单'
);

-- 2、添加任务管理菜单
INSERT INTO sys_menu (
  menu_id, menu_name, parent_name, parent_id, order_num,
  path, component, is_frame, is_cache, menu_type,
  visible, status, perms, icon, create_by, create_time, remark
) VALUES (
  2001, '任务管理', '课题二', 2000, 1,
  'designtask-manage', 'project_2/designtask-manage',
  1, 0, 'C',
  '0', '0', 'designtask:task:list',
  'list',
  'admin', SYSDATE(), '任务管理页面'
);

-- 3、添加流程发起菜单
INSERT INTO sys_menu (
  menu_id, menu_name, parent_name, parent_id, order_num,
  path, component, is_frame, is_cache, menu_type,
  visible, status, perms, icon, create_by, create_time, remark
) VALUES (
  2002, '流程发起', '课题二', 2000, 2,
  'designtask-create', 'project_2/designtask-create',
  1, 0, 'C',
  '0', '0', 'designtask:task:add',
  'form',
  'admin', SYSDATE(), '流程发起页面'
);

-- ============================================
-- 菜单SQL执行说明
-- ============================================
-- 1、请根据实际的菜单ID范围设置 menu_id，避免与其他菜单ID冲突
-- 2、parent_id 需要对应实际的父级菜单ID
-- 3、执行SQL后需要重新登录后台才能看到新菜单
-- 4、权限标识perms需要与Controller中的 @PreAuthorize 注解对应