package com.ruoyi.project1.service;

import java.util.List;
import com.ruoyi.project1.domain.AccessScopeField;

/**
 * 接入范围字段Service接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface IAccessScopeFieldService 
{
    /**
     * 查询接入范围字段
     * 
     * @param scopeFieldId 接入范围字段主键
     * @return 接入范围字段
     */
    public AccessScopeField selectAccessScopeFieldByScopeFieldId(Long scopeFieldId);

    /**
     * 查询接入范围字段列表
     * 
     * @param accessScopeField 接入范围字段
     * @return 接入范围字段集合
     */
    public List<AccessScopeField> selectAccessScopeFieldList(AccessScopeField accessScopeField);

    /**
     * 新增接入范围字段
     * 
     * @param accessScopeField 接入范围字段
     * @return 结果
     */
    public int insertAccessScopeField(AccessScopeField accessScopeField);

    /**
     * 修改接入范围字段
     * 
     * @param accessScopeField 接入范围字段
     * @return 结果
     */
    public int updateAccessScopeField(AccessScopeField accessScopeField);

    /**
     * 批量删除接入范围字段
     * 
     * @param scopeFieldIds 需要删除的接入范围字段主键集合
     * @return 结果
     */
    public int deleteAccessScopeFieldByScopeFieldIds(Long[] scopeFieldIds);

    /**
     * 删除接入范围字段信息
     * 
     * @param scopeFieldId 接入范围字段主键
     * @return 结果
     */
    public int deleteAccessScopeFieldByScopeFieldId(Long scopeFieldId);
}
