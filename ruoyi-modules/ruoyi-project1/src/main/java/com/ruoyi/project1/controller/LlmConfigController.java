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
import com.ruoyi.project1.domain.LlmConfig;
import com.ruoyi.project1.service.ILlmConfigService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * LLM 配置Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/llmConfig")
public class LlmConfigController extends BaseController
{
    @Autowired
    private ILlmConfigService llmConfigService;

    /**
     * 查询LLM 配置列表
     */
    @RequiresPermissions("project1:llmConfig:list")
    @GetMapping("/list")
    public TableDataInfo list(LlmConfig llmConfig)
    {
        startPage();
        List<LlmConfig> list = llmConfigService.selectLlmConfigList(llmConfig);
        return getDataTable(list);
    }

    /**
     * 导出LLM 配置列表
     */
    @RequiresPermissions("project1:llmConfig:export")
    @Log(title = "LLM 配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, LlmConfig llmConfig)
    {
        List<LlmConfig> list = llmConfigService.selectLlmConfigList(llmConfig);
        ExcelUtil<LlmConfig> util = new ExcelUtil<LlmConfig>(LlmConfig.class);
        util.exportExcel(response, list, "LLM 配置数据");
    }

    /**
     * 获取LLM 配置详细信息
     */
    @RequiresPermissions("project1:llmConfig:query")
    @GetMapping(value = "/{llmConfigId}")
    public AjaxResult getInfo(@PathVariable("llmConfigId") Long llmConfigId)
    {
        return success(llmConfigService.selectLlmConfigByLlmConfigId(llmConfigId));
    }

    /**
     * 新增LLM 配置
     */
    @RequiresPermissions("project1:llmConfig:add")
    @Log(title = "LLM 配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody LlmConfig llmConfig)
    {
        return toAjax(llmConfigService.insertLlmConfig(llmConfig));
    }

    /**
     * 修改LLM 配置
     */
    @RequiresPermissions("project1:llmConfig:edit")
    @Log(title = "LLM 配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody LlmConfig llmConfig)
    {
        return toAjax(llmConfigService.updateLlmConfig(llmConfig));
    }

    /**
     * 删除LLM 配置
     */
    @RequiresPermissions("project1:llmConfig:remove")
    @Log(title = "LLM 配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{llmConfigIds}")
    public AjaxResult remove(@PathVariable Long[] llmConfigIds)
    {
        return toAjax(llmConfigService.deleteLlmConfigByLlmConfigIds(llmConfigIds));
    }
}
