# spring_elk

Spring 애플리케이션 로그를 ELK로 적재하는 방식을 비교하는 멀티 모듈 샘플입니다.

## 모듈 구성

- `spring_elk_es`: 애플리케이션에서 Elasticsearch로 직접 전송
- `spring_elk_logstash`: 애플리케이션에서 Logstash TCP로 전송 후 Elasticsearch 적재

## 공통 인프라

- Elasticsearch: `http://localhost:9200/`
- Kibana: `http://localhost:5601/`
- Logstash Monitoring: `http://localhost:9600/`

## 구현 포인트

- `logback-spring.xml` 중심의 로그 적재 실험
- 파일 로그와 외부 적재를 동시에 구성
- `spring_elk_es`는 `ElasticsearchAppender` 사용
- `spring_elk_logstash`는 `LogstashTcpSocketAppender` + `AsyncAppender` 사용

## 실행 메모

- 각 서브모듈에서 개별 실행
- `spring_elk_es/docker-compose.yaml` 또는 `spring_elk_logstash/docker-compose.yaml`로 로컬 인프라 기동

## 현재 상태

- 이 모듈은 비즈니스 API보다 로깅 파이프라인 설정이 핵심입니다.
- H2/Swagger 설정 흔적은 남아 있지만 실제 목적은 로그 전송 검증입니다.

## 참고

- Spring Boot structured logging / ECS 문서를 함께 보면 방향 잡기 좋습니다.
