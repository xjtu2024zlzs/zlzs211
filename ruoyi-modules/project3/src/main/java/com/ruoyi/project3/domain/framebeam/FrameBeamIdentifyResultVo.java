package com.ruoyi.project3.domain.framebeam;

import java.util.ArrayList;
import java.util.List;

public class FrameBeamIdentifyResultVo
{
    private FrameBeamTaskVo task;
    private FrameBeamProbabilityVo probabilities;
    private FrameBeamMetricsVo metrics;
    private FrameBeamWaveformVo waveform;
    private FrameBeamStftVo stft;
    private List<FrameBeamHistoryVo> historyRows = new ArrayList<>();
    private String errorMessage;

    public FrameBeamTaskVo getTask()
    {
        return task;
    }

    public void setTask(FrameBeamTaskVo task)
    {
        this.task = task;
    }

    public FrameBeamProbabilityVo getProbabilities()
    {
        return probabilities;
    }

    public void setProbabilities(FrameBeamProbabilityVo probabilities)
    {
        this.probabilities = probabilities;
    }

    public FrameBeamMetricsVo getMetrics()
    {
        return metrics;
    }

    public void setMetrics(FrameBeamMetricsVo metrics)
    {
        this.metrics = metrics;
    }

    public FrameBeamWaveformVo getWaveform()
    {
        return waveform;
    }

    public void setWaveform(FrameBeamWaveformVo waveform)
    {
        this.waveform = waveform;
    }

    public FrameBeamStftVo getStft()
    {
        return stft;
    }

    public void setStft(FrameBeamStftVo stft)
    {
        this.stft = stft;
    }

    public List<FrameBeamHistoryVo> getHistoryRows()
    {
        return historyRows;
    }

    public void setHistoryRows(List<FrameBeamHistoryVo> historyRows)
    {
        this.historyRows = historyRows;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }
}
