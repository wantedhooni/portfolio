package com.revy.example.admin.dto;

import com.revy.example.domain.admin.Admin;

public record AdminResult(
        Long id,
        String email,
        String name,
        String role
) {

    public static AdminResult from(Admin admin) {
        return new AdminResult(admin.getId(), admin.getEmail(), admin.getName(), admin.getRole());
    }
}
