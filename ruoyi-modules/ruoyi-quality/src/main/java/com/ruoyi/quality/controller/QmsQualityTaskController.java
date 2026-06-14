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
import com.ruoyi.quality.domain.QmsQualityTask;
import com.ruoyi.quality.service.IQmsQualityTaskService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 质量问题模块处理任务Controller
 * 
 * @author ruoyi
 * @date 2026-06-13
 */
@RestController
@RequestMapping("/qms/task")
public class QmsQualityTaskController extends BaseController
{
    @Autowired
    private IQmsQualityTaskService qmsQualityTaskService;

    /**
     * 查询质量问题模块处理任务列表
     */
    //@RequiresPermissions("quality:task:list")
    @GetMapping("/list")
    public TableDataInfo list(QmsQualityTask qmsQualityTask)
    {
        startPage();
        List<QmsQualityTask> list = qmsQualityTaskService.selectQmsQualityTaskList(qmsQualityTask);
        return getDataTable(list);
    }

    /**
     * 导出质量问题模块处理任务列表
     */
    //@RequiresPermissions("quality:task:export")
    @Log(title = "质量问题模块处理任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsQualityTask qmsQualityTask)
    {
        List<QmsQualityTask> list = qmsQualityTaskService.selectQmsQualityTaskList(qmsQualityTask);
        ExcelUtil<QmsQualityTask> util = new ExcelUtil<QmsQualityTask>(QmsQualityTask.class);
        util.exportExcel(response, list, "质量问题模块处理任务数据");
    }

    /**
     * 获取质量问题模块处理任务详细信息
     */
    //@RequiresPermissions("quality:task:query")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable("taskId") Long taskId)
    {
        return success(qmsQualityTaskService.selectQmsQualityTaskByTaskId(taskId));
    }

    /**
     * 新增质量问题模块处理任务
     */
    //@RequiresPermissions("quality:task:add")
    @Log(title = "质量问题模块处理任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QmsQualityTask qmsQualityTask)
    {
        return toAjax(qmsQualityTaskService.insertQmsQualityTask(qmsQualityTask));
    }

    /**
     * 修改质量问题模块处理任务
     */
    //@RequiresPermissions("quality:task:edit")
    @Log(title = "质量问题模块处理任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QmsQualityTask qmsQualityTask)
    {
        return toAjax(qmsQualityTaskService.updateQmsQualityTask(qmsQualityTask));
    }

    /**
     * 删除质量问题模块处理任务
     */
    //@RequiresPermissions("quality:task:remove")
    @Log(title = "质量问题模块处理任务", businessType = BusinessType.DELETE)
	@DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(qmsQualityTaskService.deleteQmsQualityTaskByTaskIds(taskIds));
    }
}
