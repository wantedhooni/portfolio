package revy.com.client.kafka.producer.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import revy.com.client.kafka.dto.KafkaEventMessage;
import revy.com.client.kafka.exception.KafkaClientException;
import revy.com.client.kafka.producer.KafkaEventProducer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * {@link KafkaTemplate} 기반의 Kafka 이벤트 프로듀서 구현체입니다.
 *
 * <p>페이로드 직렬화: Spring 관리 Jackson ObjectMapper → JSON 문자열 → StringSerializer</p>
 */
@Slf4j
@Component
public class KafkaEventProducerImpl implements KafkaEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * @param objectMapper Spring이 자동 구성하는 ObjectMapper 빈을 주입받습니다.
     *                     직접 생성하지 않음으로써 Boot의 날짜 직렬화·네이밍 전략 등 전역 설정을 공유합니다.
     */
    public KafkaEventProducerImpl(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper  = objectMapper;
    }

    /**
     * 동기 전송 — 내부적으로 비동기 전송 후 결과를 블로킹으로 대기합니다.
     * 전송 실패 시 {@link KafkaClientException} 으로 래핑하여 throw 합니다.
     */
    @Override
    public <T> void send(String topic, String key, KafkaEventMessage<T> message) {
        try {
            String json = serialize(message);
            kafkaTemplate.send(topic, key, json).get();
            log.info("[Kafka][SEND] topic={}, key={}, eventId={}", topic, key, message.eventId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KafkaClientException(topic, key, "Kafka 전송 중 인터럽트 발생", e);
        } catch (ExecutionException e) {
            throw new KafkaClientException(topic, key, "Kafka 전송 실패: " + e.getCause().getMessage(), e.getCause());
        }
    }

    /**
     * 비동기 전송 — 반환된 CompletableFuture 에서 성공/실패를 처리합니다.
     */
    @Override
    public <T> CompletableFuture<SendResult<String, String>> sendAsync(String topic, String key, KafkaEventMessage<T> message) {
        String json = serialize(message);
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, json);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("[Kafka][SEND-ASYNC-FAIL] topic={}, key={}, eventId={}, error={}",
                    topic, key, message.eventId(), ex.getMessage());
            } else {
                log.info("[Kafka][SEND-ASYNC] topic={}, key={}, eventId={}, partition={}, offset={}",
                    topic, key, message.eventId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            }
        });

        return future;
    }

    // ===== private =====

    private <T> String serialize(KafkaEventMessage<T> message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new KafkaClientException("unknown", "unknown", "메시지 직렬화 실패: " + e.getMessage(), e);
        }
    }
}
