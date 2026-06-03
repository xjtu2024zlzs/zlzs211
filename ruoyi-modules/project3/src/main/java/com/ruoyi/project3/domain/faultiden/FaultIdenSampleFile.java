package com.ruoyi.project3.domain.faultiden;

import java.util.Date;

public class FaultIdenSampleFile
{
    private Long id;
    private String conditionLabel;
    private String bearingCode;
    private Integer sampleNo;
    private String fileName;
    private String sourceFile;
    private Long fileSize;
    private String dataUsage;
    private String aircraftId;
    private String subsystemId;
    private String equipmentId;
    private String componentId;
    private Date createTime;

    public Long getId()
    {
        return id;
    }
    public void setId(Long id)
    {
        this.id = id;
    }
    public String getConditionLabel()
    {
        return conditionLabel;
    }
    public void setConditionLabel(String conditionLabel)
    {
        this.conditionLabel = conditionLabel;
    }
    public String getBearingCode()
    {
        return bearingCode;
    }
    public void setBearingCode(String bearingCode)
    {
        this.bearingCode = bearingCode;
    }
    public Integer getSampleNo()
    {
        return sampleNo;
    }
    public void setSampleNo(Integer sampleNo)
    {
        this.sampleNo = sampleNo;
    }
    public String getFileName()
    {
        return fileName;
    }
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }
    public String getSourceFile()
    {
        return sourceFile;
    }
    public void setSourceFile(String sourceFile)
    {
        this.sourceFile = sourceFile;
    }
    public Long getFileSize()
    {
        return fileSize;
    }
    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }
    public String getDataUsage()
    {
        return dataUsage;
    }
    public void setDataUsage(String dataUsage)
    {
        this.dataUsage = dataUsage;
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
    public Date getCreateTime()
    {
        return createTime;
    }
    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
