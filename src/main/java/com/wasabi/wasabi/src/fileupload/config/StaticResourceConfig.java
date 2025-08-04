package com.wasabi.wasabi.src.fileupload.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class StaticResourceConfig implements WebMvcConfigurer {
    
    private final FileUploadConfig fileUploadConfig;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = fileUploadConfig.getDir();
        if (!uploadDir.startsWith("file:")) {
            uploadDir = "file:" + uploadDir + "/";
        }
        
        registry.addResourceHandler("/api/files/**")
                .addResourceLocations(uploadDir)
                .setCachePeriod(3600); // Cache for 1 hour
    }
}
