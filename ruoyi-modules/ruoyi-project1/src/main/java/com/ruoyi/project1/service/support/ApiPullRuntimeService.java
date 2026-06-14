package com.ruoyi.project1.service.support;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.project1.domain.AccessBatch;
import com.ruoyi.project1.domain.AccessFieldResult;
import com.ruoyi.project1.domain.AccessPlan;
import com.ruoyi.project1.domain.AccessScopeField;
import com.ruoyi.project1.domain.AccessScopeTable;
import com.ruoyi.project1.domain.AccessStageRecord;
import com.ruoyi.project1.domain.AccessTableResult;
import com.ruoyi.project1.domain.AccessTransformRecord;
import com.ruoyi.project1.domain.ApiPullDatasource;
import com.ruoyi.project1.domain.Datasource;
import com.ruoyi.project1.domain.MappingSpec;
import com.ruoyi.project1.domain.SchemaEntity;
import com.ruoyi.project1.domain.SchemaField;
import com.ruoyi.project1.domain.SchemaSnapshot;
import com.ruoyi.project1.mapper.AccessBatchMapper;
import com.ruoyi.project1.mapper.AccessFieldResultMapper;
import com.ruoyi.project1.mapper.AccessPlanMapper;
import com.ruoyi.project1.mapper.AccessScopeFieldMapper;
import com.ruoyi.project1.mapper.AccessScopeTableMapper;
import com.ruoyi.project1.mapper.AccessStageRecordMapper;
import com.ruoyi.project1.mapper.AccessTableResultMapper;
import com.ruoyi.project1.mapper.AccessTransformRecordMapper;
import com.ruoyi.project1.mapper.DatasourceMapper;
import com.ruoyi.project1.mapper.MappingSpecMapper;
import com.ruoyi.project1.mapper.SchemaEntityMapper;
import com.ruoyi.project1.mapper.SchemaFieldMapper;
import com.ruoyi.project1.mapper.SchemaSnapshotMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runtime integration for CF API Pull external systems.
 */
@Service
public class ApiPullRuntimeService
{
    private static final DateTimeFormatter BATCH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {};

    @Autowired private ExternalApiPullClient apiPullClient;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private DatasourceMapper datasourceMapper;
    @Autowired private SchemaSnapshotMapper schemaSnapshotMapper;
    @Autowired private SchemaEntityMapper schemaEntityMapper;
    @Autowired private SchemaFieldMapper schemaFieldMapper;
    @Autowired private AccessPlanMapper accessPlanMapper;
    @Autowired private AccessBatchMapper accessBatchMapper;
    @Autowired private AccessScopeTableMapper accessScopeTableMapper;
    @Autowired private AccessScopeFieldMapper accessScopeFieldMapper;
    @Autowired private AccessStageRecordMapper accessStageRecordMapper;
    @Autowired private AccessTransformRecordMapper accessTransformRecordMapper;
    @Autowired private AccessTableResultMapper accessTableResultMapper;
    @Autowired private AccessFieldResultMapper accessFieldResultMapper;
    @Autowired private MappingSpecMapper mappingSpecMapper;
    @Autowired private DossierWriteService dossierWriteService;
    @Autowired private PlatformTransactionManager transactionManager;

    public boolean canCallApiPull(ApiPullDatasource detail)
    {
        return detail != null
                && !isBlank(detail.getBaseUrl())
                && endpointMatches(detail.getSchemaEndpoint(), "/api/schema")
                && endpointMatches(detail.getPullEndpoint(), "/api/pull");
    }

    public Map<String, Object> testConnection(Datasource datasource, ApiPullDatasource detail)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        try
        {
            Map<String, Object> health = apiPullClient.health(detail);
            boolean ok = "ok".equalsIgnoreCase(asString(health.get("status")));
            datasource.setConnectionStatus(ok ? "success" : "failed");
            datasource.setLastTestMessage(ok ? "数据源连接正常，已具备模式读取和数据接入条件。" : "API Pull 健康检查未通过");
            result.put("health", health);
        }
        catch (RuntimeException e)
        {
            datasource.setConnectionStatus("failed");
            datasource.setLastTestMessage("API Pull 连接失败: " + e.getMessage());
            result.put("errorMessage", e.getMessage());
        }

        datasource.setLastTestTime(DateUtils.getNowDate());
        datasource.setUpdateTime(DateUtils.getNowDate());
        datasourceMapper.updateDatasource(datasource);

        result.put("datasourceId", datasource.getDatasourceId());
        result.put("connectionStatus", datasource.getConnectionStatus());
        result.put("lastTestTime", datasource.getLastTestTime());
        result.put("lastTestMessage", datasource.getLastTestMessage());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> readSchema(Datasource datasource, ApiPullDatasource detail)
    {
        try
        {
            Map<String, Object> payload = apiPullClient.schema(detail);
            SchemaSnapshot snapshot = persistSchemaPayload(datasource, payload);

            datasource.setLatestSchemaSnapshotId(snapshot.getSnapshotId());
            datasource.setConnectionStatus("success");
            datasource.setLastTestTime(DateUtils.getNowDate());
            datasource.setLastTestMessage("API Pull 模式读取完成");
            datasource.setUpdateTime(DateUtils.getNowDate());
            datasourceMapper.updateDatasource(datasource);

            Map<String, Object> result = buildStoredSchemaPayload(datasource);
            result.put("alreadyRead", false);
            result.put("message", "模式读取完成");
            return result;
        }
        catch (RuntimeException e)
        {
            datasource.setConnectionStatus("failed");
            datasource.setLastTestTime(DateUtils.getNowDate());
            datasource.setLastTestMessage("API Pull 模式读取失败: " + e.getMessage());
            datasource.setUpdateTime(DateUtils.getNowDate());
            datasourceMapper.updateDatasource(datasource);
            throw e;
        }
    }

    public Map<String, Object> buildStoredSchemaPayload(Datasource datasource)
    {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("datasourceId", datasource.getDatasourceId());
        payload.put("datasourceName", datasource.getDatasourceName());
        payload.put("accessMode", datasource.getAccessMode());
        payload.put("latestSchemaSnapshotId", datasource.getLatestSchemaSnapshotId());

        List<Map<String, Object>> rows = new ArrayList<>();
        if (datasource.getLatestSchemaSnapshotId() != null)
        {
            SchemaEntity query = new SchemaEntity();
            query.setSnapshotId(datasource.getLatestSchemaSnapshotId());
            for (SchemaEntity entity : schemaEntityMapper.selectSchemaEntityList(query))
            {
                SchemaField fieldQuery = new SchemaField();
                fieldQuery.setEntityId(entity.getEntityId());
                List<SchemaField> fields = schemaFieldMapper.selectSchemaFieldList(fieldQuery);
                List<String> sampleFields = new ArrayList<>();
                for (SchemaField field : fields)
                {
                    sampleFields.add(field.getFieldName());
                    if (sampleFields.size() >= 5)
                    {
                        break;
                    }
                }

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("entityName", entity.getEntityName());
                row.put("fieldCount", fields.size());
                row.put("sampleFields", String.join(", ", sampleFields));
                row.put("remark", entity.getSourceTable());
                rows.add(row);
            }
        }
        payload.put("rows", rows);
        return payload;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> executeAccessPlan(AccessPlan plan, Datasource datasource, ApiPullDatasource detail)
    {
        if (plan.getSpecSetId() == null)
        {
            throw new ServiceException("接入计划缺少最终接入规则集，不能执行 API Pull");
        }

        AccessBatch batch = createAccessBatch(plan);
        try
        {
            List<AccessScopeTable> scopeTables = ensureAccessScopes(plan, datasource);
            Map<String, List<AccessScopeTable>> scopesBySourceTable = groupScopeTablesBySourceTable(scopeTables);

            Map<String, Object> requestPayload = buildPullRequest(batch, scopeTables);
            Map<String, Object> pullPayload = apiPullClient.pull(detail, requestPayload);
            List<Map<String, Object>> pulledTables = mapList(pullPayload.get("tables"));

            long readCount = 0L;
            long stagedCount = 0L;
            long transformSuccessCount = 0L;
            long transformFailedCount = 0L;
            long writeSuccessCount = 0L;
            long writeFailedCount = 0L;
            long insertedCount = 0L;
            long updatedCount = 0L;
            int tableCount = 0;

            for (Map<String, Object> pulledTable : pulledTables)
            {
                String sourceTable = asString(pulledTable.get("source_table"));
                List<AccessScopeTable> matchingScopes = scopesBySourceTable.get(sourceTable);
                if (matchingScopes == null || matchingScopes.isEmpty())
                {
                    continue;
                }

                List<Map<String, Object>> records = mapList(pulledTable.get("records"));
                readCount += records.size();
                for (AccessScopeTable scopeTable : matchingScopes)
                {
                    List<AccessStageRecord> stageRecords = persistStageRecords(plan, datasource, batch, scopeTable, records);
                    TableExecutionStats stats = processTable(plan, batch, scopeTable, stageRecords);
                    stagedCount += stageRecords.size();
                    transformSuccessCount += stats.transformSuccessCount;
                    transformFailedCount += stats.transformFailedCount;
                    writeSuccessCount += stats.writeSuccessCount;
                    writeFailedCount += stats.writeFailedCount;
                    insertedCount += stats.insertedCount;
                    updatedCount += stats.updatedCount;
                    tableCount++;
                }
            }

            batch.setBatchStatus(writeFailedCount > 0L || transformFailedCount > 0L ? "partial" : "success");
            batch.setReadCount(readCount);
            batch.setStagedCount(stagedCount);
            batch.setTransformSuccessCount(transformSuccessCount);
            batch.setTransformFailedCount(transformFailedCount);
            batch.setWriteSuccessCount(writeSuccessCount);
            batch.setWriteFailedCount(writeFailedCount);
            batch.setInsertedCount(insertedCount);
            batch.setUpdatedCount(updatedCount);
            batch.setFinishedAt(DateUtils.getNowDate());
            batch.setUpdateTime(DateUtils.getNowDate());
            accessBatchMapper.updateAccessBatch(batch);

            plan.setCurrentBatchId(batch.getAccessBatchId());
            plan.setUseStatus("enabled");
            plan.setLastSuccessCount(writeSuccessCount);
            plan.setLastFailedCount(writeFailedCount + transformFailedCount);
            plan.setTotalSuccessCount(totalPlanSuccess(plan.getAccessPlanId()));
            plan.setTotalFailedCount(totalPlanFailed(plan.getAccessPlanId()));
            plan.setLastExecuteTime(DateUtils.getNowDate());
            plan.setUpdateTime(DateUtils.getNowDate());
            accessPlanMapper.updateAccessPlan(plan);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("accessPlanId", plan.getAccessPlanId());
            result.put("batchId", batch.getAccessBatchId());
            result.put("batchNo", batch.getBatchNo());
            result.put("readCount", readCount);
            result.put("stagedCount", stagedCount);
            result.put("transformSuccessCount", transformSuccessCount);
            result.put("transformFailedCount", transformFailedCount);
            result.put("writeSuccessCount", writeSuccessCount);
            result.put("writeFailedCount", writeFailedCount);
            result.put("insertedCount", insertedCount);
            result.put("updatedCount", updatedCount);
            result.put("tableCount", tableCount);
            result.put("externalBatchId", pullPayload.get("external_batch_id"));
            result.put("message", "API Pull 接入执行完成，数据已写入接入中间库并按最终规则写入卷宗库");
            return result;
        }
        catch (RuntimeException e)
        {
            markBatchFailed(batch, e);
            throw e;
        }
    }

    static Map<String, List<AccessScopeTable>> groupScopeTablesBySourceTable(List<AccessScopeTable> scopeTables)
    {
        Map<String, List<AccessScopeTable>> grouped = new LinkedHashMap<>();
        for (AccessScopeTable scopeTable : scopeTables)
        {
            grouped.computeIfAbsent(scopeTable.getSourceTable(), key -> new ArrayList<>()).add(scopeTable);
        }
        return grouped;
    }

    private SchemaSnapshot persistSchemaPayload(Datasource datasource, Map<String, Object> payload)
    {
        List<Map<String, Object>> entities = mapList(payload.get("entities"));
        if (entities.isEmpty())
        {
            throw new ServiceException("API Pull 模式响应缺少 entities");
        }

        long fieldCount = 0L;
        for (Map<String, Object> entity : entities)
        {
            fieldCount += mapList(entity.get("fields")).size();
        }

        SchemaSnapshot snapshot = new SchemaSnapshot();
        snapshot.setDatasourceId(datasource.getDatasourceId());
        snapshot.setSnapshotName(datasource.getDatasourceName() + " API Pull 模式快照");
        snapshot.setReadStatus("success");
        snapshot.setEntityCount((long) entities.size());
        snapshot.setFieldCount(fieldCount);
        snapshot.setSchemaJson(toJson(payload));
        snapshot.setReadBy("system");
        snapshot.setReadTime(DateUtils.getNowDate());
        snapshot.setCreateTime(DateUtils.getNowDate());
        schemaSnapshotMapper.insertSchemaSnapshot(snapshot);

        Map<String, Object> database = asMap(payload.get("database"));
        String databaseName = asString(database.get("database_name"));
        String schemaName = asString(database.get("schema_name"));

        for (Map<String, Object> entityPayload : entities)
        {
            SchemaEntity entity = new SchemaEntity();
            entity.setSnapshotId(snapshot.getSnapshotId());
            entity.setDatasourceId(datasource.getDatasourceId());
            entity.setEntityName(asString(entityPayload.get("entity_name")));
            entity.setSourceDatabase(databaseName);
            entity.setSourceSchema(schemaName);
            entity.setSourceTable(asString(entityPayload.get("source_table")));
            entity.setEntityType("table");
            entity.setRowCount(asLong(entityPayload.get("row_count")));
            entity.setMetadata(toJson(Collections.singletonMap("foreign_keys", entityPayload.get("foreign_keys"))));
            entity.setCreateTime(DateUtils.getNowDate());
            schemaEntityMapper.insertSchemaEntity(entity);

            for (Map<String, Object> fieldPayload : mapList(entityPayload.get("fields")))
            {
                SchemaField field = new SchemaField();
                field.setEntityId(entity.getEntityId());
                field.setSnapshotId(snapshot.getSnapshotId());
                field.setFieldName(asString(fieldPayload.get("field_name")));
                field.setSourceColumn(asString(fieldPayload.get("source_column")));
                field.setDataType(asString(fieldPayload.get("data_type")));
                field.setOrdinalPosition(asLong(fieldPayload.get("ordinal_position")));
                field.setIsPrimaryKey(asBoolean(fieldPayload.get("is_primary_key")) ? 1 : 0);
                field.setIsNullable(asBoolean(fieldPayload.get("is_nullable")) ? 1 : 0);
                field.setSampleValues(toJson(fieldPayload.get("sample_values")));
                field.setCreateTime(DateUtils.getNowDate());
                schemaFieldMapper.insertSchemaField(field);
            }
        }
        return snapshot;
    }

    private AccessBatch createAccessBatch(AccessPlan plan)
    {
        AccessBatch batch = new AccessBatch();
        batch.setAccessPlanId(plan.getAccessPlanId());
        batch.setBatchNo("API-" + plan.getAccessPlanId() + "-" + LocalDateTime.now().format(BATCH_FORMATTER));
        batch.setTriggerType("manual");
        batch.setBatchStatus("running");
        batch.setReadCount(0L);
        batch.setStagedCount(0L);
        batch.setTransformSuccessCount(0L);
        batch.setTransformFailedCount(0L);
        batch.setWriteSuccessCount(0L);
        batch.setWriteFailedCount(0L);
        batch.setInsertedCount(0L);
        batch.setUpdatedCount(0L);
        batch.setStartedAt(DateUtils.getNowDate());
        batch.setCreateTime(DateUtils.getNowDate());
        requiresNewTransaction().execute(status -> {
            accessBatchMapper.insertAccessBatch(batch);
            return null;
        });
        return batch;
    }

    private void markBatchFailed(AccessBatch batch, RuntimeException e)
    {
        requiresNewTransaction().execute(status -> {
            Date now = DateUtils.getNowDate();
            AccessBatch failedBatch = new AccessBatch();
            failedBatch.setAccessBatchId(batch.getAccessBatchId());
            failedBatch.setBatchStatus("failed");
            failedBatch.setErrorMessage(truncateErrorMessage(e.getMessage()));
            failedBatch.setFinishedAt(now);
            failedBatch.setUpdateTime(now);
            accessBatchMapper.updateAccessBatch(failedBatch);

            AccessPlan failedPlan = new AccessPlan();
            failedPlan.setAccessPlanId(batch.getAccessPlanId());
            failedPlan.setCurrentBatchId(batch.getAccessBatchId());
            failedPlan.setLastExecuteTime(now);
            failedPlan.setUpdateTime(now);
            accessPlanMapper.updateAccessPlan(failedPlan);
            return null;
        });
    }

    private TransactionTemplate requiresNewTransaction()
    {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template;
    }

    private String truncateErrorMessage(String message)
    {
        if (message == null)
        {
            return null;
        }
        return message.length() > 2000 ? message.substring(0, 2000) : message;
    }

    private List<AccessScopeTable> ensureAccessScopes(AccessPlan plan, Datasource datasource)
    {
        AccessScopeTable query = new AccessScopeTable();
        query.setAccessPlanId(plan.getAccessPlanId());
        List<AccessScopeTable> existing = accessScopeTableMapper.selectAccessScopeTableList(query);
        if (!existing.isEmpty())
        {
            return existing;
        }

        MappingSpec specQuery = new MappingSpec();
        specQuery.setSpecSetId(plan.getSpecSetId());
        List<MappingSpec> specs = mappingSpecMapper.selectMappingSpecList(specQuery);
        if (specs.isEmpty())
        {
            throw new ServiceException("最终接入规则集没有字段规则，不能生成接入范围");
        }

        Map<String, List<MappingSpec>> grouped = new LinkedHashMap<>();
        for (MappingSpec spec : specs)
        {
            String key = spec.getSourceTable() + "->" + spec.getTargetTable();
            grouped.computeIfAbsent(key, item -> new ArrayList<>()).add(spec);
        }

        List<AccessScopeTable> scopeTables = new ArrayList<>();
        for (List<MappingSpec> mappings : grouped.values())
        {
            List<MappingSpec> fieldMappings = deduplicateScopeFieldMappings(mappings);
            MappingSpec first = mappings.get(0);
            AccessScopeTable scopeTable = new AccessScopeTable();
            scopeTable.setAccessPlanId(plan.getAccessPlanId());
            scopeTable.setSpecSetId(plan.getSpecSetId());
            scopeTable.setSourceDatasourceId(datasource.getDatasourceId());
            scopeTable.setSourceDatabase(first.getSourceDatabase());
            scopeTable.setSourceTable(first.getSourceTable());
            scopeTable.setTargetTable(first.getTargetTable());
            scopeTable.setFieldMappingCount((long) fieldMappings.size());
            scopeTable.setScopeStatus("active");
            scopeTable.setCreateTime(DateUtils.getNowDate());
            accessScopeTableMapper.insertAccessScopeTable(scopeTable);
            scopeTables.add(scopeTable);

            for (MappingSpec mapping : fieldMappings)
            {
                AccessScopeField scopeField = new AccessScopeField();
                scopeField.setScopeTableId(scopeTable.getScopeTableId());
                scopeField.setAccessPlanId(plan.getAccessPlanId());
                scopeField.setMappingId(mapping.getMappingId());
                scopeField.setSourceColumn(mapping.getSourceColumn());
                scopeField.setTargetColumn(mapping.getTargetColumn());
                scopeField.setMappingType(mapping.getMappingType());
                scopeField.setTransformRule(mapping.getTransformRule());
                scopeField.setFieldStatus("active");
                scopeField.setCreateTime(DateUtils.getNowDate());
                accessScopeFieldMapper.insertAccessScopeField(scopeField);
            }
        }
        return scopeTables;
    }

    static List<MappingSpec> deduplicateScopeFieldMappings(List<MappingSpec> mappings)
    {
        Map<String, MappingSpec> unique = new LinkedHashMap<>();
        for (MappingSpec mapping : mappings)
        {
            String key = String.valueOf(mapping.getSourceColumn()) + '\u0001' + String.valueOf(mapping.getTargetColumn());
            unique.putIfAbsent(key, mapping);
        }
        return new ArrayList<>(unique.values());
    }

    static void enrichTargetDataFromRaw(String targetTable, Map<String, Object> raw, Map<String, Object> targetData)
    {
        if ("quality_event".equals(targetTable)
                && isEmptyObject(targetData.get("event_no"))
                && !isEmptyObject(raw.get("quality_event_no")))
        {
            targetData.put("event_no", raw.get("quality_event_no"));
        }
    }

    private static boolean isEmptyObject(Object value)
    {
        return value == null || (value instanceof String && ((String) value).trim().isEmpty());
    }

    private Map<String, Object> buildPullRequest(AccessBatch batch, List<AccessScopeTable> scopeTables)
    {
        Map<String, List<String>> columnsBySourceTable = new LinkedHashMap<>();
        for (AccessScopeTable scopeTable : scopeTables)
        {
            AccessScopeField fieldQuery = new AccessScopeField();
            fieldQuery.setScopeTableId(scopeTable.getScopeTableId());
            List<String> columns = columnsBySourceTable.computeIfAbsent(scopeTable.getSourceTable(), key -> new ArrayList<>());
            for (AccessScopeField field : accessScopeFieldMapper.selectAccessScopeFieldList(fieldQuery))
            {
                if (!isBlank(field.getSourceColumn()) && !columns.contains(field.getSourceColumn()))
                {
                    columns.add(field.getSourceColumn());
                }
            }
        }

        List<Map<String, Object>> tables = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : columnsBySourceTable.entrySet())
        {
            Map<String, Object> table = new LinkedHashMap<>();
            table.put("source_table", entry.getKey());
            table.put("columns", entry.getValue());
            tables.add(table);
        }

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("request_id", batch.getBatchNo());
        request.put("limit_per_table", 500);
        request.put("tables", tables);
        return request;
    }

    private List<AccessStageRecord> persistStageRecords(AccessPlan plan, Datasource datasource, AccessBatch batch,
            AccessScopeTable scopeTable, List<Map<String, Object>> records)
    {
        List<AccessStageRecord> rows = new ArrayList<>();
        Date now = DateUtils.getNowDate();
        for (Map<String, Object> recordPayload : records)
        {
            AccessStageRecord record = new AccessStageRecord();
            record.setAccessBatchId(batch.getAccessBatchId());
            record.setAccessPlanId(plan.getAccessPlanId());
            record.setScopeTableId(scopeTable.getScopeTableId());
            record.setSourceDatasourceId(datasource.getDatasourceId());
            record.setSourceDatabase(scopeTable.getSourceDatabase());
            record.setSourceTable(scopeTable.getSourceTable());
            record.setSourcePk(asString(recordPayload.get("source_pk")));
            record.setSourceOperation(defaultIfBlank(asString(recordPayload.get("source_operation")), "upsert"));
            record.setRawData(toJson(recordPayload.get("raw_data")));
            record.setReadStatus("success");
            record.setReadTime(now);
            record.setCreateTime(now);
            accessStageRecordMapper.insertAccessStageRecord(record);
            rows.add(record);
        }
        return rows;
    }

    private TableExecutionStats processTable(AccessPlan plan, AccessBatch batch, AccessScopeTable scopeTable,
            List<AccessStageRecord> stageRecords)
    {
        AccessScopeField fieldQuery = new AccessScopeField();
        fieldQuery.setScopeTableId(scopeTable.getScopeTableId());
        List<AccessScopeField> fields = accessScopeFieldMapper.selectAccessScopeFieldList(fieldQuery);
        if (fields.isEmpty())
        {
            throw new ServiceException("接入范围缺少字段映射: " + scopeTable.getSourceTable());
        }

        TableExecutionStats stats = new TableExecutionStats();
        Map<Long, FieldStats> fieldStats = new LinkedHashMap<>();
        for (AccessScopeField field : fields)
        {
            fieldStats.put(field.getScopeFieldId(), new FieldStats(field));
        }

        for (AccessStageRecord stageRecord : stageRecords)
        {
            Map<String, Object> raw = parseRawData(stageRecord.getRawData());
            Map<String, Object> targetData = new LinkedHashMap<>();
            String transformError = null;

            for (AccessScopeField field : fields)
            {
                Object value = raw.get(field.getSourceColumn());
                FieldStats currentFieldStats = fieldStats.get(field.getScopeFieldId());
                if (value == null)
                {
                    currentFieldStats.failedCount++;
                    currentFieldStats.errorMessage = "源字段缺少值";
                }
                else if (!isBlank(field.getTargetColumn()))
                {
                    targetData.put(field.getTargetColumn(), value);
                    currentFieldStats.successCount++;
                }
            }

            enrichTargetDataFromRaw(scopeTable.getTargetTable(), raw, targetData);

            if (targetData.isEmpty())
            {
                transformError = "没有可写入目标表的字段";
            }

            AccessTransformRecord transform = new AccessTransformRecord();
            transform.setStageRecordId(stageRecord.getStageRecordId());
            transform.setAccessBatchId(batch.getAccessBatchId());
            transform.setAccessPlanId(plan.getAccessPlanId());
            transform.setScopeTableId(scopeTable.getScopeTableId());
            transform.setTargetTable(scopeTable.getTargetTable());
            transform.setTransformedData(toJson(targetData));
            transform.setTransformStatus(transformError == null ? "success" : "failed");
            transform.setWriteStatus(transformError == null ? "pending" : "skipped");
            transform.setErrorMessage(transformError);
            transform.setTransformedAt(DateUtils.getNowDate());
            transform.setCreateTime(DateUtils.getNowDate());
            accessTransformRecordMapper.insertAccessTransformRecord(transform);

            if (transformError != null)
            {
                stats.transformFailedCount++;
                stats.writeFailedCount++;
                continue;
            }

            stats.transformSuccessCount++;
            DossierWriteService.WriteResult writeResult = dossierWriteService.write(scopeTable.getTargetTable(), targetData,
                    stageRecord.getSourcePk(), stageRecord.getSourceOperation());
            transform.setWriteStatus(writeResult.getStatus());
            transform.setTargetPk(writeResult.getTargetPk());
            transform.setErrorMessage(writeResult.getErrorMessage());
            transform.setWrittenAt(DateUtils.getNowDate());
            accessTransformRecordMapper.updateAccessTransformRecord(transform);

            if (writeResult.isSuccess())
            {
                stats.writeSuccessCount++;
                if ("inserted".equals(writeResult.getStatus()))
                {
                    stats.insertedCount++;
                }
                else if ("updated".equals(writeResult.getStatus()))
                {
                    stats.updatedCount++;
                }
            }
            else
            {
                stats.writeFailedCount++;
            }
        }

        AccessTableResult tableResult = createTableResult(plan, batch, scopeTable, stats, stageRecords.size());
        for (FieldStats row : fieldStats.values())
        {
            createFieldResult(plan, batch, tableResult, row);
        }
        return stats;
    }

    private AccessTableResult createTableResult(AccessPlan plan, AccessBatch batch, AccessScopeTable scopeTable,
            TableExecutionStats stats, long stagedCount)
    {
        AccessTableResult existing = findCurrentBatchTargetResult(batch.getAccessBatchId(), scopeTable.getTargetTable());
        if (existing != null)
        {
            existing.setSourceTable(mergeSourceTable(existing.getSourceTable(), scopeTable.getSourceTable()));
            existing.setResultStatus(mergeResultStatus(existing.getResultStatus(), stats));
            existing.setReadCount(value(existing.getReadCount()) + stagedCount);
            existing.setStagedCount(value(existing.getStagedCount()) + stagedCount);
            existing.setInsertedCount(value(existing.getInsertedCount()) + stats.insertedCount);
            existing.setUpdatedCount(value(existing.getUpdatedCount()) + stats.updatedCount);
            existing.setSuccessCount(value(existing.getSuccessCount()) + stats.writeSuccessCount);
            existing.setFailedCount(value(existing.getFailedCount()) + stats.writeFailedCount + stats.transformFailedCount);
            existing.setTotalSuccessCount(value(existing.getTotalSuccessCount()) + stats.writeSuccessCount);
            existing.setTotalFailedCount(value(existing.getTotalFailedCount()) + stats.writeFailedCount + stats.transformFailedCount);
            existing.setLastExecuteTime(DateUtils.getNowDate());
            existing.setUpdateTime(DateUtils.getNowDate());
            accessTableResultMapper.updateAccessTableResult(existing);
            return existing;
        }

        AccessTableResult result = new AccessTableResult();
        result.setAccessBatchId(batch.getAccessBatchId());
        result.setAccessPlanId(plan.getAccessPlanId());
        result.setScopeTableId(scopeTable.getScopeTableId());
        result.setSourceTable(scopeTable.getSourceTable());
        result.setTargetTable(scopeTable.getTargetTable());
        result.setResultStatus(stats.writeFailedCount > 0L || stats.transformFailedCount > 0L ? "partial" : "success");
        result.setReadCount(stagedCount);
        result.setStagedCount(stagedCount);
        result.setInsertedCount(stats.insertedCount);
        result.setUpdatedCount(stats.updatedCount);
        result.setSuccessCount(stats.writeSuccessCount);
        result.setFailedCount(stats.writeFailedCount + stats.transformFailedCount);
        result.setTotalSuccessCount(sumPriorSuccess(plan.getAccessPlanId(), scopeTable.getTargetTable()) + stats.writeSuccessCount);
        result.setTotalFailedCount(sumPriorFailed(plan.getAccessPlanId(), scopeTable.getTargetTable()) + stats.writeFailedCount + stats.transformFailedCount);
        result.setLastExecuteTime(DateUtils.getNowDate());
        result.setMessage("按最终字段规则接入目标表");
        result.setCreateTime(DateUtils.getNowDate());
        accessTableResultMapper.insertAccessTableResult(result);
        return result;
    }

    private AccessTableResult findCurrentBatchTargetResult(Long accessBatchId, String targetTable)
    {
        AccessTableResult query = new AccessTableResult();
        query.setAccessBatchId(accessBatchId);
        query.setTargetTable(targetTable);
        List<AccessTableResult> rows = accessTableResultMapper.selectAccessTableResultList(query);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private String mergeSourceTable(String currentSourceTables, String nextSourceTable)
    {
        if (isBlank(currentSourceTables))
        {
            return nextSourceTable;
        }
        if (isBlank(nextSourceTable) || currentSourceTables.contains(nextSourceTable))
        {
            return currentSourceTables;
        }
        return currentSourceTables + " / " + nextSourceTable;
    }

    private String mergeResultStatus(String currentStatus, TableExecutionStats stats)
    {
        if ("failed".equals(currentStatus) || "partial".equals(currentStatus))
        {
            return currentStatus;
        }
        return stats.writeFailedCount > 0L || stats.transformFailedCount > 0L ? "partial" : "success";
    }

    private void createFieldResult(AccessPlan plan, AccessBatch batch, AccessTableResult tableResult, FieldStats row)
    {
        AccessFieldResult existing = findCurrentTargetFieldResult(tableResult.getTableResultId(), row.field.getTargetColumn());
        if (existing != null)
        {
            existing.setSourceColumn(mergeSourceTable(existing.getSourceColumn(), row.field.getSourceColumn()));
            existing.setMappingType(mergeSourceTable(existing.getMappingType(), row.field.getMappingType()));
            existing.setTransformStatus("failed".equals(existing.getTransformStatus()) || row.failedCount > 0L ? "failed" : "success");
            existing.setSuccessCount(value(existing.getSuccessCount()) + row.successCount);
            existing.setFailedCount(value(existing.getFailedCount()) + row.failedCount);
            existing.setErrorMessage(mergeErrorMessage(existing.getErrorMessage(), row.errorMessage));
            existing.setUpdateTime(DateUtils.getNowDate());
            accessFieldResultMapper.updateAccessFieldResult(existing);
            return;
        }

        AccessFieldResult fieldResult = new AccessFieldResult();
        fieldResult.setTableResultId(tableResult.getTableResultId());
        fieldResult.setAccessBatchId(batch.getAccessBatchId());
        fieldResult.setAccessPlanId(plan.getAccessPlanId());
        fieldResult.setScopeFieldId(row.field.getScopeFieldId());
        fieldResult.setSourceColumn(row.field.getSourceColumn());
        fieldResult.setTargetColumn(row.field.getTargetColumn());
        fieldResult.setMappingType(row.field.getMappingType());
        fieldResult.setTransformStatus(row.failedCount > 0L ? "failed" : "success");
        fieldResult.setSuccessCount(row.successCount);
        fieldResult.setFailedCount(row.failedCount);
        fieldResult.setErrorMessage(row.errorMessage);
        fieldResult.setCreateTime(DateUtils.getNowDate());
        accessFieldResultMapper.insertAccessFieldResult(fieldResult);
    }

    private AccessFieldResult findCurrentTargetFieldResult(Long tableResultId, String targetColumn)
    {
        AccessFieldResult query = new AccessFieldResult();
        query.setTableResultId(tableResultId);
        query.setTargetColumn(targetColumn);
        List<AccessFieldResult> rows = accessFieldResultMapper.selectAccessFieldResultList(query);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private String mergeErrorMessage(String currentMessage, String nextMessage)
    {
        if (isBlank(currentMessage))
        {
            return nextMessage;
        }
        if (isBlank(nextMessage) || currentMessage.contains(nextMessage))
        {
            return currentMessage;
        }
        return currentMessage + "；" + nextMessage;
    }

    private long totalPlanSuccess(Long accessPlanId)
    {
        AccessTableResult query = new AccessTableResult();
        query.setAccessPlanId(accessPlanId);
        long total = 0L;
        for (AccessTableResult row : accessTableResultMapper.selectAccessTableResultList(query))
        {
            total += value(row.getSuccessCount());
        }
        return total;
    }

    private long totalPlanFailed(Long accessPlanId)
    {
        AccessTableResult query = new AccessTableResult();
        query.setAccessPlanId(accessPlanId);
        long total = 0L;
        for (AccessTableResult row : accessTableResultMapper.selectAccessTableResultList(query))
        {
            total += value(row.getFailedCount());
        }
        return total;
    }

    private long sumPriorSuccess(Long accessPlanId, String targetTable)
    {
        AccessTableResult query = new AccessTableResult();
        query.setAccessPlanId(accessPlanId);
        query.setTargetTable(targetTable);
        long total = 0L;
        for (AccessTableResult row : accessTableResultMapper.selectAccessTableResultList(query))
        {
            total += value(row.getSuccessCount());
        }
        return total;
    }

    private long sumPriorFailed(Long accessPlanId, String targetTable)
    {
        AccessTableResult query = new AccessTableResult();
        query.setAccessPlanId(accessPlanId);
        query.setTargetTable(targetTable);
        long total = 0L;
        for (AccessTableResult row : accessTableResultMapper.selectAccessTableResultList(query))
        {
            total += value(row.getFailedCount());
        }
        return total;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value)
    {
        if (value instanceof Map)
        {
            return (Map<String, Object>) value;
        }
        return Collections.emptyMap();
    }

    private List<Map<String, Object>> mapList(Object value)
    {
        if (!(value instanceof List))
        {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : (List<?>) value)
        {
            if (item instanceof Map)
            {
                result.add(asMap(item));
            }
        }
        return result;
    }

    private Map<String, Object> parseRawData(String rawData)
    {
        if (isBlank(rawData))
        {
            return Collections.emptyMap();
        }
        try
        {
            return objectMapper.readValue(rawData, MAP_TYPE);
        }
        catch (Exception e)
        {
            throw new ServiceException("接入中间库原始数据不是合法 JSON: " + e.getMessage());
        }
    }

    private String asString(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }

    private Long asLong(Object value)
    {
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        if (value == null)
        {
            return null;
        }
        try
        {
            return Long.parseLong(String.valueOf(value));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private boolean asBoolean(Object value)
    {
        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }
        if (value == null)
        {
            return false;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private String toJson(Object value)
    {
        try
        {
            return objectMapper.writeValueAsString(value);
        }
        catch (JsonProcessingException e)
        {
            throw new ServiceException("JSON 序列化失败: " + e.getMessage());
        }
    }

    private boolean endpointMatches(String endpoint, String expectedEndpoint)
    {
        return expectedEndpoint.equals(endpointOrDefault(endpoint, expectedEndpoint));
    }

    private String endpointOrDefault(String endpoint, String defaultEndpoint)
    {
        return isBlank(endpoint) ? defaultEndpoint : endpoint.trim();
    }

    private String defaultIfBlank(String value, String defaultValue)
    {
        return isBlank(value) ? defaultValue : value;
    }

    private long value(Long value)
    {
        return value == null ? 0L : value;
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    private static class TableExecutionStats
    {
        private long transformSuccessCount;
        private long transformFailedCount;
        private long writeSuccessCount;
        private long writeFailedCount;
        private long insertedCount;
        private long updatedCount;
    }

    private static class FieldStats
    {
        private final AccessScopeField field;
        private long successCount;
        private long failedCount;
        private String errorMessage;

        private FieldStats(AccessScopeField field)
        {
            this.field = field;
        }
    }
}
