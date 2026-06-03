package com.ruoyi.project3.domain.algorithm;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.Map;

public class DegradationTaskRequest
{
    @JsonAlias({"sourceTaskId", "source_task_id", "feature_task_id"})
    private String featureTaskId;

    @JsonAlias({"featureAnalysisTaskId", "feature_analysis_task_id", "flowTaskId", "flow_task_id"})
    private String flowTaskId;

    @JsonAlias({"degradationTaskId"})
    private String taskId;

    private Long deviceId;

    private String partCode;
    private String sourceType;
    private Long fileId;



    @JsonAlias({"featureAnalysisResult"})
    private Map<String, Object> result;

    private DegradationParams params;

    public String getFeatureTaskId()
    {
        return featureTaskId;
    }

    public void setFeatureTaskId(String featureTaskId)
    {
        this.featureTaskId = featureTaskId;
    }

    public String getFlowTaskId()
    {
        return flowTaskId;
    }

    public void setFlowTaskId(String flowTaskId)
    {
        this.flowTaskId = flowTaskId;
    }

    public String getTaskId()
    {
        return taskId;
    }
    public void setTaskId(String taskId)
    {
        this.taskId = taskId;
    }

    public Long getDeviceId()
    {
        return deviceId;
    }

    public void setDeviceId(Long deviceId)
    {
        this.deviceId = deviceId;
    }

    public String getPartCode()
    {
        return partCode;
    }



    public void setPartCode(String partCode)
    {
        this.partCode = partCode;
    }

    public String getSourceType()
    {
        return sourceType;
    }

    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;

    }

    public Long getFileId()
    {
        return fileId;
    }

    public void setFileId(Long fileId)
    {
        this.fileId = fileId;
    }

    public Map<String, Object> getResult()
    {
        return result;
    }

    public void setResult(Map<String, Object> result)
    {
        this.result = result;
    }

    public DegradationParams getParams()
    {
        return params;
    }

    public void setParams(DegradationParams params)
    {

        this.params = params;
    }

}
