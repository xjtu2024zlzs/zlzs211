package com.ruoyi.quality.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 质量问题流程日志对象 qms_flow_log
 * 
 * @author ruoyi
 * @date 2026-06-13
 */
public class QmsFlowLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 质量问题ID */
    @Excel(name = "质量问题ID")
    private Long problemId;

    /** 质量问题编号 */
    @Excel(name = "质量问题编号")
    private String problemCode;

    /** 任务ID */
    @Excel(name = "任务ID")
    private Long taskId;

    /** 动作类型 */
    @Excel(name = "动作类型")
    private String actionType;

    /** 动作名称 */
    @Excel(name = "动作名称")
    private String actionName;

    /** 操作人ID */
    @Excel(name = "操作人ID")
    private Long operatorId;

    /** 操作人姓名 */
    @Excel(name = "操作人姓名")
    private String operatorName;

    /** 原状态 */
    @Excel(name = "原状态")
    private String fromStatus;

    /** 新状态 */
    @Excel(name = "新状态")
    private String toStatus;

    /** 操作内容 */
    @Excel(name = "操作内容")
    private String actionContent;

    public void setLogId(Long logId) 
    {
        this.logId = logId;
    }

    public Long getLogId() 
    {
        return logId;
    }

    public void setProblemId(Long problemId) 
    {
        this.problemId = problemId;
    }

    public Long getProblemId() 
    {
        return problemId;
    }

    public void setProblemCode(String problemCode) 
    {
        this.problemCode = problemCode;
    }

    public String getProblemCode() 
    {
        return problemCode;
    }

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }

    public void setActionType(String actionType) 
    {
        this.actionType = actionType;
    }

    public String getActionType() 
    {
        return actionType;
    }

    public void setActionName(String actionName) 
    {
        this.actionName = actionName;
    }

    public String getActionName() 
    {
        return actionName;
    }

    public void setOperatorId(Long operatorId) 
    {
        this.operatorId = operatorId;
    }

    public Long getOperatorId() 
    {
        return operatorId;
    }

    public void setOperatorName(String operatorName) 
    {
        this.operatorName = operatorName;
    }

    public String getOperatorName() 
    {
        return operatorName;
    }

    public void setFromStatus(String fromStatus) 
    {
        this.fromStatus = fromStatus;
    }

    public String getFromStatus() 
    {
        return fromStatus;
    }

    public void setToStatus(String toStatus) 
    {
        this.toStatus = toStatus;
    }

    public String getToStatus() 
    {
        return toStatus;
    }

    public void setActionContent(String actionContent) 
    {
        this.actionContent = actionContent;
    }

    public String getActionContent() 
    {
        return actionContent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("logId", getLogId())
            .append("problemId", getProblemId())
            .append("problemCode", getProblemCode())
            .append("taskId", getTaskId())
            .append("actionType", getActionType())
            .append("actionName", getActionName())
            .append("operatorId", getOperatorId())
            .append("operatorName", getOperatorName())
            .append("fromStatus", getFromStatus())
            .append("toStatus", getToStatus())
            .append("actionContent", getActionContent())
            .append("createTime", getCreateTime())
            .toString();
    }
}
