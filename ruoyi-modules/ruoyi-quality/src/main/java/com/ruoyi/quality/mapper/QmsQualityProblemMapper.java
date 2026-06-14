package com.ruoyi.quality.mapper;

import java.util.List;
import com.ruoyi.quality.domain.QmsQualityProblem;

/**
 * 质量问题Mapper接口
 * 
 * @author ruoyi
 * @date 2026-06-13
 */
public interface QmsQualityProblemMapper 
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
     * 删除质量问题
     * 
     * @param problemId 质量问题主键
     * @return 结果
     */
    public int deleteQmsQualityProblemByProblemId(Long problemId);

    /**
     * 批量删除质量问题
     * 
     * @param problemIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteQmsQualityProblemByProblemIds(Long[] problemIds);
}
