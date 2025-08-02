package com.wasabi.wasabi.src.utils.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtProperties {
    private String secret;
    private long expirationMs;
}
