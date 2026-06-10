package com.ruoyi.designtask1.mapper;

import java.util.List;
import com.ruoyi.designtask1.domain.DesignTaskLog;

public interface DesignTaskLogMapper {

    List<DesignTaskLog> selectLogList(DesignTaskLog log);

    List<DesignTaskLog> selectLogsByTaskId(Long taskId);

    int insertLog(DesignTaskLog log);

    int deleteLogById(Long logId);

    int deleteLogsByTaskId(Long taskId);
}