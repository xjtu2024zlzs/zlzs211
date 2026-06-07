package com.ruoyi.project1.service.impl;

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
        return matchResultSetMapper.selectMatchResultSetList(matchResultSet);
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
        MatchResultSet target = requireResultSet(resultSetId);
        MatchResultSet query = new MatchResultSet();
        query.setTaskId(target.getTaskId());
        List<MatchResultSet> resultSets = matchResultSetMapper.selectMatchResultSetList(query);
        for (MatchResultSet item : resultSets)
        {
            item.setIsDefault(item.getResultSetId().equals(resultSetId) ? 1 : 0);
            item.setUpdateTime(DateUtils.getNowDate());
            matchResultSetMapper.updateMatchResultSet(item);
        }

        Map<String, Object> payload = new LinkedHashMap<>(reviewedMatchService.rebuildMappingSpecForResultSet(resultSetId));
        payload.put("taskId", target.getTaskId());
        payload.put("sourceDatasourceId", target.getSourceDatasourceId());
        payload.put("message", "已设为默认结果集，并根据审核通过结果生成最终接入规则集");
        return payload;
    }

    @Override
    public Map<String, Object> detail(Long resultSetId, String reviewStatus, String reviewedBy)
    {
        MatchResultSet resultSet = requireResultSet(resultSetId);
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
        MatchResultSet resultSet = requireResultSet(resultSetId);
        TaskMetric query = new TaskMetric();
        query.setResultSetId(resultSetId);
        List<TaskMetric> metrics = taskMetricMapper.selectTaskMetricList(query);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("cards", toMetricCards(metrics));
        payload.put("scoreDistribution", chartRows(metrics, "scoreDistribution"));
        payload.put("targetDistribution", chartRows(metrics, "targetTopDistribution"));
        payload.put("resultSetId", resultSet.getResultSetId());
        payload.put("resultDir", metrics.isEmpty() ? null : metrics.get(0).getResultDir());
        return payload;
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
