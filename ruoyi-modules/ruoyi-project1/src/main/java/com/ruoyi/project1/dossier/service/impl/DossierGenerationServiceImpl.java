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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private static final String GENERATOR_SCHEMA_VERSION = "template-source-snapshot-v3";

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
        return selectRequiredTemplate(null);
    }

    @Override
    public List<Map<String, Object>> selectTemplateList()
    {
        return generationMapper.selectGenerationTemplates();
    }

    @Override
    public Map<String, Object> prepareGeneration(String aircraftId, String templateId)
    {
        Map<String, Object> aircraft = selectRequiredAircraft(aircraftId);
        Map<String, Object> template = selectRequiredTemplate(templateId);
        return buildPrepareData(aircraft, template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> precheck(Map<String, Object> request)
    {
        String aircraftId = text(request.get("aircraftId"));
        String templateId = text(request.get("templateId"));
        Map<String, Object> aircraft = selectRequiredAircraft(aircraftId);
        Map<String, Object> template = selectRequiredTemplate(templateId);
        Map<String, Object> instance = ensureInstance(aircraft, template);
        Map<String, Object> prepareData = buildPrepareData(aircraft, template);
        Map<String, Object> run = insertPrecheckRun(instance, null, prepareData, currentUser());

        Map<String, Object> result = map();
        result.put("runId", run.get("runId"));
        result.put("runCode", run.get("runCode"));
        result.put("runStatus", precheckStatus(castMap(prepareData.get("checkSummary"))));
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
        String templateId = text(request.get("templateId"));
        Map<String, Object> aircraft = selectRequiredAircraft(aircraftId);
        Map<String, Object> template = selectRequiredTemplate(templateId);
        Map<String, Object> instance = ensureInstance(aircraft, template);
        Map<String, Object> prepareData = buildPrepareData(aircraft, template);
        assertPrecheckPassed(prepareData);
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

        MaterializedDossierStats materializedStats = materializeDossierData(text(instance.get("instanceId")), versionId, jobId,
                aircraft, prepareData, userName);
        output.put("fileCount", materializedStats.getDocumentCount());
        output.put("contentItemCount", materializedStats.getContentItemCount());
        output.put("structureNodeCount", materializedStats.getStructureNodeCount());
        resultSummary = buildResultSummary(prepareData, versionPlan, output);

        Map<String, Object> versionSummaryParams = map();
        versionSummaryParams.put("versionId", versionId);
        versionSummaryParams.put("contentSummaryJson", toJson(resultSummary));
        generationMapper.updateDossierVersionSummary(versionSummaryParams);

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
        result.put("output", buildResponseOutputSummary(output));
        result.put("logs", selectJobLogs(jobId));
        result.put("version", versionPlan);
        result.put("instanceId", instance.get("instanceId"));
        result.put("versionId", versionId);
        result.put("precheckRunId", precheckRunId);
        return result;
    }

    private MaterializedDossierStats materializeDossierData(String instanceId, String versionId, String jobId, Map<String, Object> aircraft,
            Map<String, Object> prepareData, String userName)
    {
        MaterializedDossierStats stats = new MaterializedDossierStats();
        List<Map<String, Object>> keyNodes = castList(prepareData.get("keyNodeChain"));
        Map<String, String> nodeIdToStructureId = new LinkedHashMap<>();
        Map<String, String> nodeIdToPath = new LinkedHashMap<>();
        Map<String, String> partNumberToStructureId = new LinkedHashMap<>();
        Map<String, Set<String>> sourceColumnsCache = new LinkedHashMap<>();
        Set<String> fullContentNodeIds = fullContentNodeIds(keyNodes);
        String defaultDocumentCategoryId = defaultCategoryId();
        int tubeSourceDocumentCount = 0;

        int sortOrder = 1;
        for (Map<String, Object> node : keyNodes)
        {
            String structureNodeId = newId();
            String parentBomNodeId = text(node.get("parentId"));
            String parentId = nodeIdToStructureId.get(parentBomNodeId);
            String code = text(node.get("partNumber"));
            String parentPath = nodeIdToPath.get(parentBomNodeId);
            String nodePath = hasText(parentPath) ? parentPath + "/" + code : code;
            boolean fullContentNode = fullContentNodeIds.contains(text(node.get("nodeId")));
            List<Map<String, Object>> templateSourceRows = fullContentNode
                    ? selectConfiguredSourceRows(aircraft, prepareData, node, sourceColumnsCache)
                    : Collections.emptyList();
            List<Map<String, Object>> lifecycleRows = fullContentNode ? generationMapper.selectNodeLifecycleSnapshotRows(
                    text(aircraft.get("aircraftId")), text(node.get("nodeId")), code) : Collections.emptyList();
            int contentCount = countSnapshotRows(node, templateSourceRows, lifecycleRows);

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
            params.put("contentCount", contentCount);
            params.put("missingCount", 0);
            params.put("requiredFlag", 1);
            params.put("attrsJson", toJson(structureAttrs(node)));
            params.put("sourceTraceJson", toJson(sourceTrace("CONFIG", "aircraft_bom_node", node.get("nodeId"), code)));
            generationMapper.insertStructureNode(params);
            stats.incrementStructureNodeCount();

            insertContentItem(instanceId, versionId, text(aircraft.get("aircraftId")), structureNodeId, node, sortOrder,
                    fullContentNode);
            stats.incrementContentItemCount();
            Set<String> insertedKeys = new HashSet<>();
            insertedKeys.add(snapshotRowKey("aircraft_bom_node", node.get("nodeId"), "DOSSIER", ""));
            stats.addContentItemCount(insertTemplateSourceContentItems(instanceId, versionId,
                    text(aircraft.get("aircraftId")), structureNodeId, node, templateSourceRows, sortOrder,
                    insertedKeys));
            stats.addContentItemCount(insertLifecycleContentItems(instanceId, versionId, text(aircraft.get("aircraftId")),
                    structureNodeId, node, lifecycleRows, sortOrder, insertedKeys));
            int sourceDocumentCount = fullContentNode ? insertSourceDocumentSnapshots(instanceId, versionId,
                    text(aircraft.get("aircraftId")), structureNodeId, node, defaultDocumentCategoryId) : 0;
            stats.addDocumentCount(sourceDocumentCount);
            if ("HYD-TUBE-MLG-32A".equals(code))
            {
                tubeSourceDocumentCount += sourceDocumentCount;
            }

            nodeIdToStructureId.put(text(node.get("nodeId")), structureNodeId);
            nodeIdToPath.put(text(node.get("nodeId")), nodePath);
            partNumberToStructureId.put(code, structureNodeId);
            sortOrder++;
        }

        String tubeStructureNodeId = partNumberToStructureId.get("HYD-TUBE-MLG-32A");
        if (tubeSourceDocumentCount <= 0)
        {
            stats.addDocumentCount(insertTubeDocuments(instanceId, versionId, tubeStructureNodeId));
        }
        insertOperationLog(instanceId, versionId, jobId, userName);
        return stats;
    }

    private void insertContentItem(String instanceId, String versionId, String aircraftId, String structureNodeId,
            Map<String, Object> node, int sortOrder, boolean includeDataRollup)
    {
        boolean keyPart = Boolean.TRUE.equals(node.get("highlight"));
        Map<String, Object> dataRollup = includeDataRollup
                ? generationMapper.selectNodeDataRollup(aircraftId, text(node.get("nodeId")))
                : null;
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
        params.put("sourceTable", "aircraft_bom_node");
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
        params.put("fileStorageKey", keyPart ? "/dossier/files/2026/05/B-1234/HYD-TUBE-MLG-32A/" : null);
        params.put("sourceTraceJson", toJson(sourceTrace("CONFIG", "aircraft_bom_node", node.get("nodeId"),
                node.get("partNumber"))));
        params.put("attrsJson", toJson(attrs));
        generationMapper.insertContentItem(params);
    }

    private Set<String> fullContentNodeIds(List<Map<String, Object>> nodes)
    {
        Map<String, Map<String, Object>> byId = new LinkedHashMap<>();
        for (Map<String, Object> node : nodes)
        {
            byId.put(text(node.get("nodeId")), node);
        }
        Set<String> result = new HashSet<>();
        for (Map<String, Object> node : nodes)
        {
            if (isFullContentSeed(node))
            {
                addNodeAndAncestors(result, byId, node);
            }
        }
        if (result.isEmpty() && !nodes.isEmpty())
        {
            addNodeAndAncestors(result, byId, nodes.get(0));
        }
        return result;
    }

    private boolean isFullContentSeed(Map<String, Object> node)
    {
        String partNumber = text(node.get("partNumber"));
        return Boolean.TRUE.equals(node.get("highlight")) || "HYD-TUBE-MLG-32A".equals(partNumber);
    }

    private void addNodeAndAncestors(Set<String> result, Map<String, Map<String, Object>> byId, Map<String, Object> node)
    {
        Map<String, Object> current = node;
        while (current != null)
        {
            String nodeId = text(current.get("nodeId"));
            if (!hasText(nodeId) || result.contains(nodeId))
            {
                return;
            }
            result.add(nodeId);
            current = byId.get(text(current.get("parentId")));
        }
    }

    private List<Map<String, Object>> selectConfiguredSourceRows(Map<String, Object> aircraft,
            Map<String, Object> prepareData, Map<String, Object> node, Map<String, Set<String>> sourceColumnsCache)
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<Map<String, Object>> sources = castList(prepareData.get("sources"));
        if (sources.isEmpty())
        {
            return rows;
        }
        Map<String, Map<String, Object>> chapters = chaptersById(castList(prepareData.get("chapters")));
        for (Map<String, Object> source : sources)
        {
            Map<String, Object> chapter = chapters.get(text(source.get("chapterId")));
            String category = sourceCategory(source, chapter);
            if ("composition".equals(category))
            {
                continue;
            }
            if (!sourceAppliesToNode(source, chapter, node))
            {
                continue;
            }
            for (String queryTable : queryTablesForSource(text(source.get("sourceTable")), bomObjectLevel(node),
                    category))
            {
                if (!isAllowedSourceTable(queryTable))
                {
                    continue;
                }
                Set<String> columns = sourceTableColumns(sourceColumnsCache, queryTable);
                if (columns.isEmpty())
                {
                    continue;
                }
                List<String> clauses = sourceWhereClauses(queryTable, aircraft, node, columns);
                if (clauses.isEmpty())
                {
                    continue;
                }
                clauses.addAll(sourceFilterClauses(queryTable, parseJsonObject(source.get("filterConditionJson")),
                        columns));
                Map<String, Object> params = map();
                params.put("tableName", queryTable);
                params.put("whereSql", joinSql(clauses));
                params.put("limit", sourceRowLimit(queryTable, category));
                List<Map<String, Object>> rawRows = generationMapper.selectTemplateSourceRows(params);
                for (Map<String, Object> rawRow : rawRows)
                {
                    rows.add(toTemplateSourceContentRow(source, chapter, queryTable, rawRow, category));
                }
                if (!rawRows.isEmpty())
                {
                    break;
                }
            }
        }
        return rows;
    }

    private int insertTemplateSourceContentItems(String instanceId, String versionId, String aircraftId,
            String structureNodeId, Map<String, Object> node, List<Map<String, Object>> sourceRows, int baseSortOrder,
            Set<String> insertedKeys)
    {
        int index = 1;
        int inserted = 0;
        for (Map<String, Object> row : sourceRows)
        {
            String lifecycleStage = defaultText(row.get("lifecycleStage"), "DOSSIER");
            String sourceSystem = defaultText(row.get("sourceSystem"), "DOSSIER");
            String sourceTable = defaultText(row.get("sourceTable"), "dossier_content_item");
            String sourceRecordKey = defaultText(row.get("sourceRecordKey"), text(row.get("sourceRecordId")));
            String uniqueKey = snapshotRowKey(sourceTable, row.get("sourceRecordId"), lifecycleStage,
                    text(row.get("chapterId")));
            if (insertedKeys != null && insertedKeys.contains(uniqueKey))
            {
                continue;
            }

            Map<String, Object> params = map();
            params.put("contentItemId", newId());
            params.put("instanceId", instanceId);
            params.put("versionId", versionId);
            params.put("structureNodeId", structureNodeId);
            params.put("itemCode", "CONTENT-" + String.format("%04d", baseSortOrder) + "-SRC-"
                    + String.format("%03d", index));
            params.put("itemName", defaultText(row.get("itemName"), sourceRecordKey));
            params.put("itemType", defaultText(row.get("itemType"), "template_source"));
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
            params.put("requiredFlag", defaultNumber(row.get("requiredFlag"), 1));
            params.put("includedFlag", 1);
            params.put("completenessStatus", "complete");
            params.put("itemStatus", "active");
            params.put("sortOrder", baseSortOrder * 1000 + 100 + index);
            params.put("contentSummary", defaultText(row.get("contentSummary"), sourceRecordKey));
            params.put("fileStorageKey", row.get("fileStorageKey"));
            params.put("sourceTraceJson", row.get("sourceTraceJson"));
            params.put("attrsJson", row.get("attrsJson"));
            generationMapper.insertContentItem(params);
            if (insertedKeys != null)
            {
                insertedKeys.add(uniqueKey);
            }
            inserted++;
            index++;
        }
        return inserted;
    }

    private int countSnapshotRows(Map<String, Object> node, List<Map<String, Object>> sourceRows,
            List<Map<String, Object>> lifecycleRows)
    {
        Set<String> keys = new HashSet<>();
        keys.add(snapshotRowKey("aircraft_bom_node", node.get("nodeId"), "DOSSIER", ""));
        for (Map<String, Object> row : sourceRows)
        {
            keys.add(snapshotRowKey(row.get("sourceTable"), row.get("sourceRecordId"), row.get("lifecycleStage"),
                    text(row.get("chapterId"))));
        }
        for (Map<String, Object> row : lifecycleRows)
        {
            keys.add(snapshotRowKey(row.get("sourceTable"), row.get("sourceRecordId"), row.get("lifecycleStage"),
                    text(row.get("chapterId"))));
        }
        return keys.size();
    }

    private String snapshotRowKey(Object sourceTable, Object sourceRecordId, Object lifecycleStage, String chapterId)
    {
        return text(sourceTable).toLowerCase() + "|" + text(sourceRecordId) + "|"
                + text(lifecycleStage).toUpperCase() + "|" + text(chapterId);
    }

    private Map<String, Map<String, Object>> chaptersById(List<Map<String, Object>> chapters)
    {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (Map<String, Object> chapter : chapters)
        {
            result.put(text(chapter.get("chapterId")), chapter);
        }
        return result;
    }

    private boolean sourceAppliesToNode(Map<String, Object> source, Map<String, Object> chapter,
            Map<String, Object> node)
    {
        String objectLevel = bomObjectLevel(node);
        String applyObjectType = text(source.get("applyObjectType")).toLowerCase();
        if (!hasText(applyObjectType) && chapter != null)
        {
            Map<String, Object> attrs = parseJsonObject(chapter.get("attrsJson"));
            applyObjectType = text(attrs.get("objectLevel")).toLowerCase();
        }
        if (hasText(applyObjectType) && !"all".equals(applyObjectType) && !objectLevel.equals(applyObjectType)
                && !"bom_node".equals(applyObjectType))
        {
            return false;
        }
        String keyPartScope = text(source.get("keyPartScope")).toLowerCase();
        if ("key_only".equals(keyPartScope) && !Boolean.TRUE.equals(node.get("highlight")))
        {
            return false;
        }
        if ("non_key_only".equals(keyPartScope) && Boolean.TRUE.equals(node.get("highlight")))
        {
            return false;
        }
        return true;
    }

    private String sourceCategory(Map<String, Object> source, Map<String, Object> chapter)
    {
        String code = (text(chapter == null ? source.get("sourceCode") : chapter.get("chapterCode")) + "_"
                + text(source.get("sourceCode"))).toUpperCase();
        String displayType = "";
        if (chapter != null)
        {
            displayType = text(parseJsonObject(chapter.get("attrsJson")).get("displayType"));
        }
        if ("tree_table".equals(displayType) || code.contains("BOM") || code.contains("STRUCTURE")
                || code.contains("COMPOSITION"))
        {
            return "composition";
        }
        if ("file_list".equals(displayType) || code.contains("ATTACH") || code.contains("CERT")
                || code.contains("DOCUMENTS"))
        {
            return "documents";
        }
        if (code.contains("BASIC") || code.contains("OVERVIEW") || code.contains("PROFILE"))
        {
            return "basic";
        }
        if (code.contains("DESIGN"))
        {
            return "design";
        }
        if (code.contains("MANUFACTUR") || code.contains("TRACE") || code.contains("ROUTE"))
        {
            return "manufacturing";
        }
        if (code.contains("INSPECTION") || code.contains("TEST"))
        {
            return "inspection";
        }
        if (code.contains("SERVICE") || code.contains("USAGE") || code.contains("INSTALL"))
        {
            return "service";
        }
        if (code.contains("FAULT") || code.contains("MAINT"))
        {
            return "fault";
        }
        if (code.contains("STATUS") || code.contains("CONFIG"))
        {
            return "status";
        }
        if (code.contains("INTERFACE"))
        {
            return "interface";
        }
        return "content";
    }

    private List<String> queryTablesForSource(String sourceTable, String objectLevel, String category)
    {
        String table = canonicalSourceTable(sourceTable);
        List<String> tables = new ArrayList<>();
        if ("basic".equals(category) || "dossier_structure_node".equals(table))
        {
            String profileView = profileViewForLevel(objectLevel);
            if (hasText(profileView))
            {
                tables.add(profileView);
            }
        }
        if (!"dossier_structure_node".equals(table) && !tables.contains(table))
        {
            tables.add(table);
        }
        return tables;
    }

    private String canonicalSourceTable(Object sourceTable)
    {
        String table = text(sourceTable).toLowerCase();
        if ("document_category".equals(table))
        {
            return "t1_file_category";
        }
        if ("document_master".equals(table) || "document_entry".equals(table) || "document_archive".equals(table)
                || "technical_file".equals(table) || "part_document".equals(table)
                || "certificate_record".equals(table))
        {
            return "t1_file_relation";
        }
        return table;
    }

    private String profileViewForLevel(String objectLevel)
    {
        if ("aircraft".equals(objectLevel))
        {
            return "v_aircraft_profile_detail";
        }
        if ("system".equals(objectLevel))
        {
            return "v_system_profile_detail";
        }
        if ("subsystem".equals(objectLevel))
        {
            return "v_subsystem_profile_detail";
        }
        if ("equipment".equals(objectLevel))
        {
            return "v_equipment_profile_detail";
        }
        if ("component".equals(objectLevel))
        {
            return "v_component_profile_detail";
        }
        if ("part".equals(objectLevel))
        {
            return "v_part_profile_detail";
        }
        return "";
    }

    private boolean isAllowedSourceTable(String tableName)
    {
        return allowedSourceTables().contains(text(tableName).toLowerCase());
    }

    private Set<String> allowedSourceTables()
    {
        return new HashSet<>(Arrays.asList(
                "v_aircraft_profile_detail", "v_system_profile_detail", "v_subsystem_profile_detail",
                "v_equipment_profile_detail", "v_component_profile_detail", "v_part_profile_detail",
                "physical_aircraft", "aircraft_object_profile", "system_object_profile",
                "subsystem_object_profile", "equipment_object_master", "equipment_object_instance",
                "component_object_master", "component_object_instance", "part_master", "part_instance",
                "aircraft_bom_node", "object_lifecycle_record", "object_technical_status",
                "object_status_history", "object_interface", "life_usage_record", "work_order",
                "work_order_task", "fault_event", "fault_action", "event_flight_leg", "inspection_record",
                "inspection_measurement", "shop_order", "shop_order_task", "production_operation_record",
                "material_lot_trace", "process_route", "assembly_record", "install_removal",
                "quality_text_record", "part_hydraulic_tube_impact_model", "impact_tube_segment",
                "impact_tube_support", "impact_tube_fluid", "impact_tube_boundary_condition",
                "part_parameter_value", "t1_file_category", "t1_file_asset", "t1_file_relation"));
    }

    private Set<String> sourceTableColumns(Map<String, Set<String>> cache, String tableName)
    {
        String key = text(tableName).toLowerCase();
        if (cache.containsKey(key))
        {
            return cache.get(key);
        }
        Set<String> columns = new HashSet<>();
        for (String column : generationMapper.selectSourceTableColumns(key))
        {
            columns.add(text(column).toLowerCase());
        }
        cache.put(key, columns);
        return columns;
    }

    private List<String> sourceWhereClauses(String tableName, Map<String, Object> aircraft, Map<String, Object> node,
            Set<String> columns)
    {
        List<String> clauses = new ArrayList<>();
        String table = text(tableName).toLowerCase();
        String aircraftId = text(aircraft.get("aircraftId"));
        String nodeId = text(node.get("nodeId"));
        String partInstanceId = text(node.get("partInstanceId"));
        String partNumber = text(node.get("partNumber"));
        boolean scoped = false;

        if ("t1_file_relation".equals(table))
        {
            if (columns.contains("relation_status"))
            {
                clauses.add("t.relation_status != 'deleted'");
            }
            if (columns.contains("aircraft_id") && hasText(aircraftId))
            {
                clauses.add("t.aircraft_id = " + quote(aircraftId));
            }
            if ("aircraft".equals(bomObjectLevel(node)))
            {
                scoped = columns.contains("aircraft_id") && hasText(aircraftId);
            }
            else
            {
                List<String> anchors = new ArrayList<>();
                if (columns.contains("bom_node_id") && hasText(nodeId))
                {
                    anchors.add("t.bom_node_id = " + quote(nodeId));
                }
                if (columns.contains("part_instance_id") && hasText(partInstanceId))
                {
                    anchors.add("t.part_instance_id = " + quote(partInstanceId));
                }
                if (columns.contains("part_number") && hasText(partNumber))
                {
                    anchors.add("t.part_number = " + quote(partNumber));
                }
                if (!anchors.isEmpty())
                {
                    clauses.add("(" + String.join(" or ", anchors) + ")");
                    scoped = true;
                }
            }
        }
        else if ("t1_file_asset".equals(table))
        {
            clauses.add("1 = 0");
            scoped = true;
        }
        else if ("t1_file_category".equals(table))
        {
            scoped = true;
            clauses.add("1 = 1");
        }
        else if ("physical_aircraft".equals(table) && columns.contains("id"))
        {
            clauses.add("t.id = " + quote(aircraftId));
            scoped = true;
        }
        else if (("v_aircraft_profile_detail".equals(table) || "aircraft_object_profile".equals(table))
                && columns.contains("aircraft_id"))
        {
            clauses.add("t.aircraft_id = " + quote(aircraftId));
            scoped = true;
        }
        else if ("object_interface".equals(table) && columns.contains("aircraft_id")
                && columns.contains("source_bom_node_id") && columns.contains("target_bom_node_id"))
        {
            clauses.add("t.aircraft_id = " + quote(aircraftId));
            clauses.add("(t.source_bom_node_id = " + quote(nodeId) + " or t.target_bom_node_id = "
                    + quote(nodeId) + ")");
            scoped = true;
        }
        else if ((profileViewForLevel(bomObjectLevel(node)).equals(table) || hasBomNodeSourceScope(table))
                && columns.contains("aircraft_id") && columns.contains("bom_node_id"))
        {
            clauses.add("t.aircraft_id = " + quote(aircraftId));
            clauses.add("t.bom_node_id = " + quote(nodeId));
            scoped = true;
        }
        else if (hasAircraftOnlySourceScope(table) && columns.contains("aircraft_id"))
        {
            clauses.add("t.aircraft_id = " + quote(aircraftId));
            scoped = true;
        }
        else if ("part_instance".equals(table) && hasText(partInstanceId) && columns.contains("id"))
        {
            clauses.add("t.id = " + quote(partInstanceId));
            scoped = true;
        }
        else if (hasPartInstanceSourceScope(table) && hasText(partInstanceId) && columns.contains("part_instance_id"))
        {
            clauses.add("t.part_instance_id = " + quote(partInstanceId));
            scoped = true;
        }
        else if (hasPartNumberSourceScope(table) && hasText(partNumber) && columns.contains(partNumberField(table)))
        {
            clauses.add("t." + partNumberField(table) + " = " + quote(partNumber));
            scoped = true;
        }
        return scoped ? clauses : Collections.emptyList();
    }

    private boolean hasAircraftOnlySourceScope(String tableName)
    {
        return new HashSet<>(Arrays.asList("aircraft_bom_node", "event_flight_leg")).contains(tableName);
    }

    private boolean hasBomNodeSourceScope(String tableName)
    {
        return new HashSet<>(Arrays.asList("v_system_profile_detail", "v_subsystem_profile_detail",
                "v_equipment_profile_detail", "v_component_profile_detail", "v_part_profile_detail",
                "system_object_profile", "subsystem_object_profile", "equipment_object_instance",
                "component_object_instance", "object_lifecycle_record", "object_technical_status",
                "object_status_history", "object_interface", "life_usage_record", "work_order",
                "work_order_task", "fault_event", "fault_action", "inspection_record", "inspection_measurement",
                "shop_order", "shop_order_task", "production_operation_record", "material_lot_trace",
                "assembly_record", "install_removal", "quality_text_record", "document_entry")).contains(tableName);
    }

    private boolean hasPartInstanceSourceScope(String tableName)
    {
        return new HashSet<>(Arrays.asList("part_instance", "life_usage_record", "work_order", "work_order_task",
                "fault_event", "fault_action", "inspection_record", "inspection_measurement", "shop_order",
                "shop_order_task", "production_operation_record", "material_lot_trace", "assembly_record",
                "install_removal", "quality_text_record", "document_entry")).contains(tableName);
    }

    private boolean hasPartNumberSourceScope(String tableName)
    {
        return new HashSet<>(Arrays.asList("part_master", "equipment_object_master", "component_object_master",
                "part_document", "part_parameter_value", "part_hydraulic_tube_impact_model",
                "impact_tube_segment", "impact_tube_support", "impact_tube_fluid",
                "impact_tube_boundary_condition", "process_route", "t1_file_relation")).contains(tableName);
    }

    private String partNumberField(String tableName)
    {
        if ("impact_tube_segment".equals(tableName) || "impact_tube_support".equals(tableName)
                || "impact_tube_fluid".equals(tableName) || "impact_tube_boundary_condition".equals(tableName))
        {
            return "model_part_number";
        }
        return "part_number";
    }

    private List<String> sourceFilterClauses(String tableName, Map<String, Object> filter, Set<String> columns)
    {
        List<String> clauses = new ArrayList<>();
        for (Map.Entry<String, Object> entry : filter.entrySet())
        {
            String field = canonicalSourceField(tableName, text(entry.getKey()));
            Object value = entry.getValue();
            if (!isSafeSqlField(field) || !columns.contains(field.toLowerCase())
                    || !isAllowedSourceField(tableName, field) || value instanceof Map
                    || value instanceof List)
            {
                continue;
            }
            String textValue = text(value);
            if (!hasText(textValue) || textValue.contains("${"))
            {
                continue;
            }
            clauses.add("upper(cast(t." + field + " as char)) = " + quote(textValue.toUpperCase()));
        }
        return clauses;
    }

    private boolean isAllowedSourceField(String tableName, String field)
    {
        Set<String> fields = new HashSet<>(Arrays.asList("id", "aircraft_id", "bom_node_id", "part_instance_id",
                "part_number", "model_part_number", "tail_number", "serial_number", "batch_number", "lot_number",
                "is_current", "default_flag", "status", "result", "node_type", "object_level"));
        fields.addAll(Arrays.asList("source_table", "source_record_id", "dossier_version_id", "structure_node_id",
                "route_code", "wo_number", "inspection_type", "document_status", "relation_status", "file_id",
                "target_code", "relation_type"));
        return fields.contains(field);
    }

    private String canonicalSourceField(String tableName, String field)
    {
        if ("t1_file_relation".equals(text(tableName).toLowerCase()))
        {
            if ("file_storage_key".equals(field))
            {
                return "file_id";
            }
            if ("doc_no".equals(field) || "doc_number".equals(field))
            {
                return "target_code";
            }
            if ("doc_type".equals(field) || "business_type".equals(field))
            {
                return "relation_type";
            }
            if ("document_status".equals(field))
            {
                return "relation_status";
            }
        }
        return field;
    }

    private boolean isSafeSqlField(String field)
    {
        return text(field).matches("[A-Za-z0-9_]+");
    }

    private int sourceRowLimit(String tableName, String category)
    {
        if (text(tableName).startsWith("v_") || "basic".equals(category))
        {
            return 1;
        }
        if ("aircraft_bom_node".equals(tableName))
        {
            return 200;
        }
        if ("event_flight_leg".equals(tableName))
        {
            return 30;
        }
        return 50;
    }

    private Map<String, Object> toTemplateSourceContentRow(Map<String, Object> source, Map<String, Object> chapter,
            String queryTable, Map<String, Object> rawRow, String category)
    {
        Map<String, Object> attrs = normalizeSourceAttrs(rawRow);
        attrs.put("chapterId", source.get("chapterId"));
        attrs.put("chapterCode", chapter == null ? null : chapter.get("chapterCode"));
        attrs.put("chapterName", chapter == null ? null : chapter.get("chapterName"));
        attrs.put("templateSourceId", source.get("sourceId"));
        attrs.put("templateSourceCode", source.get("sourceCode"));
        attrs.put("templateSourceName", source.get("sourceName"));
        attrs.put("templateSourceTable", source.get("sourceTable"));
        attrs.put("actualSourceTable", queryTable);
        attrs.put("category", category);

        String sourceTable = canonicalSourceTable(defaultText(source.get("sourceTable"), queryTable));
        Object sourceRecordId = firstValue(rawRow, "id", "aircraft_id", "bom_node_id", "system_id",
                "subsystem_id", "equipment_instance_id", "component_instance_id", "part_instance_id",
                "object_profile_id");
        String sourceRecordKey = defaultText(firstValue(rawRow, "part_number", "tail_number", "system_code",
                "subsystem_code", "equipment_code", "component_code", "serial_number", "doc_number",
                "wo_number", "route_code", "id"), text(sourceRecordId));
        String lifecycleStage = defaultText(source.get("lifecycleStage"), stageForCategory(category)).toUpperCase();
        String itemType = defaultText(source.get("sourceRecordType"),
                queryTable.startsWith("v_") ? "profile_detail" : "template_source");

        Map<String, Object> row = map();
        row.put("chapterId", source.get("chapterId"));
        row.put("sourceSystem", defaultText(source.get("sourceSystem"), "DOSSIER"));
        row.put("sourceTable", sourceTable);
        row.put("sourceRecordId", sourceRecordId);
        row.put("sourceRecordKey", sourceRecordKey);
        row.put("itemType", itemType);
        row.put("lifecycleStage", lifecycleStage);
        row.put("itemName", defaultText(source.get("sourceName"), sourceRecordKey));
        row.put("contentSummary", sourceSummary(rawRow, sourceRecordKey));
        row.put("fileStorageKey", firstValue(rawRow, "file_storage_key", "storage_key", "storage_path",
                "access_url"));
        row.put("requiredFlag", source.get("requiredFlag"));
        row.put("sourceTraceJson", toJson(sourceTrace(defaultText(source.get("sourceSystem"), "DOSSIER"),
                sourceTable, sourceRecordId, sourceRecordKey)));
        row.put("attrsJson", toJson(attrs));
        return row;
    }

    private Map<String, Object> normalizeSourceAttrs(Map<String, Object> rawRow)
    {
        Map<String, Object> attrs = map();
        for (Map.Entry<String, Object> entry : rawRow.entrySet())
        {
            String key = text(entry.getKey());
            Object value = entry.getValue();
            attrs.put(key, value);
            String camelKey = toCamelCase(key);
            if (!camelKey.equals(key))
            {
                attrs.put(camelKey, value);
            }
        }
        return attrs;
    }

    private String toCamelCase(String key)
    {
        StringBuilder result = new StringBuilder();
        boolean upperNext = false;
        for (char ch : text(key).toCharArray())
        {
            if (ch == '_')
            {
                upperNext = true;
            }
            else if (upperNext)
            {
                result.append(Character.toUpperCase(ch));
                upperNext = false;
            }
            else
            {
                result.append(ch);
            }
        }
        return result.toString();
    }

    private Object firstValue(Map<String, Object> row, String... keys)
    {
        for (String key : keys)
        {
            if (row.containsKey(key) && hasText(text(row.get(key))))
            {
                return row.get(key);
            }
        }
        return null;
    }

    private String sourceSummary(Map<String, Object> row, String fallback)
    {
        List<String> values = new ArrayList<>();
        for (String key : Arrays.asList("part_name", "system_name", "subsystem_name", "equipment_name",
                "component_name", "tail_number", "serial_number", "manufacturer", "material",
                "technical_status", "quality_status", "operational_status", "result", "status"))
        {
            Object value = row.get(key);
            if (hasText(text(value)))
            {
                values.add(text(value));
            }
            if (values.size() >= 4)
            {
                break;
            }
        }
        return values.isEmpty() ? fallback : join(values);
    }

    private String stageForCategory(String category)
    {
        if ("design".equals(category) || "interface".equals(category))
        {
            return "DESIGN";
        }
        if ("manufacturing".equals(category))
        {
            return "MANUFACTURING";
        }
        if ("inspection".equals(category))
        {
            return "INSPECTION";
        }
        if ("service".equals(category))
        {
            return "SERVICE";
        }
        if ("fault".equals(category))
        {
            return "FAULT";
        }
        if ("status".equals(category))
        {
            return "TECHNICAL_STATUS";
        }
        if ("documents".equals(category))
        {
            return "DOCUMENT";
        }
        return "DOSSIER";
    }

    private int insertLifecycleContentItems(String instanceId, String versionId, String aircraftId,
            String structureNodeId, Map<String, Object> node, List<Map<String, Object>> lifecycleRows, int baseSortOrder,
            Set<String> insertedKeys)
    {
        int index = 1;
        int inserted = 0;
        for (Map<String, Object> row : lifecycleRows)
        {
            String lifecycleStage = text(row.get("lifecycleStage"));
            String sourceSystem = defaultText(row.get("sourceSystem"), "DOSSIER");
            String sourceTable = defaultText(row.get("sourceTable"), "dossier_content_item");
            String sourceRecordKey = defaultText(row.get("sourceRecordKey"), text(row.get("sourceRecordId")));
            String itemType = defaultText(row.get("itemType"), "lifecycle_record");
            String uniqueKey = snapshotRowKey(sourceTable, row.get("sourceRecordId"), lifecycleStage, text(row.get("chapterId")));
            if (insertedKeys != null && insertedKeys.contains(uniqueKey))
            {
                continue;
            }

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
            if (insertedKeys != null)
            {
                insertedKeys.add(uniqueKey);
            }
            inserted++;
            index++;
        }
        return inserted;
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

    private int insertTubeDocuments(String instanceId, String versionId, String tubeStructureNodeId)
    {
        if (!hasText(tubeStructureNodeId))
        {
            return 0;
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
            params.put("fileAssetId", newId());
            params.put("documentEntryId", newId());
            params.put("instanceId", instanceId);
            params.put("versionId", versionId);
            params.put("categoryId", documentCategoryFor(title, designCategoryId, testCategoryId, workCardCategoryId));
            params.put("structureNodeId", tubeStructureNodeId);
            params.put("docNo", fileName.replaceAll("\\.[^.]+$", ""));
            params.put("title", title);
            params.put("fileStorageKey", "/dossier/files/2026/05/B-1234/HYD-TUBE-MLG-32A/" + fileName);
            params.put("sourceTraceJson", toJson(sourceTrace(attachment.get("sourceSystem"), "t1_file_relation", null,
                    "HYD-TUBE-MLG-32A/" + fileName)));
            params.put("sourceSystem", attachment.get("sourceSystem"));
            params.put("sourceTable", "t1_file_relation");
            params.put("sourceRecordId", null);
            params.put("sourceRecordKey", "HYD-TUBE-MLG-32A/" + fileName);
            params.put("documentStatus", "active");
            params.put("completenessStatus", "complete");
            params.put("requiredFlag", 1);
            params.put("includedFlag", 1);
            params.put("attrsJson", toJson(documentAttrs(title, sortOrder)));
            generationMapper.insertDocumentFileAsset(params);
            generationMapper.insertDocumentFileRelation(params);
            sortOrder++;
        }
        return sortOrder - 1;
    }

    private int insertSourceDocumentSnapshots(String instanceId, String versionId, String aircraftId,
            String structureNodeId, Map<String, Object> node, String defaultCategoryId)
    {
        List<Map<String, Object>> rows = generationMapper.selectNodeDocumentSourceRows(aircraftId,
                text(node.get("nodeId")), text(node.get("partNumber")), text(node.get("partInstanceId")));
        if (rows == null || rows.isEmpty())
        {
            return 0;
        }
        Set<String> insertedKeys = new HashSet<>();
        int inserted = 0;
        int sortOrder = 1;
        for (Map<String, Object> row : rows)
        {
            String fileAssetId = text(row.get("fileAssetId"));
            String sourceRecordId = defaultText(row.get("sourceRecordId"), text(row.get("sourceRelationId")));
            String uniqueKey = fileAssetId + "|" + sourceRecordId;
            if (!hasText(fileAssetId) || insertedKeys.contains(uniqueKey))
            {
                continue;
            }
            insertedKeys.add(uniqueKey);

            String docNo = defaultText(row.get("docNo"), defaultText(row.get("sourceRecordKey"), sourceRecordId));
            String title = defaultText(row.get("title"), docNo);
            Map<String, Object> params = map();
            params.put("fileAssetId", fileAssetId);
            params.put("documentEntryId", newId());
            params.put("instanceId", instanceId);
            params.put("versionId", versionId);
            params.put("categoryId", defaultText(row.get("categoryId"), defaultCategoryId));
            params.put("structureNodeId", structureNodeId);
            params.put("aircraftId", aircraftId);
            params.put("bomNodeId", node.get("nodeId"));
            params.put("partNumber", node.get("partNumber"));
            params.put("partInstanceId", node.get("partInstanceId"));
            params.put("objectLevel", bomObjectLevel(node));
            params.put("lifecycleStage", "DOCUMENT");
            params.put("businessDomain", "DOSSIER");
            params.put("docNo", docNo);
            params.put("title", title);
            params.put("fileStorageKey", row.get("fileStorageKey"));
            params.put("sourceSystem", defaultText(row.get("sourceSystem"), "DOC"));
            params.put("sourceTable", "t1_file_relation");
            params.put("sourceRecordId", sourceRecordId);
            params.put("sourceRecordKey", defaultText(row.get("sourceRecordKey"), docNo));
            params.put("sourceTraceJson", hasText(text(row.get("sourceTraceJson"))) ? row.get("sourceTraceJson")
                    : toJson(sourceTrace(params.get("sourceSystem"), "t1_file_relation", sourceRecordId,
                            params.get("sourceRecordKey"))));
            params.put("documentStatus", defaultText(row.get("documentStatus"), "active"));
            params.put("completenessStatus", defaultText(row.get("completenessStatus"), "complete"));
            params.put("requiredFlag", defaultNumber(row.get("requiredFlag"), 0));
            params.put("includedFlag", defaultNumber(row.get("includedFlag"), 1));
            params.put("sortOrder", sortOrder);
            params.put("attrsJson", sourceDocumentAttrs(row, node));
            inserted += generationMapper.insertDocumentFileRelation(params);
            sortOrder++;
        }
        return inserted;
    }

    private String sourceDocumentAttrs(Map<String, Object> row, Map<String, Object> node)
    {
        Map<String, Object> attrs = parseJsonObject(row.get("attrsJson"));
        attrs.put("sourceRelationId", defaultText(row.get("sourceRelationId"), text(row.get("sourceRecordId"))));
        attrs.put("sourceRelationType", row.get("sourceRelationType"));
        attrs.put("sourceTargetType", row.get("sourceTargetType"));
        attrs.put("sourceTargetId", row.get("sourceTargetId"));
        attrs.put("sourceTargetCode", row.get("sourceTargetCode"));
        attrs.put("sourcePartNumber", row.get("sourcePartNumber"));
        attrs.put("bomNodeId", node.get("nodeId"));
        attrs.put("partNumber", node.get("partNumber"));
        attrs.put("partInstanceId", node.get("partInstanceId"));
        attrs.put("objectLevel", bomObjectLevel(node));
        attrs.put("snapshotSource", "t1_file_relation");
        return toJson(attrs);
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
        params.put("businessSubjectType", "generation_job");
        params.put("businessSubjectId", jobId);
        params.put("operatorId", userName);
        params.put("operatorName", userName);
        params.put("sourceIp", "127.0.0.1");
        params.put("detailJson", toJson(sourceTrace("DOSSIER", "generation_job", jobId, "B-1234")));
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
            output = buildResponseOutputSummary(jobOutput == null ? map() : castMap(jobOutput.get("output")));
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
        output.put("fileCount", toInt(generationMapper.countDocumentEntries(versionId), toInt(output.get("fileCount"), 0)));

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
        List<Map<String, Object>> chapterSources = buildChapterSources(sources, chapters);
        List<Map<String, Object>> checks = buildChecks(aircraft, template, metrics, keyNodeChain, chapterSources, rules);
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

    private Map<String, Object> selectRequiredTemplate(String templateId)
    {
        Map<String, Object> template = hasText(templateId)
                ? generationMapper.selectTemplateById(templateId)
                : generationMapper.selectActiveTemplate();
        if (template == null)
        {
            throw new ServiceException(hasText(templateId)
                    ? "selected aircraft dossier template does not exist"
                    : "active aircraft dossier template does not exist");
        }
        if (!"aircraft".equals(text(template.get("applicableObjectType"))))
        {
            throw new ServiceException("selected template is not an aircraft dossier template");
        }
        String status = text(template.get("status"));
        if (!"active".equals(status) && !"draft".equals(status))
        {
            throw new ServiceException("selected template is not available for generation");
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
            return repairInstanceIdentityIfNeeded(instance, aircraft, template);
        }

        Map<String, Object> params = map();
        params.put("instanceId", newId());
        params.put("templateId", templateId);
        params.put("aircraftId", aircraftId);
        params.put("instanceCode", buildInstanceCode(aircraft));
        params.put("instanceName", buildInstanceName(aircraft, template));
        params.put("instanceLabel", buildInstanceLabel(aircraft, template));
        params.put("instanceOptionsJson", toJson(buildInstanceOptions(aircraft, template)));
        params.put("createdBy", currentUser());
        params.put("updatedBy", currentUser());
        generationMapper.insertDossierInstance(params);
        return generationMapper.selectDossierInstance(aircraftId, templateId);
    }

    private Map<String, Object> repairInstanceIdentityIfNeeded(Map<String, Object> instance,
            Map<String, Object> aircraft, Map<String, Object> template)
    {
        if (hasText(text(instance.get("instanceCode"))) && hasText(text(instance.get("instanceName"))))
        {
            return instance;
        }

        Map<String, Object> params = map();
        params.put("instanceId", instance.get("instanceId"));
        params.put("instanceCode", buildInstanceCode(aircraft));
        params.put("instanceName", buildInstanceName(aircraft, template));
        params.put("instanceLabel", buildInstanceLabel(aircraft, template));
        params.put("updatedBy", currentUser());
        generationMapper.updateDossierInstanceIdentity(params);
        return generationMapper.selectDossierInstance(text(aircraft.get("aircraftId")), text(template.get("templateId")));
    }

    private String buildInstanceCode(Map<String, Object> aircraft)
    {
        String tailNumber = safeCodePart(defaultText(aircraft.get("tailNumber"), text(aircraft.get("aircraftId"))));
        return "DOS-" + tailNumber + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private String buildInstanceName(Map<String, Object> aircraft, Map<String, Object> template)
    {
        return text(aircraft.get("tailNumber")) + " " + text(template.get("templateName"));
    }

    private String buildInstanceLabel(Map<String, Object> aircraft, Map<String, Object> template)
    {
        return buildInstanceName(aircraft, template);
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
        params.put("runStatus", inspectionRunStatus(summary));
        params.put("scopeDescription", "aircraft + active template + template configured data sources");
        params.put("ruleSetVersion", "R-DOSSIER-GENERATION-1.0");
        params.put("checkedObjectCount", summary.get("checkedObjectCount"));
        params.put("issueCount", toInt(summary.get("warningCount"), 0) + toInt(summary.get("blockingCount"), 0));
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

    private List<Map<String, Object>> buildChapterSources(List<Map<String, Object>> sources,
            List<Map<String, Object>> chapters)
    {
        List<Map<String, Object>> result = new ArrayList<>();
        if (sources == null)
        {
            return result;
        }
        Map<String, Map<String, Object>> hierarchyInfo = buildChapterHierarchyInfo(chapters);
        for (Map<String, Object> source : sources)
        {
            Map<String, Object> item = map();
            item.putAll(source);
            Map<String, Object> chapterInfo = hierarchyInfo.get(text(source.get("chapterId")));
            if (chapterInfo != null)
            {
                item.putAll(chapterInfo);
                item.put("chapterName", chapterInfo.get("chapterDisplayName"));
            }
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
        Collections.sort(result, (left, right) -> {
            int chapterCompare = defaultText(left.get("chapterHierarchyOrder"), "9999")
                    .compareTo(defaultText(right.get("chapterHierarchyOrder"), "9999"));
            if (chapterCompare != 0)
            {
                return chapterCompare;
            }
            int sourceCompare = Integer.compare(toInt(left.get("sortOrder"), 0), toInt(right.get("sortOrder"), 0));
            if (sourceCompare != 0)
            {
                return sourceCompare;
            }
            return text(left.get("sourceId")).compareTo(text(right.get("sourceId")));
        });
        return result;
    }

    private Map<String, Map<String, Object>> buildChapterHierarchyInfo(List<Map<String, Object>> chapters)
    {
        Map<String, Map<String, Object>> info = new LinkedHashMap<>();
        if (chapters == null)
        {
            return info;
        }
        Map<String, Map<String, Object>> nodeMap = new LinkedHashMap<>();
        for (Map<String, Object> chapter : chapters)
        {
            String chapterId = text(chapter.get("chapterId"));
            if (hasText(chapterId))
            {
                nodeMap.put(chapterId, chapter);
            }
        }

        Map<String, List<Map<String, Object>>> childrenMap = new LinkedHashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Map<String, Object> chapter : chapters)
        {
            String parentId = text(chapter.get("parentId"));
            if (hasText(parentId) && nodeMap.containsKey(parentId))
            {
                List<Map<String, Object>> children = childrenMap.get(parentId);
                if (children == null)
                {
                    children = new ArrayList<>();
                    childrenMap.put(parentId, children);
                }
                children.add(chapter);
            }
            else
            {
                roots.add(chapter);
            }
        }

        sortChapters(roots);
        for (List<Map<String, Object>> children : childrenMap.values())
        {
            sortChapters(children);
        }
        fillChapterHierarchyInfo(roots, childrenMap, info, "", "", 1);
        return info;
    }

    private void fillChapterHierarchyInfo(List<Map<String, Object>> chapters,
            Map<String, List<Map<String, Object>>> childrenMap, Map<String, Map<String, Object>> info,
            String parentLabel, String parentOrder, int depth)
    {
        for (int i = 0; i < chapters.size(); i++)
        {
            Map<String, Object> chapter = chapters.get(i);
            String chapterId = text(chapter.get("chapterId"));
            if (!hasText(chapterId))
            {
                continue;
            }
            String chapterName = defaultText(chapter.get("chapterName"), chapterId);
            String currentLabel = hasText(parentLabel) ? parentLabel + " / " + chapterName : chapterName;
            String currentOrder = hasText(parentOrder) ? parentOrder + "." + orderSegment(i) : orderSegment(i);

            Map<String, Object> item = map();
            item.put("chapterDisplayName", currentLabel);
            item.put("chapterHierarchyOrder", currentOrder);
            item.put("chapterDepth", depth);
            item.put("chapterLevel", defaultText(chapter.get("chapterLevel"), String.valueOf(depth)));
            info.put(chapterId, item);

            List<Map<String, Object>> children = childrenMap.get(chapterId);
            if (children != null && !children.isEmpty())
            {
                fillChapterHierarchyInfo(children, childrenMap, info, currentLabel, currentOrder, depth + 1);
            }
        }
    }

    private void sortChapters(List<Map<String, Object>> chapters)
    {
        Collections.sort(chapters, (left, right) -> {
            int sortCompare = Integer.compare(toInt(left.get("sortOrder"), 0), toInt(right.get("sortOrder"), 0));
            if (sortCompare != 0)
            {
                return sortCompare;
            }
            int codeCompare = text(left.get("chapterCode")).compareTo(text(right.get("chapterCode")));
            if (codeCompare != 0)
            {
                return codeCompare;
            }
            return text(left.get("chapterName")).compareTo(text(right.get("chapterName")));
        });
    }

    private String orderSegment(int index)
    {
        return String.format("%04d", index + 1);
    }

    private int estimateRecordCount(String tableName, String sourceName)
    {
        if ("physical_aircraft".equals(tableName))
        {
            return 1;
        }
        if ("aircraft_bom_node".equals(tableName))
        {
            return sourceName.contains("液压弯管") ? 1 : 15;
        }
        if ("part_instance".equals(tableName))
        {
            return 1;
        }
        if ("impact_tube_segment".equals(tableName))
        {
            return 6;
        }
        if ("part_parameter_value".equals(tableName))
        {
            return 12;
        }
        if ("part_hydraulic_tube_impact_model".equals(tableName))
        {
            return 1;
        }
        if ("material_lot_trace".equals(tableName))
        {
            return 2;
        }
        if ("process_route".equals(tableName))
        {
            return 1;
        }
        if ("production_operation_record".equals(tableName))
        {
            return 18;
        }
        if ("assembly_record".equals(tableName))
        {
            return sourceName.contains("液压弯管") ? 3 : 8;
        }
        if ("inspection_record".equals(tableName))
        {
            return sourceName.contains("液压弯管") ? 4 : 9;
        }
        if ("shop_order".equals(tableName))
        {
            return sourceName.contains("液压弯管") ? 1 : 6;
        }
        if ("shop_order_task".equals(tableName))
        {
            return 18;
        }
        if ("install_removal".equals(tableName))
        {
            return sourceName.contains("液压弯管") ? 2 : 6;
        }
        if ("fault_event".equals(tableName))
        {
            return sourceName.contains("液压弯管") ? 1 : 6;
        }
        if ("work_order".equals(tableName))
        {
            return sourceName.contains("液压弯管") ? 1 : 6;
        }
        if ("t1_file_relation".equals(tableName))
        {
            return sourceName.contains("液压弯管") ? 6 : (sourceName.contains("附件") ? 36 : 14);
        }
        if ("dossier_structure_node".equals(tableName))
        {
            return 50;
        }
        return 3;
    }

    private int estimateIssueCount(String tableName, String sourceName)
    {
        if ("t1_file_relation".equals(tableName) && !sourceName.contains("液压弯管"))
        {
            return 2;
        }
        if ("work_order".equals(tableName) && !sourceName.contains("液压弯管"))
        {
            return 1;
        }
        if ("shop_order_task".equals(tableName))
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
        if ("aircraft_bom_node".equals(tableName))
        {
            return "B-1234 / SYS-29 / SUBSYS-HYD-MLG";
        }
        return "B-1234";
    }

    private List<Map<String, Object>> buildChecks(Map<String, Object> aircraft, Map<String, Object> template,
            Map<String, Object> metrics, List<Map<String, Object>> keyNodeChain, List<Map<String, Object>> chapterSources,
            List<Map<String, Object>> rules)
    {
        List<Map<String, Object>> checks = new ArrayList<>();
        checks.add(check("aircraft", "飞机对象", "pass", "已锁定单台飞机 " + text(aircraft.get("tailNumber")), 1, 0));
        checks.add(check("template", "模板有效性", "pass",
                text(template.get("templateName")) + " " + text(template.get("templateVersion")) + " 可用",
                toInt(metrics.get("chapterCount"), 0), 0));
        checks.add(check("template-source", "模板数据来源", "pass",
                "生成范围完全由模板数据来源决定，生成时不再选择数据范围", toInt(metrics.get("sourceCount"), 0), 0));
        checks.addAll(buildRuleDrivenChecks(aircraft, keyNodeChain, chapterSources, rules));
        checks.add(check("version", "版本策略", "pass",
                "同模板版本下再次生成为小版本，模板版本变化时生成大版本", 1, 0));
        return checks;
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

    private List<Map<String, Object>> buildRuleDrivenChecks(Map<String, Object> aircraft,
            List<Map<String, Object>> keyNodeChain, List<Map<String, Object>> chapterSources,
            List<Map<String, Object>> rules)
    {
        List<Map<String, Object>> checks = new ArrayList<>();
        RuleContext context = new RuleContext(text(aircraft.get("aircraftId")), keyNodeChain, chapterSources);
        if (rules == null || rules.isEmpty())
        {
            checks.add(check("template-rules", "模板检查规则", "warning", "当前模板未配置启用的检查规则", 0, 1));
            return checks;
        }
        for (Map<String, Object> rule : rules)
        {
            checks.add(evaluateTemplateRule(rule, context));
        }
        return checks;
    }

    private Map<String, Object> evaluateTemplateRule(Map<String, Object> rule, RuleContext context)
    {
        String ruleCode = defaultText(rule.get("ruleCode"), text(rule.get("ruleId")));
        String ruleName = defaultText(rule.get("ruleName"), ruleCode);
        try
        {
            RuleEvalResult result = evaluateRule(rule, context);
            Map<String, Object> item = check(ruleCode, ruleName, result.status, result.message, result.checkedCount,
                    result.issueCount);
            enrichRuleCheck(item, rule);
            return item;
        }
        catch (Exception e)
        {
            Map<String, Object> item = check(ruleCode, ruleName, failureStatus(rule), "规则执行失败：" + e.getMessage(), 0, 1);
            enrichRuleCheck(item, rule);
            return item;
        }
    }

    private void enrichRuleCheck(Map<String, Object> item, Map<String, Object> rule)
    {
        item.put("ruleId", rule.get("ruleId"));
        item.put("ruleType", rule.get("ruleType"));
        item.put("severity", rule.get("severity"));
        item.put("chapterName", rule.get("chapterName"));
        item.put("targetTable", rule.get("targetTable"));
        item.put("targetField", rule.get("targetField"));
        item.put("remediationHint", rule.get("remediationHint"));
    }

    private RuleEvalResult evaluateRule(Map<String, Object> rule, RuleContext context)
    {
        Map<String, Object> expression = parseJsonObject(rule.get("ruleExpressionJson"));
        String ruleType = defaultText(rule.get("ruleType"), "business");
        if ("business".equals(ruleType))
        {
            RuleEvalResult result = evaluateBusinessRule(rule, expression, context);
            if (result != null)
            {
                return result;
            }
        }
        if ("required".equals(ruleType))
        {
            return evaluateRequiredRule(rule, expression, context);
        }
        if ("enum".equals(ruleType))
        {
            return evaluateEnumRule(rule, expression, context);
        }
        if ("expression".equals(ruleType))
        {
            return evaluateExpressionRule(rule, expression, context);
        }
        return evaluateRelationRule(rule, expression, context, text(rule.get("targetTable")));
    }

    private RuleEvalResult evaluateRequiredRule(Map<String, Object> rule, Map<String, Object> expression,
            RuleContext context)
    {
        String tableName = canonicalRuleTable(rule.get("targetTable"));
        List<String> fields = stringList(expression.get("fields"));
        if (fields.isEmpty() && hasText(text(rule.get("targetField"))))
        {
            fields.add(canonicalRuleField(tableName, text(rule.get("targetField"))));
        }
        int total = countBusinessRows(tableName, baseClauses(tableName, context));
        if (total <= 0)
        {
            return failed(rule, 0, "未查询到可检查的业务数据：" + tableName);
        }
        List<String> missing = new ArrayList<>();
        for (String field : fields)
        {
            String checkField = canonicalRuleField(tableName, field);
            if (!isAllowedField(tableName, checkField))
            {
                missing.add(field + "(未纳入白名单)");
                continue;
            }
            List<String> clauses = baseClauses(tableName, context);
            clauses.add(emptyFieldClause(checkField));
            if (countBusinessRows(tableName, clauses) > 0)
            {
                missing.add(field);
            }
        }
        if (missing.isEmpty())
        {
            return passed(total, "已按模板规则检查必填字段：" + join(fields));
        }
        return failed(rule, total, defaultText(rule.get("errorMessage"), "字段缺失：" + join(missing)));
    }

    private RuleEvalResult evaluateRelationRule(Map<String, Object> rule, Map<String, Object> expression,
            RuleContext context, String tableName)
    {
        tableName = canonicalRuleTable(tableName);
        List<String> clauses = baseClauses(tableName, context);
        if (asInt(expression.get("minChildren"), 0) > 0 && "aircraft_bom_node".equals(tableName))
        {
            clauses.add("t.parent_id is not null");
        }
        int total = countBusinessRows(tableName, clauses);
        if (total > 0)
        {
            return passed(total, "已查询到 " + total + " 条关联业务数据");
        }
        return failed(rule, 0, defaultText(rule.get("errorMessage"), "未查询到关联业务数据：" + tableName));
    }

    private RuleEvalResult evaluateEnumRule(Map<String, Object> rule, Map<String, Object> expression,
            RuleContext context)
    {
        String tableName = canonicalRuleTable(rule.get("targetTable"));
        String field = canonicalRuleField(tableName, text(rule.get("targetField")));
        List<String> accepted = stringList(expression.get("accepted"));
        if (accepted.isEmpty())
        {
            accepted = stringList(expression.get("values"));
        }
        if (!isAllowedField(tableName, field) || accepted.isEmpty())
        {
            return evaluateRelationRule(rule, expression, context, tableName);
        }
        int total = countBusinessRows(tableName, baseClauses(tableName, context));
        List<String> clauses = baseClauses(tableName, context);
        clauses.add(inClause(field, accepted));
        int matched = countBusinessRows(tableName, clauses);
        if (total > 0 && matched == total)
        {
            return passed(total, "字段 " + field + " 均在允许范围：" + join(accepted));
        }
        return failed(rule, total, defaultText(rule.get("errorMessage"), "字段 " + field + " 存在枚举值异常"));
    }

    private RuleEvalResult evaluateExpressionRule(Map<String, Object> rule, Map<String, Object> expression,
            RuleContext context)
    {
        String tableName = canonicalRuleTable(rule.get("targetTable"));
        int total = countBusinessRows(tableName, baseClauses(tableName, context));
        List<String> clauses = baseClauses(tableName, context);
        String field = canonicalRuleField(tableName, text(rule.get("targetField")));
        if (hasText(field) && hasText(text(expression.get("requiredStatus"))) && isAllowedField(tableName, field))
        {
            clauses.add(equalsClause(field, text(expression.get("requiredStatus"))));
        }
        if (hasText(field) && !stringList(expression.get("accepted")).isEmpty() && isAllowedField(tableName, field))
        {
            clauses.add(inClause(field, stringList(expression.get("accepted"))));
        }
        clauses.addAll(expressionClauses(tableName, text(rule.get("ruleExpression"))));
        int matched = countBusinessRows(tableName, clauses);
        if (total > 0 && matched > 0)
        {
            return passed(matched, "表达式检查通过：" + defaultText(rule.get("ruleExpression"), tableName));
        }
        return failed(rule, total, defaultText(rule.get("errorMessage"), "表达式检查未通过"));
    }

    private RuleEvalResult evaluateBusinessRule(Map<String, Object> rule, Map<String, Object> expression,
            RuleContext context)
    {
        if (!stringList(expression.get("requiredDocs")).isEmpty())
        {
            return evaluateRequiredDocuments(rule, expression, context);
        }
        if (!stringList(expression.get("requiredSteps")).isEmpty())
        {
            return evaluateRequiredSteps(rule, expression, context);
        }
        if (!stringList(expression.get("requiredTables")).isEmpty())
        {
            return evaluateRequiredTables(rule, expression, context);
        }
        if ("assembly_record".equals(text(rule.get("targetTable"))) && (expression.containsKey("torqueNm")
                || expression.containsKey("result") || expression.containsKey("postInstallLeakCheckMpa")))
        {
            return evaluateAssemblyParams(rule, expression, context);
        }
        return null;
    }

    private RuleEvalResult evaluateRequiredDocuments(Map<String, Object> rule, Map<String, Object> expression,
            RuleContext context)
    {
        List<String> docs = stringList(expression.get("requiredDocs"));
        List<String> missing = new ArrayList<>();
        int matched = 0;
        for (String doc : docs)
        {
            List<String> clauses = baseClauses("t1_file_relation", context);
            clauses.add("t.relation_status != 'deleted'");
            clauses.add("t.relation_type in ('PART_DOCUMENT','TECHNICAL_FILE','DRAWING','MODEL','ATTACHMENT','CERTIFICATE','DOSSIER_ATTACHMENT')");
            clauses.add("(json_unquote(json_extract(t.business_meta_json, '$.docNumber')) like " + quoteLike(doc)
                    + " or json_unquote(json_extract(t.business_meta_json, '$.docType')) = " + quote(doc)
                    + " or t.source_record_key like " + quoteLike(doc)
                    + " or t.target_code like " + quoteLike(doc) + ")");
            if (countBusinessRows("t1_file_relation", clauses) > 0)
            {
                matched++;
            }
            else
            {
                missing.add(doc);
            }
        }
        if (missing.isEmpty())
        {
            return passed(matched, "必需文档齐套：" + join(docs));
        }
        return failed(rule, matched, defaultText(rule.get("errorMessage"), "缺少文档：" + join(missing)));
    }

    private RuleEvalResult evaluateRequiredSteps(Map<String, Object> rule, Map<String, Object> expression,
            RuleContext context)
    {
        List<String> steps = stringList(expression.get("requiredSteps"));
        String requiredStatus = defaultText(expression.get("requiredStatus"), "COMPLETED").toUpperCase();
        List<String> missing = new ArrayList<>();
        int matched = 0;
        for (String step : steps)
        {
            List<String> clauses = baseClauses("shop_order_task", context);
            clauses.add("t.step_code = " + quote(step));
            clauses.add("upper(t.status) = " + quote(requiredStatus));
            if (countBusinessRows("shop_order_task", clauses) > 0)
            {
                matched++;
            }
            else
            {
                missing.add(step);
            }
        }
        if (missing.isEmpty())
        {
            return passed(matched, "关键工序均已完成：" + join(steps));
        }
        return failed(rule, matched, defaultText(rule.get("errorMessage"), "未完成工序：" + join(missing)));
    }

    private RuleEvalResult evaluateRequiredTables(Map<String, Object> rule, Map<String, Object> expression,
            RuleContext context)
    {
        List<String> tables = stringList(expression.get("requiredTables"));
        List<String> missing = new ArrayList<>();
        int matched = 0;
        for (String table : tables)
        {
            int count = countBusinessRows(table, baseClauses(table, context));
            int minCount = minRequiredCount(table, expression);
            if (count >= minCount)
            {
                matched += count;
            }
            else
            {
                missing.add(table + "(" + count + "/" + minCount + ")");
            }
        }
        if (missing.isEmpty())
        {
            return passed(matched, "必需业务表均有数据：" + join(tables));
        }
        return failed(rule, matched, defaultText(rule.get("errorMessage"), "缺少业务数据：" + join(missing)));
    }

    private RuleEvalResult evaluateAssemblyParams(Map<String, Object> rule, Map<String, Object> expression,
            RuleContext context)
    {
        int total = countBusinessRows("assembly_record", baseClauses("assembly_record", context));
        List<String> clauses = baseClauses("assembly_record", context);
        if (expression.containsKey("torqueNm"))
        {
            clauses.add("cast(json_unquote(json_extract(t.assembly_params, '$.torque_n_m')) as decimal(10,3)) = "
                    + asDecimal(expression.get("torqueNm")));
        }
        if (expression.containsKey("postInstallLeakCheckMpa"))
        {
            clauses.add("cast(json_unquote(json_extract(t.assembly_params, '$.post_install_leak_check_mpa')) as decimal(10,3)) >= "
                    + asDecimal(expression.get("postInstallLeakCheckMpa")));
        }
        if (expression.containsKey("result"))
        {
            clauses.add("upper(json_unquote(json_extract(t.assembly_params, '$.leak_check_result'))) = "
                    + quote(text(expression.get("result")).toUpperCase()));
        }
        int matched = countBusinessRows("assembly_record", clauses);
        if (matched > 0)
        {
            return passed(matched, "装配参数满足模板规则");
        }
        return failed(rule, total, defaultText(rule.get("errorMessage"), "装配参数未满足模板规则"));
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
            else if ("warning".equals(status) || "info".equals(status))
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
        summary.put("issueCount", warningCount + blockingCount);
        summary.put("result", blockingCount == 0 ? "passed" : "blocked");
        summary.put("checkedAt", DISPLAY_TIME.format(new Date()));
        return summary;
    }

    private int countBusinessRows(String tableName, List<String> clauses)
    {
        tableName = canonicalRuleTable(tableName);
        if (!isAllowedTable(tableName))
        {
            throw new ServiceException("unsupported rule target table: " + tableName);
        }
        Map<String, Object> params = map();
        params.put("tableName", tableName);
        params.put("whereSql", clauses == null || clauses.isEmpty() ? "1 = 1" : joinSql(clauses));
        Integer count = generationMapper.countRuleRows(params);
        return count == null ? 0 : count;
    }

    private List<String> baseClauses(String tableName, RuleContext context)
    {
        tableName = canonicalRuleTable(tableName);
        List<String> clauses = new ArrayList<>();
        clauses.add("1 = 1");
        if (!isAllowedTable(tableName))
        {
            throw new ServiceException("unsupported rule target table: " + tableName);
        }
        if ("physical_aircraft".equals(tableName))
        {
            clauses.add("t.id = " + quote(context.aircraftId));
        }
        else if ("part_instance".equals(tableName))
        {
            if (!context.partInstanceIds.isEmpty())
            {
                clauses.add("t.id in (" + quoteList(context.partInstanceIds) + ")");
            }
            else
            {
                clauses.add("t.current_aircraft_id = " + quote(context.aircraftId));
            }
        }
        else if (hasAircraftScope(tableName))
        {
            clauses.add("t.aircraft_id = " + quote(context.aircraftId));
        }
        else if (hasPartInstanceScope(tableName) && !context.partInstanceIds.isEmpty())
        {
            clauses.add("t.part_instance_id in (" + quoteList(context.partInstanceIds) + ")");
        }
        else if (hasBomNodeScope(tableName) && !context.bomNodeIds.isEmpty())
        {
            clauses.add("t.bom_node_id in (" + quoteList(context.bomNodeIds) + ")");
        }
        else if (hasPartNumberScope(tableName) && !context.partNumbers.isEmpty())
        {
            clauses.add(partNumberScopeClause(tableName, context.partNumbers));
        }
        if ("aircraft_bom_node".equals(tableName))
        {
            clauses.add("t.is_active = 1");
        }
        if ("t1_file_category".equals(tableName))
        {
            clauses.add("t.is_active = 1");
        }
        return clauses;
    }

    private List<String> expressionClauses(String tableName, String expression)
    {
        List<String> clauses = new ArrayList<>();
        if (!hasText(expression))
        {
            return clauses;
        }
        String[] parts = expression.split("(?i)\\s+and\\s+");
        for (String part : parts)
        {
            String clause = simpleExpressionClause(tableName, part.trim());
            if (hasText(clause))
            {
                clauses.add(clause);
            }
        }
        return clauses;
    }

    private String simpleExpressionClause(String tableName, String expression)
    {
        tableName = canonicalRuleTable(tableName);
        String lower = expression.toLowerCase();
        if (lower.endsWith(" is not null"))
        {
            String field = canonicalRuleField(tableName, expression.substring(0, lower.indexOf(" is not null")).trim());
            return isAllowedField(tableName, field) ? filledFieldClause(field) : "";
        }
        if (lower.contains(" in (") && expression.endsWith(")"))
        {
            String field = canonicalRuleField(tableName, expression.substring(0, lower.indexOf(" in (")).trim());
            String valueText = expression.substring(lower.indexOf(" in (") + 5, expression.length() - 1);
            List<String> values = new ArrayList<>();
            for (String item : valueText.split(","))
            {
                values.add(stripQuote(item.trim()));
            }
            return isAllowedField(tableName, field) ? inClause(field, values) : "";
        }
        if (expression.contains(">="))
        {
            String[] pair = expression.split(">=", 2);
            String field = canonicalRuleField(tableName, pair[0].trim());
            return isAllowedField(tableName, field) ? "t." + field + " >= " + asDecimal(pair[1].trim()) : "";
        }
        if (expression.contains("="))
        {
            String[] pair = expression.split("=", 2);
            String left = canonicalRuleField(tableName, pair[0].trim());
            String right = stripQuote(pair[1].trim());
            String rightField = canonicalRuleField(tableName, right);
            if (isAllowedField(tableName, left) && isAllowedField(tableName, rightField))
            {
                return "t." + left + " = t." + rightField;
            }
            return isAllowedField(tableName, left) ? equalsClause(left, right) : "";
        }
        return "";
    }

    private String emptyFieldClause(String field)
    {
        return "(t." + field + " is null or trim(cast(t." + field + " as char)) = '')";
    }

    private String filledFieldClause(String field)
    {
        return "(t." + field + " is not null and trim(cast(t." + field + " as char)) <> '')";
    }

    private String equalsClause(String field, String value)
    {
        return "upper(cast(t." + field + " as char)) = " + quote(value.toUpperCase());
    }

    private String inClause(String field, List<String> values)
    {
        List<String> normalized = new ArrayList<>();
        for (String value : values)
        {
            normalized.add(value.toUpperCase());
        }
        return "upper(cast(t." + field + " as char)) in (" + quoteList(normalized) + ")";
    }

    private String partNumberScopeClause(String tableName, List<String> partNumbers)
    {
        if ("impact_tube_segment".equals(tableName) || "impact_tube_support".equals(tableName)
                || "impact_tube_fluid".equals(tableName) || "impact_tube_boundary_condition".equals(tableName))
        {
            return "t.model_part_number in (" + quoteList(partNumbers) + ")";
        }
        return "t.part_number in (" + quoteList(partNumbers) + ")";
    }

    private int minRequiredCount(String tableName, Map<String, Object> expression)
    {
        if ("impact_tube_segment".equals(tableName))
        {
            return Math.max(1, asInt(expression.get("minSegments"), 1));
        }
        if ("impact_tube_support".equals(tableName))
        {
            return Math.max(1, asInt(expression.get("minSupports"), 1));
        }
        return 1;
    }

    private RuleEvalResult passed(int checkedCount, String message)
    {
        return new RuleEvalResult("pass", message, checkedCount, 0);
    }

    private RuleEvalResult failed(Map<String, Object> rule, int checkedCount, String message)
    {
        return new RuleEvalResult(failureStatus(rule), message, checkedCount, 1);
    }

    private String failureStatus(Map<String, Object> rule)
    {
        String severity = text(rule.get("severity"));
        if ("blocker".equals(severity) || "error".equals(severity))
        {
            return "error";
        }
        if ("info".equals(severity))
        {
            return "info";
        }
        return "warning";
    }

    private String precheckStatus(Map<String, Object> summary)
    {
        return toInt(summary.get("blockingCount"), 0) > 0 ? "blocked" : "passed";
    }

    private String inspectionRunStatus(Map<String, Object> summary)
    {
        return toInt(summary.get("blockingCount"), 0) > 0 ? "failed" : "passed";
    }

    private void assertPrecheckPassed(Map<String, Object> prepareData)
    {
        Map<String, Object> summary = castMap(prepareData.get("checkSummary"));
        if (toInt(summary.get("blockingCount"), 0) > 0)
        {
            throw new ServiceException("生成前检查存在阻断项，请处理后再开始生成");
        }
    }

    private boolean isAllowedTable(String tableName)
    {
        return allowedRuleTables().contains(canonicalRuleTable(tableName));
    }

    private boolean isAllowedField(String tableName, String field)
    {
        return allowedRuleFields(canonicalRuleTable(tableName)).contains(canonicalRuleField(tableName, field));
    }

    private String canonicalRuleTable(Object tableName)
    {
        return canonicalSourceTable(tableName);
    }

    private String canonicalRuleField(String tableName, String field)
    {
        return canonicalSourceField(canonicalRuleTable(tableName), text(field));
    }

    private Set<String> allowedRuleTables()
    {
        return new HashSet<>(Arrays.asList("physical_aircraft", "aircraft_bom_node", "part_instance", "part_master",
                "t1_file_category", "t1_file_relation", "shop_order", "shop_order_task", "production_operation_record",
                "material_lot_trace", "inspection_record", "inspection_measurement", "assembly_record",
                "install_removal", "fault_event", "work_order", "quality_text_record",
                "part_hydraulic_tube_impact_model", "impact_tube_segment", "impact_tube_support",
                "impact_tube_fluid", "impact_tube_boundary_condition", "dossier_structure_node"));
    }

    private Set<String> allowedRuleFields(String tableName)
    {
        Set<String> fields = new HashSet<>(Arrays.asList("id", "aircraft_id", "bom_node_id", "part_instance_id",
                "part_number", "model_part_number", "status", "result", "created_at", "updated_at"));
        fields.addAll(Arrays.asList("tail_number", "aircraft_type", "msn", "parent_id", "is_active", "node_type",
                "part_name", "serial_number", "batch_number", "file_id", "relation_type", "target_type", "target_id",
                "target_code", "relation_status", "completeness_status", "business_meta_json", "is_current",
                "current_aircraft_id", "current_node_id",
                "wo_number", "wo_status", "close_date", "open_date", "step_code",
                "task_code", "quantity_produced", "quantity_ordered", "inspection_std_doc", "inspection_type",
                "inspection_date", "measurement_values", "assembly_params", "lot_number", "mill_cert_number",
                "fault_code", "record_type", "source_table", "source_record_id", "file_id", "target_code",
                "relation_type", "category_code", "category_name", "category_type", "scope_type", "sort_order"));
        return fields;
    }

    private boolean hasAircraftScope(String tableName)
    {
        return new HashSet<>(Arrays.asList("aircraft_bom_node", "shop_order", "shop_order_task",
                "production_operation_record", "material_lot_trace", "inspection_record", "inspection_measurement",
                "assembly_record", "install_removal", "fault_event", "work_order", "quality_text_record",
                "t1_file_relation")).contains(tableName);
    }

    private boolean hasBomNodeScope(String tableName)
    {
        return new HashSet<>(Arrays.asList("object_lifecycle_record", "inspection_record", "inspection_measurement",
                "assembly_record", "fault_event", "work_order", "quality_text_record", "t1_file_relation")).contains(tableName);
    }

    private boolean hasPartInstanceScope(String tableName)
    {
        return new HashSet<>(Arrays.asList("shop_order", "shop_order_task",
                "production_operation_record", "material_lot_trace", "inspection_record", "inspection_measurement",
                "assembly_record", "install_removal", "fault_event", "work_order", "quality_text_record",
                "t1_file_relation")).contains(tableName);
    }

    private boolean hasPartNumberScope(String tableName)
    {
        return new HashSet<>(Arrays.asList("part_master", "t1_file_relation", "part_hydraulic_tube_impact_model",
                "impact_tube_segment", "impact_tube_support", "impact_tube_fluid",
                "impact_tube_boundary_condition")).contains(tableName);
    }

    private Map<String, Object> parseJsonObject(Object value)
    {
        if (value instanceof Map)
        {
            return castMap(value);
        }
        if (!hasText(text(value)))
        {
            return map();
        }
        try
        {
            return OBJECT_MAPPER.readValue(text(value), new TypeReference<Map<String, Object>>()
            {
            });
        }
        catch (Exception e)
        {
            return map();
        }
    }

    private List<String> stringList(Object value)
    {
        List<String> result = new ArrayList<>();
        if (value instanceof List)
        {
            for (Object item : (List<?>) value)
            {
                if (hasText(text(item)))
                {
                    result.add(text(item));
                }
            }
        }
        else if (hasText(text(value)))
        {
            result.add(text(value));
        }
        return result;
    }

    private int asInt(Object value, int defaultValue)
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

    private String asDecimal(Object value)
    {
        String normalized = text(value).replaceAll("[^0-9.\\-]", "");
        return hasText(normalized) ? normalized : "0";
    }

    private String quote(String value)
    {
        return "'" + text(value).replace("'", "''") + "'";
    }

    private String quoteLike(String value)
    {
        return quote(text(value).replace("%", "\\%").replace("_", "\\_") + "%");
    }

    private String quoteList(List<String> values)
    {
        List<String> quoted = new ArrayList<>();
        for (String value : values)
        {
            quoted.add(quote(value));
        }
        return quoted.isEmpty() ? "''" : String.join(",", quoted);
    }

    private String stripQuote(String value)
    {
        String text = text(value).trim();
        if ((text.startsWith("'") && text.endsWith("'")) || (text.startsWith("\"") && text.endsWith("\"")))
        {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }

    private String join(List<String> values)
    {
        return values == null || values.isEmpty() ? "-" : String.join("、", values);
    }

    private String joinSql(List<String> clauses)
    {
        return String.join(" and ", clauses);
    }

    private static class RuleEvalResult
    {
        private final String status;
        private final String message;
        private final int checkedCount;
        private final int issueCount;

        private RuleEvalResult(String status, String message, int checkedCount, int issueCount)
        {
            this.status = status;
            this.message = message;
            this.checkedCount = checkedCount;
            this.issueCount = issueCount;
        }
    }

    private class RuleContext
    {
        private final String aircraftId;
        private final List<String> bomNodeIds = new ArrayList<>();
        private final List<String> partInstanceIds = new ArrayList<>();
        private final List<String> partNumbers = new ArrayList<>();

        private RuleContext(String aircraftId, List<Map<String, Object>> keyNodeChain,
                List<Map<String, Object>> chapterSources)
        {
            this.aircraftId = aircraftId;
            if (keyNodeChain != null)
            {
                for (Map<String, Object> node : keyNodeChain)
                {
                    addIfPresent(bomNodeIds, node.get("nodeId"));
                    addIfPresent(partInstanceIds, node.get("partInstanceId"));
                    addIfPresent(partNumbers, node.get("partNumber"));
                }
            }
            if (chapterSources != null)
            {
                for (Map<String, Object> source : chapterSources)
                {
                    Map<String, Object> filter = parseJsonObject(source.get("filterConditionJson"));
                    addIfPresent(partNumbers, filter.get("part_number"));
                    addIfPresent(partNumbers, filter.get("model_part_number"));
                }
            }
        }

        private void addIfPresent(List<String> target, Object value)
        {
            String text = text(value);
            if (hasText(text) && !target.contains(text))
            {
                target.add(text);
            }
        }
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
        preview.put("outputDirectory", "/dossier/output/" + text(aircraft.get("tailNumber")) + "/");
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
        snapshot.put("chapters", prepareData.get("chapters"));
        snapshot.put("sources", prepareData.get("sources"));
        snapshot.put("chapterSources", prepareData.get("chapterSources"));
        snapshot.put("rules", prepareData.get("rules"));
        snapshot.put("params", prepareData.get("params"));
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
        output.put("outputPath", "/dossier/output/2026/05/" + tailNumber + "/" + versionLabel + "/");
        output.put("exportFormat", "PDF+ZIP");
        output.put("fileCount", 0);
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

    private Map<String, Object> buildResponseOutputSummary(Map<String, Object> output)
    {
        Map<String, Object> summary = map();
        summary.put("jobCode", output.get("jobCode"));
        summary.put("fileName", output.get("fileName"));
        summary.put("packageName", output.get("packageName"));
        summary.put("outputPath", output.get("outputPath"));
        summary.put("exportFormat", output.get("exportFormat"));
        summary.put("fileCount", output.get("fileCount"));
        summary.put("sourceRecordCount", output.get("sourceRecordCount"));
        summary.put("keyNodeRecordCount", output.get("keyNodeRecordCount"));
        summary.put("tubeRecordCount", output.get("tubeRecordCount"));
        summary.put("generatedAt", output.get("generatedAt"));
        summary.put("sections", output.get("sections"));
        return summary;
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
        sections.add(section("01", "飞机基本信息"));
        sections.add(section("02", "构型与BOM"));
        sections.add(section("03", "设计数据"));
        sections.add(section("04", "制造与检验"));
        sections.add(section("05", "服役与故障"));
        sections.add(section("06", "技术状态"));
        sections.add(section("07", "液压弯管专题"));
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
        result.put("fileCount", output.get("fileCount"));
        result.put("contentItemCount", output.get("contentItemCount"));
        result.put("structureNodeCount", output.get("structureNodeCount"));
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

    private Map<String, Object> section(String code, String name)
    {
        Map<String, Object> item = map();
        item.put("code", code);
        item.put("name", name);
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
            map.remove(sourceKey);
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
            return OBJECT_MAPPER.writeValueAsString(jsonSafeValue(value));
        }
        catch (Exception e)
        {
            throw new ServiceException("failed to serialize dossier generation data: " + e.getMessage())
                    .setDetailMessage(e.toString());
        }
    }

    private Object jsonSafeValue(Object value)
    {
        if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean)
        {
            return value;
        }
        if (value instanceof Date)
        {
            return DISPLAY_TIME.format((Date) value);
        }
        if (value instanceof java.time.temporal.TemporalAccessor)
        {
            return value.toString();
        }
        if (value instanceof byte[])
        {
            return java.util.Base64.getEncoder().encodeToString((byte[]) value);
        }
        if (value instanceof Map)
        {
            Map<String, Object> result = map();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet())
            {
                result.put(text(entry.getKey()), jsonSafeValue(entry.getValue()));
            }
            return result;
        }
        if (value instanceof Iterable)
        {
            List<Object> result = new ArrayList<>();
            for (Object item : (Iterable<?>) value)
            {
                result.add(jsonSafeValue(item));
            }
            return result;
        }
        if (value.getClass().isArray())
        {
            List<Object> result = new ArrayList<>();
            int length = java.lang.reflect.Array.getLength(value);
            for (int i = 0; i < length; i++)
            {
                result.add(jsonSafeValue(java.lang.reflect.Array.get(value, i)));
            }
            return result;
        }
        if (value instanceof Enum)
        {
            return ((Enum<?>) value).name();
        }
        return text(value);
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

    private String safeCodePart(String value)
    {
        String normalized = text(value).replaceAll("[^A-Za-z0-9]", "");
        return hasText(normalized) ? normalized : "UNKNOWN";
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

    private static class MaterializedDossierStats
    {
        private int structureNodeCount;

        private int contentItemCount;

        private int documentCount;

        private void incrementStructureNodeCount()
        {
            structureNodeCount++;
        }

        private int getStructureNodeCount()
        {
            return structureNodeCount;
        }

        private void incrementContentItemCount()
        {
            contentItemCount++;
        }

        private void addContentItemCount(int count)
        {
            contentItemCount += count;
        }

        private int getContentItemCount()
        {
            return contentItemCount;
        }

        private void addDocumentCount(int count)
        {
            documentCount += count;
        }

        private int getDocumentCount()
        {
            return documentCount;
        }
    }
}
