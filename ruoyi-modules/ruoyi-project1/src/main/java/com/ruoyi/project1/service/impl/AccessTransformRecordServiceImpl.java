package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.AccessTransformRecordMapper;
import com.ruoyi.project1.domain.AccessTransformRecord;
import com.ruoyi.project1.service.IAccessTransformRecordService;

/**
 * 接入转换记录Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class AccessTransformRecordServiceImpl implements IAccessTransformRecordService 
{
    @Autowired
    private AccessTransformRecordMapper accessTransformRecordMapper;

    /**
     * 查询接入转换记录
     * 
     * @param transformRecordId 接入转换记录主键
     * @return 接入转换记录
     */
    @Override
    public AccessTransformRecord selectAccessTransformRecordByTransformRecordId(Long transformRecordId)
    {
        return accessTransformRecordMapper.selectAccessTransformRecordByTransformRecordId(transformRecordId);
    }

    /**
     * 查询接入转换记录列表
     * 
     * @param accessTransformRecord 接入转换记录
     * @return 接入转换记录
     */
    @Override
    public List<AccessTransformRecord> selectAccessTransformRecordList(AccessTransformRecord accessTransformRecord)
    {
        return accessTransformRecordMapper.selectAccessTransformRecordList(accessTransformRecord);
    }

    /**
     * 新增接入转换记录
     * 
     * @param accessTransformRecord 接入转换记录
     * @return 结果
     */
    @Override
    public int insertAccessTransformRecord(AccessTransformRecord accessTransformRecord)
    {
        accessTransformRecord.setCreateTime(DateUtils.getNowDate());
        return accessTransformRecordMapper.insertAccessTransformRecord(accessTransformRecord);
    }

    /**
     * 修改接入转换记录
     * 
     * @param accessTransformRecord 接入转换记录
     * @return 结果
     */
    @Override
    public int updateAccessTransformRecord(AccessTransformRecord accessTransformRecord)
    {
        return accessTransformRecordMapper.updateAccessTransformRecord(accessTransformRecord);
    }

    /**
     * 批量删除接入转换记录
     * 
     * @param transformRecordIds 需要删除的接入转换记录主键
     * @return 结果
     */
    @Override
    public int deleteAccessTransformRecordByTransformRecordIds(Long[] transformRecordIds)
    {
        return accessTransformRecordMapper.deleteAccessTransformRecordByTransformRecordIds(transformRecordIds);
    }

    /**
     * 删除接入转换记录信息
     * 
     * @param transformRecordId 接入转换记录主键
     * @return 结果
     */
    @Override
    public int deleteAccessTransformRecordByTransformRecordId(Long transformRecordId)
    {
        return accessTransformRecordMapper.deleteAccessTransformRecordByTransformRecordId(transformRecordId);
    }
}
