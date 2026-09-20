package com.rohith.rohithmart.dto;

/**
 * Standard API response envelope adhering to Capstone Section 13:
 * { "success": true, "data": { ... }, "error": null }
 * { "success": false, "data": null, "error": { "code": "...", "message": "..." } }
 *
 * @param <T> Payload type
 */
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ApiError error;

    public ApiResponse() {
    }

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.error = null;
        return response;
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.data = null;
        response.error = new ApiError(code, message);
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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
