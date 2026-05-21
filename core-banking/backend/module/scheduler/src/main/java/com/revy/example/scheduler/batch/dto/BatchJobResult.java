package com.revy.example.scheduler.batch.dto;

public record BatchJobResult(
        String name,
        int instanceCount,
        Long lastExecutionId,
        String lastStatus,
        String lastExitCode
) {}
