package com.ruoyi.project1.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.project1.domain.AccessPlan;

/**
 * 数据接入管理Service接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface IAccessPlanService 
{
    /**
     * 查询数据接入管理
     * 
     * @param accessPlanId 数据接入管理主键
     * @return 数据接入管理
     */
    public AccessPlan selectAccessPlanByAccessPlanId(Long accessPlanId);

    /**
     * 查询数据接入管理列表
     * 
     * @param accessPlan 数据接入管理
     * @return 数据接入管理集合
     */
    public List<AccessPlan> selectAccessPlanList(AccessPlan accessPlan);

    /**
     * 新增数据接入管理
     * 
     * @param accessPlan 数据接入管理
     * @return 结果
     */
    public int insertAccessPlan(AccessPlan accessPlan);

    /**
     * 修改数据接入管理
     * 
     * @param accessPlan 数据接入管理
     * @return 结果
     */
    public int updateAccessPlan(AccessPlan accessPlan);

    /**
     * 批量删除数据接入管理
     * 
     * @param accessPlanIds 需要删除的数据接入管理主键集合
     * @return 结果
     */
    public int deleteAccessPlanByAccessPlanIds(Long[] accessPlanIds);

    /**
     * 删除数据接入管理信息
     * 
     * @param accessPlanId 数据接入管理主键
     * @return 结果
     */
    public int deleteAccessPlanByAccessPlanId(Long accessPlanId);

    /**
     * Execute one access plan without external algorithm dependency.
     *
     * @param accessPlanId access plan id
     * @return execution result payload
     */
    public Map<String, Object> execute(Long accessPlanId);

    /**
     * Pause one access plan.
     *
     * @param accessPlanId access plan id
     * @return rows affected
     */
    public int pause(Long accessPlanId);

    /**
     * Resume one access plan.
     *
     * @param accessPlanId access plan id
     * @return rows affected
     */
    public int resume(Long accessPlanId);

    /**
     * Cancel current execution of one access plan.
     *
     * @param accessPlanId access plan id
     * @return rows affected
     */
    public int cancel(Long accessPlanId);
}
