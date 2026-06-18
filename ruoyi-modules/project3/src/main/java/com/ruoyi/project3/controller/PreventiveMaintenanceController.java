package com.ruoyi.project3.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.project3.service.PreventiveMaintenanceService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/service/preventive-maintenance")
public class PreventiveMaintenanceController
{
    @Resource
    private PreventiveMaintenanceService preventiveMaintenanceService;

    @PostMapping("/tasks")
    public AjaxResult startTask(@RequestBody(required = false) Map<String, Object> request)
    {
        return AjaxResult.success(preventiveMaintenanceService.startTask(request));
    }

    @GetMapping("/tasks/{taskId}")
    public AjaxResult getTask(@PathVariable("taskId") String taskId)
    {
        return AjaxResult.success(preventiveMaintenanceService.getTask(taskId));
    }

    @GetMapping("/history")
    public AjaxResult history(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page_num", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer pageSize
    )
    {
        return AjaxResult.success(preventiveMaintenanceService.getHistory(keyword, status, pageNum, pageSize));
    }

    @GetMapping("/history/{taskId}")
    public AjaxResult historyDetail(@PathVariable("taskId") String taskId)
    {
        return AjaxResult.success(preventiveMaintenanceService.getHistoryDetail(taskId));
    }

    @DeleteMapping("/history/{taskId}")
    public AjaxResult deleteHistory(@PathVariable("taskId") String taskId)
    {
        return AjaxResult.success(preventiveMaintenanceService.deleteHistory(taskId));
    }

    @PostMapping("/tasks/{taskId}/cancel")
    public AjaxResult cancelTask(@PathVariable("taskId") String taskId)
    {
        return AjaxResult.success(preventiveMaintenanceService.cancelTask(taskId));
    }
}
