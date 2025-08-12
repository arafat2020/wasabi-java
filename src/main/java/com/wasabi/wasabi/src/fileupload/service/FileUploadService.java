package com.wasabi.wasabi.src.fileupload.service;

import com.wasabi.wasabi.src.fileupload.config.FileUploadConfig;
import com.wasabi.wasabi.src.fileupload.dto.FileMetadataDto;
import com.wasabi.wasabi.src.fileupload.dto.FileUploadResponse;
import com.wasabi.wasabi.src.fileupload.exception.FileNotFoundException;
import com.wasabi.wasabi.src.fileupload.exception.FileUploadException;
import com.wasabi.wasabi.src.fileupload.model.FileUpload;
import com.wasabi.wasabi.src.fileupload.repository.FileUploadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FileUploadService {
    
    private final FileUploadRepository fileUploadRepository;
    private final FileUploadConfig fileUploadConfig;
    
    public FileUploadResponse uploadFile(MultipartFile file, FileMetadataDto metadata, String uploadedBy) {
        try {
            validateFile(file);
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(fileUploadConfig.getDir()).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            
            // Generate unique filename
            String originalFileName = file.getOriginalFilename() != null ? StringUtils.cleanPath(file.getOriginalFilename()) : "";
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
            
            // Copy file to upload directory
            Path targetLocation = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Generate file URL
            String fileUrl = fileUploadConfig.getUrlPrefix() + "/" + uniqueFileName;
            
            // Save file metadata to database
            FileUpload fileUpload = FileUpload.builder()
                    .originalFileName(originalFileName)
                    .fileName(uniqueFileName)
                    .filePath(targetLocation.toString())
                    .fileUrl(fileUrl)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .uploadedBy(uploadedBy)
                    .isPublic(metadata != null ? metadata.getIsPublic() : true)
                    .description(metadata != null ? metadata.getDescription() : null)
                    .build();
            
            FileUpload savedFile = fileUploadRepository.save(fileUpload);
            
            log.info("File uploaded successfully: {} by user {}", uniqueFileName, uploadedBy);
            
            return mapToResponse(savedFile, "File uploaded successfully");
            
        } catch (IOException ex) {
            log.error("Failed to upload file: {}", ex.getMessage());
            throw new FileUploadException("Failed to store file " + file.getOriginalFilename(), ex);
        }
    }
    
    public List<FileUploadResponse> uploadMultipleFiles(List<MultipartFile> files, FileMetadataDto metadata, String uploadedBy) {
        return files.stream()
                .map(file -> uploadFile(file, metadata, uploadedBy))
                .collect(Collectors.toList());
    }
    
    public Resource downloadFile(String fileName) {
        try {
            FileUpload fileUpload = fileUploadRepository.findByFileName(fileName)
                    .orElseThrow(() -> new FileNotFoundException("File not found: " + fileName));
            
            Path filePath = Paths.get(fileUpload.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found or not readable: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("File not found: " + fileName, ex);
        }
    }
    
    public FileUploadResponse getFileInfo(String fileName) {
        FileUpload fileUpload = fileUploadRepository.findByFileName(fileName)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileName));
        
        return mapToResponse(fileUpload, null);
    }
    
    public List<FileUploadResponse> getUserFiles(String uploadedBy) {
        List<FileUpload> files = fileUploadRepository.findByUploadedBy(uploadedBy);
        return files.stream()
                .map(file -> mapToResponse(file, null))
                .collect(Collectors.toList());
    }
    
    public List<FileUploadResponse> getPublicFiles() {
        List<FileUpload> files = fileUploadRepository.findAllPublicFiles();
        return files.stream()
                .map(file -> mapToResponse(file, null))
                .collect(Collectors.toList());
    }
    
    public List<FileUploadResponse> getFilesByType(String contentTypePrefix) {
        List<FileUpload> files = fileUploadRepository.findByContentTypeStartingWith(contentTypePrefix);
        return files.stream()
                .map(file -> mapToResponse(file, null))
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteFile(String fileName, String uploadedBy) {
        FileUpload fileUpload = fileUploadRepository.findByFileName(fileName)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileName));
        
        // Check if user owns the file or is admin (you can customize this logic)
        if (!fileUpload.getUploadedBy().equals(uploadedBy)) {
            throw new FileUploadException("You don't have permission to delete this file");
        }
        
        try {
            // Delete physical file
            Path filePath = Paths.get(fileUpload.getFilePath());
            Files.deleteIfExists(filePath);
            
            // Delete from database
            fileUploadRepository.delete(fileUpload);
            
            log.info("File deleted successfully: {} by user {}", fileName, uploadedBy);
            
        } catch (IOException ex) {
            log.error("Failed to delete file: {}", ex.getMessage());
            throw new FileUploadException("Failed to delete file: " + fileName, ex);
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileUploadException("Please select a file to upload");
        }
        
        String fileName = file.getOriginalFilename() != null ? StringUtils.cleanPath(file.getOriginalFilename()) : "";
        if (fileName.contains("..")) {
            throw new FileUploadException("Filename contains invalid path sequence: " + fileName);
        }
        
        String fileExtension = getFileExtension(fileName);
        if (!fileUploadConfig.isExtensionAllowed(fileExtension)) {
            throw new FileUploadException("File extension not allowed: " + fileExtension);
        }
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    private FileUploadResponse mapToResponse(FileUpload fileUpload, String message) {
        return FileUploadResponse.builder()
                .id(fileUpload.getId().toString())
                .originalFileName(fileUpload.getOriginalFileName())
                .fileName(fileUpload.getFileName())
                .fileUrl(fileUpload.getFileUrl())
                .contentType(fileUpload.getContentType())
                .fileSize(fileUpload.getFileSize())
                .uploadedBy(fileUpload.getUploadedBy())
                .uploadedAt(fileUpload.getUploadedAt())
                .isPublic(fileUpload.getIsPublic())
                .description(fileUpload.getDescription())
                .message(message)
                .build();
    }
}
