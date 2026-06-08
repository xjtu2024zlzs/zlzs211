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
import com.ruoyi.project1.domain.MappingSpec;
import com.ruoyi.project1.service.IMappingSpecService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 最终接入字段规则Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/mappingSpec")
public class MappingSpecController extends BaseController
{
    @Autowired
    private IMappingSpecService mappingSpecService;

    /**
     * 查询最终接入字段规则列表
     */
    @RequiresPermissions("project1:mappingSpec:list")
    @GetMapping("/list")
    public TableDataInfo list(MappingSpec mappingSpec)
    {
        startPage();
        List<MappingSpec> list = mappingSpecService.selectMappingSpecList(mappingSpec);
        return getDataTable(list);
    }

    /**
     * 导出最终接入字段规则列表
     */
    @RequiresPermissions("project1:mappingSpec:export")
    @Log(title = "最终接入字段规则", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MappingSpec mappingSpec)
    {
        List<MappingSpec> list = mappingSpecService.selectMappingSpecList(mappingSpec);
        ExcelUtil<MappingSpec> util = new ExcelUtil<MappingSpec>(MappingSpec.class);
        util.exportExcel(response, list, "最终接入字段规则数据");
    }

    /**
     * 获取最终接入字段规则详细信息
     */
    @RequiresPermissions("project1:mappingSpec:query")
    @GetMapping(value = "/{mappingId}")
    public AjaxResult getInfo(@PathVariable("mappingId") Long mappingId)
    {
        return success(mappingSpecService.selectMappingSpecByMappingId(mappingId));
    }

    /**
     * 新增最终接入字段规则
     */
    @RequiresPermissions("project1:mappingSpec:add")
    @Log(title = "最终接入字段规则", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MappingSpec mappingSpec)
    {
        return toAjax(mappingSpecService.insertMappingSpec(mappingSpec));
    }

    /**
     * 修改最终接入字段规则
     */
    @RequiresPermissions("project1:mappingSpec:edit")
    @Log(title = "最终接入字段规则", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MappingSpec mappingSpec)
    {
        return toAjax(mappingSpecService.updateMappingSpec(mappingSpec));
    }

    /**
     * 删除最终接入字段规则
     */
    @RequiresPermissions("project1:mappingSpec:remove")
    @Log(title = "最终接入字段规则", businessType = BusinessType.DELETE)
	@DeleteMapping("/{mappingIds}")
    public AjaxResult remove(@PathVariable Long[] mappingIds)
    {
        return toAjax(mappingSpecService.deleteMappingSpecByMappingIds(mappingIds));
    }
}
