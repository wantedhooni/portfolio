# 총계정원장 (General Ledger)

> **복식부기(Double-Entry Bookkeeping)** 기반의 회계 원장. 모든 자금 이동은 차변/대변이
> 일치하는 분개로 기록되며, 회계기간 마감과 역분개를 지원합니다. 금융 시스템의 "회계적 진실".

---

## 1. 용어 정리

| 용어 | 영문/필드 | 설명 |
|------|-----------|------|
| 계정과목 | LedgerAccount | 회계 분류 단위(현금, 예수금, 수수료수익 등). 코드로 계층 표현 |
| 계정 분류 | AccountCategory | 자산/부채/자본/수익/비용 |
| 정상잔액 | NormalBalance | 계정이 증가하는 방향(차변 DEBIT / 대변 CREDIT) |
| 분개 | JournalEntry | 하나의 회계 거래(헤더). 여러 분개라인을 가짐 |
| 분개라인 | JournalLine | 차변 또는 대변 한 줄 |
| 차변 | Debit | 왼쪽. 자산·비용의 증가 |
| 대변 | Credit | 오른쪽. 부채·자본·수익의 증가 |
| 전기 | Posting | 분개를 장부에 확정 반영(DRAFT→POSTED) |
| 회계기간 | AccountingPeriod | 회계연도×월(period). OPEN/CLOSED |
| 마감 | Close | 기간을 닫아 더 이상 분개 불가하게 함 |
| 역분개 | Reversal | 잘못된 분개를 차/대변 반대로 상쇄 |

---

## 2. 복식부기의 핵심 규칙

> **모든 분개는 Σ차변 = Σ대변** 이어야 한다. (대차평형의 원리)

### 정상잔액 방향
| 분류 | 정상잔액 | 증가 시 | 예시 계정 |
|------|----------|---------|-----------|
| 자산 ASSET | 차변 | 차변 | 현금, 고객예수금 |
| 부채 LIABILITY | 대변 | 대변 | 가맹점정산부채 |
| 자본 EQUITY | 대변 | 대변 | 자본금 |
| 수익 REVENUE | 대변 | 대변 | 수수료수익, 환전수익 |
| 비용 EXPENSE | 차변 | 차변 | 지급수수료 |

`LedgerAccount` 생성 시 `category`로부터 `normalBalance`가 자동 파생됩니다.

### 분개라인 불변식
한 라인은 **차변·대변 중 정확히 한쪽만 양수**, 다른 쪽은 0이어야 합니다.
(`JournalLine.validateAmounts` — 둘 다 양수거나 둘 다 0이면 예외)

---

## 3. 계정 코드 체계

`account_code`는 문자열로 계층을 표현합니다. 예:
```
1000     자산
 1001    현금
 1010    고객예수금
2000     부채
 2002    가맹점정산부채
4000     수익
 4002    수수료수익
```
`parentId`로 상위 계정을 참조해 트리 구조를 만들 수 있습니다.

---

## 4. 업무 플로우

### 4.1 분개 생성·전기 (`createAndPostJournal`)
```
1. 기간 자동 선택   entry_date가 속한 OPEN 기간을 찾음 (없으면 예외)
2. 기간 OPEN 검증   period.validateOpen()  ← 마감된 기간엔 분개 불가
3. DRAFT 분개 생성  JournalEntry.draft(...)
4. 라인 추가        각 라인의 LedgerAccount 존재·활성 검증 후 addDebit/addCredit
5. persist          cascade로 라인 동시 저장
6. post()           최소 2라인 + Σ차변=Σ대변 검증 → POSTED, postedAt 기록
```

**예시 분개 — 주식 매수 수수료 수취**
| 라인 | 계정 | 차변 | 대변 |
|------|------|------|------|
| 1 | 현금(1001) | 1,000 | |
| 2 | 수수료수익(4002) | | 1,000 |

### 4.2 회계기간 마감 (`closePeriod`)
- 월 마감 시 `AccountingPeriod`를 `CLOSED`로 전환, 마감 관리자·시각 기록.
- 마감 후 그 기간의 `entry_date`로는 새 분개를 전기할 수 없음.
- 멱등: 이미 CLOSED면 아무 일도 하지 않음.

### 4.3 역분개 (`reverseJournal`)
이미 POSTED된 분개를 수정해야 할 때, **원본을 건드리지 않고** 반대 분개를 추가합니다.
```
1. 원본 분개 조회 (POSTED 상태여야 함)
2. 원본 기간 OPEN 검증
3. createReversal()  차변↔대변을 뒤집은 새 DRAFT 생성
4. persist + post()  역분개 전기
5. markReversedBy()  원본 status=REVERSED, 상호 참조(reverses/reversedBy) 연결
```

> **왜 역분개인가?** 회계에서는 확정된 기록을 삭제·수정하지 않습니다(감사 추적·무결성).
> 항상 "상쇄 분개"를 추가하는 방식으로 정정합니다.

---

## 5. 상태 흐름

```
JournalEntry:  DRAFT ──post──▶ POSTED ──markReversedBy──▶ REVERSED
AccountingPeriod:  OPEN ──close──▶ CLOSED
```

---

## 6. 현업 기준 유의사항

- **대차평형 강제**: `post()`에서 Σ차변≠Σ대변이면 `UnbalancedJournalEntryException`. 깨진 분개는 저장 불가.
- **마감 무결성**: 마감된 기간 수정 시도는 `ClosedPeriodException`. 월마감 후 소급 분개 방지.
- **원천 추적**: `referenceType`/`referenceId`로 원천 거래(ACCOUNT_TX, FX_CONVERSION, PREMIUM_PAYMENT, CLAIM_PAYOUT, PG_SETTLEMENT 등)를 역추적.
- **불변 원장**: 전기된 분개는 수정·삭제 대신 역분개로만 정정.
- **자동 전기**: 실거래(매매·환전·보험·PG)는 배치가 원장으로 전기 → [batch.md](./batch.md)
