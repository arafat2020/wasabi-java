package com.wasabi.wasabi.src.fileupload.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class FileSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // disable CSRF for testing/public endpoints
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/files/**").permitAll() // allow public file access
                .anyRequest().authenticated() // everything else needs login
            );

        return http.build();
    }
}
