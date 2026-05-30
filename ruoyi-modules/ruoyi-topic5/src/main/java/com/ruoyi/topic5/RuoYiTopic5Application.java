package com.ruoyi.topic5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;

/**
 * 课题五业务模块
 *
 * @author ruoyi
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class RuoYiTopic5Application
{
    public static void main(String[] args)
    {
        SpringApplication.run(RuoYiTopic5Application.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  课题五模块启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}