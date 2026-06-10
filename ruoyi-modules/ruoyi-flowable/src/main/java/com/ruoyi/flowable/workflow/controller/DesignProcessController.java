package com.ruoyi.flowable.workflow.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import lombok.RequiredArgsConstructor;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/design/process")
public class DesignProcessController {

    private static final String PROCESS_KEY = "design_optimization_process";
    private static final String PROCESS_NAME = "起落架舱门优化流程";
    private static final String PROCESS_CATEGORY = "设计制造协同优化";
    private static final String BPMN_RESOURCE = "processes/design_optimization_process.bpmn20.xml";

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    @GetMapping("/ensure")
    public AjaxResult ensureProcessDefinition() throws IOException {
        ProcessDefinition definition = latestDefinition();
        if (definition == null) {
            deployDefaultProcess();
            definition = latestDefinition();
        }
        return AjaxResult.success(definitionPayload(definition));
    }

    @GetMapping("/definitions")
    public AjaxResult definitions() throws IOException {
        if (latestDefinition() == null) {
            deployDefaultProcess();
        }
        List<Map<String, Object>> definitions = repositoryService.createProcessDefinitionQuery()
            .latestVersion()
            .active()
            .orderByProcessDefinitionName()
            .asc()
            .list()
            .stream()
            .map(this::definitionPayload)
            .collect(Collectors.toList());
        return AjaxResult.success(definitions);
    }

    @PostMapping("/deploy-default")
    public AjaxResult deployDefaultProcess() throws IOException {
        ClassPathResource resource = new ClassPathResource(BPMN_RESOURCE);
        try (InputStream inputStream = resource.getInputStream()) {
            Deployment deployment = repositoryService.createDeployment()
                .name(PROCESS_NAME)
                .category(PROCESS_CATEGORY)
                .addInputStream("design_optimization_process.bpmn20.xml", inputStream)
                .deploy();

            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .processDefinitionKey(PROCESS_KEY)
                .singleResult();
            return AjaxResult.success(definitionPayload(definition));
        }
    }

    @GetMapping("/definition/{processDefinitionId}/nodes")
    public AjaxResult definitionNodes(@PathVariable String processDefinitionId) {
        BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
        List<Map<String, Object>> nodes = new ArrayList<>();
        if (model != null) {
            model.getProcesses().forEach(process -> {
                for (FlowElement element : process.getFlowElements()) {
                    if (element instanceof UserTask userTask) {
                        Map<String, Object> node = new LinkedHashMap<>();
                        node.put("nodeKey", userTask.getId());
                        node.put("nodeName", userTask.getName());
                        node.put("assignee", userTask.getAssignee());
                        node.put("candidateUsers", userTask.getCandidateUsers());
                        node.put("candidateGroups", userTask.getCandidateGroups());
                        nodes.add(node);
                    }
                }
            });
        }
        return AjaxResult.success(nodes);
    }

    @PostMapping("/start/{processDefinitionId}")
    public AjaxResult start(@PathVariable String processDefinitionId, @RequestBody Map<String, Object> variables) {
        String businessKey = String.valueOf(variables.getOrDefault("businessKey", variables.get("taskId")));
        ProcessInstance instance = runtimeService.startProcessInstanceById(processDefinitionId, businessKey, variables);
        Task currentTask = taskService.createTaskQuery().processInstanceId(instance.getId()).active().singleResult();
        return AjaxResult.success(runtimePayload(instance.getId(), currentTask));
    }

    @GetMapping("/current-task/{processInstanceId}")
    public AjaxResult currentTask(@PathVariable String processInstanceId) {
        Task currentTask = taskService.createTaskQuery().processInstanceId(processInstanceId).active().singleResult();
        return AjaxResult.success(runtimePayload(processInstanceId, currentTask));
    }

    @PostMapping("/complete")
    public AjaxResult complete(@RequestBody Map<String, Object> body) {
        String taskId = String.valueOf(body.get("taskId"));
        String processInstanceId = String.valueOf(body.get("processInstanceId"));
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = body.get("variables") instanceof Map<?, ?> map
            ? (Map<String, Object>) map
            : Map.of();
        taskService.complete(taskId, variables);
        Task currentTask = taskService.createTaskQuery().processInstanceId(processInstanceId).active().singleResult();
        return AjaxResult.success(runtimePayload(processInstanceId, currentTask));
    }

    private ProcessDefinition latestDefinition() {
        return repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(PROCESS_KEY)
            .latestVersion()
            .singleResult();
    }

    private Map<String, Object> definitionPayload(ProcessDefinition definition) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (definition == null) {
            return data;
        }
        data.put("definitionId", definition.getId());
        data.put("processDefinitionId", definition.getId());
        data.put("processKey", definition.getKey());
        data.put("processName", definition.getName());
        data.put("version", definition.getVersion());
        data.put("category", definition.getCategory() == null ? PROCESS_CATEGORY : definition.getCategory());
        data.put("deploymentId", definition.getDeploymentId());
        return data;
    }

    private Map<String, Object> runtimePayload(String processInstanceId, Task currentTask) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("processInstanceId", processInstanceId);
        if (currentTask != null) {
            data.put("currentTaskId", currentTask.getId());
            data.put("currentNodeKey", currentTask.getTaskDefinitionKey());
            data.put("currentNodeName", currentTask.getName());
            data.put("assignee", currentTask.getAssignee());
        } else {
            data.put("currentTaskId", null);
            data.put("currentNodeKey", "end");
            data.put("currentNodeName", "流程结束");
        }
        return data;
    }
}
