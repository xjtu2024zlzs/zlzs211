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
import com.ruoyi.topic5.domain.T5DossierSampleRecord;
import com.ruoyi.topic5.service.IT5DossierSampleRecordService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 数字卷宗模拟数据Controller
 * 
 * @author ruoyi
 * @date 2026-06-02
 */
@RestController
@RequestMapping("/dossierSampleRecord")
public class T5DossierSampleRecordController extends BaseController
{
    @Autowired
    private IT5DossierSampleRecordService t5DossierSampleRecordService;

    /**
     * 查询数字卷宗模拟数据列表
     */
    @RequiresPermissions("topic5:dossierSampleRecord:list")
    @GetMapping("/list")
    public TableDataInfo list(T5DossierSampleRecord t5DossierSampleRecord)
    {
        startPage();
        List<T5DossierSampleRecord> list = t5DossierSampleRecordService.selectT5DossierSampleRecordList(t5DossierSampleRecord);
        return getDataTable(list);
    }

    /**
     * 导出数字卷宗模拟数据列表
     */
    @RequiresPermissions("topic5:dossierSampleRecord:export")
    @Log(title = "数字卷宗模拟数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, T5DossierSampleRecord t5DossierSampleRecord)
    {
        List<T5DossierSampleRecord> list = t5DossierSampleRecordService.selectT5DossierSampleRecordList(t5DossierSampleRecord);
        ExcelUtil<T5DossierSampleRecord> util = new ExcelUtil<T5DossierSampleRecord>(T5DossierSampleRecord.class);
        util.exportExcel(response, list, "数字卷宗模拟数据数据");
    }

    /**
     * 获取数字卷宗模拟数据详细信息
     */
    @RequiresPermissions("topic5:dossierSampleRecord:query")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return success(t5DossierSampleRecordService.selectT5DossierSampleRecordByRecordId(recordId));
    }

    /**
     * 新增数字卷宗模拟数据
     */
    @RequiresPermissions("topic5:dossierSampleRecord:add")
    @Log(title = "数字卷宗模拟数据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody T5DossierSampleRecord t5DossierSampleRecord)
    {
        return toAjax(t5DossierSampleRecordService.insertT5DossierSampleRecord(t5DossierSampleRecord));
    }

    /**
     * 修改数字卷宗模拟数据
     */
    @RequiresPermissions("topic5:dossierSampleRecord:edit")
    @Log(title = "数字卷宗模拟数据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody T5DossierSampleRecord t5DossierSampleRecord)
    {
        return toAjax(t5DossierSampleRecordService.updateT5DossierSampleRecord(t5DossierSampleRecord));
    }

    /**
     * 删除数字卷宗模拟数据
     */
    @RequiresPermissions("topic5:dossierSampleRecord:remove")
    @Log(title = "数字卷宗模拟数据", businessType = BusinessType.DELETE)
	@DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(t5DossierSampleRecordService.deleteT5DossierSampleRecordByRecordIds(recordIds));
    }
}
