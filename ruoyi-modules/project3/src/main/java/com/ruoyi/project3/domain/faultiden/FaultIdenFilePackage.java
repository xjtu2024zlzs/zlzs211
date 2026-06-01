package com.ruoyi.project3.domain.faultiden;

import java.util.Date;

public class FaultIdenFilePackage
{
    private Long id;
    private String taskId;
    private String fileMode;
    private String fileType;
    private String fileName;
    private String filePath;
    private String fileUrl;
    private String selectedSampleIds;
    private String dataUsage;
    private Date createTime;

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
    public String getFileMode()
    {
        return fileMode;
    }
    public void setFileMode(String fileMode)
    {
        this.fileMode = fileMode;
    }
    public String getFileType()
    {
        return fileType;
    }
    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }
    public String getFileName()
    {
        return fileName;
    }
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }
    public String getFilePath()
    {
        return filePath;
    }
    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }
    public String getFileUrl()
    {
        return fileUrl;
    }
    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
    }
    public String getSelectedSampleIds()
    {
        return selectedSampleIds;
    }
    public void setSelectedSampleIds(String selectedSampleIds)
    {
        this.selectedSampleIds = selectedSampleIds;
    }
    public String getDataUsage()
    {
        return dataUsage;
    }
    public void setDataUsage(String dataUsage)
    {
        this.dataUsage = dataUsage;
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
