package com.ruoyi.project4.controller;

import com.ruoyi.project4.domain.dto.DiagnoseReqDTO;
import com.ruoyi.project4.domain.entity.T4BearingDiagnose;
import com.ruoyi.project4.service.BearingDiagnoseService;
import com.ruoyi.common.core.domain.R;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.annotation.Resource;

@RestController
@RequestMapping("/bearing/diagnose")
public class BearingDiagnoseController {

    @Resource
    private BearingDiagnoseService bearingDiagnoseService;

    @PostMapping("/run")
    public R<T4BearingDiagnose> diagnose(
            @RequestParam("rawDataId") Long rawDataId,
            @RequestBody DiagnoseReqDTO reqDTO
    ) {
        T4BearingDiagnose result = bearingDiagnoseService.execDiagnose(rawDataId, reqDTO);
        if ("fail".equals(result.getStatus())) {
            return R.fail("故障诊断执行失败：" + result.getBizResult());
        }
        return R.ok(result, "故障诊断执行成功");
    }
}