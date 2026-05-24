package revy.com.client.kafka.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import revy.com.client.kafka.dto.KafkaEventMessage;
import revy.com.client.kafka.dto.KafkaTopic;
import revy.com.client.kafka.exception.KafkaClientException;
import revy.com.client.kafka.producer.impl.KafkaEventProducerImpl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * KafkaEventProducer 의 직렬화, 동기·비동기 전송, 예외 변환을 검증하는 단위 테스트입니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Kafka 이벤트 프로듀서")
class KafkaEventProducerTest {

    @Mock
    KafkaTemplate<String, String> kafkaTemplate;

    KafkaEventProducer producer;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        producer = new KafkaEventProducerImpl(kafkaTemplate, objectMapper);
    }

    // ===== 동기 전송 =====

    @Test
    @DisplayName("동기 전송 성공 시 KafkaTemplate.send() 가 올바른 토픽과 키로 호출된다")
    void send_success() {
        // given
        SendResult<String, String> sendResult = makeSendResult(KafkaTopic.PAYMENT_COMPLETED, 0, 42L);
        when(kafkaTemplate.send(eq(KafkaTopic.PAYMENT_COMPLETED), eq("policy-1"), anyString()))
            .thenReturn(CompletableFuture.completedFuture(sendResult));

        KafkaEventMessage<String> message = KafkaEventMessage.of("payment.completed", "pay-ok");

        // when
        producer.send(KafkaTopic.PAYMENT_COMPLETED, "policy-1", message);

        // then
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq(KafkaTopic.PAYMENT_COMPLETED), eq("policy-1"), jsonCaptor.capture());
        String json = jsonCaptor.getValue();
        assertThat(json).contains("payment.completed");
        assertThat(json).contains("pay-ok");
        assertThat(json).contains(message.eventId());
    }

    @Test
    @DisplayName("동기 전송 실패 시 KafkaClientException 으로 래핑되어 throw 된다")
    void send_failure_wrapsException() {
        // given
        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("broker unavailable"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
            .thenReturn(failedFuture);

        KafkaEventMessage<String> message = KafkaEventMessage.of("payment.completed", "pay-fail");

        // when & then
        assertThatThrownBy(() -> producer.send(KafkaTopic.PAYMENT_COMPLETED, "policy-1", message))
            .isInstanceOf(KafkaClientException.class)
            .satisfies(ex -> {
                KafkaClientException kce = (KafkaClientException) ex;
                assertThat(kce.topic()).isEqualTo(KafkaTopic.PAYMENT_COMPLETED);
                assertThat(kce.key()).isEqualTo("policy-1");
            });
    }

    // ===== 비동기 전송 =====

    @Test
    @DisplayName("비동기 전송 시 CompletableFuture 가 반환되고 전송이 정상 완료된다")
    void sendAsync_success() throws ExecutionException, InterruptedException {
        // given
        SendResult<String, String> sendResult = makeSendResult(KafkaTopic.SETTLEMENT_RESULT, 0, 7L);
        when(kafkaTemplate.send(eq(KafkaTopic.SETTLEMENT_RESULT), eq("batch-1"), anyString()))
            .thenReturn(CompletableFuture.completedFuture(sendResult));

        KafkaEventMessage<String> message = KafkaEventMessage.of("settlement.result", "batch-ok");

        // when
        CompletableFuture<SendResult<String, String>> future =
            producer.sendAsync(KafkaTopic.SETTLEMENT_RESULT, "batch-1", message);

        // then
        assertThat(future).isCompleted();
        assertThat(future.get().getRecordMetadata().offset()).isEqualTo(7L);
    }

    @Test
    @DisplayName("비동기 전송 실패 시 Future 가 예외 상태로 완료된다")
    void sendAsync_failure_futureCompletedExceptionally() {
        // given
        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("timeout"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
            .thenReturn(failedFuture);

        KafkaEventMessage<String> message = KafkaEventMessage.of("settlement.result", "fail-payload");

        // when
        CompletableFuture<SendResult<String, String>> future =
            producer.sendAsync(KafkaTopic.SETTLEMENT_RESULT, "batch-2", message);

        // then
        assertThat(future).isCompletedExceptionally();
    }

    // ===== 직렬화 검증 =====

    @Test
    @DisplayName("메시지 래퍼는 eventId, eventType, occurredAt, payload 를 JSON 으로 직렬화한다")
    void send_messageSerializedWithAllFields() {
        // given
        SendResult<String, String> sendResult = makeSendResult(KafkaTopic.POLICY_CREATED, 1, 10L);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(anyString(), anyString(), jsonCaptor.capture()))
            .thenReturn(CompletableFuture.completedFuture(sendResult));

        KafkaEventMessage<String> message = KafkaEventMessage.of("policy.created", "policy-data-123");

        // when
        producer.send(KafkaTopic.POLICY_CREATED, "policy-99", message);

        // then
        String json = jsonCaptor.getValue();
        assertThat(json).contains("\"eventId\"");
        assertThat(json).contains("\"eventType\"");
        assertThat(json).contains("\"occurredAt\"");
        assertThat(json).contains("\"payload\"");
        assertThat(json).contains("policy.created");
        assertThat(json).contains("policy-data-123");
    }

    // ===== helper =====

    private SendResult<String, String> makeSendResult(String topic, int partition, long offset) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, "key", "value");
        RecordMetadata metadata = new RecordMetadata(
            new TopicPartition(topic, partition), offset, 0, 0L, 0, 0);
        return new SendResult<>(record, metadata);
    }
}
