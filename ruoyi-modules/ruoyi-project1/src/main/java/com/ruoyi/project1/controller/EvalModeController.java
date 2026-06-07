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
import com.ruoyi.project1.domain.EvalMode;
import com.ruoyi.project1.service.IEvalModeService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 评估模式配置Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/evalMode")
public class EvalModeController extends BaseController
{
    @Autowired
    private IEvalModeService evalModeService;

    /**
     * 查询评估模式配置列表
     */
    @RequiresPermissions("project1:evalMode:list")
    @GetMapping("/list")
    public TableDataInfo list(EvalMode evalMode)
    {
        startPage();
        List<EvalMode> list = evalModeService.selectEvalModeList(evalMode);
        return getDataTable(list);
    }

    /**
     * 导出评估模式配置列表
     */
    @RequiresPermissions("project1:evalMode:export")
    @Log(title = "评估模式配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, EvalMode evalMode)
    {
        List<EvalMode> list = evalModeService.selectEvalModeList(evalMode);
        ExcelUtil<EvalMode> util = new ExcelUtil<EvalMode>(EvalMode.class);
        util.exportExcel(response, list, "评估模式配置数据");
    }

    /**
     * 获取评估模式配置详细信息
     */
    @RequiresPermissions("project1:evalMode:query")
    @GetMapping(value = "/{evalModeKey}")
    public AjaxResult getInfo(@PathVariable("evalModeKey") String evalModeKey)
    {
        return success(evalModeService.selectEvalModeByEvalModeKey(evalModeKey));
    }

    /**
     * 新增评估模式配置
     */
    @RequiresPermissions("project1:evalMode:add")
    @Log(title = "评估模式配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody EvalMode evalMode)
    {
        return toAjax(evalModeService.insertEvalMode(evalMode));
    }

    /**
     * 修改评估模式配置
     */
    @RequiresPermissions("project1:evalMode:edit")
    @Log(title = "评估模式配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody EvalMode evalMode)
    {
        return toAjax(evalModeService.updateEvalMode(evalMode));
    }

    /**
     * 删除评估模式配置
     */
    @RequiresPermissions("project1:evalMode:remove")
    @Log(title = "评估模式配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{evalModeKeys}")
    public AjaxResult remove(@PathVariable String[] evalModeKeys)
    {
        return toAjax(evalModeService.deleteEvalModeByEvalModeKeys(evalModeKeys));
    }
}
