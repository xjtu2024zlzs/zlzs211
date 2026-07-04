package com.ruoyi.project3.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.project3.client.PythonAlgorithmClient;
import com.ruoyi.project3.config.PythonAlgorithmProperties;
import com.ruoyi.project3.domain.algorithm.AlgTaskResult;
import com.ruoyi.project3.mapper.AlgTaskMapper;
import com.ruoyi.project3.service.PreventiveMaintenanceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class PreventiveMaintenanceServiceImpl implements PreventiveMaintenanceService
{
    private static final String ALGORITHM_TYPE = "PREVENTIVE_MAINTENANCE";
    private static final String TASK_NAME = "设备预防性维护优化";

    @Resource
    private PythonAlgorithmClient pythonAlgorithmClient;

    @Resource
    private PythonAlgorithmProperties pythonAlgorithmProperties;

    @Resource
    private AlgTaskMapper algTaskMapper;

    @Override
    public Map<String, Object> startTask(Map<String, Object> request)
    {
        Map<String, Object> payload = request == null ? new LinkedHashMap<>() : new LinkedHashMap<>(request);
        validate(payload);
        Map<String, Object> task = pythonAlgorithmClient.submitPythonTask(ALGORITHM_TYPE, payload);
        if (!Boolean.TRUE.equals(task.get("success")) || StringUtils.isEmpty(text(task.get("taskId"))))
        {
            throw new ServiceException("Python预防性维护任务提交失败：" + text(task.get("message")));
        }
        saveSubmittedTask(text(task.get("taskId")), payload, task);
        return task;
    }

    @Override
    public Map<String, Object> getTask(String taskId)
    {
        AlgTaskResult existing = requiredRecord(taskId);
        return synchronizeTask(existing);
    }

    @Override
    public Map<String, Object> getHistory(String keyword, String status, Integer pageNum, Integer pageSize)
    {
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        int offset = (safePageNum - 1) * safePageSize;
        List<AlgTaskResult> records = algTaskMapper.getPreventiveMaintenanceResult(
                ALGORITHM_TYPE,
                StringUtils.trim(keyword),
                StringUtils.trim(status),
                offset,
                safePageSize
        );
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AlgTaskResult record : records)
        {
            rows.add(historyRow(record));
        }
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("rows", rows);
        page.put("total", algTaskMapper.countPreventiveMaintenanceResult(
                ALGORITHM_TYPE,
                StringUtils.trim(keyword),
                StringUtils.trim(status)
        ));
        page.put("pageNum", safePageNum);
        page.put("pageSize", safePageSize);
        return page;
    }

    @Override
    public Map<String, Object> getHistoryDetail(String taskId)
    {
        AlgTaskResult record = requiredRecord(taskId);
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("success", true);
        detail.put("taskId", record.getTaskId());
        detail.put("status", record.getStatus());
        detail.put("progress", isTerminal(record.getStatus()) ? 100 : 0);
        detail.put("message", record.getSummary());
        detail.put("error", record.getErrMsg());
        detail.put("createdAt", record.getCreateTime());
        detail.put("startedAt", record.getStartAt());
        detail.put("finishedAt", record.getEndAt());
        detail.put("statusVersion", record.getStatusVersion());
        detail.put("lastSyncAt", record.getLastSyncAt());
        detail.put("lastSyncError", record.getLastSyncError());
        detail.put("syncRetryCount", record.getSyncRetryCount());
        detail.put("result", parseMap(record.getResJson()));
        return detail;
    }

    @Override
    public int deleteHistory(String taskId)
    {
        AlgTaskResult record = requiredRecord(taskId);
        if (!isTerminal(record.getStatus()))
        {
            throw new ServiceException("运行中的预防性维护优化任务不能删除");
        }
        deleteTaskResultFiles(record.getTaskId());
        int deleted = algTaskMapper.deleteByTaskIdAndType(record.getTaskId(), ALGORITHM_TYPE);
        if (deleted <= 0)
        {
            throw new ServiceException("算法执行历史删除失败");
        }
        return deleted;
    }

    private void deleteTaskResultFiles(String taskId)
    {
        if (StringUtils.isEmpty(taskId) || !taskId.matches("^PY[0-9A-Z]+$"))
        {
            throw new ServiceException("任务编号不合法，未删除数据库记录");
        }
        String rootText = pythonAlgorithmProperties.getTaskRoot();
        if (StringUtils.isEmpty(rootText))
        {
            throw new ServiceException("未配置算法任务结果目录 python.algorithm.taskRoot");
        }

        Path root = Paths.get(rootText).toAbsolutePath().normalize();
        Path target = root.resolve(taskId).normalize();
        if (!root.equals(target.getParent()))
        {
            throw new ServiceException("算法任务结果目录不合法，未删除数据库记录");
        }
        if (!Files.exists(target))
        {
            return;
        }

        try (Stream<Path> paths = Files.walk(target))
        {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList())
            {
                Files.deleteIfExists(path);
            }
        }
        catch (Exception e)
        {
            throw new ServiceException("算法结果文件删除失败，未删除数据库记录：" + text(e.getMessage()));
        }
    }

    @Override
    public Map<String, Object> cancelTask(String taskId)
    {
        AlgTaskResult existing = requiredRecord(taskId);
        if ("CANCELED".equals(existing.getStatus()))
        {
            return databaseState(existing, "");
        }
        try
        {
            Map<String, Object> state = pythonAlgorithmClient.cancelPythonTask(existing.getTaskId());
            if (!Boolean.FALSE.equals(state.get("success")))
            {
                syncTaskState(existing, state);
            }
            else
            {
                recordSyncFailure(existing.getTaskId(), firstText(state.get("message"), "Python任务取消失败"));
            }
            return state;
        }
        catch (ServiceException e)
        {
            recordSyncFailure(existing.getTaskId(), e.getMessage());
            return databaseState(existing, e.getMessage());
        }
    }

    @Override
    public int reconcileActiveTasks(Integer limit)
    {
        int safeLimit = limit == null || limit < 1 ? 20 : Math.min(limit, 100);
        List<AlgTaskResult> activeTasks = algTaskMapper.getActiveTasksByType(ALGORITHM_TYPE, safeLimit);
        int synchronizedCount = 0;
        for (AlgTaskResult task : activeTasks)
        {
            synchronizeTask(task);
            synchronizedCount++;
        }
        return synchronizedCount;
    }

    @Override
    public int reconcileActiveTasks(Integer limit, Integer activeTimeoutMinutes, Integer maxRetryCount)
    {
        int safeLimit = limit == null || limit < 1 ? 20 : Math.min(limit, 100);
        int safeTimeout = activeTimeoutMinutes == null || activeTimeoutMinutes < 1 ? 10 : Math.min(activeTimeoutMinutes, 1440);
        int safeMaxRetry = maxRetryCount == null || maxRetryCount < 1 ? 3 : Math.min(maxRetryCount, 100);
        List<AlgTaskResult> activeTasks = algTaskMapper.getActiveTasksByTypeForSync(
                ALGORITHM_TYPE,
                safeTimeout,
                safeMaxRetry,
                safeLimit
        );
        int synchronizedCount = 0;
        for (AlgTaskResult task : activeTasks)
        {
            synchronizeTask(task);
            synchronizedCount++;
        }
        synchronizedCount += failExceededSyncRetries(safeMaxRetry);
        return synchronizedCount;
    }

    @Override
    public int failExceededSyncRetries(Integer maxRetryCount)
    {
        int safeMaxRetry = maxRetryCount == null || maxRetryCount < 1 ? 3 : Math.min(maxRetryCount, 100);
        return algTaskMapper.failActiveTasksExceededSyncRetries(
                ALGORITHM_TYPE,
                safeMaxRetry,
                "Task sync retry limit exceeded"
        );
    }

    private void saveSubmittedTask(String taskId, Map<String, Object> payload, Map<String, Object> task)
    {
        AlgTaskResult record = new AlgTaskResult();
        record.setTaskId(taskId);
        record.setTaskType(ALGORITHM_TYPE);
        record.setTaskName(TASK_NAME);
        record.setStatus(firstText(task.get("status"), "PENDING"));
        record.setRequestId(cut(text(payload.get("taskNo")), 64));
        record.setAlgVersion("EMBKA/BKA");
        record.setEquipmentId(cut(text(payload.get("equipmentId")), 64));
        record.setComponentId(cut(text(payload.get("partInstanceId")), 64));
        record.setBizId(cut(text(payload.get("processExecutionId")), 64));
        record.setBizLevel("processExecution");
        record.setReqJson(JSON.toJSONString(payload));
        record.setSummary(cut(text(task.get("message")), 512));
        if ("RUNNING".equals(record.getStatus()))
        {
            record.setStartAt(new Date());
        }
        algTaskMapper.insertTask(record);
    }

    private Map<String, Object> synchronizeTask(AlgTaskResult existing)
    {
        try
        {
            Map<String, Object> state = pythonAlgorithmClient.getPythonTaskStatus(existing.getTaskId());
            if (Boolean.FALSE.equals(state.get("success")))
            {
                String message = firstText(state.get("message"), "Python任务状态不可用");
                recordSyncFailure(existing.getTaskId(), message);
                return databaseState(existing, message);
            }
            String pythonStatus = text(state.get("status")).toUpperCase();
            if (isStalePythonState(existing, state))
            {
                String message = "Ignored stale Python task state version";
                recordSyncFailure(existing.getTaskId(), message);
                return databaseState(existing, message);
            }
            if (StringUtils.isNotEmpty(pythonStatus) && !canTransition(existing.getStatus(), pythonStatus))
            {
                String message = "拒绝非法状态迁移：" + existing.getStatus() + " -> " + pythonStatus;
                recordSyncFailure(existing.getTaskId(), message);
                return databaseState(existing, message);
            }
            syncTaskState(existing, state);
            return state;
        }
        catch (ServiceException e)
        {
            recordSyncFailure(existing.getTaskId(), e.getMessage());
            return databaseState(existing, e.getMessage());
        }
    }

    private void syncTaskState(AlgTaskResult existing, Map<String, Object> state)
    {
        if (state == null || state.isEmpty())
        {
            recordSyncFailure(existing.getTaskId(), "Python任务状态返回为空");
            return;
        }
        String status = text(state.get("status")).toUpperCase();
        if (StringUtils.isEmpty(status))
        {
            recordSyncFailure(existing.getTaskId(), "Python任务状态缺少 status");
            return;
        }
        if (isStalePythonState(existing, state))
        {
            recordSyncFailure(existing.getTaskId(), "Ignored stale Python task state version");
            return;
        }
        if (!canTransition(existing.getStatus(), status))
        {
            recordSyncFailure(existing.getTaskId(),
                    "拒绝非法状态迁移：" + existing.getStatus() + " -> " + status);
            return;
        }

        AlgTaskResult update = new AlgTaskResult();
        update.setTaskId(existing.getTaskId());
        update.setStatus(status);
        update.setSummary(cut(firstText(state.get("message"), status), 512));
        if ("RUNNING".equals(status) && existing.getStartAt() == null)
        {
            update.setStartAt(new Date());
        }
        if (isTerminal(status))
        {
            update.setEndAt(new Date());
        }
        if ("FAILED".equals(status))
        {
            update.setErrMsg(cut(firstText(state.get("error"), state.get("message")), 2000));
        }

        Map<String, Object> result = mapValue(state.get("result"));
        if (!result.isEmpty())
        {
            Map<String, Object> recommended = mapValue(result.get("recommended"));
            List<?> paretoResults = listValue(result.get("paretoResults"));
            update.setResJson(JSON.toJSONString(result));
            update.setResultVal(cut(text(recommended.get("Cu")), 128));
            update.setResultUnit("cost_rate");
            update.setSummary(cut(
                    "Pareto非支配解 " + paretoResults.size()
                            + " 个，推荐单位成本费率 " + text(recommended.get("Cu")),
                    512
            ));
        }
        if (algTaskMapper.updateTask(update) <= 0)
        {
            recordSyncFailure(existing.getTaskId(), "状态更新未生效，可能已被并发更新");
        }
    }

    private Map<String, Object> historyRow(AlgTaskResult record)
    {
        Map<String, Object> request = parseMap(record.getReqJson());
        Map<String, Object> result = parseMap(record.getResJson());
        Map<String, Object> recommended = mapValue(result.get("recommended"));
        List<?> paretoResults = listValue(result.get("paretoResults"));

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("taskId", record.getTaskId());
        row.put("taskNo", firstText(record.getRequestId(), request.get("taskNo")));
        row.put("equipmentId", firstText(record.getEquipmentId(), request.get("equipmentId")));
        row.put("partInstanceId", firstText(record.getComponentId(), request.get("partInstanceId")));
        row.put("processExecutionId", firstText(record.getBizId(), request.get("processExecutionId")));
        row.put("status", record.getStatus());
        row.put("message", record.getSummary());
        row.put("error", record.getErrMsg());
        row.put("Cu", recommended.get("Cu"));
        row.put("A", recommended.get("A"));
        row.put("minR", recommended.get("minR"));
        row.put("paretoCount", paretoResults.size());
        row.put("createdAt", record.getCreateTime());
        row.put("startedAt", record.getStartAt());
        row.put("finishedAt", record.getEndAt());
        row.put("statusVersion", record.getStatusVersion());
        row.put("lastSyncAt", record.getLastSyncAt());
        row.put("lastSyncError", record.getLastSyncError());
        row.put("syncRetryCount", record.getSyncRetryCount());
        return row;
    }

    static boolean canTransition(String currentStatus, String nextStatus)
    {
        String current = currentStatus == null ? "" : currentStatus.toUpperCase();
        String next = nextStatus == null ? "" : nextStatus.toUpperCase();
        if (current.equals(next))
        {
            return true;
        }
        if ("PENDING".equals(current))
        {
            // Fast tasks may finish before Java observes the transient RUNNING state.
            return "RUNNING".equals(next) || "SUCCESS".equals(next)
                    || "FAILED".equals(next) || "CANCELED".equals(next);
        }
        if ("RUNNING".equals(current))
        {
            return "SUCCESS".equals(next) || "FAILED".equals(next) || "CANCELED".equals(next);
        }
        return false;
    }

    private void recordSyncFailure(String taskId, String message)
    {
        algTaskMapper.recordSyncFailure(taskId, cut(text(message), 1000));
    }

    private Map<String, Object> databaseState(AlgTaskResult record, String syncError)
    {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("success", true);
        state.put("taskId", record.getTaskId());
        state.put("status", record.getStatus());
        state.put("message", firstText(record.getSummary(), record.getStatus()));
        state.put("error", record.getErrMsg());
        state.put("syncError", syncError);
        state.put("result", parseMap(record.getResJson()));
        return state;
    }

    private AlgTaskResult requiredRecord(String taskId)
    {
        String normalizedTaskId = StringUtils.trim(taskId);
        if (StringUtils.isEmpty(normalizedTaskId))
        {
            throw new ServiceException("任务编号不能为空");
        }
        AlgTaskResult record = algTaskMapper.getByTaskId(normalizedTaskId);
        if (record == null || !ALGORITHM_TYPE.equals(record.getTaskType()))
        {
            throw new ServiceException("预防性维护优化历史记录不存在");
        }
        return record;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMap(String json)
    {
        if (StringUtils.isEmpty(json))
        {
            return new LinkedHashMap<>();
        }
        try
        {
            return JSON.parseObject(json, Map.class);
        }
        catch (Exception ignored)
        {
            return new LinkedHashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Object value)
    {
        return value instanceof Map ? (Map<String, Object>) value : new LinkedHashMap<>();
    }

    private List<?> listValue(Object value)
    {
        return value instanceof List ? (List<?>) value : List.of();
    }

    private boolean isTerminal(String status)
    {
        return "SUCCESS".equals(status) || "FAILED".equals(status) || "CANCELED".equals(status);
    }

    private boolean isStalePythonState(AlgTaskResult existing, Map<String, Object> state)
    {
        Long pythonVersion = longValue(state.get("version"));
        Long databaseVersion = existing.getStatusVersion();
        return pythonVersion != null
                && databaseVersion != null
                && pythonVersion < databaseVersion
                && isTerminal(existing.getStatus());
    }

    private Long longValue(Object value)
    {
        if (value == null)
        {
            return null;
        }
        try
        {
            return Long.parseLong(String.valueOf(value));
        }
        catch (NumberFormatException ignored)
        {
            return null;
        }
    }

    private String firstText(Object first, Object second)
    {
        String value = text(first);
        return StringUtils.isNotEmpty(value) ? value : text(second);
    }

    private String cut(String value, int maxLength)
    {
        if (value == null || value.length() <= maxLength)
        {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private void validate(Map<String, Object> payload)
    {
        double t1 = requiredNumber(payload, "T1");
        double t2 = requiredNumber(payload, "T2");
        double t3 = requiredNumber(payload, "T3");
        if (!(t1 >= t2 && t2 >= t3))
        {
            throw new ServiceException("决策变量必须满足 T1 >= T2 >= T3");
        }
        positiveInteger(payload, "N1");
        positiveInteger(payload, "N2");
        positiveInteger(payload, "N3");
        positiveInteger(payload, "sampleCount");
        positiveInteger(payload, "population");
        positiveInteger(payload, "iterations");
        double reliability = requiredNumber(payload, "Rm");
        if (reliability < 0 || reliability > 1)
        {
            throw new ServiceException("可靠度阈值 Rm 必须在 0 到 1 之间");
        }
        double attackThreshold = requiredNumber(payload, "attackThreshold");
        if (attackThreshold < 0 || attackThreshold > 1)
        {
            throw new ServiceException("攻击行为阈值 p 必须在 0 到 1 之间");
        }
    }

    private int positiveInteger(Map<String, Object> payload, String key)
    {
        double value = requiredNumber(payload, key);
        if (value <= 0 || value != Math.rint(value))
        {
            throw new ServiceException(key + " 必须为正整数");
        }
        return (int) value;
    }

    private double requiredNumber(Map<String, Object> payload, String key)
    {
        Object value = payload.get(key);
        if (value == null)
        {
            throw new ServiceException(key + " 不能为空");
        }
        try
        {
            return Double.parseDouble(String.valueOf(value));
        }
        catch (NumberFormatException e)
        {
            throw new ServiceException(key + " 必须为数值");
        }
    }

    private String text(Object value)
    {
        return value == null ? "" : String.valueOf(value);
    }
}
