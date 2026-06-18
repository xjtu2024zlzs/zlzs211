package com.ruoyi.project4;

import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableCustomConfig
@EnableRyFeignClients
@MapperScan("com.ruoyi.project4.mapper")
@SpringBootApplication
public class RuoYiProject4Application
{
    public static void main(String[] args)
    {
        SpringApplication.run(RuoYiProject4Application.class, args);
        System.out.println("课题四故障诊断模块启动成功");
    }
}