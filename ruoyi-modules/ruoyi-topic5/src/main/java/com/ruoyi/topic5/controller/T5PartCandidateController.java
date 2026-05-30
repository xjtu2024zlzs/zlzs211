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
import com.ruoyi.topic5.domain.T5PartCandidate;
import com.ruoyi.topic5.service.IT5PartCandidateService;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.page.TableDataInfo;

/**
 * 课题五-候选零部件定位结果Controller
 * 
 * @author ruoyi
 * @date 2026-05-30
 */
@RestController
@RequestMapping("/candidate")
public class T5PartCandidateController extends BaseController
{
    @Autowired
    private IT5PartCandidateService t5PartCandidateService;

    /**
     * 查询课题五-候选零部件定位结果列表
     */
    @RequiresPermissions("topic5:candidate:list")
    @GetMapping("/list")
    public TableDataInfo list(T5PartCandidate t5PartCandidate)
    {
        startPage();
        List<T5PartCandidate> list = t5PartCandidateService.selectT5PartCandidateList(t5PartCandidate);
        return getDataTable(list);
    }

    /**
     * 导出课题五-候选零部件定位结果列表
     */
    @RequiresPermissions("topic5:candidate:export")
    @Log(title = "课题五-候选零部件定位结果", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, T5PartCandidate t5PartCandidate)
    {
        List<T5PartCandidate> list = t5PartCandidateService.selectT5PartCandidateList(t5PartCandidate);
        ExcelUtil<T5PartCandidate> util = new ExcelUtil<T5PartCandidate>(T5PartCandidate.class);
        util.exportExcel(response, list, "课题五-候选零部件定位结果数据");
    }

    /**
     * 获取课题五-候选零部件定位结果详细信息
     */
    @RequiresPermissions("topic5:candidate:query")
    @GetMapping(value = "/{candidateId}")
    public AjaxResult getInfo(@PathVariable("candidateId") Long candidateId)
    {
        return success(t5PartCandidateService.selectT5PartCandidateByCandidateId(candidateId));
    }

    /**
     * 新增课题五-候选零部件定位结果
     */
    @RequiresPermissions("topic5:candidate:add")
    @Log(title = "课题五-候选零部件定位结果", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody T5PartCandidate t5PartCandidate)
    {
        return toAjax(t5PartCandidateService.insertT5PartCandidate(t5PartCandidate));
    }

    /**
     * 修改课题五-候选零部件定位结果
     */
    @RequiresPermissions("topic5:candidate:edit")
    @Log(title = "课题五-候选零部件定位结果", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody T5PartCandidate t5PartCandidate)
    {
        return toAjax(t5PartCandidateService.updateT5PartCandidate(t5PartCandidate));
    }

    /**
     * 删除课题五-候选零部件定位结果
     */
    @RequiresPermissions("topic5:candidate:remove")
    @Log(title = "课题五-候选零部件定位结果", businessType = BusinessType.DELETE)
	@DeleteMapping("/{candidateIds}")
    public AjaxResult remove(@PathVariable Long[] candidateIds)
    {
        return toAjax(t5PartCandidateService.deleteT5PartCandidateByCandidateIds(candidateIds));
    }
}
