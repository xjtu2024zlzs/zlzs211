package com.ruoyi.project1.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.project1.domain.MatchResultRow;
import com.ruoyi.project1.domain.MatchResultSet;
import com.ruoyi.project1.domain.ReviewedMatch;
import com.ruoyi.project1.domain.TaskMetric;
import com.ruoyi.project1.mapper.MatchResultRowMapper;
import com.ruoyi.project1.mapper.MatchResultSetMapper;
import com.ruoyi.project1.mapper.ReviewedMatchMapper;
import com.ruoyi.project1.mapper.TaskMetricMapper;
import com.ruoyi.project1.service.IMatchResultSetService;
import com.ruoyi.project1.service.IReviewedMatchService;
import com.ruoyi.project1.service.support.Project1PresetScenarioService;

/**
 * Service implementation for mode matching result sets.
 */
@Service
public class MatchResultSetServiceImpl implements IMatchResultSetService
{
    @Autowired
    private MatchResultSetMapper matchResultSetMapper;

    @Autowired
    private MatchResultRowMapper matchResultRowMapper;

    @Autowired
    private ReviewedMatchMapper reviewedMatchMapper;

    @Autowired
    private TaskMetricMapper taskMetricMapper;

    @Autowired
    private IReviewedMatchService reviewedMatchService;

    @Autowired
    private Project1PresetScenarioService presetScenarioService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final TypeReference<List<Map<String, Object>>> CHART_ROWS_TYPE = new TypeReference<List<Map<String, Object>>>() {};

    @Override
    public MatchResultSet selectMatchResultSetByResultSetId(Long resultSetId)
    {
        return matchResultSetMapper.selectMatchResultSetByResultSetId(resultSetId);
    }

    @Override
    public List<MatchResultSet> selectMatchResultSetList(MatchResultSet matchResultSet)
    {
        presetScenarioService.ensurePresetScenario();
        List<MatchResultSet> sourceRows = matchResultSetMapper.selectMatchResultSetList(matchResultSet);
        List<MatchResultSet> rows = new ArrayList<>();
        for (MatchResultSet row : sourceRows)
        {
            if (presetScenarioService.isPresetResultSet(row))
            {
                continue;
            }
            row.setF1Score(findMetricValue(row.getResultSetId(), "f1Score"));
            rows.add(row);
        }
        return rows;
    }

    @Override
    public int insertMatchResultSet(MatchResultSet matchResultSet)
    {
        matchResultSet.setCreateTime(DateUtils.getNowDate());
        return matchResultSetMapper.insertMatchResultSet(matchResultSet);
    }

    @Override
    public int updateMatchResultSet(MatchResultSet matchResultSet)
    {
        matchResultSet.setUpdateTime(DateUtils.getNowDate());
        return matchResultSetMapper.updateMatchResultSet(matchResultSet);
    }

    @Override
    public int deleteMatchResultSetByResultSetIds(Long[] resultSetIds)
    {
        return matchResultSetMapper.deleteMatchResultSetByResultSetIds(resultSetIds);
    }

    @Override
    public int deleteMatchResultSetByResultSetId(Long resultSetId)
    {
        return matchResultSetMapper.deleteMatchResultSetByResultSetId(resultSetId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> setDefault(Long resultSetId)
    {
        presetScenarioService.ensurePresetScenario();
        MatchResultSet target = requireResultSet(resultSetId);
        if (presetScenarioService.isPresetResultSet(target))
        {
            throw new ServiceException("该结果集不支持手动设为展示默认结果集");
        }
        MatchResultSet query = new MatchResultSet();
        if (target.getRecordId() != null)
        {
            query.setRecordId(target.getRecordId());
        }
        else
        {
            query.setTaskId(target.getTaskId());
        }
        List<MatchResultSet> resultSets = matchResultSetMapper.selectMatchResultSetList(query);
        for (MatchResultSet item : resultSets)
        {
            if (presetScenarioService.isPresetResultSet(item))
            {
                continue;
            }
            item.setIsDefault(item.getResultSetId().equals(resultSetId) ? 1 : 0);
            item.setUpdateTime(DateUtils.getNowDate());
            matchResultSetMapper.updateMatchResultSet(item);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", target.getTaskId());
        payload.put("sourceDatasourceId", target.getSourceDatasourceId());
        payload.put("resultSetId", target.getResultSetId());
        payload.put("message", "已设为展示默认结果集；接入执行继续使用当前最终接入规则");
        return payload;
    }

    @Override
    public Map<String, Object> detail(Long resultSetId, String reviewStatus, String reviewedBy)
    {
        presetScenarioService.ensurePresetScenario();
        MatchResultSet resultSet = requireResultSet(resultSetId);
        if (presetScenarioService.isPresetResultSet(resultSet))
        {
            throw new ServiceException("该结果集不在前端展示审核明细");
        }
        MatchResultRow query = new MatchResultRow();
        query.setResultSetId(resultSetId);
        List<MatchResultRow> resultRows = matchResultRowMapper.selectMatchResultRowList(query);

        Map<String, Object> payload = new HashMap<>();
        payload.put("resultSetId", resultSet.getResultSetId());
        payload.put("taskId", resultSet.getTaskId());
        payload.put("recordId", resultSet.getRecordId());
        payload.put("rows", toPreviewRows(resultRows, resultSetId, reviewStatus, reviewedBy));
        return payload;
    }

    @Override
    public Map<String, Object> metrics(Long resultSetId)
    {
        presetScenarioService.ensurePresetScenario();
        MatchResultSet resultSet = requireResultSet(resultSetId);
        if (presetScenarioService.isPresetResultSet(resultSet))
        {
            throw new ServiceException("该结果集不在前端展示评估指标");
        }
        TaskMetric query = new TaskMetric();
        query.setResultSetId(resultSetId);
        List<TaskMetric> metrics = taskMetricMapper.selectTaskMetricList(query);
        Map<String, Object> payload = new LinkedHashMap<>();
        List<Map<String, Object>> currentCards = currentMetricCards(metrics);
        payload.put("currentCards", currentCards);
        payload.put("cards", currentCards);
        payload.put("candidateF1Comparison", candidateF1Comparison(resultSet));
        payload.put("scoreDistribution", chartRows(metrics, "scoreDistribution"));
        payload.put("targetDistribution", normalizeTargetDistribution(chartRows(metrics, "targetTopDistribution")));
        payload.put("resultSetId", resultSet.getResultSetId());
        payload.put("resultDir", metrics.isEmpty() ? null : metrics.get(0).getResultDir());
        return payload;
    }

    private List<Map<String, Object>> candidateF1Comparison(MatchResultSet current)
    {
        if (current.getRecordId() == null)
        {
            List<Map<String, Object>> single = new ArrayList<>();
            BigDecimal f1Score = findMetricValue(current.getResultSetId(), "f1Score");
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("resultSetId", current.getResultSetId());
            row.put("label", resultSetComparisonLabel(current));
            row.put("method", current.getMethod());
            row.put("variant", current.getVariant());
            row.put("f1Score", f1Score);
            row.put("value", f1Score);
            row.put("isCurrent", true);
            row.put("isDefault", current.getIsDefault());
            row.put("totalRows", current.getTotalRows());
            single.add(row);
            return single;
        }
        MatchResultSet query = new MatchResultSet();
        query.setRecordId(current.getRecordId());
        List<MatchResultSet> resultSets = matchResultSetMapper.selectMatchResultSetList(query);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (MatchResultSet item : resultSets)
        {
            if (presetScenarioService.isPresetResultSet(item))
            {
                continue;
            }
            BigDecimal f1Score = findMetricValue(item.getResultSetId(), "f1Score");
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("resultSetId", item.getResultSetId());
            row.put("label", resultSetComparisonLabel(item));
            row.put("method", item.getMethod());
            row.put("variant", item.getVariant());
            row.put("f1Score", f1Score);
            row.put("value", f1Score);
            row.put("isCurrent", item.getResultSetId() != null && item.getResultSetId().equals(current.getResultSetId()));
            row.put("isDefault", item.getIsDefault());
            row.put("totalRows", item.getTotalRows());
            rows.add(row);
        }
        rows.sort((left, right) -> asBigDecimal(right.get("f1Score")).compareTo(asBigDecimal(left.get("f1Score"))));
        return rows;
    }

    private String resultSetComparisonLabel(MatchResultSet resultSet)
    {
        String method = StringUtils.defaultIfBlank(resultSet.getMethod(), "");
        String variant = StringUtils.defaultIfBlank(resultSet.getVariant(), "");
        String label = (method + " " + variant).trim();
        if (StringUtils.isNotBlank(label))
        {
            return label;
        }
        return StringUtils.defaultIfBlank(resultSet.getResultSetName(), String.valueOf(resultSet.getResultSetId()));
    }

    private List<Map<String, Object>> currentMetricCards(List<TaskMetric> metrics)
    {
        List<Map<String, Object>> cards = new ArrayList<>();
        addCurrentMetricCard(cards, metrics, "f1Score", "F1 综合分");
        addCurrentMetricCard(cards, metrics, "mrr", "MRR");
        addCurrentMetricCard(cards, metrics, "precision", "Precision");
        addCurrentMetricCard(cards, metrics, "recallAt20", "Recall@20");
        return cards;
    }

    private void addCurrentMetricCard(List<Map<String, Object>> cards, List<TaskMetric> metrics, String key, String label)
    {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("key", key);
        card.put("label", label);
        card.put("value", findMetricValue(metrics, key));
        cards.add(card);
    }

    private List<Map<String, Object>> chartRows(List<TaskMetric> metrics, String metricKey)
    {
        for (TaskMetric metric : metrics)
        {
            if (!metricKey.equals(metric.getMetricKey()) || StringUtils.isBlank(metric.getChartData()))
            {
                continue;
            }
            try
            {
                return objectMapper.readValue(metric.getChartData(), CHART_ROWS_TYPE);
            }
            catch (Exception e)
            {
                return List.of();
            }
        }
        return List.of();
    }

    private List<Map<String, Object>> normalizeTargetDistribution(List<Map<String, Object>> rows)
    {
        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Map<String, Object> row : rows)
        {
            Map<String, Object> item = new LinkedHashMap<>(row);
            String label = firstChartText(item.get("label"), item.get("targetTable"), item.get("target_table"), item.get("name"), item.get("key"));
            item.put("label", StringUtils.defaultIfBlank(label, "-"));
            item.put("value", normalizeChartValue(firstChartValue(item.get("value"), item.get("count"), item.get("total"))));
            if (!item.containsKey("targetTable") && StringUtils.isNotBlank(label) && !"-".equals(label))
            {
                item.put("targetTable", label);
            }
            normalized.add(item);
        }
        return normalized;
    }

    private String firstChartText(Object... values)
    {
        for (Object value : values)
        {
            if (value == null)
            {
                continue;
            }
            String text = String.valueOf(value).trim();
            if (StringUtils.isNotBlank(text))
            {
                return text;
            }
        }
        return "-";
    }

    private Object firstChartValue(Object... values)
    {
        for (Object value : values)
        {
            if (value != null)
            {
                return value;
            }
        }
        return 0;
    }

    private Object normalizeChartValue(Object value)
    {
        if (value instanceof Number)
        {
            return value;
        }
        if (value == null)
        {
            return 0;
        }
        String text = String.valueOf(value).trim();
        if (StringUtils.isBlank(text))
        {
            return 0;
        }
        try
        {
            return new BigDecimal(text);
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    private List<Map<String, Object>> toPreviewRows(List<MatchResultRow> resultRows, Long resultSetId, String reviewStatus, String reviewedBy)
    {
        List<ReviewedMatch> reviews = queryReviews(resultSetId, reviewStatus, reviewedBy);
        boolean hasFilter = hasFilter(reviewStatus) || hasFilter(reviewedBy);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (MatchResultRow row : resultRows)
        {
            ReviewedMatch review = findReview(reviews, row.getResultRowId());
            if (hasFilter && review == null)
            {
                continue;
            }
            rows.add(toPreviewRow(row, review));
        }
        return rows;
    }

    private List<ReviewedMatch> queryReviews(Long resultSetId, String reviewStatus, String reviewedBy)
    {
        ReviewedMatch reviewQuery = new ReviewedMatch();
        reviewQuery.setResultSetId(resultSetId);
        if (hasFilter(reviewStatus))
        {
            reviewQuery.setReviewStatus(reviewStatus);
        }
        if (hasFilter(reviewedBy))
        {
            reviewQuery.setReviewedBy(reviewedBy);
        }
        return reviewedMatchMapper.selectReviewedMatchList(reviewQuery);
    }

    private Map<String, Object> toPreviewRow(MatchResultRow row, ReviewedMatch review)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("reviewId", review == null ? null : review.getReviewId());
        item.put("resultRowId", row.getResultRowId());
        item.put("sourceDatabase", review == null ? row.getSourceDatabase() : review.getSourceDatabase());
        item.put("sourceTable", review == null ? row.getSourceTable() : review.getSourceTable());
        item.put("sourceColumn", review == null ? row.getSourceColumn() : review.getSourceColumn());
        item.put("proposedTargetTable", review == null ? row.getTargetTable() : review.getProposedTargetTable());
        item.put("proposedTargetColumn", review == null ? row.getTargetColumn() : review.getProposedTargetColumn());
        item.put("targetTable", review == null ? row.getTargetTable() : StringUtils.defaultIfBlank(review.getTargetTable(), review.getProposedTargetTable()));
        item.put("targetColumn", review == null ? row.getTargetColumn() : StringUtils.defaultIfBlank(review.getTargetColumn(), review.getProposedTargetColumn()));
        item.put("mappingType", review == null ? "direct" : review.getMappingType());
        item.put("score", review == null ? row.getScore() : review.getAlgorithmScore());
        item.put("algorithmScore", review == null ? row.getScore() : review.getAlgorithmScore());
        item.put("reviewStatus", review == null ? "pending" : review.getReviewStatus());
        item.put("reviewer", review == null ? "-" : StringUtils.defaultIfBlank(review.getReviewedBy(), "-"));
        item.put("reviewedAt", review == null ? null : review.getReviewedAt());
        item.put("remark", review == null ? null : review.getRemark());
        return item;
    }

    private ReviewedMatch findReview(List<ReviewedMatch> reviews, Long resultRowId)
    {
        for (ReviewedMatch review : reviews)
        {
            if (resultRowId != null && resultRowId.equals(review.getResultRowId()))
            {
                return review;
            }
        }
        return null;
    }

    private boolean hasFilter(String value)
    {
        return StringUtils.isNotBlank(value) && !"all".equals(value);
    }

    private List<Map<String, Object>> toMetricCards(List<TaskMetric> metrics)
    {
        List<Map<String, Object>> cards = new ArrayList<>();
        for (TaskMetric metric : metrics)
        {
            if (!"card".equals(metric.getChartType()) && !"summary".equals(metric.getChartType()))
            {
                continue;
            }
            Map<String, Object> card = new LinkedHashMap<>();
            card.put("label", metric.getMetricName());
            card.put("value", metric.getMetricValue());
            cards.add(card);
        }
        return cards;
    }

    private BigDecimal findMetricValue(List<TaskMetric> metrics, String metricKey)
    {
        if (metrics == null || StringUtils.isBlank(metricKey))
        {
            return null;
        }
        for (TaskMetric metric : metrics)
        {
            if (metricKey.equals(metric.getMetricKey()))
            {
                return metric.getMetricValue();
            }
        }
        return null;
    }

    private BigDecimal findMetricValue(Long resultSetId, String metricKey)
    {
        if (resultSetId == null || StringUtils.isBlank(metricKey))
        {
            return null;
        }
        TaskMetric query = new TaskMetric();
        query.setResultSetId(resultSetId);
        query.setMetricKey(metricKey);
        List<TaskMetric> rows = taskMetricMapper.selectTaskMetricList(query);
        return rows.isEmpty() ? null : rows.get(0).getMetricValue();
    }

    private BigDecimal asBigDecimal(Object value)
    {
        if (value instanceof BigDecimal)
        {
            return (BigDecimal) value;
        }
        if (value instanceof Number)
        {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value == null)
        {
            return BigDecimal.ZERO;
        }
        String text = String.valueOf(value).trim();
        if (StringUtils.isBlank(text))
        {
            return BigDecimal.ZERO;
        }
        try
        {
            return new BigDecimal(text);
        }
        catch (NumberFormatException e)
        {
            return BigDecimal.ZERO;
        }
    }

    private MatchResultSet requireResultSet(Long resultSetId)
    {
        if (resultSetId == null)
        {
            throw new ServiceException("结果集 ID 不能为空");
        }
        MatchResultSet resultSet = matchResultSetMapper.selectMatchResultSetByResultSetId(resultSetId);
        if (resultSet == null)
        {
            throw new ServiceException("结果集不存在，请刷新页面后重试");
        }
        return resultSet;
    }
}
