package com.ruoyi.designtask1.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.web.domain.BaseEntity;

public class DesignTask extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long taskId;
    private String taskName;
    private String taskNo;
    private String taskType;
    private Long templateId;
    private Integer priority;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date plannedStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date plannedEndTime;
    private String status;
    private String responsibleRole;
    private String collabRoles;
    private String processDefinitionId;
    private String processInstanceId;
    private String currentFlowableTaskId;
    private String currentNodeKey;
    private String currentNodeName;
    private Long ownerUserId;
    private String ownerUserName;
    private Long structureUserId;
    private Long layoutUserId;
    private Long aeroUserId;
    private Long hydraulicUserId;
    private Long manufacturingUserId;
    private Long designUserId;
    private Long leaderUserId;
    private String delFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getTaskNo() { return taskNo; }
    public void setTaskNo(String taskNo) { this.taskNo = taskNo; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Date getPlannedStartTime() { return plannedStartTime; }
    public void setPlannedStartTime(Date plannedStartTime) { this.plannedStartTime = plannedStartTime; }
    public Date getPlannedEndTime() { return plannedEndTime; }
    public void setPlannedEndTime(Date plannedEndTime) { this.plannedEndTime = plannedEndTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResponsibleRole() { return responsibleRole; }
    public void setResponsibleRole(String responsibleRole) { this.responsibleRole = responsibleRole; }
    public String getCollabRoles() { return collabRoles; }
    public void setCollabRoles(String collabRoles) { this.collabRoles = collabRoles; }
    public String getProcessDefinitionId() { return processDefinitionId; }
    public void setProcessDefinitionId(String processDefinitionId) { this.processDefinitionId = processDefinitionId; }
    public String getProcessInstanceId() { return processInstanceId; }
    public void setProcessInstanceId(String processInstanceId) { this.processInstanceId = processInstanceId; }
    public String getCurrentFlowableTaskId() { return currentFlowableTaskId; }
    public void setCurrentFlowableTaskId(String currentFlowableTaskId) { this.currentFlowableTaskId = currentFlowableTaskId; }
    public String getCurrentNodeKey() { return currentNodeKey; }
    public void setCurrentNodeKey(String currentNodeKey) { this.currentNodeKey = currentNodeKey; }
    public String getCurrentNodeName() { return currentNodeName; }
    public void setCurrentNodeName(String currentNodeName) { this.currentNodeName = currentNodeName; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getOwnerUserName() { return ownerUserName; }
    public void setOwnerUserName(String ownerUserName) { this.ownerUserName = ownerUserName; }
    public Long getStructureUserId() { return structureUserId; }
    public void setStructureUserId(Long structureUserId) { this.structureUserId = structureUserId; }
    public Long getLayoutUserId() { return layoutUserId; }
    public void setLayoutUserId(Long layoutUserId) { this.layoutUserId = layoutUserId; }
    public Long getAeroUserId() { return aeroUserId; }
    public void setAeroUserId(Long aeroUserId) { this.aeroUserId = aeroUserId; }
    public Long getHydraulicUserId() { return hydraulicUserId; }
    public void setHydraulicUserId(Long hydraulicUserId) { this.hydraulicUserId = hydraulicUserId; }
    public Long getManufacturingUserId() { return manufacturingUserId; }
    public void setManufacturingUserId(Long manufacturingUserId) { this.manufacturingUserId = manufacturingUserId; }
    public Long getDesignUserId() { return designUserId; }
    public void setDesignUserId(Long designUserId) { this.designUserId = designUserId; }
    public Long getLeaderUserId() { return leaderUserId; }
    public void setLeaderUserId(Long leaderUserId) { this.leaderUserId = leaderUserId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("taskId", getTaskId())
            .append("taskName", getTaskName())
            .append("taskNo", getTaskNo())
            .append("taskType", getTaskType())
            .append("templateId", getTemplateId())
            .append("priority", getPriority())
            .append("description", getDescription())
            .append("plannedStartTime", getPlannedStartTime())
            .append("plannedEndTime", getPlannedEndTime())
            .append("status", getStatus())
            .append("responsibleRole", getResponsibleRole())
            .append("collabRoles", getCollabRoles())
            .append("processDefinitionId", getProcessDefinitionId())
            .append("processInstanceId", getProcessInstanceId())
            .append("currentFlowableTaskId", getCurrentFlowableTaskId())
            .append("currentNodeKey", getCurrentNodeKey())
            .append("currentNodeName", getCurrentNodeName())
            .append("ownerUserId", getOwnerUserId())
            .append("ownerUserName", getOwnerUserName())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
