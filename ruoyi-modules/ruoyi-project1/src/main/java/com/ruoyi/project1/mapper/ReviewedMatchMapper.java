package com.ruoyi.project1.mapper;

import java.util.List;
import com.ruoyi.project1.domain.ReviewedMatch;

/**
 * 审核后的字段匹配结果Mapper接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface ReviewedMatchMapper 
{
    /**
     * 查询审核后的字段匹配结果
     * 
     * @param reviewId 审核后的字段匹配结果主键
     * @return 审核后的字段匹配结果
     */
    public ReviewedMatch selectReviewedMatchByReviewId(Long reviewId);

    /**
     * Query reviewed matches by ids.
     *
     * @param reviewIds review ids
     * @return reviewed match rows
     */
    public List<ReviewedMatch> selectReviewedMatchByReviewIds(Long[] reviewIds);

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
     * 删除审核后的字段匹配结果
     * 
     * @param reviewId 审核后的字段匹配结果主键
     * @return 结果
     */
    public int deleteReviewedMatchByReviewId(Long reviewId);

    /**
     * 批量删除审核后的字段匹配结果
     * 
     * @param reviewIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteReviewedMatchByReviewIds(Long[] reviewIds);
}
