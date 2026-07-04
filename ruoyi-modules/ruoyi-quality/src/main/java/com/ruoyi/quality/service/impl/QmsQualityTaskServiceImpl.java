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

        /*
         * 关键限制：
         * 只允许回填当前正在处理或刚分派的任务。
         * 避免同一个质量问题多次分派时，误把结果回填到历史任务。
         */
        String taskStatus = task.getTaskStatus();

        if (!"DISPATCHED".equals(taskStatus) && !"PROCESSING".equals(taskStatus))
        {
            throw new ServiceException("当前质量任务状态不是待处理或处理中，不能回填结果。当前状态：" + taskStatus);
        }

        if (StringUtils.isEmpty(submitDto.getProcessFile()))
        {
            throw new ServiceException("处理结果文件不能为空，请先在课题五导出最终溯源Word报告");
        }

        if (!isWordFile(submitDto.getProcessFile()))
        {
            throw new ServiceException("处理结果文件不是Word报告，不能回填。当前文件：" + submitDto.getProcessFile());
        }

        QmsQualityTask updateTask = new QmsQualityTask();

        /*
         * 核心：只按照 task_id 更新当前这一次分派任务
         */
        updateTask.setTaskId(task.getTaskId());
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
    }

    private boolean isWordFile(String fileUrl)
    {
        if (StringUtils.isEmpty(fileUrl))
        {
            return false;
        }

        String lower = fileUrl.toLowerCase();

        return lower.endsWith(".doc") || lower.endsWith(".docx");
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
