package com.ruoyi.project3.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.project3.service.FaultPredictService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

//故障预防
@RestController
@RequestMapping("/service/predict")
public class FaultPredictController {

    @Resource
    private FaultPredictService faultPredictService;

    @PostMapping("/tasks")
    public AjaxResult start_pre_task(@RequestBody(required = false) String request_body) {
        return AjaxResult.success(faultPredictService.start_pre_task(request_body));
    }
}
