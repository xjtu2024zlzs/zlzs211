package com.ruoyi.project1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.project1.service.IProject1HomeService;

@RestController
@RequestMapping("/home")
public class Project1HomeController extends BaseController
{
    private final IProject1HomeService project1HomeService;

    public Project1HomeController(IProject1HomeService project1HomeService)
    {
        this.project1HomeService = project1HomeService;
    }

    @RequiresPermissions("project1:home:query")
    @GetMapping("/dossier-summary")
    public AjaxResult dossierSummary()
    {
        return success(project1HomeService.selectDossierSummary());
    }
}
