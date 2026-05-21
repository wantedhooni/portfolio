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
    SAME_ACCOUNT_TRANSFER(422, "ACCOUNT-005", "출금 계좌와 입금 계좌가 동일합니다."),
    CURRENCY_MISMATCH(422, "ACCOUNT-006", "두 계좌의 통화가 일치하지 않습니다. 환전을 이용하세요."),

    // ── Stock ─────────────────────────────────────────────────────
    STOCK_NOT_FOUND(404, "STOCK-001", "종목을 찾을 수 없습니다."),
    STOCK_DUPLICATED(409, "STOCK-002", "이미 등록된 종목입니다."),
    STOCK_NOT_ACTIVE(422, "STOCK-003", "거래 중단된 종목입니다."),

    // ── Position ──────────────────────────────────────────────────
    INSUFFICIENT_POSITION(422, "POSITION-001", "보유 수량이 부족합니다."),
    INVALID_LOT_DISPOSAL(422, "POSITION-002", "처분 수량이 잔여 수량을 초과합니다."),

    // ── Insurance ─────────────────────────────────────────────────
    INSURANCE_PRODUCT_NOT_FOUND(404, "INSURANCE-001", "보험 상품을 찾을 수 없습니다."),
    INSURANCE_PRODUCT_DISCONTINUED(422, "INSURANCE-002", "판매 중단된 보험 상품입니다."),
    POLICY_NOT_FOUND(404, "POLICY-001", "보험 계약을 찾을 수 없습니다."),
    POLICY_NOT_ACTIVE(422, "POLICY-002", "활성 상태의 보험 계약이 아닙니다."),
    POLICY_EXPIRED(422, "POLICY-003", "보험 계약이 만료되었습니다."),
    POLICY_ALREADY_ACTIVE(409, "POLICY-004", "이미 활성화된 보험 계약입니다."),
    BENEFICIARY_SHARE_INVALID(422, "POLICY-005", "수익자 지분 합계는 100%여야 합니다."),
    PREMIUM_ALREADY_PAID(409, "PREMIUM-001", "이미 납부된 보험료입니다."),
    PREMIUM_PAYMENT_OVERDUE(422, "PREMIUM-002", "납부 기한이 지난 보험료입니다."),
    CLAIM_NOT_FOUND(404, "CLAIM-001", "보험금 청구를 찾을 수 없습니다."),
    CLAIM_NOT_PENDING(422, "CLAIM-002", "심사 가능한 상태가 아닙니다."),
    CLAIM_ALREADY_PAID(409, "CLAIM-003", "이미 지급된 청구입니다."),
    CLAIM_AMOUNT_EXCEEDS_COVERAGE(422, "CLAIM-004", "청구금액이 보장한도를 초과합니다."),

    // ── FX ────────────────────────────────────────────────────────
    CURRENCY_NOT_FOUND(404, "FX-001", "통화를 찾을 수 없습니다."),
    CURRENCY_DUPLICATED(409, "FX-002", "이미 등록된 통화입니다."),
    CURRENCY_NOT_ACTIVE(422, "FX-003", "거래 중단된 통화입니다."),
    EXCHANGE_RATE_NOT_FOUND(404, "FX-004", "환율을 찾을 수 없습니다."),
    FX_CONVERSION_FAILED(422, "FX-005", "환전 처리에 실패했습니다."),
    INVALID_FX_PAIR(422, "FX-006", "유효하지 않은 통화쌍입니다."),

    // ── Ledger ────────────────────────────────────────────────────
    LEDGER_ACCOUNT_NOT_FOUND(404, "LEDGER-001", "원장 계정을 찾을 수 없습니다."),
    LEDGER_ACCOUNT_DUPLICATED(409, "LEDGER-002", "이미 등록된 계정 코드입니다."),
    JOURNAL_ENTRY_NOT_FOUND(404, "LEDGER-003", "분개를 찾을 수 없습니다."),
    UNBALANCED_JOURNAL_ENTRY(422, "LEDGER-004", "차변 합계와 대변 합계가 일치하지 않습니다."),
    JOURNAL_ALREADY_POSTED(409, "LEDGER-005", "이미 전기된 분개입니다."),
    JOURNAL_NOT_POSTED(422, "LEDGER-006", "전기되지 않은 분개입니다."),
    JOURNAL_LINE_INVALID(422, "LEDGER-007", "차변과 대변은 양수 한쪽만 허용됩니다."),
    PERIOD_CLOSED(422, "LEDGER-008", "마감된 회계기간입니다."),
    PERIOD_NOT_FOUND(404, "LEDGER-009", "회계기간을 찾을 수 없습니다."),

    // ── Order ─────────────────────────────────────────────────────
    ORDER_NOT_FOUND(404,   "ORDER-001", "주문을 찾을 수 없습니다."),
    ORDER_NOT_PENDING(422, "ORDER-002", "처리 가능한 상태의 주문이 아닙니다."),
    ORDER_ALREADY_EXECUTED(409, "ORDER-003", "이미 체결된 주문입니다."),

    // ── RBAC ──────────────────────────────────────────────────────
    ROLE_NOT_FOUND(404,      "RBAC-001", "역할을 찾을 수 없습니다."),
    ROLE_DUPLICATED(409,     "RBAC-002", "이미 등록된 역할입니다."),
    ROLE_ALREADY_ASSIGNED(409, "RBAC-003", "이미 할당된 역할입니다."),
    ROLE_NOT_ASSIGNED(404,   "RBAC-004", "할당되지 않은 역할입니다.");

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
