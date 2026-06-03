-- 删除层级对象旧模板示例数据
-- 这些 ID 来自旧版“层级对象导入模板”的示例行。
-- 执行顺序按外键依赖从子表到父表删除。

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM part_instances
WHERE part_instance_id = 'PI001'
   OR part_template_id = 'PT001';

DELETE FROM part_templates
WHERE part_template_id = 'PT001'
   OR part_number = 'LYB-Z-001';

DELETE FROM components
WHERE component_id = 'C001';

DELETE FROM equipments
WHERE equipment_id = 'E001';

DELETE FROM subsystems
WHERE subsystem_id = 'S001';

DELETE FROM aircraft
WHERE aircraft_id = 'A001';

SET FOREIGN_KEY_CHECKS = 1;
