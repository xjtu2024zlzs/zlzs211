-- 设计变量库与任务变量选择初始化脚本
-- 执行位置：ruoyi-cloud 使用的业务数据库

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS p2_design_variable_catalog (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  discipline varchar(50) NOT NULL COMMENT '学科：structure/layout/aero/hydraulic/manufacturing',
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
  UNIQUE KEY uk_p2_dvc_code (variable_code),
  KEY idx_p2_dvc_discipline (discipline),
  KEY idx_p2_dvc_subtask (subtask_code),
  KEY idx_p2_dvc_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设计变量候选库';

CREATE TABLE IF NOT EXISTS p2_design_task_variable_selection (
  id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  task_id bigint(20) NOT NULL COMMENT '任务ID',
  discipline varchar(50) NOT NULL COMMENT '学科',
  subtask_code varchar(80) DEFAULT NULL COMMENT '关联子任务编码',
  variable_code varchar(80) NOT NULL COMMENT '变量编码',
  variable_name varchar(200) NOT NULL COMMENT '变量名称',
  variable_type varchar(30) DEFAULT 'continuous' COMMENT '变量类型',
  initial_value varchar(100) DEFAULT NULL COMMENT '初始值',
  lower_bound varchar(100) DEFAULT NULL COMMENT '下限',
  upper_bound varchar(100) DEFAULT NULL COMMENT '上限',
  step_value varchar(100) DEFAULT NULL COMMENT '步长',
  unit varchar(50) DEFAULT NULL COMMENT '单位',
  remark varchar(500) DEFAULT NULL COMMENT '说明',
  create_by varchar(64) DEFAULT '',
  create_time datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_p2_dtvs_task (task_id),
  KEY idx_p2_dtvs_discipline (discipline),
  KEY idx_p2_dtvs_variable (variable_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务设计变量选择';

UPDATE p2_design_variable_catalog
SET status = '1',
    update_by = 'admin',
    update_time = SYSDATE()
WHERE variable_code NOT IN ('PIPE_L1', 'PIPE_L2', 'PIPE_THETA_1', 'PIPE_THETA_2', 'PIPE_BEND_RADIUS');

INSERT INTO p2_design_variable_catalog
  (discipline, discipline_name, subtask_code, variable_code, variable_name, variable_type, default_value, lower_bound, upper_bound, step_value, unit, sort_order, status, remark, create_by, create_time)
VALUES
  ('layout', '布局工程师', 'cable_pipe_layout', 'PIPE_L1', 'L1 第一段直管长度', 'continuous', '300', '50', '550', '1', 'mm', 10, '0', '故障追因后确定的弯管路径几何关键变量', 'admin', SYSDATE()),
  ('layout', '布局工程师', 'cable_pipe_layout', 'PIPE_L2', 'L2 第二段直管长度', 'continuous', '150', '50', '300', '1', 'mm', 20, '0', '故障追因后确定的弯管路径几何关键变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_THETA_1', 'θ1 第一个弯角弯曲角度', 'continuous', '110', '30', '150', '1', 'deg', 30, '0', '故障追因后确定的抗冲击性能关键变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'hydraulic_impact', 'PIPE_THETA_2', 'θ2 第二个弯角弯曲角度', 'continuous', '120', '30', '150', '1', 'deg', 40, '0', '故障追因后确定的抗冲击性能关键变量', 'admin', SYSDATE()),
  ('hydraulic', '液压工程师', 'shared', 'PIPE_BEND_RADIUS', 'R 两处弯管圆角半径', 'continuous', '30', '20', '80', '1', 'mm', 50, '0', '共享变量，同时影响抗冲击性能和制造可行性', 'admin', SYSDATE())
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
