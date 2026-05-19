package com.revy.example.user.reader.dto;

import com.revy.example.domain.user.User;

public record UserResult(
        Long id,
        String email,
        String name,
        String role
) {

    public static UserResult from(User user) {
        return new UserResult(user.getId(), user.getEmail(), user.getName(), user.getRole());
    }
}
