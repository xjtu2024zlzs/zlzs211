CREATE TABLE t5_trace_case (
                               case_id           BIGINT NOT NULL AUTO_INCREMENT COMMENT '追溯案例ID',
                               case_no           VARCHAR(64) NOT NULL COMMENT '追溯案例编号',
                               case_name         VARCHAR(200) NOT NULL COMMENT '案例名称',

                               system_name       VARCHAR(100) COMMENT '所属系统',
                               subsystem_name    VARCHAR(100) COMMENT '所属子系统',
                               product_model     VARCHAR(100) COMMENT '产品型号/装备型号',

                               fault_title       VARCHAR(200) COMMENT '故障标题',
                               fault_phenomenon  VARCHAR(500) COMMENT '故障现象',
                               fault_desc        TEXT COMMENT '故障详细描述',
                               fault_location    VARCHAR(200) COMMENT '故障发生位置/区域',
                               fault_mode        VARCHAR(100) COMMENT '初步故障模式',

                               abnormal_param    VARCHAR(500) COMMENT '异常参数描述',
                               abnormal_value    VARCHAR(200) COMMENT '异常参数值',
                               normal_range      VARCHAR(200) COMMENT '正常范围',
                               detect_method     VARCHAR(100) COMMENT '发现方式/检测方法',

                               locate_algorithm_id BIGINT COMMENT '零件定位算法ID',
                               locate_algorithm_name VARCHAR(100) COMMENT '零件定位算法名称',

                               final_part_id     BIGINT COMMENT '最终定位故障零件ID',
                               final_part_no     VARCHAR(100) COMMENT '最终定位零件编号',
                               final_part_name   VARCHAR(200) COMMENT '最终定位零件名称',
                               locate_confidence DECIMAL(10,4) COMMENT '零件定位置信度',

                               graph_id          BIGINT COMMENT '关联知识图谱ID',
                               root_result_id    BIGINT COMMENT '关联根因分析结果ID',

                               case_status       CHAR(1) DEFAULT '0' COMMENT '案例状态：0待定位，1已定位零件，2已构建图谱，3已完成根因分析，4失败',

                               create_by         VARCHAR(64) DEFAULT '' COMMENT '创建者',
                               create_time       DATETIME COMMENT '创建时间',
                               update_by         VARCHAR(64) DEFAULT '' COMMENT '更新者',
                               update_time       DATETIME COMMENT '更新时间',
                               remark            VARCHAR(500) COMMENT '备注',

                               PRIMARY KEY (case_id),
                               UNIQUE KEY uk_case_no (case_no),
                               KEY idx_final_part_id (final_part_id),
                               KEY idx_graph_id (graph_id),
                               KEY idx_case_status (case_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课题五-追溯案例与故障表征表';
CREATE TABLE t5_algorithm_config (
                                     algorithm_id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '算法ID',
                                     algorithm_code    VARCHAR(100) NOT NULL COMMENT '算法编码',
                                     algorithm_name    VARCHAR(100) NOT NULL COMMENT '算法名称',
                                     algorithm_type    VARCHAR(50) COMMENT '算法类型：locate/kg_build/root_cause',
                                     algorithm_version VARCHAR(50) COMMENT '算法版本',

                                     algorithm_desc    TEXT COMMENT '算法描述',
                                     input_desc        TEXT COMMENT '输入说明',
                                     output_desc       TEXT COMMENT '输出说明',
                                     default_flag      CHAR(1) DEFAULT '0' COMMENT '是否默认算法：0否，1是',
                                     status            CHAR(1) DEFAULT '0' COMMENT '状态：0启用，1停用',

                                     create_by         VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                     create_time       DATETIME COMMENT '创建时间',
                                     update_by         VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                     update_time       DATETIME COMMENT '更新时间',
                                     remark            VARCHAR(500) COMMENT '备注',

                                     PRIMARY KEY (algorithm_id),
                                     UNIQUE KEY uk_algorithm_code (algorithm_code),
                                     KEY idx_algorithm_type (algorithm_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课题五-算法配置表';
CREATE TABLE t5_part_instance (
                                  part_id           BIGINT NOT NULL AUTO_INCREMENT COMMENT '零部件实例ID',
                                  part_no           VARCHAR(100) NOT NULL COMMENT '零件编号',
                                  part_name         VARCHAR(200) COMMENT '零件名称',
                                  serial_no         VARCHAR(100) COMMENT '零件序列号',
                                  batch_no          VARCHAR(100) COMMENT '批次号',

                                  product_model     VARCHAR(100) COMMENT '产品型号',
                                  system_name       VARCHAR(100) COMMENT '所属系统',
                                  subsystem_name    VARCHAR(100) COMMENT '所属子系统',
                                  material_spec     VARCHAR(100) COMMENT '材料牌号/材料规格',
                                  supplier_name     VARCHAR(200) COMMENT '供应商名称',
                                  install_position  VARCHAR(200) COMMENT '安装位置',

                                  parent_part_id    BIGINT COMMENT '父级部件ID',
                                  lifecycle_status  CHAR(1) DEFAULT '0' COMMENT '生命周期状态：0制造中，1已装配，2服役中，3维修中，4退役',

                                  create_by         VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                  create_time       DATETIME COMMENT '创建时间',
                                  update_by         VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                  update_time       DATETIME COMMENT '更新时间',
                                  remark            VARCHAR(500) COMMENT '备注',

                                  PRIMARY KEY (part_id),
                                  KEY idx_part_no (part_no),
                                  KEY idx_serial_no (serial_no),
                                  KEY idx_batch_no (batch_no),
                                  KEY idx_product_model (product_model)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课题五-零部件实例表';
CREATE TABLE t5_part_locate_result (
                                       locate_id         BIGINT NOT NULL AUTO_INCREMENT COMMENT '零件定位结果ID',
                                       case_id           BIGINT NOT NULL COMMENT '追溯案例ID',
                                       algorithm_id      BIGINT COMMENT '算法ID',
                                       algorithm_name    VARCHAR(100) COMMENT '算法名称',

                                       part_id           BIGINT COMMENT '候选零部件ID',
                                       part_no           VARCHAR(100) COMMENT '候选零件编号',
                                       part_name         VARCHAR(200) COMMENT '候选零件名称',

                                       match_score       DECIMAL(10,4) COMMENT '匹配分数',
                                       confidence        DECIMAL(10,4) COMMENT '置信度',
                                       rank_no           INT COMMENT '排序号',
                                       is_final          CHAR(1) DEFAULT '0' COMMENT '是否最终定位零件：0否，1是',

                                       locate_reason     TEXT COMMENT '定位原因',
                                       evidence_summary  TEXT COMMENT '定位证据摘要',

                                       create_by         VARCHAR(64) DEFAULT '' COMMENT '创建者',
                                       create_time       DATETIME COMMENT '创建时间',
                                       update_by         VARCHAR(64) DEFAULT '' COMMENT '更新者',
                                       update_time       DATETIME COMMENT '更新时间',
                                       remark            VARCHAR(500) COMMENT '备注',

                                       PRIMARY KEY (locate_id),
                                       KEY idx_case_id (case_id),
                                       KEY idx_part_id (part_id),
                                       KEY idx_algorithm_id (algorithm_id),
                                       KEY idx_is_final (is_final)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课题五-故障零件定位结果表';