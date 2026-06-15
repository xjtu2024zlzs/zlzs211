package com.ruoyi.project4.controller;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.project4.domain.dto.AugmentRunDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.project4.domain.FdAugmentResult;
import com.ruoyi.project4.service.IFdAugmentResultService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.project4.domain.dto.AugmentRunDto;
import java.util.List;

/**
 * 故障诊断-样本增强结果Controller
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
@RestController
@RequestMapping("/resultofen")
public class FdAugmentResultController extends BaseController
{
    @Autowired
    private IFdAugmentResultService fdAugmentResultService;

    /**
     * 查询故障诊断-样本增强结果列表
     */
    @RequiresPermissions("project4:fileofda:list")
    @GetMapping("/list")
    public TableDataInfo list(FdAugmentResult fdAugmentResult)
    {
        startPage();
        List<FdAugmentResult> list = fdAugmentResultService.selectFdAugmentResultList(fdAugmentResult);
        return getDataTable(list);
    }

    @PostMapping("/augment")
    public AjaxResult augment(@RequestBody AugmentRunDto dto)
    {
        return fdAugmentResultService.runAugment(dto);
    }

    /**
     * 导出故障诊断-样本增强结果列表
     */
    @RequiresPermissions("@ss.hasPermi('system:resultofen:export')")
    @Log(title = "故障诊断-样本增强结果", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FdAugmentResult fdAugmentResult)
    {
        List<FdAugmentResult> list = fdAugmentResultService.selectFdAugmentResultList(fdAugmentResult);
        ExcelUtil<FdAugmentResult> util = new ExcelUtil<FdAugmentResult>(FdAugmentResult.class);
        util.exportExcel(response, list, "故障诊断-样本增强结果数据");
    }

    /**
     * 获取故障诊断-样本增强结果详细信息
     */
    @RequiresPermissions("@ss.hasPermi('system:resultofen:query')")
    @GetMapping(value = "/{augmentId}")
    public AjaxResult getInfo(@PathVariable("augmentId") Long augmentId)
    {
        return success(fdAugmentResultService.selectFdAugmentResultByAugmentId(augmentId));
    }

    /**
     * 新增故障诊断-样本增强结果
     */
    @RequiresPermissions("project4:resultofen:add")
    @Log(title = "故障诊断-样本增强结果", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FdAugmentResult fdAugmentResult)
    {
        return toAjax(fdAugmentResultService.insertFdAugmentResult(fdAugmentResult));
    }

    /**
     * 修改故障诊断-样本增强结果
     */
    @RequiresPermissions("@ss.hasPermi('system:resultofen:edit')")
    @Log(title = "故障诊断-样本增强结果", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FdAugmentResult fdAugmentResult)
    {
        return toAjax(fdAugmentResultService.updateFdAugmentResult(fdAugmentResult));
    }

    /**
     * 删除故障诊断-样本增强结果
     */
    @RequiresPermissions("@ss.hasPermi('system:resultofen:remove')")
    @Log(title = "故障诊断-样本增强结果", businessType = BusinessType.DELETE)
	@DeleteMapping("/{augmentIds}")
    public AjaxResult remove(@PathVariable Long[] augmentIds)
    {
        return toAjax(fdAugmentResultService.deleteFdAugmentResultByAugmentIds(augmentIds));
    }
    /**
     * 执行样本增强算法
     */
    @PostMapping("/run")
    public AjaxResult run(@RequestBody AugmentRunDto dto)
    {
        return fdAugmentResultService.runAugment(dto);
    }

}
