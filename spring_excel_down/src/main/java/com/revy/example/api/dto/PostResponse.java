package com.revy.example.api.dto;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate
) {}
