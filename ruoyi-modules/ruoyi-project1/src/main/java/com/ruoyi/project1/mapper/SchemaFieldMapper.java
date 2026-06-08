package com.ruoyi.project1.mapper;

import java.util.List;
import com.ruoyi.project1.domain.SchemaField;

/**
 * 模式字段Mapper接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface SchemaFieldMapper 
{
    /**
     * 查询模式字段
     * 
     * @param fieldId 模式字段主键
     * @return 模式字段
     */
    public SchemaField selectSchemaFieldByFieldId(Long fieldId);

    /**
     * 查询模式字段列表
     * 
     * @param schemaField 模式字段
     * @return 模式字段集合
     */
    public List<SchemaField> selectSchemaFieldList(SchemaField schemaField);

    /**
     * 新增模式字段
     * 
     * @param schemaField 模式字段
     * @return 结果
     */
    public int insertSchemaField(SchemaField schemaField);

    /**
     * 修改模式字段
     * 
     * @param schemaField 模式字段
     * @return 结果
     */
    public int updateSchemaField(SchemaField schemaField);

    /**
     * 删除模式字段
     * 
     * @param fieldId 模式字段主键
     * @return 结果
     */
    public int deleteSchemaFieldByFieldId(Long fieldId);

    /**
     * 批量删除模式字段
     * 
     * @param fieldIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSchemaFieldByFieldIds(Long[] fieldIds);
}
