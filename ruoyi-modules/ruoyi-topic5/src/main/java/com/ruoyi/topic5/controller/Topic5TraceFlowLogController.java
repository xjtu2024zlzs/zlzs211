package com.ruoyi.topic5.controller;

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
import com.ruoyi.topic5.domain.Topic5TraceFlowLog;
import com.ruoyi.topic5.service.ITopic5TraceFlowLogService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 追溯流程记录Controller
 * 
 * @author ruoyi
 * @date 2026-06-03
 */
@RestController
@RequestMapping("/traceLog")
public class Topic5TraceFlowLogController extends BaseController
{
    @Autowired
    private ITopic5TraceFlowLogService topic5TraceFlowLogService;

    /**
     * 查询追溯流程记录列表
     */
    @RequiresPermissions("topic5:traceLog:list")
    @GetMapping("/list")
    public TableDataInfo list(Topic5TraceFlowLog topic5TraceFlowLog)
    {
        startPage();
        List<Topic5TraceFlowLog> list = topic5TraceFlowLogService.selectTopic5TraceFlowLogList(topic5TraceFlowLog);
        return getDataTable(list);
    }

    /**
     * 导出追溯流程记录列表
     */
    @RequiresPermissions("topic5:traceLog:export")
    @Log(title = "追溯流程记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Topic5TraceFlowLog topic5TraceFlowLog)
    {
        List<Topic5TraceFlowLog> list = topic5TraceFlowLogService.selectTopic5TraceFlowLogList(topic5TraceFlowLog);
        ExcelUtil<Topic5TraceFlowLog> util = new ExcelUtil<Topic5TraceFlowLog>(Topic5TraceFlowLog.class);
        util.exportExcel(response, list, "追溯流程记录数据");
    }

    /**
     * 获取追溯流程记录详细信息
     */
    @RequiresPermissions("topic5:traceLog:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(topic5TraceFlowLogService.selectTopic5TraceFlowLogById(id));
    }

    /**
     * 新增追溯流程记录
     */
    @RequiresPermissions("topic5:traceLog:add")
    @Log(title = "追溯流程记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Topic5TraceFlowLog topic5TraceFlowLog)
    {
        return toAjax(topic5TraceFlowLogService.insertTopic5TraceFlowLog(topic5TraceFlowLog));
    }

    /**
     * 修改追溯流程记录
     */
    @RequiresPermissions("topic5:traceLog:edit")
    @Log(title = "追溯流程记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Topic5TraceFlowLog topic5TraceFlowLog)
    {
        return toAjax(topic5TraceFlowLogService.updateTopic5TraceFlowLog(topic5TraceFlowLog));
    }

    /**
     * 删除追溯流程记录
     */
    @RequiresPermissions("topic5:traceLog:remove")
    @Log(title = "追溯流程记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(topic5TraceFlowLogService.deleteTopic5TraceFlowLogByIds(ids));
    }
}
