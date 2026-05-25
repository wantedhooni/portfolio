package com.revy.example.quartz.service;

/**
 * 보험료 정산 배치에서 계약 1건 처리 결과를 나타내는 열거형입니다.
 *
 * <p>기존 {@code Boolean}(null=SKIPPED) 반환 타입의 암묵성을 제거하고,
 * 호출부에서 의미를 명확히 파악할 수 있도록 합니다.</p>
 */
public enum ProcessResult {

    /** 보험료 출금 및 정산 기록 성공 */
    SUCCESS,

    /** 출금 실패 (잔고 부족 등) — 정산 기록은 FAIL 상태로 생성됨 */
    FAILED,

    /** 동일 referenceId 로 이미 처리된 계약 — 멱등성 스킵 */
    SKIPPED
}
