package com.ruoyi.project1.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 字段匹配审核历史对象 p1p_review_history
 * 
 * @author pwb
 * @date 2026-06-05
 */
public class ReviewHistory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 审核历史主键 */
    private Long historyId;

    /** 审核记录 ID */
    @Excel(name = "审核记录 ID")
    private Long reviewId;

    /** 任务 ID */
    @Excel(name = "任务 ID")
    private Long taskId;

    /** 任务版本 ID */
    @Excel(name = "任务版本 ID")
    private Long versionId;

    /** 运行记录 ID */
    @Excel(name = "运行记录 ID")
    private Long recordId;

    /** 原审核状态 */
    @Excel(name = "原审核状态")
    private String oldStatus;

    /** 新审核状态 */
    @Excel(name = "新审核状态")
    private String newStatus;

    /** 原目标表 */
    @Excel(name = "原目标表")
    private String oldTargetTable;

    /** 原目标字段 */
    @Excel(name = "原目标字段")
    private String oldTargetColumn;

    /** 新目标表 */
    @Excel(name = "新目标表")
    private String newTargetTable;

    /** 新目标字段 */
    @Excel(name = "新目标字段")
    private String newTargetColumn;

    /** 操作人 */
    @Excel(name = "操作人")
    private String actor;

    /** 操作类型：approve 通过、reject 驳回、modify 修改 */
    @Excel(name = "操作类型：approve 通过、reject 驳回、modify 修改")
    private String actionType;

    public void setHistoryId(Long historyId) 
    {
        this.historyId = historyId;
    }

    public Long getHistoryId() 
    {
        return historyId;
    }

    public void setReviewId(Long reviewId) 
    {
        this.reviewId = reviewId;
    }

    public Long getReviewId() 
    {
        return reviewId;
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

    public void setRecordId(Long recordId) 
    {
        this.recordId = recordId;
    }

    public Long getRecordId() 
    {
        return recordId;
    }

    public void setOldStatus(String oldStatus) 
    {
        this.oldStatus = oldStatus;
    }

    public String getOldStatus() 
    {
        return oldStatus;
    }

    public void setNewStatus(String newStatus) 
    {
        this.newStatus = newStatus;
    }

    public String getNewStatus() 
    {
        return newStatus;
    }

    public void setOldTargetTable(String oldTargetTable) 
    {
        this.oldTargetTable = oldTargetTable;
    }

    public String getOldTargetTable() 
    {
        return oldTargetTable;
    }

    public void setOldTargetColumn(String oldTargetColumn) 
    {
        this.oldTargetColumn = oldTargetColumn;
    }

    public String getOldTargetColumn() 
    {
        return oldTargetColumn;
    }

    public void setNewTargetTable(String newTargetTable) 
    {
        this.newTargetTable = newTargetTable;
    }

    public String getNewTargetTable() 
    {
        return newTargetTable;
    }

    public void setNewTargetColumn(String newTargetColumn) 
    {
        this.newTargetColumn = newTargetColumn;
    }

    public String getNewTargetColumn() 
    {
        return newTargetColumn;
    }

    public void setActor(String actor) 
    {
        this.actor = actor;
    }

    public String getActor() 
    {
        return actor;
    }

    public void setActionType(String actionType) 
    {
        this.actionType = actionType;
    }

    public String getActionType() 
    {
        return actionType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("historyId", getHistoryId())
            .append("reviewId", getReviewId())
            .append("taskId", getTaskId())
            .append("versionId", getVersionId())
            .append("recordId", getRecordId())
            .append("oldStatus", getOldStatus())
            .append("newStatus", getNewStatus())
            .append("oldTargetTable", getOldTargetTable())
            .append("oldTargetColumn", getOldTargetColumn())
            .append("newTargetTable", getNewTargetTable())
            .append("newTargetColumn", getNewTargetColumn())
            .append("actor", getActor())
            .append("actionType", getActionType())
            .append("createTime", getCreateTime())
            .append("remark", getRemark())
            .toString();
    }
}
