package com.ruoyi.project1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;

/**
 * Project1 service.
 *
 * @author ruoyi
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class RuoYiProject1Application
{
    public static void main(String[] args)
    {
        SpringApplication.run(RuoYiProject1Application.class, args);
        System.out.println("ruoyi-project1 service started successfully.");
    }
}
