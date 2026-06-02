package com.revy.example.kafka.consumer;

import com.revy.example.service.OrderCreatedPayload;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OrderEventConsumer {

    private final ObjectMapper objectMapper;

    public OrderEventConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "outbox.event.order", groupId = "outbox-demo-consumer")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("Receive order event from topic {}", record.topic());
        log.info("Record: key={}, value={}, partition={}, offset={}",
                record.key(), record.value(), record.partition(), record.offset());

        String eventType = resolveEventType(record);

        if (!"OrderCreated".equals(eventType)) {
            throw new IllegalArgumentException("Unsupported eventType: " + eventType);
        }

        OrderCreatedPayload payload = parse(record.value());

        handleOrderCreated(payload);

        // 비즈니스 처리 성공 후에만 ack
        ack.acknowledge();
    }

    private void handleOrderCreated(OrderCreatedPayload payload) {
        log.info("payload = {}", payload);

        // 테스트용 강제 실패 예시
        // if (payload.productName().contains("FAIL")) {
        //     throw new IllegalStateException("Forced failure");
        // }

        // 실제 처리:
        // - projection update
        // - notification
        // - cache invalidation
        // - 외부 API 호출 등
    }

    private OrderCreatedPayload parse(String value) {
        return objectMapper.readValue(value, OrderCreatedPayload.class);
    }

    private String resolveEventType(ConsumerRecord<String, String> record) {
        var header = record.headers().lastHeader("eventType");

        if (header == null) {
            return null;
        }

        return new String(header.value(), StandardCharsets.UTF_8);
    }
}