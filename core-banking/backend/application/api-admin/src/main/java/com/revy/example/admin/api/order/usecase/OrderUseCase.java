package com.revy.example.admin.api.order.usecase;

import com.revy.example.admin.api.order.payload.OrderPayload;
import com.revy.example.core.common.ApiPageResponse;
import org.springframework.data.domain.Pageable;

public interface OrderUseCase {

    OrderPayload.ModelResponse placeOrder(OrderPayload.PlaceRequest request);

    OrderPayload.ModelResponse executeOrder(Long orderId, OrderPayload.ExecuteRequest request);

    OrderPayload.ModelResponse cancelOrder(Long orderId);

    OrderPayload.ModelResponse get(Long id);

    ApiPageResponse<OrderPayload.ModelResponse> search(Pageable pageable, OrderPayload.SearchRequest request);
}
