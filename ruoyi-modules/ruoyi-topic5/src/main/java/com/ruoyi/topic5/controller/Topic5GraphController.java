package com.ruoyi.topic5.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.topic5.domain.Topic5TraceProblem;
import com.ruoyi.topic5.domain.dto.SecondAlgorithmDTO;
import com.ruoyi.topic5.service.ITopic5TraceProblemService;
import com.ruoyi.topic5.domain.dto.SecondAlgorithmConfirmDTO;

/**
 * 课题五知识图谱导入与第二部分算法运行Controller
 */
@RestController
@RequestMapping("/graph")
public class Topic5GraphController extends BaseController
{
    @Autowired
    private ITopic5TraceProblemService traceProblemService;

    /**
     * 获取追溯任务下拉框
     */
    @GetMapping("/trace/options")
    public AjaxResult options(Topic5TraceProblem query)
    {
        return success(traceProblemService.selectTraceOptions());
    }

    /**
     * 获取追溯任务详情
     */
    @GetMapping("/trace/{id}")
    public AjaxResult getTraceInfo(@PathVariable Long id)
    {
        return success(traceProblemService.selectTopic5TraceProblemById(id));
    }

    /**
     * 从课题一拉取知识图谱
     */
    @PostMapping("/{id}/pull-topic1-kg")
    public AjaxResult pullTopic1Kg(@PathVariable Long id)
    {
        Map<String, Object> result = traceProblemService.pullTopic1Kg(id);
        return success(result);
    }

    /**
     * 运行第二部分算法
     */
    @PostMapping("/{id}/run-second-algorithm")
    public AjaxResult runSecondAlgorithm(@PathVariable Long id, @RequestBody SecondAlgorithmDTO dto)
    {
        Map<String, Object> result = traceProblemService.runSecondAlgorithm(id, dto.getAlgorithmName());
        return success(result);
    }

    /**
     * 获取当前追溯任务的知识图谱信息
     */
    @GetMapping("/{id}/kg")
    public AjaxResult getTraceKg(@PathVariable Long id)
    {
        return success(traceProblemService.getTraceKg(id));
    }

    /**
     * 人工判定第二部分算法结果
     */
    @PostMapping("/{id}/confirm-second-algorithm")
    public AjaxResult confirmSecondAlgorithm(@PathVariable Long id, @RequestBody SecondAlgorithmConfirmDTO dto)
    {
        traceProblemService.confirmSecondAlgorithmResult(id, dto.getSaveFlag(), dto.getRemark());
        return success("人工判定完成");
    }
}