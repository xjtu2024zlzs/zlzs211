package com.ruoyi.project3.domain.framebeam;

public class FrameBeamHistoryVo
{
    private String taskId;
    private String objectName;
    private String inputData;
    private String result;
    private String confidence;
    private String createTime;
    private FrameBeamIdentifyResultVo apiResult;

    public String getTaskId()
    {
        return taskId;
    }

    public void setTaskId(String taskId)
    {
        this.taskId = taskId;
    }

    public String getObjectName()
    {
        return objectName;
    }

    public void setObjectName(String objectName)
    {
        this.objectName = objectName;
    }

    public String getInputData()
    {
        return inputData;
    }

    public void setInputData(String inputData)
    {
        this.inputData = inputData;
    }

    public String getResult()
    {
        return result;
    }

    public void setResult(String result)
    {
        this.result = result;
    }

    public String getConfidence()
    {
        return confidence;
    }

    public void setConfidence(String confidence)
    {
        this.confidence = confidence;
    }

    public String getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(String createTime)
    {
        this.createTime = createTime;
    }

    public FrameBeamIdentifyResultVo getApiResult()
    {
        return apiResult;
    }

    public void setApiResult(FrameBeamIdentifyResultVo apiResult)
    {
        this.apiResult = apiResult;
    }
}
