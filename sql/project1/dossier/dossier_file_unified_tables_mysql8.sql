-- =============================================================================
-- Digital dossier unified file tables - MySQL 8.0
-- Purpose: replace the scattered document/file tables with one file asset table,
-- one file-to-business-object relation table, and one optional category tree.
--
-- Old tables covered by this design:
-- t1_document_category, t1_document_master, t1_document_entry, t1_document_archive,
-- t1_technical_file, t1_part_document, t1_certificate_record.
--
-- This script is additive and migration-oriented. Do not drop old tables until
-- mapper/service code has been switched to t1_file_asset/t1_file_relation/t1_file_category.
-- =============================================================================

SET NAMES utf8mb4;
USE `ry-cloud`;

-- -----------------------------------------------------------------------------
-- 文件分类/目录树
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_file_category (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '文件分类ID',
  parent_id char(36) DEFAULT NULL COMMENT '父级分类ID，用于文件目录树',
  category_code varchar(100) NOT NULL COMMENT '分类编码，同一业务范围内建议唯一',
  category_name varchar(200) NOT NULL COMMENT '分类名称',
  category_type varchar(30) NOT NULL DEFAULT 'DOCUMENT' COMMENT '分类类型：DOCUMENT/MODEL/CERTIFICATE/IMAGE/VIDEO/DATASET/ARCHIVE/OTHER',
  scope_type varchar(30) NOT NULL DEFAULT 'GLOBAL' COMMENT '适用范围：GLOBAL/TEMPLATE/DOSSIER_VERSION/DOSSIER_NODE/BUSINESS_OBJECT',
  template_id char(36) DEFAULT NULL COMMENT '适用卷宗模板ID；scope_type=TEMPLATE时使用',
  dossier_instance_id char(36) DEFAULT NULL COMMENT '适用卷宗实例ID',
  dossier_version_id char(36) DEFAULT NULL COMMENT '适用卷宗版本ID；scope_type=DOSSIER_VERSION时使用',
  structure_node_id char(36) DEFAULT NULL COMMENT '适用卷宗目录节点ID；scope_type=DOSSIER_NODE时使用',
  path_code varchar(1000) DEFAULT NULL COMMENT '分类编码路径，便于树形查询和展示',
  path_name varchar(1000) DEFAULT NULL COMMENT '分类名称路径，便于页面展示',
  sort_order int NOT NULL DEFAULT 0 COMMENT '排序号',
  required_flag tinyint(1) NOT NULL DEFAULT 0 COMMENT '该分类下文件是否默认必需',
  is_active tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  attrs_json json NOT NULL DEFAULT (json_object()) COMMENT '分类扩展属性',
  created_by varchar(100) DEFAULT NULL COMMENT '创建人',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_by varchar(100) DEFAULT NULL COMMENT '更新人',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_file_category_parent (parent_id, sort_order),
  KEY idx_file_category_scope (scope_type, template_id, dossier_version_id, structure_node_id),
  KEY idx_file_category_code (category_code),
  KEY idx_file_category_active (is_active, sort_order),
  CONSTRAINT chk_file_category_type CHECK (category_type IN ('DOCUMENT','MODEL','CERTIFICATE','IMAGE','VIDEO','DATASET','ARCHIVE','OTHER')),
  CONSTRAINT chk_file_category_scope CHECK (scope_type IN ('GLOBAL','TEMPLATE','DOSSIER_VERSION','DOSSIER_NODE','BUSINESS_OBJECT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一文件分类/目录树：替代 t1_document_category';

-- -----------------------------------------------------------------------------
-- 文件资产：只描述文件本体和文件级元数据，不描述挂靠关系
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_file_asset (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '文件资产ID',
  file_code varchar(100) NOT NULL COMMENT '文件资产编码，系统内唯一',
  asset_kind varchar(30) NOT NULL DEFAULT 'DOCUMENT' COMMENT '资产类型：DOCUMENT/MODEL/CERTIFICATE/IMAGE/VIDEO/DATASET/ARCHIVE/OTHER',
  business_no varchar(500) DEFAULT NULL COMMENT '业务编号：文档编号、图号、证书编号、模型编号等',
  business_type varchar(100) DEFAULT NULL COMMENT '业务类型：DRAWING/SPEC/TEST_REPORT/MBD_MODEL/COC等',
  business_title varchar(1000) DEFAULT NULL COMMENT '业务标题或文件标题',
  revision varchar(50) DEFAULT NULL COMMENT '文档/模型/证书版本',
  file_status varchar(50) NOT NULL DEFAULT 'active' COMMENT '文件状态：draft/released/active/archived/superseded/deleted/missing等',
  original_file_name varchar(500) DEFAULT NULL COMMENT '上传时原始文件名',
  display_name varchar(500) NOT NULL COMMENT '页面展示文件名',
  file_ext varchar(30) DEFAULT NULL COMMENT '文件扩展名，如 pdf/jt/step/catpart',
  mime_type varchar(200) DEFAULT NULL COMMENT 'MIME类型',
  storage_type varchar(30) NOT NULL DEFAULT 'LOCAL' COMMENT '存储类型：LOCAL/MINIO/OSS/PLM/EXTERNAL/DB',
  storage_bucket varchar(200) DEFAULT NULL COMMENT '对象存储桶或文件库',
  storage_key varchar(1000) DEFAULT NULL COMMENT '文件存储Key，推荐用于MinIO/OSS/文件服务定位',
  storage_path varchar(1000) DEFAULT NULL COMMENT '文件路径，兼容本地路径或旧 file_path',
  access_url varchar(1000) DEFAULT NULL COMMENT '外部访问URL或预签名URL，不建议作为唯一定位',
  preview_storage_key varchar(1000) DEFAULT NULL COMMENT '预览/轻量化文件存储Key，例如PDF预览、JT/glTF轻量化模型',
  file_size bigint DEFAULT NULL COMMENT '文件大小，单位字节',
  hash_algorithm varchar(30) DEFAULT 'SHA-256' COMMENT '哈希算法',
  file_hash varchar(128) DEFAULT NULL COMMENT '文件哈希值，用于完整性校验和去重',
  issue_date date DEFAULT NULL COMMENT '签发/发布日期，证书和归档文件可用',
  effective_date date DEFAULT NULL COMMENT '生效日期',
  expiry_date date DEFAULT NULL COMMENT '失效/到期日期，证书可用',
  issued_by varchar(200) DEFAULT NULL COMMENT '签发单位或发布单位',
  security_level varchar(50) DEFAULT NULL COMMENT '密级/访问等级',
  is_latest tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否当前最新版本',
  source_system varchar(50) DEFAULT NULL COMMENT '来源系统',
  source_table varchar(200) DEFAULT NULL COMMENT '来源表名',
  source_record_id varchar(200) DEFAULT NULL COMMENT '来源记录ID',
  source_record_key varchar(500) DEFAULT NULL COMMENT '来源业务键',
  metadata_json json NOT NULL DEFAULT (json_object()) COMMENT '文件扩展元数据：MBD模型、证书、归档、适用性等差异属性',
  uploaded_by varchar(100) DEFAULT NULL COMMENT '上传人',
  uploaded_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '上传时间',
  created_by varchar(100) DEFAULT NULL COMMENT '创建人',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_by varchar(100) DEFAULT NULL COMMENT '更新人',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_file_asset_code (file_code),
  KEY idx_file_asset_business (business_no, revision),
  KEY idx_file_asset_kind_type (asset_kind, business_type, file_status),
  KEY idx_file_asset_hash (file_hash),
  KEY idx_file_asset_storage_key (storage_key(191)),
  KEY idx_file_asset_source (source_table, source_record_id),
  KEY idx_file_asset_latest (is_latest, file_status),
  CONSTRAINT chk_file_asset_kind CHECK (asset_kind IN ('DOCUMENT','MODEL','CERTIFICATE','IMAGE','VIDEO','DATASET','ARCHIVE','OTHER')),
  CONSTRAINT chk_file_asset_size CHECK (file_size IS NULL OR file_size >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一文件资产表：存真实文件信息，替代 t1_document_master/t1_technical_file/t1_part_document 等文件本体字段';

-- -----------------------------------------------------------------------------
-- 文件关系：把文件挂靠到卷宗节点、飞机、BOM、零件、证书等任意业务对象
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t1_file_relation (
  id char(36) NOT NULL DEFAULT (uuid()) COMMENT '文件关系ID',
  file_id char(36) NOT NULL COMMENT '文件资产ID，关联 t1_file_asset.id',
  category_id char(36) DEFAULT NULL COMMENT '文件分类ID，关联 t1_file_category.id',
  relation_type varchar(50) NOT NULL DEFAULT 'ATTACHMENT' COMMENT '关系类型：PRIMARY/ATTACHMENT/EVIDENCE/CERTIFICATE/DRAWING/MODEL/EXPORT_OUTPUT/SOURCE等',
  target_type varchar(50) NOT NULL COMMENT '挂靠对象类型：DOSSIER_INSTANCE/DOSSIER_VERSION/DOSSIER_NODE/AIRCRAFT/BOM_NODE/PART_MASTER/PART_INSTANCE/CERTIFICATE等',
  target_id varchar(200) NOT NULL COMMENT '挂靠对象ID或业务键；PART_MASTER可用件号，CERTIFICATE可用证书编号',
  target_code varchar(500) DEFAULT NULL COMMENT '挂靠对象业务编码，如飞机号、BOM节点编码、件号、证书编号',
  target_name varchar(1000) DEFAULT NULL COMMENT '挂靠对象展示名称',
  dossier_instance_id char(36) DEFAULT NULL COMMENT '卷宗实例ID，用于卷宗范围查询',
  dossier_version_id char(36) DEFAULT NULL COMMENT '卷宗版本ID，用于版本快照查询',
  structure_node_id char(36) DEFAULT NULL COMMENT '卷宗目录节点ID；文件挂到卷宗节点时必填',
  aircraft_id char(36) DEFAULT NULL COMMENT '飞机ID；文件挂到飞机或飞机下节点时填充',
  bom_node_id char(36) DEFAULT NULL COMMENT 'BOM节点ID；文件挂到BOM节点时填充',
  part_number varchar(200) DEFAULT NULL COMMENT '件号；文件挂到件号主数据或零件相关节点时填充',
  part_instance_id char(36) DEFAULT NULL COMMENT '零件/设备/组件实物实例ID',
  object_level varchar(30) DEFAULT NULL COMMENT '对象层级：aircraft/system/subsystem/equipment/component/part/chapter等',
  object_profile_id char(36) DEFAULT NULL COMMENT '统一产品对象ID',
  lifecycle_stage varchar(50) DEFAULT NULL COMMENT '生命周期阶段：design/manufacturing/inspection/service/maintenance等',
  business_domain varchar(50) DEFAULT NULL COMMENT '业务域：DESIGN/MANUFACTURING/QUALITY/SERVICE/DOSSIER/DATA_SUPPORT等',
  required_flag tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否必需文件',
  included_flag tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否纳入当前卷宗或当前视图',
  is_primary tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否主文件',
  is_current tinyint(1) NOT NULL DEFAULT 1 COMMENT '该挂靠关系是否当前有效',
  completeness_status varchar(24) NOT NULL DEFAULT 'not_checked' COMMENT '完整性状态：not_checked/complete/warning/missing/error',
  relation_status varchar(24) NOT NULL DEFAULT 'active' COMMENT '关系状态：active/archived/deleted/missing',
  sort_order int NOT NULL DEFAULT 0 COMMENT '排序号',
  source_system varchar(50) DEFAULT NULL COMMENT '来源系统',
  source_table varchar(200) DEFAULT NULL COMMENT '来源表名',
  source_record_id varchar(200) DEFAULT NULL COMMENT '来源记录ID',
  source_record_key varchar(500) DEFAULT NULL COMMENT '来源业务键',
  source_trace_json json DEFAULT NULL COMMENT '来源追溯JSON',
  business_meta_json json NOT NULL DEFAULT (json_object()) COMMENT '关系级业务元数据：证书属性、归档属性、适用性、模型定位等',
  created_by varchar(100) DEFAULT NULL COMMENT '创建人',
  created_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  updated_by varchar(100) DEFAULT NULL COMMENT '更新人',
  updated_at datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_file_relation_target_file (file_id, target_type, target_id, relation_type),
  KEY idx_file_relation_file (file_id),
  KEY idx_file_relation_category (category_id),
  KEY idx_file_relation_target (target_type, target_id),
  KEY idx_file_relation_target_code (target_type, target_code),
  KEY idx_file_relation_node (dossier_version_id, structure_node_id, included_flag),
  KEY idx_file_relation_dossier (dossier_instance_id, dossier_version_id),
  KEY idx_file_relation_aircraft_bom (aircraft_id, bom_node_id),
  KEY idx_file_relation_part (part_number, part_instance_id),
  KEY idx_file_relation_profile (object_level, object_profile_id),
  KEY idx_file_relation_source (source_table, source_record_id),
  KEY idx_file_relation_status (relation_status, completeness_status),
  CONSTRAINT chk_file_relation_required CHECK (required_flag IN (0, 1)),
  CONSTRAINT chk_file_relation_included CHECK (included_flag IN (0, 1)),
  CONSTRAINT chk_file_relation_primary CHECK (is_primary IN (0, 1)),
  CONSTRAINT chk_file_relation_current CHECK (is_current IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一文件关系表：把文件挂靠到卷宗节点、飞机、BOM、零件、证书等对象';

-- -----------------------------------------------------------------------------
-- 数据迁移：旧分类 -> t1_file_category
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO t1_file_category (
  id, parent_id, category_code, category_name, category_type, scope_type,
  template_id, sort_order, attrs_json, created_by, updated_by
)
SELECT
  id,
  parent_id,
  CONCAT('DOC-CAT-', id),
  LEFT(name, 200),
  'DOCUMENT',
  CASE WHEN template_id IS NOT NULL THEN 'TEMPLATE' ELSE 'GLOBAL' END,
  template_id,
  sort_order,
  attrs_json,
  'migration',
  'migration'
FROM t1_document_category;

-- -----------------------------------------------------------------------------
-- 数据迁移：t1_document_master -> t1_file_asset
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO t1_file_asset (
  id, file_code, asset_kind, business_no, business_type, business_title, revision,
  file_status, display_name, storage_path, source_table, source_record_id,
  metadata_json, uploaded_by, created_by, updated_by, uploaded_at, created_at, updated_at
)
SELECT
  id,
  CONCAT('DOC-MASTER-', id),
  'DOCUMENT',
  document_number,
  document_type,
  title,
  revision,
  COALESCE(LOWER(doc_status), 'active'),
  LEFT(title, 500),
  primary_file_path,
  't1_document_master',
  id,
  JSON_OBJECT('effectivity', effectivity),
  'migration',
  'migration',
  'migration',
  updated_at,
  updated_at,
  updated_at
FROM t1_document_master;

-- -----------------------------------------------------------------------------
-- 数据迁移：t1_document_entry 中无 t1_document_master 的卷宗附件 -> t1_file_asset
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO t1_file_asset (
  id, file_code, asset_kind, business_no, business_type, business_title,
  file_status, display_name, storage_key, source_system, source_table,
  source_record_id, source_record_key, metadata_json, uploaded_by, created_by,
  updated_by, uploaded_at, created_at, updated_at
)
SELECT
  de.id,
  CONCAT('DOC-ENTRY-', de.id),
  'DOCUMENT',
  de.doc_no,
  'DOSSIER_DOCUMENT',
  LEFT(de.title, 1000),
  de.document_status,
  COALESCE(NULLIF(LEFT(de.title, 500), ''), de.doc_no, CONCAT('document-', de.id)),
  de.file_storage_key,
  de.source_system,
  't1_document_entry',
  de.id,
  de.source_record_key,
  JSON_OBJECT(
    'attrs', de.attrs_json,
    'sourceTrace', de.source_trace_json,
    'originalSourceTable', de.source_table,
    'originalSourceRecordId', de.source_record_id
  ),
  'migration',
  'migration',
  'migration',
  de.created_at,
  de.created_at,
  de.updated_at
FROM t1_document_entry de
WHERE de.document_master_id IS NULL;

-- -----------------------------------------------------------------------------
-- 数据迁移：t1_document_entry -> t1_file_relation
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO t1_file_relation (
  id, file_id, category_id, relation_type, target_type, target_id, target_code,
  dossier_instance_id, dossier_version_id, structure_node_id, aircraft_id,
  bom_node_id, part_instance_id, object_level, object_profile_id, required_flag,
  included_flag, completeness_status, relation_status, source_system, source_table,
  source_record_id, source_record_key, source_trace_json, business_meta_json,
  created_by, updated_by, created_at, updated_at
)
SELECT
  de.id,
  COALESCE(de.document_master_id, de.id),
  de.category_id,
  'DOSSIER_ATTACHMENT',
  CASE
    WHEN de.structure_node_id IS NOT NULL THEN 'DOSSIER_NODE'
    WHEN de.bom_node_id IS NOT NULL THEN 'BOM_NODE'
    WHEN de.part_instance_id IS NOT NULL THEN 'PART_INSTANCE'
    WHEN de.aircraft_id IS NOT NULL THEN 'AIRCRAFT'
    ELSE 'DOSSIER_VERSION'
  END,
  COALESCE(de.structure_node_id, de.bom_node_id, de.part_instance_id, de.aircraft_id, de.dossier_version_id, de.dossier_instance_id),
  de.doc_no,
  de.dossier_instance_id,
  de.dossier_version_id,
  de.structure_node_id,
  de.aircraft_id,
  de.bom_node_id,
  de.part_instance_id,
  de.object_level,
  de.object_profile_id,
  de.required_flag,
  de.included_flag,
  de.completeness_status,
  de.document_status,
  de.source_system,
  't1_document_entry',
  de.id,
  de.source_record_key,
  de.source_trace_json,
  JSON_OBJECT(
    'docNo', de.doc_no,
    'title', de.title,
    'attrs', de.attrs_json,
    'originalSourceTable', de.source_table,
    'originalSourceRecordId', de.source_record_id,
    'systemRecordId', de.system_record_id
  ),
  'migration',
  'migration',
  de.created_at,
  de.updated_at
FROM t1_document_entry de;

-- -----------------------------------------------------------------------------
-- 数据迁移：t1_document_archive -> t1_file_asset/t1_file_relation
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO t1_file_asset (
  id, file_code, asset_kind, business_no, business_type, business_title,
  file_status, display_name, storage_path, file_hash, issue_date, expiry_date,
  issued_by, source_table, source_record_id, metadata_json, uploaded_by, created_by,
  updated_by, uploaded_at, created_at, updated_at
)
SELECT
  id,
  CONCAT('DOC-ARCHIVE-', id),
  'ARCHIVE',
  doc_number,
  doc_type,
  COALESCE(doc_number, CONCAT(doc_type, '-', id)),
  'archived',
  COALESCE(doc_number, file_path, CONCAT('archive-', id)),
  file_path,
  file_hash,
  issue_date,
  expiry_date,
  issued_by,
  't1_document_archive',
  id,
  JSON_OBJECT('refType', ref_type, 'refId', ref_id),
  'migration',
  'migration',
  'migration',
  created_at,
  created_at,
  created_at
FROM t1_document_archive;

INSERT IGNORE INTO t1_file_relation (
  id, file_id, relation_type, target_type, target_id, target_code, relation_status,
  source_table, source_record_id, business_meta_json, created_by, updated_by, created_at, updated_at
)
SELECT
  id,
  id,
  'ARCHIVE',
  UPPER(ref_type),
  ref_id,
  doc_number,
  'archived',
  't1_document_archive',
  id,
  JSON_OBJECT('docType', doc_type, 'issueDate', issue_date, 'issuedBy', issued_by, 'expiryDate', expiry_date),
  'migration',
  'migration',
  created_at,
  created_at
FROM t1_document_archive;

-- -----------------------------------------------------------------------------
-- 数据迁移：t1_technical_file -> t1_file_asset/t1_file_relation
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO t1_file_asset (
  id, file_code, asset_kind, business_type, business_title, revision, file_status,
  original_file_name, display_name, file_ext, storage_path, file_size, source_table,
  source_record_id, metadata_json, uploaded_by, created_by, updated_by, uploaded_at,
  created_at, updated_at
)
SELECT
  id,
  CONCAT('TECH-FILE-', id),
  CASE WHEN file_type IN ('MBD','MBD_MODEL','MODEL','JT','STEP','CATPART','CATPRODUCT') THEN 'MODEL' ELSE 'DOCUMENT' END,
  file_type,
  file_name,
  version,
  CASE WHEN is_active = 1 THEN 'active' ELSE 'archived' END,
  file_name,
  file_name,
  CASE WHEN file_name LIKE '%.%' THEN LOWER(SUBSTRING_INDEX(file_name, '.', -1)) ELSE NULL END,
  file_path,
  file_size,
  't1_technical_file',
  id,
  JSON_OBJECT('referenceType', reference_type, 'referenceId', reference_id, 'remarks', remarks),
  uploaded_by_id,
  'migration',
  'migration',
  uploaded_at,
  uploaded_at,
  uploaded_at
FROM t1_technical_file;

INSERT IGNORE INTO t1_file_relation (
  id, file_id, relation_type, target_type, target_id, business_domain, relation_status,
  source_table, source_record_id, business_meta_json, created_by, updated_by, created_at, updated_at
)
SELECT
  id,
  id,
  'TECHNICAL_FILE',
  UPPER(reference_type),
  reference_id,
  'DESIGN',
  CASE WHEN is_active = 1 THEN 'active' ELSE 'archived' END,
  't1_technical_file',
  id,
  JSON_OBJECT('fileType', file_type, 'version', version, 'remarks', remarks),
  'migration',
  'migration',
  uploaded_at,
  uploaded_at
FROM t1_technical_file;

-- -----------------------------------------------------------------------------
-- 数据迁移：t1_part_document -> t1_file_asset/t1_file_relation
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO t1_file_asset (
  id, file_code, asset_kind, business_no, business_type, business_title, revision,
  file_status, display_name, storage_path, effective_date, source_table,
  source_record_id, source_record_key, metadata_json, uploaded_by, created_by,
  updated_by, uploaded_at, created_at, updated_at
)
SELECT
  id,
  CONCAT('PART-DOC-', id),
  'DOCUMENT',
  doc_number,
  doc_type,
  doc_title,
  doc_revision,
  CASE WHEN is_current = 1 THEN 'active' ELSE 'superseded' END,
  COALESCE(doc_title, doc_number, CONCAT('part-document-', id)),
  file_path,
  effective_date,
  't1_part_document',
  id,
  part_number,
  JSON_OBJECT('partNumber', part_number, 'isCurrent', is_current),
  'migration',
  'migration',
  'migration',
  created_at,
  created_at,
  created_at
FROM t1_part_document;

INSERT IGNORE INTO t1_file_relation (
  id, file_id, relation_type, target_type, target_id, target_code, part_number,
  business_domain, lifecycle_stage, is_current, source_table, source_record_id,
  source_record_key, business_meta_json, created_by, updated_by, created_at, updated_at
)
SELECT
  id,
  id,
  'PART_DOCUMENT',
  'PART_MASTER',
  part_number,
  part_number,
  part_number,
  'DESIGN',
  'design',
  is_current,
  't1_part_document',
  id,
  part_number,
  JSON_OBJECT('docType', doc_type, 'docNumber', doc_number, 'docRevision', doc_revision, 'effectiveDate', effective_date),
  'migration',
  'migration',
  created_at,
  created_at
FROM t1_part_document;

-- -----------------------------------------------------------------------------
-- 数据迁移：t1_certificate_record -> t1_file_asset/t1_file_relation
-- Note: certificate business fields are kept in t1_file_asset standard fields and
-- t1_file_relation.business_meta_json, so a separate certificate file table is not
-- required for the simplified three-table model.
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO t1_file_asset (
  id, file_code, asset_kind, business_no, business_type, business_title,
  file_status, display_name, storage_key, issue_date, expiry_date, issued_by,
  source_table, source_record_id, metadata_json, uploaded_by, created_by,
  updated_by, uploaded_at, created_at, updated_at
)
SELECT
  id,
  CONCAT('CERT-', id),
  'CERTIFICATE',
  certificate_no,
  certificate_type,
  certificate_no,
  COALESCE(certificate_status, 'active'),
  certificate_no,
  file_storage_key,
  issue_date,
  expiry_date,
  issuing_authority,
  't1_certificate_record',
  id,
  JSON_OBJECT(
    'objectLevel', object_level,
    'objectId', object_id,
    'aircraftId', aircraft_id,
    'bomNodeId', bom_node_id,
    'partInstanceId', part_instance_id,
    'documentEntryId', document_entry_id,
    'attrs', attrs_json
  ),
  'migration',
  'migration',
  'migration',
  created_at,
  created_at,
  updated_at
FROM t1_certificate_record;

INSERT IGNORE INTO t1_file_relation (
  id, file_id, relation_type, target_type, target_id, target_code, aircraft_id,
  bom_node_id, part_instance_id, object_level, is_current, relation_status,
  source_table, source_record_id, business_meta_json, created_by, updated_by,
  created_at, updated_at
)
SELECT
  id,
  id,
  'CERTIFICATE',
  CASE
    WHEN part_instance_id IS NOT NULL THEN 'PART_INSTANCE'
    WHEN bom_node_id IS NOT NULL THEN 'BOM_NODE'
    WHEN aircraft_id IS NOT NULL THEN 'AIRCRAFT'
    WHEN object_level IS NOT NULL THEN UPPER(object_level)
    ELSE 'CERTIFICATE'
  END,
  COALESCE(part_instance_id, bom_node_id, aircraft_id, object_id, certificate_no, id),
  certificate_no,
  aircraft_id,
  bom_node_id,
  part_instance_id,
  object_level,
  1,
  COALESCE(certificate_status, 'active'),
  't1_certificate_record',
  id,
  JSON_OBJECT(
    'certificateNo', certificate_no,
    'certificateType', certificate_type,
    'issuingAuthority', issuing_authority,
    'issueDate', issue_date,
    'expiryDate', expiry_date,
    'documentEntryId', document_entry_id,
    'attrs', attrs_json
  ),
  'migration',
  'migration',
  created_at,
  updated_at
FROM t1_certificate_record;

-- -----------------------------------------------------------------------------
-- Verification helpers
-- -----------------------------------------------------------------------------
SELECT 't1_file_category' AS table_name, COUNT(1) AS row_count FROM t1_file_category
UNION ALL
SELECT 't1_file_asset', COUNT(1) FROM t1_file_asset
UNION ALL
SELECT 't1_file_relation', COUNT(1) FROM t1_file_relation;

-- -----------------------------------------------------------------------------
-- 模板配置切换：不再让模板数据源和规则指向旧文件表
-- -----------------------------------------------------------------------------
UPDATE t1_dossier_template_data_source
SET source_table = CASE
      WHEN source_table = 't1_document_category' THEN 't1_file_category'
      ELSE 't1_file_relation'
    END,
    source_record_type = CASE
      WHEN source_record_type = 't1_document_category' THEN 't1_file_category'
      WHEN source_record_type IN ('t1_document_entry', 't1_part_document', 'document') THEN 't1_file_relation'
      ELSE source_record_type
    END,
    updated_by = 'migration',
    updated_at = CURRENT_TIMESTAMP(6)
WHERE source_table IN (
  't1_document_category', 't1_document_master', 't1_document_entry', 't1_document_archive',
  't1_technical_file', 't1_part_document', 't1_certificate_record'
);

UPDATE t1_dossier_template_rule
SET target_table = CASE
      WHEN target_table = 't1_document_category' THEN 't1_file_category'
      ELSE 't1_file_relation'
    END,
    target_field = CASE
      WHEN target_table = 't1_document_entry' AND target_field = 'file_storage_key' THEN 'file_id'
      WHEN target_table = 't1_document_entry' AND target_field = 'doc_no' THEN 'target_code'
      WHEN target_table = 't1_part_document' AND target_field = 'doc_number' THEN 'target_code'
      WHEN target_table = 't1_part_document' AND target_field = 'doc_type' THEN 'relation_type'
      WHEN target_table = 't1_document_category' AND target_field = 'category_name' THEN 'category_name'
      ELSE target_field
    END,
    rule_expression = CASE
      WHEN rule_expression = 'file_storage_key is not null' THEN 'file_id is not null'
      ELSE rule_expression
    END,
    updated_by = 'migration',
    updated_at = CURRENT_TIMESTAMP(6)
WHERE target_table IN (
  't1_document_category', 't1_document_master', 't1_document_entry', 't1_document_archive',
  't1_technical_file', 't1_part_document', 't1_certificate_record'
);

-- -----------------------------------------------------------------------------
-- 旧表退出运行路径：改名为 legacy 备份表。确认服务已切到新表后，可另行删除这些 legacy 表。
-- -----------------------------------------------------------------------------
RENAME TABLE
  t1_document_category TO document_category_legacy_20260611,
  t1_document_master TO document_master_legacy_20260611,
  t1_document_entry TO document_entry_legacy_20260611,
  t1_document_archive TO document_archive_legacy_20260611,
  t1_technical_file TO technical_file_legacy_20260611,
  t1_part_document TO part_document_legacy_20260611,
  t1_certificate_record TO certificate_record_legacy_20260611;

SELECT 'document_category_legacy_20260611' AS legacy_table, COUNT(1) AS row_count FROM document_category_legacy_20260611
UNION ALL
SELECT 'document_master_legacy_20260611', COUNT(1) FROM document_master_legacy_20260611
UNION ALL
SELECT 'document_entry_legacy_20260611', COUNT(1) FROM document_entry_legacy_20260611
UNION ALL
SELECT 'document_archive_legacy_20260611', COUNT(1) FROM document_archive_legacy_20260611
UNION ALL
SELECT 'technical_file_legacy_20260611', COUNT(1) FROM technical_file_legacy_20260611
UNION ALL
SELECT 'part_document_legacy_20260611', COUNT(1) FROM part_document_legacy_20260611
UNION ALL
SELECT 'certificate_record_legacy_20260611', COUNT(1) FROM certificate_record_legacy_20260611;
