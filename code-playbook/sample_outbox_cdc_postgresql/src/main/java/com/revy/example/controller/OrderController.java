package com.revy.example.controller;

import com.revy.example.service.dto.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderService.OrderResponse createOrder(@Valid @RequestBody CreateOrderHttpRequest request) {
        return orderService.createOrder(
            new OrderService.CreateOrderRequest(request.customerId(), request.productName(), request.quantity()));
    }

    @GetMapping
    public List<OrderService.OrderResponse> findByCustomerId(@RequestParam Long customerId) {
        return orderService.findByCustomerId(customerId);
    }

    public record CreateOrderHttpRequest(
        @NotNull
        Long customerId,
        @NotBlank
        String productName,
        @NotNull
        @Min(1)
        Integer quantity
    ) {
    }
}