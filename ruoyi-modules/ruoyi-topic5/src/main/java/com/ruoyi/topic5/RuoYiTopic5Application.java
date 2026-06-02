package com.ruoyi.topic5;

import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 课题五质量追溯模块启动类
 *
 * 当前阶段测试：
 * 1. 故障表征输入；
 * 2. 算法选择；
 * 3. 故障零件定位；
 * 4. 候选零件结果生成；
 * 5. 最终故障零件ID回写。
 */
@EnableCustomConfig
@EnableRyFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.ruoyi.topic5.mapper")
public class RuoYiTopic5Application
{
    public static void main(String[] args)
    {
        SpringApplication.run(RuoYiTopic5Application.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  课题五质量追溯模块启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}