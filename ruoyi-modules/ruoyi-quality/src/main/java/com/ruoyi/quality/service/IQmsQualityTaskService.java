package com.ruoyi.quality.service;

import java.util.List;
import com.ruoyi.quality.domain.QmsQualityTask;

/**
 * 质量问题模块处理任务Service接口
 * 
 * @author ruoyi
 * @date 2026-06-13
 */
public interface IQmsQualityTaskService 
{
    /**
     * 查询质量问题模块处理任务
     * 
     * @param taskId 质量问题模块处理任务主键
     * @return 质量问题模块处理任务
     */
    public QmsQualityTask selectQmsQualityTaskByTaskId(Long taskId);

    /**
     * 查询质量问题模块处理任务列表
     * 
     * @param qmsQualityTask 质量问题模块处理任务
     * @return 质量问题模块处理任务集合
     */
    public List<QmsQualityTask> selectQmsQualityTaskList(QmsQualityTask qmsQualityTask);

    /**
     * 新增质量问题模块处理任务
     * 
     * @param qmsQualityTask 质量问题模块处理任务
     * @return 结果
     */
    public int insertQmsQualityTask(QmsQualityTask qmsQualityTask);

    /**
     * 修改质量问题模块处理任务
     * 
     * @param qmsQualityTask 质量问题模块处理任务
     * @return 结果
     */
    public int updateQmsQualityTask(QmsQualityTask qmsQualityTask);

    /**
     * 批量删除质量问题模块处理任务
     * 
     * @param taskIds 需要删除的质量问题模块处理任务主键集合
     * @return 结果
     */
    public int deleteQmsQualityTaskByTaskIds(Long[] taskIds);

    /**
     * 删除质量问题模块处理任务信息
     * 
     * @param taskId 质量问题模块处理任务主键
     * @return 结果
     */
    public int deleteQmsQualityTaskByTaskId(Long taskId);
}
