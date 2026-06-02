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
import com.ruoyi.topic5.domain.T5TraceCase;
import com.ruoyi.topic5.service.IT5TraceCaseService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 追溯案例Controller
 *
 * @author ruoyi
 * @date 2026-05-31
 */
@RestController
@RequestMapping("/traceCase")
public class T5TraceCaseController extends BaseController
{
    @Autowired
    private IT5TraceCaseService t5TraceCaseService;

    /**
     * 查询追溯案例列表
     */
    @RequiresPermissions("topic5:traceCase:list")
    @GetMapping("/list")
    public TableDataInfo list(T5TraceCase t5TraceCase)
    {
        startPage();
        List<T5TraceCase> list = t5TraceCaseService.selectT5TraceCaseList(t5TraceCase);
        return getDataTable(list);
    }

    /**
     * 导出追溯案例列表
     */
    @RequiresPermissions("topic5:traceCase:export")
    @Log(title = "追溯案例", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, T5TraceCase t5TraceCase)
    {
        List<T5TraceCase> list = t5TraceCaseService.selectT5TraceCaseList(t5TraceCase);
        ExcelUtil<T5TraceCase> util = new ExcelUtil<T5TraceCase>(T5TraceCase.class);
        util.exportExcel(response, list, "追溯案例数据");
    }

    /**
     * 获取追溯案例详细信息
     */
    @RequiresPermissions("topic5:traceCase:query")
    @GetMapping(value = "/{caseId}")
    public AjaxResult getInfo(@PathVariable("caseId") Long caseId)
    {
        return success(t5TraceCaseService.selectT5TraceCaseByCaseId(caseId));
    }

    /**
     * 新增追溯案例
     */
    @RequiresPermissions("topic5:traceCase:add")
    @Log(title = "追溯案例", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody T5TraceCase t5TraceCase)
    {
        int rows = t5TraceCaseService.insertT5TraceCase(t5TraceCase);
        if (rows > 0)
        {
            return AjaxResult.success(t5TraceCase);
        }
        return AjaxResult.error("新增追溯案例失败");
    }

    /**
     * 修改追溯案例
     */
    @RequiresPermissions("topic5:traceCase:edit")
    @Log(title = "追溯案例", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody T5TraceCase t5TraceCase)
    {
        return toAjax(t5TraceCaseService.updateT5TraceCase(t5TraceCase));
    }

    /**
     * 删除追溯案例
     */
    @RequiresPermissions("topic5:traceCase:remove")
    @Log(title = "追溯案例", businessType = BusinessType.DELETE)
    @DeleteMapping("/{caseIds}")
    public AjaxResult remove(@PathVariable Long[] caseIds)
    {
        return toAjax(t5TraceCaseService.deleteT5TraceCaseByCaseIds(caseIds));
    }

    /**
     * 运行故障零件定位算法
     */
    @RequiresPermissions("topic5:traceCase:locatePart")
    @Log(title = "故障零件定位", businessType = BusinessType.OTHER)
    @PostMapping("/locatePart/{caseId}")
    public AjaxResult locatePart(@PathVariable("caseId") Long caseId, Long algorithmId)
    {
        return t5TraceCaseService.locatePart(caseId, algorithmId);
    }
}