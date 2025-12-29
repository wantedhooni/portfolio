package com.revy.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@SpringBootApplication
public class ElkApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElkApplication.class, args);
    }
}
