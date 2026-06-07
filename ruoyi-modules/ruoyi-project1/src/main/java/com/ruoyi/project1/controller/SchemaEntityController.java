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
import com.ruoyi.project1.domain.SchemaEntity;
import com.ruoyi.project1.service.ISchemaEntityService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 模式实体Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/schemaEntity")
public class SchemaEntityController extends BaseController
{
    @Autowired
    private ISchemaEntityService schemaEntityService;

    /**
     * 查询模式实体列表
     */
    @RequiresPermissions("project1:schemaEntity:list")
    @GetMapping("/list")
    public TableDataInfo list(SchemaEntity schemaEntity)
    {
        startPage();
        List<SchemaEntity> list = schemaEntityService.selectSchemaEntityList(schemaEntity);
        return getDataTable(list);
    }

    /**
     * 导出模式实体列表
     */
    @RequiresPermissions("project1:schemaEntity:export")
    @Log(title = "模式实体", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SchemaEntity schemaEntity)
    {
        List<SchemaEntity> list = schemaEntityService.selectSchemaEntityList(schemaEntity);
        ExcelUtil<SchemaEntity> util = new ExcelUtil<SchemaEntity>(SchemaEntity.class);
        util.exportExcel(response, list, "模式实体数据");
    }

    /**
     * 获取模式实体详细信息
     */
    @RequiresPermissions("project1:schemaEntity:query")
    @GetMapping(value = "/{entityId}")
    public AjaxResult getInfo(@PathVariable("entityId") Long entityId)
    {
        return success(schemaEntityService.selectSchemaEntityByEntityId(entityId));
    }

    /**
     * 新增模式实体
     */
    @RequiresPermissions("project1:schemaEntity:add")
    @Log(title = "模式实体", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SchemaEntity schemaEntity)
    {
        return toAjax(schemaEntityService.insertSchemaEntity(schemaEntity));
    }

    /**
     * 修改模式实体
     */
    @RequiresPermissions("project1:schemaEntity:edit")
    @Log(title = "模式实体", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SchemaEntity schemaEntity)
    {
        return toAjax(schemaEntityService.updateSchemaEntity(schemaEntity));
    }

    /**
     * 删除模式实体
     */
    @RequiresPermissions("project1:schemaEntity:remove")
    @Log(title = "模式实体", businessType = BusinessType.DELETE)
	@DeleteMapping("/{entityIds}")
    public AjaxResult remove(@PathVariable Long[] entityIds)
    {
        return toAjax(schemaEntityService.deleteSchemaEntityByEntityIds(entityIds));
    }
}
