package com.ruoyi.project3.domain.algorithm.feature;

public class FeatureFileInfo
{
    private Long fileId;
    private String fileName;
    private String fileType;
    private String filePath;
    private String fileUrl;
    private String fileSource;
    private Long fileSize;
    private String encoding;
    private String delimiter;
    private Boolean hasHeader;
    private String timeColumn;
    private String indexColumn;
    private String valueColumn;
    private String unit;

    public Long getFileId()
    {
        return fileId;
    }

    public void setFileId(Long fileId)
    {
        this.fileId = fileId;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
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

    public String getFileSource()
    {
        return fileSource;
    }

    public void setFileSource(String fileSource)
    {
        this.fileSource = fileSource;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public String getDelimiter()
    {
        return delimiter;
    }

    public void setDelimiter(String delimiter)
    {
        this.delimiter = delimiter;
    }

    public Boolean getHasHeader()
    {
        return hasHeader;
    }

    public void setHasHeader(Boolean hasHeader)
    {
        this.hasHeader = hasHeader;
    }

    public String getTimeColumn()
    {
        return timeColumn;
    }

    public void setTimeColumn(String timeColumn)
    {
        this.timeColumn = timeColumn;
    }

    public String getIndexColumn()
    {
        return indexColumn;
    }

    public void setIndexColumn(String indexColumn)
    {
        this.indexColumn = indexColumn;
    }

    public String getValueColumn()
    {
        return valueColumn;
    }

    public void setValueColumn(String valueColumn)
    {
        this.valueColumn = valueColumn;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }
}
