package com.ruoyi.topic5.mapper;

import java.util.List;
import com.ruoyi.topic5.domain.T5PartLocateResult;

/**
 * 算法配置Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-31
 */
public interface T5PartLocateResultMapper 
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
     * 删除算法配置
     * 
     * @param locateId 算法配置主键
     * @return 结果
     */
    public int deleteT5PartLocateResultByLocateId(Long locateId);

    /**
     * 批量删除算法配置
     * 
     * @param locateIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteT5PartLocateResultByLocateIds(Long[] locateIds);
    /**
     * 根据追溯案例ID删除故障零件定位结果
     *
     * @param caseId 追溯案例ID
     * @return 结果
     */
    public int deleteT5PartLocateResultByCaseId(Long caseId);



}
