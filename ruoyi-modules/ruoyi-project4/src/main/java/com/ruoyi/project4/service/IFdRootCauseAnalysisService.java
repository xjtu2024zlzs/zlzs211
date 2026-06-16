package com.ruoyi.project4.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.project4.domain.FdRootCauseAnalysis;

/**
 * 故障诊断-根因分析结果Service接口
 *
 * @author ruoyi
 * @date 2026-05-29
 */
public interface IFdRootCauseAnalysisService
{
    /**
     * 查询故障诊断-根因分析结果
     *
     * @param analysisId 根因分析ID
     * @return 故障诊断-根因分析结果
     */
    public FdRootCauseAnalysis selectFdRootCauseAnalysisByAnalysisId(Long analysisId);

    /**
     * 查询故障诊断-根因分析结果列表
     *
     * @param fdRootCauseAnalysis 故障诊断-根因分析结果
     * @return 故障诊断-根因分析结果集合
     */
    public List<FdRootCauseAnalysis> selectFdRootCauseAnalysisList(FdRootCauseAnalysis fdRootCauseAnalysis);
    /**
     * 查询根因分析置信度统计
     *
     * @param fdRootCauseAnalysis 根因分析查询条件
     * @return 根因分析置信度统计结果
     */
    public Map<String, Object> selectConfidenceStats(FdRootCauseAnalysis fdRootCauseAnalysis);
    /**
     * 新增故障诊断-根因分析结果
     *
     * @param fdRootCauseAnalysis 故障诊断-根因分析结果
     * @return 结果
     */
    public int insertFdRootCauseAnalysis(FdRootCauseAnalysis fdRootCauseAnalysis);

    /**
     * 修改故障诊断-根因分析结果
     *
     * @param fdRootCauseAnalysis 故障诊断-根因分析结果
     * @return 结果
     */
    public int updateFdRootCauseAnalysis(FdRootCauseAnalysis fdRootCauseAnalysis);

    /**
     * 批量删除故障诊断-根因分析结果
     *
     * @param analysisIds 需要删除的根因分析ID集合
     * @return 结果
     */
    public int deleteFdRootCauseAnalysisByAnalysisIds(Long[] analysisIds);

    /**
     * 删除故障诊断-根因分析结果信息
     *
     * @param analysisId 根因分析ID
     * @return 结果
     */
    public int deleteFdRootCauseAnalysisByAnalysisId(Long analysisId);
}
