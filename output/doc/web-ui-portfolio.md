# Web UI 개선 포트폴리오

## 프로젝트 개요
- 프로젝트명: `securities_monolithic` 고객용 프론트엔드 `web-ui`
- 성격: 금융 서비스형 고객 포털 UI/UX 개선
- 목적: 기존 데모 느낌의 화면을 실제 서비스형 UI로 정리하고, 데스크톱/모바일 반응형과 주요 업무 화면 사용성을 개선

## 담당한 작업
- 공통 헤더와 홈 화면을 단순한 고객 포털 구조로 재구성
- 시장, 계좌, 환전, 거래, 주문 화면의 정보 구조와 문구를 실제 사용자 기준으로 재정리
- 모바일에서 깨지던 주문 화면을 카드형 목록으로 전환
- 차트 화면의 로딩/이동 흐름과 일부 사용성 문제를 정리
- 전역 입력창, 버튼, 셀렉트박스 스타일을 통일해 화면 일관성 개선

## 핵심 개선 포인트
### 1. 서비스형 톤으로 재정리
- 과한 소개성 문구와 장식 요소를 줄이고, 필요한 메뉴만 빠르게 이동하는 구조로 변경
- 카드, 버튼, 라운드, 그림자 밀도를 줄여 더 단정한 금융 서비스 UI로 정리

### 2. 업무 화면 중심 UX 개선
- 시장: 종목 검색, 현재가, 차트, 주문 이동 흐름을 단순화
- 계좌: 계좌 현황 확인 후 개설, 입금, 출금, 이체 작업으로 자연스럽게 연결
- 환전: 송금이 아닌 환전 계산 흐름 중심으로 정보 구조 재정리
- 거래/주문: 오류 메시지와 상태 표현을 사람이 읽기 쉬운 방식으로 정리

### 3. 반응형 구조 재설계
- PC: 다열 정보 구조 유지
- 태블릿: 2열 중심으로 재배치
- 모바일: 1열 및 전체 폭 액션 버튼 중심으로 재배치
- 주문 화면은 모바일에서 테이블 대신 카드형 목록으로 변경

## 사용 기술
- React
- Vite
- CSS
- Playwright 기반 화면 캡처

## 결과 화면
### 홈 화면
![home-desktop](/Users/revy/workspace_revy/securities_monolithic/output/playwright/portfolio-home-desktop.png)

### 시장 화면
![chart-desktop](/Users/revy/workspace_revy/securities_monolithic/output/playwright/portfolio-chart-desktop.png)

### 계좌 화면
![account-desktop](/Users/revy/workspace_revy/securities_monolithic/output/playwright/portfolio-account-desktop.png)

### 환전 화면
![exchange-desktop](/Users/revy/workspace_revy/securities_monolithic/output/playwright/portfolio-exchange-desktop.png)

### 거래 화면
![trade-desktop](/Users/revy/workspace_revy/securities_monolithic/output/playwright/portfolio-trade-desktop.png)

### 주문 화면
![orders-desktop](/Users/revy/workspace_revy/securities_monolithic/output/playwright/portfolio-orders-desktop.png)

### 모바일 홈 화면
![home-mobile](/Users/revy/workspace_revy/securities_monolithic/output/playwright/portfolio-home-mobile.png)

### 모바일 거래 화면
![trade-mobile](/Users/revy/workspace_revy/securities_monolithic/output/playwright/portfolio-trade-mobile.png)

### 모바일 주문 화면
![orders-mobile](/Users/revy/workspace_revy/securities_monolithic/output/playwright/portfolio-orders-mobile.png)

## 포트폴리오 메시지
- 데모 스타일 화면을 실제 금융 서비스형 UI로 정리할 수 있음
- 화면 디자인뿐 아니라 정보 구조, 오류 메시지, 반응형, 업무 흐름까지 함께 개선 가능
- 고객용 화면에서 PC와 모바일을 분리해 사용 맥락에 맞게 설계 가능
