package revy.com.client.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import revy.com.client.kafka.dto.KafkaEventMessage;
import revy.com.client.kafka.dto.KafkaTopic;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * PaymentEventConsumer 의 역직렬화, ACK 호출 여부, 예외 처리를 검증하는 단위 테스트입니다.
 *
 * <p>외부 Kafka 브로커 없이 ConsumerRecord 와 Acknowledgment 를 직접 주입하여 동작을 검증합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("결제 이벤트 컨슈머")
class PaymentEventConsumerTest {

    @Mock
    Acknowledgment ack;

    ObjectMapper objectMapper;
    PaymentEventConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        consumer = new PaymentEventConsumer(objectMapper);
    }

    // ===== PAYMENT_COMPLETED =====

    @Test
    @DisplayName("결제 성공 이벤트를 정상 처리하면 ACK 가 호출된다")
    void onPaymentCompleted_success_ackCalled() throws Exception {
        // given
        String json = toJson(KafkaEventMessage.of("payment.completed", "policyId=42, amount=50000"));
        ConsumerRecord<String, String> record = record(KafkaTopic.PAYMENT_COMPLETED, "policy-42", json);

        // when
        consumer.onPaymentCompleted(record, ack);

        // then
        verify(ack).acknowledge();
    }

    // ===== PAYMENT_FAILED =====

    @Test
    @DisplayName("결제 실패 이벤트를 정상 처리하면 ACK 가 호출된다")
    void onPaymentFailed_success_ackCalled() throws Exception {
        // given
        String json = toJson(KafkaEventMessage.of("payment.failed", "policyId=99, reason=insufficient_balance"));
        ConsumerRecord<String, String> record = record(KafkaTopic.PAYMENT_FAILED, "policy-99", json);

        // when
        consumer.onPaymentFailed(record, ack);

        // then
        verify(ack).acknowledge();
    }

    // ===== 예외 처리 =====

    @Test
    @DisplayName("역직렬화 실패(깨진 JSON) 시 ACK 를 호출하지 않아 재처리 대기 상태가 된다")
    void onPaymentCompleted_invalidJson_ackNotCalled() {
        // given: 깨진 JSON
        ConsumerRecord<String, String> record = record(KafkaTopic.PAYMENT_COMPLETED, "policy-1", "{invalid-json}");

        // when
        consumer.onPaymentCompleted(record, ack);

        // then: 오프셋 미커밋 → 재처리 대기
        verify(ack, never()).acknowledge();
    }

    @Test
    @DisplayName("결제 실패 이벤트에서 예외 발생 시 ACK 를 호출하지 않는다")
    void onPaymentFailed_exception_ackNotCalled() {
        // given: null 바디
        ConsumerRecord<String, String> record = record(KafkaTopic.PAYMENT_FAILED, "policy-99", null);

        // when
        consumer.onPaymentFailed(record, ack);

        // then
        verify(ack, never()).acknowledge();
    }

    // ===== helper =====

    private ConsumerRecord<String, String> record(String topic, String key, String value) {
        return new ConsumerRecord<>(topic, 0, 0L, key, value);
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
