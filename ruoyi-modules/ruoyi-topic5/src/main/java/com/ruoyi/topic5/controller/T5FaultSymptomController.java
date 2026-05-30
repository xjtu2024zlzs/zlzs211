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
import com.ruoyi.topic5.domain.T5FaultSymptom;
import com.ruoyi.topic5.service.IT5FaultSymptomService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 课题五-故障征输入Controller
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@RestController
@RequestMapping("/symptom")
public class T5FaultSymptomController extends BaseController
{
    @Autowired
    private IT5FaultSymptomService t5FaultSymptomService;

    /**
     * 查询课题五-故障征输入列表
     */
    @RequiresPermissions("topic5:symptom:list")
    @GetMapping("/list")
    public TableDataInfo list(T5FaultSymptom t5FaultSymptom)
    {
        startPage();
        List<T5FaultSymptom> list = t5FaultSymptomService.selectT5FaultSymptomList(t5FaultSymptom);
        return getDataTable(list);
    }

    /**
     * 导出课题五-故障征输入列表
     */
    @RequiresPermissions("topic5:symptom:export")
    @Log(title = "课题五-故障征输入", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, T5FaultSymptom t5FaultSymptom)
    {
        List<T5FaultSymptom> list = t5FaultSymptomService.selectT5FaultSymptomList(t5FaultSymptom);
        ExcelUtil<T5FaultSymptom> util = new ExcelUtil<T5FaultSymptom>(T5FaultSymptom.class);
        util.exportExcel(response, list, "课题五-故障征输入数据");
    }

    /**
     * 获取课题五-故障征输入详细信息
     */
    @RequiresPermissions("topic5:symptom:query")
    @GetMapping(value = "/{symptomId}")
    public AjaxResult getInfo(@PathVariable("symptomId") Long symptomId)
    {
        return success(t5FaultSymptomService.selectT5FaultSymptomBySymptomId(symptomId));
    }

    /**
     * 新增课题五-故障征输入
     */
    @RequiresPermissions("topic5:symptom:add")
    @Log(title = "课题五-故障征输入", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody T5FaultSymptom t5FaultSymptom)
    {
        return toAjax(t5FaultSymptomService.insertT5FaultSymptom(t5FaultSymptom));
    }

    /**
     * 修改课题五-故障征输入
     */
    @RequiresPermissions("topic5:symptom:edit")
    @Log(title = "课题五-故障征输入", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody T5FaultSymptom t5FaultSymptom)
    {
        return toAjax(t5FaultSymptomService.updateT5FaultSymptom(t5FaultSymptom));
    }

    /**
     * 删除课题五-故障征输入
     */
    @RequiresPermissions("topic5:symptom:remove")
    @Log(title = "课题五-故障征输入", businessType = BusinessType.DELETE)
	@DeleteMapping("/{symptomIds}")
    public AjaxResult remove(@PathVariable Long[] symptomIds)
    {
        return toAjax(t5FaultSymptomService.deleteT5FaultSymptomBySymptomIds(symptomIds));
    }
}
