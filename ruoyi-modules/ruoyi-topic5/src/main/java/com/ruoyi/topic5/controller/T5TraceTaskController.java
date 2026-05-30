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
import com.ruoyi.topic5.domain.T5TraceTask;
import com.ruoyi.topic5.service.IT5TraceTaskService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 课题五-追溯任务主Controller
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@RestController
@RequestMapping("/task")
public class T5TraceTaskController extends BaseController
{
    @Autowired
    private IT5TraceTaskService t5TraceTaskService;

    /**
     * 查询课题五-追溯任务主列表
     */
    @RequiresPermissions("topic5:task:list")
    @GetMapping("/list")
    public TableDataInfo list(T5TraceTask t5TraceTask)
    {
        startPage();
        List<T5TraceTask> list = t5TraceTaskService.selectT5TraceTaskList(t5TraceTask);
        return getDataTable(list);
    }

    /**
     * 导出课题五-追溯任务主列表
     */
    @RequiresPermissions("topic5:task:export")
    @Log(title = "课题五-追溯任务主", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, T5TraceTask t5TraceTask)
    {
        List<T5TraceTask> list = t5TraceTaskService.selectT5TraceTaskList(t5TraceTask);
        ExcelUtil<T5TraceTask> util = new ExcelUtil<T5TraceTask>(T5TraceTask.class);
        util.exportExcel(response, list, "课题五-追溯任务主数据");
    }

    /**
     * 获取课题五-追溯任务主详细信息
     */
    @RequiresPermissions("topic5:task:query")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable("taskId") Long taskId)
    {
        return success(t5TraceTaskService.selectT5TraceTaskByTaskId(taskId));
    }

    /**
     * 新增课题五-追溯任务主
     */
    @RequiresPermissions("topic5:task:add")
    @Log(title = "课题五-追溯任务主", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody T5TraceTask t5TraceTask)
    {
        return toAjax(t5TraceTaskService.insertT5TraceTask(t5TraceTask));
    }

    /**
     * 修改课题五-追溯任务主
     */
    @RequiresPermissions("topic5:task:edit")
    @Log(title = "课题五-追溯任务主", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody T5TraceTask t5TraceTask)
    {
        return toAjax(t5TraceTaskService.updateT5TraceTask(t5TraceTask));
    }

    /**
     * 删除课题五-追溯任务主
     */
    @RequiresPermissions("topic5:task:remove")
    @Log(title = "课题五-追溯任务主", businessType = BusinessType.DELETE)
	@DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(t5TraceTaskService.deleteT5TraceTaskByTaskIds(taskIds));
    }
}
