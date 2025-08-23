package com.wasabi.wasabi.src.auth.services;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wasabi.wasabi.src.auth.model.UserModel;
import com.wasabi.wasabi.src.auth.repository.AuthRepository;
import com.wasabi.wasabi.src.fileupload.dto.FileUploadResponse;
import com.wasabi.wasabi.src.fileupload.model.FileUpload;
import com.wasabi.wasabi.src.fileupload.service.FileUploadService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfilePic {
    private final AuthRepository userRepository;
    private final FileUploadService fileUploadService;

    public FileUploadResponse uploadProfilePic(UUID userId, MultipartFile file) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileUploadResponse response = fileUploadService.uploadFile(file, null, user.getUsername());

        FileUpload fileUpload = fileUploadService.getEntityById(UUID.fromString(response.getId()));

        // Update user's profile image
        user.setProfileImage(fileUpload);
        userRepository.save(user);

        return response;
    }
}
