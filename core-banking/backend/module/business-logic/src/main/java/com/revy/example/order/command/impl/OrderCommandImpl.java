package com.revy.example.order.command.impl;

import com.revy.example.account.reader.AccountReader;
import com.revy.example.domain.account.Account;
import com.revy.example.domain.account.Stock;
import com.revy.example.domain.account.StockOrder;
import com.revy.example.domain.account.enums.OrderSide;
import com.revy.example.domain.account.exception.AccountNotFoundException;
import com.revy.example.domain.account.exception.OrderNotFoundException;
import com.revy.example.domain.account.exception.StockNotFoundException;
import com.revy.example.order.command.OrderCommand;
import com.revy.example.order.command.dto.ExecuteOrderCommand;
import com.revy.example.order.command.dto.PlaceOrderCommand;
import com.revy.example.trade.command.TradeCommand;
import com.revy.example.trade.command.dto.BuyCommand;
import com.revy.example.trade.command.dto.SellCommand;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class OrderCommandImpl implements OrderCommand {

    private final EntityManager entityManager;
    private final AccountReader accountReader;
    private final TradeCommand  tradeCommand;

    @Override
    public Long placeOrder(PlaceOrderCommand cmd) {
        // 계좌·종목 존재 확인
        Account account = loadAccount(cmd.accountId());
        Stock   stock   = loadStock(cmd.stockId());

        // 매수 주문: 가용잔고 선점 (지정가 → limitPrice×qty, 시장가 → 클라이언트 제공 limitPrice 활용)
        if (cmd.side() == OrderSide.BUY) {
            if (!stock.isActive()) throw new com.revy.example.domain.account.exception.StockNotActiveException();
            java.math.BigDecimal reserveAmount = cmd.limitPrice() != null
                ? cmd.limitPrice().multiply(cmd.quantity())
                : java.math.BigDecimal.ZERO;
            if (reserveAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
                account.reserveForOrder(reserveAmount);
            }
        }

        StockOrder order = StockOrder.place(
            cmd.accountId(), cmd.stockId(),
            cmd.side(), cmd.orderType(),
            cmd.quantity(), cmd.limitPrice(),
            cmd.referenceId(), cmd.orderedAt()
        );
        entityManager.persist(order);
        // TODO:REVY - EVENT 발행(OrderPlaced)
        return order.getId();
    }

    @Override
    public void executeOrder(ExecuteOrderCommand cmd) {
        StockOrder order = loadOrder(cmd.orderId());

        // 주문의 referenceId 를 체결 AccountTx.referenceId 로 재사용
        String execRefId = order.getReferenceId();

        if (order.getSide() == OrderSide.BUY) {
            // 선점 잔고 해제 후 실제 매수 처리 (TradeCommand 내부에서 reserveForOrder + confirmBuy 호출)
            Account account = loadAccount(order.getAccountId());
            if (order.getLimitPrice() != null) {
                account.releaseReservation(order.getLimitPrice().multiply(order.getQuantity()));
            }
            tradeCommand.buy(new BuyCommand(
                order.getAccountId(), order.getStockId(),
                order.getQuantity(), cmd.executionPrice(),
                cmd.fee(), cmd.tax(),
                execRefId, cmd.executedAt()
            ));
        } else {
            tradeCommand.sell(new SellCommand(
                order.getAccountId(), order.getStockId(),
                order.getQuantity(), cmd.executionPrice(),
                cmd.fee(), cmd.tax(),
                execRefId, cmd.executedAt()
            ));
        }

        order.fill(cmd.executionPrice());
        // TODO:REVY - EVENT 발행(OrderFilled)
    }

    @Override
    public void cancelOrder(Long orderId) {
        StockOrder order = loadOrder(orderId);

        // 매수 주문 취소: 선점 잔고 복원
        if (order.getSide() == OrderSide.BUY && order.getLimitPrice() != null) {
            java.math.BigDecimal reserved = order.getLimitPrice().multiply(order.getQuantity());
            if (reserved.compareTo(java.math.BigDecimal.ZERO) > 0) {
                tradeCommand.releaseReservation(order.getAccountId(), reserved);
            }
        }

        order.cancel();
        // TODO:REVY - EVENT 발행(OrderCancelled)
    }

    // ── 내부 로딩 ─────────────────────────────────────────────────

    private StockOrder loadOrder(Long orderId) {
        StockOrder o = entityManager.find(StockOrder.class, orderId);
        if (o == null) throw new OrderNotFoundException();
        return o;
    }

    private Account loadAccount(Long accountId) {
        Account a = entityManager.find(Account.class, accountId);
        if (a == null) throw new AccountNotFoundException();
        return a;
    }

    private Stock loadStock(Long stockId) {
        Stock s = entityManager.find(Stock.class, stockId);
        if (s == null) throw new StockNotFoundException();
        return s;
    }
}
