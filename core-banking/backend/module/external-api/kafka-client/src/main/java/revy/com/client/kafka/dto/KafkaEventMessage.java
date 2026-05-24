package revy.com.client.kafka.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka 메시지 공통 래퍼입니다.
 *
 * <p>모든 도메인 이벤트를 동일한 봉투 구조로 감싸 전송합니다.
 * {@code payload} 는 각 토픽에서 사용하는 도메인 DTO 입니다.</p>
 *
 * @param <T> 도메인 페이로드 타입
 */
public record KafkaEventMessage<T>(
    /** 메시지 고유 식별자 (UUID) — 중복 처리 방지용 */
    String eventId,

    /** 이벤트 유형 이름 (e.g. "payment.completed") */
    String eventType,

    /** 이벤트 발생 일시 */
    LocalDateTime occurredAt,

    /** 도메인 페이로드 */
    T payload
) {

    /**
     * 현재 시각과 랜덤 UUID 를 자동 설정하는 팩토리 메서드입니다.
     */
    public static <T> KafkaEventMessage<T> of(String eventType, T payload) {
        return new KafkaEventMessage<>(
            UUID.randomUUID().toString(),
            eventType,
            LocalDateTime.now(),
            payload
        );
    }
}
