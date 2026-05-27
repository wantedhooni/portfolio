package com.revy.example.fx.command.dto;

import java.math.BigDecimal;

public record UpdateFxCorridorCommand(
        BigDecimal minAmount,
        /** null = 무제한 */
        BigDecimal maxAmount,
        /** null = 무제한 */
        BigDecimal dailyLimit,
        BigDecimal spreadRate
) {}
