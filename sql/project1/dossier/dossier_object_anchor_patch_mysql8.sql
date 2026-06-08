-- =============================================================================
-- Digital dossier object anchor patch - MySQL 8.0
-- Purpose:
--   Add and backfill direct object anchors for lifecycle business records.
--
-- Anchor columns:
--   aircraft_id        Root aircraft.
--   bom_node_id        Current configuration/BOM node.
--   part_instance_id   Physical instance when the record is instance-specific.
--   object_level       aircraft/system/subsystem/equipment/component/part.
--   object_profile_id  Unified six-level object identity in t1_product_object_profile.
--
-- Why:
--   Dossier generation and drilldown pages should not infer a record's object
--   only through long chains such as inspection -> task -> order -> part number.
--   These anchors make lifecycle data directly retrievable by current node.
-- =============================================================================

SET NAMES utf8mb4;
USE `ry-cloud`;

DROP PROCEDURE IF EXISTS dossier_add_column_if_missing;
DROP PROCEDURE IF EXISTS dossier_add_index_if_missing;

DELIMITER //

CREATE PROCEDURE dossier_add_column_if_missing(
  IN p_table_name varchar(128),
  IN p_column_name varchar(128),
  IN p_column_definition text
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND column_name = p_column_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN ', p_column_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

CREATE PROCEDURE dossier_add_index_if_missing(
  IN p_table_name varchar(128),
  IN p_index_name varchar(128),
  IN p_index_definition text
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND index_name = p_index_name
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ', p_index_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//

DELIMITER ;

-- -----------------------------------------------------------------------------
-- 1. Add anchor columns.
-- -----------------------------------------------------------------------------

-- Manufacturing and quality.
CALL dossier_add_column_if_missing('t1_shop_order', 'bom_node_id', '`bom_node_id` char(36) DEFAULT NULL COMMENT ''对象锚点：BOM节点ID'' AFTER `aircraft_id`');
CALL dossier_add_column_if_missing('t1_shop_order', 'part_instance_id', '`part_instance_id` char(36) DEFAULT NULL COMMENT ''对象锚点：实物实例ID'' AFTER `bom_node_id`');
CALL dossier_add_column_if_missing('t1_shop_order', 'object_level', '`object_level` varchar(30) DEFAULT NULL COMMENT ''对象锚点：层级'' AFTER `part_instance_id`');
CALL dossier_add_column_if_missing('t1_shop_order', 'object_profile_id', '`object_profile_id` char(36) DEFAULT NULL COMMENT ''对象锚点：统一对象ID'' AFTER `object_level`');

CALL dossier_add_column_if_missing('t1_shop_order_task', 'aircraft_id', '`aircraft_id` char(36) DEFAULT NULL COMMENT ''对象锚点：飞机ID'' AFTER `shop_order_id`');
CALL dossier_add_column_if_missing('t1_shop_order_task', 'bom_node_id', '`bom_node_id` char(36) DEFAULT NULL COMMENT ''对象锚点：BOM节点ID'' AFTER `aircraft_id`');
CALL dossier_add_column_if_missing('t1_shop_order_task', 'part_instance_id', '`part_instance_id` char(36) DEFAULT NULL COMMENT ''对象锚点：实物实例ID'' AFTER `bom_node_id`');
CALL dossier_add_column_if_missing('t1_shop_order_task', 'object_level', '`object_level` varchar(30) DEFAULT NULL COMMENT ''对象锚点：层级'' AFTER `part_instance_id`');
CALL dossier_add_column_if_missing('t1_shop_order_task', 'object_profile_id', '`object_profile_id` char(36) DEFAULT NULL COMMENT ''对象锚点：统一对象ID'' AFTER `object_level`');

CALL dossier_add_column_if_missing('t1_production_operation_record', 'aircraft_id', '`aircraft_id` char(36) DEFAULT NULL COMMENT ''对象锚点：飞机ID'' AFTER `shop_order_task_id`');
CALL dossier_add_column_if_missing('t1_production_operation_record', 'bom_node_id', '`bom_node_id` char(36) DEFAULT NULL COMMENT ''对象锚点：BOM节点ID'' AFTER `aircraft_id`');
CALL dossier_add_column_if_missing('t1_production_operation_record', 'part_instance_id', '`part_instance_id` char(36) DEFAULT NULL COMMENT ''对象锚点：实物实例ID'' AFTER `bom_node_id`');
CALL dossier_add_column_if_missing('t1_production_operation_record', 'object_level', '`object_level` varchar(30) DEFAULT NULL COMMENT ''对象锚点：层级'' AFTER `part_instance_id`');
CALL dossier_add_column_if_missing('t1_production_operation_record', 'object_profile_id', '`object_profile_id` char(36) DEFAULT NULL COMMENT ''对象锚点：统一对象ID'' AFTER `object_level`');

CALL dossier_add_column_if_missing('t1_material_lot_trace', 'aircraft_id', '`aircraft_id` char(36) DEFAULT NULL COMMENT ''对象锚点：飞机ID'' AFTER `shop_order_task_id`');
CALL dossier_add_column_if_missing('t1_material_lot_trace', 'bom_node_id', '`bom_node_id` char(36) DEFAULT NULL COMMENT ''对象锚点：BOM节点ID'' AFTER `aircraft_id`');
CALL dossier_add_column_if_missing('t1_material_lot_trace', 'part_instance_id', '`part_instance_id` char(36) DEFAULT NULL COMMENT ''对象锚点：实物实例ID'' AFTER `bom_node_id`');
CALL dossier_add_column_if_missing('t1_material_lot_trace', 'object_level', '`object_level` varchar(30) DEFAULT NULL COMMENT ''对象锚点：层级'' AFTER `part_instance_id`');
CALL dossier_add_column_if_missing('t1_material_lot_trace', 'object_profile_id', '`object_profile_id` char(36) DEFAULT NULL COMMENT ''对象锚点：统一对象ID'' AFTER `object_level`');

CALL dossier_add_column_if_missing('t1_inspection_record', 'aircraft_id', '`aircraft_id` char(36) DEFAULT NULL COMMENT ''对象锚点：飞机ID'' AFTER `shop_order_task_id`');
CALL dossier_add_column_if_missing('t1_inspection_record', 'bom_node_id', '`bom_node_id` char(36) DEFAULT NULL COMMENT ''对象锚点：BOM节点ID'' AFTER `aircraft_id`');
CALL dossier_add_column_if_missing('t1_inspection_record', 'part_instance_id', '`part_instance_id` char(36) DEFAULT NULL COMMENT ''对象锚点：实物实例ID'' AFTER `bom_node_id`');
CALL dossier_add_column_if_missing('t1_inspection_record', 'object_level', '`object_level` varchar(30) DEFAULT NULL COMMENT ''对象锚点：层级'' AFTER `part_instance_id`');
CALL dossier_add_column_if_missing('t1_inspection_record', 'object_profile_id', '`object_profile_id` char(36) DEFAULT NULL COMMENT ''对象锚点：统一对象ID'' AFTER `object_level`');

CALL dossier_add_column_if_missing('t1_inspection_measurement', 'aircraft_id', '`aircraft_id` char(36) DEFAULT NULL COMMENT ''对象锚点：飞机ID'' AFTER `inspection_record_id`');
CALL dossier_add_column_if_missing('t1_inspection_measurement', 'bom_node_id', '`bom_node_id` char(36) DEFAULT NULL COMMENT ''对象锚点：BOM节点ID'' AFTER `aircraft_id`');
CALL dossier_add_column_if_missing('t1_inspection_measurement', 'part_instance_id', '`part_instance_id` char(36) DEFAULT NULL COMMENT ''对象锚点：实物实例ID'' AFTER `bom_node_id`');
CALL dossier_add_column_if_missing('t1_inspection_measurement', 'object_level', '`object_level` varchar(30) DEFAULT NULL COMMENT ''对象锚点：层级'' AFTER `part_instance_id`');
CALL dossier_add_column_if_missing('t1_inspection_measurement', 'object_profile_id', '`object_profile_id` char(36) DEFAULT NULL COMMENT ''对象锚点：统一对象ID'' AFTER `object_level`');

CALL dossier_add_column_if_missing('t1_assembly_record', 'aircraft_id', '`aircraft_id` char(36) DEFAULT NULL COMMENT ''对象锚点：飞机ID'' AFTER `instance_id`');
CALL dossier_add_column_if_missing('t1_assembly_record', 'bom_node_id', '`bom_node_id` char(36) DEFAULT NULL COMMENT ''对象锚点：BOM节点ID'' AFTER `aircraft_id`');
CALL dossier_add_column_if_missing('t1_assembly_record', 'part_instance_id', '`part_instance_id` char(36) DEFAULT NULL COMMENT ''对象锚点：实物实例ID'' AFTER `bom_node_id`');
CALL dossier_add_column_if_missing('t1_assembly_record', 'object_level', '`object_level` varchar(30) DEFAULT NULL COMMENT ''对象锚点：层级'' AFTER `part_instance_id`');
CALL dossier_add_column_if_missing('t1_assembly_record', 'object_profile_id', '`object_profile_id` char(36) DEFAULT NULL COMMENT ''对象锚点：统一对象ID'' AFTER `object_level`');

-- Service and fault.
CALL dossier_add_column_if_missing('t1_install_removal', 'bom_node_id', '`bom_node_id` char(36) DEFAULT NULL COMMENT ''对象锚点：BOM节点ID'' AFTER `aircraft_id`');
CALL dossier_add_column_if_missing('t1_install_removal', 'part_instance_id', '`part_instance_id` char(36) DEFAULT NULL COMMENT ''对象锚点：实物实例ID'' AFTER `bom_node_id`');
CALL dossier_add_column_if_missing('t1_install_removal', 'object_level', '`object_level` varchar(30) DEFAULT NULL COMMENT ''对象锚点：层级'' AFTER `part_instance_id`');
CALL dossier_add_column_if_missing('t1_install_removal', 'object_profile_id', '`object_profile_id` char(36) DEFAULT NULL COMMENT ''对象锚点：统一对象ID'' AFTER `object_level`');

CALL dossier_add_column_if_missing('t1_fault_event', 'bom_node_id', '`bom_node_id` char(36) DEFAULT NULL COMMENT ''对象锚点：BOM节点ID'' AFTER `aircraft_id`');
CALL dossier_add_column_if_missing('t1_fault_event', 'part_instance_id', '`part_instance_id` char(36) DEFAULT NULL COMMENT ''对象锚点：实物实例ID'' AFTER `bom_node_id`');
CALL dossier_add_column_if_missing('t1_fault_event', 'object_level', '`object_level` varchar(30) DEFAULT NULL COMMENT ''对象锚点：层级'' AFTER `part_instance_id`');
CALL dossier_add_column_if_missing('t1_fault_event', 'object_profile_id', '`object_profile_id` char(36) DEFAULT NULL COMMENT ''对象锚点：统一对象ID'' AFTER `object_level`');

CALL dossier_add_column_if_missing('t1_fault_action', 'aircraft_id', '`aircraft_id` char(36) DEFAULT NULL COMMENT ''对象锚点：飞机ID'' AFTER `fault_event_id`');
CALL dossier_add_column_if_missing('t1_fault_action', 'bom_node_id', '`bom_node_id` char(36) DEFAULT NULL COMMENT ''对象锚点：BOM节点ID'' AFTER `aircraft_id`');
CALL dossier_add_column_if_missing('t1_fault_action', 'part_instance_id', '`part_instance_id` char(36) DEFAULT NULL COMMENT ''对象锚点：实物实例ID'' AFTER `bom_node_id`');
CALL dossier_add_column_if_missing('t1_fault_action', 'object_level', '`object_level` varchar(30) DEFAULT NULL COMMENT ''对象锚点：层级'' AFTER `part_instance_id`');
CALL dossier_add_column_if_missing('t1_fault_action', 'object_profile_id', '`object_profile_id` char(36) DEFAULT NULL COMMENT ''对象锚点：统一对象ID'' AFTER `object_level`');

CALL dossier_add_column_if_missing('t1_work_order', 'bom_node_id', '`bom_node_id` char(36) DEFAULT NULL COMMENT ''对象锚点：BOM节点ID'' AFTER `aircraft_id`');
CALL dossier_add_column_if_missing('t1_work_order', 'part_instance_id', '`part_instance_id` char(36) DEFAULT NULL COMMENT ''对象锚点：实物实例ID'' AFTER `bom_node_id`');
CALL dossier_add_column_if_missing('t1_work_order', 'object_level', '`object_level` varchar(30) DEFAULT NULL COMMENT ''对象锚点：层级'' AFTER `part_instance_id`');
CALL dossier_add_column_if_missing('t1_work_order', 'object_profile_id', '`object_profile_id` char(36) DEFAULT NULL COMMENT ''对象锚点：统一对象ID'' AFTER `object_level`');

CALL dossier_add_column_if_missing('t1_work_order_task', 'aircraft_id', '`aircraft_id` char(36) DEFAULT NULL COMMENT ''对象锚点：飞机ID'' AFTER `wo_id`');
CALL dossier_add_column_if_missing('t1_work_order_task', 'bom_node_id', '`bom_node_id` char(36) DEFAULT NULL COMMENT ''对象锚点：BOM节点ID'' AFTER `aircraft_id`');
CALL dossier_add_column_if_missing('t1_work_order_task', 'part_instance_id', '`part_instance_id` char(36) DEFAULT NULL COMMENT ''对象锚点：实物实例ID'' AFTER `bom_node_id`');
CALL dossier_add_column_if_missing('t1_work_order_task', 'object_level', '`object_level` varchar(30) DEFAULT NULL COMMENT ''对象锚点：层级'' AFTER `part_instance_id`');
CALL dossier_add_column_if_missing('t1_work_order_task', 'object_profile_id', '`object_profile_id` char(36) DEFAULT NULL COMMENT ''对象锚点：统一对象ID'' AFTER `object_level`');

-- Evidence and text records.
CALL dossier_add_column_if_missing('t1_document_entry', 'aircraft_id', '`aircraft_id` char(36) DEFAULT NULL COMMENT ''对象锚点：飞机ID'' AFTER `structure_node_id`');
CALL dossier_add_column_if_missing('t1_document_entry', 'bom_node_id', '`bom_node_id` char(36) DEFAULT NULL COMMENT ''对象锚点：BOM节点ID'' AFTER `aircraft_id`');
CALL dossier_add_column_if_missing('t1_document_entry', 'part_instance_id', '`part_instance_id` char(36) DEFAULT NULL COMMENT ''对象锚点：实物实例ID'' AFTER `bom_node_id`');
CALL dossier_add_column_if_missing('t1_document_entry', 'object_level', '`object_level` varchar(30) DEFAULT NULL COMMENT ''对象锚点：层级'' AFTER `part_instance_id`');
CALL dossier_add_column_if_missing('t1_document_entry', 'object_profile_id', '`object_profile_id` char(36) DEFAULT NULL COMMENT ''对象锚点：统一对象ID'' AFTER `object_level`');

CALL dossier_add_column_if_missing('t1_quality_text_record', 'bom_node_id', '`bom_node_id` char(36) DEFAULT NULL COMMENT ''对象锚点：标准BOM节点ID；由aircraft_bom_node_id回填'' AFTER `aircraft_bom_node_id`');
CALL dossier_add_column_if_missing('t1_quality_text_record', 'object_level', '`object_level` varchar(30) DEFAULT NULL COMMENT ''对象锚点：层级'' AFTER `part_instance_id`');
CALL dossier_add_column_if_missing('t1_quality_text_record', 'object_profile_id', '`object_profile_id` char(36) DEFAULT NULL COMMENT ''对象锚点：统一对象ID'' AFTER `object_level`');

-- -----------------------------------------------------------------------------
-- 2. Add indexes for anchor retrieval.
-- -----------------------------------------------------------------------------
CALL dossier_add_index_if_missing('t1_shop_order', 'idx_so_bom_anchor', 'ADD INDEX `idx_so_bom_anchor` (`aircraft_id`, `bom_node_id`)');
CALL dossier_add_index_if_missing('t1_shop_order', 'idx_so_part_anchor', 'ADD INDEX `idx_so_part_anchor` (`part_instance_id`)');
CALL dossier_add_index_if_missing('t1_shop_order_task', 'idx_sot_bom_anchor', 'ADD INDEX `idx_sot_bom_anchor` (`aircraft_id`, `bom_node_id`)');
CALL dossier_add_index_if_missing('t1_shop_order_task', 'idx_sot_part_anchor', 'ADD INDEX `idx_sot_part_anchor` (`part_instance_id`)');
CALL dossier_add_index_if_missing('t1_production_operation_record', 'idx_por_bom_anchor', 'ADD INDEX `idx_por_bom_anchor` (`aircraft_id`, `bom_node_id`)');
CALL dossier_add_index_if_missing('t1_production_operation_record', 'idx_por_part_anchor', 'ADD INDEX `idx_por_part_anchor` (`part_instance_id`)');
CALL dossier_add_index_if_missing('t1_material_lot_trace', 'idx_mlt_bom_anchor', 'ADD INDEX `idx_mlt_bom_anchor` (`aircraft_id`, `bom_node_id`)');
CALL dossier_add_index_if_missing('t1_material_lot_trace', 'idx_mlt_part_anchor', 'ADD INDEX `idx_mlt_part_anchor` (`part_instance_id`)');
CALL dossier_add_index_if_missing('t1_inspection_record', 'idx_ir_bom_anchor', 'ADD INDEX `idx_ir_bom_anchor` (`aircraft_id`, `bom_node_id`)');
CALL dossier_add_index_if_missing('t1_inspection_record', 'idx_ir_part_anchor', 'ADD INDEX `idx_ir_part_anchor` (`part_instance_id`)');
CALL dossier_add_index_if_missing('t1_inspection_measurement', 'idx_im_bom_anchor', 'ADD INDEX `idx_im_bom_anchor` (`aircraft_id`, `bom_node_id`)');
CALL dossier_add_index_if_missing('t1_inspection_measurement', 'idx_im_part_anchor', 'ADD INDEX `idx_im_part_anchor` (`part_instance_id`)');
CALL dossier_add_index_if_missing('t1_assembly_record', 'idx_ar_bom_anchor', 'ADD INDEX `idx_ar_bom_anchor` (`aircraft_id`, `bom_node_id`)');
CALL dossier_add_index_if_missing('t1_assembly_record', 'idx_ar_part_anchor', 'ADD INDEX `idx_ar_part_anchor` (`part_instance_id`)');
CALL dossier_add_index_if_missing('t1_install_removal', 'idx_inst_bom_anchor', 'ADD INDEX `idx_inst_bom_anchor` (`aircraft_id`, `bom_node_id`)');
CALL dossier_add_index_if_missing('t1_install_removal', 'idx_inst_part_anchor', 'ADD INDEX `idx_inst_part_anchor` (`part_instance_id`)');
CALL dossier_add_index_if_missing('t1_fault_event', 'idx_fe_bom_anchor', 'ADD INDEX `idx_fe_bom_anchor` (`aircraft_id`, `bom_node_id`)');
CALL dossier_add_index_if_missing('t1_fault_event', 'idx_fe_part_anchor', 'ADD INDEX `idx_fe_part_anchor` (`part_instance_id`)');
CALL dossier_add_index_if_missing('t1_fault_action', 'idx_fa_bom_anchor', 'ADD INDEX `idx_fa_bom_anchor` (`aircraft_id`, `bom_node_id`)');
CALL dossier_add_index_if_missing('t1_fault_action', 'idx_fa_part_anchor', 'ADD INDEX `idx_fa_part_anchor` (`part_instance_id`)');
CALL dossier_add_index_if_missing('t1_work_order', 'idx_wo_bom_anchor', 'ADD INDEX `idx_wo_bom_anchor` (`aircraft_id`, `bom_node_id`)');
CALL dossier_add_index_if_missing('t1_work_order', 'idx_wo_part_anchor', 'ADD INDEX `idx_wo_part_anchor` (`part_instance_id`)');
CALL dossier_add_index_if_missing('t1_work_order_task', 'idx_wot_bom_anchor', 'ADD INDEX `idx_wot_bom_anchor` (`aircraft_id`, `bom_node_id`)');
CALL dossier_add_index_if_missing('t1_work_order_task', 'idx_wot_part_anchor', 'ADD INDEX `idx_wot_part_anchor` (`part_instance_id`)');
CALL dossier_add_index_if_missing('t1_document_entry', 'idx_doc_bom_anchor', 'ADD INDEX `idx_doc_bom_anchor` (`aircraft_id`, `bom_node_id`)');
CALL dossier_add_index_if_missing('t1_document_entry', 'idx_doc_part_anchor', 'ADD INDEX `idx_doc_part_anchor` (`part_instance_id`)');
CALL dossier_add_index_if_missing('t1_quality_text_record', 'idx_qtr_bom_anchor', 'ADD INDEX `idx_qtr_bom_anchor` (`aircraft_id`, `bom_node_id`)');
CALL dossier_add_index_if_missing('t1_quality_text_record', 'idx_qtr_part_anchor', 'ADD INDEX `idx_qtr_part_anchor` (`part_instance_id`)');

-- -----------------------------------------------------------------------------
-- 3. Backfill anchors from existing chains.
-- -----------------------------------------------------------------------------

-- Manufacturing order anchors: aircraft + part number -> active BOM object.
UPDATE t1_shop_order so
LEFT JOIN (
  SELECT aircraft_id, part_number, MIN(id) AS bom_node_id
  FROM t1_aircraft_bom_node
  WHERE is_active = 1
    AND part_number IS NOT NULL
    AND part_number <> ''
  GROUP BY aircraft_id, part_number
) bom_key ON bom_key.aircraft_id = so.aircraft_id AND bom_key.part_number = so.part_number
LEFT JOIN t1_aircraft_bom_node abn ON abn.id = bom_key.bom_node_id
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = abn.id
SET
  so.bom_node_id = COALESCE(so.bom_node_id, abn.id),
  so.part_instance_id = COALESCE(so.part_instance_id, abn.part_instance_id),
  so.object_level = COALESCE(so.object_level, pop.object_level,
    CASE WHEN so.part_number IS NOT NULL AND so.part_number <> '' THEN 'part' ELSE 'aircraft' END),
  so.object_profile_id = COALESCE(so.object_profile_id, pop.id);

UPDATE t1_shop_order_task sot
JOIN t1_shop_order so ON so.id = sot.shop_order_id
SET
  sot.aircraft_id = COALESCE(sot.aircraft_id, so.aircraft_id),
  sot.bom_node_id = COALESCE(sot.bom_node_id, so.bom_node_id),
  sot.part_instance_id = COALESCE(sot.part_instance_id, so.part_instance_id),
  sot.object_level = COALESCE(sot.object_level, so.object_level),
  sot.object_profile_id = COALESCE(sot.object_profile_id, so.object_profile_id);

UPDATE t1_production_operation_record por
JOIN t1_shop_order_task sot ON sot.id = por.shop_order_task_id
SET
  por.aircraft_id = COALESCE(por.aircraft_id, sot.aircraft_id),
  por.bom_node_id = COALESCE(por.bom_node_id, sot.bom_node_id),
  por.part_instance_id = COALESCE(por.part_instance_id, sot.part_instance_id),
  por.object_level = COALESCE(por.object_level, sot.object_level),
  por.object_profile_id = COALESCE(por.object_profile_id, sot.object_profile_id);

UPDATE t1_material_lot_trace mlt
LEFT JOIN t1_shop_order_task sot ON sot.id = mlt.shop_order_task_id
LEFT JOIN t1_part_instance pi ON pi.id = mlt.instance_id
LEFT JOIN t1_aircraft_bom_node abn_by_instance ON abn_by_instance.part_instance_id = mlt.instance_id
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = COALESCE(sot.bom_node_id, abn_by_instance.id)
SET
  mlt.aircraft_id = COALESCE(mlt.aircraft_id, sot.aircraft_id, pi.current_aircraft_id, abn_by_instance.aircraft_id),
  mlt.bom_node_id = COALESCE(mlt.bom_node_id, sot.bom_node_id, abn_by_instance.id, pi.current_node_id),
  mlt.part_instance_id = COALESCE(mlt.part_instance_id, mlt.instance_id, sot.part_instance_id),
  mlt.object_level = COALESCE(mlt.object_level, pop.object_level, 'part'),
  mlt.object_profile_id = COALESCE(mlt.object_profile_id, pop.id);

UPDATE t1_inspection_record ir
JOIN t1_shop_order_task sot ON sot.id = ir.shop_order_task_id
SET
  ir.aircraft_id = COALESCE(ir.aircraft_id, sot.aircraft_id),
  ir.bom_node_id = COALESCE(ir.bom_node_id, sot.bom_node_id),
  ir.part_instance_id = COALESCE(ir.part_instance_id, sot.part_instance_id),
  ir.object_level = COALESCE(ir.object_level, sot.object_level),
  ir.object_profile_id = COALESCE(ir.object_profile_id, sot.object_profile_id);

UPDATE t1_inspection_measurement im
JOIN t1_inspection_record ir ON ir.id = im.inspection_record_id
SET
  im.aircraft_id = COALESCE(im.aircraft_id, ir.aircraft_id),
  im.bom_node_id = COALESCE(im.bom_node_id, ir.bom_node_id),
  im.part_instance_id = COALESCE(im.part_instance_id, ir.part_instance_id),
  im.object_level = COALESCE(im.object_level, ir.object_level),
  im.object_profile_id = COALESCE(im.object_profile_id, ir.object_profile_id);

UPDATE t1_assembly_record ar
JOIN t1_aircraft_bom_node abn ON abn.id = ar.node_id
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = abn.id
SET
  ar.aircraft_id = COALESCE(ar.aircraft_id, abn.aircraft_id),
  ar.bom_node_id = COALESCE(ar.bom_node_id, ar.node_id),
  ar.part_instance_id = COALESCE(ar.part_instance_id, ar.instance_id, abn.part_instance_id),
  ar.object_level = COALESCE(ar.object_level, pop.object_level,
    CASE WHEN UPPER(abn.node_type) = 'CONSUMABLE' THEN 'part' ELSE LOWER(abn.node_type) END),
  ar.object_profile_id = COALESCE(ar.object_profile_id, pop.id);

-- Service and fault anchors.
UPDATE t1_install_removal ir
JOIN t1_aircraft_bom_node abn ON abn.id = ir.node_id
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = abn.id
SET
  ir.bom_node_id = COALESCE(ir.bom_node_id, ir.node_id),
  ir.part_instance_id = COALESCE(ir.part_instance_id, ir.instance_id, abn.part_instance_id),
  ir.object_level = COALESCE(ir.object_level, pop.object_level,
    CASE WHEN UPPER(abn.node_type) = 'CONSUMABLE' THEN 'part' ELSE LOWER(abn.node_type) END),
  ir.object_profile_id = COALESCE(ir.object_profile_id, pop.id);

UPDATE t1_fault_event fe
LEFT JOIN t1_aircraft_bom_node abn ON abn.id = fe.node_id
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = abn.id
SET
  fe.bom_node_id = COALESCE(fe.bom_node_id, fe.node_id),
  fe.part_instance_id = COALESCE(fe.part_instance_id, fe.instance_id, abn.part_instance_id),
  fe.object_level = COALESCE(fe.object_level, pop.object_level,
    CASE
      WHEN abn.id IS NULL THEN 'aircraft'
      WHEN UPPER(abn.node_type) = 'CONSUMABLE' THEN 'part'
      ELSE LOWER(abn.node_type)
    END),
  fe.object_profile_id = COALESCE(fe.object_profile_id, pop.id);

UPDATE t1_work_order wo
LEFT JOIN (
  SELECT work_order_id, MIN(aircraft_id) AS aircraft_id, MIN(node_id) AS bom_node_id, MIN(instance_id) AS part_instance_id
  FROM t1_install_removal
  WHERE work_order_id IS NOT NULL
  GROUP BY work_order_id
) ir ON ir.work_order_id = wo.id
LEFT JOIN t1_aircraft_bom_node abn ON abn.id = ir.bom_node_id
LEFT JOIN t1_product_object_profile pop_node ON pop_node.bom_node_id = abn.id
LEFT JOIN t1_product_object_profile pop_aircraft ON pop_aircraft.aircraft_id = wo.aircraft_id AND pop_aircraft.object_level = 'aircraft'
SET
  wo.bom_node_id = COALESCE(wo.bom_node_id, ir.bom_node_id),
  wo.part_instance_id = COALESCE(wo.part_instance_id, ir.part_instance_id),
  wo.object_level = COALESCE(wo.object_level, pop_node.object_level, 'aircraft'),
  wo.object_profile_id = COALESCE(wo.object_profile_id, pop_node.id, pop_aircraft.id);

UPDATE t1_work_order_task wot
JOIN t1_work_order wo ON wo.id = wot.wo_id
LEFT JOIN t1_aircraft_bom_node abn ON abn.id = COALESCE(wot.node_id, wo.bom_node_id)
LEFT JOIN t1_install_removal ir ON ir.id = wot.install_removal_id
LEFT JOIN t1_fault_event fe ON fe.id = wot.fault_event_id
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = COALESCE(wot.node_id, ir.bom_node_id, fe.bom_node_id, wo.bom_node_id)
SET
  wot.aircraft_id = COALESCE(wot.aircraft_id, wo.aircraft_id, ir.aircraft_id, fe.aircraft_id),
  wot.bom_node_id = COALESCE(wot.bom_node_id, wot.node_id, ir.bom_node_id, fe.bom_node_id, wo.bom_node_id),
  wot.part_instance_id = COALESCE(wot.part_instance_id, ir.part_instance_id, fe.part_instance_id, wo.part_instance_id, abn.part_instance_id),
  wot.object_level = COALESCE(wot.object_level, pop.object_level, wo.object_level),
  wot.object_profile_id = COALESCE(wot.object_profile_id, pop.id, wo.object_profile_id);

UPDATE t1_fault_action fa
JOIN t1_fault_event fe ON fe.id = fa.fault_event_id
LEFT JOIN t1_work_order_task wot ON wot.id = fa.work_order_task_id
LEFT JOIN t1_install_removal ir ON ir.id = fa.install_removal_id
SET
  fa.aircraft_id = COALESCE(fa.aircraft_id, wot.aircraft_id, ir.aircraft_id, fe.aircraft_id),
  fa.bom_node_id = COALESCE(fa.bom_node_id, wot.bom_node_id, ir.bom_node_id, fe.bom_node_id),
  fa.part_instance_id = COALESCE(fa.part_instance_id, wot.part_instance_id, ir.part_instance_id, fe.part_instance_id),
  fa.object_level = COALESCE(fa.object_level, wot.object_level, ir.object_level, fe.object_level),
  fa.object_profile_id = COALESCE(fa.object_profile_id, wot.object_profile_id, ir.object_profile_id, fe.object_profile_id);

-- Evidence and text anchors.
UPDATE t1_document_entry doc
LEFT JOIN t1_dossier_structure_node sn ON sn.id = doc.structure_node_id
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = sn.bom_node_id
SET
  doc.aircraft_id = COALESCE(doc.aircraft_id, sn.aircraft_id),
  doc.bom_node_id = COALESCE(doc.bom_node_id, sn.bom_node_id),
  doc.part_instance_id = COALESCE(doc.part_instance_id, sn.part_instance_id),
  doc.object_level = COALESCE(doc.object_level, sn.object_level, pop.object_level),
  doc.object_profile_id = COALESCE(doc.object_profile_id, pop.id);

UPDATE t1_quality_text_record qtr
LEFT JOIN t1_aircraft_bom_node abn_direct ON abn_direct.id = qtr.aircraft_bom_node_id
LEFT JOIN t1_aircraft_bom_node abn_instance ON abn_instance.part_instance_id = qtr.part_instance_id
LEFT JOIN t1_product_object_profile pop ON pop.bom_node_id = COALESCE(abn_direct.id, abn_instance.id)
SET
  qtr.aircraft_id = COALESCE(qtr.aircraft_id, abn_direct.aircraft_id, abn_instance.aircraft_id),
  qtr.bom_node_id = COALESCE(qtr.bom_node_id, qtr.aircraft_bom_node_id, abn_instance.id),
  qtr.object_level = COALESCE(qtr.object_level, pop.object_level,
    CASE
      WHEN qtr.part_instance_id IS NOT NULL THEN 'part'
      WHEN COALESCE(qtr.aircraft_bom_node_id, abn_instance.id) IS NULL THEN 'aircraft'
      WHEN UPPER(COALESCE(abn_direct.node_type, abn_instance.node_type)) = 'CONSUMABLE' THEN 'part'
      ELSE LOWER(COALESCE(abn_direct.node_type, abn_instance.node_type))
    END),
  qtr.object_profile_id = COALESCE(qtr.object_profile_id, pop.id);

-- -----------------------------------------------------------------------------
-- 4. Enable template sources to use direct anchors when the source table supports
--    them. Existing table-specific filters are preserved.
-- -----------------------------------------------------------------------------
UPDATE t1_dossier_template_data_source
SET
  join_condition_json = JSON_MERGE_PATCH(
    join_condition_json,
    JSON_OBJECT(
      'aircraft_id', '${aircraftId}',
      'bom_node_id', '${bomNodeId}',
      'part_instance_id', '${partInstanceId}',
      'object_level', '${objectLevel}',
      'object_profile_id', '${objectProfileId}'
    )
  ),
  attrs_json = JSON_MERGE_PATCH(attrs_json, JSON_OBJECT('objectAnchorEnabled', TRUE)),
  updated_by = 'system',
  updated_at = CURRENT_TIMESTAMP(6)
WHERE enabled_flag = 1
  AND source_table IN (
    't1_shop_order',
    't1_shop_order_task',
    't1_production_operation_record',
    't1_material_lot_trace',
    't1_inspection_record',
    't1_inspection_measurement',
    't1_assembly_record',
    't1_install_removal',
    't1_fault_event',
    't1_fault_action',
    't1_work_order',
    't1_work_order_task',
    't1_document_entry',
    't1_quality_text_record'
  );

DROP PROCEDURE IF EXISTS dossier_add_column_if_missing;
DROP PROCEDURE IF EXISTS dossier_add_index_if_missing;

-- -----------------------------------------------------------------------------
-- 5. Verification summary.
-- -----------------------------------------------------------------------------
SELECT 'dossier_object_anchor_patch_mysql8.sql applied' AS result;

SELECT 't1_shop_order_task' AS table_name, COUNT(*) AS total_rows,
       SUM(aircraft_id IS NOT NULL) AS aircraft_anchored,
       SUM(bom_node_id IS NOT NULL) AS bom_anchored,
       SUM(part_instance_id IS NOT NULL) AS part_anchored,
       SUM(object_profile_id IS NOT NULL) AS object_anchored
FROM t1_shop_order_task
UNION ALL
SELECT 't1_production_operation_record', COUNT(*), SUM(aircraft_id IS NOT NULL), SUM(bom_node_id IS NOT NULL), SUM(part_instance_id IS NOT NULL), SUM(object_profile_id IS NOT NULL)
FROM t1_production_operation_record
UNION ALL
SELECT 't1_material_lot_trace', COUNT(*), SUM(aircraft_id IS NOT NULL), SUM(bom_node_id IS NOT NULL), SUM(part_instance_id IS NOT NULL), SUM(object_profile_id IS NOT NULL)
FROM t1_material_lot_trace
UNION ALL
SELECT 't1_inspection_record', COUNT(*), SUM(aircraft_id IS NOT NULL), SUM(bom_node_id IS NOT NULL), SUM(part_instance_id IS NOT NULL), SUM(object_profile_id IS NOT NULL)
FROM t1_inspection_record
UNION ALL
SELECT 't1_inspection_measurement', COUNT(*), SUM(aircraft_id IS NOT NULL), SUM(bom_node_id IS NOT NULL), SUM(part_instance_id IS NOT NULL), SUM(object_profile_id IS NOT NULL)
FROM t1_inspection_measurement
UNION ALL
SELECT 't1_assembly_record', COUNT(*), SUM(aircraft_id IS NOT NULL), SUM(bom_node_id IS NOT NULL), SUM(part_instance_id IS NOT NULL), SUM(object_profile_id IS NOT NULL)
FROM t1_assembly_record
UNION ALL
SELECT 't1_install_removal', COUNT(*), SUM(aircraft_id IS NOT NULL), SUM(bom_node_id IS NOT NULL), SUM(part_instance_id IS NOT NULL), SUM(object_profile_id IS NOT NULL)
FROM t1_install_removal
UNION ALL
SELECT 't1_fault_event', COUNT(*), SUM(aircraft_id IS NOT NULL), SUM(bom_node_id IS NOT NULL), SUM(part_instance_id IS NOT NULL), SUM(object_profile_id IS NOT NULL)
FROM t1_fault_event
UNION ALL
SELECT 't1_work_order_task', COUNT(*), SUM(aircraft_id IS NOT NULL), SUM(bom_node_id IS NOT NULL), SUM(part_instance_id IS NOT NULL), SUM(object_profile_id IS NOT NULL)
FROM t1_work_order_task
UNION ALL
SELECT 't1_document_entry', COUNT(*), SUM(aircraft_id IS NOT NULL), SUM(bom_node_id IS NOT NULL), SUM(part_instance_id IS NOT NULL), SUM(object_profile_id IS NOT NULL)
FROM t1_document_entry
UNION ALL
SELECT 't1_quality_text_record', COUNT(*), SUM(aircraft_id IS NOT NULL), SUM(bom_node_id IS NOT NULL), SUM(part_instance_id IS NOT NULL), SUM(object_profile_id IS NOT NULL)
FROM t1_quality_text_record;
