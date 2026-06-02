package com.ruoyi.topic5.service;

import java.util.List;
import com.ruoyi.topic5.domain.T5LifecycleGraph;
import java.util.Map;
import com.ruoyi.common.core.web.domain.AjaxResult;

/**
 * 全生命周期关联模型Service接口
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
public interface IT5LifecycleGraphService 
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
     * 批量删除全生命周期关联模型
     * 
     * @param graphIds 需要删除的全生命周期关联模型主键集合
     * @return 结果
     */
    public int deleteT5LifecycleGraphByGraphIds(Long[] graphIds);

    /**
     * 删除全生命周期关联模型信息
     * 
     * @param graphId 全生命周期关联模型主键
     * @return 结果
     */
    public int deleteT5LifecycleGraphByGraphId(Long graphId);
    /**
     * 构建全生命周期关联图模型
     *
     * @param caseId 追溯案例ID
     * @param algorithmId 算法ID
     * @return 构建结果
     */
    public AjaxResult buildLifecycleGraph(Long caseId, Long algorithmId);

    /**
     * 根据追溯案例查询最新图模型
     *
     * @param caseId 追溯案例ID
     * @return 最新图模型
     */
    public T5LifecycleGraph selectLatestGraphByCaseId(Long caseId);

    /**
     * 获取前端图网络展示数据
     *
     * @param graphId 图模型ID
     * @return 图网络数据
     */
    public Map<String, Object> getGraphData(Long graphId);
}
