package com.ruoyi.designtask1.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.designtask1.domain.DesignTemplate;
import com.ruoyi.designtask1.service.IDesignTemplateService;

@RestController
@RequestMapping({"template", "/designtask/template"})
public class DesignTemplateController extends BaseController {

    @Autowired
    private IDesignTemplateService templateService;

    /**
     * 获取模板列表
     */
    @RequiresPermissions("designtask:flow:list")
    @GetMapping("/list")
    public TableDataInfo list(DesignTemplate template) {
        startPage();
        List<DesignTemplate> list = templateService.selectTemplateList(template);
        return getDataTable(list);
    }

    /**
     * 获取所有模板（下拉框用）
     */
    @GetMapping("/optionselect")
    public AjaxResult optionselect() {
        List<DesignTemplate> list = templateService.selectTemplateAll();
        return success(list);
    }

    /**
     * 根据模板ID获取详细信息
     */
    @RequiresPermissions("designtask:flow:query")
    @GetMapping(value = "/{templateId}")
    public AjaxResult getInfo(@PathVariable Long templateId) {
        return success(templateService.selectTemplateById(templateId));
    }

    /**
     * 新增模板
     */
    @RequiresPermissions("designtask:flow:add")
    @Log(title = "流程模板", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DesignTemplate template) {
        template.setCreateBy(SecurityUtils.getUsername());
        return toAjax(templateService.insertTemplate(template));
    }

    /**
     * 修改模板
     */
    @RequiresPermissions("designtask:flow:edit")
    @Log(title = "流程模板", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DesignTemplate template) {
        template.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(templateService.updateTemplate(template));
    }

    /**
     * 删除模板
     */
    @RequiresPermissions("designtask:flow:remove")
    @Log(title = "流程模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/{templateIds}")
    public AjaxResult remove(@PathVariable Long[] templateIds) {
        return toAjax(templateService.deleteTemplateByIds(templateIds));
    }
}
