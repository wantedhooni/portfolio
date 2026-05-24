package revy.com.client.kafka.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 프로듀서 팩토리 및 KafkaTemplate 빈을 등록합니다.
 *
 * <ul>
 *   <li>Key / Value 직렬화: StringSerializer (페이로드는 JSON 문자열로 변환 후 전달)</li>
 *   <li>멱등성 프로듀서(enable.idempotence) 활성화로 exactly-once 전송 보장</li>
 * </ul>
 */
@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, String> kafkaProducerFactory(KafkaProperties properties) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, properties.producer().acks());
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, properties.producer().batchSize());
        config.put(ProducerConfig.LINGER_MS_CONFIG, properties.producer().lingerMs());
        // 멱등성 프로듀서: enable.idempotence=true 가 retries=Integer.MAX_VALUE 를 자동 보장하므로
        // retries 를 별도 설정하지 않습니다. 함께 설정하면 작은 값이 우선되어 메시지 유실 위험이 있습니다.
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> kafkaProducerFactory) {
        return new KafkaTemplate<>(kafkaProducerFactory);
    }
}
