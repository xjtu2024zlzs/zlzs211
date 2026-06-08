package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.ReviewHistoryMapper;
import com.ruoyi.project1.domain.ReviewHistory;
import com.ruoyi.project1.service.IReviewHistoryService;

/**
 * 字段匹配审核历史Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class ReviewHistoryServiceImpl implements IReviewHistoryService 
{
    @Autowired
    private ReviewHistoryMapper reviewHistoryMapper;

    /**
     * 查询字段匹配审核历史
     * 
     * @param historyId 字段匹配审核历史主键
     * @return 字段匹配审核历史
     */
    @Override
    public ReviewHistory selectReviewHistoryByHistoryId(Long historyId)
    {
        return reviewHistoryMapper.selectReviewHistoryByHistoryId(historyId);
    }

    /**
     * 查询字段匹配审核历史列表
     * 
     * @param reviewHistory 字段匹配审核历史
     * @return 字段匹配审核历史
     */
    @Override
    public List<ReviewHistory> selectReviewHistoryList(ReviewHistory reviewHistory)
    {
        return reviewHistoryMapper.selectReviewHistoryList(reviewHistory);
    }

    /**
     * 新增字段匹配审核历史
     * 
     * @param reviewHistory 字段匹配审核历史
     * @return 结果
     */
    @Override
    public int insertReviewHistory(ReviewHistory reviewHistory)
    {
        reviewHistory.setCreateTime(DateUtils.getNowDate());
        return reviewHistoryMapper.insertReviewHistory(reviewHistory);
    }

    /**
     * 修改字段匹配审核历史
     * 
     * @param reviewHistory 字段匹配审核历史
     * @return 结果
     */
    @Override
    public int updateReviewHistory(ReviewHistory reviewHistory)
    {
        return reviewHistoryMapper.updateReviewHistory(reviewHistory);
    }

    /**
     * 批量删除字段匹配审核历史
     * 
     * @param historyIds 需要删除的字段匹配审核历史主键
     * @return 结果
     */
    @Override
    public int deleteReviewHistoryByHistoryIds(Long[] historyIds)
    {
        return reviewHistoryMapper.deleteReviewHistoryByHistoryIds(historyIds);
    }

    /**
     * 删除字段匹配审核历史信息
     * 
     * @param historyId 字段匹配审核历史主键
     * @return 结果
     */
    @Override
    public int deleteReviewHistoryByHistoryId(Long historyId)
    {
        return reviewHistoryMapper.deleteReviewHistoryByHistoryId(historyId);
    }
}
