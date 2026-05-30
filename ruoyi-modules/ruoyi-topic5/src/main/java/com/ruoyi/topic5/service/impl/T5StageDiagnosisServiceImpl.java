package com.ruoyi.topic5.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.topic5.mapper.T5StageDiagnosisMapper;
import com.ruoyi.topic5.domain.T5StageDiagnosis;
import com.ruoyi.topic5.service.IT5StageDiagnosisService;

/**
 * 课题五-故障阶段诊断结果Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@Service
public class T5StageDiagnosisServiceImpl implements IT5StageDiagnosisService 
{
    @Autowired
    private T5StageDiagnosisMapper t5StageDiagnosisMapper;

    /**
     * 查询课题五-故障阶段诊断结果
     * 
     * @param diagnosisId 课题五-故障阶段诊断结果主键
     * @return 课题五-故障阶段诊断结果
     */
    @Override
    public T5StageDiagnosis selectT5StageDiagnosisByDiagnosisId(Long diagnosisId)
    {
        return t5StageDiagnosisMapper.selectT5StageDiagnosisByDiagnosisId(diagnosisId);
    }

    /**
     * 查询课题五-故障阶段诊断结果列表
     * 
     * @param t5StageDiagnosis 课题五-故障阶段诊断结果
     * @return 课题五-故障阶段诊断结果
     */
    @Override
    public List<T5StageDiagnosis> selectT5StageDiagnosisList(T5StageDiagnosis t5StageDiagnosis)
    {
        return t5StageDiagnosisMapper.selectT5StageDiagnosisList(t5StageDiagnosis);
    }

    /**
     * 新增课题五-故障阶段诊断结果
     * 
     * @param t5StageDiagnosis 课题五-故障阶段诊断结果
     * @return 结果
     */
    @Override
    public int insertT5StageDiagnosis(T5StageDiagnosis t5StageDiagnosis)
    {
        t5StageDiagnosis.setCreateTime(DateUtils.getNowDate());
        return t5StageDiagnosisMapper.insertT5StageDiagnosis(t5StageDiagnosis);
    }

    /**
     * 修改课题五-故障阶段诊断结果
     * 
     * @param t5StageDiagnosis 课题五-故障阶段诊断结果
     * @return 结果
     */
    @Override
    public int updateT5StageDiagnosis(T5StageDiagnosis t5StageDiagnosis)
    {
        t5StageDiagnosis.setUpdateTime(DateUtils.getNowDate());
        return t5StageDiagnosisMapper.updateT5StageDiagnosis(t5StageDiagnosis);
    }

    /**
     * 批量删除课题五-故障阶段诊断结果
     * 
     * @param diagnosisIds 需要删除的课题五-故障阶段诊断结果主键
     * @return 结果
     */
    @Override
    public int deleteT5StageDiagnosisByDiagnosisIds(Long[] diagnosisIds)
    {
        return t5StageDiagnosisMapper.deleteT5StageDiagnosisByDiagnosisIds(diagnosisIds);
    }

    /**
     * 删除课题五-故障阶段诊断结果信息
     * 
     * @param diagnosisId 课题五-故障阶段诊断结果主键
     * @return 结果
     */
    @Override
    public int deleteT5StageDiagnosisByDiagnosisId(Long diagnosisId)
    {
        return t5StageDiagnosisMapper.deleteT5StageDiagnosisByDiagnosisId(diagnosisId);
    }
}
