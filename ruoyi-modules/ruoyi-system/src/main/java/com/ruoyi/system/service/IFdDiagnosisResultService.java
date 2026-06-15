package com.ruoyi.system.service;

import com.ruoyi.system.domain.FdDiagnosisResult;

import java.util.List;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.system.domain.dto.DiagnosisRunDto;
/**
 * 故障诊断-诊断结果Service接口
 *
 * @author ruoyi
 * @date 2026-05-29
 */
public interface IFdDiagnosisResultService
{
    AjaxResult runDiagnosis(DiagnosisRunDto dto);
    /**
     * 查询故障诊断-诊断结果
     *
     * @param diagnosisId 诊断ID
     * @return 故障诊断-诊断结果
     */
    FdDiagnosisResult selectFdDiagnosisResultByDiagnosisId(Long diagnosisId);

    /**
     * 查询故障诊断-诊断结果列表
     *
     * @param fdDiagnosisResult 故障诊断-诊断结果
     * @return 故障诊断-诊断结果集合
     */
    List<FdDiagnosisResult> selectFdDiagnosisResultList(FdDiagnosisResult fdDiagnosisResult);

    /**
     * 新增故障诊断-诊断结果
     *
     * @param fdDiagnosisResult 故障诊断-诊断结果
     * @return 结果
     */
    int insertFdDiagnosisResult(FdDiagnosisResult fdDiagnosisResult);

    /**
     * 修改故障诊断-诊断结果
     *
     * @param fdDiagnosisResult 故障诊断-诊断结果
     * @return 结果
     */
    int updateFdDiagnosisResult(FdDiagnosisResult fdDiagnosisResult);

    /**
     * 批量删除故障诊断-诊断结果
     *
     * @param diagnosisIds 需要删除的诊断ID
     * @return 结果
     */
    int deleteFdDiagnosisResultByDiagnosisIds(Long[] diagnosisIds);

    /**
     * 删除故障诊断-诊断结果信息
     *
     * @param diagnosisId 诊断ID
     * @return 结果
     */
    int deleteFdDiagnosisResultByDiagnosisId(Long diagnosisId);
}