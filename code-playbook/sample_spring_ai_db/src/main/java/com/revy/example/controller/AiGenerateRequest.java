package com.revy.example.controller;

import java.util.Map;

public record AiGenerateRequest(
    String promptKey,
    Integer version,
    Map<String, Object> variables
) {
}