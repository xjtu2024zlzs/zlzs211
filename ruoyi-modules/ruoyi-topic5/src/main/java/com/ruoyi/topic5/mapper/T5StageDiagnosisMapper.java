package com.ruoyi.topic5.mapper;

import java.util.List;
import com.ruoyi.topic5.domain.T5StageDiagnosis;

/**
 * 课题五-故障阶段诊断结果Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface T5StageDiagnosisMapper 
{
    /**
     * 查询课题五-故障阶段诊断结果
     * 
     * @param diagnosisId 课题五-故障阶段诊断结果主键
     * @return 课题五-故障阶段诊断结果
     */
    public T5StageDiagnosis selectT5StageDiagnosisByDiagnosisId(Long diagnosisId);

    /**
     * 查询课题五-故障阶段诊断结果列表
     * 
     * @param t5StageDiagnosis 课题五-故障阶段诊断结果
     * @return 课题五-故障阶段诊断结果集合
     */
    public List<T5StageDiagnosis> selectT5StageDiagnosisList(T5StageDiagnosis t5StageDiagnosis);

    /**
     * 新增课题五-故障阶段诊断结果
     * 
     * @param t5StageDiagnosis 课题五-故障阶段诊断结果
     * @return 结果
     */
    public int insertT5StageDiagnosis(T5StageDiagnosis t5StageDiagnosis);

    /**
     * 修改课题五-故障阶段诊断结果
     * 
     * @param t5StageDiagnosis 课题五-故障阶段诊断结果
     * @return 结果
     */
    public int updateT5StageDiagnosis(T5StageDiagnosis t5StageDiagnosis);

    /**
     * 删除课题五-故障阶段诊断结果
     * 
     * @param diagnosisId 课题五-故障阶段诊断结果主键
     * @return 结果
     */
    public int deleteT5StageDiagnosisByDiagnosisId(Long diagnosisId);

    /**
     * 批量删除课题五-故障阶段诊断结果
     * 
     * @param diagnosisIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteT5StageDiagnosisByDiagnosisIds(Long[] diagnosisIds);
}
