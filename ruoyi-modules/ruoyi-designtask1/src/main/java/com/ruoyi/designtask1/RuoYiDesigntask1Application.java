package com.ruoyi.designtask1;

import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 设计制造协同优化模块
 *
 * @author ruoyi
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication(scanBasePackages = { "com.ruoyi.designtask1", "com.ruoyi.system.api.factory" })
public class RuoYiDesigntask1Application {

    public static void main(String[] args) {
        SpringApplication.run(RuoYiDesigntask1Application.class, args);
        System.out.println("ruoyi-designtask1 设计制造协同优化模块启动成功");
    }
}
