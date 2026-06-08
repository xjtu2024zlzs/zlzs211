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
import com.ruoyi.project1.domain.MatchTask;
import com.ruoyi.project1.service.IMatchTaskService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 模式映射任务创建Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/matchTask")
public class MatchTaskController extends BaseController
{
    @Autowired
    private IMatchTaskService matchTaskService;

    /**
     * 查询模式映射任务创建列表
     */
    @RequiresPermissions("project1:matchTask:list")
    @GetMapping("/list")
    public TableDataInfo list(MatchTask matchTask)
    {
        startPage();
        List<MatchTask> list = matchTaskService.selectMatchTaskList(matchTask);
        return getDataTable(list);
    }

    /**
     * 导出模式映射任务创建列表
     */
    @RequiresPermissions("project1:matchTask:export")
    @Log(title = "模式映射任务创建", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MatchTask matchTask)
    {
        List<MatchTask> list = matchTaskService.selectMatchTaskList(matchTask);
        ExcelUtil<MatchTask> util = new ExcelUtil<MatchTask>(MatchTask.class);
        util.exportExcel(response, list, "模式映射任务创建数据");
    }

    /**
     * 查询任务版本列表
     */
    @RequiresPermissions("project1:matchTask:query")
    @GetMapping("/version/{taskId}")
    public AjaxResult versions(@PathVariable("taskId") Long taskId)
    {
        return success(matchTaskService.versions(taskId));
    }

    /**
     * 查询任务运行记录列表
     */
    @RequiresPermissions("project1:matchTask:query")
    @GetMapping("/record/{taskId}")
    public AjaxResult records(@PathVariable("taskId") Long taskId)
    {
        return success(matchTaskService.records(taskId));
    }

    /**
     * 查询单次运行记录状态，用于前端进度条轮询。
     */
    @RequiresPermissions("project1:matchTask:query")
    @GetMapping("/record/status/{recordId}")
    public AjaxResult recordStatus(@PathVariable("recordId") Long recordId)
    {
        return success(matchTaskService.recordStatus(recordId));
    }

    /**
     * 获取模式映射任务创建详细信息
     */
    @RequiresPermissions("project1:matchTask:query")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable("taskId") Long taskId)
    {
        return success(matchTaskService.selectMatchTaskByTaskId(taskId));
    }

    /**
     * 新增模式映射任务创建
     */
    @RequiresPermissions("project1:matchTask:add")
    @Log(title = "模式映射任务创建", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MatchTask matchTask)
    {
        return toAjax(matchTaskService.insertMatchTask(matchTask));
    }

    /**
     * 修改模式映射任务创建
     */
    @RequiresPermissions("project1:matchTask:edit")
    @Log(title = "模式映射任务创建", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MatchTask matchTask)
    {
        return toAjax(matchTaskService.updateMatchTask(matchTask));
    }

    /**
     * 无算法模式运行任务，生成默认结果集
     */
    @RequiresPermissions("project1:matchTask:edit")
    @Log(title = "模式映射任务运行", businessType = BusinessType.UPDATE)
    @PostMapping("/run/{taskId}")
    public AjaxResult run(@PathVariable("taskId") Long taskId)
    {
        return success(matchTaskService.runMatchTask(taskId));
    }

    /**
     * 删除模式映射任务创建
     */
    @RequiresPermissions("project1:matchTask:remove")
    @Log(title = "模式映射任务创建", businessType = BusinessType.DELETE)
	@DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(matchTaskService.deleteMatchTaskByTaskIds(taskIds));
    }
}
