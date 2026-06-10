package com.ruoyi.designtask1.mapper;

import java.util.List;
import com.ruoyi.designtask1.domain.DesignTask;

public interface DesignTaskMapper {

    List<DesignTask> selectTaskList(DesignTask task);

    DesignTask selectTaskById(Long taskId);

    DesignTask selectTaskByNo(String taskNo);

    int insertTask(DesignTask task);

    int updateTask(DesignTask task);

    int deleteTaskById(Long taskId);

    int deleteTaskByIds(Long[] taskIds);

    DesignTask checkTaskNameUnique(String taskName);

    List<DesignTask> selectTaskByStatus(String status);
}