package com.wasabi.wasabi.src.fileupload.controller;

import com.wasabi.wasabi.src.fileupload.dto.FileMetadataDto;
import com.wasabi.wasabi.src.fileupload.dto.FileUploadResponse;
import com.wasabi.wasabi.src.fileupload.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "File upload and management endpoints")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a single file", description = "Upload a single file with optional metadata")
    @ApiResponse(responseCode = "201", description = "File uploaded successfully")
    @ApiResponse(responseCode = "400", description = "Invalid file or parameters")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @Parameter(description = "File to upload", required = true) @RequestParam("file") MultipartFile file,

            @Parameter(description = "File description", required = false) @RequestParam(value = "description", required = false) String description,

            @Parameter(description = "Whether the file is public", required = false) @RequestParam(value = "isPublic", required = false, defaultValue = "true") Boolean isPublic,

            @Parameter(description = "Username of the uploader", required = true) @RequestParam("uploadedBy") String uploadedBy) {
        FileMetadataDto metadata = FileMetadataDto.builder()
                .description(description)
                .isPublic(isPublic)
                .build();

        FileUploadResponse response = fileUploadService.uploadFile(file, metadata, uploadedBy);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload multiple files", description = "Upload multiple files with optional metadata")
    @ApiResponse(responseCode = "201", description = "Files uploaded successfully")
    @ApiResponse(responseCode = "400", description = "Invalid files or parameters")
    public ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(
            @Parameter(description = "Files to upload", required = true) @RequestParam("files") List<MultipartFile> files,

            @Parameter(description = "Files description", required = false) @RequestParam(value = "description", required = false) String description,

            @Parameter(description = "Whether the files are public", required = false) @RequestParam(value = "isPublic", required = false, defaultValue = "true") Boolean isPublic,

            @Parameter(description = "Username of the uploader", required = true) @RequestParam("uploadedBy") String uploadedBy) {
        FileMetadataDto metadata = FileMetadataDto.builder()
                .description(description)
                .isPublic(isPublic)
                .build();

        List<FileUploadResponse> responses = fileUploadService.uploadMultipleFiles(files, metadata, uploadedBy);
        return new ResponseEntity<>(responses, HttpStatus.CREATED);
    }

    @GetMapping("/download/{fileName:.+}")
    @Operation(summary = "Download a file", description = "Download a file by its filename")
    @ApiResponse(responseCode = "200", description = "File downloaded successfully")
    @ApiResponse(responseCode = "404", description = "File not found")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "Name of the file to download", required = true) @PathVariable String fileName) {
        // Get file info to retrieve the stored content type
        FileUploadResponse fileInfo = fileUploadService.getFileInfo(fileName);
        Resource resource = fileUploadService.downloadFile(fileName);

        String contentType = fileInfo.getContentType() != null ? fileInfo.getContentType() : "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileInfo.getOriginalFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/info/{fileName:.+}")
    public ResponseEntity<FileUploadResponse> getFileInfo(@PathVariable String fileName) {
        FileUploadResponse response = fileUploadService.getFileInfo(fileName);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<List<FileUploadResponse>> getUserFiles(@RequestParam("uploadedBy") String uploadedBy) {
        List<FileUploadResponse> responses = fileUploadService.getUserFiles(uploadedBy);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/public")
    public ResponseEntity<List<FileUploadResponse>> getPublicFiles() {
        List<FileUploadResponse> responses = fileUploadService.getPublicFiles();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/type")
    public ResponseEntity<List<FileUploadResponse>> getFilesByType(
            @RequestParam("contentType") String contentTypePrefix) {
        List<FileUploadResponse> responses = fileUploadService.getFilesByType(contentTypePrefix);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @DeleteMapping("/{fileName:.+}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileName,
            @RequestParam("uploadedBy") String uploadedBy) {
        fileUploadService.deleteFile(fileName, uploadedBy);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {
        FileUploadResponse fileInfo = fileUploadService.getFileInfo(fileName);
        Resource resource = fileUploadService.downloadFile(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                .body(resource);
    }

}
