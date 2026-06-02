# 계좌·증권 (Account & Securities)

> 예금 계좌의 입출금·이체, 그리고 주식 주문·체결과 **FIFO 취득원가 기반 손익 계산**을 담당하는 도메인.
> 코어뱅킹에서 가장 트래픽이 많고 동시성 이슈가 큰 영역입니다.

---

## 1. 용어 정리

| 용어 | 영문/필드 | 설명 |
|------|-----------|------|
| 계좌 | Account | 고객 자금을 보관하는 단위. 실계좌(REAL)/모의계좌(VIRTUAL) |
| 잔고 | balance | 계좌의 실제 보유 금액 |
| 가용잔고 | availableBalance | 출금·주문에 즉시 쓸 수 있는 금액. (주문 선점분만큼 잔고보다 작을 수 있음) |
| 거래내역 | AccountTx | 잔고를 변동시킨 모든 거래의 명세(입금/출금/매수/매도/배당/수수료/세금) |
| 멱등성 키 | referenceId | 동일 거래 중복 반영 방지용 유니크 키 |
| 종목 | Stock | 거래 가능한 주식. 티커+거래소로 식별 |
| 주문 | StockOrder | 매수/매도 주문. PENDING → FILLED \| CANCELLED |
| 포지션 | StockPosition | 계좌×종목 단위의 보유 현황(총수량·실현손익 캐시) |
| 로트 | PositionLot | 한 번의 매수로 생긴 취득 단위. **FIFO 원가추적의 최소 단위** |
| 처분 | LotDisposal | 매도 시 특정 로트에서 차감된 기록(실현손익 포함) |
| 실현손익 | realizedPnl | 매도로 확정된 손익 = (매도가 − 취득가) × 수량 |
| 미실현손익 | unrealizedPnl | 보유 중 평가손익 = (현재가 − 취득가) × 잔량 |

---

## 2. 계좌(Account) — 잔고 관리

### 상태
`ACTIVE`(정상) → `SUSPENDED`(정지) / `CLOSED`(해지)

### 잔고가 둘인 이유: balance vs availableBalance
주식 주문을 내면 **체결 전까지 돈을 묶어둬야**(선점) 합니다. 이때:

- `balance` (실잔고) : 아직 그대로. 체결되어야 차감.
- `availableBalance` (가용잔고) : 주문 금액만큼 **즉시 차감**(선점).

> 이렇게 분리하면 "주문은 냈는데 아직 안 빠진 돈"으로 또 출금하는 사고를 막습니다.
> 현업 증권사의 **증거금/예수금** 개념과 동일한 패턴입니다.

### 주요 행위
| 메서드 | 의미 |
|--------|------|
| `deposit` / `withdraw` | 입금 / 출금 (출금 시 가용잔고 검증) |
| `reserveForOrder` | 주문 제출 시 가용잔고 선점 |
| `confirmBuy` | 체결 확정 시 실잔고 차감 |
| `creditSaleProceeds` | 매도 대금 입금 |
| `releaseReservation` | 주문 취소 시 선점 복원 |
| `creditDividend` | 배당금 입금(정지 계좌도 허용, 해지는 호출부 차단) |

동시 입출금·주문 충돌을 막기 위해 `@Version` 낙관적 락 적용.

---

## 3. 거래내역(AccountTx) — 돈의 단일 기록

모든 잔고 변동은 `AccountTx` 한 줄로 남습니다. 팩토리 메서드가 **금액 부호와 검증**을 책임집니다.

| 팩토리 | 부호 | 비고 |
|--------|------|------|
| `ofDeposit` | + | 입금 |
| `ofWithdrawal` | − | 출금 |
| `ofTransferOut` | − (이체액+수수료) | 이체 출금측 |
| `ofTransferIn` | + (이체액) | 이체 입금측, 수수료는 출금측만 부담 |
| `ofBuy` | − (가격×수량+수수료+세금) | 매수 |
| `ofSell` | + (가격×수량−수수료−세금) | 매도 |
| `ofDividend` | + (배당−세금) | 배당 |

- 금액은 **반드시 양수**(`requirePositive`), 수수료·세금은 음수 불가(`requireNonNegative`)로 생성 시점에 차단.
- `tradedAt`(실제 체결 시각)과 `createdAt`(적재 시각)을 분리 — 배치 지연 적재 대비.
- `referenceId`로 주문·이체 양측을 연결.

---

## 4. 주식 주문·체결 플로우

### 4.1 주문 → 체결 (단일 체결 모델)
이 시스템은 부분 체결 없이 **전량 체결**을 가정합니다(한 주문 = 한 체결).

```
1. 주문 제출(place)         StockOrder PENDING 생성, referenceId 멱등성
2. 가용잔고 선점(reserve)   Account.availableBalance 차감
3. 체결(fill)               avgFillPrice 확정, status=FILLED
4. 매수 거래 기록           AccountTx.ofBuy → 원장 INSERT
5. 실잔고 확정(confirmBuy)  Account.balance 차감
6. 로트 추가(addLot)        StockPosition에 PositionLot 생성
```

취소(`cancel`)는 `PENDING`에서만 가능하며 선점분을 복원(`releaseReservation`)합니다.

### 4.2 매도 — FIFO 취득원가 (핵심)
매도는 **먼저 산 로트부터 소진**(First-In-First-Out)합니다. 한 번의 매도가 여러 로트에 걸칠 수 있습니다.

```
매도 100주 요청
 ├─ Lot#1 (60주, 취득가 1,000) → 60주 소진, 실현손익=(매도가−1,000)×60
 └─ Lot#2 (40주, 취득가 1,200) → 40주 소진, 실현손익=(매도가−1,200)×40
StockPosition.realizedPnl 에 두 손익 합산, totalQuantity −100
```

- `PositionLot`은 `bought_at ASC, id ASC`로 정렬되어 FIFO 보장.
- 각 로트 처분마다 `LotDisposal`을 만들어 **세무·감사 추적**.
- 동시 매도가 같은 로트 잔량을 깎아 음수가 되는 것을 `@Version`으로 방지.

> **왜 FIFO인가?** 한국 세법·회계에서 주식 양도손익·취득원가 산정의 기본이 선입선출이며,
> 로트 단위로 보관하면 "어느 매수분이 언제 얼마에 팔렸는지"를 정확히 추적할 수 있습니다.

### 4.3 배당
배당은 주문 흐름과 무관하게 `creditDividend`로 입금되며, **정지(SUSPENDED) 계좌에도 지급**됩니다
(해지 계좌만 호출부에서 차단).

---

## 5. 현업 기준 유의사항

- **잔고 정합성**: `balance ≥ availableBalance`는 항상 성립해야 하며, 선점/복원이 짝을 이뤄야 함.
- **포지션 캐시 검증**: `StockPosition.totalQuantity`는 활성 로트 잔량 합과 일치해야 함(정합성 캐시).
- **체결 멱등성**: 주문 `referenceId`가 체결 `AccountTx.referenceId`로 재사용되어 중복 체결 차단.
- **반올림**: 체결 단가는 소수 4자리 `HALF_UP`. 통화별 자릿수와 별개의 내부 정밀도.
- **원장 연계**: 매수/매도 거래는 이후 증권 정산 배치를 통해 총계정원장으로 전기됨 → [batch.md](./batch.md), [ledger.md](./ledger.md)
