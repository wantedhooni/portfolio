package com.revy.example.api;


import com.revy.example.kafka.KafkaProducer;
import com.revy.example.kafka.dto.SimpleMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PublishController {
    private final KafkaProducer kafkaProducer;

    @PostMapping("/publish")
    public String publish(@RequestParam String msg) {
        kafkaProducer.send(new SimpleMessage(UUID.randomUUID().toString(), msg));
        return "published";
    }
}
