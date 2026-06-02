package com.ruoyi.topic5.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.topic5.mapper.T5LifecycleEdgeMapper;
import com.ruoyi.topic5.domain.T5LifecycleEdge;
import com.ruoyi.topic5.service.IT5LifecycleEdgeService;

/**
 * 全生命周期图关系边Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@Service
public class T5LifecycleEdgeServiceImpl implements IT5LifecycleEdgeService 
{
    @Autowired
    private T5LifecycleEdgeMapper t5LifecycleEdgeMapper;

    /**
     * 查询全生命周期图关系边
     * 
     * @param edgeId 全生命周期图关系边主键
     * @return 全生命周期图关系边
     */
    @Override
    public T5LifecycleEdge selectT5LifecycleEdgeByEdgeId(Long edgeId)
    {
        return t5LifecycleEdgeMapper.selectT5LifecycleEdgeByEdgeId(edgeId);
    }

    /**
     * 查询全生命周期图关系边列表
     * 
     * @param t5LifecycleEdge 全生命周期图关系边
     * @return 全生命周期图关系边
     */
    @Override
    public List<T5LifecycleEdge> selectT5LifecycleEdgeList(T5LifecycleEdge t5LifecycleEdge)
    {
        return t5LifecycleEdgeMapper.selectT5LifecycleEdgeList(t5LifecycleEdge);
    }

    /**
     * 新增全生命周期图关系边
     * 
     * @param t5LifecycleEdge 全生命周期图关系边
     * @return 结果
     */
    @Override
    public int insertT5LifecycleEdge(T5LifecycleEdge t5LifecycleEdge)
    {
        t5LifecycleEdge.setCreateTime(DateUtils.getNowDate());
        return t5LifecycleEdgeMapper.insertT5LifecycleEdge(t5LifecycleEdge);
    }

    /**
     * 修改全生命周期图关系边
     * 
     * @param t5LifecycleEdge 全生命周期图关系边
     * @return 结果
     */
    @Override
    public int updateT5LifecycleEdge(T5LifecycleEdge t5LifecycleEdge)
    {
        t5LifecycleEdge.setUpdateTime(DateUtils.getNowDate());
        return t5LifecycleEdgeMapper.updateT5LifecycleEdge(t5LifecycleEdge);
    }

    /**
     * 批量删除全生命周期图关系边
     * 
     * @param edgeIds 需要删除的全生命周期图关系边主键
     * @return 结果
     */
    @Override
    public int deleteT5LifecycleEdgeByEdgeIds(Long[] edgeIds)
    {
        return t5LifecycleEdgeMapper.deleteT5LifecycleEdgeByEdgeIds(edgeIds);
    }

    /**
     * 删除全生命周期图关系边信息
     * 
     * @param edgeId 全生命周期图关系边主键
     * @return 结果
     */
    @Override
    public int deleteT5LifecycleEdgeByEdgeId(Long edgeId)
    {
        return t5LifecycleEdgeMapper.deleteT5LifecycleEdgeByEdgeId(edgeId);
    }
}
