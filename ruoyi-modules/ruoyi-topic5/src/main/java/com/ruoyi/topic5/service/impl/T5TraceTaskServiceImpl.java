package com.ruoyi.topic5.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.topic5.mapper.T5TraceTaskMapper;
import com.ruoyi.topic5.domain.T5TraceTask;
import com.ruoyi.topic5.service.IT5TraceTaskService;

/**
 * 课题五-追溯任务主Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@Service
public class T5TraceTaskServiceImpl implements IT5TraceTaskService 
{
    @Autowired
    private T5TraceTaskMapper t5TraceTaskMapper;

    /**
     * 查询课题五-追溯任务主
     * 
     * @param taskId 课题五-追溯任务主主键
     * @return 课题五-追溯任务主
     */
    @Override
    public T5TraceTask selectT5TraceTaskByTaskId(Long taskId)
    {
        return t5TraceTaskMapper.selectT5TraceTaskByTaskId(taskId);
    }

    /**
     * 查询课题五-追溯任务主列表
     * 
     * @param t5TraceTask 课题五-追溯任务主
     * @return 课题五-追溯任务主
     */
    @Override
    public List<T5TraceTask> selectT5TraceTaskList(T5TraceTask t5TraceTask)
    {
        return t5TraceTaskMapper.selectT5TraceTaskList(t5TraceTask);
    }

    /**
     * 新增课题五-追溯任务主
     * 
     * @param t5TraceTask 课题五-追溯任务主
     * @return 结果
     */
    @Override
    public int insertT5TraceTask(T5TraceTask t5TraceTask)
    {
        t5TraceTask.setCreateTime(DateUtils.getNowDate());
        return t5TraceTaskMapper.insertT5TraceTask(t5TraceTask);
    }

    /**
     * 修改课题五-追溯任务主
     * 
     * @param t5TraceTask 课题五-追溯任务主
     * @return 结果
     */
    @Override
    public int updateT5TraceTask(T5TraceTask t5TraceTask)
    {
        t5TraceTask.setUpdateTime(DateUtils.getNowDate());
        return t5TraceTaskMapper.updateT5TraceTask(t5TraceTask);
    }

    /**
     * 批量删除课题五-追溯任务主
     * 
     * @param taskIds 需要删除的课题五-追溯任务主主键
     * @return 结果
     */
    @Override
    public int deleteT5TraceTaskByTaskIds(Long[] taskIds)
    {
        return t5TraceTaskMapper.deleteT5TraceTaskByTaskIds(taskIds);
    }

    /**
     * 删除课题五-追溯任务主信息
     * 
     * @param taskId 课题五-追溯任务主主键
     * @return 结果
     */
    @Override
    public int deleteT5TraceTaskByTaskId(Long taskId)
    {
        return t5TraceTaskMapper.deleteT5TraceTaskByTaskId(taskId);
    }
}
