# 작업 계획

## 2026-05-20 포트폴리오 문서 작성
- 루트 작업 규칙 문서(`AGENTS.md`)와 계획 문서(`PLANS.md`)를 한국어로 정리한다.
- 백엔드 멀티모듈, 주요 도메인, 설계 플로우, API 목록을 확인한다.
- 프론트엔드 Admin/SaaS 구조와 사용자 흐름을 확인한다.
- 실행 가능한 화면을 캡처하여 문서에 삽입한다.
- 포트폴리오용 Markdown 문서(`PORTFOLIO.md`)를 작성한다.
- 루트 실행 스크립트와 README 링크를 보강한다.

## 2026-05-22 프로젝트 분석 및 문서 최신화
- 루트 README와 PORTFOLIO 문서가 현재 코드 구조, 포트, 도메인 범위와 일치하는지 점검한다.
- 백엔드 멀티모듈, API 컨트롤러, Flyway 마이그레이션, 인프라 compose 구성을 기준으로 분석 내용을 보강한다.
- 프론트엔드 web-admin/web-saas 화면 구조와 실행 정보를 README/PORTFOLIO에 반영한다.
- 오래된 포트(`api-saas` 8080 등)와 누락 도메인(주문, 청구, 정산, RBAC, 스케줄러)을 정리한다.

## 2026-05-22 Grafana 기본 대시보드 구성
- Grafana 컨테이너 기동 시 Prometheus datasource가 자동 등록되도록 provisioning 파일을 추가한다.
- Spring Boot 운영에서 자주 보는 HTTP 처리량/오류율/응답시간, JVM, CPU, DB 커넥션 풀, 로그 이벤트, Pushgateway 수집 지연 패널을 기본 대시보드로 구성한다.
- Pushgateway 라벨 보존을 위해 Prometheus scrape 설정을 조정하고 README에 접속 정보와 대시보드 위치를 기록한다.
