package com.ruoyi.designtask1.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
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

    @GetMapping("/task/{taskId}/archive")
    public AjaxResult taskArchive(@PathVariable Long taskId) {
        return AjaxResult.success(optimizationService.archive(taskId));
    }

    @GetMapping("/objective/catalog/{discipline}")
    public AjaxResult objectiveCatalog(@PathVariable String discipline) {
        return AjaxResult.success(optimizationService.objectiveCatalog(discipline));
    }

    @GetMapping("/design-variable/catalog/{discipline}")
    public AjaxResult designVariableCatalog(@PathVariable String discipline) {
        return AjaxResult.success(optimizationService.designVariableCatalog(discipline));
    }

    @GetMapping("/fault-pipe-parameters/default")
    public AjaxResult defaultFaultPipeParameters() {
        return AjaxResult.success(optimizationService.defaultFaultPipeParameters());
    }

    @GetMapping("/task/{taskId}/fault-pipe-parameters")
    public AjaxResult taskFaultPipeParameters(@PathVariable Long taskId) {
        return AjaxResult.success(optimizationService.faultPipeParameters(taskId));
    }

    @PostMapping("/task/{taskId}/objective-constraints")
    public AjaxResult saveObjectiveConstraints(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.saveObjectiveConstraints(taskId, body));
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

    @PostMapping("/task/{taskId}/ansys-simulation")
    public AjaxResult submitAnsysSimulation(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.submitAnsysSimulation(taskId, body));
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

    @PostMapping("/task/{taskId}/approve")
    public AjaxResult approve(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(optimizationService.approve(taskId, body));
    }
}
