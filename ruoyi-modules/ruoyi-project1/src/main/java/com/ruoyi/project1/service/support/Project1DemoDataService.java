package com.ruoyi.project1.service.support;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import com.ruoyi.project1.domain.DbDatasource;
import com.ruoyi.project1.domain.MappingSpec;
import com.ruoyi.project1.domain.MappingSpecSet;
import com.ruoyi.project1.domain.MatchResultRow;
import com.ruoyi.project1.domain.MatchResultSet;
import com.ruoyi.project1.domain.MatchResultVersion;
import com.ruoyi.project1.domain.MatchTask;
import com.ruoyi.project1.domain.MatchTaskRecord;
import com.ruoyi.project1.domain.MatchTaskVersion;
import com.ruoyi.project1.domain.ReviewedMatch;
import com.ruoyi.project1.domain.SchemaEntity;
import com.ruoyi.project1.domain.SchemaField;
import com.ruoyi.project1.domain.SchemaSnapshot;
import com.ruoyi.project1.domain.TaskMetric;
import com.ruoyi.project1.mapper.AccessBatchMapper;
import com.ruoyi.project1.mapper.AccessFieldResultMapper;
import com.ruoyi.project1.mapper.AccessPlanMapper;
import com.ruoyi.project1.mapper.AccessScopeFieldMapper;
import com.ruoyi.project1.mapper.AccessScopeTableMapper;
import com.ruoyi.project1.mapper.AccessStageRecordMapper;
import com.ruoyi.project1.mapper.AccessTableResultMapper;
import com.ruoyi.project1.mapper.AccessTransformRecordMapper;
import com.ruoyi.project1.mapper.ApiPullDatasourceMapper;
import com.ruoyi.project1.mapper.DatasourceMapper;
import com.ruoyi.project1.mapper.DbDatasourceMapper;
import com.ruoyi.project1.mapper.MappingSpecMapper;
import com.ruoyi.project1.mapper.MappingSpecSetMapper;
import com.ruoyi.project1.mapper.MatchResultRowMapper;
import com.ruoyi.project1.mapper.MatchResultSetMapper;
import com.ruoyi.project1.mapper.MatchResultVersionMapper;
import com.ruoyi.project1.mapper.MatchTaskMapper;
import com.ruoyi.project1.mapper.MatchTaskRecordMapper;
import com.ruoyi.project1.mapper.MatchTaskVersionMapper;
import com.ruoyi.project1.mapper.ReviewedMatchMapper;
import com.ruoyi.project1.mapper.SchemaEntityMapper;
import com.ruoyi.project1.mapper.SchemaFieldMapper;
import com.ruoyi.project1.mapper.SchemaSnapshotMapper;
import com.ruoyi.project1.mapper.TaskMetricMapper;

/**
 * Service-layer demo data for project1.
 *
 * Controllers remain standard RuoYi entry points. This class only prepares a
 * realistic p1p_ data flow while the real FastAPI matching service and real
 * source/target databases are not connected yet.
 */
@Service
public class Project1DemoDataService
{
    @Autowired private DatasourceMapper datasourceMapper;
    @Autowired private DbDatasourceMapper dbDatasourceMapper;
    @Autowired private ApiPullDatasourceMapper apiPullDatasourceMapper;
    @Autowired private SchemaSnapshotMapper schemaSnapshotMapper;
    @Autowired private SchemaEntityMapper schemaEntityMapper;
    @Autowired private SchemaFieldMapper schemaFieldMapper;
    @Autowired private MatchTaskMapper matchTaskMapper;
    @Autowired private MatchTaskVersionMapper matchTaskVersionMapper;
    @Autowired private MatchTaskRecordMapper matchTaskRecordMapper;
    @Autowired private MatchResultVersionMapper matchResultVersionMapper;
    @Autowired private MatchResultSetMapper matchResultSetMapper;
    @Autowired private MatchResultRowMapper matchResultRowMapper;
    @Autowired private TaskMetricMapper taskMetricMapper;
    @Autowired private ReviewedMatchMapper reviewedMatchMapper;
    @Autowired private MappingSpecSetMapper mappingSpecSetMapper;
    @Autowired private MappingSpecMapper mappingSpecMapper;
    @Autowired private AccessPlanMapper accessPlanMapper;
    @Autowired private AccessBatchMapper accessBatchMapper;
    @Autowired private AccessScopeTableMapper accessScopeTableMapper;
    @Autowired private AccessScopeFieldMapper accessScopeFieldMapper;
    @Autowired private AccessStageRecordMapper accessStageRecordMapper;
    @Autowired private AccessTransformRecordMapper accessTransformRecordMapper;
    @Autowired private AccessTableResultMapper accessTableResultMapper;
    @Autowired private AccessFieldResultMapper accessFieldResultMapper;

    @Transactional(rollbackFor = Exception.class)
    public synchronized void ensureDemoData()
    {
        if (findAccessPlanByName("CF维修记录持续接入") != null)
        {
            return;
        }

        Datasource apiPull = ensureDatasource("CF_API_PULL", "api_pull");
        Datasource dbDirect = ensureDatasource("CF_DB_DIRECT", "db_direct");
        Datasource personApi = ensureDatasource("CF人员API", "api_pull");

        ensureSchema(apiPull);
        ensureSchema(dbDirect);
        ensureSchema(personApi);

        MappingSpecSet apiSpec = ensureTaskFlow("ERP 拉模式匹配任务", apiPull, "MagnetoGPT CF-SF one2one");
        MappingSpecSet dbSpec = ensureTaskFlow("MES 直连匹配任务", dbDirect, "MagnetoGPT CF-SF one2one");
        MappingSpecSet personSpec = ensureTaskFlow("CF人员接口匹配任务", personApi, "MagnetoGPT CF-SF one2one");

        AccessPlan oncePlan = ensureAccessPlan("CF事故数据一次性接入", apiPull, "once", apiSpec.getSpecSetId());
        AccessPlan continuousPlan = ensureAccessPlan("CF维修记录持续接入", dbDirect, "continuous", dbSpec.getSpecSetId());
        AccessPlan personPlan = ensureAccessPlan("CF人员信息接入", personApi, "once", personSpec.getSpecSetId());

        seedAccessResultsIfEmpty(oncePlan);
        seedAccessResultsIfEmpty(continuousPlan);
        seedAccessResultsIfEmpty(personPlan);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> runMatchTaskWithoutAlgorithm(Long taskId)
    {
        MatchTask task = requireTask(taskId);
        Datasource datasource = requireDatasource(task.getSourceDatasourceId());
        MatchTaskVersion version = resolveCurrentTaskVersion(task, datasource);

        MatchTaskRecord record = createTaskRecord(task, version, "running", "生成演示匹配结果", false);
        clearDefaultResultSets(task.getTaskId());
        MatchResultSet resultSet = createResultSet(task, version, record, "MagnetoGPT CF-SF one2one");
        createResultRowsAndRules(task, version, record, resultSet);

        record.setDefaultResultSetId(resultSet.getResultSetId());
        record.setExecutionStatus("success");
        record.setCurrentStage("已生成默认结果集和最终接入规则集");
        record.setProgress(100L);
        record.setFinishedAt(DateUtils.getNowDate());
        matchTaskRecordMapper.updateMatchTaskRecord(record);

        task.setLifecycleStatus("active");
        task.setLastRecordStatus("success");
        task.setLastRecordStage("无算法模式已生成默认结果集，可进入数据接入计划");
        task.setUpdateTime(DateUtils.getNowDate());
        matchTaskMapper.updateMatchTask(task);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", task.getTaskId());
        payload.put("versionId", version.getVersionId());
        payload.put("versionNo", version.getVersionNo());
        payload.put("recordId", record.getRecordId());
        payload.put("resultSetId", resultSet.getResultSetId());
        payload.put("message", "任务运行完成，已生成默认结果集、审核结果和最终接入规则集");
        return payload;
    }

    @Transactional(rollbackFor = Exception.class)
    public MatchTaskVersion recordTaskVersion(MatchTask task, String summary)
    {
        Datasource datasource = task.getSourceDatasourceId() == null ? null : datasourceMapper.selectDatasourceByDatasourceId(task.getSourceDatasourceId());
        return createTaskVersion(task, datasource, summary);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long resolveDefaultSpecSetId(Long sourceDatasourceId)
    {
        MappingSpecSet query = new MappingSpecSet();
        query.setSourceDatasourceId(sourceDatasourceId);
        query.setIsDefault(1);
        query.setSpecStatus("active");
        List<MappingSpecSet> rows = mappingSpecSetMapper.selectMappingSpecSetList(query);
        if (!rows.isEmpty())
        {
            return rows.get(0).getSpecSetId();
        }

        ensureDemoData();
        rows = mappingSpecSetMapper.selectMappingSpecSetList(query);
        if (!rows.isEmpty())
        {
            return rows.get(0).getSpecSetId();
        }
        throw new ServiceException("未找到审核后生成的最终接入规则集，请先运行并审核模式映射结果");
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> executeAccessPlan(Long accessPlanId)
    {
        AccessPlan plan = accessPlanMapper.selectAccessPlanByAccessPlanId(accessPlanId);
        if (plan == null)
        {
            throw new ServiceException("接入计划不存在，请先刷新列表");
        }
        if ("paused".equals(plan.getUseStatus()))
        {
            throw new ServiceException("接入计划已暂停，请先恢复后再执行");
        }
        if ("disabled".equals(plan.getUseStatus()) || "unavailable".equals(plan.getUseStatus()))
        {
            throw new ServiceException("接入计划当前不可执行");
        }

        Datasource datasource = requireDatasource(plan.getSourceDatasourceId());
        if (plan.getSpecSetId() == null)
        {
            plan.setSpecSetId(resolveDefaultSpecSetId(plan.getSourceDatasourceId()));
        }

        AccessBatch batch = createAccessBatch(plan, "manual", "running");
        List<AccessTableResult> tableResults = createAccessPipelineRows(plan, datasource, batch);

        long successCount = 0L;
        long failedCount = 0L;
        long insertedCount = 0L;
        long updatedCount = 0L;
        for (AccessTableResult row : tableResults)
        {
            successCount += value(row.getSuccessCount());
            failedCount += value(row.getFailedCount());
            insertedCount += value(row.getInsertedCount());
            updatedCount += value(row.getUpdatedCount());
        }

        batch.setBatchStatus(failedCount > 0 ? "partial" : "success");
        batch.setReadCount(successCount + failedCount);
        batch.setStagedCount(successCount + failedCount);
        batch.setTransformSuccessCount(successCount);
        batch.setTransformFailedCount(failedCount);
        batch.setWriteSuccessCount(successCount);
        batch.setWriteFailedCount(failedCount);
        batch.setInsertedCount(insertedCount);
        batch.setUpdatedCount(updatedCount);
        batch.setFinishedAt(DateUtils.getNowDate());
        accessBatchMapper.updateAccessBatch(batch);

        plan.setCurrentBatchId(batch.getAccessBatchId());
        plan.setUseStatus("enabled");
        plan.setLastSuccessCount(successCount);
        plan.setLastFailedCount(failedCount);
        plan.setTotalSuccessCount(totalPlanSuccess(plan.getAccessPlanId()));
        plan.setTotalFailedCount(totalPlanFailed(plan.getAccessPlanId()));
        plan.setLastExecuteTime(DateUtils.getNowDate());
        plan.setUpdateTime(DateUtils.getNowDate());
        accessPlanMapper.updateAccessPlan(plan);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("accessPlanId", accessPlanId);
        payload.put("batchId", batch.getAccessBatchId());
        payload.put("batchNo", batch.getBatchNo());
        payload.put("successCount", successCount);
        payload.put("failedCount", failedCount);
        payload.put("tableCount", tableResults.size());
        payload.put("message", "接入执行完成，已记录读取、暂存、转换和写入结果");
        return payload;
    }

    @Transactional(rollbackFor = Exception.class)
    public SchemaSnapshot createSchemaSnapshot(Datasource datasource)
    {
        SchemaSnapshot snapshot = new SchemaSnapshot();
        snapshot.setDatasourceId(datasource.getDatasourceId());
        snapshot.setSnapshotName(datasource.getDatasourceName() + " 模式快照");
        snapshot.setReadStatus("success");
        snapshot.setEntityCount(3L);
        snapshot.setFieldCount(15L);
        snapshot.setSchemaJson("{\"source\":\"" + escapeJson(datasource.getDatasourceName()) + "\",\"entities\":3,\"fields\":15}");
        snapshot.setReadBy("system");
        snapshot.setReadTime(DateUtils.getNowDate());
        snapshot.setCreateTime(DateUtils.getNowDate());
        schemaSnapshotMapper.insertSchemaSnapshot(snapshot);

        for (String[] entity : schemaEntityRows(datasource))
        {
            SchemaEntity schemaEntity = new SchemaEntity();
            schemaEntity.setSnapshotId(snapshot.getSnapshotId());
            schemaEntity.setDatasourceId(datasource.getDatasourceId());
            schemaEntity.setEntityName(entity[0]);
            schemaEntity.setSourceDatabase(entity[1]);
            schemaEntity.setSourceTable(entity[2]);
            schemaEntity.setEntityType("table");
            schemaEntity.setRowCount(Long.valueOf(entity[3]));
            schemaEntity.setMetadata("{\"demo\":true}");
            schemaEntity.setCreateTime(DateUtils.getNowDate());
            schemaEntityMapper.insertSchemaEntity(schemaEntity);

            int ordinal = 1;
            for (String field : entity[4].split(","))
            {
                SchemaField schemaField = new SchemaField();
                schemaField.setEntityId(schemaEntity.getEntityId());
                schemaField.setSnapshotId(snapshot.getSnapshotId());
                schemaField.setFieldName(field.trim());
                schemaField.setSourceColumn(field.trim());
                schemaField.setDataType(ordinal == 1 ? "varchar(64)" : "varchar(255)");
                schemaField.setOrdinalPosition((long) ordinal);
                schemaField.setIsPrimaryKey(ordinal == 1 ? 1 : 0);
                schemaField.setIsNullable(ordinal == 1 ? 0 : 1);
                schemaField.setSampleValues("[\"demo_" + ordinal + "\"]");
                schemaField.setFieldComment("演示字段");
                schemaField.setCreateTime(DateUtils.getNowDate());
                schemaFieldMapper.insertSchemaField(schemaField);
                ordinal++;
            }
        }

        datasource.setLatestSchemaSnapshotId(snapshot.getSnapshotId());
        datasource.setConnectionStatus("success");
        datasource.setLastTestTime(DateUtils.getNowDate());
        datasource.setLastTestMessage("模式读取完成");
        datasource.setUpdateTime(DateUtils.getNowDate());
        datasourceMapper.updateDatasource(datasource);
        return snapshot;
    }

    @Transactional(rollbackFor = Exception.class)
    public void ensureMappingSpecForResultSet(Long resultSetId)
    {
        MatchResultSet resultSet = matchResultSetMapper.selectMatchResultSetByResultSetId(resultSetId);
        if (resultSet == null)
        {
            throw new ServiceException("结果集不存在，请先刷新列表");
        }

        MappingSpecSet query = new MappingSpecSet();
        query.setResultSetId(resultSetId);
        List<MappingSpecSet> rows = mappingSpecSetMapper.selectMappingSpecSetList(query);
        if (!rows.isEmpty())
        {
            setDefaultSpecSet(rows.get(0));
            return;
        }

        MatchTask task = requireTask(resultSet.getTaskId());
        MatchTaskVersion version = matchTaskVersionMapper.selectMatchTaskVersionByVersionId(resultSet.getVersionId());
        MatchTaskRecord record = matchTaskRecordMapper.selectMatchTaskRecordByRecordId(resultSet.getRecordId());
        createReviewedRulesAndSpecSet(task, version, record, resultSet);
    }

    private Datasource ensureDatasource(String name, String accessMode)
    {
        Datasource existing = findDatasourceByName(name);
        if (existing != null)
        {
            return existing;
        }

        Datasource datasource = new Datasource();
        datasource.setDatasourceName(name);
        datasource.setAccessMode(accessMode);
        datasource.setUseStatus("enabled");
        datasource.setConnectionStatus("success");
        datasource.setLastTestTime(DateUtils.getNowDate());
        datasource.setLastTestMessage("演示数据源连接正常");
        datasource.setStatus("0");
        datasource.setDelFlag("0");
        datasource.setCreateTime(DateUtils.getNowDate());
        datasourceMapper.insertDatasource(datasource);

        if ("db_direct".equals(accessMode))
        {
            DbDatasource db = new DbDatasource();
            db.setDatasourceId(datasource.getDatasourceId());
            db.setDbType("mysql");
            db.setHost("127.0.0.1");
            db.setPort(3306L);
            db.setDatabaseName("cf_source");
            db.setSchemaName("public");
            db.setUsername("demo_user");
            db.setSavePassword(0);
            db.setConnectionParams("{\"connectTimeout\":3000}");
            dbDatasourceMapper.insertDbDatasource(db);
        }
        else
        {
            ApiPullDatasource api = new ApiPullDatasource();
            api.setDatasourceId(datasource.getDatasourceId());
            api.setBaseUrl("http://127.0.0.1:8031/mock-cf");
            api.setPullEndpoint("/records");
            api.setSchemaEndpoint("/schema");
            api.setRequestMethod("GET");
            api.setAuthType("none");
            api.setHealthEndpoint("/health");
            api.setHeadersConfig("{}");
            api.setExtraConfig("{\"demo\":true}");
            apiPullDatasourceMapper.insertApiPullDatasource(api);
        }
        return datasource;
    }

    private MappingSpecSet ensureTaskFlow(String taskName, Datasource datasource, String resultSetName)
    {
        MatchTask task = findTaskByName(taskName);
        if (task == null)
        {
            task = new MatchTask();
            task.setTaskName(taskName);
            task.setSourceDatasourceId(datasource.getDatasourceId());
            task.setLifecycleStatus("active");
            task.setLastRecordStatus("success");
            task.setLastRecordStage("已生成演示映射结果");
            task.setStatus("0");
            task.setDelFlag("0");
            task.setCreateTime(DateUtils.getNowDate());
            matchTaskMapper.insertMatchTask(task);
        }

        MatchTaskVersion version = resolveCurrentTaskVersion(task, datasource);
        MatchTaskRecord record = createTaskRecord(task, version, "success", "演示结果已生成", true);
        MatchResultSet resultSet = findDefaultResultSet(task.getTaskId());
        if (resultSet == null)
        {
            resultSet = createResultSet(task, version, record, resultSetName);
            createResultRowsAndRules(task, version, record, resultSet);
            record.setDefaultResultSetId(resultSet.getResultSetId());
            matchTaskRecordMapper.updateMatchTaskRecord(record);
        }
        MappingSpecSet specSet = findSpecSetByResultSet(resultSet.getResultSetId());
        return specSet == null ? createReviewedRulesAndSpecSet(task, version, record, resultSet) : specSet;
    }

    private MatchTaskVersion resolveCurrentTaskVersion(MatchTask task, Datasource datasource)
    {
        if (task.getCurrentVersionId() != null)
        {
            MatchTaskVersion current = matchTaskVersionMapper.selectMatchTaskVersionByVersionId(task.getCurrentVersionId());
            if (current != null)
            {
                return current;
            }
        }

        MatchTaskVersion query = new MatchTaskVersion();
        query.setTaskId(task.getTaskId());
        List<MatchTaskVersion> versions = matchTaskVersionMapper.selectMatchTaskVersionList(query);
        if (!versions.isEmpty())
        {
            MatchTaskVersion latest = latestVersion(versions);
            task.setCurrentVersionId(latest.getVersionId());
            task.setUpdateTime(DateUtils.getNowDate());
            matchTaskMapper.updateMatchTask(task);
            return latest;
        }
        return createTaskVersion(task, datasource, "初始任务配置");
    }

    private MatchTaskVersion createTaskVersion(MatchTask task, Datasource datasource, String summary)
    {
        MatchTaskVersion version = new MatchTaskVersion();
        version.setTaskId(task.getTaskId());
        version.setVersionNo(nextTaskVersionNo(task.getTaskId()));
        version.setSourceDatasourceId(task.getSourceDatasourceId());
        version.setSchemaSnapshotId(datasource == null ? null : datasource.getLatestSchemaSnapshotId());
        version.setTargetKey("dossier");
        version.setEncodingModeKey("header_values_default");
        version.setEmbeddingModelKey("mpnet");
        version.setEvalModeKey("global_unknown");
        version.setAutoApproveThreshold(new BigDecimal("0.9000"));
        version.setAlgorithmParamsJson("{\"mode\":\"demo_without_algorithm\",\"externalAlgorithm\":false}");
        version.setChangeSummary(summary);
        version.setCreateTime(DateUtils.getNowDate());
        matchTaskVersionMapper.insertMatchTaskVersion(version);

        task.setCurrentVersionId(version.getVersionId());
        task.setUpdateTime(DateUtils.getNowDate());
        matchTaskMapper.updateMatchTask(task);
        return version;
    }

    private MatchTaskRecord createTaskRecord(MatchTask task, MatchTaskVersion version, String status, String stage, boolean reuseSuccess)
    {
        if (reuseSuccess)
        {
            MatchTaskRecord query = new MatchTaskRecord();
            query.setTaskId(task.getTaskId());
            query.setVersionId(version.getVersionId());
            query.setExecutionStatus(status);
            List<MatchTaskRecord> existing = matchTaskRecordMapper.selectMatchTaskRecordList(query);
            if (!existing.isEmpty())
            {
                return existing.get(existing.size() - 1);
            }
        }

        MatchTaskRecord record = new MatchTaskRecord();
        record.setTaskId(task.getTaskId());
        record.setVersionId(version.getVersionId());
        record.setSchemaSnapshotId(version.getSchemaSnapshotId());
        record.setExecutionStatus(status);
        record.setCurrentStage(stage);
        record.setProgress("success".equals(status) ? 100L : 80L);
        record.setAlgorithmParamsSnapshot("{\"mode\":\"demo_without_algorithm\"}");
        record.setResultsDir("cf_sf/cf-mpnet-header_values_default-deepseek_deepseek-chat-global_unknown");
        record.setStartedAt(DateUtils.getNowDate());
        record.setFinishedAt("success".equals(status) ? DateUtils.getNowDate() : null);
        record.setCreateTime(DateUtils.getNowDate());
        matchTaskRecordMapper.insertMatchTaskRecord(record);
        return record;
    }

    private MatchResultSet createResultSet(MatchTask task, MatchTaskVersion version, MatchTaskRecord record, String name)
    {
        MatchResultVersion resultVersion = new MatchResultVersion();
        resultVersion.setTaskId(task.getTaskId());
        resultVersion.setVersionId(version.getVersionId());
        resultVersion.setRecordId(record.getRecordId());
        resultVersion.setVersionLabel("V" + version.getVersionNo());
        resultVersion.setCreateTime(DateUtils.getNowDate());
        matchResultVersionMapper.insertMatchResultVersion(resultVersion);

        MatchResultSet resultSet = new MatchResultSet();
        resultSet.setResultVersionId(resultVersion.getResultVersionId());
        resultSet.setTaskId(task.getTaskId());
        resultSet.setVersionId(version.getVersionId());
        resultSet.setRecordId(record.getRecordId());
        resultSet.setSourceDatasourceId(task.getSourceDatasourceId());
        resultSet.setMethod("MagnetoGPT");
        resultSet.setVariant("CF-SF one2one");
        resultSet.setResultSetName(name);
        resultSet.setIsDefault(1);
        resultSet.setTotalRows(270L);
        resultSet.setAvgScore(new BigDecimal("0.850000"));
        resultSet.setRemark("演示模式：不调用外部算法，先生成可审核、可接入的模式匹配结果。");
        resultSet.setCreateTime(DateUtils.getNowDate());
        matchResultSetMapper.insertMatchResultSet(resultSet);
        createTaskMetrics(task, version, record, resultSet);
        return resultSet;
    }

    private void clearDefaultResultSets(Long taskId)
    {
        MatchResultSet query = new MatchResultSet();
        query.setTaskId(taskId);
        query.setIsDefault(1);
        for (MatchResultSet row : matchResultSetMapper.selectMatchResultSetList(query))
        {
            row.setIsDefault(0);
            row.setUpdateTime(DateUtils.getNowDate());
            matchResultSetMapper.updateMatchResultSet(row);
        }
    }

    private MappingSpecSet createResultRowsAndRules(MatchTask task, MatchTaskVersion version, MatchTaskRecord record, MatchResultSet resultSet)
    {
        MatchResultRow existing = new MatchResultRow();
        existing.setResultSetId(resultSet.getResultSetId());
        if (matchResultRowMapper.selectMatchResultRowList(existing).isEmpty())
        {
            long rowNo = 1L;
            for (String[] row : matchRowsFor(task.getSourceDatasourceId()))
            {
                MatchResultRow resultRow = new MatchResultRow();
                resultRow.setResultSetId(resultSet.getResultSetId());
                resultRow.setRowNo(rowNo++);
                resultRow.setSourceDatabase(row[0]);
                resultRow.setSourceTable(row[1]);
                resultRow.setSourceColumn(row[2]);
                resultRow.setTargetTable(row[3]);
                resultRow.setTargetColumn(row[4]);
                resultRow.setScore(new BigDecimal(row[5]));
                resultRow.setRawPayload("{\"demo\":true}");
                resultRow.setCreateTime(DateUtils.getNowDate());
                matchResultRowMapper.insertMatchResultRow(resultRow);
            }
        }
        return createReviewedRulesAndSpecSet(task, version, record, resultSet);
    }

    private MappingSpecSet createReviewedRulesAndSpecSet(MatchTask task, MatchTaskVersion version, MatchTaskRecord record, MatchResultSet resultSet)
    {
        MappingSpecSet existing = findSpecSetByResultSet(resultSet.getResultSetId());
        if (existing != null)
        {
            setDefaultSpecSet(existing);
            return existing;
        }

        MatchResultRow query = new MatchResultRow();
        query.setResultSetId(resultSet.getResultSetId());
        List<MatchResultRow> rows = matchResultRowMapper.selectMatchResultRowList(query);

        MappingSpecSet specSet = new MappingSpecSet();
        specSet.setSpecSetName(resultSet.getResultSetName() + " 最终接入规则集");
        specSet.setResultSetId(resultSet.getResultSetId());
        specSet.setSourceDatasourceId(resultSet.getSourceDatasourceId());
        specSet.setTaskId(task.getTaskId());
        specSet.setVersionId(version.getVersionId());
        specSet.setRecordId(record.getRecordId());
        specSet.setRuleCount(0L);
        specSet.setIsDefault(1);
        specSet.setSpecStatus("active");
        specSet.setCreateTime(DateUtils.getNowDate());
        mappingSpecSetMapper.insertMappingSpecSet(specSet);
        setDefaultSpecSet(specSet);

        BigDecimal threshold = version.getAutoApproveThreshold() == null ? new BigDecimal("0.900000") : version.getAutoApproveThreshold();
        long order = 1L;
        long acceptedCount = 0L;
        for (MatchResultRow row : rows)
        {
            boolean autoApproved = row.getScore() != null && row.getScore().compareTo(threshold) >= 0;
            ReviewedMatch review = new ReviewedMatch();
            review.setTaskId(task.getTaskId());
            review.setVersionId(version.getVersionId());
            review.setRecordId(record.getRecordId());
            review.setResultSetId(resultSet.getResultSetId());
            review.setResultRowId(row.getResultRowId());
            review.setSourceDatasourceId(resultSet.getSourceDatasourceId());
            review.setSourceDatabase(row.getSourceDatabase());
            review.setSourceTable(row.getSourceTable());
            review.setSourceColumn(row.getSourceColumn());
            review.setProposedTargetTable(row.getTargetTable());
            review.setProposedTargetColumn(row.getTargetColumn());
            review.setTargetTable(row.getTargetTable());
            review.setTargetColumn(row.getTargetColumn());
            review.setMappingType("direct");
            review.setAlgorithmScore(row.getScore());
            review.setReviewStatus(autoApproved ? "auto_approved" : "pending");
            if (autoApproved)
            {
                review.setReviewedBy("system");
                review.setReviewedAt(DateUtils.getNowDate());
            }
            review.setCreateTime(DateUtils.getNowDate());
            reviewedMatchMapper.insertReviewedMatch(review);

            if (autoApproved)
            {
                acceptedCount++;
            MappingSpec spec = new MappingSpec();
            spec.setSpecSetId(specSet.getSpecSetId());
            spec.setTaskId(task.getTaskId());
            spec.setVersionId(version.getVersionId());
            spec.setRecordId(record.getRecordId());
            spec.setSourceDatasourceId(resultSet.getSourceDatasourceId());
            spec.setReviewId(review.getReviewId());
            spec.setSourceDatabase(row.getSourceDatabase());
            spec.setSourceTable(row.getSourceTable());
            spec.setSourceColumn(row.getSourceColumn());
            spec.setTargetTable(row.getTargetTable());
            spec.setTargetColumn(row.getTargetColumn());
            spec.setMappingType("direct");
            spec.setTransformRule("{\"type\":\"direct\",\"trim\":true}");
            spec.setSpecStatus("active");
            spec.setLoadOrder(order++);
            spec.setCreateTime(DateUtils.getNowDate());
            mappingSpecMapper.insertMappingSpec(spec);
            }
        }
        specSet.setRuleCount(acceptedCount);
        specSet.setSpecStatus(acceptedCount == 0L ? "draft" : "active");
        specSet.setUpdateTime(DateUtils.getNowDate());
        mappingSpecSetMapper.updateMappingSpecSet(specSet);
        return specSet;
    }

    private void createTaskMetrics(MatchTask task, MatchTaskVersion version, MatchTaskRecord record, MatchResultSet resultSet)
    {
        addMetric(task, version, record, resultSet, "mrr", "MRR", "0.850000", "card", "{}");
        addMetric(task, version, record, resultSet, "recall20", "Recall@20", "0.630000", "card", "{}");
        addMetric(task, version, record, resultSet, "f1", "All F1 Score", "0.653000", "card", "{}");
        addMetric(task, version, record, resultSet, "one2one", "One2One 匹配数", "270.000000", "card", "{}");
        addMetric(task, version, record, resultSet, "score_dist", "匹配分数分布", "0.000000", "bar", "[{\"label\":\"score = 1.00\",\"value\":194},{\"label\":\"0.90 - 0.99\",\"value\":49},{\"label\":\"0.80 - 0.89\",\"value\":18},{\"label\":\"< 0.80\",\"value\":9}]");
        addMetric(task, version, record, resultSet, "target_dist", "目标表 Top 分布", "0.000000", "bar", "[{\"label\":\"iqs_failure_content\",\"value\":12},{\"label\":\"iqs_failure\",\"value\":11},{\"label\":\"quality_event\",\"value\":9},{\"label\":\"disposition_record\",\"value\":9}]");
        /*
        addMetric(task, version, record, resultSet, "one2one", "One2One 匹配数", "270.000000", "card", "{}");
        addMetric(task, version, record, resultSet, "score_dist", "匹配分数分布", "0.000000", "bar", "[{\"label\":\"score = 1.00\",\"value\":194},{\"label\":\"0.90 - 0.99\",\"value\":49},{\"label\":\"0.80 - 0.89\",\"value\":18},{\"label\":\"< 0.80\",\"value\":9}]");
        addMetric(task, version, record, resultSet, "target_dist", "目标表 Top 分布", "0.000000", "bar", "[{\"label\":\"iqs_failure_content\",\"value\":12},{\"label\":\"iqs_failure\",\"value\":11},{\"label\":\"quality_event\",\"value\":9},{\"label\":\"disposition_record\",\"value\":9}]");
    }

        */
    }

    private void addMetric(MatchTask task, MatchTaskVersion version, MatchTaskRecord record, MatchResultSet resultSet, String key, String name, String value, String chartType, String chartData)
    {
        TaskMetric metric = new TaskMetric();
        metric.setTaskId(task.getTaskId());
        metric.setVersionId(version.getVersionId());
        metric.setRecordId(record.getRecordId());
        metric.setResultSetId(resultSet.getResultSetId());
        metric.setMetricKey(key);
        metric.setMetricName(name);
        metric.setMetricValue(new BigDecimal(value));
        metric.setChartType(chartType);
        metric.setChartData(chartData);
        metric.setResultDir("cf_sf/cf-mpnet-header_values_default-deepseek_deepseek-chat-global_unknown");
        metric.setCreateTime(DateUtils.getNowDate());
        taskMetricMapper.insertTaskMetric(metric);
    }

    private AccessPlan ensureAccessPlan(String planName, Datasource datasource, String accessType, Long specSetId)
    {
        AccessPlan existing = findAccessPlanByName(planName);
        if (existing != null)
        {
            return existing;
        }
        AccessPlan plan = new AccessPlan();
        plan.setPlanName(planName);
        plan.setSourceDatasourceId(datasource.getDatasourceId());
        plan.setAccessMode(datasource.getAccessMode());
        plan.setAccessType(accessType);
        plan.setCycleHours("continuous".equals(accessType) ? 1L : null);
        plan.setSpecSetId(specSetId);
        plan.setUseStatus("enabled");
        plan.setCurrentBatchId(0L);
        plan.setLastSuccessCount(0L);
        plan.setLastFailedCount(0L);
        plan.setTotalSuccessCount(0L);
        plan.setTotalFailedCount(0L);
        plan.setStatus("0");
        plan.setDelFlag("0");
        plan.setRemark("演示接入计划，按审核后生成的最终接入规则集执行。");
        plan.setCreateTime(DateUtils.getNowDate());
        accessPlanMapper.insertAccessPlan(plan);
        return plan;
    }

    private void seedAccessResultsIfEmpty(AccessPlan plan)
    {
        AccessTableResult query = new AccessTableResult();
        query.setAccessPlanId(plan.getAccessPlanId());
        if (!accessTableResultMapper.selectAccessTableResultList(query).isEmpty())
        {
            return;
        }

        int times = "continuous".equals(plan.getAccessType()) ? 3 : 1;
        for (int i = 0; i < times; i++)
        {
            executeAccessPlan(plan.getAccessPlanId());
        }
    }

    private AccessBatch createAccessBatch(AccessPlan plan, String triggerType, String status)
    {
        AccessBatch batch = new AccessBatch();
        batch.setAccessPlanId(plan.getAccessPlanId());
        batch.setBatchNo("BATCH-" + String.format("%03d", plan.getAccessPlanId()) + "-" + String.format("%03d", value(plan.getCurrentBatchId()) + 1L));
        batch.setTriggerType(triggerType);
        batch.setBatchStatus(status);
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
        accessBatchMapper.insertAccessBatch(batch);
        return batch;
    }

    private List<AccessTableResult> createAccessPipelineRows(AccessPlan plan, Datasource datasource, AccessBatch batch)
    {
        List<AccessTableResult> rows = new ArrayList<>();
        List<AccessScopeTable> scopeTables = ensureAccessScopes(plan, datasource);
        long sequence = batch.getAccessBatchId() == null ? 1L : batch.getAccessBatchId();
        for (AccessScopeTable scopeTable : scopeTables)
        {
            long inserted = tableInserted(scopeTable.getTargetTable(), plan.getAccessType(), sequence);
            long updated = "continuous".equals(plan.getAccessType()) ? tableUpdated(scopeTable.getTargetTable()) : 0L;
            long failed = "inventory_batch".equals(scopeTable.getTargetTable()) && "continuous".equals(plan.getAccessType()) ? 18L : 0L;

            AccessStageRecord stage = createStageRecord(plan, datasource, batch, scopeTable, inserted + updated + failed);
            createTransformRecord(plan, batch, scopeTable, stage, failed);
            AccessTableResult tableResult = createTableResult(plan, batch, scopeTable, inserted, updated, failed);
            rows.add(tableResult);
            createFieldResults(plan, batch, scopeTable, tableResult, failed);
        }
        return rows;
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
        Map<String, List<MappingSpec>> grouped = new LinkedHashMap<>();
        for (MappingSpec spec : specs)
        {
            String key = spec.getSourceTable() + "->" + spec.getTargetTable();
            grouped.computeIfAbsent(key, item -> new ArrayList<>()).add(spec);
        }

        List<AccessScopeTable> scopeTables = new ArrayList<>();
        for (List<MappingSpec> mappings : grouped.values())
        {
            MappingSpec first = mappings.get(0);
            AccessScopeTable scopeTable = new AccessScopeTable();
            scopeTable.setAccessPlanId(plan.getAccessPlanId());
            scopeTable.setSpecSetId(plan.getSpecSetId());
            scopeTable.setSourceDatasourceId(datasource.getDatasourceId());
            scopeTable.setSourceDatabase(first.getSourceDatabase());
            scopeTable.setSourceTable(first.getSourceTable());
            scopeTable.setTargetTable(first.getTargetTable());
            scopeTable.setFieldMappingCount((long) mappings.size());
            scopeTable.setScopeStatus("active");
            scopeTable.setCreateTime(DateUtils.getNowDate());
            accessScopeTableMapper.insertAccessScopeTable(scopeTable);
            scopeTables.add(scopeTable);

            for (MappingSpec mapping : mappings)
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

    private AccessStageRecord createStageRecord(AccessPlan plan, Datasource datasource, AccessBatch batch, AccessScopeTable scopeTable, long readCount)
    {
        AccessStageRecord stage = new AccessStageRecord();
        stage.setAccessBatchId(batch.getAccessBatchId());
        stage.setAccessPlanId(plan.getAccessPlanId());
        stage.setScopeTableId(scopeTable.getScopeTableId());
        stage.setSourceDatasourceId(datasource.getDatasourceId());
        stage.setSourceDatabase(scopeTable.getSourceDatabase());
        stage.setSourceTable(scopeTable.getSourceTable());
        stage.setSourcePk(scopeTable.getSourceTable() + "_demo_" + batch.getAccessBatchId());
        stage.setSourceOperation("continuous".equals(plan.getAccessType()) ? "upsert" : "insert");
        stage.setRawData("{\"readCount\":" + readCount + ",\"sourceTable\":\"" + escapeJson(scopeTable.getSourceTable()) + "\"}");
        stage.setReadStatus("success");
        stage.setReadTime(DateUtils.getNowDate());
        stage.setCreateTime(DateUtils.getNowDate());
        accessStageRecordMapper.insertAccessStageRecord(stage);
        return stage;
    }

    private void createTransformRecord(AccessPlan plan, AccessBatch batch, AccessScopeTable scopeTable, AccessStageRecord stage, long failed)
    {
        AccessTransformRecord transform = new AccessTransformRecord();
        transform.setStageRecordId(stage.getStageRecordId());
        transform.setAccessBatchId(batch.getAccessBatchId());
        transform.setAccessPlanId(plan.getAccessPlanId());
        transform.setScopeTableId(scopeTable.getScopeTableId());
        transform.setTargetTable(scopeTable.getTargetTable());
        transform.setTargetPk(scopeTable.getTargetTable() + "_demo_" + batch.getAccessBatchId());
        transform.setTransformedData("{\"targetTable\":\"" + escapeJson(scopeTable.getTargetTable()) + "\",\"demo\":true}");
        transform.setTransformStatus(failed > 0 ? "failed" : "success");
        transform.setWriteStatus(failed > 0 ? "failed" : "inserted");
        transform.setErrorMessage(failed > 0 ? "演示失败：部分字段格式需要人工处理" : null);
        transform.setTransformedAt(DateUtils.getNowDate());
        transform.setWrittenAt(failed > 0 ? null : DateUtils.getNowDate());
        transform.setCreateTime(DateUtils.getNowDate());
        accessTransformRecordMapper.insertAccessTransformRecord(transform);
    }

    private AccessTableResult createTableResult(AccessPlan plan, AccessBatch batch, AccessScopeTable scopeTable, long inserted, long updated, long failed)
    {
        long success = inserted + updated;
        AccessTableResult result = new AccessTableResult();
        result.setAccessBatchId(batch.getAccessBatchId());
        result.setAccessPlanId(plan.getAccessPlanId());
        result.setScopeTableId(scopeTable.getScopeTableId());
        result.setSourceTable(scopeTable.getSourceTable());
        result.setTargetTable(scopeTable.getTargetTable());
        result.setResultStatus(failed > 0 ? "partial" : "success");
        result.setReadCount(success + failed);
        result.setStagedCount(success + failed);
        result.setInsertedCount(inserted);
        result.setUpdatedCount(updated);
        result.setSuccessCount(success);
        result.setFailedCount(failed);
        result.setTotalSuccessCount(sumPriorSuccess(plan.getAccessPlanId(), scopeTable.getTargetTable()) + success);
        result.setTotalFailedCount(sumPriorFailed(plan.getAccessPlanId(), scopeTable.getTargetTable()) + failed);
        result.setLastExecuteTime(DateUtils.getNowDate());
        result.setMessage("已完成读取、暂存、转换和目标表写入统计。");
        result.setCreateTime(DateUtils.getNowDate());
        accessTableResultMapper.insertAccessTableResult(result);
        return result;
    }

    private void createFieldResults(AccessPlan plan, AccessBatch batch, AccessScopeTable scopeTable, AccessTableResult tableResult, long failed)
    {
        AccessScopeField query = new AccessScopeField();
        query.setScopeTableId(scopeTable.getScopeTableId());
        for (AccessScopeField field : accessScopeFieldMapper.selectAccessScopeFieldList(query))
        {
            AccessFieldResult result = new AccessFieldResult();
            result.setTableResultId(tableResult.getTableResultId());
            result.setAccessBatchId(batch.getAccessBatchId());
            result.setAccessPlanId(plan.getAccessPlanId());
            result.setScopeFieldId(field.getScopeFieldId());
            result.setSourceColumn(field.getSourceColumn());
            result.setTargetColumn(field.getTargetColumn());
            result.setMappingType(field.getMappingType());
            result.setTransformStatus(failed > 0 ? "failed" : "success");
            result.setSuccessCount(tableResult.getSuccessCount());
            result.setFailedCount(failed);
            result.setErrorMessage(failed > 0 ? "演示失败：字段值格式不符合目标表要求" : null);
            result.setCreateTime(DateUtils.getNowDate());
            accessFieldResultMapper.insertAccessFieldResult(result);
        }
    }

    private void setDefaultSpecSet(MappingSpecSet specSet)
    {
        MappingSpecSet query = new MappingSpecSet();
        query.setSourceDatasourceId(specSet.getSourceDatasourceId());
        for (MappingSpecSet row : mappingSpecSetMapper.selectMappingSpecSetList(query))
        {
            row.setIsDefault(row.getSpecSetId().equals(specSet.getSpecSetId()) ? 1 : 0);
            row.setSpecStatus(row.getSpecSetId().equals(specSet.getSpecSetId()) ? "active" : row.getSpecStatus());
            row.setUpdateTime(DateUtils.getNowDate());
            mappingSpecSetMapper.updateMappingSpecSet(row);
        }
    }

    private void ensureSchema(Datasource datasource)
    {
        if (datasource.getLatestSchemaSnapshotId() == null)
        {
            createSchemaSnapshot(datasource);
        }
    }

    private Datasource requireDatasource(Long datasourceId)
    {
        if (datasourceId == null)
        {
            throw new ServiceException("来源数据源不能为空");
        }
        Datasource datasource = datasourceMapper.selectDatasourceByDatasourceId(datasourceId);
        if (datasource == null)
        {
            throw new ServiceException("来源数据源不存在");
        }
        return datasource;
    }

    private MatchTask requireTask(Long taskId)
    {
        if (taskId == null)
        {
            throw new ServiceException("模式映射任务 ID 不能为空");
        }
        MatchTask task = matchTaskMapper.selectMatchTaskByTaskId(taskId);
        if (task == null)
        {
            throw new ServiceException("模式映射任务不存在，请先刷新列表");
        }
        if (task.getSourceDatasourceId() == null)
        {
            throw new ServiceException("任务缺少来源数据源，不能生成结果集");
        }
        return task;
    }

    private Datasource findDatasourceByName(String name)
    {
        Datasource query = new Datasource();
        query.setDatasourceName(name);
        for (Datasource row : datasourceMapper.selectDatasourceList(query))
        {
            if (name.equals(row.getDatasourceName()))
            {
                return row;
            }
        }
        return null;
    }

    private MatchTask findTaskByName(String name)
    {
        MatchTask query = new MatchTask();
        query.setTaskName(name);
        for (MatchTask row : matchTaskMapper.selectMatchTaskList(query))
        {
            if (name.equals(row.getTaskName()))
            {
                return row;
            }
        }
        return null;
    }

    private AccessPlan findAccessPlanByName(String name)
    {
        AccessPlan query = new AccessPlan();
        query.setPlanName(name);
        for (AccessPlan row : accessPlanMapper.selectAccessPlanList(query))
        {
            if (name.equals(row.getPlanName()))
            {
                return row;
            }
        }
        return null;
    }

    private MatchResultSet findDefaultResultSet(Long taskId)
    {
        MatchResultSet query = new MatchResultSet();
        query.setTaskId(taskId);
        query.setIsDefault(1);
        List<MatchResultSet> rows = matchResultSetMapper.selectMatchResultSetList(query);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private MappingSpecSet findSpecSetByResultSet(Long resultSetId)
    {
        MappingSpecSet query = new MappingSpecSet();
        query.setResultSetId(resultSetId);
        List<MappingSpecSet> rows = mappingSpecSetMapper.selectMappingSpecSetList(query);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private MatchTaskVersion latestVersion(List<MatchTaskVersion> versions)
    {
        MatchTaskVersion latest = versions.get(0);
        for (MatchTaskVersion version : versions)
        {
            if (version.getVersionNo() != null && (latest.getVersionNo() == null || version.getVersionNo() > latest.getVersionNo()))
            {
                latest = version;
            }
        }
        return latest;
    }

    private long nextTaskVersionNo(Long taskId)
    {
        MatchTaskVersion query = new MatchTaskVersion();
        query.setTaskId(taskId);
        long max = 0L;
        for (MatchTaskVersion row : matchTaskVersionMapper.selectMatchTaskVersionList(query))
        {
            if (row.getVersionNo() != null && row.getVersionNo() > max)
            {
                max = row.getVersionNo();
            }
        }
        return max + 1L;
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

    private String[][] schemaEntityRows(Datasource datasource)
    {
        if ("api_pull".equals(datasource.getAccessMode()))
        {
            return new String[][] {
                { "equipment_asset", datasource.getDatasourceName(), "equipment_asset", "1200", "equipment_id,equipment_name,location_code" },
                { "service_event", datasource.getDatasourceName(), "service_event", "860", "event_id,service_unit_code,fault_time" },
                { "person_profile", datasource.getDatasourceName(), "person_profile", "420", "person_id,person_name,department" }
            };
        }
        return new String[][] {
            { "MES.equipment_asset", "MES", "equipment_asset", "1200", "equipment_id,equipmentname,equipment_type" },
            { "ERP.inventory_lot", "ERP", "inventory_lot", "860", "lot_id,wh_code,material_code" },
            { "MRO.service_event", "MRO", "service_event", "420", "event_id,service_unit_code,status" }
        };
    }

    private String[][] matchRowsFor(Long sourceDatasourceId)
    {
        Datasource datasource = datasourceMapper.selectDatasourceByDatasourceId(sourceDatasourceId);
        if (datasource != null && "api_pull".equals(datasource.getAccessMode()))
        {
            return new String[][] {
                { datasource.getDatasourceName(), "equipment_asset", "equipment_name", "equipment", "equipment_name", "1.000000" },
                { datasource.getDatasourceName(), "service_event", "service_unit_code", "maintenance_event", "service_unit_code", "1.000000" },
                { datasource.getDatasourceName(), "person_profile", "person_name", "person_profile", "person_name", "0.960000" },
                { datasource.getDatasourceName(), "service_event", "fault_time", "maintenance_event", "fault_time", "0.900000" },
                { datasource.getDatasourceName(), "equipment_asset", "location_code", "installed_position", "position_code", "0.860000" }
            };
        }
        return new String[][] {
            { "MES", "MES.equipment_asset", "equipmentname", "equipment", "equipment_name", "1.000000" },
            { "ERP", "ERP.inventory_lot", "wh_code", "inventory_batch", "warehouse_code", "1.000000" },
            { "MES", "MES.step_log", "stepsid", "step_execution", "step_no", "1.000000" },
            { "MRO", "MRO.service_event", "service_unit_code", "maintenance_event", "service_unit_code", "1.000000" },
            { "PLM", "PLM.gear_system_def", "position_code", "installed_position", "position_code", "0.860000" }
        };
    }

    private long tableInserted(String targetTable, String accessType, long sequence)
    {
        boolean continuous = "continuous".equals(accessType);
        if ("maintenance_event".equals(targetTable)) return continuous ? 580L + sequence * 20L : 860L;
        if ("installed_position".equals(targetTable)) return continuous ? 340L + sequence * 10L : 420L;
        if ("inventory_batch".equals(targetTable)) return continuous ? 120L : 860L;
        if ("equipment".equals(targetTable)) return continuous ? 300L : 1200L;
        if ("person_profile".equals(targetTable)) return continuous ? 90L : 420L;
        return continuous ? 80L : 260L;
    }

    private long tableUpdated(String targetTable)
    {
        if ("maintenance_event".equals(targetTable)) return 18L;
        if ("installed_position".equals(targetTable)) return 8L;
        if ("inventory_batch".equals(targetTable)) return 0L;
        return 6L;
    }

    private long value(Long value)
    {
        return value == null ? 0L : value;
    }

    private String escapeJson(String value)
    {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
