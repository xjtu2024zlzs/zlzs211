package com.ruoyi.project3.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.project3.domain.algorithm.DegradationTaskRequest;
import com.ruoyi.project3.domain.algorithm.FaultIdentifyStartRequest;
import com.ruoyi.project3.service.FaultIdentifyService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/service/identify")
public class FaultIdentifyController
{
    private static final Logger log = LoggerFactory.getLogger(FaultIdentifyController.class);

    @Resource
    private FaultIdentifyService faultIdentifyService;

    @GetMapping("/import_records")
    public AjaxResult import_records(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page_num", required = false, defaultValue = "1") Integer page_num,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer page_size
    )
    {
        return AjaxResult.success(faultIdentifyService.get_import_record(keyword, page_num, page_size));
    }

    @GetMapping("/results")
    public AjaxResult results(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "task_type", required = false) String taskType,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page_num", required = false, defaultValue = "1") Integer page_num,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer page_size
    )
    {
        return AjaxResult.success(faultIdentifyService.get_identify_result(keyword, taskType, status, page_num, page_size));
    }

    @DeleteMapping("/results/{taskId}")
    public AjaxResult delete_result(
            @PathVariable("taskId") String taskId,
            @RequestBody(required = false) Map<String, Object> options
    )
    {
        return AjaxResult.success(faultIdentifyService.delete_identify_result(taskId, options));
    }

    @GetMapping("/feature_results")
    public AjaxResult feature_results(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page_num", required = false, defaultValue = "1") Integer page_num,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer page_size
    )
    {
        return AjaxResult.success(faultIdentifyService.get_feature_result(keyword, status, page_num, page_size));
    }

    @GetMapping("/degradation_results")
    public AjaxResult degradation_results(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page_num", required = false, defaultValue = "1") Integer page_num,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer page_size
    )
    {
        return AjaxResult.success(faultIdentifyService.get_degradation_result(keyword, status, page_num, page_size));
    }

    @GetMapping("/key_process_results")
    public AjaxResult key_process_results(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "target_type", required = false) String targetType,
            @RequestParam(value = "target_id", required = false) String targetId,
            @RequestParam(value = "page_num", required = false, defaultValue = "1") Integer page_num,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer page_size
    )
    {
        return AjaxResult.success(faultIdentifyService.get_key_process_result(keyword, status, targetType, targetId, page_num, page_size));
    }

    @PostMapping("/feature_tasks")
    public AjaxResult submit_feature_task(@RequestBody(required = false) String request_body)
    {
        return AjaxResult.success(faultIdentifyService.submit_feature(request_body));
    }

    @PostMapping("/start_analysis")
    public AjaxResult start_analysis(@RequestBody(required = false) FaultIdentifyStartRequest request_data)
    {
        String businessObjectId = request_data == null ? null : request_data.getBizId();
        log.info("收到特征分析启动请求，业务对象ID={}", businessObjectId);
        Map<String, Object> startResult = faultIdentifyService.start_analysis(request_data);
        Object taskId = startResult == null ? null : (startResult.get("taskId") == null ? startResult.get("task_id") : startResult.get("taskId"));
        log.info("特征分析启动请求处理完成，业务对象ID={}，任务ID={}", businessObjectId, taskId);
        return AjaxResult.success(startResult);
    }

    @PostMapping("/key_process_tasks")
    public AjaxResult submit_key_process_task(@RequestBody(required = false) FaultIdentifyStartRequest request_data)
    {
        return AjaxResult.success(faultIdentifyService.submit_key_process(request_data));
    }

    @GetMapping("/key_process_tasks/{taskId}/status")
    public AjaxResult key_process_task_status(@PathVariable("taskId") String taskId)
    {
        return AjaxResult.success(faultIdentifyService.get_task(taskId));
    }

    @GetMapping("/task_status")
    public AjaxResult task_status(
            @RequestParam(value = "task_id", required = false) String task_id,
            @RequestParam(value = "taskId", required = false) String taskId
    )
    {
        String resolvedTaskId = (task_id != null && !task_id.trim().isEmpty()) ? task_id : taskId;
        return AjaxResult.success(faultIdentifyService.get_task(resolvedTaskId));
    }

    @PostMapping("/degradation_tasks")
    public AjaxResult submit_degradation_task(@RequestBody(required = false) DegradationTaskRequest request)
    {
        return AjaxResult.success(faultIdentifyService.submit_degradation(request));
    }

}
