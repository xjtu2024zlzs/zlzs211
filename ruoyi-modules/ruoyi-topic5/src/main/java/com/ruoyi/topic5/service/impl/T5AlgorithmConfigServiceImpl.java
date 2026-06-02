package com.ruoyi.topic5.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.topic5.mapper.T5AlgorithmConfigMapper;
import com.ruoyi.topic5.domain.T5AlgorithmConfig;
import com.ruoyi.topic5.service.IT5AlgorithmConfigService;

/**
 * 零件定位结果Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-31
 */
@Service
public class T5AlgorithmConfigServiceImpl implements IT5AlgorithmConfigService 
{
    @Autowired
    private T5AlgorithmConfigMapper t5AlgorithmConfigMapper;

    /**
     * 查询零件定位结果
     * 
     * @param algorithmId 零件定位结果主键
     * @return 零件定位结果
     */
    @Override
    public T5AlgorithmConfig selectT5AlgorithmConfigByAlgorithmId(Long algorithmId)
    {
        return t5AlgorithmConfigMapper.selectT5AlgorithmConfigByAlgorithmId(algorithmId);
    }

    /**
     * 查询零件定位结果列表
     * 
     * @param t5AlgorithmConfig 零件定位结果
     * @return 零件定位结果
     */
    @Override
    public List<T5AlgorithmConfig> selectT5AlgorithmConfigList(T5AlgorithmConfig t5AlgorithmConfig)
    {
        return t5AlgorithmConfigMapper.selectT5AlgorithmConfigList(t5AlgorithmConfig);
    }

    /**
     * 新增零件定位结果
     * 
     * @param t5AlgorithmConfig 零件定位结果
     * @return 结果
     */
    @Override
    public int insertT5AlgorithmConfig(T5AlgorithmConfig t5AlgorithmConfig)
    {
        t5AlgorithmConfig.setCreateTime(DateUtils.getNowDate());
        return t5AlgorithmConfigMapper.insertT5AlgorithmConfig(t5AlgorithmConfig);
    }

    /**
     * 修改零件定位结果
     * 
     * @param t5AlgorithmConfig 零件定位结果
     * @return 结果
     */
    @Override
    public int updateT5AlgorithmConfig(T5AlgorithmConfig t5AlgorithmConfig)
    {
        t5AlgorithmConfig.setUpdateTime(DateUtils.getNowDate());
        return t5AlgorithmConfigMapper.updateT5AlgorithmConfig(t5AlgorithmConfig);
    }

    /**
     * 批量删除零件定位结果
     * 
     * @param algorithmIds 需要删除的零件定位结果主键
     * @return 结果
     */
    @Override
    public int deleteT5AlgorithmConfigByAlgorithmIds(Long[] algorithmIds)
    {
        return t5AlgorithmConfigMapper.deleteT5AlgorithmConfigByAlgorithmIds(algorithmIds);
    }

    /**
     * 删除零件定位结果信息
     * 
     * @param algorithmId 零件定位结果主键
     * @return 结果
     */
    @Override
    public int deleteT5AlgorithmConfigByAlgorithmId(Long algorithmId)
    {
        return t5AlgorithmConfigMapper.deleteT5AlgorithmConfigByAlgorithmId(algorithmId);
    }
}
