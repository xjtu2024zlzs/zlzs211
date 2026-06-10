-- Auto-generated from DBML for CF aviation simulated dataset.
-- Source DBML: ERP源系统数据库表设计.dbml
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

CREATE TABLE IF NOT EXISTS "item_master" (
  "item_id" SERIAL PRIMARY KEY,
  "item_no" VARCHAR(100) NOT NULL,
  "item_desc" VARCHAR(200) NOT NULL,
  "item_grp_cd" VARCHAR(50),
  "mat_grade" VARCHAR(100),
  "spec_model" VARCHAR(200),
  "uom_cd" VARCHAR(30),
  "buy_made_flag" VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS "vendor_master" (
  "vendor_id" SERIAL PRIMARY KEY,
  "vendor_code" VARCHAR(100) NOT NULL,
  "vendor_name" VARCHAR(200) NOT NULL,
  "vendor_kind_cd" VARCHAR(30),
  "q_level_cd" VARCHAR(30),
  "contact_name" VARCHAR(100),
  "tel_no" VARCHAR(50),
  "addr_txt" TEXT,
  "valid_flag" VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS "purchase_order" (
  "po_id" SERIAL PRIMARY KEY,
  "vendor_id" INTEGER NOT NULL,
  "po_no" VARCHAR(100) NOT NULL,
  "task_no" VARCHAR(100),
  "order_dt_txt" VARCHAR(20),
  "plan_arrive_yyyymmdd" VARCHAR(8),
  "po_stat_cd" VARCHAR(30),
  "pri_lvl" VARCHAR(10),
  "urgent_tab" VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS "purchase_line" (
  "po_line_id" SERIAL PRIMARY KEY,
  "po_id" INTEGER NOT NULL,
  "item_id" INTEGER NOT NULL,
  "line_seq" VARCHAR(20) NOT NULL,
  "req_qty_txt" VARCHAR(40),
  "order_uom" VARCHAR(30),
  "need_dt_char" VARCHAR(12),
  "dlv_dt_txt" VARCHAR(20),
  "line_stat_cd" VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS "vendor_batch" (
  "vendor_batch_id" SERIAL PRIMARY KEY,
  "po_line_id" INTEGER NOT NULL,
  "vendor_id" INTEGER NOT NULL,
  "item_id" INTEGER NOT NULL,
  "ven_lot_no" VARCHAR(100) NOT NULL,
  "cert_doc_no" VARCHAR(100),
  "mfg_dt_txt" VARCHAR(20),
  "exp_dt_txt" VARCHAR(20),
  "batch_stat_cd" VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS "inventory_lot" (
  "inv_lot_id" SERIAL PRIMARY KEY,
  "vendor_batch_id" INTEGER,
  "item_id" INTEGER NOT NULL,
  "stock_lot_no" VARCHAR(100) NOT NULL,
  "wh_code" VARCHAR(100),
  "bin_code" VARCHAR(100),
  "onhand_qty_chr" VARCHAR(40),
  "qty_uom" VARCHAR(30),
  "inv_status_cd" VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS "receipt_bill" (
  "receipt_id" SERIAL PRIMARY KEY,
  "inv_lot_id" INTEGER NOT NULL,
  "po_line_id" INTEGER,
  "recv_no" VARCHAR(100) NOT NULL,
  "recv_time_txt" VARCHAR(30),
  "recv_qty_txt" VARCHAR(40),
  "wh_keeper" VARCHAR(100),
  "iqc_flag" VARCHAR(10),
  "recv_stat_cd" VARCHAR(30)
);

CREATE TABLE IF NOT EXISTS "issue_bill" (
  "issue_id" SERIAL PRIMARY KEY,
  "inv_lot_id" INTEGER NOT NULL,
  "mo_no" VARCHAR(100),
  "issue_no" VARCHAR(100) NOT NULL,
  "issue_time_txt" VARCHAR(30),
  "issue_qty_txt" VARCHAR(40),
  "picker_name" VARCHAR(100),
  "use_for" VARCHAR(200),
  "issue_stat_cd" VARCHAR(30)
);

-- Comments
COMMENT ON TABLE "item_master" IS 'ERP 物料主数据表，提供作动筒外购件、密封件、原材料和外协件的物料基础信息';
COMMENT ON COLUMN "item_master"."item_id" IS '物料主数据内部主键，ERP 单列表主键';
COMMENT ON COLUMN "item_master"."item_no" IS 'ERP 物料编码，外购件和原材料的业务编号';
COMMENT ON COLUMN "item_master"."item_desc" IS '物料描述或物料名称';
COMMENT ON COLUMN "item_master"."item_grp_cd" IS '物料组代码，ERP 内部分类编码';
COMMENT ON COLUMN "item_master"."mat_grade" IS '材料牌号或材质等级';
COMMENT ON COLUMN "item_master"."spec_model" IS '规格型号，可能含尺寸和执行标准';
COMMENT ON COLUMN "item_master"."uom_cd" IS '库存主计量单位代码';
COMMENT ON COLUMN "item_master"."buy_made_flag" IS '采购自制标识，例如 B 表示外购，M 表示自制';

COMMENT ON TABLE "vendor_master" IS 'ERP 供应商主数据表，记录采购来源和供应商准入信息';
COMMENT ON COLUMN "vendor_master"."vendor_id" IS '供应商内部主键，ERP 单列表主键';
COMMENT ON COLUMN "vendor_master"."vendor_code" IS '供应商编码，ERP 采购业务中的稳定业务键';
COMMENT ON COLUMN "vendor_master"."vendor_name" IS '供应商名称';
COMMENT ON COLUMN "vendor_master"."vendor_kind_cd" IS '供应商类别代码，例如材料、密封件、传感器、外协加工';
COMMENT ON COLUMN "vendor_master"."q_level_cd" IS '供应商资质等级代码，ERP 质量准入评级';
COMMENT ON COLUMN "vendor_master"."contact_name" IS '供应商联系人姓名';
COMMENT ON COLUMN "vendor_master"."tel_no" IS '供应商联系电话';
COMMENT ON COLUMN "vendor_master"."addr_txt" IS '供应商地址文本';
COMMENT ON COLUMN "vendor_master"."valid_flag" IS '有效标识，例如 Y 表示启用，N 表示停用';

COMMENT ON TABLE "purchase_order" IS 'ERP 采购订单头表，记录供应商、订单号、交期、状态和优先级';
COMMENT ON COLUMN "purchase_order"."po_id" IS '采购订单内部主键，ERP 单列表主键';
COMMENT ON COLUMN "purchase_order"."vendor_id" IS '供应商内部主键，关联供应商主数据';
COMMENT ON COLUMN "purchase_order"."po_no" IS '采购订单号，ERP 采购单据业务编号';
COMMENT ON COLUMN "purchase_order"."task_no" IS '采购任务号或项目任务编码';
COMMENT ON COLUMN "purchase_order"."order_dt_txt" IS '下单日期文本，常见格式如 20260508 或 2026/05/08';
COMMENT ON COLUMN "purchase_order"."plan_arrive_yyyymmdd" IS '计划到货日期文本，YYYYMMDD 格式';
COMMENT ON COLUMN "purchase_order"."po_stat_cd" IS '采购订单状态代码，ERP 私有枚举';
COMMENT ON COLUMN "purchase_order"."pri_lvl" IS '优先级文本代码，例如 P1、P2、P3';
COMMENT ON COLUMN "purchase_order"."urgent_tab" IS '紧急标识，历史字段名保留 tab 习惯';

COMMENT ON TABLE "purchase_line" IS 'ERP 采购订单行表，记录每一项采购物料、数量、单位和行状态';
COMMENT ON COLUMN "purchase_line"."po_line_id" IS '采购订单行内部主键，ERP 单列表主键';
COMMENT ON COLUMN "purchase_line"."po_id" IS '采购订单内部主键，关联采购订单头';
COMMENT ON COLUMN "purchase_line"."item_id" IS '物料内部主键，关联采购物料';
COMMENT ON COLUMN "purchase_line"."line_seq" IS '采购行序号，历史系统可能保存为字符';
COMMENT ON COLUMN "purchase_line"."req_qty_txt" IS '采购数量文本，可能带单位或千分位';
COMMENT ON COLUMN "purchase_line"."order_uom" IS '采购计量单位代码';
COMMENT ON COLUMN "purchase_line"."need_dt_char" IS '需求日期字符值，常见格式如 20260610';
COMMENT ON COLUMN "purchase_line"."dlv_dt_txt" IS '订单行计划到货日期文本';
COMMENT ON COLUMN "purchase_line"."line_stat_cd" IS '采购行状态代码，ERP 私有枚举';

COMMENT ON TABLE "vendor_batch" IS 'ERP 供应商批次表，承接采购行与供应商来料批次的来源追溯';
COMMENT ON COLUMN "vendor_batch"."vendor_batch_id" IS '供应商批次内部主键，ERP 单列表主键';
COMMENT ON COLUMN "vendor_batch"."po_line_id" IS '来源采购订单行内部主键';
COMMENT ON COLUMN "vendor_batch"."vendor_id" IS '供应商内部主键，便于按供应商批次追溯';
COMMENT ON COLUMN "vendor_batch"."item_id" IS '物料内部主键，批次对应的采购物料';
COMMENT ON COLUMN "vendor_batch"."ven_lot_no" IS '供应商批次号，供应商随货提供的 lot 编号';
COMMENT ON COLUMN "vendor_batch"."cert_doc_no" IS '合格证或随货质量证明文件号';
COMMENT ON COLUMN "vendor_batch"."mfg_dt_txt" IS '供应商生产日期文本';
COMMENT ON COLUMN "vendor_batch"."exp_dt_txt" IS '有效期或失效日期文本';
COMMENT ON COLUMN "vendor_batch"."batch_stat_cd" IS '供应商批次状态代码，ERP 私有枚举';

COMMENT ON TABLE "inventory_lot" IS 'ERP 库存批次表，连接供应商批次、入库和生产领料';
COMMENT ON COLUMN "inventory_lot"."inv_lot_id" IS '库存批次内部主键，ERP 单列表主键';
COMMENT ON COLUMN "inventory_lot"."vendor_batch_id" IS '供应商批次内部主键，标识库存批次来源';
COMMENT ON COLUMN "inventory_lot"."item_id" IS '物料内部主键，库存批次对应物料';
COMMENT ON COLUMN "inventory_lot"."stock_lot_no" IS '企业库存批次号，入库后生成的内部批号';
COMMENT ON COLUMN "inventory_lot"."wh_code" IS '仓库编码';
COMMENT ON COLUMN "inventory_lot"."bin_code" IS '库位或货位编码';
COMMENT ON COLUMN "inventory_lot"."onhand_qty_chr" IS '库存现存量字符值，可能带单位或格式符号';
COMMENT ON COLUMN "inventory_lot"."qty_uom" IS '库存数量单位代码';
COMMENT ON COLUMN "inventory_lot"."inv_status_cd" IS '库存批次状态代码，例如可用、冻结、待检';

COMMENT ON TABLE "receipt_bill" IS 'ERP 入库单表，记录采购到货后形成库存批次的入库业务';
COMMENT ON COLUMN "receipt_bill"."receipt_id" IS '入库单内部主键，ERP 单列表主键';
COMMENT ON COLUMN "receipt_bill"."inv_lot_id" IS '形成的库存批次内部主键';
COMMENT ON COLUMN "receipt_bill"."po_line_id" IS '来源采购订单行内部主键';
COMMENT ON COLUMN "receipt_bill"."recv_no" IS '入库单号，ERP 仓储业务编号';
COMMENT ON COLUMN "receipt_bill"."recv_time_txt" IS '入库时间文本，可能为日期或日期时间格式';
COMMENT ON COLUMN "receipt_bill"."recv_qty_txt" IS '入库数量文本，可能带单位或小数格式';
COMMENT ON COLUMN "receipt_bill"."wh_keeper" IS '仓库保管员或入库经办人';
COMMENT ON COLUMN "receipt_bill"."iqc_flag" IS '是否需要来料检验标识，例如 1 或 0';
COMMENT ON COLUMN "receipt_bill"."recv_stat_cd" IS '入库单状态代码，ERP 私有枚举';

COMMENT ON TABLE "issue_bill" IS 'ERP 领料单表，记录库存批次被生产或装配工单领用的业务';
COMMENT ON COLUMN "issue_bill"."issue_id" IS '领料单内部主键，ERP 单列表主键';
COMMENT ON COLUMN "issue_bill"."inv_lot_id" IS '领用库存批次内部主键';
COMMENT ON COLUMN "issue_bill"."mo_no" IS '生产或装配工单号，用于跨系统关联 MES 工单';
COMMENT ON COLUMN "issue_bill"."issue_no" IS '领料单号，ERP 仓储出库业务编号';
COMMENT ON COLUMN "issue_bill"."issue_time_txt" IS '领料时间文本，可能为日期或日期时间格式';
COMMENT ON COLUMN "issue_bill"."issue_qty_txt" IS '领料数量文本，可能带单位或小数格式';
COMMENT ON COLUMN "issue_bill"."picker_name" IS '领料人姓名';
COMMENT ON COLUMN "issue_bill"."use_for" IS '领料用途或投料去向说明';
COMMENT ON COLUMN "issue_bill"."issue_stat_cd" IS '领料单状态代码，ERP 私有枚举';

-- Foreign keys
ALTER TABLE "purchase_order" ADD CONSTRAINT "fk_purchase_order_vendor_id" FOREIGN KEY ("vendor_id") REFERENCES "vendor_master" ("vendor_id");
ALTER TABLE "purchase_line" ADD CONSTRAINT "fk_purchase_line_po_id" FOREIGN KEY ("po_id") REFERENCES "purchase_order" ("po_id");
ALTER TABLE "purchase_line" ADD CONSTRAINT "fk_purchase_line_item_id" FOREIGN KEY ("item_id") REFERENCES "item_master" ("item_id");
ALTER TABLE "vendor_batch" ADD CONSTRAINT "fk_vendor_batch_po_line_id" FOREIGN KEY ("po_line_id") REFERENCES "purchase_line" ("po_line_id");
ALTER TABLE "vendor_batch" ADD CONSTRAINT "fk_vendor_batch_vendor_id" FOREIGN KEY ("vendor_id") REFERENCES "vendor_master" ("vendor_id");
ALTER TABLE "vendor_batch" ADD CONSTRAINT "fk_vendor_batch_item_id" FOREIGN KEY ("item_id") REFERENCES "item_master" ("item_id");
ALTER TABLE "inventory_lot" ADD CONSTRAINT "fk_inventory_lot_vendor_batch_id" FOREIGN KEY ("vendor_batch_id") REFERENCES "vendor_batch" ("vendor_batch_id");
ALTER TABLE "inventory_lot" ADD CONSTRAINT "fk_inventory_lot_item_id" FOREIGN KEY ("item_id") REFERENCES "item_master" ("item_id");
ALTER TABLE "receipt_bill" ADD CONSTRAINT "fk_receipt_bill_inv_lot_id" FOREIGN KEY ("inv_lot_id") REFERENCES "inventory_lot" ("inv_lot_id");
ALTER TABLE "receipt_bill" ADD CONSTRAINT "fk_receipt_bill_po_line_id" FOREIGN KEY ("po_line_id") REFERENCES "purchase_line" ("po_line_id");
ALTER TABLE "issue_bill" ADD CONSTRAINT "fk_issue_bill_inv_lot_id" FOREIGN KEY ("inv_lot_id") REFERENCES "inventory_lot" ("inv_lot_id");

-- Indexes
CREATE UNIQUE INDEX IF NOT EXISTS "uq_item_master_item_no" ON "item_master" ("item_no");
CREATE INDEX IF NOT EXISTS "idx_item_master_item_grp_cd" ON "item_master" ("item_grp_cd");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_vendor_master_vendor_code" ON "vendor_master" ("vendor_code");
CREATE INDEX IF NOT EXISTS "idx_vendor_master_vendor_kind_cd" ON "vendor_master" ("vendor_kind_cd");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_purchase_order_po_no" ON "purchase_order" ("po_no");
CREATE INDEX IF NOT EXISTS "idx_purchase_order_vendor_id" ON "purchase_order" ("vendor_id");
CREATE INDEX IF NOT EXISTS "idx_purchase_order_task_no" ON "purchase_order" ("task_no");
CREATE INDEX IF NOT EXISTS "idx_purchase_line_po_id" ON "purchase_line" ("po_id");
CREATE INDEX IF NOT EXISTS "idx_purchase_line_item_id" ON "purchase_line" ("item_id");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_purchase_line_po_id_line_seq" ON "purchase_line" ("po_id", "line_seq");
CREATE INDEX IF NOT EXISTS "idx_vendor_batch_ven_lot_no" ON "vendor_batch" ("ven_lot_no");
CREATE INDEX IF NOT EXISTS "idx_vendor_batch_po_line_id" ON "vendor_batch" ("po_line_id");
CREATE INDEX IF NOT EXISTS "idx_vendor_batch_vendor_id" ON "vendor_batch" ("vendor_id");
CREATE INDEX IF NOT EXISTS "idx_vendor_batch_item_id" ON "vendor_batch" ("item_id");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_inventory_lot_stock_lot_no" ON "inventory_lot" ("stock_lot_no");
CREATE INDEX IF NOT EXISTS "idx_inventory_lot_vendor_batch_id" ON "inventory_lot" ("vendor_batch_id");
CREATE INDEX IF NOT EXISTS "idx_inventory_lot_item_id" ON "inventory_lot" ("item_id");
CREATE INDEX IF NOT EXISTS "idx_inventory_lot_wh_code_bin_code" ON "inventory_lot" ("wh_code", "bin_code");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_receipt_bill_recv_no" ON "receipt_bill" ("recv_no");
CREATE INDEX IF NOT EXISTS "idx_receipt_bill_inv_lot_id" ON "receipt_bill" ("inv_lot_id");
CREATE INDEX IF NOT EXISTS "idx_receipt_bill_po_line_id" ON "receipt_bill" ("po_line_id");
CREATE UNIQUE INDEX IF NOT EXISTS "uq_issue_bill_issue_no" ON "issue_bill" ("issue_no");
CREATE INDEX IF NOT EXISTS "idx_issue_bill_inv_lot_id" ON "issue_bill" ("inv_lot_id");
CREATE INDEX IF NOT EXISTS "idx_issue_bill_mo_no" ON "issue_bill" ("mo_no");
