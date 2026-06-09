package com.ruoyi.project3.domain.framebeam;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class FrameBeamWaveformVo
{
    @JsonProperty("xAxis")
    private List<Integer> xAxis = new ArrayList<>();
    private List<Double> raw = new ArrayList<>();
    private List<Double> denoised = new ArrayList<>();

    public List<Integer> getXAxis()
    {
        return xAxis;
    }

    public void setXAxis(List<Integer> xAxis)
    {
        this.xAxis = xAxis;
    }

    public List<Double> getRaw()
    {
        return raw;
    }

    public void setRaw(List<Double> raw)
    {
        this.raw = raw;
    }

    public List<Double> getDenoised()
    {
        return denoised;
    }

    public void setDenoised(List<Double> denoised)
    {
        this.denoised = denoised;
    }
}
