package com.ruoyi.designtask1.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.designtask1.mapper.DesignTaskMapper;
import com.ruoyi.designtask1.domain.DesignTask;
import com.ruoyi.designtask1.service.IDesignTaskService;

@Service
public class DesignTaskServiceImpl implements IDesignTaskService {

    @Autowired
    private DesignTaskMapper taskMapper;

    @Override
    public List<DesignTask> selectTaskList(DesignTask task) {
        return taskMapper.selectTaskList(task);
    }

    @Override
    public DesignTask selectTaskById(Long taskId) {
        return taskMapper.selectTaskById(taskId);
    }

    @Override
    public DesignTask selectTaskByNo(String taskNo) {
        return taskMapper.selectTaskByNo(taskNo);
    }

    @Override
    @Transactional
    public int insertTask(DesignTask task) {
        return taskMapper.insertTask(task);
    }

    @Override
    @Transactional
    public int updateTask(DesignTask task) {
        return taskMapper.updateTask(task);
    }

    @Override
    @Transactional
    public int deleteTaskById(Long taskId) {
        return taskMapper.deleteTaskById(taskId);
    }

    @Override
    @Transactional
    public int deleteTaskByIds(Long[] taskIds) {
        return taskMapper.deleteTaskByIds(taskIds);
    }

    @Override
    public List<DesignTask> selectTaskByStatus(String status) {
        return taskMapper.selectTaskByStatus(status);
    }
}