package com.ruoyi.quality.service.impl;

import java.util.List;
import com.ruoyi.qms.api.domain.QualityTaskDto;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.quality.mapper.QmsQualityTaskMapper;
import com.ruoyi.quality.domain.QmsQualityTask;
import com.ruoyi.quality.service.IQmsQualityTaskService;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.qms.api.domain.QualityTaskSubmitDto;
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
    @Override
    public void submitQualityTaskResult(QualityTaskSubmitDto submitDto)
    {
        if (submitDto == null || submitDto.getTaskId() == null)
        {
            throw new ServiceException("质量任务ID不能为空");
        }

        if (StringUtils.isEmpty(submitDto.getModuleCode()))
        {
            throw new ServiceException("提交模块编码不能为空");
        }

        QmsQualityTask task = qmsQualityTaskMapper.selectQmsQualityTaskByTaskId(submitDto.getTaskId());

        if (task == null)
        {
            throw new ServiceException("质量任务不存在");
        }

        if (!submitDto.getModuleCode().equals(task.getModuleCode()))
        {
            throw new ServiceException("当前模块无权提交该质量任务结果");
        }

        QmsQualityTask updateTask = new QmsQualityTask();
        updateTask.setTaskId(submitDto.getTaskId());
        updateTask.setTaskStatus("SUBMITTED");
        updateTask.setProcessResult(submitDto.getProcessResult());
        updateTask.setProcessFile(submitDto.getProcessFile());
        updateTask.setSubmitUserId(submitDto.getSubmitUserId());
        updateTask.setSubmitUserName(
                StringUtils.isEmpty(submitDto.getSubmitUserName()) ? "PROJECT_5系统" : submitDto.getSubmitUserName()
        );
        updateTask.setSubmitTime(DateUtils.getNowDate());
        updateTask.setUpdateTime(DateUtils.getNowDate());

        qmsQualityTaskMapper.updateQmsQualityTask(updateTask);

        // 暂时先只更新任务表。流程日志下一步再加，避免这一步改太多。
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
    @Override
    public List<QualityTaskDto> selectQualityTaskDtoListForModule(String moduleCode)
    {
        return qmsQualityTaskMapper.selectQualityTaskDtoListForModule(moduleCode);
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
