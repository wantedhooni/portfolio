package com.revy.example.core.error;


/**
 * 애플리케이션에서 사용하는 비즈니스 오류 코드를 정의합니다.
 */
public enum ErrorCode {
    // ── Common ────────────────────────────────────────────────────
    INVALID_INPUT(400, "COMMON-001", "잘못된 요청입니다."),
    UNAUTHORIZED(401, "COMMON-002", "인증이 필요합니다."),
    FORBIDDEN(403, "COMMON-003", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(500, "COMMON-004", "서버 오류가 발생했습니다."),
    ENTITY_NOT_FOUND(404, "COMMON-005", "요청한 리소스를 찾을 수 없습니다."),

    // ── Auth ──────────────────────────────────────────────────────
    INVALID_CREDENTIALS(401, "AUTH-001", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(401, "AUTH-002", "유효하지 않은 토큰입니다."),
    REFRESH_TOKEN_EXPIRED(401, "AUTH-003", "리프레시 토큰이 만료되었습니다."),
    // ── User ──────────────────────────────────────────────────────
    USER_NOT_FOUND(404, "USER-001", "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(409, "USER-002", "이미 사용 중인 이메일입니다."),
    // ── Post ──────────────────────────────────────────────────────
    POST_NOT_FOUND(404, "POST-001", "게시글을 찾을 수 없습니다."),
    // ── Account ───────────────────────────────────────────────────
    INSUFFICIENT_BALANCE(422, "ACCOUNT-001", "잔고가 부족합니다."),
    ACCOUNT_NOT_ACTIVE(403, "ACCOUNT-002", "계좌가 활성 상태가 아닙니다."),
    ACCOUNT_NOT_FOUND(404, "ACCOUNT-003", "계좌를 찾을 수 없습니다."),
    ACCOUNT_NUMBER_DUPLICATED(409, "ACCOUNT-004", "이미 사용 중인 계좌번호입니다."),

    // ── Stock ─────────────────────────────────────────────────────
    STOCK_NOT_FOUND(404, "STOCK-001", "종목을 찾을 수 없습니다."),
    STOCK_DUPLICATED(409, "STOCK-002", "이미 등록된 종목입니다."),
    STOCK_NOT_ACTIVE(422, "STOCK-003", "거래 중단된 종목입니다."),

    // ── Position ──────────────────────────────────────────────────
    INSUFFICIENT_POSITION(422, "POSITION-001", "보유 수량이 부족합니다."),
    INVALID_LOT_DISPOSAL(422, "POSITION-002", "처분 수량이 잔여 수량을 초과합니다.");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }


}
