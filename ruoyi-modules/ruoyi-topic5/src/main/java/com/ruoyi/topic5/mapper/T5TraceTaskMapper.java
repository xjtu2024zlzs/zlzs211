package com.ruoyi.topic5.mapper;

import java.util.List;
import com.ruoyi.topic5.domain.T5TraceTask;

/**
 * 课题五-追溯任务主Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface T5TraceTaskMapper 
{
    /**
     * 查询课题五-追溯任务主
     * 
     * @param taskId 课题五-追溯任务主主键
     * @return 课题五-追溯任务主
     */
    public T5TraceTask selectT5TraceTaskByTaskId(Long taskId);

    /**
     * 查询课题五-追溯任务主列表
     * 
     * @param t5TraceTask 课题五-追溯任务主
     * @return 课题五-追溯任务主集合
     */
    public List<T5TraceTask> selectT5TraceTaskList(T5TraceTask t5TraceTask);

    /**
     * 新增课题五-追溯任务主
     * 
     * @param t5TraceTask 课题五-追溯任务主
     * @return 结果
     */
    public int insertT5TraceTask(T5TraceTask t5TraceTask);

    /**
     * 修改课题五-追溯任务主
     * 
     * @param t5TraceTask 课题五-追溯任务主
     * @return 结果
     */
    public int updateT5TraceTask(T5TraceTask t5TraceTask);

    /**
     * 删除课题五-追溯任务主
     * 
     * @param taskId 课题五-追溯任务主主键
     * @return 结果
     */
    public int deleteT5TraceTaskByTaskId(Long taskId);

    /**
     * 批量删除课题五-追溯任务主
     * 
     * @param taskIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteT5TraceTaskByTaskIds(Long[] taskIds);
}
