package com.ruoyi.topic5.mapper;

import java.util.List;
import com.ruoyi.topic5.domain.T5LifecycleGraph;

/**
 * 全生命周期关联模型Mapper接口
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public interface T5LifecycleGraphMapper 
{
    /**
     * 查询全生命周期关联模型
     * 
     * @param graphId 全生命周期关联模型主键
     * @return 全生命周期关联模型
     */
    public T5LifecycleGraph selectT5LifecycleGraphByGraphId(Long graphId);

    /**
     * 查询全生命周期关联模型列表
     * 
     * @param t5LifecycleGraph 全生命周期关联模型
     * @return 全生命周期关联模型集合
     */
    public List<T5LifecycleGraph> selectT5LifecycleGraphList(T5LifecycleGraph t5LifecycleGraph);

    /**
     * 新增全生命周期关联模型
     * 
     * @param t5LifecycleGraph 全生命周期关联模型
     * @return 结果
     */
    public int insertT5LifecycleGraph(T5LifecycleGraph t5LifecycleGraph);

    /**
     * 修改全生命周期关联模型
     * 
     * @param t5LifecycleGraph 全生命周期关联模型
     * @return 结果
     */
    public int updateT5LifecycleGraph(T5LifecycleGraph t5LifecycleGraph);

    /**
     * 删除全生命周期关联模型
     * 
     * @param graphId 全生命周期关联模型主键
     * @return 结果
     */
    public int deleteT5LifecycleGraphByGraphId(Long graphId);

    /**
     * 批量删除全生命周期关联模型
     * 
     * @param graphIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteT5LifecycleGraphByGraphIds(Long[] graphIds);
    /**
     * 根据追溯案例查询最新图模型
     *
     * @param caseId 追溯案例ID
     * @return 最新图模型
     */
    public T5LifecycleGraph selectLatestGraphByCaseId(Long caseId);
}
