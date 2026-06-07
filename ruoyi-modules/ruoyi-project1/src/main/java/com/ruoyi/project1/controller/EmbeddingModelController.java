package com.ruoyi.project1.controller;

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
import com.ruoyi.project1.domain.EmbeddingModel;
import com.ruoyi.project1.service.IEmbeddingModelService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 嵌入模型配置Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/embeddingModel")
public class EmbeddingModelController extends BaseController
{
    @Autowired
    private IEmbeddingModelService embeddingModelService;

    /**
     * 查询嵌入模型配置列表
     */
    @RequiresPermissions("project1:embeddingModel:list")
    @GetMapping("/list")
    public TableDataInfo list(EmbeddingModel embeddingModel)
    {
        startPage();
        List<EmbeddingModel> list = embeddingModelService.selectEmbeddingModelList(embeddingModel);
        return getDataTable(list);
    }

    /**
     * 导出嵌入模型配置列表
     */
    @RequiresPermissions("project1:embeddingModel:export")
    @Log(title = "嵌入模型配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, EmbeddingModel embeddingModel)
    {
        List<EmbeddingModel> list = embeddingModelService.selectEmbeddingModelList(embeddingModel);
        ExcelUtil<EmbeddingModel> util = new ExcelUtil<EmbeddingModel>(EmbeddingModel.class);
        util.exportExcel(response, list, "嵌入模型配置数据");
    }

    /**
     * 获取嵌入模型配置详细信息
     */
    @RequiresPermissions("project1:embeddingModel:query")
    @GetMapping(value = "/{embeddingModelKey}")
    public AjaxResult getInfo(@PathVariable("embeddingModelKey") String embeddingModelKey)
    {
        return success(embeddingModelService.selectEmbeddingModelByEmbeddingModelKey(embeddingModelKey));
    }

    /**
     * 新增嵌入模型配置
     */
    @RequiresPermissions("project1:embeddingModel:add")
    @Log(title = "嵌入模型配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody EmbeddingModel embeddingModel)
    {
        return toAjax(embeddingModelService.insertEmbeddingModel(embeddingModel));
    }

    /**
     * 修改嵌入模型配置
     */
    @RequiresPermissions("project1:embeddingModel:edit")
    @Log(title = "嵌入模型配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody EmbeddingModel embeddingModel)
    {
        return toAjax(embeddingModelService.updateEmbeddingModel(embeddingModel));
    }

    /**
     * 删除嵌入模型配置
     */
    @RequiresPermissions("project1:embeddingModel:remove")
    @Log(title = "嵌入模型配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{embeddingModelKeys}")
    public AjaxResult remove(@PathVariable String[] embeddingModelKeys)
    {
        return toAjax(embeddingModelService.deleteEmbeddingModelByEmbeddingModelKeys(embeddingModelKeys));
    }
}
