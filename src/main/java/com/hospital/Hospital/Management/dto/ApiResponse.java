package com.hospital.Hospital.Management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper for all REST endpoints.
 * 
 * This class provides a consistent response structure across all API endpoints
 * in the hospital management system. It includes success indicators, messages,
 * and optional data payload with generic type support.
 * 
 * @param <T> The type of data being returned in the response
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    
    /**
     * Indicates whether the API operation was successful.
     */
    private boolean success;
    
    /**
     * Human-readable message describing the operation result.
     */
    private String message;
    
    /**
     * Optional data payload containing the actual response data.
     */
    private T data;
    
    /**
     * Creates a successful response with data.
     * 
     * @param <T> Type of response data
     * @param message Success message
     * @param data Response data
     * @return ApiResponse with success=true and provided data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * Creates a successful response without data.
     * 
     * @param message Success message
     * @return ApiResponse with success=true and no data
     */
    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .build();
    }
    
    /**
     * Creates an error response.
     * 
     * @param message Error message
     * @return ApiResponse with success=false and error message
     */
    public static ApiResponse<Void> error(String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .build();
    }
}