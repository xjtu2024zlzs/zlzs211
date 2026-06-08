package com.ruoyi.project1.dossier.controller;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.project1.dossier.service.IDossierExportService;
import com.ruoyi.project1.dossier.service.IDossierInstanceService;

@RestController
@RequestMapping("/dossier/instance")
public class DossierInstanceController extends BaseController
{
    @Autowired
    private IDossierInstanceService instanceService;

    @Autowired
    private IDossierExportService exportService;

    @RequiresPermissions("project1:dossier:instance:list")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam Map<String, Object> query)
    {
        return success(instanceService.selectInstancePage(query));
    }

    @RequiresPermissions("project1:dossier:instance:list")
    @GetMapping("/summary")
    public AjaxResult summary(@RequestParam Map<String, Object> query)
    {
        return success(instanceService.selectInstanceSummary(query));
    }

    @RequiresPermissions("project1:dossier:instance:list")
    @GetMapping("/{instanceId}")
    public AjaxResult detail(@PathVariable String instanceId)
    {
        return success(instanceService.selectInstanceDetail(instanceId));
    }

    @RequiresPermissions("project1:dossier:instance:list")
    @GetMapping("/{instanceId}/versions")
    public AjaxResult versions(@PathVariable String instanceId)
    {
        return success(instanceService.selectVersionList(instanceId));
    }

    @RequiresPermissions("project1:dossier:instance:list")
    @GetMapping("/{instanceId}/jobs")
    public AjaxResult jobs(@PathVariable String instanceId)
    {
        return success(instanceService.selectJobList(instanceId));
    }

    @RequiresPermissions("project1:dossier:instance:list")
    @GetMapping("/{instanceId}/files")
    public AjaxResult files(@PathVariable String instanceId, @RequestParam(required = false) String versionId)
    {
        return success(instanceService.selectFileList(instanceId, versionId));
    }

    @RequiresPermissions("project1:dossier:instance:list")
    @Log(title = "卷宗导出PDF", businessType = BusinessType.EXPORT)
    @GetMapping("/{instanceId}/export/pdf")
    public void exportPdf(@PathVariable String instanceId, @RequestParam(required = false) String versionId,
            HttpServletResponse response) throws IOException
    {
        File file = exportService.exportPdf(instanceId, versionId);
        writeFile(response, file, "application/pdf");
    }

    @RequiresPermissions("project1:dossier:instance:list")
    @Log(title = "卷宗导出ZIP", businessType = BusinessType.EXPORT)
    @GetMapping("/{instanceId}/export/zip")
    public void exportZip(@PathVariable String instanceId, @RequestParam(required = false) String versionId,
            HttpServletResponse response) throws IOException
    {
        File file = exportService.exportZip(instanceId, versionId);
        writeFile(response, file, "application/zip");
    }

    @RequiresPermissions("project1:dossier:instance:list")
    @Log(title = "卷宗发布", businessType = BusinessType.UPDATE)
    @PostMapping("/{instanceId}/publish")
    public AjaxResult publish(@PathVariable String instanceId)
    {
        instanceService.publishInstance(instanceId);
        return success();
    }

    @RequiresPermissions("project1:dossier:instance:list")
    @Log(title = "卷宗归档", businessType = BusinessType.UPDATE)
    @PostMapping("/{instanceId}/archive")
    public AjaxResult archive(@PathVariable String instanceId)
    {
        instanceService.archiveInstance(instanceId);
        return success();
    }

    private void writeFile(HttpServletResponse response, File file, String contentType) throws IOException
    {
        String encodedName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedName);
        response.setContentLengthLong(file.length());
        Files.copy(file.toPath(), response.getOutputStream());
        response.flushBuffer();
    }
}
