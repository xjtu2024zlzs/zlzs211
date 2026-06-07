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
import com.ruoyi.project1.domain.ApiPushDatasource;
import com.ruoyi.project1.service.IApiPushDatasourceService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * API 推送数据源明细Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/apiPushDatasource")
public class ApiPushDatasourceController extends BaseController
{
    @Autowired
    private IApiPushDatasourceService apiPushDatasourceService;

    /**
     * 查询API 推送数据源明细列表
     */
    @RequiresPermissions("project1:apiPushDatasource:list")
    @GetMapping("/list")
    public TableDataInfo list(ApiPushDatasource apiPushDatasource)
    {
        startPage();
        List<ApiPushDatasource> list = apiPushDatasourceService.selectApiPushDatasourceList(apiPushDatasource);
        return getDataTable(list);
    }

    /**
     * 导出API 推送数据源明细列表
     */
    @RequiresPermissions("project1:apiPushDatasource:export")
    @Log(title = "API 推送数据源明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ApiPushDatasource apiPushDatasource)
    {
        List<ApiPushDatasource> list = apiPushDatasourceService.selectApiPushDatasourceList(apiPushDatasource);
        ExcelUtil<ApiPushDatasource> util = new ExcelUtil<ApiPushDatasource>(ApiPushDatasource.class);
        util.exportExcel(response, list, "API 推送数据源明细数据");
    }

    /**
     * 获取API 推送数据源明细详细信息
     */
    @RequiresPermissions("project1:apiPushDatasource:query")
    @GetMapping(value = "/{datasourceId}")
    public AjaxResult getInfo(@PathVariable("datasourceId") Long datasourceId)
    {
        return success(apiPushDatasourceService.selectApiPushDatasourceByDatasourceId(datasourceId));
    }

    /**
     * 新增API 推送数据源明细
     */
    @RequiresPermissions("project1:apiPushDatasource:add")
    @Log(title = "API 推送数据源明细", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ApiPushDatasource apiPushDatasource)
    {
        return toAjax(apiPushDatasourceService.insertApiPushDatasource(apiPushDatasource));
    }

    /**
     * 修改API 推送数据源明细
     */
    @RequiresPermissions("project1:apiPushDatasource:edit")
    @Log(title = "API 推送数据源明细", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ApiPushDatasource apiPushDatasource)
    {
        return toAjax(apiPushDatasourceService.updateApiPushDatasource(apiPushDatasource));
    }

    /**
     * 删除API 推送数据源明细
     */
    @RequiresPermissions("project1:apiPushDatasource:remove")
    @Log(title = "API 推送数据源明细", businessType = BusinessType.DELETE)
	@DeleteMapping("/{datasourceIds}")
    public AjaxResult remove(@PathVariable Long[] datasourceIds)
    {
        return toAjax(apiPushDatasourceService.deleteApiPushDatasourceByDatasourceIds(datasourceIds));
    }
}
