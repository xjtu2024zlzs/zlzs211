package com.ruoyi.project4.controller;

import com.ruoyi.project4.domain.dto.FeatureFusionReqDTO;
import com.ruoyi.project4.domain.entity.T4BearingFeatureFusion;
import com.ruoyi.project4.service.BearingFeatureFusionService;
import com.ruoyi.common.core.domain.R;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.annotation.Resource;

@RestController
@RequestMapping("/bearing/fusion")
public class BearingFeatureFusionController {

    @Resource
    private BearingFeatureFusionService bearingFeatureFusionService;

    @PostMapping("/run")
    public R<T4BearingFeatureFusion> featureFusion(
            @RequestParam("preprocessId") Long preprocessId,
            @RequestBody FeatureFusionReqDTO reqDTO
    ) {
        T4BearingFeatureFusion result = bearingFeatureFusionService.execFusion(preprocessId, reqDTO);
        if ("fail".equals(result.getStatus())) {
            return R.fail("特征融合执行失败：" + result.getBizResult());
        }
        return R.ok(result, "特征融合执行成功");
    }
}