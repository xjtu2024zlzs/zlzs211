package com.ruoyi.project3.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.project3.service.HomeService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//首页
@RestController
public class HomeController {

    @Resource
    private HomeService homeService;

    @GetMapping("/home/overview")
    public AjaxResult overview() {
        return AjaxResult.success(homeService.get_home_ovw());
    }
}
