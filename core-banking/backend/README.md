# Core Banking Backend

코어뱅킹 백엔드 작업공간입니다.

## 최근 변경

- `frankfurter-client` 테스트 실행 시 테스트 로그와 성공/실패 이벤트가 콘솔에 출력됩니다.
- Frankfurter 실서버 연동 테스트는 Lombok 로그 애노테이션 대신 SLF4J `LoggerFactory`로 logger를 명시 생성합니다.
- Frankfurter 실서버 연동 테스트를 추가해 `https://api.frankfurter.dev`의 v2 환율/통화 목록 응답을 직접 검증할 수 있습니다.
- Frankfurter 외부 환율 REST 클라이언트 테스트를 추가해 URI 생성, 응답 매핑, 오류 예외 변환을 검증했습니다.
- Quartz 작업 실행 이력 조회 Reader에 QueryDSL projection 기반 페이지 조회를 적용했습니다.
