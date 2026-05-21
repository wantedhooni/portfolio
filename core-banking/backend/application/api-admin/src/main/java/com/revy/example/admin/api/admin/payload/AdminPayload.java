package com.revy.example.admin.api.admin.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Set;

public class AdminPayload {
    public record CreateRequest(@Email
                                @NotBlank
                                String email,
                                @NotBlank
                                String name,
                                @NotBlank
                                String password) {
    }

    public record UpdateRequest(@NotBlank
                                String name) {
    }

    public record SearchRequest(Long id,
                                String name) {
    }

    public record RoleSummary(Long id, String name, Set<String> permissions) {}

    public record ModelResponse(Long id,
                                String email,
                                String name,
                                List<RoleSummary> roles) {
    }
}
