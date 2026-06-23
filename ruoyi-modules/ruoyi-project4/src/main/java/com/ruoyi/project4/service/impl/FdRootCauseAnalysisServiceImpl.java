package com.ruoyi.project4.service.impl;

import java.util.List;
import java.util.Map;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.project4.domain.FdRootCauseAnalysis;
import com.ruoyi.project4.mapper.FdRootCauseAnalysisMapper;
import com.ruoyi.project4.service.IFdRootCauseAnalysisService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 故障诊断-根因分析结果Service业务层处理
 *
 * @author ruoyi
 * @date 2026-05-29
 */
@Service
public class FdRootCauseAnalysisServiceImpl implements IFdRootCauseAnalysisService
{
    @Autowired
    private FdRootCauseAnalysisMapper fdRootCauseAnalysisMapper;

    /**
     * 查询故障诊断-根因分析结果
     *
     * @param analysisId 根因分析ID
     * @return 故障诊断-根因分析结果
     */
    @Override
    public FdRootCauseAnalysis selectFdRootCauseAnalysisByAnalysisId(Long analysisId)
    {
        return fdRootCauseAnalysisMapper.selectFdRootCauseAnalysisByAnalysisId(analysisId);
    }

    /**
     * 查询故障诊断-根因分析结果列表
     *
     * @param fdRootCauseAnalysis 故障诊断-根因分析结果
     * @return 故障诊断-根因分析结果
     */
    @Override
    public List<FdRootCauseAnalysis> selectFdRootCauseAnalysisList(FdRootCauseAnalysis fdRootCauseAnalysis)
    {
        return fdRootCauseAnalysisMapper.selectFdRootCauseAnalysisList(fdRootCauseAnalysis);
    }

    /**
     * 查询根因分析置信度统计
     *
     * @param fdRootCauseAnalysis 根因分析查询条件
     * @return 根因分析置信度统计结果
     */
    @Override
    public Map<String, Object> selectConfidenceStats(FdRootCauseAnalysis fdRootCauseAnalysis)
    {
        return fdRootCauseAnalysisMapper.selectConfidenceStats(fdRootCauseAnalysis);
    }

    /**
     * 新增故障诊断-根因分析结果
     *
     * @param fdRootCauseAnalysis 故障诊断-根因分析结果
     * @return 结果
     */
    @Override
    public int insertFdRootCauseAnalysis(FdRootCauseAnalysis fdRootCauseAnalysis)
    {
        fdRootCauseAnalysis.setCreateTime(DateUtils.getNowDate());

        if (fdRootCauseAnalysis.getDelFlag() == null || "".equals(fdRootCauseAnalysis.getDelFlag()))
        {
            fdRootCauseAnalysis.setDelFlag("0");
        }

        return fdRootCauseAnalysisMapper.insertFdRootCauseAnalysis(fdRootCauseAnalysis);
    }

    /**
     * 修改故障诊断-根因分析结果
     *
     * @param fdRootCauseAnalysis 故障诊断-根因分析结果
     * @return 结果
     */
    @Override
    public int updateFdRootCauseAnalysis(FdRootCauseAnalysis fdRootCauseAnalysis)
    {
        fdRootCauseAnalysis.setUpdateTime(DateUtils.getNowDate());
        return fdRootCauseAnalysisMapper.updateFdRootCauseAnalysis(fdRootCauseAnalysis);
    }

    /**
     * 批量删除故障诊断-根因分析结果
     *
     * @param analysisIds 需要删除的根因分析ID
     * @return 结果
     */
    @Override
    public int deleteFdRootCauseAnalysisByAnalysisIds(Long[] analysisIds)
    {
        return fdRootCauseAnalysisMapper.deleteFdRootCauseAnalysisByAnalysisIds(analysisIds);
    }

    /**
     * 删除故障诊断-根因分析结果信息
     *
     * @param analysisId 根因分析ID
     * @return 结果
     */
    @Override
    public int deleteFdRootCauseAnalysisByAnalysisId(Long analysisId)
    {
        return fdRootCauseAnalysisMapper.deleteFdRootCauseAnalysisByAnalysisId(analysisId);
    }
}
