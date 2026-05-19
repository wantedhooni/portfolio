package com.revy.example.stock.command.dto;

import java.math.BigDecimal;

public record UpdateMarketDataCommand(
        Long stockId,
        BigDecimal lastPrice,
        BigDecimal marketCap
) {}
