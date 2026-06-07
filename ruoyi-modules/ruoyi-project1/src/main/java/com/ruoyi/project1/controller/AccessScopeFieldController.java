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
import com.ruoyi.project1.domain.AccessScopeField;
import com.ruoyi.project1.service.IAccessScopeFieldService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 接入范围字段Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/accessScopeField")
public class AccessScopeFieldController extends BaseController
{
    @Autowired
    private IAccessScopeFieldService accessScopeFieldService;

    /**
     * 查询接入范围字段列表
     */
    @RequiresPermissions("project1:accessScopeField:list")
    @GetMapping("/list")
    public TableDataInfo list(AccessScopeField accessScopeField)
    {
        startPage();
        List<AccessScopeField> list = accessScopeFieldService.selectAccessScopeFieldList(accessScopeField);
        return getDataTable(list);
    }

    /**
     * 导出接入范围字段列表
     */
    @RequiresPermissions("project1:accessScopeField:export")
    @Log(title = "接入范围字段", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AccessScopeField accessScopeField)
    {
        List<AccessScopeField> list = accessScopeFieldService.selectAccessScopeFieldList(accessScopeField);
        ExcelUtil<AccessScopeField> util = new ExcelUtil<AccessScopeField>(AccessScopeField.class);
        util.exportExcel(response, list, "接入范围字段数据");
    }

    /**
     * 获取接入范围字段详细信息
     */
    @RequiresPermissions("project1:accessScopeField:query")
    @GetMapping(value = "/{scopeFieldId}")
    public AjaxResult getInfo(@PathVariable("scopeFieldId") Long scopeFieldId)
    {
        return success(accessScopeFieldService.selectAccessScopeFieldByScopeFieldId(scopeFieldId));
    }

    /**
     * 新增接入范围字段
     */
    @RequiresPermissions("project1:accessScopeField:add")
    @Log(title = "接入范围字段", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AccessScopeField accessScopeField)
    {
        return toAjax(accessScopeFieldService.insertAccessScopeField(accessScopeField));
    }

    /**
     * 修改接入范围字段
     */
    @RequiresPermissions("project1:accessScopeField:edit")
    @Log(title = "接入范围字段", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AccessScopeField accessScopeField)
    {
        return toAjax(accessScopeFieldService.updateAccessScopeField(accessScopeField));
    }

    /**
     * 删除接入范围字段
     */
    @RequiresPermissions("project1:accessScopeField:remove")
    @Log(title = "接入范围字段", businessType = BusinessType.DELETE)
	@DeleteMapping("/{scopeFieldIds}")
    public AjaxResult remove(@PathVariable Long[] scopeFieldIds)
    {
        return toAjax(accessScopeFieldService.deleteAccessScopeFieldByScopeFieldIds(scopeFieldIds));
    }
}
