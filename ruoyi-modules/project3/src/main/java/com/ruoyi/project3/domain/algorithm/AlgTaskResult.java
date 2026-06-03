package com.ruoyi.project3.domain.algorithm;

import com.ruoyi.common.core.web.domain.BaseEntity;

import java.util.Date;

public class AlgTaskResult extends BaseEntity
{

    private Long id;
    private String taskId;
    private String taskType;
    private String taskName;
    private String status;
    private String requestId;

    private String algVersion;


    private String aircraftId;
    private String subsystemId;
    private String equipmentId;
    private String componentId;
    private String bizId;
    private String bizLevel;
    private String bizName;
    private String featTaskId;

    private String summary;
    private String resultVal;
    private String resultUnit;
    private String reqJson;
    private String resJson;
    private String errMsg;
    private Date startAt;
    private Date endAt;



    public Long getId()
    {
        return id;
    }
    public void setId(Long id)
    {
        this.id = id;
    }

    public String getTaskId()
    {
        return taskId;
    }
    public void setTaskId(String taskId)
    {
        this.taskId = taskId;
    }

    public String getTaskType()
    {
        return taskType;
    }
    public void setTaskType(String taskType)
    {
        this.taskType = taskType;
    }

    public String getTaskName()
    {
        return taskName;
    }
    public void setTaskName(String taskName)
    {
        this.taskName = taskName;
    }

    public String getStatus()
    {
        return status;
    }
    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getRequestId()
    {
        return requestId;
    }
    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
    }

    public String getAlgVersion()
    {
        return algVersion;
    }
    public void setAlgVersion(String algVersion)
    {
        this.algVersion = algVersion;
    }

    public String getAircraftId()
    {
        return aircraftId;
    }
    public void setAircraftId(String aircraftId)
    {
        this.aircraftId = aircraftId;
    }

    public String getSubsystemId()
    {
        return subsystemId;
    }
    public void setSubsystemId(String subsystemId)
    {
        this.subsystemId = subsystemId;
    }

    public String getEquipmentId()
    {
        return equipmentId;
    }
    public void setEquipmentId(String equipmentId)
    {
        this.equipmentId = equipmentId;
    }

    public String getComponentId()
    {
        return componentId;
    }
    public void setComponentId(String componentId)
    {
        this.componentId = componentId;
    }

    public String getBizId()
    {
        return bizId;
    }
    public void setBizId(String bizId)
    {
        this.bizId = bizId;
    }

    public String getBizLevel()
    {
        return bizLevel;
    }
    public void setBizLevel(String bizLevel)
    {
        this.bizLevel = bizLevel;
    }

    public String getBizName()
    {
        return bizName;
    }
    public void setBizName(String bizName)
    {
        this.bizName = bizName;
    }

    public String getFeatTaskId()
    {
        return featTaskId;
    }
    public void setFeatTaskId(String featTaskId)
    {
        this.featTaskId = featTaskId;
    }

    public String getSummary()
    {
        return summary;
    }
    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getResultVal()
    {
        return resultVal;
    }
    public void setResultVal(String resultVal)
    {
        this.resultVal = resultVal;
    }

    public String getResultUnit()
    {
        return resultUnit;
    }
    public void setResultUnit(String resultUnit)
    {
        this.resultUnit = resultUnit;
    }

    public String getReqJson()
    {
        return reqJson;
    }
    public void setReqJson(String reqJson)
    {
        this.reqJson = reqJson;
    }

    public String getResJson()
    {
        return resJson;
    }
    public void setResJson(String resJson)
    {
        this.resJson = resJson;
    }

    public String getErrMsg()
    {
        return errMsg;
    }
    public void setErrMsg(String errMsg)
    {
        this.errMsg = errMsg;
    }

    public Date getStartAt()
    {
        return startAt;
    }
    public void setStartAt(Date startAt)
    {
        this.startAt = startAt;
    }

    public Date getEndAt()
    {
        return endAt;
    }
    public void setEndAt(Date endAt)
    {
        this.endAt = endAt;
    }
}
