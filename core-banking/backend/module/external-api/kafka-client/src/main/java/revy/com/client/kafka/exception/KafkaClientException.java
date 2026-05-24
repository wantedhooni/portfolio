package revy.com.client.kafka.exception;

/**
 * Kafka 메시지 전송/처리 중 발생하는 클라이언트 예외입니다.
 */
public class KafkaClientException extends RuntimeException {

    private final String topic;
    private final String key;

    public KafkaClientException(String topic, String key, String message, Throwable cause) {
        super(message, cause);
        this.topic = topic;
        this.key   = key;
    }

    /** 전송 대상 토픽 */
    public String topic() { return topic; }

    /** 메시지 키 */
    public String key()   { return key; }
}
