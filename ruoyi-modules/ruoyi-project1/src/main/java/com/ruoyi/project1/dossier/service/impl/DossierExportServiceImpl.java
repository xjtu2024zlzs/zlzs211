package com.ruoyi.project1.dossier.service.impl;

import java.io.File;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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
        Files.createDirectories(pdfFile.toPath().getParent());
        try (PDDocument document = new PDDocument())
        {
            PDDocumentInformation info = new PDDocumentInformation();
            info.setTitle(payload.pdfFileName);
            info.setAuthor("RuoYi Cloud Dossier");
            document.setDocumentInformation(info);

            PDType0Font font = PDType0Font.load(document, resolveFontFile());
            List<String> lines = buildPdfLines(payload);
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
        lines.add("页数：" + textValue(payload.output.get("pageCount"), instance.get("pageCount")));
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
                lines.add(" - " + text(row.get("code")) + " " + text(row.get("name")) + "："
                        + text(row.get("pageCount")) + " 页，" + text(row.get("fileCount")) + " 个文件");
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
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile), StandardCharsets.UTF_8))
        {
            addFile(zip, pdfFile, payload.pdfFileName);
            addJson(zip, "数据快照/卷宗概要.json", buildSummary(payload));
            addJson(zip, "数据快照/卷宗目录.json", payload.structures);
            addJson(zip, "数据快照/内容清单.json", payload.contents);
            addJson(zip, "数据快照/附件清单.json", payload.documents);
            addJson(zip, "数据快照/数据来源.json", payload.sources);
            addText(zip, "附件/附件说明.txt", buildAttachmentNote(payload));
            addText(zip, "导出说明.txt", buildReadme(payload));
        }
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
                + "说明：本压缩包以已生成的卷宗版本为准，包含PDF正文和结构化数据快照。" + System.lineSeparator();
    }

    private void addFile(ZipOutputStream zip, File file, String entryName) throws IOException
    {
        zip.putNextEntry(new ZipEntry(entryName));
        Files.copy(file.toPath(), zip);
        zip.closeEntry();
    }

    private void addJson(ZipOutputStream zip, String entryName, Object data) throws IOException
    {
        addText(zip, entryName, OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(data));
    }

    private void addText(ZipOutputStream zip, String entryName, String text) throws IOException
    {
        zip.putNextEntry(new ZipEntry(entryName));
        zip.write(text.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
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
                toInt(payload.output.get("pageCount"), toInt(payload.instance.get("pageCount"), 0)), order++);
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
