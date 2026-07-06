package com.ruoyi.project1.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.project1.domain.AccessPlan;
import com.ruoyi.project1.domain.Datasource;
import com.ruoyi.project1.domain.MatchResultSet;
import com.ruoyi.project1.domain.Project1HomeSummary;
import com.ruoyi.project1.domain.TaskMetric;
import com.ruoyi.project1.dossier.mapper.DossierDetailMapper;
import com.ruoyi.project1.dossier.mapper.DossierInstanceMapper;
import com.ruoyi.project1.mapper.AccessPlanMapper;
import com.ruoyi.project1.mapper.DatasourceMapper;
import com.ruoyi.project1.mapper.MatchResultSetMapper;
import com.ruoyi.project1.mapper.TaskMetricMapper;
import com.ruoyi.project1.service.support.Project1PresetScenarioService;

@ExtendWith(MockitoExtension.class)
class Project1HomeServiceImplTest
{
    @Mock
    private DatasourceMapper datasourceMapper;

    @Mock
    private MatchResultSetMapper matchResultSetMapper;

    @Mock
    private TaskMetricMapper taskMetricMapper;

    @Mock
    private AccessPlanMapper accessPlanMapper;

    @Mock
    private DossierDetailMapper dossierDetailMapper;

    @Mock
    private DossierInstanceMapper dossierInstanceMapper;

    @Mock
    private Project1PresetScenarioService presetScenarioService;

    private Project1HomeServiceImpl service;

    @BeforeEach
    void setUp()
    {
        service = new Project1HomeServiceImpl(datasourceMapper, matchResultSetMapper, taskMetricMapper,
                accessPlanMapper, dossierDetailMapper, dossierInstanceMapper, presetScenarioService);
    }

    @Test
    void aggregatesHomepageSummaryFromProject1BusinessData()
    {
        doNothing().when(presetScenarioService).ensurePresetScenario();
        when(datasourceMapper.selectDatasourceList(any())).thenReturn(List.of(
                datasource(1L, "success"), datasource(2L, "success"), datasource(3L, "success"),
                datasource(4L, "success"), datasource(5L, "success")));

        when(matchResultSetMapper.selectMatchResultSetList(any())).thenReturn(List.of(
                resultSet(11L, 101L, 5570L), resultSet(12L, 102L, 5480L), resultSet(13L, 103L, 5610L),
                resultSet(14L, 104L, 5505L), resultSet(15L, 105L, 5618L), otherResultSet()));
        when(taskMetricMapper.selectTaskMetricList(any())).thenAnswer(invocation -> {
            TaskMetric query = invocation.getArgument(0);
            return List.of(metric(query.getResultSetId(), f1Score(query.getResultSetId())));
        });

        when(accessPlanMapper.selectAccessPlanList(any())).thenReturn(List.of(
                accessPlan(28640L, 86L), accessPlan(18320L, 54L), accessPlan(27480L, 112L),
                accessPlan(21460L, 73L), accessPlan(12680L, 38L), normalAccessPlan(999L, 999L)));
        when(presetScenarioService.isManagedAccessPlan(any(AccessPlan.class)))
                .thenAnswer(invocation -> ((AccessPlan) invocation.getArgument(0)).getRemark().contains("managed"));

        when(dossierDetailMapper.selectLatestInstance(null, null)).thenReturn(map(
                "instanceId", "DI-B1234",
                "tailNumber", "B-1234",
                "modelCode", "C919"));
        when(dossierDetailMapper.selectVersion("DI-B1234", null)).thenReturn(map(
                "versionId", "DV-B1234-V11",
                "versionLabel", "V1.1"));
        when(dossierInstanceMapper.selectInstanceCount(any())).thenReturn(2);
        when(dossierInstanceMapper.selectVersionList("DI-B1234"))
                .thenReturn(List.of(map("versionId", "DV-B1234-V10"), map("versionId", "DV-B1234-V11")));
        when(dossierInstanceMapper.selectJobCount("DI-B1234")).thenReturn(1);
        when(dossierDetailMapper.selectStructureNodes("DV-B1234-V11"))
                .thenReturn(Collections.nCopies(2013, Collections.emptyMap()));
        when(dossierDetailMapper.selectContentItems("DV-B1234-V11", null))
                .thenReturn(Collections.nCopies(2471, Collections.emptyMap()));

        Project1HomeSummary summary = service.selectDossierSummary();

        assertEquals(27783L, summary.getIntegration().getFieldMappingResultTotal());
        assertEquals(5L, summary.getIntegration().getDatasourceCount());
        assertEquals(5L, summary.getIntegration().getMatchTaskCount());
        assertEquals(new BigDecimal("0.716"), summary.getIntegration().getF1Average());
        assertEquals("连接正常", summary.getStatus().getDatasourceConnectionText());

        assertEquals(108580L, summary.getAccess().getSuccessAccessRecordTotal());
        assertEquals(363L, summary.getAccess().getFailedRecordTotal());
        assertEquals(5L, summary.getAccess().getEnabledPlanCount());
        assertEquals(new BigDecimal("99.67"), summary.getAccess().getSuccessRate());

        assertEquals("B-1234 / C919", summary.getDossier().getAircraftLabel());
        assertEquals("V1.1", summary.getDossier().getCurrentVersion());
        assertEquals(2L, summary.getDossier().getInstanceCount());
        assertEquals(2L, summary.getDossier().getVersionCount());
        assertEquals(1L, summary.getDossier().getGenerationTaskCount());
        assertEquals(2013L, summary.getDossier().getDirectoryNodeCount());
        assertEquals(2471L, summary.getDossier().getContentItemCount());
        verify(dossierInstanceMapper, never()).selectInstanceSummary(any());
        verify(dossierInstanceMapper, never()).selectJobList("DI-B1234");
    }

    private static Datasource datasource(Long id, String connectionStatus)
    {
        Datasource datasource = new Datasource();
        datasource.setDatasourceId(id);
        datasource.setStatus("0");
        datasource.setDelFlag("0");
        datasource.setConnectionStatus(connectionStatus);
        return datasource;
    }

    private static MatchResultSet resultSet(Long id, Long taskId, Long totalRows)
    {
        MatchResultSet resultSet = new MatchResultSet();
        resultSet.setResultSetId(id);
        resultSet.setTaskId(taskId);
        resultSet.setMethod("MagnetoGPT");
        resultSet.setVariant("one2one");
        resultSet.setResultSetName("MagnetoGPT CF-SF one2one");
        resultSet.setTotalRows(totalRows);
        return resultSet;
    }

    private static MatchResultSet otherResultSet()
    {
        MatchResultSet resultSet = resultSet(99L, 199L, 9999L);
        resultSet.setResultSetName("MagnetoGPT CF-SF other");
        return resultSet;
    }

    private static TaskMetric metric(Long resultSetId, BigDecimal value)
    {
        TaskMetric metric = new TaskMetric();
        metric.setResultSetId(resultSetId);
        metric.setMetricKey("f1Score");
        metric.setMetricValue(value);
        return metric;
    }

    private static BigDecimal f1Score(Long resultSetId)
    {
        Map<Long, BigDecimal> values = Map.of(
                11L, new BigDecimal("0.710"),
                12L, new BigDecimal("0.720"),
                13L, new BigDecimal("0.715"),
                14L, new BigDecimal("0.725"),
                15L, new BigDecimal("0.710"));
        return values.getOrDefault(resultSetId, BigDecimal.ZERO);
    }

    private static AccessPlan accessPlan(Long success, Long failed)
    {
        AccessPlan plan = new AccessPlan();
        plan.setUseStatus("enabled");
        plan.setStatus("0");
        plan.setDelFlag("0");
        plan.setTotalSuccessCount(success);
        plan.setTotalFailedCount(failed);
        plan.setRemark("managed");
        return plan;
    }

    private static AccessPlan normalAccessPlan(Long success, Long failed)
    {
        AccessPlan plan = accessPlan(success, failed);
        plan.setRemark("normal");
        return plan;
    }

    private static Map<String, Object> map(Object... values)
    {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < values.length; i += 2)
        {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }
}
