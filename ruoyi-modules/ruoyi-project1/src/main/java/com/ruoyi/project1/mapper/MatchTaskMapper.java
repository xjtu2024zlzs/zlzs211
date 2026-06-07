package com.ruoyi.project1.mapper;

import java.util.List;
import com.ruoyi.project1.domain.MatchTask;

/**
 * 模式映射任务创建Mapper接口
 * 
 * @author pwb
 * @date 2026-06-05
 */
public interface MatchTaskMapper 
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
     * 删除模式映射任务创建
     * 
     * @param taskId 模式映射任务创建主键
     * @return 结果
     */
    public int deleteMatchTaskByTaskId(Long taskId);

    /**
     * 批量删除模式映射任务创建
     * 
     * @param taskIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMatchTaskByTaskIds(Long[] taskIds);
}
