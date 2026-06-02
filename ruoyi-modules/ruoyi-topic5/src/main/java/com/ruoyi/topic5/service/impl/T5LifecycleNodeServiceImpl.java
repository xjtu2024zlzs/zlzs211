package com.ruoyi.topic5.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.topic5.mapper.T5LifecycleNodeMapper;
import com.ruoyi.topic5.domain.T5LifecycleNode;
import com.ruoyi.topic5.service.IT5LifecycleNodeService;

/**
 * 全生命周期图节点Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@Service
public class T5LifecycleNodeServiceImpl implements IT5LifecycleNodeService 
{
    @Autowired
    private T5LifecycleNodeMapper t5LifecycleNodeMapper;

    /**
     * 查询全生命周期图节点
     * 
     * @param nodeId 全生命周期图节点主键
     * @return 全生命周期图节点
     */
    @Override
    public T5LifecycleNode selectT5LifecycleNodeByNodeId(Long nodeId)
    {
        return t5LifecycleNodeMapper.selectT5LifecycleNodeByNodeId(nodeId);
    }

    /**
     * 查询全生命周期图节点列表
     * 
     * @param t5LifecycleNode 全生命周期图节点
     * @return 全生命周期图节点
     */
    @Override
    public List<T5LifecycleNode> selectT5LifecycleNodeList(T5LifecycleNode t5LifecycleNode)
    {
        return t5LifecycleNodeMapper.selectT5LifecycleNodeList(t5LifecycleNode);
    }

    /**
     * 新增全生命周期图节点
     * 
     * @param t5LifecycleNode 全生命周期图节点
     * @return 结果
     */
    @Override
    public int insertT5LifecycleNode(T5LifecycleNode t5LifecycleNode)
    {
        t5LifecycleNode.setCreateTime(DateUtils.getNowDate());
        return t5LifecycleNodeMapper.insertT5LifecycleNode(t5LifecycleNode);
    }

    /**
     * 修改全生命周期图节点
     * 
     * @param t5LifecycleNode 全生命周期图节点
     * @return 结果
     */
    @Override
    public int updateT5LifecycleNode(T5LifecycleNode t5LifecycleNode)
    {
        t5LifecycleNode.setUpdateTime(DateUtils.getNowDate());
        return t5LifecycleNodeMapper.updateT5LifecycleNode(t5LifecycleNode);
    }

    /**
     * 批量删除全生命周期图节点
     * 
     * @param nodeIds 需要删除的全生命周期图节点主键
     * @return 结果
     */
    @Override
    public int deleteT5LifecycleNodeByNodeIds(Long[] nodeIds)
    {
        return t5LifecycleNodeMapper.deleteT5LifecycleNodeByNodeIds(nodeIds);
    }

    /**
     * 删除全生命周期图节点信息
     * 
     * @param nodeId 全生命周期图节点主键
     * @return 结果
     */
    @Override
    public int deleteT5LifecycleNodeByNodeId(Long nodeId)
    {
        return t5LifecycleNodeMapper.deleteT5LifecycleNodeByNodeId(nodeId);
    }
}
