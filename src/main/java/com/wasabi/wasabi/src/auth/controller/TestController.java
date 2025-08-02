package com.wasabi.wasabi.src.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wasabi.wasabi.src.utils.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Test Controller", description = "Protected endpoints for testing JWT authentication")
public class TestController {

    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Get the current user's profile information")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<?>> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", 
            "Welcome " + username + "! This is a protected endpoint."));
    }

    @GetMapping("/public")
    @Operation(summary = "Public endpoint", description = "This endpoint doesn't require authentication")
    public ResponseEntity<ApiResponse<?>> getPublicData() {
        return ResponseEntity.ok(ApiResponse.success("Public data retrieved", 
            "This is a public endpoint - no authentication required!"));
    }
}
