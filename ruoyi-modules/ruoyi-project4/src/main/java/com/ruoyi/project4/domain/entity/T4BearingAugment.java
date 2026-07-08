package com.ruoyi.project4.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t4_bearing_augment")
public class T4BearingAugment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联预处理id t4_bearing_preprocess.id */
    private Long sourceId;

    /** 增强模式 scale */
    private String augModel;

    /** 增强倍数 */
    private Integer augScale;

    /** 完整入参json */
    private String bizParams;

    /** python返回完整结果json */
    private String bizResult;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}