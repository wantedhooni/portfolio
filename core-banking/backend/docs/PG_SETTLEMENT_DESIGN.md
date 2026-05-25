# PG 정산 도메인 설계서

> **작성일**: 2026-05-25  
> **대상 모듈**: `module/domain` → `domain.pg`  , `module/business-logic` → `pg` 패키지  
> **상태**: 설계 초안

---

## 1. 개요

PG(Payment Gateway) 정산 도메인은 외부 PG사를 통해 발생한 결제·취소·환불 거래를 집계하고, 가맹점에게 실제 자금을 입금하는 전 과정을 모델링한다.

### 1.1 핵심 개념

| 용어 | 설명 |
|------|------|
| **가맹점 (Merchant)** | PG 결제를 수납하는 사업자 |
| **PG 거래 (PgTransaction)** | 승인·취소·환불 단건 |
| **MDR** | Merchant Discount Rate — 가맹점이 부담하는 카드 수수료율 |
| **정산 배치 (SettlementBatch)** | 특정 기준일의 거래를 묶는 정산 단위 |
| **정산 명세 (SettlementLine)** | 배치 내 거래 1건의 정산 상세 |
| **정산 입금 (SettlementDeposit)** | 가맹점 계좌로 실제 입금된 이벤트 |
| **대사 (Reconciliation)** | PG사 통보 내역과 내부 기록의 일치 여부 확인 |

### 1.2 정산 주기

```
결제 승인 (D)
  └─ 익영업일(D+1) 오전 2시: 정산 배치 생성 (PENDING)
       └─ 오전 3시: 금액 계산 완료 (CALCULATING → CALCULATED)
            └─ 오전 9시: 가맹점 계좌 입금 (DEPOSITING → DEPOSITED)
                 └─ 대사 완료 (RECONCILED)
```

---

## 2. 도메인 모델 (JPA 엔티티 설계)

### 2.1 엔티티 관계도

```
PgMerchant ─── 1:N ──▶ PgTransaction
PgMerchant ─── 1:N ──▶ PgSettlementBatch
PgSettlementBatch ─── 1:N ──▶ PgSettlementLine
PgSettlementBatch ─── 1:1 ──▶ PgSettlementDeposit
PgSettlementLine ─── N:1 ──▶ PgTransaction
```

---

### 2.2 PgMerchant (가맹점)

**테이블**: `pg_merchant`

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `id` | BIGINT | PK, IDENTITY | |
| `merchant_code` | VARCHAR(30) | UNIQUE, NOT NULL | 내부 가맹점 코드 |
| `business_name` | VARCHAR(100) | NOT NULL | 상호명 |
| `business_number` | VARCHAR(20) | NOT NULL | 사업자등록번호 |
| `representative_name` | VARCHAR(50) | NOT NULL | 대표자명 |
| `settlement_account_id` | BIGINT | NOT NULL, FK→account | 정산 수취 계좌 |
| `mdr_rate` | DECIMAL(6,4) | NOT NULL | 수수료율 (e.g. 0.0200 = 2%) |
| `vat_rate` | DECIMAL(6,4) | NOT NULL, DEFAULT 0.1 | 부가세율 (수수료의 10%) |
| `settlement_cycle` | VARCHAR(10) | NOT NULL | `D1` / `D2` |
| `status` | VARCHAR(20) | NOT NULL | `ACTIVE` / `SUSPENDED` / `TERMINATED` |
| `created_at` | TIMESTAMP | NOT NULL | BaseEntity |
| `updated_at` | TIMESTAMP | NOT NULL | BaseEntity |

**상태 흐름**:
```
ACTIVE ──suspend()──▶ SUSPENDED ──reactivate()──▶ ACTIVE
ACTIVE ──terminate()──▶ TERMINATED
SUSPENDED ──terminate()──▶ TERMINATED
```

**인덱스**:
- `idx_pg_merchant_code` → `merchant_code`
- `idx_pg_merchant_status` → `status`

---

### 2.3 PgTransaction (PG 거래)

**테이블**: `pg_transaction`

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `id` | BIGINT | PK, IDENTITY | |
| `tx_number` | VARCHAR(40) | UNIQUE, NOT NULL | 내부 거래번호 (TXN-{UUID}) |
| `merchant_id` | BIGINT | NOT NULL, FK | |
| `tx_type` | VARCHAR(20) | NOT NULL | `PAYMENT` / `CANCEL` / `PARTIAL_CANCEL` / `REFUND` |
| `pay_method` | VARCHAR(20) | NOT NULL | `CARD` / `VIRTUAL_ACCOUNT` / `BANK_TRANSFER` / `EASY_PAY` |
| `status` | VARCHAR(20) | NOT NULL | `APPROVED` / `CANCELLED` / `PARTIAL_CANCELLED` / `FAILED` |
| `currency` | VARCHAR(3) | NOT NULL | |
| `gross_amount` | DECIMAL(20,4) | NOT NULL | 결제 원금 (취소시 음수) |
| `mdr_fee` | DECIMAL(20,4) | NOT NULL | MDR 수수료 |
| `vat_fee` | DECIMAL(20,4) | NOT NULL | 수수료 부가세 |
| `net_amount` | DECIMAL(20,4) | NOT NULL | 정산 대상 금액 = gross − mdr − vat |
| `pg_provider` | VARCHAR(30) | NOT NULL | `KCP` / `INICIS` / `TOSS` / `NICE` 등 |
| `pg_tx_id` | VARCHAR(100) | | PG사 거래번호 |
| `approval_number` | VARCHAR(30) | | 카드 승인번호 |
| `original_tx_id` | BIGINT | FK(self) | 원거래 ID (취소/환불 시) |
| `cancel_amount` | DECIMAL(20,4) | | 부분취소 금액 |
| `approved_at` | TIMESTAMP | | |
| `cancelled_at` | TIMESTAMP | | |
| `settled_batch_id` | BIGINT | FK(nullable) | 포함된 정산 배치 ID |
| `created_at` | TIMESTAMP | NOT NULL | |
| `updated_at` | TIMESTAMP | NOT NULL | |

**상태 흐름**:
```
APPROVED ──cancel()──▶ CANCELLED
APPROVED ──partialCancel(amount)──▶ PARTIAL_CANCELLED
```

**인덱스**:
- `idx_pg_tx_merchant_id` → `merchant_id`
- `idx_pg_tx_status` → `status`
- `idx_pg_tx_approved_at` → `approved_at`
- `idx_pg_tx_settled_batch_id` → `settled_batch_id`
- `uq_pg_tx_number` → `tx_number` (UNIQUE)
- `idx_pg_tx_pg_tx_id` → `pg_tx_id`

**비즈니스 규칙**:
- `CANCEL` / `REFUND` 의 `gross_amount` 는 음수
- 부분취소는 원거래 `gross_amount` 범위 내로 제한
- MDR/VAT 계산: `mdr_fee = |gross_amount| × mdr_rate`, `vat_fee = mdr_fee × vat_rate`

---

### 2.4 PgSettlementBatch (정산 배치)

**테이블**: `pg_settlement_batch`

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `id` | BIGINT | PK | |
| `batch_number` | VARCHAR(40) | UNIQUE, NOT NULL | `BATCH-{merchantId}-{yyyy-MM-dd}` |
| `merchant_id` | BIGINT | NOT NULL, FK | |
| `settlement_date` | DATE | NOT NULL | 정산 기준일 |
| `target_date_from` | DATE | NOT NULL | 집계 대상 기간 시작 |
| `target_date_to` | DATE | NOT NULL | 집계 대상 기간 종료 |
| `status` | VARCHAR(20) | NOT NULL | 아래 참조 |
| `currency` | VARCHAR(3) | NOT NULL | |
| `tx_count` | INT | NOT NULL, DEFAULT 0 | 대상 거래 건수 |
| `total_gross_amount` | DECIMAL(20,4) | NOT NULL | 총 결제 원금 합계 |
| `total_fee_amount` | DECIMAL(20,4) | NOT NULL | 총 수수료 합계 (MDR+VAT) |
| `total_net_amount` | DECIMAL(20,4) | NOT NULL | 총 정산 금액 |
| `calculated_at` | TIMESTAMP | | 금액 계산 완료 시각 |
| `deposited_at` | TIMESTAMP | | 입금 완료 시각 |
| `reconciled_at` | TIMESTAMP | | 대사 완료 시각 |
| `failure_reason` | VARCHAR(500) | | 실패 사유 |
| `created_at` | TIMESTAMP | NOT NULL | |
| `updated_at` | TIMESTAMP | NOT NULL | |

**`status` 상태 흐름**:
```
PENDING ──calculate()──▶ CALCULATED
CALCULATED ──startDeposit()──▶ DEPOSITING
DEPOSITING ──confirmDeposit()──▶ DEPOSITED
DEPOSITED ──reconcile()──▶ RECONCILED
PENDING/CALCULATED/DEPOSITING ──fail()──▶ FAILED
FAILED ──retry()──▶ PENDING
```

**유니크 제약**: `(merchant_id, settlement_date)` — 가맹점 + 기준일 중복 불가

**인덱스**:
- `idx_pg_batch_merchant_date` → `merchant_id, settlement_date`
- `idx_pg_batch_status` → `status`
- `idx_pg_batch_settlement_date` → `settlement_date`

---

### 2.5 PgSettlementLine (정산 명세)

**테이블**: `pg_settlement_line`

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `id` | BIGINT | PK | |
| `batch_id` | BIGINT | NOT NULL, FK | |
| `tx_id` | BIGINT | NOT NULL, FK | |
| `line_type` | VARCHAR(20) | NOT NULL | `PAYMENT` / `CANCEL` / `REFUND` / `FEE_ADJUSTMENT` |
| `currency` | VARCHAR(3) | NOT NULL | |
| `gross_amount` | DECIMAL(20,4) | NOT NULL | |
| `fee_amount` | DECIMAL(20,4) | NOT NULL | MDR + VAT |
| `net_amount` | DECIMAL(20,4) | NOT NULL | |
| `note` | VARCHAR(255) | | |
| `created_at` | TIMESTAMP | NOT NULL | |
| `updated_at` | TIMESTAMP | NOT NULL | |

**인덱스**:
- `idx_pg_line_batch_id` → `batch_id`
- `idx_pg_line_tx_id` → `tx_id`
- `uq_pg_line_batch_tx` → `(batch_id, tx_id)` UNIQUE

---

### 2.6 PgSettlementDeposit (정산 입금)

**테이블**: `pg_settlement_deposit`

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `id` | BIGINT | PK | |
| `batch_id` | BIGINT | UNIQUE, NOT NULL, FK | 배치 1건당 입금 1건 |
| `deposit_account_id` | BIGINT | NOT NULL, FK→account | 입금 대상 계좌 |
| `currency` | VARCHAR(3) | NOT NULL | |
| `deposit_amount` | DECIMAL(20,4) | NOT NULL | |
| `status` | VARCHAR(20) | NOT NULL | `PENDING` / `CONFIRMED` / `FAILED` |
| `account_tx_id` | BIGINT | FK(nullable) | 실제 AccountTx ID |
| `reference_id` | VARCHAR(64) | UNIQUE | 멱등성 키 |
| `failure_reason` | VARCHAR(500) | | |
| `deposited_at` | TIMESTAMP | | |
| `created_at` | TIMESTAMP | NOT NULL | |
| `updated_at` | TIMESTAMP | NOT NULL | |

---

### 2.7 Enum 목록

```java
// PgMerchantStatus
ACTIVE, SUSPENDED, TERMINATED

// PgTxType
PAYMENT,          // 결제
CANCEL,           // 전체 취소
PARTIAL_CANCEL,   // 부분 취소
REFUND            // 환불 (이미 정산된 건의 역거래)

// PgPayMethod
CARD, VIRTUAL_ACCOUNT, BANK_TRANSFER, EASY_PAY

// PgTxStatus
APPROVED, CANCELLED, PARTIAL_CANCELLED, FAILED

// PgSettlementBatchStatus
PENDING,       // 생성됨, 아직 계산 전
CALCULATED,    // 금액 계산 완료
DEPOSITING,    // 입금 진행 중
DEPOSITED,     // 입금 완료
RECONCILED,    // 대사 완료
FAILED         // 처리 실패

// PgSettlementLineType
PAYMENT, CANCEL, REFUND, FEE_ADJUSTMENT

// PgDepositStatus
PENDING, CONFIRMED, FAILED
```

---

## 3. 서비스 설계 (Command / Reader 패턴)

기존 프로젝트의 Command / Reader 패턴을 그대로 따른다.

### 3.1 PgMerchantCommand

```java
public interface PgMerchantCommand {

    /** 가맹점 등록 → merchant_code 중복 검사 → ACTIVE 상태로 생성 */
    Long registerMerchant(RegisterMerchantCommand command);

    /** MDR 수수료율 변경 */
    void updateMdrRate(Long merchantId, BigDecimal newMdrRate);

    /** 정산 수취 계좌 변경 */
    void updateSettlementAccount(Long merchantId, Long newAccountId);

    /** 가맹점 정지 */
    void suspendMerchant(Long merchantId);

    /** 가맹점 정지 해제 */
    void reactivateMerchant(Long merchantId);

    /** 가맹점 계약 종료 */
    void terminateMerchant(Long merchantId);
}
```

---

### 3.2 PgTransactionCommand

```java
public interface PgTransactionCommand {

    /**
     * 결제 승인 거래 기록.
     * - merchant 활성 검증
     * - MDR/VAT 자동 계산
     * - tx_number 발급 (TXN-{UUID-12})
     */
    Long recordPayment(RecordPgPaymentCommand command);

    /**
     * 전체 취소.
     * - 원거래 APPROVED 상태 검증
     * - 이미 정산 배치에 포함된 경우 REFUND 타입으로 처리 (별도 역거래)
     * - cancel_amount = 원거래 gross_amount
     */
    Long cancelTransaction(Long txId, String reason);

    /**
     * 부분 취소.
     * - 취소 금액이 원거래 범위 내인지 검증
     * - 기존 부분취소 누계 + 금번 취소 ≤ 원거래 gross_amount
     */
    Long partialCancelTransaction(Long txId, BigDecimal cancelAmount, String reason);

    /**
     * 환불 (이미 정산 완료된 거래의 역거래).
     * - REFUND 타입 거래 신규 생성 (원거래에 연결)
     * - 차기 정산 배치에서 차감 처리
     */
    Long refundTransaction(Long originalTxId, BigDecimal refundAmount, String reason);
}
```

---

### 3.3 PgSettlementCommand

```java
public interface PgSettlementCommand {

    /**
     * 정산 배치 생성 (배치 Job 에서 호출).
     * - 멱등성: (merchantId, settlementDate) 중복 시 기존 배치 ID 반환
     * - 대상 거래: targetDateFrom ~ targetDateTo 기간의 APPROVED 거래 중
     *   settled_batch_id IS NULL 인 것
     */
    Long createBatch(CreateSettlementBatchCommand command);

    /**
     * 배치 금액 계산 (PENDING → CALCULATED).
     * - 대상 거래들의 gross_amount / fee / net 합산
     * - PgSettlementLine 생성 (거래당 1건)
     * - PgTransaction.settled_batch_id 업데이트
     */
    void calculateBatch(Long batchId);

    /**
     * 입금 실행 (CALCULATED → DEPOSITING).
     * - PgSettlementDeposit 생성 (referenceId = "PG-DEP-{batchId}")
     * - AccountCommand.deposit() 호출 → 가맹점 계좌 입금
     * - 멱등성: referenceId 중복 시 스킵
     */
    void executDeposit(Long batchId);

    /**
     * 입금 확인 (DEPOSITING → DEPOSITED).
     * - AccountTx 존재 확인 후 deposit 상태 CONFIRMED
     * - 배치 상태 DEPOSITED 전이
     */
    void confirmDeposit(Long batchId);

    /**
     * 대사 완료 (DEPOSITED → RECONCILED).
     * - PG사 통보 금액과 내부 net_amount 일치 검증
     * - 불일치 시 예외 (BusinessException PG_RECONCILIATION_MISMATCH)
     */
    void reconcile(Long batchId, BigDecimal pgConfirmedAmount);

    /**
     * 배치 실패 처리 (→ FAILED).
     */
    void failBatch(Long batchId, String reason);

    /**
     * 실패 배치 재시도 (FAILED → PENDING).
     */
    void retryBatch(Long batchId);
}
```

---

### 3.4 PgMerchantReader / PgTransactionReader / PgSettlementReader

```java
public interface PgMerchantReader {
    Optional<PgMerchantResult> findById(Long merchantId);
    Optional<PgMerchantResult> findByCode(String merchantCode);
    boolean existsByCode(String merchantCode);
    Page<PgMerchantResult> search(PgMerchantSearchCondition condition, Pageable pageable);
}

public interface PgTransactionReader {
    Optional<PgTxResult> findById(Long txId);
    Optional<PgTxResult> findByTxNumber(String txNumber);
    /** 정산 배치 대상 거래 조회 (settled_batch_id IS NULL, 기간 필터) */
    List<PgTxResult> findUnsettledByMerchant(Long merchantId, LocalDate from, LocalDate to);
    Page<PgTxResult> search(PgTxSearchCondition condition, Pageable pageable);
}

public interface PgSettlementReader {
    Optional<PgSettlementBatchResult> findBatchById(Long batchId);
    Optional<PgSettlementBatchResult> findBatchByMerchantAndDate(Long merchantId, LocalDate date);
    List<PgSettlementLineResult> findLinesByBatchId(Long batchId);
    Optional<PgDepositResult> findDepositByBatchId(Long batchId);
    Page<PgSettlementBatchResult> searchBatches(PgSettlementSearchCondition condition, Pageable pageable);
}
```

---

## 4. 핵심 비즈니스 규칙

### 4.1 수수료 계산

```
MDR 수수료 = |gross_amount| × mdr_rate
VAT         = mdr_fee × vat_rate      (기본 10%)
총 수수료   = mdr_fee + vat
net_amount  = gross_amount − 총수수료    (결제는 양수, 취소/환불은 음수)
```

**정산 배치 집계**:
```
total_gross = SUM(tx.gross_amount)   -- 취소/환불은 음수 포함
total_fee   = SUM(tx.mdr_fee + tx.vat_fee)
total_net   = SUM(tx.net_amount)
```

### 4.2 멱등성 키 전략

| 오퍼레이션 | referenceId |
|-----------|-------------|
| 배치 생성 | `PG-BATCH-{merchantId}-{yyyy-MM-dd}` |
| 입금 실행 | `PG-DEP-{batchId}` |
| 환불 역거래 | `PG-REFUND-{originalTxId}-{yyyyMMddHHmmss}` |

### 4.3 정산 불가 케이스

- 가맹점 `TERMINATED` 상태 → 거래 기록 불가
- `CANCELLED` 거래 → 정산 배치 제외
- 이미 다른 배치에 포함된 거래 (`settled_batch_id IS NOT NULL`) → 중복 포함 불가

### 4.4 환불 vs 취소 구분

| 구분 | 취소 (CANCEL) | 환불 (REFUND) |
|------|-------------|-------------|
| 시점 | 정산 전 | 정산 완료 후 |
| 처리 | 원거래 상태 변경 | 신규 역거래 생성 |
| 정산 | 배치에서 제외 | 차기 배치에서 차감 |

---

## 5. ErrorCode 추가 목록

```java
// PG 가맹점
PG_MERCHANT_NOT_FOUND(404,      "PG-001", "가맹점을 찾을 수 없습니다."),
PG_MERCHANT_DUPLICATED(409,     "PG-002", "이미 등록된 가맹점 코드입니다."),
PG_MERCHANT_NOT_ACTIVE(422,     "PG-003", "활성 상태의 가맹점이 아닙니다."),
PG_MERCHANT_TERMINATED(422,     "PG-004", "계약이 종료된 가맹점입니다."),

// PG 거래
PG_TX_NOT_FOUND(404,            "PG-101", "PG 거래를 찾을 수 없습니다."),
PG_TX_NOT_APPROVED(422,         "PG-102", "승인 완료된 거래가 아닙니다."),
PG_TX_ALREADY_CANCELLED(409,    "PG-103", "이미 취소된 거래입니다."),
PG_TX_ALREADY_SETTLED(422,      "PG-104", "이미 정산된 거래입니다. 환불을 이용하세요."),
PG_TX_CANCEL_AMOUNT_EXCEEDED(422,"PG-105","취소 금액이 원거래 금액을 초과합니다."),
PG_TX_NUMBER_DUPLICATED(409,    "PG-106", "중복된 거래번호입니다."),

// PG 정산
PG_BATCH_NOT_FOUND(404,         "PG-201", "정산 배치를 찾을 수 없습니다."),
PG_BATCH_ALREADY_EXISTS(409,    "PG-202", "해당 가맹점·기준일의 정산 배치가 이미 존재합니다."),
PG_BATCH_INVALID_STATUS(422,    "PG-203", "현재 상태에서 수행할 수 없는 작업입니다."),
PG_BATCH_EMPTY(422,             "PG-204", "정산 대상 거래가 없습니다."),
PG_RECONCILIATION_MISMATCH(422, "PG-205", "PG사 통보 금액과 내부 정산 금액이 일치하지 않습니다."),
PG_DEPOSIT_FAILED(500,          "PG-206", "정산 입금 처리에 실패했습니다."),
```

---

## 6. Flyway 마이그레이션 파일 계획

```
V20260526000001__create_pg_merchant.sql
V20260526000002__create_pg_transaction.sql
V20260526000003__create_pg_settlement_batch.sql
V20260526000004__create_pg_settlement_line.sql
V20260526000005__create_pg_settlement_deposit.sql
```

---

## 7. Quartz 배치 Job 연동 계획

```
PgSettlementJob (기존 SettlementJob 패턴 참조)
  └─ 매일 오전 2시 실행 (CRON: 0 0 2 * * ?)
  └─ jobData: targetDate = YESTERDAY

처리 흐름 (가맹점별 REQUIRES_NEW 트랜잭션):
  1. 활성 가맹점 목록 조회
  2. 가맹점별 PgSettlementProcessor.process(merchant, targetDate)
     ├─ createBatch() — 멱등성 보장
     ├─ calculateBatch()
     ├─ executeDeposit()
     └─ confirmDeposit()
  3. 결과 집계 → PgSettlementJobResult 반환
```

---

## 8. 구현 우선순위

| 단계 | 범위 | 내용 |
|------|------|------|
| **1단계** | Domain + Migration | 엔티티 5종 + Flyway + ErrorCode |
| **2단계** | Command / Reader | PgMerchantCommand/Reader, PgTransactionCommand/Reader |
| **3단계** | Settlement | PgSettlementCommand/Reader, PgSettlementProcessor |
| **4단계** | Batch Job | PgSettlementJob, Quartz 등록 |
| **5단계** | API | api-admin: 가맹점 관리 / 정산 조회 엔드포인트 |
