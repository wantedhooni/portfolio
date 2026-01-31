package com.revy.api_server.application.web.api.common;

import com.revy.api_server.application.exception.AuthException;
import com.revy.common.api.ApiResponse;
import com.revy.common.error.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;


// TODO:Revy -> 나중에 Exception 정리해서 합치자.
@Slf4j
@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuth(AuthException e) {
        log.trace("AuthException:", e);
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({AuthenticationException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuthenticationException(AuthenticationException e) {
        log.trace("AuthenticationException:", e);
        return ApiResponse.fail(String.valueOf(HttpStatus.UNAUTHORIZED.value()), e.getMessage());
    }

    @ExceptionHandler({AccessDeniedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.trace("AccessDeniedException:", e);
        return ApiResponse.fail(String.valueOf(HttpStatus.UNAUTHORIZED.value()), e.getMessage());
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnknown(Exception e) {
        log.error("unknown exception:", e);
        return ApiResponse.fail("INTERNAL_ERROR", e.getMessage());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException e) {
        log.error("api exception:", e);
        HttpStatus status = resolveStatus(e);
        return ResponseEntity.status(status).body(ApiResponse.fail(e.getCode(), e.getMessage()));
    }

    private HttpStatus resolveStatus(ApiException e) {
        if (e.getErrorCode() != null) {
            return switch (e.getErrorCode()) {
                case DUPLICATE_EMAIL -> HttpStatus.CONFLICT;
                case USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
                case INVALID_PASSWORD, INVALID_TOKEN, INVALID_REFRESH_TOKEN, REFRESH_TOKEN_NOT_FOUND,
                        REFRESH_TOKEN_MISMATCH, EXPIRE_TOKEN, INACTIVE_USER -> HttpStatus.UNAUTHORIZED;
                default -> HttpStatus.BAD_REQUEST;
            };
        }
        return HttpStatus.BAD_REQUEST;
    }

}
