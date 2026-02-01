package com.revy.api_server.application.web.api.trade.usecase.impl;

import com.revy.securities.domain.account.Account;
import com.revy.securities.domain.account.enums.AccountStatus;
import com.revy.securities.domain.trade.Order;
import com.revy.securities.domain.trade.enums.OrderType;
import com.revy.securities.domain.trade.repo.OrderFillRepo;
import com.revy.securities.domain.trade.repo.OrderRepo;
import com.revy.securities.domain.trade.repo.PositionRepo;
import com.revy.securities.domain.trade.repo.TradeRepo;
import com.revy.api_server.application.web.api.trade.payload.OrderPayload;
import com.revy.api_server.application.web.api.trade.usecase.OrderUseCase;
import com.revy.api_server.application.exception.TradeException;
import com.revy.api_server.application.support.AccountSupport;
import com.revy.common.enums.Currency;
import com.revy.common.utils.BigDecimalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderUserCaseImpl implements OrderUseCase {

    private final OrderRepo orderRepo;
    private final OrderFillRepo orderFillRepo;
    private final PositionRepo positionRepo;
    private final TradeRepo tradeRepo;
    private final AccountSupport accountSupport;

    // createOrder

    @Transactional
    @Override
    public OrderPayload.Res createOrder(Long userId, OrderPayload.Req req) {
        log.debug("createOrder called with userId: {} and req: {}", userId, req);

        Account account = accountSupport.findOneByOwnerIdAndAccountNo(userId, req.accountNo());

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new TradeException("account not active");
        }
        if (account.getCurrency() != req.currency()) {
            throw new TradeException(TradeException.TracdeErrorCode.TRADE_CURRENCY_MISMATCH);
        }

        BigDecimal totalOrderPrice = BigDecimal.ZERO;
        switch (req.type()) {
            case OrderType.LIMIT -> {
                if (req.limitPriceAmount() == null || req.limitPriceAmount().signum() <= 0) {
                    throw new TradeException("LIMIT 주문은 limitPriceAmount가 필요합니다.");
                }
                totalOrderPrice = getTotalOrderPrice(req);
                log.debug("limit price: {}, qty: {}, totalOrderPrice: {}", req.limitPriceAmount(), req.qty(), totalOrderPrice);
                if(BigDecimalUtil.isGreaterThan(totalOrderPrice, account.getAvailableCash())){
                    throw new TradeException(TradeException.TracdeErrorCode.INSUFFICIENT_FUNDS);
                }
            }
            case OrderType.MARKET -> {
                if (req.limitPriceAmount() != null) {
                    throw new TradeException("MARKET 주문에는 limitPriceAmount를 넣지 마세요.");
                }
            }
            default -> throw new TradeException("지원하지 않는 주문 유형입니다: " + req.type());
        }

        if(req.type() == OrderType.LIMIT){
            account = accountSupport.updateBuyOrderAccountBalance(account, totalOrderPrice);
        }

        Order newOrder = Order.create(userId, account.getId(), req.symbol(), req.side(), req.type(), req.tif(),
                                      req.qty(), req.limitPriceAmount(), req.currency());
        newOrder = orderRepo.save(newOrder);
        return Mapper.convertOrderPayloadRes(newOrder, account.getAccountNo());

    }

    /**
     * 주문 취소
     * - FILLED/CANCELED/REJECTED는 취소 불가
     */
    @Transactional
    @Override
    public OrderPayload.Res cancelOrder(UUID orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new TradeException(TradeException.TracdeErrorCode.ORDER_NOT_FOUND));
        order.cancel();
        Account account = accountSupport.findById(order.getAccountId());
        if(order.getType() == OrderType.LIMIT){
            BigDecimal totalOrderPrice = getTotalOrderPrice(order);
            accountSupport.updateCancelOrderAccountBalance(account, totalOrderPrice);
        }

        return Mapper.convertOrderPayloadRes(order, account.getAccountNo());
    }

    /**
     * 계좌별 주문 목록(페이징)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderPayload.Res> listOrders(Long userId, Pageable pageable) {
        return orderRepo.findOrdersByUserId(userId, pageable).map(order -> {
            Account account = accountSupport.findById(order.getAccountId());
            return Mapper.convertOrderPayloadRes(order, account.getAccountNo());
        });
    }


    /** private */
    private BigDecimal getTotalOrderPrice(OrderPayload.Req req){
        return getTotalOrderPrice(req.limitPriceAmount(), req.qty(), req.currency());
    }
    private BigDecimal getTotalOrderPrice(Order order){
        return getTotalOrderPrice(order.getLimitPriceAmount(), order.getQty(), order.getCurrency());
    }
    private BigDecimal getTotalOrderPrice(BigDecimal limitPriceAmount,
                                          BigDecimal qty,
                                          Currency currency){
        Assert.notNull(limitPriceAmount, "limitPriceAmount is null");
        Assert.notNull(qty, "qty is null");
        Assert.notNull(currency, "currency is null");
        return limitPriceAmount.multiply(qty).setScale(currency.getDigits(), RoundingMode.HALF_EVEN);
    }



    static class Mapper {

        public static OrderPayload.Res convertOrderPayloadRes(Order newOrder, String accountNo) {
            return new OrderPayload.Res(newOrder.getId(), accountNo, newOrder.getSymbol(), newOrder.getSide(),
                                        newOrder.getType(), newOrder.getTif(), newOrder.getStatus(), newOrder.getQty(),
                                        newOrder.getLimitPriceAmount(), newOrder.getCurrency().name(),
                                        newOrder.getFilledQty(), newOrder.getAvgFillPriceAmount());
        }
    }
}

