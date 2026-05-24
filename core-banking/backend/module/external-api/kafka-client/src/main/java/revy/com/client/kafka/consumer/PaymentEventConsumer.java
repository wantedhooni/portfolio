package revy.com.client.kafka.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import revy.com.client.kafka.dto.KafkaEventMessage;
import revy.com.client.kafka.dto.KafkaTopic;

/**
 * {@code banking.payment.completed} / {@code banking.payment.failed} 토픽 컨슈머 샘플입니다.
 *
 * <h3>처리 흐름</h3>
 * <pre>
 *  메시지 수신
 *    → JSON 역직렬화 (KafkaEventMessage)
 *    → 비즈니스 로직 위임
 *    → ACK (MANUAL_IMMEDIATE)
 *    → 실패 시 ACK 미호출 → 재처리 대기
 * </pre>
 *
 * <h3>AckMode</h3>
 * {@link revy.com.client.kafka.config.KafkaConsumerConfig} 에서
 * {@code MANUAL_IMMEDIATE} 로 설정되어 있으므로 반드시 {@code acknowledgment.acknowledge()} 를 직접 호출해야 합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final ObjectMapper objectMapper;

    // ===== Listeners =====

    /**
     * 결제 성공 이벤트를 처리합니다.
     */
    @KafkaListener(
        topics = KafkaTopic.PAYMENT_COMPLETED,
        groupId = "${kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentCompleted(ConsumerRecord<String, String> record, Acknowledgment ack) {
        process(record, ack, "PAYMENT_COMPLETED");
    }

    /**
     * 결제 실패 이벤트를 처리합니다.
     */
    @KafkaListener(
        topics = KafkaTopic.PAYMENT_FAILED,
        groupId = "${kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentFailed(ConsumerRecord<String, String> record, Acknowledgment ack) {
        process(record, ack, "PAYMENT_FAILED");
    }

    // ===== private =====

    /**
     * 공통 처리 템플릿: 역직렬화 → 핸들러 위임 → ACK
     *
     * <p>예외 발생 시 ACK 를 호출하지 않으므로 컨슈머 그룹 내에서 재처리됩니다.</p>
     */
    private void process(ConsumerRecord<String, String> record, Acknowledgment ack, String eventLabel) {
        log.info("[Kafka][CONSUME][{}] topic={}, partition={}, offset={}, key={}",
            eventLabel,
            record.topic(),
            record.partition(),
            record.offset(),
            record.key()
        );

        try {
            KafkaEventMessage<Object> message = deserialize(record.value());

            log.info("[Kafka][CONSUME][{}] eventId={}, occurredAt={}, payload={}",
                eventLabel,
                message.eventId(),
                message.occurredAt(),
                message.payload()
            );

            handlePaymentEvent(eventLabel, message);

            ack.acknowledge();
            log.debug("[Kafka][ACK][{}] eventId={}", eventLabel, message.eventId());

        } catch (Exception e) {
            // ACK 호출 안 함 → 오프셋 미커밋 → 재처리 대기
            log.error("[Kafka][CONSUME-FAIL][{}] topic={}, key={}, error={}",
                eventLabel, record.topic(), record.key(), e.getMessage(), e);
        }
    }

    /**
     * 수신한 결제 이벤트를 실제 처리합니다.
     *
     * <p>이 모듈은 인프라 클라이언트이므로 비즈니스 로직은 business-logic 모듈로 위임하는 방식으로 확장합니다.</p>
     */
    private void handlePaymentEvent(String eventLabel, KafkaEventMessage<Object> message) {
        // TODO: business-logic 모듈의 PaymentService 등으로 위임
        log.info("[Kafka][HANDLE][{}] eventId={} — 처리 완료", eventLabel, message.eventId());
    }

    private KafkaEventMessage<Object> deserialize(String json) throws Exception {
        return objectMapper.readValue(json, new TypeReference<>() {});
    }
}
