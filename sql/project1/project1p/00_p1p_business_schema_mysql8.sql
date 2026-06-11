-- ============================================================
-- p1p RuoYi business ER diagram 2.0 - MySQL 8.0.46 DDL
-- Source DBML: E:\Desktop\zlzs211\docx\图2-6_p1p若依版业务ER图2.0.dbml
-- Target MySQL: 8.0.46
-- Table prefix: p1p_
-- Reference policy: no physical relational constraints; DBML references are represented by indexes and business traceability.
-- Access execution policy: access plans are based on audited final mapping spec sets, not unreviewed raw result sets.
-- WARNING: first-time build script. DROP TABLE will remove same-name tables and data.
-- No initialization data is included, so the script is suitable for RuoYi code generator import after table creation.
-- ============================================================

set names utf8mb4;
set foreign_key_checks = 0;
drop table if exists `p1p_datasource`;
create table `p1p_datasource` (
  `datasource_id` bigint not null auto_increment comment '数据源主键',
  `datasource_name` varchar(100) not null comment '数据源名称',
  `access_mode` varchar(30) not null default 'db_direct' comment '接入方式：db_direct 数据库直连、api_pull API 拉取、api_push API 推送',
  `use_status` varchar(30) not null default 'enabled' comment '使用状态：enabled 启用、paused 暂停、disabled 停用、unavailable 暂时不可用',
  `connection_status` varchar(20) not null default 'untested' comment '连接状态：untested 未测试、success 连接成功、failed 连接失败',
  `last_test_time` datetime default null comment '最近测试时间',
  `last_test_message` varchar(500) default null comment '最近测试结果说明',
  `latest_schema_snapshot_id` bigint default null comment '最新模式快照 ID',
  `status` char(1) default '0' comment '若依状态：0正常，1停用',
  `del_flag` char(1) default '0' comment '删除标志：0存在，2删除',
  `create_by` varchar(64) default '' comment '创建者',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_by` varchar(64) default '' comment '更新者',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`datasource_id`),
  unique key `uq_p1p_datasource_name` (`datasource_name`),
  key `idx_p1p_datasource_mode` (`access_mode`),
  key `idx_p1p_datasource_use_status` (`use_status`),
  key `idx_p1p_datasource_connection_status` (`connection_status`),
  key `idx_p1p_datasource_schema_snapshot` (`latest_schema_snapshot_id`),
  constraint `ck_p1p_datasource_access_mode` check (`access_mode` in ('db_direct', 'api_pull', 'api_push')),
  constraint `ck_p1p_datasource_use_status` check (`use_status` in ('enabled', 'paused', 'disabled', 'unavailable')),
  constraint `ck_p1p_datasource_connection_status` check (`connection_status` in ('untested', 'success', 'failed')),
  constraint `ck_p1p_datasource_status` check (`status` in ('0', '1')),
  constraint `ck_p1p_datasource_del_flag` check (`del_flag` in ('0', '2'))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='数据源主表';

drop table if exists `p1p_db_datasource`;
create table `p1p_db_datasource` (
  `datasource_id` bigint not null comment '数据源 ID，主键兼关联数据源主表',
  `db_type` varchar(30) not null default 'postgresql' comment '数据库类型，如 PostgreSQL、MySQL、SQL Server、Oracle',
  `host` varchar(200) not null comment '数据库主机地址',
  `port` int not null default 5432 comment '数据库端口',
  `database_name` varchar(100) not null comment '数据库名称',
  `schema_name` varchar(100) default null comment '数据库 Schema 名称',
  `username` varchar(100) not null comment '数据库用户名',
  `password_enc` varchar(500) default null comment '数据库密码密文',
  `save_password` tinyint(1) not null default 1 comment '是否保存密码：1保存，0不保存',
  `connection_params` varchar(500) default null comment '连接参数，如 sslmode=prefer',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`datasource_id`),
  constraint `ck_p1p_db_datasource_save_password` check (`save_password` in (0, 1))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='数据库直连数据源明细表';

drop table if exists `p1p_api_pull_datasource`;
create table `p1p_api_pull_datasource` (
  `datasource_id` bigint not null comment '数据源 ID，主键兼关联数据源主表',
  `base_url` varchar(500) not null comment '接口基础地址',
  `pull_endpoint` varchar(255) not null default '/api/pull' comment '数据拉取接口路径',
  `schema_endpoint` varchar(255) default '/api/schema' comment '模式读取接口路径',
  `request_method` varchar(10) not null default 'GET' comment '请求方法：GET 或 POST',
  `auth_type` varchar(50) default null comment '认证方式，如 Bearer Token、Basic Auth、None',
  `health_endpoint` varchar(255) not null default '/api/health' comment '健康检查接口路径',
  `api_key_enc` varchar(500) default null comment '接口密钥密文',
  `headers_config` json comment '请求头扩展配置 JSON',
  `extra_config` json comment 'API 拉取扩展配置 JSON',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`datasource_id`)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='API 拉取数据源明细表';

drop table if exists `p1p_api_push_datasource`;
create table `p1p_api_push_datasource` (
  `datasource_id` bigint not null comment '数据源 ID，主键兼关联数据源主表',
  `listen_path` varchar(255) not null default '/api/trigger-push' comment '本系统接收推送的监听路径',
  `payload_format` varchar(20) not null default 'JSON' comment '推送数据格式，如 JSON、XML、CSV',
  `auth_type` varchar(50) default null comment '鉴权方式，如签名 Header、Bearer Token、None',
  `signature_header` varchar(100) default null comment '签名 Header 名称',
  `push_secret_enc` varchar(500) default null comment '推送密钥密文',
  `ip_whitelist` varchar(500) default null comment 'IP 白名单',
  `health_endpoint` varchar(255) default '/api/health' comment '健康检查接口路径',
  `headers_config` json comment '请求头扩展配置 JSON',
  `extra_config` json comment 'API 推送扩展配置 JSON',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注，中期可暂不生成菜单',
  primary key (`datasource_id`)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='API 推送数据源明细表';

drop table if exists `p1p_schema_snapshot`;
create table `p1p_schema_snapshot` (
  `snapshot_id` bigint not null auto_increment comment '模式快照主键',
  `datasource_id` bigint not null comment '来源数据源 ID',
  `snapshot_name` varchar(200) default null comment '模式快照名称',
  `read_status` varchar(20) not null default 'success' comment '读取状态：success 成功、failed 失败',
  `entity_count` int default 0 comment '实体数量',
  `field_count` int default 0 comment '字段数量',
  `schema_json` json comment '完整模式快照 JSON',
  `error_message` text comment '读取失败原因',
  `read_by` varchar(64) default '' comment '读取操作人',
  `read_time` datetime default null comment '读取时间',
  `create_time` datetime default current_timestamp comment '创建时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`snapshot_id`),
  key `idx_p1p_schema_snapshot_datasource` (`datasource_id`),
  key `idx_p1p_schema_snapshot_status` (`read_status`),
  constraint `ck_p1p_schema_snapshot_read_status` check (`read_status` in ('success', 'failed'))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='数据源模式快照表';

drop table if exists `p1p_schema_entity`;
create table `p1p_schema_entity` (
  `entity_id` bigint not null auto_increment comment '模式实体主键',
  `snapshot_id` bigint not null comment '模式快照 ID',
  `datasource_id` bigint not null comment '来源数据源 ID',
  `entity_name` varchar(200) not null comment '实体名称，通常为源表名或接口实体名',
  `source_database` varchar(100) default null comment '源数据库名',
  `source_schema` varchar(100) default null comment '源 Schema 名称',
  `source_table` varchar(200) default null comment '源表名',
  `entity_type` varchar(30) default 'table' comment '实体类型：table 表、view 视图、api API 实体',
  `row_count` int default 0 comment '估算行数或样本行数',
  `metadata` json comment '实体元数据 JSON',
  `create_time` datetime default current_timestamp comment '创建时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`entity_id`),
  unique key `uq_p1p_schema_entity_name` (`snapshot_id`, `entity_name`),
  key `idx_p1p_schema_entity_datasource` (`datasource_id`)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='模式实体表';

drop table if exists `p1p_schema_field`;
create table `p1p_schema_field` (
  `field_id` bigint not null auto_increment comment '模式字段主键',
  `entity_id` bigint not null comment '模式实体 ID',
  `snapshot_id` bigint not null comment '模式快照 ID',
  `field_name` varchar(200) not null comment '字段名称',
  `source_column` varchar(200) default null comment '源字段名',
  `data_type` varchar(100) default null comment '字段类型',
  `ordinal_position` int default null comment '字段顺序号',
  `is_primary_key` tinyint(1) default 0 comment '是否主键：1是，0否',
  `is_nullable` tinyint(1) default 1 comment '是否可空：1可空，0不可空',
  `sample_values` json comment '字段样本值 JSON',
  `field_comment` varchar(500) default null comment '源字段注释',
  `create_time` datetime default current_timestamp comment '创建时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`field_id`),
  unique key `uq_p1p_schema_field_name` (`entity_id`, `field_name`),
  key `idx_p1p_schema_field_snapshot` (`snapshot_id`),
  constraint `ck_p1p_schema_field_is_primary_key` check (`is_primary_key` in (0, 1)),
  constraint `ck_p1p_schema_field_is_nullable` check (`is_nullable` in (0, 1))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='模式字段表';

drop table if exists `p1p_encoding_mode`;
create table `p1p_encoding_mode` (
  `encoding_mode_key` varchar(100) not null comment '编码模式主键',
  `mode_name` varchar(100) not null comment '编码模式名称',
  `description` text comment '编码模式说明',
  `enabled` tinyint(1) not null default 1 comment '是否启用：1启用，0停用',
  `sort_order` int default 0 comment '排序值',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`encoding_mode_key`),
  key `idx_p1p_encoding_mode_enabled` (`enabled`),
  constraint `ck_p1p_encoding_mode_enabled` check (`enabled` in (0, 1))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='编码模式配置表';

drop table if exists `p1p_embedding_model`;
create table `p1p_embedding_model` (
  `embedding_model_key` varchar(100) not null comment '嵌入模型主键',
  `model_label` varchar(100) not null comment '嵌入模型显示名称',
  `provider` varchar(100) default null comment '模型提供商',
  `model_name` varchar(200) not null comment '模型名称',
  `dimension` int default null comment '向量维度',
  `enabled` tinyint(1) not null default 1 comment '是否启用：1启用，0停用',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`embedding_model_key`),
  key `idx_p1p_embedding_model_enabled` (`enabled`),
  constraint `ck_p1p_embedding_model_enabled` check (`enabled` in (0, 1))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='嵌入模型配置表';

drop table if exists `p1p_eval_mode`;
create table `p1p_eval_mode` (
  `eval_mode_key` varchar(100) not null comment '评估模式主键',
  `mode_name` varchar(100) not null comment '评估模式名称',
  `description` text comment '评估模式说明',
  `enabled` tinyint(1) not null default 1 comment '是否启用：1启用，0停用',
  `sort_order` int default 0 comment '排序值',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`eval_mode_key`),
  key `idx_p1p_eval_mode_enabled` (`enabled`),
  constraint `ck_p1p_eval_mode_enabled` check (`enabled` in (0, 1))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='评估模式配置表';

drop table if exists `p1p_llm_config`;
create table `p1p_llm_config` (
  `llm_config_id` bigint not null auto_increment comment 'LLM 配置主键',
  `config_name` varchar(100) not null comment '配置名称',
  `provider` varchar(50) not null comment '模型提供商，下拉选择',
  `model_name` varchar(200) not null comment '模型名称，下拉选择',
  `base_url` varchar(500) default null comment '模型接口地址，由后端配置维护',
  `api_key_enc` varchar(500) default null comment '模型接口密钥密文，由后端配置维护',
  `enabled` tinyint(1) not null default 1 comment '是否启用：1启用，0停用',
  `create_by` varchar(64) default '' comment '创建者',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_by` varchar(64) default '' comment '更新者',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`llm_config_id`),
  unique key `uq_p1p_llm_config_name` (`config_name`),
  key `idx_p1p_llm_config_enabled` (`enabled`),
  constraint `ck_p1p_llm_config_enabled` check (`enabled` in (0, 1))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='LLM 配置表';

drop table if exists `p1p_match_task`;
create table `p1p_match_task` (
  `task_id` bigint not null auto_increment comment '任务主键',
  `task_name` varchar(200) not null comment '任务名称',
  `current_version_id` bigint default null comment '当前最新版本 ID',
  `source_datasource_id` bigint default null comment '当前来源数据源 ID，冗余便于列表查询',
  `lifecycle_status` varchar(20) not null default 'active' comment '生命周期状态：draft 草稿、active 启用、disabled 停用、archived 归档',
  `last_record_status` varchar(30) default null comment '最近运行状态摘要',
  `last_record_stage` varchar(50) default null comment '最近运行阶段摘要',
  `status` char(1) default '0' comment '若依状态：0正常，1停用',
  `del_flag` char(1) default '0' comment '删除标志：0存在，2删除',
  `create_by` varchar(64) default '' comment '创建者',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_by` varchar(64) default '' comment '更新者',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`task_id`),
  key `idx_p1p_match_task_name` (`task_name`),
  key `idx_p1p_match_task_source` (`source_datasource_id`),
  key `idx_p1p_match_task_current_version` (`current_version_id`),
  constraint `ck_p1p_match_task_lifecycle_status` check (`lifecycle_status` in ('draft', 'active', 'disabled', 'archived')),
  constraint `ck_p1p_match_task_status` check (`status` in ('0', '1')),
  constraint `ck_p1p_match_task_del_flag` check (`del_flag` in ('0', '2'))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='模式映射任务主表';

drop table if exists `p1p_match_task_version`;
create table `p1p_match_task_version` (
  `version_id` bigint not null auto_increment comment '任务版本主键',
  `task_id` bigint not null comment '所属任务 ID',
  `version_no` int not null comment '版本号，第一次新增为 1，修改后递增',
  `source_datasource_id` bigint not null comment '来源数据源 ID',
  `schema_snapshot_id` bigint default null comment '本版本使用的数据源模式快照 ID',
  `target_key` varchar(50) not null default 'dossier' comment '目标系统标识，当前固定为 dossier',
  `encoding_mode_key` varchar(100) not null comment '编码模式键',
  `embedding_model_key` varchar(100) not null comment '嵌入模型键',
  `eval_mode_key` varchar(100) not null comment '评估模式键',
  `llm_config_id` bigint default null comment 'LLM 配置 ID',
  `auto_approve_threshold` decimal(5,4) not null default 0.9000 comment '自动通过阈值',
  `algorithm_params_json` json comment '算法参数配置 JSON',
  `change_summary` text comment '版本变更摘要',
  `create_by` varchar(64) default '' comment '创建者',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_by` varchar(64) default '' comment '更新者',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`version_id`),
  unique key `uq_p1p_task_version` (`task_id`, `version_no`),
  key `idx_p1p_task_version_source` (`source_datasource_id`),
  key `idx_p1p_task_version_schema` (`schema_snapshot_id`),
  key `idx_p1p_match_task_version_encoding_mode_key` (`encoding_mode_key`),
  key `idx_p1p_match_task_version_embedding_model_key` (`embedding_model_key`),
  key `idx_p1p_match_task_version_eval_mode_key` (`eval_mode_key`),
  key `idx_p1p_match_task_version_llm_config_id` (`llm_config_id`)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='模式映射任务版本表';

drop table if exists `p1p_match_task_record`;
create table `p1p_match_task_record` (
  `record_id` bigint not null auto_increment comment '运行记录主键',
  `task_id` bigint not null comment '所属任务 ID',
  `version_id` bigint not null comment '执行任务版本 ID',
  `schema_snapshot_id` bigint default null comment '运行使用的数据源模式快照 ID',
  `default_result_set_id` bigint default null comment '本次运行默认结果集 ID',
  `execution_status` varchar(30) not null default 'queued' comment '运行状态：queued 排队、running 运行中、success 成功、failed 失败、blocked 阻断、canceled 已取消',
  `current_stage` varchar(50) not null default 'queued' comment '当前执行阶段',
  `progress` int default 0 comment '运行进度百分比',
  `algorithm_params_snapshot` json comment '算法参数运行快照 JSON',
  `results_dir` varchar(500) default null comment '算法结果目录',
  `log_file` varchar(500) default null comment '运行日志文件路径',
  `error_message` text comment '错误信息',
  `cancel_requested_at` datetime default null comment '取消请求时间',
  `canceled_at` datetime default null comment '取消完成时间',
  `started_at` datetime default null comment '开始时间',
  `finished_at` datetime default null comment '结束时间',
  `create_time` datetime default current_timestamp comment '创建时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`record_id`),
  key `idx_p1p_record_task_version` (`task_id`, `version_id`),
  key `idx_p1p_record_schema` (`schema_snapshot_id`),
  key `idx_p1p_record_default_result` (`default_result_set_id`),
  key `idx_p1p_record_status` (`execution_status`),
  key `idx_p1p_match_task_record_version_id` (`version_id`),
  constraint `ck_p1p_match_task_record_execution_status` check (`execution_status` in ('queued', 'running', 'success', 'partial', 'failed', 'blocked', 'canceled'))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='模式映射运行记录表';

drop table if exists `p1p_task_metric`;
create table `p1p_task_metric` (
  `metric_id` bigint not null auto_increment comment '评估指标主键',
  `task_id` bigint not null comment '任务 ID',
  `version_id` bigint default null comment '任务版本 ID',
  `record_id` bigint not null comment '运行记录 ID',
  `result_set_id` bigint default null comment '结果集 ID',
  `metric_key` varchar(100) not null comment '指标键，如 MRR、Recall@20、All F1Score',
  `metric_name` varchar(100) not null comment '指标显示名称',
  `metric_value` decimal(12,6) default null comment '指标数值',
  `chart_type` varchar(50) default null comment '图表类型，如 bar、stacked_bar、summary',
  `chart_data` json comment '图表展示数据 JSON',
  `result_dir` varchar(500) default null comment '算法结果目录',
  `create_time` datetime default current_timestamp comment '创建时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`metric_id`),
  unique key `uq_p1p_task_metric_result_key` (`result_set_id`, `metric_key`),
  key `idx_p1p_task_metric_record_key` (`record_id`, `metric_key`),
  key `idx_p1p_task_metric_result_set` (`result_set_id`),
  key `idx_p1p_task_metric_task_id` (`task_id`),
  key `idx_p1p_task_metric_version_id` (`version_id`)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='模式映射评估指标表';

drop table if exists `p1p_match_result_version`;
create table `p1p_match_result_version` (
  `result_version_id` bigint not null auto_increment comment '结果版本主键',
  `task_id` bigint not null comment '任务 ID',
  `version_id` bigint not null comment '任务版本 ID',
  `record_id` bigint not null comment '运行记录 ID',
  `version_label` varchar(100) default null comment '结果版本标签',
  `create_time` datetime default current_timestamp comment '创建时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`result_version_id`),
  key `idx_p1p_result_version_task_record` (`task_id`, `record_id`),
  key `idx_p1p_match_result_version_version_id` (`version_id`),
  key `idx_p1p_match_result_version_record_id` (`record_id`)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='模式映射结果版本表';

drop table if exists `p1p_match_result_set`;
create table `p1p_match_result_set` (
  `result_set_id` bigint not null auto_increment comment '结果集主键',
  `result_version_id` bigint not null comment '结果版本 ID',
  `task_id` bigint not null comment '任务 ID',
  `version_id` bigint not null comment '任务版本 ID',
  `record_id` bigint not null comment '运行记录 ID',
  `source_datasource_id` bigint not null comment '来源数据源 ID',
  `method` varchar(100) not null comment '匹配方法',
  `variant` varchar(50) not null default 'default' comment '方法变体',
  `result_set_name` varchar(200) default null comment '结果集名称',
  `is_default` tinyint(1) not null default 0 comment '是否候选默认结果集：1是，0否；接入执行仍以最终接入规则集为准',
  `total_rows` int default 0 comment '结果行数',
  `avg_score` decimal(8,6) default null comment '平均匹配分数',
  `create_by` varchar(64) default '' comment '创建者',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_by` varchar(64) default '' comment '更新者',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`result_set_id`),
  unique key `uq_p1p_result_set_record_method` (`record_id`, `method`, `variant`),
  key `idx_p1p_result_set_task_version` (`task_id`, `version_id`),
  key `idx_p1p_result_set_default` (`source_datasource_id`, `is_default`),
  key `idx_p1p_match_result_set_result_version_id` (`result_version_id`),
  key `idx_p1p_match_result_set_version_id` (`version_id`),
  constraint `ck_p1p_match_result_set_is_default` check (`is_default` in (0, 1))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='模式映射结果集表';

drop table if exists `p1p_match_result_row`;
create table `p1p_match_result_row` (
  `result_row_id` bigint not null auto_increment comment '结果明细主键',
  `result_set_id` bigint not null comment '结果集 ID',
  `row_no` int not null comment '结果行号',
  `source_database` varchar(100) default null comment '源数据库名',
  `source_table` varchar(100) not null comment '源表名',
  `source_column` varchar(100) not null comment '源字段名',
  `target_table` varchar(100) not null comment '目标表名',
  `target_column` varchar(100) not null comment '目标字段名',
  `score` decimal(8,6) default null comment '匹配分数',
  `raw_payload` json comment '原始算法结果 JSON',
  `create_time` datetime default current_timestamp comment '创建时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`result_row_id`),
  key `idx_p1p_result_row_set` (`result_set_id`),
  key `idx_p1p_result_row_source` (`source_table`, `source_column`),
  key `idx_p1p_result_row_target` (`target_table`, `target_column`)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='模式映射结果明细表';

drop table if exists `p1p_reviewed_match`;
create table `p1p_reviewed_match` (
  `review_id` bigint not null auto_increment comment '审核记录主键',
  `task_id` bigint not null comment '任务 ID',
  `version_id` bigint default null comment '任务版本 ID',
  `record_id` bigint default null comment '运行记录 ID',
  `result_set_id` bigint not null comment '结果集 ID',
  `result_row_id` bigint default null comment '结果明细 ID',
  `source_datasource_id` bigint not null comment '来源数据源 ID',
  `source_database` varchar(100) default null comment '源数据库名',
  `source_table` varchar(100) not null comment '源表名',
  `source_column` varchar(100) not null comment '源字段名',
  `proposed_target_table` varchar(100) default null comment '推荐目标表',
  `proposed_target_column` varchar(100) default null comment '推荐目标字段',
  `target_table` varchar(100) not null comment '审核确认目标表',
  `target_column` varchar(100) not null comment '审核确认目标字段',
  `mapping_type` varchar(30) default 'direct' comment '映射类型，如 direct 直接写入、rename_cast 重命名转换、lookup_normalize 字典归一',
  `algorithm_score` decimal(8,6) default null comment '算法匹配分数',
  `review_status` varchar(20) default 'pending' comment '审核状态：pending 待审核、auto_approved 自动通过、approved 审核通过、rejected 审核未通过',
  `reviewed_by` varchar(100) default null comment '审核人',
  `reviewed_at` datetime default null comment '审核时间',
  `create_time` datetime default current_timestamp comment '创建时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`review_id`),
  unique key `uq_p1p_review_result_row` (`result_row_id`),
  key `idx_p1p_review_task_record` (`task_id`, `record_id`),
  key `idx_p1p_review_record_source` (`record_id`, `source_table`, `source_column`),
  key `idx_p1p_review_status` (`review_status`),
  key `idx_p1p_reviewed_match_version_id` (`version_id`),
  key `idx_p1p_reviewed_match_result_set_id` (`result_set_id`),
  key `idx_p1p_reviewed_match_result_row_id` (`result_row_id`),
  key `idx_p1p_reviewed_match_source_datasource_id` (`source_datasource_id`),
  constraint `ck_p1p_reviewed_match_review_status` check (`review_status` in ('pending', 'auto_approved', 'approved', 'rejected'))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='审核后的字段匹配结果表';

drop table if exists `p1p_review_history`;
create table `p1p_review_history` (
  `history_id` bigint not null auto_increment comment '审核历史主键',
  `review_id` bigint not null comment '审核记录 ID',
  `task_id` bigint default null comment '任务 ID',
  `version_id` bigint default null comment '任务版本 ID',
  `record_id` bigint default null comment '运行记录 ID',
  `old_status` varchar(20) default null comment '原审核状态',
  `new_status` varchar(20) default null comment '新审核状态',
  `old_target_table` varchar(100) default null comment '原目标表',
  `old_target_column` varchar(100) default null comment '原目标字段',
  `new_target_table` varchar(100) default null comment '新目标表',
  `new_target_column` varchar(100) default null comment '新目标字段',
  `actor` varchar(100) default null comment '操作人',
  `action_type` varchar(50) default null comment '操作类型：approve 通过、reject 驳回、modify 修改',
  `create_time` datetime default current_timestamp comment '创建时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`history_id`),
  key `idx_p1p_review_history_review` (`review_id`),
  key `idx_p1p_review_history_task_record` (`task_id`, `record_id`),
  key `idx_p1p_review_history_version_id` (`version_id`),
  key `idx_p1p_review_history_record_id` (`record_id`)
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='审核历史表';

drop table if exists `p1p_mapping_spec_set`;
create table `p1p_mapping_spec_set` (
  `spec_set_id` bigint not null auto_increment comment '最终接入规则集主键',
  `spec_set_name` varchar(200) not null comment '规则集名称',
  `result_set_id` bigint not null comment '来源原始结果集 ID，用于追溯算法输出',
  `source_datasource_id` bigint not null comment '来源数据源 ID',
  `task_id` bigint default null comment '任务 ID',
  `version_id` bigint default null comment '任务版本 ID',
  `record_id` bigint default null comment '运行记录 ID',
  `rule_count` int default 0 comment '最终接入规则数量',
  `is_default` tinyint(1) not null default 0 comment '是否当前默认接入规则集：1是，0否',
  `spec_status` varchar(20) not null default 'active' comment '规则集状态：draft 草稿、active 生效、disabled 停用',
  `create_by` varchar(64) default '' comment '创建者',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_by` varchar(64) default '' comment '更新者',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`spec_set_id`),
  key `idx_p1p_spec_set_name` (`spec_set_name`),
  key `idx_p1p_spec_set_result_set` (`result_set_id`),
  key `idx_p1p_spec_set_default` (`source_datasource_id`, `is_default`),
  key `idx_p1p_spec_set_status` (`spec_status`),
  key `idx_p1p_mapping_spec_set_task_id` (`task_id`),
  key `idx_p1p_mapping_spec_set_version_id` (`version_id`),
  key `idx_p1p_mapping_spec_set_record_id` (`record_id`),
  constraint `ck_p1p_mapping_spec_set_is_default` check (`is_default` in (0, 1)),
  constraint `ck_p1p_mapping_spec_set_spec_status` check (`spec_status` in ('draft', 'active', 'disabled', 'approved', 'blocked'))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='最终接入规则集表';

drop table if exists `p1p_mapping_spec`;
create table `p1p_mapping_spec` (
  `mapping_id` bigint not null auto_increment comment '映射规则主键',
  `spec_set_id` bigint not null comment '最终接入规则集 ID',
  `task_id` bigint default null comment '任务 ID',
  `version_id` bigint default null comment '任务版本 ID',
  `record_id` bigint default null comment '运行记录 ID',
  `source_datasource_id` bigint not null comment '来源数据源 ID',
  `review_id` bigint default null comment '审核记录 ID',
  `source_database` varchar(100) default null comment '源数据库名',
  `source_table` varchar(100) not null comment '源表名',
  `source_column` varchar(100) not null comment '源字段名',
  `target_table` varchar(100) not null comment '目标表名',
  `target_column` varchar(100) not null comment '目标字段名',
  `mapping_type` varchar(30) not null default 'direct' comment '映射类型：direct 直接写入、rename_cast 重命名转换、lookup_normalize 字典归一',
  `transform_rule` json comment '字段转换规则 JSON',
  `spec_status` varchar(20) default 'approved' comment '规则状态：draft 草稿、approved 已确认、blocked 阻断',
  `load_order` int default 0 comment '目标字段装载顺序',
  `create_by` varchar(64) default '' comment '创建者',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_by` varchar(64) default '' comment '更新者',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`mapping_id`),
  unique key `uq_p1p_mapping_review` (`review_id`),
  key `idx_p1p_mapping_spec_set` (`spec_set_id`),
  key `idx_p1p_mapping_source_table` (`source_datasource_id`, `source_table`),
  key `idx_p1p_mapping_target` (`target_table`, `target_column`),
  key `idx_p1p_mapping_spec_task_id` (`task_id`),
  key `idx_p1p_mapping_spec_version_id` (`version_id`),
  key `idx_p1p_mapping_spec_record_id` (`record_id`),
  constraint `ck_p1p_mapping_spec_spec_status` check (`spec_status` in ('draft', 'active', 'disabled', 'approved', 'blocked'))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='最终接入字段映射规则明细表';

drop table if exists `p1p_access_plan`;
create table `p1p_access_plan` (
  `access_plan_id` bigint not null auto_increment comment '接入计划主键',
  `plan_name` varchar(200) not null comment '接入计划名称',
  `source_datasource_id` bigint not null comment '来源数据源 ID',
  `access_mode` varchar(30) not null comment '接入方式：db_direct 数据库直连、api_pull API 拉取、api_push API 推送',
  `access_type` varchar(20) not null default 'once' comment '接入类型：once 一次性接入、continuous 持续接入',
  `cycle_hours` int default 1 comment '持续接入更新周期，单位小时，默认 1 小时',
  `spec_set_id` bigint not null comment '最终接入规则集 ID，接入执行依据',
  `use_status` varchar(30) not null default 'enabled' comment '使用状态：enabled 启用、paused 暂停、disabled 停用、unavailable 暂时不可用',
  `current_batch_id` bigint default null comment '当前或最近执行批次 ID',
  `last_success_count` int default 0 comment '最近一次成功数',
  `last_failed_count` int default 0 comment '最近一次失败数',
  `total_success_count` int default 0 comment '累计成功数',
  `total_failed_count` int default 0 comment '累计失败数',
  `last_execute_time` datetime default null comment '最近执行时间',
  `status` char(1) default '0' comment '若依状态：0正常，1停用',
  `del_flag` char(1) default '0' comment '删除标志：0存在，2删除',
  `create_by` varchar(64) default '' comment '创建者',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_by` varchar(64) default '' comment '更新者',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`access_plan_id`),
  key `idx_p1p_access_plan_name` (`plan_name`),
  key `idx_p1p_access_plan_source` (`source_datasource_id`),
  key `idx_p1p_access_plan_spec_set` (`spec_set_id`),
  key `idx_p1p_access_plan_use_status` (`use_status`),
  key `idx_p1p_access_plan_current_batch` (`current_batch_id`),
  constraint `ck_p1p_access_plan_access_mode` check (`access_mode` in ('db_direct', 'api_pull', 'api_push')),
  constraint `ck_p1p_access_plan_access_type` check (`access_type` in ('once', 'continuous')),
  constraint `ck_p1p_access_plan_use_status` check (`use_status` in ('enabled', 'paused', 'disabled', 'unavailable')),
  constraint `ck_p1p_access_plan_status` check (`status` in ('0', '1')),
  constraint `ck_p1p_access_plan_del_flag` check (`del_flag` in ('0', '2'))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='接入计划表';

drop table if exists `p1p_access_scope_table`;
create table `p1p_access_scope_table` (
  `scope_table_id` bigint not null auto_increment comment '接入范围源表目标表关系主键',
  `access_plan_id` bigint not null comment '接入计划 ID',
  `spec_set_id` bigint not null comment '最终接入规则集 ID',
  `source_datasource_id` bigint not null comment '来源数据源 ID',
  `source_database` varchar(100) default null comment '源数据库名',
  `source_table` varchar(100) not null comment '源表名',
  `target_table` varchar(100) not null comment '目标卷宗表名',
  `source_entity_id` bigint default null comment '源模式实体 ID',
  `field_mapping_count` int default 0 comment '字段映射数量',
  `scope_status` varchar(20) default 'active' comment '范围状态：active 生效、disabled 停用',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`scope_table_id`),
  unique key `uq_p1p_access_scope_table` (`access_plan_id`, `source_table`, `target_table`),
  key `idx_p1p_access_scope_table_spec_set` (`spec_set_id`),
  key `idx_p1p_access_scope_table_entity` (`source_entity_id`),
  key `idx_p1p_access_scope_table_source_datasource_id` (`source_datasource_id`),
  constraint `ck_p1p_access_scope_table_scope_status` check (`scope_status` in ('active', 'disabled'))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='接入范围源表目标表关系表';

drop table if exists `p1p_access_scope_field`;
create table `p1p_access_scope_field` (
  `scope_field_id` bigint not null auto_increment comment '接入范围字段主键',
  `scope_table_id` bigint not null comment '接入范围目标表 ID',
  `access_plan_id` bigint not null comment '接入计划 ID',
  `mapping_id` bigint not null comment '最终映射规则 ID',
  `source_field_id` bigint default null comment '源模式字段 ID',
  `source_column` varchar(100) not null comment '源字段名',
  `target_column` varchar(100) not null comment '目标字段名',
  `mapping_type` varchar(30) not null default 'direct' comment '映射类型：direct 直接写入、rename_cast 重命名转换、lookup_normalize 字典归一',
  `transform_rule` json comment '字段转换规则 JSON',
  `field_status` varchar(20) default 'active' comment '字段范围状态：active 生效、disabled 停用',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`scope_field_id`),
  unique key `uq_p1p_access_scope_field` (`scope_table_id`, `source_column`, `target_column`),
  key `idx_p1p_access_scope_field_plan` (`access_plan_id`),
  key `idx_p1p_access_scope_field_mapping` (`mapping_id`),
  key `idx_p1p_access_scope_field_source_field_id` (`source_field_id`),
  constraint `ck_p1p_access_scope_field_field_status` check (`field_status` in ('active', 'disabled'))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='接入范围字段表';

drop table if exists `p1p_access_batch`;
create table `p1p_access_batch` (
  `access_batch_id` bigint not null auto_increment comment '接入执行批次主键',
  `access_plan_id` bigint not null comment '接入计划 ID',
  `batch_no` varchar(100) not null comment '执行批次号',
  `trigger_type` varchar(20) not null default 'manual' comment '触发方式：manual 手动、schedule 定时',
  `batch_status` varchar(20) not null default 'pending' comment '批次状态：pending 待执行、running 执行中、success 成功、partial 部分成功、failed 失败、canceled 已取消',
  `read_count` int default 0 comment '源端读取记录数',
  `staged_count` int default 0 comment '写入中间库记录数',
  `transform_success_count` int default 0 comment '转换成功数',
  `transform_failed_count` int default 0 comment '转换失败数',
  `write_success_count` int default 0 comment '写入成功数',
  `write_failed_count` int default 0 comment '写入失败数',
  `inserted_count` int default 0 comment '本次新增数',
  `updated_count` int default 0 comment '本次更新数',
  `error_message` text comment '批次错误信息',
  `cancel_requested_at` datetime default null comment '取消请求时间',
  `started_at` datetime default null comment '开始时间',
  `finished_at` datetime default null comment '结束时间',
  `create_by` varchar(64) default '' comment '创建者',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_by` varchar(64) default '' comment '更新者',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`access_batch_id`),
  unique key `uq_p1p_access_batch_no` (`batch_no`),
  key `idx_p1p_access_batch_plan` (`access_plan_id`),
  key `idx_p1p_access_batch_status` (`batch_status`),
  constraint `ck_p1p_access_batch_trigger_type` check (`trigger_type` in ('manual', 'schedule')),
  constraint `ck_p1p_access_batch_batch_status` check (`batch_status` in ('pending', 'running', 'success', 'partial', 'failed', 'canceled'))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='接入执行批次表';

drop table if exists `p1p_access_stage_record`;
create table `p1p_access_stage_record` (
  `stage_record_id` bigint not null auto_increment comment '接入中间库原始记录主键',
  `access_batch_id` bigint not null comment '接入执行批次 ID',
  `access_plan_id` bigint not null comment '接入计划 ID',
  `scope_table_id` bigint not null comment '接入范围目标表 ID',
  `source_datasource_id` bigint not null comment '来源数据源 ID',
  `source_database` varchar(100) default null comment '源数据库名',
  `source_table` varchar(100) not null comment '源表名',
  `source_pk` varchar(200) default null comment '源记录主键值或业务唯一键',
  `source_operation` varchar(20) default 'upsert' comment '源端操作类型：insert 新增、update 更新、upsert 新增或更新',
  `raw_data` json not null comment '源端读取到的原始数据 JSON',
  `read_status` varchar(20) not null default 'success' comment '读取状态：success 成功、failed 失败',
  `read_time` datetime default null comment '读取时间',
  `error_message` text comment '读取失败原因',
  `create_time` datetime default current_timestamp comment '创建时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`stage_record_id`),
  key `idx_p1p_stage_record_batch` (`access_batch_id`),
  key `idx_p1p_stage_record_plan_table` (`access_plan_id`, `source_table`),
  key `idx_p1p_stage_record_scope_pk` (`scope_table_id`, `source_pk`),
  key `idx_p1p_access_stage_record_source_datasource_id` (`source_datasource_id`),
  constraint `ck_p1p_access_stage_record_read_status` check (`read_status` in ('success', 'failed'))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='接入中间库原始记录表';

drop table if exists `p1p_access_transform_record`;
create table `p1p_access_transform_record` (
  `transform_record_id` bigint not null auto_increment comment '接入转换记录主键',
  `stage_record_id` bigint not null comment '接入中间库原始记录 ID',
  `access_batch_id` bigint not null comment '接入执行批次 ID',
  `access_plan_id` bigint not null comment '接入计划 ID',
  `scope_table_id` bigint not null comment '接入范围目标表 ID',
  `target_table` varchar(100) not null comment '目标卷宗表名',
  `target_pk` varchar(200) default null comment '目标记录主键值或业务唯一键',
  `transformed_data` json comment '格式转换后的目标数据 JSON',
  `transform_status` varchar(20) not null default 'pending' comment '转换状态：pending 待转换、success 成功、failed 失败',
  `write_status` varchar(20) not null default 'pending' comment '写入状态：pending 待写入、inserted 已新增、updated 已更新、failed 失败、skipped 跳过',
  `error_message` text comment '转换或写入失败原因',
  `transformed_at` datetime default null comment '转换时间',
  `written_at` datetime default null comment '写入时间',
  `create_time` datetime default current_timestamp comment '创建时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`transform_record_id`),
  unique key `uq_p1p_transform_stage_record` (`stage_record_id`),
  key `idx_p1p_transform_batch` (`access_batch_id`),
  key `idx_p1p_transform_scope_target` (`scope_table_id`, `target_table`),
  key `idx_p1p_transform_write_status` (`write_status`),
  key `idx_p1p_access_transform_record_access_plan_id` (`access_plan_id`),
  constraint `ck_p1p_access_transform_record_transform_status` check (`transform_status` in ('pending', 'success', 'failed')),
  constraint `ck_p1p_access_transform_record_write_status` check (`write_status` in ('pending', 'inserted', 'updated', 'failed', 'skipped'))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='接入转换记录表';

drop table if exists `p1p_access_table_result`;
create table `p1p_access_table_result` (
  `table_result_id` bigint not null auto_increment comment '目标表接入结果主键',
  `access_batch_id` bigint not null comment '接入执行批次 ID',
  `access_plan_id` bigint not null comment '接入计划 ID',
  `scope_table_id` bigint not null comment '接入范围目标表 ID',
  `source_table` varchar(100) not null comment '源表名',
  `target_table` varchar(100) not null comment '目标卷宗表名',
  `result_status` varchar(20) not null default 'pending' comment '结果状态：pending 待执行、running 执行中、success 成功、partial 部分成功、failed 失败',
  `read_count` int default 0 comment '源端读取数',
  `staged_count` int default 0 comment '中间库暂存数',
  `inserted_count` int default 0 comment '本次新增数',
  `updated_count` int default 0 comment '本次更新数',
  `success_count` int default 0 comment '本次成功数',
  `failed_count` int default 0 comment '本次失败数',
  `total_success_count` int default 0 comment '累计成功数',
  `total_failed_count` int default 0 comment '累计失败数',
  `last_execute_time` datetime default null comment '最近执行时间',
  `message` varchar(500) default null comment '结果说明',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`table_result_id`),
  unique key `uq_p1p_table_result_batch_target` (`access_batch_id`, `target_table`),
  key `idx_p1p_table_result_plan` (`access_plan_id`),
  key `idx_p1p_table_result_scope` (`scope_table_id`),
  key `idx_p1p_table_result_status` (`result_status`),
  constraint `ck_p1p_access_table_result_result_status` check (`result_status` in ('pending', 'running', 'success', 'partial', 'failed'))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='目标表接入结果表';

drop table if exists `p1p_access_field_result`;
create table `p1p_access_field_result` (
  `field_result_id` bigint not null auto_increment comment '目标字段接入结果主键',
  `table_result_id` bigint not null comment '目标表接入结果 ID',
  `access_batch_id` bigint not null comment '接入执行批次 ID',
  `access_plan_id` bigint not null comment '接入计划 ID',
  `scope_field_id` bigint not null comment '接入范围字段 ID',
  `source_column` varchar(100) not null comment '源字段名',
  `target_column` varchar(100) not null comment '目标字段名',
  `mapping_type` varchar(30) not null default 'direct' comment '映射类型：direct 直接写入、rename_cast 重命名转换、lookup_normalize 字典归一',
  `transform_status` varchar(20) not null default 'pending' comment '转换状态：pending 待转换、success 成功、failed 失败',
  `success_count` int default 0 comment '字段转换成功数',
  `failed_count` int default 0 comment '字段转换失败数',
  `error_message` text comment '字段转换失败原因',
  `create_time` datetime default current_timestamp comment '创建时间',
  `update_time` datetime default null on update current_timestamp comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`field_result_id`),
  unique key `uq_p1p_field_result_table_column` (`table_result_id`, `target_column`),
  key `idx_p1p_field_result_batch` (`access_batch_id`),
  key `idx_p1p_field_result_scope_field` (`scope_field_id`),
  key `idx_p1p_access_field_result_access_plan_id` (`access_plan_id`),
  constraint `ck_p1p_access_field_result_transform_status` check (`transform_status` in ('pending', 'success', 'failed'))
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='目标字段接入结果表';
set foreign_key_checks = 1;
