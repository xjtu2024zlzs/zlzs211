package com.ruoyi.project1.mapper;

import java.util.List;
import com.ruoyi.project1.domain.SchemaEntity;

/**
 * 模式实体Mapper接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface SchemaEntityMapper 
{
    /**
     * 查询模式实体
     * 
     * @param entityId 模式实体主键
     * @return 模式实体
     */
    public SchemaEntity selectSchemaEntityByEntityId(Long entityId);

    /**
     * 查询模式实体列表
     * 
     * @param schemaEntity 模式实体
     * @return 模式实体集合
     */
    public List<SchemaEntity> selectSchemaEntityList(SchemaEntity schemaEntity);

    /**
     * 新增模式实体
     * 
     * @param schemaEntity 模式实体
     * @return 结果
     */
    public int insertSchemaEntity(SchemaEntity schemaEntity);

    /**
     * 修改模式实体
     * 
     * @param schemaEntity 模式实体
     * @return 结果
     */
    public int updateSchemaEntity(SchemaEntity schemaEntity);

    /**
     * 删除模式实体
     * 
     * @param entityId 模式实体主键
     * @return 结果
     */
    public int deleteSchemaEntityByEntityId(Long entityId);

    /**
     * 批量删除模式实体
     * 
     * @param entityIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSchemaEntityByEntityIds(Long[] entityIds);
}
