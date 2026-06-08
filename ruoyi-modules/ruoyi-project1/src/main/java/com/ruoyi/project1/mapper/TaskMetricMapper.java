package com.ruoyi.project1.mapper;

import java.util.List;
import com.ruoyi.project1.domain.TaskMetric;

/**
 * 模式映射评估指标Mapper接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface TaskMetricMapper 
{
    /**
     * 查询模式映射评估指标
     * 
     * @param metricId 模式映射评估指标主键
     * @return 模式映射评估指标
     */
    public TaskMetric selectTaskMetricByMetricId(Long metricId);

    /**
     * 查询模式映射评估指标列表
     * 
     * @param taskMetric 模式映射评估指标
     * @return 模式映射评估指标集合
     */
    public List<TaskMetric> selectTaskMetricList(TaskMetric taskMetric);

    /**
     * 新增模式映射评估指标
     * 
     * @param taskMetric 模式映射评估指标
     * @return 结果
     */
    public int insertTaskMetric(TaskMetric taskMetric);

    /**
     * 修改模式映射评估指标
     * 
     * @param taskMetric 模式映射评估指标
     * @return 结果
     */
    public int updateTaskMetric(TaskMetric taskMetric);

    /**
     * 删除模式映射评估指标
     * 
     * @param metricId 模式映射评估指标主键
     * @return 结果
     */
    public int deleteTaskMetricByMetricId(Long metricId);

    /**
     * 批量删除模式映射评估指标
     * 
     * @param metricIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTaskMetricByMetricIds(Long[] metricIds);
}
