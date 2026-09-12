package com.revy.application.facade.administrator.role.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class RoleReaderDto {
    public record AdminRef(
            UUID id,
            String email
    ) {
    }

    public record RoleView(
            UUID id,
            String name,
            String description,
            List<AdminRef> admins,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record RolePage(
            List<RoleView> content,
            long totalElements,
            int page,
            int size
    ) {
    }
}
