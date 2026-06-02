package com.ruoyi.topic5.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.topic5.mapper.T5DossierSampleRecordMapper;
import com.ruoyi.topic5.domain.T5DossierSampleRecord;
import com.ruoyi.topic5.service.IT5DossierSampleRecordService;

/**
 * 数字卷宗模拟数据Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@Service
public class T5DossierSampleRecordServiceImpl implements IT5DossierSampleRecordService 
{
    @Autowired
    private T5DossierSampleRecordMapper t5DossierSampleRecordMapper;

    /**
     * 查询数字卷宗模拟数据
     * 
     * @param recordId 数字卷宗模拟数据主键
     * @return 数字卷宗模拟数据
     */
    @Override
    public T5DossierSampleRecord selectT5DossierSampleRecordByRecordId(Long recordId)
    {
        return t5DossierSampleRecordMapper.selectT5DossierSampleRecordByRecordId(recordId);
    }

    /**
     * 查询数字卷宗模拟数据列表
     * 
     * @param t5DossierSampleRecord 数字卷宗模拟数据
     * @return 数字卷宗模拟数据
     */
    @Override
    public List<T5DossierSampleRecord> selectT5DossierSampleRecordList(T5DossierSampleRecord t5DossierSampleRecord)
    {
        return t5DossierSampleRecordMapper.selectT5DossierSampleRecordList(t5DossierSampleRecord);
    }

    /**
     * 新增数字卷宗模拟数据
     * 
     * @param t5DossierSampleRecord 数字卷宗模拟数据
     * @return 结果
     */
    @Override
    public int insertT5DossierSampleRecord(T5DossierSampleRecord t5DossierSampleRecord)
    {
        t5DossierSampleRecord.setCreateTime(DateUtils.getNowDate());
        return t5DossierSampleRecordMapper.insertT5DossierSampleRecord(t5DossierSampleRecord);
    }

    /**
     * 修改数字卷宗模拟数据
     * 
     * @param t5DossierSampleRecord 数字卷宗模拟数据
     * @return 结果
     */
    @Override
    public int updateT5DossierSampleRecord(T5DossierSampleRecord t5DossierSampleRecord)
    {
        t5DossierSampleRecord.setUpdateTime(DateUtils.getNowDate());
        return t5DossierSampleRecordMapper.updateT5DossierSampleRecord(t5DossierSampleRecord);
    }

    /**
     * 批量删除数字卷宗模拟数据
     * 
     * @param recordIds 需要删除的数字卷宗模拟数据主键
     * @return 结果
     */
    @Override
    public int deleteT5DossierSampleRecordByRecordIds(Long[] recordIds)
    {
        return t5DossierSampleRecordMapper.deleteT5DossierSampleRecordByRecordIds(recordIds);
    }

    /**
     * 删除数字卷宗模拟数据信息
     * 
     * @param recordId 数字卷宗模拟数据主键
     * @return 结果
     */
    @Override
    public int deleteT5DossierSampleRecordByRecordId(Long recordId)
    {
        return t5DossierSampleRecordMapper.deleteT5DossierSampleRecordByRecordId(recordId);
    }
}
