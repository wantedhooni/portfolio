package com.revy.example.controller;

public record UserCreateRequest(
        String email,
        String name,
        String password
) {
}