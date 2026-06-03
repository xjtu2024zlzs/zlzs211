SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 算法结果表
CREATE TABLE IF NOT EXISTS `algorithm_task_results` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` varchar(64) NOT NULL COMMENT '算法任务ID',
  `task_type` varchar(64) NOT NULL COMMENT '算法任务类型，如 FEATURE_ANALYSIS、DEGRADATION_DETECT、FAULT_PREDICT',
  `task_name` varchar(128) DEFAULT NULL COMMENT '算法任务名称',
  `status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态：PENDING/RUNNING/SUCCESS/FAILED',

  `request_id` varchar(64) DEFAULT NULL COMMENT '请求流水号',
  `algorithm_version` varchar(32) DEFAULT NULL COMMENT '算法版本',

  `aircraft_id` varchar(64) DEFAULT NULL COMMENT '飞机ID',
  `subsystem_id` varchar(64) DEFAULT NULL COMMENT '分系统ID',
  `equipment_id` varchar(64) DEFAULT NULL COMMENT '设备ID',
  `component_id` varchar(64) DEFAULT NULL COMMENT '组件ID',

  `business_object_id` varchar(64) DEFAULT NULL COMMENT '本次选择的业务对象ID',
  `business_object_level` varchar(32) DEFAULT NULL COMMENT '业务对象层级：aircraft/subsystem/equipment/component',
  `business_object_name` varchar(128) DEFAULT NULL COMMENT '业务对象名称',

  `feature_analysis_task_id` varchar(64) DEFAULT NULL COMMENT '上游特征分析任务ID，仅退化点检测使用',

  `summary_result` varchar(512) DEFAULT NULL COMMENT '摘要结果',
  `result_value` varchar(128) DEFAULT NULL COMMENT '主要结果值',
  `result_unit` varchar(64) DEFAULT NULL COMMENT '主要结果单位',

  `request_json` longtext COMMENT '算法请求JSON',
  `result_json` longtext COMMENT '算法返回结果JSON',
  `error_message` text COMMENT '失败原因',

  `started_at` datetime DEFAULT NULL COMMENT '开始执行时间',
  `finished_at` datetime DEFAULT NULL COMMENT '完成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_algorithm_task_results_task_id` (`task_id`),
  KEY `idx_algorithm_task_results_task_type` (`task_type`),
  KEY `idx_algorithm_task_results_status` (`status`),
  KEY `idx_algorithm_task_results_aircraft_id` (`aircraft_id`),
  KEY `idx_algorithm_task_results_subsystem_id` (`subsystem_id`),
  KEY `idx_algorithm_task_results_equipment_id` (`equipment_id`),
  KEY `idx_algorithm_task_results_component_id` (`component_id`),
  KEY `idx_algorithm_task_results_business_object` (`business_object_id`, `business_object_level`),
  KEY `idx_algorithm_task_results_business_object_id` (`business_object_id`),
  KEY `idx_algorithm_task_results_feature_task_id` (`feature_analysis_task_id`),
  KEY `idx_algorithm_task_results_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通用算法任务结果表';

SET FOREIGN_KEY_CHECKS = 1;
