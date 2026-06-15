package com.ruoyi.designtask1.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.designtask1.mapper.DesignTaskLogMapper;
import com.ruoyi.designtask1.domain.DesignTaskLog;
import com.ruoyi.designtask1.service.IDesignTaskLogService;

@Service
public class DesignTaskLogServiceImpl implements IDesignTaskLogService {

    @Autowired
    private DesignTaskLogMapper logMapper;

    @Override
    public List<DesignTaskLog> selectLogList(DesignTaskLog log) {
        return logMapper.selectLogList(log);
    }

    @Override
    public List<DesignTaskLog> selectLogsByTaskId(Long taskId) {
        return logMapper.selectLogsByTaskId(taskId);
    }

    @Override
    @Transactional
    public int insertLog(DesignTaskLog log) {
        return logMapper.insertLog(log);
    }

    @Override
    @Transactional
    public int deleteLogById(Long logId) {
        return logMapper.deleteLogById(logId);
    }

    @Override
    @Transactional
    public int deleteLogsByTaskId(Long taskId) {
        return logMapper.deleteLogsByTaskId(taskId);
    }
}