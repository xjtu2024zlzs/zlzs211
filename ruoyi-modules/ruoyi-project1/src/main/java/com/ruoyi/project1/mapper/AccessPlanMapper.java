package com.ruoyi.project1.mapper;

import java.util.List;
import com.ruoyi.project1.domain.AccessPlan;

/**
 * 数据接入管理Mapper接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface AccessPlanMapper 
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
     * 删除数据接入管理
     * 
     * @param accessPlanId 数据接入管理主键
     * @return 结果
     */
    public int deleteAccessPlanByAccessPlanId(Long accessPlanId);

    /**
     * 批量删除数据接入管理
     * 
     * @param accessPlanIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAccessPlanByAccessPlanIds(Long[] accessPlanIds);
}
