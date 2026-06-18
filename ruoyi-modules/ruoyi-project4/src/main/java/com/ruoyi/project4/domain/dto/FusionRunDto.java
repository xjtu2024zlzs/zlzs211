package com.ruoyi.project4.domain.dto;

public class FusionRunDto
{
    private String pipelineId;
    private String pipelineCode;

    private Long augmentId;
    private String augmentCode;

    private Long rawSampleId;
    private Long sampleId;
    private String sampleCode;

    private String augmentOutputPath;
    private String fusionMethod;
    private Integer outputDim;

    public String getPipelineId()
    {
        return pipelineId;
    }

    public void setPipelineId(String pipelineId)
    {
        this.pipelineId = pipelineId;
    }

    public String getPipelineCode()
    {
        return pipelineCode;
    }

    public void setPipelineCode(String pipelineCode)
    {
        this.pipelineCode = pipelineCode;
    }

    public Long getAugmentId()
    {
        return augmentId;
    }

    public void setAugmentId(Long augmentId)
    {
        this.augmentId = augmentId;
    }

    public String getAugmentCode()
    {
        return augmentCode;
    }

    public void setAugmentCode(String augmentCode)
    {
        this.augmentCode = augmentCode;
    }

    public Long getRawSampleId()
    {
        return rawSampleId;
    }

    public void setRawSampleId(Long rawSampleId)
    {
        this.rawSampleId = rawSampleId;
    }

    public Long getSampleId()
    {
        return sampleId;
    }

    public void setSampleId(Long sampleId)
    {
        this.sampleId = sampleId;
    }

    public String getSampleCode()
    {
        return sampleCode;
    }

    public void setSampleCode(String sampleCode)
    {
        this.sampleCode = sampleCode;
    }

    public String getAugmentOutputPath()
    {
        return augmentOutputPath;
    }

    public void setAugmentOutputPath(String augmentOutputPath)
    {
        this.augmentOutputPath = augmentOutputPath;
    }

    public String getFusionMethod()
    {
        return fusionMethod;
    }

    public void setFusionMethod(String fusionMethod)
    {
        this.fusionMethod = fusionMethod;
    }

    public Integer getOutputDim()
    {
        return outputDim;
    }

    public void setOutputDim(Integer outputDim)
    {
        this.outputDim = outputDim;
    }
}