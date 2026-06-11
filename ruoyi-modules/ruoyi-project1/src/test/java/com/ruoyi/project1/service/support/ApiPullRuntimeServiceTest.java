package com.ruoyi.project1.service.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.ruoyi.project1.domain.AccessScopeTable;
import com.ruoyi.project1.domain.MappingSpec;
import org.junit.jupiter.api.Test;

class ApiPullRuntimeServiceTest
{
    @Test
    void deduplicatesScopeFieldMappingsAndKeepsFirstMapping()
    {
        MappingSpec first = mapping(101L, "event_no", "event_no");
        MappingSpec duplicate = mapping(102L, "event_no", "event_no");
        MappingSpec another = mapping(103L, "event_status", "event_status");

        List<MappingSpec> result = ApiPullRuntimeService.deduplicateScopeFieldMappings(
                Arrays.asList(first, duplicate, another));

        assertEquals(2, result.size());
        assertSame(first, result.get(0));
        assertSame(another, result.get(1));
    }

    @Test
    void groupsMultipleTargetScopesUnderTheSameSourceTable()
    {
        AccessScopeTable maintenanceEvent = scope(429L, "service_event", "maintenance_event");
        AccessScopeTable installedPosition = scope(430L, "service_event", "installed_position");
        AccessScopeTable maintenanceOrder = scope(431L, "repair_order", "maintenance_order");

        Map<String, List<AccessScopeTable>> result = ApiPullRuntimeService.groupScopeTablesBySourceTable(
                Arrays.asList(maintenanceEvent, installedPosition, maintenanceOrder));

        assertEquals(2, result.get("service_event").size());
        assertSame(maintenanceEvent, result.get("service_event").get(0));
        assertSame(installedPosition, result.get("service_event").get(1));
        assertSame(maintenanceOrder, result.get("repair_order").get(0));
    }

    @Test
    void enrichesQualityEventNoFromSourcePayload()
    {
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("quality_event_no", "QE-CF-2026-0001");
        raw.put("feedback_no", "FB-CF-0001");
        Map<String, Object> targetData = new LinkedHashMap<>();
        targetData.put("trigger_source_no", "FB-CF-0001");

        ApiPullRuntimeService.enrichTargetDataFromRaw("quality_event", raw, targetData);

        assertEquals("QE-CF-2026-0001", targetData.get("event_no"));
    }

    private MappingSpec mapping(Long mappingId, String sourceColumn, String targetColumn)
    {
        MappingSpec mapping = new MappingSpec();
        mapping.setMappingId(mappingId);
        mapping.setSourceColumn(sourceColumn);
        mapping.setTargetColumn(targetColumn);
        return mapping;
    }

    private AccessScopeTable scope(Long scopeTableId, String sourceTable, String targetTable)
    {
        AccessScopeTable scope = new AccessScopeTable();
        scope.setScopeTableId(scopeTableId);
        scope.setSourceTable(sourceTable);
        scope.setTargetTable(targetTable);
        return scope;
    }
}
