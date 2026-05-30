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
import com.ruoyi.topic5.domain.T5DiagnosisEvidence;
import com.ruoyi.topic5.service.IT5DiagnosisEvidenceService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 课题五-诊断证据Controller
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@RestController
@RequestMapping("/evidence")
public class T5DiagnosisEvidenceController extends BaseController
{
    @Autowired
    private IT5DiagnosisEvidenceService t5DiagnosisEvidenceService;

    /**
     * 查询课题五-诊断证据列表
     */
    @RequiresPermissions("topic5:evidence:list")
    @GetMapping("/list")
    public TableDataInfo list(T5DiagnosisEvidence t5DiagnosisEvidence)
    {
        startPage();
        List<T5DiagnosisEvidence> list = t5DiagnosisEvidenceService.selectT5DiagnosisEvidenceList(t5DiagnosisEvidence);
        return getDataTable(list);
    }

    /**
     * 导出课题五-诊断证据列表
     */
    @RequiresPermissions("topic5:evidence:export")
    @Log(title = "课题五-诊断证据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, T5DiagnosisEvidence t5DiagnosisEvidence)
    {
        List<T5DiagnosisEvidence> list = t5DiagnosisEvidenceService.selectT5DiagnosisEvidenceList(t5DiagnosisEvidence);
        ExcelUtil<T5DiagnosisEvidence> util = new ExcelUtil<T5DiagnosisEvidence>(T5DiagnosisEvidence.class);
        util.exportExcel(response, list, "课题五-诊断证据数据");
    }

    /**
     * 获取课题五-诊断证据详细信息
     */
    @RequiresPermissions("topic5:evidence:query")
    @GetMapping(value = "/{evidenceId}")
    public AjaxResult getInfo(@PathVariable("evidenceId") Long evidenceId)
    {
        return success(t5DiagnosisEvidenceService.selectT5DiagnosisEvidenceByEvidenceId(evidenceId));
    }

    /**
     * 新增课题五-诊断证据
     */
    @RequiresPermissions("topic5:evidence:add")
    @Log(title = "课题五-诊断证据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody T5DiagnosisEvidence t5DiagnosisEvidence)
    {
        return toAjax(t5DiagnosisEvidenceService.insertT5DiagnosisEvidence(t5DiagnosisEvidence));
    }

    /**
     * 修改课题五-诊断证据
     */
    @RequiresPermissions("topic5:evidence:edit")
    @Log(title = "课题五-诊断证据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody T5DiagnosisEvidence t5DiagnosisEvidence)
    {
        return toAjax(t5DiagnosisEvidenceService.updateT5DiagnosisEvidence(t5DiagnosisEvidence));
    }

    /**
     * 删除课题五-诊断证据
     */
    @RequiresPermissions("topic5:evidence:remove")
    @Log(title = "课题五-诊断证据", businessType = BusinessType.DELETE)
	@DeleteMapping("/{evidenceIds}")
    public AjaxResult remove(@PathVariable Long[] evidenceIds)
    {
        return toAjax(t5DiagnosisEvidenceService.deleteT5DiagnosisEvidenceByEvidenceIds(evidenceIds));
    }
}
