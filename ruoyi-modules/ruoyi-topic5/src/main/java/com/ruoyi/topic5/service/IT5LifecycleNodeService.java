package com.ruoyi.topic5.service;

import java.util.List;
import com.ruoyi.topic5.domain.T5LifecycleNode;

/**
 * 全生命周期图节点Service接口
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public interface IT5LifecycleNodeService 
{
    /**
     * 查询全生命周期图节点
     * 
     * @param nodeId 全生命周期图节点主键
     * @return 全生命周期图节点
     */
    public T5LifecycleNode selectT5LifecycleNodeByNodeId(Long nodeId);

    /**
     * 查询全生命周期图节点列表
     * 
     * @param t5LifecycleNode 全生命周期图节点
     * @return 全生命周期图节点集合
     */
    public List<T5LifecycleNode> selectT5LifecycleNodeList(T5LifecycleNode t5LifecycleNode);

    /**
     * 新增全生命周期图节点
     * 
     * @param t5LifecycleNode 全生命周期图节点
     * @return 结果
     */
    public int insertT5LifecycleNode(T5LifecycleNode t5LifecycleNode);

    /**
     * 修改全生命周期图节点
     * 
     * @param t5LifecycleNode 全生命周期图节点
     * @return 结果
     */
    public int updateT5LifecycleNode(T5LifecycleNode t5LifecycleNode);

    /**
     * 批量删除全生命周期图节点
     * 
     * @param nodeIds 需要删除的全生命周期图节点主键集合
     * @return 结果
     */
    public int deleteT5LifecycleNodeByNodeIds(Long[] nodeIds);

    /**
     * 删除全生命周期图节点信息
     * 
     * @param nodeId 全生命周期图节点主键
     * @return 结果
     */
    public int deleteT5LifecycleNodeByNodeId(Long nodeId);
}
