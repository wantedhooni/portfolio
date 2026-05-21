package com.revy.example.admin.dto;

import com.revy.example.domain.admin.AdminRole;

import java.util.Set;
import java.util.stream.Collectors;

public record AdminRoleResult(
        Long id,
        String name,
        String description,
        Set<String> permissions
) {

    public static AdminRoleResult from(AdminRole role) {
        Set<String> perms = role.getPermissions().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        return new AdminRoleResult(role.getId(), role.getName(), role.getDescription(), perms);
    }
}
