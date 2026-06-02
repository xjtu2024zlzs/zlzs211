package com.ruoyi.project3.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class Project3MultipartConfig
{
    @Bean
    public MultipartConfigElement multipartConfigElement()
    {
        long maxUploadSize = DataSize.ofMegabytes(600).toBytes();
        return new MultipartConfigElement("", maxUploadSize, maxUploadSize, 0);
    }
}
