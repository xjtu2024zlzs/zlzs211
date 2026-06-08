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
import com.ruoyi.project1.domain.MappingSpecSet;
import com.ruoyi.project1.service.IMappingSpecSetService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 最终接入规则集Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/mappingSpecSet")
public class MappingSpecSetController extends BaseController
{
    @Autowired
    private IMappingSpecSetService mappingSpecSetService;

    /**
     * 查询最终接入规则集列表
     */
    @RequiresPermissions("project1:mappingSpecSet:list")
    @GetMapping("/list")
    public TableDataInfo list(MappingSpecSet mappingSpecSet)
    {
        startPage();
        List<MappingSpecSet> list = mappingSpecSetService.selectMappingSpecSetList(mappingSpecSet);
        return getDataTable(list);
    }

    /**
     * 导出最终接入规则集列表
     */
    @RequiresPermissions("project1:mappingSpecSet:export")
    @Log(title = "最终接入规则集", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MappingSpecSet mappingSpecSet)
    {
        List<MappingSpecSet> list = mappingSpecSetService.selectMappingSpecSetList(mappingSpecSet);
        ExcelUtil<MappingSpecSet> util = new ExcelUtil<MappingSpecSet>(MappingSpecSet.class);
        util.exportExcel(response, list, "最终接入规则集数据");
    }

    /**
     * 获取最终接入规则集详细信息
     */
    @RequiresPermissions("project1:mappingSpecSet:query")
    @GetMapping(value = "/{specSetId}")
    public AjaxResult getInfo(@PathVariable("specSetId") Long specSetId)
    {
        return success(mappingSpecSetService.selectMappingSpecSetBySpecSetId(specSetId));
    }

    /**
     * 新增最终接入规则集
     */
    @RequiresPermissions("project1:mappingSpecSet:add")
    @Log(title = "最终接入规则集", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MappingSpecSet mappingSpecSet)
    {
        return toAjax(mappingSpecSetService.insertMappingSpecSet(mappingSpecSet));
    }

    /**
     * 修改最终接入规则集
     */
    @RequiresPermissions("project1:mappingSpecSet:edit")
    @Log(title = "最终接入规则集", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MappingSpecSet mappingSpecSet)
    {
        return toAjax(mappingSpecSetService.updateMappingSpecSet(mappingSpecSet));
    }

    /**
     * 删除最终接入规则集
     */
    @RequiresPermissions("project1:mappingSpecSet:remove")
    @Log(title = "最终接入规则集", businessType = BusinessType.DELETE)
	@DeleteMapping("/{specSetIds}")
    public AjaxResult remove(@PathVariable Long[] specSetIds)
    {
        return toAjax(mappingSpecSetService.deleteMappingSpecSetBySpecSetIds(specSetIds));
    }
}
