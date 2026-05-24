# 작업 계획

## 2026-05-24 Frankfurter 테스트 로그 출력 설정

- `frankfurter-client` 모듈 테스트 실행 시 SLF4J 로그가 콘솔에 출력되도록 Gradle `testLogging`을 설정한다.
- 테스트 실행으로 `Supported currencies` 로그가 출력되는지 확인한다.
- 작업 내역을 `TASK.md`와 `README.md`에 반영한다.

## 2026-05-24 Frankfurter 실서버 테스트 로거 정리

- `FrankfurterRestClientLiveTest`에서 Lombok 로그 애노테이션을 제거한다.
- 테스트 클래스 내부에 SLF4J `LoggerFactory` 기반 logger를 명시적으로 생성한다.
- 관련 작업 내역을 `TASK.md`와 `README.md`에 반영하고 테스트 컴파일을 확인한다.

## 2026-05-24 Frankfurter REST Client 테스트 작성

- `FrankfurterRestClientImpl`의 단건 환율, 최신 환율 목록, 통화 목록 응답 매핑을 `MockRestServiceServer` 기반 단위 테스트로 검증한다.
- Frankfurter API 오류 응답이 `FrankfurterApiException`으로 변환되는지 확인한다.
- 실제 Frankfurter v2 서버 응답 계약에 맞춘 실서버 연동 테스트를 추가한다.
- 테스트 실행 결과를 확인하고 README/TASK에 작업 내역을 남긴다.

## 2026-05-23 QueryDSL Projection 조회 완성

- `QuartzJobExecutionHistoryReaderImpl`의 미완성 repository/PageUtils 기반 코드를 QueryDSL projection 기반으로 변경한다.
- 조회 계약 인터페이스에 실행 이력 페이지 조회 메서드를 명시한다.
- 빌드로 컴파일 가능 여부를 확인한다.
