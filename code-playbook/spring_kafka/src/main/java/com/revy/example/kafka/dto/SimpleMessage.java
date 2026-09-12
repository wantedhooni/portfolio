package com.revy.example.kafka.dto;

public record SimpleMessage(
        String id,
        String message
) {}