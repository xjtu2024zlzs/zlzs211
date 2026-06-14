package com.ruoyi.designtask1.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.designtask1.service.FrameBeamCrackService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"", "/designtask"})
public class FrameBeamCrackController {

    private final FrameBeamCrackService frameBeamCrackService;

    public FrameBeamCrackController(FrameBeamCrackService frameBeamCrackService) {
        this.frameBeamCrackService = frameBeamCrackService;
    }

    @GetMapping("/task/{taskId}/frame-beam-crack")
    public AjaxResult crackInput(@PathVariable Long taskId) {
        return AjaxResult.success(frameBeamCrackService.crackInput(taskId));
    }

    @PostMapping("/task/{taskId}/frame-beam-crack")
    public AjaxResult saveCrackInput(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(frameBeamCrackService.saveCrackInput(taskId, body));
    }

    @PostMapping("/task/{taskId}/frame-beam-load-spectrum")
    public AjaxResult saveLoadSpectrum(@PathVariable Long taskId, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(frameBeamCrackService.saveLoadSpectrum(taskId, body));
    }

    @GetMapping("/task/{taskId}/frame-beam-life-prediction")
    public AjaxResult lifePrediction(@PathVariable Long taskId) {
        return AjaxResult.success(frameBeamCrackService.lifePrediction(taskId));
    }

    @PostMapping("/task/{taskId}/frame-beam-life-prediction")
    public AjaxResult runLifePrediction(@PathVariable Long taskId) {
        return AjaxResult.success(frameBeamCrackService.runLifePrediction(taskId));
    }

    @PostMapping("/task/{taskId}/frame-beam-maintenance-advice/confirm")
    public AjaxResult confirmAdvice(@PathVariable Long taskId, @RequestBody(required = false) Map<String, Object> body) {
        return AjaxResult.success(frameBeamCrackService.confirmAdvice(taskId, body));
    }
}
