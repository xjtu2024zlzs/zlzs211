package com.ruoyi.project3.domain.algorithm;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

public class DegradationParams
{

    @JsonAlias({"windowSize"})
    private Double window;

    @JsonAlias({"overlapRate"})
    private Integer overlap;

    @JsonAlias({"baselineWindowCount"})
    private Integer baseline;

    @JsonAlias({"rmsSensitivity", "rms_sensitivity"})
    private Double rmsSensitivity;

    @JsonAlias({"detectionMethods", "detection_methods"})
    private List<String> detectionMethods;


    public Double getWindow()
    {
        return window;
    }

    public void setWindow(Double window)
    {
        this.window = window;
    }
    public Integer getOverlap()
    {
        return overlap;
    }

    public void setOverlap(Integer overlap)
    {
        this.overlap = overlap;
    }

    public Integer getBaseline()
    {
        return baseline;
    }
    public void setBaseline(Integer baseline)
    {
        this.baseline = baseline;
    }

    public Double getRmsSensitivity()
    {
        return rmsSensitivity;
    }

    public void setRmsSensitivity(Double rmsSensitivity)
    {
        this.rmsSensitivity = rmsSensitivity;
    }

    public List<String> getDetectionMethods()
    {
        return detectionMethods;
    }

    public void setDetectionMethods(List<String> detectionMethods)
    {
        this.detectionMethods = detectionMethods;
    }
}
