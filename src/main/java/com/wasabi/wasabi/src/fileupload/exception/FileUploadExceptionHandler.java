package com.wasabi.wasabi.src.fileupload.exception;

import com.wasabi.wasabi.src.utils.response.ApiResponse;
import com.wasabi.wasabi.src.utils.response.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
@Slf4j
public class FileUploadExceptionHandler {
    
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiResponse<String>> handleFileUploadException(FileUploadException ex) {
        log.error("File upload error: {}", ex.getMessage());
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(false)
                .error(ex.getMessage())
                .errorType(ErrorType.VALIDATION_ERROR.getType())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleFileNotFoundException(FileNotFoundException ex) {
        log.error("File not found: {}", ex.getMessage());
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(false)
                .error(ex.getMessage())
                .errorType(ErrorType.NOT_FOUND.getType())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<String>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.error("File size exceeded: {}", ex.getMessage());
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(false)
                .error("File size exceeds maximum allowed size")
                .errorType(ErrorType.VALIDATION_ERROR.getType())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
