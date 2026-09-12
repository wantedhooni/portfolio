package com.revy.application.facade.administrator.admin.dto;

public class CreateAdminDto {
    public record Command(
            String email,
            String rawPassword
    ) {
    }

    public record Result(
            String id,
            String email
    ) {
    }
}
