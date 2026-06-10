-- ============================================
-- 设计任务模块数据表
-- ============================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1、设计任务主表
-- ----------------------------
drop table if exists p2_design_task;
create table p2_design_task (
  task_id           bigint(20)      not null auto_increment    comment '任务ID',
  task_name         varchar(200)    not null                   comment '任务名称',
  task_no           varchar(50)     default null               comment '任务编号',
  task_type         varchar(20)     not null                   comment '任务类型（HYDRAULIC/STRUCTURE/LAYOUT）',
  template_id       bigint(20)      default null               comment '流程模板ID',
  priority          int(2)          default 2                  comment '优先级（1低 2中 3高）',
  description       text                                     comment '问题描述',
  status            varchar(20)     default 'DRAFT'            comment '任务状态（DRAFT待提交/APPROVED审批中/RUNNING进行中/COMPLETED已完成/CLOSED已关闭）',
  responsible_role  varchar(50)     default null               comment '负责人角色',
  collab_roles      varchar(200)    default null               comment '协同角色（逗号分隔）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  primary key (task_id)
) engine=innodb auto_increment=1 comment = '设计任务主表';

-- ----------------------------
-- 2、流程模板表
-- ----------------------------
drop table if exists p2_design_template;
create table p2_design_template (
  template_id       bigint(20)      not null auto_increment    comment '模板ID',
  template_name     varchar(100)    not null                   comment '模板名称',
  template_desc     varchar(500)    default null               comment '模板描述',
  template_config   text                                     comment '模板配置JSON（节点列表）',
  is_default        char(1)         default '0'                comment '是否默认模板（0否 1是）',
  status            char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (template_id)
) engine=innodb auto_increment=1 comment = '流程模板表';

-- ----------------------------
-- 3、模板节点表
-- ----------------------------
drop table if exists p2_design_template_node;
create table p2_design_template_node (
  node_id           bigint(20)      not null auto_increment    comment '节点ID',
  template_id       bigint(20)      not null                   comment '模板ID',
  node_code         varchar(50)     not null                   comment '节点编码',
  node_name         varchar(100)    not null                   comment '节点名称',
  node_order        int(4)          default 0                  comment '节点顺序',
  parallel_group    varchar(50)     default null               comment '并行分组（同一分组内并行执行）',
  responsible_role  varchar(50)     default null               comment '负责人角色',
  time_limit        int(4)          default null               comment '时限（小时）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (node_id),
  key idx_template_id (template_id),
  unique key uk_template_node (template_id, node_code)
) engine=innodb auto_increment=1 comment = '模板节点表';

-- ----------------------------
-- 4、节点协作角色表
-- ----------------------------
drop table if exists p2_design_template_node_role;
create table p2_design_template_node_role (
  id                bigint(20)      not null auto_increment    comment 'ID',
  node_id           bigint(20)      not null                   comment '节点ID',
  role_code         varchar(50)     not null                   comment '角色编码',
  role_name         varchar(100)    default null               comment '角色名称',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  primary key (id),
  key idx_node_id (node_id)
) engine=innodb auto_increment=1 comment = '节点协作角色表';

-- ----------------------------
-- 5、任务节点实例表
-- ----------------------------
drop table if exists p2_design_task_node;
create table p2_design_task_node (
  id                bigint(20)      not null auto_increment    comment '实例ID',
  task_id           bigint(20)      not null                   comment '任务ID',
  template_node_id  bigint(20)      not null                   comment '模板节点ID',
  node_code         varchar(50)     not null                   comment '节点编码',
  node_name         varchar(100)    not null                   comment '节点名称',
  node_order        int(4)          default 0                  comment '节点顺序',
  parallel_group    varchar(50)     default null               comment '并行分组',
  status            varchar(20)     default 'PENDING'          comment '状态（PENDING待处理/RUNNING进行中/COMPLETED已完成/SKIPED跳过）',
  start_time        datetime                                   comment '开始时间',
  end_time          datetime                                   comment '结束时间',
  assignee_id       bigint(20)      default null               comment '处理人ID',
  assignee_name     varchar(100)    default null               comment '处理人名称',
  result            text                                     comment '处理结果',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (id),
  key idx_task_id (task_id)
) engine=innodb auto_increment=1 comment = '任务节点实例表';

-- ----------------------------
-- 6、任务附件表
-- ----------------------------
drop table if exists p2_design_task_file;
create table p2_design_task_file (
  file_id           bigint(20)      not null auto_increment    comment '附件ID',
  task_id           bigint(20)      not null                   comment '任务ID',
  file_name         varchar(255)    not null                   comment '文件名称',
  file_path         varchar(500)    not null                   comment '文件路径',
  file_size         bigint(20)      default null               comment '文件大小',
  file_type         varchar(50)     default null               comment '文件类型',
  file_suffix       varchar(20)     default null               comment '文件后缀',
  upload_by         varchar(64)     default ''                 comment '上传者',
  upload_time       datetime                                   comment '上传时间',
  primary key (file_id),
  key idx_task_id (task_id)
) engine=innodb auto_increment=1 comment = '任务附件表';

-- ----------------------------
-- 7、任务操作日志表
-- ----------------------------
drop table if exists p2_design_task_log;
create table p2_design_task_log (
  log_id            bigint(20)      not null auto_increment    comment '日志ID',
  task_id           bigint(20)      not null                   comment '任务ID',
  node_id           bigint(20)      default null               comment '节点ID（可为null）',
  action            varchar(50)     not null                   comment '操作类型（CREATE/SUBMIT/APPROVE/REJECT/COMPLETE/TRANSFER/COMMENT）',
  content           text                                     comment '操作内容',
  operator_id       bigint(20)      default null               comment '操作人ID',
  operator_name     varchar(100)    default null               comment '操作人名称',
  create_time       datetime                                   comment '操作时间',
  primary key (log_id),
  key idx_task_id (task_id)
) engine=innodb auto_increment=1 comment = '任务操作日志表';

-- ----------------------------
-- 初始化-流程模板数据
-- ----------------------------
insert into p2_design_template (template_id, template_name, template_desc, template_config, is_default, status, create_by, create_time, remark) values
(1, '标准液压优化流程', '包含需求分析、方案设计、仿真验证等环节', '[{"nodeCode":"N1","nodeName":"需求分析","parallelGroup":null,"responsibleRole":"HYDRAULIC","collaborators":["STRUCTURE"]},{"nodeCode":"N2","nodeName":"方案设计","parallelGroup":null,"responsibleRole":"HYDRAULIC","collaborators":["HYDRAULIC","LAYOUT"]},{"nodeCode":"N3","nodeName":"仿真验证","parallelGroup":null,"responsibleRole":"STRUCTURE","collaborators":["STRUCTURE","MANUFACTURE"]},{"nodeCode":"N4","nodeName":"优化迭代","parallelGroup":null,"responsibleRole":"HYDRAULIC","collaborators":["HYDRAULIC"]},{"nodeCode":"N5","nodeName":"评审归档","parallelGroup":null,"responsibleRole":"STRUCTURE","collaborators":[]}]', '1', '0', 'admin', sysdate(), '默认模板'),
(2, '快速评审流程', '适用于紧急任务的简化流程', '[{"nodeCode":"N1","nodeName":"紧急评估","parallelGroup":null,"responsibleRole":"HYDRAULIC","collaborators":["HYDRAULIC"]},{"nodeCode":"N2","nodeName":"快速验证","parallelGroup":null,"responsibleRole":"STRUCTURE","collaborators":["STRUCTURE"]},{"nodeCode":"N3","nodeName":"评审通过","parallelGroup":null,"responsibleRole":"MANUFACTURE","collaborators":["MANUFACTURE"]}]', '0', '0', 'admin', sysdate(), '快速评审'),
(3, '多学科协同流程', '支持多团队并行协作的复杂流程', '[{"nodeCode":"N1","nodeName":"任务分解","parallelGroup":null,"responsibleRole":"HYDRAULIC","collaborators":[]},{"nodeCode":"N2","nodeName":"结构设计","parallelGroup":"A","responsibleRole":"STRUCTURE","collaborators":["STRUCTURE"]},{"nodeCode":"N3","nodeName":"液压设计","parallelGroup":"A","responsibleRole":"HYDRAULIC","collaborators":["HYDRAULIC"]},{"nodeCode":"N4","nodeName":"布局设计","parallelGroup":"A","responsibleRole":"LAYOUT","collaborators":["LAYOUT"]},{"nodeCode":"N5","nodeName":"综合仿真","parallelGroup":null,"responsibleRole":"STRUCTURE","collaborators":["STRUCTURE","HYDRAULIC","MANUFACTURE"]},{"nodeCode":"N6","nodeName":"最终评审","parallelGroup":null,"responsibleRole":"STRUCTURE","collaborators":[]}]', '0', '0', 'admin', sysdate(), '多学科协同');

-- ----------------------------
-- 初始化-模板节点数据
-- ----------------------------
insert into p2_design_template_node (template_id, node_code, node_name, node_order, responsible_role, create_by, create_time) values
(1, 'N1', '需求分析', 1, 'HYDRAULIC', 'admin', sysdate()),
(1, 'N2', '方案设计', 2, 'HYDRAULIC', 'admin', sysdate()),
(1, 'N3', '仿真验证', 3, 'STRUCTURE', 'admin', sysdate()),
(1, 'N4', '优化迭代', 4, 'HYDRAULIC', 'admin', sysdate()),
(1, 'N5', '评审归档', 5, 'STRUCTURE', 'admin', sysdate()),
(2, 'N1', '紧急评估', 1, 'HYDRAULIC', 'admin', sysdate()),
(2, 'N2', '快速验证', 2, 'STRUCTURE', 'admin', sysdate()),
(2, 'N3', '评审通过', 3, 'MANUFACTURE', 'admin', sysdate()),
(3, 'N1', '任务分解', 1, 'HYDRAULIC', 'admin', sysdate()),
(3, 'N2', '结构设计', 2, 'STRUCTURE', 'admin', sysdate()),
(3, 'N3', '液压设计', 3, 'HYDRAULIC', 'admin', sysdate()),
(3, 'N4', '布局设计', 4, 'LAYOUT', 'admin', sysdate()),
(3, 'N5', '综合仿真', 5, 'STRUCTURE', 'admin', sysdate()),
(3, 'N6', '最终评审', 6, 'STRUCTURE', 'admin', sysdate());

SET FOREIGN_KEY_CHECKS = 1;
