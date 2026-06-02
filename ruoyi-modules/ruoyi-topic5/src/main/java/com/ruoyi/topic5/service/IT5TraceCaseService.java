package com.ruoyi.topic5.service;

import java.util.List;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.topic5.domain.T5TraceCase;

/**
 * 追溯案例Service接口
 *
 * @author ruoyi
 * @date 2026-05-31
 */
public interface IT5TraceCaseService
{
    /**
     * 查询追溯案例
     *
     * @param caseId 追溯案例主键
     * @return 追溯案例
     */
    public T5TraceCase selectT5TraceCaseByCaseId(Long caseId);

    /**
     * 查询追溯案例列表
     *
     * @param t5TraceCase 追溯案例
     * @return 追溯案例集合
     */
    public List<T5TraceCase> selectT5TraceCaseList(T5TraceCase t5TraceCase);

    /**
     * 新增追溯案例
     *
     * @param t5TraceCase 追溯案例
     * @return 结果
     */
    public int insertT5TraceCase(T5TraceCase t5TraceCase);

    /**
     * 修改追溯案例
     *
     * @param t5TraceCase 追溯案例
     * @return 结果
     */
    public int updateT5TraceCase(T5TraceCase t5TraceCase);

    /**
     * 批量删除追溯案例
     *
     * @param caseIds 需要删除的追溯案例主键集合
     * @return 结果
     */
    public int deleteT5TraceCaseByCaseIds(Long[] caseIds);

    /**
     * 删除追溯案例信息
     *
     * @param caseId 追溯案例主键
     * @return 结果
     */
    public int deleteT5TraceCaseByCaseId(Long caseId);

    /**
     * 运行故障零件定位算法
     *
     * @param caseId 追溯案例ID
     * @param algorithmId 算法ID
     * @return 定位结果
     */
    public AjaxResult locatePart(Long caseId, Long algorithmId);
}