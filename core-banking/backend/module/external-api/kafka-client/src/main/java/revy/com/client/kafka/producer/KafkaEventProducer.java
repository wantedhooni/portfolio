package revy.com.client.kafka.producer;

import org.springframework.kafka.support.SendResult;
import revy.com.client.kafka.dto.KafkaEventMessage;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka 이벤트 전송 클라이언트 계약입니다.
 *
 * <ul>
 *   <li>{@link #send} — 동기 전송, 실패 시 {@code KafkaClientException} throw</li>
 *   <li>{@link #sendAsync} — 비동기 전송, 콜백/체이닝으로 결과 처리</li>
 * </ul>
 */
public interface KafkaEventProducer {

    /**
     * 지정 토픽에 이벤트 메시지를 동기적으로 전송합니다.
     *
     * @param topic   대상 토픽 ({@link revy.com.client.kafka.dto.KafkaTopic} 상수 사용 권장)
     * @param key     파티션 라우팅 키 (e.g. policyId, accountId)
     * @param message 전송할 이벤트 래퍼
     * @param <T>     페이로드 타입
     */
    <T> void send(String topic, String key, KafkaEventMessage<T> message);

    /**
     * 지정 토픽에 이벤트 메시지를 비동기적으로 전송합니다.
     *
     * @return 전송 결과를 담은 {@link CompletableFuture}
     */
    <T> CompletableFuture<SendResult<String, String>> sendAsync(String topic, String key, KafkaEventMessage<T> message);
}
