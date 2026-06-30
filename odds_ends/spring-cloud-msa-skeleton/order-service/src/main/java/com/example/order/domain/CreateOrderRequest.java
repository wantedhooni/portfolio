package com.example.order.domain;

public record CreateOrderRequest(long userId, String symbol, long quantity) {}
