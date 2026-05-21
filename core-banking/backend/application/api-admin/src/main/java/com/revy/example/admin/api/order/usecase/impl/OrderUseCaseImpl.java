package com.revy.example.admin.api.order.usecase.impl;

import com.revy.example.admin.api.order.payload.OrderPayload;
import com.revy.example.admin.api.order.usecase.OrderUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.account.enums.OrderSide;
import com.revy.example.domain.account.enums.OrderStatus;
import com.revy.example.domain.account.enums.OrderType;
import com.revy.example.order.command.OrderCommand;
import com.revy.example.order.command.dto.ExecuteOrderCommand;
import com.revy.example.order.command.dto.PlaceOrderCommand;
import com.revy.example.order.reader.OrderReader;
import com.revy.example.order.reader.dto.OrderResult;
import com.revy.example.order.reader.dto.OrderSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class OrderUseCaseImpl implements OrderUseCase {

    private final OrderCommand orderCommand;
    private final OrderReader  orderReader;

    @Override
    public OrderPayload.ModelResponse placeOrder(OrderPayload.PlaceRequest req) {
        Long orderId = orderCommand.placeOrder(new PlaceOrderCommand(
            req.accountId(), req.stockId(),
            req.side(), req.orderType(),
            req.quantity(), req.limitPrice(),
            req.referenceId(), Instant.now()
        ));
        return get(orderId);
    }

    @Override
    public OrderPayload.ModelResponse executeOrder(Long orderId, OrderPayload.ExecuteRequest req) {
        orderCommand.executeOrder(new ExecuteOrderCommand(
            orderId, req.executionPrice(), req.fee(), req.tax(), req.executedAt()
        ));
        return get(orderId);
    }

    @Override
    public OrderPayload.ModelResponse cancelOrder(Long orderId) {
        orderCommand.cancelOrder(orderId);
        return get(orderId);
    }

    @Override
    public OrderPayload.ModelResponse get(Long id) {
        return orderReader.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    @Override
    public ApiPageResponse<OrderPayload.ModelResponse> search(Pageable pageable,
                                                               OrderPayload.SearchRequest req) {
        OrderSearchCondition condition = OrderSearchCondition.builder()
            .accountId(req.accountId())
            .stockId(req.stockId())
            .side(parseEnum(OrderSide.class, req.side()))
            .orderType(parseEnum(OrderType.class, req.orderType()))
            .status(parseEnum(OrderStatus.class, req.status()))
            .referenceId(req.referenceId())
            .orderedAtFrom(req.orderedAtFrom())
            .orderedAtTo(req.orderedAtTo())
            .build();

        Page<OrderResult> page = orderReader.search(pageable, condition);
        return ApiPageResponse.of(
            page.getContent().stream().map(this::toResponse).toList(),
            page.getTotalElements(), page.getNumber(), page.getSize()
        );
    }

    // ── private ──────────────────────────────────────────────────

    private OrderPayload.ModelResponse toResponse(OrderResult r) {
        return new OrderPayload.ModelResponse(
            r.id(), r.accountId(), r.stockId(),
            r.side(), r.orderType(),
            r.quantity(), r.limitPrice(),
            r.filledQuantity(), r.remainingQuantity(), r.avgFillPrice(),
            r.status(), r.referenceId(),
            r.orderedAt(), r.filledAt(), r.cancelledAt()
        );
    }

    private <E extends Enum<E>> E parseEnum(Class<E> clazz, String v) {
        if (v == null || v.isBlank()) return null;
        try { return Enum.valueOf(clazz, v.toUpperCase()); } catch (IllegalArgumentException e) { return null; }
    }
}
