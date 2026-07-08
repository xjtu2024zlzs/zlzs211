package com.ruoyi.project4.domain.dto;

import java.util.List;
import java.util.Map;

public class DossierWritebackReqDTO {

    private String instanceId;
    private String bomNodeId;
    private String sourceComponent;
    private String taskName;
    private String resultTitle;
    private String resultSummary;
    private Double confidence;
    private Map<String, Object> resultData;
    private List<Map<String, Object>> evidence;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getBomNodeId() {
        return bomNodeId;
    }

    public void setBomNodeId(String bomNodeId) {
        this.bomNodeId = bomNodeId;
    }

    public String getSourceComponent() {
        return sourceComponent;
    }

    public void setSourceComponent(String sourceComponent) {
        this.sourceComponent = sourceComponent;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getResultTitle() {
        return resultTitle;
    }

    public void setResultTitle(String resultTitle) {
        this.resultTitle = resultTitle;
    }

    public String getResultSummary() {
        return resultSummary;
    }

    public void setResultSummary(String resultSummary) {
        this.resultSummary = resultSummary;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Map<String, Object> getResultData() {
        return resultData;
    }

    public void setResultData(Map<String, Object> resultData) {
        this.resultData = resultData;
    }

    public List<Map<String, Object>> getEvidence() {
        return evidence;
    }

    public void setEvidence(List<Map<String, Object>> evidence) {
        this.evidence = evidence;
    }
}