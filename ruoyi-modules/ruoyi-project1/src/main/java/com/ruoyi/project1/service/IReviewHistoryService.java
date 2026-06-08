package com.ruoyi.project1.service;

import java.util.List;
import com.ruoyi.project1.domain.ReviewHistory;

/**
 * 字段匹配审核历史Service接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface IReviewHistoryService 
{
    /**
     * 查询字段匹配审核历史
     * 
     * @param historyId 字段匹配审核历史主键
     * @return 字段匹配审核历史
     */
    public ReviewHistory selectReviewHistoryByHistoryId(Long historyId);

    /**
     * 查询字段匹配审核历史列表
     * 
     * @param reviewHistory 字段匹配审核历史
     * @return 字段匹配审核历史集合
     */
    public List<ReviewHistory> selectReviewHistoryList(ReviewHistory reviewHistory);

    /**
     * 新增字段匹配审核历史
     * 
     * @param reviewHistory 字段匹配审核历史
     * @return 结果
     */
    public int insertReviewHistory(ReviewHistory reviewHistory);

    /**
     * 修改字段匹配审核历史
     * 
     * @param reviewHistory 字段匹配审核历史
     * @return 结果
     */
    public int updateReviewHistory(ReviewHistory reviewHistory);

    /**
     * 批量删除字段匹配审核历史
     * 
     * @param historyIds 需要删除的字段匹配审核历史主键集合
     * @return 结果
     */
    public int deleteReviewHistoryByHistoryIds(Long[] historyIds);

    /**
     * 删除字段匹配审核历史信息
     * 
     * @param historyId 字段匹配审核历史主键
     * @return 结果
     */
    public int deleteReviewHistoryByHistoryId(Long historyId);
}
