-- Auto-generated from DBML for CF aviation simulated dataset.
-- Source DBML: 数字卷宗关系型数据库表设计.dbml
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

CREATE TABLE IF NOT EXISTS "upper_equipment" (
  "upper_equipment_id" SERIAL PRIMARY KEY,
  "equipment_code" VARCHAR(100) NOT NULL,
  "equipment_name" VARCHAR(200) NOT NULL,
  "equipment_type" VARCHAR(100),
  "product_code" VARCHAR(100),
  "product_name" VARCHAR(200),
  "actual_plane_no" VARCHAR(100),
  "secret_level" VARCHAR(20),
  "created_at" TIMESTAMP,
  "updated_at" TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "landing_gear_system" (
  "landing_gear_system_id" SERIAL PRIMARY KEY,
  "upper_equipment_id" INTEGER NOT NULL,
  "system_code" VARCHAR(100) NOT NULL,
  "system_name" VARCHAR(200) NOT NULL,
  "system_position" VARCHAR(100),
  "technical_status" VARCHAR(100),
  "created_at" TIMESTAMP,
  "updated_at" TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "installed_position" (
  "installed_position_id" SERIAL PRIMARY KEY,
  "upper_equipment_id" INTEGER,
  "landing_gear_system_id" INTEGER,
  "position_code" VARCHAR(100) NOT NULL,
  "position_name" VARCHAR(200) NOT NULL,
  "station_no" VARCHAR(100),
  "side_code" VARCHAR(50),
  "created_at" TIMESTAMP,
  "updated_at" TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "component_type" (
  "component_type_id" SERIAL PRIMARY KEY,
  "component_code" VARCHAR(100) NOT NULL,
  "component_name" VARCHAR(200) NOT NULL,
  "component_spec" VARCHAR(200),
  "product_code" VARCHAR(100),
  "product_name" VARCHAR(200),
  "drawing_code" VARCHAR(200),
  "technical_status" VARCHAR(100),
  "created_at" TIMESTAMP,
  "updated_at" TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "design_version" (
  "design_version_id" SERIAL PRIMARY KEY,
  "version_code" VARCHAR(100) NOT NULL,
  "version_name" VARCHAR(200),
  "drawing_code" VARCHAR(200),
  "doc_version" VARCHAR(50),
  "effective_date" DATE,
  "expire_date" DATE,
  "release_status" VARCHAR(50),
  "created_at" TIMESTAMP,
  "updated_at" TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "standard_dictionary" (
  "dict_id" SERIAL PRIMARY KEY,
  "dict_type" VARCHAR(100) NOT NULL,
  "standard_code" VARCHAR(100) NOT NULL,
  "standard_name" VARCHAR(200) NOT NULL,
  "standard_desc" TEXT,
  "enabled" BOOLEAN,
  "sort_no" INTEGER
);

CREATE TABLE IF NOT EXISTS "material" (
  "material_id" SERIAL PRIMARY KEY,
  "material_code" VARCHAR(100) NOT NULL,
  "material_name" VARCHAR(200) NOT NULL,
  "material_type" VARCHAR(80),
  "trademark" VARCHAR(100),
  "material_spec" VARCHAR(200),
  "unit" VARCHAR(30),
  "purchased_flag" BOOLEAN,
  "created_at" TIMESTAMP,
  "updated_at" TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "part_definition" (
  "part_definition_id" SERIAL PRIMARY KEY,
  "material_id" INTEGER,
  "design_version_id" INTEGER,
  "part_no" VARCHAR(100) NOT NULL,
  "part_name" VARCHAR(200) NOT NULL,
  "part_type" VARCHAR(80),
  "drawing_code" VARCHAR(200),
  "source_type" VARCHAR(50),
  "critical_level" VARCHAR(50),
  "created_at" TIMESTAMP,
  "updated_at" TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "component_instance" (
  "component_instance_id" SERIAL PRIMARY KEY,
  "component_type_id" INTEGER NOT NULL,
  "installed_position_id" INTEGER,
  "design_version_id" INTEGER,
  "component_serial_no" VARCHAR(100) NOT NULL,
  "component_batch_no" VARCHAR(100),
  "quality_code" VARCHAR(200),
  "assembly_order_no" VARCHAR(100),
  "delivery_date" DATE,
  "component_status" VARCHAR(50),
  "created_at" TIMESTAMP,
  "updated_at" TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "bom_item" (
  "bom_item_id" SERIAL PRIMARY KEY,
  "component_type_id" INTEGER NOT NULL,
  "parent_bom_item_id" INTEGER,
  "part_definition_id" INTEGER NOT NULL,
  "bom_item_no" VARCHAR(100) NOT NULL,
  "part_no" VARCHAR(100),
  "quantity" DECIMAL(12,3) NOT NULL,
  "unit" VARCHAR(30),
  "position_code" VARCHAR(100),
  "effective_date" DATE
);

CREATE TABLE IF NOT EXISTS "process_route" (
  "process_route_id" SERIAL PRIMARY KEY,
  "component_type_id" INTEGER,
  "part_definition_id" INTEGER,
  "design_version_id" INTEGER,
  "route_code" VARCHAR(100) NOT NULL,
  "route_name" VARCHAR(200) NOT NULL,
  "route_type" VARCHAR(80),
  "doc_version" VARCHAR(50),
  "release_status" VARCHAR(50),
  "created_at" TIMESTAMP,
  "updated_at" TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "operation_definition" (
  "operation_definition_id" SERIAL PRIMARY KEY,
  "process_route_id" INTEGER NOT NULL,
  "operation_no" VARCHAR(50) NOT NULL,
  "work_no" VARCHAR(50),
  "work_name" VARCHAR(200) NOT NULL,
  "work_num" INTEGER,
  "standard_time" DECIMAL(10,2),
  "required_equipment_type" VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS "parameter_standard" (
  "parameter_standard_id" SERIAL PRIMARY KEY,
  "operation_definition_id" INTEGER NOT NULL,
  "parameter_code" VARCHAR(100) NOT NULL,
  "parameter_name" VARCHAR(200) NOT NULL,
  "parameter_unit" VARCHAR(50),
  "standard_value" DECIMAL(18,6),
  "lower_limit" DECIMAL(18,6),
  "upper_limit" DECIMAL(18,6),
  "control_level" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "inspection_spec" (
  "inspection_spec_id" SERIAL PRIMARY KEY,
  "operation_definition_id" INTEGER,
  "component_type_id" INTEGER,
  "part_definition_id" INTEGER,
  "spec_code" VARCHAR(100) NOT NULL,
  "spec_name" VARCHAR(200) NOT NULL,
  "inspection_type" VARCHAR(80),
  "item_code" VARCHAR(100),
  "item_name" VARCHAR(200),
  "acceptance_criteria" TEXT
);

CREATE TABLE IF NOT EXISTS "supplier" (
  "supplier_id" SERIAL PRIMARY KEY,
  "supplier_code" VARCHAR(100) NOT NULL,
  "supplier_name" VARCHAR(200) NOT NULL,
  "supplier_type" VARCHAR(80),
  "qualification_level" VARCHAR(80),
  "contact_person" VARCHAR(100),
  "phone" VARCHAR(50),
  "address" TEXT,
  "enabled" BOOLEAN
);

CREATE TABLE IF NOT EXISTS "purchase_order" (
  "purchase_order_id" SERIAL PRIMARY KEY,
  "supplier_id" INTEGER NOT NULL,
  "order_no" VARCHAR(100) NOT NULL,
  "task_code" VARCHAR(100),
  "order_date" DATE,
  "delivery_date" DATE,
  "order_status" VARCHAR(50),
  "priority_level" INTEGER,
  "urgent_flag" BOOLEAN
);

CREATE TABLE IF NOT EXISTS "purchase_order_line" (
  "purchase_order_line_id" SERIAL PRIMARY KEY,
  "purchase_order_id" INTEGER NOT NULL,
  "material_id" INTEGER NOT NULL,
  "line_no" INTEGER NOT NULL,
  "amount" DECIMAL(18,3) NOT NULL,
  "unit" VARCHAR(30),
  "required_date" DATE,
  "delivery_date" DATE,
  "line_status" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "supplier_batch" (
  "supplier_batch_id" SERIAL PRIMARY KEY,
  "purchase_order_line_id" INTEGER NOT NULL,
  "supplier_id" INTEGER NOT NULL,
  "material_id" INTEGER NOT NULL,
  "supplier_batch_no" VARCHAR(100) NOT NULL,
  "certificate_no" VARCHAR(100),
  "manufacture_date" DATE,
  "expire_date" DATE,
  "batch_status" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "inventory_batch" (
  "inventory_batch_id" SERIAL PRIMARY KEY,
  "supplier_batch_id" INTEGER,
  "material_id" INTEGER NOT NULL,
  "inventory_batch_no" VARCHAR(100) NOT NULL,
  "warehouse_code" VARCHAR(100),
  "location_code" VARCHAR(100),
  "amount" DECIMAL(18,3),
  "unit" VARCHAR(30),
  "batch_status" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "receiving_record" (
  "receiving_record_id" SERIAL PRIMARY KEY,
  "inventory_batch_id" INTEGER NOT NULL,
  "purchase_order_line_id" INTEGER,
  "receiving_no" VARCHAR(100) NOT NULL,
  "receiving_date" TIMESTAMP,
  "received_amount" DECIMAL(18,3),
  "receiver_name" VARCHAR(100),
  "inspection_required" BOOLEAN,
  "receiving_status" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "issue_record" (
  "issue_record_id" SERIAL PRIMARY KEY,
  "inventory_batch_id" INTEGER NOT NULL,
  "work_order_id" INTEGER,
  "issue_no" VARCHAR(100) NOT NULL,
  "issue_date" TIMESTAMP,
  "issued_amount" DECIMAL(18,3),
  "issue_person_name" VARCHAR(100),
  "issue_purpose" VARCHAR(200),
  "issue_status" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "work_order" (
  "work_order_id" SERIAL PRIMARY KEY,
  "component_instance_id" INTEGER,
  "part_definition_id" INTEGER,
  "process_route_id" INTEGER,
  "order_no" VARCHAR(100) NOT NULL,
  "task_code" VARCHAR(100),
  "work_order_type" VARCHAR(50) NOT NULL,
  "order_name" VARCHAR(200),
  "amount" INTEGER,
  "priority_level" INTEGER,
  "urgent_flag" BOOLEAN,
  "plan_start_time" TIMESTAMP,
  "plan_finish_time" TIMESTAMP,
  "report_person_name" VARCHAR(100),
  "report_time" TIMESTAMP,
  "order_status" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "production_batch" (
  "production_batch_id" SERIAL PRIMARY KEY,
  "work_order_id" INTEGER NOT NULL,
  "part_definition_id" INTEGER,
  "production_batch_no" VARCHAR(100) NOT NULL,
  "batch_amount" INTEGER,
  "start_time" TIMESTAMP,
  "finish_time" TIMESTAMP,
  "batch_status" VARCHAR(50),
  "quality_code" VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS "part_instance" (
  "part_instance_id" SERIAL PRIMARY KEY,
  "part_definition_id" INTEGER NOT NULL,
  "production_batch_id" INTEGER,
  "inventory_batch_id" INTEGER,
  "supplier_batch_id" INTEGER,
  "part_serial_no" VARCHAR(100),
  "part_lot_no" VARCHAR(100),
  "piece_no" VARCHAR(100),
  "quality_code" VARCHAR(200),
  "source_type" VARCHAR(50),
  "part_status" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "component_part_installation" (
  "installation_id" SERIAL PRIMARY KEY,
  "component_instance_id" INTEGER NOT NULL,
  "part_instance_id" INTEGER NOT NULL,
  "bom_item_id" INTEGER,
  "work_order_id" INTEGER,
  "installed_position_id" INTEGER,
  "replacement_record_id" INTEGER,
  "install_time" TIMESTAMP,
  "valid_from" TIMESTAMP,
  "valid_to" TIMESTAMP,
  "uninstall_time" TIMESTAMP,
  "uninstall_reason" VARCHAR(200),
  "install_person_name" VARCHAR(100),
  "install_status" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "workstation" (
  "workstation_id" SERIAL PRIMARY KEY,
  "station_no" VARCHAR(50) NOT NULL,
  "station_name" VARCHAR(200) NOT NULL,
  "workshop" VARCHAR(100),
  "person_count" INTEGER,
  "equipment_count" INTEGER,
  "station_status" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "equipment" (
  "equipment_id" SERIAL PRIMARY KEY,
  "workstation_id" INTEGER,
  "equipment_no" VARCHAR(100) NOT NULL,
  "equipment_name" VARCHAR(200) NOT NULL,
  "equipment_num" INTEGER,
  "equipment_type" VARCHAR(100),
  "status_code" VARCHAR(50),
  "color_flag" VARCHAR(50),
  "created_at" TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "personnel" (
  "personnel_id" SERIAL PRIMARY KEY,
  "person_no" VARCHAR(100) NOT NULL,
  "person_name" VARCHAR(100) NOT NULL,
  "workshop" VARCHAR(100),
  "phone" VARCHAR(50),
  "skill_level" VARCHAR(50),
  "work_type" VARCHAR(50),
  "dept_code" VARCHAR(100),
  "dept_name" VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS "tooling" (
  "tooling_id" SERIAL PRIMARY KEY,
  "tooling_no" VARCHAR(100) NOT NULL,
  "tooling_name" VARCHAR(200) NOT NULL,
  "tooling_type" VARCHAR(100),
  "calibration_due_date" DATE,
  "tooling_status" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "operation_execution" (
  "operation_execution_id" SERIAL PRIMARY KEY,
  "work_order_id" INTEGER NOT NULL,
  "operation_definition_id" INTEGER NOT NULL,
  "component_instance_id" INTEGER,
  "part_instance_id" INTEGER,
  "workstation_id" INTEGER,
  "equipment_id" INTEGER,
  "op_no" VARCHAR(50) NOT NULL,
  "work_no" VARCHAR(50),
  "work_name" VARCHAR(200),
  "start_time" TIMESTAMP,
  "finish_time" TIMESTAMP,
  "execution_status" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "step_execution" (
  "step_execution_id" SERIAL PRIMARY KEY,
  "operation_execution_id" INTEGER NOT NULL,
  "step_no" VARCHAR(50) NOT NULL,
  "step_name" VARCHAR(200) NOT NULL,
  "step_sequence" INTEGER,
  "start_time" TIMESTAMP,
  "finish_time" TIMESTAMP,
  "step_status" VARCHAR(50),
  "operator_name" VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS "resource_usage" (
  "resource_usage_id" SERIAL PRIMARY KEY,
  "operation_execution_id" INTEGER NOT NULL,
  "step_execution_id" INTEGER,
  "equipment_id" INTEGER,
  "personnel_id" INTEGER,
  "workstation_id" INTEGER,
  "tooling_id" INTEGER,
  "usage_start_time" TIMESTAMP,
  "usage_finish_time" TIMESTAMP,
  "mastery" DECIMAL(5,2),
  "usage_status" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "process_parameter_record" (
  "process_parameter_record_id" SERIAL PRIMARY KEY,
  "operation_execution_id" INTEGER NOT NULL,
  "step_execution_id" INTEGER,
  "parameter_standard_id" INTEGER,
  "equipment_id" INTEGER,
  "parameter_code" VARCHAR(100) NOT NULL,
  "parameter_name" VARCHAR(200) NOT NULL,
  "measured_value" DECIMAL(18,6),
  "standard_value" DECIMAL(18,6),
  "lower_limit" DECIMAL(18,6),
  "upper_limit" DECIMAL(18,6),
  "parameter_unit" VARCHAR(50),
  "collect_time" TIMESTAMP,
  "judge_result" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "inspection_record" (
  "inspection_record_id" SERIAL PRIMARY KEY,
  "inspection_spec_id" INTEGER,
  "component_instance_id" INTEGER,
  "part_instance_id" INTEGER,
  "operation_execution_id" INTEGER,
  "inventory_batch_id" INTEGER,
  "work_order_id" INTEGER,
  "inspector_id" INTEGER,
  "inspection_no" VARCHAR(100) NOT NULL,
  "inspection_type" VARCHAR(80),
  "find_date" TIMESTAMP,
  "find_type" VARCHAR(80),
  "inspection_result" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "inspection_item_result" (
  "inspection_item_result_id" SERIAL PRIMARY KEY,
  "inspection_record_id" INTEGER NOT NULL,
  "inspection_spec_id" INTEGER,
  "item_code" VARCHAR(100) NOT NULL,
  "item_name" VARCHAR(200) NOT NULL,
  "measured_value" VARCHAR(200),
  "standard_value" VARCHAR(200),
  "lower_limit" VARCHAR(100),
  "upper_limit" VARCHAR(100),
  "unit" VARCHAR(50),
  "judge_result" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "defect_record" (
  "defect_record_id" SERIAL PRIMARY KEY,
  "inspection_record_id" INTEGER,
  "component_instance_id" INTEGER,
  "part_instance_id" INTEGER,
  "operation_execution_id" INTEGER,
  "defect_code" VARCHAR(100) NOT NULL,
  "defect_name" VARCHAR(200),
  "defect_position" VARCHAR(200),
  "severity_level" VARCHAR(50),
  "failure_desc" TEXT,
  "find_dept_code" VARCHAR(100),
  "find_dept_name" VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS "maintenance_event" (
  "maintenance_event_id" SERIAL PRIMARY KEY,
  "component_instance_id" INTEGER NOT NULL,
  "installed_position_id" INTEGER,
  "event_no" VARCHAR(100) NOT NULL,
  "service_date" TIMESTAMP,
  "event_type" VARCHAR(80),
  "service_unit_code" VARCHAR(100),
  "service_unit_name" VARCHAR(200),
  "event_status" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "quality_event" (
  "quality_event_id" SERIAL PRIMARY KEY,
  "event_no" VARCHAR(100) NOT NULL,
  "event_source" VARCHAR(80),
  "event_type" VARCHAR(80),
  "event_title" VARCHAR(200),
  "event_desc" TEXT,
  "event_time" TIMESTAMP,
  "severity_level" VARCHAR(50),
  "event_status" VARCHAR(50),
  "trigger_source_no" VARCHAR(120),
  "created_at" TIMESTAMP,
  "updated_at" TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "iqs_failure" (
  "iqs_failure_id" SERIAL PRIMARY KEY,
  "quality_event_id" INTEGER NOT NULL,
  "inspection_record_id" INTEGER,
  "defect_record_id" INTEGER,
  "component_instance_id" INTEGER,
  "part_instance_id" INTEGER,
  "work_order_id" INTEGER,
  "operation_execution_id" INTEGER,
  "supplier_id" INTEGER,
  "maintenance_event_id" INTEGER,
  "doc_source" VARCHAR(80),
  "failure_type" VARCHAR(80),
  "quality_code" VARCHAR(200) NOT NULL,
  "iqs_failure_order" VARCHAR(120) NOT NULL,
  "task_code" VARCHAR(100),
  "product_code" VARCHAR(100),
  "product_name" VARCHAR(200),
  "piece_no" VARCHAR(100),
  "op_no" VARCHAR(50),
  "find_dept_code" VARCHAR(100),
  "find_dept_name" VARCHAR(200),
  "doc_status" VARCHAR(50),
  "secret_level" VARCHAR(20),
  "start_time" TIMESTAMP,
  "finish_time" TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "iqs_failure_content" (
  "iqs_failure_content_id" SERIAL PRIMARY KEY,
  "iqs_failure_id" INTEGER NOT NULL,
  "start_piece_no" INTEGER,
  "end_piece_no" INTEGER,
  "failure_desc" TEXT NOT NULL,
  "failure_place_brief" TEXT,
  "find_type" VARCHAR(80),
  "find_date" TIMESTAMP,
  "finder_type" VARCHAR(80),
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
  "duty_personnel_id" INTEGER,
  "supplier_id" INTEGER,
  "duty_dept_code" VARCHAR(100),
  "duty_dept_name" VARCHAR(200),
  "duty_user_code" VARCHAR(100),
  "duty_user_name" VARCHAR(100),
  "duty_person_ratio" DECIMAL(5,2),
  "responsibility_date" TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "disposition_record" (
  "disposition_record_id" SERIAL PRIMARY KEY,
  "quality_event_id" INTEGER NOT NULL,
  "iqs_failure_id" INTEGER,
  "iqs_failure_content_id" INTEGER,
  "work_order_id" INTEGER,
  "responsible_personnel_id" INTEGER,
  "disposition_no" VARCHAR(100) NOT NULL,
  "deal_type_find" VARCHAR(80),
  "deal_type_pass" VARCHAR(80),
  "deal_type_tech" VARCHAR(80),
  "disposition_type" VARCHAR(80),
  "disposition_desc" TEXT,
  "disposition_date" TIMESTAMP,
  "disposition_status" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "reinspection_record" (
  "reinspection_record_id" SERIAL PRIMARY KEY,
  "disposition_record_id" INTEGER NOT NULL,
  "inspection_record_id" INTEGER,
  "inspector_id" INTEGER,
  "reinspection_no" VARCHAR(100) NOT NULL,
  "reinspection_date" TIMESTAMP,
  "reinspection_result" VARCHAR(50),
  "conclusion_desc" TEXT
);

CREATE TABLE IF NOT EXISTS "maintenance_order" (
  "maintenance_order_id" SERIAL PRIMARY KEY,
  "maintenance_event_id" INTEGER NOT NULL,
  "component_instance_id" INTEGER,
  "work_order_id" INTEGER,
  "maintenance_order_no" VARCHAR(100) NOT NULL,
  "task_name" VARCHAR(200),
  "plan_start_time" TIMESTAMP,
  "plan_finish_time" TIMESTAMP,
  "order_status" VARCHAR(50),
  "report_person_name" VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS "fault_record" (
  "fault_record_id" SERIAL PRIMARY KEY,
  "maintenance_event_id" INTEGER NOT NULL,
  "component_instance_id" INTEGER,
  "fault_code" VARCHAR(100) NOT NULL,
  "fault_name" VARCHAR(200),
  "fault_desc" TEXT,
  "fault_position" VARCHAR(200),
  "find_date" TIMESTAMP,
  "finder_name" VARCHAR(100),
  "severity_level" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "replacement_record" (
  "replacement_record_id" SERIAL PRIMARY KEY,
  "maintenance_event_id" INTEGER NOT NULL,
  "maintenance_order_id" INTEGER,
  "component_instance_id" INTEGER,
  "installed_position_id" INTEGER,
  "removed_part_instance_id" INTEGER,
  "installed_part_instance_id" INTEGER,
  "inventory_batch_id" INTEGER,
  "replacement_no" VARCHAR(100) NOT NULL,
  "replacement_reason" VARCHAR(200),
  "replacement_time" TIMESTAMP,
  "operator_name" VARCHAR(100),
  "replacement_status" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "service_feedback" (
  "service_feedback_id" SERIAL PRIMARY KEY,
  "quality_event_id" INTEGER,
  "maintenance_event_id" INTEGER,
  "component_instance_id" INTEGER,
  "iqs_failure_id" INTEGER,
  "feedback_no" VARCHAR(100) NOT NULL,
  "feedback_source" VARCHAR(100),
  "feedback_date" TIMESTAMP,
  "feedback_desc" TEXT,
  "feedback_status" VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS "quality_event_subject" (
  "quality_event_subject_id" SERIAL PRIMARY KEY,
  "quality_event_id" INTEGER NOT NULL,
  "component_instance_id" INTEGER,
  "part_instance_id" INTEGER,
  "production_batch_id" INTEGER,
  "inventory_batch_id" INTEGER,
  "supplier_batch_id" INTEGER,
  "work_order_id" INTEGER,
  "operation_execution_id" INTEGER,
  "step_execution_id" INTEGER,
  "process_parameter_record_id" INTEGER,
  "inspection_record_id" INTEGER,
  "defect_record_id" INTEGER,
  "purchase_order_id" INTEGER,
  "supplier_id" INTEGER,
  "maintenance_event_id" INTEGER,
  "replacement_record_id" INTEGER,
  "service_feedback_id" INTEGER,
  "subject_type" VARCHAR(80) NOT NULL,
  "subject_role" VARCHAR(80),
  "impact_desc" TEXT,
  "created_at" TIMESTAMP
);

-- Comments
COMMENT ON TABLE "upper_equipment" IS '上级航空装备或试验装机对象表，只作为作动筒组件装机背景，不展开整机结构';
COMMENT ON COLUMN "upper_equipment"."upper_equipment_id" IS '上级装备主键，单列主键用于结构匹配锚点';
COMMENT ON COLUMN "upper_equipment"."equipment_code" IS '上级装备编号，参考机型编码或实际架次等真实业务编号';
COMMENT ON COLUMN "upper_equipment"."equipment_name" IS '上级装备名称';
COMMENT ON COLUMN "upper_equipment"."equipment_type" IS '装备类型，例如试验装机对象或某型航空装备';
COMMENT ON COLUMN "upper_equipment"."product_code" IS '机型编码，借鉴 IQS 中 PRODUCT_CODE 字段风格';
COMMENT ON COLUMN "upper_equipment"."product_name" IS '机型名称，借鉴 IQS 中 PRODUCT_NAME 字段风格';
COMMENT ON COLUMN "upper_equipment"."actual_plane_no" IS '实际架次，借鉴 IQS 中 ACTUAL_PLANE_NO 字段风格';
COMMENT ON COLUMN "upper_equipment"."secret_level" IS '密级代码，借鉴 IQS 中 SECRET_LEVEL 字段风格';
COMMENT ON COLUMN "upper_equipment"."created_at" IS '创建时间';
COMMENT ON COLUMN "upper_equipment"."updated_at" IS '更新时间';

COMMENT ON TABLE "landing_gear_system" IS '起落架系统表，用于说明作动筒组件所属功能位置';
COMMENT ON COLUMN "landing_gear_system"."landing_gear_system_id" IS '起落架系统主键';
COMMENT ON COLUMN "landing_gear_system"."upper_equipment_id" IS '所属上级装备主键';
COMMENT ON COLUMN "landing_gear_system"."system_code" IS '起落架系统编号';
COMMENT ON COLUMN "landing_gear_system"."system_name" IS '起落架系统名称';
COMMENT ON COLUMN "landing_gear_system"."system_position" IS '系统位置，例如前起落架或主起落架';
COMMENT ON COLUMN "landing_gear_system"."technical_status" IS '技术状态';
COMMENT ON COLUMN "landing_gear_system"."created_at" IS '创建时间';
COMMENT ON COLUMN "landing_gear_system"."updated_at" IS '更新时间';

COMMENT ON TABLE "installed_position" IS '作动筒组件或零件的装机位置表，服务 MRO 拆换和服役追溯';
COMMENT ON COLUMN "installed_position"."installed_position_id" IS '装机位置主键';
COMMENT ON COLUMN "installed_position"."upper_equipment_id" IS '所属上级装备主键';
COMMENT ON COLUMN "installed_position"."landing_gear_system_id" IS '所属起落架系统主键';
COMMENT ON COLUMN "installed_position"."position_code" IS '装机位置编码';
COMMENT ON COLUMN "installed_position"."position_name" IS '装机位置名称';
COMMENT ON COLUMN "installed_position"."station_no" IS '站位号或安装站位编号';
COMMENT ON COLUMN "installed_position"."side_code" IS '左右侧或安装侧别代码';
COMMENT ON COLUMN "installed_position"."created_at" IS '创建时间';
COMMENT ON COLUMN "installed_position"."updated_at" IS '更新时间';

COMMENT ON TABLE "component_type" IS '起落架收放液压作动筒组件型号表';
COMMENT ON COLUMN "component_type"."component_type_id" IS '组件型号主键';
COMMENT ON COLUMN "component_type"."component_code" IS '作动筒组件型号编码';
COMMENT ON COLUMN "component_type"."component_name" IS '作动筒组件型号名称';
COMMENT ON COLUMN "component_type"."component_spec" IS '组件规格或构型说明';
COMMENT ON COLUMN "component_type"."product_code" IS '适用机型编码，借鉴 IQS 中 PRODUCT_CODE 字段风格';
COMMENT ON COLUMN "component_type"."product_name" IS '适用机型名称，借鉴 IQS 中 PRODUCT_NAME 字段风格';
COMMENT ON COLUMN "component_type"."drawing_code" IS '组件图号，借鉴 IQS 中 DRAWING_CODE 字段风格';
COMMENT ON COLUMN "component_type"."technical_status" IS '技术状态';
COMMENT ON COLUMN "component_type"."created_at" IS '创建时间';
COMMENT ON COLUMN "component_type"."updated_at" IS '更新时间';

COMMENT ON TABLE "design_version" IS '组件、零件、图纸或技术文件的设计版本表';
COMMENT ON COLUMN "design_version"."design_version_id" IS '设计版本主键';
COMMENT ON COLUMN "design_version"."version_code" IS '设计版本编码';
COMMENT ON COLUMN "design_version"."version_name" IS '设计版本名称';
COMMENT ON COLUMN "design_version"."drawing_code" IS '图号编码，借鉴 IQS 中 DRAWING_CODE 字段风格';
COMMENT ON COLUMN "design_version"."doc_version" IS '文件版本，借鉴 IQS 中 DOC_VERSION 字段风格';
COMMENT ON COLUMN "design_version"."effective_date" IS '版本生效日期';
COMMENT ON COLUMN "design_version"."expire_date" IS '版本失效日期';
COMMENT ON COLUMN "design_version"."release_status" IS '发布状态';
COMMENT ON COLUMN "design_version"."created_at" IS '创建时间';
COMMENT ON COLUMN "design_version"."updated_at" IS '更新时间';

COMMENT ON TABLE "standard_dictionary" IS '卷宗库标准字典表，用于定义检验结果、工序状态、不合格类型、处置类型等标准枚举值；不保存源系统私有编码映射';
COMMENT ON COLUMN "standard_dictionary"."dict_id" IS '标准字典主键';
COMMENT ON COLUMN "standard_dictionary"."dict_type" IS '字典类型，例如 inspection_result、operation_status、failure_type、disposition_type';
COMMENT ON COLUMN "standard_dictionary"."standard_code" IS '卷宗标准编码';
COMMENT ON COLUMN "standard_dictionary"."standard_name" IS '卷宗标准名称';
COMMENT ON COLUMN "standard_dictionary"."standard_desc" IS '标准字典值说明';
COMMENT ON COLUMN "standard_dictionary"."enabled" IS '是否启用';
COMMENT ON COLUMN "standard_dictionary"."sort_no" IS '排序号';

COMMENT ON TABLE "material" IS '物料主数据表，承接 ERP 采购库存语义';
COMMENT ON COLUMN "material"."material_id" IS '物料主键';
COMMENT ON COLUMN "material"."material_code" IS '物料编码';
COMMENT ON COLUMN "material"."material_name" IS '物料名称';
COMMENT ON COLUMN "material"."material_type" IS '物料类型，例如自制件、外购件、原材料、外协服务';
COMMENT ON COLUMN "material"."trademark" IS '材料牌号，借鉴 IQS 中 TRADEMARK 字段风格';
COMMENT ON COLUMN "material"."material_spec" IS '材料规格';
COMMENT ON COLUMN "material"."unit" IS '计量单位';
COMMENT ON COLUMN "material"."purchased_flag" IS '是否外购，参考订单表中 PURCHASED 字段语义';
COMMENT ON COLUMN "material"."created_at" IS '创建时间';
COMMENT ON COLUMN "material"."updated_at" IS '更新时间';

COMMENT ON TABLE "part_definition" IS '作动筒零件号层面的定义表';
COMMENT ON COLUMN "part_definition"."part_definition_id" IS '零件定义主键';
COMMENT ON COLUMN "part_definition"."material_id" IS '对应物料主键';
COMMENT ON COLUMN "part_definition"."design_version_id" IS '适用设计版本主键';
COMMENT ON COLUMN "part_definition"."part_no" IS '零件号，借鉴 IQS 中 PIECE_NO 字段风格';
COMMENT ON COLUMN "part_definition"."part_name" IS '零件名称';
COMMENT ON COLUMN "part_definition"."part_type" IS '零件类型，例如缸筒、活塞杆、密封圈、位置传感器';
COMMENT ON COLUMN "part_definition"."drawing_code" IS '零件图号，借鉴 IQS 中 DRAWING_CODE 字段风格';
COMMENT ON COLUMN "part_definition"."source_type" IS '来源类型，例如自制、外购、外协';
COMMENT ON COLUMN "part_definition"."critical_level" IS '关键程度';
COMMENT ON COLUMN "part_definition"."created_at" IS '创建时间';
COMMENT ON COLUMN "part_definition"."updated_at" IS '更新时间';

COMMENT ON TABLE "component_instance" IS '具体起落架收放液压作动筒组件实例表，是质量追溯核心锚点';
COMMENT ON COLUMN "component_instance"."component_instance_id" IS '组件实例主键，算法 Anchor 表之一';
COMMENT ON COLUMN "component_instance"."component_type_id" IS '所属组件型号主键';
COMMENT ON COLUMN "component_instance"."installed_position_id" IS '当前装机位置主键';
COMMENT ON COLUMN "component_instance"."design_version_id" IS '装配适用设计版本主键';
COMMENT ON COLUMN "component_instance"."component_serial_no" IS '作动筒组件序列号';
COMMENT ON COLUMN "component_instance"."component_batch_no" IS '组件装配批次号';
COMMENT ON COLUMN "component_instance"."quality_code" IS '组件质量编号，借鉴 IQS 中 QUALITY_CODE 字段风格';
COMMENT ON COLUMN "component_instance"."assembly_order_no" IS '装配工单编号';
COMMENT ON COLUMN "component_instance"."delivery_date" IS '交付日期，参考订单表 DELIVERY_DATE 字段语义';
COMMENT ON COLUMN "component_instance"."component_status" IS '组件状态';
COMMENT ON COLUMN "component_instance"."created_at" IS '创建时间';
COMMENT ON COLUMN "component_instance"."updated_at" IS '更新时间';

COMMENT ON TABLE "bom_item" IS '作动筒组件 BOM 项表，表达组件型号与零件定义的结构关系';
COMMENT ON COLUMN "bom_item"."bom_item_id" IS 'BOM 项主键';
COMMENT ON COLUMN "bom_item"."component_type_id" IS '所属组件型号主键';
COMMENT ON COLUMN "bom_item"."parent_bom_item_id" IS '父级 BOM 项主键，用于多层 BOM';
COMMENT ON COLUMN "bom_item"."part_definition_id" IS 'BOM 对应零件定义主键';
COMMENT ON COLUMN "bom_item"."bom_item_no" IS 'BOM 项号';
COMMENT ON COLUMN "bom_item"."part_no" IS '零件号冗余字段，便于卷宗查询和样本生成';
COMMENT ON COLUMN "bom_item"."quantity" IS '装配数量';
COMMENT ON COLUMN "bom_item"."unit" IS '计量单位';
COMMENT ON COLUMN "bom_item"."position_code" IS 'BOM 装配位置编码';
COMMENT ON COLUMN "bom_item"."effective_date" IS '生效日期';

COMMENT ON TABLE "process_route" IS '作动筒组件或零件制造装配检验所遵循的工艺路线表';
COMMENT ON COLUMN "process_route"."process_route_id" IS '工艺路线主键';
COMMENT ON COLUMN "process_route"."component_type_id" IS '适用组件型号主键';
COMMENT ON COLUMN "process_route"."part_definition_id" IS '适用零件定义主键';
COMMENT ON COLUMN "process_route"."design_version_id" IS '适用设计版本主键';
COMMENT ON COLUMN "process_route"."route_code" IS '工艺路线编码';
COMMENT ON COLUMN "process_route"."route_name" IS '工艺路线名称';
COMMENT ON COLUMN "process_route"."route_type" IS '路线类型，例如零件加工、组件装配、试验检验';
COMMENT ON COLUMN "process_route"."doc_version" IS '工艺文件版本，借鉴 IQS 中 DOC_VERSION 字段风格';
COMMENT ON COLUMN "process_route"."release_status" IS '发布状态';
COMMENT ON COLUMN "process_route"."created_at" IS '创建时间';
COMMENT ON COLUMN "process_route"."updated_at" IS '更新时间';

COMMENT ON TABLE "operation_definition" IS '标准工序定义表，借鉴真实 MES 工序表命名风格';
COMMENT ON COLUMN "operation_definition"."operation_definition_id" IS '工序定义主键';
COMMENT ON COLUMN "operation_definition"."process_route_id" IS '所属工艺路线主键';
COMMENT ON COLUMN "operation_definition"."operation_no" IS '工序编号，参考 IQS 中 OP_NO 字段语义';
COMMENT ON COLUMN "operation_definition"."work_no" IS '工序编码，参考 Sys_Worksort 中 workid 字段语义';
COMMENT ON COLUMN "operation_definition"."work_name" IS '工序名称，参考 Sys_Worksort 中 workname 字段语义';
COMMENT ON COLUMN "operation_definition"."work_num" IS '工序序号，参考 Sys_Worksort 中 worknum 字段语义';
COMMENT ON COLUMN "operation_definition"."standard_time" IS '标准耗时，参考 Sys_Worksort 中 usetime 字段语义';
COMMENT ON COLUMN "operation_definition"."required_equipment_type" IS '要求设备类型';

COMMENT ON TABLE "parameter_standard" IS '工序或工步对应的工艺参数标准表';
COMMENT ON COLUMN "parameter_standard"."parameter_standard_id" IS '工艺参数标准主键';
COMMENT ON COLUMN "parameter_standard"."operation_definition_id" IS '所属工序定义主键';
COMMENT ON COLUMN "parameter_standard"."parameter_code" IS '参数编码';
COMMENT ON COLUMN "parameter_standard"."parameter_name" IS '参数名称，例如泄漏量、试验压力、镀层厚度';
COMMENT ON COLUMN "parameter_standard"."parameter_unit" IS '参数单位';
COMMENT ON COLUMN "parameter_standard"."standard_value" IS '标准值';
COMMENT ON COLUMN "parameter_standard"."lower_limit" IS '下限值';
COMMENT ON COLUMN "parameter_standard"."upper_limit" IS '上限值';
COMMENT ON COLUMN "parameter_standard"."control_level" IS '控制等级';

COMMENT ON TABLE "inspection_spec" IS '检验规范表，规定作动筒组件、零件或工序的检验项目和判定标准';
COMMENT ON COLUMN "inspection_spec"."inspection_spec_id" IS '检验规范主键';
COMMENT ON COLUMN "inspection_spec"."operation_definition_id" IS '关联工序定义主键';
COMMENT ON COLUMN "inspection_spec"."component_type_id" IS '适用组件型号主键';
COMMENT ON COLUMN "inspection_spec"."part_definition_id" IS '适用零件定义主键';
COMMENT ON COLUMN "inspection_spec"."spec_code" IS '检验规范编码';
COMMENT ON COLUMN "inspection_spec"."spec_name" IS '检验规范名称';
COMMENT ON COLUMN "inspection_spec"."inspection_type" IS '检验类型，例如来料检验、过程检验、最终检验、复检';
COMMENT ON COLUMN "inspection_spec"."item_code" IS '检验项目编码';
COMMENT ON COLUMN "inspection_spec"."item_name" IS '检验项目名称';
COMMENT ON COLUMN "inspection_spec"."acceptance_criteria" IS '验收准则';

COMMENT ON TABLE "supplier" IS '供应商主数据表，用于采购来源和外购件质量追溯';
COMMENT ON COLUMN "supplier"."supplier_id" IS '供应商主键，算法 Anchor 表之一';
COMMENT ON COLUMN "supplier"."supplier_code" IS '供应商编码';
COMMENT ON COLUMN "supplier"."supplier_name" IS '供应商名称';
COMMENT ON COLUMN "supplier"."supplier_type" IS '供应商类型，例如材料、密封件、传感器、外协加工';
COMMENT ON COLUMN "supplier"."qualification_level" IS '供应商资质等级';
COMMENT ON COLUMN "supplier"."contact_person" IS '联系人';
COMMENT ON COLUMN "supplier"."phone" IS '联系电话';
COMMENT ON COLUMN "supplier"."address" IS '供应商地址';
COMMENT ON COLUMN "supplier"."enabled" IS '是否启用';

COMMENT ON TABLE "purchase_order" IS '采购订单表，记录外购件、原材料或外协服务采购业务';
COMMENT ON COLUMN "purchase_order"."purchase_order_id" IS '采购订单主键，算法 Anchor 表之一';
COMMENT ON COLUMN "purchase_order"."supplier_id" IS '供应商主键';
COMMENT ON COLUMN "purchase_order"."order_no" IS '采购订单编号，参考订单表 ORDER_NO 字段风格';
COMMENT ON COLUMN "purchase_order"."task_code" IS '采购任务编码，借鉴 IQS 中 TASK_CODE 字段风格';
COMMENT ON COLUMN "purchase_order"."order_date" IS '订单日期';
COMMENT ON COLUMN "purchase_order"."delivery_date" IS '计划交付日期，参考订单表 DELIVERY_DATE 字段语义';
COMMENT ON COLUMN "purchase_order"."order_status" IS '订单状态';
COMMENT ON COLUMN "purchase_order"."priority_level" IS '优先级，参考订单表 yxj 字段语义';
COMMENT ON COLUMN "purchase_order"."urgent_flag" IS '紧急标识，参考订单表 urgent_tab 字段语义';

COMMENT ON TABLE "purchase_order_line" IS '采购订单明细表，记录具体物料、数量和交期';
COMMENT ON COLUMN "purchase_order_line"."purchase_order_line_id" IS '采购订单行主键';
COMMENT ON COLUMN "purchase_order_line"."purchase_order_id" IS '所属采购订单主键';
COMMENT ON COLUMN "purchase_order_line"."material_id" IS '采购物料主键';
COMMENT ON COLUMN "purchase_order_line"."line_no" IS '订单行号，参考订单表 part_NUM 字段语义';
COMMENT ON COLUMN "purchase_order_line"."amount" IS '采购数量，参考订单表 AMOUNT 字段语义';
COMMENT ON COLUMN "purchase_order_line"."unit" IS '计量单位';
COMMENT ON COLUMN "purchase_order_line"."required_date" IS '需求日期';
COMMENT ON COLUMN "purchase_order_line"."delivery_date" IS '计划到货日期';
COMMENT ON COLUMN "purchase_order_line"."line_status" IS '订单行状态';

COMMENT ON TABLE "supplier_batch" IS '供应商批次表，用于外购件和材料来源追溯';
COMMENT ON COLUMN "supplier_batch"."supplier_batch_id" IS '供应商批次主键';
COMMENT ON COLUMN "supplier_batch"."purchase_order_line_id" IS '来源采购订单行主键';
COMMENT ON COLUMN "supplier_batch"."supplier_id" IS '供应商主键';
COMMENT ON COLUMN "supplier_batch"."material_id" IS '物料主键';
COMMENT ON COLUMN "supplier_batch"."supplier_batch_no" IS '供应商批次号';
COMMENT ON COLUMN "supplier_batch"."certificate_no" IS '合格证编号';
COMMENT ON COLUMN "supplier_batch"."manufacture_date" IS '供应商生产日期';
COMMENT ON COLUMN "supplier_batch"."expire_date" IS '有效期';
COMMENT ON COLUMN "supplier_batch"."batch_status" IS '供应商批次状态';

COMMENT ON TABLE "inventory_batch" IS '企业库存批次表，连接采购入库和生产领料';
COMMENT ON COLUMN "inventory_batch"."inventory_batch_id" IS '库存批次主键';
COMMENT ON COLUMN "inventory_batch"."supplier_batch_id" IS '对应供应商批次主键';
COMMENT ON COLUMN "inventory_batch"."material_id" IS '库存物料主键';
COMMENT ON COLUMN "inventory_batch"."inventory_batch_no" IS '企业库存批次号';
COMMENT ON COLUMN "inventory_batch"."warehouse_code" IS '仓库编码';
COMMENT ON COLUMN "inventory_batch"."location_code" IS '库位编码';
COMMENT ON COLUMN "inventory_batch"."amount" IS '库存数量';
COMMENT ON COLUMN "inventory_batch"."unit" IS '计量单位';
COMMENT ON COLUMN "inventory_batch"."batch_status" IS '库存批次状态';

COMMENT ON TABLE "receiving_record" IS '物料到货入库记录表';
COMMENT ON COLUMN "receiving_record"."receiving_record_id" IS '入库记录主键';
COMMENT ON COLUMN "receiving_record"."inventory_batch_id" IS '形成的库存批次主键';
COMMENT ON COLUMN "receiving_record"."purchase_order_line_id" IS '来源采购订单行主键';
COMMENT ON COLUMN "receiving_record"."receiving_no" IS '入库单号';
COMMENT ON COLUMN "receiving_record"."receiving_date" IS '入库时间';
COMMENT ON COLUMN "receiving_record"."received_amount" IS '入库数量';
COMMENT ON COLUMN "receiving_record"."receiver_name" IS '入库人员名称';
COMMENT ON COLUMN "receiving_record"."inspection_required" IS '是否需要来料检验';
COMMENT ON COLUMN "receiving_record"."receiving_status" IS '入库状态';

COMMENT ON TABLE "issue_record" IS '生产或装配工单从库存领用物料的记录表';
COMMENT ON COLUMN "issue_record"."issue_record_id" IS '领料记录主键';
COMMENT ON COLUMN "issue_record"."inventory_batch_id" IS '领用库存批次主键';
COMMENT ON COLUMN "issue_record"."work_order_id" IS '领料对应工单主键';
COMMENT ON COLUMN "issue_record"."issue_no" IS '领料单号';
COMMENT ON COLUMN "issue_record"."issue_date" IS '领料时间';
COMMENT ON COLUMN "issue_record"."issued_amount" IS '领料数量';
COMMENT ON COLUMN "issue_record"."issue_person_name" IS '领料人员名称';
COMMENT ON COLUMN "issue_record"."issue_purpose" IS '领料用途';
COMMENT ON COLUMN "issue_record"."issue_status" IS '领料状态';

COMMENT ON TABLE "work_order" IS '生产工单、装配工单和维修工单的统一工单表';
COMMENT ON COLUMN "work_order"."work_order_id" IS '工单主键，算法 Anchor 表之一';
COMMENT ON COLUMN "work_order"."component_instance_id" IS '关联组件实例主键';
COMMENT ON COLUMN "work_order"."part_definition_id" IS '关联零件定义主键';
COMMENT ON COLUMN "work_order"."process_route_id" IS '执行工艺路线主键';
COMMENT ON COLUMN "work_order"."order_no" IS '工单编号，参考订单表 ORDER_NO 字段风格';
COMMENT ON COLUMN "work_order"."task_code" IS '任务编码，借鉴 IQS 中 TASK_CODE 字段风格';
COMMENT ON COLUMN "work_order"."work_order_type" IS '工单类型，可区分生产、装配、维修';
COMMENT ON COLUMN "work_order"."order_name" IS '工单名称或任务名称，参考 Sys_Task 中 taskname 字段语义';
COMMENT ON COLUMN "work_order"."amount" IS '计划数量，参考订单表 AMOUNT 字段语义';
COMMENT ON COLUMN "work_order"."priority_level" IS '优先级，参考订单表 yxj 字段语义';
COMMENT ON COLUMN "work_order"."urgent_flag" IS '紧急标识，参考订单表 urgent_tab 字段语义';
COMMENT ON COLUMN "work_order"."plan_start_time" IS '计划开始时间，参考 Sys_Task 中 plandate 字段语义';
COMMENT ON COLUMN "work_order"."plan_finish_time" IS '计划完成时间，参考订单表 DELIVERY_DATE 字段语义';
COMMENT ON COLUMN "work_order"."report_person_name" IS '提交人，参考 Sys_Task 中 reportman 字段语义';
COMMENT ON COLUMN "work_order"."report_time" IS '提交时间，参考 Sys_Task 中 reportdate 字段语义';
COMMENT ON COLUMN "work_order"."order_status" IS '工单状态，参考任务状态 rwstate 和 DONE 字段语义';

COMMENT ON TABLE "production_batch" IS '自制零件或装配过程形成的生产批次表';
COMMENT ON COLUMN "production_batch"."production_batch_id" IS '生产批次主键';
COMMENT ON COLUMN "production_batch"."work_order_id" IS '来源生产工单主键';
COMMENT ON COLUMN "production_batch"."part_definition_id" IS '批次对应零件定义主键';
COMMENT ON COLUMN "production_batch"."production_batch_no" IS '生产批次号';
COMMENT ON COLUMN "production_batch"."batch_amount" IS '批次数量';
COMMENT ON COLUMN "production_batch"."start_time" IS '批次开始时间';
COMMENT ON COLUMN "production_batch"."finish_time" IS '批次完成时间';
COMMENT ON COLUMN "production_batch"."batch_status" IS '批次状态';
COMMENT ON COLUMN "production_batch"."quality_code" IS '批次质量编号，借鉴 IQS 中 QUALITY_CODE 字段风格';

COMMENT ON TABLE "part_instance" IS '具体零件个体表，可反查生产批次、库存批次或供应商批次';
COMMENT ON COLUMN "part_instance"."part_instance_id" IS '零件实例主键，算法 Anchor 表之一';
COMMENT ON COLUMN "part_instance"."part_definition_id" IS '所属零件定义主键';
COMMENT ON COLUMN "part_instance"."production_batch_id" IS '自制来源生产批次主键';
COMMENT ON COLUMN "part_instance"."inventory_batch_id" IS '外购来源库存批次主键';
COMMENT ON COLUMN "part_instance"."supplier_batch_id" IS '外购来源供应商批次主键';
COMMENT ON COLUMN "part_instance"."part_serial_no" IS '零件序列号';
COMMENT ON COLUMN "part_instance"."part_lot_no" IS '零件批次号';
COMMENT ON COLUMN "part_instance"."piece_no" IS '件号，借鉴 IQS 中 PIECE_NO 字段风格';
COMMENT ON COLUMN "part_instance"."quality_code" IS '零件质量编号，借鉴 IQS 中 QUALITY_CODE 字段风格';
COMMENT ON COLUMN "part_instance"."source_type" IS '来源类型，例如自制、外购、外协';
COMMENT ON COLUMN "part_instance"."part_status" IS '零件状态';

COMMENT ON TABLE "component_part_installation" IS '组件实例与零件实例的装配关系表，支持拆换前后历史追溯';
COMMENT ON COLUMN "component_part_installation"."installation_id" IS '组件零件装配关系主键';
COMMENT ON COLUMN "component_part_installation"."component_instance_id" IS '组件实例主键';
COMMENT ON COLUMN "component_part_installation"."part_instance_id" IS '零件实例主键';
COMMENT ON COLUMN "component_part_installation"."bom_item_id" IS '对应 BOM 项主键';
COMMENT ON COLUMN "component_part_installation"."work_order_id" IS '装配工单主键';
COMMENT ON COLUMN "component_part_installation"."installed_position_id" IS '装配位置主键';
COMMENT ON COLUMN "component_part_installation"."replacement_record_id" IS '关联拆换记录主键，用于 MRO 拆换闭环';
COMMENT ON COLUMN "component_part_installation"."install_time" IS '装配时间';
COMMENT ON COLUMN "component_part_installation"."valid_from" IS '装配关系有效开始时间，通常与装配完成或检验放行时间一致';
COMMENT ON COLUMN "component_part_installation"."valid_to" IS '装配关系有效结束时间，拆下或替换后写入';
COMMENT ON COLUMN "component_part_installation"."uninstall_time" IS '拆下时间，用于服役维修拆换追溯';
COMMENT ON COLUMN "component_part_installation"."uninstall_reason" IS '拆下原因，例如维修更换、故障拆检、寿命到期';
COMMENT ON COLUMN "component_part_installation"."install_person_name" IS '装配人员名称';
COMMENT ON COLUMN "component_part_installation"."install_status" IS '装配状态';

COMMENT ON TABLE "workstation" IS '生产线或车间作业工位表';
COMMENT ON COLUMN "workstation"."workstation_id" IS '工位主键';
COMMENT ON COLUMN "workstation"."station_no" IS '工位编号，参考 Sys_Station 中 stationid 字段语义';
COMMENT ON COLUMN "workstation"."station_name" IS '工位名称，参考 Sys_Station 中 stationname 字段语义';
COMMENT ON COLUMN "workstation"."workshop" IS '所属车间';
COMMENT ON COLUMN "workstation"."person_count" IS '人员数量，参考 Sys_Station 中 percount 字段语义';
COMMENT ON COLUMN "workstation"."equipment_count" IS '设备数量，参考 Sys_Station 中 equcount 字段语义';
COMMENT ON COLUMN "workstation"."station_status" IS '工位状态';

COMMENT ON TABLE "equipment" IS '加工、装配、检测或维修设备表，借鉴真实 MES 设备字段风格';
COMMENT ON COLUMN "equipment"."equipment_id" IS '设备主键';
COMMENT ON COLUMN "equipment"."workstation_id" IS '所属工位主键';
COMMENT ON COLUMN "equipment"."equipment_no" IS '设备编号，参考设备表 sbbh 字段语义';
COMMENT ON COLUMN "equipment"."equipment_name" IS '设备名称，参考 equipmentname 字段语义';
COMMENT ON COLUMN "equipment"."equipment_num" IS '设备序号，参考 equipmentnum 字段语义';
COMMENT ON COLUMN "equipment"."equipment_type" IS '设备类型';
COMMENT ON COLUMN "equipment"."status_code" IS '设备状态，规范化参考 zhuangtai 字段';
COMMENT ON COLUMN "equipment"."color_flag" IS '颜色标识，参考 bgcolor 或 color 字段语义';
COMMENT ON COLUMN "equipment"."created_at" IS '创建时间，参考 indate 字段语义';

COMMENT ON TABLE "personnel" IS '操作员、检验员、维修人员和责任人的统一人员表';
COMMENT ON COLUMN "personnel"."personnel_id" IS '人员主键';
COMMENT ON COLUMN "personnel"."person_no" IS '员工编号，参考 personnum 字段语义';
COMMENT ON COLUMN "personnel"."person_name" IS '员工姓名，参考 personname 字段语义';
COMMENT ON COLUMN "personnel"."workshop" IS '所属车间，参考 workshop 字段语义';
COMMENT ON COLUMN "personnel"."phone" IS '联系电话，参考 phone 字段语义';
COMMENT ON COLUMN "personnel"."skill_level" IS '技能等级，规范化参考 jndj 字段';
COMMENT ON COLUMN "personnel"."work_type" IS '工种，规范化参考 gz 字段';
COMMENT ON COLUMN "personnel"."dept_code" IS '所属部门编码';
COMMENT ON COLUMN "personnel"."dept_name" IS '所属部门名称';

COMMENT ON TABLE "tooling" IS '夹具、模具、工具、专用工装等辅助资源表';
COMMENT ON COLUMN "tooling"."tooling_id" IS '工装主键';
COMMENT ON COLUMN "tooling"."tooling_no" IS '工装编号';
COMMENT ON COLUMN "tooling"."tooling_name" IS '工装名称';
COMMENT ON COLUMN "tooling"."tooling_type" IS '工装类型';
COMMENT ON COLUMN "tooling"."calibration_due_date" IS '校准到期日期';
COMMENT ON COLUMN "tooling"."tooling_status" IS '工装状态';

COMMENT ON TABLE "operation_execution" IS '工单下某道工序的实际执行记录表';
COMMENT ON COLUMN "operation_execution"."operation_execution_id" IS '工序执行主键，算法 Beneficiary 表之一';
COMMENT ON COLUMN "operation_execution"."work_order_id" IS '所属工单主键';
COMMENT ON COLUMN "operation_execution"."operation_definition_id" IS '对应工序定义主键';
COMMENT ON COLUMN "operation_execution"."component_instance_id" IS '关联组件实例主键';
COMMENT ON COLUMN "operation_execution"."part_instance_id" IS '关联零件实例主键';
COMMENT ON COLUMN "operation_execution"."workstation_id" IS '执行工位主键';
COMMENT ON COLUMN "operation_execution"."equipment_id" IS '主要执行设备主键';
COMMENT ON COLUMN "operation_execution"."op_no" IS '执行工序编号，借鉴 IQS 中 OP_NO 字段风格';
COMMENT ON COLUMN "operation_execution"."work_no" IS '执行工序编码，参考 workid 字段语义';
COMMENT ON COLUMN "operation_execution"."work_name" IS '执行工序名称，参考 workname 字段语义';
COMMENT ON COLUMN "operation_execution"."start_time" IS '实际开始时间';
COMMENT ON COLUMN "operation_execution"."finish_time" IS '实际完成时间';
COMMENT ON COLUMN "operation_execution"."execution_status" IS '执行状态';

COMMENT ON TABLE "step_execution" IS '工序下更细粒度的工步实际执行记录表';
COMMENT ON COLUMN "step_execution"."step_execution_id" IS '工步执行主键，算法 Beneficiary 表之一';
COMMENT ON COLUMN "step_execution"."operation_execution_id" IS '所属工序执行主键';
COMMENT ON COLUMN "step_execution"."step_no" IS '工步编号，参考 stepsid 字段语义';
COMMENT ON COLUMN "step_execution"."step_name" IS '工步名称，参考 stepsname 字段语义';
COMMENT ON COLUMN "step_execution"."step_sequence" IS '工步顺序';
COMMENT ON COLUMN "step_execution"."start_time" IS '工步开始时间';
COMMENT ON COLUMN "step_execution"."finish_time" IS '工步完成时间';
COMMENT ON COLUMN "step_execution"."step_status" IS '工步状态';
COMMENT ON COLUMN "step_execution"."operator_name" IS '操作人员名称';

COMMENT ON TABLE "resource_usage" IS '工序或工步执行过程中使用设备、人员、工位、工装的记录表';
COMMENT ON COLUMN "resource_usage"."resource_usage_id" IS '资源使用主键，算法 Beneficiary 表之一';
COMMENT ON COLUMN "resource_usage"."operation_execution_id" IS '所属工序执行主键';
COMMENT ON COLUMN "resource_usage"."step_execution_id" IS '所属工步执行主键';
COMMENT ON COLUMN "resource_usage"."equipment_id" IS '使用设备主键';
COMMENT ON COLUMN "resource_usage"."personnel_id" IS '使用人员主键';
COMMENT ON COLUMN "resource_usage"."workstation_id" IS '使用工位主键';
COMMENT ON COLUMN "resource_usage"."tooling_id" IS '使用工装主键';
COMMENT ON COLUMN "resource_usage"."usage_start_time" IS '资源使用开始时间';
COMMENT ON COLUMN "resource_usage"."usage_finish_time" IS '资源使用结束时间';
COMMENT ON COLUMN "resource_usage"."mastery" IS '人员对设备熟练度，参考 sys_equandper 中 mastery 字段语义';
COMMENT ON COLUMN "resource_usage"."usage_status" IS '资源使用状态';

COMMENT ON TABLE "process_parameter_record" IS '设备、工序或工步执行过程中采集的过程参数记录表';
COMMENT ON COLUMN "process_parameter_record"."process_parameter_record_id" IS '过程参数记录主键，算法 Beneficiary 表之一';
COMMENT ON COLUMN "process_parameter_record"."operation_execution_id" IS '所属工序执行主键';
COMMENT ON COLUMN "process_parameter_record"."step_execution_id" IS '所属工步执行主键';
COMMENT ON COLUMN "process_parameter_record"."parameter_standard_id" IS '对应参数标准主键';
COMMENT ON COLUMN "process_parameter_record"."equipment_id" IS '采集设备主键';
COMMENT ON COLUMN "process_parameter_record"."parameter_code" IS '参数编码';
COMMENT ON COLUMN "process_parameter_record"."parameter_name" IS '参数名称';
COMMENT ON COLUMN "process_parameter_record"."measured_value" IS '实测值';
COMMENT ON COLUMN "process_parameter_record"."standard_value" IS '标准值';
COMMENT ON COLUMN "process_parameter_record"."lower_limit" IS '下限值';
COMMENT ON COLUMN "process_parameter_record"."upper_limit" IS '上限值';
COMMENT ON COLUMN "process_parameter_record"."parameter_unit" IS '参数单位';
COMMENT ON COLUMN "process_parameter_record"."collect_time" IS '采集时间';
COMMENT ON COLUMN "process_parameter_record"."judge_result" IS '判定结果';

COMMENT ON TABLE "inspection_record" IS '一次检验活动记录表，支持来料检验、过程检验、最终检验和复检';
COMMENT ON COLUMN "inspection_record"."inspection_record_id" IS '检验记录主键，质量追溯 Anchor 表之一';
COMMENT ON COLUMN "inspection_record"."inspection_spec_id" IS '对应检验规范主键';
COMMENT ON COLUMN "inspection_record"."component_instance_id" IS '受检组件实例主键';
COMMENT ON COLUMN "inspection_record"."part_instance_id" IS '受检零件实例主键';
COMMENT ON COLUMN "inspection_record"."operation_execution_id" IS '关联工序执行主键';
COMMENT ON COLUMN "inspection_record"."inventory_batch_id" IS '来料检验关联库存批次主键';
COMMENT ON COLUMN "inspection_record"."work_order_id" IS '关联工单主键';
COMMENT ON COLUMN "inspection_record"."inspector_id" IS '检验员主键';
COMMENT ON COLUMN "inspection_record"."inspection_no" IS '检验记录编号';
COMMENT ON COLUMN "inspection_record"."inspection_type" IS '检验类型，例如来料、过程、最终、复检';
COMMENT ON COLUMN "inspection_record"."find_date" IS '发现或检验时间，借鉴 IQS 中 FIND_DATE 字段风格';
COMMENT ON COLUMN "inspection_record"."find_type" IS '发现方式，借鉴 IQS 中 FIND_TYPE 字段风格';
COMMENT ON COLUMN "inspection_record"."inspection_result" IS '检验结论';

COMMENT ON TABLE "inspection_item_result" IS '检验记录下具体检验项目的检测结果表';
COMMENT ON COLUMN "inspection_item_result"."inspection_item_result_id" IS '检验项目结果主键，算法 Beneficiary 表之一';
COMMENT ON COLUMN "inspection_item_result"."inspection_record_id" IS '所属检验记录主键';
COMMENT ON COLUMN "inspection_item_result"."inspection_spec_id" IS '对应检验规范主键';
COMMENT ON COLUMN "inspection_item_result"."item_code" IS '检验项目编码';
COMMENT ON COLUMN "inspection_item_result"."item_name" IS '检验项目名称';
COMMENT ON COLUMN "inspection_item_result"."measured_value" IS '检测值，可能包含数值或文本';
COMMENT ON COLUMN "inspection_item_result"."standard_value" IS '标准值';
COMMENT ON COLUMN "inspection_item_result"."lower_limit" IS '下限';
COMMENT ON COLUMN "inspection_item_result"."upper_limit" IS '上限';
COMMENT ON COLUMN "inspection_item_result"."unit" IS '计量单位';
COMMENT ON COLUMN "inspection_item_result"."judge_result" IS '判定结果';

COMMENT ON TABLE "defect_record" IS '检验或过程发现的缺陷记录表';
COMMENT ON COLUMN "defect_record"."defect_record_id" IS '缺陷记录主键';
COMMENT ON COLUMN "defect_record"."inspection_record_id" IS '来源检验记录主键';
COMMENT ON COLUMN "defect_record"."component_instance_id" IS '关联组件实例主键';
COMMENT ON COLUMN "defect_record"."part_instance_id" IS '关联零件实例主键';
COMMENT ON COLUMN "defect_record"."operation_execution_id" IS '关联工序执行主键';
COMMENT ON COLUMN "defect_record"."defect_code" IS '缺陷代码';
COMMENT ON COLUMN "defect_record"."defect_name" IS '缺陷名称';
COMMENT ON COLUMN "defect_record"."defect_position" IS '缺陷部位';
COMMENT ON COLUMN "defect_record"."severity_level" IS '严重程度';
COMMENT ON COLUMN "defect_record"."failure_desc" IS '不合格情况描述，借鉴 IQS 中 FAILURE_DESC 字段风格';
COMMENT ON COLUMN "defect_record"."find_dept_code" IS '发现单位编码，借鉴 IQS 中 FIND_DEPT_CODE 字段风格';
COMMENT ON COLUMN "defect_record"."find_dept_name" IS '发现单位名称，借鉴 IQS 中 FIND_DEPT_NAME 字段风格';

COMMENT ON TABLE "maintenance_event" IS '作动筒组件装机后发生的维修、保养、排故或服务事件表';
COMMENT ON COLUMN "maintenance_event"."maintenance_event_id" IS '维修事件主键，算法 Anchor 表之一';
COMMENT ON COLUMN "maintenance_event"."component_instance_id" IS '维修涉及组件实例主键';
COMMENT ON COLUMN "maintenance_event"."installed_position_id" IS '维修发生装机位置主键';
COMMENT ON COLUMN "maintenance_event"."event_no" IS '维修事件编号';
COMMENT ON COLUMN "maintenance_event"."service_date" IS '服役或维修发生时间';
COMMENT ON COLUMN "maintenance_event"."event_type" IS '事件类型，例如维修、保养、排故、拆换';
COMMENT ON COLUMN "maintenance_event"."service_unit_code" IS '维修单位编码';
COMMENT ON COLUMN "maintenance_event"."service_unit_name" IS '维修单位名称';
COMMENT ON COLUMN "maintenance_event"."event_status" IS '维修事件状态';

COMMENT ON TABLE "quality_event" IS '质量事件统一主表，表示异常入口；不合格通知单、参数异常和服役反馈均可由该表发起追溯';
COMMENT ON COLUMN "quality_event"."quality_event_id" IS '质量事件主键，作为异常入口和追溯查询起点';
COMMENT ON COLUMN "quality_event"."event_no" IS '质量事件编号，用于唯一标识一次异常事件';
COMMENT ON COLUMN "quality_event"."event_source" IS '事件来源，例如 inspection、process_parameter、mro、iqs、manual';
COMMENT ON COLUMN "quality_event"."event_type" IS '事件类型，例如不合格、缺陷、参数异常、服役故障';
COMMENT ON COLUMN "quality_event"."event_title" IS '质量事件标题';
COMMENT ON COLUMN "quality_event"."event_desc" IS '质量事件描述';
COMMENT ON COLUMN "quality_event"."event_time" IS '事件发生或发现时间';
COMMENT ON COLUMN "quality_event"."severity_level" IS '严重程度';
COMMENT ON COLUMN "quality_event"."event_status" IS '事件状态，例如新建、处理中、已关闭';
COMMENT ON COLUMN "quality_event"."trigger_source_no" IS '触发来源业务编号，例如检验单号、参数记录号、不合格通知单号或维修事件编号';
COMMENT ON COLUMN "quality_event"."created_at" IS '创建时间';
COMMENT ON COLUMN "quality_event"."updated_at" IS '更新时间';

COMMENT ON TABLE "iqs_failure" IS 'IQS 不合格通知单业务单据表，参考 zong.docx 中 IQS_FAILURE 真实字段风格；本表对象外键为单据级快捷追溯字段，多对象影响以 quality_event_subject 为准';
COMMENT ON COLUMN "iqs_failure"."iqs_failure_id" IS '不合格通知单主键，算法 Anchor 表之一';
COMMENT ON COLUMN "iqs_failure"."quality_event_id" IS '来源质量事件主键，不合格通知单由质量事件形成';
COMMENT ON COLUMN "iqs_failure"."inspection_record_id" IS '触发该不合格的检验记录主键';
COMMENT ON COLUMN "iqs_failure"."defect_record_id" IS '触发该不合格的缺陷记录主键';
COMMENT ON COLUMN "iqs_failure"."component_instance_id" IS '涉及组件实例主键';
COMMENT ON COLUMN "iqs_failure"."part_instance_id" IS '涉及零件实例主键';
COMMENT ON COLUMN "iqs_failure"."work_order_id" IS '涉及工单主键';
COMMENT ON COLUMN "iqs_failure"."operation_execution_id" IS '涉及工序执行主键';
COMMENT ON COLUMN "iqs_failure"."supplier_id" IS '涉及供应商主键';
COMMENT ON COLUMN "iqs_failure"."maintenance_event_id" IS '来源维修事件主键';
COMMENT ON COLUMN "iqs_failure"."doc_source" IS '不合格来源代码，借鉴 IQS 中 DOC_SOURCE 字段风格';
COMMENT ON COLUMN "iqs_failure"."failure_type" IS '不合格品类别代码，借鉴 IQS 中 FAILURE_TYPE 字段风格';
COMMENT ON COLUMN "iqs_failure"."quality_code" IS '质量编号，借鉴 IQS 中 QUALITY_CODE 字段风格';
COMMENT ON COLUMN "iqs_failure"."iqs_failure_order" IS '不合格通知单编号，借鉴 IQS_FAILURE_ORDER 字段风格';
COMMENT ON COLUMN "iqs_failure"."task_code" IS '任务编号，借鉴 IQS 中 TASK_CODE 字段风格';
COMMENT ON COLUMN "iqs_failure"."product_code" IS '机型编码，借鉴 IQS 中 PRODUCT_CODE 字段风格';
COMMENT ON COLUMN "iqs_failure"."product_name" IS '机型名称，借鉴 IQS 中 PRODUCT_NAME 字段风格';
COMMENT ON COLUMN "iqs_failure"."piece_no" IS '件号，借鉴 IQS 中 PIECE_NO 字段风格';
COMMENT ON COLUMN "iqs_failure"."op_no" IS '发现工序编号，借鉴 IQS 中 OP_NO 字段风格';
COMMENT ON COLUMN "iqs_failure"."find_dept_code" IS '发现单位编码，借鉴 IQS 中 FIND_DEPT_CODE 字段风格';
COMMENT ON COLUMN "iqs_failure"."find_dept_name" IS '发现单位名称，借鉴 IQS 中 FIND_DEPT_NAME 字段风格';
COMMENT ON COLUMN "iqs_failure"."doc_status" IS '归零状态或单据状态，借鉴 IQS 中 DOC_STATUS 和 BILL_STATUS 字段风格';
COMMENT ON COLUMN "iqs_failure"."secret_level" IS '密级代码，借鉴 IQS 中 SECRET_LEVEL 字段风格';
COMMENT ON COLUMN "iqs_failure"."start_time" IS '流程开始时间，借鉴 IQS 中 STARTTIME 字段风格';
COMMENT ON COLUMN "iqs_failure"."finish_time" IS '流程结束时间，借鉴 IQS 中 FINISHTIME 字段风格';

COMMENT ON TABLE "iqs_failure_content" IS '不合格通知单内容表，记录问题描述、发现方式、责任单位和实际处理情况';
COMMENT ON COLUMN "iqs_failure_content"."iqs_failure_content_id" IS '不合格通知单内容主键';
COMMENT ON COLUMN "iqs_failure_content"."iqs_failure_id" IS '所属不合格通知单主键';
COMMENT ON COLUMN "iqs_failure_content"."start_piece_no" IS '起始件号，借鉴 IQS_FAILURE_CONTENT 中 START_PIECE_NO 字段风格';
COMMENT ON COLUMN "iqs_failure_content"."end_piece_no" IS '终止件号，借鉴 IQS_FAILURE_CONTENT 中 END_PIECE_NO 字段风格';
COMMENT ON COLUMN "iqs_failure_content"."failure_desc" IS '不合格情况描述，借鉴 IQS 中 FAILURE_DESC 字段风格';
COMMENT ON COLUMN "iqs_failure_content"."failure_place_brief" IS '故障部位简要说明，借鉴 IQS 中 FAILURE_PLACE_BRIEF 字段风格';
COMMENT ON COLUMN "iqs_failure_content"."find_type" IS '发现方式，借鉴 IQS 中 FIND_TYPE 字段风格';
COMMENT ON COLUMN "iqs_failure_content"."find_date" IS '发现时间，借鉴 IQS 中 FIND_DATE 字段风格';
COMMENT ON COLUMN "iqs_failure_content"."finder_type" IS '发现人类别，借鉴 IQS 中 FINDER_TYPE 字段风格';
COMMENT ON COLUMN "iqs_failure_content"."find_user_code" IS '发现人编号，借鉴 IQS 中 FIND_USER_CODE 字段风格';
COMMENT ON COLUMN "iqs_failure_content"."find_user_name" IS '发现人姓名';
COMMENT ON COLUMN "iqs_failure_content"."duty_worksec_code" IS '责任工段编码，借鉴 IQS 中 DUTY_WORKSEC_CODE 字段风格';
COMMENT ON COLUMN "iqs_failure_content"."duty_worksec_name" IS '责任工段名称，借鉴 IQS 中 DUTY_WORKSEC_NAME 字段风格';
COMMENT ON COLUMN "iqs_failure_content"."duty_dept_code" IS '责任单位编码，借鉴 IQS 中 DUTY_DEPT_CODE 字段风格';
COMMENT ON COLUMN "iqs_failure_content"."duty_dept_name" IS '责任单位名称，借鉴 IQS 中 DUTY_DEPT_NAME 字段风格';
COMMENT ON COLUMN "iqs_failure_content"."actual_method" IS '实际处理情况，借鉴 IQS 中 ACTUAL_METHOD 字段风格';
COMMENT ON COLUMN "iqs_failure_content"."doc_no" IS '衍生单据号，借鉴 IQS 中 DOC_NO 字段风格';

COMMENT ON TABLE "iqs_failure_duty" IS '不合格责任分配表，参考 zong.docx 中 IQS_FAILURE_DUTY 真实字段风格';
COMMENT ON COLUMN "iqs_failure_duty"."iqs_failure_duty_id" IS '不合格责任分配主键';
COMMENT ON COLUMN "iqs_failure_duty"."iqs_failure_id" IS '所属不合格通知单主键';
COMMENT ON COLUMN "iqs_failure_duty"."iqs_failure_content_id" IS '关联不合格内容主键';
COMMENT ON COLUMN "iqs_failure_duty"."duty_personnel_id" IS '责任人员主键';
COMMENT ON COLUMN "iqs_failure_duty"."supplier_id" IS '责任供应商主键';
COMMENT ON COLUMN "iqs_failure_duty"."duty_dept_code" IS '责任单位编码，借鉴 IQS_FAILURE_DUTY 中 DUTY_DEPT_CODE 字段风格';
COMMENT ON COLUMN "iqs_failure_duty"."duty_dept_name" IS '责任单位名称，借鉴 IQS_FAILURE_DUTY 中 DUTY_DEPT_NAME 字段风格';
COMMENT ON COLUMN "iqs_failure_duty"."duty_user_code" IS '责任者编码，借鉴 IQS_FAILURE_DUTY 中 DUTY_USER_CODE 字段风格';
COMMENT ON COLUMN "iqs_failure_duty"."duty_user_name" IS '责任者姓名，借鉴 IQS_FAILURE_DUTY 中 DUTY_USER_NAME 字段风格';
COMMENT ON COLUMN "iqs_failure_duty"."duty_person_ratio" IS '责任人百分比，借鉴 IQS_FAILURE_DUTY 中 DUTY_PERSON_RATIO 字段风格';
COMMENT ON COLUMN "iqs_failure_duty"."responsibility_date" IS '责任认定时间';

COMMENT ON TABLE "disposition_record" IS '质量事件或不合格通知单的处置记录表，可覆盖参数异常和检验不合格后的处置闭环';
COMMENT ON COLUMN "disposition_record"."disposition_record_id" IS '处置记录主键，算法 Beneficiary 表之一';
COMMENT ON COLUMN "disposition_record"."quality_event_id" IS '关联质量事件主键，作为处置记录的规范事件入口';
COMMENT ON COLUMN "disposition_record"."iqs_failure_id" IS '关联不合格通知单主键；无不合格单的参数异常处置可为空';
COMMENT ON COLUMN "disposition_record"."iqs_failure_content_id" IS '关联不合格内容主键';
COMMENT ON COLUMN "disposition_record"."work_order_id" IS '处置关联工单主键';
COMMENT ON COLUMN "disposition_record"."responsible_personnel_id" IS '处置责任人员主键';
COMMENT ON COLUMN "disposition_record"."disposition_no" IS '处置单号';
COMMENT ON COLUMN "disposition_record"."deal_type_find" IS '发现单位处置方式，借鉴 IQS 中 DEAL_TYPE_FIND 字段风格';
COMMENT ON COLUMN "disposition_record"."deal_type_pass" IS '检验组处置方式，借鉴 IQS 中 DEAL_TYPE_PASS 字段风格';
COMMENT ON COLUMN "disposition_record"."deal_type_tech" IS '技术人员处置方式，借鉴 IQS 中 DEAL_TYPE_TECH 字段风格';
COMMENT ON COLUMN "disposition_record"."disposition_type" IS '处置类型，例如返工、返修、报废、让步接收、退货';
COMMENT ON COLUMN "disposition_record"."disposition_desc" IS '处置说明';
COMMENT ON COLUMN "disposition_record"."disposition_date" IS '处置时间';
COMMENT ON COLUMN "disposition_record"."disposition_status" IS '处置状态';

COMMENT ON TABLE "reinspection_record" IS '处置后的再次检验记录表';
COMMENT ON COLUMN "reinspection_record"."reinspection_record_id" IS '复检记录主键';
COMMENT ON COLUMN "reinspection_record"."disposition_record_id" IS '来源处置记录主键';
COMMENT ON COLUMN "reinspection_record"."inspection_record_id" IS '对应复检检验记录主键';
COMMENT ON COLUMN "reinspection_record"."inspector_id" IS '复检人员主键';
COMMENT ON COLUMN "reinspection_record"."reinspection_no" IS '复检编号';
COMMENT ON COLUMN "reinspection_record"."reinspection_date" IS '复检时间';
COMMENT ON COLUMN "reinspection_record"."reinspection_result" IS '复检结论';
COMMENT ON COLUMN "reinspection_record"."conclusion_desc" IS '复检结论说明';

COMMENT ON TABLE "maintenance_order" IS '维修事件触发的维修工单表';
COMMENT ON COLUMN "maintenance_order"."maintenance_order_id" IS '维修工单主键';
COMMENT ON COLUMN "maintenance_order"."maintenance_event_id" IS '所属维修事件主键';
COMMENT ON COLUMN "maintenance_order"."component_instance_id" IS '维修组件实例主键';
COMMENT ON COLUMN "maintenance_order"."work_order_id" IS '关联统一工单主键';
COMMENT ON COLUMN "maintenance_order"."maintenance_order_no" IS '维修工单编号';
COMMENT ON COLUMN "maintenance_order"."task_name" IS '维修任务名称，参考 Sys_Task 中 taskname 字段语义';
COMMENT ON COLUMN "maintenance_order"."plan_start_time" IS '计划开始时间';
COMMENT ON COLUMN "maintenance_order"."plan_finish_time" IS '计划完成时间';
COMMENT ON COLUMN "maintenance_order"."order_status" IS '维修工单状态';
COMMENT ON COLUMN "maintenance_order"."report_person_name" IS '提交人';

COMMENT ON TABLE "fault_record" IS '服役维修阶段发现的故障现象记录表';
COMMENT ON COLUMN "fault_record"."fault_record_id" IS '故障记录主键';
COMMENT ON COLUMN "fault_record"."maintenance_event_id" IS '所属维修事件主键';
COMMENT ON COLUMN "fault_record"."component_instance_id" IS '故障涉及组件实例主键';
COMMENT ON COLUMN "fault_record"."fault_code" IS '故障代码';
COMMENT ON COLUMN "fault_record"."fault_name" IS '故障名称';
COMMENT ON COLUMN "fault_record"."fault_desc" IS '故障描述';
COMMENT ON COLUMN "fault_record"."fault_position" IS '故障部位';
COMMENT ON COLUMN "fault_record"."find_date" IS '故障发现时间';
COMMENT ON COLUMN "fault_record"."finder_name" IS '发现人姓名';
COMMENT ON COLUMN "fault_record"."severity_level" IS '严重程度';

COMMENT ON TABLE "replacement_record" IS '维修过程中拆下或换上作动筒零件的记录表';
COMMENT ON COLUMN "replacement_record"."replacement_record_id" IS '拆换记录主键，算法 Beneficiary 表之一';
COMMENT ON COLUMN "replacement_record"."maintenance_event_id" IS '所属维修事件主键';
COMMENT ON COLUMN "replacement_record"."maintenance_order_id" IS '所属维修工单主键';
COMMENT ON COLUMN "replacement_record"."component_instance_id" IS '拆换所在组件实例主键';
COMMENT ON COLUMN "replacement_record"."installed_position_id" IS '拆换发生装机位置主键';
COMMENT ON COLUMN "replacement_record"."removed_part_instance_id" IS '拆下零件实例主键';
COMMENT ON COLUMN "replacement_record"."installed_part_instance_id" IS '换上零件实例主键';
COMMENT ON COLUMN "replacement_record"."inventory_batch_id" IS '换上件来源库存批次主键';
COMMENT ON COLUMN "replacement_record"."replacement_no" IS '拆换记录编号';
COMMENT ON COLUMN "replacement_record"."replacement_reason" IS '拆换原因，例如内泄漏、外泄漏、活塞杆划伤';
COMMENT ON COLUMN "replacement_record"."replacement_time" IS '拆换时间';
COMMENT ON COLUMN "replacement_record"."operator_name" IS '拆换操作人姓名';
COMMENT ON COLUMN "replacement_record"."replacement_status" IS '拆换状态';

COMMENT ON TABLE "service_feedback" IS '用户、维修单位或外场反馈的作动筒组件质量信息表';
COMMENT ON COLUMN "service_feedback"."service_feedback_id" IS '服役反馈主键';
COMMENT ON COLUMN "service_feedback"."quality_event_id" IS '转化形成的质量事件主键';
COMMENT ON COLUMN "service_feedback"."maintenance_event_id" IS '关联维修事件主键';
COMMENT ON COLUMN "service_feedback"."component_instance_id" IS '反馈涉及组件实例主键';
COMMENT ON COLUMN "service_feedback"."iqs_failure_id" IS '转化形成的不合格通知单主键';
COMMENT ON COLUMN "service_feedback"."feedback_no" IS '服役反馈编号';
COMMENT ON COLUMN "service_feedback"."feedback_source" IS '反馈来源，例如用户、维修单位、外场';
COMMENT ON COLUMN "service_feedback"."feedback_date" IS '反馈时间';
COMMENT ON COLUMN "service_feedback"."feedback_desc" IS '反馈内容';
COMMENT ON COLUMN "service_feedback"."feedback_status" IS '反馈状态';

COMMENT ON TABLE "quality_event_subject" IS '质量事件与受影响业务对象的多对多关联表，用于表达一个事件影响多个零件、批次、工单或过程参数记录';
COMMENT ON COLUMN "quality_event_subject"."quality_event_subject_id" IS '质量事件关联对象主键';
COMMENT ON COLUMN "quality_event_subject"."quality_event_id" IS '所属质量事件主键';
COMMENT ON COLUMN "quality_event_subject"."component_instance_id" IS '关联组件实例主键';
COMMENT ON COLUMN "quality_event_subject"."part_instance_id" IS '关联零件实例主键';
COMMENT ON COLUMN "quality_event_subject"."production_batch_id" IS '关联生产批次主键';
COMMENT ON COLUMN "quality_event_subject"."inventory_batch_id" IS '关联库存批次主键';
COMMENT ON COLUMN "quality_event_subject"."supplier_batch_id" IS '关联供应商批次主键';
COMMENT ON COLUMN "quality_event_subject"."work_order_id" IS '关联工单主键';
COMMENT ON COLUMN "quality_event_subject"."operation_execution_id" IS '关联工序执行主键';
COMMENT ON COLUMN "quality_event_subject"."step_execution_id" IS '关联工步执行主键';
COMMENT ON COLUMN "quality_event_subject"."process_parameter_record_id" IS '直接触发或关联异常的过程参数记录主键';
COMMENT ON COLUMN "quality_event_subject"."inspection_record_id" IS '关联检验记录主键';
COMMENT ON COLUMN "quality_event_subject"."defect_record_id" IS '关联缺陷记录主键';
COMMENT ON COLUMN "quality_event_subject"."purchase_order_id" IS '关联采购订单主键';
COMMENT ON COLUMN "quality_event_subject"."supplier_id" IS '关联供应商主键';
COMMENT ON COLUMN "quality_event_subject"."maintenance_event_id" IS '关联维修事件主键';
COMMENT ON COLUMN "quality_event_subject"."replacement_record_id" IS '关联拆换记录主键';
COMMENT ON COLUMN "quality_event_subject"."service_feedback_id" IS '关联服役反馈主键';
COMMENT ON COLUMN "quality_event_subject"."subject_type" IS '关联对象类型，例如组件、零件、批次、工单、参数记录、维修事件';
COMMENT ON COLUMN "quality_event_subject"."subject_role" IS '对象在事件中的角色，例如触发对象、受影响对象、来源对象、处置对象';
COMMENT ON COLUMN "quality_event_subject"."impact_desc" IS '影响说明';
COMMENT ON COLUMN "quality_event_subject"."created_at" IS '创建时间';

-- Foreign keys
ALTER TABLE "landing_gear_system" ADD CONSTRAINT "fk_landing_gear_system_upper_equipment_id" FOREIGN KEY ("upper_equipment_id") REFERENCES "upper_equipment" ("upper_equipment_id");
ALTER TABLE "installed_position" ADD CONSTRAINT "fk_installed_position_upper_equipment_id" FOREIGN KEY ("upper_equipment_id") REFERENCES "upper_equipment" ("upper_equipment_id");
ALTER TABLE "installed_position" ADD CONSTRAINT "fk_installed_position_landing_gear_system_id" FOREIGN KEY ("landing_gear_system_id") REFERENCES "landing_gear_system" ("landing_gear_system_id");
ALTER TABLE "part_definition" ADD CONSTRAINT "fk_part_definition_material_id" FOREIGN KEY ("material_id") REFERENCES "material" ("material_id");
ALTER TABLE "part_definition" ADD CONSTRAINT "fk_part_definition_design_version_id" FOREIGN KEY ("design_version_id") REFERENCES "design_version" ("design_version_id");
ALTER TABLE "component_instance" ADD CONSTRAINT "fk_component_instance_component_type_id" FOREIGN KEY ("component_type_id") REFERENCES "component_type" ("component_type_id");
ALTER TABLE "component_instance" ADD CONSTRAINT "fk_component_instance_installed_position_id" FOREIGN KEY ("installed_position_id") REFERENCES "installed_position" ("installed_position_id");
ALTER TABLE "component_instance" ADD CONSTRAINT "fk_component_instance_design_version_id" FOREIGN KEY ("design_version_id") REFERENCES "design_version" ("design_version_id");
ALTER TABLE "bom_item" ADD CONSTRAINT "fk_bom_item_component_type_id" FOREIGN KEY ("component_type_id") REFERENCES "component_type" ("component_type_id");
ALTER TABLE "bom_item" ADD CONSTRAINT "fk_bom_item_parent_bom_item_id" FOREIGN KEY ("parent_bom_item_id") REFERENCES "bom_item" ("bom_item_id");
ALTER TABLE "bom_item" ADD CONSTRAINT "fk_bom_item_part_definition_id" FOREIGN KEY ("part_definition_id") REFERENCES "part_definition" ("part_definition_id");
ALTER TABLE "process_route" ADD CONSTRAINT "fk_process_route_component_type_id" FOREIGN KEY ("component_type_id") REFERENCES "component_type" ("component_type_id");
ALTER TABLE "process_route" ADD CONSTRAINT "fk_process_route_part_definition_id" FOREIGN KEY ("part_definition_id") REFERENCES "part_definition" ("part_definition_id");
ALTER TABLE "process_route" ADD CONSTRAINT "fk_process_route_design_version_id" FOREIGN KEY ("design_version_id") REFERENCES "design_version" ("design_version_id");
ALTER TABLE "operation_definition" ADD CONSTRAINT "fk_operation_definition_process_route_id" FOREIGN KEY ("process_route_id") REFERENCES "process_route" ("process_route_id");
ALTER TABLE "parameter_standard" ADD CONSTRAINT "fk_parameter_standard_operation_definition_id" FOREIGN KEY ("operation_definition_id") REFERENCES "operation_definition" ("operation_definition_id");
ALTER TABLE "inspection_spec" ADD CONSTRAINT "fk_inspection_spec_operation_definition_id" FOREIGN KEY ("operation_definition_id") REFERENCES "operation_definition" ("operation_definition_id");
ALTER TABLE "inspection_spec" ADD CONSTRAINT "fk_inspection_spec_component_type_id" FOREIGN KEY ("component_type_id") REFERENCES "component_type" ("component_type_id");
ALTER TABLE "inspection_spec" ADD CONSTRAINT "fk_inspection_spec_part_definition_id" FOREIGN KEY ("part_definition_id") REFERENCES "part_definition" ("part_definition_id");
ALTER TABLE "purchase_order" ADD CONSTRAINT "fk_purchase_order_supplier_id" FOREIGN KEY ("supplier_id") REFERENCES "supplier" ("supplier_id");
ALTER TABLE "purchase_order_line" ADD CONSTRAINT "fk_purchase_order_line_purchase_order_id" FOREIGN KEY ("purchase_order_id") REFERENCES "purchase_order" ("purchase_order_id");
ALTER TABLE "purchase_order_line" ADD CONSTRAINT "fk_purchase_order_line_material_id" FOREIGN KEY ("material_id") REFERENCES "material" ("material_id");
ALTER TABLE "supplier_batch" ADD CONSTRAINT "fk_supplier_batch_purchase_order_line_id" FOREIGN KEY ("purchase_order_line_id") REFERENCES "purchase_order_line" ("purchase_order_line_id");
ALTER TABLE "supplier_batch" ADD CONSTRAINT "fk_supplier_batch_supplier_id" FOREIGN KEY ("supplier_id") REFERENCES "supplier" ("supplier_id");
ALTER TABLE "supplier_batch" ADD CONSTRAINT "fk_supplier_batch_material_id" FOREIGN KEY ("material_id") REFERENCES "material" ("material_id");
ALTER TABLE "inventory_batch" ADD CONSTRAINT "fk_inventory_batch_supplier_batch_id" FOREIGN KEY ("supplier_batch_id") REFERENCES "supplier_batch" ("supplier_batch_id");
ALTER TABLE "inventory_batch" ADD CONSTRAINT "fk_inventory_batch_material_id" FOREIGN KEY ("material_id") REFERENCES "material" ("material_id");
ALTER TABLE "receiving_record" ADD CONSTRAINT "fk_receiving_record_inventory_batch_id" FOREIGN KEY ("inventory_batch_id") REFERENCES "inventory_batch" ("inventory_batch_id");
ALTER TABLE "receiving_record" ADD CONSTRAINT "fk_receiving_record_purchase_order_line_id" FOREIGN KEY ("purchase_order_line_id") REFERENCES "purchase_order_line" ("purchase_order_line_id");
ALTER TABLE "issue_record" ADD CONSTRAINT "fk_issue_record_inventory_batch_id" FOREIGN KEY ("inventory_batch_id") REFERENCES "inventory_batch" ("inventory_batch_id");
ALTER TABLE "issue_record" ADD CONSTRAINT "fk_issue_record_work_order_id" FOREIGN KEY ("work_order_id") REFERENCES "work_order" ("work_order_id");
ALTER TABLE "work_order" ADD CONSTRAINT "fk_work_order_component_instance_id" FOREIGN KEY ("component_instance_id") REFERENCES "component_instance" ("component_instance_id");
ALTER TABLE "work_order" ADD CONSTRAINT "fk_work_order_part_definition_id" FOREIGN KEY ("part_definition_id") REFERENCES "part_definition" ("part_definition_id");
ALTER TABLE "work_order" ADD CONSTRAINT "fk_work_order_process_route_id" FOREIGN KEY ("process_route_id") REFERENCES "process_route" ("process_route_id");
ALTER TABLE "production_batch" ADD CONSTRAINT "fk_production_batch_work_order_id" FOREIGN KEY ("work_order_id") REFERENCES "work_order" ("work_order_id");
ALTER TABLE "production_batch" ADD CONSTRAINT "fk_production_batch_part_definition_id" FOREIGN KEY ("part_definition_id") REFERENCES "part_definition" ("part_definition_id");
ALTER TABLE "part_instance" ADD CONSTRAINT "fk_part_instance_part_definition_id" FOREIGN KEY ("part_definition_id") REFERENCES "part_definition" ("part_definition_id");
ALTER TABLE "part_instance" ADD CONSTRAINT "fk_part_instance_production_batch_id" FOREIGN KEY ("production_batch_id") REFERENCES "production_batch" ("production_batch_id");
ALTER TABLE "part_instance" ADD CONSTRAINT "fk_part_instance_inventory_batch_id" FOREIGN KEY ("inventory_batch_id") REFERENCES "inventory_batch" ("inventory_batch_id");
ALTER TABLE "part_instance" ADD CONSTRAINT "fk_part_instance_supplier_batch_id" FOREIGN KEY ("supplier_batch_id") REFERENCES "supplier_batch" ("supplier_batch_id");
ALTER TABLE "component_part_installation" ADD CONSTRAINT "fk_component_part_installation_component_instance_id" FOREIGN KEY ("component_instance_id") REFERENCES "component_instance" ("component_instance_id");
ALTER TABLE "component_part_installation" ADD CONSTRAINT "fk_component_part_installation_part_instance_id" FOREIGN KEY ("part_instance_id") REFERENCES "part_instance" ("part_instance_id");
ALTER TABLE "component_part_installation" ADD CONSTRAINT "fk_component_part_installation_bom_item_id" FOREIGN KEY ("bom_item_id") REFERENCES "bom_item" ("bom_item_id");
ALTER TABLE "component_part_installation" ADD CONSTRAINT "fk_component_part_installation_work_order_id" FOREIGN KEY ("work_order_id") REFERENCES "work_order" ("work_order_id");
ALTER TABLE "component_part_installation" ADD CONSTRAINT "fk_component_part_installation_installed_position_id" FOREIGN KEY ("installed_position_id") REFERENCES "installed_position" ("installed_position_id");
ALTER TABLE "component_part_installation" ADD CONSTRAINT "fk_component_part_installation_replacement_record_id" FOREIGN KEY ("replacement_record_id") REFERENCES "replacement_record" ("replacement_record_id");
ALTER TABLE "equipment" ADD CONSTRAINT "fk_equipment_workstation_id" FOREIGN KEY ("workstation_id") REFERENCES "workstation" ("workstation_id");
ALTER TABLE "operation_execution" ADD CONSTRAINT "fk_operation_execution_work_order_id" FOREIGN KEY ("work_order_id") REFERENCES "work_order" ("work_order_id");
ALTER TABLE "operation_execution" ADD CONSTRAINT "fk_operation_execution_operation_definition_id" FOREIGN KEY ("operation_definition_id") REFERENCES "operation_definition" ("operation_definition_id");
ALTER TABLE "operation_execution" ADD CONSTRAINT "fk_operation_execution_component_instance_id" FOREIGN KEY ("component_instance_id") REFERENCES "component_instance" ("component_instance_id");
ALTER TABLE "operation_execution" ADD CONSTRAINT "fk_operation_execution_part_instance_id" FOREIGN KEY ("part_instance_id") REFERENCES "part_instance" ("part_instance_id");
ALTER TABLE "operation_execution" ADD CONSTRAINT "fk_operation_execution_workstation_id" FOREIGN KEY ("workstation_id") REFERENCES "workstation" ("workstation_id");
ALTER TABLE "operation_execution" ADD CONSTRAINT "fk_operation_execution_equipment_id" FOREIGN KEY ("equipment_id") REFERENCES "equipment" ("equipment_id");
ALTER TABLE "step_execution" ADD CONSTRAINT "fk_step_execution_operation_execution_id" FOREIGN KEY ("operation_execution_id") REFERENCES "operation_execution" ("operation_execution_id");
ALTER TABLE "resource_usage" ADD CONSTRAINT "fk_resource_usage_operation_execution_id" FOREIGN KEY ("operation_execution_id") REFERENCES "operation_execution" ("operation_execution_id");
ALTER TABLE "resource_usage" ADD CONSTRAINT "fk_resource_usage_step_execution_id" FOREIGN KEY ("step_execution_id") REFERENCES "step_execution" ("step_execution_id");
ALTER TABLE "resource_usage" ADD CONSTRAINT "fk_resource_usage_equipment_id" FOREIGN KEY ("equipment_id") REFERENCES "equipment" ("equipment_id");
ALTER TABLE "resource_usage" ADD CONSTRAINT "fk_resource_usage_personnel_id" FOREIGN KEY ("personnel_id") REFERENCES "personnel" ("personnel_id");
ALTER TABLE "resource_usage" ADD CONSTRAINT "fk_resource_usage_workstation_id" FOREIGN KEY ("workstation_id") REFERENCES "workstation" ("workstation_id");
ALTER TABLE "resource_usage" ADD CONSTRAINT "fk_resource_usage_tooling_id" FOREIGN KEY ("tooling_id") REFERENCES "tooling" ("tooling_id");
ALTER TABLE "process_parameter_record" ADD CONSTRAINT "fk_process_parameter_record_operation_execution_id" FOREIGN KEY ("operation_execution_id") REFERENCES "operation_execution" ("operation_execution_id");
ALTER TABLE "process_parameter_record" ADD CONSTRAINT "fk_process_parameter_record_step_execution_id" FOREIGN KEY ("step_execution_id") REFERENCES "step_execution" ("step_execution_id");
ALTER TABLE "process_parameter_record" ADD CONSTRAINT "fk_process_parameter_record_parameter_standard_id" FOREIGN KEY ("parameter_standard_id") REFERENCES "parameter_standard" ("parameter_standard_id");
ALTER TABLE "process_parameter_record" ADD CONSTRAINT "fk_process_parameter_record_equipment_id" FOREIGN KEY ("equipment_id") REFERENCES "equipment" ("equipment_id");
ALTER TABLE "inspection_record" ADD CONSTRAINT "fk_inspection_record_inspection_spec_id" FOREIGN KEY ("inspection_spec_id") REFERENCES "inspection_spec" ("inspection_spec_id");
ALTER TABLE "inspection_record" ADD CONSTRAINT "fk_inspection_record_component_instance_id" FOREIGN KEY ("component_instance_id") REFERENCES "component_instance" ("component_instance_id");
ALTER TABLE "inspection_record" ADD CONSTRAINT "fk_inspection_record_part_instance_id" FOREIGN KEY ("part_instance_id") REFERENCES "part_instance" ("part_instance_id");
ALTER TABLE "inspection_record" ADD CONSTRAINT "fk_inspection_record_operation_execution_id" FOREIGN KEY ("operation_execution_id") REFERENCES "operation_execution" ("operation_execution_id");
ALTER TABLE "inspection_record" ADD CONSTRAINT "fk_inspection_record_inventory_batch_id" FOREIGN KEY ("inventory_batch_id") REFERENCES "inventory_batch" ("inventory_batch_id");
ALTER TABLE "inspection_record" ADD CONSTRAINT "fk_inspection_record_work_order_id" FOREIGN KEY ("work_order_id") REFERENCES "work_order" ("work_order_id");
ALTER TABLE "inspection_record" ADD CONSTRAINT "fk_inspection_record_inspector_id" FOREIGN KEY ("inspector_id") REFERENCES "personnel" ("personnel_id");
ALTER TABLE "inspection_item_result" ADD CONSTRAINT "fk_inspection_item_result_inspection_record_id" FOREIGN KEY ("inspection_record_id") REFERENCES "inspection_record" ("inspection_record_id");
ALTER TABLE "inspection_item_result" ADD CONSTRAINT "fk_inspection_item_result_inspection_spec_id" FOREIGN KEY ("inspection_spec_id") REFERENCES "inspection_spec" ("inspection_spec_id");
ALTER TABLE "defect_record" ADD CONSTRAINT "fk_defect_record_inspection_record_id" FOREIGN KEY ("inspection_record_id") REFERENCES "inspection_record" ("inspection_record_id");
ALTER TABLE "defect_record" ADD CONSTRAINT "fk_defect_record_component_instance_id" FOREIGN KEY ("component_instance_id") REFERENCES "component_instance" ("component_instance_id");
ALTER TABLE "defect_record" ADD CONSTRAINT "fk_defect_record_part_instance_id" FOREIGN KEY ("part_instance_id") REFERENCES "part_instance" ("part_instance_id");
ALTER TABLE "defect_record" ADD CONSTRAINT "fk_defect_record_operation_execution_id" FOREIGN KEY ("operation_execution_id") REFERENCES "operation_execution" ("operation_execution_id");
ALTER TABLE "maintenance_event" ADD CONSTRAINT "fk_maintenance_event_component_instance_id" FOREIGN KEY ("component_instance_id") REFERENCES "component_instance" ("component_instance_id");
ALTER TABLE "maintenance_event" ADD CONSTRAINT "fk_maintenance_event_installed_position_id" FOREIGN KEY ("installed_position_id") REFERENCES "installed_position" ("installed_position_id");
ALTER TABLE "iqs_failure" ADD CONSTRAINT "fk_iqs_failure_quality_event_id" FOREIGN KEY ("quality_event_id") REFERENCES "quality_event" ("quality_event_id");
ALTER TABLE "iqs_failure" ADD CONSTRAINT "fk_iqs_failure_inspection_record_id" FOREIGN KEY ("inspection_record_id") REFERENCES "inspection_record" ("inspection_record_id");
ALTER TABLE "iqs_failure" ADD CONSTRAINT "fk_iqs_failure_defect_record_id" FOREIGN KEY ("defect_record_id") REFERENCES "defect_record" ("defect_record_id");
ALTER TABLE "iqs_failure" ADD CONSTRAINT "fk_iqs_failure_component_instance_id" FOREIGN KEY ("component_instance_id") REFERENCES "component_instance" ("component_instance_id");
ALTER TABLE "iqs_failure" ADD CONSTRAINT "fk_iqs_failure_part_instance_id" FOREIGN KEY ("part_instance_id") REFERENCES "part_instance" ("part_instance_id");
ALTER TABLE "iqs_failure" ADD CONSTRAINT "fk_iqs_failure_work_order_id" FOREIGN KEY ("work_order_id") REFERENCES "work_order" ("work_order_id");
ALTER TABLE "iqs_failure" ADD CONSTRAINT "fk_iqs_failure_operation_execution_id" FOREIGN KEY ("operation_execution_id") REFERENCES "operation_execution" ("operation_execution_id");
ALTER TABLE "iqs_failure" ADD CONSTRAINT "fk_iqs_failure_supplier_id" FOREIGN KEY ("supplier_id") REFERENCES "supplier" ("supplier_id");
ALTER TABLE "iqs_failure" ADD CONSTRAINT "fk_iqs_failure_maintenance_event_id" FOREIGN KEY ("maintenance_event_id") REFERENCES "maintenance_event" ("maintenance_event_id");
ALTER TABLE "iqs_failure_content" ADD CONSTRAINT "fk_iqs_failure_content_iqs_failure_id" FOREIGN KEY ("iqs_failure_id") REFERENCES "iqs_failure" ("iqs_failure_id");
ALTER TABLE "iqs_failure_duty" ADD CONSTRAINT "fk_iqs_failure_duty_iqs_failure_id" FOREIGN KEY ("iqs_failure_id") REFERENCES "iqs_failure" ("iqs_failure_id");
ALTER TABLE "iqs_failure_duty" ADD CONSTRAINT "fk_iqs_failure_duty_iqs_failure_content_id" FOREIGN KEY ("iqs_failure_content_id") REFERENCES "iqs_failure_content" ("iqs_failure_content_id");
ALTER TABLE "iqs_failure_duty" ADD CONSTRAINT "fk_iqs_failure_duty_duty_personnel_id" FOREIGN KEY ("duty_personnel_id") REFERENCES "personnel" ("personnel_id");
ALTER TABLE "iqs_failure_duty" ADD CONSTRAINT "fk_iqs_failure_duty_supplier_id" FOREIGN KEY ("supplier_id") REFERENCES "supplier" ("supplier_id");
ALTER TABLE "disposition_record" ADD CONSTRAINT "fk_disposition_record_quality_event_id" FOREIGN KEY ("quality_event_id") REFERENCES "quality_event" ("quality_event_id");
ALTER TABLE "disposition_record" ADD CONSTRAINT "fk_disposition_record_iqs_failure_id" FOREIGN KEY ("iqs_failure_id") REFERENCES "iqs_failure" ("iqs_failure_id");
ALTER TABLE "disposition_record" ADD CONSTRAINT "fk_disposition_record_iqs_failure_content_id" FOREIGN KEY ("iqs_failure_content_id") REFERENCES "iqs_failure_content" ("iqs_failure_content_id");
ALTER TABLE "disposition_record" ADD CONSTRAINT "fk_disposition_record_work_order_id" FOREIGN KEY ("work_order_id") REFERENCES "work_order" ("work_order_id");
ALTER TABLE "disposition_record" ADD CONSTRAINT "fk_disposition_record_responsible_personnel_id" FOREIGN KEY ("responsible_personnel_id") REFERENCES "personnel" ("personnel_id");
ALTER TABLE "reinspection_record" ADD CONSTRAINT "fk_reinspection_record_disposition_record_id" FOREIGN KEY ("disposition_record_id") REFERENCES "disposition_record" ("disposition_record_id");
ALTER TABLE "reinspection_record" ADD CONSTRAINT "fk_reinspection_record_inspection_record_id" FOREIGN KEY ("inspection_record_id") REFERENCES "inspection_record" ("inspection_record_id");
ALTER TABLE "reinspection_record" ADD CONSTRAINT "fk_reinspection_record_inspector_id" FOREIGN KEY ("inspector_id") REFERENCES "personnel" ("personnel_id");
ALTER TABLE "maintenance_order" ADD CONSTRAINT "fk_maintenance_order_maintenance_event_id" FOREIGN KEY ("maintenance_event_id") REFERENCES "maintenance_event" ("maintenance_event_id");
ALTER TABLE "maintenance_order" ADD CONSTRAINT "fk_maintenance_order_component_instance_id" FOREIGN KEY ("component_instance_id") REFERENCES "component_instance" ("component_instance_id");
ALTER TABLE "maintenance_order" ADD CONSTRAINT "fk_maintenance_order_work_order_id" FOREIGN KEY ("work_order_id") REFERENCES "work_order" ("work_order_id");
ALTER TABLE "fault_record" ADD CONSTRAINT "fk_fault_record_maintenance_event_id" FOREIGN KEY ("maintenance_event_id") REFERENCES "maintenance_event" ("maintenance_event_id");
ALTER TABLE "fault_record" ADD CONSTRAINT "fk_fault_record_component_instance_id" FOREIGN KEY ("component_instance_id") REFERENCES "component_instance" ("component_instance_id");
ALTER TABLE "replacement_record" ADD CONSTRAINT "fk_replacement_record_maintenance_event_id" FOREIGN KEY ("maintenance_event_id") REFERENCES "maintenance_event" ("maintenance_event_id");
ALTER TABLE "replacement_record" ADD CONSTRAINT "fk_replacement_record_maintenance_order_id" FOREIGN KEY ("maintenance_order_id") REFERENCES "maintenance_order" ("maintenance_order_id");
ALTER TABLE "replacement_record" ADD CONSTRAINT "fk_replacement_record_component_instance_id" FOREIGN KEY ("component_instance_id") REFERENCES "component_instance" ("component_instance_id");
ALTER TABLE "replacement_record" ADD CONSTRAINT "fk_replacement_record_installed_position_id" FOREIGN KEY ("installed_position_id") REFERENCES "installed_position" ("installed_position_id");
ALTER TABLE "replacement_record" ADD CONSTRAINT "fk_replacement_record_removed_part_instance_id" FOREIGN KEY ("removed_part_instance_id") REFERENCES "part_instance" ("part_instance_id");
ALTER TABLE "replacement_record" ADD CONSTRAINT "fk_replacement_record_installed_part_instance_id" FOREIGN KEY ("installed_part_instance_id") REFERENCES "part_instance" ("part_instance_id");
ALTER TABLE "replacement_record" ADD CONSTRAINT "fk_replacement_record_inventory_batch_id" FOREIGN KEY ("inventory_batch_id") REFERENCES "inventory_batch" ("inventory_batch_id");
ALTER TABLE "service_feedback" ADD CONSTRAINT "fk_service_feedback_quality_event_id" FOREIGN KEY ("quality_event_id") REFERENCES "quality_event" ("quality_event_id");
ALTER TABLE "service_feedback" ADD CONSTRAINT "fk_service_feedback_maintenance_event_id" FOREIGN KEY ("maintenance_event_id") REFERENCES "maintenance_event" ("maintenance_event_id");
ALTER TABLE "service_feedback" ADD CONSTRAINT "fk_service_feedback_component_instance_id" FOREIGN KEY ("component_instance_id") REFERENCES "component_instance" ("component_instance_id");
ALTER TABLE "service_feedback" ADD CONSTRAINT "fk_service_feedback_iqs_failure_id" FOREIGN KEY ("iqs_failure_id") REFERENCES "iqs_failure" ("iqs_failure_id");
ALTER TABLE "quality_event_subject" ADD CONSTRAINT "fk_quality_event_subject_quality_event_id" FOREIGN KEY ("quality_event_id") REFERENCES "quality_event" ("quality_event_id");
ALTER TABLE "quality_event_subject" ADD CONSTRAINT "fk_quality_event_subject_component_instance_id" FOREIGN KEY ("component_instance_id") REFERENCES "component_instance" ("component_instance_id");
ALTER TABLE "quality_event_subject" ADD CONSTRAINT "fk_quality_event_subject_part_instance_id" FOREIGN KEY ("part_instance_id") REFERENCES "part_instance" ("part_instance_id");
ALTER TABLE "quality_event_subject" ADD CONSTRAINT "fk_quality_event_subject_production_batch_id" FOREIGN KEY ("production_batch_id") REFERENCES "production_batch" ("production_batch_id");
ALTER TABLE "quality_event_subject" ADD CONSTRAINT "fk_quality_event_subject_inventory_batch_id" FOREIGN KEY ("inventory_batch_id") REFERENCES "inventory_batch" ("inventory_batch_id");
ALTER TABLE "quality_event_subject" ADD CONSTRAINT "fk_quality_event_subject_supplier_batch_id" FOREIGN KEY ("supplier_batch_id") REFERENCES "supplier_batch" ("supplier_batch_id");
ALTER TABLE "quality_event_subject" ADD CONSTRAINT "fk_quality_event_subject_work_order_id" FOREIGN KEY ("work_order_id") REFERENCES "work_order" ("work_order_id");
ALTER TABLE "quality_event_subject" ADD CONSTRAINT "fk_quality_event_subject_operation_execution_id" FOREIGN KEY ("operation_execution_id") REFERENCES "operation_execution" ("operation_execution_id");
ALTER TABLE "quality_event_subject" ADD CONSTRAINT "fk_quality_event_subject_step_execution_id" FOREIGN KEY ("step_execution_id") REFERENCES "step_execution" ("step_execution_id");
ALTER TABLE "quality_event_subject" ADD CONSTRAINT "fk_quality_event_subject_process_parameter_record_id" FOREIGN KEY ("process_parameter_record_id") REFERENCES "process_parameter_record" ("process_parameter_record_id");
ALTER TABLE "quality_event_subject" ADD CONSTRAINT "fk_quality_event_subject_inspection_record_id" FOREIGN KEY ("inspection_record_id") REFERENCES "inspection_record" ("inspection_record_id");
ALTER TABLE "quality_event_subject" ADD CONSTRAINT "fk_quality_event_subject_defect_record_id" FOREIGN KEY ("defect_record_id") REFERENCES "defect_record" ("defect_record_id");
ALTER TABLE "quality_event_subject" ADD CONSTRAINT "fk_quality_event_subject_purchase_order_id" FOREIGN KEY ("purchase_order_id") REFERENCES "purchase_order" ("purchase_order_id");
ALTER TABLE "quality_event_subject" ADD CONSTRAINT "fk_quality_event_subject_supplier_id" FOREIGN KEY ("supplier_id") REFERENCES "supplier" ("supplier_id");
ALTER TABLE "quality_event_subject" ADD CONSTRAINT "fk_quality_event_subject_maintenance_event_id" FOREIGN KEY ("maintenance_event_id") REFERENCES "maintenance_event" ("maintenance_event_id");
ALTER TABLE "quality_event_subject" ADD CONSTRAINT "fk_quality_event_subject_replacement_record_id" FOREIGN KEY ("replacement_record_id") REFERENCES "replacement_record" ("replacement_record_id");
ALTER TABLE "quality_event_subject" ADD CONSTRAINT "fk_quality_event_subject_service_feedback_id" FOREIGN KEY ("service_feedback_id") REFERENCES "service_feedback" ("service_feedback_id");

-- Indexes
CREATE UNIQUE INDEX IF NOT EXISTS "uq_standard_dictionary_dict_type_standard_code" ON "standard_dictionary" ("dict_type", "standard_code");
CREATE INDEX IF NOT EXISTS "idx_standard_dictionary_dict_type" ON "standard_dictionary" ("dict_type");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_material_material_code" ON "material" ("material_code");
CREATE INDEX IF NOT EXISTS "idx_material_material_type" ON "material" ("material_type");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_part_definition_part_no_design_version_id" ON "part_definition" ("part_no", "design_version_id");
CREATE INDEX IF NOT EXISTS "idx_part_definition_drawing_code" ON "part_definition" ("drawing_code");
CREATE INDEX IF NOT EXISTS "idx_part_definition_source_type" ON "part_definition" ("source_type");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_component_instance_component_serial_no" ON "component_instance" ("component_serial_no");
CREATE INDEX IF NOT EXISTS "idx_component_instance_quality_code" ON "component_instance" ("quality_code");
CREATE INDEX IF NOT EXISTS "idx_component_instance_component_type_id" ON "component_instance" ("component_type_id");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_supplier_supplier_code" ON "supplier" ("supplier_code");
CREATE INDEX IF NOT EXISTS "idx_supplier_supplier_type" ON "supplier" ("supplier_type");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_purchase_order_order_no" ON "purchase_order" ("order_no");
CREATE INDEX IF NOT EXISTS "idx_purchase_order_supplier_id" ON "purchase_order" ("supplier_id");
CREATE INDEX IF NOT EXISTS "idx_purchase_order_task_code" ON "purchase_order" ("task_code");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_work_order_order_no" ON "work_order" ("order_no");
CREATE INDEX IF NOT EXISTS "idx_work_order_task_code" ON "work_order" ("task_code");
CREATE INDEX IF NOT EXISTS "idx_work_order_component_instance_id" ON "work_order" ("component_instance_id");
CREATE INDEX IF NOT EXISTS "idx_work_order_part_definition_id" ON "work_order" ("part_definition_id");
CREATE INDEX IF NOT EXISTS "idx_component_part_installation_component_instance_a0c1c13c" ON "component_part_installation" ("component_instance_id", "part_instance_id", "valid_from");
CREATE INDEX IF NOT EXISTS "idx_component_part_installation_replacement_record_id" ON "component_part_installation" ("replacement_record_id");
CREATE INDEX IF NOT EXISTS "idx_component_part_installation_work_order_id" ON "component_part_installation" ("work_order_id");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_equipment_equipment_no" ON "equipment" ("equipment_no");
CREATE INDEX IF NOT EXISTS "idx_equipment_workstation_id" ON "equipment" ("workstation_id");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_personnel_person_no" ON "personnel" ("person_no");
CREATE INDEX IF NOT EXISTS "idx_personnel_dept_code" ON "personnel" ("dept_code");
CREATE INDEX IF NOT EXISTS "idx_operation_execution_work_order_id" ON "operation_execution" ("work_order_id");
CREATE INDEX IF NOT EXISTS "idx_operation_execution_component_instance_id" ON "operation_execution" ("component_instance_id");
CREATE INDEX IF NOT EXISTS "idx_operation_execution_part_instance_id" ON "operation_execution" ("part_instance_id");
CREATE INDEX IF NOT EXISTS "idx_operation_execution_equipment_id" ON "operation_execution" ("equipment_id");
CREATE INDEX IF NOT EXISTS "idx_step_execution_operation_execution_id" ON "step_execution" ("operation_execution_id");
CREATE INDEX IF NOT EXISTS "idx_step_execution_step_no" ON "step_execution" ("step_no");
CREATE INDEX IF NOT EXISTS "idx_resource_usage_operation_execution_id" ON "resource_usage" ("operation_execution_id");
CREATE INDEX IF NOT EXISTS "idx_resource_usage_step_execution_id" ON "resource_usage" ("step_execution_id");
CREATE INDEX IF NOT EXISTS "idx_resource_usage_equipment_id" ON "resource_usage" ("equipment_id");
CREATE INDEX IF NOT EXISTS "idx_resource_usage_personnel_id" ON "resource_usage" ("personnel_id");
CREATE INDEX IF NOT EXISTS "idx_process_parameter_record_operation_execution_id" ON "process_parameter_record" ("operation_execution_id");
CREATE INDEX IF NOT EXISTS "idx_process_parameter_record_step_execution_id" ON "process_parameter_record" ("step_execution_id");
CREATE INDEX IF NOT EXISTS "idx_process_parameter_record_parameter_code" ON "process_parameter_record" ("parameter_code");
CREATE INDEX IF NOT EXISTS "idx_process_parameter_record_judge_result" ON "process_parameter_record" ("judge_result");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_inspection_record_inspection_no" ON "inspection_record" ("inspection_no");
CREATE INDEX IF NOT EXISTS "idx_inspection_record_component_instance_id" ON "inspection_record" ("component_instance_id");
CREATE INDEX IF NOT EXISTS "idx_inspection_record_part_instance_id" ON "inspection_record" ("part_instance_id");
CREATE INDEX IF NOT EXISTS "idx_inspection_record_operation_execution_id" ON "inspection_record" ("operation_execution_id");
CREATE INDEX IF NOT EXISTS "idx_inspection_record_work_order_id" ON "inspection_record" ("work_order_id");
CREATE INDEX IF NOT EXISTS "idx_inspection_item_result_inspection_record_id" ON "inspection_item_result" ("inspection_record_id");
CREATE INDEX IF NOT EXISTS "idx_inspection_item_result_item_code" ON "inspection_item_result" ("item_code");
CREATE INDEX IF NOT EXISTS "idx_inspection_item_result_judge_result" ON "inspection_item_result" ("judge_result");
CREATE INDEX IF NOT EXISTS "idx_defect_record_inspection_record_id" ON "defect_record" ("inspection_record_id");
CREATE INDEX IF NOT EXISTS "idx_defect_record_component_instance_id" ON "defect_record" ("component_instance_id");
CREATE INDEX IF NOT EXISTS "idx_defect_record_part_instance_id" ON "defect_record" ("part_instance_id");
CREATE INDEX IF NOT EXISTS "idx_defect_record_operation_execution_id" ON "defect_record" ("operation_execution_id");
CREATE INDEX IF NOT EXISTS "idx_defect_record_defect_code" ON "defect_record" ("defect_code");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_maintenance_event_event_no" ON "maintenance_event" ("event_no");
CREATE INDEX IF NOT EXISTS "idx_maintenance_event_component_instance_id" ON "maintenance_event" ("component_instance_id");
CREATE INDEX IF NOT EXISTS "idx_maintenance_event_event_type" ON "maintenance_event" ("event_type");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_quality_event_event_no" ON "quality_event" ("event_no");
CREATE INDEX IF NOT EXISTS "idx_quality_event_event_time" ON "quality_event" ("event_time");
CREATE INDEX IF NOT EXISTS "idx_quality_event_event_source_event_type" ON "quality_event" ("event_source", "event_type");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_iqs_failure_iqs_failure_order" ON "iqs_failure" ("iqs_failure_order");
CREATE INDEX IF NOT EXISTS "idx_iqs_failure_quality_event_id" ON "iqs_failure" ("quality_event_id");
CREATE INDEX IF NOT EXISTS "idx_iqs_failure_quality_code" ON "iqs_failure" ("quality_code");
CREATE INDEX IF NOT EXISTS "idx_iqs_failure_doc_source_failure_type" ON "iqs_failure" ("doc_source", "failure_type");
CREATE INDEX IF NOT EXISTS "idx_iqs_failure_component_instance_id" ON "iqs_failure" ("component_instance_id");
CREATE INDEX IF NOT EXISTS "idx_iqs_failure_part_instance_id" ON "iqs_failure" ("part_instance_id");
CREATE INDEX IF NOT EXISTS "idx_iqs_failure_work_order_id" ON "iqs_failure" ("work_order_id");
CREATE INDEX IF NOT EXISTS "idx_iqs_failure_operation_execution_id" ON "iqs_failure" ("operation_execution_id");
CREATE INDEX IF NOT EXISTS "idx_disposition_record_quality_event_id" ON "disposition_record" ("quality_event_id");
CREATE INDEX IF NOT EXISTS "idx_disposition_record_iqs_failure_id" ON "disposition_record" ("iqs_failure_id");
CREATE INDEX IF NOT EXISTS "idx_disposition_record_work_order_id" ON "disposition_record" ("work_order_id");
CREATE INDEX IF NOT EXISTS "idx_disposition_record_disposition_no" ON "disposition_record" ("disposition_no");
CREATE INDEX IF NOT EXISTS "idx_reinspection_record_disposition_record_id" ON "reinspection_record" ("disposition_record_id");
CREATE INDEX IF NOT EXISTS "idx_reinspection_record_inspection_record_id" ON "reinspection_record" ("inspection_record_id");
CREATE INDEX IF NOT EXISTS "idx_reinspection_record_reinspection_no" ON "reinspection_record" ("reinspection_no");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_maintenance_order_maintenance_order_no" ON "maintenance_order" ("maintenance_order_no");
CREATE INDEX IF NOT EXISTS "idx_maintenance_order_maintenance_event_id" ON "maintenance_order" ("maintenance_event_id");
CREATE INDEX IF NOT EXISTS "idx_maintenance_order_component_instance_id" ON "maintenance_order" ("component_instance_id");
CREATE INDEX IF NOT EXISTS "idx_maintenance_order_work_order_id" ON "maintenance_order" ("work_order_id");
CREATE INDEX IF NOT EXISTS "idx_fault_record_maintenance_event_id" ON "fault_record" ("maintenance_event_id");
CREATE INDEX IF NOT EXISTS "idx_fault_record_component_instance_id" ON "fault_record" ("component_instance_id");
CREATE INDEX IF NOT EXISTS "idx_fault_record_fault_code" ON "fault_record" ("fault_code");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_replacement_record_replacement_no" ON "replacement_record" ("replacement_no");
CREATE INDEX IF NOT EXISTS "idx_replacement_record_maintenance_event_id" ON "replacement_record" ("maintenance_event_id");
CREATE INDEX IF NOT EXISTS "idx_replacement_record_component_instance_id" ON "replacement_record" ("component_instance_id");
CREATE INDEX IF NOT EXISTS "idx_replacement_record_removed_part_instance_id" ON "replacement_record" ("removed_part_instance_id");
CREATE INDEX IF NOT EXISTS "idx_replacement_record_installed_part_instance_id" ON "replacement_record" ("installed_part_instance_id");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_service_feedback_feedback_no" ON "service_feedback" ("feedback_no");
CREATE INDEX IF NOT EXISTS "idx_service_feedback_quality_event_id" ON "service_feedback" ("quality_event_id");
CREATE INDEX IF NOT EXISTS "idx_service_feedback_maintenance_event_id" ON "service_feedback" ("maintenance_event_id");
CREATE INDEX IF NOT EXISTS "idx_service_feedback_component_instance_id" ON "service_feedback" ("component_instance_id");
CREATE INDEX IF NOT EXISTS "idx_quality_event_subject_quality_event_id_subject_type" ON "quality_event_subject" ("quality_event_id", "subject_type");
CREATE INDEX IF NOT EXISTS "idx_quality_event_subject_component_instance_id" ON "quality_event_subject" ("component_instance_id");
CREATE INDEX IF NOT EXISTS "idx_quality_event_subject_part_instance_id" ON "quality_event_subject" ("part_instance_id");
CREATE INDEX IF NOT EXISTS "idx_quality_event_subject_production_batch_id" ON "quality_event_subject" ("production_batch_id");
CREATE INDEX IF NOT EXISTS "idx_quality_event_subject_work_order_id" ON "quality_event_subject" ("work_order_id");
CREATE INDEX IF NOT EXISTS "idx_quality_event_subject_operation_execution_id" ON "quality_event_subject" ("operation_execution_id");
CREATE INDEX IF NOT EXISTS "idx_quality_event_subject_process_parameter_record_id" ON "quality_event_subject" ("process_parameter_record_id");
CREATE INDEX IF NOT EXISTS "idx_quality_event_subject_inspection_record_id" ON "quality_event_subject" ("inspection_record_id");
CREATE INDEX IF NOT EXISTS "idx_quality_event_subject_maintenance_event_id" ON "quality_event_subject" ("maintenance_event_id");
CREATE INDEX IF NOT EXISTS "idx_quality_event_subject_replacement_record_id" ON "quality_event_subject" ("replacement_record_id");
