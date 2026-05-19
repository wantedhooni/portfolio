package com.revy.example.admin.api.user.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserPayload {

    public record CreateRequest() {
    }

    public record UpdateRequest(@NotBlank
                                String name) {
    }

    public record SearchRequest(Long id,
                                String name) {
    }


    public record ModelResponse(Long id,
                                String email,
                                String name) {
    }
}
