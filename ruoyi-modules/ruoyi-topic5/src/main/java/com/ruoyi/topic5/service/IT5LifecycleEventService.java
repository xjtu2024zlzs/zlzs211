package com.ruoyi.topic5.service;

import java.util.List;
import com.ruoyi.topic5.domain.T5LifecycleEvent;

/**
 * 课题五-零部件生命周期事件Service接口
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
public interface IT5LifecycleEventService 
{
    /**
     * 查询课题五-零部件生命周期事件
     * 
     * @param eventId 课题五-零部件生命周期事件主键
     * @return 课题五-零部件生命周期事件
     */
    public T5LifecycleEvent selectT5LifecycleEventByEventId(Long eventId);

    /**
     * 查询课题五-零部件生命周期事件列表
     * 
     * @param t5LifecycleEvent 课题五-零部件生命周期事件
     * @return 课题五-零部件生命周期事件集合
     */
    public List<T5LifecycleEvent> selectT5LifecycleEventList(T5LifecycleEvent t5LifecycleEvent);

    /**
     * 新增课题五-零部件生命周期事件
     * 
     * @param t5LifecycleEvent 课题五-零部件生命周期事件
     * @return 结果
     */
    public int insertT5LifecycleEvent(T5LifecycleEvent t5LifecycleEvent);

    /**
     * 修改课题五-零部件生命周期事件
     * 
     * @param t5LifecycleEvent 课题五-零部件生命周期事件
     * @return 结果
     */
    public int updateT5LifecycleEvent(T5LifecycleEvent t5LifecycleEvent);

    /**
     * 批量删除课题五-零部件生命周期事件
     * 
     * @param eventIds 需要删除的课题五-零部件生命周期事件主键集合
     * @return 结果
     */
    public int deleteT5LifecycleEventByEventIds(Long[] eventIds);

    /**
     * 删除课题五-零部件生命周期事件信息
     * 
     * @param eventId 课题五-零部件生命周期事件主键
     * @return 结果
     */
    public int deleteT5LifecycleEventByEventId(Long eventId);
}
