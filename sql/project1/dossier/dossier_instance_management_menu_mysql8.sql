-- Digital dossier instance management menu - MySQL 8.0
-- Runtime database: ry-cloud

USE `ry-cloud`;

UPDATE sys_menu SET order_num = 2 WHERE menu_id = 2030;
UPDATE sys_menu SET order_num = 3 WHERE menu_id = 2031;
UPDATE sys_menu SET order_num = 4 WHERE menu_id = 2032;

INSERT INTO sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) VALUES (
    2029, '卷宗实例管理', 2020, 1, 'instance', 'project1/dossier/instance/index', NULL, 'DossierInstance',
    1, 0, 'C', '0', '0', 'project1:dossier:instance:list', 'documentation',
    'system', NOW(), '', NULL, '卷宗实例管理'
)
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name),
    parent_id = VALUES(parent_id),
    order_num = VALUES(order_num),
    path = VALUES(path),
    component = VALUES(component),
    route_name = VALUES(route_name),
    is_frame = VALUES(is_frame),
    is_cache = VALUES(is_cache),
    menu_type = VALUES(menu_type),
    visible = VALUES(visible),
    status = VALUES(status),
    perms = VALUES(perms),
    icon = VALUES(icon),
    update_by = 'system',
    update_time = NOW(),
    remark = VALUES(remark);
