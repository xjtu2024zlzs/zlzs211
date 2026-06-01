package com.ruoyi.project3.controller;

import com.ruoyi.common.core.domain.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * project3测试接口
 */
@RestController
public class TestController
{
    @GetMapping("/test")
    public R<String> test()
    {
        return R.ok("project3模块访问成功");
    }
}