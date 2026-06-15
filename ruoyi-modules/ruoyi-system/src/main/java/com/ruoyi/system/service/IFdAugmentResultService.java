package com.ruoyi.system.service;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.system.domain.FdAugmentResult;
import com.ruoyi.system.domain.dto.AugmentRunDto;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.system.domain.dto.AugmentRunDto;
import java.util.List;

/**
 * 故障诊断-样本增强结果Service接口
 *
 * @author ruoyi
 * @date 2026-05-29
 */
public interface IFdAugmentResultService
{
    /**
     * 查询故障诊断-样本增强结果
     *
     * @param augmentId 样本增强结果主键
     * @return 故障诊断-样本增强结果
     */
    FdAugmentResult selectFdAugmentResultByAugmentId(Long augmentId);

    /**
     * 查询故障诊断-样本增强结果列表
     *
     * @param fdAugmentResult 故障诊断-样本增强结果
     * @return 故障诊断-样本增强结果集合
     */
    List<FdAugmentResult> selectFdAugmentResultList(FdAugmentResult fdAugmentResult);

    /**
     * 新增故障诊断-样本增强结果
     *
     * @param fdAugmentResult 故障诊断-样本增强结果
     * @return 结果
     */
    int insertFdAugmentResult(FdAugmentResult fdAugmentResult);

    /**
     * 修改故障诊断-样本增强结果
     *
     * @param fdAugmentResult 故障诊断-样本增强结果
     * @return 结果
     */
    int updateFdAugmentResult(FdAugmentResult fdAugmentResult);

    /**
     * 批量删除故障诊断-样本增强结果
     *
     * @param augmentIds 需要删除的样本增强结果主键集合
     * @return 结果
     */
    int deleteFdAugmentResultByAugmentIds(Long[] augmentIds);

    /**
     * 删除故障诊断-样本增强结果信息
     *
     * @param augmentId 样本增强结果主键
     * @return 结果
     */
    int deleteFdAugmentResultByAugmentId(Long augmentId);

    /**
     * 执行样本增强算法
     *
     * @param dto 样本增强运行参数
     * @return 样本增强执行结果
     */
    AjaxResult runAugment(AugmentRunDto dto);

}
