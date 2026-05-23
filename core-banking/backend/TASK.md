# 작업 내역

## 2026-05-23

- `QuartzJobExecutionHistoryReaderImpl`의 작업별/상태별 실행 이력 조회를 QueryDSL constructor projection으로 구현했다.
- `QuartzJobExecutionHistoryReader`에 projection 페이지 조회 메서드를 추가했다.
- 기존 미정의 `repository`, `PageUtils` 의존 코드를 제거하고 count 쿼리 기반 페이징으로 정리했다.
