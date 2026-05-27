# 작업 내역

## 2026-05-26

- `MetaController`의 설정값 주입 애노테이션을 Lombok `Value`가 아닌 Spring `Value`로 수정했다.
- 명시 생성자와 함께 불필요하게 생성될 수 있는 Lombok `RequiredArgsConstructor`를 제거했다.
- `MetaExposed.value()`가 비어 있으면 enum 클래스명을 카멜케이스 메타 키로 사용하는 기본 동작을 복원했다.
- 메타 enum 조회 컨트롤러와 메서드에 한글 doc 주석을 추가했다.
- `./gradlew :application:api-admin:compileJava`로 컴파일 성공을 확인했다.

## 2026-05-24

- `frankfurter-client` 모듈 테스트 실행 시 SLF4J 로그가 콘솔에 보이도록 Gradle `testLogging.showStandardStreams`를 활성화했다.
- `FrankfurterRestClientLiveTest`의 Lombok 로그 애노테이션을 제거하고 SLF4J `LoggerFactory`로 logger를 명시 생성하도록 변경했다.
- `FrankfurterRestClientTest`에 `MockRestServiceServer` 기반 테스트를 작성했다.
- 단건 환율, 최신 환율 목록, 지원 통화 목록 응답 매핑을 검증했다.
- Frankfurter API 4xx 오류 응답이 `FrankfurterApiException`으로 변환되는 경로를 검증했다.
- `FrankfurterRestClientLiveTest`를 추가해 실제 `https://api.frankfurter.dev` 서버의 단건 환율, 최신 환율 목록, 지원 통화 목록을 호출하도록 했다.
- Frankfurter v2 실서버 응답이 배열 형태로 내려오는 `rates`, `currencies` 계약에 맞춰 클라이언트 반환 타입을 보정했다.

## 2026-05-23

- `QuartzJobExecutionHistoryReaderImpl`의 작업별/상태별 실행 이력 조회를 QueryDSL constructor projection으로 구현했다.
- `QuartzJobExecutionHistoryReader`에 projection 페이지 조회 메서드를 추가했다.
- 기존 미정의 `repository`, `PageUtils` 의존 코드를 제거하고 count 쿼리 기반 페이징으로 정리했다.
