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
import com.ruoyi.project1.domain.ApiPullDatasource;
import com.ruoyi.project1.service.IApiPullDatasourceService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * API 拉取数据源明细Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/apiPullDatasource")
public class ApiPullDatasourceController extends BaseController
{
    @Autowired
    private IApiPullDatasourceService apiPullDatasourceService;

    /**
     * 查询API 拉取数据源明细列表
     */
    @RequiresPermissions("project1:apiPullDatasource:list")
    @GetMapping("/list")
    public TableDataInfo list(ApiPullDatasource apiPullDatasource)
    {
        startPage();
        List<ApiPullDatasource> list = apiPullDatasourceService.selectApiPullDatasourceList(apiPullDatasource);
        return getDataTable(list);
    }

    /**
     * 导出API 拉取数据源明细列表
     */
    @RequiresPermissions("project1:apiPullDatasource:export")
    @Log(title = "API 拉取数据源明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ApiPullDatasource apiPullDatasource)
    {
        List<ApiPullDatasource> list = apiPullDatasourceService.selectApiPullDatasourceList(apiPullDatasource);
        ExcelUtil<ApiPullDatasource> util = new ExcelUtil<ApiPullDatasource>(ApiPullDatasource.class);
        util.exportExcel(response, list, "API 拉取数据源明细数据");
    }

    /**
     * 获取API 拉取数据源明细详细信息
     */
    @RequiresPermissions("project1:apiPullDatasource:query")
    @GetMapping(value = "/{datasourceId}")
    public AjaxResult getInfo(@PathVariable("datasourceId") Long datasourceId)
    {
        return success(apiPullDatasourceService.selectApiPullDatasourceByDatasourceId(datasourceId));
    }

    /**
     * 新增API 拉取数据源明细
     */
    @RequiresPermissions("project1:apiPullDatasource:add")
    @Log(title = "API 拉取数据源明细", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ApiPullDatasource apiPullDatasource)
    {
        return toAjax(apiPullDatasourceService.insertApiPullDatasource(apiPullDatasource));
    }

    /**
     * 修改API 拉取数据源明细
     */
    @RequiresPermissions("project1:apiPullDatasource:edit")
    @Log(title = "API 拉取数据源明细", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ApiPullDatasource apiPullDatasource)
    {
        return toAjax(apiPullDatasourceService.updateApiPullDatasource(apiPullDatasource));
    }

    /**
     * 删除API 拉取数据源明细
     */
    @RequiresPermissions("project1:apiPullDatasource:remove")
    @Log(title = "API 拉取数据源明细", businessType = BusinessType.DELETE)
	@DeleteMapping("/{datasourceIds}")
    public AjaxResult remove(@PathVariable Long[] datasourceIds)
    {
        return toAjax(apiPullDatasourceService.deleteApiPullDatasourceByDatasourceIds(datasourceIds));
    }
}
