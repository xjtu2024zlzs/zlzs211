package com.ruoyi.project1.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 接入转换记录对象 p1p_access_transform_record
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class AccessTransformRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 接入转换记录主键 */
    private Long transformRecordId;

    /** 接入中间库原始记录 ID */
    @Excel(name = "接入中间库原始记录 ID")
    private Long stageRecordId;

    /** 接入执行批次 ID */
    @Excel(name = "接入执行批次 ID")
    private Long accessBatchId;

    /** 接入计划 ID */
    @Excel(name = "接入计划 ID")
    private Long accessPlanId;

    /** 接入范围目标表 ID */
    @Excel(name = "接入范围目标表 ID")
    private Long scopeTableId;

    /** 目标卷宗表名 */
    @Excel(name = "目标卷宗表名")
    private String targetTable;

    /** 目标记录主键值或业务唯一键 */
    @Excel(name = "目标记录主键值或业务唯一键")
    private String targetPk;

    /** 格式转换后的目标数据 JSON */
    @Excel(name = "格式转换后的目标数据 JSON")
    private String transformedData;

    /** 转换状态：pending 待转换、success 成功、failed 失败 */
    @Excel(name = "转换状态：pending 待转换、success 成功、failed 失败")
    private String transformStatus;

    /** 写入状态：pending 待写入、inserted 已新增、updated 已更新、failed 失败、skipped 跳过 */
    @Excel(name = "写入状态：pending 待写入、inserted 已新增、updated 已更新、failed 失败、skipped 跳过")
    private String writeStatus;

    /** 转换或写入失败原因 */
    @Excel(name = "转换或写入失败原因")
    private String errorMessage;

    /** 转换时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "转换时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date transformedAt;

    /** 写入时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "写入时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date writtenAt;

    public void setTransformRecordId(Long transformRecordId) 
    {
        this.transformRecordId = transformRecordId;
    }

    public Long getTransformRecordId() 
    {
        return transformRecordId;
    }

    public void setStageRecordId(Long stageRecordId) 
    {
        this.stageRecordId = stageRecordId;
    }

    public Long getStageRecordId() 
    {
        return stageRecordId;
    }

    public void setAccessBatchId(Long accessBatchId) 
    {
        this.accessBatchId = accessBatchId;
    }

    public Long getAccessBatchId() 
    {
        return accessBatchId;
    }

    public void setAccessPlanId(Long accessPlanId) 
    {
        this.accessPlanId = accessPlanId;
    }

    public Long getAccessPlanId() 
    {
        return accessPlanId;
    }

    public void setScopeTableId(Long scopeTableId) 
    {
        this.scopeTableId = scopeTableId;
    }

    public Long getScopeTableId() 
    {
        return scopeTableId;
    }

    public void setTargetTable(String targetTable) 
    {
        this.targetTable = targetTable;
    }

    public String getTargetTable() 
    {
        return targetTable;
    }

    public void setTargetPk(String targetPk) 
    {
        this.targetPk = targetPk;
    }

    public String getTargetPk() 
    {
        return targetPk;
    }

    public void setTransformedData(String transformedData) 
    {
        this.transformedData = transformedData;
    }

    public String getTransformedData() 
    {
        return transformedData;
    }

    public void setTransformStatus(String transformStatus) 
    {
        this.transformStatus = transformStatus;
    }

    public String getTransformStatus() 
    {
        return transformStatus;
    }

    public void setWriteStatus(String writeStatus) 
    {
        this.writeStatus = writeStatus;
    }

    public String getWriteStatus() 
    {
        return writeStatus;
    }

    public void setErrorMessage(String errorMessage) 
    {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() 
    {
        return errorMessage;
    }

    public void setTransformedAt(Date transformedAt) 
    {
        this.transformedAt = transformedAt;
    }

    public Date getTransformedAt() 
    {
        return transformedAt;
    }

    public void setWrittenAt(Date writtenAt) 
    {
        this.writtenAt = writtenAt;
    }

    public Date getWrittenAt() 
    {
        return writtenAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("transformRecordId", getTransformRecordId())
            .append("stageRecordId", getStageRecordId())
            .append("accessBatchId", getAccessBatchId())
            .append("accessPlanId", getAccessPlanId())
            .append("scopeTableId", getScopeTableId())
            .append("targetTable", getTargetTable())
            .append("targetPk", getTargetPk())
            .append("transformedData", getTransformedData())
            .append("transformStatus", getTransformStatus())
            .append("writeStatus", getWriteStatus())
            .append("errorMessage", getErrorMessage())
            .append("transformedAt", getTransformedAt())
            .append("writtenAt", getWrittenAt())
            .append("createTime", getCreateTime())
            .append("remark", getRemark())
            .toString();
    }
}
