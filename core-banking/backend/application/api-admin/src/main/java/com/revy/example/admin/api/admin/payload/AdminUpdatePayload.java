package com.revy.example.admin.api.admin.payload;

import jakarta.validation.constraints.NotBlank;

public class AdminUpdatePayload {

    public record Request(@NotBlank String name) {
    }
}
