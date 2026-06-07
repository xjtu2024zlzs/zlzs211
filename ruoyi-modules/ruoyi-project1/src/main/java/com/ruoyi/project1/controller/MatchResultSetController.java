package com.ruoyi.project1.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.project1.domain.MatchResultSet;
import com.ruoyi.project1.domain.MatchReviewRequest;
import com.ruoyi.project1.service.IMatchResultSetService;
import com.ruoyi.project1.service.IReviewedMatchService;

/**
 * Controller for mode matching result sets and review actions.
 */
@RestController
@RequestMapping("/matchResult")
public class MatchResultSetController extends BaseController
{
    @Autowired
    private IMatchResultSetService matchResultSetService;

    @Autowired
    private IReviewedMatchService reviewedMatchService;

    /**
     * Query result set list.
     */
    @RequiresPermissions("project1:matchResult:list")
    @GetMapping("/list")
    public TableDataInfo list(MatchResultSet matchResultSet)
    {
        startPage();
        List<MatchResultSet> list = matchResultSetService.selectMatchResultSetList(matchResultSet);
        return getDataTable(list);
    }

    /**
     * Export result set list.
     */
    @RequiresPermissions("project1:matchResult:export")
    @Log(title = "模式映射结果展示", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MatchResultSet matchResultSet)
    {
        List<MatchResultSet> list = matchResultSetService.selectMatchResultSetList(matchResultSet);
        ExcelUtil<MatchResultSet> util = new ExcelUtil<>(MatchResultSet.class);
        util.exportExcel(response, list, "模式映射结果展示数据");
    }

    /**
     * Query field-level match rows for one result set.
     */
    @RequiresPermissions("project1:matchResult:query")
    @GetMapping("/detail/{resultSetId}")
    public AjaxResult detail(@PathVariable("resultSetId") Long resultSetId,
            @RequestParam(value = "reviewStatus", required = false) String reviewStatus,
            @RequestParam(value = "reviewedBy", required = false) String reviewedBy)
    {
        return success(matchResultSetService.detail(resultSetId, reviewStatus, reviewedBy));
    }

    /**
     * Query metric visualization payload.
     */
    @RequiresPermissions("project1:matchResult:query")
    @GetMapping("/metrics/{resultSetId}")
    public AjaxResult metrics(@PathVariable("resultSetId") Long resultSetId)
    {
        return success(matchResultSetService.metrics(resultSetId));
    }

    /**
     * Approve field match rows.
     */
    @RequiresPermissions("project1:matchResult:edit")
    @Log(title = "模式映射结果审核通过", businessType = BusinessType.UPDATE)
    @PostMapping("/review/approve")
    public AjaxResult approve(@RequestBody MatchReviewRequest request)
    {
        return success(reviewedMatchService.approve(request));
    }

    /**
     * Reject field match rows.
     */
    @RequiresPermissions("project1:matchResult:edit")
    @Log(title = "模式映射结果驳回", businessType = BusinessType.UPDATE)
    @PostMapping("/review/reject")
    public AjaxResult reject(@RequestBody MatchReviewRequest request)
    {
        return success(reviewedMatchService.reject(request));
    }

    /**
     * Auto approve pending rows by threshold.
     */
    @RequiresPermissions("project1:matchResult:edit")
    @Log(title = "模式映射结果自动通过", businessType = BusinessType.UPDATE)
    @PostMapping("/review/auto/{resultSetId}")
    public AjaxResult autoApprove(@PathVariable("resultSetId") Long resultSetId)
    {
        return success(reviewedMatchService.autoApprove(resultSetId));
    }

    /**
     * Query review history for one field match row.
     */
    @RequiresPermissions("project1:matchResult:query")
    @GetMapping("/review/history/{reviewId}")
    public AjaxResult history(@PathVariable("reviewId") Long reviewId)
    {
        return success(reviewedMatchService.history(reviewId));
    }

    /**
     * Query one result set.
     */
    @RequiresPermissions("project1:matchResult:query")
    @GetMapping(value = "/{resultSetId}")
    public AjaxResult getInfo(@PathVariable("resultSetId") Long resultSetId)
    {
        return success(matchResultSetService.selectMatchResultSetByResultSetId(resultSetId));
    }

    /**
     * Add result set. Kept for generated CRUD compatibility.
     */
    @RequiresPermissions("project1:matchResult:add")
    @Log(title = "模式映射结果展示", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MatchResultSet matchResultSet)
    {
        return toAjax(matchResultSetService.insertMatchResultSet(matchResultSet));
    }

    /**
     * Update result set. Kept for generated CRUD compatibility.
     */
    @RequiresPermissions("project1:matchResult:edit")
    @Log(title = "模式映射结果展示", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MatchResultSet matchResultSet)
    {
        return toAjax(matchResultSetService.updateMatchResultSet(matchResultSet));
    }

    /**
     * Mark one result set as default and rebuild final access rules.
     */
    @RequiresPermissions("project1:matchResult:edit")
    @Log(title = "设置默认结果集", businessType = BusinessType.UPDATE)
    @PostMapping("/default/{resultSetId}")
    public AjaxResult setDefault(@PathVariable("resultSetId") Long resultSetId)
    {
        return success(matchResultSetService.setDefault(resultSetId));
    }

    /**
     * Delete result sets. Kept for generated CRUD compatibility.
     */
    @RequiresPermissions("project1:matchResult:remove")
    @Log(title = "模式映射结果展示", businessType = BusinessType.DELETE)
    @DeleteMapping("/{resultSetIds}")
    public AjaxResult remove(@PathVariable Long[] resultSetIds)
    {
        return toAjax(matchResultSetService.deleteMatchResultSetByResultSetIds(resultSetIds));
    }
}
