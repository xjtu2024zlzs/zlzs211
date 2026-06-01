package com.ruoyi.project3.domain.algorithm.feature;

import java.util.List;
import java.util.Map;

public class FeatureParams
{
    private Double windowSize;
    private Double overlapRate;
    private Double fftWindowSize;
    private Double fftOverlapRate;
    private String rawFormat;
    private String channel;
    private Double samplingRate;
    private Integer sampleIntervalSeconds;
    private Integer pointsPerSample;
    private Map<String, Integer> channelColumns;
    private String fileMode;
    private List<Long> selectedSampleIds;

    public Double getWindowSize()
    {
        return windowSize;
    }

    public void setWindowSize(Double windowSize)
    {
        this.windowSize = windowSize;
    }

    public Double getOverlapRate()
    {
        return overlapRate;
    }

    public void setOverlapRate(Double overlapRate)
    {
        this.overlapRate = overlapRate;
    }

    public Double getFftWindowSize()
    {
        return fftWindowSize;
    }

    public void setFftWindowSize(Double fftWindowSize)
    {
        this.fftWindowSize = fftWindowSize;
    }

    public Double getFftOverlapRate()
    {
        return fftOverlapRate;
    }

    public void setFftOverlapRate(Double fftOverlapRate)
    {
        this.fftOverlapRate = fftOverlapRate;
    }

    public String getRawFormat() { return rawFormat; }
    public void setRawFormat(String rawFormat) { this.rawFormat = rawFormat; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public Double getSamplingRate() { return samplingRate; }
    public void setSamplingRate(Double samplingRate) { this.samplingRate = samplingRate; }
    public Integer getSampleIntervalSeconds() { return sampleIntervalSeconds; }
    public void setSampleIntervalSeconds(Integer sampleIntervalSeconds) { this.sampleIntervalSeconds = sampleIntervalSeconds; }
    public Integer getPointsPerSample() { return pointsPerSample; }
    public void setPointsPerSample(Integer pointsPerSample) { this.pointsPerSample = pointsPerSample; }
    public Map<String, Integer> getChannelColumns() { return channelColumns; }
    public void setChannelColumns(Map<String, Integer> channelColumns) { this.channelColumns = channelColumns; }
    public String getFileMode() { return fileMode; }
    public void setFileMode(String fileMode) { this.fileMode = fileMode; }
    public List<Long> getSelectedSampleIds() { return selectedSampleIds; }
    public void setSelectedSampleIds(List<Long> selectedSampleIds) { this.selectedSampleIds = selectedSampleIds; }
}
