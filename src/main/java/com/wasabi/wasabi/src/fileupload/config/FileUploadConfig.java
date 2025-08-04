package com.wasabi.wasabi.src.fileupload.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "file.upload")
@Data
public class FileUploadConfig {
    
    private String dir;
    private String urlPrefix;
    private List<String> allowedExtensions;
    
    public boolean isExtensionAllowed(String extension) {
        return allowedExtensions != null && 
               allowedExtensions.stream()
                   .anyMatch(allowed -> allowed.equalsIgnoreCase(extension));
    }
}
