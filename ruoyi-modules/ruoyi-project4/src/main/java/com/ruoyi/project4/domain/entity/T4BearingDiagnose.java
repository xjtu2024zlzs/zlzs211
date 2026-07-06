package com.ruoyi.project4.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t4_bearing_diagnose")
public class T4BearingDiagnose {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联预处理id t4_bearing_preprocess.id */
    private Long sourceId;

    /** DE模态权重 */
    private Double wX1;

    /** FE模态权重 */
    private Double wX2;

    /** 完整入参json */
    private String bizParams;

    /** python诊断全套结果（故障、logits、图表） */
    private String bizResult;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}