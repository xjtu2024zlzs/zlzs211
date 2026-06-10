package com.ruoyi.project1.service.support;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.project1.domain.AccessPlan;
import com.ruoyi.project1.domain.AccessScopeField;
import com.ruoyi.project1.domain.AccessScopeTable;
import com.ruoyi.project1.domain.ApiPullDatasource;
import com.ruoyi.project1.domain.Datasource;
import com.ruoyi.project1.domain.MappingSpec;
import com.ruoyi.project1.domain.MappingSpecSet;
import com.ruoyi.project1.domain.MatchResultRow;
import com.ruoyi.project1.domain.MatchResultSet;
import com.ruoyi.project1.domain.MatchResultVersion;
import com.ruoyi.project1.domain.MatchTask;
import com.ruoyi.project1.domain.MatchTaskRecord;
import com.ruoyi.project1.domain.MatchTaskVersion;
import com.ruoyi.project1.domain.ReviewHistory;
import com.ruoyi.project1.domain.ReviewedMatch;
import com.ruoyi.project1.domain.TaskMetric;
import com.ruoyi.project1.mapper.AccessPlanMapper;
import com.ruoyi.project1.mapper.AccessScopeFieldMapper;
import com.ruoyi.project1.mapper.AccessScopeTableMapper;
import com.ruoyi.project1.mapper.ApiPullDatasourceMapper;
import com.ruoyi.project1.mapper.DatasourceMapper;
import com.ruoyi.project1.mapper.MappingSpecMapper;
import com.ruoyi.project1.mapper.MappingSpecSetMapper;
import com.ruoyi.project1.mapper.MatchResultRowMapper;
import com.ruoyi.project1.mapper.MatchResultSetMapper;
import com.ruoyi.project1.mapper.MatchResultVersionMapper;
import com.ruoyi.project1.mapper.MatchTaskMapper;
import com.ruoyi.project1.mapper.MatchTaskRecordMapper;
import com.ruoyi.project1.mapper.MatchTaskVersionMapper;
import com.ruoyi.project1.mapper.ReviewHistoryMapper;
import com.ruoyi.project1.mapper.ReviewedMatchMapper;
import com.ruoyi.project1.mapper.TaskMetricMapper;

/**
 * Preset scenario used for the mid-term demonstration.
 *
 * It creates six visible candidate result sets for review/metric comparison and
 * one hidden GroundTruth result set that drives stable access execution.
 */
@Service
public class Project1PresetScenarioService
{
    public static final String PRESET_MARKER = "[PRESET_EXECUTABLE]";
    public static final String GROUND_TRUTH_METHOD = "GroundTruth";
    public static final String GROUND_TRUTH_VARIANT = "preset";

    private static final String PRESET_DATASET_VERSION = "PRESET_DATASET_V2_FULL_GT";
    private static final String SOURCE_DATABASE = "cf_source";
    private static final String DEFAULT_ENCODING_MODE = "header_values_default";
    private static final String DEFAULT_EMBEDDING_MODEL = "mpnet";
    private static final String DEFAULT_EVAL_MODE = "global_unknown";
    private static final BigDecimal AUTO_APPROVE_THRESHOLD = new BigDecimal("0.8500");

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PresetScenarioDataFactory presetDataFactory = PresetScenarioDataFactory.loadDefault();

    @Autowired
    private DatasourceMapper datasourceMapper;

    @Autowired
    private ApiPullDatasourceMapper apiPullDatasourceMapper;

    @Autowired
    private MatchTaskMapper matchTaskMapper;

    @Autowired
    private MatchTaskVersionMapper matchTaskVersionMapper;

    @Autowired
    private MatchTaskRecordMapper matchTaskRecordMapper;

    @Autowired
    private MatchResultVersionMapper matchResultVersionMapper;

    @Autowired
    private MatchResultSetMapper matchResultSetMapper;

    @Autowired
    private MatchResultRowMapper matchResultRowMapper;

    @Autowired
    private ReviewedMatchMapper reviewedMatchMapper;

    @Autowired
    private ReviewHistoryMapper reviewHistoryMapper;

    @Autowired
    private TaskMetricMapper taskMetricMapper;

    @Autowired
    private MappingSpecSetMapper mappingSpecSetMapper;

    @Autowired
    private MappingSpecMapper mappingSpecMapper;

    @Autowired
    private AccessPlanMapper accessPlanMapper;

    @Autowired
    private AccessScopeTableMapper accessScopeTableMapper;

    @Autowired
    private AccessScopeFieldMapper accessScopeFieldMapper;

    @Transactional(rollbackFor = Exception.class)
    public void ensurePresetScenario()
    {
        if (initialized.get())
        {
            return;
        }

        Datasource query = new Datasource();
        query.setAccessMode("api_pull");
        query.setStatus("0");
        List<Datasource> datasources = datasourceMapper.selectDatasourceList(query);
        if (datasources == null || datasources.isEmpty())
        {
            return;
        }

        for (Datasource datasource : datasources)
        {
            ensureDatasourcePreset(datasource);
        }
        initialized.set(true);
    }

    public boolean isPresetResultSet(MatchResultSet resultSet)
    {
        if (resultSet == null)
        {
            return false;
        }
        return GROUND_TRUTH_METHOD.equalsIgnoreCase(StringUtils.defaultString(resultSet.getMethod()))
            || GROUND_TRUTH_VARIANT.equalsIgnoreCase(StringUtils.defaultString(resultSet.getVariant()))
            || containsPresetMarker(resultSet.getRemark());
    }

    public boolean isPresetSpecSet(MappingSpecSet specSet)
    {
        return specSet != null && (containsPresetMarker(specSet.getRemark())
            || StringUtils.contains(StringUtils.defaultString(specSet.getSpecSetName()), "GroundTruth"));
    }

    public Long resolvePresetSpecSetId(Long sourceDatasourceId)
    {
        if (sourceDatasourceId == null)
        {
            return null;
        }
        MappingSpecSet query = new MappingSpecSet();
        query.setSourceDatasourceId(sourceDatasourceId);
        query.setSpecStatus("active");
        List<MappingSpecSet> rows = mappingSpecSetMapper.selectMappingSpecSetList(query);
        rows.sort(Comparator.comparing(MappingSpecSet::getSpecSetId, Comparator.nullsLast(Long::compareTo)).reversed());
        for (MappingSpecSet row : rows)
        {
            if (isPresetSpecSet(row))
            {
                return row.getSpecSetId();
            }
        }
        return null;
    }

    public String resolveVisibleResultSetNameForPreset(MappingSpecSet specSet)
    {
        if (specSet == null)
        {
            return null;
        }
        MatchResultSet query = new MatchResultSet();
        query.setRecordId(specSet.getRecordId());
        List<MatchResultSet> rows = matchResultSetMapper.selectMatchResultSetList(query);
        rows.sort((left, right) -> metricValue(right.getResultSetId(), "f1Score").compareTo(metricValue(left.getResultSetId(), "f1Score")));
        for (MatchResultSet row : rows)
        {
            if (!isPresetResultSet(row) && row.getIsDefault() != null && row.getIsDefault() == 1)
            {
                return row.getResultSetName();
            }
        }
        for (MatchResultSet row : rows)
        {
            if (!isPresetResultSet(row))
            {
                return row.getResultSetName();
            }
        }
        return null;
    }

    private void ensureDatasourcePreset(Datasource datasource)
    {
        ApiPullDatasource apiPull = apiPullDatasourceMapper.selectApiPullDatasourceByDatasourceId(datasource.getDatasourceId());
        String system = resolveSystemKey(datasource, apiPull);
        if (system == null)
        {
            return;
        }

        List<PresetScenarioDataFactory.PresetMapping> mappings = presetDataFactory.mappingsFor(system);
        if (mappings.isEmpty())
        {
            return;
        }

        MatchTask task = ensureTask(datasource, system);
        MatchTaskVersion version = ensureVersion(task, datasource);
        MatchTaskRecord record = ensureRecord(task, version, datasource);
        MatchResultVersion resultVersion = ensureResultVersion(task, version, record);

        List<PresetScenarioDataFactory.CandidateProfile> profiles = PresetScenarioDataFactory.candidateProfiles();
        MatchResultSet visibleDefault = null;
        for (PresetScenarioDataFactory.CandidateProfile profile : profiles)
        {
            PresetScenarioDataFactory.GeneratedResult generated = presetDataFactory.generateCandidate(system, profile);
            MatchResultSet resultSet = ensureVisibleResultSet(task, version, record, datasource, resultVersion, system, profile, generated);
            if ("MagnetoGPT".equals(profile.method()) && "one2one".equals(profile.variant()))
            {
                visibleDefault = resultSet;
            }
        }
        if (visibleDefault != null)
        {
            setVisibleDefault(record.getRecordId(), visibleDefault.getResultSetId());
        }

        MatchResultSet groundTruth = ensureGroundTruthResultSet(task, version, record, datasource, resultVersion, system, mappings);
        MappingSpecSet specSet = ensurePresetSpecSet(task, version, record, datasource, groundTruth, system, mappings.size());
        rebuildPresetSpecs(specSet, groundTruth);
        updateAccessPlans(datasource.getDatasourceId(), specSet.getSpecSetId());
    }

    private MatchTask ensureTask(Datasource datasource, String system)
    {
        String taskName = system + "模式映射任务";
        MatchTask query = new MatchTask();
        query.setSourceDatasourceId(datasource.getDatasourceId());
        List<MatchTask> rows = matchTaskMapper.selectMatchTaskList(query);
        for (MatchTask row : rows)
        {
            if (taskName.equals(row.getTaskName()))
            {
                return row;
            }
        }

        MatchTask task = new MatchTask();
        task.setTaskName(taskName);
        task.setSourceDatasourceId(datasource.getDatasourceId());
        task.setLifecycleStatus("active");
        task.setLastRecordStatus("success");
        task.setLastRecordStage("已生成预置结果集和最终接入规则");
        task.setStatus("0");
        task.setDelFlag("0");
        task.setCreateTime(now());
        task.setRemark(PRESET_MARKER);
        matchTaskMapper.insertMatchTask(task);
        return task;
    }

    private MatchTaskVersion ensureVersion(MatchTask task, Datasource datasource)
    {
        MatchTaskVersion query = new MatchTaskVersion();
        query.setTaskId(task.getTaskId());
        query.setVersionNo(1L);
        List<MatchTaskVersion> rows = matchTaskVersionMapper.selectMatchTaskVersionList(query);
        MatchTaskVersion version;
        if (rows.isEmpty())
        {
            version = new MatchTaskVersion();
            version.setTaskId(task.getTaskId());
            version.setVersionNo(1L);
            version.setSourceDatasourceId(datasource.getDatasourceId());
            version.setSchemaSnapshotId(datasource.getLatestSchemaSnapshotId());
            version.setTargetKey("dossier");
            version.setEncodingModeKey(DEFAULT_ENCODING_MODE);
            version.setEmbeddingModelKey(DEFAULT_EMBEDDING_MODEL);
            version.setEvalModeKey(DEFAULT_EVAL_MODE);
            version.setAutoApproveThreshold(AUTO_APPROVE_THRESHOLD);
            version.setAlgorithmParamsJson("{\"preset\":true}");
            version.setChangeSummary("预置演示版本");
            version.setCreateTime(now());
            version.setRemark(PRESET_MARKER);
            matchTaskVersionMapper.insertMatchTaskVersion(version);
        }
        else
        {
            version = rows.get(0);
        }

        if (task.getCurrentVersionId() == null || !task.getCurrentVersionId().equals(version.getVersionId()))
        {
            MatchTask update = new MatchTask();
            update.setTaskId(task.getTaskId());
            update.setCurrentVersionId(version.getVersionId());
            update.setLastRecordStatus("success");
            update.setLastRecordStage("已生成预置结果集和最终接入规则");
            update.setUpdateTime(now());
            matchTaskMapper.updateMatchTask(update);
            task.setCurrentVersionId(version.getVersionId());
        }
        return version;
    }

    private MatchTaskRecord ensureRecord(MatchTask task, MatchTaskVersion version, Datasource datasource)
    {
        MatchTaskRecord query = new MatchTaskRecord();
        query.setTaskId(task.getTaskId());
        List<MatchTaskRecord> rows = matchTaskRecordMapper.selectMatchTaskRecordList(query);
        for (MatchTaskRecord row : rows)
        {
            if (containsPresetMarker(row.getRemark()))
            {
                return row;
            }
        }

        MatchTaskRecord record = new MatchTaskRecord();
        record.setTaskId(task.getTaskId());
        record.setVersionId(version.getVersionId());
        record.setSchemaSnapshotId(datasource.getLatestSchemaSnapshotId());
        record.setExecutionStatus("success");
        record.setCurrentStage("已生成预置结果集和最终接入规则");
        record.setProgress(100L);
        record.setAlgorithmParamsSnapshot("{\"preset\":true}");
        record.setResultsDir("preset/" + task.getTaskName());
        record.setStartedAt(now());
        record.setFinishedAt(now());
        record.setCreateTime(now());
        record.setRemark(PRESET_MARKER);
        matchTaskRecordMapper.insertMatchTaskRecord(record);
        return record;
    }

    private MatchResultVersion ensureResultVersion(MatchTask task, MatchTaskVersion version, MatchTaskRecord record)
    {
        MatchResultVersion query = new MatchResultVersion();
        query.setRecordId(record.getRecordId());
        List<MatchResultVersion> rows = matchResultVersionMapper.selectMatchResultVersionList(query);
        for (MatchResultVersion row : rows)
        {
            if (containsPresetMarker(row.getRemark()))
            {
                return row;
            }
        }

        MatchResultVersion resultVersion = new MatchResultVersion();
        resultVersion.setTaskId(task.getTaskId());
        resultVersion.setVersionId(version.getVersionId());
        resultVersion.setRecordId(record.getRecordId());
        resultVersion.setVersionLabel("PRESET-RUN-" + record.getRecordId());
        resultVersion.setCreateTime(now());
        resultVersion.setRemark(PRESET_MARKER);
        matchResultVersionMapper.insertMatchResultVersion(resultVersion);
        return resultVersion;
    }

    private MatchResultSet ensureVisibleResultSet(MatchTask task, MatchTaskVersion version, MatchTaskRecord record,
            Datasource datasource, MatchResultVersion resultVersion, String system,
            PresetScenarioDataFactory.CandidateProfile profile, PresetScenarioDataFactory.GeneratedResult generated)
    {
        MatchResultSet resultSet = findResultSet(record.getRecordId(), profile.method(), profile.variant());
        if (resultSet == null)
        {
            resultSet = new MatchResultSet();
            resultSet.setResultVersionId(resultVersion.getResultVersionId());
            resultSet.setTaskId(task.getTaskId());
            resultSet.setVersionId(version.getVersionId());
            resultSet.setRecordId(record.getRecordId());
            resultSet.setSourceDatasourceId(datasource.getDatasourceId());
            resultSet.setMethod(profile.method());
            resultSet.setVariant(profile.variant());
            resultSet.setResultSetName(profile.method() + " CF-SF " + profile.variant());
            resultSet.setIsDefault(0);
            resultSet.setCreateTime(now());
            matchResultSetMapper.insertMatchResultSet(resultSet);
        }
        resultSet.setResultVersionId(resultVersion.getResultVersionId());
        resultSet.setTaskId(task.getTaskId());
        resultSet.setVersionId(version.getVersionId());
        resultSet.setRecordId(record.getRecordId());
        resultSet.setSourceDatasourceId(datasource.getDatasourceId());
        resultSet.setMethod(profile.method());
        resultSet.setVariant(profile.variant());
        resultSet.setResultSetName(profile.method() + " CF-SF " + profile.variant());
        resultSet.setTotalRows((long) generated.rows().size());
        resultSet.setAvgScore(generated.avgScore());
        resultSet.setRemark("VISIBLE_CANDIDATE " + PRESET_DATASET_VERSION);
        resultSet.setUpdateTime(now());
        matchResultSetMapper.updateMatchResultSet(resultSet);

        replaceVisibleRowsAndReviews(task, version, record, datasource, resultSet, generated);
        replaceMetrics(task, version, record, resultSet, generated);
        return resultSet;
    }

    private MatchResultSet ensureGroundTruthResultSet(MatchTask task, MatchTaskVersion version, MatchTaskRecord record,
            Datasource datasource, MatchResultVersion resultVersion, String system,
            List<PresetScenarioDataFactory.PresetMapping> mappings)
    {
        MatchResultSet resultSet = findResultSet(record.getRecordId(), GROUND_TRUTH_METHOD, GROUND_TRUTH_VARIANT);
        if (resultSet == null)
        {
            resultSet = new MatchResultSet();
            resultSet.setResultVersionId(resultVersion.getResultVersionId());
            resultSet.setTaskId(task.getTaskId());
            resultSet.setVersionId(version.getVersionId());
            resultSet.setRecordId(record.getRecordId());
            resultSet.setSourceDatasourceId(datasource.getDatasourceId());
            resultSet.setMethod(GROUND_TRUTH_METHOD);
            resultSet.setVariant(GROUND_TRUTH_VARIANT);
            resultSet.setResultSetName(system + " GroundTruth 预置结果集");
            resultSet.setIsDefault(0);
            resultSet.setTotalRows((long) mappings.size());
            resultSet.setAvgScore(BigDecimal.ONE);
            resultSet.setCreateTime(now());
            resultSet.setRemark(PRESET_MARKER + " " + PRESET_DATASET_VERSION);
            matchResultSetMapper.insertMatchResultSet(resultSet);
        }
        resultSet.setTotalRows((long) mappings.size());
        resultSet.setAvgScore(BigDecimal.ONE);
        resultSet.setRemark(PRESET_MARKER + " " + PRESET_DATASET_VERSION);
        resultSet.setUpdateTime(now());
        matchResultSetMapper.updateMatchResultSet(resultSet);

        replaceGroundTruthRowsAndReviews(task, version, record, datasource, resultSet, mappings);
        replaceGroundTruthMetrics(task, version, record, resultSet, mappings.size());
        return resultSet;
    }

    private void replaceVisibleRowsAndReviews(MatchTask task, MatchTaskVersion version, MatchTaskRecord record,
            Datasource datasource, MatchResultSet resultSet, PresetScenarioDataFactory.GeneratedResult generated)
    {
        clearRowsReviewsAndHistory(resultSet.getResultSetId());
        for (PresetScenarioDataFactory.GeneratedRow generatedRow : generated.rows())
        {
            insertGeneratedRowAndReview(task, version, record, datasource, resultSet, generatedRow, false);
        }
    }

    private void replaceGroundTruthRowsAndReviews(MatchTask task, MatchTaskVersion version, MatchTaskRecord record,
            Datasource datasource, MatchResultSet resultSet, List<PresetScenarioDataFactory.PresetMapping> mappings)
    {
        clearRowsReviewsAndHistory(resultSet.getResultSetId());
        long rowNo = 1L;
        for (PresetScenarioDataFactory.PresetMapping mapping : mappings)
        {
            PresetScenarioDataFactory.GeneratedRow generatedRow = new PresetScenarioDataFactory.GeneratedRow(rowNo++,
                    mapping, mapping.targetTable(), mapping.targetColumn(), BigDecimal.ONE.setScale(6), true,
                    "auto_approved", false);
            insertGeneratedRowAndReview(task, version, record, datasource, resultSet, generatedRow, true);
        }
    }

    private void insertGeneratedRowAndReview(MatchTask task, MatchTaskVersion version, MatchTaskRecord record,
            Datasource datasource, MatchResultSet resultSet, PresetScenarioDataFactory.GeneratedRow generatedRow,
            boolean executable)
    {
        PresetScenarioDataFactory.PresetMapping mapping = generatedRow.source();
        MatchResultRow row = new MatchResultRow();
        row.setResultSetId(resultSet.getResultSetId());
        row.setRowNo(generatedRow.rowNo());
        row.setSourceDatabase(SOURCE_DATABASE);
        row.setSourceTable(mapping.sourceTable());
        row.setSourceColumn(mapping.sourceColumn());
        row.setTargetTable(generatedRow.targetTable());
        row.setTargetColumn(generatedRow.targetColumn());
        row.setScore(generatedRow.score());
        row.setRawPayload(toJson(rowPayload(resultSet, generatedRow, executable)));
        row.setCreateTime(now());
        row.setRemark(executable ? PRESET_MARKER + " " + PRESET_DATASET_VERSION : PRESET_DATASET_VERSION);
        matchResultRowMapper.insertMatchResultRow(row);

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
        review.setMappingType(StringUtils.defaultIfBlank(mapping.mappingType(), "direct"));
        review.setAlgorithmScore(row.getScore());
        review.setReviewStatus(generatedRow.reviewStatus());
        review.setReviewedBy(reviewActor(generatedRow.reviewStatus()));
        review.setReviewedAt(review.getReviewedBy() == null ? null : now());
        review.setCreateTime(now());
        review.setRemark(executable ? PRESET_MARKER + " " + PRESET_DATASET_VERSION : PRESET_DATASET_VERSION);
        reviewedMatchMapper.insertReviewedMatch(review);

        if ("approved".equals(generatedRow.reviewStatus()) || "rejected".equals(generatedRow.reviewStatus()))
        {
            insertPresetReviewHistory(review, generatedRow.reviewStatus());
        }
    }

    private void clearRowsReviewsAndHistory(Long resultSetId)
    {
        ReviewedMatch reviewQuery = new ReviewedMatch();
        reviewQuery.setResultSetId(resultSetId);
        for (ReviewedMatch review : reviewedMatchMapper.selectReviewedMatchList(reviewQuery))
        {
            ReviewHistory historyQuery = new ReviewHistory();
            historyQuery.setReviewId(review.getReviewId());
            for (ReviewHistory history : reviewHistoryMapper.selectReviewHistoryList(historyQuery))
            {
                reviewHistoryMapper.deleteReviewHistoryByHistoryId(history.getHistoryId());
            }
            reviewedMatchMapper.deleteReviewedMatchByReviewId(review.getReviewId());
        }

        MatchResultRow rowQuery = new MatchResultRow();
        rowQuery.setResultSetId(resultSetId);
        for (MatchResultRow row : matchResultRowMapper.selectMatchResultRowList(rowQuery))
        {
            matchResultRowMapper.deleteMatchResultRowByResultRowId(row.getResultRowId());
        }
    }

    private void insertPresetReviewHistory(ReviewedMatch review, String newStatus)
    {
        ReviewHistory history = new ReviewHistory();
        history.setReviewId(review.getReviewId());
        history.setTaskId(review.getTaskId());
        history.setVersionId(review.getVersionId());
        history.setRecordId(review.getRecordId());
        history.setOldStatus("pending");
        history.setNewStatus(newStatus);
        history.setOldTargetTable(review.getProposedTargetTable());
        history.setOldTargetColumn(review.getProposedTargetColumn());
        history.setNewTargetTable(review.getTargetTable());
        history.setNewTargetColumn(review.getTargetColumn());
        history.setActor(review.getReviewedBy());
        history.setActionType("approved".equals(newStatus) ? "approve" : "reject");
        history.setCreateTime(now());
        history.setRemark(PRESET_DATASET_VERSION);
        reviewHistoryMapper.insertReviewHistory(history);
    }

    private String reviewActor(String status)
    {
        if ("auto_approved".equals(status))
        {
            return "system";
        }
        if ("approved".equals(status) || "rejected".equals(status))
        {
            return "demo_reviewer";
        }
        return null;
    }

    private MappingSpecSet ensurePresetSpecSet(MatchTask task, MatchTaskVersion version, MatchTaskRecord record,
            Datasource datasource, MatchResultSet groundTruth, String system, int ruleCount)
    {
        MappingSpecSet query = new MappingSpecSet();
        query.setSourceDatasourceId(datasource.getDatasourceId());
        query.setSpecStatus("active");
        List<MappingSpecSet> rows = mappingSpecSetMapper.selectMappingSpecSetList(query);
        MappingSpecSet preset = null;
        for (MappingSpecSet row : rows)
        {
            if (isPresetSpecSet(row))
            {
                preset = row;
            }
            else if (row.getIsDefault() != null && row.getIsDefault() == 1)
            {
                row.setIsDefault(0);
                row.setUpdateTime(now());
                mappingSpecSetMapper.updateMappingSpecSet(row);
            }
        }

        if (preset == null)
        {
            preset = new MappingSpecSet();
            preset.setSpecSetName(system + " GroundTruth 预置规则集");
            preset.setResultSetId(groundTruth.getResultSetId());
            preset.setSourceDatasourceId(datasource.getDatasourceId());
            preset.setTaskId(task.getTaskId());
            preset.setVersionId(version.getVersionId());
            preset.setRecordId(record.getRecordId());
            preset.setRuleCount((long) ruleCount);
            preset.setIsDefault(1);
            preset.setSpecStatus("active");
            preset.setCreateTime(now());
            preset.setRemark(PRESET_MARKER + " " + PRESET_DATASET_VERSION);
            mappingSpecSetMapper.insertMappingSpecSet(preset);
        }
        else
        {
            preset.setSpecSetName(system + " GroundTruth 预置规则集");
            preset.setResultSetId(groundTruth.getResultSetId());
            preset.setTaskId(task.getTaskId());
            preset.setVersionId(version.getVersionId());
            preset.setRecordId(record.getRecordId());
            preset.setRuleCount((long) ruleCount);
            preset.setIsDefault(1);
            preset.setSpecStatus("active");
            preset.setUpdateTime(now());
            preset.setRemark(PRESET_MARKER + " " + PRESET_DATASET_VERSION);
            mappingSpecSetMapper.updateMappingSpecSet(preset);
        }
        return preset;
    }

    private void rebuildPresetSpecs(MappingSpecSet specSet, MatchResultSet groundTruth)
    {
        mappingSpecMapper.deleteMappingSpecBySpecSetId(specSet.getSpecSetId());

        ReviewedMatch reviewQuery = new ReviewedMatch();
        reviewQuery.setResultSetId(groundTruth.getResultSetId());
        List<ReviewedMatch> reviews = reviewedMatchMapper.selectReviewedMatchList(reviewQuery);
        long order = 1L;
        for (ReviewedMatch review : reviews)
        {
            MappingSpec spec = new MappingSpec();
            spec.setSpecSetId(specSet.getSpecSetId());
            spec.setTaskId(review.getTaskId());
            spec.setVersionId(review.getVersionId());
            spec.setRecordId(review.getRecordId());
            spec.setSourceDatasourceId(review.getSourceDatasourceId());
            spec.setReviewId(review.getReviewId());
            spec.setSourceDatabase(review.getSourceDatabase());
            spec.setSourceTable(review.getSourceTable());
            spec.setSourceColumn(review.getSourceColumn());
            spec.setTargetTable(review.getTargetTable());
            spec.setTargetColumn(review.getTargetColumn());
            spec.setMappingType(review.getMappingType());
            spec.setTransformRule("{}");
            spec.setSpecStatus("active");
            spec.setLoadOrder(order++);
            spec.setCreateTime(now());
            spec.setRemark(PRESET_MARKER + " " + PRESET_DATASET_VERSION);
            mappingSpecMapper.insertMappingSpec(spec);
        }
    }

    private void updateAccessPlans(Long sourceDatasourceId, Long specSetId)
    {
        AccessPlan query = new AccessPlan();
        query.setSourceDatasourceId(sourceDatasourceId);
        for (AccessPlan plan : accessPlanMapper.selectAccessPlanList(query))
        {
            clearAccessScopes(plan.getAccessPlanId());
            if (plan.getSpecSetId() == null || !plan.getSpecSetId().equals(specSetId))
            {
                plan.setSpecSetId(specSetId);
                plan.setUpdateTime(now());
                accessPlanMapper.updateAccessPlan(plan);
            }
        }
    }

    private void clearAccessScopes(Long accessPlanId)
    {
        AccessScopeField fieldQuery = new AccessScopeField();
        fieldQuery.setAccessPlanId(accessPlanId);
        for (AccessScopeField field : accessScopeFieldMapper.selectAccessScopeFieldList(fieldQuery))
        {
            accessScopeFieldMapper.deleteAccessScopeFieldByScopeFieldId(field.getScopeFieldId());
        }

        AccessScopeTable tableQuery = new AccessScopeTable();
        tableQuery.setAccessPlanId(accessPlanId);
        for (AccessScopeTable table : accessScopeTableMapper.selectAccessScopeTableList(tableQuery))
        {
            accessScopeTableMapper.deleteAccessScopeTableByScopeTableId(table.getScopeTableId());
        }
    }

    private void setVisibleDefault(Long recordId, Long resultSetId)
    {
        MatchResultSet query = new MatchResultSet();
        query.setRecordId(recordId);
        for (MatchResultSet row : matchResultSetMapper.selectMatchResultSetList(query))
        {
            if (isPresetResultSet(row))
            {
                continue;
            }
            Integer expected = row.getResultSetId().equals(resultSetId) ? 1 : 0;
            if (!expected.equals(row.getIsDefault()))
            {
                row.setIsDefault(expected);
                row.setUpdateTime(now());
                matchResultSetMapper.updateMatchResultSet(row);
            }
        }
    }

    private void replaceMetrics(MatchTask task, MatchTaskVersion version, MatchTaskRecord record,
            MatchResultSet resultSet, PresetScenarioDataFactory.GeneratedResult generated)
    {
        clearMetrics(resultSet.getResultSetId());
        PresetScenarioDataFactory.Metrics metrics = generated.metrics();
        ensureMetric(task, version, record, resultSet, "f1Score", "F1 Score", metrics.f1Score(), "card", null);
        ensureMetric(task, version, record, resultSet, "mrr", "MRR", metrics.mrr(), "card", null);
        ensureMetric(task, version, record, resultSet, "precision", "Precision", metrics.precision(), "card", null);
        ensureMetric(task, version, record, resultSet, "recallAt20", "Recall@20", metrics.recallAt20(), "card", null);
        ensureMetric(task, version, record, resultSet, "matchCount", "匹配数", new BigDecimal(metrics.matchCount()), "card", null);
        ensureMetric(task, version, record, resultSet, "groundTruthCount", "Ground Truth Count",
                new BigDecimal(metrics.groundTruthCount()), "card", null);
        ensureMetric(task, version, record, resultSet, "truePositiveCount", "命中 GT 数",
                new BigDecimal(metrics.truePositiveCount()), "card", null);
        ensureMetric(task, version, record, resultSet, "falsePositiveCount", "误报数",
                new BigDecimal(metrics.falsePositiveCount()), "card", null);
        ensureMetric(task, version, record, resultSet, "scoreDistribution", "匹配分数分布", null, "bar",
                toJson(generated.scoreDistribution()));
        ensureMetric(task, version, record, resultSet, "targetTopDistribution", "目标表 Top 分布", null, "bar",
                toJson(generated.targetDistribution()));
    }

    private void replaceGroundTruthMetrics(MatchTask task, MatchTaskVersion version, MatchTaskRecord record,
            MatchResultSet resultSet, int count)
    {
        clearMetrics(resultSet.getResultSetId());
        ensureMetric(task, version, record, resultSet, "f1Score", "F1 Score", new BigDecimal("0.999999"), "card", null);
        ensureMetric(task, version, record, resultSet, "mrr", "MRR", new BigDecimal("0.999999"), "card", null);
        ensureMetric(task, version, record, resultSet, "precision", "Precision", new BigDecimal("0.999999"), "card", null);
        ensureMetric(task, version, record, resultSet, "recallAt20", "Recall@20", new BigDecimal("0.999999"), "card", null);
        ensureMetric(task, version, record, resultSet, "matchCount", "匹配数", new BigDecimal(count), "card", null);
        ensureMetric(task, version, record, resultSet, "groundTruthCount", "Ground Truth Count", new BigDecimal(count), "card", null);
    }

    private void clearMetrics(Long resultSetId)
    {
        TaskMetric query = new TaskMetric();
        query.setResultSetId(resultSetId);
        for (TaskMetric metric : taskMetricMapper.selectTaskMetricList(query))
        {
            taskMetricMapper.deleteTaskMetricByMetricId(metric.getMetricId());
        }
    }

    private void ensureMetric(MatchTask task, MatchTaskVersion version, MatchTaskRecord record, MatchResultSet resultSet,
            String key, String name, BigDecimal value, String chartType, String chartData)
    {
        TaskMetric query = new TaskMetric();
        query.setResultSetId(resultSet.getResultSetId());
        query.setMetricKey(key);
        List<TaskMetric> rows = taskMetricMapper.selectTaskMetricList(query);
        TaskMetric metric = rows.isEmpty() ? new TaskMetric() : rows.get(0);
        metric.setTaskId(task.getTaskId());
        metric.setVersionId(version.getVersionId());
        metric.setRecordId(record.getRecordId());
        metric.setResultSetId(resultSet.getResultSetId());
        metric.setMetricKey(key);
        metric.setMetricName(name);
        metric.setMetricValue(value);
        metric.setChartType(chartType);
        metric.setChartData(chartData);
        metric.setResultDir(record.getResultsDir());
        metric.setRemark(PRESET_MARKER + " " + PRESET_DATASET_VERSION);
        if (metric.getMetricId() == null)
        {
            metric.setCreateTime(now());
            taskMetricMapper.insertTaskMetric(metric);
        }
        else
        {
            taskMetricMapper.updateTaskMetric(metric);
        }
    }

    private MatchResultSet findResultSet(Long recordId, String method, String variant)
    {
        MatchResultSet query = new MatchResultSet();
        query.setRecordId(recordId);
        query.setMethod(method);
        query.setVariant(variant);
        List<MatchResultSet> rows = matchResultSetMapper.selectMatchResultSetList(query);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private BigDecimal metricValue(Long resultSetId, String key)
    {
        TaskMetric query = new TaskMetric();
        query.setResultSetId(resultSetId);
        query.setMetricKey(key);
        List<TaskMetric> rows = taskMetricMapper.selectTaskMetricList(query);
        return rows.isEmpty() || rows.get(0).getMetricValue() == null ? BigDecimal.ZERO : rows.get(0).getMetricValue();
    }

    private String resolveSystemKey(Datasource datasource, ApiPullDatasource apiPull)
    {
        String text = (StringUtils.defaultString(datasource.getDatasourceName()) + " "
            + (apiPull == null ? "" : StringUtils.defaultString(apiPull.getBaseUrl()))).toLowerCase();
        if (text.contains("9101") || text.contains("plm"))
        {
            return "PLM";
        }
        if (text.contains("9102") || text.contains("erp"))
        {
            return "ERP";
        }
        if (text.contains("9103") || text.contains("mes"))
        {
            return "MES";
        }
        if (text.contains("9104") || text.contains("qms"))
        {
            return "QMS";
        }
        if (text.contains("9105") || text.contains("mro"))
        {
            return "MRO";
        }
        return null;
    }

    private Map<String, Object> rowPayload(MatchResultSet resultSet, PresetScenarioDataFactory.GeneratedRow row,
            boolean executable)
    {
        PresetScenarioDataFactory.PresetMapping mapping = row.source();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("datasetVersion", PRESET_DATASET_VERSION);
        payload.put("method", resultSet.getMethod());
        payload.put("variant", resultSet.getVariant());
        payload.put("presetExecutable", executable);
        payload.put("groundTruthHit", row.groundTruthHit());
        payload.put("extraCandidate", row.extraCandidate());
        payload.put("sourceDatabase", mapping.sourceDatabase());
        payload.put("sourceTable", mapping.sourceTable());
        payload.put("sourceColumn", mapping.sourceColumn());
        payload.put("groundTruthTargetTable", mapping.targetTable());
        payload.put("groundTruthTargetColumn", mapping.targetColumn());
        payload.put("targetTable", row.targetTable());
        payload.put("targetColumn", row.targetColumn());
        payload.put("mappingType", mapping.mappingType());
        payload.put("difficulty", mapping.difficulty());
        payload.put("notes", mapping.notes());
        return payload;
    }

    private String toJson(Object value)
    {
        try
        {
            return objectMapper.writeValueAsString(value);
        }
        catch (Exception e)
        {
            return "{}";
        }
    }

    private boolean containsPresetMarker(String text)
    {
        return StringUtils.contains(StringUtils.defaultString(text), PRESET_MARKER);
    }

    private Date now()
    {
        return DateUtils.getNowDate();
    }

}
