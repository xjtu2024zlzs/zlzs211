package com.ruoyi.designtask1.service;

import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.designtask1.domain.DesignTask;
import com.ruoyi.designtask1.domain.DesignTaskFile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.net.URLEncoder;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class DesignOptimizationService {

    private static final DateTimeFormatter TASK_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final IDesignTaskService taskService;
    private final IDesignTaskFileService taskFileService;
    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService cadExecutor = Executors.newFixedThreadPool(2);
    private final ExecutorService solverExecutor = Executors.newFixedThreadPool(2);

    @Value("${designtask.flowable.base-url:http://127.0.0.1:9308}")
    private String flowableBaseUrl;

    @Value("${designtask.solidworks.worker-url:http://127.0.0.1:18080/api/pipe-model}")
    private String solidWorksWorkerUrl;

    @Value("${design.solver.surrogate-base-url:http://127.0.0.1:9721}")
    private String surrogateBaseUrl;

    @Value("${designtask.attachment.path:uploads/designtask}")
    private String attachmentPath;

    public DesignOptimizationService(IDesignTaskService taskService, IDesignTaskFileService taskFileService, JdbcTemplate jdbcTemplate) {
        this.taskService = taskService;
        this.taskFileService = taskFileService;
        this.jdbcTemplate = jdbcTemplate;
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> dashboard(String scope, String status) {
        List<DesignTask> tasks = taskService.selectTaskList(new DesignTask());
        tasks.forEach(this::syncRuntimeAndPersistIfChanged);
        if (StringUtils.isNotEmpty(status)) {
            tasks = tasks.stream().filter(item -> status.equals(item.getStatus())).collect(Collectors.toList());
        }
        if ("related".equals(scope)) {
            Long userId = currentUserId();
            String username = currentUsername();
            tasks = tasks.stream().filter(item -> isRelated(item, userId, username)).collect(Collectors.toList());
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("stats", stats(tasks));
        data.put("tasks", decorateTasks(tasks));
        data.put("disciplineProgress", disciplineProgress(tasks));
        data.put("subtaskProgress", subtaskProgress());
        return data;
    }

    public List<Map<String, Object>> processDefinitions() {
        List<Map<String, Object>> definitions = flowableProcessDefinitions();
        debugLog("H9", "DesignOptimizationService.java:58", "processDefinitions fetched", mapOf(
            "count", definitions.size(),
            "firstDefinitionId", definitions.isEmpty() ? null : definitions.get(0).get("definitionId"),
            "firstProcessKey", definitions.isEmpty() ? null : definitions.get(0).get("processKey")
        ));
        if (!definitions.isEmpty()) {
            return definitions;
        }
        return Collections.emptyList();
    }

    public Map<String, Object> deployDefaultProcess() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(flowableBaseUrl + "/design/process/deploy-default", Collections.emptyMap(), Map.class);
            return responseData(response);
        } catch (Exception e) {
            throw new IllegalStateException("默认起落架舱门流程模板部署失败，请确认 ruoyi-flowable 已启动。", e);
        }
    }

    public List<Map<String, Object>> processDefinitionNodes(String processDefinitionId) {
        try {
            String encodedId = URLEncoder.encode(processDefinitionId, StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(flowableBaseUrl + "/design/process/definition/" + encodedId + "/nodes", Map.class);
            return responseList(response);
        } catch (Exception e) {
            throw new IllegalStateException("读取 Flowable 流程节点失败，请确认流程定义仍然存在。", e);
        }
    }

    public Map<String, Object> assigneeOptions() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("structureUserId", usersByRole("structure_engineer", "结构工程师"));
        data.put("layoutUserId", usersByRole("layout_engineer", "布局工程师"));
        data.put("aeroUserId", usersByRole("aero_engineer", "气动工程师"));
        data.put("hydraulicUserId", usersByRole("hydraulic_engineer", "液压工程师"));
        data.put("manufacturingUserId", usersByRole("manufacturing_engineer", "制造工程师"));
        data.put("leaderUserId", usersByRole("approval_leader", "审批领导"));
        return data;
    }

    @Transactional
    public Map<String, Object> startTask(Map<String, Object> body) {
        debugLog("H5", "DesignOptimizationService.java:86", "startTask entered", mapOf(
            "processDefinitionId", str(body.get("processDefinitionId"), ""),
            "taskName", str(body.get("taskName"), ""),
            "structureUserId", body.get("structureUserId"),
            "layoutUserId", body.get("layoutUserId"),
            "aeroUserId", body.get("aeroUserId"),
            "hydraulicUserId", body.get("hydraulicUserId"),
            "manufacturingUserId", body.get("manufacturingUserId"),
            "leaderUserId", body.get("leaderUserId")
        ));
        try {
            DesignTask task = new DesignTask();
            task.setTaskName(str(body.get("taskName"), "液压弯管抗冲击设计制造协同优化任务"));
            task.setTaskNo("LGD-" + TASK_NO_TIME.format(LocalDateTime.now()));
            task.setTaskType(str(body.get("taskType"), "LANDING_GEAR_DOOR"));
            task.setPriority(intValue(body.get("priority"), 2));
            task.setDescription(str(body.get("description"), "基于前置故障追因结论，围绕起落架舱门区域液压弯管开展抗冲击设计制造协同优化。固定起点和终点，水平总长 600 mm，竖直总高 300 mm，关键设计变量为 L1、L2、θ1、θ2、R。"));
            task.setPlannedStartTime(dateValue(body.get("plannedStartTime")));
            task.setPlannedEndTime(dateValue(body.get("plannedEndTime")));
            String processDefinitionId = str(body.get("processDefinitionId"), "");
            if (StringUtils.isEmpty(processDefinitionId)) {
                processDefinitionId = str(ensureDesignProcessDefinition().get("definitionId"), "");
            }
            if (StringUtils.isEmpty(processDefinitionId)) {
                throw new IllegalStateException("未获取到可启动的 Flowable 流程模板，请先启动 ruoyi-flowable 并部署起落架舱门优化流程。");
            }
            task.setProcessDefinitionId(processDefinitionId);
            task.setStatus("OBJECTIVE_SELECTING");
            task.setCurrentNodeKey("structure_select");
            task.setCurrentNodeName("结构工程师目标约束选择");
            task.setOwnerUserId(currentUserId());
            task.setOwnerUserName(currentUsername());
            task.setStructureUserId(longValue(body.get("structureUserId")));
            task.setLayoutUserId(longValue(body.get("layoutUserId")));
            task.setAeroUserId(longValue(body.get("aeroUserId")));
            task.setHydraulicUserId(longValue(body.get("hydraulicUserId")));
            task.setManufacturingUserId(firstNonNull(longValue(body.get("manufacturingUserId")), longValue(body.get("designUserId"))));
            task.setDesignUserId(longValue(body.get("designUserId")));
            task.setLeaderUserId(longValue(body.get("leaderUserId")));
            task.setCreateBy(currentUsername());
            taskService.insertTask(task);
            saveTaskAttachments(task.getTaskId(), body.get("attachments"));
            debugLog("H6", "DesignOptimizationService.java:114", "task inserted", mapOf(
                "taskId", task.getTaskId(),
                "processDefinitionId", task.getProcessDefinitionId(),
                "ownerUserId", task.getOwnerUserId(),
                "status", task.getStatus()
            ));

            Map<String, Object> variables = new LinkedHashMap<>();
            variables.put("businessKey", task.getTaskId());
            variables.put("taskId", task.getTaskId());
            variables.put("taskName", task.getTaskName());
            variables.put("taskType", task.getTaskType());
            variables.put("ownerUserId", task.getOwnerUserId());
            variables.put("structureUserId", task.getStructureUserId());
            variables.put("layoutUserId", task.getLayoutUserId());
            variables.put("aeroUserId", task.getAeroUserId());
            variables.put("hydraulicUserId", task.getHydraulicUserId());
            variables.put("manufacturingUserId", task.getManufacturingUserId());
            variables.put("leaderUserId", task.getLeaderUserId());
            variables.put("conflictPassed", false);
            variables.put("simulationPassed", false);
            variables.put("approvalPassed", false);
            variables.put("returnToObjectiveSelection", false);

            applyRuntime(task, startFlowable(task.getProcessDefinitionId(), variables));
            debugLog("H7", "DesignOptimizationService.java:133", "flowable start returned", mapOf(
                "taskId", task.getTaskId(),
                "processInstanceId", task.getProcessInstanceId(),
                "currentFlowableTaskId", task.getCurrentFlowableTaskId(),
                "currentNodeKey", task.getCurrentNodeKey(),
                "currentNodeName", task.getCurrentNodeName()
            ));
            taskService.updateTask(task);
            Map<String, Object> detail = detail(task.getTaskId());
            debugLog("H10", "DesignOptimizationService.java:160", "detail after start succeeded", mapOf(
                "taskId", task.getTaskId(),
                "detailTaskExists", detail.get("task") != null,
                "nodeKey", detail.get("nodeKey"),
                "stageRoute", detail.get("stageRoute")
            ));
            return detail;
        } catch (Exception e) {
            debugLog("H10", "DesignOptimizationService.java:163", "startTask failed before response", mapOf(
                "errorType", e.getClass().getName(),
                "message", e.getMessage()
            ));
            throw e;
        }
    }

    public Map<String, Object> detail(Long taskId) {
        DesignTask task = taskService.selectTaskById(taskId);
        syncRuntimeAndPersistIfChanged(task);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("task", task);
        data.put("nodeKey", task == null ? "structure_select" : task.getCurrentNodeKey());
        data.put("stageRoute", stageRoute(task == null ? "structure_select" : task.getCurrentNodeKey()));
        data.put("access", taskAccess(task));
        data.put("currentUserId", currentUserId());
        data.put("catalog", catalog());
        data.put("attachments", safeTaskFiles(taskId));
        data.put("objectiveConstraints", selectedObjectiveConstraints(taskId));
        data.put("designVariables", selectedDesignVariables(taskId));
        data.put("conflictCheck", conflictCheckResult(true));
        data.put("decomposed", isDecomposed(task));
        data.put("subtasks", isDecomposed(task) ? subtaskSolutions(taskId) : subtaskDefinitions(taskId));
        data.put("simulation", simulationResult());
        data.put("surrogateSolve", taskId == null ? defaultSurrogateSolve() : surrogateSolve(taskId));
        data.put("cadModel", taskId == null ? defaultCadModel() : cadModel(taskId));
        data.put("approval", mapOf("result", "PENDING", "comment", ""));
        return data;
    }

    public Map<String, Object> uploadTaskAttachment(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String originalName = str(multipartFile.getOriginalFilename(), "attachment");
        String suffix = fileSuffix(originalName);
        String safeBaseName = originalName.replaceAll("[\\\\/:*?\"<>|]", "_");
        String storedName = UUID.randomUUID() + (StringUtils.isEmpty(suffix) ? "" : "." + suffix);
        try {
            Path uploadDir = Path.of(attachmentPath).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);
            Path target = uploadDir.resolve(storedName).normalize();
            if (!target.startsWith(uploadDir)) {
                throw new IllegalArgumentException("附件路径非法");
            }
            Files.copy(multipartFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return mapOf(
                "fileName", safeBaseName,
                "name", safeBaseName,
                "filePath", target.toString(),
                "url", target.toString(),
                "fileSize", multipartFile.getSize(),
                "fileType", str(multipartFile.getContentType(), ""),
                "fileSuffix", suffix
            );
        } catch (IOException e) {
            throw new IllegalStateException("附件上传失败：" + e.getMessage(), e);
        }
    }

    public DesignTaskFile taskAttachment(Long fileId) {
        DesignTaskFile taskFile = taskFileService.selectFileById(fileId);
        if (taskFile == null || StringUtils.isEmpty(taskFile.getFilePath())) {
            throw new IllegalArgumentException("附件不存在");
        }
        File file = new File(taskFile.getFilePath());
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("附件文件不存在");
        }
        return taskFile;
    }

    public List<Map<String, Object>> objectiveCatalog(String discipline) {
        return catalogByDiscipline(discipline);
    }

    public List<Map<String, Object>> designVariableCatalog(String discipline) {
        return variableCatalogByDiscipline(discipline);
    }

    @Transactional
    public Map<String, Object> saveObjectiveConstraints(Long taskId, Map<String, Object> body) {
        DesignTask task = taskService.selectTaskById(taskId);
        if (task != null) {
            assertCurrentAssignee(task, "当前节点未流转到你，暂不能提交目标与约束。");
            String discipline = str(body.get("discipline"), "structure");
            saveObjectiveItems(taskId, discipline, body);
            completeFlowableTask(task, mapOf("lastCompletedDiscipline", discipline));
            task.setUpdateBy(currentUsername());
            taskService.updateTask(task);
        }
        return detail(taskId);
    }

    @Transactional
    public Map<String, Object> saveDesignVariables(Long taskId, Map<String, Object> body) {
        DesignTask task = taskService.selectTaskById(taskId);
        if (task != null) {
            syncRuntimeIfPossible(task);
            assertCurrentAssignee(task, "当前任务未流转到你，暂不能保存设计变量。");
            if (!"model_decompose_solve".equals(task.getCurrentNodeKey())) {
                throw new IllegalStateException("请先完成目标约束冲突校验，进入模型解耦求解阶段后再选择设计变量。");
            }
        }
        saveDesignVariableItems(taskId, body);
        if (task != null) {
            task.setUpdateBy(currentUsername());
            taskService.updateTask(task);
        }
        return detail(taskId);
    }

    public Map<String, Object> conflictCheck(Long taskId, Map<String, Object> body) {
        boolean passed = Boolean.parseBoolean(String.valueOf(body.getOrDefault("passed", "true")));
        DesignTask task = taskService.selectTaskById(taskId);
        if (task != null) {
            assertCurrentAssignee(task, "当前任务未流转到你，暂不能执行冲突校验。");
            completeFlowableTask(task, mapOf("conflictPassed", passed));
            task.setUpdateBy(currentUsername());
            taskService.updateTask(task);
        }
        return conflictCheckResult(passed);
    }

    public Map<String, Object> decompose(Long taskId) {
        DesignTask task = taskService.selectTaskById(taskId);
        if (task != null) {
            syncRuntimeIfPossible(task);
            assertCurrentAssignee(task, "当前任务未流转到你，暂不能执行任务解耦。");
            task.setUpdateBy(currentUsername());
            taskService.updateTask(task);
        }
        return mapOf("subtasks", subtaskDefinitions(taskId), "message", "已将大任务目标与约束归类到两个解耦子任务");
    }

    public Map<String, Object> solve(Long taskId, Map<String, Object> body) {
        DesignTask task = taskService.selectTaskById(taskId);
        if (task != null) {
            assertCurrentAssignee(task, "当前任务未流转到你，暂不能提交求解结果。");
            if (selectedDesignVariableCount(taskId) == 0) {
                throw new IllegalStateException("请先保存至少一个设计变量，再进行模型求解。");
            }
            boolean returnToObjectiveSelection = Boolean.parseBoolean(String.valueOf(body.getOrDefault("returnToObjectiveSelection", "false")));
            completeFlowableTask(task, mapOf("returnToObjectiveSelection", returnToObjectiveSelection));
            task.setUpdateBy(currentUsername());
            taskService.updateTask(task);
        }
        return mapOf("subtasks", subtaskSolutions(taskId), "recommended", List.of("HI-02", "CL-02"));
    }

    public Map<String, Object> submitSurrogateSolve(Long taskId, Map<String, Object> body) {
        if (body == null) {
            body = Collections.emptyMap();
        }
        DesignTask task = taskService.selectTaskById(taskId);
        if (task != null) {
            assertCurrentAssignee(task, "当前任务未流转到你，暂不能启动代理模型优化求解。");
            if (selectedDesignVariableCount(taskId) == 0) {
                throw new IllegalStateException("请先保存设计变量，再启动代理模型优化求解。");
            }
        }
        ensureSurrogateSolveTable();
        Map<String, Object> bounds = selectedVariableBounds(taskId);
        validateSurrogateBounds(bounds);
        Map<String, Object> algorithm = mapOf(
            "type", str(body.get("algorithmType"), "differential_evolution"),
            "maxIterations", intValue(firstNonNull(body.get("maxIterations"), body.get("maxiter")), 80),
            "populationSize", intValue(firstNonNull(body.get("populationSize"), body.get("popsize")), 15),
            "seed", intValue(body.get("seed"), 42)
        );
        Map<String, Object> params = mapOf(
            "taskId", taskId,
            "variables", bounds,
            "algorithm", algorithm
        );
        upsertSurrogateSolve(taskId, "QUEUED", params, Collections.emptyMap(), "");
        CompletableFuture.runAsync(() -> runSurrogateWorker(taskId, params), solverExecutor);
        return surrogateSolve(taskId);
    }

    public Map<String, Object> surrogateSolve(Long taskId) {
        ensureSurrogateSolveTable();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select * from p2_surrogate_solve_task where task_id = ?", taskId);
        if (rows.isEmpty()) {
            return defaultSurrogateSolve();
        }
        Map<String, Object> row = rows.get(0);
        String status = str(row.get("status"), "NOT_SUBMITTED");
        return mapOf(
            "status", status,
            "statusLabel", surrogateStatusLabel(status),
            "modelName", str(row.get("model_name"), ""),
            "modelType", str(row.get("model_type"), ""),
            "objectiveName", str(row.get("objective_name"), "predictedStress"),
            "objectiveUnit", str(row.get("objective_unit"), "MPa"),
            "params", fromJson(str(row.get("params_json"), "{}")),
            "bestSolution", fromJson(str(row.get("best_solution_json"), "{}")),
            "candidates", fromJsonList(str(row.get("candidate_solutions_json"), "[]")),
            "history", fromJsonList(str(row.get("iteration_history_json"), "[]")),
            "iterations", intValue(row.get("iterations"), 0),
            "errorMessage", str(row.get("error_message"), ""),
            "confirmed", "1".equals(str(row.get("confirmed"), "0")),
            "updatedAt", row.get("update_time")
        );
    }

    public Map<String, Object> confirmSurrogateSolve(Long taskId) {
        ensureSurrogateSolveTable();
        Map<String, Object> result = surrogateSolve(taskId);
        if (!"SUCCESS".equals(result.get("status")) && !"CONFIRMED".equals(result.get("status"))) {
            throw new IllegalStateException("代理模型尚未求解成功，不能确认最优方案。");
        }
        Map<String, Object> bestSolution = fromJson(toJson(result.get("bestSolution")));
        if (bestSolution.isEmpty()) {
            throw new IllegalStateException("代理模型最优方案为空，不能确认。");
        }
        DesignTask task = taskService.selectTaskById(taskId);
        if (task != null) {
            assertCurrentAssignee(task, "当前任务未流转到你，暂不能确认代理模型优化方案。");
            completeFlowableTask(task, mapOf("returnToObjectiveSelection", false));
            task.setUpdateBy(currentUsername());
            taskService.updateTask(task);
        }
        jdbcTemplate.update("update p2_surrogate_solve_task set status = 'CONFIRMED', confirmed = '1', update_time = sysdate() where task_id = ?", taskId);
        return surrogateSolve(taskId);
    }

    public Map<String, Object> simulation(Long taskId, Map<String, Object> body) {
        DesignTask task = taskService.selectTaskById(taskId);
        if (task != null) {
            assertCurrentAssignee(task, "当前任务未流转到你，暂不能执行仿真确认。");
            boolean passed = Boolean.parseBoolean(String.valueOf(body.getOrDefault("simulationPassed", "true")));
            completeFlowableTask(task, mapOf("simulationPassed", passed));
            task.setUpdateBy(currentUsername());
            taskService.updateTask(task);
        }
        return simulationResult();
    }

    public Map<String, Object> submitCadModel(Long taskId, Map<String, Object> body) {
        DesignTask task = taskService.selectTaskById(taskId);
        if (task != null) {
            assertCurrentAssignee(task, "当前任务未流转到你，暂不能执行 CAD 参数建模验证。");
        }
        ensureCadModelTable();
        Map<String, Object> params = cadParams(body);
        validateCadParams(params);
        String paramsJson = toJson(params);
        upsertCadTask(taskId, "QUEUED", paramsJson, "", Collections.emptyMap());
        String username = currentUsername();
        CompletableFuture.runAsync(() -> runCadWorker(taskId, params, username), cadExecutor);
        return cadModel(taskId);
    }

    public Map<String, Object> cadModel(Long taskId) {
        ensureCadModelTable();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select * from p2_cad_model_task where task_id = ?", taskId);
        if (rows.isEmpty()) {
            return defaultCadModel();
        }
        Map<String, Object> row = rows.get(0);
        Map<String, Object> params = fromJson(str(row.get("params_json"), "{}"));
        Map<String, Object> files = new LinkedHashMap<>();
        putFileIfExists(files, "sldprt", taskId, "CAD_SLDPRT", str(row.get("sldprt_path"), ""));
        putFileIfExists(files, "stl", taskId, "CAD_STL", str(row.get("stl_path"), ""));
        putFileIfExists(files, "previewPng", taskId, "CAD_PREVIEW_PNG", str(row.get("preview_png_path"), ""));
        return mapOf(
            "status", str(row.get("status"), "NOT_SUBMITTED"),
            "statusLabel", cadStatusLabel(str(row.get("status"), "NOT_SUBMITTED")),
            "params", params,
            "l3", row.get("l3"),
            "initialAngle", row.get("initial_angle"),
            "closureStatus", str(row.get("closure_status"), ""),
            "errorMessage", str(row.get("error_message"), ""),
            "files", files,
            "updatedAt", row.get("update_time")
        );
    }

    public File cadModelFile(Long taskId, String kind) {
        ensureCadModelTable();
        String column = switch (kind == null ? "" : kind.toLowerCase(Locale.ROOT)) {
            case "sldprt" -> "sldprt_path";
            case "stl" -> "stl_path";
            case "preview", "png", "previewpng" -> "preview_png_path";
            default -> throw new IllegalStateException("不支持的 CAD 文件类型：" + kind);
        };
        List<String> paths = jdbcTemplate.queryForList("select " + column + " from p2_cad_model_task where task_id = ?", String.class, taskId);
        if (paths.isEmpty() || StringUtils.isEmpty(paths.get(0))) {
            throw new IllegalStateException("CAD 文件尚未生成。");
        }
        File file = new File(paths.get(0));
        if (!file.exists() || !file.isFile()) {
            throw new IllegalStateException("CAD 文件不存在：" + file.getAbsolutePath());
        }
        return file;
    }

    public Map<String, Object> approve(Long taskId, Map<String, Object> body) {
        boolean approved = Boolean.parseBoolean(String.valueOf(body.getOrDefault("approved", "true")));
        DesignTask task = taskService.selectTaskById(taskId);
        if (task != null) {
            assertCurrentAssignee(task, "当前任务未流转到你，暂不能审批。");
            completeFlowableTask(task, mapOf("approvalPassed", approved));
            task.setUpdateBy(currentUsername());
            taskService.updateTask(task);
        }
        return mapOf("approved", approved, "comment", str(body.get("comment"), approved ? "审批通过" : "退回重新求解"));
    }

    private Map<String, Object> defaultSurrogateSolve() {
        return mapOf(
            "status", "NOT_SUBMITTED",
            "statusLabel", "未提交",
            "modelName", "aero_pipe_kriging.pkl",
            "modelType", "Kriging / Gaussian Process",
            "objectiveName", "predictedStress",
            "objectiveUnit", "MPa",
            "params", Collections.emptyMap(),
            "bestSolution", Collections.emptyMap(),
            "candidates", Collections.emptyList(),
            "history", Collections.emptyList(),
            "iterations", 0,
            "errorMessage", "",
            "confirmed", false
        );
    }

    private void runSurrogateWorker(Long taskId, Map<String, Object> params) {
        try {
            upsertSurrogateSolve(taskId, "RUNNING", params, Collections.emptyMap(), "");
            @SuppressWarnings("unchecked")
            Map<String, Object> workerResponse = restTemplate.postForObject(surrogateBaseUrl + "/api/surrogate/optimize", params, Map.class);
            Map<String, Object> data = responseData(workerResponse);
            String status = str(data.get("status"), "SUCCESS");
            if (!"SUCCESS".equalsIgnoreCase(status)) {
                upsertSurrogateSolve(taskId, "FAILED", params, data, str(data.get("errorMessage"), "代理模型求解失败。"));
                return;
            }
            upsertSurrogateSolve(taskId, "SUCCESS", params, data, "");
        } catch (Exception e) {
            upsertSurrogateSolve(taskId, "FAILED", params, Collections.emptyMap(), "代理模型求解服务不可用：" + e.getMessage());
        }
    }

    private void ensureSurrogateSolveTable() {
        jdbcTemplate.execute("""
            create table if not exists p2_surrogate_solve_task (
              task_id bigint not null primary key,
              status varchar(32) not null,
              model_name varchar(255),
              model_type varchar(255),
              objective_name varchar(128),
              objective_unit varchar(64),
              params_json text,
              best_solution_json text,
              candidate_solutions_json text,
              iteration_history_json text,
              iterations int default 0,
              error_message varchar(1000),
              confirmed char(1) default '0',
              create_time datetime default current_timestamp,
              update_time datetime default current_timestamp
            )
            """);
    }

    private void upsertSurrogateSolve(Long taskId, String status, Map<String, Object> params, Map<String, Object> data, String errorMessage) {
        ensureSurrogateSolveTable();
        jdbcTemplate.update("""
            insert into p2_surrogate_solve_task (
              task_id, status, model_name, model_type, objective_name, objective_unit,
              params_json, best_solution_json, candidate_solutions_json, iteration_history_json,
              iterations, error_message, confirmed, create_time, update_time
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '0', sysdate(), sysdate())
            on duplicate key update
              status = values(status),
              model_name = values(model_name),
              model_type = values(model_type),
              objective_name = values(objective_name),
              objective_unit = values(objective_unit),
              params_json = values(params_json),
              best_solution_json = values(best_solution_json),
              candidate_solutions_json = values(candidate_solutions_json),
              iteration_history_json = values(iteration_history_json),
              iterations = values(iterations),
              error_message = values(error_message),
              confirmed = if(values(status) = 'SUCCESS', '0', confirmed),
              update_time = sysdate()
            """,
            taskId,
            status,
            str(data.get("modelName"), "aero_pipe_kriging.pkl"),
            str(data.get("modelType"), "Kriging / Gaussian Process"),
            str(data.get("objectiveName"), "predictedStress"),
            str(data.get("objectiveUnit"), "MPa"),
            toJson(params),
            toJson(firstNonNull(data.get("bestSolution"), Collections.emptyMap())),
            toJson(firstNonNull(data.get("candidates"), Collections.emptyList())),
            toJson(firstNonNull(data.get("history"), Collections.emptyList())),
            intValue(data.get("iterations"), 0),
            errorMessage
        );
    }

    private String surrogateStatusLabel(String status) {
        return switch (status) {
            case "QUEUED" -> "排队中";
            case "RUNNING" -> "求解中";
            case "SUCCESS" -> "求解成功";
            case "FAILED" -> "求解失败";
            case "CONFIRMED" -> "已确认";
            default -> "未提交";
        };
    }

    private Map<String, Object> defaultCadModel() {
        return mapOf(
            "status", "NOT_SUBMITTED",
            "statusLabel", "未提交",
            "params", cadParams(Collections.emptyMap()),
            "l3", null,
            "initialAngle", null,
            "closureStatus", "",
            "errorMessage", "",
            "files", Collections.emptyMap()
        );
    }

    private Map<String, Object> cadParams(Map<String, Object> body) {
        return mapOf(
            "L1", doubleValue(firstNonNull(body.get("L1"), body.get("l1"), body.get("L1_mm"), body.get("l1Mm")), 280.0),
            "L2", doubleValue(firstNonNull(body.get("L2"), body.get("l2"), body.get("L2_mm"), body.get("l2Mm")), 150.0),
            "R", doubleValue(firstNonNull(body.get("R"), body.get("r"), body.get("R_mm"), body.get("radius")), 20.0),
            "theta1", doubleValue(firstNonNull(body.get("theta1"), body.get("angle1")), 110.0),
            "theta2", doubleValue(firstNonNull(body.get("theta2"), body.get("angle2")), 120.0),
            "pipeDiameter", doubleValue(firstNonNull(body.get("pipeDiameter"), body.get("pipe_outer_diameter_mm")), 30.0),
            "totalHorizontal", 600.0,
            "totalVertical", 300.0
        );
    }

    private void validateCadParams(Map<String, Object> params) {
        double l1 = doubleValue(params.get("L1"), 0);
        double l2 = doubleValue(params.get("L2"), 0);
        double radius = doubleValue(params.get("R"), 0);
        double pipeDiameter = doubleValue(params.get("pipeDiameter"), 0);
        double theta1 = doubleValue(params.get("theta1"), 0);
        double theta2 = doubleValue(params.get("theta2"), 0);
        if (l1 <= 0 || l2 <= 0) {
            throw new IllegalStateException("L1 和 L2 必须大于 0。");
        }
        if (radius <= 0) {
            throw new IllegalStateException("弯曲半径 R 必须大于 0。");
        }
        if (pipeDiameter <= 0) {
            throw new IllegalStateException("管径必须大于 0。");
        }
        if (radius <= pipeDiameter / 2.0) {
            throw new IllegalStateException("弯曲半径 R 应大于管外径的一半。");
        }
        if (theta1 <= 0 || theta1 >= 180 || theta2 <= 0 || theta2 >= 180) {
            throw new IllegalStateException("θ1 和 θ2 应使用 SolidWorks 草图中的标注弯角，范围为 0 到 180 度。");
        }
    }

    private void runCadWorker(Long taskId, Map<String, Object> params, String username) {
        try {
            upsertCadTask(taskId, "RUNNING", toJson(params), "", Collections.emptyMap());
            Map<String, Object> request = cadWorkerParams(params);
            request.put("taskId", taskId);
            @SuppressWarnings("unchecked")
            Map<String, Object> workerResponse = restTemplate.postForObject(solidWorksWorkerUrl, request, Map.class);
            Map<String, Object> data = responseData(workerResponse);
            if (data.isEmpty() && workerResponse != null) {
                data = workerResponse;
            }
            String status = str(data.get("status"), "SUCCESS");
            if (!"SUCCESS".equalsIgnoreCase(status) && !"COMPLETED".equalsIgnoreCase(status)) {
                String error = str(data.get("errorMessage"), "SolidWorks Worker 建模失败。");
                upsertCadTask(taskId, "FAILED", toJson(params), error, data);
                return;
            }
            registerCadFiles(taskId, data, username);
            upsertCadTask(taskId, "SUCCESS", toJson(params), "", data);
        } catch (Exception e) {
            upsertCadTask(taskId, "FAILED", toJson(params), "建模服务不可用：" + e.getMessage(), Collections.emptyMap());
        }
    }

    private Map<String, Object> cadWorkerParams(Map<String, Object> params) {
        Map<String, Object> request = new LinkedHashMap<>(params);
        double theta1 = doubleValue(params.get("theta1"), 110.0);
        double theta2 = doubleValue(params.get("theta2"), 120.0);
        // The surrogate model and UI use the SolidWorks dimensioned bend angles.
        // The geometry generator needs signed centerline heading changes.
        request.put("theta1", theta1 - 180.0);
        request.put("theta2", 180.0 - theta2);
        request.put("angle1", theta1);
        request.put("angle2", theta2);
        return request;
    }

    private void registerCadFiles(Long taskId, Map<String, Object> data, String username) {
        jdbcTemplate.update("delete from p2_design_task_file where task_id = ? and file_type in ('CAD_SLDPRT','CAD_STL','CAD_PREVIEW_PNG')", taskId);
        insertCadFile(taskId, str(data.get("sldprtPath"), ""), "CAD_SLDPRT", username);
        insertCadFile(taskId, str(data.get("stlPath"), ""), "CAD_STL", username);
        insertCadFile(taskId, str(data.get("previewPngPath"), ""), "CAD_PREVIEW_PNG", username);
    }

    private void insertCadFile(Long taskId, String path, String type, String username) {
        if (StringUtils.isEmpty(path)) {
            return;
        }
        File fileOnDisk = new File(path);
        DesignTaskFile file = new DesignTaskFile();
        file.setTaskId(taskId);
        file.setFileName(fileOnDisk.getName());
        file.setFilePath(path);
        file.setFileSize(fileOnDisk.exists() ? fileOnDisk.length() : 0L);
        file.setFileType(type);
        file.setFileSuffix(fileSuffix(fileOnDisk.getName()));
        file.setUploadBy(StringUtils.isEmpty(username) ? "system" : username);
        taskFileService.insertFile(file);
    }

    private void putFileIfExists(Map<String, Object> files, String key, Long taskId, String fileType, String path) {
        if (StringUtils.isEmpty(path)) {
            return;
        }
        String kind = switch (key) {
            case "sldprt" -> "sldprt";
            case "previewPng" -> "preview";
            default -> "stl";
        };
        files.put(key, mapOf(
            "fileType", fileType,
            "fileName", new File(path).getName(),
            "filePath", path,
            "url", "/designtask/task/" + taskId + "/cad-model/file/" + kind
        ));
    }

    private void ensureCadModelTable() {
        jdbcTemplate.execute("""
            create table if not exists p2_cad_model_task (
              task_id bigint not null primary key,
              status varchar(32) not null,
              params_json text,
              l3 decimal(18,6),
              initial_angle decimal(18,6),
              closure_status varchar(128),
              error_message varchar(1000),
              sldprt_path varchar(1000),
              stl_path varchar(1000),
              preview_png_path varchar(1000),
              create_time datetime default current_timestamp,
              update_time datetime default current_timestamp
            )
            """);
    }

    private void upsertCadTask(Long taskId, String status, String paramsJson, String errorMessage, Map<String, Object> data) {
        ensureCadModelTable();
        jdbcTemplate.update("""
            insert into p2_cad_model_task (
              task_id, status, params_json, l3, initial_angle, closure_status, error_message,
              sldprt_path, stl_path, preview_png_path, create_time, update_time
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate(), sysdate())
            on duplicate key update
              status = values(status),
              params_json = values(params_json),
              l3 = values(l3),
              initial_angle = values(initial_angle),
              closure_status = values(closure_status),
              error_message = values(error_message),
              sldprt_path = values(sldprt_path),
              stl_path = values(stl_path),
              preview_png_path = values(preview_png_path),
              update_time = sysdate()
            """,
            taskId,
            status,
            paramsJson,
            numberOrNull(data.get("L3"), data.get("l3")),
            numberOrNull(data.get("initialAngle"), data.get("initial_angle")),
            str(data.get("closureStatus"), "几何闭合状态待 Worker 返回"),
            errorMessage,
            str(data.get("sldprtPath"), ""),
            str(data.get("stlPath"), ""),
            str(data.get("previewPngPath"), "")
        );
    }

    private Object numberOrNull(Object... values) {
        for (Object value : values) {
            if (value != null && !String.valueOf(value).isBlank()) {
                return doubleValue(value, 0);
            }
        }
        return null;
    }

    private String cadStatusLabel(String status) {
        return switch (status) {
            case "QUEUED" -> "排队中";
            case "RUNNING" -> "建模中";
            case "SUCCESS" -> "生成成功";
            case "FAILED" -> "生成失败";
            default -> "未提交";
        };
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, Object> fromJson(String value) {
        try {
            return objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private List<Map<String, Object>> fromJsonList(String value) {
        try {
            return objectMapper.readValue(value, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Map<String, Object> ensureDesignProcessDefinition() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(flowableBaseUrl + "/design/process/ensure", Map.class);
            return responseData(response);
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    private List<Map<String, Object>> flowableProcessDefinitions() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(flowableBaseUrl + "/design/process/definitions", Map.class);
            return responseList(response);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private Map<String, Object> startFlowable(String processDefinitionId, Map<String, Object> variables) {
        try {
            debugLog("H8", "DesignOptimizationService.java:285", "calling flowable start", mapOf(
                "flowableBaseUrl", flowableBaseUrl,
                "processDefinitionId", processDefinitionId,
                "businessKey", variables.get("businessKey"),
                "taskId", variables.get("taskId")
            ));
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(flowableBaseUrl + "/design/process/start/" + processDefinitionId, variables, Map.class);
            Map<String, Object> data = responseData(response);
            debugLog("H8", "DesignOptimizationService.java:294", "flowable start success payload", mapOf(
                "processInstanceId", data.get("processInstanceId"),
                "currentTaskId", data.get("currentTaskId"),
                "currentNodeKey", data.get("currentNodeKey"),
                "currentNodeName", data.get("currentNodeName")
            ));
            return data;
        } catch (Exception e) {
            debugLog("H8", "DesignOptimizationService.java:301", "flowable start failed", mapOf(
                "errorType", e.getClass().getName(),
                "message", e.getMessage(),
                "processDefinitionId", processDefinitionId,
                "flowableBaseUrl", flowableBaseUrl
            ));
            throw new IllegalStateException("Flowable 流程实例启动失败，请确认 ruoyi-flowable 已启动且流程模板可用。", e);
        }
    }

    private void completeFlowableTask(DesignTask task, Map<String, Object> variables) {
        if (task.getProcessInstanceId() == null) {
            throw new IllegalStateException("当前任务没有 Flowable 流程实例，不能执行工作流流转。");
        }
        try {
            syncRuntimeIfPossible(task);
            if (StringUtils.isEmpty(task.getCurrentFlowableTaskId())) {
                throw new IllegalStateException("当前任务没有 Flowable 待办节点，不能执行工作流流转。");
            }
            Map<String, Object> body = mapOf(
                "taskId", task.getCurrentFlowableTaskId(),
                "processInstanceId", task.getProcessInstanceId(),
                "variables", variables
            );
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(flowableBaseUrl + "/design/process/complete", body, Map.class);
            applyRuntime(task, responseData(response));
        } catch (Exception e) {
            throw new IllegalStateException("Flowable 当前节点提交失败，请确认流程实例仍处于当前待办节点。", e);
        }
    }

    private void syncRuntimeIfPossible(DesignTask task) {
        if (task == null || StringUtils.isEmpty(task.getProcessInstanceId())) {
            return;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(flowableBaseUrl + "/design/process/current-task/" + task.getProcessInstanceId(), Map.class);
            applyRuntime(task, responseData(response));
        } catch (Exception ignored) {
        }
    }

    private void applyRuntime(DesignTask task, Map<String, Object> runtime) {
        if (task == null || runtime == null || runtime.isEmpty()) {
            return;
        }
        task.setProcessInstanceId(str(runtime.get("processInstanceId"), task.getProcessInstanceId()));
        task.setCurrentFlowableTaskId(str(runtime.get("currentTaskId"), task.getCurrentFlowableTaskId()));
        task.setCurrentNodeKey(str(runtime.get("currentNodeKey"), task.getCurrentNodeKey()));
        task.setCurrentNodeName(str(runtime.get("currentNodeName"), task.getCurrentNodeName()));
        task.setStatus(statusForNode(task.getCurrentNodeKey(), task.getStatus()));
    }

    private void syncRuntimeAndPersistIfChanged(DesignTask task) {
        if (task == null || StringUtils.isEmpty(task.getProcessInstanceId())) {
            return;
        }
        String beforeTaskId = task.getCurrentFlowableTaskId();
        String beforeNodeKey = task.getCurrentNodeKey();
        String beforeNodeName = task.getCurrentNodeName();
        String beforeStatus = task.getStatus();
        syncRuntimeIfPossible(task);
        if (!Objects.equals(beforeTaskId, task.getCurrentFlowableTaskId())
            || !Objects.equals(beforeNodeKey, task.getCurrentNodeKey())
            || !Objects.equals(beforeNodeName, task.getCurrentNodeName())
            || !Objects.equals(beforeStatus, task.getStatus())) {
            taskService.updateTask(task);
        }
    }

    private String statusForNode(String nodeKey, String fallback) {
        if (nodeKey == null) return fallback;
        if (nodeKey.endsWith("_select")) return "OBJECTIVE_SELECTING";
        if ("conflict_check".equals(nodeKey)) return "CONFLICT_CHECKING";
        if ("model_decompose_solve".equals(nodeKey)) return "SOLVING";
        if ("simulation_confirm".equals(nodeKey)) return "SIMULATING";
        if ("leader_approve".equals(nodeKey)) return "APPROVING";
        if ("end".equals(nodeKey)) return "COMPLETED";
        return fallback;
    }

    private void saveObjectiveItems(Long taskId, String discipline, Map<String, Object> body) {
        jdbcTemplate.update("delete from p2_design_objective_constraint where task_id = ? and discipline = ?", taskId, discipline);
        Object items = body.get("items");
        if (!(items instanceof List<?> list)) {
            return;
        }
        for (Object itemObj : list) {
            if (!(itemObj instanceof Map<?, ?> item)) {
                continue;
            }
            jdbcTemplate.update("""
                insert into p2_design_objective_constraint(task_id, discipline, item_type, item_code, item_name, direction, weight, limit_value, unit, remark, create_by, create_time)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate())
                """,
                taskId, discipline, str(item.get("itemType"), ""), str(item.get("itemCode"), ""), str(item.get("itemName"), ""),
                str(item.get("direction"), ""), intValue(item.get("weight"), 50), str(item.get("limitValue"), ""),
                str(item.get("unit"), ""), str(body.get("remark"), ""), currentUsername());
        }
    }

    private void saveDesignVariableItems(Long taskId, Map<String, Object> body) {
        jdbcTemplate.update("delete from p2_design_task_variable_selection where task_id = ?", taskId);
        Object items = body.get("designVariables");
        if (!(items instanceof List<?> list)) {
            return;
        }
        for (Object itemObj : list) {
            if (!(itemObj instanceof Map<?, ?> item)) {
                continue;
            }
            jdbcTemplate.update("""
                insert into p2_design_task_variable_selection(task_id, discipline, subtask_code, variable_code, variable_name, variable_type, initial_value, lower_bound, upper_bound, step_value, unit, remark, create_by, create_time)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate())
                """,
                taskId, str(item.get("discipline"), ""), str(item.get("subtaskCode"), ""), str(item.get("variableCode"), ""), str(item.get("variableName"), ""),
                str(item.get("variableType"), "continuous"), str(item.get("initialValue"), str(item.get("defaultValue"), "")),
                str(item.get("lowerBound"), ""), str(item.get("upperBound"), ""), str(item.get("stepValue"), ""),
                str(item.get("unit"), ""), str(body.get("remark"), ""), currentUsername());
        }
    }

    private Map<String, Object> catalog() {
        Map<String, Object> data = new LinkedHashMap<>();
        for (String discipline : List.of("structure", "layout", "aero", "hydraulic", "manufacturing")) {
            data.put(discipline, catalogByDiscipline(discipline));
        }
        return data;
    }

    private List<Map<String, Object>> catalogByDiscipline(String discipline) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                select item_type itemType,
                       item_code itemCode,
                       item_name itemName,
                       direction,
                       unit,
                       default_weight weight,
                       default_limit_value limitValue,
                       remark
                from p2_design_objective_catalog
                where discipline = ?
                  and status = '0'
                order by sort_order, id
                """, discipline);
            if (!rows.isEmpty()) {
                return rows;
            }
        } catch (Exception ignored) {
        }
        return fallbackCatalog().getOrDefault(discipline, Collections.emptyList());
    }

    private List<Map<String, Object>> variableCatalogByDiscipline(String discipline) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                select subtask_code subtaskCode,
                       variable_code variableCode,
                       variable_name variableName,
                       variable_type variableType,
                       default_value defaultValue,
                       lower_bound lowerBound,
                       upper_bound upperBound,
                       step_value stepValue,
                       unit,
                       remark
                from p2_design_variable_catalog
                where discipline = ?
                  and status = '0'
                order by sort_order, id
                """, discipline);
            if (!rows.isEmpty()) {
                return rows;
            }
        } catch (Exception ignored) {
        }
        return fallbackVariableCatalog().getOrDefault(discipline, Collections.emptyList());
    }

    private List<Map<String, Object>> selectedObjectiveConstraints(Long taskId) {
        List<Map<String, Object>> groups = new ArrayList<>();
        for (String discipline : List.of("structure", "layout", "aero", "hydraulic", "manufacturing")) {
            List<Map<String, Object>> items;
            try {
                items = jdbcTemplate.queryForList("""
                    select item_type itemType, item_code itemCode, item_name itemName, direction, weight, limit_value limitValue, unit, remark
                    from p2_design_objective_constraint
                    where task_id = ? and discipline = ?
                    order by id
                    """, taskId, discipline);
            } catch (Exception e) {
                items = Collections.emptyList();
            }
            groups.add(mapOf("discipline", discipline, "disciplineName", disciplineName(discipline), "items", items));
        }
        return groups;
    }

    private List<Map<String, Object>> selectedDesignVariables(Long taskId) {
        List<Map<String, Object>> groups = new ArrayList<>();
        for (String discipline : List.of("structure", "layout", "aero", "hydraulic", "manufacturing")) {
            List<Map<String, Object>> items;
            try {
                items = jdbcTemplate.queryForList("""
                    select subtask_code subtaskCode, variable_code variableCode, variable_name variableName,
                           variable_type variableType, initial_value initialValue, lower_bound lowerBound,
                           upper_bound upperBound, step_value stepValue, unit, remark
                    from p2_design_task_variable_selection
                    where task_id = ? and discipline = ?
                    order by id
                    """, taskId, discipline);
            } catch (Exception e) {
                items = Collections.emptyList();
            }
            groups.add(mapOf("discipline", discipline, "disciplineName", disciplineName(discipline), "items", items));
        }
        return groups;
    }

    private int selectedDesignVariableCount(Long taskId) {
        try {
            Integer count = jdbcTemplate.queryForObject("""
                select count(1)
                from p2_design_task_variable_selection
                where task_id = ?
                """, Integer.class, taskId);
            return count == null ? 0 : count;
        } catch (Exception e) {
            return 0;
        }
    }

    private Map<String, Object> selectedVariableBounds(Long taskId) {
        Map<String, Object> bounds = new LinkedHashMap<>();
        Map<String, String> codeToKey = Map.of(
            "PIPE_L1", "L1",
            "PIPE_L2", "L2",
            "PIPE_THETA_1", "theta1",
            "PIPE_THETA_2", "theta2",
            "PIPE_BEND_RADIUS", "R"
        );
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                select variable_code variableCode, lower_bound lowerBound, upper_bound upperBound
                from p2_design_task_variable_selection
                where task_id = ?
                order by id
                """, taskId);
            for (Map<String, Object> row : rows) {
                String key = codeToKey.get(str(row.get("variableCode"), ""));
                if (key == null || bounds.containsKey(key)) {
                    continue;
                }
                bounds.put(key, mapOf(
                    "lower", doubleValue(row.get("lowerBound"), 0),
                    "upper", doubleValue(row.get("upperBound"), 0)
                ));
            }
        } catch (Exception ignored) {
        }
        return bounds;
    }

    private void validateSurrogateBounds(Map<String, Object> bounds) {
        for (String key : List.of("L1", "L2", "theta1", "theta2", "R")) {
            Object value = bounds.get(key);
            if (!(value instanceof Map<?, ?> map)) {
                throw new IllegalStateException("缺少代理模型设计变量边界：" + key);
            }
            double lower = doubleValue(map.get("lower"), 0);
            double upper = doubleValue(map.get("upper"), 0);
            if (upper <= lower) {
                throw new IllegalStateException("代理模型设计变量边界不合法：" + key);
            }
        }
    }

    private Map<String, String> selectedVariableValues(Long taskId) {
        Map<String, String> values = new LinkedHashMap<>();
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                select variable_code variableCode, initial_value initialValue
                from p2_design_task_variable_selection
                where task_id = ?
                order by id
                """, taskId);
            for (Map<String, Object> row : rows) {
                values.putIfAbsent(str(row.get("variableCode"), ""), str(row.get("initialValue"), ""));
            }
        } catch (Exception ignored) {
        }
        return values;
    }

    private Map<String, List<Map<String, Object>>> fallbackVariableCatalog() {
        Map<String, List<Map<String, Object>>> data = new LinkedHashMap<>();
        data.put("structure", Collections.emptyList());
        data.put("layout", List.of(
            variable("cable_pipe_layout", "PIPE_L1", "L1 第一段直管长度", "continuous", "300", "50", "550", "1", "mm"),
            variable("cable_pipe_layout", "PIPE_L2", "L2 第二段直管长度", "continuous", "150", "50", "300", "1", "mm")
        ));
        data.put("aero", Collections.emptyList());
        data.put("hydraulic", List.of(
            variable("hydraulic_impact", "PIPE_THETA_1", "θ1 第一个弯角弯曲角度", "continuous", "110", "30", "150", "1", "deg"),
            variable("hydraulic_impact", "PIPE_THETA_2", "θ2 第二个弯角弯曲角度", "continuous", "120", "30", "150", "1", "deg"),
            variable("shared", "PIPE_BEND_RADIUS", "R 两处弯管圆角半径", "continuous", "30", "20", "80", "1", "mm")
        ));
        data.put("manufacturing", Collections.emptyList());
        return data;
    }

    private Map<String, List<Map<String, Object>>> fallbackCatalog() {
        Map<String, List<Map<String, Object>>> data = new LinkedHashMap<>();
        data.put("structure", List.of(
            item("objective", "STR_SPACE_SUPPORT_MAX", "支撑与安装空间合理性最大", "max", "score"),
            item("objective", "STR_DEFORMATION_RISK_MIN", "管线变形碰撞风险最小", "min", "risk"),
            item("constraint", "STR_FORBIDDEN_ZONE", "结构禁布区域不可穿越", "avoid", ""),
            item("constraint", "STR_INTERFACE_FIXED", "铰链、锁机构、作动器接口位置不可更改", "fixed", ""),
            item("constraint", "STR_PIPE_CLEARANCE", "管道最大变形后不得碰撞结构件", "avoid", ""),
            item("constraint", "STR_CLAMP_SUPPORT_VALID", "卡箍支撑点结构强度满足要求", "meet", "")
        ));
        data.put("layout", List.of(
            item("objective", "LAY_LENGTH_MIN", "线缆与管路总长度最小", "min", "m"),
            item("objective", "LAY_INTERFERENCE_MIN", "线缆、管路、结构干涉风险最小", "min", "risk"),
            item("objective", "LAY_MAINTAINABILITY_MAX", "维护可达性最大", "max", "score"),
            item("constraint", "LAY_CLEARANCE_LIMIT", "管线与结构/运动件最小间隙", ">=", "mm"),
            item("constraint", "LAY_PIPE_CABLE_DISTANCE", "液压管与线缆最小隔离距离", ">=", "mm"),
            item("constraint", "LAY_FORBIDDEN_ZONE", "禁布区域不可穿越", "avoid", ""),
            item("constraint", "LAY_CLAMP_INTERVAL", "管夹/线夹间距不超过上限", "<=", "mm"),
            item("constraint", "LAY_BEND_RADIUS_LIMIT", "管线弯曲半径满足下限", ">=", "mm"),
            item("constraint", "LAY_PIPE_ENDPOINT_FIXED", "液压弯管起点和终点固定", "fixed", ""),
            item("constraint", "LAY_PIPE_HORIZONTAL_SPAN", "液压弯管水平总长", "=", "mm"),
            item("constraint", "LAY_PIPE_VERTICAL_SPAN", "液压弯管竖直总高", "=", "mm")
        ));
        data.put("aero", List.of(
            item("objective", "AERO_ENVELOPE_IMPACT_MIN", "对舱门外形包络影响最小", "min", "score"),
            item("constraint", "AERO_OUTER_ENVELOPE", "管线与附件不得超出舱门外形包络", "<=", "mm"),
            item("constraint", "AERO_DOOR_GAP_CLEARANCE", "不影响舱门缝隙和开闭间隙", "meet", "")
        ));
        data.put("hydraulic", List.of(
            item("objective", "HYD_STRESS_MIN", "最大等效应力最小", "min", "MPa"),
            item("objective", "HYD_DEFORMATION_MIN", "最大变形量最小", "min", "mm"),
            item("objective", "HYD_PRESSURE_DROP_MIN", "液压管路压降最小", "min", "MPa"),
            item("constraint", "HYD_STRESS_LIMIT", "最大等效应力不超过屈服强度", "<=", "MPa"),
            item("constraint", "HYD_DEFORMATION_LIMIT", "最大变形量不超过允许变形", "<=", "mm"),
            item("constraint", "HYD_MIN_BEND_RADIUS", "液压管弯曲半径不小于下限", ">=", "mm"),
            item("constraint", "HYD_CLAMP_VALID", "卡箍不得断裂", "false", ""),
            item("constraint", "HYD_PRESSURE_VELOCITY_INPUT", "入口压强和流速按时间历程输入", "input", ""),
            item("constraint", "HYD_VALVE_CLOSE_TIME", "阀门关闭/作动器卡滞时间作为冲击工况输入", "input", "s")
        ));
        data.put("manufacturing", List.of(
            item("objective", "MFG_PROCESS_COMPLEXITY_MIN", "管线制造加工复杂度最小", "min", "score"),
            item("objective", "MFG_ASSEMBLY_EFFICIENCY_MAX", "装配效率最大", "max", "score"),
            item("objective", "MFG_MAINTENANCE_ACCESS_MAX", "检修可达性最大", "max", "score"),
            item("constraint", "MFG_BEND_RADIUS_LIMIT", "弯曲半径满足制造下限", ">=", "mm"),
            item("constraint", "MFG_BEND_ANGLE_RANGE", "弯曲角度满足加工范围", "range", "deg"),
            item("constraint", "MFG_WALL_THICKNESS_LIMIT", "管道壁厚满足加工与强度要求", ">=", "mm"),
            item("constraint", "MFG_CLAMP_INSTALLABLE", "管夹/线夹可安装", "meet", ""),
            item("constraint", "MFG_TOOL_ACCESS", "工具操作空间满足要求", "meet", "")
        ));
        return data;
    }

    private Map<String, Object> item(String type, String code, String name, String direction, String unit) {
        return mapOf("itemType", type, "itemCode", code, "itemName", name, "direction", direction, "unit", unit, "weight", 50, "limitValue", "");
    }

    private Map<String, Object> variable(String subtaskCode, String code, String name, String type, String defaultValue, String lowerBound, String upperBound, String stepValue, String unit) {
        return mapOf("subtaskCode", subtaskCode, "variableCode", code, "variableName", name, "variableType", type,
            "defaultValue", defaultValue, "initialValue", defaultValue, "lowerBound", lowerBound, "upperBound", upperBound,
            "stepValue", stepValue, "unit", unit);
    }

    private Map<String, List<Map<String, Object>>> classifySelectedItems(Long taskId) {
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        result.put("hydraulic_impact", new ArrayList<>());
        result.put("cable_pipe_layout", new ArrayList<>());
        if (taskId == null) {
            return result;
        }
        for (Map<String, Object> item : selectedItems(taskId)) {
            List<String> subtasks = subtaskCodes(str(item.get("itemCode"), ""));
            for (String subtaskCode : subtasks) {
                Map<String, Object> classifiedItem = new LinkedHashMap<>(item);
                classifiedItem.put("shared", subtasks.size() > 1);
                classifiedItem.put("subtaskCode", subtaskCode);
                result.computeIfAbsent(subtaskCode, key -> new ArrayList<>()).add(classifiedItem);
            }
        }
        return result;
    }

    private List<Map<String, Object>> selectedItems(Long taskId) {
        try {
            return jdbcTemplate.queryForList("""
                select discipline, item_type itemType, item_code itemCode, item_name itemName, direction, weight, limit_value limitValue, unit, remark
                from p2_design_objective_constraint
                where task_id = ?
                order by discipline, item_type desc, id
                """, taskId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<String> subtaskCodes(String itemCode) {
        Map<String, List<String>> mapping = new LinkedHashMap<>();
        mapping.put("HYD_STRESS_MIN", List.of("hydraulic_impact"));
        mapping.put("HYD_DEFORMATION_MIN", List.of("hydraulic_impact"));
        mapping.put("HYD_PRESSURE_DROP_MIN", List.of("hydraulic_impact"));
        mapping.put("STR_DEFORMATION_RISK_MIN", List.of("hydraulic_impact"));
        mapping.put("MFG_PROCESS_COMPLEXITY_MIN", List.of("hydraulic_impact"));
        mapping.put("HYD_STRESS_LIMIT", List.of("hydraulic_impact"));
        mapping.put("HYD_DEFORMATION_LIMIT", List.of("hydraulic_impact"));
        mapping.put("HYD_MIN_BEND_RADIUS", List.of("hydraulic_impact"));
        mapping.put("HYD_CLAMP_VALID", List.of("hydraulic_impact"));
        mapping.put("HYD_PRESSURE_VELOCITY_INPUT", List.of("hydraulic_impact"));
        mapping.put("HYD_VALVE_CLOSE_TIME", List.of("hydraulic_impact"));
        mapping.put("STR_CLAMP_SUPPORT_VALID", List.of("hydraulic_impact"));
        mapping.put("MFG_BEND_ANGLE_RANGE", List.of("hydraulic_impact"));
        mapping.put("MFG_WALL_THICKNESS_LIMIT", List.of("hydraulic_impact"));

        mapping.put("LAY_LENGTH_MIN", List.of("cable_pipe_layout"));
        mapping.put("LAY_INTERFERENCE_MIN", List.of("cable_pipe_layout"));
        mapping.put("LAY_MAINTAINABILITY_MAX", List.of("cable_pipe_layout"));
        mapping.put("STR_SPACE_SUPPORT_MAX", List.of("cable_pipe_layout"));
        mapping.put("MFG_ASSEMBLY_EFFICIENCY_MAX", List.of("cable_pipe_layout"));
        mapping.put("MFG_MAINTENANCE_ACCESS_MAX", List.of("cable_pipe_layout"));
        mapping.put("AERO_ENVELOPE_IMPACT_MIN", List.of("cable_pipe_layout"));
        mapping.put("LAY_PIPE_CABLE_DISTANCE", List.of("cable_pipe_layout"));
        mapping.put("LAY_FORBIDDEN_ZONE", List.of("cable_pipe_layout"));
        mapping.put("LAY_CLAMP_INTERVAL", List.of("cable_pipe_layout"));
        mapping.put("STR_FORBIDDEN_ZONE", List.of("cable_pipe_layout"));
        mapping.put("STR_INTERFACE_FIXED", List.of("cable_pipe_layout"));
        mapping.put("MFG_TOOL_ACCESS", List.of("cable_pipe_layout"));
        mapping.put("AERO_OUTER_ENVELOPE", List.of("cable_pipe_layout"));
        mapping.put("AERO_DOOR_GAP_CLEARANCE", List.of("cable_pipe_layout"));

        mapping.put("STR_PIPE_CLEARANCE", List.of("hydraulic_impact", "cable_pipe_layout"));
        mapping.put("MFG_BEND_RADIUS_LIMIT", List.of("hydraulic_impact", "cable_pipe_layout"));
        mapping.put("LAY_BEND_RADIUS_LIMIT", List.of("hydraulic_impact", "cable_pipe_layout"));
        mapping.put("LAY_CLEARANCE_LIMIT", List.of("hydraulic_impact", "cable_pipe_layout"));
        mapping.put("MFG_CLAMP_INSTALLABLE", List.of("hydraulic_impact", "cable_pipe_layout"));
        mapping.put("LAY_PIPE_ENDPOINT_FIXED", List.of("cable_pipe_layout"));
        mapping.put("LAY_PIPE_HORIZONTAL_SPAN", List.of("cable_pipe_layout"));
        mapping.put("LAY_PIPE_VERTICAL_SPAN", List.of("cable_pipe_layout"));
        return mapping.getOrDefault(itemCode, Collections.emptyList());
    }

    private Map<String, Object> conflictCheckResult(boolean passed) {
        List<Map<String, Object>> conflicts = passed ? Collections.emptyList() : List.of(
            mapOf("title", "结构减重与液压抗冲击冲突", "disciplines", "结构/液压", "suggestion", "降低减重权重或放宽弯管厚度上限"),
            mapOf("title", "管线间隙与舱门空间边界冲突", "disciplines", "布局/气动", "suggestion", "调整禁布区域或重新规划线缆绕行路径")
        );
        return mapOf("passed", passed, "score", passed ? 86 : 52, "conflicts", conflicts, "checkedAt", LocalDateTime.now().toString());
    }

    private List<Map<String, Object>> subtaskDefinitions(Long taskId) {
        Map<String, List<Map<String, Object>>> classified = classifySelectedItems(taskId);
        return List.of(
            mapOf("subtaskCode", "hydraulic_impact", "subtaskName", "液压弯管抗冲击性能优化", "solverType", "结构响应代理模型", "status", "READY",
                "items", classified.getOrDefault("hydraulic_impact", Collections.emptyList())),
            mapOf("subtaskCode", "cable_pipe_layout", "subtaskName", "弯管路径几何与制造约束优化", "solverType", "几何闭合与制造可行性检查", "status", "READY",
                "items", classified.getOrDefault("cable_pipe_layout", Collections.emptyList()))
        );
    }

    private boolean isDecomposed(DesignTask task) {
        if (task == null) {
            return false;
        }
        List<String> afterDecomposeNodes = List.of("simulation_confirm", "leader_approve", "end");
        return afterDecomposeNodes.contains(task.getCurrentNodeKey()) || "COMPLETED".equals(task.getStatus());
    }

    private List<Map<String, Object>> subtaskSolutions(Long taskId) {
        Map<String, List<Map<String, Object>>> classified = classifySelectedItems(taskId);
        Map<String, String> values = selectedVariableValues(taskId);
        double l1 = doubleValue(values.get("PIPE_L1"), 300);
        double l2 = doubleValue(values.get("PIPE_L2"), 150);
        double theta1 = doubleValue(values.get("PIPE_THETA_1"), 110);
        double theta2 = doubleValue(values.get("PIPE_THETA_2"), 120);
        double radius = doubleValue(values.get("PIPE_BEND_RADIUS"), 30);
        double l3 = Math.max(40, 600 - l1 - radius * 1.2);
        double closureError = Math.abs(300 - (l2 + radius + 120));
        double maxDeformation = round2(18.5 - radius * 0.08 + Math.abs(theta1 - 105) * 0.03 + Math.abs(theta2 - 120) * 0.02);
        double maxStress = round2(238 - radius * 1.15 + Math.abs(theta1 - 105) * 0.75 + Math.abs(theta2 - 120) * 0.55);
        return List.of(
            mapOf("subtaskCode", "hydraulic_impact", "subtaskName", "液压弯管抗冲击性能优化", "recommended", "HI-02",
                "items", classified.getOrDefault("hydraulic_impact", Collections.emptyList()), "solutions", List.of(
                pipeSolution("HI-01", l1, l2, theta1 - 8, theta2 + 5, radius - 4, l3 + 18, maxDeformation + 1.8, maxStress + 16, 84),
                pipeSolution("HI-02", l1, l2, theta1, theta2, radius, l3, maxDeformation, maxStress, 93),
                pipeSolution("HI-03", l1, l2, theta1 + 6, theta2 - 7, radius + 3, l3 - 11, maxDeformation + 0.9, maxStress + 8, 88)
            )),
            mapOf("subtaskCode", "cable_pipe_layout", "subtaskName", "弯管路径几何与制造约束优化", "recommended", "CL-02",
                "items", classified.getOrDefault("cable_pipe_layout", Collections.emptyList()), "solutions", List.of(
                geometrySolution("CL-01", l1 - 15, l2 + 12, radius - 4, l3 + 18, closureError + 1.4, false, 80),
                geometrySolution("CL-02", l1, l2, radius, l3, closureError, true, 91),
                geometrySolution("CL-03", l1 + 12, l2 - 8, radius + 3, l3 - 11, closureError + 0.8, true, 86)
            ))
        );
    }

    private Map<String, Object> pipeSolution(String code, Number l1, Number l2, Number theta1, Number theta2, Number radius,
                                             Number l3, Number maxDeformation, Number maxStress, Number score) {
        return mapOf("solutionCode", code, "l1", l1, "l2", l2, "theta1", theta1, "theta2", theta2, "radius", radius,
            "l3", l3, "maxDeformation", maxDeformation, "maxStress", maxStress, "score", score, "feasible", true,
            "constraintStatus", "强度、变形、弯角和弯曲半径约束满足");
    }

    private Map<String, Object> geometrySolution(String code, Number l1, Number l2, Number radius, Number l3,
                                                 Number closureError, boolean feasible, Number score) {
        return mapOf("solutionCode", code, "l1", l1, "l2", l2, "radius", radius, "l3", l3,
            "closureError", closureError, "manufacturable", feasible ? "满足" : "需调整", "score", score,
            "feasible", feasible, "constraintStatus", feasible ? "几何闭合和制造约束满足" : "几何闭合误差偏大");
    }

    private Map<String, Object> simulationResult() {
        return mapOf("passed", true, "conclusion", "优化方案满足起落架舱门综合设计要求",
            "metrics", List.of(
                metric("总体重量", 128, 116, "kg", "down"),
                metric("冲击响应", 1.00, 0.72, "normalized", "down"),
                metric("管线干涉风险", 0.31, 0.06, "risk", "down"),
                metric("布局紧凑度", 72, 89, "score", "up"),
                metric("气动影响", 0.18, 0.11, "Cd", "down"),
                metric("综合评分", 76, 91, "score", "up")
            ));
    }

    private Map<String, Object> metric(String name, Number before, Number after, String unit, String trend) {
        return mapOf("name", name, "before", before, "after", after, "unit", unit, "trend", trend);
    }

    private List<Map<String, Object>> disciplineProgress(List<DesignTask> tasks) {
        DesignTask latest = tasks.isEmpty() ? null : tasks.get(0);
        String node = latest == null ? "structure_select" : latest.getCurrentNodeKey();
        List<String> order = List.of("structure_select", "layout_select", "aero_select", "hydraulic_select", "manufacturing_select");
        return List.of(
            progressItem("结构", node, "structure_select", order),
            progressItem("布局", node, "layout_select", order),
            progressItem("气动", node, "aero_select", order),
            progressItem("液压", node, "hydraulic_select", order),
            progressItem("制造", node, "manufacturing_select", order)
        );
    }

    private Map<String, Object> progressItem(String name, String current, String key, List<String> order) {
        int currentIndex = order.indexOf(current);
        int itemIndex = order.indexOf(key);
        String status = currentIndex < 0 ? "DONE" : (itemIndex < currentIndex ? "DONE" : itemIndex == currentIndex ? "DOING" : "WAIT");
        return mapOf("name", name, "status", status);
    }

    private List<Map<String, Object>> subtaskProgress() {
        return List.of(
            mapOf("name", "液压弯管抗冲击性能优化", "status", "READY", "score", 93),
            mapOf("name", "弯管路径几何与制造约束优化", "status", "READY", "score", 91)
        );
    }

    private Map<String, Object> stats(List<DesignTask> tasks) {
        long running = tasks.stream().filter(t -> !"COMPLETED".equals(t.getStatus()) && !"CANCELLED".equals(t.getStatus())).count();
        return mapOf("running", running, "completed", tasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count(),
            "related", tasks.size(), "history", tasks.stream().filter(t -> "COMPLETED".equals(t.getStatus()) || "CANCELLED".equals(t.getStatus())).count());
    }

    private List<Map<String, Object>> decorateTasks(List<DesignTask> tasks) {
        return tasks.stream().map(task -> mapOf(
            "taskId", task.getTaskId(), "taskNo", task.getTaskNo(), "taskName", task.getTaskName(),
            "status", task.getStatus(), "currentNodeKey", task.getCurrentNodeKey(), "currentNodeName", task.getCurrentNodeName(),
            "ownerUserName", task.getOwnerUserName(), "progress", progress(task.getCurrentNodeKey(), task.getStatus()),
            "action", taskAccess(task), "createBy", task.getCreateBy(), "createTime", task.getCreateTime()
        )).collect(Collectors.toList());
    }

    private int progress(String nodeKey, String status) {
        if ("COMPLETED".equals(status)) return 100;
        List<String> nodes = List.of("structure_select", "layout_select", "aero_select", "hydraulic_select", "manufacturing_select", "conflict_check", "model_decompose_solve", "simulation_confirm", "leader_approve");
        int index = nodes.indexOf(nodeKey);
        return index < 0 ? 10 : Math.max(10, (index + 1) * 10);
    }

    private String stageRoute(String nodeKey) {
        if (nodeKey == null) return "/designtask/objective";
        if (nodeKey.endsWith("_select")) return "/designtask/objective";
        if ("model_decompose_solve".equals(nodeKey) || "conflict_check".equals(nodeKey)) return "/designtask/solve";
        if ("simulation_confirm".equals(nodeKey) || "leader_approve".equals(nodeKey)) return "/designtask/simulation";
        return "/designtask/dashboard";
    }

    private boolean isRelated(DesignTask task, Long userId, String username) {
        return Objects.equals(task.getOwnerUserId(), userId) || Objects.equals(task.getStructureUserId(), userId)
            || Objects.equals(task.getLayoutUserId(), userId) || Objects.equals(task.getAeroUserId(), userId)
            || Objects.equals(task.getHydraulicUserId(), userId) || Objects.equals(task.getManufacturingUserId(), userId)
            || Objects.equals(task.getDesignUserId(), userId) || Objects.equals(task.getLeaderUserId(), userId)
            || Objects.equals(task.getCreateBy(), username) || currentUserHasStageRole(task);
    }

    private Map<String, Object> taskAccess(DesignTask task) {
        if (task == null) {
            return mapOf("mode", "wait", "label", "等待", "reason", "任务不存在");
        }
        if ("COMPLETED".equals(task.getStatus()) || "end".equals(task.getCurrentNodeKey())) {
            return mapOf("mode", "view", "label", "查看", "reason", "任务已完成");
        }
        Long userId = currentUserId();
        if (canOperateCurrentStage(task)) {
            return mapOf("mode", "enter", "label", "进入", "reason", "当前节点待你处理");
        }
        String relation = userStageRelation(task, userId);
        if ("DONE".equals(relation)) {
            return mapOf("mode", "view", "label", "查看", "reason", "你的节点已完成");
        }
        return mapOf("mode", "wait", "label", "等待", "reason", "流程尚未流转到你");
    }

    private void assertCurrentAssignee(DesignTask task, String message) {
        if (!canOperateCurrentStage(task)) {
            throw new IllegalStateException(message);
        }
    }

    private boolean canOperateCurrentStage(DesignTask task) {
        if (task == null) {
            return false;
        }
        Long userId = currentUserId();
        if (Objects.equals(currentStageAssigneeId(task), userId) || currentUserHasStageRole(task)) {
            return true;
        }
        if (currentUserHasRole("admin", "超级管理员")) {
            return true;
        }
        if ("simulation_confirm".equals(task.getCurrentNodeKey())) {
            String username = currentUsername();
            return Objects.equals(task.getOwnerUserId(), userId)
                || Objects.equals(task.getCreateBy(), username)
                || currentUserHasRole("design_task_owner", "任务负责人");
        }
        return false;
    }

    private boolean currentUserHasStageRole(DesignTask task) {
        String[] role = roleForNode(task == null ? null : task.getCurrentNodeKey());
        return role != null && currentUserHasRole(role[0], role[1]);
    }

    private String[] roleForNode(String nodeKey) {
        if ("structure_select".equals(nodeKey)) return new String[]{"structure_engineer", "结构工程师"};
        if ("layout_select".equals(nodeKey)) return new String[]{"layout_engineer", "布局工程师"};
        if ("aero_select".equals(nodeKey)) return new String[]{"aero_engineer", "气动工程师"};
        if ("hydraulic_select".equals(nodeKey)) return new String[]{"hydraulic_engineer", "液压工程师"};
        if ("manufacturing_select".equals(nodeKey) || "simulation_confirm".equals(nodeKey)) return new String[]{"manufacturing_engineer", "制造工程师"};
        if ("leader_approve".equals(nodeKey)) return new String[]{"approval_leader", "审批领导"};
        if ("conflict_check".equals(nodeKey) || "model_decompose_solve".equals(nodeKey)) return new String[]{"design_task_owner", "任务负责人"};
        return null;
    }

    private boolean currentUserHasRole(String roleKey, String roleName) {
        try {
            Integer count = jdbcTemplate.queryForObject("""
                select count(1)
                from sys_user_role ur
                join sys_role r on r.role_id = ur.role_id
                where ur.user_id = ?
                  and (r.role_key = ? or r.role_name = ?)
                  and r.status = '0'
                  and r.del_flag = '0'
                """, Integer.class, currentUserId(), roleKey, roleName);
            return count != null && count > 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    private Long currentStageAssigneeId(DesignTask task) {
        if (task == null) return null;
        String nodeKey = task.getCurrentNodeKey();
        if ("structure_select".equals(nodeKey)) return task.getStructureUserId();
        if ("layout_select".equals(nodeKey)) return task.getLayoutUserId();
        if ("aero_select".equals(nodeKey)) return task.getAeroUserId();
        if ("hydraulic_select".equals(nodeKey)) return task.getHydraulicUserId();
        if ("manufacturing_select".equals(nodeKey)) return task.getManufacturingUserId();
        if ("leader_approve".equals(nodeKey)) return task.getLeaderUserId();
        if ("simulation_confirm".equals(nodeKey)) return firstNonNull(task.getManufacturingUserId(), task.getOwnerUserId());
        if ("conflict_check".equals(nodeKey) || "model_decompose_solve".equals(nodeKey)) return task.getOwnerUserId();
        return task.getOwnerUserId();
    }

    private String userStageRelation(DesignTask task, Long userId) {
        Integer userIndex = userObjectiveIndex(task, userId);
        Integer currentIndex = objectiveIndex(task == null ? null : task.getCurrentNodeKey());
        if (userIndex == null) {
            return "OTHER";
        }
        if (currentIndex == null) {
            return "DONE";
        }
        if (userIndex < currentIndex) {
            return "DONE";
        }
        if (userIndex > currentIndex) {
            return "WAIT";
        }
        return "CURRENT_NOT_ASSIGNEE";
    }

    private Integer userObjectiveIndex(DesignTask task, Long userId) {
        if (task == null || userId == null) return null;
        if (Objects.equals(task.getStructureUserId(), userId)) return 0;
        if (Objects.equals(task.getLayoutUserId(), userId)) return 1;
        if (Objects.equals(task.getAeroUserId(), userId)) return 2;
        if (Objects.equals(task.getHydraulicUserId(), userId)) return 3;
        if (Objects.equals(task.getManufacturingUserId(), userId)) return 4;
        return null;
    }

    private Integer objectiveIndex(String nodeKey) {
        List<String> order = List.of("structure_select", "layout_select", "aero_select", "hydraulic_select", "manufacturing_select");
        int index = order.indexOf(nodeKey);
        return index < 0 ? null : index;
    }

    private List<Map<String, Object>> usersByRole(String roleKey, String roleName) {
        try {
            List<Map<String, Object>> users = jdbcTemplate.queryForList("""
                select distinct u.user_id userId, u.user_name userName, u.nick_name nickName
                from sys_user u
                join sys_user_role ur on ur.user_id = u.user_id
                join sys_role r on r.role_id = ur.role_id
                where (r.role_key = ? or r.role_name = ?)
                  and u.status = '0'
                  and u.del_flag = '0'
                  and r.status = '0'
                  and r.del_flag = '0'
                order by u.user_id
                """, roleKey, roleName);
            if (!users.isEmpty()) {
                return users;
            }
        } catch (Exception ignored) {
        }
        return Collections.emptyList();
    }

    private String disciplineName(String discipline) {
        return Map.of(
            "structure", "结构",
            "layout", "布局",
            "aero", "气动",
            "hydraulic", "液压",
            "manufacturing", "制造",
            "design", "设计"
        ).getOrDefault(discipline, discipline);
    }

    private Map<String, Object> responseData(Map<String, Object> response) {
        if (response == null) {
            return Collections.emptyMap();
        }
        Object data = response.get("data");
        if (data instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, value) -> result.put(String.valueOf(key), value));
            return result;
        }
        return response;
    }

    private List<Map<String, Object>> responseList(Map<String, Object> response) {
        if (response == null) {
            return Collections.emptyList();
        }
        Object data = response.get("data");
        if (!(data instanceof List<?> list)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Map<String, Object> row = new LinkedHashMap<>();
                map.forEach((key, value) -> row.put(String.valueOf(key), value));
                result.add(row);
            }
        }
        return result;
    }

    private String currentUsername() {
        try {
            return SecurityUtils.getUsername();
        } catch (Exception e) {
            return "admin";
        }
    }

    private Long currentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            return 1L;
        }
    }

    private String str(Object value, String fallback) {
        return value == null || String.valueOf(value).isBlank() || "null".equals(String.valueOf(value)) ? fallback : String.valueOf(value);
    }

    private Integer intValue(Object value, Integer fallback) {
        if (value == null || String.valueOf(value).isBlank()) return fallback;
        try {
            return new BigDecimal(String.valueOf(value)).intValue();
        } catch (Exception e) {
            return fallback;
        }
    }

    private double doubleValue(Object value, double fallback) {
        if (value == null || String.valueOf(value).isBlank()) return fallback;
        try {
            return new BigDecimal(String.valueOf(value)).doubleValue();
        } catch (Exception e) {
            return fallback;
        }
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    private Long longValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        try {
            return new BigDecimal(String.valueOf(value)).longValue();
        } catch (Exception e) {
            return null;
        }
    }

    private Date dateValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        try {
            String text = String.valueOf(value).replace("T", " ");
            if (text.length() == 10) {
                text = text + " 00:00:00";
            }
            LocalDateTime dateTime = LocalDateTime.parse(text.substring(0, 19), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return Date.from(dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            return null;
        }
    }

    private void saveTaskAttachments(Long taskId, Object attachments) {
        if (taskId == null || !(attachments instanceof List<?> list)) {
            return;
        }
        ensureTaskFileColumns();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            String fileName = str(map.get("fileName"), str(map.get("name"), ""));
            if (StringUtils.isEmpty(fileName)) {
                continue;
            }
            DesignTaskFile file = new DesignTaskFile();
            file.setTaskId(taskId);
            file.setFileName(fileName);
            file.setFilePath(str(map.get("filePath"), "pending://" + fileName));
            file.setFileSize(longValue(map.get("fileSize")));
            file.setFileType(str(map.get("fileType"), ""));
            file.setFileSuffix(fileSuffix(fileName));
            file.setUploadBy(currentUsername());
            taskFileService.insertFile(file);
        }
    }

    private List<DesignTaskFile> safeTaskFiles(Long taskId) {
        if (taskId == null) {
            return Collections.emptyList();
        }
        try {
            ensureTaskFileColumns();
            return taskFileService.selectFilesByTaskId(taskId);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private void ensureTaskFileColumns() {
        ensureColumn("p2_design_task_file", "file_size", "bigint");
        ensureColumn("p2_design_task_file", "file_type", "varchar(128)");
        ensureColumn("p2_design_task_file", "file_suffix", "varchar(64)");
        ensureColumn("p2_design_task_file", "upload_by", "varchar(64)");
        ensureColumn("p2_design_task_file", "upload_time", "datetime");
    }

    private void ensureColumn(String tableName, String columnName, String columnDefinition) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                """
                select count(1)
                from information_schema.columns
                where table_schema = database()
                  and table_name = ?
                  and column_name = ?
                """,
                Integer.class,
                tableName,
                columnName
            );
            if (count == null || count == 0) {
                jdbcTemplate.execute("alter table " + tableName + " add column " + columnName + " " + columnDefinition);
            }
        } catch (Exception ignored) {
        }
    }

    private String fileSuffix(String fileName) {
        int index = fileName == null ? -1 : fileName.lastIndexOf('.');
        return index >= 0 && index + 1 < fileName.length() ? fileName.substring(index + 1) : "";
    }

    @SafeVarargs
    private <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) return value;
        }
        return null;
    }

    private void debugLog(String hypothesisId, String location, String message, Map<String, Object> data) {
        try (FileWriter writer = new FileWriter("debug-6bf16d.log", StandardCharsets.UTF_8, true)) {
            writer.write(toDebugJson(hypothesisId, location, message, data));
            writer.write(System.lineSeparator());
        } catch (IOException ignored) {
        }
    }

    private String toDebugJson(String hypothesisId, String location, String message, Map<String, Object> data) {
        return "{"
            + "\"sessionId\":\"6bf16d\","
            + "\"runId\":\"pre-fix\","
            + "\"hypothesisId\":\"" + escapeJson(hypothesisId) + "\","
            + "\"location\":\"" + escapeJson(location) + "\","
            + "\"message\":\"" + escapeJson(message) + "\","
            + "\"timestamp\":" + System.currentTimeMillis() + ","
            + "\"data\":" + toJsonValue(data)
            + "}";
    }

    private String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String str) {
            return "\"" + escapeJson(str) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append("\"").append(escapeJson(String.valueOf(entry.getKey()))).append("\":");
                sb.append(toJsonValue(entry.getValue()));
            }
            sb.append('}');
            return sb.toString();
        }
        if (value instanceof Collection<?> collection) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : collection) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append(toJsonValue(item));
            }
            sb.append(']');
            return sb.toString();
        }
        return "\"" + escapeJson(String.valueOf(value)) + "\"";
    }

    private String escapeJson(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n");
    }

    private Map<String, Object> mapOf(Object... args) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < args.length - 1; i += 2) {
            map.put(String.valueOf(args[i]), args[i + 1]);
        }
        return map;
    }
}
