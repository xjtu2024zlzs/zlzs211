package com.ruoyi.system.domain.dto;

public class PreprocessRunDto
{
    private String pipelineId;
    private String pipelineCode;

    private Long datasetId;
    private Long fileId;
    private Long equipmentId;
    private Long sensorId;

    private String datasetCode;
    private String filePath;
    private Integer keyNum;
    private Integer windowSize;
    private Integer stride;
    private Integer maxWindows;

    private Integer label;
    private String faultType;
    private String samplePrefix;

    public String getPipelineId() { return pipelineId; }
    public void setPipelineId(String pipelineId) { this.pipelineId = pipelineId; }

    public String getPipelineCode() { return pipelineCode; }
    public void setPipelineCode(String pipelineCode) { this.pipelineCode = pipelineCode; }

    public Long getDatasetId() { return datasetId; }
    public void setDatasetId(Long datasetId) { this.datasetId = datasetId; }

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }

    public Long getSensorId() { return sensorId; }
    public void setSensorId(Long sensorId) { this.sensorId = sensorId; }

    public String getDatasetCode() { return datasetCode; }
    public void setDatasetCode(String datasetCode) { this.datasetCode = datasetCode; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Integer getKeyNum() { return keyNum; }
    public void setKeyNum(Integer keyNum) { this.keyNum = keyNum; }

    public Integer getWindowSize() { return windowSize; }
    public void setWindowSize(Integer windowSize) { this.windowSize = windowSize; }

    public Integer getStride() { return stride; }
    public void setStride(Integer stride) { this.stride = stride; }

    public Integer getMaxWindows() { return maxWindows; }
    public void setMaxWindows(Integer maxWindows) { this.maxWindows = maxWindows; }

    public Integer getLabel() { return label; }
    public void setLabel(Integer label) { this.label = label; }

    public String getFaultType() { return faultType; }
    public void setFaultType(String faultType) { this.faultType = faultType; }

    public String getSamplePrefix() { return samplePrefix; }
    public void setSamplePrefix(String samplePrefix) { this.samplePrefix = samplePrefix; }
}