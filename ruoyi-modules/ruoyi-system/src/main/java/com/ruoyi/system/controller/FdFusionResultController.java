package com.ruoyi.system.controller;

import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.domain.FdFusionResult;
import com.ruoyi.system.service.IFdFusionResultService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.system.domain.dto.FusionRunDto;
import java.util.List;
import com.ruoyi.system.domain.dto.FusionRunDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
/**
 * 故障诊断-融合征结果Controller
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
@RestController
@RequestMapping("/resultoffu")
public class FdFusionResultController extends BaseController
{
    @Autowired
    private IFdFusionResultService fdFusionResultService;

    /**
     * 查询故障诊断-融合征结果列表
     */
    @RequiresPermissions("@ss.hasPermi('system:resultoffu:list')")
    @GetMapping("/list")
    public TableDataInfo list(FdFusionResult fdFusionResult)
    {
        startPage();
        List<FdFusionResult> list = fdFusionResultService.selectFdFusionResultList(fdFusionResult);
        return getDataTable(list);
    }

    /**
     * 导出故障诊断-融合征结果列表
     */
    @RequiresPermissions("@ss.hasPermi('system:resultoffu:export')")
    @Log(title = "故障诊断-融合征结果", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FdFusionResult fdFusionResult)
    {
        List<FdFusionResult> list = fdFusionResultService.selectFdFusionResultList(fdFusionResult);
        ExcelUtil<FdFusionResult> util = new ExcelUtil<FdFusionResult>(FdFusionResult.class);
        util.exportExcel(response, list, "故障诊断-融合征结果数据");
    }

    /**
     * 获取故障诊断-融合征结果详细信息
     */
    @RequiresPermissions("@ss.hasPermi('system:resultoffu:query')")
    @GetMapping(value = "/{fusionId}")
    public AjaxResult getInfo(@PathVariable("fusionId") Long fusionId)
    {
        return success(fdFusionResultService.selectFdFusionResultByFusionId(fusionId));
    }

    /**
     * 新增故障诊断-融合征结果
     */
    @RequiresPermissions("system:resultoffu:add")
    @Log(title = "故障诊断-融合征结果", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FdFusionResult fdFusionResult)
    {
        return toAjax(fdFusionResultService.insertFdFusionResult(fdFusionResult));
    }

    /**
     * 修改故障诊断-融合征结果
     */
    @RequiresPermissions("@ss.hasPermi('system:resultoffu:edit')")
    @Log(title = "故障诊断-融合征结果", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FdFusionResult fdFusionResult)
    {
        return toAjax(fdFusionResultService.updateFdFusionResult(fdFusionResult));
    }

    /**
     * 删除故障诊断-融合征结果
     */
    @RequiresPermissions("@ss.hasPermi('system:resultoffu:remove')")
    @Log(title = "故障诊断-融合征结果", businessType = BusinessType.DELETE)
	@DeleteMapping("/{fusionIds}")
    public AjaxResult remove(@PathVariable Long[] fusionIds)
    {
        return toAjax(fdFusionResultService.deleteFdFusionResultByFusionIds(fusionIds));
    }
    /**
     * 执行特征融合算法
     */
    @PostMapping("/run")
    public AjaxResult run(@RequestBody FusionRunDto dto)
    {
        return fdFusionResultService.runFusion(dto);
    }
}
