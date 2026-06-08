-- Project1 menu and permission initialization for RuoYi-Cloud.
-- Execute after the base RuoYi schema has been initialized.
-- This script is idempotent and only touches project1 menu entries.

set names utf8mb4;

drop temporary table if exists tmp_project1_menu_ids;
create temporary table tmp_project1_menu_ids (
    menu_id bigint not null primary key
) engine = memory;

insert ignore into tmp_project1_menu_ids (menu_id)
select menu_id
from sys_menu
where path = 'project1'
   or component like 'project1/%'
   or perms like 'project1:%';

insert ignore into tmp_project1_menu_ids (menu_id)
select child.menu_id
from sys_menu child
inner join tmp_project1_menu_ids parent on child.parent_id = parent.menu_id;

delete rm
from sys_role_menu rm
inner join tmp_project1_menu_ids m on rm.menu_id = m.menu_id;

delete sm
from sys_menu sm
inner join tmp_project1_menu_ids m on sm.menu_id = m.menu_id;

drop temporary table if exists tmp_project1_menu_ids;

insert into sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values
    (2100, '课题一', 0, 5, 'project1', null, '', 'Project1', 1, 0, 'M', '0', '0', '', 'nested', 'admin', sysdate(), '', null, '课题一目录'),
    (2101, '数据源管理', 2100, 1, 'datasource', 'project1/datasource/index', '', 'Project1Datasource', 1, 0, 'C', '0', '0', 'project1:datasource:list', 'druid', 'admin', sysdate(), '', null, '数据源管理菜单'),
    (2102, '异构信息集成', 2100, 2, 'accessPlan', 'project1/accessPlan/index', '', 'Project1AccessPlan', 1, 0, 'C', '0', '0', 'project1:accessPlan:list', 'list', 'admin', sysdate(), '', null, '异构信息集成菜单'),
    (2103, '异构数据接入', 2100, 3, 'accessResult', 'project1/accessResult/index', '', 'Project1AccessResult', 1, 0, 'C', '0', '0', 'project1:accessResult:list', 'chart', 'admin', sysdate(), '', null, '异构数据接入菜单'),
    (2104, '模式映射任务创建', 2100, 4, 'matchTask', 'project1/matchTask/index', '', 'Project1MatchTask', 1, 0, 'C', '0', '0', 'project1:matchTask:list', 'edit', 'admin', sysdate(), '', null, '模式映射任务创建菜单'),
    (2105, '模式映射结果展示', 2100, 5, 'matchResult', 'project1/matchResult/index', '', 'Project1MatchResult', 1, 0, 'C', '0', '0', 'project1:matchResult:list', 'table', 'admin', sysdate(), '', null, '模式映射结果展示菜单'),
    (2110, '数据源查询', 2101, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:datasource:query', '#', 'admin', sysdate(), '', null, ''),
    (2111, '数据源新增', 2101, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:datasource:add', '#', 'admin', sysdate(), '', null, ''),
    (2112, '数据源修改', 2101, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:datasource:edit', '#', 'admin', sysdate(), '', null, ''),
    (2113, '数据源删除', 2101, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:datasource:remove', '#', 'admin', sysdate(), '', null, ''),
    (2114, '数据源导出', 2101, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:datasource:export', '#', 'admin', sysdate(), '', null, ''),
    (2120, '集成计划查询', 2102, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:accessPlan:query', '#', 'admin', sysdate(), '', null, ''),
    (2121, '集成计划新增', 2102, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:accessPlan:add', '#', 'admin', sysdate(), '', null, ''),
    (2122, '集成计划修改', 2102, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:accessPlan:edit', '#', 'admin', sysdate(), '', null, ''),
    (2123, '集成计划删除', 2102, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:accessPlan:remove', '#', 'admin', sysdate(), '', null, ''),
    (2124, '集成计划导出', 2102, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:accessPlan:export', '#', 'admin', sysdate(), '', null, ''),
    (2130, '接入结果查询', 2103, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:accessResult:query', '#', 'admin', sysdate(), '', null, ''),
    (2131, '接入结果新增', 2103, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:accessResult:add', '#', 'admin', sysdate(), '', null, ''),
    (2132, '接入结果修改', 2103, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:accessResult:edit', '#', 'admin', sysdate(), '', null, ''),
    (2133, '接入结果删除', 2103, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:accessResult:remove', '#', 'admin', sysdate(), '', null, ''),
    (2134, '接入结果导出', 2103, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:accessResult:export', '#', 'admin', sysdate(), '', null, ''),
    (2140, '映射任务查询', 2104, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:matchTask:query', '#', 'admin', sysdate(), '', null, ''),
    (2141, '映射任务新增', 2104, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:matchTask:add', '#', 'admin', sysdate(), '', null, ''),
    (2142, '映射任务修改', 2104, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:matchTask:edit', '#', 'admin', sysdate(), '', null, ''),
    (2143, '映射任务删除', 2104, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:matchTask:remove', '#', 'admin', sysdate(), '', null, ''),
    (2144, '映射任务导出', 2104, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:matchTask:export', '#', 'admin', sysdate(), '', null, ''),
    (2150, '映射结果查询', 2105, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:matchResult:query', '#', 'admin', sysdate(), '', null, ''),
    (2151, '映射结果新增', 2105, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:matchResult:add', '#', 'admin', sysdate(), '', null, ''),
    (2152, '映射结果修改', 2105, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:matchResult:edit', '#', 'admin', sysdate(), '', null, ''),
    (2153, '映射结果删除', 2105, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:matchResult:remove', '#', 'admin', sysdate(), '', null, ''),
    (2154, '映射结果导出', 2105, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'project1:matchResult:export', '#', 'admin', sysdate(), '', null, '');

insert ignore into sys_role_menu (role_id, menu_id)
select r.role_id, m.menu_id
from sys_role r
cross join sys_menu m
where r.role_key = 'common'
  and m.menu_id between 2100 and 2199;
