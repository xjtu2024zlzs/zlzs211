package com.ruoyi.project1.dossier.service.impl;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.uuid.IdUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.project1.dossier.mapper.DossierOpenApiMapper;
import com.ruoyi.project1.dossier.service.IDossierOpenApiService;

@Service
public class DossierOpenApiServiceImpl implements IDossierOpenApiService
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String DISPLAY_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final String DEFAULT_PROJECT3_PART_NUMBER = "HYD-TUBE-MLG-32A";

    private static final String DEFAULT_PROJECT3_BOM_NODE_ID = "f1000006-0006-4006-8006-000000000006";

    @Autowired
    private DossierOpenApiMapper openApiMapper;

    @Override
    public Map<String, Object> selectDossierPage(Map<String, Object> query)
    {
        Map<String, Object> params = normalizePageQuery(safeMap(query));
        int total = openApiMapper.selectDossierCount(params);
        List<Map<String, Object>> rows = normalizeRows(openApiMapper.selectDossierList(params));

        Map<String, Object> result = map();
        result.put("rows", rows);
        result.put("total", total);
        result.put("pageNum", params.get("pageNum"));
        result.put("pageSize", params.get("pageSize"));
        return result;
    }

    @Override
    public Map<String, Object> selectDossierDetail(String instanceId)
    {
        Map<String, Object> detail = normalizeRow(openApiMapper.selectDossierDetail(instanceId));
        if (detail.isEmpty())
        {
            throw new ServiceException("Dossier instance not found.");
        }
        detail.put("versions", selectDossierVersions(instanceId));
        detail.put("summary", selectDossierSummary(instanceId));
        return detail;
    }

    @Override
    public List<Map<String, Object>> selectDossierVersions(String instanceId)
    {
        List<Map<String, Object>> rows = normalizeRows(openApiMapper.selectDossierVersions(instanceId));
        for (Map<String, Object> row : rows)
        {
            parseJsonField(row, "contentSummaryJson", "contentSummary");
            parseJsonField(row, "generationParamsJson", "generationParams");
            parseJsonField(row, "resultSummaryJson", "resultSummary");
            parseJsonField(row, "outputJson", "output");
        }
        return rows;
    }

    @Override
    public Map<String, Object> selectDossierSummary(String instanceId)
    {
        return normalizeRow(openApiMapper.selectDossierSummary(instanceId));
    }

    @Override
    public Map<String, Object> selectProductStructure(String productId, Map<String, Object> query)
    {
        query = safeMap(query);
        String parentId = blankToNull(query.get("parentId"));
        Map<String, Object> result = map();
        result.put("productId", productId);
        result.put("productType", defaultText(query.get("productType"), "aircraft"));
        result.put("parentId", parentId);
        result.put("nodes", selectBomChildren(productId, parentId));
        return result;
    }

    @Override
    public List<Map<String, Object>> selectBomChildren(String aircraftId, String parentId)
    {
        return normalizeBomNodes(openApiMapper.selectBomChildren(aircraftId, blankToNull(parentId)));
    }

    @Override
    public List<Map<String, Object>> searchBomNodes(String aircraftId, String keyword)
    {
        if (!hasText(keyword))
        {
            return new ArrayList<>();
        }
        return normalizeBomNodes(openApiMapper.searchBomNodes(aircraftId, keyword));
    }

    @Override
    public Map<String, Object> selectBomNode(String nodeId)
    {
        Map<String, Object> node = normalizeBomNode(openApiMapper.selectBomNode(nodeId));
        if (node.isEmpty())
        {
            throw new ServiceException("BOM node not found.");
        }
        return node;
    }

    @Override
    public List<Map<String, Object>> selectBomPath(String nodeId)
    {
        return normalizeBomNodes(openApiMapper.selectBomPath(nodeId));
    }

    @Override
    public List<Map<String, Object>> selectBomRelations(String nodeId)
    {
        return normalizeBomNodes(openApiMapper.selectBomRelations(nodeId));
    }

    @Override
    public List<Map<String, Object>> selectDictionaries()
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(dictionary("object_level", "Object Level", "aircraft:Aircraft", "system:System",
                "subsystem:Subsystem", "equipment:Equipment", "component:Component", "part:Part"));
        rows.add(dictionary("lifecycle_stage", "Lifecycle Stage", "DESIGN:Design", "MANUFACTURING:Manufacturing",
                "SERVICE:Service", "DOSSIER:Dossier"));
        rows.add(dictionary("dossier_status", "Dossier Status", "building:Building", "ready:Ready",
                "published:Published", "archived:Archived"));
        rows.add(dictionary("completeness_status", "Completeness Status", "complete:Complete", "warning:Warning",
                "missing:Missing", "error:Error"));
        rows.add(dictionary("writeback_type", "Writeback Type", "optimization:Optimization",
                "quality_monitoring:Quality Monitoring", "fault_diagnosis:Fault Diagnosis",
                "traceability:Traceability", "data_completion:Data Completion"));
        return rows;
    }

    @Override
    public List<Map<String, Object>> selectQualityCharacteristics()
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(quality("KQC-STRUCTURE", "Structural Integrity",
                "Structure, fatigue, assembly stress and related key characteristics.", "DESIGN/MANUFACTURING"));
        rows.add(quality("KQC-HYDRAULIC", "Hydraulic Sealing And Pressure",
                "Pressure test, leakage, cleanliness and sealing status.", "MANUFACTURING/SERVICE"));
        rows.add(quality("KQC-DIMENSION", "Dimension And Tolerance",
                "Dimension recheck, form tolerance and assembly clearance.", "MANUFACTURING"));
        rows.add(quality("KQC-FAULT", "Fault Symptom And Cause",
                "Fault location, category, cause, evidence and confidence.", "SERVICE"));
        rows.add(quality("KQC-TRACE", "Trace Chain Completeness",
                "Batch, process, supplier, installation and service trace chain.", "DOSSIER"));
        return rows;
    }

    @Override
    public List<Map<String, Object>> selectNodeQualityCharacteristics(String nodeId)
    {
        List<Map<String, Object>> rows = normalizeRows(openApiMapper.selectNodeQualityCharacteristics(nodeId));
        for (Map<String, Object> row : rows)
        {
            parseJsonField(row, "attrsJson", "attrs");
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> selectProductQualityFeatures(String productId, Map<String, Object> query)
    {
        Map<String, Object> params = normalizeOpenDataQuery(query);
        params.put("productId", productId);
        if (hasText(params.get("nodeId")))
        {
            return selectNodeQualityCharacteristics(text(params.get("nodeId")));
        }
        List<Map<String, Object>> rows = normalizeRows(openApiMapper.selectProductQualityFeatures(params));
        for (Map<String, Object> row : rows)
        {
            parseJsonField(row, "attrsJson", "attrs");
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> selectDataStandards()
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(standard("instanceId", "Dossier Instance ID", "String", "Unique ID of a dossier instance."));
        rows.add(standard("versionId", "Dossier Version ID", "String", "Unique ID of a dossier version."));
        rows.add(standard("aircraftId", "Aircraft ID", "String", "Unique ID of an aircraft."));
        rows.add(standard("bomNodeId", "BOM Node ID", "String", "Unique ID of a BOM node."));
        rows.add(standard("partNumber", "Part Number", "String", "Business part number for search and matching."));
        rows.add(standard("lifecycleStage", "Lifecycle Stage", "Enum", "DESIGN, MANUFACTURING, SERVICE, DOSSIER."));
        rows.add(standard("confidence", "Confidence", "Decimal", "A value between 0 and 1."));
        rows.add(standard("sourceComponent", "Source Component", "String", "External component name or code."));
        return rows;
    }

    @Override
    public List<Map<String, Object>> selectNodeLifecycleData(String nodeId, String dataType)
    {
        List<Map<String, Object>> rows = normalizeRows(openApiMapper.selectNodeLifecycleData(nodeId, dataType));
        for (Map<String, Object> row : rows)
        {
            parseJsonField(row, "attrsJson", "attrs");
            parseJsonField(row, "sourceTraceJson", "sourceTrace");
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> selectManufacturingProcessData(Map<String, Object> query)
    {
        Map<String, Object> params = normalizeOpenDataQuery(query);
        params.put("dataGroup", "manufacturing");
        if (hasText(params.get("nodeId")))
        {
            return selectNodeLifecycleData(text(params.get("nodeId")), "manufacturing");
        }
        return normalizeLifecycleRows(openApiMapper.selectLifecycleDataByProduct(params));
    }

    @Override
    public byte[] exportProject3HierarchyWorkbook(Map<String, Object> query)
    {
        query = safeMap(query);
        Map<String, Object> params = normalizeProject3HierarchyQuery(query);
        List<Map<String, Object>> path = normalizeRows(openApiMapper.selectProject3HierarchyBomPath(params));
        if (path.isEmpty())
        {
            throw new ServiceException("未找到可导出的层级对象BOM链路。");
        }

        Map<String, Object> aircraftNode = path.get(0);
        Map<String, Object> subsystemNode = findProject3HierarchyNode(path, "SUBSYSTEM");
        Map<String, Object> systemNode = findProject3HierarchyNode(path, "SYSTEM");
        Map<String, Object> equipmentNode = findProject3HierarchyNode(path, "EQUIPMENT");
        Map<String, Object> componentNode = findProject3HierarchyNode(path, "COMPONENT");
        Map<String, Object> partNode = findProject3HierarchyNode(path, "PART");
        if (partNode.isEmpty())
        {
            partNode = path.get(path.size() - 1);
        }
        validateProject3HierarchyPath(subsystemNode, equipmentNode, componentNode, partNode);

        String aircraftId = text(partNode.get("aircraftId"));
        if (!hasText(aircraftId))
        {
            aircraftId = text(aircraftNode.get("aircraftId"));
        }
        String partNumber = defaultText(partNode.get("partNumber"), params.get("partNumber"));
        String bomNodeId = text(partNode.get("nodeId"));

        Map<String, Object> dataParams = map();
        dataParams.put("aircraftId", aircraftId);
        dataParams.put("partNumber", partNumber);
        dataParams.put("bomNodeId", bomNodeId);
        dataParams.put("partInstanceId", partNode.get("partInstanceId"));

        Map<String, Object> aircraft = normalizeRow(openApiMapper.selectProject3HierarchyAircraft(dataParams));
        Map<String, Object> partTemplate = normalizeRow(openApiMapper.selectProject3HierarchyPartTemplate(dataParams));
        Map<String, Object> partInstance = normalizeRow(openApiMapper.selectProject3HierarchyPartInstance(dataParams));
        if (aircraft.isEmpty())
        {
            throw new ServiceException("未找到可导出的飞机数据：" + aircraftId);
        }
        if (partTemplate.isEmpty())
        {
            throw new ServiceException("未找到可导出的零件模板数据：" + partNumber);
        }
        if (partInstance.isEmpty())
        {
            throw new ServiceException("未找到可导出的零件实例数据：" + partNumber);
        }

        Map<String, Object> subsystem = map();
        subsystem.put("subsystem_id", subsystemNode.get("nodeId"));
        subsystem.put("subsystem_name", subsystemNode.get("partName"));
        subsystem.put("aircraft_id", aircraftId);
        subsystem.put("remarks", hasText(systemNode.get("partName")) ? "上级系统：" + text(systemNode.get("partName")) : "");

        Map<String, Object> equipment = map();
        equipment.put("equipment_id", equipmentNode.get("nodeId"));
        equipment.put("equipment_name", equipmentNode.get("partName"));
        equipment.put("subsystem_id", subsystemNode.get("nodeId"));
        equipment.put("remarks", equipmentNode.get("partNumber"));

        Map<String, Object> component = map();
        component.put("component_id", componentNode.get("nodeId"));
        component.put("component_name", componentNode.get("partName"));
        component.put("equipment_id", equipmentNode.get("nodeId"));
        component.put("specification", componentNode.get("partNumber"));
        component.put("remarks", componentNode.get("remark"));

        partTemplate.put("component_id", componentNode.get("nodeId"));
        partInstance.put("part_template_id", partNumber);

        validateProject3HierarchyRows(aircraft, subsystem, equipment, component, partTemplate, partInstance);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            new Project3HierarchyWorkbookBuilder().write(outputStream, Collections.singletonList(aircraft),
                    Collections.singletonList(subsystem), Collections.singletonList(equipment),
                    Collections.singletonList(component), Collections.singletonList(partTemplate),
                    Collections.singletonList(partInstance));
            return outputStream.toByteArray();
        }
        catch (Exception e)
        {
            throw new ServiceException("生成课题三层级对象Excel失败：" + e.getMessage());
        }
    }

    @Override
    public byte[] exportProject3PartProcessWorkbook(Map<String, Object> query)
    {
        query = safeMap(query);
        String partNumber = firstText(query.get("partNumber"), query.get("partNo"));
        partNumber = firstText(partNumber, query.get("part_template_id"));
        partNumber = defaultText(partNumber, DEFAULT_PROJECT3_PART_NUMBER);

        Map<String, Object> part = normalizeRow(openApiMapper.selectProject3PartProcessPart(partNumber));
        if (part.isEmpty())
        {
            throw new ServiceException("未找到可导出的零件标准制造过程零件数据：" + partNumber);
        }

        List<Map<String, Object>> routes = normalizeRows(openApiMapper.selectProject3PartProcessRoutes(partNumber));
        List<Map<String, Object>> steps = normalizeRows(openApiMapper.selectProject3PartProcessSteps(partNumber));
        validateProject3PartProcessRows(partNumber, routes, steps);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            new Project3PartProcessWorkbookBuilder().write(outputStream, Collections.singletonList(part), routes, steps);
            return outputStream.toByteArray();
        }
        catch (Exception e)
        {
            throw new ServiceException("生成课题三零件标准制造过程Excel失败：" + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> selectOperationMaintenanceData(Map<String, Object> query)
    {
        Map<String, Object> params = normalizeOpenDataQuery(query);
        params.put("dataGroup", "operation_maintenance");
        if (hasText(params.get("nodeId")))
        {
            return selectNodeLifecycleData(text(params.get("nodeId")), "service");
        }
        return normalizeLifecycleRows(openApiMapper.selectLifecycleDataByProduct(params));
    }

    @Override
    public List<Map<String, Object>> selectFilesAndModels(Map<String, Object> query)
    {
        Map<String, Object> params = normalizeOpenDataQuery(query);
        if (hasText(params.get("nodeId")))
        {
            return selectNodeFiles(text(params.get("nodeId")));
        }
        List<Map<String, Object>> rows = normalizeRows(openApiMapper.selectFilesAndModels(params));
        for (Map<String, Object> row : rows)
        {
            parseJsonField(row, "attrsJson", "attrs");
            parseJsonField(row, "sourceTraceJson", "sourceTrace");
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> selectNodeFiles(String nodeId)
    {
        List<Map<String, Object>> rows = normalizeRows(openApiMapper.selectNodeFiles(nodeId));
        for (Map<String, Object> row : rows)
        {
            parseJsonField(row, "attrsJson", "attrs");
            parseJsonField(row, "sourceTraceJson", "sourceTrace");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> writebackResult(String resultType, Map<String, Object> request)
    {
        request = safeMap(request);
        String instanceId = text(request.get("instanceId"));
        String versionId = text(request.get("versionId"));
        String bomNodeId = firstText(request.get("bomNodeId"), request.get("nodeId"));
        if (!hasText(instanceId) && hasText(bomNodeId))
        {
            Map<String, Object> current = openApiMapper.selectCurrentDossierByBomNode(bomNodeId);
            instanceId = text(current == null ? null : current.get("instanceId"));
            versionId = hasText(versionId) ? versionId : text(current == null ? null : current.get("versionId"));
        }
        if (!hasText(instanceId))
        {
            throw new ServiceException("Writeback requires instanceId or a BOM node that belongs to a dossier.");
        }

        String taskId = IdUtils.randomUUID();
        String resultId = IdUtils.randomUUID();
        String operationLogId = IdUtils.randomUUID();
        String taskCode = "WB-" + resultType.toUpperCase() + "-" + System.currentTimeMillis();
        String sourceComponent = defaultText(request.get("sourceComponent"), "external-component");
        String relatedObjectType = defaultText(request.get("relatedObjectType"), hasText(bomNodeId) ? "bom_node" : "dossier");
        String relatedObjectId = defaultText(request.get("relatedObjectId"), hasText(bomNodeId) ? bomNodeId : instanceId);

        Map<String, Object> scope = map();
        scope.put("instanceId", instanceId);
        scope.put("versionId", versionId);
        scope.put("bomNodeId", bomNodeId);
        scope.put("sourceComponent", sourceComponent);

        Map<String, Object> task = map();
        task.put("taskId", taskId);
        task.put("taskCode", taskCode);
        task.put("taskName", defaultText(request.get("taskName"), writebackName(resultType)));
        task.put("scopeType", relatedObjectType);
        task.put("scopeJson", toJson(scope));
        task.put("algorithmCode", text(request.get("algorithmCode")));
        task.put("algorithmVersion", text(request.get("algorithmVersion")));
        task.put("paramsJson", toJson(request.get("params") == null ? request : request.get("params")));
        task.put("createdBy", currentUser());
        openApiMapper.insertAnalysisTask(task);

        Map<String, Object> resultValue = castMap(request.get("resultData"));
        if (resultValue.isEmpty())
        {
            resultValue.putAll(request);
        }
        Map<String, Object> result = map();
        result.put("resultId", resultId);
        result.put("taskId", taskId);
        result.put("resultType", resultType);
        result.put("resultTitle", defaultText(request.get("resultTitle"), writebackName(resultType)));
        result.put("resultSummary", defaultText(request.get("resultSummary"), defaultText(request.get("summary"), "External component writeback result.")));
        result.put("resultValueJson", toJson(resultValue));
        result.put("confidence", request.get("confidence"));
        result.put("rankNo", request.get("rankNo") == null ? 1 : request.get("rankNo"));
        result.put("relatedObjectType", relatedObjectType);
        result.put("relatedObjectId", relatedObjectId);
        result.put("evidenceJson", toJson(request.get("evidence") == null ? new ArrayList<>() : request.get("evidence")));
        openApiMapper.insertAnalysisResult(result);

        List<Map<String, Object>> writebackDocuments = saveWritebackDocuments(resultType, request, resultValue,
                instanceId, versionId, bomNodeId, resultId, sourceComponent);

        Map<String, Object> detail = map();
        detail.put("sourceComponent", sourceComponent);
        detail.put("resultType", resultType);
        detail.put("taskId", taskId);
        detail.put("resultId", resultId);
        detail.put("request", request);
        detail.put("writebackDocuments", writebackDocuments);

        Map<String, Object> log = map();
        log.put("operationLogId", operationLogId);
        log.put("instanceId", instanceId);
        log.put("versionId", blankToNull(versionId));
        log.put("operationName", writebackName(resultType));
        log.put("businessSubjectType", relatedObjectType);
        log.put("businessSubjectId", relatedObjectId);
        log.put("operatorId", currentUser());
        log.put("operatorName", currentUser());
        log.put("detailJson", toJson(detail));
        log.put("resultMessage", "Accepted writeback result from " + sourceComponent);
        openApiMapper.insertOperationLog(log);

        Map<String, Object> response = map();
        response.put("taskId", taskId);
        response.put("resultId", resultId);
        response.put("operationLogId", operationLogId);
        response.put("instanceId", instanceId);
        response.put("versionId", versionId);
        response.put("resultType", resultType);
        response.put("writebackDocumentCount", writebackDocuments.size());
        response.put("writebackDocuments", writebackDocuments);
        return response;
    }

    private List<Map<String, Object>> saveWritebackDocuments(String resultType, Map<String, Object> request,
            Map<String, Object> resultValue, String instanceId, String versionId, String bomNodeId, String resultId,
            String sourceComponent)
    {
        List<Map<String, Object>> documents = extractWritebackDocuments(request, resultValue);
        List<Map<String, Object>> saved = new ArrayList<>();
        if (documents.isEmpty())
        {
            return saved;
        }
        if (!hasText(bomNodeId))
        {
            throw new ServiceException("Document writeback requires bomNodeId so the file can be attached to a dossier node.");
        }

        Map<String, Object> node = selectBomNode(bomNodeId);
        String aircraftId = text(node.get("aircraftId"));
        String partNumber = text(node.get("partNumber"));
        String partInstanceId = text(node.get("partInstanceId"));
        String partName = text(node.get("partName"));
        String userName = currentUser();

        int index = 1;
        for (Map<String, Object> document : documents)
        {
            String location = firstText(document.get("fileUrl"), document.get("accessUrl"));
            location = firstText(location, document.get("url"));
            location = firstText(location, document.get("storageKey"));
            location = firstText(location, document.get("fileStorageKey"));
            location = firstText(location, document.get("storagePath"));
            if (!hasText(location))
            {
                throw new ServiceException("Document writeback requires fileUrl, accessUrl, storageKey or storagePath.");
            }

            String fileName = firstText(document.get("fileName"), document.get("filename"));
            fileName = firstText(fileName, document.get("originalFileName"));
            fileName = defaultText(fileName, fileNameFromLocation(location));
            String docNo = firstText(document.get("docNo"), document.get("documentNo"));
            docNo = firstText(docNo, document.get("businessNo"));
            docNo = firstText(docNo, document.get("code"));
            docNo = defaultText(docNo, "WB-" + sourceComponent + "-" + resultId + "-" + index);
            String title = firstText(document.get("docName"), document.get("documentName"));
            title = firstText(title, document.get("title"));
            title = firstText(title, document.get("name"));
            title = defaultText(title, defaultText(fileName, docNo));
            String docType = firstText(document.get("docType"), document.get("documentType"));
            docType = firstText(docType, document.get("businessType"));
            docType = defaultText(docType, defaultDocumentType(resultType));
            String fileExt = firstText(document.get("fileExt"), document.get("extension"));
            fileExt = defaultText(fileExt, extensionOf(defaultText(fileName, location)));
            String mimeType = defaultText(document.get("mimeType"), mimeType(fileExt));
            boolean httpLocation = isHttpUrl(location);
            String accessUrl = httpLocation ? location : firstText(document.get("accessUrl"), null);
            String storageKey = httpLocation ? text(document.get("storageKey"))
                    : firstText(document.get("storageKey"), document.get("fileStorageKey"));
            String storagePath = httpLocation ? text(document.get("storagePath")) : location;
            String storageType = defaultText(document.get("storageType"), httpLocation ? "EXTERNAL" : "LOCAL");
            String previewStorageKey = firstText(document.get("previewStorageKey"), document.get("previewUrl"));

            String fileCode = writebackFileCode(sourceComponent, docNo, location);
            String fileAssetId = openApiMapper.selectFileAssetIdByCode(fileCode);
            if (!hasText(fileAssetId))
            {
                fileAssetId = IdUtils.randomUUID();
            }

            Map<String, Object> metadata = map();
            metadata.put("resultType", resultType);
            metadata.put("resultId", resultId);
            metadata.put("sourceComponent", sourceComponent);
            metadata.put("instanceId", instanceId);
            metadata.put("versionId", versionId);
            metadata.put("bomNodeId", bomNodeId);
            metadata.put("partNumber", partNumber);
            metadata.put("document", document);

            Map<String, Object> asset = map();
            asset.put("fileAssetId", fileAssetId);
            asset.put("fileCode", fileCode);
            asset.put("assetKind", defaultText(document.get("assetKind"), "DOCUMENT"));
            asset.put("docNo", docNo);
            asset.put("docType", docType);
            asset.put("title", title);
            asset.put("revision", blankToNull(firstText(document.get("revision"), document.get("version"))));
            asset.put("fileName", defaultText(fileName, title));
            asset.put("displayName", defaultText(document.get("displayName"), title));
            asset.put("fileExt", blankToNull(fileExt));
            asset.put("mimeType", blankToNull(mimeType));
            asset.put("storageType", storageType);
            asset.put("storageKey", blankToNull(storageKey));
            asset.put("storagePath", blankToNull(storagePath));
            asset.put("accessUrl", blankToNull(accessUrl));
            asset.put("previewStorageKey", blankToNull(previewStorageKey));
            asset.put("fileSize", document.get("fileSize"));
            asset.put("issuedBy", defaultText(document.get("issuedBy"), sourceComponent));
            asset.put("sourceComponent", sourceComponent);
            asset.put("resultId", resultId);
            asset.put("metadataJson", toJson(metadata));
            asset.put("createdBy", userName);
            openApiMapper.upsertWritebackFileAsset(asset);
            fileAssetId = defaultText(openApiMapper.selectFileAssetIdByCode(fileCode), fileAssetId);

            String targetType = "BOM_NODE";
            String relationType = "DOSSIER_ATTACHMENT";
            String relationId = openApiMapper.selectWritebackFileRelationId(fileAssetId, targetType, bomNodeId,
                    relationType);
            if (!hasText(relationId))
            {
                relationId = IdUtils.randomUUID();
            }

            Map<String, Object> trace = map();
            trace.put("sourceSystem", sourceComponent);
            trace.put("sourceTable", "openapi_writeback");
            trace.put("sourceRecordId", resultId);
            trace.put("sourceRecordKey", docNo);
            trace.put("bomNodeId", bomNodeId);
            trace.put("partNumber", partNumber);

            Map<String, Object> relationMeta = map();
            relationMeta.putAll(metadata);
            relationMeta.put("docNo", docNo);
            relationMeta.put("docType", docType);
            relationMeta.put("title", title);
            relationMeta.put("fileName", fileName);
            relationMeta.put("fileUrl", location);

            Map<String, Object> relation = map();
            relation.put("relationId", relationId);
            relation.put("fileAssetId", fileAssetId);
            relation.put("relationType", relationType);
            relation.put("targetType", targetType);
            relation.put("targetId", bomNodeId);
            relation.put("docNo", docNo);
            relation.put("title", title);
            relation.put("instanceId", instanceId);
            relation.put("aircraftId", aircraftId);
            relation.put("bomNodeId", bomNodeId);
            relation.put("partNumber", partNumber);
            relation.put("partInstanceId", blankToNull(partInstanceId));
            relation.put("objectLevel", defaultText(node.get("objectLevel"), "part"));
            relation.put("lifecycleStage", "DOCUMENT");
            relation.put("businessDomain", documentBusinessDomain(resultType));
            relation.put("sortOrder", 9000 + index);
            relation.put("sourceComponent", sourceComponent);
            relation.put("resultId", resultId);
            relation.put("sourceTraceJson", toJson(trace));
            relation.put("businessMetaJson", toJson(relationMeta));
            relation.put("createdBy", userName);
            openApiMapper.upsertWritebackFileRelation(relation);

            Map<String, Object> savedItem = map();
            savedItem.put("documentEntryId", relationId);
            savedItem.put("fileAssetId", fileAssetId);
            savedItem.put("docNo", docNo);
            savedItem.put("title", title);
            savedItem.put("fileName", fileName);
            savedItem.put("fileUrl", location);
            savedItem.put("bomNodeId", bomNodeId);
            savedItem.put("partNumber", partNumber);
            savedItem.put("partName", partName);
            saved.add(savedItem);
            index++;
        }
        return saved;
    }

    private List<Map<String, Object>> extractWritebackDocuments(Map<String, Object> request,
            Map<String, Object> resultValue)
    {
        List<Map<String, Object>> documents = new ArrayList<>();
        addDocumentCandidates(documents, resultValue.get("documents"));
        addDocumentCandidates(documents, resultValue.get("document"));
        addDocumentCandidates(documents, request.get("documents"));
        addDocumentCandidates(documents, request.get("document"));
        if (documents.isEmpty() && hasDocumentLocation(resultValue))
        {
            documents.add(resultValue);
        }
        if (documents.isEmpty() && hasDocumentLocation(request))
        {
            documents.add(request);
        }
        return documents;
    }

    private void addDocumentCandidates(List<Map<String, Object>> documents, Object value)
    {
        if (value instanceof List)
        {
            for (Object item : (List<?>) value)
            {
                Map<String, Object> document = castMap(item);
                if (!document.isEmpty())
                {
                    documents.add(document);
                }
            }
            return;
        }
        Map<String, Object> document = castMap(value);
        if (!document.isEmpty())
        {
            documents.add(document);
        }
    }

    private boolean hasDocumentLocation(Map<String, Object> value)
    {
        return hasText(value.get("fileUrl")) || hasText(value.get("accessUrl")) || hasText(value.get("url"))
                || hasText(value.get("storageKey")) || hasText(value.get("fileStorageKey"))
                || hasText(value.get("storagePath"));
    }

    private String writebackFileCode(String sourceComponent, String docNo, String location)
    {
        String businessKey = hasText(docNo) ? docNo : defaultText(location, "");
        String key = defaultText(sourceComponent, "external") + "|" + businessKey;
        UUID uuid = UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
        String prefix = defaultText(sourceComponent, "external").toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "-");
        if (prefix.length() > 24)
        {
            prefix = prefix.substring(0, 24);
        }
        return "WB-" + prefix + "-" + uuid;
    }

    private String fileNameFromLocation(String location)
    {
        String text = text(location);
        int queryIndex = text.indexOf('?');
        if (queryIndex >= 0)
        {
            text = text.substring(0, queryIndex);
        }
        int slashIndex = Math.max(text.lastIndexOf('/'), text.lastIndexOf('\\'));
        if (slashIndex >= 0 && slashIndex + 1 < text.length())
        {
            return text.substring(slashIndex + 1);
        }
        return text;
    }

    private String extensionOf(String fileName)
    {
        String text = text(fileName);
        int dotIndex = text.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex + 1 < text.length())
        {
            return text.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        }
        return "";
    }

    private String mimeType(String fileExt)
    {
        String ext = text(fileExt).toLowerCase(Locale.ROOT);
        if ("pdf".equals(ext))
        {
            return "application/pdf";
        }
        if ("docx".equals(ext))
        {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        if ("doc".equals(ext))
        {
            return "application/msword";
        }
        if ("png".equals(ext))
        {
            return "image/png";
        }
        if ("jpg".equals(ext) || "jpeg".equals(ext))
        {
            return "image/jpeg";
        }
        if ("xlsx".equals(ext))
        {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        return hasText(ext) ? "application/octet-stream" : "";
    }

    private String defaultDocumentType(String resultType)
    {
        if ("design_optimization".equals(resultType) || "optimization".equals(resultType))
        {
            return "DESIGN_OPTIMIZATION_DOCUMENT";
        }
        if ("quality_monitoring".equals(resultType) || "quality_supervision".equals(resultType))
        {
            return "QUALITY_DOCUMENT";
        }
        if ("fault_diagnosis".equals(resultType))
        {
            return "FAULT_DIAGNOSIS_DOCUMENT";
        }
        if ("traceability".equals(resultType))
        {
            return "TRACEABILITY_DOCUMENT";
        }
        return "WRITEBACK_DOCUMENT";
    }

    private String documentBusinessDomain(String resultType)
    {
        if ("design_optimization".equals(resultType) || "optimization".equals(resultType))
        {
            return "DESIGN";
        }
        if ("quality_monitoring".equals(resultType) || "quality_supervision".equals(resultType))
        {
            return "QUALITY";
        }
        if ("fault_diagnosis".equals(resultType))
        {
            return "SERVICE";
        }
        if ("traceability".equals(resultType))
        {
            return "DOSSIER";
        }
        return "DATA_SUPPORT";
    }

    private boolean isHttpUrl(String value)
    {
        String text = text(value).toLowerCase(Locale.ROOT);
        return text.startsWith("http://") || text.startsWith("https://");
    }

    @Override
    public Map<String, Object> selectChangePage(Map<String, Object> query)
    {
        query = safeMap(query);
        Map<String, Object> params = normalizePageQuery(query);
        params.put("subjectType", blankToNull(query.get("subjectType")));
        params.put("instanceId", blankToNull(query.get("instanceId")));
        params.put("beginTime", normalizeBeginTime(query.get("beginTime")));
        params.put("endTime", normalizeEndTime(query.get("endTime")));
        int total = openApiMapper.selectChangeCount(params);
        List<Map<String, Object>> rows = normalizeRows(openApiMapper.selectChangeList(params));
        for (Map<String, Object> row : rows)
        {
            parseJsonField(row, "detailJson", "detail");
        }

        Map<String, Object> result = map();
        result.put("rows", rows);
        result.put("total", total);
        result.put("pageNum", params.get("pageNum"));
        result.put("pageSize", params.get("pageSize"));
        return result;
    }

    @Override
    public Map<String, Object> selectChangeDetail(String changeId)
    {
        Map<String, Object> row = normalizeRow(openApiMapper.selectChangeDetail(changeId));
        parseJsonField(row, "detailJson", "detail");
        return row;
    }

    @Override
    public Map<String, Object> selectAccessInfo()
    {
        Map<String, Object> result = map();
        result.put("username", currentUser());
        result.put("module", "dossier-openapi");
        result.put("authenticated", true);
        result.put("canRead", true);
        result.put("canWrite", true);
        result.put("checkedAt", displayTime(new Date()));
        return result;
    }

    @Override
    public Map<String, Object> checkAccess(Map<String, Object> query)
    {
        query = safeMap(query);
        Map<String, Object> result = selectAccessInfo();
        result.put("action", defaultText(query.get("action"), "read"));
        result.put("resourceType", defaultText(query.get("resourceType"), "dossier"));
        result.put("resourceId", text(query.get("resourceId")));
        result.put("allowed", true);
        result.put("reason", "Login user can access dossier openapi.");
        return result;
    }

    @Override
    public Map<String, Object> selectOperationLogPage(Map<String, Object> query)
    {
        query = safeMap(query);
        Map<String, Object> params = normalizePageQuery(query);
        params.put("operationType", blankToNull(query.get("operationType")));
        params.put("instanceId", blankToNull(query.get("instanceId")));
        params.put("operatorName", blankToNull(query.get("operatorName")));
        params.put("beginTime", normalizeBeginTime(query.get("beginTime")));
        params.put("endTime", normalizeEndTime(query.get("endTime")));
        int total = openApiMapper.selectOperationLogCount(params);
        List<Map<String, Object>> rows = normalizeRows(openApiMapper.selectOperationLogList(params));
        for (Map<String, Object> row : rows)
        {
            parseJsonField(row, "detailJson", "detail");
        }

        Map<String, Object> result = map();
        result.put("rows", rows);
        result.put("total", total);
        result.put("pageNum", params.get("pageNum"));
        result.put("pageSize", params.get("pageSize"));
        return result;
    }

    @Override
    public Map<String, Object> selectOperationLogDetail(String logId)
    {
        Map<String, Object> row = normalizeRow(openApiMapper.selectOperationLogDetail(logId));
        parseJsonField(row, "detailJson", "detail");
        return row;
    }

    @Override
    public Map<String, Object> createSubscription(Map<String, Object> request)
    {
        request = safeMap(request);
        String subscriptionId = IdUtils.randomUUID();
        String subscriptionCode = "SUB-" + System.currentTimeMillis();
        Map<String, Object> params = map();
        params.put("subscriptionId", subscriptionId);
        params.put("subscriptionCode", subscriptionCode);
        params.put("subscriptionName", defaultText(request.get("subscriptionName"), defaultText(request.get("name"), "Dossier Change Subscription")));
        params.put("targetDomain", defaultText(request.get("targetDomain"), "DOSSIER"));
        params.put("conditionJson", toJson(request.get("condition") == null ? request : request.get("condition")));
        params.put("receiverType", defaultText(request.get("receiverType"), "external"));
        params.put("receiverRef", defaultText(request.get("receiverRef"), defaultText(request.get("sourceComponent"), "external-component")));
        params.put("messageTemplate", defaultText(request.get("messageTemplate"), "Digital dossier data changed."));
        params.put("createdBy", currentUser());
        openApiMapper.insertSubscription(params);

        Map<String, Object> result = map();
        result.put("subscriptionId", subscriptionId);
        result.put("subscriptionCode", subscriptionCode);
        result.put("status", "active");
        return result;
    }

    @Override
    public int deleteSubscription(String subscriptionId)
    {
        return openApiMapper.disableSubscription(subscriptionId, currentUser());
    }

    @Override
    public List<Map<String, Object>> selectCatalog()
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(api("GET", "/dossier/openapi/dossiers", "List dossier instances"));
        rows.add(api("GET", "/dossier/openapi/product-dossiers", "List product dossiers"));
        rows.add(api("GET", "/dossier/openapi/dossiers/{instanceId}", "Get dossier detail"));
        rows.add(api("GET", "/dossier/openapi/product-dossiers/{instanceId}", "Get product dossier detail"));
        rows.add(api("GET", "/dossier/openapi/dossiers/{instanceId}/versions", "List dossier versions"));
        rows.add(api("GET", "/dossier/openapi/dossiers/{instanceId}/summary", "Get dossier summary"));
        rows.add(api("GET", "/dossier/openapi/products/{productId}/structure", "List product structure"));
        rows.add(api("GET", "/dossier/openapi/products/{productId}/bom", "List product BOM"));
        rows.add(api("GET", "/dossier/openapi/aircraft/{aircraftId}/bom/children", "List BOM children"));
        rows.add(api("GET", "/dossier/openapi/aircraft/{aircraftId}/bom/search", "Search BOM nodes"));
        rows.add(api("GET", "/dossier/openapi/bom/nodes/{nodeId}", "Get BOM node detail"));
        rows.add(api("GET", "/dossier/openapi/bom/nodes/{nodeId}/path", "Get BOM node path"));
        rows.add(api("GET", "/dossier/openapi/bom/nodes/{nodeId}/relations", "Get BOM node relations"));
        rows.add(api("GET", "/dossier/openapi/dictionaries", "List dictionaries"));
        rows.add(api("GET", "/dossier/openapi/quality-characteristics", "List quality characteristics"));
        rows.add(api("GET", "/dossier/openapi/bom/nodes/{nodeId}/quality-characteristics", "List node quality characteristics"));
        rows.add(api("GET", "/dossier/openapi/products/{productId}/quality-features", "List product quality features"));
        rows.add(api("GET", "/dossier/openapi/data-standards", "List data standards"));
        rows.add(api("GET", "/dossier/openapi/bom/nodes/{nodeId}/design-data", "List node design data"));
        rows.add(api("GET", "/dossier/openapi/bom/nodes/{nodeId}/manufacturing-data", "List node manufacturing data"));
        rows.add(api("GET", "/dossier/openapi/manufacturing/process-data", "List manufacturing process data"));
        rows.add(api("GET", "/dossier/openapi/project3/hierarchy/export", "Export Project 3 hierarchy Excel"));
        rows.add(api("GET", "/dossier/openapi/project3/part-process/export", "Export Project 3 standard part process Excel"));
        rows.add(api("GET", "/dossier/openapi/products/{productId}/manufacturing-process-data", "List product manufacturing process data"));
        rows.add(api("GET", "/dossier/openapi/bom/nodes/{nodeId}/inspection-records", "List node inspection records"));
        rows.add(api("GET", "/dossier/openapi/bom/nodes/{nodeId}/installation-records", "List node installation records"));
        rows.add(api("GET", "/dossier/openapi/bom/nodes/{nodeId}/service-records", "List node service records"));
        rows.add(api("GET", "/dossier/openapi/operation-maintenance/data", "List operation and maintenance data"));
        rows.add(api("GET", "/dossier/openapi/products/{productId}/operation-maintenance-data", "List product operation and maintenance data"));
        rows.add(api("GET", "/dossier/openapi/bom/nodes/{nodeId}/fault-records", "List node fault records"));
        rows.add(api("GET", "/dossier/openapi/bom/nodes/{nodeId}/files", "List node files"));
        rows.add(api("GET", "/dossier/openapi/files-models", "List files and models"));
        rows.add(api("GET", "/dossier/openapi/products/{productId}/files-models", "List product files and models"));
        rows.add(api("POST", "/dossier/openapi/writeback/optimization-results", "Write back optimization results"));
        rows.add(api("POST", "/dossier/openapi/writeback/design-optimization", "Write back design optimization results"));
        rows.add(api("POST", "/dossier/openapi/writeback/quality-monitoring-results", "Write back quality monitoring results"));
        rows.add(api("POST", "/dossier/openapi/writeback/quality-supervision", "Write back quality supervision results"));
        rows.add(api("POST", "/dossier/openapi/writeback/fault-diagnosis-results", "Write back fault diagnosis results"));
        rows.add(api("POST", "/dossier/openapi/writeback/fault-diagnosis", "Write back fault diagnosis results"));
        rows.add(api("POST", "/dossier/openapi/writeback/traceability-results", "Write back traceability results"));
        rows.add(api("POST", "/dossier/openapi/writeback/traceability", "Write back traceability results"));
        rows.add(api("POST", "/dossier/openapi/writeback/data-completion-results", "Write back data completion results"));
        rows.add(api("GET", "/dossier/openapi/changes", "List data changes"));
        rows.add(api("GET", "/dossier/openapi/data-change/notifications", "List data change notifications"));
        rows.add(api("GET", "/dossier/openapi/changes/{changeId}", "Get data change detail"));
        rows.add(api("POST", "/dossier/openapi/subscriptions", "Create change subscription"));
        rows.add(api("POST", "/dossier/openapi/data-change/subscriptions", "Create data change subscription"));
        rows.add(api("DELETE", "/dossier/openapi/subscriptions/{subscriptionId}", "Disable change subscription"));
        rows.add(api("GET", "/dossier/openapi/access/current", "Get current access information"));
        rows.add(api("GET", "/dossier/openapi/access/check", "Check access"));
        rows.add(api("GET", "/dossier/openapi/operation-logs", "List operation logs"));
        rows.add(api("GET", "/dossier/openapi/operation-logs/{logId}", "Get operation log detail"));
        rows.add(api("GET", "/dossier/openapi/catalog", "List OpenAPI catalog"));
        rows.add(api("GET", "/dossier/openapi/health", "OpenAPI health check"));
        rows.add(api("GET", "/dossier/openapi/version", "Get OpenAPI version"));
        return rows;
    }

    @Override
    public Map<String, Object> selectHealth()
    {
        Map<String, Object> result = map();
        result.put("status", "UP");
        result.put("module", "ruoyi-dossier");
        result.put("apiGroup", "dossier-openapi");
        result.put("checkedAt", displayTime(new Date()));
        result.put("catalogCount", selectCatalog().size());
        return result;
    }

    @Override
    public Map<String, Object> selectVersion()
    {
        Map<String, Object> result = map();
        result.put("apiVersion", "v1");
        result.put("basePath", "/dossier/openapi");
        result.put("compatibleFramework", "RuoYi Cloud Vue3");
        result.put("updatedAt", "2026-06-07");
        return result;
    }

    private Map<String, Object> normalizeProject3HierarchyQuery(Map<String, Object> query)
    {
        Map<String, Object> params = map();
        String bomNodeId = firstText(query.get("bomNodeId"), query.get("nodeId"));
        String partNumber = firstText(query.get("partNumber"), query.get("partNo"));
        String aircraftId = firstText(query.get("aircraftId"), query.get("productId"));
        if (!hasText(bomNodeId) && !hasText(partNumber) && !hasText(aircraftId))
        {
            bomNodeId = DEFAULT_PROJECT3_BOM_NODE_ID;
            partNumber = DEFAULT_PROJECT3_PART_NUMBER;
        }
        if (!hasText(bomNodeId) && !hasText(partNumber))
        {
            partNumber = DEFAULT_PROJECT3_PART_NUMBER;
        }
        params.put("bomNodeId", blankToNull(bomNodeId));
        params.put("partNumber", blankToNull(partNumber));
        params.put("aircraftId", blankToNull(aircraftId));
        return params;
    }

    private Map<String, Object> findProject3HierarchyNode(List<Map<String, Object>> path, String nodeType)
    {
        for (Map<String, Object> node : path)
        {
            if (nodeType.equalsIgnoreCase(text(node.get("nodeType"))))
            {
                return node;
            }
        }
        return map();
    }

    private void validateProject3HierarchyPath(Map<String, Object> subsystemNode,
            Map<String, Object> equipmentNode, Map<String, Object> componentNode, Map<String, Object> partNode)
    {
        if (subsystemNode.isEmpty())
        {
            throw new ServiceException("层级对象导出缺少分系统节点。");
        }
        if (equipmentNode.isEmpty())
        {
            throw new ServiceException("层级对象导出缺少设备节点。");
        }
        if (componentNode.isEmpty())
        {
            throw new ServiceException("层级对象导出缺少组件节点。");
        }
        if (partNode.isEmpty())
        {
            throw new ServiceException("层级对象导出缺少零件节点。");
        }
    }

    private void validateProject3HierarchyRows(Map<String, Object> aircraft, Map<String, Object> subsystem,
            Map<String, Object> equipment, Map<String, Object> component, Map<String, Object> partTemplate,
            Map<String, Object> partInstance)
    {
        requireProject3Value(aircraft, "aircraft_id", "aircraft", 2, "飞机ID");
        requireProject3Value(aircraft, "aircraft_name", "aircraft", 2, "飞机名称");
        requireProject3Value(subsystem, "subsystem_id", "subsystems", 2, "分系统ID");
        requireProject3Value(subsystem, "subsystem_name", "subsystems", 2, "分系统名称");
        requireProject3Value(subsystem, "aircraft_id", "subsystems", 2, "飞机ID");
        requireProject3Value(equipment, "equipment_id", "equipments", 2, "设备ID");
        requireProject3Value(equipment, "equipment_name", "equipments", 2, "设备名称");
        requireProject3Value(equipment, "subsystem_id", "equipments", 2, "分系统ID");
        requireProject3Value(component, "component_id", "components", 2, "组件ID");
        requireProject3Value(component, "component_name", "components", 2, "组件名称");
        requireProject3Value(component, "equipment_id", "components", 2, "设备ID");
        requireProject3Value(partTemplate, "part_template_id", "part_templates", 2, "零件模板ID");
        requireProject3Value(partTemplate, "part_name", "part_templates", 2, "零件名称");
        requireProject3Value(partTemplate, "component_id", "part_templates", 2, "组件ID");
        requireProject3Value(partInstance, "part_instance_id", "part_instances", 2, "零件实例ID");
        requireProject3Value(partInstance, "part_template_id", "part_instances", 2, "零件模板ID");
        requireProject3Value(partInstance, "serial_number", "part_instances", 2, "序列号");
    }

    private void validateProject3PartProcessRows(String partNumber, List<Map<String, Object>> routes,
            List<Map<String, Object>> steps)
    {
        if (routes.isEmpty())
        {
            throw new ServiceException("未找到可导出的工序路线：" + partNumber);
        }
        if (steps.isEmpty())
        {
            throw new ServiceException("未找到可导出的详细工序：" + partNumber);
        }
        for (int i = 0; i < routes.size(); i++)
        {
            Map<String, Object> route = routes.get(i);
            requireProject3Value(route, "route_id", "工序路线", i + 2, "工序路线id");
            requireProject3Value(route, "part_template_id", "工序路线", i + 2, "零件id");
            requireProject3Value(route, "route_name", "工序路线", i + 2, "工序路线名称");
        }
        for (int i = 0; i < steps.size(); i++)
        {
            Map<String, Object> step = steps.get(i);
            requireProject3Value(step, "process_def_id", "详细工序", i + 2, "工序id");
            requireProject3Value(step, "route_id", "详细工序", i + 2, "工序路线id");
            requireProject3Value(step, "process_number", "详细工序", i + 2, "工序序号");
            requireProject3Value(step, "process_name", "详细工序", i + 2, "工序名称");
            requireProject3Value(step, "equipment_type", "详细工序", i + 2, "设备类型");
        }
    }

    private void requireProject3Value(Map<String, Object> row, String key, String sheetName, int rowNumber,
            String fieldName)
    {
        if (!hasText(row.get(key)))
        {
            throw new ServiceException("课题三Excel数据缺少必填字段，Sheet：" + sheetName
                    + "，第" + rowNumber + "行，字段：" + fieldName);
        }
    }

    private Map<String, Object> normalizeOpenDataQuery(Map<String, Object> query)
    {
        query = safeMap(query);
        Map<String, Object> params = normalizePageQuery(query);
        params.put("productId", firstText(query.get("productId"), query.get("aircraftId")));
        params.put("productType", defaultText(query.get("productType"), "aircraft"));
        params.put("nodeId", blankToNull(firstText(query.get("nodeId"), query.get("bomNodeId"))));
        params.put("itemType", blankToNull(query.get("itemType")));
        params.put("fileType", blankToNull(query.get("fileType")));
        params.put("lifecycleStage", blankToNull(query.get("lifecycleStage")));
        return params;
    }

    private List<Map<String, Object>> normalizeLifecycleRows(List<Map<String, Object>> rows)
    {
        List<Map<String, Object>> result = normalizeRows(rows);
        for (Map<String, Object> row : result)
        {
            parseJsonField(row, "attrsJson", "attrs");
            parseJsonField(row, "sourceTraceJson", "sourceTrace");
        }
        return result;
    }

    private Map<String, Object> normalizePageQuery(Map<String, Object> query)
    {
        Map<String, Object> params = map();
        int pageNum = toInt(query.get("pageNum"), 1);
        int pageSize = toInt(query.get("pageSize"), 10);
        pageNum = Math.max(pageNum, 1);
        pageSize = Math.min(Math.max(pageSize, 1), 100);
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        params.put("offset", (pageNum - 1) * pageSize);
        params.put("modelId", blankToNull(query.get("modelId")));
        params.put("aircraftId", blankToNull(query.get("aircraftId")));
        params.put("tailNumber", blankToNull(query.get("tailNumber")));
        params.put("status", blankToNull(query.get("status")));
        params.put("keyword", blankToNull(query.get("keyword")));
        params.put("sortOrder", "asc".equalsIgnoreCase(text(query.get("sortOrder"))) ? "asc" : "desc");
        return params;
    }

    private List<Map<String, Object>> normalizeRows(List<Map<String, Object>> rows)
    {
        List<Map<String, Object>> result = new ArrayList<>();
        if (rows == null)
        {
            return result;
        }
        for (Map<String, Object> row : rows)
        {
            result.add(normalizeRow(row));
        }
        return result;
    }

    private Map<String, Object> normalizeRow(Map<String, Object> row)
    {
        Map<String, Object> result = map();
        if (row == null)
        {
            return result;
        }
        result.putAll(row);
        for (String key : new String[] { "generateTime", "createdAt", "updatedAt", "publishedAt", "archivedAt",
                "latestVersionTime", "changedAt" })
        {
            if (result.containsKey(key))
            {
                result.put(key, displayTime(result.get(key)));
            }
        }
        return result;
    }

    private List<Map<String, Object>> normalizeBomNodes(List<Map<String, Object>> rows)
    {
        List<Map<String, Object>> result = new ArrayList<>();
        if (rows == null)
        {
            return result;
        }
        for (Map<String, Object> row : rows)
        {
            result.add(normalizeBomNode(row));
        }
        return result;
    }

    private Map<String, Object> normalizeBomNode(Map<String, Object> row)
    {
        Map<String, Object> item = normalizeRow(row);
        if (item.isEmpty())
        {
            return item;
        }
        String objectLevel = objectLevel(item);
        item.put("objectLevel", objectLevel);
        item.put("levelName", levelName(objectLevel));
        item.put("label", text(item.get("partName")) + " " + text(item.get("partNumber")));
        item.put("leaf", toInt(item.get("childCount"), 0) == 0);
        item.put("status", "active");
        return item;
    }

    private String objectLevel(Map<String, Object> row)
    {
        String nodeType = text(row.get("nodeType")).toUpperCase();
        int level = toInt(row.get("nodeLevel"), 0);
        if ("AIRCRAFT".equals(nodeType) || "AIRCRAFT".equals(text(row.get("objectLevel")).toUpperCase()) || level == 1)
        {
            return "aircraft";
        }
        if ("SYSTEM".equals(nodeType) || level == 2)
        {
            return "system";
        }
        if ("SUBSYSTEM".equals(nodeType) || "SUB_SYS".equals(nodeType) || level == 3)
        {
            return "subsystem";
        }
        if ("EQUIPMENT".equals(nodeType) || level == 4)
        {
            return "equipment";
        }
        if ("COMPONENT".equals(nodeType) || level == 5)
        {
            return "component";
        }
        if ("PART".equals(nodeType) || "CONSUMABLE".equals(nodeType) || level >= 6)
        {
            return "part";
        }
        return "part";
    }

    private String levelName(String objectLevel)
    {
        if ("aircraft".equals(objectLevel))
        {
            return "Aircraft";
        }
        if ("system".equals(objectLevel))
        {
            return "System";
        }
        if ("subsystem".equals(objectLevel))
        {
            return "Subsystem";
        }
        if ("equipment".equals(objectLevel))
        {
            return "Equipment";
        }
        if ("component".equals(objectLevel))
        {
            return "Component";
        }
        return "Part";
    }

    private Map<String, Object> dictionary(String code, String name, String... values)
    {
        Map<String, Object> row = map();
        row.put("dictCode", code);
        row.put("dictName", name);
        List<Map<String, Object>> items = new ArrayList<>();
        for (String value : values)
        {
            String[] parts = value.split(":", 2);
            Map<String, Object> item = map();
            item.put("value", parts[0]);
            item.put("label", parts.length > 1 ? parts[1] : parts[0]);
            items.add(item);
        }
        row.put("items", items);
        return row;
    }

    private Map<String, Object> quality(String code, String name, String desc, String stage)
    {
        Map<String, Object> row = map();
        row.put("characteristicCode", code);
        row.put("characteristicName", name);
        row.put("description", desc);
        row.put("stage", stage);
        return row;
    }

    private Map<String, Object> standard(String field, String name, String type, String desc)
    {
        Map<String, Object> row = map();
        row.put("fieldName", field);
        row.put("displayName", name);
        row.put("dataType", type);
        row.put("description", desc);
        return row;
    }

    private Map<String, Object> api(String method, String path, String desc)
    {
        Map<String, Object> row = map();
        row.put("method", method);
        row.put("path", path);
        row.put("description", desc);
        return row;
    }

    private void parseJsonField(Map<String, Object> row, String sourceKey, String targetKey)
    {
        row.put(targetKey, parseJsonObject(row.get(sourceKey)));
    }

    private Map<String, Object> parseJsonObject(Object value)
    {
        if (value == null || !hasText(String.valueOf(value)))
        {
            return map();
        }
        if (value instanceof Map)
        {
            return castMap(value);
        }
        try
        {
            return OBJECT_MAPPER.readValue(String.valueOf(value), new TypeReference<Map<String, Object>>() {});
        }
        catch (Exception e)
        {
            return map();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value)
    {
        if (value instanceof Map)
        {
            return (Map<String, Object>) value;
        }
        return map();
    }

    private String toJson(Object value)
    {
        try
        {
            return OBJECT_MAPPER.writeValueAsString(value == null ? map() : value);
        }
        catch (Exception e)
        {
            return "{}";
        }
    }

    private String writebackName(String resultType)
    {
        if ("optimization".equals(resultType))
        {
            return "Optimization Result Writeback";
        }
        if ("design_optimization".equals(resultType))
        {
            return "Design Optimization Result Writeback";
        }
        if ("quality_monitoring".equals(resultType))
        {
            return "Quality Monitoring Result Writeback";
        }
        if ("quality_supervision".equals(resultType))
        {
            return "Quality Supervision Result Writeback";
        }
        if ("fault_diagnosis".equals(resultType))
        {
            return "Fault Diagnosis Result Writeback";
        }
        if ("traceability".equals(resultType))
        {
            return "Traceability Result Writeback";
        }
        if ("data_completion".equals(resultType))
        {
            return "Data Completion Result Writeback";
        }
        return "External Result Writeback";
    }

    private String normalizeBeginTime(Object value)
    {
        String text = text(value);
        if (!hasText(text))
        {
            return null;
        }
        return text.length() == 10 ? text + " 00:00:00" : text;
    }

    private String normalizeEndTime(Object value)
    {
        String text = text(value);
        if (!hasText(text))
        {
            return null;
        }
        return text.length() == 10 ? text + " 23:59:59" : text;
    }

    private String currentUser()
    {
        try
        {
            return SecurityUtils.getUsername();
        }
        catch (Exception e)
        {
            return "system";
        }
    }

    private String displayTime(Object value)
    {
        if (value == null)
        {
            return "";
        }
        if (value instanceof Date)
        {
            return new SimpleDateFormat(DISPLAY_TIME_PATTERN).format((Date) value);
        }
        return String.valueOf(value).replace("T", " ");
    }

    private String defaultText(Object value, Object fallback)
    {
        return hasText(value) ? text(value) : text(fallback);
    }

    private String firstText(Object first, Object second)
    {
        return hasText(first) ? text(first) : text(second);
    }

    private String blankToNull(Object value)
    {
        String text = text(value);
        return hasText(text) ? text : null;
    }

    private boolean hasText(Object value)
    {
        return value != null && String.valueOf(value).trim().length() > 0;
    }

    private int toInt(Object value, int defaultValue)
    {
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        try
        {
            return Integer.parseInt(text(value));
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    private String text(Object value)
    {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private Map<String, Object> safeMap(Map<String, Object> value)
    {
        return value == null ? map() : value;
    }

    private Map<String, Object> map()
    {
        return new LinkedHashMap<>();
    }
}
