package com.ruoyi.project4.mapper;

import com.ruoyi.project4.domain.FdAugmentResult;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 故障诊断-样本增强结果Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
@Mapper
public interface FdAugmentResultMapper 
{
    /**
     * 查询故障诊断-样本增强结果
     * 
     * @param augmentId 故障诊断-样本增强结果主键
     * @return 故障诊断-样本增强结果
     */
    public FdAugmentResult selectFdAugmentResultByAugmentId(Long augmentId);

    /**
     * 查询故障诊断-样本增强结果列表
     * 
     * @param fdAugmentResult 故障诊断-样本增强结果
     * @return 故障诊断-样本增强结果集合
     */
    public List<FdAugmentResult> selectFdAugmentResultList(FdAugmentResult fdAugmentResult);

    /**
     * 新增故障诊断-样本增强结果
     * 
     * @param fdAugmentResult 故障诊断-样本增强结果
     * @return 结果
     */
    public int insertFdAugmentResult(FdAugmentResult fdAugmentResult);

    /**
     * 修改故障诊断-样本增强结果
     * 
     * @param fdAugmentResult 故障诊断-样本增强结果
     * @return 结果
     */
    public int updateFdAugmentResult(FdAugmentResult fdAugmentResult);

    /**
     * 删除故障诊断-样本增强结果
     * 
     * @param augmentId 故障诊断-样本增强结果主键
     * @return 结果
     */
    public int deleteFdAugmentResultByAugmentId(Long augmentId);

    /**
     * 批量删除故障诊断-样本增强结果
     * 
     * @param augmentIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteFdAugmentResultByAugmentIds(Long[] augmentIds);
}

