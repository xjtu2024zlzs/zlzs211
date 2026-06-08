package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.SchemaSnapshotMapper;
import com.ruoyi.project1.domain.SchemaSnapshot;
import com.ruoyi.project1.service.ISchemaSnapshotService;

/**
 * 数据源模式快照Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class SchemaSnapshotServiceImpl implements ISchemaSnapshotService 
{
    @Autowired
    private SchemaSnapshotMapper schemaSnapshotMapper;

    /**
     * 查询数据源模式快照
     * 
     * @param snapshotId 数据源模式快照主键
     * @return 数据源模式快照
     */
    @Override
    public SchemaSnapshot selectSchemaSnapshotBySnapshotId(Long snapshotId)
    {
        return schemaSnapshotMapper.selectSchemaSnapshotBySnapshotId(snapshotId);
    }

    /**
     * 查询数据源模式快照列表
     * 
     * @param schemaSnapshot 数据源模式快照
     * @return 数据源模式快照
     */
    @Override
    public List<SchemaSnapshot> selectSchemaSnapshotList(SchemaSnapshot schemaSnapshot)
    {
        return schemaSnapshotMapper.selectSchemaSnapshotList(schemaSnapshot);
    }

    /**
     * 新增数据源模式快照
     * 
     * @param schemaSnapshot 数据源模式快照
     * @return 结果
     */
    @Override
    public int insertSchemaSnapshot(SchemaSnapshot schemaSnapshot)
    {
        schemaSnapshot.setCreateTime(DateUtils.getNowDate());
        return schemaSnapshotMapper.insertSchemaSnapshot(schemaSnapshot);
    }

    /**
     * 修改数据源模式快照
     * 
     * @param schemaSnapshot 数据源模式快照
     * @return 结果
     */
    @Override
    public int updateSchemaSnapshot(SchemaSnapshot schemaSnapshot)
    {
        return schemaSnapshotMapper.updateSchemaSnapshot(schemaSnapshot);
    }

    /**
     * 批量删除数据源模式快照
     * 
     * @param snapshotIds 需要删除的数据源模式快照主键
     * @return 结果
     */
    @Override
    public int deleteSchemaSnapshotBySnapshotIds(Long[] snapshotIds)
    {
        return schemaSnapshotMapper.deleteSchemaSnapshotBySnapshotIds(snapshotIds);
    }

    /**
     * 删除数据源模式快照信息
     * 
     * @param snapshotId 数据源模式快照主键
     * @return 结果
     */
    @Override
    public int deleteSchemaSnapshotBySnapshotId(Long snapshotId)
    {
        return schemaSnapshotMapper.deleteSchemaSnapshotBySnapshotId(snapshotId);
    }
}
