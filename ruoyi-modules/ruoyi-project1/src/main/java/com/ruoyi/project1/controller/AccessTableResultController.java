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
import com.ruoyi.project1.domain.AccessTableResult;
import com.ruoyi.project1.service.IAccessTableResultService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 数据接入结果展示Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/accessResult")
public class AccessTableResultController extends BaseController
{
    @Autowired
    private IAccessTableResultService accessTableResultService;

    /**
     * 查询数据接入结果展示列表
     */
    @RequiresPermissions("project1:accessResult:list")
    @GetMapping("/list")
    public TableDataInfo list(AccessTableResult accessTableResult)
    {
        startPage();
        List<AccessTableResult> list = accessTableResultService.selectAccessTableResultList(accessTableResult);
        return getDataTable(list);
    }

    /**
     * 导出数据接入结果展示列表
     */
    @RequiresPermissions("project1:accessResult:export")
    @Log(title = "数据接入结果展示", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AccessTableResult accessTableResult)
    {
        List<AccessTableResult> list = accessTableResultService.selectAccessTableResultList(accessTableResult);
        ExcelUtil<AccessTableResult> util = new ExcelUtil<AccessTableResult>(AccessTableResult.class);
        util.exportExcel(response, list, "数据接入结果展示数据");
    }

    /**
     * 查询某个接入计划的图表看板数据
     */
    @RequiresPermissions("project1:accessResult:list")
    @GetMapping("/dashboard/{accessPlanId}")
    public AjaxResult dashboard(@PathVariable("accessPlanId") Long accessPlanId)
    {
        return success(accessTableResultService.dashboard(accessPlanId));
    }

    /**
     * 获取数据接入结果展示详细信息
     */
    @RequiresPermissions("project1:accessResult:query")
    @GetMapping(value = "/{tableResultId}")
    public AjaxResult getInfo(@PathVariable("tableResultId") Long tableResultId)
    {
        return success(accessTableResultService.selectAccessTableResultByTableResultId(tableResultId));
    }

    /**
     * 新增数据接入结果展示
     */
    @RequiresPermissions("project1:accessResult:add")
    @Log(title = "数据接入结果展示", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AccessTableResult accessTableResult)
    {
        return toAjax(accessTableResultService.insertAccessTableResult(accessTableResult));
    }

    /**
     * 修改数据接入结果展示
     */
    @RequiresPermissions("project1:accessResult:edit")
    @Log(title = "数据接入结果展示", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AccessTableResult accessTableResult)
    {
        return toAjax(accessTableResultService.updateAccessTableResult(accessTableResult));
    }

    /**
     * 汇总某个接入计划的结果
     */
    @RequiresPermissions("project1:accessResult:list")
    @GetMapping("/summary/{accessPlanId}")
    public AjaxResult summary(@PathVariable("accessPlanId") Long accessPlanId)
    {
        return success(accessTableResultService.summary(accessPlanId));
    }

    /**
     * 删除数据接入结果展示
     */
    @RequiresPermissions("project1:accessResult:remove")
    @Log(title = "数据接入结果展示", businessType = BusinessType.DELETE)
	@DeleteMapping("/{tableResultIds}")
    public AjaxResult remove(@PathVariable Long[] tableResultIds)
    {
        return toAjax(accessTableResultService.deleteAccessTableResultByTableResultIds(tableResultIds));
    }
}
