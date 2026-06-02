package com.ruoyi.topic5.mapper;

import java.util.List;
import com.ruoyi.topic5.domain.T5AlgorithmConfig;

/**
 * 零件定位结果Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-31
 */
public interface T5AlgorithmConfigMapper 
{
    /**
     * 查询零件定位结果
     * 
     * @param algorithmId 零件定位结果主键
     * @return 零件定位结果
     */
    public T5AlgorithmConfig selectT5AlgorithmConfigByAlgorithmId(Long algorithmId);

    /**
     * 查询零件定位结果列表
     * 
     * @param t5AlgorithmConfig 零件定位结果
     * @return 零件定位结果集合
     */
    public List<T5AlgorithmConfig> selectT5AlgorithmConfigList(T5AlgorithmConfig t5AlgorithmConfig);

    /**
     * 新增零件定位结果
     * 
     * @param t5AlgorithmConfig 零件定位结果
     * @return 结果
     */
    public int insertT5AlgorithmConfig(T5AlgorithmConfig t5AlgorithmConfig);

    /**
     * 修改零件定位结果
     * 
     * @param t5AlgorithmConfig 零件定位结果
     * @return 结果
     */
    public int updateT5AlgorithmConfig(T5AlgorithmConfig t5AlgorithmConfig);

    /**
     * 删除零件定位结果
     * 
     * @param algorithmId 零件定位结果主键
     * @return 结果
     */
    public int deleteT5AlgorithmConfigByAlgorithmId(Long algorithmId);

    /**
     * 批量删除零件定位结果
     * 
     * @param algorithmIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteT5AlgorithmConfigByAlgorithmIds(Long[] algorithmIds);
}
