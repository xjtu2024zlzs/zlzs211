package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.EvalModeMapper;
import com.ruoyi.project1.domain.EvalMode;
import com.ruoyi.project1.service.IEvalModeService;

/**
 * 评估模式配置Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class EvalModeServiceImpl implements IEvalModeService 
{
    @Autowired
    private EvalModeMapper evalModeMapper;

    /**
     * 查询评估模式配置
     * 
     * @param evalModeKey 评估模式配置主键
     * @return 评估模式配置
     */
    @Override
    public EvalMode selectEvalModeByEvalModeKey(String evalModeKey)
    {
        return evalModeMapper.selectEvalModeByEvalModeKey(evalModeKey);
    }

    /**
     * 查询评估模式配置列表
     * 
     * @param evalMode 评估模式配置
     * @return 评估模式配置
     */
    @Override
    public List<EvalMode> selectEvalModeList(EvalMode evalMode)
    {
        return evalModeMapper.selectEvalModeList(evalMode);
    }

    /**
     * 新增评估模式配置
     * 
     * @param evalMode 评估模式配置
     * @return 结果
     */
    @Override
    public int insertEvalMode(EvalMode evalMode)
    {
        evalMode.setCreateTime(DateUtils.getNowDate());
        return evalModeMapper.insertEvalMode(evalMode);
    }

    /**
     * 修改评估模式配置
     * 
     * @param evalMode 评估模式配置
     * @return 结果
     */
    @Override
    public int updateEvalMode(EvalMode evalMode)
    {
        evalMode.setUpdateTime(DateUtils.getNowDate());
        return evalModeMapper.updateEvalMode(evalMode);
    }

    /**
     * 批量删除评估模式配置
     * 
     * @param evalModeKeys 需要删除的评估模式配置主键
     * @return 结果
     */
    @Override
    public int deleteEvalModeByEvalModeKeys(String[] evalModeKeys)
    {
        return evalModeMapper.deleteEvalModeByEvalModeKeys(evalModeKeys);
    }

    /**
     * 删除评估模式配置信息
     * 
     * @param evalModeKey 评估模式配置主键
     * @return 结果
     */
    @Override
    public int deleteEvalModeByEvalModeKey(String evalModeKey)
    {
        return evalModeMapper.deleteEvalModeByEvalModeKey(evalModeKey);
    }
}
