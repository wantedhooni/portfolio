package com.revy.example.fx.command.dto;

public record RegisterCurrencyCommand(
        String code,
        String name,
        String symbol,
        Integer decimalPlaces
) {}
