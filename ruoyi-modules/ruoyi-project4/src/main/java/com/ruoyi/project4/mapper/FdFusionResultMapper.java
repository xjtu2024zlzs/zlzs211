package com.ruoyi.project4.mapper;

import com.ruoyi.project4.domain.FdFusionResult;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
@Mapper
/**
 * 故障诊断-融合征结果Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
public interface FdFusionResultMapper 
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
     * 删除故障诊断-融合征结果
     * 
     * @param fusionId 故障诊断-融合征结果主键
     * @return 结果
     */
    public int deleteFdFusionResultByFusionId(Long fusionId);

    /**
     * 批量删除故障诊断-融合征结果
     * 
     * @param fusionIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFdFusionResultByFusionIds(Long[] fusionIds);
}

