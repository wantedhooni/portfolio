package com.revy.application.facade.administrator.admin.dto;

import com.revy.common.domain.enums.admin.AdminStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AdminReaderDto {
    public record RoleRef(
            UUID id,
            String name
    ) {
    }

    public record AuthAdmin(
            UUID id,
            String email,
            String encodedPassword,
            AdminStatus status,
            boolean enabled,
            Set<String> roleNames
    ) {
    }

    public record AdminView(
            UUID id,
            String email,
            AdminStatus status,
            boolean enabled,
            Set<UUID> roleIds,
            Set<String> roleNames,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record AdminPage(
            List<AdminView> content,
            long totalElements,
            int page,
            int size
    ) {
    }
}
