package com.ruoyi.project3.service;

import java.util.Map;

public interface PreventiveMaintenanceService
{
    Map<String, Object> startTask(Map<String, Object> request);

    Map<String, Object> getTask(String taskId);

    Map<String, Object> getHistory(String keyword, String status, Integer pageNum, Integer pageSize);

    Map<String, Object> getHistoryDetail(String taskId);

    int deleteHistory(String taskId);

    Map<String, Object> cancelTask(String taskId);

    int reconcileActiveTasks(Integer limit);

    int reconcileActiveTasks(Integer limit, Integer activeTimeoutMinutes, Integer maxRetryCount);

    int failExceededSyncRetries(Integer maxRetryCount);
}
