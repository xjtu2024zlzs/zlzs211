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
import com.ruoyi.project1.domain.TaskMetric;
import com.ruoyi.project1.service.ITaskMetricService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 模式映射评估指标Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/taskMetric")
public class TaskMetricController extends BaseController
{
    @Autowired
    private ITaskMetricService taskMetricService;

    /**
     * 查询模式映射评估指标列表
     */
    @RequiresPermissions("project1:taskMetric:list")
    @GetMapping("/list")
    public TableDataInfo list(TaskMetric taskMetric)
    {
        startPage();
        List<TaskMetric> list = taskMetricService.selectTaskMetricList(taskMetric);
        return getDataTable(list);
    }

    /**
     * 导出模式映射评估指标列表
     */
    @RequiresPermissions("project1:taskMetric:export")
    @Log(title = "模式映射评估指标", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TaskMetric taskMetric)
    {
        List<TaskMetric> list = taskMetricService.selectTaskMetricList(taskMetric);
        ExcelUtil<TaskMetric> util = new ExcelUtil<TaskMetric>(TaskMetric.class);
        util.exportExcel(response, list, "模式映射评估指标数据");
    }

    /**
     * 获取模式映射评估指标详细信息
     */
    @RequiresPermissions("project1:taskMetric:query")
    @GetMapping(value = "/{metricId}")
    public AjaxResult getInfo(@PathVariable("metricId") Long metricId)
    {
        return success(taskMetricService.selectTaskMetricByMetricId(metricId));
    }

    /**
     * 新增模式映射评估指标
     */
    @RequiresPermissions("project1:taskMetric:add")
    @Log(title = "模式映射评估指标", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TaskMetric taskMetric)
    {
        return toAjax(taskMetricService.insertTaskMetric(taskMetric));
    }

    /**
     * 修改模式映射评估指标
     */
    @RequiresPermissions("project1:taskMetric:edit")
    @Log(title = "模式映射评估指标", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TaskMetric taskMetric)
    {
        return toAjax(taskMetricService.updateTaskMetric(taskMetric));
    }

    /**
     * 删除模式映射评估指标
     */
    @RequiresPermissions("project1:taskMetric:remove")
    @Log(title = "模式映射评估指标", businessType = BusinessType.DELETE)
	@DeleteMapping("/{metricIds}")
    public AjaxResult remove(@PathVariable Long[] metricIds)
    {
        return toAjax(taskMetricService.deleteTaskMetricByMetricIds(metricIds));
    }
}
