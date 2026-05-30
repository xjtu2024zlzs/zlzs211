package com.ruoyi.topic5.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.topic5.mapper.T5FaultSymptomMapper;
import com.ruoyi.topic5.domain.T5FaultSymptom;
import com.ruoyi.topic5.service.IT5FaultSymptomService;

/**
 * 课题五-故障征输入Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@Service
public class T5FaultSymptomServiceImpl implements IT5FaultSymptomService 
{
    @Autowired
    private T5FaultSymptomMapper t5FaultSymptomMapper;

    /**
     * 查询课题五-故障征输入
     * 
     * @param symptomId 课题五-故障征输入主键
     * @return 课题五-故障征输入
     */
    @Override
    public T5FaultSymptom selectT5FaultSymptomBySymptomId(Long symptomId)
    {
        return t5FaultSymptomMapper.selectT5FaultSymptomBySymptomId(symptomId);
    }

    /**
     * 查询课题五-故障征输入列表
     * 
     * @param t5FaultSymptom 课题五-故障征输入
     * @return 课题五-故障征输入
     */
    @Override
    public List<T5FaultSymptom> selectT5FaultSymptomList(T5FaultSymptom t5FaultSymptom)
    {
        return t5FaultSymptomMapper.selectT5FaultSymptomList(t5FaultSymptom);
    }

    /**
     * 新增课题五-故障征输入
     * 
     * @param t5FaultSymptom 课题五-故障征输入
     * @return 结果
     */
    @Override
    public int insertT5FaultSymptom(T5FaultSymptom t5FaultSymptom)
    {
        t5FaultSymptom.setCreateTime(DateUtils.getNowDate());
        return t5FaultSymptomMapper.insertT5FaultSymptom(t5FaultSymptom);
    }

    /**
     * 修改课题五-故障征输入
     * 
     * @param t5FaultSymptom 课题五-故障征输入
     * @return 结果
     */
    @Override
    public int updateT5FaultSymptom(T5FaultSymptom t5FaultSymptom)
    {
        t5FaultSymptom.setUpdateTime(DateUtils.getNowDate());
        return t5FaultSymptomMapper.updateT5FaultSymptom(t5FaultSymptom);
    }

    /**
     * 批量删除课题五-故障征输入
     * 
     * @param symptomIds 需要删除的课题五-故障征输入主键
     * @return 结果
     */
    @Override
    public int deleteT5FaultSymptomBySymptomIds(Long[] symptomIds)
    {
        return t5FaultSymptomMapper.deleteT5FaultSymptomBySymptomIds(symptomIds);
    }

    /**
     * 删除课题五-故障征输入信息
     * 
     * @param symptomId 课题五-故障征输入主键
     * @return 结果
     */
    @Override
    public int deleteT5FaultSymptomBySymptomId(Long symptomId)
    {
        return t5FaultSymptomMapper.deleteT5FaultSymptomBySymptomId(symptomId);
    }
}
