-- 协同机制生成页面任务信息升级
-- 用途：在已有 p2_design_task 表上增加计划开始时间和预计结束时间字段。
-- 说明：附件仍使用已有 p2_design_task_file 表，本脚本不重复建附件表。

ALTER TABLE p2_design_task
  ADD COLUMN planned_start_time DATETIME NULL COMMENT '任务开始时间' AFTER description,
  ADD COLUMN planned_end_time DATETIME NULL COMMENT '预计结束时间' AFTER planned_start_time;

