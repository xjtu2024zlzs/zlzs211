package com.ruoyi.project3.config.faultiden;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "quality.fault-identify.feature")
public class FaultIdenParaProperties
{
    private String algorithmVersion = "v1.0";
    private String rawFormat = "FAULT_IDEN";
    private String signalType = "vibration";
    private String sourceType = "db_export_file";
    private String defaultChannel = "vertical";
    private Double defaultSamplingRate = 25600D;
    private Integer sampleIntervalSeconds = 60;
    private Integer pointsPerSample = 32768;
    private String unit = "m/s^2";
    private Double defaultWindowSize = 1.0D;
    private Double defaultOverlapRate = 50.0D;
    private String singleFileSource = "fault_iden_raw";
    private String zipFileSource = "fault_iden_raw_package";
    private String exportStatus = "SUCCESS";
    private String exportTableName = "fault_iden_bearing_sample";
    private String exportNote = "Spring Boot did not modify raw CSV content";
    private Map<String, Integer> channelColumns = defaultCols();

    public String getAlgorithmVersion() { return text(algorithmVersion, "v1.0"); }
    public void setAlgorithmVersion(String algorithmVersion) { this.algorithmVersion = algorithmVersion; }
    public String getRawFormat() { return text(rawFormat, "FAULT_IDEN"); }
    public void setRawFormat(String rawFormat) { this.rawFormat = rawFormat; }
    public String getSignalType() { return text(signalType, "vibration"); }
    public void setSignalType(String signalType) { this.signalType = signalType; }
    public String getSourceType() { return text(sourceType, "db_export_file"); }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getDefaultChannel() { return text(defaultChannel, "vertical"); }
    public void setDefaultChannel(String defaultChannel) { this.defaultChannel = defaultChannel; }
    public Double getDefaultSamplingRate() { return defaultSamplingRate == null ? 25600D : defaultSamplingRate; }
    public void setDefaultSamplingRate(Double defaultSamplingRate) { this.defaultSamplingRate = defaultSamplingRate; }
    public Integer getSampleIntervalSeconds() { return sampleIntervalSeconds == null ? 60 : sampleIntervalSeconds; }
    public void setSampleIntervalSeconds(Integer sampleIntervalSeconds) { this.sampleIntervalSeconds = sampleIntervalSeconds; }
    public Integer getPointsPerSample() { return pointsPerSample == null ? 32768 : pointsPerSample; }
    public void setPointsPerSample(Integer pointsPerSample) { this.pointsPerSample = pointsPerSample; }
    public String getUnit() { return text(unit, "m/s^2"); }
    public void setUnit(String unit) { this.unit = unit; }
    public Double getDefaultWindowSize() { return defaultWindowSize == null ? 1.0D : defaultWindowSize; }
    public void setDefaultWindowSize(Double defaultWindowSize) { this.defaultWindowSize = defaultWindowSize; }
    public Double getDefaultOverlapRate() { return defaultOverlapRate == null ? 50.0D : defaultOverlapRate; }
    public void setDefaultOverlapRate(Double defaultOverlapRate) { this.defaultOverlapRate = defaultOverlapRate; }
    public String getSingleFileSource() { return text(singleFileSource, "fault_iden_raw"); }
    public void setSingleFileSource(String singleFileSource) { this.singleFileSource = singleFileSource; }
    public String getZipFileSource() { return text(zipFileSource, "fault_iden_raw_package"); }
    public void setZipFileSource(String zipFileSource) { this.zipFileSource = zipFileSource; }
    public String getExportStatus() { return text(exportStatus, "SUCCESS"); }
    public void setExportStatus(String exportStatus) { this.exportStatus = exportStatus; }
    public String getExportTableName() { return text(exportTableName, "fault_iden_bearing_sample"); }
    public void setExportTableName(String exportTableName) { this.exportTableName = exportTableName; }
    public String getExportNote() { return text(exportNote, "Spring Boot did not modify raw CSV content"); }
    public void setExportNote(String exportNote) { this.exportNote = exportNote; }

    public Map<String, Integer> getChannelColumns()
    {
        return channelColumns == null || channelColumns.isEmpty() ? defaultCols() : channelColumns;
    }

    public void setChannelColumns(Map<String, Integer> channelColumns)
    {
        this.channelColumns = channelColumns;
    }

    private String text(String value, String fallback)
    {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static Map<String, Integer> defaultCols()
    {
        Map<String, Integer> cols = new LinkedHashMap<>();
        cols.put("horizontal", 0);
        cols.put("vertical", 1);
        return cols;
    }
}
