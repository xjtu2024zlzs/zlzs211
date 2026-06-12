package com.ruoyi.project1.dossier.controller;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.project1.dossier.service.IDossierDetailService;

@RestController
@RequestMapping("/dossier/detail")
public class DossierDetailController extends BaseController
{
    @Autowired
    private IDossierDetailService detailService;

    @Value("${dossier.file.root-path:D:/ruoyi/data}")
    private String dossierFileRootPath;

    @Value("${dossier.file.legacy-root-path:D:/ruoyi}")
    private String dossierLegacyFileRootPath;

    @Value("${dossier.export.root-path:D:/ruoyi/data/dossier-output}")
    private String dossierExportRootPath;

    @RequiresPermissions("project1:dossier:detail:list")
    @GetMapping("/current")
    public AjaxResult current(@RequestParam(required = false) String aircraftId,
            @RequestParam(required = false) String instanceId,
            @RequestParam(required = false) String versionId)
    {
        return success(detailService.selectCurrentDetail(aircraftId, instanceId, versionId));
    }

    @RequiresPermissions("project1:dossier:detail:list")
    @GetMapping("/node")
    public AjaxResult node(@RequestParam String instanceId, @RequestParam String versionId,
            @RequestParam String bomNodeId)
    {
        return success(detailService.selectNodeDetail(instanceId, versionId, bomNodeId));
    }

    @RequiresPermissions("project1:dossier:detail:list")
    @GetMapping("/bom/children")
    public AjaxResult bomChildren(@RequestParam String aircraftId,
            @RequestParam(required = false) String parentId,
            @RequestParam(required = false) String instanceId,
            @RequestParam(required = false) String versionId)
    {
        return success(detailService.selectBomChildren(aircraftId, parentId, instanceId, versionId));
    }

    @RequiresPermissions("project1:dossier:detail:list")
    @GetMapping("/bom/search")
    public AjaxResult bomSearch(@RequestParam String aircraftId, @RequestParam String keyword,
            @RequestParam(required = false) String instanceId,
            @RequestParam(required = false) String versionId)
    {
        return success(detailService.searchBomNodes(aircraftId, keyword, instanceId, versionId));
    }

    @RequiresPermissions("project1:dossier:detail:list")
    @GetMapping("/bom/path/{nodeId}")
    public AjaxResult bomPath(@PathVariable String nodeId,
            @RequestParam(required = false) String versionId)
    {
        return success(detailService.selectBomPath(nodeId, versionId));
    }

    @RequiresPermissions("project1:dossier:detail:list")
    @GetMapping("/files/{documentEntryId}/preview")
    public void previewFile(@PathVariable String documentEntryId, HttpServletResponse response) throws IOException
    {
        Map<String, Object> fileInfo = detailService.selectPreviewFile(documentEntryId);
        if (fileInfo == null)
        {
            writeFileNotFound(response, "没有找到文件挂靠记录");
            return;
        }

        String accessUrl = text(fileInfo.get("accessUrl"));
        if (isHttpUrl(accessUrl))
        {
            response.sendRedirect(accessUrl);
            return;
        }

        File file = resolvePreviewFile(fileInfo);
        if (file == null || !file.isFile())
        {
            writeFileNotFound(response, "文件不存在或尚未落盘");
            return;
        }

        String contentType = contentType(fileInfo, file);
        writeFile(response, file, previewFileName(fileInfo, file), contentType, isInlinePreview(contentType, file));
    }

    @RequiresPermissions("project1:dossier:detail:list")
    @PostMapping("/files/export")
    public void exportFiles(@RequestBody List<String> documentEntryIds, HttpServletResponse response) throws IOException
    {
        if (documentEntryIds == null || documentEntryIds.isEmpty())
        {
            writeFileNotFound(response, "No exportable files");
            return;
        }

        List<PreviewExportFile> files = new ArrayList<>();
        for (String documentEntryId : documentEntryIds)
        {
            if (!hasText(documentEntryId))
            {
                continue;
            }
            Map<String, Object> fileInfo = detailService.selectPreviewFile(documentEntryId);
            if (fileInfo == null || isHttpUrl(text(fileInfo.get("accessUrl"))))
            {
                continue;
            }
            File file = resolvePreviewFile(fileInfo);
            if (file != null && file.isFile())
            {
                files.add(new PreviewExportFile(file, previewFileName(fileInfo, file)));
            }
        }

        if (files.isEmpty())
        {
            writeFileNotFound(response, "No exportable files found on disk");
            return;
        }

        String encodedName = URLEncoder.encode("dossier-attachments.zip", StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedName);
        response.setHeader("X-Content-Type-Options", "nosniff");

        Map<String, Integer> usedNames = new HashMap<>();
        try (ZipOutputStream zip = new ZipOutputStream(response.getOutputStream(), StandardCharsets.UTF_8))
        {
            for (PreviewExportFile item : files)
            {
                zip.putNextEntry(new ZipEntry(uniqueZipEntryName(item.fileName, usedNames)));
                Files.copy(item.file.toPath(), zip);
                zip.closeEntry();
            }
        }
        response.flushBuffer();
    }

    private File resolvePreviewFile(Map<String, Object> fileInfo)
    {
        List<String> locations = new ArrayList<>();
        addLocation(locations, fileInfo.get("previewStorageKey"));
        addLocation(locations, fileInfo.get("storagePath"));
        addLocation(locations, fileInfo.get("storageKey"));
        addLocation(locations, fileInfo.get("fileStorageKey"));
        addLocation(locations, fileInfo.get("accessUrl"));
        addLocation(locations, fileInfo.get("sourceRecordKey"));

        List<Path> roots = allowedRoots();
        for (String location : locations)
        {
            Path direct = directPath(location, roots);
            if (direct != null && Files.isRegularFile(direct))
            {
                return direct.toFile();
            }

            String relative = relativePathText(location);
            if (!hasText(relative))
            {
                continue;
            }
            for (Path root : roots)
            {
                Path candidate = root.resolve(relative).normalize();
                if (candidate.startsWith(root) && Files.isRegularFile(candidate))
                {
                    return candidate.toFile();
                }
            }
        }
        return null;
    }

    private List<Path> allowedRoots()
    {
        List<Path> roots = new ArrayList<>();
        addRoot(roots, dossierFileRootPath);
        addRoot(roots, dossierExportRootPath);
        addRoot(roots, dossierLegacyFileRootPath);
        return roots;
    }

    private void addRoot(List<Path> roots, String rootPath)
    {
        if (!hasText(rootPath))
        {
            return;
        }
        try
        {
            Path root = Paths.get(rootPath).toAbsolutePath().normalize();
            if (!roots.contains(root))
            {
                roots.add(root);
            }
        }
        catch (InvalidPathException ignored)
        {
        }
    }

    private Path directPath(String location, List<Path> roots)
    {
        if (!hasText(location) || isHttpUrl(location))
        {
            return null;
        }
        try
        {
            Path path = location.startsWith("file:") ? Paths.get(URI.create(location)) : Paths.get(location);
            if (!path.isAbsolute())
            {
                return null;
            }
            Path normalized = path.normalize();
            for (Path root : roots)
            {
                if (normalized.startsWith(root))
                {
                    return normalized;
                }
            }
        }
        catch (Exception ignored)
        {
        }
        return null;
    }

    private String relativePathText(String location)
    {
        if (!hasText(location) || isHttpUrl(location) || location.startsWith("file:"))
        {
            return "";
        }
        String value = location.replace("\\", "/");
        while (value.startsWith("/"))
        {
            value = value.substring(1);
        }
        return value.contains("..") ? "" : value;
    }

    private void addLocation(List<String> locations, Object value)
    {
        String text = text(value);
        if (hasText(text) && !locations.contains(text))
        {
            locations.add(text);
        }
    }

    private String previewFileName(Map<String, Object> fileInfo, File file)
    {
        String name = firstText(fileInfo.get("originalFileName"), fileInfo.get("displayName"), fileInfo.get("title"),
                fileInfo.get("docNo"), file.getName());
        String extension = extension(file.getName());
        if (hasText(extension) && !name.toLowerCase().endsWith("." + extension.toLowerCase()))
        {
            name = name + "." + extension;
        }
        return name;
    }

    private String contentType(Map<String, Object> fileInfo, File file) throws IOException
    {
        String mimeType = text(fileInfo.get("mimeType"));
        if (hasText(mimeType))
        {
            return mimeType;
        }
        String probed = Files.probeContentType(file.toPath());
        if (hasText(probed))
        {
            return probed;
        }
        String extension = extension(file.getName()).toLowerCase();
        if ("pdf".equals(extension))
        {
            return "application/pdf";
        }
        if ("png".equals(extension))
        {
            return "image/png";
        }
        if ("jpg".equals(extension) || "jpeg".equals(extension))
        {
            return "image/jpeg";
        }
        if ("txt".equals(extension) || "csv".equals(extension) || "json".equals(extension))
        {
            return "text/plain; charset=UTF-8";
        }
        if ("zip".equals(extension))
        {
            return "application/zip";
        }
        return "application/octet-stream";
    }

    private boolean isInlinePreview(String contentType, File file)
    {
        String type = contentType.toLowerCase();
        String extension = extension(file.getName()).toLowerCase();
        return type.startsWith("image/") || type.startsWith("text/plain") || "application/pdf".equals(type)
                || "pdf".equals(extension);
    }

    private void writeFile(HttpServletResponse response, File file, String fileName, String contentType, boolean inline)
            throws IOException
    {
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", (inline ? "inline" : "attachment") + "; filename*=UTF-8''"
                + encodedName);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentLengthLong(file.length());
        Files.copy(file.toPath(), response.getOutputStream());
        response.flushBuffer();
    }

    private String uniqueZipEntryName(String fileName, Map<String, Integer> usedNames)
    {
        String name = text(fileName).replaceAll("[\\\\/:*?\"<>|]", "_");
        if (!hasText(name))
        {
            name = "file";
        }
        int count = usedNames.getOrDefault(name, 0) + 1;
        usedNames.put(name, count);
        if (count == 1)
        {
            return name;
        }
        int index = name.lastIndexOf('.');
        if (index > 0 && index < name.length() - 1)
        {
            return name.substring(0, index) + "(" + count + ")" + name.substring(index);
        }
        return name + "(" + count + ")";
    }

    private void writeFileNotFound(HttpServletResponse response, String message) throws IOException
    {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":404,\"msg\":\"" + message + "\"}");
        response.flushBuffer();
    }

    private boolean isHttpUrl(String value)
    {
        String text = text(value).toLowerCase();
        return text.startsWith("http://") || text.startsWith("https://");
    }

    private String extension(String fileName)
    {
        String text = text(fileName);
        int index = text.lastIndexOf('.');
        return index >= 0 && index < text.length() - 1 ? text.substring(index + 1) : "";
    }

    private String firstText(Object... values)
    {
        for (Object value : values)
        {
            if (hasText(value))
            {
                return text(value);
            }
        }
        return "";
    }

    private boolean hasText(Object value)
    {
        return value != null && String.valueOf(value).trim().length() > 0;
    }

    private String text(Object value)
    {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static class PreviewExportFile
    {
        private final File file;
        private final String fileName;

        private PreviewExportFile(File file, String fileName)
        {
            this.file = file;
            this.fileName = fileName;
        }
    }
}
