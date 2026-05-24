package revy.com.client.kafka.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * kafka-client 모듈 최상위 설정 클래스입니다.
 * KafkaProperties 를 스프링 컨텍스트에 등록합니다.
 */
@Configuration
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaModuleConfig {
}
