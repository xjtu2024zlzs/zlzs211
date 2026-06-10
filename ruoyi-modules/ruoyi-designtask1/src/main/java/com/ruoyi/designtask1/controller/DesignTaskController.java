package com.ruoyi.designtask1.controller;

import java.util.List;
import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.designtask1.domain.DesignTask;
import com.ruoyi.designtask1.domain.DesignTaskNode;
import com.ruoyi.designtask1.domain.DesignTaskLog;
import com.ruoyi.designtask1.domain.DesignTemplate;
import com.ruoyi.designtask1.domain.DesignTemplateNode;
import com.ruoyi.designtask1.service.IDesignTaskService;
import com.ruoyi.designtask1.service.IDesignTaskNodeService;
import com.ruoyi.designtask1.service.IDesignTaskLogService;
import com.ruoyi.designtask1.service.IDesignTemplateService;

@RestController
@RequestMapping({"task", "/designtask/task"})
public class DesignTaskController extends BaseController {

    @Autowired
    private IDesignTaskService taskService;

    @Autowired
    private IDesignTaskNodeService taskNodeService;

    @Autowired
    private IDesignTaskLogService taskLogService;

    @Autowired
    private IDesignTemplateService templateService;

    /**
     * 获取任务列表
     */
    @RequiresPermissions("designtask:task:list")
    @GetMapping("/list")
    public TableDataInfo list(DesignTask task) {
        startPage();
        List<DesignTask> list = taskService.selectTaskList(task);
        return getDataTable(list);
    }

    /**
     * 根据任务ID获取详细信息
     */
    @RequiresPermissions("designtask:task:query")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable Long taskId) {
        DesignTask task = taskService.selectTaskById(taskId);
        if (StringUtils.isNull(task)) {
            return error("任务不存在");
        }
        // 获取任务节点列表
        List<DesignTaskNode> nodes = taskNodeService.selectNodesByTaskId(taskId);
        return success().put("task", task).put("nodes", nodes);
    }

    /**
     * 新增任务
     */
    @RequiresPermissions("designtask:task:add")
    @Log(title = "设计任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DesignTask task) {
        task.setCreateBy(SecurityUtils.getUsername());
        // 设置默认状态
        if (StringUtils.isNull(task.getStatus())) {
            task.setStatus("RUNNING");
        }
        int rows = taskService.insertTask(task);
        if (rows > 0) {
            Long taskId = task.getTaskId();
            // 根据模板生成任务节点
            if (task.getTemplateId() != null) {
                DesignTemplate template = templateService.selectTemplateById(task.getTemplateId());
                if (template != null && StringUtils.isNotNull(template.getTemplateConfig())) {
                    try {
                        List<DesignTemplateNode> templateNodes = JSON.parseArray(
                            template.getTemplateConfig(), DesignTemplateNode.class);
                        if (templateNodes != null && !templateNodes.isEmpty()) {
                            for (DesignTemplateNode tNode : templateNodes) {
                                DesignTaskNode taskNode = new DesignTaskNode();
                                taskNode.setTaskId(taskId);
                                taskNode.setTemplateNodeId(tNode.getNodeId());
                                taskNode.setNodeCode(tNode.getNodeCode());
                                taskNode.setNodeName(tNode.getNodeName());
                                taskNode.setNodeOrder(tNode.getNodeOrder());
                                taskNode.setParallelGroup(tNode.getParallelGroup());
                                taskNode.setStatus("PENDING");
                                taskNode.setCreateBy(SecurityUtils.getUsername());
                                taskNodeService.insertTaskNode(taskNode);
                            }
                        }
                    } catch (Exception e) {
                        // JSON解析失败，跳过节点生成
                    }
                }
            }
            // 记录日志
            DesignTaskLog log = new DesignTaskLog();
            log.setTaskId(taskId);
            log.setAction("CREATE");
            log.setContent("任务创建");
            log.setOperatorId(SecurityUtils.getUserId());
            log.setOperatorName(SecurityUtils.getUsername());
            taskLogService.insertLog(log);
        }
        return toAjax(rows);
    }

    /**
     * 修改任务
     */
    @RequiresPermissions("designtask:task:edit")
    @Log(title = "设计任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DesignTask task) {
        task.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(taskService.updateTask(task));
    }

    /**
     * 删除任务
     */
    @RequiresPermissions("designtask:task:remove")
    @Log(title = "设计任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds) {
        return toAjax(taskService.deleteTaskByIds(taskIds));
    }

    /**
     * 任务提交（更新状态）
     */
    @RequiresPermissions("designtask:task:edit")
    @Log(title = "设计任务", businessType = BusinessType.UPDATE)
    @PostMapping("/{taskId}/submit")
    public AjaxResult submitTask(@PathVariable Long taskId) {
        DesignTask task = taskService.selectTaskById(taskId);
        if (StringUtils.isNull(task)) {
            return error("任务不存在");
        }
        task.setStatus("RUNNING");
        task.setUpdateBy(SecurityUtils.getUsername());
        int rows = taskService.updateTask(task);
        if (rows > 0) {
            DesignTaskLog log = new DesignTaskLog();
            log.setTaskId(taskId);
            log.setAction("SUBMIT");
            log.setContent("任务提交");
            log.setOperatorId(SecurityUtils.getUserId());
            log.setOperatorName(SecurityUtils.getUsername());
            taskLogService.insertLog(log);
        }
        return toAjax(rows);
    }

    /**
     * 获取任务操作日志
     */
    @RequiresPermissions("designtask:task:query")
    @GetMapping("/{taskId}/logs")
    public AjaxResult getLogs(@PathVariable Long taskId) {
        List<DesignTaskLog> logs = taskLogService.selectLogsByTaskId(taskId);
        return success(logs);
    }

    /**
     * 获取任务类型列表
     */
    @GetMapping("/types")
    public AjaxResult getTaskTypes() {
        return success(List.of(
            java.util.Map.of("taskType", "STRUCTURE", "typeName", "结构设计", "typeDesc", "结构优化、强度分析和结构校核", "color", "#409EFF", "icon", "S", "collaboratorRoles", "LAYOUT,MANUFACTURE"),
            java.util.Map.of("taskType", "LAYOUT", "typeName", "布局设计", "typeDesc", "整机布局、装配关系和空间约束", "color", "#67C23A", "icon", "L", "collaboratorRoles", "STRUCTURE,HYDRAULIC"),
            java.util.Map.of("taskType", "MANUFACTURE", "typeName", "工艺设计", "typeDesc", "制造工艺、加工约束和工艺路线", "color", "#E6A23C", "icon", "M", "collaboratorRoles", "STRUCTURE,LAYOUT")
        ));
    }

    /**
     * 取消任务
     */
    @RequiresPermissions("designtask:task:edit")
    @Log(title = "设计任务", businessType = BusinessType.UPDATE)
    @PutMapping("/{taskId}/cancel")
    public AjaxResult cancelTask(@PathVariable Long taskId, @RequestBody(required = false) DesignTask task) {
        DesignTask existing = taskService.selectTaskById(taskId);
        if (StringUtils.isNull(existing)) {
            return error("任务不存在");
        }
        DesignTask update = new DesignTask();
        update.setTaskId(taskId);
        update.setStatus("CANCELLED");
        update.setUpdateBy(SecurityUtils.getUsername());
        int rows = taskService.updateTask(update);
        if (rows > 0) {
            DesignTaskLog log = new DesignTaskLog();
            log.setTaskId(taskId);
            log.setAction("CANCEL");
            log.setContent(task != null && StringUtils.isNotEmpty(task.getDescription()) ? task.getDescription() : "任务取消");
            log.setOperatorId(SecurityUtils.getUserId());
            log.setOperatorName(SecurityUtils.getUsername());
            taskLogService.insertLog(log);
        }
        return toAjax(rows);
    }

    /**
     * 完成任务节点
     */
    @RequiresPermissions("designtask:task:edit")
    @Log(title = "任务节点", businessType = BusinessType.UPDATE)
    @PutMapping("/node/{nodeId}/complete")
    public AjaxResult completeNode(@PathVariable Long nodeId,
            @RequestBody(required = false) DesignTaskNode taskNode) {
        if (taskNode == null) {
            taskNode = new DesignTaskNode();
        }
        taskNode.setId(nodeId);
        taskNode.setStatus("COMPLETED");
        taskNode.setUpdateBy(SecurityUtils.getUsername());
        int rows = taskNodeService.updateTaskNode(taskNode);
        return toAjax(rows);
    }
}
