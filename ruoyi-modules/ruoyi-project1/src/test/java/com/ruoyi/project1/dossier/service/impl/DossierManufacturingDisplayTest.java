package com.ruoyi.project1.dossier.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
