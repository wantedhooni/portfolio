package com.revy.example.admin.dto;

public record RegisterAdminCommand(
        String email,
        String encodedPassword,
        String name
) {}
