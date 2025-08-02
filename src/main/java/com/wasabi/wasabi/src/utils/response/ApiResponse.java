package com.wasabi.wasabi.src.utils.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private String error;
    private String errorType;
    private List<String> validationErrors;
    private String details;
    private T data;

    // Success response builders
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .build();
    }

    // Error response builders
    public static ApiResponse<Void> error(String error, String errorType) {
        return ApiResponse.<Void>builder()
                .success(false)
                .error(error)
                .errorType(errorType)
                .build();
    }

    public static ApiResponse<Void> validationError(String error, List<String> validationErrors) {
        return ApiResponse.<Void>builder()
                .success(false)
                .error(error)
                .errorType("VALIDATION_ERROR")
                .validationErrors(validationErrors)
                .build();
    }

    public static ApiResponse<Void> internalError(String error, String details) {
        return ApiResponse.<Void>builder()
                .success(false)
                .error(error)
                .errorType("INTERNAL_ERROR")
                .details(details)
                .build();
    }
}
