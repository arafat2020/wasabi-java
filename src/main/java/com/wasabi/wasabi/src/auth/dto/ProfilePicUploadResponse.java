package com.wasabi.wasabi.src.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfilePicUploadResponse {
     private String fileId;
    private String originalFileName;
    private String fileUrl;
}
