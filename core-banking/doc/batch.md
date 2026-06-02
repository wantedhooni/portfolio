# 배치 (Quartz 스케줄러 잡)

> 도메인을 **횡단(cross-cutting)** 하는 주기적 업무를 처리하는 스케줄 잡 모음.
> 환율 갱신, 보험료 자동이체, 증권/PG 정산, 보험 원장 전기 등 "사람이 매일 손으로 할 수 없는 일"을 자동화합니다.

`quartz-batch` 모듈은 세 부분으로 나뉩니다.
- **quartz-batch-common** : 잡 공통 타입·DTO
- **quartz-batch-executor** : 실제 잡 로직(아래 잡들)
- **quartz-batch-management** : 잡 등록/조회/수정(관리자 API 연동)

---

## 1. 잡 목록

| 잡 | 도메인 | 하는 일 |
|----|--------|---------|
| ExchangeRateRefreshJob | 외환 | 외부 API에서 환율을 받아 갱신 |
| InsurancePremiumSettlementJob | 보험 | 납부 예정 보험료 자동이체 + 정산 기록 |
| InsuranceLedgerPostingJob | 보험·원장 | 보험 거래를 총계정원장으로 전기 |
| SecuritiesSettlementJob | 증권·원장 | 주식 체결을 정산·원장 전기 |
| PgSettlementJob | PG | 승인 결제를 가맹점별 정산(T+n) |
| SettlementJob | 청구·정산 | 정산 레코드 처리 |

> 잡 실행 이력은 `QuartzJobHistoryListener`가 기록하여 관리자 화면에서 추적 가능.

---

## 2. 환율 갱신 — ExchangeRateRefreshJob

```
1. (대상 결정)  FxCorridor에 등록된 통화쌍 목록을 quotes로 사용 (refresh)
                또는 Frankfurter 제공 전체 통화 조회 (refreshAll)
2. 외부 조회    Frankfurter REST API에서 mid-market 환율 조회
3. 저장         FxCommand.quoteRate → ExchangeRate UPSERT + History INSERT
                · Frankfurter는 mid 환율 → RateType.MID로 저장, source="frankfurter"
```
- **부분 성공 허용**: 개별 통화쌍 저장 실패는 로그만 남기고 나머지는 계속 처리.
- 관련 도메인: [fx.md](./fx.md)

---

## 3. 보험료 자동이체 — InsurancePremiumSettlementJob

납부일이 도래한 계약들의 보험료를 출금하고 정산을 기록합니다.

### 구조: 계약 단위 독립 트랜잭션
잡 본체(`InsurancePremiumSettlementServiceImpl`)는 **대상 계약 루프 + 집계**만 담당하고,
계약 1건 처리는 `InsurancePremiumSettlementProcessor`가 **`REQUIRES_NEW` 독립 트랜잭션**으로 수행합니다.

> 한 계약의 출금 실패가 **다른 계약 처리까지 롤백시키지 않도록** 트랜잭션을 분리.
> (self-injection 안티패턴 대신 별도 빈으로 분리해 AOP 프록시가 정상 동작)

### 계약 1건 처리 흐름 (`process`)
```
1. 멱등성 체크   referenceId(계약+납부일)로 이미 처리됐으면 → SKIPPED
2. 납부 예약     PremiumPayment(PENDING) 생성
3. 보험료 출금   billing_account에서 출금(AccountTx)
   ├─ 성공 → markPaid, 다음 납부일 전진, 정산(Settlement) 기록 → SUCCESS
   └─ 실패 → markFailed(잔고부족 등), 정산 FAIL 기록 → FAILED
```

### 결과 집계 (`ProcessResult`)
| 결과 | 의미 |
|------|------|
| SUCCESS | 출금·정산 성공 |
| FAILED | 출금 실패(잔고부족 등). 정산은 FAIL로 기록 |
| SKIPPED | 멱등성 — 이미 처리된 계약 |

잡은 `successCount` / `failedCount` / `skippedCount`를 집계해 반환.
- 관련 도메인: [insurance.md](./insurance.md), [billing.md](./billing.md)

---

## 4. 정산·원장 전기 잡

| 잡 | 흐름 요약 |
|----|-----------|
| **SecuritiesSettlementJob** | 주식 체결(AccountTx)을 정산 처리하고 총계정원장으로 전기 |
| **PgSettlementJob** | 매출기준일의 APPROVED 결제를 가맹점별 집계 → PgSettlement 생성 → 가맹점 입금·원장 전기(현금/정산부채/수수료수익 3분할) |
| **InsuranceLedgerPostingJob** | 보험료 수취·보험금 지급을 원장으로 전기 |
| **SettlementJob** | PENDING 정산 레코드를 SETTLED로 처리 |

공통 원칙:
- 모든 정산·전기는 `referenceId` **멱등성**으로 중복 실행 방어.
- 회계 반영은 [ledger.md](./ledger.md)의 복식부기 규칙(Σ차변=Σ대변)을 따름.
- 관련 도메인: [pg.md](./pg.md), [account.md](./account.md), [billing.md](./billing.md)

---

## 5. 현업 기준 유의사항

- **멱등성 필수**: 배치는 재실행·재시도가 잦음. 모든 잡은 `referenceId`로 "이미 처리됨"을 판별해 중복 출금/정산을 막아야 함.
- **트랜잭션 격리**: 대량 건 처리는 건별 독립 트랜잭션으로 분리해 한 건 실패가 전체를 롤백시키지 않도록 함(부분 성공).
- **실행 이력**: 잡 성공/실패·소요시간을 기록해 운영 모니터링·재처리 근거 확보.
- **시각 처리**: 정산 기준일은 `LocalDate`, 실행 시각은 `Instant`. 컨테이너 타임존에 의존하지 않도록 기준일을 명시적으로 전달.
- **외부 의존 격리**: 환율 등 외부 API 실패는 부분 성공으로 흡수하고 다음 주기에 복구.
