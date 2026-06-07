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
import com.ruoyi.project1.domain.AccessFieldResult;
import com.ruoyi.project1.service.IAccessFieldResultService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 目标字段接入结果Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/accessFieldResult")
public class AccessFieldResultController extends BaseController
{
    @Autowired
    private IAccessFieldResultService accessFieldResultService;

    /**
     * 查询目标字段接入结果列表
     */
    @RequiresPermissions("project1:accessFieldResult:list")
    @GetMapping("/list")
    public TableDataInfo list(AccessFieldResult accessFieldResult)
    {
        startPage();
        List<AccessFieldResult> list = accessFieldResultService.selectAccessFieldResultList(accessFieldResult);
        return getDataTable(list);
    }

    /**
     * 导出目标字段接入结果列表
     */
    @RequiresPermissions("project1:accessFieldResult:export")
    @Log(title = "目标字段接入结果", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AccessFieldResult accessFieldResult)
    {
        List<AccessFieldResult> list = accessFieldResultService.selectAccessFieldResultList(accessFieldResult);
        ExcelUtil<AccessFieldResult> util = new ExcelUtil<AccessFieldResult>(AccessFieldResult.class);
        util.exportExcel(response, list, "目标字段接入结果数据");
    }

    /**
     * 获取目标字段接入结果详细信息
     */
    @RequiresPermissions("project1:accessFieldResult:query")
    @GetMapping(value = "/{fieldResultId}")
    public AjaxResult getInfo(@PathVariable("fieldResultId") Long fieldResultId)
    {
        return success(accessFieldResultService.selectAccessFieldResultByFieldResultId(fieldResultId));
    }

    /**
     * 新增目标字段接入结果
     */
    @RequiresPermissions("project1:accessFieldResult:add")
    @Log(title = "目标字段接入结果", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AccessFieldResult accessFieldResult)
    {
        return toAjax(accessFieldResultService.insertAccessFieldResult(accessFieldResult));
    }

    /**
     * 修改目标字段接入结果
     */
    @RequiresPermissions("project1:accessFieldResult:edit")
    @Log(title = "目标字段接入结果", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AccessFieldResult accessFieldResult)
    {
        return toAjax(accessFieldResultService.updateAccessFieldResult(accessFieldResult));
    }

    /**
     * 删除目标字段接入结果
     */
    @RequiresPermissions("project1:accessFieldResult:remove")
    @Log(title = "目标字段接入结果", businessType = BusinessType.DELETE)
	@DeleteMapping("/{fieldResultIds}")
    public AjaxResult remove(@PathVariable Long[] fieldResultIds)
    {
        return toAjax(accessFieldResultService.deleteAccessFieldResultByFieldResultIds(fieldResultIds));
    }
}
