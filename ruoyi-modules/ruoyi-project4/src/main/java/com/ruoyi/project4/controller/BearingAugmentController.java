package com.ruoyi.project4.controller;

import com.ruoyi.project4.domain.dto.AugmentReqDTO;
import com.ruoyi.project4.domain.entity.T4BearingAugment;
import com.ruoyi.project4.service.BearingAugmentService;
import com.ruoyi.common.core.domain.R;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.annotation.Resource;

@RestController
@RequestMapping("/bearing/augment")
public class BearingAugmentController {

    @Resource
    private BearingAugmentService bearingAugmentService;

    @PostMapping("/run")
    public R<T4BearingAugment> augment(
            @RequestParam("preprocessId") Long preprocessId,
            @RequestBody AugmentReqDTO reqDTO
    ) {
        T4BearingAugment result = bearingAugmentService.execAugment(preprocessId, reqDTO);
        if ("fail".equals(result.getStatus())) {
            return R.fail("数据增强执行失败：" + result.getBizResult());
        }
        return R.ok(result, "数据增强执行成功");
    }
}