-- 课题四：航空装备智能故障诊断模块业务表
-- 表名前缀统一使用 t4_

CREATE TABLE IF NOT EXISTS t4_t4_data_file (
                                               file_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件ID',
                                               file_name VARCHAR(255) NOT NULL COMMENT '文件名称',
    file_path VARCHAR(500) COMMENT '文件路径',
    file_type VARCHAR(50) COMMENT '文件类型',
    file_size BIGINT COMMENT '文件大小',
    upload_status VARCHAR(20) DEFAULT '0' COMMENT '上传状态',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (file_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课题四-数据文件表';

CREATE TABLE IF NOT EXISTS t4_t4_augment_result (
                                                    augment_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据增强结果ID',
                                                    file_id BIGINT COMMENT '文件ID',
                                                    augment_code VARCHAR(100) COMMENT '增强任务编号',
    augment_method VARCHAR(100) COMMENT '增强方法',
    output_path VARCHAR(500) COMMENT '输出路径',
    result_json TEXT COMMENT '增强结果JSON',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (augment_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课题四-数据增强结果表';

CREATE TABLE IF NOT EXISTS t4_t4_diagnosis_result (
                                                      diagnosis_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '故障诊断结果ID',
                                                      file_id BIGINT COMMENT '文件ID',
                                                      sample_id BIGINT COMMENT '样本ID',
                                                      sample_code VARCHAR(100) COMMENT '样本编号',
    fault_type VARCHAR(100) COMMENT '故障类型',
    confidence DECIMAL(10,4) COMMENT '置信度',
    result_json TEXT COMMENT '诊断结果JSON',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (diagnosis_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课题四-故障诊断结果表';

CREATE TABLE IF NOT EXISTS t4_t4_fusion_result (
                                                   fusion_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '融合结果ID',
                                                   fusion_code VARCHAR(100) COMMENT '融合编号',
    pipeline_id BIGINT COMMENT '流程ID',
    dataset_id BIGINT COMMENT '数据集ID',
    sample_id BIGINT COMMENT '样本ID',
    sample_code VARCHAR(100) COMMENT '样本编号',
    augment_id BIGINT COMMENT '增强结果ID',
    feature_components VARCHAR(500) COMMENT '特征组成',
    fusion_method VARCHAR(100) COMMENT '融合方法',
    confidence_weight DECIMAL(10,4) COMMENT '置信权重',
    output_dimension BIGINT COMMENT '输出维度',
    vector_length BIGINT COMMENT '向量长度',
    fusion_vector_json TEXT COMMENT '融合向量JSON',
    vector_path VARCHAR(500) COMMENT '向量路径',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (fusion_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课题四-特征融合结果表';

CREATE TABLE IF NOT EXISTS t4_t4_root_cause_analysis (
                                                         analysis_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '根因分析ID',
                                                         diagnosis_id BIGINT COMMENT '诊断结果ID',
                                                         file_id BIGINT COMMENT '文件ID',
                                                         cause_type VARCHAR(100) COMMENT '根因类型',
    cause_result TEXT COMMENT '根因分析结果',
    result_json TEXT COMMENT '分析结果JSON',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (analysis_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课题四-根因分析结果表';
