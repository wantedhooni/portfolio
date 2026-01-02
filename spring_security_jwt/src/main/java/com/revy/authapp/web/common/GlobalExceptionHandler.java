package com.revy.authapp.web.common;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 공통 예외 처리를 담당한다.
 */
@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ApiException을 처리한다.
     *
     * @param exception API 예외
     * @return 에러 응답
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
        return ResponseEntity.badRequest().body(ErrorResponse.from(exception.getErrorCode()));
    }

    /**
     * 처리되지 않은 예외를 처리한다.
     *
     * @param exception 예외
     * @return 에러 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.from(ErrorCode.INTERNAL_ERROR, exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, Object> body = body("VALIDATION_ERROR", "Invalid request");
        List<Map<String, String>> errors = e.getBindingResult().getFieldErrors().stream()
                                            .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
                                            .toList();
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }


    private Map<String, Object> body(String code, String message) {
        return new LinkedHashMap<>() {{
            put("timestamp", Instant.now().toString());
            put("code", code);
            put("message", message);
        }};
    }
}
