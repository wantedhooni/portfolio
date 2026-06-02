package com.revy.example.controller;

public record AiGenerateResponse(
    String promptKey,
    Integer version,
    String model,
    Double temperature,
    String content
) {
}