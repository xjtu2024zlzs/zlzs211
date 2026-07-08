package com.ruoyi.project1.dossier.service.impl;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
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

    private static final String DEMO_TRACE_BOM_NODE_ID = "f1000006-0006-4006-8006-000000000006";

    private static final String DEMO_TRACE_PART_NUMBER = "HYD-TUBE-MLG-32A";

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

    @Override
    public byte[] exportTraceResultWord(String instanceId, String versionId, String bomNodeId, String resultId)
    {
        if (!hasText(resultId))
        {
            throw new ServiceException("Trace result id is required.");
        }
        Map<String, Object> node = null;
        if (hasText(versionId) && hasText(bomNodeId))
        {
            node = detailMapper.selectStructureBomNode(versionId, bomNodeId);
        }
        Map<String, Object> bomNode = node == null ? null : normalizeBomNode(node);
        List<Map<String, Object>> traceRows = bomNode != null
                ? detailMapper.selectQualityTraceRows(qualityTraceTokens(bomNode), demoTraceEnabled(bomNode))
                : Collections.emptyList();
        List<Map<String, Object>> traceResults = buildTraceResults(traceRows, Collections.emptyList());
        for (Map<String, Object> result : traceResults)
        {
            if (resultId.equals(text(result.get("resultId"))))
            {
                return buildTraceResultWord(result);
            }
        }
        throw new ServiceException("Trace result not found.");
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
        List<Map<String, Object>> traceRows = detailMapper.selectQualityTraceRows(qualityTraceTokens(bomNode),
                demoTraceEnabled(bomNode));
        List<Map<String, Object>> traceResults = buildTraceResults(traceRows, Collections.emptyList());
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
        result.put("traceResults", traceResults);
        result.put("detail", buildNodeDetail(bomNode, structureNode, contentItems, documents, directoryRows,
                traceResults));
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
            List<Map<String, Object>> directoryRows, List<Map<String, Object>> traceResults)
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
        detail.put("traceResults", traceResults);
        return detail;
    }

    private List<Map<String, Object>> buildTraceResults(List<Map<String, Object>> rows,
            List<Map<String, Object>> documents)
    {
        List<Map<String, Object>> normalizedRows = normalizeRows(rows);
        Map<String, List<Map<String, Object>>> documentsByResult = groupTraceDocuments(documents);
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map<String, Object> row : normalizedRows)
        {
            if (isQualityTraceRow(row))
            {
                results.add(qualityTraceResult(row));
                continue;
            }
            String resultId = text(row.get("resultId"));
            Map<String, Object> value = parseJsonObject(row.get("resultValueJson"));
            Map<String, Object> scope = parseJsonObject(row.get("scopeJson"));
            List<Map<String, Object>> resultDocuments = documentsByResult.getOrDefault(resultId,
                    Collections.emptyList());

            Map<String, Object> item = map();
            item.put("key", resultId);
            item.put("resultId", resultId);
            item.put("taskId", row.get("taskId"));
            item.put("taskCode", row.get("taskCode"));
            item.put("taskName", row.get("taskName"));
            item.put("title", defaultText(row.get("resultTitle"), row.get("taskName")));
            item.put("summary", defaultText(row.get("resultSummary"), ""));
            item.put("resultType", row.get("resultType"));
            item.put("resultTypeLabel", traceResultTypeLabel(row.get("resultType")));
            item.put("sourceComponent", defaultText(row.get("sourceComponent"), scope.get("sourceComponent")));
            item.put("sourceLabel", sourceComponentLabel(item.get("sourceComponent")));
            item.put("createdAt", displayTime(row.get("createdAt")));
            item.put("confidence", row.get("confidence"));
            item.put("rankNo", row.get("rankNo"));
            item.put("algorithmCode", row.get("algorithmCode"));
            item.put("algorithmVersion", row.get("algorithmVersion"));
            item.put("relatedObjectType", row.get("relatedObjectType"));
            item.put("relatedObjectId", row.get("relatedObjectId"));
            item.put("fields", traceFieldRows(value));
            item.put("fullContent", traceFullContent(value, row));
            item.put("documents", resultDocuments);
            item.put("attachmentCount", resultDocuments.size());
            item.put("status", "complete");
            results.add(item);
        }
        return results;
    }

    private boolean isQualityTraceRow(Map<String, Object> row)
    {
        return row.containsKey("problemId") || row.containsKey("problemCode") || row.containsKey("taskStatus");
    }

    private Map<String, Object> qualityTraceResult(Map<String, Object> row)
    {
        String taskId = text(row.get("taskId"));
        String problemId = text(row.get("problemId"));
        String problemCode = text(row.get("problemCode"));
        String problemTitle = defaultText(row.get("problemTitle"), row.get("problemCode"));
        String moduleName = firstText(row.get("moduleName"), row.get("currentModuleName"), "\u8d28\u91cf\u7ba1\u7406\u4e2d\u5fc3");
        String summary = firstText(row.get("processResult"), row.get("dispatchOpinion"),
                row.get("problemDescription"), row.get("problemTitle"));

        Map<String, Object> item = map();
        item.put("key", hasText(taskId) ? "quality-task-" + taskId : "quality-problem-" + problemId);
        item.put("resultId", item.get("key"));
        item.put("taskId", row.get("taskId"));
        item.put("taskCode", row.get("problemCode"));
        item.put("taskName", moduleName);
        item.put("title", qualityTraceTitle(problemCode, problemTitle, moduleName, hasText(taskId)));
        item.put("summary", defaultText(summary, "\u6682\u65e0\u5904\u7406\u7ed3\u679c"));
        item.put("resultType", hasText(taskId) ? "quality_task" : "quality_problem");
        item.put("resultTypeLabel", hasText(taskId) ? "\u8ffd\u6eaf\u4efb\u52a1" : "\u8ffd\u6eaf\u95ee\u9898");
        item.put("sourceComponent", defaultText(row.get("moduleCode"), "qms"));
        item.put("sourceLabel", moduleName);
        item.put("createdAt", displayTime(firstText(row.get("confirmTime"), row.get("submitTime"),
                row.get("dispatchTime"), row.get("taskUpdateTime"), row.get("problemUpdateTime"),
                row.get("occurTime"), row.get("problemCreateTime"))));
        item.put("confidence", "-");
        item.put("rankNo", null);
        item.put("algorithmCode", row.get("moduleCode"));
        item.put("algorithmVersion", "");
        item.put("relatedObjectType", "quality_problem");
        item.put("relatedObjectId", row.get("problemId"));
        item.put("fields", qualityTraceFields(row));
        item.put("fullContent", qualityTraceFullContent(row));
        item.put("documents", qualityTraceDocuments(row));
        item.put("attachmentCount", castList(item.get("documents")).size());
        item.put("status", firstText(row.get("taskStatus"), row.get("problemStatus"), "complete"));
        return item;
    }

    private String qualityTraceTitle(String problemCode, String problemTitle, String moduleName, boolean hasTask)
    {
        List<String> parts = new ArrayList<>();
        if (hasText(problemCode))
        {
            parts.add(problemCode);
        }
        if (hasText(problemTitle))
        {
            parts.add(problemTitle);
        }
        if (hasTask && hasText(moduleName))
        {
            parts.add(moduleName);
        }
        return parts.isEmpty() ? "\u8d28\u91cf\u8ffd\u6eaf" : String.join(" / ", parts);
    }

    private List<Map<String, Object>> qualityTraceFields(Map<String, Object> row)
    {
        List<Map<String, Object>> fields = new ArrayList<>();
        addTraceField(fields, "\u8d28\u91cf\u95ee\u9898\u7f16\u53f7", row.get("problemCode"));
        addTraceField(fields, "\u95ee\u9898\u72b6\u6001", row.get("problemStatus"));
        addTraceField(fields, "\u4e25\u91cd\u7a0b\u5ea6", row.get("severity"));
        addTraceField(fields, "\u53d1\u751f\u90e8\u4f4d", row.get("occurPart"));
        addTraceField(fields, "\u90e8\u4ef6\u7f16\u53f7", row.get("componentCode"));
        addTraceField(fields, "\u5904\u7406\u8bfe\u9898", firstText(row.get("moduleName"), row.get("currentModuleName")));
        addTraceField(fields, "\u4efb\u52a1\u72b6\u6001", row.get("taskStatus"));
        addTraceField(fields, "\u5206\u6d3e\u8bf4\u660e", row.get("dispatchOpinion"));
        addTraceField(fields, "\u5904\u7406\u7ed3\u679c", row.get("processResult"));
        addTraceField(fields, "\u5904\u7406\u6587\u4ef6", row.get("processFile"));
        addTraceField(fields, "\u53d1\u751f\u65f6\u95f4", displayTime(row.get("occurTime")));
        addTraceField(fields, "\u63d0\u4ea4\u65f6\u95f4", displayTime(row.get("submitTime")));
        addTraceField(fields, "\u786e\u8ba4\u65f6\u95f4", displayTime(row.get("confirmTime")));
        return fields;
    }

    private void addTraceField(List<Map<String, Object>> fields, String label, Object value)
    {
        if (!hasText(value))
        {
            return;
        }
        Map<String, Object> field = map();
        field.put("label", label);
        field.put("value", text(value));
        fields.add(field);
    }

    private String qualityTraceFullContent(Map<String, Object> row)
    {
        List<String> lines = new ArrayList<>();
        addTraceLine(lines, "\u8d28\u91cf\u95ee\u9898\u7f16\u53f7", row.get("problemCode"));
        addTraceLine(lines, "\u95ee\u9898\u6807\u9898", row.get("problemTitle"));
        addTraceLine(lines, "\u95ee\u9898\u63cf\u8ff0", row.get("problemDescription"));
        addTraceLine(lines, "\u53d1\u751f\u90e8\u4f4d", row.get("occurPart"));
        addTraceLine(lines, "\u90e8\u4ef6\u7f16\u53f7", row.get("componentCode"));
        addTraceLine(lines, "\u95ee\u9898\u72b6\u6001", row.get("problemStatus"));
        addTraceLine(lines, "\u5904\u7406\u8bfe\u9898", firstText(row.get("moduleName"), row.get("currentModuleName")));
        addTraceLine(lines, "\u4efb\u52a1\u72b6\u6001", row.get("taskStatus"));
        addTraceLine(lines, "\u5206\u6d3e\u8bf4\u660e", row.get("dispatchOpinion"));
        addTraceLine(lines, "\u5904\u7406\u7ed3\u679c", row.get("processResult"));
        addTraceLine(lines, "\u5904\u7406\u6587\u4ef6", row.get("processFile"));
        return lines.isEmpty() ? "-" : String.join("\n", lines);
    }

    private void addTraceLine(List<String> lines, String label, Object value)
    {
        if (hasText(value))
        {
            lines.add(label + "\uff1a" + text(value));
        }
    }

    private List<Map<String, Object>> qualityTraceDocuments(Map<String, Object> row)
    {
        if (!hasText(row.get("processFile")))
        {
            return Collections.emptyList();
        }
        Map<String, Object> document = map();
        document.put("title", fileNameFromPath(row.get("processFile")));
        document.put("docNo", row.get("problemCode"));
        document.put("fileType", fileType(text(row.get("processFile"))));
        document.put("accessUrl", row.get("processFile"));
        return Collections.singletonList(document);
    }

    private String fileNameFromPath(Object value)
    {
        String path = text(value).replace("\\", "/");
        int index = path.lastIndexOf('/');
        return index >= 0 && index < path.length() - 1 ? path.substring(index + 1) : path;
    }

    private List<String> qualityTraceTokens(Map<String, Object> node)
    {
        Set<String> tokens = new LinkedHashSet<>();
        addQualityTraceToken(tokens, node == null ? null : node.get("partNumber"));
        addQualityTraceToken(tokens, node == null ? null : node.get("partName"));
        addQualityTraceToken(tokens, node == null ? null : node.get("serialNumber"));
        addQualityTraceToken(tokens, node == null ? null : node.get("positionCode"));
        addQualityTraceToken(tokens, node == null ? null : node.get("partInstanceId"));
        addQualityTraceToken(tokens, node == null ? null : node.get("nodeId"));

        return new ArrayList<>(tokens);
    }

    private boolean demoTraceEnabled(Map<String, Object> node)
    {
        if (node == null)
        {
            return false;
        }
        return DEMO_TRACE_BOM_NODE_ID.equals(text(node.get("nodeId")))
                || DEMO_TRACE_PART_NUMBER.equals(text(node.get("partNumber")));
    }

    private void addQualityTraceToken(Set<String> tokens, Object value)
    {
        if (!hasText(value))
        {
            return;
        }
        String token = text(value).trim();
        if (token.length() < 2)
        {
            return;
        }
        tokens.add(token);
    }

    private Map<String, List<Map<String, Object>>> groupTraceDocuments(List<Map<String, Object>> documents)
    {
        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> document : normalizeTraceDocuments(documents))
        {
            String resultId = traceDocumentResultId(document);
            if (!hasText(resultId))
            {
                continue;
            }
            grouped.computeIfAbsent(resultId, key -> new ArrayList<>()).add(document);
        }
        return grouped;
    }

    private List<Map<String, Object>> normalizeTraceDocuments(List<Map<String, Object>> documents)
    {
        List<Map<String, Object>> result = normalizeRows(documents);
        for (Map<String, Object> item : result)
        {
            if (!item.containsKey("attrs"))
            {
                parseJsonField(item, "attrsJson", "attrs");
            }
            if (!item.containsKey("sourceTrace"))
            {
                parseJsonField(item, "sourceTraceJson", "sourceTrace");
            }
            item.put("fileType", defaultText(item.get("fileType"),
                    fileType(defaultText(item.get("fileStorageKey"), item.get("fileExt")))));
            item.put("createdAt", displayTime(item.get("createdAt")));
        }
        return result;
    }

    private String traceDocumentResultId(Map<String, Object> document)
    {
        Map<String, Object> sourceTrace = castMap(document.get("sourceTrace"));
        Map<String, Object> attrs = castMap(document.get("attrs"));
        return firstText(document.get("sourceRecordId"), sourceTrace.get("sourceRecordId"), attrs.get("resultId"));
    }

    private List<Map<String, Object>> traceFieldRows(Map<String, Object> value)
    {
        if (value.isEmpty())
        {
            return Collections.emptyList();
        }
        List<Map<String, Object>> fields = new ArrayList<>();
        for (Map.Entry<String, Object> entry : value.entrySet())
        {
            String key = entry.getKey();
            if (!hasText(key) || traceFieldSkipped(key))
            {
                continue;
            }
            Map<String, Object> field = map();
            field.put("label", key);
            field.put("value", traceValueText(entry.getValue()));
            fields.add(field);
        }
        return fields;
    }

    private boolean traceFieldSkipped(String key)
    {
        String lower = key.toLowerCase(Locale.ROOT);
        return "documents".equals(lower) || "document".equals(lower) || "files".equals(lower)
                || "file".equals(lower);
    }

    private String traceFullContent(Map<String, Object> value, Map<String, Object> row)
    {
        if (!value.isEmpty())
        {
            return toPrettyJson(value);
        }
        return defaultText(row.get("resultSummary"), row.get("resultTitle"));
    }

    private String traceValueText(Object value)
    {
        if (value instanceof Map || value instanceof List)
        {
            return toCompactJson(value);
        }
        return text(value);
    }

    private byte[] buildTraceResultWord(Map<String, Object> result)
    {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream())
        {
            addWordTitle(document, "\u8ffd\u6eaf\u7ed3\u679c\u62a5\u544a");
            addWordSection(document, "\u57fa\u672c\u4fe1\u606f");
            List<String[]> basicRows = new ArrayList<>();
            basicRows.add(new String[] { "\u7ed3\u679c\u6807\u9898", text(result.get("title")) });
            basicRows.add(new String[] { "\u6765\u6e90\u8bfe\u9898", text(result.get("sourceLabel")) });
            basicRows.add(new String[] { "\u7ed3\u679c\u7c7b\u578b", text(result.get("resultTypeLabel")) });
            basicRows.add(new String[] { "\u56de\u5199\u65f6\u95f4", text(result.get("createdAt")) });
            basicRows.add(new String[] { "\u7f6e\u4fe1\u5ea6", defaultText(result.get("confidence"), "-") });
            basicRows.add(new String[] { "\u6458\u8981", text(result.get("summary")) });
            addWordKeyValueTable(document, basicRows);

            addWordSection(document, "\u5b57\u6bb5\u660e\u7ec6");
            addWordMapTable(document, castList(result.get("fields")), "\u5b57\u6bb5", "\u503c", "label", "value");

            addWordSection(document, "\u5b8c\u6574\u5185\u5bb9");
            addWordParagraph(document, defaultText(result.get("fullContent"), "-"));

            addWordSection(document, "\u5173\u8054\u6587\u6863");
            addWordMapTable(document, castList(result.get("documents")), "\u6587\u6863\u540d\u79f0",
                    "\u6587\u6863\u7f16\u53f7", "title", "docNo");

            document.write(output);
            return output.toByteArray();
        }
        catch (Exception e)
        {
            throw new ServiceException("Failed to export trace result.");
        }
    }

    private void addWordTitle(XWPFDocument document, String text)
    {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setFontSize(18);
        run.setText(text);
    }

    private void addWordSection(XWPFDocument document, String text)
    {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setFontSize(13);
        run.setText(text);
    }

    private void addWordParagraph(XWPFDocument document, String text)
    {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setFontSize(10);
        run.setText(text);
    }

    private void addWordKeyValueTable(XWPFDocument document, List<String[]> rows)
    {
        if (rows.isEmpty())
        {
            addWordParagraph(document, "\u65e0");
            return;
        }
        XWPFTable table = document.createTable(rows.size(), 2);
        for (int i = 0; i < rows.size(); i++)
        {
            XWPFTableRow row = table.getRow(i);
            setWordCell(row, 0, rows.get(i)[0], true);
            setWordCell(row, 1, rows.get(i)[1], false);
        }
    }

    private void addWordMapTable(XWPFDocument document, List<Map<String, Object>> rows, String firstHeader,
            String secondHeader, String firstKey, String secondKey)
    {
        if (rows.isEmpty())
        {
            addWordParagraph(document, "\u65e0");
            return;
        }
        XWPFTable table = document.createTable(rows.size() + 1, 2);
        setWordCell(table.getRow(0), 0, firstHeader, true);
        setWordCell(table.getRow(0), 1, secondHeader, true);
        for (int i = 0; i < rows.size(); i++)
        {
            XWPFTableRow row = table.getRow(i + 1);
            setWordCell(row, 0, text(rows.get(i).get(firstKey)), false);
            setWordCell(row, 1, text(rows.get(i).get(secondKey)), false);
        }
    }

    private void setWordCell(XWPFTableRow row, int index, String text, boolean bold)
    {
        XWPFRun run = row.getCell(index).getParagraphArray(0).createRun();
        run.setBold(bold);
        run.setText(defaultText(text, "-"));
    }

    private String sourceComponentLabel(Object sourceComponent)
    {
        String source = text(sourceComponent).toLowerCase(Locale.ROOT);
        if (source.contains("project2") || source.contains("topic2"))
        {
            return "\u8bfe\u9898\u4e8c";
        }
        if (source.contains("project3") || source.contains("topic3"))
        {
            return "\u8bfe\u9898\u4e09";
        }
        if (source.contains("project4") || source.contains("topic4"))
        {
            return "\u8bfe\u9898\u56db";
        }
        if (source.contains("project5") || source.contains("topic5"))
        {
            return "\u8bfe\u9898\u4e94";
        }
        return hasText(sourceComponent) ? text(sourceComponent) : "\u5916\u90e8\u7ed3\u679c";
    }

    private String traceResultTypeLabel(Object resultType)
    {
        String type = text(resultType).toLowerCase(Locale.ROOT);
        if (type.contains("design") || type.contains("optimization"))
        {
            return "\u8bbe\u8ba1\u4f18\u5316";
        }
        if (type.contains("quality"))
        {
            return "\u8d28\u91cf\u76d1\u63a7";
        }
        if (type.contains("fault"))
        {
            return "\u6545\u969c\u8bca\u65ad";
        }
        if (type.contains("trace"))
        {
            return "\u8ffd\u6eaf\u5206\u6790";
        }
        if (type.contains("completion"))
        {
            return "\u6570\u636e\u8865\u5168";
        }
        return hasText(resultType) ? text(resultType) : "\u8ffd\u6eaf\u7ed3\u679c";
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
            String category = effectiveDirectoryCategory(directory);
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
        String category = effectiveDirectoryCategory(directory);
        String chapterId = text(directory.get("chapterId"));
        List<String> sourceTables = toStringList(directory.get("sourceTables"));
        List<String> lifecycleStages = toStringList(directory.get("lifecycleStages"));
        List<Map<String, Object>> items = filterContentItems(category, chapterId, sourceTables, lifecycleStages,
                contentItems);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> item : items)
        {
            if ("manufacturing".equals(category))
            {
                rows.add(manufacturingRow(item));
            }
            else if ("installation".equals(category))
            {
                rows.add(serviceRow(item));
            }
            else if ("inspection".equals(category))
            {
                rows.add(inspectionRow(item));
            }
            else if ("service".equals(category))
            {
                rows.add(serviceRow(item));
            }
            else if ("fault".equals(category))
            {
                rows.add(faultRow(item));
            }
            else
            {
                rows.add(row(defaultText(item.get("itemName"), item.get("itemCode")),
                        defaultText(item.get("contentSummary"), item.get("sourceRecordKey")),
                        defaultText(item.get("completenessStatus"), item.get("itemStatus"))));
            }
        }
        if (rows.isEmpty())
        {
            rows.add(row("数据状态", "当前目录暂无匹配记录，等待数据来源补充或重新生成", "missing"));
        }
        return rows;
    }

    private Map<String, Object> serviceRow(Map<String, Object> item)
    {
        Map<String, Object> attrs = castMap(item.get("attrs"));
        String table = logicalSourceTable(defaultText(attrs.get("actualSourceTable"), item.get("sourceTable")));
        Object status = defaultText(item.get("completenessStatus"), item.get("itemStatus"));
        if ("install_removal".equals(table))
        {
            Map<String, Object> params = parseJsonObject(attr(attrs, "assemblyParams", "assembly_params"));
            String action = text(attr(attrs, "actionType", "action_type")).toUpperCase();
            String stage = text(item.get("lifecycleStage")).toUpperCase();
            boolean installAction = action.contains("INSTALL") || "INSTALLATION".equals(stage);
            String group = installAction ? "install" : "removal";
            String title = (installAction ? "装机记录：" : action.contains("REMOVAL") ? "拆卸记录：" : "装拆记录：")
                    + defaultText(attr(attrs, "objectName", "object_name", "partName", "part_name"),
                            defaultText(item.get("itemName"), item.get("sourceRecordKey")));
            return serviceGroupedRow(group, title,
                    joinParts(Arrays.asList(valueText("时间", attr(attrs, "actionDate", "action_date",
                            "eventTime", "event_time")),
                            valueText("位置", attr(attrs, "installPosition", "install_position",
                                    "positionCode", "position_code", "installPosCode", "install_pos_code")),
                            valueText("原因", attr(attrs, "removalReason", "removal_reason", "reason")),
                            valueText("故障", attr(attrs, "removalFaultCode", "removal_fault_code")),
                            valueText("力矩", valueWithUnit(attr(params, "torqueNm", "torque_n_m"), "N*m")),
                            valueText("泄漏检查", attr(params, "leakCheckResult", "leak_check_result")),
                            text(attr(attrs, "removalRemark", "removal_remark", "remarks", "remark")),
                            text(attr(params, "displayText", "display_text")),
                            cleanRecordSummary(item.get("contentSummary"), item.get("sourceRecordKey"),
                                    item.get("sourceRecordId")))),
                    status);
        }
        if ("life_usage_record".equals(table))
        {
            return serviceGroupedRow("usage",
                    "使用量记录：" + defaultText(attr(attrs, "objectName", "object_name"),
                            defaultText(item.get("itemName"), item.get("sourceRecordKey"))),
                    joinParts(Arrays.asList(valueText("累计FH", attr(attrs, "tsnFh", "tsn_fh", "totalFh",
                            "total_fh")),
                            valueText("累计FC", attr(attrs, "tsnFc", "tsn_fc", "totalFc", "total_fc")),
                            valueText("本次FH", attr(attrs, "fhDelta", "fh_delta")),
                            valueText("本次FC", attr(attrs, "fcDelta", "fc_delta")),
                            valueText("剩余寿命", valueWithUnit(attr(attrs, "remainingLifeValue",
                                    "remaining_life_value"), attr(attrs, "remainingLifeUnit",
                                            "remaining_life_unit"))),
                            valueText("时间", attr(attrs, "recordTime", "record_time", "eventTime",
                                    "event_time")),
                            cleanRecordSummary(item.get("contentSummary"), item.get("sourceRecordKey"),
                                    item.get("sourceRecordId")))),
                    status);
        }
        if ("work_order".equals(table))
        {
            return serviceGroupedRow("work_order",
                    "维修工单：" + defaultText(attr(attrs, "woNumber", "wo_number", "orderCode", "order_code"),
                            item.get("sourceRecordKey")),
                    joinParts(Arrays.asList(valueText("类型", attr(attrs, "workOrderType", "work_order_type")),
                            valueText("状态", readableStatus(attr(attrs, "status", "woStatus", "wo_status"))),
                            timeRange(attr(attrs, "plannedStart", "planned_start", "actualStart",
                                    "actual_start"), attr(attrs, "actualFinish", "actual_finish", "closedAt",
                                            "closed_at")),
                            text(attr(attrs, "workDesc", "work_desc", "taskTitle", "task_title")),
                            text(attr(attrs, "closeSummary", "close_summary", "resultNotes", "result_notes")),
                            cleanRecordSummary(item.get("contentSummary"), item.get("sourceRecordKey"),
                                    item.get("sourceRecordId")))),
                    status);
        }
        if ("quality_text_record".equals(table))
        {
            return serviceGroupedRow("feedback",
                    "巡检记录：" + defaultText(attr(attrs, "recordTitle", "record_title", "title"),
                            defaultText(item.get("itemName"), item.get("sourceRecordKey"))),
                    joinParts(Arrays.asList(valueText("时间", attr(attrs, "recordTime", "record_time",
                            "createdAt", "created_at")),
                            text(attr(attrs, "recordText", "record_text", "textContent", "text_content",
                                    "feedbackText", "feedback_text", "remarks", "remark")),
                            valueText("结论", readableStatus(attr(attrs, "result", "status"))),
                            cleanRecordSummary(item.get("contentSummary"), item.get("sourceRecordKey"),
                                    item.get("sourceRecordId")))),
                    status);
        }
        return serviceGroupedRow("other", defaultText(item.get("itemName"), item.get("itemCode")),
                defaultText(item.get("contentSummary"), item.get("sourceRecordKey")), status);
    }

    private Map<String, Object> faultRow(Map<String, Object> item)
    {
        Map<String, Object> attrs = castMap(item.get("attrs"));
        String table = logicalSourceTable(defaultText(attrs.get("actualSourceTable"), item.get("sourceTable")));
        Object status = defaultText(item.get("completenessStatus"), item.get("itemStatus"));
        if ("fault_event".equals(table))
        {
            Object eventStatus = defaultText(attr(attrs, "status", "eventStatus", "event_status"),
                    item.get("itemStatus"));
            return faultGroupedRow("fault",
                    "故障事件：" + defaultText(attr(attrs, "faultCode", "fault_code", "eventCode", "event_code"),
                            item.get("sourceRecordKey")),
                    joinParts(Arrays.asList(text(attr(attrs, "faultDescription", "fault_description",
                            "description", "eventDescription", "event_description")),
                            valueText("严重度", attr(attrs, "severityLevel", "severity_level", "severity")),
                            valueText("状态", readableStatus(eventStatus)),
                            valueText("处置", attr(attrs, "resolutionType", "resolution_type",
                                    "resolutionAction", "resolution_action", "resolution")),
                            valueText("发现时间", attr(attrs, "reportedAt", "reported_at", "eventTime",
                                    "event_time")),
                            valueText("关闭时间", attr(attrs, "closedAt", "closed_at")),
                            cleanRecordSummary(item.get("contentSummary"), item.get("sourceRecordKey"),
                                    item.get("sourceRecordId")))),
                    status);
        }
        return faultGroupedRow("other", defaultText(item.get("itemName"), item.get("itemCode")),
                defaultText(item.get("contentSummary"), item.get("sourceRecordKey")), status);
    }

    private Map<String, Object> serviceGroupedRow(String group, Object name, Object value, Object status)
    {
        Map<String, Object> row = row(name, value, status);
        row.put("groupKey", group);
        row.put("groupLabel", serviceGroupLabel(group));
        row.put("groupOrder", serviceGroupOrder(group));
        return row;
    }

    private String serviceGroupLabel(String group)
    {
        if ("install".equals(group)) return "装机履历";
        if ("removal".equals(group)) return "拆卸复查";
        if ("usage".equals(group)) return "使用量";
        if ("work_order".equals(group)) return "维修工单";
        if ("feedback".equals(group)) return "巡检反馈";
        return "其他服役数据";
    }

    private int serviceGroupOrder(String group)
    {
        if ("install".equals(group)) return 10;
        if ("usage".equals(group)) return 20;
        if ("work_order".equals(group)) return 30;
        if ("removal".equals(group)) return 40;
        if ("feedback".equals(group)) return 50;
        return 90;
    }

    private Map<String, Object> faultGroupedRow(String group, Object name, Object value, Object status)
    {
        Map<String, Object> row = row(name, value, status);
        row.put("groupKey", group);
        row.put("groupLabel", faultGroupLabel(group));
        row.put("groupOrder", faultGroupOrder(group));
        return row;
    }

    private String faultGroupLabel(String group)
    {
        if ("fault".equals(group)) return "故障事件";
        if ("analysis".equals(group)) return "分析结果";
        return "其他故障数据";
    }

    private int faultGroupOrder(String group)
    {
        if ("fault".equals(group)) return 10;
        if ("analysis".equals(group)) return 20;
        return 90;
    }

    private String cleanRecordSummary(Object summary, Object recordKey, Object recordId)
    {
        String value = text(summary);
        if (!hasText(value))
        {
            return "";
        }
        if (value.equalsIgnoreCase(text(recordKey)) || value.equalsIgnoreCase(text(recordId)))
        {
            return "";
        }
        return value;
    }

    private Map<String, Object> manufacturingRow(Map<String, Object> item)
    {
        Map<String, Object> attrs = castMap(item.get("attrs"));
        String table = logicalSourceTable(defaultText(attrs.get("actualSourceTable"), item.get("sourceTable")));
        if ("shop_order".equals(table))
        {
            return manufacturingGroupedRow(table,
                    "制造工单：" + defaultText(attr(attrs, "orderCode", "order_code"), item.get("sourceRecordKey")),
                    joinParts(Arrays.asList(valueText("状态", readableStatus(attr(attrs, "status"))),
                            valueText("路线", attr(attrs, "routeCode", "route_code")),
                            timeRange(attr(attrs, "actualStart", "actual_start"),
                                    attr(attrs, "actualFinish", "actual_finish")),
                            text(attr(attrs, "remarks")))),
                    defaultText(item.get("completenessStatus"), item.get("itemStatus")));
        }
        if ("process_route".equals(table))
        {
            return manufacturingGroupedRow(table, "工艺路线：" + defaultText(attr(attrs, "routeName", "route_name"),
                    attr(attrs, "routeCode", "route_code")),
                    joinParts(Arrays.asList(valueText("编号", attr(attrs, "routeCode", "route_code")),
                            valueText("版本", attr(attrs, "routeVersion", "route_version")),
                            valueText("标准工时", attr(attrs, "totalStandardHours", "total_standard_hours")),
                            valueText("状态", readableStatus(attr(attrs, "status"))),
                            text(attr(attrs, "remarks")))),
                    defaultText(item.get("completenessStatus"), item.get("itemStatus")));
        }
        if ("shop_order_task".equals(table))
        {
            return manufacturingGroupedRow(table,
                    "制造任务：" + defaultText(attr(attrs, "taskCode", "task_code"), item.get("sourceRecordKey")),
                    joinParts(Arrays.asList(valueText("工序", attr(attrs, "stepCode", "step_code")),
                            valueText("状态", readableStatus(attr(attrs, "status"))),
                            valueText("设备", attr(attrs, "assignedEquipmentId", "assigned_equipment_id",
                                    "equipmentId", "equipment_id")),
                            valueText("人员", attr(attrs, "assignedPersonnelId", "assigned_personnel_id",
                                    "operatorId", "operator_id")),
                            timeRange(attr(attrs, "actualStart", "actual_start"),
                                    attr(attrs, "actualFinish", "actual_finish")),
                            text(attr(attrs, "resultNotes", "result_notes")))),
                    defaultText(item.get("completenessStatus"), item.get("itemStatus")));
        }
        if ("production_operation_record".equals(table))
        {
            Map<String, Object> params = parseJsonObject(attr(attrs, "actualParams", "actual_params"));
            String processName = defaultText(attr(params, "processName", "process_name"),
                    defaultText(item.get("sourceRecordKey"), item.get("itemName")));
            return manufacturingGroupedRow(table, "加工记录：" + processName,
                    joinParts(Arrays.asList(valueText("人员", attr(attrs, "operatorId", "operator_id")),
                            valueText("设备", attr(attrs, "equipmentId", "equipment_id")),
                            timeRange(attr(attrs, "startTime", "start_time"), attr(attrs, "endTime", "end_time")),
                            valueText("结论", defaultText(attr(params, "conclusion", "result"),
                                    attr(attrs, "result", "status"))),
                            valueText("推荐设备", attr(params, "recommendedDevice", "recommended_device")),
                            text(attr(attrs, "remarks")))),
                    defaultText(item.get("completenessStatus"), item.get("itemStatus")));
        }
        if ("material_lot_trace".equals(table))
        {
            return manufacturingGroupedRow(table, "材料批次：" + defaultText(attr(attrs, "materialPn", "material_pn"),
                    item.get("sourceRecordKey")),
                    joinParts(Arrays.asList(valueText("批号", attr(attrs, "lotNumber", "lot_number",
                            "batchNumber", "batch_number")),
                            valueText("证书", attr(attrs, "millCertNumber", "mill_cert_number",
                                    "certificateNo", "certificate_no")),
                            valueText("供应商", attr(attrs, "supplier", "supplierName")),
                            valueText("用量", quantityWithUnit(attrs)))),
                    defaultText(item.get("completenessStatus"), item.get("itemStatus")));
        }
        if ("quality_characteristic".equals(table))
        {
            return manufacturingGroupedRow(table, "质量特性：" + defaultText(attr(attrs, "characteristicName", "characteristic_name"),
                    attr(attrs, "characteristicCode", "characteristic_code")),
                    joinParts(Arrays.asList(valueText("目标", attr(attrs, "nominalValue", "nominal_value")),
                            valueText("范围", limitRange(attrs)),
                            valueText("方法", attr(attrs, "inspectionMethod", "inspection_method")),
                            valueText("风险", attr(attrs, "riskLevel", "risk_level")))),
                    defaultText(item.get("completenessStatus"), item.get("itemStatus")));
        }
        if ("manufacturing_anomaly".equals(table))
        {
            return manufacturingGroupedRow(table, "制造异常：" + defaultText(attr(attrs, "anomalyNumber", "anomaly_number"),
                    item.get("sourceRecordKey")),
                    joinParts(Arrays.asList(text(attr(attrs, "anomalyName", "anomaly_name", "title")),
                            valueText("状态", readableStatus(attr(attrs, "status"))),
                            valueText("处置", attr(attrs, "resolution", "resolutionAction",
                                    "resolution_action")))),
                    defaultText(item.get("completenessStatus"), item.get("itemStatus")));
        }
        if ("nonconformance_record".equals(table))
        {
            return manufacturingGroupedRow(table,
                    "问题闭环：" + defaultText(attr(attrs, "ncNumber", "nc_number"), item.get("sourceRecordKey")),
                    joinParts(Arrays.asList(text(attr(attrs, "ncTitle", "nc_title", "title")),
                            valueText("状态", readableStatus(attr(attrs, "status"))),
                            valueText("处置", attr(attrs, "disposition", "dispositionResult",
                                    "disposition_result")),
                            valueText("关闭", attr(attrs, "closedAt", "closed_at")))),
                    defaultText(item.get("completenessStatus"), item.get("itemStatus")));
        }
        if ("release_record".equals(table))
        {
            return manufacturingGroupedRow(table, "制造放行：" + defaultText(attr(attrs, "releaseNumber", "release_number"),
                    item.get("sourceRecordKey")),
                    joinParts(Arrays.asList(valueText("状态", readableStatus(attr(attrs, "releaseStatus",
                            "release_status", "status"))),
                            valueText("放行人", attr(attrs, "releasedBy", "released_by")),
                            valueText("时间", attr(attrs, "releasedAt", "released_at")),
                            valueText("依据", attr(attrs, "releaseBasis", "release_basis")))),
                    defaultText(item.get("completenessStatus"), item.get("itemStatus")));
        }
        return manufacturingGroupedRow(table, defaultText(item.get("itemName"), item.get("itemCode")),
                defaultText(item.get("contentSummary"), item.get("sourceRecordKey")),
                defaultText(item.get("completenessStatus"), item.get("itemStatus")));
    }

    private Map<String, Object> inspectionRow(Map<String, Object> item)
    {
        Map<String, Object> attrs = castMap(item.get("attrs"));
        String table = logicalSourceTable(defaultText(attrs.get("actualSourceTable"), item.get("sourceTable")));
        if ("inspection_record".equals(table))
        {
            Map<String, Object> values = parseJsonObject(attr(attrs, "measurementValues", "measurement_values"));
            Object result = defaultText(attr(attrs, "result", "resultStatus", "result_status"),
                    item.get("itemStatus"));
            return inspectionGroupedRow(table,
                    "检验单：" + defaultText(attr(attrs, "inspectionType", "inspection_type"),
                            defaultText(item.get("itemName"), item.get("sourceRecordKey"))),
                    joinParts(Arrays.asList(
                            valueText("规范", attr(attrs, "inspectionStdDoc", "inspection_std_doc")),
                            valueText("结论", readableStatus(result)),
                            valueText("日期", attr(attrs, "inspectionDate", "inspection_date", "eventTime",
                                    "event_time")),
                            valueText("检验员", attr(attrs, "inspectorId", "inspector_id")),
                            inspectionMeasurementValues(values),
                            cleanInspectionSummary(item.get("contentSummary")))),
                    defaultText(item.get("completenessStatus"), item.get("itemStatus")), result);
        }
        if ("inspection_measurement".equals(table))
        {
            Object result = attr(attrs, "resultFlag", "result_flag");
            return inspectionGroupedRow(table,
                    "测量项：" + defaultText(attr(attrs, "indicatorName", "indicator_name"),
                            defaultText(item.get("itemName"), attr(attrs, "indicatorCode", "indicator_code"))),
                    joinParts(Arrays.asList(
                            valueText("实测", measuredValueWithUnit(attrs)),
                            valueText("名义值", valueWithUnit(attr(attrs, "nominalValue", "nominal_value"),
                                    attr(attrs, "unit"))),
                            valueText("允许范围", limitRange(attrs)),
                            valueText("结论", readableStatus(result)),
                            valueText("缺陷", attr(attrs, "defectCode", "defect_code", "defectLevel",
                                    "defect_level")),
                            cleanMeasurementSummary(item.get("contentSummary")))),
                    defaultText(item.get("completenessStatus"), item.get("itemStatus")), result);
        }
        return inspectionGroupedRow(table, defaultText(item.get("itemName"), item.get("itemCode")),
                defaultText(item.get("contentSummary"), item.get("sourceRecordKey")),
                defaultText(item.get("completenessStatus"), item.get("itemStatus")), item.get("itemStatus"));
    }

    private Map<String, Object> inspectionGroupedRow(String table, Object name, Object value, Object status,
            Object inspectionResult)
    {
        Map<String, Object> row = row(name, value, status);
        row.put("groupKey", inspectionGroupKey(table, inspectionResult));
        row.put("groupLabel", inspectionGroupLabel(table, inspectionResult));
        row.put("groupOrder", inspectionGroupOrder(table, inspectionResult));
        return row;
    }

    private String inspectionGroupKey(String table, Object inspectionResult)
    {
        String result = text(inspectionResult).toUpperCase();
        if (result.contains("FAIL") || result.contains("CONCESSION") || result.contains("WARNING"))
        {
            return "issue";
        }
        if ("inspection_record".equals(table))
        {
            return "record";
        }
        if ("inspection_measurement".equals(table))
        {
            return "measurement";
        }
        if (table.contains("file") || table.contains("document"))
        {
            return "evidence";
        }
        return "other";
    }

    private String inspectionGroupLabel(String table, Object inspectionResult)
    {
        String group = inspectionGroupKey(table, inspectionResult);
        if ("record".equals(group)) return "检验记录";
        if ("measurement".equals(group)) return "测量明细";
        if ("issue".equals(group)) return "问题与复验";
        if ("evidence".equals(group)) return "证明附件";
        return "其他检验数据";
    }

    private int inspectionGroupOrder(String table, Object inspectionResult)
    {
        String group = inspectionGroupKey(table, inspectionResult);
        if ("record".equals(group)) return 10;
        if ("measurement".equals(group)) return 20;
        if ("issue".equals(group)) return 30;
        if ("evidence".equals(group)) return 40;
        return 90;
    }

    private Map<String, Object> manufacturingGroupedRow(String table, Object name, Object value, Object status)
    {
        Map<String, Object> row = row(name, value, status);
        row.put("groupKey", manufacturingGroupKey(table));
        row.put("groupLabel", manufacturingGroupLabel(table));
        row.put("groupOrder", manufacturingGroupOrder(table));
        return row;
    }

    private String manufacturingGroupKey(String table)
    {
        if ("shop_order".equals(table) || "process_route".equals(table))
        {
            return "overview";
        }
        if ("material_lot_trace".equals(table))
        {
            return "material";
        }
        if ("shop_order_task".equals(table) || "production_operation_record".equals(table)
                || "assembly_record".equals(table))
        {
            return "operation";
        }
        if ("quality_characteristic".equals(table) || "inspection_record".equals(table)
                || "inspection_measurement".equals(table))
        {
            return "quality";
        }
        if ("manufacturing_anomaly".equals(table) || "nonconformance_record".equals(table))
        {
            return "issue";
        }
        if ("release_record".equals(table))
        {
            return "release";
        }
        return "other";
    }

    private String manufacturingGroupLabel(String table)
    {
        String group = manufacturingGroupKey(table);
        if ("overview".equals(group)) return "制造概况";
        if ("material".equals(group)) return "来料与材料";
        if ("operation".equals(group)) return "任务与加工";
        if ("quality".equals(group)) return "质量检测";
        if ("issue".equals(group)) return "异常与闭环";
        if ("release".equals(group)) return "制造放行";
        return "其他记录";
    }

    private int manufacturingGroupOrder(String table)
    {
        String group = manufacturingGroupKey(table);
        if ("overview".equals(group)) return 10;
        if ("material".equals(group)) return 20;
        if ("operation".equals(group)) return 30;
        if ("quality".equals(group)) return 40;
        if ("issue".equals(group)) return 50;
        if ("release".equals(group)) return 60;
        return 90;
    }

    private String logicalSourceTable(Object sourceTable)
    {
        String table = canonicalSourceTable(sourceTable);
        return table.startsWith("t1_") ? table.substring(3) : table;
    }

    private Object attr(Map<String, Object> attrs, String... fields)
    {
        for (String field : fields)
        {
            Object value = lookupAttr(attrs, field);
            if (hasText(value))
            {
                return value;
            }
        }
        return null;
    }

    private String quantityWithUnit(Map<String, Object> attrs)
    {
        Object quantity = attr(attrs, "quantityUsed", "quantity_used", "quantity", "usedQuantity",
                "used_quantity");
        if (!hasText(quantity))
        {
            return "";
        }
        return text(quantity) + text(attr(attrs, "unit", "uom"));
    }

    private String measuredValueWithUnit(Map<String, Object> attrs)
    {
        return valueWithUnit(attr(attrs, "measuredValue", "measured_value"), attr(attrs, "unit"));
    }

    private String valueWithUnit(Object value, Object unit)
    {
        if (!hasText(value))
        {
            return "";
        }
        return text(value) + text(unit);
    }

    private String limitRange(Map<String, Object> attrs)
    {
        String lower = text(attr(attrs, "lowerLimit", "lower_limit"));
        String upper = text(attr(attrs, "upperLimit", "upper_limit"));
        String unit = text(attr(attrs, "unit"));
        if (!hasText(lower) && !hasText(upper))
        {
            return "";
        }
        return lower + " - " + upper + unit;
    }

    private String inspectionMeasurementValues(Map<String, Object> values)
    {
        if (values.isEmpty())
        {
            return "";
        }
        return joinParts(Arrays.asList(
                valueText("项目", attr(values, "item", "stepName", "step_name", "kind")),
                valueText("标准", attr(values, "standard")),
                valueText("实测", attr(values, "actual")),
                valueText("保压压力", valueWithUnit(attr(values, "holdPressureMpa", "hold_pressure_mpa"),
                        "MPa")),
                valueText("泄漏率", valueWithUnit(attr(values, "leakRateMlMin", "leak_rate_ml_min"),
                        "mL/min")),
                valueText("复核说明", attr(values, "reviewNote", "review_note"))));
    }

    private String cleanInspectionSummary(Object value)
    {
        String summary = text(value);
        if (!hasText(summary))
        {
            return "";
        }
        return summary.replace("结果：PASS", "结果：合格")
                .replace("结果：FAIL", "结果：不合格")
                .replace("结果：CONCESSION", "结果：让步接收");
    }

    private String cleanMeasurementSummary(Object value)
    {
        String summary = cleanInspectionSummary(value);
        if (!hasText(summary))
        {
            return "";
        }
        summary = summary.replace("实测：-，上限：-，下限：-，", "")
                .replace("结论：PASS，", "结论：合格；")
                .replace("结论：FAIL，", "结论：不合格；")
                .replace("结论：WARNING，", "结论：需关注；");
        return summary;
    }

    private String timeRange(Object start, Object finish)
    {
        if (!hasText(start) && !hasText(finish))
        {
            return "";
        }
        if (!hasText(start))
        {
            return "完成：" + text(finish);
        }
        if (!hasText(finish))
        {
            return "开始：" + text(start);
        }
        return text(start) + " 至 " + text(finish);
    }

    private String valueText(String label, Object value)
    {
        return hasText(value) ? label + "：" + text(value) : "";
    }

    private String joinParts(List<String> parts)
    {
        List<String> result = new ArrayList<>();
        for (String part : parts)
        {
            if (hasText(part))
            {
                result.add(part);
            }
        }
        return result.isEmpty() ? "-" : String.join("；", result);
    }

    private String readableStatus(Object value)
    {
        String status = text(value);
        switch (status.toUpperCase())
        {
            case "CLOSED":
                return "已归档";
            case "COMPLETED":
                return "已完成";
            case "ACTIVE":
                return "有效";
            case "PASS":
            case "PASSED":
                return "合格";
            case "FAIL":
            case "FAILED":
                return "不合格";
            default:
                return status;
        }
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
            List<Map<String, Object>> items = filterContentItems(effectiveDirectoryCategory(directory),
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
            String stage = text(item.get("lifecycleStage")).toUpperCase();
            String itemType = text(item.get("itemType")).toLowerCase();
            String sourceTable = text(item.get("sourceTable")).toLowerCase();
            String itemChapterId = contentItemChapterId(item);
            if (hasDirectoryChapter && hasText(itemChapterId))
            {
                if (chapterId.equals(itemChapterId))
                {
                    if (matchesDirectoryFilters(sourceTableSet, lifecycleStageSet, stage, sourceTable))
                    {
                        exact.add(item);
                    }
                }
                continue;
            }
            if (hasDirectoryChapter && !isDirectoryFallbackCandidate(item))
            {
                continue;
            }
            if ("installation".equals(category) && matchesCategory(category, stage, itemType, sourceTable))
            {
                fallback.add(item);
                continue;
            }
            if (!sourceTableSet.isEmpty() && !lifecycleStageSet.isEmpty())
            {
                if (sourceTableMatches(sourceTableSet, sourceTable) && lifecycleStageSet.contains(stage))
                {
                    fallback.add(item);
                }
                continue;
            }
            if (!sourceTableSet.isEmpty() && sourceTableMatches(sourceTableSet, sourceTable))
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
        return exact.isEmpty() ? fallback : mergeDetailedFallbackItems(exact, fallback);
    }

    private List<Map<String, Object>> mergeDetailedFallbackItems(List<Map<String, Object>> exact,
            List<Map<String, Object>> fallback)
    {
        if (fallback.isEmpty())
        {
            return exact;
        }
        List<Map<String, Object>> result = new ArrayList<>(exact);
        for (Map<String, Object> item : fallback)
        {
            if (!hasInformativeContent(item))
            {
                continue;
            }
            String key = contentItemMergeKey(item);
            int existingIndex = findContentItemIndex(result, key);
            if (existingIndex < 0)
            {
                result.add(item);
                continue;
            }
            Map<String, Object> existing = result.get(existingIndex);
            if (contentInfoScore(item) > contentInfoScore(existing))
            {
                result.set(existingIndex, item);
            }
        }
        return result;
    }

    private int findContentItemIndex(List<Map<String, Object>> items, String key)
    {
        for (int i = 0; i < items.size(); i++)
        {
            if (key.equals(contentItemMergeKey(items.get(i))))
            {
                return i;
            }
        }
        return -1;
    }

    private String contentItemMergeKey(Map<String, Object> item)
    {
        String table = logicalSourceTable(item.get("sourceTable"));
        String record = defaultText(item.get("sourceRecordId"), item.get("sourceRecordKey"));
        if (hasText(record))
        {
            return table + "|" + record;
        }
        return table + "|" + text(item.get("itemName")) + "|" + text(item.get("contentSummary"));
    }

    private boolean hasInformativeContent(Map<String, Object> item)
    {
        return contentInfoScore(item) > 0;
    }

    private int contentInfoScore(Map<String, Object> item)
    {
        int score = 0;
        String summary = text(item.get("contentSummary"));
        if (hasText(summary) && !summary.equalsIgnoreCase(text(item.get("sourceRecordKey")))
                && !summary.equalsIgnoreCase(text(item.get("sourceRecordId"))))
        {
            score += summary.length();
        }
        Map<String, Object> attrs = castMap(item.get("attrs"));
        for (Map.Entry<String, Object> entry : attrs.entrySet())
        {
            if (hasText(text(entry.getValue())) && !"chapterId".equalsIgnoreCase(entry.getKey()))
            {
                score += 5;
            }
        }
        return score;
    }

    private boolean matchesDirectoryFilters(Set<String> sourceTableSet, Set<String> lifecycleStageSet, String stage,
            String sourceTable)
    {
        if (!sourceTableSet.isEmpty() && !sourceTableMatches(sourceTableSet, sourceTable))
        {
            return false;
        }
        if (!lifecycleStageSet.isEmpty() && !lifecycleStageSet.contains(stage))
        {
            return false;
        }
        return true;
    }

    private String effectiveDirectoryCategory(Map<String, Object> directory)
    {
        String category = text(directory.get("category"));
        if (isInstallationDirectory(directory))
        {
            return "installation";
        }
        return category;
    }

    private boolean isInstallationDirectory(Map<String, Object> directory)
    {
        Map<String, Object> attrs = castMap(directory.get("attrs"));
        List<String> fields = toStringList(directory.get("primaryFields"));
        if (fields.isEmpty())
        {
            fields = toStringList(attrs.get("primaryFields"));
        }
        for (String field : fields)
        {
            String normalized = text(field).toLowerCase();
            if ("install_date".equals(normalized) || "installdate".equals(normalized)
                    || "torque_n_m".equals(normalized) || "torquenm".equals(normalized)
                    || "leak_check_result".equals(normalized) || "leakcheckresult".equals(normalized))
            {
                return true;
            }
        }
        Set<String> sourceTables = lowerSet(toStringList(directory.get("sourceTables")));
        return sourceTableMatches(sourceTables, "t1_assembly_record");
    }

    private boolean sourceTableMatches(Set<String> expectedTables, String actualTable)
    {
        String table = text(actualTable).toLowerCase();
        if (expectedTables.contains(table))
        {
            return true;
        }
        String logicalTable = table.startsWith("t1_") ? table.substring(3) : table;
        if (expectedTables.contains(logicalTable))
        {
            return true;
        }
        return expectedTables.contains("t1_" + logicalTable);
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
        if ("installation".equals(category))
        {
            return "INSTALLATION".equals(stage) || sourceTable.contains("assembly") || sourceTable.contains("install");
        }
        if ("inspection".equals(category))
        {
            return "INSPECTION".equals(stage)
                    || (sourceTable.contains("inspection") || itemType.contains("inspection"))
                            && !"MANUFACTURING".equals(stage) && !"INSTALLATION".equals(stage);
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
        if (containsAny(label, "装机"))
        {
            return "installation";
        }
        if (containsAny(label, "服役", "使用", "履历", "维修"))
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

    private String toPrettyJson(Object value)
    {
        try
        {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        }
        catch (Exception e)
        {
            return text(value);
        }
    }

    private String toCompactJson(Object value)
    {
        try
        {
            return OBJECT_MAPPER.writeValueAsString(value);
        }
        catch (Exception e)
        {
            return text(value);
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

    private String firstText(Object... values)
    {
        if (values == null)
        {
            return "";
        }
        for (Object value : values)
        {
            if (hasText(value))
            {
                return text(value);
            }
        }
        return "";
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
