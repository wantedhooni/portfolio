package com.revy.example.admin.dto;

import com.revy.example.domain.admin.Admin;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record AdminResult(
        Long id,
        String email,
        String name,
        String role,
        Set<String> permissions,
        List<AdminRoleResult> roles
) {

    public static AdminResult from(Admin admin) {
        List<AdminRoleResult> roleResults = admin.getRoles().stream()
                .map(AdminRoleResult::from)
                .toList();
        Set<String> permissions = admin.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Enum::name)
                .collect(Collectors.toSet());
        return new AdminResult(admin.getId(), admin.getEmail(), admin.getName(), admin.getRole(), permissions, roleResults);
    }
}
