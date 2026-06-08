package com.ruoyi.project1.dossier.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private Map<String, Object> buildDetailData(Map<String, Object> instance, Map<String, Object> version,
            Map<String, Object> selectedNode)
    {
        Map<String, Object> result = map();
        result.put("context", buildContext(instance, version));
        result.put("metrics", buildMetrics(version));
        result.put("versions", normalizeRows(detailMapper.selectVersionList(text(instance.get("instanceId")))));
        result.put("filePackages", buildFilePackages(version));
        result.put("structureNodes", normalizeStructureNodes(detailMapper.selectStructureNodes(text(version.get("versionId")))));
        result.put("allDocuments", normalizeDocuments(detailMapper.selectAllDocuments(text(version.get("versionId")))));
        result.put("dataSources", normalizeRows(detailMapper.selectDataSources(text(version.get("versionId")))));
        result.put("operationLogs", normalizeRows(detailMapper.selectOperationLogs(text(instance.get("instanceId")),
                text(version.get("versionId")))));
        result.putAll(buildNodePayload(instance, version, selectedNode));
        return result;
    }

    private Map<String, Object> buildNodePayload(Map<String, Object> instance, Map<String, Object> version,
            Map<String, Object> node)
    {
        Map<String, Object> bomNode = normalizeBomNode(node);
        String versionId = text(version.get("versionId"));
        String bomNodeId = text(bomNode.get("nodeId"));
        Map<String, Object> structureNode = detailMapper.selectStructureNodeByBom(versionId, bomNodeId);
        if (structureNode != null)
        {
            parseJsonField(structureNode, "attrsJson", "attrs");
            parseJsonField(structureNode, "sourceTraceJson", "sourceTrace");
        }

        List<Map<String, Object>> contentItems = normalizeContentItems(detailMapper.selectContentItems(versionId, bomNodeId));
        String structureNodeId = structureNode == null ? null : text(structureNode.get("structureNodeId"));
        List<Map<String, Object>> documents = normalizeDocuments(detailMapper.selectDocuments(versionId, structureNodeId));
        if (documents.isEmpty() && "HYD-TUBE-MLG-32A".equals(text(bomNode.get("partNumber"))))
        {
            documents = normalizeDocuments(detailMapper.selectAllDocuments(versionId));
        }

        Map<String, Object> result = map();
        result.put("currentNode", bomNode);
        result.put("bomPath", selectBomPath(bomNodeId, versionId));
        result.put("bomChildren", selectBomChildren(text(instance.get("aircraftId")), bomNodeId,
                text(instance.get("instanceId")), versionId));
        result.put("directory", buildDirectory(text(bomNode.get("objectLevel"))));
        result.put("structureNode", structureNode);
        result.put("contentItems", contentItems);
        result.put("documents", documents);
        result.put("detail", buildNodeDetail(bomNode, contentItems, documents));
        return result;
    }

    private Map<String, Object> buildContext(Map<String, Object> instance, Map<String, Object> version)
    {
        Map<String, Object> context = map();
        context.putAll(instance);
        context.putAll(version);
        context.put("createdAt", displayTime(version.get("createdAt")));
        context.put("publishedAt", displayTime(version.get("publishedAt")));
        parseJsonField(context, "contentSummaryJson", "contentSummary");
        parseJsonField(context, "outputJson", "output");
        parseJsonField(context, "generationParamsJson", "generationParams");
        return context;
    }

    private Map<String, Object> buildMetrics(Map<String, Object> version)
    {
        Map<String, Object> summary = parseJsonObject(version.get("contentSummaryJson"));
        Map<String, Object> checkSummary = castMap(summary.get("checkSummary"));
        Map<String, Object> output = parseJsonObject(version.get("outputJson"));
        Map<String, Object> bomMetrics = detailMapper.selectStructureBomMetrics(text(version.get("versionId")));
        if (bomMetrics == null)
        {
            bomMetrics = map();
        }

        Map<String, Object> metrics = map();
        metrics.put("pageCount", defaultNumber(summary.get("pageCount"), output.get("pageCount"), 186));
        metrics.put("fileCount", defaultNumber(summary.get("fileCount"), output.get("fileCount"), 36));
        metrics.put("sourceRecordCount", defaultNumber(summary.get("sourceRecordCount"), output.get("sourceRecordCount"), 182));
        metrics.put("warningCount", defaultNumber(checkSummary.get("warningCount"), 3));
        metrics.put("blockingCount", defaultNumber(checkSummary.get("blockingCount"), 0));
        metrics.put("bomNodeCount", defaultNumber(bomMetrics.get("totalCount"), 0));
        metrics.put("systemCount", defaultNumber(bomMetrics.get("systemCount"), 0));
        metrics.put("subsystemCount", defaultNumber(bomMetrics.get("subsystemCount"), 0));
        metrics.put("equipmentCount", defaultNumber(bomMetrics.get("equipmentCount"), 0));
        metrics.put("partCount", defaultNumber(bomMetrics.get("partCount"), 0));
        metrics.put("tubeRecordCount", 48);
        metrics.put("completenessRate", "92%");
        return metrics;
    }

    private List<Map<String, Object>> buildFilePackages(Map<String, Object> version)
    {
        Map<String, Object> output = parseJsonObject(version.get("outputJson"));
        String versionLabel = defaultText(version.get("versionLabel"), "V1.0");
        String baseName = "B-1234_综合卷宗_" + versionLabel;
        List<Map<String, Object>> files = new ArrayList<>();
        files.add(file("PDF", baseName + ".pdf", defaultText(output.get("fileSize"), "42.8 MB"),
                defaultNumber(output.get("pageCount"), 186) + " 页"));
        files.add(file("ZIP", baseName + ".zip", "128.6 MB", defaultNumber(output.get("fileCount"), 36) + " 文件"));
        files.add(file("JSON", "dossier_structure_node_snapshot.json", "2.4 MB", "BOM节点快照"));
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

    private Map<String, Object> buildDirectory(String objectLevel)
    {
        List<String> labels;
        String title;
        if ("system".equals(objectLevel))
        {
            title = "系统目录";
            labels = Arrays.asList("系统概况", "系统结构", "接口关系", "系统设计数据", "技术状态", "相关故障", "维修记录", "附件材料");
        }
        else if ("subsystem".equals(objectLevel))
        {
            title = "子系统目录";
            labels = Arrays.asList("子系统概况", "子系统结构", "接口关系", "组成设备", "子系统设计数据", "技术状态", "相关故障", "维修记录", "附件材料");
        }
        else if ("equipment".equals(objectLevel))
        {
            title = "设备目录";
            labels = Arrays.asList("设备基本信息", "组成结构", "设计数据", "制造数据", "装配记录", "检验记录", "装机履历", "故障维修", "技术变更", "附件材料");
        }
        else if ("component".equals(objectLevel))
        {
            title = "组件目录";
            labels = Arrays.asList("组件基本信息", "组成零件", "设计数据", "制造追溯", "检验记录", "装配关系", "装机履历", "服役记录", "故障维修", "证明附件");
        }
        else if ("part".equals(objectLevel))
        {
            title = "零件目录";
            labels = Arrays.asList("零件基本信息", "设计数据", "制造追溯", "检验记录", "装机履历", "服役维修", "故障记录", "证明附件");
        }
        else
        {
            title = "整机目录";
            labels = Arrays.asList("飞机基本信息", "构型 / BOM", "设计数据", "制造数据", "服役数据", "故障维修", "技术状态", "附件材料");
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++)
        {
            Map<String, Object> item = map();
            item.put("key", directoryKey(labels.get(i), i));
            item.put("orderNo", String.format("%02d", i + 1));
            item.put("label", labels.get(i));
            item.put("status", i == 0 || isCompositionLabel(labels.get(i)) ? "complete" : "normal");
            rows.add(item);
        }

        Map<String, Object> directory = map();
        directory.put("title", title);
        directory.put("rows", rows);
        directory.put("count", rows.size());
        return directory;
    }

    private String directoryKey(String label, int index)
    {
        if (label.contains("BOM"))
        {
            return "bom";
        }
        if (isCompositionLabel(label))
        {
            return "composition";
        }
        if (label.contains("基本") || label.contains("概况"))
        {
            return "basic";
        }
        if (label.contains("设计"))
        {
            return "design";
        }
        if (label.contains("制造") || label.contains("检验") || label.contains("装配"))
        {
            return "manufacturing_" + index;
        }
        if (label.contains("服役") || label.contains("使用") || label.contains("履历"))
        {
            return "service_" + index;
        }
        if (label.contains("故障") || label.contains("维修"))
        {
            return "fault_" + index;
        }
        if (label.contains("附件") || label.contains("证明"))
        {
            return "document_" + index;
        }
        return "section_" + index;
    }

    private boolean isCompositionLabel(String label)
    {
        return label.contains("BOM") || label.contains("结构") || label.contains("组成");
    }

    private Map<String, Object> buildNodeDetail(Map<String, Object> node, List<Map<String, Object>> contentItems,
            List<Map<String, Object>> documents)
    {
        String partNumber = text(node.get("partNumber"));
        Map<String, Object> attrs = contentItems.isEmpty() ? map() : castMap(contentItems.get(0).get("attrs"));

        Map<String, Object> detail = map();
        detail.put("title", text(node.get("partName")));
        detail.put("subtitle", text(node.get("partNumber")) + valueSuffix(" / ", node.get("serialNumber")));
        detail.put("status", "有效");
        detail.put("objectLevel", node.get("objectLevel"));
        detail.put("levelName", node.get("levelName"));
        detail.put("basicFields", basicFields(node));
        detail.put("parameterCards", buildParameterCards(node, attrs));
        detail.put("tables", buildDetailTables(partNumber, node, attrs, documents));
        detail.put("contentSummary", contentItems.isEmpty() ? buildSummary(partNumber) : contentItems.get(0).get("contentSummary"));
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
        fields.add(field("ATA", defaultText(node.get("ataChapter"), "32")));
        fields.add(field("装机日期", defaultText(node.get("installDate"), "-")));
        fields.add(field("TSN / CSN", decimalText(node.get("tsnFh")) + " FH / " + defaultNumber(node.get("tsnFc"), 0) + " FC"));
        return fields;
    }

    private List<Map<String, Object>> buildParameterCards(Map<String, Object> node, Map<String, Object> attrs)
    {
        if (!"HYD-TUBE-MLG-32A".equals(text(node.get("partNumber"))))
        {
            return Arrays.asList(
                    field("直接子节点", defaultNumber(node.get("childCount"), 0)),
                    field("数据状态", "已纳入当前卷宗"),
                    field("来源", "t1_aircraft_bom_node"),
                    field("完整性", "已检查"));
        }
        return Arrays.asList(
                field("材料", "06Cr19Ni10 不锈钢无缝管"),
                field("外径 / 壁厚", "9.53 mm / 0.71 mm"),
                field("展开长度", "485 mm"),
                field("最小弯曲半径", "28.6 mm"),
                field("工作压力", "20.7 MPa"),
                field("压力试验", "31.5 MPa / 5 min"),
                field("清洁度", "NAS 1638 6 级"),
                field("TSN / CSN", decimalText(node.get("tsnFh")) + " FH / " + defaultNumber(node.get("tsnFc"), 0) + " FC"));
    }

    private List<Map<String, Object>> buildDetailTables(String partNumber, Map<String, Object> node,
            Map<String, Object> attrs, List<Map<String, Object>> documents)
    {
        List<Map<String, Object>> tables = new ArrayList<>();
        tables.add(table("基础信息", Arrays.asList(
                row("对象层级", node.get("levelName"), "有效"),
                row("业务编号", node.get("partNumber"), "有效"),
                row("装机位置", defaultText(node.get("positionCode"), "-"), "有效"),
                row("上级节点", defaultText(node.get("parentId"), "-"), "有效"))));

        if ("HYD-TUBE-MLG-32A".equals(partNumber))
        {
            tables.add(table("设计数据", Arrays.asList(
                    row("图纸编号", "DWG-HYD-TUBE-MLG-32A-Rev.C", "有效"),
                    row("技术条件", "TS-HYD-TUBE-06Cr19Ni10-2026", "有效"),
                    row("材料规范", "06Cr19Ni10 不锈钢无缝管 / AMS-5567 等效", "有效"),
                    row("外径 / 壁厚", "9.53 mm / 0.71 mm", "有效"),
                    row("展开长度", "485 mm，端口余量 12 mm", "有效"),
                    row("弯曲段", "B1 42.5° / B2 18.0° / B3 76.0° / B4 23.5° / B5 31.0° / B6 12.5°", "有效"),
                    row("最小弯曲半径", "28.6 mm，不小于 3D", "有效"),
                    row("额定工作压力", "20.7 MPa，Skydrol LD-4", "有效"))));
            tables.add(table("制造追溯", Arrays.asList(
                    row("制造工单", "MO-HYD-TUBE-202602-0042", "有效"),
                    row("材料炉批", "HT-L20260218-A / 入厂复验合格", "有效"),
                    row("设备编号", "CNC-BEND-06 / 弯管模具 R28.6-D9.53", "有效"),
                    row("工序路线", "下料 -> 去毛刺 -> 数控弯管 -> 端头扩口 -> 钝化 -> 清洗封存", "有效"),
                    row("生产日期", "2026-02-12 至 2026-02-15", "有效"),
                    row("标识追溯", "激光标识 HYD-TUBE-MLG-32A / HT-MLG-32A-2026-0042", "有效"))));
            tables.add(table("检验记录", Arrays.asList(
                    row("尺寸复验", "三坐标复验 16 项，最大偏差 0.18 mm", "通过"),
                    row("外观检查", "无压痕、划伤、折皱、裂纹", "通过"),
                    row("压力试验", "31.5 MPa / 5 min，无渗漏、无永久变形", "通过"),
                    row("清洁度", "NAS 1638 6 级，颗粒计数合格", "通过"),
                    row("端口保护", "双端堵盖齐套，封签完好", "通过"),
                    row("终检结论", "允许装配至 HYD-MLG-PKG-01", "通过"))));
            tables.add(table("装机履历", Arrays.asList(
                    row("装机飞机", "B-1234 / C919 / MSN-C919-1234", "有效"),
                    row("上级组件", "液压供压管路组件 HYD-MLG-PKG-01", "有效"),
                    row("装机位置", "MLG-HYD-STA / 左主起落架舱", "有效"),
                    row("装机日期", "2026-02-20，装机记录 INST-HYD-MLG-20260220-07", "有效"))));
            tables.add(table("服役维修", Arrays.asList(
                    row("累计使用", decimalText(node.get("tsnFh")) + " FH / " + defaultNumber(node.get("tsnFc"), 0) + " FC", "有效"),
                    row("最近例检", "2026-05-18 A 检，管路固定和渗漏检查正常", "有效"),
                    row("压力复核", "系统地面加压 20.7 MPa，无渗漏", "通过"),
                    row("维护动作", "端口区域清洁、卡箍力矩复核 8 处", "完成"),
                    row("下次检查", "1500 FH 或 1000 FC，以先到为准", "提醒"))));
            tables.add(table("故障记录", Arrays.asList(
                    row("未关闭故障", "0 条", "有效"),
                    row("历史事件", "1 条表面轻微擦伤复验记录，结论可用", "有效"),
                    row("关联维修", "MR-HYD-20260518-03，复验后继续使用", "完成"))));
            tables.add(table("证明附件", documentRows(documents)));
        }
        else if (isHydPackageChildPart(partNumber))
        {
            tables.addAll(buildHydPackageChildTables(partNumber, node));
        }
        else if ("HYD-MLG-PKG-01".equals(partNumber))
        {
            tables.add(table("组件组成", Arrays.asList(
                    row("供压弯管", "HYD-TUBE-MLG-32A / HT-MLG-32A-2026-0042", "有效"),
                    row("回油直管", "HYD-TUBE-MLG-32B / 2 件", "有效"),
                    row("三通接头", "HYD-FIT-TEE-01 / 3 件", "有效"),
                    row("固定卡箍", "HYD-CLAMP-MLG-01 / 8 件", "有效"))));
            tables.add(table("设计数据", Arrays.asList(
                    row("组件图纸", "DWG-HYD-MLG-PKG-01-Rev.B", "有效"),
                    row("管路清单", "BOM-HYD-MLG-PKG-01 / 14 项", "有效"),
                    row("工作介质", "Skydrol LD-4", "有效"),
                    row("额定压力", "20.7 MPa", "有效"),
                    row("接口标准", "37° 扩口接头 / NAS 1760 等效", "有效"),
                    row("清洁度要求", "NAS 1638 6 级", "有效"))));
            tables.add(table("制造追溯", Arrays.asList(
                    row("装配工单", "ASM-HYD-MLG-PKG-20260218", "有效"),
                    row("装配批次", "HYD-PKG-BATCH-20260218-L", "有效"),
                    row("关键零件批次", "HYD-TUBE-MLG-32A / HT-L20260218-A", "有效"),
                    row("清洗封存", "CLEAN-HYD-MLG-20260218，端口双层防护", "通过"),
                    row("装配人员", "A12 班组 / 复核 QA-HYD-07", "有效"))));
            tables.add(table("检验记录", Arrays.asList(
                    row("密封性试验", "31.5 MPa / 5 min，无压降异常", "通过"),
                    row("安装尺寸", "接口姿态、卡箍间距、管路间隙复核合格", "通过"),
                    row("清洁度检测", "颗粒计数满足 NAS 1638 6 级", "通过"),
                    row("标识检查", "件号、序列号、流向标识齐全", "通过"))));
            tables.add(table("装配关系", Arrays.asList(
                    row("上级设备", "主起液压供压设备 EQP-HYD-MLG-SUPPLY", "有效"),
                    row("安装区域", "左主起液压供压设备管路区域", "有效"),
                    row("接口关系", "连接主液压供压歧管与作动筒入口", "有效"),
                    row("固定方式", "8 处卡箍固定，力矩 6.8 N·m", "有效"))));
            tables.add(table("装机履历", Arrays.asList(
                    row("装机飞机", "B-1234 / C919", "有效"),
                    row("装机日期", "2026-02-20", "有效"),
                    row("装机记录", "INST-HYD-MLG-PKG-20260220-02", "有效"),
                    row("装机照片", "IMG-HYD-MLG-PKG-20260220 / 5 张", "有效"))));
            tables.add(table("服役记录", Arrays.asList(
                    row("累计使用", "1280.50 FH / 892 FC", "有效"),
                    row("最近检查", "2026-05-18 A 检，未见渗漏", "有效"),
                    row("系统压力", "地面加压 20.7 MPa，压力保持正常", "通过"),
                    row("维护建议", "下次 A 检复核卡箍和端口封严", "提醒"))));
            tables.add(table("故障维修", Arrays.asList(
                    row("未关闭故障", "0 条", "有效"),
                    row("历史故障", "0 条影响放行事件", "有效"),
                    row("维修记录", "MR-HYD-20260518-03，清洁与力矩复核完成", "完成"))));
            tables.add(table("证明附件", Arrays.asList(
                    row("组件图纸", "DWG-HYD-MLG-PKG-01.pdf", "齐套"),
                    row("密封性试验报告", "LEAK-HYD-MLG-20260218.pdf", "齐套"),
                    row("清洁度报告", "CLEAN-HYD-MLG-20260218.pdf", "齐套"),
                    row("装机照片包", "IMG-HYD-MLG-PKG-20260220.zip", "齐套"))));
        }
        else if ("EQP-HYD-MLG-SUPPLY".equals(partNumber))
        {
            tables.add(table("设备组成", Arrays.asList(
                    row("主起收放供压接口", "HYD-MLG-SUPPLY-A", "有效"),
                    row("液压供压管路组件", "HYD-MLG-PKG-01", "有效"),
                    row("供压压力监测点", "HYD-MLG-PT-01", "有效"))));
        }
        else if ("SUBSYS-HYD-MLG".equals(partNumber))
        {
            tables.add(table("子系统结构", Arrays.asList(
                    row("主起液压供压设备", "EQP-HYD-MLG-SUPPLY", "有效"),
                    row("主起液压回油设备", "EQP-HYD-MLG-RETURN", "有效"),
                    row("刹车与转弯液压接口", "HYD-BRK-STEER-IF", "有效"))));
        }
        else if ("SYS-29".equals(partNumber) || "SYS-32".equals(partNumber))
        {
            tables.add(table("系统结构", Arrays.asList(
                    row("起落架液压子系统", "SUBSYS-HYD-MLG", "有效"),
                    row("刹车液压子系统", "SUBSYS-HYD-BRK", "有效"),
                    row("转弯液压子系统", "SUBSYS-HYD-STEER", "有效"))));
        }
        else
        {
            tables.add(table("整机状态", Arrays.asList(
                    row("当前模板", "单台份飞机综合卷宗模板", "有效"),
                    row("BOM快照", "按当前有效构型读取", "有效"),
                    row("数据来源", "模板配置驱动", "有效"))));
        }
        return tables;
    }

    private boolean isHydPackageChildPart(String partNumber)
    {
        return "HYD-TUBE-MLG-32B".equals(partNumber)
                || "HYD-FIT-TEE-01".equals(partNumber)
                || "HYD-CLAMP-MLG-01".equals(partNumber)
                || "HYD-SEAL-032".equals(partNumber)
                || "HYD-SLEEVE-MLG-01".equals(partNumber);
    }

    private List<Map<String, Object>> buildHydPackageChildTables(String partNumber, Map<String, Object> node)
    {
        List<Map<String, Object>> tables = new ArrayList<>();
        if ("HYD-TUBE-MLG-32B".equals(partNumber))
        {
            tables.add(table("设计数据", Arrays.asList(
                    row("图纸编号", "DWG-HYD-TUBE-MLG-32B-Rev.A", "有效"),
                    row("材料规范", "06Cr19Ni10 不锈钢无缝管 / AMS-5567 等效", "有效"),
                    row("外径 / 壁厚", "9.53 mm / 0.71 mm", "有效"),
                    row("展开长度", "362 mm，端口余量 10 mm", "有效"),
                    row("弯曲段", "B1 35.0° / B2 22.5° / B3 18.0°", "有效"),
                    row("额定压力", "20.7 MPa，回油侧按同等级验证", "有效"))));
            tables.add(table("制造追溯", Arrays.asList(
                    row("制造工单", "MO-HYD-TUBE-202602-0051", "有效"),
                    row("材料炉批", "HT-L20260218-B / 入厂复验合格", "有效"),
                    row("加工设备", "CNC-BEND-06 / R24-D9.53 模具", "有效"),
                    row("工序路线", "下料 -> 数控弯管 -> 端头扩口 -> 清洗 -> 封存", "有效"),
                    row("生产数量", defaultText(node.get("quantity"), "2") + " " + defaultText(node.get("unit"), "EA"), "有效"))));
            tables.add(table("检验记录", Arrays.asList(
                    row("尺寸复验", "三坐标复验 12 项，最大偏差 0.16 mm", "通过"),
                    row("压力试验", "31.5 MPa / 5 min，无渗漏", "通过"),
                    row("清洁度", "NAS 1638 6 级", "通过"),
                    row("端口防护", "双端堵盖与封签齐套", "通过"))));
        }
        else if ("HYD-FIT-TEE-01".equals(partNumber))
        {
            tables.add(table("设计数据", Arrays.asList(
                    row("图纸编号", "DWG-HYD-FIT-TEE-01-Rev.B", "有效"),
                    row("材料规范", "15-5PH 不锈钢锻件 / AMS 5659", "有效"),
                    row("接口标准", "37° 扩口三通，NAS 1760 等效", "有效"),
                    row("通径", "DN06，三口一致", "有效"),
                    row("额定压力", "20.7 MPa / 证明压力 31.5 MPa", "有效"))));
            tables.add(table("制造追溯", Arrays.asList(
                    row("制造批次", "FIT-TEE-20260218-03", "有效"),
                    row("加工路线", "锻件验收 -> 数控车铣 -> 螺纹加工 -> 钝化 -> 清洗", "有效"),
                    row("热处理状态", "H1025，硬度 HRC 38-42", "有效"),
                    row("生产数量", defaultText(node.get("quantity"), "3") + " " + defaultText(node.get("unit"), "EA"), "有效"))));
            tables.add(table("检验记录", Arrays.asList(
                    row("螺纹量规", "三口 GO/NO-GO 检验合格", "通过"),
                    row("密封面", "无压痕、划伤和毛刺", "通过"),
                    row("耐压试验", "31.5 MPa / 5 min，无渗漏", "通过"),
                    row("清洁度", "端口颗粒计数满足 NAS 1638 6 级", "通过"))));
        }
        else if ("HYD-CLAMP-MLG-01".equals(partNumber))
        {
            tables.add(table("设计数据", Arrays.asList(
                    row("图纸编号", "DWG-HYD-CLAMP-MLG-01-Rev.A", "有效"),
                    row("材料规范", "17-4PH 卡箍体 + 氟硅橡胶衬垫", "有效"),
                    row("适配管径", "9.53 mm 管路，含防磨间隙", "有效"),
                    row("安装力矩", "6.8 N·m，允许偏差 ±0.5 N·m", "有效"),
                    row("环境要求", "-55°C 至 135°C，耐 Skydrol LD-4", "有效"))));
            tables.add(table("制造追溯", Arrays.asList(
                    row("制造批次", "CLAMP-MLG-20260218-08", "有效"),
                    row("表面处理", "钝化 + 防松标识漆", "有效"),
                    row("衬垫批次", "FSR-20260210-14", "有效"),
                    row("生产数量", defaultText(node.get("quantity"), "8") + " " + defaultText(node.get("unit"), "EA"), "有效"))));
            tables.add(table("检验记录", Arrays.asList(
                    row("外观检查", "卡箍边缘无毛刺，衬垫无裂纹", "通过"),
                    row("力矩复核", "8 处安装点复核合格", "通过"),
                    row("防松标记", "螺钉防松漆线连续", "通过"),
                    row("装配间隙", "管路与结构件间隙不小于 5 mm", "通过"))));
        }
        else if ("HYD-SEAL-032".equals(partNumber))
        {
            tables.add(table("设计数据", Arrays.asList(
                    row("规范编号", "SPEC-HYD-SEAL-032-Rev.A", "有效"),
                    row("材料", "氟橡胶 FKM，耐磷酸酯液压油", "有效"),
                    row("规格", "9.53 mm 扩口端密封圈", "有效"),
                    row("寿命要求", "装配后随管路检查周期复核", "有效"),
                    row("储存期限", "自制造日起 60 个月", "有效"))));
            tables.add(table("制造追溯", Arrays.asList(
                    row("供应批次", "SEAL-FKM-20260128-32", "有效"),
                    row("供应商", "AVIC Fluid Sealing Plant", "有效"),
                    row("入库复验", "尺寸、硬度、外观抽检合格", "通过"),
                    row("使用数量", defaultText(node.get("quantity"), "12") + " " + defaultText(node.get("unit"), "EA"), "有效"))));
            tables.add(table("检验记录", Arrays.asList(
                    row("外观", "无缺口、裂纹、压扁和污染", "通过"),
                    row("硬度", "75 Shore A，批次均值 74.8", "通过"),
                    row("装配确认", "端口密封圈数量和方向复核合格", "通过"))));
        }
        else
        {
            tables.add(table("设计数据", Arrays.asList(
                    row("图纸编号", "DWG-HYD-SLEEVE-MLG-01-Rev.A", "有效"),
                    row("材料", "PTFE 玻纤编织护套，耐磨阻燃", "有效"),
                    row("适配位置", "管路穿越结构边缘与卡箍邻近区", "有效"),
                    row("长度规格", "120 mm / 160 mm 两种裁切长度", "有效"))));
            tables.add(table("制造追溯", Arrays.asList(
                    row("供应批次", "SLEEVE-PTFE-20260205-04", "有效"),
                    row("裁切记录", "按装配图裁切 4 件，端部热封", "有效"),
                    row("入库复验", "阻燃标识、宽度和厚度抽检合格", "通过"),
                    row("使用数量", defaultText(node.get("quantity"), "4") + " " + defaultText(node.get("unit"), "EA"), "有效"))));
            tables.add(table("检验记录", Arrays.asList(
                    row("外观", "护套编织完整，无破损抽丝", "通过"),
                    row("安装覆盖", "覆盖潜在磨损区，边界不干涉活动件", "通过"),
                    row("防护效果", "与结构边缘最小间隙满足要求", "通过"))));
        }

        tables.add(table("装机履历", Arrays.asList(
                row("装机飞机", "B-1234 / C919", "有效"),
                row("上级组件", "液压供压管路组件 HYD-MLG-PKG-01", "有效"),
                row("装机位置", defaultText(node.get("positionCode"), "HYD-MLG-PKG"), "有效"),
                row("装机日期", defaultText(node.get("installDate"), "2026-02-20"), "有效"))));
        tables.add(table("服役维修", Arrays.asList(
                row("累计使用", decimalText(node.get("tsnFh")) + " FH / " + defaultNumber(node.get("tsnFc"), 0) + " FC", "有效"),
                row("最近例检", "2026-05-18 A 检，与组件一起完成目视和渗漏检查", "有效"),
                row("维护动作", "随 HYD-MLG-PKG-01 完成清洁、标识和固定状态复核", "完成"),
                row("下次检查", "下次 A 检随管路组件复核", "提醒"))));
        tables.add(table("故障记录", Arrays.asList(
                row("未关闭故障", "0 条", "有效"),
                row("影响放行事件", "0 条", "有效"),
                row("关联维修", "MR-HYD-20260518-03，检查结论可继续使用", "完成"))));
        tables.add(table("证明附件", Arrays.asList(
                row("图纸 / 规范", "随组件附件包归档", "齐套"),
                row("制造或供应批次证明", "批次证明已纳入 HYD-MLG-PKG-01 文件包", "齐套"),
                row("检验记录", "尺寸、清洁度和装配确认记录齐套", "齐套"))));
        return tables;
    }

    private String buildSummary(String partNumber)
    {
        if ("HYD-TUBE-MLG-32A".equals(partNumber))
        {
            return "液压弯管的设计参数、制造工序、检验试验、装机服役和证明附件均已纳入当前卷宗版本。";
        }
        if ("HYD-MLG-PKG-01".equals(partNumber))
        {
            return "液压供压管路组件的组成零件、装配追溯、检验试验、装机关系和服役记录均已纳入当前卷宗版本。";
        }
        if (isHydPackageChildPart(partNumber))
        {
            return "该零件属于液压供压管路组件，可查看设计、制造追溯、检验、装机服役和证明附件数据。";
        }
        return "当前节点已纳入整机综合卷宗，可按节点目录查看关联数据。";
    }

    private List<Map<String, Object>> documentRows(List<Map<String, Object>> documents)
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> document : documents)
        {
            rows.add(row(text(document.get("title")), text(document.get("sourceSystem")), text(document.get("completenessStatus"))));
        }
        return rows;
    }

    private List<Map<String, Object>> rowsFromList(Object value, String status)
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (value instanceof List)
        {
            int index = 1;
            for (Object item : (List<?>) value)
            {
                rows.add(row(String.format("%02d", index), item, status));
                index++;
            }
        }
        return rows;
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
        String partNumber = text(row.get("partNumber"));
        if ("HYD-MLG-PKG-01".equals(partNumber))
        {
            return "component";
        }
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
            if ("HYD-MLG-PKG-01".equals(text(item.get("code"))))
            {
                item.put("objectLevel", "component");
            }
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
            item.put("fileType", fileType(text(item.get("fileStorageKey"))));
        }
        return result;
    }

    private String fileType(String fileStorageKey)
    {
        String lower = fileStorageKey.toLowerCase();
        if (lower.endsWith(".pdf"))
        {
            return "PDF";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".png") || lower.endsWith(".jpeg"))
        {
            return "IMG";
        }
        if (lower.endsWith(".zip"))
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
    private Map<String, Object> castMap(Object value)
    {
        if (value instanceof Map)
        {
            return (Map<String, Object>) value;
        }
        return map();
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
