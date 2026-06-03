package com.ruoyi.project3.domain.algorithm.feature;

import java.util.List;

public class FeatureReq
{
    private String filePath;
    private String fileUrl;
    private List<String> filePaths;
    private List<String> fileUrls;
    private String fileMode;
    private List<Long> sampleIds;
    private String batchPath;
    private String datasetId;
    private Double samplingFrequency;
    private Integer columnIndex;
    private Double windowSize;
    private Double overlapPercent;
    private Boolean removeOutliers;
    private Double maxSeconds;
    private String authCode;
    private String requestId;
    private String taskId;
    private String algorithmVersion;
    private String requestTime;
    private String aircraftId;
    private String aircraftCode;
    private String targetType;
    private String targetId;
    private String targetName;
    private HierarchyContext hierarchyContext;
    private String signalType;
    private String channel;
    private String sourceType;
    private Double samplingRate;
    private String collectStartTime;
    private String collectEndTime;
    private Double analysisStartTime;
    private Double analysisEndTime;
    private DbExportInfo dbExportInfo;
    private FeatureFileInfo fileInfo;
    private FeatureParams params;

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

    public List<String> getFilePaths()
    {
        return filePaths;
    }
    public void setFilePaths(List<String> filePaths)
    {
        this.filePaths = filePaths;
    }

    public List<String> getFileUrls()
    {
        return fileUrls;
    }
    public void setFileUrls(List<String> fileUrls)
    {
        this.fileUrls = fileUrls;
    }

    public String getFileMode()
    {
        return fileMode;
    }
    public void setFileMode(String fileMode)
    {
        this.fileMode = fileMode;
    }

    public List<Long> getSampleIds()
    {
        return sampleIds;
    }
    public void setSampleIds(List<Long> sampleIds)
    {
        this.sampleIds = sampleIds;
    }

    public String getBatchPath()
    {
        return batchPath;
    }
    public void setBatchPath(String batchPath)
    {
        this.batchPath = batchPath;
    }

    public String getDatasetId()
    {
        return datasetId;
    }
    public void setDatasetId(String datasetId)
    {
        this.datasetId = datasetId;
    }

    public Double getSamplingFrequency()
    {
        return samplingFrequency;
    }
    public void setSamplingFrequency(Double samplingFrequency)
    {
        this.samplingFrequency = samplingFrequency;
    }

    public Integer getColumnIndex()
    {
        return columnIndex;
    }
    public void setColumnIndex(Integer columnIndex)
    {
        this.columnIndex = columnIndex;
    }

    public Double getWindowSize()
    {
        return windowSize;
    }
    public void setWindowSize(Double windowSize)
    {
        this.windowSize = windowSize;
    }

    public Double getOverlapPercent()
    {
        return overlapPercent;
    }
    public void setOverlapPercent(Double overlapPercent)
    {
        this.overlapPercent = overlapPercent;
    }

    public Boolean getRemoveOutliers()
    {
        return removeOutliers;
    }
    public void setRemoveOutliers(Boolean removeOutliers)
    {
        this.removeOutliers = removeOutliers;
    }

    public Double getMaxSeconds()
    {
        return maxSeconds;
    }
    public void setMaxSeconds(Double maxSeconds)
    {
        this.maxSeconds = maxSeconds;
    }

    public String getAuthCode()
    {
        return authCode;
    }
    public void setAuthCode(String authCode)
    {
        this.authCode = authCode;
    }

    public String getRequestId()
    {
        return requestId;
    }
    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
    }

    public String getTaskId()
    {
        return taskId;
    }
    public void setTaskId(String taskId)
    {
        this.taskId = taskId;
    }

    public String getAlgorithmVersion()
    {
        return algorithmVersion;
    }
    public void setAlgorithmVersion(String algorithmVersion)
    {
        this.algorithmVersion = algorithmVersion;
    }

    public String getRequestTime()
    {
        return requestTime;
    }
    public void setRequestTime(String requestTime)
    {
        this.requestTime = requestTime;
    }

    public String getAircraftId()
    {
        return aircraftId;
    }
    public void setAircraftId(String aircraftId)
    {
        this.aircraftId = aircraftId;
    }

    public String getAircraftCode()
    {
        return aircraftCode;
    }
    public void setAircraftCode(String aircraftCode)
    {
        this.aircraftCode = aircraftCode;
    }

    public String getTargetType()
    {
        return targetType;
    }
    public void setTargetType(String targetType)
    {
        this.targetType = targetType;
    }

    public String getTargetId()
    {
        return targetId;
    }
    public void setTargetId(String targetId)
    {
        this.targetId = targetId;
    }

    public String getTargetName()
    {
        return targetName;
    }
    public void setTargetName(String targetName)
    {
        this.targetName = targetName;
    }

    public HierarchyContext getHierarchyContext()
    {
        return hierarchyContext;
    }
    public void setHierarchyContext(HierarchyContext hierarchyContext)
    {
        this.hierarchyContext = hierarchyContext;
    }

    public String getSignalType()
    {
        return signalType;
    }
    public void setSignalType(String signalType)
    {
        this.signalType = signalType;
    }

    public String getChannel()
    {
        return channel;
    }
    public void setChannel(String channel)
    {
        this.channel = channel;
    }

    public String getSourceType()
    {
        return sourceType;
    }
    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;
    }

    public Double getSamplingRate()
    {
        return samplingRate;
    }
    public void setSamplingRate(Double samplingRate)
    {
        this.samplingRate = samplingRate;
    }

    public String getCollectStartTime()
    {
        return collectStartTime;
    }
    public void setCollectStartTime(String collectStartTime)
    {
        this.collectStartTime = collectStartTime;
    }

    public String getCollectEndTime()
    {
        return collectEndTime;
    }
    public void setCollectEndTime(String collectEndTime)
    {
        this.collectEndTime = collectEndTime;
    }

    public Double getAnalysisStartTime()
    {
        return analysisStartTime;
    }
    public void setAnalysisStartTime(Double analysisStartTime)
    {
        this.analysisStartTime = analysisStartTime;
    }

    public Double getAnalysisEndTime()
    {
        return analysisEndTime;
    }
    public void setAnalysisEndTime(Double analysisEndTime)
    {
        this.analysisEndTime = analysisEndTime;
    }

    public DbExportInfo getDbExportInfo()
    {
        return dbExportInfo;
    }
    public void setDbExportInfo(DbExportInfo dbExportInfo)
    {
        this.dbExportInfo = dbExportInfo;
    }

    public FeatureFileInfo getFileInfo()
    {
        return fileInfo;
    }
    public void setFileInfo(FeatureFileInfo fileInfo)
    {
        this.fileInfo = fileInfo;
    }

    public FeatureParams getParams()
    {
        return params;
    }
    public void setParams(FeatureParams params)
    {
        this.params = params;
    }
}
