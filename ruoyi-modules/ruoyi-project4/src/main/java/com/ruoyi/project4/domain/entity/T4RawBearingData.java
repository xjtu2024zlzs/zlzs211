package com.ruoyi.project4.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t4_raw_bearing_data")
public class T4RawBearingData {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 数据集根目录 */
    private String dataRootPath;

    /** 数据编号 */
    private Integer keyNum;

    /** 原始采样、DE/FE信号JSON */
    private String sampleInfo;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}