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
import com.ruoyi.topic5.domain.T5AlgorithmRun;
import com.ruoyi.topic5.service.IT5AlgorithmRunService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 课题五-算法运行记录Controller
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@RestController
@RequestMapping("/algorithmRun")
public class T5AlgorithmRunController extends BaseController
{
    @Autowired
    private IT5AlgorithmRunService t5AlgorithmRunService;

    /**
     * 查询课题五-算法运行记录列表
     */
    @RequiresPermissions("topic5:algorithmRun:list")
    @GetMapping("/list")
    public TableDataInfo list(T5AlgorithmRun t5AlgorithmRun)
    {
        startPage();
        List<T5AlgorithmRun> list = t5AlgorithmRunService.selectT5AlgorithmRunList(t5AlgorithmRun);
        return getDataTable(list);
    }

    /**
     * 导出课题五-算法运行记录列表
     */
    @RequiresPermissions("topic5:algorithmRun:export")
    @Log(title = "课题五-算法运行记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, T5AlgorithmRun t5AlgorithmRun)
    {
        List<T5AlgorithmRun> list = t5AlgorithmRunService.selectT5AlgorithmRunList(t5AlgorithmRun);
        ExcelUtil<T5AlgorithmRun> util = new ExcelUtil<T5AlgorithmRun>(T5AlgorithmRun.class);
        util.exportExcel(response, list, "课题五-算法运行记录数据");
    }

    /**
     * 获取课题五-算法运行记录详细信息
     */
    @RequiresPermissions("topic5:algorithmRun:query")
    @GetMapping(value = "/{runId}")
    public AjaxResult getInfo(@PathVariable("runId") Long runId)
    {
        return success(t5AlgorithmRunService.selectT5AlgorithmRunByRunId(runId));
    }

    /**
     * 新增课题五-算法运行记录
     */
    @RequiresPermissions("topic5:algorithmRun:add")
    @Log(title = "课题五-算法运行记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody T5AlgorithmRun t5AlgorithmRun)
    {
        return toAjax(t5AlgorithmRunService.insertT5AlgorithmRun(t5AlgorithmRun));
    }

    /**
     * 修改课题五-算法运行记录
     */
    @RequiresPermissions("topic5:algorithmRun:edit")
    @Log(title = "课题五-算法运行记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody T5AlgorithmRun t5AlgorithmRun)
    {
        return toAjax(t5AlgorithmRunService.updateT5AlgorithmRun(t5AlgorithmRun));
    }

    /**
     * 删除课题五-算法运行记录
     */
    @RequiresPermissions("topic5:algorithmRun:remove")
    @Log(title = "课题五-算法运行记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{runIds}")
    public AjaxResult remove(@PathVariable Long[] runIds)
    {
        return toAjax(t5AlgorithmRunService.deleteT5AlgorithmRunByRunIds(runIds));
    }
}
