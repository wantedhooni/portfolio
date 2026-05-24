package revy.com.client.kafka.dto;

/**
 * 코어뱅킹 시스템에서 사용하는 Kafka 토픽 상수 모음입니다.
 *
 * <p>토픽 명명 규칙: {@code {도메인}.{이벤트명}}</p>
 */
public final class KafkaTopic {

    private KafkaTopic() {}

    // ===== Payment (결제) =====
    /** 결제 성공 이벤트 */
    public static final String PAYMENT_COMPLETED = "banking.payment.completed";

    /** 결제 실패 이벤트 */
    public static final String PAYMENT_FAILED    = "banking.payment.failed";

    // ===== Settlement (정산) =====
    /** 보험료 정산 배치 처리 결과 이벤트 */
    public static final String SETTLEMENT_RESULT = "banking.settlement.result";

    // ===== Policy (계약) =====
    /** 보험 계약 생성 이벤트 */
    public static final String POLICY_CREATED    = "banking.policy.created";

    /** 보험 계약 갱신/변경 이벤트 */
    public static final String POLICY_UPDATED    = "banking.policy.updated";

    // ===== Claim (보험금 청구) =====
    /** 보험금 청구 접수 이벤트 */
    public static final String CLAIM_SUBMITTED   = "banking.claim.submitted";
}
