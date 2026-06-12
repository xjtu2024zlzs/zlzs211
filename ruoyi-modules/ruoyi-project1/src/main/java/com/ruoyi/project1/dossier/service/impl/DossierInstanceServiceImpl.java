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
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.project1.dossier.mapper.DossierInstanceMapper;
import com.ruoyi.project1.dossier.service.IDossierInstanceService;

@Service
public class DossierInstanceServiceImpl implements IDossierInstanceService
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final SimpleDateFormat DISPLAY_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private DossierInstanceMapper instanceMapper;

    @Override
    public Map<String, Object> selectInstancePage(Map<String, Object> query)
    {
        Map<String, Object> params = normalizeQuery(query);
        int total = instanceMapper.selectInstanceCount(params);
        List<Map<String, Object>> rows = normalizeRows(instanceMapper.selectInstanceList(params));

        Map<String, Object> result = map();
        result.put("rows", rows);
        result.put("total", total);
        result.put("pageNum", params.get("pageNum"));
        result.put("pageSize", params.get("pageSize"));
        return result;
    }

    @Override
    public Map<String, Object> selectInstanceSummary(Map<String, Object> query)
    {
        Map<String, Object> summary = instanceMapper.selectInstanceSummary(normalizeQuery(query));
        if (summary == null)
        {
            return map();
        }
        return normalizeRow(summary);
    }

    @Override
    public Map<String, Object> selectInstanceDetail(String instanceId)
    {
        Map<String, Object> detail = instanceMapper.selectInstanceDetail(instanceId);
        if (detail == null)
        {
            throw new ServiceException("没有找到卷宗实例");
        }
        detail = normalizeRow(detail);
        detail.put("versions", selectVersionList(instanceId));
        detail.put("jobs", selectJobList(instanceId));
        detail.put("files", selectFileList(instanceId, text(detail.get("currentVersionId"))));
        return detail;
    }

    @Override
    public List<Map<String, Object>> selectVersionList(String instanceId)
    {
        List<Map<String, Object>> rows = normalizeRows(instanceMapper.selectVersionList(instanceId));
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
    public List<Map<String, Object>> selectJobList(String instanceId)
    {
        List<Map<String, Object>> rows = normalizeRows(instanceMapper.selectJobList(instanceId));
        for (Map<String, Object> row : rows)
        {
            parseJsonField(row, "resultSummaryJson", "resultSummary");
            parseJsonField(row, "outputJson", "output");
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> selectFileList(String instanceId, String versionId)
    {
        List<Map<String, Object>> rows = normalizeRows(instanceMapper.selectFileList(instanceId, blankToNull(versionId)));
        if (!rows.isEmpty())
        {
            return rows;
        }

        Map<String, Object> detail = instanceMapper.selectInstanceDetail(instanceId);
        if (detail == null)
        {
            return rows;
        }
        List<Map<String, Object>> fallback = new ArrayList<>();
        addFallbackFile(fallback, "main_pdf", "PDF", detail.get("pdfFileName"), "application/pdf");
        addFallbackFile(fallback, "attachment_zip", "ZIP", detail.get("zipFileName"), "application/zip");
        return fallback;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishInstance(String instanceId)
    {
        Map<String, Object> detail = instanceMapper.selectInstanceDetail(instanceId);
        if (detail == null)
        {
            throw new ServiceException("没有找到卷宗实例");
        }
        if (!hasText(text(detail.get("currentVersionId"))))
        {
            throw new ServiceException("当前卷宗还没有可发布版本");
        }
        if ("building".equals(text(detail.get("instanceStatus"))) || "running".equals(text(detail.get("generationJobStatus"))))
        {
            throw new ServiceException("卷宗生成中，不能发布");
        }
        if ("failed".equals(text(detail.get("generationJobStatus"))))
        {
            throw new ServiceException("最近一次生成失败，请重新生成后再发布");
        }

        Map<String, Object> params = map();
        params.put("instanceId", instanceId);
        params.put("status", "published");
        params.put("updatedBy", currentUser());
        params.put("published", true);
        params.put("archived", false);
        instanceMapper.updateInstanceStatus(params);
        instanceMapper.updateCurrentVersionPublished(instanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveInstance(String instanceId)
    {
        Map<String, Object> detail = instanceMapper.selectInstanceDetail(instanceId);
        if (detail == null)
        {
            throw new ServiceException("没有找到卷宗实例");
        }
        if ("building".equals(text(detail.get("instanceStatus"))) || "running".equals(text(detail.get("generationJobStatus"))))
        {
            throw new ServiceException("卷宗生成中，不能归档");
        }

        Map<String, Object> params = map();
        params.put("instanceId", instanceId);
        params.put("status", "archived");
        params.put("updatedBy", currentUser());
        params.put("published", false);
        params.put("archived", true);
        instanceMapper.updateInstanceStatus(params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInstance(String instanceId)
    {
        Map<String, Object> detail = instanceMapper.selectInstanceDetail(instanceId);
        if (detail == null)
        {
            throw new ServiceException("没有找到卷宗实例");
        }
        if ("building".equals(text(detail.get("instanceStatus"))) || "running".equals(text(detail.get("generationJobStatus"))))
        {
            throw new ServiceException("卷宗生成中，不能删除");
        }

        Map<String, Object> params = map();
        params.put("instanceId", instanceId);
        params.put("updatedBy", currentUser());
        if (instanceMapper.deleteInstance(params) <= 0)
        {
            throw new ServiceException("删除卷宗实例失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVersion(String instanceId, String versionId)
    {
        Map<String, Object> detail = instanceMapper.selectInstanceDetail(instanceId);
        if (detail == null)
        {
            throw new ServiceException("没有找到卷宗实例");
        }
        if (!hasText(versionId))
        {
            throw new ServiceException("请选择要删除的卷宗版本");
        }

        Map<String, Object> version = null;
        for (Map<String, Object> row : selectVersionList(instanceId))
        {
            if (versionId.equals(text(row.get("versionId"))))
            {
                version = row;
                break;
            }
        }
        if (version == null)
        {
            throw new ServiceException("没有找到卷宗版本");
        }
        if (versionId.equals(text(detail.get("currentVersionId"))) || toInt(version.get("isCurrent"), 0) == 1)
        {
            throw new ServiceException("当前版本不能删除，请先生成新版本后再删除历史版本");
        }
        if (hasText(text(version.get("publishedAt"))))
        {
            throw new ServiceException("已发布版本不能删除");
        }
        if ("running".equals(text(version.get("generationJobStatus"))) || "queued".equals(text(version.get("generationJobStatus"))))
        {
            throw new ServiceException("版本生成中，不能删除");
        }

        Map<String, Object> params = map();
        params.put("instanceId", instanceId);
        params.put("versionId", versionId);
        params.put("deletedBy", currentUser());
        if (instanceMapper.deleteVersion(params) <= 0)
        {
            throw new ServiceException("删除卷宗版本失败");
        }
    }

    private Map<String, Object> normalizeQuery(Map<String, Object> query)
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
        params.put("tailNumber", blankToNull(query.get("tailNumber")));
        params.put("templateId", blankToNull(query.get("templateId")));
        params.put("versionNo", blankToNull(query.get("versionNo")));
        params.put("keyword", blankToNull(query.get("keyword")));
        params.put("beginTime", normalizeBeginTime(query.get("beginTime")));
        params.put("endTime", normalizeEndTime(query.get("endTime")));

        String status = text(query.get("status"));
        if ("all".equals(status))
        {
            status = "";
        }
        if ("generated".equals(status))
        {
            status = "ready";
        }
        if ("failed".equals(status))
        {
            params.put("failedOnly", true);
        }
        else
        {
            params.put("failedOnly", false);
            params.put("status", blankToNull(status));
        }

        String sortOrder = text(query.get("sortOrder"));
        params.put("sortOrder", "asc".equalsIgnoreCase(sortOrder) ? "asc" : "desc");
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
        if (row == null)
        {
            return map();
        }
        row.put("generateTime", displayTime(row.get("generateTime")));
        row.put("createdAt", displayTime(row.get("createdAt")));
        row.put("updatedAt", displayTime(row.get("updatedAt")));
        row.put("publishedAt", displayTimeOrBlank(row.get("publishedAt")));
        row.put("archivedAt", displayTimeOrBlank(row.get("archivedAt")));
        row.put("startedAt", displayTimeOrBlank(row.get("startedAt")));
        row.put("finishedAt", displayTimeOrBlank(row.get("finishedAt")));
        if (row.containsKey("documentEntryCount") && row.get("documentEntryCount") != null)
        {
            row.put("fileCount", row.get("documentEntryCount"));
        }
        row.put("statusName", statusName(row));
        row.put("fileSizeText", fileSizeText(row.get("fileSize")));
        return row;
    }

    private String statusName(Map<String, Object> row)
    {
        String name = text(row.get("statusName"));
        if (hasText(name) && !name.contains("?") && !name.contains("�"))
        {
            return name;
        }
        if ("failed".equals(text(row.get("generationJobStatus"))))
        {
            return "生成失败";
        }
        String status = text(row.get("instanceStatus"));
        if ("published".equals(status))
        {
            return "已发布";
        }
        if ("ready".equals(status))
        {
            return "已生成";
        }
        if ("building".equals(status) || "queued".equals(text(row.get("generationJobStatus")))
                || "running".equals(text(row.get("generationJobStatus"))))
        {
            return "生成中";
        }
        if ("archived".equals(status))
        {
            return "已归档";
        }
        return "草稿";
    }

    private void addFallbackFile(List<Map<String, Object>> files, String role, String format, Object name, String mimeType)
    {
        if (!hasText(text(name)))
        {
            return;
        }
        Map<String, Object> item = map();
        item.put("fileRole", role);
        item.put("fileFormat", format);
        item.put("fileName", name);
        item.put("mimeType", mimeType);
        item.put("fileSizeText", "模拟文件");
        files.add(item);
    }

    private void parseJsonField(Map<String, Object> row, String sourceKey, String targetKey)
    {
        Object raw = row.get(sourceKey);
        if (raw == null)
        {
            row.put(targetKey, map());
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
            row.put(targetKey, map());
        }
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

    private String displayTime(Object value)
    {
        if (value instanceof Date)
        {
            return DISPLAY_TIME.format((Date) value);
        }
        return hasText(text(value)) ? text(value) : "";
    }

    private String displayTimeOrBlank(Object value)
    {
        return hasText(text(value)) ? displayTime(value) : "";
    }

    private String fileSizeText(Object value)
    {
        long size = toLong(value, 0L);
        if (size <= 0)
        {
            return "";
        }
        if (size >= 1024L * 1024L)
        {
            return String.format("%.1f MB", size / 1024.0 / 1024.0);
        }
        return String.format("%.1f KB", size / 1024.0);
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

    private String blankToNull(Object value)
    {
        String text = text(value);
        return hasText(text) ? text : null;
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

    private long toLong(Object value, long defaultValue)
    {
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        try
        {
            return Long.parseLong(text(value));
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

    private boolean hasText(String value)
    {
        return value != null && !value.trim().isEmpty();
    }

    private Map<String, Object> map()
    {
        return new LinkedHashMap<>();
    }
}
