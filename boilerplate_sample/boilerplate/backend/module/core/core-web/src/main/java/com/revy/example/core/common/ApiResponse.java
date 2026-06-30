package com.revy.example.core.common;

public record ApiResponse<T>(boolean success,
                             T data,
                             String message,
                             long timestamp

) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, System.currentTimeMillis());
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null, System.currentTimeMillis());
    }

    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(false, null, message, System.currentTimeMillis());
    }
}
