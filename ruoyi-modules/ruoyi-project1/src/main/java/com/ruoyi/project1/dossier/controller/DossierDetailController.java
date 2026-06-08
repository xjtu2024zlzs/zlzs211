package com.ruoyi.project1.dossier.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
}
