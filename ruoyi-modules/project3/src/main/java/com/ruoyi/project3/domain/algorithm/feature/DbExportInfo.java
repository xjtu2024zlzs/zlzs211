package com.ruoyi.project3.domain.algorithm.feature;

import java.util.Map;

public class DbExportInfo
{
    private String tableName;
    private String exportTime;
    private Long recordCount;
    private String exportStatus;
    private String exportFileFormat;
    private Map<String, Object> queryCondition;

    public String getTableName()
    {
        return tableName;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public String getExportTime()
    {
        return exportTime;
    }

    public void setExportTime(String exportTime)
    {
        this.exportTime = exportTime;
    }

    public Long getRecordCount()
    {
        return recordCount;
    }

    public void setRecordCount(Long recordCount)
    {
        this.recordCount = recordCount;
    }

    public String getExportStatus()
    {
        return exportStatus;
    }

    public void setExportStatus(String exportStatus)
    {
        this.exportStatus = exportStatus;
    }

    public String getExportFileFormat()
    {
        return exportFileFormat;
    }

    public void setExportFileFormat(String exportFileFormat)
    {
        this.exportFileFormat = exportFileFormat;
    }

    public Map<String, Object> getQueryCondition()
    {
        return queryCondition;
    }

    public void setQueryCondition(Map<String, Object> queryCondition)
    {
        this.queryCondition = queryCondition;
    }
}
