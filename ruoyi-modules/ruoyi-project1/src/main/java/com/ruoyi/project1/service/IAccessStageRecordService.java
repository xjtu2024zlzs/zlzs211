package com.ruoyi.project1.service;

import java.util.List;
import com.ruoyi.project1.domain.AccessStageRecord;

/**
 * 接入中间库原始记录Service接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface IAccessStageRecordService 
{
    /**
     * 查询接入中间库原始记录
     * 
     * @param stageRecordId 接入中间库原始记录主键
     * @return 接入中间库原始记录
     */
    public AccessStageRecord selectAccessStageRecordByStageRecordId(Long stageRecordId);

    /**
     * 查询接入中间库原始记录列表
     * 
     * @param accessStageRecord 接入中间库原始记录
     * @return 接入中间库原始记录集合
     */
    public List<AccessStageRecord> selectAccessStageRecordList(AccessStageRecord accessStageRecord);

    /**
     * 新增接入中间库原始记录
     * 
     * @param accessStageRecord 接入中间库原始记录
     * @return 结果
     */
    public int insertAccessStageRecord(AccessStageRecord accessStageRecord);

    /**
     * 修改接入中间库原始记录
     * 
     * @param accessStageRecord 接入中间库原始记录
     * @return 结果
     */
    public int updateAccessStageRecord(AccessStageRecord accessStageRecord);

    /**
     * 批量删除接入中间库原始记录
     * 
     * @param stageRecordIds 需要删除的接入中间库原始记录主键集合
     * @return 结果
     */
    public int deleteAccessStageRecordByStageRecordIds(Long[] stageRecordIds);

    /**
     * 删除接入中间库原始记录信息
     * 
     * @param stageRecordId 接入中间库原始记录主键
     * @return 结果
     */
    public int deleteAccessStageRecordByStageRecordId(Long stageRecordId);
}
