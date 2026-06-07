package com.ruoyi.project1.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 接入中间库原始记录对象 p1p_access_stage_record
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class AccessStageRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 接入中间库原始记录主键 */
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

    /** 来源数据源 ID */
    @Excel(name = "来源数据源 ID")
    private Long sourceDatasourceId;

    /** 源数据库名 */
    @Excel(name = "源数据库名")
    private String sourceDatabase;

    /** 源表名 */
    @Excel(name = "源表名")
    private String sourceTable;

    /** 源记录主键值或业务唯一键 */
    @Excel(name = "源记录主键值或业务唯一键")
    private String sourcePk;

    /** 源端操作类型：insert 新增、update 更新、upsert 新增或更新 */
    @Excel(name = "源端操作类型：insert 新增、update 更新、upsert 新增或更新")
    private String sourceOperation;

    /** 源端读取到的原始数据 JSON */
    @Excel(name = "源端读取到的原始数据 JSON")
    private String rawData;

    /** 读取状态：success 成功、failed 失败 */
    @Excel(name = "读取状态：success 成功、failed 失败")
    private String readStatus;

    /** 读取时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "读取时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date readTime;

    /** 读取失败原因 */
    @Excel(name = "读取失败原因")
    private String errorMessage;

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

    public void setSourceDatasourceId(Long sourceDatasourceId) 
    {
        this.sourceDatasourceId = sourceDatasourceId;
    }

    public Long getSourceDatasourceId() 
    {
        return sourceDatasourceId;
    }

    public void setSourceDatabase(String sourceDatabase) 
    {
        this.sourceDatabase = sourceDatabase;
    }

    public String getSourceDatabase() 
    {
        return sourceDatabase;
    }

    public void setSourceTable(String sourceTable) 
    {
        this.sourceTable = sourceTable;
    }

    public String getSourceTable() 
    {
        return sourceTable;
    }

    public void setSourcePk(String sourcePk) 
    {
        this.sourcePk = sourcePk;
    }

    public String getSourcePk() 
    {
        return sourcePk;
    }

    public void setSourceOperation(String sourceOperation) 
    {
        this.sourceOperation = sourceOperation;
    }

    public String getSourceOperation() 
    {
        return sourceOperation;
    }

    public void setRawData(String rawData) 
    {
        this.rawData = rawData;
    }

    public String getRawData() 
    {
        return rawData;
    }

    public void setReadStatus(String readStatus) 
    {
        this.readStatus = readStatus;
    }

    public String getReadStatus() 
    {
        return readStatus;
    }

    public void setReadTime(Date readTime) 
    {
        this.readTime = readTime;
    }

    public Date getReadTime() 
    {
        return readTime;
    }

    public void setErrorMessage(String errorMessage) 
    {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() 
    {
        return errorMessage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("stageRecordId", getStageRecordId())
            .append("accessBatchId", getAccessBatchId())
            .append("accessPlanId", getAccessPlanId())
            .append("scopeTableId", getScopeTableId())
            .append("sourceDatasourceId", getSourceDatasourceId())
            .append("sourceDatabase", getSourceDatabase())
            .append("sourceTable", getSourceTable())
            .append("sourcePk", getSourcePk())
            .append("sourceOperation", getSourceOperation())
            .append("rawData", getRawData())
            .append("readStatus", getReadStatus())
            .append("readTime", getReadTime())
            .append("errorMessage", getErrorMessage())
            .append("createTime", getCreateTime())
            .append("remark", getRemark())
            .toString();
    }
}
