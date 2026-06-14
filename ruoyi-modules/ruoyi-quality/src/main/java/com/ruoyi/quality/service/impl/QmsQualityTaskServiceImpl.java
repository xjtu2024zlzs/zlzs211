package com.ruoyi.quality.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.quality.mapper.QmsQualityTaskMapper;
import com.ruoyi.quality.domain.QmsQualityTask;
import com.ruoyi.quality.service.IQmsQualityTaskService;

/**
 * 质量问题模块处理任务Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-06-13
 */
@Service
public class QmsQualityTaskServiceImpl implements IQmsQualityTaskService 
{
    @Autowired
    private QmsQualityTaskMapper qmsQualityTaskMapper;

    /**
     * 查询质量问题模块处理任务
     * 
     * @param taskId 质量问题模块处理任务主键
     * @return 质量问题模块处理任务
     */
    @Override
    public QmsQualityTask selectQmsQualityTaskByTaskId(Long taskId)
    {
        return qmsQualityTaskMapper.selectQmsQualityTaskByTaskId(taskId);
    }

    /**
     * 查询质量问题模块处理任务列表
     * 
     * @param qmsQualityTask 质量问题模块处理任务
     * @return 质量问题模块处理任务
     */
    @Override
    public List<QmsQualityTask> selectQmsQualityTaskList(QmsQualityTask qmsQualityTask)
    {
        return qmsQualityTaskMapper.selectQmsQualityTaskList(qmsQualityTask);
    }

    /**
     * 新增质量问题模块处理任务
     * 
     * @param qmsQualityTask 质量问题模块处理任务
     * @return 结果
     */
    @Override
    public int insertQmsQualityTask(QmsQualityTask qmsQualityTask)
    {
        qmsQualityTask.setCreateTime(DateUtils.getNowDate());
        return qmsQualityTaskMapper.insertQmsQualityTask(qmsQualityTask);
    }

    /**
     * 修改质量问题模块处理任务
     * 
     * @param qmsQualityTask 质量问题模块处理任务
     * @return 结果
     */
    @Override
    public int updateQmsQualityTask(QmsQualityTask qmsQualityTask)
    {
        qmsQualityTask.setUpdateTime(DateUtils.getNowDate());
        return qmsQualityTaskMapper.updateQmsQualityTask(qmsQualityTask);
    }

    /**
     * 批量删除质量问题模块处理任务
     * 
     * @param taskIds 需要删除的质量问题模块处理任务主键
     * @return 结果
     */
    @Override
    public int deleteQmsQualityTaskByTaskIds(Long[] taskIds)
    {
        return qmsQualityTaskMapper.deleteQmsQualityTaskByTaskIds(taskIds);
    }

    /**
     * 删除质量问题模块处理任务信息
     * 
     * @param taskId 质量问题模块处理任务主键
     * @return 结果
     */
    @Override
    public int deleteQmsQualityTaskByTaskId(Long taskId)
    {
        return qmsQualityTaskMapper.deleteQmsQualityTaskByTaskId(taskId);
    }
}
