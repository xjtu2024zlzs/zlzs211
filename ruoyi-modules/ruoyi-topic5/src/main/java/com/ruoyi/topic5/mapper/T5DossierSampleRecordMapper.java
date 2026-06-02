package com.ruoyi.topic5.mapper;

import java.util.List;
import com.ruoyi.topic5.domain.T5DossierSampleRecord;

/**
 * 数字卷宗模拟数据Mapper接口
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public interface T5DossierSampleRecordMapper 
{
    /**
     * 查询数字卷宗模拟数据
     * 
     * @param recordId 数字卷宗模拟数据主键
     * @return 数字卷宗模拟数据
     */
    public T5DossierSampleRecord selectT5DossierSampleRecordByRecordId(Long recordId);

    /**
     * 查询数字卷宗模拟数据列表
     * 
     * @param t5DossierSampleRecord 数字卷宗模拟数据
     * @return 数字卷宗模拟数据集合
     */
    public List<T5DossierSampleRecord> selectT5DossierSampleRecordList(T5DossierSampleRecord t5DossierSampleRecord);

    /**
     * 新增数字卷宗模拟数据
     * 
     * @param t5DossierSampleRecord 数字卷宗模拟数据
     * @return 结果
     */
    public int insertT5DossierSampleRecord(T5DossierSampleRecord t5DossierSampleRecord);

    /**
     * 修改数字卷宗模拟数据
     * 
     * @param t5DossierSampleRecord 数字卷宗模拟数据
     * @return 结果
     */
    public int updateT5DossierSampleRecord(T5DossierSampleRecord t5DossierSampleRecord);

    /**
     * 删除数字卷宗模拟数据
     * 
     * @param recordId 数字卷宗模拟数据主键
     * @return 结果
     */
    public int deleteT5DossierSampleRecordByRecordId(Long recordId);

    /**
     * 批量删除数字卷宗模拟数据
     * 
     * @param recordIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteT5DossierSampleRecordByRecordIds(Long[] recordIds);
}
