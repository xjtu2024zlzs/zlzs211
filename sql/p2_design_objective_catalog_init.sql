-- 目标与约束候选库初始化脚本
-- 执行位置：ruoyi-cloud 使用的业务数据库
-- 说明：该表用于按角色/学科维护前端可选择的目标及约束目录。

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS p2_design_objective_catalog (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  discipline varchar(50) NOT NULL COMMENT '学科：structure/layout/aero/hydraulic/manufacturing',
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
  UNIQUE KEY uk_p2_doc_catalog_code (item_code),
  KEY idx_p2_doc_catalog_discipline (discipline),
  KEY idx_p2_doc_catalog_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设计任务目标约束候选库';

UPDATE p2_design_objective_catalog
SET status = '1',
    update_by = 'admin',
    update_time = SYSDATE()
WHERE item_code IN (
  'STR_WEIGHT', 'STR_STIFFNESS', 'STR_STRESS_CONCENTRATION', 'STR_MAX_STRESS', 'STR_SAFETY_FACTOR', 'STR_THICKNESS_RANGE',
  'LAY_PIPE_LENGTH', 'LAY_SPACE_OCCUPANCY', 'LAY_MAINTAINABILITY', 'LAY_MIN_CLEARANCE', 'LAY_BEND_LIMIT',
  'AERO_DRAG_DISTURBANCE', 'AERO_DRAG_COEFFICIENT', 'AERO_MOTION_BOUNDARY', 'AERO_GAP_LIMIT',
  'HYD_IMPACT_RESISTANCE', 'HYD_PRESSURE_DROP', 'HYD_RELIABILITY', 'HYD_BEND_RADIUS', 'HYD_PIPE_DIAMETER', 'HYD_IMPACT_LOAD',
  'MFG_ASSEMBLY_COST', 'MFG_PROCESS_STABILITY', 'MFG_MAINTENANCE_CYCLE', 'MFG_PROCESS_LIMIT', 'MFG_INTERFACE_COMPAT'
);

INSERT INTO p2_design_objective_catalog
  (discipline, discipline_name, item_type, item_code, item_name, direction, default_weight, default_limit_value, unit, sort_order, status, remark, create_by, create_time)
VALUES
  ('structure', '结构工程师', 'objective', 'STR_SPACE_SUPPORT_MAX', '支撑与安装空间合理性最大', 'max', 50, '', 'score', 10, '0', '提升结构支撑、安装与维护空间的综合合理性', 'admin', SYSDATE()),
  ('structure', '结构工程师', 'objective', 'STR_DEFORMATION_RISK_MIN', '管线变形碰撞风险最小', 'min', 50, '', 'risk', 20, '0', '降低结构变形后管线与结构件发生碰撞的风险', 'admin', SYSDATE()),
  ('structure', '结构工程师', 'constraint', 'STR_FORBIDDEN_ZONE', '结构禁布区域不可穿越', 'avoid', 50, '', '', 30, '0', '避让结构禁布区域', 'admin', SYSDATE()),
  ('structure', '结构工程师', 'constraint', 'STR_INTERFACE_FIXED', '铰链、锁机构、作动器接口位置不可更改', 'fixed', 50, '', '', 40, '0', '保持关键结构接口位置稳定', 'admin', SYSDATE()),
  ('structure', '结构工程师', 'constraint', 'STR_PIPE_CLEARANCE', '管道最大变形后不得碰撞结构件', 'avoid', 50, '', '', 50, '0', '满足结构与管线间隙约束', 'admin', SYSDATE()),
  ('structure', '结构工程师', 'constraint', 'STR_CLAMP_SUPPORT_VALID', '卡箍支撑点结构强度满足要求', 'meet', 50, '', '', 60, '0', '卡箍支撑点需要满足强度与安装要求', 'admin', SYSDATE()),

  ('layout', '布局工程师', 'objective', 'LAY_LENGTH_MIN', '线缆与管路总长度最小', 'min', 50, '', 'm', 10, '0', '缩短线缆与管路路径长度', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'objective', 'LAY_INTERFERENCE_MIN', '线缆、管路、结构干涉风险最小', 'min', 50, '', 'risk', 20, '0', '降低多系统布置干涉风险', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'objective', 'LAY_MAINTAINABILITY_MAX', '维护可达性最大', 'max', 50, '', 'score', 30, '0', '提升检查、维护与更换便利性', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_CLEARANCE_LIMIT', '管线与结构/运动件最小间隙', '>=', 50, '', 'mm', 40, '0', '满足安全间隙约束', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_PIPE_CABLE_DISTANCE', '液压管与线缆最小隔离距离', '>=', 50, '', 'mm', 50, '0', '满足管线隔离与安全布置要求', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_FORBIDDEN_ZONE', '禁布区域不可穿越', 'avoid', 50, '', '', 60, '0', '避让禁布区域', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_CLAMP_INTERVAL', '管夹/线夹间距不超过上限', '<=', 50, '', 'mm', 70, '0', '控制支撑间距', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_BEND_RADIUS_LIMIT', '管线弯曲半径满足下限', '>=', 50, '', 'mm', 80, '0', '满足管线弯曲半径约束', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_PIPE_ENDPOINT_FIXED', '液压弯管起点和终点固定', 'fixed', 50, '固定', '', 90, '0', '故障追因后的展示任务固定几何边界', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_PIPE_HORIZONTAL_SPAN', '液压弯管水平总长', '=', 50, '600', 'mm', 100, '0', '管道起终点水平距离固定为 600 mm', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'constraint', 'LAY_PIPE_VERTICAL_SPAN', '液压弯管竖直总高', '=', 50, '300', 'mm', 110, '0', '管道起终点竖直距离固定为 300 mm', 'admin', SYSDATE()),

  ('aero', '气动工程师', 'objective', 'AERO_ENVELOPE_IMPACT_MIN', '对舱门外形包络影响最小', 'min', 50, '', 'score', 10, '0', '降低内部布置对舱门外形包络的影响', 'admin', SYSDATE()),
  ('aero', '气动工程师', 'constraint', 'AERO_OUTER_ENVELOPE', '管线与附件不得超出舱门外形包络', '<=', 50, '', 'mm', 20, '0', '不得突破外形包络', 'admin', SYSDATE()),
  ('aero', '气动工程师', 'constraint', 'AERO_DOOR_GAP_CLEARANCE', '不影响舱门缝隙和开闭间隙', 'meet', 50, '', '', 30, '0', '满足舱门缝隙与开闭间隙要求', 'admin', SYSDATE()),

  ('hydraulic', '液压工程师', 'objective', 'HYD_STRESS_MIN', '最大等效应力最小', 'min', 50, '', 'MPa', 10, '0', '降低液压管路冲击工况下应力水平', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'objective', 'HYD_DEFORMATION_MIN', '最大变形量最小', 'min', 50, '', 'mm', 20, '0', '降低液压管路变形', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'objective', 'HYD_PRESSURE_DROP_MIN', '液压管路压降最小', 'min', 50, '', 'MPa', 30, '0', '降低液压系统局部压降', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_STRESS_LIMIT', '最大等效应力不超过屈服强度', '<=', 50, '', 'MPa', 40, '0', '满足液压管路强度约束', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_DEFORMATION_LIMIT', '最大变形量不超过允许变形', '<=', 50, '', 'mm', 50, '0', '满足变形约束', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_MIN_BEND_RADIUS', '液压管弯曲半径不小于下限', '>=', 50, '', 'mm', 60, '0', '满足弯管制造与使用约束', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_CLAMP_VALID', '卡箍不得断裂', 'false', 50, '', '', 70, '0', '卡箍支撑需要满足强度要求', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_PRESSURE_VELOCITY_INPUT', '入口压强和流速按时间历程输入', 'input', 50, '', '', 80, '0', '用于液压冲击仿真输入', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'constraint', 'HYD_VALVE_CLOSE_TIME', '阀门关闭/作动器卡滞时间作为冲击工况输入', 'input', 50, '', 's', 90, '0', '用于定义液压冲击工况', 'admin', SYSDATE()),

  ('manufacturing', '制造工程师', 'objective', 'MFG_PROCESS_COMPLEXITY_MIN', '管线制造加工复杂度最小', 'min', 50, '', 'score', 10, '0', '降低制造加工复杂度', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'objective', 'MFG_ASSEMBLY_EFFICIENCY_MAX', '装配效率最大', 'max', 50, '', 'score', 20, '0', '提升装配效率', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'objective', 'MFG_MAINTENANCE_ACCESS_MAX', '检修可达性最大', 'max', 50, '', 'score', 30, '0', '提升检修可达性', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'constraint', 'MFG_BEND_RADIUS_LIMIT', '弯曲半径满足制造下限', '>=', 50, '', 'mm', 40, '0', '满足制造工艺弯曲半径约束', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'constraint', 'MFG_BEND_ANGLE_RANGE', '弯曲角度满足加工范围', 'range', 50, '', 'deg', 50, '0', '满足弯曲角度加工范围', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'constraint', 'MFG_WALL_THICKNESS_LIMIT', '管道壁厚满足加工与强度要求', '>=', 50, '', 'mm', 60, '0', '满足壁厚制造和强度要求', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'constraint', 'MFG_CLAMP_INSTALLABLE', '管夹/线夹可安装', 'meet', 50, '', '', 70, '0', '满足夹具安装空间要求', 'admin', SYSDATE()),
  ('manufacturing', '制造工程师', 'constraint', 'MFG_TOOL_ACCESS', '工具操作空间满足要求', 'meet', 50, '', '', 80, '0', '满足装配和维护工具可达性要求', 'admin', SYSDATE())
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
