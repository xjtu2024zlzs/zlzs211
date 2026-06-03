package com.ruoyi.project3.config;

import com.ruoyi.common.core.utils.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "python.algorithm")
public class PythonAlgorithmProperties
{


    private String baseUrl;
    private String keyProcessPath = "/api/v1/key-process/identify";
    private String keyProcessAuthCode = "ALGO_KEY_PROCESS_IDENTIFY_EXECUTE";
    private String keyProcessUrl;
    private String featureAnalysisPath = "/python/data-analysis";
    private String featureProcessingPath = "/python/feature-analysis";
    private String featureAnalysisAuthCode = "ALGO_FEATURE_ANALYSIS_EXECUTE";
    private String predictPath = "/algorithm/prevention/analyze";
    private String predictAuthCode = "ALGO_FAULT_PREDICT_EXECUTE";
    private String predictUrl;
    private String warningPath = "/algorithm/process-anomaly/execute";
    private String warningAuthCode = "ALGO_PROCESS_ANOMALY_EXECUTE";
    private String warningUrl;
    private String faultPath = "/algorithm/detection/early-fault";
    private String faultUrl;
    private boolean mock = false;


    public String getBaseUrl()
    {
        return StringUtils.trim(baseUrl);
    }

    public void setBaseUrl(String v)
    {
        this.baseUrl = v;
    }

    public void setKeyProcessPath(String v)
    {
        this.keyProcessPath = v;
    }

    public void setKeyProcessAuthCode(String v)
    {
        this.keyProcessAuthCode = v;
    }

    public void setKeyProcessUrl(String v)
    {
        this.keyProcessUrl = v;
    }

    public void setFeatureAnalysisPath(String v)
    {
        this.featureAnalysisPath = v;
    }

    public void setFeatureProcessingPath(String v)
    {
        this.featureProcessingPath = v;
    }

    public void setFeatureAnalysisAuthCode(String v)
    {
        this.featureAnalysisAuthCode = v;
    }

    public void setPredictPath(String v)
    {
        this.predictPath = v;
    }

    public void setPredictAuthCode(String v)
    {
        this.predictAuthCode = v;
    }

    public void setPredictUrl(String v)
    {
        this.predictUrl = v;
    }

    public void setWarningPath(String v)
    {
        this.warningPath = v;
    }

    public void setWarningAuthCode(String v)
    {
        this.warningAuthCode = v;
    }

    public void setWarningUrl(String v)
    {
        this.warningUrl = v;
    }

    public void setFaultPath(String v)
    {
        this.faultPath = v;
    }

    public void setFaultUrl(String v)
    {

        this.faultUrl = v;
    }


    public String getKeyProcessPath()
    {
        String path = StringUtils.trim(keyProcessPath);

        if (StringUtils.isEmpty(path))
        {
            return path;
        }
        return StringUtils.startsWith(path, "/") ? path : "/" + path;
    }

    public String getKeyProcessAuthCode()
    {
        String authCode = StringUtils.trim(keyProcessAuthCode);
        return StringUtils.isEmpty(authCode) ? "ALGO_KEY_PROCESS_IDENTIFY_EXECUTE" : authCode;
    }

    public String getKeyProcessUrl()
    {
        String url = StringUtils.trim(keyProcessUrl);
        if (StringUtils.isNotEmpty(url))
        {
            return url;
        }

        String base = getBaseUrl();
        if (StringUtils.isEmpty(base))
        {
            return null;
        }

        String path = getKeyProcessPath();
        if (StringUtils.isEmpty(path))
        {
            return base;
        }
        return StringUtils.endsWith(base, "/") ? base + path.substring(1) : base + path;
    }

    public String getFeatureAnalysisPath()
    {
        String path = StringUtils.trim(featureAnalysisPath);

        if (StringUtils.isEmpty(path))
        {
            return path;
        }
        return StringUtils.startsWith(path, "/") ? path : "/" + path;
    }

    public String getFeatureAnalysisAuthCode()
    {
        String authCode = StringUtils.trim(featureAnalysisAuthCode);
        return StringUtils.isEmpty(authCode) ? "ALGO_FEATURE_ANALYSIS_EXECUTE" : authCode;
    }

    public String getFeatureProcessingPath()
    {
        String path = StringUtils.trim(featureProcessingPath);

        if (StringUtils.isEmpty(path))
        {
            return path;
        }
        return StringUtils.startsWith(path, "/") ? path : "/" + path;
    }

    public String getPredictPath()
    {
        String path = StringUtils.trim(predictPath);

        if (StringUtils.isEmpty(path))
        {
            return path;
        }
        return StringUtils.startsWith(path, "/") ? path : "/" + path;
    }

    public String getPredictAuthCode()
    {
        String authCode = StringUtils.trim(predictAuthCode);
        return StringUtils.isEmpty(authCode) ? "ALGO_FAULT_PREDICT_EXECUTE" : authCode;
    }

    public String getPredictUrl()
    {
        String url = StringUtils.trim(predictUrl);
        if (StringUtils.isNotEmpty(url))
        {
            return url;
        }

        String base = getBaseUrl();
        if (StringUtils.isEmpty(base))
        {
            return null;
        }

        String path = getPredictPath();
        if (StringUtils.isEmpty(path))
        {
            return base;
        }
        return StringUtils.endsWith(base, "/") ? base + path.substring(1) : base + path;
    }

    public String getWarningPath()
    {
        String path = StringUtils.trim(warningPath);

        if (StringUtils.isEmpty(path))
        {
            return path;
        }
        return StringUtils.startsWith(path, "/") ? path : "/" + path;
    }

    public String getWarningAuthCode()
    {
        String authCode = StringUtils.trim(warningAuthCode);
        return StringUtils.isEmpty(authCode) ? "ALGO_PROCESS_ANOMALY_EXECUTE" : authCode;
    }

    public String getWarningUrl()
    {
        String url = StringUtils.trim(warningUrl);
        if (StringUtils.isNotEmpty(url))
        {
            return url;
        }

        String base = getBaseUrl();
        if (StringUtils.isEmpty(base))
        {
            return null;
        }

        String path = getWarningPath();
        if (StringUtils.isEmpty(path))
        {
            return base;
        }
        return StringUtils.endsWith(base, "/") ? base + path.substring(1) : base + path;
    }

    public String getFaultUrl()
    {
        String url = StringUtils.trim(faultUrl);
        if (StringUtils.isNotEmpty(url))
        {
            return url;
        }

        String base = getBaseUrl();
        if (StringUtils.isEmpty(base))
        {
            return null;
        }

        String path = StringUtils.trim(faultPath);
        if (StringUtils.isEmpty(path))
        {
            return base;
        }

        String ep = StringUtils.startsWith(path, "/") ? path : "/" + path;
        return StringUtils.endsWith(base, "/") ? base + ep.substring(1) : base + ep;
    }

    public boolean isMock()
    {
        return mock;
    }

    public void setMock(boolean mock)
    {
        this.mock = mock;
    }

}
