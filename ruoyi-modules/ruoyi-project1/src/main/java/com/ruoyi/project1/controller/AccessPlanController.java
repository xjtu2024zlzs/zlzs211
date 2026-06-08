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
import com.ruoyi.project1.domain.AccessPlan;
import com.ruoyi.project1.service.IAccessPlanService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 数据接入管理Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/accessPlan")
public class AccessPlanController extends BaseController
{
    @Autowired
    private IAccessPlanService accessPlanService;

    /**
     * 查询数据接入管理列表
     */
    @RequiresPermissions("project1:accessPlan:list")
    @GetMapping("/list")
    public TableDataInfo list(AccessPlan accessPlan)
    {
        startPage();
        List<AccessPlan> list = accessPlanService.selectAccessPlanList(accessPlan);
        return getDataTable(list);
    }

    /**
     * 导出数据接入管理列表
     */
    @RequiresPermissions("project1:accessPlan:export")
    @Log(title = "数据接入管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AccessPlan accessPlan)
    {
        List<AccessPlan> list = accessPlanService.selectAccessPlanList(accessPlan);
        ExcelUtil<AccessPlan> util = new ExcelUtil<AccessPlan>(AccessPlan.class);
        util.exportExcel(response, list, "数据接入管理数据");
    }

    /**
     * 获取数据接入管理详细信息
     */
    @RequiresPermissions("project1:accessPlan:query")
    @GetMapping(value = "/{accessPlanId}")
    public AjaxResult getInfo(@PathVariable("accessPlanId") Long accessPlanId)
    {
        return success(accessPlanService.selectAccessPlanByAccessPlanId(accessPlanId));
    }

    /**
     * 新增数据接入管理
     */
    @RequiresPermissions("project1:accessPlan:add")
    @Log(title = "数据接入管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AccessPlan accessPlan)
    {
        return toAjax(accessPlanService.insertAccessPlan(accessPlan));
    }

    /**
     * 修改数据接入管理
     */
    @RequiresPermissions("project1:accessPlan:edit")
    @Log(title = "数据接入管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AccessPlan accessPlan)
    {
        return toAjax(accessPlanService.updateAccessPlan(accessPlan));
    }

    /**
     * 执行接入计划
     */
    @RequiresPermissions("project1:accessPlan:edit")
    @Log(title = "执行接入计划", businessType = BusinessType.UPDATE)
    @PostMapping("/execute/{accessPlanId}")
    public AjaxResult execute(@PathVariable("accessPlanId") Long accessPlanId)
    {
        return success(accessPlanService.execute(accessPlanId));
    }

    /**
     * 暂停接入计划
     */
    @RequiresPermissions("project1:accessPlan:edit")
    @Log(title = "暂停接入计划", businessType = BusinessType.UPDATE)
    @PostMapping("/pause/{accessPlanId}")
    public AjaxResult pause(@PathVariable("accessPlanId") Long accessPlanId)
    {
        return toAjax(accessPlanService.pause(accessPlanId));
    }

    /**
     * 恢复接入计划
     */
    @RequiresPermissions("project1:accessPlan:edit")
    @Log(title = "恢复接入计划", businessType = BusinessType.UPDATE)
    @PostMapping("/resume/{accessPlanId}")
    public AjaxResult resume(@PathVariable("accessPlanId") Long accessPlanId)
    {
        return toAjax(accessPlanService.resume(accessPlanId));
    }

    /**
     * 取消当前接入执行
     */
    @RequiresPermissions("project1:accessPlan:edit")
    @Log(title = "取消接入执行", businessType = BusinessType.UPDATE)
    @PostMapping("/cancel/{accessPlanId}")
    public AjaxResult cancel(@PathVariable("accessPlanId") Long accessPlanId)
    {
        return toAjax(accessPlanService.cancel(accessPlanId));
    }

    /**
     * 删除数据接入管理
     */
    @RequiresPermissions("project1:accessPlan:remove")
    @Log(title = "数据接入管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{accessPlanIds}")
    public AjaxResult remove(@PathVariable Long[] accessPlanIds)
    {
        return toAjax(accessPlanService.deleteAccessPlanByAccessPlanIds(accessPlanIds));
    }
}
