package com.revy.example.fx.reader.dto;

import com.revy.example.domain.fx.Currency;

public record CurrencyResult(
        Long id,
        String code,
        String name,
        String symbol,
        Integer decimalPlaces,
        boolean isActive
) {
    public static CurrencyResult from(Currency c) {
        return new CurrencyResult(c.getId(), c.getCode(), c.getName(), c.getSymbol(),
                                  c.getDecimalPlaces(), c.isActive());
    }
}
