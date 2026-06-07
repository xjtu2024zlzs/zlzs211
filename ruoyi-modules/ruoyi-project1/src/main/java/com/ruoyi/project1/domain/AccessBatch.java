package com.ruoyi.project1.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 接入执行批次对象 p1p_access_batch
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class AccessBatch extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 接入执行批次主键 */
    private Long accessBatchId;

    /** 接入计划 ID */
    @Excel(name = "接入计划 ID")
    private Long accessPlanId;

    /** 执行批次号 */
    @Excel(name = "执行批次号")
    private String batchNo;

    /** 触发方式：manual 手动、schedule 定时 */
    @Excel(name = "触发方式：manual 手动、schedule 定时")
    private String triggerType;

    /** 批次状态：pending 待执行、running 执行中、success 成功、partial 部分成功、failed 失败、canceled 已取消 */
    @Excel(name = "批次状态：pending 待执行、running 执行中、success 成功、partial 部分成功、failed 失败、canceled 已取消")
    private String batchStatus;

    /** 源端读取记录数 */
    @Excel(name = "源端读取记录数")
    private Long readCount;

    /** 写入中间库记录数 */
    @Excel(name = "写入中间库记录数")
    private Long stagedCount;

    /** 转换成功数 */
    @Excel(name = "转换成功数")
    private Long transformSuccessCount;

    /** 转换失败数 */
    @Excel(name = "转换失败数")
    private Long transformFailedCount;

    /** 写入成功数 */
    @Excel(name = "写入成功数")
    private Long writeSuccessCount;

    /** 写入失败数 */
    @Excel(name = "写入失败数")
    private Long writeFailedCount;

    /** 本次新增数 */
    @Excel(name = "本次新增数")
    private Long insertedCount;

    /** 本次更新数 */
    @Excel(name = "本次更新数")
    private Long updatedCount;

    /** 批次错误信息 */
    @Excel(name = "批次错误信息")
    private String errorMessage;

    /** 取消请求时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "取消请求时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date cancelRequestedAt;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startedAt;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date finishedAt;

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

    public void setBatchNo(String batchNo) 
    {
        this.batchNo = batchNo;
    }

    public String getBatchNo() 
    {
        return batchNo;
    }

    public void setTriggerType(String triggerType) 
    {
        this.triggerType = triggerType;
    }

    public String getTriggerType() 
    {
        return triggerType;
    }

    public void setBatchStatus(String batchStatus) 
    {
        this.batchStatus = batchStatus;
    }

    public String getBatchStatus() 
    {
        return batchStatus;
    }

    public void setReadCount(Long readCount) 
    {
        this.readCount = readCount;
    }

    public Long getReadCount() 
    {
        return readCount;
    }

    public void setStagedCount(Long stagedCount) 
    {
        this.stagedCount = stagedCount;
    }

    public Long getStagedCount() 
    {
        return stagedCount;
    }

    public void setTransformSuccessCount(Long transformSuccessCount) 
    {
        this.transformSuccessCount = transformSuccessCount;
    }

    public Long getTransformSuccessCount() 
    {
        return transformSuccessCount;
    }

    public void setTransformFailedCount(Long transformFailedCount) 
    {
        this.transformFailedCount = transformFailedCount;
    }

    public Long getTransformFailedCount() 
    {
        return transformFailedCount;
    }

    public void setWriteSuccessCount(Long writeSuccessCount) 
    {
        this.writeSuccessCount = writeSuccessCount;
    }

    public Long getWriteSuccessCount() 
    {
        return writeSuccessCount;
    }

    public void setWriteFailedCount(Long writeFailedCount) 
    {
        this.writeFailedCount = writeFailedCount;
    }

    public Long getWriteFailedCount() 
    {
        return writeFailedCount;
    }

    public void setInsertedCount(Long insertedCount) 
    {
        this.insertedCount = insertedCount;
    }

    public Long getInsertedCount() 
    {
        return insertedCount;
    }

    public void setUpdatedCount(Long updatedCount) 
    {
        this.updatedCount = updatedCount;
    }

    public Long getUpdatedCount() 
    {
        return updatedCount;
    }

    public void setErrorMessage(String errorMessage) 
    {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() 
    {
        return errorMessage;
    }

    public void setCancelRequestedAt(Date cancelRequestedAt) 
    {
        this.cancelRequestedAt = cancelRequestedAt;
    }

    public Date getCancelRequestedAt() 
    {
        return cancelRequestedAt;
    }

    public void setStartedAt(Date startedAt) 
    {
        this.startedAt = startedAt;
    }

    public Date getStartedAt() 
    {
        return startedAt;
    }

    public void setFinishedAt(Date finishedAt) 
    {
        this.finishedAt = finishedAt;
    }

    public Date getFinishedAt() 
    {
        return finishedAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("accessBatchId", getAccessBatchId())
            .append("accessPlanId", getAccessPlanId())
            .append("batchNo", getBatchNo())
            .append("triggerType", getTriggerType())
            .append("batchStatus", getBatchStatus())
            .append("readCount", getReadCount())
            .append("stagedCount", getStagedCount())
            .append("transformSuccessCount", getTransformSuccessCount())
            .append("transformFailedCount", getTransformFailedCount())
            .append("writeSuccessCount", getWriteSuccessCount())
            .append("writeFailedCount", getWriteFailedCount())
            .append("insertedCount", getInsertedCount())
            .append("updatedCount", getUpdatedCount())
            .append("errorMessage", getErrorMessage())
            .append("cancelRequestedAt", getCancelRequestedAt())
            .append("startedAt", getStartedAt())
            .append("finishedAt", getFinishedAt())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
