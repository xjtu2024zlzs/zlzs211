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
import com.ruoyi.project1.domain.AccessScopeTable;
import com.ruoyi.project1.service.IAccessScopeTableService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 接入范围源目标关系Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/accessScopeTable")
public class AccessScopeTableController extends BaseController
{
    @Autowired
    private IAccessScopeTableService accessScopeTableService;

    /**
     * 查询接入范围源目标关系列表
     */
    @RequiresPermissions("project1:accessScopeTable:list")
    @GetMapping("/list")
    public TableDataInfo list(AccessScopeTable accessScopeTable)
    {
        startPage();
        List<AccessScopeTable> list = accessScopeTableService.selectAccessScopeTableList(accessScopeTable);
        return getDataTable(list);
    }

    /**
     * 导出接入范围源目标关系列表
     */
    @RequiresPermissions("project1:accessScopeTable:export")
    @Log(title = "接入范围源目标关系", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AccessScopeTable accessScopeTable)
    {
        List<AccessScopeTable> list = accessScopeTableService.selectAccessScopeTableList(accessScopeTable);
        ExcelUtil<AccessScopeTable> util = new ExcelUtil<AccessScopeTable>(AccessScopeTable.class);
        util.exportExcel(response, list, "接入范围源目标关系数据");
    }

    /**
     * 获取接入范围源目标关系详细信息
     */
    @RequiresPermissions("project1:accessScopeTable:query")
    @GetMapping(value = "/{scopeTableId}")
    public AjaxResult getInfo(@PathVariable("scopeTableId") Long scopeTableId)
    {
        return success(accessScopeTableService.selectAccessScopeTableByScopeTableId(scopeTableId));
    }

    /**
     * 新增接入范围源目标关系
     */
    @RequiresPermissions("project1:accessScopeTable:add")
    @Log(title = "接入范围源目标关系", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AccessScopeTable accessScopeTable)
    {
        return toAjax(accessScopeTableService.insertAccessScopeTable(accessScopeTable));
    }

    /**
     * 修改接入范围源目标关系
     */
    @RequiresPermissions("project1:accessScopeTable:edit")
    @Log(title = "接入范围源目标关系", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AccessScopeTable accessScopeTable)
    {
        return toAjax(accessScopeTableService.updateAccessScopeTable(accessScopeTable));
    }

    /**
     * 删除接入范围源目标关系
     */
    @RequiresPermissions("project1:accessScopeTable:remove")
    @Log(title = "接入范围源目标关系", businessType = BusinessType.DELETE)
	@DeleteMapping("/{scopeTableIds}")
    public AjaxResult remove(@PathVariable Long[] scopeTableIds)
    {
        return toAjax(accessScopeTableService.deleteAccessScopeTableByScopeTableIds(scopeTableIds));
    }
}
