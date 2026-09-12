package com.revy.authapp.web.common;

/**
 * 에러 응답 정보를 전달한다.
 */
public record ErrorResponse(
    String code,
    String message
) {
    /**
     * 에러 코드로부터 응답을 생성한다.
     *
     * @param errorCode 에러 코드
     * @return 에러 응답
     */
    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }

    public static ErrorResponse from(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.getCode(), message);
    }
}
