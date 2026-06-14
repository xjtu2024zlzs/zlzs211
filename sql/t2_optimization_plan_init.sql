-- Two-subtask optimization plan initialization for t2_ tables.
-- Execute in the business database used by ruoyi-system-dev.yml, usually `ry-cloud`.
-- This script only updates catalogs; it does not delete existing task data.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS t2_design_objective_catalog (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  discipline varchar(50) NOT NULL COMMENT '学科',
  discipline_name varchar(80) NOT NULL COMMENT '学科名称',
  item_type varchar(20) NOT NULL COMMENT '条目类型：objective/constraint',
  item_code varchar(80) NOT NULL COMMENT '条目编码',
  item_name varchar(200) NOT NULL COMMENT '条目名称',
  direction varchar(30) DEFAULT NULL COMMENT '优化方向或约束关系',
  default_weight int(3) DEFAULT 50 COMMENT '默认权重',
  default_limit_value varchar(100) DEFAULT NULL COMMENT '默认阈值',
  unit varchar(50) DEFAULT NULL COMMENT '单位',
  sort_order int(4) DEFAULT 0 COMMENT '排序',
  status char(1) DEFAULT '0' COMMENT '状态：0正常 1停用',
  remark varchar(500) DEFAULT NULL COMMENT '说明',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  update_by varchar(64) DEFAULT '',
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_t2_doc_catalog_code (item_code),
  KEY idx_t2_doc_catalog_discipline (discipline),
  KEY idx_t2_doc_catalog_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设计任务目标约束候选库';

CREATE TABLE IF NOT EXISTS t2_design_variable_catalog (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  discipline varchar(50) NOT NULL COMMENT '学科',
  discipline_name varchar(80) NOT NULL COMMENT '学科名称',
  subtask_code varchar(80) DEFAULT NULL COMMENT '建议关联子任务编码',
  variable_code varchar(80) NOT NULL COMMENT '变量编码',
  variable_name varchar(200) NOT NULL COMMENT '变量名称',
  variable_type varchar(30) DEFAULT 'continuous' COMMENT '变量类型：continuous/discrete/enum',
  default_value varchar(100) DEFAULT NULL COMMENT '默认值',
  lower_bound varchar(100) DEFAULT NULL COMMENT '下限',
  upper_bound varchar(100) DEFAULT NULL COMMENT '上限',
  step_value varchar(100) DEFAULT NULL COMMENT '步长',
  unit varchar(50) DEFAULT NULL COMMENT '单位',
  sort_order int(4) DEFAULT 0 COMMENT '排序',
  status char(1) DEFAULT '0' COMMENT '状态：0正常 1停用',
  remark varchar(500) DEFAULT NULL COMMENT '说明',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  update_by varchar(64) DEFAULT '',
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_t2_dvc_code (variable_code),
  KEY idx_t2_dvc_discipline (discipline),
  KEY idx_t2_dvc_subtask (subtask_code),
  KEY idx_t2_dvc_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设计变量候选库';

UPDATE t2_design_variable_catalog
SET status = '1',
    update_by = 'admin',
    update_time = SYSDATE()
WHERE variable_code NOT IN (
  'PIPE_L1', 'PIPE_L2', 'PIPE_BEND_RADIUS', 'PIPE_THETA_1', 'PIPE_THETA_2',
  'CABLE_ROUTE_SIDE', 'CABLE_PIPE_CLEARANCE', 'CABLE_OFFSET',
  'CABLE_BEND_RADIUS', 'CLAMP_SPACING', 'SERVICE_MARGIN'
);

INSERT INTO t2_design_variable_catalog
  (discipline, discipline_name, subtask_code, variable_code, variable_name, variable_type, default_value, lower_bound, upper_bound, step_value, unit, sort_order, status, remark, create_by, create_time)
VALUES
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_L1', 'L1 第一段直管长度', 'continuous', '300', '50', '550', '1', 'mm', 10, '0', '液压弯管抗冲击优化几何变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_L2', 'L2 第二段直管长度', 'continuous', '150', '50', '300', '1', 'mm', 20, '0', '液压弯管抗冲击优化几何变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_BEND_RADIUS', 'R 两处弯管圆角半径', 'continuous', '30', '20', '80', '1', 'mm', 30, '0', '液压弯管抗冲击优化几何变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_THETA_1', 'θ1 第一个弯角弯曲角度', 'continuous', '110', '30', '150', '1', 'deg', 40, '0', '液压弯管抗冲击优化几何变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_THETA_2', 'θ2 第二个弯角弯曲角度', 'continuous', '120', '30', '150', '1', 'deg', 50, '0', '液压弯管抗冲击优化几何变量', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CABLE_ROUTE_SIDE', '线缆布置侧别', 'enum', 'upper', 'upper', 'outer', '', '', 60, '0', '可选 upper/lower/inner/outer', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CABLE_PIPE_CLEARANCE', '线缆与液压管最小隔离距离', 'continuous', '40', '20', '80', '1', 'mm', 70, '0', '线缆与液压管保持安全距离', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CABLE_OFFSET', '线缆相对液压管中心线偏移距离', 'continuous', '60', '30', '120', '1', 'mm', 80, '0', '围绕液压管路径的偏移布置参数', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CABLE_BEND_RADIUS', '线缆最小弯曲半径', 'continuous', '50', '30', '120', '1', 'mm', 90, '0', '满足线缆弯曲半径要求', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'CLAMP_SPACING', '线夹/管夹布置间距', 'continuous', '180', '100', '250', '5', 'mm', 100, '0', '控制线夹和管夹支撑间距', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'SERVICE_MARGIN', '检修操作预留空间', 'continuous', '40', '30', '100', '1', 'mm', 110, '0', '保证维护和装配操作空间', 'admin', SYSDATE())
ON DUPLICATE KEY UPDATE
  discipline = VALUES(discipline),
  discipline_name = VALUES(discipline_name),
  subtask_code = VALUES(subtask_code),
  variable_name = VALUES(variable_name),
  variable_type = VALUES(variable_type),
  default_value = VALUES(default_value),
  lower_bound = VALUES(lower_bound),
  upper_bound = VALUES(upper_bound),
  step_value = VALUES(step_value),
  unit = VALUES(unit),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  remark = VALUES(remark),
  update_by = 'admin',
  update_time = SYSDATE();

UPDATE t2_design_objective_catalog
SET status = '1',
    update_by = 'admin',
    update_time = SYSDATE()
WHERE item_code IN (
  'LAY_LENGTH_MIN', 'LAY_INTERFERENCE_MIN', 'LAY_CLEARANCE_LIMIT',
  'LAY_PIPE_CABLE_DISTANCE', 'LAY_FORBIDDEN_ZONE', 'LAY_CLAMP_INTERVAL',
  'LAY_BEND_RADIUS_LIMIT', 'LAY_PIPE_ENDPOINT_FIXED',
  'LAY_PIPE_HORIZONTAL_SPAN', 'LAY_PIPE_VERTICAL_SPAN'
);

INSERT INTO t2_design_objective_catalog
  (discipline, discipline_name, item_type, item_code, item_name, direction, default_weight, default_limit_value, unit, sort_order, status, remark, create_by, create_time)
VALUES
  ('hydraulic', '液压工程师', 'objective', 'HYD_STRESS_MIN', '最大等效应力最小', 'min', 50, '', 'MPa', 10, '0', '降低液压管路冲击工况下应力水平', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'objective', 'HYD_DEFORMATION_MIN', '最大变形量最小', 'min', 50, '', 'mm', 20, '0', '降低液压管路变形', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_STRESS_LIMIT', '最大等效应力不超过屈服强度', '<=', 50, '', 'MPa', 30, '0', '满足液压管路强度约束', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_DEFORMATION_LIMIT', '最大变形量不超过允许变形', '<=', 50, '', 'mm', 40, '0', '满足液压管路变形约束', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_MIN_BEND_RADIUS', '液压管弯曲半径不小于下限', '>=', 50, '20', 'mm', 50, '0', '弯曲半径 R 不小于 20 mm', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'objective', 'LAY_INTERFERENCE_RISK_MIN', '线缆与液压管干涉风险最小', 'min', 50, '', 'risk', 60, '0', '降低线缆与液压管、结构件、运动件的干涉风险', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'objective', 'LAY_CABLE_LENGTH_MIN', '线缆路径长度最小', 'min', 50, '', 'm', 70, '0', '在满足避让条件下缩短线缆路径', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'objective', 'LAY_MAINTAINABILITY_MAX', '维护可达性最大', 'max', 50, '', 'score', 80, '0', '提升检查、维护与更换便利性', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'objective', 'LAY_COMPACTNESS_MAX', '布局紧凑度最大', 'max', 50, '', 'score', 90, '0', '提升局部管线布置紧凑性', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_PIPE_CLEARANCE_LIMIT', '线缆与液压管保持安全间距', '>=', 50, '40', 'mm', 100, '0', 'clearance >= CABLE_PIPE_CLEARANCE', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_FORBIDDEN_ZONE_AVOID', '线缆不得穿越结构禁布区域', 'avoid', 50, '', '', 110, '0', '避让结构禁布区域', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_DOOR_ENVELOPE_AVOID', '线缆不得进入舱门运动包络', 'avoid', 50, '', '', 120, '0', '避让舱门运动包络', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_CABLE_BEND_RADIUS_LIMIT', '线缆弯曲半径满足要求', '>=', 50, '50', 'mm', 130, '0', 'CABLE_BEND_RADIUS 满足线缆弯曲要求', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_CLAMP_SPACING_LIMIT', '线夹间距不超过上限', '<=', 50, '250', 'mm', 140, '0', 'CLAMP_SPACING <= 250 mm', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_SERVICE_MARGIN_LIMIT', '检修空间满足要求', '>=', 50, '30', 'mm', 150, '0', 'SERVICE_MARGIN >= 30 mm', 'admin', SYSDATE())
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
