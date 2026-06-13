package com.ruoyi.project1.dossier.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.project1.dossier.mapper.DossierDetailMapper;
import com.ruoyi.project1.dossier.service.IDossierDetailService;

@Service
public class DossierDetailServiceImpl implements IDossierDetailService
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final SimpleDateFormat DISPLAY_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private DossierDetailMapper detailMapper;

    @Override
    public Map<String, Object> selectCurrentDetail(String aircraftId, String instanceId, String versionId)
    {
        Map<String, Object> instance = detailMapper.selectLatestInstance(blankToNull(aircraftId), blankToNull(instanceId));
        if (instance == null)
        {
            throw new ServiceException("没有找到可查看的卷宗实例");
        }
        Map<String, Object> version = detailMapper.selectVersion(text(instance.get("instanceId")), blankToNull(versionId));
        if (version == null)
        {
            throw new ServiceException("没有找到可查看的卷宗版本");
        }
        Map<String, Object> rootNode = detailMapper.selectRootStructureBomNode(text(version.get("versionId")));
        if (rootNode == null)
        {
            throw new ServiceException("当前卷宗版本没有结构快照，请重新生成卷宗");
        }
        return buildDetailData(instance, version, rootNode);
    }

    @Override
    public Map<String, Object> selectNodeDetail(String instanceId, String versionId, String bomNodeId)
    {
        Map<String, Object> instance = detailMapper.selectLatestInstance(null, blankToNull(instanceId));
        if (instance == null)
        {
            throw new ServiceException("没有找到卷宗实例");
        }
        Map<String, Object> version = detailMapper.selectVersion(text(instance.get("instanceId")), blankToNull(versionId));
        if (version == null)
        {
            throw new ServiceException("没有找到卷宗版本");
        }
        Map<String, Object> node = detailMapper.selectStructureBomNode(text(version.get("versionId")), bomNodeId);
        if (node == null)
        {
            throw new ServiceException("当前卷宗版本没有该BOM节点快照");
        }
        return buildNodePayload(instance, version, node);
    }

    @Override
    public List<Map<String, Object>> selectBomChildren(String aircraftId, String parentId, String instanceId,
            String versionId)
    {
        String resolvedVersionId = resolveVersionId(aircraftId, instanceId, versionId);
        if (hasText(resolvedVersionId))
        {
            return normalizeBomNodes(detailMapper.selectStructureBomChildren(resolvedVersionId, blankToNull(parentId)));
        }
        return normalizeBomNodes(detailMapper.selectBomChildren(aircraftId, blankToNull(parentId)));
    }

    @Override
    public List<Map<String, Object>> searchBomNodes(String aircraftId, String keyword, String instanceId,
            String versionId)
    {
        if (!hasText(keyword))
        {
            return Collections.emptyList();
        }
        String resolvedVersionId = resolveVersionId(aircraftId, instanceId, versionId);
        if (hasText(resolvedVersionId))
        {
            return normalizeBomNodes(detailMapper.searchStructureBomNodes(resolvedVersionId, keyword));
        }
        return normalizeBomNodes(detailMapper.searchBomNodes(aircraftId, keyword));
    }

    @Override
    public List<Map<String, Object>> selectBomPath(String nodeId, String versionId)
    {
        if (hasText(versionId))
        {
            return normalizeBomNodes(detailMapper.selectStructureBomPath(versionId, nodeId));
        }
        return normalizeBomNodes(detailMapper.selectBomPath(nodeId));
    }

    @Override
    public Map<String, Object> selectPreviewFile(String documentEntryId)
    {
        if (!hasText(documentEntryId))
        {
            return null;
        }
        return detailMapper.selectPreviewFile(documentEntryId);
    }

    private Map<String, Object> buildDetailData(Map<String, Object> instance, Map<String, Object> version,
            Map<String, Object> selectedNode)
    {
        Map<String, Object> templateConfig = resolveTemplateConfig(version);
        String versionId = text(version.get("versionId"));

        Map<String, Object> result = map();
        result.put("context", buildContext(instance, version, templateConfig));
        result.put("metrics", buildMetrics(version, templateConfig));
        result.put("versions", normalizeRows(detailMapper.selectVersionList(text(instance.get("instanceId")))));
        result.put("filePackages", buildFilePackages(instance, version));
        result.put("structureNodes", normalizeStructureNodes(detailMapper.selectStructureNodes(versionId)));
        result.put("allDocuments", normalizeDocuments(detailMapper.selectAllDocuments(versionId)));
        result.put("dataSources", normalizeRows(detailMapper.selectDataSources(versionId)));
        result.put("operationLogs", normalizeRows(detailMapper.selectOperationLogs(text(instance.get("instanceId")),
                versionId)));
        result.putAll(buildNodePayload(instance, version, selectedNode));
        return result;
    }

    private Map<String, Object> buildNodePayload(Map<String, Object> instance, Map<String, Object> version,
            Map<String, Object> node)
    {
        Map<String, Object> bomNode = normalizeBomNode(node);
        String versionId = text(version.get("versionId"));
        String bomNodeId = text(bomNode.get("nodeId"));
        Map<String, Object> templateConfig = resolveTemplateConfig(version);

        Map<String, Object> structureNode = detailMapper.selectStructureNodeByBom(versionId, bomNodeId);
        if (structureNode != null)
        {
            parseJsonField(structureNode, "attrsJson", "attrs");
            parseJsonField(structureNode, "sourceTraceJson", "sourceTrace");
        }

        List<Map<String, Object>> contentItems = normalizeContentItems(detailMapper.selectContentItems(versionId, bomNodeId));
        String structureNodeId = structureNode == null ? null : text(structureNode.get("structureNodeId"));
        List<Map<String, Object>> documents = normalizeDocuments(detailMapper.selectDocuments(versionId, structureNodeId));
        List<Map<String, Object>> directoryRows = buildDirectoryRows(templateConfig, text(bomNode.get("objectLevel")),
                contentItems, documents);

        Map<String, Object> result = map();
        result.put("currentNode", bomNode);
        result.put("bomPath", selectBomPath(bomNodeId, versionId));
        result.put("bomChildren", selectBomChildren(text(instance.get("aircraftId")), bomNodeId,
                text(instance.get("instanceId")), versionId));
        result.put("directory", buildDirectory(text(bomNode.get("objectLevel")), directoryRows));
        result.put("structureNode", structureNode);
        result.put("contentItems", contentItems);
        result.put("documents", documents);
        result.put("detail", buildNodeDetail(bomNode, structureNode, contentItems, documents, directoryRows));
        return result;
    }

    private Map<String, Object> buildContext(Map<String, Object> instance, Map<String, Object> version,
            Map<String, Object> templateConfig)
    {
        Map<String, Object> context = map();
        context.putAll(instance);
        context.putAll(version);
        context.put("createdAt", displayTime(version.get("createdAt")));
        context.put("publishedAt", displayTime(version.get("publishedAt")));
        context.put("templateDisplayConfig", templateConfig);
        parseJsonField(context, "contentSummaryJson", "contentSummary");
        parseJsonField(context, "outputJson", "output");
        parseJsonField(context, "generationParamsJson", "generationParams");
        parseJsonField(context, "templateSnapshotJson", "templateSnapshot");
        return context;
    }

    private Map<String, Object> buildMetrics(Map<String, Object> version, Map<String, Object> templateConfig)
    {
        Map<String, Object> summary = parseJsonObject(version.get("contentSummaryJson"));
        Map<String, Object> checkSummary = castMap(summary.get("checkSummary"));
        Map<String, Object> output = parseJsonObject(version.get("outputJson"));
        Map<String, Object> bomMetrics = detailMapper.selectStructureBomMetrics(text(version.get("versionId")));
        if (bomMetrics == null)
        {
            bomMetrics = map();
        }
        int documentCount = toInt(detailMapper.countDocuments(text(version.get("versionId"))), 0);

        Map<String, Object> metrics = map();
        metrics.put("fileCount", documentCount);
        metrics.put("sourceRecordCount", defaultNumber(summary.get("sourceRecordCount"), output.get("sourceRecordCount"), 0));
        metrics.put("warningCount", defaultNumber(checkSummary.get("warningCount"), 0));
        metrics.put("blockingCount", defaultNumber(checkSummary.get("blockingCount"), 0));
        metrics.put("directoryCount", castList(templateConfig.get("chapters")).size());
        metrics.put("bomNodeCount", defaultNumber(bomMetrics.get("totalCount"), 0));
        metrics.put("systemCount", defaultNumber(bomMetrics.get("systemCount"), 0));
        metrics.put("subsystemCount", defaultNumber(bomMetrics.get("subsystemCount"), 0));
        metrics.put("equipmentCount", defaultNumber(bomMetrics.get("equipmentCount"), 0));
        metrics.put("partCount", defaultNumber(bomMetrics.get("partCount"), 0));
        metrics.put("completenessRate", completenessRate(metrics));
        return metrics;
    }

    private List<Map<String, Object>> buildFilePackages(Map<String, Object> instance, Map<String, Object> version)
    {
        Map<String, Object> output = parseJsonObject(version.get("outputJson"));
        String versionLabel = defaultText(version.get("versionLabel"), "current");
        String tailNumber = defaultText(instance.get("tailNumber"), "dossier");
        String fileName = defaultText(output.get("fileName"), tailNumber + "_" + versionLabel + ".pdf");
        String packageName = defaultText(output.get("packageName"), tailNumber + "_" + versionLabel + ".zip");
        int documentCount = toInt(detailMapper.countDocuments(text(version.get("versionId"))), 0);

        List<Map<String, Object>> files = new ArrayList<>();
        files.add(file("PDF", fileName, defaultText(output.get("fileSize"), "-"),
                "卷宗正文"));
        files.add(file("ZIP", packageName, defaultText(output.get("packageSize"), "-"),
                documentCount + " 文件"));
        files.add(file("JSON", "dossier_structure_node_snapshot.json", "-", "BOM节点快照"));
        return files;
    }

    private Map<String, Object> file(String type, String name, Object size, Object summary)
    {
        Map<String, Object> item = map();
        item.put("type", type);
        item.put("name", name);
        item.put("size", size);
        item.put("summary", summary);
        return item;
    }

    private Map<String, Object> resolveTemplateConfig(Map<String, Object> version)
    {
        Map<String, Object> snapshot = parseJsonObject(version.get("templateSnapshotJson"));
        List<Map<String, Object>> chapters = normalizeTemplateChapters(castList(snapshot.get("chapters")));
        List<Map<String, Object>> sources = normalizeRows(castList(snapshot.get("sources")));
        if (sources.isEmpty())
        {
            sources = normalizeRows(castList(snapshot.get("chapterSources")));
        }

        String templateId = text(version.get("templateId"));
        if (chapters.isEmpty() && hasText(templateId))
        {
            chapters = normalizeTemplateChapters(detailMapper.selectTemplateChapters(templateId));
        }
        if (sources.isEmpty() && hasText(templateId))
        {
            sources = normalizeRows(detailMapper.selectTemplateDataSources(templateId));
        }

        Map<String, Object> config = map();
        config.put("template", snapshot.get("template"));
        config.put("chapters", chapters);
        config.put("sources", sources);
        config.put("rules", normalizeRows(castList(snapshot.get("rules"))));
        config.put("params", normalizeRows(castList(snapshot.get("params"))));
        return config;
    }

    private List<Map<String, Object>> normalizeTemplateChapters(List<Map<String, Object>> rows)
    {
        List<Map<String, Object>> result = normalizeRows(rows);
        for (Map<String, Object> item : result)
        {
            parseJsonField(item, "attrsJson", "attrs");
            if (!hasText(item.get("chapterId")) && hasText(item.get("id")))
            {
                item.put("chapterId", item.get("id"));
            }
        }
        return result;
    }

    private Map<String, Object> buildDirectory(String objectLevel, List<Map<String, Object>> rows)
    {
        Map<String, Object> directory = map();
        directory.put("title", levelName(objectLevel) + "目录");
        directory.put("rows", rows);
        directory.put("count", rows.size());
        return directory;
    }

    private List<Map<String, Object>> buildDirectoryRows(Map<String, Object> templateConfig, String objectLevel,
            List<Map<String, Object>> contentItems, List<Map<String, Object>> documents)
    {
        List<Map<String, Object>> chapters = castList(templateConfig.get("chapters"));
        List<Map<String, Object>> sources = castList(templateConfig.get("sources"));
        List<Map<String, Object>> rows = new ArrayList<>();
        boolean hasBomKey = false;
        int order = 1;

        for (Map<String, Object> chapter : chapters)
        {
            Map<String, Object> attrs = castMap(chapter.get("attrs"));
            if (isDirectoryWrapper(chapter))
            {
                continue;
            }
            String chapterLevel = defaultText(attrs.get("objectLevel"), "aircraft");
            if (!objectLevel.equals(chapterLevel))
            {
                continue;
            }
            String label = defaultText(chapter.get("chapterName"), chapter.get("chapterCode"));
            String chapterCode = text(chapter.get("chapterCode"));
            String displayType = defaultText(attrs.get("displayType"), "summary_table");
            boolean compositionTree = isCompositionTreeChapter(chapterCode, label, displayType);
            String category = categoryOf(chapterCode, label, displayType);
            String chapterId = text(chapter.get("chapterId"));
            List<String> sourceTables = sourceValues(sources, chapterId, "sourceTable");
            List<String> lifecycleStages = sourceValues(sources, chapterId, "lifecycleStage");
            List<String> primaryFields = toStringList(attrs.get("primaryFields"));
            List<String> blocks = toStringList(attrs.get("blocks"));

            Map<String, Object> item = map();
            item.put("key", directoryKey(chapterId, category, hasBomKey));
            item.put("chapterId", chapterId);
            item.put("orderNo", String.format("%02d", order));
            item.put("label", label);
            item.put("category", category);
            item.put("displayType", displayType);
            item.put("compositionTree", compositionTree);
            item.put("sortMode", defaultText(attrs.get("sortMode"), "business_order"));
            item.put("primaryFields", primaryFields);
            item.put("blocks", blocks);
            item.put("sourceTables", sourceTables);
            item.put("lifecycleStages", lifecycleStages);
            item.put("showMissingTips", defaultBoolean(attrs.get("showMissingTips"), true));
            item.put("attrs", attrs);
            item.put("status", directoryStatus(category, chapterId, sourceTables, lifecycleStages, contentItems,
                    documents, defaultBoolean(attrs.get("showMissingTips"), true)));
            rows.add(item);

            if ("composition".equals(category))
            {
                hasBomKey = true;
            }
            order++;
        }

        if (rows.isEmpty())
        {
            return fallbackDirectoryRows(objectLevel, contentItems, documents);
        }
        return rows;
    }

    private boolean isDirectoryWrapper(Map<String, Object> chapter)
    {
        String nodeKind = text(chapter.get("nodeKind"));
        if ("group".equals(nodeKind))
        {
            return true;
        }
        int chapterLevel = toInt(chapter.get("chapterLevel"), 0);
        if (chapterLevel <= 1 && !hasText(chapter.get("parentId")))
        {
            return true;
        }
        String chapterCode = text(chapter.get("chapterCode")).toUpperCase();
        String chapterName = text(chapter.get("chapterName"));
        return chapterCode.endsWith("_ROOT") && chapterName.endsWith("目录");
    }

    private String directoryKey(String chapterId, String category, boolean hasBomKey)
    {
        if ("composition".equals(category) && !hasBomKey)
        {
            return "bom";
        }
        return hasText(chapterId) ? "chapter-" + chapterId : category + "-" + System.nanoTime();
    }

    private String directoryStatus(String category, String chapterId, List<String> sourceTables,
            List<String> lifecycleStages, List<Map<String, Object>> contentItems,
            List<Map<String, Object>> documents, boolean showMissingTips)
    {
        if ("composition".equals(category) || "basic".equals(category))
        {
            return "complete";
        }
        boolean hasData = "documents".equals(category)
                ? !documents.isEmpty()
                : !filterContentItems(category, chapterId, sourceTables, lifecycleStages, contentItems).isEmpty();
        if (hasData)
        {
            return "complete";
        }
        return showMissingTips ? "missing" : "normal";
    }

    private List<Map<String, Object>> fallbackDirectoryRows(String objectLevel, List<Map<String, Object>> contentItems,
            List<Map<String, Object>> documents)
    {
        List<String> labels = new ArrayList<>();
        labels.add("基本信息");
        labels.add("组成结构");
        labels.add("设计数据");
        labels.add("制造数据");
        labels.add("检验记录");
        labels.add("装机履历");
        labels.add("服役维修");
        labels.add("故障记录");
        labels.add("附件材料");

        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++)
        {
            String label = labels.get(i);
            String displayType = label.contains("结构") ? "tree_table"
                    : label.contains("附件") ? "file_list"
                    : label.contains("履历") || label.contains("维修") || label.contains("故障") ? "timeline_files"
                    : "summary_table";
            String category = categoryOf("", label, displayType);
            Map<String, Object> item = map();
            item.put("key", i == 1 ? ("aircraft".equals(objectLevel) ? "bom" : "composition") : category + "-" + i);
            item.put("orderNo", String.format("%02d", i + 1));
            item.put("label", label);
            item.put("category", category);
            item.put("displayType", displayType);
            item.put("compositionTree", "composition".equals(category));
            item.put("sortMode", "business_order");
            item.put("primaryFields", Collections.emptyList());
            item.put("blocks", defaultBlocks(displayType));
            item.put("sourceTables", Collections.emptyList());
            item.put("lifecycleStages", Collections.emptyList());
            item.put("showMissingTips", true);
            item.put("status", directoryStatus(category, "", Collections.emptyList(), Collections.emptyList(),
                    contentItems, documents, true));
            rows.add(item);
        }
        return rows;
    }

    private Map<String, Object> buildNodeDetail(Map<String, Object> node, Map<String, Object> structureNode,
            List<Map<String, Object>> contentItems, List<Map<String, Object>> documents,
            List<Map<String, Object>> directoryRows)
    {
        Map<String, Object> detail = map();
        Map<String, Object> fieldMap = buildFieldMap(node, structureNode, contentItems, documents);
        detail.put("title", text(node.get("partName")));
        detail.put("subtitle", text(node.get("partNumber")) + valueSuffix(" / ", node.get("serialNumber")));
        detail.put("status", defaultText(node.get("status"), "有效"));
        detail.put("objectLevel", node.get("objectLevel"));
        detail.put("levelName", node.get("levelName"));
        detail.put("fieldMap", fieldMap);
        detail.put("basicFields", basicFields(node));
        detail.put("parameterCards", parameterCards(node, contentItems, documents));
        detail.put("tables", buildConfiguredTables(directoryRows, node, contentItems, documents));
        detail.put("timeline", buildTimeline(directoryRows, contentItems));
        detail.put("contentSummary", contentSummary(node, contentItems));
        return detail;
    }

    private List<Map<String, Object>> basicFields(Map<String, Object> node)
    {
        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(field("层级", node.get("levelName")));
        fields.add(field("编号", node.get("partNumber")));
        fields.add(field("名称", node.get("partName")));
        fields.add(field("序列号", defaultText(node.get("serialNumber"), "-")));
        fields.add(field("位置", defaultText(node.get("positionCode"), "-")));
        fields.add(field("ATA", defaultText(node.get("ataChapter"), "-")));
        fields.add(field("装机日期", defaultText(node.get("installDate"), "-")));
        fields.add(field("TSN / CSN", decimalText(node.get("tsnFh")) + " FH / " + defaultNumber(node.get("tsnFc"), 0) + " FC"));
        return fields;
    }

    private List<Map<String, Object>> parameterCards(Map<String, Object> node, List<Map<String, Object>> contentItems,
            List<Map<String, Object>> documents)
    {
        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(field("直接子节点", defaultNumber(node.get("childCount"), 0)));
        fields.add(field("内容记录", contentItems.size()));
        fields.add(field("附件材料", documents.size()));
        fields.add(field("完整性", missingCount(contentItems, documents) == 0 ? "已齐套" : "待补充"));
        return fields;
    }

    private List<Map<String, Object>> buildConfiguredTables(List<Map<String, Object>> directoryRows,
            Map<String, Object> node, List<Map<String, Object>> contentItems, List<Map<String, Object>> documents)
    {
        List<Map<String, Object>> tables = new ArrayList<>();
        for (Map<String, Object> directory : directoryRows)
        {
            String category = text(directory.get("category"));
            if ("composition".equals(category))
            {
                continue;
            }

            Map<String, Object> table = map();
            table.put("key", directory.get("key"));
            table.put("category", category);
            table.put("displayType", directory.get("displayType"));
            table.put("title", directory.get("label"));
            table.put("primaryFields", directory.get("primaryFields"));

            if ("documents".equals(category))
            {
                table.put("rows", documentRows(documents));
            }
            else if ("basic".equals(category))
            {
                table.put("rows", basicRows(node, directory, contentItems));
            }
            else
            {
                table.put("rows", contentRows(directory, contentItems));
            }
            tables.add(table);
        }
        return tables;
    }

    private List<Map<String, Object>> contentRows(Map<String, Object> directory, List<Map<String, Object>> contentItems)
    {
        String category = text(directory.get("category"));
        String chapterId = text(directory.get("chapterId"));
        List<String> sourceTables = toStringList(directory.get("sourceTables"));
        List<String> lifecycleStages = toStringList(directory.get("lifecycleStages"));
        List<Map<String, Object>> items = filterContentItems(category, chapterId, sourceTables, lifecycleStages,
                contentItems);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> item : items)
        {
            rows.add(row(defaultText(item.get("itemName"), item.get("itemCode")),
                    defaultText(item.get("contentSummary"), item.get("sourceRecordKey")),
                    defaultText(item.get("completenessStatus"), item.get("itemStatus"))));
        }
        if (rows.isEmpty())
        {
            rows.add(row("数据状态", "当前目录暂无匹配记录，等待数据来源补充或重新生成", "missing"));
        }
        return rows;
    }

    private List<Map<String, Object>> basicRows(Map<String, Object> node, Map<String, Object> directory,
            List<Map<String, Object>> contentItems)
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<Map<String, Object>> matchedItems = filterContentItems("basic", text(directory.get("chapterId")),
                toStringList(directory.get("sourceTables")), toStringList(directory.get("lifecycleStages")),
                contentItems);
        Map<String, Object> profileAttrs = firstProfileAttrs(matchedItems);
        List<String> configuredFields = toStringList(directory.get("primaryFields"));
        if (!profileAttrs.isEmpty())
        {
            List<String> fields = configuredFields.isEmpty()
                    ? preferredBasicFields(text(node.get("objectLevel")), profileAttrs)
                    : configuredFields;
            for (String field : fields)
            {
                Object value = lookupAttr(profileAttrs, field);
                if (hasText(text(value)))
                {
                    rows.add(row(fieldLabel(field), value, "complete"));
                }
            }
        }
        if (rows.isEmpty())
        {
            for (Map<String, Object> field : basicFields(node))
            {
                rows.add(row(field.get("label"), field.get("value"), "complete"));
            }
        }
        return rows;
    }

    private Map<String, Object> firstProfileAttrs(List<Map<String, Object>> items)
    {
        for (Map<String, Object> item : items)
        {
            Map<String, Object> attrs = castMap(item.get("attrs"));
            String itemType = text(item.get("itemType"));
            String actualSourceTable = text(attrs.get("actualSourceTable"));
            if ("profile_detail".equals(itemType) || actualSourceTable.startsWith("v_") || !attrs.isEmpty())
            {
                return attrs;
            }
        }
        return map();
    }

    private List<String> preferredBasicFields(String objectLevel, Map<String, Object> attrs)
    {
        List<String> fields = new ArrayList<>();
        if ("aircraft".equals(objectLevel))
        {
            fields.addAll(Arrays.asList("tailNumber", "aircraftType", "registrationNumber", "msn", "variant",
                    "engineType", "manufacturer", "deliveryDate", "operationalStatus", "currentOperator",
                    "totalFh", "totalFc", "currentConfigurationBaseline", "currentBomVersion"));
        }
        else if ("system".equals(objectLevel))
        {
            fields.addAll(Arrays.asList("systemCode", "systemName", "ataChapter", "functionSummary",
                    "systemBoundary", "configurationBaseline", "technicalStatus", "operationalStatus",
                    "subsystemCount", "directEquipmentCount"));
        }
        else if ("subsystem".equals(objectLevel))
        {
            fields.addAll(Arrays.asList("subsystemCode", "subsystemName", "systemName", "functionArea",
                    "functionSummary", "boundaryDescription", "configurationBaseline", "healthStatus",
                    "directEquipmentCount"));
        }
        else if ("equipment".equals(objectLevel))
        {
            fields.addAll(Arrays.asList("equipmentCode", "equipmentName", "equipmentType", "partNumber",
                    "serialNumber", "manufacturer", "supplierName", "installationPosition", "positionCode",
                    "installationStatus", "configurationVersion", "qualityStatus"));
        }
        else if ("component".equals(objectLevel))
        {
            fields.addAll(Arrays.asList("componentCode", "componentName", "componentType", "partNumber",
                    "serialNumber", "assemblyWorkOrderNo", "assemblyDate", "installationPosition",
                    "positionCode", "assemblyStatus", "qualityStatus"));
        }
        else
        {
            fields.addAll(Arrays.asList("partNumber", "partName", "serialNumber", "batchNumber", "lotNumber",
                    "manufacturer", "material", "materialGrade", "drawingNo", "drawingRevision",
                    "designRevision", "specification", "installationPosition", "positionCode",
                    "installationStatus", "qualityStatus", "remainingLifeValue", "remainingLifeUnit"));
        }
        List<String> result = new ArrayList<>();
        for (String field : fields)
        {
            if (lookupAttr(attrs, field) != null)
            {
                result.add(field);
            }
        }
        return result.isEmpty() ? fields : result;
    }

    private Object lookupAttr(Map<String, Object> attrs, String field)
    {
        if (attrs.containsKey(field))
        {
            return attrs.get(field);
        }
        String snake = toSnakeCase(field);
        if (attrs.containsKey(snake))
        {
            return attrs.get(snake);
        }
        for (Map.Entry<String, Object> entry : attrs.entrySet())
        {
            if (entry.getKey().equalsIgnoreCase(field))
            {
                return entry.getValue();
            }
        }
        return null;
    }

    private String toSnakeCase(String value)
    {
        return text(value).replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }

    private String fieldLabel(String field)
    {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("tailNumber", "机号");
        labels.put("aircraftType", "机型");
        labels.put("registrationNumber", "注册号");
        labels.put("msn", "MSN");
        labels.put("variant", "构型");
        labels.put("engineType", "发动机");
        labels.put("manufacturer", "制造商");
        labels.put("deliveryDate", "交付日期");
        labels.put("operationalStatus", "运行状态");
        labels.put("currentOperator", "当前运营方");
        labels.put("totalFh", "总飞行小时");
        labels.put("totalFc", "总循环");
        labels.put("currentConfigurationBaseline", "当前构型基线");
        labels.put("currentBomVersion", "当前BOM版本");
        labels.put("systemCode", "系统代码");
        labels.put("systemName", "系统名称");
        labels.put("subsystemCode", "子系统代码");
        labels.put("subsystemName", "子系统名称");
        labels.put("equipmentCode", "设备代码");
        labels.put("equipmentName", "设备名称");
        labels.put("componentCode", "组件代码");
        labels.put("componentName", "组件名称");
        labels.put("partNumber", "件号");
        labels.put("partName", "名称");
        labels.put("serialNumber", "序列号");
        labels.put("batchNumber", "批次号");
        labels.put("lotNumber", "炉批号");
        labels.put("ataChapter", "ATA");
        labels.put("functionSummary", "功能说明");
        labels.put("systemBoundary", "系统边界");
        labels.put("boundaryDescription", "边界说明");
        labels.put("configurationBaseline", "构型基线");
        labels.put("technicalStatus", "技术状态");
        labels.put("qualityStatus", "质量状态");
        labels.put("healthStatus", "健康状态");
        labels.put("material", "材料");
        labels.put("materialGrade", "材料牌号");
        labels.put("drawingNo", "图号");
        labels.put("drawingRevision", "图纸版次");
        labels.put("designRevision", "设计版次");
        labels.put("specification", "规格");
        labels.put("installationPosition", "安装位置");
        labels.put("positionCode", "位号");
        labels.put("installationStatus", "装机状态");
        labels.put("remainingLifeValue", "剩余寿命");
        labels.put("remainingLifeUnit", "寿命单位");
        return labels.containsKey(field) ? labels.get(field) : field;
    }

    private List<Map<String, Object>> documentRows(List<Map<String, Object>> documents)
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> document : documents)
        {
            rows.add(row(defaultText(document.get("title"), document.get("docNo")),
                    defaultText(document.get("sourceRecordKey"), document.get("fileStorageKey")),
                    defaultText(document.get("completenessStatus"), document.get("documentStatus"))));
        }
        if (rows.isEmpty())
        {
            rows.add(row("附件状态", "当前目录暂无附件材料", "missing"));
        }
        return rows;
    }

    private List<Map<String, Object>> buildTimeline(List<Map<String, Object>> directoryRows,
            List<Map<String, Object>> contentItems)
    {
        List<Map<String, Object>> timeline = new ArrayList<>();
        for (Map<String, Object> directory : directoryRows)
        {
            String displayType = text(directory.get("displayType"));
            List<String> blocks = toStringList(directory.get("blocks"));
            if (!"timeline_files".equals(displayType) && !blocks.contains("timeline"))
            {
                continue;
            }
            List<Map<String, Object>> items = filterContentItems(text(directory.get("category")),
                    text(directory.get("chapterId")), toStringList(directory.get("sourceTables")),
                    toStringList(directory.get("lifecycleStages")), contentItems);
            for (Map<String, Object> item : items)
            {
                Map<String, Object> row = map();
                row.put("key", directory.get("key"));
                row.put("time", displayTime(item.get("createdAt")));
                row.put("stage", item.get("lifecycleStage"));
                row.put("title", defaultText(item.get("itemName"), item.get("itemCode")));
                row.put("detail", defaultText(item.get("contentSummary"), item.get("sourceRecordKey")));
                row.put("sourceSystem", item.get("sourceSystem"));
                row.put("status", defaultText(item.get("completenessStatus"), item.get("itemStatus")));
                timeline.add(row);
            }
        }
        return timeline;
    }

    private Map<String, Object> buildFieldMap(Map<String, Object> node, Map<String, Object> structureNode,
            List<Map<String, Object>> contentItems, List<Map<String, Object>> documents)
    {
        Map<String, Object> fields = map();
        putField(fields, "partNumber", node.get("partNumber"));
        putField(fields, "件号", node.get("partNumber"));
        putField(fields, "partName", node.get("partName"));
        putField(fields, "名称", node.get("partName"));
        putField(fields, "serialNumber", node.get("serialNumber"));
        putField(fields, "序列号", node.get("serialNumber"));
        putField(fields, "positionCode", node.get("positionCode"));
        putField(fields, "位置", node.get("positionCode"));
        putField(fields, "ataChapter", node.get("ataChapter"));
        putField(fields, "ATA", node.get("ataChapter"));
        putField(fields, "installDate", node.get("installDate"));
        putField(fields, "装机日期", node.get("installDate"));
        putField(fields, "tsnFh", node.get("tsnFh"));
        putField(fields, "tsnFc", node.get("tsnFc"));
        putField(fields, "childCount", node.get("childCount"));
        putField(fields, "contentCount", contentItems.size());
        putField(fields, "documentCount", documents.size());
        if (structureNode != null)
        {
            putField(fields, "completenessStatus", structureNode.get("completenessStatus"));
            putField(fields, "missingCount", structureNode.get("missingCount"));
            Map<String, Object> attrs = castMap(structureNode.get("attrs"));
            for (Map.Entry<String, Object> entry : attrs.entrySet())
            {
                putField(fields, entry.getKey(), entry.getValue());
            }
        }
        for (Map<String, Object> item : contentItems)
        {
            Map<String, Object> attrs = castMap(item.get("attrs"));
            for (Map.Entry<String, Object> entry : attrs.entrySet())
            {
                if (!(entry.getValue() instanceof Map) && !(entry.getValue() instanceof List))
                {
                    putField(fields, entry.getKey(), entry.getValue());
                }
            }
        }
        return fields;
    }

    private String contentSummary(Map<String, Object> node, List<Map<String, Object>> contentItems)
    {
        for (Map<String, Object> item : contentItems)
        {
            if (hasText(item.get("contentSummary")))
            {
                return text(item.get("contentSummary"));
            }
        }
        return text(node.get("partName")) + "已纳入当前卷宗版本，可按模板目录查看关联数据。";
    }

    private List<Map<String, Object>> filterContentItems(String category, String chapterId, List<String> sourceTables,
            List<String> lifecycleStages, List<Map<String, Object>> contentItems)
    {
        Set<String> sourceTableSet = lowerSet(sourceTables);
        Set<String> lifecycleStageSet = upperSet(lifecycleStages);
        boolean hasDirectoryChapter = hasText(chapterId);
        List<Map<String, Object>> exact = new ArrayList<>();
        List<Map<String, Object>> fallback = new ArrayList<>();
        for (Map<String, Object> item : contentItems)
        {
            String itemChapterId = contentItemChapterId(item);
            if (hasDirectoryChapter && hasText(itemChapterId))
            {
                if (chapterId.equals(itemChapterId))
                {
                    exact.add(item);
                }
                continue;
            }
            if (hasDirectoryChapter && !isDirectoryFallbackCandidate(item))
            {
                continue;
            }
            String stage = text(item.get("lifecycleStage")).toUpperCase();
            String itemType = text(item.get("itemType")).toLowerCase();
            String sourceTable = text(item.get("sourceTable")).toLowerCase();
            if (!sourceTableSet.isEmpty() && !lifecycleStageSet.isEmpty())
            {
                if (sourceTableSet.contains(sourceTable) && lifecycleStageSet.contains(stage))
                {
                    fallback.add(item);
                }
                continue;
            }
            if (!sourceTableSet.isEmpty() && sourceTableSet.contains(sourceTable))
            {
                fallback.add(item);
                continue;
            }
            if (!lifecycleStageSet.isEmpty() && lifecycleStageSet.contains(stage))
            {
                fallback.add(item);
                continue;
            }
            if (!hasDirectoryChapter && matchesCategory(category, stage, itemType, sourceTable))
            {
                fallback.add(item);
            }
        }
        return exact.isEmpty() ? fallback : exact;
    }

    private String contentItemChapterId(Map<String, Object> item)
    {
        Map<String, Object> attrs = castMap(item.get("attrs"));
        String chapterId = text(attrs.get("chapterId"));
        return hasText(chapterId) ? chapterId : text(item.get("chapterId"));
    }

    private boolean isDirectoryFallbackCandidate(Map<String, Object> item)
    {
        String stage = text(item.get("lifecycleStage")).toUpperCase();
        String itemType = text(item.get("itemType")).toLowerCase();
        String sourceTable = text(item.get("sourceTable")).toLowerCase();
        return !"key_node_summary".equals(itemType)
                && !"DOSSIER".equals(stage)
                && !"FULL_LIFECYCLE".equals(stage)
                && !"DOCUMENT".equals(stage)
                && !"dossier_content_item".equals(sourceTable);
    }

    private boolean matchesCategory(String category, String stage, String itemType, String sourceTable)
    {
        if ("basic".equals(category))
        {
            return "profile_detail".equals(itemType) || sourceTable.contains("profile")
                    || "physical_aircraft".equals(sourceTable) || "part_instance".equals(sourceTable)
                    || "part_master".equals(sourceTable) || "key_node_summary".equals(itemType);
        }
        if ("design".equals(category))
        {
            return "DESIGN".equals(stage) || itemType.contains("design") || sourceTable.contains("design");
        }
        if ("manufacturing".equals(category))
        {
            return "MANUFACTURING".equals(stage) || "INSTALLATION".equals(stage)
                    || sourceTable.contains("manufacturing") || sourceTable.contains("work_order")
                    || sourceTable.contains("shop");
        }
        if ("inspection".equals(category))
        {
            return "INSPECTION".equals(stage) || sourceTable.contains("inspection") || itemType.contains("inspection");
        }
        if ("service".equals(category))
        {
            return "SERVICE".equals(stage) || sourceTable.contains("usage") || sourceTable.contains("service")
                    || sourceTable.contains("install") || sourceTable.contains("work_order")
                    || itemType.contains("maintenance");
        }
        if ("fault".equals(category))
        {
            return "FAULT".equals(stage) || sourceTable.contains("fault") || itemType.contains("fault");
        }
        if ("status".equals(category))
        {
            return "TECHNICAL_STATUS".equals(stage) || sourceTable.contains("status") || itemType.contains("status");
        }
        if ("interface".equals(category))
        {
            return "INTERFACE".equals(stage) || sourceTable.contains("interface") || itemType.contains("interface");
        }
        return true;
    }

    private String categoryOf(String chapterCode, String label, String displayType)
    {
        if (isCompositionTreeChapter(chapterCode, label, displayType))
        {
            return "composition";
        }
        if ("file_list".equals(displayType) || containsAny(label, "附件", "证明", "文件"))
        {
            return "documents";
        }
        if (containsAny(label, "故障"))
        {
            return "fault";
        }
        if (containsAny(label, "检验", "试验", "检查"))
        {
            return "inspection";
        }
        if (containsAny(label, "制造", "追溯", "装配"))
        {
            return "manufacturing";
        }
        if (containsAny(label, "装机", "服役", "使用", "履历", "维修"))
        {
            return "service";
        }
        if (containsAny(label, "技术状态", "状态", "变更"))
        {
            return "status";
        }
        if (containsAny(label, "接口"))
        {
            return "interface";
        }
        if (containsAny(label, "设计", "图纸", "规范", "参数"))
        {
            return "design";
        }
        if (containsAny(label, "基本", "概况"))
        {
            return "basic";
        }
        return "content";
    }

    private boolean isCompositionTreeChapter(String chapterCode, String label, String displayType)
    {
        if (!"tree_table".equals(displayType))
        {
            return false;
        }
        String code = text(chapterCode).toUpperCase();
        if (code.endsWith("_ROOT") || code.contains("TEMPLATE_ROOT"))
        {
            return false;
        }
        if (code.contains("BOM") || code.endsWith("_STRUCTURE") || code.endsWith("_PARTS"))
        {
            return true;
        }
        return containsAny(label, "BOM", "构型 / BOM", "组成结构", "系统结构", "子系统结构", "组成零件");
    }

    private boolean containsAny(String value, String... patterns)
    {
        String text = text(value);
        for (String pattern : patterns)
        {
            if (text.contains(pattern))
            {
                return true;
            }
        }
        return false;
    }

    private List<String> sourceValues(List<Map<String, Object>> sources, String chapterId, String key)
    {
        List<String> values = new ArrayList<>();
        if (!hasText(chapterId))
        {
            return values;
        }
        for (Map<String, Object> source : sources)
        {
            if (chapterId.equals(text(source.get("chapterId"))) && hasText(source.get(key)))
            {
                values.add("sourceTable".equals(key) ? canonicalSourceTable(source.get(key)) : text(source.get(key)));
            }
        }
        return values;
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

    private List<String> defaultBlocks(String displayType)
    {
        List<String> blocks = new ArrayList<>();
        blocks.add("summary");
        if ("tree_table".equals(displayType))
        {
            blocks.add("relation");
        }
        else if ("timeline_files".equals(displayType))
        {
            blocks.add("timeline");
            blocks.add("documents");
        }
        else if ("file_list".equals(displayType))
        {
            blocks.add("documents");
        }
        else
        {
            blocks.add("details");
        }
        return blocks;
    }

    private int missingCount(List<Map<String, Object>> contentItems, List<Map<String, Object>> documents)
    {
        int count = 0;
        for (Map<String, Object> item : contentItems)
        {
            if ("missing".equals(text(item.get("completenessStatus"))))
            {
                count++;
            }
        }
        for (Map<String, Object> document : documents)
        {
            if ("missing".equals(text(document.get("completenessStatus"))))
            {
                count++;
            }
        }
        return count;
    }

    private String completenessRate(Map<String, Object> metrics)
    {
        int warning = toInt(metrics.get("warningCount"), 0);
        int blocking = toInt(metrics.get("blockingCount"), 0);
        if (blocking > 0)
        {
            return "待处理";
        }
        if (warning > 0)
        {
            return "需复核";
        }
        return "完整";
    }

    private Map<String, Object> table(String title, List<Map<String, Object>> rows)
    {
        Map<String, Object> table = map();
        table.put("title", title);
        table.put("rows", rows);
        return table;
    }

    private Map<String, Object> row(Object name, Object value, Object status)
    {
        Map<String, Object> row = map();
        row.put("name", name);
        row.put("value", value);
        row.put("status", status);
        return row;
    }

    private Map<String, Object> field(Object label, Object value)
    {
        Map<String, Object> field = map();
        field.put("label", label);
        field.put("value", value);
        return field;
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
        Map<String, Object> item = map();
        item.putAll(row);
        String objectLevel = objectLevel(row);
        item.put("objectLevel", objectLevel);
        item.put("levelName", levelName(objectLevel));
        item.put("label", text(row.get("partName")) + "  " + text(row.get("partNumber")));
        item.put("leaf", toInt(row.get("childCount"), 0) == 0);
        item.put("status", "有效");
        return item;
    }

    private String objectLevel(Map<String, Object> row)
    {
        String nodeType = text(row.get("nodeType")).toUpperCase();
        int level = toInt(row.get("nodeLevel"), 0);
        if ("AIRCRAFT".equals(nodeType) || level == 1)
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
        return "part";
    }

    private String levelName(String objectLevel)
    {
        if ("aircraft".equals(objectLevel))
        {
            return "整机";
        }
        if ("system".equals(objectLevel))
        {
            return "系统";
        }
        if ("subsystem".equals(objectLevel))
        {
            return "子系统";
        }
        if ("equipment".equals(objectLevel))
        {
            return "设备";
        }
        if ("component".equals(objectLevel))
        {
            return "组件";
        }
        return "零件";
    }

    private List<Map<String, Object>> normalizeStructureNodes(List<Map<String, Object>> rows)
    {
        List<Map<String, Object>> result = normalizeRows(rows);
        for (Map<String, Object> item : result)
        {
            parseJsonField(item, "attrsJson", "attrs");
            parseJsonField(item, "sourceTraceJson", "sourceTrace");
        }
        return result;
    }

    private List<Map<String, Object>> normalizeContentItems(List<Map<String, Object>> rows)
    {
        List<Map<String, Object>> result = normalizeRows(rows);
        for (Map<String, Object> item : result)
        {
            parseJsonField(item, "attrsJson", "attrs");
            parseJsonField(item, "sourceTraceJson", "sourceTrace");
            item.put("createdAt", displayTime(item.get("createdAt")));
        }
        return result;
    }

    private List<Map<String, Object>> normalizeDocuments(List<Map<String, Object>> rows)
    {
        List<Map<String, Object>> result = normalizeRows(rows);
        for (Map<String, Object> item : result)
        {
            parseJsonField(item, "attrsJson", "attrs");
            parseJsonField(item, "sourceTraceJson", "sourceTrace");
            item.put("fileType", fileType(defaultText(item.get("fileStorageKey"), item.get("fileExt"))));
            item.put("createdAt", displayTime(item.get("createdAt")));
        }
        return result;
    }

    private String fileType(String fileStorageKey)
    {
        String lower = fileStorageKey.toLowerCase();
        if (lower.endsWith(".pdf") || "pdf".equals(lower))
        {
            return "PDF";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".png") || lower.endsWith(".jpeg")
                || "jpg".equals(lower) || "png".equals(lower) || "jpeg".equals(lower))
        {
            return "IMG";
        }
        if (lower.endsWith(".zip") || "zip".equals(lower))
        {
            return "ZIP";
        }
        return "DOC";
    }

    private List<Map<String, Object>> normalizeRows(List<Map<String, Object>> rows)
    {
        if (rows == null)
        {
            return new ArrayList<>();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows)
        {
            Map<String, Object> item = map();
            item.putAll(row);
            result.add(item);
        }
        return result;
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

    private List<String> toStringList(Object value)
    {
        List<String> result = new ArrayList<>();
        if (value instanceof List)
        {
            for (Object item : (List<?>) value)
            {
                if (hasText(item))
                {
                    result.add(text(item));
                }
            }
        }
        return result;
    }

    private Set<String> lowerSet(List<String> values)
    {
        Set<String> result = new HashSet<>();
        for (String value : values)
        {
            result.add(value.toLowerCase());
        }
        return result;
    }

    private Set<String> upperSet(List<String> values)
    {
        Set<String> result = new HashSet<>();
        for (String value : values)
        {
            result.add(value.toUpperCase());
        }
        return result;
    }

    private void putField(Map<String, Object> fields, String key, Object value)
    {
        if (hasText(key) && hasText(value) && !fields.containsKey(key))
        {
            fields.put(key, value);
        }
    }

    private Object defaultNumber(Object first, Object second, Object fallback)
    {
        if (first != null)
        {
            return first;
        }
        if (second != null)
        {
            return second;
        }
        return fallback;
    }

    private Object defaultNumber(Object first, Object fallback)
    {
        return first == null ? fallback : first;
    }

    private boolean defaultBoolean(Object value, boolean fallback)
    {
        if (value == null)
        {
            return fallback;
        }
        if (value instanceof Boolean)
        {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private int toInt(Object value, int fallback)
    {
        if (value == null)
        {
            return fallback;
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
            return fallback;
        }
    }

    private String decimalText(Object value)
    {
        if (value == null)
        {
            return "0";
        }
        if (value instanceof BigDecimal)
        {
            return ((BigDecimal) value).stripTrailingZeros().toPlainString();
        }
        return String.valueOf(value);
    }

    private String displayTime(Object value)
    {
        if (value == null)
        {
            return "";
        }
        if (value instanceof Date)
        {
            return DISPLAY_TIME.format((Date) value);
        }
        return String.valueOf(value).replace("T", " ");
    }

    private String valueSuffix(String prefix, Object value)
    {
        return hasText(value) ? prefix + text(value) : "";
    }

    private String defaultText(Object value, Object fallback)
    {
        return hasText(value) ? text(value) : text(fallback);
    }

    private String blankToNull(String value)
    {
        return hasText(value) ? value : null;
    }

    private String resolveVersionId(String aircraftId, String instanceId, String versionId)
    {
        if (hasText(versionId))
        {
            return versionId;
        }
        if (!hasText(instanceId) && !hasText(aircraftId))
        {
            return null;
        }
        Map<String, Object> instance = detailMapper.selectLatestInstance(blankToNull(aircraftId), blankToNull(instanceId));
        if (instance == null)
        {
            return null;
        }
        Map<String, Object> version = detailMapper.selectVersion(text(instance.get("instanceId")), null);
        return version == null ? null : text(version.get("versionId"));
    }

    private boolean hasText(Object value)
    {
        return value != null && String.valueOf(value).trim().length() > 0;
    }

    private String text(Object value)
    {
        return value == null ? "" : String.valueOf(value);
    }

    private Map<String, Object> map()
    {
        return new LinkedHashMap<>();
    }
}
