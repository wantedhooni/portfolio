package com.revy.example.stock.reader.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StockSearchCondition {

    String ticker;
    String name;
    String exchange;
    String sector;
    String currency;
    Boolean isActive;

    @Builder
    public StockSearchCondition(String ticker, String name, String exchange,
                                String sector, String currency, Boolean isActive) {
        this.ticker   = ticker;
        this.name     = name;
        this.exchange = exchange;
        this.sector   = sector;
        this.currency = currency;
        this.isActive = isActive;
    }
}
