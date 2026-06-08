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
import com.ruoyi.project1.domain.DbDatasource;
import com.ruoyi.project1.service.IDbDatasourceService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 数据库直连数据源明细Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/dbDatasource")
public class DbDatasourceController extends BaseController
{
    @Autowired
    private IDbDatasourceService dbDatasourceService;

    /**
     * 查询数据库直连数据源明细列表
     */
    @RequiresPermissions("project1:dbDatasource:list")
    @GetMapping("/list")
    public TableDataInfo list(DbDatasource dbDatasource)
    {
        startPage();
        List<DbDatasource> list = dbDatasourceService.selectDbDatasourceList(dbDatasource);
        return getDataTable(list);
    }

    /**
     * 导出数据库直连数据源明细列表
     */
    @RequiresPermissions("project1:dbDatasource:export")
    @Log(title = "数据库直连数据源明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DbDatasource dbDatasource)
    {
        List<DbDatasource> list = dbDatasourceService.selectDbDatasourceList(dbDatasource);
        ExcelUtil<DbDatasource> util = new ExcelUtil<DbDatasource>(DbDatasource.class);
        util.exportExcel(response, list, "数据库直连数据源明细数据");
    }

    /**
     * 获取数据库直连数据源明细详细信息
     */
    @RequiresPermissions("project1:dbDatasource:query")
    @GetMapping(value = "/{datasourceId}")
    public AjaxResult getInfo(@PathVariable("datasourceId") Long datasourceId)
    {
        return success(dbDatasourceService.selectDbDatasourceByDatasourceId(datasourceId));
    }

    /**
     * 新增数据库直连数据源明细
     */
    @RequiresPermissions("project1:dbDatasource:add")
    @Log(title = "数据库直连数据源明细", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DbDatasource dbDatasource)
    {
        return toAjax(dbDatasourceService.insertDbDatasource(dbDatasource));
    }

    /**
     * 修改数据库直连数据源明细
     */
    @RequiresPermissions("project1:dbDatasource:edit")
    @Log(title = "数据库直连数据源明细", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DbDatasource dbDatasource)
    {
        return toAjax(dbDatasourceService.updateDbDatasource(dbDatasource));
    }

    /**
     * 删除数据库直连数据源明细
     */
    @RequiresPermissions("project1:dbDatasource:remove")
    @Log(title = "数据库直连数据源明细", businessType = BusinessType.DELETE)
	@DeleteMapping("/{datasourceIds}")
    public AjaxResult remove(@PathVariable Long[] datasourceIds)
    {
        return toAjax(dbDatasourceService.deleteDbDatasourceByDatasourceIds(datasourceIds));
    }
}
