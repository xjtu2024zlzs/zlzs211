package com.ruoyi.system.service;

import com.ruoyi.system.domain.FdFusionResult;

import java.util.List;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.system.domain.dto.FusionRunDto;
/**
 * 故障诊断-融合征结果Service接口
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
public interface IFdFusionResultService 
{
    /**
     * 查询故障诊断-融合征结果
     * 
     * @param fusionId 故障诊断-融合征结果主键
     * @return 故障诊断-融合征结果
     */
    public FdFusionResult selectFdFusionResultByFusionId(Long fusionId);

    /**
     * 查询故障诊断-融合征结果列表
     * 
     * @param fdFusionResult 故障诊断-融合征结果
     * @return 故障诊断-融合征结果集合
     */
    public List<FdFusionResult> selectFdFusionResultList(FdFusionResult fdFusionResult);

    /**
     * 新增故障诊断-融合征结果
     * 
     * @param fdFusionResult 故障诊断-融合征结果
     * @return 结果
     */
    public int insertFdFusionResult(FdFusionResult fdFusionResult);

    /**
     * 修改故障诊断-融合征结果
     * 
     * @param fdFusionResult 故障诊断-融合征结果
     * @return 结果
     */
    public int updateFdFusionResult(FdFusionResult fdFusionResult);

    /**
     * 批量删除故障诊断-融合征结果
     * 
     * @param fusionIds 需要删除的故障诊断-融合征结果主键集合
     * @return 结果
     */
    public int deleteFdFusionResultByFusionIds(Long[] fusionIds);

    /**
     * 删除故障诊断-融合征结果信息
     * 
     * @param fusionId 故障诊断-融合征结果主键
     * @return 结果
     */
    public int deleteFdFusionResultByFusionId(Long fusionId);

    /**
     * 执行特征融合算法
     *
     * @param dto 特征融合运行参数
     * @return 融合结果
     */
    AjaxResult runFusion(FusionRunDto dto);
}
