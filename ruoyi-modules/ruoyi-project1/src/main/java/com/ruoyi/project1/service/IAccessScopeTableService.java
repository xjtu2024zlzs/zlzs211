package com.ruoyi.project1.service;

import java.util.List;
import com.ruoyi.project1.domain.AccessScopeTable;

/**
 * 接入范围源目标关系Service接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface IAccessScopeTableService 
{
    /**
     * 查询接入范围源目标关系
     * 
     * @param scopeTableId 接入范围源目标关系主键
     * @return 接入范围源目标关系
     */
    public AccessScopeTable selectAccessScopeTableByScopeTableId(Long scopeTableId);

    /**
     * 查询接入范围源目标关系列表
     * 
     * @param accessScopeTable 接入范围源目标关系
     * @return 接入范围源目标关系集合
     */
    public List<AccessScopeTable> selectAccessScopeTableList(AccessScopeTable accessScopeTable);

    /**
     * 新增接入范围源目标关系
     * 
     * @param accessScopeTable 接入范围源目标关系
     * @return 结果
     */
    public int insertAccessScopeTable(AccessScopeTable accessScopeTable);

    /**
     * 修改接入范围源目标关系
     * 
     * @param accessScopeTable 接入范围源目标关系
     * @return 结果
     */
    public int updateAccessScopeTable(AccessScopeTable accessScopeTable);

    /**
     * 批量删除接入范围源目标关系
     * 
     * @param scopeTableIds 需要删除的接入范围源目标关系主键集合
     * @return 结果
     */
    public int deleteAccessScopeTableByScopeTableIds(Long[] scopeTableIds);

    /**
     * 删除接入范围源目标关系信息
     * 
     * @param scopeTableId 接入范围源目标关系主键
     * @return 结果
     */
    public int deleteAccessScopeTableByScopeTableId(Long scopeTableId);
}
