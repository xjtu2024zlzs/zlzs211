package com.ruoyi.project3.service;

import com.ruoyi.project3.domain.PageRows;

import java.util.List;
import java.util.Map;

public interface FeedbackWarningService {

    PageRows partPage(
            String keyword,
            String riskLevel,
            String sortBy,
            String sortOrder,
            Integer pageNum,
            Integer pageSize
    );

    PageRows partInstances(
            String partTemplateId,
            Integer pageNum,
            Integer pageSize
    );

    List<Map<String, Object>> partProc(String partId);

    Map<String, Object> createProcessExecution(Map<String, Object> row);

    void updateProcessExecution(String processExecId, Map<String, Object> row);

    void deleteProcessExecution(String processExecId);

    String partImage(String partId);

    void updatePartImage(String partId, String imageUrl);

    PageRows devPage(
            String keyword,
            String riskLevel,
            Integer pageNum,
            Integer pageSize
    );

    List<Map<String, Object>> devPart(String deviceId);

    Map<String, Object> startDetect(String requestBody);

    Map<String, Object> kqcMining(String requestBody);

    Map<String, Object> kqcMiningStatus(String taskId);

    Map<String, Object> cancelKqcMining(String taskId);

    Map<String, Object> detectStatus(String taskId);

    List<Map<String, Object>> detectLogs(String taskId);

    Map<String, Object> cancelDetect(String taskId);

    PageRows detectResults(
            String status,
            String targetType,
            String targetId,
            String beginTime,
            String endTime,
            Integer pageNum,
            Integer pageSize
    );

    PageRows kqcMiningResults(
            String keyword,
            String status,
            Integer pageNum,
            Integer pageSize
    );

    Map<String, Object> deleteAlgorithmResult(String taskId);
}
