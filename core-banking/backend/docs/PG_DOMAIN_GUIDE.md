# PG(Payment Gateway) 도메인 완전 가이드

> **대상 독자**: 백엔드 개발자, 도메인 이해가 필요한 팀원  
> **연관 문서**: [PG_SETTLEMENT_DESIGN.md](./PG_SETTLEMENT_DESIGN.md) — JPA·서비스 설계 상세  
> **작성일**: 2026-05-25

---

## 목차

1. [PG 생태계 이해](#1-pg-생태계-이해)
2. [결제 플로우](#2-결제-플로우)
3. [취소·환불 플로우](#3-취소환불-플로우)
4. [정산 플로우](#4-정산-플로우)
5. [대사(Reconciliation) 플로우](#5-대사reconciliation-플로우)
6. [엔티티 라이프사이클](#6-엔티티-라이프사이클)
7. [수수료 계산 구조](#7-수수료-계산-구조)
8. [실무 주요 이슈](#8-실무-주요-이슈)
9. [영업일·정산 주기](#9-영업일정산-주기)
10. [이상 거래·분쟁 처리](#10-이상-거래분쟁-처리)

---

## 1. PG 생태계 이해

### 1.1 플레이어 구조

```
┌────────────┐   결제 요청    ┌─────────────┐   승인 요청    ┌──────────────┐
│  소비자     │ ─────────────▶│  가맹점 앱  │ ─────────────▶│  PG사        │
│ (Consumer) │               │  (Merchant) │               │ (KCP/이니시스│
└────────────┘               └─────────────┘               │  토스/나이스) │
                                                            └──────┬───────┘
                                                                   │ 카드 승인 요청
                                                                   ▼
                                                            ┌──────────────┐
                                                            │  카드사 / 은행│
                                                            │ (Visa/신한/BC)│
                                                            └──────────────┘
```

| 역할 | 설명 | 예시 |
|------|------|------|
| **소비자** | 결제하는 고객 | 앱 사용자 |
| **가맹점 (Merchant)** | 상품·서비스를 판매하는 사업자 | 온라인 쇼핑몰 |
| **PG사** | 가맹점 대신 카드사와 통신하는 중개자 | KCP, 이니시스, 토스페이먼츠, NICE |
| **카드사 / 은행** | 실제 결제 승인 주체 | 신한카드, 국민카드, BC카드 |
| **우리 시스템** | 가맹점 정산 자금 보관·지급 코어 뱅킹 | PG 정산 도메인 |

### 1.2 PG사가 존재하는 이유

카드사는 수백 개, 은행은 수십 개다. 가맹점이 이들 모두와 직접 계약하려면 엄청난 비용이 든다. PG사는 이를 대행하며 가맹점에게 단일 API 인터페이스를 제공한다.

```
가맹점이 PG사 없이 직접 연동하는 경우:
  가맹점 ──계약──▶ 신한카드
  가맹점 ──계약──▶ 국민카드
  가맹점 ──계약──▶ 현대카드
  가맹점 ──계약──▶ 삼성카드  ... (20+ 개)

가맹점이 PG사를 통해 연동하는 경우:
  가맹점 ──계약──▶ PG사 ──계약──▶ 모든 카드사
```

---

## 2. 결제 플로우

### 2.1 카드 결제 전체 시퀀스

```
소비자           가맹점 앱        우리 PG API       PG사(KCP 등)     카드사
  │                │                │                  │               │
  │──결제 요청──▶│                │                  │               │
  │                │──recordPayment▶│                  │               │
  │                │                │──승인 요청 API──▶│               │
  │                │                │                  │──카드승인──▶  │
  │                │                │                  │◀──승인번호────│
  │                │                │◀──pg_tx_id 반환─│               │
  │                │◀── txNumber ──│                  │               │
  │◀──결제 완료──│                │                  │               │
```

### 2.2 `recordPayment` 내부 처리 흐름

```
PgTransactionCommand.recordPayment(command)
  │
  ├─ 1. 가맹점 활성 상태 검증 (ACTIVE 여부)
  │
  ├─ 2. 수수료 계산
  │      mdr_fee = gross_amount × mdr_rate
  │      vat_fee = mdr_fee × vat_rate
  │      net_amount = gross_amount - mdr_fee - vat_fee
  │
  ├─ 3. 거래번호 채번 ("TXN-" + UUID 앞 12자리)
  │
  ├─ 4. PgTransaction 생성 (status = APPROVED)
  │
  └─ 5. txId 반환
```

### 2.3 가상계좌 결제 흐름

카드와 달리 가상계좌는 **비동기 입금 확인**이 필요하다.

```
소비자            가맹점            PG사              우리 시스템
  │                │                 │                   │
  │──결제 요청──▶ │                 │                   │
  │                │──가상계좌 발급▶│                   │
  │◀──계좌번호───│                 │                   │
  │                │                 │                   │
  │ (나중에)       │                 │                   │
  │──입금──────────────────────────▶│                   │
  │                │                 │──웹훅(Webhook)──▶│
  │                │                 │                   ├─ PgTransaction 상태 APPROVED 전환
  │                │                 │                   └─ 이후 정산 대상에 포함
```

> **구현 포인트**: `VIRTUAL_ACCOUNT` 결제는 최초 생성 시 `status = PENDING`, 입금 확인 콜백 수신 후 `APPROVED`로 전환하는 별도 흐름이 필요하다. 현재 설계에서는 간소화하여 승인 후 기록으로 처리한다.

---

## 3. 취소·환불 플로우

### 3.1 취소 vs 환불 — 가장 중요한 구분

실무에서 가장 혼동하는 개념이다. **정산 여부**가 기준이다.

```
결제 승인 (D일)
     │
     ├─ 정산 전 취소 (D일 ~ 정산 전) ─────▶ CANCEL
     │     └─ 원거래를 CANCELLED 상태로 변경
     │     └─ 정산 배치에서 제외됨
     │     └─ 소비자에게 즉시 환불 (카드사 레벨)
     │
     └─ 정산 후 환불 (정산 완료 이후) ──────▶ REFUND
           └─ 원거래는 그대로 유지
           └─ 별도의 REFUND 역거래 생성 (음수 금액)
           └─ 차기 정산 배치에서 차감 처리
           └─ 가맹점 정산금에서 환불금 회수
```

### 3.2 전체 취소 플로우

```
cancelTransaction(txId, reason)
  │
  ├─ 1. 원거래 조회 및 검증
  │      ├─ status = APPROVED 여부 확인
  │      └─ settled_batch_id IS NULL 여부 확인 (정산 전인지)
  │           └─ NOT NULL이면 → 이미 정산됨 → REFUND로 유도 (예외)
  │
  ├─ 2. 원거래 상태 변경: APPROVED → CANCELLED
  │
  └─ 3. cancelled_at 기록
```

### 3.3 부분 취소 플로우

```
partialCancelTransaction(txId, cancelAmount, reason)
  │
  ├─ 1. 원거래 조회 및 검증
  │
  ├─ 2. 누계 취소 금액 검증
  │      기존 부분취소 누계 + 금번 cancelAmount ≤ 원거래 gross_amount
  │      초과 시 → PG_TX_CANCEL_AMOUNT_EXCEEDED 예외
  │
  ├─ 3. PARTIAL_CANCEL 거래 생성
  │      gross_amount = -cancelAmount (음수)
  │      original_tx_id = 원거래 id
  │
  ├─ 4. 원거래 cancel_amount 누계 업데이트
  │
  └─ 5. 원거래 status: APPROVED → PARTIAL_CANCELLED
```

### 3.4 환불 플로우 (정산 후)

```
refundTransaction(originalTxId, refundAmount, reason)
  │
  ├─ 1. 원거래 조회 — settled_batch_id IS NOT NULL 확인 (정산 완료 건만 환불 가능)
  │
  ├─ 2. 환불 금액 검증
  │      refundAmount ≤ 원거래 net_amount
  │
  ├─ 3. REFUND 타입 신규 PgTransaction 생성
  │      gross_amount = -refundAmount (음수)
  │      tx_type = REFUND
  │      original_tx_id = 원거래 id
  │      settled_batch_id = null  ← 차기 배치에서 처리
  │
  └─ 4. 소비자 환불은 PG사/카드사에서 처리 (우리 시스템은 기록만 담당)
```

---

## 4. 정산 플로우

### 4.1 전체 정산 파이프라인

```
[매일 D+1 오전 02:00] Quartz PgSettlementJob 실행
          │
          ▼
활성 가맹점 전체 목록 조회
          │
    ┌─────▼──────┐
    │ 가맹점 루프  │  (가맹점별 REQUIRES_NEW 독립 트랜잭션)
    └─────┬──────┘
          │
          ▼
  ① createBatch()         — 배치 생성 (PENDING)
          │
          ▼
  ② calculateBatch()      — 금액 집계, Line 생성 (→ CALCULATED)
          │
          ▼
  ③ executeDeposit()      — 가맹점 계좌 입금 (→ DEPOSITING)
          │
          ▼
  ④ confirmDeposit()      — 입금 확인 (→ DEPOSITED)
          │
          ▼
  [별도 Job / 수동] reconcile()  — 대사 완료 (→ RECONCILED)
```

### 4.2 calculateBatch 상세

정산 대상 거래 조건:
- `merchant_id` = 해당 가맹점
- `approved_at` ∈ `[target_date_from, target_date_to)`
- `status` ∈ (`APPROVED`, `CANCELLED`, `PARTIAL_CANCELLED`, `REFUND`)  
  → CANCELLED 거래도 수수료 정산은 PG사 정책에 따라 포함 가능
- `settled_batch_id IS NULL`  ← 아직 정산 안 된 건만

```
calculateBatch(batchId)
  │
  ├─ 1. 대상 거래 목록 조회
  │
  ├─ 2. 거래별 PgSettlementLine 생성
  │      line_type = tx.tx_type
  │      gross_amount = tx.gross_amount
  │      fee_amount   = tx.mdr_fee + tx.vat_fee
  │      net_amount   = tx.net_amount
  │
  ├─ 3. 배치 합계 계산
  │      total_gross = SUM(line.gross_amount)
  │      total_fee   = SUM(line.fee_amount)
  │      total_net   = SUM(line.net_amount)
  │
  ├─ 4. PgTransaction.settled_batch_id = batchId 업데이트 (bulk)
  │
  ├─ 5. 배치 상태: PENDING → CALCULATED
  │
  └─ 6. 대상 거래가 0건이면 → PG_BATCH_EMPTY 예외 또는 빈 배치로 SKIP
```

### 4.3 executeDeposit 상세

```
executeDeposit(batchId)
  │
  ├─ 1. 배치 상태 검증 (CALCULATED 여부)
  │
  ├─ 2. 멱등성 체크 — referenceId = "PG-DEP-{batchId}"
  │      이미 존재하면 SKIP (중복 입금 방지)
  │
  ├─ 3. PgSettlementDeposit 생성 (status = PENDING)
  │
  ├─ 4. AccountCommand.deposit() 호출
  │      depositAccountId = merchant.settlementAccountId
  │      amount = batch.totalNetAmount
  │      referenceId = "PG-DEP-{batchId}"
  │
  ├─ 5. 배치 상태: CALCULATED → DEPOSITING
  │
  └─ 6. 입금 실패 시 → PG_DEPOSIT_FAILED 예외, 배치 FAILED 전이
```

### 4.4 실제 자금 흐름 (D+1 정산 기준)

```
D일 (결제 발생)
  소비자 카드사 계좌 ──결제──▶ PG사 에스크로

D+1일 오전 (정산 처리)
  PG사 에스크로 ──정산 통보──▶ 우리 시스템
  우리 시스템 ──AccountCommand.deposit()──▶ 가맹점 정산 계좌
```

> **핵심**: 우리 시스템은 PG사로부터 자금을 받아 가맹점 계좌로 지급하는 **중간 정산 계층**이다. 실제 PG사 → 우리 은행 계좌 입금은 별도 뱅킹 연동이 필요하다 (현재 설계에서는 내부 AccountTx로 모델링).

---

## 5. 대사(Reconciliation) 플로우

### 5.1 대사란?

PG사가 매일 오전 **정산 통보 파일(CSV/API)**을 발송한다. 우리 시스템에서 집계한 금액과 PG사가 통보한 금액이 일치하는지 검증하는 작업이다.

```
우리 시스템 계산값: total_net_amount = 9,850,000원
PG사 통보값:        pg_confirmed_amount = 9,850,000원
                                          ↓
                              일치 → RECONCILED
                              불일치 → PG_RECONCILIATION_MISMATCH 예외
                                       → 수동 조사 필요
```

### 5.2 대사 불일치 원인 (실무)

| 원인 | 설명 | 처리 방법 |
|------|------|-----------|
| **누락 거래** | PG사 파일에는 있는데 우리 DB에 없음 | 수동 거래 등록 후 재대사 |
| **중복 거래** | 네트워크 오류로 동일 거래 2회 수신 | 중복 txId 확인 후 1건 삭제 |
| **취소 시점 차이** | 취소가 배치 집계 후 도달 | 다음 배치에서 환불 처리 |
| **수수료율 오차** | MDR 변경 사항이 양쪽에 반영 시점 불일치 | 수수료 조정 Line(FEE_ADJUSTMENT) 추가 |
| **PG사 오류** | PG사 파일 자체 오류 | PG사 CS 접수 |

### 5.3 `FEE_ADJUSTMENT` 명세

대사 차이 조정을 위한 특수 라인 타입이다.

```
정상 정산 net_amount:  9,850,000원
PG사 통보 금액:        9,840,000원
차이:                    -10,000원

→ FEE_ADJUSTMENT Line 추가
   gross_amount = 0
   fee_amount   = 10,000 (추가 수수료)
   net_amount   = -10,000
   note = "수수료율 조정 2026-05-25"
```

---

## 6. 엔티티 라이프사이클

### 6.1 PgMerchant 라이프사이클

```
                  ┌─────────────────────────────────┐
                  │                                 │
            registerMerchant()                      │
                  │                                 │
                  ▼                                 │
              ┌────────┐                            │
              │ ACTIVE │                            │
              └────┬───┘                            │
                   │                                │
          suspend()│                                │
                   ▼                                │
           ┌────────────┐         reactivate()      │
           │ SUSPENDED  │ ───────────────────────▶  │
           └─────┬──────┘                           │
                 │                                  │
       terminate()│                       terminate()│
                 ▼                                  │
           ┌────────────┐                           │
           │ TERMINATED │ ◀─────────────────────────┘
           └────────────┘

※ TERMINATED 는 복구 불가 (재계약 시 신규 가맹점으로 등록)
```

**가맹점 정지(SUSPENDED) 상태에서**:
- 신규 거래 기록 불가 (`PG_MERCHANT_NOT_ACTIVE` 예외)
- 기존 정산 배치는 정상 처리 (이미 생성된 배치는 영향 없음)
- 취소/환불은 허용 (기존 거래에 대한 권리)

### 6.2 PgTransaction 라이프사이클

```
                       recordPayment()
                             │
                             ▼
               ┌──────────────────────────┐
               │         APPROVED          │
               └────┬──────────┬──────────┘
                    │          │
           cancel() │          │ partialCancel()
                    │          │
                    ▼          ▼
              ┌──────────┐ ┌───────────────────┐
              │CANCELLED │ │ PARTIAL_CANCELLED  │
              └──────────┘ └───────────────────┘

※ CANCELLED / PARTIAL_CANCELLED 이후 추가 상태 전이 없음
※ 정산 완료 후 환불 = 원거래 유지 + 신규 REFUND 거래 생성
```

**정산과의 관계**:
```
APPROVED + settled_batch_id = NULL  → 미정산 (배치 대상)
APPROVED + settled_batch_id = batchId → 정산 포함됨
CANCELLED + settled_batch_id = NULL  → 배치 제외
REFUND    + settled_batch_id = NULL  → 차기 배치에서 차감
```

### 6.3 PgSettlementBatch 라이프사이클 (핵심)

```
         createBatch()
               │
               ▼
          ┌─────────┐
          │ PENDING │ ◀──────────────────── retryBatch()
          └────┬────┘                              ▲
               │ calculateBatch()                  │
               ▼                                   │
         ┌───────────┐                             │
         │CALCULATED │                             │
         └─────┬─────┘                             │
               │ executeDeposit()                  │
               ▼                                   │
         ┌───────────┐         fail()              │
         │ DEPOSITING│ ────────────────────────────┤
         └─────┬─────┘                        ┌────┴────┐
               │ confirmDeposit()             │ FAILED  │
               ▼                              └─────────┘
         ┌───────────┐     fail()                  ▲
         │ DEPOSITED │ ────────────────────────────┘
         └─────┬─────┘
               │ reconcile()
               ▼
         ┌────────────┐
         │ RECONCILED │  ← 최종 완료 상태
         └────────────┘

PENDING/CALCULATED/DEPOSITING/DEPOSITED 에서 fail() 가능
FAILED 에서 retryBatch() → PENDING 으로 복구
```

### 6.4 PgSettlementDeposit 라이프사이클

```
    executeDeposit() 내부에서 자동 생성
               │
               ▼
          ┌─────────┐
          │ PENDING │
          └────┬────┘
               │
       ┌───────┴───────┐
       │               │
  confirmDeposit()   입금 실패
       │               │
       ▼               ▼
  ┌──────────┐    ┌────────┐
  │CONFIRMED │    │ FAILED │
  └──────────┘    └────────┘
```

---

## 7. 수수료 계산 구조

### 7.1 MDR(Merchant Discount Rate) 이해

MDR은 가맹점이 PG사에 지불하는 결제 서비스 수수료다. 카드 종류, 가맹점 규모, 계약 조건에 따라 다르다.

```
일반적인 MDR 범위:
  온라인 결제: 1.5% ~ 3.0%
  소형 가맹점: 2.5% ~ 3.5%
  대형 가맹점: 0.8% ~ 1.5% (협상 가능)
  간편결제(카카오페이 등): 1.0% ~ 2.5%
```

### 7.2 수수료 계산 예시

```
결제 금액:     100,000원
MDR 요율:      2.0%  (= 0.0200)
VAT 요율:      10%   (= 0.1, 수수료의 10%)

MDR 수수료:   100,000 × 0.0200 = 2,000원
VAT(부가세):    2,000 × 0.1000 =   200원
총 수수료:      2,000 + 200    = 2,200원

net_amount:   100,000 - 2,200  = 97,800원  ← 가맹점 수취액
```

### 7.3 배치 합계 계산 (혼재 케이스)

```
거래 목록 (D일):
  TXN-001  PAYMENT      +100,000   MDR=2,000  VAT=200   net=+97,800
  TXN-002  PAYMENT       +50,000   MDR=1,000  VAT=100   net=+48,900
  TXN-003  CANCEL        -30,000   MDR=-600   VAT=-60   net=-29,340  ← 음수
  TXN-004  REFUND (*)    -20,000   MDR=-400   VAT=-40   net=-19,560  ← 음수

  (*) REFUND는 차기 배치가 원칙이지만 설명을 위해 포함

total_gross = 100,000 + 50,000 - 30,000 - 20,000 = 100,000원
total_fee   = (2,000+200) + (1,000+100) - (600+60) - (400+40) = 2,200원
total_net   = 97,800 + 48,900 - 29,340 - 19,560    = 97,800원
```

### 7.4 가맹점별 MDR 변경 시 처리

MDR이 변경되면 **변경일 이후 거래부터** 새 요율을 적용한다. 기존 거래는 소급 적용하지 않는다.

```java
// MDR 변경 이력을 남겨야 하는 경우 별도 테이블 필요
// 현재 설계: pg_merchant.mdr_rate 단일 컬럼 → 과거 거래 요율은 pg_transaction에 보존
// pg_transaction.mdr_fee 에 계산 당시 금액을 저장하므로 이력 추적 가능
```

---

## 8. 실무 주요 이슈

### 8.1 네트워크 중복 전송 (Duplicate Transaction)

PG사가 네트워크 오류로 동일 승인 요청을 2회 발송하는 경우.

```
해결 방법: pg_tx_id (PG사 거래번호) UNIQUE 인덱스
  → 두 번째 recordPayment() 시 PG_TX_NUMBER_DUPLICATED 예외
  → 가맹점에 첫 번째 txNumber 반환 (멱등성 응답)
```

### 8.2 배치 실패 재처리

정산 배치가 중간에 실패했을 때의 복구 흐름:

```
CASE 1: calculateBatch() 실패
  → 배치 FAILED 전이
  → PgTransaction.settled_batch_id = NULL 복구 (rollback으로 자동)
  → retryBatch() → PENDING → calculateBatch() 재시도

CASE 2: executeDeposit() 실패 (입금 실패)
  → PgSettlementDeposit.status = FAILED
  → 배치 FAILED 전이
  → 수동 확인 후 retryBatch() → 재시도 시 멱등성 체크로 중복 입금 방지

CASE 3: 배치는 DEPOSITED인데 실제 은행 입금 실패
  → 대사에서 미입금 탐지
  → 수동 재입금 요청
  → 수동 confirmDeposit() 호출
```

### 8.3 공휴일·영업일 처리

```
정산 주기가 D+1이라도 영업일 기준이다.
  - 금요일 결제 → 월요일 정산
  - 공휴일 전날 결제 → 공휴일 다음 영업일 정산

구현 방법:
  - 영업일 캘린더 테이블 관리 (holiday_calendar)
  - 배치 실행 전 targetDate 계산 시 영업일 보정
  - 현재 설계: targetDate = YESTERDAY (단순화), 실제 운영 시 영업일 캘린더 연동 필요
```

### 8.4 외화 결제 정산

```
해외 결제 (외화):
  거래 통화: USD  gross_amount = 100.00 USD
  정산 통화: KRW  (환율 적용)

  환전 시점: PG사가 처리 (각 PG사마다 다름)
  우리 시스템: PG사 통보 금액(KRW) 그대로 수신

  실무 주의:
  - 환율 변동으로 승인 금액과 정산 금액이 다를 수 있음
  - 대사 시 환차손/익 처리 필요 (FEE_ADJUSTMENT 활용)
```

### 8.5 부가세 환급 (세금계산서)

```
가맹점은 PG 수수료 VAT에 대해 세금계산서를 받아 매입 세액 공제 가능.

우리 시스템의 역할:
  - vat_fee 컬럼에 금액 보관
  - 월별 세금계산서 발행 기능 (향후 구현)
  - 정산 명세서 다운로드 API
```

---

## 9. 영업일·정산 주기

### 9.1 D+1 vs D+2 정산

가맹점 계약에 따라 정산 주기가 다르다.

| 구분 | 내용 | 장단점 |
|------|------|--------|
| **D+1** | 결제 다음 영업일 정산 | 가맹점 자금 회전 빠름, 우리 시스템 부담 큼 |
| **D+2** | 결제 2영업일 후 정산 | 리스크 관리 여유, 소규모 가맹점 불리 |
| **주 1회** | 매주 특정 요일 정산 | 정산 건수 최소화, 유동성 예측 쉬움 |

현재 설계의 `settlement_cycle` 컬럼: `D1` / `D2`

### 9.2 정산 기준일 계산 로직

```
배치 생성 시 target_date_from / target_date_to 결정:

D+1 가맹점:
  settlement_date = YESTERDAY (= D)
  target_date_from = YESTERDAY
  target_date_to   = YESTERDAY

D+2 가맹점:
  settlement_date = TODAY (= D+2)
  target_date_from = DAY_BEFORE_YESTERDAY (= D)
  target_date_to   = DAY_BEFORE_YESTERDAY (= D)
```

---

## 10. 이상 거래·분쟁 처리

### 10.1 차지백(Chargeback)

소비자가 카드사에 직접 이의를 제기하는 경우. PG 취소와 다르다.

```
일반 취소:   소비자 → 가맹점 → PG사 → 카드사
차지백:      소비자 ──────────────────▶ 카드사 (가맹점 동의 불필요)

차지백 발생 시:
  1. 카드사가 PG사에 통보
  2. PG사가 우리 시스템에 Webhook 발송
  3. 우리 시스템: 해당 거래 REFUND 처리
  4. 가맹점 정산금에서 차지백 금액 회수

차지백 대응:
  - 가맹점에 증빙 자료 요청 (배송 증명, 서명 등)
  - 60일 이내 이의제기 가능
  - 현재 설계: chargeback_reason, chargeback_at 컬럼 추가 필요 시 확장
```

### 10.2 분쟁 상태 관리 (향후 확장)

```
현재 PgTransaction 상태에 추가 가능한 상태:
  CHARGEBACK_RECEIVED   → 차지백 접수
  CHARGEBACK_DISPUTED   → 이의제기 중
  CHARGEBACK_RESOLVED   → 분쟁 해결 (가맹점 승소)
  CHARGEBACK_LOST       → 분쟁 패소 (가맹점 부담)
```

### 10.3 모니터링 포인트 (실무)

```
정산 담당자가 매일 확인해야 하는 항목:

1. FAILED 배치 목록 → 즉시 재처리 또는 수동 조사
2. RECONCILIATION_MISMATCH → PG사 대사 파일과 대조
3. 미정산 거래 (settled_batch_id IS NULL, N일 이상) → 누락 가능성
4. 대용량 거래 알림 → 임계값 초과 거래 (이상 거래 탐지)
5. 가맹점 잔액 변동 → 정산 입금 후 가맹점 계좌 잔액 확인
```

---

## 부록: 용어 사전

| 용어 | 풀이 |
|------|------|
| **VAN사** | Value Added Network — PG와 비슷하지만 오프라인 단말기 중심. 현재 설계 범위 외 |
| **에스크로** | 구매 확정 전 PG사가 결제금을 보관하는 방식 (상품 미수령 보호) |
| **PG 가맹점 ID** | PG사에서 부여하는 가맹점 식별자 (우리 merchant_code와 별도 관리 필요) |
| **TID** | Terminal ID — 가상 단말기 ID, 거래 라우팅에 사용 |
| **MID** | Merchant ID — PG사에서 부여하는 가맹점 ID |
| **승인번호** | 카드사가 부여하는 결제 승인 고유번호 (영수증에 표시) |
| **매입** | 카드사가 PG사로부터 거래 데이터를 수집하는 과정 |
| **정산 파일** | PG사가 익일 발송하는 CSV/Excel 형식의 정산 명세 |
| **대사** | PG사 파일과 내부 DB 금액 일치 여부 검증 |
| **차지백** | 소비자가 카드사에 직접 이의를 제기해 결제 취소하는 절차 |
