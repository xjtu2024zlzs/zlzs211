-- ============================================================
-- Project 4: 航空装备故障诊断与根因分析模块数据库脚本
-- File: project4_data.sql
-- Description: 课题四增量数据库脚本，仅包含课题四业务表结构与演示数据。
-- Database: ry-cloud
-- Notes:
--   1. 本文件不包含 RuoYi 主系统基础表，例如 sys_user、sys_role、sys_menu 等。
--   2. 若课题四菜单由主课题统一维护，则无需额外导入菜单 SQL。
--   3. 如需接入真实后端持久化，可在对应实体/Mapper 中使用下列表结构。
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 清理课题四旧表
-- ============================================================

DROP TABLE IF EXISTS `fd_root_cause_analysis`;
DROP TABLE IF EXISTS `fd_diagnosis_result`;
DROP TABLE IF EXISTS `fd_fusion_result`;
DROP TABLE IF EXISTS `fd_augment_result`;
DROP TABLE IF EXISTS `fd_processed_sample`;
DROP TABLE IF EXISTS `fd_data_file`;
DROP TABLE IF EXISTS `fd_analysis_pipeline`;
DROP TABLE IF EXISTS `fd_dataset_info`;

-- ============================================================
-- 2. 数据集信息表
-- ============================================================

CREATE TABLE `fd_dataset_info` (
  `dataset_id` bigint NOT NULL AUTO_INCREMENT COMMENT '数据集ID',
  `dataset_code` varchar(64) NOT NULL COMMENT '数据集编号',
  `dataset_name` varchar(128) NOT NULL COMMENT '数据集名称',
  `dataset_source` varchar(128) DEFAULT NULL COMMENT '数据来源',
  `dataset_desc` varchar(500) DEFAULT NULL COMMENT '数据集说明',
  `sample_count` int DEFAULT 0 COMMENT '样本数量',
  `status` varchar(32) DEFAULT '有效' COMMENT '状态',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dataset_id`),
  UNIQUE KEY `uk_fd_dataset_code` (`dataset_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课题四数据集信息表';

-- ============================================================
-- 3. 全流程流水线表
-- ============================================================

CREATE TABLE `fd_analysis_pipeline` (
  `pipeline_id` bigint NOT NULL AUTO_INCREMENT COMMENT '流程ID',
  `pipeline_code` varchar(64) NOT NULL COMMENT '流程编号',
  `dataset_id` bigint DEFAULT NULL COMMENT '数据集ID',
  `dataset_name` varchar(128) DEFAULT NULL COMMENT '数据集名称',
  `current_stage` varchar(64) DEFAULT NULL COMMENT '当前阶段：DATA_IMPORTED/PREPROCESSED/AUGMENTED/FUSED/DIAGNOSED/ROOT_CAUSE_DONE',
  `status` varchar(32) DEFAULT '运行中' COMMENT '流程状态',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`pipeline_id`),
  UNIQUE KEY `uk_fd_pipeline_code` (`pipeline_code`),
  KEY `idx_fd_pipeline_dataset_id` (`dataset_id`),
  KEY `idx_fd_pipeline_stage` (`current_stage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课题四分析流程流水线表';

-- ============================================================
-- 4. 原始数据文件表
-- ============================================================

CREATE TABLE `fd_data_file` (
  `file_id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `dataset_id` bigint DEFAULT NULL COMMENT '数据集ID',
  `pipeline_id` bigint DEFAULT NULL COMMENT '流程ID',
  `file_code` varchar(64) NOT NULL COMMENT '文件编号',
  `original_file_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_suffix` varchar(32) DEFAULT NULL COMMENT '文件后缀',
  `file_type` varchar(64) DEFAULT NULL COMMENT '文件类型',
  `file_size` bigint DEFAULT 0 COMMENT '文件大小',
  `storage_path` varchar(500) DEFAULT NULL COMMENT '存储路径',
  `file_md5` varchar(128) DEFAULT NULL COMMENT '文件MD5',
  `data_source` varchar(128) DEFAULT NULL COMMENT '数据来源',
  `import_status` varchar(32) DEFAULT '成功' COMMENT '导入状态',
  `parse_status` varchar(32) DEFAULT '成功' COMMENT '解析状态',
  `sample_count` int DEFAULT 0 COMMENT '样本数量',
  `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`file_id`),
  UNIQUE KEY `uk_fd_data_file_code` (`file_code`),
  KEY `idx_fd_data_file_dataset_id` (`dataset_id`),
  KEY `idx_fd_data_file_pipeline_id` (`pipeline_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课题四原始数据文件表';

-- ============================================================
-- 5. 预处理样本表
-- ============================================================

CREATE TABLE `fd_processed_sample` (
  `sample_id` bigint NOT NULL AUTO_INCREMENT COMMENT '样本ID',
  `pipeline_id` bigint DEFAULT NULL COMMENT '流程ID',
  `dataset_id` bigint DEFAULT NULL COMMENT '数据集ID',
  `file_id` bigint DEFAULT NULL COMMENT '来源文件ID',
  `sample_code` varchar(64) NOT NULL COMMENT '样本编号',
  `source_file_name` varchar(255) DEFAULT NULL COMMENT '来源文件',
  `time_window_length` int DEFAULT NULL COMMENT '时间窗长度',
  `stride` int DEFAULT NULL COMMENT '步长',
  `overlap_rate` varchar(32) DEFAULT NULL COMMENT '重叠率',
  `denoise_method` varchar(64) DEFAULT '未启用' COMMENT '去噪方法',
  `normalize_method` varchar(64) DEFAULT '未启用' COMMENT '归一化方法',
  `sample_dimension` varchar(64) DEFAULT NULL COMMENT '样本维度',
  `sample_path` varchar(500) DEFAULT NULL COMMENT '样本数据路径',
  `process_status` varchar(32) DEFAULT '已生成' COMMENT '处理状态',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`sample_id`),
  UNIQUE KEY `uk_fd_processed_sample_code` (`sample_code`),
  KEY `idx_fd_processed_pipeline_id` (`pipeline_id`),
  KEY `idx_fd_processed_dataset_id` (`dataset_id`),
  KEY `idx_fd_processed_file_id` (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课题四预处理样本表';

-- ============================================================
-- 6. 样本增强结果表
-- ============================================================

CREATE TABLE `fd_augment_result` (
  `augment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '增强ID',
  `augment_code` varchar(64) NOT NULL COMMENT '增强编号',
  `pipeline_id` bigint DEFAULT NULL COMMENT '流程ID',
  `dataset_id` bigint DEFAULT NULL COMMENT '数据集ID',
  `sample_id` bigint DEFAULT NULL COMMENT '原始样本ID',
  `sample_code` varchar(64) DEFAULT NULL COMMENT '原始样本编号',
  `source_file_name` varchar(255) DEFAULT NULL COMMENT '来源文件',
  `augment_method` varchar(64) DEFAULT NULL COMMENT '增强算法',
  `augment_ratio` int DEFAULT NULL COMMENT '增强倍数',
  `generated_count` int DEFAULT NULL COMMENT '生成数量',
  `quality_strategy` varchar(255) DEFAULT NULL COMMENT '质量策略',
  `validity` varchar(32) DEFAULT '有效' COMMENT '有效性',
  `param_json` longtext COMMENT '参数JSON',
  `result_summary` varchar(500) DEFAULT NULL COMMENT '结果摘要',
  `output_path` varchar(500) DEFAULT NULL COMMENT '输出路径',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`augment_id`),
  UNIQUE KEY `uk_fd_augment_code` (`augment_code`),
  KEY `idx_fd_augment_pipeline_id` (`pipeline_id`),
  KEY `idx_fd_augment_dataset_id` (`dataset_id`),
  KEY `idx_fd_augment_sample_id` (`sample_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课题四样本增强结果表';

-- ============================================================
-- 7. 特征融合结果表
-- ============================================================

CREATE TABLE `fd_fusion_result` (
  `fusion_id` bigint NOT NULL AUTO_INCREMENT COMMENT '融合ID',
  `fusion_code` varchar(64) NOT NULL COMMENT '融合编号',
  `pipeline_id` bigint DEFAULT NULL COMMENT '流程ID',
  `dataset_id` bigint DEFAULT NULL COMMENT '数据集ID',
  `sample_id` bigint DEFAULT NULL COMMENT '样本ID',
  `sample_code` varchar(64) DEFAULT NULL COMMENT '样本编号',
  `augment_id` bigint DEFAULT NULL COMMENT '增强ID',
  `feature_components` varchar(500) DEFAULT NULL COMMENT '关联特征组成',
  `fusion_method` varchar(64) DEFAULT NULL COMMENT '融合方法',
  `confidence_weight` decimal(10,4) DEFAULT NULL COMMENT '置信权重',
  `output_dimension` int DEFAULT NULL COMMENT '输出维度',
  `vector_length` int DEFAULT NULL COMMENT '向量长度',
  `fusion_vector_json` longtext COMMENT '融合向量JSON',
  `vector_path` varchar(500) DEFAULT NULL COMMENT '向量文件路径',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`fusion_id`),
  UNIQUE KEY `uk_fd_fusion_code` (`fusion_code`),
  KEY `idx_fd_fusion_pipeline_id` (`pipeline_id`),
  KEY `idx_fd_fusion_dataset_id` (`dataset_id`),
  KEY `idx_fd_fusion_sample_id` (`sample_id`),
  KEY `idx_fd_fusion_augment_id` (`augment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课题四特征融合结果表';

-- ============================================================
-- 8. 故障诊断结果表
-- ============================================================

CREATE TABLE `fd_diagnosis_result` (
  `diagnosis_id` bigint NOT NULL AUTO_INCREMENT COMMENT '诊断ID',
  `diagnosis_code` varchar(64) NOT NULL COMMENT '诊断编号',
  `pipeline_id` bigint DEFAULT NULL COMMENT '流程ID',
  `dataset_id` bigint DEFAULT NULL COMMENT '数据集ID',
  `sample_id` bigint DEFAULT NULL COMMENT '样本ID',
  `sample_code` varchar(64) DEFAULT NULL COMMENT '样本编号',
  `fusion_id` bigint DEFAULT NULL COMMENT '融合ID',
  `model_id` bigint DEFAULT NULL COMMENT '模型ID',
  `model_name` varchar(128) DEFAULT NULL COMMENT '模型名称',
  `fault_type` varchar(64) DEFAULT NULL COMMENT '故障类型',
  `fault_location` varchar(64) DEFAULT NULL COMMENT '故障位置',
  `confidence` decimal(10,4) DEFAULT NULL COMMENT '诊断结果置信度',
  `threshold` decimal(10,4) DEFAULT NULL COMMENT '阈值',
  `health_score` decimal(10,2) DEFAULT NULL COMMENT '健康评分',
  `alarm_level` varchar(32) DEFAULT NULL COMMENT '告警等级',
  `diagnosis_time` datetime DEFAULT NULL COMMENT '诊断时间',
  `root_status` varchar(32) DEFAULT '待根因分析' COMMENT '根因状态',
  `diagnosis_result_json` longtext COMMENT '诊断结果JSON',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`diagnosis_id`),
  UNIQUE KEY `uk_fd_diagnosis_code` (`diagnosis_code`),
  KEY `idx_fd_diagnosis_pipeline_id` (`pipeline_id`),
  KEY `idx_fd_diagnosis_dataset_id` (`dataset_id`),
  KEY `idx_fd_diagnosis_sample_id` (`sample_id`),
  KEY `idx_fd_diagnosis_fusion_id` (`fusion_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课题四故障诊断结果表';

-- ============================================================
-- 9. 根因分析结果表
-- ============================================================

CREATE TABLE `fd_root_cause_analysis` (
  `analysis_id` bigint NOT NULL AUTO_INCREMENT COMMENT '根因分析ID',
  `analysis_code` varchar(64) NOT NULL COMMENT '根因分析编号',
  `pipeline_id` bigint DEFAULT NULL COMMENT '流程ID',
  `dataset_id` bigint DEFAULT NULL COMMENT '数据集ID',
  `sample_id` bigint DEFAULT NULL COMMENT '样本ID',
  `sample_code` varchar(64) DEFAULT NULL COMMENT '样本编号',
  `diagnosis_id` bigint DEFAULT NULL COMMENT '诊断ID',
  `fault_type` varchar(64) DEFAULT NULL COMMENT '故障类型',
  `fault_location` varchar(64) DEFAULT NULL COMMENT '故障位置',
  `specific_root_cause` varchar(128) DEFAULT NULL COMMENT '具体根因',
  `root_cause_confidence` decimal(10,4) DEFAULT NULL COMMENT '根因置信度',
  `evidence_summary` varchar(500) DEFAULT NULL COMMENT '证据链摘要',
  `evidence_json` longtext COMMENT '证据链JSON',
  `maintenance_suggestion` varchar(1000) DEFAULT NULL COMMENT '整改建议',
  `analysis_status` varchar(32) DEFAULT '已分析' COMMENT '分析状态',
  `analyst` varchar(64) DEFAULT NULL COMMENT '分析器/分析人',
  `analysis_time` datetime DEFAULT NULL COMMENT '分析时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`analysis_id`),
  UNIQUE KEY `uk_fd_root_cause_code` (`analysis_code`),
  KEY `idx_fd_root_pipeline_id` (`pipeline_id`),
  KEY `idx_fd_root_dataset_id` (`dataset_id`),
  KEY `idx_fd_root_sample_id` (`sample_id`),
  KEY `idx_fd_root_diagnosis_id` (`diagnosis_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课题四根因分析结果表';

-- ============================================================
-- 10. 课题四演示数据
-- ============================================================

INSERT INTO `fd_dataset_info` (`dataset_id`, `dataset_code`, `dataset_name`, `dataset_source`, `dataset_desc`, `sample_count`, `status`, `create_by`, `create_time`, `remark`) VALUES
(1, 'DATASET-CWRU-001', 'CWRU轴承故障数据集', 'CWRU Bearing Data Center', '用于航空装备质量追溯场景演示的轴承故障数据集，包含正常、内圈故障、滚动体故障和外圈故障。', 12, '有效', 'admin', NOW(), '课题四演示数据');

INSERT INTO `fd_analysis_pipeline` (`pipeline_id`, `pipeline_code`, `dataset_id`, `dataset_name`, `current_stage`, `status`, `start_time`, `end_time`, `create_by`, `create_time`, `remark`) VALUES
(1, 'P-1780654370908', 1, 'CWRU轴承故障数据集', 'ROOT_CAUSE_DONE', '已完成', '2026-06-05 17:19:42', '2026-06-05 17:32:10', 'admin', NOW(), '课题四前端流程打通演示流水线');

INSERT INTO `fd_data_file` (`file_id`, `dataset_id`, `pipeline_id`, `file_code`, `original_file_name`, `file_suffix`, `file_type`, `file_size`, `storage_path`, `data_source`, `import_status`, `parse_status`, `sample_count`, `create_by`, `create_time`, `remark`) VALUES
(1, 1, 1, 'FILE-CWRU-001', 'normal_0hp.mat', 'mat', 'mat', 0, '/data/cwru/normal_0hp.mat', 'CWRU', '成功', '成功', 2, 'admin', NOW(), '正常样本文件'),
(2, 1, 1, 'FILE-CWRU-002', 'inner_race_0hp_007.mat', 'mat', 'mat', 0, '/data/cwru/inner_race_0hp_007.mat', 'CWRU', '成功', '成功', 2, 'admin', NOW(), '内圈故障样本文件'),
(3, 1, 1, 'FILE-CWRU-003', 'ball_0hp_007.mat', 'mat', 'mat', 0, '/data/cwru/ball_0hp_007.mat', 'CWRU', '成功', '成功', 2, 'admin', NOW(), '滚动体故障样本文件'),
(4, 1, 1, 'FILE-CWRU-004', 'outer_race_0hp_007.mat', 'mat', 'mat', 0, '/data/cwru/outer_race_0hp_007.mat', 'CWRU', '成功', '成功', 2, 'admin', NOW(), '外圈故障样本文件'),
(5, 1, 1, 'FILE-CWRU-005', 'inner_race_1hp_014.mat', 'mat', 'mat', 0, '/data/cwru/inner_race_1hp_014.mat', 'CWRU', '成功', '成功', 2, 'admin', NOW(), '内圈故障1HP样本文件'),
(6, 1, 1, 'FILE-CWRU-006', 'outer_race_2hp_021.mat', 'mat', 'mat', 0, '/data/cwru/outer_race_2hp_021.mat', 'CWRU', '成功', '成功', 2, 'admin', NOW(), '外圈故障2HP样本文件');

INSERT INTO `fd_processed_sample` (`sample_id`, `pipeline_id`, `dataset_id`, `file_id`, `sample_code`, `source_file_name`, `time_window_length`, `stride`, `overlap_rate`, `denoise_method`, `normalize_method`, `sample_dimension`, `sample_path`, `process_status`, `create_by`, `create_time`) VALUES
(1, 1, 1, 1, 'SAMPLE-001', 'normal_0hp.mat', 1024, 512, '50%', '未启用', '未启用', '1024×1', '/data/processed/SAMPLE-001.npy', '已生成', 'admin', '2026-06-05 17:19:42'),
(2, 1, 1, 1, 'SAMPLE-002', 'normal_0hp.mat', 1024, 512, '50%', '未启用', '未启用', '1024×1', '/data/processed/SAMPLE-002.npy', '已生成', 'admin', '2026-06-05 17:19:42'),
(11, 1, 1, 2, 'SAMPLE-011', 'inner_race_0hp_007.mat', 1024, 512, '50%', '未启用', '未启用', '1024×1', '/data/processed/SAMPLE-011.npy', '已生成', 'admin', '2026-06-05 17:19:42'),
(12, 1, 1, 2, 'SAMPLE-012', 'inner_race_0hp_007.mat', 1024, 512, '50%', '未启用', '未启用', '1024×1', '/data/processed/SAMPLE-012.npy', '已生成', 'admin', '2026-06-05 17:19:42'),
(21, 1, 1, 3, 'SAMPLE-021', 'ball_0hp_007.mat', 1024, 512, '50%', '未启用', '未启用', '1024×1', '/data/processed/SAMPLE-021.npy', '已生成', 'admin', '2026-06-05 17:19:42'),
(22, 1, 1, 3, 'SAMPLE-022', 'ball_0hp_007.mat', 1024, 512, '50%', '未启用', '未启用', '1024×1', '/data/processed/SAMPLE-022.npy', '已生成', 'admin', '2026-06-05 17:19:42'),
(31, 1, 1, 4, 'SAMPLE-031', 'outer_race_0hp_007.mat', 1024, 512, '50%', '未启用', '未启用', '1024×1', '/data/processed/SAMPLE-031.npy', '已生成', 'admin', '2026-06-05 17:19:42'),
(32, 1, 1, 4, 'SAMPLE-032', 'outer_race_0hp_007.mat', 1024, 512, '50%', '未启用', '未启用', '1024×1', '/data/processed/SAMPLE-032.npy', '已生成', 'admin', '2026-06-05 17:19:42'),
(41, 1, 1, 5, 'SAMPLE-041', 'inner_race_1hp_014.mat', 1024, 512, '50%', '未启用', '未启用', '1024×1', '/data/processed/SAMPLE-041.npy', '已生成', 'admin', '2026-06-05 17:19:42'),
(42, 1, 1, 5, 'SAMPLE-042', 'inner_race_1hp_014.mat', 1024, 512, '50%', '未启用', '未启用', '1024×1', '/data/processed/SAMPLE-042.npy', '已生成', 'admin', '2026-06-05 17:19:42'),
(51, 1, 1, 6, 'SAMPLE-051', 'outer_race_2hp_021.mat', 1024, 512, '50%', '未启用', '未启用', '1024×1', '/data/processed/SAMPLE-051.npy', '已生成', 'admin', '2026-06-05 17:19:42'),
(52, 1, 1, 6, 'SAMPLE-052', 'outer_race_2hp_021.mat', 1024, 512, '50%', '未启用', '未启用', '1024×1', '/data/processed/SAMPLE-052.npy', '已生成', 'admin', '2026-06-05 17:19:42');

INSERT INTO `fd_augment_result` (`augment_id`, `augment_code`, `pipeline_id`, `dataset_id`, `sample_id`, `sample_code`, `source_file_name`, `augment_method`, `augment_ratio`, `generated_count`, `quality_strategy`, `validity`, `param_json`, `result_summary`, `output_path`, `create_by`, `create_time`, `remark`) VALUES
(1, 'AUG-001', 1, 1, 1, 'SAMPLE-001', 'normal_0hp.mat', 'SMOTE', 3, 6, '类别均衡与质量一致性控制', '有效', '{"method":"SMOTE","ratio":3,"sourceSample":"SAMPLE-001"}', '基于SAMPLE-001生成6条增强样本，频谱形态保持一致。', '/data/augment/AUG-001', 'admin', NOW(), '样本增强演示数据'),
(2, 'AUG-002', 1, 1, 11, 'SAMPLE-011', 'inner_race_0hp_007.mat', 'SMOTE', 3, 6, '类别均衡与质量一致性控制', '有效', '{"method":"SMOTE","ratio":3,"sourceSample":"SAMPLE-011"}', '基于SAMPLE-011生成6条增强样本，内圈故障特征保持清晰。', '/data/augment/AUG-002', 'admin', NOW(), '样本增强演示数据'),
(3, 'AUG-003', 1, 1, 21, 'SAMPLE-021', 'ball_0hp_007.mat', 'SMOTE', 3, 6, '类别均衡与质量一致性控制', '有效', '{"method":"SMOTE","ratio":3,"sourceSample":"SAMPLE-021"}', '基于SAMPLE-021生成6条增强样本，滚动体冲击特征保持清晰。', '/data/augment/AUG-003', 'admin', NOW(), '样本增强演示数据'),
(4, 'AUG-004', 1, 1, 31, 'SAMPLE-031', 'outer_race_0hp_007.mat', 'SMOTE', 3, 6, '类别均衡与质量一致性控制', '有效', '{"method":"SMOTE","ratio":3,"sourceSample":"SAMPLE-031"}', '基于SAMPLE-031生成6条增强样本，外圈故障频率成分保持稳定。', '/data/augment/AUG-004', 'admin', NOW(), '样本增强演示数据'),
(5, 'AUG-005', 1, 1, 41, 'SAMPLE-041', 'inner_race_1hp_014.mat', 'SMOTE', 3, 6, '类别均衡与质量一致性控制', '有效', '{"method":"SMOTE","ratio":3,"sourceSample":"SAMPLE-041"}', '基于SAMPLE-041生成6条增强样本，高载荷内圈故障特征保持清晰。', '/data/augment/AUG-005', 'admin', NOW(), '样本增强演示数据'),
(6, 'AUG-006', 1, 1, 51, 'SAMPLE-051', 'outer_race_2hp_021.mat', 'SMOTE', 3, 6, '类别均衡与质量一致性控制', '有效', '{"method":"SMOTE","ratio":3,"sourceSample":"SAMPLE-051"}', '基于SAMPLE-051生成6条增强样本，高载荷外圈故障特征保持清晰。', '/data/augment/AUG-006', 'admin', NOW(), '样本增强演示数据');

INSERT INTO `fd_fusion_result` (`fusion_id`, `fusion_code`, `pipeline_id`, `dataset_id`, `sample_id`, `sample_code`, `augment_id`, `feature_components`, `fusion_method`, `confidence_weight`, `output_dimension`, `vector_length`, `fusion_vector_json`, `vector_path`, `create_by`, `create_time`, `remark`) VALUES
(1, 'FUS-001', 1, 1, 1, 'SAMPLE-001', 1, '多传感器时序窗口特征,传感器空间关系特征,时间演化特征,故障类别条件特征', 'PCA+Attention', 0.8500, 128, 128, '{"featureComponents":["多传感器时序窗口特征","传感器空间关系特征","时间演化特征","故障类别条件特征"],"vectorDim":128}', '/data/fusion/FUS-001.json', 'admin', NOW(), '特征融合演示数据'),
(2, 'FUS-002', 1, 1, 11, 'SAMPLE-011', 2, '多传感器时序窗口特征,传感器空间关系特征,时间演化特征,故障类别条件特征', 'PCA+Attention', 0.8500, 128, 128, '{"featureComponents":["多传感器时序窗口特征","传感器空间关系特征","时间演化特征","故障类别条件特征"],"vectorDim":128}', '/data/fusion/FUS-002.json', 'admin', NOW(), '特征融合演示数据'),
(3, 'FUS-003', 1, 1, 21, 'SAMPLE-021', 3, '多传感器时序窗口特征,传感器空间关系特征,时间演化特征,故障类别条件特征', 'PCA+Attention', 0.8500, 128, 128, '{"featureComponents":["多传感器时序窗口特征","传感器空间关系特征","时间演化特征","故障类别条件特征"],"vectorDim":128}', '/data/fusion/FUS-003.json', 'admin', NOW(), '特征融合演示数据'),
(4, 'FUS-004', 1, 1, 31, 'SAMPLE-031', 4, '多传感器时序窗口特征,传感器空间关系特征,时间演化特征,故障类别条件特征', 'PCA+Attention', 0.8500, 128, 128, '{"featureComponents":["多传感器时序窗口特征","传感器空间关系特征","时间演化特征","故障类别条件特征"],"vectorDim":128}', '/data/fusion/FUS-004.json', 'admin', NOW(), '特征融合演示数据'),
(5, 'FUS-005', 1, 1, 41, 'SAMPLE-041', 5, '多传感器时序窗口特征,传感器空间关系特征,时间演化特征,故障类别条件特征', 'PCA+Attention', 0.8500, 128, 128, '{"featureComponents":["多传感器时序窗口特征","传感器空间关系特征","时间演化特征","故障类别条件特征"],"vectorDim":128}', '/data/fusion/FUS-005.json', 'admin', NOW(), '特征融合演示数据'),
(6, 'FUS-006', 1, 1, 51, 'SAMPLE-051', 6, '多传感器时序窗口特征,传感器空间关系特征,时间演化特征,故障类别条件特征', 'PCA+Attention', 0.8500, 128, 128, '{"featureComponents":["多传感器时序窗口特征","传感器空间关系特征","时间演化特征","故障类别条件特征"],"vectorDim":128}', '/data/fusion/FUS-006.json', 'admin', NOW(), '特征融合演示数据');

INSERT INTO `fd_diagnosis_result` (`diagnosis_id`, `diagnosis_code`, `pipeline_id`, `dataset_id`, `sample_id`, `sample_code`, `fusion_id`, `model_id`, `model_name`, `fault_type`, `fault_location`, `confidence`, `threshold`, `health_score`, `alarm_level`, `diagnosis_time`, `root_status`, `diagnosis_result_json`, `create_by`, `create_time`, `remark`) VALUES
(1, 'DG-001', 1, 1, 1, 'SAMPLE-001', 1, 1, 'CNN-BiLSTM-Attention', '正常', '无', 0.9600, 0.8000, 96.00, '正常', NOW(), '无需根因分析', '{"faultType":"正常","faultLocation":"无","confidence":0.96,"healthScore":96}', 'admin', NOW(), '故障诊断演示数据'),
(2, 'DG-002', 1, 1, 11, 'SAMPLE-011', 2, 1, 'CNN-BiLSTM-Attention', '内圈故障', '轴承内圈', 0.9100, 0.8000, 72.00, '一般', NOW(), '已完成根因分析', '{"faultType":"内圈故障","faultLocation":"轴承内圈","confidence":0.91,"healthScore":72}', 'admin', NOW(), '故障诊断演示数据'),
(3, 'DG-003', 1, 1, 21, 'SAMPLE-021', 3, 1, 'CNN-BiLSTM-Attention', '滚动体故障', '轴承滚动体', 0.8800, 0.8000, 66.00, '一般', NOW(), '已完成根因分析', '{"faultType":"滚动体故障","faultLocation":"轴承滚动体","confidence":0.88,"healthScore":66}', 'admin', NOW(), '故障诊断演示数据'),
(4, 'DG-004', 1, 1, 31, 'SAMPLE-031', 4, 1, 'CNN-BiLSTM-Attention', '外圈故障', '轴承外圈', 0.8600, 0.8000, 69.00, '严重', NOW(), '已完成根因分析', '{"faultType":"外圈故障","faultLocation":"轴承外圈","confidence":0.86,"healthScore":69}', 'admin', NOW(), '故障诊断演示数据'),
(5, 'DG-005', 1, 1, 41, 'SAMPLE-041', 5, 1, 'CNN-BiLSTM-Attention', '内圈故障', '轴承内圈', 0.8900, 0.8000, 70.00, '一般', NOW(), '已完成根因分析', '{"faultType":"内圈故障","faultLocation":"轴承内圈","confidence":0.89,"healthScore":70}', 'admin', NOW(), '故障诊断演示数据'),
(6, 'DG-006', 1, 1, 51, 'SAMPLE-051', 6, 1, 'CNN-BiLSTM-Attention', '外圈故障', '轴承外圈', 0.8700, 0.8000, 68.00, '严重', NOW(), '已完成根因分析', '{"faultType":"外圈故障","faultLocation":"轴承外圈","confidence":0.87,"healthScore":68}', 'admin', NOW(), '故障诊断演示数据');

INSERT INTO `fd_root_cause_analysis` (`analysis_id`, `analysis_code`, `pipeline_id`, `dataset_id`, `sample_id`, `sample_code`, `diagnosis_id`, `fault_type`, `fault_location`, `specific_root_cause`, `root_cause_confidence`, `evidence_summary`, `evidence_json`, `maintenance_suggestion`, `analysis_status`, `analyst`, `analysis_time`, `create_by`, `create_time`, `remark`) VALUES
(1, 'RCA-001', 1, 1, 11, 'SAMPLE-011', 2, '内圈故障', '轴承内圈', '轴承预紧力设置偏大', 0.8000, '预紧力偏大导致运行温升升高，接触应力增加，进而诱发内圈区域异常振动。', '{"reasoningChain":["预处理样本形成标准化时间窗","样本增强改善类别不平衡","特征融合突出时序和空间关系特征","诊断识别为内圈故障","根因推理得到轴承预紧力设置偏大"]}', '建议复核轴承预紧参数和装配工艺记录，对预紧力偏大的装配件进行重新调整。', '已分析', 'Topic4-RCA-Engine', NOW(), 'admin', NOW(), '根因分析演示数据'),
(2, 'RCA-002', 1, 1, 21, 'SAMPLE-021', 3, '滚动体故障', '轴承滚动体', '滚道微小损伤', 0.7200, '滚道微小损伤导致滚动体经过缺陷区域时产生周期性冲击。', '{"reasoningChain":["滚动体故障样本冲击成分明显","融合特征中时间演化特征贡献升高","诊断结果指向滚动体区域","根因推理得到滚道微小损伤"]}', '建议对滚道和滚动体表面进行显微检查，重点排查划伤、点蚀和剥落。', '已分析', 'Topic4-RCA-Engine', NOW(), 'admin', NOW(), '根因分析演示数据'),
(3, 'RCA-003', 1, 1, 31, 'SAMPLE-031', 4, '外圈故障', '轴承外圈', '装配同轴度偏差', 0.7600, '装配同轴度偏差导致轴承长期承受偏载，外圈接触区域出现局部疲劳损伤。', '{"reasoningChain":["外圈故障特征频率稳定出现","空间关系特征贡献较高","诊断结果指向轴承外圈","根因推理得到装配同轴度偏差"]}', '建议复核轴承座、转轴和端盖装配同轴度，对超差部件重新定位校准。', '已分析', 'Topic4-RCA-Engine', NOW(), 'admin', NOW(), '根因分析演示数据'),
(4, 'RCA-004', 1, 1, 41, 'SAMPLE-041', 5, '内圈故障', '轴承内圈', '批次材料硬度波动', 0.6900, '同批次材料硬度存在波动，使局部接触疲劳寿命下降，诱发内圈故障。', '{"reasoningChain":["高载荷样本内圈故障特征增强","类别条件特征与材料批次信息相关","诊断结果指向轴承内圈","根因推理得到批次材料硬度波动"]}', '建议复查同批次轴承材料硬度检测记录，加强来料检验和批次追溯。', '已分析', 'Topic4-RCA-Engine', NOW(), 'admin', NOW(), '根因分析演示数据'),
(5, 'RCA-005', 1, 1, 51, 'SAMPLE-051', 6, '外圈故障', '轴承外圈', '密封失效导致污染物进入', 0.7300, '密封失效后污染物进入轴承内部，造成润滑劣化和外圈磨粒磨损。', '{"reasoningChain":["外圈故障样本出现宽频噪声抬升","融合特征显示高频波动增强","诊断结果指向轴承外圈","根因推理得到密封失效导致污染物进入"]}', '建议检查密封结构完整性，清理轴承腔体并更换受污染润滑脂。', '已分析', 'Topic4-RCA-Engine', NOW(), 'admin', NOW(), '根因分析演示数据');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- End of project4_data.sql
-- ============================================================
