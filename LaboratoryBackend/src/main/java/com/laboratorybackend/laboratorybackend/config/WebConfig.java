package com.laboratorybackend.laboratorybackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${image.upload-path}")
    private String uploadPath;

    @Value("${fault.image.upload-path}")
    private String faultUploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //将/images/**映射到外部目录
        //以 file:开头，并且路径末尾要加/
        String location = "file:" + uploadPath.replace("\\", "/");
        if (!location.endsWith("/")) {
            location = location + "/";
        }

        registry.addResourceHandler("/images/**")
                .addResourceLocations(location);

        //故障图片映射
        String faultLocation = "file:" + faultUploadPath.replace("\\", "/");
        if (!faultLocation.endsWith("/")) {
            faultLocation = faultLocation + "/";
        }
        registry.addResourceHandler("/fault-images/**")
                .addResourceLocations(faultLocation);
    }
}