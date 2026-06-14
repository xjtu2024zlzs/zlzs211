package com.ruoyi.quality.controller;

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
import com.ruoyi.quality.domain.QmsWorkModule;
import com.ruoyi.quality.service.IQmsWorkModuleService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 质量问题工作模块配置Controller
 * 
 * @author ruoyi
 * @date 2026-06-13
 */
@RestController
@RequestMapping("/qms/module")
public class QmsWorkModuleController extends BaseController
{
    @Autowired
    private IQmsWorkModuleService qmsWorkModuleService;

    /**
     * 查询质量问题工作模块配置列表
     */
    //@RequiresPermissions("quality:module:list")
    @GetMapping("/list")
    public TableDataInfo list(QmsWorkModule qmsWorkModule)
    {
        startPage();
        List<QmsWorkModule> list = qmsWorkModuleService.selectQmsWorkModuleList(qmsWorkModule);
        return getDataTable(list);
    }

    /**
     * 导出质量问题工作模块配置列表
     */
    //@RequiresPermissions("quality:module:export")
    @Log(title = "质量问题工作模块配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsWorkModule qmsWorkModule)
    {
        List<QmsWorkModule> list = qmsWorkModuleService.selectQmsWorkModuleList(qmsWorkModule);
        ExcelUtil<QmsWorkModule> util = new ExcelUtil<QmsWorkModule>(QmsWorkModule.class);
        util.exportExcel(response, list, "质量问题工作模块配置数据");
    }

    /**
     * 获取质量问题工作模块配置详细信息
     */
    //@RequiresPermissions("quality:module:query")
    @GetMapping(value = "/{moduleId}")
    public AjaxResult getInfo(@PathVariable("moduleId") Long moduleId)
    {
        return success(qmsWorkModuleService.selectQmsWorkModuleByModuleId(moduleId));
    }

    /**
     * 新增质量问题工作模块配置
     */
    //@RequiresPermissions("quality:module:add")
    @Log(title = "质量问题工作模块配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QmsWorkModule qmsWorkModule)
    {
        return toAjax(qmsWorkModuleService.insertQmsWorkModule(qmsWorkModule));
    }

    /**
     * 修改质量问题工作模块配置
     */
    //@RequiresPermissions("quality:module:edit")
    @Log(title = "质量问题工作模块配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QmsWorkModule qmsWorkModule)
    {
        return toAjax(qmsWorkModuleService.updateQmsWorkModule(qmsWorkModule));
    }

    /**
     * 删除质量问题工作模块配置
     */
    //@RequiresPermissions("quality:module:remove")
    @Log(title = "质量问题工作模块配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{moduleIds}")
    public AjaxResult remove(@PathVariable Long[] moduleIds)
    {
        return toAjax(qmsWorkModuleService.deleteQmsWorkModuleByModuleIds(moduleIds));
    }
}
