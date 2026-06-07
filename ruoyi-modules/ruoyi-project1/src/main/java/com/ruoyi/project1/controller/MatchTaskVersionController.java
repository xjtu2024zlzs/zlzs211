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
import com.ruoyi.project1.domain.MatchTaskVersion;
import com.ruoyi.project1.service.IMatchTaskVersionService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 模式映射任务版本Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/matchTaskVersion")
public class MatchTaskVersionController extends BaseController
{
    @Autowired
    private IMatchTaskVersionService matchTaskVersionService;

    /**
     * 查询模式映射任务版本列表
     */
    @RequiresPermissions("project1:matchTaskVersion:list")
    @GetMapping("/list")
    public TableDataInfo list(MatchTaskVersion matchTaskVersion)
    {
        startPage();
        List<MatchTaskVersion> list = matchTaskVersionService.selectMatchTaskVersionList(matchTaskVersion);
        return getDataTable(list);
    }

    /**
     * 导出模式映射任务版本列表
     */
    @RequiresPermissions("project1:matchTaskVersion:export")
    @Log(title = "模式映射任务版本", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MatchTaskVersion matchTaskVersion)
    {
        List<MatchTaskVersion> list = matchTaskVersionService.selectMatchTaskVersionList(matchTaskVersion);
        ExcelUtil<MatchTaskVersion> util = new ExcelUtil<MatchTaskVersion>(MatchTaskVersion.class);
        util.exportExcel(response, list, "模式映射任务版本数据");
    }

    /**
     * 获取模式映射任务版本详细信息
     */
    @RequiresPermissions("project1:matchTaskVersion:query")
    @GetMapping(value = "/{versionId}")
    public AjaxResult getInfo(@PathVariable("versionId") Long versionId)
    {
        return success(matchTaskVersionService.selectMatchTaskVersionByVersionId(versionId));
    }

    /**
     * 新增模式映射任务版本
     */
    @RequiresPermissions("project1:matchTaskVersion:add")
    @Log(title = "模式映射任务版本", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MatchTaskVersion matchTaskVersion)
    {
        return toAjax(matchTaskVersionService.insertMatchTaskVersion(matchTaskVersion));
    }

    /**
     * 修改模式映射任务版本
     */
    @RequiresPermissions("project1:matchTaskVersion:edit")
    @Log(title = "模式映射任务版本", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MatchTaskVersion matchTaskVersion)
    {
        return toAjax(matchTaskVersionService.updateMatchTaskVersion(matchTaskVersion));
    }

    /**
     * 删除模式映射任务版本
     */
    @RequiresPermissions("project1:matchTaskVersion:remove")
    @Log(title = "模式映射任务版本", businessType = BusinessType.DELETE)
	@DeleteMapping("/{versionIds}")
    public AjaxResult remove(@PathVariable Long[] versionIds)
    {
        return toAjax(matchTaskVersionService.deleteMatchTaskVersionByVersionIds(versionIds));
    }
}
