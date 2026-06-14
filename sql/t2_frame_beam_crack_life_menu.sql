-- Project22 / frame beam crack life prediction menu and permissions.
-- Parent menu 2600 is the existing "设计制造协同优化平台".
-- Adjust IDs if your sys_menu already uses 2630-2644.

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, update_by, update_time, remark)
VALUES
  (2630, '任务协同', 2600, 2, 'task-collaboration', NULL, '', '',
   1, 0, 'M', '0', '0', '', 'guide', 'admin', SYSDATE(), '', NULL, 'Project22 grouped menu'),
  (2631, '模型分析', 2600, 4, 'model-analysis', NULL, '', '',
   1, 0, 'M', '0', '0', '', 'component', 'admin', SYSDATE(), '', NULL, 'Project22 grouped menu'),
  (2632, '验证与决策', 2600, 5, 'verification-decision', NULL, '', '',
   1, 0, 'M', '0', '0', '', 'checkbox', 'admin', SYSDATE(), '', NULL, 'Project22 grouped menu'),
  (2633, '寿命预测评估', 2631, 2, 'life-prediction', 'designtask/frameBeam/life-prediction', '', '',
   1, 0, 'C', '0', '0', 'designtask:framebeam:predict', 'chart', 'admin', SYSDATE(), '', NULL, 'Frame beam life prediction'),
  (2634, '决策建议确认', 2632, 2, 'decision-advice', 'designtask/frameBeam/decision-advice', '', '',
   1, 0, 'C', '0', '0', 'designtask:framebeam:advice', 'checkbox', 'admin', SYSDATE(), '', NULL, 'Frame beam maintenance advice')
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  parent_id = VALUES(parent_id),
  order_num = VALUES(order_num),
  path = VALUES(path),
  component = VALUES(component),
  menu_type = VALUES(menu_type),
  visible = VALUES(visible),
  status = VALUES(status),
  perms = VALUES(perms),
  icon = VALUES(icon),
  update_time = SYSDATE();

-- Optional grouping for existing design-platform pages.
-- Keep the original menu IDs, only move them under the new grouped menus.
UPDATE sys_menu SET parent_id = 2630, order_num = 1, update_time = SYSDATE()
WHERE menu_id = 2602 AND menu_name = '协同机制生成';

UPDATE sys_menu SET parent_id = 2630, order_num = 2, update_time = SYSDATE()
WHERE menu_id = 2603 AND menu_name = '目标约束选择';

UPDATE sys_menu SET parent_id = 2631, order_num = 1, update_time = SYSDATE()
WHERE menu_id = 2604 AND menu_name = '模型解耦求解';

UPDATE sys_menu SET parent_id = 2632, order_num = 1, update_time = SYSDATE()
WHERE menu_id = 2605 AND menu_name = '仿真验证确认';

INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, update_by, update_time, remark)
VALUES
  (2640, '框梁裂纹输入', 2633, 1, '', '', '', '', 1, 0, 'F', '0', '0',
   'designtask:framebeam:input', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2641, '框梁寿命预测', 2633, 2, '', '', '', '', 1, 0, 'F', '0', '0',
   'designtask:framebeam:predict', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2642, '框梁维修建议', 2634, 1, '', '', '', '', 1, 0, 'F', '0', '0',
   'designtask:framebeam:advice', '#', 'admin', SYSDATE(), '', NULL, ''),
  (2643, '框梁专家审批', 2634, 2, '', '', '', '', 1, 0, 'F', '0', '0',
   'designtask:framebeam:approve', '#', 'admin', SYSDATE(), '', NULL, '')
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  parent_id = VALUES(parent_id),
  order_num = VALUES(order_num),
  perms = VALUES(perms),
  update_time = SYSDATE();
