package com.ruoyi.project1.service;

import java.util.List;
import com.ruoyi.project1.domain.SchemaSnapshot;

/**
 * 数据源模式快照Service接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface ISchemaSnapshotService 
{
    /**
     * 查询数据源模式快照
     * 
     * @param snapshotId 数据源模式快照主键
     * @return 数据源模式快照
     */
    public SchemaSnapshot selectSchemaSnapshotBySnapshotId(Long snapshotId);

    /**
     * 查询数据源模式快照列表
     * 
     * @param schemaSnapshot 数据源模式快照
     * @return 数据源模式快照集合
     */
    public List<SchemaSnapshot> selectSchemaSnapshotList(SchemaSnapshot schemaSnapshot);

    /**
     * 新增数据源模式快照
     * 
     * @param schemaSnapshot 数据源模式快照
     * @return 结果
     */
    public int insertSchemaSnapshot(SchemaSnapshot schemaSnapshot);

    /**
     * 修改数据源模式快照
     * 
     * @param schemaSnapshot 数据源模式快照
     * @return 结果
     */
    public int updateSchemaSnapshot(SchemaSnapshot schemaSnapshot);

    /**
     * 批量删除数据源模式快照
     * 
     * @param snapshotIds 需要删除的数据源模式快照主键集合
     * @return 结果
     */
    public int deleteSchemaSnapshotBySnapshotIds(Long[] snapshotIds);

    /**
     * 删除数据源模式快照信息
     * 
     * @param snapshotId 数据源模式快照主键
     * @return 结果
     */
    public int deleteSchemaSnapshotBySnapshotId(Long snapshotId);
}
