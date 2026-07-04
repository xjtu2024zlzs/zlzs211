package com.ruoyi.project4;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = {
        "com.ruoyi.project4.feign",
        "com.ruoyi.system.api"
})
@MapperScan("com.ruoyi.project4.domain.mapper")
@SpringBootApplication
public class RuoYiProject4Application
{
    public static void main(String[] args)
    {
        SpringApplication.run(RuoYiProject4Application.class, args);
        System.out.println("课题四故障诊断模块启动成功");
    }
}