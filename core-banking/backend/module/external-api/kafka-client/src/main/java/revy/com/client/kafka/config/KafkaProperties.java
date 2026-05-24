package revy.com.client.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * kafka-client 모듈의 설정 프로퍼티입니다.
 * kafka.yml 의 'kafka' 접두사와 바인딩됩니다.
 */
@Validated
@ConfigurationProperties(prefix = "kafka")
public record KafkaProperties(

    /** 카프카 부트스트랩 서버 주소 목록 (e.g. localhost:9092) */
    List<String> bootstrapServers,

    Producer producer,
    Consumer consumer

) {

    public record Producer(
        /** 프로듀서 ACK 레벨 (all, 1, 0) */
        String acks,

        /** 배치 전송 크기 (bytes) */
        int batchSize,

        /** 배치 전송 대기 시간 (ms) — 처리량 vs 지연 트레이드오프 */
        int lingerMs
    ) {}

    public record Consumer(
        /** 컨슈머 그룹 ID */
        String groupId,

        /** 오프셋 초기 정책 (earliest / latest) */
        String autoOffsetReset,

        /** 자동 커밋 여부 (수동 ACK 사용 시 false) */
        boolean enableAutoCommit,

        /** 동시 컨슈머 스레드 수 */
        int concurrency
    ) {}
}
