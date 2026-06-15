package com.ruoyi.designtask1.service;

import java.util.List;
import com.ruoyi.designtask1.domain.DesignTaskLog;

public interface IDesignTaskLogService {

    List<DesignTaskLog> selectLogList(DesignTaskLog log);

    List<DesignTaskLog> selectLogsByTaskId(Long taskId);

    int insertLog(DesignTaskLog log);

    int deleteLogById(Long logId);

    int deleteLogsByTaskId(Long taskId);
}