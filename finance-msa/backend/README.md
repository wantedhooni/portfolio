# financial-msa-source

금융 MSA Java 백엔드 MVP 소스 트리입니다.

## 포함 범위

- Gradle 멀티모듈
- common-core / common-web / common-security / common-jpa / common-kafka / common-outbox
- gateway-service
- auth-service
- account-service
- risk-service
- trading-service
- securities / credit / insurance / pension / settlement / notification 서비스 부트스트랩
- docker-compose 로컬 인프라
- 기본 DDL

## 실행 순서

```bash
docker compose up -d

./gradlew :auth-service:bootRun
./gradlew :account-service:bootRun
./gradlew :risk-service:bootRun
./gradlew :trading-service:bootRun
./gradlew :gateway-service:bootRun
```

## 주의

- Refresh Token 저장소, Flyway 마이그레이션, DLQ, mTLS, OpenAPI, 상세 정산/여신/보험/연금 도메인은 MVP 이후 확장 대상으로 남겨두었습니다.
- Java 25 toolchain 기준입니다.
