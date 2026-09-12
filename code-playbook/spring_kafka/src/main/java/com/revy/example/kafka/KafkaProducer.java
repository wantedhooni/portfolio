package com.revy.example.kafka;

import com.revy.example.kafka.dto.SimpleMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer {
    private static final String TOPIC = "sample.topic";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(SimpleMessage message) {
        log.info("send message: {}", message);
        // key를 id로 사용 (파티션 일관성)
        kafkaTemplate.send(TOPIC, message.id(), message);
    }
}
