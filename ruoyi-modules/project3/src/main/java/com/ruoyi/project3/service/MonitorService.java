package com.ruoyi.project3.service;

import com.ruoyi.project3.domain.ModuleNode;
import com.ruoyi.project3.domain.ModuleNodeView;
import com.ruoyi.project3.domain.PageRows;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface MonitorService {

    List<ModuleNode> getTree();
    ModuleNodeView getNodeView(String nodeId);

    PageRows getPartInstances(
            String moduleId,
            String keyword,
            String material,
            String status,
            Integer pageNum,
            Integer pageSize
    );

    Map<String, Object> getPartQualityTables(String partInstanceId);

    Map<String, Object> createModule(String currentNodeId, String moduleName);

    void deleteModule(String nodeId);

    Map<String, Object> createPartInstance(
            String moduleId,
            String partCode,
            String partName,
            String material,
            String specModel,
            String serialNumber,
            String batchNumber,
            String manufacturer,
            String productionDate,
            String status,
            String qualityLevel,
            String keyDegree
    );

    Map<String, Object> createPartTemplate(
            String moduleId,
            String partCode,
            String partName,
            String material,
            String specModel
    );

    void updatePartInstance(
            String partInstanceId,
            String serialNumber,
            String batchNumber,
            String manufacturer,
            String productionDate,
            String status,
            String qualityLevel,
            String keyDegree
    );

    void deletePartInstance(String partInstanceId);

    Map<String, Object> importProcessText(String componentId, MultipartFile file);

    void writeProcessTemplate(OutputStream outputStream) throws IOException;

    Map<String, Object> importHierarchy(MultipartFile file);

    void writeHierarchyTemplate(OutputStream outputStream) throws IOException;
}


