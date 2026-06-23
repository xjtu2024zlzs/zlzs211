package com.ruoyi.project4.controller;

import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.project4.domain.FdDiagnosisResult;
import com.ruoyi.project4.domain.dto.DiagnosisRunDto;
import com.ruoyi.project4.service.IFdDiagnosisResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 故障诊断-诊断结果Controller
 *
 * 注意：
 * 微服务版中，前端请求路径是 /project4/resultofgr/list。
 * Gateway 会把 /system/** 转发到 ruoyi-system 服务。
 * 所以当前 Controller 只写 /resultofgr，不要写 /project4/resultofgr。
 *
 * @author ruoyi
 * @date 2026-05-29
 */
@RestController
@RequestMapping("/resultofgr")
public class FdDiagnosisResultController extends BaseController
{
    @Autowired
    private IFdDiagnosisResultService fdDiagnosisResultService;

    /**
     * 查询故障诊断-诊断结果列表
     */
    @RequiresPermissions("project4:resultofgr:list")
    @GetMapping("/list")
    public TableDataInfo list(FdDiagnosisResult fdDiagnosisResult)
    {
        startPage();
        List<FdDiagnosisResult> list = fdDiagnosisResultService.selectFdDiagnosisResultList(fdDiagnosisResult);
        return getDataTable(list);
    }

    /**
     * 导出故障诊断-诊断结果列表
     */
    @RequiresPermissions("project4:resultofgr:export")
    @Log(title = "故障诊断-诊断结果", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FdDiagnosisResult fdDiagnosisResult)
    {
        List<FdDiagnosisResult> list = fdDiagnosisResultService.selectFdDiagnosisResultList(fdDiagnosisResult);
        ExcelUtil<FdDiagnosisResult> util = new ExcelUtil<FdDiagnosisResult>(FdDiagnosisResult.class);
        util.exportExcel(response, list, "故障诊断-诊断结果数据");
    }

    /**
     * 获取故障诊断-诊断结果详细信息
     */
    @RequiresPermissions("project4:resultofgr:query")
    @GetMapping(value = "/{diagnosisId}")
    public AjaxResult getInfo(@PathVariable("diagnosisId") Long diagnosisId)
    {
        return success(fdDiagnosisResultService.selectFdDiagnosisResultByDiagnosisId(diagnosisId));
    }

    /**
     * 新增故障诊断-诊断结果
     */
    @RequiresPermissions("project4:resultofgr:add")
    @Log(title = "故障诊断-诊断结果", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FdDiagnosisResult fdDiagnosisResult)
    {
        return toAjax(fdDiagnosisResultService.insertFdDiagnosisResult(fdDiagnosisResult));
    }

    /**
     * 修改故障诊断-诊断结果
     */
    @RequiresPermissions("project4:resultofgr:edit")
    @Log(title = "故障诊断-诊断结果", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FdDiagnosisResult fdDiagnosisResult)
    {
        return toAjax(fdDiagnosisResultService.updateFdDiagnosisResult(fdDiagnosisResult));
    }

    /**
     * 删除故障诊断-诊断结果
     */
    @RequiresPermissions("project4:resultofgr:remove")
    @Log(title = "故障诊断-诊断结果", businessType = BusinessType.DELETE)
    @DeleteMapping("/{diagnosisIds}")
    public AjaxResult remove(@PathVariable Long[] diagnosisIds)
    {
        return toAjax(fdDiagnosisResultService.deleteFdDiagnosisResultByDiagnosisIds(diagnosisIds));
    }

    /**
     * 执行故障诊断算法
     *
     * 说明：
     * - 为了先测试 Java 调 Python 是否能跑通，这里暂时不加权限注解。
     * - 等 PowerShell 和前端都调通后，可以再加：
     *   @RequiresPermissions("project4:resultofgr:execute")
     */
    @PostMapping("/run")
    public AjaxResult run(@RequestBody DiagnosisRunDto dto)
    {
        return fdDiagnosisResultService.runDiagnosis(dto);
    }
}

