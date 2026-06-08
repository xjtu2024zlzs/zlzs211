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
import com.ruoyi.project1.domain.EncodingMode;
import com.ruoyi.project1.service.IEncodingModeService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 编码模式配置Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/encodingMode")
public class EncodingModeController extends BaseController
{
    @Autowired
    private IEncodingModeService encodingModeService;

    /**
     * 查询编码模式配置列表
     */
    @RequiresPermissions("project1:encodingMode:list")
    @GetMapping("/list")
    public TableDataInfo list(EncodingMode encodingMode)
    {
        startPage();
        List<EncodingMode> list = encodingModeService.selectEncodingModeList(encodingMode);
        return getDataTable(list);
    }

    /**
     * 导出编码模式配置列表
     */
    @RequiresPermissions("project1:encodingMode:export")
    @Log(title = "编码模式配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, EncodingMode encodingMode)
    {
        List<EncodingMode> list = encodingModeService.selectEncodingModeList(encodingMode);
        ExcelUtil<EncodingMode> util = new ExcelUtil<EncodingMode>(EncodingMode.class);
        util.exportExcel(response, list, "编码模式配置数据");
    }

    /**
     * 获取编码模式配置详细信息
     */
    @RequiresPermissions("project1:encodingMode:query")
    @GetMapping(value = "/{encodingModeKey}")
    public AjaxResult getInfo(@PathVariable("encodingModeKey") String encodingModeKey)
    {
        return success(encodingModeService.selectEncodingModeByEncodingModeKey(encodingModeKey));
    }

    /**
     * 新增编码模式配置
     */
    @RequiresPermissions("project1:encodingMode:add")
    @Log(title = "编码模式配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody EncodingMode encodingMode)
    {
        return toAjax(encodingModeService.insertEncodingMode(encodingMode));
    }

    /**
     * 修改编码模式配置
     */
    @RequiresPermissions("project1:encodingMode:edit")
    @Log(title = "编码模式配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody EncodingMode encodingMode)
    {
        return toAjax(encodingModeService.updateEncodingMode(encodingMode));
    }

    /**
     * 删除编码模式配置
     */
    @RequiresPermissions("project1:encodingMode:remove")
    @Log(title = "编码模式配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{encodingModeKeys}")
    public AjaxResult remove(@PathVariable String[] encodingModeKeys)
    {
        return toAjax(encodingModeService.deleteEncodingModeByEncodingModeKeys(encodingModeKeys));
    }
}
