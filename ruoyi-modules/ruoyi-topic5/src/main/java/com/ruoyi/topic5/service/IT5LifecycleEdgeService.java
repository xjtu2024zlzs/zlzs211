package com.ruoyi.topic5.service;

import java.util.List;
import com.ruoyi.topic5.domain.T5LifecycleEdge;

/**
 * 全生命周期图关系边Service接口
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public interface IT5LifecycleEdgeService 
{
    /**
     * 查询全生命周期图关系边
     * 
     * @param edgeId 全生命周期图关系边主键
     * @return 全生命周期图关系边
     */
    public T5LifecycleEdge selectT5LifecycleEdgeByEdgeId(Long edgeId);

    /**
     * 查询全生命周期图关系边列表
     * 
     * @param t5LifecycleEdge 全生命周期图关系边
     * @return 全生命周期图关系边集合
     */
    public List<T5LifecycleEdge> selectT5LifecycleEdgeList(T5LifecycleEdge t5LifecycleEdge);

    /**
     * 新增全生命周期图关系边
     * 
     * @param t5LifecycleEdge 全生命周期图关系边
     * @return 结果
     */
    public int insertT5LifecycleEdge(T5LifecycleEdge t5LifecycleEdge);

    /**
     * 修改全生命周期图关系边
     * 
     * @param t5LifecycleEdge 全生命周期图关系边
     * @return 结果
     */
    public int updateT5LifecycleEdge(T5LifecycleEdge t5LifecycleEdge);

    /**
     * 批量删除全生命周期图关系边
     * 
     * @param edgeIds 需要删除的全生命周期图关系边主键集合
     * @return 结果
     */
    public int deleteT5LifecycleEdgeByEdgeIds(Long[] edgeIds);

    /**
     * 删除全生命周期图关系边信息
     * 
     * @param edgeId 全生命周期图关系边主键
     * @return 结果
     */
    public int deleteT5LifecycleEdgeByEdgeId(Long edgeId);
}
