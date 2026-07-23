package com.ruoyi.designtask1.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresRoles;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.designtask1.domain.DesignTaskFile;
import com.ruoyi.designtask1.service.DesignOptimizationService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLConnection;
import java.util.Map;

import com.ruoyi.common.core.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping({"", "/designtask"})
public class DesignOptimizationController {

    private final DesignOptimizationService optimizationService;

    public DesignOptimizationController(DesignOptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    @GetMapping("/dashboard")
    public AjaxResult dashboard(String scope, String status) {
        return AjaxResult.success(optimizationService.dashboard(scope, status));
    }

    @GetMapping("/process/definitions")
    public AjaxResult processDefinitions() {
        return AjaxResult.success(optimizationService.processDefinitions());
    }

    @PostMapping("/process/deploy-default")
    public AjaxResult deployDefaultProcess() {
        return AjaxResult.success(optimizationService.deployDefaultProcess());
    }

    @GetMapping("/process/definition/{processDefinitionId}/nodes")
    public AjaxResult processDefinitionNodes(@PathVariable String processDefinitionId) {
        return AjaxResult.success(optimizationService.processDefinitionNodes(processDefinitionId));
    }

    @GetMapping("/assignee-options")
    public AjaxResult assigneeOptions() {
        return AjaxResult.success(optimizationService.assigneeOptions());
    }

    @PostMapping("/task/start")
    public AjaxResult startTask(@RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.startTask(body));
    }

    @PostMapping("/task/upload-attachment")
    public AjaxResult uploadTaskAttachment(@RequestParam("file") MultipartFile file) {
        return AjaxResult.success(optimizationService.uploadTaskAttachment(file));
    }

    @GetMapping("/task/attachment/{fileId}")
    public ResponseEntity<Resource> taskAttachment(@PathVariable Long fileId) {
        DesignTaskFile taskFile = optimizationService.taskAttachment(fileId);
        File file = new File(taskFile.getFilePath());
        Resource resource = new FileSystemResource(file);
        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        return ResponseEntity.ok()
            .contentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
            .body(resource);
    }

    @GetMapping("/task/detail/{taskId}")
    public AjaxResult taskDetail(@PathVariable Long taskId) {
        return AjaxResult.success(optimizationService.detail(taskId));
    }

    @GetMapping("/task/quality-task/{qualityTaskId}")
    public AjaxResult taskByQualityTask(@PathVariable Long qualityTaskId) {
        return AjaxResult.success(optimizationService.taskByQualityTask(qualityTaskId));
    }

    @GetMapping("/task/{taskId}/archive")
    public AjaxResult taskArchive(@PathVariable Long taskId) {
        return AjaxResult.success(optimizationService.archive(taskId));
    }

    @RequiresRoles("admin")
    @Log(title = "设计任务归档数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/task/{taskId}/archive-data")
    public AjaxResult deleteArchivedTaskData(@PathVariable Long taskId) {
        if (!SecurityUtils.isAdmin()) {
            return AjaxResult.error("仅超级管理员可以删除归档任务数据");
        }
        return AjaxResult.success("删除成功", optimizationService.deleteArchivedTaskData(taskId));
    }

    @GetMapping("/objective/catalog/{discipline}")
    public AjaxResult objectiveCatalog(@PathVariable String discipline, Long taskId, String taskType) {
        return AjaxResult.success(optimizationService.objectiveCatalog(discipline, taskId, taskType));
    }

    @GetMapping("/design-variable/catalog/{discipline}")
    public AjaxResult designVariableCatalog(@PathVariable String discipline) {
        return AjaxResult.success(optimizationService.designVariableCatalog(discipline));
    }

    @GetMapping("/fault-pipe-parameters/default")
    public AjaxResult defaultFaultPipeParameters() {
        return AjaxResult.success(optimizationService.defaultFaultPipeParameters());
    }

    @GetMapping("/fault-pipe-parameters/options")
    public AjaxResult faultPipeParameterOptions() {
        return AjaxResult.success(optimizationService.faultPipeParameterOptions());
    }

    @GetMapping("/task/{taskId}/fault-pipe-parameters")
    public AjaxResult taskFaultPipeParameters(@PathVariable Long taskId) {
        return AjaxResult.success(optimizationService.faultPipeParameters(taskId));
    }

    @PostMapping("/task/{taskId}/objective-constraints")
    public AjaxResult saveObjectiveConstraints(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.saveObjectiveConstraints(taskId, body));
    }

    @PostMapping("/task/{taskId}/objective-weights")
    public AjaxResult saveObjectiveWeights(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.saveObjectiveWeights(taskId, body));
    }

    @PostMapping("/task/{taskId}/design-variables")
    public AjaxResult saveDesignVariables(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.saveDesignVariables(taskId, body));
    }

    @PostMapping("/task/{taskId}/conflict-check")
    public AjaxResult conflictCheck(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.conflictCheck(taskId, body));
    }

    @PostMapping("/task/{taskId}/decompose")
    public AjaxResult decompose(@PathVariable Long taskId) {
        return AjaxResult.success(optimizationService.decompose(taskId));
    }

    @PostMapping("/task/{taskId}/solve")
    public AjaxResult solve(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.solve(taskId, body));
    }

    @PostMapping("/task/{taskId}/surrogate-solve")
    public AjaxResult submitSurrogateSolve(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.submitSurrogateSolve(taskId, body));
    }

    @GetMapping("/surrogate-models")
    public AjaxResult surrogateModels() {
        return AjaxResult.success(optimizationService.surrogateModels());
    }

    @GetMapping("/task/{taskId}/surrogate-solve")
    public AjaxResult surrogateSolve(@PathVariable Long taskId) {
        return AjaxResult.success(optimizationService.surrogateSolve(taskId));
    }

    @PostMapping("/task/{taskId}/surrogate-solve/confirm")
    public AjaxResult confirmSurrogateSolve(@PathVariable Long taskId) {
        return AjaxResult.success(optimizationService.confirmSurrogateSolve(taskId));
    }

    @PostMapping("/task/{taskId}/simulation")
    public AjaxResult simulation(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.simulation(taskId, body));
    }

    @PostMapping("/task/{taskId}/design-report")
    public AjaxResult submitDesignReport(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.submitDesignReport(taskId, body));
    }

    @GetMapping("/task/{taskId}/design-report")
    public AjaxResult designReport(@PathVariable Long taskId) {
        return AjaxResult.success(optimizationService.designReport(taskId));
    }

    @PostMapping("/task/{taskId}/ansys-simulation")
    public AjaxResult submitAnsysSimulation(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.submitAnsysSimulation(taskId, body));
    }

    @PostMapping("/task/{taskId}/ansys-simulation/params")
    public AjaxResult saveAnsysSimulationParams(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.saveAnsysSimulationParams(taskId, body));
    }

    @PostMapping("/task/{taskId}/ansys-simulation/open")
    public AjaxResult openAnsysSimulation(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.openAnsysSimulation(taskId, body));
    }

    @PostMapping("/task/{taskId}/ansys-simulation/import-result")
    public AjaxResult importAnsysSimulationResult(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.importAnsysSimulationResult(taskId, body));
    }

    @PostMapping("/task/{taskId}/ansys-simulation/import-result-file")
    public AjaxResult importAnsysResultFile(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.importAnsysResultFile(taskId, body));
    }

    @GetMapping("/task/{taskId}/ansys-simulation")
    public AjaxResult ansysSimulation(@PathVariable Long taskId, @RequestParam(required = false) String simulationMode) {
        return AjaxResult.success(optimizationService.ansysSimulation(taskId, simulationMode));
    }

    @GetMapping("/task/{taskId}/ansys-simulation/image")
    public ResponseEntity<Resource> ansysSimulationImage(@PathVariable Long taskId, @RequestParam(required = false) String simulationMode) {
        File file = optimizationService.ansysSimulationImage(taskId, simulationMode);
        Resource resource = new FileSystemResource(file);
        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        return ResponseEntity.ok()
            .contentType(contentType == null ? MediaType.IMAGE_PNG : MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
            .body(resource);
    }

    @PostMapping("/task/{taskId}/cad-model")
    public AjaxResult submitCadModel(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.submitCadModel(taskId, body));
    }

    @GetMapping("/task/{taskId}/cad-model")
    public AjaxResult cadModel(@PathVariable Long taskId) {
        return AjaxResult.success(optimizationService.cadModel(taskId));
    }

    @GetMapping("/task/{taskId}/cad-model/file/{kind}")
    public ResponseEntity<Resource> cadModelFile(@PathVariable Long taskId, @PathVariable String kind) {
        File file = optimizationService.cadModelFile(taskId, kind);
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
            .body(resource);
    }

    @GetMapping("/cable-routing/default-params")
    public AjaxResult cableRoutingDefaultParams() {
        return AjaxResult.success(optimizationService.cableRoutingDefaultParams());
    }

    @GetMapping("/cable-routing/algorithms")
    public AjaxResult cableRoutingAlgorithms() {
        return AjaxResult.success(optimizationService.cableRoutingAlgorithms());
    }

    @PostMapping("/task/{taskId}/cable-routing/solve")
    public AjaxResult submitCableRoutingSolve(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.submitCableRoutingSolve(taskId, body));
    }

    @GetMapping("/task/{taskId}/cable-routing/solve")
    public AjaxResult cableRoutingSolve(@PathVariable Long taskId) {
        return AjaxResult.success(optimizationService.cableRoutingSolve(taskId));
    }

    @PostMapping("/task/{taskId}/cable-routing/model")
    public AjaxResult submitCableRoutingModel(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.submitCableRoutingModel(taskId, body));
    }

    @GetMapping("/task/{taskId}/cable-routing/model")
    public AjaxResult cableRoutingModel(@PathVariable Long taskId) {
        return AjaxResult.success(optimizationService.cableRoutingModel(taskId));
    }

    @PostMapping("/task/{taskId}/cable-routing/report")
    public AjaxResult submitCableRoutingReport(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.submitCableRoutingReport(taskId, body));
    }

    @GetMapping("/task/{taskId}/cable-routing/report")
    public AjaxResult cableRoutingReport(@PathVariable Long taskId) {
        return AjaxResult.success(optimizationService.subtaskSubmission(taskId, "cable_pipe_layout"));
    }

    @GetMapping("/task/{taskId}/cable-routing/model/file/{kind}")
    public ResponseEntity<Resource> cableRoutingModelFile(@PathVariable Long taskId, @PathVariable String kind) {
        File file = optimizationService.cableRoutingModelFile(taskId, kind);
        Resource resource = new FileSystemResource(file);
        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        boolean inline = kind != null && kind.toLowerCase().contains("preview");
        return ResponseEntity.ok()
            .contentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, (inline ? "inline" : "attachment") + "; filename=\"" + file.getName() + "\"")
            .body(resource);
    }

    @PostMapping("/task/{taskId}/approve")
    public AjaxResult approve(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.approve(taskId, body));
    }

    //--------------------------------------课题二报告---------------------------
    @Value("${topic2.report-dir:D:/2.11/data/topic2/report}")
    private String topic2ReportDir;

    @GetMapping("/quality/report/downloadByPath")
    public void downloadTopic2ReportByPath(String filePath, HttpServletResponse response) throws Exception
    {
        if (filePath == null || "".equals(filePath.trim()))
        {
            throw new ServiceException("报告路径不能为空");
        }

        String normalizedPath = filePath.replace("\\", "/");

        if (!normalizedPath.startsWith("/profile/topic2/report/"))
        {
            throw new ServiceException("非法报告路径：" + filePath);
        }

        String fileName = normalizedPath.substring(normalizedPath.lastIndexOf("/") + 1);

        if (fileName == null || "".equals(fileName.trim()))
        {
            throw new ServiceException("报告文件名不能为空");
        }

        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\"))
        {
            throw new ServiceException("非法报告文件名：" + fileName);
        }

        String lowerName = fileName.toLowerCase();

        if (!lowerName.endsWith(".doc") && !lowerName.endsWith(".docx"))
        {
            throw new ServiceException("当前文件不是Word报告：" + fileName);
        }

        Path baseDir = Paths.get(topic2ReportDir).toAbsolutePath().normalize();
        Path reportPath = baseDir.resolve(fileName).normalize();

        if (!reportPath.startsWith(baseDir))
        {
            throw new ServiceException("非法报告访问路径：" + filePath);
        }

        if (!Files.exists(reportPath))
        {
            throw new ServiceException("报告文件不存在：" + reportPath);
        }

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20");

        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        Files.copy(reportPath, response.getOutputStream());
    }
}
