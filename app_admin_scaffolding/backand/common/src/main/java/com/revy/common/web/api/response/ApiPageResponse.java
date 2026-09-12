package com.revy.common.web.api.response;

import java.util.List;

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
