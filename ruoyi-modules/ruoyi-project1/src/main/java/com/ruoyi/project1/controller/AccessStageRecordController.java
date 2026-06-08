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
import com.ruoyi.project1.domain.AccessStageRecord;
import com.ruoyi.project1.service.IAccessStageRecordService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 接入中间库原始记录Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/accessStageRecord")
public class AccessStageRecordController extends BaseController
{
    @Autowired
    private IAccessStageRecordService accessStageRecordService;

    /**
     * 查询接入中间库原始记录列表
     */
    @RequiresPermissions("project1:accessStageRecord:list")
    @GetMapping("/list")
    public TableDataInfo list(AccessStageRecord accessStageRecord)
    {
        startPage();
        List<AccessStageRecord> list = accessStageRecordService.selectAccessStageRecordList(accessStageRecord);
        return getDataTable(list);
    }

    /**
     * 导出接入中间库原始记录列表
     */
    @RequiresPermissions("project1:accessStageRecord:export")
    @Log(title = "接入中间库原始记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AccessStageRecord accessStageRecord)
    {
        List<AccessStageRecord> list = accessStageRecordService.selectAccessStageRecordList(accessStageRecord);
        ExcelUtil<AccessStageRecord> util = new ExcelUtil<AccessStageRecord>(AccessStageRecord.class);
        util.exportExcel(response, list, "接入中间库原始记录数据");
    }

    /**
     * 获取接入中间库原始记录详细信息
     */
    @RequiresPermissions("project1:accessStageRecord:query")
    @GetMapping(value = "/{stageRecordId}")
    public AjaxResult getInfo(@PathVariable("stageRecordId") Long stageRecordId)
    {
        return success(accessStageRecordService.selectAccessStageRecordByStageRecordId(stageRecordId));
    }

    /**
     * 新增接入中间库原始记录
     */
    @RequiresPermissions("project1:accessStageRecord:add")
    @Log(title = "接入中间库原始记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AccessStageRecord accessStageRecord)
    {
        return toAjax(accessStageRecordService.insertAccessStageRecord(accessStageRecord));
    }

    /**
     * 修改接入中间库原始记录
     */
    @RequiresPermissions("project1:accessStageRecord:edit")
    @Log(title = "接入中间库原始记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AccessStageRecord accessStageRecord)
    {
        return toAjax(accessStageRecordService.updateAccessStageRecord(accessStageRecord));
    }

    /**
     * 删除接入中间库原始记录
     */
    @RequiresPermissions("project1:accessStageRecord:remove")
    @Log(title = "接入中间库原始记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{stageRecordIds}")
    public AjaxResult remove(@PathVariable Long[] stageRecordIds)
    {
        return toAjax(accessStageRecordService.deleteAccessStageRecordByStageRecordIds(stageRecordIds));
    }
}
