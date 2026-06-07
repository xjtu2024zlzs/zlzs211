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
import com.ruoyi.project1.domain.ReviewHistory;
import com.ruoyi.project1.service.IReviewHistoryService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 字段匹配审核历史Controller
 * 
 * @author pwb
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/reviewHistory")
public class ReviewHistoryController extends BaseController
{
    @Autowired
    private IReviewHistoryService reviewHistoryService;

    /**
     * 查询字段匹配审核历史列表
     */
    @RequiresPermissions("project1:reviewHistory:list")
    @GetMapping("/list")
    public TableDataInfo list(ReviewHistory reviewHistory)
    {
        startPage();
        List<ReviewHistory> list = reviewHistoryService.selectReviewHistoryList(reviewHistory);
        return getDataTable(list);
    }

    /**
     * 导出字段匹配审核历史列表
     */
    @RequiresPermissions("project1:reviewHistory:export")
    @Log(title = "字段匹配审核历史", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ReviewHistory reviewHistory)
    {
        List<ReviewHistory> list = reviewHistoryService.selectReviewHistoryList(reviewHistory);
        ExcelUtil<ReviewHistory> util = new ExcelUtil<ReviewHistory>(ReviewHistory.class);
        util.exportExcel(response, list, "字段匹配审核历史数据");
    }

    /**
     * 获取字段匹配审核历史详细信息
     */
    @RequiresPermissions("project1:reviewHistory:query")
    @GetMapping(value = "/{historyId}")
    public AjaxResult getInfo(@PathVariable("historyId") Long historyId)
    {
        return success(reviewHistoryService.selectReviewHistoryByHistoryId(historyId));
    }

    /**
     * 新增字段匹配审核历史
     */
    @RequiresPermissions("project1:reviewHistory:add")
    @Log(title = "字段匹配审核历史", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ReviewHistory reviewHistory)
    {
        return toAjax(reviewHistoryService.insertReviewHistory(reviewHistory));
    }

    /**
     * 修改字段匹配审核历史
     */
    @RequiresPermissions("project1:reviewHistory:edit")
    @Log(title = "字段匹配审核历史", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ReviewHistory reviewHistory)
    {
        return toAjax(reviewHistoryService.updateReviewHistory(reviewHistory));
    }

    /**
     * 删除字段匹配审核历史
     */
    @RequiresPermissions("project1:reviewHistory:remove")
    @Log(title = "字段匹配审核历史", businessType = BusinessType.DELETE)
	@DeleteMapping("/{historyIds}")
    public AjaxResult remove(@PathVariable Long[] historyIds)
    {
        return toAjax(reviewHistoryService.deleteReviewHistoryByHistoryIds(historyIds));
    }
}
