package com.ruoyi.project1.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
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
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {};

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

    @Autowired
    private TransactionTemplate transactionTemplate;

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
    public Map<String, Object> runMatchTask(Long taskId)
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
        CompletableFuture.runAsync(() -> executeMatchTaskInBackground(task.getTaskId(), version.getVersionId(),
                datasource.getDatasourceId(), record.getRecordId(), requestPayload));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", task.getTaskId());
        payload.put("recordId", record.getRecordId());
        payload.put("executionStatus", record.getExecutionStatus());
        payload.put("progress", record.getProgress());
        payload.put("currentStage", record.getCurrentStage());
        payload.put("message", "模式映射任务已开始运行，正在生成候选结果集");
        return payload;
    }

    @Override
    public Map<String, Object> recordStatus(Long recordId)
    {
        MatchTaskRecord record = matchTaskRecordMapper.selectMatchTaskRecordByRecordId(recordId);
        if (record == null)
        {
            throw new ServiceException("运行记录不存在，请刷新页面后重试");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("recordId", record.getRecordId());
        payload.put("taskId", record.getTaskId());
        payload.put("versionId", record.getVersionId());
        payload.put("executionStatus", record.getExecutionStatus());
        payload.put("progress", record.getProgress());
        payload.put("currentStage", record.getCurrentStage());
        String recordMessage = record.getErrorMessage();
        payload.put("noticeMessage", noticeMessage(recordMessage));
        payload.put("errorMessage", noticeMessage(recordMessage) == null ? recordMessage : null);
        payload.put("resultSetCount", countResultSets(recordId));
        payload.put("startedAt", record.getStartedAt());
        payload.put("finishedAt", record.getFinishedAt());
        return payload;
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

    private void executeMatchTaskInBackground(Long taskId, Long versionId, Long datasourceId, Long recordId,
            Map<String, Object> requestPayload)
    {
        MatchTask task = matchTaskMapper.selectMatchTaskByTaskId(taskId);
        MatchTaskVersion version = matchTaskVersionMapper.selectMatchTaskVersionByVersionId(versionId);
        Datasource datasource = datasourceMapper.selectDatasourceByDatasourceId(datasourceId);
        MatchTaskRecord record = matchTaskRecordMapper.selectMatchTaskRecordByRecordId(recordId);
        if (task == null || version == null || datasource == null || record == null)
        {
            markRecordFailed(recordId, taskId, "任务上下文缺失，无法继续执行");
            return;
        }

        try
        {
            updateRunProgress(recordId, taskId, 15L, "构造源模式和目标模式", null);
            updateRunProgress(recordId, taskId, 30L, "调用模式匹配算法", null);
            Map<String, Object> response = algorithmMatchClient.run(requestPayload);
            int resultSetCount = asNumber(response.get("resultSetCount"), mapList(response.get("resultSets")).size());
            updateRunProgress(recordId, taskId, 55L, "解析 " + resultSetCount + " 个候选结果集", null);

            transactionTemplate.execute(status -> {
                persistAlgorithmResultSets(task, version, record, datasource, response);
                return null;
            });

            updateRunProgress(recordId, taskId, 90L, "初始化审核记录", null);
            String successStage = "算法结果已入库，生成 " + resultSetCount + " 个候选结果集";
            String notice = firstWarningMessage(response);
            MatchTaskRecord finishedRecord = new MatchTaskRecord();
            finishedRecord.setRecordId(recordId);
            finishedRecord.setExecutionStatus("success");
            finishedRecord.setCurrentStage(successStage);
            finishedRecord.setProgress(100L);
            finishedRecord.setResultsDir(asString(response.get("requestId")));
            finishedRecord.setErrorMessage(StringUtils.isBlank(notice) ? null : "NOTICE:" + notice);
            finishedRecord.setFinishedAt(DateUtils.getNowDate());
            matchTaskRecordMapper.updateMatchTaskRecord(finishedRecord);

            MatchTask taskUpdate = new MatchTask();
            taskUpdate.setTaskId(taskId);
            taskUpdate.setLastRecordStatus("success");
            taskUpdate.setLastRecordStage(successStage);
            taskUpdate.setUpdateTime(DateUtils.getNowDate());
            matchTaskMapper.updateMatchTask(taskUpdate);
        }
        catch (RuntimeException e)
        {
            markRecordFailed(recordId, taskId, e.getMessage());
        }
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
        algorithm.put("methods", List.of("Magneto", "MagnetoBoost", "MagnetoGPT"));
        algorithm.put("variants", List.of("all", "one2one"));
        algorithm.put("embeddingModel", defaultString(version.getEmbeddingModelKey(), DEFAULT_EMBEDDING_MODEL));
        algorithm.put("encodingMode", defaultString(version.getEncodingModeKey(), DEFAULT_ENCODING_MODE));
        algorithm.put("evalMode", defaultString(version.getEvalModeKey(), DEFAULT_EVAL_MODE));
        String llmModel = resolveLlmModel(version);
        if (StringUtils.isNotBlank(llmModel))
        {
            algorithm.put("llmModel", llmModel);
        }
        algorithm.put("samplingMode", defaultString(asString(params.get("samplingMode")), "mixed"));
        algorithm.put("samplingSize", asNumber(params.get("samplingSize"), 10));
        algorithm.put("topk", asNumber(params.get("topk"), 20));
        algorithm.put("threshold", asDouble(params.get("threshold"), 0.0D));
        algorithm.put("useLlmReranker", StringUtils.isNotBlank(llmModel));
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
        record.setCurrentStage("创建运行记录");
        record.setProgress(5L);
        record.setAlgorithmParamsSnapshot(toJson(requestPayload));
        record.setStartedAt(DateUtils.getNowDate());
        record.setCreateTime(DateUtils.getNowDate());
        matchTaskRecordMapper.insertMatchTaskRecord(record);

        MatchTask taskUpdate = new MatchTask();
        taskUpdate.setTaskId(task.getTaskId());
        taskUpdate.setLastRecordStatus("running");
        taskUpdate.setLastRecordStage(record.getCurrentStage());
        taskUpdate.setUpdateTime(DateUtils.getNowDate());
        matchTaskMapper.updateMatchTask(taskUpdate);
        return record;
    }

    private List<MatchResultSet> persistAlgorithmResultSets(MatchTask task, MatchTaskVersion version, MatchTaskRecord record,
            Datasource datasource, Map<String, Object> response)
    {
        List<Map<String, Object>> resultSetPayloads = mapList(response.get("resultSets"));
        if (resultSetPayloads.isEmpty())
        {
            resultSetPayloads = legacyResultSets(response);
        }
        if (resultSetPayloads.isEmpty())
        {
            throw new ServiceException("算法服务未返回 resultSets，无法生成候选结果集");
        }

        MatchResultVersion resultVersion = new MatchResultVersion();
        resultVersion.setTaskId(task.getTaskId());
        resultVersion.setVersionId(version.getVersionId());
        resultVersion.setRecordId(record.getRecordId());
        resultVersion.setVersionLabel("RUN-" + record.getRecordId());
        resultVersion.setCreateTime(DateUtils.getNowDate());
        matchResultVersionMapper.insertMatchResultVersion(resultVersion);

        List<MatchResultSet> resultSets = new ArrayList<>();
        for (Map<String, Object> resultSetPayload : resultSetPayloads)
        {
            MatchResultSet resultSet = persistOneResultSet(task, version, record, datasource, resultVersion, response, resultSetPayload);
            resultSets.add(resultSet);
        }
        return resultSets;
    }

    private MatchResultSet persistOneResultSet(MatchTask task, MatchTaskVersion version, MatchTaskRecord record,
            Datasource datasource, MatchResultVersion resultVersion, Map<String, Object> response, Map<String, Object> resultSetPayload)
    {
        Map<String, Object> summary = asMap(resultSetPayload.get("summary"));
        List<Map<String, Object>> rows = mapList(resultSetPayload.get("rows"));

        String method = defaultString(asString(resultSetPayload.get("method")), "Magneto");
        String variant = defaultString(asString(resultSetPayload.get("variant")), "one2one");
        String resultSetName = defaultString(asString(resultSetPayload.get("resultSetName")), method + " CF-SF " + variant);

        MatchResultSet resultSet = new MatchResultSet();
        resultSet.setResultVersionId(resultVersion.getResultVersionId());
        resultSet.setTaskId(task.getTaskId());
        resultSet.setVersionId(version.getVersionId());
        resultSet.setRecordId(record.getRecordId());
        resultSet.setSourceDatasourceId(datasource.getDatasourceId());
        resultSet.setMethod(method);
        resultSet.setVariant(variant);
        resultSet.setResultSetName(resultSetName);
        resultSet.setIsDefault(0);
        resultSet.setTotalRows(asLong(summary.get("totalRows"), rows.size()));
        resultSet.setAvgScore(asBigDecimal(summary.get("avgScore"), averageScore(rows)));
        resultSet.setCreateTime(DateUtils.getNowDate());
        matchResultSetMapper.insertMatchResultSet(resultSet);

        long fallbackRowNo = 1L;
        for (Map<String, Object> rowPayload : rows)
        {
            MatchResultRow row = new MatchResultRow();
            row.setResultSetId(resultSet.getResultSetId());
            row.setRowNo(asLong(rowPayload.get("rowNo"), fallbackRowNo++));
            row.setSourceDatabase(asString(rowPayload.get("sourceDatabase")));
            row.setSourceTable(asString(rowPayload.get("sourceTable")));
            row.setSourceColumn(asString(rowPayload.get("sourceColumn")));
            row.setTargetTable(normalizeTargetTableForBusiness(firstNonBlank(asString(rowPayload.get("targetTable")),
                    asString(rowPayload.get("targetPhysicalTable")))));
            row.setTargetColumn(asString(rowPayload.get("targetColumn")));
            row.setScore(asBigDecimal(rowPayload.get("score"), BigDecimal.ZERO));
            row.setRawPayload(toJson(rowPayload));
            row.setCreateTime(DateUtils.getNowDate());
            matchResultRowMapper.insertMatchResultRow(row);

            persistInitialReview(task, version, record, datasource, resultSet, row);
        }

        persistMetrics(task, version, record, resultSet, response, resultSetPayload);
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

    private void persistMetrics(MatchTask task, MatchTaskVersion version, MatchTaskRecord record, MatchResultSet resultSet,
            Map<String, Object> response, Map<String, Object> resultSetPayload)
    {
        String resultDir = asString(response.get("requestId"));
        Map<String, Object> summary = asMap(resultSetPayload.get("metricsSummary"));
        insertSummaryMetric(task, version, record, resultSet, "mrr", "MRR", summary.get("mrr"), resultDir);
        insertSummaryMetric(task, version, record, resultSet, "recallAt20", "Recall@20", summary.get("recallAt20"), resultDir);
        insertSummaryMetric(task, version, record, resultSet, "precision", "Precision", summary.get("precision"), resultDir);
        insertSummaryMetric(task, version, record, resultSet, "recall", "Recall", summary.get("recall"), resultDir);
        insertSummaryMetric(task, version, record, resultSet, "f1Score", "F1 Score", summary.get("f1Score"), resultDir);
        insertSummaryMetric(task, version, record, resultSet, "matchCount", "匹配数", summary.get("matchCount"), resultDir);

        for (Map<String, Object> metricPayload : mapList(resultSetPayload.get("metrics")))
        {
            String metricKey = firstNonBlank(asString(metricPayload.get("metricKey")), asString(metricPayload.get("key")));
            if (StringUtils.isBlank(metricKey) || hasMetric(resultSet.getResultSetId(), metricKey))
            {
                continue;
            }
            TaskMetric metric = new TaskMetric();
            metric.setTaskId(task.getTaskId());
            metric.setVersionId(version.getVersionId());
            metric.setRecordId(record.getRecordId());
            metric.setResultSetId(resultSet.getResultSetId());
            metric.setMetricKey(metricKey);
            metric.setMetricName(firstNonBlank(asString(metricPayload.get("metricName")), asString(metricPayload.get("name")), metricKey));
            metric.setMetricValue(asBigDecimal(firstNonNull(metricPayload.get("metricValue"), metricPayload.get("value")), BigDecimal.ZERO));
            metric.setChartType("summary");
            metric.setResultDir(resultDir);
            metric.setCreateTime(DateUtils.getNowDate());
            taskMetricMapper.insertTaskMetric(metric);
        }

        Map<String, Object> charts = asMap(resultSetPayload.get("charts"));
        insertChartMetric(task, version, record, resultSet, "scoreDistribution", "匹配分数分布", "bar",
                charts.get("scoreDistribution"), resultDir);
        insertChartMetric(task, version, record, resultSet, "targetTopDistribution", "目标表 Top 分布", "bar",
                charts.get("targetTopDistribution"), resultDir);
    }

    private void insertSummaryMetric(MatchTask task, MatchTaskVersion version, MatchTaskRecord record, MatchResultSet resultSet,
            String key, String name, Object value, String resultDir)
    {
        if (value == null)
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
        metric.setMetricValue(asBigDecimal(value, BigDecimal.ZERO));
        metric.setChartType("card");
        metric.setResultDir(resultDir);
        metric.setCreateTime(DateUtils.getNowDate());
        taskMetricMapper.insertTaskMetric(metric);
    }

    private void insertChartMetric(MatchTask task, MatchTaskVersion version, MatchTaskRecord record, MatchResultSet resultSet,
            String key, String name, String chartType, Object chartData, String resultDir)
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
        metric.setResultDir(resultDir);
        metric.setCreateTime(DateUtils.getNowDate());
        taskMetricMapper.insertTaskMetric(metric);
    }

    private boolean hasMetric(Long resultSetId, String metricKey)
    {
        TaskMetric query = new TaskMetric();
        query.setResultSetId(resultSetId);
        query.setMetricKey(metricKey);
        return !taskMetricMapper.selectTaskMetricList(query).isEmpty();
    }

    private List<Map<String, Object>> legacyResultSets(Map<String, Object> response)
    {
        List<Map<String, Object>> rows = mapList(firstNonNull(response.get("oneToOneRows"), response.get("rows")));
        if (rows.isEmpty())
        {
            return Collections.emptyList();
        }
        Map<String, Object> legacy = new LinkedHashMap<>();
        String method = defaultString(asString(response.get("method")), "Magneto");
        legacy.put("method", method);
        legacy.put("variant", "one2one");
        legacy.put("resultSetName", method + " CF-SF one2one");
        legacy.put("summary", response.get("summary"));
        legacy.put("metricsSummary", response.get("metricsSummary"));
        legacy.put("metrics", response.get("metrics"));
        legacy.put("charts", response.get("charts"));
        legacy.put("rows", rows);
        return List.of(legacy);
    }

    private String firstWarningMessage(Map<String, Object> response)
    {
        for (Map<String, Object> warning : mapList(response.get("warnings")))
        {
            String message = asString(warning.get("message"));
            if (StringUtils.isNotBlank(message))
            {
                return message;
            }
        }
        return null;
    }

    private String noticeMessage(String message)
    {
        if (StringUtils.isBlank(message) || !message.startsWith("NOTICE:"))
        {
            return null;
        }
        return message.substring("NOTICE:".length());
    }

    private int countResultSets(Long recordId)
    {
        if (recordId == null)
        {
            return 0;
        }
        MatchResultSet query = new MatchResultSet();
        query.setRecordId(recordId);
        return matchResultSetMapper.selectMatchResultSetList(query).size();
    }

    private void updateRunProgress(Long recordId, Long taskId, Long progress, String stage, String errorMessage)
    {
        MatchTaskRecord record = new MatchTaskRecord();
        record.setRecordId(recordId);
        record.setExecutionStatus("running");
        record.setCurrentStage(stage);
        record.setProgress(progress);
        record.setErrorMessage(errorMessage);
        matchTaskRecordMapper.updateMatchTaskRecord(record);

        MatchTask taskUpdate = new MatchTask();
        taskUpdate.setTaskId(taskId);
        taskUpdate.setLastRecordStatus("running");
        taskUpdate.setLastRecordStage(stage);
        taskUpdate.setUpdateTime(DateUtils.getNowDate());
        matchTaskMapper.updateMatchTask(taskUpdate);
    }

    private void markRecordFailed(Long recordId, Long taskId, String message)
    {
        MatchTaskRecord record = new MatchTaskRecord();
        record.setRecordId(recordId);
        record.setExecutionStatus("failed");
        record.setCurrentStage("算法运行失败");
        record.setProgress(100L);
        record.setErrorMessage(defaultString(message, "算法运行失败"));
        record.setFinishedAt(DateUtils.getNowDate());
        matchTaskRecordMapper.updateMatchTaskRecord(record);

        if (taskId != null)
        {
            MatchTask taskUpdate = new MatchTask();
            taskUpdate.setTaskId(taskId);
            taskUpdate.setLastRecordStatus("failed");
            taskUpdate.setLastRecordStage("算法运行失败");
            taskUpdate.setUpdateTime(DateUtils.getNowDate());
            matchTaskMapper.updateMatchTask(taskUpdate);
        }
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
        String provider = StringUtils.trimToNull(version.getLlmProvider());
        String model = StringUtils.trimToNull(version.getLlmModel());
        boolean hasPageLlmChoice = provider != null || model != null;
        if (StringUtils.isBlank(provider) || StringUtils.isBlank(model)
                || "none".equalsIgnoreCase(provider) || "none".equalsIgnoreCase(model))
        {
            if (!hasPageLlmChoice && version.getLlmConfigId() != null)
            {
                return version.getLlmConfigId();
            }
            return null;
        }

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

    private String resolveLlmModel(MatchTaskVersion version)
    {
        decorateVersion(version, version.getVersionId());
        String provider = version.getLlmProvider();
        String model = version.getLlmModel();
        if (StringUtils.isBlank(provider) || StringUtils.isBlank(model)
                || "none".equalsIgnoreCase(provider) || "none".equalsIgnoreCase(model))
        {
            return null;
        }
        if (model.contains("/"))
        {
            return model;
        }
        return provider + "/" + model;
    }

    private String buildAlgorithmParamsJson(MatchTaskVersion version)
    {
        return "{\"methods\":[\"Magneto\",\"MagnetoBoost\",\"MagnetoGPT\"],"
            + "\"variants\":[\"all\",\"one2one\"],"
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

    private long asLong(Object value, long defaultValue)
    {
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        if (value != null)
        {
            try
            {
                return Long.parseLong(String.valueOf(value));
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
        return total.divide(BigDecimal.valueOf(rows.size()), 6, RoundingMode.HALF_UP);
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
