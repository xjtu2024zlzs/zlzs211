package com.ruoyi.project1.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * Match task object p1p_match_task.
 *
 * The extra current* fields are transient page-facing fields populated by the
 * service layer. They are not database columns.
 */
public class MatchTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** Task primary key. */
    private Long taskId;

    /** Task name. */
    @Excel(name = "任务名称")
    private String taskName;

    /** Current task version primary key. */
    @Excel(name = "当前版本ID")
    private Long currentVersionId;

    /** Current version number within this task, used only for page display. */
    private Long currentVersionNo;

    /** Current version encoding mode key, used only for page display. */
    private String currentEncodingModeKey;

    /** Current version embedding model key, used only for page display. */
    private String currentEmbeddingModelKey;

    /** Current version evaluation mode key, used only for page display. */
    private String currentEvalModeKey;

    /** Current version auto approve threshold, used only for page display. */
    private BigDecimal currentAutoApproveThreshold;

    /** Latest run progress, used only for page display. */
    private Long lastRecordProgress;

    /** Current version detail object, used only for edit/detail payloads. */
    private MatchTaskVersion currentVersion;

    /** Source datasource id. */
    @Excel(name = "来源数据源ID")
    private Long sourceDatasourceId;

    /** Lifecycle status: draft, active, disabled, archived. */
    @Excel(name = "任务状态")
    private String lifecycleStatus;

    /** Latest run status summary. */
    @Excel(name = "最近运行状态")
    private String lastRecordStatus;

    /** Latest run stage summary. */
    @Excel(name = "运行阶段")
    private String lastRecordStage;

    /** RuoYi status: 0 normal, 1 disabled. */
    @Excel(name = "若依状态")
    private String status;

    /** Delete flag: 0 exists, 2 deleted. */
    private String delFlag;

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskName(String taskName)
    {
        this.taskName = taskName;
    }

    public String getTaskName()
    {
        return taskName;
    }

    public void setCurrentVersionId(Long currentVersionId)
    {
        this.currentVersionId = currentVersionId;
    }

    public Long getCurrentVersionId()
    {
        return currentVersionId;
    }

    public void setCurrentVersionNo(Long currentVersionNo)
    {
        this.currentVersionNo = currentVersionNo;
    }

    public Long getCurrentVersionNo()
    {
        return currentVersionNo;
    }

    public void setCurrentEncodingModeKey(String currentEncodingModeKey)
    {
        this.currentEncodingModeKey = currentEncodingModeKey;
    }

    public String getCurrentEncodingModeKey()
    {
        return currentEncodingModeKey;
    }

    public void setCurrentEmbeddingModelKey(String currentEmbeddingModelKey)
    {
        this.currentEmbeddingModelKey = currentEmbeddingModelKey;
    }

    public String getCurrentEmbeddingModelKey()
    {
        return currentEmbeddingModelKey;
    }

    public void setCurrentEvalModeKey(String currentEvalModeKey)
    {
        this.currentEvalModeKey = currentEvalModeKey;
    }

    public String getCurrentEvalModeKey()
    {
        return currentEvalModeKey;
    }

    public void setCurrentAutoApproveThreshold(BigDecimal currentAutoApproveThreshold)
    {
        this.currentAutoApproveThreshold = currentAutoApproveThreshold;
    }

    public BigDecimal getCurrentAutoApproveThreshold()
    {
        return currentAutoApproveThreshold;
    }

    public void setLastRecordProgress(Long lastRecordProgress)
    {
        this.lastRecordProgress = lastRecordProgress;
    }

    public Long getLastRecordProgress()
    {
        return lastRecordProgress;
    }

    public void setCurrentVersion(MatchTaskVersion currentVersion)
    {
        this.currentVersion = currentVersion;
    }

    public MatchTaskVersion getCurrentVersion()
    {
        return currentVersion;
    }

    public void setSourceDatasourceId(Long sourceDatasourceId)
    {
        this.sourceDatasourceId = sourceDatasourceId;
    }

    public Long getSourceDatasourceId()
    {
        return sourceDatasourceId;
    }

    public void setLifecycleStatus(String lifecycleStatus)
    {
        this.lifecycleStatus = lifecycleStatus;
    }

    public String getLifecycleStatus()
    {
        return lifecycleStatus;
    }

    public void setLastRecordStatus(String lastRecordStatus)
    {
        this.lastRecordStatus = lastRecordStatus;
    }

    public String getLastRecordStatus()
    {
        return lastRecordStatus;
    }

    public void setLastRecordStage(String lastRecordStage)
    {
        this.lastRecordStage = lastRecordStage;
    }

    public String getLastRecordStage()
    {
        return lastRecordStage;
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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("taskId", getTaskId())
            .append("taskName", getTaskName())
            .append("currentVersionId", getCurrentVersionId())
            .append("currentVersionNo", getCurrentVersionNo())
            .append("currentEncodingModeKey", getCurrentEncodingModeKey())
            .append("currentEmbeddingModelKey", getCurrentEmbeddingModelKey())
            .append("currentEvalModeKey", getCurrentEvalModeKey())
            .append("currentAutoApproveThreshold", getCurrentAutoApproveThreshold())
            .append("lastRecordProgress", getLastRecordProgress())
            .append("currentVersion", getCurrentVersion())
            .append("sourceDatasourceId", getSourceDatasourceId())
            .append("lifecycleStatus", getLifecycleStatus())
            .append("lastRecordStatus", getLastRecordStatus())
            .append("lastRecordStage", getLastRecordStage())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
