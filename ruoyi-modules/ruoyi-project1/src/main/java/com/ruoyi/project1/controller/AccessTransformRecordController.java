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
import com.ruoyi.project1.domain.AccessTransformRecord;
import com.ruoyi.project1.service.IAccessTransformRecordService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 接入转换记录Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/accessTransformRecord")
public class AccessTransformRecordController extends BaseController
{
    @Autowired
    private IAccessTransformRecordService accessTransformRecordService;

    /**
     * 查询接入转换记录列表
     */
    @RequiresPermissions("project1:accessTransformRecord:list")
    @GetMapping("/list")
    public TableDataInfo list(AccessTransformRecord accessTransformRecord)
    {
        startPage();
        List<AccessTransformRecord> list = accessTransformRecordService.selectAccessTransformRecordList(accessTransformRecord);
        return getDataTable(list);
    }

    /**
     * 导出接入转换记录列表
     */
    @RequiresPermissions("project1:accessTransformRecord:export")
    @Log(title = "接入转换记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AccessTransformRecord accessTransformRecord)
    {
        List<AccessTransformRecord> list = accessTransformRecordService.selectAccessTransformRecordList(accessTransformRecord);
        ExcelUtil<AccessTransformRecord> util = new ExcelUtil<AccessTransformRecord>(AccessTransformRecord.class);
        util.exportExcel(response, list, "接入转换记录数据");
    }

    /**
     * 获取接入转换记录详细信息
     */
    @RequiresPermissions("project1:accessTransformRecord:query")
    @GetMapping(value = "/{transformRecordId}")
    public AjaxResult getInfo(@PathVariable("transformRecordId") Long transformRecordId)
    {
        return success(accessTransformRecordService.selectAccessTransformRecordByTransformRecordId(transformRecordId));
    }

    /**
     * 新增接入转换记录
     */
    @RequiresPermissions("project1:accessTransformRecord:add")
    @Log(title = "接入转换记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AccessTransformRecord accessTransformRecord)
    {
        return toAjax(accessTransformRecordService.insertAccessTransformRecord(accessTransformRecord));
    }

    /**
     * 修改接入转换记录
     */
    @RequiresPermissions("project1:accessTransformRecord:edit")
    @Log(title = "接入转换记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AccessTransformRecord accessTransformRecord)
    {
        return toAjax(accessTransformRecordService.updateAccessTransformRecord(accessTransformRecord));
    }

    /**
     * 删除接入转换记录
     */
    @RequiresPermissions("project1:accessTransformRecord:remove")
    @Log(title = "接入转换记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{transformRecordIds}")
    public AjaxResult remove(@PathVariable Long[] transformRecordIds)
    {
        return toAjax(accessTransformRecordService.deleteAccessTransformRecordByTransformRecordIds(transformRecordIds));
    }
}
