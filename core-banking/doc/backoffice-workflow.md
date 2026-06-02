# 백오피스 운영자 업무 플로우 (Back-Office Operations)

> 이 문서는 **개발자가 아닌 운영자(백오피스 사용자)** 관점에서, 관리자 콘솔(web-admin)에서
> 실제로 **어떤 화면에서 → 어떤 순서로 → 무엇을 클릭/입력하는지**를 정리한 업무 매뉴얼입니다.
>
> 데이터 모델·규칙의 "왜"가 궁금하면 각 도메인 문서([account](./account.md), [ledger](./ledger.md),
> [fx](./fx.md), [insurance](./insurance.md), [billing](./billing.md), [pg](./pg.md))를 참고하세요.
> 이 문서는 **"무슨 메뉴에서 무슨 버튼"** 에 집중합니다.

---

## 0. 공통 사항

### 로그인 & 권한
1. **로그인** 화면에서 관리자 계정으로 로그인 → JWT 발급.
2. 좌측 메뉴는 **부여된 권한(RBAC)** 에 따라 노출/동작이 제한됨.
   예) `ACCOUNT_TRANSFER` 권한이 없으면 계좌이체 버튼이 비활성.
3. 민감 행위(이체·체결·보험금지급·기간마감·배치실행)는 **행위자(관리자 ID)가 이력에 기록**됨.

### 화면 공통 패턴
거의 모든 메뉴가 동일한 구조입니다.
```
[목록 페이지] ──검색/필터──▶ [행 클릭] ──▶ [상세 페이지] ──▶ [상태별 액션 버튼]
```
- 목록: 페이지네이션 + 상태/기간 필터
- 상세: 현재 상태에 **허용된 액션 버튼만** 활성화 (예: PENDING 주문에만 "체결/취소")

### 주요 메뉴 한눈에
| 대분류 | 메뉴 | 운영 업무 |
|--------|------|-----------|
| 회원/권한 | User, Admin, RBAC Role | 회원 조회, 관리자/역할·권한 관리 |
| 계좌 | Account, Account Transaction, Transfer | 계좌 상태·입출금·이체, 거래내역 조회 |
| 증권 | Stock, Order, Trade, Dividend, Portfolio | 종목·주문·체결·배당·보유현황 |
| 외환 | Currency, Rate, Corridor, Conversion | 통화·환율·환전정책·환전내역 |
| 보험 | Product, Policy, Premium Payment, Claim | 상품·계약·보험료·보험금청구 |
| 원장 | Ledger Account, Journal, Period, Trial Balance | 계정과목·분개·기간마감·시산표 |
| 청구/정산 | Billing Invoice, Settlement | 청구서·정산 |
| PG | Merchant, Payment, Settlement | 가맹점·결제·정산 |
| 배치 | Quartz, Quartz History, Batch | 스케줄 잡 관리·실행·이력 |

---

## 1. 회원·관리자·권한 운영

### 1-1. 회원 관리 (User)
- `User` 메뉴 → 회원 목록 조회/검색.
- 회원 상세에서 이름·역할 확인. (고객 행위의 주체)

### 1-2. 관리자·역할 관리 (Admin / RBAC Role)
**신규 운영자 온보딩 플로우**
```
1. RBAC > Role 메뉴에서 역할 생성/확인
   - 역할명 + 권한 체크(USER_READ, ACCOUNT_WRITE, LEDGER_WRITE ...)
   - "읽기 전용 운영자", "회계 담당", "보험 심사역" 등 직무별 역할 설계
2. Admin 메뉴에서 관리자 계정 생성
3. 해당 관리자에게 역할 부여(assign)
```
> **원칙(최소권한)**: 직무에 필요한 권한만. 조회만 하는 운영자에게 쓰기/특수권한을 주지 않음.

---

## 2. 계좌 운영 (Account)

### 2-1. 입금/출금
```
Account 목록 → 계좌 상세 → [입금] 또는 [출금] 버튼 → 금액 입력 → 실행
```
- 출금은 **가용잔고**가 충분해야 함(부족 시 실패 메시지).

### 2-2. 계좌이체
```
Account > Transfer 화면 → 출금계좌 / 입금계좌 / 금액(+수수료) 입력 → 실행
```
- 출금측은 (이체액+수수료) 차감, 입금측은 이체액 입금 → **두 건의 거래내역**이 동시에 생성.
- 권한: `ACCOUNT_TRANSFER` 필요.

### 2-3. 계좌 정지
```
계좌 상세 → [정지(suspend)]  (정지 계좌는 입출금·주문 차단, 단 배당 입금은 허용)
```

### 2-4. 거래내역 조회 (Account Transaction)
- `Account Transaction` 메뉴에서 모든 잔고 변동(입출금/매수/매도/배당/수수료/세금)을 조회.
- 거래 상세에서 `referenceId`로 원천 거래(주문·이체·환전 등) 추적.

---

## 3. 증권 운영 (Stock / Order / Trade)

### 3-1. 종목 관리 (Stock)
- `Stock` 메뉴에서 종목 등록(티커·거래소·통화·섹터)·조회. 상장폐지 처리.

### 3-2. 주문 → 체결 → 취소 (Order)
```
Order 목록 → 주문 상세
 ├─ PENDING 주문 → [체결(execute)] : 체결단가 입력 → FILLED
 │     · 매수: 실잔고 차감 + 보유 로트 추가
 │     · 매도: FIFO로 로트 소진 + 매도대금 입금 + 실현손익 확정
 └─ PENDING 주문 → [취소(cancel)] : 선점 가용잔고 복원 → CANCELLED
```
- 신규 주문은 `Order > Place` 화면에서 계좌·종목·매수/매도·수량·(지정가) 입력.

### 3-3. 배당 지급 (Dividend)
```
Stock > Dividend 화면 → 계좌·종목·배당액·세금 입력 → 지급
```
- 정지 계좌에도 입금됨(해지 계좌는 차단).

### 3-4. 체결·포트폴리오 조회
- `Trade` : 체결 내역 조회.
- `Portfolio` : 계좌별 보유 종목·평가손익(미실현손익) 조회.

---

## 4. 외환 운영 (FX)

> 환전을 열려면 **통화 → 환율 → 환전구간(활성화)** 순서로 준비되어 있어야 합니다.

### 4-1. 사전 설정
```
1. FX > Currency  : 통화 등록(코드·기호·소수자릿수), 활성/비활성
2. FX > Rate      : 환율 수동 등록(quote) 또는 배치 자동 갱신 확인
                    - Rate > History 에서 시세 이력 조회
3. FX > Corridor  : 통화쌍 환전구간 생성(최소/최대/일한도/스프레드)
                    → [활성화(activate)] 해야 환전 가능
                    (필요시 [정지(suspend)] / [비활성(deactivate)])
```

### 4-2. 환전 내역 조회 (Conversion)
- `FX > Conversion` 에서 고객 환전 거래(적용환율·수수료·상태) 조회.
- 적용환율은 거래 시점 스냅샷으로 보관되어 사후 시세변동과 무관하게 추적.

---

## 5. 보험 운영 (Insurance)

### 5-1. 상품 설계 (Product)
- `Insurance > Product` 에서 상품 등록(보장한도·기준보험료·납입주기·기간)·단종.

### 5-2. 계약 생애주기 (Policy)
```
Policy 목록 → 계약 상세
 PENDING(청약) → [활성화(activate)]   ← ★1차 수익자 지분 합 100% 검증 통과해야 함
 ACTIVE        → [정지(suspend)] / [해지(terminate)]
 SUSPENDED     → [재개(reactivate)]
 PENDING       → [철회(cancel)]
 ACTIVE        → [보험료 수납(pay-premium)] 수동 처리 가능
```

### 5-3. 보험료 관리 (Premium Payment)
- 정기 보험료는 **배치(보험료 정산 잡)** 가 자동이체. → [batch.md](./batch.md)
- 수동 보정: `Premium Payment` 상세 → [수납(pay)] / [연체(overdue)] 처리.

### 5-4. 보험금 청구 심사 (Claim) — 핵심 심사 업무
```
Claim 목록 → 청구 상세
 SUBMITTED(접수) → [심사 시작(start-review)]  (심사자=본인 관리자 ID 기록)
 REVIEWING(심사중)
   ├─ [승인(approve)] : 승인금액 입력 → ★보장한도 초과 시 거부됨
   └─ [거절(reject)]  : 사유 입력
 APPROVED(승인) → [지급(pay)] : 지급계좌로 보험금 입금 → PAID
```
> **심사 원칙**: 승인액 ≤ 청구액 ≤ 보장한도. 심사노트·심사자·각 시각이 감사 추적용으로 남음.

---

## 6. 회계·원장 운영 (Ledger) — 회계 담당자 업무

### 6-1. 계정과목 관리 (Ledger Account)
- `Ledger > Account` 에서 계정과목(현금·예수금·수수료수익 등) 등록·계층 관리·단종.

### 6-2. 분개 입력·전기 (Journal)
```
Ledger > Journal > New 화면
 1. 분개 헤더(전기일·적요·원천참조) 입력
 2. 차변/대변 라인 추가  ← ★각 라인은 차변 또는 대변 한쪽만
 3. [전기(post)] : Σ차변 = Σ대변 검증 통과 시 POSTED
```
- 정정이 필요하면 분개 상세 → **[역분개(reverse)]** (원본은 삭제하지 않고 반대 분개 생성).

### 6-3. 회계기간 마감 (Period)
```
Ledger > Period
 1. 신규 기간 [개설(open)] (회계연도·월·시작/종료일)
 2. 월 결산 후 해당 기간 [마감(close)]
    → 마감 후엔 그 기간 전기일로 새 분개 불가
```

### 6-4. 시산표 (Trial Balance)
- `Ledger > Trial Balance` 에서 계정별 차변/대변 합계로 **대차 일치 여부** 확인(결산 검증).

---

## 7. 청구·정산 운영 (Billing / Settlement)

### 7-1. 청구서 (Billing Invoice)
```
Billing > Invoice
 1. 청구서 [생성] (계좌·청구월·통화)  status=DRAFT
 2. [항목 추가] (수수료 종류·수량·단가)  → 합계 자동 계산
 3. [발행(issue)] (납기일 입력)  DRAFT→ISSUED   ← ★항목 0건이면 발행 불가
 4. [수납(pay)]  ISSUED/OVERDUE→PAID
    (미납 시 [연체(overdue)] 처리)
```

### 7-2. 정산 (Settlement)
```
Settlement 목록 → 상세
 PENDING → [정산처리(settle)] → SETTLED
 PENDING → [실패(fail)] (사유) → FAILED
```
- 대량 정산은 배치가 생성, 운영자는 예외 건만 수동 처리.

---

## 8. PG 운영 (Payment Gateway)

### 8-1. 가맹점 관리 (Merchant)
- `PG > Merchant` 에서 가맹점 등록(수수료율·정산주기 T+n·정산계좌)·활성/비활성.

### 8-2. 결제 관리 (Payment)
```
PG > Payment 목록 → 결제 상세
 REQUESTED → [승인(approve)] → APPROVED
 APPROVED  → [취소(cancel)] / [환불(refund)]
```
- 결제수단: 카드/계좌이체/카카오·네이버·토스페이/가상계좌.

### 8-3. 정산 관리 (Settlement)
- 정산은 **PG 정산 배치** 가 매출일+정산주기에 자동 생성(가맹점별 집계). → [batch.md](./batch.md)
- 운영자는 `PG > Settlement` 에서 정산 결과 조회, 문제 건은 상세에서 [실패(fail)] 처리.

---

## 9. 배치 운영 (Quartz / Batch) — 가장 운영 비중 높은 화면

### 9-1. 잡 관리 (Quartz)
```
Quartz > Jobs 목록
 ├─ [실행 중(running)] 잡 모니터링
 ├─ 잡 상세 → [즉시 실행(run)]   ← 수동 트리거 (예: 환율 긴급 갱신)
 ├─ [일시중지(pause)] / [재개(resume)]
 ├─ [스케줄 변경(schedule)]  (cron 수정)
 ├─ [신규 등록(jobs)] / [수정] / [삭제]
```

### 9-2. 실행 이력 (Quartz History / Batch)
```
Quartz History / Batch > Executions
 ├─ 잡별 성공/실패·소요시간·처리 건수 조회
 ├─ 실패 잡 → [재시도(retry)]
 └─ Batch > Summary 로 전체 배치 현황 대시보드 확인
```
> **일상 운영**: 매일 아침 배치 이력에서 전일 정산/보험료/환율 잡의 성공 여부를 확인하고,
> 실패 건은 원인 확인 후 재시도. 멱등성이 보장되므로 재실행해도 중복 처리되지 않음.

---

## 10. 운영자 일일 체크리스트 (예시)

| 시점 | 업무 | 메뉴 |
|------|------|------|
| 장 시작 전 | 환율 갱신 잡 정상 여부 | Quartz History / FX Rate |
| 오전 | 전일 정산·보험료 배치 결과 확인, 실패 건 재시도 | Batch / Settlement |
| 수시 | 보험금 청구 접수 건 심사(승인/거절/지급) | Insurance Claim |
| 수시 | PG 결제 승인/취소/환불 처리 | PG Payment |
| 수시 | 계좌이체·입출금 요청 처리 | Account / Transfer |
| 월말 | 청구서 발행, 회계 분개·시산표 확인 | Billing / Ledger |
| 월 마감 | 회계기간 마감 | Ledger Period |
