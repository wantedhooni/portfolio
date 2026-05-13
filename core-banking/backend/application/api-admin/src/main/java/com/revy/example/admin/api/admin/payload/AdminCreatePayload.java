package com.revy.example.admin.api.admin.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AdminCreatePayload {

    public record Request(
            @Email @NotBlank String email,
            @NotBlank String name,
            @NotBlank String password
    ) {
    }
}
