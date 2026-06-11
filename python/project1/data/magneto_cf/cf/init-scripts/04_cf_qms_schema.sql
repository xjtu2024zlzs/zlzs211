-- Auto-generated from DBML for CF aviation simulated dataset.
-- Source DBML: QMS源系统数据库表设计.dbml
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

CREATE TABLE IF NOT EXISTS "quality_event_doc" (
  "quality_event_doc_id" SERIAL PRIMARY KEY,
  "quality_event_no" VARCHAR(100) NOT NULL,
  "event_source_code" VARCHAR(80),
  "event_type_code" VARCHAR(80),
  "event_title" VARCHAR(200),
  "event_desc" TEXT,
  "event_time_text" VARCHAR(50),
  "severity_code" VARCHAR(50),
  "event_status_code" VARCHAR(50),
  "trigger_source_no" VARCHAR(120),
  "component_serial_no" VARCHAR(100),
  "part_serial_no" VARCHAR(100),
  "work_order_no" VARCHAR(100),
  "subject_type_code" VARCHAR(80),
  "subject_role_code" VARCHAR(80),
  "impact_desc" TEXT
);

CREATE TABLE IF NOT EXISTS "inspection_doc" (
  "inspection_doc_id" SERIAL PRIMARY KEY,
  "quality_event_doc_id" INTEGER,
  "inspection_no" VARCHAR(100) NOT NULL,
  "spec_code" VARCHAR(100),
  "inspection_type_code" VARCHAR(80),
  "find_date_text" VARCHAR(50),
  "find_type_code" VARCHAR(80),
  "result_code" VARCHAR(50),
  "component_serial_no" VARCHAR(100),
  "part_serial_no" VARCHAR(100),
  "work_order_no" VARCHAR(100),
  "op_no" VARCHAR(50),
  "inventory_batch_no" VARCHAR(100),
  "inspector_no" VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS "inspection_item" (
  "inspection_item_id" SERIAL PRIMARY KEY,
  "inspection_doc_id" INTEGER NOT NULL,
  "spec_code" VARCHAR(100),
  "item_code" VARCHAR(100) NOT NULL,
  "item_name" VARCHAR(200) NOT NULL,
  "measured_text" VARCHAR(200),
  "standard_text" VARCHAR(200),
  "lower_limit_text" VARCHAR(100),
  "upper_limit_text" VARCHAR(100),
  "uom" VARCHAR(30),
  "judge_code" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "defect_log" (
  "defect_log_id" SERIAL PRIMARY KEY,
  "inspection_doc_id" INTEGER,
  "quality_event_doc_id" INTEGER,
  "component_serial_no" VARCHAR(100),
  "part_serial_no" VARCHAR(100),
  "work_order_no" VARCHAR(100),
  "op_no" VARCHAR(50),
  "defect_code" VARCHAR(100) NOT NULL,
  "defect_name" VARCHAR(200),
  "defect_position_text" VARCHAR(200),
  "severity_code" VARCHAR(50),
  "failure_desc" TEXT,
  "find_dept_code" VARCHAR(100),
  "find_dept_name" VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS "iqs_failure" (
  "iqs_failure_id" SERIAL PRIMARY KEY,
  "quality_event_doc_id" INTEGER NOT NULL,
  "inspection_doc_id" INTEGER,
  "defect_log_id" INTEGER,
  "component_serial_no" VARCHAR(100),
  "part_serial_no" VARCHAR(100),
  "work_order_no" VARCHAR(100),
  "op_no" VARCHAR(50),
  "supplier_code" VARCHAR(100),
  "maintenance_event_no" VARCHAR(100),
  "doc_source" VARCHAR(80),
  "failure_type" VARCHAR(80),
  "quality_code" VARCHAR(200) NOT NULL,
  "iqs_failure_order" VARCHAR(120) NOT NULL,
  "task_code" VARCHAR(100),
  "product_code" VARCHAR(100),
  "product_name" VARCHAR(200),
  "piece_no" VARCHAR(100),
  "find_dept_code" VARCHAR(100),
  "find_dept_name" VARCHAR(200),
  "doc_status_code" VARCHAR(50),
  "secret_level_code" VARCHAR(20),
  "starttime_text" VARCHAR(50),
  "finishtime_text" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "iqs_failure_content" (
  "iqs_failure_content_id" SERIAL PRIMARY KEY,
  "iqs_failure_id" INTEGER NOT NULL,
  "start_piece_no_text" VARCHAR(50),
  "end_piece_no_text" VARCHAR(50),
  "failure_desc" TEXT NOT NULL,
  "failure_place_brief" TEXT,
  "find_type_code" VARCHAR(80),
  "find_date_text" VARCHAR(50),
  "finder_type_code" VARCHAR(80),
  "find_user_code" VARCHAR(100),
  "find_user_name" VARCHAR(100),
  "duty_worksec_code" VARCHAR(100),
  "duty_worksec_name" VARCHAR(200),
  "duty_dept_code" VARCHAR(100),
  "duty_dept_name" VARCHAR(200),
  "actual_method" TEXT,
  "doc_no" VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS "iqs_failure_duty" (
  "iqs_failure_duty_id" SERIAL PRIMARY KEY,
  "iqs_failure_id" INTEGER NOT NULL,
  "iqs_failure_content_id" INTEGER,
  "duty_user_code" VARCHAR(100),
  "duty_user_name" VARCHAR(100),
  "duty_dept_code" VARCHAR(100),
  "duty_dept_name" VARCHAR(200),
  "supplier_code" VARCHAR(100),
  "duty_person_ratio_text" VARCHAR(30),
  "responsibility_date_text" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "disposal_order" (
  "disposal_order_id" SERIAL PRIMARY KEY,
  "quality_event_doc_id" INTEGER NOT NULL,
  "iqs_failure_id" INTEGER,
  "iqs_failure_content_id" INTEGER,
  "work_order_no" VARCHAR(100),
  "responsible_user_code" VARCHAR(100),
  "disposal_no" VARCHAR(100) NOT NULL,
  "deal_type_find_code" VARCHAR(80),
  "deal_type_pass_code" VARCHAR(80),
  "deal_type_tech_code" VARCHAR(80),
  "disposal_type_code" VARCHAR(80),
  "disposal_desc" TEXT,
  "disposal_date_text" VARCHAR(50),
  "disposal_status_code" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "recheck_record" (
  "recheck_record_id" SERIAL PRIMARY KEY,
  "disposal_order_id" INTEGER NOT NULL,
  "inspection_doc_id" INTEGER,
  "inspector_no" VARCHAR(100),
  "recheck_no" VARCHAR(100) NOT NULL,
  "recheck_date_text" VARCHAR(50),
  "recheck_result_code" VARCHAR(50),
  "conclusion_desc" TEXT
);

-- Comments
COMMENT ON TABLE "quality_event_doc" IS 'QMS 质量事件源单表，作为检验异常、缺陷和不合格闭环的入口单据';
COMMENT ON COLUMN "quality_event_doc"."quality_event_doc_id" IS '质量事件源单主键';
COMMENT ON COLUMN "quality_event_doc"."quality_event_no" IS '质量事件编号';
COMMENT ON COLUMN "quality_event_doc"."event_source_code" IS '事件来源代码';
COMMENT ON COLUMN "quality_event_doc"."event_type_code" IS '事件类型代码';
COMMENT ON COLUMN "quality_event_doc"."event_title" IS '质量事件标题';
COMMENT ON COLUMN "quality_event_doc"."event_desc" IS '质量事件描述';
COMMENT ON COLUMN "quality_event_doc"."event_time_text" IS '事件发生或发现时间文本';
COMMENT ON COLUMN "quality_event_doc"."severity_code" IS '严重程度代码';
COMMENT ON COLUMN "quality_event_doc"."event_status_code" IS '事件状态代码';
COMMENT ON COLUMN "quality_event_doc"."trigger_source_no" IS '触发来源业务编号';
COMMENT ON COLUMN "quality_event_doc"."component_serial_no" IS '关联组件序列号';
COMMENT ON COLUMN "quality_event_doc"."part_serial_no" IS '关联零件序列号';
COMMENT ON COLUMN "quality_event_doc"."work_order_no" IS '关联工单编号';
COMMENT ON COLUMN "quality_event_doc"."subject_type_code" IS '事件关联对象类型代码';
COMMENT ON COLUMN "quality_event_doc"."subject_role_code" IS '事件对象角色代码';
COMMENT ON COLUMN "quality_event_doc"."impact_desc" IS '事件影响说明';

COMMENT ON TABLE "inspection_doc" IS 'QMS 检验单主表，记录来料、过程、最终和复检等检验活动';
COMMENT ON COLUMN "inspection_doc"."inspection_doc_id" IS '检验单源表主键';
COMMENT ON COLUMN "inspection_doc"."quality_event_doc_id" IS '关联质量事件源单主键';
COMMENT ON COLUMN "inspection_doc"."inspection_no" IS '检验单号';
COMMENT ON COLUMN "inspection_doc"."spec_code" IS '检验规范编码';
COMMENT ON COLUMN "inspection_doc"."inspection_type_code" IS '检验类型代码';
COMMENT ON COLUMN "inspection_doc"."find_date_text" IS '发现或检验时间文本';
COMMENT ON COLUMN "inspection_doc"."find_type_code" IS '发现方式代码';
COMMENT ON COLUMN "inspection_doc"."result_code" IS '检验结论代码';
COMMENT ON COLUMN "inspection_doc"."component_serial_no" IS '受检组件序列号';
COMMENT ON COLUMN "inspection_doc"."part_serial_no" IS '受检零件序列号';
COMMENT ON COLUMN "inspection_doc"."work_order_no" IS '关联工单编号';
COMMENT ON COLUMN "inspection_doc"."op_no" IS '关联工序编号';
COMMENT ON COLUMN "inspection_doc"."inventory_batch_no" IS '来料检验库存批次号';
COMMENT ON COLUMN "inspection_doc"."inspector_no" IS '检验员编号';

COMMENT ON TABLE "inspection_item" IS 'QMS 检验项目结果表，作为检验记录下的明细受益表';
COMMENT ON COLUMN "inspection_item"."inspection_item_id" IS '检验项目源表主键';
COMMENT ON COLUMN "inspection_item"."inspection_doc_id" IS '所属检验单主键';
COMMENT ON COLUMN "inspection_item"."spec_code" IS '检验规范编码';
COMMENT ON COLUMN "inspection_item"."item_code" IS '检验项目编码';
COMMENT ON COLUMN "inspection_item"."item_name" IS '检验项目名称';
COMMENT ON COLUMN "inspection_item"."measured_text" IS '实测值文本';
COMMENT ON COLUMN "inspection_item"."standard_text" IS '标准值文本';
COMMENT ON COLUMN "inspection_item"."lower_limit_text" IS '下限值文本';
COMMENT ON COLUMN "inspection_item"."upper_limit_text" IS '上限值文本';
COMMENT ON COLUMN "inspection_item"."uom" IS '计量单位代码';
COMMENT ON COLUMN "inspection_item"."judge_code" IS '项目判定结果代码';

COMMENT ON TABLE "defect_log" IS 'QMS 缺陷记录表，承接检验发现的问题并连接后续不合格通知';
COMMENT ON COLUMN "defect_log"."defect_log_id" IS '缺陷记录源表主键';
COMMENT ON COLUMN "defect_log"."inspection_doc_id" IS '来源检验单主键';
COMMENT ON COLUMN "defect_log"."quality_event_doc_id" IS '关联质量事件源单主键';
COMMENT ON COLUMN "defect_log"."component_serial_no" IS '关联组件序列号';
COMMENT ON COLUMN "defect_log"."part_serial_no" IS '关联零件序列号';
COMMENT ON COLUMN "defect_log"."work_order_no" IS '关联工单编号';
COMMENT ON COLUMN "defect_log"."op_no" IS '发现工序编号';
COMMENT ON COLUMN "defect_log"."defect_code" IS '缺陷代码';
COMMENT ON COLUMN "defect_log"."defect_name" IS '缺陷名称';
COMMENT ON COLUMN "defect_log"."defect_position_text" IS '缺陷部位描述';
COMMENT ON COLUMN "defect_log"."severity_code" IS '缺陷严重程度代码';
COMMENT ON COLUMN "defect_log"."failure_desc" IS '不合格或缺陷情况描述';
COMMENT ON COLUMN "defect_log"."find_dept_code" IS '发现单位编码';
COMMENT ON COLUMN "defect_log"."find_dept_name" IS '发现单位名称';

COMMENT ON TABLE "iqs_failure" IS 'IQS 不合格通知单源表，保留真实 IQS 单据字段习惯并作为不合格闭环 Anchor';
COMMENT ON COLUMN "iqs_failure"."iqs_failure_id" IS '不合格通知单源表主键';
COMMENT ON COLUMN "iqs_failure"."quality_event_doc_id" IS '来源质量事件源单主键';
COMMENT ON COLUMN "iqs_failure"."inspection_doc_id" IS '触发不合格的检验单主键';
COMMENT ON COLUMN "iqs_failure"."defect_log_id" IS '触发不合格的缺陷记录主键';
COMMENT ON COLUMN "iqs_failure"."component_serial_no" IS '涉及组件序列号';
COMMENT ON COLUMN "iqs_failure"."part_serial_no" IS '涉及零件序列号';
COMMENT ON COLUMN "iqs_failure"."work_order_no" IS '涉及工单编号';
COMMENT ON COLUMN "iqs_failure"."op_no" IS '发现工序编号';
COMMENT ON COLUMN "iqs_failure"."supplier_code" IS '责任或来源供应商编码';
COMMENT ON COLUMN "iqs_failure"."maintenance_event_no" IS '来源维修事件编号';
COMMENT ON COLUMN "iqs_failure"."doc_source" IS '不合格来源代码';
COMMENT ON COLUMN "iqs_failure"."failure_type" IS '不合格类型代码';
COMMENT ON COLUMN "iqs_failure"."quality_code" IS '质量编号';
COMMENT ON COLUMN "iqs_failure"."iqs_failure_order" IS '不合格通知单编号';
COMMENT ON COLUMN "iqs_failure"."task_code" IS '任务编号';
COMMENT ON COLUMN "iqs_failure"."product_code" IS '机型编码';
COMMENT ON COLUMN "iqs_failure"."product_name" IS '机型名称';
COMMENT ON COLUMN "iqs_failure"."piece_no" IS '件号';
COMMENT ON COLUMN "iqs_failure"."find_dept_code" IS '发现单位编码';
COMMENT ON COLUMN "iqs_failure"."find_dept_name" IS '发现单位名称';
COMMENT ON COLUMN "iqs_failure"."doc_status_code" IS '单据状态代码';
COMMENT ON COLUMN "iqs_failure"."secret_level_code" IS '密级代码';
COMMENT ON COLUMN "iqs_failure"."starttime_text" IS '流程开始时间文本';
COMMENT ON COLUMN "iqs_failure"."finishtime_text" IS '流程结束时间文本';

COMMENT ON TABLE "iqs_failure_content" IS 'IQS 不合格内容源表，记录问题描述、发现信息、责任单位和实际处理内容';
COMMENT ON COLUMN "iqs_failure_content"."iqs_failure_content_id" IS '不合格内容源表主键';
COMMENT ON COLUMN "iqs_failure_content"."iqs_failure_id" IS '所属不合格通知单主键';
COMMENT ON COLUMN "iqs_failure_content"."start_piece_no_text" IS '起始件号文本';
COMMENT ON COLUMN "iqs_failure_content"."end_piece_no_text" IS '终止件号文本';
COMMENT ON COLUMN "iqs_failure_content"."failure_desc" IS '不合格情况描述';
COMMENT ON COLUMN "iqs_failure_content"."failure_place_brief" IS '故障部位简要说明';
COMMENT ON COLUMN "iqs_failure_content"."find_type_code" IS '发现方式代码';
COMMENT ON COLUMN "iqs_failure_content"."find_date_text" IS '发现时间文本';
COMMENT ON COLUMN "iqs_failure_content"."finder_type_code" IS '发现人类别代码';
COMMENT ON COLUMN "iqs_failure_content"."find_user_code" IS '发现人编号';
COMMENT ON COLUMN "iqs_failure_content"."find_user_name" IS '发现人姓名';
COMMENT ON COLUMN "iqs_failure_content"."duty_worksec_code" IS '责任工段编码';
COMMENT ON COLUMN "iqs_failure_content"."duty_worksec_name" IS '责任工段名称';
COMMENT ON COLUMN "iqs_failure_content"."duty_dept_code" IS '责任单位编码';
COMMENT ON COLUMN "iqs_failure_content"."duty_dept_name" IS '责任单位名称';
COMMENT ON COLUMN "iqs_failure_content"."actual_method" IS '实际处理情况';
COMMENT ON COLUMN "iqs_failure_content"."doc_no" IS '衍生单据号';

COMMENT ON TABLE "iqs_failure_duty" IS 'IQS 不合格责任分配源表，记录责任人员、责任部门、供应商和责任比例';
COMMENT ON COLUMN "iqs_failure_duty"."iqs_failure_duty_id" IS '不合格责任源表主键';
COMMENT ON COLUMN "iqs_failure_duty"."iqs_failure_id" IS '所属不合格通知单主键';
COMMENT ON COLUMN "iqs_failure_duty"."iqs_failure_content_id" IS '关联不合格内容主键';
COMMENT ON COLUMN "iqs_failure_duty"."duty_user_code" IS '责任者编码';
COMMENT ON COLUMN "iqs_failure_duty"."duty_user_name" IS '责任者姓名';
COMMENT ON COLUMN "iqs_failure_duty"."duty_dept_code" IS '责任单位编码';
COMMENT ON COLUMN "iqs_failure_duty"."duty_dept_name" IS '责任单位名称';
COMMENT ON COLUMN "iqs_failure_duty"."supplier_code" IS '责任供应商编码';
COMMENT ON COLUMN "iqs_failure_duty"."duty_person_ratio_text" IS '责任人占比文本';
COMMENT ON COLUMN "iqs_failure_duty"."responsibility_date_text" IS '责任认定时间文本';

COMMENT ON TABLE "disposal_order" IS 'QMS 处置单源表，记录不合格或质量事件的处置措施和闭环状态';
COMMENT ON COLUMN "disposal_order"."disposal_order_id" IS '处置单源表主键';
COMMENT ON COLUMN "disposal_order"."quality_event_doc_id" IS '关联质量事件源单主键';
COMMENT ON COLUMN "disposal_order"."iqs_failure_id" IS '关联不合格通知单主键';
COMMENT ON COLUMN "disposal_order"."iqs_failure_content_id" IS '关联不合格内容主键';
COMMENT ON COLUMN "disposal_order"."work_order_no" IS '处置关联工单编号';
COMMENT ON COLUMN "disposal_order"."responsible_user_code" IS '处置责任人员编号';
COMMENT ON COLUMN "disposal_order"."disposal_no" IS '处置单号';
COMMENT ON COLUMN "disposal_order"."deal_type_find_code" IS '发现单位处置方式代码';
COMMENT ON COLUMN "disposal_order"."deal_type_pass_code" IS '检验组处置方式代码';
COMMENT ON COLUMN "disposal_order"."deal_type_tech_code" IS '技术人员处置方式代码';
COMMENT ON COLUMN "disposal_order"."disposal_type_code" IS '综合处置类型代码';
COMMENT ON COLUMN "disposal_order"."disposal_desc" IS '处置说明';
COMMENT ON COLUMN "disposal_order"."disposal_date_text" IS '处置时间文本';
COMMENT ON COLUMN "disposal_order"."disposal_status_code" IS '处置状态代码';

COMMENT ON TABLE "recheck_record" IS 'QMS 复检记录源表，记录处置后的再次检验和结论';
COMMENT ON COLUMN "recheck_record"."recheck_record_id" IS '复检记录源表主键';
COMMENT ON COLUMN "recheck_record"."disposal_order_id" IS '来源处置单主键';
COMMENT ON COLUMN "recheck_record"."inspection_doc_id" IS '对应复检检验单主键';
COMMENT ON COLUMN "recheck_record"."inspector_no" IS '复检人员编号';
COMMENT ON COLUMN "recheck_record"."recheck_no" IS '复检编号';
COMMENT ON COLUMN "recheck_record"."recheck_date_text" IS '复检时间文本';
COMMENT ON COLUMN "recheck_record"."recheck_result_code" IS '复检结论代码';
COMMENT ON COLUMN "recheck_record"."conclusion_desc" IS '复检结论说明';

-- Foreign keys
ALTER TABLE "inspection_doc" ADD CONSTRAINT "fk_inspection_doc_quality_event_doc_id" FOREIGN KEY ("quality_event_doc_id") REFERENCES "quality_event_doc" ("quality_event_doc_id");
ALTER TABLE "inspection_item" ADD CONSTRAINT "fk_inspection_item_inspection_doc_id" FOREIGN KEY ("inspection_doc_id") REFERENCES "inspection_doc" ("inspection_doc_id");
ALTER TABLE "defect_log" ADD CONSTRAINT "fk_defect_log_inspection_doc_id" FOREIGN KEY ("inspection_doc_id") REFERENCES "inspection_doc" ("inspection_doc_id");
ALTER TABLE "defect_log" ADD CONSTRAINT "fk_defect_log_quality_event_doc_id" FOREIGN KEY ("quality_event_doc_id") REFERENCES "quality_event_doc" ("quality_event_doc_id");
ALTER TABLE "iqs_failure" ADD CONSTRAINT "fk_iqs_failure_quality_event_doc_id" FOREIGN KEY ("quality_event_doc_id") REFERENCES "quality_event_doc" ("quality_event_doc_id");
ALTER TABLE "iqs_failure" ADD CONSTRAINT "fk_iqs_failure_inspection_doc_id" FOREIGN KEY ("inspection_doc_id") REFERENCES "inspection_doc" ("inspection_doc_id");
ALTER TABLE "iqs_failure" ADD CONSTRAINT "fk_iqs_failure_defect_log_id" FOREIGN KEY ("defect_log_id") REFERENCES "defect_log" ("defect_log_id");
ALTER TABLE "iqs_failure_content" ADD CONSTRAINT "fk_iqs_failure_content_iqs_failure_id" FOREIGN KEY ("iqs_failure_id") REFERENCES "iqs_failure" ("iqs_failure_id");
ALTER TABLE "iqs_failure_duty" ADD CONSTRAINT "fk_iqs_failure_duty_iqs_failure_id" FOREIGN KEY ("iqs_failure_id") REFERENCES "iqs_failure" ("iqs_failure_id");
ALTER TABLE "iqs_failure_duty" ADD CONSTRAINT "fk_iqs_failure_duty_iqs_failure_content_id" FOREIGN KEY ("iqs_failure_content_id") REFERENCES "iqs_failure_content" ("iqs_failure_content_id");
ALTER TABLE "disposal_order" ADD CONSTRAINT "fk_disposal_order_quality_event_doc_id" FOREIGN KEY ("quality_event_doc_id") REFERENCES "quality_event_doc" ("quality_event_doc_id");
ALTER TABLE "disposal_order" ADD CONSTRAINT "fk_disposal_order_iqs_failure_id" FOREIGN KEY ("iqs_failure_id") REFERENCES "iqs_failure" ("iqs_failure_id");
ALTER TABLE "disposal_order" ADD CONSTRAINT "fk_disposal_order_iqs_failure_content_id" FOREIGN KEY ("iqs_failure_content_id") REFERENCES "iqs_failure_content" ("iqs_failure_content_id");
ALTER TABLE "recheck_record" ADD CONSTRAINT "fk_recheck_record_disposal_order_id" FOREIGN KEY ("disposal_order_id") REFERENCES "disposal_order" ("disposal_order_id");
ALTER TABLE "recheck_record" ADD CONSTRAINT "fk_recheck_record_inspection_doc_id" FOREIGN KEY ("inspection_doc_id") REFERENCES "inspection_doc" ("inspection_doc_id");

-- Indexes
CREATE UNIQUE INDEX IF NOT EXISTS "uq_quality_event_doc_quality_event_no" ON "quality_event_doc" ("quality_event_no");
CREATE INDEX IF NOT EXISTS "idx_quality_event_doc_trigger_source_no" ON "quality_event_doc" ("trigger_source_no");
CREATE INDEX IF NOT EXISTS "idx_quality_event_doc_event_source_code_event_type_code" ON "quality_event_doc" ("event_source_code", "event_type_code");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_inspection_doc_inspection_no" ON "inspection_doc" ("inspection_no");
CREATE INDEX IF NOT EXISTS "idx_inspection_doc_quality_event_doc_id" ON "inspection_doc" ("quality_event_doc_id");
CREATE INDEX IF NOT EXISTS "idx_inspection_doc_component_serial_no" ON "inspection_doc" ("component_serial_no");
CREATE INDEX IF NOT EXISTS "idx_inspection_doc_work_order_no_op_no" ON "inspection_doc" ("work_order_no", "op_no");
CREATE INDEX IF NOT EXISTS "idx_inspection_item_inspection_doc_id" ON "inspection_item" ("inspection_doc_id");
CREATE INDEX IF NOT EXISTS "idx_inspection_item_item_code" ON "inspection_item" ("item_code");
CREATE INDEX IF NOT EXISTS "idx_inspection_item_judge_code" ON "inspection_item" ("judge_code");
CREATE INDEX IF NOT EXISTS "idx_defect_log_inspection_doc_id" ON "defect_log" ("inspection_doc_id");
CREATE INDEX IF NOT EXISTS "idx_defect_log_quality_event_doc_id" ON "defect_log" ("quality_event_doc_id");
CREATE INDEX IF NOT EXISTS "idx_defect_log_defect_code" ON "defect_log" ("defect_code");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_iqs_failure_iqs_failure_order" ON "iqs_failure" ("iqs_failure_order");
CREATE INDEX IF NOT EXISTS "idx_iqs_failure_quality_event_doc_id" ON "iqs_failure" ("quality_event_doc_id");
CREATE INDEX IF NOT EXISTS "idx_iqs_failure_inspection_doc_id" ON "iqs_failure" ("inspection_doc_id");
CREATE INDEX IF NOT EXISTS "idx_iqs_failure_defect_log_id" ON "iqs_failure" ("defect_log_id");
CREATE INDEX IF NOT EXISTS "idx_iqs_failure_quality_code" ON "iqs_failure" ("quality_code");
CREATE INDEX IF NOT EXISTS "idx_iqs_failure_content_iqs_failure_id" ON "iqs_failure_content" ("iqs_failure_id");
CREATE INDEX IF NOT EXISTS "idx_iqs_failure_content_find_user_code" ON "iqs_failure_content" ("find_user_code");
CREATE INDEX IF NOT EXISTS "idx_iqs_failure_content_duty_dept_code" ON "iqs_failure_content" ("duty_dept_code");
CREATE INDEX IF NOT EXISTS "idx_iqs_failure_duty_iqs_failure_id" ON "iqs_failure_duty" ("iqs_failure_id");
CREATE INDEX IF NOT EXISTS "idx_iqs_failure_duty_iqs_failure_content_id" ON "iqs_failure_duty" ("iqs_failure_content_id");
CREATE INDEX IF NOT EXISTS "idx_iqs_failure_duty_duty_user_code" ON "iqs_failure_duty" ("duty_user_code");
CREATE INDEX IF NOT EXISTS "idx_iqs_failure_duty_supplier_code" ON "iqs_failure_duty" ("supplier_code");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_disposal_order_disposal_no" ON "disposal_order" ("disposal_no");
CREATE INDEX IF NOT EXISTS "idx_disposal_order_quality_event_doc_id" ON "disposal_order" ("quality_event_doc_id");
CREATE INDEX IF NOT EXISTS "idx_disposal_order_iqs_failure_id" ON "disposal_order" ("iqs_failure_id");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_recheck_record_recheck_no" ON "recheck_record" ("recheck_no");
CREATE INDEX IF NOT EXISTS "idx_recheck_record_disposal_order_id" ON "recheck_record" ("disposal_order_id");
CREATE INDEX IF NOT EXISTS "idx_recheck_record_inspection_doc_id" ON "recheck_record" ("inspection_doc_id");
