package com.ruoyi.project3.config;

import com.ruoyi.common.core.utils.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "frame-beam-crack")
public class FrameBeamCrackProperties
{
    private Algorithm algorithm = new Algorithm();
    private ImportConfig importConfig = new ImportConfig();

    public Algorithm getAlgorithm()
    {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm)
    {
        this.algorithm = algorithm;
    }

    public ImportConfig getImportConfig()
    {
        return importConfig;
    }

    public void setImportConfig(ImportConfig importConfig)
    {
        this.importConfig = importConfig;
    }

    public static class Algorithm
    {
        private String baseUrl = "http://127.0.0.1:9741";
        private String predictPath = "/frame-beam-crack/predict";
        private Integer connectTimeout = 5000;
        private Integer readTimeout = 600000;

        public String getBaseUrl()
        {
            return StringUtils.trim(baseUrl);
        }

        public void setBaseUrl(String baseUrl)
        {
            this.baseUrl = baseUrl;
        }

        public String getPredictPath()
        {
            String path = StringUtils.trim(predictPath);
            return StringUtils.isEmpty(path) || StringUtils.startsWith(path, "/") ? path : "/" + path;
        }

        public void setPredictPath(String predictPath)
        {
            this.predictPath = predictPath;
        }

        public Integer getConnectTimeout()
        {
            return connectTimeout == null || connectTimeout <= 0 ? 5000 : connectTimeout;
        }

        public void setConnectTimeout(Integer connectTimeout)
        {
            this.connectTimeout = connectTimeout;
        }

        public Integer getReadTimeout()
        {
            return readTimeout == null || readTimeout <= 0 ? 600000 : readTimeout;
        }

        public void setReadTimeout(Integer readTimeout)
        {
            this.readTimeout = readTimeout;
        }

        public String getPredictUrl()
        {
            String base = getBaseUrl();
            String path = getPredictPath();
            if (StringUtils.isEmpty(base))
            {
                return null;
            }
            if (StringUtils.isEmpty(path))
            {
                return base;
            }
            return StringUtils.endsWith(base, "/") ? base + path.substring(1) : base + path;
        }
    }

    public static class ImportConfig
    {
        private Long maxFileSize = 500L * 1024L * 1024L;
        private String storageDir = "frame-beam-crack";

        public Long getMaxFileSize()
        {
            return maxFileSize == null || maxFileSize <= 0 ? 500L * 1024L * 1024L : maxFileSize;
        }

        public void setMaxFileSize(Long maxFileSize)
        {
            this.maxFileSize = maxFileSize;
        }

        public String getStorageDir()
        {
            return StringUtils.isEmpty(StringUtils.trim(storageDir)) ? "frame-beam-crack" : StringUtils.trim(storageDir);
        }

        public void setStorageDir(String storageDir)
        {
            this.storageDir = storageDir;
        }
    }
}
