-- 数字卷宗详情演示补充数据：液压供压管路组件下级零件
-- 适用 MySQL 8.0，运行库：ry-cloud

USE `ry-cloud`;

SET @demo_aircraft_id = 'b0000001-0001-4001-8001-000000000001';
SET @demo_root_id = (
  SELECT id
  FROM t1_aircraft_bom_node
  WHERE aircraft_id = @demo_aircraft_id
    AND parent_id IS NULL
  ORDER BY node_level, id
  LIMIT 1
);
SET @hyd_system_id = 'f1000031-0031-4031-8031-000000000031';
SET @hyd_subsystem_id = 'f1000032-0032-4032-8032-000000000032';
SET @hyd_equipment_id = 'f1000033-0033-4033-8033-000000000033';

INSERT INTO t1_aircraft_bom_node (
    id, parent_id, aircraft_id, node_level, node_type,
    part_number, part_name, serial_number, batch_number, manufacturer, cage_code,
    quantity, unit, position_code, position_desc, ata_chapter,
    install_date, install_tsn, install_csn,
    is_active, is_serialized, is_rotable, is_expendable,
    tsn_fh, tsn_fc, created_by, updated_by, remark
) VALUES
(
    @hyd_system_id,
    @demo_root_id,
    @demo_aircraft_id,
    2, 'SYSTEM',
    'SYS-29', '液压系统',
    NULL, NULL, 'COMAC', 'COMAC',
    1, 'SET', 'ATA-29', '整机液压能源、供压、回油和监控系统', '29',
    '2026-02-20', 0, 0,
    1, 0, 0, 0,
    1280.50, 892, 'system', 'system', '新层级演示系统：液压系统'
),
(
    @hyd_subsystem_id,
    @hyd_system_id,
    @demo_aircraft_id,
    3, 'SUBSYSTEM',
    'SUBSYS-HYD-MLG', '起落架液压子系统',
    NULL, NULL, 'COMAC', 'COMAC',
    1, 'SET', 'ATA-29-MLG', '面向起落架收放、锁定和刹车转弯接口供压', '29',
    '2026-02-20', 0, 0,
    1, 0, 0, 0,
    1280.50, 892, 'system', 'system', '新层级演示子系统：起落架液压子系统'
),
(
    @hyd_equipment_id,
    @hyd_subsystem_id,
    @demo_aircraft_id,
    4, 'EQUIPMENT',
    'EQP-HYD-MLG-SUPPLY', '主起液压供压设备',
    'EQP-HYD-MLG-SUPPLY-2026-0001', 'EQP-HYD-20260218', 'AVIC Fluid Systems', 'AVFS',
    1, 'SET', 'MLG-HYD-SUPPLY', '主起舱液压供压功能设备，承载供压管路组件', '29',
    '2026-02-20', 0, 0,
    1, 1, 0, 0,
    1280.50, 892, 'system', 'system', '新层级演示设备：主起液压供压设备'
)
ON DUPLICATE KEY UPDATE
    parent_id = VALUES(parent_id),
    aircraft_id = VALUES(aircraft_id),
    node_level = VALUES(node_level),
    node_type = VALUES(node_type),
    part_number = VALUES(part_number),
    part_name = VALUES(part_name),
    serial_number = VALUES(serial_number),
    batch_number = VALUES(batch_number),
    manufacturer = VALUES(manufacturer),
    cage_code = VALUES(cage_code),
    quantity = VALUES(quantity),
    unit = VALUES(unit),
    position_code = VALUES(position_code),
    position_desc = VALUES(position_desc),
    ata_chapter = VALUES(ata_chapter),
    install_date = VALUES(install_date),
    install_tsn = VALUES(install_tsn),
    install_csn = VALUES(install_csn),
    is_active = VALUES(is_active),
    is_serialized = VALUES(is_serialized),
    is_rotable = VALUES(is_rotable),
    is_expendable = VALUES(is_expendable),
    tsn_fh = VALUES(tsn_fh),
    tsn_fc = VALUES(tsn_fc),
    updated_by = VALUES(updated_by),
    remark = VALUES(remark);

UPDATE t1_aircraft_bom_node
SET parent_id = @hyd_equipment_id,
    node_level = 5,
    node_type = 'COMPONENT',
    position_code = 'MLG-HYD-SUPPLY-PKG',
    position_desc = '主起液压供压设备内的管路组件',
    ata_chapter = '29',
    updated_by = 'system',
    remark = '新层级演示组件：液压供压管路组件'
WHERE aircraft_id = @demo_aircraft_id
  AND part_number = 'HYD-MLG-PKG-01';

SET @hyd_component_id = (
  SELECT id
  FROM t1_aircraft_bom_node
  WHERE aircraft_id = @demo_aircraft_id
    AND part_number = 'HYD-MLG-PKG-01'
  LIMIT 1
);

UPDATE t1_aircraft_bom_node
SET parent_id = @hyd_component_id,
    node_level = 6,
    node_type = 'PART',
    serial_number = 'HT-MLG-32A-2026-0042',
    batch_number = 'HT-L20260218-A',
    manufacturer = 'AVIC Fluid Systems',
    cage_code = 'AVFS',
    quantity = 1,
    unit = 'EA',
    position_desc = '主起液压供压设备内，供压管路组件 A 路由',
    ata_chapter = '29',
    install_date = '2026-02-20',
    install_tsn = 0,
    install_csn = 0,
    tsn_fh = 1280.50,
    tsn_fc = 892,
    is_serialized = 1,
    updated_by = 'system',
    remark = '卷宗详情演示重点零件：供压弯管全生命周期数据'
WHERE aircraft_id = @demo_aircraft_id
  AND (id = 'f1000006-0006-4006-8006-000000000006' OR part_number = 'HYD-TUBE-MLG-32A');

INSERT INTO t1_aircraft_bom_node (
    id, parent_id, aircraft_id, node_level, node_type,
    part_number, part_name, serial_number, batch_number, manufacturer, cage_code,
    quantity, unit, position_code, position_desc, ata_chapter,
    install_date, install_tsn, install_csn,
    is_active, is_serialized, is_rotable, is_expendable,
    tsn_fh, tsn_fc, created_by, updated_by, remark
) VALUES
(
    'f1000021-0021-4021-8021-000000000021',
    @hyd_component_id,
    @demo_aircraft_id,
    6, 'PART',
    'HYD-TUBE-MLG-32B', '主起液压回油弯管',
    'HT-MLG-32B-2026-0051', 'HT-L20260218-B', 'AVIC Fluid Systems', 'AVFS',
    2, 'EA', 'MLG-HYD-RTN', '主起舱回油管路，左右对称布置', '32',
    '2026-02-20', 0, 0,
    1, 1, 0, 0,
    1280.50, 892, 'system', 'system', '管路组件演示下级零件：回油弯管'
),
(
    'f1000022-0022-4022-8022-000000000022',
    @hyd_component_id,
    @demo_aircraft_id,
    6, 'PART',
    'HYD-FIT-TEE-01', '液压三通接头',
    'FIT-TEE-2026-0318', 'FIT-TEE-20260218-03', 'AVIC Fluid Systems', 'AVFS',
    3, 'EA', 'MLG-HYD-FIT', '供压与回油管路转接三通', '32',
    '2026-02-20', 0, 0,
    1, 1, 0, 0,
    1280.50, 892, 'system', 'system', '管路组件演示下级零件：三通接头'
),
(
    'f1000023-0023-4023-8023-000000000023',
    @hyd_component_id,
    @demo_aircraft_id,
    6, 'PART',
    'HYD-CLAMP-MLG-01', '液压管路固定卡箍',
    'CLAMP-MLG-2026-0088', 'CLAMP-MLG-20260218-08', 'AVIC Fluid Systems', 'AVFS',
    8, 'EA', 'MLG-HYD-CLAMP', '主起舱管路固定点 1 至 8', '32',
    '2026-02-20', 0, 0,
    1, 1, 0, 0,
    1280.50, 892, 'system', 'system', '管路组件演示下级零件：固定卡箍'
),
(
    'f1000024-0024-4024-8024-000000000024',
    @hyd_component_id,
    @demo_aircraft_id,
    6, 'CONSUMABLE',
    'HYD-SEAL-032', '扩口接头密封圈',
    'SEAL-032-2026-0128', 'SEAL-FKM-20260128-32', 'AVIC Fluid Sealing Plant', 'AVSP',
    12, 'EA', 'MLG-HYD-SEAL', '管路扩口端密封件', '32',
    '2026-02-20', 0, 0,
    1, 0, 0, 1,
    1280.50, 892, 'system', 'system', '管路组件演示下级件：密封圈'
),
(
    'f1000025-0025-4025-8025-000000000025',
    @hyd_component_id,
    @demo_aircraft_id,
    6, 'CONSUMABLE',
    'HYD-SLEEVE-MLG-01', '管路防磨护套',
    'SLEEVE-MLG-2026-0041', 'SLEEVE-PTFE-20260205-04', 'AVIC Fluid Systems', 'AVFS',
    4, 'EA', 'MLG-HYD-SLEEVE', '结构边缘和卡箍邻近区防磨保护', '32',
    '2026-02-20', 0, 0,
    1, 0, 0, 1,
    1280.50, 892, 'system', 'system', '管路组件演示下级件：防磨护套'
)
ON DUPLICATE KEY UPDATE
    parent_id = VALUES(parent_id),
    aircraft_id = VALUES(aircraft_id),
    node_level = VALUES(node_level),
    node_type = VALUES(node_type),
    part_number = VALUES(part_number),
    part_name = VALUES(part_name),
    serial_number = VALUES(serial_number),
    batch_number = VALUES(batch_number),
    manufacturer = VALUES(manufacturer),
    cage_code = VALUES(cage_code),
    quantity = VALUES(quantity),
    unit = VALUES(unit),
    position_code = VALUES(position_code),
    position_desc = VALUES(position_desc),
    ata_chapter = VALUES(ata_chapter),
    install_date = VALUES(install_date),
    install_tsn = VALUES(install_tsn),
    install_csn = VALUES(install_csn),
    is_active = VALUES(is_active),
    is_serialized = VALUES(is_serialized),
    is_rotable = VALUES(is_rotable),
    is_expendable = VALUES(is_expendable),
    tsn_fh = VALUES(tsn_fh),
    tsn_fc = VALUES(tsn_fc),
    updated_by = VALUES(updated_by),
    remark = VALUES(remark);
