package com.ruoyi.project3.domain.framebeam;

public class FrameBeamMetricsVo
{
    private Double accuracy;
    private Double earlyCrackAccuracy;
    private Double weightedF1;
    private Double testLoss;

    public Double getAccuracy()
    {
        return accuracy;
    }

    public void setAccuracy(Double accuracy)
    {
        this.accuracy = accuracy;
    }

    public Double getEarlyCrackAccuracy()
    {
        return earlyCrackAccuracy;
    }

    public void setEarlyCrackAccuracy(Double earlyCrackAccuracy)
    {
        this.earlyCrackAccuracy = earlyCrackAccuracy;
    }

    public Double getWeightedF1()
    {
        return weightedF1;
    }

    public void setWeightedF1(Double weightedF1)
    {
        this.weightedF1 = weightedF1;
    }

    public Double getTestLoss()
    {
        return testLoss;
    }

    public void setTestLoss(Double testLoss)
    {
        this.testLoss = testLoss;
    }
}
