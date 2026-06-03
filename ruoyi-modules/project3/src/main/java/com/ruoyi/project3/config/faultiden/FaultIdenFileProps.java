package com.ruoyi.project3.config.faultiden;

import com.ruoyi.common.core.utils.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "quality.fault-identify")
public class FaultIdenFileProps
{
    private String sourceRoot;
    private String exportDir;
    private String sourceFileUrlPrefix;
    private String predictSourceFileUrlPrefix;
    private String warningSourceFileUrlPrefix;
    private String exportFileUrlPrefix;
    private String defaultStartTime = "2026-01-01 00:00:00";
    private Integer maxPackageSamples = 20;

    public String getSourceRoot()
    {
        return trim(sourceRoot);
    }
    public void setSourceRoot(String sourceRoot)
    {
        this.sourceRoot = sourceRoot;
    }
    public String getExportDir()
    {
        return trim(exportDir);
    }
    public void setExportDir(String exportDir)
    {
        this.exportDir = exportDir;
    }
    public String getSourceFileUrlPrefix()
    {
        return trim(sourceFileUrlPrefix);
    }
    public void setSourceFileUrlPrefix(String sourceFileUrlPrefix)
    {
        this.sourceFileUrlPrefix = sourceFileUrlPrefix;
    }
    public String getPredictSourceFileUrlPrefix()
    {
        String value = trim(predictSourceFileUrlPrefix);
        if (!StringUtils.isEmpty(value))
        {
            return value;
        }
        String sourcePrefix = getSourceFileUrlPrefix();
        return StringUtils.isEmpty(sourcePrefix) ? null : sourcePrefix.replace("/source-file", "/predict-source-file");
    }
    public void setPredictSourceFileUrlPrefix(String predictSourceFileUrlPrefix)
    {
        this.predictSourceFileUrlPrefix = predictSourceFileUrlPrefix;
    }
    public String getWarningSourceFileUrlPrefix()
    {
        String value = trim(warningSourceFileUrlPrefix);
        if (!StringUtils.isEmpty(value))
        {
            return value;
        }
        String sourcePrefix = getSourceFileUrlPrefix();
        return StringUtils.isEmpty(sourcePrefix) ? null : sourcePrefix.replace("/source-file", "/warning-source-file");
    }
    public void setWarningSourceFileUrlPrefix(String warningSourceFileUrlPrefix)
    {
        this.warningSourceFileUrlPrefix = warningSourceFileUrlPrefix;
    }
    public String getExportFileUrlPrefix()
    {
        return trim(exportFileUrlPrefix);
    }
    public void setExportFileUrlPrefix(String exportFileUrlPrefix)
    {
        this.exportFileUrlPrefix = exportFileUrlPrefix;
    }
    public String getDefaultStartTime()
    {
        return StringUtils.isEmpty(defaultStartTime) ? "2026-01-01 00:00:00" : defaultStartTime;
    }
    public void setDefaultStartTime(String defaultStartTime)
    {
        this.defaultStartTime = defaultStartTime;
    }
    public Integer getMaxPackageSamples()
    {
        return maxPackageSamples == null || maxPackageSamples <= 0 ? 20 : maxPackageSamples;
    }
    public void setMaxPackageSamples(Integer maxPackageSamples)
    {
        this.maxPackageSamples = maxPackageSamples;
    }
    private String trim(String value)
    {
        return value == null ? null : value.trim();
    }
}
