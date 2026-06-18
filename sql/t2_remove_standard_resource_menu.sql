-- Remove the Standard Resource Management menu from the design platform.
-- This only removes menu entries and role-menu grants. Resource data tables are kept intact.

SET NAMES utf8mb4;

DELETE FROM sys_role_menu
WHERE menu_id IN (2614, 2615)
   OR menu_id IN (
     SELECT menu_id
     FROM sys_menu
     WHERE perms IN ('designtask:resource:list', 'designtask:resource:add')
        OR component = 'designtask/resource/index'
   );

DELETE FROM sys_menu
WHERE menu_id IN (2615, 2614)
   OR perms IN ('designtask:resource:add', 'designtask:resource:list')
   OR component = 'designtask/resource/index';
