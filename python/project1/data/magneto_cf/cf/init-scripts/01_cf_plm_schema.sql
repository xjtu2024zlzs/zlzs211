-- Auto-generated from DBML for CF aviation simulated dataset.
-- Source DBML: PLM源系统数据库表设计.dbml
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

CREATE TABLE IF NOT EXISTS "product_model" (
  "product_model_id" SERIAL PRIMARY KEY,
  "model_code" VARCHAR(80) NOT NULL,
  "model_name" VARCHAR(200) NOT NULL,
  "model_series" VARCHAR(80),
  "product_line" VARCHAR(100),
  "actual_plane_no" VARCHAR(100),
  "model_status_cd" VARCHAR(30),
  "cert_level_cd" VARCHAR(30),
  "eff_date_txt" VARCHAR(20),
  "expire_date_txt" VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS "gear_system_def" (
  "gear_system_def_id" SERIAL PRIMARY KEY,
  "product_model_id" INTEGER NOT NULL,
  "sys_code" VARCHAR(80) NOT NULL,
  "sys_name" VARCHAR(200) NOT NULL,
  "install_zone" VARCHAR(80),
  "station_no" VARCHAR(100),
  "side_mark" VARCHAR(20),
  "tech_status_cd" VARCHAR(30),
  "eff_date_txt" VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS "actuator_model" (
  "actuator_model_id" SERIAL PRIMARY KEY,
  "gear_system_def_id" INTEGER NOT NULL,
  "actuator_code" VARCHAR(100) NOT NULL,
  "actuator_name" VARCHAR(200) NOT NULL,
  "actuator_spec" VARCHAR(200),
  "dwg_no" VARCHAR(120),
  "config_no" VARCHAR(80),
  "model_status_cd" VARCHAR(30),
  "secret_lvl_cd" VARCHAR(20),
  "tech_status_text" VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS "design_revision" (
  "design_revision_id" SERIAL PRIMARY KEY,
  "actuator_model_id" INTEGER NOT NULL,
  "rev_no" VARCHAR(60) NOT NULL,
  "rev_name" VARCHAR(200),
  "dwg_no" VARCHAR(120),
  "doc_rev" VARCHAR(50),
  "issue_status_cd" VARCHAR(30),
  "eff_date_txt" VARCHAR(20),
  "expire_date_txt" VARCHAR(20),
  "released_by" VARCHAR(80)
);

CREATE TABLE IF NOT EXISTS "part_master" (
  "part_master_id" SERIAL PRIMARY KEY,
  "design_revision_id" INTEGER NOT NULL,
  "part_no" VARCHAR(100) NOT NULL,
  "part_cn_name" VARCHAR(200) NOT NULL,
  "part_cat_cd" VARCHAR(30),
  "dwg_no" VARCHAR(120),
  "material_no" VARCHAR(100),
  "mat_grade" VARCHAR(100),
  "spec_text" VARCHAR(200),
  "uom" VARCHAR(30),
  "make_buy_cd" VARCHAR(20),
  "key_level_cd" VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS "bom_header" (
  "bom_header_id" SERIAL PRIMARY KEY,
  "actuator_model_id" INTEGER NOT NULL,
  "design_revision_id" INTEGER NOT NULL,
  "root_part_master_id" INTEGER,
  "bom_no" VARCHAR(100) NOT NULL,
  "bom_rev" VARCHAR(50),
  "bom_status_cd" VARCHAR(30),
  "eff_date_txt" VARCHAR(20),
  "end_date_txt" VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS "bom_line" (
  "bom_line_id" SERIAL PRIMARY KEY,
  "bom_header_id" INTEGER NOT NULL,
  "parent_part_master_id" INTEGER,
  "child_part_master_id" INTEGER NOT NULL,
  "line_no" INTEGER NOT NULL,
  "item_no" VARCHAR(60) NOT NULL,
  "qty_per_txt" VARCHAR(40) NOT NULL,
  "uom" VARCHAR(30),
  "install_pos_code" VARCHAR(100),
  "find_no" VARCHAR(80),
  "alt_group" VARCHAR(60),
  "line_status_cd" VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS "route_card" (
  "route_card_id" SERIAL PRIMARY KEY,
  "actuator_model_id" INTEGER,
  "part_master_id" INTEGER,
  "design_revision_id" INTEGER NOT NULL,
  "route_no" VARCHAR(100) NOT NULL,
  "route_name" VARCHAR(200) NOT NULL,
  "route_class_cd" VARCHAR(30),
  "proc_doc_rev" VARCHAR(50),
  "release_state_cd" VARCHAR(30),
  "planned_time_unit" VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS "operation_card" (
  "operation_card_id" SERIAL PRIMARY KEY,
  "route_card_id" INTEGER NOT NULL,
  "op_no" VARCHAR(50) NOT NULL,
  "workid" VARCHAR(50),
  "op_name" VARCHAR(200) NOT NULL,
  "seq_no_txt" VARCHAR(20),
  "std_hours_txt" VARCHAR(40),
  "resource_type" VARCHAR(100),
  "special_proc_cd" VARCHAR(30),
  "key_op_flag" VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS "technical_parameter" (
  "technical_parameter_id" SERIAL PRIMARY KEY,
  "operation_card_id" INTEGER NOT NULL,
  "para_code" VARCHAR(100) NOT NULL,
  "para_short_name" VARCHAR(200) NOT NULL,
  "unit_txt" VARCHAR(50),
  "nominal_txt" VARCHAR(80),
  "lsl_txt" VARCHAR(80),
  "usl_txt" VARCHAR(80),
  "ctrl_grade_cd" VARCHAR(30),
  "spec_expr" VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS "inspection_standard" (
  "inspection_standard_id" SERIAL PRIMARY KEY,
  "operation_card_id" INTEGER,
  "actuator_model_id" INTEGER,
  "part_master_id" INTEGER,
  "insp_spec_no" VARCHAR(100) NOT NULL,
  "spec_title" VARCHAR(200) NOT NULL,
  "insp_kind_cd" VARCHAR(30),
  "check_item_code" VARCHAR(100),
  "check_item_name" VARCHAR(200),
  "accept_rule" TEXT,
  "sample_rule_cd" VARCHAR(30),
  "result_rule_cd" VARCHAR(30)
);

-- Comments
COMMENT ON TABLE "product_model" IS 'PLM 产品型号表，描述上级航空装备或试验装机对象的型号主数据';
COMMENT ON COLUMN "product_model"."product_model_id" IS '产品型号主键';
COMMENT ON COLUMN "product_model"."model_code" IS '产品型号编码';
COMMENT ON COLUMN "product_model"."model_name" IS '产品型号名称';
COMMENT ON COLUMN "product_model"."model_series" IS '产品系列代码';
COMMENT ON COLUMN "product_model"."product_line" IS '产品线名称';
COMMENT ON COLUMN "product_model"."actual_plane_no" IS '实际架次号或试验装机对象编号';
COMMENT ON COLUMN "product_model"."model_status_cd" IS '型号状态代码';
COMMENT ON COLUMN "product_model"."cert_level_cd" IS '密级或适航级别代码';
COMMENT ON COLUMN "product_model"."eff_date_txt" IS '型号生效日期文本';
COMMENT ON COLUMN "product_model"."expire_date_txt" IS '型号失效日期文本';

COMMENT ON TABLE "gear_system_def" IS '起落架系统定义表，记录产品型号下起落架收放系统的功能层级';
COMMENT ON COLUMN "gear_system_def"."gear_system_def_id" IS '起落架系统定义主键';
COMMENT ON COLUMN "gear_system_def"."product_model_id" IS '所属产品型号主键';
COMMENT ON COLUMN "gear_system_def"."sys_code" IS '系统编码';
COMMENT ON COLUMN "gear_system_def"."sys_name" IS '系统名称';
COMMENT ON COLUMN "gear_system_def"."install_zone" IS '安装区域或功能位置代码';
COMMENT ON COLUMN "gear_system_def"."station_no" IS '设计站位号或装机站位号';
COMMENT ON COLUMN "gear_system_def"."side_mark" IS '安装侧别标识';
COMMENT ON COLUMN "gear_system_def"."tech_status_cd" IS '技术状态代码';
COMMENT ON COLUMN "gear_system_def"."eff_date_txt" IS '系统生效日期文本';

COMMENT ON TABLE "actuator_model" IS '收放液压作动筒组件型号表，作为设计和 BOM 的核心型号锚点';
COMMENT ON COLUMN "actuator_model"."actuator_model_id" IS '作动筒型号主键';
COMMENT ON COLUMN "actuator_model"."gear_system_def_id" IS '所属起落架系统定义主键';
COMMENT ON COLUMN "actuator_model"."actuator_code" IS '作动筒组件型号编码';
COMMENT ON COLUMN "actuator_model"."actuator_name" IS '作动筒组件型号名称';
COMMENT ON COLUMN "actuator_model"."actuator_spec" IS '作动筒规格或构型说明';
COMMENT ON COLUMN "actuator_model"."dwg_no" IS '组件图号';
COMMENT ON COLUMN "actuator_model"."config_no" IS '构型编号';
COMMENT ON COLUMN "actuator_model"."model_status_cd" IS '组件型号状态代码';
COMMENT ON COLUMN "actuator_model"."secret_lvl_cd" IS '密级代码';
COMMENT ON COLUMN "actuator_model"."tech_status_text" IS '技术状态文本';

COMMENT ON TABLE "design_revision" IS '设计版本表，记录组件图纸、技术文件和构型版本的发布信息';
COMMENT ON COLUMN "design_revision"."design_revision_id" IS '设计版本主键';
COMMENT ON COLUMN "design_revision"."actuator_model_id" IS '所属作动筒型号主键';
COMMENT ON COLUMN "design_revision"."rev_no" IS '设计版本号';
COMMENT ON COLUMN "design_revision"."rev_name" IS '设计版本名称';
COMMENT ON COLUMN "design_revision"."dwg_no" IS '版本对应图号';
COMMENT ON COLUMN "design_revision"."doc_rev" IS '技术文件版本';
COMMENT ON COLUMN "design_revision"."issue_status_cd" IS '发布状态代码';
COMMENT ON COLUMN "design_revision"."eff_date_txt" IS '版本生效日期文本';
COMMENT ON COLUMN "design_revision"."expire_date_txt" IS '版本失效日期文本';
COMMENT ON COLUMN "design_revision"."released_by" IS '发布人';

COMMENT ON TABLE "part_master" IS '零件主数据表，描述作动筒缸筒、活塞杆、密封件等零件定义';
COMMENT ON COLUMN "part_master"."part_master_id" IS '零件主数据主键';
COMMENT ON COLUMN "part_master"."design_revision_id" IS '适用设计版本主键';
COMMENT ON COLUMN "part_master"."part_no" IS '零件号';
COMMENT ON COLUMN "part_master"."part_cn_name" IS '零件中文名称';
COMMENT ON COLUMN "part_master"."part_cat_cd" IS '零件类别代码';
COMMENT ON COLUMN "part_master"."dwg_no" IS '零件图号';
COMMENT ON COLUMN "part_master"."material_no" IS '材料或物料编码';
COMMENT ON COLUMN "part_master"."mat_grade" IS '材料牌号';
COMMENT ON COLUMN "part_master"."spec_text" IS '材料规格文本';
COMMENT ON COLUMN "part_master"."uom" IS '计量单位';
COMMENT ON COLUMN "part_master"."make_buy_cd" IS '自制外购代码';
COMMENT ON COLUMN "part_master"."key_level_cd" IS '关键等级代码';

COMMENT ON TABLE "bom_header" IS 'BOM 主表，定义作动筒组件在某设计版本下的结构清单';
COMMENT ON COLUMN "bom_header"."bom_header_id" IS 'BOM 主表主键';
COMMENT ON COLUMN "bom_header"."actuator_model_id" IS '所属作动筒型号主键';
COMMENT ON COLUMN "bom_header"."design_revision_id" IS '适用设计版本主键';
COMMENT ON COLUMN "bom_header"."root_part_master_id" IS '根零件主键';
COMMENT ON COLUMN "bom_header"."bom_no" IS 'BOM 编号';
COMMENT ON COLUMN "bom_header"."bom_rev" IS 'BOM 版本号';
COMMENT ON COLUMN "bom_header"."bom_status_cd" IS 'BOM 状态代码';
COMMENT ON COLUMN "bom_header"."eff_date_txt" IS 'BOM 生效日期文本';
COMMENT ON COLUMN "bom_header"."end_date_txt" IS 'BOM 失效日期文本';

COMMENT ON TABLE "bom_line" IS 'BOM 明细表，记录父子零件、用量和装配位置';
COMMENT ON COLUMN "bom_line"."bom_line_id" IS 'BOM 明细主键';
COMMENT ON COLUMN "bom_line"."bom_header_id" IS '所属 BOM 主表主键';
COMMENT ON COLUMN "bom_line"."parent_part_master_id" IS '父级零件主键';
COMMENT ON COLUMN "bom_line"."child_part_master_id" IS '子级零件主键';
COMMENT ON COLUMN "bom_line"."line_no" IS 'BOM 行号';
COMMENT ON COLUMN "bom_line"."item_no" IS 'BOM 项号';
COMMENT ON COLUMN "bom_line"."qty_per_txt" IS '单台用量文本';
COMMENT ON COLUMN "bom_line"."uom" IS '用量单位';
COMMENT ON COLUMN "bom_line"."install_pos_code" IS '装配位置编码';
COMMENT ON COLUMN "bom_line"."find_no" IS '查找号或位号';
COMMENT ON COLUMN "bom_line"."alt_group" IS '替代组号';
COMMENT ON COLUMN "bom_line"."line_status_cd" IS 'BOM 行状态代码';

COMMENT ON TABLE "route_card" IS '工艺路线卡表，定义作动筒组件或零件制造装配检验的标准流程';
COMMENT ON COLUMN "route_card"."route_card_id" IS '工艺路线卡主键';
COMMENT ON COLUMN "route_card"."actuator_model_id" IS '适用作动筒型号主键';
COMMENT ON COLUMN "route_card"."part_master_id" IS '适用零件主键';
COMMENT ON COLUMN "route_card"."design_revision_id" IS '适用设计版本主键';
COMMENT ON COLUMN "route_card"."route_no" IS '工艺路线编号';
COMMENT ON COLUMN "route_card"."route_name" IS '工艺路线名称';
COMMENT ON COLUMN "route_card"."route_class_cd" IS '路线类别代码';
COMMENT ON COLUMN "route_card"."proc_doc_rev" IS '工艺文件版本';
COMMENT ON COLUMN "route_card"."release_state_cd" IS '路线发布状态代码';
COMMENT ON COLUMN "route_card"."planned_time_unit" IS '计划工时单位';

COMMENT ON TABLE "operation_card" IS '工序卡表，记录工艺路线下的标准工序定义';
COMMENT ON COLUMN "operation_card"."operation_card_id" IS '工序卡主键';
COMMENT ON COLUMN "operation_card"."route_card_id" IS '所属工艺路线卡主键';
COMMENT ON COLUMN "operation_card"."op_no" IS '工序号';
COMMENT ON COLUMN "operation_card"."workid" IS '历史工序编码';
COMMENT ON COLUMN "operation_card"."op_name" IS '工序名称';
COMMENT ON COLUMN "operation_card"."seq_no_txt" IS '工序顺序号文本';
COMMENT ON COLUMN "operation_card"."std_hours_txt" IS '标准工时文本';
COMMENT ON COLUMN "operation_card"."resource_type" IS '资源或设备类型';
COMMENT ON COLUMN "operation_card"."special_proc_cd" IS '特殊过程代码';
COMMENT ON COLUMN "operation_card"."key_op_flag" IS '关键工序标识';

COMMENT ON TABLE "technical_parameter" IS '技术参数标准表，定义试验压力、泄漏量、镀层厚度等工艺参数范围';
COMMENT ON COLUMN "technical_parameter"."technical_parameter_id" IS '技术参数标准主键';
COMMENT ON COLUMN "technical_parameter"."operation_card_id" IS '所属工序卡主键';
COMMENT ON COLUMN "technical_parameter"."para_code" IS '参数编码';
COMMENT ON COLUMN "technical_parameter"."para_short_name" IS '参数简称或项目名称';
COMMENT ON COLUMN "technical_parameter"."unit_txt" IS '参数单位文本';
COMMENT ON COLUMN "technical_parameter"."nominal_txt" IS '名义值文本';
COMMENT ON COLUMN "technical_parameter"."lsl_txt" IS '下限值文本';
COMMENT ON COLUMN "technical_parameter"."usl_txt" IS '上限值文本';
COMMENT ON COLUMN "technical_parameter"."ctrl_grade_cd" IS '控制等级代码';
COMMENT ON COLUMN "technical_parameter"."spec_expr" IS '规格表达式';

COMMENT ON TABLE "inspection_standard" IS '检验规范表，记录作动筒组件、零件或工序的检验项目与验收规则';
COMMENT ON COLUMN "inspection_standard"."inspection_standard_id" IS '检验规范主键';
COMMENT ON COLUMN "inspection_standard"."operation_card_id" IS '关联工序卡主键';
COMMENT ON COLUMN "inspection_standard"."actuator_model_id" IS '适用作动筒型号主键';
COMMENT ON COLUMN "inspection_standard"."part_master_id" IS '适用零件主键';
COMMENT ON COLUMN "inspection_standard"."insp_spec_no" IS '检验规范编号';
COMMENT ON COLUMN "inspection_standard"."spec_title" IS '检验规范标题';
COMMENT ON COLUMN "inspection_standard"."insp_kind_cd" IS '检验类型代码';
COMMENT ON COLUMN "inspection_standard"."check_item_code" IS '检验项目编码';
COMMENT ON COLUMN "inspection_standard"."check_item_name" IS '检验项目名称';
COMMENT ON COLUMN "inspection_standard"."accept_rule" IS '验收准则';
COMMENT ON COLUMN "inspection_standard"."sample_rule_cd" IS '抽样规则代码';
COMMENT ON COLUMN "inspection_standard"."result_rule_cd" IS '判定规则代码';

-- Foreign keys
ALTER TABLE "gear_system_def" ADD CONSTRAINT "fk_gear_system_def_product_model_id" FOREIGN KEY ("product_model_id") REFERENCES "product_model" ("product_model_id");
ALTER TABLE "actuator_model" ADD CONSTRAINT "fk_actuator_model_gear_system_def_id" FOREIGN KEY ("gear_system_def_id") REFERENCES "gear_system_def" ("gear_system_def_id");
ALTER TABLE "design_revision" ADD CONSTRAINT "fk_design_revision_actuator_model_id" FOREIGN KEY ("actuator_model_id") REFERENCES "actuator_model" ("actuator_model_id");
ALTER TABLE "part_master" ADD CONSTRAINT "fk_part_master_design_revision_id" FOREIGN KEY ("design_revision_id") REFERENCES "design_revision" ("design_revision_id");
ALTER TABLE "bom_header" ADD CONSTRAINT "fk_bom_header_actuator_model_id" FOREIGN KEY ("actuator_model_id") REFERENCES "actuator_model" ("actuator_model_id");
ALTER TABLE "bom_header" ADD CONSTRAINT "fk_bom_header_design_revision_id" FOREIGN KEY ("design_revision_id") REFERENCES "design_revision" ("design_revision_id");
ALTER TABLE "bom_header" ADD CONSTRAINT "fk_bom_header_root_part_master_id" FOREIGN KEY ("root_part_master_id") REFERENCES "part_master" ("part_master_id");
ALTER TABLE "bom_line" ADD CONSTRAINT "fk_bom_line_bom_header_id" FOREIGN KEY ("bom_header_id") REFERENCES "bom_header" ("bom_header_id");
ALTER TABLE "bom_line" ADD CONSTRAINT "fk_bom_line_parent_part_master_id" FOREIGN KEY ("parent_part_master_id") REFERENCES "part_master" ("part_master_id");
ALTER TABLE "bom_line" ADD CONSTRAINT "fk_bom_line_child_part_master_id" FOREIGN KEY ("child_part_master_id") REFERENCES "part_master" ("part_master_id");
ALTER TABLE "route_card" ADD CONSTRAINT "fk_route_card_actuator_model_id" FOREIGN KEY ("actuator_model_id") REFERENCES "actuator_model" ("actuator_model_id");
ALTER TABLE "route_card" ADD CONSTRAINT "fk_route_card_part_master_id" FOREIGN KEY ("part_master_id") REFERENCES "part_master" ("part_master_id");
ALTER TABLE "route_card" ADD CONSTRAINT "fk_route_card_design_revision_id" FOREIGN KEY ("design_revision_id") REFERENCES "design_revision" ("design_revision_id");
ALTER TABLE "operation_card" ADD CONSTRAINT "fk_operation_card_route_card_id" FOREIGN KEY ("route_card_id") REFERENCES "route_card" ("route_card_id");
ALTER TABLE "technical_parameter" ADD CONSTRAINT "fk_technical_parameter_operation_card_id" FOREIGN KEY ("operation_card_id") REFERENCES "operation_card" ("operation_card_id");
ALTER TABLE "inspection_standard" ADD CONSTRAINT "fk_inspection_standard_operation_card_id" FOREIGN KEY ("operation_card_id") REFERENCES "operation_card" ("operation_card_id");
ALTER TABLE "inspection_standard" ADD CONSTRAINT "fk_inspection_standard_actuator_model_id" FOREIGN KEY ("actuator_model_id") REFERENCES "actuator_model" ("actuator_model_id");
ALTER TABLE "inspection_standard" ADD CONSTRAINT "fk_inspection_standard_part_master_id" FOREIGN KEY ("part_master_id") REFERENCES "part_master" ("part_master_id");

-- Indexes
CREATE UNIQUE INDEX IF NOT EXISTS "uq_product_model_model_code" ON "product_model" ("model_code");
CREATE INDEX IF NOT EXISTS "idx_gear_system_def_product_model_id" ON "gear_system_def" ("product_model_id");
CREATE INDEX IF NOT EXISTS "idx_gear_system_def_sys_code" ON "gear_system_def" ("sys_code");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_actuator_model_actuator_code" ON "actuator_model" ("actuator_code");
CREATE INDEX IF NOT EXISTS "idx_actuator_model_gear_system_def_id" ON "actuator_model" ("gear_system_def_id");
CREATE INDEX IF NOT EXISTS "idx_design_revision_actuator_model_id" ON "design_revision" ("actuator_model_id");
CREATE INDEX IF NOT EXISTS "idx_design_revision_rev_no" ON "design_revision" ("rev_no");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_part_master_part_no_design_revision_id" ON "part_master" ("part_no", "design_revision_id");
CREATE INDEX IF NOT EXISTS "idx_part_master_material_no" ON "part_master" ("material_no");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_bom_header_bom_no" ON "bom_header" ("bom_no");
CREATE INDEX IF NOT EXISTS "idx_bom_header_actuator_model_id" ON "bom_header" ("actuator_model_id");
CREATE INDEX IF NOT EXISTS "idx_bom_header_design_revision_id" ON "bom_header" ("design_revision_id");
CREATE INDEX IF NOT EXISTS "idx_bom_line_bom_header_id" ON "bom_line" ("bom_header_id");
CREATE INDEX IF NOT EXISTS "idx_bom_line_child_part_master_id" ON "bom_line" ("child_part_master_id");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_bom_line_bom_header_id_item_no" ON "bom_line" ("bom_header_id", "item_no");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_route_card_route_no" ON "route_card" ("route_no");
CREATE INDEX IF NOT EXISTS "idx_route_card_actuator_model_id" ON "route_card" ("actuator_model_id");
CREATE INDEX IF NOT EXISTS "idx_route_card_part_master_id" ON "route_card" ("part_master_id");
CREATE INDEX IF NOT EXISTS "idx_operation_card_route_card_id" ON "operation_card" ("route_card_id");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_operation_card_route_card_id_op_no" ON "operation_card" ("route_card_id", "op_no");
CREATE INDEX IF NOT EXISTS "idx_technical_parameter_operation_card_id" ON "technical_parameter" ("operation_card_id");
CREATE INDEX IF NOT EXISTS "idx_technical_parameter_para_code" ON "technical_parameter" ("para_code");
CREATE INDEX IF NOT EXISTS "idx_inspection_standard_insp_spec_no" ON "inspection_standard" ("insp_spec_no");
CREATE INDEX IF NOT EXISTS "idx_inspection_standard_operation_card_id" ON "inspection_standard" ("operation_card_id");
CREATE INDEX IF NOT EXISTS "idx_inspection_standard_part_master_id" ON "inspection_standard" ("part_master_id");
