package com.ruoyi.project1.domain;

import java.math.BigDecimal;

/**
 * Homepage summary payload for project1 dossier block.
 */
public class Project1HomeSummary
{
    private IntegrationSummary integration = new IntegrationSummary();

    private AccessSummary access = new AccessSummary();

    private DossierSummary dossier = new DossierSummary();

    private StatusSummary status = new StatusSummary();

    public IntegrationSummary getIntegration()
    {
        return integration;
    }

    public void setIntegration(IntegrationSummary integration)
    {
        this.integration = integration;
    }

    public AccessSummary getAccess()
    {
        return access;
    }

    public void setAccess(AccessSummary access)
    {
        this.access = access;
    }

    public DossierSummary getDossier()
    {
        return dossier;
    }

    public void setDossier(DossierSummary dossier)
    {
        this.dossier = dossier;
    }

    public StatusSummary getStatus()
    {
        return status;
    }

    public void setStatus(StatusSummary status)
    {
        this.status = status;
    }

    public static class IntegrationSummary
    {
        private Long fieldMappingResultTotal = 0L;

        private Long datasourceCount = 0L;

        private Long matchTaskCount = 0L;

        private BigDecimal f1Average = BigDecimal.ZERO;

        public Long getFieldMappingResultTotal()
        {
            return fieldMappingResultTotal;
        }

        public void setFieldMappingResultTotal(Long fieldMappingResultTotal)
        {
            this.fieldMappingResultTotal = fieldMappingResultTotal;
        }

        public Long getDatasourceCount()
        {
            return datasourceCount;
        }

        public void setDatasourceCount(Long datasourceCount)
        {
            this.datasourceCount = datasourceCount;
        }

        public Long getMatchTaskCount()
        {
            return matchTaskCount;
        }

        public void setMatchTaskCount(Long matchTaskCount)
        {
            this.matchTaskCount = matchTaskCount;
        }

        public BigDecimal getF1Average()
        {
            return f1Average;
        }

        public void setF1Average(BigDecimal f1Average)
        {
            this.f1Average = f1Average;
        }
    }

    public static class AccessSummary
    {
        private Long successAccessRecordTotal = 0L;

        private Long failedRecordTotal = 0L;

        private Long enabledPlanCount = 0L;

        private BigDecimal successRate = BigDecimal.ZERO;

        public Long getSuccessAccessRecordTotal()
        {
            return successAccessRecordTotal;
        }

        public void setSuccessAccessRecordTotal(Long successAccessRecordTotal)
        {
            this.successAccessRecordTotal = successAccessRecordTotal;
        }

        public Long getFailedRecordTotal()
        {
            return failedRecordTotal;
        }

        public void setFailedRecordTotal(Long failedRecordTotal)
        {
            this.failedRecordTotal = failedRecordTotal;
        }

        public Long getEnabledPlanCount()
        {
            return enabledPlanCount;
        }

        public void setEnabledPlanCount(Long enabledPlanCount)
        {
            this.enabledPlanCount = enabledPlanCount;
        }

        public BigDecimal getSuccessRate()
        {
            return successRate;
        }

        public void setSuccessRate(BigDecimal successRate)
        {
            this.successRate = successRate;
        }
    }

    public static class DossierSummary
    {
        private String aircraftLabel = "-";

        private String currentVersion = "-";

        private Long instanceCount = 0L;

        private Long versionCount = 0L;

        private Long generationTaskCount = 0L;

        private Long directoryNodeCount = 0L;

        private Long contentItemCount = 0L;

        public String getAircraftLabel()
        {
            return aircraftLabel;
        }

        public void setAircraftLabel(String aircraftLabel)
        {
            this.aircraftLabel = aircraftLabel;
        }

        public String getCurrentVersion()
        {
            return currentVersion;
        }

        public void setCurrentVersion(String currentVersion)
        {
            this.currentVersion = currentVersion;
        }

        public Long getInstanceCount()
        {
            return instanceCount;
        }

        public void setInstanceCount(Long instanceCount)
        {
            this.instanceCount = instanceCount;
        }

        public Long getVersionCount()
        {
            return versionCount;
        }

        public void setVersionCount(Long versionCount)
        {
            this.versionCount = versionCount;
        }

        public Long getGenerationTaskCount()
        {
            return generationTaskCount;
        }

        public void setGenerationTaskCount(Long generationTaskCount)
        {
            this.generationTaskCount = generationTaskCount;
        }

        public Long getDirectoryNodeCount()
        {
            return directoryNodeCount;
        }

        public void setDirectoryNodeCount(Long directoryNodeCount)
        {
            this.directoryNodeCount = directoryNodeCount;
        }

        public Long getContentItemCount()
        {
            return contentItemCount;
        }

        public void setContentItemCount(Long contentItemCount)
        {
            this.contentItemCount = contentItemCount;
        }
    }

    public static class StatusSummary
    {
        private String datasourceConnectionText = "待检测";

        public String getDatasourceConnectionText()
        {
            return datasourceConnectionText;
        }

        public void setDatasourceConnectionText(String datasourceConnectionText)
        {
            this.datasourceConnectionText = datasourceConnectionText;
        }
    }
}
