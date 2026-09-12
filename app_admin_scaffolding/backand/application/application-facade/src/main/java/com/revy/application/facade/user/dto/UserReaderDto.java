package com.revy.application.facade.user.dto;

import com.revy.common.domain.enums.user.UserStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UserReaderDto {

    public record AuthUser(
            UUID id,
            String email,
            String encodedPassword,
            UserStatus status,
            boolean enabled,
            Set<String> roleNames
    ) {

    }

    public record UserView(
            UUID id,
            String email,
            String firstName,
            String lastName,
            String nickName,
            UserStatus status,
            int failCount,
            boolean enabled,
            Set<String> permissions,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record UserPage(
            List<UserReaderDto.UserView> content,
            long totalElements,
            int page,
            int size
    ) {
    }
}
