package com.revy.example.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler defaultErrorHandler(
            KafkaOperations<Object, Object> kafkaOperations
    ) {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaOperations,
                        (ConsumerRecord<?, ?> record, Exception exception) ->
                                new TopicPartition(
                                        record.topic() + ".DLT",
                                        record.partition()
                                )
                );

        // interval: 1초, maxAttempts: 3회 재시도
        FixedBackOff backOff = new FixedBackOff(1000L, 3L);

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(recoverer, backOff);

        // DLT 발행 성공 후 해당 offset commit
        errorHandler.setCommitRecovered(true);

        return errorHandler;
    }
}