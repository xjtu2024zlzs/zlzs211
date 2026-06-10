-- ============================================================
-- project1 menu and permission SQL
-- Target: RuoYi-Cloud sys_menu / sys_role_menu
-- Menu:
-- 数字卷宗
--   异构信息集成
--     数据源管理
--     模式映射任务创建
--     模式映射结果展示
--   异构数据接入
--     数据接入管理
--     数据接入结果展示
-- ============================================================

set names utf8mb4;

delete from sys_role_menu where menu_id between 2100 and 2199;
delete from sys_menu where menu_id between 2100 and 2199;

insert into sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values
    (2100, '数字卷宗', 0, 5, 'dossier', null, '', 'Dossier', 1, 0, 'M', '0', '0', '', 'documentation', 'admin', sysdate(), '', null, '数字卷宗目录'),
    (2110, '异构信息集成', 2100, 1, 'integration', null, '', 'DossierIntegration', 1, 0, 'M', '0', '0', '', 'tree-table', 'admin', sysdate(), '', null, '异构信息集成目录'),
    (2120, '异构数据接入', 2100, 2, 'access', null, '', 'DossierAccess', 1, 0, 'M', '0', '0', '', 'list', 'admin', sysdate(), '', null, '异构数据接入目录');

insert into sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values
    (2111, '数据源管理', 2110, 1, 'datasource', 'project1/datasource/index', '', 'Project1Datasource', 1, 0, 'C', '0', '0', 'project1:datasource:list', 'database', 'admin', sysdate(), '', null, '数据源管理菜单'),
    (2112, '模式映射任务创建', 2110, 2, 'matchTask', 'project1/matchTask/index', '', 'Project1MatchTask', 1, 0, 'C', '0', '0', 'project1:matchTask:list', 'edit', 'admin', sysdate(), '', null, '模式映射任务创建菜单'),
    (2113, '模式映射结果展示', 2110, 3, 'matchResult', 'project1/matchResult/index', '', 'Project1MatchResult', 1, 0, 'C', '0', '0', 'project1:matchResult:list', 'table', 'admin', sysdate(), '', null, '模式映射结果展示菜单'),
    (2121, '数据接入管理', 2120, 1, 'accessPlan', 'project1/accessPlan/index', '', 'Project1AccessPlan', 1, 0, 'C', '0', '0', 'project1:accessPlan:list', 'form', 'admin', sysdate(), '', null, '数据接入管理菜单'),
    (2122, '数据接入结果展示', 2120, 2, 'accessResult', 'project1/accessResult/index', '', 'Project1AccessResult', 1, 0, 'C', '0', '0', 'project1:accessResult:list', 'chart', 'admin', sysdate(), '', null, '数据接入结果展示菜单');

insert into sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values
    (2130, '数据源查询', 2111, 1, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:datasource:query', '#', 'admin', sysdate(), '', null, ''),
    (2131, '数据源新增', 2111, 2, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:datasource:add', '#', 'admin', sysdate(), '', null, ''),
    (2132, '数据源修改', 2111, 3, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:datasource:edit', '#', 'admin', sysdate(), '', null, ''),
    (2133, '数据源删除', 2111, 4, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:datasource:remove', '#', 'admin', sysdate(), '', null, ''),
    (2134, '数据源导出', 2111, 5, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:datasource:export', '#', 'admin', sysdate(), '', null, ''),
    (2135, '连接测试', 2111, 6, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:datasource:test', '#', 'admin', sysdate(), '', null, ''),
    (2136, '模式读取', 2111, 7, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:datasource:schemaRead', '#', 'admin', sysdate(), '', null, ''),
    (2137, '模式查看', 2111, 8, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:datasource:schemaView', '#', 'admin', sysdate(), '', null, ''),

    (2140, '映射任务查询', 2112, 1, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:matchTask:query', '#', 'admin', sysdate(), '', null, ''),
    (2141, '映射任务新增', 2112, 2, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:matchTask:add', '#', 'admin', sysdate(), '', null, ''),
    (2142, '映射任务修改', 2112, 3, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:matchTask:edit', '#', 'admin', sysdate(), '', null, ''),
    (2143, '映射任务删除', 2112, 4, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:matchTask:remove', '#', 'admin', sysdate(), '', null, ''),
    (2144, '映射任务导出', 2112, 5, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:matchTask:export', '#', 'admin', sysdate(), '', null, ''),
    (2145, '运行映射任务', 2112, 6, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:matchTask:run', '#', 'admin', sysdate(), '', null, ''),
    (2146, '查看任务版本', 2112, 7, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:matchTask:version', '#', 'admin', sysdate(), '', null, ''),
    (2147, '查看运行记录', 2112, 8, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:matchTask:record', '#', 'admin', sysdate(), '', null, ''),

    (2150, '映射结果查询', 2113, 1, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:matchResult:query', '#', 'admin', sysdate(), '', null, ''),
    (2151, '映射结果审核', 2113, 2, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:matchResult:edit', '#', 'admin', sysdate(), '', null, ''),
    (2152, '设为默认结果集', 2113, 3, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:matchResult:default', '#', 'admin', sysdate(), '', null, ''),
    (2153, '批量通过', 2113, 4, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:matchResult:approve', '#', 'admin', sysdate(), '', null, ''),
    (2154, '批量驳回', 2113, 5, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:matchResult:reject', '#', 'admin', sysdate(), '', null, ''),
    (2155, '自动通过', 2113, 6, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:matchResult:auto', '#', 'admin', sysdate(), '', null, ''),
    (2156, '审核历史', 2113, 7, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:matchResult:history', '#', 'admin', sysdate(), '', null, ''),

    (2160, '接入计划查询', 2121, 1, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:accessPlan:query', '#', 'admin', sysdate(), '', null, ''),
    (2161, '接入计划新增', 2121, 2, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:accessPlan:add', '#', 'admin', sysdate(), '', null, ''),
    (2162, '接入计划修改', 2121, 3, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:accessPlan:edit', '#', 'admin', sysdate(), '', null, ''),
    (2163, '接入计划删除', 2121, 4, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:accessPlan:remove', '#', 'admin', sysdate(), '', null, ''),
    (2164, '接入计划导出', 2121, 5, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:accessPlan:export', '#', 'admin', sysdate(), '', null, ''),
    (2165, '手动执行接入', 2121, 6, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:accessPlan:execute', '#', 'admin', sysdate(), '', null, ''),
    (2166, '暂停接入计划', 2121, 7, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:accessPlan:pause', '#', 'admin', sysdate(), '', null, ''),
    (2167, '恢复接入计划', 2121, 8, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:accessPlan:resume', '#', 'admin', sysdate(), '', null, ''),
    (2168, '取消当前执行', 2121, 9, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:accessPlan:cancel', '#', 'admin', sysdate(), '', null, ''),

    (2170, '接入结果查询', 2122, 1, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:accessResult:query', '#', 'admin', sysdate(), '', null, ''),
    (2171, '接入结果导出', 2122, 2, '', null, '', '', 1, 0, 'F', '0', '0', 'project1:accessResult:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu (role_id, menu_id)
select r.role_id, m.menu_id
from sys_role r
join sys_menu m on m.menu_id between 2100 and 2199
where r.role_key = 'common'
  and not exists (
      select 1
      from sys_role_menu rm
      where rm.role_id = r.role_id
        and rm.menu_id = m.menu_id
  );
