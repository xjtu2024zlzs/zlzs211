package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.FdDiagnosisResult;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 故障诊断-诊断结果Mapper接口
 */
@Mapper
public interface FdDiagnosisResultMapper
{
    /**
     * 查询故障诊断-诊断结果
     */
    FdDiagnosisResult selectFdDiagnosisResultByDiagnosisId(Long diagnosisId);

    /**
     * 查询故障诊断-诊断结果列表
     */
    List<FdDiagnosisResult> selectFdDiagnosisResultList(FdDiagnosisResult fdDiagnosisResult);

    /**
     * 新增故障诊断-诊断结果
     */
    int insertFdDiagnosisResult(FdDiagnosisResult fdDiagnosisResult);

    /**
     * 修改故障诊断-诊断结果
     */
    int updateFdDiagnosisResult(FdDiagnosisResult fdDiagnosisResult);

    /**
     * 删除故障诊断-诊断结果
     */
    int deleteFdDiagnosisResultByDiagnosisId(Long diagnosisId);

    /**
     * 批量删除故障诊断-诊断结果
     */
    int deleteFdDiagnosisResultByDiagnosisIds(Long[] diagnosisIds);
}