package com.ruoyi.project3.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.file.FileUtils;
import com.ruoyi.project3.service.MonitorService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/monitor")
public class MonitorController {

    @Resource
    private MonitorService monitorService;

    @GetMapping("/tree")
    public AjaxResult getTree() {
        return AjaxResult.success(monitorService.getTree());
    }

    @GetMapping("/node/{node_id}/view")
    public AjaxResult getNodeView(@PathVariable("node_id") String nodeId) {
        return AjaxResult.success(monitorService.getNodeView(nodeId));
    }

    @GetMapping("/parts")
    public AjaxResult getPartInstances(
            @RequestParam(value = "module_id", required = false) String moduleId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "material", required = false) String material,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page_num", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer pageSize
    ) {
        return AjaxResult.success(monitorService.getPartInstances(moduleId, keyword, material, status, pageNum, pageSize));
    }

    @GetMapping("/parts/{part_instance_id}/quality")
    public AjaxResult getPartQualityTables(@PathVariable("part_instance_id") String partInstanceId) {
        return AjaxResult.success(monitorService.getPartQualityTables(partInstanceId));
    }

    @PostMapping("/module")
    public AjaxResult createModule(@RequestBody Map<String, Object> payload) {
        return AjaxResult.success(monitorService.createModule(
                getString(payload, "current_node_id"),
                getString(payload, "module_name")
        ));
    }

    @DeleteMapping("/module/{node_id}")
    public AjaxResult deleteModule(@PathVariable("node_id") String nodeId) {
        monitorService.deleteModule(nodeId);
        return AjaxResult.success();
    }

    @PostMapping("/parts")
    public AjaxResult createPartInstance(@RequestBody Map<String, Object> payload) {
        return AjaxResult.success(monitorService.createPartInstance(
                getString(payload, "module_id"),
                getString(payload, "part_code"),
                getString(payload, "part_name"),
                getString(payload, "material"),
                getString(payload, "spec_model"),
                getString(payload, "serial_number"),
                getString(payload, "batch_number"),
                getString(payload, "manufacturer"),
                getString(payload, "production_date"),
                getString(payload, "status"),
                getString(payload, "quality_level"),
                getString(payload, "key_degree")
        ));
    }

    @PostMapping("/part-template")
    public AjaxResult createPartTemplate(@RequestBody Map<String, Object> payload) {
        return AjaxResult.success(monitorService.createPartTemplate(
                getString(payload, "module_id"),
                getString(payload, "part_code"),
                getString(payload, "part_name"),
                getString(payload, "material"),
                getString(payload, "spec_model")
        ));
    }

    @DeleteMapping("/parts/{part_instance_id}")
    public AjaxResult deletePartInstance(@PathVariable("part_instance_id") String partInstanceId) {
        monitorService.deletePartInstance(partInstanceId);
        return AjaxResult.success();
    }

    @PutMapping("/parts/{part_instance_id}")
    public AjaxResult updatePartInstance(@PathVariable("part_instance_id") String partInstanceId, @RequestBody Map<String, Object> payload) {
        monitorService.updatePartInstance(
                partInstanceId,
                getString(payload, "serial_number"),
                getString(payload, "batch_number"),
                getString(payload, "manufacturer"),
                getString(payload, "production_date"),
                getString(payload, "status"),
                getString(payload, "quality_level"),
                getString(payload, "key_degree")
        );
        return AjaxResult.success();
    }

    @PostMapping("/text/process/import")
    public AjaxResult importProcessText(
            @RequestParam("componentId") String componentId,
            @RequestParam("file") MultipartFile file
    ) {
        return AjaxResult.success("导入完成", monitorService.importProcessText(componentId, file));
    }

    @GetMapping("/text/process/template")
    public void downloadProcessTemplate(HttpServletResponse response) throws Exception {
        String fileName = "零件工序导入模板.xlsx";
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        FileUtils.setAttachmentResponseHeader(response, fileName);
        monitorService.writeProcessTemplate(response.getOutputStream());
    }

    @PostMapping("/text/hierarchy/import")
    public AjaxResult importHierarchy(@RequestParam("file") MultipartFile file) {
        return AjaxResult.success("导入完成", monitorService.importHierarchy(file));
    }

    @GetMapping("/text/hierarchy/template")
    public void downloadHierarchyTemplate(HttpServletResponse response) throws Exception {
        String fileName = "层级对象导入模板.xlsx";
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        FileUtils.setAttachmentResponseHeader(response, fileName);
        monitorService.writeHierarchyTemplate(response.getOutputStream());
    }

    private String getString(Map<String, Object> payload, String field) {
        Object value = payload == null ? null : payload.get(field);
        return value == null ? null : String.valueOf(value);
    }
}


