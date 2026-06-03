package com.ruoyi.project3.domain.algorithm.predict;

import java.util.Map;

public class PredictReq
{
    private String authCode;
    private String requestId;
    private String taskId;
    private String requestTime;
    private String aircraftId;
    private String targetType;
    private String targetId;
    private String targetName;
    private Map<String, Object> hierarchyContext;
    private Map<String, Object> fileInfo;

    public String getAuthCode()
    {
        return authCode;
    }

    public void setAuthCode(String authCode)
    {
        this.authCode = authCode;
    }

    public String getRequestId()
    {
        return requestId;
    }

    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
    }

    public String getTaskId()
    {
        return taskId;
    }

    public void setTaskId(String taskId)
    {
        this.taskId = taskId;
    }

    public String getRequestTime()
    {
        return requestTime;
    }

    public void setRequestTime(String requestTime)
    {
        this.requestTime = requestTime;
    }

    public String getAircraftId()
    {
        return aircraftId;
    }

    public void setAircraftId(String aircraftId)
    {
        this.aircraftId = aircraftId;
    }

    public String getTargetType()
    {
        return targetType;
    }

    public void setTargetType(String targetType)
    {
        this.targetType = targetType;
    }

    public String getTargetId()
    {
        return targetId;
    }

    public void setTargetId(String targetId)
    {
        this.targetId = targetId;
    }

    public String getTargetName()
    {
        return targetName;
    }

    public void setTargetName(String targetName)
    {
        this.targetName = targetName;
    }

    public Map<String, Object> getHierarchyContext()
    {
        return hierarchyContext;
    }

    public void setHierarchyContext(Map<String, Object> hierarchyContext)
    {
        this.hierarchyContext = hierarchyContext;
    }

    public Map<String, Object> getFileInfo()
    {
        return fileInfo;
    }

    public void setFileInfo(Map<String, Object> fileInfo)
    {
        this.fileInfo = fileInfo;
    }

}
