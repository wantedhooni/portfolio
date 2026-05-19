package com.revy.example.core.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "ApiPageResponse")
public record ApiPageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {
    public static <T> ApiPageResponse<T> of(List<T> content, long totalElements, int page, int size) {
        int safeSize = Math.max(size, 1);
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        return new ApiPageResponse<>(content, totalElements, totalPages, page, size);
    }
}
