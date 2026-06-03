package com.ruoyi.topic5.domain.dto;

public class Topic4CallbackDTO
{
    private Long traceId;
    private Integer topic4Status;
    private String topic4Result;
    private String suggestedCause;
    private String riskLevel;
    private String suggestedAction;

    public Long getTraceId()
    {
        return traceId;
    }

    public void setTraceId(Long traceId)
    {
        this.traceId = traceId;
    }

    public Integer getTopic4Status()
    {
        return topic4Status;
    }

    public void setTopic4Status(Integer topic4Status)
    {
        this.topic4Status = topic4Status;
    }

    public String getTopic4Result()
    {
        return topic4Result;
    }

    public void setTopic4Result(String topic4Result)
    {
        this.topic4Result = topic4Result;
    }

    public String getSuggestedCause()
    {
        return suggestedCause;
    }

    public void setSuggestedCause(String suggestedCause)
    {
        this.suggestedCause = suggestedCause;
    }

    public String getRiskLevel()
    {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel)
    {
        this.riskLevel = riskLevel;
    }

    public String getSuggestedAction()
    {
        return suggestedAction;
    }

    public void setSuggestedAction(String suggestedAction)
    {
        this.suggestedAction = suggestedAction;
    }
}