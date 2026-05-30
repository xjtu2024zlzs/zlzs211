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
import com.ruoyi.topic5.domain.T5PartInstance;
import com.ruoyi.topic5.service.IT5PartInstanceService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 课题五-零部件实例Controller
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@RestController
@RequestMapping("/instance")
public class T5PartInstanceController extends BaseController
{
    @Autowired
    private IT5PartInstanceService t5PartInstanceService;

    /**
     * 查询课题五-零部件实例列表
     */
    @RequiresPermissions("topic5:instance:list")
    @GetMapping("/list")
    public TableDataInfo list(T5PartInstance t5PartInstance)
    {
        startPage();
        List<T5PartInstance> list = t5PartInstanceService.selectT5PartInstanceList(t5PartInstance);
        return getDataTable(list);
    }

    /**
     * 导出课题五-零部件实例列表
     */
    @RequiresPermissions("topic5:instance:export")
    @Log(title = "课题五-零部件实例", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, T5PartInstance t5PartInstance)
    {
        List<T5PartInstance> list = t5PartInstanceService.selectT5PartInstanceList(t5PartInstance);
        ExcelUtil<T5PartInstance> util = new ExcelUtil<T5PartInstance>(T5PartInstance.class);
        util.exportExcel(response, list, "课题五-零部件实例数据");
    }

    /**
     * 获取课题五-零部件实例详细信息
     */
    @RequiresPermissions("topic5:instance:query")
    @GetMapping(value = "/{partId}")
    public AjaxResult getInfo(@PathVariable("partId") Long partId)
    {
        return success(t5PartInstanceService.selectT5PartInstanceByPartId(partId));
    }

    /**
     * 新增课题五-零部件实例
     */
    @RequiresPermissions("topic5:instance:add")
    @Log(title = "课题五-零部件实例", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody T5PartInstance t5PartInstance)
    {
        return toAjax(t5PartInstanceService.insertT5PartInstance(t5PartInstance));
    }

    /**
     * 修改课题五-零部件实例
     */
    @RequiresPermissions("topic5:instance:edit")
    @Log(title = "课题五-零部件实例", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody T5PartInstance t5PartInstance)
    {
        return toAjax(t5PartInstanceService.updateT5PartInstance(t5PartInstance));
    }

    /**
     * 删除课题五-零部件实例
     */
    @RequiresPermissions("topic5:instance:remove")
    @Log(title = "课题五-零部件实例", businessType = BusinessType.DELETE)
	@DeleteMapping("/{partIds}")
    public AjaxResult remove(@PathVariable Long[] partIds)
    {
        return toAjax(t5PartInstanceService.deleteT5PartInstanceByPartIds(partIds));
    }
}
