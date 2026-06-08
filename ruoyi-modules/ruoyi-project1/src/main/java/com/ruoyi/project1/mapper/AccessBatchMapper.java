package com.ruoyi.project1.mapper;

import java.util.List;
import com.ruoyi.project1.domain.AccessBatch;

/**
 * 接入执行批次Mapper接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface AccessBatchMapper 
{
    /**
     * 查询接入执行批次
     * 
     * @param accessBatchId 接入执行批次主键
     * @return 接入执行批次
     */
    public AccessBatch selectAccessBatchByAccessBatchId(Long accessBatchId);

    /**
     * 查询接入执行批次列表
     * 
     * @param accessBatch 接入执行批次
     * @return 接入执行批次集合
     */
    public List<AccessBatch> selectAccessBatchList(AccessBatch accessBatch);

    /**
     * 新增接入执行批次
     * 
     * @param accessBatch 接入执行批次
     * @return 结果
     */
    public int insertAccessBatch(AccessBatch accessBatch);

    /**
     * 修改接入执行批次
     * 
     * @param accessBatch 接入执行批次
     * @return 结果
     */
    public int updateAccessBatch(AccessBatch accessBatch);

    /**
     * 删除接入执行批次
     * 
     * @param accessBatchId 接入执行批次主键
     * @return 结果
     */
    public int deleteAccessBatchByAccessBatchId(Long accessBatchId);

    /**
     * 批量删除接入执行批次
     * 
     * @param accessBatchIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAccessBatchByAccessBatchIds(Long[] accessBatchIds);
}
