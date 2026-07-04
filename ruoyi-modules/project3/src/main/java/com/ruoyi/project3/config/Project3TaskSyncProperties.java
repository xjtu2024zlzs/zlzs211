package com.ruoyi.project3.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "project3.task-sync")
public class Project3TaskSyncProperties
{
    private boolean enabled = false;

    private long fixedDelayMs = 60000L;

    private int activeTimeoutMinutes = 10;

    private int batchSize = 20;

    private int maxRetryCount = 3;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public long getFixedDelayMs()
    {
        return fixedDelayMs;
    }

    public void setFixedDelayMs(long fixedDelayMs)
    {
        this.fixedDelayMs = fixedDelayMs;
    }

    public int getActiveTimeoutMinutes()
    {
        return activeTimeoutMinutes;
    }

    public void setActiveTimeoutMinutes(int activeTimeoutMinutes)
    {
        this.activeTimeoutMinutes = activeTimeoutMinutes;
    }

    public int getBatchSize()
    {
        return batchSize;
    }

    public void setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
    }

    public int getMaxRetryCount()
    {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount)
    {
        this.maxRetryCount = maxRetryCount;
    }
}
