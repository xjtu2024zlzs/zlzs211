-- ============================================
-- 设计任务模块完整SQL（建表 + 菜单）
-- 执行顺序：先执行建表SQL，再执行菜单SQL
-- ============================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 第一部分：建表SQL（design_task系列）
-- ============================================

-- ----------------------------
-- 1、设计任务主表
-- ----------------------------
DROP TABLE IF EXISTS p2_design_task;
CREATE TABLE p2_design_task (
  task_id           BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '任务ID',
  task_name         VARCHAR(200)    NOT NULL                   COMMENT '任务名称',
  task_no           VARCHAR(50)     DEFAULT NULL               COMMENT '任务编号',
  task_type         VARCHAR(20)     NOT NULL                   COMMENT '任务类型（HYDRAULIC/STRUCTURE/LAYOUT）',
  template_id       BIGINT(20)      DEFAULT NULL               COMMENT '流程模板ID',
  priority          INT(2)          DEFAULT 2                  COMMENT '优先级（1低 2中 3高）',
  description       TEXT                                        COMMENT '问题描述',
  status            VARCHAR(20)     DEFAULT 'DRAFT'            COMMENT '任务状态（DRAFT待提交/APPROVED审批中/RUNNING进行中/COMPLETED已完成/CLOSED已关闭）',
  responsible_role  VARCHAR(50)     DEFAULT NULL               COMMENT '负责人角色',
  collab_roles      VARCHAR(200)    DEFAULT NULL               COMMENT '协同角色（逗号分隔）',
  create_by         VARCHAR(64)     DEFAULT ''                 COMMENT '创建者',
  create_time       DATETIME                                    COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''                 COMMENT '更新者',
  update_time       DATETIME                                    COMMENT '更新时间',
  del_flag          CHAR(1)         DEFAULT '0'                 COMMENT '删除标志（0存在 2删除）',
  PRIMARY KEY (task_id)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '设计任务主表';

-- ----------------------------
-- 2、流程模板表
-- ----------------------------
DROP TABLE IF EXISTS p2_design_template;
CREATE TABLE p2_design_template (
  template_id       BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '模板ID',
  template_name     VARCHAR(100)    NOT NULL                   COMMENT '模板名称',
  template_desc     VARCHAR(500)    DEFAULT NULL               COMMENT '模板描述',
  template_config   TEXT                                        COMMENT '模板配置JSON（节点列表）',
  is_default        CHAR(1)         DEFAULT '0'                COMMENT '是否默认模板（0否 1是）',
  status            CHAR(1)         DEFAULT '0'                COMMENT '状态（0正常 1停用）',
  create_by         VARCHAR(64)     DEFAULT ''                 COMMENT '创建者',
  create_time       DATETIME                                    COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''                 COMMENT '更新者',
  update_time       DATETIME                                    COMMENT '更新时间',
  remark            VARCHAR(500)    DEFAULT NULL               COMMENT '备注',
  PRIMARY KEY (template_id)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '流程模板表';

-- ----------------------------
-- 3、模板节点表
-- ----------------------------
DROP TABLE IF EXISTS p2_design_template_node;
CREATE TABLE p2_design_template_node (
  node_id           BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '节点ID',
  template_id       BIGINT(20)      NOT NULL                   COMMENT '模板ID',
  node_code         VARCHAR(50)     NOT NULL                   COMMENT '节点编码',
  node_name         VARCHAR(100)    NOT NULL                   COMMENT '节点名称',
  node_order        INT(4)          DEFAULT 0                  COMMENT '节点顺序',
  parallel_group    VARCHAR(50)     DEFAULT NULL               COMMENT '并行分组（同一分组内并行执行）',
  responsible_role  VARCHAR(50)     DEFAULT NULL               COMMENT '负责人角色',
  time_limit        INT(4)          DEFAULT NULL               COMMENT '时限（小时）',
  create_by         VARCHAR(64)     DEFAULT ''                 COMMENT '创建者',
  create_time       DATETIME                                    COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''                 COMMENT '更新者',
  update_time       DATETIME                                    COMMENT '更新时间',
  PRIMARY KEY (node_id),
  KEY idx_template_id (template_id),
  UNIQUE KEY uk_template_node (template_id, node_code)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '模板节点表';

-- ----------------------------
-- 4、节点协作角色表
-- ----------------------------
DROP TABLE IF EXISTS p2_design_template_node_role;
CREATE TABLE p2_design_template_node_role (
  id                BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT 'ID',
  node_id           BIGINT(20)      NOT NULL                   COMMENT '节点ID',
  role_code         VARCHAR(50)     NOT NULL                   COMMENT '角色编码',
  role_name         VARCHAR(100)    DEFAULT NULL               COMMENT '角色名称',
  create_by         VARCHAR(64)     DEFAULT ''                 COMMENT '创建者',
  create_time       DATETIME                                    COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_node_id (node_id)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '节点协作角色表';

-- ----------------------------
-- 5、任务节点实例表
-- ----------------------------
DROP TABLE IF EXISTS p2_design_task_node;
CREATE TABLE p2_design_task_node (
  id                BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '实例ID',
  task_id           BIGINT(20)      NOT NULL                   COMMENT '任务ID',
  template_node_id  BIGINT(20)      NOT NULL                   COMMENT '模板节点ID',
  node_code         VARCHAR(50)     NOT NULL                   COMMENT '节点编码',
  node_name         VARCHAR(100)    NOT NULL                   COMMENT '节点名称',
  node_order        INT(4)          DEFAULT 0                  COMMENT '节点顺序',
  parallel_group    VARCHAR(50)     DEFAULT NULL               COMMENT '并行分组',
  status            VARCHAR(20)     DEFAULT 'PENDING'           COMMENT '状态（PENDING待处理/RUNNING进行中/COMPLETED已完成/SKIPED跳过）',
  start_time        DATETIME                                    COMMENT '开始时间',
  end_time          DATETIME                                    COMMENT '结束时间',
  assignee_id       BIGINT(20)      DEFAULT NULL               COMMENT '处理人ID',
  assignee_name     VARCHAR(100)    DEFAULT NULL               COMMENT '处理人名称',
  result            TEXT                                        COMMENT '处理结果',
  create_by         VARCHAR(64)     DEFAULT ''                 COMMENT '创建者',
  create_time       DATETIME                                    COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''                 COMMENT '更新者',
  update_time       DATETIME                                    COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_task_id (task_id)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '任务节点实例表';

-- ----------------------------
-- 6、任务附件表
-- ----------------------------
DROP TABLE IF EXISTS p2_design_task_file;
CREATE TABLE p2_design_task_file (
  file_id           BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '附件ID',
  task_id           BIGINT(20)      NOT NULL                   COMMENT '任务ID',
  file_name         VARCHAR(255)    NOT NULL                   COMMENT '文件名称',
  file_path         VARCHAR(500)    NOT NULL                   COMMENT '文件路径',
  file_size         BIGINT(20)      DEFAULT NULL               COMMENT '文件大小',
  file_type         VARCHAR(50)     DEFAULT NULL               COMMENT '文件类型',
  file_suffix       VARCHAR(20)     DEFAULT NULL               COMMENT '文件后缀',
  upload_by         VARCHAR(64)     DEFAULT ''                 COMMENT '上传者',
  upload_time       DATETIME                                    COMMENT '上传时间',
  PRIMARY KEY (file_id),
  KEY idx_task_id (task_id)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '任务附件表';

-- ----------------------------
-- 7、任务操作日志表
-- ----------------------------
DROP TABLE IF EXISTS p2_design_task_log;
CREATE TABLE p2_design_task_log (
  log_id            BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '日志ID',
  task_id           BIGINT(20)      NOT NULL                   COMMENT '任务ID',
  node_id           BIGINT(20)      DEFAULT NULL               COMMENT '节点ID（可为null）',
  action            VARCHAR(50)     NOT NULL                   COMMENT '操作类型（CREATE/SUBMIT/APPROVE/REJECT/COMPLETE/TRANSFER/COMMENT）',
  content           TEXT                                        COMMENT '操作内容',
  operator_id       BIGINT(20)      DEFAULT NULL               COMMENT '操作人ID',
  operator_name     VARCHAR(100)    DEFAULT NULL               COMMENT '操作人名称',
  create_time       DATETIME                                    COMMENT '操作时间',
  PRIMARY KEY (log_id),
  KEY idx_task_id (task_id)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '任务操作日志表';

-- ----------------------------
-- 初始化-流程模板数据
-- ----------------------------
INSERT INTO p2_design_template (template_id, template_name, template_desc, template_config, is_default, status, create_by, create_time, remark) VALUES
(1, '标准液压优化流程', '包含需求分析、方案设计、仿真验证等环节', '[{"nodeCode":"N1","nodeName":"需求分析","parallelGroup":null,"responsibleRole":"HYDRAULIC","collaborators":["STRUCTURE"]},{"nodeCode":"N2","nodeName":"方案设计","parallelGroup":null,"responsibleRole":"HYDRAULIC","collaborators":["HYDRAULIC","LAYOUT"]},{"nodeCode":"N3","nodeName":"仿真验证","parallelGroup":null,"responsibleRole":"STRUCTURE","collaborators":["STRUCTURE","MANUFACTURE"]},{"nodeCode":"N4","nodeName":"优化迭代","parallelGroup":null,"responsibleRole":"HYDRAULIC","collaborators":["HYDRAULIC"]},{"nodeCode":"N5","nodeName":"评审归档","parallelGroup":null,"responsibleRole":"STRUCTURE","collaborators":[]}]', '1', '0', 'admin', SYSDATE(), '默认模板'),
(2, '快速评审流程', '适用于紧急任务的简化流程', '[{"nodeCode":"N1","nodeName":"紧急评估","parallelGroup":null,"responsibleRole":"HYDRAULIC","collaborators":["HYDRAULIC"]},{"nodeCode":"N2","nodeName":"快速验证","parallelGroup":null,"responsibleRole":"STRUCTURE","collaborators":["STRUCTURE"]},{"nodeCode":"N3","nodeName":"评审通过","parallelGroup":null,"responsibleRole":"MANUFACTURE","collaborators":["MANUFACTURE"]}]', '0', '0', 'admin', SYSDATE(), '快速评审'),
(3, '多学科协同流程', '支持多团队并行协作的复杂流程', '[{"nodeCode":"N1","nodeName":"任务分解","parallelGroup":null,"responsibleRole":"HYDRAULIC","collaborators":[]},{"nodeCode":"N2","nodeName":"结构设计","parallelGroup":"A","responsibleRole":"STRUCTURE","collaborators":["STRUCTURE"]},{"nodeCode":"N3","nodeName":"液压设计","parallelGroup":"A","responsibleRole":"HYDRAULIC","collaborators":["HYDRAULIC"]},{"nodeCode":"N4","nodeName":"布局设计","parallelGroup":"A","responsibleRole":"LAYOUT","collaborators":["LAYOUT"]},{"nodeCode":"N5","nodeName":"综合仿真","parallelGroup":null,"responsibleRole":"STRUCTURE","collaborators":["STRUCTURE","HYDRAULIC","MANUFACTURE"]},{"nodeCode":"N6","nodeName":"最终评审","parallelGroup":null,"responsibleRole":"STRUCTURE","collaborators":[]}]', '0', '0', 'admin', SYSDATE(), '多学科协同');

-- ----------------------------
-- 初始化-模板节点数据
-- ----------------------------
INSERT INTO p2_design_template_node (template_id, node_code, node_name, node_order, responsible_role, create_by, create_time) VALUES
(1, 'N1', '需求分析', 1, 'HYDRAULIC', 'admin', SYSDATE()),
(1, 'N2', '方案设计', 2, 'HYDRAULIC', 'admin', SYSDATE()),
(1, 'N3', '仿真验证', 3, 'STRUCTURE', 'admin', SYSDATE()),
(1, 'N4', '优化迭代', 4, 'HYDRAULIC', 'admin', SYSDATE()),
(1, 'N5', '评审归档', 5, 'STRUCTURE', 'admin', SYSDATE()),
(2, 'N1', '紧急评估', 1, 'HYDRAULIC', 'admin', SYSDATE()),
(2, 'N2', '快速验证', 2, 'STRUCTURE', 'admin', SYSDATE()),
(2, 'N3', '评审通过', 3, 'MANUFACTURE', 'admin', SYSDATE()),
(3, 'N1', '任务分解', 1, 'HYDRAULIC', 'admin', SYSDATE()),
(3, 'N2', '结构设计', 2, 'STRUCTURE', 'admin', SYSDATE()),
(3, 'N3', '液压设计', 3, 'HYDRAULIC', 'admin', SYSDATE()),
(3, 'N4', '布局设计', 4, 'LAYOUT', 'admin', SYSDATE()),
(3, 'N5', '综合仿真', 5, 'STRUCTURE', 'admin', SYSDATE()),
(3, 'N6', '最终评审', 6, 'STRUCTURE', 'admin', SYSDATE());


-- ============================================
-- 第二部分：菜单SQL
-- 说明：请先执行上面的建表SQL
--       再执行下面的菜单SQL
-- ============================================

-- ============================================
-- 菜单SQL执行说明
-- ============================================
-- 1、menu_id 使用 2000-2005，如果你已有这些ID，请修改为其他值
-- 2、执行后需要重新登录后台才能看到新菜单
-- ============================================

-- 1、添加一级菜单（设计任务）
INSERT INTO sys_menu (
  menu_id, menu_name, parent_name, parent_id, order_num,
  path, component, is_frame, is_cache, menu_type,
  visible, status, perms, icon, create_by, create_time, remark
) VALUES (
  2000, '设计任务', '0', 0, 9,
  'designtask', NULL,
  1, 0, 'M',
  '0', '0', '',
  'clipboard',
  'admin', SYSDATE(), '设计任务主菜单'
);

-- 2、添加任务发起菜单
INSERT INTO sys_menu (
  menu_id, menu_name, parent_name, parent_id, order_num,
  path, component, is_frame, is_cache, menu_type,
  visible, status, perms, icon, create_by, create_time, remark
) VALUES (
  2001, '任务发起', '设计任务', 2000, 1,
  'task/create', 'designtask/task/create',
  1, 0, 'C',
  '0', '0', 'designtask:task:add',
  '#',
  'admin', SYSDATE(), '任务发起页面'
);

-- 3、添加任务看板菜单
INSERT INTO sys_menu (
  menu_id, menu_name, parent_name, parent_id, order_num,
  path, component, is_frame, is_cache, menu_type,
  visible, status, perms, icon, create_by, create_time, remark
) VALUES (
  2002, '任务看板', '设计任务', 2000, 2,
  'task/kanban', 'designtask/task/kanban',
  1, 0, 'C',
  '0', '0', 'designtask:task:list',
  '#',
  'admin', SYSDATE(), '任务看板视图'
);

-- 4、添加历史记录菜单
INSERT INTO sys_menu (
  menu_id, menu_name, parent_name, parent_id, order_num,
  path, component, is_frame, is_cache, menu_type,
  visible, status, perms, icon, create_by, create_time, remark
) VALUES (
  2003, '历史记录', '设计任务', 2000, 3,
  'task/history', 'designtask/task/history',
  1, 0, 'C',
  '0', '0', 'designtask:task:list',
  '#',
  'admin', SYSDATE(), '历史任务列表'
);

-- 5、添加流程管理菜单
INSERT INTO sys_menu (
  menu_id, menu_name, parent_name, parent_id, order_num,
  path, component, is_frame, is_cache, menu_type,
  visible, status, perms, icon, create_by, create_time, remark
) VALUES (
  2004, '流程管理', '设计任务', 2000, 4,
  'flow/template', 'designtask/flow/template',
  1, 0, 'C',
  '0', '0', 'designtask:flow:list',
  '#',
  'admin', SYSDATE(), '流程模板管理'
);

SET FOREIGN_KEY_CHECKS = 1;
