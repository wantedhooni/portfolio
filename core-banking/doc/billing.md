# 청구·정산 (Billing & Settlement)

> 계좌 단위의 **월별 수수료 청구서(Invoice)** 발행과, 거래/수수료 단위의 **정산(Settlement)**
> 레코드를 관리하는 도메인. "고객에게 무엇을 청구하고, 무엇을 정산했는가"를 기록합니다.

---

## 1. 용어 정리

| 용어 | 영문/필드 | 설명 |
|------|-----------|------|
| 청구서 | BillingInvoice | 계좌×월(billingPeriod) 단위 수수료 집계 문서 |
| 청구항목 | BillingItem | 청구서 한 줄(수수료 종류·수량·단가) |
| 청구기간 | billingPeriod | YYYY-MM |
| 소계 | subtotal | 세전 합계 |
| 세액 | taxAmount | subtotal × taxRate |
| 청구총액 | totalAmount | subtotal + taxAmount |
| 정산 | Settlement | 특정 거래/기간의 금액 정산 레코드 |
| 총액 | grossAmount | 수수료·세금 차감 전 |
| 순정산액 | netAmount | gross ∓ fee ∓ tax |

### 청구 항목 종류(BillingItemType)
ACCOUNT_FEE(계좌유지), TRADE_COMMISSION(매매수수료), FX_SPREAD_FEE(환전스프레드),
TRANSFER_FEE(이체수수료), INSURANCE_PREMIUM(보험료), SERVICE_FEE(기타)

### 정산 종류(SettlementType)
TRADE(주식체결), FX(외환), INSURANCE_PREMIUM(보험료), FEE(수수료)

---

## 2. 청구서(BillingInvoice) — 애그리거트

청구서는 여러 청구항목을 묶는 **애그리거트 루트**입니다. 항목 추가 시 합계가 자동 재계산됩니다.

### 상태 흐름
```
DRAFT(초안) ──issue──▶ ISSUED(발행/납부대기) ──markPaid──▶ PAID(납부완료)
   │                       │
   └──cancel──▶ CANCELLED  └──markOverdue──▶ OVERDUE ──markPaid──▶ PAID
```

### 플로우
```
1. 생성        BillingInvoice.create(account, period, currency)  status=DRAFT
2. 항목 추가   addItem(type, desc, qty, unitPrice, taxRate)
               · BillingItem.amount = qty × unitPrice
               · recalculate: subtotal/tax/total 자동 갱신
3. 발행        issue(dueDate)  ← DRAFT만, 항목 0건이면 불가. ISSUED
4. 납부        markPaid(paidAt)  ← ISSUED/OVERDUE → PAID
```

규칙:
- **DRAFT에서만 항목 추가/발행** 가능(발행 후 금액 변경 차단).
- 항목 없는 청구서는 발행 불가.
- **PAID/CANCELLED는 취소 불가**(이미 납부/취소된 건 보호).

---

## 3. 정산(Settlement)

거래 한 건 또는 기간을 정산한 결과 레코드입니다. 청구서와 달리 **단일 레코드** 모델.

### 상태 흐름
```
PENDING ──settle──▶ SETTLED
   │  └──fail(reason)──▶ FAILED
   └──cancel──▶ CANCELLED
```

- `netAmount = grossAmount ∓ feeAmount ∓ taxAmount` (정산 종류에 따라 부호).
- `referenceId`로 원천 거래(`AccountTx.referenceId` 등)와 연결.
- 모든 전이는 **PENDING에서만** 가능(SETTLED/FAILED/CANCELLED은 종료 상태).

> **청구(Invoice) vs 정산(Settlement) 구분**
> - 청구: "고객에게 받을 돈"을 월 단위로 모아 **청구서로 발행**(여러 항목).
> - 정산: "거래 결과로 주고받을 금액"을 **건/기간 단위로 확정**(단일 레코드).

---

## 4. 현업 기준 유의사항

- **상태 가드 일관성**: 모든 상태 전이는 현재 상태를 검증하고 위반 시 예외(불법 전이 차단).
- **합계 정합성**: `totalAmount = subtotal + taxAmount`, 항목 변경 시 항상 재계산.
- **반올림**: 금액은 소수 4자리 `HALF_UP`.
- **종료 상태 보호**: PAID/SETTLED/CANCELLED 등 종료 상태는 되돌리지 않음.
- **배치 연계**: 증권 정산·보험료 정산·PG 정산 배치가 정산 레코드를 생성 → [batch.md](./batch.md)
