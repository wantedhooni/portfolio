# 작업 기록

## 2026-05-20
- 루트 작업 지침 문서 `AGENTS.md`를 한국어로 작성했다.
- 포트폴리오 문서 작성 계획을 `PLANS.md`에 정리했다.
- 백엔드 컨트롤러, 멀티모듈 설정, 프론트엔드 라우트와 서비스 구성을 확인했다.
- Swagger UI와 프론트엔드 로그인 화면을 캡처하여 `docs/images/`에 저장했다.
- 개인 포트폴리오용 문서 `PORTFOLIO.md`를 작성했다.
- 프로젝트 루트 실행 스크립트 `script/all-start.sh`, `script/all-stop.sh`, `script/all-restart.sh`를 추가했다.
- 루트 `README.md`에 포트폴리오 문서 링크와 실행 스크립트 안내를 추가했다.
- `PORTFOLIO.md`에 백엔드/프론트엔드/데이터베이스/캐시/로그/메트릭을 포함한 인프라 구성도를 추가했다.
- `business-logic`의 주요 상태 변경 Command 구현부에 commit 이후 이벤트 발행 필요 지점을 `TODO:REVY` 주석으로 표시했다.

## 2026-05-22
- `backend/settings.gradle`, 애플리케이션 설정, 컨트롤러 매핑, 도메인 엔티티, 프론트엔드 라우트와 package script를 기준으로 프로젝트를 재분석했다.
- README의 `api-saas` 포트, Swagger URL, 매매 API 경로, 도메인/모듈 설명을 현재 구현에 맞게 수정했다.
- README에 실행 정보, 운영 스택, 도메인 분석, 구현 완성도와 남은 개선 포인트를 추가했다.
- PORTFOLIO 문서에 주문, 청구, 정산, RBAC, 스케줄러까지 포함한 최신 분석 요약과 포트폴리오 강조 포인트를 보강했다.
- `core/common`은 공통 응답 모듈이 아니라 공통 utils 모듈이므로 README/PORTFOLIO의 모듈 설명을 정정했다.
- `backend/**/build.gradle`의 실제 `project(...)` 의존성을 기준으로 모듈 계층을 재분석하고 README/PORTFOLIO 모듈 다이어그램과 의존성 표를 갱신했다.
- Grafana datasource와 dashboard provisioning 파일을 추가해 컨테이너 기동 시 `Core Banking Operations` 대시보드가 자동 등록되도록 했다.
- 기본 대시보드에 HTTP 처리량, 5xx 비율, 응답시간 p95/평균, JVM heap/메모리 풀/스레드/GC, CPU, HikariCP 커넥션, 로그 이벤트, Pushgateway 수집 지연 패널을 구성했다.
- Prometheus Pushgateway scrape에 `honor_labels: true`를 적용해 애플리케이션 `job`, `application`, `instance` 라벨이 Grafana 필터에서 유지되도록 했다.
- 실행 중인 `web-admin`, `web-saas`에 로그인해 운영자 계좌/종목 관리와 사용자 워크스페이스 대표 화면을 캡처하고 README/PORTFOLIO 스크린샷 섹션에 추가했다.
- `PORTFOLIO.md`의 프론트엔드 스크린샷 섹션을 `web-admin`과 `web-saas`로 분리하고, SaaS 가입/워크스페이스/계좌이체 화면을 추가했다.

## 2026-05-27
- 변경된 FX 도메인(`ExchangeRate`, `ExchangeRateHistory`, `FxCorridor`) 기준으로 관리자 API 연결 상태를 점검했다.
- `FxUseCaseImpl`에 현재 환율 목록 조회와 통화 회랑 생성/조회/검색/수정/삭제/상태 변경 매핑을 추가했다.
- `api-admin`에 `GET /api/v1/fx/rate/current`, `GET /api/v1/fx/rate/history`, `POST/PATCH/DELETE /api/v1/fx/corridor`, 회랑 활성화/비활성화/정지 API를 추가했다.
- `web-admin`에 현재 환율 관리, 환율 이력 조회, 통화 회랑 관리 페이지를 추가하고 외환 사이드바 메뉴에 연결했다.
- 환율 타입 옵션을 현재 도메인 enum(`MID`, `BUY`, `SELL`, `CASH_BUY`, `CASH_SELL`, `REMIT_BUY`, `REMIT_SELL`)과 일치하도록 정리했다.
- 루트 `README.md`에 FX 도메인, Flyway 마이그레이션, 관리자 API/화면 범위를 갱신했다.
- 검증: `./gradlew :application:api-admin:compileJava`, `npx eslint`(변경 FX 파일), `npm run build`를 실행해 통과를 확인했다.
- 참고: `npm run lint` 전체 실행은 기존 관리자 상세/스케줄러/공통 UI 파일의 `react-hooks/set-state-in-effect` 등 기존 lint 오류로 실패했다.
