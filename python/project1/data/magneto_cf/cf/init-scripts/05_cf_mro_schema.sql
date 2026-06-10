-- Auto-generated from DBML for CF aviation simulated dataset.
-- Source DBML: MRO源系统数据库表设计.dbml
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

CREATE TABLE IF NOT EXISTS "service_event" (
  "service_event_id" SERIAL PRIMARY KEY,
  "event_no" VARCHAR(100) NOT NULL,
  "component_serial_no" VARCHAR(100) NOT NULL,
  "install_pos_code" VARCHAR(100),
  "service_time_text" VARCHAR(30),
  "event_kind_code" VARCHAR(30),
  "service_unit_code" VARCHAR(100),
  "service_unit_name" VARCHAR(200),
  "evt_state" VARCHAR(30),
  "event_summary" TEXT
);

CREATE TABLE IF NOT EXISTS "repair_order" (
  "repair_order_id" SERIAL PRIMARY KEY,
  "service_event_id" INTEGER NOT NULL,
  "repair_order_no" VARCHAR(100) NOT NULL,
  "component_serial_no" VARCHAR(100),
  "wo_no" VARCHAR(100),
  "task_title" VARCHAR(200),
  "plan_start_text" VARCHAR(30),
  "plan_finish_text" VARCHAR(30),
  "ro_state" VARCHAR(30),
  "report_man" VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS "fault_report" (
  "fault_report_id" SERIAL PRIMARY KEY,
  "service_event_id" INTEGER NOT NULL,
  "repair_order_id" INTEGER,
  "component_serial_no" VARCHAR(100),
  "fault_sym_code" VARCHAR(100) NOT NULL,
  "fault_title" VARCHAR(200),
  "fault_text" TEXT,
  "fail_pos_desc" VARCHAR(200),
  "found_at_text" VARCHAR(30),
  "finder_name" VARCHAR(100),
  "gzjb" VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS "remove_install_record" (
  "remove_install_record_id" SERIAL PRIMARY KEY,
  "service_event_id" INTEGER NOT NULL,
  "repair_order_id" INTEGER,
  "fault_report_id" INTEGER,
  "change_no" VARCHAR(100) NOT NULL,
  "component_serial_no" VARCHAR(100) NOT NULL,
  "install_pos_code" VARCHAR(100),
  "removed_part_sn" VARCHAR(100),
  "installed_part_sn" VARCHAR(100),
  "source_batch_no" VARCHAR(100),
  "remove_cause" VARCHAR(80),
  "change_time_text" VARCHAR(30),
  "remove_time_text" VARCHAR(30),
  "install_time_text" VARCHAR(30),
  "new_valid_from_text" VARCHAR(30),
  "operator_name" VARCHAR(100),
  "install_person" VARCHAR(100),
  "change_state" VARCHAR(30),
  "install_state" VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS "field_feedback" (
  "field_feedback_id" SERIAL PRIMARY KEY,
  "service_event_id" INTEGER,
  "fault_report_id" INTEGER,
  "quality_event_no" VARCHAR(100),
  "component_serial_no" VARCHAR(100),
  "feedback_no" VARCHAR(100) NOT NULL,
  "feedback_source_code" VARCHAR(50),
  "feedback_time_text" VARCHAR(30),
  "feedback_content" TEXT,
  "feedback_state" VARCHAR(30)
);

-- Comments
COMMENT ON TABLE "service_event" IS 'MRO 服役维修事件主表，作为维修工单、故障、拆换和反馈的结构锚点';
COMMENT ON COLUMN "service_event"."service_event_id" IS '服役维修事件源表主键';
COMMENT ON COLUMN "service_event"."event_no" IS '服役维修事件编号';
COMMENT ON COLUMN "service_event"."component_serial_no" IS '作动筒组件序列号';
COMMENT ON COLUMN "service_event"."install_pos_code" IS '装机位置编码或外场站位代码';
COMMENT ON COLUMN "service_event"."service_time_text" IS '源系统记录的服役或维修发生时间文本';
COMMENT ON COLUMN "service_event"."event_kind_code" IS '维修事件类型代码';
COMMENT ON COLUMN "service_event"."service_unit_code" IS '维修单位编码';
COMMENT ON COLUMN "service_event"."service_unit_name" IS '维修单位名称';
COMMENT ON COLUMN "service_event"."evt_state" IS '事件状态代码';
COMMENT ON COLUMN "service_event"."event_summary" IS '事件摘要或备注说明';

COMMENT ON TABLE "repair_order" IS 'MRO 维修工单表，承接服役事件并向故障报告和拆换记录传递工单上下文';
COMMENT ON COLUMN "repair_order"."repair_order_id" IS '维修工单源表主键';
COMMENT ON COLUMN "repair_order"."service_event_id" IS '所属服役维修事件主键';
COMMENT ON COLUMN "repair_order"."repair_order_no" IS '维修工单编号';
COMMENT ON COLUMN "repair_order"."component_serial_no" IS '维修对象组件序列号';
COMMENT ON COLUMN "repair_order"."wo_no" IS '统一工单号或维修派工号';
COMMENT ON COLUMN "repair_order"."task_title" IS '维修任务名称';
COMMENT ON COLUMN "repair_order"."plan_start_text" IS '计划开始时间文本';
COMMENT ON COLUMN "repair_order"."plan_finish_text" IS '计划完成时间文本';
COMMENT ON COLUMN "repair_order"."ro_state" IS '维修工单状态代码';
COMMENT ON COLUMN "repair_order"."report_man" IS '提交人姓名';

COMMENT ON TABLE "fault_report" IS 'MRO 故障报告表，记录服役维修阶段发现的故障现象和严重程度';
COMMENT ON COLUMN "fault_report"."fault_report_id" IS '故障报告源表主键';
COMMENT ON COLUMN "fault_report"."service_event_id" IS '所属服役维修事件主键';
COMMENT ON COLUMN "fault_report"."repair_order_id" IS '关联维修工单主键';
COMMENT ON COLUMN "fault_report"."component_serial_no" IS '故障涉及组件序列号';
COMMENT ON COLUMN "fault_report"."fault_sym_code" IS '故障现象代码';
COMMENT ON COLUMN "fault_report"."fault_title" IS '故障名称或标题';
COMMENT ON COLUMN "fault_report"."fault_text" IS '故障现象描述';
COMMENT ON COLUMN "fault_report"."fail_pos_desc" IS '故障部位说明';
COMMENT ON COLUMN "fault_report"."found_at_text" IS '故障发现时间文本';
COMMENT ON COLUMN "fault_report"."finder_name" IS '故障发现人姓名';
COMMENT ON COLUMN "fault_report"."gzjb" IS '故障级别代码';

COMMENT ON TABLE "remove_install_record" IS 'MRO 拆下与换上记录表，表达故障维修中的零件拆换和装配有效期变化';
COMMENT ON COLUMN "remove_install_record"."remove_install_record_id" IS '拆换记录源表主键';
COMMENT ON COLUMN "remove_install_record"."service_event_id" IS '所属服役维修事件主键';
COMMENT ON COLUMN "remove_install_record"."repair_order_id" IS '关联维修工单主键';
COMMENT ON COLUMN "remove_install_record"."fault_report_id" IS '触发拆换的故障报告主键';
COMMENT ON COLUMN "remove_install_record"."change_no" IS '拆换记录编号';
COMMENT ON COLUMN "remove_install_record"."component_serial_no" IS '拆换所在组件序列号';
COMMENT ON COLUMN "remove_install_record"."install_pos_code" IS '拆换发生装机位置编码';
COMMENT ON COLUMN "remove_install_record"."removed_part_sn" IS '拆下零件序列号';
COMMENT ON COLUMN "remove_install_record"."installed_part_sn" IS '换上零件序列号';
COMMENT ON COLUMN "remove_install_record"."source_batch_no" IS '换上件来源库存批次号';
COMMENT ON COLUMN "remove_install_record"."remove_cause" IS '拆下或更换原因代码';
COMMENT ON COLUMN "remove_install_record"."change_time_text" IS '拆换完成时间文本';
COMMENT ON COLUMN "remove_install_record"."remove_time_text" IS '拆下时间文本';
COMMENT ON COLUMN "remove_install_record"."install_time_text" IS '装上时间文本';
COMMENT ON COLUMN "remove_install_record"."new_valid_from_text" IS '新装配关系有效开始时间文本';
COMMENT ON COLUMN "remove_install_record"."operator_name" IS '拆换操作人姓名';
COMMENT ON COLUMN "remove_install_record"."install_person" IS '装配人员姓名';
COMMENT ON COLUMN "remove_install_record"."change_state" IS '拆换记录状态代码';
COMMENT ON COLUMN "remove_install_record"."install_state" IS '装配关系状态代码';

COMMENT ON TABLE "field_feedback" IS 'MRO 外场和用户反馈表，记录服役阶段质量信息并可转化为质量事件';
COMMENT ON COLUMN "field_feedback"."field_feedback_id" IS '外场反馈源表主键';
COMMENT ON COLUMN "field_feedback"."service_event_id" IS '关联服役维修事件主键';
COMMENT ON COLUMN "field_feedback"."fault_report_id" IS '关联故障报告主键';
COMMENT ON COLUMN "field_feedback"."quality_event_no" IS '反馈转化形成的质量事件编号';
COMMENT ON COLUMN "field_feedback"."component_serial_no" IS '反馈涉及组件序列号';
COMMENT ON COLUMN "field_feedback"."feedback_no" IS '外场反馈编号';
COMMENT ON COLUMN "field_feedback"."feedback_source_code" IS '反馈来源代码';
COMMENT ON COLUMN "field_feedback"."feedback_time_text" IS '反馈时间文本';
COMMENT ON COLUMN "field_feedback"."feedback_content" IS '反馈内容';
COMMENT ON COLUMN "field_feedback"."feedback_state" IS '反馈处理状态代码';

-- Foreign keys
ALTER TABLE "repair_order" ADD CONSTRAINT "fk_repair_order_service_event_id" FOREIGN KEY ("service_event_id") REFERENCES "service_event" ("service_event_id");
ALTER TABLE "fault_report" ADD CONSTRAINT "fk_fault_report_service_event_id" FOREIGN KEY ("service_event_id") REFERENCES "service_event" ("service_event_id");
ALTER TABLE "fault_report" ADD CONSTRAINT "fk_fault_report_repair_order_id" FOREIGN KEY ("repair_order_id") REFERENCES "repair_order" ("repair_order_id");
ALTER TABLE "remove_install_record" ADD CONSTRAINT "fk_remove_install_record_service_event_id" FOREIGN KEY ("service_event_id") REFERENCES "service_event" ("service_event_id");
ALTER TABLE "remove_install_record" ADD CONSTRAINT "fk_remove_install_record_repair_order_id" FOREIGN KEY ("repair_order_id") REFERENCES "repair_order" ("repair_order_id");
ALTER TABLE "remove_install_record" ADD CONSTRAINT "fk_remove_install_record_fault_report_id" FOREIGN KEY ("fault_report_id") REFERENCES "fault_report" ("fault_report_id");
ALTER TABLE "field_feedback" ADD CONSTRAINT "fk_field_feedback_service_event_id" FOREIGN KEY ("service_event_id") REFERENCES "service_event" ("service_event_id");
ALTER TABLE "field_feedback" ADD CONSTRAINT "fk_field_feedback_fault_report_id" FOREIGN KEY ("fault_report_id") REFERENCES "fault_report" ("fault_report_id");

-- Indexes
CREATE UNIQUE INDEX IF NOT EXISTS "uq_service_event_event_no" ON "service_event" ("event_no");
CREATE INDEX IF NOT EXISTS "idx_service_event_component_serial_no" ON "service_event" ("component_serial_no");
CREATE INDEX IF NOT EXISTS "idx_service_event_event_kind_code" ON "service_event" ("event_kind_code");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_repair_order_repair_order_no" ON "repair_order" ("repair_order_no");
CREATE INDEX IF NOT EXISTS "idx_repair_order_service_event_id" ON "repair_order" ("service_event_id");
CREATE INDEX IF NOT EXISTS "idx_repair_order_wo_no" ON "repair_order" ("wo_no");
CREATE INDEX IF NOT EXISTS "idx_fault_report_service_event_id" ON "fault_report" ("service_event_id");
CREATE INDEX IF NOT EXISTS "idx_fault_report_repair_order_id" ON "fault_report" ("repair_order_id");
CREATE INDEX IF NOT EXISTS "idx_fault_report_fault_sym_code" ON "fault_report" ("fault_sym_code");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_remove_install_record_change_no" ON "remove_install_record" ("change_no");
CREATE INDEX IF NOT EXISTS "idx_remove_install_record_service_event_id" ON "remove_install_record" ("service_event_id");
CREATE INDEX IF NOT EXISTS "idx_remove_install_record_repair_order_id" ON "remove_install_record" ("repair_order_id");
CREATE INDEX IF NOT EXISTS "idx_remove_install_record_fault_report_id" ON "remove_install_record" ("fault_report_id");
CREATE INDEX IF NOT EXISTS "idx_remove_install_record_removed_part_sn" ON "remove_install_record" ("removed_part_sn");
CREATE INDEX IF NOT EXISTS "idx_remove_install_record_installed_part_sn" ON "remove_install_record" ("installed_part_sn");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_field_feedback_feedback_no" ON "field_feedback" ("feedback_no");
CREATE INDEX IF NOT EXISTS "idx_field_feedback_service_event_id" ON "field_feedback" ("service_event_id");
CREATE INDEX IF NOT EXISTS "idx_field_feedback_fault_report_id" ON "field_feedback" ("fault_report_id");
CREATE INDEX IF NOT EXISTS "idx_field_feedback_quality_event_no" ON "field_feedback" ("quality_event_no");
CREATE INDEX IF NOT EXISTS "idx_field_feedback_component_serial_no" ON "field_feedback" ("component_serial_no");
