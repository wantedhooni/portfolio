package com.revy.example.trade.command;

import com.revy.example.trade.command.dto.BuyCommand;
import com.revy.example.trade.command.dto.DividendCommand;
import com.revy.example.trade.command.dto.SellCommand;

import java.math.BigDecimal;

public interface TradeCommand {

    /**
     * 매수 — 가용잔고 선점 → 원장 INSERT → 로트 추가 → 실잔고 확정.
     * 단일 트랜잭션 / referenceId 멱등성.
     */
    void buy(BuyCommand command);

    /**
     * 매도 (FIFO) — 원장 INSERT → 활성 로트 fetch join → 로트 소진 → 매도대금 입금.
     * 단일 트랜잭션 / referenceId 멱등성.
     */
    void sell(SellCommand command);

    /** 배당금 입금 — 정지 계좌에도 처리 */
    void dividend(DividendCommand command);

    /** 주문 취소 보상 — 가용잔고 복원 */
    void releaseReservation(Long accountId, BigDecimal amount);
}
