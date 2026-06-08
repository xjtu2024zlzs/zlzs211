package com.ruoyi.project1.dossier.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

        Map<String, Object> detail = map();
        detail.put("sourceComponent", sourceComponent);
        detail.put("resultType", resultType);
        detail.put("taskId", taskId);
        detail.put("resultId", resultId);
        detail.put("request", request);

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
        return response;
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
        rows.add(api("GET", "/project1/dossier/openapi/dossiers", "List dossier instances"));
        rows.add(api("GET", "/project1/dossier/openapi/product-dossiers", "List product dossiers"));
        rows.add(api("GET", "/project1/dossier/openapi/dossiers/{instanceId}", "Get dossier detail"));
        rows.add(api("GET", "/project1/dossier/openapi/product-dossiers/{instanceId}", "Get product dossier detail"));
        rows.add(api("GET", "/project1/dossier/openapi/dossiers/{instanceId}/versions", "List dossier versions"));
        rows.add(api("GET", "/project1/dossier/openapi/dossiers/{instanceId}/summary", "Get dossier summary"));
        rows.add(api("GET", "/project1/dossier/openapi/products/{productId}/structure", "List product structure"));
        rows.add(api("GET", "/project1/dossier/openapi/products/{productId}/bom", "List product BOM"));
        rows.add(api("GET", "/project1/dossier/openapi/aircraft/{aircraftId}/bom/children", "List BOM children"));
        rows.add(api("GET", "/project1/dossier/openapi/aircraft/{aircraftId}/bom/search", "Search BOM nodes"));
        rows.add(api("GET", "/project1/dossier/openapi/bom/nodes/{nodeId}", "Get BOM node detail"));
        rows.add(api("GET", "/project1/dossier/openapi/bom/nodes/{nodeId}/path", "Get BOM node path"));
        rows.add(api("GET", "/project1/dossier/openapi/bom/nodes/{nodeId}/relations", "Get BOM node relations"));
        rows.add(api("GET", "/project1/dossier/openapi/dictionaries", "List dictionaries"));
        rows.add(api("GET", "/project1/dossier/openapi/quality-characteristics", "List quality characteristics"));
        rows.add(api("GET", "/project1/dossier/openapi/bom/nodes/{nodeId}/quality-characteristics", "List node quality characteristics"));
        rows.add(api("GET", "/project1/dossier/openapi/products/{productId}/quality-features", "List product quality features"));
        rows.add(api("GET", "/project1/dossier/openapi/data-standards", "List data standards"));
        rows.add(api("GET", "/project1/dossier/openapi/bom/nodes/{nodeId}/design-data", "List node design data"));
        rows.add(api("GET", "/project1/dossier/openapi/bom/nodes/{nodeId}/manufacturing-data", "List node manufacturing data"));
        rows.add(api("GET", "/project1/dossier/openapi/manufacturing/process-data", "List manufacturing process data"));
        rows.add(api("GET", "/project1/dossier/openapi/products/{productId}/manufacturing-process-data", "List product manufacturing process data"));
        rows.add(api("GET", "/project1/dossier/openapi/bom/nodes/{nodeId}/inspection-records", "List node inspection records"));
        rows.add(api("GET", "/project1/dossier/openapi/bom/nodes/{nodeId}/installation-records", "List node installation records"));
        rows.add(api("GET", "/project1/dossier/openapi/bom/nodes/{nodeId}/service-records", "List node service records"));
        rows.add(api("GET", "/project1/dossier/openapi/operation-maintenance/data", "List operation and maintenance data"));
        rows.add(api("GET", "/project1/dossier/openapi/products/{productId}/operation-maintenance-data", "List product operation and maintenance data"));
        rows.add(api("GET", "/project1/dossier/openapi/bom/nodes/{nodeId}/fault-records", "List node fault records"));
        rows.add(api("GET", "/project1/dossier/openapi/bom/nodes/{nodeId}/files", "List node files"));
        rows.add(api("GET", "/project1/dossier/openapi/files-models", "List files and models"));
        rows.add(api("GET", "/project1/dossier/openapi/products/{productId}/files-models", "List product files and models"));
        rows.add(api("POST", "/project1/dossier/openapi/writeback/optimization-results", "Write back optimization results"));
        rows.add(api("POST", "/project1/dossier/openapi/writeback/design-optimization", "Write back design optimization results"));
        rows.add(api("POST", "/project1/dossier/openapi/writeback/quality-monitoring-results", "Write back quality monitoring results"));
        rows.add(api("POST", "/project1/dossier/openapi/writeback/quality-supervision", "Write back quality supervision results"));
        rows.add(api("POST", "/project1/dossier/openapi/writeback/fault-diagnosis-results", "Write back fault diagnosis results"));
        rows.add(api("POST", "/project1/dossier/openapi/writeback/fault-diagnosis", "Write back fault diagnosis results"));
        rows.add(api("POST", "/project1/dossier/openapi/writeback/traceability-results", "Write back traceability results"));
        rows.add(api("POST", "/project1/dossier/openapi/writeback/traceability", "Write back traceability results"));
        rows.add(api("POST", "/project1/dossier/openapi/writeback/data-completion-results", "Write back data completion results"));
        rows.add(api("GET", "/project1/dossier/openapi/changes", "List data changes"));
        rows.add(api("GET", "/project1/dossier/openapi/data-change/notifications", "List data change notifications"));
        rows.add(api("GET", "/project1/dossier/openapi/changes/{changeId}", "Get data change detail"));
        rows.add(api("POST", "/project1/dossier/openapi/subscriptions", "Create change subscription"));
        rows.add(api("POST", "/project1/dossier/openapi/data-change/subscriptions", "Create data change subscription"));
        rows.add(api("DELETE", "/project1/dossier/openapi/subscriptions/{subscriptionId}", "Disable change subscription"));
        rows.add(api("GET", "/project1/dossier/openapi/access/current", "Get current access information"));
        rows.add(api("GET", "/project1/dossier/openapi/access/check", "Check access"));
        rows.add(api("GET", "/project1/dossier/openapi/operation-logs", "List operation logs"));
        rows.add(api("GET", "/project1/dossier/openapi/operation-logs/{logId}", "Get operation log detail"));
        rows.add(api("GET", "/project1/dossier/openapi/catalog", "List OpenAPI catalog"));
        rows.add(api("GET", "/project1/dossier/openapi/health", "OpenAPI health check"));
        rows.add(api("GET", "/project1/dossier/openapi/version", "Get OpenAPI version"));
        return rows;
    }

    @Override
    public Map<String, Object> selectHealth()
    {
        Map<String, Object> result = map();
        result.put("status", "UP");
        result.put("module", "ruoyi-project1");
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
        result.put("basePath", "/project1/dossier/openapi");
        result.put("compatibleFramework", "RuoYi Cloud Vue3");
        result.put("updatedAt", "2026-06-07");
        return result;
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
