package com.ruoyi.project1.dossier.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import com.ruoyi.project1.dossier.mapper.DossierDetailMapper;
import com.ruoyi.project1.dossier.mapper.DossierGenerationMapper;

class DossierManufacturingDisplayTest
{
    @Test
    void sourceSummaryDescribesManufacturingShopOrder() throws Exception
    {
        DossierGenerationServiceImpl service = new DossierGenerationServiceImpl();
        Method sourceSummary = DossierGenerationServiceImpl.class.getDeclaredMethod("sourceSummary", Map.class,
                String.class);
        sourceSummary.setAccessible(true);

        Map<String, Object> row = map();
        row.put("order_code", "WO-HYD-TUBE-MLG-32A-0042");
        row.put("status", "CLOSED");
        row.put("route_code", "R-HYD-TUBE-MLG-32A-REV-C");
        row.put("actual_start", "2025-11-17 08:10:00");
        row.put("actual_finish", "2025-11-19 16:35:00");

        String summary = (String) sourceSummary.invoke(service, row, "fallback");

        assertTrue(summary.contains("WO-HYD-TUBE-MLG-32A-0042"));
        assertTrue(summary.contains("R-HYD-TUBE-MLG-32A-REV-C"));
        assertTrue(summary.contains("2025-11-17 08:10:00"));
    }

    @Test
    void sourceSummaryDescribesServiceInstallAndFaultRecords() throws Exception
    {
        DossierGenerationServiceImpl service = new DossierGenerationServiceImpl();
        Method sourceSummary = DossierGenerationServiceImpl.class.getDeclaredMethod("sourceSummary", Map.class,
                String.class);
        sourceSummary.setAccessible(true);

        Map<String, Object> install = map();
        install.put("action_type", "INSTALL");
        install.put("action_date", "2026-02-18 16:30:00");
        install.put("install_position", "MLG-HYD-PKG");
        install.put("assembly_params", "{\"torqueNm\":24,\"leakCheckResult\":\"PASS\"}");

        Map<String, Object> usage = map();
        usage.put("tsn_fh", "303.45");
        usage.put("tsn_fc", "414");
        usage.put("remaining_life_value", "1196.55");
        usage.put("remaining_life_unit", "FH");

        Map<String, Object> fault = map();
        fault.put("fault_code", "32-HYD-PULSE-01");
        fault.put("fault_description", "pressure pulse at hydraulic tube port");
        fault.put("status", "RESOLVED");
        fault.put("resolution_type", "seal support replaced");

        String installSummary = (String) sourceSummary.invoke(service, install, "fallback");
        String usageSummary = (String) sourceSummary.invoke(service, usage, "fallback");
        String faultSummary = (String) sourceSummary.invoke(service, fault, "fallback");

        assertTrue(installSummary.contains("MLG-HYD-PKG"));
        assertTrue(installSummary.contains("24"));
        assertTrue(usageSummary.contains("303.45"));
        assertTrue(usageSummary.contains("414"));
        assertTrue(faultSummary.contains("32-HYD-PULSE-01"));
        assertTrue(faultSummary.contains("pressure pulse"));
        assertTrue(faultSummary.contains("seal support replaced"));
    }

    @Test
    void contentRowsExpandManufacturingOperationDetails() throws Exception
    {
        DossierDetailServiceImpl service = new DossierDetailServiceImpl();
        Method contentRows = DossierDetailServiceImpl.class.getDeclaredMethod("contentRows", Map.class, List.class);
        contentRows.setAccessible(true);

        Map<String, Object> directory = map();
        directory.put("category", "manufacturing");
        directory.put("chapterId", "chapter-manufacturing");
        directory.put("sourceTables", Collections.emptyList());
        directory.put("lifecycleStages", Collections.singletonList("MANUFACTURING"));

        Map<String, Object> attrs = map();
        attrs.put("chapterId", "chapter-manufacturing");
        attrs.put("actualSourceTable", "production_operation_record");
        attrs.put("equipmentId", "EQP-BEND-01");
        attrs.put("operatorId", "EMP-MF-002");
        attrs.put("startTime", "2025-11-18 08:30:00");
        attrs.put("endTime", "2025-11-18 09:45:00");
        attrs.put("actualParams", "{\"processName\":\"三维数控弯管\",\"conclusion\":\"合格\"}");

        Map<String, Object> item = map();
        item.put("chapterId", "chapter-manufacturing");
        item.put("sourceTable", "production_operation_record");
        item.put("lifecycleStage", "MANUFACTURING");
        item.put("itemName", "加工记录");
        item.put("contentSummary", "old summary");
        item.put("completenessStatus", "complete");
        item.put("attrs", attrs);

        List<Map<String, Object>> rows = castRows(contentRows.invoke(service, directory,
                new ArrayList<>(Collections.singletonList(item))));

        assertEquals(1, rows.size());
        assertTrue(String.valueOf(rows.get(0).get("name")).contains("三维数控弯管"));
        assertTrue(String.valueOf(rows.get(0).get("value")).contains("EQP-BEND-01"));
        assertTrue(String.valueOf(rows.get(0).get("value")).contains("EMP-MF-002"));
        assertTrue(String.valueOf(rows.get(0).get("value")).contains("合格"));
    }

    @Test
    void contentRowsAddManufacturingGroupsForClearDisplay() throws Exception
    {
        DossierDetailServiceImpl service = new DossierDetailServiceImpl();
        Method contentRows = DossierDetailServiceImpl.class.getDeclaredMethod("contentRows", Map.class, List.class);
        contentRows.setAccessible(true);

        Map<String, Object> directory = map();
        directory.put("category", "manufacturing");
        directory.put("chapterId", "chapter-manufacturing");
        directory.put("sourceTables", Collections.emptyList());
        directory.put("lifecycleStages", Collections.singletonList("MANUFACTURING"));

        Map<String, Object> attrs = map();
        attrs.put("chapterId", "chapter-manufacturing");
        attrs.put("actualSourceTable", "material_lot_trace");
        attrs.put("materialPn", "TB3 Ti-3Al-2.5V");
        attrs.put("lotNumber", "LOT-TB3-2025-Q4-18");

        Map<String, Object> item = map();
        item.put("chapterId", "chapter-manufacturing");
        item.put("sourceTable", "material_lot_trace");
        item.put("lifecycleStage", "MANUFACTURING");
        item.put("itemName", "material");
        item.put("contentSummary", "material summary");
        item.put("completenessStatus", "complete");
        item.put("attrs", attrs);

        List<Map<String, Object>> rows = castRows(contentRows.invoke(service, directory,
                new ArrayList<>(Collections.singletonList(item))));

        assertEquals("material", rows.get(0).get("groupKey"));
        assertEquals(20, rows.get(0).get("groupOrder"));
        assertTrue(String.valueOf(rows.get(0).get("groupLabel")).contains("材料"));
    }

    @Test
    void contentRowsPreferDetailedInspectionFallbackWhenExactRowsAreSimplified() throws Exception
    {
        DossierDetailServiceImpl service = new DossierDetailServiceImpl();
        Method contentRows = DossierDetailServiceImpl.class.getDeclaredMethod("contentRows", Map.class, List.class);
        contentRows.setAccessible(true);

        Map<String, Object> directory = map();
        directory.put("category", "inspection");
        directory.put("chapterId", "chapter-inspection");
        directory.put("sourceTables", Collections.emptyList());
        directory.put("lifecycleStages", Collections.singletonList("INSPECTION"));

        Map<String, Object> simplifiedAttrs = map();
        simplifiedAttrs.put("chapterId", "chapter-inspection");

        Map<String, Object> simplified = map();
        simplified.put("chapterId", "chapter-inspection");
        simplified.put("sourceTable", "inspection_record");
        simplified.put("lifecycleStage", "MANUFACTURING");
        simplified.put("itemName", "液压弯管终检与气密");
        simplified.put("sourceRecordKey", "PASS");
        simplified.put("contentSummary", "PASS");
        simplified.put("completenessStatus", "complete");
        simplified.put("attrs", simplifiedAttrs);

        Map<String, Object> detailedAttrs = map();
        detailedAttrs.put("actualSourceTable", "t1_inspection_record");
        detailedAttrs.put("inspectionType", "FINAL");
        detailedAttrs.put("inspectionStdDoc", "Q/COMAC-HYD-INS-2025");
        detailedAttrs.put("inspectionDate", "2025-11-20 10:30:00");
        detailedAttrs.put("inspectorId", "IQC-HYD-008");
        detailedAttrs.put("result", "PASS");
        detailedAttrs.put("remarks", "气密泄漏检验合格");

        Map<String, Object> detailed = map();
        detailed.put("sourceTable", "t1_inspection_record");
        detailed.put("lifecycleStage", "INSPECTION");
        detailed.put("itemName", "检验记录：FINAL");
        detailed.put("sourceRecordKey", "FINAL-HYD-001");
        detailed.put("contentSummary", "标准：Q/COMAC-HYD-INS-2025，结果：PASS，气密泄漏检验合格。");
        detailed.put("completenessStatus", "complete");
        detailed.put("attrs", detailedAttrs);

        List<Map<String, Object>> rows = castRows(contentRows.invoke(service, directory,
                new ArrayList<>(Arrays.asList(simplified, detailed))));

        assertEquals(1, rows.size());
        assertTrue(String.valueOf(rows.get(0).get("value")).contains("气密泄漏检验合格"));
        assertTrue(String.valueOf(rows.get(0).get("groupLabel")).contains("检验记录"));
        assertNotEquals("PASS", rows.get(0).get("value"));
    }

    @Test
    void contentRowsKeepDetailedServiceFallbackWhenExactRowsAreSimplified() throws Exception
    {
        DossierDetailServiceImpl service = new DossierDetailServiceImpl();
        Method contentRows = DossierDetailServiceImpl.class.getDeclaredMethod("contentRows", Map.class, List.class);
        contentRows.setAccessible(true);

        Map<String, Object> directory = map();
        directory.put("category", "service");
        directory.put("chapterId", "chapter-service");
        directory.put("sourceTables", Collections.singletonList("t1_life_usage_record"));
        directory.put("lifecycleStages", Collections.singletonList("SERVICE"));

        Map<String, Object> simplifiedAttrs = map();
        simplifiedAttrs.put("chapterId", "chapter-service");

        Map<String, Object> simplified = map();
        simplified.put("chapterId", "chapter-service");
        simplified.put("sourceTable", "t1_life_usage_record");
        simplified.put("sourceRecordId", "dlife006");
        simplified.put("sourceRecordKey", "HYD-TUBE-MLG-32A-SERVICE");
        simplified.put("lifecycleStage", "SERVICE");
        simplified.put("itemName", "life usage");
        simplified.put("contentSummary", "dlife006");
        simplified.put("completenessStatus", "complete");
        simplified.put("attrs", simplifiedAttrs);

        Map<String, Object> detailedAttrs = map();
        detailedAttrs.put("actualSourceTable", "t1_life_usage_record");
        detailedAttrs.put("tsnFh", "303.45");
        detailedAttrs.put("tsnFc", "414");
        detailedAttrs.put("remainingLifeValue", "1196.55");
        detailedAttrs.put("remainingLifeUnit", "FH");

        Map<String, Object> detailed = map();
        detailed.put("sourceTable", "t1_life_usage_record");
        detailed.put("sourceRecordId", "dlife006");
        detailed.put("sourceRecordKey", "HYD-TUBE-MLG-32A-SERVICE");
        detailed.put("lifecycleStage", "SERVICE");
        detailed.put("itemName", "hydraulic tube service usage");
        detailed.put("contentSummary", "installed usage 303.45 FH / 414 FC");
        detailed.put("completenessStatus", "complete");
        detailed.put("attrs", detailedAttrs);

        List<Map<String, Object>> rows = castRows(contentRows.invoke(service, directory,
                new ArrayList<>(Arrays.asList(simplified, detailed))));

        assertEquals(1, rows.size());
        assertTrue(String.valueOf(rows.get(0).get("value")).contains("303.45"));
        assertTrue(String.valueOf(rows.get(0).get("value")).contains("414"));
        assertNotEquals("dlife006", rows.get(0).get("value"));
    }

    @Test
    void contentRowsTreatInstallFieldsAsInstallationDirectory() throws Exception
    {
        DossierDetailServiceImpl service = new DossierDetailServiceImpl();
        Method contentRows = DossierDetailServiceImpl.class.getDeclaredMethod("contentRows", Map.class, List.class);
        contentRows.setAccessible(true);

        Map<String, Object> directory = map();
        directory.put("category", "service");
        directory.put("chapterId", "chapter-install");
        directory.put("sourceTables", Arrays.asList("t1_aircraft_bom_node", "t1_assembly_record"));
        directory.put("lifecycleStages", Arrays.asList("DOSSIER", "MANUFACTURING"));
        directory.put("primaryFields", Arrays.asList("tail_number", "install_date", "torque_n_m",
                "leak_check_result"));

        Map<String, Object> exactAttrs = map();
        exactAttrs.put("chapterId", "chapter-install");

        Map<String, Object> exact = map();
        exact.put("chapterId", "chapter-install");
        exact.put("sourceTable", "t1_assembly_record");
        exact.put("sourceRecordId", "assembly-001");
        exact.put("lifecycleStage", "MANUFACTURING");
        exact.put("itemName", "assembly");
        exact.put("contentSummary", "assembly-001");
        exact.put("completenessStatus", "complete");
        exact.put("attrs", exactAttrs);

        Map<String, Object> detailedAttrs = map();
        detailedAttrs.put("actualSourceTable", "t1_install_removal");
        detailedAttrs.put("actionType", "INSTALL");
        detailedAttrs.put("actionDate", "2026-02-18 09:30:00");
        detailedAttrs.put("installPosition", "MLG-HYD-PKG");
        detailedAttrs.put("assemblyParams", "{\"torqueNm\":\"28\",\"leakCheckResult\":\"PASS\"}");

        Map<String, Object> detailed = map();
        detailed.put("sourceTable", "t1_install_removal");
        detailed.put("sourceRecordId", "ir-install-001");
        detailed.put("sourceRecordKey", "HYD-TUBE-MLG-32A-INSTALL");
        detailed.put("lifecycleStage", "INSTALLATION");
        detailed.put("itemName", "hydraulic tube installed");
        detailed.put("contentSummary", "installed at MLG-HYD-PKG");
        detailed.put("completenessStatus", "complete");
        detailed.put("attrs", detailedAttrs);

        List<Map<String, Object>> rows = castRows(contentRows.invoke(service, directory,
                new ArrayList<>(Arrays.asList(exact, detailed))));

        assertTrue(rows.stream().anyMatch(row -> String.valueOf(row.get("value")).contains("MLG-HYD-PKG")));
        assertTrue(rows.stream().anyMatch(row -> String.valueOf(row.get("value")).contains("PASS")));
    }

    @Test
    void contentRowsKeepDetailedFaultFallbackWhenExactRowsAreSimplified() throws Exception
    {
        DossierDetailServiceImpl service = new DossierDetailServiceImpl();
        Method contentRows = DossierDetailServiceImpl.class.getDeclaredMethod("contentRows", Map.class, List.class);
        contentRows.setAccessible(true);

        Map<String, Object> directory = map();
        directory.put("category", "fault");
        directory.put("chapterId", "chapter-fault");
        directory.put("sourceTables", Collections.singletonList("t1_fault_event"));
        directory.put("lifecycleStages", Collections.singletonList("FAULT"));

        Map<String, Object> simplifiedAttrs = map();
        simplifiedAttrs.put("chapterId", "chapter-fault");

        Map<String, Object> simplified = map();
        simplified.put("chapterId", "chapter-fault");
        simplified.put("sourceTable", "t1_fault_event");
        simplified.put("sourceRecordId", "fault-001");
        simplified.put("sourceRecordKey", "32-HYD-PULSE-01");
        simplified.put("lifecycleStage", "FAULT");
        simplified.put("itemName", "fault event");
        simplified.put("contentSummary", "RESOLVED");
        simplified.put("completenessStatus", "complete");
        simplified.put("attrs", simplifiedAttrs);

        Map<String, Object> detailedAttrs = map();
        detailedAttrs.put("actualSourceTable", "t1_fault_event");
        detailedAttrs.put("faultCode", "32-HYD-PULSE-01");
        detailedAttrs.put("faultDescription", "pressure pulse at hydraulic tube port");
        detailedAttrs.put("status", "RESOLVED");
        detailedAttrs.put("resolutionType", "seal support replaced");

        Map<String, Object> detailed = map();
        detailed.put("sourceTable", "t1_fault_event");
        detailed.put("sourceRecordId", "fault-001");
        detailed.put("sourceRecordKey", "32-HYD-PULSE-01");
        detailed.put("lifecycleStage", "FAULT");
        detailed.put("itemName", "hydraulic tube fault closure");
        detailed.put("contentSummary", "pressure pulse closed after seal support replaced");
        detailed.put("completenessStatus", "complete");
        detailed.put("attrs", detailedAttrs);

        List<Map<String, Object>> rows = castRows(contentRows.invoke(service, directory,
                new ArrayList<>(Arrays.asList(simplified, detailed))));

        assertEquals(1, rows.size());
        assertTrue(String.valueOf(rows.get(0).get("value")).contains("pressure pulse"));
        assertTrue(String.valueOf(rows.get(0).get("value")).contains("seal support replaced"));
        assertNotEquals("RESOLVED", rows.get(0).get("value"));
    }

    @Test
    void traceResultsKeepFullWritebackFieldsForProjectThreeAndFour() throws Exception
    {
        DossierDetailServiceImpl service = new DossierDetailServiceImpl();
        Method buildTraceResults = DossierDetailServiceImpl.class.getDeclaredMethod("buildTraceResults",
                List.class, List.class);
        buildTraceResults.setAccessible(true);

        Map<String, Object> row = map();
        row.put("resultId", "result-project3-001");
        row.put("resultType", "fault_diagnosis");
        row.put("resultTitle", "Hydraulic tube diagnosis");
        row.put("resultSummary", "Pressure pulse root cause found");
        row.put("resultValueJson", "{\"faultCode\":\"32-HYD-PULSE-01\",\"confidence\":\"0.96\",\"advice\":\"replace support gasket\"}");
        row.put("evidenceJson", "[{\"name\":\"pressure curve\"}]");
        row.put("sourceComponent", "project3");
        row.put("createdAt", "2026-07-08 09:30:00");

        List<Map<String, Object>> results = castRows(buildTraceResults.invoke(service,
                Collections.singletonList(row), Collections.emptyList()));

        assertEquals(1, results.size());
        Map<String, Object> result = results.get(0);
        assertEquals("project3", result.get("sourceComponent"));
        assertEquals(0, result.get("attachmentCount"));
        assertTrue(String.valueOf(result.get("fullContent")).contains("32-HYD-PULSE-01"));
        assertTrue(String.valueOf(result.get("fullContent")).contains("replace support gasket"));
        assertTrue(castRows(result.get("fields")).stream()
                .anyMatch(field -> "faultCode".equals(field.get("label"))
                        && String.valueOf(field.get("value")).contains("32-HYD-PULSE-01")));
    }

    @Test
    void traceResultsShowQualityProblemTaskRows() throws Exception
    {
        DossierDetailServiceImpl service = new DossierDetailServiceImpl();
        Method buildTraceResults = DossierDetailServiceImpl.class.getDeclaredMethod("buildTraceResults",
                List.class, List.class);
        buildTraceResults.setAccessible(true);

        Map<String, Object> row = map();
        row.put("problemId", 12L);
        row.put("problemCode", "QF-20260615-19986");
        row.put("problemTitle", "Hydraulic system piston pump fault");
        row.put("problemDescription", "Main landing gear hydraulic fault was traced by quality center.");
        row.put("problemStatus", "FINISHED");
        row.put("severity", "MAJOR");
        row.put("taskId", 41L);
        row.put("moduleCode", "PROJECT_3");
        row.put("moduleName", "Project 3");
        row.put("taskStatus", "CONFIRMED");
        row.put("dispatchOpinion", "Analyze manufacturing and service fields.");
        row.put("processResult", "Pressure fluctuation source was confirmed.");
        row.put("processFile", "/profile/qms/report/project3.docx");
        row.put("submitTime", "2026-07-08 11:20:00");

        List<Map<String, Object>> results = castRows(buildTraceResults.invoke(service,
                Collections.singletonList(row), Collections.emptyList()));

        assertEquals(1, results.size());
        Map<String, Object> result = results.get(0);
        assertEquals("quality-task-41", result.get("resultId"));
        assertEquals("Project 3", result.get("sourceLabel"));
        assertEquals("追溯任务", result.get("resultTypeLabel"));
        assertTrue(String.valueOf(result.get("title")).contains("QF-20260615-19986"));
        assertTrue(String.valueOf(result.get("summary")).contains("Pressure fluctuation"));
        assertTrue(String.valueOf(result.get("fullContent")).contains("Main landing gear hydraulic fault"));
        assertTrue(castRows(result.get("fields")).stream()
                .anyMatch(field -> "处理文件".equals(field.get("label"))
                        && String.valueOf(field.get("value")).contains("project3.docx")));
    }

    @Test
    void nodePayloadShowsQualityTraceWhenQualityCenterSelectedCurrentNode() throws Exception
    {
        DossierDetailServiceImpl service = new DossierDetailServiceImpl();
        setDetailMapper(service, detailMapperReturning(Collections.emptyList()));
        Method buildNodePayload = DossierDetailServiceImpl.class.getDeclaredMethod("buildNodePayload",
                Map.class, Map.class, Map.class);
        buildNodePayload.setAccessible(true);

        Map<String, Object> instance = map();
        instance.put("instanceId", "instance-1");
        instance.put("aircraftId", "aircraft-1");

        Map<String, Object> version = map();
        version.put("versionId", "version-1");

        Map<String, Object> node = map();
        node.put("nodeId", "safe-node");
        node.put("parentId", "parent-node");
        node.put("partNumber", "B-1234-HYD-EQ040");
        node.put("partName", "Landing gear hydraulic relief valve");
        node.put("positionCode", "POS-HYD-00389");
        node.put("objectLevel", "part");

        Map<String, Object> payload = castRow(buildNodePayload.invoke(service, instance, version, node));

        List<Map<String, Object>> traceResults = castRows(payload.get("traceResults"));
        assertEquals(1, traceResults.size());
        assertEquals("quality-task-41", traceResults.get(0).get("resultId"));
    }

    @Test
    void nodePayloadEnablesDemoTraceAggregationOnlyForDemoPart() throws Exception
    {
        DossierDetailServiceImpl service = new DossierDetailServiceImpl();
        List<Boolean> demoTraceFlags = new ArrayList<>();
        setDetailMapper(service, detailMapperCapturingDemoTraceFlag(demoTraceFlags));
        Method buildNodePayload = DossierDetailServiceImpl.class.getDeclaredMethod("buildNodePayload",
                Map.class, Map.class, Map.class);
        buildNodePayload.setAccessible(true);

        Map<String, Object> instance = map();
        instance.put("instanceId", "instance-1");
        instance.put("aircraftId", "aircraft-1");

        Map<String, Object> version = map();
        version.put("versionId", "version-1");

        Map<String, Object> demoNode = map();
        demoNode.put("nodeId", "f1000006-0006-4006-8006-000000000006");
        demoNode.put("partNumber", "HYD-TUBE-MLG-32A");
        demoNode.put("partName", "main landing gear hydraulic tube");
        demoNode.put("objectLevel", "part");

        Map<String, Object> otherNode = map();
        otherNode.put("nodeId", "other-node");
        otherNode.put("partNumber", "B-1234-HYD-EQ040");
        otherNode.put("partName", "other hydraulic node");
        otherNode.put("objectLevel", "part");

        buildNodePayload.invoke(service, instance, version, demoNode);
        buildNodePayload.invoke(service, instance, version, otherNode);

        assertEquals(2, demoTraceFlags.size());
        assertTrue(demoTraceFlags.get(0));
        assertFalse(demoTraceFlags.get(1));
    }

    @Test
    void traceResultWordIncludesFullContentAndAttachments() throws Exception
    {
        DossierDetailServiceImpl service = new DossierDetailServiceImpl();
        Method buildTraceResultWord = DossierDetailServiceImpl.class.getDeclaredMethod("buildTraceResultWord",
                Map.class);
        buildTraceResultWord.setAccessible(true);

        Map<String, Object> result = map();
        result.put("resultId", "result-project5-001");
        result.put("sourceLabel", "Project 5");
        result.put("resultTypeLabel", "Traceability");
        result.put("title", "Hydraulic tube trace report");
        result.put("summary", "Trace report summary");
        result.put("fullContent", "complete trace report body");
        result.put("createdAt", "2026-07-08 10:00:00");
        Map<String, Object> document = map();
        document.put("title", "trace-report.docx");
        result.put("documents", Collections.singletonList(document));

        byte[] bytes = (byte[]) buildTraceResultWord.invoke(service, result);

        try (XWPFDocument documentFile = new XWPFDocument(new ByteArrayInputStream(bytes)))
        {
            String paragraphText = documentFile.getParagraphs().stream()
                    .map(paragraph -> paragraph.getText())
                    .reduce("", (left, right) -> left + "\n" + right);
            String tableText = documentFile.getTables().stream()
                    .flatMap(table -> table.getRows().stream())
                    .flatMap(row -> row.getTableCells().stream())
                    .map(cell -> cell.getText())
                    .reduce("", (left, right) -> left + "\n" + right);
            String text = paragraphText + "\n" + tableText;
            assertTrue(text.contains("Hydraulic tube trace report"));
            assertTrue(text.contains("complete trace report body"));
            assertTrue(text.contains("trace-report.docx"));
        }
    }

    @Test
    void contentHashChangesWhenTemplateSourcesChange() throws Exception
    {
        DossierGenerationServiceImpl service = new DossierGenerationServiceImpl();
        Field mapperField = DossierGenerationServiceImpl.class.getDeclaredField("generationMapper");
        mapperField.setAccessible(true);
        mapperField.set(service, Proxy.newProxyInstance(DossierGenerationMapper.class.getClassLoader(),
                new Class<?>[] { DossierGenerationMapper.class }, (proxy, method, args) -> {
                    if ("selectGenerationDataFingerprint".equals(method.getName()))
                    {
                        return Collections.emptyMap();
                    }
                    if ("selectAnalysisResultRows".equals(method.getName()))
                    {
                        return Collections.emptyList();
                    }
                    return null;
                }));

        Method buildContentHash = DossierGenerationServiceImpl.class.getDeclaredMethod("buildContentHash", Map.class,
                Map.class, Map.class, Map.class);
        buildContentHash.setAccessible(true);

        Map<String, Object> aircraft = map();
        aircraft.put("aircraftId", "aircraft-1");
        Map<String, Object> template = map();
        template.put("templateId", "template-1");
        template.put("templateCode", "TPL");
        template.put("templateVersion", "V1.0");
        Map<String, Object> instance = map();
        instance.put("instanceId", "instance-1");

        Map<String, Object> firstPrepareData = prepareDataWithSourceFilter("{\"status\":\"CLOSED\"}");
        Map<String, Object> secondPrepareData = prepareDataWithSourceFilter("{\"order_code\":\"WO-HYD\"}");

        String firstHash = (String) buildContentHash.invoke(service, aircraft, template, firstPrepareData, instance);
        String secondHash = (String) buildContentHash.invoke(service, aircraft, template, secondPrepareData, instance);

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    void snapshotRowKeyTreatsTemplateAndAutoSourcesAsSameRecord() throws Exception
    {
        DossierGenerationServiceImpl service = new DossierGenerationServiceImpl();
        Method snapshotRowKey = DossierGenerationServiceImpl.class.getDeclaredMethod("snapshotRowKey", Object.class,
                Object.class, Object.class, String.class);
        snapshotRowKey.setAccessible(true);

        String templateKey = (String) snapshotRowKey.invoke(service, "inspection_record",
                "IR-HYD-TUBE-001", "INSPECTION", "chapter-inspection");
        String automaticKey = (String) snapshotRowKey.invoke(service, "t1_inspection_record",
                "IR-HYD-TUBE-001", "INSPECTION", "");

        assertEquals(templateKey, automaticKey);
    }

    @Test
    void templateSourceContentRowsKeepT1TablePrefix() throws Exception
    {
        DossierGenerationServiceImpl service = new DossierGenerationServiceImpl();
        Method toTemplateSourceContentRow = DossierGenerationServiceImpl.class.getDeclaredMethod(
                "toTemplateSourceContentRow", Map.class, Map.class, String.class, Map.class, String.class);
        toTemplateSourceContentRow.setAccessible(true);

        Map<String, Object> source = map();
        source.put("sourceId", "source-shop-order");
        source.put("chapterId", "chapter-manufacturing");
        source.put("sourceCode", "SRC-PART-SHOP-ORDER-HYD");
        source.put("sourceTable", "t1_shop_order");
        source.put("sourceName", "制造工单");
        source.put("lifecycleStage", "MANUFACTURING");

        Map<String, Object> chapter = map();
        chapter.put("chapterId", "chapter-manufacturing");
        chapter.put("chapterCode", "PART_TRACE");
        chapter.put("chapterName", "制造追溯");

        Map<String, Object> raw = map();
        raw.put("id", "shop-order-1");
        raw.put("order_code", "WO-HYD-001");
        raw.put("status", "CLOSED");

        Map<String, Object> row = castRow(toTemplateSourceContentRow.invoke(service, source, chapter,
                "t1_shop_order", raw, "manufacturing"));

        assertEquals("t1_shop_order", row.get("sourceTable"));
    }

    @Test
    void sourceTraceKeepsT1TablePrefixForDossierTables() throws Exception
    {
        DossierGenerationServiceImpl service = new DossierGenerationServiceImpl();
        Method sourceTrace = DossierGenerationServiceImpl.class.getDeclaredMethod("sourceTrace", Object.class,
                Object.class, Object.class, Object.class);
        sourceTrace.setAccessible(true);

        Map<String, Object> trace = castRow(sourceTrace.invoke(service, "CONFIG", "aircraft_bom_node",
                "node-1", "AC-B1234"));

        assertEquals("t1_aircraft_bom_node", trace.get("sourceTable"));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> castRows(Object value)
    {
        return (List<Map<String, Object>>) value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castRow(Object value)
    {
        return (Map<String, Object>) value;
    }

    private static Map<String, Object> map()
    {
        return new LinkedHashMap<>();
    }

    private static void setDetailMapper(DossierDetailServiceImpl service, DossierDetailMapper mapper) throws Exception
    {
        Field field = DossierDetailServiceImpl.class.getDeclaredField("detailMapper");
        field.setAccessible(true);
        field.set(service, mapper);
    }

    private static DossierDetailMapper detailMapperReturning(List<Map<String, Object>> contentItems)
    {
        return (DossierDetailMapper) Proxy.newProxyInstance(DossierDetailMapper.class.getClassLoader(),
                new Class<?>[] { DossierDetailMapper.class }, (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("selectContentItems".equals(methodName))
                    {
                        return contentItems;
                    }
                    if ("selectQualityTraceRows".equals(methodName))
                    {
                        return Collections.singletonList(qualityTraceRow());
                    }
                    if ("selectDocuments".equals(methodName) || "selectBomPath".equals(methodName)
                            || "selectStructureBomChildren".equals(methodName))
                    {
                        return Collections.emptyList();
                    }
                    if ("selectStructureNodeByBom".equals(methodName))
                    {
                        return null;
                    }
                    if ("toString".equals(methodName))
                    {
                        return "DossierDetailMapper test proxy";
                    }
                    return null;
                });
    }

    private static DossierDetailMapper detailMapperCapturingDemoTraceFlag(List<Boolean> demoTraceFlags)
    {
        return (DossierDetailMapper) Proxy.newProxyInstance(DossierDetailMapper.class.getClassLoader(),
                new Class<?>[] { DossierDetailMapper.class }, (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("selectContentItems".equals(methodName) || "selectDocuments".equals(methodName)
                            || "selectBomPath".equals(methodName) || "selectStructureBomChildren".equals(methodName))
                    {
                        return Collections.emptyList();
                    }
                    if ("selectStructureNodeByBom".equals(methodName))
                    {
                        return null;
                    }
                    if ("selectQualityTraceRows".equals(methodName))
                    {
                        demoTraceFlags.add(args != null && args.length > 1 && Boolean.TRUE.equals(args[1]));
                        return Collections.emptyList();
                    }
                    if ("toString".equals(methodName))
                    {
                        return "DossierDetailMapper demo trace flag test proxy";
                    }
                    return null;
                });
    }

    private static Map<String, Object> qualityTraceRow()
    {
        Map<String, Object> row = map();
        row.put("problemId", 12L);
        row.put("problemCode", "QF-20260615-19986");
        row.put("problemTitle", "Landing gear hydraulic problem");
        row.put("problemStatus", "FINISHED");
        row.put("taskId", 41L);
        row.put("moduleCode", "PROJECT_3");
        row.put("moduleName", "Project 3");
        row.put("taskStatus", "CONFIRMED");
        row.put("processResult", "Quality task result");
        return row;
    }

    private static Map<String, Object> prepareDataWithSourceFilter(String filterConditionJson)
    {
        Map<String, Object> chapter = map();
        chapter.put("chapterId", "chapter-manufacturing");
        chapter.put("chapterCode", "PART_TRACE");
        chapter.put("chapterName", "制造追溯");
        chapter.put("attrsJson", "{\"displayType\":\"timeline_files\"}");

        Map<String, Object> source = map();
        source.put("sourceId", "source-shop-order");
        source.put("chapterId", "chapter-manufacturing");
        source.put("sourceCode", "SRC-PART-SHOP-ORDER-HYD");
        source.put("sourceTable", "t1_shop_order");
        source.put("sourceName", "液压弯管制造工单");
        source.put("lifecycleStage", "MANUFACTURING");
        source.put("joinConditionJson", "{}");
        source.put("filterConditionJson", filterConditionJson);
        source.put("sortOrder", 46);

        Map<String, Object> prepareData = map();
        prepareData.put("keyNodeChain", Collections.emptyList());
        prepareData.put("chapters", Collections.singletonList(chapter));
        prepareData.put("sources", Collections.singletonList(source));
        return prepareData;
    }
}
