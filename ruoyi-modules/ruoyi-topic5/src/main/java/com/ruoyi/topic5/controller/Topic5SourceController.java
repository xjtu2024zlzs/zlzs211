package com.ruoyi.topic5.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.topic5.domain.Topic5TraceProblem;
import com.ruoyi.topic5.domain.dto.SourceAlgorithmDTO;
import com.ruoyi.topic5.service.ITopic5TraceProblemService;

@RestController
@RequestMapping("/source")
public class Topic5SourceController extends BaseController
{
    @Autowired
    private ITopic5TraceProblemService traceProblemService;

    @GetMapping("/trace/options")
    public AjaxResult options(Topic5TraceProblem query)
    {
        return success(traceProblemService.selectTraceOptions());
    }

    @GetMapping("/trace/{id}")
    public AjaxResult getTraceInfo(@PathVariable Long id)
    {
        return success(traceProblemService.selectTopic5TraceProblemById(id));
    }

    @GetMapping("/{id}/result")
    public AjaxResult getSourceResult(@PathVariable Long id)
    {
        return success(traceProblemService.getSourceResult(id));
    }

    @PostMapping("/{id}/run-source-algorithm")
    public AjaxResult runSourceAlgorithm(@PathVariable Long id, @RequestBody SourceAlgorithmDTO dto)
    {
        Map<String, Object> result = traceProblemService.runSourceAlgorithm(id, dto.getAlgorithmName());
        return success(result);
    }

    @PostMapping("/{id}/export-report")
    public AjaxResult exportSourceReport(@PathVariable Long id)
    {
        Map<String, Object> result = traceProblemService.exportSourceReport(id);
        return success(result);
    }

    @PostMapping("/{id}/push-topic2")
    public AjaxResult pushTopic2(@PathVariable Long id)
    {
        traceProblemService.pushTopic2(id);
        return success("已推送课题二");
    }

    @PostMapping("/{id}/push-topic3")
    public AjaxResult pushTopic3(@PathVariable Long id)
    {
        traceProblemService.pushTopic3(id);
        return success("已推送课题三");
    }
}