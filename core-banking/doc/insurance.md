# 보험 (Insurance)

> 보험 상품 설계, 계약(청약→효력), 수익자 관리, **보험료 자동이체**, 그리고
> **보험금 청구·심사·지급**의 전체 생애주기를 다루는 도메인.

---

## 1. 용어 정리

| 용어 | 영문/필드 | 설명 |
|------|-----------|------|
| 상품 | InsuranceProduct | 판매되는 보험 상품(보장한도·기준보험료·납입주기) |
| 계약 | InsurancePolicy | 고객이 가입한 보험 증권 |
| 계약자 | userId | 보험료 납입 의무자 |
| 피보험자 | insuredUserId | 보험 대상이 되는 사람(계약자와 다를 수 있음) |
| 수익자 | Beneficiary | 보험금을 받는 사람. 1차(PRIMARY)/2차(CONTINGENT) |
| 보험료 | premium | 정기적으로 내는 돈 |
| 납입주기 | PremiumFrequency | 월/분기/반기/연/일시납 |
| 보장한도 | coverageAmount | 보험금 지급 상한 |
| 보험료 납입 | PremiumPayment | 매 주기 자동이체 레코드 |
| 청구 | InsuranceClaim | 사고 발생 후 보험금 지급 요청 |
| 승인금액 | approvedAmount | 심사 후 실제 지급 결정액(≤청구액, ≤보장한도) |

### 보험 종류(InsuranceType)
LIFE(생명), HEALTH(건강), AUTO(자동차), PROPERTY(재산), TRAVEL(여행), ANNUITY(연금), PET(반려동물)

---

## 2. 계약(Policy) 생애주기

```
PENDING(청약접수) ──activate──▶ ACTIVE(효력발생)
   │                              │  │
   │                              │  └──suspend──▶ SUSPENDED ──reactivate──▶ ACTIVE
   ├──cancel──▶ CANCELLED         │  (보험료 연체 등)
   (청약철회)                      ├──terminate──▶ TERMINATED (해지)
                                   └──expireIfDue──▶ EXPIRED (만기)
```

### 효력 발생(activate)의 핵심 검증
계약을 `ACTIVE`로 만들 때 **1차 수익자 지분 합계가 정확히 100%**인지 검증합니다.
(`validateBeneficiariesShare` — 합이 100이 아니면 `InvalidBeneficiaryShareException`)

> 보험금이 누구에게 몇 %씩 가는지 모호하면 지급 분쟁이 생깁니다. 효력 발생 전에 강제 검증.

### 만기 처리
`expireIfDue(today)` — ACTIVE 상태에서 `end_date`가 지나면 `EXPIRED`로 전환(배치가 호출).

---

## 3. 보험료 자동이체 플로우

`PremiumPayment`는 매 납입 주기의 출금 레코드입니다.

```
1. 예약        PremiumPayment.schedule(...) PENDING, referenceId 멱등성
2. 출금        billing_account에서 보험료 출금(AccountTx)
3. 납입 확정   markPaid(accountTxId, paidAt) → PAID
4. 다음 주기   Policy.advanceNextPaymentDate()  주기만큼 nextPaymentDate 전진
```

- 잔고 부족 등 실패 시 `markFailed(reason)` → FAILED, 연체 시 `markOverdue()` → OVERDUE.
- `referenceId` 유니크로 **자동이체 재시도 중복 출금 방지**.
- 정기 실행은 보험료 정산 배치(`InsurancePremiumSettlementJob`)가 담당 → [batch.md](./batch.md)
- 납입주기별 다음 납부일: 월(+1M)/분기(+3M)/반기(+6M)/연(+1Y)/일시납(없음).

---

## 4. 보험금 청구·심사·지급 플로우

`InsuranceClaim`의 상태 흐름:
```
SUBMITTED(접수) ──startReview──▶ REVIEWING(심사중)
   │                               │
   │                               ├──approve──▶ APPROVED ──markPaid──▶ PAID
   │                               └──reject───▶ REJECTED
   └─(SUBMITTED에서도 바로 approve/reject 가능)
```

```
1. 청구 접수   submit(...)  사고일·청구사유·청구액·지급계좌. status=SUBMITTED
2. 심사 시작   startReview(adminId) → REVIEWING
3. 심사 결정
   ├─ 승인 approve(approvedAmount, coverage, notes)
   │     · approvedAmount ≤ 보장한도 검증 (초과 시 예외)
   │     · status=APPROVED
   └─ 거절 reject(notes) → REJECTED
4. 지급      markPaid(accountTxId, paidAt)  지급계좌 입금 후 → PAID
```

- 승인액이 보장한도를 넘으면 `ClaimAmountExceedsCoverageException`.
- 지급은 반드시 `APPROVED` 상태에서만.
- `@Version`으로 동시 심사 충돌 방지.
- 심사자(reviewerAdminId)·심사노트(reviewNotes)·각 시각(submitted/reviewed/paid)을 감사 추적용으로 기록.

---

## 5. 수익자(Beneficiary)

- 계약(`InsurancePolicy`) 애그리거트의 일부 — `policy.addBeneficiary(...)`로만 생성.
- `sharePercent`(0~100, 소수 2자리), `beneficiaryType`(PRIMARY/CONTINGENT).
- 시스템 사용자(`beneficiaryUserId`)일 수도, 외부인(이름만)일 수도 있음.
- **1차 수익자 지분 합 = 100%** 규칙(효력 발생 시 검증).

---

## 6. 현업 기준 유의사항

- **계약자 ≠ 피보험자 ≠ 수익자**: 세 주체가 모두 다를 수 있음(예: 부모가 계약자, 자녀가 피보험자, 배우자가 수익자).
- **지분 합 100% 강제**: 효력 발생 전 1차 수익자 지분 검증으로 지급 분쟁 예방.
- **보장한도 상한**: 승인·지급액은 항상 `coverageAmount` 이하.
- **자동이체 멱등성**: 재시도가 잦은 정기 출금에서 중복 출금을 `referenceId`로 차단.
- **연체 관리**: 미납 시 OVERDUE → 계약 SUSPENDED 연동(보험료 연체 정지).
- **원장 연계**: 보험료 수취·보험금 지급은 원장으로 전기(reference_type=PREMIUM_PAYMENT / CLAIM_PAYOUT).
