package com.ruoyi.project4.domain;

import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 故障诊断-原始数据文件对象 t4_data_file
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
public class FdDataFile extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 文件ID */
    private Long fileId;

    /** 数据集ID */
    @Excel(name = "数据集ID")
    private Long datasetId;

    /**
     * 文件编号
     */
    @Excel(name = "文件编号")
    private String fileCode;

    /** 原始文件名 */
    @Excel(name = "原始文件名")
    private String originalFileName;

    /** 文件后缀 */
    @Excel(name = "文件后缀")
    private String fileSuffix;

    /** 文件类型：mat/csv/xlsx/txt/image */
    @Excel(name = "文件类型：mat/csv/xlsx/txt/image")
    private String fileType;

    /** 文件大小，单位字节 */
    @Excel(name = "文件大小，单位字节")
    private Long fileSize;

    /** 文件存储路径 */
    @Excel(name = "文件存储路径")
    private String storagePath;

    /** 文件MD5 */
    @Excel(name = "文件MD5")
    private String fileMd5;

    /** 数据来源：CWRU/文件上传/传感器上传/维修记录/飞行日志/手工录入 */
    @Excel(name = "数据来源：CWRU/文件上传/传感器上传/维修记录/飞行日志/手工录入")
    private String sourceType;

    /** 导入状态：未导入/导入中/成功/失败 */
    @Excel(name = "导入状态：未导入/导入中/成功/失败")
    private String importStatus;

    /** 解析状态：未解析/成功/失败 */
    @Excel(name = "解析状态：未解析/成功/失败")
    private String parseStatus;

    /** 解析出的样本数量 */
    @Excel(name = "解析出的样本数量")
    private Long sampleCount;

    /** 错误信息 */
    @Excel(name = "错误信息")
    private String errorMsg;

    /** 删除标志：0存在 2删除 */
    private String delFlag;

    public void setFileId(Long fileId) 
    {
        this.fileId = fileId;
    }

    public Long getFileId() 
    {
        return fileId;
    }

    public void setDatasetId(Long datasetId) 
    {
        this.datasetId = datasetId;
    }

    public Long getDatasetId() 
    {
        return datasetId;
    }

    public void setFileCode(String fileCode) 
    {
        this.fileCode = fileCode;
    }

    public String getFileCode() 
    {
        return fileCode;
    }

    public void setOriginalFileName(String originalFileName) 
    {
        this.originalFileName = originalFileName;
    }

    public String getOriginalFileName() 
    {
        return originalFileName;
    }

    public void setFileSuffix(String fileSuffix) 
    {
        this.fileSuffix = fileSuffix;
    }

    public String getFileSuffix() 
    {
        return fileSuffix;
    }

    public void setFileType(String fileType) 
    {
        this.fileType = fileType;
    }

    public String getFileType() 
    {
        return fileType;
    }

    public void setFileSize(Long fileSize) 
    {
        this.fileSize = fileSize;
    }

    public Long getFileSize() 
    {
        return fileSize;
    }

    public void setStoragePath(String storagePath) 
    {
        this.storagePath = storagePath;
    }

    public String getStoragePath() 
    {
        return storagePath;
    }

    public void setFileMd5(String fileMd5) 
    {
        this.fileMd5 = fileMd5;
    }

    public String getFileMd5() 
    {
        return fileMd5;
    }

    public void setSourceType(String sourceType) 
    {
        this.sourceType = sourceType;
    }

    public String getSourceType() 
    {
        return sourceType;
    }

    public void setImportStatus(String importStatus) 
    {
        this.importStatus = importStatus;
    }

    public String getImportStatus() 
    {
        return importStatus;
    }

    public void setParseStatus(String parseStatus) 
    {
        this.parseStatus = parseStatus;
    }

    public String getParseStatus() 
    {
        return parseStatus;
    }

    public void setSampleCount(Long sampleCount) 
    {
        this.sampleCount = sampleCount;
    }

    public Long getSampleCount() 
    {
        return sampleCount;
    }

    public void setErrorMsg(String errorMsg) 
    {
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() 
    {
        return errorMsg;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("fileId", getFileId())
            .append("datasetId", getDatasetId())
            .append("fileCode", getFileCode())
            .append("originalFileName", getOriginalFileName())
            .append("fileSuffix", getFileSuffix())
            .append("fileType", getFileType())
            .append("fileSize", getFileSize())
            .append("storagePath", getStoragePath())
            .append("fileMd5", getFileMd5())
            .append("sourceType", getSourceType())
            .append("importStatus", getImportStatus())
            .append("parseStatus", getParseStatus())
            .append("sampleCount", getSampleCount())
            .append("errorMsg", getErrorMsg())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

