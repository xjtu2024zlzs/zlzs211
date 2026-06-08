package com.ruoyi.project1.service;

import java.util.List;
import com.ruoyi.project1.domain.AccessTransformRecord;

/**
 * 接入转换记录Service接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface IAccessTransformRecordService 
{
    /**
     * 查询接入转换记录
     * 
     * @param transformRecordId 接入转换记录主键
     * @return 接入转换记录
     */
    public AccessTransformRecord selectAccessTransformRecordByTransformRecordId(Long transformRecordId);

    /**
     * 查询接入转换记录列表
     * 
     * @param accessTransformRecord 接入转换记录
     * @return 接入转换记录集合
     */
    public List<AccessTransformRecord> selectAccessTransformRecordList(AccessTransformRecord accessTransformRecord);

    /**
     * 新增接入转换记录
     * 
     * @param accessTransformRecord 接入转换记录
     * @return 结果
     */
    public int insertAccessTransformRecord(AccessTransformRecord accessTransformRecord);

    /**
     * 修改接入转换记录
     * 
     * @param accessTransformRecord 接入转换记录
     * @return 结果
     */
    public int updateAccessTransformRecord(AccessTransformRecord accessTransformRecord);

    /**
     * 批量删除接入转换记录
     * 
     * @param transformRecordIds 需要删除的接入转换记录主键集合
     * @return 结果
     */
    public int deleteAccessTransformRecordByTransformRecordIds(Long[] transformRecordIds);

    /**
     * 删除接入转换记录信息
     * 
     * @param transformRecordId 接入转换记录主键
     * @return 结果
     */
    public int deleteAccessTransformRecordByTransformRecordId(Long transformRecordId);
}
