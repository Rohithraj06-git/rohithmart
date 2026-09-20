package com.rohith.rohithmart.dto;

import com.rohith.rohithmart.exception.AppException;

import java.util.Map;

/**
 * Standard API response envelope supporting both:
 * 1) Concise format:
 *    Success: { "success": true, "message": "...", "data": { ... } }
 *    Error:   { "success": false, "message": "...", "errorCode": "...", "fieldErrors": { ... } }
 * 2) Nested error envelope format:
 *    { "success": false, "data": null, "error": { "code": "...", "message": "..." } }
 *
 * @param <T> Payload type
 */
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private String errorCode;
    private Map<String, String> fieldErrors;
    private T data;
    private ApiError error;

    public ApiResponse() {
    }

    public static <T> ApiResponse<T> success(T data) {
        return success("Operation successful", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        response.data = data;
        response.errorCode = null;
        response.error = null;
        return response;
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return error(errorCode, message, null);
    }

    public static <T> ApiResponse<T> error(String errorCode, String message, Map<String, String> fieldErrors) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.errorCode = errorCode;
        response.fieldErrors = (fieldErrors != null && !fieldErrors.isEmpty()) ? fieldErrors : null;
        response.data = null;
        response.error = new ApiError(errorCode, message);
        return response;
    }

    public static <T> ApiResponse<T> fromException(AppException ex) {
        return error(ex.getErrorCode(), ex.getMessage(), ex.getFieldErrors());
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ApiError getError() {
        return error;
    }

    public void setError(ApiError error) {
        this.error = error;
    }
}
