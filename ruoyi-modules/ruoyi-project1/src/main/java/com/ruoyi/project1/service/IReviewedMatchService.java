package com.ruoyi.project1.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.project1.domain.MatchReviewRequest;
import com.ruoyi.project1.domain.ReviewHistory;
import com.ruoyi.project1.domain.ReviewedMatch;

/**
 * 审核后的字段匹配结果Service接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface IReviewedMatchService 
{
    /**
     * 查询审核后的字段匹配结果
     * 
     * @param reviewId 审核后的字段匹配结果主键
     * @return 审核后的字段匹配结果
     */
    public ReviewedMatch selectReviewedMatchByReviewId(Long reviewId);

    /**
     * 查询审核后的字段匹配结果列表
     * 
     * @param reviewedMatch 审核后的字段匹配结果
     * @return 审核后的字段匹配结果集合
     */
    public List<ReviewedMatch> selectReviewedMatchList(ReviewedMatch reviewedMatch);

    /**
     * 新增审核后的字段匹配结果
     * 
     * @param reviewedMatch 审核后的字段匹配结果
     * @return 结果
     */
    public int insertReviewedMatch(ReviewedMatch reviewedMatch);

    /**
     * 修改审核后的字段匹配结果
     * 
     * @param reviewedMatch 审核后的字段匹配结果
     * @return 结果
     */
    public int updateReviewedMatch(ReviewedMatch reviewedMatch);

    /**
     * 批量删除审核后的字段匹配结果
     * 
     * @param reviewIds 需要删除的审核后的字段匹配结果主键集合
     * @return 结果
     */
    public int deleteReviewedMatchByReviewIds(Long[] reviewIds);

    /**
     * 删除审核后的字段匹配结果信息
     * 
     * @param reviewId 审核后的字段匹配结果主键
     * @return 结果
     */
    public int deleteReviewedMatchByReviewId(Long reviewId);

    /**
     * Approve reviewed match rows.
     *
     * @param request review request
     * @return operation summary
     */
    public Map<String, Object> approve(MatchReviewRequest request);

    /**
     * Reject reviewed match rows.
     *
     * @param request review request
     * @return operation summary
     */
    public Map<String, Object> reject(MatchReviewRequest request);

    /**
     * Auto approve pending rows by current task version threshold.
     *
     * @param resultSetId result set id
     * @return operation summary
     */
    public Map<String, Object> autoApprove(Long resultSetId);

    /**
     * Query review history for one review row.
     *
     * @param reviewId review id
     * @return review history rows
     */
    public List<ReviewHistory> history(Long reviewId);

    /**
     * Rebuild final access mapping rules from approved review rows.
     *
     * @param resultSetId result set id
     * @return rebuild summary
     */
    public Map<String, Object> rebuildMappingSpecForResultSet(Long resultSetId);
}
