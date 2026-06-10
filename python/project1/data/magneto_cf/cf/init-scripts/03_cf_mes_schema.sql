-- Auto-generated from DBML for CF aviation simulated dataset.
-- Source DBML: MES源系统数据库表设计.dbml
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

CREATE TABLE IF NOT EXISTS "sys_task" (
  "task_id" SERIAL PRIMARY KEY,
  "workid" VARCHAR(100) NOT NULL,
  "taskcode" VARCHAR(100),
  "worktype" VARCHAR(50),
  "workname" VARCHAR(200),
  "partno" VARCHAR(100),
  "route_no" VARCHAR(100),
  "zzh" VARCHAR(100),
  "planqty" VARCHAR(30),
  "yxj" VARCHAR(10),
  "urgent_tab" VARCHAR(5),
  "plandate" VARCHAR(30),
  "delivery_date_char" VARCHAR(30),
  "reportman" VARCHAR(100),
  "reportdate" VARCHAR(30),
  "rwstate" VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS "batch_card" (
  "batch_id" SERIAL PRIMARY KEY,
  "task_id" INTEGER NOT NULL,
  "batchno" VARCHAR(100) NOT NULL,
  "partno" VARCHAR(100),
  "batch_qty" VARCHAR(30),
  "starttime_txt" VARCHAR(30),
  "fintime_txt" VARCHAR(30),
  "zhuangtai" VARCHAR(20),
  "quality_code" VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS "part_track" (
  "track_id" SERIAL PRIMARY KEY,
  "batch_id" INTEGER NOT NULL,
  "partno" VARCHAR(100) NOT NULL,
  "part_sn" VARCHAR(100),
  "lotno" VARCHAR(100),
  "jianhao" VARCHAR(100),
  "qcode" VARCHAR(200),
  "source_code" VARCHAR(20),
  "part_state" VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS "assembly_record" (
  "assembly_id" SERIAL PRIMARY KEY,
  "task_id" INTEGER NOT NULL,
  "batch_id" INTEGER,
  "part_id" INTEGER NOT NULL,
  "component_sn" VARCHAR(100) NOT NULL,
  "bom_item_code" VARCHAR(100),
  "position_code" VARCHAR(100),
  "install_dt_txt" VARCHAR(30),
  "valid_begin_txt" VARCHAR(30),
  "valid_end_txt" VARCHAR(30),
  "uninstall_dt_txt" VARCHAR(30),
  "remove_reason_code" VARCHAR(50),
  "personname" VARCHAR(100),
  "install_state" VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS "sys_station" (
  "station_id" SERIAL PRIMARY KEY,
  "stationid" VARCHAR(50) NOT NULL,
  "stationname" VARCHAR(200) NOT NULL,
  "workshop" VARCHAR(100),
  "percount" VARCHAR(20),
  "equcount" VARCHAR(20),
  "zhuangtai" VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS "equipment_asset" (
  "equipment_asset_id" SERIAL PRIMARY KEY,
  "station_id" INTEGER,
  "equipmentid" VARCHAR(100) NOT NULL,
  "equipmentname" VARCHAR(200) NOT NULL,
  "equipmentnum" VARCHAR(20),
  "eqtype_code" VARCHAR(50),
  "zhuangtai" VARCHAR(20),
  "bgcolor" VARCHAR(50),
  "indate" VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS "person_info" (
  "person_id" SERIAL PRIMARY KEY,
  "station_id" INTEGER,
  "personnum" VARCHAR(100) NOT NULL,
  "personname" VARCHAR(100) NOT NULL,
  "workshop" VARCHAR(100),
  "phone" VARCHAR(50),
  "jndj" VARCHAR(50),
  "gz" VARCHAR(50),
  "deptid" VARCHAR(100),
  "deptname" VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS "tooling_asset" (
  "tooling_asset_id" SERIAL PRIMARY KEY,
  "station_id" INTEGER,
  "toolingid" VARCHAR(100) NOT NULL,
  "toolingname" VARCHAR(200) NOT NULL,
  "gzzl" VARCHAR(50),
  "jzrq_txt" VARCHAR(30),
  "zhuangtai" VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS "work_log" (
  "worklog_id" SERIAL PRIMARY KEY,
  "task_id" INTEGER NOT NULL,
  "batch_id" INTEGER,
  "part_id" INTEGER,
  "assembly_id" INTEGER,
  "station_id" INTEGER,
  "equipment_asset_id" INTEGER,
  "person_id" INTEGER,
  "zzh" VARCHAR(100),
  "op_no" VARCHAR(50) NOT NULL,
  "workid" VARCHAR(50),
  "workname" VARCHAR(200),
  "startdate_txt" VARCHAR(30),
  "enddate_txt" VARCHAR(30),
  "status_code" VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS "step_log" (
  "steplog_id" SERIAL PRIMARY KEY,
  "worklog_id" INTEGER NOT NULL,
  "person_id" INTEGER,
  "tooling_asset_id" INTEGER,
  "stepsid" VARCHAR(50) NOT NULL,
  "stepsname" VARCHAR(200) NOT NULL,
  "stepnum" VARCHAR(20),
  "start_time_text" VARCHAR(30),
  "finish_time_text" VARCHAR(30),
  "zhuangtai" VARCHAR(20),
  "opername" VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS "resource_usage" (
  "usage_id" SERIAL PRIMARY KEY,
  "worklog_id" INTEGER NOT NULL,
  "steplog_id" INTEGER,
  "equipment_asset_id" INTEGER,
  "person_id" INTEGER,
  "station_id" INTEGER,
  "tooling_asset_id" INTEGER,
  "use_start_txt" VARCHAR(30),
  "use_end_txt" VARCHAR(30),
  "mastery_txt" VARCHAR(20),
  "use_state" VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS "process_param" (
  "param_id" SERIAL PRIMARY KEY,
  "worklog_id" INTEGER NOT NULL,
  "steplog_id" INTEGER,
  "equipment_asset_id" INTEGER,
  "param_std_code" VARCHAR(100),
  "paramid" VARCHAR(100) NOT NULL,
  "paramname" VARCHAR(200) NOT NULL,
  "lqyl" VARCHAR(50),
  "bzval" VARCHAR(50),
  "llower" VARCHAR(50),
  "uupper" VARCHAR(50),
  "unitname" VARCHAR(50),
  "collect_dt" VARCHAR(30),
  "pdjg" VARCHAR(20)
);

-- Comments
COMMENT ON TABLE "sys_task" IS 'MES 任务与工单主表，作为制造执行场景 Anchor 表';
COMMENT ON COLUMN "sys_task"."task_id" IS '任务主键，MES 源侧单列自增主键';
COMMENT ON COLUMN "sys_task"."workid" IS '工单号，现场系统常用任务编号';
COMMENT ON COLUMN "sys_task"."taskcode" IS '任务编码，用于跨系统追溯';
COMMENT ON COLUMN "sys_task"."worktype" IS '工单类型源编码，例如生产或装配';
COMMENT ON COLUMN "sys_task"."workname" IS '工单名称或任务名称';
COMMENT ON COLUMN "sys_task"."partno" IS '零件号或组件型号号，用于解析目标零件定义';
COMMENT ON COLUMN "sys_task"."route_no" IS '执行工艺路线编号，用于解析目标工艺路线';
COMMENT ON COLUMN "sys_task"."zzh" IS '组件序列号，现场历史字段写法';
COMMENT ON COLUMN "sys_task"."planqty" IS '计划数量，源侧按文本保存';
COMMENT ON COLUMN "sys_task"."yxj" IS '优先级代码，现场缩写字段';
COMMENT ON COLUMN "sys_task"."urgent_tab" IS '是否紧急标识，源侧使用 Y 或 N';
COMMENT ON COLUMN "sys_task"."plandate" IS '计划开始时间，源侧文本日期';
COMMENT ON COLUMN "sys_task"."delivery_date_char" IS '计划完成时间，源侧字符日期';
COMMENT ON COLUMN "sys_task"."reportman" IS '提交人姓名';
COMMENT ON COLUMN "sys_task"."reportdate" IS '提交时间，源侧文本时间';
COMMENT ON COLUMN "sys_task"."rwstate" IS '任务状态源编码';

COMMENT ON TABLE "batch_card" IS 'MES 生产批次卡表，连接工单与零件追踪';
COMMENT ON COLUMN "batch_card"."batch_id" IS '批次卡主键，MES 源侧单列自增主键';
COMMENT ON COLUMN "batch_card"."task_id" IS '所属任务主键';
COMMENT ON COLUMN "batch_card"."batchno" IS '生产批次号';
COMMENT ON COLUMN "batch_card"."partno" IS '批次对应零件号';
COMMENT ON COLUMN "batch_card"."batch_qty" IS '批次数量，源侧按文本保存';
COMMENT ON COLUMN "batch_card"."starttime_txt" IS '批次开始时间文本';
COMMENT ON COLUMN "batch_card"."fintime_txt" IS '批次完成时间文本';
COMMENT ON COLUMN "batch_card"."zhuangtai" IS '批次状态源编码';
COMMENT ON COLUMN "batch_card"."quality_code" IS '批次质量编号';

COMMENT ON TABLE "part_track" IS 'MES 零件个体追踪表，作为零件实例 Anchor 表';
COMMENT ON COLUMN "part_track"."track_id" IS '零件追踪主键，MES 源侧单列自增主键';
COMMENT ON COLUMN "part_track"."batch_id" IS '来源批次主键';
COMMENT ON COLUMN "part_track"."partno" IS '零件号';
COMMENT ON COLUMN "part_track"."part_sn" IS '零件序列号';
COMMENT ON COLUMN "part_track"."lotno" IS '零件批次号';
COMMENT ON COLUMN "part_track"."jianhao" IS '件号，现场拼音字段';
COMMENT ON COLUMN "part_track"."qcode" IS '零件质量编号';
COMMENT ON COLUMN "part_track"."source_code" IS '零件来源类型源编码';
COMMENT ON COLUMN "part_track"."part_state" IS '零件状态源编码';

COMMENT ON TABLE "assembly_record" IS 'MES 组件与零件装配记录表，连接零件追踪和组件装配关系';
COMMENT ON COLUMN "assembly_record"."assembly_id" IS '装配记录主键，MES 源侧单列自增主键';
COMMENT ON COLUMN "assembly_record"."task_id" IS '装配任务主键';
COMMENT ON COLUMN "assembly_record"."batch_id" IS '装配批次主键';
COMMENT ON COLUMN "assembly_record"."part_id" IS '被装配零件追踪主键';
COMMENT ON COLUMN "assembly_record"."component_sn" IS '组件序列号';
COMMENT ON COLUMN "assembly_record"."bom_item_code" IS 'BOM 项编码';
COMMENT ON COLUMN "assembly_record"."position_code" IS '装配位置编码';
COMMENT ON COLUMN "assembly_record"."install_dt_txt" IS '装配时间文本';
COMMENT ON COLUMN "assembly_record"."valid_begin_txt" IS '装配关系有效开始时间文本';
COMMENT ON COLUMN "assembly_record"."valid_end_txt" IS '装配关系有效结束时间文本';
COMMENT ON COLUMN "assembly_record"."uninstall_dt_txt" IS '拆下时间文本';
COMMENT ON COLUMN "assembly_record"."remove_reason_code" IS '拆下原因源编码';
COMMENT ON COLUMN "assembly_record"."personname" IS '装配人员姓名';
COMMENT ON COLUMN "assembly_record"."install_state" IS '装配状态源编码';

COMMENT ON TABLE "sys_station" IS 'MES 工位基础表，提供执行地点与资源归属';
COMMENT ON COLUMN "sys_station"."station_id" IS '工位主键，MES 源侧单列自增主键';
COMMENT ON COLUMN "sys_station"."stationid" IS '工位编号';
COMMENT ON COLUMN "sys_station"."stationname" IS '工位名称';
COMMENT ON COLUMN "sys_station"."workshop" IS '所属车间';
COMMENT ON COLUMN "sys_station"."percount" IS '工位人员数量文本';
COMMENT ON COLUMN "sys_station"."equcount" IS '工位设备数量文本';
COMMENT ON COLUMN "sys_station"."zhuangtai" IS '工位状态源编码';

COMMENT ON TABLE "equipment_asset" IS 'MES 设备资产表，作为制造资源 Anchor 表';
COMMENT ON COLUMN "equipment_asset"."equipment_asset_id" IS '设备资产主键，MES 源侧单列自增主键';
COMMENT ON COLUMN "equipment_asset"."station_id" IS '所属工位主键';
COMMENT ON COLUMN "equipment_asset"."equipmentid" IS '设备编号';
COMMENT ON COLUMN "equipment_asset"."equipmentname" IS '设备名称';
COMMENT ON COLUMN "equipment_asset"."equipmentnum" IS '设备序号文本';
COMMENT ON COLUMN "equipment_asset"."eqtype_code" IS '设备类型源编码';
COMMENT ON COLUMN "equipment_asset"."zhuangtai" IS '设备状态源编码';
COMMENT ON COLUMN "equipment_asset"."bgcolor" IS '设备颜色或看板标识';
COMMENT ON COLUMN "equipment_asset"."indate" IS '设备建档时间文本';

COMMENT ON TABLE "person_info" IS 'MES 人员基础信息表，覆盖操作员与装配人员';
COMMENT ON COLUMN "person_info"."person_id" IS '人员主键，MES 源侧单列自增主键';
COMMENT ON COLUMN "person_info"."station_id" IS '常驻工位主键';
COMMENT ON COLUMN "person_info"."personnum" IS '员工编号';
COMMENT ON COLUMN "person_info"."personname" IS '员工姓名';
COMMENT ON COLUMN "person_info"."workshop" IS '所属车间';
COMMENT ON COLUMN "person_info"."phone" IS '联系电话';
COMMENT ON COLUMN "person_info"."jndj" IS '技能等级源编码';
COMMENT ON COLUMN "person_info"."gz" IS '工种源编码';
COMMENT ON COLUMN "person_info"."deptid" IS '所属部门编码';
COMMENT ON COLUMN "person_info"."deptname" IS '所属部门名称';

COMMENT ON TABLE "tooling_asset" IS 'MES 工装资产表，记录夹具和专用工具';
COMMENT ON COLUMN "tooling_asset"."tooling_asset_id" IS '工装主键，MES 源侧单列自增主键';
COMMENT ON COLUMN "tooling_asset"."station_id" IS '常用工位主键';
COMMENT ON COLUMN "tooling_asset"."toolingid" IS '工装编号';
COMMENT ON COLUMN "tooling_asset"."toolingname" IS '工装名称';
COMMENT ON COLUMN "tooling_asset"."gzzl" IS '工装种类源编码';
COMMENT ON COLUMN "tooling_asset"."jzrq_txt" IS '校准到期日期文本';
COMMENT ON COLUMN "tooling_asset"."zhuangtai" IS '工装状态源编码';

COMMENT ON TABLE "work_log" IS 'MES 工序执行日志表，作为制造执行 Beneficiary 表';
COMMENT ON COLUMN "work_log"."worklog_id" IS '工序日志主键，MES 源侧单列自增主键';
COMMENT ON COLUMN "work_log"."task_id" IS '所属任务主键';
COMMENT ON COLUMN "work_log"."batch_id" IS '所属批次主键';
COMMENT ON COLUMN "work_log"."part_id" IS '关联零件追踪主键';
COMMENT ON COLUMN "work_log"."assembly_id" IS '关联装配记录主键';
COMMENT ON COLUMN "work_log"."station_id" IS '执行工位主键';
COMMENT ON COLUMN "work_log"."equipment_asset_id" IS '执行设备主键';
COMMENT ON COLUMN "work_log"."person_id" IS '主操作人员主键';
COMMENT ON COLUMN "work_log"."zzh" IS '组件序列号冗余字段';
COMMENT ON COLUMN "work_log"."op_no" IS '工序编号';
COMMENT ON COLUMN "work_log"."workid" IS '工序编码，现场字段名';
COMMENT ON COLUMN "work_log"."workname" IS '工序名称';
COMMENT ON COLUMN "work_log"."startdate_txt" IS '实际开始时间文本';
COMMENT ON COLUMN "work_log"."enddate_txt" IS '实际完成时间文本';
COMMENT ON COLUMN "work_log"."status_code" IS '工序执行状态源编码';

COMMENT ON TABLE "step_log" IS 'MES 工步执行日志表，作为工序执行的下游 Beneficiary 表';
COMMENT ON COLUMN "step_log"."steplog_id" IS '工步日志主键，MES 源侧单列自增主键';
COMMENT ON COLUMN "step_log"."worklog_id" IS '所属工序日志主键';
COMMENT ON COLUMN "step_log"."person_id" IS '操作人员主键';
COMMENT ON COLUMN "step_log"."tooling_asset_id" IS '使用工装主键';
COMMENT ON COLUMN "step_log"."stepsid" IS '工步编号';
COMMENT ON COLUMN "step_log"."stepsname" IS '工步名称';
COMMENT ON COLUMN "step_log"."stepnum" IS '工步顺序文本';
COMMENT ON COLUMN "step_log"."start_time_text" IS '工步开始时间文本';
COMMENT ON COLUMN "step_log"."finish_time_text" IS '工步完成时间文本';
COMMENT ON COLUMN "step_log"."zhuangtai" IS '工步状态源编码';
COMMENT ON COLUMN "step_log"."opername" IS '操作人员姓名冗余字段';

COMMENT ON TABLE "resource_usage" IS 'MES 资源使用记录表，覆盖工序或工步使用的设备人员工位工装';
COMMENT ON COLUMN "resource_usage"."usage_id" IS '资源使用主键，MES 源侧单列自增主键';
COMMENT ON COLUMN "resource_usage"."worklog_id" IS '所属工序日志主键';
COMMENT ON COLUMN "resource_usage"."steplog_id" IS '所属工步日志主键';
COMMENT ON COLUMN "resource_usage"."equipment_asset_id" IS '使用设备主键';
COMMENT ON COLUMN "resource_usage"."person_id" IS '使用人员主键';
COMMENT ON COLUMN "resource_usage"."station_id" IS '使用工位主键';
COMMENT ON COLUMN "resource_usage"."tooling_asset_id" IS '使用工装主键';
COMMENT ON COLUMN "resource_usage"."use_start_txt" IS '资源使用开始时间文本';
COMMENT ON COLUMN "resource_usage"."use_end_txt" IS '资源使用结束时间文本';
COMMENT ON COLUMN "resource_usage"."mastery_txt" IS '人员熟练度文本，例如百分比';
COMMENT ON COLUMN "resource_usage"."use_state" IS '资源使用状态源编码';

COMMENT ON TABLE "process_param" IS 'MES 过程参数采集表，作为制造执行场景重点 Beneficiary 表';
COMMENT ON COLUMN "process_param"."param_id" IS '过程参数主键，MES 源侧单列自增主键';
COMMENT ON COLUMN "process_param"."worklog_id" IS '所属工序日志主键';
COMMENT ON COLUMN "process_param"."steplog_id" IS '所属工步日志主键';
COMMENT ON COLUMN "process_param"."equipment_asset_id" IS '采集设备主键';
COMMENT ON COLUMN "process_param"."param_std_code" IS '参数标准编码';
COMMENT ON COLUMN "process_param"."paramid" IS '参数编码';
COMMENT ON COLUMN "process_param"."paramname" IS '参数名称';
COMMENT ON COLUMN "process_param"."lqyl" IS '现场采集值，可能带单位或旧字段缩写';
COMMENT ON COLUMN "process_param"."bzval" IS '标准值文本';
COMMENT ON COLUMN "process_param"."llower" IS '下限值文本';
COMMENT ON COLUMN "process_param"."uupper" IS '上限值文本';
COMMENT ON COLUMN "process_param"."unitname" IS '参数单位名称';
COMMENT ON COLUMN "process_param"."collect_dt" IS '采集时间文本';
COMMENT ON COLUMN "process_param"."pdjg" IS '判定结果源编码';

-- Foreign keys
ALTER TABLE "batch_card" ADD CONSTRAINT "fk_batch_card_task_id" FOREIGN KEY ("task_id") REFERENCES "sys_task" ("task_id");
ALTER TABLE "part_track" ADD CONSTRAINT "fk_part_track_batch_id" FOREIGN KEY ("batch_id") REFERENCES "batch_card" ("batch_id");
ALTER TABLE "assembly_record" ADD CONSTRAINT "fk_assembly_record_task_id" FOREIGN KEY ("task_id") REFERENCES "sys_task" ("task_id");
ALTER TABLE "assembly_record" ADD CONSTRAINT "fk_assembly_record_batch_id" FOREIGN KEY ("batch_id") REFERENCES "batch_card" ("batch_id");
ALTER TABLE "assembly_record" ADD CONSTRAINT "fk_assembly_record_part_id" FOREIGN KEY ("part_id") REFERENCES "part_track" ("track_id");
ALTER TABLE "equipment_asset" ADD CONSTRAINT "fk_equipment_asset_station_id" FOREIGN KEY ("station_id") REFERENCES "sys_station" ("station_id");
ALTER TABLE "person_info" ADD CONSTRAINT "fk_person_info_station_id" FOREIGN KEY ("station_id") REFERENCES "sys_station" ("station_id");
ALTER TABLE "tooling_asset" ADD CONSTRAINT "fk_tooling_asset_station_id" FOREIGN KEY ("station_id") REFERENCES "sys_station" ("station_id");
ALTER TABLE "work_log" ADD CONSTRAINT "fk_work_log_task_id" FOREIGN KEY ("task_id") REFERENCES "sys_task" ("task_id");
ALTER TABLE "work_log" ADD CONSTRAINT "fk_work_log_batch_id" FOREIGN KEY ("batch_id") REFERENCES "batch_card" ("batch_id");
ALTER TABLE "work_log" ADD CONSTRAINT "fk_work_log_part_id" FOREIGN KEY ("part_id") REFERENCES "part_track" ("track_id");
ALTER TABLE "work_log" ADD CONSTRAINT "fk_work_log_assembly_id" FOREIGN KEY ("assembly_id") REFERENCES "assembly_record" ("assembly_id");
ALTER TABLE "work_log" ADD CONSTRAINT "fk_work_log_station_id" FOREIGN KEY ("station_id") REFERENCES "sys_station" ("station_id");
ALTER TABLE "work_log" ADD CONSTRAINT "fk_work_log_equipment_asset_id" FOREIGN KEY ("equipment_asset_id") REFERENCES "equipment_asset" ("equipment_asset_id");
ALTER TABLE "work_log" ADD CONSTRAINT "fk_work_log_person_id" FOREIGN KEY ("person_id") REFERENCES "person_info" ("person_id");
ALTER TABLE "step_log" ADD CONSTRAINT "fk_step_log_worklog_id" FOREIGN KEY ("worklog_id") REFERENCES "work_log" ("worklog_id");
ALTER TABLE "step_log" ADD CONSTRAINT "fk_step_log_person_id" FOREIGN KEY ("person_id") REFERENCES "person_info" ("person_id");
ALTER TABLE "step_log" ADD CONSTRAINT "fk_step_log_tooling_asset_id" FOREIGN KEY ("tooling_asset_id") REFERENCES "tooling_asset" ("tooling_asset_id");
ALTER TABLE "resource_usage" ADD CONSTRAINT "fk_resource_usage_worklog_id" FOREIGN KEY ("worklog_id") REFERENCES "work_log" ("worklog_id");
ALTER TABLE "resource_usage" ADD CONSTRAINT "fk_resource_usage_steplog_id" FOREIGN KEY ("steplog_id") REFERENCES "step_log" ("steplog_id");
ALTER TABLE "resource_usage" ADD CONSTRAINT "fk_resource_usage_equipment_asset_id" FOREIGN KEY ("equipment_asset_id") REFERENCES "equipment_asset" ("equipment_asset_id");
ALTER TABLE "resource_usage" ADD CONSTRAINT "fk_resource_usage_person_id" FOREIGN KEY ("person_id") REFERENCES "person_info" ("person_id");
ALTER TABLE "resource_usage" ADD CONSTRAINT "fk_resource_usage_station_id" FOREIGN KEY ("station_id") REFERENCES "sys_station" ("station_id");
ALTER TABLE "resource_usage" ADD CONSTRAINT "fk_resource_usage_tooling_asset_id" FOREIGN KEY ("tooling_asset_id") REFERENCES "tooling_asset" ("tooling_asset_id");
ALTER TABLE "process_param" ADD CONSTRAINT "fk_process_param_worklog_id" FOREIGN KEY ("worklog_id") REFERENCES "work_log" ("worklog_id");
ALTER TABLE "process_param" ADD CONSTRAINT "fk_process_param_steplog_id" FOREIGN KEY ("steplog_id") REFERENCES "step_log" ("steplog_id");
ALTER TABLE "process_param" ADD CONSTRAINT "fk_process_param_equipment_asset_id" FOREIGN KEY ("equipment_asset_id") REFERENCES "equipment_asset" ("equipment_asset_id");

-- Indexes
CREATE UNIQUE INDEX IF NOT EXISTS "uq_sys_task_workid" ON "sys_task" ("workid");
CREATE INDEX IF NOT EXISTS "idx_sys_task_taskcode" ON "sys_task" ("taskcode");
CREATE INDEX IF NOT EXISTS "idx_sys_task_zzh" ON "sys_task" ("zzh");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_batch_card_batchno" ON "batch_card" ("batchno");
CREATE INDEX IF NOT EXISTS "idx_batch_card_task_id" ON "batch_card" ("task_id");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_part_track_part_sn" ON "part_track" ("part_sn");
CREATE INDEX IF NOT EXISTS "idx_part_track_batch_id" ON "part_track" ("batch_id");
CREATE INDEX IF NOT EXISTS "idx_part_track_partno" ON "part_track" ("partno");
CREATE INDEX IF NOT EXISTS "idx_assembly_record_task_id" ON "assembly_record" ("task_id");
CREATE INDEX IF NOT EXISTS "idx_assembly_record_part_id" ON "assembly_record" ("part_id");
CREATE INDEX IF NOT EXISTS "idx_assembly_record_component_sn" ON "assembly_record" ("component_sn");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_sys_station_stationid" ON "sys_station" ("stationid");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_equipment_asset_equipmentid" ON "equipment_asset" ("equipmentid");
CREATE INDEX IF NOT EXISTS "idx_equipment_asset_station_id" ON "equipment_asset" ("station_id");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_person_info_personnum" ON "person_info" ("personnum");
CREATE INDEX IF NOT EXISTS "idx_person_info_station_id" ON "person_info" ("station_id");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_tooling_asset_toolingid" ON "tooling_asset" ("toolingid");
CREATE INDEX IF NOT EXISTS "idx_tooling_asset_station_id" ON "tooling_asset" ("station_id");
CREATE INDEX IF NOT EXISTS "idx_work_log_task_id" ON "work_log" ("task_id");
CREATE INDEX IF NOT EXISTS "idx_work_log_station_id" ON "work_log" ("station_id");
CREATE INDEX IF NOT EXISTS "idx_work_log_equipment_asset_id" ON "work_log" ("equipment_asset_id");
CREATE INDEX IF NOT EXISTS "idx_work_log_op_no" ON "work_log" ("op_no");
CREATE INDEX IF NOT EXISTS "idx_step_log_worklog_id" ON "step_log" ("worklog_id");
CREATE INDEX IF NOT EXISTS "idx_step_log_stepsid" ON "step_log" ("stepsid");
CREATE INDEX IF NOT EXISTS "idx_resource_usage_worklog_id" ON "resource_usage" ("worklog_id");
CREATE INDEX IF NOT EXISTS "idx_resource_usage_steplog_id" ON "resource_usage" ("steplog_id");
CREATE INDEX IF NOT EXISTS "idx_resource_usage_equipment_asset_id" ON "resource_usage" ("equipment_asset_id");
CREATE INDEX IF NOT EXISTS "idx_resource_usage_person_id" ON "resource_usage" ("person_id");
CREATE INDEX IF NOT EXISTS "idx_process_param_worklog_id" ON "process_param" ("worklog_id");
CREATE INDEX IF NOT EXISTS "idx_process_param_steplog_id" ON "process_param" ("steplog_id");
CREATE INDEX IF NOT EXISTS "idx_process_param_paramid" ON "process_param" ("paramid");
