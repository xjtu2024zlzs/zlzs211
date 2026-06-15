package com.ruoyi.project4.controller;
import com.ruoyi.common.security.utils.SecurityUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.project4.domain.FdRootCauseAnalysis;
import com.ruoyi.project4.service.IFdRootCauseAnalysisService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 故障诊断-根因分析结果Controller
 *
 * 本Controller包括两类接口：
 * 1. 后台管理接口：用于页面增删改查。
 * 2. 输出接口：用于体现“课题四 → 课题五”的根因分析结果输出。
 *
 * @author ruoyi
 * @date 2026-05-29
 */
@RestController
@RequestMapping("/resultofrc")
public class FdRootCauseAnalysisController extends BaseController
{
    @Autowired
    private IFdRootCauseAnalysisService fdRootCauseAnalysisService;

    /**
     * 查询故障诊断-根因分析结果列表
     */
    @RequiresPermissions("project4:resultofrc:list")
    @GetMapping("/list")
    public TableDataInfo list(FdRootCauseAnalysis fdRootCauseAnalysis)
    {
        startPage();
        List<FdRootCauseAnalysis> list = fdRootCauseAnalysisService.selectFdRootCauseAnalysisList(fdRootCauseAnalysis);
        return getDataTable(list);
    }

    /**
     * 查询根因分析置信度统计
     */
    @RequiresPermissions("project4:resultofrc:list")
    @GetMapping("/confidenceStats")
    public AjaxResult confidenceStats(FdRootCauseAnalysis fdRootCauseAnalysis)
    {
        return success(fdRootCauseAnalysisService.selectConfidenceStats(fdRootCauseAnalysis));
    }

    /**
     * 导出故障诊断-根因分析结果列表
     */
    @RequiresPermissions("project4:resultofrc:export")
    @Log(title = "故障诊断-根因分析结果", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FdRootCauseAnalysis fdRootCauseAnalysis)
    {
        List<FdRootCauseAnalysis> list = fdRootCauseAnalysisService.selectFdRootCauseAnalysisList(fdRootCauseAnalysis);
        ExcelUtil<FdRootCauseAnalysis> util = new ExcelUtil<FdRootCauseAnalysis>(FdRootCauseAnalysis.class);
        util.exportExcel(response, list, "根因分析结果数据");
    }

    /**
     * 获取故障诊断-根因分析结果详细信息
     */
    @RequiresPermissions("project4:resultofrc:query")
    @GetMapping(value = "/{analysisId}")
    public AjaxResult getInfo(@PathVariable("analysisId") Long analysisId)
    {
        return success(fdRootCauseAnalysisService.selectFdRootCauseAnalysisByAnalysisId(analysisId));
    }

    /**
     * 新增故障诊断-根因分析结果
     */
    @RequiresPermissions("project4:resultofrc:add")
    @Log(title = "故障诊断-根因分析结果", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FdRootCauseAnalysis fdRootCauseAnalysis)
    {
        fdRootCauseAnalysis.setCreateBy(SecurityUtils.getUsername());
        return toAjax(fdRootCauseAnalysisService.insertFdRootCauseAnalysis(fdRootCauseAnalysis));
    }

    /**
     * 修改故障诊断-根因分析结果
     */
    @RequiresPermissions("project4:resultofrc:edit")
    @Log(title = "故障诊断-根因分析结果", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FdRootCauseAnalysis fdRootCauseAnalysis)
    {
        fdRootCauseAnalysis.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(fdRootCauseAnalysisService.updateFdRootCauseAnalysis(fdRootCauseAnalysis));
    }

    /**
     * 删除故障诊断-根因分析结果
     */
    @RequiresPermissions("project4:resultofrc:remove")
    @Log(title = "故障诊断-根因分析结果", businessType = BusinessType.DELETE)
    @DeleteMapping("/{analysisIds}")
    public AjaxResult remove(@PathVariable Long[] analysisIds)
    {
        return toAjax(fdRootCauseAnalysisService.deleteFdRootCauseAnalysisByAnalysisIds(analysisIds));
    }

    /**
     * 根因分析结果输出接口
     *
     * 课题四向课题五输出：
     * 1. 根因判断
     * 2. 证据链
     * 3. 根因分析置信度
     *
     * 该接口用于体现申报书/接口表格中的“根因分析结果输出接口”。
     */
    @RequiresPermissions("project4:resultofrc:list")
    @GetMapping("/output/list")
    public AjaxResult outputList(FdRootCauseAnalysis fdRootCauseAnalysis)
    {
        List<FdRootCauseAnalysis> list = fdRootCauseAnalysisService.selectFdRootCauseAnalysisList(fdRootCauseAnalysis);

        List<Map<String, Object>> outputList = new ArrayList<Map<String, Object>>();

        for (FdRootCauseAnalysis item : list)
        {
            outputList.add(buildOutputMap(item));
        }

        return success(outputList);
    }

    /**
     * 根因分析结果单条输出接口
     *
     * 用于课题五根据根因分析ID获取单条根因分析结果。
     */
    @RequiresPermissions("project4:resultofrc:query")
    @GetMapping("/output/{analysisId}")
    public AjaxResult outputInfo(@PathVariable("analysisId") Long analysisId)
    {
        FdRootCauseAnalysis item = fdRootCauseAnalysisService.selectFdRootCauseAnalysisByAnalysisId(analysisId);

        if (item == null)
        {
            return error("未找到对应的根因分析结果");
        }

        return success(buildOutputMap(item));
    }

    /**
     * 构建课题四向课题五输出的数据结构。
     *
     * 输出口径与接口表格保持一致：
     * - 根因判断：rootCauseJudgment
     * - 证据链：evidenceChain
     * - 根因分析置信度：rootCauseConfidence
     */
    private Map<String, Object> buildOutputMap(FdRootCauseAnalysis item)
    {
        Map<String, Object> map = new LinkedHashMap<String, Object>();

        map.put("interfaceType", "根因分析结果输出接口");
        map.put("sourceSubject", "课题四");
        map.put("targetSubject", "课题五");

        map.put("analysisId", item.getAnalysisId());
        map.put("analysisCode", item.getAnalysisCode());
        map.put("diagnosisId", item.getDiagnosisId());
        map.put("sampleId", item.getSampleId());

        map.put("rootCauseJudgment", buildRootCauseJudgment(item));
        map.put("rootCauseType", item.getRootCauseType());
        map.put("rootCauseDesc", item.getRootCauseDesc());

        map.put("evidenceChain", item.getEvidenceJson());
        map.put("rootCauseConfidence", item.getProbability());

        map.put("analysisMethod", item.getAnalysisMethod());
        map.put("analysisStatus", item.getAnalysisStatus());
        map.put("analyst", item.getAnalyst());
        map.put("analysisTime", item.getAnalysisTime());

        map.put("maintenanceSuggestion", item.getMaintenanceSuggestion());
        map.put("remark", item.getRemark());

        return map;
    }

    /**
     * 生成根因判断文本。
     */
    private String buildRootCauseJudgment(FdRootCauseAnalysis item)
    {
        String type = item.getRootCauseType();
        String desc = item.getRootCauseDesc();

        if (type == null || type.trim().length() == 0)
        {
            type = "未定义根因类型";
        }

        if (desc == null || desc.trim().length() == 0)
        {
            desc = "未填写根因描述";
        }

        return type + "：" + desc;
    }
}