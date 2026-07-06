package com.ruoyi.project1.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.domain.AccessPlan;
import com.ruoyi.project1.domain.Datasource;
import com.ruoyi.project1.domain.MatchResultSet;
import com.ruoyi.project1.domain.Project1HomeSummary;
import com.ruoyi.project1.domain.Project1HomeSummary.AccessSummary;
import com.ruoyi.project1.domain.Project1HomeSummary.DossierSummary;
import com.ruoyi.project1.domain.Project1HomeSummary.IntegrationSummary;
import com.ruoyi.project1.domain.Project1HomeSummary.StatusSummary;
import com.ruoyi.project1.domain.TaskMetric;
import com.ruoyi.project1.dossier.mapper.DossierDetailMapper;
import com.ruoyi.project1.dossier.mapper.DossierInstanceMapper;
import com.ruoyi.project1.mapper.AccessPlanMapper;
import com.ruoyi.project1.mapper.DatasourceMapper;
import com.ruoyi.project1.mapper.MatchResultSetMapper;
import com.ruoyi.project1.mapper.TaskMetricMapper;
import com.ruoyi.project1.service.IProject1HomeService;
import com.ruoyi.project1.service.support.Project1PresetScenarioService;

@Service
public class Project1HomeServiceImpl implements IProject1HomeService
{
    private static final String TARGET_RESULT_SET_NAME = "MagnetoGPT CF-SF one2one";

    private final DatasourceMapper datasourceMapper;

    private final MatchResultSetMapper matchResultSetMapper;

    private final TaskMetricMapper taskMetricMapper;

    private final AccessPlanMapper accessPlanMapper;

    private final DossierDetailMapper dossierDetailMapper;

    private final DossierInstanceMapper dossierInstanceMapper;

    private final Project1PresetScenarioService presetScenarioService;

    public Project1HomeServiceImpl(DatasourceMapper datasourceMapper, MatchResultSetMapper matchResultSetMapper,
            TaskMetricMapper taskMetricMapper, AccessPlanMapper accessPlanMapper, DossierDetailMapper dossierDetailMapper,
            DossierInstanceMapper dossierInstanceMapper, Project1PresetScenarioService presetScenarioService)
    {
        this.datasourceMapper = datasourceMapper;
        this.matchResultSetMapper = matchResultSetMapper;
        this.taskMetricMapper = taskMetricMapper;
        this.accessPlanMapper = accessPlanMapper;
        this.dossierDetailMapper = dossierDetailMapper;
        this.dossierInstanceMapper = dossierInstanceMapper;
        this.presetScenarioService = presetScenarioService;
    }

    @Override
    public Project1HomeSummary selectDossierSummary()
    {
        presetScenarioService.ensurePresetScenario();
        presetScenarioService.syncManagedAccessSchedules();

        Project1HomeSummary summary = new Project1HomeSummary();
        applyDatasourceSummary(summary.getIntegration(), summary.getStatus());
        applyMatchSummary(summary.getIntegration());
        applyAccessSummary(summary.getAccess());
        applyDossierSummary(summary.getDossier());
        return summary;
    }

    private void applyDatasourceSummary(IntegrationSummary integration, StatusSummary status)
    {
        Datasource query = new Datasource();
        query.setStatus("0");
        List<Datasource> rows = datasourceMapper.selectDatasourceList(query);
        long count = 0L;
        long successCount = 0L;
        boolean hasFailed = false;
        boolean hasPending = false;

        for (Datasource row : rows)
        {
            if (!isNormalRow(row.getStatus(), row.getDelFlag()))
            {
                continue;
            }
            count++;
            String connectionStatus = StringUtils.defaultString(row.getConnectionStatus());
            if ("success".equals(connectionStatus))
            {
                successCount++;
            }
            else if ("failed".equals(connectionStatus))
            {
                hasFailed = true;
            }
            else
            {
                hasPending = true;
            }
        }

        integration.setDatasourceCount(count);
        if (count == 0)
        {
            status.setDatasourceConnectionText("待检测");
        }
        else if (hasFailed)
        {
            status.setDatasourceConnectionText("存在异常");
        }
        else if (successCount == count)
        {
            status.setDatasourceConnectionText("连接正常");
        }
        else if (hasPending)
        {
            status.setDatasourceConnectionText("待检测");
        }
    }

    private void applyMatchSummary(IntegrationSummary integration)
    {
        MatchResultSet query = new MatchResultSet();
        query.setResultSetName(TARGET_RESULT_SET_NAME);
        List<MatchResultSet> rows = matchResultSetMapper.selectMatchResultSetList(query);

        long totalRows = 0L;
        Set<Long> taskIds = new HashSet<>();
        BigDecimal f1Total = BigDecimal.ZERO;
        long f1Count = 0L;

        for (MatchResultSet row : rows)
        {
            if (!isTargetResultSet(row))
            {
                continue;
            }
            totalRows += defaultLong(row.getTotalRows());
            if (row.getTaskId() != null)
            {
                taskIds.add(row.getTaskId());
            }

            BigDecimal f1Score = findMetricValue(row.getResultSetId(), "f1Score");
            if (f1Score != null)
            {
                f1Total = f1Total.add(f1Score);
                f1Count++;
            }
        }

        integration.setFieldMappingResultTotal(totalRows);
        integration.setMatchTaskCount((long) taskIds.size());
        integration.setF1Average(f1Count == 0 ? BigDecimal.ZERO : f1Total.divide(BigDecimal.valueOf(f1Count), 3, RoundingMode.HALF_UP));
    }

    private void applyAccessSummary(AccessSummary access)
    {
        AccessPlan query = new AccessPlan();
        query.setStatus("0");
        List<AccessPlan> rows = accessPlanMapper.selectAccessPlanList(query);

        long successTotal = 0L;
        long failedTotal = 0L;
        long enabledCount = 0L;

        for (AccessPlan row : rows)
        {
            if (!isNormalRow(row.getStatus(), row.getDelFlag()) || !presetScenarioService.isManagedAccessPlan(row))
            {
                continue;
            }
            successTotal += defaultLong(row.getTotalSuccessCount());
            failedTotal += defaultLong(row.getTotalFailedCount());
            if ("enabled".equals(row.getUseStatus()))
            {
                enabledCount++;
            }
        }

        access.setSuccessAccessRecordTotal(successTotal);
        access.setFailedRecordTotal(failedTotal);
        access.setEnabledPlanCount(enabledCount);
        long denominator = successTotal + failedTotal;
        access.setSuccessRate(denominator == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(successTotal).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP));
    }

    private void applyDossierSummary(DossierSummary dossier)
    {
        Map<String, Object> latestInstance = dossierDetailMapper.selectLatestInstance(null, null);
        dossier.setInstanceCount((long) dossierInstanceMapper.selectInstanceCount(new HashMap<>()));

        if (latestInstance == null || latestInstance.isEmpty())
        {
            return;
        }

        String instanceId = textValue(latestInstance, "instanceId");
        dossier.setAircraftLabel(buildAircraftLabel(latestInstance));

        Map<String, Object> version = dossierDetailMapper.selectVersion(instanceId, null);
        String versionId = textValue(version, "versionId");
        if (StringUtils.isBlank(versionId))
        {
            versionId = textValue(latestInstance, "currentVersionId");
        }
        dossier.setCurrentVersion(StringUtils.defaultIfBlank(textValue(version, "versionLabel"),
                StringUtils.defaultIfBlank(textValue(latestInstance, "currentVersionNo"), "-")));

        dossier.setVersionCount((long) safeSize(dossierInstanceMapper.selectVersionList(instanceId)));
        dossier.setGenerationTaskCount((long) dossierInstanceMapper.selectJobCount(instanceId));
        if (StringUtils.isNotBlank(versionId))
        {
            dossier.setDirectoryNodeCount((long) safeSize(dossierDetailMapper.selectStructureNodes(versionId)));
            dossier.setContentItemCount((long) safeSize(dossierDetailMapper.selectContentItems(versionId, null)));
        }
    }

    private boolean isTargetResultSet(MatchResultSet row)
    {
        return row != null && TARGET_RESULT_SET_NAME.equals(row.getResultSetName())
                && "MagnetoGPT".equals(row.getMethod())
                && "one2one".equals(row.getVariant())
                && !presetScenarioService.isPresetResultSet(row);
    }

    private BigDecimal findMetricValue(Long resultSetId, String metricKey)
    {
        if (resultSetId == null || StringUtils.isBlank(metricKey))
        {
            return null;
        }
        TaskMetric query = new TaskMetric();
        query.setResultSetId(resultSetId);
        query.setMetricKey(metricKey);
        List<TaskMetric> metrics = taskMetricMapper.selectTaskMetricList(query);
        if (metrics == null || metrics.isEmpty())
        {
            return null;
        }
        return metrics.get(0).getMetricValue();
    }

    private boolean isNormalRow(String status, String delFlag)
    {
        return !"1".equals(status) && !"2".equals(delFlag);
    }

    private long defaultLong(Long value)
    {
        return value == null ? 0L : value;
    }

    private int safeSize(List<?> rows)
    {
        return rows == null ? 0 : rows.size();
    }

    private Long numberValue(Map<String, Object> row, String key)
    {
        if (row == null || row.get(key) == null)
        {
            return 0L;
        }
        Object value = row.get(key);
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        try
        {
            return Long.parseLong(String.valueOf(value));
        }
        catch (NumberFormatException e)
        {
            return 0L;
        }
    }

    private String textValue(Map<String, Object> row, String key)
    {
        if (row == null || row.get(key) == null)
        {
            return "";
        }
        return String.valueOf(row.get(key));
    }

    private String buildAircraftLabel(Map<String, Object> latestInstance)
    {
        String tailNumber = textValue(latestInstance, "tailNumber");
        String modelCode = textValue(latestInstance, "modelCode");
        String label = (StringUtils.defaultIfBlank(tailNumber, "-") + " / " + StringUtils.defaultIfBlank(modelCode, "-")).trim();
        return StringUtils.defaultIfBlank(label, "-");
    }
}
