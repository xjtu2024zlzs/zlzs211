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
import com.ruoyi.topic5.domain.T5StageDiagnosis;
import com.ruoyi.topic5.service.IT5StageDiagnosisService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 课题五-故障阶段诊断结果Controller
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@RestController
@RequestMapping("/diagnosis")
public class T5StageDiagnosisController extends BaseController
{
    @Autowired
    private IT5StageDiagnosisService t5StageDiagnosisService;

    /**
     * 查询课题五-故障阶段诊断结果列表
     */
    @RequiresPermissions("topic5:diagnosis:list")
    @GetMapping("/list")
    public TableDataInfo list(T5StageDiagnosis t5StageDiagnosis)
    {
        startPage();
        List<T5StageDiagnosis> list = t5StageDiagnosisService.selectT5StageDiagnosisList(t5StageDiagnosis);
        return getDataTable(list);
    }

    /**
     * 导出课题五-故障阶段诊断结果列表
     */
    @RequiresPermissions("topic5:diagnosis:export")
    @Log(title = "课题五-故障阶段诊断结果", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, T5StageDiagnosis t5StageDiagnosis)
    {
        List<T5StageDiagnosis> list = t5StageDiagnosisService.selectT5StageDiagnosisList(t5StageDiagnosis);
        ExcelUtil<T5StageDiagnosis> util = new ExcelUtil<T5StageDiagnosis>(T5StageDiagnosis.class);
        util.exportExcel(response, list, "课题五-故障阶段诊断结果数据");
    }

    /**
     * 获取课题五-故障阶段诊断结果详细信息
     */
    @RequiresPermissions("topic5:diagnosis:query")
    @GetMapping(value = "/{diagnosisId}")
    public AjaxResult getInfo(@PathVariable("diagnosisId") Long diagnosisId)
    {
        return success(t5StageDiagnosisService.selectT5StageDiagnosisByDiagnosisId(diagnosisId));
    }

    /**
     * 新增课题五-故障阶段诊断结果
     */
    @RequiresPermissions("topic5:diagnosis:add")
    @Log(title = "课题五-故障阶段诊断结果", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody T5StageDiagnosis t5StageDiagnosis)
    {
        return toAjax(t5StageDiagnosisService.insertT5StageDiagnosis(t5StageDiagnosis));
    }

    /**
     * 修改课题五-故障阶段诊断结果
     */
    @RequiresPermissions("topic5:diagnosis:edit")
    @Log(title = "课题五-故障阶段诊断结果", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody T5StageDiagnosis t5StageDiagnosis)
    {
        return toAjax(t5StageDiagnosisService.updateT5StageDiagnosis(t5StageDiagnosis));
    }

    /**
     * 删除课题五-故障阶段诊断结果
     */
    @RequiresPermissions("topic5:diagnosis:remove")
    @Log(title = "课题五-故障阶段诊断结果", businessType = BusinessType.DELETE)
	@DeleteMapping("/{diagnosisIds}")
    public AjaxResult remove(@PathVariable Long[] diagnosisIds)
    {
        return toAjax(t5StageDiagnosisService.deleteT5StageDiagnosisByDiagnosisIds(diagnosisIds));
    }
}
