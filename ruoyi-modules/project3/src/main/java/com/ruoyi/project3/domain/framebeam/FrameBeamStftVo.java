package com.ruoyi.project3.domain.framebeam;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class FrameBeamStftVo
{
    @JsonProperty("xAxis")
    private List<Integer> xAxis = new ArrayList<>();
    @JsonProperty("yAxis")
    private List<Integer> yAxis = new ArrayList<>();
    private List<List<Number>> data = new ArrayList<>();
    private Double min;
    private Double max;

    public List<Integer> getXAxis()
    {
        return xAxis;
    }

    public void setXAxis(List<Integer> xAxis)
    {
        this.xAxis = xAxis;
    }

    public List<Integer> getYAxis()
    {
        return yAxis;
    }

    public void setYAxis(List<Integer> yAxis)
    {
        this.yAxis = yAxis;
    }

    public List<List<Number>> getData()
    {
        return data;
    }

    public void setData(List<List<Number>> data)
    {
        this.data = data;
    }

    public Double getMin()
    {
        return min;
    }

    public void setMin(Double min)
    {
        this.min = min;
    }

    public Double getMax()
    {
        return max;
    }

    public void setMax(Double max)
    {
        this.max = max;
    }
}
