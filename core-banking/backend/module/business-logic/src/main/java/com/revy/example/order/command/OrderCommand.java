package com.revy.example.order.command;

import com.revy.example.order.command.dto.ExecuteOrderCommand;
import com.revy.example.order.command.dto.PlaceOrderCommand;

public interface OrderCommand {

    /**
     * 주문 접수 — StockOrder 생성 (PENDING).
     * 매수 주문의 경우 가용잔고를 선점하여 중복 체결을 방지한다.
     */
    Long placeOrder(PlaceOrderCommand command);

    /**
     * 주문 체결 — 가격·수수료·세금을 받아 TradeCommand 를 통해 원장 및 포지션을 갱신하고
     * StockOrder 상태를 FILLED 로 전이한다.
     */
    void executeOrder(ExecuteOrderCommand command);

    /**
     * 주문 취소 — PENDING 상태인 주문을 CANCELLED 로 전이한다.
     * 매수 주문이면 선점된 가용잔고를 복원한다.
     */
    void cancelOrder(Long orderId);
}
