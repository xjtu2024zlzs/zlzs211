package com.ruoyi.project1.service.support;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.domain.AccessTableResult;
import com.ruoyi.project1.domain.MatchResultSet;
import com.ruoyi.project1.domain.MatchTask;

/**
 * Project1 demonstration data provider.
 *
 * The mock payloads are kept in the service layer so controllers remain standard
 * RuoYi entry points and can be switched to real business logic later.
 */
@Service
public class Project1MockDataService
{
    public List<Map<String, Object>> schemaRows(String accessMode)
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        if ("api_pull".equals(accessMode))
        {
            rows.add(schemaRow("equipment_asset", 18, "equipment_id, equipment_name, location_code", "API 返回设备资产实体"));
            rows.add(schemaRow("service_event", 16, "event_id, service_unit_code, fault_time", "API 返回维修事件实体"));
            rows.add(schemaRow("person_profile", 12, "person_id, person_name, department", "API 返回人员信息实体"));
            return rows;
        }
        if ("api_push".equals(accessMode))
        {
            rows.add(schemaRow("person_profile", 12, "person_id, person_name, department", "外部系统推送人员信息"));
            rows.add(schemaRow("quality_event", 14, "event_id, aircraft_no, quality_desc", "外部系统推送质量事件"));
            return rows;
        }
        rows.add(schemaRow("MES.equipment_asset", 24, "equipment_id, equipmentname, equipment_type", "源数据库设备资产表"));
        rows.add(schemaRow("ERP.inventory_lot", 21, "lot_id, wh_code, material_code", "源数据库库存批次表"));
        rows.add(schemaRow("MRO.service_event", 19, "event_id, service_unit_code, status", "源数据库维修事件表"));
        return rows;
    }

    public List<Map<String, Object>> taskVersions(MatchTask task)
    {
        int current = task.getCurrentVersionId() == null ? 1 : task.getCurrentVersionId().intValue();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = current; i >= 1; i--)
        {
            Map<String, Object> row = new HashMap<>();
            row.put("version", "v" + i);
            row.put("summary", i == current ? "当前任务配置" : "历史任务配置");
            row.put("createTime", task.getCreateTime());
            row.put("status", i == current ? "当前" : "历史");
            rows.add(row);
        }
        return rows;
    }

    public List<Map<String, Object>> taskRecords(MatchTask task)
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("record", "#1");
        row.put("status", task.getLastRecordStatus() == null ? "pending" : task.getLastRecordStatus());
        row.put("stage", task.getLastRecordStage() == null ? "等待运行" : task.getLastRecordStage());
        row.put("message", "无算法模式运行记录，后续可替换为真实算法或规则生成记录。");
        rows.add(row);
        return rows;
    }

    public Map<String, Object> matchMetrics(MatchResultSet resultSet)
    {
        Map<String, Object> payload = new HashMap<>();
        List<Map<String, Object>> cards = new ArrayList<>();
        cards.add(metricCard("MRR", resultSet.getAvgScore() == null ? "0.850" : formatScore(resultSet.getAvgScore())));
        cards.add(metricCard("Recall@20", "0.630"));
        cards.add(metricCard("All F1 Score", "0.653"));
        cards.add(metricCard("One2One 匹配数", resultSet.getTotalRows() == null ? 270L : resultSet.getTotalRows()));

        payload.put("cards", cards);
        payload.put("scoreDistribution", scoreDistribution());
        payload.put("targetDistribution", targetDistribution());
        return payload;
    }

    public List<Map<String, Object>> matchRows(Long resultSetId)
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(matchRow("MES.equipment_asset", "equipmentname", "equipment", "equipment_name", "1.00", "auto_approved", "系统自动"));
        rows.add(matchRow("ERP.inventory_lot", "wh_code", "inventory_batch", "warehouse_code", "1.00", "auto_approved", "系统自动"));
        rows.add(matchRow("MES.step_log", "stepsid", "step_execution", "step_no", "1.00", "approved", "张三"));
        rows.add(matchRow("MRO.service_event", "service_unit_code", "maintenance_event", "service_unit_code", "1.00", "approved", "张三"));
        rows.add(matchRow("PLM.gear_system_def", "position_code", "installed_position", "position_code", "0.86", "pending", "-"));
        return rows;
    }

    public List<AccessTableResult> defaultAccessResultRows(Long accessPlanId)
    {
        List<AccessTableResult> rows = new ArrayList<>();
        rows.add(accessResultRow(accessPlanId, 5L, "MRO.service_event", "maintenance_event", "success", 638L, 18L, 0L, 1218L));
        rows.add(accessResultRow(accessPlanId, 5L, "PLM.gear_system_def", "installed_position", "running", 388L, 8L, 0L, 388L));
        rows.add(accessResultRow(accessPlanId, 5L, "ERP.inventory_lot", "inventory_batch", "partial", 122L, 0L, 18L, 422L));
        return rows;
    }

    public Map<String, Object> accessDashboard(List<AccessTableResult> rows)
    {
        Map<String, Object> payload = new HashMap<>();
        List<AccessTableResult> effectiveRows = rows == null ? new ArrayList<>() : rows;
        payload.put("recentBatches", recentBatches(effectiveRows));
        payload.put("topTables", topTables(effectiveRows));
        payload.put("batchOptions", batchOptions(effectiveRows));
        return payload;
    }

    private Map<String, Object> schemaRow(String entityName, Integer fieldCount, String sampleFields, String remark)
    {
        Map<String, Object> row = new HashMap<>();
        row.put("entityName", entityName);
        row.put("fieldCount", fieldCount);
        row.put("sampleFields", sampleFields);
        row.put("remark", remark);
        return row;
    }

    private Map<String, Object> metricCard(String label, Object value)
    {
        Map<String, Object> row = new HashMap<>();
        row.put("label", label);
        row.put("value", value);
        return row;
    }

    private String formatScore(BigDecimal score)
    {
        return score.setScale(3, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private List<Map<String, Object>> scoreDistribution()
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(namedValue("score = 1.00", 194L));
        rows.add(namedValue("0.90 - 0.99", 49L));
        rows.add(namedValue("0.80 - 0.89", 18L));
        rows.add(namedValue("< 0.80", 9L));
        return rows;
    }

    private List<Map<String, Object>> targetDistribution()
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(namedValue("iqs_failure_content", 12L));
        rows.add(namedValue("iqs_failure", 11L));
        rows.add(namedValue("quality_event", 9L));
        rows.add(namedValue("disposition_record", 9L));
        return rows;
    }

    private Map<String, Object> namedValue(String label, Long value)
    {
        Map<String, Object> row = new HashMap<>();
        row.put("label", label);
        row.put("value", value);
        return row;
    }

    private Map<String, Object> matchRow(String sourceTable, String sourceColumn, String targetTable, String targetColumn, String score, String reviewStatus, String reviewer)
    {
        Map<String, Object> row = new HashMap<>();
        row.put("sourceTable", sourceTable);
        row.put("sourceColumn", sourceColumn);
        row.put("targetTable", targetTable);
        row.put("targetColumn", targetColumn);
        row.put("score", score);
        row.put("reviewStatus", reviewStatus);
        row.put("reviewer", reviewer);
        return row;
    }

    private AccessTableResult accessResultRow(Long accessPlanId, Long batchId, String sourceTable, String targetTable, String status, Long inserted, Long updated, Long failed, Long totalSuccess)
    {
        Long success = value(inserted) + value(updated);
        AccessTableResult row = new AccessTableResult();
        row.setAccessPlanId(accessPlanId);
        row.setAccessBatchId(batchId);
        row.setSourceTable(sourceTable);
        row.setTargetTable(targetTable);
        row.setResultStatus(status);
        row.setReadCount(success + value(failed));
        row.setStagedCount(success + value(failed));
        row.setInsertedCount(inserted);
        row.setUpdatedCount(updated);
        row.setSuccessCount(success);
        row.setFailedCount(failed);
        row.setTotalSuccessCount(totalSuccess);
        row.setTotalFailedCount(failed);
        row.setMessage("按最终接入规则完成源端读取、中间库暂存、格式转换和目标表写入统计。");
        return row;
    }

    private List<Map<String, Object>> recentBatches(List<AccessTableResult> rows)
    {
        Map<Long, long[]> aggregates = new LinkedHashMap<>();
        for (AccessTableResult row : rows)
        {
            if (row.getAccessBatchId() == null)
            {
                continue;
            }
            long[] counts = aggregates.computeIfAbsent(row.getAccessBatchId(), key -> new long[3]);
            counts[0] += value(row.getInsertedCount());
            counts[1] += value(row.getUpdatedCount());
            counts[2] += value(row.getFailedCount());
        }
        List<Long> batchIds = new ArrayList<>(aggregates.keySet());
        batchIds.sort(Long::compareTo);
        int from = Math.max(0, batchIds.size() - 5);
        List<Map<String, Object>> batches = new ArrayList<>();
        for (Long batchId : batchIds.subList(from, batchIds.size()))
        {
            long[] counts = aggregates.get(batchId);
            batches.add(batchRow(batchId, counts[0], counts[1], counts[2]));
        }
        return batches;
    }

    private Map<String, Object> batchRow(Long batchId, Long inserted, Long updated, Long failed)
    {
        Map<String, Object> row = new HashMap<>();
        row.put("batchId", batchId);
        row.put("batchName", "BATCH-" + String.format("%03d", batchId));
        row.put("insertedCount", inserted);
        row.put("updatedCount", updated);
        row.put("failedCount", failed);
        return row;
    }

    private List<Map<String, Object>> topTables(List<AccessTableResult> rows)
    {
        Map<String, Long> totals = new LinkedHashMap<>();
        for (AccessTableResult row : rows)
        {
            totals.put(row.getTargetTable(), totals.getOrDefault(row.getTargetTable(), 0L) + value(row.getSuccessCount()));
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : totals.entrySet())
        {
            result.add(namedValue(entry.getKey(), entry.getValue()));
        }
        result.sort((a, b) -> Long.compare(((Number) b.get("value")).longValue(), ((Number) a.get("value")).longValue()));
        return result.size() > 10 ? result.subList(0, 10) : result;
    }

    private List<Map<String, Object>> batchOptions(List<AccessTableResult> rows)
    {
        return recentBatches(rows);
    }

    private long value(Long value)
    {
        return value == null ? 0L : value;
    }
}
