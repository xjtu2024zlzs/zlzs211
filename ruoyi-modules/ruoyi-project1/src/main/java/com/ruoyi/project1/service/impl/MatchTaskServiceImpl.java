package com.ruoyi.project1.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.project1.domain.ApiPullDatasource;
import com.ruoyi.project1.domain.Datasource;
import com.ruoyi.project1.domain.LlmConfig;
import com.ruoyi.project1.domain.MatchResultRow;
import com.ruoyi.project1.domain.MatchResultSet;
import com.ruoyi.project1.domain.MatchResultVersion;
import com.ruoyi.project1.domain.MatchTask;
import com.ruoyi.project1.domain.MatchTaskRecord;
import com.ruoyi.project1.domain.MatchTaskVersion;
import com.ruoyi.project1.domain.ReviewedMatch;
import com.ruoyi.project1.domain.TaskMetric;
import com.ruoyi.project1.mapper.ApiPullDatasourceMapper;
import com.ruoyi.project1.mapper.DatasourceMapper;
import com.ruoyi.project1.mapper.LlmConfigMapper;
import com.ruoyi.project1.mapper.MatchResultRowMapper;
import com.ruoyi.project1.mapper.MatchResultSetMapper;
import com.ruoyi.project1.mapper.MatchResultVersionMapper;
import com.ruoyi.project1.mapper.MatchTaskMapper;
import com.ruoyi.project1.mapper.MatchTaskRecordMapper;
import com.ruoyi.project1.mapper.MatchTaskVersionMapper;
import com.ruoyi.project1.mapper.ReviewedMatchMapper;
import com.ruoyi.project1.mapper.TaskMetricMapper;
import com.ruoyi.project1.service.IMatchTaskService;
import com.ruoyi.project1.service.support.AlgorithmMatchClient;

/**
 * Match task service.
 *
 * Version rules:
 * 1. versionId is a global database primary key and is never shown as Vn.
 * 2. versionNo is the per-task display version number.
 * 3. Creating a task creates V1. Editing task parameters creates V2/V3...
 * 4. Running a task creates a run record only; it does not create a version.
 */
@Service
public class MatchTaskServiceImpl implements IMatchTaskService
{
    private static final String DEFAULT_ENCODING_MODE = "header_values_default";
    private static final String DEFAULT_EMBEDDING_MODEL = "mpnet";
    private static final String DEFAULT_EVAL_MODE = "global_unknown";
    private static final String DEFAULT_PROVIDER = "deepseek";
    private static final String DEFAULT_MODEL = "deepseek-chat";
    private static final BigDecimal DEFAULT_THRESHOLD = new BigDecimal("0.9000");

    @Autowired
    private MatchTaskMapper matchTaskMapper;

    @Autowired
    private MatchTaskVersionMapper matchTaskVersionMapper;

    @Autowired
    private MatchTaskRecordMapper matchTaskRecordMapper;

    @Autowired
    private DatasourceMapper datasourceMapper;

    @Autowired
    private LlmConfigMapper llmConfigMapper;

    @Autowired
    private ApiPullDatasourceMapper apiPullDatasourceMapper;

    @Autowired
    private MatchResultVersionMapper matchResultVersionMapper;

    @Autowired
    private MatchResultSetMapper matchResultSetMapper;

    @Autowired
    private MatchResultRowMapper matchResultRowMapper;

    @Autowired
    private ReviewedMatchMapper reviewedMatchMapper;

    @Autowired
    private TaskMetricMapper taskMetricMapper;

    @Autowired
    private AlgorithmMatchClient algorithmMatchClient;

    @Autowired
    private ObjectMapper objectMapper;

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {};

    @Override
    public MatchTask selectMatchTaskByTaskId(Long taskId)
    {
        MatchTask task = requireTask(taskId);
        decorateTask(task);
        return task;
    }

    @Override
    public List<MatchTask> selectMatchTaskList(MatchTask matchTask)
    {
        List<MatchTask> rows = matchTaskMapper.selectMatchTaskList(matchTask);
        for (MatchTask row : rows)
        {
            decorateTask(row);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMatchTask(MatchTask matchTask)
    {
        validateTaskPayload(matchTask);
        matchTask.setCurrentVersionId(null);
        matchTask.setLifecycleStatus("active");
        matchTask.setLastRecordStatus("pending");
        matchTask.setLastRecordStage("等待运行");
        matchTask.setStatus(defaultString(matchTask.getStatus(), "0"));
        matchTask.setDelFlag(defaultString(matchTask.getDelFlag(), "0"));
        matchTask.setCreateTime(DateUtils.getNowDate());
        int rows = matchTaskMapper.insertMatchTask(matchTask);

        MatchTaskVersion version = createTaskVersion(matchTask, matchTask.getCurrentVersion(), "新增任务配置");
        MatchTask update = new MatchTask();
        update.setTaskId(matchTask.getTaskId());
        update.setCurrentVersionId(version.getVersionId());
        update.setUpdateTime(DateUtils.getNowDate());
        matchTaskMapper.updateMatchTask(update);
        matchTask.setCurrentVersionId(version.getVersionId());
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMatchTask(MatchTask matchTask)
    {
        validateTaskPayload(matchTask);
        MatchTask existing = requireTask(matchTask.getTaskId());

        MatchTask update = new MatchTask();
        update.setTaskId(existing.getTaskId());
        update.setTaskName(matchTask.getTaskName());
        update.setSourceDatasourceId(matchTask.getSourceDatasourceId());
        update.setLifecycleStatus(defaultString(existing.getLifecycleStatus(), "active"));
        update.setLastRecordStatus(defaultString(existing.getLastRecordStatus(), "pending"));
        update.setLastRecordStage(defaultString(existing.getLastRecordStage(), "等待运行"));
        update.setStatus(defaultString(existing.getStatus(), "0"));
        update.setDelFlag(defaultString(existing.getDelFlag(), "0"));
        update.setRemark(matchTask.getRemark());
        update.setUpdateTime(DateUtils.getNowDate());
        matchTaskMapper.updateMatchTask(update);

        update.setCurrentVersion(matchTask.getCurrentVersion());
        MatchTaskVersion version = createTaskVersion(update, matchTask.getCurrentVersion(), "修改任务参数配置，生成新版本");
        MatchTask versionUpdate = new MatchTask();
        versionUpdate.setTaskId(existing.getTaskId());
        versionUpdate.setCurrentVersionId(version.getVersionId());
        versionUpdate.setUpdateTime(DateUtils.getNowDate());
        return matchTaskMapper.updateMatchTask(versionUpdate);
    }

    @Override
    public int deleteMatchTaskByTaskIds(Long[] taskIds)
    {
        return matchTaskMapper.deleteMatchTaskByTaskIds(taskIds);
    }

    @Override
    public int deleteMatchTaskByTaskId(Long taskId)
    {
        return matchTaskMapper.deleteMatchTaskByTaskId(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> runWithoutAlgorithm(Long taskId)
    {
        MatchTask task = requireTask(taskId);
        MatchTaskVersion version = resolveCurrentVersion(task);
        if (version == null)
        {
            throw new ServiceException("模式映射任务缺少当前版本，请先保存任务参数");
        }

        Datasource datasource = datasourceMapper.selectDatasourceByDatasourceId(task.getSourceDatasourceId());
        if (datasource == null)
        {
            throw new ServiceException("来源数据源不存在，请先刷新数据源管理页面");
        }
        if (!"api_pull".equals(datasource.getAccessMode()))
        {
            throw new ServiceException("当前阶段模式匹配只支持 API 拉取数据源");
        }

        ApiPullDatasource apiPull = apiPullDatasourceMapper.selectApiPullDatasourceByDatasourceId(datasource.getDatasourceId());
        if (apiPull == null || StringUtils.isBlank(apiPull.getBaseUrl()))
        {
            throw new ServiceException("API 拉取数据源缺少基础地址，无法调用模式匹配算法");
        }

        Map<String, Object> requestPayload = buildAlgorithmRequest(task, version, datasource, apiPull);
        MatchTaskRecord record = createRunningRecord(task, version, requestPayload);
        try
        {
            Map<String, Object> response = algorithmMatchClient.run(requestPayload);
            MatchResultSet resultSet = persistAlgorithmResult(task, version, record, datasource, response);

            record.setDefaultResultSetId(resultSet.getResultSetId());
            record.setExecutionStatus("success");
            record.setCurrentStage("算法结果已入库");
            record.setProgress(100L);
            record.setResultsDir(asString(response.get("requestId")));
            record.setFinishedAt(DateUtils.getNowDate());
            matchTaskRecordMapper.updateMatchTaskRecord(record);

            MatchTask taskUpdate = new MatchTask();
            taskUpdate.setTaskId(task.getTaskId());
            taskUpdate.setLastRecordStatus("success");
            taskUpdate.setLastRecordStage("算法结果已入库");
            taskUpdate.setUpdateTime(DateUtils.getNowDate());
            matchTaskMapper.updateMatchTask(taskUpdate);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("taskId", task.getTaskId());
            payload.put("recordId", record.getRecordId());
            payload.put("resultSetId", resultSet.getResultSetId());
            payload.put("resultSetName", resultSet.getResultSetName());
            payload.put("rowCount", resultSet.getTotalRows());
            payload.put("message", "模式匹配算法运行完成，结果已写入若依业务表");
            return payload;
        }
        catch (RuntimeException e)
        {
            record.setExecutionStatus("failed");
            record.setCurrentStage("算法运行失败");
            record.setProgress(100L);
            record.setErrorMessage(e.getMessage());
            record.setFinishedAt(DateUtils.getNowDate());
            matchTaskRecordMapper.updateMatchTaskRecord(record);

            MatchTask taskUpdate = new MatchTask();
            taskUpdate.setTaskId(task.getTaskId());
            taskUpdate.setLastRecordStatus("failed");
            taskUpdate.setLastRecordStage("算法运行失败");
            taskUpdate.setUpdateTime(DateUtils.getNowDate());
            matchTaskMapper.updateMatchTask(taskUpdate);
            throw e;
        }
    }

    @Override
    public Map<String, Object> versions(Long taskId)
    {
        MatchTask task = requireTask(taskId);
        List<MatchTaskVersion> rows = listVersionsByTask(taskId);
        rows.sort(Comparator.comparing(MatchTaskVersion::getVersionNo, Comparator.nullsLast(Long::compareTo)).reversed());
        for (MatchTaskVersion row : rows)
        {
            decorateVersion(row, task.getCurrentVersionId());
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", task.getTaskId());
        payload.put("rows", rows);
        return payload;
    }

    @Override
    public Map<String, Object> records(Long taskId)
    {
        requireTask(taskId);
        List<MatchTaskRecord> records = listRecordsByTask(taskId);
        records.sort(Comparator.comparing(MatchTaskRecord::getRecordId, Comparator.nullsLast(Long::compareTo)).reversed());

        Map<Long, Long> versionNoMap = versionNoMap(taskId);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (MatchTaskRecord record : records)
        {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("recordId", record.getRecordId());
            row.put("recordLabel", "#" + record.getRecordId());
            row.put("versionId", record.getVersionId());
            row.put("versionNo", versionNoMap.get(record.getVersionId()));
            row.put("executionStatus", record.getExecutionStatus());
            row.put("progress", record.getProgress());
            row.put("currentStage", record.getCurrentStage());
            row.put("startedAt", record.getStartedAt());
            row.put("finishedAt", record.getFinishedAt());
            row.put("errorMessage", record.getErrorMessage());
            rows.add(row);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", taskId);
        payload.put("rows", rows);
        return payload;
    }

    private Map<String, Object> buildAlgorithmRequest(MatchTask task, MatchTaskVersion version, Datasource datasource, ApiPullDatasource apiPull)
    {
        String requestId = "match-" + task.getTaskId() + "-" + System.currentTimeMillis();
        Map<String, Object> params = parseJsonMap(version.getAlgorithmParamsJson());

        Map<String, Object> source = new LinkedHashMap<>();
        source.put("systemKey", resolveSourceSystemKey(datasource, apiPull));
        source.put("baseUrl", normalizeApiPullBaseUrl(apiPull.getBaseUrl()));
        source.put("tables", Collections.emptyList());
        source.put("limitPerTable", 300);

        Map<String, Object> target = new LinkedHashMap<>();
        target.put("mode", "mysql");
        target.put("tablePrefix", "p1p_dossier_");
        target.put("tables", Collections.emptyList());
        target.put("limitPerTable", 300);

        Map<String, Object> groundTruth = new LinkedHashMap<>();
        groundTruth.put("enabled", true);
        groundTruth.put("mode", "file");

        Map<String, Object> algorithm = new LinkedHashMap<>();
        algorithm.put("method", defaultString(asString(params.get("method")), "Magneto"));
        algorithm.put("embeddingModel", defaultString(version.getEmbeddingModelKey(), DEFAULT_EMBEDDING_MODEL));
        algorithm.put("encodingMode", defaultString(version.getEncodingModeKey(), DEFAULT_ENCODING_MODE));
        algorithm.put("samplingMode", defaultString(asString(params.get("samplingMode")), "mixed"));
        algorithm.put("samplingSize", asNumber(params.get("samplingSize"), 10));
        algorithm.put("topk", asNumber(params.get("topk"), 20));
        algorithm.put("threshold", asDouble(params.get("threshold"), 0.0D));
        algorithm.put("useLlmReranker", false);
        algorithm.put("boostThreshold", asDouble(params.get("boostThreshold"), 0.8D));
        algorithm.put("boostAlpha", asDouble(params.get("boostAlpha"), 0.15D));

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("requestId", requestId);
        request.put("source", source);
        request.put("target", target);
        request.put("groundTruth", groundTruth);
        request.put("algorithm", algorithm);
        return request;
    }

    private MatchTaskRecord createRunningRecord(MatchTask task, MatchTaskVersion version, Map<String, Object> requestPayload)
    {
        MatchTaskRecord record = new MatchTaskRecord();
        record.setTaskId(task.getTaskId());
        record.setVersionId(version.getVersionId());
        record.setSchemaSnapshotId(version.getSchemaSnapshotId());
        record.setExecutionStatus("running");
        record.setCurrentStage("调用模式匹配算法");
        record.setProgress(20L);
        record.setAlgorithmParamsSnapshot(toJson(requestPayload));
        record.setStartedAt(DateUtils.getNowDate());
        record.setCreateTime(DateUtils.getNowDate());
        matchTaskRecordMapper.insertMatchTaskRecord(record);

        MatchTask taskUpdate = new MatchTask();
        taskUpdate.setTaskId(task.getTaskId());
        taskUpdate.setLastRecordStatus("running");
        taskUpdate.setLastRecordStage("调用模式匹配算法");
        taskUpdate.setUpdateTime(DateUtils.getNowDate());
        matchTaskMapper.updateMatchTask(taskUpdate);
        return record;
    }

    private MatchResultSet persistAlgorithmResult(MatchTask task, MatchTaskVersion version, MatchTaskRecord record, Datasource datasource, Map<String, Object> response)
    {
        Map<String, Object> summary = asMap(response.get("summary"));
        List<Map<String, Object>> selectedRows = mapList(response.get("oneToOneRows"));
        if (selectedRows.isEmpty())
        {
            selectedRows = mapList(response.get("rows"));
        }
        if (selectedRows.isEmpty())
        {
            throw new ServiceException("算法服务未返回字段匹配结果");
        }

        MatchResultVersion resultVersion = new MatchResultVersion();
        resultVersion.setTaskId(task.getTaskId());
        resultVersion.setVersionId(version.getVersionId());
        resultVersion.setRecordId(record.getRecordId());
        resultVersion.setVersionLabel("RUN-" + record.getRecordId());
        resultVersion.setCreateTime(DateUtils.getNowDate());
        matchResultVersionMapper.insertMatchResultVersion(resultVersion);

        MatchResultSet resultSet = new MatchResultSet();
        resultSet.setResultVersionId(resultVersion.getResultVersionId());
        resultSet.setTaskId(task.getTaskId());
        resultSet.setVersionId(version.getVersionId());
        resultSet.setRecordId(record.getRecordId());
        resultSet.setSourceDatasourceId(datasource.getDatasourceId());
        resultSet.setMethod(defaultString(asString(response.get("method")), "Magneto"));
        resultSet.setVariant(defaultString(version.getEncodingModeKey(), DEFAULT_ENCODING_MODE));
        resultSet.setResultSetName(resultSet.getMethod() + " CF-SF one2one");
        resultSet.setIsDefault(0);
        resultSet.setTotalRows((long) selectedRows.size());
        resultSet.setAvgScore(asBigDecimal(summary.get("avgScore"), averageScore(selectedRows)));
        resultSet.setCreateTime(DateUtils.getNowDate());
        matchResultSetMapper.insertMatchResultSet(resultSet);

        long rowNo = 1L;
        for (Map<String, Object> rowPayload : selectedRows)
        {
            MatchResultRow row = new MatchResultRow();
            row.setResultSetId(resultSet.getResultSetId());
            row.setRowNo(rowNo++);
            row.setSourceDatabase(asString(rowPayload.get("sourceDatabase")));
            row.setSourceTable(asString(rowPayload.get("sourceTable")));
            row.setSourceColumn(asString(rowPayload.get("sourceColumn")));
            row.setTargetTable(normalizeTargetTableForBusiness(asString(rowPayload.get("targetTable"))));
            row.setTargetColumn(asString(rowPayload.get("targetColumn")));
            row.setScore(asBigDecimal(rowPayload.get("score"), BigDecimal.ZERO));
            row.setRawPayload(toJson(rowPayload));
            row.setCreateTime(DateUtils.getNowDate());
            matchResultRowMapper.insertMatchResultRow(row);

            persistInitialReview(task, version, record, datasource, resultSet, row);
        }

        persistMetrics(task, version, record, resultSet, response);
        return resultSet;
    }

    private void persistInitialReview(MatchTask task, MatchTaskVersion version, MatchTaskRecord record, Datasource datasource,
            MatchResultSet resultSet, MatchResultRow row)
    {
        BigDecimal threshold = version.getAutoApproveThreshold() == null ? DEFAULT_THRESHOLD : version.getAutoApproveThreshold();
        boolean autoApproved = row.getScore() != null && row.getScore().compareTo(threshold) >= 0;

        ReviewedMatch review = new ReviewedMatch();
        review.setTaskId(task.getTaskId());
        review.setVersionId(version.getVersionId());
        review.setRecordId(record.getRecordId());
        review.setResultSetId(resultSet.getResultSetId());
        review.setResultRowId(row.getResultRowId());
        review.setSourceDatasourceId(datasource.getDatasourceId());
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
        review.setReviewedBy(autoApproved ? "system" : null);
        review.setReviewedAt(autoApproved ? DateUtils.getNowDate() : null);
        review.setCreateTime(DateUtils.getNowDate());
        reviewedMatchMapper.insertReviewedMatch(review);
    }

    private void persistMetrics(MatchTask task, MatchTaskVersion version, MatchTaskRecord record, MatchResultSet resultSet, Map<String, Object> response)
    {
        for (Map<String, Object> metricPayload : mapList(response.get("metrics")))
        {
            TaskMetric metric = new TaskMetric();
            metric.setTaskId(task.getTaskId());
            metric.setVersionId(version.getVersionId());
            metric.setRecordId(record.getRecordId());
            metric.setResultSetId(resultSet.getResultSetId());
            metric.setMetricKey(firstNonBlank(asString(metricPayload.get("metricKey")), asString(metricPayload.get("key"))));
            metric.setMetricName(firstNonBlank(asString(metricPayload.get("metricName")), asString(metricPayload.get("name")), metric.getMetricKey()));
            metric.setMetricValue(asBigDecimal(firstNonNull(metricPayload.get("metricValue"), metricPayload.get("value")), BigDecimal.ZERO));
            metric.setChartType("summary");
            metric.setResultDir(asString(response.get("requestId")));
            metric.setCreateTime(DateUtils.getNowDate());
            taskMetricMapper.insertTaskMetric(metric);
        }

        Map<String, Object> charts = asMap(response.get("charts"));
        insertChartMetric(task, version, record, resultSet, "scoreDistribution", "匹配分数分布", "bar", charts.get("scoreDistribution"), response);
        insertChartMetric(task, version, record, resultSet, "targetTopDistribution", "目标表 Top 分布", "bar", charts.get("targetTopDistribution"), response);
    }

    private void insertChartMetric(MatchTask task, MatchTaskVersion version, MatchTaskRecord record, MatchResultSet resultSet,
            String key, String name, String chartType, Object chartData, Map<String, Object> response)
    {
        if (chartData == null)
        {
            return;
        }
        TaskMetric metric = new TaskMetric();
        metric.setTaskId(task.getTaskId());
        metric.setVersionId(version.getVersionId());
        metric.setRecordId(record.getRecordId());
        metric.setResultSetId(resultSet.getResultSetId());
        metric.setMetricKey(key);
        metric.setMetricName(name);
        metric.setChartType(chartType);
        metric.setChartData(toJson(chartData));
        metric.setResultDir(asString(response.get("requestId")));
        metric.setCreateTime(DateUtils.getNowDate());
        taskMetricMapper.insertTaskMetric(metric);
    }

    private String normalizeApiPullBaseUrl(String baseUrl)
    {
        if (StringUtils.isBlank(baseUrl))
        {
            throw new ServiceException("API 拉取数据源缺少基础地址，无法调用模式匹配算法");
        }
        String normalized = baseUrl.trim();
        while (normalized.endsWith("/"))
        {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        String lower = normalized.toLowerCase();
        for (String endpoint : new String[] { "/api/pull", "/api/schema", "/api/health" })
        {
            if (lower.endsWith(endpoint))
            {
                normalized = normalized.substring(0, normalized.length() - endpoint.length());
                break;
            }
        }
        return normalized;
    }

    private String resolveSourceSystemKey(Datasource datasource, ApiPullDatasource apiPull)
    {
        String text = (defaultString(datasource.getDatasourceName(), "") + " " + defaultString(apiPull.getBaseUrl(), "")).toLowerCase();
        if (text.contains("9101") || text.contains("plm"))
        {
            return "plm";
        }
        if (text.contains("9102") || text.contains("erp"))
        {
            return "erp";
        }
        if (text.contains("9103") || text.contains("mes"))
        {
            return "mes";
        }
        if (text.contains("9104") || text.contains("qms"))
        {
            return "qms";
        }
        if (text.contains("9105") || text.contains("mro"))
        {
            return "mro";
        }
        throw new ServiceException("无法识别 API Pull 数据源所属 CF 系统，请在数据源名称中包含 PLM/ERP/MES/QMS/MRO");
    }

    private String normalizeTargetTableForBusiness(String targetTable)
    {
        if (StringUtils.isBlank(targetTable))
        {
            return targetTable;
        }
        return targetTable.startsWith("p1p_dossier_") ? targetTable.substring("p1p_dossier_".length()) : targetTable;
    }

    private void validateTaskPayload(MatchTask matchTask)
    {
        if (matchTask == null)
        {
            throw new ServiceException("模式映射任务不能为空");
        }
        if (StringUtils.isBlank(matchTask.getTaskName()))
        {
            throw new ServiceException("任务名称不能为空");
        }
        if (matchTask.getSourceDatasourceId() == null)
        {
            throw new ServiceException("源数据源不能为空");
        }
    }

    private MatchTaskVersion createTaskVersion(MatchTask task, MatchTaskVersion payload, String defaultSummary)
    {
        MatchTaskVersion source = payload == null ? new MatchTaskVersion() : payload;
        Datasource datasource = datasourceMapper.selectDatasourceByDatasourceId(task.getSourceDatasourceId());

        MatchTaskVersion version = new MatchTaskVersion();
        version.setTaskId(task.getTaskId());
        version.setVersionNo(nextTaskVersionNo(task.getTaskId()));
        version.setSourceDatasourceId(task.getSourceDatasourceId());
        version.setSchemaSnapshotId(datasource == null ? null : datasource.getLatestSchemaSnapshotId());
        version.setTargetKey("dossier");
        version.setEncodingModeKey(defaultString(source.getEncodingModeKey(), DEFAULT_ENCODING_MODE));
        version.setEmbeddingModelKey(defaultString(source.getEmbeddingModelKey(), DEFAULT_EMBEDDING_MODEL));
        version.setEvalModeKey(defaultString(source.getEvalModeKey(), DEFAULT_EVAL_MODE));
        version.setAutoApproveThreshold(source.getAutoApproveThreshold() == null ? DEFAULT_THRESHOLD : source.getAutoApproveThreshold());
        version.setLlmConfigId(resolveLlmConfigId(source));
        version.setAlgorithmParamsJson(defaultString(source.getAlgorithmParamsJson(), buildAlgorithmParamsJson(version)));
        version.setChangeSummary(defaultString(source.getChangeSummary(), defaultSummary));
        version.setCreateTime(DateUtils.getNowDate());
        matchTaskVersionMapper.insertMatchTaskVersion(version);
        return version;
    }

    private Long resolveLlmConfigId(MatchTaskVersion version)
    {
        if (version.getLlmConfigId() != null)
        {
            return version.getLlmConfigId();
        }

        String provider = defaultString(version.getLlmProvider(), DEFAULT_PROVIDER);
        String model = defaultString(version.getLlmModel(), DEFAULT_MODEL);

        LlmConfig query = new LlmConfig();
        query.setProvider(provider);
        query.setModelName(model);
        List<LlmConfig> rows = llmConfigMapper.selectLlmConfigList(query);
        for (LlmConfig row : rows)
        {
            if (provider.equals(row.getProvider()) && model.equals(row.getModelName()))
            {
                return row.getLlmConfigId();
            }
        }

        LlmConfig config = new LlmConfig();
        config.setConfigName(provider + "-" + model);
        config.setProvider(provider);
        config.setModelName(model);
        config.setEnabled(1);
        config.setCreateTime(DateUtils.getNowDate());
        llmConfigMapper.insertLlmConfig(config);
        return config.getLlmConfigId();
    }

    private String buildAlgorithmParamsJson(MatchTaskVersion version)
    {
        return "{\"method\":\"Magneto\","
            + "\"externalAlgorithm\":true,"
            + "\"encodingMode\":\"" + escapeJson(version.getEncodingModeKey()) + "\","
            + "\"embeddingModel\":\"" + escapeJson(version.getEmbeddingModelKey()) + "\","
            + "\"evalMode\":\"" + escapeJson(version.getEvalModeKey()) + "\"}";
    }

    private void decorateTask(MatchTask task)
    {
        MatchTaskVersion version = resolveCurrentVersion(task);
        if (version != null)
        {
            decorateVersion(version, task.getCurrentVersionId());
            task.setCurrentVersion(version);
            task.setCurrentVersionNo(version.getVersionNo());
            task.setCurrentEncodingModeKey(version.getEncodingModeKey());
            task.setCurrentEmbeddingModelKey(version.getEmbeddingModelKey());
            task.setCurrentEvalModeKey(version.getEvalModeKey());
            task.setCurrentAutoApproveThreshold(version.getAutoApproveThreshold());
        }

        MatchTaskRecord record = latestRecord(task.getTaskId());
        if (record != null)
        {
            task.setLastRecordStatus(record.getExecutionStatus());
            task.setLastRecordStage(record.getCurrentStage());
            task.setLastRecordProgress(record.getProgress());
        }
        else
        {
            task.setLastRecordStatus(defaultString(task.getLastRecordStatus(), "pending"));
            task.setLastRecordStage(defaultString(task.getLastRecordStage(), "等待运行"));
            task.setLastRecordProgress(0L);
        }
    }

    private MatchTaskVersion resolveCurrentVersion(MatchTask task)
    {
        if (task.getCurrentVersionId() != null)
        {
            MatchTaskVersion version = matchTaskVersionMapper.selectMatchTaskVersionByVersionId(task.getCurrentVersionId());
            if (version != null)
            {
                return version;
            }
        }

        List<MatchTaskVersion> rows = listVersionsByTask(task.getTaskId());
        rows.sort(Comparator.comparing(MatchTaskVersion::getVersionNo, Comparator.nullsLast(Long::compareTo)).reversed());
        return rows.isEmpty() ? null : rows.get(0);
    }

    private MatchTaskRecord latestRecord(Long taskId)
    {
        List<MatchTaskRecord> rows = listRecordsByTask(taskId);
        rows.sort(Comparator.comparing(MatchTaskRecord::getRecordId, Comparator.nullsLast(Long::compareTo)).reversed());
        return rows.isEmpty() ? null : rows.get(0);
    }

    private void decorateVersion(MatchTaskVersion version, Long currentVersionId)
    {
        if (version.getLlmConfigId() != null)
        {
            LlmConfig config = llmConfigMapper.selectLlmConfigByLlmConfigId(version.getLlmConfigId());
            if (config != null)
            {
                version.setLlmProvider(config.getProvider());
                version.setLlmModel(config.getModelName());
            }
        }
        version.setLlmProvider(defaultString(version.getLlmProvider(), DEFAULT_PROVIDER));
        version.setLlmModel(defaultString(version.getLlmModel(), DEFAULT_MODEL));
        version.setCurrent(currentVersionId != null && currentVersionId.equals(version.getVersionId()));
    }

    private long nextTaskVersionNo(Long taskId)
    {
        long max = 0L;
        for (MatchTaskVersion version : listVersionsByTask(taskId))
        {
            if (version.getVersionNo() != null && version.getVersionNo() > max)
            {
                max = version.getVersionNo();
            }
        }
        return max + 1L;
    }

    private List<MatchTaskVersion> listVersionsByTask(Long taskId)
    {
        MatchTaskVersion query = new MatchTaskVersion();
        query.setTaskId(taskId);
        return matchTaskVersionMapper.selectMatchTaskVersionList(query);
    }

    private List<MatchTaskRecord> listRecordsByTask(Long taskId)
    {
        MatchTaskRecord query = new MatchTaskRecord();
        query.setTaskId(taskId);
        return matchTaskRecordMapper.selectMatchTaskRecordList(query);
    }

    private Map<Long, Long> versionNoMap(Long taskId)
    {
        Map<Long, Long> rows = new HashMap<>();
        for (MatchTaskVersion version : listVersionsByTask(taskId))
        {
            rows.put(version.getVersionId(), version.getVersionNo());
        }
        return rows;
    }

    private Map<String, Object> parseJsonMap(String json)
    {
        if (StringUtils.isBlank(json))
        {
            return Collections.emptyMap();
        }
        try
        {
            return objectMapper.readValue(json, MAP_TYPE);
        }
        catch (Exception e)
        {
            return Collections.emptyMap();
        }
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

    private String asString(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }

    private int asNumber(Object value, int defaultValue)
    {
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        if (value != null)
        {
            try
            {
                return Integer.parseInt(String.valueOf(value));
            }
            catch (NumberFormatException ignored)
            {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private double asDouble(Object value, double defaultValue)
    {
        if (value instanceof Number)
        {
            return ((Number) value).doubleValue();
        }
        if (value != null)
        {
            try
            {
                return Double.parseDouble(String.valueOf(value));
            }
            catch (NumberFormatException ignored)
            {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private BigDecimal asBigDecimal(Object value, BigDecimal defaultValue)
    {
        if (value instanceof BigDecimal)
        {
            return (BigDecimal) value;
        }
        if (value instanceof Number)
        {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value != null)
        {
            try
            {
                return new BigDecimal(String.valueOf(value));
            }
            catch (NumberFormatException ignored)
            {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private BigDecimal averageScore(List<Map<String, Object>> rows)
    {
        if (rows == null || rows.isEmpty())
        {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> row : rows)
        {
            total = total.add(asBigDecimal(row.get("score"), BigDecimal.ZERO));
        }
        return total.divide(BigDecimal.valueOf(rows.size()), 6, java.math.RoundingMode.HALF_UP);
    }

    private Object firstNonNull(Object... values)
    {
        if (values == null)
        {
            return null;
        }
        for (Object value : values)
        {
            if (value != null)
            {
                return value;
            }
        }
        return null;
    }

    private String firstNonBlank(String... values)
    {
        if (values == null)
        {
            return null;
        }
        for (String value : values)
        {
            if (StringUtils.isNotBlank(value))
            {
                return value;
            }
        }
        return null;
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
        return task;
    }

    private String defaultString(String value, String defaultValue)
    {
        return StringUtils.isBlank(value) ? defaultValue : value;
    }

    private String escapeJson(String value)
    {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
