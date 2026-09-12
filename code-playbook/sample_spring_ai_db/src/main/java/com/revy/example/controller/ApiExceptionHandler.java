package com.revy.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage(), e);
        return new ErrorResponse(
            LocalDateTime.now(),
            "BAD_REQUEST",
            e.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception e) {
        log.error("Exception: {}", e.getMessage(), e);
        return new ErrorResponse(
            LocalDateTime.now(),
            "INTERNAL_SERVER_ERROR",
            e.getMessage()
        );
    }

    public record ErrorResponse(
        LocalDateTime timestamp,
        String code,
        String message
    ) {
    }
}