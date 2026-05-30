package com.ruoyi.topic5.service;

import java.util.List;
import com.ruoyi.topic5.domain.T5FaultSymptom;

/**
 * 课题五-故障征输入Service接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface IT5FaultSymptomService 
{
    /**
     * 查询课题五-故障征输入
     * 
     * @param symptomId 课题五-故障征输入主键
     * @return 课题五-故障征输入
     */
    public T5FaultSymptom selectT5FaultSymptomBySymptomId(Long symptomId);

    /**
     * 查询课题五-故障征输入列表
     * 
     * @param t5FaultSymptom 课题五-故障征输入
     * @return 课题五-故障征输入集合
     */
    public List<T5FaultSymptom> selectT5FaultSymptomList(T5FaultSymptom t5FaultSymptom);

    /**
     * 新增课题五-故障征输入
     * 
     * @param t5FaultSymptom 课题五-故障征输入
     * @return 结果
     */
    public int insertT5FaultSymptom(T5FaultSymptom t5FaultSymptom);

    /**
     * 修改课题五-故障征输入
     * 
     * @param t5FaultSymptom 课题五-故障征输入
     * @return 结果
     */
    public int updateT5FaultSymptom(T5FaultSymptom t5FaultSymptom);

    /**
     * 批量删除课题五-故障征输入
     * 
     * @param symptomIds 需要删除的课题五-故障征输入主键集合
     * @return 结果
     */
    public int deleteT5FaultSymptomBySymptomIds(Long[] symptomIds);

    /**
     * 删除课题五-故障征输入信息
     * 
     * @param symptomId 课题五-故障征输入主键
     * @return 结果
     */
    public int deleteT5FaultSymptomBySymptomId(Long symptomId);
}
