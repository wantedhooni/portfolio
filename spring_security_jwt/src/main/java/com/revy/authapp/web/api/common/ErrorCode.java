package com.revy.authapp.web.api.common;

/**
 * 예외 코드 목록을 정의한다.
 */
public enum ErrorCode {
    DUPLICATE_EMAIL("AUTH-001", "이미 사용 중인 이메일입니다."),
    USER_NOT_FOUND("AUTH-002", "존재하지 않는 사용자입니다."),
    INVALID_PASSWORD("AUTH-003", "비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN("AUTH-004", "유효하지 않은 토큰입니다."),
    INVALID_REFRESH_TOKEN("AUTH-005", "리프레시 토큰이 유효하지 않습니다."),
    REFRESH_TOKEN_NOT_FOUND("AUTH-006", "리프레시 토큰이 존재하지 않습니다."),
    REFRESH_TOKEN_MISMATCH("AUTH-007", "리프레시 토큰이 일치하지 않습니다."),
    ROLE_NOT_FOUND("AUTH-008", "기본 역할 정보가 없습니다."),
    AUTHORITY_NOT_FOUND("AUTH-009", "권한 정보가 없습니다."),
    BEARER_TOKEN_REQUIRED("AUTH-010", "Bearer 토큰이 필요합니다."),
    INTERNAL_ERROR("SYS-001", "서버 오류가 발생했습니다.");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 에러 코드를 반환한다.
     *
     * @return 에러 코드
     */
    public String getCode() {
        return code;
    }

    /**
     * 에러 메시지를 반환한다.
     *
     * @return 에러 메시지
     */
    public String getMessage() {
        return message;
    }
}
