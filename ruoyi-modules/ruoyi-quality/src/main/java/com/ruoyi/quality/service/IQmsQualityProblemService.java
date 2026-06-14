package com.ruoyi.quality.service;

import java.util.List;
import com.ruoyi.quality.domain.QmsQualityProblem;

/**
 * 质量问题Service接口
 * 
 * @author ruoyi
 * @date 2026-06-13
 */
public interface IQmsQualityProblemService 
{
    /**
     * 查询质量问题
     * 
     * @param problemId 质量问题主键
     * @return 质量问题
     */
    public QmsQualityProblem selectQmsQualityProblemByProblemId(Long problemId);

    /**
     * 查询质量问题列表
     * 
     * @param qmsQualityProblem 质量问题
     * @return 质量问题集合
     */
    public List<QmsQualityProblem> selectQmsQualityProblemList(QmsQualityProblem qmsQualityProblem);

    /**
     * 新增质量问题
     * 
     * @param qmsQualityProblem 质量问题
     * @return 结果
     */
    public int insertQmsQualityProblem(QmsQualityProblem qmsQualityProblem);

    /**
     * 修改质量问题
     * 
     * @param qmsQualityProblem 质量问题
     * @return 结果
     */
    public int updateQmsQualityProblem(QmsQualityProblem qmsQualityProblem);

    /**
     * 批量删除质量问题
     * 
     * @param problemIds 需要删除的质量问题主键集合
     * @return 结果
     */
    public int deleteQmsQualityProblemByProblemIds(Long[] problemIds);

    /**
     * 删除质量问题信息
     * 
     * @param problemId 质量问题主键
     * @return 结果
     */
    public int deleteQmsQualityProblemByProblemId(Long problemId);
}
