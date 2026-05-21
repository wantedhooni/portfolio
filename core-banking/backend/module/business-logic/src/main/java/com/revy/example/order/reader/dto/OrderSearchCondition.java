package com.revy.example.order.reader.dto;

import com.revy.example.domain.account.enums.OrderSide;
import com.revy.example.domain.account.enums.OrderStatus;
import com.revy.example.domain.account.enums.OrderType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
public class OrderSearchCondition {

    Long        accountId;
    Long        stockId;
    OrderSide   side;
    OrderType   orderType;
    OrderStatus status;
    String      referenceId;
    Instant     orderedAtFrom;
    Instant     orderedAtTo;

    @Builder
    public OrderSearchCondition(Long accountId, Long stockId, OrderSide side,
                                OrderType orderType, OrderStatus status,
                                String referenceId, Instant orderedAtFrom, Instant orderedAtTo) {
        this.accountId    = accountId;
        this.stockId      = stockId;
        this.side         = side;
        this.orderType    = orderType;
        this.status       = status;
        this.referenceId  = referenceId;
        this.orderedAtFrom = orderedAtFrom;
        this.orderedAtTo   = orderedAtTo;
    }
}
