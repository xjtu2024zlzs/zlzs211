package com.ruoyi.project1.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 数据接入管理对象 p1p_access_plan
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class AccessPlan extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 接入计划主键 */
    private Long accessPlanId;

    /** 接入计划名称 */
    @Excel(name = "接入计划名称")
    private String planName;

    /** 来源数据源 ID */
    @Excel(name = "来源数据源 ID")
    private Long sourceDatasourceId;

    /** 接入方式：db_direct 数据库直连、api_pull API 拉取、api_push API 推送 */
    @Excel(name = "接入方式：db_direct 数据库直连、api_pull API 拉取、api_push API 推送")
    private String accessMode;

    /** 接入类型：once 一次性接入、continuous 持续接入 */
    @Excel(name = "接入类型：once 一次性接入、continuous 持续接入")
    private String accessType;

    /** 持续接入更新周期，单位小时，默认 1 小时 */
    @Excel(name = "持续接入更新周期，单位小时，默认 1 小时")
    private Long cycleHours;

    /** 最终接入规则集 ID，接入执行依据 */
    @Excel(name = "最终接入规则集 ID，接入执行依据")
    private Long specSetId;

    /** 使用状态：enabled 启用、paused 暂停、disabled 停用、unavailable 暂时不可用 */
    @Excel(name = "使用状态：enabled 启用、paused 暂停、disabled 停用、unavailable 暂时不可用")
    private String useStatus;

    /** 当前或最近执行批次 ID */
    @Excel(name = "当前或最近执行批次 ID")
    private Long currentBatchId;

    /** 最近一次成功数 */
    @Excel(name = "最近一次成功数")
    private Long lastSuccessCount;

    /** 最近一次失败数 */
    @Excel(name = "最近一次失败数")
    private Long lastFailedCount;

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

    /** 若依状态：0正常，1停用 */
    @Excel(name = "若依状态：0正常，1停用")
    private String status;

    /** 删除标志：0存在，2删除 */
    private String delFlag;

    /** 来源数据源名称，列表展示用 */
    private String sourceDatasourceName;

    /** 默认结果集名称，列表展示用 */
    private String defaultResultSetName;

    /** 最近一次接入新增数，列表展示用 */
    private Long lastInsertedCount;

    /** 最近一次接入更新数，列表展示用 */
    private Long lastUpdatedCount;

    /** 当前展示状态，列表展示用 */
    private String displayStatus;

    public void setAccessPlanId(Long accessPlanId) 
    {
        this.accessPlanId = accessPlanId;
    }

    public Long getAccessPlanId() 
    {
        return accessPlanId;
    }

    public void setPlanName(String planName) 
    {
        this.planName = planName;
    }

    public String getPlanName() 
    {
        return planName;
    }

    public void setSourceDatasourceId(Long sourceDatasourceId) 
    {
        this.sourceDatasourceId = sourceDatasourceId;
    }

    public Long getSourceDatasourceId() 
    {
        return sourceDatasourceId;
    }

    public void setAccessMode(String accessMode) 
    {
        this.accessMode = accessMode;
    }

    public String getAccessMode() 
    {
        return accessMode;
    }

    public void setAccessType(String accessType) 
    {
        this.accessType = accessType;
    }

    public String getAccessType() 
    {
        return accessType;
    }

    public void setCycleHours(Long cycleHours) 
    {
        this.cycleHours = cycleHours;
    }

    public Long getCycleHours() 
    {
        return cycleHours;
    }

    public void setSpecSetId(Long specSetId) 
    {
        this.specSetId = specSetId;
    }

    public Long getSpecSetId() 
    {
        return specSetId;
    }

    public void setUseStatus(String useStatus) 
    {
        this.useStatus = useStatus;
    }

    public String getUseStatus() 
    {
        return useStatus;
    }

    public void setCurrentBatchId(Long currentBatchId) 
    {
        this.currentBatchId = currentBatchId;
    }

    public Long getCurrentBatchId() 
    {
        return currentBatchId;
    }

    public void setLastSuccessCount(Long lastSuccessCount) 
    {
        this.lastSuccessCount = lastSuccessCount;
    }

    public Long getLastSuccessCount() 
    {
        return lastSuccessCount;
    }

    public void setLastFailedCount(Long lastFailedCount) 
    {
        this.lastFailedCount = lastFailedCount;
    }

    public Long getLastFailedCount() 
    {
        return lastFailedCount;
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

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    public void setSourceDatasourceName(String sourceDatasourceName)
    {
        this.sourceDatasourceName = sourceDatasourceName;
    }

    public String getSourceDatasourceName()
    {
        return sourceDatasourceName;
    }

    public void setDefaultResultSetName(String defaultResultSetName)
    {
        this.defaultResultSetName = defaultResultSetName;
    }

    public String getDefaultResultSetName()
    {
        return defaultResultSetName;
    }

    public void setLastInsertedCount(Long lastInsertedCount)
    {
        this.lastInsertedCount = lastInsertedCount;
    }

    public Long getLastInsertedCount()
    {
        return lastInsertedCount;
    }

    public void setLastUpdatedCount(Long lastUpdatedCount)
    {
        this.lastUpdatedCount = lastUpdatedCount;
    }

    public Long getLastUpdatedCount()
    {
        return lastUpdatedCount;
    }

    public void setDisplayStatus(String displayStatus)
    {
        this.displayStatus = displayStatus;
    }

    public String getDisplayStatus()
    {
        return displayStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("accessPlanId", getAccessPlanId())
            .append("planName", getPlanName())
            .append("sourceDatasourceId", getSourceDatasourceId())
            .append("accessMode", getAccessMode())
            .append("accessType", getAccessType())
            .append("cycleHours", getCycleHours())
            .append("specSetId", getSpecSetId())
            .append("useStatus", getUseStatus())
            .append("currentBatchId", getCurrentBatchId())
            .append("lastSuccessCount", getLastSuccessCount())
            .append("lastFailedCount", getLastFailedCount())
            .append("totalSuccessCount", getTotalSuccessCount())
            .append("totalFailedCount", getTotalFailedCount())
            .append("lastExecuteTime", getLastExecuteTime())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("sourceDatasourceName", getSourceDatasourceName())
            .append("defaultResultSetName", getDefaultResultSetName())
            .append("lastInsertedCount", getLastInsertedCount())
            .append("lastUpdatedCount", getLastUpdatedCount())
            .append("displayStatus", getDisplayStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
