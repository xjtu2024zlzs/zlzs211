package com.ruoyi.project1.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 模式映射运行记录对象 p1p_match_task_record
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class MatchTaskRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 运行记录主键 */
    private Long recordId;

    /** 所属任务 ID */
    @Excel(name = "所属任务 ID")
    private Long taskId;

    /** 执行任务版本 ID */
    @Excel(name = "执行任务版本 ID")
    private Long versionId;

    /** 运行使用的数据源模式快照 ID */
    @Excel(name = "运行使用的数据源模式快照 ID")
    private Long schemaSnapshotId;

    /** 本次运行默认结果集 ID */
    @Excel(name = "本次运行默认结果集 ID")
    private Long defaultResultSetId;

    /** 运行状态：queued 排队、running 运行中、success 成功、failed 失败、blocked 阻断、canceled 已取消 */
    @Excel(name = "运行状态：queued 排队、running 运行中、success 成功、failed 失败、blocked 阻断、canceled 已取消")
    private String executionStatus;

    /** 当前执行阶段 */
    @Excel(name = "当前执行阶段")
    private String currentStage;

    /** 运行进度百分比 */
    @Excel(name = "运行进度百分比")
    private Long progress;

    /** 算法参数运行快照 JSON */
    @Excel(name = "算法参数运行快照 JSON")
    private String algorithmParamsSnapshot;

    /** 算法结果目录 */
    @Excel(name = "算法结果目录")
    private String resultsDir;

    /** 运行日志文件路径 */
    @Excel(name = "运行日志文件路径")
    private String logFile;

    /** 错误信息 */
    @Excel(name = "错误信息")
    private String errorMessage;

    /** 取消请求时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "取消请求时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date cancelRequestedAt;

    /** 取消完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "取消完成时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date canceledAt;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startedAt;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date finishedAt;

    public void setRecordId(Long recordId) 
    {
        this.recordId = recordId;
    }

    public Long getRecordId() 
    {
        return recordId;
    }

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }

    public void setVersionId(Long versionId) 
    {
        this.versionId = versionId;
    }

    public Long getVersionId() 
    {
        return versionId;
    }

    public void setSchemaSnapshotId(Long schemaSnapshotId) 
    {
        this.schemaSnapshotId = schemaSnapshotId;
    }

    public Long getSchemaSnapshotId() 
    {
        return schemaSnapshotId;
    }

    public void setDefaultResultSetId(Long defaultResultSetId) 
    {
        this.defaultResultSetId = defaultResultSetId;
    }

    public Long getDefaultResultSetId() 
    {
        return defaultResultSetId;
    }

    public void setExecutionStatus(String executionStatus) 
    {
        this.executionStatus = executionStatus;
    }

    public String getExecutionStatus() 
    {
        return executionStatus;
    }

    public void setCurrentStage(String currentStage) 
    {
        this.currentStage = currentStage;
    }

    public String getCurrentStage() 
    {
        return currentStage;
    }

    public void setProgress(Long progress) 
    {
        this.progress = progress;
    }

    public Long getProgress() 
    {
        return progress;
    }

    public void setAlgorithmParamsSnapshot(String algorithmParamsSnapshot) 
    {
        this.algorithmParamsSnapshot = algorithmParamsSnapshot;
    }

    public String getAlgorithmParamsSnapshot() 
    {
        return algorithmParamsSnapshot;
    }

    public void setResultsDir(String resultsDir) 
    {
        this.resultsDir = resultsDir;
    }

    public String getResultsDir() 
    {
        return resultsDir;
    }

    public void setLogFile(String logFile) 
    {
        this.logFile = logFile;
    }

    public String getLogFile() 
    {
        return logFile;
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

    public void setCanceledAt(Date canceledAt) 
    {
        this.canceledAt = canceledAt;
    }

    public Date getCanceledAt() 
    {
        return canceledAt;
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
            .append("recordId", getRecordId())
            .append("taskId", getTaskId())
            .append("versionId", getVersionId())
            .append("schemaSnapshotId", getSchemaSnapshotId())
            .append("defaultResultSetId", getDefaultResultSetId())
            .append("executionStatus", getExecutionStatus())
            .append("currentStage", getCurrentStage())
            .append("progress", getProgress())
            .append("algorithmParamsSnapshot", getAlgorithmParamsSnapshot())
            .append("resultsDir", getResultsDir())
            .append("logFile", getLogFile())
            .append("errorMessage", getErrorMessage())
            .append("cancelRequestedAt", getCancelRequestedAt())
            .append("canceledAt", getCanceledAt())
            .append("startedAt", getStartedAt())
            .append("finishedAt", getFinishedAt())
            .append("createTime", getCreateTime())
            .append("remark", getRemark())
            .toString();
    }
}
