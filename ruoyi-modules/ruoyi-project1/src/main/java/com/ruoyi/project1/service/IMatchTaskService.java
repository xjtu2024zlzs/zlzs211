package com.ruoyi.project1.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.project1.domain.MatchTask;

/**
 * 模式映射任务创建Service接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface IMatchTaskService 
{
    /**
     * 查询模式映射任务创建
     * 
     * @param taskId 模式映射任务创建主键
     * @return 模式映射任务创建
     */
    public MatchTask selectMatchTaskByTaskId(Long taskId);

    /**
     * 查询模式映射任务创建列表
     * 
     * @param matchTask 模式映射任务创建
     * @return 模式映射任务创建集合
     */
    public List<MatchTask> selectMatchTaskList(MatchTask matchTask);

    /**
     * 新增模式映射任务创建
     * 
     * @param matchTask 模式映射任务创建
     * @return 结果
     */
    public int insertMatchTask(MatchTask matchTask);

    /**
     * 修改模式映射任务创建
     * 
     * @param matchTask 模式映射任务创建
     * @return 结果
     */
    public int updateMatchTask(MatchTask matchTask);

    /**
     * 批量删除模式映射任务创建
     * 
     * @param taskIds 需要删除的模式映射任务创建主键集合
     * @return 结果
     */
    public int deleteMatchTaskByTaskIds(Long[] taskIds);

    /**
     * 删除模式映射任务创建信息
     * 
     * @param taskId 模式映射任务创建主键
     * @return 结果
     */
    public int deleteMatchTaskByTaskId(Long taskId);

    /**
     * Run matching task without external algorithm. This creates a default result set
     * that lets the access flow continue for midterm demonstration.
     *
     * @param taskId task id
     * @return run result payload
     */
    public Map<String, Object> runWithoutAlgorithm(Long taskId);

    /**
     * Query task version rows for page display.
     *
     * @param taskId task id
     * @return version payload
     */
    public Map<String, Object> versions(Long taskId);

    /**
     * Query task run record rows for page display.
     *
     * @param taskId task id
     * @return record payload
     */
    public Map<String, Object> records(Long taskId);
}
