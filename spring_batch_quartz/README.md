# Spring Batch + Quartz 클러스터 샘플

## 

## Quartz 클러스터링 구조
- `spring.quartz.job-store-type: jdbc` 기반으로 Quartz 메타데이터를 DB에 저장합니다.
- `org.quartz.scheduler.instanceId: AUTO` 설정으로 각 노드가 자동으로 인스턴스를 구분합니다.
- `org.quartz.jobStore.isClustered: true` 설정으로 클러스터 노드가 동일한 스케줄을 공유합니다.
- `QRTZ_*` 테이블을 통해 트리거 상태, 잠금, 실행 이력을 공유합니다.

## 모니터링 / Grafana 설정
- Actuator + Prometheus 메트릭을 노출합니다.
  - Prometheus 스크래핑 URL: `http://localhost:8080/actuator/prometheus`
- Grafana 샘플 대시보드 JSON은 아래 경로에 포함되어 있습니다.
  - `src/main/resources/monitoring/grafana-dashboard.json`
- 배치/스텝 실행 카운트 및 실행 시간 메트릭을 제공합니다.

### REST API
- 배치 잡 실행 이력
  - `GET /api/batch/jobs/{jobName}/executions`
- 스텝 상세 실행 정보
  - `GET /api/batch/executions/{executionId}/steps`
- Quartz 트리거 상태
  - `GET /api/quartz/triggers`

## Retry / Alert 패턴 설명
- 배치 스텝은 `faultTolerant()` 설정으로 재시도를 수행합니다.
- 재시도 횟수 및 백오프 전략은 `application.yml`의 `app.batch.retry` 설정을 사용합니다.
- 잡 실패 시 `JobExecutionListener`가 실패 이벤트를 감지합니다.
- `AlertService` 인터페이스를 통해 이메일/슬랙/웹훅 알림 예시를 제공합니다.

## 스키마/마이그레이션
- 배치 메타데이터: `src/main/resources/db/migration/V20260104005745__batch_schema.sql`
- 쿼츠 메타데이터: `src/main/resources/db/migration/V20260104005800__quartz_schema.sql`

## 최신 트랜드 적용 포인트
- Spring Boot 3.x + Micrometer 기반 메트릭 수집
- Quartz 클러스터 모드와 배치 잡 오케스트레이션 분리
- REST 기반 모니터링 API 제공

## note
mac timestamp
````
date "+%Y%m%d%H%M%S"
```