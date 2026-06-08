package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.AccessScopeTableMapper;
import com.ruoyi.project1.domain.AccessScopeTable;
import com.ruoyi.project1.service.IAccessScopeTableService;

/**
 * 接入范围源目标关系Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class AccessScopeTableServiceImpl implements IAccessScopeTableService 
{
    @Autowired
    private AccessScopeTableMapper accessScopeTableMapper;

    /**
     * 查询接入范围源目标关系
     * 
     * @param scopeTableId 接入范围源目标关系主键
     * @return 接入范围源目标关系
     */
    @Override
    public AccessScopeTable selectAccessScopeTableByScopeTableId(Long scopeTableId)
    {
        return accessScopeTableMapper.selectAccessScopeTableByScopeTableId(scopeTableId);
    }

    /**
     * 查询接入范围源目标关系列表
     * 
     * @param accessScopeTable 接入范围源目标关系
     * @return 接入范围源目标关系
     */
    @Override
    public List<AccessScopeTable> selectAccessScopeTableList(AccessScopeTable accessScopeTable)
    {
        return accessScopeTableMapper.selectAccessScopeTableList(accessScopeTable);
    }

    /**
     * 新增接入范围源目标关系
     * 
     * @param accessScopeTable 接入范围源目标关系
     * @return 结果
     */
    @Override
    public int insertAccessScopeTable(AccessScopeTable accessScopeTable)
    {
        accessScopeTable.setCreateTime(DateUtils.getNowDate());
        return accessScopeTableMapper.insertAccessScopeTable(accessScopeTable);
    }

    /**
     * 修改接入范围源目标关系
     * 
     * @param accessScopeTable 接入范围源目标关系
     * @return 结果
     */
    @Override
    public int updateAccessScopeTable(AccessScopeTable accessScopeTable)
    {
        accessScopeTable.setUpdateTime(DateUtils.getNowDate());
        return accessScopeTableMapper.updateAccessScopeTable(accessScopeTable);
    }

    /**
     * 批量删除接入范围源目标关系
     * 
     * @param scopeTableIds 需要删除的接入范围源目标关系主键
     * @return 结果
     */
    @Override
    public int deleteAccessScopeTableByScopeTableIds(Long[] scopeTableIds)
    {
        return accessScopeTableMapper.deleteAccessScopeTableByScopeTableIds(scopeTableIds);
    }

    /**
     * 删除接入范围源目标关系信息
     * 
     * @param scopeTableId 接入范围源目标关系主键
     * @return 结果
     */
    @Override
    public int deleteAccessScopeTableByScopeTableId(Long scopeTableId)
    {
        return accessScopeTableMapper.deleteAccessScopeTableByScopeTableId(scopeTableId);
    }
}
