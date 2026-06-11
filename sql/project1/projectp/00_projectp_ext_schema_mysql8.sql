-- projectp CF external source simulation schema.
-- Generated from algorithm-service/algorithm-pwb/data/cf_data/cf/init-scripts/01-05_cf_*_schema.sql
-- Target: RuoYi-Cloud MySQL 8.0.46, database `ry-cloud`.
-- Scope: only simulated external source tables with prefix `p1p_ext_`.

use `ry-cloud`;
set names utf8mb4;
set foreign_key_checks = 0;

-- Drop only projectp simulated external source tables.
drop table if exists `p1p_ext_mro_field_feedback`;
drop table if exists `p1p_ext_mro_remove_install_record`;
drop table if exists `p1p_ext_mro_fault_report`;
drop table if exists `p1p_ext_mro_repair_order`;
drop table if exists `p1p_ext_mro_service_event`;
drop table if exists `p1p_ext_qms_recheck_record`;
drop table if exists `p1p_ext_qms_disposal_order`;
drop table if exists `p1p_ext_qms_iqs_failure_duty`;
drop table if exists `p1p_ext_qms_iqs_failure_content`;
drop table if exists `p1p_ext_qms_iqs_failure`;
drop table if exists `p1p_ext_qms_defect_log`;
drop table if exists `p1p_ext_qms_inspection_item`;
drop table if exists `p1p_ext_qms_inspection_doc`;
drop table if exists `p1p_ext_qms_quality_event_doc`;
drop table if exists `p1p_ext_mes_process_param`;
drop table if exists `p1p_ext_mes_resource_usage`;
drop table if exists `p1p_ext_mes_step_log`;
drop table if exists `p1p_ext_mes_work_log`;
drop table if exists `p1p_ext_mes_tooling_asset`;
drop table if exists `p1p_ext_mes_person_info`;
drop table if exists `p1p_ext_mes_equipment_asset`;
drop table if exists `p1p_ext_mes_sys_station`;
drop table if exists `p1p_ext_mes_assembly_record`;
drop table if exists `p1p_ext_mes_part_track`;
drop table if exists `p1p_ext_mes_batch_card`;
drop table if exists `p1p_ext_mes_sys_task`;
drop table if exists `p1p_ext_erp_issue_bill`;
drop table if exists `p1p_ext_erp_receipt_bill`;
drop table if exists `p1p_ext_erp_inventory_lot`;
drop table if exists `p1p_ext_erp_vendor_batch`;
drop table if exists `p1p_ext_erp_purchase_line`;
drop table if exists `p1p_ext_erp_purchase_order`;
drop table if exists `p1p_ext_erp_vendor_master`;
drop table if exists `p1p_ext_erp_item_master`;
drop table if exists `p1p_ext_plm_inspection_standard`;
drop table if exists `p1p_ext_plm_technical_parameter`;
drop table if exists `p1p_ext_plm_operation_card`;
drop table if exists `p1p_ext_plm_route_card`;
drop table if exists `p1p_ext_plm_bom_line`;
drop table if exists `p1p_ext_plm_bom_header`;
drop table if exists `p1p_ext_plm_part_master`;
drop table if exists `p1p_ext_plm_design_revision`;
drop table if exists `p1p_ext_plm_actuator_model`;
drop table if exists `p1p_ext_plm_gear_system_def`;
drop table if exists `p1p_ext_plm_product_model`;

-- Tables
-- PLM: product_model
create table `p1p_ext_plm_product_model` (
  `product_model_id` bigint not null auto_increment primary key comment '产品型号主键',
  `model_code` varchar(80) not null comment '产品型号编码',
  `model_name` varchar(200) not null comment '产品型号名称',
  `model_series` varchar(80) comment '产品系列代码',
  `product_line` varchar(100) comment '产品线名称',
  `actual_plane_no` varchar(100) comment '实际架次号或试验装机对象编号',
  `model_status_cd` varchar(30) comment '型号状态代码',
  `cert_level_cd` varchar(30) comment '密级或适航级别代码',
  `eff_date_txt` varchar(20) comment '型号生效日期文本',
  `expire_date_txt` varchar(20) comment '型号失效日期文本'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='PLM 产品型号表，描述上级航空装备或试验装机对象的型号主数据';

-- PLM: gear_system_def
create table `p1p_ext_plm_gear_system_def` (
  `gear_system_def_id` bigint not null auto_increment primary key comment '起落架系统定义主键',
  `product_model_id` bigint not null comment '所属产品型号主键',
  `sys_code` varchar(80) not null comment '系统编码',
  `sys_name` varchar(200) not null comment '系统名称',
  `install_zone` varchar(80) comment '安装区域或功能位置代码',
  `station_no` varchar(100) comment '设计站位号或装机站位号',
  `side_mark` varchar(20) comment '安装侧别标识',
  `tech_status_cd` varchar(30) comment '技术状态代码',
  `eff_date_txt` varchar(20) comment '系统生效日期文本'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='起落架系统定义表，记录产品型号下起落架收放系统的功能层级';

-- PLM: actuator_model
create table `p1p_ext_plm_actuator_model` (
  `actuator_model_id` bigint not null auto_increment primary key comment '作动筒型号主键',
  `gear_system_def_id` bigint not null comment '所属起落架系统定义主键',
  `actuator_code` varchar(100) not null comment '作动筒组件型号编码',
  `actuator_name` varchar(200) not null comment '作动筒组件型号名称',
  `actuator_spec` varchar(200) comment '作动筒规格或构型说明',
  `dwg_no` varchar(120) comment '组件图号',
  `config_no` varchar(80) comment '构型编号',
  `model_status_cd` varchar(30) comment '组件型号状态代码',
  `secret_lvl_cd` varchar(20) comment '密级代码',
  `tech_status_text` varchar(100) comment '技术状态文本'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='收放液压作动筒组件型号表，作为设计和 BOM 的核心型号锚点';

-- PLM: design_revision
create table `p1p_ext_plm_design_revision` (
  `design_revision_id` bigint not null auto_increment primary key comment '设计版本主键',
  `actuator_model_id` bigint not null comment '所属作动筒型号主键',
  `rev_no` varchar(60) not null comment '设计版本号',
  `rev_name` varchar(200) comment '设计版本名称',
  `dwg_no` varchar(120) comment '版本对应图号',
  `doc_rev` varchar(50) comment '技术文件版本',
  `issue_status_cd` varchar(30) comment '发布状态代码',
  `eff_date_txt` varchar(20) comment '版本生效日期文本',
  `expire_date_txt` varchar(20) comment '版本失效日期文本',
  `released_by` varchar(80) comment '发布人'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='设计版本表，记录组件图纸、技术文件和构型版本的发布信息';

-- PLM: part_master
create table `p1p_ext_plm_part_master` (
  `part_master_id` bigint not null auto_increment primary key comment '零件主数据主键',
  `design_revision_id` bigint not null comment '适用设计版本主键',
  `part_no` varchar(100) not null comment '零件号',
  `part_cn_name` varchar(200) not null comment '零件中文名称',
  `part_cat_cd` varchar(30) comment '零件类别代码',
  `dwg_no` varchar(120) comment '零件图号',
  `material_no` varchar(100) comment '材料或物料编码',
  `mat_grade` varchar(100) comment '材料牌号',
  `spec_text` varchar(200) comment '材料规格文本',
  `uom` varchar(30) comment '计量单位',
  `make_buy_cd` varchar(20) comment '自制外购代码',
  `key_level_cd` varchar(30) comment '关键等级代码'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='零件主数据表，描述作动筒缸筒、活塞杆、密封件等零件定义';

-- PLM: bom_header
create table `p1p_ext_plm_bom_header` (
  `bom_header_id` bigint not null auto_increment primary key comment 'BOM 主表主键',
  `actuator_model_id` bigint not null comment '所属作动筒型号主键',
  `design_revision_id` bigint not null comment '适用设计版本主键',
  `root_part_master_id` bigint comment '根零件主键',
  `bom_no` varchar(100) not null comment 'BOM 编号',
  `bom_rev` varchar(50) comment 'BOM 版本号',
  `bom_status_cd` varchar(30) comment 'BOM 状态代码',
  `eff_date_txt` varchar(20) comment 'BOM 生效日期文本',
  `end_date_txt` varchar(20) comment 'BOM 失效日期文本'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='BOM 主表，定义作动筒组件在某设计版本下的结构清单';

-- PLM: bom_line
create table `p1p_ext_plm_bom_line` (
  `bom_line_id` bigint not null auto_increment primary key comment 'BOM 明细主键',
  `bom_header_id` bigint not null comment '所属 BOM 主表主键',
  `parent_part_master_id` bigint comment '父级零件主键',
  `child_part_master_id` bigint not null comment '子级零件主键',
  `line_no` int not null comment 'BOM 行号',
  `item_no` varchar(60) not null comment 'BOM 项号',
  `qty_per_txt` varchar(40) not null comment '单台用量文本',
  `uom` varchar(30) comment '用量单位',
  `install_pos_code` varchar(100) comment '装配位置编码',
  `find_no` varchar(80) comment '查找号或位号',
  `alt_group` varchar(60) comment '替代组号',
  `line_status_cd` varchar(30) comment 'BOM 行状态代码'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='BOM 明细表，记录父子零件、用量和装配位置';

-- PLM: route_card
create table `p1p_ext_plm_route_card` (
  `route_card_id` bigint not null auto_increment primary key comment '工艺路线卡主键',
  `actuator_model_id` bigint comment '适用作动筒型号主键',
  `part_master_id` bigint comment '适用零件主键',
  `design_revision_id` bigint not null comment '适用设计版本主键',
  `route_no` varchar(100) not null comment '工艺路线编号',
  `route_name` varchar(200) not null comment '工艺路线名称',
  `route_class_cd` varchar(30) comment '路线类别代码',
  `proc_doc_rev` varchar(50) comment '工艺文件版本',
  `release_state_cd` varchar(30) comment '路线发布状态代码',
  `planned_time_unit` varchar(30) comment '计划工时单位'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='工艺路线卡表，定义作动筒组件或零件制造装配检验的标准流程';

-- PLM: operation_card
create table `p1p_ext_plm_operation_card` (
  `operation_card_id` bigint not null auto_increment primary key comment '工序卡主键',
  `route_card_id` bigint not null comment '所属工艺路线卡主键',
  `op_no` varchar(50) not null comment '工序号',
  `workid` varchar(50) comment '历史工序编码',
  `op_name` varchar(200) not null comment '工序名称',
  `seq_no_txt` varchar(20) comment '工序顺序号文本',
  `std_hours_txt` varchar(40) comment '标准工时文本',
  `resource_type` varchar(100) comment '资源或设备类型',
  `special_proc_cd` varchar(30) comment '特殊过程代码',
  `key_op_flag` varchar(10) comment '关键工序标识'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='工序卡表，记录工艺路线下的标准工序定义';

-- PLM: technical_parameter
create table `p1p_ext_plm_technical_parameter` (
  `technical_parameter_id` bigint not null auto_increment primary key comment '技术参数标准主键',
  `operation_card_id` bigint not null comment '所属工序卡主键',
  `para_code` varchar(100) not null comment '参数编码',
  `para_short_name` varchar(200) not null comment '参数简称或项目名称',
  `unit_txt` varchar(50) comment '参数单位文本',
  `nominal_txt` varchar(80) comment '名义值文本',
  `lsl_txt` varchar(80) comment '下限值文本',
  `usl_txt` varchar(80) comment '上限值文本',
  `ctrl_grade_cd` varchar(30) comment '控制等级代码',
  `spec_expr` varchar(200) comment '规格表达式'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='技术参数标准表，定义试验压力、泄漏量、镀层厚度等工艺参数范围';

-- PLM: inspection_standard
create table `p1p_ext_plm_inspection_standard` (
  `inspection_standard_id` bigint not null auto_increment primary key comment '检验规范主键',
  `operation_card_id` bigint comment '关联工序卡主键',
  `actuator_model_id` bigint comment '适用作动筒型号主键',
  `part_master_id` bigint comment '适用零件主键',
  `insp_spec_no` varchar(100) not null comment '检验规范编号',
  `spec_title` varchar(200) not null comment '检验规范标题',
  `insp_kind_cd` varchar(30) comment '检验类型代码',
  `check_item_code` varchar(100) comment '检验项目编码',
  `check_item_name` varchar(200) comment '检验项目名称',
  `accept_rule` text comment '验收准则',
  `sample_rule_cd` varchar(30) comment '抽样规则代码',
  `result_rule_cd` varchar(30) comment '判定规则代码'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='检验规范表，记录作动筒组件、零件或工序的检验项目与验收规则';

-- ERP: item_master
create table `p1p_ext_erp_item_master` (
  `item_id` bigint not null auto_increment primary key comment '物料主数据内部主键，ERP 单列表主键',
  `item_no` varchar(100) not null comment 'ERP 物料编码，外购件和原材料的业务编号',
  `item_desc` varchar(200) not null comment '物料描述或物料名称',
  `item_grp_cd` varchar(50) comment '物料组代码，ERP 内部分类编码',
  `mat_grade` varchar(100) comment '材料牌号或材质等级',
  `spec_model` varchar(200) comment '规格型号，可能含尺寸和执行标准',
  `uom_cd` varchar(30) comment '库存主计量单位代码',
  `buy_made_flag` varchar(10) comment '采购自制标识，例如 B 表示外购，M 表示自制'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='ERP 物料主数据表，提供作动筒外购件、密封件、原材料和外协件的物料基础信息';

-- ERP: vendor_master
create table `p1p_ext_erp_vendor_master` (
  `vendor_id` bigint not null auto_increment primary key comment '供应商内部主键，ERP 单列表主键',
  `vendor_code` varchar(100) not null comment '供应商编码，ERP 采购业务中的稳定业务键',
  `vendor_name` varchar(200) not null comment '供应商名称',
  `vendor_kind_cd` varchar(30) comment '供应商类别代码，例如材料、密封件、传感器、外协加工',
  `q_level_cd` varchar(30) comment '供应商资质等级代码，ERP 质量准入评级',
  `contact_name` varchar(100) comment '供应商联系人姓名',
  `tel_no` varchar(50) comment '供应商联系电话',
  `addr_txt` text comment '供应商地址文本',
  `valid_flag` varchar(10) comment '有效标识，例如 Y 表示启用，N 表示停用'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='ERP 供应商主数据表，记录采购来源和供应商准入信息';

-- ERP: purchase_order
create table `p1p_ext_erp_purchase_order` (
  `po_id` bigint not null auto_increment primary key comment '采购订单内部主键，ERP 单列表主键',
  `vendor_id` bigint not null comment '供应商内部主键，关联供应商主数据',
  `po_no` varchar(100) not null comment '采购订单号，ERP 采购单据业务编号',
  `task_no` varchar(100) comment '采购任务号或项目任务编码',
  `order_dt_txt` varchar(20) comment '下单日期文本，常见格式如 20260508 或 2026/05/08',
  `plan_arrive_yyyymmdd` varchar(8) comment '计划到货日期文本，YYYYMMDD 格式',
  `po_stat_cd` varchar(30) comment '采购订单状态代码，ERP 私有枚举',
  `pri_lvl` varchar(10) comment '优先级文本代码，例如 P1、P2、P3',
  `urgent_tab` varchar(10) comment '紧急标识，历史字段名保留 tab 习惯'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='ERP 采购订单头表，记录供应商、订单号、交期、状态和优先级';

-- ERP: purchase_line
create table `p1p_ext_erp_purchase_line` (
  `po_line_id` bigint not null auto_increment primary key comment '采购订单行内部主键，ERP 单列表主键',
  `po_id` bigint not null comment '采购订单内部主键，关联采购订单头',
  `item_id` bigint not null comment '物料内部主键，关联采购物料',
  `line_seq` varchar(20) not null comment '采购行序号，历史系统可能保存为字符',
  `req_qty_txt` varchar(40) comment '采购数量文本，可能带单位或千分位',
  `order_uom` varchar(30) comment '采购计量单位代码',
  `need_dt_char` varchar(12) comment '需求日期字符值，常见格式如 20260610',
  `dlv_dt_txt` varchar(20) comment '订单行计划到货日期文本',
  `line_stat_cd` varchar(30) comment '采购行状态代码，ERP 私有枚举'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='ERP 采购订单行表，记录每一项采购物料、数量、单位和行状态';

-- ERP: vendor_batch
create table `p1p_ext_erp_vendor_batch` (
  `vendor_batch_id` bigint not null auto_increment primary key comment '供应商批次内部主键，ERP 单列表主键',
  `po_line_id` bigint not null comment '来源采购订单行内部主键',
  `vendor_id` bigint not null comment '供应商内部主键，便于按供应商批次追溯',
  `item_id` bigint not null comment '物料内部主键，批次对应的采购物料',
  `ven_lot_no` varchar(100) not null comment '供应商批次号，供应商随货提供的 lot 编号',
  `cert_doc_no` varchar(100) comment '合格证或随货质量证明文件号',
  `mfg_dt_txt` varchar(20) comment '供应商生产日期文本',
  `exp_dt_txt` varchar(20) comment '有效期或失效日期文本',
  `batch_stat_cd` varchar(30) comment '供应商批次状态代码，ERP 私有枚举'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='ERP 供应商批次表，承接采购行与供应商来料批次的来源追溯';

-- ERP: inventory_lot
create table `p1p_ext_erp_inventory_lot` (
  `inv_lot_id` bigint not null auto_increment primary key comment '库存批次内部主键，ERP 单列表主键',
  `vendor_batch_id` bigint comment '供应商批次内部主键，标识库存批次来源',
  `item_id` bigint not null comment '物料内部主键，库存批次对应物料',
  `stock_lot_no` varchar(100) not null comment '企业库存批次号，入库后生成的内部批号',
  `wh_code` varchar(100) comment '仓库编码',
  `bin_code` varchar(100) comment '库位或货位编码',
  `onhand_qty_chr` varchar(40) comment '库存现存量字符值，可能带单位或格式符号',
  `qty_uom` varchar(30) comment '库存数量单位代码',
  `inv_status_cd` varchar(30) comment '库存批次状态代码，例如可用、冻结、待检'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='ERP 库存批次表，连接供应商批次、入库和生产领料';

-- ERP: receipt_bill
create table `p1p_ext_erp_receipt_bill` (
  `receipt_id` bigint not null auto_increment primary key comment '入库单内部主键，ERP 单列表主键',
  `inv_lot_id` bigint not null comment '形成的库存批次内部主键',
  `po_line_id` bigint comment '来源采购订单行内部主键',
  `recv_no` varchar(100) not null comment '入库单号，ERP 仓储业务编号',
  `recv_time_txt` varchar(30) comment '入库时间文本，可能为日期或日期时间格式',
  `recv_qty_txt` varchar(40) comment '入库数量文本，可能带单位或小数格式',
  `wh_keeper` varchar(100) comment '仓库保管员或入库经办人',
  `iqc_flag` varchar(10) comment '是否需要来料检验标识，例如 1 或 0',
  `recv_stat_cd` varchar(30) comment '入库单状态代码，ERP 私有枚举'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='ERP 入库单表，记录采购到货后形成库存批次的入库业务';

-- ERP: issue_bill
create table `p1p_ext_erp_issue_bill` (
  `issue_id` bigint not null auto_increment primary key comment '领料单内部主键，ERP 单列表主键',
  `inv_lot_id` bigint not null comment '领用库存批次内部主键',
  `mo_no` varchar(100) comment '生产或装配工单号，用于跨系统关联 MES 工单',
  `issue_no` varchar(100) not null comment '领料单号，ERP 仓储出库业务编号',
  `issue_time_txt` varchar(30) comment '领料时间文本，可能为日期或日期时间格式',
  `issue_qty_txt` varchar(40) comment '领料数量文本，可能带单位或小数格式',
  `picker_name` varchar(100) comment '领料人姓名',
  `use_for` varchar(200) comment '领料用途或投料去向说明',
  `issue_stat_cd` varchar(30) comment '领料单状态代码，ERP 私有枚举'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='ERP 领料单表，记录库存批次被生产或装配工单领用的业务';

-- MES: sys_task
create table `p1p_ext_mes_sys_task` (
  `task_id` bigint not null auto_increment primary key comment '任务主键，MES 源侧单列自增主键',
  `workid` varchar(100) not null comment '工单号，现场系统常用任务编号',
  `taskcode` varchar(100) comment '任务编码，用于跨系统追溯',
  `worktype` varchar(50) comment '工单类型源编码，例如生产或装配',
  `workname` varchar(200) comment '工单名称或任务名称',
  `partno` varchar(100) comment '零件号或组件型号号，用于解析目标零件定义',
  `route_no` varchar(100) comment '执行工艺路线编号，用于解析目标工艺路线',
  `zzh` varchar(100) comment '组件序列号，现场历史字段写法',
  `planqty` varchar(30) comment '计划数量，源侧按文本保存',
  `yxj` varchar(10) comment '优先级代码，现场缩写字段',
  `urgent_tab` varchar(5) comment '是否紧急标识，源侧使用 Y 或 N',
  `plandate` varchar(30) comment '计划开始时间，源侧文本日期',
  `delivery_date_char` varchar(30) comment '计划完成时间，源侧字符日期',
  `reportman` varchar(100) comment '提交人姓名',
  `reportdate` varchar(30) comment '提交时间，源侧文本时间',
  `rwstate` varchar(20) comment '任务状态源编码'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='MES 任务与工单主表，作为制造执行场景 Anchor 表';

-- MES: batch_card
create table `p1p_ext_mes_batch_card` (
  `batch_id` bigint not null auto_increment primary key comment '批次卡主键，MES 源侧单列自增主键',
  `task_id` bigint not null comment '所属任务主键',
  `batchno` varchar(100) not null comment '生产批次号',
  `partno` varchar(100) comment '批次对应零件号',
  `batch_qty` varchar(30) comment '批次数量，源侧按文本保存',
  `starttime_txt` varchar(30) comment '批次开始时间文本',
  `fintime_txt` varchar(30) comment '批次完成时间文本',
  `zhuangtai` varchar(20) comment '批次状态源编码',
  `quality_code` varchar(200) comment '批次质量编号'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='MES 生产批次卡表，连接工单与零件追踪';

-- MES: part_track
create table `p1p_ext_mes_part_track` (
  `track_id` bigint not null auto_increment primary key comment '零件追踪主键，MES 源侧单列自增主键',
  `batch_id` bigint not null comment '来源批次主键',
  `partno` varchar(100) not null comment '零件号',
  `part_sn` varchar(100) comment '零件序列号',
  `lotno` varchar(100) comment '零件批次号',
  `jianhao` varchar(100) comment '件号，现场拼音字段',
  `qcode` varchar(200) comment '零件质量编号',
  `source_code` varchar(20) comment '零件来源类型源编码',
  `part_state` varchar(20) comment '零件状态源编码'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='MES 零件个体追踪表，作为零件实例 Anchor 表';

-- MES: assembly_record
create table `p1p_ext_mes_assembly_record` (
  `assembly_id` bigint not null auto_increment primary key comment '装配记录主键，MES 源侧单列自增主键',
  `task_id` bigint not null comment '装配任务主键',
  `batch_id` bigint comment '装配批次主键',
  `part_id` bigint not null comment '被装配零件追踪主键',
  `component_sn` varchar(100) not null comment '组件序列号',
  `bom_item_code` varchar(100) comment 'BOM 项编码',
  `position_code` varchar(100) comment '装配位置编码',
  `install_dt_txt` varchar(30) comment '装配时间文本',
  `valid_begin_txt` varchar(30) comment '装配关系有效开始时间文本',
  `valid_end_txt` varchar(30) comment '装配关系有效结束时间文本',
  `uninstall_dt_txt` varchar(30) comment '拆下时间文本',
  `remove_reason_code` varchar(50) comment '拆下原因源编码',
  `personname` varchar(100) comment '装配人员姓名',
  `install_state` varchar(20) comment '装配状态源编码'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='MES 组件与零件装配记录表，连接零件追踪和组件装配关系';

-- MES: sys_station
create table `p1p_ext_mes_sys_station` (
  `station_id` bigint not null auto_increment primary key comment '工位主键，MES 源侧单列自增主键',
  `stationid` varchar(50) not null comment '工位编号',
  `stationname` varchar(200) not null comment '工位名称',
  `workshop` varchar(100) comment '所属车间',
  `percount` varchar(20) comment '工位人员数量文本',
  `equcount` varchar(20) comment '工位设备数量文本',
  `zhuangtai` varchar(20) comment '工位状态源编码'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='MES 工位基础表，提供执行地点与资源归属';

-- MES: equipment_asset
create table `p1p_ext_mes_equipment_asset` (
  `equipment_asset_id` bigint not null auto_increment primary key comment '设备资产主键，MES 源侧单列自增主键',
  `station_id` bigint comment '所属工位主键',
  `equipmentid` varchar(100) not null comment '设备编号',
  `equipmentname` varchar(200) not null comment '设备名称',
  `equipmentnum` varchar(20) comment '设备序号文本',
  `eqtype_code` varchar(50) comment '设备类型源编码',
  `zhuangtai` varchar(20) comment '设备状态源编码',
  `bgcolor` varchar(50) comment '设备颜色或看板标识',
  `indate` varchar(30) comment '设备建档时间文本'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='MES 设备资产表，作为制造资源 Anchor 表';

-- MES: person_info
create table `p1p_ext_mes_person_info` (
  `person_id` bigint not null auto_increment primary key comment '人员主键，MES 源侧单列自增主键',
  `station_id` bigint comment '常驻工位主键',
  `personnum` varchar(100) not null comment '员工编号',
  `personname` varchar(100) not null comment '员工姓名',
  `workshop` varchar(100) comment '所属车间',
  `phone` varchar(50) comment '联系电话',
  `jndj` varchar(50) comment '技能等级源编码',
  `gz` varchar(50) comment '工种源编码',
  `deptid` varchar(100) comment '所属部门编码',
  `deptname` varchar(200) comment '所属部门名称'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='MES 人员基础信息表，覆盖操作员与装配人员';

-- MES: tooling_asset
create table `p1p_ext_mes_tooling_asset` (
  `tooling_asset_id` bigint not null auto_increment primary key comment '工装主键，MES 源侧单列自增主键',
  `station_id` bigint comment '常用工位主键',
  `toolingid` varchar(100) not null comment '工装编号',
  `toolingname` varchar(200) not null comment '工装名称',
  `gzzl` varchar(50) comment '工装种类源编码',
  `jzrq_txt` varchar(30) comment '校准到期日期文本',
  `zhuangtai` varchar(20) comment '工装状态源编码'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='MES 工装资产表，记录夹具和专用工具';

-- MES: work_log
create table `p1p_ext_mes_work_log` (
  `worklog_id` bigint not null auto_increment primary key comment '工序日志主键，MES 源侧单列自增主键',
  `task_id` bigint not null comment '所属任务主键',
  `batch_id` bigint comment '所属批次主键',
  `part_id` bigint comment '关联零件追踪主键',
  `assembly_id` bigint comment '关联装配记录主键',
  `station_id` bigint comment '执行工位主键',
  `equipment_asset_id` bigint comment '执行设备主键',
  `person_id` bigint comment '主操作人员主键',
  `zzh` varchar(100) comment '组件序列号冗余字段',
  `op_no` varchar(50) not null comment '工序编号',
  `workid` varchar(50) comment '工序编码，现场字段名',
  `workname` varchar(200) comment '工序名称',
  `startdate_txt` varchar(30) comment '实际开始时间文本',
  `enddate_txt` varchar(30) comment '实际完成时间文本',
  `status_code` varchar(20) comment '工序执行状态源编码'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='MES 工序执行日志表，作为制造执行 Beneficiary 表';

-- MES: step_log
create table `p1p_ext_mes_step_log` (
  `steplog_id` bigint not null auto_increment primary key comment '工步日志主键，MES 源侧单列自增主键',
  `worklog_id` bigint not null comment '所属工序日志主键',
  `person_id` bigint comment '操作人员主键',
  `tooling_asset_id` bigint comment '使用工装主键',
  `stepsid` varchar(50) not null comment '工步编号',
  `stepsname` varchar(200) not null comment '工步名称',
  `stepnum` varchar(20) comment '工步顺序文本',
  `start_time_text` varchar(30) comment '工步开始时间文本',
  `finish_time_text` varchar(30) comment '工步完成时间文本',
  `zhuangtai` varchar(20) comment '工步状态源编码',
  `opername` varchar(100) comment '操作人员姓名冗余字段'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='MES 工步执行日志表，作为工序执行的下游 Beneficiary 表';

-- MES: resource_usage
create table `p1p_ext_mes_resource_usage` (
  `usage_id` bigint not null auto_increment primary key comment '资源使用主键，MES 源侧单列自增主键',
  `worklog_id` bigint not null comment '所属工序日志主键',
  `steplog_id` bigint comment '所属工步日志主键',
  `equipment_asset_id` bigint comment '使用设备主键',
  `person_id` bigint comment '使用人员主键',
  `station_id` bigint comment '使用工位主键',
  `tooling_asset_id` bigint comment '使用工装主键',
  `use_start_txt` varchar(30) comment '资源使用开始时间文本',
  `use_end_txt` varchar(30) comment '资源使用结束时间文本',
  `mastery_txt` varchar(20) comment '人员熟练度文本，例如百分比',
  `use_state` varchar(20) comment '资源使用状态源编码'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='MES 资源使用记录表，覆盖工序或工步使用的设备人员工位工装';

-- MES: process_param
create table `p1p_ext_mes_process_param` (
  `param_id` bigint not null auto_increment primary key comment '过程参数主键，MES 源侧单列自增主键',
  `worklog_id` bigint not null comment '所属工序日志主键',
  `steplog_id` bigint comment '所属工步日志主键',
  `equipment_asset_id` bigint comment '采集设备主键',
  `param_std_code` varchar(100) comment '参数标准编码',
  `paramid` varchar(100) not null comment '参数编码',
  `paramname` varchar(200) not null comment '参数名称',
  `lqyl` varchar(50) comment '现场采集值，可能带单位或旧字段缩写',
  `bzval` varchar(50) comment '标准值文本',
  `llower` varchar(50) comment '下限值文本',
  `uupper` varchar(50) comment '上限值文本',
  `unitname` varchar(50) comment '参数单位名称',
  `collect_dt` varchar(30) comment '采集时间文本',
  `pdjg` varchar(20) comment '判定结果源编码'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='MES 过程参数采集表，作为制造执行场景重点 Beneficiary 表';

-- QMS: quality_event_doc
create table `p1p_ext_qms_quality_event_doc` (
  `quality_event_doc_id` bigint not null auto_increment primary key comment '质量事件源单主键',
  `quality_event_no` varchar(100) not null comment '质量事件编号',
  `event_source_code` varchar(80) comment '事件来源代码',
  `event_type_code` varchar(80) comment '事件类型代码',
  `event_title` varchar(200) comment '质量事件标题',
  `event_desc` text comment '质量事件描述',
  `event_time_text` varchar(50) comment '事件发生或发现时间文本',
  `severity_code` varchar(50) comment '严重程度代码',
  `event_status_code` varchar(50) comment '事件状态代码',
  `trigger_source_no` varchar(120) comment '触发来源业务编号',
  `component_serial_no` varchar(100) comment '关联组件序列号',
  `part_serial_no` varchar(100) comment '关联零件序列号',
  `work_order_no` varchar(100) comment '关联工单编号',
  `subject_type_code` varchar(80) comment '事件关联对象类型代码',
  `subject_role_code` varchar(80) comment '事件对象角色代码',
  `impact_desc` text comment '事件影响说明'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='QMS 质量事件源单表，作为检验异常、缺陷和不合格闭环的入口单据';

-- QMS: inspection_doc
create table `p1p_ext_qms_inspection_doc` (
  `inspection_doc_id` bigint not null auto_increment primary key comment '检验单源表主键',
  `quality_event_doc_id` bigint comment '关联质量事件源单主键',
  `inspection_no` varchar(100) not null comment '检验单号',
  `spec_code` varchar(100) comment '检验规范编码',
  `inspection_type_code` varchar(80) comment '检验类型代码',
  `find_date_text` varchar(50) comment '发现或检验时间文本',
  `find_type_code` varchar(80) comment '发现方式代码',
  `result_code` varchar(50) comment '检验结论代码',
  `component_serial_no` varchar(100) comment '受检组件序列号',
  `part_serial_no` varchar(100) comment '受检零件序列号',
  `work_order_no` varchar(100) comment '关联工单编号',
  `op_no` varchar(50) comment '关联工序编号',
  `inventory_batch_no` varchar(100) comment '来料检验库存批次号',
  `inspector_no` varchar(100) comment '检验员编号'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='QMS 检验单主表，记录来料、过程、最终和复检等检验活动';

-- QMS: inspection_item
create table `p1p_ext_qms_inspection_item` (
  `inspection_item_id` bigint not null auto_increment primary key comment '检验项目源表主键',
  `inspection_doc_id` bigint not null comment '所属检验单主键',
  `spec_code` varchar(100) comment '检验规范编码',
  `item_code` varchar(100) not null comment '检验项目编码',
  `item_name` varchar(200) not null comment '检验项目名称',
  `measured_text` varchar(200) comment '实测值文本',
  `standard_text` varchar(200) comment '标准值文本',
  `lower_limit_text` varchar(100) comment '下限值文本',
  `upper_limit_text` varchar(100) comment '上限值文本',
  `uom` varchar(30) comment '计量单位代码',
  `judge_code` varchar(50) comment '项目判定结果代码'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='QMS 检验项目结果表，作为检验记录下的明细受益表';

-- QMS: defect_log
create table `p1p_ext_qms_defect_log` (
  `defect_log_id` bigint not null auto_increment primary key comment '缺陷记录源表主键',
  `inspection_doc_id` bigint comment '来源检验单主键',
  `quality_event_doc_id` bigint comment '关联质量事件源单主键',
  `component_serial_no` varchar(100) comment '关联组件序列号',
  `part_serial_no` varchar(100) comment '关联零件序列号',
  `work_order_no` varchar(100) comment '关联工单编号',
  `op_no` varchar(50) comment '发现工序编号',
  `defect_code` varchar(100) not null comment '缺陷代码',
  `defect_name` varchar(200) comment '缺陷名称',
  `defect_position_text` varchar(200) comment '缺陷部位描述',
  `severity_code` varchar(50) comment '缺陷严重程度代码',
  `failure_desc` text comment '不合格或缺陷情况描述',
  `find_dept_code` varchar(100) comment '发现单位编码',
  `find_dept_name` varchar(200) comment '发现单位名称'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='QMS 缺陷记录表，承接检验发现的问题并连接后续不合格通知';

-- QMS: iqs_failure
create table `p1p_ext_qms_iqs_failure` (
  `iqs_failure_id` bigint not null auto_increment primary key comment '不合格通知单源表主键',
  `quality_event_doc_id` bigint not null comment '来源质量事件源单主键',
  `inspection_doc_id` bigint comment '触发不合格的检验单主键',
  `defect_log_id` bigint comment '触发不合格的缺陷记录主键',
  `component_serial_no` varchar(100) comment '涉及组件序列号',
  `part_serial_no` varchar(100) comment '涉及零件序列号',
  `work_order_no` varchar(100) comment '涉及工单编号',
  `op_no` varchar(50) comment '发现工序编号',
  `supplier_code` varchar(100) comment '责任或来源供应商编码',
  `maintenance_event_no` varchar(100) comment '来源维修事件编号',
  `doc_source` varchar(80) comment '不合格来源代码',
  `failure_type` varchar(80) comment '不合格类型代码',
  `quality_code` varchar(200) not null comment '质量编号',
  `iqs_failure_order` varchar(120) not null comment '不合格通知单编号',
  `task_code` varchar(100) comment '任务编号',
  `product_code` varchar(100) comment '机型编码',
  `product_name` varchar(200) comment '机型名称',
  `piece_no` varchar(100) comment '件号',
  `find_dept_code` varchar(100) comment '发现单位编码',
  `find_dept_name` varchar(200) comment '发现单位名称',
  `doc_status_code` varchar(50) comment '单据状态代码',
  `secret_level_code` varchar(20) comment '密级代码',
  `starttime_text` varchar(50) comment '流程开始时间文本',
  `finishtime_text` varchar(50) comment '流程结束时间文本'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='IQS 不合格通知单源表，保留真实 IQS 单据字段习惯并作为不合格闭环 Anchor';

-- QMS: iqs_failure_content
create table `p1p_ext_qms_iqs_failure_content` (
  `iqs_failure_content_id` bigint not null auto_increment primary key comment '不合格内容源表主键',
  `iqs_failure_id` bigint not null comment '所属不合格通知单主键',
  `start_piece_no_text` varchar(50) comment '起始件号文本',
  `end_piece_no_text` varchar(50) comment '终止件号文本',
  `failure_desc` text not null comment '不合格情况描述',
  `failure_place_brief` text comment '故障部位简要说明',
  `find_type_code` varchar(80) comment '发现方式代码',
  `find_date_text` varchar(50) comment '发现时间文本',
  `finder_type_code` varchar(80) comment '发现人类别代码',
  `find_user_code` varchar(100) comment '发现人编号',
  `find_user_name` varchar(100) comment '发现人姓名',
  `duty_worksec_code` varchar(100) comment '责任工段编码',
  `duty_worksec_name` varchar(200) comment '责任工段名称',
  `duty_dept_code` varchar(100) comment '责任单位编码',
  `duty_dept_name` varchar(200) comment '责任单位名称',
  `actual_method` text comment '实际处理情况',
  `doc_no` varchar(100) comment '衍生单据号'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='IQS 不合格内容源表，记录问题描述、发现信息、责任单位和实际处理内容';

-- QMS: iqs_failure_duty
create table `p1p_ext_qms_iqs_failure_duty` (
  `iqs_failure_duty_id` bigint not null auto_increment primary key comment '不合格责任源表主键',
  `iqs_failure_id` bigint not null comment '所属不合格通知单主键',
  `iqs_failure_content_id` bigint comment '关联不合格内容主键',
  `duty_user_code` varchar(100) comment '责任者编码',
  `duty_user_name` varchar(100) comment '责任者姓名',
  `duty_dept_code` varchar(100) comment '责任单位编码',
  `duty_dept_name` varchar(200) comment '责任单位名称',
  `supplier_code` varchar(100) comment '责任供应商编码',
  `duty_person_ratio_text` varchar(30) comment '责任人占比文本',
  `responsibility_date_text` varchar(50) comment '责任认定时间文本'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='IQS 不合格责任分配源表，记录责任人员、责任部门、供应商和责任比例';

-- QMS: disposal_order
create table `p1p_ext_qms_disposal_order` (
  `disposal_order_id` bigint not null auto_increment primary key comment '处置单源表主键',
  `quality_event_doc_id` bigint not null comment '关联质量事件源单主键',
  `iqs_failure_id` bigint comment '关联不合格通知单主键',
  `iqs_failure_content_id` bigint comment '关联不合格内容主键',
  `work_order_no` varchar(100) comment '处置关联工单编号',
  `responsible_user_code` varchar(100) comment '处置责任人员编号',
  `disposal_no` varchar(100) not null comment '处置单号',
  `deal_type_find_code` varchar(80) comment '发现单位处置方式代码',
  `deal_type_pass_code` varchar(80) comment '检验组处置方式代码',
  `deal_type_tech_code` varchar(80) comment '技术人员处置方式代码',
  `disposal_type_code` varchar(80) comment '综合处置类型代码',
  `disposal_desc` text comment '处置说明',
  `disposal_date_text` varchar(50) comment '处置时间文本',
  `disposal_status_code` varchar(50) comment '处置状态代码'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='QMS 处置单源表，记录不合格或质量事件的处置措施和闭环状态';

-- QMS: recheck_record
create table `p1p_ext_qms_recheck_record` (
  `recheck_record_id` bigint not null auto_increment primary key comment '复检记录源表主键',
  `disposal_order_id` bigint not null comment '来源处置单主键',
  `inspection_doc_id` bigint comment '对应复检检验单主键',
  `inspector_no` varchar(100) comment '复检人员编号',
  `recheck_no` varchar(100) not null comment '复检编号',
  `recheck_date_text` varchar(50) comment '复检时间文本',
  `recheck_result_code` varchar(50) comment '复检结论代码',
  `conclusion_desc` text comment '复检结论说明'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='QMS 复检记录源表，记录处置后的再次检验和结论';

-- MRO: service_event
create table `p1p_ext_mro_service_event` (
  `service_event_id` bigint not null auto_increment primary key comment '服役维修事件源表主键',
  `event_no` varchar(100) not null comment '服役维修事件编号',
  `component_serial_no` varchar(100) not null comment '作动筒组件序列号',
  `install_pos_code` varchar(100) comment '装机位置编码或外场站位代码',
  `service_time_text` varchar(30) comment '源系统记录的服役或维修发生时间文本',
  `event_kind_code` varchar(30) comment '维修事件类型代码',
  `service_unit_code` varchar(100) comment '维修单位编码',
  `service_unit_name` varchar(200) comment '维修单位名称',
  `evt_state` varchar(30) comment '事件状态代码',
  `event_summary` text comment '事件摘要或备注说明'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='MRO 服役维修事件主表，作为维修工单、故障、拆换和反馈的结构锚点';

-- MRO: repair_order
create table `p1p_ext_mro_repair_order` (
  `repair_order_id` bigint not null auto_increment primary key comment '维修工单源表主键',
  `service_event_id` bigint not null comment '所属服役维修事件主键',
  `repair_order_no` varchar(100) not null comment '维修工单编号',
  `component_serial_no` varchar(100) comment '维修对象组件序列号',
  `wo_no` varchar(100) comment '统一工单号或维修派工号',
  `task_title` varchar(200) comment '维修任务名称',
  `plan_start_text` varchar(30) comment '计划开始时间文本',
  `plan_finish_text` varchar(30) comment '计划完成时间文本',
  `ro_state` varchar(30) comment '维修工单状态代码',
  `report_man` varchar(100) comment '提交人姓名'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='MRO 维修工单表，承接服役事件并向故障报告和拆换记录传递工单上下文';

-- MRO: fault_report
create table `p1p_ext_mro_fault_report` (
  `fault_report_id` bigint not null auto_increment primary key comment '故障报告源表主键',
  `service_event_id` bigint not null comment '所属服役维修事件主键',
  `repair_order_id` bigint comment '关联维修工单主键',
  `component_serial_no` varchar(100) comment '故障涉及组件序列号',
  `fault_sym_code` varchar(100) not null comment '故障现象代码',
  `fault_title` varchar(200) comment '故障名称或标题',
  `fault_text` text comment '故障现象描述',
  `fail_pos_desc` varchar(200) comment '故障部位说明',
  `found_at_text` varchar(30) comment '故障发现时间文本',
  `finder_name` varchar(100) comment '故障发现人姓名',
  `gzjb` varchar(30) comment '故障级别代码'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='MRO 故障报告表，记录服役维修阶段发现的故障现象和严重程度';

-- MRO: remove_install_record
create table `p1p_ext_mro_remove_install_record` (
  `remove_install_record_id` bigint not null auto_increment primary key comment '拆换记录源表主键',
  `service_event_id` bigint not null comment '所属服役维修事件主键',
  `repair_order_id` bigint comment '关联维修工单主键',
  `fault_report_id` bigint comment '触发拆换的故障报告主键',
  `change_no` varchar(100) not null comment '拆换记录编号',
  `component_serial_no` varchar(100) not null comment '拆换所在组件序列号',
  `install_pos_code` varchar(100) comment '拆换发生装机位置编码',
  `removed_part_sn` varchar(100) comment '拆下零件序列号',
  `installed_part_sn` varchar(100) comment '换上零件序列号',
  `source_batch_no` varchar(100) comment '换上件来源库存批次号',
  `remove_cause` varchar(80) comment '拆下或更换原因代码',
  `change_time_text` varchar(30) comment '拆换完成时间文本',
  `remove_time_text` varchar(30) comment '拆下时间文本',
  `install_time_text` varchar(30) comment '装上时间文本',
  `new_valid_from_text` varchar(30) comment '新装配关系有效开始时间文本',
  `operator_name` varchar(100) comment '拆换操作人姓名',
  `install_person` varchar(100) comment '装配人员姓名',
  `change_state` varchar(30) comment '拆换记录状态代码',
  `install_state` varchar(30) comment '装配关系状态代码'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='MRO 拆下与换上记录表，表达故障维修中的零件拆换和装配有效期变化';

-- MRO: field_feedback
create table `p1p_ext_mro_field_feedback` (
  `field_feedback_id` bigint not null auto_increment primary key comment '外场反馈源表主键',
  `service_event_id` bigint comment '关联服役维修事件主键',
  `fault_report_id` bigint comment '关联故障报告主键',
  `quality_event_no` varchar(100) comment '反馈转化形成的质量事件编号',
  `component_serial_no` varchar(100) comment '反馈涉及组件序列号',
  `feedback_no` varchar(100) not null comment '外场反馈编号',
  `feedback_source_code` varchar(50) comment '反馈来源代码',
  `feedback_time_text` varchar(30) comment '反馈时间文本',
  `feedback_content` text comment '反馈内容',
  `feedback_state` varchar(30) comment '反馈处理状态代码'
) engine=innodb default charset=utf8mb4 collate=utf8mb4_0900_ai_ci comment='MRO 外场和用户反馈表，记录服役阶段质量信息并可转化为质量事件';

-- Foreign keys
alter table `p1p_ext_plm_gear_system_def` add constraint `fk_p1p_ext_plm_gear_system_def_product_model_id` foreign key (`product_model_id`) references `p1p_ext_plm_product_model` (`product_model_id`);
alter table `p1p_ext_plm_actuator_model` add constraint `fk_p1p_ext_plm_actuator_model_gear_system_def_id` foreign key (`gear_system_def_id`) references `p1p_ext_plm_gear_system_def` (`gear_system_def_id`);
alter table `p1p_ext_plm_design_revision` add constraint `fk_p1p_ext_plm_design_revision_actuator_model_id` foreign key (`actuator_model_id`) references `p1p_ext_plm_actuator_model` (`actuator_model_id`);
alter table `p1p_ext_plm_part_master` add constraint `fk_p1p_ext_plm_part_master_design_revision_id` foreign key (`design_revision_id`) references `p1p_ext_plm_design_revision` (`design_revision_id`);
alter table `p1p_ext_plm_bom_header` add constraint `fk_p1p_ext_plm_bom_header_actuator_model_id` foreign key (`actuator_model_id`) references `p1p_ext_plm_actuator_model` (`actuator_model_id`);
alter table `p1p_ext_plm_bom_header` add constraint `fk_p1p_ext_plm_bom_header_design_revision_id` foreign key (`design_revision_id`) references `p1p_ext_plm_design_revision` (`design_revision_id`);
alter table `p1p_ext_plm_bom_header` add constraint `fk_p1p_ext_plm_bom_header_root_part_master_id` foreign key (`root_part_master_id`) references `p1p_ext_plm_part_master` (`part_master_id`);
alter table `p1p_ext_plm_bom_line` add constraint `fk_p1p_ext_plm_bom_line_bom_header_id` foreign key (`bom_header_id`) references `p1p_ext_plm_bom_header` (`bom_header_id`);
alter table `p1p_ext_plm_bom_line` add constraint `fk_p1p_ext_plm_bom_line_parent_part_master_id` foreign key (`parent_part_master_id`) references `p1p_ext_plm_part_master` (`part_master_id`);
alter table `p1p_ext_plm_bom_line` add constraint `fk_p1p_ext_plm_bom_line_child_part_master_id` foreign key (`child_part_master_id`) references `p1p_ext_plm_part_master` (`part_master_id`);
alter table `p1p_ext_plm_route_card` add constraint `fk_p1p_ext_plm_route_card_actuator_model_id` foreign key (`actuator_model_id`) references `p1p_ext_plm_actuator_model` (`actuator_model_id`);
alter table `p1p_ext_plm_route_card` add constraint `fk_p1p_ext_plm_route_card_part_master_id` foreign key (`part_master_id`) references `p1p_ext_plm_part_master` (`part_master_id`);
alter table `p1p_ext_plm_route_card` add constraint `fk_p1p_ext_plm_route_card_design_revision_id` foreign key (`design_revision_id`) references `p1p_ext_plm_design_revision` (`design_revision_id`);
alter table `p1p_ext_plm_operation_card` add constraint `fk_p1p_ext_plm_operation_card_route_card_id` foreign key (`route_card_id`) references `p1p_ext_plm_route_card` (`route_card_id`);
alter table `p1p_ext_plm_technical_parameter` add constraint `fk_p1p_ext_plm_technical_parameter_operation_card_id` foreign key (`operation_card_id`) references `p1p_ext_plm_operation_card` (`operation_card_id`);
alter table `p1p_ext_plm_inspection_standard` add constraint `fk_p1p_ext_plm_inspection_standard_operation_card_id` foreign key (`operation_card_id`) references `p1p_ext_plm_operation_card` (`operation_card_id`);
alter table `p1p_ext_plm_inspection_standard` add constraint `fk_p1p_ext_plm_inspection_standard_actuator_model_id` foreign key (`actuator_model_id`) references `p1p_ext_plm_actuator_model` (`actuator_model_id`);
alter table `p1p_ext_plm_inspection_standard` add constraint `fk_p1p_ext_plm_inspection_standard_part_master_id` foreign key (`part_master_id`) references `p1p_ext_plm_part_master` (`part_master_id`);
alter table `p1p_ext_erp_purchase_order` add constraint `fk_p1p_ext_erp_purchase_order_vendor_id` foreign key (`vendor_id`) references `p1p_ext_erp_vendor_master` (`vendor_id`);
alter table `p1p_ext_erp_purchase_line` add constraint `fk_p1p_ext_erp_purchase_line_po_id` foreign key (`po_id`) references `p1p_ext_erp_purchase_order` (`po_id`);
alter table `p1p_ext_erp_purchase_line` add constraint `fk_p1p_ext_erp_purchase_line_item_id` foreign key (`item_id`) references `p1p_ext_erp_item_master` (`item_id`);
alter table `p1p_ext_erp_vendor_batch` add constraint `fk_p1p_ext_erp_vendor_batch_po_line_id` foreign key (`po_line_id`) references `p1p_ext_erp_purchase_line` (`po_line_id`);
alter table `p1p_ext_erp_vendor_batch` add constraint `fk_p1p_ext_erp_vendor_batch_vendor_id` foreign key (`vendor_id`) references `p1p_ext_erp_vendor_master` (`vendor_id`);
alter table `p1p_ext_erp_vendor_batch` add constraint `fk_p1p_ext_erp_vendor_batch_item_id` foreign key (`item_id`) references `p1p_ext_erp_item_master` (`item_id`);
alter table `p1p_ext_erp_inventory_lot` add constraint `fk_p1p_ext_erp_inventory_lot_vendor_batch_id` foreign key (`vendor_batch_id`) references `p1p_ext_erp_vendor_batch` (`vendor_batch_id`);
alter table `p1p_ext_erp_inventory_lot` add constraint `fk_p1p_ext_erp_inventory_lot_item_id` foreign key (`item_id`) references `p1p_ext_erp_item_master` (`item_id`);
alter table `p1p_ext_erp_receipt_bill` add constraint `fk_p1p_ext_erp_receipt_bill_inv_lot_id` foreign key (`inv_lot_id`) references `p1p_ext_erp_inventory_lot` (`inv_lot_id`);
alter table `p1p_ext_erp_receipt_bill` add constraint `fk_p1p_ext_erp_receipt_bill_po_line_id` foreign key (`po_line_id`) references `p1p_ext_erp_purchase_line` (`po_line_id`);
alter table `p1p_ext_erp_issue_bill` add constraint `fk_p1p_ext_erp_issue_bill_inv_lot_id` foreign key (`inv_lot_id`) references `p1p_ext_erp_inventory_lot` (`inv_lot_id`);
alter table `p1p_ext_mes_batch_card` add constraint `fk_p1p_ext_mes_batch_card_task_id` foreign key (`task_id`) references `p1p_ext_mes_sys_task` (`task_id`);
alter table `p1p_ext_mes_part_track` add constraint `fk_p1p_ext_mes_part_track_batch_id` foreign key (`batch_id`) references `p1p_ext_mes_batch_card` (`batch_id`);
alter table `p1p_ext_mes_assembly_record` add constraint `fk_p1p_ext_mes_assembly_record_task_id` foreign key (`task_id`) references `p1p_ext_mes_sys_task` (`task_id`);
alter table `p1p_ext_mes_assembly_record` add constraint `fk_p1p_ext_mes_assembly_record_batch_id` foreign key (`batch_id`) references `p1p_ext_mes_batch_card` (`batch_id`);
alter table `p1p_ext_mes_assembly_record` add constraint `fk_p1p_ext_mes_assembly_record_part_id` foreign key (`part_id`) references `p1p_ext_mes_part_track` (`track_id`);
alter table `p1p_ext_mes_equipment_asset` add constraint `fk_p1p_ext_mes_equipment_asset_station_id` foreign key (`station_id`) references `p1p_ext_mes_sys_station` (`station_id`);
alter table `p1p_ext_mes_person_info` add constraint `fk_p1p_ext_mes_person_info_station_id` foreign key (`station_id`) references `p1p_ext_mes_sys_station` (`station_id`);
alter table `p1p_ext_mes_tooling_asset` add constraint `fk_p1p_ext_mes_tooling_asset_station_id` foreign key (`station_id`) references `p1p_ext_mes_sys_station` (`station_id`);
alter table `p1p_ext_mes_work_log` add constraint `fk_p1p_ext_mes_work_log_task_id` foreign key (`task_id`) references `p1p_ext_mes_sys_task` (`task_id`);
alter table `p1p_ext_mes_work_log` add constraint `fk_p1p_ext_mes_work_log_batch_id` foreign key (`batch_id`) references `p1p_ext_mes_batch_card` (`batch_id`);
alter table `p1p_ext_mes_work_log` add constraint `fk_p1p_ext_mes_work_log_part_id` foreign key (`part_id`) references `p1p_ext_mes_part_track` (`track_id`);
alter table `p1p_ext_mes_work_log` add constraint `fk_p1p_ext_mes_work_log_assembly_id` foreign key (`assembly_id`) references `p1p_ext_mes_assembly_record` (`assembly_id`);
alter table `p1p_ext_mes_work_log` add constraint `fk_p1p_ext_mes_work_log_station_id` foreign key (`station_id`) references `p1p_ext_mes_sys_station` (`station_id`);
alter table `p1p_ext_mes_work_log` add constraint `fk_p1p_ext_mes_work_log_equipment_asset_id` foreign key (`equipment_asset_id`) references `p1p_ext_mes_equipment_asset` (`equipment_asset_id`);
alter table `p1p_ext_mes_work_log` add constraint `fk_p1p_ext_mes_work_log_person_id` foreign key (`person_id`) references `p1p_ext_mes_person_info` (`person_id`);
alter table `p1p_ext_mes_step_log` add constraint `fk_p1p_ext_mes_step_log_worklog_id` foreign key (`worklog_id`) references `p1p_ext_mes_work_log` (`worklog_id`);
alter table `p1p_ext_mes_step_log` add constraint `fk_p1p_ext_mes_step_log_person_id` foreign key (`person_id`) references `p1p_ext_mes_person_info` (`person_id`);
alter table `p1p_ext_mes_step_log` add constraint `fk_p1p_ext_mes_step_log_tooling_asset_id` foreign key (`tooling_asset_id`) references `p1p_ext_mes_tooling_asset` (`tooling_asset_id`);
alter table `p1p_ext_mes_resource_usage` add constraint `fk_p1p_ext_mes_resource_usage_worklog_id` foreign key (`worklog_id`) references `p1p_ext_mes_work_log` (`worklog_id`);
alter table `p1p_ext_mes_resource_usage` add constraint `fk_p1p_ext_mes_resource_usage_steplog_id` foreign key (`steplog_id`) references `p1p_ext_mes_step_log` (`steplog_id`);
alter table `p1p_ext_mes_resource_usage` add constraint `fk_p1p_ext_mes_resource_usage_equipment_asset_id` foreign key (`equipment_asset_id`) references `p1p_ext_mes_equipment_asset` (`equipment_asset_id`);
alter table `p1p_ext_mes_resource_usage` add constraint `fk_p1p_ext_mes_resource_usage_person_id` foreign key (`person_id`) references `p1p_ext_mes_person_info` (`person_id`);
alter table `p1p_ext_mes_resource_usage` add constraint `fk_p1p_ext_mes_resource_usage_station_id` foreign key (`station_id`) references `p1p_ext_mes_sys_station` (`station_id`);
alter table `p1p_ext_mes_resource_usage` add constraint `fk_p1p_ext_mes_resource_usage_tooling_asset_id` foreign key (`tooling_asset_id`) references `p1p_ext_mes_tooling_asset` (`tooling_asset_id`);
alter table `p1p_ext_mes_process_param` add constraint `fk_p1p_ext_mes_process_param_worklog_id` foreign key (`worklog_id`) references `p1p_ext_mes_work_log` (`worklog_id`);
alter table `p1p_ext_mes_process_param` add constraint `fk_p1p_ext_mes_process_param_steplog_id` foreign key (`steplog_id`) references `p1p_ext_mes_step_log` (`steplog_id`);
alter table `p1p_ext_mes_process_param` add constraint `fk_p1p_ext_mes_process_param_equipment_asset_id` foreign key (`equipment_asset_id`) references `p1p_ext_mes_equipment_asset` (`equipment_asset_id`);
alter table `p1p_ext_qms_inspection_doc` add constraint `fk_p1p_ext_qms_inspection_doc_quality_event_doc_id` foreign key (`quality_event_doc_id`) references `p1p_ext_qms_quality_event_doc` (`quality_event_doc_id`);
alter table `p1p_ext_qms_inspection_item` add constraint `fk_p1p_ext_qms_inspection_item_inspection_doc_id` foreign key (`inspection_doc_id`) references `p1p_ext_qms_inspection_doc` (`inspection_doc_id`);
alter table `p1p_ext_qms_defect_log` add constraint `fk_p1p_ext_qms_defect_log_inspection_doc_id` foreign key (`inspection_doc_id`) references `p1p_ext_qms_inspection_doc` (`inspection_doc_id`);
alter table `p1p_ext_qms_defect_log` add constraint `fk_p1p_ext_qms_defect_log_quality_event_doc_id` foreign key (`quality_event_doc_id`) references `p1p_ext_qms_quality_event_doc` (`quality_event_doc_id`);
alter table `p1p_ext_qms_iqs_failure` add constraint `fk_p1p_ext_qms_iqs_failure_quality_event_doc_id` foreign key (`quality_event_doc_id`) references `p1p_ext_qms_quality_event_doc` (`quality_event_doc_id`);
alter table `p1p_ext_qms_iqs_failure` add constraint `fk_p1p_ext_qms_iqs_failure_inspection_doc_id` foreign key (`inspection_doc_id`) references `p1p_ext_qms_inspection_doc` (`inspection_doc_id`);
alter table `p1p_ext_qms_iqs_failure` add constraint `fk_p1p_ext_qms_iqs_failure_defect_log_id` foreign key (`defect_log_id`) references `p1p_ext_qms_defect_log` (`defect_log_id`);
alter table `p1p_ext_qms_iqs_failure_content` add constraint `fk_p1p_ext_qms_iqs_failure_content_iqs_failure_id` foreign key (`iqs_failure_id`) references `p1p_ext_qms_iqs_failure` (`iqs_failure_id`);
alter table `p1p_ext_qms_iqs_failure_duty` add constraint `fk_p1p_ext_qms_iqs_failure_duty_iqs_failure_id` foreign key (`iqs_failure_id`) references `p1p_ext_qms_iqs_failure` (`iqs_failure_id`);
alter table `p1p_ext_qms_iqs_failure_duty` add constraint `fk_p1p_ext_qms_iqs_failure_duty_iqs_failure_content_id` foreign key (`iqs_failure_content_id`) references `p1p_ext_qms_iqs_failure_content` (`iqs_failure_content_id`);
alter table `p1p_ext_qms_disposal_order` add constraint `fk_p1p_ext_qms_disposal_order_quality_event_doc_id` foreign key (`quality_event_doc_id`) references `p1p_ext_qms_quality_event_doc` (`quality_event_doc_id`);
alter table `p1p_ext_qms_disposal_order` add constraint `fk_p1p_ext_qms_disposal_order_iqs_failure_id` foreign key (`iqs_failure_id`) references `p1p_ext_qms_iqs_failure` (`iqs_failure_id`);
alter table `p1p_ext_qms_disposal_order` add constraint `fk_p1p_ext_qms_disposal_order_iqs_failure_content_id` foreign key (`iqs_failure_content_id`) references `p1p_ext_qms_iqs_failure_content` (`iqs_failure_content_id`);
alter table `p1p_ext_qms_recheck_record` add constraint `fk_p1p_ext_qms_recheck_record_disposal_order_id` foreign key (`disposal_order_id`) references `p1p_ext_qms_disposal_order` (`disposal_order_id`);
alter table `p1p_ext_qms_recheck_record` add constraint `fk_p1p_ext_qms_recheck_record_inspection_doc_id` foreign key (`inspection_doc_id`) references `p1p_ext_qms_inspection_doc` (`inspection_doc_id`);
alter table `p1p_ext_mro_repair_order` add constraint `fk_p1p_ext_mro_repair_order_service_event_id` foreign key (`service_event_id`) references `p1p_ext_mro_service_event` (`service_event_id`);
alter table `p1p_ext_mro_fault_report` add constraint `fk_p1p_ext_mro_fault_report_service_event_id` foreign key (`service_event_id`) references `p1p_ext_mro_service_event` (`service_event_id`);
alter table `p1p_ext_mro_fault_report` add constraint `fk_p1p_ext_mro_fault_report_repair_order_id` foreign key (`repair_order_id`) references `p1p_ext_mro_repair_order` (`repair_order_id`);
alter table `p1p_ext_mro_remove_install_record` add constraint `fk_p1p_ext_mro_remove_install_record_service_event_id` foreign key (`service_event_id`) references `p1p_ext_mro_service_event` (`service_event_id`);
alter table `p1p_ext_mro_remove_install_record` add constraint `fk_p1p_ext_mro_remove_install_record_repair_order_id` foreign key (`repair_order_id`) references `p1p_ext_mro_repair_order` (`repair_order_id`);
alter table `p1p_ext_mro_remove_install_record` add constraint `fk_p1p_ext_mro_remove_install_record_fault_report_id` foreign key (`fault_report_id`) references `p1p_ext_mro_fault_report` (`fault_report_id`);
alter table `p1p_ext_mro_field_feedback` add constraint `fk_p1p_ext_mro_field_feedback_service_event_id` foreign key (`service_event_id`) references `p1p_ext_mro_service_event` (`service_event_id`);
alter table `p1p_ext_mro_field_feedback` add constraint `fk_p1p_ext_mro_field_feedback_fault_report_id` foreign key (`fault_report_id`) references `p1p_ext_mro_fault_report` (`fault_report_id`);

-- Source-defined indexes
create unique index `uq_p1p_ext_plm_product_model_model_code` on `p1p_ext_plm_product_model` (`model_code`);
create index `idx_p1p_ext_plm_gear_system_def_product_model_id` on `p1p_ext_plm_gear_system_def` (`product_model_id`);
create index `idx_p1p_ext_plm_gear_system_def_sys_code` on `p1p_ext_plm_gear_system_def` (`sys_code`);
create unique index `uq_p1p_ext_plm_actuator_model_actuator_code` on `p1p_ext_plm_actuator_model` (`actuator_code`);
create index `idx_p1p_ext_plm_actuator_model_gear_system_def_id` on `p1p_ext_plm_actuator_model` (`gear_system_def_id`);
create index `idx_p1p_ext_plm_design_revision_actuator_model_id` on `p1p_ext_plm_design_revision` (`actuator_model_id`);
create index `idx_p1p_ext_plm_design_revision_rev_no` on `p1p_ext_plm_design_revision` (`rev_no`);
create unique index `uq_p1p_ext_plm_part_master_part_no_design_revision_id` on `p1p_ext_plm_part_master` (`part_no`, `design_revision_id`);
create index `idx_p1p_ext_plm_part_master_material_no` on `p1p_ext_plm_part_master` (`material_no`);
create unique index `uq_p1p_ext_plm_bom_header_bom_no` on `p1p_ext_plm_bom_header` (`bom_no`);
create index `idx_p1p_ext_plm_bom_header_actuator_model_id` on `p1p_ext_plm_bom_header` (`actuator_model_id`);
create index `idx_p1p_ext_plm_bom_header_design_revision_id` on `p1p_ext_plm_bom_header` (`design_revision_id`);
create index `idx_p1p_ext_plm_bom_line_bom_header_id` on `p1p_ext_plm_bom_line` (`bom_header_id`);
create index `idx_p1p_ext_plm_bom_line_child_part_master_id` on `p1p_ext_plm_bom_line` (`child_part_master_id`);
create unique index `uq_p1p_ext_plm_bom_line_bom_header_id_item_no` on `p1p_ext_plm_bom_line` (`bom_header_id`, `item_no`);
create unique index `uq_p1p_ext_plm_route_card_route_no` on `p1p_ext_plm_route_card` (`route_no`);
create index `idx_p1p_ext_plm_route_card_actuator_model_id` on `p1p_ext_plm_route_card` (`actuator_model_id`);
create index `idx_p1p_ext_plm_route_card_part_master_id` on `p1p_ext_plm_route_card` (`part_master_id`);
create index `idx_p1p_ext_plm_operation_card_route_card_id` on `p1p_ext_plm_operation_card` (`route_card_id`);
create unique index `uq_p1p_ext_plm_operation_card_route_card_id_op_no` on `p1p_ext_plm_operation_card` (`route_card_id`, `op_no`);
create index `idx_p1p_ext_plm_technical_parameter_operation_card_id` on `p1p_ext_plm_technical_parameter` (`operation_card_id`);
create index `idx_p1p_ext_plm_technical_parameter_para_code` on `p1p_ext_plm_technical_parameter` (`para_code`);
create index `idx_p1p_ext_plm_inspection_standard_insp_spec_no` on `p1p_ext_plm_inspection_standard` (`insp_spec_no`);
create index `idx_p1p_ext_plm_inspection_standard_operation_card_id` on `p1p_ext_plm_inspection_standard` (`operation_card_id`);
create index `idx_p1p_ext_plm_inspection_standard_part_master_id` on `p1p_ext_plm_inspection_standard` (`part_master_id`);
create unique index `uq_p1p_ext_erp_item_master_item_no` on `p1p_ext_erp_item_master` (`item_no`);
create index `idx_p1p_ext_erp_item_master_item_grp_cd` on `p1p_ext_erp_item_master` (`item_grp_cd`);
create unique index `uq_p1p_ext_erp_vendor_master_vendor_code` on `p1p_ext_erp_vendor_master` (`vendor_code`);
create index `idx_p1p_ext_erp_vendor_master_vendor_kind_cd` on `p1p_ext_erp_vendor_master` (`vendor_kind_cd`);
create unique index `uq_p1p_ext_erp_purchase_order_po_no` on `p1p_ext_erp_purchase_order` (`po_no`);
create index `idx_p1p_ext_erp_purchase_order_vendor_id` on `p1p_ext_erp_purchase_order` (`vendor_id`);
create index `idx_p1p_ext_erp_purchase_order_task_no` on `p1p_ext_erp_purchase_order` (`task_no`);
create index `idx_p1p_ext_erp_purchase_line_po_id` on `p1p_ext_erp_purchase_line` (`po_id`);
create index `idx_p1p_ext_erp_purchase_line_item_id` on `p1p_ext_erp_purchase_line` (`item_id`);
create unique index `uq_p1p_ext_erp_purchase_line_po_id_line_seq` on `p1p_ext_erp_purchase_line` (`po_id`, `line_seq`);
create index `idx_p1p_ext_erp_vendor_batch_ven_lot_no` on `p1p_ext_erp_vendor_batch` (`ven_lot_no`);
create index `idx_p1p_ext_erp_vendor_batch_po_line_id` on `p1p_ext_erp_vendor_batch` (`po_line_id`);
create index `idx_p1p_ext_erp_vendor_batch_vendor_id` on `p1p_ext_erp_vendor_batch` (`vendor_id`);
create index `idx_p1p_ext_erp_vendor_batch_item_id` on `p1p_ext_erp_vendor_batch` (`item_id`);
create unique index `uq_p1p_ext_erp_inventory_lot_stock_lot_no` on `p1p_ext_erp_inventory_lot` (`stock_lot_no`);
create index `idx_p1p_ext_erp_inventory_lot_vendor_batch_id` on `p1p_ext_erp_inventory_lot` (`vendor_batch_id`);
create index `idx_p1p_ext_erp_inventory_lot_item_id` on `p1p_ext_erp_inventory_lot` (`item_id`);
create index `idx_p1p_ext_erp_inventory_lot_wh_code_bin_code` on `p1p_ext_erp_inventory_lot` (`wh_code`, `bin_code`);
create unique index `uq_p1p_ext_erp_receipt_bill_recv_no` on `p1p_ext_erp_receipt_bill` (`recv_no`);
create index `idx_p1p_ext_erp_receipt_bill_inv_lot_id` on `p1p_ext_erp_receipt_bill` (`inv_lot_id`);
create index `idx_p1p_ext_erp_receipt_bill_po_line_id` on `p1p_ext_erp_receipt_bill` (`po_line_id`);
create unique index `uq_p1p_ext_erp_issue_bill_issue_no` on `p1p_ext_erp_issue_bill` (`issue_no`);
create index `idx_p1p_ext_erp_issue_bill_inv_lot_id` on `p1p_ext_erp_issue_bill` (`inv_lot_id`);
create index `idx_p1p_ext_erp_issue_bill_mo_no` on `p1p_ext_erp_issue_bill` (`mo_no`);
create unique index `uq_p1p_ext_mes_sys_task_workid` on `p1p_ext_mes_sys_task` (`workid`);
create index `idx_p1p_ext_mes_sys_task_taskcode` on `p1p_ext_mes_sys_task` (`taskcode`);
create index `idx_p1p_ext_mes_sys_task_zzh` on `p1p_ext_mes_sys_task` (`zzh`);
create unique index `uq_p1p_ext_mes_batch_card_batchno` on `p1p_ext_mes_batch_card` (`batchno`);
create index `idx_p1p_ext_mes_batch_card_task_id` on `p1p_ext_mes_batch_card` (`task_id`);
create unique index `uq_p1p_ext_mes_part_track_part_sn` on `p1p_ext_mes_part_track` (`part_sn`);
create index `idx_p1p_ext_mes_part_track_batch_id` on `p1p_ext_mes_part_track` (`batch_id`);
create index `idx_p1p_ext_mes_part_track_partno` on `p1p_ext_mes_part_track` (`partno`);
create index `idx_p1p_ext_mes_assembly_record_task_id` on `p1p_ext_mes_assembly_record` (`task_id`);
create index `idx_p1p_ext_mes_assembly_record_part_id` on `p1p_ext_mes_assembly_record` (`part_id`);
create index `idx_p1p_ext_mes_assembly_record_component_sn` on `p1p_ext_mes_assembly_record` (`component_sn`);
create unique index `uq_p1p_ext_mes_sys_station_stationid` on `p1p_ext_mes_sys_station` (`stationid`);
create unique index `uq_p1p_ext_mes_equipment_asset_equipmentid` on `p1p_ext_mes_equipment_asset` (`equipmentid`);
create index `idx_p1p_ext_mes_equipment_asset_station_id` on `p1p_ext_mes_equipment_asset` (`station_id`);
create unique index `uq_p1p_ext_mes_person_info_personnum` on `p1p_ext_mes_person_info` (`personnum`);
create index `idx_p1p_ext_mes_person_info_station_id` on `p1p_ext_mes_person_info` (`station_id`);
create unique index `uq_p1p_ext_mes_tooling_asset_toolingid` on `p1p_ext_mes_tooling_asset` (`toolingid`);
create index `idx_p1p_ext_mes_tooling_asset_station_id` on `p1p_ext_mes_tooling_asset` (`station_id`);
create index `idx_p1p_ext_mes_work_log_task_id` on `p1p_ext_mes_work_log` (`task_id`);
create index `idx_p1p_ext_mes_work_log_station_id` on `p1p_ext_mes_work_log` (`station_id`);
create index `idx_p1p_ext_mes_work_log_equipment_asset_id` on `p1p_ext_mes_work_log` (`equipment_asset_id`);
create index `idx_p1p_ext_mes_work_log_op_no` on `p1p_ext_mes_work_log` (`op_no`);
create index `idx_p1p_ext_mes_step_log_worklog_id` on `p1p_ext_mes_step_log` (`worklog_id`);
create index `idx_p1p_ext_mes_step_log_stepsid` on `p1p_ext_mes_step_log` (`stepsid`);
create index `idx_p1p_ext_mes_resource_usage_worklog_id` on `p1p_ext_mes_resource_usage` (`worklog_id`);
create index `idx_p1p_ext_mes_resource_usage_steplog_id` on `p1p_ext_mes_resource_usage` (`steplog_id`);
create index `idx_p1p_ext_mes_resource_usage_equipment_asset_id` on `p1p_ext_mes_resource_usage` (`equipment_asset_id`);
create index `idx_p1p_ext_mes_resource_usage_person_id` on `p1p_ext_mes_resource_usage` (`person_id`);
create index `idx_p1p_ext_mes_process_param_worklog_id` on `p1p_ext_mes_process_param` (`worklog_id`);
create index `idx_p1p_ext_mes_process_param_steplog_id` on `p1p_ext_mes_process_param` (`steplog_id`);
create index `idx_p1p_ext_mes_process_param_paramid` on `p1p_ext_mes_process_param` (`paramid`);
create unique index `uq_p1p_ext_qms_quality_event_doc_quality_event_no` on `p1p_ext_qms_quality_event_doc` (`quality_event_no`);
create index `idx_p1p_ext_qms_quality_event_doc_trigger_source_no` on `p1p_ext_qms_quality_event_doc` (`trigger_source_no`);
create index `idx_p1p_ext_qms_quality_event_doc_event_source_code_eve_cad09ed1` on `p1p_ext_qms_quality_event_doc` (`event_source_code`, `event_type_code`);
create unique index `uq_p1p_ext_qms_inspection_doc_inspection_no` on `p1p_ext_qms_inspection_doc` (`inspection_no`);
create index `idx_p1p_ext_qms_inspection_doc_quality_event_doc_id` on `p1p_ext_qms_inspection_doc` (`quality_event_doc_id`);
create index `idx_p1p_ext_qms_inspection_doc_component_serial_no` on `p1p_ext_qms_inspection_doc` (`component_serial_no`);
create index `idx_p1p_ext_qms_inspection_doc_work_order_no_op_no` on `p1p_ext_qms_inspection_doc` (`work_order_no`, `op_no`);
create index `idx_p1p_ext_qms_inspection_item_inspection_doc_id` on `p1p_ext_qms_inspection_item` (`inspection_doc_id`);
create index `idx_p1p_ext_qms_inspection_item_item_code` on `p1p_ext_qms_inspection_item` (`item_code`);
create index `idx_p1p_ext_qms_inspection_item_judge_code` on `p1p_ext_qms_inspection_item` (`judge_code`);
create index `idx_p1p_ext_qms_defect_log_inspection_doc_id` on `p1p_ext_qms_defect_log` (`inspection_doc_id`);
create index `idx_p1p_ext_qms_defect_log_quality_event_doc_id` on `p1p_ext_qms_defect_log` (`quality_event_doc_id`);
create index `idx_p1p_ext_qms_defect_log_defect_code` on `p1p_ext_qms_defect_log` (`defect_code`);
create unique index `uq_p1p_ext_qms_iqs_failure_iqs_failure_order` on `p1p_ext_qms_iqs_failure` (`iqs_failure_order`);
create index `idx_p1p_ext_qms_iqs_failure_quality_event_doc_id` on `p1p_ext_qms_iqs_failure` (`quality_event_doc_id`);
create index `idx_p1p_ext_qms_iqs_failure_inspection_doc_id` on `p1p_ext_qms_iqs_failure` (`inspection_doc_id`);
create index `idx_p1p_ext_qms_iqs_failure_defect_log_id` on `p1p_ext_qms_iqs_failure` (`defect_log_id`);
create index `idx_p1p_ext_qms_iqs_failure_quality_code` on `p1p_ext_qms_iqs_failure` (`quality_code`);
create index `idx_p1p_ext_qms_iqs_failure_content_iqs_failure_id` on `p1p_ext_qms_iqs_failure_content` (`iqs_failure_id`);
create index `idx_p1p_ext_qms_iqs_failure_content_find_user_code` on `p1p_ext_qms_iqs_failure_content` (`find_user_code`);
create index `idx_p1p_ext_qms_iqs_failure_content_duty_dept_code` on `p1p_ext_qms_iqs_failure_content` (`duty_dept_code`);
create index `idx_p1p_ext_qms_iqs_failure_duty_iqs_failure_id` on `p1p_ext_qms_iqs_failure_duty` (`iqs_failure_id`);
create index `idx_p1p_ext_qms_iqs_failure_duty_iqs_failure_content_id` on `p1p_ext_qms_iqs_failure_duty` (`iqs_failure_content_id`);
create index `idx_p1p_ext_qms_iqs_failure_duty_duty_user_code` on `p1p_ext_qms_iqs_failure_duty` (`duty_user_code`);
create index `idx_p1p_ext_qms_iqs_failure_duty_supplier_code` on `p1p_ext_qms_iqs_failure_duty` (`supplier_code`);
create unique index `uq_p1p_ext_qms_disposal_order_disposal_no` on `p1p_ext_qms_disposal_order` (`disposal_no`);
create index `idx_p1p_ext_qms_disposal_order_quality_event_doc_id` on `p1p_ext_qms_disposal_order` (`quality_event_doc_id`);
create index `idx_p1p_ext_qms_disposal_order_iqs_failure_id` on `p1p_ext_qms_disposal_order` (`iqs_failure_id`);
create unique index `uq_p1p_ext_qms_recheck_record_recheck_no` on `p1p_ext_qms_recheck_record` (`recheck_no`);
create index `idx_p1p_ext_qms_recheck_record_disposal_order_id` on `p1p_ext_qms_recheck_record` (`disposal_order_id`);
create index `idx_p1p_ext_qms_recheck_record_inspection_doc_id` on `p1p_ext_qms_recheck_record` (`inspection_doc_id`);
create unique index `uq_p1p_ext_mro_service_event_event_no` on `p1p_ext_mro_service_event` (`event_no`);
create index `idx_p1p_ext_mro_service_event_component_serial_no` on `p1p_ext_mro_service_event` (`component_serial_no`);
create index `idx_p1p_ext_mro_service_event_event_kind_code` on `p1p_ext_mro_service_event` (`event_kind_code`);
create unique index `uq_p1p_ext_mro_repair_order_repair_order_no` on `p1p_ext_mro_repair_order` (`repair_order_no`);
create index `idx_p1p_ext_mro_repair_order_service_event_id` on `p1p_ext_mro_repair_order` (`service_event_id`);
create index `idx_p1p_ext_mro_repair_order_wo_no` on `p1p_ext_mro_repair_order` (`wo_no`);
create index `idx_p1p_ext_mro_fault_report_service_event_id` on `p1p_ext_mro_fault_report` (`service_event_id`);
create index `idx_p1p_ext_mro_fault_report_repair_order_id` on `p1p_ext_mro_fault_report` (`repair_order_id`);
create index `idx_p1p_ext_mro_fault_report_fault_sym_code` on `p1p_ext_mro_fault_report` (`fault_sym_code`);
create unique index `uq_p1p_ext_mro_remove_install_record_change_no` on `p1p_ext_mro_remove_install_record` (`change_no`);
create index `idx_p1p_ext_mro_remove_install_record_service_event_id` on `p1p_ext_mro_remove_install_record` (`service_event_id`);
create index `idx_p1p_ext_mro_remove_install_record_repair_order_id` on `p1p_ext_mro_remove_install_record` (`repair_order_id`);
create index `idx_p1p_ext_mro_remove_install_record_fault_report_id` on `p1p_ext_mro_remove_install_record` (`fault_report_id`);
create index `idx_p1p_ext_mro_remove_install_record_removed_part_sn` on `p1p_ext_mro_remove_install_record` (`removed_part_sn`);
create index `idx_p1p_ext_mro_remove_install_record_installed_part_sn` on `p1p_ext_mro_remove_install_record` (`installed_part_sn`);
create unique index `uq_p1p_ext_mro_field_feedback_feedback_no` on `p1p_ext_mro_field_feedback` (`feedback_no`);
create index `idx_p1p_ext_mro_field_feedback_service_event_id` on `p1p_ext_mro_field_feedback` (`service_event_id`);
create index `idx_p1p_ext_mro_field_feedback_fault_report_id` on `p1p_ext_mro_field_feedback` (`fault_report_id`);
create index `idx_p1p_ext_mro_field_feedback_quality_event_no` on `p1p_ext_mro_field_feedback` (`quality_event_no`);
create index `idx_p1p_ext_mro_field_feedback_component_serial_no` on `p1p_ext_mro_field_feedback` (`component_serial_no`);

set foreign_key_checks = 1;
