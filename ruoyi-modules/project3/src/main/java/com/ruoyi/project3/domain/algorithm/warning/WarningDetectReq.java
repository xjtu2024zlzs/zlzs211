package com.ruoyi.project3.domain.algorithm.warning;

import java.util.Map;

public class WarningDetectReq
{
    private String authCode;
    private String requestId;
    private String taskId;
    private String requestTime;
    private String targetType;
    private String targetId;
    private String targetName;
    private String partId;
    private String processId;
    private String processName;
    private Integer featureCol;
    private Map<String, Object> hierarchyContext;
    private Map<String, Object> fileInfo;
    private Map<String, Object> trainFileInfo;
    private Map<String, Object> detectFileInfo;
    private String boschTrainNumericPath;
    private String station;
    private String featureColName;
    private String selectionMode;
    private Integer maxRows;
    private Integer minFailedObserved;
    private Integer minObserved;
    private Integer epochs;
    private Double alphaSample;
    private Double ewmaLambda;
    private Integer seed;
    private String device;

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
    public String getRequestTime()
    {
        return requestTime;
    }
    public void setRequestTime(String requestTime)
    {
        this.requestTime = requestTime;
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
    public String getPartId()
    {
        return partId;
    }
    public void setPartId(String partId)
    {
        this.partId = partId;
    }
    public String getProcessId()
    {
        return processId;
    }
    public void setProcessId(String processId)
    {
        this.processId = processId;
    }
    public String getProcessName()
    {
        return processName;
    }
    public void setProcessName(String processName)
    {
        this.processName = processName;
    }
    public Integer getFeatureCol()
    {
        return featureCol;
    }
    public void setFeatureCol(Integer featureCol)
    {
        this.featureCol = featureCol;
    }
    public Map<String, Object> getHierarchyContext()
    {
        return hierarchyContext;
    }
    public void setHierarchyContext(Map<String, Object> hierarchyContext)
    {
        this.hierarchyContext = hierarchyContext;
    }
    public Map<String, Object> getFileInfo()
    {
        return fileInfo;
    }
    public void setFileInfo(Map<String, Object> fileInfo)
    {
        this.fileInfo = fileInfo;
    }
    public Map<String, Object> getTrainFileInfo()
    {
        return trainFileInfo;
    }
    public void setTrainFileInfo(Map<String, Object> trainFileInfo)
    {
        this.trainFileInfo = trainFileInfo;
    }
    public Map<String, Object> getDetectFileInfo()
    {
        return detectFileInfo;
    }
    public void setDetectFileInfo(Map<String, Object> detectFileInfo)
    {
        this.detectFileInfo = detectFileInfo;
    }
    public String getBoschTrainNumericPath()
    {
        return boschTrainNumericPath;
    }
    public void setBoschTrainNumericPath(String boschTrainNumericPath)
    {
        this.boschTrainNumericPath = boschTrainNumericPath;
    }
    public String getStation()
    {
        return station;
    }
    public void setStation(String station)
    {
        this.station = station;
    }
    public String getFeatureColName()
    {
        return featureColName;
    }
    public void setFeatureColName(String featureColName)
    {
        this.featureColName = featureColName;
    }
    public String getSelectionMode()
    {
        return selectionMode;
    }
    public void setSelectionMode(String selectionMode)
    {
        this.selectionMode = selectionMode;
    }
    public Integer getMaxRows()
    {
        return maxRows;
    }
    public void setMaxRows(Integer maxRows)
    {
        this.maxRows = maxRows;
    }
    public Integer getMinFailedObserved()
    {
        return minFailedObserved;
    }
    public void setMinFailedObserved(Integer minFailedObserved)
    {
        this.minFailedObserved = minFailedObserved;
    }
    public Integer getMinObserved()
    {
        return minObserved;
    }
    public void setMinObserved(Integer minObserved)
    {
        this.minObserved = minObserved;
    }
    public Integer getEpochs()
    {
        return epochs;
    }
    public void setEpochs(Integer epochs)
    {
        this.epochs = epochs;
    }
    public Double getAlphaSample()
    {
        return alphaSample;
    }
    public void setAlphaSample(Double alphaSample)
    {
        this.alphaSample = alphaSample;
    }
    public Double getEwmaLambda()
    {
        return ewmaLambda;
    }
    public void setEwmaLambda(Double ewmaLambda)
    {
        this.ewmaLambda = ewmaLambda;
    }
    public Integer getSeed()
    {
        return seed;
    }
    public void setSeed(Integer seed)
    {
        this.seed = seed;
    }
    public String getDevice()
    {
        return device;
    }
    public void setDevice(String device)
    {
        this.device = device;
    }
}
