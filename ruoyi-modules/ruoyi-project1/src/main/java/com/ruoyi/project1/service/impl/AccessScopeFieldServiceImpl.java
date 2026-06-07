package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.AccessScopeFieldMapper;
import com.ruoyi.project1.domain.AccessScopeField;
import com.ruoyi.project1.service.IAccessScopeFieldService;

/**
 * 接入范围字段Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class AccessScopeFieldServiceImpl implements IAccessScopeFieldService 
{
    @Autowired
    private AccessScopeFieldMapper accessScopeFieldMapper;

    /**
     * 查询接入范围字段
     * 
     * @param scopeFieldId 接入范围字段主键
     * @return 接入范围字段
     */
    @Override
    public AccessScopeField selectAccessScopeFieldByScopeFieldId(Long scopeFieldId)
    {
        return accessScopeFieldMapper.selectAccessScopeFieldByScopeFieldId(scopeFieldId);
    }

    /**
     * 查询接入范围字段列表
     * 
     * @param accessScopeField 接入范围字段
     * @return 接入范围字段
     */
    @Override
    public List<AccessScopeField> selectAccessScopeFieldList(AccessScopeField accessScopeField)
    {
        return accessScopeFieldMapper.selectAccessScopeFieldList(accessScopeField);
    }

    /**
     * 新增接入范围字段
     * 
     * @param accessScopeField 接入范围字段
     * @return 结果
     */
    @Override
    public int insertAccessScopeField(AccessScopeField accessScopeField)
    {
        accessScopeField.setCreateTime(DateUtils.getNowDate());
        return accessScopeFieldMapper.insertAccessScopeField(accessScopeField);
    }

    /**
     * 修改接入范围字段
     * 
     * @param accessScopeField 接入范围字段
     * @return 结果
     */
    @Override
    public int updateAccessScopeField(AccessScopeField accessScopeField)
    {
        accessScopeField.setUpdateTime(DateUtils.getNowDate());
        return accessScopeFieldMapper.updateAccessScopeField(accessScopeField);
    }

    /**
     * 批量删除接入范围字段
     * 
     * @param scopeFieldIds 需要删除的接入范围字段主键
     * @return 结果
     */
    @Override
    public int deleteAccessScopeFieldByScopeFieldIds(Long[] scopeFieldIds)
    {
        return accessScopeFieldMapper.deleteAccessScopeFieldByScopeFieldIds(scopeFieldIds);
    }

    /**
     * 删除接入范围字段信息
     * 
     * @param scopeFieldId 接入范围字段主键
     * @return 结果
     */
    @Override
    public int deleteAccessScopeFieldByScopeFieldId(Long scopeFieldId)
    {
        return accessScopeFieldMapper.deleteAccessScopeFieldByScopeFieldId(scopeFieldId);
    }
}
