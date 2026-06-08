package com.ruoyi.project1.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.project1.domain.MappingSpec;
import com.ruoyi.project1.domain.MappingSpecSet;
import com.ruoyi.project1.domain.MatchResultSet;
import com.ruoyi.project1.domain.MatchReviewRequest;
import com.ruoyi.project1.domain.MatchTaskVersion;
import com.ruoyi.project1.domain.ReviewHistory;
import com.ruoyi.project1.domain.ReviewedMatch;
import com.ruoyi.project1.mapper.MappingSpecMapper;
import com.ruoyi.project1.mapper.MappingSpecSetMapper;
import com.ruoyi.project1.mapper.MatchResultSetMapper;
import com.ruoyi.project1.mapper.MatchTaskVersionMapper;
import com.ruoyi.project1.mapper.ReviewHistoryMapper;
import com.ruoyi.project1.mapper.ReviewedMatchMapper;
import com.ruoyi.project1.service.IReviewedMatchService;

/**
 * Service implementation for reviewed field match rows.
 */
@Service
public class ReviewedMatchServiceImpl implements IReviewedMatchService
{
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_AUTO_APPROVED = "auto_approved";
    private static final String STATUS_APPROVED = "approved";
    private static final String STATUS_REJECTED = "rejected";

    @Autowired
    private ReviewedMatchMapper reviewedMatchMapper;

    @Autowired
    private ReviewHistoryMapper reviewHistoryMapper;

    @Autowired
    private MatchResultSetMapper matchResultSetMapper;

    @Autowired
    private MatchTaskVersionMapper matchTaskVersionMapper;

    @Autowired
    private MappingSpecSetMapper mappingSpecSetMapper;

    @Autowired
    private MappingSpecMapper mappingSpecMapper;

    @Override
    public ReviewedMatch selectReviewedMatchByReviewId(Long reviewId)
    {
        return reviewedMatchMapper.selectReviewedMatchByReviewId(reviewId);
    }

    @Override
    public List<ReviewedMatch> selectReviewedMatchList(ReviewedMatch reviewedMatch)
    {
        return reviewedMatchMapper.selectReviewedMatchList(reviewedMatch);
    }

    @Override
    public int insertReviewedMatch(ReviewedMatch reviewedMatch)
    {
        reviewedMatch.setCreateTime(DateUtils.getNowDate());
        return reviewedMatchMapper.insertReviewedMatch(reviewedMatch);
    }

    @Override
    public int updateReviewedMatch(ReviewedMatch reviewedMatch)
    {
        return reviewedMatchMapper.updateReviewedMatch(reviewedMatch);
    }

    @Override
    public int deleteReviewedMatchByReviewIds(Long[] reviewIds)
    {
        return reviewedMatchMapper.deleteReviewedMatchByReviewIds(reviewIds);
    }

    @Override
    public int deleteReviewedMatchByReviewId(Long reviewId)
    {
        return reviewedMatchMapper.deleteReviewedMatchByReviewId(reviewId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> approve(MatchReviewRequest request)
    {
        List<ReviewedMatch> rows = requireReviewRows(request);
        String actor = currentUser();
        int changed = 0;
        for (ReviewedMatch row : rows)
        {
            String targetTable = StringUtils.defaultIfBlank(request.getTargetTable(), row.getTargetTable());
            String targetColumn = StringUtils.defaultIfBlank(request.getTargetColumn(), row.getTargetColumn());
            if (StringUtils.isBlank(targetTable))
            {
                targetTable = row.getProposedTargetTable();
            }
            if (StringUtils.isBlank(targetColumn))
            {
                targetColumn = row.getProposedTargetColumn();
            }
            changed += changeStatus(row, STATUS_APPROVED, targetTable, targetColumn, actor, "approve", request.getRemark());
        }
        refreshTouchedResultSets(rows);
        return summary("approve", changed, "审核通过完成");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> reject(MatchReviewRequest request)
    {
        List<ReviewedMatch> rows = requireReviewRows(request);
        String actor = currentUser();
        int changed = 0;
        for (ReviewedMatch row : rows)
        {
            changed += changeStatus(row, STATUS_REJECTED, row.getTargetTable(), row.getTargetColumn(), actor, "reject", request.getRemark());
        }
        refreshTouchedResultSets(rows);
        return summary("reject", changed, "驳回完成");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> autoApprove(Long resultSetId)
    {
        MatchResultSet resultSet = requireResultSet(resultSetId);
        BigDecimal threshold = resolveThreshold(resultSet);

        ReviewedMatch query = new ReviewedMatch();
        query.setResultSetId(resultSetId);
        query.setReviewStatus(STATUS_PENDING);
        List<ReviewedMatch> rows = reviewedMatchMapper.selectReviewedMatchList(query);

        int changed = 0;
        for (ReviewedMatch row : rows)
        {
            BigDecimal score = row.getAlgorithmScore();
            if (score != null && score.compareTo(threshold) >= 0)
            {
                changed += changeStatus(row, STATUS_AUTO_APPROVED, row.getTargetTable(), row.getTargetColumn(), "system", "auto_approve",
                        "按自动通过阈值审核");
            }
        }
        refreshExistingSpecSetIfPresent(resultSetId);

        Map<String, Object> payload = summary("auto_approve", changed, "自动通过完成");
        payload.put("threshold", threshold);
        return payload;
    }

    @Override
    public List<ReviewHistory> history(Long reviewId)
    {
        if (reviewId == null)
        {
            throw new ServiceException("审核记录 ID 不能为空");
        }
        ReviewHistory query = new ReviewHistory();
        query.setReviewId(reviewId);
        return reviewHistoryMapper.selectReviewHistoryList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> rebuildMappingSpecForResultSet(Long resultSetId)
    {
        MatchResultSet resultSet = requireResultSet(resultSetId);
        List<ReviewedMatch> approvedRows = selectApprovedRows(resultSetId);
        if (approvedRows.isEmpty())
        {
            throw new ServiceException("请先审核通过至少一条字段匹配结果");
        }

        MappingSpecSet specSet = ensureSpecSet(resultSet, approvedRows.size(), true);
        rebuildSpecs(specSet, approvedRows);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("resultSetId", resultSetId);
        payload.put("specSetId", specSet.getSpecSetId());
        payload.put("ruleCount", approvedRows.size());
        payload.put("message", "已根据审核通过结果生成最终接入规则集");
        return payload;
    }

    private List<ReviewedMatch> requireReviewRows(MatchReviewRequest request)
    {
        if (request == null || request.getReviewIds() == null || request.getReviewIds().length == 0)
        {
            throw new ServiceException("请选择需要审核的字段匹配结果");
        }
        List<ReviewedMatch> rows = reviewedMatchMapper.selectReviewedMatchByReviewIds(request.getReviewIds());
        if (rows == null || rows.isEmpty())
        {
            throw new ServiceException("审核记录不存在，请刷新页面后重试");
        }
        return rows;
    }

    private int changeStatus(ReviewedMatch row, String newStatus, String targetTable, String targetColumn, String actor, String actionType, String remark)
    {
        String oldStatus = row.getReviewStatus();
        String oldTargetTable = row.getTargetTable();
        String oldTargetColumn = row.getTargetColumn();

        row.setReviewStatus(newStatus);
        row.setTargetTable(StringUtils.defaultIfBlank(targetTable, oldTargetTable));
        row.setTargetColumn(StringUtils.defaultIfBlank(targetColumn, oldTargetColumn));
        row.setReviewedBy(actor);
        row.setReviewedAt(DateUtils.getNowDate());
        row.setRemark(remark);
        int changed = reviewedMatchMapper.updateReviewedMatch(row);

        ReviewHistory history = new ReviewHistory();
        history.setReviewId(row.getReviewId());
        history.setTaskId(row.getTaskId());
        history.setVersionId(row.getVersionId());
        history.setRecordId(row.getRecordId());
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setOldTargetTable(oldTargetTable);
        history.setOldTargetColumn(oldTargetColumn);
        history.setNewTargetTable(row.getTargetTable());
        history.setNewTargetColumn(row.getTargetColumn());
        history.setActor(actor);
        history.setActionType(actionType);
        history.setCreateTime(DateUtils.getNowDate());
        history.setRemark(remark);
        reviewHistoryMapper.insertReviewHistory(history);
        return changed;
    }

    private void refreshTouchedResultSets(List<ReviewedMatch> rows)
    {
        List<Long> resultSetIds = new ArrayList<>();
        for (ReviewedMatch row : rows)
        {
            if (row.getResultSetId() != null && !resultSetIds.contains(row.getResultSetId()))
            {
                resultSetIds.add(row.getResultSetId());
            }
        }
        for (Long resultSetId : resultSetIds)
        {
            refreshExistingSpecSetIfPresent(resultSetId);
        }
    }

    private void refreshExistingSpecSetIfPresent(Long resultSetId)
    {
        MappingSpecSet query = new MappingSpecSet();
        query.setResultSetId(resultSetId);
        List<MappingSpecSet> specSets = mappingSpecSetMapper.selectMappingSpecSetList(query);
        if (specSets.isEmpty())
        {
            return;
        }

        MappingSpecSet specSet = specSets.get(0);
        List<ReviewedMatch> approvedRows = selectApprovedRows(resultSetId);
        mappingSpecMapper.deleteMappingSpecBySpecSetId(specSet.getSpecSetId());
        specSet.setRuleCount((long) approvedRows.size());
        specSet.setSpecStatus(approvedRows.isEmpty() ? "draft" : "active");
        specSet.setUpdateBy(currentUser());
        specSet.setUpdateTime(DateUtils.getNowDate());
        mappingSpecSetMapper.updateMappingSpecSet(specSet);
        if (!approvedRows.isEmpty())
        {
            insertSpecs(specSet, approvedRows);
        }
    }

    private MappingSpecSet ensureSpecSet(MatchResultSet resultSet, int ruleCount, boolean makeDefault)
    {
        MappingSpecSet query = new MappingSpecSet();
        query.setResultSetId(resultSet.getResultSetId());
        List<MappingSpecSet> specSets = mappingSpecSetMapper.selectMappingSpecSetList(query);
        MappingSpecSet specSet = specSets.isEmpty() ? null : specSets.get(0);
        String actor = currentUser();
        if (specSet == null)
        {
            specSet = new MappingSpecSet();
            specSet.setSpecSetName(resultSet.getResultSetName() + " 最终接入规则集");
            specSet.setResultSetId(resultSet.getResultSetId());
            specSet.setSourceDatasourceId(resultSet.getSourceDatasourceId());
            specSet.setTaskId(resultSet.getTaskId());
            specSet.setVersionId(resultSet.getVersionId());
            specSet.setRecordId(resultSet.getRecordId());
            specSet.setRuleCount((long) ruleCount);
            specSet.setIsDefault(makeDefault ? 1 : 0);
            specSet.setSpecStatus("active");
            specSet.setCreateBy(actor);
            specSet.setCreateTime(DateUtils.getNowDate());
            mappingSpecSetMapper.insertMappingSpecSet(specSet);
        }
        else
        {
            specSet.setSpecSetName(StringUtils.defaultIfBlank(specSet.getSpecSetName(), resultSet.getResultSetName() + " 最终接入规则集"));
            specSet.setRuleCount((long) ruleCount);
            specSet.setIsDefault(makeDefault ? 1 : specSet.getIsDefault());
            specSet.setSpecStatus("active");
            specSet.setUpdateBy(actor);
            specSet.setUpdateTime(DateUtils.getNowDate());
            mappingSpecSetMapper.updateMappingSpecSet(specSet);
        }
        if (makeDefault)
        {
            setDefaultSpecSet(specSet);
        }
        return specSet;
    }

    private void setDefaultSpecSet(MappingSpecSet selected)
    {
        MappingSpecSet query = new MappingSpecSet();
        query.setSourceDatasourceId(selected.getSourceDatasourceId());
        List<MappingSpecSet> rows = mappingSpecSetMapper.selectMappingSpecSetList(query);
        for (MappingSpecSet row : rows)
        {
            row.setIsDefault(row.getSpecSetId().equals(selected.getSpecSetId()) ? 1 : 0);
            row.setUpdateBy(currentUser());
            row.setUpdateTime(DateUtils.getNowDate());
            mappingSpecSetMapper.updateMappingSpecSet(row);
        }
    }

    private void rebuildSpecs(MappingSpecSet specSet, List<ReviewedMatch> approvedRows)
    {
        mappingSpecMapper.deleteMappingSpecBySpecSetId(specSet.getSpecSetId());
        insertSpecs(specSet, approvedRows);
    }

    private void insertSpecs(MappingSpecSet specSet, List<ReviewedMatch> approvedRows)
    {
        long loadOrder = 1L;
        String actor = currentUser();
        for (ReviewedMatch review : approvedRows)
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
            spec.setTargetTable(StringUtils.defaultIfBlank(review.getTargetTable(), review.getProposedTargetTable()));
            spec.setTargetColumn(StringUtils.defaultIfBlank(review.getTargetColumn(), review.getProposedTargetColumn()));
            spec.setMappingType(StringUtils.defaultIfBlank(review.getMappingType(), "direct"));
            spec.setTransformRule("{\"type\":\"direct\",\"trim\":true}");
            spec.setSpecStatus("active");
            spec.setLoadOrder(loadOrder++);
            spec.setCreateBy(actor);
            spec.setCreateTime(DateUtils.getNowDate());
            mappingSpecMapper.insertMappingSpec(spec);
        }
    }

    private List<ReviewedMatch> selectApprovedRows(Long resultSetId)
    {
        List<ReviewedMatch> rows = new ArrayList<>();
        ReviewedMatch autoQuery = new ReviewedMatch();
        autoQuery.setResultSetId(resultSetId);
        autoQuery.setReviewStatus(STATUS_AUTO_APPROVED);
        rows.addAll(reviewedMatchMapper.selectReviewedMatchList(autoQuery));

        ReviewedMatch approvedQuery = new ReviewedMatch();
        approvedQuery.setResultSetId(resultSetId);
        approvedQuery.setReviewStatus(STATUS_APPROVED);
        rows.addAll(reviewedMatchMapper.selectReviewedMatchList(approvedQuery));
        return rows;
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

    private BigDecimal resolveThreshold(MatchResultSet resultSet)
    {
        if (resultSet.getVersionId() == null)
        {
            return new BigDecimal("0.900000");
        }
        MatchTaskVersion version = matchTaskVersionMapper.selectMatchTaskVersionByVersionId(resultSet.getVersionId());
        if (version == null || version.getAutoApproveThreshold() == null)
        {
            return new BigDecimal("0.900000");
        }
        return version.getAutoApproveThreshold();
    }

    private Map<String, Object> summary(String action, int changed, String message)
    {
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", action);
        payload.put("changed", changed);
        payload.put("message", message);
        return payload;
    }

    private String currentUser()
    {
        try
        {
            String username = SecurityUtils.getUsername();
            return StringUtils.isBlank(username) ? "system" : username;
        }
        catch (Exception ex)
        {
            return "system";
        }
    }
}
