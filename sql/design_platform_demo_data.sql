-- 起落架舱门协同优化平台演示数据与真实流程支撑脚本
-- 适用库：ry-cloud
-- 说明：
-- 1. 可重复执行。
-- 2. 目标约束指标先按起落架舱门多学科设计特点分配；后续可按你的图片指标精确替换。
-- 3. 真实 Flowable 流程模板由 ruoyi-flowable 启动后通过 /design/process/ensure 自动部署。

SET NAMES utf8mb4;

-- 一、业务任务表补充字段
DROP PROCEDURE IF EXISTS add_design_task_column_if_missing;
DELIMITER $$
CREATE PROCEDURE add_design_task_column_if_missing(
  IN p_table_name varchar(64),
  IN p_column_name varchar(64),
  IN p_column_def varchar(1000)
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND column_name = p_column_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN ', p_column_name, ' ', p_column_def);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CALL add_design_task_column_if_missing('p2_design_task', 'current_flowable_task_id', "varchar(128) DEFAULT NULL COMMENT 'Flowable当前待办任务ID' AFTER process_instance_id");
CALL add_design_task_column_if_missing('p2_design_task', 'manufacturing_user_id', "bigint(20) DEFAULT NULL COMMENT '制造工程师用户ID' AFTER hydraulic_user_id");

DROP PROCEDURE IF EXISTS add_design_task_column_if_missing;

-- 二、目标约束候选指标字典
CREATE TABLE IF NOT EXISTS p2_design_objective_catalog (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  discipline varchar(50) NOT NULL COMMENT '学科：structure/layout/aero/hydraulic/manufacturing',
  discipline_name varchar(80) NOT NULL COMMENT '学科名称',
  item_type varchar(20) NOT NULL COMMENT 'objective/constraint',
  item_code varchar(80) NOT NULL COMMENT '指标编码',
  item_name varchar(200) NOT NULL COMMENT '指标名称',
  direction varchar(30) DEFAULT NULL COMMENT '优化方向或约束关系',
  default_weight int(3) DEFAULT 50 COMMENT '默认权重',
  default_limit_value varchar(100) DEFAULT NULL COMMENT '默认阈值',
  unit varchar(50) DEFAULT NULL COMMENT '单位',
  sort_order int(4) DEFAULT 0 COMMENT '排序',
  status char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  remark varchar(500) DEFAULT NULL COMMENT '说明',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  update_by varchar(64) DEFAULT '',
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_doc_catalog_code (item_code),
  KEY idx_doc_catalog_discipline (discipline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设计任务目标约束候选指标字典';

INSERT INTO p2_design_objective_catalog
  (discipline, discipline_name, item_type, item_code, item_name, direction, default_weight, default_limit_value, unit, sort_order, status, remark, create_by, create_time)
VALUES
  ('structure', '结构工程师', 'objective', 'STR_WEIGHT', '舱门结构减重', 'min', 80, '', 'kg', 10, '0', '控制舱门结构件整体重量', 'admin', SYSDATE()),
  ('structure', '结构工程师', 'objective', 'STR_STIFFNESS', '提高舱门刚度', 'max', 70, '', 'N/mm', 20, '0', '提升舱门开启和受载工况下的刚度', 'admin', SYSDATE()),
  ('structure', '结构工程师', 'objective', 'STR_STRESS_CONCENTRATION', '降低应力集中', 'min', 75, '', 'MPa', 30, '0', '降低铰链、锁扣和加强筋局部应力集中', 'admin', SYSDATE()),
  ('structure', '结构工程师', 'constraint', 'STR_MAX_STRESS', '最大等效应力', '<=', 50, '材料许用应力', 'MPa', 40, '0', '结构强度约束', 'admin', SYSDATE()),
  ('structure', '结构工程师', 'constraint', 'STR_SAFETY_FACTOR', '最小安全系数', '>=', 50, '1.5', '', 50, '0', '满足强度安全裕度', 'admin', SYSDATE()),
  ('structure', '结构工程师', 'constraint', 'STR_THICKNESS_RANGE', '材料厚度范围', 'between', 50, '工艺允许范围', 'mm', 60, '0', '制造与结构共同约束', 'admin', SYSDATE()),

  ('layout', '布局工程师', 'objective', 'LAY_PIPE_LENGTH', '缩短管线路径长度', 'min', 75, '', 'm', 10, '0', '降低管路和线缆布置长度', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'objective', 'LAY_SPACE_OCCUPANCY', '降低空间占用', 'min', 70, '', '%', 20, '0', '提高舱门内部空间利用率', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'objective', 'LAY_MAINTAINABILITY', '提升维护可达性', 'max', 65, '', 'score', 30, '0', '便于检修和更换', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_MIN_CLEARANCE', '最小间隙', '>=', 50, '10', 'mm', 40, '0', '线缆、管路和结构之间的最小安全间隙', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_FORBIDDEN_ZONE', '禁布区域', 'avoid', 50, '避让舱门运动包络', '', 50, '0', '避让运动机构和维护通道', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_BEND_LIMIT', '线缆最小弯曲半径', '>=', 50, '规范值', 'mm', 60, '0', '满足线缆和软管布设规范', 'admin', SYSDATE()),

  ('aero', '气动工程师', 'objective', 'AERO_DRAG_DISTURBANCE', '降低外形扰动', 'min', 70, '', 'score', 10, '0', '降低舱门外形变化对流场的影响', 'admin', SYSDATE()),
  ('aero', '气动工程师', 'objective', 'AERO_DRAG_COEFFICIENT', '减少阻力影响', 'min', 75, '', 'Cd', 20, '0', '降低气动阻力增量', 'admin', SYSDATE()),
  ('aero', '气动工程师', 'constraint', 'AERO_OUTER_ENVELOPE', '舱门外形包络', '<=', 50, '设计外形包络', 'mm', 30, '0', '不得突破外形包络', 'admin', SYSDATE()),
  ('aero', '气动工程师', 'constraint', 'AERO_MOTION_BOUNDARY', '开闭运动边界', 'inside', 50, '运动包络', '', 40, '0', '舱门开闭过程不越界', 'admin', SYSDATE()),
  ('aero', '气动工程师', 'constraint', 'AERO_GAP_LIMIT', '气动间隙', '<=', 50, '3', 'mm', 50, '0', '控制外形缝隙和阶差', 'admin', SYSDATE()),

  ('hydraulic', '液压工程师', 'objective', 'HYD_IMPACT_RESISTANCE', '提高弯管抗冲击能力', 'max', 85, '', 'score', 10, '0', '液压弯管抗冲击优化子任务核心目标', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'objective', 'HYD_PRESSURE_DROP', '降低压降', 'min', 70, '', 'MPa', 20, '0', '降低液压系统局部压降', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'objective', 'HYD_RELIABILITY', '提升液压管路可靠性', 'max', 65, '', 'score', 30, '0', '提高振动和冲击载荷下可靠性', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_BEND_RADIUS', '弯管半径', '>=', 50, '规范值', 'mm', 40, '0', '液压弯管成形和抗冲击约束', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_PIPE_DIAMETER', '管径范围', 'between', 50, '系统允许范围', 'mm', 50, '0', '满足流量和布置约束', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_IMPACT_LOAD', '冲击载荷', '>=', 50, '任务载荷谱', 'g', 60, '0', '满足冲击载荷工况', 'admin', SYSDATE()),

  ('manufacturing', '制造工程师', 'objective', 'MFG_ASSEMBLY_COST', '降低制造装配成本', 'min', 70, '', 'score', 10, '0', '控制制造和装配成本', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'objective', 'MFG_PROCESS_STABILITY', '提升工艺稳定性', 'max', 65, '', 'score', 20, '0', '提高加工和装配一致性', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'objective', 'MFG_MAINTENANCE_CYCLE', '延长维护周期', 'max', 60, '', 'h', 30, '0', '综合考虑维护性', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'constraint', 'MFG_PROCESS_LIMIT', '制造工艺限制', 'meet', 50, '满足', '', 40, '0', '满足材料、加工和装配工艺限制', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'constraint', 'MFG_INTERFACE_COMPAT', '接口兼容性', 'meet', 50, '满足', '', 50, '0', '保持与已有接口兼容', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'constraint', 'MFG_TOOL_ACCESS', '装配工具可达性', '>=', 50, '80', 'score', 60, '0', '保证装配工具和检修空间', 'admin', SYSDATE())
ON DUPLICATE KEY UPDATE
  discipline = VALUES(discipline),
  discipline_name = VALUES(discipline_name),
  item_type = VALUES(item_type),
  item_name = VALUES(item_name),
  direction = VALUES(direction),
  default_weight = VALUES(default_weight),
  default_limit_value = VALUES(default_limit_value),
  unit = VALUES(unit),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  remark = VALUES(remark),
  update_by = 'admin',
  update_time = SYSDATE();

-- 三、演示资源数据
CREATE TABLE IF NOT EXISTS p2_design_resource (
  resource_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  resource_name varchar(200) NOT NULL COMMENT '资源名称',
  category varchar(80) DEFAULT NULL COMMENT '资源分类',
  version varchar(40) DEFAULT NULL COMMENT '版本',
  file_type varchar(40) DEFAULT NULL COMMENT '文件类型',
  file_path varchar(500) DEFAULT NULL COMMENT '文件路径',
  description varchar(500) DEFAULT NULL COMMENT '说明',
  status char(1) DEFAULT '0' COMMENT '状态',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  update_by varchar(64) DEFAULT '',
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标准资源';

INSERT INTO p2_design_resource(resource_id, resource_name, category, version, file_type, file_path, description, status, create_by, create_time)
VALUES
  (9001, '起落架舱门结构边界条件', '结构资源', 'v1.0', 'PDF', '/profile/design/resource/lgd_structure_boundary.pdf', '用于结构目标约束选择', '0', 'admin', SYSDATE()),
  (9002, '舱门管线路径禁布区域', '布局资源', 'v1.0', 'DWG', '/profile/design/resource/lgd_forbidden_zone.dwg', '用于线缆管路联合布局', '0', 'admin', SYSDATE()),
  (9003, '液压弯管冲击载荷谱', '液压资源', 'v1.0', 'XLSX', '/profile/design/resource/hydraulic_impact_load.xlsx', '用于液压弯管抗冲击优化', '0', 'admin', SYSDATE()),
  (9004, '气动外形包络约束', '气动资源', 'v1.0', 'PNG', '/profile/design/resource/aero_envelope.png', '用于气动约束确认', '0', 'admin', SYSDATE()),
  (9005, '制造装配工艺约束清单', '制造资源', 'v1.0', 'DOCX', '/profile/design/resource/manufacturing_constraints.docx', '用于制造目标约束选择', '0', 'admin', SYSDATE())
ON DUPLICATE KEY UPDATE
  resource_name = VALUES(resource_name),
  category = VALUES(category),
  version = VALUES(version),
  file_type = VALUES(file_type),
  file_path = VALUES(file_path),
  description = VALUES(description),
  status = VALUES(status),
  update_by = 'admin',
  update_time = SYSDATE();

