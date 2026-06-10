package com.ruoyi.designtask.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.designtask.api.domain.DesignTask;
import com.ruoyi.designtask.api.domain.DesignTemplate;
import com.ruoyi.designtask.api.factory.RemoteDesignTaskFallbackFactory;

@FeignClient(contextId = "remoteDesignTaskService", value = ServiceNameConstants.DESIGN_SERVICE, fallbackFactory = RemoteDesignTaskFallbackFactory.class)
public interface RemoteDesignTaskService {

    @GetMapping("/task/optionselect")
    public R<Object> listTasks(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/task/{taskId}")
    public R<Object> getTask(@PathVariable("taskId") Long taskId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/task")
    public R<Object> addTask(@RequestBody DesignTask task, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PutMapping("/task/{taskId}/submit")
    public R<Object> submitTask(@PathVariable("taskId") Long taskId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/template/optionselect")
    public R<Object> listTemplates(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/template/{templateId}")
    public R<Object> getTemplate(@PathVariable("templateId") Long templateId, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}