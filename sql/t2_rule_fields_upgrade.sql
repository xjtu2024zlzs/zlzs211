-- Add reserved executable-rule fields for objective and constraint catalogs.
-- Run this script once before `t2_refined_optimization_plan_init.sql`.
-- If a column already exists, skip the repeated ALTER statement in your SQL tool.

SET NAMES utf8mb4;

ALTER TABLE t2_design_objective_catalog
  ADD COLUMN rule_type varchar(64) DEFAULT NULL COMMENT '规则类型' AFTER remark;
ALTER TABLE t2_design_objective_catalog
  ADD COLUMN rule_expression varchar(500) DEFAULT NULL COMMENT '规则表达式' AFTER rule_type;
ALTER TABLE t2_design_objective_catalog
  ADD COLUMN target_field varchar(128) DEFAULT NULL COMMENT '校核对象字段' AFTER rule_expression;
ALTER TABLE t2_design_objective_catalog
  ADD COLUMN reference_field varchar(128) DEFAULT NULL COMMENT '参考对象字段' AFTER target_field;
ALTER TABLE t2_design_objective_catalog
  ADD COLUMN operator_code varchar(32) DEFAULT NULL COMMENT '执行操作符' AFTER reference_field;
ALTER TABLE t2_design_objective_catalog
  ADD COLUMN threshold_value varchar(100) DEFAULT NULL COMMENT '执行阈值' AFTER operator_code;
ALTER TABLE t2_design_objective_catalog
  ADD COLUMN execute_mode varchar(32) DEFAULT 'reserved' COMMENT '执行模式：reserved/manual/auto' AFTER threshold_value;
ALTER TABLE t2_design_objective_catalog
  ADD COLUMN rule_payload text COMMENT '扩展规则参数JSON' AFTER execute_mode;

ALTER TABLE t2_design_objective_constraint
  ADD COLUMN rule_type varchar(64) DEFAULT NULL COMMENT '规则类型' AFTER remark;
ALTER TABLE t2_design_objective_constraint
  ADD COLUMN rule_expression varchar(500) DEFAULT NULL COMMENT '规则表达式' AFTER rule_type;
ALTER TABLE t2_design_objective_constraint
  ADD COLUMN target_field varchar(128) DEFAULT NULL COMMENT '校核对象字段' AFTER rule_expression;
ALTER TABLE t2_design_objective_constraint
  ADD COLUMN reference_field varchar(128) DEFAULT NULL COMMENT '参考对象字段' AFTER target_field;
ALTER TABLE t2_design_objective_constraint
  ADD COLUMN operator_code varchar(32) DEFAULT NULL COMMENT '执行操作符' AFTER reference_field;
ALTER TABLE t2_design_objective_constraint
  ADD COLUMN threshold_value varchar(100) DEFAULT NULL COMMENT '执行阈值' AFTER operator_code;
ALTER TABLE t2_design_objective_constraint
  ADD COLUMN execute_mode varchar(32) DEFAULT 'reserved' COMMENT '执行模式：reserved/manual/auto' AFTER threshold_value;
ALTER TABLE t2_design_objective_constraint
  ADD COLUMN rule_payload text COMMENT '扩展规则参数JSON' AFTER execute_mode;
