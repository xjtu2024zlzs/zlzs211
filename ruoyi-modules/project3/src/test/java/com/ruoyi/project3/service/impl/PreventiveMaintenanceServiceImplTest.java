package com.ruoyi.project3.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project3.client.PythonAlgorithmClient;
import com.ruoyi.project3.config.PythonAlgorithmProperties;
import com.ruoyi.project3.domain.algorithm.AlgTaskResult;
import com.ruoyi.project3.mapper.AlgTaskMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreventiveMaintenanceServiceImplTest
{
    @Mock
    private PythonAlgorithmClient pythonAlgorithmClient;

    @Mock
    private PythonAlgorithmProperties pythonAlgorithmProperties;

    @Mock
    private AlgTaskMapper algTaskMapper;

    @InjectMocks
    private PreventiveMaintenanceServiceImpl service;

    @Test
    void startTaskValidatesBeforeCallingPython()
    {
        Map<String, Object> payload = validPayload();
        payload.put("T1", 60);
        payload.put("T2", 70);

        assertThrows(ServiceException.class, () -> service.startTask(payload));

        verify(pythonAlgorithmClient, never()).submitPythonTask(any(), any());
    }

    @Test
    void startTaskPersistsSubmittedTask()
    {
        when(pythonAlgorithmClient.submitPythonTask(eq("PREVENTIVE_MAINTENANCE"), any()))
                .thenReturn(Map.of(
                        "success", true,
                        "taskId", "PY001",
                        "status", "PENDING",
                        "message", "submitted"
                ));

        Map<String, Object> result = service.startTask(validPayload());

        assertEquals("PY001", result.get("taskId"));
        ArgumentCaptor<AlgTaskResult> captor = ArgumentCaptor.forClass(AlgTaskResult.class);
        verify(algTaskMapper).insertTask(captor.capture());
        assertEquals("PY001", captor.getValue().getTaskId());
        assertEquals("PREVENTIVE_MAINTENANCE", captor.getValue().getTaskType());
        assertEquals("PENDING", captor.getValue().getStatus());
    }

    @Test
    void getTaskSynchronizesSuccessfulPythonResult()
    {
        AlgTaskResult existing = new AlgTaskResult();
        existing.setTaskId("PY001");
        existing.setTaskType("PREVENTIVE_MAINTENANCE");
        existing.setStatus("RUNNING");
        when(algTaskMapper.getByTaskId("PY001")).thenReturn(existing);
        when(algTaskMapper.updateTask(any())).thenReturn(1);
        when(pythonAlgorithmClient.getPythonTaskStatus("PY001")).thenReturn(Map.of(
                "status", "SUCCESS",
                "message", "done",
                "result", Map.of(
                        "recommended", Map.of("Cu", 12.5),
                        "paretoResults", List.of(Map.of("Cu", 12.5))
                )
        ));

        service.getTask("PY001");

        ArgumentCaptor<AlgTaskResult> captor = ArgumentCaptor.forClass(AlgTaskResult.class);
        verify(algTaskMapper).updateTask(captor.capture());
        assertEquals("SUCCESS", captor.getValue().getStatus());
        assertEquals("12.5", captor.getValue().getResultVal());
        assertEquals("cost_rate", captor.getValue().getResultUnit());
    }

    @Test
    void cancelTaskSynchronizesCanceledState()
    {
        AlgTaskResult existing = new AlgTaskResult();
        existing.setTaskId("PY001");
        existing.setTaskType("PREVENTIVE_MAINTENANCE");
        existing.setStatus("RUNNING");
        when(algTaskMapper.getByTaskId("PY001")).thenReturn(existing);
        when(algTaskMapper.updateTask(any())).thenReturn(1);
        when(pythonAlgorithmClient.cancelPythonTask("PY001")).thenReturn(Map.of(
                "status", "CANCELED",
                "message", "canceled"
        ));

        Map<String, Object> result = service.cancelTask(" PY001 ");

        assertEquals("CANCELED", result.get("status"));
        ArgumentCaptor<AlgTaskResult> captor = ArgumentCaptor.forClass(AlgTaskResult.class);
        verify(algTaskMapper).updateTask(captor.capture());
        assertEquals("PY001", captor.getValue().getTaskId());
        assertEquals("CANCELED", captor.getValue().getStatus());
    }

    @Test
    void supportsOnlyForwardStateTransitions()
    {
        assertTrue(PreventiveMaintenanceServiceImpl.canTransition("PENDING", "RUNNING"));
        assertTrue(PreventiveMaintenanceServiceImpl.canTransition("PENDING", "SUCCESS"));
        assertTrue(PreventiveMaintenanceServiceImpl.canTransition("RUNNING", "SUCCESS"));
        assertTrue(PreventiveMaintenanceServiceImpl.canTransition("RUNNING", "FAILED"));
        assertTrue(PreventiveMaintenanceServiceImpl.canTransition("RUNNING", "CANCELED"));
        assertTrue(PreventiveMaintenanceServiceImpl.canTransition("SUCCESS", "SUCCESS"));
        assertFalse(PreventiveMaintenanceServiceImpl.canTransition("SUCCESS", "RUNNING"));
        assertFalse(PreventiveMaintenanceServiceImpl.canTransition("FAILED", "SUCCESS"));
        assertFalse(PreventiveMaintenanceServiceImpl.canTransition("CANCELED", "SUCCESS"));
        assertFalse(PreventiveMaintenanceServiceImpl.canTransition("RUNNING", "PENDING"));
    }

    @Test
    void getTaskSynchronizesRunningState()
    {
        AlgTaskResult existing = task("PENDING");
        when(algTaskMapper.getByTaskId("PY001")).thenReturn(existing);
        when(algTaskMapper.updateTask(any())).thenReturn(1);
        when(pythonAlgorithmClient.getPythonTaskStatus("PY001"))
                .thenReturn(Map.of("status", "RUNNING", "message", "running"));

        service.getTask("PY001");

        ArgumentCaptor<AlgTaskResult> captor = ArgumentCaptor.forClass(AlgTaskResult.class);
        verify(algTaskMapper).updateTask(captor.capture());
        assertEquals("RUNNING", captor.getValue().getStatus());
    }

    @Test
    void getTaskSynchronizesFailedState()
    {
        AlgTaskResult existing = task("RUNNING");
        when(algTaskMapper.getByTaskId("PY001")).thenReturn(existing);
        when(algTaskMapper.updateTask(any())).thenReturn(1);
        when(pythonAlgorithmClient.getPythonTaskStatus("PY001"))
                .thenReturn(Map.of("status", "FAILED", "message", "failed", "error", "invalid input"));

        service.getTask("PY001");

        ArgumentCaptor<AlgTaskResult> captor = ArgumentCaptor.forClass(AlgTaskResult.class);
        verify(algTaskMapper).updateTask(captor.capture());
        assertEquals("FAILED", captor.getValue().getStatus());
        assertEquals("invalid input", captor.getValue().getErrMsg());
    }

    @Test
    void getTaskSynchronizesCanceledState()
    {
        AlgTaskResult existing = task("RUNNING");
        when(algTaskMapper.getByTaskId("PY001")).thenReturn(existing);
        when(algTaskMapper.updateTask(any())).thenReturn(1);
        when(pythonAlgorithmClient.getPythonTaskStatus("PY001"))
                .thenReturn(Map.of("status", "CANCELED", "message", "canceled"));

        service.getTask("PY001");

        ArgumentCaptor<AlgTaskResult> captor = ArgumentCaptor.forClass(AlgTaskResult.class);
        verify(algTaskMapper).updateTask(captor.capture());
        assertEquals("CANCELED", captor.getValue().getStatus());
    }

    @Test
    void terminalStateIsNotOverwrittenByOlderPythonState()
    {
        AlgTaskResult existing = task("SUCCESS");
        existing.setStatusVersion(2L);
        when(algTaskMapper.getByTaskId("PY001")).thenReturn(existing);
        when(pythonAlgorithmClient.getPythonTaskStatus("PY001"))
                .thenReturn(Map.of("status", "RUNNING", "version", 1, "message", "stale"));

        Map<String, Object> result = service.getTask("PY001");

        assertEquals("SUCCESS", result.get("status"));
        assertEquals("Ignored stale Python task state version", result.get("syncError"));
        verify(algTaskMapper, never()).updateTask(any());
        verify(algTaskMapper).recordSyncFailure(eq("PY001"), any());
    }

    @Test
    void missingPythonTaskKeepsTerminalDatabaseState()
    {
        AlgTaskResult existing = task("SUCCESS");
        existing.setSummary("completed");
        when(algTaskMapper.getByTaskId("PY001")).thenReturn(existing);
        when(pythonAlgorithmClient.getPythonTaskStatus("PY001"))
                .thenReturn(Map.of("success", false, "message", "任务不存在"));

        Map<String, Object> result = service.getTask("PY001");

        assertEquals("SUCCESS", result.get("status"));
        assertEquals("任务不存在", result.get("syncError"));
        verify(algTaskMapper, never()).updateTask(any());
        verify(algTaskMapper).recordSyncFailure("PY001", "任务不存在");
    }

    @Test
    void pythonTimeoutKeepsTerminalDatabaseState()
    {
        AlgTaskResult existing = task("FAILED");
        when(algTaskMapper.getByTaskId("PY001")).thenReturn(existing);
        when(pythonAlgorithmClient.getPythonTaskStatus("PY001"))
                .thenThrow(new ServiceException("Python算法任务状态查询超时"));

        Map<String, Object> result = service.getTask("PY001");

        assertEquals("FAILED", result.get("status"));
        verify(algTaskMapper, never()).updateTask(any());
        verify(algTaskMapper).recordSyncFailure(eq("PY001"), any());
    }

    @Test
    void repeatedCancelDoesNotCallPythonAgain()
    {
        AlgTaskResult existing = task("CANCELED");
        when(algTaskMapper.getByTaskId("PY001")).thenReturn(existing);

        Map<String, Object> first = service.cancelTask("PY001");
        Map<String, Object> second = service.cancelTask("PY001");

        assertEquals("CANCELED", first.get("status"));
        assertEquals("CANCELED", second.get("status"));
        verify(pythonAlgorithmClient, never()).cancelPythonTask(any());
        verify(algTaskMapper, times(2)).getByTaskId("PY001");
    }

    @Test
    void reconcileLimitsAndSynchronizesActiveTasks()
    {
        AlgTaskResult first = task("PENDING");
        AlgTaskResult second = task("RUNNING");
        second.setTaskId("PY002");
        when(algTaskMapper.getActiveTasksByType("PREVENTIVE_MAINTENANCE", 2))
                .thenReturn(List.of(first, second));
        when(algTaskMapper.updateTask(any())).thenReturn(1);
        when(pythonAlgorithmClient.getPythonTaskStatus("PY001"))
                .thenReturn(Map.of("status", "RUNNING"));
        when(pythonAlgorithmClient.getPythonTaskStatus("PY002"))
                .thenReturn(Map.of("status", "SUCCESS"));

        assertEquals(2, service.reconcileActiveTasks(2));
        verify(algTaskMapper, times(2)).updateTask(any());
    }

    @Test
    void scheduledReconcileUsesTimeoutRetryWindowAndFailsExceededRetries()
    {
        AlgTaskResult active = task("RUNNING");
        when(algTaskMapper.getActiveTasksByTypeForSync("PREVENTIVE_MAINTENANCE", 15, 4, 3))
                .thenReturn(List.of(active));
        when(pythonAlgorithmClient.getPythonTaskStatus("PY001"))
                .thenReturn(Map.of("status", "SUCCESS", "version", 3));
        when(algTaskMapper.updateTask(any())).thenReturn(1);
        when(algTaskMapper.failActiveTasksExceededSyncRetries(
                eq("PREVENTIVE_MAINTENANCE"),
                eq(4),
                eq("Task sync retry limit exceeded")
        )).thenReturn(2);

        assertEquals(3, service.reconcileActiveTasks(3, 15, 4));
        verify(algTaskMapper).getActiveTasksByTypeForSync("PREVENTIVE_MAINTENANCE", 15, 4, 3);
        verify(algTaskMapper).failActiveTasksExceededSyncRetries(
                "PREVENTIVE_MAINTENANCE",
                4,
                "Task sync retry limit exceeded"
        );
    }

    private AlgTaskResult task(String status)
    {
        AlgTaskResult task = new AlgTaskResult();
        task.setTaskId("PY001");
        task.setTaskType("PREVENTIVE_MAINTENANCE");
        task.setStatus(status);
        return task;
    }

    private Map<String, Object> validPayload()
    {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("T1", 90);
        payload.put("T2", 80);
        payload.put("T3", 70);
        payload.put("N1", 2);
        payload.put("N2", 2);
        payload.put("N3", 2);
        payload.put("sampleCount", 5);
        payload.put("population", 4);
        payload.put("iterations", 1);
        payload.put("Rm", 0.75);
        payload.put("attackThreshold", 0.3);
        return payload;
    }
}
