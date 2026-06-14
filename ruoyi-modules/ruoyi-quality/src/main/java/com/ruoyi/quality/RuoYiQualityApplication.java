package com.ruoyi.quality;

import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 质量问题管理中心模块
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class RuoYiQualityApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(RuoYiQualityApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  质量问题管理中心模块启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}