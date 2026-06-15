package com.ruoyi.designtask1.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.web.domain.BaseEntity;

public class DesignTaskLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long logId;
    private Long taskId;
    private Long nodeId;
    private String action;
    private String content;
    private Long operatorId;
    private String operatorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getNodeId() { return nodeId; }
    public void setNodeId(Long nodeId) { this.nodeId = nodeId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("logId", getLogId())
            .append("taskId", getTaskId())
            .append("action", getAction())
            .append("operatorName", getOperatorName())
            .toString();
    }
}