-- 起落架舱门设计制造协同优化平台第一版初始化脚本
-- 说明：在已存在若依 ry-cloud 库和 p2_design_task 表的基础上执行。

SET NAMES utf8mb4;

ALTER TABLE p2_design_task
  ADD COLUMN process_definition_id varchar(128) DEFAULT NULL COMMENT 'Flowable流程定义ID' AFTER collab_roles,
  ADD COLUMN process_instance_id varchar(128) DEFAULT NULL COMMENT 'Flowable流程实例ID' AFTER process_definition_id,
  ADD COLUMN current_flowable_task_id varchar(128) DEFAULT NULL COMMENT 'Flowable当前任务ID' AFTER process_instance_id,
  ADD COLUMN current_node_key varchar(100) DEFAULT NULL COMMENT '当前节点Key' AFTER current_flowable_task_id,
  ADD COLUMN current_node_name varchar(100) DEFAULT NULL COMMENT '当前节点名称' AFTER current_node_key,
  ADD COLUMN owner_user_id bigint(20) DEFAULT NULL COMMENT '任务负责人用户ID' AFTER current_node_name,
  ADD COLUMN owner_user_name varchar(100) DEFAULT NULL COMMENT '任务负责人名称' AFTER owner_user_id,
  ADD COLUMN structure_user_id bigint(20) DEFAULT NULL COMMENT '结构工程师用户ID' AFTER owner_user_name,
  ADD COLUMN layout_user_id bigint(20) DEFAULT NULL COMMENT '布局工程师用户ID' AFTER structure_user_id,
  ADD COLUMN aero_user_id bigint(20) DEFAULT NULL COMMENT '气动工程师用户ID' AFTER layout_user_id,
  ADD COLUMN hydraulic_user_id bigint(20) DEFAULT NULL COMMENT '液压工程师用户ID' AFTER aero_user_id,
  ADD COLUMN manufacturing_user_id bigint(20) DEFAULT NULL COMMENT '制造工程师用户ID' AFTER hydraulic_user_id,
  ADD COLUMN design_user_id bigint(20) DEFAULT NULL COMMENT '设计工程师用户ID' AFTER manufacturing_user_id,
  ADD COLUMN leader_user_id bigint(20) DEFAULT NULL COMMENT '审批领导用户ID' AFTER design_user_id;

CREATE TABLE IF NOT EXISTS p2_design_objective_constraint (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  task_id bigint(20) NOT NULL COMMENT '任务ID',
  discipline varchar(50) NOT NULL COMMENT '学科',
  item_type varchar(20) NOT NULL COMMENT 'objective/constraint',
  item_code varchar(80) NOT NULL COMMENT '条目编码',
  item_name varchar(200) NOT NULL COMMENT '条目名称',
  direction varchar(30) DEFAULT NULL COMMENT '方向或关系',
  weight int(3) DEFAULT NULL COMMENT '权重',
  limit_value varchar(100) DEFAULT NULL COMMENT '阈值',
  unit varchar(50) DEFAULT NULL COMMENT '单位',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_doc_task (task_id),
  KEY idx_doc_discipline (discipline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设计任务目标约束选择';

CREATE TABLE IF NOT EXISTS p2_design_conflict_check (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  task_id bigint(20) NOT NULL COMMENT '任务ID',
  passed char(1) DEFAULT '1' COMMENT '是否通过',
  score int(3) DEFAULT NULL COMMENT '校验评分',
  conflicts_json text COMMENT '冲突项JSON',
  suggestions_json text COMMENT '调整建议JSON',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_dcc_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目标约束冲突校验结果';

CREATE TABLE IF NOT EXISTS p2_design_subtask_solution (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  task_id bigint(20) NOT NULL COMMENT '任务ID',
  subtask_code varchar(80) NOT NULL COMMENT '子任务编码',
  subtask_name varchar(200) NOT NULL COMMENT '子任务名称',
  params_json text COMMENT '求解参数JSON',
  result_json text COMMENT '求解结果JSON',
  recommended_solution varchar(80) DEFAULT NULL COMMENT '推荐方案',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_dss_task (task_id),
  KEY idx_dss_subtask (subtask_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型解耦子任务求解结果';

CREATE TABLE IF NOT EXISTS p2_design_simulation_result (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  task_id bigint(20) NOT NULL COMMENT '任务ID',
  passed char(1) DEFAULT '1' COMMENT '是否通过',
  metrics_json text COMMENT '指标对比JSON',
  conclusion varchar(500) DEFAULT NULL COMMENT '仿真结论',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_dsr_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仿真验证结果';

CREATE TABLE IF NOT EXISTS p2_design_approval_record (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  task_id bigint(20) NOT NULL COMMENT '任务ID',
  approved char(1) DEFAULT '1' COMMENT '是否通过',
  comment varchar(1000) DEFAULT NULL COMMENT '审批意见',
  approve_by varchar(64) DEFAULT '',
  approve_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_dar_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='领导审批记录';

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
  .
  
  update_by varchar(64) DEFAULT '',
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标准资源';

-- 菜单ID如与现有菜单冲突，请调整 2600-2609。
INSERT INTO sys_menu VALUES (2600, '设计制造协同优化平台', '0', 5, 'design-platform', null, '', '', 1, 0, 'M', '0', '0', '', 'tree-table', 'admin', sysdate(), '', null, '协同优化平台目录')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);
INSERT INTO sys_menu VALUES (2601, '首页（任务看板）', '2600', 1, 'dashboard', 'designtask/dashboard/index', '', '', 1, 0, 'C', '0', '0', 'designtask:task:list', 'dashboard', 'admin', sysdate(), '', null, '')
ON DUPLICATE KEY UPDATE component = VALUES(component);
INSERT INTO sys_menu VALUES (2602, '协同机制生成', '2600', 2, 'mechanism', 'designtask/mechanism/index', '', '', 1, 0, 'C', '0', '0', 'designtask:task:add', 'guide', 'admin', sysdate(), '', null, '')
ON DUPLICATE KEY UPDATE component = VALUES(component);
INSERT INTO sys_menu VALUES (2603, '目标约束选择', '2600', 3, 'objective', 'designtask/objective/index', '', '', 1, 0, 'C', '0', '0', 'designtask:task:handle', 'checkbox', 'admin', sysdate(), '', null, '')
ON DUPLICATE KEY UPDATE component = VALUES(component);
INSERT INTO sys_menu VALUES (2604, '模型解耦求解', '2600', 4, 'solve', 'designtask/solve/index', '', '', 1, 0, 'C', '0', '0', 'designtask:task:handle', 'component', 'admin', sysdate(), '', null, '')
ON DUPLICATE KEY UPDATE component = VALUES(component);
INSERT INTO sys_menu VALUES (2605, '仿真验证确认', '2600', 5, 'simulation', 'designtask/simulation/index', '', '', 1, 0, 'C', '0', '0', 'designtask:task:approve', 'chart', 'admin', sysdate(), '', null, '')
ON DUPLICATE KEY UPDATE component = VALUES(component);

-- 如 Nacos 网关配置还没有 designtask 路由，请在 ruoyi-gateway-dev.yml 的 routes 下加入：
-- - id: ruoyi-designtask1
--   uri: lb://ruoyi-designtask1
--   predicates:
--     - Path=/designtask/**
--   filters:
--     - StripPrefix=1

