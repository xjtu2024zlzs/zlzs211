package com.ruoyi.system.mapper;
import java.util.Map;
import java.util.List;
import com.ruoyi.system.domain.FdRootCauseAnalysis;
import org.apache.ibatis.annotations.Mapper;
/**
 * 故障诊断-根因分析结果Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-29
 */
@Mapper
public interface FdRootCauseAnalysisMapper
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
     * 删除故障诊断-根因分析结果
     *
     * @param analysisId 根因分析ID
     * @return 结果
     */
    public int deleteFdRootCauseAnalysisByAnalysisId(Long analysisId);

    /**
     * 批量删除故障诊断-根因分析结果
     *
     * @param analysisIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFdRootCauseAnalysisByAnalysisIds(Long[] analysisIds);
}