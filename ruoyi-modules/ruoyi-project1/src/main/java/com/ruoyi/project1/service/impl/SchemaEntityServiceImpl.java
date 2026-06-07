package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.SchemaEntityMapper;
import com.ruoyi.project1.domain.SchemaEntity;
import com.ruoyi.project1.service.ISchemaEntityService;

/**
 * 模式实体Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class SchemaEntityServiceImpl implements ISchemaEntityService 
{
    @Autowired
    private SchemaEntityMapper schemaEntityMapper;

    /**
     * 查询模式实体
     * 
     * @param entityId 模式实体主键
     * @return 模式实体
     */
    @Override
    public SchemaEntity selectSchemaEntityByEntityId(Long entityId)
    {
        return schemaEntityMapper.selectSchemaEntityByEntityId(entityId);
    }

    /**
     * 查询模式实体列表
     * 
     * @param schemaEntity 模式实体
     * @return 模式实体
     */
    @Override
    public List<SchemaEntity> selectSchemaEntityList(SchemaEntity schemaEntity)
    {
        return schemaEntityMapper.selectSchemaEntityList(schemaEntity);
    }

    /**
     * 新增模式实体
     * 
     * @param schemaEntity 模式实体
     * @return 结果
     */
    @Override
    public int insertSchemaEntity(SchemaEntity schemaEntity)
    {
        schemaEntity.setCreateTime(DateUtils.getNowDate());
        return schemaEntityMapper.insertSchemaEntity(schemaEntity);
    }

    /**
     * 修改模式实体
     * 
     * @param schemaEntity 模式实体
     * @return 结果
     */
    @Override
    public int updateSchemaEntity(SchemaEntity schemaEntity)
    {
        return schemaEntityMapper.updateSchemaEntity(schemaEntity);
    }

    /**
     * 批量删除模式实体
     * 
     * @param entityIds 需要删除的模式实体主键
     * @return 结果
     */
    @Override
    public int deleteSchemaEntityByEntityIds(Long[] entityIds)
    {
        return schemaEntityMapper.deleteSchemaEntityByEntityIds(entityIds);
    }

    /**
     * 删除模式实体信息
     * 
     * @param entityId 模式实体主键
     * @return 结果
     */
    @Override
    public int deleteSchemaEntityByEntityId(Long entityId)
    {
        return schemaEntityMapper.deleteSchemaEntityByEntityId(entityId);
    }
}
