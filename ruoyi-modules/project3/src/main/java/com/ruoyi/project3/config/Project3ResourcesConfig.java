package com.ruoyi.project3.config;

import com.ruoyi.common.core.constant.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class Project3ResourcesConfig implements WebMvcConfigurer
{
    @Value("${project3.profile:${user.dir}/uploadPath}")
    private String profile;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        registry.addResourceHandler(Constants.RESOURCE_PREFIX + "/**")
                .addResourceLocations("file:" + profile + File.separator);
    }
}