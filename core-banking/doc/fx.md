# 외환 (FX / Foreign Exchange)

> 통화 등록, 환율 시세 관리, **환전 구간(Corridor) 기반 스프레드 환전**을 담당하는 도메인.
> 외부 시세(Frankfurter API)를 받아 고객 적용 환율을 산출하고 계좌 간 통화를 교환합니다.

---

## 1. 용어 정리

| 용어 | 영문/필드 | 설명 |
|------|-----------|------|
| 통화 | Currency | ISO 4217 코드(KRW/USD/...), 기호, 소수 자릿수 |
| 통화쌍 | base/quote | `base/quote` = 1 base가 몇 quote인지. 예 USD/KRW = 1380 |
| 매매기준율 | MID | 은행이 사고파는 기준 중간 환율 |
| 현재환율 | ExchangeRate | 통화쌍×rateType별 **최신 1행**(UPSERT 유지) |
| 환율이력 | ExchangeRateHistory | 시세가 들어올 때마다 INSERT되는 이력 |
| 환전구간 | FxCorridor | 통화쌍별 환전 조건(최소/최대/일한도/스프레드) |
| 스프레드 | spreadRate | 시장환율에 얹는 마진. 은행의 환전 수익원 |
| 환전 | FxConversion | 실제 환전 거래. PENDING→COMPLETED |
| 적용환율 | appliedRate | 환전 시점에 스냅샷된 고객 적용 환율 |

### 환율 타입(RateType) — 현업 환율 구조
| 타입 | 의미 |
|------|------|
| MID | 매매기준율(중간) |
| BUY / SELL | 은행이 사는/파는 가격 |
| CASH_BUY / CASH_SELL | 현찰 살 때/팔 때 |
| REMIT_BUY / REMIT_SELL | 송금 받을 때/보낼 때 |

> 실제 은행은 같은 통화라도 "현찰/송금/매매기준"에 따라 환율이 다릅니다. 이 시스템도 동일하게 구분.

---

## 2. 환율 저장 구조 — 현재값 + 이력

새 시세가 들어오면 **2단계로 저장**합니다.
```
1. ExchangeRateHistory 에 이력 INSERT   (감사·차트용, 계속 쌓임)
2. ExchangeRate 에 UPSERT               (통화쌍+타입당 항상 1행, refresh로 in-place 갱신)
```
- `ExchangeRate`는 `(base, quote, rateType)` 유니크 → 조회는 항상 최신 1건.
- 환율 갱신은 배치(`ExchangeRateRefreshJob`)가 주기적으로 외부 API에서 받아 수행 → [batch.md](./batch.md)

---

## 3. 환전 구간(FxCorridor) — 환전 정책

특정 통화쌍의 환전 가능 조건을 정의합니다.

| 항목 | 의미 |
|------|------|
| minAmount / maxAmount | 건당 최소/최대 환전 금액(maxAmount null=무제한) |
| dailyLimit | 1일 누적 한도(null=무제한) |
| spreadRate | 스프레드율. 0.015 = 1.5% |
| status | INACTIVE → ACTIVE / SUSPENDED |

### 고객 적용 환율 계산
```
customerRate = marketRate × (1 − spreadRate)
```
예) 시장환율 1,380, 스프레드 1.5% → 고객환율 = 1,380 × 0.985 = 1,359.3
- 차액(1,380 − 1,359.3 = 20.7/USD)이 **은행의 환전 수익**.
- 구간이 `ACTIVE`가 아니면 `applySpread`에서 예외 → 환전 차단.
- 동일 통화쌍은 Corridor 1개만 허용(유니크).

---

## 4. 환전 플로우 (`FxConversion`)

```
1. 검증        Corridor.validateAmount(금액)  ← 최소/최대 한도 체크
2. 환율 산출   Corridor.applySpread(marketRate) → appliedRate
3. 금액 계산   toAmount = fromAmount × appliedRate (− 수수료)
4. 환전 요청   FxConversion.request(...) PENDING, referenceId 멱등성
5. 계좌 처리   from 계좌 출금(AccountTx) + to 계좌 입금(AccountTx)
6. 완료        complete(debitTxId, creditTxId, now) → COMPLETED
   (실패 시 fail(reason) → FAILED, 취소는 PENDING에서만 cancel)
```

- `appliedRate`를 **스냅샷**으로 보관 → 이후 시세가 변해도 그 거래의 환율을 추적 가능.
- `from_account_id`와 `to_account_id`는 **서로 다른 통화** 계좌(예: USD계좌→KRW계좌).
- `@Version` 낙관적 락으로 동시 환전 충돌 방지.

---

## 5. 상태 흐름

```
FxConversion:  PENDING ──complete──▶ COMPLETED
                  │  └──fail──▶ FAILED
                  └──cancel──▶ CANCELLED
FxCorridor:     INACTIVE ──activate──▶ ACTIVE ──suspend──▶ SUSPENDED
```

---

## 6. 현업 기준 유의사항

- **환율 스냅샷 필수**: 환전 후 시세가 변동해도 `appliedRate`로 그 거래를 재현·정산할 수 있어야 함.
- **동일 통화 금지**: base==quote 환율/환전은 `InvalidFxPairException`.
- **양수 환율**: rate ≤ 0은 차단.
- **한도 관리**: 건당 한도(Corridor)와 일 누적 한도(dailyLimit)는 자금세탁방지(AML)·리스크 관리 관점에서 중요.
- **소수 자릿수**: 통화별 `decimalPlaces`(KRW 0, USD 2)에 맞춰 최종 반올림. 내부 환율 계산은 8자리 정밀도.
- **원장 연계**: 환전손익·수수료는 `JournalEntry`로 전기(reference_type=FX_CONVERSION).
