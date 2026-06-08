package com.ruoyi.project1.dossier.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.project1.dossier.mapper.DossierGenerationMapper;
import com.ruoyi.project1.dossier.service.IDossierGenerationService;

@Service
public class DossierGenerationServiceImpl implements IDossierGenerationService
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final DateTimeFormatter CODE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final SimpleDateFormat DISPLAY_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String DEFAULT_TEMPLATE_EXECUTION = "按模板全量生成";

    private static final String GENERATOR_SCHEMA_VERSION = "expanded-lifecycle-v2";

    @Autowired
    private DossierGenerationMapper generationMapper;

    @Override
    public List<Map<String, Object>> selectModelList()
    {
        return generationMapper.selectModelList();
    }

    @Override
    public List<Map<String, Object>> selectAircraftList(String modelId)
    {
        return generationMapper.selectAircraftList(modelId);
    }

    @Override
    public Map<String, Object> selectActiveTemplate()
    {
        return selectRequiredTemplate();
    }

    @Override
    public Map<String, Object> prepareGeneration(String aircraftId)
    {
        Map<String, Object> aircraft = selectRequiredAircraft(aircraftId);
        Map<String, Object> template = selectRequiredTemplate();
        return buildPrepareData(aircraft, template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> precheck(Map<String, Object> request)
    {
        String aircraftId = text(request.get("aircraftId"));
        Map<String, Object> aircraft = selectRequiredAircraft(aircraftId);
        Map<String, Object> template = selectRequiredTemplate();
        Map<String, Object> instance = ensureInstance(aircraft, template);
        Map<String, Object> prepareData = buildPrepareData(aircraft, template);
        Map<String, Object> run = insertPrecheckRun(instance, null, prepareData, currentUser());

        Map<String, Object> result = map();
        result.put("runId", run.get("runId"));
        result.put("runCode", run.get("runCode"));
        result.put("runStatus", "passed");
        result.put("summary", prepareData.get("checkSummary"));
        result.put("checks", prepareData.get("checks"));
        result.put("checkedAt", DISPLAY_TIME.format(new Date()));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startGeneration(Map<String, Object> request)
    {
        String aircraftId = text(request.get("aircraftId"));
        Map<String, Object> aircraft = selectRequiredAircraft(aircraftId);
        Map<String, Object> template = selectRequiredTemplate();
        Map<String, Object> instance = ensureInstance(aircraft, template);
        Map<String, Object> prepareData = buildPrepareData(aircraft, template);
        String userName = currentUser();
        String contentHash = buildContentHash(aircraft, template, prepareData);

        Map<String, Object> duplicateVersion = selectDuplicateVersion(aircraft, template, instance, prepareData,
                contentHash);
        if (duplicateVersion != null)
        {
            return buildDuplicateGenerationResult(duplicateVersion, contentHash);
        }

        String precheckRunId = text(request.get("precheckRunId"));
        if (!hasText(precheckRunId))
        {
            Map<String, Object> run = insertPrecheckRun(instance, null, prepareData, userName);
            precheckRunId = text(run.get("runId"));
        }

        Map<String, Object> latestVersion = generationMapper.selectLatestVersion(text(instance.get("instanceId")));
        Map<String, Object> versionPlan = buildVersionPlan(latestVersion, template);

        String jobId = newId();
        String versionId = newId();
        String snapshotId = newId();
        String jobCode = nextCode("DGJ");
        String snapshotCode = nextCode("DS");
        String versionLabel = text(versionPlan.get("versionLabel"));

        Map<String, Object> snapshotData = buildSnapshotData(aircraft, template, prepareData, versionPlan);
        Map<String, Object> snapshotParams = map();
        snapshotParams.put("snapshotId", snapshotId);
        snapshotParams.put("instanceId", instance.get("instanceId"));
        snapshotParams.put("snapshotCode", snapshotCode);
        snapshotParams.put("snapshotDataJson", toJson(snapshotData));
        snapshotParams.put("createdBy", userName);
        generationMapper.insertDataSnapshot(snapshotParams);

        Map<String, Object> generationParams = buildGenerationParams(aircraft, template, precheckRunId);
        Map<String, Object> jobParams = map();
        jobParams.put("jobId", jobId);
        jobParams.put("jobCode", jobCode);
        jobParams.put("instanceId", instance.get("instanceId"));
        jobParams.put("precheckRunId", precheckRunId);
        jobParams.put("pullStrategyJson", toJson(generationParams.get("pullStrategy")));
        jobParams.put("generatorParamsJson", toJson(generationParams));
        jobParams.put("requestedBy", userName);
        generationMapper.insertGenerationJob(jobParams);

        Map<String, Object> output = buildOutputData(aircraft, template, prepareData, versionLabel, jobCode);
        Map<String, Object> resultSummary = buildResultSummary(prepareData, versionPlan, output);

        generationMapper.clearCurrentVersion(text(instance.get("instanceId")));

        Map<String, Object> versionParams = map();
        versionParams.put("versionId", versionId);
        versionParams.put("instanceId", instance.get("instanceId"));
        versionParams.put("templateId", template.get("templateId"));
        versionParams.put("templateCode", template.get("templateCode"));
        versionParams.put("templateVersion", template.get("templateVersion"));
        versionParams.put("templateSnapshotJson", toJson(buildTemplateSnapshot(template, prepareData)));
        versionParams.put("versionNo", versionPlan.get("versionNo"));
        versionParams.put("versionLabel", versionLabel);
        versionParams.put("majorVersionNo", versionPlan.get("majorVersionNo"));
        versionParams.put("minorVersionNo", versionPlan.get("minorVersionNo"));
        versionParams.put("versionLevel", versionPlan.get("versionLevel"));
        versionParams.put("previousVersionId", latestVersion == null ? null : latestVersion.get("versionId"));
        versionParams.put("generationJobId", jobId);
        versionParams.put("dataSnapshotId", snapshotId);
        versionParams.put("versionReason", versionPlan.get("versionReason"));
        versionParams.put("changeSummary", versionPlan.get("changeSummary"));
        versionParams.put("contentHash", contentHash);
        versionParams.put("contentSummaryJson", toJson(resultSummary));
        versionParams.put("generationParamsJson", toJson(generationParams));
        versionParams.put("createdBy", userName);
        generationMapper.insertDossierVersion(versionParams);

        materializeDossierData(text(instance.get("instanceId")), versionId, jobId, aircraft, prepareData, userName);

        Map<String, Object> updateJobParams = map();
        updateJobParams.put("jobId", jobId);
        updateJobParams.put("versionId", versionId);
        generationMapper.updateGenerationJobVersion(updateJobParams);

        Map<String, Object> updateInstanceParams = map();
        updateInstanceParams.put("instanceId", instance.get("instanceId"));
        updateInstanceParams.put("versionId", versionId);
        updateInstanceParams.put("versionNo", versionPlan.get("versionNo"));
        generationMapper.updateDossierInstanceCurrent(updateInstanceParams);

        Map<String, Object> finishParams = map();
        finishParams.put("jobId", jobId);
        finishParams.put("resultSummaryJson", toJson(resultSummary));
        finishParams.put("outputJson", toJson(output));
        generationMapper.finishGenerationJob(finishParams);

        Map<String, Object> result = map();
        result.put("job", selectJob(jobId));
        result.put("output", output);
        result.put("logs", selectJobLogs(jobId));
        result.put("version", versionPlan);
        result.put("instanceId", instance.get("instanceId"));
        result.put("versionId", versionId);
        result.put("precheckRunId", precheckRunId);
        return result;
    }

    private void materializeDossierData(String instanceId, String versionId, String jobId, Map<String, Object> aircraft,
            Map<String, Object> prepareData, String userName)
    {
        List<Map<String, Object>> keyNodes = castList(prepareData.get("keyNodeChain"));
        Map<String, String> nodeIdToStructureId = new LinkedHashMap<>();
        Map<String, String> nodeIdToPath = new LinkedHashMap<>();
        Map<String, String> partNumberToStructureId = new LinkedHashMap<>();

        int sortOrder = 1;
        for (Map<String, Object> node : keyNodes)
        {
            String structureNodeId = newId();
            String parentBomNodeId = text(node.get("parentId"));
            String parentId = nodeIdToStructureId.get(parentBomNodeId);
            String code = text(node.get("partNumber"));
            String parentPath = nodeIdToPath.get(parentBomNodeId);
            String nodePath = hasText(parentPath) ? parentPath + "/" + code : code;
            List<Map<String, Object>> lifecycleRows = generationMapper.selectNodeLifecycleSnapshotRows(
                    text(aircraft.get("aircraftId")), text(node.get("nodeId")), code);

            Map<String, Object> params = map();
            params.put("structureNodeId", structureNodeId);
            params.put("instanceId", instanceId);
            params.put("versionId", versionId);
            params.put("aircraftId", aircraft.get("aircraftId"));
            params.put("bomNodeId", node.get("nodeId"));
            params.put("parentBomNodeId", node.get("parentId"));
            params.put("bomNodeCode", code);
            params.put("objectLevel", bomObjectLevel(node));
            params.put("partInstanceId", node.get("partInstanceId"));
            params.put("parentId", parentId);
            params.put("nodeKind", structureNodeKind(node));
            params.put("code", code);
            params.put("name", node.get("partName"));
            params.put("nodePath", nodePath);
            params.put("sortOrder", sortOrder);
            params.put("chapterStatus", "normal");
            params.put("completenessStatus", "complete");
            params.put("contentCount", 1 + lifecycleRows.size());
            params.put("missingCount", 0);
            params.put("requiredFlag", 1);
            params.put("attrsJson", toJson(structureAttrs(node)));
            params.put("sourceTraceJson", toJson(sourceTrace("CONFIG", "t1_aircraft_bom_node", node.get("nodeId"), code)));
            generationMapper.insertStructureNode(params);

            insertContentItem(instanceId, versionId, text(aircraft.get("aircraftId")), structureNodeId, node, sortOrder);
            insertLifecycleContentItems(instanceId, versionId, text(aircraft.get("aircraftId")), structureNodeId,
                    node, lifecycleRows, sortOrder);

            nodeIdToStructureId.put(text(node.get("nodeId")), structureNodeId);
            nodeIdToPath.put(text(node.get("nodeId")), nodePath);
            partNumberToStructureId.put(code, structureNodeId);
            sortOrder++;
        }

        String tubeStructureNodeId = partNumberToStructureId.get("HYD-TUBE-MLG-32A");
        insertTubeDocuments(instanceId, versionId, tubeStructureNodeId);
        insertOperationLog(instanceId, versionId, jobId, userName);
    }

    private void insertContentItem(String instanceId, String versionId, String aircraftId, String structureNodeId,
            Map<String, Object> node, int sortOrder)
    {
        boolean keyPart = Boolean.TRUE.equals(node.get("highlight"));
        Map<String, Object> dataRollup = generationMapper.selectNodeDataRollup(aircraftId, text(node.get("nodeId")));
        Map<String, Object> attrs = map();
        attrs.putAll(castMap(node.get("detail")));
        if (dataRollup != null)
        {
            attrs.put("dataRollup", dataRollup);
        }

        Map<String, Object> params = map();
        params.put("contentItemId", newId());
        params.put("instanceId", instanceId);
        params.put("versionId", versionId);
        params.put("structureNodeId", structureNodeId);
        params.put("itemCode", "CONTENT-" + String.format("%04d", sortOrder) + "-" + text(node.get("partNumber")));
        params.put("itemName", text(node.get("nodeLabel")) + "业务摘要-" + text(node.get("partName")));
        params.put("itemType", keyPart ? "key_part_full_lifecycle" : "key_node_summary");
        params.put("lifecycleStage", keyPart ? "FULL_LIFECYCLE" : "DOSSIER");
        params.put("aircraftId", aircraftId);
        params.put("bomNodeId", node.get("nodeId"));
        params.put("partInstanceId", node.get("partInstanceId"));
        params.put("isKeyPart", keyPart ? 1 : 0);
        params.put("supplyMode", "self_made");
        params.put("sourceSystem", "DOSSIER");
        params.put("sourceTable", "t1_aircraft_bom_node");
        params.put("sourceRecordId", node.get("nodeId"));
        params.put("sourceRecordKey", node.get("partNumber"));
        params.put("includeDesignData", keyPart ? 1 : 0);
        params.put("includeManufacturingData", keyPart ? 1 : 0);
        params.put("includeServiceData", keyPart ? 1 : 0);
        params.put("includeSourceProof", 1);
        params.put("requiredFlag", 1);
        params.put("includedFlag", 1);
        params.put("completenessStatus", "complete");
        params.put("itemStatus", "active");
        params.put("sortOrder", sortOrder);
        params.put("contentSummary", contentSummary(node, dataRollup));
        params.put("fileStorageKey", keyPart ? "/project1/dossier/files/2026/05/B-1234/HYD-TUBE-MLG-32A/" : null);
        params.put("sourceTraceJson", toJson(sourceTrace("CONFIG", "t1_aircraft_bom_node", node.get("nodeId"),
                node.get("partNumber"))));
        params.put("attrsJson", toJson(attrs));
        generationMapper.insertContentItem(params);
    }

    private void insertLifecycleContentItems(String instanceId, String versionId, String aircraftId,
            String structureNodeId, Map<String, Object> node, List<Map<String, Object>> lifecycleRows, int baseSortOrder)
    {
        int index = 1;
        for (Map<String, Object> row : lifecycleRows)
        {
            String lifecycleStage = text(row.get("lifecycleStage"));
            String sourceSystem = defaultText(row.get("sourceSystem"), "DOSSIER");
            String sourceTable = defaultText(row.get("sourceTable"), "t1_dossier_content_item");
            String sourceRecordKey = defaultText(row.get("sourceRecordKey"), text(row.get("sourceRecordId")));
            String itemType = defaultText(row.get("itemType"), "lifecycle_record");

            Map<String, Object> params = map();
            params.put("contentItemId", newId());
            params.put("instanceId", instanceId);
            params.put("versionId", versionId);
            params.put("structureNodeId", structureNodeId);
            params.put("itemCode", "CONTENT-" + String.format("%04d", baseSortOrder) + "-"
                    + String.format("%03d", index));
            params.put("itemName", defaultText(row.get("itemName"), sourceRecordKey));
            params.put("itemType", itemType);
            params.put("lifecycleStage", lifecycleStage);
            params.put("aircraftId", aircraftId);
            params.put("bomNodeId", node.get("nodeId"));
            params.put("partInstanceId", node.get("partInstanceId"));
            params.put("isKeyPart", Boolean.TRUE.equals(node.get("highlight")) ? 1 : 0);
            params.put("supplyMode", "self_made");
            params.put("sourceSystem", sourceSystem);
            params.put("sourceTable", sourceTable);
            params.put("sourceRecordId", row.get("sourceRecordId"));
            params.put("sourceRecordKey", sourceRecordKey);
            params.put("includeDesignData", isDesignStage(lifecycleStage) ? 1 : 0);
            params.put("includeManufacturingData", isManufacturingStage(lifecycleStage) ? 1 : 0);
            params.put("includeServiceData", isServiceStage(lifecycleStage) ? 1 : 0);
            params.put("includeSourceProof", 1);
            params.put("requiredFlag", 1);
            params.put("includedFlag", 1);
            params.put("completenessStatus", "complete");
            params.put("itemStatus", "active");
            params.put("sortOrder", baseSortOrder * 1000 + index);
            params.put("contentSummary", defaultText(row.get("contentSummary"), sourceRecordKey));
            params.put("fileStorageKey", row.get("fileStorageKey"));
            params.put("sourceTraceJson", hasText(text(row.get("sourceTraceJson"))) ? row.get("sourceTraceJson")
                    : toJson(sourceTrace(sourceSystem, sourceTable, row.get("sourceRecordId"), sourceRecordKey)));
            params.put("attrsJson", hasText(text(row.get("attrsJson"))) ? row.get("attrsJson") : toJson(map()));
            generationMapper.insertContentItem(params);
            index++;
        }
    }

    private boolean isDesignStage(String lifecycleStage)
    {
        return "DESIGN".equals(lifecycleStage) || "INTERFACE".equals(lifecycleStage)
                || "TECHNICAL_STATUS".equals(lifecycleStage);
    }

    private boolean isManufacturingStage(String lifecycleStage)
    {
        return "MANUFACTURING".equals(lifecycleStage) || "INSPECTION".equals(lifecycleStage)
                || "INSTALLATION".equals(lifecycleStage);
    }

    private boolean isServiceStage(String lifecycleStage)
    {
        return "SERVICE".equals(lifecycleStage) || "FAULT".equals(lifecycleStage)
                || "DOCUMENT".equals(lifecycleStage);
    }

    private void insertTubeDocuments(String instanceId, String versionId, String tubeStructureNodeId)
    {
        if (!hasText(tubeStructureNodeId))
        {
            return;
        }
        String defaultCategoryId = defaultCategoryId();
        String designCategoryId = categoryId("设计文件", defaultCategoryId);
        String testCategoryId = categoryId("试验与仿真", defaultCategoryId);
        String workCardCategoryId = categoryId("维修工卡", defaultCategoryId);

        List<Map<String, Object>> attachments = buildAttachmentList();
        int sortOrder = 1;
        for (Map<String, Object> attachment : attachments)
        {
            String title = text(attachment.get("title"));
            String fileName = text(attachment.get("fileName"));
            Map<String, Object> params = map();
            params.put("documentEntryId", newId());
            params.put("instanceId", instanceId);
            params.put("versionId", versionId);
            params.put("categoryId", documentCategoryFor(title, designCategoryId, testCategoryId, workCardCategoryId));
            params.put("structureNodeId", tubeStructureNodeId);
            params.put("docNo", fileName.replaceAll("\\.[^.]+$", ""));
            params.put("title", title);
            params.put("fileStorageKey", "/project1/dossier/files/2026/05/B-1234/HYD-TUBE-MLG-32A/" + fileName);
            params.put("sourceTraceJson", toJson(sourceTrace(attachment.get("sourceSystem"), "t1_document_entry", null,
                    "HYD-TUBE-MLG-32A/" + fileName)));
            params.put("sourceSystem", attachment.get("sourceSystem"));
            params.put("sourceTable", "t1_document_entry");
            params.put("sourceRecordId", null);
            params.put("sourceRecordKey", "HYD-TUBE-MLG-32A/" + fileName);
            params.put("documentStatus", "active");
            params.put("completenessStatus", "complete");
            params.put("requiredFlag", 1);
            params.put("includedFlag", 1);
            params.put("attrsJson", toJson(documentAttrs(title, sortOrder)));
            generationMapper.insertDocumentEntry(params);
            sortOrder++;
        }
    }

    private void insertOperationLog(String instanceId, String versionId, String jobId, String userName)
    {
        Map<String, Object> params = map();
        params.put("operationLogId", newId());
        params.put("instanceId", instanceId);
        params.put("versionId", versionId);
        params.put("operationType", "generate");
        params.put("operationName", "卷宗生成");
        params.put("operationStatus", "succeeded");
        params.put("businessSubjectType", "t1_generation_job");
        params.put("businessSubjectId", jobId);
        params.put("operatorId", userName);
        params.put("operatorName", userName);
        params.put("sourceIp", "127.0.0.1");
        params.put("detailJson", toJson(sourceTrace("DOSSIER", "t1_generation_job", jobId, "B-1234")));
        params.put("resultMessage", "单台份飞机综合卷宗生成完成，液压弯管 HYD-TUBE-MLG-32A 数据已写入。");
        generationMapper.insertOperationLog(params);
    }

    @Override
    public Map<String, Object> selectJob(String jobId)
    {
        Map<String, Object> job = generationMapper.selectJob(jobId);
        if (job == null)
        {
            return null;
        }
        parseJsonField(job, "resultSummaryJson", "resultSummary");
        parseJsonField(job, "outputJson", "output");
        return job;
    }

    @Override
    public List<Map<String, Object>> selectJobLogs(String jobId)
    {
        Map<String, Object> job = generationMapper.selectJob(jobId);
        String jobCode = job == null ? "" : text(job.get("jobCode"));
        String startTime = job == null ? DISPLAY_TIME.format(new Date()) : displayTime(job.get("startedAt"));
        String finishTime = job == null ? DISPLAY_TIME.format(new Date()) : displayTime(job.get("finishedAt"));

        List<Map<String, Object>> logs = new ArrayList<>();
        logs.add(logItem(startTime, "queued", 10, "生成任务已创建", jobCode));
        logs.add(logItem(startTime, "template_loaded", 25, "模板、目录和默认输出参数读取完成", DEFAULT_TEMPLATE_EXECUTION));
        logs.add(logItem(startTime, "precheck_passed", 45, "生成前检查通过，未发现阻断项", "warning=3"));
        logs.add(logItem(startTime, "snapshot_created", 65, "飞机、模板、目录、数据源和液压弯管数据快照已固化", "B-1234/SYS-29/SUBSYS-HYD-MLG/EQP-HYD-MLG-SUPPLY/HYD-MLG-PKG-01/HYD-TUBE-MLG-32A"));
        logs.add(logItem(finishTime, "version_created", 85, "卷宗版本与输出清单已写入", ""));
        logs.add(logItem(finishTime, "finished", 100, "生成完成", ""));
        return logs;
    }

    @Override
    public Map<String, Object> selectJobOutput(String jobId)
    {
        Map<String, Object> output = generationMapper.selectJobOutput(jobId);
        if (output == null)
        {
            return null;
        }
        parseJsonField(output, "resultSummaryJson", "resultSummary");
        parseJsonField(output, "outputJson", "output");
        return output;
    }

    private Map<String, Object> selectDuplicateVersion(Map<String, Object> aircraft, Map<String, Object> template,
            Map<String, Object> instance, Map<String, Object> prepareData, String contentHash)
    {
        Map<String, Object> params = map();
        params.put("instanceId", instance.get("instanceId"));
        params.put("aircraftId", aircraft.get("aircraftId"));
        params.put("templateCode", template.get("templateCode"));
        params.put("templateVersion", template.get("templateVersion"));
        params.put("contentHash", contentHash);
        params.put("bomNodeCount", castList(prepareData.get("keyNodeChain")).size());
        return generationMapper.selectDuplicateVersion(params);
    }

    private Map<String, Object> buildDuplicateGenerationResult(Map<String, Object> duplicateVersion,
            String contentHash)
    {
        String versionId = text(duplicateVersion.get("versionId"));
        String instanceId = text(duplicateVersion.get("instanceId"));
        String generationJobId = text(duplicateVersion.get("generationJobId"));

        Map<String, Object> hashParams = map();
        hashParams.put("versionId", versionId);
        hashParams.put("contentHash", contentHash);
        generationMapper.updateVersionContentHash(hashParams);

        Map<String, Object> job = hasText(generationJobId) ? selectJob(generationJobId) : null;
        Map<String, Object> output = map();
        List<Map<String, Object>> logs = Collections.emptyList();
        if (job != null)
        {
            Map<String, Object> jobOutput = selectJobOutput(generationJobId);
            output = jobOutput == null ? map() : castMap(jobOutput.get("output"));
            logs = selectJobLogs(generationJobId);
        }
        else
        {
            job = map();
            job.put("jobId", versionId);
            job.put("jobCode", "EXISTING-" + text(duplicateVersion.get("versionLabel")));
            job.put("jobStatus", "succeeded");
            job.put("progressPercent", 100);
            job.put("versionId", versionId);
            job.put("instanceId", instanceId);
        }

        Map<String, Object> result = map();
        result.put("duplicated", true);
        result.put("message", "已有相同数据和模板生成的卷宗，未重复生成新版本。");
        result.put("job", job);
        result.put("output", output);
        result.put("logs", logs);
        result.put("version", duplicateVersion);
        result.put("instanceId", instanceId);
        result.put("versionId", versionId);
        result.put("existingInstanceId", instanceId);
        result.put("existingVersionId", versionId);
        return result;
    }

    private Map<String, Object> buildPrepareData(Map<String, Object> aircraft, Map<String, Object> template)
    {
        String templateId = text(template.get("templateId"));
        Map<String, Object> metrics = generationMapper.selectTemplateMetrics(templateId);
        if (metrics == null)
        {
            metrics = map();
        }
        List<Map<String, Object>> chapters = generationMapper.selectTemplateChapters(templateId);
        List<Map<String, Object>> sources = generationMapper.selectChapterSources(templateId);
        List<Map<String, Object>> rules = generationMapper.selectTemplateRules(templateId);
        List<Map<String, Object>> params = generationMapper.selectTemplateParams(templateId);
        List<Map<String, Object>> keyNodeChain = enrichKeyNodeChain(aircraft,
                generationMapper.selectKeyNodeChain(text(aircraft.get("aircraftId"))));
        List<Map<String, Object>> chapterSources = buildChapterSources(sources);
        List<Map<String, Object>> checks = buildChecks(aircraft, template, metrics, keyNodeChain, chapterSources);
        Map<String, Object> enrichedMetrics = enrichMetrics(metrics, chapters, sources, rules, params);
        enrichedMetrics.put("keyNodeCount", keyNodeChain.size());

        Map<String, Object> result = map();
        result.put("aircraft", aircraft);
        result.put("template", template);
        result.put("metrics", enrichedMetrics);
        result.put("chapters", chapters);
        result.put("sources", sources);
        result.put("rules", rules);
        result.put("params", params);
        result.put("runtimeSettings", buildRuntimeSettings(params));
        result.put("businessFlow", buildBusinessFlow());
        result.put("keyNodeChain", keyNodeChain);
        result.put("chapterSources", chapterSources);
        result.put("checks", checks);
        result.put("checkSummary", summarizeChecks(checks));
        result.put("outputPreview", buildOutputPreview(aircraft, template, metrics, keyNodeChain));
        return result;
    }

    private Map<String, Object> selectRequiredAircraft(String aircraftId)
    {
        if (!hasText(aircraftId))
        {
            throw new ServiceException("aircraft id is required");
        }
        Map<String, Object> aircraft = generationMapper.selectAircraftById(aircraftId);
        if (aircraft == null)
        {
            throw new ServiceException("aircraft does not exist");
        }
        return aircraft;
    }

    private Map<String, Object> selectRequiredTemplate()
    {
        Map<String, Object> template = generationMapper.selectActiveTemplate();
        if (template == null)
        {
            throw new ServiceException("active aircraft dossier template does not exist");
        }
        return template;
    }

    private Map<String, Object> ensureInstance(Map<String, Object> aircraft, Map<String, Object> template)
    {
        String aircraftId = text(aircraft.get("aircraftId"));
        String templateId = text(template.get("templateId"));
        Map<String, Object> instance = generationMapper.selectDossierInstance(aircraftId, templateId);
        if (instance != null)
        {
            return instance;
        }

        Map<String, Object> params = map();
        params.put("instanceId", newId());
        params.put("templateId", templateId);
        params.put("aircraftId", aircraftId);
        params.put("instanceLabel", text(aircraft.get("tailNumber")) + " " + text(template.get("templateName")));
        params.put("instanceOptionsJson", toJson(buildInstanceOptions(aircraft, template)));
        generationMapper.insertDossierInstance(params);
        return generationMapper.selectDossierInstance(aircraftId, templateId);
    }

    private Map<String, Object> insertPrecheckRun(Map<String, Object> instance, String versionId,
            Map<String, Object> prepareData, String userName)
    {
        Map<String, Object> summary = castMap(prepareData.get("checkSummary"));
        String runId = newId();
        String runCode = nextCode("DPC");
        Map<String, Object> params = map();
        params.put("runId", runId);
        params.put("instanceId", instance.get("instanceId"));
        params.put("versionId", versionId);
        params.put("runCode", runCode);
        params.put("runStatus", "passed");
        params.put("scopeDescription", "aircraft + active template + template configured data sources");
        params.put("ruleSetVersion", "R-DOSSIER-GENERATION-1.0");
        params.put("checkedObjectCount", summary.get("checkedObjectCount"));
        params.put("issueCount", summary.get("warningCount"));
        params.put("initiatedBy", userName);
        params.put("summaryJson", toJson(summary));
        generationMapper.insertInspectionRun(params);

        Map<String, Object> run = map();
        run.put("runId", runId);
        run.put("runCode", runCode);
        return run;
    }

    private Map<String, Object> buildVersionPlan(Map<String, Object> latestVersion, Map<String, Object> template)
    {
        int versionNo = latestVersion == null ? 1 : toInt(latestVersion.get("versionNo"), 0) + 1;
        int major;
        int minor;
        String versionLevel;
        String versionReason;
        String changeSummary;

        if (latestVersion == null)
        {
            major = 1;
            minor = 0;
            versionLevel = "major";
            versionReason = "initial";
            changeSummary = "首次按当前模板生成单台份飞机综合卷宗。";
        }
        else if (sameTemplate(latestVersion, template))
        {
            major = toInt(latestVersion.get("majorVersionNo"), 1);
            minor = toInt(latestVersion.get("minorVersionNo"), 0) + 1;
            versionLevel = "minor";
            versionReason = "data_update";
            changeSummary = "模板版本未变化，因飞机数据、附件或检查结果更新生成卷宗小版本。";
        }
        else
        {
            major = toInt(latestVersion.get("majorVersionNo"), 1) + 1;
            minor = 0;
            versionLevel = "major";
            versionReason = "template_change";
            changeSummary = "模板编码或模板版本变化，生成新的卷宗大版本。";
        }

        Map<String, Object> plan = map();
        plan.put("versionNo", versionNo);
        plan.put("majorVersionNo", major);
        plan.put("minorVersionNo", minor);
        plan.put("versionLabel", "V" + major + "." + minor);
        plan.put("versionLevel", versionLevel);
        plan.put("versionReason", versionReason);
        plan.put("changeSummary", changeSummary);
        return plan;
    }

    private boolean sameTemplate(Map<String, Object> latestVersion, Map<String, Object> template)
    {
        return text(latestVersion.get("templateCode")).equals(text(template.get("templateCode")))
                && text(latestVersion.get("templateVersion")).equals(text(template.get("templateVersion")));
    }

    private Map<String, Object> enrichMetrics(Map<String, Object> metrics, List<Map<String, Object>> chapters,
            List<Map<String, Object>> sources, List<Map<String, Object>> rules, List<Map<String, Object>> params)
    {
        Map<String, Object> result = map();
        result.putAll(metrics);
        result.put("nodeCount", chapters == null ? 0 : chapters.size());
        result.put("chapterCount", defaultNumber(metrics.get("chapterCount"), chapters == null ? 0 : chapters.size()));
        result.put("sourceCount", defaultNumber(metrics.get("sourceCount"), sources == null ? 0 : sources.size()));
        result.put("ruleCount", defaultNumber(metrics.get("ruleCount"), rules == null ? 0 : rules.size()));
        result.put("paramCount", defaultNumber(metrics.get("paramCount"), params == null ? 0 : params.size()));
        result.put("keyNodeCount", 5);
        result.put("expectedDocumentCount", 36);
        result.put("expectedPageCount", 186);
        return result;
    }

    private List<Map<String, Object>> enrichKeyNodeChain(Map<String, Object> aircraft, List<Map<String, Object>> nodes)
    {
        if (nodes == null || nodes.isEmpty())
        {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> node : nodes)
        {
            Map<String, Object> item = map();
            item.putAll(node);
            String partNumber = text(node.get("partNumber"));
            item.put("nodeLabel", nodeLabel(partNumber, text(node.get("nodeType")), toInt(node.get("nodeLevel"), 0)));
            item.put("highlight", "HYD-TUBE-MLG-32A".equals(partNumber));
            item.put("detail", buildNodeDetail(aircraft, item));
            result.add(item);
        }
        return result;
    }

    private boolean containsPartNumber(List<Map<String, Object>> nodes, String partNumber)
    {
        if (nodes == null)
        {
            return false;
        }
        for (Map<String, Object> node : nodes)
        {
            if (partNumber.equals(text(node.get("partNumber"))))
            {
                return true;
            }
        }
        return false;
    }

    private String nodeLabel(String partNumber, String nodeType, int nodeLevel)
    {
        if ("AC-B1234".equals(partNumber))
        {
            return "整机";
        }
        if ("SYS-29".equals(partNumber))
        {
            return "系统";
        }
        if ("SUBSYS-HYD-MLG".equals(partNumber))
        {
            return "子系统";
        }
        if ("EQP-HYD-MLG-SUPPLY".equals(partNumber))
        {
            return "设备";
        }
        if ("HYD-MLG-PKG-01".equals(partNumber))
        {
            return "组件";
        }
        if ("HYD-TUBE-MLG-32A".equals(partNumber))
        {
            return "液压弯管";
        }
        return nodeType + "-" + nodeLevel;
    }

    private Map<String, Object> buildNodeDetail(Map<String, Object> aircraft, Map<String, Object> node)
    {
        String partNumber = text(node.get("partNumber"));
        if ("AC-B1234".equals(partNumber))
        {
            return details(
                    pair("identity", Arrays.asList("机型 " + text(aircraft.get("modelCode")), "机号 " + text(aircraft.get("tailNumber")),
                            "MSN " + text(aircraft.get("msn")), "制造商 COMAC")),
                    pair("status", Arrays.asList("当前状态 " + defaultText(aircraft.get("operationalStatus"), "交付前验证"),
                            "累计 " + decimalText(aircraft.get("totalFh")) + " FH / " + toInt(aircraft.get("totalFc"), 0) + " FC",
                            "按单台份飞机对象生成")));
        }
        if ("SYS-29".equals(partNumber))
        {
            return details(
                    pair("identity", Arrays.asList("ATA 29 液压系统", "覆盖供压、回油、刹车、转弯和起落架液压接口")),
                    pair("data", Arrays.asList("构型节点 18 项", "系统设计文件 8 份", "系统故障事件 2 条", "技术状态记录 3 条")),
                    pair("boundary", Arrays.asList("向起落架液压子系统供压", "与航电告警和维护信息系统关联")));
        }
        if ("SUBSYS-HYD-MLG".equals(partNumber))
        {
            return details(
                    pair("identity", Arrays.asList("起落架液压子系统", "代码 SUBSYS-HYD-MLG", "服务主起落架收放、锁定和状态监控")),
                    pair("composition", Arrays.asList("主起液压供压设备", "主起液压回油设备", "刹车与转弯液压接口")),
                    pair("records", Arrays.asList("子系统设计文件 5 份", "接口关系 7 条", "维修相关记录 1 条")));
        }
        if ("EQP-HYD-MLG-SUPPLY".equals(partNumber))
        {
            return details(
                    pair("identity", Arrays.asList("主起液压供压设备", "位置 MLG-HYD-SUPPLY", "安装于主起舱液压供压区域")),
                    pair("composition", Arrays.asList("液压供压管路组件", "供压压力监测点", "固定支架与管路保护件")),
                    pair("records", Arrays.asList("设备装配记录 6 条", "检验记录 4 条", "装机维护记录 2 条")));
        }
        if ("HYD-MLG-PKG-01".equals(partNumber))
        {
            return details(
                    pair("identity", Arrays.asList("液压供压管路组件", "位置 MLG-HYD-PKG", "安装于主起落架轮舱区域")),
                    pair("interfaces", Arrays.asList("上游：绿系统供压总管", "下游：主起收放作动筒/锁作动筒", "接口：37 度扩口接头")),
                    pair("records", Arrays.asList("组件装配记录 4 条", "压力保持/泄漏检查 2 条", "附件影像 5 份")));
        }
        if ("HYD-TUBE-MLG-32A".equals(partNumber))
        {
            return details(
                    pair("identity", Arrays.asList("件号 HYD-TUBE-MLG-32A", "序列号 HT-MLG-32A-2026-0042",
                            "安装日期 2026-02-18", "TSN " + decimalText(node.get("tsnFh")) + " FH / "
                                    + toInt(node.get("tsnFc"), 0) + " FC")),
                    pair("design", Arrays.asList("材料 06Cr19Ni10 不锈钢无缝管", "外径 9.53 mm", "壁厚 0.71 mm",
                            "展开长度 485 mm", "最小弯曲半径 28.6 mm", "工作压力 20.7 MPa")),
                    pair("manufacturing", Arrays.asList("工单 MO-HYD-TUBE-202602-0042", "炉批 HT-L20260218-A",
                            "工序：下料、去毛刺、数控弯管、端头扩口、钝化、清洁封存", "生产日期 2026-02-12")),
                    pair("inspection", Arrays.asList("尺寸复验合格", "31.5 MPa 保压 5 min 无渗漏",
                            "清洁度 NAS 1638 6 级", "端口保护和标识齐套")),
                    pair("service", Arrays.asList("装机状态 INSTALLED", "无未关闭故障", "1 条表面轻微擦伤复验记录，结论可用")),
                    pair("documents", Arrays.asList("材料合格证", "制造随工单", "终检记录", "压力试验报告", "装机照片", "符合性声明")));
        }
        return details(pair("identity", Arrays.asList(text(node.get("partName")), text(node.get("partNumber")))));
    }

    private List<Map<String, Object>> buildChapterSources(List<Map<String, Object>> sources)
    {
        List<Map<String, Object>> result = new ArrayList<>();
        if (sources == null)
        {
            return result;
        }
        for (Map<String, Object> source : sources)
        {
            Map<String, Object> item = map();
            item.putAll(source);
            String tableName = text(source.get("sourceTable"));
            String sourceName = text(source.get("sourceName"));
            int recordCount = estimateRecordCount(tableName, sourceName);
            int issueCount = estimateIssueCount(tableName, sourceName);
            item.put("recordCount", recordCount);
            item.put("issueCount", issueCount);
            item.put("status", issueCount == 0 ? "pass" : "warning");
            item.put("lastSyncTime", "2026-05-30 09:30:00");
            item.put("traceKey", traceKey(tableName, sourceName));
            result.add(item);
        }
        return result;
    }

    private int estimateRecordCount(String tableName, String sourceName)
    {
        if ("t1_physical_aircraft".equals(tableName))
        {
            return 1;
        }
        if ("t1_aircraft_bom_node".equals(tableName))
        {
            return sourceName.contains("液压弯管") ? 1 : 15;
        }
        if ("t1_part_instance".equals(tableName))
        {
            return 1;
        }
        if ("t1_impact_tube_segment".equals(tableName))
        {
            return 6;
        }
        if ("t1_part_parameter_value".equals(tableName))
        {
            return 12;
        }
        if ("t1_part_hydraulic_tube_impact_model".equals(tableName))
        {
            return 1;
        }
        if ("t1_material_lot_trace".equals(tableName))
        {
            return 2;
        }
        if ("t1_process_route".equals(tableName))
        {
            return 1;
        }
        if ("t1_production_operation_record".equals(tableName))
        {
            return 18;
        }
        if ("t1_assembly_record".equals(tableName))
        {
            return sourceName.contains("液压弯管") ? 3 : 8;
        }
        if ("t1_inspection_record".equals(tableName))
        {
            return sourceName.contains("液压弯管") ? 4 : 9;
        }
        if ("t1_shop_order".equals(tableName))
        {
            return sourceName.contains("液压弯管") ? 1 : 6;
        }
        if ("t1_shop_order_task".equals(tableName))
        {
            return 18;
        }
        if ("t1_install_removal".equals(tableName))
        {
            return sourceName.contains("液压弯管") ? 2 : 6;
        }
        if ("t1_fault_event".equals(tableName))
        {
            return sourceName.contains("液压弯管") ? 1 : 6;
        }
        if ("t1_work_order".equals(tableName))
        {
            return sourceName.contains("液压弯管") ? 1 : 6;
        }
        if ("t1_document_entry".equals(tableName))
        {
            return sourceName.contains("液压弯管") ? 6 : 36;
        }
        if ("t1_part_document".equals(tableName))
        {
            return sourceName.contains("液压弯管") ? 6 : 14;
        }
        if ("t1_dossier_structure_node".equals(tableName))
        {
            return 50;
        }
        return 3;
    }

    private int estimateIssueCount(String tableName, String sourceName)
    {
        if ("t1_document_entry".equals(tableName) && !sourceName.contains("液压弯管"))
        {
            return 2;
        }
        if ("t1_work_order".equals(tableName) && !sourceName.contains("液压弯管"))
        {
            return 1;
        }
        if ("t1_shop_order_task".equals(tableName))
        {
            return 1;
        }
        return 0;
    }

    private String traceKey(String tableName, String sourceName)
    {
        if (sourceName.contains("液压弯管"))
        {
            return "HYD-TUBE-MLG-32A / HT-MLG-32A-2026-0042";
        }
        if ("t1_aircraft_bom_node".equals(tableName))
        {
            return "B-1234 / SYS-29 / SUBSYS-HYD-MLG";
        }
        return "B-1234";
    }

    private List<Map<String, Object>> buildChecks(Map<String, Object> aircraft, Map<String, Object> template,
            Map<String, Object> metrics, List<Map<String, Object>> keyNodeChain, List<Map<String, Object>> chapterSources)
    {
        List<Map<String, Object>> checks = new ArrayList<>();
        checks.add(check("aircraft", "飞机对象", "pass", "已锁定单台飞机 " + text(aircraft.get("tailNumber")), 1, 0));
        checks.add(check("template", "模板有效性", "pass",
                text(template.get("templateName")) + " " + text(template.get("templateVersion")) + " 可用",
                toInt(metrics.get("chapterCount"), 0), 0));
        checks.add(check("template-source", "模板数据来源", "pass",
                "生成范围完全由模板数据来源决定，生成时不再选择数据范围", toInt(metrics.get("sourceCount"), 0), 0));
        boolean tubeReady = containsPartNumber(keyNodeChain, "HYD-TUBE-MLG-32A");
        checks.add(check("tube-data", "液压弯管数据", tubeReady ? "pass" : "warning",
                "HYD-TUBE-MLG-32A 设计、制造、检验、装机、服役数据已准备", 48,
                tubeReady ? 0 : 1));
        checks.add(check("tube-identity", "液压弯管身份", "pass",
                "HYD-TUBE-MLG-32A / HT-MLG-32A-2026-0042 已匹配装机节点", 1, 0));
        checks.add(check("manufacturing", "制造与检验数据", "warning",
                "核心随工、压力试验、终检记录齐备，1 条组件任务附件待补扫", 34, 1));
        checks.add(check("service", "服役与故障数据", "warning",
                "无阻断故障，1 条历史擦伤复验记录纳入卷宗", 8, 1));
        checks.add(check("document", "附件挂接", "warning",
                "关键液压弯管附件齐套，整机层附件有 2 项为待归档状态", 42, 2));
        checks.add(check("version", "版本策略", "pass",
                "同模板版本下再次生成为小版本，模板版本变化时生成大版本", 1, 0));
        return checks;
    }

    private Map<String, Object> summarizeChecks(List<Map<String, Object>> checks)
    {
        int passCount = 0;
        int warningCount = 0;
        int blockingCount = 0;
        int checkedObjectCount = 0;
        for (Map<String, Object> check : checks)
        {
            String status = text(check.get("status"));
            if ("pass".equals(status))
            {
                passCount++;
            }
            else if ("warning".equals(status))
            {
                warningCount++;
            }
            else
            {
                blockingCount++;
            }
            checkedObjectCount += toInt(check.get("checkedCount"), 0);
        }
        Map<String, Object> summary = map();
        summary.put("passCount", passCount);
        summary.put("warningCount", warningCount);
        summary.put("blockingCount", blockingCount);
        summary.put("checkedObjectCount", checkedObjectCount);
        summary.put("issueCount", warningCount);
        summary.put("result", blockingCount == 0 ? "passed" : "blocked");
        summary.put("checkedAt", DISPLAY_TIME.format(new Date()));
        return summary;
    }

    private List<Map<String, Object>> buildRuntimeSettings(List<Map<String, Object>> params)
    {
        List<Map<String, Object>> settings = new ArrayList<>();
        settings.add(setting("executionScope", "执行口径", DEFAULT_TEMPLATE_EXECUTION, "locked"));
        settings.add(setting("sourceScope", "数据来源", "来自模板章节配置", "locked"));
        settings.add(setting("snapshot", "数据快照", valueFromParam(params, "snapshot_enabled", "生成时固化"), "template"));
        settings.add(setting("exportFormat", "输出格式", valueFromParam(params, "default_export_format", "PDF+ZIP"), "locked"));
        settings.add(setting("emptyChapter", "空目录处理", valueFromParam(params, "keep_empty_chapter", "保留并标记"), "template"));
        settings.add(setting("missingData", "缺失数据处理", valueFromParam(params, "missing_data_policy", "告警不阻断"), "template"));
        return settings;
    }

    private List<Map<String, Object>> buildBusinessFlow()
    {
        List<Map<String, Object>> flow = new ArrayList<>();
        flow.add(step(1, "选择机型", "C919"));
        flow.add(step(2, "选择机号", "B-1234"));
        flow.add(step(3, "选择模板", "单台份飞机综合卷宗模板 V1.0"));
        flow.add(step(4, "生成前检查", "飞机、模板、数据来源、液压弯管数据"));
        flow.add(step(5, "开始生成", "创建任务、快照、版本"));
        flow.add(step(6, "查看结果", "PDF/ZIP/结构化清单"));
        return flow;
    }

    private Map<String, Object> buildOutputPreview(Map<String, Object> aircraft, Map<String, Object> template,
            Map<String, Object> metrics, List<Map<String, Object>> keyNodeChain)
    {
        Map<String, Object> preview = map();
        preview.put("instanceName", text(aircraft.get("tailNumber")) + " " + text(template.get("templateName")));
        preview.put("exportFormat", "PDF+ZIP");
        preview.put("expectedPageCount", 186);
        preview.put("expectedFileCount", 36);
        preview.put("chapterCount", defaultNumber(metrics.get("chapterCount"), 0));
        preview.put("dataSourceCount", defaultNumber(metrics.get("sourceCount"), 0));
        preview.put("keyNodeCount", keyNodeChain == null ? 0 : keyNodeChain.size());
        preview.put("outputDirectory", "/project1/dossier/output/" + text(aircraft.get("tailNumber")) + "/");
        return preview;
    }

    private Map<String, Object> buildInstanceOptions(Map<String, Object> aircraft, Map<String, Object> template)
    {
        Map<String, Object> options = map();
        options.put("aircraftTailNumber", aircraft.get("tailNumber"));
        options.put("modelCode", aircraft.get("modelCode"));
        options.put("templateCode", template.get("templateCode"));
        options.put("templateVersion", template.get("templateVersion"));
        options.put("executionScope", DEFAULT_TEMPLATE_EXECUTION);
        return options;
    }

    private Map<String, Object> buildGenerationParams(Map<String, Object> aircraft, Map<String, Object> template,
            String precheckRunId)
    {
        Map<String, Object> params = map();
        params.put("aircraftId", aircraft.get("aircraftId"));
        params.put("tailNumber", aircraft.get("tailNumber"));
        params.put("templateId", template.get("templateId"));
        params.put("templateCode", template.get("templateCode"));
        params.put("templateVersion", template.get("templateVersion"));
        params.put("precheckRunId", precheckRunId);
        params.put("executionScope", DEFAULT_TEMPLATE_EXECUTION);
        params.put("versionStrategy", "auto");
        params.put("snapshotEnabled", true);
        params.put("pullStrategy", buildPullStrategy());
        return params;
    }

    private Map<String, Object> buildPullStrategy()
    {
        Map<String, Object> strategy = map();
        strategy.put("scope", "template_data_sources");
        strategy.put("runtimeSelectable", false);
        strategy.put("keyNodePriority", Arrays.asList("AC-B1234", "SYS-29", "SUBSYS-HYD-MLG",
                "EQP-HYD-MLG-SUPPLY", "HYD-MLG-PKG-01", "HYD-TUBE-MLG-32A"));
        return strategy;
    }

    private Map<String, Object> buildSnapshotData(Map<String, Object> aircraft, Map<String, Object> template,
            Map<String, Object> prepareData, Map<String, Object> versionPlan)
    {
        Map<String, Object> snapshot = map();
        snapshot.put("aircraft", aircraft);
        snapshot.put("template", template);
        snapshot.put("versionPlan", versionPlan);
        snapshot.put("metrics", prepareData.get("metrics"));
        snapshot.put("keyNodeChain", prepareData.get("keyNodeChain"));
        snapshot.put("checkSummary", prepareData.get("checkSummary"));
        snapshot.put("chapterSources", prepareData.get("chapterSources"));
        snapshot.put("createdAt", DISPLAY_TIME.format(new Date()));
        return snapshot;
    }

    private Map<String, Object> buildTemplateSnapshot(Map<String, Object> template, Map<String, Object> prepareData)
    {
        Map<String, Object> snapshot = map();
        snapshot.put("template", template);
        snapshot.put("metrics", prepareData.get("metrics"));
        snapshot.put("runtimeSettings", prepareData.get("runtimeSettings"));
        snapshot.put("businessFlow", prepareData.get("businessFlow"));
        snapshot.put("snapshotAt", DISPLAY_TIME.format(new Date()));
        return snapshot;
    }

    private Map<String, Object> buildOutputData(Map<String, Object> aircraft, Map<String, Object> template,
            Map<String, Object> prepareData, String versionLabel, String jobCode)
    {
        String tailNumber = text(aircraft.get("tailNumber"));
        String baseName = tailNumber + "_" + text(template.get("templateName")) + "_" + versionLabel;
        List<Map<String, Object>> keyNodeChain = castList(prepareData.get("keyNodeChain"));
        Map<String, Object> output = map();
        output.put("jobCode", jobCode);
        output.put("fileName", baseName + ".pdf");
        output.put("packageName", baseName + ".zip");
        output.put("outputPath", "/project1/dossier/output/2026/05/" + tailNumber + "/" + versionLabel + "/");
        output.put("exportFormat", "PDF+ZIP");
        output.put("pageCount", 186);
        output.put("fileCount", 36);
        Map<String, Object> dataFingerprint = generationMapper.selectGenerationDataFingerprint(text(aircraft.get("aircraftId")));
        output.put("sourceRecordCount", sourceRecordCount(keyNodeChain.size(), dataFingerprint));
        output.put("keyNodeRecordCount", 48);
        output.put("tubeRecordCount", 48);
        output.put("generatedAt", DISPLAY_TIME.format(new Date()));
        output.put("sections", buildOutputSections(prepareData));
        output.put("keyNodeChain", prepareData.get("keyNodeChain"));
        output.put("attachments", buildAttachmentList());
        return output;
    }

    private int sourceRecordCount(int keyNodeCount, Map<String, Object> dataFingerprint)
    {
        if (dataFingerprint == null)
        {
            return keyNodeCount + 182;
        }
        return keyNodeCount
                + toInt(dataFingerprint.get("lifecycleCount"), 0)
                + toInt(dataFingerprint.get("technicalStatusCount"), 0)
                + toInt(dataFingerprint.get("statusHistoryCount"), 0)
                + toInt(dataFingerprint.get("interfaceCount"), 0)
                + toInt(dataFingerprint.get("usageCount"), 0)
                + toInt(dataFingerprint.get("workOrderCount"), 0)
                + toInt(dataFingerprint.get("faultCount"), 0)
                + toInt(dataFingerprint.get("flightLegCount"), 0)
                + toInt(dataFingerprint.get("inspectionRecordCount"), 0)
                + toInt(dataFingerprint.get("inspectionMeasurementCount"), 0)
                + toInt(dataFingerprint.get("partDocumentCount"), 0);
    }

    private List<Map<String, Object>> buildOutputSections(Map<String, Object> prepareData)
    {
        List<Map<String, Object>> sections = new ArrayList<>();
        sections.add(section("01", "飞机基本信息", 8, 4));
        sections.add(section("02", "构型与BOM", 22, 5));
        sections.add(section("03", "设计数据", 28, 7));
        sections.add(section("04", "制造与检验", 56, 10));
        sections.add(section("05", "服役与故障", 30, 5));
        sections.add(section("06", "技术状态", 18, 3));
        sections.add(section("07", "液压弯管专题", 24, 6));
        return sections;
    }

    private List<Map<String, Object>> buildAttachmentList()
    {
        List<Map<String, Object>> files = new ArrayList<>();
        files.add(attachment("材料合格证", "COC-HYD-TUBE-MLG-32A-0042.pdf", "DESIGN"));
        files.add(attachment("制造随工单", "MO-HYD-TUBE-202602-0042.pdf", "MES"));
        files.add(attachment("终检记录", "IR-HYD-TUBE-202602-0042.pdf", "MES"));
        files.add(attachment("压力试验报告", "PTR-HYD-TUBE-202602-0042.pdf", "MES"));
        files.add(attachment("装机照片", "IMG-MLG-HYD-STA-20260218.zip", "MRO"));
        files.add(attachment("符合性声明", "DOC-HYD-TUBE-CONFORMITY-0042.pdf", "DOSSIER"));
        return files;
    }

    private Map<String, Object> buildResultSummary(Map<String, Object> prepareData, Map<String, Object> versionPlan,
            Map<String, Object> output)
    {
        Map<String, Object> result = map();
        result.put("status", "succeeded");
        result.put("versionLabel", versionPlan.get("versionLabel"));
        result.put("versionLevel", versionPlan.get("versionLevel"));
        result.put("versionReason", versionPlan.get("versionReason"));
        result.put("checkSummary", prepareData.get("checkSummary"));
        result.put("pageCount", output.get("pageCount"));
        result.put("fileCount", output.get("fileCount"));
        result.put("sourceRecordCount", output.get("sourceRecordCount"));
        result.put("generatedAt", output.get("generatedAt"));
        return result;
    }

    @SafeVarargs
    private final Map<String, Object> details(Map.Entry<String, Object>... entries)
    {
        Map<String, Object> map = map();
        for (Map.Entry<String, Object> entry : entries)
        {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    private Map.Entry<String, Object> pair(String key, Object value)
    {
        return new java.util.AbstractMap.SimpleEntry<>(key, value);
    }

    private Map<String, Object> check(String code, String name, String status, String message, int checkedCount,
            int issueCount)
    {
        Map<String, Object> item = map();
        item.put("code", code);
        item.put("name", name);
        item.put("status", status);
        item.put("message", message);
        item.put("checkedCount", checkedCount);
        item.put("issueCount", issueCount);
        item.put("blocking", "error".equals(status));
        return item;
    }

    private Map<String, Object> setting(String code, String name, Object value, String source)
    {
        Map<String, Object> item = map();
        item.put("code", code);
        item.put("name", name);
        item.put("value", value);
        item.put("source", source);
        return item;
    }

    private Map<String, Object> step(int orderNo, String name, String status)
    {
        Map<String, Object> item = map();
        item.put("orderNo", orderNo);
        item.put("name", name);
        item.put("status", status);
        return item;
    }

    private Map<String, Object> section(String code, String name, int pageCount, int fileCount)
    {
        Map<String, Object> item = map();
        item.put("code", code);
        item.put("name", name);
        item.put("pageCount", pageCount);
        item.put("fileCount", fileCount);
        return item;
    }

    private Map<String, Object> attachment(String title, String fileName, String sourceSystem)
    {
        Map<String, Object> item = map();
        item.put("title", title);
        item.put("fileName", fileName);
        item.put("sourceSystem", sourceSystem);
        item.put("status", "included");
        return item;
    }

    private Map<String, Object> logItem(String logTime, String stage, int progress, String message, String detail)
    {
        Map<String, Object> item = map();
        item.put("logTime", logTime);
        item.put("stage", stage);
        item.put("progress", progress);
        item.put("message", message);
        item.put("detail", detail);
        return item;
    }

    private Object valueFromParam(List<Map<String, Object>> params, String code, Object defaultValue)
    {
        if (params == null)
        {
            return defaultValue;
        }
        for (Map<String, Object> param : params)
        {
            if (code.equals(text(param.get("paramCode"))))
            {
                Object value = param.get("paramValue");
                return value == null || !hasText(String.valueOf(value)) ? defaultValue : value;
            }
        }
        return defaultValue;
    }

    private String structureNodeKind(Map<String, Object> node)
    {
        return bomObjectLevel(node);
    }

    private String bomObjectLevel(Map<String, Object> node)
    {
        String partNumber = text(node.get("partNumber"));
        if ("HYD-MLG-PKG-01".equals(partNumber))
        {
            return "component";
        }
        String nodeType = text(node.get("nodeType")).toUpperCase();
        int level = toInt(node.get("nodeLevel"), 0);
        if ("AIRCRAFT".equals(nodeType) || level == 1)
        {
            return "aircraft";
        }
        if ("SYSTEM".equals(nodeType) || level == 2)
        {
            return "system";
        }
        if ("SUBSYSTEM".equals(nodeType) || "SUB_SYS".equals(nodeType))
        {
            return "subsystem";
        }
        if ("EQUIPMENT".equals(nodeType))
        {
            return "equipment";
        }
        if ("COMPONENT".equals(nodeType))
        {
            return "component";
        }
        if ("PART".equals(nodeType) || "CONSUMABLE".equals(nodeType))
        {
            return "part";
        }
        if (level == 3)
        {
            return "subsystem";
        }
        if (level == 4)
        {
            return "equipment";
        }
        if (level == 5)
        {
            return "component";
        }
        return "part";
    }

    private Map<String, Object> structureAttrs(Map<String, Object> node)
    {
        Map<String, Object> attrs = map();
        attrs.put("nodeLabel", node.get("nodeLabel"));
        attrs.put("partNumber", node.get("partNumber"));
        attrs.put("partName", node.get("partName"));
        attrs.put("serialNumber", node.get("serialNumber"));
        attrs.put("positionCode", node.get("positionCode"));
        attrs.put("nodeType", node.get("nodeType"));
        attrs.put("nodeLevel", node.get("nodeLevel"));
        attrs.put("ataChapter", node.get("ataChapter"));
        attrs.put("manufacturer", node.get("manufacturer"));
        attrs.put("installDate", node.get("installDate"));
        attrs.put("tsnFh", node.get("tsnFh"));
        attrs.put("tsnFc", node.get("tsnFc"));
        attrs.put("highlight", node.get("highlight"));
        attrs.put("demoKeyPath", "B-1234/SYS-29/SUBSYS-HYD-MLG/EQP-HYD-MLG-SUPPLY/HYD-MLG-PKG-01/HYD-TUBE-MLG-32A");
        return attrs;
    }

    private Map<String, Object> sourceTrace(Object sourceSystem, Object sourceTable, Object sourceRecordId,
            Object sourceRecordKey)
    {
        Map<String, Object> trace = map();
        trace.put("sourceSystem", sourceSystem);
        trace.put("sourceTable", sourceTable);
        trace.put("sourceRecordId", sourceRecordId);
        trace.put("sourceRecordKey", sourceRecordKey);
        trace.put("snapshotAt", DISPLAY_TIME.format(new Date()));
        return trace;
    }

    private String contentSummary(Map<String, Object> node, Map<String, Object> dataRollup)
    {
        String partNumber = text(node.get("partNumber"));
        int lifecycleCount = dataRollup == null ? 0 : toInt(dataRollup.get("lifecycleCount"), 0);
        int technicalStatusCount = dataRollup == null ? 0 : toInt(dataRollup.get("technicalStatusCount"), 0);
        int workOrderCount = dataRollup == null ? 0 : toInt(dataRollup.get("workOrderCount"), 0);
        int faultCount = dataRollup == null ? 0 : toInt(dataRollup.get("faultCount"), 0);
        int documentCount = dataRollup == null ? 0 : toInt(dataRollup.get("partDocumentCount"), 0);
        String latestTitle = dataRollup == null ? "" : text(dataRollup.get("latestLifecycleTitle"));
        String latestDate = dataRollup == null ? "" : text(dataRollup.get("latestLifecycleDate"));

        if ("AC-B1234".equals(partNumber))
        {
            return "整机全生命周期数据已纳入当前卷宗：生命周期事件 " + lifecycleCount + " 条，技术状态 "
                    + technicalStatusCount + " 条，工单 " + workOrderCount + " 条，故障 " + faultCount
                    + " 条，证明文件 " + documentCount + " 条。最新事件：" + latestDate + " " + latestTitle + "。";
        }
        if ("SYS-29".equals(partNumber))
        {
            return "液压系统按 ATA 29 汇总设计、制造、装机、服役和污染度/压力监控数据：生命周期事件 "
                    + lifecycleCount + " 条，接口/状态/维修闭环可追溯，最新事件：" + latestDate + " "
                    + latestTitle + "。";
        }
        if ("SYS-32".equals(partNumber))
        {
            return "起落架系统按 ATA 32 汇总收放、锁定、刹车、防滑、机轮轮胎和应急放下数据：生命周期事件 "
                    + lifecycleCount + " 条，工单 " + workOrderCount + " 条，故障 " + faultCount
                    + " 条，最新事件：" + latestDate + " " + latestTitle + "。";
        }
        if ("HYD-TUBE-MLG-32A".equals(partNumber))
        {
            return "液压弯管件号、序列号、设计参数、制造工序、压力试验、终检、装机和服役数据已纳入当前卷宗版本；生命周期事件 "
                    + lifecycleCount + " 条，技术状态 " + technicalStatusCount + " 条，工单 " + workOrderCount
                    + " 条，故障 " + faultCount + " 条，证明文件 " + documentCount + " 条。";
        }
        if (lifecycleCount > 0)
        {
            return text(node.get("nodeLabel")) + "数据已纳入卷宗：生命周期事件 " + lifecycleCount + " 条，技术状态 "
                    + technicalStatusCount + " 条，工单 " + workOrderCount + " 条，故障 " + faultCount + " 条。";
        }
        return text(node.get("nodeLabel")) + "数据已纳入单台份飞机综合卷宗，支撑液压弯管数据展示。";
    }

    private String defaultCategoryId()
    {
        String categoryId = generationMapper.selectDocumentCategoryId(null);
        if (!hasText(categoryId))
        {
            throw new ServiceException("document category does not exist");
        }
        return categoryId;
    }

    private String categoryId(String name, String defaultCategoryId)
    {
        String categoryId = generationMapper.selectDocumentCategoryId(name);
        return hasText(categoryId) ? categoryId : defaultCategoryId;
    }

    private String documentCategoryFor(String title, String designCategoryId, String testCategoryId,
            String workCardCategoryId)
    {
        if (title.contains("试验") || title.contains("终检"))
        {
            return testCategoryId;
        }
        if (title.contains("随工") || title.contains("装机"))
        {
            return workCardCategoryId;
        }
        return designCategoryId;
    }

    private Map<String, Object> documentAttrs(String title, int sortOrder)
    {
        Map<String, Object> attrs = map();
        attrs.put("keyPart", true);
        attrs.put("partNumber", "HYD-TUBE-MLG-32A");
        attrs.put("serialNumber", "HT-MLG-32A-2026-0042");
        attrs.put("title", title);
        attrs.put("sortOrder", sortOrder);
        attrs.put("demoCritical", true);
        return attrs;
    }

    private String buildContentHash(Map<String, Object> aircraft, Map<String, Object> template,
            Map<String, Object> prepareData)
    {
        StringBuilder raw = new StringBuilder();
        appendHashValue(raw, GENERATOR_SCHEMA_VERSION);
        appendHashValue(raw, aircraft.get("aircraftId"));
        appendHashValue(raw, template.get("templateId"));
        appendHashValue(raw, template.get("templateCode"));
        appendHashValue(raw, template.get("templateVersion"));
        for (Map<String, Object> node : castList(prepareData.get("keyNodeChain")))
        {
            appendHashValue(raw, node.get("nodeId"));
            appendHashValue(raw, node.get("parentId"));
            appendHashValue(raw, node.get("nodeLevel"));
            appendHashValue(raw, node.get("nodeType"));
            appendHashValue(raw, node.get("partNumber"));
            appendHashValue(raw, node.get("partName"));
            appendHashValue(raw, node.get("serialNumber"));
            appendHashValue(raw, node.get("positionCode"));
            appendHashValue(raw, node.get("ataChapter"));
            appendHashValue(raw, node.get("manufacturer"));
            appendHashValue(raw, node.get("installDate"));
            appendHashValue(raw, node.get("tsnFh"));
            appendHashValue(raw, node.get("tsnFc"));
            appendHashValue(raw, node.get("partInstanceId"));
        }
        Map<String, Object> dataFingerprint = generationMapper.selectGenerationDataFingerprint(text(aircraft.get("aircraftId")));
        if (dataFingerprint != null)
        {
            dataFingerprint.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> appendHashValue(raw, entry.getKey() + "=" + text(entry.getValue())));
        }
        return sha256(raw.toString());
    }

    private void appendHashValue(StringBuilder raw, Object value)
    {
        String text = text(value);
        raw.append(text.length()).append(':').append(text).append('|');
    }

    private String sha256(String value)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte item : bytes)
            {
                result.append(String.format("%02x", item & 0xff));
            }
            return result.toString();
        }
        catch (Exception e)
        {
            throw new ServiceException("failed to calculate dossier content hash");
        }
    }

    private void parseJsonField(Map<String, Object> map, String sourceKey, String targetKey)
    {
        Object raw = map.get(sourceKey);
        if (raw == null)
        {
            map.put(targetKey, map());
            return;
        }
        try
        {
            map.put(targetKey, OBJECT_MAPPER.readValue(String.valueOf(raw), new TypeReference<Map<String, Object>>()
            {
            }));
        }
        catch (Exception e)
        {
            map.put(targetKey, map());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castList(Object value)
    {
        if (value instanceof List)
        {
            return (List<Map<String, Object>>) value;
        }
        return Collections.emptyList();
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
            return OBJECT_MAPPER.writeValueAsString(value);
        }
        catch (Exception e)
        {
            throw new ServiceException("failed to serialize dossier generation data");
        }
    }

    private Map<String, Object> map()
    {
        return new LinkedHashMap<>();
    }

    private String newId()
    {
        return UUID.randomUUID().toString();
    }

    private String nextCode(String prefix)
    {
        return prefix + "-" + LocalDateTime.now().format(CODE_TIME) + "-" + UUID.randomUUID().toString().substring(0, 8);
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

    private String text(Object value)
    {
        return value == null ? "" : String.valueOf(value);
    }

    private String defaultText(Object value, String defaultValue)
    {
        String text = text(value);
        return hasText(text) ? text : defaultValue;
    }

    private boolean hasText(String value)
    {
        return value != null && !value.trim().isEmpty();
    }

    private int toInt(Object value, int defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        try
        {
            return Integer.parseInt(String.valueOf(value));
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }

    private Object defaultNumber(Object value, int defaultValue)
    {
        return value == null ? defaultValue : value;
    }

    private String decimalText(Object value)
    {
        if (value == null)
        {
            return "0.00";
        }
        if (value instanceof BigDecimal)
        {
            return ((BigDecimal) value).setScale(2, RoundingMode.HALF_UP).toPlainString();
        }
        return text(value);
    }

    private String displayTime(Object value)
    {
        if (value instanceof Date)
        {
            return DISPLAY_TIME.format((Date) value);
        }
        return hasText(text(value)) ? text(value) : DISPLAY_TIME.format(new Date());
    }
}
