package com.revy.application.facade.administrator.permission.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PermissionReaderDto {
    public record PermissionView(
            UUID id,
            String code,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record PermissionPage(
            List<PermissionView> content,
            long totalElements,
            int page,
            int size
    ) {
    }
}
