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
import com.ruoyi.quality.domain.QmsFlowLog;
import com.ruoyi.quality.service.IQmsFlowLogService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 质量问题流程日志Controller
 * 
 * @author ruoyi
 * @date 2026-06-13
 */
@RestController
@RequestMapping("/qms/log")
public class QmsFlowLogController extends BaseController
{
    @Autowired
    private IQmsFlowLogService qmsFlowLogService;

    /**
     * 查询质量问题流程日志列表
     */
    //@RequiresPermissions("quality:log:list")
    @GetMapping("/list")
    public TableDataInfo list(QmsFlowLog qmsFlowLog)
    {
        startPage();
        List<QmsFlowLog> list = qmsFlowLogService.selectQmsFlowLogList(qmsFlowLog);
        return getDataTable(list);
    }

    /**
     * 导出质量问题流程日志列表
     */
    //@RequiresPermissions("quality:log:export")
    @Log(title = "质量问题流程日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsFlowLog qmsFlowLog)
    {
        List<QmsFlowLog> list = qmsFlowLogService.selectQmsFlowLogList(qmsFlowLog);
        ExcelUtil<QmsFlowLog> util = new ExcelUtil<QmsFlowLog>(QmsFlowLog.class);
        util.exportExcel(response, list, "质量问题流程日志数据");
    }

    /**
     * 获取质量问题流程日志详细信息
     */
    //@RequiresPermissions("quality:log:query")
    @GetMapping(value = "/{logId}")
    public AjaxResult getInfo(@PathVariable("logId") Long logId)
    {
        return success(qmsFlowLogService.selectQmsFlowLogByLogId(logId));
    }

    /**
     * 新增质量问题流程日志
     */
    //@RequiresPermissions("quality:log:add")
    @Log(title = "质量问题流程日志", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QmsFlowLog qmsFlowLog)
    {
        return toAjax(qmsFlowLogService.insertQmsFlowLog(qmsFlowLog));
    }

    /**
     * 修改质量问题流程日志
     */
    //@RequiresPermissions("quality:log:edit")
    @Log(title = "质量问题流程日志", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QmsFlowLog qmsFlowLog)
    {
        return toAjax(qmsFlowLogService.updateQmsFlowLog(qmsFlowLog));
    }

    /**
     * 删除质量问题流程日志
     */
    //@RequiresPermissions("quality:log:remove")
    @Log(title = "质量问题流程日志", businessType = BusinessType.DELETE)
	@DeleteMapping("/{logIds}")
    public AjaxResult remove(@PathVariable Long[] logIds)
    {
        return toAjax(qmsFlowLogService.deleteQmsFlowLogByLogIds(logIds));
    }
}
