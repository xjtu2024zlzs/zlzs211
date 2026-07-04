package com.ruoyi.project4.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t4_bearing_preprocess")
public class T4BearingPreprocess {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联原始数据id t4_raw_bearing_data.id */
    private Long sourceId;

    /** 接口入参完整json */
    private String bizParams;

    /** python返回完整结果json */
    private String bizResult;

    /** success / fail */
    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}