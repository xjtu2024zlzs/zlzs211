package com.ruoyi.project1.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.project1.domain.MatchResultVersion;
import com.ruoyi.project1.service.IMatchResultVersionService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 模式映射结果版本Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/matchResultVersion")
public class MatchResultVersionController extends BaseController
{
    @Autowired
    private IMatchResultVersionService matchResultVersionService;

    /**
     * 查询模式映射结果版本列表
     */
    @RequiresPermissions("project1:matchResultVersion:list")
    @GetMapping("/list")
    public TableDataInfo list(MatchResultVersion matchResultVersion)
    {
        startPage();
        List<MatchResultVersion> list = matchResultVersionService.selectMatchResultVersionList(matchResultVersion);
        return getDataTable(list);
    }

    /**
     * 导出模式映射结果版本列表
     */
    @RequiresPermissions("project1:matchResultVersion:export")
    @Log(title = "模式映射结果版本", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MatchResultVersion matchResultVersion)
    {
        List<MatchResultVersion> list = matchResultVersionService.selectMatchResultVersionList(matchResultVersion);
        ExcelUtil<MatchResultVersion> util = new ExcelUtil<MatchResultVersion>(MatchResultVersion.class);
        util.exportExcel(response, list, "模式映射结果版本数据");
    }

    /**
     * 获取模式映射结果版本详细信息
     */
    @RequiresPermissions("project1:matchResultVersion:query")
    @GetMapping(value = "/{resultVersionId}")
    public AjaxResult getInfo(@PathVariable("resultVersionId") Long resultVersionId)
    {
        return success(matchResultVersionService.selectMatchResultVersionByResultVersionId(resultVersionId));
    }

    /**
     * 新增模式映射结果版本
     */
    @RequiresPermissions("project1:matchResultVersion:add")
    @Log(title = "模式映射结果版本", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MatchResultVersion matchResultVersion)
    {
        return toAjax(matchResultVersionService.insertMatchResultVersion(matchResultVersion));
    }

    /**
     * 修改模式映射结果版本
     */
    @RequiresPermissions("project1:matchResultVersion:edit")
    @Log(title = "模式映射结果版本", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MatchResultVersion matchResultVersion)
    {
        return toAjax(matchResultVersionService.updateMatchResultVersion(matchResultVersion));
    }

    /**
     * 删除模式映射结果版本
     */
    @RequiresPermissions("project1:matchResultVersion:remove")
    @Log(title = "模式映射结果版本", businessType = BusinessType.DELETE)
	@DeleteMapping("/{resultVersionIds}")
    public AjaxResult remove(@PathVariable Long[] resultVersionIds)
    {
        return toAjax(matchResultVersionService.deleteMatchResultVersionByResultVersionIds(resultVersionIds));
    }
}
