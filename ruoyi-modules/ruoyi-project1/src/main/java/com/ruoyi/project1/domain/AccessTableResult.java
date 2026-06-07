package com.ruoyi.project1.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 数据接入结果展示对象 p1p_access_table_result
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class AccessTableResult extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 目标表接入结果主键 */
    private Long tableResultId;

    /** 接入执行批次 ID */
    @Excel(name = "接入执行批次 ID")
    private Long accessBatchId;

    /** 接入计划 ID */
    @Excel(name = "接入计划 ID")
    private Long accessPlanId;

    /** 接入范围目标表 ID */
    @Excel(name = "接入范围目标表 ID")
    private Long scopeTableId;

    /** 源表名 */
    @Excel(name = "源表名")
    private String sourceTable;

    /** 目标卷宗表名 */
    @Excel(name = "目标卷宗表名")
    private String targetTable;

    /** 结果状态：pending 待执行、running 执行中、success 成功、partial 部分成功、failed 失败 */
    @Excel(name = "结果状态：pending 待执行、running 执行中、success 成功、partial 部分成功、failed 失败")
    private String resultStatus;

    /** 源端读取数 */
    @Excel(name = "源端读取数")
    private Long readCount;

    /** 中间库暂存数 */
    @Excel(name = "中间库暂存数")
    private Long stagedCount;

    /** 本次新增数 */
    @Excel(name = "本次新增数")
    private Long insertedCount;

    /** 本次更新数 */
    @Excel(name = "本次更新数")
    private Long updatedCount;

    /** 本次成功数 */
    @Excel(name = "本次成功数")
    private Long successCount;

    /** 本次失败数 */
    @Excel(name = "本次失败数")
    private Long failedCount;

    /** 累计成功数 */
    @Excel(name = "累计成功数")
    private Long totalSuccessCount;

    /** 累计失败数 */
    @Excel(name = "累计失败数")
    private Long totalFailedCount;

    /** 最近执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最近执行时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastExecuteTime;

    /** 结果说明 */
    @Excel(name = "结果说明")
    private String message;

    public void setTableResultId(Long tableResultId) 
    {
        this.tableResultId = tableResultId;
    }

    public Long getTableResultId() 
    {
        return tableResultId;
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

    public void setSourceTable(String sourceTable) 
    {
        this.sourceTable = sourceTable;
    }

    public String getSourceTable() 
    {
        return sourceTable;
    }

    public void setTargetTable(String targetTable) 
    {
        this.targetTable = targetTable;
    }

    public String getTargetTable() 
    {
        return targetTable;
    }

    public void setResultStatus(String resultStatus) 
    {
        this.resultStatus = resultStatus;
    }

    public String getResultStatus() 
    {
        return resultStatus;
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

    public void setSuccessCount(Long successCount) 
    {
        this.successCount = successCount;
    }

    public Long getSuccessCount() 
    {
        return successCount;
    }

    public void setFailedCount(Long failedCount) 
    {
        this.failedCount = failedCount;
    }

    public Long getFailedCount() 
    {
        return failedCount;
    }

    public void setTotalSuccessCount(Long totalSuccessCount) 
    {
        this.totalSuccessCount = totalSuccessCount;
    }

    public Long getTotalSuccessCount() 
    {
        return totalSuccessCount;
    }

    public void setTotalFailedCount(Long totalFailedCount) 
    {
        this.totalFailedCount = totalFailedCount;
    }

    public Long getTotalFailedCount() 
    {
        return totalFailedCount;
    }

    public void setLastExecuteTime(Date lastExecuteTime) 
    {
        this.lastExecuteTime = lastExecuteTime;
    }

    public Date getLastExecuteTime() 
    {
        return lastExecuteTime;
    }

    public void setMessage(String message) 
    {
        this.message = message;
    }

    public String getMessage() 
    {
        return message;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("tableResultId", getTableResultId())
            .append("accessBatchId", getAccessBatchId())
            .append("accessPlanId", getAccessPlanId())
            .append("scopeTableId", getScopeTableId())
            .append("sourceTable", getSourceTable())
            .append("targetTable", getTargetTable())
            .append("resultStatus", getResultStatus())
            .append("readCount", getReadCount())
            .append("stagedCount", getStagedCount())
            .append("insertedCount", getInsertedCount())
            .append("updatedCount", getUpdatedCount())
            .append("successCount", getSuccessCount())
            .append("failedCount", getFailedCount())
            .append("totalSuccessCount", getTotalSuccessCount())
            .append("totalFailedCount", getTotalFailedCount())
            .append("lastExecuteTime", getLastExecuteTime())
            .append("message", getMessage())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
