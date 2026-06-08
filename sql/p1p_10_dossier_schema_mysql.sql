-- Generated from algorithm-service/algorithm-pwb/data/cf_data/cf/init-scripts/06_cf_dossier_schema.sql
-- Target: RuoYi-Cloud MySQL 8.0.46, database `ry-cloud`.
-- Scope: only CF dossier target tables with prefix `p1p_dossier_`.

use `ry-cloud`;
set names utf8mb4;
set foreign_key_checks = 0;

-- Drop only dossier target tables for this subject.
drop table if exists `p1p_dossier_quality_event_subject`;
drop table if exists `p1p_dossier_service_feedback`;
drop table if exists `p1p_dossier_replacement_record`;
drop table if exists `p1p_dossier_fault_record`;
drop table if exists `p1p_dossier_maintenance_order`;
drop table if exists `p1p_dossier_reinspection_record`;
drop table if exists `p1p_dossier_disposition_record`;
drop table if exists `p1p_dossier_iqs_failure_duty`;
drop table if exists `p1p_dossier_iqs_failure_content`;
drop table if exists `p1p_dossier_iqs_failure`;
drop table if exists `p1p_dossier_quality_event`;
drop table if exists `p1p_dossier_maintenance_event`;
drop table if exists `p1p_dossier_defect_record`;
drop table if exists `p1p_dossier_inspection_item_result`;
drop table if exists `p1p_dossier_inspection_record`;
drop table if exists `p1p_dossier_process_parameter_record`;
drop table if exists `p1p_dossier_resource_usage`;
drop table if exists `p1p_dossier_step_execution`;
drop table if exists `p1p_dossier_operation_execution`;
drop table if exists `p1p_dossier_tooling`;
drop table if exists `p1p_dossier_personnel`;
drop table if exists `p1p_dossier_equipment`;
drop table if exists `p1p_dossier_workstation`;
drop table if exists `p1p_dossier_component_part_installation`;
drop table if exists `p1p_dossier_part_instance`;
drop table if exists `p1p_dossier_production_batch`;
drop table if exists `p1p_dossier_work_order`;
drop table if exists `p1p_dossier_issue_record`;
drop table if exists `p1p_dossier_receiving_record`;
drop table if exists `p1p_dossier_inventory_batch`;
drop table if exists `p1p_dossier_supplier_batch`;
drop table if exists `p1p_dossier_purchase_order_line`;
drop table if exists `p1p_dossier_purchase_order`;
drop table if exists `p1p_dossier_supplier`;
drop table if exists `p1p_dossier_inspection_spec`;
drop table if exists `p1p_dossier_parameter_standard`;
drop table if exists `p1p_dossier_operation_definition`;
drop table if exists `p1p_dossier_process_route`;
drop table if exists `p1p_dossier_bom_item`;
drop table if exists `p1p_dossier_component_instance`;
drop table if exists `p1p_dossier_part_definition`;
drop table if exists `p1p_dossier_material`;
drop table if exists `p1p_dossier_standard_dictionary`;
drop table if exists `p1p_dossier_design_version`;
drop table if exists `p1p_dossier_component_type`;
drop table if exists `p1p_dossier_installed_position`;
drop table if exists `p1p_dossier_landing_gear_system`;
drop table if exists `p1p_dossier_upper_equipment`;

-- Tables
create table `p1p_dossier_upper_equipment` (
  `upper_equipment_id` bigint not null auto_increment primary key comment '上级装备主键，单列主键用于结构匹配锚点',
  `equipment_code` varchar(100) not null comment '上级装备编号，参考机型编码或实际架次等真实业务编号',
  `equipment_name` varchar(200) not null comment '上级装备名称',
  `equipment_type` varchar(100) comment '装备类型，例如试验装机对象或某型航空装备',
  `product_code` varchar(100) comment '机型编码，借鉴 IQS 中 PRODUCT_CODE 字段风格',
  `product_name` varchar(200) comment '机型名称，借鉴 IQS 中 PRODUCT_NAME 字段风格',
  `actual_plane_no` varchar(100) comment '实际架次，借鉴 IQS 中 ACTUAL_PLANE_NO 字段风格',
  `secret_level` varchar(20) comment '密级代码，借鉴 IQS 中 SECRET_LEVEL 字段风格',
  `created_at` datetime comment '创建时间',
  `updated_at` datetime comment '更新时间'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='上级航空装备或试验装机对象表，只作为作动筒组件装机背景，不展开整机结构';

create table `p1p_dossier_landing_gear_system` (
  `landing_gear_system_id` bigint not null auto_increment primary key comment '起落架系统主键',
  `upper_equipment_id` bigint not null comment '所属上级装备主键',
  `system_code` varchar(100) not null comment '起落架系统编号',
  `system_name` varchar(200) not null comment '起落架系统名称',
  `system_position` varchar(100) comment '系统位置，例如前起落架或主起落架',
  `technical_status` varchar(100) comment '技术状态',
  `created_at` datetime comment '创建时间',
  `updated_at` datetime comment '更新时间'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='起落架系统表，用于说明作动筒组件所属功能位置';

create table `p1p_dossier_installed_position` (
  `installed_position_id` bigint not null auto_increment primary key comment '装机位置主键',
  `upper_equipment_id` bigint comment '所属上级装备主键',
  `landing_gear_system_id` bigint comment '所属起落架系统主键',
  `position_code` varchar(100) not null comment '装机位置编码',
  `position_name` varchar(200) not null comment '装机位置名称',
  `station_no` varchar(100) comment '站位号或安装站位编号',
  `side_code` varchar(50) comment '左右侧或安装侧别代码',
  `created_at` datetime comment '创建时间',
  `updated_at` datetime comment '更新时间'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='作动筒组件或零件的装机位置表，服务 MRO 拆换和服役追溯';

create table `p1p_dossier_component_type` (
  `component_type_id` bigint not null auto_increment primary key comment '组件型号主键',
  `component_code` varchar(100) not null comment '作动筒组件型号编码',
  `component_name` varchar(200) not null comment '作动筒组件型号名称',
  `component_spec` varchar(200) comment '组件规格或构型说明',
  `product_code` varchar(100) comment '适用机型编码，借鉴 IQS 中 PRODUCT_CODE 字段风格',
  `product_name` varchar(200) comment '适用机型名称，借鉴 IQS 中 PRODUCT_NAME 字段风格',
  `drawing_code` varchar(200) comment '组件图号，借鉴 IQS 中 DRAWING_CODE 字段风格',
  `technical_status` varchar(100) comment '技术状态',
  `created_at` datetime comment '创建时间',
  `updated_at` datetime comment '更新时间'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='起落架收放液压作动筒组件型号表';

create table `p1p_dossier_design_version` (
  `design_version_id` bigint not null auto_increment primary key comment '设计版本主键',
  `version_code` varchar(100) not null comment '设计版本编码',
  `version_name` varchar(200) comment '设计版本名称',
  `drawing_code` varchar(200) comment '图号编码，借鉴 IQS 中 DRAWING_CODE 字段风格',
  `doc_version` varchar(50) comment '文件版本，借鉴 IQS 中 DOC_VERSION 字段风格',
  `effective_date` date comment '版本生效日期',
  `expire_date` date comment '版本失效日期',
  `release_status` varchar(50) comment '发布状态',
  `created_at` datetime comment '创建时间',
  `updated_at` datetime comment '更新时间'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='组件、零件、图纸或技术文件的设计版本表';

create table `p1p_dossier_standard_dictionary` (
  `dict_id` bigint not null auto_increment primary key comment '标准字典主键',
  `dict_type` varchar(100) not null comment '字典类型，例如 inspection_result、operation_status、failure_type、disposition_type',
  `standard_code` varchar(100) not null comment '卷宗标准编码',
  `standard_name` varchar(200) not null comment '卷宗标准名称',
  `standard_desc` text comment '标准字典值说明',
  `enabled` tinyint(1) comment '是否启用',
  `sort_no` int comment '排序号'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='卷宗库标准字典表，用于定义检验结果、工序状态、不合格类型、处置类型等标准枚举值；不保存源系统私有编码映射';

create table `p1p_dossier_material` (
  `material_id` bigint not null auto_increment primary key comment '物料主键',
  `material_code` varchar(100) not null comment '物料编码',
  `material_name` varchar(200) not null comment '物料名称',
  `material_type` varchar(80) comment '物料类型，例如自制件、外购件、原材料、外协服务',
  `trademark` varchar(100) comment '材料牌号，借鉴 IQS 中 TRADEMARK 字段风格',
  `material_spec` varchar(200) comment '材料规格',
  `unit` varchar(30) comment '计量单位',
  `purchased_flag` tinyint(1) comment '是否外购，参考订单表中 PURCHASED 字段语义',
  `created_at` datetime comment '创建时间',
  `updated_at` datetime comment '更新时间'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='物料主数据表，承接 ERP 采购库存语义';

create table `p1p_dossier_part_definition` (
  `part_definition_id` bigint not null auto_increment primary key comment '零件定义主键',
  `material_id` bigint comment '对应物料主键',
  `design_version_id` bigint comment '适用设计版本主键',
  `part_no` varchar(100) not null comment '零件号，借鉴 IQS 中 PIECE_NO 字段风格',
  `part_name` varchar(200) not null comment '零件名称',
  `part_type` varchar(80) comment '零件类型，例如缸筒、活塞杆、密封圈、位置传感器',
  `drawing_code` varchar(200) comment '零件图号，借鉴 IQS 中 DRAWING_CODE 字段风格',
  `source_type` varchar(50) comment '来源类型，例如自制、外购、外协',
  `critical_level` varchar(50) comment '关键程度',
  `created_at` datetime comment '创建时间',
  `updated_at` datetime comment '更新时间'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='作动筒零件号层面的定义表';

create table `p1p_dossier_component_instance` (
  `component_instance_id` bigint not null auto_increment primary key comment '组件实例主键，算法 Anchor 表之一',
  `component_type_id` bigint not null comment '所属组件型号主键',
  `installed_position_id` bigint comment '当前装机位置主键',
  `design_version_id` bigint comment '装配适用设计版本主键',
  `component_serial_no` varchar(100) not null comment '作动筒组件序列号',
  `component_batch_no` varchar(100) comment '组件装配批次号',
  `quality_code` varchar(200) comment '组件质量编号，借鉴 IQS 中 QUALITY_CODE 字段风格',
  `assembly_order_no` varchar(100) comment '装配工单编号',
  `delivery_date` date comment '交付日期，参考订单表 DELIVERY_DATE 字段语义',
  `component_status` varchar(50) comment '组件状态',
  `created_at` datetime comment '创建时间',
  `updated_at` datetime comment '更新时间'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='具体起落架收放液压作动筒组件实例表，是质量追溯核心锚点';

create table `p1p_dossier_bom_item` (
  `bom_item_id` bigint not null auto_increment primary key comment 'BOM 项主键',
  `component_type_id` bigint not null comment '所属组件型号主键',
  `parent_bom_item_id` bigint comment '父级 BOM 项主键，用于多层 BOM',
  `part_definition_id` bigint not null comment 'BOM 对应零件定义主键',
  `bom_item_no` varchar(100) not null comment 'BOM 项号',
  `part_no` varchar(100) comment '零件号冗余字段，便于卷宗查询和样本生成',
  `quantity` decimal(12,3) not null comment '装配数量',
  `unit` varchar(30) comment '计量单位',
  `position_code` varchar(100) comment 'BOM 装配位置编码',
  `effective_date` date comment '生效日期'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='作动筒组件 BOM 项表，表达组件型号与零件定义的结构关系';

create table `p1p_dossier_process_route` (
  `process_route_id` bigint not null auto_increment primary key comment '工艺路线主键',
  `component_type_id` bigint comment '适用组件型号主键',
  `part_definition_id` bigint comment '适用零件定义主键',
  `design_version_id` bigint comment '适用设计版本主键',
  `route_code` varchar(100) not null comment '工艺路线编码',
  `route_name` varchar(200) not null comment '工艺路线名称',
  `route_type` varchar(80) comment '路线类型，例如零件加工、组件装配、试验检验',
  `doc_version` varchar(50) comment '工艺文件版本，借鉴 IQS 中 DOC_VERSION 字段风格',
  `release_status` varchar(50) comment '发布状态',
  `created_at` datetime comment '创建时间',
  `updated_at` datetime comment '更新时间'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='作动筒组件或零件制造装配检验所遵循的工艺路线表';

create table `p1p_dossier_operation_definition` (
  `operation_definition_id` bigint not null auto_increment primary key comment '工序定义主键',
  `process_route_id` bigint not null comment '所属工艺路线主键',
  `operation_no` varchar(50) not null comment '工序编号，参考 IQS 中 OP_NO 字段语义',
  `work_no` varchar(50) comment '工序编码，参考 Sys_Worksort 中 workid 字段语义',
  `work_name` varchar(200) not null comment '工序名称，参考 Sys_Worksort 中 workname 字段语义',
  `work_num` int comment '工序序号，参考 Sys_Worksort 中 worknum 字段语义',
  `standard_time` decimal(10,2) comment '标准耗时，参考 Sys_Worksort 中 usetime 字段语义',
  `required_equipment_type` varchar(100) comment '要求设备类型'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='标准工序定义表，借鉴真实 MES 工序表命名风格';

create table `p1p_dossier_parameter_standard` (
  `parameter_standard_id` bigint not null auto_increment primary key comment '工艺参数标准主键',
  `operation_definition_id` bigint not null comment '所属工序定义主键',
  `parameter_code` varchar(100) not null comment '参数编码',
  `parameter_name` varchar(200) not null comment '参数名称，例如泄漏量、试验压力、镀层厚度',
  `parameter_unit` varchar(50) comment '参数单位',
  `standard_value` decimal(18,6) comment '标准值',
  `lower_limit` decimal(18,6) comment '下限值',
  `upper_limit` decimal(18,6) comment '上限值',
  `control_level` varchar(50) comment '控制等级'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='工序或工步对应的工艺参数标准表';

create table `p1p_dossier_inspection_spec` (
  `inspection_spec_id` bigint not null auto_increment primary key comment '检验规范主键',
  `operation_definition_id` bigint comment '关联工序定义主键',
  `component_type_id` bigint comment '适用组件型号主键',
  `part_definition_id` bigint comment '适用零件定义主键',
  `spec_code` varchar(100) not null comment '检验规范编码',
  `spec_name` varchar(200) not null comment '检验规范名称',
  `inspection_type` varchar(80) comment '检验类型，例如来料检验、过程检验、最终检验、复检',
  `item_code` varchar(100) comment '检验项目编码',
  `item_name` varchar(200) comment '检验项目名称',
  `acceptance_criteria` text comment '验收准则'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='检验规范表，规定作动筒组件、零件或工序的检验项目和判定标准';

create table `p1p_dossier_supplier` (
  `supplier_id` bigint not null auto_increment primary key comment '供应商主键，算法 Anchor 表之一',
  `supplier_code` varchar(100) not null comment '供应商编码',
  `supplier_name` varchar(200) not null comment '供应商名称',
  `supplier_type` varchar(80) comment '供应商类型，例如材料、密封件、传感器、外协加工',
  `qualification_level` varchar(80) comment '供应商资质等级',
  `contact_person` varchar(100) comment '联系人',
  `phone` varchar(50) comment '联系电话',
  `address` text comment '供应商地址',
  `enabled` tinyint(1) comment '是否启用'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='供应商主数据表，用于采购来源和外购件质量追溯';

create table `p1p_dossier_purchase_order` (
  `purchase_order_id` bigint not null auto_increment primary key comment '采购订单主键，算法 Anchor 表之一',
  `supplier_id` bigint not null comment '供应商主键',
  `order_no` varchar(100) not null comment '采购订单编号，参考订单表 ORDER_NO 字段风格',
  `task_code` varchar(100) comment '采购任务编码，借鉴 IQS 中 TASK_CODE 字段风格',
  `order_date` date comment '订单日期',
  `delivery_date` date comment '计划交付日期，参考订单表 DELIVERY_DATE 字段语义',
  `order_status` varchar(50) comment '订单状态',
  `priority_level` int comment '优先级，参考订单表 yxj 字段语义',
  `urgent_flag` tinyint(1) comment '紧急标识，参考订单表 urgent_tab 字段语义'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='采购订单表，记录外购件、原材料或外协服务采购业务';

create table `p1p_dossier_purchase_order_line` (
  `purchase_order_line_id` bigint not null auto_increment primary key comment '采购订单行主键',
  `purchase_order_id` bigint not null comment '所属采购订单主键',
  `material_id` bigint not null comment '采购物料主键',
  `line_no` int not null comment '订单行号，参考订单表 part_NUM 字段语义',
  `amount` decimal(18,3) not null comment '采购数量，参考订单表 AMOUNT 字段语义',
  `unit` varchar(30) comment '计量单位',
  `required_date` date comment '需求日期',
  `delivery_date` date comment '计划到货日期',
  `line_status` varchar(50) comment '订单行状态'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='采购订单明细表，记录具体物料、数量和交期';

create table `p1p_dossier_supplier_batch` (
  `supplier_batch_id` bigint not null auto_increment primary key comment '供应商批次主键',
  `purchase_order_line_id` bigint not null comment '来源采购订单行主键',
  `supplier_id` bigint not null comment '供应商主键',
  `material_id` bigint not null comment '物料主键',
  `supplier_batch_no` varchar(100) not null comment '供应商批次号',
  `certificate_no` varchar(100) comment '合格证编号',
  `manufacture_date` date comment '供应商生产日期',
  `expire_date` date comment '有效期',
  `batch_status` varchar(50) comment '供应商批次状态'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='供应商批次表，用于外购件和材料来源追溯';

create table `p1p_dossier_inventory_batch` (
  `inventory_batch_id` bigint not null auto_increment primary key comment '库存批次主键',
  `supplier_batch_id` bigint comment '对应供应商批次主键',
  `material_id` bigint not null comment '库存物料主键',
  `inventory_batch_no` varchar(100) not null comment '企业库存批次号',
  `warehouse_code` varchar(100) comment '仓库编码',
  `location_code` varchar(100) comment '库位编码',
  `amount` decimal(18,3) comment '库存数量',
  `unit` varchar(30) comment '计量单位',
  `batch_status` varchar(50) comment '库存批次状态'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='企业库存批次表，连接采购入库和生产领料';

create table `p1p_dossier_receiving_record` (
  `receiving_record_id` bigint not null auto_increment primary key comment '入库记录主键',
  `inventory_batch_id` bigint not null comment '形成的库存批次主键',
  `purchase_order_line_id` bigint comment '来源采购订单行主键',
  `receiving_no` varchar(100) not null comment '入库单号',
  `receiving_date` datetime comment '入库时间',
  `received_amount` decimal(18,3) comment '入库数量',
  `receiver_name` varchar(100) comment '入库人员名称',
  `inspection_required` tinyint(1) comment '是否需要来料检验',
  `receiving_status` varchar(50) comment '入库状态'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='物料到货入库记录表';

create table `p1p_dossier_issue_record` (
  `issue_record_id` bigint not null auto_increment primary key comment '领料记录主键',
  `inventory_batch_id` bigint not null comment '领用库存批次主键',
  `work_order_id` bigint comment '领料对应工单主键',
  `issue_no` varchar(100) not null comment '领料单号',
  `issue_date` datetime comment '领料时间',
  `issued_amount` decimal(18,3) comment '领料数量',
  `issue_person_name` varchar(100) comment '领料人员名称',
  `issue_purpose` varchar(200) comment '领料用途',
  `issue_status` varchar(50) comment '领料状态'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='生产或装配工单从库存领用物料的记录表';

create table `p1p_dossier_work_order` (
  `work_order_id` bigint not null auto_increment primary key comment '工单主键，算法 Anchor 表之一',
  `component_instance_id` bigint comment '关联组件实例主键',
  `part_definition_id` bigint comment '关联零件定义主键',
  `process_route_id` bigint comment '执行工艺路线主键',
  `order_no` varchar(100) not null comment '工单编号，参考订单表 ORDER_NO 字段风格',
  `task_code` varchar(100) comment '任务编码，借鉴 IQS 中 TASK_CODE 字段风格',
  `work_order_type` varchar(50) not null comment '工单类型，可区分生产、装配、维修',
  `order_name` varchar(200) comment '工单名称或任务名称，参考 Sys_Task 中 taskname 字段语义',
  `amount` int comment '计划数量，参考订单表 AMOUNT 字段语义',
  `priority_level` int comment '优先级，参考订单表 yxj 字段语义',
  `urgent_flag` tinyint(1) comment '紧急标识，参考订单表 urgent_tab 字段语义',
  `plan_start_time` datetime comment '计划开始时间，参考 Sys_Task 中 plandate 字段语义',
  `plan_finish_time` datetime comment '计划完成时间，参考订单表 DELIVERY_DATE 字段语义',
  `report_person_name` varchar(100) comment '提交人，参考 Sys_Task 中 reportman 字段语义',
  `report_time` datetime comment '提交时间，参考 Sys_Task 中 reportdate 字段语义',
  `order_status` varchar(50) comment '工单状态，参考任务状态 rwstate 和 DONE 字段语义'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='生产工单、装配工单和维修工单的统一工单表';

create table `p1p_dossier_production_batch` (
  `production_batch_id` bigint not null auto_increment primary key comment '生产批次主键',
  `work_order_id` bigint not null comment '来源生产工单主键',
  `part_definition_id` bigint comment '批次对应零件定义主键',
  `production_batch_no` varchar(100) not null comment '生产批次号',
  `batch_amount` int comment '批次数量',
  `start_time` datetime comment '批次开始时间',
  `finish_time` datetime comment '批次完成时间',
  `batch_status` varchar(50) comment '批次状态',
  `quality_code` varchar(200) comment '批次质量编号，借鉴 IQS 中 QUALITY_CODE 字段风格'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='自制零件或装配过程形成的生产批次表';

create table `p1p_dossier_part_instance` (
  `part_instance_id` bigint not null auto_increment primary key comment '零件实例主键，算法 Anchor 表之一',
  `part_definition_id` bigint not null comment '所属零件定义主键',
  `production_batch_id` bigint comment '自制来源生产批次主键',
  `inventory_batch_id` bigint comment '外购来源库存批次主键',
  `supplier_batch_id` bigint comment '外购来源供应商批次主键',
  `part_serial_no` varchar(100) comment '零件序列号',
  `part_lot_no` varchar(100) comment '零件批次号',
  `piece_no` varchar(100) comment '件号，借鉴 IQS 中 PIECE_NO 字段风格',
  `quality_code` varchar(200) comment '零件质量编号，借鉴 IQS 中 QUALITY_CODE 字段风格',
  `source_type` varchar(50) comment '来源类型，例如自制、外购、外协',
  `part_status` varchar(50) comment '零件状态'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='具体零件个体表，可反查生产批次、库存批次或供应商批次';

create table `p1p_dossier_component_part_installation` (
  `installation_id` bigint not null auto_increment primary key comment '组件零件装配关系主键',
  `component_instance_id` bigint not null comment '组件实例主键',
  `part_instance_id` bigint not null comment '零件实例主键',
  `bom_item_id` bigint comment '对应 BOM 项主键',
  `work_order_id` bigint comment '装配工单主键',
  `installed_position_id` bigint comment '装配位置主键',
  `replacement_record_id` bigint comment '关联拆换记录主键，用于 MRO 拆换闭环',
  `install_time` datetime comment '装配时间',
  `valid_from` datetime comment '装配关系有效开始时间，通常与装配完成或检验放行时间一致',
  `valid_to` datetime comment '装配关系有效结束时间，拆下或替换后写入',
  `uninstall_time` datetime comment '拆下时间，用于服役维修拆换追溯',
  `uninstall_reason` varchar(200) comment '拆下原因，例如维修更换、故障拆检、寿命到期',
  `install_person_name` varchar(100) comment '装配人员名称',
  `install_status` varchar(50) comment '装配状态'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='组件实例与零件实例的装配关系表，支持拆换前后历史追溯';

create table `p1p_dossier_workstation` (
  `workstation_id` bigint not null auto_increment primary key comment '工位主键',
  `station_no` varchar(50) not null comment '工位编号，参考 Sys_Station 中 stationid 字段语义',
  `station_name` varchar(200) not null comment '工位名称，参考 Sys_Station 中 stationname 字段语义',
  `workshop` varchar(100) comment '所属车间',
  `person_count` int comment '人员数量，参考 Sys_Station 中 percount 字段语义',
  `equipment_count` int comment '设备数量，参考 Sys_Station 中 equcount 字段语义',
  `station_status` varchar(50) comment '工位状态'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='生产线或车间作业工位表';

create table `p1p_dossier_equipment` (
  `equipment_id` bigint not null auto_increment primary key comment '设备主键',
  `workstation_id` bigint comment '所属工位主键',
  `equipment_no` varchar(100) not null comment '设备编号，参考设备表 sbbh 字段语义',
  `equipment_name` varchar(200) not null comment '设备名称，参考 equipmentname 字段语义',
  `equipment_num` int comment '设备序号，参考 equipmentnum 字段语义',
  `equipment_type` varchar(100) comment '设备类型',
  `status_code` varchar(50) comment '设备状态，规范化参考 zhuangtai 字段',
  `color_flag` varchar(50) comment '颜色标识，参考 bgcolor 或 color 字段语义',
  `created_at` datetime comment '创建时间，参考 indate 字段语义'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='加工、装配、检测或维修设备表，借鉴真实 MES 设备字段风格';

create table `p1p_dossier_personnel` (
  `personnel_id` bigint not null auto_increment primary key comment '人员主键',
  `person_no` varchar(100) not null comment '员工编号，参考 personnum 字段语义',
  `person_name` varchar(100) not null comment '员工姓名，参考 personname 字段语义',
  `workshop` varchar(100) comment '所属车间，参考 workshop 字段语义',
  `phone` varchar(50) comment '联系电话，参考 phone 字段语义',
  `skill_level` varchar(50) comment '技能等级，规范化参考 jndj 字段',
  `work_type` varchar(50) comment '工种，规范化参考 gz 字段',
  `dept_code` varchar(100) comment '所属部门编码',
  `dept_name` varchar(200) comment '所属部门名称'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='操作员、检验员、维修人员和责任人的统一人员表';

create table `p1p_dossier_tooling` (
  `tooling_id` bigint not null auto_increment primary key comment '工装主键',
  `tooling_no` varchar(100) not null comment '工装编号',
  `tooling_name` varchar(200) not null comment '工装名称',
  `tooling_type` varchar(100) comment '工装类型',
  `calibration_due_date` date comment '校准到期日期',
  `tooling_status` varchar(50) comment '工装状态'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='夹具、模具、工具、专用工装等辅助资源表';

create table `p1p_dossier_operation_execution` (
  `operation_execution_id` bigint not null auto_increment primary key comment '工序执行主键，算法 Beneficiary 表之一',
  `work_order_id` bigint not null comment '所属工单主键',
  `operation_definition_id` bigint not null comment '对应工序定义主键',
  `component_instance_id` bigint comment '关联组件实例主键',
  `part_instance_id` bigint comment '关联零件实例主键',
  `workstation_id` bigint comment '执行工位主键',
  `equipment_id` bigint comment '主要执行设备主键',
  `op_no` varchar(50) not null comment '执行工序编号，借鉴 IQS 中 OP_NO 字段风格',
  `work_no` varchar(50) comment '执行工序编码，参考 workid 字段语义',
  `work_name` varchar(200) comment '执行工序名称，参考 workname 字段语义',
  `start_time` datetime comment '实际开始时间',
  `finish_time` datetime comment '实际完成时间',
  `execution_status` varchar(50) comment '执行状态'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='工单下某道工序的实际执行记录表';

create table `p1p_dossier_step_execution` (
  `step_execution_id` bigint not null auto_increment primary key comment '工步执行主键，算法 Beneficiary 表之一',
  `operation_execution_id` bigint not null comment '所属工序执行主键',
  `step_no` varchar(50) not null comment '工步编号，参考 stepsid 字段语义',
  `step_name` varchar(200) not null comment '工步名称，参考 stepsname 字段语义',
  `step_sequence` int comment '工步顺序',
  `start_time` datetime comment '工步开始时间',
  `finish_time` datetime comment '工步完成时间',
  `step_status` varchar(50) comment '工步状态',
  `operator_name` varchar(100) comment '操作人员名称'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='工序下更细粒度的工步实际执行记录表';

create table `p1p_dossier_resource_usage` (
  `resource_usage_id` bigint not null auto_increment primary key comment '资源使用主键，算法 Beneficiary 表之一',
  `operation_execution_id` bigint not null comment '所属工序执行主键',
  `step_execution_id` bigint comment '所属工步执行主键',
  `equipment_id` bigint comment '使用设备主键',
  `personnel_id` bigint comment '使用人员主键',
  `workstation_id` bigint comment '使用工位主键',
  `tooling_id` bigint comment '使用工装主键',
  `usage_start_time` datetime comment '资源使用开始时间',
  `usage_finish_time` datetime comment '资源使用结束时间',
  `mastery` decimal(5,2) comment '人员对设备熟练度，参考 sys_equandper 中 mastery 字段语义',
  `usage_status` varchar(50) comment '资源使用状态'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='工序或工步执行过程中使用设备、人员、工位、工装的记录表';

create table `p1p_dossier_process_parameter_record` (
  `process_parameter_record_id` bigint not null auto_increment primary key comment '过程参数记录主键，算法 Beneficiary 表之一',
  `operation_execution_id` bigint not null comment '所属工序执行主键',
  `step_execution_id` bigint comment '所属工步执行主键',
  `parameter_standard_id` bigint comment '对应参数标准主键',
  `equipment_id` bigint comment '采集设备主键',
  `parameter_code` varchar(100) not null comment '参数编码',
  `parameter_name` varchar(200) not null comment '参数名称',
  `measured_value` decimal(18,6) comment '实测值',
  `standard_value` decimal(18,6) comment '标准值',
  `lower_limit` decimal(18,6) comment '下限值',
  `upper_limit` decimal(18,6) comment '上限值',
  `parameter_unit` varchar(50) comment '参数单位',
  `collect_time` datetime comment '采集时间',
  `judge_result` varchar(50) comment '判定结果'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='设备、工序或工步执行过程中采集的过程参数记录表';

create table `p1p_dossier_inspection_record` (
  `inspection_record_id` bigint not null auto_increment primary key comment '检验记录主键，质量追溯 Anchor 表之一',
  `inspection_spec_id` bigint comment '对应检验规范主键',
  `component_instance_id` bigint comment '受检组件实例主键',
  `part_instance_id` bigint comment '受检零件实例主键',
  `operation_execution_id` bigint comment '关联工序执行主键',
  `inventory_batch_id` bigint comment '来料检验关联库存批次主键',
  `work_order_id` bigint comment '关联工单主键',
  `inspector_id` bigint comment '检验员主键',
  `inspection_no` varchar(100) not null comment '检验记录编号',
  `inspection_type` varchar(80) comment '检验类型，例如来料、过程、最终、复检',
  `find_date` datetime comment '发现或检验时间，借鉴 IQS 中 FIND_DATE 字段风格',
  `find_type` varchar(80) comment '发现方式，借鉴 IQS 中 FIND_TYPE 字段风格',
  `inspection_result` varchar(50) comment '检验结论'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='一次检验活动记录表，支持来料检验、过程检验、最终检验和复检';

create table `p1p_dossier_inspection_item_result` (
  `inspection_item_result_id` bigint not null auto_increment primary key comment '检验项目结果主键，算法 Beneficiary 表之一',
  `inspection_record_id` bigint not null comment '所属检验记录主键',
  `inspection_spec_id` bigint comment '对应检验规范主键',
  `item_code` varchar(100) not null comment '检验项目编码',
  `item_name` varchar(200) not null comment '检验项目名称',
  `measured_value` varchar(200) comment '检测值，可能包含数值或文本',
  `standard_value` varchar(200) comment '标准值',
  `lower_limit` varchar(100) comment '下限',
  `upper_limit` varchar(100) comment '上限',
  `unit` varchar(50) comment '计量单位',
  `judge_result` varchar(50) comment '判定结果'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='检验记录下具体检验项目的检测结果表';

create table `p1p_dossier_defect_record` (
  `defect_record_id` bigint not null auto_increment primary key comment '缺陷记录主键',
  `inspection_record_id` bigint comment '来源检验记录主键',
  `component_instance_id` bigint comment '关联组件实例主键',
  `part_instance_id` bigint comment '关联零件实例主键',
  `operation_execution_id` bigint comment '关联工序执行主键',
  `defect_code` varchar(100) not null comment '缺陷代码',
  `defect_name` varchar(200) comment '缺陷名称',
  `defect_position` varchar(200) comment '缺陷部位',
  `severity_level` varchar(50) comment '严重程度',
  `failure_desc` text comment '不合格情况描述，借鉴 IQS 中 FAILURE_DESC 字段风格',
  `find_dept_code` varchar(100) comment '发现单位编码，借鉴 IQS 中 FIND_DEPT_CODE 字段风格',
  `find_dept_name` varchar(200) comment '发现单位名称，借鉴 IQS 中 FIND_DEPT_NAME 字段风格'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='检验或过程发现的缺陷记录表';

create table `p1p_dossier_maintenance_event` (
  `maintenance_event_id` bigint not null auto_increment primary key comment '维修事件主键，算法 Anchor 表之一',
  `component_instance_id` bigint not null comment '维修涉及组件实例主键',
  `installed_position_id` bigint comment '维修发生装机位置主键',
  `event_no` varchar(100) not null comment '维修事件编号',
  `service_date` datetime comment '服役或维修发生时间',
  `event_type` varchar(80) comment '事件类型，例如维修、保养、排故、拆换',
  `service_unit_code` varchar(100) comment '维修单位编码',
  `service_unit_name` varchar(200) comment '维修单位名称',
  `event_status` varchar(50) comment '维修事件状态'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='作动筒组件装机后发生的维修、保养、排故或服务事件表';

create table `p1p_dossier_quality_event` (
  `quality_event_id` bigint not null auto_increment primary key comment '质量事件主键，作为异常入口和追溯查询起点',
  `event_no` varchar(100) not null comment '质量事件编号，用于唯一标识一次异常事件',
  `event_source` varchar(80) comment '事件来源，例如 inspection、process_parameter、mro、iqs、manual',
  `event_type` varchar(80) comment '事件类型，例如不合格、缺陷、参数异常、服役故障',
  `event_title` varchar(200) comment '质量事件标题',
  `event_desc` text comment '质量事件描述',
  `event_time` datetime comment '事件发生或发现时间',
  `severity_level` varchar(50) comment '严重程度',
  `event_status` varchar(50) comment '事件状态，例如新建、处理中、已关闭',
  `trigger_source_no` varchar(120) comment '触发来源业务编号，例如检验单号、参数记录号、不合格通知单号或维修事件编号',
  `created_at` datetime comment '创建时间',
  `updated_at` datetime comment '更新时间'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='质量事件统一主表，表示异常入口；不合格通知单、参数异常和服役反馈均可由该表发起追溯';

create table `p1p_dossier_iqs_failure` (
  `iqs_failure_id` bigint not null auto_increment primary key comment '不合格通知单主键，算法 Anchor 表之一',
  `quality_event_id` bigint not null comment '来源质量事件主键，不合格通知单由质量事件形成',
  `inspection_record_id` bigint comment '触发该不合格的检验记录主键',
  `defect_record_id` bigint comment '触发该不合格的缺陷记录主键',
  `component_instance_id` bigint comment '涉及组件实例主键',
  `part_instance_id` bigint comment '涉及零件实例主键',
  `work_order_id` bigint comment '涉及工单主键',
  `operation_execution_id` bigint comment '涉及工序执行主键',
  `supplier_id` bigint comment '涉及供应商主键',
  `maintenance_event_id` bigint comment '来源维修事件主键',
  `doc_source` varchar(80) comment '不合格来源代码，借鉴 IQS 中 DOC_SOURCE 字段风格',
  `failure_type` varchar(80) comment '不合格品类别代码，借鉴 IQS 中 FAILURE_TYPE 字段风格',
  `quality_code` varchar(200) not null comment '质量编号，借鉴 IQS 中 QUALITY_CODE 字段风格',
  `iqs_failure_order` varchar(120) not null comment '不合格通知单编号，借鉴 IQS_FAILURE_ORDER 字段风格',
  `task_code` varchar(100) comment '任务编号，借鉴 IQS 中 TASK_CODE 字段风格',
  `product_code` varchar(100) comment '机型编码，借鉴 IQS 中 PRODUCT_CODE 字段风格',
  `product_name` varchar(200) comment '机型名称，借鉴 IQS 中 PRODUCT_NAME 字段风格',
  `piece_no` varchar(100) comment '件号，借鉴 IQS 中 PIECE_NO 字段风格',
  `op_no` varchar(50) comment '发现工序编号，借鉴 IQS 中 OP_NO 字段风格',
  `find_dept_code` varchar(100) comment '发现单位编码，借鉴 IQS 中 FIND_DEPT_CODE 字段风格',
  `find_dept_name` varchar(200) comment '发现单位名称，借鉴 IQS 中 FIND_DEPT_NAME 字段风格',
  `doc_status` varchar(50) comment '归零状态或单据状态，借鉴 IQS 中 DOC_STATUS 和 BILL_STATUS 字段风格',
  `secret_level` varchar(20) comment '密级代码，借鉴 IQS 中 SECRET_LEVEL 字段风格',
  `start_time` datetime comment '流程开始时间，借鉴 IQS 中 STARTTIME 字段风格',
  `finish_time` datetime comment '流程结束时间，借鉴 IQS 中 FINISHTIME 字段风格'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='IQS 不合格通知单业务单据表，参考 zong.docx 中 IQS_FAILURE 真实字段风格；本表对象外键为单据级快捷追溯字段，多对象影响以 quality_event_subject 为准';

create table `p1p_dossier_iqs_failure_content` (
  `iqs_failure_content_id` bigint not null auto_increment primary key comment '不合格通知单内容主键',
  `iqs_failure_id` bigint not null comment '所属不合格通知单主键',
  `start_piece_no` int comment '起始件号，借鉴 IQS_FAILURE_CONTENT 中 START_PIECE_NO 字段风格',
  `end_piece_no` int comment '终止件号，借鉴 IQS_FAILURE_CONTENT 中 END_PIECE_NO 字段风格',
  `failure_desc` text not null comment '不合格情况描述，借鉴 IQS 中 FAILURE_DESC 字段风格',
  `failure_place_brief` text comment '故障部位简要说明，借鉴 IQS 中 FAILURE_PLACE_BRIEF 字段风格',
  `find_type` varchar(80) comment '发现方式，借鉴 IQS 中 FIND_TYPE 字段风格',
  `find_date` datetime comment '发现时间，借鉴 IQS 中 FIND_DATE 字段风格',
  `finder_type` varchar(80) comment '发现人类别，借鉴 IQS 中 FINDER_TYPE 字段风格',
  `find_user_code` varchar(100) comment '发现人编号，借鉴 IQS 中 FIND_USER_CODE 字段风格',
  `find_user_name` varchar(100) comment '发现人姓名',
  `duty_worksec_code` varchar(100) comment '责任工段编码，借鉴 IQS 中 DUTY_WORKSEC_CODE 字段风格',
  `duty_worksec_name` varchar(200) comment '责任工段名称，借鉴 IQS 中 DUTY_WORKSEC_NAME 字段风格',
  `duty_dept_code` varchar(100) comment '责任单位编码，借鉴 IQS 中 DUTY_DEPT_CODE 字段风格',
  `duty_dept_name` varchar(200) comment '责任单位名称，借鉴 IQS 中 DUTY_DEPT_NAME 字段风格',
  `actual_method` text comment '实际处理情况，借鉴 IQS 中 ACTUAL_METHOD 字段风格',
  `doc_no` varchar(100) comment '衍生单据号，借鉴 IQS 中 DOC_NO 字段风格'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='不合格通知单内容表，记录问题描述、发现方式、责任单位和实际处理情况';

create table `p1p_dossier_iqs_failure_duty` (
  `iqs_failure_duty_id` bigint not null auto_increment primary key comment '不合格责任分配主键',
  `iqs_failure_id` bigint not null comment '所属不合格通知单主键',
  `iqs_failure_content_id` bigint comment '关联不合格内容主键',
  `duty_personnel_id` bigint comment '责任人员主键',
  `supplier_id` bigint comment '责任供应商主键',
  `duty_dept_code` varchar(100) comment '责任单位编码，借鉴 IQS_FAILURE_DUTY 中 DUTY_DEPT_CODE 字段风格',
  `duty_dept_name` varchar(200) comment '责任单位名称，借鉴 IQS_FAILURE_DUTY 中 DUTY_DEPT_NAME 字段风格',
  `duty_user_code` varchar(100) comment '责任者编码，借鉴 IQS_FAILURE_DUTY 中 DUTY_USER_CODE 字段风格',
  `duty_user_name` varchar(100) comment '责任者姓名，借鉴 IQS_FAILURE_DUTY 中 DUTY_USER_NAME 字段风格',
  `duty_person_ratio` decimal(5,2) comment '责任人百分比，借鉴 IQS_FAILURE_DUTY 中 DUTY_PERSON_RATIO 字段风格',
  `responsibility_date` datetime comment '责任认定时间'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='不合格责任分配表，参考 zong.docx 中 IQS_FAILURE_DUTY 真实字段风格';

create table `p1p_dossier_disposition_record` (
  `disposition_record_id` bigint not null auto_increment primary key comment '处置记录主键，算法 Beneficiary 表之一',
  `quality_event_id` bigint not null comment '关联质量事件主键，作为处置记录的规范事件入口',
  `iqs_failure_id` bigint comment '关联不合格通知单主键；无不合格单的参数异常处置可为空',
  `iqs_failure_content_id` bigint comment '关联不合格内容主键',
  `work_order_id` bigint comment '处置关联工单主键',
  `responsible_personnel_id` bigint comment '处置责任人员主键',
  `disposition_no` varchar(100) not null comment '处置单号',
  `deal_type_find` varchar(80) comment '发现单位处置方式，借鉴 IQS 中 DEAL_TYPE_FIND 字段风格',
  `deal_type_pass` varchar(80) comment '检验组处置方式，借鉴 IQS 中 DEAL_TYPE_PASS 字段风格',
  `deal_type_tech` varchar(80) comment '技术人员处置方式，借鉴 IQS 中 DEAL_TYPE_TECH 字段风格',
  `disposition_type` varchar(80) comment '处置类型，例如返工、返修、报废、让步接收、退货',
  `disposition_desc` text comment '处置说明',
  `disposition_date` datetime comment '处置时间',
  `disposition_status` varchar(50) comment '处置状态'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='质量事件或不合格通知单的处置记录表，可覆盖参数异常和检验不合格后的处置闭环';

create table `p1p_dossier_reinspection_record` (
  `reinspection_record_id` bigint not null auto_increment primary key comment '复检记录主键',
  `disposition_record_id` bigint not null comment '来源处置记录主键',
  `inspection_record_id` bigint comment '对应复检检验记录主键',
  `inspector_id` bigint comment '复检人员主键',
  `reinspection_no` varchar(100) not null comment '复检编号',
  `reinspection_date` datetime comment '复检时间',
  `reinspection_result` varchar(50) comment '复检结论',
  `conclusion_desc` text comment '复检结论说明'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='处置后的再次检验记录表';

create table `p1p_dossier_maintenance_order` (
  `maintenance_order_id` bigint not null auto_increment primary key comment '维修工单主键',
  `maintenance_event_id` bigint not null comment '所属维修事件主键',
  `component_instance_id` bigint comment '维修组件实例主键',
  `work_order_id` bigint comment '关联统一工单主键',
  `maintenance_order_no` varchar(100) not null comment '维修工单编号',
  `task_name` varchar(200) comment '维修任务名称，参考 Sys_Task 中 taskname 字段语义',
  `plan_start_time` datetime comment '计划开始时间',
  `plan_finish_time` datetime comment '计划完成时间',
  `order_status` varchar(50) comment '维修工单状态',
  `report_person_name` varchar(100) comment '提交人'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='维修事件触发的维修工单表';

create table `p1p_dossier_fault_record` (
  `fault_record_id` bigint not null auto_increment primary key comment '故障记录主键',
  `maintenance_event_id` bigint not null comment '所属维修事件主键',
  `component_instance_id` bigint comment '故障涉及组件实例主键',
  `fault_code` varchar(100) not null comment '故障代码',
  `fault_name` varchar(200) comment '故障名称',
  `fault_desc` text comment '故障描述',
  `fault_position` varchar(200) comment '故障部位',
  `find_date` datetime comment '故障发现时间',
  `finder_name` varchar(100) comment '发现人姓名',
  `severity_level` varchar(50) comment '严重程度'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='服役维修阶段发现的故障现象记录表';

create table `p1p_dossier_replacement_record` (
  `replacement_record_id` bigint not null auto_increment primary key comment '拆换记录主键，算法 Beneficiary 表之一',
  `maintenance_event_id` bigint not null comment '所属维修事件主键',
  `maintenance_order_id` bigint comment '所属维修工单主键',
  `component_instance_id` bigint comment '拆换所在组件实例主键',
  `installed_position_id` bigint comment '拆换发生装机位置主键',
  `removed_part_instance_id` bigint comment '拆下零件实例主键',
  `installed_part_instance_id` bigint comment '换上零件实例主键',
  `inventory_batch_id` bigint comment '换上件来源库存批次主键',
  `replacement_no` varchar(100) not null comment '拆换记录编号',
  `replacement_reason` varchar(200) comment '拆换原因，例如内泄漏、外泄漏、活塞杆划伤',
  `replacement_time` datetime comment '拆换时间',
  `operator_name` varchar(100) comment '拆换操作人姓名',
  `replacement_status` varchar(50) comment '拆换状态'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='维修过程中拆下或换上作动筒零件的记录表';

create table `p1p_dossier_service_feedback` (
  `service_feedback_id` bigint not null auto_increment primary key comment '服役反馈主键',
  `quality_event_id` bigint comment '转化形成的质量事件主键',
  `maintenance_event_id` bigint comment '关联维修事件主键',
  `component_instance_id` bigint comment '反馈涉及组件实例主键',
  `iqs_failure_id` bigint comment '转化形成的不合格通知单主键',
  `feedback_no` varchar(100) not null comment '服役反馈编号',
  `feedback_source` varchar(100) comment '反馈来源，例如用户、维修单位、外场',
  `feedback_date` datetime comment '反馈时间',
  `feedback_desc` text comment '反馈内容',
  `feedback_status` varchar(50) comment '反馈状态'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='用户、维修单位或外场反馈的作动筒组件质量信息表';

create table `p1p_dossier_quality_event_subject` (
  `quality_event_subject_id` bigint not null auto_increment primary key comment '质量事件关联对象主键',
  `quality_event_id` bigint not null comment '所属质量事件主键',
  `component_instance_id` bigint comment '关联组件实例主键',
  `part_instance_id` bigint comment '关联零件实例主键',
  `production_batch_id` bigint comment '关联生产批次主键',
  `inventory_batch_id` bigint comment '关联库存批次主键',
  `supplier_batch_id` bigint comment '关联供应商批次主键',
  `work_order_id` bigint comment '关联工单主键',
  `operation_execution_id` bigint comment '关联工序执行主键',
  `step_execution_id` bigint comment '关联工步执行主键',
  `process_parameter_record_id` bigint comment '直接触发或关联异常的过程参数记录主键',
  `inspection_record_id` bigint comment '关联检验记录主键',
  `defect_record_id` bigint comment '关联缺陷记录主键',
  `purchase_order_id` bigint comment '关联采购订单主键',
  `supplier_id` bigint comment '关联供应商主键',
  `maintenance_event_id` bigint comment '关联维修事件主键',
  `replacement_record_id` bigint comment '关联拆换记录主键',
  `service_feedback_id` bigint comment '关联服役反馈主键',
  `subject_type` varchar(80) not null comment '关联对象类型，例如组件、零件、批次、工单、参数记录、维修事件',
  `subject_role` varchar(80) comment '对象在事件中的角色，例如触发对象、受影响对象、来源对象、处置对象',
  `impact_desc` text comment '影响说明',
  `created_at` datetime comment '创建时间'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_general_ci comment='质量事件与受影响业务对象的多对多关联表，用于表达一个事件影响多个零件、批次、工单或过程参数记录';

-- Foreign keys
alter table `p1p_dossier_landing_gear_system` add constraint `fk_p1p_dossier_landing_gear_system_upper_equipment_id` foreign key (`upper_equipment_id`) references `p1p_dossier_upper_equipment` (`upper_equipment_id`);
alter table `p1p_dossier_installed_position` add constraint `fk_p1p_dossier_installed_position_upper_equipment_id` foreign key (`upper_equipment_id`) references `p1p_dossier_upper_equipment` (`upper_equipment_id`);
alter table `p1p_dossier_installed_position` add constraint `fk_p1p_dossier_installed_position_landing_gear_system_id` foreign key (`landing_gear_system_id`) references `p1p_dossier_landing_gear_system` (`landing_gear_system_id`);
alter table `p1p_dossier_part_definition` add constraint `fk_p1p_dossier_part_definition_material_id` foreign key (`material_id`) references `p1p_dossier_material` (`material_id`);
alter table `p1p_dossier_part_definition` add constraint `fk_p1p_dossier_part_definition_design_version_id` foreign key (`design_version_id`) references `p1p_dossier_design_version` (`design_version_id`);
alter table `p1p_dossier_component_instance` add constraint `fk_p1p_dossier_component_instance_component_type_id` foreign key (`component_type_id`) references `p1p_dossier_component_type` (`component_type_id`);
alter table `p1p_dossier_component_instance` add constraint `fk_p1p_dossier_component_instance_installed_position_id` foreign key (`installed_position_id`) references `p1p_dossier_installed_position` (`installed_position_id`);
alter table `p1p_dossier_component_instance` add constraint `fk_p1p_dossier_component_instance_design_version_id` foreign key (`design_version_id`) references `p1p_dossier_design_version` (`design_version_id`);
alter table `p1p_dossier_bom_item` add constraint `fk_p1p_dossier_bom_item_component_type_id` foreign key (`component_type_id`) references `p1p_dossier_component_type` (`component_type_id`);
alter table `p1p_dossier_bom_item` add constraint `fk_p1p_dossier_bom_item_parent_bom_item_id` foreign key (`parent_bom_item_id`) references `p1p_dossier_bom_item` (`bom_item_id`);
alter table `p1p_dossier_bom_item` add constraint `fk_p1p_dossier_bom_item_part_definition_id` foreign key (`part_definition_id`) references `p1p_dossier_part_definition` (`part_definition_id`);
alter table `p1p_dossier_process_route` add constraint `fk_p1p_dossier_process_route_component_type_id` foreign key (`component_type_id`) references `p1p_dossier_component_type` (`component_type_id`);
alter table `p1p_dossier_process_route` add constraint `fk_p1p_dossier_process_route_part_definition_id` foreign key (`part_definition_id`) references `p1p_dossier_part_definition` (`part_definition_id`);
alter table `p1p_dossier_process_route` add constraint `fk_p1p_dossier_process_route_design_version_id` foreign key (`design_version_id`) references `p1p_dossier_design_version` (`design_version_id`);
alter table `p1p_dossier_operation_definition` add constraint `fk_p1p_dossier_operation_definition_process_route_id` foreign key (`process_route_id`) references `p1p_dossier_process_route` (`process_route_id`);
alter table `p1p_dossier_parameter_standard` add constraint `fk_p1p_dossier_parameter_standard_operation_definition_id` foreign key (`operation_definition_id`) references `p1p_dossier_operation_definition` (`operation_definition_id`);
alter table `p1p_dossier_inspection_spec` add constraint `fk_p1p_dossier_inspection_spec_operation_definition_id` foreign key (`operation_definition_id`) references `p1p_dossier_operation_definition` (`operation_definition_id`);
alter table `p1p_dossier_inspection_spec` add constraint `fk_p1p_dossier_inspection_spec_component_type_id` foreign key (`component_type_id`) references `p1p_dossier_component_type` (`component_type_id`);
alter table `p1p_dossier_inspection_spec` add constraint `fk_p1p_dossier_inspection_spec_part_definition_id` foreign key (`part_definition_id`) references `p1p_dossier_part_definition` (`part_definition_id`);
alter table `p1p_dossier_purchase_order` add constraint `fk_p1p_dossier_purchase_order_supplier_id` foreign key (`supplier_id`) references `p1p_dossier_supplier` (`supplier_id`);
alter table `p1p_dossier_purchase_order_line` add constraint `fk_p1p_dossier_purchase_order_line_purchase_order_id` foreign key (`purchase_order_id`) references `p1p_dossier_purchase_order` (`purchase_order_id`);
alter table `p1p_dossier_purchase_order_line` add constraint `fk_p1p_dossier_purchase_order_line_material_id` foreign key (`material_id`) references `p1p_dossier_material` (`material_id`);
alter table `p1p_dossier_supplier_batch` add constraint `fk_p1p_dossier_supplier_batch_purchase_order_line_id` foreign key (`purchase_order_line_id`) references `p1p_dossier_purchase_order_line` (`purchase_order_line_id`);
alter table `p1p_dossier_supplier_batch` add constraint `fk_p1p_dossier_supplier_batch_supplier_id` foreign key (`supplier_id`) references `p1p_dossier_supplier` (`supplier_id`);
alter table `p1p_dossier_supplier_batch` add constraint `fk_p1p_dossier_supplier_batch_material_id` foreign key (`material_id`) references `p1p_dossier_material` (`material_id`);
alter table `p1p_dossier_inventory_batch` add constraint `fk_p1p_dossier_inventory_batch_supplier_batch_id` foreign key (`supplier_batch_id`) references `p1p_dossier_supplier_batch` (`supplier_batch_id`);
alter table `p1p_dossier_inventory_batch` add constraint `fk_p1p_dossier_inventory_batch_material_id` foreign key (`material_id`) references `p1p_dossier_material` (`material_id`);
alter table `p1p_dossier_receiving_record` add constraint `fk_p1p_dossier_receiving_record_inventory_batch_id` foreign key (`inventory_batch_id`) references `p1p_dossier_inventory_batch` (`inventory_batch_id`);
alter table `p1p_dossier_receiving_record` add constraint `fk_p1p_dossier_receiving_record_purchase_order_line_id` foreign key (`purchase_order_line_id`) references `p1p_dossier_purchase_order_line` (`purchase_order_line_id`);
alter table `p1p_dossier_issue_record` add constraint `fk_p1p_dossier_issue_record_inventory_batch_id` foreign key (`inventory_batch_id`) references `p1p_dossier_inventory_batch` (`inventory_batch_id`);
alter table `p1p_dossier_issue_record` add constraint `fk_p1p_dossier_issue_record_work_order_id` foreign key (`work_order_id`) references `p1p_dossier_work_order` (`work_order_id`);
alter table `p1p_dossier_work_order` add constraint `fk_p1p_dossier_work_order_component_instance_id` foreign key (`component_instance_id`) references `p1p_dossier_component_instance` (`component_instance_id`);
alter table `p1p_dossier_work_order` add constraint `fk_p1p_dossier_work_order_part_definition_id` foreign key (`part_definition_id`) references `p1p_dossier_part_definition` (`part_definition_id`);
alter table `p1p_dossier_work_order` add constraint `fk_p1p_dossier_work_order_process_route_id` foreign key (`process_route_id`) references `p1p_dossier_process_route` (`process_route_id`);
alter table `p1p_dossier_production_batch` add constraint `fk_p1p_dossier_production_batch_work_order_id` foreign key (`work_order_id`) references `p1p_dossier_work_order` (`work_order_id`);
alter table `p1p_dossier_production_batch` add constraint `fk_p1p_dossier_production_batch_part_definition_id` foreign key (`part_definition_id`) references `p1p_dossier_part_definition` (`part_definition_id`);
alter table `p1p_dossier_part_instance` add constraint `fk_p1p_dossier_part_instance_part_definition_id` foreign key (`part_definition_id`) references `p1p_dossier_part_definition` (`part_definition_id`);
alter table `p1p_dossier_part_instance` add constraint `fk_p1p_dossier_part_instance_production_batch_id` foreign key (`production_batch_id`) references `p1p_dossier_production_batch` (`production_batch_id`);
alter table `p1p_dossier_part_instance` add constraint `fk_p1p_dossier_part_instance_inventory_batch_id` foreign key (`inventory_batch_id`) references `p1p_dossier_inventory_batch` (`inventory_batch_id`);
alter table `p1p_dossier_part_instance` add constraint `fk_p1p_dossier_part_instance_supplier_batch_id` foreign key (`supplier_batch_id`) references `p1p_dossier_supplier_batch` (`supplier_batch_id`);
alter table `p1p_dossier_component_part_installation` add constraint `fk_p1p_dossier_component_part_installation_component_instance_id` foreign key (`component_instance_id`) references `p1p_dossier_component_instance` (`component_instance_id`);
alter table `p1p_dossier_component_part_installation` add constraint `fk_p1p_dossier_component_part_installation_part_instance_id` foreign key (`part_instance_id`) references `p1p_dossier_part_instance` (`part_instance_id`);
alter table `p1p_dossier_component_part_installation` add constraint `fk_p1p_dossier_component_part_installation_bom_item_id` foreign key (`bom_item_id`) references `p1p_dossier_bom_item` (`bom_item_id`);
alter table `p1p_dossier_component_part_installation` add constraint `fk_p1p_dossier_component_part_installation_work_order_id` foreign key (`work_order_id`) references `p1p_dossier_work_order` (`work_order_id`);
alter table `p1p_dossier_component_part_installation` add constraint `fk_p1p_dossier_component_part_installation_installed_position_id` foreign key (`installed_position_id`) references `p1p_dossier_installed_position` (`installed_position_id`);
alter table `p1p_dossier_component_part_installation` add constraint `fk_p1p_dossier_component_part_installation_replacement_record_id` foreign key (`replacement_record_id`) references `p1p_dossier_replacement_record` (`replacement_record_id`);
alter table `p1p_dossier_equipment` add constraint `fk_p1p_dossier_equipment_workstation_id` foreign key (`workstation_id`) references `p1p_dossier_workstation` (`workstation_id`);
alter table `p1p_dossier_operation_execution` add constraint `fk_p1p_dossier_operation_execution_work_order_id` foreign key (`work_order_id`) references `p1p_dossier_work_order` (`work_order_id`);
alter table `p1p_dossier_operation_execution` add constraint `fk_p1p_dossier_operation_execution_operation_definition_id` foreign key (`operation_definition_id`) references `p1p_dossier_operation_definition` (`operation_definition_id`);
alter table `p1p_dossier_operation_execution` add constraint `fk_p1p_dossier_operation_execution_component_instance_id` foreign key (`component_instance_id`) references `p1p_dossier_component_instance` (`component_instance_id`);
alter table `p1p_dossier_operation_execution` add constraint `fk_p1p_dossier_operation_execution_part_instance_id` foreign key (`part_instance_id`) references `p1p_dossier_part_instance` (`part_instance_id`);
alter table `p1p_dossier_operation_execution` add constraint `fk_p1p_dossier_operation_execution_workstation_id` foreign key (`workstation_id`) references `p1p_dossier_workstation` (`workstation_id`);
alter table `p1p_dossier_operation_execution` add constraint `fk_p1p_dossier_operation_execution_equipment_id` foreign key (`equipment_id`) references `p1p_dossier_equipment` (`equipment_id`);
alter table `p1p_dossier_step_execution` add constraint `fk_p1p_dossier_step_execution_operation_execution_id` foreign key (`operation_execution_id`) references `p1p_dossier_operation_execution` (`operation_execution_id`);
alter table `p1p_dossier_resource_usage` add constraint `fk_p1p_dossier_resource_usage_operation_execution_id` foreign key (`operation_execution_id`) references `p1p_dossier_operation_execution` (`operation_execution_id`);
alter table `p1p_dossier_resource_usage` add constraint `fk_p1p_dossier_resource_usage_step_execution_id` foreign key (`step_execution_id`) references `p1p_dossier_step_execution` (`step_execution_id`);
alter table `p1p_dossier_resource_usage` add constraint `fk_p1p_dossier_resource_usage_equipment_id` foreign key (`equipment_id`) references `p1p_dossier_equipment` (`equipment_id`);
alter table `p1p_dossier_resource_usage` add constraint `fk_p1p_dossier_resource_usage_personnel_id` foreign key (`personnel_id`) references `p1p_dossier_personnel` (`personnel_id`);
alter table `p1p_dossier_resource_usage` add constraint `fk_p1p_dossier_resource_usage_workstation_id` foreign key (`workstation_id`) references `p1p_dossier_workstation` (`workstation_id`);
alter table `p1p_dossier_resource_usage` add constraint `fk_p1p_dossier_resource_usage_tooling_id` foreign key (`tooling_id`) references `p1p_dossier_tooling` (`tooling_id`);
alter table `p1p_dossier_process_parameter_record` add constraint `fk_p1p_dossier_process_parameter_record_operation_execution_id` foreign key (`operation_execution_id`) references `p1p_dossier_operation_execution` (`operation_execution_id`);
alter table `p1p_dossier_process_parameter_record` add constraint `fk_p1p_dossier_process_parameter_record_step_execution_id` foreign key (`step_execution_id`) references `p1p_dossier_step_execution` (`step_execution_id`);
alter table `p1p_dossier_process_parameter_record` add constraint `fk_p1p_dossier_process_parameter_record_parameter_standard_id` foreign key (`parameter_standard_id`) references `p1p_dossier_parameter_standard` (`parameter_standard_id`);
alter table `p1p_dossier_process_parameter_record` add constraint `fk_p1p_dossier_process_parameter_record_equipment_id` foreign key (`equipment_id`) references `p1p_dossier_equipment` (`equipment_id`);
alter table `p1p_dossier_inspection_record` add constraint `fk_p1p_dossier_inspection_record_inspection_spec_id` foreign key (`inspection_spec_id`) references `p1p_dossier_inspection_spec` (`inspection_spec_id`);
alter table `p1p_dossier_inspection_record` add constraint `fk_p1p_dossier_inspection_record_component_instance_id` foreign key (`component_instance_id`) references `p1p_dossier_component_instance` (`component_instance_id`);
alter table `p1p_dossier_inspection_record` add constraint `fk_p1p_dossier_inspection_record_part_instance_id` foreign key (`part_instance_id`) references `p1p_dossier_part_instance` (`part_instance_id`);
alter table `p1p_dossier_inspection_record` add constraint `fk_p1p_dossier_inspection_record_operation_execution_id` foreign key (`operation_execution_id`) references `p1p_dossier_operation_execution` (`operation_execution_id`);
alter table `p1p_dossier_inspection_record` add constraint `fk_p1p_dossier_inspection_record_inventory_batch_id` foreign key (`inventory_batch_id`) references `p1p_dossier_inventory_batch` (`inventory_batch_id`);
alter table `p1p_dossier_inspection_record` add constraint `fk_p1p_dossier_inspection_record_work_order_id` foreign key (`work_order_id`) references `p1p_dossier_work_order` (`work_order_id`);
alter table `p1p_dossier_inspection_record` add constraint `fk_p1p_dossier_inspection_record_inspector_id` foreign key (`inspector_id`) references `p1p_dossier_personnel` (`personnel_id`);
alter table `p1p_dossier_inspection_item_result` add constraint `fk_p1p_dossier_inspection_item_result_inspection_record_id` foreign key (`inspection_record_id`) references `p1p_dossier_inspection_record` (`inspection_record_id`);
alter table `p1p_dossier_inspection_item_result` add constraint `fk_p1p_dossier_inspection_item_result_inspection_spec_id` foreign key (`inspection_spec_id`) references `p1p_dossier_inspection_spec` (`inspection_spec_id`);
alter table `p1p_dossier_defect_record` add constraint `fk_p1p_dossier_defect_record_inspection_record_id` foreign key (`inspection_record_id`) references `p1p_dossier_inspection_record` (`inspection_record_id`);
alter table `p1p_dossier_defect_record` add constraint `fk_p1p_dossier_defect_record_component_instance_id` foreign key (`component_instance_id`) references `p1p_dossier_component_instance` (`component_instance_id`);
alter table `p1p_dossier_defect_record` add constraint `fk_p1p_dossier_defect_record_part_instance_id` foreign key (`part_instance_id`) references `p1p_dossier_part_instance` (`part_instance_id`);
alter table `p1p_dossier_defect_record` add constraint `fk_p1p_dossier_defect_record_operation_execution_id` foreign key (`operation_execution_id`) references `p1p_dossier_operation_execution` (`operation_execution_id`);
alter table `p1p_dossier_maintenance_event` add constraint `fk_p1p_dossier_maintenance_event_component_instance_id` foreign key (`component_instance_id`) references `p1p_dossier_component_instance` (`component_instance_id`);
alter table `p1p_dossier_maintenance_event` add constraint `fk_p1p_dossier_maintenance_event_installed_position_id` foreign key (`installed_position_id`) references `p1p_dossier_installed_position` (`installed_position_id`);
alter table `p1p_dossier_iqs_failure` add constraint `fk_p1p_dossier_iqs_failure_quality_event_id` foreign key (`quality_event_id`) references `p1p_dossier_quality_event` (`quality_event_id`);
alter table `p1p_dossier_iqs_failure` add constraint `fk_p1p_dossier_iqs_failure_inspection_record_id` foreign key (`inspection_record_id`) references `p1p_dossier_inspection_record` (`inspection_record_id`);
alter table `p1p_dossier_iqs_failure` add constraint `fk_p1p_dossier_iqs_failure_defect_record_id` foreign key (`defect_record_id`) references `p1p_dossier_defect_record` (`defect_record_id`);
alter table `p1p_dossier_iqs_failure` add constraint `fk_p1p_dossier_iqs_failure_component_instance_id` foreign key (`component_instance_id`) references `p1p_dossier_component_instance` (`component_instance_id`);
alter table `p1p_dossier_iqs_failure` add constraint `fk_p1p_dossier_iqs_failure_part_instance_id` foreign key (`part_instance_id`) references `p1p_dossier_part_instance` (`part_instance_id`);
alter table `p1p_dossier_iqs_failure` add constraint `fk_p1p_dossier_iqs_failure_work_order_id` foreign key (`work_order_id`) references `p1p_dossier_work_order` (`work_order_id`);
alter table `p1p_dossier_iqs_failure` add constraint `fk_p1p_dossier_iqs_failure_operation_execution_id` foreign key (`operation_execution_id`) references `p1p_dossier_operation_execution` (`operation_execution_id`);
alter table `p1p_dossier_iqs_failure` add constraint `fk_p1p_dossier_iqs_failure_supplier_id` foreign key (`supplier_id`) references `p1p_dossier_supplier` (`supplier_id`);
alter table `p1p_dossier_iqs_failure` add constraint `fk_p1p_dossier_iqs_failure_maintenance_event_id` foreign key (`maintenance_event_id`) references `p1p_dossier_maintenance_event` (`maintenance_event_id`);
alter table `p1p_dossier_iqs_failure_content` add constraint `fk_p1p_dossier_iqs_failure_content_iqs_failure_id` foreign key (`iqs_failure_id`) references `p1p_dossier_iqs_failure` (`iqs_failure_id`);
alter table `p1p_dossier_iqs_failure_duty` add constraint `fk_p1p_dossier_iqs_failure_duty_iqs_failure_id` foreign key (`iqs_failure_id`) references `p1p_dossier_iqs_failure` (`iqs_failure_id`);
alter table `p1p_dossier_iqs_failure_duty` add constraint `fk_p1p_dossier_iqs_failure_duty_iqs_failure_content_id` foreign key (`iqs_failure_content_id`) references `p1p_dossier_iqs_failure_content` (`iqs_failure_content_id`);
alter table `p1p_dossier_iqs_failure_duty` add constraint `fk_p1p_dossier_iqs_failure_duty_duty_personnel_id` foreign key (`duty_personnel_id`) references `p1p_dossier_personnel` (`personnel_id`);
alter table `p1p_dossier_iqs_failure_duty` add constraint `fk_p1p_dossier_iqs_failure_duty_supplier_id` foreign key (`supplier_id`) references `p1p_dossier_supplier` (`supplier_id`);
alter table `p1p_dossier_disposition_record` add constraint `fk_p1p_dossier_disposition_record_quality_event_id` foreign key (`quality_event_id`) references `p1p_dossier_quality_event` (`quality_event_id`);
alter table `p1p_dossier_disposition_record` add constraint `fk_p1p_dossier_disposition_record_iqs_failure_id` foreign key (`iqs_failure_id`) references `p1p_dossier_iqs_failure` (`iqs_failure_id`);
alter table `p1p_dossier_disposition_record` add constraint `fk_p1p_dossier_disposition_record_iqs_failure_content_id` foreign key (`iqs_failure_content_id`) references `p1p_dossier_iqs_failure_content` (`iqs_failure_content_id`);
alter table `p1p_dossier_disposition_record` add constraint `fk_p1p_dossier_disposition_record_work_order_id` foreign key (`work_order_id`) references `p1p_dossier_work_order` (`work_order_id`);
alter table `p1p_dossier_disposition_record` add constraint `fk_p1p_dossier_disposition_record_responsible_personnel_id` foreign key (`responsible_personnel_id`) references `p1p_dossier_personnel` (`personnel_id`);
alter table `p1p_dossier_reinspection_record` add constraint `fk_p1p_dossier_reinspection_record_disposition_record_id` foreign key (`disposition_record_id`) references `p1p_dossier_disposition_record` (`disposition_record_id`);
alter table `p1p_dossier_reinspection_record` add constraint `fk_p1p_dossier_reinspection_record_inspection_record_id` foreign key (`inspection_record_id`) references `p1p_dossier_inspection_record` (`inspection_record_id`);
alter table `p1p_dossier_reinspection_record` add constraint `fk_p1p_dossier_reinspection_record_inspector_id` foreign key (`inspector_id`) references `p1p_dossier_personnel` (`personnel_id`);
alter table `p1p_dossier_maintenance_order` add constraint `fk_p1p_dossier_maintenance_order_maintenance_event_id` foreign key (`maintenance_event_id`) references `p1p_dossier_maintenance_event` (`maintenance_event_id`);
alter table `p1p_dossier_maintenance_order` add constraint `fk_p1p_dossier_maintenance_order_component_instance_id` foreign key (`component_instance_id`) references `p1p_dossier_component_instance` (`component_instance_id`);
alter table `p1p_dossier_maintenance_order` add constraint `fk_p1p_dossier_maintenance_order_work_order_id` foreign key (`work_order_id`) references `p1p_dossier_work_order` (`work_order_id`);
alter table `p1p_dossier_fault_record` add constraint `fk_p1p_dossier_fault_record_maintenance_event_id` foreign key (`maintenance_event_id`) references `p1p_dossier_maintenance_event` (`maintenance_event_id`);
alter table `p1p_dossier_fault_record` add constraint `fk_p1p_dossier_fault_record_component_instance_id` foreign key (`component_instance_id`) references `p1p_dossier_component_instance` (`component_instance_id`);
alter table `p1p_dossier_replacement_record` add constraint `fk_p1p_dossier_replacement_record_maintenance_event_id` foreign key (`maintenance_event_id`) references `p1p_dossier_maintenance_event` (`maintenance_event_id`);
alter table `p1p_dossier_replacement_record` add constraint `fk_p1p_dossier_replacement_record_maintenance_order_id` foreign key (`maintenance_order_id`) references `p1p_dossier_maintenance_order` (`maintenance_order_id`);
alter table `p1p_dossier_replacement_record` add constraint `fk_p1p_dossier_replacement_record_component_instance_id` foreign key (`component_instance_id`) references `p1p_dossier_component_instance` (`component_instance_id`);
alter table `p1p_dossier_replacement_record` add constraint `fk_p1p_dossier_replacement_record_installed_position_id` foreign key (`installed_position_id`) references `p1p_dossier_installed_position` (`installed_position_id`);
alter table `p1p_dossier_replacement_record` add constraint `fk_p1p_dossier_replacement_record_removed_part_instance_id` foreign key (`removed_part_instance_id`) references `p1p_dossier_part_instance` (`part_instance_id`);
alter table `p1p_dossier_replacement_record` add constraint `fk_p1p_dossier_replacement_record_installed_part_instance_id` foreign key (`installed_part_instance_id`) references `p1p_dossier_part_instance` (`part_instance_id`);
alter table `p1p_dossier_replacement_record` add constraint `fk_p1p_dossier_replacement_record_inventory_batch_id` foreign key (`inventory_batch_id`) references `p1p_dossier_inventory_batch` (`inventory_batch_id`);
alter table `p1p_dossier_service_feedback` add constraint `fk_p1p_dossier_service_feedback_quality_event_id` foreign key (`quality_event_id`) references `p1p_dossier_quality_event` (`quality_event_id`);
alter table `p1p_dossier_service_feedback` add constraint `fk_p1p_dossier_service_feedback_maintenance_event_id` foreign key (`maintenance_event_id`) references `p1p_dossier_maintenance_event` (`maintenance_event_id`);
alter table `p1p_dossier_service_feedback` add constraint `fk_p1p_dossier_service_feedback_component_instance_id` foreign key (`component_instance_id`) references `p1p_dossier_component_instance` (`component_instance_id`);
alter table `p1p_dossier_service_feedback` add constraint `fk_p1p_dossier_service_feedback_iqs_failure_id` foreign key (`iqs_failure_id`) references `p1p_dossier_iqs_failure` (`iqs_failure_id`);
alter table `p1p_dossier_quality_event_subject` add constraint `fk_p1p_dossier_quality_event_subject_quality_event_id` foreign key (`quality_event_id`) references `p1p_dossier_quality_event` (`quality_event_id`);
alter table `p1p_dossier_quality_event_subject` add constraint `fk_p1p_dossier_quality_event_subject_component_instance_id` foreign key (`component_instance_id`) references `p1p_dossier_component_instance` (`component_instance_id`);
alter table `p1p_dossier_quality_event_subject` add constraint `fk_p1p_dossier_quality_event_subject_part_instance_id` foreign key (`part_instance_id`) references `p1p_dossier_part_instance` (`part_instance_id`);
alter table `p1p_dossier_quality_event_subject` add constraint `fk_p1p_dossier_quality_event_subject_production_batch_id` foreign key (`production_batch_id`) references `p1p_dossier_production_batch` (`production_batch_id`);
alter table `p1p_dossier_quality_event_subject` add constraint `fk_p1p_dossier_quality_event_subject_inventory_batch_id` foreign key (`inventory_batch_id`) references `p1p_dossier_inventory_batch` (`inventory_batch_id`);
alter table `p1p_dossier_quality_event_subject` add constraint `fk_p1p_dossier_quality_event_subject_supplier_batch_id` foreign key (`supplier_batch_id`) references `p1p_dossier_supplier_batch` (`supplier_batch_id`);
alter table `p1p_dossier_quality_event_subject` add constraint `fk_p1p_dossier_quality_event_subject_work_order_id` foreign key (`work_order_id`) references `p1p_dossier_work_order` (`work_order_id`);
alter table `p1p_dossier_quality_event_subject` add constraint `fk_p1p_dossier_quality_event_subject_operation_execution_id` foreign key (`operation_execution_id`) references `p1p_dossier_operation_execution` (`operation_execution_id`);
alter table `p1p_dossier_quality_event_subject` add constraint `fk_p1p_dossier_quality_event_subject_step_execution_id` foreign key (`step_execution_id`) references `p1p_dossier_step_execution` (`step_execution_id`);
alter table `p1p_dossier_quality_event_subject` add constraint `fk_p1p_dossier_quality_event_subject_process_parameter_record_id` foreign key (`process_parameter_record_id`) references `p1p_dossier_process_parameter_record` (`process_parameter_record_id`);
alter table `p1p_dossier_quality_event_subject` add constraint `fk_p1p_dossier_quality_event_subject_inspection_record_id` foreign key (`inspection_record_id`) references `p1p_dossier_inspection_record` (`inspection_record_id`);
alter table `p1p_dossier_quality_event_subject` add constraint `fk_p1p_dossier_quality_event_subject_defect_record_id` foreign key (`defect_record_id`) references `p1p_dossier_defect_record` (`defect_record_id`);
alter table `p1p_dossier_quality_event_subject` add constraint `fk_p1p_dossier_quality_event_subject_purchase_order_id` foreign key (`purchase_order_id`) references `p1p_dossier_purchase_order` (`purchase_order_id`);
alter table `p1p_dossier_quality_event_subject` add constraint `fk_p1p_dossier_quality_event_subject_supplier_id` foreign key (`supplier_id`) references `p1p_dossier_supplier` (`supplier_id`);
alter table `p1p_dossier_quality_event_subject` add constraint `fk_p1p_dossier_quality_event_subject_maintenance_event_id` foreign key (`maintenance_event_id`) references `p1p_dossier_maintenance_event` (`maintenance_event_id`);
alter table `p1p_dossier_quality_event_subject` add constraint `fk_p1p_dossier_quality_event_subject_replacement_record_id` foreign key (`replacement_record_id`) references `p1p_dossier_replacement_record` (`replacement_record_id`);
alter table `p1p_dossier_quality_event_subject` add constraint `fk_p1p_dossier_quality_event_subject_service_feedback_id` foreign key (`service_feedback_id`) references `p1p_dossier_service_feedback` (`service_feedback_id`);

-- Source-defined indexes
create unique index `uq_p1p_dossier_standard_dictionary_dict_type_standard_code` on `p1p_dossier_standard_dictionary` (`dict_type`, `standard_code`);
create index `idx_p1p_dossier_standard_dictionary_dict_type` on `p1p_dossier_standard_dictionary` (`dict_type`);
create unique index `uq_p1p_dossier_material_material_code` on `p1p_dossier_material` (`material_code`);
create index `idx_p1p_dossier_material_material_type` on `p1p_dossier_material` (`material_type`);
create unique index `uq_p1p_dossier_part_definition_part_no_design_version_id` on `p1p_dossier_part_definition` (`part_no`, `design_version_id`);
create index `idx_p1p_dossier_part_definition_drawing_code` on `p1p_dossier_part_definition` (`drawing_code`);
create index `idx_p1p_dossier_part_definition_source_type` on `p1p_dossier_part_definition` (`source_type`);
create unique index `uq_p1p_dossier_component_instance_component_serial_no` on `p1p_dossier_component_instance` (`component_serial_no`);
create index `idx_p1p_dossier_component_instance_quality_code` on `p1p_dossier_component_instance` (`quality_code`);
create index `idx_p1p_dossier_component_instance_component_type_id` on `p1p_dossier_component_instance` (`component_type_id`);
create unique index `uq_p1p_dossier_supplier_supplier_code` on `p1p_dossier_supplier` (`supplier_code`);
create index `idx_p1p_dossier_supplier_supplier_type` on `p1p_dossier_supplier` (`supplier_type`);
create unique index `uq_p1p_dossier_purchase_order_order_no` on `p1p_dossier_purchase_order` (`order_no`);
create index `idx_p1p_dossier_purchase_order_supplier_id` on `p1p_dossier_purchase_order` (`supplier_id`);
create index `idx_p1p_dossier_purchase_order_task_code` on `p1p_dossier_purchase_order` (`task_code`);
create unique index `uq_p1p_dossier_work_order_order_no` on `p1p_dossier_work_order` (`order_no`);
create index `idx_p1p_dossier_work_order_task_code` on `p1p_dossier_work_order` (`task_code`);
create index `idx_p1p_dossier_work_order_component_instance_id` on `p1p_dossier_work_order` (`component_instance_id`);
create index `idx_p1p_dossier_work_order_part_definition_id` on `p1p_dossier_work_order` (`part_definition_id`);
create index `idx_p1p_dossier_component_part_installation_component_i_49a4ac21` on `p1p_dossier_component_part_installation` (`component_instance_id`, `part_instance_id`, `valid_from`);
create index `idx_p1p_dossier_component_part_installation_replacement_748347a6` on `p1p_dossier_component_part_installation` (`replacement_record_id`);
create index `idx_p1p_dossier_component_part_installation_work_order_id` on `p1p_dossier_component_part_installation` (`work_order_id`);
create unique index `uq_p1p_dossier_equipment_equipment_no` on `p1p_dossier_equipment` (`equipment_no`);
create index `idx_p1p_dossier_equipment_workstation_id` on `p1p_dossier_equipment` (`workstation_id`);
create unique index `uq_p1p_dossier_personnel_person_no` on `p1p_dossier_personnel` (`person_no`);
create index `idx_p1p_dossier_personnel_dept_code` on `p1p_dossier_personnel` (`dept_code`);
create index `idx_p1p_dossier_operation_execution_work_order_id` on `p1p_dossier_operation_execution` (`work_order_id`);
create index `idx_p1p_dossier_operation_execution_component_instance_id` on `p1p_dossier_operation_execution` (`component_instance_id`);
create index `idx_p1p_dossier_operation_execution_part_instance_id` on `p1p_dossier_operation_execution` (`part_instance_id`);
create index `idx_p1p_dossier_operation_execution_equipment_id` on `p1p_dossier_operation_execution` (`equipment_id`);
create index `idx_p1p_dossier_step_execution_operation_execution_id` on `p1p_dossier_step_execution` (`operation_execution_id`);
create index `idx_p1p_dossier_step_execution_step_no` on `p1p_dossier_step_execution` (`step_no`);
create index `idx_p1p_dossier_resource_usage_operation_execution_id` on `p1p_dossier_resource_usage` (`operation_execution_id`);
create index `idx_p1p_dossier_resource_usage_step_execution_id` on `p1p_dossier_resource_usage` (`step_execution_id`);
create index `idx_p1p_dossier_resource_usage_equipment_id` on `p1p_dossier_resource_usage` (`equipment_id`);
create index `idx_p1p_dossier_resource_usage_personnel_id` on `p1p_dossier_resource_usage` (`personnel_id`);
create index `idx_p1p_dossier_process_parameter_record_operation_execution_id` on `p1p_dossier_process_parameter_record` (`operation_execution_id`);
create index `idx_p1p_dossier_process_parameter_record_step_execution_id` on `p1p_dossier_process_parameter_record` (`step_execution_id`);
create index `idx_p1p_dossier_process_parameter_record_parameter_code` on `p1p_dossier_process_parameter_record` (`parameter_code`);
create index `idx_p1p_dossier_process_parameter_record_judge_result` on `p1p_dossier_process_parameter_record` (`judge_result`);
create unique index `uq_p1p_dossier_inspection_record_inspection_no` on `p1p_dossier_inspection_record` (`inspection_no`);
create index `idx_p1p_dossier_inspection_record_component_instance_id` on `p1p_dossier_inspection_record` (`component_instance_id`);
create index `idx_p1p_dossier_inspection_record_part_instance_id` on `p1p_dossier_inspection_record` (`part_instance_id`);
create index `idx_p1p_dossier_inspection_record_operation_execution_id` on `p1p_dossier_inspection_record` (`operation_execution_id`);
create index `idx_p1p_dossier_inspection_record_work_order_id` on `p1p_dossier_inspection_record` (`work_order_id`);
create index `idx_p1p_dossier_inspection_item_result_inspection_record_id` on `p1p_dossier_inspection_item_result` (`inspection_record_id`);
create index `idx_p1p_dossier_inspection_item_result_item_code` on `p1p_dossier_inspection_item_result` (`item_code`);
create index `idx_p1p_dossier_inspection_item_result_judge_result` on `p1p_dossier_inspection_item_result` (`judge_result`);
create index `idx_p1p_dossier_defect_record_inspection_record_id` on `p1p_dossier_defect_record` (`inspection_record_id`);
create index `idx_p1p_dossier_defect_record_component_instance_id` on `p1p_dossier_defect_record` (`component_instance_id`);
create index `idx_p1p_dossier_defect_record_part_instance_id` on `p1p_dossier_defect_record` (`part_instance_id`);
create index `idx_p1p_dossier_defect_record_operation_execution_id` on `p1p_dossier_defect_record` (`operation_execution_id`);
create index `idx_p1p_dossier_defect_record_defect_code` on `p1p_dossier_defect_record` (`defect_code`);
create unique index `uq_p1p_dossier_maintenance_event_event_no` on `p1p_dossier_maintenance_event` (`event_no`);
create index `idx_p1p_dossier_maintenance_event_component_instance_id` on `p1p_dossier_maintenance_event` (`component_instance_id`);
create index `idx_p1p_dossier_maintenance_event_event_type` on `p1p_dossier_maintenance_event` (`event_type`);
create unique index `uq_p1p_dossier_quality_event_event_no` on `p1p_dossier_quality_event` (`event_no`);
create index `idx_p1p_dossier_quality_event_event_time` on `p1p_dossier_quality_event` (`event_time`);
create index `idx_p1p_dossier_quality_event_event_source_event_type` on `p1p_dossier_quality_event` (`event_source`, `event_type`);
create unique index `uq_p1p_dossier_iqs_failure_iqs_failure_order` on `p1p_dossier_iqs_failure` (`iqs_failure_order`);
create index `idx_p1p_dossier_iqs_failure_quality_event_id` on `p1p_dossier_iqs_failure` (`quality_event_id`);
create index `idx_p1p_dossier_iqs_failure_quality_code` on `p1p_dossier_iqs_failure` (`quality_code`);
create index `idx_p1p_dossier_iqs_failure_doc_source_failure_type` on `p1p_dossier_iqs_failure` (`doc_source`, `failure_type`);
create index `idx_p1p_dossier_iqs_failure_component_instance_id` on `p1p_dossier_iqs_failure` (`component_instance_id`);
create index `idx_p1p_dossier_iqs_failure_part_instance_id` on `p1p_dossier_iqs_failure` (`part_instance_id`);
create index `idx_p1p_dossier_iqs_failure_work_order_id` on `p1p_dossier_iqs_failure` (`work_order_id`);
create index `idx_p1p_dossier_iqs_failure_operation_execution_id` on `p1p_dossier_iqs_failure` (`operation_execution_id`);
create index `idx_p1p_dossier_disposition_record_quality_event_id` on `p1p_dossier_disposition_record` (`quality_event_id`);
create index `idx_p1p_dossier_disposition_record_iqs_failure_id` on `p1p_dossier_disposition_record` (`iqs_failure_id`);
create index `idx_p1p_dossier_disposition_record_work_order_id` on `p1p_dossier_disposition_record` (`work_order_id`);
create index `idx_p1p_dossier_disposition_record_disposition_no` on `p1p_dossier_disposition_record` (`disposition_no`);
create index `idx_p1p_dossier_reinspection_record_disposition_record_id` on `p1p_dossier_reinspection_record` (`disposition_record_id`);
create index `idx_p1p_dossier_reinspection_record_inspection_record_id` on `p1p_dossier_reinspection_record` (`inspection_record_id`);
create index `idx_p1p_dossier_reinspection_record_reinspection_no` on `p1p_dossier_reinspection_record` (`reinspection_no`);
create unique index `uq_p1p_dossier_maintenance_order_maintenance_order_no` on `p1p_dossier_maintenance_order` (`maintenance_order_no`);
create index `idx_p1p_dossier_maintenance_order_maintenance_event_id` on `p1p_dossier_maintenance_order` (`maintenance_event_id`);
create index `idx_p1p_dossier_maintenance_order_component_instance_id` on `p1p_dossier_maintenance_order` (`component_instance_id`);
create index `idx_p1p_dossier_maintenance_order_work_order_id` on `p1p_dossier_maintenance_order` (`work_order_id`);
create index `idx_p1p_dossier_fault_record_maintenance_event_id` on `p1p_dossier_fault_record` (`maintenance_event_id`);
create index `idx_p1p_dossier_fault_record_component_instance_id` on `p1p_dossier_fault_record` (`component_instance_id`);
create index `idx_p1p_dossier_fault_record_fault_code` on `p1p_dossier_fault_record` (`fault_code`);
create unique index `uq_p1p_dossier_replacement_record_replacement_no` on `p1p_dossier_replacement_record` (`replacement_no`);
create index `idx_p1p_dossier_replacement_record_maintenance_event_id` on `p1p_dossier_replacement_record` (`maintenance_event_id`);
create index `idx_p1p_dossier_replacement_record_component_instance_id` on `p1p_dossier_replacement_record` (`component_instance_id`);
create index `idx_p1p_dossier_replacement_record_removed_part_instance_id` on `p1p_dossier_replacement_record` (`removed_part_instance_id`);
create index `idx_p1p_dossier_replacement_record_installed_part_instance_id` on `p1p_dossier_replacement_record` (`installed_part_instance_id`);
create unique index `uq_p1p_dossier_service_feedback_feedback_no` on `p1p_dossier_service_feedback` (`feedback_no`);
create index `idx_p1p_dossier_service_feedback_quality_event_id` on `p1p_dossier_service_feedback` (`quality_event_id`);
create index `idx_p1p_dossier_service_feedback_maintenance_event_id` on `p1p_dossier_service_feedback` (`maintenance_event_id`);
create index `idx_p1p_dossier_service_feedback_component_instance_id` on `p1p_dossier_service_feedback` (`component_instance_id`);
create index `idx_p1p_dossier_quality_event_subject_quality_event_id__b9e6683f` on `p1p_dossier_quality_event_subject` (`quality_event_id`, `subject_type`);
create index `idx_p1p_dossier_quality_event_subject_component_instance_id` on `p1p_dossier_quality_event_subject` (`component_instance_id`);
create index `idx_p1p_dossier_quality_event_subject_part_instance_id` on `p1p_dossier_quality_event_subject` (`part_instance_id`);
create index `idx_p1p_dossier_quality_event_subject_production_batch_id` on `p1p_dossier_quality_event_subject` (`production_batch_id`);
create index `idx_p1p_dossier_quality_event_subject_work_order_id` on `p1p_dossier_quality_event_subject` (`work_order_id`);
create index `idx_p1p_dossier_quality_event_subject_operation_execution_id` on `p1p_dossier_quality_event_subject` (`operation_execution_id`);
create index `idx_p1p_dossier_quality_event_subject_process_parameter_530cc806` on `p1p_dossier_quality_event_subject` (`process_parameter_record_id`);
create index `idx_p1p_dossier_quality_event_subject_inspection_record_id` on `p1p_dossier_quality_event_subject` (`inspection_record_id`);
create index `idx_p1p_dossier_quality_event_subject_maintenance_event_id` on `p1p_dossier_quality_event_subject` (`maintenance_event_id`);
create index `idx_p1p_dossier_quality_event_subject_replacement_record_id` on `p1p_dossier_quality_event_subject` (`replacement_record_id`);

set foreign_key_checks = 1;
