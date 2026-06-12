package com.ruoyi.project3.domain.framebeam;

import java.util.List;

public class FrameBeamIdentifyParams
{
    private String dataDir;
    private String filePath;
    private String batchPath;
    private List<String> filePaths;
    private String uploadBatchId;
    private Integer segmentLength;
    private Double overlapRate;
    private Integer sampleRate;
    private Integer batchSize;
    private Integer epochs;

    public String getDataDir()
    {
        return dataDir;
    }

    public void setDataDir(String dataDir)
    {
        this.dataDir = dataDir;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public String getBatchPath()
    {
        return batchPath;
    }

    public void setBatchPath(String batchPath)
    {
        this.batchPath = batchPath;
    }

    public List<String> getFilePaths()
    {
        return filePaths;
    }

    public void setFilePaths(List<String> filePaths)
    {
        this.filePaths = filePaths;
    }

    public String getUploadBatchId()
    {
        return uploadBatchId;
    }

    public void setUploadBatchId(String uploadBatchId)
    {
        this.uploadBatchId = uploadBatchId;
    }

    public Integer getSegmentLength()
    {
        return segmentLength;
    }

    public void setSegmentLength(Integer segmentLength)
    {
        this.segmentLength = segmentLength;
    }

    public Double getOverlapRate()
    {
        return overlapRate;
    }

    public void setOverlapRate(Double overlapRate)
    {
        this.overlapRate = overlapRate;
    }

    public Integer getSampleRate()
    {
        return sampleRate;
    }

    public void setSampleRate(Integer sampleRate)
    {
        this.sampleRate = sampleRate;
    }

    public Integer getBatchSize()
    {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize)
    {
        this.batchSize = batchSize;
    }

    public Integer getEpochs()
    {
        return epochs;
    }

    public void setEpochs(Integer epochs)
    {
        this.epochs = epochs;
    }
}
