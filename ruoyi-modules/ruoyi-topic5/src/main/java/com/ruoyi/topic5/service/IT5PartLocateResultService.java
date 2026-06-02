package com.ruoyi.topic5.service;

import java.util.List;
import com.ruoyi.topic5.domain.T5PartLocateResult;

/**
 * 算法配置Service接口
 * 
 * @author ruoyi
 * @date 2026-05-31
 */
public interface IT5PartLocateResultService 
{
    /**
     * 查询算法配置
     * 
     * @param locateId 算法配置主键
     * @return 算法配置
     */
    public T5PartLocateResult selectT5PartLocateResultByLocateId(Long locateId);

    /**
     * 查询算法配置列表
     * 
     * @param t5PartLocateResult 算法配置
     * @return 算法配置集合
     */
    public List<T5PartLocateResult> selectT5PartLocateResultList(T5PartLocateResult t5PartLocateResult);

    /**
     * 新增算法配置
     * 
     * @param t5PartLocateResult 算法配置
     * @return 结果
     */
    public int insertT5PartLocateResult(T5PartLocateResult t5PartLocateResult);

    /**
     * 修改算法配置
     * 
     * @param t5PartLocateResult 算法配置
     * @return 结果
     */
    public int updateT5PartLocateResult(T5PartLocateResult t5PartLocateResult);

    /**
     * 批量删除算法配置
     * 
     * @param locateIds 需要删除的算法配置主键集合
     * @return 结果
     */
    public int deleteT5PartLocateResultByLocateIds(Long[] locateIds);

    /**
     * 删除算法配置信息
     * 
     * @param locateId 算法配置主键
     * @return 结果
     */
    public int deleteT5PartLocateResultByLocateId(Long locateId);
}
