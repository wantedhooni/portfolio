package com.revy.example.kafka;

import com.revy.example.kafka.dto.SimpleMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaConsumer {
    @KafkaListener(topics = "sample.topic", groupId = "sample-group")
    public void listen(SimpleMessage message) {
        log.info("Consumed: {}", message);
    }
}
