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
import com.ruoyi.topic5.domain.T5AlgorithmConfig;
import com.ruoyi.topic5.service.IT5AlgorithmConfigService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 零件定位结果Controller
 * 
 * @author ruoyi
 * @date 2026-05-31
 */
@RestController
@RequestMapping("/algorithmConfig")
public class T5AlgorithmConfigController extends BaseController
{
    @Autowired
    private IT5AlgorithmConfigService t5AlgorithmConfigService;

    /**
     * 查询零件定位结果列表
     */
    @RequiresPermissions("topic5:partLocateResult:list")
    @GetMapping("/list")
    public TableDataInfo list(T5AlgorithmConfig t5AlgorithmConfig)
    {
        startPage();
        List<T5AlgorithmConfig> list = t5AlgorithmConfigService.selectT5AlgorithmConfigList(t5AlgorithmConfig);
        return getDataTable(list);
    }

    /**
     * 导出零件定位结果列表
     */
    @RequiresPermissions("topic5:partLocateResult:export")
    @Log(title = "零件定位结果", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, T5AlgorithmConfig t5AlgorithmConfig)
    {
        List<T5AlgorithmConfig> list = t5AlgorithmConfigService.selectT5AlgorithmConfigList(t5AlgorithmConfig);
        ExcelUtil<T5AlgorithmConfig> util = new ExcelUtil<T5AlgorithmConfig>(T5AlgorithmConfig.class);
        util.exportExcel(response, list, "零件定位结果数据");
    }

    /**
     * 获取零件定位结果详细信息
     */
    @RequiresPermissions("topic5:partLocateResult:query")
    @GetMapping(value = "/{algorithmId}")
    public AjaxResult getInfo(@PathVariable("algorithmId") Long algorithmId)
    {
        return success(t5AlgorithmConfigService.selectT5AlgorithmConfigByAlgorithmId(algorithmId));
    }

    /**
     * 新增零件定位结果
     */
    @RequiresPermissions("topic5:partLocateResult:add")
    @Log(title = "零件定位结果", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody T5AlgorithmConfig t5AlgorithmConfig)
    {
        return toAjax(t5AlgorithmConfigService.insertT5AlgorithmConfig(t5AlgorithmConfig));
    }

    /**
     * 修改零件定位结果
     */
    @RequiresPermissions("topic5:partLocateResult:edit")
    @Log(title = "零件定位结果", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody T5AlgorithmConfig t5AlgorithmConfig)
    {
        return toAjax(t5AlgorithmConfigService.updateT5AlgorithmConfig(t5AlgorithmConfig));
    }

    /**
     * 删除零件定位结果
     */
    @RequiresPermissions("topic5:partLocateResult:remove")
    @Log(title = "零件定位结果", businessType = BusinessType.DELETE)
	@DeleteMapping("/{algorithmIds}")
    public AjaxResult remove(@PathVariable Long[] algorithmIds)
    {
        return toAjax(t5AlgorithmConfigService.deleteT5AlgorithmConfigByAlgorithmIds(algorithmIds));
    }
}
