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
import com.ruoyi.project1.domain.Datasource;
import com.ruoyi.project1.service.IDatasourceService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 数据源管理Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/datasource")
public class DatasourceController extends BaseController
{
    @Autowired
    private IDatasourceService datasourceService;

    /**
     * 查询数据源管理列表
     */
    @RequiresPermissions("project1:datasource:list")
    @GetMapping("/list")
    public TableDataInfo list(Datasource datasource)
    {
        startPage();
        List<Datasource> list = datasourceService.selectDatasourceList(datasource);
        return getDataTable(list);
    }

    /**
     * 导出数据源管理列表
     */
    @RequiresPermissions("project1:datasource:export")
    @Log(title = "数据源管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Datasource datasource)
    {
        List<Datasource> list = datasourceService.selectDatasourceList(datasource);
        ExcelUtil<Datasource> util = new ExcelUtil<Datasource>(Datasource.class);
        util.exportExcel(response, list, "数据源管理数据");
    }

    /**
     * 获取数据源管理详细信息
     */
    @RequiresPermissions("project1:datasource:query")
    @GetMapping(value = "/{datasourceId}")
    public AjaxResult getInfo(@PathVariable("datasourceId") Long datasourceId)
    {
        return success(datasourceService.selectDatasourceByDatasourceId(datasourceId));
    }

    /**
     * 新增数据源管理
     */
    @RequiresPermissions("project1:datasource:add")
    @Log(title = "数据源管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Datasource datasource)
    {
        return toAjax(datasourceService.insertDatasource(datasource));
    }

    /**
     * 修改数据源管理
     */
    @RequiresPermissions("project1:datasource:edit")
    @Log(title = "数据源管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Datasource datasource)
    {
        return toAjax(datasourceService.updateDatasource(datasource));
    }

    /**
     * 测试数据源连接
     */
    @RequiresPermissions("project1:datasource:edit")
    @Log(title = "数据源连接测试", businessType = BusinessType.UPDATE)
    @PostMapping("/test/{datasourceId}")
    public AjaxResult testConnection(@PathVariable("datasourceId") Long datasourceId)
    {
        return success(datasourceService.testConnection(datasourceId));
    }

    /**
     * 读取数据源模式
     */
    @RequiresPermissions("project1:datasource:edit")
    @Log(title = "数据源模式读取", businessType = BusinessType.UPDATE)
    @PostMapping("/schema/read/{datasourceId}")
    public AjaxResult readSchema(@PathVariable("datasourceId") Long datasourceId)
    {
        return success(datasourceService.readSchema(datasourceId));
    }

    /**
     * 查看数据源模式
     */
    @RequiresPermissions("project1:datasource:query")
    @GetMapping("/schema/{datasourceId}")
    public AjaxResult schema(@PathVariable("datasourceId") Long datasourceId)
    {
        return success(datasourceService.getSchema(datasourceId));
    }

    /**
     * 删除数据源管理
     */
    @RequiresPermissions("project1:datasource:remove")
    @Log(title = "数据源管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{datasourceIds}")
    public AjaxResult remove(@PathVariable Long[] datasourceIds)
    {
        return toAjax(datasourceService.deleteDatasourceByDatasourceIds(datasourceIds));
    }
}
