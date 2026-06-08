package com.ruoyi.project1.mapper;

import java.util.List;
import com.ruoyi.project1.domain.EvalMode;

/**
 * 评估模式配置Mapper接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface EvalModeMapper 
{
    /**
     * 查询评估模式配置
     * 
     * @param evalModeKey 评估模式配置主键
     * @return 评估模式配置
     */
    public EvalMode selectEvalModeByEvalModeKey(String evalModeKey);

    /**
     * 查询评估模式配置列表
     * 
     * @param evalMode 评估模式配置
     * @return 评估模式配置集合
     */
    public List<EvalMode> selectEvalModeList(EvalMode evalMode);

    /**
     * 新增评估模式配置
     * 
     * @param evalMode 评估模式配置
     * @return 结果
     */
    public int insertEvalMode(EvalMode evalMode);

    /**
     * 修改评估模式配置
     * 
     * @param evalMode 评估模式配置
     * @return 结果
     */
    public int updateEvalMode(EvalMode evalMode);

    /**
     * 删除评估模式配置
     * 
     * @param evalModeKey 评估模式配置主键
     * @return 结果
     */
    public int deleteEvalModeByEvalModeKey(String evalModeKey);

    /**
     * 批量删除评估模式配置
     * 
     * @param evalModeKeys 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteEvalModeByEvalModeKeys(String[] evalModeKeys);
}
