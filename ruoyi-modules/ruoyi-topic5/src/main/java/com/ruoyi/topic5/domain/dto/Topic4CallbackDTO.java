package com.ruoyi.topic5.domain.dto;

public class Topic4CallbackDTO
{
    /** 追溯任务ID */
    private Long traceId;

    /** 课题四处理状态：0未推送，1处理中，2已完成，3失败 */
    private Long topic4Status;

    /** 故障类型 */
    private String faultType;

    /** 故障位置 */
    private String faultLocation;

    /** 原因分析 */
    private String causeAnalysis;

    /** 故障推演过程 */
    private String deductionProcess;

    /** 根因置信度 */
    private String rootConfidence;

    public Long getTraceId()
    {
        return traceId;
    }

    public void setTraceId(Long traceId)
    {
        this.traceId = traceId;
    }

    public Long getTopic4Status()
    {
        return topic4Status;
    }

    public void setTopic4Status(Long topic4Status)
    {
        this.topic4Status = topic4Status;
    }

    public String getFaultType()
    {
        return faultType;
    }

    public void setFaultType(String faultType)
    {
        this.faultType = faultType;
    }

    public String getFaultLocation()
    {
        return faultLocation;
    }

    public void setFaultLocation(String faultLocation)
    {
        this.faultLocation = faultLocation;
    }

    public String getCauseAnalysis()
    {
        return causeAnalysis;
    }

    public void setCauseAnalysis(String causeAnalysis)
    {
        this.causeAnalysis = causeAnalysis;
    }

    public String getDeductionProcess()
    {
        return deductionProcess;
    }

    public void setDeductionProcess(String deductionProcess)
    {
        this.deductionProcess = deductionProcess;
    }

    public String getRootConfidence()
    {
        return rootConfidence;
    }

    public void setRootConfidence(String rootConfidence)
    {
        this.rootConfidence = rootConfidence;
    }

}