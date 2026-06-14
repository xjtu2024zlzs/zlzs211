package com.ruoyi.project1.dossier.service.impl;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.project1.dossier.mapper.DossierDetailMapper;
import com.ruoyi.project1.dossier.mapper.DossierInstanceMapper;
import com.ruoyi.project1.dossier.service.IDossierExportService;

@Service
public class DossierExportServiceImpl implements IDossierExportService
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static
    {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private static final SimpleDateFormat CODE_TIME = new SimpleDateFormat("yyyyMMddHHmmss");

    private static final SimpleDateFormat DISPLAY_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Value("${dossier.export.root-path:D:/ruoyi/data/dossier-output}")
    private String exportRootPath;

    @Value("${dossier.file.root-path:D:/ruoyi/data}")
    private String fileRootPath;

    @Autowired
    private DossierInstanceMapper instanceMapper;

    @Autowired
    private DossierDetailMapper detailMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public File exportPdf(String instanceId, String versionId)
    {
        try
        {
            ExportPayload payload = loadPayload(instanceId, versionId);
            File pdfFile = payload.outputDir.resolve(payload.pdfFileName).toFile();
            writePdf(pdfFile, payload);
            saveExportRecord(payload, "PDF", pdfFile, null);
            return pdfFile;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("生成PDF失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public File exportZip(String instanceId, String versionId)
    {
        try
        {
            ExportPayload payload = loadPayload(instanceId, versionId);
            File pdfFile = payload.outputDir.resolve(payload.pdfFileName).toFile();
            writePdf(pdfFile, payload);

            File zipFile = payload.outputDir.resolve(payload.zipFileName).toFile();
            writeZip(zipFile, pdfFile, payload);
            saveExportRecord(payload, "ZIP", pdfFile, zipFile);
            return zipFile;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("生成ZIP失败：" + e.getMessage());
        }
    }

    private ExportPayload loadPayload(String instanceId, String requestedVersionId) throws IOException
    {
        Map<String, Object> instance = instanceMapper.selectInstanceDetail(instanceId);
        if (instance == null)
        {
            throw new ServiceException("没有找到卷宗");
        }
        String versionId = hasText(requestedVersionId) ? requestedVersionId : text(instance.get("currentVersionId"));
        if (!hasText(versionId))
        {
            throw new ServiceException("当前卷宗还没有可导出的版本");
        }

        Map<String, Object> version = findVersion(instanceId, versionId);
        if (version == null)
        {
            throw new ServiceException("没有找到要导出的卷宗版本");
        }

        parseJsonField(version, "contentSummaryJson", "contentSummary");
        parseJsonField(version, "generationParamsJson", "generationParams");
        parseJsonField(version, "resultSummaryJson", "resultSummary");
        parseJsonField(version, "outputJson", "output");

        List<Map<String, Object>> structures = detailMapper.selectStructureNodes(versionId);
        List<Map<String, Object>> contents = detailMapper.selectContentItems(versionId, null);
        List<Map<String, Object>> documents = detailMapper.selectAllDocuments(versionId);
        List<Map<String, Object>> sources = detailMapper.selectDataSources(versionId);
        List<Map<String, Object>> logs = detailMapper.selectOperationLogs(instanceId, versionId);
        normalizeJsonFields(structures, "attrsJson", "sourceTraceJson");
        normalizeJsonFields(contents, "attrsJson", "sourceTraceJson");
        normalizeJsonFields(documents, "attrsJson", "sourceTraceJson");
        normalizeJsonFields(logs, "detailJson");

        @SuppressWarnings("unchecked")
        Map<String, Object> output = (Map<String, Object>) version.get("output");
        output.put("fileCount", documents.size());
        String pdfFileName = firstText(output.get("fileName"), instance.get("pdfFileName"),
                safeName(instance.get("tailNumber")) + "_dossier_" + safeName(version.get("versionLabel")) + ".pdf");
        String zipFileName = firstText(output.get("packageName"), instance.get("zipFileName"),
                safeName(instance.get("tailNumber")) + "_dossier_" + safeName(version.get("versionLabel")) + ".zip");

        Path outputDir = Paths.get(exportRootPath, safeName(instance.get("instanceCode")),
                safeName(version.get("versionLabel"))).toAbsolutePath().normalize();
        Files.createDirectories(outputDir);

        ExportPayload payload = new ExportPayload();
        payload.instance = instance;
        payload.version = version;
        payload.structures = structures;
        payload.contents = contents;
        payload.documents = documents;
        payload.sources = sources;
        payload.logs = logs;
        payload.output = output;
        payload.outputDir = outputDir;
        payload.pdfFileName = sanitizeFileName(pdfFileName);
        payload.zipFileName = sanitizeFileName(zipFileName);
        payload.exportCode = "EXP-" + safeName(instance.get("tailNumber")) + "-"
                + safeName(version.get("versionLabel")) + "-" + CODE_TIME.format(new Date())
                + "-" + newId().substring(0, 8);
        return payload;
    }

    private Map<String, Object> findVersion(String instanceId, String versionId)
    {
        List<Map<String, Object>> versions = instanceMapper.selectVersionList(instanceId);
        for (Map<String, Object> version : versions)
        {
            if (versionId.equals(text(version.get("versionId"))))
            {
                return version;
            }
        }
        return null;
    }

    private void writePdf(File pdfFile, ExportPayload payload) throws IOException
    {
        writePdfLines(pdfFile, payload.pdfFileName, buildPdfLines(payload));
    }

    private void writePdfLines(File pdfFile, String title, List<String> lines) throws IOException
    {
        Files.createDirectories(pdfFile.toPath().getParent());
        try (PDDocument document = new PDDocument())
        {
            PDDocumentInformation info = new PDDocumentInformation();
            info.setTitle(title);
            info.setAuthor("RuoYi Cloud Dossier");
            document.setDocumentInformation(info);

            PDType0Font font = PDType0Font.load(document, resolveFontFile());
            renderLines(document, font, lines);
            document.save(pdfFile);
        }
    }

    private File resolveFontFile()
    {
        String[] candidates = {
                "C:/Windows/Fonts/simhei.ttf",
                "C:/Windows/Fonts/NotoSansSC-VF.ttf",
                "C:/Windows/Fonts/simsunb.ttf"
        };
        for (String candidate : candidates)
        {
            File file = new File(candidate);
            if (file.exists())
            {
                return file;
            }
        }
        throw new ServiceException("没有找到可用于生成中文PDF的字体");
    }

    private List<String> buildPdfLines(ExportPayload payload)
    {
        List<String> lines = new ArrayList<>();
        Map<String, Object> instance = payload.instance;
        Map<String, Object> version = payload.version;

        lines.add(text(instance.get("instanceName")));
        lines.add("");
        lines.add("导出时间：" + DISPLAY_TIME.format(new Date()));
        lines.add("卷宗编号：" + text(instance.get("instanceCode")));
        lines.add("飞机号：" + text(instance.get("tailNumber")));
        lines.add("模板：" + text(instance.get("templateName")) + " / " + text(instance.get("templateVersion")));
        lines.add("版本：" + text(version.get("versionLabel")));
        lines.add("文件数：" + textValue(payload.output.get("fileCount"), instance.get("fileCount")));
        lines.add("");

        lines.add("一、卷宗目录");
        addSectionLines(lines, payload.output.get("sections"));
        lines.add("");

        lines.add("二、卷宗结构节点");
        for (Map<String, Object> row : limit(payload.structures, 80))
        {
            lines.add(" - " + text(row.get("nodePath")) + " | " + text(row.get("name")) + " | "
                    + statusText(row.get("completenessStatus")));
        }
        lines.add("");

        lines.add("三、卷宗内容");
        for (Map<String, Object> row : limit(payload.contents, 120))
        {
            lines.add(" - " + text(row.get("itemName")) + " | 来源：" + text(row.get("sourceSystem"))
                    + " | 追溯：" + text(row.get("sourceRecordKey")));
            if (hasText(text(row.get("contentSummary"))))
            {
                lines.add("   " + text(row.get("contentSummary")));
            }
        }
        lines.add("");

        lines.add("四、附件材料");
        if (payload.documents.isEmpty())
        {
            lines.add(" - 当前版本没有登记附件材料");
        }
        for (Map<String, Object> row : limit(payload.documents, 120))
        {
            lines.add(" - " + text(row.get("title")) + " | " + text(row.get("fileStorageKey"))
                    + " | " + statusText(row.get("completenessStatus")));
        }
        lines.add("");

        lines.add("五、数据来源");
        for (Map<String, Object> row : payload.sources)
        {
            lines.add(" - " + text(row.get("sourceSystem")) + " / " + text(row.get("sourceTable"))
                    + "：" + text(row.get("recordCount")) + " 条");
        }
        lines.add("");

        lines.add("六、操作记录");
        for (Map<String, Object> row : payload.logs)
        {
            lines.add(" - " + text(row.get("operatedAt")) + " | " + text(row.get("operationName"))
                    + " | " + text(row.get("operatorName")) + " | " + text(row.get("operationStatus")));
        }
        return lines;
    }

    @SuppressWarnings("unchecked")
    private void addSectionLines(List<String> lines, Object sections)
    {
        if (!(sections instanceof List))
        {
            lines.add(" - 当前版本没有输出目录清单");
            return;
        }
        for (Object item : (List<Object>) sections)
        {
            if (item instanceof Map)
            {
                Map<String, Object> row = (Map<String, Object>) item;
                lines.add(" - " + text(row.get("code")) + " " + text(row.get("name")));
            }
        }
    }

    private void renderLines(PDDocument document, PDType0Font font, List<String> lines) throws IOException
    {
        float fontSize = 10.5f;
        float lineHeight = 17f;
        float margin = 52f;
        float pageWidth = PDRectangle.A4.getWidth() - margin * 2;
        float y = PDRectangle.A4.getHeight() - margin;

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        PDPageContentStream stream = new PDPageContentStream(document, page);
        stream.setFont(font, fontSize);
        stream.beginText();
        stream.newLineAtOffset(margin, y);

        for (String rawLine : lines)
        {
            List<String> wrapped = wrapLine(font, fontSize, rawLine, pageWidth);
            for (String line : wrapped)
            {
                if (y <= margin)
                {
                    stream.endText();
                    stream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    stream = new PDPageContentStream(document, page);
                    stream.setFont(font, fontSize);
                    y = PDRectangle.A4.getHeight() - margin;
                    stream.beginText();
                    stream.newLineAtOffset(margin, y);
                }
                stream.showText(line.isEmpty() ? " " : line);
                stream.newLineAtOffset(0, -lineHeight);
                y -= lineHeight;
            }
        }
        stream.endText();
        stream.close();
    }

    private List<String> wrapLine(PDType0Font font, float fontSize, String line, float maxWidth) throws IOException
    {
        List<String> result = new ArrayList<>();
        String text = line == null ? "" : line;
        if (text.isEmpty())
        {
            result.add("");
            return result;
        }

        StringBuilder current = new StringBuilder();
        for (int offset = 0; offset < text.length();)
        {
            int codePoint = text.codePointAt(offset);
            String next = new String(Character.toChars(codePoint));
            String candidate = current + next;
            if (current.length() > 0 && width(font, fontSize, candidate) > maxWidth)
            {
                result.add(current.toString());
                current.setLength(0);
            }
            current.append(next);
            offset += Character.charCount(codePoint);
        }
        if (current.length() > 0)
        {
            result.add(current.toString());
        }
        return result;
    }

    private float width(PDType0Font font, float fontSize, String text) throws IOException
    {
        return font.getStringWidth(text) / 1000f * fontSize;
    }

    private void writeZip(File zipFile, File pdfFile, ExportPayload payload) throws IOException
    {
        Files.createDirectories(zipFile.toPath().getParent());
        File notePdfFile = payload.outputDir.resolve("00_卷宗说明.pdf").toFile();
        writePdfLines(notePdfFile, "卷宗导出说明", buildNotePdfLines(payload));
        List<Map<String, Object>> fileManifest = buildFileManifest(payload);
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile), StandardCharsets.UTF_8))
        {
            Set<String> entryNames = new HashSet<>();
            addFile(zip, notePdfFile, "00_卷宗说明.pdf", entryNames);
            addFile(zip, pdfFile, "01_卷宗主文档.pdf", entryNames);
            addBytes(zip, "02_卷宗数据清单.xlsx", buildWorkbook(payload, fileManifest), entryNames);
            addAttachmentFiles(zip, fileManifest, entryNames);
            addJson(zip, "04_机器可读数据/dossier.json", buildSummary(payload), entryNames);
            addJson(zip, "04_机器可读数据/nodes.json", payload.structures, entryNames);
            addJson(zip, "04_机器可读数据/content_items.json", payload.contents, entryNames);
            addJson(zip, "04_机器可读数据/file_manifest.json", fileManifest, entryNames);
            addJson(zip, "04_机器可读数据/traceability.json", buildTraceability(payload), entryNames);
            addJson(zip, "04_机器可读数据/data_sources.json", payload.sources, entryNames);
            addText(zip, "导出说明.txt", buildReadme(payload), entryNames);
        }
    }

    private List<String> buildNotePdfLines(ExportPayload payload)
    {
        List<String> lines = new ArrayList<>();
        lines.add("卷宗导出说明");
        lines.add("");
        lines.add("卷宗：" + text(payload.instance.get("instanceName")));
        lines.add("卷宗编号：" + text(payload.instance.get("instanceCode")));
        lines.add("飞机号：" + text(payload.instance.get("tailNumber")));
        lines.add("版本：" + text(payload.version.get("versionLabel")));
        lines.add("导出编号：" + payload.exportCode);
        lines.add("导出时间：" + DISPLAY_TIME.format(new Date()));
        lines.add("");
        lines.add("导出包组织方式：");
        lines.add("00_卷宗说明.pdf：说明导出范围、版本和包内结构。");
        lines.add("01_卷宗主文档.pdf：面向人工审查和归档的卷宗正文。");
        lines.add("02_卷宗数据清单.xlsx：目录、节点、内容、附件、来源、日志等结构化清单。");
        lines.add("03_附件材料/：按对象层级和节点归集的原始附件文件。");
        lines.add("04_机器可读数据/：系统复核和接口传递使用的 JSON 数据。");
        lines.add("");
        lines.add("说明：若原始附件在本地存储中不存在，导出包会在 file_manifest.json 中保留登记记录，并标记为 missing。");
        return lines;
    }

    private byte[] buildWorkbook(ExportPayload payload, List<Map<String, Object>> fileManifest) throws IOException
    {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream())
        {
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            writeKeyValueSheet(workbook, headerStyle, "卷宗概要", summaryRows(payload));
            writeTableSheet(workbook, headerStyle, "结构节点", payload.structures,
                    "nodePath", "objectLevel", "name", "bomNodeCode", "completenessStatus", "contentCount", "missingCount");
            writeTableSheet(workbook, headerStyle, "内容清单", payload.contents,
                    "itemName", "lifecycleStage", "sourceSystem", "sourceTable", "sourceRecordKey", "completenessStatus");
            writeTableSheet(workbook, headerStyle, "附件清单", fileManifest,
                    "title", "docNo", "fileCode", "revision", "objectLevel", "targetName", "relationType",
                    "businessDomain", "lifecycleStage", "zipPath", "exportStatus", "fileStorageKey", "resolvedHash",
                    "documentStatus", "completenessStatus");
            writeTableSheet(workbook, headerStyle, "数据来源", payload.sources,
                    "sourceSystem", "sourceTable", "recordCount");
            writeTableSheet(workbook, headerStyle, "操作记录", payload.logs,
                    "operatedAt", "operationName", "operatorName", "operationStatus");

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private List<Map<String, Object>> summaryRows(ExportPayload payload)
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(pair("卷宗名称", payload.instance.get("instanceName")));
        rows.add(pair("卷宗编号", payload.instance.get("instanceCode")));
        rows.add(pair("飞机号", payload.instance.get("tailNumber")));
        rows.add(pair("模板", text(payload.instance.get("templateName")) + " / " + text(payload.instance.get("templateVersion"))));
        rows.add(pair("版本", payload.version.get("versionLabel")));
        rows.add(pair("导出编号", payload.exportCode));
        rows.add(pair("导出时间", DISPLAY_TIME.format(new Date())));
        rows.add(pair("结构节点数", payload.structures.size()));
        rows.add(pair("内容记录数", payload.contents.size()));
        rows.add(pair("附件数", payload.documents.size()));
        rows.add(pair("数据来源数", payload.sources.size()));
        rows.add(pair("操作记录数", payload.logs.size()));
        return rows;
    }

    private Map<String, Object> pair(String name, Object value)
    {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("项目", name);
        row.put("内容", value);
        return row;
    }

    private void writeKeyValueSheet(XSSFWorkbook workbook, CellStyle headerStyle, String sheetName,
            List<Map<String, Object>> rows)
    {
        Sheet sheet = workbook.createSheet(sheetName);
        Row header = sheet.createRow(0);
        writeCell(header, 0, "项目", headerStyle);
        writeCell(header, 1, "内容", headerStyle);
        int rowIndex = 1;
        for (Map<String, Object> rowData : rows)
        {
            Row row = sheet.createRow(rowIndex++);
            writeCell(row, 0, rowData.get("项目"), null);
            writeCell(row, 1, rowData.get("内容"), null);
        }
        autoSize(sheet, 2);
    }

    private void writeTableSheet(XSSFWorkbook workbook, CellStyle headerStyle, String sheetName,
            List<Map<String, Object>> rows, String... columns)
    {
        Sheet sheet = workbook.createSheet(sheetName);
        Row header = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++)
        {
            writeCell(header, i, columnLabel(columns[i]), headerStyle);
        }
        int rowIndex = 1;
        for (Map<String, Object> rowData : rows)
        {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < columns.length; i++)
            {
                writeCell(row, i, rowData.get(columns[i]), null);
            }
        }
        autoSize(sheet, columns.length);
    }

    private void writeCell(Row row, int index, Object value, CellStyle style)
    {
        Cell cell = row.createCell(index);
        cell.setCellValue(formatCellValue(value));
        if (style != null)
        {
            cell.setCellStyle(style);
        }
    }

    private String formatCellValue(Object value)
    {
        if (value instanceof Map || value instanceof List)
        {
            return toJson(value);
        }
        return text(value);
    }

    private void autoSize(Sheet sheet, int columns)
    {
        for (int i = 0; i < columns; i++)
        {
            sheet.autoSizeColumn(i);
            int width = Math.min(Math.max(sheet.getColumnWidth(i), 3000), 12000);
            sheet.setColumnWidth(i, width);
        }
    }

    private String columnLabel(String column)
    {
        Map<String, String> labels = mapOfString(
                "nodePath", "节点路径",
                "objectLevel", "对象层级",
                "name", "节点名称",
                "bomNodeCode", "节点编号",
                "completenessStatus", "完整性",
                "contentCount", "内容数",
                "missingCount", "缺失数",
                "itemName", "内容名称",
                "lifecycleStage", "生命周期阶段",
                "sourceSystem", "来源系统",
                "sourceTable", "来源表",
                "sourceRecordKey", "追溯键",
                "title", "文件名称",
                "docNo", "文件编号",
                "fileCode", "资产编号",
                "revision", "版本/修订",
                "relationType", "挂靠关系",
                "businessDomain", "业务域",
                "targetName", "挂靠对象",
                "fileStorageKey", "存储路径",
                "fileHash", "文件哈希",
                "zipPath", "包内路径",
                "exportStatus", "导出状态",
                "resolvedHash", "导出文件哈希",
                "documentStatus", "文件状态",
                "recordCount", "记录数",
                "operatedAt", "操作时间",
                "operationName", "操作",
                "operatorName", "操作人",
                "operationStatus", "操作状态");
        return labels.getOrDefault(column, column);
    }

    private Map<String, String> mapOfString(String... values)
    {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2)
        {
            map.put(values[i], values[i + 1]);
        }
        return map;
    }

    private List<Map<String, Object>> buildFileManifest(ExportPayload payload) throws IOException
    {
        List<Map<String, Object>> manifest = new ArrayList<>();
        Map<String, Map<String, Object>> nodeIndex = structureIndex(payload.structures);
        Set<String> zipPaths = new HashSet<>();
        for (Map<String, Object> row : payload.documents)
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.putAll(row);
            Path source = resolveAttachmentPath(row);
            Map<String, Object> node = nodeIndex.get(text(row.get("structureNodeId")));
            String zipPath = buildAttachmentZipPath(row, node, zipPaths);
            item.put("zipPath", zipPath);
            item.put("resolvedPath", source == null ? "" : source.toString());
            item.put("exportStatus", source != null && Files.isRegularFile(source) ? "included" : "missing");
            item.put("resolvedHash", source != null && Files.isRegularFile(source) ? sha256(source) : "");
            manifest.add(item);
        }
        return manifest;
    }

    private Map<String, Map<String, Object>> structureIndex(List<Map<String, Object>> structures)
    {
        Map<String, Map<String, Object>> index = new LinkedHashMap<>();
        for (Map<String, Object> row : structures)
        {
            String structureNodeId = text(row.get("structureNodeId"));
            if (hasText(structureNodeId))
            {
                index.put(structureNodeId, row);
            }
        }
        return index;
    }

    private String buildAttachmentZipPath(Map<String, Object> document, Map<String, Object> node, Set<String> used)
    {
        String level = levelLabel(firstText(document.get("objectLevel"), node == null ? "" : node.get("objectLevel")));
        String nodeName = firstText(node == null ? "" : node.get("nodePath"), document.get("targetName"),
                document.get("targetCode"), document.get("partNumber"), "未归类对象");
        String fileName = firstText(document.get("originalFileName"), document.get("displayName"), document.get("title"),
                document.get("docNo"), document.get("documentEntryId"));
        String extension = fileExtension(firstText(document.get("fileStorageKey"), document.get("storagePath"),
                document.get("storageKey"), document.get("fileExt")));
        if (hasText(extension) && !fileName.toLowerCase().endsWith("." + extension.toLowerCase()))
        {
            fileName = fileName + "." + extension;
        }
        String base = "03_附件材料/" + safeName(level) + "/" + safeName(nodeName) + "/" + sanitizeFileName(fileName);
        return uniqueEntryName(base, used);
    }

    private void addAttachmentFiles(ZipOutputStream zip, List<Map<String, Object>> manifest, Set<String> entryNames)
            throws IOException
    {
        List<String> missing = new ArrayList<>();
        for (Map<String, Object> item : manifest)
        {
            if (!"included".equals(text(item.get("exportStatus"))))
            {
                missing.add(text(item.get("title")) + " | " + text(item.get("fileStorageKey")));
                continue;
            }
            Path path = Paths.get(text(item.get("resolvedPath")));
            addPath(zip, path, text(item.get("zipPath")), entryNames);
        }
        if (!missing.isEmpty())
        {
            addText(zip, "03_附件材料/未落盘附件清单.txt", String.join(System.lineSeparator(), missing), entryNames);
        }
    }

    private List<Map<String, Object>> buildTraceability(ExportPayload payload)
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> item : payload.contents)
        {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("type", "content");
            row.put("name", item.get("itemName"));
            row.put("lifecycleStage", item.get("lifecycleStage"));
            row.put("sourceSystem", item.get("sourceSystem"));
            row.put("sourceTable", item.get("sourceTable"));
            row.put("sourceRecordKey", item.get("sourceRecordKey"));
            row.put("structureNodeId", item.get("structureNodeId"));
            row.put("bomNodeId", item.get("bomNodeId"));
            rows.add(row);
        }
        for (Map<String, Object> item : payload.documents)
        {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("type", "file");
            row.put("name", item.get("title"));
            row.put("relationType", item.get("relationType"));
            row.put("lifecycleStage", item.get("lifecycleStage"));
            row.put("sourceSystem", item.get("sourceSystem"));
            row.put("sourceTable", item.get("sourceTable"));
            row.put("sourceRecordKey", item.get("sourceRecordKey"));
            row.put("targetType", item.get("targetType"));
            row.put("targetName", item.get("targetName"));
            row.put("structureNodeId", item.get("structureNodeId"));
            row.put("fileStorageKey", item.get("fileStorageKey"));
            rows.add(row);
        }
        return rows;
    }

    private Map<String, Object> buildSummary(ExportPayload payload)
    {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("instance", payload.instance);
        summary.put("version", payload.version);
        summary.put("output", payload.output);
        summary.put("exportCode", payload.exportCode);
        summary.put("exportedAt", DISPLAY_TIME.format(new Date()));
        return summary;
    }

    private String buildAttachmentNote(ExportPayload payload)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("本文件列出当前卷宗版本登记的附件。").append(System.lineSeparator());
        builder.append("如果附件原文件存在于本地存储，可在后续版本中按 fileStorageKey 追加打包。").append(System.lineSeparator());
        builder.append(System.lineSeparator());
        for (Map<String, Object> row : payload.documents)
        {
            builder.append("- ").append(text(row.get("title"))).append(" | ")
                    .append(text(row.get("fileStorageKey"))).append(System.lineSeparator());
        }
        return builder.toString();
    }

    private String buildReadme(ExportPayload payload)
    {
        return "卷宗导出说明" + System.lineSeparator()
                + "卷宗：" + text(payload.instance.get("instanceName")) + System.lineSeparator()
                + "版本：" + text(payload.version.get("versionLabel")) + System.lineSeparator()
                + "导出编号：" + payload.exportCode + System.lineSeparator()
                + "导出时间：" + DISPLAY_TIME.format(new Date()) + System.lineSeparator()
                + "00_卷宗说明.pdf：导出范围、版本和包结构说明。" + System.lineSeparator()
                + "01_卷宗主文档.pdf：面向人工查看、审签和归档的主文档。" + System.lineSeparator()
                + "02_卷宗数据清单.xlsx：卷宗概要、结构节点、内容项、附件、来源和日志清单。" + System.lineSeparator()
                + "03_附件材料/：按对象层级和节点归集的原始附件文件。" + System.lineSeparator()
                + "04_机器可读数据/：供系统复核、接口传递和追溯使用的 JSON 数据。" + System.lineSeparator();
    }

    private void addFile(ZipOutputStream zip, File file, String entryName, Set<String> entryNames) throws IOException
    {
        addPath(zip, file.toPath(), entryName, entryNames);
    }

    private void addPath(ZipOutputStream zip, Path path, String entryName, Set<String> entryNames) throws IOException
    {
        String uniqueName = uniqueEntryName(entryName, entryNames);
        zip.putNextEntry(new ZipEntry(uniqueName));
        Files.copy(path, zip);
        zip.closeEntry();
    }

    private void addJson(ZipOutputStream zip, String entryName, Object data, Set<String> entryNames) throws IOException
    {
        addText(zip, entryName, OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(data), entryNames);
    }

    private void addText(ZipOutputStream zip, String entryName, String text, Set<String> entryNames) throws IOException
    {
        addBytes(zip, entryName, text.getBytes(StandardCharsets.UTF_8), entryNames);
    }

    private void addBytes(ZipOutputStream zip, String entryName, byte[] bytes, Set<String> entryNames) throws IOException
    {
        String uniqueName = uniqueEntryName(entryName, entryNames);
        zip.putNextEntry(new ZipEntry(uniqueName));
        zip.write(bytes);
        zip.closeEntry();
    }

    private String uniqueEntryName(String entryName, Set<String> entryNames)
    {
        String normalized = entryName.replace('\\', '/');
        if (entryNames.add(normalized))
        {
            return normalized;
        }
        int slash = normalized.lastIndexOf('/');
        String dir = slash >= 0 ? normalized.substring(0, slash + 1) : "";
        String name = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        String ext = dot > 0 ? name.substring(dot) : "";
        int index = 2;
        while (true)
        {
            String candidate = dir + base + "_" + index + ext;
            if (entryNames.add(candidate))
            {
                return candidate;
            }
            index++;
        }
    }

    private Path resolveAttachmentPath(Map<String, Object> row)
    {
        String storage = firstText(row.get("fileStorageKey"), row.get("storagePath"), row.get("storageKey"),
                row.get("accessUrl"));
        if (!hasText(storage) || storage.startsWith("http://") || storage.startsWith("https://"))
        {
            return null;
        }
        String normalized = URLDecoder.decode(storage, StandardCharsets.UTF_8).replace('\\', '/');
        List<Path> candidates = new ArrayList<>();
        Path raw = Paths.get(normalized);
        candidates.add(raw);
        candidates.add(Paths.get(fileRootPath, normalized));
        candidates.add(Paths.get(exportRootPath, normalized));
        candidates.add(Paths.get("D:/ruoyi", normalized));
        for (Path candidate : candidates)
        {
            Path path = candidate.toAbsolutePath().normalize();
            if (Files.isRegularFile(path))
            {
                return path;
            }
        }
        return null;
    }

    private String levelLabel(String objectLevel)
    {
        String value = text(objectLevel).toLowerCase();
        if ("aircraft".equals(value))
        {
            return "整机";
        }
        if ("system".equals(value))
        {
            return "系统";
        }
        if ("subsystem".equals(value))
        {
            return "子系统";
        }
        if ("equipment".equals(value))
        {
            return "设备";
        }
        if ("component".equals(value))
        {
            return "组件";
        }
        if ("part".equals(value))
        {
            return "零件";
        }
        return hasText(value) ? value : "未归类";
    }

    private String fileExtension(String value)
    {
        String text = text(value);
        if (!hasText(text))
        {
            return "";
        }
        if (!text.contains(".") && text.matches("(?i)^[a-z0-9]+$"))
        {
            return text.toLowerCase();
        }
        String clean = text.split("[?#]", 2)[0];
        int index = clean.lastIndexOf('.');
        return index >= 0 && index < clean.length() - 1 ? clean.substring(index + 1).toLowerCase() : "";
    }

    private void saveExportRecord(ExportPayload payload, String mode, File pdfFile, File zipFile) throws IOException
    {
        String jobId = newId();
        Map<String, Object> job = new LinkedHashMap<>();
        job.put("exportJobId", jobId);
        job.put("instanceId", payload.instance.get("instanceId"));
        job.put("versionId", payload.version.get("versionId"));
        job.put("generationJobId", payload.version.get("generationJobId"));
        job.put("exportCode", payload.exportCode);
        job.put("exportScopeJson", toJson(buildSummary(payload)));
        job.put("exportParamsJson", toJson(mapOf("mode", mode, "outputDir", payload.outputDir.toString())));
        job.put("requestedBy", currentUser());
        instanceMapper.insertExportJob(job);

        int order = 1;
        saveExportFile(jobId, pdfFile, payload.pdfFileName, "PDF", "application/pdf", "main_pdf", true,
                null, order++);
        if (zipFile != null)
        {
            saveExportFile(jobId, zipFile, payload.zipFileName, "ZIP", "application/zip", "attachment_zip", true,
                    null, order++);
        }
    }

    private void saveExportFile(String jobId, File file, String fileName, String format, String mimeType,
            String role, boolean primary, Integer pageCount, int order) throws IOException
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("exportFileId", newId());
        item.put("exportJobId", jobId);
        item.put("fileName", fileName);
        item.put("fileStorageKey", file.toPath().toAbsolutePath().normalize().toString());
        item.put("fileFormat", format);
        item.put("mimeType", mimeType);
        item.put("fileRole", role);
        item.put("isPrimary", primary ? 1 : 0);
        item.put("fileSize", file.length());
        item.put("pageCount", pageCount);
        item.put("fileHash", sha256(file.toPath()));
        item.put("displayOrder", order);
        instanceMapper.insertExportFile(item);
    }

    private String sha256(Path path) throws IOException
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = Files.readAllBytes(path);
            byte[] hash = digest.digest(bytes);
            StringBuilder builder = new StringBuilder();
            for (byte b : hash)
            {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private void normalizeJsonFields(List<Map<String, Object>> rows, String... fields)
    {
        for (Map<String, Object> row : rows)
        {
            for (String field : fields)
            {
                parseJsonField(row, field, field.replace("Json", ""));
                row.remove(field);
            }
        }
    }

    private void parseJsonField(Map<String, Object> row, String sourceKey, String targetKey)
    {
        Object raw = row.get(sourceKey);
        if (raw == null || !hasText(String.valueOf(raw)))
        {
            row.put(targetKey, new LinkedHashMap<>());
            return;
        }
        try
        {
            row.put(targetKey, OBJECT_MAPPER.readValue(String.valueOf(raw), new TypeReference<Map<String, Object>>()
            {
            }));
        }
        catch (Exception e)
        {
            row.put(targetKey, new LinkedHashMap<>());
        }
    }

    private String toJson(Object value)
    {
        try
        {
            return OBJECT_MAPPER.writeValueAsString(value);
        }
        catch (Exception e)
        {
            return "{}";
        }
    }

    private Map<String, Object> mapOf(Object... values)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2)
        {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }

    private List<Map<String, Object>> limit(List<Map<String, Object>> rows, int max)
    {
        if (rows.size() <= max)
        {
            return rows;
        }
        return rows.subList(0, max);
    }

    private String statusText(Object value)
    {
        String status = text(value);
        if ("complete".equals(status))
        {
            return "齐套";
        }
        if ("warning".equals(status))
        {
            return "提醒";
        }
        if ("missing".equals(status))
        {
            return "缺失";
        }
        if ("active".equals(status))
        {
            return "有效";
        }
        return hasText(status) ? status : "-";
    }

    private String textValue(Object first, Object second)
    {
        return hasText(text(first)) ? text(first) : text(second);
    }

    private String firstText(Object... values)
    {
        for (Object value : values)
        {
            String text = text(value);
            if (hasText(text))
            {
                return text;
            }
        }
        return "";
    }

    private String text(Object value)
    {
        if (value == null)
        {
            return "";
        }
        if (value instanceof BigDecimal)
        {
            return ((BigDecimal) value).stripTrailingZeros().toPlainString();
        }
        return String.valueOf(value).trim();
    }

    private boolean hasText(String value)
    {
        return value != null && !value.trim().isEmpty();
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

    private String safeName(Object value)
    {
        String text = sanitizeFileName(text(value));
        return hasText(text) ? text : "unknown";
    }

    private String sanitizeFileName(String value)
    {
        String decoded = URLDecoder.decode(value == null ? "" : value, StandardCharsets.UTF_8);
        return decoded.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("\\s+", "_");
    }

    private String newId()
    {
        return UUID.randomUUID().toString();
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

    private static class ExportPayload
    {
        private Map<String, Object> instance;
        private Map<String, Object> version;
        private Map<String, Object> output;
        private List<Map<String, Object>> structures;
        private List<Map<String, Object>> contents;
        private List<Map<String, Object>> documents;
        private List<Map<String, Object>> sources;
        private List<Map<String, Object>> logs;
        private Path outputDir;
        private String pdfFileName;
        private String zipFileName;
        private String exportCode;
    }
}
