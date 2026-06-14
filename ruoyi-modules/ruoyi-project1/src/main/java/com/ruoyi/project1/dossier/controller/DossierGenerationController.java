package com.ruoyi.project1.dossier.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.project1.dossier.service.IDossierGenerationService;

@RestController
@RequestMapping("/dossier/generate")
public class DossierGenerationController extends BaseController
{
    @Autowired
    private IDossierGenerationService generationService;

    @RequiresPermissions("project1:dossier:generation:list")
    @GetMapping("/model/list")
    public AjaxResult modelList()
    {
        return success(generationService.selectModelList());
    }

    @RequiresPermissions("project1:dossier:generation:list")
    @GetMapping("/aircraft/list")
    public AjaxResult aircraftList(@RequestParam(required = false) String modelId)
    {
        return success(generationService.selectAircraftList(modelId));
    }

    @RequiresPermissions("project1:dossier:generation:list")
    @GetMapping("/template/active")
    public AjaxResult activeTemplate()
    {
        return success(generationService.selectActiveTemplate());
    }

    @RequiresPermissions("project1:dossier:generation:list")
    @GetMapping("/template/list")
    public AjaxResult templateList()
    {
        return success(generationService.selectTemplateList());
    }

    @RequiresPermissions("project1:dossier:generation:list")
    @GetMapping("/prepare/{aircraftId}")
    public AjaxResult prepare(@PathVariable String aircraftId, @RequestParam(required = false) String templateId)
    {
        return success(generationService.prepareGeneration(aircraftId, templateId));
    }

    @RequiresPermissions("project1:dossier:generation:list")
    @Log(title = "卷宗生成前检查", businessType = BusinessType.OTHER)
    @PostMapping("/precheck")
    public AjaxResult precheck(@RequestBody Map<String, Object> request)
    {
        return success(generationService.precheck(request));
    }

    @RequiresPermissions("project1:dossier:generation:list")
    @Log(title = "卷宗生成", businessType = BusinessType.INSERT)
    @PostMapping("/start")
    public AjaxResult start(@RequestBody Map<String, Object> request)
    {
        return success(generationService.startGeneration(request));
    }

    @RequiresPermissions("project1:dossier:generation:list")
    @GetMapping("/job/{jobId}")
    public AjaxResult job(@PathVariable String jobId)
    {
        return success(generationService.selectJob(jobId));
    }

    @RequiresPermissions("project1:dossier:generation:list")
    @GetMapping("/job/{jobId}/logs")
    public AjaxResult logs(@PathVariable String jobId)
    {
        return success(generationService.selectJobLogs(jobId));
    }

    @RequiresPermissions("project1:dossier:generation:list")
    @GetMapping("/job/{jobId}/output")
    public AjaxResult output(@PathVariable String jobId)
    {
        return success(generationService.selectJobOutput(jobId));
    }
}
