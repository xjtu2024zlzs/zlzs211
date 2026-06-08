-- =============================================================================
-- Digital dossier supplemental feature tables - MySQL 8.0
-- Purpose: relation graph, virtual-physical mapping, search/QA, push, analysis,
-- generation details, and engineering change execution.
-- =============================================================================

SET NAMES utf8mb4;
USE `ry-cloud`;

-- -----------------------------------------------------------------------------
-- 数据关系可视化：节点
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_data_relation_node (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '节点ID',
  node_code varchar(100) NOT NULL COMMENT '节点编码',
  node_type varchar(50) NOT NULL COMMENT '节点类型：AIRCRAFT/PART/DOCUMENT/WORK_ORDER/FAULT/DOSSIER等',
  domain varchar(30) DEFAULT NULL COMMENT '所属阶段：DESIGN/MANUFACTURING/SERVICE/DOSSIER',
  display_name varchar(300) NOT NULL COMMENT '展示名称',
  source_table varchar(200) NOT NULL COMMENT '来源表名',
  source_record_id char(36) DEFAULT NULL COMMENT '来源记录ID',
  source_record_key varchar(500) DEFAULT NULL COMMENT '来源业务键',
  dossier_instance_id char(36) DEFAULT NULL COMMENT '关联卷宗实例ID',
  dossier_version_id char(36) DEFAULT NULL COMMENT '关联卷宗版本ID',
  aircraft_id char(36) DEFAULT NULL COMMENT '关联飞机ID',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '节点扩展属性',
  is_active tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否有效',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_data_relation_node_code (node_code),
  KEY idx_data_relation_node_type (node_type, domain),
  KEY idx_data_relation_node_source (source_table, source_record_id),
  KEY idx_data_relation_node_dossier (dossier_instance_id, dossier_version_id),
  KEY idx_data_relation_node_aircraft (aircraft_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据关系图节点：用于卷宗数据关联关系可视化';

-- -----------------------------------------------------------------------------
-- 数据关系可视化：关系边
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_data_relation_edge (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '关系ID',
  edge_code varchar(100) DEFAULT NULL COMMENT '关系编码',
  source_node_id char(36) NOT NULL COMMENT '起点节点ID',
  target_node_id char(36) NOT NULL COMMENT '终点节点ID',
  relation_type varchar(50) NOT NULL COMMENT '关系类型：BELONGS_TO/SOURCE_OF/GENERATES/REFERENCES/IMPACTS等',
  relation_name varchar(200) DEFAULT NULL COMMENT '关系名称',
  direction varchar(20) NOT NULL DEFAULT 'directed' COMMENT '方向：directed/undirected',
  weight decimal(10,4) NOT NULL DEFAULT 1.0000 COMMENT '关系权重',
  confidence decimal(5,4) DEFAULT NULL COMMENT '可信度',
  evidence_type varchar(50) DEFAULT NULL COMMENT '证据类型',
  evidence_table varchar(200) DEFAULT NULL COMMENT '证据来源表',
  evidence_record_id char(36) DEFAULT NULL COMMENT '证据记录ID',
  evidence_summary text COMMENT '证据摘要',
  dossier_instance_id char(36) DEFAULT NULL COMMENT '关联卷宗实例ID',
  aircraft_id char(36) DEFAULT NULL COMMENT '关联飞机ID',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '关系扩展属性',
  is_active tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否有效',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_data_relation_edge_code (edge_code),
  KEY idx_data_relation_edge_source (source_node_id),
  KEY idx_data_relation_edge_target (target_node_id),
  KEY idx_data_relation_edge_type (relation_type),
  KEY idx_data_relation_edge_dossier (dossier_instance_id),
  KEY idx_data_relation_edge_aircraft (aircraft_id),
  CONSTRAINT chk_data_relation_edge_direction CHECK (direction IN ('directed','undirected'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据关系图关系：用于检索结果关联展开和跨域关联展示';

-- -----------------------------------------------------------------------------
-- 虚实映射
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_virtual_physical_mapping (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '映射ID',
  mapping_code varchar(100) NOT NULL COMMENT '映射编码',
  physical_object_type varchar(50) NOT NULL COMMENT '实体对象类型：AIRCRAFT/PART_INSTANCE/BOM_NODE/TOOLING等',
  physical_object_id char(36) DEFAULT NULL COMMENT '实体对象ID',
  physical_object_key varchar(500) DEFAULT NULL COMMENT '实体对象业务键',
  virtual_object_type varchar(50) NOT NULL COMMENT '数字对象类型：DOSSIER_ENTRY/STRUCTURE_NODE/DOCUMENT_ENTRY/SYSTEM_RECORD等',
  virtual_object_id char(36) DEFAULT NULL COMMENT '数字对象ID',
  virtual_object_key varchar(500) DEFAULT NULL COMMENT '数字对象业务键',
  source_system varchar(50) DEFAULT NULL COMMENT '来源系统',
  mapping_status varchar(24) NOT NULL DEFAULT 'active' COMMENT '映射状态：active/inactive/pending/invalid',
  confidence decimal(5,4) DEFAULT NULL COMMENT '映射可信度',
  valid_from datetime(6) DEFAULT NULL COMMENT '生效时间',
  valid_to datetime(6) DEFAULT NULL COMMENT '失效时间',
  mapping_rule_json json NOT NULL DEFAULT (json_object()) COMMENT '映射规则',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '扩展属性',
  created_by varchar(100) NOT NULL DEFAULT 'system' COMMENT '创建人',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_virtual_physical_mapping_code (mapping_code),
  KEY idx_vpm_physical (physical_object_type, physical_object_id),
  KEY idx_vpm_virtual (virtual_object_type, virtual_object_id),
  KEY idx_vpm_status (mapping_status),
  KEY idx_vpm_source_system (source_system),
  CONSTRAINT chk_vpm_status CHECK (mapping_status IN ('active','inactive','pending','invalid'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='虚实映射：记录实物对象与数字卷宗对象的对应关系';

CREATE TABLE IF NOT EXISTS t1_virtual_physical_mapping_history (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '历史ID',
  mapping_id char(36) NOT NULL COMMENT '映射ID',
  change_type varchar(30) NOT NULL COMMENT '变更类型：create/update/activate/deactivate/invalidate',
  old_status varchar(24) DEFAULT NULL COMMENT '变更前状态',
  new_status varchar(24) DEFAULT NULL COMMENT '变更后状态',
  old_payload_json json DEFAULT NULL COMMENT '变更前内容',
  new_payload_json json DEFAULT NULL COMMENT '变更后内容',
  change_reason text COMMENT '变更原因',
  changed_by varchar(100) NOT NULL DEFAULT 'system' COMMENT '变更人',
  changed_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '变更时间',
  PRIMARY KEY (id),
  KEY idx_vpm_history_mapping (mapping_id),
  KEY idx_vpm_history_changed_at (changed_at),
  CONSTRAINT chk_vpm_history_change_type CHECK (change_type IN ('create','update','activate','deactivate','invalidate'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='虚实映射变更历史';

-- -----------------------------------------------------------------------------
-- 智能检索：索引记录和检索历史
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_search_index_record (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '索引记录ID',
  index_code varchar(100) NOT NULL COMMENT '索引编码',
  object_type varchar(50) NOT NULL COMMENT '对象类型：DOSSIER/DOCUMENT/STRUCTURE_NODE/WORK_ORDER/FAULT等',
  object_id char(36) DEFAULT NULL COMMENT '对象ID',
  object_key varchar(500) DEFAULT NULL COMMENT '对象业务键',
  dossier_instance_id char(36) DEFAULT NULL COMMENT '卷宗实例ID',
  dossier_version_id char(36) DEFAULT NULL COMMENT '卷宗版本ID',
  aircraft_id char(36) DEFAULT NULL COMMENT '飞机ID',
  title varchar(1000) NOT NULL COMMENT '标题',
  summary text COMMENT '摘要',
  keywords varchar(1000) DEFAULT NULL COMMENT '关键词',
  lifecycle_stage varchar(30) DEFAULT NULL COMMENT '生命周期阶段：DESIGN/MANUFACTURING/SERVICE',
  source_system varchar(50) DEFAULT NULL COMMENT '来源系统',
  source_table varchar(200) DEFAULT NULL COMMENT '来源表',
  source_record_id char(36) DEFAULT NULL COMMENT '来源记录ID',
  content_text longtext COMMENT '可检索正文',
  metadata_json json NOT NULL DEFAULT (json_object()) COMMENT '检索元数据',
  index_status varchar(24) NOT NULL DEFAULT 'active' COMMENT '索引状态：active/disabled/deleted',
  indexed_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '入库时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_search_index_record_code (index_code),
  KEY idx_search_index_object (object_type, object_id),
  KEY idx_search_index_dossier (dossier_instance_id, dossier_version_id),
  KEY idx_search_index_aircraft (aircraft_id),
  KEY idx_search_index_stage (lifecycle_stage),
  KEY idx_search_index_source (source_system, source_table, source_record_id),
  FULLTEXT KEY ft_search_index_text (title, summary, keywords, content_text),
  CONSTRAINT chk_search_index_status CHECK (index_status IN ('active','disabled','deleted'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能检索索引记录：汇总卷宗、文档和业务数据的可检索内容';

CREATE TABLE IF NOT EXISTS t1_search_record (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '检索记录ID',
  search_code varchar(100) NOT NULL COMMENT '检索编码',
  search_type varchar(30) NOT NULL COMMENT '检索类型：keyword/filter/semantic/qa',
  query_text text NOT NULL COMMENT '检索内容',
  filter_json json NOT NULL DEFAULT (json_object()) COMMENT '筛选条件',
  result_count int NOT NULL DEFAULT 0 COMMENT '结果数量',
  top_result_json json NOT NULL DEFAULT (json_array()) COMMENT '前置结果摘要',
  search_status varchar(24) NOT NULL DEFAULT 'succeeded' COMMENT '状态：succeeded/failed',
  execution_ms int DEFAULT NULL COMMENT '执行耗时毫秒',
  requested_by varchar(100) DEFAULT NULL COMMENT '发起人',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_search_record_code (search_code),
  KEY idx_search_record_type (search_type),
  KEY idx_search_record_created_at (created_at),
  CONSTRAINT chk_search_record_type CHECK (search_type IN ('keyword','filter','semantic','qa')),
  CONSTRAINT chk_search_record_status CHECK (search_status IN ('succeeded','failed'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能检索历史：记录全域检索、多维筛选和语义检索行为';

-- -----------------------------------------------------------------------------
-- 智能问答
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_qa_session (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '会话ID',
  session_code varchar(100) NOT NULL COMMENT '会话编码',
  session_title varchar(300) DEFAULT NULL COMMENT '会话标题',
  dossier_instance_id char(36) DEFAULT NULL COMMENT '卷宗实例ID',
  aircraft_id char(36) DEFAULT NULL COMMENT '飞机ID',
  session_status varchar(24) NOT NULL DEFAULT 'open' COMMENT '会话状态：open/closed/archived',
  context_scope_json json NOT NULL DEFAULT (json_object()) COMMENT '问答上下文范围',
  created_by varchar(100) DEFAULT NULL COMMENT '创建人',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  closed_at datetime(6) DEFAULT NULL COMMENT '关闭时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_qa_session_code (session_code),
  KEY idx_qa_session_dossier (dossier_instance_id),
  KEY idx_qa_session_aircraft (aircraft_id),
  KEY idx_qa_session_status (session_status),
  CONSTRAINT chk_qa_session_status CHECK (session_status IN ('open','closed','archived'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能问答会话';

CREATE TABLE IF NOT EXISTS t1_qa_message (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '消息ID',
  session_id char(36) NOT NULL COMMENT '会话ID',
  message_role varchar(20) NOT NULL COMMENT '消息角色：user/assistant/system',
  message_text longtext NOT NULL COMMENT '消息内容',
  related_search_id char(36) DEFAULT NULL COMMENT '关联检索记录ID',
  evidence_json json NOT NULL DEFAULT (json_array()) COMMENT '回答依据',
  model_name varchar(100) DEFAULT NULL COMMENT '模型名称',
  token_count int DEFAULT NULL COMMENT '消耗量',
  feedback_score int DEFAULT NULL COMMENT '反馈评分',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_qa_message_session (session_id, created_at),
  KEY idx_qa_message_search (related_search_id),
  CONSTRAINT chk_qa_message_role CHECK (message_role IN ('user','assistant','system'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能问答消息记录';

-- -----------------------------------------------------------------------------
-- 智能信息推送
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_push_rule (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '推送规则ID',
  rule_code varchar(100) NOT NULL COMMENT '规则编码',
  rule_name varchar(200) NOT NULL COMMENT '规则名称',
  trigger_type varchar(50) NOT NULL COMMENT '触发类型：issue/alert/update/export/search_subscription',
  target_domain varchar(50) DEFAULT NULL COMMENT '目标领域',
  condition_json json NOT NULL DEFAULT (json_object()) COMMENT '触发条件',
  receiver_type varchar(30) NOT NULL COMMENT '接收对象类型：user/role/org/external',
  receiver_ref varchar(300) NOT NULL COMMENT '接收对象标识',
  message_template text COMMENT '消息模板',
  rule_status varchar(24) NOT NULL DEFAULT 'active' COMMENT '规则状态：active/disabled',
  created_by varchar(100) DEFAULT NULL COMMENT '创建人',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_push_rule_code (rule_code),
  KEY idx_push_rule_trigger (trigger_type),
  KEY idx_push_rule_status (rule_status),
  CONSTRAINT chk_push_rule_status CHECK (rule_status IN ('active','disabled'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能信息推送规则';

CREATE TABLE IF NOT EXISTS t1_push_record (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '推送记录ID',
  rule_id char(36) DEFAULT NULL COMMENT '推送规则ID',
  subject_type varchar(50) NOT NULL COMMENT '推送对象类型',
  subject_id char(36) DEFAULT NULL COMMENT '推送对象ID',
  receiver varchar(300) NOT NULL COMMENT '接收人或接收地址',
  channel varchar(30) NOT NULL DEFAULT 'system' COMMENT '推送渠道：system/email/message/webhook',
  push_title varchar(300) NOT NULL COMMENT '推送标题',
  push_content text COMMENT '推送内容',
  push_status varchar(24) NOT NULL DEFAULT 'pending' COMMENT '推送状态：pending/sent/read/failed',
  sent_at datetime(6) DEFAULT NULL COMMENT '发送时间',
  read_at datetime(6) DEFAULT NULL COMMENT '已读时间',
  error_message text COMMENT '错误信息',
  payload_json json NOT NULL DEFAULT (json_object()) COMMENT '推送载荷',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_push_record_rule (rule_id),
  KEY idx_push_record_subject (subject_type, subject_id),
  KEY idx_push_record_receiver (receiver),
  KEY idx_push_record_status (push_status),
  CONSTRAINT chk_push_record_status CHECK (push_status IN ('pending','sent','read','failed'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='智能信息推送记录';

-- -----------------------------------------------------------------------------
-- 数据挖掘和分析结果
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_analysis_task (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '分析任务ID',
  task_code varchar(100) NOT NULL COMMENT '任务编码',
  task_name varchar(300) NOT NULL COMMENT '任务名称',
  analysis_type varchar(50) NOT NULL COMMENT '分析类型：text_mining/correlation/statistics/cross_domain',
  scope_type varchar(50) DEFAULT NULL COMMENT '分析范围类型',
  scope_json json NOT NULL DEFAULT (json_object()) COMMENT '分析范围',
  task_status varchar(24) NOT NULL DEFAULT 'queued' COMMENT '任务状态：queued/running/succeeded/failed/cancelled',
  algorithm_code varchar(64) DEFAULT NULL COMMENT '算法编码',
  algorithm_version varchar(32) DEFAULT NULL COMMENT '算法版本',
  params_json json NOT NULL DEFAULT (json_object()) COMMENT '分析参数',
  started_at datetime(6) DEFAULT NULL COMMENT '开始时间',
  finished_at datetime(6) DEFAULT NULL COMMENT '完成时间',
  error_message text COMMENT '错误信息',
  created_by varchar(100) DEFAULT NULL COMMENT '创建人',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_analysis_task_code (task_code),
  KEY idx_analysis_task_type (analysis_type),
  KEY idx_analysis_task_status (task_status),
  CONSTRAINT chk_analysis_task_status CHECK (task_status IN ('queued','running','succeeded','failed','cancelled'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据分析任务：记录文本挖掘、关联分析、统计分析和跨域分析任务';

CREATE TABLE IF NOT EXISTS t1_analysis_result (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '分析结果ID',
  task_id char(36) NOT NULL COMMENT '分析任务ID',
  result_type varchar(50) NOT NULL COMMENT '结果类型',
  result_title varchar(300) DEFAULT NULL COMMENT '结果标题',
  result_summary text COMMENT '结果摘要',
  result_value_json json NOT NULL DEFAULT (json_object()) COMMENT '结果数据',
  confidence decimal(5,4) DEFAULT NULL COMMENT '可信度',
  rank_no int DEFAULT NULL COMMENT '排序号',
  related_object_type varchar(50) DEFAULT NULL COMMENT '关联对象类型',
  related_object_id char(36) DEFAULT NULL COMMENT '关联对象ID',
  evidence_json json NOT NULL DEFAULT (json_array()) COMMENT '证据',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_analysis_result_task (task_id),
  KEY idx_analysis_result_type (result_type),
  KEY idx_analysis_result_object (related_object_type, related_object_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据分析结果汇总';

CREATE TABLE IF NOT EXISTS t1_text_mining_result (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '文本挖掘结果ID',
  task_id char(36) NOT NULL COMMENT '分析任务ID',
  source_object_type varchar(50) NOT NULL COMMENT '来源对象类型',
  source_object_id char(36) DEFAULT NULL COMMENT '来源对象ID',
  source_text_hash varchar(128) DEFAULT NULL COMMENT '来源文本哈希',
  keyword varchar(200) DEFAULT NULL COMMENT '关键词',
  topic_label varchar(200) DEFAULT NULL COMMENT '主题标签',
  entity_type varchar(50) DEFAULT NULL COMMENT '实体类型',
  entity_value varchar(500) DEFAULT NULL COMMENT '实体值',
  sentiment_label varchar(50) DEFAULT NULL COMMENT '倾向标签',
  confidence decimal(5,4) DEFAULT NULL COMMENT '可信度',
  evidence_excerpt text COMMENT '证据片段',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_text_mining_task (task_id),
  KEY idx_text_mining_source (source_object_type, source_object_id),
  KEY idx_text_mining_keyword (keyword),
  KEY idx_text_mining_entity (entity_type, entity_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文本挖掘结果：记录关键词、主题和实体抽取结果';

CREATE TABLE IF NOT EXISTS t1_correlation_result (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '相关性结果ID',
  task_id char(36) NOT NULL COMMENT '分析任务ID',
  source_object_type varchar(50) NOT NULL COMMENT '源对象类型',
  source_object_id char(36) DEFAULT NULL COMMENT '源对象ID',
  target_object_type varchar(50) NOT NULL COMMENT '目标对象类型',
  target_object_id char(36) DEFAULT NULL COMMENT '目标对象ID',
  relation_type varchar(50) NOT NULL COMMENT '关联类型',
  correlation_score decimal(10,6) NOT NULL DEFAULT 0.000000 COMMENT '相关性得分',
  support_count int NOT NULL DEFAULT 0 COMMENT '支持样本数',
  confidence decimal(5,4) DEFAULT NULL COMMENT '可信度',
  lift_value decimal(10,6) DEFAULT NULL COMMENT '提升度',
  result_summary text COMMENT '结果说明',
  evidence_json json NOT NULL DEFAULT (json_array()) COMMENT '证据',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_correlation_task (task_id),
  KEY idx_correlation_source (source_object_type, source_object_id),
  KEY idx_correlation_target (target_object_type, target_object_id),
  KEY idx_correlation_type (relation_type),
  KEY idx_correlation_score (correlation_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='跨域相关性分析结果';

-- -----------------------------------------------------------------------------
-- 卷宗生成明细
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_generation_job_item (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '生成明细ID',
  generation_job_id char(36) NOT NULL COMMENT '生成任务ID',
  item_order int NOT NULL DEFAULT 0 COMMENT '明细顺序',
  source_domain varchar(50) DEFAULT NULL COMMENT '数据阶段：DESIGN/MANUFACTURING/SERVICE',
  source_system varchar(50) DEFAULT NULL COMMENT '来源系统',
  source_table varchar(200) DEFAULT NULL COMMENT '来源表',
  source_record_id char(36) DEFAULT NULL COMMENT '来源记录ID',
  source_record_key varchar(500) DEFAULT NULL COMMENT '来源业务键',
  target_node_id char(36) DEFAULT NULL COMMENT '目标目录节点ID',
  target_document_entry_id char(36) DEFAULT NULL COMMENT '目标文档条目ID',
  action_type varchar(30) NOT NULL DEFAULT 'collect' COMMENT '动作类型：collect/generate/link/skip',
  item_status varchar(24) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/running/succeeded/failed/skipped',
  error_message text COMMENT '错误信息',
  source_trace_json json NOT NULL DEFAULT (json_object()) COMMENT '来源追踪',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_generation_job_item_job (generation_job_id, item_order),
  KEY idx_generation_job_item_source (source_table, source_record_id),
  KEY idx_generation_job_item_target_node (target_node_id),
  KEY idx_generation_job_item_status (item_status),
  CONSTRAINT chk_generation_job_item_action CHECK (action_type IN ('collect','generate','link','skip')),
  CONSTRAINT chk_generation_job_item_status CHECK (item_status IN ('pending','running','succeeded','failed','skipped'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卷宗生成明细：记录每次生成时抽取和生成的具体数据';

-- -----------------------------------------------------------------------------
-- 工程更改执行记录
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_engineering_change_execution (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '执行记录ID',
  execution_code varchar(100) NOT NULL COMMENT '执行记录编码',
  change_type varchar(30) NOT NULL COMMENT '更改类型：ecr/eco/sb/deviation',
  change_ref_id char(36) DEFAULT NULL COMMENT '更改来源ID',
  change_code varchar(100) NOT NULL COMMENT '更改编号',
  title varchar(300) NOT NULL COMMENT '标题',
  aircraft_id char(36) DEFAULT NULL COMMENT '飞机ID',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID',
  part_instance_id char(36) DEFAULT NULL COMMENT '零件实例ID',
  execution_status varchar(24) NOT NULL DEFAULT 'planned' COMMENT '执行状态：planned/running/completed/verified/cancelled',
  planned_start_at datetime(6) DEFAULT NULL COMMENT '计划开始时间',
  planned_finish_at datetime(6) DEFAULT NULL COMMENT '计划完成时间',
  actual_start_at datetime(6) DEFAULT NULL COMMENT '实际开始时间',
  actual_finish_at datetime(6) DEFAULT NULL COMMENT '实际完成时间',
  responsible_person_id char(36) DEFAULT NULL COMMENT '责任人ID',
  responsible_org_id char(36) DEFAULT NULL COMMENT '责任组织ID',
  verified_by varchar(100) DEFAULT NULL COMMENT '确认人',
  verified_at datetime(6) DEFAULT NULL COMMENT '确认时间',
  before_snapshot_id char(36) DEFAULT NULL COMMENT '执行前快照ID',
  after_snapshot_id char(36) DEFAULT NULL COMMENT '执行后快照ID',
  result_summary text COMMENT '执行结果说明',
  evidence_json json NOT NULL DEFAULT (json_array()) COMMENT '执行证据',
  created_by varchar(100) NOT NULL DEFAULT 'system' COMMENT '创建人',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_engineering_change_execution_code (execution_code),
  KEY idx_ece_change (change_type, change_code),
  KEY idx_ece_aircraft (aircraft_id),
  KEY idx_ece_bom_node (bom_node_id),
  KEY idx_ece_part_instance (part_instance_id),
  KEY idx_ece_status (execution_status),
  CONSTRAINT chk_ece_change_type CHECK (change_type IN ('ecr','eco','sb','deviation')),
  CONSTRAINT chk_ece_status CHECK (execution_status IN ('planned','running','completed','verified','cancelled'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工程更改执行记录：记录更改落实到单机、BOM节点或零件实例的执行情况';

