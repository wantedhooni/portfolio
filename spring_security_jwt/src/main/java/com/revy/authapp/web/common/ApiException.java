package com.revy.authapp.web.common;

/**
 * 에러 코드 기반 예외를 정의한다.
 */
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * 에러 코드로 예외를 생성한다.
     *
     * @param errorCode 에러 코드
     */
    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 에러 코드를 반환한다.
     *
     * @return 에러 코드
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
