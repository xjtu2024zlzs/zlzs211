package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.AccessBatchMapper;
import com.ruoyi.project1.domain.AccessBatch;
import com.ruoyi.project1.service.IAccessBatchService;

/**
 * 接入执行批次Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class AccessBatchServiceImpl implements IAccessBatchService 
{
    @Autowired
    private AccessBatchMapper accessBatchMapper;

    /**
     * 查询接入执行批次
     * 
     * @param accessBatchId 接入执行批次主键
     * @return 接入执行批次
     */
    @Override
    public AccessBatch selectAccessBatchByAccessBatchId(Long accessBatchId)
    {
        return accessBatchMapper.selectAccessBatchByAccessBatchId(accessBatchId);
    }

    /**
     * 查询接入执行批次列表
     * 
     * @param accessBatch 接入执行批次
     * @return 接入执行批次
     */
    @Override
    public List<AccessBatch> selectAccessBatchList(AccessBatch accessBatch)
    {
        return accessBatchMapper.selectAccessBatchList(accessBatch);
    }

    /**
     * 新增接入执行批次
     * 
     * @param accessBatch 接入执行批次
     * @return 结果
     */
    @Override
    public int insertAccessBatch(AccessBatch accessBatch)
    {
        accessBatch.setCreateTime(DateUtils.getNowDate());
        return accessBatchMapper.insertAccessBatch(accessBatch);
    }

    /**
     * 修改接入执行批次
     * 
     * @param accessBatch 接入执行批次
     * @return 结果
     */
    @Override
    public int updateAccessBatch(AccessBatch accessBatch)
    {
        accessBatch.setUpdateTime(DateUtils.getNowDate());
        return accessBatchMapper.updateAccessBatch(accessBatch);
    }

    /**
     * 批量删除接入执行批次
     * 
     * @param accessBatchIds 需要删除的接入执行批次主键
     * @return 结果
     */
    @Override
    public int deleteAccessBatchByAccessBatchIds(Long[] accessBatchIds)
    {
        return accessBatchMapper.deleteAccessBatchByAccessBatchIds(accessBatchIds);
    }

    /**
     * 删除接入执行批次信息
     * 
     * @param accessBatchId 接入执行批次主键
     * @return 结果
     */
    @Override
    public int deleteAccessBatchByAccessBatchId(Long accessBatchId)
    {
        return accessBatchMapper.deleteAccessBatchByAccessBatchId(accessBatchId);
    }
}
