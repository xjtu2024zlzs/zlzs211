package com.ruoyi.topic5.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 追溯附件对象 topic5_trace_attachment
 * 
 * @author ruoyi
 * @date 2026-06-03
 */
public class Topic5TraceAttachment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 追溯任务ID */
    @Excel(name = "追溯任务ID")
    private Long traceId;

    /** 文件名称 */
    @Excel(name = "文件名称")
    private String fileName;

    /** 文件类型 */
    @Excel(name = "文件类型")
    private String fileType;

    /** 文件路径 */
    @Excel(name = "文件路径")
    private String fileUrl;

    /** 文件来源 */
    @Excel(name = "文件来源")
    private String fileSource;

    /** 传感器类型 */
    @Excel(name = "传感器类型")
    private String sensorType;

    /** 数据采集时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "数据采集时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date dataTime;

    /** 文件大小 */
    @Excel(name = "文件大小")
    private String fileSize;

    /** 是否已保存：0临时，1已保存 */
    @Excel(name = "是否已保存：0临时，1已保存")
    private Long savedFlag;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setTraceId(Long traceId) 
    {
        this.traceId = traceId;
    }

    public Long getTraceId() 
    {
        return traceId;
    }

    public void setFileName(String fileName) 
    {
        this.fileName = fileName;
    }

    public String getFileName() 
    {
        return fileName;
    }

    public void setFileType(String fileType) 
    {
        this.fileType = fileType;
    }

    public String getFileType() 
    {
        return fileType;
    }

    public void setFileUrl(String fileUrl) 
    {
        this.fileUrl = fileUrl;
    }

    public String getFileUrl() 
    {
        return fileUrl;
    }

    public void setFileSource(String fileSource) 
    {
        this.fileSource = fileSource;
    }

    public String getFileSource() 
    {
        return fileSource;
    }

    public void setSensorType(String sensorType) 
    {
        this.sensorType = sensorType;
    }

    public String getSensorType() 
    {
        return sensorType;
    }

    public void setDataTime(Date dataTime) 
    {
        this.dataTime = dataTime;
    }

    public Date getDataTime() 
    {
        return dataTime;
    }

    public void setFileSize(String fileSize) 
    {
        this.fileSize = fileSize;
    }

    public String getFileSize() 
    {
        return fileSize;
    }

    public void setSavedFlag(Long savedFlag) 
    {
        this.savedFlag = savedFlag;
    }

    public Long getSavedFlag() 
    {
        return savedFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("traceId", getTraceId())
            .append("fileName", getFileName())
            .append("fileType", getFileType())
            .append("fileUrl", getFileUrl())
            .append("fileSource", getFileSource())
            .append("sensorType", getSensorType())
            .append("dataTime", getDataTime())
            .append("fileSize", getFileSize())
            .append("savedFlag", getSavedFlag())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .toString();
    }
}
