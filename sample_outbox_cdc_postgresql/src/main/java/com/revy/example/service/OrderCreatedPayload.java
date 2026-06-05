package com.revy.example.service;

public record OrderCreatedPayload(
    Long orderId,
    Long customerId,
    String productName,
    Integer quantity
) {
}