package com.ruoyi.topic5.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.topic5.mapper.T5PartLocateResultMapper;
import com.ruoyi.topic5.domain.T5PartLocateResult;
import com.ruoyi.topic5.service.IT5PartLocateResultService;

/**
 * 算法配置Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-31
 */
@Service
public class T5PartLocateResultServiceImpl implements IT5PartLocateResultService 
{
    @Autowired
    private T5PartLocateResultMapper t5PartLocateResultMapper;

    /**
     * 查询算法配置
     * 
     * @param locateId 算法配置主键
     * @return 算法配置
     */
    @Override
    public T5PartLocateResult selectT5PartLocateResultByLocateId(Long locateId)
    {
        return t5PartLocateResultMapper.selectT5PartLocateResultByLocateId(locateId);
    }

    /**
     * 查询算法配置列表
     * 
     * @param t5PartLocateResult 算法配置
     * @return 算法配置
     */
    @Override
    public List<T5PartLocateResult> selectT5PartLocateResultList(T5PartLocateResult t5PartLocateResult)
    {
        return t5PartLocateResultMapper.selectT5PartLocateResultList(t5PartLocateResult);
    }

    /**
     * 新增算法配置
     * 
     * @param t5PartLocateResult 算法配置
     * @return 结果
     */
    @Override
    public int insertT5PartLocateResult(T5PartLocateResult t5PartLocateResult)
    {
        t5PartLocateResult.setCreateTime(DateUtils.getNowDate());
        return t5PartLocateResultMapper.insertT5PartLocateResult(t5PartLocateResult);
    }

    /**
     * 修改算法配置
     * 
     * @param t5PartLocateResult 算法配置
     * @return 结果
     */
    @Override
    public int updateT5PartLocateResult(T5PartLocateResult t5PartLocateResult)
    {
        t5PartLocateResult.setUpdateTime(DateUtils.getNowDate());
        return t5PartLocateResultMapper.updateT5PartLocateResult(t5PartLocateResult);
    }

    /**
     * 批量删除算法配置
     * 
     * @param locateIds 需要删除的算法配置主键
     * @return 结果
     */
    @Override
    public int deleteT5PartLocateResultByLocateIds(Long[] locateIds)
    {
        return t5PartLocateResultMapper.deleteT5PartLocateResultByLocateIds(locateIds);
    }

    /**
     * 删除算法配置信息
     * 
     * @param locateId 算法配置主键
     * @return 结果
     */
    @Override
    public int deleteT5PartLocateResultByLocateId(Long locateId)
    {
        return t5PartLocateResultMapper.deleteT5PartLocateResultByLocateId(locateId);
    }
}
