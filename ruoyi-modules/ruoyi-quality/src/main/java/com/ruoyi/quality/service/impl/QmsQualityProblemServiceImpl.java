package com.ruoyi.quality.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.quality.mapper.QmsQualityProblemMapper;
import com.ruoyi.quality.domain.QmsQualityProblem;
import com.ruoyi.quality.service.IQmsQualityProblemService;

/**
 * 质量问题Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-13
 */
@Service
public class QmsQualityProblemServiceImpl implements IQmsQualityProblemService 
{
    @Autowired
    private QmsQualityProblemMapper qmsQualityProblemMapper;

    /**
     * 查询质量问题
     * 
     * @param problemId 质量问题主键
     * @return 质量问题
     */
    @Override
    public QmsQualityProblem selectQmsQualityProblemByProblemId(Long problemId)
    {
        return qmsQualityProblemMapper.selectQmsQualityProblemByProblemId(problemId);
    }

    /**
     * 查询质量问题列表
     * 
     * @param qmsQualityProblem 质量问题
     * @return 质量问题
     */
    @Override
    public List<QmsQualityProblem> selectQmsQualityProblemList(QmsQualityProblem qmsQualityProblem)
    {
        return qmsQualityProblemMapper.selectQmsQualityProblemList(qmsQualityProblem);
    }

    /**
     * 新增质量问题
     * 
     * @param qmsQualityProblem 质量问题
     * @return 结果
     */
    @Override
    public int insertQmsQualityProblem(QmsQualityProblem qmsQualityProblem)
    {
        qmsQualityProblem.setCreateTime(DateUtils.getNowDate());
        return qmsQualityProblemMapper.insertQmsQualityProblem(qmsQualityProblem);
    }

    /**
     * 修改质量问题
     * 
     * @param qmsQualityProblem 质量问题
     * @return 结果
     */
    @Override
    public int updateQmsQualityProblem(QmsQualityProblem qmsQualityProblem)
    {
        qmsQualityProblem.setUpdateTime(DateUtils.getNowDate());
        return qmsQualityProblemMapper.updateQmsQualityProblem(qmsQualityProblem);
    }

    /**
     * 批量删除质量问题
     * 
     * @param problemIds 需要删除的质量问题主键
     * @return 结果
     */
    @Override
    public int deleteQmsQualityProblemByProblemIds(Long[] problemIds)
    {
        return qmsQualityProblemMapper.deleteQmsQualityProblemByProblemIds(problemIds);
    }

    /**
     * 删除质量问题信息
     * 
     * @param problemId 质量问题主键
     * @return 结果
     */
    @Override
    public int deleteQmsQualityProblemByProblemId(Long problemId)
    {
        return qmsQualityProblemMapper.deleteQmsQualityProblemByProblemId(problemId);
    }
}
