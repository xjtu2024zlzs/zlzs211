package com.ruoyi.project1.dossier.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.project1.dossier.domain.DossierTemplate;
import com.ruoyi.project1.dossier.domain.DossierTemplateDetail;
import com.ruoyi.project1.dossier.service.IDossierTemplateService;

@RestController
@RequestMapping("/dossier/template")
public class DossierTemplateController extends BaseController
{
    @Autowired
    private IDossierTemplateService templateService;

    @RequiresPermissions("project1:dossier:template:list")
    @GetMapping("/list")
    public TableDataInfo list(DossierTemplate template)
    {
        startPage();
        List<DossierTemplate> list = templateService.selectDossierTemplateList(template);
        return getDataTable(list);
    }

    @RequiresPermissions("project1:dossier:template:query")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable String id)
    {
        return success(templateService.selectDossierTemplateById(id));
    }

    @RequiresPermissions("project1:dossier:template:add")
    @Log(title = "Dossier template", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DossierTemplateDetail template)
    {
        return toAjax(templateService.insertDossierTemplate(template));
    }

    @RequiresPermissions("project1:dossier:template:edit")
    @Log(title = "Dossier template", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DossierTemplateDetail template)
    {
        return toAjax(templateService.updateDossierTemplate(template));
    }

    @RequiresPermissions("project1:dossier:template:remove")
    @Log(title = "Dossier template", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(templateService.deleteDossierTemplateByIds(ids));
    }

    @RequiresPermissions("project1:dossier:template:add")
    @Log(title = "Dossier template copy", businessType = BusinessType.INSERT)
    @PostMapping("/{id}/copy")
    public AjaxResult copy(@PathVariable String id)
    {
        return success(templateService.copyDossierTemplate(id));
    }

    @RequiresPermissions("project1:dossier:template:add")
    @Log(title = "Dossier template version", businessType = BusinessType.INSERT)
    @PostMapping("/{id}/version")
    public AjaxResult newVersion(@PathVariable String id)
    {
        return success(templateService.createDossierTemplateVersion(id));
    }

    @RequiresPermissions("project1:dossier:template:query")
    @GetMapping("/{id}/check")
    public AjaxResult check(@PathVariable String id)
    {
        return success(templateService.checkDossierTemplate(id));
    }

    @RequiresPermissions("project1:dossier:template:query")
    @GetMapping("/metadata/source")
    public AjaxResult sourceMetadata(String tableName)
    {
        Map<String, Object> metadata = templateService.selectDataSourceMetadata(tableName);
        return success(metadata);
    }

    @RequiresPermissions("project1:dossier:template:edit")
    @Log(title = "Dossier template default", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/default")
    public AjaxResult setDefault(@PathVariable String id)
    {
        return toAjax(templateService.setDefaultTemplate(id));
    }

    @RequiresPermissions("project1:dossier:template:edit")
    @Log(title = "Dossier template status", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/status/{status}")
    public AjaxResult updateStatus(@PathVariable String id, @PathVariable String status)
    {
        return toAjax(templateService.updateTemplateStatus(id, status));
    }
}
