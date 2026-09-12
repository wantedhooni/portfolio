# Spring Batch + Quartz 클러스터 샘플

Spring Batch 작업을 Quartz로 스케줄링하고, 실행 결과를 모니터링하는 샘플입니다.

## 구현 범위

- JDBC JobStore 기반 Quartz 설정
- Quartz 클러스터 모드 설정
- Spring Batch 잡/스텝 구성
- 재시도와 백오프 정책
- 잡/스텝 실행 이력 조회 API
- Prometheus 메트릭 노출
- 실패 시 다중 채널 알림 인터페이스
- Flyway로 Batch / Quartz 메타데이터 스키마 관리

## Quartz 클러스터링 구조

- `spring.quartz.job-store-type: jdbc`
- `org.quartz.scheduler.instanceId: AUTO`
- `org.quartz.jobStore.isClustered: true`
- Quartz 메타데이터는 `QRTZ_*` 테이블에 저장

## 배치 흐름

- `sampleJob` 안에 `sampleStep` 1개를 구성
- `ListItemReader`가 `alpha`, `retry`, `beta`, `gamma`를 읽음
- `retry` 처리 시 재시도 정책과 백오프를 적용
- `BatchJobListener`, `BatchStepListener`로 실행 결과를 기록

## 모니터링 API

- `GET /api/batch/jobs/{jobName}/executions`
- `GET /api/batch/executions/{executionId}/steps`
- `GET /api/quartz/triggers`

## 실행 정보

- 실행: `./gradlew bootRun`
- Prometheus: `http://localhost:8080/actuator/prometheus`
- Actuator exposure: `health`, `info`, `prometheus`, `quartz`
- DB: `jdbc:mariadb://localhost:23306/batch_quartz`

## 스키마

- Batch: `src/main/resources/db/migration/V20260104005745__batch_schema.sql`
- Quartz: `src/main/resources/db/migration/V20260104005800__quartz_schema.sql`

## 메모

- `AlertNotifier`가 `Slack`, `Email`, `Webhook` 구현체를 한 번에 호출하는 구조입니다.
- README 기준보다는 코드가 더 정확하며, 현재 Quartz trigger 설정은 cron 설명보다 단순 스케줄에 가깝습니다.
