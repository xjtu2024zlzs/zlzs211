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
import com.ruoyi.project1.domain.MatchResultRow;
import com.ruoyi.project1.service.IMatchResultRowService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 模式映射结果明细Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/matchResultRow")
public class MatchResultRowController extends BaseController
{
    @Autowired
    private IMatchResultRowService matchResultRowService;

    /**
     * 查询模式映射结果明细列表
     */
    @RequiresPermissions("project1:matchResultRow:list")
    @GetMapping("/list")
    public TableDataInfo list(MatchResultRow matchResultRow)
    {
        startPage();
        List<MatchResultRow> list = matchResultRowService.selectMatchResultRowList(matchResultRow);
        return getDataTable(list);
    }

    /**
     * 导出模式映射结果明细列表
     */
    @RequiresPermissions("project1:matchResultRow:export")
    @Log(title = "模式映射结果明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MatchResultRow matchResultRow)
    {
        List<MatchResultRow> list = matchResultRowService.selectMatchResultRowList(matchResultRow);
        ExcelUtil<MatchResultRow> util = new ExcelUtil<MatchResultRow>(MatchResultRow.class);
        util.exportExcel(response, list, "模式映射结果明细数据");
    }

    /**
     * 获取模式映射结果明细详细信息
     */
    @RequiresPermissions("project1:matchResultRow:query")
    @GetMapping(value = "/{resultRowId}")
    public AjaxResult getInfo(@PathVariable("resultRowId") Long resultRowId)
    {
        return success(matchResultRowService.selectMatchResultRowByResultRowId(resultRowId));
    }

    /**
     * 新增模式映射结果明细
     */
    @RequiresPermissions("project1:matchResultRow:add")
    @Log(title = "模式映射结果明细", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MatchResultRow matchResultRow)
    {
        return toAjax(matchResultRowService.insertMatchResultRow(matchResultRow));
    }

    /**
     * 修改模式映射结果明细
     */
    @RequiresPermissions("project1:matchResultRow:edit")
    @Log(title = "模式映射结果明细", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MatchResultRow matchResultRow)
    {
        return toAjax(matchResultRowService.updateMatchResultRow(matchResultRow));
    }

    /**
     * 删除模式映射结果明细
     */
    @RequiresPermissions("project1:matchResultRow:remove")
    @Log(title = "模式映射结果明细", businessType = BusinessType.DELETE)
	@DeleteMapping("/{resultRowIds}")
    public AjaxResult remove(@PathVariable Long[] resultRowIds)
    {
        return toAjax(matchResultRowService.deleteMatchResultRowByResultRowIds(resultRowIds));
    }
}
