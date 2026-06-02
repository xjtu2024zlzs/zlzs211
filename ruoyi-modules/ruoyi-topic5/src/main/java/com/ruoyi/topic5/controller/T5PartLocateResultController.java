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
import com.ruoyi.topic5.domain.T5PartLocateResult;
import com.ruoyi.topic5.service.IT5PartLocateResultService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 算法配置Controller
 * 
 * @author ruoyi
 * @date 2026-05-31
 */
@RestController
@RequestMapping("/partLocateResult")
public class T5PartLocateResultController extends BaseController
{
    @Autowired
    private IT5PartLocateResultService t5PartLocateResultService;

    /**
     * 查询算法配置列表
     */
    @RequiresPermissions("topic5:algorithmConfig:list")
    @GetMapping("/list")
    public TableDataInfo list(T5PartLocateResult t5PartLocateResult)
    {
        startPage();
        List<T5PartLocateResult> list = t5PartLocateResultService.selectT5PartLocateResultList(t5PartLocateResult);
        return getDataTable(list);
    }

    /**
     * 导出算法配置列表
     */
    @RequiresPermissions("topic5:algorithmConfig:export")
    @Log(title = "算法配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, T5PartLocateResult t5PartLocateResult)
    {
        List<T5PartLocateResult> list = t5PartLocateResultService.selectT5PartLocateResultList(t5PartLocateResult);
        ExcelUtil<T5PartLocateResult> util = new ExcelUtil<T5PartLocateResult>(T5PartLocateResult.class);
        util.exportExcel(response, list, "算法配置数据");
    }

    /**
     * 获取算法配置详细信息
     */
    @RequiresPermissions("topic5:algorithmConfig:query")
    @GetMapping(value = "/{locateId}")
    public AjaxResult getInfo(@PathVariable("locateId") Long locateId)
    {
        return success(t5PartLocateResultService.selectT5PartLocateResultByLocateId(locateId));
    }

    /**
     * 新增算法配置
     */
    @RequiresPermissions("topic5:algorithmConfig:add")
    @Log(title = "算法配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody T5PartLocateResult t5PartLocateResult)
    {
        return toAjax(t5PartLocateResultService.insertT5PartLocateResult(t5PartLocateResult));
    }

    /**
     * 修改算法配置
     */
    @RequiresPermissions("topic5:algorithmConfig:edit")
    @Log(title = "算法配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody T5PartLocateResult t5PartLocateResult)
    {
        return toAjax(t5PartLocateResultService.updateT5PartLocateResult(t5PartLocateResult));
    }

    /**
     * 删除算法配置
     */
    @RequiresPermissions("topic5:algorithmConfig:remove")
    @Log(title = "算法配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{locateIds}")
    public AjaxResult remove(@PathVariable Long[] locateIds)
    {
        return toAjax(t5PartLocateResultService.deleteT5PartLocateResultByLocateIds(locateIds));
    }
}
