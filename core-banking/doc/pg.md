# PG 결제 (Payment Gateway)

> 가맹점(Merchant)의 카드·간편결제를 승인/취소/환불하고, **정산 주기(T+n)**에 따라
> 수수료를 제외한 금액을 가맹점 계좌로 지급하는 결제대행 도메인.

---

## 1. 용어 정리

| 용어 | 영문/필드 | 설명 |
|------|-----------|------|
| 가맹점 | PgMerchant | 결제를 받는 사업자. 수수료율·정산주기 보유 |
| 결제 | PgPayment | 한 건의 결제 거래 |
| 주문번호 | orderNo | 가맹점이 발행하는 고유 식별자(멱등성 키) |
| 수수료율 | commissionRate | 예 0.0300 = 3% |
| 수수료 | commissionAmount | amount × commissionRate |
| 정산예정액 | netAmount | amount − commissionAmount(가맹점이 받을 돈) |
| 정산주기 | settlementCycle | T+n. 매출일로부터 며칠 뒤 지급 |
| 정산 | PgSettlement | 가맹점×일별 결제를 모아 지급하는 레코드 |
| 매출기준일 | targetDate | 결제 승인일 |
| 정산일 | settlementDate | targetDate + settlementCycle |

### 결제수단(PaymentMethod)
CARD, BANK_TRANSFER, KAKAO_PAY, NAVER_PAY, TOSS_PAY, VIRTUAL_ACCOUNT

---

## 2. 가맹점(PgMerchant)

- `commissionRate`(수수료율 ≥ 0)와 `settlementCycle`(≥1일, 기본 T+2)을 관리.
- `settlementAccountId` — 정산금이 입금될 계좌.
- 비활성 가맹점은 `validateActive()`에서 차단(결제 불가).

---

## 3. 결제(PgPayment) 플로우

### 상태 흐름
```
REQUESTED(요청) ──approve──▶ APPROVED(승인/정산대기)
   │                          │  ├──cancel──▶ CANCELLED(고객취소)
   └──fail──▶ FAILED          │  └──refund──▶ REFUNDED(환불)
                              └──linkToSettlement──▶ 정산 연결
```

```
1. 결제 요청   request(merchant, method, orderNo, amount, commission, net, currency)
               · amount 양수 검증, orderNo 멱등성(유니크)
               · commissionAmount = amount × merchant.commissionRate
               · netAmount = amount − commissionAmount
               · status=REQUESTED
2. 승인        approve()  → APPROVED(승인시각 기록)
3. (사후) 취소/환불  cancel()/refund()  ← APPROVED에서만
4. 정산 연결   linkToSettlement(settlementId)  ← 정산 배치가 호출(중복 연결 차단)
```

- `orderNo` 유니크 → **동일 주문 중복 결제 방지**.
- 취소·환불은 **승인된 결제에만** 가능.
- 이미 정산된 결제를 다시 연결하면 `PG_PAYMENT_ALREADY_SETTLED` 예외.

---

## 4. 정산(PgSettlement) 플로우 — T+n

승인 완료된 결제를 **가맹점별·일별로 집계**해 정산 레코드를 만들고, 정산일에 가맹점 계좌로 지급합니다.

```
매출기준일(targetDate)의 APPROVED 결제들
   │  집계
   ▼
PgSettlement.create(merchant, targetDate, settlementDate=targetDate+cycle, ...)
   · paymentCount, totalAmount, commissionAmount, netAmount 집계
   · referenceId = "PGSTL-{merchantId}-{targetDate}"  (멱등성)
   · status=PENDING
   │
   ▼ 정산일 도래
complete()  → SETTLED(가맹점 계좌 입금)   /   fail(reason) → FAILED
```

### 원장 분개(전기 시)
```
DR 현금           (1001) = totalAmount        ← 고객이 낸 총액
   CR 가맹점정산부채 (2002) = netAmount         ← 가맹점에 줄 돈
   CR 수수료수익     (4002) = commissionAmount  ← PG사 수익
```

- 정산은 `PENDING`에서만 complete/fail 가능.
- `referenceId`(PGSTL-가맹점-날짜) 유니크로 **같은 날 중복 정산 방지**.
- 정산 실행은 PG 정산 배치(`PgSettlementJob`)가 담당 → [batch.md](./batch.md)

---

## 5. 현업 기준 유의사항

- **T+n 정산**: 카드 매출은 즉시 지급이 아니라 정산주기(보통 T+2) 후 지급. 환불·취소 대비 버퍼.
- **수수료 구조**: 고객이 낸 금액(total) = 가맹점 지급(net) + PG 수익(commission). 원장에서 3분할 분개.
- **멱등성 2중 방어**: 결제는 `orderNo`, 정산은 `PGSTL-{merchant}-{date}`로 각각 중복 차단.
- **취소/환불 시점**: 정산 전(승인 직후) 취소와 정산 후 환불은 회계 처리가 다름(역분개 필요).
- **가맹점 상태**: 비활성 가맹점은 신규 결제 차단.
