# spring_kafka

Kafka producer/consumer 최소 예제를 확인하는 샘플입니다.

## 구현 범위

- Docker Compose 기반 단일 Kafka 브로커
- Kafka UI 포함
- REST 요청으로 메시지 발행
- `@KafkaListener`로 메시지 소비

## 주요 API

- `POST /publish?msg=hello`

## 동작 방식

- `PublishController`가 `SimpleMessage`를 생성해 `sample.topic`으로 발행합니다.
- `KafkaConsumer`가 동일 토픽을 `sample-group`으로 구독합니다.

## 실행 정보

- 실행: `./gradlew bootRun`
- Kafka UI: `http://localhost:8090/`
- Kafka broker: `localhost:9092`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/api-docs`
- H2 Console: `http://localhost:8080/h2-console`

## 메모

- `spring.docker.compose` 설정이 있어 애플리케이션 실행 시 로컬 Kafka 구성을 함께 띄우는 용도로 맞춰져 있습니다.
- DB 설정도 들어가 있지만 핵심 관심사는 메시지 발행/소비 흐름입니다.
