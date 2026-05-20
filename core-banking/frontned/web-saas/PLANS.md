# web-saas 개발 계획

## 목표
- `saas-api-docs.json`의 SAAS-API 계약을 바탕으로 사용자 SaaS용 코어뱅킹 프론트엔드를 구현한다.
- 로그인, 계좌, 입출금, 매매, 배당, 보유종목, 포트폴리오, 거래내역, 종목 조회를 한 화면에서 운영할 수 있게 한다.

## 구현 방향
- Next.js App Router 구조를 유지하고, 상호작용이 필요한 워크스페이스만 Client Component로 분리한다.
- API 호출은 서비스 클래스로 캡슐화하고 토큰 갱신은 기존 `api` axios 인스턴스를 재사용한다.
- UI는 금융 SaaS 운영 화면에 맞춰 조용하고 밀도 있는 정보 구조, 명확한 상태, 빠른 액션 중심으로 구성한다.

## 완료 기준
- `npm run build`가 통과한다.
- 주요 화면이 브라우저에서 깨지지 않고 렌더링된다.
- 실행/중지/재시작 스크립트와 README 사용법이 실제 포트와 데모 계정 정보를 안내한다.

---

## 추가 기능 로드맵 (2026-05-20~)

> 우선순위: 🔴 높음 / 🟡 중간 / 🟢 낮음

---

### 🔴 P1 — 핵심 누락 기능 (API 연결이 되어 있지만 UI가 없는 것)

#### 1. 계좌 생성 / 수정 / 삭제 (`POST`, `PATCH`, `DELETE /api/v1/accounts`)
- **현황**: 계좌 목록 조회·입출금은 구현됨. 계좌를 프론트에서 직접 생성하는 UI가 없음.
- **기획**:
  - `AccountPanel` 상단에 "계좌 추가" 버튼 → 슬라이드 인 폼 (계좌명, 통화, 유형)
  - 계좌 행 우측에 수정(연필)·삭제(휴지통) 아이콘 액션
  - 삭제 시 confirm 다이얼로그
- **파일**: `features/account/AccountPanel.tsx`, `features/account/account.service.ts`

#### 2. 보유 포지션 상세 뷰 (`GET /api/v1/accounts/{accountId}/positions`)
- **현황**: `AllocationPanel`에 비율 바 표시만. 실제 보유 수량·평균단가·평가손익이 표시되지 않음.
- **기획**:
  - `/workspace/accounts` 또는 `/workspace/portfolio` 신규 섹션
  - 포지션 테이블: 티커 | 보유수량 | 평균단가 | 현재가 | 평가금액 | 미실현손익(금액·%) | 실현손익
  - 포지션 행 클릭 → 종목 상세 사이드패널
- **파일**: `features/portfolio/PositionTable.tsx`, `features/portfolio/portfolio.service.ts`

#### 3. 내 프로필 (`GET /api/v1/auth/me`)
- **현황**: 토큰 디코딩으로 userId만 사용. `/me` API 미활용.
- **기획**:
  - 사이드바 하단: 아바타(이니셜) + 이름 + 이메일
  - 클릭 시 프로필 드롭다운: 계정 정보·로그아웃
- **파일**: `features/auth/UserProfile.tsx`, `workspace/WorkspaceSidebar.tsx`

---

### 🟡 P2 — UX 완성도 (있지만 불편한 것)

#### 4. 거래내역 필터 + 무한 스크롤 / 페이지네이션
- **현황**: `GET /api/v1/accounts/{accountId}/transactions`에 `pageable·request` 파라미터가 있지만 미활용. 전체를 한 번에 가져옴.
- **기획**:
  - 거래 유형 필터 칩 (전체·매수·매도·입금·출금·배당)
  - 날짜 범위 필터 (DatePicker)
  - "더 보기" 버튼 or 가상 스크롤 (100건 초과 시)
- **파일**: `features/trade/LedgerPanel.tsx`, `features/trade/trade.service.ts`

#### 5. 종목 상세 모달 (`GET /api/v1/stocks/{stockId}`)
- **현황**: 종목 리스트만 표시, 상세 정보 없음.
- **기획**:
  - 종목 행 클릭 → 슬라이드 오버 패널
  - 표시: 전체명·티커·거래소·섹터·현재가·통화
  - "매수하기" / "매도하기" 빠른 진입 버튼
- **파일**: `features/stock/StockDetailPanel.tsx`

#### 6. 거래내역 단건 상세 (`GET /api/v1/accounts/{accountId}/transactions/{transactionId}`)
- **기획**:
  - `LedgerPanel` 행 클릭 → 다이얼로그
  - 참조번호·유형·종목·수량·단가·수수료·세금·체결시각 전체 표시
- **파일**: `features/trade/TransactionDetailDialog.tsx`

---

### 🟢 P3 — 품질·경험 개선

#### 7. 포트폴리오 시각화 (도넛 차트)
- **기획**:
  - 대시보드 또는 포지션 페이지에 자산 배분 도넛 차트
  - 현금 + 보유 종목별 비율
  - 라이브러리: `recharts` (Next.js 친화적, 이미 많은 shadcn 블록에서 사용)
- **파일**: `features/portfolio/AllocationChart.tsx`

#### 8. 다크 모드 토글
- **기획**:
  - 사이드바 하단 Sun/Moon 아이콘 버튼
  - `next-themes` 패키지 활용, `globals.css`의 `.dark` 변수 적용
- **파일**: `workspace/WorkspaceSidebar.tsx`, `app/layout.tsx`

#### 9. 자동 새로고침 (선택 옵션)
- **기획**:
  - 사이드바 새로고침 버튼 옆 드롭다운: 끄기 / 30초 / 1분 / 5분
  - `useInterval` 커스텀 훅으로 폴링
- **파일**: `workspace/useWorkspace.ts`

#### 10. 계좌 없음 빈 상태(Empty State) 개선
- **현황**: 계좌가 없을 때 사이드바 셀렉트가 공란. 대시보드 수치가 0으로 보임.
- **기획**:
  - 첫 진입 시 "계좌를 만들어 시작하세요" 온보딩 카드
  - 빈 상태 일러스트·CTA 버튼

---

## 현재 구현 완료 목록

| 기능 | 라우트 | API |
|---|---|---|
| 랜딩 페이지 | `/` | — |
| 로그인 | `/login` | POST /auth/login |
| 회원가입 | `/signup` | POST /auth/signup |
| 가입 완료 | `/signup/complete` | — |
| 대시보드 | `/workspace` | portfolio, transactions |
| 계좌 관리·입출금 | `/workspace/accounts` | accounts, deposit, withdraw |
| 주식 거래·거래내역 | `/workspace/trades` | trades/buy·sell·dividend, transactions |
| 종목 조회 | `/workspace/stocks` | stocks |
