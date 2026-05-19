package com.revy.example.stock.command.dto;

public record CreateStockCommand(
        String ticker,
        String name,
        String exchange,
        String sector,
        String currency
) {}
