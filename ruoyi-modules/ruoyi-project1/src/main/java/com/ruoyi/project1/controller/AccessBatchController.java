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
import com.ruoyi.project1.domain.AccessBatch;
import com.ruoyi.project1.service.IAccessBatchService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 接入执行批次Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/accessBatch")
public class AccessBatchController extends BaseController
{
    @Autowired
    private IAccessBatchService accessBatchService;

    /**
     * 查询接入执行批次列表
     */
    @RequiresPermissions("project1:accessBatch:list")
    @GetMapping("/list")
    public TableDataInfo list(AccessBatch accessBatch)
    {
        startPage();
        List<AccessBatch> list = accessBatchService.selectAccessBatchList(accessBatch);
        return getDataTable(list);
    }

    /**
     * 导出接入执行批次列表
     */
    @RequiresPermissions("project1:accessBatch:export")
    @Log(title = "接入执行批次", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AccessBatch accessBatch)
    {
        List<AccessBatch> list = accessBatchService.selectAccessBatchList(accessBatch);
        ExcelUtil<AccessBatch> util = new ExcelUtil<AccessBatch>(AccessBatch.class);
        util.exportExcel(response, list, "接入执行批次数据");
    }

    /**
     * 获取接入执行批次详细信息
     */
    @RequiresPermissions("project1:accessBatch:query")
    @GetMapping(value = "/{accessBatchId}")
    public AjaxResult getInfo(@PathVariable("accessBatchId") Long accessBatchId)
    {
        return success(accessBatchService.selectAccessBatchByAccessBatchId(accessBatchId));
    }

    /**
     * 新增接入执行批次
     */
    @RequiresPermissions("project1:accessBatch:add")
    @Log(title = "接入执行批次", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AccessBatch accessBatch)
    {
        return toAjax(accessBatchService.insertAccessBatch(accessBatch));
    }

    /**
     * 修改接入执行批次
     */
    @RequiresPermissions("project1:accessBatch:edit")
    @Log(title = "接入执行批次", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AccessBatch accessBatch)
    {
        return toAjax(accessBatchService.updateAccessBatch(accessBatch));
    }

    /**
     * 删除接入执行批次
     */
    @RequiresPermissions("project1:accessBatch:remove")
    @Log(title = "接入执行批次", businessType = BusinessType.DELETE)
	@DeleteMapping("/{accessBatchIds}")
    public AjaxResult remove(@PathVariable Long[] accessBatchIds)
    {
        return toAjax(accessBatchService.deleteAccessBatchByAccessBatchIds(accessBatchIds));
    }
}
