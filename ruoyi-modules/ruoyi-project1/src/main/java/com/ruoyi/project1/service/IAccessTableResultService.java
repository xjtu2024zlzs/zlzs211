package com.ruoyi.project1.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.project1.domain.AccessTableResult;

/**
 * 数据接入结果展示Service接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface IAccessTableResultService 
{
    /**
     * 查询数据接入结果展示
     * 
     * @param tableResultId 数据接入结果展示主键
     * @return 数据接入结果展示
     */
    public AccessTableResult selectAccessTableResultByTableResultId(Long tableResultId);

    /**
     * 查询数据接入结果展示列表
     * 
     * @param accessTableResult 数据接入结果展示
     * @return 数据接入结果展示集合
     */
    public List<AccessTableResult> selectAccessTableResultList(AccessTableResult accessTableResult);

    /**
     * 新增数据接入结果展示
     * 
     * @param accessTableResult 数据接入结果展示
     * @return 结果
     */
    public int insertAccessTableResult(AccessTableResult accessTableResult);

    /**
     * 修改数据接入结果展示
     * 
     * @param accessTableResult 数据接入结果展示
     * @return 结果
     */
    public int updateAccessTableResult(AccessTableResult accessTableResult);

    /**
     * 批量删除数据接入结果展示
     * 
     * @param tableResultIds 需要删除的数据接入结果展示主键集合
     * @return 结果
     */
    public int deleteAccessTableResultByTableResultIds(Long[] tableResultIds);

    /**
     * 删除数据接入结果展示信息
     * 
     * @param tableResultId 数据接入结果展示主键
     * @return 结果
     */
    public int deleteAccessTableResultByTableResultId(Long tableResultId);

    /**
     * Summarize access results for one plan.
     *
     * @param accessPlanId access plan id
     * @return summary payload
     */
    public Map<String, Object> summary(Long accessPlanId);

    /**
     * Query chart dashboard payload for one access plan.
     *
     * @param accessPlanId access plan id
     * @return dashboard payload
     */
    public Map<String, Object> dashboard(Long accessPlanId);
}
