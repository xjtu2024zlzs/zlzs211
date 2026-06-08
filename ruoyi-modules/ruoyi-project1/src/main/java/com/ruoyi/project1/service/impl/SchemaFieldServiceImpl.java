package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.SchemaFieldMapper;
import com.ruoyi.project1.domain.SchemaField;
import com.ruoyi.project1.service.ISchemaFieldService;

/**
 * 模式字段Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class SchemaFieldServiceImpl implements ISchemaFieldService 
{
    @Autowired
    private SchemaFieldMapper schemaFieldMapper;

    /**
     * 查询模式字段
     * 
     * @param fieldId 模式字段主键
     * @return 模式字段
     */
    @Override
    public SchemaField selectSchemaFieldByFieldId(Long fieldId)
    {
        return schemaFieldMapper.selectSchemaFieldByFieldId(fieldId);
    }

    /**
     * 查询模式字段列表
     * 
     * @param schemaField 模式字段
     * @return 模式字段
     */
    @Override
    public List<SchemaField> selectSchemaFieldList(SchemaField schemaField)
    {
        return schemaFieldMapper.selectSchemaFieldList(schemaField);
    }

    /**
     * 新增模式字段
     * 
     * @param schemaField 模式字段
     * @return 结果
     */
    @Override
    public int insertSchemaField(SchemaField schemaField)
    {
        schemaField.setCreateTime(DateUtils.getNowDate());
        return schemaFieldMapper.insertSchemaField(schemaField);
    }

    /**
     * 修改模式字段
     * 
     * @param schemaField 模式字段
     * @return 结果
     */
    @Override
    public int updateSchemaField(SchemaField schemaField)
    {
        return schemaFieldMapper.updateSchemaField(schemaField);
    }

    /**
     * 批量删除模式字段
     * 
     * @param fieldIds 需要删除的模式字段主键
     * @return 结果
     */
    @Override
    public int deleteSchemaFieldByFieldIds(Long[] fieldIds)
    {
        return schemaFieldMapper.deleteSchemaFieldByFieldIds(fieldIds);
    }

    /**
     * 删除模式字段信息
     * 
     * @param fieldId 模式字段主键
     * @return 结果
     */
    @Override
    public int deleteSchemaFieldByFieldId(Long fieldId)
    {
        return schemaFieldMapper.deleteSchemaFieldByFieldId(fieldId);
    }
}
