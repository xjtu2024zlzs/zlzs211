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
import com.ruoyi.project1.domain.ReviewedMatch;
import com.ruoyi.project1.service.IReviewedMatchService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 审核后的字段匹配结果Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/reviewedMatch")
public class ReviewedMatchController extends BaseController
{
    @Autowired
    private IReviewedMatchService reviewedMatchService;

    /**
     * 查询审核后的字段匹配结果列表
     */
    @RequiresPermissions("project1:reviewedMatch:list")
    @GetMapping("/list")
    public TableDataInfo list(ReviewedMatch reviewedMatch)
    {
        startPage();
        List<ReviewedMatch> list = reviewedMatchService.selectReviewedMatchList(reviewedMatch);
        return getDataTable(list);
    }

    /**
     * 导出审核后的字段匹配结果列表
     */
    @RequiresPermissions("project1:reviewedMatch:export")
    @Log(title = "审核后的字段匹配结果", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ReviewedMatch reviewedMatch)
    {
        List<ReviewedMatch> list = reviewedMatchService.selectReviewedMatchList(reviewedMatch);
        ExcelUtil<ReviewedMatch> util = new ExcelUtil<ReviewedMatch>(ReviewedMatch.class);
        util.exportExcel(response, list, "审核后的字段匹配结果数据");
    }

    /**
     * 获取审核后的字段匹配结果详细信息
     */
    @RequiresPermissions("project1:reviewedMatch:query")
    @GetMapping(value = "/{reviewId}")
    public AjaxResult getInfo(@PathVariable("reviewId") Long reviewId)
    {
        return success(reviewedMatchService.selectReviewedMatchByReviewId(reviewId));
    }

    /**
     * 新增审核后的字段匹配结果
     */
    @RequiresPermissions("project1:reviewedMatch:add")
    @Log(title = "审核后的字段匹配结果", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReviewedMatch reviewedMatch)
    {
        return toAjax(reviewedMatchService.insertReviewedMatch(reviewedMatch));
    }

    /**
     * 修改审核后的字段匹配结果
     */
    @RequiresPermissions("project1:reviewedMatch:edit")
    @Log(title = "审核后的字段匹配结果", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ReviewedMatch reviewedMatch)
    {
        return toAjax(reviewedMatchService.updateReviewedMatch(reviewedMatch));
    }

    /**
     * 删除审核后的字段匹配结果
     */
    @RequiresPermissions("project1:reviewedMatch:remove")
    @Log(title = "审核后的字段匹配结果", businessType = BusinessType.DELETE)
	@DeleteMapping("/{reviewIds}")
    public AjaxResult remove(@PathVariable Long[] reviewIds)
    {
        return toAjax(reviewedMatchService.deleteReviewedMatchByReviewIds(reviewIds));
    }
}
