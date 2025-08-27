package com.wasabi.wasabi.src.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wasabi.wasabi.src.auth.dto.ProfilePicUploadResponse;
import com.wasabi.wasabi.src.auth.services.ProfilePic;
import com.wasabi.wasabi.src.fileupload.dto.FileUploadResponse;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile-pic")
public class ProfilePicController {
    final private ProfilePic profilePicService;

    @PostMapping("/upload")
    public ResponseEntity<ProfilePicUploadResponse> uploadProfilePic(
              @Parameter(description = "File to upload", required = true) @RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UUID userId = UUID.fromString(authentication.getName());
        FileUploadResponse response = profilePicService.uploadProfilePic(userId, file);

        ProfilePicUploadResponse dto = new ProfilePicUploadResponse(
                response.getId(),
                response.getOriginalFileName(),
                response.getFileUrl());
        return ResponseEntity.ok(dto);
    }

}
