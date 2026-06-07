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
import com.ruoyi.project1.domain.SchemaField;
import com.ruoyi.project1.service.ISchemaFieldService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 模式字段Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/schemaField")
public class SchemaFieldController extends BaseController
{
    @Autowired
    private ISchemaFieldService schemaFieldService;

    /**
     * 查询模式字段列表
     */
    @RequiresPermissions("project1:schemaField:list")
    @GetMapping("/list")
    public TableDataInfo list(SchemaField schemaField)
    {
        startPage();
        List<SchemaField> list = schemaFieldService.selectSchemaFieldList(schemaField);
        return getDataTable(list);
    }

    /**
     * 导出模式字段列表
     */
    @RequiresPermissions("project1:schemaField:export")
    @Log(title = "模式字段", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SchemaField schemaField)
    {
        List<SchemaField> list = schemaFieldService.selectSchemaFieldList(schemaField);
        ExcelUtil<SchemaField> util = new ExcelUtil<SchemaField>(SchemaField.class);
        util.exportExcel(response, list, "模式字段数据");
    }

    /**
     * 获取模式字段详细信息
     */
    @RequiresPermissions("project1:schemaField:query")
    @GetMapping(value = "/{fieldId}")
    public AjaxResult getInfo(@PathVariable("fieldId") Long fieldId)
    {
        return success(schemaFieldService.selectSchemaFieldByFieldId(fieldId));
    }

    /**
     * 新增模式字段
     */
    @RequiresPermissions("project1:schemaField:add")
    @Log(title = "模式字段", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SchemaField schemaField)
    {
        return toAjax(schemaFieldService.insertSchemaField(schemaField));
    }

    /**
     * 修改模式字段
     */
    @RequiresPermissions("project1:schemaField:edit")
    @Log(title = "模式字段", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SchemaField schemaField)
    {
        return toAjax(schemaFieldService.updateSchemaField(schemaField));
    }

    /**
     * 删除模式字段
     */
    @RequiresPermissions("project1:schemaField:remove")
    @Log(title = "模式字段", businessType = BusinessType.DELETE)
	@DeleteMapping("/{fieldIds}")
    public AjaxResult remove(@PathVariable Long[] fieldIds)
    {
        return toAjax(schemaFieldService.deleteSchemaFieldByFieldIds(fieldIds));
    }
}
