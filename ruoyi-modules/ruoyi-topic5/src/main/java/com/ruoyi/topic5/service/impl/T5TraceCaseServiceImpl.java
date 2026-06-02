package com.ruoyi.topic5.service.impl;

import java.math.BigDecimal;
import java.util.List;

import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.web.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.topic5.domain.T5PartInstance;
import com.ruoyi.topic5.domain.T5PartLocateResult;
import com.ruoyi.topic5.domain.T5TraceCase;
import com.ruoyi.topic5.mapper.T5PartInstanceMapper;
import com.ruoyi.topic5.mapper.T5PartLocateResultMapper;
import com.ruoyi.topic5.mapper.T5TraceCaseMapper;
import com.ruoyi.topic5.service.IT5TraceCaseService;

/**
 * 追溯案例Service业务层处理
 *
 * @author ruoyi
 * @date 2026-05-31
 */
@Service
public class T5TraceCaseServiceImpl implements IT5TraceCaseService
{
    @Autowired
    private T5TraceCaseMapper t5TraceCaseMapper;

    @Autowired
    private T5PartInstanceMapper t5PartInstanceMapper;

    @Autowired
    private T5PartLocateResultMapper t5PartLocateResultMapper;

    /**
     * 查询追溯案例
     *
     * @param caseId 追溯案例主键
     * @return 追溯案例
     */
    @Override
    public T5TraceCase selectT5TraceCaseByCaseId(Long caseId)
    {
        return t5TraceCaseMapper.selectT5TraceCaseByCaseId(caseId);
    }

    /**
     * 查询追溯案例列表
     *
     * @param t5TraceCase 追溯案例
     * @return 追溯案例集合
     */
    @Override
    public List<T5TraceCase> selectT5TraceCaseList(T5TraceCase t5TraceCase)
    {
        return t5TraceCaseMapper.selectT5TraceCaseList(t5TraceCase);
    }

    /**
     * 新增追溯案例
     *
     * @param t5TraceCase 追溯案例
     * @return 结果
     */
    @Override
    public int insertT5TraceCase(T5TraceCase t5TraceCase)
    {
        t5TraceCase.setCreateTime(DateUtils.getNowDate());
        return t5TraceCaseMapper.insertT5TraceCase(t5TraceCase);
    }

    /**
     * 修改追溯案例
     *
     * @param t5TraceCase 追溯案例
     * @return 结果
     */
    @Override
    public int updateT5TraceCase(T5TraceCase t5TraceCase)
    {
        t5TraceCase.setUpdateTime(DateUtils.getNowDate());
        return t5TraceCaseMapper.updateT5TraceCase(t5TraceCase);
    }

    /**
     * 批量删除追溯案例
     *
     * @param caseIds 需要删除的追溯案例主键集合
     * @return 结果
     */
    @Override
    public int deleteT5TraceCaseByCaseIds(Long[] caseIds)
    {
        return t5TraceCaseMapper.deleteT5TraceCaseByCaseIds(caseIds);
    }

    /**
     * 删除追溯案例信息
     *
     * @param caseId 追溯案例主键
     * @return 结果
     */
    @Override
    public int deleteT5TraceCaseByCaseId(Long caseId)
    {
        return t5TraceCaseMapper.deleteT5TraceCaseByCaseId(caseId);
    }

    /**
     * 运行故障零件定位算法
     *
     * 当前版本为演示用模拟算法：
     * 1. 根据 caseId 查询故障表征；
     * 2. 从零部件实例库中读取候选零件；
     * 3. 取前三个零件作为候选故障零件；
     * 4. 写入 t5_part_locate_result；
     * 5. 将排序第一的零件回写为最终故障零件。
     *
     * @param caseId 追溯案例ID
     * @param algorithmId 算法ID
     * @return 定位结果
     */
    @Override
    public AjaxResult locatePart(Long caseId, Long algorithmId)
    {
        T5TraceCase traceCase = t5TraceCaseMapper.selectT5TraceCaseByCaseId(caseId);
        if (traceCase == null)
        {
            return AjaxResult.error("追溯案例不存在");
        }

        if (algorithmId == null)
        {
            return AjaxResult.error("请选择故障零件定位算法");
        }

        // 查询零部件实例库
        T5PartInstance query = new T5PartInstance();
        List<T5PartInstance> partList = t5PartInstanceMapper.selectT5PartInstanceList(query);

        if (partList == null || partList.isEmpty())
        {
            return AjaxResult.error("零部件实例库为空，请先录入零部件样例数据");
        }

        // 删除该案例已有定位结果，避免重复运行后结果叠加
        t5PartLocateResultMapper.deleteT5PartLocateResultByCaseId(caseId);

        // 演示用候选结果分数
        BigDecimal[] scores = {
                new BigDecimal("0.9200"),
                new BigDecimal("0.7800"),
                new BigDecimal("0.6100")
        };

        int limit = Math.min(3, partList.size());

        for (int i = 0; i < limit; i++)
        {
            T5PartInstance part = partList.get(i);

            T5PartLocateResult result = new T5PartLocateResult();
            result.setCaseId(caseId);
            result.setAlgorithmId(algorithmId);
            result.setAlgorithmName(traceCase.getLocateAlgorithmName());

            result.setPartId(part.getPartId());
            result.setPartNo(part.getPartNo());
            result.setPartName(part.getPartName());

            result.setMatchScore(scores[i]);
            result.setConfidence(scores[i]);
            result.setRankNo((long) (i + 1));
            result.setIsFinal(i == 0 ? "1" : "0");

            result.setLocateReason("基于故障表征与零部件所属系统、子系统、安装位置等信息进行模拟匹配。");
            result.setEvidenceSummary("演示数据：根据样例零部件库生成候选故障零件。");
            result.setCreateTime(DateUtils.getNowDate());

            t5PartLocateResultMapper.insertT5PartLocateResult(result);
        }

        // 将排序第一的零件作为最终故障零件
        T5PartInstance finalPart = partList.get(0);

        traceCase.setLocateAlgorithmId(algorithmId);
        traceCase.setFinalPartId(finalPart.getPartId());
        traceCase.setFinalPartNo(finalPart.getPartNo());
        traceCase.setFinalPartName(finalPart.getPartName());
        traceCase.setLocateConfidence(scores[0]);
        traceCase.setCaseStatus("1");
        traceCase.setUpdateTime(DateUtils.getNowDate());

        t5TraceCaseMapper.updateT5TraceCase(traceCase);

        return AjaxResult.success(traceCase);
    }
}