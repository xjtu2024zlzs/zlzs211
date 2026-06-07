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

        Map<String, Object> writable = filterWritableData(targetData, columns);
        if (writable.isEmpty())
        {
            return WriteResult.skipped("转换后的目标数据没有命中目标表字段");
        }

        try
        {
            List<String> matchedKey = findMatchedUniqueKey(physicalTable, writable);
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
        String sql = "select column_name, extra from information_schema.columns "
                + "where table_schema = database() and table_name = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, tableName);
        Map<String, ColumnInfo> columns = new LinkedHashMap<>();
        for (Map<String, Object> row : rows)
        {
            String name = String.valueOf(row.get("column_name"));
            String extra = row.get("extra") == null ? "" : String.valueOf(row.get("extra"));
            columns.put(name, new ColumnInfo(name, extra.toLowerCase(Locale.ROOT).contains("auto_increment")));
        }
        return columns;
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

    private boolean existsByKey(String tableName, List<String> keyColumns, Map<String, Object> writable)
    {
        StringBuilder sql = new StringBuilder("select count(1) from ");
        sql.append(quote(tableName)).append(" where ");
        List<Object> params = new ArrayList<>();
        appendWhere(sql, params, keyColumns, writable);
        Long count = jdbcTemplate.queryForObject(sql.toString(), params.toArray(), Long.class);
        return count != null && count > 0;
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

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    private String defaultString(String value, String defaultValue)
    {
        return isBlank(value) ? defaultValue : value;
    }

    private static class ColumnInfo
    {
        private final String name;
        private final boolean autoIncrement;

        private ColumnInfo(String name, boolean autoIncrement)
        {
            this.name = name;
            this.autoIncrement = autoIncrement;
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
