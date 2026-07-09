package com.ruoyi.project1.dossier.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class DossierDetailMapperSqlTest
{
    @Test
    void selectVersionOrdersOnlyNarrowVersionIdsBeforeReadingSnapshotJson() throws Exception
    {
        String mapperXml = mapperXml();

        String selectVersion = mapperXml.substring(mapperXml.indexOf("<select id=\"selectVersion\""),
                mapperXml.indexOf("<select id=\"selectVersionList\""));

        assertTrue(Pattern.compile("inner\\s+join\\s*\\(\\s*select\\s+id\\s+from\\s+t1_dossier_version",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(selectVersion).find());
    }

    @Test
    void qualityTraceRowsUseFinishedWorkflowReturnedResultFilter() throws Exception
    {
        String mapperXml = mapperXml();

        String selectQualityTraceRows = mapperXml.substring(mapperXml.indexOf("<select id=\"selectQualityTraceRows\""),
                mapperXml.indexOf("<select id=\"selectAllDocuments\""));

        assertTrue(selectQualityTraceRows.contains("and p.status = 'FINISHED'"));
        assertTrue(selectQualityTraceRows.contains("t.task_status in ('SUBMITTED', 'CONFIRMED')"));
        assertTrue(selectQualityTraceRows.contains("t.process_result is not null"));
        assertTrue(selectQualityTraceRows.contains("p.component_code in"));
        assertFalse(selectQualityTraceRows.contains("or p.occur_part like"));
        assertFalse(selectQualityTraceRows.contains("or p.title like"));
        assertFalse(selectQualityTraceRows.contains("or p.involved_system like"));
        assertFalse(selectQualityTraceRows.contains("or p.product_model like"));
    }

    @Test
    void qualityTraceRowsLimitDemoAggregationToLatestFinishedQualityWorkflow() throws Exception
    {
        String mapperXml = mapperXml();

        String selectQualityTraceRows = mapperXml.substring(mapperXml.indexOf("<select id=\"selectQualityTraceRows\""),
                mapperXml.indexOf("<select id=\"selectAllDocuments\""));

        assertTrue(selectQualityTraceRows.contains("demoTraceEnabled"));
        assertTrue(selectQualityTraceRows.contains("latest_p.problem_id"));
        assertTrue(selectQualityTraceRows.contains("latest_p.status = 'FINISHED'"));
        assertTrue(selectQualityTraceRows.contains("latest_t.task_status in ('SUBMITTED', 'CONFIRMED')"));
        assertFalse(selectQualityTraceRows.contains("p.product_model = 'C001'"));
        assertFalse(selectQualityTraceRows.contains("p.involved_system = '液压系统'"));
    }

    private String mapperXml() throws Exception
    {
        try (InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("mapper/project1/dossier/DossierDetailMapper.xml"))
        {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
