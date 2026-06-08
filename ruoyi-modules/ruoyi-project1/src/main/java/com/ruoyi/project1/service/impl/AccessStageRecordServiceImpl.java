package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.AccessStageRecordMapper;
import com.ruoyi.project1.domain.AccessStageRecord;
import com.ruoyi.project1.service.IAccessStageRecordService;

/**
 * 接入中间库原始记录Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class AccessStageRecordServiceImpl implements IAccessStageRecordService 
{
    @Autowired
    private AccessStageRecordMapper accessStageRecordMapper;

    /**
     * 查询接入中间库原始记录
     * 
     * @param stageRecordId 接入中间库原始记录主键
     * @return 接入中间库原始记录
     */
    @Override
    public AccessStageRecord selectAccessStageRecordByStageRecordId(Long stageRecordId)
    {
        return accessStageRecordMapper.selectAccessStageRecordByStageRecordId(stageRecordId);
    }

    /**
     * 查询接入中间库原始记录列表
     * 
     * @param accessStageRecord 接入中间库原始记录
     * @return 接入中间库原始记录
     */
    @Override
    public List<AccessStageRecord> selectAccessStageRecordList(AccessStageRecord accessStageRecord)
    {
        return accessStageRecordMapper.selectAccessStageRecordList(accessStageRecord);
    }

    /**
     * 新增接入中间库原始记录
     * 
     * @param accessStageRecord 接入中间库原始记录
     * @return 结果
     */
    @Override
    public int insertAccessStageRecord(AccessStageRecord accessStageRecord)
    {
        accessStageRecord.setCreateTime(DateUtils.getNowDate());
        return accessStageRecordMapper.insertAccessStageRecord(accessStageRecord);
    }

    /**
     * 修改接入中间库原始记录
     * 
     * @param accessStageRecord 接入中间库原始记录
     * @return 结果
     */
    @Override
    public int updateAccessStageRecord(AccessStageRecord accessStageRecord)
    {
        return accessStageRecordMapper.updateAccessStageRecord(accessStageRecord);
    }

    /**
     * 批量删除接入中间库原始记录
     * 
     * @param stageRecordIds 需要删除的接入中间库原始记录主键
     * @return 结果
     */
    @Override
    public int deleteAccessStageRecordByStageRecordIds(Long[] stageRecordIds)
    {
        return accessStageRecordMapper.deleteAccessStageRecordByStageRecordIds(stageRecordIds);
    }

    /**
     * 删除接入中间库原始记录信息
     * 
     * @param stageRecordId 接入中间库原始记录主键
     * @return 结果
     */
    @Override
    public int deleteAccessStageRecordByStageRecordId(Long stageRecordId)
    {
        return accessStageRecordMapper.deleteAccessStageRecordByStageRecordId(stageRecordId);
    }
}
