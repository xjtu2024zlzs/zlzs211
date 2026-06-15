package com.ruoyi.project4.domain.dto;

public class DiagnosisRunDto
{
    private Long sampleId;

    private String sampleCode;

    private String filePath;

    private Integer keyNum;

    private Integer length;

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

    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public Integer getKeyNum()
    {
        return keyNum;
    }

    public void setKeyNum(Integer keyNum)
    {
        this.keyNum = keyNum;
    }

    public Integer getLength()
    {
        return length;
    }

    public void setLength(Integer length)
    {
        this.length = length;
    }
}