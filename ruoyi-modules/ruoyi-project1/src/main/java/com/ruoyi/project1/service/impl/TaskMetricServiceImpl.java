package com.ruoyi.project1.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project1.mapper.TaskMetricMapper;
import com.ruoyi.project1.domain.TaskMetric;
import com.ruoyi.project1.service.ITaskMetricService;

/**
 * 模式映射评估指标Service业务层处理
 * 
 * @author pwb
 * @date 2026-06-05
 */
@Service
public class TaskMetricServiceImpl implements ITaskMetricService 
{
    @Autowired
    private TaskMetricMapper taskMetricMapper;

    /**
     * 查询模式映射评估指标
     * 
     * @param metricId 模式映射评估指标主键
     * @return 模式映射评估指标
     */
    @Override
    public TaskMetric selectTaskMetricByMetricId(Long metricId)
    {
        return taskMetricMapper.selectTaskMetricByMetricId(metricId);
    }

    /**
     * 查询模式映射评估指标列表
     * 
     * @param taskMetric 模式映射评估指标
     * @return 模式映射评估指标
     */
    @Override
    public List<TaskMetric> selectTaskMetricList(TaskMetric taskMetric)
    {
        return taskMetricMapper.selectTaskMetricList(taskMetric);
    }

    /**
     * 新增模式映射评估指标
     * 
     * @param taskMetric 模式映射评估指标
     * @return 结果
     */
    @Override
    public int insertTaskMetric(TaskMetric taskMetric)
    {
        taskMetric.setCreateTime(DateUtils.getNowDate());
        return taskMetricMapper.insertTaskMetric(taskMetric);
    }

    /**
     * 修改模式映射评估指标
     * 
     * @param taskMetric 模式映射评估指标
     * @return 结果
     */
    @Override
    public int updateTaskMetric(TaskMetric taskMetric)
    {
        return taskMetricMapper.updateTaskMetric(taskMetric);
    }

    /**
     * 批量删除模式映射评估指标
     * 
     * @param metricIds 需要删除的模式映射评估指标主键
     * @return 结果
     */
    @Override
    public int deleteTaskMetricByMetricIds(Long[] metricIds)
    {
        return taskMetricMapper.deleteTaskMetricByMetricIds(metricIds);
    }

    /**
     * 删除模式映射评估指标信息
     * 
     * @param metricId 模式映射评估指标主键
     * @return 结果
     */
    @Override
    public int deleteTaskMetricByMetricId(Long metricId)
    {
        return taskMetricMapper.deleteTaskMetricByMetricId(metricId);
    }
}
