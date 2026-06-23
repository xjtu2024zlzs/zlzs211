package com.ruoyi.project4.controller;

import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.project4.domain.FdDataFile;
import com.ruoyi.project4.service.IFdDataFileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import com.ruoyi.project4.domain.dto.PreprocessRunDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
/**
 * 故障诊断-原始数据文件Controller
 * 
 * @author ruoyi
 * @date 2026-05-29
 */
@RestController
@RequestMapping("/fileofda")
public class FdDataFileController extends BaseController
{
    @Autowired
    private IFdDataFileService fdDataFileService;
    /**
     * 执行数据预处理
     */
    @PostMapping("/preprocess")
    public AjaxResult preprocess(@RequestBody PreprocessRunDto dto)
    {
        return fdDataFileService.runPreprocess(dto);
    }
    /**
     * 查询故障诊断-原始数据文件列表
     */
    @RequiresPermissions("@ss.hasPermi('system:fileofda:list')")
    @GetMapping("/list")
    public TableDataInfo list(FdDataFile fdDataFile)
    {
        startPage();
        List<FdDataFile> list = fdDataFileService.selectFdDataFileList(fdDataFile);
        return getDataTable(list);
    }

    /**
     * 导出故障诊断-原始数据文件列表
     */
    @RequiresPermissions("@ss.hasPermi('system:fileofda:export')")
    @Log(title = "故障诊断-原始数据文件", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FdDataFile fdDataFile)
    {
        List<FdDataFile> list = fdDataFileService.selectFdDataFileList(fdDataFile);
        ExcelUtil<FdDataFile> util = new ExcelUtil<FdDataFile>(FdDataFile.class);
        util.exportExcel(response, list, "故障诊断-原始数据文件数据");
    }

    /**
     * 获取故障诊断-原始数据文件详细信息
     */
    @RequiresPermissions("@ss.hasPermi('system:fileofda:query')")
    @GetMapping(value = "/{fileId}")
    public AjaxResult getInfo(@PathVariable("fileId") Long fileId)
    {
        return success(fdDataFileService.selectFdDataFileByFileId(fileId));
    }

    /**
     * 新增故障诊断-原始数据文件
     */
    @RequiresPermissions("project4:fileofda:add")
    @Log(title = "故障诊断-原始数据文件", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FdDataFile fdDataFile)
    {
        return toAjax(fdDataFileService.insertFdDataFile(fdDataFile));
    }

    /**
     * 修改故障诊断-原始数据文件
     */
    @RequiresPermissions("@ss.hasPermi('system:fileofda:edit')")
    @Log(title = "故障诊断-原始数据文件", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FdDataFile fdDataFile)
    {
        return toAjax(fdDataFileService.updateFdDataFile(fdDataFile));
    }

    /**
     * 删除故障诊断-原始数据文件
     */
    @RequiresPermissions("@ss.hasPermi('system:fileofda:remove')")
    @Log(title = "故障诊断-原始数据文件", businessType = BusinessType.DELETE)
	@DeleteMapping("/{fileIds}")
    public AjaxResult remove(@PathVariable Long[] fileIds)
    {
        return toAjax(fdDataFileService.deleteFdDataFileByFileIds(fileIds));
    }
    /**
     * 查询真实预处理样本列表
     */
    @GetMapping("/rawSamples")
    public AjaxResult rawSamples()
    {
        return fdDataFileService.listRawSamples();
    }
}

