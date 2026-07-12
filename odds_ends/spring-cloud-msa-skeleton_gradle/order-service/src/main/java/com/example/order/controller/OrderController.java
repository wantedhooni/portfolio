package com.example.order.controller;

import java.util.List;

import com.example.common.api.ApiResponse;
import com.example.order.domain.CreateOrderRequest;
import com.example.order.entity.OrderEntity;
import com.example.order.service.OrderService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderEntity> create(@RequestBody CreateOrderRequest req) {
        return ApiResponse.ok(orderService.create(req));
    }

    @GetMapping
    public ApiResponse<List<OrderEntity>> listByUser(@RequestParam long userId) {
        return ApiResponse.ok(orderService.listByUser(userId));
    }
}
