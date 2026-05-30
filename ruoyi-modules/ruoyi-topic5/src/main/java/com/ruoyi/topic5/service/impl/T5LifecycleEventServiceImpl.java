package com.ruoyi.topic5.service.impl;

import java.util.List;
import com.ruoyi.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.topic5.mapper.T5LifecycleEventMapper;
import com.ruoyi.topic5.domain.T5LifecycleEvent;
import com.ruoyi.topic5.service.IT5LifecycleEventService;

/**
 * 课题五-零部件生命周期事件Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@Service
public class T5LifecycleEventServiceImpl implements IT5LifecycleEventService 
{
    @Autowired
    private T5LifecycleEventMapper t5LifecycleEventMapper;

    /**
     * 查询课题五-零部件生命周期事件
     * 
     * @param eventId 课题五-零部件生命周期事件主键
     * @return 课题五-零部件生命周期事件
     */
    @Override
    public T5LifecycleEvent selectT5LifecycleEventByEventId(Long eventId)
    {
        return t5LifecycleEventMapper.selectT5LifecycleEventByEventId(eventId);
    }

    /**
     * 查询课题五-零部件生命周期事件列表
     * 
     * @param t5LifecycleEvent 课题五-零部件生命周期事件
     * @return 课题五-零部件生命周期事件
     */
    @Override
    public List<T5LifecycleEvent> selectT5LifecycleEventList(T5LifecycleEvent t5LifecycleEvent)
    {
        return t5LifecycleEventMapper.selectT5LifecycleEventList(t5LifecycleEvent);
    }

    /**
     * 新增课题五-零部件生命周期事件
     * 
     * @param t5LifecycleEvent 课题五-零部件生命周期事件
     * @return 结果
     */
    @Override
    public int insertT5LifecycleEvent(T5LifecycleEvent t5LifecycleEvent)
    {
        t5LifecycleEvent.setCreateTime(DateUtils.getNowDate());
        return t5LifecycleEventMapper.insertT5LifecycleEvent(t5LifecycleEvent);
    }

    /**
     * 修改课题五-零部件生命周期事件
     * 
     * @param t5LifecycleEvent 课题五-零部件生命周期事件
     * @return 结果
     */
    @Override
    public int updateT5LifecycleEvent(T5LifecycleEvent t5LifecycleEvent)
    {
        t5LifecycleEvent.setUpdateTime(DateUtils.getNowDate());
        return t5LifecycleEventMapper.updateT5LifecycleEvent(t5LifecycleEvent);
    }

    /**
     * 批量删除课题五-零部件生命周期事件
     * 
     * @param eventIds 需要删除的课题五-零部件生命周期事件主键
     * @return 结果
     */
    @Override
    public int deleteT5LifecycleEventByEventIds(Long[] eventIds)
    {
        return t5LifecycleEventMapper.deleteT5LifecycleEventByEventIds(eventIds);
    }

    /**
     * 删除课题五-零部件生命周期事件信息
     * 
     * @param eventId 课题五-零部件生命周期事件主键
     * @return 结果
     */
    @Override
    public int deleteT5LifecycleEventByEventId(Long eventId)
    {
        return t5LifecycleEventMapper.deleteT5LifecycleEventByEventId(eventId);
    }
}
