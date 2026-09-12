package com.revy.api_server.application.web.api.trade.usecase;

import com.revy.api_server.application.web.api.trade.payload.OrderPayload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface OrderUseCase {
    @Transactional
    OrderPayload.Res createOrder(Long userId, OrderPayload.Req req);

    @Transactional
    OrderPayload.Res cancelOrder(UUID orderId);

    @Transactional(readOnly = true)
    Page<OrderPayload.Res> listOrders(Long id, Pageable pageable);
}
