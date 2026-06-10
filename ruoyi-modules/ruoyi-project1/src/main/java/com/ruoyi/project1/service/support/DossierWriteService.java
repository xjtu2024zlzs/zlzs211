package com.ruoyi.project1.service.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import com.ruoyi.common.core.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Safe writer for CF dossier target tables.
 */
@Service
public class DossierWriteService
{
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z0-9_]+");
    private static final String DOSSIER_PREFIX = "p1p_dossier_";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public WriteResult write(String targetTable, Map<String, Object> targetData, String sourcePk, String sourceOperation)
    {
        String physicalTable = normalizeTargetTable(targetTable);
        Map<String, ColumnInfo> columns = loadColumns(physicalTable);
        if (columns.isEmpty())
        {
            return WriteResult.failed("目标表不存在或无可写字段: " + physicalTable);
        }

        Map<String, Object> prepared = prepareTargetData(physicalTable, targetData, columns);
        Map<String, Object> writable = filterWritableData(prepared, columns);
        if (writable.isEmpty())
        {
            return WriteResult.skipped("转换后的目标数据没有命中目标表字段");
        }

        try
        {
            List<String> matchedKey = findMatchedBusinessKey(physicalTable, writable);
            if (matchedKey.isEmpty())
            {
                matchedKey = findMatchedUniqueKey(physicalTable, writable);
            }
            if (!matchedKey.isEmpty() && existsByKey(physicalTable, matchedKey, writable))
            {
                int updated = updateByKey(physicalTable, matchedKey, writable);
                return updated > 0
                        ? WriteResult.updated(buildTargetPk(matchedKey, writable))
                        : WriteResult.skipped("唯一键存在，但没有可更新字段");
            }

            insertRow(physicalTable, writable);
            return WriteResult.inserted(defaultString(sourcePk, buildInsertedPkHint(physicalTable, writable)));
        }
        catch (DataAccessException e)
        {
            return WriteResult.failed(e.getMostSpecificCause() == null ? e.getMessage() : e.getMostSpecificCause().getMessage());
        }
    }

    private String normalizeTargetTable(String targetTable)
    {
        if (isBlank(targetTable))
        {
            throw new ServiceException("目标表不能为空");
        }
        String normalized = targetTable.trim();
        if (!normalized.startsWith(DOSSIER_PREFIX))
        {
            normalized = DOSSIER_PREFIX + normalized;
        }
        validateIdentifier(normalized);
        if (!normalized.startsWith(DOSSIER_PREFIX))
        {
            throw new ServiceException("目标表必须以 " + DOSSIER_PREFIX + " 开头");
        }
        return normalized;
    }

    private Map<String, ColumnInfo> loadColumns(String tableName)
    {
        String sql = "select column_name, data_type, extra from information_schema.columns "
                + "where table_schema = database() and table_name = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, tableName);
        Map<String, ColumnInfo> columns = new LinkedHashMap<>();
        for (Map<String, Object> row : rows)
        {
            String name = String.valueOf(row.get("column_name"));
            String dataType = String.valueOf(row.get("data_type"));
            String extra = row.get("extra") == null ? "" : String.valueOf(row.get("extra"));
            columns.put(name, new ColumnInfo(name, dataType, extra.toLowerCase(Locale.ROOT).contains("auto_increment")));
        }
        return columns;
    }

    private Map<String, Object> prepareTargetData(String tableName, Map<String, Object> targetData,
            Map<String, ColumnInfo> columns)
    {
        Map<String, Object> prepared = new LinkedHashMap<>();
        if (targetData != null)
        {
            prepared.putAll(targetData);
        }

        deriveRequiredDemoFields(tableName, prepared);
        resolveReferenceValues(prepared, columns);
        return prepared;
    }

    static void deriveRequiredDemoFields(String tableName, Map<String, Object> targetData)
    {
        if (targetData == null)
        {
            return;
        }
        if (tableName.endsWith("installed_position"))
        {
            Object positionCode = targetData.get("position_code");
            if (isEmptyValueStatic(targetData.get("position_name")) && !isEmptyValueStatic(positionCode))
            {
                targetData.put("position_name", String.valueOf(positionCode).trim() + " Position");
            }
        }
        if (tableName.endsWith("quality_event"))
        {
            Object triggerSourceNo = targetData.get("trigger_source_no");
            if (isEmptyValueStatic(targetData.get("event_no")) && !isEmptyValueStatic(triggerSourceNo))
            {
                targetData.put("event_no", "QE-" + String.valueOf(triggerSourceNo).trim());
            }
            targetData.putIfAbsent("event_source", "MRO");
            targetData.putIfAbsent("event_type", "MRO");
        }
    }

    private void resolveReferenceValues(Map<String, Object> targetData, Map<String, ColumnInfo> columns)
    {
        for (Map.Entry<String, Object> entry : new ArrayList<>(targetData.entrySet()))
        {
            ColumnInfo columnInfo = columns.get(entry.getKey());
            if (columnInfo == null || columnInfo.autoIncrement || !columnInfo.isIntegerType()
                    || isEmptyValue(entry.getValue()))
            {
                continue;
            }

            Long resolved = resolveReferenceValue(entry.getKey(), entry.getValue(), targetData);
            if (resolved != null)
            {
                targetData.put(entry.getKey(), resolved);
            }
        }
    }

    private Long resolveReferenceValue(String column, Object value, Map<String, Object> row)
    {
        if ("component_instance_id".equals(column))
        {
            return resolveOrCreateComponentInstance(value);
        }
        if ("installed_position_id".equals(column))
        {
            return resolveOrCreateInstalledPosition(value);
        }
        if ("part_instance_id".equals(column) || "removed_part_instance_id".equals(column)
                || "installed_part_instance_id".equals(column))
        {
            return resolveOrCreatePartInstance(value);
        }
        if ("maintenance_event_id".equals(column))
        {
            return resolveOrCreateMaintenanceEvent(value, row);
        }
        if ("maintenance_order_id".equals(column))
        {
            return resolveOrCreateMaintenanceOrder(value, row);
        }
        if ("replacement_record_id".equals(column))
        {
            return resolveOrCreateReplacementRecord(value, row);
        }
        if ("quality_event_id".equals(column))
        {
            return resolveOrCreateQualityEvent(value, row);
        }
        if ("inventory_batch_id".equals(column))
        {
            return resolveOrCreateInventoryBatch(value);
        }
        return parseLong(value);
    }

    private Map<String, Object> filterWritableData(Map<String, Object> targetData, Map<String, ColumnInfo> columns)
    {
        Map<String, Object> writable = new LinkedHashMap<>();
        if (targetData == null)
        {
            return writable;
        }

        for (Map.Entry<String, Object> entry : targetData.entrySet())
        {
            if (isBlank(entry.getKey()))
            {
                continue;
            }
            String column = entry.getKey().trim();
            validateIdentifier(column);
            ColumnInfo columnInfo = columns.get(column);
            if (columnInfo == null || (columnInfo.autoIncrement && isEmptyValue(entry.getValue())))
            {
                continue;
            }
            if (!isEmptyValue(entry.getValue()))
            {
                writable.put(column, entry.getValue());
            }
        }
        return writable;
    }

    private Long resolveOrCreateComponentInstance(Object value)
    {
        Long numeric = parseLong(value);
        if (numeric != null)
        {
            return ensureComponentInstance(numeric, "LGACT-2026-" + pad4(numeric));
        }

        String serialNo = clean(value);
        Long existing = findIdByNaturalKey("p1p_dossier_component_instance", "component_instance_id",
                "component_serial_no", serialNo);
        if (existing != null)
        {
            return existing;
        }

        Long componentTypeId = firstId("p1p_dossier_component_type", "component_type_id");
        if (componentTypeId == null)
        {
            return null;
        }
        try
        {
            jdbcTemplate.update("insert into `p1p_dossier_component_instance` "
                    + "(`component_type_id`, `component_serial_no`, `component_status`, `created_at`, `updated_at`) "
                    + "values (?, ?, ?, now(), now())", componentTypeId, serialNo, "NORMAL");
        }
        catch (DataAccessException ignored)
        {
            // Another row may have created the same natural key; query again below.
        }
        return findIdByNaturalKey("p1p_dossier_component_instance", "component_instance_id",
                "component_serial_no", serialNo);
    }

    private Long ensureComponentInstance(Long componentInstanceId, String serialNo)
    {
        if (existsById("p1p_dossier_component_instance", "component_instance_id", componentInstanceId))
        {
            return componentInstanceId;
        }
        Long componentTypeId = firstId("p1p_dossier_component_type", "component_type_id");
        if (componentTypeId == null)
        {
            return null;
        }
        try
        {
            jdbcTemplate.update("insert into `p1p_dossier_component_instance` "
                    + "(`component_instance_id`, `component_type_id`, `component_serial_no`, `component_status`, `created_at`, `updated_at`) "
                    + "values (?, ?, ?, ?, now(), now())", componentInstanceId, componentTypeId, serialNo, "NORMAL");
        }
        catch (DataAccessException ignored)
        {
            // Keep idempotent for repeated demo executions.
        }
        return existsById("p1p_dossier_component_instance", "component_instance_id", componentInstanceId)
                ? componentInstanceId : null;
    }

    private Long resolveOrCreateInstalledPosition(Object value)
    {
        Long numeric = parseLong(value);
        if (numeric != null && existsById("p1p_dossier_installed_position", "installed_position_id", numeric))
        {
            return numeric;
        }

        String positionCode = numeric == null ? clean(value) : "POS-LG-" + String.format(Locale.ROOT, "%02d", ((numeric - 1) % 6) + 1);
        Long existing = findIdByNaturalKey("p1p_dossier_installed_position", "installed_position_id",
                "position_code", positionCode);
        if (existing != null)
        {
            return existing;
        }

        try
        {
            jdbcTemplate.update("insert into `p1p_dossier_installed_position` "
                    + "(`position_code`, `position_name`, `created_at`, `updated_at`) values (?, ?, now(), now())",
                    positionCode, positionCode + " Position");
        }
        catch (DataAccessException ignored)
        {
            // Idempotent fallback; query below handles races or duplicate manual data.
        }
        return findIdByNaturalKey("p1p_dossier_installed_position", "installed_position_id",
                "position_code", positionCode);
    }

    private Long resolveOrCreatePartInstance(Object value)
    {
        Long numeric = parseLong(value);
        if (numeric != null)
        {
            return ensurePartInstance(numeric, "PIN-2026-" + pad4(numeric));
        }

        String partSerialNo = clean(value);
        Long existing = findIdByNaturalKey("p1p_dossier_part_instance", "part_instance_id",
                "part_serial_no", partSerialNo);
        if (existing != null)
        {
            return existing;
        }

        Long partDefinitionId = firstId("p1p_dossier_part_definition", "part_definition_id");
        if (partDefinitionId == null)
        {
            return null;
        }
        try
        {
            jdbcTemplate.update("insert into `p1p_dossier_part_instance` "
                    + "(`part_definition_id`, `part_serial_no`, `source_type`, `part_status`) values (?, ?, ?, ?)",
                    partDefinitionId, partSerialNo, "MRO", "NORMAL");
        }
        catch (DataAccessException ignored)
        {
            // Query again below.
        }
        return findIdByNaturalKey("p1p_dossier_part_instance", "part_instance_id",
                "part_serial_no", partSerialNo);
    }

    private Long ensurePartInstance(Long partInstanceId, String partSerialNo)
    {
        if (existsById("p1p_dossier_part_instance", "part_instance_id", partInstanceId))
        {
            return partInstanceId;
        }
        Long partDefinitionId = firstId("p1p_dossier_part_definition", "part_definition_id");
        if (partDefinitionId == null)
        {
            return null;
        }
        try
        {
            jdbcTemplate.update("insert into `p1p_dossier_part_instance` "
                    + "(`part_instance_id`, `part_definition_id`, `part_serial_no`, `source_type`, `part_status`) "
                    + "values (?, ?, ?, ?, ?)", partInstanceId, partDefinitionId, partSerialNo, "MRO", "NORMAL");
        }
        catch (DataAccessException ignored)
        {
            // Keep idempotent.
        }
        return existsById("p1p_dossier_part_instance", "part_instance_id", partInstanceId) ? partInstanceId : null;
    }

    private Long resolveOrCreateInventoryBatch(Object value)
    {
        Long numeric = parseLong(value);
        if (numeric != null && existsById("p1p_dossier_inventory_batch", "inventory_batch_id", numeric))
        {
            return numeric;
        }

        String batchNo = numeric == null ? clean(value) : "INV-CF-" + pad4(numeric);
        Long existing = findIdByNaturalKey("p1p_dossier_inventory_batch", "inventory_batch_id",
                "inventory_batch_no", batchNo);
        if (existing != null)
        {
            return existing;
        }

        Long materialId = firstId("p1p_dossier_material", "material_id");
        if (materialId == null)
        {
            return null;
        }
        try
        {
            jdbcTemplate.update("insert into `p1p_dossier_inventory_batch` "
                    + "(`material_id`, `inventory_batch_no`, `batch_status`) values (?, ?, ?)",
                    materialId, batchNo, "NORMAL");
        }
        catch (DataAccessException ignored)
        {
            // Query again below.
        }
        return findIdByNaturalKey("p1p_dossier_inventory_batch", "inventory_batch_id",
                "inventory_batch_no", batchNo);
    }

    private Long resolveOrCreateMaintenanceEvent(Object value, Map<String, Object> row)
    {
        Long numeric = parseLong(value);
        if (numeric != null && existsById("p1p_dossier_maintenance_event", "maintenance_event_id", numeric))
        {
            return numeric;
        }

        String eventNo = numeric == null ? clean(value) : "MRO-EVT-" + pad4(numeric);
        Long existing = findIdByNaturalKey("p1p_dossier_maintenance_event", "maintenance_event_id",
                "event_no", eventNo);
        if (existing != null)
        {
            return existing;
        }

        Long componentInstanceId = parseLong(row.get("component_instance_id"));
        if (componentInstanceId == null || !existsById("p1p_dossier_component_instance", "component_instance_id", componentInstanceId))
        {
            componentInstanceId = firstId("p1p_dossier_component_instance", "component_instance_id");
        }
        if (componentInstanceId == null)
        {
            componentInstanceId = resolveOrCreateComponentInstance("LGACT-2026-0001");
        }
        try
        {
            if (numeric == null)
            {
                jdbcTemplate.update("insert into `p1p_dossier_maintenance_event` "
                        + "(`component_instance_id`, `event_no`, `event_type`, `event_status`) values (?, ?, ?, ?)",
                        componentInstanceId, eventNo, "MRO", "NORMAL");
            }
            else
            {
                jdbcTemplate.update("insert into `p1p_dossier_maintenance_event` "
                        + "(`maintenance_event_id`, `component_instance_id`, `event_no`, `event_type`, `event_status`) "
                        + "values (?, ?, ?, ?, ?)", numeric, componentInstanceId, eventNo, "MRO", "NORMAL");
            }
        }
        catch (DataAccessException ignored)
        {
            // Query again below.
        }
        return numeric != null && existsById("p1p_dossier_maintenance_event", "maintenance_event_id", numeric)
                ? numeric
                : findIdByNaturalKey("p1p_dossier_maintenance_event", "maintenance_event_id", "event_no", eventNo);
    }

    private Long resolveOrCreateMaintenanceOrder(Object value, Map<String, Object> row)
    {
        Long numeric = parseLong(value);
        if (numeric != null && existsById("p1p_dossier_maintenance_order", "maintenance_order_id", numeric))
        {
            return numeric;
        }

        String orderNo = numeric == null ? clean(value) : "RO-CF-" + pad4(numeric);
        Long existing = findIdByNaturalKey("p1p_dossier_maintenance_order", "maintenance_order_id",
                "maintenance_order_no", orderNo);
        if (existing != null)
        {
            return existing;
        }

        Long maintenanceEventId = resolveOrCreateMaintenanceEvent(defaultStringObject(row.get("maintenance_event_id"), numeric), row);
        if (maintenanceEventId == null)
        {
            return null;
        }
        try
        {
            if (numeric == null)
            {
                jdbcTemplate.update("insert into `p1p_dossier_maintenance_order` "
                        + "(`maintenance_event_id`, `maintenance_order_no`, `order_status`) values (?, ?, ?)",
                        maintenanceEventId, orderNo, "NORMAL");
            }
            else
            {
                jdbcTemplate.update("insert into `p1p_dossier_maintenance_order` "
                        + "(`maintenance_order_id`, `maintenance_event_id`, `maintenance_order_no`, `order_status`) "
                        + "values (?, ?, ?, ?)", numeric, maintenanceEventId, orderNo, "NORMAL");
            }
        }
        catch (DataAccessException ignored)
        {
            // Query again below.
        }
        return numeric != null && existsById("p1p_dossier_maintenance_order", "maintenance_order_id", numeric)
                ? numeric
                : findIdByNaturalKey("p1p_dossier_maintenance_order", "maintenance_order_id", "maintenance_order_no", orderNo);
    }

    private Long resolveOrCreateReplacementRecord(Object value, Map<String, Object> row)
    {
        Long numeric = parseLong(value);
        if (numeric != null && existsById("p1p_dossier_replacement_record", "replacement_record_id", numeric))
        {
            return numeric;
        }

        String replacementNo = numeric == null ? clean(value) : "REP-CF-" + pad4(numeric);
        Long existing = findIdByNaturalKey("p1p_dossier_replacement_record", "replacement_record_id",
                "replacement_no", replacementNo);
        if (existing != null)
        {
            return existing;
        }

        Long maintenanceEventId = resolveOrCreateMaintenanceEvent(defaultStringObject(row.get("maintenance_event_id"), numeric), row);
        if (maintenanceEventId == null)
        {
            return null;
        }
        try
        {
            jdbcTemplate.update("insert into `p1p_dossier_replacement_record` "
                    + "(`maintenance_event_id`, `replacement_no`, `replacement_status`) values (?, ?, ?)",
                    maintenanceEventId, replacementNo, "NORMAL");
        }
        catch (DataAccessException ignored)
        {
            // Query again below.
        }
        return findIdByNaturalKey("p1p_dossier_replacement_record", "replacement_record_id",
                "replacement_no", replacementNo);
    }

    private Long resolveOrCreateQualityEvent(Object value, Map<String, Object> row)
    {
        Long numeric = parseLong(value);
        if (numeric != null && existsById("p1p_dossier_quality_event", "quality_event_id", numeric))
        {
            return numeric;
        }

        String eventNo = numeric == null ? clean(value) : "QE-CF-2026-" + pad4(numeric);
        Long existing = findIdByNaturalKey("p1p_dossier_quality_event", "quality_event_id", "event_no", eventNo);
        if (existing != null)
        {
            return existing;
        }

        try
        {
            jdbcTemplate.update("insert into `p1p_dossier_quality_event` "
                    + "(`event_no`, `event_source`, `event_type`, `event_status`, `trigger_source_no`) "
                    + "values (?, ?, ?, ?, ?)", eventNo, "MRO", "MRO", "NORMAL", clean(row.get("feedback_no")));
        }
        catch (DataAccessException ignored)
        {
            // Query again below.
        }
        return findIdByNaturalKey("p1p_dossier_quality_event", "quality_event_id", "event_no", eventNo);
    }

    private List<List<String>> loadUniqueKeys(String tableName)
    {
        String sql = "select index_name, seq_in_index, column_name "
                + "from information_schema.statistics "
                + "where table_schema = database() and table_name = ? and non_unique = 0 "
                + "order by case when index_name = 'PRIMARY' then 0 else 1 end, index_name, seq_in_index";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, tableName);
        Map<String, List<String>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> row : rows)
        {
            String indexName = String.valueOf(row.get("index_name"));
            String column = String.valueOf(row.get("column_name"));
            grouped.computeIfAbsent(indexName, key -> new ArrayList<>()).add(column);
        }
        return new ArrayList<>(grouped.values());
    }

    private List<String> findMatchedUniqueKey(String tableName, Map<String, Object> writable)
    {
        for (List<String> keyColumns : loadUniqueKeys(tableName))
        {
            boolean matched = true;
            for (String keyColumn : keyColumns)
            {
                if (isEmptyValue(writable.get(keyColumn)))
                {
                    matched = false;
                    break;
                }
            }
            if (matched)
            {
                return keyColumns;
            }
        }
        return new ArrayList<>();
    }

    private List<String> findMatchedBusinessKey(String tableName, Map<String, Object> writable)
    {
        if (tableName.endsWith("installed_position") && !isEmptyValue(writable.get("position_code")))
        {
            return singleKey("position_code");
        }
        return new ArrayList<>();
    }

    private List<String> singleKey(String key)
    {
        List<String> keys = new ArrayList<>();
        keys.add(key);
        return keys;
    }

    private boolean existsByKey(String tableName, List<String> keyColumns, Map<String, Object> writable)
    {
        StringBuilder sql = new StringBuilder("select count(1) from ");
        sql.append(quote(tableName)).append(" where ");
        List<Object> params = new ArrayList<>();
        appendWhere(sql, params, keyColumns, writable);
        Long count = jdbcTemplate.queryForObject(sql.toString(), params.toArray(), Long.class);
        return count != null && count > 0;
    }

    private boolean existsById(String tableName, String idColumn, Long id)
    {
        if (id == null)
        {
            return false;
        }
        String sql = "select count(1) from " + quote(tableName) + " where " + quote(idColumn) + " = ?";
        Long count = jdbcTemplate.queryForObject(sql, new Object[] { id }, Long.class);
        return count != null && count > 0;
    }

    private Long findIdByNaturalKey(String tableName, String idColumn, String naturalColumn, String value)
    {
        if (isBlank(value))
        {
            return null;
        }
        String sql = "select " + quote(idColumn) + " from " + quote(tableName)
                + " where " + quote(naturalColumn) + " = ? order by " + quote(idColumn) + " limit 1";
        return firstLong(sql, value);
    }

    private Long firstId(String tableName, String idColumn)
    {
        String sql = "select " + quote(idColumn) + " from " + quote(tableName)
                + " order by " + quote(idColumn) + " limit 1";
        return firstLong(sql);
    }

    private Long firstLong(String sql, Object... args)
    {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args);
        if (rows.isEmpty())
        {
            return null;
        }
        Object value = rows.get(0).values().iterator().next();
        return parseLong(value);
    }

    private int updateByKey(String tableName, List<String> keyColumns, Map<String, Object> writable)
    {
        List<String> updateColumns = new ArrayList<>();
        for (String column : writable.keySet())
        {
            if (!keyColumns.contains(column))
            {
                updateColumns.add(column);
            }
        }
        if (updateColumns.isEmpty())
        {
            return 0;
        }

        StringBuilder sql = new StringBuilder("update ");
        sql.append(quote(tableName)).append(" set ");
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < updateColumns.size(); i++)
        {
            if (i > 0)
            {
                sql.append(", ");
            }
            String column = updateColumns.get(i);
            sql.append(quote(column)).append(" = ?");
            params.add(writable.get(column));
        }
        sql.append(" where ");
        appendWhere(sql, params, keyColumns, writable);
        return jdbcTemplate.update(sql.toString(), params.toArray());
    }

    private void insertRow(String tableName, Map<String, Object> writable)
    {
        StringBuilder sql = new StringBuilder("insert into ");
        sql.append(quote(tableName)).append(" (");
        List<Object> params = new ArrayList<>();
        int index = 0;
        for (String column : writable.keySet())
        {
            if (index++ > 0)
            {
                sql.append(", ");
            }
            sql.append(quote(column));
            params.add(writable.get(column));
        }
        sql.append(") values (");
        for (int i = 0; i < writable.size(); i++)
        {
            if (i > 0)
            {
                sql.append(", ");
            }
            sql.append("?");
        }
        sql.append(")");
        jdbcTemplate.update(sql.toString(), params.toArray());
    }

    private void appendWhere(StringBuilder sql, List<Object> params, List<String> keyColumns, Map<String, Object> writable)
    {
        for (int i = 0; i < keyColumns.size(); i++)
        {
            if (i > 0)
            {
                sql.append(" and ");
            }
            String column = keyColumns.get(i);
            sql.append(quote(column)).append(" = ?");
            params.add(writable.get(column));
        }
    }

    private String buildTargetPk(List<String> keyColumns, Map<String, Object> writable)
    {
        List<String> parts = new ArrayList<>();
        for (String keyColumn : keyColumns)
        {
            parts.add(keyColumn + "=" + writable.get(keyColumn));
        }
        return String.join(",", parts);
    }

    private String buildInsertedPkHint(String tableName, Map<String, Object> writable)
    {
        for (String column : writable.keySet())
        {
            if (column.endsWith("_code") || column.endsWith("_no") || column.endsWith("_id"))
            {
                return tableName + "." + column + "=" + writable.get(column);
            }
        }
        return tableName + ".inserted";
    }

    private String quote(String identifier)
    {
        validateIdentifier(identifier);
        return "`" + identifier + "`";
    }

    private void validateIdentifier(String identifier)
    {
        if (isBlank(identifier) || !IDENTIFIER.matcher(identifier).matches())
        {
            throw new ServiceException("非法数据库标识符: " + identifier);
        }
    }

    private boolean isEmptyValue(Object value)
    {
        return value == null || (value instanceof String && ((String) value).trim().isEmpty());
    }

    private static boolean isEmptyValueStatic(Object value)
    {
        return value == null || (value instanceof String && ((String) value).trim().isEmpty());
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    private String defaultString(String value, String defaultValue)
    {
        return isBlank(value) ? defaultValue : value;
    }

    private Object defaultStringObject(Object value, Object defaultValue)
    {
        return isEmptyValue(value) ? defaultValue : value;
    }

    private String clean(Object value)
    {
        return value == null ? null : String.valueOf(value).trim();
    }

    private Long parseLong(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty())
        {
            return null;
        }
        try
        {
            return Long.parseLong(text);
        }
        catch (NumberFormatException ignored)
        {
            return null;
        }
    }

    private String pad4(Long value)
    {
        return String.format(Locale.ROOT, "%04d", value);
    }

    private static class ColumnInfo
    {
        private final String name;
        private final String dataType;
        private final boolean autoIncrement;

        private ColumnInfo(String name, String dataType, boolean autoIncrement)
        {
            this.name = name;
            this.dataType = dataType == null ? "" : dataType.toLowerCase(Locale.ROOT);
            this.autoIncrement = autoIncrement;
        }

        private boolean isIntegerType()
        {
            return "bigint".equals(dataType) || "int".equals(dataType) || "integer".equals(dataType)
                    || "smallint".equals(dataType) || "tinyint".equals(dataType) || "mediumint".equals(dataType);
        }
    }

    public static class WriteResult
    {
        private final String status;
        private final String targetPk;
        private final String errorMessage;

        private WriteResult(String status, String targetPk, String errorMessage)
        {
            this.status = status;
            this.targetPk = targetPk;
            this.errorMessage = errorMessage;
        }

        public static WriteResult inserted(String targetPk)
        {
            return new WriteResult("inserted", targetPk, null);
        }

        public static WriteResult updated(String targetPk)
        {
            return new WriteResult("updated", targetPk, null);
        }

        public static WriteResult skipped(String errorMessage)
        {
            return new WriteResult("skipped", null, errorMessage);
        }

        public static WriteResult failed(String errorMessage)
        {
            return new WriteResult("failed", null, errorMessage);
        }

        public String getStatus()
        {
            return status;
        }

        public String getTargetPk()
        {
            return targetPk;
        }

        public String getErrorMessage()
        {
            return errorMessage;
        }

        public boolean isSuccess()
        {
            return "inserted".equals(status) || "updated".equals(status);
        }
    }
}
